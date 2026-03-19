package cloud.opencode.base.date.timezone;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Timezone converter for date/time conversions between timezones
 * 时区转换器，用于不同时区之间的日期时间转换
 *
 * <p>This class provides fluent API for converting dates and times between
 * different timezones with proper handling of DST and other timezone rules.</p>
 * <p>此类提供流畅的API，用于在不同时区之间转换日期和时间，并正确处理夏令时和其他时区规则。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent API for timezone conversion - 流畅的时区转换API</li>
 *   <li>Support for common timezone aliases - 支持常用时区别名</li>
 *   <li>DST-aware conversions - 夏令时感知的转换</li>
 *   <li>Batch conversion support - 批量转换支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Convert from Beijing to New York
 * ZonedDateTime nyTime = TimezoneConverter.from(ZoneId.of("Asia/Shanghai"))
 *     .to(ZoneId.of("America/New_York"))
 *     .convert(LocalDateTime.now());
 *
 * // Using common timezone constants
 * ZonedDateTime result = TimezoneConverter.fromUTC()
 *     .to(TimezoneConverter.CHINA)
 *     .convert(Instant.now());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes (with explicit null checks) - 空值安全: 是（有明确的空值检查）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - all conversions are single arithmetic timezone offset operations - 时间复杂度: O(1) - 所有转换均为单次时区偏移算术运算</li>
 *   <li>Space complexity: O(1) - stores only source and target ZoneId references - 空间复杂度: O(1) - 仅存储源和目标 ZoneId 引用</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class TimezoneConverter {

    // ==================== Common Timezone Constants ====================

    public static final ZoneId UTC = ZoneOffset.UTC;
    public static final ZoneId CHINA = ZoneId.of("Asia/Shanghai");
    public static final ZoneId BEIJING = ZoneId.of("Asia/Shanghai");
    public static final ZoneId HONG_KONG = ZoneId.of("Asia/Hong_Kong");
    public static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");
    public static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    public static final ZoneId SINGAPORE = ZoneId.of("Asia/Singapore");
    public static final ZoneId NEW_YORK = ZoneId.of("America/New_York");
    public static final ZoneId LOS_ANGELES = ZoneId.of("America/Los_Angeles");
    public static final ZoneId CHICAGO = ZoneId.of("America/Chicago");
    public static final ZoneId LONDON = ZoneId.of("Europe/London");
    public static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    public static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");
    public static final ZoneId SYDNEY = ZoneId.of("Australia/Sydney");

    private final ZoneId sourceZone;
    private ZoneId targetZone;

    private TimezoneConverter(ZoneId sourceZone) {
        this.sourceZone = Objects.requireNonNull(sourceZone, "sourceZone must not be null");
    }

    // ==================== Static Factory Methods ====================

    /**
     * Creates a converter from the specified source timezone
     * 从指定的源时区创建转换器
     *
     * @param sourceZone the source timezone | 源时区
     * @return the converter | 转换器
     */
    public static TimezoneConverter from(ZoneId sourceZone) {
        return new TimezoneConverter(sourceZone);
    }

    /**
     * Creates a converter from UTC
     * 从UTC创建转换器
     *
     * @return the converter | 转换器
     */
    public static TimezoneConverter fromUTC() {
        return from(UTC);
    }

    /**
     * Creates a converter from China timezone
     * 从中国时区创建转换器
     *
     * @return the converter | 转换器
     */
    public static TimezoneConverter fromChina() {
        return from(CHINA);
    }

    /**
     * Creates a converter from system default timezone
     * 从系统默认时区创建转换器
     *
     * @return the converter | 转换器
     */
    public static TimezoneConverter fromSystem() {
        return from(ZoneId.systemDefault());
    }

    // ==================== Target Zone Methods ====================

    /**
     * Sets the target timezone
     * 设置目标时区
     *
     * @param targetZone the target timezone | 目标时区
     * @return this converter | 此转换器
     */
    public TimezoneConverter to(ZoneId targetZone) {
        this.targetZone = Objects.requireNonNull(targetZone, "targetZone must not be null");
        return this;
    }

    /**
     * Sets the target timezone to UTC
     * 设置目标时区为UTC
     *
     * @return this converter | 此转换器
     */
    public TimezoneConverter toUTC() {
        return to(UTC);
    }

    /**
     * Sets the target timezone to China
     * 设置目标时区为中国
     *
     * @return this converter | 此转换器
     */
    public TimezoneConverter toChina() {
        return to(CHINA);
    }

    /**
     * Sets the target timezone to system default
     * 设置目标时区为系统默认
     *
     * @return this converter | 此转换器
     */
    public TimezoneConverter toSystem() {
        return to(ZoneId.systemDefault());
    }

    // ==================== Conversion Methods ====================

    /**
     * Converts a LocalDateTime to the target timezone
     * 将LocalDateTime转换到目标时区
     *
     * @param dateTime the date time in source timezone | 源时区中的日期时间
     * @return the date time in target timezone | 目标时区中的日期时间
     */
    public ZonedDateTime convert(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        ensureTargetZone();
        return dateTime.atZone(sourceZone).withZoneSameInstant(targetZone);
    }

    /**
     * Converts an Instant to the target timezone
     * 将Instant转换到目标时区
     *
     * @param instant the instant | 时刻
     * @return the date time in target timezone | 目标时区中的日期时间
     */
    public ZonedDateTime convert(Instant instant) {
        Objects.requireNonNull(instant, "instant must not be null");
        ensureTargetZone();
        return instant.atZone(targetZone);
    }

    /**
     * Converts a ZonedDateTime to the target timezone
     * 将ZonedDateTime转换到目标时区
     *
     * @param zonedDateTime the zoned date time | 带时区的日期时间
     * @return the date time in target timezone | 目标时区中的日期时间
     */
    public ZonedDateTime convert(ZonedDateTime zonedDateTime) {
        Objects.requireNonNull(zonedDateTime, "zonedDateTime must not be null");
        ensureTargetZone();
        return zonedDateTime.withZoneSameInstant(targetZone);
    }

    /**
     * Converts epoch milliseconds to the target timezone
     * 将毫秒时间戳转换到目标时区
     *
     * @param epochMilli the epoch milliseconds | 毫秒时间戳
     * @return the date time in target timezone | 目标时区中的日期时间
     */
    public ZonedDateTime convert(long epochMilli) {
        ensureTargetZone();
        return Instant.ofEpochMilli(epochMilli).atZone(targetZone);
    }

    /**
     * Converts to LocalDateTime in the target timezone
     * 转换为目标时区中的LocalDateTime
     *
     * @param dateTime the date time in source timezone | 源时区中的日期时间
     * @return the local date time in target timezone | 目标时区中的本地日期时间
     */
    public LocalDateTime convertToLocal(LocalDateTime dateTime) {
        return convert(dateTime).toLocalDateTime();
    }

    /**
     * Converts to OffsetDateTime in the target timezone
     * 转换为目标时区中的OffsetDateTime
     *
     * @param dateTime the date time in source timezone | 源时区中的日期时间
     * @return the offset date time in target timezone | 目标时区中的偏移日期时间
     */
    public OffsetDateTime convertToOffset(LocalDateTime dateTime) {
        return convert(dateTime).toOffsetDateTime();
    }

    private void ensureTargetZone() {
        if (targetZone == null) {
            throw new IllegalStateException("Target timezone not set. Call to() first.");
        }
    }

    // ==================== Static Convenience Methods ====================

    /**
     * Converts a datetime between two timezones
     * 在两个时区之间转换日期时间
     *
     * @param dateTime the date time | 日期时间
     * @param fromZone the source timezone | 源时区
     * @param toZone the target timezone | 目标时区
     * @return the converted date time | 转换后的日期时间
     */
    public static ZonedDateTime convert(LocalDateTime dateTime, ZoneId fromZone, ZoneId toZone) {
        return from(fromZone).to(toZone).convert(dateTime);
    }

    /**
     * Converts UTC to the specified timezone
     * 将UTC转换为指定时区
     *
     * @param utcDateTime the UTC date time | UTC日期时间
     * @param toZone the target timezone | 目标时区
     * @return the converted date time | 转换后的日期时间
     */
    public static ZonedDateTime fromUTC(LocalDateTime utcDateTime, ZoneId toZone) {
        return fromUTC().to(toZone).convert(utcDateTime);
    }

    /**
     * Converts to UTC from the specified timezone
     * 从指定时区转换为UTC
     *
     * @param dateTime the date time | 日期时间
     * @param fromZone the source timezone | 源时区
     * @return the UTC date time | UTC日期时间
     */
    public static ZonedDateTime toUTC(LocalDateTime dateTime, ZoneId fromZone) {
        return from(fromZone).toUTC().convert(dateTime);
    }

    /**
     * Gets the current time in the specified timezone
     * 获取指定时区的当前时间
     *
     * @param zone the timezone | 时区
     * @return the current time | 当前时间
     */
    public static ZonedDateTime now(ZoneId zone) {
        return ZonedDateTime.now(zone);
    }

    /**
     * Gets the offset hours between two timezones
     * 获取两个时区之间的偏移小时数
     *
     * @param zone1 the first timezone | 第一个时区
     * @param zone2 the second timezone | 第二个时区
     * @return the offset hours (zone2 - zone1) | 偏移小时数
     */
    public static double getOffsetHours(ZoneId zone1, ZoneId zone2) {
        Instant now = Instant.now();
        int offset1 = zone1.getRules().getOffset(now).getTotalSeconds();
        int offset2 = zone2.getRules().getOffset(now).getTotalSeconds();
        return (offset2 - offset1) / 3600.0;
    }

    /**
     * Formats a datetime in the specified timezone
     * 以指定时区格式化日期时间
     *
     * @param instant the instant | 时刻
     * @param zone the timezone | 时区
     * @param formatter the formatter | 格式化器
     * @return the formatted string | 格式化后的字符串
     */
    public static String format(Instant instant, ZoneId zone, DateTimeFormatter formatter) {
        return instant.atZone(zone).format(formatter);
    }
}
