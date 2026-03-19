package cloud.opencode.base.date.extra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Years 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("Years 测试")
class YearsTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of(int) 创建Years")
        void testOfInt() {
            Years y = Years.of(5);
            assertThat(y.getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("of(long) 创建Years")
        void testOfLong() {
            Years y = Years.of(5L);
            assertThat(y.getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("of() 零值返回ZERO常量")
        void testOfZero() {
            assertThat(Years.of(0)).isSameAs(Years.ZERO);
        }

        @Test
        @DisplayName("of() 1返回ONE常量")
        void testOfOne() {
            assertThat(Years.of(1)).isSameAs(Years.ONE);
        }

        @Test
        @DisplayName("from() 从Period创建")
        void testFrom() {
            Years y = Years.from(Period.ofYears(3));
            assertThat(y.getAmount()).isEqualTo(3);
        }

        @Test
        @DisplayName("from() null抛出异常")
        void testFromNull() {
            assertThatThrownBy(() -> Years.from(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("between() 计算两个日期之间的年数")
        void testBetween() {
            LocalDate start = LocalDate.of(2020, 1, 1);
            LocalDate end = LocalDate.of(2024, 1, 1);
            Years y = Years.between(start, end);
            assertThat(y.getAmount()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("获取器测试")
    class GetterTests {

        @Test
        @DisplayName("isZero() 检查是否为零")
        void testIsZero() {
            assertThat(Years.ZERO.isZero()).isTrue();
            assertThat(Years.of(1).isZero()).isFalse();
        }

        @Test
        @DisplayName("isNegative() 检查是否为负")
        void testIsNegative() {
            assertThat(Years.of(-1).isNegative()).isTrue();
            assertThat(Years.of(0).isNegative()).isFalse();
            assertThat(Years.of(1).isNegative()).isFalse();
        }

        @Test
        @DisplayName("isPositive() 检查是否为正")
        void testIsPositive() {
            assertThat(Years.of(1).isPositive()).isTrue();
            assertThat(Years.of(0).isPositive()).isFalse();
            assertThat(Years.of(-1).isPositive()).isFalse();
        }

        @Test
        @DisplayName("isLeap() 检查是否闰年")
        void testIsLeap() {
            assertThat(Years.of(2024).isLeap()).isTrue();
            assertThat(Years.of(2023).isLeap()).isFalse();
            assertThat(Years.of(2000).isLeap()).isTrue();
            assertThat(Years.of(1900).isLeap()).isFalse();
            assertThat(Years.of(-1).isLeap()).isFalse(); // negative years
        }
    }

    @Nested
    @DisplayName("计算方法测试")
    class CalculationTests {

        @Test
        @DisplayName("plus(Years) 加年数")
        void testPlusYears() {
            Years result = Years.of(3).plus(Years.of(2));
            assertThat(result.getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("plus(int) 加年数值")
        void testPlusInt() {
            Years result = Years.of(3).plus(2);
            assertThat(result.getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("minus(Years) 减年数")
        void testMinusYears() {
            Years result = Years.of(5).minus(Years.of(2));
            assertThat(result.getAmount()).isEqualTo(3);
        }

        @Test
        @DisplayName("minus(int) 减年数值")
        void testMinusInt() {
            Years result = Years.of(5).minus(2);
            assertThat(result.getAmount()).isEqualTo(3);
        }

        @Test
        @DisplayName("multipliedBy() 乘以标量")
        void testMultipliedBy() {
            Years result = Years.of(3).multipliedBy(4);
            assertThat(result.getAmount()).isEqualTo(12);
        }

        @Test
        @DisplayName("dividedBy() 除以标量")
        void testDividedBy() {
            Years result = Years.of(12).dividedBy(4);
            assertThat(result.getAmount()).isEqualTo(3);
        }

        @Test
        @DisplayName("negated() 取负")
        void testNegated() {
            assertThat(Years.of(5).negated().getAmount()).isEqualTo(-5);
            assertThat(Years.of(-5).negated().getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("abs() 取绝对值")
        void testAbs() {
            assertThat(Years.of(-5).abs().getAmount()).isEqualTo(5);
            assertThat(Years.of(5).abs().getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("abs() 正数返回自身")
        void testAbsPositive() {
            Years y = Years.of(5);
            assertThat(y.abs()).isSameAs(y);
        }
    }

    @Nested
    @DisplayName("转换方法测试")
    class ConversionTests {

        @Test
        @DisplayName("toPeriod() 转换为Period")
        void testToPeriod() {
            Period p = Years.of(3).toPeriod();
            assertThat(p).isEqualTo(Period.ofYears(3));
        }

        @Test
        @DisplayName("toMonths() 转换为Months")
        void testToMonths() {
            Months m = Years.of(2).toMonths();
            assertThat(m.getAmount()).isEqualTo(24);
        }
    }

    @Nested
    @DisplayName("TemporalAmount实现测试")
    class TemporalAmountTests {

        @Test
        @DisplayName("get() 获取单位值")
        void testGet() {
            Years y = Years.of(5);
            assertThat(y.get(ChronoUnit.YEARS)).isEqualTo(5);
        }

        @Test
        @DisplayName("get() 不支持单位抛出异常")
        void testGetUnsupported() {
            assertThatThrownBy(() -> Years.of(5).get(ChronoUnit.MONTHS))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getUnits() 获取支持的单位")
        void testGetUnits() {
            assertThat(Years.ZERO.getUnits()).containsExactly(ChronoUnit.YEARS);
        }

        @Test
        @DisplayName("addTo() 加到日期")
        void testAddTo() {
            LocalDate date = LocalDate.of(2024, 3, 15);
            LocalDate result = (LocalDate) Years.of(2).addTo(date);
            assertThat(result).isEqualTo(LocalDate.of(2026, 3, 15));
        }

        @Test
        @DisplayName("subtractFrom() 从日期减去")
        void testSubtractFrom() {
            LocalDate date = LocalDate.of(2024, 3, 15);
            LocalDate result = (LocalDate) Years.of(2).subtractFrom(date);
            assertThat(result).isEqualTo(LocalDate.of(2022, 3, 15));
        }
    }

    @Nested
    @DisplayName("Comparable实现测试")
    class ComparableTests {

        @Test
        @DisplayName("compareTo() 比较")
        void testCompareTo() {
            Years y1 = Years.of(3);
            Years y2 = Years.of(5);
            Years y3 = Years.of(3);

            assertThat(y1.compareTo(y2)).isLessThan(0);
            assertThat(y2.compareTo(y1)).isGreaterThan(0);
            assertThat(y1.compareTo(y3)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等对象")
        void testEquals() {
            Years y1 = Years.of(5);
            Years y2 = Years.of(5);
            Years y3 = Years.of(6);

            assertThat(y1).isEqualTo(y2);
            assertThat(y1).isNotEqualTo(y3);
            assertThat(y1).isEqualTo(y1);
            assertThat(y1).isNotEqualTo(null);
            assertThat(y1).isNotEqualTo("5");
        }

        @Test
        @DisplayName("hashCode() 相等对象相同哈希码")
        void testHashCode() {
            Years y1 = Years.of(5);
            Years y2 = Years.of(5);
            assertThat(y1.hashCode()).isEqualTo(y2.hashCode());
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            assertThat(Years.of(5).toString()).isEqualTo("P5Y");
        }
    }
}
