package cloud.opencode.base.expression;

import cloud.opencode.base.expression.context.EvaluationContext;

/**
 * Expression Interface
 * 表达式接口
 *
 * <p>Represents a parsed expression that can be evaluated.</p>
 * <p>表示可以求值的已解析表达式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Evaluate expressions with or without context - 支持有无上下文的表达式求值</li>
 *   <li>Type-safe evaluation with automatic conversion - 类型安全求值与自动转换</li>
 *   <li>Root object binding for property access - 根对象绑定用于属性访问</li>
 *   <li>Optional writable expressions - 可选的可写表达式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Expression expr = OpenExpression.parse("1 + 2 * 3");
 * Object result = expr.getValue();  // 7
 *
 * // With type conversion
 * int typed = expr.getValue(Integer.class);  // 7
 *
 * // With context
 * StandardContext ctx = new StandardContext();
 * ctx.setVariable("x", 10);
 * Expression expr2 = OpenExpression.parse("x + 5");
 * Object result2 = expr2.getValue(ctx);  // 15
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Yes, null context creates default - 空值安全: 是，null上下文创建默认值</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public interface Expression {

    /**
     * Get the expression string
     * 获取表达式字符串
     *
     * @return the expression string | 表达式字符串
     */
    String getExpressionString();

    /**
     * Evaluate the expression
     * 求值表达式
     *
     * @return the result | 结果
     */
    Object getValue();

    /**
     * Evaluate the expression with context
     * 使用上下文求值表达式
     *
     * @param context the evaluation context | 求值上下文
     * @return the result | 结果
     */
    Object getValue(EvaluationContext context);

    /**
     * Evaluate the expression and convert to type
     * 求值表达式并转换为指定类型
     *
     * @param targetType the target type | 目标类型
     * @param <T> the target type | 目标类型
     * @return the typed result | 类型化结果
     */
    <T> T getValue(Class<T> targetType);

    /**
     * Evaluate the expression with context and convert to type
     * 使用上下文求值表达式并转换为指定类型
     *
     * @param context the evaluation context | 求值上下文
     * @param targetType the target type | 目标类型
     * @param <T> the target type | 目标类型
     * @return the typed result | 类型化结果
     */
    <T> T getValue(EvaluationContext context, Class<T> targetType);

    /**
     * Evaluate the expression with root object
     * 使用根对象求值表达式
     *
     * @param rootObject the root object | 根对象
     * @return the result | 结果
     */
    Object getValue(Object rootObject);

    /**
     * Evaluate the expression with root object and convert to type
     * 使用根对象求值表达式并转换为指定类型
     *
     * @param rootObject the root object | 根对象
     * @param targetType the target type | 目标类型
     * @param <T> the target type | 目标类型
     * @return the typed result | 类型化结果
     */
    <T> T getValue(Object rootObject, Class<T> targetType);

    /**
     * Get the value type
     * 获取值类型
     *
     * @return the value type | 值类型
     */
    Class<?> getValueType();

    /**
     * Get the value type with context
     * 使用上下文获取值类型
     *
     * @param context the evaluation context | 求值上下文
     * @return the value type | 值类型
     */
    Class<?> getValueType(EvaluationContext context);

    /**
     * Check if expression is writable
     * 检查表达式是否可写
     *
     * @return true if writable | 如果可写返回true
     */
    default boolean isWritable() {
        return false;
    }

    /**
     * Set the value of the expression
     * 设置表达式的值
     *
     * @param context the evaluation context | 求值上下文
     * @param value the value to set | 要设置的值
     */
    default void setValue(EvaluationContext context, Object value) {
        throw new UnsupportedOperationException("Expression is not writable");
    }
}
