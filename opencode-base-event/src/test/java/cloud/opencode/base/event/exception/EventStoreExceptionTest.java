package cloud.opencode.base.event.exception;

import cloud.opencode.base.event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * EventStoreException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("EventStoreException 测试")
class EventStoreExceptionTest {

    static class TestEvent extends Event {}

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造")
        void testMessageOnlyConstructor() {
            EventStoreException ex = new EventStoreException("Store error");

            assertThat(ex.getRawMessage()).isEqualTo("Store error");
            assertThat(ex.getCause()).isNull();
            assertThat(ex.getEventErrorCode()).isEqualTo(EventErrorCode.STORE_ERROR);
        }

        @Test
        @DisplayName("消息和原因构造")
        void testMessageAndCauseConstructor() {
            RuntimeException cause = new RuntimeException("Cause");

            EventStoreException ex = new EventStoreException("Store error", cause);

            assertThat(ex.getRawMessage()).isEqualTo("Store error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getEventErrorCode()).isEqualTo(EventErrorCode.STORE_ERROR);
        }

        @Test
        @DisplayName("全参数构造")
        void testFullConstructor() {
            RuntimeException cause = new RuntimeException("Cause");
            TestEvent event = new TestEvent();

            EventStoreException ex = new EventStoreException(
                    "Store error", cause, event, EventErrorCode.PERSIST_FAILED);

            assertThat(ex.getRawMessage()).isEqualTo("Store error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getEvent()).isEqualTo(event);
            assertThat(ex.getEventErrorCode()).isEqualTo(EventErrorCode.PERSIST_FAILED);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("是EventException的子类")
        void testIsEventException() {
            EventStoreException ex = new EventStoreException("Error");

            assertThat(ex).isInstanceOf(EventException.class);
        }
    }
}
