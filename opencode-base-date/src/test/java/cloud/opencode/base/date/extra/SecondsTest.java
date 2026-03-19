package cloud.opencode.base.date.extra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Seconds 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("Seconds 测试")
class SecondsTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of() 创建Seconds")
        void testOf() {
            Seconds s = Seconds.of(30);
            assertThat(s.getAmount()).isEqualTo(30);
        }

        @Test
        @DisplayName("of() 零值返回ZERO常量")
        void testOfZero() {
            assertThat(Seconds.of(0)).isSameAs(Seconds.ZERO);
        }

        @Test
        @DisplayName("ofMinutes() 从分钟创建")
        void testOfMinutes() {
            Seconds s = Seconds.ofMinutes(2);
            assertThat(s.getAmount()).isEqualTo(120);
        }

        @Test
        @DisplayName("ofHours() 从小时创建")
        void testOfHours() {
            Seconds s = Seconds.ofHours(1);
            assertThat(s.getAmount()).isEqualTo(3600);
        }

        @Test
        @DisplayName("ofDays() 从天数创建")
        void testOfDays() {
            Seconds s = Seconds.ofDays(1);
            assertThat(s.getAmount()).isEqualTo(86400);
        }

        @Test
        @DisplayName("from() 从Duration创建")
        void testFrom() {
            Seconds s = Seconds.from(Duration.ofSeconds(45));
            assertThat(s.getAmount()).isEqualTo(45);
        }

        @Test
        @DisplayName("from() null抛出异常")
        void testFromNull() {
            assertThatThrownBy(() -> Seconds.from(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("between() 计算两个时间之间的秒数")
        void testBetween() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
            LocalDateTime end = LocalDateTime.of(2024, 1, 1, 10, 0, 30);
            Seconds s = Seconds.between(start, end);
            assertThat(s.getAmount()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("获取器测试")
    class GetterTests {

        @Test
        @DisplayName("isZero() 检查是否为零")
        void testIsZero() {
            assertThat(Seconds.ZERO.isZero()).isTrue();
            assertThat(Seconds.of(1).isZero()).isFalse();
        }

        @Test
        @DisplayName("isNegative() 检查是否为负")
        void testIsNegative() {
            assertThat(Seconds.of(-1).isNegative()).isTrue();
            assertThat(Seconds.of(0).isNegative()).isFalse();
            assertThat(Seconds.of(1).isNegative()).isFalse();
        }

        @Test
        @DisplayName("isPositive() 检查是否为正")
        void testIsPositive() {
            assertThat(Seconds.of(1).isPositive()).isTrue();
            assertThat(Seconds.of(0).isPositive()).isFalse();
            assertThat(Seconds.of(-1).isPositive()).isFalse();
        }
    }

    @Nested
    @DisplayName("计算方法测试")
    class CalculationTests {

        @Test
        @DisplayName("plus(Seconds) 加秒数")
        void testPlusSeconds() {
            Seconds result = Seconds.of(10).plus(Seconds.of(5));
            assertThat(result.getAmount()).isEqualTo(15);
        }

        @Test
        @DisplayName("plus(long) 加秒数值")
        void testPlusLong() {
            Seconds result = Seconds.of(10).plus(5);
            assertThat(result.getAmount()).isEqualTo(15);
        }

        @Test
        @DisplayName("minus(Seconds) 减秒数")
        void testMinusSeconds() {
            Seconds result = Seconds.of(10).minus(Seconds.of(3));
            assertThat(result.getAmount()).isEqualTo(7);
        }

        @Test
        @DisplayName("minus(long) 减秒数值")
        void testMinusLong() {
            Seconds result = Seconds.of(10).minus(3);
            assertThat(result.getAmount()).isEqualTo(7);
        }

        @Test
        @DisplayName("multipliedBy() 乘以标量")
        void testMultipliedBy() {
            Seconds result = Seconds.of(5).multipliedBy(3);
            assertThat(result.getAmount()).isEqualTo(15);
        }

        @Test
        @DisplayName("dividedBy() 除以标量")
        void testDividedBy() {
            Seconds result = Seconds.of(15).dividedBy(3);
            assertThat(result.getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("negated() 取负")
        void testNegated() {
            assertThat(Seconds.of(5).negated().getAmount()).isEqualTo(-5);
            assertThat(Seconds.of(-5).negated().getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("abs() 取绝对值")
        void testAbs() {
            assertThat(Seconds.of(-5).abs().getAmount()).isEqualTo(5);
            assertThat(Seconds.of(5).abs().getAmount()).isEqualTo(5);
        }

        @Test
        @DisplayName("abs() 正数返回自身")
        void testAbsPositive() {
            Seconds s = Seconds.of(5);
            assertThat(s.abs()).isSameAs(s);
        }
    }

    @Nested
    @DisplayName("转换方法测试")
    class ConversionTests {

        @Test
        @DisplayName("toDuration() 转换为Duration")
        void testToDuration() {
            Duration d = Seconds.of(120).toDuration();
            assertThat(d).isEqualTo(Duration.ofSeconds(120));
        }

        @Test
        @DisplayName("toMinutes() 转换为Minutes")
        void testToMinutes() {
            Minutes m = Seconds.of(180).toMinutes();
            assertThat(m.getAmount()).isEqualTo(3);
        }

        @Test
        @DisplayName("toHours() 转换为Hours")
        void testToHours() {
            Hours h = Seconds.of(7200).toHours();
            assertThat(h.getAmount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("TemporalAmount实现测试")
    class TemporalAmountTests {

        @Test
        @DisplayName("get() 获取单位值")
        void testGet() {
            Seconds s = Seconds.of(100);
            assertThat(s.get(ChronoUnit.SECONDS)).isEqualTo(100);
        }

        @Test
        @DisplayName("get() 不支持单位抛出异常")
        void testGetUnsupported() {
            assertThatThrownBy(() -> Seconds.of(100).get(ChronoUnit.MINUTES))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getUnits() 获取支持的单位")
        void testGetUnits() {
            assertThat(Seconds.ZERO.getUnits()).containsExactly(ChronoUnit.SECONDS);
        }

        @Test
        @DisplayName("addTo() 加到时间")
        void testAddTo() {
            LocalDateTime dt = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
            LocalDateTime result = (LocalDateTime) Seconds.of(30).addTo(dt);
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0, 30));
        }

        @Test
        @DisplayName("subtractFrom() 从时间减去")
        void testSubtractFrom() {
            LocalDateTime dt = LocalDateTime.of(2024, 1, 1, 10, 0, 30);
            LocalDateTime result = (LocalDateTime) Seconds.of(30).subtractFrom(dt);
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0, 0));
        }
    }

    @Nested
    @DisplayName("Comparable实现测试")
    class ComparableTests {

        @Test
        @DisplayName("compareTo() 比较")
        void testCompareTo() {
            Seconds s1 = Seconds.of(10);
            Seconds s2 = Seconds.of(20);
            Seconds s3 = Seconds.of(10);

            assertThat(s1.compareTo(s2)).isLessThan(0);
            assertThat(s2.compareTo(s1)).isGreaterThan(0);
            assertThat(s1.compareTo(s3)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等对象")
        void testEquals() {
            Seconds s1 = Seconds.of(30);
            Seconds s2 = Seconds.of(30);
            Seconds s3 = Seconds.of(31);

            assertThat(s1).isEqualTo(s2);
            assertThat(s1).isNotEqualTo(s3);
            assertThat(s1).isEqualTo(s1);
            assertThat(s1).isNotEqualTo(null);
            assertThat(s1).isNotEqualTo("30");
        }

        @Test
        @DisplayName("hashCode() 相等对象相同哈希码")
        void testHashCode() {
            Seconds s1 = Seconds.of(30);
            Seconds s2 = Seconds.of(30);
            assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            assertThat(Seconds.of(30).toString()).isEqualTo("PT30S");
        }
    }
}
