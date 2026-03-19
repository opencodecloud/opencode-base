package cloud.opencode.base.crypto.codec;

import cloud.opencode.base.core.OpenBase64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * Base64UrlCodec 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("Base64UrlCodec 测试")
class Base64UrlCodecTest {

    @Nested
    @DisplayName("encode 测试")
    class EncodeTests {

        @Test
        @DisplayName("编码空字节数组")
        void testEncodeEmpty() {
            assertThat(Base64UrlCodec.encode(new byte[0])).isEqualTo("");
        }

        @Test
        @DisplayName("编码简单字符串")
        void testEncodeSimple() {
            byte[] data = "Hello".getBytes(StandardCharsets.UTF_8);
            String encoded = Base64UrlCodec.encode(data);
            assertThat(encoded).doesNotContain("+").doesNotContain("/");
        }

        @Test
        @DisplayName("编码特殊字符数据")
        void testEncodeSpecialChars() {
            // 这些字节在标准Base64中会产生+和/
            byte[] data = {(byte) 0xfb, (byte) 0xff};
            String encoded = Base64UrlCodec.encode(data);
            // Base64URL使用-代替+, _代替/
            assertThat(encoded).doesNotContain("+").doesNotContain("/");
        }

        @Test
        @DisplayName("编码null抛出异常")
        void testEncodeNull() {
            assertThatThrownBy(() -> Base64UrlCodec.encode(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("decode 测试")
    class DecodeTests {

        @Test
        @DisplayName("解码空字符串")
        void testDecodeEmpty() {
            assertThat(Base64UrlCodec.decode("")).isEmpty();
        }

        @Test
        @DisplayName("解码URL安全字符")
        void testDecodeUrlSafe() {
            // 先编码再解码
            byte[] original = {(byte) 0xfb, (byte) 0xff};
            String encoded = Base64UrlCodec.encode(original);
            byte[] decoded = Base64UrlCodec.decode(encoded);
            assertThat(decoded).isEqualTo(original);
        }

        @Test
        @DisplayName("解码null抛出异常")
        void testDecodeNull() {
            assertThatThrownBy(() -> Base64UrlCodec.decode(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("解码无效数据抛出异常")
        void testDecodeInvalid() {
            assertThatThrownBy(() -> Base64UrlCodec.decode("!!!"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("encodeNoPadding 测试")
    class EncodeNoPaddingTests {

        @Test
        @DisplayName("无填充编码")
        void testEncodeNoPadding() {
            byte[] data = "Hi".getBytes(StandardCharsets.UTF_8);
            String encoded = Base64UrlCodec.encodeNoPadding(data);
            assertThat(encoded).doesNotContain("=");
        }

        @Test
        @DisplayName("无填充编码null抛出异常")
        void testEncodeNoPaddingNull() {
            assertThatThrownBy(() -> Base64UrlCodec.encodeNoPadding(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("JWT风格编码 - 无填充无特殊字符")
        void testJwtStyleEncoding() {
            byte[] data = "test-data-for-jwt".getBytes(StandardCharsets.UTF_8);
            String encoded = Base64UrlCodec.encodeNoPadding(data);
            assertThat(encoded)
                    .doesNotContain("=")
                    .doesNotContain("+")
                    .doesNotContain("/");
        }
    }

    @Nested
    @DisplayName("decodeNoPadding 测试")
    class DecodeNoPaddingTests {

        @Test
        @DisplayName("解码无填充数据")
        void testDecodeNoPadding() {
            String encoded = Base64UrlCodec.encodeNoPadding("Hi".getBytes(StandardCharsets.UTF_8));
            byte[] decoded = Base64UrlCodec.decodeNoPadding(encoded);
            assertThat(new String(decoded, StandardCharsets.UTF_8)).isEqualTo("Hi");
        }

        @Test
        @DisplayName("解码无填充null抛出异常")
        void testDecodeNoPaddingNull() {
            assertThatThrownBy(() -> Base64UrlCodec.decodeNoPadding(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("往返测试")
    class RoundTripTests {

        @Test
        @DisplayName("带填充往返")
        void testRoundTripWithPadding() {
            byte[] original = "Hello, World!".getBytes(StandardCharsets.UTF_8);
            String encoded = Base64UrlCodec.encode(original);
            byte[] decoded = Base64UrlCodec.decode(encoded);
            assertThat(decoded).isEqualTo(original);
        }

        @Test
        @DisplayName("无填充往返")
        void testRoundTripNoPadding() {
            byte[] original = "Test data".getBytes(StandardCharsets.UTF_8);
            String encoded = Base64UrlCodec.encodeNoPadding(original);
            byte[] decoded = Base64UrlCodec.decodeNoPadding(encoded);
            assertThat(decoded).isEqualTo(original);
        }

        @Test
        @DisplayName("二进制数据往返")
        void testBinaryRoundTrip() {
            byte[] original = new byte[256];
            for (int i = 0; i < 256; i++) {
                original[i] = (byte) i;
            }
            String encoded = Base64UrlCodec.encode(original);
            byte[] decoded = Base64UrlCodec.decode(encoded);
            assertThat(decoded).isEqualTo(original);
        }

        @Test
        @DisplayName("大量数据往返")
        void testLargeDataRoundTrip() {
            byte[] original = new byte[10000];
            for (int i = 0; i < original.length; i++) {
                original[i] = (byte) (i & 0xff);
            }
            String encoded = Base64UrlCodec.encodeNoPadding(original);
            byte[] decoded = Base64UrlCodec.decodeNoPadding(encoded);
            assertThat(decoded).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("与标准Base64对比测试")
    class ComparisonTests {

        @Test
        @DisplayName("URL安全编码不包含+和/")
        void testUrlSafeCharacters() {
            // 产生包含+和/的数据
            byte[] data = new byte[100];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i * 3);
            }
            String urlEncoded = Base64UrlCodec.encode(data);
            String standardEncoded = OpenBase64.encode(data);

            // URL编码不包含+和/
            assertThat(urlEncoded).doesNotContain("+").doesNotContain("/");

            // 如果标准编码包含这些字符，URL编码应该用-和_替换
            if (standardEncoded.contains("+")) {
                assertThat(urlEncoded).contains("-");
            }
            if (standardEncoded.contains("/")) {
                assertThat(urlEncoded).contains("_");
            }
        }
    }

    @Nested
    @DisplayName("实例化测试")
    class InstantiationTests {

        @Test
        @DisplayName("无法实例化工具类")
        void testCannotInstantiate() {
            assertThatThrownBy(() -> {
                var constructor = Base64UrlCodec.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            }).hasCauseInstanceOf(UnsupportedOperationException.class);
        }
    }
}
