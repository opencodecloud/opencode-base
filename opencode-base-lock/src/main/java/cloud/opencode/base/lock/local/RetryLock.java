package cloud.opencode.base.lock.local;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.LockGuard;
import cloud.opencode.base.lock.exception.OpenLockAcquireException;
import cloud.opencode.base.lock.exception.OpenLockTimeoutException;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * Retry Lock Decorator with Exponential Backoff
 * 带指数退避的重试锁装饰器
 *
 * <p>A decorator that adds configurable retry logic with exponential backoff
 * to any {@link Lock} implementation. When lock acquisition fails with a
 * timeout, RetryLock automatically retries up to the configured number of
 * attempts, applying exponential backoff between retries.</p>
 * <p>一个装饰器，为任何 {@link Lock} 实现添加可配置的指数退避重试逻辑。
 * 当锁获取因超时失败时，RetryLock 会自动重试到配置的尝试次数，
 * 并在重试之间应用指数退避。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable max retries - 可配置最大重试次数</li>
 *   <li>Exponential backoff with configurable multiplier - 可配置倍数的指数退避</li>
 *   <li>Maximum delay cap to prevent unbounded waits - 最大延迟上限防止无限等待</li>
 *   <li>Virtual Thread friendly (uses Thread.sleep) - 虚拟线程友好（使用 Thread.sleep）</li>
 *   <li>Builder pattern for flexible configuration - 构建器模式灵活配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Wrap any lock with retry logic | 为任何锁添加重试逻辑
 * Lock<Long> base = new LocalLock();
 * RetryLock<Long> retryLock = RetryLock.builder(base)
 *     .maxRetries(5)
 *     .retryDelay(Duration.ofMillis(200))
 *     .backoffMultiplier(1.5)
 *     .maxDelay(Duration.ofSeconds(3))
 *     .build();
 *
 * // Use like any other lock | 像其他锁一样使用
 * try (var guard = retryLock.lock()) {
 *     // Critical section | 临界区
 * }
 *
 * // With defaults (3 retries, 100ms delay, 2x backoff, 5s max) | 使用默认值
 * RetryLock<Long> defaultRetry = new RetryLock<>(base);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Minimal overhead on first attempt success - 首次成功时开销极小</li>
 *   <li>Backoff prevents thundering herd on contention - 退避防止争用时的惊群效应</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (delegates to thread-safe lock) - 线程安全: 是（委托给线程安全锁）</li>
 *   <li>Virtual Thread friendly: Yes - 虚拟线程友好: 是</li>
 *   <li>Interrupt-aware: Yes - 中断感知: 是</li>
 * </ul>
 *
 * @param <T> the type of lock token | 锁令牌类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Lock
 * @see LocalLock
 * @since JDK 25, opencode-base-lock V1.0.3
 */
public class RetryLock<T> implements Lock<T> {

    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final Duration DEFAULT_RETRY_DELAY = Duration.ofMillis(100);
    private static final double DEFAULT_BACKOFF_MULTIPLIER = 2.0;
    private static final Duration DEFAULT_MAX_DELAY = Duration.ofSeconds(5);

    private final Lock<T> delegate;
    private final int maxRetries;
    private final Duration retryDelay;
    private final double backoffMultiplier;
    private final Duration maxDelay;

    /**
     * Creates a retry lock with default configuration
     * 使用默认配置创建重试锁
     *
     * <p>Defaults: 3 retries, 100ms initial delay, 2x backoff, 5s max delay.</p>
     * <p>默认值: 3次重试, 100ms初始延迟, 2倍退避, 5秒最大延迟。</p>
     *
     * @param delegate the underlying lock to decorate | 要装饰的底层锁
     * @throws NullPointerException if delegate is null | 如果delegate为null则抛出
     */
    public RetryLock(Lock<T> delegate) {
        this(delegate, DEFAULT_MAX_RETRIES, DEFAULT_RETRY_DELAY,
                DEFAULT_BACKOFF_MULTIPLIER, DEFAULT_MAX_DELAY);
    }

    /**
     * Creates a retry lock with custom configuration
     * 使用自定义配置创建重试锁
     *
     * @param delegate          the underlying lock to decorate | 要装饰的底层锁
     * @param maxRetries        maximum retry attempts (must be >= 0) | 最大重试次数（必须 >= 0）
     * @param retryDelay        initial delay between retries | 重试之间的初始延迟
     * @param backoffMultiplier backoff multiplier (must be >= 1.0) | 退避倍数（必须 >= 1.0）
     * @param maxDelay          maximum delay cap | 最大延迟上限
     * @throws NullPointerException     if any argument is null | 如果任何参数为null则抛出
     * @throws IllegalArgumentException if maxRetries < 0 or backoffMultiplier < 1.0 | 参数非法时抛出
     */
    public RetryLock(Lock<T> delegate, int maxRetries, Duration retryDelay,
                     double backoffMultiplier, Duration maxDelay) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be >= 0, got: " + maxRetries);
        }
        this.maxRetries = maxRetries;
        this.retryDelay = Objects.requireNonNull(retryDelay, "retryDelay must not be null");
        if (backoffMultiplier < 1.0) {
            throw new IllegalArgumentException(
                    "backoffMultiplier must be >= 1.0, got: " + backoffMultiplier);
        }
        this.backoffMultiplier = backoffMultiplier;
        this.maxDelay = Objects.requireNonNull(maxDelay, "maxDelay must not be null");
    }

    /**
     * Creates a builder for configuring a retry lock
     * 创建用于配置重试锁的构建器
     *
     * @param delegate the underlying lock to decorate | 要装饰的底层锁
     * @param <T>      the type of lock token | 锁令牌类型
     * @return a new builder instance | 新的构建器实例
     */
    public static <T> Builder<T> builder(Lock<T> delegate) {
        return new Builder<>(delegate);
    }

    @Override
    public LockGuard<T> lock() {
        long startNanos = System.nanoTime();
        long delayMillis = retryDelay.toMillis();

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            if (attempt > 0) {
                sleepBeforeRetry(delayMillis);
                delayMillis = nextDelay(delayMillis);
            }
            try {
                return delegate.lock();
            } catch (OpenLockTimeoutException ignored) {
            }
        }

        Duration totalElapsed = Duration.ofNanos(System.nanoTime() - startNanos);
        throw new OpenLockTimeoutException(
                "Failed to acquire lock after " + (maxRetries + 1) + " attempts"
                        + " (total elapsed: " + totalElapsed + ")",
                totalElapsed);
    }

    @Override
    public LockGuard<T> lock(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout must not be null");

        long startNanos = System.nanoTime();
        long timeoutNanos = timeout.toNanos();
        long deadline = safeDeadline(startNanos, timeoutNanos);
        long delayMillis = retryDelay.toMillis();

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            long remaining = deadline - System.nanoTime();
            if (attempt > 0) {
                if (remaining <= 0) {
                    break;
                }
                sleepBeforeRetry(Math.min(delayMillis, Duration.ofNanos(remaining).toMillis()));
                delayMillis = nextDelay(delayMillis);
                remaining = deadline - System.nanoTime();
                if (remaining <= 0) {
                    break;
                }
            }
            try {
                Duration attemptTimeout = Duration.ofNanos(Math.max(remaining, 0));
                return delegate.lock(attemptTimeout);
            } catch (OpenLockTimeoutException ignored) {
            }
        }

        Duration totalElapsed = Duration.ofNanos(System.nanoTime() - startNanos);
        throw new OpenLockTimeoutException(
                "Failed to acquire lock after " + (maxRetries + 1)
                        + " attempts within " + timeout
                        + " (total elapsed: " + totalElapsed + ")",
                totalElapsed);
    }

    @Override
    public boolean tryLock() {
        return delegate.tryLock();
    }

    @Override
    public boolean tryLock(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout must not be null");

        long now = System.nanoTime();
        long timeoutNanos = timeout.toNanos();
        long deadline = safeDeadline(now, timeoutNanos);
        long delayMillis = retryDelay.toMillis();

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            long remaining = deadline - System.nanoTime();
            if (attempt > 0) {
                if (remaining <= 0) {
                    return false;
                }
                sleepBeforeRetry(Math.min(delayMillis, Duration.ofNanos(remaining).toMillis()));
                delayMillis = nextDelay(delayMillis);
                remaining = deadline - System.nanoTime();
                if (remaining <= 0) {
                    return false;
                }
            }
            Duration attemptTimeout = Duration.ofNanos(Math.max(remaining, 0));
            if (delegate.tryLock(attemptTimeout)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public LockGuard<T> lockInterruptibly() throws InterruptedException {
        long startNanos = System.nanoTime();
        long delayMillis = retryDelay.toMillis();

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            if (Thread.interrupted()) {
                throw new InterruptedException("Interrupted before retry attempt " + attempt);
            }
            if (attempt > 0) {
                Thread.sleep(Duration.ofMillis(delayMillis));
                delayMillis = nextDelay(delayMillis);
            }
            try {
                return delegate.lockInterruptibly();
            } catch (OpenLockTimeoutException ignored) {
            }
        }

        Duration totalElapsed = Duration.ofNanos(System.nanoTime() - startNanos);
        throw new OpenLockTimeoutException(
                "Failed to acquire lock interruptibly after " + (maxRetries + 1)
                        + " attempts (total elapsed: " + totalElapsed + ")",
                totalElapsed);
    }

    @Override
    public void unlock() {
        delegate.unlock();
    }

    @Override
    public boolean isHeldByCurrentThread() {
        return delegate.isHeldByCurrentThread();
    }

    @Override
    public Optional<T> getToken() {
        return delegate.getToken();
    }

    /**
     * Gets the maximum number of retry attempts
     * 获取最大重试次数
     *
     * @return the max retries | 最大重试次数
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * Gets the initial retry delay
     * 获取初始重试延迟
     *
     * @return the retry delay | 重试延迟
     */
    public Duration getRetryDelay() {
        return retryDelay;
    }

    /**
     * Gets the backoff multiplier
     * 获取退避倍数
     *
     * @return the backoff multiplier | 退避倍数
     */
    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    /**
     * Gets the maximum delay cap
     * 获取最大延迟上限
     *
     * @return the max delay | 最大延迟
     */
    public Duration getMaxDelay() {
        return maxDelay;
    }

    /**
     * Gets the underlying delegate lock
     * 获取底层委托锁
     *
     * @return the delegate lock | 委托锁
     */
    public Lock<T> getDelegate() {
        return delegate;
    }

    /**
     * Calculates the next delay with exponential backoff, capped at maxDelay
     * 计算带指数退避的下一次延迟，上限为maxDelay
     */
    private long nextDelay(long currentDelayMillis) {
        long next = (long) (currentDelayMillis * backoffMultiplier);
        // Guard overflow: if multiplication overflows, cap at maxDelay
        if (next < 0 || next > maxDelay.toMillis()) {
            return maxDelay.toMillis();
        }
        return next;
    }

    /**
     * Sleeps before a retry attempt, translating InterruptedException to OpenLockAcquireException
     * 在重试之前睡眠，将InterruptedException转换为OpenLockAcquireException
     */
    private void sleepBeforeRetry(long delayMillis) {
        if (delayMillis <= 0) {
            return;
        }
        try {
            Thread.sleep(Duration.ofMillis(delayMillis));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpenLockAcquireException("Retry sleep interrupted", e);
        }
    }

    /**
     * Computes a safe deadline, guarding against nanoTime overflow
     * 计算安全的截止时间，防止nanoTime溢出
     */
    private static long safeDeadline(long now, long timeoutNanos) {
        return (Long.MAX_VALUE - now < timeoutNanos) ? Long.MAX_VALUE : now + timeoutNanos;
    }

    /**
     * Builder for RetryLock Configuration
     * RetryLock 配置构建器
     *
     * <p>Provides a fluent API for configuring retry behavior.</p>
     * <p>提供流式API用于配置重试行为。</p>
     *
     * @param <T> the type of lock token | 锁令牌类型
     */
    public static class Builder<T> {

        private final Lock<T> delegate;
        private int maxRetries = DEFAULT_MAX_RETRIES;
        private Duration retryDelay = DEFAULT_RETRY_DELAY;
        private double backoffMultiplier = DEFAULT_BACKOFF_MULTIPLIER;
        private Duration maxDelay = DEFAULT_MAX_DELAY;

        /**
         * Creates a builder for the given delegate lock
         * 为给定的委托锁创建构建器
         *
         * @param delegate the underlying lock | 底层锁
         */
        Builder(Lock<T> delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        }

        /**
         * Sets the maximum number of retry attempts
         * 设置最大重试次数
         *
         * @param maxRetries the max retries (>= 0) | 最大重试次数（>= 0）
         * @return this builder | 此构建器
         */
        public Builder<T> maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Sets the initial delay between retries
         * 设置重试之间的初始延迟
         *
         * @param retryDelay the initial delay | 初始延迟
         * @return this builder | 此构建器
         */
        public Builder<T> retryDelay(Duration retryDelay) {
            this.retryDelay = retryDelay;
            return this;
        }

        /**
         * Sets the backoff multiplier
         * 设置退避倍数
         *
         * @param backoffMultiplier the multiplier (>= 1.0) | 倍数（>= 1.0）
         * @return this builder | 此构建器
         */
        public Builder<T> backoffMultiplier(double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
            return this;
        }

        /**
         * Sets the maximum delay cap
         * 设置最大延迟上限
         *
         * @param maxDelay the max delay | 最大延迟
         * @return this builder | 此构建器
         */
        public Builder<T> maxDelay(Duration maxDelay) {
            this.maxDelay = maxDelay;
            return this;
        }

        /**
         * Builds the configured RetryLock
         * 构建配置好的 RetryLock
         *
         * @return a new RetryLock instance | 新的 RetryLock 实例
         */
        public RetryLock<T> build() {
            return new RetryLock<>(delegate, maxRetries, retryDelay, backoffMultiplier, maxDelay);
        }
    }
}
