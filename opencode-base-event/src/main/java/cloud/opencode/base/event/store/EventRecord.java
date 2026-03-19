package cloud.opencode.base.event.store;

import cloud.opencode.base.event.Event;

import java.time.Instant;

/**
 * Event Record
 * 事件记录
 *
 * <p>Immutable record representing a stored event with metadata.</p>
 * <p>表示带有元数据的已存储事件的不可变记录。</p>
 *
 * <p><strong>Fields | 字段:</strong></p>
 * <ul>
 *   <li>id - Event unique identifier | 事件唯一标识符</li>
 *   <li>event - The actual event object | 实际事件对象</li>
 *   <li>eventType - Event class name | 事件类名</li>
 *   <li>timestamp - Event timestamp | 事件时间戳</li>
 *   <li>source - Event source | 事件来源</li>
 *   <li>storedAt - Storage timestamp | 存储时间戳</li>
 *   <li>sequenceNumber - Sequence number for ordering | 用于排序的序列号</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EventRecord record = new EventRecord(
 *     event.getId(),
 *     event,
 *     event.getClass().getName(),
 *     event.getTimestamp(),
 *     event.getSource(),
 *     Instant.now(),
 *     sequenceGenerator.next()
 * );
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core functionality - 核心功能</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @param id             the unique event ID | 唯一事件ID
 * @param event          the event object | 事件对象
 * @param eventType      the event type class name | 事件类型类名
 * @param timestamp      the event timestamp | 事件时间戳
 * @param source         the event source | 事件来源
 * @param storedAt       the storage timestamp | 存储时间戳
 * @param sequenceNumber the sequence number | 序列号
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
public record EventRecord(
    String id,
    Event event,
    String eventType,
    Instant timestamp,
    String source,
    Instant storedAt,
    long sequenceNumber
) {

    /**
     * Create event record from an event
     * 从事件创建事件记录
     *
     * @param event          the event | 事件
     * @param sequenceNumber the sequence number | 序列号
     * @return new event record | 新的事件记录
     */
    public static EventRecord of(Event event, long sequenceNumber) {
        return new EventRecord(
            event.getId(),
            event,
            event.getClass().getName(),
            event.getTimestamp(),
            event.getSource(),
            Instant.now(),
            sequenceNumber
        );
    }

    /**
     * Check if the event is of a specific type
     * 检查事件是否为特定类型
     *
     * @param type the event type class | 事件类型类
     * @return true if matches | 如果匹配返回true
     */
    public boolean isType(Class<? extends Event> type) {
        return type.isInstance(event);
    }

    /**
     * Check if the event is within a time range
     * 检查事件是否在时间范围内
     *
     * @param from start time (inclusive) | 开始时间（包含）
     * @param to   end time (exclusive) | 结束时间（不包含）
     * @return true if within range | 如果在范围内返回true
     */
    public boolean isWithinTimeRange(Instant from, Instant to) {
        return !timestamp.isBefore(from) && timestamp.isBefore(to);
    }
}
