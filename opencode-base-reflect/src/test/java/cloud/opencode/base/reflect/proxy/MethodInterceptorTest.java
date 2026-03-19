package cloud.opencode.base.reflect.proxy;

import org.junit.jupiter.api.*;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/**
 * MethodInterceptorTest Tests
 * MethodInterceptorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("MethodInterceptor 测试")
class MethodInterceptorTest {

    @Nested
    @DisplayName("intercept方法测试")
    class InterceptTests {

        @Test
        @DisplayName("函数式接口拦截")
        void testIntercept() throws Throwable {
            MethodInterceptor interceptor = (proxy, method, args, invoker) -> "intercepted";
            Method method = Object.class.getMethod("toString");
            Object result = interceptor.intercept(new Object(), method, new Object[]{}, MethodInvoker.noOp());
            assertThat(result).isEqualTo("intercepted");
        }

        @Test
        @DisplayName("调用原始方法")
        void testInterceptCallOriginal() throws Throwable {
            MethodInterceptor interceptor = (proxy, method, args, invoker) -> invoker.invoke(args);
            Method method = Object.class.getMethod("toString");
            Object result = interceptor.intercept(new Object(), method, new Object[]{},
                    MethodInvoker.constant("original"));
            assertThat(result).isEqualTo("original");
        }
    }

    @Nested
    @DisplayName("passThrough静态方法测试")
    class PassThroughTests {

        @Test
        @DisplayName("创建透传拦截器")
        void testPassThrough() throws Throwable {
            MethodInterceptor interceptor = MethodInterceptor.passThrough();
            assertThat(interceptor).isNotNull();

            Method method = Object.class.getMethod("toString");
            Object result = interceptor.intercept(new Object(), method, new Object[]{},
                    MethodInvoker.constant("passed"));
            assertThat(result).isEqualTo("passed");
        }
    }

    @Nested
    @DisplayName("constant静态方法测试")
    class ConstantTests {

        @Test
        @DisplayName("创建常量拦截器")
        void testConstant() throws Throwable {
            MethodInterceptor interceptor = MethodInterceptor.constant("always");
            assertThat(interceptor).isNotNull();

            Method method = Object.class.getMethod("toString");
            Object result = interceptor.intercept(new Object(), method, new Object[]{},
                    MethodInvoker.constant("ignored"));
            assertThat(result).isEqualTo("always");
        }

        @Test
        @DisplayName("常量拦截器可返回null")
        void testConstantNull() throws Throwable {
            MethodInterceptor interceptor = MethodInterceptor.constant(null);
            Method method = Object.class.getMethod("toString");
            Object result = interceptor.intercept(new Object(), method, new Object[]{},
                    MethodInvoker.noOp());
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("throwing静态方法测试")
    class ThrowingTests {

        @Test
        @DisplayName("创建抛异常拦截器")
        void testThrowing() throws NoSuchMethodException {
            RuntimeException exception = new RuntimeException("test");
            MethodInterceptor interceptor = MethodInterceptor.throwing(exception);
            assertThat(interceptor).isNotNull();

            Method method = Object.class.getMethod("toString");
            assertThatThrownBy(() -> interceptor.intercept(new Object(), method, new Object[]{},
                    MethodInvoker.noOp()))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("抛受检异常")
        void testThrowingChecked() throws NoSuchMethodException {
            Exception exception = new Exception("checked");
            MethodInterceptor interceptor = MethodInterceptor.throwing(exception);
            Method method = Object.class.getMethod("toString");
            assertThatThrownBy(() -> interceptor.intercept(new Object(), method, new Object[]{},
                    MethodInvoker.noOp()))
                    .isSameAs(exception);
        }
    }

    @Nested
    @DisplayName("andThen方法测试")
    class AndThenTests {

        @Test
        @DisplayName("链接两个拦截器")
        void testAndThen() throws Throwable {
            StringBuilder log = new StringBuilder();
            MethodInterceptor first = (proxy, method, args, invoker) -> {
                log.append("first");
                return "first-result";
            };
            MethodInterceptor second = (proxy, method, args, invoker) -> {
                log.append("-second");
                return "second-result";
            };

            MethodInterceptor chained = first.andThen(second);
            Method method = Object.class.getMethod("toString");
            Object result = chained.intercept(new Object(), method, new Object[]{}, MethodInvoker.noOp());

            assertThat(log.toString()).isEqualTo("first-second");
            assertThat(result).isEqualTo("second-result");
        }
    }

    @Nested
    @DisplayName("compose方法测试")
    class ComposeTests {

        @Test
        @DisplayName("compose组合拦截器")
        void testCompose() throws Throwable {
            StringBuilder log = new StringBuilder();
            MethodInterceptor first = (proxy, method, args, invoker) -> {
                log.append("first");
                return "first-result";
            };
            MethodInterceptor second = (proxy, method, args, invoker) -> {
                log.append("-second");
                return "second-result";
            };

            MethodInterceptor composed = second.compose(first);
            Method method = Object.class.getMethod("toString");
            Object result = composed.intercept(new Object(), method, new Object[]{}, MethodInvoker.noOp());

            assertThat(log.toString()).isEqualTo("first-second");
            assertThat(result).isEqualTo("second-result");
        }
    }
}
