package cloud.opencode.base.date.formatter;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Formats and parses Period and Duration objects as human-readable strings
 * 将Period和Duration对象格式化和解析为人类可读的字符串
 *
 * <p>This class provides methods to format periods and durations into
 * various human-readable formats, and parse strings back into temporal amounts.</p>
 * <p>此类提供将周期和持续时间格式化为各种人类可读格式的方法，
 * 并将字符串解析回时间量。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Format Period as readable string - 将Period格式化为可读字符串</li>
 *   <li>Format Duration as readable string - 将Duration格式化为可读字符串</li>
 *   <li>Parse human-readable strings - 解析人类可读的字符串</li>
 *   <li>Multiple format styles (full, short, compact) - 多种格式样式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Period period = Period.of(1, 2, 15);
 * String formatted = PeriodFormatter.format(period);
 * // "1 year, 2 months, 15 days"
 *
 * Duration duration = Duration.ofHours(25).plusMinutes(30);
 * String durationStr = PeriodFormatter.format(duration);
 * // "1 day, 1 hour, 30 minutes"
 *
 * // Parse back
 * Period parsed = PeriodFormatter.parsePeriod("2 years 3 months");
 * Duration parsedDur = PeriodFormatter.parseDuration("5h 30m");
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
 *   <li>Time complexity: O(1) for format methods (fixed number of period/duration fields); O(n) for parsePeriod/parseDuration where n=input string length (regex match) - 时间复杂度: format 方法为 O(1)（固定数量的周期/持续时间字段）；parsePeriod/parseDuration 为 O(n)，n 为输入字符串长度（正则匹配）</li>
 *   <li>Space complexity: O(1) - compiled regex patterns are cached as static fields - 空间复杂度: O(1) - 编译的正则表达式模式缓存为静态字段</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class PeriodFormatter {

    // ==================== Patterns | 模式 ====================

    private static final Pattern PERIOD_PATTERN = Pattern.compile(
            "(?:(\\d+)\\s*(?:years?|yrs?|y))?" +
                    "\\s*(?:(\\d+)\\s*(?:months?|mons?|mo|M))?" +
                    "\\s*(?:(\\d+)\\s*(?:weeks?|wks?|w))?" +
                    "\\s*(?:(\\d+)\\s*(?:days?|d))?",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DURATION_PATTERN = Pattern.compile(
            "(?:(\\d+)\\s*(?:days?|d))?" +
                    "\\s*(?:(\\d+)\\s*(?:hours?|hrs?|h))?" +
                    "\\s*(?:(\\d+)\\s*(?:minutes?|mins?|m))?" +
                    "\\s*(?:(\\d+)\\s*(?:seconds?|secs?|s))?",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CHINESE_PERIOD_PATTERN = Pattern.compile(
            "(?:(\\d+)年)?\\s*(?:(\\d+)个?月)?\\s*(?:(\\d+)周)?\\s*(?:(\\d+)天)?"
    );

    private static final Pattern CHINESE_DURATION_PATTERN = Pattern.compile(
            "(?:(\\d+)天)?\\s*(?:(\\d+)小时)?\\s*(?:(\\d+)分钟?)?\\s*(?:(\\d+)秒)?"
    );

    private static final Pattern TIME_FORMAT_PATTERN = Pattern.compile("-?\\d{1,2}:\\d{2}(:\\d{2})?");

    // ==================== Private Constructor | 私有构造函数 ====================

    private PeriodFormatter() {
        // Utility class
    }

    // ==================== Period Formatting | Period格式化 ====================

    /**
     * Formats a Period as a human-readable string
     * 将Period格式化为人类可读的字符串
     *
     * @param period the period to format | 要格式化的Period
     * @return the formatted string | 格式化的字符串
     */
    public static String format(Period period) {
        Objects.requireNonNull(period, "period must not be null");

        if (period.isZero()) {
            return "0 days";
        }

        StringBuilder sb = new StringBuilder();
        appendIfPositive(sb, period.getYears(), "year", "years");
        appendIfPositive(sb, period.getMonths(), "month", "months");
        appendIfPositive(sb, period.getDays(), "day", "days");

        return sb.toString();
    }

    /**
     * Formats a Period as a Chinese string
     * 将Period格式化为中文字符串
     *
     * @param period the period to format | 要格式化的Period
     * @return the formatted string in Chinese | 中文格式化的字符串
     */
    public static String formatChinese(Period period) {
        Objects.requireNonNull(period, "period must not be null");

        if (period.isZero()) {
            return "0天";
        }

        StringBuilder sb = new StringBuilder();
        if (period.getYears() != 0) {
            sb.append(period.getYears()).append("年");
        }
        if (period.getMonths() != 0) {
            sb.append(period.getMonths()).append("个月");
        }
        if (period.getDays() != 0) {
            sb.append(period.getDays()).append("天");
        }

        return sb.toString();
    }

    /**
     * Formats a Period in short form (e.g., "1y 2m 15d")
     * 将Period格式化为短格式（如"1y 2m 15d"）
     *
     * @param period the period to format | 要格式化的Period
     * @return the short formatted string | 短格式字符串
     */
    public static String formatShort(Period period) {
        Objects.requireNonNull(period, "period must not be null");

        if (period.isZero()) {
            return "0d";
        }

        StringBuilder sb = new StringBuilder();
        if (period.getYears() != 0) {
            sb.append(period.getYears()).append("y ");
        }
        if (period.getMonths() != 0) {
            sb.append(period.getMonths()).append("m ");
        }
        if (period.getDays() != 0) {
            sb.append(period.getDays()).append("d");
        }

        return sb.toString().trim();
    }

    /**
     * Formats a Period in compact form (e.g., "P1Y2M15D")
     * 将Period格式化为紧凑格式（如"P1Y2M15D"）
     *
     * @param period the period to format | 要格式化的Period
     * @return the compact formatted string | 紧凑格式字符串
     */
    public static String formatCompact(Period period) {
        return period.toString();
    }

    // ==================== Duration Formatting | Duration格式化 ====================

    /**
     * Formats a Duration as a human-readable string
     * 将Duration格式化为人类可读的字符串
     *
     * @param duration the duration to format | 要格式化的Duration
     * @return the formatted string | 格式化的字符串
     */
    public static String format(Duration duration) {
        Objects.requireNonNull(duration, "duration must not be null");

        if (duration.isZero()) {
            return "0 seconds";
        }

        long totalSeconds = duration.getSeconds();
        boolean negative = totalSeconds < 0;
        totalSeconds = Math.abs(totalSeconds);

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (negative) {
            sb.append("-");
        }

        appendIfPositive(sb, (int) days, "day", "days");
        appendIfPositive(sb, (int) hours, "hour", "hours");
        appendIfPositive(sb, (int) minutes, "minute", "minutes");
        appendIfPositive(sb, (int) seconds, "second", "seconds");

        return sb.toString();
    }

    /**
     * Formats a Duration as a Chinese string
     * 将Duration格式化为中文字符串
     *
     * @param duration the duration to format | 要格式化的Duration
     * @return the formatted string in Chinese | 中文格式化的字符串
     */
    public static String formatChinese(Duration duration) {
        Objects.requireNonNull(duration, "duration must not be null");

        if (duration.isZero()) {
            return "0秒";
        }

        long totalSeconds = duration.getSeconds();
        boolean negative = totalSeconds < 0;
        totalSeconds = Math.abs(totalSeconds);

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (negative) {
            sb.append("负");
        }

        if (days > 0) sb.append(days).append("天");
        if (hours > 0) sb.append(hours).append("小时");
        if (minutes > 0) sb.append(minutes).append("分钟");
        if (seconds > 0) sb.append(seconds).append("秒");

        return sb.toString();
    }

    /**
     * Formats a Duration in short form (e.g., "1d 2h 30m 15s")
     * 将Duration格式化为短格式（如"1d 2h 30m 15s"）
     *
     * @param duration the duration to format | 要格式化的Duration
     * @return the short formatted string | 短格式字符串
     */
    public static String formatShort(Duration duration) {
        Objects.requireNonNull(duration, "duration must not be null");

        if (duration.isZero()) {
            return "0s";
        }

        long totalSeconds = duration.getSeconds();
        boolean negative = totalSeconds < 0;
        totalSeconds = Math.abs(totalSeconds);

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (negative) {
            sb.append("-");
        }

        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    /**
     * Formats a Duration as time format (HH:MM:SS)
     * 将Duration格式化为时间格式（HH:MM:SS）
     *
     * @param duration the duration to format | 要格式化的Duration
     * @return the time formatted string | 时间格式字符串
     */
    public static String formatTime(Duration duration) {
        Objects.requireNonNull(duration, "duration must not be null");

        long totalSeconds = Math.abs(duration.getSeconds());
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String sign = duration.isNegative() ? "-" : "";
        return String.format("%s%02d:%02d:%02d", sign, hours, minutes, seconds);
    }

    /**
     * Formats a Duration in compact form (e.g., "PT1H30M")
     * 将Duration格式化为紧凑格式（如"PT1H30M"）
     *
     * @param duration the duration to format | 要格式化的Duration
     * @return the compact formatted string | 紧凑格式字符串
     */
    public static String formatCompact(Duration duration) {
        return duration.toString();
    }

    // ==================== Parsing | 解析 ====================

    /**
     * Parses a string into a Period
     * 将字符串解析为Period
     *
     * @param text the text to parse | 要解析的文本
     * @return the parsed Period | 解析的Period
     * @throws IllegalArgumentException if the text cannot be parsed | 如果文本无法解析则抛出异常
     */
    public static Period parsePeriod(String text) {
        Objects.requireNonNull(text, "text must not be null");

        String trimmed = text.trim();

        // Try ISO format first
        if (trimmed.startsWith("P") || trimmed.startsWith("-P")) {
            return Period.parse(trimmed);
        }

        // Try Chinese format
        Matcher cnMatcher = CHINESE_PERIOD_PATTERN.matcher(trimmed);
        if (cnMatcher.matches()) {
            int years = parseGroup(cnMatcher.group(1));
            int months = parseGroup(cnMatcher.group(2));
            int weeks = parseGroup(cnMatcher.group(3));
            int days = parseGroup(cnMatcher.group(4)) + weeks * 7;

            if (years > 0 || months > 0 || days > 0) {
                return Period.of(years, months, days);
            }
        }

        // Try English format
        Matcher enMatcher = PERIOD_PATTERN.matcher(trimmed);
        if (enMatcher.matches()) {
            int years = parseGroup(enMatcher.group(1));
            int months = parseGroup(enMatcher.group(2));
            int weeks = parseGroup(enMatcher.group(3));
            int days = parseGroup(enMatcher.group(4)) + weeks * 7;

            if (years > 0 || months > 0 || days > 0) {
                return Period.of(years, months, days);
            }
        }

        throw new IllegalArgumentException("Cannot parse period: " + text);
    }

    /**
     * Parses a string into a Duration
     * 将字符串解析为Duration
     *
     * @param text the text to parse | 要解析的文本
     * @return the parsed Duration | 解析的Duration
     * @throws IllegalArgumentException if the text cannot be parsed | 如果文本无法解析则抛出异常
     */
    public static Duration parseDuration(String text) {
        Objects.requireNonNull(text, "text must not be null");

        String trimmed = text.trim();

        // Try ISO format first
        if (trimmed.startsWith("PT") || trimmed.startsWith("-PT") || trimmed.startsWith("P")) {
            return Duration.parse(trimmed);
        }

        // Try time format (HH:MM:SS)
        if (TIME_FORMAT_PATTERN.matcher(trimmed).matches()) {
            return parseTimeFormat(trimmed);
        }

        // Try Chinese format
        Matcher cnMatcher = CHINESE_DURATION_PATTERN.matcher(trimmed);
        if (cnMatcher.matches()) {
            int days = parseGroup(cnMatcher.group(1));
            int hours = parseGroup(cnMatcher.group(2));
            int minutes = parseGroup(cnMatcher.group(3));
            int seconds = parseGroup(cnMatcher.group(4));

            if (days > 0 || hours > 0 || minutes > 0 || seconds > 0) {
                return Duration.ofDays(days)
                        .plusHours(hours)
                        .plusMinutes(minutes)
                        .plusSeconds(seconds);
            }
        }

        // Try English format
        Matcher enMatcher = DURATION_PATTERN.matcher(trimmed);
        if (enMatcher.matches()) {
            int days = parseGroup(enMatcher.group(1));
            int hours = parseGroup(enMatcher.group(2));
            int minutes = parseGroup(enMatcher.group(3));
            int seconds = parseGroup(enMatcher.group(4));

            if (days > 0 || hours > 0 || minutes > 0 || seconds > 0) {
                return Duration.ofDays(days)
                        .plusHours(hours)
                        .plusMinutes(minutes)
                        .plusSeconds(seconds);
            }
        }

        throw new IllegalArgumentException("Cannot parse duration: " + text);
    }

    /**
     * Tries to parse a string into a Period, returning null if parsing fails
     * 尝试将字符串解析为Period，如果解析失败则返回null
     *
     * @param text the text to parse | 要解析的文本
     * @return the parsed Period, or null if parsing fails | 解析的Period，如果解析失败则返回null
     */
    public static Period tryParsePeriod(String text) {
        try {
            return parsePeriod(text);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Tries to parse a string into a Duration, returning null if parsing fails
     * 尝试将字符串解析为Duration，如果解析失败则返回null
     *
     * @param text the text to parse | 要解析的文本
     * @return the parsed Duration, or null if parsing fails | 解析的Duration，如果解析失败则返回null
     */
    public static Duration tryParseDuration(String text) {
        try {
            return parseDuration(text);
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== Generic Formatting | 通用格式化 ====================

    /**
     * Formats any TemporalAmount as a human-readable string
     * 将任意TemporalAmount格式化为人类可读的字符串
     *
     * @param amount the temporal amount to format | 要格式化的时间量
     * @return the formatted string | 格式化的字符串
     */
    public static String format(TemporalAmount amount) {
        Objects.requireNonNull(amount, "amount must not be null");

        if (amount instanceof Period period) {
            return format(period);
        }
        if (amount instanceof Duration duration) {
            return format(duration);
        }

        throw new UnsupportedOperationException("Unsupported TemporalAmount type: " +
                amount.getClass().getSimpleName());
    }

    // ==================== Helper Methods | 辅助方法 ====================

    private static void appendIfPositive(StringBuilder sb, int value, String singular, String plural) {
        if (value != 0) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(Math.abs(value)).append(" ").append(Math.abs(value) == 1 ? singular : plural);
        }
    }

    private static int parseGroup(String group) {
        if (group == null || group.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(group);
    }

    private static Duration parseTimeFormat(String text) {
        boolean negative = text.startsWith("-");
        if (negative) {
            text = text.substring(1);
        }

        String[] parts = text.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

        Duration duration = Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
        return negative ? duration.negated() : duration;
    }
}
