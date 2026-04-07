package cloud.opencode.base.event;

/**
 * Subscription Handle - Represents an active event subscription
 * 订阅句柄 - 表示一个活跃的事件订阅
 *
 * <p>Returned by event bus registration methods to allow precise lifecycle management
 * of individual subscriptions. Implements {@link AutoCloseable} for try-with-resources support.</p>
 * <p>由事件总线注册方法返回，允许精确管理单个订阅的生命周期。
 * 实现 {@link AutoCloseable} 以支持 try-with-resources。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Precise unsubscription - 精确取消订阅</li>
 *   <li>AutoCloseable support - 自动关闭支持</li>
 *   <li>Active state checking - 活跃状态检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Manual lifecycle management
 * Subscription sub = eventBus.subscribe(MyEvent.class, e -> handle(e));
 * // ... later
 * sub.unsubscribe();
 *
 * // Try-with-resources
 * try (var sub = eventBus.subscribe(MyEvent.class, e -> handle(e))) {
 *     eventBus.publish(new MyEvent());
 * } // auto-unsubscribed
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.3
 */
public interface Subscription extends AutoCloseable {

    /**
     * Unsubscribe this listener from the event bus
     * 从事件总线取消订阅此监听器
     *
     * <p>Idempotent: calling multiple times has no additional effect.</p>
     * <p>幂等：多次调用没有额外效果。</p>
     */
    void unsubscribe();

    /**
     * Check if this subscription is still active
     * 检查此订阅是否仍然活跃
     *
     * @return true if the subscription is active | 如果订阅活跃返回 true
     */
    boolean isActive();

    /**
     * Get the event type this subscription is for
     * 获取此订阅的事件类型
     *
     * @return the event type class | 事件类型类
     */
    Class<? extends Event> getEventType();

    /**
     * Close this subscription (delegates to unsubscribe)
     * 关闭此订阅（委托给 unsubscribe）
     */
    @Override
    default void close() {
        unsubscribe();
    }
}
