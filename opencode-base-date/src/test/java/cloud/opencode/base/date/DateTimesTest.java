package cloud.opencode.base.date;

import cloud.opencode.base.date.extra.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

/**
 * DateTimes 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("DateTimes 测试")
class DateTimesTest {

    @Nested
    @DisplayName("LocalDate工厂方法测试")
    class LocalDateFactoryTests {

        @Test
        @DisplayName("date(year, month, day) 创建LocalDate")
        void testDateIntIntInt() {
            LocalDate result = DateTimes.date(2024, 1, 15);
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("date(year, Month, day) 使用Month枚举创建LocalDate")
        void testDateIntMonthInt() {
            LocalDate result = DateTimes.date(2024, Month.JANUARY, 15);
            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("today() 返回今天日期")
        void testToday() {
            LocalDate result = DateTimes.today();
            assertThat(result).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("yesterday() 返回昨天日期")
        void testYesterday() {
            LocalDate result = DateTimes.yesterday();
            assertThat(result).isEqualTo(LocalDate.now().minusDays(1));
        }

        @Test
        @DisplayName("tomorrow() 返回明天日期")
        void testTomorrow() {
            LocalDate result = DateTimes.tomorrow();
            assertThat(result).isEqualTo(LocalDate.now().plusDays(1));
        }
    }

    @Nested
    @DisplayName("LocalTime工厂方法测试")
    class LocalTimeFactoryTests {

        @Test
        @DisplayName("time(hour, minute) 创建LocalTime")
        void testTimeHourMinute() {
            LocalTime result = DateTimes.time(10, 30);
            assertThat(result).isEqualTo(LocalTime.of(10, 30));
        }

        @Test
        @DisplayName("time(hour, minute, second) 创建带秒的LocalTime")
        void testTimeHourMinuteSecond() {
            LocalTime result = DateTimes.time(10, 30, 45);
            assertThat(result).isEqualTo(LocalTime.of(10, 30, 45));
        }

        @Test
        @DisplayName("midnight() 返回午夜")
        void testMidnight() {
            LocalTime result = DateTimes.midnight();
            assertThat(result).isEqualTo(LocalTime.MIDNIGHT);
            assertThat(result.getHour()).isEqualTo(0);
            assertThat(result.getMinute()).isEqualTo(0);
        }

        @Test
        @DisplayName("noon() 返回中午")
        void testNoon() {
            LocalTime result = DateTimes.noon();
            assertThat(result).isEqualTo(LocalTime.NOON);
            assertThat(result.getHour()).isEqualTo(12);
            assertThat(result.getMinute()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("LocalDateTime工厂方法测试")
    class LocalDateTimeFactoryTests {

        @Test
        @DisplayName("dateTime(year, month, day, hour, minute) 创建LocalDateTime")
        void testDateTimeFiveParams() {
            LocalDateTime result = DateTimes.dateTime(2024, 1, 15, 10, 30);
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30));
        }

        @Test
        @DisplayName("dateTime(year, month, day, hour, minute, second) 创建带秒的LocalDateTime")
        void testDateTimeSixParams() {
            LocalDateTime result = DateTimes.dateTime(2024, 1, 15, 10, 30, 45);
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 45));
        }

        @Test
        @DisplayName("dateTime(LocalDate, LocalTime) 从日期和时间创建LocalDateTime")
        void testDateTimeFromDateAndTime() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            LocalTime time = LocalTime.of(10, 30, 45);
            LocalDateTime result = DateTimes.dateTime(date, time);
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 45));
        }

        @Test
        @DisplayName("now() 返回当前日期时间")
        void testNow() {
            LocalDateTime result = DateTimes.now();
            LocalDateTime expected = LocalDateTime.now();
            assertThat(result.getYear()).isEqualTo(expected.getYear());
            assertThat(result.getMonth()).isEqualTo(expected.getMonth());
            assertThat(result.getDayOfMonth()).isEqualTo(expected.getDayOfMonth());
            assertThat(result.getHour()).isEqualTo(expected.getHour());
        }
    }

    @Nested
    @DisplayName("YearMonth和Year工厂方法测试")
    class YearMonthYearFactoryTests {

        @Test
        @DisplayName("yearMonth(year, month) 创建YearMonth")
        void testYearMonth() {
            YearMonth result = DateTimes.yearMonth(2024, 6);
            assertThat(result).isEqualTo(YearMonth.of(2024, 6));
        }

        @Test
        @DisplayName("year(year) 创建Year")
        void testYear() {
            Year result = DateTimes.year(2024);
            assertThat(result).isEqualTo(Year.of(2024));
        }

        @Test
        @DisplayName("currentYearMonth() 返回当前YearMonth")
        void testCurrentYearMonth() {
            YearMonth result = DateTimes.currentYearMonth();
            assertThat(result).isEqualTo(YearMonth.now());
        }

        @Test
        @DisplayName("currentYear() 返回当前Year")
        void testCurrentYear() {
            Year result = DateTimes.currentYear();
            assertThat(result).isEqualTo(Year.now());
        }
    }

    @Nested
    @DisplayName("扩展类型工厂方法测试")
    class ExtendedTypeFactoryTests {

        @Test
        @DisplayName("quarter(int) 创建Quarter")
        void testQuarter() {
            Quarter result = DateTimes.quarter(1);
            assertThat(result).isEqualTo(Quarter.Q1);

            assertThat(DateTimes.quarter(2)).isEqualTo(Quarter.Q2);
            assertThat(DateTimes.quarter(3)).isEqualTo(Quarter.Q3);
            assertThat(DateTimes.quarter(4)).isEqualTo(Quarter.Q4);
        }

        @Test
        @DisplayName("yearQuarter(year, quarter) 创建YearQuarter")
        void testYearQuarterInt() {
            YearQuarter result = DateTimes.yearQuarter(2024, 1);
            assertThat(result.getYear()).isEqualTo(2024);
            assertThat(result.getQuarter()).isEqualTo(Quarter.Q1);
        }

        @Test
        @DisplayName("yearQuarter(year, Quarter) 使用Quarter枚举创建YearQuarter")
        void testYearQuarterEnum() {
            YearQuarter result = DateTimes.yearQuarter(2024, Quarter.Q2);
            assertThat(result.getYear()).isEqualTo(2024);
            assertThat(result.getQuarter()).isEqualTo(Quarter.Q2);
        }

        @Test
        @DisplayName("currentQuarter() 返回当前YearQuarter")
        void testCurrentQuarter() {
            YearQuarter result = DateTimes.currentQuarter();
            assertThat(result).isEqualTo(YearQuarter.now());
        }

        @Test
        @DisplayName("yearWeek(weekBasedYear, week) 创建YearWeek")
        void testYearWeek() {
            YearWeek result = DateTimes.yearWeek(2024, 1);
            assertThat(result.getYear()).isEqualTo(2024);
            assertThat(result.getWeek()).isEqualTo(1);
        }

        @Test
        @DisplayName("currentWeek() 返回当前YearWeek")
        void testCurrentWeek() {
            YearWeek result = DateTimes.currentWeek();
            assertThat(result).isEqualTo(YearWeek.now());
        }
    }

    @Nested
    @DisplayName("Instant工厂方法测试")
    class InstantFactoryTests {

        @Test
        @DisplayName("instant() 返回当前Instant")
        void testInstant() {
            Instant result = DateTimes.instant();
            assertThat(result).isNotNull();
            assertThat(result.getEpochSecond()).isCloseTo(Instant.now().getEpochSecond(), within(1L));
        }

        @Test
        @DisplayName("instant(epochMilli) 从毫秒时间戳创建Instant")
        void testInstantFromEpochMilli() {
            long epochMilli = 1705312245000L;
            Instant result = DateTimes.instant(epochMilli);
            assertThat(result).isEqualTo(Instant.ofEpochMilli(epochMilli));
        }

        @Test
        @DisplayName("instant(epochSecond, nanoAdjust) 从秒时间戳创建Instant")
        void testInstantFromEpochSecond() {
            long epochSecond = 1705312245L;
            long nanoAdjust = 500_000_000L;
            Instant result = DateTimes.instant(epochSecond, nanoAdjust);
            assertThat(result).isEqualTo(Instant.ofEpochSecond(epochSecond, nanoAdjust));
        }
    }

    @Nested
    @DisplayName("Duration工厂方法测试")
    class DurationFactoryTests {

        @Test
        @DisplayName("nanos(long) 创建纳秒Duration")
        void testNanos() {
            Duration result = DateTimes.nanos(1_000_000_000L);
            assertThat(result).isEqualTo(Duration.ofNanos(1_000_000_000L));
            assertThat(result.getSeconds()).isEqualTo(1);
        }

        @Test
        @DisplayName("millis(long) 创建毫秒Duration")
        void testMillis() {
            Duration result = DateTimes.millis(1000);
            assertThat(result).isEqualTo(Duration.ofMillis(1000));
            assertThat(result.getSeconds()).isEqualTo(1);
        }

        @Test
        @DisplayName("seconds(long) 创建秒Duration")
        void testSeconds() {
            Duration result = DateTimes.seconds(60);
            assertThat(result).isEqualTo(Duration.ofSeconds(60));
            assertThat(result.toMinutes()).isEqualTo(1);
        }

        @Test
        @DisplayName("minutes(long) 创建分钟Duration")
        void testMinutes() {
            Duration result = DateTimes.minutes(30);
            assertThat(result).isEqualTo(Duration.ofMinutes(30));
            assertThat(result.toSeconds()).isEqualTo(1800);
        }

        @Test
        @DisplayName("hours(long) 创建小时Duration")
        void testHours() {
            Duration result = DateTimes.hours(2);
            assertThat(result).isEqualTo(Duration.ofHours(2));
            assertThat(result.toMinutes()).isEqualTo(120);
        }

        @Test
        @DisplayName("days(long) 创建天数Duration")
        void testDays() {
            Duration result = DateTimes.days(7);
            assertThat(result).isEqualTo(Duration.ofDays(7));
            assertThat(result.toHours()).isEqualTo(168);
        }
    }

    @Nested
    @DisplayName("Period工厂方法测试")
    class PeriodFactoryTests {

        @Test
        @DisplayName("periodDays(int) 创建天数Period")
        void testPeriodDays() {
            Period result = DateTimes.periodDays(10);
            assertThat(result).isEqualTo(Period.ofDays(10));
            assertThat(result.getDays()).isEqualTo(10);
        }

        @Test
        @DisplayName("weeks(int) 创建周数Period")
        void testWeeks() {
            Period result = DateTimes.weeks(2);
            assertThat(result).isEqualTo(Period.ofWeeks(2));
            assertThat(result.getDays()).isEqualTo(14);
        }

        @Test
        @DisplayName("months(int) 创建月数Period")
        void testMonths() {
            Period result = DateTimes.months(3);
            assertThat(result).isEqualTo(Period.ofMonths(3));
            assertThat(result.getMonths()).isEqualTo(3);
        }

        @Test
        @DisplayName("years(int) 创建年数Period")
        void testYears() {
            Period result = DateTimes.years(5);
            assertThat(result).isEqualTo(Period.ofYears(5));
            assertThat(result.getYears()).isEqualTo(5);
        }

        @Test
        @DisplayName("period(years, months, days) 创建完整Period")
        void testPeriod() {
            Period result = DateTimes.period(1, 2, 15);
            assertThat(result).isEqualTo(Period.of(1, 2, 15));
            assertThat(result.getYears()).isEqualTo(1);
            assertThat(result.getMonths()).isEqualTo(2);
            assertThat(result.getDays()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("Range工厂方法测试")
    class RangeFactoryTests {

        @Test
        @DisplayName("dateRange(start, end) 创建LocalDateRange")
        void testDateRange() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 12, 31);
            LocalDateRange result = DateTimes.dateRange(start, end);

            assertThat(result.getStart()).isEqualTo(start);
            assertThat(result.getEnd()).isEqualTo(end);
        }

        @Test
        @DisplayName("dateTimeRange(start, end) 创建LocalDateTimeRange")
        void testDateTimeRange() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59);
            LocalDateTimeRange result = DateTimes.dateTimeRange(start, end);

            assertThat(result.getStart()).isEqualTo(start);
            assertThat(result.getEnd()).isEqualTo(end);
        }

        @Test
        @DisplayName("interval(start, end) 创建Interval")
        void testIntervalFromInstants() {
            Instant start = Instant.parse("2024-01-01T00:00:00Z");
            Instant end = Instant.parse("2024-01-01T12:00:00Z");
            Interval result = DateTimes.interval(start, end);

            assertThat(result.getStart()).isEqualTo(start);
            assertThat(result.getEnd()).isEqualTo(end);
        }

        @Test
        @DisplayName("interval(start, duration) 从开始时刻和时长创建Interval")
        void testIntervalFromInstantAndDuration() {
            Instant start = Instant.parse("2024-01-01T00:00:00Z");
            Duration duration = Duration.ofHours(12);
            Interval result = DateTimes.interval(start, duration);

            assertThat(result.getStart()).isEqualTo(start);
            assertThat(result.getEnd()).isEqualTo(start.plus(duration));
        }
    }

    @Nested
    @DisplayName("PeriodDuration工厂方法测试")
    class PeriodDurationFactoryTests {

        @Test
        @DisplayName("periodDuration(Period, Duration) 从Period和Duration创建PeriodDuration")
        void testPeriodDuration() {
            Period period = Period.of(1, 2, 15);
            Duration duration = Duration.ofHours(5);
            PeriodDuration result = DateTimes.periodDuration(period, duration);

            assertThat(result.getPeriod()).isEqualTo(period);
            assertThat(result.getDuration()).isEqualTo(duration);
        }

        @Test
        @DisplayName("between(start, end) 计算两个LocalDateTime之间的PeriodDuration")
        void testBetween() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime end = LocalDateTime.of(2024, 3, 15, 15, 30);
            PeriodDuration result = DateTimes.between(start, end);

            assertThat(result).isNotNull();
            assertThat(result.getPeriod().getMonths()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("私有构造函数测试")
    class PrivateConstructorTests {

        @Test
        @DisplayName("DateTimes是工具类，不能实例化")
        void testPrivateConstructor() throws Exception {
            var constructor = DateTimes.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).doesNotThrowAnyException();
        }
    }
}
