package cloud.opencode.base.event.monitor;

import cloud.opencode.base.event.DataEvent;
import cloud.opencode.base.event.DeadEvent;
import cloud.opencode.base.event.Event;
import cloud.opencode.base.event.OpenEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * EventBusMetrics Tests
 * 事件总线指标测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.3
 */
@DisplayName("EventBusMetrics 测试")
class EventBusMetricsTest {

    static class TestEvent extends Event {
        TestEvent() { super(); }
    }

    @Nested
    @DisplayName("指标计数")
    class MetricsCountingTests {

        @Test
        @DisplayName("初始指标应全为零")
        void initialMetricsShouldBeZero() {
            try (var bus = OpenEvent.create()) {
                var metrics = bus.getMetrics();
                assertThat(metrics.totalPublished()).isZero();
                assertThat(metrics.totalDelivered()).isZero();
                assertThat(metrics.totalErrors()).isZero();
                assertThat(metrics.totalDeadEvents()).isZero();
                assertThat(metrics.listenerCount()).isZero();
            }
        }

        @Test
        @DisplayName("发布事件应递增publishedCount")
        void publishShouldIncrementCount() {
            try (var bus = OpenEvent.create()) {
                bus.on(TestEvent.class, _ -> {});
                bus.publish(new TestEvent());
                bus.publish(new TestEvent());

                assertThat(bus.getMetrics().totalPublished()).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("监听器调用应递增deliveredCount")
        void listenerInvocationShouldIncrementDelivered() {
            try (var bus = OpenEvent.create()) {
                bus.on(TestEvent.class, _ -> {});
                bus.on(TestEvent.class, _ -> {});
                bus.publish(new TestEvent());

                assertThat(bus.getMetrics().totalDelivered()).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("监听器异常应递增errorCount")
        void listenerErrorShouldIncrementErrorCount() {
            try (var bus = OpenEvent.create()) {
                bus.on(TestEvent.class, _ -> { throw new RuntimeException("boom"); });
                bus.publish(new TestEvent());

                assertThat(bus.getMetrics().totalErrors()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("无监听器事件应递增deadEventCount")
        void noListenerShouldIncrementDeadEvents() {
            try (var bus = OpenEvent.create()) {
                bus.publish(new TestEvent());

                assertThat(bus.getMetrics().totalDeadEvents()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("listenerCount应反映当前注册数")
        void listenerCountShouldReflectRegistered() {
            try (var bus = OpenEvent.create()) {
                bus.on(TestEvent.class, _ -> {});
                bus.on(TestEvent.class, _ -> {});
                bus.on(DeadEvent.class, _ -> {});

                assertThat(bus.getMetrics().listenerCount()).isEqualTo(3);
            }
        }
    }

    @Nested
    @DisplayName("resetMetrics")
    class ResetTests {

        @Test
        @DisplayName("重置后指标应为零")
        void resetShouldZeroOutCounters() {
            try (var bus = OpenEvent.create()) {
                bus.on(TestEvent.class, _ -> {});
                bus.publish(new TestEvent());
                bus.resetMetrics();

                var metrics = bus.getMetrics();
                assertThat(metrics.totalPublished()).isZero();
                assertThat(metrics.totalDelivered()).isZero();
                assertThat(metrics.totalErrors()).isZero();
                assertThat(metrics.totalDeadEvents()).isZero();
                // listenerCount is live count, not reset
                assertThat(metrics.listenerCount()).isEqualTo(1);
            }
        }
    }
}
