package cloud.opencode.base.event.exception;

import cloud.opencode.base.event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * EventListenerException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("EventListenerException 测试")
class EventListenerExceptionTest {

    static class TestEvent extends Event {}

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造")
        void testMessageOnlyConstructor() {
            EventListenerException ex = new EventListenerException("Listener error");

            assertThat(ex.getRawMessage()).isEqualTo("Listener error");
            assertThat(ex.getCause()).isNull();
            assertThat(ex.getEventErrorCode()).isEqualTo(EventErrorCode.LISTENER_ERROR);
        }

        @Test
        @DisplayName("消息和原因构造")
        void testMessageAndCauseConstructor() {
            RuntimeException cause = new RuntimeException("Cause");

            EventListenerException ex = new EventListenerException("Listener error", cause);

            assertThat(ex.getRawMessage()).isEqualTo("Listener error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getEventErrorCode()).isEqualTo(EventErrorCode.LISTENER_ERROR);
        }

        @Test
        @DisplayName("全参数构造")
        void testFullConstructor() {
            RuntimeException cause = new RuntimeException("Cause");
            TestEvent event = new TestEvent();

            EventListenerException ex = new EventListenerException(
                    "Listener error", cause, event, EventErrorCode.REGISTRATION_FAILED);

            assertThat(ex.getRawMessage()).isEqualTo("Listener error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getEvent()).isEqualTo(event);
            assertThat(ex.getEventErrorCode()).isEqualTo(EventErrorCode.REGISTRATION_FAILED);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("是EventException的子类")
        void testIsEventException() {
            EventListenerException ex = new EventListenerException("Error");

            assertThat(ex).isInstanceOf(EventException.class);
        }
    }
}
