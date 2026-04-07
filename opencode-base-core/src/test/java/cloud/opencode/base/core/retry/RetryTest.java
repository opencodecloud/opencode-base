package cloud.opencode.base.core.retry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Retry")
class RetryTest {

    @Nested
    @DisplayName("BackoffStrategy")
    class BackoffStrategyTests {

        @Test
        void fixedReturnsConstantDelay() {
            var strategy = BackoffStrategy.fixed(Duration.ofMillis(200));
            assertThat(strategy.delay(1)).isEqualTo(Duration.ofMillis(200));
            assertThat(strategy.delay(2)).isEqualTo(Duration.ofMillis(200));
            assertThat(strategy.delay(5)).isEqualTo(Duration.ofMillis(200));
        }

        @Test
        void fixedRejectsNegativeDuration() {
            assertThatThrownBy(() -> BackoffStrategy.fixed(Duration.ofMillis(-1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        void fixedRejectsZeroDuration() {
            assertThatThrownBy(() -> BackoffStrategy.fixed(Duration.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        void fixedRejectsNullDuration() {
            assertThatThrownBy(() -> BackoffStrategy.fixed(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void exponentialGrows() {
            var strategy = BackoffStrategy.exponential(Duration.ofMillis(100), 2.0);
            assertThat(strategy.delay(1)).isEqualTo(Duration.ofMillis(100));
            assertThat(strategy.delay(2)).isEqualTo(Duration.ofMillis(200));
            assertThat(strategy.delay(3)).isEqualTo(Duration.ofMillis(400));
        }

        @Test
        void exponentialRejectsMultiplierBelowOne() {
            assertThatThrownBy(() -> BackoffStrategy.exponential(Duration.ofMillis(100), 0.5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("multiplier");
        }

        @Test
        void exponentialRejectsNegativeDelay() {
            assertThatThrownBy(() -> BackoffStrategy.exponential(Duration.ofMillis(-1), 2.0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void exponentialWithMultiplierOne() {
            var strategy = BackoffStrategy.exponential(Duration.ofMillis(100), 1.0);
            assertThat(strategy.delay(1)).isEqualTo(Duration.ofMillis(100));
            assertThat(strategy.delay(5)).isEqualTo(Duration.ofMillis(100));
        }

        @Test
        void jitterDelayIsWithinRange() {
            var strategy = BackoffStrategy.exponentialWithJitter(Duration.ofMillis(1000), 1.0, 0.5);
            for (int i = 0; i < 100; i++) {
                Duration d = strategy.delay(1);
                assertThat(d.toMillis()).isBetween(500L, 1500L);
            }
        }

        @Test
        void jitterRejectsNegativeFactor() {
            assertThatThrownBy(() -> BackoffStrategy.exponentialWithJitter(
                    Duration.ofMillis(100), 1.0, -0.1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("jitterFactor");
        }

        @Test
        void jitterRejectsFactorAboveOne() {
            assertThatThrownBy(() -> BackoffStrategy.exponentialWithJitter(
                    Duration.ofMillis(100), 1.0, 1.1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("jitterFactor");
        }

        @Test
        void jitterWithZeroFactorReturnsExactDelay() {
            var strategy = BackoffStrategy.exponentialWithJitter(Duration.ofMillis(500), 2.0, 0.0);
            assertThat(strategy.delay(1)).isEqualTo(Duration.ofMillis(500));
            assertThat(strategy.delay(2)).isEqualTo(Duration.ofMillis(1000));
        }

        @Test
        void fibonacciGrows() {
            var strategy = BackoffStrategy.fibonacci(Duration.ofMillis(100));
            assertThat(strategy.delay(1).toMillis()).isEqualTo(100);
            assertThat(strategy.delay(2).toMillis()).isEqualTo(100);
            assertThat(strategy.delay(3).toMillis()).isEqualTo(200);
            assertThat(strategy.delay(4).toMillis()).isEqualTo(300);
            assertThat(strategy.delay(5).toMillis()).isEqualTo(500);
        }

        @Test
        void fibonacciRejectsNegativeDelay() {
            assertThatThrownBy(() -> BackoffStrategy.fibonacci(Duration.ofMillis(-10)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void fibonacciRejectsZeroDelay() {
            assertThatThrownBy(() -> BackoffStrategy.fibonacci(Duration.ZERO))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void fibonacciSaturatesOnOverflow() {
            var strategy = BackoffStrategy.fibonacci(Duration.ofMillis(1));
            // Fibonacci overflows long around attempt 93 with 1ms base
            Duration d = strategy.delay(200);
            assertThat(d.toMillis()).isEqualTo(Long.MAX_VALUE);
        }

        @Test
        void exponentialSaturatesOnOverflow() {
            var strategy = BackoffStrategy.exponential(Duration.ofMillis(1000), 2.0);
            // 1000 * 2^200 would overflow
            Duration d = strategy.delay(200);
            assertThat(d.toMillis()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("RetryConfig")
    class RetryConfigTests {

        @Test
        void defaultConfigHasExpectedValues() {
            RetryConfig config = RetryConfig.DEFAULT;
            assertThat(config.maxAttempts()).isEqualTo(3);
            assertThat(config.backoff()).isInstanceOf(BackoffStrategy.Fixed.class);
            assertThat(config.maxDelay()).isNull();
            assertThat(config.retryOn()).isNotNull();
            assertThat(config.abortOn()).isNotNull();
            assertThat(config.onRetry()).isNotNull();
            // default abortOn should never abort
            assertThat(config.abortOn().test(new RuntimeException())).isFalse();
        }

        @Test
        void rejectsInvalidMaxAttempts() {
            assertThatThrownBy(() -> new RetryConfig(0, null, null, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("maxAttempts");
        }

        @Test
        void rejectsNegativeMaxAttempts() {
            assertThatThrownBy(() -> new RetryConfig(-1, null, null, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void nullsGetDefaults() {
            RetryConfig config = new RetryConfig(5, null, null, null, null, null);
            assertThat(config.maxAttempts()).isEqualTo(5);
            assertThat(config.backoff()).isNotNull();
            assertThat(config.retryOn()).isNotNull();
            assertThat(config.abortOn()).isNotNull();
            assertThat(config.onRetry()).isNotNull();
            // retryOn default should accept any exception
            assertThat(config.retryOn().test(new RuntimeException())).isTrue();
            // abortOn default should not abort
            assertThat(config.abortOn().test(new RuntimeException())).isFalse();
        }

        @Test
        void customValuesPreserved() {
            BackoffStrategy backoff = BackoffStrategy.exponential(Duration.ofMillis(50), 3.0);
            Duration maxDelay = Duration.ofSeconds(10);
            RetryConfig config = new RetryConfig(7, backoff, maxDelay,
                    IOException.class::isInstance, IllegalArgumentException.class::isInstance, (a, e) -> {});
            assertThat(config.maxAttempts()).isEqualTo(7);
            assertThat(config.backoff()).isSameAs(backoff);
            assertThat(config.maxDelay()).isEqualTo(maxDelay);
            assertThat(config.retryOn().test(new IOException())).isTrue();
            assertThat(config.abortOn().test(new IllegalArgumentException())).isTrue();
        }
    }

    @Nested
    @DisplayName("Retry execution")
    class ExecutionTests {

        @Test
        void succeedsOnFirstTry() {
            String result = Retry.of(() -> "hello").execute();
            assertThat(result).isEqualTo("hello");
        }

        @Test
        void retriesAndSucceeds() {
            AtomicInteger count = new AtomicInteger(0);
            String result = Retry.of(() -> {
                if (count.incrementAndGet() < 3) {
                    throw new RuntimeException("fail");
                }
                return "success";
            }).maxAttempts(5).delay(Duration.ofMillis(1)).execute();
            assertThat(result).isEqualTo("success");
            assertThat(count.get()).isEqualTo(3);
        }

        @Test
        void throwsAfterExhausted() {
            assertThatThrownBy(() -> Retry.of(() -> {
                throw new IOException("fail");
            }).maxAttempts(2).delay(Duration.ofMillis(1)).execute())
                    .isInstanceOf(RuntimeException.class)
                    .hasCauseInstanceOf(IOException.class)
                    .hasMessageContaining("Retry exhausted");
        }

        @Test
        void retryOnPredicateFilters() {
            AtomicInteger count = new AtomicInteger(0);
            assertThatThrownBy(() -> Retry.of(() -> {
                count.incrementAndGet();
                throw new IllegalArgumentException("bad");
            }).maxAttempts(3).delay(Duration.ofMillis(1))
                    .retryOn(IOException.class).execute());
            assertThat(count.get()).isEqualTo(1); // No retry - predicate didn't match
        }

        @Test
        void retryOnPredicateAllowsMatching() {
            AtomicInteger count = new AtomicInteger(0);
            assertThatThrownBy(() -> Retry.of(() -> {
                count.incrementAndGet();
                throw new IOException("io fail");
            }).maxAttempts(3).delay(Duration.ofMillis(1))
                    .retryOn(IOException.class).execute());
            assertThat(count.get()).isEqualTo(3); // All 3 attempts made
        }

        @Test
        void onRetryCallbackInvoked() {
            AtomicInteger retryCount = new AtomicInteger(0);
            Retry.of(() -> {
                if (retryCount.get() < 2) {
                    throw new RuntimeException("fail");
                }
                return "ok";
            }).maxAttempts(3).delay(Duration.ofMillis(1))
                    .onRetry((attempt, ex) -> retryCount.incrementAndGet())
                    .execute();
            assertThat(retryCount.get()).isEqualTo(2);
        }

        @Test
        void maxDelayCaps() {
            // Use exponential backoff that would produce large delays
            AtomicInteger count = new AtomicInteger(0);
            long start = System.nanoTime();
            assertThatThrownBy(() -> Retry.of(() -> {
                count.incrementAndGet();
                throw new RuntimeException("fail");
            }).maxAttempts(3)
                    .backoff(BackoffStrategy.exponential(Duration.ofSeconds(10), 2.0))
                    .maxDelay(Duration.ofMillis(1))
                    .execute());
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            // With maxDelay of 1ms for 2 sleeps, should complete quickly
            assertThat(elapsed).isLessThan(1000);
            assertThat(count.get()).isEqualTo(3);
        }

        @Test
        void staticExecuteWithDefaults() {
            String result = Retry.execute(() -> "data");
            assertThat(result).isEqualTo("data");
        }

        @Test
        void staticExecuteWithMaxAttempts() {
            AtomicInteger count = new AtomicInteger(0);
            String result = Retry.execute(() -> {
                if (count.incrementAndGet() < 2) {
                    throw new RuntimeException("fail");
                }
                return "done";
            }, 3);
            assertThat(result).isEqualTo("done");
            assertThat(count.get()).isEqualTo(2);
        }

        @Test
        void withConfigWorks() {
            RetryConfig config = new RetryConfig(
                    2, BackoffStrategy.fixed(Duration.ofMillis(1)), null, null, null, null);
            String result = Retry.withConfig(() -> "ok", config).execute();
            assertThat(result).isEqualTo("ok");
        }

        @Test
        void withConfigRetriesCorrectly() {
            AtomicInteger count = new AtomicInteger(0);
            RetryConfig config = new RetryConfig(
                    3, BackoffStrategy.fixed(Duration.ofMillis(1)), null, null, null, null);
            String result = Retry.withConfig(() -> {
                if (count.incrementAndGet() < 3) {
                    throw new RuntimeException("fail");
                }
                return "done";
            }, config).execute();
            assertThat(result).isEqualTo("done");
            assertThat(count.get()).isEqualTo(3);
        }

        @Test
        void rejectsNullTask() {
            assertThatThrownBy(() -> Retry.of(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("task");
        }

        @Test
        void rejectsNullConfig() {
            assertThatThrownBy(() -> Retry.withConfig(() -> "x", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("config");
        }

        @Test
        void runtimeExceptionRethrownDirectly() {
            IllegalStateException ex = new IllegalStateException("boom");
            assertThatThrownBy(() -> Retry.of(() -> {
                throw ex;
            }).maxAttempts(1).execute())
                    .isSameAs(ex);
        }

        @Test
        void errorRethrownDirectly() {
            OutOfMemoryError err = new OutOfMemoryError("oom");
            assertThatThrownBy(() -> Retry.of(() -> {
                throw err;
            }).maxAttempts(1).execute())
                    .isSameAs(err);
        }

        @Test
        void errorNeverRetried() {
            AtomicInteger count = new AtomicInteger(0);
            assertThatThrownBy(() -> Retry.of(() -> {
                count.incrementAndGet();
                throw new StackOverflowError("boom");
            }).maxAttempts(5).delay(Duration.ofMillis(1)).execute())
                    .isInstanceOf(StackOverflowError.class);
            assertThat(count.get()).isEqualTo(1);
        }

        @Test
        void checkedExceptionWrapped() {
            assertThatThrownBy(() -> Retry.of(() -> {
                throw new IOException("io");
            }).maxAttempts(1).execute())
                    .isInstanceOf(RuntimeException.class)
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        void rejectsMaxAttemptsLessThanOne() {
            assertThatThrownBy(() -> Retry.of(() -> "x").maxAttempts(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("maxAttempts");
        }

        @Test
        void rejectsNullBackoff() {
            assertThatThrownBy(() -> Retry.of(() -> "x").backoff(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void rejectsNullRetryOnPredicate() {
            assertThatThrownBy(() -> Retry.of(() -> "x").retryOn((java.util.function.Predicate<Throwable>) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void rejectsNullRetryOnClass() {
            assertThatThrownBy(() -> Retry.of(() -> "x").retryOn((Class<? extends Throwable>) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void rejectsNullOnRetryListener() {
            assertThatThrownBy(() -> Retry.of(() -> "x").onRetry(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void exponentialBackoffConvenience() {
            AtomicInteger count = new AtomicInteger(0);
            String result = Retry.of(() -> {
                if (count.incrementAndGet() < 2) {
                    throw new RuntimeException("fail");
                }
                return "ok";
            }).maxAttempts(3)
                    .exponentialBackoff(Duration.ofMillis(1), 2.0)
                    .execute();
            assertThat(result).isEqualTo("ok");
        }

        @Test
        void singleAttemptNoRetry() {
            AtomicInteger count = new AtomicInteger(0);
            assertThatThrownBy(() -> Retry.of(() -> {
                count.incrementAndGet();
                throw new RuntimeException("fail");
            }).maxAttempts(1).execute());
            assertThat(count.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("retryOnAny - multiple exception types | 多异常类型重试")
    class RetryOnAnyTests {

        @Test
        void retriesOnAnyMatchingType() {
            AtomicInteger count = new AtomicInteger(0);
            assertThatThrownBy(() -> Retry.of(() -> {
                count.incrementAndGet();
                throw new IOException("io");
            }).maxAttempts(3).delay(Duration.ofMillis(1))
                    .retryOnAny(IOException.class, TimeoutException.class)
                    .execute());
            assertThat(count.get()).isEqualTo(3);
        }

        @Test
        void retriesOnSecondType() {
            AtomicInteger count = new AtomicInteger(0);
            assertThatThrownBy(() -> Retry.of(() -> {
                count.incrementAndGet();
                throw new TimeoutException("timeout");
            }).maxAttempts(3).delay(Duration.ofMillis(1))
                    .retryOnAny(IOException.class, TimeoutException.class)
                    .execute());
            assertThat(count.get()).isEqualTo(3);
        }

        @Test
        void doesNotRetryOnNonMatchingType() {
            AtomicInteger count = new AtomicInteger(0);
            assertThatThrownBy(() -> Retry.of(() -> {
                count.incrementAndGet();
                throw new IllegalArgumentException("bad");
            }).maxAttempts(3).delay(Duration.ofMillis(1))
                    .retryOnAny(IOException.class, TimeoutException.class)
                    .execute());
            assertThat(count.get()).isEqualTo(1);
        }

        @Test
        void matchesSubclasses() {
            AtomicInteger count = new AtomicInteger(0);
            assertThatThrownBy(() -> Retry.of(() -> {
                count.incrementAndGet();
                throw new ConnectException("connect fail"); // subclass of IOException
            }).maxAttempts(2).delay(Duration.ofMillis(1))
                    .retryOnAny(IOException.class)
                    .execute());
            assertThat(count.get()).isEqualTo(2);
        }

        @Test
        void rejectsEmptyArray() {
            assertThatThrownBy(() -> Retry.of(() -> "x").retryOnAny())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least one");
        }

        @Test
        void rejectsNullArray() {
            assertThatThrownBy(() -> Retry.of(() -> "x").retryOnAny((Class<? extends Throwable>[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void rejectsNullElementInArray() {
            assertThatThrownBy(() -> Retry.of(() -> "x").retryOnAny(IOException.class, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("exception type");
        }
    }

    @Nested
    @DisplayName("abortOn / abortIf - abort retry | 中止重试")
    class AbortTests {

        @Test
        void abortOnStopsRetryImmediately() {
            AtomicInteger count = new AtomicInteger(0);
            assertThatThrownBy(() -> Retry.of(() -> {
                count.incrementAndGet();
                throw new IllegalArgumentException("bad input");
            }).maxAttempts(5).delay(Duration.ofMillis(1))
                    .abortOn(IllegalArgumentException.class)
                    .execute());
            assertThat(count.get()).isEqualTo(1);
        }

        @Test
        void abortOnTakesPrecedenceOverRetryOn() {
            AtomicInteger count = new AtomicInteger(0);
            assertThatThrownBy(() -> Retry.of(() -> {
                count.incrementAndGet();
                throw new IOException("io");
            }).maxAttempts(5).delay(Duration.ofMillis(1))
                    .retryOn(IOException.class)
                    .abortOn(IOException.class) // abort wins
                    .execute());
            assertThat(count.get()).isEqualTo(1);
        }

        @Test
        void abortIfWithPredicateStopsRetry() {
            AtomicInteger count = new AtomicInteger(0);
            assertThatThrownBy(() -> Retry.of(() -> {
                count.incrementAndGet();
                throw new RuntimeException("fatal: data corrupted");
            }).maxAttempts(5).delay(Duration.ofMillis(1))
                    .abortIf(ex -> ex.getMessage() != null && ex.getMessage().startsWith("fatal"))
                    .execute());
            assertThat(count.get()).isEqualTo(1);
        }

        @Test
        void nonMatchingAbortAllowsRetry() {
            AtomicInteger count = new AtomicInteger(0);
            assertThatThrownBy(() -> Retry.of(() -> {
                count.incrementAndGet();
                throw new IOException("transient");
            }).maxAttempts(3).delay(Duration.ofMillis(1))
                    .abortOn(IllegalArgumentException.class) // doesn't match IOException
                    .execute());
            assertThat(count.get()).isEqualTo(3);
        }

        @Test
        void abortOnMatchesSubclass() {
            AtomicInteger count = new AtomicInteger(0);
            assertThatThrownBy(() -> Retry.of(() -> {
                count.incrementAndGet();
                throw new ConnectException("refused"); // subclass of IOException
            }).maxAttempts(5).delay(Duration.ofMillis(1))
                    .abortOn(IOException.class)
                    .execute());
            assertThat(count.get()).isEqualTo(1);
        }

        @Test
        void rejectsNullAbortOnClass() {
            assertThatThrownBy(() -> Retry.of(() -> "x").abortOn(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void rejectsNullAbortIfPredicate() {
            assertThatThrownBy(() -> Retry.of(() -> "x").abortIf(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void withConfigAbortOnWorks() {
            AtomicInteger count = new AtomicInteger(0);
            RetryConfig config = new RetryConfig(5, BackoffStrategy.fixed(Duration.ofMillis(1)),
                    null, null, IllegalArgumentException.class::isInstance, null);
            assertThatThrownBy(() -> Retry.withConfig(() -> {
                count.incrementAndGet();
                throw new IllegalArgumentException("bad");
            }, config).execute());
            assertThat(count.get()).isEqualTo(1); // aborted immediately
        }
    }

    @Nested
    @DisplayName("Predicate exception isolation | 谓词异常隔离")
    class PredicateIsolationTests {

        @Test
        void faultyRetryOnPredicateDoesNotMaskTaskException() {
            AtomicInteger count = new AtomicInteger(0);
            assertThatThrownBy(() -> Retry.of(() -> {
                count.incrementAndGet();
                throw new IOException("task fail");
            }).maxAttempts(3).delay(Duration.ofMillis(1))
                    .retryOn(ex -> { throw new RuntimeException("predicate bug"); })
                    .execute())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Retry exhausted")
                    .hasCauseInstanceOf(IOException.class);
            assertThat(count.get()).isEqualTo(1);
        }

        @Test
        void faultyAbortOnPredicateDoesNotMaskTaskException() {
            AtomicInteger count = new AtomicInteger(0);
            assertThatThrownBy(() -> Retry.of(() -> {
                count.incrementAndGet();
                throw new RuntimeException("task fail");
            }).maxAttempts(3).delay(Duration.ofMillis(1))
                    .abortIf(ex -> { throw new RuntimeException("predicate bug"); })
                    .execute());
            // Should still retry since faulty abortOn defaults to false (no abort)
            assertThat(count.get()).isEqualTo(3);
        }
    }

    // ==================== retryOnResult ====================

    @Nested
    @DisplayName("retryOnResult - result-based retry | 基于结果的重试")
    class RetryOnResultTests {

        @Test
        void retriesWhenResultMatchesPredicate() {
            AtomicInteger count = new AtomicInteger(0);
            String result = Retry.of(() -> {
                int c = count.incrementAndGet();
                return c < 3 ? null : "found";
            }).retryOnResult(r -> r == null)
                    .maxAttempts(5).delay(Duration.ofMillis(1)).execute();
            assertThat(result).isEqualTo("found");
            assertThat(count.get()).isEqualTo(3);
        }

        @Test
        void returnsLastResultWhenExhausted() {
            AtomicInteger count = new AtomicInteger(0);
            String result = Retry.of(() -> {
                count.incrementAndGet();
                return "empty";
            }).retryOnResult("empty"::equals)
                    .maxAttempts(3).delay(Duration.ofMillis(1)).execute();
            assertThat(result).isEqualTo("empty");
            assertThat(count.get()).isEqualTo(3);
        }

        @Test
        void noRetryWhenResultSatisfies() {
            AtomicInteger count = new AtomicInteger(0);
            String result = Retry.of(() -> {
                count.incrementAndGet();
                return "ok";
            }).retryOnResult(r -> r == null)
                    .maxAttempts(5).delay(Duration.ofMillis(1)).execute();
            assertThat(result).isEqualTo("ok");
            assertThat(count.get()).isEqualTo(1);
        }

        @Test
        void onRetryCalledWithNullExceptionForResultRetry() {
            AtomicReference<Throwable> captured = new AtomicReference<>(new RuntimeException("sentinel"));
            Retry.of(() -> {
                return null;
            }).retryOnResult(r -> r == null)
                    .maxAttempts(2).delay(Duration.ofMillis(1))
                    .onRetry((attempt, ex) -> captured.set(ex))
                    .execute();
            assertThat(captured.get()).isNull();
        }

        @Test
        void rejectsNullPredicate() {
            assertThatThrownBy(() -> Retry.of(() -> "x").retryOnResult(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void faultyResultPredicateDoesNotCrash() {
            String result = Retry.of(() -> "ok")
                    .retryOnResult(r -> { throw new RuntimeException("predicate bug"); })
                    .maxAttempts(3).delay(Duration.ofMillis(1)).execute();
            assertThat(result).isEqualTo("ok");
        }
    }

    // ==================== onSuccess / onExhausted ====================

    @Nested
    @DisplayName("onSuccess / onExhausted - lifecycle callbacks | 生命周期回调")
    class LifecycleCallbackTests {

        @Test
        void onSuccessCalledOnSuccess() {
            AtomicReference<String> captured = new AtomicReference<>();
            Retry.of(() -> "hello")
                    .onSuccess(captured::set)
                    .execute();
            assertThat(captured.get()).isEqualTo("hello");
        }

        @Test
        void onSuccessCalledAfterRetryThenSuccess() {
            AtomicInteger count = new AtomicInteger(0);
            AtomicReference<String> captured = new AtomicReference<>();
            Retry.of(() -> {
                if (count.incrementAndGet() < 2) throw new RuntimeException("fail");
                return "recovered";
            }).maxAttempts(3).delay(Duration.ofMillis(1))
                    .onSuccess(captured::set)
                    .execute();
            assertThat(captured.get()).isEqualTo("recovered");
        }

        @Test
        void onSuccessNotCalledOnFailure() {
            AtomicReference<Object> captured = new AtomicReference<>();
            assertThatThrownBy(() -> Retry.of(() -> {
                throw new RuntimeException("fail");
            }).maxAttempts(1).onSuccess(captured::set).execute());
            assertThat(captured.get()).isNull();
        }

        @Test
        void onExhaustedCalledOnFailure() {
            AtomicReference<Throwable> captured = new AtomicReference<>();
            assertThatThrownBy(() -> Retry.of(() -> {
                throw new IOException("fail");
            }).maxAttempts(2).delay(Duration.ofMillis(1))
                    .onExhausted(captured::set).execute());
            assertThat(captured.get()).isInstanceOf(IOException.class);
        }

        @Test
        void onExhaustedNotCalledOnSuccess() {
            AtomicReference<Throwable> captured = new AtomicReference<>();
            Retry.of(() -> "ok")
                    .onExhausted(captured::set)
                    .execute();
            assertThat(captured.get()).isNull();
        }

        @Test
        void faultyOnSuccessDoesNotAffectResult() {
            String result = Retry.of(() -> "data")
                    .onSuccess(r -> { throw new RuntimeException("callback bug"); })
                    .execute();
            assertThat(result).isEqualTo("data");
        }

        @Test
        void faultyOnExhaustedDoesNotMaskException() {
            assertThatThrownBy(() -> Retry.of(() -> {
                throw new IOException("task fail");
            }).maxAttempts(1)
                    .onExhausted(ex -> { throw new RuntimeException("callback bug"); })
                    .execute())
                    .isInstanceOf(RuntimeException.class)
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        void rejectsNullOnSuccess() {
            assertThatThrownBy(() -> Retry.of(() -> "x").onSuccess(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void rejectsNullOnExhausted() {
            assertThatThrownBy(() -> Retry.of(() -> "x").onExhausted(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== timeout ====================

    @Nested
    @DisplayName("timeout - total timeout | 总超时")
    class TimeoutTests {

        @Test
        void timeoutStopsRetryLoop() {
            AtomicInteger count = new AtomicInteger(0);
            long start = System.nanoTime();
            assertThatThrownBy(() -> Retry.of(() -> {
                count.incrementAndGet();
                throw new RuntimeException("fail");
            }).maxAttempts(100)
                    .delay(Duration.ofMillis(50))
                    .timeout(Duration.ofMillis(150))
                    .execute());
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            assertThat(elapsed).isLessThan(500);
            assertThat(count.get()).isLessThan(100);
        }

        @Test
        void timeoutDoesNotAffectFastSuccess() {
            String result = Retry.of(() -> "fast")
                    .timeout(Duration.ofSeconds(10))
                    .execute();
            assertThat(result).isEqualTo("fast");
        }

        @Test
        void rejectsNullTimeout() {
            assertThatThrownBy(() -> Retry.of(() -> "x").timeout(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void rejectsZeroTimeout() {
            assertThatThrownBy(() -> Retry.of(() -> "x").timeout(Duration.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        void rejectsNegativeTimeout() {
            assertThatThrownBy(() -> Retry.of(() -> "x").timeout(Duration.ofMillis(-1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }
    }

    // ==================== executeAsync ====================

    @Nested
    @DisplayName("executeAsync - async retry | 异步重试")
    class AsyncTests {

        @Test
        void asyncSucceeds() throws Exception {
            CompletableFuture<String> future = Retry.of(() -> "async-ok")
                    .executeAsync();
            assertThat(future.get()).isEqualTo("async-ok");
        }

        @Test
        void asyncRetriesAndSucceeds() throws Exception {
            AtomicInteger count = new AtomicInteger(0);
            CompletableFuture<String> future = Retry.of(() -> {
                if (count.incrementAndGet() < 3) throw new RuntimeException("fail");
                return "recovered";
            }).maxAttempts(5).delay(Duration.ofMillis(1)).executeAsync();
            assertThat(future.get()).isEqualTo("recovered");
            assertThat(count.get()).isEqualTo(3);
        }

        @Test
        void asyncFailsAfterExhausted() {
            CompletableFuture<String> future = Retry.<String>of(() -> {
                throw new IOException("fail");
            }).maxAttempts(2).delay(Duration.ofMillis(1))
                    .executeAsync();
            assertThatThrownBy(future::get)
                    .hasCauseInstanceOf(RuntimeException.class);
        }

        @Test
        void asyncWithCustomExecutor() throws Exception {
            AtomicReference<String> threadName = new AtomicReference<>();
            CompletableFuture<String> future = Retry.of(() -> {
                threadName.set(Thread.currentThread().getName());
                return "done";
            }).executeAsync(r -> {
                Thread t = new Thread(r, "custom-executor");
                t.start();
            });
            assertThat(future.get()).isEqualTo("done");
            assertThat(threadName.get()).isEqualTo("custom-executor");
        }

        @Test
        void rejectsNullExecutor() {
            assertThatThrownBy(() -> Retry.of(() -> "x").executeAsync(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== RetryConfig backward compatibility ====================

    @Nested
    @DisplayName("RetryConfig v1.0.3 enhancements | RetryConfig 增强")
    class RetryConfigEnhancementTests {

        @Test
        void backwardCompatibleConstructorWorks() {
            RetryConfig config = new RetryConfig(
                    2, BackoffStrategy.fixed(Duration.ofMillis(1)), null, null, null, null);
            assertThat(config.maxAttempts()).isEqualTo(2);
            assertThat(config.timeout()).isNull();
            assertThat(config.onSuccess()).isNotNull();
            assertThat(config.onExhausted()).isNotNull();
        }

        @Test
        void fullConstructorWithAllFields() {
            AtomicReference<Object> successCapture = new AtomicReference<>();
            AtomicReference<Throwable> exhaustedCapture = new AtomicReference<>();
            RetryConfig config = new RetryConfig(
                    3, BackoffStrategy.fixed(Duration.ofMillis(1)),
                    Duration.ofSeconds(5), Duration.ofSeconds(30),
                    null, null, null, null,
                    r -> successCapture.set(r),
                    exhaustedCapture::set);
            assertThat(config.timeout()).isEqualTo(Duration.ofSeconds(30));
            config.onSuccess().accept("test");
            assertThat(successCapture.get()).isEqualTo("test");
        }

        @Test
        void withConfigAppliesNewFields() throws Exception {
            AtomicReference<Object> successCapture = new AtomicReference<>();
            RetryConfig config = new RetryConfig(
                    3, BackoffStrategy.fixed(Duration.ofMillis(1)),
                    null, Duration.ofSeconds(10),
                    null, null, null, null,
                    r -> successCapture.set(r), null);
            String result = Retry.withConfig(() -> "hello", config).execute();
            assertThat(result).isEqualTo("hello");
            assertThat(successCapture.get()).isEqualTo("hello");
        }

        @Test
        void withConfigAppliesRetryOnResult() {
            AtomicInteger count = new AtomicInteger(0);
            RetryConfig config = new RetryConfig(
                    5, BackoffStrategy.fixed(Duration.ofMillis(1)),
                    null, null, null, null,
                    r -> r == null, null, null, null);
            String result = Retry.withConfig(() -> {
                return count.incrementAndGet() < 3 ? null : "found";
            }, config).execute();
            assertThat(result).isEqualTo("found");
            assertThat(count.get()).isEqualTo(3);
        }

        @Test
        void rejectsInvalidTimeout() {
            assertThatThrownBy(() -> new RetryConfig(
                    3, null, null, Duration.ZERO, null, null, null, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("timeout");
        }
    }
}
