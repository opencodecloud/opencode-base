package cloud.opencode.base.date.formatter;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Objects;

/**
 * Formats temporal objects as relative time strings (e.g., "3 hours ago", "in 2 days")
 * 将时间对象格式化为相对时间字符串（如"3小时前"、"2天后"）
 *
 * <p>This class provides human-friendly relative time formatting, similar to what you see
 * in social media applications ("just now", "5 minutes ago", "yesterday").</p>
 * <p>此类提供人性化的相对时间格式化，类似于社交媒体应用中看到的
 * （"刚刚"、"5分钟前"、"昨天"）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Smart relative time formatting - 智能相对时间格式化</li>
 *   <li>Support for past and future times - 支持过去和未来时间</li>
 *   <li>Multiple language support (EN/CN) - 多语言支持（英文/中文）</li>
 *   <li>Configurable thresholds - 可配置的阈值</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LocalDateTime time = LocalDateTime.now().minusMinutes(5);
 * String relative = RelativeTimeFormatter.format(time);
 * // "5 minutes ago"
 *
 * String relativeCn = RelativeTimeFormatter.formatChinese(time);
 * // "5分钟前"
 *
 * // Future time
 * LocalDateTime future = LocalDateTime.now().plusDays(2);
 * String inFuture = RelativeTimeFormatter.format(future);
 * // "in 2 days"
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
 *   <li>Time complexity: O(1) - all format methods compare against a fixed set of time thresholds - 时间复杂度: O(1) - 所有 format 方法均与固定数量的时间阈值进行比较</li>
 *   <li>Space complexity: O(1) - output string size is bounded by fixed message templates - 空间复杂度: O(1) - 输出字符串大小由固定消息模板限定</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class RelativeTimeFormatter {

    // ==================== Constants | 常量 ====================

    private static final long SECONDS_PER_MINUTE = 60;
    private static final long SECONDS_PER_HOUR = 3600;
    private static final long SECONDS_PER_DAY = 86400;
    private static final long SECONDS_PER_WEEK = 604800;
    private static final long SECONDS_PER_MONTH = 2592000; // 30 days
    private static final long SECONDS_PER_YEAR = 31536000; // 365 days

    private static final long THRESHOLD_JUST_NOW = 60; // 1 minute
    private static final long THRESHOLD_RECENT = 3600; // 1 hour

    // ==================== Private Constructor | 私有构造函数 ====================

    private RelativeTimeFormatter() {
        // Utility class
    }

    // ==================== English Formatting | 英文格式化 ====================

    /**
     * Formats a temporal as a relative time string in English
     * 将时间格式化为英文相对时间字符串
     *
     * @param temporal the temporal to format | 要格式化的时间
     * @return the relative time string | 相对时间字符串
     */
    public static String format(Temporal temporal) {
        return format(temporal, LocalDateTime.now());
    }

    /**
     * Formats a temporal as a relative time string relative to a reference time
     * 相对于参考时间将时间格式化为相对时间字符串
     *
     * @param temporal  the temporal to format | 要格式化的时间
     * @param reference the reference time | 参考时间
     * @return the relative time string | 相对时间字符串
     */
    public static String format(Temporal temporal, Temporal reference) {
        Objects.requireNonNull(temporal, "temporal must not be null");
        Objects.requireNonNull(reference, "reference must not be null");

        long seconds = getSecondsBetween(temporal, reference);
        boolean past = seconds > 0;
        long absSeconds = Math.abs(seconds);

        String timeStr = formatTime(absSeconds, false);

        if (timeStr.equals("just now") || timeStr.equals("now")) {
            return timeStr;
        }

        return past ? timeStr + " ago" : "in " + timeStr;
    }

    /**
     * Formats an Instant as a relative time string
     * 将Instant格式化为相对时间字符串
     *
     * @param instant the instant to format | 要格式化的Instant
     * @return the relative time string | 相对时间字符串
     */
    public static String format(Instant instant) {
        return format(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }

    /**
     * Formats a Duration as a relative time string
     * 将Duration格式化为相对时间字符串
     *
     * @param duration the duration to format | 要格式化的Duration
     * @return the relative time string | 相对时间字符串
     */
    public static String formatDuration(Duration duration) {
        Objects.requireNonNull(duration, "duration must not be null");
        long seconds = Math.abs(duration.getSeconds());
        return formatTime(seconds, false);
    }

    // ==================== Chinese Formatting | 中文格式化 ====================

    /**
     * Formats a temporal as a relative time string in Chinese
     * 将时间格式化为中文相对时间字符串
     *
     * @param temporal the temporal to format | 要格式化的时间
     * @return the relative time string in Chinese | 中文相对时间字符串
     */
    public static String formatChinese(Temporal temporal) {
        return formatChinese(temporal, LocalDateTime.now());
    }

    /**
     * Formats a temporal as a relative time string in Chinese relative to reference
     * 相对于参考时间将时间格式化为中文相对时间字符串
     *
     * @param temporal  the temporal to format | 要格式化的时间
     * @param reference the reference time | 参考时间
     * @return the relative time string in Chinese | 中文相对时间字符串
     */
    public static String formatChinese(Temporal temporal, Temporal reference) {
        Objects.requireNonNull(temporal, "temporal must not be null");
        Objects.requireNonNull(reference, "reference must not be null");

        long seconds = getSecondsBetween(temporal, reference);
        boolean past = seconds > 0;
        long absSeconds = Math.abs(seconds);

        String timeStr = formatTime(absSeconds, true);

        if (timeStr.equals("刚刚") || timeStr.equals("现在")) {
            return timeStr;
        }

        return past ? timeStr + "前" : timeStr + "后";
    }

    /**
     * Formats an Instant as a relative time string in Chinese
     * 将Instant格式化为中文相对时间字符串
     *
     * @param instant the instant to format | 要格式化的Instant
     * @return the relative time string in Chinese | 中文相对时间字符串
     */
    public static String formatChinese(Instant instant) {
        return formatChinese(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }

    /**
     * Formats a Duration as a relative time string in Chinese
     * 将Duration格式化为中文相对时间字符串
     *
     * @param duration the duration to format | 要格式化的Duration
     * @return the relative time string in Chinese | 中文相对时间字符串
     */
    public static String formatDurationChinese(Duration duration) {
        Objects.requireNonNull(duration, "duration must not be null");
        long seconds = Math.abs(duration.getSeconds());
        return formatTime(seconds, true);
    }

    // ==================== Smart Formatting | 智能格式化 ====================

    /**
     * Formats using smart relative time with day-specific terms
     * 使用智能相对时间格式化，包含日期特定术语
     *
     * @param dateTime the date-time to format | 要格式化的日期时间
     * @return the smart relative time string | 智能相对时间字符串
     */
    public static String formatSmart(LocalDateTime dateTime) {
        return formatSmart(dateTime, false);
    }

    /**
     * Formats using smart relative time with day-specific terms in Chinese
     * 使用智能相对时间格式化，包含中文日期特定术语
     *
     * @param dateTime the date-time to format | 要格式化的日期时间
     * @return the smart relative time string in Chinese | 中文智能相对时间字符串
     */
    public static String formatSmartChinese(LocalDateTime dateTime) {
        return formatSmart(dateTime, true);
    }

    private static String formatSmart(LocalDateTime dateTime, boolean chinese) {
        LocalDate today = LocalDate.now();
        LocalDate date = dateTime.toLocalDate();

        long daysDiff = ChronoUnit.DAYS.between(date, today);

        if (daysDiff == 0) {
            // Today - use time-based relative formatting
            return chinese ? formatChinese(dateTime) : format(dateTime);
        } else if (daysDiff == 1) {
            return chinese ? "昨天" : "yesterday";
        } else if (daysDiff == -1) {
            return chinese ? "明天" : "tomorrow";
        } else if (daysDiff == 2) {
            return chinese ? "前天" : "2 days ago";
        } else if (daysDiff == -2) {
            return chinese ? "后天" : "in 2 days";
        } else if (daysDiff > 0 && daysDiff <= 7) {
            return chinese ? daysDiff + "天前" : daysDiff + " days ago";
        } else if (daysDiff < 0 && daysDiff >= -7) {
            long abs = Math.abs(daysDiff);
            return chinese ? abs + "天后" : "in " + abs + " days";
        } else {
            // Fall back to regular formatting
            return chinese ? formatChinese(dateTime) : format(dateTime);
        }
    }

    // ==================== Helper Methods | 辅助方法 ====================

    /**
     * Gets the seconds between two temporals
     */
    private static long getSecondsBetween(Temporal from, Temporal to) {
        if (from instanceof LocalDateTime fromDt && to instanceof LocalDateTime toDt) {
            return ChronoUnit.SECONDS.between(fromDt, toDt);
        }
        if (from instanceof LocalDate fromDate && to instanceof LocalDate toDate) {
            return ChronoUnit.DAYS.between(fromDate, toDate) * SECONDS_PER_DAY;
        }
        if (from instanceof LocalDate fromDate && to instanceof LocalDateTime toDt) {
            return ChronoUnit.SECONDS.between(fromDate.atStartOfDay(), toDt);
        }
        if (from instanceof LocalDateTime fromDt && to instanceof LocalDate toDate) {
            return ChronoUnit.SECONDS.between(fromDt, toDate.atStartOfDay());
        }

        throw new UnsupportedOperationException("Unsupported temporal types: " +
                from.getClass().getSimpleName() + " and " + to.getClass().getSimpleName());
    }

    /**
     * Formats seconds as a time string
     */
    private static String formatTime(long seconds, boolean chinese) {
        if (seconds < THRESHOLD_JUST_NOW) {
            return chinese ? "刚刚" : "just now";
        }

        if (seconds < SECONDS_PER_MINUTE) {
            return chinese ?
                    seconds + "秒" :
                    seconds + " second" + (seconds > 1 ? "s" : "");
        }

        if (seconds < SECONDS_PER_HOUR) {
            long minutes = seconds / SECONDS_PER_MINUTE;
            return chinese ?
                    minutes + "分钟" :
                    minutes + " minute" + (minutes > 1 ? "s" : "");
        }

        if (seconds < SECONDS_PER_DAY) {
            long hours = seconds / SECONDS_PER_HOUR;
            return chinese ?
                    hours + "小时" :
                    hours + " hour" + (hours > 1 ? "s" : "");
        }

        if (seconds < SECONDS_PER_WEEK) {
            long days = seconds / SECONDS_PER_DAY;
            return chinese ?
                    days + "天" :
                    days + " day" + (days > 1 ? "s" : "");
        }

        if (seconds < SECONDS_PER_MONTH) {
            long weeks = seconds / SECONDS_PER_WEEK;
            return chinese ?
                    weeks + "周" :
                    weeks + " week" + (weeks > 1 ? "s" : "");
        }

        if (seconds < SECONDS_PER_YEAR) {
            long months = seconds / SECONDS_PER_MONTH;
            return chinese ?
                    months + "个月" :
                    months + " month" + (months > 1 ? "s" : "");
        }

        long years = seconds / SECONDS_PER_YEAR;
        return chinese ?
                years + "年" :
                years + " year" + (years > 1 ? "s" : "");
    }

    // ==================== Compact Formatting | 紧凑格式化 ====================

    /**
     * Formats as a compact relative time string (e.g., "5m", "3h", "2d")
     * 格式化为紧凑的相对时间字符串（如"5m"、"3h"、"2d"）
     *
     * @param temporal the temporal to format | 要格式化的时间
     * @return the compact relative time string | 紧凑相对时间字符串
     */
    public static String formatCompact(Temporal temporal) {
        Objects.requireNonNull(temporal, "temporal must not be null");

        long seconds = Math.abs(getSecondsBetween(temporal, LocalDateTime.now()));

        if (seconds < SECONDS_PER_MINUTE) {
            return seconds + "s";
        }
        if (seconds < SECONDS_PER_HOUR) {
            return (seconds / SECONDS_PER_MINUTE) + "m";
        }
        if (seconds < SECONDS_PER_DAY) {
            return (seconds / SECONDS_PER_HOUR) + "h";
        }
        if (seconds < SECONDS_PER_WEEK) {
            return (seconds / SECONDS_PER_DAY) + "d";
        }
        if (seconds < SECONDS_PER_MONTH) {
            return (seconds / SECONDS_PER_WEEK) + "w";
        }
        if (seconds < SECONDS_PER_YEAR) {
            return (seconds / SECONDS_PER_MONTH) + "mo";
        }
        return (seconds / SECONDS_PER_YEAR) + "y";
    }
}
