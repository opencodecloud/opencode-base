package cloud.opencode.base.date.extra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Hours 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("Hours 测试")
class HoursTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of() 创建Hours")
        void testOf() {
            Hours h = Hours.of(8);
            assertThat(h.getAmount()).isEqualTo(8);
        }

        @Test
        @DisplayName("of() 零值返回ZERO常量")
        void testOfZero() {
            assertThat(Hours.of(0)).isSameAs(Hours.ZERO);
        }

        @Test
        @DisplayName("ofMinutes() 从分钟创建")
        void testOfMinutes() {
            Hours h = Hours.ofMinutes(180);
            assertThat(h.getAmount()).isEqualTo(3);
        }

        @Test
        @DisplayName("ofDays() 从天数创建")
        void testOfDays() {
            Hours h = Hours.ofDays(2);
            assertThat(h.getAmount()).isEqualTo(48);
        }

        @Test
        @DisplayName("from() 从Duration创建")
        void testFrom() {
            Hours h = Hours.from(Duration.ofHours(5));
            assertThat(h.getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("from() null抛出异常")
        void testFromNull() {
            assertThatThrownBy(() -> Hours.from(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("between() 计算两个时间之间的小时数")
        void testBetween() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime end = LocalDateTime.of(2024, 1, 1, 18, 0);
            Hours h = Hours.between(start, end);
            assertThat(h.getAmount()).isEqualTo(8);
        }
    }

    @Nested
    @DisplayName("获取器测试")
    class GetterTests {

        @Test
        @DisplayName("isZero() 检查是否为零")
        void testIsZero() {
            assertThat(Hours.ZERO.isZero()).isTrue();
            assertThat(Hours.of(1).isZero()).isFalse();
        }

        @Test
        @DisplayName("isNegative() 检查是否为负")
        void testIsNegative() {
            assertThat(Hours.of(-1).isNegative()).isTrue();
            assertThat(Hours.of(0).isNegative()).isFalse();
            assertThat(Hours.of(1).isNegative()).isFalse();
        }

        @Test
        @DisplayName("isPositive() 检查是否为正")
        void testIsPositive() {
            assertThat(Hours.of(1).isPositive()).isTrue();
            assertThat(Hours.of(0).isPositive()).isFalse();
            assertThat(Hours.of(-1).isPositive()).isFalse();
        }
    }

    @Nested
    @DisplayName("计算方法测试")
    class CalculationTests {

        @Test
        @DisplayName("plus(Hours) 加小时数")
        void testPlusHours() {
            Hours result = Hours.of(10).plus(Hours.of(5));
            assertThat(result.getAmount()).isEqualTo(15);
        }

        @Test
        @DisplayName("plus(long) 加小时数值")
        void testPlusLong() {
            Hours result = Hours.of(10).plus(5);
            assertThat(result.getAmount()).isEqualTo(15);
        }

        @Test
        @DisplayName("minus(Hours) 减小时数")
        void testMinusHours() {
            Hours result = Hours.of(10).minus(Hours.of(3));
            assertThat(result.getAmount()).isEqualTo(7);
        }

        @Test
        @DisplayName("minus(long) 减小时数值")
        void testMinusLong() {
            Hours result = Hours.of(10).minus(3);
            assertThat(result.getAmount()).isEqualTo(7);
        }

        @Test
        @DisplayName("multipliedBy() 乘以标量")
        void testMultipliedBy() {
            Hours result = Hours.of(5).multipliedBy(3);
            assertThat(result.getAmount()).isEqualTo(15);
        }

        @Test
        @DisplayName("dividedBy() 除以标量")
        void testDividedBy() {
            Hours result = Hours.of(15).dividedBy(3);
            assertThat(result.getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("negated() 取负")
        void testNegated() {
            assertThat(Hours.of(5).negated().getAmount()).isEqualTo(-5);
            assertThat(Hours.of(-5).negated().getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("abs() 取绝对值")
        void testAbs() {
            assertThat(Hours.of(-5).abs().getAmount()).isEqualTo(5);
            assertThat(Hours.of(5).abs().getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("abs() 正数返回自身")
        void testAbsPositive() {
            Hours h = Hours.of(5);
            assertThat(h.abs()).isSameAs(h);
        }
    }

    @Nested
    @DisplayName("转换方法测试")
    class ConversionTests {

        @Test
        @DisplayName("toDuration() 转换为Duration")
        void testToDuration() {
            Duration d = Hours.of(3).toDuration();
            assertThat(d).isEqualTo(Duration.ofHours(3));
        }

        @Test
        @DisplayName("toSeconds() 转换为Seconds")
        void testToSeconds() {
            Seconds s = Hours.of(2).toSeconds();
            assertThat(s.getAmount()).isEqualTo(7200);
        }

        @Test
        @DisplayName("toMinutes() 转换为Minutes")
        void testToMinutes() {
            Minutes m = Hours.of(2).toMinutes();
            assertThat(m.getAmount()).isEqualTo(120);
        }

        @Test
        @DisplayName("toDays() 转换为Days")
        void testToDays() {
            Days d = Hours.of(48).toDays();
            assertThat(d.getAmount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("TemporalAmount实现测试")
    class TemporalAmountTests {

        @Test
        @DisplayName("get() 获取单位值")
        void testGet() {
            Hours h = Hours.of(10);
            assertThat(h.get(ChronoUnit.HOURS)).isEqualTo(10);
        }

        @Test
        @DisplayName("get() 不支持单位抛出异常")
        void testGetUnsupported() {
            assertThatThrownBy(() -> Hours.of(10).get(ChronoUnit.MINUTES))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getUnits() 获取支持的单位")
        void testGetUnits() {
            assertThat(Hours.ZERO.getUnits()).containsExactly(ChronoUnit.HOURS);
        }

        @Test
        @DisplayName("addTo() 加到时间")
        void testAddTo() {
            LocalDateTime dt = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime result = (LocalDateTime) Hours.of(2).addTo(dt);
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 1, 12, 0));
        }

        @Test
        @DisplayName("subtractFrom() 从时间减去")
        void testSubtractFrom() {
            LocalDateTime dt = LocalDateTime.of(2024, 1, 1, 12, 0);
            LocalDateTime result = (LocalDateTime) Hours.of(2).subtractFrom(dt);
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        }
    }

    @Nested
    @DisplayName("Comparable实现测试")
    class ComparableTests {

        @Test
        @DisplayName("compareTo() 比较")
        void testCompareTo() {
            Hours h1 = Hours.of(10);
            Hours h2 = Hours.of(20);
            Hours h3 = Hours.of(10);

            assertThat(h1.compareTo(h2)).isLessThan(0);
            assertThat(h2.compareTo(h1)).isGreaterThan(0);
            assertThat(h1.compareTo(h3)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等对象")
        void testEquals() {
            Hours h1 = Hours.of(8);
            Hours h2 = Hours.of(8);
            Hours h3 = Hours.of(9);

            assertThat(h1).isEqualTo(h2);
            assertThat(h1).isNotEqualTo(h3);
            assertThat(h1).isEqualTo(h1);
            assertThat(h1).isNotEqualTo(null);
            assertThat(h1).isNotEqualTo("8");
        }

        @Test
        @DisplayName("hashCode() 相等对象相同哈希码")
        void testHashCode() {
            Hours h1 = Hours.of(8);
            Hours h2 = Hours.of(8);
            assertThat(h1.hashCode()).isEqualTo(h2.hashCode());
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            assertThat(Hours.of(8).toString()).isEqualTo("PT8H");
        }
    }
}
