package cloud.opencode.base.event.store;

import cloud.opencode.base.event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * EventRecord 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("EventRecord 测试")
class EventRecordTest {

    static class TestEvent extends Event {
        public TestEvent() {
            super();
        }

        public TestEvent(String source) {
            super(source);
        }
    }

    static class OtherEvent extends Event {}

    @Nested
    @DisplayName("记录构造测试")
    class RecordConstructorTests {

        @Test
        @DisplayName("全参数构造")
        void testFullConstructor() {
            Instant now = Instant.now();
            TestEvent event = new TestEvent("source");

            EventRecord record = new EventRecord(
                    "event-123",
                    event,
                    TestEvent.class.getName(),
                    now,
                    "source",
                    now.plusMillis(10),
                    1L
            );

            assertThat(record.id()).isEqualTo("event-123");
            assertThat(record.event()).isEqualTo(event);
            assertThat(record.eventType()).isEqualTo(TestEvent.class.getName());
            assertThat(record.timestamp()).isEqualTo(now);
            assertThat(record.source()).isEqualTo("source");
            assertThat(record.storedAt()).isEqualTo(now.plusMillis(10));
            assertThat(record.sequenceNumber()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("of() 工厂方法测试")
    class OfFactoryTests {

        @Test
        @DisplayName("从事件创建记录")
        void testOfFromEvent() {
            TestEvent event = new TestEvent("mySource");

            EventRecord record = EventRecord.of(event, 100L);

            assertThat(record.id()).isEqualTo(event.getId());
            assertThat(record.event()).isEqualTo(event);
            assertThat(record.eventType()).isEqualTo(TestEvent.class.getName());
            assertThat(record.timestamp()).isEqualTo(event.getTimestamp());
            assertThat(record.source()).isEqualTo("mySource");
            assertThat(record.storedAt()).isNotNull();
            assertThat(record.sequenceNumber()).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("isType() 测试")
    class IsTypeTests {

        @Test
        @DisplayName("匹配正确类型返回true")
        void testMatchingType() {
            TestEvent event = new TestEvent();
            EventRecord record = EventRecord.of(event, 1L);

            assertThat(record.isType(TestEvent.class)).isTrue();
        }

        @Test
        @DisplayName("匹配父类型返回true")
        void testMatchingParentType() {
            TestEvent event = new TestEvent();
            EventRecord record = EventRecord.of(event, 1L);

            assertThat(record.isType(Event.class)).isTrue();
        }

        @Test
        @DisplayName("不匹配类型返回false")
        void testNonMatchingType() {
            TestEvent event = new TestEvent();
            EventRecord record = EventRecord.of(event, 1L);

            assertThat(record.isType(OtherEvent.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isWithinTimeRange() 测试")
    class IsWithinTimeRangeTests {

        @Test
        @DisplayName("在范围内返回true")
        void testWithinRange() {
            Instant eventTime = Instant.now();
            TestEvent event = new TestEvent();
            EventRecord record = new EventRecord(
                    "id", event, "type", eventTime, null, eventTime, 1L);

            Instant from = eventTime.minusSeconds(1);
            Instant to = eventTime.plusSeconds(1);

            assertThat(record.isWithinTimeRange(from, to)).isTrue();
        }

        @Test
        @DisplayName("等于from边界返回true")
        void testAtFromBoundary() {
            Instant eventTime = Instant.now();
            TestEvent event = new TestEvent();
            EventRecord record = new EventRecord(
                    "id", event, "type", eventTime, null, eventTime, 1L);

            assertThat(record.isWithinTimeRange(eventTime, eventTime.plusSeconds(1))).isTrue();
        }

        @Test
        @DisplayName("等于to边界返回false")
        void testAtToBoundary() {
            Instant eventTime = Instant.now();
            TestEvent event = new TestEvent();
            EventRecord record = new EventRecord(
                    "id", event, "type", eventTime, null, eventTime, 1L);

            assertThat(record.isWithinTimeRange(eventTime.minusSeconds(1), eventTime)).isFalse();
        }

        @Test
        @DisplayName("在范围外返回false")
        void testOutsideRange() {
            Instant eventTime = Instant.now();
            TestEvent event = new TestEvent();
            EventRecord record = new EventRecord(
                    "id", event, "type", eventTime, null, eventTime, 1L);

            Instant from = eventTime.plusSeconds(1);
            Instant to = eventTime.plusSeconds(2);

            assertThat(record.isWithinTimeRange(from, to)).isFalse();
        }
    }
}
