package cloud.opencode.base.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock Entry Class - Provides interface mocking capabilities
 * Mock入口类 - 提供接口模拟能力
 *
 * <p>Zero-dependency mock library for creating interface mocks without
 * external bytecode manipulation libraries.</p>
 * <p>零依赖模拟库，无需外部字节码操作库即可创建接口模拟。</p>
 *
 * <p><strong>Limitations | 限制:</strong></p>
 * <ul>
 *   <li>Only supports interface mocking (not classes) - 仅支持接口模拟（非类）</li>
 *   <li>Uses JDK Proxy - 使用JDK代理</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Create mock interfaces - 创建模拟接口</li>
 *   <li>Stub method returns - 桩方法返回值</li>
 *   <li>Verify invocations - 验证调用</li>
 *   <li>Thread-safe - 线程安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple mock
 * UserService mock = OpenMock.mock(UserService.class);
 *
 * // With stubbing
 * UserService mock = OpenMock.when(UserService.class)
 *     .thenReturn("getName", "John")
 *     .thenReturn("getAge", 25)
 *     .build();
 *
 * // Verify invocations
 * OpenMock.verify(mock).wasInvoked("getName");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (synchronized handler map, ConcurrentHashMap stubs) - 线程安全: 是（同步处理器映射，ConcurrentHashMap存根）</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class OpenMock {

    private static final Map<Object, MockInvocationHandler> HANDLERS = new ConcurrentHashMap<>();

    private OpenMock() {
    }

    // ==================== Mock Creation | Mock创建 ====================

    /**
     * Creates a mock of the given interface type
     * 创建给定接口类型的模拟
     *
     * @param type the interface type to mock | 要模拟的接口类型
     * @param <T>  the type | 类型
     * @return the mock instance | 模拟实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T mock(Class<T> type) {
        if (!type.isInterface()) {
            throw new IllegalArgumentException("OpenMock only supports interfaces. Class: " + type.getName());
        }
        MockInvocationHandler handler = new MockInvocationHandler();
        T proxy = (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                handler
        );
        HANDLERS.put(proxy, handler);
        return proxy;
    }

    /**
     * Creates a mock builder for fluent configuration
     * 创建用于流畅配置的模拟构建器
     *
     * @param type the interface type to mock | 要模拟的接口类型
     * @param <T>  the type | 类型
     * @return the mock builder | 模拟构建器
     */
    public static <T> MockBuilder<T> when(Class<T> type) {
        return new MockBuilder<>(type);
    }

    // ==================== Verification | 验证 ====================

    /**
     * Gets verification wrapper for the mock
     * 获取模拟的验证包装器
     *
     * @param mock the mock instance | 模拟实例
     * @param <T>  the type | 类型
     * @return the verification wrapper | 验证包装器
     */
    public static <T> Verification<T> verify(T mock) {
        MockInvocationHandler handler = HANDLERS.get(mock);
        if (handler == null) {
            throw new IllegalArgumentException("Not a mock object created by OpenMock");
        }
        return new Verification<>(handler);
    }

    /**
     * Resets all recorded invocations for the mock
     * 重置模拟的所有记录调用
     *
     * @param mock the mock instance | 模拟实例
     */
    public static void reset(Object mock) {
        MockInvocationHandler handler = HANDLERS.get(mock);
        if (handler != null) {
            handler.reset();
        }
    }

    // ==================== Mock Invocation Handler | Mock调用处理器 ====================

    /**
     * Mock Invocation Handler - Handles method calls on mock
     * Mock调用处理器 - 处理mock上的方法调用
     */
    static class MockInvocationHandler implements InvocationHandler {
        private final Map<MethodKey, Object> stubs = new ConcurrentHashMap<>();
        private final List<Invocation> invocations = Collections.synchronizedList(new ArrayList<>());

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            // Handle Object methods
            if (method.getName().equals("toString")) {
                return "Mock@" + System.identityHashCode(proxy);
            }
            if (method.getName().equals("hashCode")) {
                return System.identityHashCode(proxy);
            }
            if (method.getName().equals("equals")) {
                return proxy == args[0];
            }

            invocations.add(new Invocation(method.getName(), method.getParameterTypes(), args));

            MethodKey key = new MethodKey(method.getName(), args);
            Object stubResult = stubs.get(key);
            if (stubResult != null) {
                return stubResult;
            }

            // Also check for method name only stub
            MethodKey nameOnlyKey = new MethodKey(method.getName(), null);
            stubResult = stubs.get(nameOnlyKey);
            if (stubResult != null) {
                return stubResult;
            }

            return getDefaultValue(method.getReturnType());
        }

        public void stub(String methodName, Object[] args, Object returnValue) {
            stubs.put(new MethodKey(methodName, args), returnValue);
        }

        public List<Invocation> getInvocations() {
            return new ArrayList<>(invocations);
        }

        public void reset() {
            invocations.clear();
            stubs.clear();
        }

        private Object getDefaultValue(Class<?> type) {
            if (!type.isPrimitive()) return null;
            if (type == boolean.class) return false;
            if (type == char.class) return '\0';
            if (type == byte.class) return (byte) 0;
            if (type == short.class) return (short) 0;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0.0f;
            if (type == double.class) return 0.0d;
            return 0;
        }
    }

    // ==================== Helper Records | 辅助记录类 ====================

    /**
     * Method key for stub lookup
     * 用于桩查找的方法键
     */
    record MethodKey(String name, Object[] args) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MethodKey mk)) return false;
            return name.equals(mk.name) && Arrays.deepEquals(args, mk.args);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, Arrays.deepHashCode(args));
        }
    }

    /**
     * Invocation record
     * 调用记录
     */
    public record Invocation(String methodName, Class<?>[] parameterTypes, Object[] args) {
    }

    // ==================== Mock Builder | Mock构建器 ====================

    /**
     * Mock Builder for fluent mock configuration
     * 用于流畅mock配置的Mock构建器
     *
     * @param <T> the mock type | mock类型
     */
    public static class MockBuilder<T> {
        private final Class<T> type;
        private final MockInvocationHandler handler = new MockInvocationHandler();

        MockBuilder(Class<T> type) {
            if (!type.isInterface()) {
                throw new IllegalArgumentException("OpenMock only supports interfaces. Class: " + type.getName());
            }
            this.type = type;
        }

        /**
         * Stubs method to return value
         * 桩方法返回值
         *
         * @param methodName  the method name | 方法名
         * @param returnValue the return value | 返回值
         * @return this builder | 此构建器
         */
        public MockBuilder<T> thenReturn(String methodName, Object returnValue) {
            handler.stub(methodName, null, returnValue);
            return this;
        }

        /**
         * Stubs method with specific args to return value
         * 桩特定参数的方法返回值
         *
         * @param methodName  the method name | 方法名
         * @param args        the arguments | 参数
         * @param returnValue the return value | 返回值
         * @return this builder | 此构建器
         */
        public MockBuilder<T> thenReturn(String methodName, Object[] args, Object returnValue) {
            handler.stub(methodName, args, returnValue);
            return this;
        }

        /**
         * Builds the mock
         * 构建模拟
         *
         * @return the mock instance | 模拟实例
         */
        @SuppressWarnings("unchecked")
        public T build() {
            T proxy = (T) Proxy.newProxyInstance(
                    type.getClassLoader(),
                    new Class<?>[]{type},
                    handler
            );
            synchronized (HANDLERS) {
                HANDLERS.put(proxy, handler);
            }
            return proxy;
        }
    }

    // ==================== Verification | 验证 ====================

    /**
     * Verification wrapper for asserting mock invocations
     * 用于断言mock调用的验证包装器
     *
     * @param <T> the mock type | mock类型
     */
    public static class Verification<T> {
        private final MockInvocationHandler handler;

        Verification(MockInvocationHandler handler) {
            this.handler = handler;
        }

        /**
         * Verifies method was invoked
         * 验证方法被调用
         *
         * @param methodName the method name | 方法名
         * @return this verification | 此验证
         */
        public Verification<T> wasInvoked(String methodName) {
            boolean found = handler.getInvocations().stream()
                    .anyMatch(inv -> inv.methodName().equals(methodName));
            if (!found) {
                throw new AssertionError("Method was not invoked: " + methodName);
            }
            return this;
        }

        /**
         * Verifies method was invoked exactly N times
         * 验证方法被调用恰好N次
         *
         * @param methodName the method name | 方法名
         * @param times      the expected times | 期望次数
         * @return this verification | 此验证
         */
        public Verification<T> wasInvoked(String methodName, int times) {
            long count = handler.getInvocations().stream()
                    .filter(inv -> inv.methodName().equals(methodName))
                    .count();
            if (count != times) {
                throw new AssertionError(
                        "Method " + methodName + " was invoked " + count + " times, expected " + times);
            }
            return this;
        }

        /**
         * Verifies method was never invoked
         * 验证方法从未被调用
         *
         * @param methodName the method name | 方法名
         * @return this verification | 此验证
         */
        public Verification<T> wasNeverInvoked(String methodName) {
            return wasInvoked(methodName, 0);
        }

        /**
         * Gets the invocation count for a method
         * 获取方法的调用次数
         *
         * @param methodName the method name | 方法名
         * @return the count | 次数
         */
        public int invocationCount(String methodName) {
            return (int) handler.getInvocations().stream()
                    .filter(inv -> inv.methodName().equals(methodName))
                    .count();
        }

        /**
         * Gets all invocations
         * 获取所有调用
         *
         * @return the invocations | 调用列表
         */
        public List<Invocation> getAllInvocations() {
            return handler.getInvocations();
        }
    }
}
