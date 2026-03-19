package cloud.opencode.base.lock.local;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.LockConfig;
import cloud.opencode.base.lock.LockGuard;
import cloud.opencode.base.lock.exception.OpenLockAcquireException;
import cloud.opencode.base.lock.exception.OpenLockTimeoutException;
import cloud.opencode.base.lock.metrics.DefaultLockMetrics;
import cloud.opencode.base.lock.metrics.LockMetrics;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Local Lock Implementation Based on JDK ReentrantLock
 * 基于JDK ReentrantLock的本地锁实现
 *
 * <p>A high-performance local lock implementation with Virtual Thread support,
 * avoiding synchronized keyword to prevent thread pinning.</p>
 * <p>高性能本地锁实现，支持虚拟线程，避免使用synchronized关键字以防止线程固定。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Virtual Thread friendly (no pinning) - 虚拟线程友好（无固定）</li>
 *   <li>Reentrant lock support - 支持可重入锁</li>
 *   <li>Fair/unfair lock modes - 公平/非公平锁模式</li>
 *   <li>Configurable timeout - 可配置超时</li>
 *   <li>Built-in metrics collection - 内置指标收集</li>
 *   <li>Try-with-resources support - 支持try-with-resources</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create lock with default configuration | 使用默认配置创建锁
 * LocalLock lock = new LocalLock();
 *
 * // Execute with automatic release | 自动释放的执行
 * lock.execute(() -> {
 *     // Critical section | 临界区
 * });
 *
 * // Try-with-resources pattern | try-with-resources模式
 * try (var guard = lock.lock()) {
 *     // Critical section | 临界区
 * }
 *
 * // With custom configuration | 使用自定义配置
 * LocalLock fairLock = new LocalLock(LockConfig.builder()
 *     .fair(true)
 *     .enableMetrics(true)
 *     .build());
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Low contention overhead - 低争用开销</li>
 *   <li>Metrics tracking with minimal impact - 最小影响的指标跟踪</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Virtual Thread friendly: Yes - 虚拟线程友好: 是</li>
 *   <li>Reentrant: Configurable - 可重入: 可配置</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Lock
 * @see LockConfig
 * @see LockGuard
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public class LocalLock implements Lock<Long> {

    private final ReentrantLock jdkLock;
    private final LockConfig config;
    private final DefaultLockMetrics metrics;
    private final AtomicLong tokenGenerator = new AtomicLong(0);
    private final ThreadLocal<Long> currentToken = new ThreadLocal<>();

    /**
     * Creates a local lock with default configuration
     * 使用默认配置创建本地锁
     */
    public LocalLock() {
        this(LockConfig.defaults());
    }

    /**
     * Creates a local lock with specified configuration
     * 使用指定配置创建本地锁
     *
     * @param config the lock configuration | 锁配置
     */
    public LocalLock(LockConfig config) {
        this.config = config;
        this.jdkLock = new ReentrantLock(config.fair());
        this.metrics = config.enableMetrics() ? new DefaultLockMetrics() : null;
    }

    @Override
    public LockGuard<Long> lock() {
        return lock(config.defaultTimeout());
    }

    @Override
    public LockGuard<Long> lock(Duration timeout) {
        long startTime = System.nanoTime();
        try {
            if (!jdkLock.tryLock(timeout.toNanos(), TimeUnit.NANOSECONDS)) {
                recordTimeout();
                throw new OpenLockTimeoutException(
                        "Failed to acquire lock within " + timeout, timeout);
            }
            Long token = tokenGenerator.incrementAndGet();
            currentToken.set(token);
            recordAcquire(System.nanoTime() - startTime);
            return new LockGuard<>(this, token);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpenLockAcquireException("Lock acquisition interrupted", e);
        }
    }

    @Override
    public boolean tryLock() {
        boolean acquired = jdkLock.tryLock();
        if (acquired) {
            Long token = tokenGenerator.incrementAndGet();
            currentToken.set(token);
            recordAcquire(0);
        }
        return acquired;
    }

    @Override
    public boolean tryLock(Duration timeout) {
        try {
            long startTime = System.nanoTime();
            boolean acquired = jdkLock.tryLock(timeout.toNanos(), TimeUnit.NANOSECONDS);
            if (acquired) {
                Long token = tokenGenerator.incrementAndGet();
                currentToken.set(token);
                recordAcquire(System.nanoTime() - startTime);
            } else {
                recordTimeout();
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public LockGuard<Long> lockInterruptibly() throws InterruptedException {
        long startTime = System.nanoTime();
        jdkLock.lockInterruptibly();
        Long token = tokenGenerator.incrementAndGet();
        currentToken.set(token);
        recordAcquire(System.nanoTime() - startTime);
        return new LockGuard<>(this, token);
    }

    @Override
    public void unlock() {
        if (jdkLock.isHeldByCurrentThread()) {
            try {
                jdkLock.unlock();
                recordRelease();
            } finally {
                currentToken.remove();
            }
        }
    }

    @Override
    public boolean isHeldByCurrentThread() {
        return jdkLock.isHeldByCurrentThread();
    }

    @Override
    public Optional<Long> getToken() {
        return Optional.ofNullable(currentToken.get());
    }

    /**
     * Gets the hold count for current thread
     * 获取当前线程的持有计数
     *
     * @return the number of holds on this lock | 此锁的持有次数
     */
    public int getHoldCount() {
        return jdkLock.getHoldCount();
    }

    /**
     * Checks if this lock uses fair ordering policy
     * 检查此锁是否使用公平排序策略
     *
     * @return true if fair | true表示公平锁
     */
    public boolean isFair() {
        return jdkLock.isFair();
    }

    /**
     * Checks if any threads are waiting to acquire this lock
     * 检查是否有线程正在等待获取此锁
     *
     * @return true if there are waiting threads | true表示有等待线程
     */
    public boolean hasQueuedThreads() {
        return jdkLock.hasQueuedThreads();
    }

    /**
     * Gets the estimated number of threads waiting to acquire this lock
     * 获取等待获取此锁的线程估计数量
     *
     * @return the number of waiting threads | 等待线程数
     */
    public int getQueueLength() {
        return jdkLock.getQueueLength();
    }

    /**
     * Gets the lock metrics if metrics collection is enabled
     * 如果启用了指标收集，则获取锁指标
     *
     * @return lock metrics, or empty if metrics disabled | 锁指标，如果禁用则为空
     */
    public Optional<LockMetrics> getMetrics() {
        return Optional.ofNullable(metrics);
    }

    private void recordAcquire(long waitNanos) {
        if (metrics != null) {
            metrics.recordAcquire(Duration.ofNanos(waitNanos));
        }
    }

    private void recordRelease() {
        if (metrics != null) {
            metrics.recordRelease();
        }
    }

    private void recordTimeout() {
        if (metrics != null) {
            metrics.recordTimeout();
        }
    }
}
