package cloud.opencode.base.event.handler;

import cloud.opencode.base.event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * EventExceptionHandler 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("EventExceptionHandler 测试")
class EventExceptionHandlerTest {

    static class TestEvent extends Event {}

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("可用作Lambda")
        void testUsableAsLambda() {
            AtomicBoolean called = new AtomicBoolean(false);

            EventExceptionHandler handler = (event, exception, listenerName) -> called.set(true);

            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Lambda接收所有参数")
        void testLambdaReceivesAllParameters() {
            AtomicReference<Event> receivedEvent = new AtomicReference<>();
            AtomicReference<Throwable> receivedException = new AtomicReference<>();
            AtomicReference<String> receivedListenerName = new AtomicReference<>();

            EventExceptionHandler handler = (event, exception, listenerName) -> {
                receivedEvent.set(event);
                receivedException.set(exception);
                receivedListenerName.set(listenerName);
            };

            TestEvent event = new TestEvent();
            RuntimeException exception = new RuntimeException("Error");

            handler.handleException(event, exception, "TestListener");

            assertThat(receivedEvent.get()).isEqualTo(event);
            assertThat(receivedException.get()).isEqualTo(exception);
            assertThat(receivedListenerName.get()).isEqualTo("TestListener");
        }
    }

    @Nested
    @DisplayName("handleException() 测试")
    class HandleExceptionTests {

        @Test
        @DisplayName("处理异常")
        void testHandleException() {
            AtomicBoolean handled = new AtomicBoolean(false);

            EventExceptionHandler handler = (event, exception, listenerName) -> handled.set(true);
            handler.handleException(new TestEvent(), new RuntimeException(), "test");

            assertThat(handled.get()).isTrue();
        }

        @Test
        @DisplayName("处理null参数不抛异常")
        void testHandleNullParameters() {
            EventExceptionHandler handler = (event, exception, listenerName) -> {};

            assertThatNoException().isThrownBy(() ->
                    handler.handleException(null, null, null));
        }
    }
}
