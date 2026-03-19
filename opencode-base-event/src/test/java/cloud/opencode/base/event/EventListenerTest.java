package cloud.opencode.base.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * EventListener 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("EventListener 测试")
class EventListenerTest {

    static class TestEvent extends Event {
        private final String message;

        public TestEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("可用作Lambda表达式")
        void testUsableAsLambda() {
            AtomicBoolean called = new AtomicBoolean(false);

            EventListener<TestEvent> listener = event -> called.set(true);

            assertThat(listener).isNotNull();
        }

        @Test
        @DisplayName("Lambda监听器处理事件")
        void testLambdaProcessesEvent() {
            AtomicReference<String> receivedMessage = new AtomicReference<>();

            EventListener<TestEvent> listener = event -> receivedMessage.set(event.getMessage());
            listener.onEvent(new TestEvent("Hello"));

            assertThat(receivedMessage.get()).isEqualTo("Hello");
        }

        @Test
        @DisplayName("可用作方法引用")
        void testUsableAsMethodReference() {
            EventListenerTest handler = new EventListenerTest();
            EventListener<TestEvent> listener = handler::handleEvent;

            assertThat(listener).isNotNull();
        }
    }

    @Nested
    @DisplayName("onEvent() 测试")
    class OnEventTests {

        @Test
        @DisplayName("接收并处理事件")
        void testReceivesEvent() {
            AtomicReference<TestEvent> receivedEvent = new AtomicReference<>();

            EventListener<TestEvent> listener = receivedEvent::set;
            TestEvent event = new TestEvent("test");
            listener.onEvent(event);

            assertThat(receivedEvent.get()).isSameAs(event);
        }

        @Test
        @DisplayName("处理null事件不抛异常")
        void testHandlesNullEvent() {
            EventListener<TestEvent> listener = event -> {
                // 什么都不做
            };

            assertThatNoException().isThrownBy(() -> listener.onEvent(null));
        }

        @Test
        @DisplayName("监听器可以抛出异常")
        void testCanThrowException() {
            EventListener<TestEvent> listener = event -> {
                throw new RuntimeException("Test exception");
            };

            assertThatThrownBy(() -> listener.onEvent(new TestEvent("test")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Test exception");
        }
    }

    @Nested
    @DisplayName("泛型类型测试")
    class GenericTypeTests {

        @Test
        @DisplayName("监听DataEvent")
        void testListenToDataEvent() {
            AtomicReference<String> data = new AtomicReference<>();

            EventListener<DataEvent<String>> listener = event -> data.set(event.getData());
            listener.onEvent(new DataEvent<>("Hello World"));

            assertThat(data.get()).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("监听自定义事件")
        void testListenToCustomEvent() {
            AtomicReference<String> receivedMessage = new AtomicReference<>();

            EventListener<TestEvent> listener = event -> receivedMessage.set(event.getMessage());
            listener.onEvent(new TestEvent("Custom Message"));

            assertThat(receivedMessage.get()).isEqualTo("Custom Message");
        }
    }

    @Nested
    @DisplayName("匿名类实现测试")
    class AnonymousClassTests {

        @Test
        @DisplayName("匿名类实现接口")
        void testAnonymousClassImplementation() {
            AtomicBoolean invoked = new AtomicBoolean(false);

            EventListener<TestEvent> listener = new EventListener<>() {
                @Override
                public void onEvent(TestEvent event) {
                    invoked.set(true);
                }
            };

            listener.onEvent(new TestEvent("test"));

            assertThat(invoked.get()).isTrue();
        }
    }

    // 用于方法引用测试的方法
    void handleEvent(TestEvent event) {
        // 处理事件
    }
}
