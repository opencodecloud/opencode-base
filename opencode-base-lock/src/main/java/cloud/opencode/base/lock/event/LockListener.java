package cloud.opencode.base.lock.event;

/**
 * Lock Event Listener Interface
 * 锁事件监听器接口
 *
 * <p>A functional interface for receiving lock lifecycle events.
 * Implementations are notified when locks are acquired, released,
 * timed out, or encounter errors.</p>
 * <p>用于接收锁生命周期事件的函数式接口。当锁被获取、释放、
 * 超时或遇到错误时，实现会收到通知。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Functional interface for lambda usage - 函数式接口支持lambda使用</li>
 *   <li>Event-driven lock monitoring - 事件驱动的锁监控</li>
 *   <li>Decoupled lock observation - 解耦的锁观察</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Lambda listener | Lambda监听器
 * LockListener logger = event ->
 *     System.out.println(event.type() + ": " + event.lockName());
 *
 * // Method reference listener | 方法引用监听器
 * LockListener metrics = this::recordMetrics;
 *
 * // Register with observable lock | 注册到可观察锁
 * ObservableLock<Long> lock = new ObservableLock<>(delegate, "myLock");
 * lock.addListener(logger);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Exception isolation: Exceptions from listeners are caught and
 *       suppressed by {@link ObservableLock} - 异常隔离: 监听器异常由
 *       {@link ObservableLock} 捕获并抑制</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see LockEvent
 * @see ObservableLock
 * @since JDK 25, opencode-base-lock V1.0.3
 */
@FunctionalInterface
public interface LockListener {

    /**
     * Called when a lock event occurs
     * 当锁事件发生时调用
     *
     * <p>Implementations should be lightweight and non-blocking to avoid
     * impacting lock performance. Exceptions thrown by this method are
     * caught and suppressed by {@link ObservableLock}.</p>
     * <p>实现应轻量且非阻塞，以避免影响锁性能。此方法抛出的异常
     * 由 {@link ObservableLock} 捕获并抑制。</p>
     *
     * @param event the lock event | 锁事件
     */
    void onEvent(LockEvent event);
}
