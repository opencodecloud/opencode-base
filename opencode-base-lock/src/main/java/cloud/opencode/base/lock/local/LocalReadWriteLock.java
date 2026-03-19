package cloud.opencode.base.lock.local;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.LockConfig;
import cloud.opencode.base.lock.LockGuard;
import cloud.opencode.base.lock.ReadWriteLock;
import cloud.opencode.base.lock.exception.OpenLockAcquireException;
import cloud.opencode.base.lock.exception.OpenLockTimeoutException;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Local Read-Write Lock Implementation Based on JDK ReentrantReadWriteLock
 * 基于JDK ReentrantReadWriteLock的本地读写锁实现
 *
 * <p>A read-write lock that allows multiple concurrent readers while
 * ensuring exclusive access for writers. Virtual Thread friendly.</p>
 * <p>允许多个并发读取者同时访问，同时确保写入者独占访问的读写锁。虚拟线程友好。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple concurrent readers - 多个并发读取者</li>
 *   <li>Exclusive writer access - 独占写入访问</li>
 *   <li>Fair/unfair lock modes - 公平/非公平锁模式</li>
 *   <li>Virtual Thread friendly - 虚拟线程友好</li>
 *   <li>Convenient execute methods - 便捷的执行方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create read-write lock | 创建读写锁
 * LocalReadWriteLock rwLock = new LocalReadWriteLock();
 *
 * // Read operation (multiple readers allowed) | 读操作（允许多个读取者）
 * String data = rwLock.executeRead(() -> loadData());
 *
 * // Write operation (exclusive access) | 写操作（独占访问）
 * rwLock.executeWrite(() -> saveData(newData));
 *
 * // Manual lock management | 手动锁管理
 * try (var guard = rwLock.readLock().lock()) {
 *     // Read operations | 读操作
 * }
 *
 * // Fair lock for preventing starvation | 防止饥饿的公平锁
 * LocalReadWriteLock fairLock = new LocalReadWriteLock(
 *     LockConfig.builder().fair(true).build());
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>High read throughput with concurrent readers - 并发读取时高读取吞吐量</li>
 *   <li>Write operations serialize access - 写操作序列化访问</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Virtual Thread friendly: Yes - 虚拟线程友好: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ReadWriteLock
 * @see Lock
 * @see LockConfig
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public class LocalReadWriteLock implements ReadWriteLock<Long> {

    private final ReentrantReadWriteLock jdkLock;
    private final Lock<Long> readLock;
    private final Lock<Long> writeLock;
    private final LockConfig config;

    /**
     * Creates a read-write lock with default configuration
     * 使用默认配置创建读写锁
     */
    public LocalReadWriteLock() {
        this(LockConfig.defaults());
    }

    /**
     * Creates a read-write lock with specified configuration
     * 使用指定配置创建读写锁
     *
     * @param config the lock configuration | 锁配置
     */
    public LocalReadWriteLock(LockConfig config) {
        this.config = config;
        this.jdkLock = new ReentrantReadWriteLock(config.fair());
        this.readLock = new LockAdapter(jdkLock.readLock(), config, "read");
        this.writeLock = new LockAdapter(jdkLock.writeLock(), config, "write");
    }

    @Override
    public Lock<Long> readLock() {
        return readLock;
    }

    @Override
    public Lock<Long> writeLock() {
        return writeLock;
    }

    /**
     * Gets the total number of read locks held
     * 获取持有的读锁总数
     *
     * @return the number of read holds | 读锁持有数
     */
    public int getReadLockCount() {
        return jdkLock.getReadLockCount();
    }

    /**
     * Gets the number of read locks held by the current thread
     * 获取当前线程持有的读锁数量
     *
     * @return the number of read holds by current thread | 当前线程读锁持有数
     */
    public int getReadHoldCount() {
        return jdkLock.getReadHoldCount();
    }

    /**
     * Checks if the write lock is held by any thread
     * 检查写锁是否被任何线程持有
     *
     * @return true if write lock is held | true表示写锁被持有
     */
    public boolean isWriteLocked() {
        return jdkLock.isWriteLocked();
    }

    /**
     * Checks if the write lock is held by the current thread
     * 检查当前线程是否持有写锁
     *
     * @return true if write lock is held by current thread | true表示当前线程持有写锁
     */
    public boolean isWriteLockedByCurrentThread() {
        return jdkLock.isWriteLockedByCurrentThread();
    }

    /**
     * Gets the number of write locks held by the current thread
     * 获取当前线程持有的写锁数量
     *
     * @return the number of write holds | 写锁持有数
     */
    public int getWriteHoldCount() {
        return jdkLock.getWriteHoldCount();
    }

    /**
     * Lock Adapter for Read/Write Locks
     * 读/写锁适配器
     *
     * <p>Internal adapter that wraps JDK lock to implement the Lock interface.</p>
     * <p>内部适配器，包装JDK锁以实现Lock接口。</p>
     */
    private static class LockAdapter implements Lock<Long> {
        private final java.util.concurrent.locks.Lock jdkLock;
        private final LockConfig config;
        private final String lockType;
        private final AtomicLong tokenGenerator = new AtomicLong(0);
        private final ThreadLocal<Long> currentToken = new ThreadLocal<>();
        private final ThreadLocal<Integer> holdCount = ThreadLocal.withInitial(() -> 0);

        LockAdapter(java.util.concurrent.locks.Lock jdkLock, LockConfig config, String lockType) {
            this.jdkLock = jdkLock;
            this.config = config;
            this.lockType = lockType;
        }

        @Override
        public LockGuard<Long> lock() {
            return lock(config.defaultTimeout());
        }

        @Override
        public LockGuard<Long> lock(Duration timeout) {
            try {
                if (!jdkLock.tryLock(timeout.toNanos(), TimeUnit.NANOSECONDS)) {
                    throw new OpenLockTimeoutException(
                            "Failed to acquire " + lockType + " lock within " + timeout, timeout);
                }
                Long token = tokenGenerator.incrementAndGet();
                currentToken.set(token);
                holdCount.set(holdCount.get() + 1);
                return new LockGuard<>(this, token);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new OpenLockAcquireException(lockType + " lock acquisition interrupted", e);
            }
        }

        @Override
        public boolean tryLock() {
            boolean acquired = jdkLock.tryLock();
            if (acquired) {
                Long token = tokenGenerator.incrementAndGet();
                currentToken.set(token);
                holdCount.set(holdCount.get() + 1);
            }
            return acquired;
        }

        @Override
        public boolean tryLock(Duration timeout) {
            try {
                boolean acquired = jdkLock.tryLock(timeout.toNanos(), TimeUnit.NANOSECONDS);
                if (acquired) {
                    Long token = tokenGenerator.incrementAndGet();
                    currentToken.set(token);
                    holdCount.set(holdCount.get() + 1);
                }
                return acquired;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        @Override
        public LockGuard<Long> lockInterruptibly() throws InterruptedException {
            jdkLock.lockInterruptibly();
            Long token = tokenGenerator.incrementAndGet();
            currentToken.set(token);
            holdCount.set(holdCount.get() + 1);
            return new LockGuard<>(this, token);
        }

        @Override
        public void unlock() {
            int count = holdCount.get() - 1;
            if (count <= 0) {
                holdCount.remove();
                currentToken.remove();
            } else {
                holdCount.set(count);
            }
            jdkLock.unlock();
        }

        @Override
        public boolean isHeldByCurrentThread() {
            return currentToken.get() != null;
        }

        @Override
        public Optional<Long> getToken() {
            return Optional.ofNullable(currentToken.get());
        }
    }
}
