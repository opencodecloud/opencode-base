package cloud.opencode.base.reflect.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * Abstract Invocation Handler
 * 抽象调用处理器
 *
 * <p>Base class for creating custom invocation handlers.
 * Provides default implementations for Object methods.</p>
 * <p>创建自定义调用处理器的基类。
 * 为Object方法提供默认实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Default equals/hashCode/toString handling - 默认equals/hashCode/toString处理</li>
 *   <li>Simplified handler implementation - 简化的处理器实现</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * InvocationHandler handler = new AbstractInvocationHandler() {
 *     protected Object handleInvocation(Object proxy, Method method, Object[] args) {
 *         return method.invoke(target, args);
 *     }
 * };
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on subclass implementation - 线程安全: 取决于子类实现</li>
 *   <li>Null-safe: No (caller must ensure non-null proxy) - 空值安全: 否（调用方须确保非空代理）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public abstract class AbstractInvocationHandler implements InvocationHandler {

    /**
     * Default constructor
     * 默认构造器
     */
    protected AbstractInvocationHandler() {
    }

    @Override
    public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Handle Object methods
        if (method.getDeclaringClass() == Object.class) {
            return handleObjectMethod(proxy, method, args);
        }

        // Handle default methods
        if (method.isDefault()) {
            return handleDefaultMethod(proxy, method, args);
        }

        // Delegate to subclass
        return handleInvocation(proxy, method, args);
    }

    /**
     * Handles the actual method invocation
     * 处理实际的方法调用
     *
     * @param proxy  the proxy object | 代理对象
     * @param method the method | 方法
     * @param args   the arguments | 参数
     * @return the result | 结果
     * @throws Throwable if an error occurs | 如果发生错误
     */
    protected abstract Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable;

    /**
     * Handles Object class methods
     * 处理Object类方法
     *
     * @param proxy  the proxy object | 代理对象
     * @param method the method | 方法
     * @param args   the arguments | 参数
     * @return the result | 结果
     */
    protected Object handleObjectMethod(Object proxy, Method method, Object[] args) {
        String name = method.getName();

        return switch (name) {
            case "equals" -> proxyEquals(proxy, args[0]);
            case "hashCode" -> proxyHashCode(proxy);
            case "toString" -> proxyToString(proxy);
            default -> throw new UnsupportedOperationException("Unsupported Object method: " + name);
        };
    }

    /**
     * Handles interface default methods
     * 处理接口默认方法
     *
     * @param proxy  the proxy object | 代理对象
     * @param method the method | 方法
     * @param args   the arguments | 参数
     * @return the result | 结果
     * @throws Throwable if an error occurs | 如果发生错误
     */
    protected Object handleDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable {
        // Use MethodHandle to invoke default method
        return java.lang.invoke.MethodHandles.lookup()
                .findSpecial(
                        method.getDeclaringClass(),
                        method.getName(),
                        java.lang.invoke.MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
                        method.getDeclaringClass()
                )
                .bindTo(proxy)
                .invokeWithArguments(args);
    }

    /**
     * Determines equality for the proxy
     * 确定代理的相等性
     *
     * @param proxy the proxy | 代理
     * @param other the other object | 另一个对象
     * @return true if equal | 如果相等返回true
     */
    protected boolean proxyEquals(Object proxy, Object other) {
        if (proxy == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!Proxy.isProxyClass(other.getClass())) {
            return false;
        }
        InvocationHandler handler = Proxy.getInvocationHandler(other);
        return this.equals(handler);
    }

    /**
     * Computes hash code for the proxy
     * 计算代理的哈希码
     *
     * @param proxy the proxy | 代理
     * @return the hash code | 哈希码
     */
    protected int proxyHashCode(Object proxy) {
        return this.hashCode();
    }

    /**
     * Returns string representation for the proxy
     * 返回代理的字符串表示
     *
     * @param proxy the proxy | 代理
     * @return the string representation | 字符串表示
     */
    protected String proxyToString(Object proxy) {
        return proxy.getClass().getName() + "@" + Integer.toHexString(proxyHashCode(proxy));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AbstractInvocationHandler;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
