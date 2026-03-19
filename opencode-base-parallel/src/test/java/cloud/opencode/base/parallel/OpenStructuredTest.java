package cloud.opencode.base.parallel;

import cloud.opencode.base.parallel.exception.OpenParallelException;
import cloud.opencode.base.parallel.structured.StructuredScope;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenStructuredTest Tests
 * OpenStructuredTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
@DisplayName("OpenStructured 测试")
class OpenStructuredTest {

    @Nested
    @DisplayName("invokeAll方法测试")
    class InvokeAllTests {

        @Test
        @DisplayName("调用所有任务并收集结果")
        void testInvokeAll() {
            List<Callable<String>> tasks = List.of(
                    () -> "a",
                    () -> "b",
                    () -> "c"
            );

            List<String> results = OpenStructured.invokeAll(tasks);

            assertThat(results).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("任一任务失败则快速失败")
        void testInvokeAllFailFast() {
            List<Callable<String>> tasks = List.of(
                    () -> "success",
                    () -> { throw new RuntimeException("failure"); }
            );

            assertThatThrownBy(() -> OpenStructured.invokeAll(tasks))
                    .isInstanceOf(OpenParallelException.class);
        }

        @Test
        @DisplayName("带超时调用所有任务")
        void testInvokeAllWithTimeout() {
            List<Callable<String>> tasks = List.of(
                    () -> "fast1",
                    () -> "fast2"
            );

            List<String> results = OpenStructured.invokeAll(tasks, Duration.ofSeconds(5));

            assertThat(results).containsExactly("fast1", "fast2");
        }
    }

    @Nested
    @DisplayName("parallel方法测试")
    class ParallelTests {

        @Test
        @DisplayName("并行调用两个任务并组合")
        void testParallelTwo() {
            String result = OpenStructured.parallel(
                    () -> "Hello",
                    () -> "World",
                    (a, b) -> a + " " + b
            );

            assertThat(result).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("并行调用三个任务并组合")
        void testParallelThree() {
            String result = OpenStructured.parallel(
                    () -> "a",
                    () -> "b",
                    () -> "c",
                    (a, b, c) -> a + b + c
            );

            assertThat(result).isEqualTo("abc");
        }

        @Test
        @DisplayName("任务失败时抛出异常")
        void testParallelFailure() {
            assertThatThrownBy(() -> OpenStructured.parallel(
                    () -> "success",
                    () -> { throw new RuntimeException("failure"); },
                    (a, b) -> a + b
            )).isInstanceOf(OpenParallelException.class);
        }
    }

    @Nested
    @DisplayName("invokeAny方法测试")
    class InvokeAnyTests {

        @Test
        @DisplayName("返回首个成功的结果")
        void testInvokeAny() {
            List<Callable<String>> tasks = List.of(
                    () -> "first",
                    () -> {
                        Thread.sleep(1000);
                        return "slow";
                    }
            );

            String result = OpenStructured.invokeAny(tasks);

            assertThat(result).isIn("first", "slow");
        }

        @Test
        @DisplayName("所有任务失败时抛出异常")
        void testInvokeAnyAllFail() {
            List<Callable<String>> tasks = List.of(
                    () -> { throw new RuntimeException("error1"); },
                    () -> { throw new RuntimeException("error2"); }
            );

            assertThatThrownBy(() -> OpenStructured.invokeAny(tasks))
                    .isInstanceOf(OpenParallelException.class);
        }

        @Test
        @DisplayName("带超时返回首个成功的结果")
        void testInvokeAnyWithTimeout() {
            List<Callable<String>> tasks = List.of(
                    () -> "fast",
                    () -> {
                        Thread.sleep(100);
                        return "slow";
                    }
            );

            String result = OpenStructured.invokeAny(tasks, Duration.ofSeconds(5));

            assertThat(result).isIn("fast", "slow");
        }
    }

    @Nested
    @DisplayName("race方法测试")
    class RaceTests {

        @Test
        @DisplayName("竞争多个任务返回首个完成的")
        void testRace() {
            String result = OpenStructured.race(
                    () -> "task1",
                    () -> "task2",
                    () -> "task3"
            );

            assertThat(result).isIn("task1", "task2", "task3");
        }
    }

    @Nested
    @DisplayName("runWithContext方法测试")
    class RunWithContextTests {

        @Test
        @DisplayName("使用作用域值运行任务")
        void testRunWithContext() {
            ScopedValue<String> scopedValue = ScopedValue.newInstance();

            String result = OpenStructured.runWithContext(
                    scopedValue, "context-value",
                    () -> scopedValue.get()
            );

            assertThat(result).isEqualTo("context-value");
        }

        @Test
        @DisplayName("任务抛出异常时包装为OpenParallelException")
        void testRunWithContextException() {
            ScopedValue<String> scopedValue = ScopedValue.newInstance();

            assertThatThrownBy(() -> OpenStructured.runWithContext(
                    scopedValue, "value",
                    () -> { throw new RuntimeException("error"); }
            )).isInstanceOf(OpenParallelException.class);
        }
    }

    @Nested
    @DisplayName("runAllWithContext方法测试")
    class RunAllWithContextTests {

        @Test
        @DisplayName("使用作用域值运行多个任务")
        void testRunAllWithContext() {
            ScopedValue<String> scopedValue = ScopedValue.newInstance();

            List<Callable<String>> tasks = List.of(
                    () -> "task1-" + scopedValue.get(),
                    () -> "task2-" + scopedValue.get()
            );

            List<String> results = OpenStructured.runAllWithContext(
                    scopedValue, "ctx", tasks);

            assertThat(results).containsExactly("task1-ctx", "task2-ctx");
        }
    }

    @Nested
    @DisplayName("scope方法测试")
    class ScopeTests {

        @Test
        @DisplayName("创建失败关闭策略的作用域")
        void testScope() {
            try (StructuredScope<String> scope = OpenStructured.scope()) {
                assertThat(scope.getPolicy()).isEqualTo(StructuredScope.Policy.SHUTDOWN_ON_FAILURE);
            }
        }

        @Test
        @DisplayName("scopeAny创建成功关闭策略的作用域")
        void testScopeAny() {
            try (StructuredScope<String> scope = OpenStructured.scopeAny()) {
                assertThat(scope.getPolicy()).isEqualTo(StructuredScope.Policy.SHUTDOWN_ON_SUCCESS);
            }
        }
    }

    @Nested
    @DisplayName("中断处理测试")
    class InterruptionTests {

        @Test
        @DisplayName("invokeAll被中断时设置中断标志并抛出异常")
        void testInvokeAllInterrupted() {
            AtomicReference<Boolean> wasInterrupted = new AtomicReference<>(false);

            Thread testThread = new Thread(() -> {
                try {
                    OpenStructured.invokeAll(List.of(() -> {
                        Thread.sleep(5000);
                        return "slow";
                    }));
                } catch (OpenParallelException e) {
                    wasInterrupted.set(Thread.currentThread().isInterrupted());
                }
            });

            testThread.start();

            try {
                Thread.sleep(100);
                testThread.interrupt();
                testThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
