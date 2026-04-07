package cloud.opencode.base.functional.function;

import cloud.opencode.base.functional.exception.OpenFunctionalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * CheckedRunnable 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.3
 */
@DisplayName("CheckedRunnable 测试")
class CheckedRunnableTest {

    @Nested
    @DisplayName("run() 测试")
    class RunTests {

        @Test
        @DisplayName("正常执行")
        void testRunNormally() throws Exception {
            AtomicBoolean executed = new AtomicBoolean(false);
            CheckedRunnable runnable = () -> executed.set(true);

            runnable.run();

            assertThat(executed).isTrue();
        }

        @Test
        @DisplayName("抛出受检异常")
        void testRunThrowsCheckedException() {
            CheckedRunnable throwsIO = () -> {
                throw new IOException("IO error");
            };

            assertThatThrownBy(throwsIO::run)
                    .isInstanceOf(IOException.class)
                    .hasMessage("IO error");
        }

        @Test
        @DisplayName("抛出运行时异常")
        void testRunThrowsRuntimeException() {
            CheckedRunnable throwsRuntime = () -> {
                throw new IllegalStateException("bad state");
            };

            assertThatThrownBy(throwsRuntime::run)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("bad state");
        }
    }

    @Nested
    @DisplayName("unchecked() 测试")
    class UncheckedTests {

        @Test
        @DisplayName("转换为标准Runnable")
        void testUnchecked() {
            AtomicBoolean executed = new AtomicBoolean(false);
            CheckedRunnable checked = () -> executed.set(true);

            Runnable unchecked = checked.unchecked();
            unchecked.run();

            assertThat(executed).isTrue();
        }

        @Test
        @DisplayName("受检异常包装为OpenFunctionalException")
        void testUncheckedWrapsCheckedException() {
            CheckedRunnable throwsIO = () -> {
                throw new IOException("IO error");
            };

            Runnable unchecked = throwsIO.unchecked();

            assertThatThrownBy(unchecked::run)
                    .isInstanceOf(OpenFunctionalException.class)
                    .hasMessageContaining("Checked runnable failed")
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("RuntimeException不被包装")
        void testUncheckedDoesNotWrapRuntimeException() {
            CheckedRunnable throwsRuntime = () -> {
                throw new IllegalArgumentException("bad arg");
            };

            Runnable unchecked = throwsRuntime.unchecked();

            assertThatThrownBy(unchecked::run)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("bad arg");
        }

        @Test
        @DisplayName("可用于线程执行")
        void testUncheckedInThread() throws InterruptedException {
            AtomicBoolean executed = new AtomicBoolean(false);
            CheckedRunnable checked = () -> executed.set(true);

            Thread thread = new Thread(checked.unchecked());
            thread.start();
            thread.join();

            assertThat(executed).isTrue();
        }
    }

    @Nested
    @DisplayName("runQuietly() 测试")
    class RunQuietlyTests {

        @Test
        @DisplayName("正常执行")
        void testRunQuietlySuccess() {
            AtomicBoolean executed = new AtomicBoolean(false);
            CheckedRunnable runnable = () -> executed.set(true);

            runnable.runQuietly();

            assertThat(executed).isTrue();
        }

        @Test
        @DisplayName("受检异常时静默忽略")
        void testRunQuietlyIgnoresCheckedException() {
            CheckedRunnable throwsIO = () -> {
                throw new IOException("IO error");
            };

            assertThatNoException().isThrownBy(throwsIO::runQuietly);
        }

        @Test
        @DisplayName("运行时异常时静默忽略")
        void testRunQuietlyIgnoresRuntimeException() {
            CheckedRunnable throwsRuntime = () -> {
                throw new RuntimeException("error");
            };

            assertThatNoException().isThrownBy(throwsRuntime::runQuietly);
        }
    }

    @Nested
    @DisplayName("andThen() 测试")
    class AndThenTests {

        @Test
        @DisplayName("链接两个Runnable")
        void testAndThen() throws Exception {
            List<String> list = new ArrayList<>();
            CheckedRunnable first = () -> list.add("first");
            CheckedRunnable second = () -> list.add("second");

            CheckedRunnable combined = first.andThen(second);
            combined.run();

            assertThat(list).containsExactly("first", "second");
        }

        @Test
        @DisplayName("第一个Runnable异常时传播")
        void testAndThenFirstRunnableException() {
            CheckedRunnable throwsError = () -> {
                throw new IOException("error");
            };
            CheckedRunnable second = () -> {};

            CheckedRunnable combined = throwsError.andThen(second);

            assertThatThrownBy(combined::run)
                    .isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("第二个Runnable异常时传播")
        void testAndThenSecondRunnableException() {
            List<String> list = new ArrayList<>();
            CheckedRunnable first = () -> list.add("first");
            CheckedRunnable throwsError = () -> {
                throw new IOException("error");
            };

            CheckedRunnable combined = first.andThen(throwsError);

            assertThatThrownBy(combined::run)
                    .isInstanceOf(IOException.class);
            // 第一个Runnable已执行
            assertThat(list).containsExactly("first");
        }

        @Test
        @DisplayName("after为null时抛出NullPointerException")
        void testAndThenNullAfter() {
            CheckedRunnable runnable = () -> {};

            assertThatThrownBy(() -> runnable.andThen(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("after must not be null");
        }

        @Test
        @DisplayName("链接三个Runnable")
        void testAndThenMultipleChain() throws Exception {
            List<String> list = new ArrayList<>();
            CheckedRunnable a = () -> list.add("a");
            CheckedRunnable b = () -> list.add("b");
            CheckedRunnable c = () -> list.add("c");

            a.andThen(b).andThen(c).run();

            assertThat(list).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("of() 测试")
    class OfTests {

        @Test
        @DisplayName("包装标准Runnable")
        void testOf() throws Exception {
            AtomicBoolean executed = new AtomicBoolean(false);
            Runnable stdRunnable = () -> executed.set(true);

            CheckedRunnable checked = CheckedRunnable.of(stdRunnable);
            checked.run();

            assertThat(executed).isTrue();
        }

        @Test
        @DisplayName("包装后的Runnable可链式调用")
        void testOfAndThen() throws Exception {
            List<String> list = new ArrayList<>();
            Runnable stdRunnable = () -> list.add("std");

            CheckedRunnable checked = CheckedRunnable.of(stdRunnable);
            checked.andThen(() -> list.add("after")).run();

            assertThat(list).containsExactly("std", "after");
        }
    }
}
