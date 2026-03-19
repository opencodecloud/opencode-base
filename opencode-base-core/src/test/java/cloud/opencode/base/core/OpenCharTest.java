package cloud.opencode.base.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenChar 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenChar 测试")
class OpenCharTest {

    @Nested
    @DisplayName("类型检查测试")
    class TypeCheckTests {

        @Test
        @DisplayName("isLetter")
        void testIsLetter() {
            assertThat(OpenChar.isLetter('A')).isTrue();
            assertThat(OpenChar.isLetter('z')).isTrue();
            assertThat(OpenChar.isLetter('中')).isTrue();
            assertThat(OpenChar.isLetter('1')).isFalse();
            assertThat(OpenChar.isLetter(' ')).isFalse();
        }

        @Test
        @DisplayName("isDigit")
        void testIsDigit() {
            assertThat(OpenChar.isDigit('0')).isTrue();
            assertThat(OpenChar.isDigit('9')).isTrue();
            assertThat(OpenChar.isDigit('A')).isFalse();
        }

        @Test
        @DisplayName("isAlphanumeric")
        void testIsAlphanumeric() {
            assertThat(OpenChar.isAlphanumeric('A')).isTrue();
            assertThat(OpenChar.isAlphanumeric('5')).isTrue();
            assertThat(OpenChar.isAlphanumeric(' ')).isFalse();
            assertThat(OpenChar.isAlphanumeric('-')).isFalse();
        }

        @Test
        @DisplayName("isWhitespace")
        void testIsWhitespace() {
            assertThat(OpenChar.isWhitespace(' ')).isTrue();
            assertThat(OpenChar.isWhitespace('\t')).isTrue();
            assertThat(OpenChar.isWhitespace('\n')).isTrue();
            assertThat(OpenChar.isWhitespace('A')).isFalse();
        }

        @Test
        @DisplayName("isAscii")
        void testIsAscii() {
            assertThat(OpenChar.isAscii('A')).isTrue();
            assertThat(OpenChar.isAscii('\0')).isTrue();
            assertThat(OpenChar.isAscii((char) 127)).isTrue();
            assertThat(OpenChar.isAscii('中')).isFalse();
        }

        @Test
        @DisplayName("isPrintableAscii")
        void testIsPrintableAscii() {
            assertThat(OpenChar.isPrintableAscii(' ')).isTrue();
            assertThat(OpenChar.isPrintableAscii('~')).isTrue();
            assertThat(OpenChar.isPrintableAscii('\t')).isFalse();
            assertThat(OpenChar.isPrintableAscii((char) 127)).isFalse();
        }

        @Test
        @DisplayName("isControl")
        void testIsControl() {
            assertThat(OpenChar.isControl('\0')).isTrue();
            assertThat(OpenChar.isControl('\n')).isTrue();
            assertThat(OpenChar.isControl('\t')).isTrue();
            assertThat(OpenChar.isControl('A')).isFalse();
        }

        @Test
        @DisplayName("isUpperCase")
        void testIsUpperCase() {
            assertThat(OpenChar.isUpperCase('A')).isTrue();
            assertThat(OpenChar.isUpperCase('Z')).isTrue();
            assertThat(OpenChar.isUpperCase('a')).isFalse();
            assertThat(OpenChar.isUpperCase('1')).isFalse();
        }

        @Test
        @DisplayName("isLowerCase")
        void testIsLowerCase() {
            assertThat(OpenChar.isLowerCase('a')).isTrue();
            assertThat(OpenChar.isLowerCase('z')).isTrue();
            assertThat(OpenChar.isLowerCase('A')).isFalse();
        }

        @Test
        @DisplayName("isHexDigit")
        void testIsHexDigit() {
            assertThat(OpenChar.isHexDigit('0')).isTrue();
            assertThat(OpenChar.isHexDigit('9')).isTrue();
            assertThat(OpenChar.isHexDigit('a')).isTrue();
            assertThat(OpenChar.isHexDigit('f')).isTrue();
            assertThat(OpenChar.isHexDigit('A')).isTrue();
            assertThat(OpenChar.isHexDigit('F')).isTrue();
            assertThat(OpenChar.isHexDigit('g')).isFalse();
            assertThat(OpenChar.isHexDigit('G')).isFalse();
        }

        @Test
        @DisplayName("isOctalDigit")
        void testIsOctalDigit() {
            assertThat(OpenChar.isOctalDigit('0')).isTrue();
            assertThat(OpenChar.isOctalDigit('7')).isTrue();
            assertThat(OpenChar.isOctalDigit('8')).isFalse();
            assertThat(OpenChar.isOctalDigit('9')).isFalse();
        }
    }

    @Nested
    @DisplayName("大小写转换测试")
    class CaseConversionTests {

        @Test
        @DisplayName("toUpperCase")
        void testToUpperCase() {
            assertThat(OpenChar.toUpperCase('a')).isEqualTo('A');
            assertThat(OpenChar.toUpperCase('z')).isEqualTo('Z');
            assertThat(OpenChar.toUpperCase('A')).isEqualTo('A');
            assertThat(OpenChar.toUpperCase('1')).isEqualTo('1');
        }

        @Test
        @DisplayName("toLowerCase")
        void testToLowerCase() {
            assertThat(OpenChar.toLowerCase('A')).isEqualTo('a');
            assertThat(OpenChar.toLowerCase('Z')).isEqualTo('z');
            assertThat(OpenChar.toLowerCase('a')).isEqualTo('a');
            assertThat(OpenChar.toLowerCase('1')).isEqualTo('1');
        }

        @Test
        @DisplayName("toggleCase")
        void testToggleCase() {
            assertThat(OpenChar.toggleCase('A')).isEqualTo('a');
            assertThat(OpenChar.toggleCase('a')).isEqualTo('A');
            assertThat(OpenChar.toggleCase('1')).isEqualTo('1');
            assertThat(OpenChar.toggleCase(' ')).isEqualTo(' ');
        }
    }

    @Nested
    @DisplayName("转换测试")
    class ConversionTests {

        @Test
        @DisplayName("toString")
        void testToString() {
            assertThat(OpenChar.toString('A')).isEqualTo("A");
            assertThat(OpenChar.toString('中')).isEqualTo("中");
        }

        @Test
        @DisplayName("toString 缓存")
        void testToStringCache() {
            // ASCII 字符应使用缓存
            String s1 = OpenChar.toString('A');
            String s2 = OpenChar.toString('A');
            assertThat(s1).isSameAs(s2);
        }

        @Test
        @DisplayName("toCodePoint")
        void testToCodePoint() {
            assertThat(OpenChar.toCodePoint('A')).isEqualTo(65);
            assertThat(OpenChar.toCodePoint('中')).isEqualTo(0x4E2D);
        }

        @Test
        @DisplayName("fromCodePoint")
        void testFromCodePoint() {
            assertThat(OpenChar.fromCodePoint(65)).isEqualTo('A');
            assertThat(OpenChar.fromCodePoint(0x4E2D)).isEqualTo('中');
        }

        @Test
        @DisplayName("fromCodePoint 无效值")
        void testFromCodePointInvalid() {
            assertThatThrownBy(() -> OpenChar.fromCodePoint(-1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> OpenChar.fromCodePoint(Character.MAX_VALUE + 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("toHexString")
        void testToHexString() {
            assertThat(OpenChar.toHexString('A')).isEqualTo("41");
            assertThat(OpenChar.toHexString('中')).isEqualTo("4e2d");
        }

        @Test
        @DisplayName("toUnicode")
        void testToUnicode() {
            assertThat(OpenChar.toUnicode('A')).isEqualTo("\\u0041");
            assertThat(OpenChar.toUnicode('中')).isEqualTo("\\u4e2d");
        }

        @Test
        @DisplayName("toDigit")
        void testToDigit() {
            assertThat(OpenChar.toDigit('0')).isEqualTo(0);
            assertThat(OpenChar.toDigit('9')).isEqualTo(9);
            assertThat(OpenChar.toDigit('A')).isEqualTo(-1);
        }

        @Test
        @DisplayName("toDigit 指定进制")
        void testToDigitRadix() {
            assertThat(OpenChar.toDigit('A', 16)).isEqualTo(10);
            assertThat(OpenChar.toDigit('F', 16)).isEqualTo(15);
            assertThat(OpenChar.toDigit('G', 16)).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("字符操作测试")
    class OperationTests {

        @Test
        @DisplayName("repeat")
        void testRepeat() {
            assertThat(OpenChar.repeat('A', 3)).isEqualTo("AAA");
            assertThat(OpenChar.repeat('-', 5)).isEqualTo("-----");
            assertThat(OpenChar.repeat('X', 0)).isEmpty();
            assertThat(OpenChar.repeat('X', -1)).isEmpty();
        }

        @Test
        @DisplayName("equalsIgnoreCase")
        void testEqualsIgnoreCase() {
            assertThat(OpenChar.equalsIgnoreCase('a', 'A')).isTrue();
            assertThat(OpenChar.equalsIgnoreCase('A', 'a')).isTrue();
            assertThat(OpenChar.equalsIgnoreCase('a', 'a')).isTrue();
            assertThat(OpenChar.equalsIgnoreCase('a', 'b')).isFalse();
        }

        @Test
        @DisplayName("getNumericValue")
        void testGetNumericValue() {
            assertThat(OpenChar.getNumericValue('0')).isEqualTo(0);
            assertThat(OpenChar.getNumericValue('9')).isEqualTo(9);
            assertThat(OpenChar.getNumericValue('A')).isEqualTo(10);
        }

        @Test
        @DisplayName("inRange")
        void testInRange() {
            assertThat(OpenChar.inRange('b', 'a', 'z')).isTrue();
            assertThat(OpenChar.inRange('a', 'a', 'z')).isTrue();
            assertThat(OpenChar.inRange('z', 'a', 'z')).isTrue();
            assertThat(OpenChar.inRange('A', 'a', 'z')).isFalse();
        }
    }
}
