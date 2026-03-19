package cloud.opencode.base.date.formatter;

import cloud.opencode.base.date.exception.OpenDateException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Smart Date Parser that can automatically detect and parse various date formats
 * 智能日期解析器，可自动检测和解析各种日期格式
 *
 * <p>This class provides intelligent parsing that automatically detects the format
 * of the input string and parses it appropriately. It supports 20+ common date formats.</p>
 * <p>此类提供智能解析，自动检测输入字符串的格式并进行适当解析。支持20多种常见日期格式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Auto-detect and parse 20+ common date/time formats - 自动检测并解析20多种常见日期时间格式</li>
 *   <li>Parse dates, times, and date-times - 解析日期、时间和日期时间</li>
 *   <li>Parse from Unix epoch (seconds and milliseconds) - 从Unix纪元（秒和毫秒）解析</li>
 *   <li>Parse with explicit pattern - 使用显式模式解析</li>
 * </ul>
 *
 * <p><strong>Supported Formats | 支持的格式:</strong></p>
 * <ul>
 *   <li>yyyy-MM-dd, yyyy/MM/dd, yyyy.MM.dd</li>
 *   <li>yyyy-MM-dd HH:mm:ss, yyyy-MM-dd'T'HH:mm:ss</li>
 *   <li>yyyyMMdd, yyyyMMddHHmmss</li>
 *   <li>yyyy年MM月dd日, yyyy年MM月dd日 HH时mm分ss秒</li>
 *   <li>Unix timestamp (seconds and milliseconds)</li>
 *   <li>ISO 8601, RFC 1123, and more</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Smart parse - auto-detects format
 * LocalDateTime dt1 = DateParser.parseDateTime("2024-01-15 14:30:45");
 * LocalDateTime dt2 = DateParser.parseDateTime("2024/01/15 14:30:45");
 * LocalDateTime dt3 = DateParser.parseDateTime("20240115143045");
 * LocalDateTime dt4 = DateParser.parseDateTime("2024年01月15日 14时30分45秒");
 *
 * // Parse date only
 * LocalDate date = DateParser.parseDate("2024-01-15");
 *
 * // Parse time only
 * LocalTime time = DateParser.parseTime("14:30:45");
 *
 * // Parse from timestamp
 * LocalDateTime fromMillis = DateParser.fromEpochMilli(1705312245000L);
 * LocalDateTime fromSeconds = DateParser.fromEpochSecond(1705312245L);
 *
 * // Parse with explicit pattern
 * LocalDateTime explicit = DateParser.parse("15/01/2024", "dd/MM/yyyy");
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Pattern matching for quick format detection - 模式匹配快速格式检测</li>
 *   <li>Cached formatters for repeated parsing - 缓存格式化器用于重复解析</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Input validation: Yes - 输入验证: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see DateFormatter
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class DateParser {

    private DateParser() {
        // Utility class
    }

    // ==================== Pattern Definitions | 模式定义 ====================

    /**
     * Pattern for pure numeric date: 20240115
     */
    private static final Pattern PURE_DATE_PATTERN = Pattern.compile("^\\d{8}$");

    /**
     * Pattern for pure numeric datetime: 20240115143045
     */
    private static final Pattern PURE_DATETIME_PATTERN = Pattern.compile("^\\d{14}$");

    /**
     * Pattern for pure numeric datetime with millis: 20240115143045123
     */
    private static final Pattern PURE_DATETIME_MS_PATTERN = Pattern.compile("^\\d{17}$");

    /**
     * Pattern for timestamp in milliseconds: 1705312245000
     */
    private static final Pattern TIMESTAMP_MS_PATTERN = Pattern.compile("^\\d{13}$");

    /**
     * Pattern for timestamp in seconds: 1705312245
     */
    private static final Pattern TIMESTAMP_S_PATTERN = Pattern.compile("^\\d{10}$");

    /**
     * Pattern for Chinese date: yyyy年MM月dd日
     */
    private static final Pattern CHINESE_DATE_PATTERN = Pattern.compile("^\\d{4}年\\d{1,2}月\\d{1,2}日.*$");

    /**
     * Common datetime format patterns to try
     */
    private static final List<DateTimeFormatter> DATETIME_FORMATTERS = List.of(
            // Standard formats
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"),

            // ISO formats
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_ZONED_DATE_TIME,
            DateTimeFormatter.ISO_INSTANT,

            // Chinese formats
            DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分ss秒"),
            DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy年M月d日 H时m分s秒"),
            DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm:ss"),

            // Compact formats
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss"),
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"),
            DateTimeFormatter.ofPattern("yyyyMMdd HHmmss"),

            // Other common formats
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),

            // RFC formats
            DateTimeFormatter.RFC_1123_DATE_TIME
    );

    /**
     * Common date format patterns to try
     */
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("yyyyMMdd"),
            DateTimeFormatter.ofPattern("yyyy年MM月dd日"),
            DateTimeFormatter.ofPattern("yyyy年M月d日"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ISO_LOCAL_DATE
    );

    /**
     * Common time format patterns to try
     */
    private static final List<DateTimeFormatter> TIME_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("HHmmss"),
            DateTimeFormatter.ofPattern("HHmmssSSS"),
            DateTimeFormatter.ofPattern("HH时mm分ss秒"),
            DateTimeFormatter.ofPattern("H时m分s秒"),
            DateTimeFormatter.ISO_LOCAL_TIME
    );

    // ==================== Smart Parse Methods | 智能解析方法 ====================

    /**
     * Parses a string to LocalDateTime using smart format detection
     * 使用智能格式检测将字符串解析为LocalDateTime
     *
     * @param text the text to parse | 要解析的文本
     * @return the parsed LocalDateTime | 解析后的LocalDateTime
     * @throws OpenDateException if the text cannot be parsed | 如果文本无法解析则抛出异常
     */
    public static LocalDateTime parseDateTime(String text) {
        if (text == null || text.isBlank()) {
            throw new OpenDateException("Text to parse must not be null or blank");
        }
        text = text.trim();

        // Try timestamp first (most specific patterns)
        if (TIMESTAMP_MS_PATTERN.matcher(text).matches()) {
            return fromEpochMilli(Long.parseLong(text));
        }
        if (TIMESTAMP_S_PATTERN.matcher(text).matches()) {
            return fromEpochSecond(Long.parseLong(text));
        }

        // Try pure numeric formats
        if (PURE_DATETIME_MS_PATTERN.matcher(text).matches()) {
            return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        }
        if (PURE_DATETIME_PATTERN.matcher(text).matches()) {
            return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        }

        // Try each formatter
        for (DateTimeFormatter formatter : DATETIME_FORMATTERS) {
            try {
                TemporalAccessor parsed = formatter.parse(text);

                // Try to extract LocalDateTime
                LocalDate date = parsed.query(TemporalQueries.localDate());
                LocalTime time = parsed.query(TemporalQueries.localTime());

                if (date != null && time != null) {
                    return LocalDateTime.of(date, time);
                }
                if (date != null) {
                    return date.atStartOfDay();
                }

                // Try ZonedDateTime or OffsetDateTime
                try {
                    ZonedDateTime zdt = ZonedDateTime.from(parsed);
                    return zdt.toLocalDateTime();
                } catch (Exception ignored) {
                }

                try {
                    OffsetDateTime odt = OffsetDateTime.from(parsed);
                    return odt.toLocalDateTime();
                } catch (Exception ignored) {
                }

                try {
                    Instant instant = Instant.from(parsed);
                    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                } catch (Exception ignored) {
                }

            } catch (DateTimeParseException ignored) {
                // Try next formatter
            }
        }

        throw OpenDateException.parseError(text);
    }

    /**
     * Parses a string to LocalDate using smart format detection
     * 使用智能格式检测将字符串解析为LocalDate
     *
     * @param text the text to parse | 要解析的文本
     * @return the parsed LocalDate | 解析后的LocalDate
     * @throws OpenDateException if the text cannot be parsed | 如果文本无法解析则抛出异常
     */
    public static LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) {
            throw new OpenDateException("Text to parse must not be null or blank");
        }
        text = text.trim();

        // Try pure numeric format
        if (PURE_DATE_PATTERN.matcher(text).matches()) {
            return LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

        // Try each date formatter
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
                // Try next formatter
            }
        }

        // Try parsing as datetime and extract date
        try {
            return parseDateTime(text).toLocalDate();
        } catch (Exception ignored) {
        }

        throw OpenDateException.parseError(text);
    }

    /**
     * Parses a string to LocalTime using smart format detection
     * 使用智能格式检测将字符串解析为LocalTime
     *
     * @param text the text to parse | 要解析的文本
     * @return the parsed LocalTime | 解析后的LocalTime
     * @throws OpenDateException if the text cannot be parsed | 如果文本无法解析则抛出异常
     */
    public static LocalTime parseTime(String text) {
        if (text == null || text.isBlank()) {
            throw new OpenDateException("Text to parse must not be null or blank");
        }
        text = text.trim();

        // Try each time formatter
        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                return LocalTime.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
                // Try next formatter
            }
        }

        // Try parsing as datetime and extract time
        try {
            return parseDateTime(text).toLocalTime();
        } catch (Exception ignored) {
        }

        throw OpenDateException.parseError(text);
    }

    // ==================== Explicit Pattern Parse | 明确模式解析 ====================

    /**
     * Parses a string to LocalDateTime using the specified pattern
     * 使用指定模式将字符串解析为LocalDateTime
     *
     * @param text    the text to parse | 要解析的文本
     * @param pattern the pattern | 模式
     * @return the parsed LocalDateTime | 解析后的LocalDateTime
     * @throws OpenDateException if the text cannot be parsed | 如果文本无法解析则抛出异常
     */
    public static LocalDateTime parse(String text, String pattern) {
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");
        try {
            return LocalDateTime.parse(text.trim(), DateFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            throw OpenDateException.parseError(text, pattern, e);
        }
    }

    /**
     * Parses a string to LocalDate using the specified pattern
     * 使用指定模式将字符串解析为LocalDate
     *
     * @param text    the text to parse | 要解析的文本
     * @param pattern the pattern | 模式
     * @return the parsed LocalDate | 解析后的LocalDate
     * @throws OpenDateException if the text cannot be parsed | 如果文本无法解析则抛出异常
     */
    public static LocalDate parseDate(String text, String pattern) {
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");
        try {
            return LocalDate.parse(text.trim(), DateFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            throw OpenDateException.parseError(text, pattern, e);
        }
    }

    /**
     * Parses a string to LocalTime using the specified pattern
     * 使用指定模式将字符串解析为LocalTime
     *
     * @param text    the text to parse | 要解析的文本
     * @param pattern the pattern | 模式
     * @return the parsed LocalTime | 解析后的LocalTime
     * @throws OpenDateException if the text cannot be parsed | 如果文本无法解析则抛出异常
     */
    public static LocalTime parseTime(String text, String pattern) {
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");
        try {
            return LocalTime.parse(text.trim(), DateFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            throw OpenDateException.parseError(text, pattern, e);
        }
    }

    // ==================== Timestamp Parse | 时间戳解析 ====================

    /**
     * Creates a LocalDateTime from epoch milliseconds
     * 从毫秒时间戳创建LocalDateTime
     *
     * @param epochMilli the epoch milliseconds | 毫秒时间戳
     * @return the LocalDateTime | LocalDateTime
     */
    public static LocalDateTime fromEpochMilli(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }

    /**
     * Creates a LocalDateTime from epoch milliseconds with specified zone
     * 从毫秒时间戳创建LocalDateTime（指定时区）
     *
     * @param epochMilli the epoch milliseconds | 毫秒时间戳
     * @param zone       the zone | 时区
     * @return the LocalDateTime | LocalDateTime
     */
    public static LocalDateTime fromEpochMilli(long epochMilli, ZoneId zone) {
        Objects.requireNonNull(zone, "zone must not be null");
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), zone);
    }

    /**
     * Creates a LocalDateTime from epoch seconds
     * 从秒时间戳创建LocalDateTime
     *
     * @param epochSecond the epoch seconds | 秒时间戳
     * @return the LocalDateTime | LocalDateTime
     */
    public static LocalDateTime fromEpochSecond(long epochSecond) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ZoneId.systemDefault());
    }

    /**
     * Creates a LocalDateTime from epoch seconds with specified zone
     * 从秒时间戳创建LocalDateTime（指定时区）
     *
     * @param epochSecond the epoch seconds | 秒时间戳
     * @param zone        the zone | 时区
     * @return the LocalDateTime | LocalDateTime
     */
    public static LocalDateTime fromEpochSecond(long epochSecond, ZoneId zone) {
        Objects.requireNonNull(zone, "zone must not be null");
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), zone);
    }

    // ==================== Try Parse Methods | 尝试解析方法 ====================

    /**
     * Tries to parse a string to LocalDateTime, returns null on failure
     * 尝试将字符串解析为LocalDateTime，失败返回null
     *
     * @param text the text to parse | 要解析的文本
     * @return the parsed LocalDateTime, or null if parsing fails | 解析后的LocalDateTime，解析失败返回null
     */
    public static LocalDateTime tryParseDateTime(String text) {
        try {
            return parseDateTime(text);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Tries to parse a string to LocalDate, returns null on failure
     * 尝试将字符串解析为LocalDate，失败返回null
     *
     * @param text the text to parse | 要解析的文本
     * @return the parsed LocalDate, or null if parsing fails | 解析后的LocalDate，解析失败返回null
     */
    public static LocalDate tryParseDate(String text) {
        try {
            return parseDate(text);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Tries to parse a string to LocalTime, returns null on failure
     * 尝试将字符串解析为LocalTime，失败返回null
     *
     * @param text the text to parse | 要解析的文本
     * @return the parsed LocalTime, or null if parsing fails | 解析后的LocalTime，解析失败返回null
     */
    public static LocalTime tryParseTime(String text) {
        try {
            return parseTime(text);
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== Validation Methods | 验证方法 ====================

    /**
     * Checks if the given text can be parsed as a datetime
     * 检查给定文本是否可以解析为日期时间
     *
     * @param text the text to check | 要检查的文本
     * @return true if parseable | 如果可解析返回true
     */
    public static boolean isValidDateTime(String text) {
        return tryParseDateTime(text) != null;
    }

    /**
     * Checks if the given text can be parsed as a date
     * 检查给定文本是否可以解析为日期
     *
     * @param text the text to check | 要检查的文本
     * @return true if parseable | 如果可解析返回true
     */
    public static boolean isValidDate(String text) {
        return tryParseDate(text) != null;
    }

    /**
     * Checks if the given text can be parsed as a time
     * 检查给定文本是否可以解析为时间
     *
     * @param text the text to check | 要检查的文本
     * @return true if parseable | 如果可解析返回true
     */
    public static boolean isValidTime(String text) {
        return tryParseTime(text) != null;
    }
}
