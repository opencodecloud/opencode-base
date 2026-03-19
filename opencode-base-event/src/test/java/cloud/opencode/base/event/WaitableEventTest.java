package cloud.opencode.base.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * WaitableEvent 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("WaitableEvent 测试")
class WaitableEventTest {

    static class SimpleEvent extends Event {
        public SimpleEvent() {
            super();
        }

        public SimpleEvent(String source) {
            super(source);
        }
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用事件和latch创建")
        void testConstructorWithEventAndLatch() {
            SimpleEvent innerEvent = new SimpleEvent("source");
            CountDownLatch latch = new CountDownLatch(1);

            WaitableEvent event = new WaitableEvent(innerEvent, latch);

            assertThat(event.getWrappedEvent()).isEqualTo(innerEvent);
            assertThat(event.getLatch()).isEqualTo(latch);
        }

        @Test
        @DisplayName("仅使用事件创建")
        void testConstructorWithEventOnly() {
            SimpleEvent innerEvent = new SimpleEvent();

            WaitableEvent event = new WaitableEvent(innerEvent);

            assertThat(event.getWrappedEvent()).isEqualTo(innerEvent);
            assertThat(event.getLatch()).isNotNull();
            assertThat(event.getLatch().getCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("继承被包装事件的source")
        void testInheritsSource() {
            SimpleEvent innerEvent = new SimpleEvent("MySource");

            WaitableEvent event = new WaitableEvent(innerEvent);

            assertThat(event.getSource()).isEqualTo("MySource");
        }
    }

    @Nested
    @DisplayName("getWrappedEvent() 测试")
    class GetWrappedEventTests {

        @Test
        @DisplayName("返回被包装的事件")
        void testReturnsWrappedEvent() {
            SimpleEvent innerEvent = new SimpleEvent();

            WaitableEvent event = new WaitableEvent(innerEvent);

            assertThat(event.getWrappedEvent()).isSameAs(innerEvent);
        }
    }

    @Nested
    @DisplayName("getLatch() 测试")
    class GetLatchTests {

        @Test
        @DisplayName("返回latch")
        void testReturnsLatch() {
            CountDownLatch latch = new CountDownLatch(2);
            SimpleEvent innerEvent = new SimpleEvent();

            WaitableEvent event = new WaitableEvent(innerEvent, latch);

            assertThat(event.getLatch()).isSameAs(latch);
            assertThat(event.getLatch().getCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("complete() 测试")
    class CompleteTests {

        @Test
        @DisplayName("complete减少latch计数")
        void testCompleteCountsDown() {
            SimpleEvent innerEvent = new SimpleEvent();
            WaitableEvent event = new WaitableEvent(innerEvent);

            assertThat(event.getLatch().getCount()).isEqualTo(1);

            event.complete();

            assertThat(event.getLatch().getCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("多次complete不抛异常")
        void testMultipleComplete() {
            SimpleEvent innerEvent = new SimpleEvent();
            WaitableEvent event = new WaitableEvent(innerEvent);

            assertThatNoException().isThrownBy(() -> {
                event.complete();
                event.complete();
                event.complete();
            });
        }
    }

    @Nested
    @DisplayName("await() 测试")
    class AwaitTests {

        @Test
        @DisplayName("await带超时成功")
        void testAwaitWithTimeoutSuccess() throws InterruptedException {
            SimpleEvent innerEvent = new SimpleEvent();
            WaitableEvent event = new WaitableEvent(innerEvent);

            // 在另一个线程中complete
            Thread.startVirtualThread(() -> {
                try {
                    Thread.sleep(50);
                    event.complete();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            boolean result = event.await(1000);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("await超时返回false")
        void testAwaitTimeout() throws InterruptedException {
            SimpleEvent innerEvent = new SimpleEvent();
            WaitableEvent event = new WaitableEvent(innerEvent);

            boolean result = event.await(50);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("await无限等待直到complete")
        void testAwaitIndefinitely() throws InterruptedException {
            SimpleEvent innerEvent = new SimpleEvent();
            WaitableEvent event = new WaitableEvent(innerEvent);
            AtomicBoolean completed = new AtomicBoolean(false);

            Thread thread = Thread.startVirtualThread(() -> {
                try {
                    event.await();
                    completed.set(true);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            Thread.sleep(50);
            assertThat(completed.get()).isFalse();

            event.complete();
            thread.join(1000);

            assertThat(completed.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("cancel相关测试")
    class CancellationTests {

        @Test
        @DisplayName("isCancelled委托给被包装事件")
        void testIsCancelledDelegates() {
            SimpleEvent innerEvent = new SimpleEvent();
            WaitableEvent event = new WaitableEvent(innerEvent);

            assertThat(event.isCancelled()).isFalse();

            innerEvent.cancel();

            assertThat(event.isCancelled()).isTrue();
        }

        @Test
        @DisplayName("cancel委托给被包装事件")
        void testCancelDelegates() {
            SimpleEvent innerEvent = new SimpleEvent();
            WaitableEvent event = new WaitableEvent(innerEvent);

            event.cancel();

            assertThat(innerEvent.isCancelled()).isTrue();
            assertThat(event.isCancelled()).isTrue();
        }
    }

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("包含WaitableEvent")
        void testToStringContainsWaitableEvent() {
            SimpleEvent innerEvent = new SimpleEvent();
            WaitableEvent event = new WaitableEvent(innerEvent);

            assertThat(event.toString()).contains("WaitableEvent");
        }

        @Test
        @DisplayName("包含被包装事件信息")
        void testToStringContainsWrappedEvent() {
            SimpleEvent innerEvent = new SimpleEvent();
            WaitableEvent event = new WaitableEvent(innerEvent);

            assertThat(event.toString()).contains("wrappedEvent");
        }
    }
}
