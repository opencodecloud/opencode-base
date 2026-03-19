package cloud.opencode.base.date;

import cloud.opencode.base.date.extra.LocalDateRange;
import cloud.opencode.base.date.extra.YearQuarter;
import cloud.opencode.base.date.extra.YearWeek;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenDate 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("OpenDate 测试")
class OpenDateTest {

    @Nested
    @DisplayName("当前时间方法测试")
    class CurrentTimeTests {

        @Test
        @DisplayName("today() 返回今天的日期")
        void testToday() {
            LocalDate today = OpenDate.today();

            assertThat(today).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("today(ZoneId) 返回指定时区的今天")
        void testTodayWithZone() {
            ZoneId zone = ZoneId.of("Asia/Shanghai");
            LocalDate today = OpenDate.today(zone);

            assertThat(today).isEqualTo(LocalDate.now(zone));
        }

        @Test
        @DisplayName("now() 返回当前日期时间")
        void testNow() {
            LocalDateTime now = OpenDate.now();

            assertThat(now).isNotNull();
            assertThat(now.toLocalDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("now(ZoneId) 返回指定时区的当前日期时间")
        void testNowWithZone() {
            ZoneId zone = ZoneId.of("Asia/Shanghai");
            LocalDateTime now = OpenDate.now(zone);

            assertThat(now).isNotNull();
        }

        @Test
        @DisplayName("currentTimeMillis() 返回当前毫秒时间戳")
        void testCurrentTimeMillis() {
            long millis = OpenDate.currentTimeMillis();

            assertThat(millis).isCloseTo(System.currentTimeMillis(), within(1000L));
        }

        @Test
        @DisplayName("currentTimeSeconds() 返回当前秒时间戳")
        void testCurrentTimeSeconds() {
            long seconds = OpenDate.currentTimeSeconds();

            assertThat(seconds).isCloseTo(Instant.now().getEpochSecond(), within(1L));
        }

        @Test
        @DisplayName("instant() 返回当前Instant")
        void testInstant() {
            Instant instant = OpenDate.instant();

            assertThat(instant).isNotNull();
            assertThat(instant.toEpochMilli()).isCloseTo(System.currentTimeMillis(), within(1000L));
        }
    }

    @Nested
    @DisplayName("创建方法测试")
    class CreationTests {

        @Test
        @DisplayName("of(year, month, day) 创建LocalDate")
        void testOfDate() {
            LocalDate date = OpenDate.of(2024, 6, 15);

            assertThat(date.getYear()).isEqualTo(2024);
            assertThat(date.getMonthValue()).isEqualTo(6);
            assertThat(date.getDayOfMonth()).isEqualTo(15);
        }

        @Test
        @DisplayName("of(year, month, day, hour, minute, second) 创建LocalDateTime")
        void testOfDateTime() {
            LocalDateTime dateTime = OpenDate.of(2024, 6, 15, 10, 30, 45);

            assertThat(dateTime.getYear()).isEqualTo(2024);
            assertThat(dateTime.getMonthValue()).isEqualTo(6);
            assertThat(dateTime.getDayOfMonth()).isEqualTo(15);
            assertThat(dateTime.getHour()).isEqualTo(10);
            assertThat(dateTime.getMinute()).isEqualTo(30);
            assertThat(dateTime.getSecond()).isEqualTo(45);
        }

        @Test
        @DisplayName("ofEpochMilli() 从毫秒时间戳创建LocalDateTime")
        void testOfEpochMilli() {
            long epochMilli = System.currentTimeMillis();
            LocalDateTime dateTime = OpenDate.ofEpochMilli(epochMilli);

            assertThat(dateTime).isNotNull();
        }

        @Test
        @DisplayName("ofEpochSecond() 从秒时间戳创建LocalDateTime")
        void testOfEpochSecond() {
            long epochSecond = Instant.now().getEpochSecond();
            LocalDateTime dateTime = OpenDate.ofEpochSecond(epochSecond);

            assertThat(dateTime).isNotNull();
        }

        @Test
        @DisplayName("from(Date) 从Date创建LocalDateTime")
        void testFromDate() {
            Date date = new Date();
            LocalDateTime dateTime = OpenDate.from(date);

            assertThat(dateTime).isNotNull();
        }

        @Test
        @DisplayName("from(Date) null参数抛出异常")
        void testFromDateNull() {
            assertThatThrownBy(() -> OpenDate.from((Date) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("from(Calendar) 从Calendar创建LocalDateTime")
        void testFromCalendar() {
            Calendar calendar = Calendar.getInstance();
            LocalDateTime dateTime = OpenDate.from(calendar);

            assertThat(dateTime).isNotNull();
        }

        @Test
        @DisplayName("from(Calendar) null参数抛出异常")
        void testFromCalendarNull() {
            assertThatThrownBy(() -> OpenDate.from((Calendar) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("解析方法测试")
    class ParseTests {

        @Test
        @DisplayName("parse() 智能解析日期时间字符串")
        void testParse() {
            LocalDateTime dateTime = OpenDate.parse("2024-06-15 10:30:45");

            assertThat(dateTime.getYear()).isEqualTo(2024);
            assertThat(dateTime.getMonthValue()).isEqualTo(6);
            assertThat(dateTime.getDayOfMonth()).isEqualTo(15);
            assertThat(dateTime.getHour()).isEqualTo(10);
        }

        @Test
        @DisplayName("parseDate() 解析日期字符串")
        void testParseDate() {
            LocalDate date = OpenDate.parseDate("2024-06-15");

            assertThat(date.getYear()).isEqualTo(2024);
            assertThat(date.getMonthValue()).isEqualTo(6);
            assertThat(date.getDayOfMonth()).isEqualTo(15);
        }

        @Test
        @DisplayName("parseTime() 解析时间字符串")
        void testParseTime() {
            LocalTime time = OpenDate.parseTime("10:30:45");

            assertThat(time.getHour()).isEqualTo(10);
            assertThat(time.getMinute()).isEqualTo(30);
            assertThat(time.getSecond()).isEqualTo(45);
        }

        @Test
        @DisplayName("parse(text, pattern) 使用指定模式解析")
        void testParseWithPattern() {
            LocalDateTime dateTime = OpenDate.parse("15/06/2024 10:30:45", "dd/MM/yyyy HH:mm:ss");

            assertThat(dateTime.getYear()).isEqualTo(2024);
            assertThat(dateTime.getDayOfMonth()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("格式化方法测试")
    class FormatTests {

        @Test
        @DisplayName("format(Temporal) 格式化为默认格式")
        void testFormat() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 45);
            String result = OpenDate.format(dateTime);

            assertThat(result).contains("2024");
            assertThat(result).contains("06");
            assertThat(result).contains("15");
        }

        @Test
        @DisplayName("format(Temporal) null参数抛出异常")
        void testFormatNull() {
            assertThatThrownBy(() -> OpenDate.format(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("format(Temporal, pattern) 格式化为指定模式")
        void testFormatWithPattern() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 45);
            String result = OpenDate.format(dateTime, "yyyy/MM/dd");

            assertThat(result).isEqualTo("2024/06/15");
        }

        @Test
        @DisplayName("format() 格式化LocalDate")
        void testFormatLocalDate() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            String result = OpenDate.format(date);

            assertThat(result).contains("2024");
            assertThat(result).contains("06");
            assertThat(result).contains("15");
        }

        @Test
        @DisplayName("format() 格式化LocalTime")
        void testFormatLocalTime() {
            LocalTime time = LocalTime.of(10, 30, 45);
            String result = OpenDate.format(time);

            assertThat(result).contains("10");
            assertThat(result).contains("30");
            assertThat(result).contains("45");
        }

        @Test
        @DisplayName("formatIso() 格式化为ISO格式")
        void testFormatIso() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 45);
            String result = OpenDate.formatIso(dateTime);

            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("formatIso() null参数抛出异常")
        void testFormatIsoNull() {
            assertThatThrownBy(() -> OpenDate.formatIso(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("formatChinese() 格式化为中文格式")
        void testFormatChinese() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 45);
            String result = OpenDate.formatChinese(dateTime);

            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("formatChinese(LocalDate) 格式化日期为中文格式")
        void testFormatChineseLocalDate() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            String result = OpenDate.formatChinese(date);

            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("formatChinese() null参数抛出异常")
        void testFormatChineseNull() {
            assertThatThrownBy(() -> OpenDate.formatChinese(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("截断方法测试")
    class TruncationTests {

        @Test
        @DisplayName("truncateToDay() 截断到天")
        void testTruncateToDay() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 45, 123456789);
            LocalDateTime result = OpenDate.truncateToDay(dateTime);

            assertThat(result.getHour()).isEqualTo(0);
            assertThat(result.getMinute()).isEqualTo(0);
            assertThat(result.getSecond()).isEqualTo(0);
            assertThat(result.getNano()).isEqualTo(0);
        }

        @Test
        @DisplayName("truncateToDay() null参数抛出异常")
        void testTruncateToDayNull() {
            assertThatThrownBy(() -> OpenDate.truncateToDay(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("truncateToHour() 截断到小时")
        void testTruncateToHour() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 45);
            LocalDateTime result = OpenDate.truncateToHour(dateTime);

            assertThat(result.getHour()).isEqualTo(10);
            assertThat(result.getMinute()).isEqualTo(0);
            assertThat(result.getSecond()).isEqualTo(0);
        }

        @Test
        @DisplayName("truncateToHour() null参数抛出异常")
        void testTruncateToHourNull() {
            assertThatThrownBy(() -> OpenDate.truncateToHour(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("truncateToMinute() 截断到分钟")
        void testTruncateToMinute() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 45);
            LocalDateTime result = OpenDate.truncateToMinute(dateTime);

            assertThat(result.getMinute()).isEqualTo(30);
            assertThat(result.getSecond()).isEqualTo(0);
        }

        @Test
        @DisplayName("truncateToMinute() null参数抛出异常")
        void testTruncateToMinuteNull() {
            assertThatThrownBy(() -> OpenDate.truncateToMinute(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("truncate() 通用截断方法")
        void testTruncate() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 45, 123456789);

            // truncate() 方法将纳秒设置为0
            LocalDateTime result = OpenDate.truncate(dateTime, java.time.temporal.ChronoUnit.SECONDS);
            assertThat(result.getNano()).isEqualTo(0);
            assertThat(result.getSecond()).isEqualTo(45);
            assertThat(result.getMinute()).isEqualTo(30);
            assertThat(result.getHour()).isEqualTo(10);
        }

        @Test
        @DisplayName("truncate() null参数抛出异常")
        void testTruncateNull() {
            assertThatThrownBy(() -> OpenDate.truncate(null, java.time.temporal.ChronoUnit.DAYS))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> OpenDate.truncate(LocalDateTime.now(), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("加减方法测试")
    class AddSubtractTests {

        @Test
        @DisplayName("plusYears() 添加年")
        void testPlusYears() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
            LocalDateTime result = OpenDate.plusYears(dateTime, 2);

            assertThat(result.getYear()).isEqualTo(2026);
        }

        @Test
        @DisplayName("plusYears() null参数抛出异常")
        void testPlusYearsNull() {
            assertThatThrownBy(() -> OpenDate.plusYears(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("plusMonths() 添加月")
        void testPlusMonths() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
            LocalDateTime result = OpenDate.plusMonths(dateTime, 3);

            assertThat(result.getMonthValue()).isEqualTo(9);
        }

        @Test
        @DisplayName("plusMonths() null参数抛出异常")
        void testPlusMonthsNull() {
            assertThatThrownBy(() -> OpenDate.plusMonths(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("plusWeeks() 添加周")
        void testPlusWeeks() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
            LocalDateTime result = OpenDate.plusWeeks(dateTime, 2);

            assertThat(result.getDayOfMonth()).isEqualTo(29);
        }

        @Test
        @DisplayName("plusWeeks() null参数抛出异常")
        void testPlusWeeksNull() {
            assertThatThrownBy(() -> OpenDate.plusWeeks(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("plusDays() 添加天")
        void testPlusDays() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
            LocalDateTime result = OpenDate.plusDays(dateTime, 10);

            assertThat(result.getDayOfMonth()).isEqualTo(25);
        }

        @Test
        @DisplayName("plusDays() null参数抛出异常")
        void testPlusDaysNull() {
            assertThatThrownBy(() -> OpenDate.plusDays(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("plusHours() 添加小时")
        void testPlusHours() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
            LocalDateTime result = OpenDate.plusHours(dateTime, 5);

            assertThat(result.getHour()).isEqualTo(15);
        }

        @Test
        @DisplayName("plusHours() null参数抛出异常")
        void testPlusHoursNull() {
            assertThatThrownBy(() -> OpenDate.plusHours(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("plusMinutes() 添加分钟")
        void testPlusMinutes() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
            LocalDateTime result = OpenDate.plusMinutes(dateTime, 45);

            assertThat(result.getHour()).isEqualTo(11);
            assertThat(result.getMinute()).isEqualTo(15);
        }

        @Test
        @DisplayName("plusMinutes() null参数抛出异常")
        void testPlusMinutesNull() {
            assertThatThrownBy(() -> OpenDate.plusMinutes(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("plusSeconds() 添加秒")
        void testPlusSeconds() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
            LocalDateTime result = OpenDate.plusSeconds(dateTime, 90);

            assertThat(result.getMinute()).isEqualTo(31);
            assertThat(result.getSecond()).isEqualTo(30);
        }

        @Test
        @DisplayName("plusSeconds() null参数抛出异常")
        void testPlusSecondsNull() {
            assertThatThrownBy(() -> OpenDate.plusSeconds(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("边界方法测试")
    class BoundaryTests {

        @Test
        @DisplayName("startOfMonth() 返回月份开始")
        void testStartOfMonth() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate result = OpenDate.startOfMonth(date);

            assertThat(result.getDayOfMonth()).isEqualTo(1);
            assertThat(result.getMonthValue()).isEqualTo(6);
        }

        @Test
        @DisplayName("startOfMonth() null参数抛出异常")
        void testStartOfMonthNull() {
            assertThatThrownBy(() -> OpenDate.startOfMonth(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("endOfMonth() 返回月份结束")
        void testEndOfMonth() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate result = OpenDate.endOfMonth(date);

            assertThat(result.getDayOfMonth()).isEqualTo(30);
            assertThat(result.getMonthValue()).isEqualTo(6);
        }

        @Test
        @DisplayName("endOfMonth() null参数抛出异常")
        void testEndOfMonthNull() {
            assertThatThrownBy(() -> OpenDate.endOfMonth(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("startOfYear() 返回年份开始")
        void testStartOfYear() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate result = OpenDate.startOfYear(date);

            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 1));
        }

        @Test
        @DisplayName("startOfYear() null参数抛出异常")
        void testStartOfYearNull() {
            assertThatThrownBy(() -> OpenDate.startOfYear(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("endOfYear() 返回年份结束")
        void testEndOfYear() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate result = OpenDate.endOfYear(date);

            assertThat(result).isEqualTo(LocalDate.of(2024, 12, 31));
        }

        @Test
        @DisplayName("endOfYear() null参数抛出异常")
        void testEndOfYearNull() {
            assertThatThrownBy(() -> OpenDate.endOfYear(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("startOfWeek() 返回周开始（周一）")
        void testStartOfWeek() {
            LocalDate date = LocalDate.of(2024, 6, 15); // 周六
            LocalDate result = OpenDate.startOfWeek(date);

            assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 10));
        }

        @Test
        @DisplayName("startOfWeek() null参数抛出异常")
        void testStartOfWeekNull() {
            assertThatThrownBy(() -> OpenDate.startOfWeek(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("endOfWeek() 返回周结束（周日）")
        void testEndOfWeek() {
            LocalDate date = LocalDate.of(2024, 6, 15); // 周六
            LocalDate result = OpenDate.endOfWeek(date);

            assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 16));
        }

        @Test
        @DisplayName("endOfWeek() null参数抛出异常")
        void testEndOfWeekNull() {
            assertThatThrownBy(() -> OpenDate.endOfWeek(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("startOfQuarter() 返回季度开始")
        void testStartOfQuarter() {
            LocalDate date = LocalDate.of(2024, 5, 15);
            LocalDate result = OpenDate.startOfQuarter(date);

            assertThat(result).isEqualTo(LocalDate.of(2024, 4, 1));
        }

        @Test
        @DisplayName("startOfQuarter() null参数抛出异常")
        void testStartOfQuarterNull() {
            assertThatThrownBy(() -> OpenDate.startOfQuarter(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("endOfQuarter() 返回季度结束")
        void testEndOfQuarter() {
            LocalDate date = LocalDate.of(2024, 5, 15);
            LocalDate result = OpenDate.endOfQuarter(date);

            assertThat(result).isEqualTo(LocalDate.of(2024, 6, 30));
        }

        @Test
        @DisplayName("endOfQuarter() null参数抛出异常")
        void testEndOfQuarterNull() {
            assertThatThrownBy(() -> OpenDate.endOfQuarter(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("比较方法测试")
    class ComparisonTests {

        @Test
        @DisplayName("isBetween(LocalDate) 判断日期是否在范围内")
        void testIsBetweenLocalDate() {
            LocalDate start = LocalDate.of(2024, 6, 1);
            LocalDate end = LocalDate.of(2024, 6, 30);
            LocalDate middle = LocalDate.of(2024, 6, 15);
            LocalDate outside = LocalDate.of(2024, 7, 1);

            assertThat(OpenDate.isBetween(middle, start, end)).isTrue();
            assertThat(OpenDate.isBetween(outside, start, end)).isFalse();
        }

        @Test
        @DisplayName("isBetween(LocalDate) null参数抛出异常")
        void testIsBetweenLocalDateNull() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            LocalDate start = LocalDate.of(2024, 6, 1);
            LocalDate end = LocalDate.of(2024, 6, 30);

            assertThatThrownBy(() -> OpenDate.isBetween(null, start, end))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("isBetween(LocalDateTime) 判断日期时间是否在范围内")
        void testIsBetweenLocalDateTime() {
            LocalDateTime start = LocalDateTime.of(2024, 6, 15, 0, 0, 0);
            LocalDateTime end = LocalDateTime.of(2024, 6, 15, 23, 59, 59);
            LocalDateTime middle = LocalDateTime.of(2024, 6, 15, 12, 0, 0);

            assertThat(OpenDate.isBetween(middle, start, end)).isTrue();
        }

        @Test
        @DisplayName("isBetween(LocalDateTime) null参数抛出异常")
        void testIsBetweenLocalDateTimeNull() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 12, 0, 0);
            LocalDateTime start = LocalDateTime.of(2024, 6, 15, 0, 0, 0);
            LocalDateTime end = LocalDateTime.of(2024, 6, 15, 23, 59, 59);

            assertThatThrownBy(() -> OpenDate.isBetween(null, start, end))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("isToday() 判断是否为今天")
        void testIsToday() {
            LocalDate today = LocalDate.now();
            LocalDate yesterday = LocalDate.now().minusDays(1);

            assertThat(OpenDate.isToday(today)).isTrue();
            assertThat(OpenDate.isToday(yesterday)).isFalse();
        }

        @Test
        @DisplayName("isToday() null参数抛出异常")
        void testIsTodayNull() {
            assertThatThrownBy(() -> OpenDate.isToday(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("isYesterday() 判断是否为昨天")
        void testIsYesterday() {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDate today = LocalDate.now();

            assertThat(OpenDate.isYesterday(yesterday)).isTrue();
            assertThat(OpenDate.isYesterday(today)).isFalse();
        }

        @Test
        @DisplayName("isYesterday() null参数抛出异常")
        void testIsYesterdayNull() {
            assertThatThrownBy(() -> OpenDate.isYesterday(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("isTomorrow() 判断是否为明天")
        void testIsTomorrow() {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            LocalDate today = LocalDate.now();

            assertThat(OpenDate.isTomorrow(tomorrow)).isTrue();
            assertThat(OpenDate.isTomorrow(today)).isFalse();
        }

        @Test
        @DisplayName("isTomorrow() null参数抛出异常")
        void testIsTomorrowNull() {
            assertThatThrownBy(() -> OpenDate.isTomorrow(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("isWeekend() 判断是否为周末")
        void testIsWeekend() {
            LocalDate saturday = LocalDate.of(2024, 6, 15); // 周六
            LocalDate monday = LocalDate.of(2024, 6, 17); // 周一

            assertThat(OpenDate.isWeekend(saturday)).isTrue();
            assertThat(OpenDate.isWeekend(monday)).isFalse();
        }

        @Test
        @DisplayName("isWeekend() null参数抛出异常")
        void testIsWeekendNull() {
            assertThatThrownBy(() -> OpenDate.isWeekend(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("isLeapYear() 判断是否为闰年")
        void testIsLeapYear() {
            assertThat(OpenDate.isLeapYear(2024)).isTrue();
            assertThat(OpenDate.isLeapYear(2023)).isFalse();
            assertThat(OpenDate.isLeapYear(2000)).isTrue();
            assertThat(OpenDate.isLeapYear(1900)).isFalse();
        }
    }

    @Nested
    @DisplayName("时长计算测试")
    class DurationTests {

        @Test
        @DisplayName("daysBetween() 计算天数差")
        void testDaysBetween() {
            LocalDate start = LocalDate.of(2024, 6, 1);
            LocalDate end = LocalDate.of(2024, 6, 15);

            assertThat(OpenDate.daysBetween(start, end)).isEqualTo(14);
        }

        @Test
        @DisplayName("daysBetween() null参数抛出异常")
        void testDaysBetweenNull() {
            LocalDate date = LocalDate.of(2024, 6, 15);

            assertThatThrownBy(() -> OpenDate.daysBetween(null, date))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("monthsBetween() 计算月数差")
        void testMonthsBetween() {
            LocalDate start = LocalDate.of(2024, 1, 15);
            LocalDate end = LocalDate.of(2024, 6, 15);

            assertThat(OpenDate.monthsBetween(start, end)).isEqualTo(5);
        }

        @Test
        @DisplayName("monthsBetween() null参数抛出异常")
        void testMonthsBetweenNull() {
            LocalDate date = LocalDate.of(2024, 6, 15);

            assertThatThrownBy(() -> OpenDate.monthsBetween(null, date))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("yearsBetween() 计算年数差")
        void testYearsBetween() {
            LocalDate start = LocalDate.of(2020, 6, 15);
            LocalDate end = LocalDate.of(2024, 6, 15);

            assertThat(OpenDate.yearsBetween(start, end)).isEqualTo(4);
        }

        @Test
        @DisplayName("yearsBetween() null参数抛出异常")
        void testYearsBetweenNull() {
            LocalDate date = LocalDate.of(2024, 6, 15);

            assertThatThrownBy(() -> OpenDate.yearsBetween(null, date))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("periodBetween() 返回Period")
        void testPeriodBetween() {
            LocalDate start = LocalDate.of(2024, 1, 15);
            LocalDate end = LocalDate.of(2024, 6, 20);

            Period period = OpenDate.periodBetween(start, end);

            assertThat(period.getMonths()).isEqualTo(5);
            assertThat(period.getDays()).isEqualTo(5);
        }

        @Test
        @DisplayName("periodBetween() null参数抛出异常")
        void testPeriodBetweenNull() {
            LocalDate date = LocalDate.of(2024, 6, 15);

            assertThatThrownBy(() -> OpenDate.periodBetween(null, date))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("durationBetween() 返回Duration")
        void testDurationBetween() {
            LocalDateTime start = LocalDateTime.of(2024, 6, 15, 10, 0, 0);
            LocalDateTime end = LocalDateTime.of(2024, 6, 15, 14, 30, 0);

            Duration duration = OpenDate.durationBetween(start, end);

            assertThat(duration.toHours()).isEqualTo(4);
            assertThat(duration.toMinutes()).isEqualTo(270);
        }

        @Test
        @DisplayName("durationBetween() null参数抛出异常")
        void testDurationBetweenNull() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 12, 0, 0);

            assertThatThrownBy(() -> OpenDate.durationBetween(null, dateTime))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("转换方法测试")
    class ConversionTests {

        @Test
        @DisplayName("toEpochMilli() 转换为毫秒时间戳")
        void testToEpochMilli() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 12, 0, 0);

            long result = OpenDate.toEpochMilli(dateTime);

            assertThat(result).isPositive();
        }

        @Test
        @DisplayName("toEpochMilli() null参数抛出异常")
        void testToEpochMilliNull() {
            assertThatThrownBy(() -> OpenDate.toEpochMilli(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("toEpochSecond() 转换为秒时间戳")
        void testToEpochSecond() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 12, 0, 0);

            long result = OpenDate.toEpochSecond(dateTime);

            assertThat(result).isPositive();
        }

        @Test
        @DisplayName("toEpochSecond() null参数抛出异常")
        void testToEpochSecondNull() {
            assertThatThrownBy(() -> OpenDate.toEpochSecond(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("toDate(LocalDateTime) 转换为Date")
        void testToDateLocalDateTime() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 6, 15, 12, 0, 0);

            Date result = OpenDate.toDate(dateTime);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("toDate(LocalDateTime) null参数抛出异常")
        void testToDateLocalDateTimeNull() {
            assertThatThrownBy(() -> OpenDate.toDate((LocalDateTime) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("toDate(LocalDate) 转换为Date")
        void testToDateLocalDate() {
            LocalDate date = LocalDate.of(2024, 6, 15);

            Date result = OpenDate.toDate(date);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("toDate(LocalDate) null参数抛出异常")
        void testToDateLocalDateNull() {
            assertThatThrownBy(() -> OpenDate.toDate((LocalDate) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("范围和扩展类型测试")
    class RangeAndExtensionTests {

        @Test
        @DisplayName("range() 创建LocalDateRange")
        void testRange() {
            LocalDate start = LocalDate.of(2024, 6, 1);
            LocalDate end = LocalDate.of(2024, 6, 30);

            LocalDateRange range = OpenDate.range(start, end);

            assertThat(range).isNotNull();
        }

        @Test
        @DisplayName("currentQuarter() 返回当前YearQuarter")
        void testCurrentQuarter() {
            YearQuarter quarter = OpenDate.currentQuarter();

            assertThat(quarter).isNotNull();
            assertThat(quarter.getYear()).isEqualTo(LocalDate.now().getYear());
        }

        @Test
        @DisplayName("currentWeek() 返回当前YearWeek")
        void testCurrentWeek() {
            YearWeek week = OpenDate.currentWeek();

            assertThat(week).isNotNull();
            assertThat(week.getYear()).isGreaterThanOrEqualTo(LocalDate.now().getYear() - 1);
        }
    }
}
