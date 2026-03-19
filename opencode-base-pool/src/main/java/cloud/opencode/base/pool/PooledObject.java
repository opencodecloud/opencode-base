package cloud.opencode.base.pool;

import cloud.opencode.base.pool.factory.PooledObjectState;

import java.time.Duration;
import java.time.Instant;

/**
 * PooledObject - Pooled Object Wrapper Interface
 * PooledObject - 池化对象包装接口
 *
 * <p>Wraps objects managed by an object pool, tracking state
 * and timing information.</p>
 * <p>包装由对象池管理的对象，追踪状态和时间信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Object wrapping and access - 对象包装和访问</li>
 *   <li>State management - 状态管理</li>
 *   <li>Timing information tracking - 时间信息追踪</li>
 *   <li>Borrow count statistics - 借用次数统计</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PooledObject<Connection> pooledConn = pool.borrowPooledObject();
 * Connection conn = pooledConn.getObject();
 * Duration idle = pooledConn.getIdleDuration();
 * long count = pooledConn.getBorrowCount();
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
public interface PooledObject<T> {

    /**
     * Gets the actual pooled object.
     * 获取实际的池化对象。
     *
     * @return the object - 对象
     */
    T getObject();

    /**
     * Gets the creation instant.
     * 获取创建时间。
     *
     * @return the creation instant - 创建时间
     */
    Instant getCreateInstant();

    /**
     * Gets the last borrow instant.
     * 获取最后借用时间。
     *
     * @return the last borrow instant - 最后借用时间
     */
    Instant getLastBorrowInstant();

    /**
     * Gets the last return instant.
     * 获取最后归还时间。
     *
     * @return the last return instant - 最后归还时间
     */
    Instant getLastReturnInstant();

    /**
     * Gets the last use instant.
     * 获取最后使用时间。
     *
     * @return the last use instant - 最后使用时间
     */
    Instant getLastUseInstant();

    /**
     * Gets the current state.
     * 获取当前状态。
     *
     * @return the state - 状态
     */
    PooledObjectState getState();

    /**
     * Gets the borrow count.
     * 获取借用次数。
     *
     * @return the borrow count - 借用次数
     */
    long getBorrowCount();

    /**
     * Gets the active duration (time since last borrow).
     * 获取活跃时长（自上次借用以来的时间）。
     *
     * @return the active duration - 活跃时长
     */
    Duration getActiveDuration();

    /**
     * Gets the idle duration (time since last return).
     * 获取空闲时长（自上次归还以来的时间）。
     *
     * @return the idle duration - 空闲时长
     */
    Duration getIdleDuration();

    /**
     * Compares and sets the state atomically.
     * 原子地比较并设置状态。
     *
     * @param expect the expected state - 期望状态
     * @param update the new state - 新状态
     * @return true if successful - 如果成功返回true
     */
    boolean compareAndSetState(PooledObjectState expect, PooledObjectState update);
}
