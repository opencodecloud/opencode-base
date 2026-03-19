package cloud.opencode.base.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenBase64 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenBase64 测试")
class OpenBase64Test {

    @Nested
    @DisplayName("标准编码测试")
    class StandardEncodeTests {

        @Test
        @DisplayName("encode 字节数组")
        void testEncodeBytes() {
            byte[] data = "Hello, World!".getBytes(StandardCharsets.UTF_8);
            String encoded = OpenBase64.encode(data);
            assertThat(encoded).isEqualTo("SGVsbG8sIFdvcmxkIQ==");
        }

        @Test
        @DisplayName("encode 字符串")
        void testEncodeString() {
            String encoded = OpenBase64.encode("Hello");
            assertThat(encoded).isEqualTo("SGVsbG8=");
        }

        @Test
        @DisplayName("encode 指定字符集")
        void testEncodeWithCharset() {
            String encoded = OpenBase64.encode("你好", StandardCharsets.UTF_8);
            assertThat(encoded).isNotEmpty();
        }

        @Test
        @DisplayName("encode null")
        void testEncodeNull() {
            assertThat(OpenBase64.encode((byte[]) null)).isNull();
            assertThat(OpenBase64.encode((String) null)).isNull();
        }

        @Test
        @DisplayName("encodeToBytes")
        void testEncodeToBytes() {
            byte[] data = "Hello".getBytes();
            byte[] encoded = OpenBase64.encodeToBytes(data);
            assertThat(new String(encoded)).isEqualTo("SGVsbG8=");
        }

        @Test
        @DisplayName("encodeToBytes null")
        void testEncodeToBytesNull() {
            assertThat(OpenBase64.encodeToBytes(null)).isNull();
        }
    }

    @Nested
    @DisplayName("标准解码测试")
    class StandardDecodeTests {

        @Test
        @DisplayName("decode 字符串")
        void testDecodeString() {
            byte[] decoded = OpenBase64.decode("SGVsbG8sIFdvcmxkIQ==");
            assertThat(new String(decoded, StandardCharsets.UTF_8)).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("decodeToString")
        void testDecodeToString() {
            String decoded = OpenBase64.decodeToString("SGVsbG8=");
            assertThat(decoded).isEqualTo("Hello");
        }

        @Test
        @DisplayName("decodeToString 指定字符集")
        void testDecodeToStringCharset() {
            String original = "你好";
            String encoded = OpenBase64.encode(original);
            String decoded = OpenBase64.decodeToString(encoded, StandardCharsets.UTF_8);
            assertThat(decoded).isEqualTo(original);
        }

        @Test
        @DisplayName("decode null")
        void testDecodeNull() {
            assertThat(OpenBase64.decode((String) null)).isNull();
            assertThat(OpenBase64.decodeToString(null)).isNull();
        }

        @Test
        @DisplayName("decode 字节数组")
        void testDecodeBytes() {
            byte[] encoded = "SGVsbG8=".getBytes();
            byte[] decoded = OpenBase64.decode(encoded);
            assertThat(new String(decoded)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("decode 字节数组 null")
        void testDecodeBytesNull() {
            assertThat(OpenBase64.decode((byte[]) null)).isNull();
        }
    }

    @Nested
    @DisplayName("URL 安全编码测试")
    class UrlSafeTests {

        @Test
        @DisplayName("encodeUrlSafe 字节数组")
        void testEncodeUrlSafeBytes() {
            // 使用会产生 + 或 / 的数据
            byte[] data = {(byte) 0xfb, (byte) 0xff, (byte) 0xfe};
            String encoded = OpenBase64.encodeUrlSafe(data);
            assertThat(encoded).doesNotContain("+", "/", "=");
        }

        @Test
        @DisplayName("encodeUrlSafe 字符串")
        void testEncodeUrlSafeString() {
            String encoded = OpenBase64.encodeUrlSafe("Hello");
            assertThat(encoded).isEqualTo("SGVsbG8");
        }

        @Test
        @DisplayName("encodeUrlSafe null")
        void testEncodeUrlSafeNull() {
            assertThat(OpenBase64.encodeUrlSafe((byte[]) null)).isNull();
            assertThat(OpenBase64.encodeUrlSafe((String) null)).isNull();
        }

        @Test
        @DisplayName("decodeUrlSafe")
        void testDecodeUrlSafe() {
            byte[] decoded = OpenBase64.decodeUrlSafe("SGVsbG8");
            assertThat(new String(decoded)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("decodeUrlSafe null")
        void testDecodeUrlSafeNull() {
            assertThat(OpenBase64.decodeUrlSafe(null)).isNull();
        }

        @Test
        @DisplayName("decodeUrlSafeToString")
        void testDecodeUrlSafeToString() {
            String decoded = OpenBase64.decodeUrlSafeToString("SGVsbG8");
            assertThat(decoded).isEqualTo("Hello");
        }

        @Test
        @DisplayName("decodeUrlSafeToString null")
        void testDecodeUrlSafeToStringNull() {
            assertThat(OpenBase64.decodeUrlSafeToString(null)).isNull();
        }
    }

    @Nested
    @DisplayName("MIME 编码测试")
    class MimeTests {

        @Test
        @DisplayName("encodeMime")
        void testEncodeMime() {
            // 创建超过 76 字符的数据
            byte[] data = new byte[100];
            for (int i = 0; i < 100; i++) {
                data[i] = (byte) (i % 256);
            }
            String encoded = OpenBase64.encodeMime(data);
            // MIME 编码会在 76 字符后换行
            assertThat(encoded).contains("\r\n");
        }

        @Test
        @DisplayName("encodeMime null")
        void testEncodeMimeNull() {
            assertThat(OpenBase64.encodeMime(null)).isNull();
        }

        @Test
        @DisplayName("decodeMime")
        void testDecodeMime() {
            byte[] data = "Hello".getBytes();
            String encoded = OpenBase64.encodeMime(data);
            byte[] decoded = OpenBase64.decodeMime(encoded);
            assertThat(new String(decoded)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("decodeMime null")
        void testDecodeMimeNull() {
            assertThat(OpenBase64.decodeMime(null)).isNull();
        }
    }

    @Nested
    @DisplayName("无填充编码测试")
    class NoPaddingTests {

        @Test
        @DisplayName("encodeNoPadding")
        void testEncodeNoPadding() {
            byte[] data = "Hello".getBytes();
            String encoded = OpenBase64.encodeNoPadding(data);
            assertThat(encoded).isEqualTo("SGVsbG8");
            assertThat(encoded).doesNotContain("=");
        }

        @Test
        @DisplayName("encodeNoPadding null")
        void testEncodeNoPaddingNull() {
            assertThat(OpenBase64.encodeNoPadding(null)).isNull();
        }

        @Test
        @DisplayName("encodeUrlSafeNoPadding")
        void testEncodeUrlSafeNoPadding() {
            byte[] data = "Hello".getBytes();
            String encoded = OpenBase64.encodeUrlSafeNoPadding(data);
            assertThat(encoded).doesNotContain("=");
        }
    }

    @Nested
    @DisplayName("验证测试")
    class ValidationTests {

        @Test
        @DisplayName("isBase64 有效")
        void testIsBase64Valid() {
            assertThat(OpenBase64.isBase64("SGVsbG8=")).isTrue();
            assertThat(OpenBase64.isBase64("SGVsbG8sIFdvcmxkIQ==")).isTrue();
        }

        @Test
        @DisplayName("isBase64 无效")
        void testIsBase64Invalid() {
            assertThat(OpenBase64.isBase64("not valid base64!!!")).isFalse();
            assertThat(OpenBase64.isBase64(null)).isFalse();
            assertThat(OpenBase64.isBase64("")).isFalse();
        }

        @Test
        @DisplayName("isBase64UrlSafe 有效")
        void testIsBase64UrlSafeValid() {
            assertThat(OpenBase64.isBase64UrlSafe("SGVsbG8")).isTrue();
        }

        @Test
        @DisplayName("isBase64UrlSafe 无效")
        void testIsBase64UrlSafeInvalid() {
            assertThat(OpenBase64.isBase64UrlSafe("!!!")).isFalse();
            assertThat(OpenBase64.isBase64UrlSafe(null)).isFalse();
            assertThat(OpenBase64.isBase64UrlSafe("")).isFalse();
        }
    }

    @Nested
    @DisplayName("流式编解码测试")
    class StreamTests {

        @Test
        @DisplayName("encodingWrap 和 decodingWrap")
        void testEncodingDecodingWrap() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (OutputStream encoded = OpenBase64.encodingWrap(baos)) {
                encoded.write("Hello".getBytes());
            }

            String encodedStr = baos.toString();
            assertThat(encodedStr).isEqualTo("SGVsbG8=");

            ByteArrayInputStream bais = new ByteArrayInputStream(encodedStr.getBytes());
            try (InputStream decoded = OpenBase64.decodingWrap(bais)) {
                byte[] buffer = new byte[100];
                int len = decoded.read(buffer);
                assertThat(new String(buffer, 0, len)).isEqualTo("Hello");
            }
        }

        @Test
        @DisplayName("encodingWrapUrlSafe")
        void testEncodingWrapUrlSafe() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (OutputStream encoded = OpenBase64.encodingWrapUrlSafe(baos)) {
                encoded.write("Hello".getBytes());
            }
            assertThat(baos.toString()).doesNotContain("+", "/");
        }

        @Test
        @DisplayName("decodingWrapUrlSafe")
        void testDecodingWrapUrlSafe() throws IOException {
            String encoded = OpenBase64.encodeUrlSafe("Hello".getBytes());
            ByteArrayInputStream bais = new ByteArrayInputStream(encoded.getBytes());
            try (InputStream decoded = OpenBase64.decodingWrapUrlSafe(bais)) {
                byte[] buffer = new byte[100];
                int len = decoded.read(buffer);
                assertThat(new String(buffer, 0, len)).isEqualTo("Hello");
            }
        }
    }

    @Nested
    @DisplayName("往返测试")
    class RoundTripTests {

        @Test
        @DisplayName("标准编解码往返")
        void testStandardRoundTrip() {
            String original = "The quick brown fox jumps over the lazy dog. 中文测试。";
            String encoded = OpenBase64.encode(original);
            String decoded = OpenBase64.decodeToString(encoded);
            assertThat(decoded).isEqualTo(original);
        }

        @Test
        @DisplayName("URL 安全编解码往返")
        void testUrlSafeRoundTrip() {
            byte[] original = new byte[256];
            for (int i = 0; i < 256; i++) {
                original[i] = (byte) i;
            }
            String encoded = OpenBase64.encodeUrlSafe(original);
            byte[] decoded = OpenBase64.decodeUrlSafe(encoded);
            assertThat(decoded).isEqualTo(original);
        }

        @Test
        @DisplayName("MIME 编解码往返")
        void testMimeRoundTrip() {
            byte[] original = new byte[1000];
            for (int i = 0; i < 1000; i++) {
                original[i] = (byte) (i % 256);
            }
            String encoded = OpenBase64.encodeMime(original);
            byte[] decoded = OpenBase64.decodeMime(encoded);
            assertThat(decoded).isEqualTo(original);
        }
    }
}
