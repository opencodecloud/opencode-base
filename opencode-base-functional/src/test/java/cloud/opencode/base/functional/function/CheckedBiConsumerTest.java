package cloud.opencode.base.functional.function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.*;

/**
 * CheckedBiConsumer 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("CheckedBiConsumer 测试")
class CheckedBiConsumerTest {

    @Nested
    @DisplayName("accept() 测试")
    class AcceptTests {

        @Test
        @DisplayName("正常执行")
        void testAcceptNormally() throws Exception {
            List<String> list = new ArrayList<>();
            CheckedBiConsumer<List<String>, String> addToList = List::add;

            addToList.accept(list, "hello");

            assertThat(list).containsExactly("hello");
        }

        @Test
        @DisplayName("抛出受检异常")
        void testAcceptThrowsCheckedException() {
            CheckedBiConsumer<String, String> throwsIO = (a, b) -> {
                throw new IOException("IO error");
            };

            assertThatThrownBy(() -> throwsIO.accept("a", "b"))
                    .isInstanceOf(IOException.class)
                    .hasMessage("IO error");
        }

        @Test
        @DisplayName("可用作Lambda")
        void testAsLambda() throws Exception {
            List<Integer> list = new ArrayList<>();
            CheckedBiConsumer<List<Integer>, Integer> addToList = (l, i) -> l.add(i);

            addToList.accept(list, 42);

            assertThat(list).containsExactly(42);
        }
    }

    @Nested
    @DisplayName("unchecked() 测试")
    class UncheckedTests {

        @Test
        @DisplayName("转换为标准BiConsumer")
        void testUnchecked() {
            List<String> list = new ArrayList<>();
            CheckedBiConsumer<List<String>, String> addToList = List::add;

            BiConsumer<List<String>, String> unchecked = addToList.unchecked();
            unchecked.accept(list, "hello");

            assertThat(list).containsExactly("hello");
        }

        @Test
        @DisplayName("受检异常包装为RuntimeException")
        void testUncheckedWrapsCheckedException() {
            CheckedBiConsumer<String, String> throwsIO = (a, b) -> {
                throw new IOException("IO error");
            };

            BiConsumer<String, String> unchecked = throwsIO.unchecked();

            assertThatThrownBy(() -> unchecked.accept("a", "b"))
                    .isInstanceOf(RuntimeException.class)
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("RuntimeException不被包装")
        void testUncheckedDoesNotWrapRuntimeException() {
            CheckedBiConsumer<String, String> throwsRuntime = (a, b) -> {
                throw new IllegalArgumentException("bad arg");
            };

            BiConsumer<String, String> unchecked = throwsRuntime.unchecked();

            assertThatThrownBy(() -> unchecked.accept("a", "b"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("bad arg");
        }
    }

    @Nested
    @DisplayName("acceptQuietly() 测试")
    class AcceptQuietlyTests {

        @Test
        @DisplayName("正常执行")
        void testAcceptQuietlySuccess() {
            List<String> list = new ArrayList<>();
            CheckedBiConsumer<List<String>, String> addToList = List::add;

            addToList.acceptQuietly(list, "hello");

            assertThat(list).containsExactly("hello");
        }

        @Test
        @DisplayName("异常时静默忽略")
        void testAcceptQuietlyIgnoresException() {
            CheckedBiConsumer<String, String> throwsError = (a, b) -> {
                throw new RuntimeException("error");
            };

            assertThatNoException().isThrownBy(() -> throwsError.acceptQuietly("a", "b"));
        }
    }

    @Nested
    @DisplayName("andThen() 测试")
    class AndThenTests {

        @Test
        @DisplayName("链接两个消费者")
        void testAndThen() throws Exception {
            List<String> list = new ArrayList<>();
            CheckedBiConsumer<List<String>, String> addToList = List::add;
            CheckedBiConsumer<List<String>, String> addAgain = (l, s) -> l.add(s + "!");

            CheckedBiConsumer<List<String>, String> combined = addToList.andThen(addAgain);
            combined.accept(list, "hello");

            assertThat(list).containsExactly("hello", "hello!");
        }

        @Test
        @DisplayName("第一个消费者异常时传播")
        void testAndThenFirstConsumerException() {
            CheckedBiConsumer<String, String> throwsError = (a, b) -> {
                throw new IOException("error");
            };
            CheckedBiConsumer<String, String> second = (a, b) -> {};

            CheckedBiConsumer<String, String> combined = throwsError.andThen(second);

            assertThatThrownBy(() -> combined.accept("a", "b"))
                    .isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("第二个消费者异常时传播")
        void testAndThenSecondConsumerException() {
            List<String> list = new ArrayList<>();
            CheckedBiConsumer<List<String>, String> addToList = List::add;
            CheckedBiConsumer<List<String>, String> throwsError = (l, s) -> {
                throw new IOException("error");
            };

            CheckedBiConsumer<List<String>, String> combined = addToList.andThen(throwsError);

            assertThatThrownBy(() -> combined.accept(list, "hello"))
                    .isInstanceOf(IOException.class);
            // 第一个消费者已执行
            assertThat(list).containsExactly("hello");
        }
    }

    @Nested
    @DisplayName("of() 测试")
    class OfTests {

        @Test
        @DisplayName("包装标准BiConsumer")
        void testOf() throws Exception {
            List<String> list = new ArrayList<>();
            BiConsumer<List<String>, String> stdConsumer = List::add;

            CheckedBiConsumer<List<String>, String> checked = CheckedBiConsumer.of(stdConsumer);
            checked.accept(list, "hello");

            assertThat(list).containsExactly("hello");
        }
    }
}
