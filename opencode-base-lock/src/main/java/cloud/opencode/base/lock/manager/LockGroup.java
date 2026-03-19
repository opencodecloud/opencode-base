package cloud.opencode.base.lock.manager;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.exception.OpenLockAcquireException;
import cloud.opencode.base.lock.exception.OpenLockTimeoutException;

import java.time.Duration;
import java.util.*;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lock Group for Atomic Multi-Lock Acquisition with Deadlock Prevention
 * 带死锁预防的原子多锁获取锁组
 *
 * <p>Supports atomically acquiring multiple locks with deadlock prevention
 * through consistent lock ordering based on identity hash code.</p>
 * <p>支持通过基于身份哈希码的一致锁顺序来原子获取多个锁，防止死锁。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Atomic multi-lock acquisition - 原子多锁获取</li>
 *   <li>Deadlock prevention via consistent ordering - 通过一致顺序防止死锁</li>
 *   <li>Automatic rollback on failure - 失败时自动回滚</li>
 *   <li>Try-with-resources support - 支持try-with-resources</li>
 *   <li>Configurable timeout - 可配置超时</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create lock group | 创建锁组
 * LockGroup group = LockGroup.builder()
 *     .add(lockA)
 *     .add(lockB)
 *     .add(lockC)
 *     .timeout(Duration.ofSeconds(10))
 *     .build();
 *
 * // Atomic acquisition with auto-release | 自动释放的原子获取
 * try (var guard = group.lockAll()) {
 *     // All locks acquired atomically | 原子获取所有锁
 *     transferFunds(accountA, accountB, accountC);
 * }
 * // All locks automatically released | 所有锁自动释放
 *
 * // Try without exception | 无异常尝试
 * if (group.tryLockAll()) {
 *     try {
 *         // Critical section | 临界区
 *     } finally {
 *         group.releaseAll();
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Deadlock Prevention | 死锁预防:</strong></p>
 * <p>Locks are sorted by identity hash code before acquisition, ensuring
 * all threads acquire locks in the same order, preventing circular wait.</p>
 * <p>锁在获取前按身份哈希码排序，确保所有线程以相同顺序获取锁，防止循环等待。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Deadlock-free: Yes (with proper usage) - 无死锁: 是（正确使用时）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Lock
 * @see OpenLock#lockGroup()
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public class LockGroup implements AutoCloseable {

    private final List<Lock<?>> locks;
    private final List<Lock<?>> acquiredLocks = new CopyOnWriteArrayList<>();
    private final Duration timeout;

    private LockGroup(List<Lock<?>> locks, Duration timeout) {
        Objects.requireNonNull(locks, "locks cannot be null");
        Objects.requireNonNull(timeout, "timeout cannot be null");
        if (locks.isEmpty()) {
            throw new IllegalArgumentException("locks cannot be empty");
        }
        if (locks.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("locks cannot contain null elements");
        }
        // Sort locks to prevent deadlock (using identity hash code)
        this.locks = locks.stream()
                .sorted(Comparator.comparingInt(System::identityHashCode))
                .toList();
        this.timeout = timeout;
    }

    /**
     * Creates a new lock group builder
     * 创建新的锁组构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Acquires all locks in the group atomically
     * 原子获取组内所有锁
     *
     * @return lock guard for automatic release | 用于自动释放的锁守卫
     * @throws OpenLockTimeoutException if unable to acquire all locks | 如果无法获取所有锁则抛出异常
     * @throws OpenLockAcquireException if lock acquisition fails | 如果锁获取失败则抛出异常
     */
    public LockGroupGuard lockAll() {
        // Clear any leftover state from a previous acquisition attempt
        acquiredLocks.clear();
        for (Lock<?> lock : locks) {
            try {
                if (lock.tryLock(timeout)) {
                    acquiredLocks.add(lock);
                } else {
                    // Rollback acquired locks
                    releaseAll();
                    throw new OpenLockTimeoutException(
                            "Failed to acquire all locks within " + timeout, timeout);
                }
            } catch (OpenLockTimeoutException e) {
                throw e;
            } catch (Exception e) {
                releaseAll();
                throw new OpenLockAcquireException("Failed to acquire lock", e);
            }
        }
        return new LockGroupGuard(this);
    }

    /**
     * Tries to acquire all locks without waiting
     * 无等待尝试获取所有锁
     *
     * @return true if all locks acquired | true表示成功获取所有锁
     */
    public boolean tryLockAll() {
        // Clear any leftover state from a previous acquisition attempt
        acquiredLocks.clear();
        for (Lock<?> lock : locks) {
            if (lock.tryLock()) {
                acquiredLocks.add(lock);
            } else {
                releaseAll();
                return false;
            }
        }
        return true;
    }

    /**
     * Tries to acquire all locks with specified timeout
     * 使用指定超时尝试获取所有锁
     *
     * @param timeout the timeout duration | 超时时长
     * @return true if all locks acquired within timeout | true表示在超时内成功获取所有锁
     */
    public boolean tryLockAll(Duration timeout) {
        // Clear any leftover state from a previous acquisition attempt
        acquiredLocks.clear();
        long now = System.nanoTime();
        long timeoutNanos = timeout.toNanos();
        // Guard against deadline overflow: clamp to Long.MAX_VALUE
        long deadline;
        if (timeoutNanos > 0 && now > Long.MAX_VALUE - timeoutNanos) {
            deadline = Long.MAX_VALUE;
        } else {
            deadline = now + timeoutNanos;
        }
        for (Lock<?> lock : locks) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0 || !lock.tryLock(Duration.ofNanos(remaining))) {
                releaseAll();
                return false;
            }
            acquiredLocks.add(lock);
        }
        return true;
    }

    /**
     * Releases all acquired locks in reverse order
     * 按相反顺序释放所有已获取的锁
     */
    public void releaseAll() {
        // Release in reverse order
        for (int i = acquiredLocks.size() - 1; i >= 0; i--) {
            try {
                acquiredLocks.get(i).unlock();
            } catch (Exception e) {
                // Log and continue
            }
        }
        acquiredLocks.clear();
    }

    /**
     * Gets the total number of locks in the group
     * 获取锁组中的锁总数
     *
     * @return the number of locks | 锁数量
     */
    public int size() {
        return locks.size();
    }

    /**
     * Gets the number of currently acquired locks
     * 获取当前已获取的锁数量
     *
     * @return the number of acquired locks | 已获取的锁数量
     */
    public int acquiredCount() {
        return acquiredLocks.size();
    }

    @Override
    public void close() {
        releaseAll();
    }

    /**
     * Lock Group Builder for Fluent API
     * 锁组构建器 - 流式API
     *
     * <p>Provides fluent API for building lock groups.</p>
     * <p>提供用于构建锁组的流式API。</p>
     */
    public static class Builder {
        private final List<Lock<?>> locks = new ArrayList<>();
        private Duration timeout = Duration.ofSeconds(30);

        /**
         * Adds a lock to the group
         * 添加锁到组
         *
         * @param lock the lock to add | 要添加的锁
         * @return this builder | 此构建器
         */
        public Builder add(Lock<?> lock) {
            locks.add(lock);
            return this;
        }

        /**
         * Adds multiple locks to the group
         * 添加多个锁到组
         *
         * @param locks the locks to add | 要添加的锁集合
         * @return this builder | 此构建器
         */
        public Builder addAll(Collection<? extends Lock<?>> locks) {
            this.locks.addAll(locks);
            return this;
        }

        /**
         * Sets the timeout for acquiring all locks
         * 设置获取所有锁的超时
         *
         * @param timeout the timeout duration | 超时时长
         * @return this builder | 此构建器
         */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Builds the lock group
         * 构建锁组
         *
         * @return the lock group | 锁组
         * @throws IllegalArgumentException if no locks were added | 如果未添加锁则抛出异常
         */
        public LockGroup build() {
            if (locks.isEmpty()) {
                throw new IllegalArgumentException("Lock group must contain at least one lock");
            }
            return new LockGroup(new ArrayList<>(locks), timeout);
        }
    }

    /**
     * Lock Group Guard for Automatic Resource Release
     * 锁组守卫 - 用于自动资源释放
     *
     * <p>Implements AutoCloseable for use with try-with-resources.</p>
     * <p>实现AutoCloseable接口，配合try-with-resources使用。</p>
     *
     * @param group the lock group to guard | 要守卫的锁组
     */
    public record LockGroupGuard(LockGroup group) implements AutoCloseable {
        @Override
        public void close() {
            group.releaseAll();
        }
    }
}
