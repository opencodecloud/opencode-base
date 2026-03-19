package cloud.opencode.base.parallel.executor;

import cloud.opencode.base.parallel.exception.OpenParallelException;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * VirtualExecutorTest Tests
 * VirtualExecutorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
@DisplayName("VirtualExecutor 测试")
class VirtualExecutorTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("shared返回共享执行器")
        void testShared() {
            VirtualExecutor executor1 = VirtualExecutor.shared();
            VirtualExecutor executor2 = VirtualExecutor.shared();

            assertThat(executor1).isSameAs(executor2);
        }

        @Test
        @DisplayName("create创建新执行器")
        void testCreate() {
            try (VirtualExecutor executor = VirtualExecutor.create()) {
                assertThat(executor).isNotNull();
                assertThat(executor.getAvailablePermits()).isEqualTo(-1);
            }
        }

        @Test
        @DisplayName("withConcurrency创建带并发限制的执行器")
        void testWithConcurrency() {
            try (VirtualExecutor executor = VirtualExecutor.withConcurrency(10)) {
                assertThat(executor.getAvailablePermits()).isEqualTo(10);
                assertThat(executor.getConfig().getMaxConcurrency()).isEqualTo(10);
            }
        }

        @Test
        @DisplayName("withConfig使用配置创建执行器")
        void testWithConfig() {
            ExecutorConfig config = ExecutorConfig.builder()
                    .namePrefix("test-")
                    .maxConcurrency(5)
                    .build();

            try (VirtualExecutor executor = VirtualExecutor.withConfig(config)) {
                assertThat(executor.getAvailablePermits()).isEqualTo(5);
                assertThat(executor.getConfig()).isEqualTo(config);
            }
        }
    }

    @Nested
    @DisplayName("submit方法测试")
    class SubmitTests {

        @Test
        @DisplayName("提交Runnable任务")
        void testSubmitRunnable() {
            try (VirtualExecutor executor = VirtualExecutor.create()) {
                AtomicInteger counter = new AtomicInteger(0);

                CompletableFuture<Void> future = executor.submit((Runnable) counter::incrementAndGet);
                future.join();

                assertThat(counter.get()).isEqualTo(1);
                assertThat(executor.getSubmittedCount()).isEqualTo(1);
                assertThat(executor.getCompletedCount()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("提交Callable任务")
        void testSubmitCallable() {
            try (VirtualExecutor executor = VirtualExecutor.create()) {
                CompletableFuture<String> future = executor.submit(() -> "result");

                assertThat(future.join()).isEqualTo("result");
            }
        }

        @Test
        @DisplayName("任务失败增加失败计数")
        void testSubmitFailedTask() {
            try (VirtualExecutor executor = VirtualExecutor.create()) {
                CompletableFuture<Void> future = executor.submit(() -> {
                    throw new RuntimeException("test error");
                });

                assertThatThrownBy(future::join).hasCauseInstanceOf(OpenParallelException.class);
                assertThat(executor.getFailedCount()).isEqualTo(1);
            }
        }
    }

    @Nested
    @DisplayName("invokeAll方法测试")
    class InvokeAllTests {

        @Test
        @DisplayName("调用所有任务并收集结果")
        void testInvokeAll() {
            try (VirtualExecutor executor = VirtualExecutor.create()) {
                List<String> results = executor.invokeAll(List.of(
                        () -> "a",
                        () -> "b",
                        () -> "c"
                ));

                assertThat(results).containsExactly("a", "b", "c");
            }
        }

        @Test
        @DisplayName("带超时调用所有任务")
        void testInvokeAllWithTimeout() {
            try (VirtualExecutor executor = VirtualExecutor.create()) {
                List<String> results = executor.invokeAll(
                        List.of(() -> "a", () -> "b"),
                        Duration.ofSeconds(5)
                );

                assertThat(results).containsExactly("a", "b");
            }
        }

        @Test
        @DisplayName("超时时抛出异常")
        void testInvokeAllTimeout() {
            try (VirtualExecutor executor = VirtualExecutor.create()) {
                assertThatThrownBy(() -> executor.invokeAll(
                        List.of(() -> {
                            Thread.sleep(5000);
                            return "slow";
                        }),
                        Duration.ofMillis(50)
                )).isInstanceOf(OpenParallelException.class)
                  .hasMessageContaining("timeout");
            }
        }
    }

    @Nested
    @DisplayName("execute方法测试")
    class ExecuteTests {

        @Test
        @DisplayName("使用底层执行器执行任务")
        void testExecute() {
            try (VirtualExecutor executor = VirtualExecutor.create()) {
                AtomicInteger counter = new AtomicInteger(0);

                executor.execute(() -> counter.incrementAndGet()).get();

                assertThat(counter.get()).isEqualTo(1);
            } catch (Exception e) {
                fail("Unexpected exception", e);
            }
        }
    }

    @Nested
    @DisplayName("统计方法测试")
    class StatisticsTests {

        @Test
        @DisplayName("获取各种计数")
        void testStatistics() {
            try (VirtualExecutor executor = VirtualExecutor.create()) {
                executor.submit(() -> "a").join();
                executor.submit(() -> "b").join();

                assertThat(executor.getSubmittedCount()).isEqualTo(2);
                assertThat(executor.getCompletedCount()).isEqualTo(2);
                assertThat(executor.getFailedCount()).isZero();
                assertThat(executor.getPendingCount()).isZero();
            }
        }
    }

    @Nested
    @DisplayName("生命周期方法测试")
    class LifecycleTests {

        @Test
        @DisplayName("shutdown关闭执行器")
        void testShutdown() {
            VirtualExecutor executor = VirtualExecutor.create();
            assertThat(executor.isShutdown()).isFalse();

            executor.shutdown();

            assertThat(executor.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("shutdownAndAwait等待终止")
        void testShutdownAndAwait() throws InterruptedException {
            VirtualExecutor executor = VirtualExecutor.create();

            boolean terminated = executor.shutdownAndAwait(Duration.ofSeconds(5));

            assertThat(terminated).isTrue();
            assertThat(executor.isTerminated()).isTrue();
        }

        @Test
        @DisplayName("close调用shutdown")
        void testClose() {
            VirtualExecutor executor = VirtualExecutor.create();

            executor.close();

            assertThat(executor.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("共享执行器不会被关闭")
        void testSharedNotShutdown() {
            VirtualExecutor shared = VirtualExecutor.shared();

            shared.shutdown();

            assertThat(shared.isShutdown()).isFalse();
        }

        @Test
        @DisplayName("共享执行器shutdownAndAwait直接返回true")
        void testSharedShutdownAndAwait() throws InterruptedException {
            VirtualExecutor shared = VirtualExecutor.shared();

            boolean result = shared.shutdownAndAwait(Duration.ofSeconds(1));

            assertThat(result).isTrue();
            assertThat(shared.isShutdown()).isFalse();
        }
    }

    @Nested
    @DisplayName("并发限制测试")
    class ConcurrencyLimitTests {

        @Test
        @DisplayName("并发限制控制同时执行的任务数")
        void testConcurrencyLimit() {
            try (VirtualExecutor executor = VirtualExecutor.withConcurrency(2)) {
                AtomicInteger concurrent = new AtomicInteger(0);
                AtomicInteger maxConcurrent = new AtomicInteger(0);

                Runnable task = () -> {
                    int c = concurrent.incrementAndGet();
                    maxConcurrent.updateAndGet(m -> Math.max(m, c));
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    concurrent.decrementAndGet();
                };

                List<CompletableFuture<Void>> futures = List.of(
                        executor.submit(task),
                        executor.submit(task),
                        executor.submit(task)
                );

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                assertThat(maxConcurrent.get()).isLessThanOrEqualTo(2);
            }
        }
    }
}
