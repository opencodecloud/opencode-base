package cloud.opencode.base.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * Event 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("Event 测试")
class EventTest {

    // 测试用的具体Event实现
    static class TestEvent extends Event {
        public TestEvent() {
            super();
        }

        public TestEvent(String source) {
            super(source);
        }
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("无参构造创建事件")
        void testDefaultConstructor() {
            TestEvent event = new TestEvent();

            assertThat(event.getId()).isNotNull().isNotEmpty();
            assertThat(event.getTimestamp()).isNotNull();
            assertThat(event.getSource()).isNull();
            assertThat(event.isCancelled()).isFalse();
        }

        @Test
        @DisplayName("带source构造创建事件")
        void testConstructorWithSource() {
            TestEvent event = new TestEvent("TestService");

            assertThat(event.getId()).isNotNull().isNotEmpty();
            assertThat(event.getTimestamp()).isNotNull();
            assertThat(event.getSource()).isEqualTo("TestService");
            assertThat(event.isCancelled()).isFalse();
        }

        @Test
        @DisplayName("null source构造事件")
        void testConstructorWithNullSource() {
            TestEvent event = new TestEvent(null);

            assertThat(event.getSource()).isNull();
        }
    }

    @Nested
    @DisplayName("getId() 测试")
    class GetIdTests {

        @Test
        @DisplayName("ID是UUID格式")
        void testIdIsUuid() {
            TestEvent event = new TestEvent();

            assertThat(event.getId()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }

        @Test
        @DisplayName("每个事件ID唯一")
        void testIdIsUnique() {
            TestEvent event1 = new TestEvent();
            TestEvent event2 = new TestEvent();

            assertThat(event1.getId()).isNotEqualTo(event2.getId());
        }
    }

    @Nested
    @DisplayName("getTimestamp() 测试")
    class GetTimestampTests {

        @Test
        @DisplayName("时间戳不为null")
        void testTimestampNotNull() {
            TestEvent event = new TestEvent();

            assertThat(event.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("时间戳接近当前时间")
        void testTimestampIsRecent() {
            Instant before = Instant.now();
            TestEvent event = new TestEvent();
            Instant after = Instant.now();

            assertThat(event.getTimestamp())
                    .isAfterOrEqualTo(before)
                    .isBeforeOrEqualTo(after);
        }
    }

    @Nested
    @DisplayName("cancel() 和 isCancelled() 测试")
    class CancellationTests {

        @Test
        @DisplayName("默认未取消")
        void testDefaultNotCancelled() {
            TestEvent event = new TestEvent();

            assertThat(event.isCancelled()).isFalse();
        }

        @Test
        @DisplayName("取消后isCancelled返回true")
        void testCancelSetsFlag() {
            TestEvent event = new TestEvent();
            event.cancel();

            assertThat(event.isCancelled()).isTrue();
        }

        @Test
        @DisplayName("多次取消不抛异常")
        void testMultipleCancels() {
            TestEvent event = new TestEvent();

            assertThatNoException().isThrownBy(() -> {
                event.cancel();
                event.cancel();
                event.cancel();
            });

            assertThat(event.isCancelled()).isTrue();
        }
    }

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含类名")
        void testToStringContainsClassName() {
            TestEvent event = new TestEvent();

            assertThat(event.toString()).contains("TestEvent");
        }

        @Test
        @DisplayName("toString包含ID")
        void testToStringContainsId() {
            TestEvent event = new TestEvent();

            assertThat(event.toString()).contains(event.getId());
        }

        @Test
        @DisplayName("toString包含source")
        void testToStringContainsSource() {
            TestEvent event = new TestEvent("MySource");

            assertThat(event.toString()).contains("MySource");
        }

        @Test
        @DisplayName("toString包含cancelled状态")
        void testToStringContainsCancelledStatus() {
            TestEvent event = new TestEvent();

            assertThat(event.toString()).contains("cancelled=false");

            event.cancel();
            assertThat(event.toString()).contains("cancelled=true");
        }
    }
}
