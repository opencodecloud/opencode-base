package cloud.opencode.base.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Subscription Tests
 * 订阅句柄测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.3
 */
@DisplayName("Subscription 测试")
class SubscriptionTest {

    static class TestEvent extends Event {
        TestEvent() { super(); }
    }

    @Nested
    @DisplayName("基本功能")
    class BasicTests {

        @Test
        @DisplayName("subscribe应返回活跃的Subscription")
        void subscribeShouldReturnActiveSubscription() {
            try (var bus = OpenEvent.create()) {
                var sub = bus.subscribe(TestEvent.class, _ -> {});

                assertThat(sub.isActive()).isTrue();
                assertThat(sub.getEventType()).isEqualTo(TestEvent.class);
            }
        }

        @Test
        @DisplayName("unsubscribe后应不再接收事件")
        void shouldNotReceiveEventsAfterUnsubscribe() {
            try (var bus = OpenEvent.create()) {
                var counter = new AtomicInteger();
                var sub = bus.subscribe(TestEvent.class, _ -> counter.incrementAndGet());

                bus.publish(new TestEvent());
                assertThat(counter.get()).isEqualTo(1);

                sub.unsubscribe();
                assertThat(sub.isActive()).isFalse();

                bus.publish(new TestEvent());
                assertThat(counter.get()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("unsubscribe应幂等")
        void unsubscribeShouldBeIdempotent() {
            try (var bus = OpenEvent.create()) {
                var sub = bus.subscribe(TestEvent.class, _ -> {});

                sub.unsubscribe();
                assertThatCode(sub::unsubscribe).doesNotThrowAnyException();
                assertThat(sub.isActive()).isFalse();
            }
        }

        @Test
        @DisplayName("close应等同于unsubscribe")
        void closeShouldDelegateToUnsubscribe() {
            try (var bus = OpenEvent.create()) {
                var counter = new AtomicInteger();
                var sub = bus.subscribe(TestEvent.class, _ -> counter.incrementAndGet());

                sub.close();
                assertThat(sub.isActive()).isFalse();

                bus.publish(new TestEvent());
                assertThat(counter.get()).isEqualTo(0);
            }
        }

        @Test
        @DisplayName("try-with-resources应自动取消订阅")
        void tryWithResourcesShouldAutoUnsubscribe() {
            try (var bus = OpenEvent.create()) {
                var counter = new AtomicInteger();
                Subscription sub;
                try (var s = bus.subscribe(TestEvent.class, _ -> counter.incrementAndGet())) {
                    sub = s;
                    bus.publish(new TestEvent());
                    assertThat(counter.get()).isEqualTo(1);
                }

                assertThat(sub.isActive()).isFalse();
                bus.publish(new TestEvent());
                assertThat(counter.get()).isEqualTo(1);
            }
        }
    }

    @Nested
    @DisplayName("过滤器")
    class FilterTests {

        @Test
        @DisplayName("subscribe with filter应仅接收匹配事件")
        void shouldOnlyReceiveFilteredEvents() {
            try (var bus = OpenEvent.create()) {
                var counter = new AtomicInteger();
                bus.subscribe(DataEvent.class, _ -> counter.incrementAndGet(),
                        e -> e.getData() instanceof String s && s.startsWith("A"));

                bus.publish(new DataEvent<>("Alpha"));
                bus.publish(new DataEvent<>("Beta"));
                bus.publish(new DataEvent<>("Arctic"));

                assertThat(counter.get()).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("null过滤器应接受所有事件")
        void nullFilterShouldAcceptAll() {
            try (var bus = OpenEvent.create()) {
                var counter = new AtomicInteger();
                bus.subscribe(TestEvent.class, _ -> counter.incrementAndGet(), null);

                bus.publish(new TestEvent());
                bus.publish(new TestEvent());

                assertThat(counter.get()).isEqualTo(2);
            }
        }
    }
}
