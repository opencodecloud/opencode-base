package cloud.opencode.base.parallel.executor;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Token Bucket Rate Limiter - Standalone non-blocking rate limiter.
 * 令牌桶限流器 - 独立的非阻塞限流器。
 *
 * <p>Implements a token bucket algorithm that limits the rate of operations.
 * Unlike {@link RateLimitedExecutor} which combines rate limiting with task execution,
 * this class is a pure rate limiter primitive suitable for use in any context —
 * including reactive pipelines, cache access guards, and API throttling.</p>
 * <p>实现令牌桶算法，限制操作速率。与将限流和任务执行结合在一起的 {@link RateLimitedExecutor} 不同，
 * 此类是纯粹的限流原语，适用于任何场景，包括响应式管道、缓存访问保护和 API 限流。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable permits-per-second rate - 可配置的每秒许可速率</li>
 *   <li>Non-blocking tryAcquire for single or multiple permits - 非阻塞的单个或多个许可获取</li>
 *   <li>Automatic time-based token refill - 基于时间的自动令牌补充</li>
 *   <li>Available permit count querying - 可用许可数量查询</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TokenBucketRateLimiter limiter = TokenBucketRateLimiter.of(1000.0); // 1000 permits/second
 *
 * if (limiter.tryAcquire()) {
 *     // proceed with operation
 * } else {
 *     // request throttled
 * }
 *
 * // Acquire multiple permits for batch operation
 * if (limiter.tryAcquire(5)) {
 *     // proceed with batch
 * }
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>AtomicLong CAS for lock-free permit consumption - AtomicLong CAS 用于无锁许可消耗</li>
 *   <li>Synchronized refill only when new tokens are available - 仅在有新令牌可用时同步补充</li>
 *   <li>O(1) for tryAcquire when permits are available - 许可可用时 tryAcquire 为 O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (AtomicLong + synchronized refill) - 线程安全: 是（AtomicLong + 同步补充）</li>
 *   <li>Null-safe: Yes (no nullable parameters) - 空值安全: 是（无可空参数）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see RateLimitedExecutor
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
public final class TokenBucketRateLimiter {

    private final double permitsPerSecond;
    private final long maxPermits;
    private final AtomicLong availablePermits;
    private volatile long lastRefillTime;
    private final Object refillLock = new Object();

    private TokenBucketRateLimiter(double permitsPerSecond) {
        if (permitsPerSecond <= 0) {
            throw new IllegalArgumentException(
                    "permitsPerSecond must be positive, got: " + permitsPerSecond);
        }
        this.permitsPerSecond = permitsPerSecond;
        this.maxPermits = Math.max(1, (long) permitsPerSecond);
        this.availablePermits = new AtomicLong(this.maxPermits);
        this.lastRefillTime = System.nanoTime();
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a rate limiter with the specified permits per second.
     * 创建具有指定每秒许可数的限流器。
     *
     * @param permitsPerSecond the maximum permits per second | 每秒最大许可数
     * @return a new rate limiter | 新的限流器
     * @throws IllegalArgumentException if permitsPerSecond is not positive | 如果 permitsPerSecond 不为正数
     */
    public static TokenBucketRateLimiter of(double permitsPerSecond) {
        return new TokenBucketRateLimiter(permitsPerSecond);
    }

    // ==================== Permit Operations | 许可操作 ====================

    /**
     * Tries to acquire a single permit without blocking.
     * 尝试获取单个许可，不阻塞。
     *
     * @return true if a permit was acquired, false if the rate limit is exceeded |
     *         如果获取到许可则返回 true，超出速率限制则返回 false
     */
    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    /**
     * Tries to acquire the specified number of permits without blocking.
     * 尝试获取指定数量的许可，不阻塞。
     *
     * @param permits the number of permits to acquire | 要获取的许可数量
     * @return true if all permits were acquired, false if the rate limit is exceeded |
     *         如果获取到所有许可则返回 true，超出速率限制则返回 false
     * @throws IllegalArgumentException if permits is not positive | 如果 permits 不为正数
     */
    public boolean tryAcquire(int permits) {
        if (permits <= 0) {
            throw new IllegalArgumentException("permits must be positive, got: " + permits);
        }
        refill();
        long current;
        do {
            current = availablePermits.get();
            if (current < permits) {
                return false;
            }
        } while (!availablePermits.compareAndSet(current, current - permits));
        return true;
    }

    // ==================== Metrics | 指标 ====================

    /**
     * Returns the current number of available permits (after refill).
     * 返回当前可用的许可数量（补充后）。
     *
     * @return the available permits | 可用的许可数量
     */
    public long availablePermits() {
        refill();
        return availablePermits.get();
    }

    /**
     * Returns the configured permits per second.
     * 返回配置的每秒许可数。
     *
     * @return the permits per second | 每秒许可数
     */
    public double getPermitsPerSecond() {
        return permitsPerSecond;
    }

    /**
     * Returns the maximum bucket capacity (equal to {@code permitsPerSecond} rounded down, minimum 1).
     * 返回最大桶容量（等于 {@code permitsPerSecond} 向下取整，最小为 1）。
     *
     * @return the maximum permits in the bucket | 桶中的最大许可数
     */
    public long getMaxPermits() {
        return maxPermits;
    }

    // ==================== Internal | 内部实现 ====================

    private void refill() {
        long now = System.nanoTime();
        long elapsed = now - lastRefillTime;
        if (elapsed <= 0) {
            return;
        }

        long newPermits = (long) (elapsed * permitsPerSecond / 1_000_000_000L);
        if (newPermits > 0) {
            synchronized (refillLock) {
                // Re-capture time inside lock to avoid using a stale timestamp.
                // 在锁内重新获取时间，避免使用锁外捕获的过时时间戳。
                long nowInLock = System.nanoTime();
                elapsed = nowInLock - lastRefillTime;
                newPermits = (long) (elapsed * permitsPerSecond / 1_000_000_000L);
                if (newPermits > 0) {
                    long current = availablePermits.get();
                    long updated = Math.min(maxPermits, current + newPermits);
                    availablePermits.set(updated);
                    lastRefillTime = nowInLock;
                }
            }
        }
    }

    @Override
    public String toString() {
        return "TokenBucketRateLimiter{permitsPerSecond=" + permitsPerSecond
                + ", available=" + availablePermits()
                + ", max=" + maxPermits + "}";
    }
}
