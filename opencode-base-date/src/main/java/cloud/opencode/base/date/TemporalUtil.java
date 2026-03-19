package cloud.opencode.base.date;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * Utility class for temporal operations
 * 时间操作工具类
 *
 * <p>This class provides utility methods for working with temporal objects,
 * including type conversions, comparisons, and common operations.</p>
 * <p>此类提供处理时间对象的工具方法，包括类型转换、比较和常用操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type conversions between temporal types - 时间类型之间的转换</li>
 *   <li>Comparison utilities - 比较工具</li>
 *   <li>Truncation and rounding - 截断和取整</li>
 *   <li>Range and boundary operations - 范围和边界操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Convert between types
 * LocalDateTime dateTime = TemporalUtil.toLocalDateTime(instant, ZoneId.systemDefault());
 * Instant instant = TemporalUtil.toInstant(localDateTime, ZoneId.systemDefault());
 *
 * // Comparisons
 * boolean between = TemporalUtil.isBetween(date, start, end);
 * LocalDate max = TemporalUtil.max(date1, date2);
 *
 * // Truncation
 * LocalDateTime startOfDay = TemporalUtil.startOfDay(dateTime);
 * LocalDateTime endOfMonth = TemporalUtil.endOfMonth(dateTime);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes (with explicit null checks) - 空值安全: 是（有明确的空值检查）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - all operations are constant-time type conversions and comparisons - 时间复杂度: O(1) - 所有操作均为常数时间的类型转换和比较</li>
 *   <li>Space complexity: O(1) - no additional data structures allocated - 空间复杂度: O(1) - 不分配额外的数据结构</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class TemporalUtil {

    // ==================== Private Constructor | 私有构造函数 ====================

    private TemporalUtil() {
        // Utility class
    }

    // ==================== Conversion Methods | 转换方法 ====================

    /**
     * Converts an Instant to LocalDateTime using the specified zone
     * 使用指定时区将Instant转换为LocalDateTime
     *
     * @param instant the instant | 瞬间
     * @param zone    the zone | 时区
     * @return the LocalDateTime | 本地日期时间
     */
    public static LocalDateTime toLocalDateTime(Instant instant, ZoneId zone) {
        Objects.requireNonNull(instant, "instant must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
        return LocalDateTime.ofInstant(instant, zone);
    }

    /**
     * Converts an Instant to LocalDateTime using the system default zone
     * 使用系统默认时区将Instant转换为LocalDateTime
     *
     * @param instant the instant | 瞬间
     * @return the LocalDateTime | 本地日期时间
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        return toLocalDateTime(instant, ZoneId.systemDefault());
    }

    /**
     * Converts a LocalDateTime to Instant using the specified zone
     * 使用指定时区将LocalDateTime转换为Instant
     *
     * @param dateTime the date-time | 日期时间
     * @param zone     the zone | 时区
     * @return the Instant | 瞬间
     */
    public static Instant toInstant(LocalDateTime dateTime, ZoneId zone) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
        return dateTime.atZone(zone).toInstant();
    }

    /**
     * Converts a LocalDateTime to Instant using the system default zone
     * 使用系统默认时区将LocalDateTime转换为Instant
     *
     * @param dateTime the date-time | 日期时间
     * @return the Instant | 瞬间
     */
    public static Instant toInstant(LocalDateTime dateTime) {
        return toInstant(dateTime, ZoneId.systemDefault());
    }

    /**
     * Converts a LocalDate to LocalDateTime at start of day
     * 将LocalDate转换为当天开始的LocalDateTime
     *
     * @param date the date | 日期
     * @return the LocalDateTime | 本地日期时间
     */
    public static LocalDateTime toLocalDateTime(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.atStartOfDay();
    }

    /**
     * Converts epoch milliseconds to LocalDateTime
     * 将纪元毫秒转换为LocalDateTime
     *
     * @param epochMilli the epoch milliseconds | 纪元毫秒
     * @return the LocalDateTime | 本地日期时间
     */
    public static LocalDateTime fromEpochMilli(long epochMilli) {
        return toLocalDateTime(Instant.ofEpochMilli(epochMilli));
    }

    /**
     * Converts epoch seconds to LocalDateTime
     * 将纪元秒转换为LocalDateTime
     *
     * @param epochSecond the epoch seconds | 纪元秒
     * @return the LocalDateTime | 本地日期时间
     */
    public static LocalDateTime fromEpochSecond(long epochSecond) {
        return toLocalDateTime(Instant.ofEpochSecond(epochSecond));
    }

    /**
     * Converts LocalDateTime to epoch milliseconds
     * 将LocalDateTime转换为纪元毫秒
     *
     * @param dateTime the date-time | 日期时间
     * @return the epoch milliseconds | 纪元毫秒
     */
    public static long toEpochMilli(LocalDateTime dateTime) {
        return toInstant(dateTime).toEpochMilli();
    }

    /**
     * Converts LocalDateTime to epoch seconds
     * 将LocalDateTime转换为纪元秒
     *
     * @param dateTime the date-time | 日期时间
     * @return the epoch seconds | 纪元秒
     */
    public static long toEpochSecond(LocalDateTime dateTime) {
        return toInstant(dateTime).getEpochSecond();
    }

    // ==================== Generic Conversion | 通用转换 ====================

    /**
     * Converts any supported temporal/date object to Instant.
     * 将任意支持的时间/日期对象转换为 Instant。
     *
     * <p>Supports: Instant, LocalDate, LocalDateTime, ZonedDateTime, OffsetDateTime,
     * java.util.Date, java.util.Calendar. Uses system default timezone for local types.</p>
     * <p>支持：Instant、LocalDate、LocalDateTime、ZonedDateTime、OffsetDateTime、
     * java.util.Date、java.util.Calendar。本地类型使用系统默认时区。</p>
     *
     * @param temporal the temporal object | 时间对象
     * @return the Instant, or null if unsupported type | Instant，不支持的类型返回 null
     */
    public static Instant toInstant(Object temporal) {
        return switch (temporal) {
            case null -> null;
            case Instant i -> i;
            case LocalDateTime ldt -> ldt.atZone(ZoneId.systemDefault()).toInstant();
            case ZonedDateTime zdt -> zdt.toInstant();
            case OffsetDateTime odt -> odt.toInstant();
            case LocalDate ld -> ld.atStartOfDay(ZoneId.systemDefault()).toInstant();
            case Date d -> d.toInstant();
            case Calendar c -> c.toInstant();
            default -> null;
        };
    }

    /**
     * Compares two temporal/date objects generically.
     * 通用比较两个时间/日期对象。
     *
     * <p>If both objects are the same Comparable type, uses natural ordering.
     * Otherwise falls back to Instant conversion for cross-type comparison.</p>
     * <p>如果两个对象是相同的 Comparable 类型，使用自然排序。
     * 否则回退到 Instant 转换进行跨类型比较。</p>
     *
     * @param a the first temporal | 第一个时间
     * @param b the second temporal | 第二个时间
     * @return negative if a &lt; b, 0 if equal, positive if a &gt; b
     * @throws IllegalArgumentException if types are not comparable | 类型不可比较时抛出
     */
    @SuppressWarnings("unchecked")
    public static int compareTemporal(Object a, Object b) {
        Objects.requireNonNull(a, "first temporal must not be null");
        Objects.requireNonNull(b, "second temporal must not be null");

        // Same-type Comparable fast path
        if (a.getClass() == b.getClass() && a instanceof Comparable<?> ca) {
            return ((Comparable<Object>) ca).compareTo(b);
        }

        // Cross-type via Instant
        Instant ia = toInstant(a);
        Instant ib = toInstant(b);
        if (ia != null && ib != null) {
            return ia.compareTo(ib);
        }

        // Generic Comparable fallback
        if (a instanceof Comparable<?> ca && a.getClass().isAssignableFrom(b.getClass())) {
            return ((Comparable<Object>) ca).compareTo(b);
        }

        throw new IllegalArgumentException(
            "Cannot compare temporal types: " + a.getClass().getName() + " and " + b.getClass().getName());
    }

    // ==================== Comparison Methods | 比较方法 ====================

    /**
     * Checks if a date is between two dates (inclusive)
     * 检查日期是否在两个日期之间（包含边界）
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
     * Checks if a date-time is between two date-times (inclusive)
     * 检查日期时间是否在两个日期时间之间（包含边界）
     *
     * @param dateTime the date-time to check | 要检查的日期时间
     * @param start    the start date-time | 开始日期时间
     * @param end      the end date-time | 结束日期时间
     * @return true if between | 如果在之间返回true
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }

    /**
     * Gets the maximum of two dates
     * 获取两个日期中的较大值
     *
     * @param a the first date | 第一个日期
     * @param b the second date | 第二个日期
     * @return the maximum | 较大值
     */
    public static LocalDate max(LocalDate a, LocalDate b) {
        Objects.requireNonNull(a, "a must not be null");
        Objects.requireNonNull(b, "b must not be null");
        return a.isAfter(b) ? a : b;
    }

    /**
     * Gets the minimum of two dates
     * 获取两个日期中的较小值
     *
     * @param a the first date | 第一个日期
     * @param b the second date | 第二个日期
     * @return the minimum | 较小值
     */
    public static LocalDate min(LocalDate a, LocalDate b) {
        Objects.requireNonNull(a, "a must not be null");
        Objects.requireNonNull(b, "b must not be null");
        return a.isBefore(b) ? a : b;
    }

    /**
     * Gets the maximum of two date-times
     * 获取两个日期时间中的较大值
     *
     * @param a the first date-time | 第一个日期时间
     * @param b the second date-time | 第二个日期时间
     * @return the maximum | 较大值
     */
    public static LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        Objects.requireNonNull(a, "a must not be null");
        Objects.requireNonNull(b, "b must not be null");
        return a.isAfter(b) ? a : b;
    }

    /**
     * Gets the minimum of two date-times
     * 获取两个日期时间中的较小值
     *
     * @param a the first date-time | 第一个日期时间
     * @param b the second date-time | 第二个日期时间
     * @return the minimum | 较小值
     */
    public static LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        Objects.requireNonNull(a, "a must not be null");
        Objects.requireNonNull(b, "b must not be null");
        return a.isBefore(b) ? a : b;
    }

    // ==================== Truncation Methods | 截断方法 ====================

    /**
     * Gets the start of day for a date-time
     * 获取日期时间的当天开始
     *
     * @param dateTime the date-time | 日期时间
     * @return the start of day | 当天开始
     */
    public static LocalDateTime startOfDay(LocalDateTime dateTime) {
        return dateTime.truncatedTo(ChronoUnit.DAYS);
    }

    /**
     * Gets the end of day for a date-time
     * 获取日期时间的当天结束
     *
     * @param dateTime the date-time | 日期时间
     * @return the end of day | 当天结束
     */
    public static LocalDateTime endOfDay(LocalDateTime dateTime) {
        return dateTime.toLocalDate().atTime(LocalTime.MAX);
    }

    /**
     * Gets the start of month for a date-time
     * 获取日期时间的当月开始
     *
     * @param dateTime the date-time | 日期时间
     * @return the start of month | 当月开始
     */
    public static LocalDateTime startOfMonth(LocalDateTime dateTime) {
        return dateTime.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
    }

    /**
     * Gets the end of month for a date-time
     * 获取日期时间的当月结束
     *
     * @param dateTime the date-time | 日期时间
     * @return the end of month | 当月结束
     */
    public static LocalDateTime endOfMonth(LocalDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atTime(LocalTime.MAX);
    }

    /**
     * Gets the start of year for a date-time
     * 获取日期时间的当年开始
     *
     * @param dateTime the date-time | 日期时间
     * @return the start of year | 当年开始
     */
    public static LocalDateTime startOfYear(LocalDateTime dateTime) {
        return dateTime.withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
    }

    /**
     * Gets the end of year for a date-time
     * 获取日期时间的当年结束
     *
     * @param dateTime the date-time | 日期时间
     * @return the end of year | 当年结束
     */
    public static LocalDateTime endOfYear(LocalDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.lastDayOfYear()).toLocalDate().atTime(LocalTime.MAX);
    }

    /**
     * Gets the start of week for a date-time (Monday)
     * 获取日期时间的当周开始（周一）
     *
     * @param dateTime the date-time | 日期时间
     * @return the start of week | 当周开始
     */
    public static LocalDateTime startOfWeek(LocalDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .truncatedTo(ChronoUnit.DAYS);
    }

    /**
     * Gets the end of week for a date-time (Sunday)
     * 获取日期时间的当周结束（周日）
     *
     * @param dateTime the date-time | 日期时间
     * @return the end of week | 当周结束
     */
    public static LocalDateTime endOfWeek(LocalDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .toLocalDate().atTime(LocalTime.MAX);
    }

    // ==================== Period/Duration Calculation | 周期/持续时间计算 ====================

    /**
     * Calculates days between two dates
     * 计算两个日期之间的天数
     *
     * @param start the start date | 开始日期
     * @param end   the end date | 结束日期
     * @return the number of days | 天数
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Calculates months between two dates
     * 计算两个日期之间的月数
     *
     * @param start the start date | 开始日期
     * @param end   the end date | 结束日期
     * @return the number of months | 月数
     */
    public static long monthsBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.MONTHS.between(start, end);
    }

    /**
     * Calculates years between two dates
     * 计算两个日期之间的年数
     *
     * @param start the start date | 开始日期
     * @param end   the end date | 结束日期
     * @return the number of years | 年数
     */
    public static long yearsBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.YEARS.between(start, end);
    }

    /**
     * Calculates hours between two date-times
     * 计算两个日期时间之间的小时数
     *
     * @param start the start date-time | 开始日期时间
     * @param end   the end date-time | 结束日期时间
     * @return the number of hours | 小时数
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * Calculates minutes between two date-times
     * 计算两个日期时间之间的分钟数
     *
     * @param start the start date-time | 开始日期时间
     * @param end   the end date-time | 结束日期时间
     * @return the number of minutes | 分钟数
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }

    // ==================== Day of Week Methods | 星期几方法 ====================

    /**
     * Checks if a date is a weekend
     * 检查日期是否为周末
     *
     * @param date the date | 日期
     * @return true if weekend | 如果是周末返回true
     */
    public static boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    /**
     * Checks if a date is a weekday
     * 检查日期是否为工作日（周一到周五）
     *
     * @param date the date | 日期
     * @return true if weekday | 如果是工作日返回true
     */
    public static boolean isWeekday(LocalDate date) {
        return !isWeekend(date);
    }

    // ==================== Leap Year Methods | 闰年方法 ====================

    /**
     * Checks if a year is a leap year
     * 检查年份是否为闰年
     *
     * @param year the year | 年份
     * @return true if leap year | 如果是闰年返回true
     */
    public static boolean isLeapYear(int year) {
        return Year.isLeap(year);
    }

    /**
     * Checks if a date is in a leap year
     * 检查日期是否在闰年中
     *
     * @param date the date | 日期
     * @return true if in leap year | 如果在闰年中返回true
     */
    public static boolean isLeapYear(LocalDate date) {
        return date.isLeapYear();
    }

    // ==================== Month Methods | 月份方法 ====================

    /**
     * Gets the number of days in a month
     * 获取月份的天数
     *
     * @param year  the year | 年份
     * @param month the month | 月份
     * @return the number of days | 天数
     */
    public static int daysInMonth(int year, int month) {
        return YearMonth.of(year, month).lengthOfMonth();
    }

    /**
     * Gets the number of days in a month
     * 获取月份的天数
     *
     * @param yearMonth the year-month | 年月
     * @return the number of days | 天数
     */
    public static int daysInMonth(YearMonth yearMonth) {
        return yearMonth.lengthOfMonth();
    }

    // ==================== Quarter Methods | 季度方法 ====================

    /**
     * Gets the quarter for a month (1-4)
     * 获取月份的季度（1-4）
     *
     * @param month the month (1-12) | 月份（1-12）
     * @return the quarter (1-4) | 季度（1-4）
     */
    public static int getQuarter(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        return (month - 1) / 3 + 1;
    }

    /**
     * Gets the quarter for a date (1-4)
     * 获取日期的季度（1-4）
     *
     * @param date the date | 日期
     * @return the quarter (1-4) | 季度（1-4）
     */
    public static int getQuarter(LocalDate date) {
        return getQuarter(date.getMonthValue());
    }
}
