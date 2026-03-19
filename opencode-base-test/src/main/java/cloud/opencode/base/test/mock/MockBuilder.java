package cloud.opencode.base.test.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Mock Builder
 * 模拟构建器
 *
 * <p>Simple mock builder for interfaces.</p>
 * <p>接口的简单模拟构建器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core functionality - 核心功能</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use type
 * type instance = ...;
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 * </ul>
 *
 *
 * @param <T> the interface type | 接口类型
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) to build; O(1) per method invocation on the mock - 时间复杂度: O(1) 构建；O(1) 每次方法调用</li>
 *   <li>Space complexity: O(m) where m is the number of registered method handlers - 空间复杂度: O(m)，m 为注册的方法处理器数量</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public class MockBuilder<T> {

    private final Class<T> interfaceType;
    private final Map<String, Function<Object[], Object>> methodHandlers = new HashMap<>();
    private Function<Method, Object> defaultHandler = m -> null;

    private MockBuilder(Class<T> interfaceType) {
        if (!interfaceType.isInterface()) {
            throw new IllegalArgumentException("MockBuilder only supports interfaces: " + interfaceType.getName());
        }
        this.interfaceType = interfaceType;
    }

    /**
     * Create builder for interface
     * 为接口创建构建器
     *
     * @param interfaceType the interface type | 接口类型
     * @param <T> the type | 类型
     * @return the builder | 构建器
     */
    public static <T> MockBuilder<T> of(Class<T> interfaceType) {
        return new MockBuilder<>(interfaceType);
    }

    /**
     * When method called, return value
     * 当方法调用时，返回值
     *
     * @param methodName the method name | 方法名称
     * @param returnValue the return value | 返回值
     * @return this builder | 此构建器
     */
    public MockBuilder<T> when(String methodName, Object returnValue) {
        methodHandlers.put(methodName, args -> returnValue);
        return this;
    }

    /**
     * When method called, execute function
     * 当方法调用时，执行函数
     *
     * @param methodName the method name | 方法名称
     * @param handler the handler function | 处理函数
     * @return this builder | 此构建器
     */
    public MockBuilder<T> when(String methodName, Function<Object[], Object> handler) {
        methodHandlers.put(methodName, handler);
        return this;
    }

    /**
     * Set default return value
     * 设置默认返回值
     *
     * @param defaultValue the default value | 默认值
     * @return this builder | 此构建器
     */
    public MockBuilder<T> defaultReturn(Object defaultValue) {
        this.defaultHandler = m -> defaultValue;
        return this;
    }

    /**
     * Set default handler
     * 设置默认处理器
     *
     * @param handler the handler | 处理器
     * @return this builder | 此构建器
     */
    public MockBuilder<T> defaultHandler(Function<Method, Object> handler) {
        this.defaultHandler = handler;
        return this;
    }

    /**
     * Build the mock
     * 构建模拟对象
     *
     * @return the mock instance | 模拟实例
     */
    @SuppressWarnings("unchecked")
    public T build() {
        InvocationHandler handler = (proxy, method, args) -> {
            // Handle Object methods
            if (method.getName().equals("toString")) {
                return "Mock[" + interfaceType.getSimpleName() + "]";
            }
            if (method.getName().equals("hashCode")) {
                return System.identityHashCode(proxy);
            }
            if (method.getName().equals("equals")) {
                return proxy == args[0];
            }

            // Check for registered handler
            Function<Object[], Object> methodHandler = methodHandlers.get(method.getName());
            if (methodHandler != null) {
                return methodHandler.apply(args);
            }

            // Use default handler
            return defaultHandler.apply(method);
        };

        return (T) Proxy.newProxyInstance(
            interfaceType.getClassLoader(),
            new Class<?>[]{interfaceType},
            handler
        );
    }

    /**
     * Quick create mock with default values
     * 快速创建带默认值的模拟
     *
     * @param interfaceType the interface type | 接口类型
     * @param <T> the type | 类型
     * @return the mock | 模拟对象
     */
    public static <T> T mock(Class<T> interfaceType) {
        return MockBuilder.of(interfaceType).build();
    }
}
