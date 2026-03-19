package cloud.opencode.base.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenHex 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenHex 测试")
class OpenHexTest {

    @Nested
    @DisplayName("编码测试")
    class EncodeTests {

        @Test
        @DisplayName("encodeHex 小写")
        void testEncodeHex() {
            byte[] data = "Hello".getBytes();
            String hex = OpenHex.encodeHex(data);
            assertThat(hex).isEqualTo("48656c6c6f");
        }

        @Test
        @DisplayName("encodeHexUpper 大写")
        void testEncodeHexUpper() {
            byte[] data = "Hello".getBytes();
            String hex = OpenHex.encodeHexUpper(data);
            assertThat(hex).isEqualTo("48656C6C6F");
        }

        @Test
        @DisplayName("encodeHex null")
        void testEncodeHexNull() {
            assertThat(OpenHex.encodeHex(null)).isNull();
        }

        @Test
        @DisplayName("encodeHex 空数组")
        void testEncodeHexEmpty() {
            assertThat(OpenHex.encodeHex(new byte[0])).isEmpty();
        }

        @Test
        @DisplayName("encodeHexChars")
        void testEncodeHexChars() {
            byte[] data = {0x48, 0x65};
            char[] chars = OpenHex.encodeHexChars(data);
            assertThat(chars).containsExactly('4', '8', '6', '5');
        }

        @Test
        @DisplayName("encodeHexChars 大写")
        void testEncodeHexCharsUpper() {
            byte[] data = {(byte) 0xAB, (byte) 0xCD};
            char[] chars = OpenHex.encodeHexChars(data, false);
            assertThat(chars).containsExactly('A', 'B', 'C', 'D');
        }

        @Test
        @DisplayName("byteToHex 单字节")
        void testByteToHex() {
            assertThat(OpenHex.byteToHex((byte) 0x0F)).isEqualTo("0f");
            assertThat(OpenHex.byteToHex((byte) 0xFF)).isEqualTo("ff");
            assertThat(OpenHex.byteToHex((byte) 0x00)).isEqualTo("00");
        }
    }

    @Nested
    @DisplayName("解码测试")
    class DecodeTests {

        @Test
        @DisplayName("decodeHex 字符串")
        void testDecodeHexString() {
            byte[] result = OpenHex.decodeHex("48656c6c6f");
            assertThat(new String(result)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("decodeHex 大写")
        void testDecodeHexUpperCase() {
            byte[] result = OpenHex.decodeHex("48656C6C6F");
            assertThat(new String(result)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("decodeHex null")
        void testDecodeHexNull() {
            assertThat(OpenHex.decodeHex((String) null)).isNull();
        }

        @Test
        @DisplayName("decodeHex 奇数长度抛异常")
        void testDecodeHexOddLength() {
            assertThatThrownBy(() -> OpenHex.decodeHex("48656"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("even length");
        }

        @Test
        @DisplayName("decodeHex 无效字符抛异常")
        void testDecodeHexInvalidChar() {
            assertThatThrownBy(() -> OpenHex.decodeHex("48GG"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid hex character");
        }

        @Test
        @DisplayName("decodeHex 字符数组")
        void testDecodeHexChars() {
            char[] hex = {'4', '8', '6', '5'};
            byte[] result = OpenHex.decodeHex(hex);
            assertThat(result).containsExactly(0x48, 0x65);
        }

        @Test
        @DisplayName("往返转换")
        void testRoundTrip() {
            byte[] original = {0x00, 0x7F, (byte) 0x80, (byte) 0xFF};
            String hex = OpenHex.encodeHex(original);
            byte[] restored = OpenHex.decodeHex(hex);
            assertThat(restored).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("验证测试")
    class ValidationTests {

        @Test
        @DisplayName("isHexNumber 有效十六进制数")
        void testIsHexNumberValid() {
            assertThat(OpenHex.isHexNumber("0x1a2b")).isTrue();
            assertThat(OpenHex.isHexNumber("0X1A2B")).isTrue();
            assertThat(OpenHex.isHexNumber("-0x1a2b")).isTrue();
            assertThat(OpenHex.isHexNumber("1a2b")).isTrue();
        }

        @Test
        @DisplayName("isHexNumber 无效")
        void testIsHexNumberInvalid() {
            assertThat(OpenHex.isHexNumber(null)).isFalse();
            assertThat(OpenHex.isHexNumber("")).isFalse();
            assertThat(OpenHex.isHexNumber("0x")).isFalse();
            assertThat(OpenHex.isHexNumber("0xGH")).isFalse();
        }

        @Test
        @DisplayName("isHexString 有效")
        void testIsHexStringValid() {
            assertThat(OpenHex.isHexString("0123456789abcdef")).isTrue();
            assertThat(OpenHex.isHexString("ABCDEF")).isTrue();
        }

        @Test
        @DisplayName("isHexString 无效")
        void testIsHexStringInvalid() {
            assertThat(OpenHex.isHexString(null)).isFalse();
            assertThat(OpenHex.isHexString("")).isFalse();
            assertThat(OpenHex.isHexString("0xAB")).isFalse();
            assertThat(OpenHex.isHexString("GH")).isFalse();
        }
    }

    @Nested
    @DisplayName("格式化测试")
    class FormatTests {

        @Test
        @DisplayName("format 添加空格")
        void testFormat() {
            assertThat(OpenHex.format("48656c6c6f")).isEqualTo("48 65 6c 6c 6f");
        }

        @Test
        @DisplayName("format null")
        void testFormatNull() {
            assertThat(OpenHex.format(null)).isNull();
        }

        @Test
        @DisplayName("format 短字符串")
        void testFormatShort() {
            assertThat(OpenHex.format("48")).isEqualTo("48");
        }

        @Test
        @DisplayName("normalize 移除分隔符")
        void testNormalize() {
            assertThat(OpenHex.normalize("48 65 6C 6C 6F")).isEqualTo("48656C6C6F");
            assertThat(OpenHex.normalize("48-65-6C")).isEqualTo("48656C");
        }

        @Test
        @DisplayName("normalize null")
        void testNormalizeNull() {
            assertThat(OpenHex.normalize(null)).isNull();
        }
    }

    @Nested
    @DisplayName("整数转换测试")
    class IntegerConversionTests {

        @Test
        @DisplayName("toHex int")
        void testToHexInt() {
            assertThat(OpenHex.toHex(255)).isEqualTo("ff");
            assertThat(OpenHex.toHex(16)).isEqualTo("10");
        }

        @Test
        @DisplayName("toHex long")
        void testToHexLong() {
            assertThat(OpenHex.toHex(255L)).isEqualTo("ff");
            assertThat(OpenHex.toHex(0x123456789L)).isEqualTo("123456789");
        }

        @Test
        @DisplayName("toInt")
        void testToInt() {
            assertThat(OpenHex.toInt("ff")).isEqualTo(255);
            assertThat(OpenHex.toInt("FF")).isEqualTo(255);
            assertThat(OpenHex.toInt("10")).isEqualTo(16);
        }

        @Test
        @DisplayName("toInt 带空格")
        void testToIntWithSpaces() {
            assertThat(OpenHex.toInt("f f")).isEqualTo(255);
        }

        @Test
        @DisplayName("toLong")
        void testToLong() {
            assertThat(OpenHex.toLong("ff")).isEqualTo(255L);
            assertThat(OpenHex.toLong("123456789")).isEqualTo(0x123456789L);
        }
    }
}
