package cloud.opencode.base.hash;

import cloud.opencode.base.hash.exception.OpenHashException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * HashCode Base64 转换测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("HashCode Base64 测试")
class HashCodeBase64Test {

    @Nested
    @DisplayName("toBase64 测试")
    class ToBase64Test {

        @Test
        @DisplayName("toBase64与fromBase64往返转换")
        void testToBase64RoundtripsWithFromBase64() {
            HashCode original = HashCode.fromBytes(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
            String base64 = original.toBase64();
            HashCode restored = HashCode.fromBase64(base64);

            assertThat(restored.asBytes()).isEqualTo(original.asBytes());
        }

        @Test
        @DisplayName("toBase64生成URL安全字符")
        void testUrlSafeCharacters() {
            // Use bytes that produce + and / in standard base64
            byte[] bytes = new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0xFD};
            HashCode hash = HashCode.fromBytes(bytes);
            String base64 = hash.toBase64();

            assertThat(base64).doesNotContain("+");
            assertThat(base64).doesNotContain("/");
        }

        @Test
        @DisplayName("toBase64无填充字符")
        void testNoPaddingChars() {
            // 1 byte -> would normally have == padding, 2 bytes -> = padding
            HashCode hash1 = HashCode.fromBytes(new byte[]{1});
            HashCode hash2 = HashCode.fromBytes(new byte[]{1, 2});

            assertThat(hash1.toBase64()).doesNotContain("=");
            assertThat(hash2.toBase64()).doesNotContain("=");
        }
    }

    @Nested
    @DisplayName("fromBase64 测试")
    class FromBase64Test {

        @Test
        @DisplayName("有效Base64字符串")
        void testValidBase64() {
            // "AQID" is base64 for bytes {1, 2, 3}
            HashCode hash = HashCode.fromBase64("AQID");

            assertThat(hash.asBytes()).isEqualTo(new byte[]{1, 2, 3});
        }

        @Test
        @DisplayName("null输入抛出异常")
        void testNullThrows() {
            assertThatThrownBy(() -> HashCode.fromBase64(null))
                    .isInstanceOf(OpenHashException.class);
        }

        @Test
        @DisplayName("空字符串抛出异常")
        void testEmptyThrows() {
            assertThatThrownBy(() -> HashCode.fromBase64(""))
                    .isInstanceOf(OpenHashException.class);
        }

        @Test
        @DisplayName("无效Base64字符串抛出异常")
        void testInvalidBase64Throws() {
            assertThatThrownBy(() -> HashCode.fromBase64("!!!not-valid!!!"))
                    .isInstanceOf(OpenHashException.class);
        }
    }

    @Nested
    @DisplayName("往返转换测试")
    class RoundtripTest {

        @Test
        @DisplayName("fromBytes→toBase64→fromBase64→asBytes等价")
        void testFullRoundtrip() {
            byte[] original = new byte[]{
                    (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF,
                    0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                    (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE
            };

            HashCode hash = HashCode.fromBytes(original);
            String base64 = hash.toBase64();
            HashCode restored = HashCode.fromBase64(base64);

            assertThat(restored.asBytes()).isEqualTo(original);
        }

        @Test
        @DisplayName("单字节往返转换")
        void testSingleByteRoundtrip() {
            byte[] original = new byte[]{(byte) 0xFF};
            HashCode hash = HashCode.fromBytes(original);
            String base64 = hash.toBase64();
            HashCode restored = HashCode.fromBase64(base64);

            assertThat(restored.asBytes()).isEqualTo(original);
        }

        @Test
        @DisplayName("大数组往返转换")
        void testLargeArrayRoundtrip() {
            byte[] original = new byte[256];
            for (int i = 0; i < 256; i++) {
                original[i] = (byte) i;
            }
            HashCode hash = HashCode.fromBytes(original);
            String base64 = hash.toBase64();
            HashCode restored = HashCode.fromBase64(base64);

            assertThat(restored.asBytes()).isEqualTo(original);
        }
    }
}
