package cloud.opencode.base.expression.sandbox;

import java.lang.reflect.Method;

/**
 * Security Sandbox Interface
 * 安全沙箱接口
 *
 * <p>Provides security constraints for expression evaluation.</p>
 * <p>为表达式求值提供安全约束。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Class access control - 类访问控制</li>
 *   <li>Method call control - 方法调用控制</li>
 *   <li>Property access control - 属性访问控制</li>
 *   <li>Construction control - 构造控制</li>
 *   <li>Expression length, depth, and time limits - 表达式长度、深度和时间限制</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Sandbox sandbox = DefaultSandbox.standard();
 * StandardContext ctx = StandardContext.builder()
 *     .sandbox(sandbox)
 *     .build();
 * // Expressions evaluated with ctx will be constrained by the sandbox
 * Object result = OpenExpression.eval("user.name", ctx);
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
public interface Sandbox {

    /**
     * Check if a class is allowed
     * 检查是否允许访问类
     *
     * @param clazz the class | 类
     * @return true if allowed | 如果允许返回true
     */
    boolean isClassAllowed(Class<?> clazz);

    /**
     * Check if a method call is allowed
     * 检查是否允许调用方法
     *
     * @param target the target object | 目标对象
     * @param method the method | 方法
     * @return true if allowed | 如果允许返回true
     */
    boolean isMethodAllowed(Object target, Method method);

    /**
     * Check if a property access is allowed
     * 检查是否允许访问属性
     *
     * @param target the target object | 目标对象
     * @param property the property name | 属性名
     * @return true if allowed | 如果允许返回true
     */
    boolean isPropertyAllowed(Object target, String property);

    /**
     * Check if a type construction is allowed
     * 检查是否允许类型构造
     *
     * @param clazz the class to construct | 要构造的类
     * @return true if allowed | 如果允许返回true
     */
    default boolean isConstructionAllowed(Class<?> clazz) {
        return isClassAllowed(clazz);
    }

    /**
     * Get the maximum expression length
     * 获取最大表达式长度
     *
     * @return the max length, -1 for unlimited | 最大长度，-1表示无限
     */
    default int getMaxExpressionLength() {
        return -1;
    }

    /**
     * Get the maximum evaluation depth
     * 获取最大求值深度
     *
     * @return the max depth, -1 for unlimited | 最大深度，-1表示无限
     */
    default int getMaxEvaluationDepth() {
        return 100;
    }

    /**
     * Get the maximum evaluation time in milliseconds
     * 获取最大求值时间（毫秒）
     *
     * @return the max time, -1 for unlimited | 最大时间，-1表示无限
     */
    default long getMaxEvaluationTime() {
        return -1;
    }
}
