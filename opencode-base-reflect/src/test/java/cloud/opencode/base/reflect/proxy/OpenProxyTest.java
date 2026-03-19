package cloud.opencode.base.reflect.proxy;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import org.junit.jupiter.api.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenProxyTest Tests
 * OpenProxyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("OpenProxy 测试")
class OpenProxyTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = OpenProxy.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("create方法测试")
    class CreateTests {

        @Test
        @DisplayName("使用MethodInterceptor创建")
        void testCreateWithInterceptor() {
            TestInterface proxy = OpenProxy.create(TestInterface.class,
                    (p, m, a, i) -> "intercepted");
            assertThat(proxy.getValue()).isEqualTo("intercepted");
        }

        @Test
        @DisplayName("使用InvocationHandler创建")
        void testCreateWithHandler() {
            TestInterface proxy = OpenProxy.create(TestInterface.class,
                    (p, m, a) -> "handled");
            assertThat(proxy.getValue()).isEqualTo("handled");
        }

        @Test
        @DisplayName("为多个接口创建代理")
        void testCreateMultipleInterfaces() {
            InvocationHandler handler = (p, m, a) -> null;
            Object proxy = OpenProxy.create(handler, TestInterface.class, AnotherInterface.class);
            assertThat(proxy).isInstanceOf(TestInterface.class);
            assertThat(proxy).isInstanceOf(AnotherInterface.class);
        }
    }

    @Nested
    @DisplayName("wrap方法测试")
    class WrapTests {

        @Test
        @DisplayName("包装目标对象")
        void testWrap() {
            TestImpl impl = new TestImpl("hello");
            TestInterface proxy = OpenProxy.wrap(TestInterface.class, impl);
            assertThat(proxy.getValue()).isEqualTo("hello");
        }

        @Test
        @DisplayName("带拦截器包装")
        void testWrapWithInterceptor() {
            TestImpl impl = new TestImpl("hello");
            TestInterface proxy = OpenProxy.wrap(TestInterface.class, impl,
                    (p, m, a, i) -> i.invoke(a) + "-wrapped");
            assertThat(proxy.getValue()).isEqualTo("hello-wrapped");
        }
    }

    @Nested
    @DisplayName("factory方法测试")
    class FactoryTests {

        @Test
        @DisplayName("获取工厂")
        void testFactory() {
            ProxyFactory<TestInterface> factory = OpenProxy.factory(TestInterface.class);
            assertThat(factory).isNotNull();
        }
    }

    @Nested
    @DisplayName("isProxy方法测试")
    class IsProxyTests {

        @Test
        @DisplayName("代理返回true")
        void testIsProxyTrue() {
            TestInterface proxy = OpenProxy.create(TestInterface.class, (p, m, a, i) -> null);
            assertThat(OpenProxy.isProxy(proxy)).isTrue();
        }

        @Test
        @DisplayName("非代理返回false")
        void testIsProxyFalse() {
            assertThat(OpenProxy.isProxy(new TestImpl("test"))).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void testIsProxyNull() {
            assertThat(OpenProxy.isProxy(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("getHandler方法测试")
    class GetHandlerTests {

        @Test
        @DisplayName("获取代理处理器")
        void testGetHandler() {
            TestInterface proxy = OpenProxy.create(TestInterface.class, (p, m, a, i) -> null);
            InvocationHandler handler = OpenProxy.getHandler(proxy);
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("非代理抛出异常")
        void testGetHandlerNotProxy() {
            assertThatThrownBy(() -> OpenProxy.getHandler(new TestImpl("test")))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("getInterfaces方法测试")
    class GetInterfacesTests {

        @Test
        @DisplayName("获取代理接口")
        void testGetInterfaces() {
            TestInterface proxy = OpenProxy.create(TestInterface.class, (p, m, a, i) -> null);
            Class<?>[] interfaces = OpenProxy.getInterfaces(proxy);
            assertThat(interfaces).contains(TestInterface.class);
        }

        @Test
        @DisplayName("非代理抛出异常")
        void testGetInterfacesNotProxy() {
            assertThatThrownBy(() -> OpenProxy.getInterfaces(new TestImpl("test")))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("implementsInterface方法测试")
    class ImplementsInterfaceTests {

        @Test
        @DisplayName("实现接口返回true")
        void testImplementsInterfaceTrue() {
            TestInterface proxy = OpenProxy.create(TestInterface.class, (p, m, a, i) -> null);
            assertThat(OpenProxy.implementsInterface(proxy, TestInterface.class)).isTrue();
        }

        @Test
        @DisplayName("未实现接口返回false")
        void testImplementsInterfaceFalse() {
            TestInterface proxy = OpenProxy.create(TestInterface.class, (p, m, a, i) -> null);
            assertThat(OpenProxy.implementsInterface(proxy, AnotherInterface.class)).isFalse();
        }

        @Test
        @DisplayName("非代理返回false")
        void testImplementsInterfaceNotProxy() {
            assertThat(OpenProxy.implementsInterface(new TestImpl("test"), TestInterface.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("unwrap方法测试")
    class UnwrapTests {

        @Test
        @DisplayName("解包代理获取目标")
        void testUnwrap() {
            TestImpl impl = new TestImpl("hello");
            TestInterface proxy = OpenProxy.wrap(TestInterface.class, impl);
            TestImpl unwrapped = OpenProxy.unwrap(proxy);
            assertThat(unwrapped).isSameAs(impl);
        }

        @Test
        @DisplayName("非代理返回原对象")
        void testUnwrapNotProxy() {
            TestImpl impl = new TestImpl("hello");
            TestImpl result = OpenProxy.unwrap(impl);
            assertThat(result).isSameAs(impl);
        }
    }

    @Nested
    @DisplayName("createNoOp方法测试")
    class CreateNoOpTests {

        @Test
        @DisplayName("创建空操作代理")
        void testCreateNoOp() {
            TestInterface proxy = OpenProxy.createNoOp(TestInterface.class);
            assertThat(proxy.getValue()).isNull();
        }

        @Test
        @DisplayName("空操作代理返回基本类型默认值")
        void testCreateNoOpPrimitive() {
            PrimitiveInterface proxy = OpenProxy.createNoOp(PrimitiveInterface.class);
            assertThat(proxy.getInt()).isEqualTo(0);
            assertThat(proxy.getBoolean()).isFalse();
            assertThat(proxy.getDouble()).isEqualTo(0d);
        }
    }

    @Nested
    @DisplayName("createRecording方法测试")
    class CreateRecordingTests {

        @Test
        @DisplayName("创建记录代理")
        void testCreateRecording() {
            OpenProxy.RecordingProxy<TestInterface> recording = OpenProxy.createRecording(TestInterface.class);
            recording.proxy().getValue();
            assertThat(recording.getCallCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("wasCalled检查")
        void testWasCalled() {
            OpenProxy.RecordingProxy<TestInterface> recording = OpenProxy.createRecording(TestInterface.class);
            recording.proxy().getValue();
            assertThat(recording.wasCalled("getValue")).isTrue();
            assertThat(recording.wasCalled("setValue")).isFalse();
        }

        @Test
        @DisplayName("getCallsFor获取调用")
        void testGetCallsFor() {
            OpenProxy.RecordingProxy<TestInterface> recording = OpenProxy.createRecording(TestInterface.class);
            recording.proxy().getValue();
            recording.proxy().getValue();
            assertThat(recording.getCallsFor("getValue")).hasSize(2);
        }

        @Test
        @DisplayName("clearCalls清除调用")
        void testClearCalls() {
            OpenProxy.RecordingProxy<TestInterface> recording = OpenProxy.createRecording(TestInterface.class);
            recording.proxy().getValue();
            recording.clearCalls();
            assertThat(recording.getCallCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("MethodCall记录测试")
    class MethodCallTests {

        @Test
        @DisplayName("获取方法名")
        void testGetMethodName() {
            OpenProxy.RecordingProxy<TestInterface> recording = OpenProxy.createRecording(TestInterface.class);
            recording.proxy().getValue();
            assertThat(recording.calls().get(0).getMethodName()).isEqualTo("getValue");
        }

        @Test
        @DisplayName("获取参数数量")
        void testGetArgCount() {
            OpenProxy.RecordingProxy<TestInterface> recording = OpenProxy.createRecording(TestInterface.class);
            recording.proxy().setValue("test");
            assertThat(recording.calls().get(0).getArgCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("获取参数值")
        void testGetArg() {
            OpenProxy.RecordingProxy<TestInterface> recording = OpenProxy.createRecording(TestInterface.class);
            recording.proxy().setValue("test");
            assertThat(recording.calls().get(0).<String>getArg(0)).isEqualTo("test");
        }
    }

    // Test interfaces and classes
    interface TestInterface {
        String getValue();
        void setValue(String value);
    }

    interface AnotherInterface {
        void doSomething();
    }

    interface PrimitiveInterface {
        int getInt();
        boolean getBoolean();
        double getDouble();
    }

    static class TestImpl implements TestInterface {
        private String value;

        TestImpl(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public void setValue(String value) {
            this.value = value;
        }
    }
}
