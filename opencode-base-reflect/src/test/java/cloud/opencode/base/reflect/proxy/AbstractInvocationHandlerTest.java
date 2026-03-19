package cloud.opencode.base.reflect.proxy;

import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.*;

/**
 * AbstractInvocationHandlerTest Tests
 * AbstractInvocationHandlerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("AbstractInvocationHandler 测试")
class AbstractInvocationHandlerTest {

    @Nested
    @DisplayName("invoke方法测试")
    class InvokeTests {

        @Test
        @DisplayName("调用普通方法")
        void testInvokeNormalMethod() throws Throwable {
            TestHandler handler = new TestHandler("result");
            TestInterface proxy = createProxy(handler);
            assertThat(proxy.getValue()).isEqualTo("result");
        }

        @Test
        @DisplayName("记录调用参数")
        void testInvokeRecordsArgs() throws Throwable {
            TestHandler handler = new TestHandler(null);
            TestInterface proxy = createProxy(handler);
            proxy.setValue("test");
            assertThat(handler.lastArgs).containsExactly("test");
        }
    }

    @Nested
    @DisplayName("handleObjectMethod方法测试")
    class HandleObjectMethodTests {

        @Test
        @DisplayName("处理toString")
        void testToString() {
            TestHandler handler = new TestHandler(null);
            TestInterface proxy = createProxy(handler);
            String str = proxy.toString();
            assertThat(str).contains("@");
        }

        @Test
        @DisplayName("处理hashCode")
        void testHashCode() {
            TestHandler handler = new TestHandler(null);
            TestInterface proxy = createProxy(handler);
            int hash = proxy.hashCode();
            assertThat(hash).isEqualTo(handler.hashCode());
        }

        @Test
        @DisplayName("处理equals - 相同代理")
        void testEqualsSameProxy() {
            TestHandler handler = new TestHandler(null);
            TestInterface proxy = createProxy(handler);
            assertThat(proxy.equals(proxy)).isTrue();
        }

        @Test
        @DisplayName("处理equals - 不同对象")
        void testEqualsNonProxy() {
            TestHandler handler = new TestHandler(null);
            TestInterface proxy = createProxy(handler);
            assertThat(proxy.equals("not a proxy")).isFalse();
        }

        @Test
        @DisplayName("处理equals - null")
        void testEqualsNull() {
            TestHandler handler = new TestHandler(null);
            TestInterface proxy = createProxy(handler);
            assertThat(proxy.equals(null)).isFalse();
        }

        @Test
        @DisplayName("处理equals - 相同handler的代理")
        void testEqualsSameHandler() {
            TestHandler handler = new TestHandler(null);
            TestInterface proxy1 = createProxy(handler);
            TestInterface proxy2 = createProxy(handler);
            assertThat(proxy1.equals(proxy2)).isTrue();
        }
    }

    @Nested
    @DisplayName("handleDefaultMethod方法测试")
    class HandleDefaultMethodTests {

        @Test
        @DisplayName("调用接口默认方法")
        void testDefaultMethod() {
            TestHandler handler = new TestHandler(null);
            InterfaceWithDefault proxy = createProxyWithDefault(handler);
            assertThat(proxy.getDefault()).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("proxyEquals方法测试")
    class ProxyEqualsTests {

        @Test
        @DisplayName("代理与自身相等")
        void testProxyEqualsSelf() {
            TestHandler handler = new TestHandler(null);
            TestInterface proxy = createProxy(handler);
            assertThat(proxy).isEqualTo(proxy);
        }
    }

    @Nested
    @DisplayName("proxyHashCode方法测试")
    class ProxyHashCodeTests {

        @Test
        @DisplayName("代理hashCode基于handler")
        void testProxyHashCode() {
            TestHandler handler = new TestHandler(null);
            TestInterface proxy = createProxy(handler);
            assertThat(proxy.hashCode()).isEqualTo(handler.hashCode());
        }
    }

    @Nested
    @DisplayName("proxyToString方法测试")
    class ProxyToStringTests {

        @Test
        @DisplayName("代理toString包含类名和哈希")
        void testProxyToString() {
            TestHandler handler = new TestHandler(null);
            TestInterface proxy = createProxy(handler);
            String str = proxy.toString();
            assertThat(str).contains("@");
            assertThat(str).contains(Integer.toHexString(handler.hashCode()));
        }
    }

    @Nested
    @DisplayName("equals和hashCode测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("AbstractInvocationHandler equals")
        void testHandlerEquals() {
            TestHandler h1 = new TestHandler(null);
            TestHandler h2 = new TestHandler(null);
            assertThat(h1).isEqualTo(h2);
        }

        @Test
        @DisplayName("AbstractInvocationHandler hashCode")
        void testHandlerHashCode() {
            TestHandler h1 = new TestHandler(null);
            TestHandler h2 = new TestHandler(null);
            assertThat(h1.hashCode()).isEqualTo(h2.hashCode());
        }
    }

    // Helper methods
    private TestInterface createProxy(TestHandler handler) {
        return (TestInterface) Proxy.newProxyInstance(
                TestInterface.class.getClassLoader(),
                new Class<?>[]{TestInterface.class},
                handler
        );
    }

    private InterfaceWithDefault createProxyWithDefault(TestHandler handler) {
        return (InterfaceWithDefault) Proxy.newProxyInstance(
                InterfaceWithDefault.class.getClassLoader(),
                new Class<?>[]{InterfaceWithDefault.class},
                handler
        );
    }

    // Test interfaces
    interface TestInterface {
        String getValue();
        void setValue(String value);
    }

    interface InterfaceWithDefault {
        default String getDefault() {
            return "default";
        }
    }

    // Test handler
    static class TestHandler extends AbstractInvocationHandler {
        private final Object result;
        Object[] lastArgs;
        Method lastMethod;

        TestHandler(Object result) {
            this.result = result;
        }

        @Override
        protected Object handleInvocation(Object proxy, Method method, Object[] args) {
            this.lastMethod = method;
            this.lastArgs = args;
            return result;
        }
    }
}
