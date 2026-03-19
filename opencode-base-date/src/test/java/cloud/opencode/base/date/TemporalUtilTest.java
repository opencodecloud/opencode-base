package cloud.opencode.base.date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * TemporalUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("TemporalUtil 测试")
class TemporalUtilTest {

    @Nested
    @DisplayName("转换方法测试")
    class ConversionTests {

        @Test
        @DisplayName("toLocalDateTime(Instant, ZoneId) 转换Instant到LocalDateTime")
        void testToLocalDateTimeFromInstantWithZone() {
            Instant instant = Instant.parse("2024-06-15T04:00:00Z");
            ZoneId zone = ZoneId.of("Asia/Shanghai");

            LocalDateTime result = TemporalUtil.toLocalDateTime(instant, zone);

            assertThat(result.getHour()).isEqualTo(12); // UTC+8
            assertThat(result.toLocalDate()).isEqualTo(LocalDate.of(2024, 6, 15));
        }

        @Test
        @DisplayName("toLocalDateTime(Instant) 使用系统默认时区")
        void testToLocalDateTimeFromInstant() {
            Instant instant = Instant.now();

            LocalDateTime result = TemporalUtil.toLocalDateTime(instant);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("toLocalDateTime() null参数抛出异常")
        void testToLocalDateTimeNull() {
            assertThatThrownBy(() -> TemporalUtil.toLocalDateTime((Instant) null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> TemporalUtil.toLocalDateTime(Instant.now(), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("toLocalDateTime(LocalDate) 转换LocalDate到LocalDateTime")
        void testToLocalDateTimeFromLocalDate() {
            LocalDate date = LocalDate.of(2024, 6, 15);

            LocalDateTime result = TemporalUtil.toLocalDateTime(date);

            assertThat(result.toLocalDate()).isEqualTo(date);
            assertThat(result.toLocalTime()).isEqualTo(LocalTime.MIDNIGHT);
        }

        @Test
        @DisplayName("toLocalDateTime(LocalDate) null抛出异常")
        void testToLocalDateTimeFromLocalDateNull() {
            assertThatThrownBy(() -> TemporalUtil.toLocalDateTime((LocalDate) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("toInstant(LocalDateTime, ZoneId) 转换LocalDateTime到Instant")
        void testToInstantWithZone() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 12, 0, 0);
            ZoneId zone = ZoneId.of("Asia/Shanghai");

            Instant result = TemporalUtil.toInstant(dateTime, zone);

            assertThat(result).isEqualTo(Instant.parse("2024-06-15T04:00:00Z"));
        }

        @Test
        @DisplayName("toInstant(LocalDateTime) 使用系统默认时区")
        void testToInstant() {
            LocalDateTime dateTime = LocalDateTime.now();

            Instant result = TemporalUtil.toInstant(dateTime);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("toInstant() null参数抛出异常")
        void testToInstantNull() {
            assertThatThrownBy(() -> TemporalUtil.toInstant(null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> TemporalUtil.toInstant(LocalDateTime.now(), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fromEpochMilli() 从毫秒时间戳转换")
        void testFromEpochMilli() {
            long epochMilli = 1718438400000L; // 2024-06-15T04:00:00Z

            LocalDateTime result = TemporalUtil.fromEpochMilli(epochMilli);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("fromEpochSecond() 从秒时间戳转换")
        void testFromEpochSecond() {
            long epochSecond = 1718438400L;

            LocalDateTime result = TemporalUtil.fromEpochSecond(epochSecond);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("toEpochMilli() 转换为毫秒时间戳")
        void testToEpochMilli() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 12, 0, 0);

            long result = TemporalUtil.toEpochMilli(dateTime);

            assertThat(result).isPositive();
        }

        @Test
        @DisplayName("toEpochSecond() 转换为秒时间戳")
        void testToEpochSecond() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 12, 0, 0);

            long result = TemporalUtil.toEpochSecond(dateTime);

            assertThat(result).isPositive();
        }
    }

    @Nested
    @DisplayName("比较方法测试")
    class ComparisonTests {

        @Test
        @DisplayName("isBetween(LocalDate) 检查日期是否在范围内")
        void testIsBetweenLocalDate() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate start = LocalDate.of(2024, 6, 1);
            LocalDate end = LocalDate.of(2024, 6, 30);

            assertThat(TemporalUtil.isBetween(date, start, end)).isTrue();
            assertThat(TemporalUtil.isBetween(start, start, end)).isTrue(); // 包含边界
            assertThat(TemporalUtil.isBetween(end, start, end)).isTrue(); // 包含边界
        }

        @Test
        @DisplayName("isBetween(LocalDate) 不在范围内返回false")
        void testIsBetweenLocalDateFalse() {
            LocalDate date = LocalDate.of(2024, 7, 15);
            LocalDate start = LocalDate.of(2024, 6, 1);
            LocalDate end = LocalDate.of(2024, 6, 30);

            assertThat(TemporalUtil.isBetween(date, start, end)).isFalse();
        }

        @Test
        @DisplayName("isBetween(LocalDate) null参数抛出异常")
        void testIsBetweenLocalDateNull() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate start = LocalDate.of(2024, 6, 1);
            LocalDate end = LocalDate.of(2024, 6, 30);

            assertThatThrownBy(() -> TemporalUtil.isBetween(null, start, end))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> TemporalUtil.isBetween(date, null, end))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> TemporalUtil.isBetween(date, start, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("isBetween(LocalDateTime) 检查日期时间是否在范围内")
        void testIsBetweenLocalDateTime() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 12, 0, 0);
            LocalDateTime start = LocalDateTime.of(2024, 6, 15, 0, 0, 0);
            LocalDateTime end = LocalDateTime.of(2024, 6, 15, 23, 59, 59);

            assertThat(TemporalUtil.isBetween(dateTime, start, end)).isTrue();
        }

        @Test
        @DisplayName("isBetween(LocalDateTime) null参数抛出异常")
        void testIsBetweenLocalDateTimeNull() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 12, 0, 0);
            LocalDateTime start = LocalDateTime.of(2024, 6, 15, 0, 0, 0);
            LocalDateTime end = LocalDateTime.of(2024, 6, 15, 23, 59, 59);

            assertThatThrownBy(() -> TemporalUtil.isBetween(null, start, end))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("max(LocalDate) 返回较大的日期")
        void testMaxLocalDate() {
            LocalDate a = LocalDate.of(2024, 6, 15);
            LocalDate b = LocalDate.of(2024, 6, 20);

            assertThat(TemporalUtil.max(a, b)).isEqualTo(b);
            assertThat(TemporalUtil.max(b, a)).isEqualTo(b);
        }

        @Test
        @DisplayName("max(LocalDate) null参数抛出异常")
        void testMaxLocalDateNull() {
            LocalDate date = LocalDate.of(2024, 6, 15);

            assertThatThrownBy(() -> TemporalUtil.max(null, date))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> TemporalUtil.max(date, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("min(LocalDate) 返回较小的日期")
        void testMinLocalDate() {
            LocalDate a = LocalDate.of(2024, 6, 15);
            LocalDate b = LocalDate.of(2024, 6, 20);

            assertThat(TemporalUtil.min(a, b)).isEqualTo(a);
            assertThat(TemporalUtil.min(b, a)).isEqualTo(a);
        }

        @Test
        @DisplayName("min(LocalDate) null参数抛出异常")
        void testMinLocalDateNull() {
            LocalDate date = LocalDate.of(2024, 6, 15);

            assertThatThrownBy(() -> TemporalUtil.min(null, date))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("max(LocalDateTime) 返回较大的日期时间")
        void testMaxLocalDateTime() {
            LocalDateTime a = LocalDateTime.of(2024, 6, 15, 10, 0, 0);
            LocalDateTime b = LocalDateTime.of(2024, 6, 15, 14, 0, 0);

            assertThat(TemporalUtil.max(a, b)).isEqualTo(b);
        }

        @Test
        @DisplayName("max(LocalDateTime) null参数抛出异常")
        void testMaxLocalDateTimeNull() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 12, 0, 0);

            assertThatThrownBy(() -> TemporalUtil.max(null, dateTime))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("min(LocalDateTime) 返回较小的日期时间")
        void testMinLocalDateTime() {
            LocalDateTime a = LocalDateTime.of(2024, 6, 15, 10, 0, 0);
            LocalDateTime b = LocalDateTime.of(2024, 6, 15, 14, 0, 0);

            assertThat(TemporalUtil.min(a, b)).isEqualTo(a);
        }

        @Test
        @DisplayName("min(LocalDateTime) null参数抛出异常")
        void testMinLocalDateTimeNull() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 12, 0, 0);

            assertThatThrownBy(() -> TemporalUtil.min(null, dateTime))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("截断方法测试")
    class TruncationTests {

        @Test
        @DisplayName("startOfDay() 返回当天开始")
        void testStartOfDay() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 14, 30, 45, 123456789);

            LocalDateTime result = TemporalUtil.startOfDay(dateTime);

            assertThat(result).isEqualTo(LocalDateTime.of(2024, 6, 15, 0, 0, 0));
        }

        @Test
        @DisplayName("endOfDay() 返回当天结束")
        void testEndOfDay() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 14, 30, 45);

            LocalDateTime result = TemporalUtil.endOfDay(dateTime);

            assertThat(result.toLocalDate()).isEqualTo(LocalDate.of(2024, 6, 15));
            assertThat(result.toLocalTime()).isEqualTo(LocalTime.MAX);
        }

        @Test
        @DisplayName("startOfMonth() 返回当月开始")
        void testStartOfMonth() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 14, 30, 45);

            LocalDateTime result = TemporalUtil.startOfMonth(dateTime);

            assertThat(result).isEqualTo(LocalDateTime.of(2024, 6, 1, 0, 0, 0));
        }

        @Test
        @DisplayName("endOfMonth() 返回当月结束")
        void testEndOfMonth() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 14, 30, 45);

            LocalDateTime result = TemporalUtil.endOfMonth(dateTime);

            assertThat(result.toLocalDate()).isEqualTo(LocalDate.of(2024, 6, 30));
            assertThat(result.toLocalTime()).isEqualTo(LocalTime.MAX);
        }

        @Test
        @DisplayName("startOfYear() 返回当年开始")
        void testStartOfYear() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 14, 30, 45);

            LocalDateTime result = TemporalUtil.startOfYear(dateTime);

            assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0, 0));
        }

        @Test
        @DisplayName("endOfYear() 返回当年结束")
        void testEndOfYear() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 14, 30, 45);

            LocalDateTime result = TemporalUtil.endOfYear(dateTime);

            assertThat(result.toLocalDate()).isEqualTo(LocalDate.of(2024, 12, 31));
            assertThat(result.toLocalTime()).isEqualTo(LocalTime.MAX);
        }

        @Test
        @DisplayName("startOfWeek() 返回当周开始（周一）")
        void testStartOfWeek() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 14, 30, 45); // 周六

            LocalDateTime result = TemporalUtil.startOfWeek(dateTime);

            assertThat(result.toLocalDate()).isEqualTo(LocalDate.of(2024, 6, 10)); // 周一
            assertThat(result.toLocalTime()).isEqualTo(LocalTime.MIDNIGHT);
        }

        @Test
        @DisplayName("endOfWeek() 返回当周结束（周日）")
        void testEndOfWeek() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 14, 30, 45); // 周六

            LocalDateTime result = TemporalUtil.endOfWeek(dateTime);

            assertThat(result.toLocalDate()).isEqualTo(LocalDate.of(2024, 6, 16)); // 周日
            assertThat(result.toLocalTime()).isEqualTo(LocalTime.MAX);
        }
    }

    @Nested
    @DisplayName("周期计算测试")
    class PeriodCalculationTests {

        @Test
        @DisplayName("daysBetween() 计算天数差")
        void testDaysBetween() {
            LocalDate start = LocalDate.of(2024, 6, 1);
            LocalDate end = LocalDate.of(2024, 6, 15);

            long result = TemporalUtil.daysBetween(start, end);

            assertThat(result).isEqualTo(14);
        }

        @Test
        @DisplayName("daysBetween() 负数差异")
        void testDaysBetweenNegative() {
            LocalDate start = LocalDate.of(2024, 6, 15);
            LocalDate end = LocalDate.of(2024, 6, 1);

            long result = TemporalUtil.daysBetween(start, end);

            assertThat(result).isEqualTo(-14);
        }

        @Test
        @DisplayName("monthsBetween() 计算月数差")
        void testMonthsBetween() {
            LocalDate start = LocalDate.of(2024, 1, 15);
            LocalDate end = LocalDate.of(2024, 6, 15);

            long result = TemporalUtil.monthsBetween(start, end);

            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("yearsBetween() 计算年数差")
        void testYearsBetween() {
            LocalDate start = LocalDate.of(2020, 6, 15);
            LocalDate end = LocalDate.of(2024, 6, 15);

            long result = TemporalUtil.yearsBetween(start, end);

            assertThat(result).isEqualTo(4);
        }

        @Test
        @DisplayName("hoursBetween() 计算小时差")
        void testHoursBetween() {
            LocalDateTime start = LocalDateTime.of(2024, 6, 15, 10, 0, 0);
            LocalDateTime end = LocalDateTime.of(2024, 6, 15, 14, 0, 0);

            long result = TemporalUtil.hoursBetween(start, end);

            assertThat(result).isEqualTo(4);
        }

        @Test
        @DisplayName("minutesBetween() 计算分钟差")
        void testMinutesBetween() {
            LocalDateTime start = LocalDateTime.of(2024, 6, 15, 10, 0, 0);
            LocalDateTime end = LocalDateTime.of(2024, 6, 15, 10, 30, 0);

            long result = TemporalUtil.minutesBetween(start, end);

            assertThat(result).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("星期几方法测试")
    class DayOfWeekTests {

        @Test
        @DisplayName("isWeekend() 检查是否为周末")
        void testIsWeekend() {
            LocalDate saturday = LocalDate.of(2024, 6, 15); // 周六
            LocalDate sunday = LocalDate.of(2024, 6, 16); // 周日
            LocalDate monday = LocalDate.of(2024, 6, 17); // 周一

            assertThat(TemporalUtil.isWeekend(saturday)).isTrue();
            assertThat(TemporalUtil.isWeekend(sunday)).isTrue();
            assertThat(TemporalUtil.isWeekend(monday)).isFalse();
        }

        @Test
        @DisplayName("isWeekday() 检查是否为工作日")
        void testIsWeekday() {
            LocalDate saturday = LocalDate.of(2024, 6, 15); // 周六
            LocalDate monday = LocalDate.of(2024, 6, 17); // 周一

            assertThat(TemporalUtil.isWeekday(saturday)).isFalse();
            assertThat(TemporalUtil.isWeekday(monday)).isTrue();
        }
    }

    @Nested
    @DisplayName("闰年方法测试")
    class LeapYearTests {

        @Test
        @DisplayName("isLeapYear(int) 检查年份是否为闰年")
        void testIsLeapYearInt() {
            assertThat(TemporalUtil.isLeapYear(2024)).isTrue();
            assertThat(TemporalUtil.isLeapYear(2023)).isFalse();
            assertThat(TemporalUtil.isLeapYear(2000)).isTrue();
            assertThat(TemporalUtil.isLeapYear(1900)).isFalse();
        }

        @Test
        @DisplayName("isLeapYear(LocalDate) 检查日期是否在闰年")
        void testIsLeapYearLocalDate() {
            LocalDate leapYear = LocalDate.of(2024, 6, 15);
            LocalDate nonLeapYear = LocalDate.of(2023, 6, 15);

            assertThat(TemporalUtil.isLeapYear(leapYear)).isTrue();
            assertThat(TemporalUtil.isLeapYear(nonLeapYear)).isFalse();
        }
    }

    @Nested
    @DisplayName("月份方法测试")
    class MonthTests {

        @Test
        @DisplayName("daysInMonth(int, int) 返回月份天数")
        void testDaysInMonthIntInt() {
            assertThat(TemporalUtil.daysInMonth(2024, 2)).isEqualTo(29); // 闰年
            assertThat(TemporalUtil.daysInMonth(2023, 2)).isEqualTo(28); // 非闰年
            assertThat(TemporalUtil.daysInMonth(2024, 1)).isEqualTo(31);
            assertThat(TemporalUtil.daysInMonth(2024, 4)).isEqualTo(30);
        }

        @Test
        @DisplayName("daysInMonth(YearMonth) 返回月份天数")
        void testDaysInMonthYearMonth() {
            YearMonth feb2024 = YearMonth.of(2024, 2);
            YearMonth feb2023 = YearMonth.of(2023, 2);

            assertThat(TemporalUtil.daysInMonth(feb2024)).isEqualTo(29);
            assertThat(TemporalUtil.daysInMonth(feb2023)).isEqualTo(28);
        }
    }

    @Nested
    @DisplayName("季度方法测试")
    class QuarterTests {

        @Test
        @DisplayName("getQuarter(int) 返回月份对应的季度")
        void testGetQuarterInt() {
            assertThat(TemporalUtil.getQuarter(1)).isEqualTo(1);
            assertThat(TemporalUtil.getQuarter(3)).isEqualTo(1);
            assertThat(TemporalUtil.getQuarter(4)).isEqualTo(2);
            assertThat(TemporalUtil.getQuarter(6)).isEqualTo(2);
            assertThat(TemporalUtil.getQuarter(7)).isEqualTo(3);
            assertThat(TemporalUtil.getQuarter(9)).isEqualTo(3);
            assertThat(TemporalUtil.getQuarter(10)).isEqualTo(4);
            assertThat(TemporalUtil.getQuarter(12)).isEqualTo(4);
        }

        @Test
        @DisplayName("getQuarter(int) 无效月份抛出异常")
        void testGetQuarterIntInvalid() {
            assertThatThrownBy(() -> TemporalUtil.getQuarter(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> TemporalUtil.getQuarter(13))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("getQuarter(LocalDate) 返回日期对应的季度")
        void testGetQuarterLocalDate() {
            assertThat(TemporalUtil.getQuarter(LocalDate.of(2024, 1, 15))).isEqualTo(1);
            assertThat(TemporalUtil.getQuarter(LocalDate.of(2024, 6, 15))).isEqualTo(2);
            assertThat(TemporalUtil.getQuarter(LocalDate.of(2024, 9, 15))).isEqualTo(3);
            assertThat(TemporalUtil.getQuarter(LocalDate.of(2024, 12, 15))).isEqualTo(4);
        }
    }
}
