package cloud.opencode.base.date;

import cloud.opencode.base.date.extra.Interval;
import cloud.opencode.base.date.extra.LocalDateRange;
import cloud.opencode.base.date.extra.LocalDateTimeRange;
import cloud.opencode.base.date.extra.PeriodDuration;
import cloud.opencode.base.date.extra.Quarter;
import cloud.opencode.base.date.extra.YearQuarter;
import cloud.opencode.base.date.extra.YearWeek;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Static Factory for Date and Time Types
 * 日期时间类型的静态工厂
 *
 * <p>This class provides convenient static factory methods for creating various date/time
 * types. It serves as a unified entry point for creating instances of standard java.time
 * types as well as extended types from this module.</p>
 * <p>此类提供方便的静态工厂方法来创建各种日期时间类型。
 * 它作为创建标准java.time类型以及本模块扩展类型实例的统一入口点。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Create standard java.time types - 创建标准java.time类型</li>
 *   <li>Create extended types (YearQuarter, YearWeek, Interval) - 创建扩展类型</li>
 *   <li>Create durations and periods - 创建时长和周期</li>
 *   <li>Create ranges - 创建范围</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create dates
 * LocalDate date = DateTimes.date(2024, 1, 15);
 * LocalTime time = DateTimes.time(14, 30, 0);
 * LocalDateTime dateTime = DateTimes.dateTime(2024, 1, 15, 14, 30, 0);
 *
 * // Create extended types
 * YearQuarter yq = DateTimes.yearQuarter(2024, 1);
 * YearWeek yw = DateTimes.yearWeek(2024, 1);
 *
 * // Create durations
 * Duration d1 = DateTimes.hours(2);
 * Duration d2 = DateTimes.minutes(30);
 * Period p = DateTimes.months(3);
 *
 * // Create ranges
 * LocalDateRange range = DateTimes.dateRange(
 *     LocalDate.of(2024, 1, 1),
 *     LocalDate.of(2024, 12, 31)
 * );
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>All methods are static and lightweight - 所有方法都是静态和轻量级的</li>
 *   <li>No caching overhead - 无缓存开销</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable results: Yes - 不可变结果: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see OpenDate
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class DateTimes {

    private DateTimes() {
        // Utility class
    }

    // ==================== LocalDate Factory | LocalDate工厂 ====================

    /**
     * Creates a LocalDate
     * 创建LocalDate
     *
     * @param year       the year | 年
     * @param month      the month | 月
     * @param dayOfMonth the day | 日
     * @return the LocalDate | LocalDate
     */
    public static LocalDate date(int year, int month, int dayOfMonth) {
        return LocalDate.of(year, month, dayOfMonth);
    }

    /**
     * Creates a LocalDate using Month enum
     * 使用Month枚举创建LocalDate
     *
     * @param year       the year | 年
     * @param month      the month | 月
     * @param dayOfMonth the day | 日
     * @return the LocalDate | LocalDate
     */
    public static LocalDate date(int year, Month month, int dayOfMonth) {
        return LocalDate.of(year, month, dayOfMonth);
    }

    /**
     * Gets today's date
     * 获取今天的日期
     *
     * @return today | 今天
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * Gets yesterday's date
     * 获取昨天的日期
     *
     * @return yesterday | 昨天
     */
    public static LocalDate yesterday() {
        return LocalDate.now().minusDays(1);
    }

    /**
     * Gets tomorrow's date
     * 获取明天的日期
     *
     * @return tomorrow | 明天
     */
    public static LocalDate tomorrow() {
        return LocalDate.now().plusDays(1);
    }

    // ==================== LocalTime Factory | LocalTime工厂 ====================

    /**
     * Creates a LocalTime
     * 创建LocalTime
     *
     * @param hour   the hour | 时
     * @param minute the minute | 分
     * @return the LocalTime | LocalTime
     */
    public static LocalTime time(int hour, int minute) {
        return LocalTime.of(hour, minute);
    }

    /**
     * Creates a LocalTime with seconds
     * 创建带秒的LocalTime
     *
     * @param hour   the hour | 时
     * @param minute the minute | 分
     * @param second the second | 秒
     * @return the LocalTime | LocalTime
     */
    public static LocalTime time(int hour, int minute, int second) {
        return LocalTime.of(hour, minute, second);
    }

    /**
     * Gets midnight (00:00:00)
     * 获取午夜（00:00:00）
     *
     * @return midnight | 午夜
     */
    public static LocalTime midnight() {
        return LocalTime.MIDNIGHT;
    }

    /**
     * Gets noon (12:00:00)
     * 获取中午（12:00:00）
     *
     * @return noon | 中午
     */
    public static LocalTime noon() {
        return LocalTime.NOON;
    }

    // ==================== LocalDateTime Factory | LocalDateTime工厂 ====================

    /**
     * Creates a LocalDateTime
     * 创建LocalDateTime
     *
     * @param year       the year | 年
     * @param month      the month | 月
     * @param dayOfMonth the day | 日
     * @param hour       the hour | 时
     * @param minute     the minute | 分
     * @return the LocalDateTime | LocalDateTime
     */
    public static LocalDateTime dateTime(int year, int month, int dayOfMonth, int hour, int minute) {
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute);
    }

    /**
     * Creates a LocalDateTime with seconds
     * 创建带秒的LocalDateTime
     *
     * @param year       the year | 年
     * @param month      the month | 月
     * @param dayOfMonth the day | 日
     * @param hour       the hour | 时
     * @param minute     the minute | 分
     * @param second     the second | 秒
     * @return the LocalDateTime | LocalDateTime
     */
    public static LocalDateTime dateTime(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
    }

    /**
     * Creates a LocalDateTime from date and time
     * 从日期和时间创建LocalDateTime
     *
     * @param date the date | 日期
     * @param time the time | 时间
     * @return the LocalDateTime | LocalDateTime
     */
    public static LocalDateTime dateTime(LocalDate date, LocalTime time) {
        return LocalDateTime.of(date, time);
    }

    /**
     * Gets the current datetime
     * 获取当前日期时间
     *
     * @return now | 现在
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    // ==================== YearMonth and Year Factory | 年月和年工厂 ====================

    /**
     * Creates a YearMonth
     * 创建YearMonth
     *
     * @param year  the year | 年
     * @param month the month | 月
     * @return the YearMonth | YearMonth
     */
    public static YearMonth yearMonth(int year, int month) {
        return YearMonth.of(year, month);
    }

    /**
     * Creates a Year
     * 创建Year
     *
     * @param year the year | 年
     * @return the Year | Year
     */
    public static Year year(int year) {
        return Year.of(year);
    }

    /**
     * Gets the current YearMonth
     * 获取当前YearMonth
     *
     * @return the current YearMonth | 当前YearMonth
     */
    public static YearMonth currentYearMonth() {
        return YearMonth.now();
    }

    /**
     * Gets the current Year
     * 获取当前Year
     *
     * @return the current Year | 当前Year
     */
    public static Year currentYear() {
        return Year.now();
    }

    // ==================== Extended Types Factory | 扩展类型工厂 ====================

    /**
     * Creates a Quarter
     * 创建Quarter
     *
     * @param quarter the quarter value (1-4) | 季度值（1-4）
     * @return the Quarter | Quarter
     */
    public static Quarter quarter(int quarter) {
        return Quarter.of(quarter);
    }

    /**
     * Creates a YearQuarter
     * 创建YearQuarter
     *
     * @param year    the year | 年
     * @param quarter the quarter (1-4) | 季度（1-4）
     * @return the YearQuarter | YearQuarter
     */
    public static YearQuarter yearQuarter(int year, int quarter) {
        return YearQuarter.of(year, quarter);
    }

    /**
     * Creates a YearQuarter with Quarter enum
     * 使用Quarter枚举创建YearQuarter
     *
     * @param year    the year | 年
     * @param quarter the Quarter | Quarter枚举
     * @return the YearQuarter | YearQuarter
     */
    public static YearQuarter yearQuarter(int year, Quarter quarter) {
        return YearQuarter.of(year, quarter);
    }

    /**
     * Gets the current YearQuarter
     * 获取当前YearQuarter
     *
     * @return the current YearQuarter | 当前YearQuarter
     */
    public static YearQuarter currentQuarter() {
        return YearQuarter.now();
    }

    /**
     * Creates a YearWeek
     * 创建YearWeek
     *
     * @param weekBasedYear the week-based year | 周基准年
     * @param week          the week (1-52/53) | 周（1-52/53）
     * @return the YearWeek | YearWeek
     */
    public static YearWeek yearWeek(int weekBasedYear, int week) {
        return YearWeek.of(weekBasedYear, week);
    }

    /**
     * Gets the current YearWeek
     * 获取当前YearWeek
     *
     * @return the current YearWeek | 当前YearWeek
     */
    public static YearWeek currentWeek() {
        return YearWeek.now();
    }

    // ==================== Instant Factory | Instant工厂 ====================

    /**
     * Gets the current Instant
     * 获取当前Instant
     *
     * @return the current Instant | 当前Instant
     */
    public static Instant instant() {
        return Instant.now();
    }

    /**
     * Creates an Instant from epoch milliseconds
     * 从毫秒时间戳创建Instant
     *
     * @param epochMilli the epoch milliseconds | 毫秒时间戳
     * @return the Instant | Instant
     */
    public static Instant instant(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli);
    }

    /**
     * Creates an Instant from epoch seconds
     * 从秒时间戳创建Instant
     *
     * @param epochSecond the epoch seconds | 秒时间戳
     * @param nanoAdjust  the nanosecond adjustment | 纳秒调整
     * @return the Instant | Instant
     */
    public static Instant instant(long epochSecond, long nanoAdjust) {
        return Instant.ofEpochSecond(epochSecond, nanoAdjust);
    }

    // ==================== Duration Factory | Duration工厂 ====================

    /**
     * Creates a Duration of the specified nanoseconds
     * 创建指定纳秒数的Duration
     *
     * @param nanos the nanoseconds | 纳秒数
     * @return the Duration | Duration
     */
    public static Duration nanos(long nanos) {
        return Duration.ofNanos(nanos);
    }

    /**
     * Creates a Duration of the specified milliseconds
     * 创建指定毫秒数的Duration
     *
     * @param millis the milliseconds | 毫秒数
     * @return the Duration | Duration
     */
    public static Duration millis(long millis) {
        return Duration.ofMillis(millis);
    }

    /**
     * Creates a Duration of the specified seconds
     * 创建指定秒数的Duration
     *
     * @param seconds the seconds | 秒数
     * @return the Duration | Duration
     */
    public static Duration seconds(long seconds) {
        return Duration.ofSeconds(seconds);
    }

    /**
     * Creates a Duration of the specified minutes
     * 创建指定分钟数的Duration
     *
     * @param minutes the minutes | 分钟数
     * @return the Duration | Duration
     */
    public static Duration minutes(long minutes) {
        return Duration.ofMinutes(minutes);
    }

    /**
     * Creates a Duration of the specified hours
     * 创建指定小时数的Duration
     *
     * @param hours the hours | 小时数
     * @return the Duration | Duration
     */
    public static Duration hours(long hours) {
        return Duration.ofHours(hours);
    }

    /**
     * Creates a Duration of the specified days
     * 创建指定天数的Duration
     *
     * @param days the days | 天数
     * @return the Duration | Duration
     */
    public static Duration days(long days) {
        return Duration.ofDays(days);
    }

    // ==================== Period Factory | Period工厂 ====================

    /**
     * Creates a Period of the specified days
     * 创建指定天数的Period
     *
     * @param days the days | 天数
     * @return the Period | Period
     */
    public static Period periodDays(int days) {
        return Period.ofDays(days);
    }

    /**
     * Creates a Period of the specified weeks
     * 创建指定周数的Period
     *
     * @param weeks the weeks | 周数
     * @return the Period | Period
     */
    public static Period weeks(int weeks) {
        return Period.ofWeeks(weeks);
    }

    /**
     * Creates a Period of the specified months
     * 创建指定月数的Period
     *
     * @param months the months | 月数
     * @return the Period | Period
     */
    public static Period months(int months) {
        return Period.ofMonths(months);
    }

    /**
     * Creates a Period of the specified years
     * 创建指定年数的Period
     *
     * @param years the years | 年数
     * @return the Period | Period
     */
    public static Period years(int years) {
        return Period.ofYears(years);
    }

    /**
     * Creates a Period from years, months, and days
     * 从年、月、日创建Period
     *
     * @param years  the years | 年
     * @param months the months | 月
     * @param days   the days | 日
     * @return the Period | Period
     */
    public static Period period(int years, int months, int days) {
        return Period.of(years, months, days);
    }

    // ==================== Range Factory | 范围工厂 ====================

    /**
     * Creates a LocalDateRange
     * 创建LocalDateRange
     *
     * @param start the start date | 开始日期
     * @param end   the end date | 结束日期
     * @return the LocalDateRange | LocalDateRange
     */
    public static LocalDateRange dateRange(LocalDate start, LocalDate end) {
        return LocalDateRange.of(start, end);
    }

    /**
     * Creates a LocalDateTimeRange
     * 创建LocalDateTimeRange
     *
     * @param start the start datetime | 开始日期时间
     * @param end   the end datetime | 结束日期时间
     * @return the LocalDateTimeRange | LocalDateTimeRange
     */
    public static LocalDateTimeRange dateTimeRange(LocalDateTime start, LocalDateTime end) {
        return LocalDateTimeRange.of(start, end);
    }

    /**
     * Creates an Interval
     * 创建Interval
     *
     * @param start the start instant | 开始时刻
     * @param end   the end instant | 结束时刻
     * @return the Interval | Interval
     */
    public static Interval interval(Instant start, Instant end) {
        return Interval.of(start, end);
    }

    /**
     * Creates an Interval from start and duration
     * 从开始时刻和时长创建Interval
     *
     * @param start    the start instant | 开始时刻
     * @param duration the duration | 时长
     * @return the Interval | Interval
     */
    public static Interval interval(Instant start, Duration duration) {
        return Interval.of(start, duration);
    }

    // ==================== PeriodDuration Factory | PeriodDuration工厂 ====================

    /**
     * Creates a PeriodDuration from Period and Duration
     * 从Period和Duration创建PeriodDuration
     *
     * @param period   the period | 周期
     * @param duration the duration | 时长
     * @return the PeriodDuration | PeriodDuration
     */
    public static PeriodDuration periodDuration(Period period, Duration duration) {
        return PeriodDuration.of(period, duration);
    }

    /**
     * Creates a PeriodDuration between two LocalDateTimes
     * 计算两个LocalDateTime之间的PeriodDuration
     *
     * @param start the start | 开始
     * @param end   the end | 结束
     * @return the PeriodDuration | PeriodDuration
     */
    public static PeriodDuration between(LocalDateTime start, LocalDateTime end) {
        return PeriodDuration.between(start, end);
    }
}
