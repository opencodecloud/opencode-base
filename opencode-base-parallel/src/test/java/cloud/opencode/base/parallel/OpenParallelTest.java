package cloud.opencode.base.parallel;

import cloud.opencode.base.parallel.exception.OpenParallelException;
import cloud.opencode.base.parallel.executor.RateLimitedExecutor;
import cloud.opencode.base.parallel.pipeline.AsyncPipeline;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenParallelTest Tests
 * OpenParallelTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
@DisplayName("OpenParallel 测试")
class OpenParallelTest {

    @Nested
    @DisplayName("runAll方法测试")
    class RunAllTests {

        @Test
        @DisplayName("并行运行所有任务(varargs)")
        void testRunAllVarargs() {
            AtomicInteger counter = new AtomicInteger(0);

            OpenParallel.runAll(
                    () -> counter.incrementAndGet(),
                    () -> counter.incrementAndGet(),
                    () -> counter.incrementAndGet()
            );

            assertThat(counter.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("并行运行所有任务(Collection)")
        void testRunAllCollection() {
            AtomicInteger counter = new AtomicInteger(0);
            List<Runnable> tasks = List.of(
                    () -> counter.incrementAndGet(),
                    () -> counter.incrementAndGet()
            );

            OpenParallel.runAll(tasks);

            assertThat(counter.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("并行运行所有任务带超时")
        void testRunAllWithTimeout() {
            AtomicInteger counter = new AtomicInteger(0);
            List<Runnable> tasks = List.of(
                    () -> counter.incrementAndGet(),
                    () -> counter.incrementAndGet()
            );

            OpenParallel.runAll(tasks, Duration.ofSeconds(5));

            assertThat(counter.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("超时时抛出异常")
        void testRunAllTimeout() {
            List<Runnable> tasks = List.of(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            assertThatThrownBy(() -> OpenParallel.runAll(tasks, Duration.ofMillis(50)))
                    .isInstanceOf(OpenParallelException.class);
        }
    }

    @Nested
    @DisplayName("invokeAll方法测试")
    class InvokeAllTests {

        @Test
        @DisplayName("并行调用所有Supplier(varargs)")
        void testInvokeAllVarargs() {
            List<String> results = OpenParallel.invokeAll(
                    () -> "a",
                    () -> "b",
                    () -> "c"
            );

            assertThat(results).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("并行调用所有Supplier(Collection)")
        void testInvokeAllCollection() {
            List<Supplier<String>> suppliers = List.of(
                    () -> "x",
                    () -> "y"
            );

            List<String> results = OpenParallel.invokeAll(suppliers);

            assertThat(results).containsExactly("x", "y");
        }

        @Test
        @DisplayName("并行调用所有Supplier带超时")
        void testInvokeAllWithTimeout() {
            List<Supplier<String>> suppliers = List.of(
                    () -> "fast1",
                    () -> "fast2"
            );

            List<String> results = OpenParallel.invokeAll(suppliers, Duration.ofSeconds(5));

            assertThat(results).containsExactly("fast1", "fast2");
        }

        @Test
        @DisplayName("超时时抛出异常")
        void testInvokeAllTimeout() {
            List<Supplier<String>> suppliers = List.of(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "slow";
            });

            assertThatThrownBy(() -> OpenParallel.invokeAll(suppliers, Duration.ofMillis(50)))
                    .isInstanceOf(OpenParallelException.class);
        }
    }

    @Nested
    @DisplayName("invokeAny方法测试")
    class InvokeAnyTests {

        @Test
        @DisplayName("返回首个完成的结果(varargs)")
        void testInvokeAnyVarargs() {
            String result = OpenParallel.invokeAny(
                    () -> "first",
                    () -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return "slow";
                    }
            );

            assertThat(result).isIn("first", "slow");
        }

        @Test
        @DisplayName("返回首个完成的结果(Collection)")
        void testInvokeAnyCollection() {
            List<Supplier<String>> suppliers = List.of(
                    () -> "result1",
                    () -> "result2"
            );

            String result = OpenParallel.invokeAny(suppliers);

            assertThat(result).isIn("result1", "result2");
        }
    }

    @Nested
    @DisplayName("parallelMap方法测试")
    class ParallelMapTests {

        @Test
        @DisplayName("并行映射列表")
        void testParallelMap() {
            List<Integer> items = List.of(1, 2, 3, 4, 5);

            List<Integer> results = OpenParallel.parallelMap(items, x -> x * 2);

            assertThat(results).containsExactlyInAnyOrder(2, 4, 6, 8, 10);
        }

        @Test
        @DisplayName("并行映射带并发限制")
        void testParallelMapWithParallelism() {
            List<Integer> items = List.of(1, 2, 3, 4, 5);

            List<Integer> results = OpenParallel.parallelMap(items, x -> x * 2, 2);

            assertThat(results).containsExactly(2, 4, 6, 8, 10);
        }

        @Test
        @DisplayName("并行映射带并发限制和超时")
        void testParallelMapWithTimeout() {
            List<Integer> items = List.of(1, 2, 3);

            List<Integer> results = OpenParallel.parallelMap(
                    items, x -> x * 2, 2, Duration.ofSeconds(5));

            assertThat(results).containsExactly(2, 4, 6);
        }

        @Test
        @DisplayName("超时时抛出异常")
        void testParallelMapTimeout() {
            List<Integer> items = List.of(1);

            assertThatThrownBy(() -> OpenParallel.parallelMap(items, x -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return x;
            }, 1, Duration.ofMillis(50)))
                    .isInstanceOf(OpenParallelException.class);
        }
    }

    @Nested
    @DisplayName("processBatch方法测试")
    class ProcessBatchTests {

        @Test
        @DisplayName("批量处理项目")
        void testProcessBatch() {
            List<Integer> items = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            AtomicInteger batchCount = new AtomicInteger(0);

            OpenParallel.processBatch(items, 3, batch -> batchCount.incrementAndGet());

            assertThat(batchCount.get()).isEqualTo(4); // ceil(10/3) = 4
        }

        @Test
        @DisplayName("批量处理并收集结果")
        void testProcessBatchAndCollect() {
            List<Integer> items = List.of(1, 2, 3, 4, 5);

            List<Integer> results = OpenParallel.processBatchAndCollect(items, 2,
                    batch -> batch.stream().map(x -> x * 2).toList());

            assertThat(results).containsExactlyInAnyOrder(2, 4, 6, 8, 10);
        }
    }

    @Nested
    @DisplayName("pipeline方法测试")
    class PipelineTests {

        @Test
        @DisplayName("从Supplier创建流水线")
        void testPipelineFromSupplier() {
            AsyncPipeline<String> pipeline = OpenParallel.pipeline(() -> "initial");

            assertThat(pipeline.get()).isEqualTo("initial");
        }

        @Test
        @DisplayName("从CompletableFuture创建流水线")
        void testPipelineFromFuture() {
            CompletableFuture<String> future = CompletableFuture.completedFuture("value");
            AsyncPipeline<String> pipeline = OpenParallel.pipeline(future);

            assertThat(pipeline.get()).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("combine方法测试")
    class CombineTests {

        @Test
        @DisplayName("组合两个Future")
        void testCombineTwo() {
            CompletableFuture<String> f1 = CompletableFuture.completedFuture("Hello");
            CompletableFuture<String> f2 = CompletableFuture.completedFuture("World");

            CompletableFuture<String> combined = OpenParallel.combine(f1, f2, (a, b) -> a + " " + b);

            assertThat(combined.join()).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("组合三个Future")
        void testCombineThree() {
            CompletableFuture<String> f1 = CompletableFuture.completedFuture("a");
            CompletableFuture<String> f2 = CompletableFuture.completedFuture("b");
            CompletableFuture<String> f3 = CompletableFuture.completedFuture("c");

            CompletableFuture<String> combined = OpenParallel.combine(f1, f2, f3,
                    (a, b, c) -> a + b + c);

            assertThat(combined.join()).isEqualTo("abc");
        }
    }

    @Nested
    @DisplayName("async方法测试")
    class AsyncTests {

        @Test
        @DisplayName("异步执行Supplier")
        void testAsyncSupplier() {
            CompletableFuture<String> future = OpenParallel.async(() -> "async result");

            assertThat(future.join()).isEqualTo("async result");
        }

        @Test
        @DisplayName("异步执行Runnable")
        void testAsyncRunnable() {
            AtomicInteger counter = new AtomicInteger(0);

            CompletableFuture<Void> future = OpenParallel.async((Runnable) counter::incrementAndGet);
            future.join();

            assertThat(counter.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("delay方法测试")
    class DelayTests {

        @Test
        @DisplayName("延迟执行")
        void testDelay() {
            long start = System.currentTimeMillis();

            CompletableFuture<String> future = OpenParallel.delay(
                    Duration.ofMillis(100), () -> "delayed");
            future.join();

            long elapsed = System.currentTimeMillis() - start;
            assertThat(elapsed).isGreaterThanOrEqualTo(90);
        }
    }

    @Nested
    @DisplayName("getExecutor方法测试")
    class GetExecutorTests {

        @Test
        @DisplayName("返回共享执行器")
        void testGetExecutor() {
            ExecutorService executor = OpenParallel.getExecutor();

            assertThat(executor).isNotNull();
            assertThat(executor.isShutdown()).isFalse();
        }
    }

    @Nested
    @DisplayName("rateLimited方法测试")
    class RateLimitedTests {

        @Test
        @DisplayName("创建限速执行器")
        void testRateLimited() {
            try (RateLimitedExecutor executor = OpenParallel.rateLimited(100)) {
                assertThat(executor).isNotNull();
            }
        }

        @Test
        @DisplayName("创建带突发容量的限速执行器")
        void testRateLimitedWithBurst() {
            try (RateLimitedExecutor executor = OpenParallel.rateLimited(100, 20)) {
                assertThat(executor).isNotNull();
            }
        }

        @Test
        @DisplayName("限速执行所有任务")
        void testInvokeAllRateLimited() {
            List<String> results = OpenParallel.invokeAllRateLimited(100,
                    () -> "a",
                    () -> "b"
            );

            assertThat(results).containsExactly("a", "b");
        }
    }

}
