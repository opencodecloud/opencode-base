package cloud.opencode.base.expression.function;

/**
 * Function Interface
 * 函数接口
 *
 * <p>Represents a callable function in expressions.</p>
 * <p>表示表达式中可调用的函数。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Functional interface for lambda-based function definition - 函数式接口，支持lambda定义</li>
 *   <li>Variable argument support - 可变参数支持</li>
 *   <li>Argument count validation - 参数数量验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Register a custom function
 * Function myFunc = args -> ((Number) args[0]).intValue() * 2;
 * registry.register("double", myFunc);
 *
 * // Use in expression
 * Object result = OpenExpression.eval("double(21)");  // 42
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Depends on implementation - 空值安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@FunctionalInterface
public interface Function {

    /**
     * Apply the function
     * 应用函数
     *
     * @param args the arguments | 参数
     * @return the result | 结果
     */
    Object apply(Object... args);

    /**
     * Get function name
     * 获取函数名
     *
     * @return the function name | 函数名
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Get minimum argument count
     * 获取最小参数数量
     *
     * @return the minimum argument count | 最小参数数量
     */
    default int getMinArgs() {
        return 0;
    }

    /**
     * Get maximum argument count
     * 获取最大参数数量
     *
     * @return the maximum argument count, -1 for unlimited | 最大参数数量，-1表示无限
     */
    default int getMaxArgs() {
        return -1;
    }

    /**
     * Check if argument count is valid
     * 检查参数数量是否有效
     *
     * @param argCount the argument count | 参数数量
     * @return true if valid | 如果有效返回true
     */
    default boolean isValidArgCount(int argCount) {
        if (argCount < getMinArgs()) {
            return false;
        }
        return getMaxArgs() < 0 || argCount <= getMaxArgs();
    }
}
