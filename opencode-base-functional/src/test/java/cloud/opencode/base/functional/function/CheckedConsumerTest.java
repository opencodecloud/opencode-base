package cloud.opencode.base.functional.function;

import cloud.opencode.base.functional.exception.OpenFunctionalException;
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
 * @since JDK 25, opencode-base-functional V1.0.3
 */
@DisplayName("CheckedConsumer 测试")
class CheckedConsumerTest {

    @Nested
    @DisplayName("accept() 测试")
    class AcceptTests {

        @Test
        @DisplayName("正常执行")
        void testAcceptNormally() throws Exception {
            List<String> list = new ArrayList<>();
            CheckedConsumer<String> addToList = list::add;

            addToList.accept("hello");

            assertThat(list).containsExactly("hello");
        }

        @Test
        @DisplayName("抛出受检异常")
        void testAcceptThrowsCheckedException() {
            CheckedConsumer<String> throwsIO = s -> {
                throw new IOException("IO error");
            };

            assertThatThrownBy(() -> throwsIO.accept("test"))
                    .isInstanceOf(IOException.class)
                    .hasMessage("IO error");
        }

        @Test
        @DisplayName("可用作Lambda")
        void testAsLambda() throws Exception {
            List<Integer> list = new ArrayList<>();
            CheckedConsumer<Integer> addToList = i -> list.add(i);

            addToList.accept(42);

            assertThat(list).containsExactly(42);
        }
    }

    @Nested
    @DisplayName("unchecked() 测试")
    class UncheckedTests {

        @Test
        @DisplayName("转换为标准Consumer")
        void testUnchecked() {
            List<String> list = new ArrayList<>();
            CheckedConsumer<String> addToList = list::add;

            Consumer<String> unchecked = addToList.unchecked();
            unchecked.accept("hello");

            assertThat(list).containsExactly("hello");
        }

        @Test
        @DisplayName("受检异常包装为OpenFunctionalException")
        void testUncheckedWrapsCheckedException() {
            CheckedConsumer<String> throwsIO = s -> {
                throw new IOException("IO error");
            };

            Consumer<String> unchecked = throwsIO.unchecked();

            assertThatThrownBy(() -> unchecked.accept("test"))
                    .isInstanceOf(OpenFunctionalException.class)
                    .hasMessageContaining("Checked consumer failed")
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("RuntimeException不被包装")
        void testUncheckedDoesNotWrapRuntimeException() {
            CheckedConsumer<String> throwsRuntime = s -> {
                throw new IllegalArgumentException("bad arg");
            };

            Consumer<String> unchecked = throwsRuntime.unchecked();

            assertThatThrownBy(() -> unchecked.accept("test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("bad arg");
        }

        @Test
        @DisplayName("RuntimeException不被双重包装")
        void testUncheckedDoesNotDoubleWrapRuntimeException() {
            CheckedConsumer<String> throwsRuntime = s -> {
                throw new UnsupportedOperationException("unsupported");
            };

            Consumer<String> unchecked = throwsRuntime.unchecked();

            assertThatThrownBy(() -> unchecked.accept("test"))
                    .isExactlyInstanceOf(UnsupportedOperationException.class)
                    .hasNoCause();
        }
    }

    @Nested
    @DisplayName("acceptQuietly() 测试")
    class AcceptQuietlyTests {

        @Test
        @DisplayName("正常执行")
        void testAcceptQuietlySuccess() {
            List<String> list = new ArrayList<>();
            CheckedConsumer<String> addToList = list::add;

            addToList.acceptQuietly("hello");

            assertThat(list).containsExactly("hello");
        }

        @Test
        @DisplayName("受检异常时静默忽略")
        void testAcceptQuietlyIgnoresCheckedException() {
            CheckedConsumer<String> throwsError = s -> {
                throw new IOException("IO error");
            };

            assertThatNoException().isThrownBy(() -> throwsError.acceptQuietly("test"));
        }

        @Test
        @DisplayName("运行时异常时静默忽略")
        void testAcceptQuietlyIgnoresRuntimeException() {
            CheckedConsumer<String> throwsError = s -> {
                throw new RuntimeException("error");
            };

            assertThatNoException().isThrownBy(() -> throwsError.acceptQuietly("test"));
        }
    }

    @Nested
    @DisplayName("andThen() 测试")
    class AndThenTests {

        @Test
        @DisplayName("链接两个消费者")
        void testAndThen() throws Exception {
            List<String> list = new ArrayList<>();
            CheckedConsumer<String> first = s -> list.add("first:" + s);
            CheckedConsumer<String> second = s -> list.add("second:" + s);

            CheckedConsumer<String> combined = first.andThen(second);
            combined.accept("hello");

            assertThat(list).containsExactly("first:hello", "second:hello");
        }

        @Test
        @DisplayName("第一个消费者异常时传播")
        void testAndThenFirstConsumerException() {
            CheckedConsumer<String> throwsError = s -> {
                throw new IOException("error");
            };
            CheckedConsumer<String> second = s -> {};

            CheckedConsumer<String> combined = throwsError.andThen(second);

            assertThatThrownBy(() -> combined.accept("test"))
                    .isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("第二个消费者异常时传播")
        void testAndThenSecondConsumerException() {
            List<String> list = new ArrayList<>();
            CheckedConsumer<String> first = list::add;
            CheckedConsumer<String> throwsError = s -> {
                throw new IOException("error");
            };

            CheckedConsumer<String> combined = first.andThen(throwsError);

            assertThatThrownBy(() -> combined.accept("hello"))
                    .isInstanceOf(IOException.class);
            // 第一个消费者已执行
            assertThat(list).containsExactly("hello");
        }

        @Test
        @DisplayName("after为null时抛出NullPointerException")
        void testAndThenNullAfter() {
            CheckedConsumer<String> consumer = s -> {};

            assertThatThrownBy(() -> consumer.andThen(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("after must not be null");
        }

        @Test
        @DisplayName("链接三个消费者")
        void testAndThenMultipleChain() throws Exception {
            List<String> list = new ArrayList<>();
            CheckedConsumer<String> a = s -> list.add("a");
            CheckedConsumer<String> b = s -> list.add("b");
            CheckedConsumer<String> c = s -> list.add("c");

            a.andThen(b).andThen(c).accept("x");

            assertThat(list).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("of() 测试")
    class OfTests {

        @Test
        @DisplayName("包装标准Consumer")
        void testOf() throws Exception {
            List<String> list = new ArrayList<>();
            Consumer<String> stdConsumer = list::add;

            CheckedConsumer<String> checked = CheckedConsumer.of(stdConsumer);
            checked.accept("hello");

            assertThat(list).containsExactly("hello");
        }

        @Test
        @DisplayName("包装后的Consumer可链式调用")
        void testOfAndThen() throws Exception {
            List<String> list = new ArrayList<>();
            Consumer<String> stdConsumer = list::add;

            CheckedConsumer<String> checked = CheckedConsumer.of(stdConsumer);
            checked.andThen(s -> list.add(s + "!")).accept("hello");

            assertThat(list).containsExactly("hello", "hello!");
        }
    }
}
