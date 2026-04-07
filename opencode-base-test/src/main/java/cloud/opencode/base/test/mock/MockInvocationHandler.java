package cloud.opencode.base.test.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Mock Invocation Handler - Handles method invocations on mock proxies
 * Mock调用处理器 - 处理Mock代理上的方法调用
 *
 * <p>Records method invocations and returns stubbed values.</p>
 * <p>记录方法调用并返回存根值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Invocation recording with timestamps - 带时间戳的调用记录</li>
 *   <li>Method stubbing (return value, throw, answer) - 方法存根（返回值、抛异常、应答）</li>
 *   <li>Invocation counting and filtering - 调用计数和过滤</li>
 *   <li>Default value return for primitive types - 原始类型默认值返回</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MockInvocationHandler handler = new MockInvocationHandler(UserService.class);
 *
 * // Configure stubbing
 * handler.when("findById", 1L).thenReturn(new User("John"));
 * handler.when("save").thenAnswer(args -> args[0]);
 * handler.when("delete").thenThrow(new RuntimeException("Error"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (CopyOnWriteArrayList for invocations, ConcurrentHashMap for stubs) - 线程安全: 是（调用使用CopyOnWriteArrayList，存根使用ConcurrentHashMap）</li>
 *   <li>Null-safe: Yes (validates mocked type) - 空值安全: 是（验证Mock类型）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class MockInvocationHandler implements InvocationHandler {

    private final Class<?> mockedType;
    private final List<Invocation> invocations = Collections.synchronizedList(new ArrayList<>());
    private final Map<StubKey, Stub> stubs = new ConcurrentHashMap<>();

    /**
     * Creates handler for mocked type.
     * 为Mock类型创建处理器。
     *
     * @param mockedType the mocked type | Mock类型
     */
    public MockInvocationHandler(Class<?> mockedType) {
        this.mockedType = Objects.requireNonNull(mockedType, "mockedType cannot be null");
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();

        // Handle Object methods
        if ("toString".equals(methodName) && (args == null || args.length == 0)) {
            return "Mock[" + mockedType.getSimpleName() + "]";
        }
        if ("hashCode".equals(methodName) && (args == null || args.length == 0)) {
            return System.identityHashCode(proxy);
        }
        if ("equals".equals(methodName) && args != null && args.length == 1) {
            return proxy == args[0];
        }

        // Record invocation
        Invocation invocation = Invocation.of(method, args);
        invocations.add(invocation);

        // Fast-path: skip stub lookup when no stubs are configured
        if (!stubs.isEmpty()) {
            // Look for stub
            StubKey key = new StubKey(methodName, args);
            Stub stub = stubs.get(key);

            // Try without args match
            if (stub == null) {
                stub = stubs.get(new StubKey(methodName, null));
            }

            if (stub != null) {
                return stub.apply(args);
            }
        }

        // Return default value
        return getDefaultValue(method.getReturnType());
    }

    /**
     * Sets up stubbing for method.
     * 为方法设置存根。
     *
     * @param methodName the method name | 方法名
     * @param args       the expected arguments (or empty for any) | 期望参数（或空表示任意）
     * @return the stubbing | 存根配置
     */
    public Stubbing when(String methodName, Object... args) {
        return new Stubbing(this, methodName, args.length > 0 ? args : null);
    }

    /**
     * Gets all recorded invocations.
     * 获取所有记录的调用。
     *
     * @return the invocations | 调用列表
     */
    public List<Invocation> getInvocations() {
        return Collections.unmodifiableList(new ArrayList<>(invocations));
    }

    /**
     * Gets invocations for method.
     * 获取方法的调用。
     *
     * @param methodName the method name | 方法名
     * @return the invocations | 调用列表
     */
    public List<Invocation> getInvocations(String methodName) {
        return invocations.stream()
                .filter(i -> i.isMethod(methodName))
                .toList();
    }

    /**
     * Counts invocations for method.
     * 统计方法的调用次数。
     *
     * @param methodName the method name | 方法名
     * @return the count | 次数
     */
    public int countInvocations(String methodName) {
        return (int) invocations.stream()
                .filter(i -> i.isMethod(methodName))
                .count();
    }

    /**
     * Counts invocations for method with specific args.
     * 统计特定参数方法的调用次数。
     *
     * @param methodName the method name | 方法名
     * @param args       the expected arguments | 期望参数
     * @return the count | 次数
     */
    public int countInvocations(String methodName, Object... args) {
        return (int) invocations.stream()
                .filter(i -> i.isMethod(methodName) && i.argsMatch(args))
                .count();
    }

    /**
     * Clears all invocations.
     * 清除所有调用。
     */
    public void clearInvocations() {
        invocations.clear();
    }

    /**
     * Resets handler (clears invocations and stubs).
     * 重置处理器（清除调用和存根）。
     */
    public void reset() {
        invocations.clear();
        stubs.clear();
    }

    /**
     * Gets the mocked type.
     * 获取Mock类型。
     *
     * @return the mocked type | Mock类型
     */
    public Class<?> getMockedType() {
        return mockedType;
    }

    void addStub(String methodName, Object[] args, Stub stub) {
        stubs.put(new StubKey(methodName, args), stub);
    }

    private Object getDefaultValue(Class<?> type) {
        if (type == void.class || type == Void.class) return null;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == double.class) return 0.0d;
        if (type == char.class) return '\0';
        return null;
    }

    /**
     * Stub key for matching.
     */
    private record StubKey(String methodName, Object[] args) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StubKey key)) return false;
            return methodName.equals(key.methodName) && Arrays.deepEquals(args, key.args);
        }

        @Override
        public int hashCode() {
            return Objects.hash(methodName, Arrays.deepHashCode(args));
        }
    }

    /**
     * Stub interface.
     */
    @FunctionalInterface
    interface Stub {
        Object apply(Object[] args) throws Throwable;
    }

    /**
     * Stubbing configuration.
     * 存根配置。
     */
    public static class Stubbing {
        private final MockInvocationHandler handler;
        private final String methodName;
        private final Object[] args;

        Stubbing(MockInvocationHandler handler, String methodName, Object[] args) {
            this.handler = handler;
            this.methodName = methodName;
            this.args = args;
        }

        /**
         * Returns specified value.
         * 返回指定值。
         *
         * @param value the value to return | 要返回的值
         * @return this | 此对象
         */
        public Stubbing thenReturn(Object value) {
            handler.addStub(methodName, args, a -> value);
            return this;
        }

        /**
         * Throws specified exception.
         * 抛出指定异常。
         *
         * @param throwable the exception to throw | 要抛出的异常
         * @return this | 此对象
         */
        public Stubbing thenThrow(Throwable throwable) {
            handler.addStub(methodName, args, a -> { throw throwable; });
            return this;
        }

        /**
         * Answers with function.
         * 使用函数应答。
         *
         * @param answer the answer function | 应答函数
         * @return this | 此对象
         */
        public Stubbing thenAnswer(Function<Object[], Object> answer) {
            handler.addStub(methodName, args, answer::apply);
            return this;
        }

        /**
         * Calls real method (for partial mocks).
         * 调用真实方法（部分Mock）。
         *
         * @return this | 此对象
         */
        public Stubbing thenCallRealMethod() {
            // Not supported for interface mocks
            throw new UnsupportedOperationException("Cannot call real method on interface mock");
        }
    }
}
