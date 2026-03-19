package cloud.opencode.base.core.convert.impl;

import cloud.opencode.base.core.convert.Converter;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Date/Time Type Converter - Converts between Java 8+ and legacy date types
 * 日期时间类型转换器 - 支持 Java 8+ 和旧版日期类型的相互转换
 *
 * <p>Supports conversion between modern Java time API and legacy date types.</p>
 * <p>支持现代 Java 时间 API 和旧版日期类型的相互转换。</p>
 *
 * <p><strong>Supported Types | 支持的类型:</strong></p>
 * <ul>
 *   <li>Java 8+: LocalDate, LocalDateTime, LocalTime, Instant, ZonedDateTime, OffsetDateTime</li>
 *   <li>Legacy: Date, java.sql.Date, java.sql.Time, Timestamp, Calendar - 旧版日期</li>
 *   <li>Timestamp: Long (milliseconds) - 时间戳</li>
 * </ul>
 *
 * <p><strong>Supported String Formats | 支持的字符串格式:</strong></p>
 * <ul>
 *   <li>ISO: 2024-01-15, 2024-01-15T10:30:00</li>
 *   <li>Common: yyyy-MM-dd, yyyy/MM/dd, yyyyMMdd - 常见格式</li>
 *   <li>With time: yyyy-MM-dd HH:mm:ss - 带时间格式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // String to LocalDate - 字符串转日期
 * LocalDate date = DateConverter.localDateConverter().convert("2024-01-15");
 *
 * // Date to LocalDateTime - Date 转 LocalDateTime
 * LocalDateTime dt = DateConverter.localDateTimeConverter().convert(new Date());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable formatters) - 线程安全: 是 (不可变格式器)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Java 8+ and legacy date type conversion - Java 8+和旧版日期类型转换</li>
 *   <li>Multiple string format parsing (ISO, common patterns) - 多种字符串格式解析</li>
 *   <li>Timestamp (Long) conversion support - 时间戳（Long）转换支持</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per conversion - 每次转换 O(1)</li>
 *   <li>Space complexity: O(1) - O(1)</li>
 * </ul>
 *
 * @param <T> target date type - 目标日期类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public class DateConverter<T> implements Converter<T> {

    private static final Pattern DIGITS_ONLY_PATTERN = Pattern.compile("^\\d+$");

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyyMMdd"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    );

    private static final List<DateTimeFormatter> DATETIME_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
    );

    private static final List<DateTimeFormatter> TIME_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_TIME,
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("HHmmss")
    );

    private final Class<T> targetType;

    public DateConverter(Class<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T convert(Object value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (targetType.isInstance(value)) {
            return (T) value;
        }

        try {
            // 先转换为 Instant 作为中间类型
            Instant instant = toInstant(value);
            if (instant == null) {
                return defaultValue;
            }
            return fromInstant(instant, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Converts any object to an Instant
     * 将任意对象转换为 Instant
     */
    private Instant toInstant(Object value) {
        if (value instanceof Instant inst) {
            return inst;
        }
        if (value instanceof Date d) {
            return d.toInstant();
        }
        if (value instanceof Calendar c) {
            return c.toInstant();
        }
        if (value instanceof LocalDateTime ldt) {
            return ldt.atZone(ZoneId.systemDefault()).toInstant();
        }
        if (value instanceof LocalDate ld) {
            return ld.atStartOfDay(ZoneId.systemDefault()).toInstant();
        }
        if (value instanceof LocalTime lt) {
            return lt.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant();
        }
        if (value instanceof ZonedDateTime zdt) {
            return zdt.toInstant();
        }
        if (value instanceof OffsetDateTime odt) {
            return odt.toInstant();
        }
        if (value instanceof Long l) {
            return Instant.ofEpochMilli(l);
        }
        if (value instanceof Number n) {
            return Instant.ofEpochMilli(n.longValue());
        }
        if (value instanceof TemporalAccessor ta) {
            return Instant.from(ta);
        }

        // 字符串解析
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return null;
        }
        return parseString(str);
    }

    /**
     * Parses a string into an Instant
     * 解析字符串为 Instant
     */
    private Instant parseString(String str) {
        // 尝试 ISO Instant 格式
        try {
            return Instant.parse(str);
        } catch (DateTimeParseException ignored) {
        }

        // 尝试日期时间格式
        for (DateTimeFormatter formatter : DATETIME_FORMATTERS) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(str, formatter);
                return ldt.atZone(ZoneId.systemDefault()).toInstant();
            } catch (DateTimeParseException ignored) {
            }
        }

        // 尝试纯日期格式
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDate ld = LocalDate.parse(str, formatter);
                return ld.atStartOfDay(ZoneId.systemDefault()).toInstant();
            } catch (DateTimeParseException ignored) {
            }
        }

        // 尝试时间格式
        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                LocalTime lt = LocalTime.parse(str, formatter);
                return lt.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant();
            } catch (DateTimeParseException ignored) {
            }
        }

        // 尝试解析为时间戳数字（只有纯数字且长度超过8才当作时间戳）
        if (DIGITS_ONLY_PATTERN.matcher(str).matches() && str.length() > 8) {
            long ts = Long.parseLong(str);
            // 判断是秒还是毫秒
            if (ts < 100000000000L) {
                return Instant.ofEpochSecond(ts);
            }
            return Instant.ofEpochMilli(ts);
        }

        return null;
    }

    /**
     * Converts from an Instant to the target type
     * 从 Instant 转换为目标类型
     */
    @SuppressWarnings("unchecked")
    private T fromInstant(Instant instant, T defaultValue) {
        try {
            ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());

            if (targetType == Instant.class) {
                return (T) instant;
            }
            if (targetType == Date.class) {
                return (T) Date.from(instant);
            }
            if (targetType == java.sql.Date.class) {
                return (T) java.sql.Date.valueOf(zdt.toLocalDate());
            }
            if (targetType == java.sql.Time.class) {
                return (T) java.sql.Time.valueOf(zdt.toLocalTime());
            }
            if (targetType == Timestamp.class) {
                return (T) Timestamp.from(instant);
            }
            if (targetType == Calendar.class) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(instant.toEpochMilli());
                return (T) cal;
            }
            if (targetType == LocalDateTime.class) {
                return (T) zdt.toLocalDateTime();
            }
            if (targetType == LocalDate.class) {
                return (T) zdt.toLocalDate();
            }
            if (targetType == LocalTime.class) {
                return (T) zdt.toLocalTime();
            }
            if (targetType == ZonedDateTime.class) {
                return (T) zdt;
            }
            if (targetType == OffsetDateTime.class) {
                return (T) instant.atOffset(zdt.getOffset());
            }
            if (targetType == Long.class || targetType == long.class) {
                return (T) Long.valueOf(instant.toEpochMilli());
            }

            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // ==================== 静态工厂方法 ====================

    public static Converter<Date> dateConverter() {
        return new DateConverter<>(Date.class);
    }

    public static Converter<java.sql.Date> sqlDateConverter() {
        return new DateConverter<>(java.sql.Date.class);
    }

    public static Converter<java.sql.Time> sqlTimeConverter() {
        return new DateConverter<>(java.sql.Time.class);
    }

    public static Converter<Timestamp> timestampConverter() {
        return new DateConverter<>(Timestamp.class);
    }

    public static Converter<Calendar> calendarConverter() {
        return new DateConverter<>(Calendar.class);
    }

    public static Converter<LocalDate> localDateConverter() {
        return new DateConverter<>(LocalDate.class);
    }

    public static Converter<LocalDateTime> localDateTimeConverter() {
        return new DateConverter<>(LocalDateTime.class);
    }

    public static Converter<LocalTime> localTimeConverter() {
        return new DateConverter<>(LocalTime.class);
    }

    public static Converter<Instant> instantConverter() {
        return new DateConverter<>(Instant.class);
    }

    public static Converter<ZonedDateTime> zonedDateTimeConverter() {
        return new DateConverter<>(ZonedDateTime.class);
    }

    public static Converter<OffsetDateTime> offsetDateTimeConverter() {
        return new DateConverter<>(OffsetDateTime.class);
    }
}
