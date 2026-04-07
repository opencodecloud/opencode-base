package cloud.opencode.base.event;

import java.util.Objects;

/**
 * Dead Event - Wraps events that have no subscribers
 * 死事件 - 包装没有订阅者的事件
 *
 * <p>When an event is published but no listeners are registered for its type,
 * the event bus wraps it in a {@code DeadEvent} and re-dispatches it.
 * This allows monitoring and debugging of unhandled events.</p>
 * <p>当事件被发布但没有任何监听器注册其类型时，事件总线将其包装为 {@code DeadEvent} 并重新分发。
 * 这允许监控和调试未处理的事件。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Wraps unhandled events - 包装未处理的事件</li>
 *   <li>Preserves original event - 保留原始事件</li>
 *   <li>Enables dead event monitoring - 启用死事件监控</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Monitor dead events
 * eventBus.on(DeadEvent.class, dead -> {
 *     log.warn("Unhandled event: type={}, id={}",
 *         dead.getOriginalEvent().getClass().getSimpleName(),
 *         dead.getOriginalEvent().getId());
 * });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.3
 */
public final class DeadEvent extends Event {

    private final Event originalEvent;

    /**
     * Create a dead event wrapping the original unhandled event
     * 创建包装原始未处理事件的死事件
     *
     * @param originalEvent the event that had no subscribers | 没有订阅者的事件
     * @throws NullPointerException if originalEvent is null | 如果 originalEvent 为 null
     */
    public DeadEvent(Event originalEvent) {
        super("DeadEvent");
        this.originalEvent = Objects.requireNonNull(originalEvent, "originalEvent cannot be null");
    }

    /**
     * Get the original event that had no subscribers
     * 获取没有订阅者的原始事件
     *
     * @return the original event | 原始事件
     */
    public Event getOriginalEvent() {
        return originalEvent;
    }

    @Override
    public String toString() {
        return "DeadEvent{originalEvent=" + originalEvent + '}';
    }
}
