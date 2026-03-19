package cloud.opencode.base.core.func;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.*;

/**
 * CheckedCallable 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("CheckedCallable 测试")
class CheckedCallableTest {

    @Nested
    @DisplayName("call 测试")
    class CallTests {

        @Test
        @DisplayName("正常返回值")
        void testNormalReturn() throws Exception {
            CheckedCallable<String> callable = () -> "hello";
            assertThat(callable.call()).isEqualTo("hello");
        }

        @Test
        @DisplayName("返回 null")
        void testReturnNull() throws Exception {
            CheckedCallable<String> callable = () -> null;
            assertThat(callable.call()).isNull();
        }

        @Test
        @DisplayName("抛出受检异常")
        void testThrowCheckedException() {
            CheckedCallable<String> callable = () -> {
                throw new IOException("IO error");
            };

            assertThatThrownBy(callable::call)
                    .isInstanceOf(IOException.class)
                    .hasMessage("IO error");
        }
    }

    @Nested
    @DisplayName("toCallable 测试")
    class ToCallableTests {

        @Test
        @DisplayName("转换为 JDK Callable")
        void testToCallable() throws Exception {
            CheckedCallable<String> checked = () -> "hello";
            Callable<String> callable = checked.toCallable();

            assertThat(callable.call()).isEqualTo("hello");
        }

        @Test
        @DisplayName("异常传递")
        void testExceptionPropagation() {
            CheckedCallable<String> checked = () -> {
                throw new IOException("IO error");
            };
            Callable<String> callable = checked.toCallable();

            assertThatThrownBy(callable::call)
                    .isInstanceOf(IOException.class);
        }
    }

    @Nested
    @DisplayName("callQuietly 测试")
    class CallQuietlyTests {

        @Test
        @DisplayName("正常返回值")
        void testNormalReturn() {
            CheckedCallable<String> callable = () -> "hello";
            assertThat(callable.callQuietly()).isEqualTo("hello");
        }

        @Test
        @DisplayName("异常时返回 null")
        void testReturnNullOnException() {
            CheckedCallable<String> callable = () -> {
                throw new IOException("IO error");
            };
            assertThat(callable.callQuietly()).isNull();
        }
    }

    @Nested
    @DisplayName("callOrDefault 测试")
    class CallOrDefaultTests {

        @Test
        @DisplayName("正常返回值")
        void testNormalReturn() {
            CheckedCallable<String> callable = () -> "hello";
            assertThat(callable.callOrDefault("default")).isEqualTo("hello");
        }

        @Test
        @DisplayName("异常时返回默认值")
        void testReturnDefaultOnException() {
            CheckedCallable<String> callable = () -> {
                throw new IOException("IO error");
            };
            assertThat(callable.callOrDefault("default")).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("of 测试")
    class OfTests {

        @Test
        @DisplayName("从 Callable 创建 CheckedCallable")
        void testOfCallable() throws Exception {
            Callable<String> callable = () -> "hello";
            CheckedCallable<String> checked = CheckedCallable.of(callable);

            assertThat(checked.call()).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("from 测试")
    class FromTests {

        @Test
        @DisplayName("从 CheckedSupplier 创建 CheckedCallable")
        void testFromSupplier() throws Exception {
            CheckedSupplier<String> supplier = () -> "hello";
            CheckedCallable<String> callable = CheckedCallable.from(supplier);

            assertThat(callable.call()).isEqualTo("hello");
        }
    }
}
