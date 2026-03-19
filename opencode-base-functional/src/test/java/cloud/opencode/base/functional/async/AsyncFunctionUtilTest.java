package cloud.opencode.base.functional.async;

import cloud.opencode.base.functional.monad.Try;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * AsyncFunctionUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("AsyncFunctionUtil 测试")
class AsyncFunctionUtilTest {

    @Nested
    @DisplayName("async() 测试")
    class AsyncTests {

        @Test
        @DisplayName("async() 异步执行 Supplier")
        void testAsync() {
            CompletableFuture<Integer> future = AsyncFunctionUtil.async(() -> 42);

            assertThat(future.join()).isEqualTo(42);
        }

        @Test
        @DisplayName("async() 在虚拟线程上执行")
        void testAsyncOnVirtualThread() {
            CompletableFuture<Boolean> future = AsyncFunctionUtil.async(
                    () -> Thread.currentThread().isVirtual()
            );

            assertThat(future.join()).isTrue();
        }

        @Test
        @DisplayName("async() 处理计算")
        void testAsyncComputation() {
            CompletableFuture<Integer> future = AsyncFunctionUtil.async(() -> {
                int sum = 0;
                for (int i = 1; i <= 10; i++) {
                    sum += i;
                }
                return sum;
            });

            assertThat(future.join()).isEqualTo(55);
        }
    }

    @Nested
    @DisplayName("asyncRun() 测试")
    class AsyncRunTests {

        @Test
        @DisplayName("asyncRun() 异步执行 Runnable")
        void testAsyncRun() {
            AtomicBoolean executed = new AtomicBoolean(false);
            CompletableFuture<Void> future = AsyncFunctionUtil.asyncRun(() -> executed.set(true));

            future.join();
            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("asyncRun() 在虚拟线程上执行")
        void testAsyncRunOnVirtualThread() {
            AtomicBoolean isVirtual = new AtomicBoolean(false);
            CompletableFuture<Void> future = AsyncFunctionUtil.asyncRun(
                    () -> isVirtual.set(Thread.currentThread().isVirtual())
            );

            future.join();
            assertThat(isVirtual.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("asyncWithTimeout() 测试")
    class AsyncWithTimeoutTests {

        @Test
        @DisplayName("asyncWithTimeout() 成功时返回 Success")
        void testAsyncWithTimeoutSuccess() {
            Try<Integer> result = AsyncFunctionUtil.asyncWithTimeout(
                    () -> 42,
                    Duration.ofSeconds(1)
            );

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("asyncWithTimeout() 超时时返回 Failure")
        void testAsyncWithTimeoutTimeout() {
            Try<Integer> result = AsyncFunctionUtil.asyncWithTimeout(
                    () -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return 42;
                    },
                    Duration.ofMillis(50)
            );

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause().get()).isInstanceOf(TimeoutException.class);
        }

        @Test
        @DisplayName("asyncWithTimeout() 异常时返回 Failure")
        void testAsyncWithTimeoutException() {
            Try<Integer> result = AsyncFunctionUtil.asyncWithTimeout(
                    () -> {
                        throw new RuntimeException("error");
                    },
                    Duration.ofSeconds(1)
            );

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause().get()).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("parallelMap() 测试")
    class ParallelMapTests {

        @Test
        @DisplayName("parallelMap() 并行映射列表")
        void testParallelMap() {
            List<Integer> items = List.of(1, 2, 3, 4, 5);

            List<Integer> results = AsyncFunctionUtil.parallelMap(items, n -> n * 2);

            assertThat(results).containsExactly(2, 4, 6, 8, 10);
        }

        @Test
        @DisplayName("parallelMap() 保持原始顺序")
        void testParallelMapPreservesOrder() {
            List<Integer> items = List.of(5, 4, 3, 2, 1);

            List<String> results = AsyncFunctionUtil.parallelMap(items, String::valueOf);

            assertThat(results).containsExactly("5", "4", "3", "2", "1");
        }

        @Test
        @DisplayName("parallelMap() 空列表返回空列表")
        void testParallelMapEmptyList() {
            List<Integer> items = List.of();

            List<Integer> results = AsyncFunctionUtil.parallelMap(items, n -> n * 2);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("parallelMap() 真正并行执行")
        void testParallelMapIsParallel() {
            List<Integer> items = List.of(1, 2, 3, 4, 5);
            AtomicInteger concurrentCount = new AtomicInteger(0);
            AtomicInteger maxConcurrent = new AtomicInteger(0);

            List<Integer> results = AsyncFunctionUtil.parallelMap(items, n -> {
                int current = concurrentCount.incrementAndGet();
                maxConcurrent.updateAndGet(max -> Math.max(max, current));
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                concurrentCount.decrementAndGet();
                return n * 2;
            });

            assertThat(results).containsExactly(2, 4, 6, 8, 10);
            // 应该有并行执行
            assertThat(maxConcurrent.get()).isGreaterThan(1);
        }
    }

    @Nested
    @DisplayName("parallelMapTry() 测试")
    class ParallelMapTryTests {

        @Test
        @DisplayName("parallelMapTry() 成功时返回 Success")
        void testParallelMapTrySuccess() {
            List<Integer> items = List.of(1, 2, 3);

            Try<List<Integer>> result = AsyncFunctionUtil.parallelMapTry(items, n -> n * 2);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).containsExactly(2, 4, 6);
        }

        @Test
        @DisplayName("parallelMapTry() 失败时返回 Failure")
        void testParallelMapTryFailure() {
            List<Integer> items = List.of(1, 2, 3);

            Try<List<Integer>> result = AsyncFunctionUtil.parallelMapTry(items, n -> {
                if (n == 2) {
                    throw new RuntimeException("error on 2");
                }
                return n * 2;
            });

            assertThat(result.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("runAll() 测试")
    class RunAllTests {

        @Test
        @DisplayName("runAll() 并行运行多个 Supplier")
        void testRunAll() {
            List<CompletableFuture<Integer>> futures = AsyncFunctionUtil.runAll(
                    () -> 1,
                    () -> 2,
                    () -> 3
            );

            assertThat(futures).hasSize(3);
            assertThat(futures.get(0).join()).isEqualTo(1);
            assertThat(futures.get(1).join()).isEqualTo(2);
            assertThat(futures.get(2).join()).isEqualTo(3);
        }

        @Test
        @DisplayName("runAll() 空参数返回空列表")
        void testRunAllEmpty() {
            List<CompletableFuture<Integer>> futures = AsyncFunctionUtil.runAll();

            assertThat(futures).isEmpty();
        }
    }

    @Nested
    @DisplayName("runAllAsync() 测试")
    class RunAllAsyncTests {

        @Test
        @DisplayName("runAllAsync() 并行运行多个 Runnable")
        void testRunAllAsync() {
            AtomicInteger counter = new AtomicInteger(0);

            List<CompletableFuture<Void>> futures = AsyncFunctionUtil.runAllAsync(
                    counter::incrementAndGet,
                    counter::incrementAndGet,
                    counter::incrementAndGet
            );

            // 等待所有完成
            futures.forEach(CompletableFuture::join);

            assertThat(futures).hasSize(3);
            assertThat(counter.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("runAllAsync() 空参数返回空列表")
        void testRunAllAsyncEmpty() {
            List<CompletableFuture<Void>> futures = AsyncFunctionUtil.runAllAsync();

            assertThat(futures).isEmpty();
        }
    }

    @Nested
    @DisplayName("awaitAll() 测试")
    class AwaitAllTests {

        @Test
        @DisplayName("awaitAll() 等待所有 Future 完成")
        void testAwaitAll() {
            List<CompletableFuture<Integer>> futures = List.of(
                    CompletableFuture.completedFuture(1),
                    CompletableFuture.completedFuture(2),
                    CompletableFuture.completedFuture(3)
            );

            Try<List<Integer>> result = AsyncFunctionUtil.awaitAll(futures);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("awaitAll() 失败时返回 Failure")
        void testAwaitAllFailure() {
            List<CompletableFuture<Integer>> futures = List.of(
                    CompletableFuture.completedFuture(1),
                    CompletableFuture.failedFuture(new RuntimeException("error")),
                    CompletableFuture.completedFuture(3)
            );

            Try<List<Integer>> result = AsyncFunctionUtil.awaitAll(futures);

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("awaitAll() 带超时成功")
        void testAwaitAllWithTimeoutSuccess() {
            List<CompletableFuture<Integer>> futures = List.of(
                    CompletableFuture.completedFuture(1),
                    CompletableFuture.completedFuture(2)
            );

            Try<List<Integer>> result = AsyncFunctionUtil.awaitAll(futures, Duration.ofSeconds(1));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).containsExactly(1, 2);
        }

        @Test
        @DisplayName("awaitAll() 带超时超时")
        void testAwaitAllWithTimeoutTimeout() {
            List<CompletableFuture<Integer>> futures = List.of(
                    AsyncFunctionUtil.async(() -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return 1;
                    })
            );

            Try<List<Integer>> result = AsyncFunctionUtil.awaitAll(futures, Duration.ofMillis(50));

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause().get()).isInstanceOf(TimeoutException.class);
        }
    }

    @Nested
    @DisplayName("awaitFirst() 测试")
    class AwaitFirstTests {

        @Test
        @DisplayName("awaitFirst() 返回第一个完成的结果")
        void testAwaitFirst() {
            List<CompletableFuture<Integer>> futures = List.of(
                    CompletableFuture.completedFuture(1),
                    CompletableFuture.completedFuture(2)
            );

            CompletableFuture<Integer> result = AsyncFunctionUtil.awaitFirst(futures);

            assertThat(result.join()).isIn(1, 2);
        }

        @Test
        @DisplayName("awaitFirst() 快的 Future 先完成")
        void testAwaitFirstFastWins() {
            List<CompletableFuture<Integer>> futures = List.of(
                    AsyncFunctionUtil.async(() -> {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return 1;
                    }),
                    CompletableFuture.completedFuture(2)
            );

            CompletableFuture<Integer> result = AsyncFunctionUtil.awaitFirst(futures);

            assertThat(result.join()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("thenAsync() 测试")
    class ThenAsyncTests {

        @Test
        @DisplayName("thenAsync() 链式异步操作")
        void testThenAsync() {
            CompletableFuture<Integer> future = CompletableFuture.completedFuture(5);

            CompletableFuture<Integer> result = AsyncFunctionUtil.thenAsync(future, n -> n * 2);

            assertThat(result.join()).isEqualTo(10);
        }

        @Test
        @DisplayName("thenAsync() 在虚拟线程上执行")
        void testThenAsyncOnVirtualThread() {
            CompletableFuture<Integer> future = CompletableFuture.completedFuture(5);

            CompletableFuture<Boolean> result = AsyncFunctionUtil.thenAsync(
                    future,
                    n -> Thread.currentThread().isVirtual()
            );

            assertThat(result.join()).isTrue();
        }
    }

    @Nested
    @DisplayName("thenFlatAsync() 测试")
    class ThenFlatAsyncTests {

        @Test
        @DisplayName("thenFlatAsync() 链式异步操作")
        void testThenFlatAsync() {
            CompletableFuture<Integer> future = CompletableFuture.completedFuture(5);

            CompletableFuture<Integer> result = AsyncFunctionUtil.thenFlatAsync(
                    future,
                    n -> CompletableFuture.completedFuture(n * 2)
            );

            assertThat(result.join()).isEqualTo(10);
        }

        @Test
        @DisplayName("thenFlatAsync() 在虚拟线程上执行")
        void testThenFlatAsyncOnVirtualThread() {
            CompletableFuture<Integer> future = CompletableFuture.completedFuture(5);

            CompletableFuture<Boolean> result = AsyncFunctionUtil.thenFlatAsync(
                    future,
                    n -> CompletableFuture.completedFuture(Thread.currentThread().isVirtual())
            );

            assertThat(result.join()).isTrue();
        }
    }

    @Nested
    @DisplayName("recover() 测试")
    class RecoverTests {

        @Test
        @DisplayName("recover() 从失败恢复")
        void testRecover() {
            CompletableFuture<Integer> future = CompletableFuture.failedFuture(
                    new RuntimeException("error")
            );

            CompletableFuture<Integer> result = AsyncFunctionUtil.recover(future, e -> 0);

            assertThat(result.join()).isEqualTo(0);
        }

        @Test
        @DisplayName("recover() 成功时不执行")
        void testRecoverOnSuccess() {
            CompletableFuture<Integer> future = CompletableFuture.completedFuture(42);

            CompletableFuture<Integer> result = AsyncFunctionUtil.recover(future, e -> 0);

            assertThat(result.join()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("recoverAsync() 测试")
    class RecoverAsyncTests {

        @Test
        @DisplayName("recoverAsync() 从失败异步恢复")
        void testRecoverAsync() {
            CompletableFuture<Integer> future = CompletableFuture.failedFuture(
                    new RuntimeException("error")
            );

            CompletableFuture<Integer> result = AsyncFunctionUtil.recoverAsync(
                    future,
                    e -> CompletableFuture.completedFuture(0)
            );

            assertThat(result.join()).isEqualTo(0);
        }

        @Test
        @DisplayName("recoverAsync() 成功时不执行")
        void testRecoverAsyncOnSuccess() {
            CompletableFuture<Integer> future = CompletableFuture.completedFuture(42);

            CompletableFuture<Integer> result = AsyncFunctionUtil.recoverAsync(
                    future,
                    e -> CompletableFuture.completedFuture(0)
            );

            assertThat(result.join()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("delay() 测试")
    class DelayTests {

        @Test
        @DisplayName("delay() 延迟执行")
        void testDelay() {
            long start = System.currentTimeMillis();

            CompletableFuture<Void> future = AsyncFunctionUtil.delay(Duration.ofMillis(100));
            future.join();

            long elapsed = System.currentTimeMillis() - start;
            assertThat(elapsed).isGreaterThanOrEqualTo(100);
        }
    }

    @Nested
    @DisplayName("completed() 测试")
    class CompletedTests {

        @Test
        @DisplayName("completed() 创建已完成的 Future")
        void testCompleted() {
            CompletableFuture<Integer> future = AsyncFunctionUtil.completed(42);

            assertThat(future.isDone()).isTrue();
            assertThat(future.join()).isEqualTo(42);
        }

        @Test
        @DisplayName("completed() 带 null 值")
        void testCompletedWithNull() {
            CompletableFuture<Integer> future = AsyncFunctionUtil.completed(null);

            assertThat(future.isDone()).isTrue();
            assertThat(future.join()).isNull();
        }
    }

    @Nested
    @DisplayName("failed() 测试")
    class FailedTests {

        @Test
        @DisplayName("failed() 创建失败的 Future")
        void testFailed() {
            CompletableFuture<Integer> future = AsyncFunctionUtil.failed(
                    new RuntimeException("error")
            );

            assertThat(future.isDone()).isTrue();
            assertThat(future.isCompletedExceptionally()).isTrue();
        }
    }

    @Nested
    @DisplayName("executor() 测试")
    class ExecutorTests {

        @Test
        @DisplayName("executor() 返回虚拟线程执行器")
        void testExecutor() {
            ExecutorService executor = AsyncFunctionUtil.executor();

            assertThat(executor).isNotNull();
        }

        @Test
        @DisplayName("executor() 返回相同实例")
        void testExecutorSameInstance() {
            ExecutorService executor1 = AsyncFunctionUtil.executor();
            ExecutorService executor2 = AsyncFunctionUtil.executor();

            assertThat(executor1).isSameAs(executor2);
        }

        @Test
        @DisplayName("executor() 使用虚拟线程")
        void testExecutorUsesVirtualThreads() throws Exception {
            ExecutorService executor = AsyncFunctionUtil.executor();
            AtomicBoolean isVirtual = new AtomicBoolean(false);

            executor.submit(() -> isVirtual.set(Thread.currentThread().isVirtual())).get();

            assertThat(isVirtual.get()).isTrue();
        }
    }
}
