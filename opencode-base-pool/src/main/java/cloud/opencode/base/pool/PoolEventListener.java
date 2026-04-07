package cloud.opencode.base.pool;

import java.time.Duration;

/**
 * PoolEventListener - Pool Lifecycle Event Listener
 * PoolEventListener - 池生命周期事件监听器
 *
 * <p>Listener interface for receiving notifications about pool lifecycle events
 * such as borrowing, returning, creation, destruction, eviction, exhaustion,
 * and timeout. All methods have default no-op implementations so that users
 * only need to override the events they are interested in.</p>
 * <p>用于接收池生命周期事件通知的监听器接口，包括借用、归还、创建、销毁、
 * 驱逐、耗尽和超时事件。所有方法都有默认的空操作实现，用户只需覆盖
 * 感兴趣的事件即可。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Borrow/return event notification - 借用/归还事件通知</li>
 *   <li>Create/destroy event notification - 创建/销毁事件通知</li>
 *   <li>Eviction event notification - 驱逐事件通知</li>
 *   <li>Pool exhaustion notification - 池耗尽通知</li>
 *   <li>Timeout notification with wait duration - 带等待时长的超时通知</li>
 *   <li>Default no-op for all methods - 所有方法默认空操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PoolEventListener<Connection> listener = new PoolEventListener<>() {
 *     @Override
 *     public void onBorrow(Connection conn) {
 *         log.info("Borrowed connection: {}", conn);
 *     }
 *
 *     @Override
 *     public void onExhausted() {
 *         log.warn("Connection pool exhausted!");
 *     }
 *
 *     @Override
 *     public void onTimeout(Duration waitDuration) {
 *         log.error("Timed out after {}ms", waitDuration.toMillis());
 *     }
 * };
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Exception handling: Implementations should not throw - 异常处理: 实现不应抛出异常</li>
 * </ul>
 *
 * @param <T> the type of object being pooled - 池化对象类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.3
 */
public interface PoolEventListener<T> {

    /**
     * Called after an object is successfully borrowed from the pool.
     * 对象从池中成功借出后调用。
     *
     * @param object the borrowed object - 借出的对象
     */
    default void onBorrow(T object) {
        // no-op
    }

    /**
     * Called after an object is successfully returned to the pool.
     * 对象成功归还到池中后调用。
     *
     * @param object the returned object - 归还的对象
     */
    default void onReturn(T object) {
        // no-op
    }

    /**
     * Called after a new object is successfully created by the pool.
     * 池成功创建新对象后调用。
     *
     * @param object the created object - 创建的对象
     */
    default void onCreate(T object) {
        // no-op
    }

    /**
     * Called before an object is destroyed by the pool.
     * 池销毁对象前调用。
     *
     * @param object the object about to be destroyed - 即将被销毁的对象
     */
    default void onDestroy(T object) {
        // no-op
    }

    /**
     * Called before an object is evicted from the pool.
     * 对象从池中驱逐前调用。
     *
     * @param object the object about to be evicted - 即将被驱逐的对象
     */
    default void onEvict(T object) {
        // no-op
    }

    /**
     * Called when the pool is exhausted (no objects available and at max capacity).
     * 当池耗尽时调用（无可用对象且已达到最大容量）。
     */
    default void onExhausted() {
        // no-op
    }

    /**
     * Called when a borrow request times out.
     * 当借用请求超时时调用。
     *
     * @param waitDuration the duration the caller waited before timing out - 调用者超时前的等待时长
     */
    default void onTimeout(Duration waitDuration) {
        // no-op
    }
}
