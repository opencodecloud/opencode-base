package cloud.opencode.base.serialization;

import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * FormatDetectorTest Tests
 * FormatDetectorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.3
 */
@DisplayName("FormatDetector Tests")
class FormatDetectorTest {

    @Nested
    @DisplayName("detect JSON Tests")
    class DetectJsonTests {

        @Test
        @DisplayName("should detect JSON object starting with brace")
        void shouldDetectJsonObjectStartingWithBrace() {
            byte[] data = "{\"name\":\"test\"}".getBytes(StandardCharsets.UTF_8);

            assertThat(FormatDetector.detect(data)).isEqualTo("json");
        }

        @Test
        @DisplayName("should detect JSON array starting with bracket")
        void shouldDetectJsonArrayStartingWithBracket() {
            byte[] data = "[1,2,3]".getBytes(StandardCharsets.UTF_8);

            assertThat(FormatDetector.detect(data)).isEqualTo("json");
        }

        @Test
        @DisplayName("should detect JSON string starting with quote")
        void shouldDetectJsonStringStartingWithQuote() {
            byte[] data = "\"hello\"".getBytes(StandardCharsets.UTF_8);

            assertThat(FormatDetector.detect(data)).isEqualTo("json");
        }

        @Test
        @DisplayName("should detect JSON with leading whitespace before brace")
        void shouldDetectJsonWithLeadingWhitespaceBeforeBrace() {
            byte[] data = "  \t\n{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8);

            assertThat(FormatDetector.detect(data)).isEqualTo("json");
        }

        @Test
        @DisplayName("should detect JSON with leading whitespace before bracket")
        void shouldDetectJsonWithLeadingWhitespaceBeforeBracket() {
            byte[] data = "  \n[1, 2, 3]".getBytes(StandardCharsets.UTF_8);

            assertThat(FormatDetector.detect(data)).isEqualTo("json");
        }

        @Test
        @DisplayName("should detect minimal JSON object")
        void shouldDetectMinimalJsonObject() {
            byte[] data = "{}".getBytes(StandardCharsets.UTF_8);

            assertThat(FormatDetector.detect(data)).isEqualTo("json");
        }

        @Test
        @DisplayName("should detect minimal JSON array")
        void shouldDetectMinimalJsonArray() {
            byte[] data = "[]".getBytes(StandardCharsets.UTF_8);

            assertThat(FormatDetector.detect(data)).isEqualTo("json");
        }
    }

    @Nested
    @DisplayName("detect XML Tests")
    class DetectXmlTests {

        @Test
        @DisplayName("should detect XML starting with angle bracket")
        void shouldDetectXmlStartingWithAngleBracket() {
            byte[] data = "<root><item>test</item></root>".getBytes(StandardCharsets.UTF_8);

            assertThat(FormatDetector.detect(data)).isEqualTo("xml");
        }

        @Test
        @DisplayName("should detect XML with declaration")
        void shouldDetectXmlWithDeclaration() {
            byte[] data = "<?xml version=\"1.0\"?><root/>".getBytes(StandardCharsets.UTF_8);

            assertThat(FormatDetector.detect(data)).isEqualTo("xml");
        }

        @Test
        @DisplayName("should detect XML with UTF-8 BOM prefix")
        void shouldDetectXmlWithUtf8BomPrefix() {
            byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            byte[] xml = "<root/>".getBytes(StandardCharsets.UTF_8);
            byte[] data = new byte[bom.length + xml.length];
            System.arraycopy(bom, 0, data, 0, bom.length);
            System.arraycopy(xml, 0, data, bom.length, xml.length);

            assertThat(FormatDetector.detect(data)).isEqualTo("xml");
        }

        @Test
        @DisplayName("should detect XML with leading whitespace")
        void shouldDetectXmlWithLeadingWhitespace() {
            byte[] data = "  \n<root/>".getBytes(StandardCharsets.UTF_8);

            assertThat(FormatDetector.detect(data)).isEqualTo("xml");
        }

        @Test
        @DisplayName("should detect self-closing XML element")
        void shouldDetectSelfClosingXmlElement() {
            byte[] data = "<element/>".getBytes(StandardCharsets.UTF_8);

            assertThat(FormatDetector.detect(data)).isEqualTo("xml");
        }
    }

    @Nested
    @DisplayName("detect Binary Tests")
    class DetectBinaryTests {

        @Test
        @DisplayName("should detect binary data with non-text bytes")
        void shouldDetectBinaryDataWithNonTextBytes() {
            byte[] data = {0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0xFE};

            String detected = FormatDetector.detect(data);

            assertThat(detected).isIn("binary", "unknown");
        }

        @Test
        @DisplayName("should detect Java serialization magic bytes as binary")
        void shouldDetectJavaSerializationMagicBytesAsBinary() {
            // Java serialization starts with 0xACED
            byte[] data = {(byte) 0xAC, (byte) 0xED, 0x00, 0x05, 0x00};

            String detected = FormatDetector.detect(data);

            assertThat(detected).isIn("binary", "unknown");
        }
    }

    @Nested
    @DisplayName("isJson() Tests")
    class IsJsonTests {

        @Test
        @DisplayName("isJson should return true for JSON object")
        void isJsonShouldReturnTrueForJsonObject() {
            byte[] data = "{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8);

            assertThat(FormatDetector.isJson(data)).isTrue();
        }

        @Test
        @DisplayName("isJson should return true for JSON array")
        void isJsonShouldReturnTrueForJsonArray() {
            byte[] data = "[1,2,3]".getBytes(StandardCharsets.UTF_8);

            assertThat(FormatDetector.isJson(data)).isTrue();
        }

        @Test
        @DisplayName("isJson should return false for XML")
        void isJsonShouldReturnFalseForXml() {
            byte[] data = "<root/>".getBytes(StandardCharsets.UTF_8);

            assertThat(FormatDetector.isJson(data)).isFalse();
        }

        @Test
        @DisplayName("isJson should return false for binary data")
        void isJsonShouldReturnFalseForBinaryData() {
            byte[] data = {0x00, 0x01, (byte) 0xFF};

            assertThat(FormatDetector.isJson(data)).isFalse();
        }

        @Test
        @DisplayName("isJson should return false for null")
        void isJsonShouldReturnFalseForNull() {
            assertThat(FormatDetector.isJson(null)).isFalse();
        }

        @Test
        @DisplayName("isJson should return false for empty array")
        void isJsonShouldReturnFalseForEmptyArray() {
            assertThat(FormatDetector.isJson(new byte[0])).isFalse();
        }
    }

    @Nested
    @DisplayName("isXml() Tests")
    class IsXmlTests {

        @Test
        @DisplayName("isXml should return true for XML document")
        void isXmlShouldReturnTrueForXmlDocument() {
            byte[] data = "<root><child/></root>".getBytes(StandardCharsets.UTF_8);

            assertThat(FormatDetector.isXml(data)).isTrue();
        }

        @Test
        @DisplayName("isXml should return true for XML declaration")
        void isXmlShouldReturnTrueForXmlDeclaration() {
            byte[] data = "<?xml version=\"1.0\"?><root/>".getBytes(StandardCharsets.UTF_8);

            assertThat(FormatDetector.isXml(data)).isTrue();
        }

        @Test
        @DisplayName("isXml should return false for JSON")
        void isXmlShouldReturnFalseForJson() {
            byte[] data = "{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8);

            assertThat(FormatDetector.isXml(data)).isFalse();
        }

        @Test
        @DisplayName("isXml should return false for binary data")
        void isXmlShouldReturnFalseForBinaryData() {
            byte[] data = {0x00, 0x01, (byte) 0xFF};

            assertThat(FormatDetector.isXml(data)).isFalse();
        }

        @Test
        @DisplayName("isXml should return false for null")
        void isXmlShouldReturnFalseForNull() {
            assertThat(FormatDetector.isXml(null)).isFalse();
        }

        @Test
        @DisplayName("isXml should return false for empty array")
        void isXmlShouldReturnFalseForEmptyArray() {
            assertThat(FormatDetector.isXml(new byte[0])).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("detect should handle null gracefully")
        void detectShouldHandleNullGracefully() {
            String result = FormatDetector.detect(null);

            assertThat(result).isEqualTo("unknown");
        }

        @Test
        @DisplayName("detect should handle empty array")
        void detectShouldHandleEmptyArray() {
            String result = FormatDetector.detect(new byte[0]);

            assertThat(result).isEqualTo("unknown");
        }

        @Test
        @DisplayName("detect should handle single byte")
        void detectShouldHandleSingleByte() {
            byte[] data = {0x41}; // 'A'

            String result = FormatDetector.detect(data);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("detect should handle whitespace-only data")
        void detectShouldHandleWhitespaceOnlyData() {
            byte[] data = "   \t\n\r   ".getBytes(StandardCharsets.UTF_8);

            String result = FormatDetector.detect(data);

            assertThat(result).isEqualTo("unknown");
        }

        @Test
        @DisplayName("detect should handle plain text")
        void detectShouldHandlePlainText() {
            byte[] data = "Hello, World!".getBytes(StandardCharsets.UTF_8);

            String result = FormatDetector.detect(data);

            assertThat(result).isIn("unknown", "binary");
        }
    }
}
