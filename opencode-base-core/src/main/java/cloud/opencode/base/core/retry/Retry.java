package cloud.opencode.base.core.retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Retry - General purpose retry utility with configurable backoff strategies
 * 重试工具 - 通用重试工具，支持可配置的退避策略
 *
 * <p>Provides a fluent builder API for configuring retry behavior including max attempts,
 * backoff strategy, retry predicates, and callbacks.</p>
 * <p>提供流式构建器 API 来配置重试行为，包括最大尝试次数、退避策略、重试谓词和回调。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple retry with defaults (3 attempts, 100ms fixed delay)
 * String data = Retry.execute(() -> fetchData());
 *
 * // Builder pattern
 * String result = Retry.of(() -> httpClient.get(url))
 *     .maxAttempts(5)
 *     .backoff(BackoffStrategy.exponential(Duration.ofMillis(200), 2.0))
 *     .retryOn(IOException.class)
 *     .onRetry((attempt, ex) -> log.warn("Retry #{}: {}", attempt, ex.getMessage()))
 *     .maxDelay(Duration.ofSeconds(30))
 *     .execute();
 *
 * // Result-based retry (retry when result is null)
 * String value = Retry.of(() -> cache.get(key))
 *     .retryOnResult(Objects::isNull)
 *     .maxAttempts(5)
 *     .execute();
 *
 * // Async retry
 * CompletableFuture<String> future = Retry.of(() -> fetchData())
 *     .maxAttempts(3)
 *     .executeAsync();
 *
 * // Timeout across all attempts
 * String data = Retry.of(() -> slowService.call())
 *     .timeout(Duration.ofSeconds(30))
 *     .maxAttempts(10)
 *     .execute();
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>Retry instances are NOT thread-safe. Create a new instance for each execution context.</p>
 * <p>Retry 实例不是线程安全的。请为每个执行上下文创建新实例。</p>
 *
 * @param <T> the result type - 结果类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.3
 */
public final class Retry<T> {

    private final Callable<T> task;
    private int maxAttempts = 3;
    private BackoffStrategy backoff = BackoffStrategy.fixed(Duration.ofMillis(100));
    private Duration maxDelay = null;
    private Duration timeout = null;
    private Predicate<Throwable> retryOn = ex -> true;
    private Predicate<Throwable> abortOn = ex -> false;
    private Predicate<T> retryOnResult = result -> false;
    private BiConsumer<Integer, Throwable> onRetry = (a, e) -> {};
    private Consumer<T> onSuccess = result -> {};
    private Consumer<Throwable> onExhausted = ex -> {};

    private Retry(Callable<T> task) {
        this.task = Objects.requireNonNull(task, "task must not be null");
    }

    /**
     * Create a new Retry builder for the given task.
     * 为给定任务创建新的 Retry 构建器。
     *
     * @param task the task to retry - 要重试的任务
     * @param <T>  the result type - 结果类型
     * @return a new Retry builder - 新的 Retry 构建器
     */
    public static <T> Retry<T> of(Callable<T> task) {
        return new Retry<>(task);
    }

    /**
     * Set the maximum number of attempts (including the initial attempt).
     * 设置最大尝试次数（包括初始尝试）。
     *
     * @param maxAttempts max attempts, must be &gt;= 1 - 最大尝试次数，必须 &gt;= 1
     * @return this builder - 当前构建器
     */
    public Retry<T> maxAttempts(int maxAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        this.maxAttempts = maxAttempts;
        return this;
    }

    /**
     * Set the backoff strategy.
     * 设置退避策略。
     *
     * @param backoff the backoff strategy - 退避策略
     * @return this builder - 当前构建器
     */
    public Retry<T> backoff(BackoffStrategy backoff) {
        this.backoff = Objects.requireNonNull(backoff, "backoff must not be null");
        return this;
    }

    /**
     * Set a fixed delay between retries (convenience for {@code backoff(BackoffStrategy.fixed(delay))}).
     * 设置重试之间的固定延迟（{@code backoff(BackoffStrategy.fixed(delay))} 的便捷方法）。
     *
     * @param delay the fixed delay - 固定延迟
     * @return this builder - 当前构建器
     */
    public Retry<T> delay(Duration delay) {
        this.backoff = BackoffStrategy.fixed(delay);
        return this;
    }

    /**
     * Set an exponential backoff strategy (convenience method).
     * 设置指数退避策略（便捷方法）。
     *
     * @param initialDelay the initial delay - 初始延迟
     * @param multiplier   the multiplier - 乘数
     * @return this builder - 当前构建器
     */
    public Retry<T> exponentialBackoff(Duration initialDelay, double multiplier) {
        this.backoff = BackoffStrategy.exponential(initialDelay, multiplier);
        return this;
    }

    /**
     * Set the maximum delay cap. If the computed delay exceeds this, it will be capped.
     * 设置最大延迟上限。如果计算的延迟超过此值，将被截断。
     *
     * @param maxDelay the maximum delay - 最大延迟
     * @return this builder - 当前构建器
     */
    public Retry<T> maxDelay(Duration maxDelay) {
        this.maxDelay = maxDelay;
        return this;
    }

    /**
     * Set a total timeout for all retry attempts combined. If the timeout is exceeded,
     * the retry loop stops and throws the last exception.
     * 设置所有重试尝试的总超时时间。如果超时，重试循环停止并抛出最后一个异常。
     *
     * @param timeout the total timeout duration - 总超时时间
     * @return this builder - 当前构建器
     */
    public Retry<T> timeout(Duration timeout) {
        this.timeout = Objects.requireNonNull(timeout, "timeout must not be null");
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        return this;
    }

    /**
     * Set a predicate to determine if a given exception should trigger a retry.
     * 设置谓词以确定给定异常是否应触发重试。
     *
     * @param predicate the retry predicate - 重试谓词
     * @return this builder - 当前构建器
     */
    public Retry<T> retryOn(Predicate<Throwable> predicate) {
        this.retryOn = Objects.requireNonNull(predicate, "predicate must not be null");
        return this;
    }

    /**
     * Set the exception type to retry on (convenience for {@code retryOn(type::isInstance)}).
     * 设置要重试的异常类型（{@code retryOn(type::isInstance)} 的便捷方法）。
     *
     * @param exceptionType the exception class to retry on - 要重试的异常类
     * @return this builder - 当前构建器
     */
    public Retry<T> retryOn(Class<? extends Throwable> exceptionType) {
        Objects.requireNonNull(exceptionType, "exceptionType must not be null");
        this.retryOn = exceptionType::isInstance;
        return this;
    }

    /**
     * Set multiple exception types to retry on. Retries if the exception is an instance of any of the given types.
     * 设置多个要重试的异常类型。如果异常是给定类型中的任意一个实例，则进行重试。
     *
     * @param exceptionTypes the exception classes to retry on - 要重试的异常类列表
     * @return this builder - 当前构建器
     */
    @SafeVarargs
    public final Retry<T> retryOnAny(Class<? extends Throwable>... exceptionTypes) {
        Objects.requireNonNull(exceptionTypes, "exceptionTypes must not be null");
        if (exceptionTypes.length == 0) {
            throw new IllegalArgumentException("at least one exception type must be specified");
        }
        for (Class<? extends Throwable> type : exceptionTypes) {
            Objects.requireNonNull(type, "exception type must not be null");
        }
        Class<? extends Throwable>[] copy = Arrays.copyOf(exceptionTypes, exceptionTypes.length);
        this.retryOn = ex -> {
            for (Class<? extends Throwable> type : copy) {
                if (type.isInstance(ex)) {
                    return true;
                }
            }
            return false;
        };
        return this;
    }

    /**
     * Set a predicate to retry based on the task result. When the result matches the predicate,
     * the task is retried even though no exception was thrown.
     * 设置基于结果的重试谓词。当结果匹配谓词时，即使未抛出异常也会重试。
     *
     * <p><strong>Usage | 用法:</strong></p>
     * <pre>{@code
     * // Retry when result is null
     * Retry.of(() -> cache.get(key))
     *     .retryOnResult(Objects::isNull)
     *     .execute();
     *
     * // Retry when list is empty
     * Retry.of(() -> query.list())
     *     .retryOnResult(List::isEmpty)
     *     .execute();
     * }</pre>
     *
     * @param predicate the result predicate - returns true when the result should trigger a retry
     *                  结果谓词 - 当结果应触发重试时返回 true
     * @return this builder - 当前构建器
     */
    public Retry<T> retryOnResult(Predicate<T> predicate) {
        this.retryOnResult = Objects.requireNonNull(predicate, "predicate must not be null");
        return this;
    }

    /**
     * Set a predicate to abort retry immediately when matched. Takes precedence over {@code retryOn}.
     * 设置谓词，匹配时立即中止重试。优先级高于 {@code retryOn}。
     *
     * @param predicate the abort predicate - 中止谓词
     * @return this builder - 当前构建器
     */
    public Retry<T> abortIf(Predicate<Throwable> predicate) {
        this.abortOn = Objects.requireNonNull(predicate, "predicate must not be null");
        return this;
    }

    /**
     * Set the exception type to abort retry immediately. Takes precedence over {@code retryOn}.
     * 设置遇到该异常类型时立即中止重试。优先级高于 {@code retryOn}。
     *
     * @param exceptionType the exception class to abort on - 要中止重试的异常类
     * @return this builder - 当前构建器
     */
    public Retry<T> abortOn(Class<? extends Throwable> exceptionType) {
        Objects.requireNonNull(exceptionType, "exceptionType must not be null");
        this.abortOn = exceptionType::isInstance;
        return this;
    }

    /**
     * Set a callback invoked before each retry attempt.
     * 设置在每次重试之前调用的回调。
     *
     * @param listener the callback receiving (attempt number, exception) - 接收（尝试次数，异常）的回调
     * @return this builder - 当前构建器
     */
    public Retry<T> onRetry(BiConsumer<Integer, Throwable> listener) {
        this.onRetry = Objects.requireNonNull(listener, "listener must not be null");
        return this;
    }

    /**
     * Set a callback invoked when the task succeeds.
     * 设置任务成功时调用的回调。
     *
     * <p>The callback receives the successful result value. Exceptions thrown by the callback
     * are isolated and do not affect the return value.</p>
     * <p>回调接收成功的结果值。回调抛出的异常会被隔离，不影响返回值。</p>
     *
     * @param listener the success callback - 成功回调
     * @return this builder - 当前构建器
     */
    public Retry<T> onSuccess(Consumer<T> listener) {
        this.onSuccess = Objects.requireNonNull(listener, "listener must not be null");
        return this;
    }

    /**
     * Set a callback invoked when all retry attempts are exhausted.
     * 设置所有重试尝试用尽时调用的回调。
     *
     * <p>The callback receives the last exception. Exceptions thrown by the callback
     * are isolated and do not mask the original failure.</p>
     * <p>回调接收最后一个异常。回调抛出的异常会被隔离，不会掩盖原始失败。</p>
     *
     * @param listener the exhausted callback - 用尽回调
     * @return this builder - 当前构建器
     */
    public Retry<T> onExhausted(Consumer<Throwable> listener) {
        this.onExhausted = Objects.requireNonNull(listener, "listener must not be null");
        return this;
    }

    /**
     * Execute the task with retry logic.
     * 使用重试逻辑执行任务。
     *
     * @return the task result - 任务结果
     * @throws RuntimeException if all attempts are exhausted or a non-retryable exception occurs -
     *                          如果所有尝试用尽或发生不可重试的异常
     */
    public T execute() {
        long deadlineNanos = timeout != null
                ? addNanosSaturating(System.nanoTime(), timeout.toNanos())
                : Long.MAX_VALUE;
        Throwable lastException = null;
        T lastResult = null;
        boolean resultRetry = false;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            // Check timeout before each attempt
            if (timeout != null && System.nanoTime() - deadlineNanos >= 0) {
                break;
            }
            try {
                T result = task.call();

                // Check result-based retry
                boolean shouldRetryResult;
                try {
                    shouldRetryResult = retryOnResult.test(result);
                } catch (RuntimeException predicateEx) {
                    shouldRetryResult = false;
                }

                if (shouldRetryResult) {
                    lastResult = result;
                    resultRetry = true;
                    if (attempt == maxAttempts) {
                        break; // exhausted — do not treat as success
                    }
                    // Check timeout before sleeping
                    if (timeout != null && System.nanoTime() - deadlineNanos >= 0) {
                        break;
                    }
                    try {
                        onRetry.accept(attempt, null);
                    } catch (RuntimeException ignored) {
                    }
                    sleep(backoff.delay(attempt), deadlineNanos);
                    continue;
                }

                // Success — result satisfied the predicate (or no result predicate set)
                try {
                    onSuccess.accept(result);
                } catch (RuntimeException ignored) {
                }
                return result;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Retry interrupted", e);
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                lastException = e;
                resultRetry = false;
                boolean shouldAbort;
                try {
                    shouldAbort = abortOn.test(e);
                } catch (RuntimeException predicateEx) {
                    shouldAbort = false;
                }
                boolean shouldRetry;
                try {
                    shouldRetry = retryOn.test(e);
                } catch (RuntimeException predicateEx) {
                    shouldRetry = false;
                }
                if (attempt == maxAttempts || shouldAbort || !shouldRetry) {
                    break;
                }
                // Check timeout before sleeping
                if (timeout != null && System.nanoTime() - deadlineNanos >= 0) {
                    break;
                }
                try {
                    onRetry.accept(attempt, e);
                } catch (RuntimeException ignored) {
                }
                sleep(backoff.delay(attempt), deadlineNanos);
            }
        }

        // Exhausted
        if (resultRetry) {
            // Last result didn't satisfy the predicate but no exception occurred
            try {
                onExhausted.accept(null);
            } catch (RuntimeException ignored) {
            }
            return lastResult;
        }

        try {
            onExhausted.accept(lastException);
        } catch (RuntimeException ignored) {
        }
        if (lastException instanceof RuntimeException re) {
            throw re;
        }
        throw new RuntimeException("Retry exhausted after " + maxAttempts + " attempts", lastException);
    }

    /**
     * Execute the task asynchronously with retry logic. Returns a {@link CompletableFuture}
     * that completes with the result or fails with the last exception.
     * 异步执行带重试逻辑的任务。返回 {@link CompletableFuture}，成功时完成结果，失败时携带最后一个异常。
     *
     * <p>Uses a virtual thread for the retry loop.</p>
     * <p>使用虚拟线程执行重试循环。</p>
     *
     * @return a CompletableFuture with the result - 包含结果的 CompletableFuture
     */
    public CompletableFuture<T> executeAsync() {
        ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();
        return CompletableFuture.supplyAsync(() -> execute(), exec)
                .whenComplete((r, t) -> exec.shutdown());
    }

    /**
     * Execute the task asynchronously with retry logic using the specified executor.
     * 使用指定的执行器异步执行带重试逻辑的任务。
     *
     * @param executor the executor to run the retry loop on - 执行重试循环的执行器
     * @return a CompletableFuture with the result - 包含结果的 CompletableFuture
     */
    public CompletableFuture<T> executeAsync(Executor executor) {
        Objects.requireNonNull(executor, "executor must not be null");
        return CompletableFuture.supplyAsync(() -> execute(), executor);
    }

    private void sleep(Duration delay, long deadlineNanos) {
        long sleepNanos = delay.toNanos();
        if (maxDelay != null) {
            long maxNanos = maxDelay.toNanos();
            if (sleepNanos > maxNanos) {
                sleepNanos = maxNanos;
            }
        }
        // Cap sleep to not exceed the deadline
        if (deadlineNanos != Long.MAX_VALUE) {
            long remainingNanos = deadlineNanos - System.nanoTime();
            if (remainingNanos <= 0) {
                return;
            }
            if (sleepNanos > remainingNanos) {
                sleepNanos = remainingNanos;
            }
        }
        try {
            Thread.sleep(Duration.ofNanos(sleepNanos));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry interrupted during backoff", e);
        }
    }

    private static long addNanosSaturating(long a, long b) {
        long result = a + b;
        if (((a ^ result) & (b ^ result)) < 0) {
            // Overflow detected: both operands same sign, result different sign
            return (a >= 0) ? Long.MAX_VALUE : Long.MIN_VALUE;
        }
        return result;
    }

    /**
     * Execute a task with default retry settings (3 attempts, 100ms fixed delay).
     * 使用默认重试设置执行任务（3次尝试，100毫秒固定延迟）。
     *
     * @param task the task to execute - 要执行的任务
     * @param <T>  the result type - 结果类型
     * @return the task result - 任务结果
     */
    public static <T> T execute(Callable<T> task) {
        return new Retry<>(task).execute();
    }

    /**
     * Execute a task with the specified max attempts and default backoff.
     * 使用指定的最大尝试次数和默认退避策略执行任务。
     *
     * @param task        the task to execute - 要执行的任务
     * @param maxAttempts the maximum number of attempts - 最大尝试次数
     * @param <T>         the result type - 结果类型
     * @return the task result - 任务结果
     */
    public static <T> T execute(Callable<T> task, int maxAttempts) {
        return new Retry<>(task).maxAttempts(maxAttempts).execute();
    }

    /**
     * Create a Retry instance from a {@link RetryConfig}.
     * 从 {@link RetryConfig} 创建 Retry 实例。
     *
     * @param task   the task to execute - 要执行的任务
     * @param config the retry configuration - 重试配置
     * @param <T>    the result type - 结果类型
     * @return a configured Retry instance - 已配置的 Retry 实例
     */
    public static <T> Retry<T> withConfig(Callable<T> task, RetryConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        Retry<T> retry = new Retry<>(task);
        retry.maxAttempts = config.maxAttempts();
        retry.backoff = config.backoff();
        retry.maxDelay = config.maxDelay();
        retry.timeout = config.timeout();
        retry.retryOn = config.retryOn();
        retry.abortOn = config.abortOn();
        @SuppressWarnings("unchecked")
        Predicate<T> resultPredicate = (Predicate<T>) (Predicate<?>) config.retryOnResult();
        retry.retryOnResult = resultPredicate;
        retry.onRetry = config.onRetry();
        @SuppressWarnings("unchecked")
        Consumer<T> successCallback = (Consumer<T>) (Consumer<?>) config.onSuccess();
        retry.onSuccess = successCallback;
        retry.onExhausted = config.onExhausted();
        return retry;
    }
}
