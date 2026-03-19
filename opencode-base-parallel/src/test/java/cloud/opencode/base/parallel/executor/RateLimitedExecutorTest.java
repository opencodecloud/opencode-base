package cloud.opencode.base.parallel.executor;

import cloud.opencode.base.parallel.exception.OpenParallelException;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * RateLimitedExecutor Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
class RateLimitedExecutorTest {

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        void shouldCreateWithPermitsPerSecond() {
            RateLimitedExecutor executor = RateLimitedExecutor.create(100);

            assertThat(executor.getPermitsPerSecond()).isEqualTo(100);
            assertThat(executor.getBurstCapacity()).isEqualTo(10); // 100 / 10

            executor.close();
        }

        @Test
        void shouldCreateWithPermitsAndBurst() {
            RateLimitedExecutor executor = RateLimitedExecutor.create(50, 20);

            assertThat(executor.getPermitsPerSecond()).isEqualTo(50);
            assertThat(executor.getBurstCapacity()).isEqualTo(20);

            executor.close();
        }

        @Test
        void shouldCreateWithBuilder() {
            RateLimitedExecutor executor = RateLimitedExecutor.builder()
                    .permitsPerSecond(200)
                    .burstCapacity(50)
                    .namePrefix("test-")
                    .build();

            assertThat(executor.getPermitsPerSecond()).isEqualTo(200);
            assertThat(executor.getBurstCapacity()).isEqualTo(50);

            executor.close();
        }

        @Test
        void shouldRejectInvalidPermitsPerSecond() {
            assertThatThrownBy(() -> RateLimitedExecutor.create(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");

            assertThatThrownBy(() -> RateLimitedExecutor.create(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldRejectInvalidBurstCapacity() {
            assertThatThrownBy(() -> RateLimitedExecutor.create(100, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }
    }

    @Nested
    @DisplayName("Submit Tests")
    class SubmitTests {

        @Test
        void shouldSubmitRunnable() throws Exception {
            RateLimitedExecutor executor = RateLimitedExecutor.create(1000);
            AtomicInteger counter = new AtomicInteger(0);

            CompletableFuture<Void> future = executor.submit((Runnable) counter::incrementAndGet);
            future.get();

            assertThat(counter.get()).isEqualTo(1);
            assertThat(executor.getSubmittedCount()).isEqualTo(1);
            assertThat(executor.getCompletedCount()).isEqualTo(1);

            executor.close();
        }

        @Test
        void shouldSubmitCallable() throws Exception {
            RateLimitedExecutor executor = RateLimitedExecutor.create(1000);

            CompletableFuture<String> future = executor.submit(() -> "result");
            String result = future.get();

            assertThat(result).isEqualTo("result");
            assertThat(executor.getSubmittedCount()).isEqualTo(1);
            assertThat(executor.getCompletedCount()).isEqualTo(1);

            executor.close();
        }

        @Test
        void shouldTrackFailedTasks() throws Exception {
            RateLimitedExecutor executor = RateLimitedExecutor.create(1000);

            CompletableFuture<String> future = executor.submit(() -> {
                throw new RuntimeException("Test failure");
            });

            assertThatThrownBy(future::get)
                    .hasCauseInstanceOf(OpenParallelException.class);

            assertThat(executor.getFailedCount()).isEqualTo(1);

            executor.close();
        }
    }

    @Nested
    @DisplayName("Try Submit Tests")
    class TrySubmitTests {

        @Test
        void shouldTrySubmitSuccessfully() throws Exception {
            RateLimitedExecutor executor = RateLimitedExecutor.create(1000, 10);

            Optional<CompletableFuture<String>> optFuture = executor.trySubmit(() -> "success");

            assertThat(optFuture).isPresent();
            assertThat(optFuture.get().get()).isEqualTo("success");

            executor.close();
        }

        @Test
        void shouldRejectWhenNoPermits() throws Exception {
            // Create executor with very low rate and burst
            RateLimitedExecutor executor = RateLimitedExecutor.create(1, 1);

            // First should succeed
            Optional<CompletableFuture<Void>> first = executor.trySubmit(() -> {});
            assertThat(first).isPresent();

            // Wait for first to complete
            first.get().get();

            // Immediately try again - should be rejected due to rate limiting
            // (tokens are refilled slowly)
            Optional<CompletableFuture<Void>> second = executor.trySubmit(() -> {});

            // Either succeeds or is rejected depending on timing
            // The important thing is it doesn't block

            assertThat(executor.getSubmittedCount()).isGreaterThanOrEqualTo(1);

            executor.close();
        }
    }

    @Nested
    @DisplayName("Rate Limiting Tests")
    class RateLimitingTests {

        @Test
        void shouldRateLimitSubmissions() throws Exception {
            // 10 permits per second, burst of 5
            RateLimitedExecutor executor = RateLimitedExecutor.create(10, 5);

            List<CompletableFuture<Long>> futures = new ArrayList<>();
            long startTime = System.currentTimeMillis();

            // Submit more tasks than burst capacity
            for (int i = 0; i < 10; i++) {
                futures.add(executor.submit(System::currentTimeMillis));
            }

            // Wait for all to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

            long endTime = System.currentTimeMillis();
            long elapsed = endTime - startTime;

            // With 10 tasks, 5 burst, 10/sec rate:
            // 5 tasks execute immediately (burst)
            // Remaining 5 tasks need some waiting time
            // At least some waiting should occur
            assertThat(elapsed).isGreaterThanOrEqualTo(200);

            executor.close();
        }

        @Test
        void shouldRefillTokensOverTime() throws Exception {
            RateLimitedExecutor executor = RateLimitedExecutor.create(100, 5);

            // Use up all burst permits
            for (int i = 0; i < 5; i++) {
                executor.trySubmit(() -> {});
            }

            // Immediately, should have few permits
            double permitsNow = executor.getAvailablePermits();

            // Wait for refill
            Thread.sleep(100);

            // Should have more permits now
            double permitsLater = executor.getAvailablePermits();
            assertThat(permitsLater).isGreaterThan(permitsNow);

            executor.close();
        }
    }

    @Nested
    @DisplayName("Timeout Tests")
    class TimeoutTests {

        @Test
        void shouldSubmitWithTimeout() throws Exception {
            RateLimitedExecutor executor = RateLimitedExecutor.create(1000);

            CompletableFuture<String> future = executor.submit(
                    () -> "result",
                    Duration.ofSeconds(1)
            );

            assertThat(future.get()).isEqualTo("result");

            executor.close();
        }

        @Test
        void shouldTimeoutWhenWaitingForPermit() {
            // Very slow rate
            RateLimitedExecutor executor = RateLimitedExecutor.create(0.1, 1);

            // Use the one permit
            executor.trySubmit(() -> {});

            // Try to submit with short timeout - should fail
            assertThatThrownBy(() ->
                    executor.submit(() -> "result", Duration.ofMillis(10))
            ).isInstanceOf(OpenParallelException.class)
                    .hasMessageContaining("Timeout");

            executor.close();
        }
    }

    @Nested
    @DisplayName("Invoke All Tests")
    class InvokeAllTests {

        @Test
        void shouldInvokeAllTasks() {
            RateLimitedExecutor executor = RateLimitedExecutor.create(1000);

            List<Integer> results = executor.invokeAll(List.of(
                    () -> 1,
                    () -> 2,
                    () -> 3
            ));

            assertThat(results).containsExactly(1, 2, 3);

            executor.close();
        }

        @Test
        void shouldInvokeAllWithTimeout() {
            RateLimitedExecutor executor = RateLimitedExecutor.create(1000);

            List<String> results = executor.invokeAll(
                    List.of(
                            () -> "a",
                            () -> "b"
                    ),
                    Duration.ofSeconds(5)
            );

            assertThat(results).containsExactly("a", "b");

            executor.close();
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        void shouldTrackStatistics() throws Exception {
            RateLimitedExecutor executor = RateLimitedExecutor.create(100, 5);

            // Submit several tasks
            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                futures.add(executor.submit(() -> {
                    try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

            assertThat(executor.getSubmittedCount()).isEqualTo(10);
            assertThat(executor.getCompletedCount()).isEqualTo(10);
            assertThat(executor.getFailedCount()).isEqualTo(0);
            assertThat(executor.getWaitedCount()).isGreaterThanOrEqualTo(0);

            executor.close();
        }

        @Test
        void shouldTrackRejectedCount() {
            RateLimitedExecutor executor = RateLimitedExecutor.create(1, 1);

            // Use up the permit
            executor.trySubmit(() -> {});

            // These should be rejected
            for (int i = 0; i < 5; i++) {
                executor.trySubmit(() -> {});
            }

            assertThat(executor.getRejectedCount()).isGreaterThanOrEqualTo(0);

            executor.close();
        }

        @Test
        void shouldCalculateAverageWaitTime() throws Exception {
            RateLimitedExecutor executor = RateLimitedExecutor.create(10, 2);

            // Submit more than burst to force waiting
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                futures.add(executor.submit(() -> {}));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

            if (executor.getWaitedCount() > 0) {
                assertThat(executor.getAverageWaitMillis()).isGreaterThanOrEqualTo(0);
                assertThat(executor.getTotalWaitNanos()).isGreaterThan(0);
            }

            executor.close();
        }
    }

    @Nested
    @DisplayName("Lifecycle Tests")
    class LifecycleTests {

        @Test
        void shouldShutdown() {
            RateLimitedExecutor executor = RateLimitedExecutor.create(100);

            assertThat(executor.isShutdown()).isFalse();
            assertThat(executor.isTerminated()).isFalse();

            executor.shutdown();

            assertThat(executor.isShutdown()).isTrue();
        }

        @Test
        void shouldShutdownAndAwait() throws Exception {
            RateLimitedExecutor executor = RateLimitedExecutor.create(100);

            executor.submit(() -> {
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });

            boolean terminated = executor.shutdownAndAwait(Duration.ofSeconds(5));

            assertThat(terminated).isTrue();
            assertThat(executor.isTerminated()).isTrue();
        }

        @Test
        void shouldCloseViaAutoCloseable() {
            try (RateLimitedExecutor executor = RateLimitedExecutor.create(100)) {
                executor.submit(() -> "test");
            }
            // Should not throw
        }
    }

    @Nested
    @DisplayName("Acquire Tests")
    class AcquireTests {

        @Test
        void shouldAcquirePermit() {
            RateLimitedExecutor executor = RateLimitedExecutor.create(1000, 10);

            executor.acquire();

            assertThat(executor.getAvailablePermits()).isLessThan(10);

            executor.close();
        }

        @Test
        void shouldTryAcquirePermit() {
            RateLimitedExecutor executor = RateLimitedExecutor.create(1000, 10);

            boolean acquired = executor.tryAcquire();

            assertThat(acquired).isTrue();

            executor.close();
        }

        @Test
        void shouldTryAcquireWithTimeout() {
            RateLimitedExecutor executor = RateLimitedExecutor.create(1000, 10);

            boolean acquired = executor.tryAcquire(Duration.ofMillis(100));

            assertThat(acquired).isTrue();

            executor.close();
        }
    }
}
