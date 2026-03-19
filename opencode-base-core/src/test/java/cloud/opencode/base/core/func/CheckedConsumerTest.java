package cloud.opencode.base.core.func;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;

/**
 * CheckedConsumer 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("CheckedConsumer 测试")
class CheckedConsumerTest {

    @Nested
    @DisplayName("accept 测试")
    class AcceptTests {

        @Test
        @DisplayName("正常执行")
        void testNormalExecution() throws Exception {
            List<String> result = new ArrayList<>();
            CheckedConsumer<String> consumer = result::add;

            consumer.accept("test");

            assertThat(result).containsExactly("test");
        }

        @Test
        @DisplayName("抛出受检异常")
        void testThrowCheckedException() {
            CheckedConsumer<String> consumer = s -> {
                throw new IOException("IO error");
            };

            assertThatThrownBy(() -> consumer.accept("test"))
                    .isInstanceOf(IOException.class)
                    .hasMessage("IO error");
        }

        @Test
        @DisplayName("抛出运行时异常")
        void testThrowRuntimeException() {
            CheckedConsumer<String> consumer = s -> {
                throw new IllegalArgumentException("Invalid");
            };

            assertThatThrownBy(() -> consumer.accept("test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid");
        }
    }

    @Nested
    @DisplayName("unchecked 测试")
    class UncheckedTests {

        @Test
        @DisplayName("正常执行转换为 Consumer")
        void testNormalExecution() {
            List<String> result = new ArrayList<>();
            CheckedConsumer<String> checked = result::add;
            Consumer<String> consumer = checked.unchecked();

            consumer.accept("test");

            assertThat(result).containsExactly("test");
        }

        @Test
        @DisplayName("受检异常包装为 RuntimeException")
        void testCheckedExceptionWrapped() {
            CheckedConsumer<String> checked = s -> {
                throw new IOException("IO error");
            };
            Consumer<String> consumer = checked.unchecked();

            assertThatThrownBy(() -> consumer.accept("test"))
                    .isInstanceOf(RuntimeException.class)
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("运行时异常直接抛出")
        void testRuntimeExceptionDirectly() {
            CheckedConsumer<String> checked = s -> {
                throw new IllegalArgumentException("Invalid");
            };
            Consumer<String> consumer = checked.unchecked();

            assertThatThrownBy(() -> consumer.accept("test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid");
        }
    }

    @Nested
    @DisplayName("acceptQuietly 测试")
    class AcceptQuietlyTests {

        @Test
        @DisplayName("正常执行")
        void testNormalExecution() {
            List<String> result = new ArrayList<>();
            CheckedConsumer<String> consumer = result::add;

            consumer.acceptQuietly("test");

            assertThat(result).containsExactly("test");
        }

        @Test
        @DisplayName("静默忽略异常")
        void testIgnoreException() {
            CheckedConsumer<String> consumer = s -> {
                throw new IOException("IO error");
            };

            assertThatNoException().isThrownBy(() -> consumer.acceptQuietly("test"));
        }
    }

    @Nested
    @DisplayName("andThen 测试")
    class AndThenTests {

        @Test
        @DisplayName("组合两个 Consumer")
        void testComposition() throws Exception {
            List<String> result = new ArrayList<>();
            CheckedConsumer<String> first = s -> result.add("first:" + s);
            CheckedConsumer<String> second = s -> result.add("second:" + s);

            first.andThen(second).accept("test");

            assertThat(result).containsExactly("first:test", "second:test");
        }

        @Test
        @DisplayName("第一个抛异常时不执行第二个")
        void testFirstThrows() {
            List<String> result = new ArrayList<>();
            CheckedConsumer<String> first = s -> {
                throw new IOException("First failed");
            };
            CheckedConsumer<String> second = s -> result.add("second:" + s);

            assertThatThrownBy(() -> first.andThen(second).accept("test"))
                    .isInstanceOf(IOException.class);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("第二个抛异常")
        void testSecondThrows() {
            List<String> result = new ArrayList<>();
            CheckedConsumer<String> first = s -> result.add("first:" + s);
            CheckedConsumer<String> second = s -> {
                throw new IOException("Second failed");
            };

            assertThatThrownBy(() -> first.andThen(second).accept("test"))
                    .isInstanceOf(IOException.class);
            assertThat(result).containsExactly("first:test");
        }
    }

    @Nested
    @DisplayName("of 测试")
    class OfTests {

        @Test
        @DisplayName("从 Consumer 创建 CheckedConsumer")
        void testOfConsumer() throws Exception {
            List<String> result = new ArrayList<>();
            Consumer<String> consumer = result::add;
            CheckedConsumer<String> checked = CheckedConsumer.of(consumer);

            checked.accept("test");

            assertThat(result).containsExactly("test");
        }
    }

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("作为 lambda 使用")
        void testAsLambda() throws Exception {
            List<Integer> result = new ArrayList<>();
            CheckedConsumer<Integer> consumer = i -> result.add(i * 2);

            consumer.accept(5);

            assertThat(result).containsExactly(10);
        }

        @Test
        @DisplayName("作为方法引用使用")
        void testAsMethodReference() throws Exception {
            List<String> result = new ArrayList<>();
            CheckedConsumer<String> consumer = result::add;

            consumer.accept("hello");

            assertThat(result).containsExactly("hello");
        }
    }
}
