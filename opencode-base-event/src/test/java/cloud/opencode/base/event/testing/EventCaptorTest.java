package cloud.opencode.base.event.testing;

import cloud.opencode.base.event.Event;
import cloud.opencode.base.event.OpenEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * EventCaptor Tests
 * 事件捕获器测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.3
 */
@DisplayName("EventCaptor 测试")
class EventCaptorTest {

    static class TestEvent extends Event {
        private final String value;
        TestEvent(String value) { super(); this.value = value; }
        String getValue() { return value; }
    }

    @Nested
    @DisplayName("捕获功能")
    class CaptureTests {

        @Test
        @DisplayName("应捕获发布的事件")
        void shouldCapturePublishedEvents() {
            try (var bus = OpenEvent.create()) {
                var captor = new EventCaptor<TestEvent>();
                bus.subscribe(TestEvent.class, captor);

                bus.publish(new TestEvent("a"));
                bus.publish(new TestEvent("b"));

                assertThat(captor.count()).isEqualTo(2);
                assertThat(captor.hasCaptured()).isTrue();
                assertThat(captor.getFirst().getValue()).isEqualTo("a");
                assertThat(captor.getLast().getValue()).isEqualTo("b");
                assertThat(captor.getCapturedEvents()).hasSize(2);
            }
        }

        @Test
        @DisplayName("初始状态应为空")
        void initialStateShouldBeEmpty() {
            var captor = new EventCaptor<TestEvent>();
            assertThat(captor.count()).isZero();
            assertThat(captor.hasCaptured()).isFalse();
            assertThat(captor.getFirst()).isNull();
            assertThat(captor.getLast()).isNull();
            assertThat(captor.getCapturedEvents()).isEmpty();
        }

        @Test
        @DisplayName("reset应清除所有捕获")
        void resetShouldClearAll() {
            try (var bus = OpenEvent.create()) {
                var captor = new EventCaptor<TestEvent>();
                bus.subscribe(TestEvent.class, captor);

                bus.publish(new TestEvent("a"));
                assertThat(captor.count()).isEqualTo(1);

                captor.reset();
                assertThat(captor.count()).isZero();
                assertThat(captor.hasCaptured()).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("等待功能")
    class AwaitTests {

        @Test
        @DisplayName("已有事件时awaitEvent应立即返回")
        void awaitShouldReturnImmediatelyWhenEventExists() {
            try (var bus = OpenEvent.create()) {
                var captor = new EventCaptor<TestEvent>();
                bus.subscribe(TestEvent.class, captor);
                bus.publish(new TestEvent("a"));

                assertThat(captor.awaitEvent(Duration.ofMillis(100))).isTrue();
            }
        }

        @Test
        @DisplayName("异步事件应可等待")
        void shouldAwaitAsyncEvents() {
            try (var bus = OpenEvent.create()) {
                var captor = new EventCaptor<TestEvent>();
                bus.subscribe(TestEvent.class, captor);

                bus.publishAsync(new TestEvent("async"));

                assertThat(captor.awaitEvent(Duration.ofSeconds(5))).isTrue();
                assertThat(captor.getFirst().getValue()).isEqualTo("async");
            }
        }

        @Test
        @DisplayName("超时时应返回false")
        void shouldReturnFalseOnTimeout() {
            var captor = new EventCaptor<TestEvent>();
            assertThat(captor.awaitEvent(Duration.ofMillis(50))).isFalse();
        }

        @Test
        @DisplayName("awaitEvents应等待指定数量")
        void awaitEventsShouldWaitForCount() {
            try (var bus = OpenEvent.create()) {
                var captor = new EventCaptor<TestEvent>();
                bus.subscribe(TestEvent.class, captor);

                bus.publish(new TestEvent("a"));
                bus.publish(new TestEvent("b"));

                assertThat(captor.awaitEvents(2, Duration.ofSeconds(1))).isTrue();
            }
        }

        @Test
        @DisplayName("count小于1应抛异常")
        void countLessThanOneShouldThrow() {
            var captor = new EventCaptor<TestEvent>();
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> captor.awaitEvents(0, Duration.ofSeconds(1)));
        }

        @Test
        @DisplayName("null timeout应抛NPE")
        void nullTimeoutShouldThrow() {
            var captor = new EventCaptor<TestEvent>();
            assertThatNullPointerException()
                    .isThrownBy(() -> captor.awaitEvent(null));
        }

        @Test
        @DisplayName("awaitEvents null timeout应抛NPE")
        void awaitEventsNullTimeoutShouldThrow() {
            var captor = new EventCaptor<TestEvent>();
            assertThatNullPointerException()
                    .isThrownBy(() -> captor.awaitEvents(1, null));
        }

        @Test
        @DisplayName("awaitEvents已有足够事件应立即返回")
        void awaitEventsAlreadySufficientShouldReturnImmediately() {
            try (var bus = OpenEvent.create()) {
                var captor = new EventCaptor<TestEvent>();
                bus.subscribe(TestEvent.class, captor);

                bus.publish(new TestEvent("a"));
                bus.publish(new TestEvent("b"));
                bus.publish(new TestEvent("c"));

                assertThat(captor.awaitEvents(2, Duration.ofMillis(10))).isTrue();
            }
        }

        @Test
        @DisplayName("awaitEvents超时应返回false")
        void awaitEventsTimeoutShouldReturnFalse() {
            var captor = new EventCaptor<TestEvent>();
            assertThat(captor.awaitEvents(5, Duration.ofMillis(50))).isFalse();
        }

        @Test
        @DisplayName("awaitEvent线程中断应恢复中断标记")
        void awaitEventInterruptShouldSetInterruptFlag() {
            var captor = new EventCaptor<TestEvent>();
            Thread.currentThread().interrupt();
            boolean result = captor.awaitEvent(Duration.ofMillis(100));
            assertThat(result).isFalse();
            // Clear interrupt flag
            assertThat(Thread.interrupted()).isTrue();
        }

        @Test
        @DisplayName("awaitEvents线程中断应恢复中断标记")
        void awaitEventsInterruptShouldSetInterruptFlag() {
            var captor = new EventCaptor<TestEvent>();
            Thread.currentThread().interrupt();
            boolean result = captor.awaitEvents(1, Duration.ofMillis(100));
            assertThat(result).isFalse();
            assertThat(Thread.interrupted()).isTrue();
        }

        @Test
        @DisplayName("异步事件到达应唤醒awaitEvents")
        void asyncEventShouldWakeUpAwaitEvents() {
            try (var bus = OpenEvent.create()) {
                var captor = new EventCaptor<TestEvent>();
                bus.subscribe(TestEvent.class, captor);

                // Publish asynchronously with slight delay
                bus.publishAsync(new TestEvent("x"));

                assertThat(captor.awaitEvents(1, Duration.ofSeconds(5))).isTrue();
                assertThat(captor.count()).isGreaterThanOrEqualTo(1);
            }
        }
    }
}
