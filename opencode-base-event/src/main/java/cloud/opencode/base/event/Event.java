package cloud.opencode.base.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Event Base Class
 * 事件基类
 *
 * <p>Base class for all events in the event-driven architecture.</p>
 * <p>事件驱动架构中所有事件的基类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unique event ID - 唯一事件ID</li>
 *   <li>Timestamp tracking - 时间戳跟踪</li>
 *   <li>Event source identification - 事件来源标识</li>
 *   <li>Cancellation support - 取消支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Define custom event
 * public class UserRegisteredEvent extends Event {
 *     private final Long userId;
 *     private final String email;
 *
 *     public UserRegisteredEvent(Long userId, String email) {
 *         this.userId = userId;
 *         this.email = email;
 *     }
 * }
 *
 * // Publish event
 * OpenEvent.getDefault().publish(new UserRegisteredEvent(1L, "user@example.com"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable properties, volatile cancelled) - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
public abstract class Event {

    private final String id;
    private final Instant timestamp;
    private final String source;
    private volatile boolean cancelled;

    /**
     * Create event with no source
     * 创建无来源的事件
     */
    protected Event() {
        this(null);
    }

    /**
     * Create event with source
     * 创建带来源的事件
     *
     * @param source the event source identifier | 事件来源标识
     */
    protected Event(String source) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.source = source;
    }

    /**
     * Get unique event ID
     * 获取唯一事件ID
     *
     * @return the event ID | 事件ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get event timestamp
     * 获取事件时间戳
     *
     * @return the timestamp | 时间戳
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Get event source
     * 获取事件来源
     *
     * @return the source or null | 来源或null
     */
    public String getSource() {
        return source;
    }

    /**
     * Check if event is cancelled
     * 检查事件是否已取消
     *
     * @return true if cancelled | 已取消返回true
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Cancel the event to stop further processing
     * 取消事件以停止后续处理
     */
    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id='" + id + '\'' +
                ", timestamp=" + timestamp +
                ", source='" + source + '\'' +
                ", cancelled=" + cancelled +
                '}';
    }
}
