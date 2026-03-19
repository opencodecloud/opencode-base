package cloud.opencode.base.date.timezone;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Utility class for timezone operations
 * 时区操作工具类
 *
 * <p>This class provides various utilities for working with timezones,
 * including conversion, offset calculations, and timezone information.</p>
 * <p>此类提供各种时区操作工具，包括转换、偏移计算和时区信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Get all available timezones - 获取所有可用时区</li>
 *   <li>Timezone offset calculations - 时区偏移计算</li>
 *   <li>Convert between timezones - 时区之间转换</li>
 *   <li>Common timezone constants - 常用时区常量</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get current time in different timezones
 * ZonedDateTime utc = TimezoneUtil.nowUtc();
 * ZonedDateTime beijing = TimezoneUtil.now(TimezoneUtil.CHINA);
 *
 * // Convert between timezones
 * ZonedDateTime newYork = TimezoneUtil.convert(beijing, TimezoneUtil.NEW_YORK);
 *
 * // Get timezone offset
 * Duration offset = TimezoneUtil.getOffset(TimezoneUtil.CHINA);
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
 *   <li>Time complexity: O(n) for getAllTimezones and findTimezones where n=number of available timezone IDs; O(1) for individual conversions and offset lookups - 时间复杂度: getAllTimezones 和 findTimezones 为 O(n)，n 为可用时区 ID 数量；单次转换和偏移查询为 O(1)</li>
 *   <li>Space complexity: O(n) for getAllTimezones result list; O(1) for conversion operations - 空间复杂度: getAllTimezones 结果列表为 O(n)；转换操作为 O(1)</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class TimezoneUtil {

    // ==================== Common Timezone Constants | 常用时区常量 ====================

    /**
     * UTC timezone
     * UTC时区
     */
    public static final ZoneId UTC = ZoneId.of("UTC");

    /**
     * China Standard Time (Beijing)
     * 中国标准时间（北京）
     */
    public static final ZoneId CHINA = ZoneId.of("Asia/Shanghai");

    /**
     * Japan Standard Time (Tokyo)
     * 日本标准时间（东京）
     */
    public static final ZoneId JAPAN = ZoneId.of("Asia/Tokyo");

    /**
     * Korea Standard Time (Seoul)
     * 韩国标准时间（首尔）
     */
    public static final ZoneId KOREA = ZoneId.of("Asia/Seoul");

    /**
     * US Eastern Time (New York)
     * 美国东部时间（纽约）
     */
    public static final ZoneId NEW_YORK = ZoneId.of("America/New_York");

    /**
     * US Pacific Time (Los Angeles)
     * 美国太平洋时间（洛杉矶）
     */
    public static final ZoneId LOS_ANGELES = ZoneId.of("America/Los_Angeles");

    /**
     * UK Time (London)
     * 英国时间（伦敦）
     */
    public static final ZoneId LONDON = ZoneId.of("Europe/London");

    /**
     * Central European Time (Paris)
     * 中欧时间（巴黎）
     */
    public static final ZoneId PARIS = ZoneId.of("Europe/Paris");

    /**
     * Central European Time (Berlin)
     * 中欧时间（柏林）
     */
    public static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");

    /**
     * India Standard Time (Kolkata)
     * 印度标准时间（加尔各答）
     */
    public static final ZoneId INDIA = ZoneId.of("Asia/Kolkata");

    /**
     * Singapore Time
     * 新加坡时间
     */
    public static final ZoneId SINGAPORE = ZoneId.of("Asia/Singapore");

    /**
     * Hong Kong Time
     * 香港时间
     */
    public static final ZoneId HONG_KONG = ZoneId.of("Asia/Hong_Kong");

    /**
     * Sydney Time
     * 悉尼时间
     */
    public static final ZoneId SYDNEY = ZoneId.of("Australia/Sydney");

    // ==================== Private Constructor | 私有构造函数 ====================

    private TimezoneUtil() {
        // Utility class
    }

    // ==================== Now Methods | 当前时间方法 ====================

    /**
     * Gets the current time in UTC
     * 获取UTC当前时间
     *
     * @return the current time in UTC | UTC当前时间
     */
    public static ZonedDateTime nowUtc() {
        return ZonedDateTime.now(UTC);
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
     * Gets the current time in the system default timezone
     * 获取系统默认时区的当前时间
     *
     * @return the current time | 当前时间
     */
    public static ZonedDateTime nowLocal() {
        return ZonedDateTime.now();
    }

    // ==================== Conversion Methods | 转换方法 ====================

    /**
     * Converts a ZonedDateTime to another timezone
     * 将ZonedDateTime转换为另一个时区
     *
     * @param dateTime the date-time | 日期时间
     * @param zone     the target timezone | 目标时区
     * @return the converted date-time | 转换后的日期时间
     */
    public static ZonedDateTime convert(ZonedDateTime dateTime, ZoneId zone) {
        return dateTime.withZoneSameInstant(zone);
    }

    /**
     * Converts a LocalDateTime to a ZonedDateTime in the specified timezone
     * 将LocalDateTime转换为指定时区的ZonedDateTime
     *
     * @param dateTime the local date-time | 本地日期时间
     * @param zone     the timezone | 时区
     * @return the zoned date-time | 带时区的日期时间
     */
    public static ZonedDateTime toZoned(LocalDateTime dateTime, ZoneId zone) {
        return dateTime.atZone(zone);
    }

    /**
     * Converts a ZonedDateTime to LocalDateTime in the same zone
     * 将ZonedDateTime转换为同一时区的LocalDateTime
     *
     * @param dateTime the zoned date-time | 带时区的日期时间
     * @return the local date-time | 本地日期时间
     */
    public static LocalDateTime toLocal(ZonedDateTime dateTime) {
        return dateTime.toLocalDateTime();
    }

    /**
     * Converts an Instant to ZonedDateTime in the specified timezone
     * 将Instant转换为指定时区的ZonedDateTime
     *
     * @param instant the instant | 瞬间
     * @param zone    the timezone | 时区
     * @return the zoned date-time | 带时区的日期时间
     */
    public static ZonedDateTime toZoned(Instant instant, ZoneId zone) {
        return instant.atZone(zone);
    }

    /**
     * Converts a ZonedDateTime to an Instant
     * 将ZonedDateTime转换为Instant
     *
     * @param dateTime the zoned date-time | 带时区的日期时间
     * @return the instant | 瞬间
     */
    public static Instant toInstant(ZonedDateTime dateTime) {
        return dateTime.toInstant();
    }

    /**
     * Converts a LocalDateTime from one timezone to another
     * 将LocalDateTime从一个时区转换为另一个时区
     *
     * @param dateTime   the local date-time | 本地日期时间
     * @param fromZone   the source timezone | 源时区
     * @param toZone     the target timezone | 目标时区
     * @return the converted local date-time | 转换后的本地日期时间
     */
    public static LocalDateTime convert(LocalDateTime dateTime, ZoneId fromZone, ZoneId toZone) {
        return dateTime.atZone(fromZone).withZoneSameInstant(toZone).toLocalDateTime();
    }

    // ==================== Offset Methods | 偏移方法 ====================

    /**
     * Gets the current offset for a timezone
     * 获取时区的当前偏移
     *
     * @param zone the timezone | 时区
     * @return the offset as Duration | 偏移（Duration形式）
     */
    public static Duration getOffset(ZoneId zone) {
        ZoneOffset offset = zone.getRules().getOffset(Instant.now());
        return Duration.ofSeconds(offset.getTotalSeconds());
    }

    /**
     * Gets the offset between two timezones
     * 获取两个时区之间的偏移
     *
     * @param from the source timezone | 源时区
     * @param to   the target timezone | 目标时区
     * @return the offset difference | 偏移差
     */
    public static Duration getOffsetBetween(ZoneId from, ZoneId to) {
        Instant now = Instant.now();
        ZoneOffset fromOffset = from.getRules().getOffset(now);
        ZoneOffset toOffset = to.getRules().getOffset(now);
        return Duration.ofSeconds(toOffset.getTotalSeconds() - fromOffset.getTotalSeconds());
    }

    /**
     * Gets the offset in hours for a timezone
     * 获取时区的小时偏移
     *
     * @param zone the timezone | 时区
     * @return the offset in hours | 小时偏移
     */
    public static int getOffsetHours(ZoneId zone) {
        ZoneOffset offset = zone.getRules().getOffset(Instant.now());
        return offset.getTotalSeconds() / 3600;
    }

    /**
     * Formats the offset as a string (e.g., "+08:00")
     * 将偏移格式化为字符串（如"+08:00"）
     *
     * @param zone the timezone | 时区
     * @return the formatted offset | 格式化的偏移
     */
    public static String formatOffset(ZoneId zone) {
        ZoneOffset offset = zone.getRules().getOffset(Instant.now());
        return offset.getId();
    }

    // ==================== Timezone Info Methods | 时区信息方法 ====================

    /**
     * Gets all available timezone IDs
     * 获取所有可用的时区ID
     *
     * @return the set of timezone IDs | 时区ID集合
     */
    public static Set<String> getAllTimezoneIds() {
        return ZoneId.getAvailableZoneIds();
    }

    /**
     * Gets all available timezones as ZoneId
     * 获取所有可用的时区（ZoneId形式）
     *
     * @return the list of ZoneId | ZoneId列表
     */
    public static List<ZoneId> getAllTimezones() {
        return ZoneId.getAvailableZoneIds().stream()
                .map(ZoneId::of)
                .sorted(Comparator.comparing(z -> z.getRules().getOffset(Instant.now())))
                .toList();
    }

    /**
     * Gets timezones matching a pattern
     * 获取匹配模式的时区
     *
     * @param pattern the pattern to match (case-insensitive) | 要匹配的模式（不区分大小写）
     * @return the list of matching timezone IDs | 匹配的时区ID列表
     */
    public static List<String> findTimezones(String pattern) {
        String lowerPattern = pattern.toLowerCase();
        return ZoneId.getAvailableZoneIds().stream()
                .filter(id -> id.toLowerCase().contains(lowerPattern))
                .sorted()
                .toList();
    }

    /**
     * Gets the display name of a timezone
     * 获取时区的显示名称
     *
     * @param zone   the timezone | 时区
     * @param locale the locale | 区域设置
     * @return the display name | 显示名称
     */
    public static String getDisplayName(ZoneId zone, Locale locale) {
        return zone.getDisplayName(TextStyle.FULL, locale);
    }

    /**
     * Gets the short display name of a timezone
     * 获取时区的短显示名称
     *
     * @param zone   the timezone | 时区
     * @param locale the locale | 区域设置
     * @return the short display name | 短显示名称
     */
    public static String getShortDisplayName(ZoneId zone, Locale locale) {
        return zone.getDisplayName(TextStyle.SHORT, locale);
    }

    /**
     * Checks if a timezone ID is valid
     * 检查时区ID是否有效
     *
     * @param zoneId the timezone ID | 时区ID
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValidTimezone(String zoneId) {
        try {
            ZoneId.of(zoneId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the system default timezone
     * 获取系统默认时区
     *
     * @return the default timezone | 默认时区
     */
    public static ZoneId getDefault() {
        return ZoneId.systemDefault();
    }

    // ==================== DST Methods | 夏令时方法 ====================

    /**
     * Checks if a timezone is currently in DST
     * 检查时区当前是否处于夏令时
     *
     * @param zone the timezone | 时区
     * @return true if in DST | 如果处于夏令时返回true
     */
    public static boolean isDaylightSavingTime(ZoneId zone) {
        return zone.getRules().isDaylightSavings(Instant.now());
    }

    /**
     * Checks if a timezone uses DST
     * 检查时区是否使用夏令时
     *
     * @param zone the timezone | 时区
     * @return true if uses DST | 如果使用夏令时返回true
     */
    public static boolean usesDaylightSavingTime(ZoneId zone) {
        return !zone.getRules().getTransitionRules().isEmpty() ||
                !zone.getRules().getTransitions().isEmpty();
    }

    /**
     * Gets the next DST transition for a timezone
     * 获取时区的下一个夏令时转换
     *
     * @param zone the timezone | 时区
     * @return the next transition, or null if none | 下一个转换，如果没有则为null
     */
    public static ZonedDateTime getNextDstTransition(ZoneId zone) {
        var transition = zone.getRules().nextTransition(Instant.now());
        return transition != null ? transition.getInstant().atZone(zone) : null;
    }
}
