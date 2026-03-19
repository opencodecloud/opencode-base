package cloud.opencode.base.pool;

import cloud.opencode.base.pool.exception.OpenPoolException;

import java.time.Duration;
import java.util.function.BiFunction;

/**
 * KeyedObjectPool - Keyed Object Pool Interface
 * KeyedObjectPool - 键控对象池接口
 *
 * <p>Object pool interface that manages objects by key. Each key has its own
 * pool of objects, useful for multi-tenant or multi-datasource scenarios.</p>
 * <p>按键管理对象的对象池接口。每个键有自己的对象池，适用于多租户或多数据源场景。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Key-based object management - 基于键的对象管理</li>
 *   <li>Per-key borrow/return - 每键借用/归还</li>
 *   <li>Per-key statistics - 每键统计</li>
 *   <li>Multi-tenant support - 多租户支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Multi-datasource scenario
 * Connection masterConn = pool.borrowObject("master");
 * Connection slaveConn = pool.borrowObject("slave");
 * try {
 *     // use connections
 * } finally {
 *     pool.returnObject("master", masterConn);
 *     pool.returnObject("slave", slaveConn);
 * }
 *
 * // Execute pattern
 * String result = pool.execute("master", (key, conn) -> {
 *     return conn.executeQuery("SELECT...");
 * });
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 * @param <K> the key type - 键类型
 * @param <V> the value type - 值类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public interface KeyedObjectPool<K, V> extends AutoCloseable {

    /**
     * Borrows an object for the given key.
     * 为给定的键借用对象。
     *
     * @param key the key - 键
     * @return the borrowed object - 借用的对象
     * @throws OpenPoolException if borrowing fails - 如果借用失败
     */
    V borrowObject(K key) throws OpenPoolException;

    /**
     * Borrows an object for the given key with timeout.
     * 带超时为给定的键借用对象。
     *
     * @param key     the key - 键
     * @param timeout the maximum wait time - 最大等待时间
     * @return the borrowed object - 借用的对象
     * @throws OpenPoolException if borrowing fails or times out - 如果借用失败或超时
     */
    V borrowObject(K key, Duration timeout) throws OpenPoolException;

    /**
     * Returns an object for the given key.
     * 归还给定键的对象。
     *
     * @param key the key - 键
     * @param obj the object to return - 要归还的对象
     */
    void returnObject(K key, V obj);

    /**
     * Invalidates an object for the given key.
     * 使给定键的对象失效。
     *
     * @param key the key - 键
     * @param obj the object to invalidate - 要失效的对象
     */
    void invalidateObject(K key, V obj);

    /**
     * Gets the idle count for the given key.
     * 获取给定键的空闲数。
     *
     * @param key the key - 键
     * @return the idle count - 空闲数
     */
    int getNumIdle(K key);

    /**
     * Gets the active count for the given key.
     * 获取给定键的活跃数。
     *
     * @param key the key - 键
     * @return the active count - 活跃数
     */
    int getNumActive(K key);

    /**
     * Clears objects for the given key.
     * 清除给定键的对象。
     *
     * @param key the key - 键
     */
    void clear(K key);

    /**
     * Clears all objects for all keys.
     * 清除所有键的所有对象。
     */
    void clear();

    /**
     * Gets the total number of keys.
     * 获取键的总数。
     *
     * @return the key count - 键数
     */
    int getNumKeys();

    /**
     * Executes an action with a pooled object (auto-return).
     * 使用池化对象执行操作（自动归还）。
     *
     * @param <R>    the result type - 结果类型
     * @param key    the key - 键
     * @param action the action (receives key and object) - 操作（接收键和对象）
     * @return the action result - 操作结果
     * @throws OpenPoolException if execution fails - 如果执行失败
     */
    default <R> R execute(K key, BiFunction<K, V, R> action) throws OpenPoolException {
        V obj = borrowObject(key);
        try {
            return action.apply(key, obj);
        } finally {
            returnObject(key, obj);
        }
    }
}
