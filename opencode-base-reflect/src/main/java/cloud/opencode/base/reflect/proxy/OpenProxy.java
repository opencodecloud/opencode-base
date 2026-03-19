package cloud.opencode.base.reflect.proxy;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.reflect.*;
import java.util.*;

/**
 * Proxy Facade Entry Class
 * 代理门面入口类
 *
 * <p>Provides common dynamic proxy operations API.</p>
 * <p>提供常用动态代理操作API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>JDK dynamic proxy creation - JDK动态代理创建</li>
 *   <li>Proxy inspection - 代理检查</li>
 *   <li>Invocation handler utilities - 调用处理器工具</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create proxy with interceptor
 * MyService proxy = OpenProxy.create(MyService.class, (p, method, args, invoker) -> {
 *     System.out.println("Before: " + method.getName());
 *     return invoker.invoke(args);
 * });
 *
 * // Check if object is proxy
 * boolean isProxy = OpenProxy.isProxy(obj);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (caller must ensure non-null arguments) - 空值安全: 否（调用方须确保非空参数）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class OpenProxy {

    private OpenProxy() {
    }

    // ==================== Proxy Creation | 代理创建 ====================

    /**
     * Creates a proxy for an interface with interceptor
     * 为接口创建带拦截器的代理
     *
     * @param interfaceClass the interface class | 接口类
     * @param interceptor    the method interceptor | 方法拦截器
     * @param <T>            the interface type | 接口类型
     * @return the proxy | 代理
     */
    public static <T> T create(Class<T> interfaceClass, MethodInterceptor interceptor) {
        return ProxyFactory.forInterface(interfaceClass)
                .intercept(interceptor)
                .create();
    }

    /**
     * Creates a proxy for an interface with handler
     * 为接口创建带处理器的代理
     *
     * @param interfaceClass the interface class | 接口类
     * @param handler        the invocation handler | 调用处理器
     * @param <T>            the interface type | 接口类型
     * @return the proxy | 代理
     */
    public static <T> T create(Class<T> interfaceClass, InvocationHandler handler) {
        return ProxyFactory.forInterface(interfaceClass)
                .handler(handler)
                .create();
    }

    /**
     * Creates a proxy wrapping a target object
     * 创建包装目标对象的代理
     *
     * @param interfaceClass the interface class | 接口类
     * @param target         the target object | 目标对象
     * @param <T>            the interface type | 接口类型
     * @return the proxy | 代理
     */
    public static <T> T wrap(Class<T> interfaceClass, T target) {
        return ProxyFactory.forInterface(interfaceClass)
                .target(target)
                .create();
    }

    /**
     * Creates a proxy wrapping a target with interceptor
     * 创建带拦截器的包装目标对象的代理
     *
     * @param interfaceClass the interface class | 接口类
     * @param target         the target object | 目标对象
     * @param interceptor    the method interceptor | 方法拦截器
     * @param <T>            the interface type | 接口类型
     * @return the proxy | 代理
     */
    public static <T> T wrap(Class<T> interfaceClass, T target, MethodInterceptor interceptor) {
        return ProxyFactory.forInterface(interfaceClass)
                .target(target)
                .intercept(interceptor)
                .create();
    }

    /**
     * Creates a proxy for multiple interfaces
     * 为多个接口创建代理
     *
     * @param handler    the invocation handler | 调用处理器
     * @param interfaces the interfaces | 接口
     * @return the proxy | 代理
     */
    public static Object create(InvocationHandler handler, Class<?>... interfaces) {
        ClassLoader loader = interfaces.length > 0 ? interfaces[0].getClassLoader() : Thread.currentThread().getContextClassLoader();
        return Proxy.newProxyInstance(loader, interfaces, handler);
    }

    /**
     * Creates a factory for building proxies
     * 创建构建代理的工厂
     *
     * @param interfaceClass the interface class | 接口类
     * @param <T>            the interface type | 接口类型
     * @return the factory | 工厂
     */
    public static <T> ProxyFactory<T> factory(Class<T> interfaceClass) {
        return ProxyFactory.forInterface(interfaceClass);
    }

    // ==================== Proxy Inspection | 代理检查 ====================

    /**
     * Checks if an object is a proxy
     * 检查对象是否为代理
     *
     * @param object the object | 对象
     * @return true if proxy | 如果是代理返回true
     */
    public static boolean isProxy(Object object) {
        return object != null && Proxy.isProxyClass(object.getClass());
    }

    /**
     * Gets the invocation handler of a proxy
     * 获取代理的调用处理器
     *
     * @param proxy the proxy | 代理
     * @return the handler | 处理器
     */
    public static InvocationHandler getHandler(Object proxy) {
        if (!isProxy(proxy)) {
            throw new OpenReflectException("Object is not a proxy");
        }
        return Proxy.getInvocationHandler(proxy);
    }

    /**
     * Gets the interfaces implemented by a proxy
     * 获取代理实现的接口
     *
     * @param proxy the proxy | 代理
     * @return the interfaces | 接口
     */
    public static Class<?>[] getInterfaces(Object proxy) {
        if (!isProxy(proxy)) {
            throw new OpenReflectException("Object is not a proxy");
        }
        return proxy.getClass().getInterfaces();
    }

    /**
     * Checks if proxy implements an interface
     * 检查代理是否实现接口
     *
     * @param proxy          the proxy | 代理
     * @param interfaceClass the interface class | 接口类
     * @return true if implements | 如果实现返回true
     */
    public static boolean implementsInterface(Object proxy, Class<?> interfaceClass) {
        if (!isProxy(proxy)) {
            return false;
        }
        for (Class<?> iface : proxy.getClass().getInterfaces()) {
            if (iface.equals(interfaceClass)) {
                return true;
            }
        }
        return false;
    }

    // ==================== Unwrapping | 解包 ====================

    /**
     * Unwraps a proxy to get the underlying target
     * 解包代理获取底层目标
     *
     * @param proxy the proxy | 代理
     * @param <T>   the target type | 目标类型
     * @return the target or null | 目标或null
     */
    @SuppressWarnings("unchecked")
    public static <T> T unwrap(Object proxy) {
        if (!isProxy(proxy)) {
            return (T) proxy;
        }

        InvocationHandler handler = getHandler(proxy);

        // Try to find target field
        try {
            Field targetField = findTargetField(handler.getClass());
            if (targetField != null) {
                targetField.setAccessible(true);
                return (T) targetField.get(handler);
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Creates a no-op proxy that returns default values
     * 创建返回默认值的空操作代理
     *
     * @param interfaceClass the interface class | 接口类
     * @param <T>            the interface type | 接口类型
     * @return the proxy | 代理
     */
    public static <T> T createNoOp(Class<T> interfaceClass) {
        return create(interfaceClass, (proxy, method, args, invoker) -> getDefaultValue(method.getReturnType()));
    }

    /**
     * Creates a mock proxy that records method calls
     * 创建记录方法调用的模拟代理
     *
     * @param interfaceClass the interface class | 接口类
     * @param <T>            the interface type | 接口类型
     * @return the recording proxy | 记录代理
     */
    public static <T> RecordingProxy<T> createRecording(Class<T> interfaceClass) {
        List<MethodCall> calls = new ArrayList<>();
        T proxy = create(interfaceClass, (p, method, args, invoker) -> {
            calls.add(new MethodCall(method, args));
            return getDefaultValue(method.getReturnType());
        });
        return new RecordingProxy<>(proxy, calls);
    }

    private static Field findTargetField(Class<?> handlerClass) {
        for (Field field : handlerClass.getDeclaredFields()) {
            if (field.getName().equals("target") || field.getName().equals("delegate")) {
                return field;
            }
        }
        return null;
    }

    private static Object getDefaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        if (type == char.class) return '\0';
        return null;
    }

    /**
     * Recording proxy wrapper
     * 记录代理包装器
     *
     * @param <T> the interface type | 接口类型
     */
    public record RecordingProxy<T>(T proxy, List<MethodCall> calls) {

        /**
         * Gets the number of method calls
         * 获取方法调用次数
         *
         * @return the count | 计数
         */
        public int getCallCount() {
            return calls.size();
        }

        /**
         * Checks if a method was called
         * 检查方法是否被调用
         *
         * @param methodName the method name | 方法名
         * @return true if called | 如果被调用返回true
         */
        public boolean wasCalled(String methodName) {
            return calls.stream().anyMatch(c -> c.method().getName().equals(methodName));
        }

        /**
         * Gets calls for a specific method
         * 获取特定方法的调用
         *
         * @param methodName the method name | 方法名
         * @return list of calls | 调用列表
         */
        public List<MethodCall> getCallsFor(String methodName) {
            return calls.stream()
                    .filter(c -> c.method().getName().equals(methodName))
                    .toList();
        }

        /**
         * Clears recorded calls
         * 清除记录的调用
         */
        public void clearCalls() {
            calls.clear();
        }
    }

    /**
     * Method call record
     * 方法调用记录
     *
     * @param method the method | 方法
     * @param args   the arguments | 参数
     */
    public record MethodCall(Method method, Object[] args) {

        /**
         * Gets the method name
         * 获取方法名
         *
         * @return the name | 名称
         */
        public String getMethodName() {
            return method.getName();
        }

        /**
         * Gets the argument count
         * 获取参数数量
         *
         * @return the count | 数量
         */
        public int getArgCount() {
            return args != null ? args.length : 0;
        }

        /**
         * Gets an argument by index
         * 按索引获取参数
         *
         * @param index the index | 索引
         * @param <T>   the argument type | 参数类型
         * @return the argument | 参数
         */
        @SuppressWarnings("unchecked")
        public <T> T getArg(int index) {
            return (T) args[index];
        }
    }
}
