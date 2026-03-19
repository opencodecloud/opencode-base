package cloud.opencode.base.event.store;

import cloud.opencode.base.event.Event;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Event Store Interface
 * 事件存储接口
 *
 * <p>Interface for persisting and retrieving events.</p>
 * <p>用于持久化和检索事件的接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Event persistence - 事件持久化</li>
 *   <li>Event retrieval - 事件检索</li>
 *   <li>Event replay - 事件重放</li>
 *   <li>Event sourcing support - 事件溯源支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EventStore store = new InMemoryEventStore(1000);
 * store.save(event);
 *
 * List<EventRecord> records = store.findByType(OrderCreatedEvent.class);
 * store.replay(OrderEvent.class, event -> processEvent(event));
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
public interface EventStore {

    /**
     * Save an event to the store
     * 将事件保存到存储
     *
     * @param event the event to save | 要保存的事件
     * @return the saved event record | 保存的事件记录
     */
    EventRecord save(Event event);

    /**
     * Find event record by event ID
     * 根据事件ID查找事件记录
     *
     * @param eventId the event ID | 事件ID
     * @return optional containing the event record if found | 如果找到则包含事件记录的Optional
     */
    Optional<EventRecord> findById(String eventId);

    /**
     * Find all event records by event type
     * 根据事件类型查找所有事件记录
     *
     * @param eventType the event type class | 事件类型类
     * @return list of event records | 事件记录列表
     */
    List<EventRecord> findByType(Class<? extends Event> eventType);

    /**
     * Find event records within a time range
     * 在时间范围内查找事件记录
     *
     * @param from start time (inclusive) | 开始时间（包含）
     * @param to   end time (exclusive) | 结束时间（不包含）
     * @return list of event records | 事件记录列表
     */
    List<EventRecord> findByTimeRange(Instant from, Instant to);

    /**
     * Find event records by source
     * 根据来源查找事件记录
     *
     * @param source the event source | 事件来源
     * @return list of event records | 事件记录列表
     */
    List<EventRecord> findBySource(String source);

    /**
     * Replay events of a specific type
     * 重放特定类型的事件
     *
     * @param eventType the event type to replay | 要重放的事件类型
     * @param handler   the handler to process each event | 处理每个事件的处理器
     */
    void replay(Class<? extends Event> eventType, Consumer<Event> handler);

    /**
     * Replay events within a time range
     * 在时间范围内重放事件
     *
     * @param from    start time (inclusive) | 开始时间（包含）
     * @param to      end time (exclusive) | 结束时间（不包含）
     * @param handler the handler to process each event | 处理每个事件的处理器
     */
    void replayByTimeRange(Instant from, Instant to, Consumer<Event> handler);

    /**
     * Get the total count of stored events
     * 获取存储事件的总数
     *
     * @return total event count | 事件总数
     */
    long count();

    /**
     * Clear all stored events
     * 清除所有存储的事件
     */
    void clear();
}
