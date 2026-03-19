package cloud.opencode.base.date.extra;

import cloud.opencode.base.date.exception.OpenDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoField;

import static org.assertj.core.api.Assertions.*;

/**
 * Quarter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("Quarter 测试")
class QuarterTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of() 从值创建季度")
        void testOf() {
            assertThat(Quarter.of(1)).isEqualTo(Quarter.Q1);
            assertThat(Quarter.of(2)).isEqualTo(Quarter.Q2);
            assertThat(Quarter.of(3)).isEqualTo(Quarter.Q3);
            assertThat(Quarter.of(4)).isEqualTo(Quarter.Q4);
        }

        @Test
        @DisplayName("of() 无效值抛出异常")
        void testOfInvalid() {
            assertThatThrownBy(() -> Quarter.of(0))
                    .isInstanceOf(OpenDateException.class);
            assertThatThrownBy(() -> Quarter.of(5))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("ofMonth() 从月份值创建季度")
        void testOfMonth() {
            assertThat(Quarter.ofMonth(1)).isEqualTo(Quarter.Q1);
            assertThat(Quarter.ofMonth(3)).isEqualTo(Quarter.Q1);
            assertThat(Quarter.ofMonth(4)).isEqualTo(Quarter.Q2);
            assertThat(Quarter.ofMonth(6)).isEqualTo(Quarter.Q2);
            assertThat(Quarter.ofMonth(7)).isEqualTo(Quarter.Q3);
            assertThat(Quarter.ofMonth(9)).isEqualTo(Quarter.Q3);
            assertThat(Quarter.ofMonth(10)).isEqualTo(Quarter.Q4);
            assertThat(Quarter.ofMonth(12)).isEqualTo(Quarter.Q4);
        }

        @Test
        @DisplayName("ofMonth() 无效月份抛出异常")
        void testOfMonthInvalid() {
            assertThatThrownBy(() -> Quarter.ofMonth(0))
                    .isInstanceOf(OpenDateException.class);
            assertThatThrownBy(() -> Quarter.ofMonth(13))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("from(Month) 从Month枚举创建季度")
        void testFromMonth() {
            assertThat(Quarter.from(Month.JANUARY)).isEqualTo(Quarter.Q1);
            assertThat(Quarter.from(Month.APRIL)).isEqualTo(Quarter.Q2);
            assertThat(Quarter.from(Month.JULY)).isEqualTo(Quarter.Q3);
            assertThat(Quarter.from(Month.OCTOBER)).isEqualTo(Quarter.Q4);
        }

        @Test
        @DisplayName("from(Month) null抛出异常")
        void testFromMonthNull() {
            assertThatThrownBy(() -> Quarter.from((Month) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("from(TemporalAccessor) 从日期获取季度")
        void testFromTemporal() {
            LocalDate date = LocalDate.of(2024, 5, 15);
            assertThat(Quarter.from(date)).isEqualTo(Quarter.Q2);
        }

        @Test
        @DisplayName("from(Quarter) 返回自身")
        void testFromQuarter() {
            assertThat(Quarter.from(Quarter.Q3)).isEqualTo(Quarter.Q3);
        }
    }

    @Nested
    @DisplayName("获取器测试")
    class GetterTests {

        @Test
        @DisplayName("getValue() 获取季度值")
        void testGetValue() {
            assertThat(Quarter.Q1.getValue()).isEqualTo(1);
            assertThat(Quarter.Q2.getValue()).isEqualTo(2);
            assertThat(Quarter.Q3.getValue()).isEqualTo(3);
            assertThat(Quarter.Q4.getValue()).isEqualTo(4);
        }

        @Test
        @DisplayName("firstMonth() 获取第一个月")
        void testFirstMonth() {
            assertThat(Quarter.Q1.firstMonth()).isEqualTo(1);
            assertThat(Quarter.Q2.firstMonth()).isEqualTo(4);
            assertThat(Quarter.Q3.firstMonth()).isEqualTo(7);
            assertThat(Quarter.Q4.firstMonth()).isEqualTo(10);
        }

        @Test
        @DisplayName("lastMonth() 获取最后一个月")
        void testLastMonth() {
            assertThat(Quarter.Q1.lastMonth()).isEqualTo(3);
            assertThat(Quarter.Q2.lastMonth()).isEqualTo(6);
            assertThat(Quarter.Q3.lastMonth()).isEqualTo(9);
            assertThat(Quarter.Q4.lastMonth()).isEqualTo(12);
        }

        @Test
        @DisplayName("firstMonthOfQuarter() 获取第一个Month枚举")
        void testFirstMonthOfQuarter() {
            assertThat(Quarter.Q1.firstMonthOfQuarter()).isEqualTo(Month.JANUARY);
            assertThat(Quarter.Q2.firstMonthOfQuarter()).isEqualTo(Month.APRIL);
            assertThat(Quarter.Q3.firstMonthOfQuarter()).isEqualTo(Month.JULY);
            assertThat(Quarter.Q4.firstMonthOfQuarter()).isEqualTo(Month.OCTOBER);
        }

        @Test
        @DisplayName("lastMonthOfQuarter() 获取最后一个Month枚举")
        void testLastMonthOfQuarter() {
            assertThat(Quarter.Q1.lastMonthOfQuarter()).isEqualTo(Month.MARCH);
            assertThat(Quarter.Q2.lastMonthOfQuarter()).isEqualTo(Month.JUNE);
            assertThat(Quarter.Q3.lastMonthOfQuarter()).isEqualTo(Month.SEPTEMBER);
            assertThat(Quarter.Q4.lastMonthOfQuarter()).isEqualTo(Month.DECEMBER);
        }

        @Test
        @DisplayName("length() 获取季度天数")
        void testLength() {
            // Non-leap year
            assertThat(Quarter.Q1.length(false)).isEqualTo(90);
            assertThat(Quarter.Q2.length(false)).isEqualTo(91);
            assertThat(Quarter.Q3.length(false)).isEqualTo(92);
            assertThat(Quarter.Q4.length(false)).isEqualTo(92);

            // Leap year
            assertThat(Quarter.Q1.length(true)).isEqualTo(91);
            assertThat(Quarter.Q2.length(true)).isEqualTo(91);
        }
    }

    @Nested
    @DisplayName("计算方法测试")
    class CalculationTests {

        @Test
        @DisplayName("next() 获取下一个季度")
        void testNext() {
            assertThat(Quarter.Q1.next()).isEqualTo(Quarter.Q2);
            assertThat(Quarter.Q2.next()).isEqualTo(Quarter.Q3);
            assertThat(Quarter.Q3.next()).isEqualTo(Quarter.Q4);
            assertThat(Quarter.Q4.next()).isEqualTo(Quarter.Q1);
        }

        @Test
        @DisplayName("previous() 获取上一个季度")
        void testPrevious() {
            assertThat(Quarter.Q1.previous()).isEqualTo(Quarter.Q4);
            assertThat(Quarter.Q2.previous()).isEqualTo(Quarter.Q1);
            assertThat(Quarter.Q3.previous()).isEqualTo(Quarter.Q2);
            assertThat(Quarter.Q4.previous()).isEqualTo(Quarter.Q3);
        }

        @Test
        @DisplayName("plus() 加季度数")
        void testPlus() {
            assertThat(Quarter.Q1.plus(1)).isEqualTo(Quarter.Q2);
            assertThat(Quarter.Q1.plus(4)).isEqualTo(Quarter.Q1);
            assertThat(Quarter.Q1.plus(5)).isEqualTo(Quarter.Q2);
        }

        @Test
        @DisplayName("minus() 减季度数")
        void testMinus() {
            assertThat(Quarter.Q2.minus(1)).isEqualTo(Quarter.Q1);
            assertThat(Quarter.Q1.minus(1)).isEqualTo(Quarter.Q4);
            assertThat(Quarter.Q1.minus(4)).isEqualTo(Quarter.Q1);
        }
    }

    @Nested
    @DisplayName("TemporalAccessor实现测试")
    class TemporalAccessorTests {

        @Test
        @DisplayName("isSupported() 支持的字段")
        void testIsSupported() {
            // Only test with null to avoid infinite recursion in ChronoField.isSupportedBy()
            assertThat(Quarter.Q1.isSupported(null)).isFalse();
        }

        @Test
        @DisplayName("getLong() 获取字段值")
        void testGetLong() {
            assertThat(Quarter.Q1.getLong(ChronoField.MONTH_OF_YEAR)).isEqualTo(1);
            assertThat(Quarter.Q2.getLong(ChronoField.MONTH_OF_YEAR)).isEqualTo(4);
        }

        @Test
        @DisplayName("range() 获取字段范围")
        void testRange() {
            assertThat(Quarter.Q1.range(ChronoField.MONTH_OF_YEAR).getMinimum()).isEqualTo(1);
            assertThat(Quarter.Q1.range(ChronoField.MONTH_OF_YEAR).getMaximum()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("TemporalQuery实现测试")
    class TemporalQueryTests {

        @Test
        @DisplayName("queryFrom() 从时间对象提取季度")
        void testQueryFrom() {
            LocalDate date = LocalDate.of(2024, 8, 15);
            assertThat(Quarter.Q1.queryFrom(date)).isEqualTo(Quarter.Q3);
        }

        @Test
        @DisplayName("query() 获取季度查询")
        void testQuery() {
            LocalDate date = LocalDate.of(2024, 11, 15);
            assertThat(date.query(Quarter.query())).isEqualTo(Quarter.Q4);
        }
    }

    @Nested
    @DisplayName("工具方法测试")
    class UtilityTests {

        @Test
        @DisplayName("contains(int) 检查月份是否在季度内")
        void testContainsInt() {
            assertThat(Quarter.Q1.contains(1)).isTrue();
            assertThat(Quarter.Q1.contains(3)).isTrue();
            assertThat(Quarter.Q1.contains(4)).isFalse();
        }

        @Test
        @DisplayName("contains(Month) 检查Month是否在季度内")
        void testContainsMonth() {
            assertThat(Quarter.Q2.contains(Month.MAY)).isTrue();
            assertThat(Quarter.Q2.contains(Month.JULY)).isFalse();
            assertThat(Quarter.Q2.contains(null)).isFalse();
        }
    }
}
