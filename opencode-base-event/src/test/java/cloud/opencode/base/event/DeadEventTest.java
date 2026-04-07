package cloud.opencode.base.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * DeadEvent Tests
 * 死事件测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.3
 */
@DisplayName("DeadEvent 测试")
class DeadEventTest {

    static class TestEvent extends Event {
        TestEvent() { super("test"); }
    }

    @Nested
    @DisplayName("构造和属性")
    class ConstructorTests {

        @Test
        @DisplayName("应保留原始事件")
        void shouldPreserveOriginalEvent() {
            var original = new TestEvent();
            var dead = new DeadEvent(original);

            assertThat(dead.getOriginalEvent()).isSameAs(original);
            assertThat(dead.getSource()).isEqualTo("DeadEvent");
            assertThat(dead.getId()).isNotNull();
            assertThat(dead.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("null原始事件应抛出NPE")
        void shouldThrowOnNullOriginalEvent() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new DeadEvent(null))
                    .withMessage("originalEvent cannot be null");
        }

        @Test
        @DisplayName("toString应包含原始事件信息")
        void toStringShouldIncludeOriginal() {
            var original = new TestEvent();
            var dead = new DeadEvent(original);
            assertThat(dead.toString()).contains("DeadEvent");
            assertThat(dead.toString()).contains("originalEvent=");
        }
    }

    @Nested
    @DisplayName("事件总线集成")
    class EventBusIntegrationTests {

        @Test
        @DisplayName("无监听器的事件应触发DeadEvent")
        void shouldFireDeadEventForUnhandledEvent() {
            try (var bus = OpenEvent.create()) {
                var captured = new java.util.concurrent.atomic.AtomicReference<DeadEvent>();
                bus.on(DeadEvent.class, captured::set);

                var event = new TestEvent();
                bus.publish(event);

                assertThat(captured.get()).isNotNull();
                assertThat(captured.get().getOriginalEvent()).isSameAs(event);
            }
        }

        @Test
        @DisplayName("有监听器的事件不应触发DeadEvent")
        void shouldNotFireDeadEventForHandledEvent() {
            try (var bus = OpenEvent.create()) {
                var deadCaptured = new java.util.concurrent.atomic.AtomicReference<DeadEvent>();
                bus.on(DeadEvent.class, deadCaptured::set);
                bus.on(TestEvent.class, _ -> {});

                bus.publish(new TestEvent());

                assertThat(deadCaptured.get()).isNull();
            }
        }

        @Test
        @DisplayName("DeadEvent自身不应递归")
        void deadEventShouldNotRecurse() {
            try (var bus = OpenEvent.create()) {
                // No DeadEvent listener registered - the DeadEvent itself should not trigger another DeadEvent
                assertThatCode(() -> bus.publish(new TestEvent())).doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("死事件指标应递增")
        void deadEventMetricsShouldIncrement() {
            try (var bus = OpenEvent.create()) {
                bus.on(DeadEvent.class, _ -> {});
                bus.publish(new TestEvent());

                assertThat(bus.getMetrics().totalDeadEvents()).isEqualTo(1);
            }
        }
    }
}
