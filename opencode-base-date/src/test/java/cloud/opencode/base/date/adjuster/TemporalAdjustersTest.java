package cloud.opencode.base.date.adjuster;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjuster;

import static org.assertj.core.api.Assertions.*;

/**
 * TemporalAdjusters 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("TemporalAdjusters 测试")
class TemporalAdjustersTest {

    @Nested
    @DisplayName("JDK别名测试")
    class JdkAliasTests {

        @Test
        @DisplayName("firstDayOfMonth() 月初")
        void testFirstDayOfMonth() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate result = date.with(TemporalAdjusters.firstDayOfMonth());
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 1));
        }

        @Test
        @DisplayName("lastDayOfMonth() 月末")
        void testLastDayOfMonth() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate result = date.with(TemporalAdjusters.lastDayOfMonth());
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 30));
        }

        @Test
        @DisplayName("firstDayOfNextMonth() 下月初")
        void testFirstDayOfNextMonth() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate result = date.with(TemporalAdjusters.firstDayOfNextMonth());
            assertThat(result).isEqualTo(LocalDate.of(2024, 7, 1));
        }

        @Test
        @DisplayName("firstDayOfYear() 年初")
        void testFirstDayOfYear() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate result = date.with(TemporalAdjusters.firstDayOfYear());
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 1));
        }

        @Test
        @DisplayName("lastDayOfYear() 年末")
        void testLastDayOfYear() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate result = date.with(TemporalAdjusters.lastDayOfYear());
            assertThat(result).isEqualTo(LocalDate.of(2024, 12, 31));
        }

        @Test
        @DisplayName("firstDayOfNextYear() 下年初")
        void testFirstDayOfNextYear() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate result = date.with(TemporalAdjusters.firstDayOfNextYear());
            assertThat(result).isEqualTo(LocalDate.of(2025, 1, 1));
        }

        @Test
        @DisplayName("next() 下一个周几")
        void testNext() {
            LocalDate date = LocalDate.of(2024, 6, 10); // Monday
            LocalDate result = date.with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 12));
        }

        @Test
        @DisplayName("nextOrSame() 下一个或相同周几")
        void testNextOrSame() {
            LocalDate monday = LocalDate.of(2024, 6, 10);
            assertThat(monday.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY)))
                    .isEqualTo(monday);
        }

        @Test
        @DisplayName("previous() 上一个周几")
        void testPrevious() {
            LocalDate date = LocalDate.of(2024, 6, 10); // Monday
            LocalDate result = date.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 7));
        }

        @Test
        @DisplayName("previousOrSame() 上一个或相同周几")
        void testPreviousOrSame() {
            LocalDate monday = LocalDate.of(2024, 6, 10);
            assertThat(monday.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
                    .isEqualTo(monday);
        }

        @Test
        @DisplayName("dayOfWeekInMonth() 月内第N个周几")
        void testDayOfWeekInMonth() {
            LocalDate date = LocalDate.of(2024, 6, 1);
            LocalDate result = date.with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.MONDAY));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 17));
        }
    }

    @Nested
    @DisplayName("季度调整器测试")
    class QuarterAdjusterTests {

        @Test
        @DisplayName("firstDayOfQuarter() 季度首日")
        void testFirstDayOfQuarter() {
            assertThat(LocalDate.of(2024, 2, 15).with(TemporalAdjusters.firstDayOfQuarter()))
                    .isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(LocalDate.of(2024, 5, 15).with(TemporalAdjusters.firstDayOfQuarter()))
                    .isEqualTo(LocalDate.of(2024, 4, 1));
            assertThat(LocalDate.of(2024, 8, 15).with(TemporalAdjusters.firstDayOfQuarter()))
                    .isEqualTo(LocalDate.of(2024, 7, 1));
            assertThat(LocalDate.of(2024, 11, 15).with(TemporalAdjusters.firstDayOfQuarter()))
                    .isEqualTo(LocalDate.of(2024, 10, 1));
        }

        @Test
        @DisplayName("lastDayOfQuarter() 季度末日")
        void testLastDayOfQuarter() {
            assertThat(LocalDate.of(2024, 2, 15).with(TemporalAdjusters.lastDayOfQuarter()))
                    .isEqualTo(LocalDate.of(2024, 3, 31));
            assertThat(LocalDate.of(2024, 5, 15).with(TemporalAdjusters.lastDayOfQuarter()))
                    .isEqualTo(LocalDate.of(2024, 6, 30));
            assertThat(LocalDate.of(2024, 8, 15).with(TemporalAdjusters.lastDayOfQuarter()))
                    .isEqualTo(LocalDate.of(2024, 9, 30));
            assertThat(LocalDate.of(2024, 11, 15).with(TemporalAdjusters.lastDayOfQuarter()))
                    .isEqualTo(LocalDate.of(2024, 12, 31));
        }

        @Test
        @DisplayName("firstDayOfNextQuarter() 下季度首日")
        void testFirstDayOfNextQuarter() {
            assertThat(LocalDate.of(2024, 2, 15).with(TemporalAdjusters.firstDayOfNextQuarter()))
                    .isEqualTo(LocalDate.of(2024, 4, 1));
            assertThat(LocalDate.of(2024, 11, 15).with(TemporalAdjusters.firstDayOfNextQuarter()))
                    .isEqualTo(LocalDate.of(2025, 1, 1));
        }
    }

    @Nested
    @DisplayName("半年调整器测试")
    class HalfYearAdjusterTests {

        @Test
        @DisplayName("firstDayOfHalf() 半年首日")
        void testFirstDayOfHalf() {
            assertThat(LocalDate.of(2024, 3, 15).with(TemporalAdjusters.firstDayOfHalf()))
                    .isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(LocalDate.of(2024, 8, 15).with(TemporalAdjusters.firstDayOfHalf()))
                    .isEqualTo(LocalDate.of(2024, 7, 1));
        }

        @Test
        @DisplayName("lastDayOfHalf() 半年末日")
        void testLastDayOfHalf() {
            assertThat(LocalDate.of(2024, 3, 15).with(TemporalAdjusters.lastDayOfHalf()))
                    .isEqualTo(LocalDate.of(2024, 6, 30));
            assertThat(LocalDate.of(2024, 8, 15).with(TemporalAdjusters.lastDayOfHalf()))
                    .isEqualTo(LocalDate.of(2024, 12, 31));
        }
    }

    @Nested
    @DisplayName("周调整器测试")
    class WeekAdjusterTests {

        @Test
        @DisplayName("firstDayOfWeek() 默认周一")
        void testFirstDayOfWeek() {
            LocalDate thursday = LocalDate.of(2024, 6, 13);
            LocalDate result = thursday.with(TemporalAdjusters.firstDayOfWeek());
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 10));
            assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        }

        @Test
        @DisplayName("firstDayOfWeek(DayOfWeek) 自定义首日")
        void testFirstDayOfWeekCustom() {
            LocalDate thursday = LocalDate.of(2024, 6, 13);
            LocalDate result = thursday.with(TemporalAdjusters.firstDayOfWeek(DayOfWeek.SUNDAY));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 9));
        }

        @Test
        @DisplayName("lastDayOfWeek() 默认周日")
        void testLastDayOfWeek() {
            LocalDate thursday = LocalDate.of(2024, 6, 13);
            LocalDate result = thursday.with(TemporalAdjusters.lastDayOfWeek());
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 16));
            assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
        }

        @Test
        @DisplayName("lastDayOfWeek(DayOfWeek) 自定义末日")
        void testLastDayOfWeekCustom() {
            LocalDate thursday = LocalDate.of(2024, 6, 13);
            LocalDate result = thursday.with(TemporalAdjusters.lastDayOfWeek(DayOfWeek.SATURDAY));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 15));
        }
    }

    @Nested
    @DisplayName("工作日调整器测试")
    class WorkdayAdjusterTests {

        @Test
        @DisplayName("nextWorkday() 下一个工作日")
        void testNextWorkday() {
            LocalDate friday = LocalDate.of(2024, 6, 14);
            LocalDate result = friday.with(TemporalAdjusters.nextWorkday());
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 17)); // Monday
        }

        @Test
        @DisplayName("previousWorkday() 上一个工作日")
        void testPreviousWorkday() {
            LocalDate monday = LocalDate.of(2024, 6, 17);
            LocalDate result = monday.with(TemporalAdjusters.previousWorkday());
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 14)); // Friday
        }

        @Test
        @DisplayName("nextOrSameWorkday() 已是工作日返回自身")
        void testNextOrSameWorkdayAlready() {
            LocalDate wednesday = LocalDate.of(2024, 6, 12);
            LocalDate result = wednesday.with(TemporalAdjusters.nextOrSameWorkday());
            assertThat(result).isEqualTo(wednesday);
        }

        @Test
        @DisplayName("nextOrSameWorkday() 周末返回下周一")
        void testNextOrSameWorkdayWeekend() {
            LocalDate saturday = LocalDate.of(2024, 6, 15);
            LocalDate result = saturday.with(TemporalAdjusters.nextOrSameWorkday());
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 17)); // Monday
        }

        @Test
        @DisplayName("plusWorkdays() 加工作日")
        void testPlusWorkdays() {
            LocalDate friday = LocalDate.of(2024, 6, 14);
            LocalDate result = friday.with(TemporalAdjusters.plusWorkdays(5));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 21)); // Friday
        }

        @Test
        @DisplayName("minusWorkdays() 减工作日")
        void testMinusWorkdays() {
            LocalDate friday = LocalDate.of(2024, 6, 21);
            LocalDate result = friday.with(TemporalAdjusters.minusWorkdays(5));
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 14)); // Friday
        }
    }

    @Nested
    @DisplayName("日内时间调整器测试")
    class TimeOfDayAdjusterTests {

        @Test
        @DisplayName("startOfDay() 日初")
        void testStartOfDay() {
            LocalDateTime dt = LocalDateTime.of(2024, 6, 15, 14, 30, 45);
            LocalDateTime result = (LocalDateTime) TemporalAdjusters.startOfDay().adjustInto(dt);
            assertThat(result.toLocalTime()).isEqualTo(LocalTime.MIN);
        }

        @Test
        @DisplayName("startOfDay() 非LocalDateTime不变")
        void testStartOfDayNonLocalDateTime() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate result = (LocalDate) TemporalAdjusters.startOfDay().adjustInto(date);
            assertThat(result).isEqualTo(date);
        }

        @Test
        @DisplayName("endOfDay() 日末")
        void testEndOfDay() {
            LocalDateTime dt = LocalDateTime.of(2024, 6, 15, 14, 30, 45);
            LocalDateTime result = (LocalDateTime) TemporalAdjusters.endOfDay().adjustInto(dt);
            assertThat(result.toLocalTime()).isEqualTo(LocalTime.MAX);
        }

        @Test
        @DisplayName("noon() 正午")
        void testNoon() {
            LocalDateTime dt = LocalDateTime.of(2024, 6, 15, 14, 30, 45);
            LocalDateTime result = (LocalDateTime) TemporalAdjusters.noon().adjustInto(dt);
            assertThat(result.toLocalTime()).isEqualTo(LocalTime.NOON);
        }

        @Test
        @DisplayName("atHour() 指定小时")
        void testAtHour() {
            LocalDateTime dt = LocalDateTime.of(2024, 6, 15, 14, 30, 45, 123456789);
            LocalDateTime result = (LocalDateTime) TemporalAdjusters.atHour(9).adjustInto(dt);
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 6, 15, 9, 0, 0, 0));
        }

        @Test
        @DisplayName("atHour() 无效小时抛出异常")
        void testAtHourInvalid() {
            assertThatThrownBy(() -> TemporalAdjusters.atHour(-1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> TemporalAdjusters.atHour(24))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("组合调整器测试")
    class CompositeAdjusterTests {

        @Test
        @DisplayName("compose() 组合多个调整器")
        void testCompose() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            TemporalAdjuster combined = TemporalAdjusters.compose(
                    TemporalAdjusters.firstDayOfNextMonth(),
                    TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY)
            );
            LocalDate result = date.with(combined);
            assertThat(result).isEqualTo(LocalDate.of(2024, 7, 1)); // July 1st is Monday
        }

        @Test
        @DisplayName("compose() 空数组返回恒等调整器")
        void testComposeEmpty() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            TemporalAdjuster combined = TemporalAdjusters.compose();
            LocalDate result = date.with(combined);
            assertThat(result).isEqualTo(date);
        }

        @Test
        @DisplayName("compose() null抛出异常")
        void testComposeNull() {
            assertThatThrownBy(() -> TemporalAdjusters.compose((TemporalAdjuster[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("andThen() 依次应用")
        void testAndThen() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            TemporalAdjuster combined = TemporalAdjusters.andThen(
                    TemporalAdjusters.firstDayOfMonth(),
                    TemporalAdjusters.next(DayOfWeek.FRIDAY)
            );
            LocalDate result = date.with(combined);
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 7)); // First Friday in June
        }

        @Test
        @DisplayName("andThen() null抛出异常")
        void testAndThenNull() {
            assertThatThrownBy(() -> TemporalAdjusters.andThen(null, TemporalAdjusters.firstDayOfMonth()))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> TemporalAdjusters.andThen(TemporalAdjusters.firstDayOfMonth(), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("自定义调整器测试")
    class CustomAdjusterTests {

        @Test
        @DisplayName("of() 包装自定义调整器")
        void testOf() {
            TemporalAdjuster custom = temporal -> LocalDate.from(temporal).plusDays(10);
            TemporalAdjuster wrapped = TemporalAdjusters.of(custom);
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate result = date.with(wrapped);
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 25));
        }

        @Test
        @DisplayName("of() null抛出异常")
        void testOfNull() {
            assertThatThrownBy(() -> TemporalAdjusters.of(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
