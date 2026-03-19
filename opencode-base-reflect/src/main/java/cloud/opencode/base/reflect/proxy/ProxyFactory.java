package cloud.opencode.base.reflect.proxy;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.BiFunction;

/**
 * Proxy Factory
 * 代理工厂
 *
 * <p>Fluent factory for creating dynamic proxies with various configurations.</p>
 * <p>用于创建具有各种配置的动态代理的流式工厂。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent proxy configuration API - 流式代理配置API</li>
 *   <li>Multiple interface support - 多接口支持</li>
 *   <li>Method-level interceptor binding - 方法级拦截器绑定</li>
 *   <li>Target delegation - 目标委托</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MyService proxy = ProxyFactory.forInterface(MyService.class)
 *     .target(realService)
 *     .intercept(loggingInterceptor)
 *     .create();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (builder pattern, not thread-safe during construction) - 线程安全: 否（构建器模式，构建期间非线程安全）</li>
 *   <li>Null-safe: No (caller must ensure non-null interface class) - 空值安全: 否（调用方须确保非空接口类）</li>
 * </ul>
 *
 * @param <T> the primary interface type | 主接口类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public class ProxyFactory<T> {

    private final Class<T> primaryInterface;
    private final List<Class<?>> additionalInterfaces;
    private ClassLoader classLoader;
    private Object target;
    private final Map<Method, MethodInterceptor> methodInterceptors;
    private MethodInterceptor defaultInterceptor;
    private InvocationHandler customHandler;

    private ProxyFactory(Class<T> primaryInterface) {
        this.primaryInterface = Objects.requireNonNull(primaryInterface, "primaryInterface must not be null");
        this.additionalInterfaces = new ArrayList<>();
        this.classLoader = primaryInterface.getClassLoader();
        this.methodInterceptors = new HashMap<>();
    }

    /**
     * Creates a ProxyFactory for an interface
     * 为接口创建ProxyFactory
     *
     * @param interfaceClass the interface class | 接口类
     * @param <T>            the interface type | 接口类型
     * @return the factory | 工厂
     */
    public static <T> ProxyFactory<T> forInterface(Class<T> interfaceClass) {
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("Must be an interface: " + interfaceClass.getName());
        }
        return new ProxyFactory<>(interfaceClass);
    }

    /**
     * Adds an additional interface
     * 添加额外接口
     *
     * @param interfaceClass the interface class | 接口类
     * @return this factory | 此工厂
     */
    public ProxyFactory<T> implement(Class<?> interfaceClass) {
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("Must be an interface: " + interfaceClass.getName());
        }
        additionalInterfaces.add(interfaceClass);
        return this;
    }

    /**
     * Sets the ClassLoader
     * 设置ClassLoader
     *
     * @param classLoader the class loader | 类加载器
     * @return this factory | 此工厂
     */
    public ProxyFactory<T> classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    /**
     * Sets a target object for delegation
     * 设置委托的目标对象
     *
     * @param target the target object | 目标对象
     * @return this factory | 此工厂
     */
    public ProxyFactory<T> target(Object target) {
        this.target = target;
        return this;
    }

    /**
     * Sets the default interceptor
     * 设置默认拦截器
     *
     * @param interceptor the interceptor | 拦截器
     * @return this factory | 此工厂
     */
    public ProxyFactory<T> intercept(MethodInterceptor interceptor) {
        this.defaultInterceptor = interceptor;
        return this;
    }

    /**
     * Sets an interceptor for a specific method
     * 为特定方法设置拦截器
     *
     * @param methodName  the method name | 方法名
     * @param interceptor the interceptor | 拦截器
     * @return this factory | 此工厂
     */
    public ProxyFactory<T> intercept(String methodName, MethodInterceptor interceptor) {
        for (Method method : primaryInterface.getMethods()) {
            if (method.getName().equals(methodName)) {
                methodInterceptors.put(method, interceptor);
            }
        }
        return this;
    }

    /**
     * Sets an interceptor for a specific method with parameter types
     * 为特定方法（带参数类型）设置拦截器
     *
     * @param methodName     the method name | 方法名
     * @param parameterTypes the parameter types | 参数类型
     * @param interceptor    the interceptor | 拦截器
     * @return this factory | 此工厂
     */
    public ProxyFactory<T> intercept(String methodName, Class<?>[] parameterTypes, MethodInterceptor interceptor) {
        try {
            Method method = primaryInterface.getMethod(methodName, parameterTypes);
            methodInterceptors.put(method, interceptor);
        } catch (NoSuchMethodException e) {
            throw new OpenReflectException("Method not found: " + methodName, e);
        }
        return this;
    }

    /**
     * Sets a custom invocation handler
     * 设置自定义调用处理器
     *
     * @param handler the handler | 处理器
     * @return this factory | 此工厂
     */
    public ProxyFactory<T> handler(InvocationHandler handler) {
        this.customHandler = handler;
        return this;
    }

    /**
     * Creates the proxy
     * 创建代理
     *
     * @return the proxy instance | 代理实例
     */
    @SuppressWarnings("unchecked")
    public T create() {
        Class<?>[] interfaces = getInterfaces();
        InvocationHandler handler = createHandler();

        return (T) Proxy.newProxyInstance(classLoader, interfaces, handler);
    }

    private Class<?>[] getInterfaces() {
        List<Class<?>> allInterfaces = new ArrayList<>();
        allInterfaces.add(primaryInterface);
        allInterfaces.addAll(additionalInterfaces);
        return allInterfaces.toArray(new Class<?>[0]);
    }

    private InvocationHandler createHandler() {
        if (customHandler != null) {
            return customHandler;
        }

        return new InterceptingHandler(target, methodInterceptors, defaultInterceptor);
    }

    /**
     * Intercepting InvocationHandler implementation
     * 拦截InvocationHandler实现
     */
    private static class InterceptingHandler extends AbstractInvocationHandler {
        private final Object target;
        private final Map<Method, MethodInterceptor> methodInterceptors;
        private final MethodInterceptor defaultInterceptor;

        InterceptingHandler(Object target, Map<Method, MethodInterceptor> methodInterceptors,
                            MethodInterceptor defaultInterceptor) {
            this.target = target;
            this.methodInterceptors = new HashMap<>(methodInterceptors);
            this.defaultInterceptor = defaultInterceptor;
        }

        @Override
        protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
            MethodInterceptor interceptor = methodInterceptors.get(method);
            if (interceptor == null) {
                interceptor = defaultInterceptor;
            }

            MethodInvoker invoker = createInvoker(method);

            if (interceptor != null) {
                return interceptor.intercept(proxy, method, args, invoker);
            }

            return invoker.invoke(args);
        }

        private MethodInvoker createInvoker(Method method) {
            if (target != null) {
                return args -> method.invoke(target, args);
            }
            return MethodInvoker.noOp();
        }
    }

    /**
     * Builder for creating proxy with lambda-style configuration
     * 用Lambda风格配置创建代理的构建器
     *
     * @param <T> the interface type | 接口类型
     */
    public static class Builder<T> {
        private final ProxyFactory<T> factory;

        private Builder(ProxyFactory<T> factory) {
            this.factory = factory;
        }

        /**
         * Creates a builder for an interface
         * 为接口创建构建器
         *
         * @param interfaceClass the interface class | 接口类
         * @param <T>            the interface type | 接口类型
         * @return the builder | 构建器
         */
        public static <T> Builder<T> of(Class<T> interfaceClass) {
            return new Builder<>(ProxyFactory.forInterface(interfaceClass));
        }

        /**
         * Sets the handler function
         * 设置处理器函数
         *
         * @param handler the handler (method, args) -&gt; result | 处理器
         * @return this builder | 此构建器
         */
        public Builder<T> handle(BiFunction<Method, Object[], Object> handler) {
            factory.intercept((proxy, method, args, invoker) -> handler.apply(method, args));
            return this;
        }

        /**
         * Creates the proxy
         * 创建代理
         *
         * @return the proxy | 代理
         */
        public T build() {
            return factory.create();
        }
    }
}
