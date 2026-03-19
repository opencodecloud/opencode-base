package cloud.opencode.base.date.formatter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Predefined Date Formatters with caching support
 * 预定义的日期格式化器，支持缓存
 *
 * <p>This class provides commonly used DateTimeFormatter instances and a caching mechanism
 * for custom patterns. All formatters are thread-safe and reusable.</p>
 * <p>此类提供常用的DateTimeFormatter实例和自定义模式的缓存机制。
 * 所有格式化器都是线程安全的和可重用的。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Predefined common formats (ISO, Chinese, etc.) - 预定义常用格式（ISO、中文等）</li>
 *   <li>Formatter caching for custom patterns - 自定义模式的格式化器缓存</li>
 *   <li>Thread-safe formatter access - 线程安全的格式化器访问</li>
 *   <li>Quick format methods - 快速格式化方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use predefined formatters
 * String date = DateFormatter.NORM_DATE.format(LocalDate.now());       // 2024-01-15
 * String time = DateFormatter.NORM_TIME.format(LocalTime.now());       // 14:30:45
 * String dateTime = DateFormatter.NORM_DATETIME.format(LocalDateTime.now()); // 2024-01-15 14:30:45
 *
 * // Use Chinese format
 * String chinese = DateFormatter.CHINESE_DATE.format(LocalDate.now()); // 2024年01月15日
 *
 * // Get cached formatter
 * DateTimeFormatter custom = DateFormatter.ofPattern("yyyy/MM/dd");
 *
 * // Quick format
 * String formatted = DateFormatter.format(LocalDateTime.now(), "yyyy-MM-dd");
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Formatter caching reduces object creation - 格式化器缓存减少对象创建</li>
 *   <li>Thread-safe using ConcurrentHashMap - 使用ConcurrentHashMap保证线程安全</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable formatters: Yes - 不可变格式化器: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see DateParser
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class DateFormatter {

    private DateFormatter() {
        // Utility class
    }

    /**
     * Cache for custom pattern formatters
     */
    private static final ConcurrentMap<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap<>();

    // ==================== Standard Date Formats | 标准日期格式 ====================

    /**
     * Standard date format: yyyy-MM-dd (e.g., 2024-01-15)
     * 标准日期格式：yyyy-MM-dd（例如：2024-01-15）
     */
    public static final DateTimeFormatter NORM_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Standard time format: HH:mm:ss (e.g., 14:30:45)
     * 标准时间格式：HH:mm:ss（例如：14:30:45）
     */
    public static final DateTimeFormatter NORM_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Standard datetime format: yyyy-MM-dd HH:mm:ss (e.g., 2024-01-15 14:30:45)
     * 标准日期时间格式：yyyy-MM-dd HH:mm:ss（例如：2024-01-15 14:30:45）
     */
    public static final DateTimeFormatter NORM_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Datetime with milliseconds: yyyy-MM-dd HH:mm:ss.SSS
     * 带毫秒的日期时间：yyyy-MM-dd HH:mm:ss.SSS
     */
    public static final DateTimeFormatter NORM_DATETIME_MS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Time with milliseconds: HH:mm:ss.SSS
     * 带毫秒的时间：HH:mm:ss.SSS
     */
    public static final DateTimeFormatter NORM_TIME_MS = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    // ==================== Compact Formats | 紧凑格式 ====================

    /**
     * Compact date format: yyyyMMdd (e.g., 20240115)
     * 紧凑日期格式：yyyyMMdd（例如：20240115）
     */
    public static final DateTimeFormatter PURE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Compact time format: HHmmss (e.g., 143045)
     * 紧凑时间格式：HHmmss（例如：143045）
     */
    public static final DateTimeFormatter PURE_TIME = DateTimeFormatter.ofPattern("HHmmss");

    /**
     * Compact datetime format: yyyyMMddHHmmss (e.g., 20240115143045)
     * 紧凑日期时间格式：yyyyMMddHHmmss（例如：20240115143045）
     */
    public static final DateTimeFormatter PURE_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Compact datetime with milliseconds: yyyyMMddHHmmssSSS
     * 紧凑日期时间带毫秒：yyyyMMddHHmmssSSS
     */
    public static final DateTimeFormatter PURE_DATETIME_MS = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    // ==================== Chinese Formats | 中文格式 ====================

    /**
     * Chinese date format: yyyy年MM月dd日 (e.g., 2024年01月15日)
     * 中文日期格式：yyyy年MM月dd日（例如：2024年01月15日）
     */
    public static final DateTimeFormatter CHINESE_DATE = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    /**
     * Chinese time format: HH时mm分ss秒 (e.g., 14时30分45秒)
     * 中文时间格式：HH时mm分ss秒（例如：14时30分45秒）
     */
    public static final DateTimeFormatter CHINESE_TIME = DateTimeFormatter.ofPattern("HH时mm分ss秒");

    /**
     * Chinese datetime format: yyyy年MM月dd日 HH时mm分ss秒
     * 中文日期时间格式：yyyy年MM月dd日 HH时mm分ss秒
     */
    public static final DateTimeFormatter CHINESE_DATETIME = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分ss秒");

    /**
     * Chinese date format (short): yyyy年M月d日 (e.g., 2024年1月5日)
     * 中文日期格式（短）：yyyy年M月d日（例如：2024年1月5日）
     */
    public static final DateTimeFormatter CHINESE_DATE_SHORT = DateTimeFormatter.ofPattern("yyyy年M月d日");

    // ==================== ISO Formats | ISO格式 ====================

    /**
     * ISO date format: yyyy-MM-dd (same as DateTimeFormatter.ISO_LOCAL_DATE)
     * ISO日期格式：yyyy-MM-dd（与DateTimeFormatter.ISO_LOCAL_DATE相同）
     */
    public static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * ISO time format: HH:mm:ss (same as DateTimeFormatter.ISO_LOCAL_TIME)
     * ISO时间格式：HH:mm:ss（与DateTimeFormatter.ISO_LOCAL_TIME相同）
     */
    public static final DateTimeFormatter ISO_TIME = DateTimeFormatter.ISO_LOCAL_TIME;

    /**
     * ISO datetime format: yyyy-MM-dd'T'HH:mm:ss
     * ISO日期时间格式：yyyy-MM-dd'T'HH:mm:ss
     */
    public static final DateTimeFormatter ISO_DATETIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * ISO offset datetime format: yyyy-MM-dd'T'HH:mm:ssXXX
     * ISO偏移日期时间格式：yyyy-MM-dd'T'HH:mm:ssXXX
     */
    public static final DateTimeFormatter ISO_OFFSET_DATETIME = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    /**
     * ISO zoned datetime format: yyyy-MM-dd'T'HH:mm:ssXXX[VV]
     * ISO带时区日期时间格式：yyyy-MM-dd'T'HH:mm:ssXXX[VV]
     */
    public static final DateTimeFormatter ISO_ZONED_DATETIME = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    // ==================== HTTP and RFC Formats | HTTP和RFC格式 ====================

    /**
     * HTTP date format: EEE, dd MMM yyyy HH:mm:ss 'GMT' (RFC 7231)
     * HTTP日期格式：EEE, dd MMM yyyy HH:mm:ss 'GMT'（RFC 7231）
     */
    public static final DateTimeFormatter HTTP_DATE = DateTimeFormatter.RFC_1123_DATE_TIME;

    // ==================== Month/Year Formats | 月份/年份格式 ====================

    /**
     * Year-month format: yyyy-MM (e.g., 2024-01)
     * 年-月格式：yyyy-MM（例如：2024-01）
     */
    public static final DateTimeFormatter NORM_MONTH = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * Year format: yyyy (e.g., 2024)
     * 年份格式：yyyy（例如：2024）
     */
    public static final DateTimeFormatter NORM_YEAR = DateTimeFormatter.ofPattern("yyyy");

    /**
     * Chinese month format: yyyy年MM月 (e.g., 2024年01月)
     * 中文月份格式：yyyy年MM月（例如：2024年01月）
     */
    public static final DateTimeFormatter CHINESE_MONTH = DateTimeFormatter.ofPattern("yyyy年MM月");

    // ==================== Flexible Formats | 弹性格式 ====================

    /**
     * Flexible datetime format that tolerates various separators
     * 弹性日期时间格式，容忍各种分隔符
     */
    public static final DateTimeFormatter FLEXIBLE_DATETIME = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .optionalStart().appendLiteral('-').optionalEnd()
            .optionalStart().appendLiteral('/').optionalEnd()
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .optionalStart().appendLiteral('-').optionalEnd()
            .optionalStart().appendLiteral('/').optionalEnd()
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .optionalStart()
            .optionalStart().appendLiteral(' ').optionalEnd()
            .optionalStart().appendLiteral('T').optionalEnd()
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .optionalStart().appendLiteral(':').optionalEnd()
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .optionalStart()
            .optionalStart().appendLiteral(':').optionalEnd()
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .optionalEnd()
            .optionalEnd()
            .toFormatter();

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Gets or creates a cached DateTimeFormatter for the given pattern
     * 获取或创建给定模式的缓存DateTimeFormatter
     *
     * @param pattern the pattern | 模式
     * @return the DateTimeFormatter | DateTimeFormatter
     * @throws NullPointerException     if pattern is null | 如果模式为null则抛出异常
     * @throws IllegalArgumentException if pattern is invalid | 如果模式无效则抛出异常
     */
    public static DateTimeFormatter ofPattern(String pattern) {
        Objects.requireNonNull(pattern, "pattern must not be null");
        return FORMATTER_CACHE.computeIfAbsent(pattern, DateTimeFormatter::ofPattern);
    }

    /**
     * Gets or creates a cached DateTimeFormatter for the given pattern and locale
     * 获取或创建给定模式和区域设置的缓存DateTimeFormatter
     *
     * @param pattern the pattern | 模式
     * @param locale  the locale | 区域设置
     * @return the DateTimeFormatter | DateTimeFormatter
     */
    public static DateTimeFormatter ofPattern(String pattern, Locale locale) {
        Objects.requireNonNull(pattern, "pattern must not be null");
        Objects.requireNonNull(locale, "locale must not be null");
        String key = pattern + "_" + locale.toLanguageTag();
        return FORMATTER_CACHE.computeIfAbsent(key, k -> DateTimeFormatter.ofPattern(pattern, locale));
    }

    // ==================== Quick Format Methods | 快速格式化方法 ====================

    /**
     * Formats a temporal using the given pattern
     * 使用给定模式格式化时间对象
     *
     * @param temporal the temporal | 时间对象
     * @param pattern  the pattern | 模式
     * @return the formatted string | 格式化的字符串
     */
    public static String format(TemporalAccessor temporal, String pattern) {
        Objects.requireNonNull(temporal, "temporal must not be null");
        return ofPattern(pattern).format(temporal);
    }

    /**
     * Formats a LocalDate to standard date format (yyyy-MM-dd)
     * 将LocalDate格式化为标准日期格式（yyyy-MM-dd）
     *
     * @param date the date | 日期
     * @return the formatted string | 格式化的字符串
     */
    public static String formatDate(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return NORM_DATE.format(date);
    }

    /**
     * Formats a LocalTime to standard time format (HH:mm:ss)
     * 将LocalTime格式化为标准时间格式（HH:mm:ss）
     *
     * @param time the time | 时间
     * @return the formatted string | 格式化的字符串
     */
    public static String formatTime(LocalTime time) {
        Objects.requireNonNull(time, "time must not be null");
        return NORM_TIME.format(time);
    }

    /**
     * Formats a LocalDateTime to standard datetime format (yyyy-MM-dd HH:mm:ss)
     * 将LocalDateTime格式化为标准日期时间格式（yyyy-MM-dd HH:mm:ss）
     *
     * @param dateTime the datetime | 日期时间
     * @return the formatted string | 格式化的字符串
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return NORM_DATETIME.format(dateTime);
    }

    /**
     * Formats a LocalDateTime to ISO format (yyyy-MM-dd'T'HH:mm:ss)
     * 将LocalDateTime格式化为ISO格式（yyyy-MM-dd'T'HH:mm:ss）
     *
     * @param dateTime the datetime | 日期时间
     * @return the formatted string | 格式化的字符串
     */
    public static String formatIso(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return ISO_DATETIME.format(dateTime);
    }

    /**
     * Formats a temporal to Chinese date format (yyyy年MM月dd日)
     * 将时间对象格式化为中文日期格式（yyyy年MM月dd日）
     *
     * @param date the date | 日期
     * @return the formatted string | 格式化的字符串
     */
    public static String formatChinese(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return CHINESE_DATE.format(date);
    }

    /**
     * Formats a temporal to Chinese datetime format
     * 将时间对象格式化为中文日期时间格式
     *
     * @param dateTime the datetime | 日期时间
     * @return the formatted string | 格式化的字符串
     */
    public static String formatChinese(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return CHINESE_DATETIME.format(dateTime);
    }

    // ==================== Cache Management | 缓存管理 ====================

    /**
     * Gets the current cache size
     * 获取当前缓存大小
     *
     * @return the cache size | 缓存大小
     */
    public static int cacheSize() {
        return FORMATTER_CACHE.size();
    }

    /**
     * Clears the formatter cache
     * 清除格式化器缓存
     */
    public static void clearCache() {
        FORMATTER_CACHE.clear();
    }
}
