package cloud.opencode.base.date.cron;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility class for Cron expression operations
 * Cron表达式工具类
 *
 * <p>This class provides static methods for working with Cron expressions,
 * including validation, parsing, and calculating next execution times.</p>
 * <p>此类提供处理Cron表达式的静态方法，包括验证、解析和计算下次执行时间。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Validate Cron expressions - 验证Cron表达式</li>
 *   <li>Calculate next execution time - 计算下次执行时间</li>
 *   <li>Get multiple upcoming executions - 获取多个即将执行的时间</li>
 *   <li>Common Cron expression constants - 常用Cron表达式常量</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check if expression is valid
 * boolean valid = CronUtil.isValid("0 0 * * * ?");
 *
 * // Get next execution time
 * Optional<LocalDateTime> next = CronUtil.getNextExecutionTime("0 0 12 * * ?");
 *
 * // Get next 5 execution times
 * List<LocalDateTime> nextFive = CronUtil.getNextExecutionTimes("0 0 9 * * MON-FRI", 5);
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
 *   <li>Time complexity: O(n) for getNextExecutionTimes where n=count; O(1) for single next-time lookup - 时间复杂度: getNextExecutionTimes 为 O(n)，n 为查询次数；单次查询为 O(1)</li>
 *   <li>Space complexity: O(n) for result list in getNextExecutionTimes; O(1) otherwise - 空间复杂度: getNextExecutionTimes 结果列表为 O(n)；其余为 O(1)</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class CronUtil {

    private CronUtil() {
        // Utility class
    }

    // ==================== Common Cron Expressions ====================

    /** Every second */
    public static final String EVERY_SECOND = "* * * * * ?";

    /** Every minute */
    public static final String EVERY_MINUTE = "0 * * * * ?";

    /** Every hour */
    public static final String EVERY_HOUR = "0 0 * * * ?";

    /** Every day at midnight */
    public static final String DAILY_MIDNIGHT = "0 0 0 * * ?";

    /** Every day at noon */
    public static final String DAILY_NOON = "0 0 12 * * ?";

    /** Every Monday at midnight */
    public static final String WEEKLY_MONDAY = "0 0 0 ? * MON";

    /** First day of every month at midnight */
    public static final String MONTHLY_FIRST = "0 0 0 1 * ?";

    /** Every weekday (Monday to Friday) at 9 AM */
    public static final String WEEKDAYS_9AM = "0 0 9 ? * MON-FRI";

    /** Every 5 minutes */
    public static final String EVERY_5_MINUTES = "0 0/5 * * * ?";

    /** Every 15 minutes */
    public static final String EVERY_15_MINUTES = "0 0/15 * * * ?";

    /** Every 30 minutes */
    public static final String EVERY_30_MINUTES = "0 0/30 * * * ?";

    // ==================== Validation Methods ====================

    /**
     * Checks if a Cron expression is valid
     * 检查Cron表达式是否有效
     *
     * @param expression the Cron expression | Cron表达式
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValid(String expression) {
        if (expression == null || expression.isBlank()) {
            return false;
        }
        try {
            CronExpression.parse(expression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates a Cron expression and throws if invalid
     * 验证Cron表达式，如果无效则抛出异常
     *
     * @param expression the Cron expression | Cron表达式
     * @throws IllegalArgumentException if invalid | 如果无效则抛出异常
     */
    public static void validate(String expression) {
        Objects.requireNonNull(expression, "expression must not be null");
        CronExpression.parse(expression);
    }

    // ==================== Execution Time Methods ====================

    /**
     * Gets the next execution time for a Cron expression
     * 获取Cron表达式的下次执行时间
     *
     * @param expression the Cron expression | Cron表达式
     * @return the next execution time, or empty if not calculable | 下次执行时间，如果无法计算则为空
     */
    public static Optional<LocalDateTime> getNextExecutionTime(String expression) {
        return getNextExecutionTime(expression, LocalDateTime.now());
    }

    /**
     * Gets the next execution time after a specific time
     * 获取指定时间之后的下次执行时间
     *
     * @param expression the Cron expression | Cron表达式
     * @param after the reference time | 参考时间
     * @return the next execution time, or empty if not calculable | 下次执行时间，如果无法计算则为空
     */
    public static Optional<LocalDateTime> getNextExecutionTime(String expression, LocalDateTime after) {
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(after, "after must not be null");

        try {
            CronExpression cron = CronExpression.parse(expression);
            return Optional.ofNullable(cron.nextExecution(after));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Gets multiple next execution times
     * 获取多个下次执行时间
     *
     * @param expression the Cron expression | Cron表达式
     * @param count the number of times to get | 要获取的次数
     * @return list of execution times | 执行时间列表
     */
    public static List<LocalDateTime> getNextExecutionTimes(String expression, int count) {
        return getNextExecutionTimes(expression, LocalDateTime.now(), count);
    }

    /**
     * Gets multiple next execution times after a specific time
     * 获取指定时间之后的多个执行时间
     *
     * @param expression the Cron expression | Cron表达式
     * @param after the reference time | 参考时间
     * @param count the number of times to get | 要获取的次数
     * @return list of execution times | 执行时间列表
     */
    public static List<LocalDateTime> getNextExecutionTimes(String expression, LocalDateTime after, int count) {
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(after, "after must not be null");
        if (count <= 0) {
            return List.of();
        }

        List<LocalDateTime> times = new ArrayList<>(count);
        CronExpression cron = CronExpression.parse(expression);
        LocalDateTime current = after;

        for (int i = 0; i < count; i++) {
            try {
                LocalDateTime next = cron.nextExecution(current);
                times.add(next);
                current = next;
            } catch (Exception e) {
                break;
            }
        }

        return times;
    }

    // ==================== Time Zone Methods ====================

    /**
     * Gets the next execution time in a specific timezone
     * 获取特定时区的下次执行时间
     *
     * @param expression the Cron expression | Cron表达式
     * @param zone the timezone | 时区
     * @return the next execution time | 下次执行时间
     */
    public static Optional<ZonedDateTime> getNextExecutionTime(String expression, ZoneId zone) {
        return getNextExecutionTime(expression, ZonedDateTime.now(zone));
    }

    /**
     * Gets the next execution time after a specific zoned time
     * 获取指定时区时间之后的下次执行时间
     *
     * @param expression the Cron expression | Cron表达式
     * @param after the reference time | 参考时间
     * @return the next execution time | 下次执行时间
     */
    public static Optional<ZonedDateTime> getNextExecutionTime(String expression, ZonedDateTime after) {
        Objects.requireNonNull(after, "after must not be null");
        return getNextExecutionTime(expression, after.toLocalDateTime())
                .map(dt -> dt.atZone(after.getZone()));
    }

    // ==================== Description Methods ====================

    /**
     * Gets a human-readable description of a Cron expression
     * 获取Cron表达式的可读描述
     *
     * @param expression the Cron expression | Cron表达式
     * @return the description | 描述
     */
    public static String describe(String expression) {
        Objects.requireNonNull(expression, "expression must not be null");

        // Simple descriptions for common patterns
        return switch (expression) {
            case EVERY_SECOND -> "Every second";
            case EVERY_MINUTE -> "Every minute";
            case EVERY_HOUR -> "Every hour";
            case DAILY_MIDNIGHT -> "Every day at midnight";
            case DAILY_NOON -> "Every day at noon";
            case WEEKLY_MONDAY -> "Every Monday at midnight";
            case MONTHLY_FIRST -> "First day of every month at midnight";
            case WEEKDAYS_9AM -> "Every weekday at 9:00 AM";
            case EVERY_5_MINUTES -> "Every 5 minutes";
            case EVERY_15_MINUTES -> "Every 15 minutes";
            case EVERY_30_MINUTES -> "Every 30 minutes";
            default -> "Cron: " + expression;
        };
    }

    /**
     * Gets a Chinese description of a Cron expression
     * 获取Cron表达式的中文描述
     *
     * @param expression the Cron expression | Cron表达式
     * @return the Chinese description | 中文描述
     */
    public static String describeInChinese(String expression) {
        Objects.requireNonNull(expression, "expression must not be null");

        return switch (expression) {
            case EVERY_SECOND -> "每秒执行";
            case EVERY_MINUTE -> "每分钟执行";
            case EVERY_HOUR -> "每小时执行";
            case DAILY_MIDNIGHT -> "每天午夜执行";
            case DAILY_NOON -> "每天中午执行";
            case WEEKLY_MONDAY -> "每周一午夜执行";
            case MONTHLY_FIRST -> "每月第一天午夜执行";
            case WEEKDAYS_9AM -> "每个工作日上午9点执行";
            case EVERY_5_MINUTES -> "每5分钟执行";
            case EVERY_15_MINUTES -> "每15分钟执行";
            case EVERY_30_MINUTES -> "每30分钟执行";
            default -> "Cron表达式: " + expression;
        };
    }

    // ==================== Builder Methods ====================

    /**
     * Creates a Cron expression for running at a specific time every day
     * 创建每天在特定时间运行的Cron表达式
     *
     * @param hour the hour (0-23) | 小时（0-23）
     * @param minute the minute (0-59) | 分钟（0-59）
     * @return the Cron expression | Cron表达式
     */
    public static String dailyAt(int hour, int minute) {
        validateHour(hour);
        validateMinute(minute);
        return String.format("0 %d %d * * ?", minute, hour);
    }

    /**
     * Creates a Cron expression for running at a specific time on weekdays
     * 创建工作日在特定时间运行的Cron表达式
     *
     * @param hour the hour (0-23) | 小时（0-23）
     * @param minute the minute (0-59) | 分钟（0-59）
     * @return the Cron expression | Cron表达式
     */
    public static String weekdaysAt(int hour, int minute) {
        validateHour(hour);
        validateMinute(minute);
        return String.format("0 %d %d ? * MON-FRI", minute, hour);
    }

    /**
     * Creates a Cron expression for running every N minutes
     * 创建每N分钟运行的Cron表达式
     *
     * @param minutes the interval in minutes | 分钟间隔
     * @return the Cron expression | Cron表达式
     */
    public static String everyMinutes(int minutes) {
        if (minutes <= 0 || minutes > 59) {
            throw new IllegalArgumentException("Minutes must be between 1 and 59");
        }
        return String.format("0 0/%d * * * ?", minutes);
    }

    /**
     * Creates a Cron expression for running every N hours
     * 创建每N小时运行的Cron表达式
     *
     * @param hours the interval in hours | 小时间隔
     * @return the Cron expression | Cron表达式
     */
    public static String everyHours(int hours) {
        if (hours <= 0 || hours > 23) {
            throw new IllegalArgumentException("Hours must be between 1 and 23");
        }
        return String.format("0 0 0/%d * * ?", hours);
    }

    private static void validateHour(int hour) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Hour must be between 0 and 23");
        }
    }

    private static void validateMinute(int minute) {
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Minute must be between 0 and 59");
        }
    }
}
