package cloud.opencode.base.event.exception;

import cloud.opencode.base.event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * EventSecurityException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("EventSecurityException 测试")
class EventSecurityExceptionTest {

    static class TestEvent extends Event {}

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造")
        void testMessageOnlyConstructor() {
            EventSecurityException ex = new EventSecurityException("Security error");

            assertThat(ex.getMessage()).isEqualTo("Security error");
            assertThat(ex.getCause()).isNull();
            assertThat(ex.getErrorCode()).isEqualTo(EventErrorCode.SECURITY_VIOLATION);
        }

        @Test
        @DisplayName("消息和错误码构造")
        void testMessageAndErrorCodeConstructor() {
            EventSecurityException ex = new EventSecurityException(
                    "Rate limit exceeded", EventErrorCode.RATE_LIMITED);

            assertThat(ex.getMessage()).isEqualTo("Rate limit exceeded");
            assertThat(ex.getErrorCode()).isEqualTo(EventErrorCode.RATE_LIMITED);
        }

        @Test
        @DisplayName("消息和原因构造")
        void testMessageAndCauseConstructor() {
            RuntimeException cause = new RuntimeException("Cause");

            EventSecurityException ex = new EventSecurityException("Security error", cause);

            assertThat(ex.getMessage()).isEqualTo("Security error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getErrorCode()).isEqualTo(EventErrorCode.SECURITY_VIOLATION);
        }

        @Test
        @DisplayName("全参数构造")
        void testFullConstructor() {
            RuntimeException cause = new RuntimeException("Cause");
            TestEvent event = new TestEvent();

            EventSecurityException ex = new EventSecurityException(
                    "Security error", cause, event, EventErrorCode.VERIFICATION_FAILED);

            assertThat(ex.getMessage()).isEqualTo("Security error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getEvent()).isEqualTo(event);
            assertThat(ex.getErrorCode()).isEqualTo(EventErrorCode.VERIFICATION_FAILED);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("是EventException的子类")
        void testIsEventException() {
            EventSecurityException ex = new EventSecurityException("Error");

            assertThat(ex).isInstanceOf(EventException.class);
        }
    }
}
