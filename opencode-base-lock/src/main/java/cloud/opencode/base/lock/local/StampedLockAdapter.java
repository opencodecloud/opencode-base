package cloud.opencode.base.lock.local;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.LockConfig;
import cloud.opencode.base.lock.LockGuard;
import cloud.opencode.base.lock.ReadWriteLock;
import cloud.opencode.base.lock.exception.OpenLockAcquireException;
import cloud.opencode.base.lock.exception.OpenLockTimeoutException;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

/**
 * StampedLock Adapter - Safe Wrapper Around JDK StampedLock
 * StampedLock 适配器 - JDK StampedLock 的安全封装
 *
 * <p>Provides a safe, high-level API around {@link StampedLock} implementing
 * {@link ReadWriteLock}{@code <Long>}. The stamp returned by the underlying
 * StampedLock is used directly as the lock token.</p>
 * <p>围绕 {@link StampedLock} 提供安全的高级 API，实现
 * {@link ReadWriteLock}{@code <Long>}。底层 StampedLock 返回的戳记直接用作锁令牌。</p>
 *
 * <p><strong>WARNING: StampedLock is NOT reentrant | 警告：StampedLock 不可重入</strong></p>
 * <p>Unlike {@link LocalReadWriteLock}, StampedLock does not support reentrant locking.
 * A thread that already holds a write lock and attempts to acquire it again will deadlock.
 * Similarly, a thread holding a read lock cannot upgrade to a write lock via this adapter.</p>
 * <p>与 {@link LocalReadWriteLock} 不同，StampedLock 不支持可重入锁定。
 * 已持有写锁的线程再次尝试获取将导致死锁。
 * 同样，持有读锁的线程无法通过此适配器升级为写锁。</p>
 *
 * <p><strong>WARNING: Limited Virtual Thread support | 警告：有限的虚拟线程支持</strong></p>
 * <p>StampedLock uses spin-based optimizations that are not ideal for virtual threads.
 * For virtual-thread-heavy workloads, prefer {@link LocalReadWriteLock}.</p>
 * <p>StampedLock 使用基于自旋的优化，对虚拟线程不太理想。
 * 对于虚拟线程密集的工作负载，建议使用 {@link LocalReadWriteLock}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Optimistic read support for high-throughput reads - 乐观读支持高吞吐量读取</li>
 *   <li>Automatic fallback from optimistic to pessimistic read - 从乐观读自动降级到悲观读</li>
 *   <li>Multiple concurrent readers with exclusive writer - 多个并发读取者与独占写入者</li>
 *   <li>Stamp-based token management - 基于戳记的令牌管理</li>
 *   <li>try-with-resources support via LockGuard - 通过 LockGuard 支持 try-with-resources</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create adapter | 创建适配器
 * StampedLockAdapter adapter = new StampedLockAdapter();
 *
 * // Optimistic read (best performance) | 乐观读（最佳性能）
 * String value = adapter.optimisticRead(() -> sharedData);
 *
 * // Read with lock | 加锁读取
 * try (var guard = adapter.readLock().lock()) {
 *     // Read operations | 读操作
 * }
 *
 * // Write with lock | 加锁写入
 * try (var guard = adapter.writeLock().lock()) {
 *     // Write operations | 写操作
 * }
 *
 * // Using execute methods | 使用执行方法
 * adapter.executeRead(() -> readData());
 * adapter.executeWrite(() -> writeData());
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Optimistic reads avoid locking entirely when no writes occur - 无写入时乐观读完全避免加锁</li>
 *   <li>Higher throughput than ReentrantReadWriteLock under low contention - 低竞争下比 ReentrantReadWriteLock 更高吞吐量</li>
 *   <li>Not suitable for long-held locks or reentrant patterns - 不适合长期持有锁或可重入模式</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Virtual Thread friendly: Limited (spin-based) - 虚拟线程友好: 有限（基于自旋）</li>
 *   <li>Reentrant: No - 可重入: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see StampedLock
 * @see ReadWriteLock
 * @see LocalReadWriteLock
 * @since JDK 25, opencode-base-lock V1.0.3
 */
public class StampedLockAdapter implements ReadWriteLock<Long> {

    private final StampedLock stampedLock;
    private final Lock<Long> readLock;
    private final Lock<Long> writeLock;
    private final LockConfig config;

    /**
     * Creates a StampedLock adapter with default configuration
     * 使用默认配置创建 StampedLock 适配器
     */
    public StampedLockAdapter() {
        this(LockConfig.defaults());
    }

    /**
     * Creates a StampedLock adapter with specified configuration
     * 使用指定配置创建 StampedLock 适配器
     *
     * <p>Only the {@code defaultTimeout} from the configuration is used.
     * The {@code fair} and {@code reentrant} settings are ignored because
     * StampedLock is always unfair and non-reentrant.</p>
     * <p>仅使用配置中的 {@code defaultTimeout}。
     * {@code fair} 和 {@code reentrant} 设置被忽略，因为
     * StampedLock 始终是非公平且不可重入的。</p>
     *
     * @param config the lock configuration | 锁配置
     * @throws NullPointerException if config is null | 如果 config 为 null 则抛出
     */
    public StampedLockAdapter(LockConfig config) {
        Objects.requireNonNull(config, "LockConfig must not be null | LockConfig 不能为 null");
        this.config = config;
        this.stampedLock = new StampedLock();
        this.readLock = new ReadLockView(stampedLock, config);
        this.writeLock = new WriteLockView(stampedLock, config);
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
     * Performs an optimistic read operation with automatic fallback
     * 执行乐观读操作并自动降级
     *
     * <p>First attempts an optimistic read (no lock acquisition). If the data
     * is concurrently modified during the read, automatically falls back to a
     * pessimistic read lock using the configured default timeout.</p>
     * <p>首先尝试乐观读（无需获取锁）。如果在读取期间数据被并发修改，
     * 则自动降级为使用配置的默认超时的悲观读锁。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * StampedLockAdapter adapter = new StampedLockAdapter();
     * String value = adapter.optimisticRead(() -> sharedMap.get("key"));
     * }</pre>
     *
     * @param reader the read operation to execute | 要执行的读操作
     * @param <R>    the result type | 结果类型
     * @return the result of the read operation | 读操作的结果
     * @throws NullPointerException  if reader is null | 如果 reader 为 null 则抛出
     * @throws OpenLockTimeoutException if fallback lock acquisition times out | 降级锁获取超时时抛出
     * @throws OpenLockAcquireException if fallback lock acquisition is interrupted | 降级锁获取被中断时抛出
     */
    public <R> R optimisticRead(Supplier<R> reader) {
        return optimisticRead(reader, config.defaultTimeout());
    }

    /**
     * Performs an optimistic read with timeout for fallback lock acquisition
     * 执行乐观读操作，降级时使用指定超时
     *
     * <p>First attempts an optimistic read (no lock acquisition). If the data
     * is concurrently modified during the read, falls back to a pessimistic
     * read lock with the specified timeout.</p>
     * <p>首先尝试乐观读（无需获取锁）。如果在读取期间数据被并发修改，
     * 则降级为具有指定超时的悲观读锁。</p>
     *
     * @param reader  the read operation to execute | 要执行的读操作
     * @param timeout timeout for fallback pessimistic read lock | 降级悲观读锁的超时时间
     * @param <R>     the result type | 结果类型
     * @return the result of the read operation | 读操作的结果
     * @throws NullPointerException     if reader or timeout is null | 如果 reader 或 timeout 为 null 则抛出
     * @throws OpenLockTimeoutException if fallback lock acquisition times out | 降级锁获取超时时抛出
     * @throws OpenLockAcquireException if fallback lock acquisition is interrupted | 降级锁获取被中断时抛出
     */
    public <R> R optimisticRead(Supplier<R> reader, Duration timeout) {
        Objects.requireNonNull(reader, "reader must not be null | reader 不能为 null");
        Objects.requireNonNull(timeout, "timeout must not be null | timeout 不能为 null");

        // Attempt optimistic read | 尝试乐观读
        long stamp = stampedLock.tryOptimisticRead();
        if (stamp != 0L) {
            R result = reader.get();
            if (stampedLock.validate(stamp)) {
                return result;
            }
        }

        // Fallback to pessimistic read lock | 降级为悲观读锁
        try {
            stamp = stampedLock.tryReadLock(timeout.toNanos(), TimeUnit.NANOSECONDS);
            if (stamp == 0L) {
                throw new OpenLockTimeoutException(
                        "Failed to acquire read lock within " + timeout
                                + " during optimistic read fallback"
                                + " | 乐观读降级时未能在 " + timeout + " 内获取读锁",
                        timeout);
            }
            try {
                return reader.get();
            } finally {
                stampedLock.unlockRead(stamp);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpenLockAcquireException(
                    "Read lock acquisition interrupted during optimistic read fallback"
                            + " | 乐观读降级时读锁获取被中断", e);
        }
    }

    /**
     * Checks if the lock is currently held for reading
     * 检查锁当前是否被读取持有
     *
     * @return true if any thread holds a read lock | 如果任何线程持有读锁则返回 true
     */
    public boolean isReadLocked() {
        return stampedLock.isReadLocked();
    }

    /**
     * Checks if the lock is currently held for writing
     * 检查锁当前是否被写入持有
     *
     * @return true if any thread holds the write lock | 如果任何线程持有写锁则返回 true
     */
    public boolean isWriteLocked() {
        return stampedLock.isWriteLocked();
    }

    /**
     * Read Lock View - Wraps StampedLock Read Operations
     * 读锁视图 - 封装 StampedLock 读操作
     *
     * <p>Implements {@link Lock}{@code <Long>} by delegating to StampedLock's
     * read lock methods. The stamp is used as the lock token.</p>
     * <p>通过委托给 StampedLock 的读锁方法实现 {@link Lock}{@code <Long>}。
     * 戳记用作锁令牌。</p>
     *
     * <p><strong>WARNING: NOT reentrant | 警告：不可重入</strong></p>
     * <p>A thread that already holds this read lock must not call lock() again.</p>
     * <p>已持有此读锁的线程不能再次调用 lock()。</p>
     */
    private static class ReadLockView implements Lock<Long> {

        private final StampedLock stampedLock;
        private final LockConfig config;
        private final ThreadLocal<Long> currentToken = new ThreadLocal<>();

        ReadLockView(StampedLock stampedLock, LockConfig config) {
            this.stampedLock = stampedLock;
            this.config = config;
        }

        @Override
        public LockGuard<Long> lock() {
            return lock(config.defaultTimeout());
        }

        @Override
        public LockGuard<Long> lock(Duration timeout) {
            Objects.requireNonNull(timeout, "timeout must not be null | timeout 不能为 null");
            try {
                long stamp = stampedLock.tryReadLock(timeout.toNanos(), TimeUnit.NANOSECONDS);
                if (stamp == 0L) {
                    throw new OpenLockTimeoutException(
                            "Failed to acquire read lock within " + timeout
                                    + " | 未能在 " + timeout + " 内获取读锁",
                            timeout);
                }
                currentToken.set(stamp);
                return new LockGuard<>(this, stamp);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new OpenLockAcquireException(
                        "Read lock acquisition interrupted | 读锁获取被中断", e);
            }
        }

        @Override
        public boolean tryLock() {
            long stamp = stampedLock.tryReadLock();
            if (stamp != 0L) {
                currentToken.set(stamp);
                return true;
            }
            return false;
        }

        @Override
        public boolean tryLock(Duration timeout) {
            Objects.requireNonNull(timeout, "timeout must not be null | timeout 不能为 null");
            try {
                long stamp = stampedLock.tryReadLock(timeout.toNanos(), TimeUnit.NANOSECONDS);
                if (stamp != 0L) {
                    currentToken.set(stamp);
                    return true;
                }
                return false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        @Override
        public LockGuard<Long> lockInterruptibly() throws InterruptedException {
            long stamp = stampedLock.readLockInterruptibly();
            currentToken.set(stamp);
            return new LockGuard<>(this, stamp);
        }

        @Override
        public void unlock() {
            Long stamp = currentToken.get();
            if (stamp == null) {
                throw new OpenLockAcquireException(
                        "Cannot unlock read lock: not held by current thread"
                                + " | 无法解锁读锁：当前线程未持有");
            }
            try {
                stampedLock.unlockRead(stamp);
            } finally {
                currentToken.remove();
            }
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

    /**
     * Write Lock View - Wraps StampedLock Write Operations
     * 写锁视图 - 封装 StampedLock 写操作
     *
     * <p>Implements {@link Lock}{@code <Long>} by delegating to StampedLock's
     * write lock methods. The stamp is used as the lock token.</p>
     * <p>通过委托给 StampedLock 的写锁方法实现 {@link Lock}{@code <Long>}。
     * 戳记用作锁令牌。</p>
     *
     * <p><strong>WARNING: NOT reentrant | 警告：不可重入</strong></p>
     * <p>A thread that already holds this write lock must not call lock() again;
     * doing so will deadlock. StampedLock does not support reentrant writes.</p>
     * <p>已持有此写锁的线程不能再次调用 lock()；这样做会导致死锁。
     * StampedLock 不支持可重入写入。</p>
     */
    private static class WriteLockView implements Lock<Long> {

        private final StampedLock stampedLock;
        private final LockConfig config;
        private final ThreadLocal<Long> currentToken = new ThreadLocal<>();

        WriteLockView(StampedLock stampedLock, LockConfig config) {
            this.stampedLock = stampedLock;
            this.config = config;
        }

        @Override
        public LockGuard<Long> lock() {
            return lock(config.defaultTimeout());
        }

        @Override
        public LockGuard<Long> lock(Duration timeout) {
            Objects.requireNonNull(timeout, "timeout must not be null | timeout 不能为 null");
            try {
                long stamp = stampedLock.tryWriteLock(timeout.toNanos(), TimeUnit.NANOSECONDS);
                if (stamp == 0L) {
                    throw new OpenLockTimeoutException(
                            "Failed to acquire write lock within " + timeout
                                    + " | 未能在 " + timeout + " 内获取写锁",
                            timeout);
                }
                currentToken.set(stamp);
                return new LockGuard<>(this, stamp);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new OpenLockAcquireException(
                        "Write lock acquisition interrupted | 写锁获取被中断", e);
            }
        }

        @Override
        public boolean tryLock() {
            long stamp = stampedLock.tryWriteLock();
            if (stamp != 0L) {
                currentToken.set(stamp);
                return true;
            }
            return false;
        }

        @Override
        public boolean tryLock(Duration timeout) {
            Objects.requireNonNull(timeout, "timeout must not be null | timeout 不能为 null");
            try {
                long stamp = stampedLock.tryWriteLock(timeout.toNanos(), TimeUnit.NANOSECONDS);
                if (stamp != 0L) {
                    currentToken.set(stamp);
                    return true;
                }
                return false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        @Override
        public LockGuard<Long> lockInterruptibly() throws InterruptedException {
            long stamp = stampedLock.writeLockInterruptibly();
            currentToken.set(stamp);
            return new LockGuard<>(this, stamp);
        }

        @Override
        public void unlock() {
            Long stamp = currentToken.get();
            if (stamp == null) {
                throw new OpenLockAcquireException(
                        "Cannot unlock write lock: not held by current thread"
                                + " | 无法解锁写锁：当前线程未持有");
            }
            try {
                stampedLock.unlockWrite(stamp);
            } finally {
                currentToken.remove();
            }
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
