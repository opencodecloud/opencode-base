package cloud.opencode.base.core.codec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for the Codec API
 * 编解码 API 综合测试
 */
@DisplayName("OpenCodec - 编解码门面类测试")
class OpenCodecTest {

    // ==================== AC-1: Codec Roundtrip ====================

    @Nested
    @DisplayName("AC-1: Codec Roundtrip - 编解码往返")
    class RoundtripTest {

        @Test
        @DisplayName("Base64 roundtrip - Base64 往返")
        void base64Roundtrip() {
            Codec<byte[], String> codec = OpenCodec.base64();
            byte[] input = "Hello, World!".getBytes(StandardCharsets.UTF_8);
            assertThat(codec.decode(codec.encode(input))).isEqualTo(input);
        }

        @Test
        @DisplayName("Hex roundtrip - Hex 往返")
        void hexRoundtrip() {
            Codec<byte[], String> codec = OpenCodec.hex();
            byte[] input = {0x48, 0x65, 0x6C, 0x6C, 0x6F};
            assertThat(codec.decode(codec.encode(input))).isEqualTo(input);
        }

        @Test
        @DisplayName("URL roundtrip - URL 往返")
        void urlRoundtrip() {
            Codec<String, String> codec = OpenCodec.url();
            String input = "hello world&foo=bar/baz?key=value#fragment";
            assertThat(codec.decode(codec.encode(input))).isEqualTo(input);
        }

        @Test
        @DisplayName("HTML roundtrip - HTML 往返")
        void htmlRoundtrip() {
            Codec<String, String> codec = OpenCodec.html();
            String input = "<script>alert(\"xss\")</script>";
            assertThat(codec.decode(codec.encode(input))).isEqualTo(input);
        }

        @Test
        @DisplayName("Base32 roundtrip - Base32 往返")
        void base32Roundtrip() {
            Codec<byte[], String> codec = OpenCodec.base32();
            byte[] input = "Hello, World!".getBytes(StandardCharsets.UTF_8);
            assertThat(codec.decode(codec.encode(input))).isEqualTo(input);
        }

        @Test
        @DisplayName("ASCII85 roundtrip - ASCII85 往返")
        void ascii85Roundtrip() {
            Codec<byte[], String> codec = OpenCodec.ascii85();
            byte[] input = "Hello, World!".getBytes(StandardCharsets.UTF_8);
            assertThat(codec.decode(codec.encode(input))).isEqualTo(input);
        }

        @Test
        @DisplayName("Empty byte array roundtrip - 空字节数组往返")
        void emptyByteArrayRoundtrip() {
            for (Codec<byte[], String> codec : new Codec[]{
                    OpenCodec.base64(), OpenCodec.hex(),
                    OpenCodec.base32(), OpenCodec.ascii85()}) {
                byte[] input = new byte[0];
                assertThat(codec.decode(codec.encode(input))).isEqualTo(input);
            }
        }

        @Test
        @DisplayName("Empty string roundtrip - 空字符串往返")
        void emptyStringRoundtrip() {
            assertThat(OpenCodec.url().decode(OpenCodec.url().encode(""))).isEqualTo("");
            assertThat(OpenCodec.html().decode(OpenCodec.html().encode(""))).isEqualTo("");
        }

        @Test
        @DisplayName("Binary data roundtrip - 二进制数据往返")
        void binaryDataRoundtrip() {
            byte[] input = new byte[256];
            for (int i = 0; i < 256; i++) {
                input[i] = (byte) i;
            }
            for (Codec<byte[], String> codec : new Codec[]{
                    OpenCodec.base64(), OpenCodec.hex(),
                    OpenCodec.base32(), OpenCodec.ascii85()}) {
                assertThat(codec.decode(codec.encode(input))).isEqualTo(input);
            }
        }
    }

    // ==================== AC-2: Codec Composition ====================

    @Nested
    @DisplayName("AC-2: Codec Composition - 编解码组合")
    class CompositionTest {

        @Test
        @DisplayName("andThen produces correct encode - andThen 编码正确")
        void andThenEncode() {
            Codec<byte[], String> hex = OpenCodec.hex();
            Codec<String, String> url = OpenCodec.url();
            Codec<byte[], String> combined = hex.andThen(url);

            byte[] input = {(byte) 0xFF, 0x00};
            String hexEncoded = hex.encode(input);
            String expected = url.encode(hexEncoded);
            assertThat(combined.encode(input)).isEqualTo(expected);
        }

        @Test
        @DisplayName("andThen produces correct decode - andThen 解码正确")
        void andThenDecode() {
            Codec<byte[], String> hex = OpenCodec.hex();
            Codec<String, String> url = OpenCodec.url();
            Codec<byte[], String> combined = hex.andThen(url);

            byte[] original = {(byte) 0xFF, 0x00};
            String encoded = combined.encode(original);
            assertThat(combined.decode(encoded)).isEqualTo(original);
        }

        @Test
        @DisplayName("andThen null codec throws NPE - andThen null 编解码器抛出 NPE")
        void andThenNullThrowsNPE() {
            assertThatThrownBy(() -> OpenCodec.hex().andThen(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== AC-3: URL Encoding ====================

    @Nested
    @DisplayName("AC-3: URL Encoding - URL 编码")
    class UrlEncodingTest {

        @Test
        @DisplayName("Spaces encoded as %20 - 空格编码为 %20")
        void spaceEncodedAsPercent20() {
            Codec<String, String> codec = OpenCodec.url();
            assertThat(codec.encode("hello world")).isEqualTo("hello%20world");
        }

        @Test
        @DisplayName("Query parameter encoding - 查询参数编码")
        void queryParameterEncoding() {
            Codec<String, String> codec = OpenCodec.url();
            String encoded = codec.encode("hello world&foo=bar");
            assertThat(encoded).isEqualTo("hello%20world%26foo%3Dbar");
            assertThat(codec.decode(encoded)).isEqualTo("hello world&foo=bar");
        }

        @Test
        @DisplayName("Unreserved characters not encoded - 未保留字符不编码")
        void unreservedNotEncoded() {
            Codec<String, String> codec = OpenCodec.url();
            String unreserved = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~";
            assertThat(codec.encode(unreserved)).isEqualTo(unreserved);
        }

        @Test
        @DisplayName("UTF-8 multibyte encoding - UTF-8 多字节编码")
        void utf8MultibiteEncoding() {
            Codec<String, String> codec = OpenCodec.url();
            String input = "你好";
            String encoded = codec.encode(input);
            assertThat(encoded).doesNotContain("你").doesNotContain("好");
            assertThat(codec.decode(encoded)).isEqualTo(input);
        }

        @Test
        @DisplayName("Incomplete percent-encoding throws - 不完整的百分号编码抛出异常")
        void incompletePercentEncodingThrows() {
            Codec<String, String> codec = OpenCodec.url();
            assertThatThrownBy(() -> codec.decode("hello%2"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Invalid hex digit throws - 无效十六进制数字抛出异常")
        void invalidHexDigitThrows() {
            Codec<String, String> codec = OpenCodec.url();
            assertThatThrownBy(() -> codec.decode("hello%GG"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== AC-4: HTML Escaping ====================

    @Nested
    @DisplayName("AC-4: HTML Escaping - HTML 转义")
    class HtmlEscapingTest {

        @Test
        @DisplayName("OWASP 5 characters escaped - OWASP 5 个字符被转义")
        void owasp5CharactersEscaped() {
            Codec<String, String> codec = OpenCodec.html();
            String input = "<script>alert(\"xss\")</script>";
            String expected = "&lt;script&gt;alert(&quot;xss&quot;)&lt;/script&gt;";
            assertThat(codec.encode(input)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Single quote escaped - 单引号被转义")
        void singleQuoteEscaped() {
            Codec<String, String> codec = OpenCodec.html();
            assertThat(codec.encode("it's")).isEqualTo("it&#39;s");
        }

        @Test
        @DisplayName("Ampersand escaped - & 被转义")
        void ampersandEscaped() {
            Codec<String, String> codec = OpenCodec.html();
            assertThat(codec.encode("a&b")).isEqualTo("a&amp;b");
        }

        @Test
        @DisplayName("Normal text not escaped - 普通文本不转义")
        void normalTextNotEscaped() {
            Codec<String, String> codec = OpenCodec.html();
            String input = "Hello World 123";
            assertThat(codec.encode(input)).isEqualTo(input);
        }

        @Test
        @DisplayName("HTML unescape named entities - HTML 反转义命名实体")
        void unescapeNamedEntities() {
            Codec<String, String> codec = OpenCodec.html();
            assertThat(codec.decode("&lt;")).isEqualTo("<");
            assertThat(codec.decode("&gt;")).isEqualTo(">");
            assertThat(codec.decode("&amp;")).isEqualTo("&");
            assertThat(codec.decode("&quot;")).isEqualTo("\"");
            assertThat(codec.decode("&apos;")).isEqualTo("'");
        }

        @Test
        @DisplayName("Full roundtrip with all OWASP characters - 包含所有 OWASP 字符的完整往返")
        void fullRoundtrip() {
            Codec<String, String> codec = OpenCodec.html();
            String input = "<div class=\"test\" data-val='x&y'>content</div>";
            assertThat(codec.decode(codec.encode(input))).isEqualTo(input);
        }
    }

    // ==================== AC-5: HTML Numeric Entities ====================

    @Nested
    @DisplayName("AC-5: HTML Numeric Entities - HTML 数字实体")
    class HtmlNumericEntityTest {

        @Test
        @DisplayName("Decimal entity &#60; decoded to < - 十进制实体 &#60; 解码为 <")
        void decimalEntity() {
            Codec<String, String> codec = OpenCodec.html();
            assertThat(codec.decode("&#60;")).isEqualTo("<");
        }

        @Test
        @DisplayName("Hex entity &#x3C; decoded to < - 十六进制实体 &#x3C; 解码为 <")
        void hexEntity() {
            Codec<String, String> codec = OpenCodec.html();
            assertThat(codec.decode("&#x3C;")).isEqualTo("<");
        }

        @Test
        @DisplayName("Mixed entities decoded correctly - 混合实体正确解码")
        void mixedEntities() {
            Codec<String, String> codec = OpenCodec.html();
            assertThat(codec.decode("&#60;script&#x3E;"))
                    .isEqualTo("<script>");
        }

        @Test
        @DisplayName("Uppercase hex entity &#x3c; - 大写十六进制实体")
        void uppercaseHexEntity() {
            Codec<String, String> codec = OpenCodec.html();
            assertThat(codec.decode("&#x3c;")).isEqualTo("<");
        }

        @Test
        @DisplayName("Unicode code point entity - Unicode 码点实体")
        void unicodeCodePoint() {
            Codec<String, String> codec = OpenCodec.html();
            assertThat(codec.decode("&#20320;")).isEqualTo("你");
            assertThat(codec.decode("&#x4F60;")).isEqualTo("你");
        }

        @Test
        @DisplayName("Invalid entity preserved as literal - 无效实体保留为字面量")
        void invalidEntityPreserved() {
            Codec<String, String> codec = OpenCodec.html();
            assertThat(codec.decode("&unknown;")).isEqualTo("&unknown;");
        }

        @Test
        @DisplayName("Ampersand without semicolon preserved - 无分号的 & 保留")
        void ampersandWithoutSemicolon() {
            Codec<String, String> codec = OpenCodec.html();
            assertThat(codec.decode("a&b")).isEqualTo("a&b");
        }
    }

    // ==================== AC-6: Base32 ====================

    @Nested
    @DisplayName("AC-6: Base32 - Base32 编解码")
    class Base32Test {

        @Test
        @DisplayName("RFC 4648 test vectors - RFC 4648 测试向量")
        void rfcTestVectors() {
            Codec<byte[], String> codec = OpenCodec.base32();
            // RFC 4648 Section 10 test vectors
            assertThat(codec.encode("".getBytes())).isEqualTo("");
            assertThat(codec.encode("f".getBytes())).isEqualTo("MY======");
            assertThat(codec.encode("fo".getBytes())).isEqualTo("MZXQ====");
            assertThat(codec.encode("foo".getBytes())).isEqualTo("MZXW6===");
            assertThat(codec.encode("foob".getBytes())).isEqualTo("MZXW6YQ=");
            assertThat(codec.encode("fooba".getBytes())).isEqualTo("MZXW6YTB");
            assertThat(codec.encode("foobar".getBytes())).isEqualTo("MZXW6YTBOI======");
        }

        @Test
        @DisplayName("No-padding mode - 无填充模式")
        void noPaddingMode() {
            Codec<byte[], String> codec = OpenCodec.base32NoPadding();
            assertThat(codec.encode("f".getBytes())).isEqualTo("MY");
            assertThat(codec.encode("fo".getBytes())).isEqualTo("MZXQ");
            assertThat(codec.encode("foo".getBytes())).isEqualTo("MZXW6");
        }

        @Test
        @DisplayName("Decode with padding - 带填充解码")
        void decodeWithPadding() {
            Codec<byte[], String> codec = OpenCodec.base32();
            assertThat(codec.decode("MY======")).isEqualTo("f".getBytes());
            assertThat(codec.decode("MZXW6YTBOI======")).isEqualTo("foobar".getBytes());
        }

        @Test
        @DisplayName("Decode without padding - 无填充解码")
        void decodeWithoutPadding() {
            Codec<byte[], String> codec = OpenCodec.base32NoPadding();
            assertThat(codec.decode("MY")).isEqualTo("f".getBytes());
            assertThat(codec.decode("MZXW6")).isEqualTo("foo".getBytes());
        }

        @Test
        @DisplayName("Case insensitive decode - 大小写不敏感解码")
        void caseInsensitiveDecode() {
            Codec<byte[], String> codec = OpenCodec.base32();
            assertThat(codec.decode("mzxw6===")).isEqualTo("foo".getBytes());
        }

        @Test
        @DisplayName("Invalid character throws - 无效字符抛出异常")
        void invalidCharacterThrows() {
            Codec<byte[], String> codec = OpenCodec.base32();
            assertThatThrownBy(() -> codec.decode("MZXW1==="))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== AC-7: ASCII85 ====================

    @Nested
    @DisplayName("AC-7: ASCII85 - ASCII85 编解码")
    class Ascii85Test {

        @Test
        @DisplayName("Basic encoding - 基本编码")
        void basicEncoding() {
            Codec<byte[], String> codec = OpenCodec.ascii85();
            byte[] input = "Man ".getBytes(StandardCharsets.US_ASCII);
            // "Man " = 0x4D616E20 → 9jqo^
            assertThat(codec.encode(input)).isEqualTo("9jqo^");
        }

        @Test
        @DisplayName("Zero group encoded as z - 全零组编码为 z")
        void zeroGroupEncodedAsZ() {
            Codec<byte[], String> codec = OpenCodec.ascii85();
            byte[] input = {0, 0, 0, 0};
            assertThat(codec.encode(input)).isEqualTo("z");
        }

        @Test
        @DisplayName("Zero group z decoded - z 解码为全零")
        void zDecoded() {
            Codec<byte[], String> codec = OpenCodec.ascii85();
            assertThat(codec.decode("z")).isEqualTo(new byte[]{0, 0, 0, 0});
        }

        @Test
        @DisplayName("Partial group roundtrip - 非完整组往返")
        void partialGroupRoundtrip() {
            Codec<byte[], String> codec = OpenCodec.ascii85();
            byte[] input = {1, 2, 3};
            assertThat(codec.decode(codec.encode(input))).isEqualTo(input);
        }

        @Test
        @DisplayName("Various lengths roundtrip - 各种长度往返")
        void variousLengthsRoundtrip() {
            Codec<byte[], String> codec = OpenCodec.ascii85();
            for (int len = 0; len <= 20; len++) {
                byte[] input = new byte[len];
                for (int i = 0; i < len; i++) {
                    input[i] = (byte) (i * 17 + 3);
                }
                assertThat(codec.decode(codec.encode(input)))
                        .as("length=%d", len)
                        .isEqualTo(input);
            }
        }
    }

    // ==================== AC-8: Facade Methods ====================

    @Nested
    @DisplayName("AC-8: Facade Methods - 门面方法")
    class FacadeTest {

        @Test
        @DisplayName("base64() returns usable codec - base64() 返回可用的编解码器")
        void base64Usable() {
            assertThat(OpenCodec.base64()).isNotNull();
            assertThat(OpenCodec.base64().encode("test".getBytes())).isNotEmpty();
        }

        @Test
        @DisplayName("hex() returns usable codec - hex() 返回可用的编解码器")
        void hexUsable() {
            assertThat(OpenCodec.hex()).isNotNull();
            assertThat(OpenCodec.hex().encode(new byte[]{0x0F})).isEqualTo("0f");
        }

        @Test
        @DisplayName("url() returns usable codec - url() 返回可用的编解码器")
        void urlUsable() {
            assertThat(OpenCodec.url()).isNotNull();
            assertThat(OpenCodec.url().encode("a b")).isEqualTo("a%20b");
        }

        @Test
        @DisplayName("html() returns usable codec - html() 返回可用的编解码器")
        void htmlUsable() {
            assertThat(OpenCodec.html()).isNotNull();
            assertThat(OpenCodec.html().encode("<")).isEqualTo("&lt;");
        }

        @Test
        @DisplayName("base32() returns usable codec - base32() 返回可用的编解码器")
        void base32Usable() {
            assertThat(OpenCodec.base32()).isNotNull();
            assertThat(OpenCodec.base32().encode("A".getBytes())).isEqualTo("IE======");
        }

        @Test
        @DisplayName("ascii85() returns usable codec - ascii85() 返回可用的编解码器")
        void ascii85Usable() {
            assertThat(OpenCodec.ascii85()).isNotNull();
            assertThat(OpenCodec.ascii85().encode(new byte[]{1})).isNotEmpty();
        }

        @Test
        @DisplayName("Singletons returned - 返回单例")
        void singletons() {
            assertThat(OpenCodec.base64()).isSameAs(OpenCodec.base64());
            assertThat(OpenCodec.hex()).isSameAs(OpenCodec.hex());
            assertThat(OpenCodec.url()).isSameAs(OpenCodec.url());
            assertThat(OpenCodec.html()).isSameAs(OpenCodec.html());
            assertThat(OpenCodec.base32()).isSameAs(OpenCodec.base32());
            assertThat(OpenCodec.ascii85()).isSameAs(OpenCodec.ascii85());
        }
    }

    // ==================== AC-9: Null Safety ====================

    @Nested
    @DisplayName("AC-9: Null Safety - 空值安全")
    class NullSafetyTest {

        @Test
        @DisplayName("Base64 encode null throws NPE - Base64 编码 null 抛出 NPE")
        void base64EncodeNull() {
            assertThatThrownBy(() -> OpenCodec.base64().encode(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Base64 decode null throws NPE - Base64 解码 null 抛出 NPE")
        void base64DecodeNull() {
            assertThatThrownBy(() -> OpenCodec.base64().decode(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Hex encode null throws NPE - Hex 编码 null 抛出 NPE")
        void hexEncodeNull() {
            assertThatThrownBy(() -> OpenCodec.hex().encode(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Hex decode null throws NPE - Hex 解码 null 抛出 NPE")
        void hexDecodeNull() {
            assertThatThrownBy(() -> OpenCodec.hex().decode(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("URL encode null throws NPE - URL 编码 null 抛出 NPE")
        void urlEncodeNull() {
            assertThatThrownBy(() -> OpenCodec.url().encode(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("URL decode null throws NPE - URL 解码 null 抛出 NPE")
        void urlDecodeNull() {
            assertThatThrownBy(() -> OpenCodec.url().decode(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("HTML encode null throws NPE - HTML 编码 null 抛出 NPE")
        void htmlEncodeNull() {
            assertThatThrownBy(() -> OpenCodec.html().encode(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("HTML decode null throws NPE - HTML 解码 null 抛出 NPE")
        void htmlDecodeNull() {
            assertThatThrownBy(() -> OpenCodec.html().decode(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Base32 encode null throws NPE - Base32 编码 null 抛出 NPE")
        void base32EncodeNull() {
            assertThatThrownBy(() -> OpenCodec.base32().encode(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Base32 decode null throws NPE - Base32 解码 null 抛出 NPE")
        void base32DecodeNull() {
            assertThatThrownBy(() -> OpenCodec.base32().decode(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("ASCII85 encode null throws NPE - ASCII85 编码 null 抛出 NPE")
        void ascii85EncodeNull() {
            assertThatThrownBy(() -> OpenCodec.ascii85().encode(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("ASCII85 decode null throws NPE - ASCII85 解码 null 抛出 NPE")
        void ascii85DecodeNull() {
            assertThatThrownBy(() -> OpenCodec.ascii85().decode(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== AC-10: Thread Safety ====================

    @Nested
    @DisplayName("AC-10: Thread Safety - 线程安全")
    class ThreadSafetyTest {

        @Test
        @DisplayName("Concurrent Base64 encoding - 并发 Base64 编码")
        void concurrentBase64() throws Exception {
            runConcurrently(() -> {
                Codec<byte[], String> codec = OpenCodec.base64();
                byte[] input = "thread-safe-test".getBytes(StandardCharsets.UTF_8);
                byte[] result = codec.decode(codec.encode(input));
                assertThat(result).isEqualTo(input);
            });
        }

        @Test
        @DisplayName("Concurrent URL encoding - 并发 URL 编码")
        void concurrentUrl() throws Exception {
            runConcurrently(() -> {
                Codec<String, String> codec = OpenCodec.url();
                String input = "hello world&concurrent=true";
                String result = codec.decode(codec.encode(input));
                assertThat(result).isEqualTo(input);
            });
        }

        @Test
        @DisplayName("Concurrent HTML encoding - 并发 HTML 编码")
        void concurrentHtml() throws Exception {
            runConcurrently(() -> {
                Codec<String, String> codec = OpenCodec.html();
                String input = "<div class=\"test\">&value</div>";
                String result = codec.decode(codec.encode(input));
                assertThat(result).isEqualTo(input);
            });
        }

        @Test
        @DisplayName("Concurrent Base32 encoding - 并发 Base32 编码")
        void concurrentBase32() throws Exception {
            runConcurrently(() -> {
                Codec<byte[], String> codec = OpenCodec.base32();
                byte[] input = "concurrent-test".getBytes(StandardCharsets.UTF_8);
                byte[] result = codec.decode(codec.encode(input));
                assertThat(result).isEqualTo(input);
            });
        }

        private void runConcurrently(Runnable task) throws Exception {
            int threads = 16;
            int iterations = 1000;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            CountDownLatch latch = new CountDownLatch(threads);
            AtomicInteger errors = new AtomicInteger(0);
            try {
                for (int t = 0; t < threads; t++) {
                    executor.submit(() -> {
                        try {
                            for (int i = 0; i < iterations; i++) {
                                task.run();
                            }
                        } catch (Exception e) {
                            errors.incrementAndGet();
                        } finally {
                            latch.countDown();
                        }
                    });
                }
                latch.await();
                assertThat(errors.get()).isZero();
            } finally {
                executor.shutdown();
            }
        }
    }

    // ==================== Additional Edge Cases ====================

    @Nested
    @DisplayName("Additional Edge Cases - 额外边界测试")
    class EdgeCaseTest {

        @Test
        @DisplayName("Base64 URL-safe mode - Base64 URL 安全模式")
        void base64UrlSafe() {
            Codec<byte[], String> codec = OpenCodec.base64UrlSafe();
            byte[] input = {(byte) 0xFF, (byte) 0xFE, (byte) 0xFD};
            String encoded = codec.encode(input);
            assertThat(encoded).doesNotContain("+").doesNotContain("/").doesNotContain("=");
            assertThat(codec.decode(encoded)).isEqualTo(input);
        }

        @Test
        @DisplayName("Base64 no-padding mode - Base64 无填充模式")
        void base64NoPadding() {
            Codec<byte[], String> codec = OpenCodec.base64NoPadding();
            byte[] input = {1, 2};
            String encoded = codec.encode(input);
            assertThat(encoded).doesNotContain("=");
            assertThat(codec.decode(encoded)).isEqualTo(input);
        }

        @Test
        @DisplayName("Hex uppercase mode - Hex 大写模式")
        void hexUppercase() {
            Codec<byte[], String> codec = OpenCodec.hexUpper();
            byte[] input = {(byte) 0xAB, (byte) 0xCD};
            assertThat(codec.encode(input)).isEqualTo("ABCD");
        }

        @Test
        @DisplayName("Hex odd-length decode throws - Hex 奇数长度解码抛出异常")
        void hexOddLengthThrows() {
            assertThatThrownBy(() -> OpenCodec.hex().decode("abc"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("URL special characters - URL 特殊字符")
        void urlSpecialCharacters() {
            Codec<String, String> codec = OpenCodec.url();
            assertThat(codec.encode("/")).isEqualTo("%2F");
            assertThat(codec.encode("?")).isEqualTo("%3F");
            assertThat(codec.encode("#")).isEqualTo("%23");
            assertThat(codec.encode("@")).isEqualTo("%40");
        }

        @Test
        @DisplayName("HTML &#39; decoded as single quote - HTML &#39; 解码为单引号")
        void htmlNumeric39() {
            Codec<String, String> codec = OpenCodec.html();
            assertThat(codec.decode("&#39;")).isEqualTo("'");
        }

        @Test
        @DisplayName("Base32 no-padding roundtrip - Base32 无填充往返")
        void base32NoPaddingRoundtrip() {
            Codec<byte[], String> codec = OpenCodec.base32NoPadding();
            for (int len = 0; len <= 10; len++) {
                byte[] input = new byte[len];
                for (int i = 0; i < len; i++) input[i] = (byte) (i + 1);
                assertThat(codec.decode(codec.encode(input)))
                        .as("length=%d", len)
                        .isEqualTo(input);
            }
        }

        @Test
        @DisplayName("ASCII85 multiple zero groups - ASCII85 多个全零组")
        void ascii85MultipleZeroGroups() {
            Codec<byte[], String> codec = OpenCodec.ascii85();
            byte[] input = new byte[12]; // three zero groups
            String encoded = codec.encode(input);
            assertThat(encoded).isEqualTo("zzz");
            assertThat(codec.decode(encoded)).isEqualTo(input);
        }
    }

    // ==================== Audit Fix Regression Tests ====================

    @Nested
    @DisplayName("Audit Fixes - 审计修复回归测试")
    class AuditFixTest {

        @Test
        @DisplayName("[HIGH-2] URL decode rejects non-ASCII chars - URL 解码拒绝非 ASCII 字符")
        void urlDecodeRejectsNonAscii() {
            Codec<String, String> codec = OpenCodec.url();
            assertThatThrownBy(() -> codec.decode("\u00E9"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Non-ASCII");
        }

        @Test
        @DisplayName("[HIGH-2] URL decode rejects non-ASCII without percent - 无百分号时也拒绝非 ASCII")
        void urlDecodeRejectsNonAsciiNoPercent() {
            Codec<String, String> codec = OpenCodec.url();
            assertThatThrownBy(() -> codec.decode("hello\u4F60"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Non-ASCII");
        }

        @Test
        @DisplayName("[HIGH-3] HTML decode rejects surrogate code points - HTML 解码拒绝代理码点")
        void htmlDecodeRejectsSurrogate() {
            Codec<String, String> codec = OpenCodec.html();
            // Surrogate code points should be treated as invalid, preserved as literal
            assertThat(codec.decode("&#xD800;")).isEqualTo("&#xD800;");
            assertThat(codec.decode("&#xDFFF;")).isEqualTo("&#xDFFF;");
            assertThat(codec.decode("&#55296;")).isEqualTo("&#55296;");
        }

        @Test
        @DisplayName("[MEDIUM-1] ASCII85 rejects z mid-group - ASCII85 拒绝组内 z")
        void ascii85RejectsZMidGroup() {
            Codec<byte[], String> codec = OpenCodec.ascii85();
            assertThatThrownBy(() -> codec.decode("!z"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("start of a group");
        }

        @Test
        @DisplayName("[MEDIUM-2] Base64 no-padding roundtrip all lengths - Base64 无填充所有长度往返")
        void base64NoPaddingAllLengths() {
            Codec<byte[], String> codec = OpenCodec.base64NoPadding();
            for (int len = 0; len <= 20; len++) {
                byte[] input = new byte[len];
                for (int i = 0; i < len; i++) input[i] = (byte) (i + 1);
                assertThat(codec.decode(codec.encode(input)))
                        .as("length=%d", len)
                        .isEqualTo(input);
            }
        }
    }
}
