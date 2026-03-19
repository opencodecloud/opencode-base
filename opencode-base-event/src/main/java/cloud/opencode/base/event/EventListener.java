package cloud.opencode.base.event;

/**
 * Event Listener Interface
 * 事件监听器接口
 *
 * <p>Functional interface for handling events.</p>
 * <p>用于处理事件的函数式接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lambda support - Lambda支持</li>
 *   <li>Type-safe event handling - 类型安全的事件处理</li>
 *   <li>Functional interface - 函数式接口</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Lambda listener
 * OpenEvent.getDefault().on(UserRegisteredEvent.class, event -> {
 *     System.out.println("User registered: " + event.getUserId());
 * });
 *
 * // Method reference
 * OpenEvent.getDefault().on(UserRegisteredEvent.class, this::handleUserRegistered);
 *
 * // Anonymous class
 * OpenEvent.getDefault().on(UserRegisteredEvent.class, new EventListener<>() {
 *     @Override
 *     public void onEvent(UserRegisteredEvent event) {
 *         // Handle event
 *     }
 * });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @param <E> the type of event to listen for | 要监听的事件类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@FunctionalInterface
public interface EventListener<E extends Event> {

    /**
     * Handle an event
     * 处理事件
     *
     * @param event the event to handle | 要处理的事件
     */
    void onEvent(E event);
}
