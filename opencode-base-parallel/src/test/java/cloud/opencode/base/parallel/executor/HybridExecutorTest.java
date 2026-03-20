package cloud.opencode.base.parallel.executor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link HybridExecutor}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
@DisplayName("HybridExecutor")
class HybridExecutorTest {

    private HybridExecutor executor;

    @AfterEach
    void tearDown() {
        if (executor != null) {
            executor.close();
        }
    }

    @Nested
    @DisplayName("create() factory method")
    class CreateFactory {

        @Test
        @DisplayName("should create non-null executor")
        void shouldCreateNonNull() {
            executor = HybridExecutor.create();
            assertThat(executor).isNotNull();
        }

        @Test
        @DisplayName("should not be shutdown initially")
        void shouldNotBeShutdownInitially() {
            executor = HybridExecutor.create();
            assertThat(executor.isShutdown()).isFalse();
        }

        @Test
        @DisplayName("should have zero initial counts")
        void shouldHaveZeroInitialCounts() {
            executor = HybridExecutor.create();
            assertThat(executor.getCpuSubmittedCount()).isZero();
            assertThat(executor.getIoSubmittedCount()).isZero();
            assertThat(executor.getCompletedCount()).isZero();
            assertThat(executor.getFailedCount()).isZero();
        }
    }

    @Nested
    @DisplayName("withCpuPoolSize() factory method")
    class WithCpuPoolSize {

        @Test
        @DisplayName("should create executor with specified pool size")
        void shouldCreateWithPoolSize() {
            executor = HybridExecutor.withCpuPoolSize(2);
            assertThat(executor).isNotNull();
        }

        @Test
        @DisplayName("should throw for zero pool size")
        void shouldThrowForZeroPoolSize() {
            assertThatThrownBy(() -> HybridExecutor.withCpuPoolSize(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw for negative pool size")
        void shouldThrowForNegativePoolSize() {
            assertThatThrownBy(() -> HybridExecutor.withCpuPoolSize(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("builder()")
    class BuilderTests {

        @Test
        @DisplayName("should create executor with builder defaults")
        void shouldCreateWithDefaults() {
            executor = HybridExecutor.builder().build();
            assertThat(executor).isNotNull();
        }

        @Test
        @DisplayName("should create executor with custom CPU pool size")
        void shouldAcceptCustomCpuPoolSize() {
            executor = HybridExecutor.builder().cpuPoolSize(4).build();
            assertThat(executor).isNotNull();
        }

        @Test
        @DisplayName("should accept custom thread name prefixes")
        void shouldAcceptCustomPrefixes() {
            executor = HybridExecutor.builder()
                    .cpuThreadNamePrefix("test-cpu-")
                    .ioThreadNamePrefix("test-io-")
                    .build();
            assertThat(executor).isNotNull();
        }

        @Test
        @DisplayName("should throw for null CPU thread name prefix")
        void shouldThrowForNullCpuPrefix() {
            assertThatThrownBy(() -> HybridExecutor.builder().cpuThreadNamePrefix(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw for null IO thread name prefix")
        void shouldThrowForNullIoPrefix() {
            assertThatThrownBy(() -> HybridExecutor.builder().ioThreadNamePrefix(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw for invalid CPU pool size via builder")
        void shouldThrowForInvalidPoolSize() {
            assertThatThrownBy(() -> HybridExecutor.builder().cpuPoolSize(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("execute(Runnable)")
    class ExecuteTests {

        @Test
        @DisplayName("should execute IO-bound task on IO pool")
        void shouldExecuteIoTask() throws InterruptedException {
            executor = HybridExecutor.create();
            CountDownLatch latch = new CountDownLatch(1);
            executor.execute(latch::countDown);
            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(executor.getIoSubmittedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should execute CpuBound task on CPU pool")
        void shouldExecuteCpuBoundTaskOnCpuPool() throws InterruptedException {
            executor = HybridExecutor.create();
            CountDownLatch latch = new CountDownLatch(1);
            CpuBound cpuTask = latch::countDown;
            executor.execute(cpuTask);
            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(executor.getCpuSubmittedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should throw for null task")
        void shouldThrowForNullTask() {
            executor = HybridExecutor.create();
            assertThatThrownBy(() -> executor.execute(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw after shutdown")
        void shouldThrowAfterShutdown() {
            executor = HybridExecutor.create();
            executor.shutdown();
            assertThatThrownBy(() -> executor.execute(() -> {}))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("executeOnCpuPool()")
    class ExecuteOnCpuPoolTests {

        @Test
        @DisplayName("should execute task on CPU pool")
        void shouldExecuteOnCpuPool() throws InterruptedException {
            executor = HybridExecutor.create();
            CountDownLatch latch = new CountDownLatch(1);
            executor.executeOnCpuPool(latch::countDown);
            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(executor.getCpuSubmittedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should throw for null task")
        void shouldThrowForNullTask() {
            executor = HybridExecutor.create();
            assertThatThrownBy(() -> executor.executeOnCpuPool(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("executeOnIoPool()")
    class ExecuteOnIoPoolTests {

        @Test
        @DisplayName("should execute task on IO pool")
        void shouldExecuteOnIoPool() throws InterruptedException {
            executor = HybridExecutor.create();
            CountDownLatch latch = new CountDownLatch(1);
            executor.executeOnIoPool(latch::countDown);
            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(executor.getIoSubmittedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should throw for null task")
        void shouldThrowForNullTask() {
            executor = HybridExecutor.create();
            assertThatThrownBy(() -> executor.executeOnIoPool(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("submit(Callable)")
    class SubmitTests {

        @Test
        @DisplayName("should submit IO-bound callable to IO pool")
        void shouldSubmitIoCallable() throws Exception {
            executor = HybridExecutor.create();
            CompletableFuture<String> future = executor.submit(() -> "result");
            assertThat(future.get(5, TimeUnit.SECONDS)).isEqualTo("result");
            assertThat(executor.getIoSubmittedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should throw for null callable")
        void shouldThrowForNullCallable() {
            executor = HybridExecutor.create();
            assertThatThrownBy(() -> executor.submit(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw after shutdown")
        void shouldThrowAfterShutdown() {
            executor = HybridExecutor.create();
            executor.shutdown();
            assertThatThrownBy(() -> executor.submit(() -> "x"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("submitOnCpuPool()")
    class SubmitOnCpuPoolTests {

        @Test
        @DisplayName("should submit callable to CPU pool and return result")
        void shouldSubmitToCpuPool() throws Exception {
            executor = HybridExecutor.create();
            CompletableFuture<Integer> future = executor.submitOnCpuPool(() -> 42);
            assertThat(future.get(5, TimeUnit.SECONDS)).isEqualTo(42);
            assertThat(executor.getCpuSubmittedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should throw for null callable")
        void shouldThrowForNullCallable() {
            executor = HybridExecutor.create();
            assertThatThrownBy(() -> executor.submitOnCpuPool(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("submitOnIoPool()")
    class SubmitOnIoPoolTests {

        @Test
        @DisplayName("should submit callable to IO pool and return result")
        void shouldSubmitToIoPool() throws Exception {
            executor = HybridExecutor.create();
            CompletableFuture<String> future = executor.submitOnIoPool(() -> "io-result");
            assertThat(future.get(5, TimeUnit.SECONDS)).isEqualTo("io-result");
            assertThat(executor.getIoSubmittedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should throw for null callable")
        void shouldThrowForNullCallable() {
            executor = HybridExecutor.create();
            assertThatThrownBy(() -> executor.submitOnIoPool(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("statistics tracking")
    class StatisticsTests {

        @Test
        @DisplayName("should track completed count")
        void shouldTrackCompleted() throws Exception {
            executor = HybridExecutor.create();
            CompletableFuture<String> f = executor.submitOnIoPool(() -> "ok");
            f.get(5, TimeUnit.SECONDS);
            // Allow time for whenComplete callback
            Thread.sleep(50);
            assertThat(executor.getCompletedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should track failed count for throwing callable")
        void shouldTrackFailed() throws Exception {
            executor = HybridExecutor.create();
            CompletableFuture<String> f = executor.submitOnIoPool(() -> {
                throw new RuntimeException("fail");
            });
            assertThatThrownBy(() -> f.get(5, TimeUnit.SECONDS)).isNotNull();
            // Allow time for whenComplete callback
            Thread.sleep(50);
            assertThat(executor.getFailedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should track failed count for throwing runnable")
        void shouldTrackFailedRunnable() throws InterruptedException {
            executor = HybridExecutor.create();
            CountDownLatch latch = new CountDownLatch(1);
            executor.executeOnIoPool(() -> {
                try {
                    throw new RuntimeException("fail");
                } finally {
                    latch.countDown();
                }
            });
            latch.await(5, TimeUnit.SECONDS);
            Thread.sleep(50);
            assertThat(executor.getFailedCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("lifecycle")
    class LifecycleTests {

        @Test
        @DisplayName("shutdown should set shutdown flag")
        void shutdownShouldSetFlag() {
            executor = HybridExecutor.create();
            executor.shutdown();
            assertThat(executor.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("shutdownNow should set shutdown flag")
        void shutdownNowShouldSetFlag() {
            executor = HybridExecutor.create();
            executor.shutdownNow();
            assertThat(executor.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("close should set shutdown flag")
        void closeShouldSetFlag() {
            executor = HybridExecutor.create();
            executor.close();
            assertThat(executor.isShutdown()).isTrue();
            executor = null; // prevent double close in tearDown
        }

        @Test
        @DisplayName("shutdownAndAwait should return true for idle executor")
        void shutdownAndAwaitShouldReturnTrue() throws InterruptedException {
            executor = HybridExecutor.create();
            boolean result = executor.shutdownAndAwait(Duration.ofSeconds(5));
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("shutdownAndAwait should wait for running tasks")
        void shutdownAndAwaitShouldWaitForTasks() throws Exception {
            executor = HybridExecutor.create();
            AtomicBoolean completed = new AtomicBoolean(false);
            executor.executeOnIoPool(() -> {
                try {
                    Thread.sleep(100);
                    completed.set(true);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            boolean result = executor.shutdownAndAwait(Duration.ofSeconds(5));
            assertThat(result).isTrue();
            assertThat(completed.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("auto-dispatch based on CpuBound marker")
    class AutoDispatchTests {

        @Test
        @DisplayName("regular Runnable should go to IO pool")
        void regularRunnableShouldGoToIo() throws InterruptedException {
            executor = HybridExecutor.create();
            CountDownLatch latch = new CountDownLatch(1);
            Runnable ioTask = latch::countDown;
            executor.execute(ioTask);
            latch.await(5, TimeUnit.SECONDS);
            assertThat(executor.getIoSubmittedCount()).isEqualTo(1);
            assertThat(executor.getCpuSubmittedCount()).isZero();
        }

        @Test
        @DisplayName("CpuBound Runnable should go to CPU pool")
        void cpuBoundRunnableShouldGoToCpu() throws InterruptedException {
            executor = HybridExecutor.create();
            CountDownLatch latch = new CountDownLatch(1);
            CpuBound cpuTask = latch::countDown;
            executor.execute(cpuTask);
            latch.await(5, TimeUnit.SECONDS);
            assertThat(executor.getCpuSubmittedCount()).isEqualTo(1);
            assertThat(executor.getIoSubmittedCount()).isZero();
        }
    }
}
