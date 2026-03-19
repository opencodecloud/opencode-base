package cloud.opencode.base.event.dispatcher;

import cloud.opencode.base.event.Event;

import java.util.List;
import java.util.function.Consumer;

/**
 * Event Dispatcher Interface
 * 事件分发器接口
 *
 * <p>Interface for dispatching events to registered listeners.</p>
 * <p>将事件分发到注册的监听器的接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Event dispatching - 事件分发</li>
 *   <li>Priority ordering - 优先级排序</li>
 *   <li>Sync/Async support - 同步/异步支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EventDispatcher dispatcher = new SyncDispatcher();
 * dispatcher.dispatch(event, listeners);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
public interface EventDispatcher {

    /**
     * Dispatch event to listeners
     * 将事件分发到监听器
     *
     * @param event     the event to dispatch | 要分发的事件
     * @param listeners the list of listener handlers | 监听器处理器列表
     */
    void dispatch(Event event, List<Consumer<Event>> listeners);

    /**
     * Check if this dispatcher supports async dispatching
     * 检查此分发器是否支持异步分发
     *
     * @return true if supports async | 如果支持异步返回true
     */
    default boolean isAsync() {
        return false;
    }

    /**
     * Shutdown the dispatcher and release resources
     * 关闭分发器并释放资源
     */
    default void shutdown() {
        // Default no-op implementation
    }
}
