package cloud.opencode.base.event.store;

import cloud.opencode.base.event.Event;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * In-Memory Event Store
 * 内存事件存储
 *
 * <p>Thread-safe in-memory implementation of EventStore.</p>
 * <p>EventStore的线程安全内存实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-safe storage - 线程安全存储</li>
 *   <li>Automatic capacity management - 自动容量管理</li>
 *   <li>Event replay support - 事件重放支持</li>
 *   <li>Time-based queries - 基于时间的查询</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * InMemoryEventStore store = new InMemoryEventStore(10000);
 * store.save(event);
 *
 * // Query events
 * List<EventRecord> records = store.findByType(OrderCreatedEvent.class);
 *
 * // Replay events
 * store.replay(OrderEvent.class, e -> processEvent(e));
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
public class InMemoryEventStore implements EventStore {

    private final ConcurrentLinkedDeque<EventRecord> events;
    private final int maxCapacity;
    private final AtomicLong sequenceGenerator;
    private final AtomicInteger sizeCounter;

    /**
     * Create in-memory event store with default capacity (10000)
     * 使用默认容量(10000)创建内存事件存储
     */
    public InMemoryEventStore() {
        this(10000);
    }

    /**
     * Create in-memory event store with specified capacity
     * 使用指定容量创建内存事件存储
     *
     * @param maxCapacity the maximum number of events to store | 存储的最大事件数
     */
    public InMemoryEventStore(int maxCapacity) {
        this.events = new ConcurrentLinkedDeque<>();
        this.maxCapacity = maxCapacity;
        this.sequenceGenerator = new AtomicLong(0);
        this.sizeCounter = new AtomicInteger(0);
    }

    @Override
    public EventRecord save(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        EventRecord record = EventRecord.of(event, sequenceGenerator.incrementAndGet());
        events.addLast(record);
        sizeCounter.incrementAndGet();

        // Batch remove oldest events if capacity exceeded
        int excess = sizeCounter.get() - maxCapacity;
        if (excess > 0) {
            int removed = 0;
            for (int i = 0; i < excess; i++) {
                if (events.pollFirst() != null) {
                    removed++;
                } else {
                    break;
                }
            }
            if (removed > 0) {
                sizeCounter.addAndGet(-removed);
            }
        }

        return record;
    }

    @Override
    public Optional<EventRecord> findById(String eventId) {
        if (eventId == null) {
            return Optional.empty();
        }

        return events.stream()
            .filter(record -> eventId.equals(record.id()))
            .findFirst();
    }

    @Override
    public List<EventRecord> findByType(Class<? extends Event> eventType) {
        if (eventType == null) {
            return List.of();
        }

        return events.stream()
            .filter(record -> record.isType(eventType))
            .toList();
    }

    @Override
    public List<EventRecord> findByTimeRange(Instant from, Instant to) {
        if (from == null || to == null) {
            return List.of();
        }

        return events.stream()
            .filter(record -> record.isWithinTimeRange(from, to))
            .toList();
    }

    @Override
    public List<EventRecord> findBySource(String source) {
        if (source == null) {
            return List.of();
        }

        return events.stream()
            .filter(record -> source.equals(record.source()))
            .toList();
    }

    @Override
    public void replay(Class<? extends Event> eventType, Consumer<Event> handler) {
        if (eventType == null || handler == null) {
            return;
        }

        events.stream()
            .filter(record -> record.isType(eventType))
            .map(EventRecord::event)
            .forEach(handler);
    }

    @Override
    public void replayByTimeRange(Instant from, Instant to, Consumer<Event> handler) {
        if (from == null || to == null || handler == null) {
            return;
        }

        events.stream()
            .filter(record -> record.isWithinTimeRange(from, to))
            .map(EventRecord::event)
            .forEach(handler);
    }

    @Override
    public long count() {
        return sizeCounter.get();
    }

    @Override
    public void clear() {
        events.clear();
        sequenceGenerator.set(0);
        sizeCounter.set(0);
    }

    /**
     * Get the maximum capacity
     * 获取最大容量
     *
     * @return the maximum capacity | 最大容量
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Get all event records
     * 获取所有事件记录
     *
     * @return list of all event records | 所有事件记录列表
     */
    public List<EventRecord> findAll() {
        return List.copyOf(events);
    }

    /**
     * Get the current sequence number
     * 获取当前序列号
     *
     * @return current sequence number | 当前序列号
     */
    public long getCurrentSequence() {
        return sequenceGenerator.get();
    }
}
