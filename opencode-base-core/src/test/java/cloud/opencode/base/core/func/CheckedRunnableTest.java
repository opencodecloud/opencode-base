package cloud.opencode.base.core.func;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * CheckedRunnable 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("CheckedRunnable 测试")
class CheckedRunnableTest {

    @Nested
    @DisplayName("run 测试")
    class RunTests {

        @Test
        @DisplayName("正常执行")
        void testNormalExecution() throws Exception {
            List<String> result = new ArrayList<>();
            CheckedRunnable runnable = () -> result.add("executed");

            runnable.run();

            assertThat(result).containsExactly("executed");
        }

        @Test
        @DisplayName("抛出受检异常")
        void testThrowCheckedException() {
            CheckedRunnable runnable = () -> {
                throw new IOException("IO error");
            };

            assertThatThrownBy(runnable::run)
                    .isInstanceOf(IOException.class)
                    .hasMessage("IO error");
        }

        @Test
        @DisplayName("抛出运行时异常")
        void testThrowRuntimeException() {
            CheckedRunnable runnable = () -> {
                throw new IllegalArgumentException("Invalid");
            };

            assertThatThrownBy(runnable::run)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid");
        }
    }

    @Nested
    @DisplayName("unchecked 测试")
    class UncheckedTests {

        @Test
        @DisplayName("正常执行转换为 Runnable")
        void testNormalExecution() {
            List<String> result = new ArrayList<>();
            CheckedRunnable checked = () -> result.add("executed");
            Runnable runnable = checked.unchecked();

            runnable.run();

            assertThat(result).containsExactly("executed");
        }

        @Test
        @DisplayName("受检异常包装为 RuntimeException")
        void testCheckedExceptionWrapped() {
            CheckedRunnable checked = () -> {
                throw new IOException("IO error");
            };
            Runnable runnable = checked.unchecked();

            assertThatThrownBy(runnable::run)
                    .isInstanceOf(RuntimeException.class)
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("运行时异常直接抛出")
        void testRuntimeExceptionDirectly() {
            CheckedRunnable checked = () -> {
                throw new IllegalArgumentException("Invalid");
            };
            Runnable runnable = checked.unchecked();

            assertThatThrownBy(runnable::run)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid");
        }
    }

    @Nested
    @DisplayName("runQuietly 测试")
    class RunQuietlyTests {

        @Test
        @DisplayName("正常执行")
        void testNormalExecution() {
            List<String> result = new ArrayList<>();
            CheckedRunnable runnable = () -> result.add("executed");

            runnable.runQuietly();

            assertThat(result).containsExactly("executed");
        }

        @Test
        @DisplayName("静默忽略异常")
        void testIgnoreException() {
            CheckedRunnable runnable = () -> {
                throw new IOException("IO error");
            };

            assertThatNoException().isThrownBy(runnable::runQuietly);
        }
    }

    @Nested
    @DisplayName("andThen 测试")
    class AndThenTests {

        @Test
        @DisplayName("组合两个 Runnable")
        void testComposition() throws Exception {
            List<String> result = new ArrayList<>();
            CheckedRunnable first = () -> result.add("first");
            CheckedRunnable second = () -> result.add("second");

            first.andThen(second).run();

            assertThat(result).containsExactly("first", "second");
        }

        @Test
        @DisplayName("第一个抛异常时不执行第二个")
        void testFirstThrows() {
            List<String> result = new ArrayList<>();
            CheckedRunnable first = () -> {
                throw new IOException("First failed");
            };
            CheckedRunnable second = () -> result.add("second");

            assertThatThrownBy(() -> first.andThen(second).run())
                    .isInstanceOf(IOException.class);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("第二个抛异常")
        void testSecondThrows() {
            List<String> result = new ArrayList<>();
            CheckedRunnable first = () -> result.add("first");
            CheckedRunnable second = () -> {
                throw new IOException("Second failed");
            };

            assertThatThrownBy(() -> first.andThen(second).run())
                    .isInstanceOf(IOException.class);
            assertThat(result).containsExactly("first");
        }

        @Test
        @DisplayName("链式组合多个 Runnable")
        void testChainedComposition() throws Exception {
            List<String> result = new ArrayList<>();
            CheckedRunnable r1 = () -> result.add("1");
            CheckedRunnable r2 = () -> result.add("2");
            CheckedRunnable r3 = () -> result.add("3");

            r1.andThen(r2).andThen(r3).run();

            assertThat(result).containsExactly("1", "2", "3");
        }
    }

    @Nested
    @DisplayName("of 测试")
    class OfTests {

        @Test
        @DisplayName("从 Runnable 创建 CheckedRunnable")
        void testOfRunnable() throws Exception {
            List<String> result = new ArrayList<>();
            Runnable runnable = () -> result.add("executed");
            CheckedRunnable checked = CheckedRunnable.of(runnable);

            checked.run();

            assertThat(result).containsExactly("executed");
        }
    }

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("作为 lambda 使用")
        void testAsLambda() throws Exception {
            List<Integer> result = new ArrayList<>();
            CheckedRunnable runnable = () -> result.add(42);

            runnable.run();

            assertThat(result).containsExactly(42);
        }
    }
}
