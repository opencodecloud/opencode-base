package cloud.opencode.base.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenRadix 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenRadix 测试")
class OpenRadixTest {

    @Nested
    @DisplayName("十进制转其他进制测试")
    class DecimalToOtherTests {

        @Test
        @DisplayName("decimalToBinary")
        void testDecimalToBinary() {
            assertThat(OpenRadix.decimalToBinary(0)).isEqualTo("0");
            assertThat(OpenRadix.decimalToBinary(1)).isEqualTo("1");
            assertThat(OpenRadix.decimalToBinary(255)).isEqualTo("11111111");
            assertThat(OpenRadix.decimalToBinary(1024)).isEqualTo("10000000000");
        }

        @Test
        @DisplayName("decimalToOctal")
        void testDecimalToOctal() {
            assertThat(OpenRadix.decimalToOctal(0)).isEqualTo("0");
            assertThat(OpenRadix.decimalToOctal(8)).isEqualTo("10");
            assertThat(OpenRadix.decimalToOctal(64)).isEqualTo("100");
            assertThat(OpenRadix.decimalToOctal(255)).isEqualTo("377");
        }

        @Test
        @DisplayName("decimalToHexadecimal")
        void testDecimalToHexadecimal() {
            assertThat(OpenRadix.decimalToHexadecimal(0)).isEqualTo("0");
            assertThat(OpenRadix.decimalToHexadecimal(15)).isEqualTo("F");
            assertThat(OpenRadix.decimalToHexadecimal(255)).isEqualTo("FF");
            assertThat(OpenRadix.decimalToHexadecimal(4096)).isEqualTo("1000");
        }

        @Test
        @DisplayName("decimalToHexadecimalLower")
        void testDecimalToHexadecimalLower() {
            assertThat(OpenRadix.decimalToHexadecimalLower(255)).isEqualTo("ff");
        }

        @Test
        @DisplayName("toBase")
        void testToBase() {
            assertThat(OpenRadix.toBase(1000, 36)).isEqualTo("RS");
            assertThat(OpenRadix.toBase(100, 2)).isEqualTo("1100100");
            assertThat(OpenRadix.toBase(100, 16)).isEqualTo("64");
        }

        @Test
        @DisplayName("toBase 无效进制抛异常")
        void testToBaseInvalidRadix() {
            assertThatThrownBy(() -> OpenRadix.toBase(100, 1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> OpenRadix.toBase(100, 37))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("toBaseExtended")
        void testToBaseExtended() {
            assertThat(OpenRadix.toBaseExtended(0, 62)).isEqualTo("0");
            assertThat(OpenRadix.toBaseExtended(100, 36)).isEqualTo("2S");
            // 62 进制测试
            assertThat(OpenRadix.toBaseExtended(61, 62)).isEqualTo("z");
        }

        @Test
        @DisplayName("toBaseExtended 负数")
        void testToBaseExtendedNegative() {
            String result = OpenRadix.toBaseExtended(-100, 62);
            assertThat(result).startsWith("-");
        }

        @Test
        @DisplayName("toBaseExtended 无效进制抛异常")
        void testToBaseExtendedInvalidRadix() {
            assertThatThrownBy(() -> OpenRadix.toBaseExtended(100, 1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> OpenRadix.toBaseExtended(100, 63))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("其他进制转十进制测试")
    class OtherToDecimalTests {

        @Test
        @DisplayName("binaryToDecimal")
        void testBinaryToDecimal() {
            assertThat(OpenRadix.binaryToDecimal("0")).isEqualTo(0);
            assertThat(OpenRadix.binaryToDecimal("1")).isEqualTo(1);
            assertThat(OpenRadix.binaryToDecimal("11111111")).isEqualTo(255);
            assertThat(OpenRadix.binaryToDecimal("10000000000")).isEqualTo(1024);
        }

        @Test
        @DisplayName("octalToDecimal")
        void testOctalToDecimal() {
            assertThat(OpenRadix.octalToDecimal("0")).isEqualTo(0);
            assertThat(OpenRadix.octalToDecimal("10")).isEqualTo(8);
            assertThat(OpenRadix.octalToDecimal("377")).isEqualTo(255);
        }

        @Test
        @DisplayName("hexadecimalToDecimal")
        void testHexadecimalToDecimal() {
            assertThat(OpenRadix.hexadecimalToDecimal("0")).isEqualTo(0);
            assertThat(OpenRadix.hexadecimalToDecimal("F")).isEqualTo(15);
            assertThat(OpenRadix.hexadecimalToDecimal("FF")).isEqualTo(255);
            assertThat(OpenRadix.hexadecimalToDecimal("ff")).isEqualTo(255);
        }

        @Test
        @DisplayName("fromBase")
        void testFromBase() {
            assertThat(OpenRadix.fromBase("RS", 36)).isEqualTo(1000);
            assertThat(OpenRadix.fromBase("1100100", 2)).isEqualTo(100);
        }

        @Test
        @DisplayName("fromBase 无效进制抛异常")
        void testFromBaseInvalidRadix() {
            assertThatThrownBy(() -> OpenRadix.fromBase("100", 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("fromBaseExtended")
        void testFromBaseExtended() {
            assertThat(OpenRadix.fromBaseExtended("z", 62)).isEqualTo(61);
            assertThat(OpenRadix.fromBaseExtended("2S", 36)).isEqualTo(100);
        }

        @Test
        @DisplayName("fromBaseExtended 负数")
        void testFromBaseExtendedNegative() {
            long result = OpenRadix.fromBaseExtended("-10", 62);
            assertThat(result).isLessThan(0);
        }

        @Test
        @DisplayName("fromBaseExtended 无效字符抛异常")
        void testFromBaseExtendedInvalidChar() {
            assertThatThrownBy(() -> OpenRadix.fromBaseExtended("!", 62))
                    .isInstanceOf(NumberFormatException.class);
        }
    }

    @Nested
    @DisplayName("进制间转换测试")
    class InterRadixTests {

        @Test
        @DisplayName("binaryToHex")
        void testBinaryToHex() {
            assertThat(OpenRadix.binaryToHex("11111111")).isEqualTo("FF");
            assertThat(OpenRadix.binaryToHex("10000")).isEqualTo("10");
        }

        @Test
        @DisplayName("hexToBinary")
        void testHexToBinary() {
            assertThat(OpenRadix.hexToBinary("FF")).isEqualTo("11111111");
            assertThat(OpenRadix.hexToBinary("10")).isEqualTo("10000");
        }

        @Test
        @DisplayName("octalToHex")
        void testOctalToHex() {
            assertThat(OpenRadix.octalToHex("377")).isEqualTo("FF");
        }

        @Test
        @DisplayName("hexToOctal")
        void testHexToOctal() {
            assertThat(OpenRadix.hexToOctal("FF")).isEqualTo("377");
        }

        @Test
        @DisplayName("convert 通用转换")
        void testConvert() {
            assertThat(OpenRadix.convert("FF", 16, 2)).isEqualTo("11111111");
            assertThat(OpenRadix.convert("100", 10, 16)).isEqualTo("64");
            assertThat(OpenRadix.convert("1010", 2, 8)).isEqualTo("12");
        }
    }

    @Nested
    @DisplayName("格式化测试")
    class FormatTests {

        @Test
        @DisplayName("formatBinary")
        void testFormatBinary() {
            String result = OpenRadix.formatBinary(255);
            assertThat(result).isEqualTo("1111_1111");
        }

        @Test
        @DisplayName("formatBinary 不足 4 位")
        void testFormatBinaryShort() {
            String result = OpenRadix.formatBinary(5);
            assertThat(result).isEqualTo("101");
        }

        @Test
        @DisplayName("formatHex")
        void testFormatHex() {
            String result = OpenRadix.formatHex(0xABCD);
            assertThat(result).isEqualTo("AB CD");
        }

        @Test
        @DisplayName("formatHex 奇数位")
        void testFormatHexOdd() {
            String result = OpenRadix.formatHex(0xABC);
            assertThat(result).isEqualTo("A BC");
        }
    }

    @Nested
    @DisplayName("验证测试")
    class ValidationTests {

        @Test
        @DisplayName("isBinary")
        void testIsBinary() {
            assertThat(OpenRadix.isBinary("0")).isTrue();
            assertThat(OpenRadix.isBinary("1")).isTrue();
            assertThat(OpenRadix.isBinary("01010101")).isTrue();
            assertThat(OpenRadix.isBinary("2")).isFalse();
            assertThat(OpenRadix.isBinary("abc")).isFalse();
            assertThat(OpenRadix.isBinary(null)).isFalse();
            assertThat(OpenRadix.isBinary("")).isFalse();
        }

        @Test
        @DisplayName("isOctal")
        void testIsOctal() {
            assertThat(OpenRadix.isOctal("0")).isTrue();
            assertThat(OpenRadix.isOctal("7")).isTrue();
            assertThat(OpenRadix.isOctal("01234567")).isTrue();
            assertThat(OpenRadix.isOctal("8")).isFalse();
            assertThat(OpenRadix.isOctal("9")).isFalse();
            assertThat(OpenRadix.isOctal(null)).isFalse();
            assertThat(OpenRadix.isOctal("")).isFalse();
        }

        @Test
        @DisplayName("isHexadecimal")
        void testIsHexadecimal() {
            assertThat(OpenRadix.isHexadecimal("0123456789")).isTrue();
            assertThat(OpenRadix.isHexadecimal("abcdef")).isTrue();
            assertThat(OpenRadix.isHexadecimal("ABCDEF")).isTrue();
            assertThat(OpenRadix.isHexadecimal("0123456789ABCDEFabcdef")).isTrue();
            assertThat(OpenRadix.isHexadecimal("G")).isFalse();
            assertThat(OpenRadix.isHexadecimal("xyz")).isFalse();
            assertThat(OpenRadix.isHexadecimal(null)).isFalse();
            assertThat(OpenRadix.isHexadecimal("")).isFalse();
        }
    }

    @Nested
    @DisplayName("往返测试")
    class RoundTripTests {

        @Test
        @DisplayName("二进制往返")
        void testBinaryRoundTrip() {
            long original = 12345;
            String binary = OpenRadix.decimalToBinary(original);
            long restored = OpenRadix.binaryToDecimal(binary);
            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("八进制往返")
        void testOctalRoundTrip() {
            long original = 12345;
            String octal = OpenRadix.decimalToOctal(original);
            long restored = OpenRadix.octalToDecimal(octal);
            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("十六进制往返")
        void testHexRoundTrip() {
            long original = 12345;
            String hex = OpenRadix.decimalToHexadecimal(original);
            long restored = OpenRadix.hexadecimalToDecimal(hex);
            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("自定义进制往返")
        void testCustomRadixRoundTrip() {
            long original = 12345;
            String base36 = OpenRadix.toBase(original, 36);
            long restored = OpenRadix.fromBase(base36, 36);
            assertThat(restored).isEqualTo(original);
        }
    }
}
