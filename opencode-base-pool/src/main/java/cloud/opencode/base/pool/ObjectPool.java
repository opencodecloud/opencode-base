package cloud.opencode.base.pool;

import cloud.opencode.base.pool.exception.OpenPoolException;
import cloud.opencode.base.pool.metrics.PoolMetrics;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * ObjectPool - Object Pool Interface
 * ObjectPool - 对象池接口
 *
 * <p>Core interface for object pooling, providing borrow/return semantics
 * with automatic resource management.</p>
 * <p>对象池化的核心接口，提供借用/归还语义和自动资源管理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Object borrowing with timeout - 带超时的对象借用</li>
 *   <li>Object returning - 对象归还</li>
 *   <li>Object invalidation - 对象失效</li>
 *   <li>Pool metrics access - 池指标访问</li>
 *   <li>Execute pattern for auto-return - 自动归还的执行模式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Manual borrow/return
 * Connection conn = pool.borrowObject();
 * try {
 *     // use connection
 * } finally {
 *     pool.returnObject(conn);
 * }
 *
 * // Execute pattern (recommended)
 * String result = pool.execute(conn -> {
 *     return conn.executeQuery("SELECT...");
 * });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @param <T> the type of object being pooled - 池化对象类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public interface ObjectPool<T> extends AutoCloseable {

    /**
     * Borrows an object from the pool.
     * 从池中借用对象。
     *
     * <p>Uses default timeout from pool configuration.</p>
     * <p>使用池配置的默认超时。</p>
     *
     * @return the borrowed object - 借用的对象
     * @throws OpenPoolException if borrowing fails - 如果借用失败
     */
    T borrowObject() throws OpenPoolException;

    /**
     * Borrows an object from the pool with timeout.
     * 带超时从池中借用对象。
     *
     * @param timeout the maximum wait time - 最大等待时间
     * @return the borrowed object - 借用的对象
     * @throws OpenPoolException if borrowing fails or times out - 如果借用失败或超时
     */
    T borrowObject(Duration timeout) throws OpenPoolException;

    /**
     * Returns an object to the pool.
     * 将对象归还到池中。
     *
     * @param obj the object to return - 要归还的对象
     */
    void returnObject(T obj);

    /**
     * Invalidates an object (will not be returned to pool).
     * 使对象失效（不会返回池中）。
     *
     * @param obj the object to invalidate - 要失效的对象
     */
    void invalidateObject(T obj);

    /**
     * Adds a new object to the pool.
     * 向池中添加新对象。
     *
     * @throws OpenPoolException if adding fails - 如果添加失败
     */
    void addObject() throws OpenPoolException;

    /**
     * Gets the number of idle objects.
     * 获取空闲对象数。
     *
     * @return the idle count - 空闲数
     */
    int getNumIdle();

    /**
     * Gets the number of active (borrowed) objects.
     * 获取活跃（借出）对象数。
     *
     * @return the active count - 活跃数
     */
    int getNumActive();

    /**
     * Clears all idle objects from the pool.
     * 清除池中所有空闲对象。
     */
    void clear();

    /**
     * Gets the pool metrics.
     * 获取池指标。
     *
     * @return the metrics - 指标
     */
    PoolMetrics getMetrics();

    /**
     * Executes an action with a pooled object (auto-return).
     * 使用池化对象执行操作（自动归还）。
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * String result = pool.execute(conn -> {
     *     return conn.executeQuery("SELECT...");
     * });
     * }</pre>
     *
     * @param <R>    the result type - 结果类型
     * @param action the action to execute - 要执行的操作
     * @return the action result - 操作结果
     * @throws OpenPoolException if execution fails - 如果执行失败
     */
    default <R> R execute(Function<T, R> action) throws OpenPoolException {
        T obj = borrowObject();
        try {
            return action.apply(obj);
        } finally {
            returnObject(obj);
        }
    }

    /**
     * Executes a void action with a pooled object (auto-return).
     * 使用池化对象执行无返回值操作（自动归还）。
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * pool.execute(conn -> {
     *     conn.executeUpdate("UPDATE...");
     * });
     * }</pre>
     *
     * @param action the action to execute - 要执行的操作
     * @throws OpenPoolException if execution fails - 如果执行失败
     */
    default void execute(Consumer<T> action) throws OpenPoolException {
        T obj = borrowObject();
        try {
            action.accept(obj);
        } finally {
            returnObject(obj);
        }
    }
}
