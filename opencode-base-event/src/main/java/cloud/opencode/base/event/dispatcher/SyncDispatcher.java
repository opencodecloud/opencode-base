package cloud.opencode.base.event.dispatcher;

import cloud.opencode.base.event.Event;
import cloud.opencode.base.event.exception.EventListenerException;

import java.util.List;
import java.util.function.Consumer;

/**
 * Synchronous Event Dispatcher
 * 同步事件分发器
 *
 * <p>Dispatches events synchronously in the calling thread.</p>
 * <p>在调用线程中同步分发事件。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sequential execution - 顺序执行</li>
 *   <li>Priority ordering - 优先级排序</li>
 *   <li>Cancellation support - 取消支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SyncDispatcher dispatcher = new SyncDispatcher();
 * dispatcher.dispatch(event, listeners);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
public class SyncDispatcher implements EventDispatcher {

    private static final System.Logger LOGGER = System.getLogger(SyncDispatcher.class.getName());

    private final boolean stopOnError;

    /**
     * Create sync dispatcher with default settings
     * 使用默认设置创建同步分发器
     */
    public SyncDispatcher() {
        this(false);
    }

    /**
     * Create sync dispatcher with error handling configuration
     * 使用错误处理配置创建同步分发器
     *
     * @param stopOnError if true, stop dispatching on first error | 如果为true，遇到第一个错误时停止分发
     */
    public SyncDispatcher(boolean stopOnError) {
        this.stopOnError = stopOnError;
    }

    /**
     * Dispatch event to listeners synchronously
     * 同步将事件分发到监听器
     *
     * <p>Listeners are invoked in order. If an event is cancelled,
     * subsequent listeners will not be invoked.</p>
     * <p>监听器按顺序调用。如果事件被取消，后续监听器将不会被调用。</p>
     *
     * @param event     the event to dispatch | 要分发的事件
     * @param listeners the list of listener handlers | 监听器处理器列表
     * @throws EventListenerException if stopOnError is true and a listener throws | 如果stopOnError为true且监听器抛出异常
     */
    @Override
    public void dispatch(Event event, List<Consumer<Event>> listeners) {
        if (event == null || listeners == null || listeners.isEmpty()) {
            return;
        }

        for (Consumer<Event> listener : listeners) {
            // Check if event has been cancelled
            if (event.isCancelled()) {
                break;
            }

            try {
                listener.accept(event);
            } catch (Exception e) {
                if (stopOnError) {
                    throw new EventListenerException("Listener invocation failed", e);
                }
                // Log and continue with next listener
                LOGGER.log(System.Logger.Level.WARNING,
                        "Event listener failed for event type {0}: {1}",
                        event.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean isAsync() {
        return false;
    }
}
