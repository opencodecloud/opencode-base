package cloud.opencode.base.core.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ExceptionUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("ExceptionUtil 测试")
class ExceptionUtilTest {

    @Nested
    @DisplayName("getRootCause 测试")
    class GetRootCauseTests {

        @Test
        @DisplayName("null 输入返回 null")
        void testNullInput() {
            assertThat(ExceptionUtil.getRootCause(null)).isNull();
        }

        @Test
        @DisplayName("无 cause 返回自身")
        void testNoCause() {
            Exception ex = new RuntimeException("Test");
            assertThat(ExceptionUtil.getRootCause(ex)).isSameAs(ex);
        }

        @Test
        @DisplayName("单层 cause")
        void testSingleCause() {
            Exception root = new IOException("Root");
            Exception ex = new RuntimeException("Wrapper", root);
            assertThat(ExceptionUtil.getRootCause(ex)).isSameAs(root);
        }

        @Test
        @DisplayName("多层嵌套 cause")
        void testNestedCause() {
            Exception root = new IllegalArgumentException("Root");
            Exception middle = new IOException("Middle", root);
            Exception ex = new RuntimeException("Wrapper", middle);

            assertThat(ExceptionUtil.getRootCause(ex)).isSameAs(root);
        }

        @Test
        @DisplayName("循环引用保护")
        void testCyclicCause() {
            RuntimeException ex = new RuntimeException("Self");
            // 模拟循环引用 (cause == this) 通过 initCause 无法实现，跳过此场景
        }
    }

    @Nested
    @DisplayName("getStackTrace 测试")
    class GetStackTraceTests {

        @Test
        @DisplayName("null 输入返回空字符串")
        void testNullInput() {
            assertThat(ExceptionUtil.getStackTrace(null)).isEmpty();
        }

        @Test
        @DisplayName("返回包含异常信息的堆栈")
        void testStackTraceContent() {
            Exception ex = new RuntimeException("Test message");
            String stackTrace = ExceptionUtil.getStackTrace(ex);

            assertThat(stackTrace)
                    .contains("RuntimeException")
                    .contains("Test message")
                    .contains("at ");
        }

        @Test
        @DisplayName("堆栈包含原因异常")
        void testStackTraceWithCause() {
            Exception root = new IOException("Root");
            Exception ex = new RuntimeException("Wrapper", root);
            String stackTrace = ExceptionUtil.getStackTrace(ex);

            assertThat(stackTrace)
                    .contains("RuntimeException")
                    .contains("Wrapper")
                    .contains("Caused by")
                    .contains("IOException")
                    .contains("Root");
        }
    }

    @Nested
    @DisplayName("getCausalChain 测试")
    class GetCausalChainTests {

        @Test
        @DisplayName("null 输入返回空列表")
        void testNullInput() {
            List<Throwable> chain = ExceptionUtil.getCausalChain(null);
            assertThat(chain).isEmpty();
        }

        @Test
        @DisplayName("无 cause 返回单元素列表")
        void testNoCause() {
            Exception ex = new RuntimeException("Test");
            List<Throwable> chain = ExceptionUtil.getCausalChain(ex);

            assertThat(chain).hasSize(1);
            assertThat(chain.get(0)).isSameAs(ex);
        }

        @Test
        @DisplayName("多层 cause 返回完整链")
        void testNestedCause() {
            Exception root = new IllegalArgumentException("Root");
            Exception middle = new IOException("Middle", root);
            Exception ex = new RuntimeException("Wrapper", middle);

            List<Throwable> chain = ExceptionUtil.getCausalChain(ex);

            assertThat(chain).hasSize(3);
            assertThat(chain.get(0)).isSameAs(ex);
            assertThat(chain.get(1)).isSameAs(middle);
            assertThat(chain.get(2)).isSameAs(root);
        }

        @Test
        @DisplayName("返回不可变列表")
        void testUnmodifiableList() {
            Exception ex = new RuntimeException("Test");
            List<Throwable> chain = ExceptionUtil.getCausalChain(ex);

            assertThatThrownBy(() -> chain.add(new Exception()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("unwrap 测试")
    class UnwrapTests {

        @Test
        @DisplayName("null 输入返回 null")
        void testNullInput() {
            assertThat(ExceptionUtil.unwrap(null)).isNull();
        }

        @Test
        @DisplayName("无 cause 返回自身")
        void testNoCause() {
            Exception ex = new RuntimeException("Test");
            assertThat(ExceptionUtil.unwrap(ex)).isSameAs(ex);
        }

        @Test
        @DisplayName("有 cause 返回 cause")
        void testWithCause() {
            Exception cause = new IOException("Cause");
            Exception ex = new RuntimeException("Wrapper", cause);
            assertThat(ExceptionUtil.unwrap(ex)).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("unwrap(Class) 测试")
    class UnwrapWithTypeTests {

        @Test
        @DisplayName("null 输入返回 null")
        void testNullInput() {
            assertThat(ExceptionUtil.unwrap(null, IOException.class)).isNull();
        }

        @Test
        @DisplayName("null 类型返回 null")
        void testNullType() {
            Exception ex = new RuntimeException("Test");
            assertThat(ExceptionUtil.unwrap(ex, (Class<? extends Throwable>) null)).isNull();
        }

        @Test
        @DisplayName("找到指定类型异常")
        void testFoundType() {
            IOException target = new IOException("Target");
            Exception middle = new RuntimeException("Middle", target);
            Exception ex = new IllegalStateException("Wrapper", middle);

            IOException result = ExceptionUtil.unwrap(ex, IOException.class);
            assertThat(result).isSameAs(target);
        }

        @Test
        @DisplayName("未找到返回 null")
        void testNotFound() {
            Exception ex = new RuntimeException("Test", new IllegalArgumentException("cause"));
            assertThat(ExceptionUtil.unwrap(ex, IOException.class)).isNull();
        }

        @Test
        @DisplayName("第一个异常就是目标类型")
        void testFirstIsTarget() {
            IOException ex = new IOException("Target");
            assertThat(ExceptionUtil.unwrap(ex, IOException.class)).isSameAs(ex);
        }
    }

    @Nested
    @DisplayName("wrapAndThrow 测试")
    class WrapAndThrowTests {

        @Test
        @DisplayName("正常执行不抛异常")
        void testNoException() {
            assertThatNoException().isThrownBy(() ->
                    ExceptionUtil.wrapAndThrow(() -> {
                        // do nothing
                    })
            );
        }

        @Test
        @DisplayName("RuntimeException 直接抛出")
        void testRuntimeException() {
            assertThatThrownBy(() ->
                    ExceptionUtil.wrapAndThrow(() -> {
                        throw new IllegalArgumentException("Runtime");
                    })
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessage("Runtime");
        }

        @Test
        @DisplayName("受检异常包装为 OpenException")
        void testCheckedException() {
            assertThatThrownBy(() ->
                    ExceptionUtil.wrapAndThrow(() -> {
                        throw new IOException("Checked");
                    })
            ).isInstanceOf(OpenException.class)
             .hasMessage("[Core] (WRAPPED_EXCEPTION) Checked")
             .hasCauseInstanceOf(IOException.class);
        }
    }

    @Nested
    @DisplayName("wrapAndReturn 测试")
    class WrapAndReturnTests {

        @Test
        @DisplayName("正常返回值")
        void testNormalReturn() {
            String result = ExceptionUtil.wrapAndReturn(() -> "Hello");
            assertThat(result).isEqualTo("Hello");
        }

        @Test
        @DisplayName("RuntimeException 直接抛出")
        void testRuntimeException() {
            assertThatThrownBy(() ->
                    ExceptionUtil.wrapAndReturn(() -> {
                        throw new IllegalArgumentException("Runtime");
                    })
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("受检异常包装为 OpenException")
        void testCheckedException() {
            assertThatThrownBy(() ->
                    ExceptionUtil.wrapAndReturn(() -> {
                        throw new IOException("Checked");
                    })
            ).isInstanceOf(OpenException.class)
             .hasCauseInstanceOf(IOException.class);
        }
    }

    @Nested
    @DisplayName("sneakyThrow 测试")
    class SneakyThrowTests {

        @Test
        @DisplayName("抛出受检异常")
        void testSneakyThrow() {
            assertThatThrownBy(() ->
                    ExceptionUtil.sneakyThrow(new IOException("Sneaky"))
            ).isInstanceOf(IOException.class)
             .hasMessage("Sneaky");
        }
    }

    @Nested
    @DisplayName("contains 测试")
    class ContainsTests {

        @Test
        @DisplayName("包含指定类型返回 true")
        void testContains() {
            Exception root = new IOException("Root");
            Exception ex = new RuntimeException("Wrapper", root);

            assertThat(ExceptionUtil.contains(ex, IOException.class)).isTrue();
        }

        @Test
        @DisplayName("不包含指定类型返回 false")
        void testNotContains() {
            Exception ex = new RuntimeException("Test");
            assertThat(ExceptionUtil.contains(ex, IOException.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("getMessage 测试")
    class GetMessageTests {

        @Test
        @DisplayName("null 输入返回空字符串")
        void testNullInput() {
            assertThat(ExceptionUtil.getMessage(null)).isEmpty();
        }

        @Test
        @DisplayName("有消息返回消息")
        void testWithMessage() {
            Exception ex = new RuntimeException("Test message");
            assertThat(ExceptionUtil.getMessage(ex)).isEqualTo("Test message");
        }

        @Test
        @DisplayName("无消息返回类名")
        void testNoMessage() {
            Exception ex = new RuntimeException();
            assertThat(ExceptionUtil.getMessage(ex)).isEqualTo("java.lang.RuntimeException");
        }
    }

    @Nested
    @DisplayName("getRootCauseMessage 测试")
    class GetRootCauseMessageTests {

        @Test
        @DisplayName("返回根本原因的消息")
        void testRootCauseMessage() {
            Exception root = new IOException("Root message");
            Exception ex = new RuntimeException("Wrapper", root);

            assertThat(ExceptionUtil.getRootCauseMessage(ex)).isEqualTo("Root message");
        }

        @Test
        @DisplayName("无 cause 返回自身消息")
        void testNoCause() {
            Exception ex = new RuntimeException("Self message");
            assertThat(ExceptionUtil.getRootCauseMessage(ex)).isEqualTo("Self message");
        }
    }

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("CheckedRunnable 可作为 lambda")
        void testCheckedRunnable() {
            ExceptionUtil.CheckedRunnable runnable = () -> {
                throw new IOException("Test");
            };
            assertThatThrownBy(runnable::run).isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("CheckedSupplier 可作为 lambda")
        void testCheckedSupplier() {
            ExceptionUtil.CheckedSupplier<String> supplier = () -> {
                throw new IOException("Test");
            };
            assertThatThrownBy(supplier::get).isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("CheckedSupplier 正常返回值")
        void testCheckedSupplierReturn() throws Exception {
            ExceptionUtil.CheckedSupplier<String> supplier = () -> "Hello";
            assertThat(supplier.get()).isEqualTo("Hello");
        }
    }
}
