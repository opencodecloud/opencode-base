package cloud.opencode.base.core.func;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

/**
 * CheckedSupplier 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("CheckedSupplier 测试")
class CheckedSupplierTest {

    @Nested
    @DisplayName("get 测试")
    class GetTests {

        @Test
        @DisplayName("正常返回值")
        void testNormalReturn() throws Exception {
            CheckedSupplier<String> supplier = () -> "hello";
            assertThat(supplier.get()).isEqualTo("hello");
        }

        @Test
        @DisplayName("返回 null")
        void testReturnNull() throws Exception {
            CheckedSupplier<String> supplier = () -> null;
            assertThat(supplier.get()).isNull();
        }

        @Test
        @DisplayName("抛出受检异常")
        void testThrowCheckedException() {
            CheckedSupplier<String> supplier = () -> {
                throw new IOException("IO error");
            };

            assertThatThrownBy(supplier::get)
                    .isInstanceOf(IOException.class)
                    .hasMessage("IO error");
        }

        @Test
        @DisplayName("抛出运行时异常")
        void testThrowRuntimeException() {
            CheckedSupplier<String> supplier = () -> {
                throw new IllegalStateException("Invalid state");
            };

            assertThatThrownBy(supplier::get)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Invalid state");
        }
    }

    @Nested
    @DisplayName("unchecked 测试")
    class UncheckedTests {

        @Test
        @DisplayName("正常返回值")
        void testNormalReturn() {
            CheckedSupplier<String> checked = () -> "hello";
            Supplier<String> supplier = checked.unchecked();

            assertThat(supplier.get()).isEqualTo("hello");
        }

        @Test
        @DisplayName("受检异常包装为 RuntimeException")
        void testCheckedExceptionWrapped() {
            CheckedSupplier<String> checked = () -> {
                throw new IOException("IO error");
            };
            Supplier<String> supplier = checked.unchecked();

            assertThatThrownBy(supplier::get)
                    .isInstanceOf(RuntimeException.class)
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("运行时异常直接抛出")
        void testRuntimeExceptionDirectly() {
            CheckedSupplier<String> checked = () -> {
                throw new IllegalArgumentException("Invalid");
            };
            Supplier<String> supplier = checked.unchecked();

            assertThatThrownBy(supplier::get)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid");
        }
    }

    @Nested
    @DisplayName("getQuietly 测试")
    class GetQuietlyTests {

        @Test
        @DisplayName("正常返回值")
        void testNormalReturn() {
            CheckedSupplier<String> supplier = () -> "hello";
            assertThat(supplier.getQuietly()).isEqualTo("hello");
        }

        @Test
        @DisplayName("异常时返回 null")
        void testReturnNullOnException() {
            CheckedSupplier<String> supplier = () -> {
                throw new IOException("IO error");
            };
            assertThat(supplier.getQuietly()).isNull();
        }
    }

    @Nested
    @DisplayName("getOrDefault 测试")
    class GetOrDefaultTests {

        @Test
        @DisplayName("正常返回值")
        void testNormalReturn() {
            CheckedSupplier<String> supplier = () -> "hello";
            assertThat(supplier.getOrDefault("default")).isEqualTo("hello");
        }

        @Test
        @DisplayName("异常时返回默认值")
        void testReturnDefaultOnException() {
            CheckedSupplier<String> supplier = () -> {
                throw new IOException("IO error");
            };
            assertThat(supplier.getOrDefault("default")).isEqualTo("default");
        }

        @Test
        @DisplayName("返回 null 时返回 null 而非默认值")
        void testReturnNullNotDefault() {
            CheckedSupplier<String> supplier = () -> null;
            assertThat(supplier.getOrDefault("default")).isNull();
        }
    }

    @Nested
    @DisplayName("of 测试")
    class OfTests {

        @Test
        @DisplayName("从 Supplier 创建 CheckedSupplier")
        void testOfSupplier() throws Exception {
            Supplier<String> supplier = () -> "hello";
            CheckedSupplier<String> checked = CheckedSupplier.of(supplier);

            assertThat(checked.get()).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("作为 lambda 使用")
        void testAsLambda() throws Exception {
            CheckedSupplier<Integer> supplier = () -> 42;
            assertThat(supplier.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("作为方法引用使用")
        void testAsMethodReference() throws Exception {
            CheckedSupplier<Long> supplier = System::currentTimeMillis;
            assertThat(supplier.get()).isPositive();
        }
    }
}
