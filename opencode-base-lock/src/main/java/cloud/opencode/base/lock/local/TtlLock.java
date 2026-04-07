package cloud.opencode.base.lock.local;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.LockGuard;
import cloud.opencode.base.lock.exception.OpenLockAcquireException;
import cloud.opencode.base.lock.exception.OpenLockTimeoutException;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Time-To-Live Lock with Automatic Expiry
 * 带自动过期的生存时间锁
 *
 * <p>A lock implementation that tracks holding duration and marks the lock as expired
 * after a configured TTL (Time-To-Live). When a lock holder exceeds the TTL,
 * the ownership tracking is cleared, and {@link #isExpired()} returns {@code true}.
 * The actual underlying lock release still depends on the holding thread calling
 * {@link #unlock()}. This serves as a monitoring and detection mechanism for
 * long-held locks.</p>
 * <p>一个跟踪持有时间并在配置的 TTL（生存时间）到期后将锁标记为过期的锁实现。
 * 当锁持有者超过 TTL 时，所有权跟踪被清除，{@link #isExpired()} 返回 {@code true}。
 * 底层锁的实际释放仍然取决于持有线程调用 {@link #unlock()}。
 * 这主要用作长期持有锁的监控和检测机制。</p>
 *
 * <p><strong>Behavior on TTL Expiry | TTL 过期时的行为:</strong></p>
 * <ul>
 *   <li>Ownership tracking ({@code ownerThread}, {@code acquireTimeNanos}) is atomically cleared -
 *       所有权跟踪被原子清除</li>
 *   <li>{@link #isExpired()} returns true, {@link #getRemainingTtl()} returns ZERO -
 *       isExpired() 返回 true，getRemainingTtl() 返回 ZERO</li>
 *   <li>The underlying ReentrantLock is NOT force-released (impossible from another thread) -
 *       底层 ReentrantLock 不会被强制释放（无法从其他线程释放）</li>
 *   <li>New acquirers still block until the old holder calls unlock() -
 *       新获取者仍然阻塞直到旧持有者调用 unlock()</li>
 * </ul>
 *
 * <p><strong>WARNING | 警告:</strong></p>
 * <p>TTL expiry is a detection mechanism, not a forced reclamation.
 * Applications should monitor {@link #isExpired()} and take corrective action
 * (e.g., interrupt the holding thread) rather than relying on automatic release.
 * Set TTL much longer than expected critical section duration.</p>
 * <p>TTL 过期是一种检测机制，而非强制回收。应用程序应监控 {@link #isExpired()}
 * 并采取纠正措施（如中断持有线程），而非依赖自动释放。
 * 应将 TTL 设置为远大于预期临界区持续时间的值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable TTL with automatic expiry detection - 可配置的 TTL 自动过期检测</li>
 *   <li>Ownership tracking cleared on expiry - 过期时清除所有权跟踪</li>
 *   <li>Remaining TTL query - 剩余 TTL 查询</li>
 *   <li>Fair/unfair lock modes - 公平/非公平锁模式</li>
 *   <li>Virtual Thread friendly - 虚拟线程友好</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a TTL lock with 30-second expiry | 创建30秒过期的TTL锁
 * TtlLock lock = new TtlLock(Duration.ofSeconds(30));
 *
 * // Use like any other lock | 像其他锁一样使用
 * try (var guard = lock.lock()) {
 *     // Critical section - must complete within 30s | 临界区 - 必须在30秒内完成
 *     processData();
 * }
 *
 * // Check remaining TTL | 检查剩余TTL
 * Duration remaining = lock.getRemainingTtl();
 * if (remaining.isZero()) {
 *     log.warn("Lock has expired!");
 * }
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>TTL checks use nanoTime for monotonic timing - TTL检查使用nanoTime单调计时</li>
 *   <li>Atomic operations for thread-safe state management - 原子操作保证线程安全状态管理</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Virtual Thread friendly: Yes - 虚拟线程友好: 是</li>
 *   <li>nanoTime overflow guard: Yes - nanoTime溢出保护: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Lock
 * @see LocalLock
 * @since JDK 25, opencode-base-lock V1.0.3
 */
public class TtlLock implements Lock<Long> {

    private final ReentrantLock jdkLock;
    private final Duration ttl;
    private final long ttlNanos;
    private final AtomicLong acquireTimeNanos = new AtomicLong(0);
    private final AtomicReference<Thread> ownerThread = new AtomicReference<>();
    private final AtomicLong tokenGenerator = new AtomicLong(0);
    private final ThreadLocal<Long> currentToken = new ThreadLocal<>();

    /**
     * Creates a TTL lock with the specified time-to-live and unfair ordering
     * 使用指定的生存时间和非公平排序创建TTL锁
     *
     * @param ttl the time-to-live duration for lock holding | 锁持有的生存时间
     * @throws NullPointerException     if ttl is null | 如果ttl为null则抛出
     * @throws IllegalArgumentException if ttl is non-positive | 如果ttl非正则抛出
     */
    public TtlLock(Duration ttl) {
        this(ttl, false);
    }

    /**
     * Creates a TTL lock with the specified time-to-live and fairness policy
     * 使用指定的生存时间和公平策略创建TTL锁
     *
     * @param ttl  the time-to-live duration for lock holding | 锁持有的生存时间
     * @param fair true for fair ordering, false for unfair | true为公平排序，false为非公平
     * @throws NullPointerException     if ttl is null | 如果ttl为null则抛出
     * @throws IllegalArgumentException if ttl is non-positive | 如果ttl非正则抛出
     */
    public TtlLock(Duration ttl, boolean fair) {
        Objects.requireNonNull(ttl, "ttl must not be null");
        if (ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("ttl must be positive, got: " + ttl);
        }
        this.ttl = ttl;
        this.ttlNanos = ttl.toNanos();
        this.jdkLock = new ReentrantLock(fair);
    }

    @Override
    public LockGuard<Long> lock() {
        tryForceReleaseIfExpired();
        jdkLock.lock();
        return recordAcquire();
    }

    @Override
    public LockGuard<Long> lock(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout must not be null");
        tryForceReleaseIfExpired();
        try {
            if (!jdkLock.tryLock(timeout.toNanos(), TimeUnit.NANOSECONDS)) {
                // Check again: maybe the holder expired during our wait
                tryForceReleaseIfExpired();
                if (!jdkLock.tryLock()) {
                    throw new OpenLockTimeoutException(
                            "Failed to acquire TTL lock within " + timeout, timeout);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpenLockAcquireException("TTL lock acquisition interrupted", e);
        }
        return recordAcquire();
    }

    @Override
    public boolean tryLock() {
        tryForceReleaseIfExpired();
        boolean acquired = jdkLock.tryLock();
        if (acquired) {
            recordAcquire();
        }
        return acquired;
    }

    @Override
    public boolean tryLock(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout must not be null");
        tryForceReleaseIfExpired();
        try {
            boolean acquired = jdkLock.tryLock(timeout.toNanos(), TimeUnit.NANOSECONDS);
            if (!acquired) {
                // Re-check TTL expiry after wait
                tryForceReleaseIfExpired();
                acquired = jdkLock.tryLock();
            }
            if (acquired) {
                recordAcquire();
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public LockGuard<Long> lockInterruptibly() throws InterruptedException {
        tryForceReleaseIfExpired();
        jdkLock.lockInterruptibly();
        return recordAcquire();
    }

    @Override
    public void unlock() {
        if (jdkLock.isHeldByCurrentThread()) {
            try {
                jdkLock.unlock();
                // Only clear TTL tracking when fully released (holdCount == 0)
                if (!jdkLock.isHeldByCurrentThread()) {
                    ownerThread.set(null);
                    acquireTimeNanos.set(0);
                }
            } finally {
                if (!jdkLock.isHeldByCurrentThread()) {
                    currentToken.remove();
                }
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
     * Checks if the current lock hold has expired beyond the TTL
     * 检查当前锁持有是否已超过TTL过期
     *
     * @return true if the lock is held and has exceeded the TTL | 如果锁被持有且已超过TTL则返回true
     */
    public boolean isExpired() {
        Thread owner = ownerThread.get();
        if (owner == null) {
            return false;
        }
        long acquireTime = acquireTimeNanos.get();
        if (acquireTime == 0) {
            return false;
        }
        long now = System.nanoTime();
        long expiryDeadline = safeDeadline(acquireTime, ttlNanos);
        return now - expiryDeadline >= 0;
    }

    /**
     * Gets the remaining TTL duration for the currently held lock
     * 获取当前持有锁的剩余TTL时间
     *
     * <p>Returns {@link Duration#ZERO} if the lock is not held or has expired.</p>
     * <p>如果锁未被持有或已过期则返回 {@link Duration#ZERO}。</p>
     *
     * @return the remaining TTL duration | 剩余TTL时间
     */
    public Duration getRemainingTtl() {
        Thread owner = ownerThread.get();
        if (owner == null) {
            return Duration.ZERO;
        }
        long acquireTime = acquireTimeNanos.get();
        if (acquireTime == 0) {
            return Duration.ZERO;
        }
        long now = System.nanoTime();
        long expiryDeadline = safeDeadline(acquireTime, ttlNanos);
        long remaining = expiryDeadline - now;
        if (remaining <= 0) {
            return Duration.ZERO;
        }
        return Duration.ofNanos(remaining);
    }

    /**
     * Gets the configured TTL duration
     * 获取配置的TTL时间
     *
     * @return the TTL duration | TTL时间
     */
    public Duration getTtl() {
        return ttl;
    }

    /**
     * Attempts to force-release the lock if the current holder has exceeded the TTL.
     * This is inherently unsafe - the original holder may still be in a critical section.
     * 如果当前持有者超过TTL则尝试强制释放锁。
     * 这本质上是不安全的 - 原始持有者可能仍在临界区中。
     */
    private void tryForceReleaseIfExpired() {
        Thread owner = ownerThread.get();
        if (owner == null) {
            return;
        }
        // Don't force-release our own thread's lock
        if (owner == Thread.currentThread()) {
            return;
        }
        long acquireTime = acquireTimeNanos.get();
        if (acquireTime == 0) {
            return;
        }
        long now = System.nanoTime();
        long expiryDeadline = safeDeadline(acquireTime, ttlNanos);
        if (now - expiryDeadline < 0) {
            // Not yet expired
            return;
        }
        // TTL expired - attempt to clear ownership tracking atomically
        if (ownerThread.compareAndSet(owner, null)) {
            // Use CAS to avoid wiping a new acquirer's freshly-set value
            acquireTimeNanos.compareAndSet(acquireTime, 0);
            // The jdkLock is still held by the expired owner thread.
            // We cannot force-release it from this thread.
            // The expired owner's eventual unlock() call will still release
            // jdkLock because unlock() checks jdkLock.isHeldByCurrentThread(),
            // not ownerThread. New acquirers block on jdkLock until the old
            // thread calls unlock() or terminates.
        }
    }

    /**
     * Records lock acquisition state and returns a LockGuard
     * 记录锁获取状态并返回LockGuard
     */
    private LockGuard<Long> recordAcquire() {
        // Only set TTL tracking on first (non-reentrant) acquisition
        if (jdkLock.getHoldCount() == 1) {
            acquireTimeNanos.set(System.nanoTime());
            ownerThread.set(Thread.currentThread());
        }
        long token = tokenGenerator.incrementAndGet();
        currentToken.set(token);
        return new LockGuard<>(this, token);
    }

    /**
     * Computes a safe deadline, guarding against nanoTime overflow
     * 计算安全的截止时间，防止nanoTime溢出
     */
    private static long safeDeadline(long now, long durationNanos) {
        return (Long.MAX_VALUE - now < durationNanos) ? Long.MAX_VALUE : now + durationNanos;
    }
}
