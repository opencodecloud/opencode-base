package cloud.opencode.base.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * Sticky Event Tests
 * 粘性事件测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.3
 */
@DisplayName("StickyEvent 测试")
class StickyEventTest {

    static class ConfigEvent extends Event {
        private final String config;
        ConfigEvent(String config) { super(); this.config = config; }
        String getConfig() { return config; }
    }

    static class OtherEvent extends Event {
        OtherEvent() { super(); }
    }

    @Nested
    @DisplayName("publishSticky")
    class PublishStickyTests {

        @Test
        @DisplayName("应存储并发布给当前监听器")
        void shouldStoreAndPublishToCurrent() {
            try (var bus = OpenEvent.create()) {
                var captured = new AtomicReference<ConfigEvent>();
                bus.on(ConfigEvent.class, captured::set);

                var event = new ConfigEvent("v1");
                bus.publishSticky(event);

                assertThat(captured.get()).isSameAs(event);
                assertThat(bus.getStickyEvent(ConfigEvent.class)).isSameAs(event);
            }
        }

        @Test
        @DisplayName("后续注册的订阅者应收到粘性事件")
        void lateSubscriberShouldReceiveSticky() {
            try (var bus = OpenEvent.create()) {
                var event = new ConfigEvent("v1");
                bus.publishSticky(event);

                var captured = new AtomicReference<ConfigEvent>();
                bus.subscribe(ConfigEvent.class, captured::set);

                assertThat(captured.get()).isSameAs(event);
            }
        }

        @Test
        @DisplayName("最后一个粘性事件应覆盖之前的")
        void lastStickyEventShouldOverride() {
            try (var bus = OpenEvent.create()) {
                bus.publishSticky(new ConfigEvent("v1"));
                bus.publishSticky(new ConfigEvent("v2"));

                var captured = new AtomicReference<ConfigEvent>();
                bus.subscribe(ConfigEvent.class, captured::set);

                assertThat(captured.get().getConfig()).isEqualTo("v2");
            }
        }

        @Test
        @DisplayName("不同事件类型的粘性事件独立存储")
        void differentTypesShouldBeIndependent() {
            try (var bus = OpenEvent.create()) {
                bus.publishSticky(new ConfigEvent("v1"));
                assertThat(bus.getStickyEvent(OtherEvent.class)).isNull();
            }
        }
    }

    @Nested
    @DisplayName("getStickyEvent / removeStickyEvent")
    class AccessorTests {

        @Test
        @DisplayName("无粘性事件时返回null")
        void shouldReturnNullWhenNone() {
            try (var bus = OpenEvent.create()) {
                assertThat(bus.getStickyEvent(ConfigEvent.class)).isNull();
            }
        }

        @Test
        @DisplayName("removeStickyEvent应移除并返回")
        void removeShouldReturnAndDelete() {
            try (var bus = OpenEvent.create()) {
                var event = new ConfigEvent("v1");
                bus.publishSticky(event);

                var removed = bus.removeStickyEvent(ConfigEvent.class);
                assertThat(removed).isSameAs(event);
                assertThat(bus.getStickyEvent(ConfigEvent.class)).isNull();
            }
        }

        @Test
        @DisplayName("removeStickyEvent无事件时返回null")
        void removeShouldReturnNullWhenNone() {
            try (var bus = OpenEvent.create()) {
                assertThat(bus.removeStickyEvent(ConfigEvent.class)).isNull();
            }
        }
    }

    @Nested
    @DisplayName("粘性事件与过滤器")
    class StickyWithFilterTests {

        @Test
        @DisplayName("过滤器应对粘性事件生效")
        void filterShouldApplyToStickyDelivery() {
            try (var bus = OpenEvent.create()) {
                bus.publishSticky(new ConfigEvent("v1"));

                var counter = new AtomicInteger();
                bus.subscribe(ConfigEvent.class, _ -> counter.incrementAndGet(),
                        e -> e.getConfig().equals("v2"));

                // Should not receive because filter doesn't match
                assertThat(counter.get()).isEqualTo(0);
            }
        }

        @Test
        @DisplayName("匹配过滤器的粘性事件应投递")
        void matchingFilterShouldDeliverSticky() {
            try (var bus = OpenEvent.create()) {
                bus.publishSticky(new ConfigEvent("v2"));

                var counter = new AtomicInteger();
                bus.subscribe(ConfigEvent.class, _ -> counter.incrementAndGet(),
                        e -> e.getConfig().equals("v2"));

                assertThat(counter.get()).isEqualTo(1);
            }
        }
    }
}
