package cloud.opencode.base.test.mock;

import cloud.opencode.base.test.exception.MockException;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;

/**
 * Mock Proxy - Factory for creating mock proxy instances
 * Mock代理 - 创建Mock代理实例的工厂
 *
 * <p>Creates dynamic proxy instances for interface mocking in tests.</p>
 * <p>为测试中的接口Mock创建动态代理实例。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>JDK dynamic proxy-based mock creation - 基于JDK动态代理的Mock创建</li>
 *   <li>Multiple interface support - 多接口支持</li>
 *   <li>Invocation recording and verification - 调用记录和验证</li>
 *   <li>Mock detection and reset - Mock检测和重置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a mock
 * UserService mock = MockProxy.create(UserService.class);
 *
 * // Get the handler to configure behavior
 * MockInvocationHandler handler = MockProxy.getHandler(mock);
 * handler.when("findById", 1L).thenReturn(new User("John"));
 *
 * // Use the mock
 * User user = mock.findById(1L);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless factory, thread-safe handler) - 线程安全: 是（无状态工厂，线程安全处理器）</li>
 *   <li>Null-safe: Yes (validates non-null inputs) - 空值安全: 是（验证非空输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class MockProxy {

    private MockProxy() {
    }

    /**
     * Creates a mock proxy for the given interface.
     * 为给定接口创建Mock代理。
     *
     * @param interfaceType the interface type | 接口类型
     * @param <T>           the interface type | 接口类型
     * @return the mock instance | Mock实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> interfaceType) {
        Objects.requireNonNull(interfaceType, "interfaceType cannot be null");
        if (!interfaceType.isInterface()) {
            throw MockException.notInterface(interfaceType);
        }

        MockInvocationHandler handler = new MockInvocationHandler(interfaceType);
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                handler
        );
    }

    /**
     * Creates a mock proxy for multiple interfaces.
     * 为多个接口创建Mock代理。
     *
     * @param interfaces the interfaces | 接口
     * @return the mock instance | Mock实例
     */
    public static Object create(Class<?>... interfaces) {
        Objects.requireNonNull(interfaces, "interfaces cannot be null");
        if (interfaces.length == 0) {
            throw new IllegalArgumentException("At least one interface is required");
        }
        for (Class<?> intf : interfaces) {
            if (!intf.isInterface()) {
                throw MockException.notInterface(intf);
            }
        }

        MockInvocationHandler handler = new MockInvocationHandler(interfaces[0]);
        return Proxy.newProxyInstance(
                interfaces[0].getClassLoader(),
                interfaces,
                handler
        );
    }

    /**
     * Gets the invocation handler for a mock.
     * 获取Mock的调用处理器。
     *
     * @param mock the mock instance | Mock实例
     * @return the handler | 处理器
     */
    public static MockInvocationHandler getHandler(Object mock) {
        Objects.requireNonNull(mock, "mock cannot be null");
        if (!Proxy.isProxyClass(mock.getClass())) {
            throw new IllegalArgumentException("Object is not a mock proxy");
        }
        java.lang.reflect.InvocationHandler handler = Proxy.getInvocationHandler(mock);
        if (!(handler instanceof MockInvocationHandler)) {
            throw new IllegalArgumentException("Object is not a MockProxy instance");
        }
        return (MockInvocationHandler) handler;
    }

    /**
     * Checks if object is a mock.
     * 检查对象是否是Mock。
     *
     * @param object the object | 对象
     * @return true if mock | 如果是Mock返回true
     */
    public static boolean isMock(Object object) {
        if (object == null) return false;
        if (!Proxy.isProxyClass(object.getClass())) return false;
        return Proxy.getInvocationHandler(object) instanceof MockInvocationHandler;
    }

    /**
     * Gets all invocations recorded on a mock.
     * 获取Mock上记录的所有调用。
     *
     * @param mock the mock instance | Mock实例
     * @return the invocations | 调用列表
     */
    public static List<Invocation> getInvocations(Object mock) {
        return getHandler(mock).getInvocations();
    }

    /**
     * Clears all invocations on a mock.
     * 清除Mock上的所有调用。
     *
     * @param mock the mock instance | Mock实例
     */
    public static void clearInvocations(Object mock) {
        getHandler(mock).clearInvocations();
    }

    /**
     * Resets a mock (clears invocations and stubbing).
     * 重置Mock（清除调用和存根）。
     *
     * @param mock the mock instance | Mock实例
     */
    public static void reset(Object mock) {
        getHandler(mock).reset();
    }

    /**
     * Verifies that a method was called on the mock.
     * 验证Mock上的方法被调用。
     *
     * @param mock the mock instance | Mock实例
     * @return the verification | 验证
     */
    public static MockVerification verify(Object mock) {
        return new MockVerification(getHandler(mock));
    }

    /**
     * Verifies that a method was called exactly n times.
     * 验证方法恰好被调用n次。
     *
     * @param mock  the mock instance | Mock实例
     * @param times the expected times | 期望次数
     * @return the verification | 验证
     */
    public static MockVerification verify(Object mock, int times) {
        return new MockVerification(getHandler(mock), times);
    }

    /**
     * Mock verification helper.
     * Mock验证助手。
     */
    public static class MockVerification {
        private final MockInvocationHandler handler;
        private final int expectedTimes;

        MockVerification(MockInvocationHandler handler) {
            this(handler, 1);
        }

        MockVerification(MockInvocationHandler handler, int expectedTimes) {
            this.handler = handler;
            this.expectedTimes = expectedTimes;
        }

        /**
         * Verifies method was called.
         * 验证方法被调用。
         *
         * @param methodName the method name | 方法名
         * @param args       the arguments | 参数
         */
        public void called(String methodName, Object... args) {
            int actualTimes = handler.countInvocations(methodName, args);
            if (actualTimes != expectedTimes) {
                throw MockException.verificationFailed(
                        methodName + " called " + expectedTimes + " time(s)",
                        actualTimes
                );
            }
        }

        /**
         * Verifies method was never called.
         * 验证方法从未被调用。
         *
         * @param methodName the method name | 方法名
         */
        public void neverCalled(String methodName) {
            int actualTimes = handler.countInvocations(methodName);
            if (actualTimes > 0) {
                throw MockException.verificationFailed(methodName + " never called", actualTimes);
            }
        }
    }
}
