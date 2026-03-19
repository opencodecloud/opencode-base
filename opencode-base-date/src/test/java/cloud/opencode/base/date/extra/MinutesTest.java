package cloud.opencode.base.date.extra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Minutes 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("Minutes 测试")
class MinutesTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of() 创建Minutes")
        void testOf() {
            Minutes m = Minutes.of(30);
            assertThat(m.getAmount()).isEqualTo(30);
        }

        @Test
        @DisplayName("of() 零值返回ZERO常量")
        void testOfZero() {
            assertThat(Minutes.of(0)).isSameAs(Minutes.ZERO);
        }

        @Test
        @DisplayName("ofSeconds() 从秒数创建")
        void testOfSeconds() {
            Minutes m = Minutes.ofSeconds(180);
            assertThat(m.getAmount()).isEqualTo(3);
        }

        @Test
        @DisplayName("ofHours() 从小时创建")
        void testOfHours() {
            Minutes m = Minutes.ofHours(2);
            assertThat(m.getAmount()).isEqualTo(120);
        }

        @Test
        @DisplayName("ofDays() 从天数创建")
        void testOfDays() {
            Minutes m = Minutes.ofDays(1);
            assertThat(m.getAmount()).isEqualTo(1440);
        }

        @Test
        @DisplayName("from() 从Duration创建")
        void testFrom() {
            Minutes m = Minutes.from(Duration.ofMinutes(45));
            assertThat(m.getAmount()).isEqualTo(45);
        }

        @Test
        @DisplayName("from() null抛出异常")
        void testFromNull() {
            assertThatThrownBy(() -> Minutes.from(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("between() 计算两个时间之间的分钟数")
        void testBetween() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime end = LocalDateTime.of(2024, 1, 1, 10, 30);
            Minutes m = Minutes.between(start, end);
            assertThat(m.getAmount()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("获取器测试")
    class GetterTests {

        @Test
        @DisplayName("isZero() 检查是否为零")
        void testIsZero() {
            assertThat(Minutes.ZERO.isZero()).isTrue();
            assertThat(Minutes.of(1).isZero()).isFalse();
        }

        @Test
        @DisplayName("isNegative() 检查是否为负")
        void testIsNegative() {
            assertThat(Minutes.of(-1).isNegative()).isTrue();
            assertThat(Minutes.of(0).isNegative()).isFalse();
            assertThat(Minutes.of(1).isNegative()).isFalse();
        }

        @Test
        @DisplayName("isPositive() 检查是否为正")
        void testIsPositive() {
            assertThat(Minutes.of(1).isPositive()).isTrue();
            assertThat(Minutes.of(0).isPositive()).isFalse();
            assertThat(Minutes.of(-1).isPositive()).isFalse();
        }
    }

    @Nested
    @DisplayName("计算方法测试")
    class CalculationTests {

        @Test
        @DisplayName("plus(Minutes) 加分钟数")
        void testPlusMinutes() {
            Minutes result = Minutes.of(10).plus(Minutes.of(5));
            assertThat(result.getAmount()).isEqualTo(15);
        }

        @Test
        @DisplayName("plus(long) 加分钟数值")
        void testPlusLong() {
            Minutes result = Minutes.of(10).plus(5);
            assertThat(result.getAmount()).isEqualTo(15);
        }

        @Test
        @DisplayName("minus(Minutes) 减分钟数")
        void testMinusMinutes() {
            Minutes result = Minutes.of(10).minus(Minutes.of(3));
            assertThat(result.getAmount()).isEqualTo(7);
        }

        @Test
        @DisplayName("minus(long) 减分钟数值")
        void testMinusLong() {
            Minutes result = Minutes.of(10).minus(3);
            assertThat(result.getAmount()).isEqualTo(7);
        }

        @Test
        @DisplayName("multipliedBy() 乘以标量")
        void testMultipliedBy() {
            Minutes result = Minutes.of(5).multipliedBy(3);
            assertThat(result.getAmount()).isEqualTo(15);
        }

        @Test
        @DisplayName("dividedBy() 除以标量")
        void testDividedBy() {
            Minutes result = Minutes.of(15).dividedBy(3);
            assertThat(result.getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("negated() 取负")
        void testNegated() {
            assertThat(Minutes.of(5).negated().getAmount()).isEqualTo(-5);
            assertThat(Minutes.of(-5).negated().getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("abs() 取绝对值")
        void testAbs() {
            assertThat(Minutes.of(-5).abs().getAmount()).isEqualTo(5);
            assertThat(Minutes.of(5).abs().getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("abs() 正数返回自身")
        void testAbsPositive() {
            Minutes m = Minutes.of(5);
            assertThat(m.abs()).isSameAs(m);
        }
    }

    @Nested
    @DisplayName("转换方法测试")
    class ConversionTests {

        @Test
        @DisplayName("toDuration() 转换为Duration")
        void testToDuration() {
            Duration d = Minutes.of(30).toDuration();
            assertThat(d).isEqualTo(Duration.ofMinutes(30));
        }

        @Test
        @DisplayName("toSeconds() 转换为Seconds")
        void testToSeconds() {
            Seconds s = Minutes.of(3).toSeconds();
            assertThat(s.getAmount()).isEqualTo(180);
        }

        @Test
        @DisplayName("toHours() 转换为Hours")
        void testToHours() {
            Hours h = Minutes.of(120).toHours();
            assertThat(h.getAmount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("TemporalAmount实现测试")
    class TemporalAmountTests {

        @Test
        @DisplayName("get() 获取单位值")
        void testGet() {
            Minutes m = Minutes.of(100);
            assertThat(m.get(ChronoUnit.MINUTES)).isEqualTo(100);
        }

        @Test
        @DisplayName("get() 不支持单位抛出异常")
        void testGetUnsupported() {
            assertThatThrownBy(() -> Minutes.of(100).get(ChronoUnit.SECONDS))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getUnits() 获取支持的单位")
        void testGetUnits() {
            assertThat(Minutes.ZERO.getUnits()).containsExactly(ChronoUnit.MINUTES);
        }

        @Test
        @DisplayName("addTo() 加到时间")
        void testAddTo() {
            LocalDateTime dt = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime result = (LocalDateTime) Minutes.of(30).addTo(dt);
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 30));
        }

        @Test
        @DisplayName("subtractFrom() 从时间减去")
        void testSubtractFrom() {
            LocalDateTime dt = LocalDateTime.of(2024, 1, 1, 10, 30);
            LocalDateTime result = (LocalDateTime) Minutes.of(30).subtractFrom(dt);
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        }
    }

    @Nested
    @DisplayName("Comparable实现测试")
    class ComparableTests {

        @Test
        @DisplayName("compareTo() 比较")
        void testCompareTo() {
            Minutes m1 = Minutes.of(10);
            Minutes m2 = Minutes.of(20);
            Minutes m3 = Minutes.of(10);

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
            Minutes m1 = Minutes.of(30);
            Minutes m2 = Minutes.of(30);
            Minutes m3 = Minutes.of(31);

            assertThat(m1).isEqualTo(m2);
            assertThat(m1).isNotEqualTo(m3);
            assertThat(m1).isEqualTo(m1);
            assertThat(m1).isNotEqualTo(null);
            assertThat(m1).isNotEqualTo("30");
        }

        @Test
        @DisplayName("hashCode() 相等对象相同哈希码")
        void testHashCode() {
            Minutes m1 = Minutes.of(30);
            Minutes m2 = Minutes.of(30);
            assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            assertThat(Minutes.of(30).toString()).isEqualTo("PT30M");
        }
    }
}
