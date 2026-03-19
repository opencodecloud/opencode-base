package cloud.opencode.base.date.extra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Months 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("Months 测试")
class MonthsTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of(int) 创建Months")
        void testOfInt() {
            Months m = Months.of(6);
            assertThat(m.getAmount()).isEqualTo(6);
        }

        @Test
        @DisplayName("of(long) 创建Months")
        void testOfLong() {
            Months m = Months.of(6L);
            assertThat(m.getAmount()).isEqualTo(6);
        }

        @Test
        @DisplayName("of() 零值返回ZERO常量")
        void testOfZero() {
            assertThat(Months.of(0)).isSameAs(Months.ZERO);
        }

        @Test
        @DisplayName("of() 1返回ONE常量")
        void testOfOne() {
            assertThat(Months.of(1)).isSameAs(Months.ONE);
        }

        @Test
        @DisplayName("ofYears() 从年数创建")
        void testOfYears() {
            Months m = Months.ofYears(2);
            assertThat(m.getAmount()).isEqualTo(24);
        }

        @Test
        @DisplayName("from() 从Period创建")
        void testFrom() {
            Months m = Months.from(Period.of(1, 3, 0));
            assertThat(m.getAmount()).isEqualTo(15);
        }

        @Test
        @DisplayName("from() null抛出异常")
        void testFromNull() {
            assertThatThrownBy(() -> Months.from(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("between() 计算两个日期之间的月数")
        void testBetween() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 7, 1);
            Months m = Months.between(start, end);
            assertThat(m.getAmount()).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("获取器测试")
    class GetterTests {

        @Test
        @DisplayName("isZero() 检查是否为零")
        void testIsZero() {
            assertThat(Months.ZERO.isZero()).isTrue();
            assertThat(Months.of(1).isZero()).isFalse();
        }

        @Test
        @DisplayName("isNegative() 检查是否为负")
        void testIsNegative() {
            assertThat(Months.of(-1).isNegative()).isTrue();
            assertThat(Months.of(0).isNegative()).isFalse();
            assertThat(Months.of(1).isNegative()).isFalse();
        }

        @Test
        @DisplayName("isPositive() 检查是否为正")
        void testIsPositive() {
            assertThat(Months.of(1).isPositive()).isTrue();
            assertThat(Months.of(0).isPositive()).isFalse();
            assertThat(Months.of(-1).isPositive()).isFalse();
        }
    }

    @Nested
    @DisplayName("计算方法测试")
    class CalculationTests {

        @Test
        @DisplayName("plus(Months) 加月数")
        void testPlusMonths() {
            Months result = Months.of(6).plus(Months.of(3));
            assertThat(result.getAmount()).isEqualTo(9);
        }

        @Test
        @DisplayName("plus(int) 加月数值")
        void testPlusInt() {
            Months result = Months.of(6).plus(3);
            assertThat(result.getAmount()).isEqualTo(9);
        }

        @Test
        @DisplayName("minus(Months) 减月数")
        void testMinusMonths() {
            Months result = Months.of(6).minus(Months.of(2));
            assertThat(result.getAmount()).isEqualTo(4);
        }

        @Test
        @DisplayName("minus(int) 减月数值")
        void testMinusInt() {
            Months result = Months.of(6).minus(2);
            assertThat(result.getAmount()).isEqualTo(4);
        }

        @Test
        @DisplayName("multipliedBy() 乘以标量")
        void testMultipliedBy() {
            Months result = Months.of(3).multipliedBy(4);
            assertThat(result.getAmount()).isEqualTo(12);
        }

        @Test
        @DisplayName("dividedBy() 除以标量")
        void testDividedBy() {
            Months result = Months.of(12).dividedBy(4);
            assertThat(result.getAmount()).isEqualTo(3);
        }

        @Test
        @DisplayName("negated() 取负")
        void testNegated() {
            assertThat(Months.of(6).negated().getAmount()).isEqualTo(-6);
            assertThat(Months.of(-6).negated().getAmount()).isEqualTo(6);
        }

        @Test
        @DisplayName("abs() 取绝对值")
        void testAbs() {
            assertThat(Months.of(-6).abs().getAmount()).isEqualTo(6);
            assertThat(Months.of(6).abs().getAmount()).isEqualTo(6);
        }

        @Test
        @DisplayName("abs() 正数返回自身")
        void testAbsPositive() {
            Months m = Months.of(6);
            assertThat(m.abs()).isSameAs(m);
        }
    }

    @Nested
    @DisplayName("转换方法测试")
    class ConversionTests {

        @Test
        @DisplayName("toPeriod() 转换为Period")
        void testToPeriod() {
            Period p = Months.of(6).toPeriod();
            assertThat(p).isEqualTo(Period.ofMonths(6));
        }

        @Test
        @DisplayName("toYears() 转换为Years")
        void testToYears() {
            Years y = Months.of(24).toYears();
            assertThat(y.getAmount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("TemporalAmount实现测试")
    class TemporalAmountTests {

        @Test
        @DisplayName("get() 获取单位值")
        void testGet() {
            Months m = Months.of(6);
            assertThat(m.get(ChronoUnit.MONTHS)).isEqualTo(6);
        }

        @Test
        @DisplayName("get() 不支持单位抛出异常")
        void testGetUnsupported() {
            assertThatThrownBy(() -> Months.of(6).get(ChronoUnit.YEARS))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getUnits() 获取支持的单位")
        void testGetUnits() {
            assertThat(Months.ZERO.getUnits()).containsExactly(ChronoUnit.MONTHS);
        }

        @Test
        @DisplayName("addTo() 加到日期")
        void testAddTo() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            LocalDate result = (LocalDate) Months.of(3).addTo(date);
            assertThat(result).isEqualTo(LocalDate.of(2024, 4, 15));
        }

        @Test
        @DisplayName("subtractFrom() 从日期减去")
        void testSubtractFrom() {
            LocalDate date = LocalDate.of(2024, 4, 15);
            LocalDate result = (LocalDate) Months.of(3).subtractFrom(date);
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 15));
        }
    }

    @Nested
    @DisplayName("Comparable实现测试")
    class ComparableTests {

        @Test
        @DisplayName("compareTo() 比较")
        void testCompareTo() {
            Months m1 = Months.of(6);
            Months m2 = Months.of(12);
            Months m3 = Months.of(6);

            assertThat(m1.compareTo(m2)).isLessThan(0);
            assertThat(m2.compareTo(m1)).isGreaterThan(0);
            assertThat(m1.compareTo(m3)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等对象")
        void testEquals() {
            Months m1 = Months.of(6);
            Months m2 = Months.of(6);
            Months m3 = Months.of(7);

            assertThat(m1).isEqualTo(m2);
            assertThat(m1).isNotEqualTo(m3);
            assertThat(m1).isEqualTo(m1);
            assertThat(m1).isNotEqualTo(null);
            assertThat(m1).isNotEqualTo("6");
        }

        @Test
        @DisplayName("hashCode() 相等对象相同哈希码")
        void testHashCode() {
            Months m1 = Months.of(6);
            Months m2 = Months.of(6);
            assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            assertThat(Months.of(6).toString()).isEqualTo("P6M");
        }
    }
}
