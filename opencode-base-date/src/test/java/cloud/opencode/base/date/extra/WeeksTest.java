package cloud.opencode.base.date.extra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Weeks 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("Weeks 测试")
class WeeksTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of(int) 创建Weeks")
        void testOfInt() {
            Weeks w = Weeks.of(4);
            assertThat(w.getAmount()).isEqualTo(4);
        }

        @Test
        @DisplayName("of(long) 创建Weeks")
        void testOfLong() {
            Weeks w = Weeks.of(4L);
            assertThat(w.getAmount()).isEqualTo(4);
        }

        @Test
        @DisplayName("of() 零值返回ZERO常量")
        void testOfZero() {
            assertThat(Weeks.of(0)).isSameAs(Weeks.ZERO);
        }

        @Test
        @DisplayName("of() 1返回ONE常量")
        void testOfOne() {
            assertThat(Weeks.of(1)).isSameAs(Weeks.ONE);
        }

        @Test
        @DisplayName("ofDays() 从天数创建")
        void testOfDays() {
            Weeks w = Weeks.ofDays(21);
            assertThat(w.getAmount()).isEqualTo(3);
        }

        @Test
        @DisplayName("between() 计算两个日期之间的周数")
        void testBetween() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 1, 22);
            Weeks w = Weeks.between(start, end);
            assertThat(w.getAmount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("获取器测试")
    class GetterTests {

        @Test
        @DisplayName("isZero() 检查是否为零")
        void testIsZero() {
            assertThat(Weeks.ZERO.isZero()).isTrue();
            assertThat(Weeks.of(1).isZero()).isFalse();
        }

        @Test
        @DisplayName("isNegative() 检查是否为负")
        void testIsNegative() {
            assertThat(Weeks.of(-1).isNegative()).isTrue();
            assertThat(Weeks.of(0).isNegative()).isFalse();
            assertThat(Weeks.of(1).isNegative()).isFalse();
        }

        @Test
        @DisplayName("isPositive() 检查是否为正")
        void testIsPositive() {
            assertThat(Weeks.of(1).isPositive()).isTrue();
            assertThat(Weeks.of(0).isPositive()).isFalse();
            assertThat(Weeks.of(-1).isPositive()).isFalse();
        }
    }

    @Nested
    @DisplayName("计算方法测试")
    class CalculationTests {

        @Test
        @DisplayName("plus(Weeks) 加周数")
        void testPlusWeeks() {
            Weeks result = Weeks.of(2).plus(Weeks.of(3));
            assertThat(result.getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("plus(int) 加周数值")
        void testPlusInt() {
            Weeks result = Weeks.of(2).plus(3);
            assertThat(result.getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("minus(Weeks) 减周数")
        void testMinusWeeks() {
            Weeks result = Weeks.of(5).minus(Weeks.of(2));
            assertThat(result.getAmount()).isEqualTo(3);
        }

        @Test
        @DisplayName("minus(int) 减周数值")
        void testMinusInt() {
            Weeks result = Weeks.of(5).minus(2);
            assertThat(result.getAmount()).isEqualTo(3);
        }

        @Test
        @DisplayName("multipliedBy() 乘以标量")
        void testMultipliedBy() {
            Weeks result = Weeks.of(3).multipliedBy(2);
            assertThat(result.getAmount()).isEqualTo(6);
        }

        @Test
        @DisplayName("dividedBy() 除以标量")
        void testDividedBy() {
            Weeks result = Weeks.of(6).dividedBy(2);
            assertThat(result.getAmount()).isEqualTo(3);
        }

        @Test
        @DisplayName("negated() 取负")
        void testNegated() {
            assertThat(Weeks.of(3).negated().getAmount()).isEqualTo(-3);
            assertThat(Weeks.of(-3).negated().getAmount()).isEqualTo(3);
        }

        @Test
        @DisplayName("abs() 取绝对值")
        void testAbs() {
            assertThat(Weeks.of(-3).abs().getAmount()).isEqualTo(3);
            assertThat(Weeks.of(3).abs().getAmount()).isEqualTo(3);
        }

        @Test
        @DisplayName("abs() 正数返回自身")
        void testAbsPositive() {
            Weeks w = Weeks.of(3);
            assertThat(w.abs()).isSameAs(w);
        }
    }

    @Nested
    @DisplayName("转换方法测试")
    class ConversionTests {

        @Test
        @DisplayName("toPeriod() 转换为Period")
        void testToPeriod() {
            Period p = Weeks.of(2).toPeriod();
            assertThat(p).isEqualTo(Period.ofWeeks(2));
        }

        @Test
        @DisplayName("toDays() 转换为Days")
        void testToDays() {
            Days d = Weeks.of(2).toDays();
            assertThat(d.getAmount()).isEqualTo(14);
        }
    }

    @Nested
    @DisplayName("TemporalAmount实现测试")
    class TemporalAmountTests {

        @Test
        @DisplayName("get() 获取单位值")
        void testGet() {
            Weeks w = Weeks.of(4);
            assertThat(w.get(ChronoUnit.WEEKS)).isEqualTo(4);
        }

        @Test
        @DisplayName("get() 不支持单位抛出异常")
        void testGetUnsupported() {
            assertThatThrownBy(() -> Weeks.of(4).get(ChronoUnit.DAYS))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getUnits() 获取支持的单位")
        void testGetUnits() {
            assertThat(Weeks.ZERO.getUnits()).containsExactly(ChronoUnit.WEEKS);
        }

        @Test
        @DisplayName("addTo() 加到日期")
        void testAddTo() {
            LocalDate date = LocalDate.of(2024, 1, 1);
            LocalDate result = (LocalDate) Weeks.of(2).addTo(date);
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("subtractFrom() 从日期减去")
        void testSubtractFrom() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            LocalDate result = (LocalDate) Weeks.of(2).subtractFrom(date);
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 1));
        }
    }

    @Nested
    @DisplayName("Comparable实现测试")
    class ComparableTests {

        @Test
        @DisplayName("compareTo() 比较")
        void testCompareTo() {
            Weeks w1 = Weeks.of(2);
            Weeks w2 = Weeks.of(4);
            Weeks w3 = Weeks.of(2);

            assertThat(w1.compareTo(w2)).isLessThan(0);
            assertThat(w2.compareTo(w1)).isGreaterThan(0);
            assertThat(w1.compareTo(w3)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等对象")
        void testEquals() {
            Weeks w1 = Weeks.of(4);
            Weeks w2 = Weeks.of(4);
            Weeks w3 = Weeks.of(5);

            assertThat(w1).isEqualTo(w2);
            assertThat(w1).isNotEqualTo(w3);
            assertThat(w1).isEqualTo(w1);
            assertThat(w1).isNotEqualTo(null);
            assertThat(w1).isNotEqualTo("4");
        }

        @Test
        @DisplayName("hashCode() 相等对象相同哈希码")
        void testHashCode() {
            Weeks w1 = Weeks.of(4);
            Weeks w2 = Weeks.of(4);
            assertThat(w1.hashCode()).isEqualTo(w2.hashCode());
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            assertThat(Weeks.of(4).toString()).isEqualTo("P4W");
        }
    }
}
