package cloud.opencode.base.cron;

import cloud.opencode.base.cron.exception.OpenCronException;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Cron Validator - Cron Expression Validation Utility
 * Cron验证器 - Cron表达式验证工具类
 *
 * <p>Validates cron expressions for syntax correctness and optional minimum interval.
 * Supports all syntax recognized by {@link CronExpression} including aliases,
 * special characters (L, W, #), and macros.</p>
 * <p>验证Cron表达式的语法正确性和可选的最小间隔。
 * 支持 {@link CronExpression} 识别的所有语法，包括别名、
 * 特殊字符（L、W、#）和宏。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Syntax validation via parse - 通过解析进行语法验证</li>
 *   <li>Minimum interval enforcement - 最小间隔强制检查</li>
 *   <li>Estimated interval calculation - 预估间隔计算</li>
 *   <li>Boolean isValid check (no exception) - 布尔isValid检查（无异常）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Quick check | 快速检查
 * CronValidator.isValid("0 9 * * MON-FRI")  // true
 * CronValidator.isValid("invalid")           // false
 *
 * // Validate with exception | 验证（抛异常）
 * CronValidator.validate("0 9 * * MON-FRI"); // OK
 *
 * // Minimum interval check | 最小间隔检查
 * CronValidator.validate("* * * * * *", Duration.ofSeconds(5)); // throws
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless static methods) - 线程安全: 是（无状态静态方法）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see CronExpression
 * @since JDK 25, opencode-base-cron V1.0.0
 */
public final class CronValidator {

    private static final Duration DEFAULT_MIN_INTERVAL = Duration.ofSeconds(1);

    private CronValidator() {
    }

    /**
     * Validates a cron expression
     * 验证Cron表达式
     *
     * @param expression the cron expression | Cron表达式
     * @throws OpenCronException if invalid | 如果无效
     */
    public static void validate(String expression) {
        validate(expression, DEFAULT_MIN_INTERVAL);
    }

    /**
     * Validates a cron expression with a minimum interval check
     * 验证Cron表达式并检查最小间隔
     *
     * @param expression  the cron expression | Cron表达式
     * @param minInterval the minimum interval between executions | 执行之间的最小间隔
     * @throws OpenCronException if invalid or interval too short | 如果无效或间隔太短
     */
    public static void validate(String expression, Duration minInterval) {
        Objects.requireNonNull(expression, "Cron expression must not be null");

        // Parse validates syntax
        CronExpression cron = CronExpression.parse(expression);

        // Check minimum interval
        checkMinInterval(cron, expression, minInterval);
    }

    /**
     * Checks if a cron expression is valid
     * 检查Cron表达式是否有效
     *
     * @param expression the cron expression | Cron表达式
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValid(String expression) {
        try {
            CronExpression.parse(expression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the estimated interval between consecutive executions
     * 获取连续执行之间的预估间隔
     *
     * @param expression the cron expression | Cron表达式
     * @return the estimated interval | 预估间隔
     */
    public static Duration getEstimatedInterval(String expression) {
        CronExpression cron = CronExpression.parse(expression);
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime next = cron.nextExecution(now);
        if (next == null) return Duration.ZERO;
        ZonedDateTime nextNext = cron.nextExecution(next);
        if (nextNext == null) return Duration.ZERO;
        return Duration.between(next, nextNext);
    }

    private static void checkMinInterval(CronExpression cron, String expression, Duration minInterval) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime next = cron.nextExecution(now);
        if (next == null) return;
        ZonedDateTime nextNext = cron.nextExecution(next);
        if (nextNext == null) return;

        Duration interval = Duration.between(next, nextNext);
        if (interval.compareTo(minInterval) < 0) {
            throw new OpenCronException(
                    "Cron interval " + interval + " is shorter than minimum " + minInterval,
                    expression, null, null);
        }
    }
}
