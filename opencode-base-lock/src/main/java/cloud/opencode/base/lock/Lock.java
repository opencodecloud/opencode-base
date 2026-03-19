package cloud.opencode.base.lock;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Unified Lock Interface for Local and Distributed Locks
 * 本地锁和分布式锁的统一抽象接口
 *
 * <p>Provides a unified abstraction for both local and distributed locks,
 * supporting try-with-resources pattern for automatic release.</p>
 * <p>提供本地锁和分布式锁的统一抽象，支持 try-with-resources 模式自动释放。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Automatic release with try-with-resources - 通过 try-with-resources 自动释放</li>
 *   <li>Timeout support for lock acquisition - 支持超时获取锁</li>
 *   <li>Interruptible lock acquisition - 支持可中断的锁获取</li>
 *   <li>Token-based lock identification - 基于令牌的锁标识</li>
 *   <li>Convenient execute methods - 便捷的执行方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // 1. Basic usage with try-with-resources | 基本用法
 * try (var guard = lock.lock()) {
 *     // Critical section | 临界区
 * }
 *
 * // 2. Try lock with timeout | 带超时尝试获取锁
 * if (lock.tryLock(Duration.ofSeconds(5))) {
 *     try {
 *         // Critical section | 临界区
 *     } finally {
 *         lock.unlock();
 *     }
 * }
 *
 * // 3. Execute with lock (recommended) | 使用锁执行（推荐）
 * lock.execute(() -> {
 *     // Critical section | 临界区
 * });
 *
 * // 4. Execute with result | 执行并返回结果
 * String result = lock.executeWithResult(() -> {
 *     return computeValue();
 * });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Virtual Thread friendly: Yes - 虚拟线程友好: 是</li>
 * </ul>
 *
 * @param <T> the type of lock token | 锁令牌类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see LockGuard
 * @see OpenLock
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public interface Lock<T> extends AutoCloseable {

    /**
     * Acquires the lock, blocking until available
     * 获取锁，阻塞直到可用
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * try (var guard = lock.lock()) {
     *     // Use the lock | 使用锁
     * } // Automatically released | 自动释放
     * }</pre>
     *
     * @return lock guard for auto-release | 用于自动释放的锁守卫
     */
    LockGuard<T> lock();

    /**
     * Acquires the lock with timeout
     * 带超时获取锁
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * try (var guard = lock.lock(Duration.ofSeconds(5))) {
     *     // Use the lock | 使用锁
     * }
     * }</pre>
     *
     * @param timeout maximum time to wait | 最大等待时间
     * @return lock guard for auto-release | 用于自动释放的锁守卫
     * @throws cloud.opencode.base.lock.exception.OpenLockTimeoutException if timeout exceeded | 超时时抛出
     */
    LockGuard<T> lock(Duration timeout);

    /**
     * Tries to acquire the lock immediately without waiting
     * 立即尝试获取锁，不等待
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * if (lock.tryLock()) {
     *     try {
     *         // Use the lock | 使用锁
     *     } finally {
     *         lock.unlock();
     *     }
     * }
     * }</pre>
     *
     * @return true if lock acquired, false otherwise | 获取成功返回true，否则返回false
     */
    boolean tryLock();

    /**
     * Tries to acquire the lock with timeout
     * 带超时尝试获取锁
     *
     * @param timeout maximum time to wait | 最大等待时间
     * @return true if lock acquired, false otherwise | 获取成功返回true，否则返回false
     */
    boolean tryLock(Duration timeout);

    /**
     * Acquires the lock interruptibly
     * 可中断地获取锁
     *
     * @return lock guard for auto-release | 用于自动释放的锁守卫
     * @throws InterruptedException if interrupted while waiting | 等待时被中断则抛出
     */
    LockGuard<T> lockInterruptibly() throws InterruptedException;

    /**
     * Releases the lock
     * 释放锁
     *
     * <p>Should be called in a finally block if not using try-with-resources.</p>
     * <p>如果不使用 try-with-resources，应在 finally 块中调用。</p>
     */
    void unlock();

    /**
     * Checks if lock is held by current thread
     * 检查当前线程是否持有锁
     *
     * @return true if current thread holds the lock | 当前线程持有锁返回true
     */
    boolean isHeldByCurrentThread();

    /**
     * Gets the current lock token
     * 获取当前锁令牌
     *
     * @return lock token, or empty if not locked | 锁令牌，未锁定时返回空
     */
    Optional<T> getToken();

    /**
     * Executes action with lock held
     * 持有锁执行操作
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * lock.execute(() -> {
     *     // This code runs with lock held | 此代码在持有锁时运行
     *     updateSharedState();
     * });
     * }</pre>
     *
     * @param action the action to execute | 要执行的操作
     */
    default void execute(Runnable action) {
        try (var guard = lock()) {
            action.run();
        }
    }

    /**
     * Executes action with lock and timeout
     * 带超时持有锁执行操作
     *
     * @param action  the action to execute | 要执行的操作
     * @param timeout maximum time to wait for lock | 获取锁的最大等待时间
     */
    default void execute(Runnable action, Duration timeout) {
        try (var guard = lock(timeout)) {
            action.run();
        }
    }

    /**
     * Executes supplier with lock and returns result
     * 持有锁执行并返回结果
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * String result = lock.executeWithResult(() -> {
     *     return readSharedState();
     * });
     * }</pre>
     *
     * @param supplier the supplier to execute | 要执行的供应者
     * @param <R>      the result type | 结果类型
     * @return the result | 结果
     */
    default <R> R executeWithResult(Supplier<R> supplier) {
        try (var guard = lock()) {
            return supplier.get();
        }
    }

    /**
     * Executes supplier with lock, timeout and returns result
     * 带超时持有锁执行并返回结果
     *
     * @param supplier the supplier to execute | 要执行的供应者
     * @param timeout  maximum time to wait for lock | 获取锁的最大等待时间
     * @param <R>      the result type | 结果类型
     * @return the result | 结果
     */
    default <R> R executeWithResult(Supplier<R> supplier, Duration timeout) {
        try (var guard = lock(timeout)) {
            return supplier.get();
        }
    }

    /**
     * Releases lock if held by current thread
     * 如果当前线程持有锁则释放
     */
    @Override
    default void close() {
        if (isHeldByCurrentThread()) {
            unlock();
        }
    }
}
