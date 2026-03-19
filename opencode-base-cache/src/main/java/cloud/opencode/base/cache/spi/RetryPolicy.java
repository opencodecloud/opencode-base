package cloud.opencode.base.cache.spi;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/**
 * Retry Policy - Defines retry behavior for cache operations
 * 重试策略 - 定义缓存操作的重试行为
 *
 * <p>Provides various retry strategies including fixed delay, exponential backoff,
 * and custom implementations.</p>
 * <p>提供多种重试策略，包括固定延迟、指数退避和自定义实现。</p>
 *
 * <p><strong>Built-in Policies | 内置策略:</strong></p>
 * <ul>
 *   <li>{@link #noRetry()} - No retry | 不重试</li>
 *   <li>{@link #fixedDelay(int, Duration)} - Fixed delay between retries | 固定延迟重试</li>
 *   <li>{@link #exponentialBackoff(int, Duration, Duration)} - Exponential backoff | 指数退避</li>
 *   <li>{@link #exponentialBackoffWithJitter(int, Duration, Duration)} - With random jitter | 带随机抖动</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // No retry
 * RetryPolicy noRetry = RetryPolicy.noRetry();
 *
 * // Fixed delay: 3 retries, 100ms between each
 * RetryPolicy fixed = RetryPolicy.fixedDelay(3, Duration.ofMillis(100));
 *
 * // Exponential backoff: 5 retries, 100ms initial, 10s max
 * RetryPolicy exponential = RetryPolicy.exponentialBackoff(5,
 *     Duration.ofMillis(100), Duration.ofSeconds(10));
 *
 * // With jitter to prevent thundering herd
 * RetryPolicy withJitter = RetryPolicy.exponentialBackoffWithJitter(5,
 *     Duration.ofMillis(100), Duration.ofSeconds(10));
 *
 * // Custom: only retry on specific exceptions
 * RetryPolicy custom = RetryPolicy.exponentialBackoff(3,
 *         Duration.ofMillis(100), Duration.ofSeconds(5))
 *     .retryOn(ex -> ex instanceof java.io.IOException);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fixed delay retry - 固定延迟重试</li>
 *   <li>Exponential backoff - 指数退避</li>
 *   <li>Jitter support - 抖动支持</li>
 *   <li>Exception-based filtering - 基于异常的过滤</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (sealed interface, immutable implementations) - 线程安全: 是（密封接口，不可变实现）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.9.0
 */
public sealed interface RetryPolicy permits
        RetryPolicy.NoRetry,
        RetryPolicy.FixedDelay,
        RetryPolicy.ExponentialBackoff,
        RetryPolicy.CustomRetryPolicy {

    /**
     * Get the maximum number of retry attempts
     * 获取最大重试次数
     *
     * @return max retries (0 means no retry) | 最大重试次数（0 表示不重试）
     */
    int maxRetries();

    /**
     * Calculate delay before the next retry attempt
     * 计算下次重试前的延迟
     *
     * @param attempt current attempt number (1-based) | 当前尝试次数（从 1 开始）
     * @return delay before retry | 重试前的延迟
     */
    Duration getDelay(int attempt);

    /**
     * Check if the exception should trigger a retry
     * 检查异常是否应触发重试
     *
     * @param exception the exception | 异常
     * @return true if should retry | 应重试返回 true
     */
    default boolean shouldRetry(Throwable exception) {
        return true;
    }

    /**
     * Create a policy that only retries on specific exceptions
     * 创建仅在特定异常时重试的策略
     *
     * @param predicate exception predicate | 异常判断条件
     * @return new policy with exception filter | 带异常过滤的新策略
     */
    default RetryPolicy retryOn(Predicate<Throwable> predicate) {
        return new CustomRetryPolicy(this, predicate);
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * No retry policy
     * 不重试策略
     *
     * @return no retry policy | 不重试策略
     */
    static RetryPolicy noRetry() {
        return NoRetry.INSTANCE;
    }

    /**
     * Fixed delay retry policy
     * 固定延迟重试策略
     *
     * @param maxRetries maximum retry attempts | 最大重试次数
     * @param delay      delay between retries | 重试间隔
     * @return fixed delay policy | 固定延迟策略
     */
    static RetryPolicy fixedDelay(int maxRetries, Duration delay) {
        return new FixedDelay(maxRetries, delay);
    }

    /**
     * Exponential backoff retry policy
     * 指数退避重试策略
     *
     * <p>Delay doubles after each attempt up to maxDelay.</p>
     * <p>每次尝试后延迟翻倍，直到 maxDelay。</p>
     *
     * @param maxRetries   maximum retry attempts | 最大重试次数
     * @param initialDelay initial delay | 初始延迟
     * @param maxDelay     maximum delay cap | 最大延迟上限
     * @return exponential backoff policy | 指数退避策略
     */
    static RetryPolicy exponentialBackoff(int maxRetries, Duration initialDelay, Duration maxDelay) {
        return new ExponentialBackoff(maxRetries, initialDelay, maxDelay, false);
    }

    /**
     * Exponential backoff with jitter
     * 带抖动的指数退避
     *
     * <p>Adds random jitter (0-50% of delay) to prevent thundering herd.</p>
     * <p>添加随机抖动（延迟的 0-50%）以防止惊群效应。</p>
     *
     * @param maxRetries   maximum retry attempts | 最大重试次数
     * @param initialDelay initial delay | 初始延迟
     * @param maxDelay     maximum delay cap | 最大延迟上限
     * @return policy with jitter | 带抖动的策略
     */
    static RetryPolicy exponentialBackoffWithJitter(int maxRetries, Duration initialDelay, Duration maxDelay) {
        return new ExponentialBackoff(maxRetries, initialDelay, maxDelay, true);
    }

    // ==================== Implementations | 实现类 ====================

    /**
     * No retry implementation
     */
    final class NoRetry implements RetryPolicy {
        static final NoRetry INSTANCE = new NoRetry();

        private NoRetry() {
        }

        @Override
        public int maxRetries() {
            return 0;
        }

        @Override
        public Duration getDelay(int attempt) {
            return Duration.ZERO;
        }
    }

    /**
     * Fixed delay implementation
     */
    final class FixedDelay implements RetryPolicy {
        private final int maxRetries;
        private final Duration delay;

        FixedDelay(int maxRetries, Duration delay) {
            if (maxRetries < 0) {
                throw new IllegalArgumentException("maxRetries must be >= 0");
            }
            this.maxRetries = maxRetries;
            this.delay = Objects.requireNonNull(delay, "delay cannot be null");
        }

        @Override
        public int maxRetries() {
            return maxRetries;
        }

        @Override
        public Duration getDelay(int attempt) {
            return delay;
        }
    }

    /**
     * Exponential backoff implementation
     */
    final class ExponentialBackoff implements RetryPolicy {
        private final int maxRetries;
        private final Duration initialDelay;
        private final Duration maxDelay;
        private final boolean withJitter;

        ExponentialBackoff(int maxRetries, Duration initialDelay, Duration maxDelay, boolean withJitter) {
            if (maxRetries < 0) {
                throw new IllegalArgumentException("maxRetries must be >= 0");
            }
            this.maxRetries = maxRetries;
            this.initialDelay = Objects.requireNonNull(initialDelay, "initialDelay cannot be null");
            this.maxDelay = Objects.requireNonNull(maxDelay, "maxDelay cannot be null");
            this.withJitter = withJitter;
        }

        @Override
        public int maxRetries() {
            return maxRetries;
        }

        @Override
        public Duration getDelay(int attempt) {
            // Calculate exponential delay: initialDelay * 2^(attempt-1)
            long delayMs = initialDelay.toMillis() * (1L << (attempt - 1));
            delayMs = Math.min(delayMs, maxDelay.toMillis());

            if (withJitter) {
                // Add jitter: 0-50% of the delay
                long jitter = (long) (delayMs * 0.5 * ThreadLocalRandom.current().nextDouble());
                delayMs += jitter;
            }

            return Duration.ofMillis(delayMs);
        }
    }

    /**
     * Custom retry policy implementation - wraps a delegate with custom exception filter
     * 自定义重试策略实现 - 用自定义异常过滤器包装委托
     */
    final class CustomRetryPolicy implements RetryPolicy {
        private final RetryPolicy delegate;
        private final Predicate<Throwable> predicate;

        CustomRetryPolicy(RetryPolicy delegate, Predicate<Throwable> predicate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
            this.predicate = Objects.requireNonNull(predicate, "predicate cannot be null");
        }

        @Override
        public int maxRetries() {
            return delegate.maxRetries();
        }

        @Override
        public Duration getDelay(int attempt) {
            return delegate.getDelay(attempt);
        }

        @Override
        public boolean shouldRetry(Throwable exception) {
            return predicate.test(exception);
        }
    }
}
