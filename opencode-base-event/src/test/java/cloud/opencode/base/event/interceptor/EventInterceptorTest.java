package cloud.opencode.base.event.interceptor;

import cloud.opencode.base.event.Event;
import cloud.opencode.base.event.OpenEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * EventInterceptor Tests
 * 事件拦截器测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.3
 */
@DisplayName("EventInterceptor 测试")
class EventInterceptorTest {

    static class TestEvent extends Event {
        TestEvent() { super(); }
    }

    @Nested
    @DisplayName("beforePublish")
    class BeforePublishTests {

        @Test
        @DisplayName("返回true应允许事件发布")
        void returnTrueShouldAllowPublish() {
            try (var bus = OpenEvent.create()) {
                var counter = new AtomicInteger();
                bus.on(TestEvent.class, _ -> counter.incrementAndGet());
                bus.addInterceptor(new EventInterceptor() {
                    @Override
                    public boolean beforePublish(Event event) { return true; }
                });

                bus.publish(new TestEvent());
                assertThat(counter.get()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("返回false应阻止事件发布")
        void returnFalseShouldBlockPublish() {
            try (var bus = OpenEvent.create()) {
                var counter = new AtomicInteger();
                bus.on(TestEvent.class, _ -> counter.incrementAndGet());
                bus.addInterceptor(new EventInterceptor() {
                    @Override
                    public boolean beforePublish(Event event) { return false; }
                });

                bus.publish(new TestEvent());
                assertThat(counter.get()).isEqualTo(0);
            }
        }

        @Test
        @DisplayName("多个拦截器应按序执行")
        void multipleInterceptorsShouldRunInOrder() {
            try (var bus = OpenEvent.create()) {
                var order = new ArrayList<String>();
                bus.addInterceptor(new EventInterceptor() {
                    @Override
                    public boolean beforePublish(Event event) { order.add("first"); return true; }
                });
                bus.addInterceptor(new EventInterceptor() {
                    @Override
                    public boolean beforePublish(Event event) { order.add("second"); return true; }
                });
                bus.on(TestEvent.class, _ -> {});

                bus.publish(new TestEvent());
                assertThat(order).containsExactly("first", "second");
            }
        }

        @Test
        @DisplayName("第一个拦截器返回false应短路后续拦截器")
        void firstFalseShouldShortCircuit() {
            try (var bus = OpenEvent.create()) {
                var secondCalled = new AtomicBoolean(false);
                bus.addInterceptor(new EventInterceptor() {
                    @Override
                    public boolean beforePublish(Event event) { return false; }
                });
                bus.addInterceptor(new EventInterceptor() {
                    @Override
                    public boolean beforePublish(Event event) { secondCalled.set(true); return true; }
                });

                bus.publish(new TestEvent());
                assertThat(secondCalled.get()).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("afterPublish")
    class AfterPublishTests {

        @Test
        @DisplayName("成功发布后应调用afterPublish")
        void shouldCallAfterPublishOnSuccess() {
            try (var bus = OpenEvent.create()) {
                var dispatched = new AtomicBoolean(false);
                bus.on(TestEvent.class, _ -> {});
                bus.addInterceptor(new EventInterceptor() {
                    @Override
                    public boolean beforePublish(Event event) { return true; }
                    @Override
                    public void afterPublish(Event event, boolean d) { dispatched.set(d); }
                });

                bus.publish(new TestEvent());
                assertThat(dispatched.get()).isTrue();
            }
        }

        @Test
        @DisplayName("无监听器时dispatched应为false")
        void shouldBeFalseWhenNoListeners() {
            try (var bus = OpenEvent.create()) {
                var dispatched = new AtomicBoolean(true);
                bus.addInterceptor(new EventInterceptor() {
                    @Override
                    public boolean beforePublish(Event event) { return true; }
                    @Override
                    public void afterPublish(Event event, boolean d) { dispatched.set(d); }
                });

                bus.publish(new TestEvent());
                assertThat(dispatched.get()).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("拦截器异常隔离")
    class ExceptionIsolationTests {

        @Test
        @DisplayName("拦截器异常应阻止事件发布（fail-closed安全策略）")
        void interceptorExceptionShouldBlockPublish() {
            try (var bus = OpenEvent.create()) {
                var counter = new AtomicInteger();
                bus.on(TestEvent.class, _ -> counter.incrementAndGet());
                bus.addInterceptor(new EventInterceptor() {
                    @Override
                    public boolean beforePublish(Event event) { throw new RuntimeException("boom"); }
                });

                // Fail-closed: interceptor exception blocks publishing (security-safe default)
                bus.publish(new TestEvent());
                assertThat(counter.get()).isEqualTo(0);
            }
        }
    }

    @Nested
    @DisplayName("Builder配置")
    class BuilderTests {

        @Test
        @DisplayName("Builder应支持添加拦截器")
        void builderShouldSupportInterceptor() {
            var called = new AtomicBoolean(false);
            try (var bus = OpenEvent.builder()
                    .interceptor(new EventInterceptor() {
                        @Override
                        public boolean beforePublish(Event event) { called.set(true); return true; }
                    })
                    .build()) {
                bus.on(TestEvent.class, _ -> {});
                bus.publish(new TestEvent());
                assertThat(called.get()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("removeInterceptor")
    class RemoveTests {

        @Test
        @DisplayName("移除拦截器后不再调用")
        void removedInterceptorShouldNotBeCalled() {
            try (var bus = OpenEvent.create()) {
                var counter = new AtomicInteger();
                EventInterceptor interceptor = new EventInterceptor() {
                    @Override
                    public boolean beforePublish(Event event) { counter.incrementAndGet(); return true; }
                };
                bus.addInterceptor(interceptor);
                bus.on(TestEvent.class, _ -> {});

                bus.publish(new TestEvent());
                assertThat(counter.get()).isEqualTo(1);

                bus.removeInterceptor(interceptor);
                bus.publish(new TestEvent());
                assertThat(counter.get()).isEqualTo(1);
            }
        }
    }
}
