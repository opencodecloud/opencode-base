package cloud.opencode.base.crypto.codec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * HexCodec 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("HexCodec 测试")
class HexCodecTest {

    @Nested
    @DisplayName("encode 测试")
    class EncodeTests {

        @Test
        @DisplayName("编码空字节数组")
        void testEncodeEmpty() {
            assertThat(HexCodec.encode(new byte[0])).isEqualTo("");
        }

        @Test
        @DisplayName("编码单字节")
        void testEncodeSingleByte() {
            assertThat(HexCodec.encode(new byte[]{(byte) 0xab})).isEqualTo("ab");
        }

        @Test
        @DisplayName("编码多字节")
        void testEncodeMultipleBytes() {
            byte[] data = {0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef};
            assertThat(HexCodec.encode(data)).isEqualTo("0123456789abcdef");
        }

        @Test
        @DisplayName("编码null抛出异常")
        void testEncodeNull() {
            assertThatThrownBy(() -> HexCodec.encode(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("编码全零字节")
        void testEncodeZeros() {
            assertThat(HexCodec.encode(new byte[]{0x00, 0x00, 0x00})).isEqualTo("000000");
        }

        @Test
        @DisplayName("编码全ff字节")
        void testEncodeAllFs() {
            assertThat(HexCodec.encode(new byte[]{(byte) 0xff, (byte) 0xff})).isEqualTo("ffff");
        }
    }

    @Nested
    @DisplayName("encodeUpperCase 测试")
    class EncodeUpperCaseTests {

        @Test
        @DisplayName("大写编码多字节")
        void testEncodeUpperCase() {
            byte[] data = {0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef};
            assertThat(HexCodec.encodeUpperCase(data)).isEqualTo("0123456789ABCDEF");
        }

        @Test
        @DisplayName("大写编码null抛出异常")
        void testEncodeUpperCaseNull() {
            assertThatThrownBy(() -> HexCodec.encodeUpperCase(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("大写编码空数组")
        void testEncodeUpperCaseEmpty() {
            assertThat(HexCodec.encodeUpperCase(new byte[0])).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("decode 测试")
    class DecodeTests {

        @Test
        @DisplayName("解码空字符串")
        void testDecodeEmpty() {
            assertThat(HexCodec.decode("")).isEmpty();
        }

        @Test
        @DisplayName("解码小写十六进制")
        void testDecodeLowercase() {
            byte[] result = HexCodec.decode("0123456789abcdef");
            assertThat(result).containsExactly(0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef);
        }

        @Test
        @DisplayName("解码大写十六进制")
        void testDecodeUppercase() {
            byte[] result = HexCodec.decode("0123456789ABCDEF");
            assertThat(result).containsExactly(0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef);
        }

        @Test
        @DisplayName("解码混合大小写")
        void testDecodeMixedCase() {
            byte[] result = HexCodec.decode("AbCdEf");
            assertThat(result).containsExactly((byte) 0xab, (byte) 0xcd, (byte) 0xef);
        }

        @Test
        @DisplayName("解码null抛出异常")
        void testDecodeNull() {
            assertThatThrownBy(() -> HexCodec.decode(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("解码无效字符抛出异常")
        void testDecodeInvalidChars() {
            assertThatThrownBy(() -> HexCodec.decode("ghij"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid");
        }

        @Test
        @DisplayName("解码奇数长度抛出异常")
        void testDecodeOddLength() {
            assertThatThrownBy(() -> HexCodec.decode("abc"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("isValidHex 测试")
    class IsValidHexTests {

        @Test
        @DisplayName("有效的小写十六进制")
        void testValidLowercase() {
            assertThat(HexCodec.isValidHex("0123456789abcdef")).isTrue();
        }

        @Test
        @DisplayName("有效的大写十六进制")
        void testValidUppercase() {
            assertThat(HexCodec.isValidHex("0123456789ABCDEF")).isTrue();
        }

        @Test
        @DisplayName("混合大小写有效")
        void testValidMixedCase() {
            assertThat(HexCodec.isValidHex("AbCd1234")).isTrue();
        }

        @Test
        @DisplayName("null无效")
        void testNullInvalid() {
            assertThat(HexCodec.isValidHex(null)).isFalse();
        }

        @Test
        @DisplayName("空字符串无效")
        void testEmptyInvalid() {
            assertThat(HexCodec.isValidHex("")).isFalse();
        }

        @Test
        @DisplayName("奇数长度无效")
        void testOddLengthInvalid() {
            assertThat(HexCodec.isValidHex("abc")).isFalse();
        }

        @Test
        @DisplayName("非十六进制字符无效")
        void testInvalidChars() {
            assertThat(HexCodec.isValidHex("ghij")).isFalse();
        }

        @Test
        @DisplayName("包含空格无效")
        void testSpacesInvalid() {
            assertThat(HexCodec.isValidHex("ab cd")).isFalse();
        }
    }

    @Nested
    @DisplayName("往返测试")
    class RoundTripTests {

        @Test
        @DisplayName("编码解码往返 - 普通数据")
        void testRoundTrip() {
            byte[] original = {0x00, 0x01, 0x7f, (byte) 0x80, (byte) 0xfe, (byte) 0xff};
            String encoded = HexCodec.encode(original);
            byte[] decoded = HexCodec.decode(encoded);
            assertThat(decoded).isEqualTo(original);
        }

        @Test
        @DisplayName("编码解码往返 - 大写")
        void testRoundTripUpperCase() {
            byte[] original = {0x12, 0x34, (byte) 0xab, (byte) 0xcd};
            String encoded = HexCodec.encodeUpperCase(original);
            byte[] decoded = HexCodec.decode(encoded);
            assertThat(decoded).isEqualTo(original);
        }

        @Test
        @DisplayName("编码解码往返 - 大量数据")
        void testRoundTripLargeData() {
            byte[] original = new byte[1000];
            for (int i = 0; i < original.length; i++) {
                original[i] = (byte) (i & 0xff);
            }
            String encoded = HexCodec.encode(original);
            byte[] decoded = HexCodec.decode(encoded);
            assertThat(decoded).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("实例化测试")
    class InstantiationTests {

        @Test
        @DisplayName("无法实例化工具类")
        void testCannotInstantiate() {
            assertThatThrownBy(() -> {
                var constructor = HexCodec.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            }).hasCauseInstanceOf(UnsupportedOperationException.class);
        }
    }
}
