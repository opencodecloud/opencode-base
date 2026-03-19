package cloud.opencode.base.expression;

import cloud.opencode.base.core.exception.OpenException;

/**
 * Expression Exception
 * 表达式异常
 *
 * <p>Base exception for all expression-related errors including parsing,
 * evaluation, type conversion, and security violations.</p>
 * <p>所有表达式相关错误的基础异常，包括解析、求值、类型转换和安全违规。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Rich error context with expression string and position - 丰富的错误上下文，包含表达式字符串和位置</li>
 *   <li>Static factory methods for common error types - 常见错误类型的静态工厂方法</li>
 *   <li>Supports parse, evaluation, type, property, method, and security errors - 支持解析、求值、类型、属性、方法和安全错误</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Catch expression errors
 * try {
 *     OpenExpression.eval("invalid ++ expr");
 * } catch (OpenExpressionException e) {
 *     String expr = e.getExpression();
 *     int pos = e.getPosition();
 * }
 *
 * // Create specific error types
 * throw OpenExpressionException.parseError("Unexpected token", "1 ++ 2", 2);
 * throw OpenExpressionException.divisionByZero();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable after construction - 线程安全: 是，构造后不可变</li>
 *   <li>Null-safe: Yes, null expression/position handled - 空值安全: 是，null表达式/位置已处理</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public class OpenExpressionException extends OpenException {

    private static final String COMPONENT = "Expression";

    private final String expression;
    private final int position;

    /**
     * Create exception with message
     * 创建带消息的异常
     *
     * @param message the error message | 错误消息
     */
    public OpenExpressionException(String message) {
        this(message, null, -1);
    }

    /**
     * Create exception with message and cause
     * 创建带消息和原因的异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public OpenExpressionException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.expression = null;
        this.position = -1;
    }

    /**
     * Create exception with expression context
     * 创建带表达式上下文的异常
     *
     * @param message the error message | 错误消息
     * @param expression the expression string | 表达式字符串
     * @param position the error position | 错误位置
     */
    public OpenExpressionException(String message, String expression, int position) {
        super(COMPONENT, null, formatMessage(message, expression, position));
        this.expression = expression;
        this.position = position;
    }

    private static String formatMessage(String message, String expression, int position) {
        if (expression == null || position < 0) {
            return message;
        }
        return String.format("%s at position %d in expression: %s", message, position, expression);
    }

    /**
     * Get expression string
     * 获取表达式字符串
     *
     * @return the expression | 表达式
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Get error position
     * 获取错误位置
     *
     * @return the position | 位置
     */
    public int getPosition() {
        return position;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Create parse error
     * 创建解析错误
     *
     * @param message the error message | 错误消息
     * @param expression the expression | 表达式
     * @param position the position | 位置
     * @return the exception | 异常
     */
    public static OpenExpressionException parseError(String message, String expression, int position) {
        return new OpenExpressionException("Parse error: " + message, expression, position);
    }

    /**
     * Create parse error with position only
     * 创建只带位置的解析错误
     *
     * @param message the error message | 错误消息
     * @param position the position | 位置
     * @return the exception | 异常
     */
    public static OpenExpressionException parseError(String message, int position) {
        return new OpenExpressionException("Parse error at position " + position + ": " + message);
    }

    /**
     * Create evaluation error
     * 创建求值错误
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     * @return the exception | 异常
     */
    public static OpenExpressionException evaluationError(String message, Throwable cause) {
        return new OpenExpressionException("Evaluation error: " + message, cause);
    }

    /**
     * Create evaluation error
     * 创建求值错误
     *
     * @param message the error message | 错误消息
     * @return the exception | 异常
     */
    public static OpenExpressionException evaluationError(String message) {
        return new OpenExpressionException("Evaluation error: " + message);
    }

    /**
     * Create type error
     * 创建类型错误
     *
     * @param expected the expected type | 期望类型
     * @param actual the actual value | 实际值
     * @return the exception | 异常
     */
    public static OpenExpressionException typeError(String expected, Object actual) {
        return new OpenExpressionException(String.format(
            "Type error: expected %s but got %s",
            expected,
            actual == null ? "null" : actual.getClass().getSimpleName()
        ));
    }

    /**
     * Create property not found error
     * 创建属性未找到错误
     *
     * @param property the property name | 属性名
     * @param type the target type | 目标类型
     * @return the exception | 异常
     */
    public static OpenExpressionException propertyNotFound(String property, Class<?> type) {
        return new OpenExpressionException(String.format(
            "Property '%s' not found on type %s", property, type.getSimpleName()
        ));
    }

    /**
     * Create method not found error
     * 创建方法未找到错误
     *
     * @param method the method name | 方法名
     * @param type the target type | 目标类型
     * @return the exception | 异常
     */
    public static OpenExpressionException methodNotFound(String method, Class<?> type) {
        return new OpenExpressionException(String.format(
            "Method '%s' not found on type %s", method, type.getSimpleName()
        ));
    }

    /**
     * Create function not found error
     * 创建函数未找到错误
     *
     * @param function the function name | 函数名
     * @return the exception | 异常
     */
    public static OpenExpressionException functionNotFound(String function) {
        return new OpenExpressionException("Function not found: " + function);
    }

    /**
     * Create security violation error
     * 创建安全违规错误
     *
     * @param message the error message | 错误消息
     * @return the exception | 异常
     */
    public static OpenExpressionException securityViolation(String message) {
        return new OpenExpressionException("Security violation: " + message);
    }

    /**
     * Create timeout error
     * 创建超时错误
     *
     * @param millis the timeout in milliseconds | 超时毫秒数
     * @return the exception | 异常
     */
    public static OpenExpressionException timeout(long millis) {
        return new OpenExpressionException("Expression evaluation timed out after " + millis + "ms");
    }

    /**
     * Create division by zero error
     * 创建除零错误
     *
     * @return the exception | 异常
     */
    public static OpenExpressionException divisionByZero() {
        return new OpenExpressionException("Division by zero");
    }

    /**
     * Create null pointer error
     * 创建空指针错误
     *
     * @param context the context description | 上下文描述
     * @return the exception | 异常
     */
    public static OpenExpressionException nullPointer(String context) {
        return new OpenExpressionException("Null pointer in " + context);
    }
}
