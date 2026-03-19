package cloud.opencode.base.pool.factory;

/**
 * PooledObjectState - Pooled Object State Enumeration
 * PooledObjectState - 池化对象状态枚举
 *
 * <p>Defines the lifecycle states of a pooled object.</p>
 * <p>定义池化对象的生命周期状态。</p>
 *
 * <p><strong>State Transitions | 状态转换:</strong></p>
 * <pre>{@code
 * IDLE -> ALLOCATED (borrow)
 * ALLOCATED -> RETURNING -> IDLE (return)
 * IDLE -> EVICTION -> INVALID (evict)
 * ALLOCATED -> INVALID (invalidate)
 * Any -> ABANDONED (timeout detection)
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>State tracking for pooled objects - 池化对象状态追踪</li>
 *   <li>Thread-safe state transitions - 线程安全的状态转换</li>
 *   <li>Lifecycle management support - 生命周期管理支持</li>
 * </ul>
 *
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PooledObjectState state = pooledObject.getState();
 * if (state == PooledObjectState.IDLE) {
 *     // Object available for borrowing
 * }
 * }</pre>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public enum PooledObjectState {

    /**
     * Object is idle in the pool, available for borrowing.
     * 对象空闲在池中，可被借用。
     */
    IDLE,

    /**
     * Object has been allocated to a client.
     * 对象已被分配给客户端。
     */
    ALLOCATED,

    /**
     * Object is being evicted from the pool.
     * 对象正在被从池中驱逐。
     */
    EVICTION,

    /**
     * Object is being validated.
     * 对象正在被验证。
     */
    VALIDATION,

    /**
     * Object has been invalidated and will be destroyed.
     * 对象已失效，将被销毁。
     */
    INVALID,

    /**
     * Object is being returned to the pool.
     * 对象正在被归还到池中。
     */
    RETURNING,

    /**
     * Object has been abandoned (not returned within timeout).
     * 对象已被废弃（超时未归还）。
     */
    ABANDONED
}
