package cloud.opencode.base.reflect.proxy;

import java.lang.reflect.Method;

/**
 * Method Interceptor Interface
 * 方法拦截器接口
 *
 * <p>Intercepts method invocations on proxy objects.</p>
 * <p>拦截代理对象上的方法调用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Method invocation interception - 方法调用拦截</li>
 *   <li>Chainable interceptors (andThen, compose) - 可链接拦截器</li>
 *   <li>Factory methods: passThrough, constant, throwing - 工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MethodInterceptor logger = (proxy, method, args, invoker) -> {
 *     System.out.println("Calling: " + method.getName());
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
public interface MethodInterceptor {

    /**
     * Intercepts a method invocation
     * 拦截方法调用
     *
     * @param proxy    the proxy object | 代理对象
     * @param method   the method being invoked | 被调用的方法
     * @param args     the method arguments | 方法参数
     * @param invoker  the method invoker for calling the original | 用于调用原始方法的调用器
     * @return the result | 结果
     * @throws Throwable if an error occurs | 如果发生错误
     */
    Object intercept(Object proxy, Method method, Object[] args, MethodInvoker invoker) throws Throwable;

    /**
     * Creates an interceptor that does nothing and just invokes the original
     * 创建一个不做任何事只调用原始方法的拦截器
     *
     * @return the pass-through interceptor | 透传拦截器
     */
    static MethodInterceptor passThrough() {
        return (proxy, method, args, invoker) -> invoker.invoke(args);
    }

    /**
     * Creates an interceptor that always returns a value
     * 创建一个总是返回指定值的拦截器
     *
     * @param value the value to return | 要返回的值
     * @return the constant interceptor | 常量拦截器
     */
    static MethodInterceptor constant(Object value) {
        return (proxy, method, args, invoker) -> value;
    }

    /**
     * Creates an interceptor that throws an exception
     * 创建一个总是抛出异常的拦截器
     *
     * @param exception the exception to throw | 要抛出的异常
     * @return the throwing interceptor | 抛异常拦截器
     */
    static MethodInterceptor throwing(Throwable exception) {
        return (proxy, method, args, invoker) -> {
            throw exception;
        };
    }

    /**
     * Chains this interceptor with another
     * 将此拦截器与另一个链接
     *
     * @param next the next interceptor | 下一个拦截器
     * @return the chained interceptor | 链接后的拦截器
     */
    default MethodInterceptor andThen(MethodInterceptor next) {
        return (proxy, method, args, invoker) -> {
            Object result = this.intercept(proxy, method, args, invoker);
            return next.intercept(proxy, method, args, invoker);
        };
    }

    /**
     * Creates an interceptor that runs before this one
     * 创建一个在此拦截器之前运行的拦截器
     *
     * @param before the before interceptor | 之前的拦截器
     * @return the chained interceptor | 链接后的拦截器
     */
    default MethodInterceptor compose(MethodInterceptor before) {
        return before.andThen(this);
    }
}
