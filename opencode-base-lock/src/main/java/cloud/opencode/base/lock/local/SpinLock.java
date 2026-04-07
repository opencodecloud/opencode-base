package cloud.opencode.base.lock.local;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.LockConfig;
import cloud.opencode.base.lock.LockGuard;
import cloud.opencode.base.lock.LockType;
import cloud.opencode.base.lock.exception.OpenLockException;
import cloud.opencode.base.lock.exception.OpenLockTimeoutException;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Spin Lock Implementation for Short Critical Sections
 * 短临界区自旋锁实现
 *
 * <p>A spin lock implementation that busy-waits for the lock, suitable for
 * very short critical sections where lock contention is expected to be brief.</p>
 * <p>一种忙等待锁的自旋锁实现，适用于锁争用预期较短的极短临界区。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Optimized for nanosecond-level operations - 针对纳秒级操作优化</li>
 *   <li>Configurable spin count before yielding - 可配置让步前的自旋次数</li>
 *   <li>Optional reentrant support - 可选的可重入支持</li>
 *   <li>No OS-level context switching overhead - 无操作系统级上下文切换开销</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create spin lock | 创建自旋锁
 * SpinLock lock = new SpinLock();
 *
 * // Best for very short critical sections | 最适合极短临界区
 * lock.execute(() -> {
 *     counter.incrementAndGet();
 * });
 *
 * // Custom spin count | 自定义自旋次数
 * SpinLock customLock = new SpinLock(
 *     LockConfig.builder().spinCount(2000).build());
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Best for operations measured in nanoseconds - 最适合纳秒级操作</li>
 *   <li>Yields after max spin count to prevent CPU starvation - 达到最大自旋次数后让步以防止CPU饥饿</li>
 *   <li>For longer operations, use {@link LocalLock} - 对于较长操作，请使用LocalLock</li>
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
 * @see LocalLock
 * @see LockConfig
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public class SpinLock implements Lock<Long> {

    private final AtomicReference<Thread> owner = new AtomicReference<>();
    private final AtomicInteger holdCount = new AtomicInteger(0);
    private final AtomicLong tokenGenerator = new AtomicLong(0);
    private final ThreadLocal<Long> currentToken = new ThreadLocal<>();
    private final int maxSpinCount;
    private final LockConfig config;

    /**
     * Creates a spin lock with default configuration
     * 使用默认配置创建自旋锁
     */
    public SpinLock() {
        this(LockConfig.builder().lockType(LockType.SPIN).build());
    }

    /**
     * Creates a spin lock with specified configuration
     * 使用指定配置创建自旋锁
     *
     * @param config the lock configuration | 锁配置
     */
    public SpinLock(LockConfig config) {
        this.config = config;
        this.maxSpinCount = config.spinCount();
    }

    @Override
    public LockGuard<Long> lock() {
        return lock(config.defaultTimeout());
    }

    @Override
    public LockGuard<Long> lock(Duration timeout) {
        Thread currentThread = Thread.currentThread();
        long now = System.nanoTime();
        long timeoutNanos = timeout.toNanos();
        // Guard against nanoTime overflow
        long deadline = (Long.MAX_VALUE - now < timeoutNanos) ? Long.MAX_VALUE : now + timeoutNanos;

        // Check reentrant
        if (owner.get() == currentThread) {
            if (config.reentrant()) {
                holdCount.incrementAndGet();
                return new LockGuard<>(this, currentToken.get());
            } else {
                throw new OpenLockException("Lock is not reentrant");
            }
        }

        int spinCount = 0;
        while (!owner.compareAndSet(null, currentThread)) {
            if (System.nanoTime() - deadline >= 0) {
                throw new OpenLockTimeoutException(
                        "Failed to acquire spin lock within " + timeout, timeout);
            }

            spinCount++;
            if (spinCount > maxSpinCount) {
                // Yield after max spin count
                Thread.yield();
                spinCount = 0;
            }
        }

        holdCount.set(1);
        long token = tokenGenerator.incrementAndGet();
        currentToken.set(token);
        return new LockGuard<>(this, token);
    }

    @Override
    public boolean tryLock() {
        Thread currentThread = Thread.currentThread();

        if (owner.get() == currentThread && config.reentrant()) {
            holdCount.incrementAndGet();
            return true;
        }

        if (owner.compareAndSet(null, currentThread)) {
            holdCount.set(1);
            long token = tokenGenerator.incrementAndGet();
            currentToken.set(token);
            return true;
        }
        return false;
    }

    @Override
    public boolean tryLock(Duration timeout) {
        try {
            lock(timeout);
            return true;
        } catch (OpenLockTimeoutException e) {
            return false;
        }
    }

    @Override
    public LockGuard<Long> lockInterruptibly() throws InterruptedException {
        Thread currentThread = Thread.currentThread();

        if (owner.get() == currentThread && config.reentrant()) {
            holdCount.incrementAndGet();
            return new LockGuard<>(this, currentToken.get());
        }

        while (!owner.compareAndSet(null, currentThread)) {
            if (Thread.interrupted()) {
                throw new InterruptedException("Spin lock acquisition interrupted");
            }
            Thread.yield();
        }

        holdCount.set(1);
        long token = tokenGenerator.incrementAndGet();
        currentToken.set(token);
        return new LockGuard<>(this, token);
    }

    @Override
    public void unlock() {
        Thread currentThread = Thread.currentThread();
        if (owner.get() != currentThread) {
            throw new OpenLockException("Lock not held by current thread");
        }

        if (holdCount.decrementAndGet() == 0) {
            currentToken.remove();
            owner.set(null);
        }
    }

    @Override
    public boolean isHeldByCurrentThread() {
        return owner.get() == Thread.currentThread();
    }

    @Override
    public Optional<Long> getToken() {
        return Optional.ofNullable(currentToken.get());
    }

    /**
     * Gets the number of holds on this lock by the current thread
     * 获取当前线程对此锁的持有次数
     *
     * @return the hold count | 持有计数
     */
    public int getHoldCount() {
        return holdCount.get();
    }
}
