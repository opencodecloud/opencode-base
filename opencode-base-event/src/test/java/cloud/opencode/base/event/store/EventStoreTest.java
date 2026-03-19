package cloud.opencode.base.event.store;

import cloud.opencode.base.event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;

/**
 * EventStore 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("EventStore 测试")
class EventStoreTest {

    static class TestEvent extends Event {}

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("可创建简单实现")
        void testSimpleImplementation() {
            EventStore store = new EventStore() {
                @Override
                public EventRecord save(Event event) {
                    return EventRecord.of(event, 1L);
                }

                @Override
                public Optional<EventRecord> findById(String eventId) {
                    return Optional.empty();
                }

                @Override
                public List<EventRecord> findByType(Class<? extends Event> eventType) {
                    return List.of();
                }

                @Override
                public List<EventRecord> findByTimeRange(Instant from, Instant to) {
                    return List.of();
                }

                @Override
                public List<EventRecord> findBySource(String source) {
                    return List.of();
                }

                @Override
                public void replay(Class<? extends Event> eventType, Consumer<Event> handler) {
                }

                @Override
                public void replayByTimeRange(Instant from, Instant to, Consumer<Event> handler) {
                }

                @Override
                public long count() {
                    return 0;
                }

                @Override
                public void clear() {
                }
            };

            assertThat(store).isNotNull();
        }
    }
}
