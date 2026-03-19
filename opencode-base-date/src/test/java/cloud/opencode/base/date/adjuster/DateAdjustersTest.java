package cloud.opencode.base.date.adjuster;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * DateAdjusters 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("DateAdjusters 测试")
class DateAdjustersTest {

    @Nested
    @DisplayName("年调整器测试")
    class YearAdjusterTests {

        @Test
        @DisplayName("startOfYear() 返回年初")
        void testStartOfYear() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate result = date.with(DateAdjusters.startOfYear());
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 1));
        }

        @Test
        @DisplayName("endOfYear() 返回年末")
        void testEndOfYear() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate result = date.with(DateAdjusters.endOfYear());
            assertThat(result).isEqualTo(LocalDate.of(2024, 12, 31));
        }

        @Test
        @DisplayName("endOfYear() 闰年返回366天")
        void testEndOfYearLeap() {
            LocalDate date = LocalDate.of(2024, 3, 1);
            LocalDate result = date.with(DateAdjusters.endOfYear());
            assertThat(result.getDayOfYear()).isEqualTo(366);
        }
    }

    @Nested
    @DisplayName("季度调整器测试")
    class QuarterAdjusterTests {

        @Test
        @DisplayName("startOfQuarter() Q1")
        void testStartOfQuarterQ1() {
            LocalDate date = LocalDate.of(2024, 2, 15);
            LocalDate result = date.with(DateAdjusters.startOfQuarter());
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 1));
        }

        @Test
        @DisplayName("startOfQuarter() Q2")
        void testStartOfQuarterQ2() {
            LocalDate date = LocalDate.of(2024, 5, 20);
            LocalDate result = date.with(DateAdjusters.startOfQuarter());
            assertThat(result).isEqualTo(LocalDate.of(2024, 4, 1));
        }

        @Test
        @DisplayName("startOfQuarter() Q3")
        void testStartOfQuarterQ3() {
            LocalDate date = LocalDate.of(2024, 8, 10);
            LocalDate result = date.with(DateAdjusters.startOfQuarter());
            assertThat(result).isEqualTo(LocalDate.of(2024, 7, 1));
        }

        @Test
        @DisplayName("startOfQuarter() Q4")
        void testStartOfQuarterQ4() {
            LocalDate date = LocalDate.of(2024, 11, 25);
            LocalDate result = date.with(DateAdjusters.startOfQuarter());
            assertThat(result).isEqualTo(LocalDate.of(2024, 10, 1));
        }

        @Test
        @DisplayName("endOfQuarter() Q1")
        void testEndOfQuarterQ1() {
            LocalDate date = LocalDate.of(2024, 2, 15);
            LocalDate result = date.with(DateAdjusters.endOfQuarter());
            assertThat(result).isEqualTo(LocalDate.of(2024, 3, 31));
        }

        @Test
        @DisplayName("endOfQuarter() Q2")
        void testEndOfQuarterQ2() {
            LocalDate date = LocalDate.of(2024, 5, 20);
            LocalDate result = date.with(DateAdjusters.endOfQuarter());
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 30));
        }

        @Test
        @DisplayName("startOfQuarter(int) 特定季度")
        void testStartOfQuarterSpecific() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            assertThat(date.with(DateAdjusters.startOfQuarter(1))).isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(date.with(DateAdjusters.startOfQuarter(2))).isEqualTo(LocalDate.of(2024, 4, 1));
            assertThat(date.with(DateAdjusters.startOfQuarter(3))).isEqualTo(LocalDate.of(2024, 7, 1));
            assertThat(date.with(DateAdjusters.startOfQuarter(4))).isEqualTo(LocalDate.of(2024, 10, 1));
        }

        @Test
        @DisplayName("startOfQuarter(int) 无效季度抛出异常")
        void testStartOfQuarterInvalid() {
            assertThatThrownBy(() -> DateAdjusters.startOfQuarter(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> DateAdjusters.startOfQuarter(5))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("月调整器测试")
    class MonthAdjusterTests {

        @Test
        @DisplayName("startOfMonth() 返回月初")
        void testStartOfMonth() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate result = date.with(DateAdjusters.startOfMonth());
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 1));
        }

        @Test
        @DisplayName("endOfMonth() 返回月末")
        void testEndOfMonth() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate result = date.with(DateAdjusters.endOfMonth());
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 30));
        }

        @Test
        @DisplayName("endOfMonth() 闰年2月")
        void testEndOfMonthFebLeap() {
            LocalDate date = LocalDate.of(2024, 2, 15);
            LocalDate result = date.with(DateAdjusters.endOfMonth());
            assertThat(result).isEqualTo(LocalDate.of(2024, 2, 29));
        }
    }

    @Nested
    @DisplayName("周调整器测试")
    class WeekAdjusterTests {

        @Test
        @DisplayName("startOfWeek() 默认返回周一")
        void testStartOfWeek() {
            LocalDate date = LocalDate.of(2024, 6, 13); // Thursday
            LocalDate result = date.with(DateAdjusters.startOfWeek());
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 10)); // Monday
            assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        }

        @Test
        @DisplayName("startOfWeek(DayOfWeek) 自定义首日")
        void testStartOfWeekCustom() {
            LocalDate date = LocalDate.of(2024, 6, 13); // Thursday
            LocalDate result = date.with(DateAdjusters.startOfWeek(DayOfWeek.SUNDAY));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 9)); // Sunday
        }

        @Test
        @DisplayName("endOfWeek() 默认返回周日")
        void testEndOfWeek() {
            LocalDate date = LocalDate.of(2024, 6, 13); // Thursday
            LocalDate result = date.with(DateAdjusters.endOfWeek());
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 16)); // Sunday
            assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
        }

        @Test
        @DisplayName("endOfWeek(DayOfWeek) 自定义末日")
        void testEndOfWeekCustom() {
            LocalDate date = LocalDate.of(2024, 6, 13); // Thursday
            LocalDate result = date.with(DateAdjusters.endOfWeek(DayOfWeek.SATURDAY));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 15)); // Saturday
        }
    }

    @Nested
    @DisplayName("第N天调整器测试")
    class NthDayAdjusterTests {

        @Test
        @DisplayName("dayOfMonth() 设置月份日期")
        void testDayOfMonth() {
            LocalDate date = LocalDate.of(2024, 6, 1);
            LocalDate result = date.with(DateAdjusters.dayOfMonth(15));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 15));
        }

        @Test
        @DisplayName("dayOfMonth() 超出月份天数则取最大")
        void testDayOfMonthMax() {
            LocalDate date = LocalDate.of(2024, 2, 1);
            LocalDate result = date.with(DateAdjusters.dayOfMonth(31));
            assertThat(result).isEqualTo(LocalDate.of(2024, 2, 29)); // leap year
        }

        @Test
        @DisplayName("dayOfMonth() 无效天数抛出异常")
        void testDayOfMonthInvalid() {
            assertThatThrownBy(() -> DateAdjusters.dayOfMonth(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> DateAdjusters.dayOfMonth(32))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("nthDayOfWeekInMonth() 第N个周几")
        void testNthDayOfWeekInMonth() {
            LocalDate date = LocalDate.of(2024, 6, 1);
            // 第2个周一
            LocalDate result = date.with(DateAdjusters.nthDayOfWeekInMonth(2, DayOfWeek.MONDAY));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 10));
        }

        @Test
        @DisplayName("nthDayOfWeekInMonth() 最后一个周几")
        void testLastDayOfWeekInMonth() {
            LocalDate date = LocalDate.of(2024, 6, 1);
            LocalDate result = date.with(DateAdjusters.nthDayOfWeekInMonth(-1, DayOfWeek.FRIDAY));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 28));
        }

        @Test
        @DisplayName("nthDayOfWeekInMonth() 无效序数抛出异常")
        void testNthDayOfWeekInMonthInvalid() {
            assertThatThrownBy(() -> DateAdjusters.nthDayOfWeekInMonth(0, DayOfWeek.MONDAY))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> DateAdjusters.nthDayOfWeekInMonth(6, DayOfWeek.MONDAY))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("相对调整器测试")
    class RelativeAdjusterTests {

        @Test
        @DisplayName("nextOrSame() 相同则返回自身")
        void testNextOrSameSame() {
            LocalDate date = LocalDate.of(2024, 6, 10); // Monday
            LocalDate result = date.with(DateAdjusters.nextOrSame(DayOfWeek.MONDAY));
            assertThat(result).isEqualTo(date);
        }

        @Test
        @DisplayName("nextOrSame() 不同则返回下一个")
        void testNextOrSameNext() {
            LocalDate date = LocalDate.of(2024, 6, 11); // Tuesday
            LocalDate result = date.with(DateAdjusters.nextOrSame(DayOfWeek.FRIDAY));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 14));
        }

        @Test
        @DisplayName("previousOrSame() 相同则返回自身")
        void testPreviousOrSameSame() {
            LocalDate date = LocalDate.of(2024, 6, 10); // Monday
            LocalDate result = date.with(DateAdjusters.previousOrSame(DayOfWeek.MONDAY));
            assertThat(result).isEqualTo(date);
        }

        @Test
        @DisplayName("next() 返回下一个")
        void testNext() {
            LocalDate date = LocalDate.of(2024, 6, 10); // Monday
            LocalDate result = date.with(DateAdjusters.next(DayOfWeek.MONDAY));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 17));
        }

        @Test
        @DisplayName("previous() 返回上一个")
        void testPrevious() {
            LocalDate date = LocalDate.of(2024, 6, 10); // Monday
            LocalDate result = date.with(DateAdjusters.previous(DayOfWeek.MONDAY));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 3));
        }
    }

    @Nested
    @DisplayName("半年调整器测试")
    class HalfYearAdjusterTests {

        @Test
        @DisplayName("startOfFirstHalf() 返回上半年开始")
        void testStartOfFirstHalf() {
            LocalDate date = LocalDate.of(2024, 3, 15);
            LocalDate result = date.with(DateAdjusters.startOfFirstHalf());
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 1));
        }

        @Test
        @DisplayName("endOfFirstHalf() 返回上半年结束")
        void testEndOfFirstHalf() {
            LocalDate date = LocalDate.of(2024, 3, 15);
            LocalDate result = date.with(DateAdjusters.endOfFirstHalf());
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 30));
        }

        @Test
        @DisplayName("startOfSecondHalf() 返回下半年开始")
        void testStartOfSecondHalf() {
            LocalDate date = LocalDate.of(2024, 8, 15);
            LocalDate result = date.with(DateAdjusters.startOfSecondHalf());
            assertThat(result).isEqualTo(LocalDate.of(2024, 7, 1));
        }

        @Test
        @DisplayName("endOfSecondHalf() 返回下半年结束")
        void testEndOfSecondHalf() {
            LocalDate date = LocalDate.of(2024, 8, 15);
            LocalDate result = date.with(DateAdjusters.endOfSecondHalf());
            assertThat(result).isEqualTo(LocalDate.of(2024, 12, 31));
        }
    }

    @Nested
    @DisplayName("特殊日期调整器测试")
    class SpecialDateAdjusterTests {

        @Test
        @DisplayName("nextMonthDay() 下一个特定月日")
        void testNextMonthDay() {
            LocalDate date = LocalDate.of(2024, 3, 1);
            LocalDate result = date.with(DateAdjusters.nextMonthDay(Month.JUNE, 15));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 15));
        }

        @Test
        @DisplayName("nextMonthDay() 已过则下一年")
        void testNextMonthDayNextYear() {
            LocalDate date = LocalDate.of(2024, 7, 1);
            LocalDate result = date.with(DateAdjusters.nextMonthDay(Month.JUNE, 15));
            assertThat(result).isEqualTo(LocalDate.of(2025, 6, 15));
        }

        @Test
        @DisplayName("plusBusinessDays() 加工作日")
        void testPlusBusinessDays() {
            LocalDate date = LocalDate.of(2024, 6, 14); // Friday
            LocalDate result = date.with(DateAdjusters.plusBusinessDays(3));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 19)); // Wednesday
        }

        @Test
        @DisplayName("plusBusinessDays() 负数减工作日")
        void testPlusBusinessDaysNegative() {
            LocalDate date = LocalDate.of(2024, 6, 19); // Wednesday
            LocalDate result = date.with(DateAdjusters.plusBusinessDays(-3));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 14)); // Friday
        }

        @Test
        @DisplayName("nearestWeekday() 周六变周五")
        void testNearestWeekdaySaturday() {
            LocalDate saturday = LocalDate.of(2024, 6, 15);
            LocalDate result = saturday.with(DateAdjusters.nearestWeekday());
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 14)); // Friday
        }

        @Test
        @DisplayName("nearestWeekday() 周日变周一")
        void testNearestWeekdaySunday() {
            LocalDate sunday = LocalDate.of(2024, 6, 16);
            LocalDate result = sunday.with(DateAdjusters.nearestWeekday());
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 17)); // Monday
        }

        @Test
        @DisplayName("nearestWeekday() 工作日不变")
        void testNearestWeekdayWeekday() {
            LocalDate wednesday = LocalDate.of(2024, 6, 12);
            LocalDate result = wednesday.with(DateAdjusters.nearestWeekday());
            assertThat(result).isEqualTo(wednesday);
        }
    }

    @Nested
    @DisplayName("工具调整器测试")
    class UtilityAdjusterTests {

        @Test
        @DisplayName("plus() 添加指定单位")
        void testPlus() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate result = date.with(DateAdjusters.plus(5, ChronoUnit.DAYS));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 20));
        }

        @Test
        @DisplayName("minus() 减去指定单位")
        void testMinus() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate result = date.with(DateAdjusters.minus(5, ChronoUnit.DAYS));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 10));
        }
    }
}
