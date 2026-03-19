package cloud.opencode.base.date;

import cloud.opencode.base.date.exception.OpenDateException;
import cloud.opencode.base.date.extra.LocalDateRange;
import cloud.opencode.base.date.extra.Quarter;
import cloud.opencode.base.date.extra.YearQuarter;
import cloud.opencode.base.date.extra.YearWeek;
import cloud.opencode.base.date.formatter.DateFormatter;
import cloud.opencode.base.date.formatter.DateParser;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * Core Date Utility Class - Main Entry Point for OpenCode Date Module
 * 核心日期工具类 - OpenCode日期模块的主入口
 *
 * <p>This class provides comprehensive date/time operations as the main facade for the
 * date module. It is designed to be the one-stop solution for most date/time needs,
 * comparable to Commons DateUtils + Hutool DateUtil.</p>
 * <p>此类提供全面的日期时间操作，作为日期模块的主要门面。
 * 设计为满足大多数日期时间需求的一站式解决方案，
 * 可与Commons DateUtils + Hutool DateUtil相媲美。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Current time access - 当前时间访问</li>
 *   <li>Date/time creation - 日期时间创建</li>
 *   <li>Smart parsing - 智能解析</li>
 *   <li>Formatting - 格式化</li>
 *   <li>Truncation and rounding - 截断和取整</li>
 *   <li>Add/subtract operations - 加减操作</li>
 *   <li>Range and comparison - 范围和比较</li>
 *   <li>Boundary calculations - 边界计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Current time
 * LocalDate today = OpenDate.today();
 * LocalDateTime now = OpenDate.now();
 * long millis = OpenDate.currentTimeMillis();
 *
 * // Create dates
 * LocalDate date = OpenDate.of(2024, 1, 15);
 * LocalDateTime dateTime = OpenDate.of(2024, 1, 15, 14, 30, 0);
 *
 * // Smart parsing
 * LocalDateTime dt1 = OpenDate.parse("2024-01-15 14:30:45");
 * LocalDateTime dt2 = OpenDate.parse("20240115143045");
 *
 * // Formatting
 * String formatted = OpenDate.format(now);
 * String chinese = OpenDate.formatChinese(now);
 *
 * // Truncation
 * LocalDateTime truncated = OpenDate.truncateToDay(now);
 *
 * // Add/subtract
 * LocalDateTime future = OpenDate.plusDays(now, 7);
 *
 * // Boundaries
 * LocalDate monthStart = OpenDate.startOfMonth(today);
 * LocalDate monthEnd = OpenDate.endOfMonth(today);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>All methods are static and stateless - 所有方法都是静态和无状态的</li>
 *   <li>Cached formatters for performance - 缓存格式化器以提高性能</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes (with explicit null checks) - 空值安全: 是（有明确的空值检查）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see DateTimes
 * @see StopWatch
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class OpenDate {

    private OpenDate() {
        // Utility class
    }

    // ==================== Current Time | 当前时间 ====================

    /**
     * Gets the current date
     * 获取当前日期
     *
     * @return today's date | 今天的日期
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * Gets the current date for the specified zone
     * 获取指定时区的当前日期
     *
     * @param zone the zone | 时区
     * @return today's date in the zone | 该时区的今天日期
     */
    public static LocalDate today(ZoneId zone) {
        return LocalDate.now(zone);
    }

    /**
     * Gets the current datetime
     * 获取当前日期时间
     *
     * @return the current datetime | 当前日期时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Gets the current datetime for the specified zone
     * 获取指定时区的当前日期时间
     *
     * @param zone the zone | 时区
     * @return the current datetime in the zone | 该时区的当前日期时间
     */
    public static LocalDateTime now(ZoneId zone) {
        return LocalDateTime.now(zone);
    }

    /**
     * Gets the current time in milliseconds
     * 获取当前时间戳（毫秒）
     *
     * @return current time in milliseconds | 当前毫秒时间戳
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Gets the current time in seconds
     * 获取当前时间戳（秒）
     *
     * @return current time in seconds | 当前秒时间戳
     */
    public static long currentTimeSeconds() {
        return Instant.now().getEpochSecond();
    }

    /**
     * Gets the current Instant
     * 获取当前Instant
     *
     * @return the current Instant | 当前Instant
     */
    public static Instant instant() {
        return Instant.now();
    }

    // ==================== Creation | 创建 ====================

    /**
     * Creates a LocalDate
     * 创建LocalDate
     *
     * @param year       the year | 年
     * @param month      the month | 月
     * @param dayOfMonth the day | 日
     * @return the LocalDate | LocalDate
     */
    public static LocalDate of(int year, int month, int dayOfMonth) {
        return LocalDate.of(year, month, dayOfMonth);
    }

    /**
     * Creates a LocalDateTime
     * 创建LocalDateTime
     *
     * @param year       the year | 年
     * @param month      the month | 月
     * @param dayOfMonth the day | 日
     * @param hour       the hour | 时
     * @param minute     the minute | 分
     * @param second     the second | 秒
     * @return the LocalDateTime | LocalDateTime
     */
    public static LocalDateTime of(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
    }

    /**
     * Creates a LocalDateTime from epoch milliseconds
     * 从毫秒时间戳创建LocalDateTime
     *
     * @param epochMilli the epoch milliseconds | 毫秒时间戳
     * @return the LocalDateTime | LocalDateTime
     */
    public static LocalDateTime ofEpochMilli(long epochMilli) {
        return DateParser.fromEpochMilli(epochMilli);
    }

    /**
     * Creates a LocalDateTime from epoch seconds
     * 从秒时间戳创建LocalDateTime
     *
     * @param epochSecond the epoch seconds | 秒时间戳
     * @return the LocalDateTime | LocalDateTime
     */
    public static LocalDateTime ofEpochSecond(long epochSecond) {
        return DateParser.fromEpochSecond(epochSecond);
    }

    /**
     * Converts a Date to LocalDateTime
     * 将Date转换为LocalDateTime
     *
     * @param date the Date | Date对象
     * @return the LocalDateTime | LocalDateTime
     */
    public static LocalDateTime from(Date date) {
        Objects.requireNonNull(date, "date must not be null");
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * Converts a Calendar to LocalDateTime
     * 将Calendar转换为LocalDateTime
     *
     * @param calendar the Calendar | Calendar对象
     * @return the LocalDateTime | LocalDateTime
     */
    public static LocalDateTime from(Calendar calendar) {
        Objects.requireNonNull(calendar, "calendar must not be null");
        return LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
    }

    // ==================== Parsing | 解析 ====================

    /**
     * Parses a string to LocalDateTime using smart format detection
     * 使用智能格式检测将字符串解析为LocalDateTime
     *
     * @param text the text to parse | 要解析的文本
     * @return the parsed LocalDateTime | 解析后的LocalDateTime
     * @throws OpenDateException if parsing fails | 如果解析失败则抛出异常
     */
    public static LocalDateTime parse(String text) {
        return DateParser.parseDateTime(text);
    }

    /**
     * Parses a string to LocalDate using smart format detection
     * 使用智能格式检测将字符串解析为LocalDate
     *
     * @param text the text to parse | 要解析的文本
     * @return the parsed LocalDate | 解析后的LocalDate
     * @throws OpenDateException if parsing fails | 如果解析失败则抛出异常
     */
    public static LocalDate parseDate(String text) {
        return DateParser.parseDate(text);
    }

    /**
     * Parses a string to LocalTime using smart format detection
     * 使用智能格式检测将字符串解析为LocalTime
     *
     * @param text the text to parse | 要解析的文本
     * @return the parsed LocalTime | 解析后的LocalTime
     * @throws OpenDateException if parsing fails | 如果解析失败则抛出异常
     */
    public static LocalTime parseTime(String text) {
        return DateParser.parseTime(text);
    }

    /**
     * Parses a string to LocalDateTime using the specified pattern
     * 使用指定模式将字符串解析为LocalDateTime
     *
     * @param text    the text to parse | 要解析的文本
     * @param pattern the pattern | 模式
     * @return the parsed LocalDateTime | 解析后的LocalDateTime
     * @throws OpenDateException if parsing fails | 如果解析失败则抛出异常
     */
    public static LocalDateTime parse(String text, String pattern) {
        return DateParser.parse(text, pattern);
    }

    // ==================== Formatting | 格式化 ====================

    /**
     * Formats a temporal to the default format (yyyy-MM-dd HH:mm:ss)
     * 将时间对象格式化为默认格式（yyyy-MM-dd HH:mm:ss）
     *
     * @param temporal the temporal | 时间对象
     * @return the formatted string | 格式化的字符串
     */
    public static String format(Temporal temporal) {
        Objects.requireNonNull(temporal, "temporal must not be null");
        if (temporal instanceof LocalDate date) {
            return DateFormatter.formatDate(date);
        }
        if (temporal instanceof LocalTime time) {
            return DateFormatter.formatTime(time);
        }
        if (temporal instanceof LocalDateTime dateTime) {
            return DateFormatter.formatDateTime(dateTime);
        }
        return DateFormatter.NORM_DATETIME.format(temporal);
    }

    /**
     * Formats a temporal to the specified pattern
     * 将时间对象格式化为指定模式
     *
     * @param temporal the temporal | 时间对象
     * @param pattern  the pattern | 模式
     * @return the formatted string | 格式化的字符串
     */
    public static String format(Temporal temporal, String pattern) {
        Objects.requireNonNull(temporal, "temporal must not be null");
        return DateFormatter.format(temporal, pattern);
    }

    /**
     * Formats a temporal to ISO format
     * 将时间对象格式化为ISO格式
     *
     * @param temporal the temporal | 时间对象
     * @return the formatted string | 格式化的字符串
     */
    public static String formatIso(Temporal temporal) {
        Objects.requireNonNull(temporal, "temporal must not be null");
        if (temporal instanceof LocalDateTime dateTime) {
            return DateFormatter.formatIso(dateTime);
        }
        return DateFormatter.ISO_DATETIME.format(temporal);
    }

    /**
     * Formats a temporal to Chinese format
     * 将时间对象格式化为中文格式
     *
     * @param temporal the temporal | 时间对象
     * @return the formatted string | 格式化的字符串
     */
    public static String formatChinese(Temporal temporal) {
        Objects.requireNonNull(temporal, "temporal must not be null");
        if (temporal instanceof LocalDate date) {
            return DateFormatter.formatChinese(date);
        }
        if (temporal instanceof LocalDateTime dateTime) {
            return DateFormatter.formatChinese(dateTime);
        }
        return DateFormatter.CHINESE_DATETIME.format(temporal);
    }

    // ==================== Truncation | 截断 ====================

    /**
     * Truncates a temporal to the specified unit
     * 将时间对象截断到指定单位
     *
     * @param temporal the temporal | 时间对象
     * @param unit     the unit | 单位
     * @param <T>      the temporal type | 时间类型
     * @return the truncated temporal | 截断后的时间对象
     */
    @SuppressWarnings("unchecked")
    public static <T extends Temporal> T truncate(T temporal, TemporalUnit unit) {
        Objects.requireNonNull(temporal, "temporal must not be null");
        Objects.requireNonNull(unit, "unit must not be null");
        // Zero out all time fields smaller than the requested unit's duration
        Duration unitDuration = unit.getDuration();
        Duration nanoDuration = ChronoUnit.NANOS.getDuration();
        Duration secondDuration = ChronoUnit.SECONDS.getDuration();
        Duration minuteDuration = ChronoUnit.MINUTES.getDuration();
        Duration hourDuration = ChronoUnit.HOURS.getDuration();

        Temporal result = temporal;
        if (unitDuration.compareTo(nanoDuration) > 0 && result.isSupported(ChronoField.NANO_OF_SECOND)) {
            if (unitDuration.compareTo(secondDuration) >= 0) {
                result = result.with(ChronoField.NANO_OF_SECOND, 0);
            } else {
                // Truncate within the second at the sub-second unit boundary
                long nanos = result.get(ChronoField.NANO_OF_SECOND);
                long unitNanos = unitDuration.toNanos();
                result = result.with(ChronoField.NANO_OF_SECOND, (nanos / unitNanos) * unitNanos);
            }
        }
        if (unitDuration.compareTo(minuteDuration) >= 0 && result.isSupported(ChronoField.SECOND_OF_MINUTE)) {
            result = result.with(ChronoField.SECOND_OF_MINUTE, 0);
        }
        if (unitDuration.compareTo(hourDuration) >= 0 && result.isSupported(ChronoField.MINUTE_OF_HOUR)) {
            result = result.with(ChronoField.MINUTE_OF_HOUR, 0);
        }
        if (unitDuration.compareTo(hourDuration) > 0 && result.isSupported(ChronoField.HOUR_OF_DAY)) {
            result = result.with(ChronoField.HOUR_OF_DAY, 0);
        }
        return (T) result;
    }

    /**
     * Truncates a LocalDateTime to the start of day
     * 将LocalDateTime截断到当天开始
     *
     * @param dateTime the datetime | 日期时间
     * @return the truncated datetime | 截断后的日期时间
     */
    public static LocalDateTime truncateToDay(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return dateTime.truncatedTo(ChronoUnit.DAYS);
    }

    /**
     * Truncates a LocalDateTime to the start of hour
     * 将LocalDateTime截断到小时开始
     *
     * @param dateTime the datetime | 日期时间
     * @return the truncated datetime | 截断后的日期时间
     */
    public static LocalDateTime truncateToHour(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return dateTime.truncatedTo(ChronoUnit.HOURS);
    }

    /**
     * Truncates a LocalDateTime to the start of minute
     * 将LocalDateTime截断到分钟开始
     *
     * @param dateTime the datetime | 日期时间
     * @return the truncated datetime | 截断后的日期时间
     */
    public static LocalDateTime truncateToMinute(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return dateTime.truncatedTo(ChronoUnit.MINUTES);
    }

    // ==================== Add/Subtract | 加减 ====================

    /**
     * Adds years to a LocalDateTime
     * 在LocalDateTime上加年
     *
     * @param dateTime the datetime | 日期时间
     * @param years    the years to add | 要加的年数
     * @return the result | 结果
     */
    public static LocalDateTime plusYears(LocalDateTime dateTime, long years) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return dateTime.plusYears(years);
    }

    /**
     * Adds months to a LocalDateTime
     * 在LocalDateTime上加月
     *
     * @param dateTime the datetime | 日期时间
     * @param months   the months to add | 要加的月数
     * @return the result | 结果
     */
    public static LocalDateTime plusMonths(LocalDateTime dateTime, long months) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return dateTime.plusMonths(months);
    }

    /**
     * Adds weeks to a LocalDateTime
     * 在LocalDateTime上加周
     *
     * @param dateTime the datetime | 日期时间
     * @param weeks    the weeks to add | 要加的周数
     * @return the result | 结果
     */
    public static LocalDateTime plusWeeks(LocalDateTime dateTime, long weeks) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return dateTime.plusWeeks(weeks);
    }

    /**
     * Adds days to a LocalDateTime
     * 在LocalDateTime上加天
     *
     * @param dateTime the datetime | 日期时间
     * @param days     the days to add | 要加的天数
     * @return the result | 结果
     */
    public static LocalDateTime plusDays(LocalDateTime dateTime, long days) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return dateTime.plusDays(days);
    }

    /**
     * Adds hours to a LocalDateTime
     * 在LocalDateTime上加小时
     *
     * @param dateTime the datetime | 日期时间
     * @param hours    the hours to add | 要加的小时数
     * @return the result | 结果
     */
    public static LocalDateTime plusHours(LocalDateTime dateTime, long hours) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return dateTime.plusHours(hours);
    }

    /**
     * Adds minutes to a LocalDateTime
     * 在LocalDateTime上加分钟
     *
     * @param dateTime the datetime | 日期时间
     * @param minutes  the minutes to add | 要加的分钟数
     * @return the result | 结果
     */
    public static LocalDateTime plusMinutes(LocalDateTime dateTime, long minutes) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return dateTime.plusMinutes(minutes);
    }

    /**
     * Adds seconds to a LocalDateTime
     * 在LocalDateTime上加秒
     *
     * @param dateTime the datetime | 日期时间
     * @param seconds  the seconds to add | 要加的秒数
     * @return the result | 结果
     */
    public static LocalDateTime plusSeconds(LocalDateTime dateTime, long seconds) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return dateTime.plusSeconds(seconds);
    }

    // ==================== Boundaries | 边界 ====================

    /**
     * Gets the first day of the month
     * 获取月份的第一天
     *
     * @param date the date | 日期
     * @return the first day of the month | 月份的第一天
     */
    public static LocalDate startOfMonth(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.withDayOfMonth(1);
    }

    /**
     * Gets the last day of the month
     * 获取月份的最后一天
     *
     * @param date the date | 日期
     * @return the last day of the month | 月份的最后一天
     */
    public static LocalDate endOfMonth(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Gets the first day of the year
     * 获取年份的第一天
     *
     * @param date the date | 日期
     * @return the first day of the year | 年份的第一天
     */
    public static LocalDate startOfYear(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.withDayOfYear(1);
    }

    /**
     * Gets the last day of the year
     * 获取年份的最后一天
     *
     * @param date the date | 日期
     * @return the last day of the year | 年份的最后一天
     */
    public static LocalDate endOfYear(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.with(TemporalAdjusters.lastDayOfYear());
    }

    /**
     * Gets the first day of the week (Monday)
     * 获取周的第一天（周一）
     *
     * @param date the date | 日期
     * @return the first day of the week | 周的第一天
     */
    public static LocalDate startOfWeek(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Gets the last day of the week (Sunday)
     * 获取周的最后一天（周日）
     *
     * @param date the date | 日期
     * @return the last day of the week | 周的最后一天
     */
    public static LocalDate endOfWeek(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    /**
     * Gets the first day of the quarter
     * 获取季度的第一天
     *
     * @param date the date | 日期
     * @return the first day of the quarter | 季度的第一天
     */
    public static LocalDate startOfQuarter(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        Quarter quarter = Quarter.ofMonth(date.getMonthValue());
        return LocalDate.of(date.getYear(), quarter.firstMonth(), 1);
    }

    /**
     * Gets the last day of the quarter
     * 获取季度的最后一天
     *
     * @param date the date | 日期
     * @return the last day of the quarter | 季度的最后一天
     */
    public static LocalDate endOfQuarter(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        Quarter quarter = Quarter.ofMonth(date.getMonthValue());
        Month lastMonth = Month.of(quarter.lastMonth());
        return LocalDate.of(date.getYear(), lastMonth, lastMonth.length(date.isLeapYear()));
    }

    // ==================== Comparison | 比较 ====================

    /**
     * Checks if a date is between two dates (inclusive)
     * 检查日期是否在两个日期之间（包含）
     *
     * @param date  the date to check | 要检查的日期
     * @param start the start date | 开始日期
     * @param end   the end date | 结束日期
     * @return true if between | 如果在之间返回true
     */
    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        return !date.isBefore(start) && !date.isAfter(end);
    }

    /**
     * Checks if a datetime is between two datetimes (inclusive)
     * 检查日期时间是否在两个日期时间之间（包含）
     *
     * @param dateTime the datetime to check | 要检查的日期时间
     * @param start    the start datetime | 开始日期时间
     * @param end      the end datetime | 结束日期时间
     * @return true if between | 如果在之间返回true
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }

    /**
     * Checks if a date is today
     * 检查日期是否为今天
     *
     * @param date the date | 日期
     * @return true if today | 如果是今天返回true
     */
    public static boolean isToday(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.equals(today());
    }

    /**
     * Checks if a date is yesterday
     * 检查日期是否为昨天
     *
     * @param date the date | 日期
     * @return true if yesterday | 如果是昨天返回true
     */
    public static boolean isYesterday(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.equals(today().minusDays(1));
    }

    /**
     * Checks if a date is tomorrow
     * 检查日期是否为明天
     *
     * @param date the date | 日期
     * @return true if tomorrow | 如果是明天返回true
     */
    public static boolean isTomorrow(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.equals(today().plusDays(1));
    }

    /**
     * Checks if a date is a weekend (Saturday or Sunday)
     * 检查日期是否为周末（周六或周日）
     *
     * @param date the date | 日期
     * @return true if weekend | 如果是周末返回true
     */
    public static boolean isWeekend(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * Checks if a year is a leap year
     * 检查年份是否为闰年
     *
     * @param year the year | 年份
     * @return true if leap year | 如果是闰年返回true
     */
    public static boolean isLeapYear(int year) {
        return java.time.Year.isLeap(year);
    }

    // ==================== Duration Calculation | 时长计算 ====================

    /**
     * Gets the number of days between two dates
     * 获取两个日期之间的天数
     *
     * @param start the start date | 开始日期
     * @param end   the end date | 结束日期
     * @return the number of days | 天数
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Gets the number of months between two dates
     * 获取两个日期之间的月数
     *
     * @param start the start date | 开始日期
     * @param end   the end date | 结束日期
     * @return the number of months | 月数
     */
    public static long monthsBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        return ChronoUnit.MONTHS.between(start, end);
    }

    /**
     * Gets the number of years between two dates
     * 获取两个日期之间的年数
     *
     * @param start the start date | 开始日期
     * @param end   the end date | 结束日期
     * @return the number of years | 年数
     */
    public static long yearsBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        return ChronoUnit.YEARS.between(start, end);
    }

    /**
     * Gets the Period between two dates
     * 获取两个日期之间的Period
     *
     * @param start the start date | 开始日期
     * @param end   the end date | 结束日期
     * @return the Period | Period
     */
    public static Period periodBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        return Period.between(start, end);
    }

    /**
     * Gets the Duration between two datetimes
     * 获取两个日期时间之间的Duration
     *
     * @param start the start datetime | 开始日期时间
     * @param end   the end datetime | 结束日期时间
     * @return the Duration | Duration
     */
    public static Duration durationBetween(LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        return Duration.between(start, end);
    }

    // ==================== Conversion | 转换 ====================

    /**
     * Converts a LocalDateTime to epoch milliseconds
     * 将LocalDateTime转换为毫秒时间戳
     *
     * @param dateTime the datetime | 日期时间
     * @return the epoch milliseconds | 毫秒时间戳
     */
    public static long toEpochMilli(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Converts a LocalDateTime to epoch seconds
     * 将LocalDateTime转换为秒时间戳
     *
     * @param dateTime the datetime | 日期时间
     * @return the epoch seconds | 秒时间戳
     */
    public static long toEpochSecond(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return dateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    /**
     * Converts a LocalDateTime to java.util.Date
     * 将LocalDateTime转换为java.util.Date
     *
     * @param dateTime the datetime | 日期时间
     * @return the Date | Date
     */
    public static Date toDate(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Converts a LocalDate to java.util.Date
     * 将LocalDate转换为java.util.Date
     *
     * @param date the date | 日期
     * @return the Date | Date
     */
    public static Date toDate(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // ==================== Range | 范围 ====================

    /**
     * Creates a LocalDateRange from start to end
     * 创建从开始到结束的LocalDateRange
     *
     * @param start the start date | 开始日期
     * @param end   the end date | 结束日期
     * @return the LocalDateRange | LocalDateRange
     */
    public static LocalDateRange range(LocalDate start, LocalDate end) {
        return LocalDateRange.of(start, end);
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
     * Gets the current YearWeek
     * 获取当前YearWeek
     *
     * @return the current YearWeek | 当前YearWeek
     */
    public static YearWeek currentWeek() {
        return YearWeek.now();
    }
}
