package cloud.opencode.base.reflect.proxy;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import org.junit.jupiter.api.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.*;

/**
 * ProxyFactoryTest Tests
 * ProxyFactoryTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("ProxyFactory 测试")
class ProxyFactoryTest {

    @Nested
    @DisplayName("forInterface静态方法测试")
    class ForInterfaceTests {

        @Test
        @DisplayName("为接口创建工厂")
        void testForInterface() {
            ProxyFactory<TestInterface> factory = ProxyFactory.forInterface(TestInterface.class);
            assertThat(factory).isNotNull();
        }

        @Test
        @DisplayName("非接口抛出异常")
        void testForInterfaceNotInterface() {
            assertThatThrownBy(() -> ProxyFactory.forInterface(String.class))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Must be an interface");
        }

        @Test
        @DisplayName("null参数抛出异常")
        void testForInterfaceNull() {
            assertThatThrownBy(() -> ProxyFactory.forInterface(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("implement方法测试")
    class ImplementTests {

        @Test
        @DisplayName("添加额外接口")
        void testImplement() {
            ProxyFactory<TestInterface> factory = ProxyFactory.forInterface(TestInterface.class)
                    .implement(AnotherInterface.class);
            TestInterface proxy = factory.create();
            assertThat(proxy).isInstanceOf(TestInterface.class);
            assertThat(proxy).isInstanceOf(AnotherInterface.class);
        }

        @Test
        @DisplayName("非接口抛出异常")
        void testImplementNotInterface() {
            ProxyFactory<TestInterface> factory = ProxyFactory.forInterface(TestInterface.class);
            assertThatThrownBy(() -> factory.implement(String.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("classLoader方法测试")
    class ClassLoaderTests {

        @Test
        @DisplayName("设置ClassLoader")
        void testClassLoader() {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            ProxyFactory<TestInterface> factory = ProxyFactory.forInterface(TestInterface.class)
                    .classLoader(loader);
            TestInterface proxy = factory.create();
            assertThat(proxy).isNotNull();
        }
    }

    @Nested
    @DisplayName("target方法测试")
    class TargetTests {

        @Test
        @DisplayName("设置委托目标")
        void testTarget() {
            TestImpl impl = new TestImpl("hello");
            ProxyFactory<TestInterface> factory = ProxyFactory.forInterface(TestInterface.class)
                    .target(impl);
            TestInterface proxy = factory.create();
            assertThat(proxy.getValue()).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("intercept方法测试")
    class InterceptTests {

        @Test
        @DisplayName("设置默认拦截器")
        void testInterceptDefault() {
            ProxyFactory<TestInterface> factory = ProxyFactory.forInterface(TestInterface.class)
                    .intercept((proxy, method, args, invoker) -> "intercepted");
            TestInterface proxy = factory.create();
            assertThat(proxy.getValue()).isEqualTo("intercepted");
        }

        @Test
        @DisplayName("按方法名设置拦截器")
        void testInterceptByName() {
            ProxyFactory<TestInterface> factory = ProxyFactory.forInterface(TestInterface.class)
                    .intercept("getValue", (proxy, method, args, invoker) -> "name-intercepted");
            TestInterface proxy = factory.create();
            assertThat(proxy.getValue()).isEqualTo("name-intercepted");
        }

        @Test
        @DisplayName("按方法名和参数类型设置拦截器")
        void testInterceptByNameAndParams() {
            ProxyFactory<TestInterface> factory = ProxyFactory.forInterface(TestInterface.class)
                    .intercept("setValue", new Class<?>[]{String.class},
                            (proxy, method, args, invoker) -> "set-intercepted");
            TestInterface proxy = factory.create();
            assertThat(proxy.setValue("test")).isEqualTo("set-intercepted");
        }

        @Test
        @DisplayName("方法不存在抛出异常")
        void testInterceptMethodNotFound() {
            ProxyFactory<TestInterface> factory = ProxyFactory.forInterface(TestInterface.class);
            assertThatThrownBy(() -> factory.intercept("nonexistent", new Class<?>[]{},
                    (proxy, method, args, invoker) -> null))
                    .isInstanceOf(OpenReflectException.class);
        }
    }

    @Nested
    @DisplayName("handler方法测试")
    class HandlerTests {

        @Test
        @DisplayName("设置自定义处理器")
        void testHandler() {
            InvocationHandler handler = (proxy, method, args) -> "custom";
            ProxyFactory<TestInterface> factory = ProxyFactory.forInterface(TestInterface.class)
                    .handler(handler);
            TestInterface proxy = factory.create();
            assertThat(proxy.getValue()).isEqualTo("custom");
        }
    }

    @Nested
    @DisplayName("create方法测试")
    class CreateTests {

        @Test
        @DisplayName("创建代理")
        void testCreate() {
            TestInterface proxy = ProxyFactory.forInterface(TestInterface.class)
                    .intercept((p, m, a, i) -> null)
                    .create();
            assertThat(proxy).isNotNull();
            assertThat(Proxy.isProxyClass(proxy.getClass())).isTrue();
        }

        @Test
        @DisplayName("无拦截器和目标时使用noOp")
        void testCreateNoInterceptorNoTarget() {
            TestInterface proxy = ProxyFactory.forInterface(TestInterface.class).create();
            assertThat(proxy.getValue()).isNull();
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("使用Builder创建")
        void testBuilderOf() {
            TestInterface proxy = ProxyFactory.Builder.of(TestInterface.class)
                    .handle((method, args) -> "builder-result")
                    .build();
            assertThat(proxy.getValue()).isEqualTo("builder-result");
        }

        @Test
        @DisplayName("Builder handle方法")
        void testBuilderHandle() {
            TestInterface proxy = ProxyFactory.Builder.of(TestInterface.class)
                    .handle((method, args) -> method.getName())
                    .build();
            assertThat(proxy.getValue()).isEqualTo("getValue");
        }
    }

    // Test interfaces and classes
    interface TestInterface {
        String getValue();
        String setValue(String value);
    }

    interface AnotherInterface {
        void doSomething();
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
        public String setValue(String value) {
            this.value = value;
            return value;
        }
    }
}
