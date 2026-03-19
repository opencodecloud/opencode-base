package cloud.opencode.base.lock;

import java.util.function.Supplier;

/**
 * Read-Write Lock Interface for Concurrent Read Access
 * 读写锁接口 - 支持并发读取访问
 *
 * <p>Provides separate read and write locks where multiple readers
 * can hold the lock simultaneously, but writers have exclusive access.</p>
 * <p>提供独立的读锁和写锁，多个读取者可以同时持有锁，
 * 但写入者拥有独占访问权限。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Concurrent read access - 并发读取访问</li>
 *   <li>Exclusive write access - 独占写入访问</li>
 *   <li>Convenient execute methods - 便捷的执行方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ReadWriteLock<Long> rwLock = OpenLock.readWriteLock();
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
 * @param <T> the type of lock token | 锁令牌类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Lock
 * @see OpenLock#readWriteLock()
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public interface ReadWriteLock<T> {

    /**
     * Gets the read lock for concurrent read access
     * 获取用于并发读取访问的读锁
     *
     * @return the read lock | 读锁
     */
    Lock<T> readLock();

    /**
     * Gets the write lock for exclusive write access
     * 获取用于独占写入访问的写锁
     *
     * @return the write lock | 写锁
     */
    Lock<T> writeLock();

    /**
     * Executes action with read lock held
     * 持有读锁执行操作
     *
     * @param action the action to execute | 要执行的操作
     */
    default void executeRead(Runnable action) {
        readLock().execute(action);
    }

    /**
     * Executes supplier with read lock and returns result
     * 持有读锁执行并返回结果
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * String data = rwLock.executeRead(() -> readFromCache());
     * }</pre>
     *
     * @param supplier the supplier to execute | 要执行的供应者
     * @param <R>      the result type | 结果类型
     * @return the result | 结果
     */
    default <R> R executeRead(Supplier<R> supplier) {
        return readLock().executeWithResult(supplier);
    }

    /**
     * Executes action with write lock held
     * 持有写锁执行操作
     *
     * @param action the action to execute | 要执行的操作
     */
    default void executeWrite(Runnable action) {
        writeLock().execute(action);
    }

    /**
     * Executes supplier with write lock and returns result
     * 持有写锁执行并返回结果
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * String result = rwLock.executeWrite(() -> updateAndReturn(newData));
     * }</pre>
     *
     * @param supplier the supplier to execute | 要执行的供应者
     * @param <R>      the result type | 结果类型
     * @return the result | 结果
     */
    default <R> R executeWrite(Supplier<R> supplier) {
        return writeLock().executeWithResult(supplier);
    }
}
