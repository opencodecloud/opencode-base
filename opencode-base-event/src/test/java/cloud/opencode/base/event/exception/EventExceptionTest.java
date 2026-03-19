package cloud.opencode.base.event.exception;

import cloud.opencode.base.event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * EventException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("EventException 测试")
class EventExceptionTest {

    static class TestEvent extends Event {}

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造")
        void testMessageOnlyConstructor() {
            EventException ex = new EventException("Test error");

            assertThat(ex.getMessage()).isEqualTo("Test error");
            assertThat(ex.getCause()).isNull();
            assertThat(ex.getEvent()).isNull();
            assertThat(ex.getErrorCode()).isEqualTo(EventErrorCode.UNKNOWN);
        }

        @Test
        @DisplayName("消息和原因构造")
        void testMessageAndCauseConstructor() {
            RuntimeException cause = new RuntimeException("Cause");

            EventException ex = new EventException("Test error", cause);

            assertThat(ex.getMessage()).isEqualTo("Test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getEvent()).isNull();
            assertThat(ex.getErrorCode()).isEqualTo(EventErrorCode.UNKNOWN);
        }

        @Test
        @DisplayName("InterruptedException原因返回TIMEOUT错误码")
        void testInterruptedExceptionCause() {
            InterruptedException cause = new InterruptedException("Interrupted");

            EventException ex = new EventException("Test error", cause);

            assertThat(ex.getErrorCode()).isEqualTo(EventErrorCode.TIMEOUT);
        }

        @Test
        @DisplayName("消息和错误码构造")
        void testMessageAndErrorCodeConstructor() {
            EventException ex = new EventException("Publish failed", EventErrorCode.PUBLISH_FAILED);

            assertThat(ex.getMessage()).isEqualTo("Publish failed");
            assertThat(ex.getCause()).isNull();
            assertThat(ex.getEvent()).isNull();
            assertThat(ex.getErrorCode()).isEqualTo(EventErrorCode.PUBLISH_FAILED);
        }

        @Test
        @DisplayName("全参数构造")
        void testFullConstructor() {
            RuntimeException cause = new RuntimeException("Cause");
            TestEvent event = new TestEvent();

            EventException ex = new EventException("Test error", cause, event, EventErrorCode.LISTENER_ERROR);

            assertThat(ex.getMessage()).isEqualTo("Test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getEvent()).isEqualTo(event);
            assertThat(ex.getErrorCode()).isEqualTo(EventErrorCode.LISTENER_ERROR);
        }

        @Test
        @DisplayName("null错误码使用UNKNOWN")
        void testNullErrorCodeUsesUnknown() {
            EventException ex = new EventException("Test error", null, null, null);

            assertThat(ex.getErrorCode()).isEqualTo(EventErrorCode.UNKNOWN);
        }
    }

    @Nested
    @DisplayName("getErrorCode() 测试")
    class GetErrorCodeTests {

        @Test
        @DisplayName("返回设置的错误码")
        void testReturnsSetErrorCode() {
            EventException ex = new EventException("Error", EventErrorCode.STORE_ERROR);

            assertThat(ex.getErrorCode()).isEqualTo(EventErrorCode.STORE_ERROR);
        }
    }

    @Nested
    @DisplayName("getEvent() 测试")
    class GetEventTests {

        @Test
        @DisplayName("返回设置的事件")
        void testReturnsSetEvent() {
            TestEvent event = new TestEvent();
            EventException ex = new EventException("Error", null, event, EventErrorCode.UNKNOWN);

            assertThat(ex.getEvent()).isEqualTo(event);
        }

        @Test
        @DisplayName("无事件返回null")
        void testReturnsNullWhenNoEvent() {
            EventException ex = new EventException("Error");

            assertThat(ex.getEvent()).isNull();
        }
    }

    @Nested
    @DisplayName("RuntimeException继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("是RuntimeException的子类")
        void testIsRuntimeException() {
            EventException ex = new EventException("Error");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可以不捕获抛出")
        void testCanThrowUnchecked() {
            assertThatThrownBy(() -> {
                throw new EventException("Test");
            }).isInstanceOf(EventException.class);
        }
    }
}
