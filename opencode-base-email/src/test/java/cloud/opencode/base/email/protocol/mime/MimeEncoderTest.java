package cloud.opencode.base.email.protocol.mime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MimeEncoder}.
 */
class MimeEncoderTest {

    // ========== Base64 ==========

    @Nested
    class EncodeBase64Tests {

        @Test
        void shouldEncodeEmptyArray() {
            String result = MimeEncoder.encodeBase64(new byte[0]);
            assertThat(result).isEmpty();
        }

        @Test
        void shouldEncodeSmallData() {
            byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);
            String encoded = MimeEncoder.encodeBase64(data);
            assertThat(encoded).isEqualTo("SGVsbG8gV29ybGQ=");
        }

        @Test
        void shouldEncodeLargeDataWithLineWrapping() {
            // 100 bytes will produce base64 > 76 chars, triggering MIME line wrapping
            byte[] data = new byte[100];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }
            String encoded = MimeEncoder.encodeBase64(data);
            // MIME base64 wraps at 76 chars with \r\n
            assertThat(encoded).contains("\r\n");
            // Each line (except possibly last) should be <= 76 chars
            String[] lines = encoded.split("\r\n");
            for (String line : lines) {
                assertThat(line.length()).isLessThanOrEqualTo(76);
            }
        }

        @Test
        void shouldRoundTripEncodeAndDecode() {
            byte[] original = "The quick brown fox jumps over the lazy dog.".getBytes(StandardCharsets.UTF_8);
            String encoded = MimeEncoder.encodeBase64(original);
            byte[] decoded = MimeEncoder.decodeBase64(encoded);
            assertThat(decoded).isEqualTo(original);
        }

        @Test
        void shouldRoundTripBinaryData() {
            byte[] original = new byte[256];
            for (int i = 0; i < 256; i++) {
                original[i] = (byte) i;
            }
            String encoded = MimeEncoder.encodeBase64(original);
            byte[] decoded = MimeEncoder.decodeBase64(encoded);
            assertThat(decoded).isEqualTo(original);
        }
    }

    @Nested
    class DecodeBase64Tests {

        @Test
        void shouldDecodeValidBase64() {
            byte[] result = MimeEncoder.decodeBase64("SGVsbG8gV29ybGQ=");
            assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("Hello World");
        }

        @Test
        void shouldDecodeBase64WithWhitespace() {
            // MIME decoder should handle line breaks within base64
            String base64WithWrapping = "SGVs\r\nbG8g\r\nV29ybGQ=";
            byte[] result = MimeEncoder.decodeBase64(base64WithWrapping);
            assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("Hello World");
        }

        @Test
        void shouldDecodeEmptyString() {
            byte[] result = MimeEncoder.decodeBase64("");
            assertThat(result).isEmpty();
        }
    }

    // ========== Quoted-Printable ==========

    @Nested
    class EncodeQuotedPrintableTests {

        @Test
        void shouldPassThroughAsciiText() {
            String text = "Hello World";
            String encoded = MimeEncoder.encodeQuotedPrintable(text, "UTF-8");
            assertThat(encoded).isEqualTo("Hello World");
        }

        @Test
        void shouldEncodeEqualsSign() {
            String text = "a=b";
            String encoded = MimeEncoder.encodeQuotedPrintable(text, "UTF-8");
            assertThat(encoded).isEqualTo("a=3Db");
        }

        @Test
        void shouldEncodeNonAsciiCharacters() {
            // UTF-8 encoding of a non-ASCII char
            String text = "\u00E9"; // e-acute
            String encoded = MimeEncoder.encodeQuotedPrintable(text, "UTF-8");
            assertThat(encoded).isEqualTo("=C3=A9");
        }

        @Test
        void shouldWrapLongLines() {
            // Build a long line of printable chars
            String text = "a".repeat(80);
            String encoded = MimeEncoder.encodeQuotedPrintable(text, "UTF-8");
            // Should contain a soft line break
            assertThat(encoded).contains("=\r\n");
            // No content line should exceed 76 chars
            String[] lines = encoded.split("\r\n");
            for (String line : lines) {
                // Lines ending with = soft break: the = is at position 75 (0-indexed)
                assertThat(line.length()).isLessThanOrEqualTo(76);
            }
        }

        @Test
        void shouldEncodeTrailingWhitespace() {
            // Trailing space before CRLF should be encoded
            String text = "Hello \r\nWorld";
            String encoded = MimeEncoder.encodeQuotedPrintable(text, "UTF-8");
            // The space before \r\n should become =20
            assertThat(encoded).contains("=20\r\n");
            assertThat(encoded).contains("World");
        }

        @Test
        void shouldEncodeTrailingWhitespaceAtEndOfString() {
            String text = "Hello ";
            String encoded = MimeEncoder.encodeQuotedPrintable(text, "UTF-8");
            // Trailing space at EOF must be encoded
            assertThat(encoded).endsWith("=20");
        }

        @Test
        void shouldEncodeTrailingTab() {
            String text = "Hello\t";
            String encoded = MimeEncoder.encodeQuotedPrintable(text, "UTF-8");
            assertThat(encoded).endsWith("=09");
        }

        @Test
        void shouldPassThroughCrLf() {
            String text = "Line1\r\nLine2";
            String encoded = MimeEncoder.encodeQuotedPrintable(text, "UTF-8");
            assertThat(encoded).isEqualTo("Line1\r\nLine2");
        }

        @Test
        void shouldEncodeEmptyString() {
            String encoded = MimeEncoder.encodeQuotedPrintable("", "UTF-8");
            assertThat(encoded).isEmpty();
        }

        @Test
        void shouldHandleTabInMiddle() {
            String text = "Hello\tWorld";
            String encoded = MimeEncoder.encodeQuotedPrintable(text, "UTF-8");
            // Tab not at end of line should pass through
            assertThat(encoded).isEqualTo("Hello\tWorld");
        }

        @Test
        void shouldEncodeControlCharacters() {
            String text = "Hello\u0001World";
            String encoded = MimeEncoder.encodeQuotedPrintable(text, "UTF-8");
            assertThat(encoded).contains("=01");
        }

        @Test
        void shouldHandleIso8859Charset() {
            String text = "\u00E9"; // e-acute
            String encoded = MimeEncoder.encodeQuotedPrintable(text, "ISO-8859-1");
            // In ISO-8859-1, e-acute is 0xE9 (single byte)
            assertThat(encoded).isEqualTo("=E9");
        }
    }

    @Nested
    class DecodeQuotedPrintableTests {

        @Test
        void shouldDecodeSimpleText() {
            String result = MimeEncoder.decodeQuotedPrintable("Hello World");
            assertThat(result).isEqualTo("Hello World");
        }

        @Test
        void shouldDecodeHexSequences() {
            String result = MimeEncoder.decodeQuotedPrintable("a=3Db");
            assertThat(result).isEqualTo("a=b");
        }

        @Test
        void shouldDecodeSoftLineBreakCrLf() {
            String result = MimeEncoder.decodeQuotedPrintable("Hello=\r\n World");
            assertThat(result).isEqualTo("Hello World");
        }

        @Test
        void shouldDecodeSoftLineBreakBareLf() {
            // Bare LF soft break is only handled when fewer than 2 chars remain after '='
            // When 2+ chars remain, the code tries hex decode first and falls through
            String result = MimeEncoder.decodeQuotedPrintable("end=\n");
            assertThat(result).isEqualTo("end");
        }

        @Test
        void shouldDecodeNonAsciiUtf8() {
            // e-acute in UTF-8 is C3 A9
            String result = MimeEncoder.decodeQuotedPrintable("caf=C3=A9");
            assertThat(result).isEqualTo("caf\u00E9");
        }

        @Test
        void shouldDecodeWithSpecificCharset() {
            // e-acute in ISO-8859-1 is E9
            String result = MimeEncoder.decodeQuotedPrintable("caf=E9",
                    java.nio.charset.Charset.forName("ISO-8859-1"));
            assertThat(result).isEqualTo("caf\u00E9");
        }

        @Test
        void shouldHandleMalformedEqualsAtEnd() {
            // '=' at end with no hex chars
            String result = MimeEncoder.decodeQuotedPrintable("test=");
            assertThat(result).isEqualTo("test=");
        }

        @Test
        void shouldHandleMalformedEqualsWithInvalidHex() {
            // '=' followed by non-hex chars
            String result = MimeEncoder.decodeQuotedPrintable("test=ZZ");
            assertThat(result).isEqualTo("test=ZZ");
        }

        @Test
        void shouldHandleNonAsciiCharDirectly() {
            // A non-ASCII char appearing directly in the QP stream (> 0x7F)
            // This triggers the multi-byte write path
            String input = "Hello\u00E9World";
            String result = MimeEncoder.decodeQuotedPrintable(input);
            assertThat(result).isEqualTo("Hello\u00E9World");
        }

        @Test
        void shouldRoundTripQuotedPrintable() {
            String original = "Caf\u00E9 au lait = good!";
            String encoded = MimeEncoder.encodeQuotedPrintable(original, "UTF-8");
            String decoded = MimeEncoder.decodeQuotedPrintable(encoded);
            assertThat(decoded).isEqualTo(original);
        }

        @Test
        void shouldDecodeLowercaseHex() {
            String result = MimeEncoder.decodeQuotedPrintable("=c3=a9");
            assertThat(result).isEqualTo("\u00E9");
        }
    }

    // ========== RFC 2047 Encoded Words ==========

    @Nested
    class EncodeWordTests {

        @Test
        void shouldReturnNullForNull() {
            assertThat(MimeEncoder.encodeWord(null, "UTF-8")).isNull();
        }

        @Test
        void shouldReturnEmptyForEmpty() {
            assertThat(MimeEncoder.encodeWord("", "UTF-8")).isEmpty();
        }

        @Test
        void shouldNotEncodeAsciiOnly() {
            String result = MimeEncoder.encodeWord("Hello World", "UTF-8");
            assertThat(result).isEqualTo("Hello World");
        }

        @Test
        void shouldEncodeNonAsciiWord() {
            String result = MimeEncoder.encodeWord("\u4F60\u597D", "UTF-8");
            assertThat(result).startsWith("=?UTF-8?B?");
            assertThat(result).endsWith("?=");
        }

        @Test
        void shouldRoundTripWithDecodeWord() {
            String original = "\u4F60\u597D\u4E16\u754C"; // "Hello World" in Chinese
            String encoded = MimeEncoder.encodeWord(original, "UTF-8");
            String decoded = MimeEncoder.decodeWord(encoded);
            assertThat(decoded).isEqualTo(original);
        }

        @Test
        void shouldEncodeMixedAsciiAndNonAscii() {
            String original = "Hello \u4E16\u754C";
            String encoded = MimeEncoder.encodeWord(original, "UTF-8");
            assertThat(encoded).startsWith("=?UTF-8?B?");
            String decoded = MimeEncoder.decodeWord(encoded);
            assertThat(decoded).isEqualTo(original);
        }
    }

    @Nested
    class DecodeWordTests {

        @Test
        void shouldReturnNullForNull() {
            assertThat(MimeEncoder.decodeWord(null)).isNull();
        }

        @Test
        void shouldReturnEmptyForEmpty() {
            assertThat(MimeEncoder.decodeWord("")).isEmpty();
        }

        @Test
        void shouldReturnNonEncodedWordAsIs() {
            assertThat(MimeEncoder.decodeWord("Hello World")).isEqualTo("Hello World");
        }

        @Test
        void shouldDecodeBase64EncodedWord() {
            // "Hello" in base64
            String encoded = "=?UTF-8?B?SGVsbG8=?=";
            assertThat(MimeEncoder.decodeWord(encoded)).isEqualTo("Hello");
        }

        @Test
        void shouldDecodeLowercaseBEncoding() {
            String encoded = "=?UTF-8?b?SGVsbG8=?=";
            assertThat(MimeEncoder.decodeWord(encoded)).isEqualTo("Hello");
        }

        @Test
        void shouldDecodeQuotedPrintableEncodedWord() {
            // "Hello World" in Q-encoding (underscore = space)
            String encoded = "=?UTF-8?Q?Hello_World?=";
            assertThat(MimeEncoder.decodeWord(encoded)).isEqualTo("Hello World");
        }

        @Test
        void shouldDecodeLowercaseQEncoding() {
            String encoded = "=?UTF-8?q?Hello_World?=";
            assertThat(MimeEncoder.decodeWord(encoded)).isEqualTo("Hello World");
        }

        @Test
        void shouldDecodeQEncodingWithHexEscapes() {
            // =3D is '=' in Q-encoding
            String encoded = "=?UTF-8?Q?a=3Db?=";
            assertThat(MimeEncoder.decodeWord(encoded)).isEqualTo("a=b");
        }

        @Test
        void shouldDecodeMultipleConsecutiveEncodedWords() {
            // RFC 2047: whitespace between adjacent encoded words should be dropped
            String encoded = "=?UTF-8?B?SGVs?= =?UTF-8?B?bG8=?=";
            assertThat(MimeEncoder.decodeWord(encoded)).isEqualTo("Hello");
        }

        @Test
        void shouldDecodeMixedEncodedAndPlainText() {
            String encoded = "Re: =?UTF-8?B?SGVsbG8=?= there";
            assertThat(MimeEncoder.decodeWord(encoded)).isEqualTo("Re: Hello there");
        }

        @Test
        void shouldDecodeWithTrailingText() {
            String encoded = "=?UTF-8?B?SGVsbG8=?= World";
            assertThat(MimeEncoder.decodeWord(encoded)).isEqualTo("Hello World");
        }

        @Test
        void shouldDecodeWithDifferentCharset() {
            // ISO-8859-1 encoding of e-acute (0xE9)
            String encoded = "=?ISO-8859-1?Q?caf=E9?=";
            assertThat(MimeEncoder.decodeWord(encoded)).isEqualTo("caf\u00E9");
        }

        @Test
        void shouldHandleQEncodingWithInvalidHex() {
            // Invalid hex after = in Q-encoding should pass through the =
            String encoded = "=?UTF-8?Q?test=ZZmore?=";
            String decoded = MimeEncoder.decodeWord(encoded);
            assertThat(decoded).isEqualTo("test=ZZmore");
        }

        @Test
        void shouldHandleQEncodingEqualsAtEnd() {
            // = with fewer than 2 chars remaining
            String encoded = "=?UTF-8?Q?test=?=";
            String decoded = MimeEncoder.decodeWord(encoded);
            assertThat(decoded).isEqualTo("test=");
        }
    }

    // ========== Boundary & Message-ID ==========

    @Nested
    class GenerateBoundaryTests {

        @Test
        void shouldGenerateNonNullNonEmpty() {
            String boundary = MimeEncoder.generateBoundary();
            assertThat(boundary).isNotNull().isNotEmpty();
        }

        @Test
        void shouldStartWithPrefix() {
            String boundary = MimeEncoder.generateBoundary();
            assertThat(boundary).startsWith("----=_Part_");
        }

        @Test
        void shouldGenerateUniqueValues() {
            Set<String> boundaries = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                boundaries.add(MimeEncoder.generateBoundary());
            }
            assertThat(boundaries).hasSize(100);
        }

        @Test
        void shouldNotContainHyphens() {
            // UUID hyphens should be removed
            String boundary = MimeEncoder.generateBoundary();
            String uuidPart = boundary.substring("----=_Part_".length());
            assertThat(uuidPart).doesNotContain("-");
        }
    }

    @Nested
    class GenerateMessageIdTests {

        @Test
        void shouldHaveAngleBracketFormat() {
            String messageId = MimeEncoder.generateMessageId("example.com");
            assertThat(messageId).startsWith("<");
            assertThat(messageId).endsWith(">");
        }

        @Test
        void shouldContainDomainAfterAt() {
            String messageId = MimeEncoder.generateMessageId("example.com");
            assertThat(messageId).contains("@example.com>");
        }

        @Test
        void shouldBeUnique() {
            Set<String> ids = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                ids.add(MimeEncoder.generateMessageId("test.com"));
            }
            assertThat(ids).hasSize(100);
        }

        @Test
        void shouldWorkWithVariousDomains() {
            String id1 = MimeEncoder.generateMessageId("mail.example.org");
            assertThat(id1).contains("@mail.example.org>");

            String id2 = MimeEncoder.generateMessageId("localhost");
            assertThat(id2).contains("@localhost>");
        }
    }
}
