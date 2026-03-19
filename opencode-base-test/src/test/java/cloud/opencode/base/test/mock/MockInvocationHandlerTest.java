package cloud.opencode.base.test.mock;

import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * MockInvocationHandlerTest Tests
 * MockInvocationHandlerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("MockInvocationHandler Tests")
class MockInvocationHandlerTest {

    interface TestService {
        String getName();
        int getValue();
        void doSomething();
        String greet(String name);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create handler with mocked type")
        void shouldCreateHandlerWithMockedType() {
            MockInvocationHandler handler = new MockInvocationHandler(TestService.class);
            assertThat(handler.getMockedType()).isEqualTo(TestService.class);
        }

        @Test
        @DisplayName("Should throw for null mocked type")
        void shouldThrowForNullMockedType() {
            assertThatNullPointerException()
                .isThrownBy(() -> new MockInvocationHandler(null));
        }
    }

    @Nested
    @DisplayName("Invoke Tests")
    class InvokeTests {

        @Test
        @DisplayName("Should record invocation")
        void shouldRecordInvocation() throws Throwable {
            MockInvocationHandler handler = new MockInvocationHandler(TestService.class);
            Method method = TestService.class.getMethod("getName");
            Object proxy = createProxy(handler);

            handler.invoke(proxy, method, null);

            assertThat(handler.getInvocations()).hasSize(1);
        }

        @Test
        @DisplayName("Should return default value for non-stubbed method")
        void shouldReturnDefaultValueForNonStubbedMethod() throws Throwable {
            MockInvocationHandler handler = new MockInvocationHandler(TestService.class);
            Object proxy = createProxy(handler);

            Object result = handler.invoke(proxy, TestService.class.getMethod("getName"), null);
            assertThat(result).isNull();

            result = handler.invoke(proxy, TestService.class.getMethod("getValue"), null);
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle toString method")
        void shouldHandleToStringMethod() throws Throwable {
            MockInvocationHandler handler = new MockInvocationHandler(TestService.class);
            Object proxy = createProxy(handler);

            Object result = handler.invoke(proxy, Object.class.getMethod("toString"), null);
            assertThat(result).isEqualTo("Mock[TestService]");
        }

        @Test
        @DisplayName("Should handle hashCode method")
        void shouldHandleHashCodeMethod() throws Throwable {
            MockInvocationHandler handler = new MockInvocationHandler(TestService.class);
            Object proxy = createProxy(handler);

            Object result = handler.invoke(proxy, Object.class.getMethod("hashCode"), null);
            assertThat(result).isEqualTo(System.identityHashCode(proxy));
        }

        @Test
        @DisplayName("Should handle equals method")
        void shouldHandleEqualsMethod() throws Throwable {
            MockInvocationHandler handler = new MockInvocationHandler(TestService.class);
            Object proxy = createProxy(handler);

            Object result = handler.invoke(proxy, Object.class.getMethod("equals", Object.class), new Object[]{proxy});
            assertThat(result).isEqualTo(true);

            result = handler.invoke(proxy, Object.class.getMethod("equals", Object.class), new Object[]{"other"});
            assertThat(result).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("Stubbing Tests")
    class StubbingTests {

        @Test
        @DisplayName("thenReturn should return stubbed value")
        void thenReturnShouldReturnStubbedValue() throws Throwable {
            MockInvocationHandler handler = new MockInvocationHandler(TestService.class);
            handler.when("getName").thenReturn("stubbed");
            Object proxy = createProxy(handler);

            Object result = handler.invoke(proxy, TestService.class.getMethod("getName"), null);
            assertThat(result).isEqualTo("stubbed");
        }

        @Test
        @DisplayName("thenReturn with specific args should match args")
        void thenReturnWithSpecificArgsShouldMatchArgs() throws Throwable {
            MockInvocationHandler handler = new MockInvocationHandler(TestService.class);
            handler.when("greet", "John").thenReturn("Hello John");
            Object proxy = createProxy(handler);

            Object result = handler.invoke(proxy, TestService.class.getMethod("greet", String.class), new Object[]{"John"});
            assertThat(result).isEqualTo("Hello John");
        }

        @Test
        @DisplayName("thenThrow should throw exception")
        void thenThrowShouldThrowException() {
            MockInvocationHandler handler = new MockInvocationHandler(TestService.class);
            handler.when("getName").thenThrow(new RuntimeException("error"));
            Object proxy = createProxy(handler);

            assertThatThrownBy(() -> handler.invoke(proxy, TestService.class.getMethod("getName"), null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("error");
        }

        @Test
        @DisplayName("thenAnswer should use function")
        void thenAnswerShouldUseFunction() throws Throwable {
            MockInvocationHandler handler = new MockInvocationHandler(TestService.class);
            handler.when("greet").thenAnswer(args -> "Hello " + args[0]);
            Object proxy = createProxy(handler);

            Object result = handler.invoke(proxy, TestService.class.getMethod("greet", String.class), new Object[]{"World"});
            assertThat(result).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("thenCallRealMethod should throw UnsupportedOperationException")
        void thenCallRealMethodShouldThrowUnsupportedOperationException() {
            MockInvocationHandler handler = new MockInvocationHandler(TestService.class);

            assertThatThrownBy(() -> handler.when("getName").thenCallRealMethod())
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Invocation Query Tests")
    class InvocationQueryTests {

        @Test
        @DisplayName("getInvocations should return all invocations")
        void getInvocationsShouldReturnAllInvocations() throws Throwable {
            MockInvocationHandler handler = new MockInvocationHandler(TestService.class);
            Object proxy = createProxy(handler);

            handler.invoke(proxy, TestService.class.getMethod("getName"), null);
            handler.invoke(proxy, TestService.class.getMethod("getValue"), null);

            assertThat(handler.getInvocations()).hasSize(2);
        }

        @Test
        @DisplayName("getInvocations by method name should filter invocations")
        void getInvocationsByMethodNameShouldFilterInvocations() throws Throwable {
            MockInvocationHandler handler = new MockInvocationHandler(TestService.class);
            Object proxy = createProxy(handler);

            handler.invoke(proxy, TestService.class.getMethod("getName"), null);
            handler.invoke(proxy, TestService.class.getMethod("getName"), null);
            handler.invoke(proxy, TestService.class.getMethod("getValue"), null);

            List<Invocation> invocations = handler.getInvocations("getName");
            assertThat(invocations).hasSize(2);
        }

        @Test
        @DisplayName("countInvocations should return count")
        void countInvocationsShouldReturnCount() throws Throwable {
            MockInvocationHandler handler = new MockInvocationHandler(TestService.class);
            Object proxy = createProxy(handler);

            handler.invoke(proxy, TestService.class.getMethod("getName"), null);
            handler.invoke(proxy, TestService.class.getMethod("getName"), null);

            assertThat(handler.countInvocations("getName")).isEqualTo(2);
        }

        @Test
        @DisplayName("countInvocations with args should match args")
        void countInvocationsWithArgsShouldMatchArgs() throws Throwable {
            MockInvocationHandler handler = new MockInvocationHandler(TestService.class);
            Object proxy = createProxy(handler);

            handler.invoke(proxy, TestService.class.getMethod("greet", String.class), new Object[]{"John"});
            handler.invoke(proxy, TestService.class.getMethod("greet", String.class), new Object[]{"Jane"});
            handler.invoke(proxy, TestService.class.getMethod("greet", String.class), new Object[]{"John"});

            assertThat(handler.countInvocations("greet", "John")).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Clear and Reset Tests")
    class ClearAndResetTests {

        @Test
        @DisplayName("clearInvocations should clear all invocations")
        void clearInvocationsShouldClearAllInvocations() throws Throwable {
            MockInvocationHandler handler = new MockInvocationHandler(TestService.class);
            Object proxy = createProxy(handler);

            handler.invoke(proxy, TestService.class.getMethod("getName"), null);
            handler.clearInvocations();

            assertThat(handler.getInvocations()).isEmpty();
        }

        @Test
        @DisplayName("reset should clear invocations and stubs")
        void resetShouldClearInvocationsAndStubs() throws Throwable {
            MockInvocationHandler handler = new MockInvocationHandler(TestService.class);
            handler.when("getName").thenReturn("stubbed");
            Object proxy = createProxy(handler);

            handler.invoke(proxy, TestService.class.getMethod("getName"), null);
            handler.reset();

            assertThat(handler.getInvocations()).isEmpty();
            // After reset, should return default value
            Object result = handler.invoke(proxy, TestService.class.getMethod("getName"), null);
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Default Value Tests")
    class DefaultValueTests {

        interface PrimitiveService {
            boolean getBool();
            byte getByte();
            short getShort();
            int getInt();
            long getLong();
            float getFloat();
            double getDouble();
            char getChar();
            void doVoid();
            Void getVoid();
        }

        @Test
        @DisplayName("Should return default values for primitive types")
        void shouldReturnDefaultValuesForPrimitiveTypes() throws Throwable {
            MockInvocationHandler handler = new MockInvocationHandler(PrimitiveService.class);
            Object proxy = createProxy(handler, PrimitiveService.class);

            assertThat(handler.invoke(proxy, PrimitiveService.class.getMethod("getBool"), null)).isEqualTo(false);
            assertThat(handler.invoke(proxy, PrimitiveService.class.getMethod("getByte"), null)).isEqualTo((byte) 0);
            assertThat(handler.invoke(proxy, PrimitiveService.class.getMethod("getShort"), null)).isEqualTo((short) 0);
            assertThat(handler.invoke(proxy, PrimitiveService.class.getMethod("getInt"), null)).isEqualTo(0);
            assertThat(handler.invoke(proxy, PrimitiveService.class.getMethod("getLong"), null)).isEqualTo(0L);
            assertThat(handler.invoke(proxy, PrimitiveService.class.getMethod("getFloat"), null)).isEqualTo(0.0f);
            assertThat(handler.invoke(proxy, PrimitiveService.class.getMethod("getDouble"), null)).isEqualTo(0.0d);
            assertThat(handler.invoke(proxy, PrimitiveService.class.getMethod("getChar"), null)).isEqualTo('\0');
            assertThat(handler.invoke(proxy, PrimitiveService.class.getMethod("doVoid"), null)).isNull();
            assertThat(handler.invoke(proxy, PrimitiveService.class.getMethod("getVoid"), null)).isNull();
        }
    }

    private Object createProxy(MockInvocationHandler handler) {
        return createProxy(handler, TestService.class);
    }

    private Object createProxy(MockInvocationHandler handler, Class<?> interfaceType) {
        return Proxy.newProxyInstance(
            interfaceType.getClassLoader(),
            new Class<?>[]{interfaceType},
            handler
        );
    }
}
