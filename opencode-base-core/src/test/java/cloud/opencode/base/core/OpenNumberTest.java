package cloud.opencode.base.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenNumber 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenNumber 测试")
class OpenNumberTest {

    @Nested
    @DisplayName("验证测试")
    class ValidationTests {

        @Test
        @DisplayName("isNumber")
        void testIsNumber() {
            assertThat(OpenNumber.isNumber("123")).isTrue();
            assertThat(OpenNumber.isNumber("-123.45")).isTrue();
            assertThat(OpenNumber.isNumber("1.23E10")).isTrue();
            assertThat(OpenNumber.isNumber("abc")).isFalse();
            assertThat(OpenNumber.isNumber(null)).isFalse();
            assertThat(OpenNumber.isNumber("")).isFalse();
        }

        @Test
        @DisplayName("isInteger")
        void testIsInteger() {
            assertThat(OpenNumber.isInteger("123")).isTrue();
            assertThat(OpenNumber.isInteger("-123")).isTrue();
            assertThat(OpenNumber.isInteger("+123")).isTrue();
            assertThat(OpenNumber.isInteger("123.45")).isFalse();
            assertThat(OpenNumber.isInteger("abc")).isFalse();
            assertThat(OpenNumber.isInteger("-")).isFalse();
            assertThat(OpenNumber.isInteger(null)).isFalse();
        }

        @Test
        @DisplayName("isLong")
        void testIsLong() {
            assertThat(OpenNumber.isLong("123")).isTrue();
            assertThat(OpenNumber.isLong("9223372036854775807")).isTrue();
            assertThat(OpenNumber.isLong("9223372036854775808")).isFalse(); // 溢出
            assertThat(OpenNumber.isLong("abc")).isFalse();
        }

        @Test
        @DisplayName("isDouble")
        void testIsDouble() {
            assertThat(OpenNumber.isDouble("123.45")).isTrue();
            assertThat(OpenNumber.isDouble("-123.45")).isTrue();
            assertThat(OpenNumber.isDouble("1.23E10")).isTrue();
            assertThat(OpenNumber.isDouble("Infinity")).isFalse();
            assertThat(OpenNumber.isDouble("NaN")).isFalse();
            assertThat(OpenNumber.isDouble(null)).isFalse();
        }

        @Test
        @DisplayName("isCreatable")
        void testIsCreatable() {
            assertThat(OpenNumber.isCreatable("123")).isTrue();
            assertThat(OpenNumber.isCreatable("0x1A")).isTrue();
            assertThat(OpenNumber.isCreatable("-0x1A")).isTrue();
            assertThat(OpenNumber.isCreatable("017")).isTrue(); // 八进制
            assertThat(OpenNumber.isCreatable("abc")).isFalse();
        }

        @Test
        @DisplayName("isParsable")
        void testIsParsable() {
            assertThat(OpenNumber.isParsable("123")).isTrue();
            assertThat(OpenNumber.isParsable("123.45")).isTrue();
            assertThat(OpenNumber.isParsable("-123.45")).isTrue();
            assertThat(OpenNumber.isParsable("123.")).isFalse();
            assertThat(OpenNumber.isParsable("12.34.56")).isFalse();
            assertThat(OpenNumber.isParsable("-")).isFalse();
        }
    }

    @Nested
    @DisplayName("解析测试")
    class ParseTests {

        @Test
        @DisplayName("toInt 带默认值")
        void testToInt() {
            assertThat(OpenNumber.toInt("123", 0)).isEqualTo(123);
            assertThat(OpenNumber.toInt(" 123 ", 0)).isEqualTo(123);
            assertThat(OpenNumber.toInt("abc", 99)).isEqualTo(99);
            assertThat(OpenNumber.toInt(null, 99)).isEqualTo(99);
        }

        @Test
        @DisplayName("toLong 带默认值")
        void testToLong() {
            assertThat(OpenNumber.toLong("123", 0L)).isEqualTo(123L);
            assertThat(OpenNumber.toLong("abc", 99L)).isEqualTo(99L);
            assertThat(OpenNumber.toLong(null, 99L)).isEqualTo(99L);
        }

        @Test
        @DisplayName("toFloat 带默认值")
        void testToFloat() {
            assertThat(OpenNumber.toFloat("1.5", 0f)).isEqualTo(1.5f);
            assertThat(OpenNumber.toFloat("abc", 99f)).isEqualTo(99f);
            assertThat(OpenNumber.toFloat(null, 99f)).isEqualTo(99f);
        }

        @Test
        @DisplayName("toDouble 带默认值")
        void testToDouble() {
            assertThat(OpenNumber.toDouble("1.5", 0.0)).isEqualTo(1.5);
            assertThat(OpenNumber.toDouble("abc", 99.0)).isEqualTo(99.0);
            assertThat(OpenNumber.toDouble(null, 99.0)).isEqualTo(99.0);
        }

        @Test
        @DisplayName("toBigDecimal")
        void testToBigDecimal() {
            assertThat(OpenNumber.toBigDecimal("123.45")).isEqualTo(new BigDecimal("123.45"));
            assertThat(OpenNumber.toBigDecimal("abc")).isNull();
            assertThat(OpenNumber.toBigDecimal(null)).isNull();
        }

        @Test
        @DisplayName("toBigDecimal 带默认值")
        void testToBigDecimalDefault() {
            BigDecimal def = new BigDecimal("99");
            assertThat(OpenNumber.toBigDecimal("abc", def)).isEqualTo(def);
        }

        @Test
        @DisplayName("toBigInteger")
        void testToBigInteger() {
            assertThat(OpenNumber.toBigInteger("12345")).isEqualTo(new BigInteger("12345"));
            assertThat(OpenNumber.toBigInteger("abc")).isNull();
            assertThat(OpenNumber.toBigInteger(null)).isNull();
        }
    }

    @Nested
    @DisplayName("安全解析测试")
    class SafeParseTests {

        @Test
        @DisplayName("tryParseInt")
        void testTryParseInt() {
            OptionalInt opt = OpenNumber.tryParseInt("123");
            assertThat(opt).isPresent();
            assertThat(opt.getAsInt()).isEqualTo(123);

            assertThat(OpenNumber.tryParseInt("abc")).isEmpty();
            assertThat(OpenNumber.tryParseInt(null)).isEmpty();
        }

        @Test
        @DisplayName("tryParseLong")
        void testTryParseLong() {
            OptionalLong opt = OpenNumber.tryParseLong("123");
            assertThat(opt).isPresent();
            assertThat(opt.getAsLong()).isEqualTo(123L);

            assertThat(OpenNumber.tryParseLong("abc")).isEmpty();
        }

        @Test
        @DisplayName("tryParseDouble")
        void testTryParseDouble() {
            OptionalDouble opt = OpenNumber.tryParseDouble("1.5");
            assertThat(opt).isPresent();
            assertThat(opt.getAsDouble()).isEqualTo(1.5);

            assertThat(OpenNumber.tryParseDouble("abc")).isEmpty();
        }
    }

    @Nested
    @DisplayName("溢出安全转换测试")
    class OverflowTests {

        @Test
        @DisplayName("saturatedCast long to int")
        void testSaturatedCastLong() {
            assertThat(OpenNumber.saturatedCast(100L)).isEqualTo(100);
            assertThat(OpenNumber.saturatedCast(Long.MAX_VALUE)).isEqualTo(Integer.MAX_VALUE);
            assertThat(OpenNumber.saturatedCast(Long.MIN_VALUE)).isEqualTo(Integer.MIN_VALUE);
        }

        @Test
        @DisplayName("checkedCast 正常")
        void testCheckedCastNormal() {
            assertThat(OpenNumber.checkedCast(100L)).isEqualTo(100);
        }

        @Test
        @DisplayName("checkedCast 溢出")
        void testCheckedCastOverflow() {
            assertThatThrownBy(() -> OpenNumber.checkedCast(Long.MAX_VALUE))
                    .isInstanceOf(ArithmeticException.class);
        }

        @Test
        @DisplayName("saturatedCast BigDecimal")
        void testSaturatedCastBigDecimal() {
            assertThat(OpenNumber.saturatedCast(new BigDecimal("100"))).isEqualTo(100);
            assertThat(OpenNumber.saturatedCast(new BigDecimal("9999999999999"))).isEqualTo(Integer.MAX_VALUE);
            assertThat(OpenNumber.saturatedCast((BigDecimal) null)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("比较测试")
    class CompareTests {

        @Test
        @DisplayName("compare int")
        void testCompareInt() {
            assertThat(OpenNumber.compare(1, 2)).isLessThan(0);
            assertThat(OpenNumber.compare(2, 1)).isGreaterThan(0);
            assertThat(OpenNumber.compare(1, 1)).isEqualTo(0);
        }

        @Test
        @DisplayName("compare long")
        void testCompareLong() {
            assertThat(OpenNumber.compare(1L, 2L)).isLessThan(0);
        }

        @Test
        @DisplayName("compare double")
        void testCompareDouble() {
            assertThat(OpenNumber.compare(1.0, 2.0)).isLessThan(0);
        }

        @Test
        @DisplayName("max 和 min Comparable")
        void testMaxMinComparable() {
            assertThat(OpenNumber.max("a", "b")).isEqualTo("b");
            assertThat(OpenNumber.min("a", "b")).isEqualTo("a");
            assertThat(OpenNumber.max(null, "b")).isEqualTo("b");
            assertThat(OpenNumber.min("a", null)).isEqualTo("a");
        }

        @Test
        @DisplayName("max 和 min int 数组")
        void testMaxMinIntArray() {
            assertThat(OpenNumber.max(1, 5, 3, 2)).isEqualTo(5);
            assertThat(OpenNumber.min(1, 5, 3, 2)).isEqualTo(1);
        }

        @Test
        @DisplayName("max 和 min 空数组抛异常")
        void testMaxMinEmptyArray() {
            assertThatThrownBy(() -> OpenNumber.max(new int[0]))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> OpenNumber.min(new int[0]))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("max 和 min long 数组")
        void testMaxMinLongArray() {
            assertThat(OpenNumber.max(1L, 5L, 3L)).isEqualTo(5L);
            assertThat(OpenNumber.min(1L, 5L, 3L)).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("范围控制测试")
    class RangeTests {

        @Test
        @DisplayName("clamp int")
        void testClampInt() {
            assertThat(OpenNumber.clamp(5, 0, 10)).isEqualTo(5);
            assertThat(OpenNumber.clamp(-5, 0, 10)).isEqualTo(0);
            assertThat(OpenNumber.clamp(15, 0, 10)).isEqualTo(10);
        }

        @Test
        @DisplayName("clamp long")
        void testClampLong() {
            assertThat(OpenNumber.clamp(5L, 0L, 10L)).isEqualTo(5L);
        }

        @Test
        @DisplayName("clamp double")
        void testClampDouble() {
            assertThat(OpenNumber.clamp(5.0, 0.0, 10.0)).isEqualTo(5.0);
        }

        @Test
        @DisplayName("constrainToRange")
        void testConstrainToRange() {
            assertThat(OpenNumber.constrainToRange(5, 0, 10)).isEqualTo(5);
        }

        @Test
        @DisplayName("inRange int")
        void testInRangeInt() {
            assertThat(OpenNumber.inRange(5, 0, 10)).isTrue();
            assertThat(OpenNumber.inRange(0, 0, 10)).isTrue();
            assertThat(OpenNumber.inRange(10, 0, 10)).isTrue();
            assertThat(OpenNumber.inRange(15, 0, 10)).isFalse();
        }

        @Test
        @DisplayName("inRange long")
        void testInRangeLong() {
            assertThat(OpenNumber.inRange(5L, 0L, 10L)).isTrue();
            assertThat(OpenNumber.inRange(15L, 0L, 10L)).isFalse();
        }
    }

    @Nested
    @DisplayName("高精度运算测试")
    class ArithmeticTests {

        @Test
        @DisplayName("add 高精度加法")
        void testAdd() {
            BigDecimal result = OpenNumber.add(1, 2, 3);
            assertThat(result).isEqualTo(new BigDecimal("6"));
        }

        @Test
        @DisplayName("add null 处理")
        void testAddNull() {
            assertThat(OpenNumber.add((Number[]) null)).isEqualTo(BigDecimal.ZERO);
            assertThat(OpenNumber.add(1, null, 2)).isEqualTo(new BigDecimal("3"));
        }

        @Test
        @DisplayName("subtract 高精度减法")
        void testSubtract() {
            BigDecimal result = OpenNumber.subtract(new BigDecimal("10"), new BigDecimal("3"));
            assertThat(result).isEqualTo(new BigDecimal("7"));
        }

        @Test
        @DisplayName("subtract null 处理")
        void testSubtractNull() {
            assertThat(OpenNumber.subtract(null, null)).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("multiply 高精度乘法")
        void testMultiply() {
            BigDecimal result = OpenNumber.multiply(new BigDecimal("3"), new BigDecimal("4"));
            assertThat(result).isEqualTo(new BigDecimal("12"));
        }

        @Test
        @DisplayName("multiply null 返回 0")
        void testMultiplyNull() {
            assertThat(OpenNumber.multiply(null, new BigDecimal("3"))).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("divide 高精度除法")
        void testDivide() {
            BigDecimal result = OpenNumber.divide(new BigDecimal("10"), new BigDecimal("3"), 2);
            assertThat(result).isEqualTo(new BigDecimal("3.33"));
        }

        @Test
        @DisplayName("divide 指定舍入模式")
        void testDivideWithMode() {
            BigDecimal result = OpenNumber.divide(new BigDecimal("10"), new BigDecimal("3"), 2, RoundingMode.DOWN);
            assertThat(result).isEqualTo(new BigDecimal("3.33"));
        }

        @Test
        @DisplayName("divide 除零异常")
        void testDivideByZero() {
            assertThatThrownBy(() -> OpenNumber.divide(new BigDecimal("10"), BigDecimal.ZERO, 2))
                    .isInstanceOf(ArithmeticException.class);
        }
    }

    @Nested
    @DisplayName("四舍五入测试")
    class RoundTests {

        @Test
        @DisplayName("round BigDecimal")
        void testRoundBigDecimal() {
            BigDecimal result = OpenNumber.round(new BigDecimal("3.456"), 2);
            assertThat(result).isEqualTo(new BigDecimal("3.46"));
        }

        @Test
        @DisplayName("round BigDecimal 指定模式")
        void testRoundBigDecimalMode() {
            BigDecimal result = OpenNumber.round(new BigDecimal("3.455"), 2, RoundingMode.DOWN);
            assertThat(result).isEqualTo(new BigDecimal("3.45"));
        }

        @Test
        @DisplayName("round double")
        void testRoundDouble() {
            assertThat(OpenNumber.round(3.456, 2)).isEqualTo(3.46);
        }

        @Test
        @DisplayName("round 负数 scale 抛异常")
        void testRoundNegativeScale() {
            assertThatThrownBy(() -> OpenNumber.round(3.456, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("roundHalfEven 银行家舍入")
        void testRoundHalfEven() {
            assertThat(OpenNumber.roundHalfEven(new BigDecimal("2.5"), 0)).isEqualTo(new BigDecimal("2"));
            assertThat(OpenNumber.roundHalfEven(new BigDecimal("3.5"), 0)).isEqualTo(new BigDecimal("4"));
        }
    }

    @Nested
    @DisplayName("格式化测试")
    class FormatTests {

        @Test
        @DisplayName("format double")
        void testFormatDouble() {
            String result = OpenNumber.format(1234.567, "#,##0.00");
            assertThat(result).isEqualTo("1,234.57");
        }

        @Test
        @DisplayName("format BigDecimal")
        void testFormatBigDecimal() {
            String result = OpenNumber.format(new BigDecimal("1234.567"), "#,##0.00");
            assertThat(result).isEqualTo("1,234.57");
        }

        @Test
        @DisplayName("format BigDecimal null")
        void testFormatBigDecimalNull() {
            assertThat(OpenNumber.format((BigDecimal) null, "#")).isEmpty();
        }

        @Test
        @DisplayName("formatPercent")
        void testFormatPercent() {
            String result = OpenNumber.formatPercent(0.1234, 2);
            assertThat(result).contains("12.34");
        }

        @Test
        @DisplayName("formatMoney")
        void testFormatMoney() {
            String result = OpenNumber.formatMoney(new BigDecimal("1234.56"));
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("formatMoney null")
        void testFormatMoneyNull() {
            assertThat(OpenNumber.formatMoney(null)).isEmpty();
        }
    }
}
