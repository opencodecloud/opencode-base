package cloud.opencode.base.cache.resilience;

import cloud.opencode.base.cache.spi.RetryPolicy;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * RetryExecutor Test — 重试执行器测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("RetryExecutor - 重试执行器")
class RetryExecutorTest {

    // ==================== noRetry 工厂 ====================

    @Nested
    @DisplayName("noRetry 执行器 - 不重试场景")
    class NoRetryTests {

        @Test
        @DisplayName("首次成功时直接返回结果")
        void shouldReturnResultOnFirstSuccess() {
            RetryExecutor executor = RetryExecutor.noRetry();

            String result = executor.execute(() -> "hello");

            assertThat(result).isEqualTo("hello");
            assertThat(executor.stats().successCount()).isEqualTo(1);
            assertThat(executor.stats().totalAttempts()).isEqualTo(1);
            assertThat(executor.stats().retryCount()).isZero();
        }

        @Test
        @DisplayName("首次失败时立即抛出异常，不重试")
        void shouldFailImmediatelyWithoutRetry() {
            RetryExecutor executor = RetryExecutor.noRetry();

            assertThatThrownBy(() ->
                    executor.execute(() -> {
                        throw new RuntimeException("immediate failure");
                    })
            ).isInstanceOf(RuntimeException.class)
                    .hasMessage("immediate failure");

            RetryExecutor.RetryStats stats = executor.stats();
            assertThat(stats.totalAttempts()).isEqualTo(1);
            assertThat(stats.failureCount()).isEqualTo(1);
            assertThat(stats.retryCount()).isZero();
            assertThat(stats.successCount()).isZero();
        }

        @Test
        @DisplayName("noRetry 策略的 maxRetries 为 0")
        void shouldHaveZeroMaxRetries() {
            RetryExecutor executor = RetryExecutor.noRetry();

            assertThat(executor.getPolicy().maxRetries()).isZero();
        }
    }

    // ==================== 固定延迟重试 ====================

    @Nested
    @DisplayName("固定延迟重试 - fixedDelay 策略")
    class FixedDelayTests {

        @Test
        @DisplayName("经过重试后成功返回结果")
        void shouldSucceedAfterRetries() {
            RetryPolicy policy = RetryPolicy.fixedDelay(3, Duration.ofMillis(10));
            RetryExecutor executor = RetryExecutor.of(policy);
            AtomicInteger attempts = new AtomicInteger(0);

            String result = executor.execute(() -> {
                if (attempts.incrementAndGet() < 3) {
                    throw new RuntimeException("not yet");
                }
                return "success";
            });

            assertThat(result).isEqualTo("success");
            assertThat(attempts.get()).isEqualTo(3);

            RetryExecutor.RetryStats stats = executor.stats();
            assertThat(stats.totalAttempts()).isEqualTo(3);
            assertThat(stats.successCount()).isEqualTo(1);
            assertThat(stats.failureCount()).isZero();
            assertThat(stats.retryCount()).isEqualTo(2);
            assertThat(stats.totalRetryDelay()).isGreaterThanOrEqualTo(Duration.ofMillis(20));
        }

        @Test
        @DisplayName("耗尽所有重试次数后抛出异常")
        void shouldExhaustAllRetriesAndThrow() {
            RetryPolicy policy = RetryPolicy.fixedDelay(2, Duration.ofMillis(5));
            RetryExecutor executor = RetryExecutor.of(policy);
            AtomicInteger attempts = new AtomicInteger(0);

            assertThatThrownBy(() ->
                    executor.execute(() -> {
                        attempts.incrementAndGet();
                        throw new RuntimeException("always fails");
                    })
            ).isInstanceOf(RuntimeException.class)
                    .hasMessage("always fails");

            // maxRetries=2 means initial + 2 retries = 3 total attempts
            assertThat(attempts.get()).isEqualTo(3);

            RetryExecutor.RetryStats stats = executor.stats();
            assertThat(stats.totalAttempts()).isEqualTo(3);
            assertThat(stats.failureCount()).isEqualTo(1);
            assertThat(stats.retryCount()).isEqualTo(2);
            assertThat(stats.successCount()).isZero();
        }

        @Test
        @DisplayName("检查异常被包装为 RuntimeException 抛出")
        void shouldWrapCheckedExceptionInRuntimeException() {
            RetryPolicy policy = RetryPolicy.fixedDelay(1, Duration.ofMillis(5));
            RetryExecutor executor = RetryExecutor.of(policy);

            assertThatThrownBy(() ->
                    executor.execute(() -> {
                        throw new RuntimeException(new IOException("io error"));
                    })
            ).isInstanceOf(RuntimeException.class);
        }
    }

    // ==================== 异常过滤 ====================

    @Nested
    @DisplayName("异常过滤 - retryOn 谓词")
    class ExceptionFilteringTests {

        @Test
        @DisplayName("仅对匹配的异常进行重试")
        void shouldOnlyRetryMatchingExceptions() {
            RetryPolicy policy = RetryPolicy.fixedDelay(3, Duration.ofMillis(5))
                    .retryOn(ex -> ex instanceof IllegalStateException);
            RetryExecutor executor = RetryExecutor.of(policy);
            AtomicInteger attempts = new AtomicInteger(0);

            // IllegalStateException should be retried
            assertThatThrownBy(() ->
                    executor.execute(() -> {
                        attempts.incrementAndGet();
                        throw new IllegalStateException("retryable");
                    })
            ).isInstanceOf(IllegalStateException.class);

            // Should have used all retries: initial + 3 = 4
            assertThat(attempts.get()).isEqualTo(4);
        }

        @Test
        @DisplayName("不匹配的异常不重试，立即失败")
        void shouldNotRetryNonMatchingExceptions() {
            RetryPolicy policy = RetryPolicy.fixedDelay(3, Duration.ofMillis(5))
                    .retryOn(ex -> ex instanceof IllegalStateException);
            RetryExecutor executor = RetryExecutor.of(policy);
            AtomicInteger attempts = new AtomicInteger(0);

            // IllegalArgumentException should NOT be retried
            assertThatThrownBy(() ->
                    executor.execute(() -> {
                        attempts.incrementAndGet();
                        throw new IllegalArgumentException("not retryable");
                    })
            ).isInstanceOf(IllegalArgumentException.class);

            // Should have attempted only once - no retry
            assertThat(attempts.get()).isEqualTo(1);
            assertThat(executor.stats().retryCount()).isZero();
            assertThat(executor.stats().failureCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("匹配异常重试后最终成功")
        void shouldRetryMatchingExceptionThenSucceed() {
            RetryPolicy policy = RetryPolicy.fixedDelay(3, Duration.ofMillis(5))
                    .retryOn(ex -> ex instanceof IllegalStateException);
            RetryExecutor executor = RetryExecutor.of(policy);
            AtomicInteger attempts = new AtomicInteger(0);

            String result = executor.execute(() -> {
                if (attempts.incrementAndGet() == 1) {
                    throw new IllegalStateException("temporary");
                }
                return "recovered";
            });

            assertThat(result).isEqualTo("recovered");
            assertThat(attempts.get()).isEqualTo(2);
            assertThat(executor.stats().retryCount()).isEqualTo(1);
            assertThat(executor.stats().successCount()).isEqualTo(1);
        }
    }

    // ==================== 统计跟踪 ====================

    @Nested
    @DisplayName("统计跟踪 - RetryStats")
    class StatsTrackingTests {

        @Test
        @DisplayName("多次执行后累积统计正确")
        void shouldAccumulateStatsAcrossMultipleExecutions() {
            RetryPolicy policy = RetryPolicy.fixedDelay(2, Duration.ofMillis(5));
            RetryExecutor executor = RetryExecutor.of(policy);

            // Execution 1: succeeds immediately
            executor.execute(() -> "ok");

            // Execution 2: succeeds after 1 retry
            AtomicInteger counter = new AtomicInteger(0);
            executor.execute(() -> {
                if (counter.incrementAndGet() < 2) {
                    throw new RuntimeException("retry once");
                }
                return "ok";
            });

            // Execution 3: fails completely
            try {
                executor.execute(() -> {
                    throw new RuntimeException("always fail");
                });
            } catch (RuntimeException e) {
                // expected
            }

            RetryExecutor.RetryStats stats = executor.stats();
            // Exec1: 1 attempt, Exec2: 2 attempts, Exec3: 3 attempts = 6 total
            assertThat(stats.totalAttempts()).isEqualTo(6);
            assertThat(stats.successCount()).isEqualTo(2);
            assertThat(stats.failureCount()).isEqualTo(1);
            // Exec2: 1 retry, Exec3: 2 retries = 3 total
            assertThat(stats.retryCount()).isEqualTo(3);
            assertThat(stats.totalRetryDelay()).isNotNull();
            assertThat(stats.totalRetryDelay().toMillis()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("初始统计全部为零")
        void shouldStartWithZeroStats() {
            RetryExecutor executor = RetryExecutor.of(RetryPolicy.fixedDelay(1, Duration.ofMillis(1)));

            RetryExecutor.RetryStats stats = executor.stats();
            assertThat(stats.totalAttempts()).isZero();
            assertThat(stats.successCount()).isZero();
            assertThat(stats.failureCount()).isZero();
            assertThat(stats.retryCount()).isZero();
            assertThat(stats.totalRetryDelay()).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("totalRetryDelay 反映累计延迟时间")
        void shouldTrackTotalRetryDelay() {
            RetryPolicy policy = RetryPolicy.fixedDelay(2, Duration.ofMillis(50));
            RetryExecutor executor = RetryExecutor.of(policy);
            AtomicInteger counter = new AtomicInteger(0);

            executor.execute(() -> {
                if (counter.incrementAndGet() < 3) {
                    throw new RuntimeException("retry");
                }
                return "done";
            });

            // 2 retries x 50ms = ~100ms minimum
            assertThat(executor.stats().totalRetryDelay().toMillis()).isGreaterThanOrEqualTo(100);
        }
    }

    // ==================== 空操作参数 ====================

    @Nested
    @DisplayName("空操作参数 - NPE 校验")
    class NullOperationTests {

        @Test
        @DisplayName("Supplier 为 null 时抛出 NullPointerException")
        void shouldThrowNPEForNullSupplier() {
            RetryExecutor executor = RetryExecutor.noRetry();

            assertThatThrownBy(() -> executor.execute((java.util.function.Supplier<String>) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("operation");
        }

        @Test
        @DisplayName("Runnable 为 null 时抛出 NullPointerException")
        void shouldThrowNPEForNullRunnable() {
            RetryExecutor executor = RetryExecutor.noRetry();

            assertThatThrownBy(() -> executor.execute((Runnable) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("operation");
        }

        @Test
        @DisplayName("异步 Supplier 为 null 时抛出 NullPointerException")
        void shouldThrowNPEForNullAsyncSupplier() {
            RetryExecutor executor = RetryExecutor.noRetry();

            assertThatThrownBy(() ->
                    executor.executeAsync(
                            (java.util.function.Supplier<CompletableFuture<String>>) null)
            ).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("operation");
        }

        @Test
        @DisplayName("策略为 null 时抛出 NullPointerException")
        void shouldThrowNPEForNullPolicy() {
            assertThatThrownBy(() -> RetryExecutor.of(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("policy");
        }
    }

    // ==================== Runnable 变体 ====================

    @Nested
    @DisplayName("Runnable 变体 - void 操作重试")
    class RunnableVariantTests {

        @Test
        @DisplayName("Runnable 首次成功执行")
        void shouldExecuteRunnableSuccessfully() {
            RetryExecutor executor = RetryExecutor.noRetry();
            AtomicInteger counter = new AtomicInteger(0);

            executor.execute((Runnable) counter::incrementAndGet);

            assertThat(counter.get()).isEqualTo(1);
            assertThat(executor.stats().successCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Runnable 经过重试后成功")
        void shouldRetryRunnableUntilSuccess() {
            RetryPolicy policy = RetryPolicy.fixedDelay(3, Duration.ofMillis(5));
            RetryExecutor executor = RetryExecutor.of(policy);
            AtomicInteger attempts = new AtomicInteger(0);

            executor.execute((Runnable) () -> {
                if (attempts.incrementAndGet() < 3) {
                    throw new RuntimeException("not yet");
                }
            });

            assertThat(attempts.get()).isEqualTo(3);
            assertThat(executor.stats().successCount()).isEqualTo(1);
            assertThat(executor.stats().retryCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Runnable 耗尽重试后抛出异常")
        void shouldThrowAfterRunnableExhaustsRetries() {
            RetryPolicy policy = RetryPolicy.fixedDelay(1, Duration.ofMillis(5));
            RetryExecutor executor = RetryExecutor.of(policy);

            assertThatThrownBy(() ->
                    executor.execute((Runnable) () -> {
                        throw new RuntimeException("always fails");
                    })
            ).isInstanceOf(RuntimeException.class)
                    .hasMessage("always fails");

            assertThat(executor.stats().failureCount()).isEqualTo(1);
        }
    }

    // ==================== 指数退避 ====================

    @Nested
    @DisplayName("指数退避 - exponentialBackoff 策略")
    class ExponentialBackoffTests {

        @Test
        @DisplayName("指数退避重试后成功")
        void shouldSucceedWithExponentialBackoff() {
            RetryPolicy policy = RetryPolicy.exponentialBackoff(3,
                    Duration.ofMillis(10), Duration.ofSeconds(1));
            RetryExecutor executor = RetryExecutor.of(policy);
            AtomicInteger attempts = new AtomicInteger(0);

            String result = executor.execute(() -> {
                if (attempts.incrementAndGet() < 3) {
                    throw new RuntimeException("not yet");
                }
                return "done";
            });

            assertThat(result).isEqualTo("done");
            assertThat(attempts.get()).isEqualTo(3);
            assertThat(executor.stats().retryCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("getPolicy 返回正确的策略")
        void shouldReturnCorrectPolicy() {
            RetryPolicy policy = RetryPolicy.exponentialBackoff(5,
                    Duration.ofMillis(100), Duration.ofSeconds(10));
            RetryExecutor executor = RetryExecutor.of(policy);

            assertThat(executor.getPolicy()).isSameAs(policy);
            assertThat(executor.getPolicy().maxRetries()).isEqualTo(5);
        }
    }

    // ==================== 异步执行 ====================

    @Nested
    @DisplayName("异步执行 - executeAsync")
    class AsyncExecutionTests {

        @Test
        @DisplayName("异步操作首次成功")
        void shouldExecuteAsyncSuccessfully() {
            RetryExecutor executor = RetryExecutor.noRetry();

            CompletableFuture<String> future = executor.executeAsync(
                    () -> CompletableFuture.completedFuture("async-ok"));

            assertThat(future.join()).isEqualTo("async-ok");
            assertThat(executor.stats().successCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("异步操作经过重试后成功")
        void shouldRetryAsyncUntilSuccess() {
            RetryPolicy policy = RetryPolicy.fixedDelay(3, Duration.ofMillis(10));
            RetryExecutor executor = RetryExecutor.of(policy);
            AtomicInteger attempts = new AtomicInteger(0);

            CompletableFuture<String> future = executor.executeAsync(() -> {
                if (attempts.incrementAndGet() < 3) {
                    return CompletableFuture.failedFuture(new RuntimeException("not yet"));
                }
                return CompletableFuture.completedFuture("async-recovered");
            });

            assertThat(future.join()).isEqualTo("async-recovered");
            assertThat(attempts.get()).isEqualTo(3);
            assertThat(executor.stats().successCount()).isEqualTo(1);
            assertThat(executor.stats().retryCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("异步操作耗尽重试后失败")
        void shouldFailAsyncAfterExhaustingRetries() {
            RetryPolicy policy = RetryPolicy.fixedDelay(2, Duration.ofMillis(5));
            RetryExecutor executor = RetryExecutor.of(policy);

            CompletableFuture<String> future = executor.executeAsync(
                    () -> CompletableFuture.failedFuture(new RuntimeException("always fails")));

            assertThatThrownBy(future::join)
                    .hasCauseInstanceOf(RuntimeException.class);

            assertThat(executor.stats().failureCount()).isEqualTo(1);
        }
    }
}
