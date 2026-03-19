package cloud.opencode.base.cron.exception;

import java.io.Serial;

/**
 * Cron Exception - Exception for Cron Expression Errors
 * Cron异常 - Cron表达式错误异常
 *
 * <p>Thrown when cron expression parsing, validation, or evaluation fails.
 * Carries diagnostic context including the original expression and the
 * problematic field name.</p>
 * <p>当Cron表达式解析、验证或计算失败时抛出。
 * 携带诊断上下文，包括原始表达式和有问题的字段名称。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Diagnostic fields: expression, field name - 诊断字段：表达式、字段名称</li>
 *   <li>Factory methods for common error types - 常见错误类型的工厂方法</li>
 *   <li>Serializable - 可序列化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw OpenCronException.parseError("invalid", "expected 5 or 6 fields");
 * throw OpenCronException.fieldError("hour", 25, 0, 23);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable exception) - 线程安全: 是（不可变异常）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cron V1.0.0
 */
public class OpenCronException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String expression;
    private final String field;

    /**
     * Constructs exception with message
     * 构造带消息的异常
     *
     * @param message the detail message | 详细消息
     */
    public OpenCronException(String message) {
        this(message, null, null, null);
    }

    /**
     * Constructs exception with message and cause
     * 构造带消息和原因的异常
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public OpenCronException(String message, Throwable cause) {
        this(message, null, null, cause);
    }

    /**
     * Constructs exception with all diagnostic fields
     * 构造带所有诊断字段的异常
     *
     * @param message    the detail message | 详细消息
     * @param expression the cron expression | Cron表达式
     * @param field      the problematic field | 有问题的字段
     * @param cause      the cause | 原因
     */
    public OpenCronException(String message, String expression, String field, Throwable cause) {
        super(message, cause);
        this.expression = expression;
        this.field = field;
    }

    /**
     * Gets the cron expression that caused the error
     * 获取导致错误的Cron表达式
     *
     * @return the expression, or null | 表达式，或null
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Gets the problematic field name
     * 获取有问题的字段名称
     *
     * @return the field name, or null | 字段名称，或null
     */
    public String getField() {
        return field;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates exception for parse errors
     * 为解析错误创建异常
     *
     * @param expression the expression | 表达式
     * @param reason     the reason | 原因
     * @return the exception | 异常
     */
    public static OpenCronException parseError(String expression, String reason) {
        return new OpenCronException(
                "Invalid cron expression '" + expression + "': " + reason,
                expression, null, null);
    }

    /**
     * Creates exception for parse errors with cause
     * 为带原因的解析错误创建异常
     *
     * @param expression the expression | 表达式
     * @param reason     the reason | 原因
     * @param cause      the cause | 原因
     * @return the exception | 异常
     */
    public static OpenCronException parseError(String expression, String reason, Throwable cause) {
        return new OpenCronException(
                "Invalid cron expression '" + expression + "': " + reason,
                expression, null, cause);
    }

    /**
     * Creates exception for invalid field values
     * 为无效字段值创建异常
     *
     * @param field the field name | 字段名称
     * @param value the invalid value | 无效值
     * @param min   the minimum allowed value | 允许的最小值
     * @param max   the maximum allowed value | 允许的最大值
     * @return the exception | 异常
     */
    public static OpenCronException fieldError(String field, int value, int min, int max) {
        return new OpenCronException(
                "Value " + value + " out of range [" + min + "-" + max + "] in " + field + " field",
                null, field, null);
    }

    /**
     * Creates exception for invalid field expression
     * 为无效字段表达式创建异常
     *
     * @param field      the field name | 字段名称
     * @param fieldValue the invalid field value | 无效字段值
     * @param reason     the reason | 原因
     * @return the exception | 异常
     */
    public static OpenCronException fieldError(String field, String fieldValue, String reason) {
        return new OpenCronException(
                "Invalid " + field + " field '" + fieldValue + "': " + reason,
                null, field, null);
    }

    /**
     * Creates exception for unknown macro
     * 为未知宏创建异常
     *
     * @param macro the unknown macro | 未知宏
     * @return the exception | 异常
     */
    public static OpenCronException unknownMacro(String macro) {
        return new OpenCronException(
                "Unknown cron macro: " + macro,
                macro, null, null);
    }
}
