package cloud.opencode.base.reflect.proxy;

/**
 * Method Invoker Interface
 * 方法调用器接口
 *
 * <p>Provides a way to invoke the original method from an interceptor.</p>
 * <p>提供从拦截器调用原始方法的方式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Original method invocation from interceptor - 从拦截器调用原始方法</li>
 *   <li>No-op and constant factory methods - 空操作和常量工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Used inside MethodInterceptor
 * MethodInterceptor interceptor = (proxy, method, args, invoker) -> {
 *     // invoke the original method
 *     return invoker.invoke(args);
 * };
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (args may be null for no-arg methods) - 空值安全: 否（无参方法的args可能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@FunctionalInterface
public interface MethodInvoker {

    /**
     * Invokes the original method
     * 调用原始方法
     *
     * @param args the arguments | 参数
     * @return the result | 结果
     * @throws Throwable if an error occurs | 如果发生错误
     */
    Object invoke(Object[] args) throws Throwable;

    /**
     * Creates a no-op invoker that returns null
     * 创建一个返回null的空操作调用器
     *
     * @return the no-op invoker | 空操作调用器
     */
    static MethodInvoker noOp() {
        return args -> null;
    }

    /**
     * Creates an invoker that always returns a constant
     * 创建一个总是返回常量的调用器
     *
     * @param value the value to return | 要返回的值
     * @return the constant invoker | 常量调用器
     */
    static MethodInvoker constant(Object value) {
        return args -> value;
    }
}
