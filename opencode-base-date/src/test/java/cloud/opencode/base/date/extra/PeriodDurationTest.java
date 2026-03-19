package cloud.opencode.base.date.extra;

import cloud.opencode.base.date.exception.OpenDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * PeriodDuration 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("PeriodDuration 测试")
class PeriodDurationTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of() 创建PeriodDuration")
        void testOf() {
            PeriodDuration pd = PeriodDuration.of(Period.ofMonths(2), Duration.ofHours(5));
            assertThat(pd.getPeriod()).isEqualTo(Period.ofMonths(2));
            assertThat(pd.getDuration()).isEqualTo(Duration.ofHours(5));
        }

        @Test
        @DisplayName("of() 零值返回ZERO常量")
        void testOfZero() {
            PeriodDuration pd = PeriodDuration.of(Period.ZERO, Duration.ZERO);
            assertThat(pd).isSameAs(PeriodDuration.ZERO);
        }

        @Test
        @DisplayName("of() null抛出异常")
        void testOfNull() {
            assertThatThrownBy(() -> PeriodDuration.of(null, Duration.ZERO))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> PeriodDuration.of(Period.ZERO, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("ofPeriod() 仅从Period创建")
        void testOfPeriod() {
            PeriodDuration pd = PeriodDuration.ofPeriod(Period.ofMonths(3));
            assertThat(pd.getPeriod()).isEqualTo(Period.ofMonths(3));
            assertThat(pd.getDuration()).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("ofDuration() 仅从Duration创建")
        void testOfDuration() {
            PeriodDuration pd = PeriodDuration.ofDuration(Duration.ofHours(5));
            assertThat(pd.getPeriod()).isEqualTo(Period.ZERO);
            assertThat(pd.getDuration()).isEqualTo(Duration.ofHours(5));
        }

        @Test
        @DisplayName("parse() 解析Period格式")
        void testParsePeriod() {
            PeriodDuration pd = PeriodDuration.parse("P1Y2M3D");
            assertThat(pd.getPeriod().getYears()).isEqualTo(1);
            assertThat(pd.getPeriod().getMonths()).isEqualTo(2);
            assertThat(pd.getPeriod().getDays()).isEqualTo(3);
        }

        @Test
        @DisplayName("parse() 解析Duration格式")
        void testParseDuration() {
            PeriodDuration pd = PeriodDuration.parse("PT4H5M6S");
            assertThat(pd.getDuration()).isEqualTo(Duration.parse("PT4H5M6S"));
        }

        @Test
        @DisplayName("parse() 解析完整格式")
        void testParseFull() {
            PeriodDuration pd = PeriodDuration.parse("P1Y2M3DT4H5M6S");
            assertThat(pd.getPeriod().getYears()).isEqualTo(1);
            assertThat(pd.getPeriod().getMonths()).isEqualTo(2);
            assertThat(pd.getPeriod().getDays()).isEqualTo(3);
            assertThat(pd.getDuration()).isEqualTo(Duration.parse("PT4H5M6S"));
        }

        @Test
        @DisplayName("parse() 解析负值")
        void testParseNegative() {
            PeriodDuration pd = PeriodDuration.parse("-P1M");
            assertThat(pd.isNegative()).isTrue();
        }

        @Test
        @DisplayName("parse() 无效格式抛出异常")
        void testParseInvalid() {
            assertThatThrownBy(() -> PeriodDuration.parse("invalid"))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("between() 计算两个日期时间之间的差")
        void testBetween() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 9, 0);
            LocalDateTime end = LocalDateTime.of(2024, 3, 15, 14, 30);

            PeriodDuration pd = PeriodDuration.between(start, end);
            assertThat(pd.getPeriod().getMonths()).isEqualTo(2);
            assertThat(pd.getPeriod().getDays()).isEqualTo(14);
            assertThat(pd.getDuration()).isEqualTo(Duration.parse("PT5H30M"));
        }

        @Test
        @DisplayName("between() 跨日时间调整")
        void testBetweenCrossDay() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 22, 0);
            LocalDateTime end = LocalDateTime.of(2024, 1, 2, 10, 0);

            PeriodDuration pd = PeriodDuration.between(start, end);
            assertThat(pd.getPeriod().getDays()).isEqualTo(0);
            assertThat(pd.getDuration()).isEqualTo(Duration.ofHours(12));
        }
    }

    @Nested
    @DisplayName("获取器测试")
    class GetterTests {

        @Test
        @DisplayName("isZero() 检查是否为零")
        void testIsZero() {
            assertThat(PeriodDuration.ZERO.isZero()).isTrue();
            assertThat(PeriodDuration.of(Period.ofDays(1), Duration.ZERO).isZero()).isFalse();
            assertThat(PeriodDuration.of(Period.ZERO, Duration.ofHours(1)).isZero()).isFalse();
        }

        @Test
        @DisplayName("isNegative() 检查是否为负")
        void testIsNegative() {
            assertThat(PeriodDuration.of(Period.ofDays(-1), Duration.ZERO).isNegative()).isTrue();
            assertThat(PeriodDuration.of(Period.ZERO, Duration.ofHours(-1)).isNegative()).isTrue();
            assertThat(PeriodDuration.of(Period.ofDays(1), Duration.ofHours(1)).isNegative()).isFalse();
        }
    }

    @Nested
    @DisplayName("计算方法测试")
    class CalculationTests {

        @Test
        @DisplayName("plus() 加另一PeriodDuration")
        void testPlus() {
            PeriodDuration pd1 = PeriodDuration.of(Period.ofMonths(1), Duration.ofHours(2));
            PeriodDuration pd2 = PeriodDuration.of(Period.ofMonths(2), Duration.ofHours(3));

            PeriodDuration result = pd1.plus(pd2);
            assertThat(result.getPeriod().getMonths()).isEqualTo(3);
            assertThat(result.getDuration()).isEqualTo(Duration.ofHours(5));
        }

        @Test
        @DisplayName("minus() 减另一PeriodDuration")
        void testMinus() {
            PeriodDuration pd1 = PeriodDuration.of(Period.ofMonths(3), Duration.ofHours(5));
            PeriodDuration pd2 = PeriodDuration.of(Period.ofMonths(1), Duration.ofHours(2));

            PeriodDuration result = pd1.minus(pd2);
            assertThat(result.getPeriod().getMonths()).isEqualTo(2);
            assertThat(result.getDuration()).isEqualTo(Duration.ofHours(3));
        }

        @Test
        @DisplayName("negated() 取负")
        void testNegated() {
            PeriodDuration pd = PeriodDuration.of(Period.ofMonths(2), Duration.ofHours(3));
            PeriodDuration negated = pd.negated();

            assertThat(negated.getPeriod().getMonths()).isEqualTo(-2);
            assertThat(negated.getDuration()).isEqualTo(Duration.ofHours(-3));
        }

        @Test
        @DisplayName("multipliedBy() 乘以标量")
        void testMultipliedBy() {
            PeriodDuration pd = PeriodDuration.of(Period.ofMonths(2), Duration.ofHours(3));
            PeriodDuration result = pd.multipliedBy(2);

            assertThat(result.getPeriod().getMonths()).isEqualTo(4);
            assertThat(result.getDuration()).isEqualTo(Duration.ofHours(6));
        }

        @Test
        @DisplayName("multipliedBy() 乘以0返回ZERO")
        void testMultipliedByZero() {
            PeriodDuration pd = PeriodDuration.of(Period.ofMonths(2), Duration.ofHours(3));
            assertThat(pd.multipliedBy(0)).isSameAs(PeriodDuration.ZERO);
        }

        @Test
        @DisplayName("multipliedBy() 乘以1返回自身")
        void testMultipliedByOne() {
            PeriodDuration pd = PeriodDuration.of(Period.ofMonths(2), Duration.ofHours(3));
            assertThat(pd.multipliedBy(1)).isSameAs(pd);
        }

        @Test
        @DisplayName("normalized() 标准化")
        void testNormalized() {
            PeriodDuration pd = PeriodDuration.of(Period.of(1, 13, 0), Duration.ZERO);
            PeriodDuration normalized = pd.normalized();

            assertThat(normalized.getPeriod().getYears()).isEqualTo(2);
            assertThat(normalized.getPeriod().getMonths()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("TemporalAmount实现测试")
    class TemporalAmountTests {

        @Test
        @DisplayName("get() 获取单位值")
        void testGet() {
            PeriodDuration pd = PeriodDuration.of(
                    Period.of(1, 2, 3),
                    Duration.ofSeconds(100)
            );

            assertThat(pd.get(ChronoUnit.YEARS)).isEqualTo(1);
            assertThat(pd.get(ChronoUnit.MONTHS)).isEqualTo(2);
            assertThat(pd.get(ChronoUnit.DAYS)).isEqualTo(3);
            assertThat(pd.get(ChronoUnit.SECONDS)).isEqualTo(100);
        }

        @Test
        @DisplayName("getUnits() 获取支持的单位")
        void testGetUnits() {
            PeriodDuration pd = PeriodDuration.ZERO;
            assertThat(pd.getUnits()).containsExactly(
                    ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.DAYS,
                    ChronoUnit.SECONDS, ChronoUnit.NANOS
            );
        }

        @Test
        @DisplayName("addTo() 加到日期时间")
        void testAddTo() {
            PeriodDuration pd = PeriodDuration.of(Period.ofMonths(1), Duration.ofHours(2));
            LocalDateTime dt = LocalDateTime.of(2024, 1, 1, 10, 0);

            LocalDateTime result = (LocalDateTime) pd.addTo(dt);
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 2, 1, 12, 0));
        }

        @Test
        @DisplayName("subtractFrom() 从日期时间减去")
        void testSubtractFrom() {
            PeriodDuration pd = PeriodDuration.of(Period.ofMonths(1), Duration.ofHours(2));
            LocalDateTime dt = LocalDateTime.of(2024, 2, 1, 12, 0);

            LocalDateTime result = (LocalDateTime) pd.subtractFrom(dt);
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等对象")
        void testEquals() {
            PeriodDuration pd1 = PeriodDuration.of(Period.ofMonths(2), Duration.ofHours(3));
            PeriodDuration pd2 = PeriodDuration.of(Period.ofMonths(2), Duration.ofHours(3));
            PeriodDuration pd3 = PeriodDuration.of(Period.ofMonths(3), Duration.ofHours(3));

            assertThat(pd1).isEqualTo(pd2);
            assertThat(pd1).isNotEqualTo(pd3);
            assertThat(pd1).isEqualTo(pd1);
            assertThat(pd1).isNotEqualTo(null);
        }

        @Test
        @DisplayName("hashCode() 相等对象相同哈希码")
        void testHashCode() {
            PeriodDuration pd1 = PeriodDuration.of(Period.ofMonths(2), Duration.ofHours(3));
            PeriodDuration pd2 = PeriodDuration.of(Period.ofMonths(2), Duration.ofHours(3));
            assertThat(pd1.hashCode()).isEqualTo(pd2.hashCode());
        }

        @Test
        @DisplayName("toString() ZERO格式")
        void testToStringZero() {
            assertThat(PeriodDuration.ZERO.toString()).isEqualTo("PT0S");
        }

        @Test
        @DisplayName("toString() 仅Period格式")
        void testToStringPeriodOnly() {
            PeriodDuration pd = PeriodDuration.ofPeriod(Period.ofMonths(2));
            assertThat(pd.toString()).isEqualTo("P2M");
        }

        @Test
        @DisplayName("toString() 仅Duration格式")
        void testToStringDurationOnly() {
            PeriodDuration pd = PeriodDuration.ofDuration(Duration.ofHours(3));
            assertThat(pd.toString()).isEqualTo("PT3H");
        }

        @Test
        @DisplayName("toString() 完整格式")
        void testToStringFull() {
            PeriodDuration pd = PeriodDuration.of(Period.ofMonths(2), Duration.ofHours(3));
            assertThat(pd.toString()).contains("P2M");
            assertThat(pd.toString()).contains("T3H");
        }
    }
}
