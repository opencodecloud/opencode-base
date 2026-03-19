package cloud.opencode.base.cache.resilience;

import cloud.opencode.base.cache.spi.RetryPolicy;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Retry Executor — Executes operations with retry according to a {@link RetryPolicy}.
 * 重试执行器 — 根据 {@link RetryPolicy} 执行带重试的操作。
 *
 * <p>Complements the {@link RetryPolicy} strategy interface by providing a reusable
 * execution engine that applies the policy to arbitrary {@link Supplier}, {@link Runnable},
 * and async operations. Tracks cumulative statistics for observability.</p>
 * <p>通过提供可复用的执行引擎来补充 {@link RetryPolicy} 策略接口，
 * 该引擎将策略应用于任意 {@link Supplier}、{@link Runnable} 和异步操作。
 * 跟踪累积统计信息以供可观测性使用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Works with any {@link RetryPolicy} — fixed delay, exponential backoff, custom - 适用于任何 {@link RetryPolicy}</li>
 *   <li>Synchronous, void, and async execution modes - 同步、无返回值和异步执行模式</li>
 *   <li>Cumulative retry statistics (attempts, successes, failures, total delay) - 累积重试统计</li>
 *   <li>Virtual thread friendly (Thread.sleep does not pin carrier thread) - 对虚拟线程友好</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RetryPolicy policy = RetryPolicy.exponentialBackoff(3,
 *         Duration.ofMillis(100), Duration.ofSeconds(5));
 *
 * RetryExecutor executor = RetryExecutor.of(policy);
 *
 * // Execute a Supplier with retry
 * String value = executor.execute(() -> remoteCache.get("key"));
 *
 * // Execute a Runnable with retry
 * executor.execute(() -> remoteCache.put("key", "value"));
 *
 * // Execute async with retry
 * CompletableFuture<String> future = executor.executeAsync(
 *         () -> CompletableFuture.supplyAsync(() -> fetchFromRemote("key")));
 *
 * // Check statistics
 * RetryStats stats = executor.stats();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Lock-free statistics with AtomicLong counters - AtomicLong 无锁统计</li>
 *   <li>Thread.sleep for delays (virtual thread friendly) - Thread.sleep 对虚拟线程友好</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (AtomicLong counters, immutable policy) - 线程安全: 是</li>
 *   <li>Null-safe: No (operation must not be null) - 空值安全: 否（操作不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see RetryPolicy
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class RetryExecutor {

    private static final System.Logger LOGGER = System.getLogger(RetryExecutor.class.getName());

    /**
     * Snapshot of cumulative retry statistics.
     * 累积重试统计信息的快照。
     *
     * @param totalAttempts   total operation attempts (initial + retries) | 操作总尝试次数（初始 + 重试）
     * @param successCount    operations that eventually succeeded | 最终成功的操作数
     * @param failureCount    operations that exhausted all retries | 耗尽所有重试的操作数
     * @param retryCount      total retry attempts (excludes initial) | 重试尝试总数（不含初始）
     * @param totalRetryDelay cumulative delay waited between retries | 在重试之间等待的累计延迟
     */
    public record RetryStats(
            long totalAttempts,
            long successCount,
            long failureCount,
            long retryCount,
            Duration totalRetryDelay
    ) {}

    private final RetryPolicy policy;
    private final AtomicLong totalAttempts = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);
    private final AtomicLong retryCount = new AtomicLong(0);
    private final AtomicLong totalRetryDelayMillis = new AtomicLong(0);

    private RetryExecutor(RetryPolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy must not be null");
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a RetryExecutor backed by the given policy.
     * 使用给定策略创建 RetryExecutor。
     *
     * @param policy the retry policy | 重试策略
     * @return a new executor | 新的执行器
     */
    public static RetryExecutor of(RetryPolicy policy) {
        return new RetryExecutor(policy);
    }

    /**
     * Creates a RetryExecutor that never retries.
     * 创建从不重试的 RetryExecutor。
     *
     * @return a no-retry executor | 不重试的执行器
     */
    public static RetryExecutor noRetry() {
        return new RetryExecutor(RetryPolicy.noRetry());
    }

    // ==================== Execute Methods | 执行方法 ====================

    /**
     * Executes an operation with retry and returns its result.
     * 带重试地执行操作并返回结果。
     *
     * <p>Attempts up to {@code maxRetries + 1} times. Sleeps between retries according to
     * the policy's delay. Retries only if the exception passes {@link RetryPolicy#shouldRetry}.</p>
     * <p>最多尝试 {@code maxRetries + 1} 次。根据策略的延迟在重试之间休眠。
     * 仅当异常通过 {@link RetryPolicy#shouldRetry} 时才重试。</p>
     *
     * @param <T>       the return type | 返回类型
     * @param operation the operation to execute | 要执行的操作
     * @return the result of the first successful attempt | 第一次成功尝试的结果
     * @throws RuntimeException if all retries are exhausted | 所有重试耗尽时抛出
     */
    public <T> T execute(Supplier<T> operation) {
        Objects.requireNonNull(operation, "operation must not be null");

        Exception lastException = null;

        for (int attempt = 0; attempt <= policy.maxRetries(); attempt++) {
            totalAttempts.incrementAndGet();
            try {
                T result = operation.get();
                successCount.incrementAndGet();
                return result;
            } catch (Exception e) {
                lastException = e;

                if (attempt == policy.maxRetries()) {
                    break;
                }
                if (!policy.shouldRetry(e)) {
                    LOGGER.log(System.Logger.Level.DEBUG,
                            "Non-retryable exception on attempt {0}: {1}",
                            attempt + 1, e.getClass().getName());
                    break;
                }

                retryCount.incrementAndGet();
                Duration delay = policy.getDelay(attempt + 1);
                long sleepMillis = delay.toMillis();
                totalRetryDelayMillis.addAndGet(sleepMillis);

                LOGGER.log(System.Logger.Level.DEBUG,
                        "Retry attempt {0}/{1} after {2}ms, cause: {3}",
                        attempt + 1, policy.maxRetries(), sleepMillis, e.getMessage());

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry delay", ie);
                }
            }
        }

        failureCount.incrementAndGet();
        LOGGER.log(System.Logger.Level.WARNING,
                "All {0} retries exhausted, last error: {1}",
                policy.maxRetries(), lastException != null ? lastException.getMessage() : "unknown");

        throw lastException instanceof RuntimeException rte
                ? rte
                : new RuntimeException("Operation failed after " + (policy.maxRetries() + 1) + " attempts", lastException);
    }

    /**
     * Executes a void operation with retry.
     * 带重试地执行无返回值操作。
     *
     * @param operation the operation to execute | 要执行的操作
     * @throws RuntimeException if all retries are exhausted | 所有重试耗尽时抛出
     */
    public void execute(Runnable operation) {
        Objects.requireNonNull(operation, "operation must not be null");
        execute(() -> {
            operation.run();
            return null;
        });
    }

    /**
     * Executes an async operation with retry, returning a CompletableFuture.
     * 带重试地执行异步操作，返回 CompletableFuture。
     *
     * <p>On exceptional completion, retries after the policy delay using an async chain.
     * Tracks statistics across all async attempts.</p>
     * <p>异常完成时，通过异步链在策略延迟后重试。跟踪所有异步尝试的统计信息。</p>
     *
     * @param <T>       the return type | 返回类型
     * @param operation the async operation supplier | 异步操作 supplier
     * @return a future that resolves to the result | 解析为结果的 future
     */
    public <T> CompletableFuture<T> executeAsync(Supplier<CompletableFuture<T>> operation) {
        Objects.requireNonNull(operation, "operation must not be null");
        return executeAsyncInternal(operation, 0);
    }

    // ==================== Statistics | 统计 ====================

    /**
     * Returns a snapshot of the current retry statistics.
     * 返回当前重试统计信息的快照。
     *
     * @return the retry statistics | 重试统计信息
     */
    public RetryStats stats() {
        return new RetryStats(
                totalAttempts.get(),
                successCount.get(),
                failureCount.get(),
                retryCount.get(),
                Duration.ofMillis(totalRetryDelayMillis.get())
        );
    }

    /**
     * Returns the retry policy backing this executor.
     * 返回此执行器使用的重试策略。
     *
     * @return the retry policy | 重试策略
     */
    public RetryPolicy getPolicy() {
        return policy;
    }

    // ==================== Internal | 内部实现 ====================

    private <T> CompletableFuture<T> executeAsyncInternal(
            Supplier<CompletableFuture<T>> operation, int attempt) {

        totalAttempts.incrementAndGet();

        return operation.get().handle((result, throwable) -> {
            if (throwable == null) {
                successCount.incrementAndGet();
                return CompletableFuture.completedFuture(result);
            }

            Throwable cause = throwable instanceof java.util.concurrent.CompletionException ce
                    ? ce.getCause() : throwable;

            if (attempt >= policy.maxRetries() || !policy.shouldRetry(cause)) {
                failureCount.incrementAndGet();
                LOGGER.log(System.Logger.Level.WARNING,
                        "Async retry exhausted after {0} attempt(s): {1}",
                        attempt + 1, cause.getMessage());
                return CompletableFuture.<T>failedFuture(cause);
            }

            retryCount.incrementAndGet();
            Duration delay = policy.getDelay(attempt + 1);
            long sleepMillis = delay.toMillis();
            totalRetryDelayMillis.addAndGet(sleepMillis);

            LOGGER.log(System.Logger.Level.DEBUG,
                    "Async retry attempt {0}/{1} after {2}ms",
                    attempt + 1, policy.maxRetries(), sleepMillis);

            return CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during async retry delay", ie);
                }
            }).thenCompose(_ -> executeAsyncInternal(operation, attempt + 1));
        }).thenCompose(f -> f);
    }
}
