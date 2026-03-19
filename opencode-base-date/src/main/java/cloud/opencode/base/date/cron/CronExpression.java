package cloud.opencode.base.date.cron;

import cloud.opencode.base.date.exception.OpenDateException;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A cron expression parser and scheduler
 * Cron表达式解析器和调度器
 *
 * <p>This class parses and evaluates cron expressions. It supports the standard
 * 5-field cron format (minute, hour, day of month, month, day of week).</p>
 * <p>此类解析和计算cron表达式。支持标准的5字段cron格式
 * （分钟、小时、日期、月份、星期几）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Parse standard 5-field cron expressions - 解析标准5字段cron表达式</li>
 *   <li>Support wildcards, ranges, lists, and step values - 支持通配符、范围、列表和步进值</li>
 *   <li>Calculate next execution time from a given date-time - 计算给定日期时间后的下次执行时间</li>
 *   <li>Match a date-time against the expression - 将日期时间与表达式匹配</li>
 * </ul>
 *
 * <p><strong>Cron Expression Format | Cron表达式格式:</strong></p>
 * <pre>
 * ┌───────────── minute (0 - 59)
 * │ ┌───────────── hour (0 - 23)
 * │ │ ┌───────────── day of month (1 - 31)
 * │ │ │ ┌───────────── month (1 - 12)
 * │ │ │ │ ┌───────────── day of week (0 - 6) (Sunday to Saturday)
 * │ │ │ │ │
 * * * * * *
 * </pre>
 *
 * <p><strong>Special Characters | 特殊字符:</strong></p>
 * <ul>
 *   <li>* - any value | 任意值</li>
 *   <li>, - value list separator | 值列表分隔符</li>
 *   <li>- - range of values | 值范围</li>
 *   <li>/ - step values | 步进值</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Every minute
 * CronExpression cron = CronExpression.parse("* * * * *");
 *
 * // Every day at 8:30
 * CronExpression daily = CronExpression.parse("30 8 * * *");
 *
 * // Get next execution time
 * LocalDateTime next = cron.nextExecution();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes (with explicit null checks) - 空值安全: 是（有明确的空值检查）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class CronExpression implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Pattern CRON_PATTERN = Pattern.compile(
            "^\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+$"
    );

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern BRACKET_PATTERN = Pattern.compile("[\\[\\]]");

    /**
     * The original expression string
     */
    private final String expression;

    /**
     * Allowed minutes (0-59)
     */
    private final Set<Integer> minutes;

    /**
     * Allowed hours (0-23)
     */
    private final Set<Integer> hours;

    /**
     * Allowed days of month (1-31)
     */
    private final Set<Integer> daysOfMonth;

    /**
     * Allowed months (1-12)
     */
    private final Set<Integer> months;

    /**
     * Allowed days of week (0-6, Sunday=0)
     */
    private final Set<Integer> daysOfWeek;

    // ==================== Preset Expressions | 预设表达式 ====================

    /**
     * Every minute
     * 每分钟
     */
    public static final String EVERY_MINUTE = "* * * * *";

    /**
     * Every hour
     * 每小时
     */
    public static final String EVERY_HOUR = "0 * * * *";

    /**
     * Every day at midnight
     * 每天午夜
     */
    public static final String DAILY = "0 0 * * *";

    /**
     * Every Monday at midnight
     * 每周一午夜
     */
    public static final String WEEKLY = "0 0 * * 1";

    /**
     * First day of every month at midnight
     * 每月第一天午夜
     */
    public static final String MONTHLY = "0 0 1 * *";

    /**
     * First day of every year at midnight
     * 每年第一天午夜
     */
    public static final String YEARLY = "0 0 1 1 *";

    // ==================== Constructors | 构造函数 ====================

    /**
     * Private constructor
     */
    private CronExpression(String expression, Set<Integer> minutes, Set<Integer> hours,
                           Set<Integer> daysOfMonth, Set<Integer> months, Set<Integer> daysOfWeek) {
        this.expression = expression;
        this.minutes = Collections.unmodifiableSet(new TreeSet<>(minutes));
        this.hours = Collections.unmodifiableSet(new TreeSet<>(hours));
        this.daysOfMonth = Collections.unmodifiableSet(new TreeSet<>(daysOfMonth));
        this.months = Collections.unmodifiableSet(new TreeSet<>(months));
        this.daysOfWeek = Collections.unmodifiableSet(new TreeSet<>(daysOfWeek));
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Parses a cron expression
     * 解析cron表达式
     *
     * @param expression the cron expression | cron表达式
     * @return the CronExpression | CronExpression
     * @throws OpenDateException if the expression is invalid | 如果表达式无效则抛出异常
     */
    public static CronExpression parse(String expression) {
        Objects.requireNonNull(expression, "expression must not be null");

        String trimmed = expression.trim();
        if (!CRON_PATTERN.matcher(trimmed).matches()) {
            throw OpenDateException.cronError(expression, "Invalid cron expression format");
        }

        String[] parts = WHITESPACE_PATTERN.split(trimmed);
        if (parts.length != 5) {
            throw OpenDateException.cronError(expression, "Cron expression must have 5 fields");
        }

        try {
            Set<Integer> minutes = parseField(parts[0], 0, 59);
            Set<Integer> hours = parseField(parts[1], 0, 23);
            Set<Integer> daysOfMonth = parseField(parts[2], 1, 31);
            Set<Integer> months = parseField(parts[3], 1, 12);
            Set<Integer> daysOfWeek = parseField(parts[4], 0, 6);

            return new CronExpression(expression, minutes, hours, daysOfMonth, months, daysOfWeek);
        } catch (Exception e) {
            throw OpenDateException.cronError(expression, "Failed to parse: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a cron expression for a specific time
     * 为特定时间创建cron表达式
     *
     * @param hour   the hour (0-23) | 小时
     * @param minute the minute (0-59) | 分钟
     * @return the CronExpression | CronExpression
     */
    public static CronExpression daily(int hour, int minute) {
        return parse(String.format("%d %d * * *", minute, hour));
    }

    /**
     * Creates a cron expression for every N minutes
     * 为每N分钟创建cron表达式
     *
     * @param minutes the interval in minutes | 分钟间隔
     * @return the CronExpression | CronExpression
     */
    public static CronExpression everyMinutes(int minutes) {
        if (minutes <= 0 || minutes > 59) {
            throw new IllegalArgumentException("minutes must be between 1 and 59");
        }
        return parse(String.format("*/%d * * * *", minutes));
    }

    /**
     * Creates a cron expression for every N hours
     * 为每N小时创建cron表达式
     *
     * @param hours the interval in hours | 小时间隔
     * @return the CronExpression | CronExpression
     */
    public static CronExpression everyHours(int hours) {
        if (hours <= 0 || hours > 23) {
            throw new IllegalArgumentException("hours must be between 1 and 23");
        }
        return parse(String.format("0 */%d * * *", hours));
    }

    // ==================== Execution Methods | 执行方法 ====================

    /**
     * Gets the next execution time after now
     * 获取当前之后的下一次执行时间
     *
     * @return the next execution time | 下一次执行时间
     */
    public LocalDateTime nextExecution() {
        return nextExecution(LocalDateTime.now());
    }

    /**
     * Gets the next execution time after the specified time
     * 获取指定时间之后的下一次执行时间
     *
     * @param after the starting time | 开始时间
     * @return the next execution time | 下一次执行时间
     */
    public LocalDateTime nextExecution(LocalDateTime after) {
        LocalDateTime next = after.plusMinutes(1).withSecond(0).withNano(0);

        // Limit search to 4 years to avoid infinite loop
        LocalDateTime limit = after.plusYears(4);

        while (next.isBefore(limit)) {
            if (matches(next)) {
                return next;
            }

            // Optimize: skip to next valid month if current month doesn't match
            if (!months.contains(next.getMonthValue())) {
                next = next.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0);
                continue;
            }

            // Skip to next valid day
            if (!matchesDay(next)) {
                next = next.plusDays(1).withHour(0).withMinute(0);
                continue;
            }

            // Skip to next valid hour
            if (!hours.contains(next.getHour())) {
                next = next.plusHours(1).withMinute(0);
                continue;
            }

            // Skip to next valid minute
            next = next.plusMinutes(1);
        }

        throw OpenDateException.cronError(expression, "No valid execution time found within 4 years");
    }

    /**
     * Gets the previous execution time before now
     * 获取当前之前的上一次执行时间
     *
     * @return the previous execution time | 上一次执行时间
     */
    public LocalDateTime previousExecution() {
        return previousExecution(LocalDateTime.now());
    }

    /**
     * Gets the previous execution time before the specified time
     * 获取指定时间之前的上一次执行时间
     *
     * @param before the ending time | 结束时间
     * @return the previous execution time | 上一次执行时间
     */
    public LocalDateTime previousExecution(LocalDateTime before) {
        LocalDateTime prev = before.minusMinutes(1).withSecond(0).withNano(0);

        LocalDateTime limit = before.minusYears(4);

        while (prev.isAfter(limit)) {
            if (matches(prev)) {
                return prev;
            }
            // Skip backwards efficiently
            if (!months.contains(prev.getMonthValue())) {
                prev = prev.withDayOfMonth(1).withHour(0).withMinute(0).minusMinutes(1);
                continue;
            }
            if (!matchesDay(prev)) {
                prev = prev.withHour(0).withMinute(0).minusDays(1)
                        .withHour(23).withMinute(59);
                continue;
            }
            if (!hours.contains(prev.getHour())) {
                prev = prev.withMinute(0).minusMinutes(1);
                continue;
            }
            prev = prev.minusMinutes(1);
        }

        throw OpenDateException.cronError(expression, "No valid execution time found within 4 years");
    }

    /**
     * Gets the next N execution times
     * 获取接下来的N次执行时间
     *
     * @param count the number of executions | 执行次数
     * @return the list of execution times | 执行时间列表
     */
    public List<LocalDateTime> nextExecutions(int count) {
        return nextExecutions(LocalDateTime.now(), count);
    }

    /**
     * Gets the next N execution times after the specified time
     * 获取指定时间之后的N次执行时间
     *
     * @param after the starting time | 开始时间
     * @param count the number of executions | 执行次数
     * @return the list of execution times | 执行时间列表
     */
    public List<LocalDateTime> nextExecutions(LocalDateTime after, int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }

        List<LocalDateTime> executions = new ArrayList<>(count);
        LocalDateTime current = after;

        for (int i = 0; i < count; i++) {
            current = nextExecution(current);
            executions.add(current);
        }

        return executions;
    }

    // ==================== Match Methods | 匹配方法 ====================

    /**
     * Checks if the expression matches the specified date-time
     * 检查表达式是否匹配指定的日期时间
     *
     * @param dateTime the date-time to check | 要检查的日期时间
     * @return true if matches | 如果匹配返回true
     */
    public boolean matches(LocalDateTime dateTime) {
        return minutes.contains(dateTime.getMinute()) &&
                hours.contains(dateTime.getHour()) &&
                months.contains(dateTime.getMonthValue()) &&
                matchesDay(dateTime);
    }

    private boolean matchesDay(LocalDateTime dateTime) {
        // Day of month and day of week are ORed
        boolean domMatch = daysOfMonth.contains(dateTime.getDayOfMonth());
        boolean dowMatch = daysOfWeek.contains(dateTime.getDayOfWeek().getValue() % 7);

        // If both are wildcards (*), any day matches
        if (daysOfMonth.size() == 31 && daysOfWeek.size() == 7) {
            return true;
        }

        // If day of month is specified but not day of week
        if (daysOfMonth.size() < 31 && daysOfWeek.size() == 7) {
            return domMatch;
        }

        // If day of week is specified but not day of month
        if (daysOfMonth.size() == 31 && daysOfWeek.size() < 7) {
            return dowMatch;
        }

        // Both are specified - OR them
        return domMatch || dowMatch;
    }

    // ==================== Getters | 获取器 ====================

    /**
     * Gets the original expression string
     * 获取原始表达式字符串
     *
     * @return the expression | 表达式
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Gets the allowed minutes
     * 获取允许的分钟
     *
     * @return the minutes | 分钟集合
     */
    public Set<Integer> getMinutes() {
        return minutes;
    }

    /**
     * Gets the allowed hours
     * 获取允许的小时
     *
     * @return the hours | 小时集合
     */
    public Set<Integer> getHours() {
        return hours;
    }

    /**
     * Gets the allowed days of month
     * 获取允许的日期
     *
     * @return the days of month | 日期集合
     */
    public Set<Integer> getDaysOfMonth() {
        return daysOfMonth;
    }

    /**
     * Gets the allowed months
     * 获取允许的月份
     *
     * @return the months | 月份集合
     */
    public Set<Integer> getMonths() {
        return months;
    }

    /**
     * Gets the allowed days of week
     * 获取允许的星期几
     *
     * @return the days of week | 星期几集合
     */
    public Set<Integer> getDaysOfWeek() {
        return daysOfWeek;
    }

    // ==================== Helper Methods | 辅助方法 ====================

    private static Set<Integer> parseField(String field, int min, int max) {
        Set<Integer> values = new TreeSet<>();

        if (field.equals("*")) {
            for (int i = min; i <= max; i++) {
                values.add(i);
            }
            return values;
        }

        for (String part : field.split(",")) {
            if (part.contains("/")) {
                parseStep(part, min, max, values);
            } else if (part.contains("-")) {
                parseRange(part, min, max, values);
            } else {
                int value = Integer.parseInt(part);
                if (value < min || value > max) {
                    throw new IllegalArgumentException("Value out of range: " + value);
                }
                values.add(value);
            }
        }

        return values;
    }

    private static void parseStep(String part, int min, int max, Set<Integer> values) {
        String[] stepParts = part.split("/");
        int step = Integer.parseInt(stepParts[1]);
        if (step <= 0) {
            throw new IllegalArgumentException("Step must be positive: " + step);
        }

        int start = min;
        int end = max;

        if (!stepParts[0].equals("*")) {
            if (stepParts[0].contains("-")) {
                String[] range = stepParts[0].split("-");
                start = Integer.parseInt(range[0]);
                end = Integer.parseInt(range[1]);
            } else {
                start = Integer.parseInt(stepParts[0]);
            }
        }

        if (start < min || start > max) {
            throw new IllegalArgumentException("Start value out of range [" + min + "-" + max + "]: " + start);
        }
        if (end < min || end > max) {
            throw new IllegalArgumentException("End value out of range [" + min + "-" + max + "]: " + end);
        }
        if (start > end) {
            throw new IllegalArgumentException("Invalid range in step: " + part);
        }

        for (int i = start; i <= end; i += step) {
            values.add(i);
        }
    }

    private static void parseRange(String part, int min, int max, Set<Integer> values) {
        String[] range = part.split("-");
        int start = Integer.parseInt(range[0]);
        int end = Integer.parseInt(range[1]);

        if (start < min || end > max || start > end) {
            throw new IllegalArgumentException("Invalid range: " + part);
        }

        for (int i = start; i <= end; i++) {
            values.add(i);
        }
    }

    // ==================== Description Method | 描述方法 ====================

    /**
     * Gets a human-readable description of the cron expression
     * 获取cron表达式的人类可读描述
     *
     * @return the description | 描述
     */
    public String describe() {
        StringBuilder sb = new StringBuilder();

        // Minute
        if (minutes.size() == 60) {
            sb.append("Every minute");
        } else if (minutes.size() == 1) {
            sb.append("At minute ").append(minutes.iterator().next());
        } else {
            sb.append("At minutes ").append(formatSet(minutes));
        }

        // Hour
        if (hours.size() == 24) {
            sb.append(" of every hour");
        } else if (hours.size() == 1) {
            sb.append(" of hour ").append(hours.iterator().next());
        } else {
            sb.append(" of hours ").append(formatSet(hours));
        }

        return sb.toString();
    }

    private String formatSet(Set<Integer> set) {
        return BRACKET_PATTERN.matcher(set.toString()).replaceAll("");
    }

    // ==================== Object Methods | Object方法 ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CronExpression other)) return false;
        return Objects.equals(expression, other.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }

    @Override
    public String toString() {
        return expression;
    }
}
