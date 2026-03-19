package cloud.opencode.base.date.extra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Days 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("Days 测试")
class DaysTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of(int) 创建Days")
        void testOfInt() {
            Days d = Days.of(7);
            assertThat(d.getAmount()).isEqualTo(7);
        }

        @Test
        @DisplayName("of(long) 创建Days")
        void testOfLong() {
            Days d = Days.of(7L);
            assertThat(d.getAmount()).isEqualTo(7);
        }

        @Test
        @DisplayName("of() 零值返回ZERO常量")
        void testOfZero() {
            assertThat(Days.of(0)).isSameAs(Days.ZERO);
        }

        @Test
        @DisplayName("of() 1返回ONE常量")
        void testOfOne() {
            assertThat(Days.of(1)).isSameAs(Days.ONE);
        }

        @Test
        @DisplayName("ofWeeks() 从周数创建")
        void testOfWeeks() {
            Days d = Days.ofWeeks(2);
            assertThat(d.getAmount()).isEqualTo(14);
        }

        @Test
        @DisplayName("from() 从Period创建")
        void testFrom() {
            Days d = Days.from(Period.ofDays(10));
            assertThat(d.getAmount()).isEqualTo(10);
        }

        @Test
        @DisplayName("from() null抛出异常")
        void testFromNull() {
            assertThatThrownBy(() -> Days.from(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("between() 计算两个日期之间的天数")
        void testBetween() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 1, 15);
            Days d = Days.between(start, end);
            assertThat(d.getAmount()).isEqualTo(14);
        }
    }

    @Nested
    @DisplayName("获取器测试")
    class GetterTests {

        @Test
        @DisplayName("isZero() 检查是否为零")
        void testIsZero() {
            assertThat(Days.ZERO.isZero()).isTrue();
            assertThat(Days.of(1).isZero()).isFalse();
        }

        @Test
        @DisplayName("isNegative() 检查是否为负")
        void testIsNegative() {
            assertThat(Days.of(-1).isNegative()).isTrue();
            assertThat(Days.of(0).isNegative()).isFalse();
            assertThat(Days.of(1).isNegative()).isFalse();
        }

        @Test
        @DisplayName("isPositive() 检查是否为正")
        void testIsPositive() {
            assertThat(Days.of(1).isPositive()).isTrue();
            assertThat(Days.of(0).isPositive()).isFalse();
            assertThat(Days.of(-1).isPositive()).isFalse();
        }
    }

    @Nested
    @DisplayName("计算方法测试")
    class CalculationTests {

        @Test
        @DisplayName("plus(Days) 加天数")
        void testPlusDays() {
            Days result = Days.of(10).plus(Days.of(5));
            assertThat(result.getAmount()).isEqualTo(15);
        }

        @Test
        @DisplayName("plus(int) 加天数值")
        void testPlusInt() {
            Days result = Days.of(10).plus(5);
            assertThat(result.getAmount()).isEqualTo(15);
        }

        @Test
        @DisplayName("minus(Days) 减天数")
        void testMinusDays() {
            Days result = Days.of(10).minus(Days.of(3));
            assertThat(result.getAmount()).isEqualTo(7);
        }

        @Test
        @DisplayName("minus(int) 减天数值")
        void testMinusInt() {
            Days result = Days.of(10).minus(3);
            assertThat(result.getAmount()).isEqualTo(7);
        }

        @Test
        @DisplayName("multipliedBy() 乘以标量")
        void testMultipliedBy() {
            Days result = Days.of(5).multipliedBy(3);
            assertThat(result.getAmount()).isEqualTo(15);
        }

        @Test
        @DisplayName("dividedBy() 除以标量")
        void testDividedBy() {
            Days result = Days.of(15).dividedBy(3);
            assertThat(result.getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("negated() 取负")
        void testNegated() {
            assertThat(Days.of(5).negated().getAmount()).isEqualTo(-5);
            assertThat(Days.of(-5).negated().getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("abs() 取绝对值")
        void testAbs() {
            assertThat(Days.of(-5).abs().getAmount()).isEqualTo(5);
            assertThat(Days.of(5).abs().getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("abs() 正数返回自身")
        void testAbsPositive() {
            Days d = Days.of(5);
            assertThat(d.abs()).isSameAs(d);
        }
    }

    @Nested
    @DisplayName("转换方法测试")
    class ConversionTests {

        @Test
        @DisplayName("toPeriod() 转换为Period")
        void testToPeriod() {
            Period p = Days.of(10).toPeriod();
            assertThat(p).isEqualTo(Period.ofDays(10));
        }

        @Test
        @DisplayName("toDuration() 转换为Duration")
        void testToDuration() {
            Duration d = Days.of(2).toDuration();
            assertThat(d).isEqualTo(Duration.ofDays(2));
        }

        @Test
        @DisplayName("toHours() 转换为Hours")
        void testToHours() {
            Hours h = Days.of(2).toHours();
            assertThat(h.getAmount()).isEqualTo(48);
        }

        @Test
        @DisplayName("toWeeks() 转换为Weeks")
        void testToWeeks() {
            Weeks w = Days.of(14).toWeeks();
            assertThat(w.getAmount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("TemporalAmount实现测试")
    class TemporalAmountTests {

        @Test
        @DisplayName("get() 获取单位值")
        void testGet() {
            Days d = Days.of(10);
            assertThat(d.get(ChronoUnit.DAYS)).isEqualTo(10);
        }

        @Test
        @DisplayName("get() 不支持单位抛出异常")
        void testGetUnsupported() {
            assertThatThrownBy(() -> Days.of(10).get(ChronoUnit.HOURS))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getUnits() 获取支持的单位")
        void testGetUnits() {
            assertThat(Days.ZERO.getUnits()).containsExactly(ChronoUnit.DAYS);
        }

        @Test
        @DisplayName("addTo() 加到日期")
        void testAddTo() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            LocalDate result = (LocalDate) Days.of(10).addTo(date);
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 11));
        }

        @Test
        @DisplayName("subtractFrom() 从日期减去")
        void testSubtractFrom() {
            LocalDate date = LocalDate.of(2024, 1, 11);
            LocalDate result = (LocalDate) Days.of(10).subtractFrom(date);
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 1));
        }
    }

    @Nested
    @DisplayName("Comparable实现测试")
    class ComparableTests {

        @Test
        @DisplayName("compareTo() 比较")
        void testCompareTo() {
            Days d1 = Days.of(10);
            Days d2 = Days.of(20);
            Days d3 = Days.of(10);

            assertThat(d1.compareTo(d2)).isLessThan(0);
            assertThat(d2.compareTo(d1)).isGreaterThan(0);
            assertThat(d1.compareTo(d3)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等对象")
        void testEquals() {
            Days d1 = Days.of(7);
            Days d2 = Days.of(7);
            Days d3 = Days.of(8);

            assertThat(d1).isEqualTo(d2);
            assertThat(d1).isNotEqualTo(d3);
            assertThat(d1).isEqualTo(d1);
            assertThat(d1).isNotEqualTo(null);
            assertThat(d1).isNotEqualTo("7");
        }

        @Test
        @DisplayName("hashCode() 相等对象相同哈希码")
        void testHashCode() {
            Days d1 = Days.of(7);
            Days d2 = Days.of(7);
            assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            assertThat(Days.of(7).toString()).isEqualTo("P7D");
        }
    }
}
