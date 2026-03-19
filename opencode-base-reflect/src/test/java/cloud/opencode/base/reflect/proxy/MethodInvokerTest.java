package cloud.opencode.base.reflect.proxy;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * MethodInvokerTest Tests
 * MethodInvokerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("MethodInvoker 测试")
class MethodInvokerTest {

    @Nested
    @DisplayName("invoke方法测试")
    class InvokeTests {

        @Test
        @DisplayName("函数式接口调用")
        void testInvoke() throws Throwable {
            MethodInvoker invoker = args -> "result";
            Object result = invoker.invoke(new Object[]{"arg1"});
            assertThat(result).isEqualTo("result");
        }

        @Test
        @DisplayName("带参数调用")
        void testInvokeWithArgs() throws Throwable {
            MethodInvoker invoker = args -> args[0] + "-" + args[1];
            Object result = invoker.invoke(new Object[]{"a", "b"});
            assertThat(result).isEqualTo("a-b");
        }

        @Test
        @DisplayName("null参数调用")
        void testInvokeWithNullArgs() throws Throwable {
            MethodInvoker invoker = args -> args;
            Object result = invoker.invoke(null);
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("noOp静态方法测试")
    class NoOpTests {

        @Test
        @DisplayName("创建空操作调用器")
        void testNoOp() throws Throwable {
            MethodInvoker invoker = MethodInvoker.noOp();
            assertThat(invoker).isNotNull();
            assertThat(invoker.invoke(new Object[]{"any"})).isNull();
        }

        @Test
        @DisplayName("空操作调用器始终返回null")
        void testNoOpAlwaysNull() throws Throwable {
            MethodInvoker invoker = MethodInvoker.noOp();
            assertThat(invoker.invoke(null)).isNull();
            assertThat(invoker.invoke(new Object[]{})).isNull();
            assertThat(invoker.invoke(new Object[]{"a", "b", "c"})).isNull();
        }
    }

    @Nested
    @DisplayName("constant静态方法测试")
    class ConstantTests {

        @Test
        @DisplayName("创建常量调用器")
        void testConstant() throws Throwable {
            MethodInvoker invoker = MethodInvoker.constant("constant");
            assertThat(invoker).isNotNull();
            assertThat(invoker.invoke(new Object[]{})).isEqualTo("constant");
        }

        @Test
        @DisplayName("常量调用器始终返回相同值")
        void testConstantAlwaysSame() throws Throwable {
            MethodInvoker invoker = MethodInvoker.constant(42);
            assertThat(invoker.invoke(null)).isEqualTo(42);
            assertThat(invoker.invoke(new Object[]{})).isEqualTo(42);
            assertThat(invoker.invoke(new Object[]{"ignored"})).isEqualTo(42);
        }

        @Test
        @DisplayName("常量调用器可返回null")
        void testConstantNull() throws Throwable {
            MethodInvoker invoker = MethodInvoker.constant(null);
            assertThat(invoker.invoke(new Object[]{})).isNull();
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionTests {

        @Test
        @DisplayName("调用器抛出异常")
        void testInvokerThrows() {
            MethodInvoker invoker = args -> {
                throw new RuntimeException("test error");
            };
            assertThatThrownBy(() -> invoker.invoke(new Object[]{}))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("test error");
        }

        @Test
        @DisplayName("调用器抛出受检异常")
        void testInvokerThrowsChecked() {
            MethodInvoker invoker = args -> {
                throw new Exception("checked error");
            };
            assertThatThrownBy(() -> invoker.invoke(new Object[]{}))
                    .isInstanceOf(Exception.class)
                    .hasMessage("checked error");
        }
    }
}
