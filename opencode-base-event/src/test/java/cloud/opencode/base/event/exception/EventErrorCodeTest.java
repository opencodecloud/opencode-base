package cloud.opencode.base.event.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * EventErrorCode 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
@DisplayName("EventErrorCode 测试")
class EventErrorCodeTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValueTests {

        @Test
        @DisplayName("所有枚举值存在")
        void testAllEnumValues() {
            assertThat(EventErrorCode.values()).contains(
                    EventErrorCode.UNKNOWN,
                    EventErrorCode.PUBLISH_FAILED,
                    EventErrorCode.EVENT_CANCELLED,
                    EventErrorCode.TIMEOUT,
                    EventErrorCode.LISTENER_ERROR,
                    EventErrorCode.REGISTRATION_FAILED,
                    EventErrorCode.DUPLICATE_LISTENER,
                    EventErrorCode.INVALID_LISTENER_METHOD,
                    EventErrorCode.STORE_ERROR,
                    EventErrorCode.PERSIST_FAILED,
                    EventErrorCode.REPLAY_FAILED,
                    EventErrorCode.VERIFICATION_FAILED,
                    EventErrorCode.RATE_LIMITED,
                    EventErrorCode.SECURITY_VIOLATION
            );
        }

        @Test
        @DisplayName("枚举值数量正确")
        void testEnumCount() {
            assertThat(EventErrorCode.values()).hasSize(14);
        }
    }

    @Nested
    @DisplayName("getCode() 测试")
    class GetCodeTests {

        @Test
        @DisplayName("UNKNOWN返回0")
        void testUnknownCode() {
            assertThat(EventErrorCode.UNKNOWN.getCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("发布错误码范围1xxx")
        void testPublishErrorCodeRange() {
            assertThat(EventErrorCode.PUBLISH_FAILED.getCode()).isEqualTo(1001);
            assertThat(EventErrorCode.EVENT_CANCELLED.getCode()).isEqualTo(1002);
            assertThat(EventErrorCode.TIMEOUT.getCode()).isEqualTo(1003);
        }

        @Test
        @DisplayName("监听器错误码范围2xxx")
        void testListenerErrorCodeRange() {
            assertThat(EventErrorCode.LISTENER_ERROR.getCode()).isEqualTo(2001);
            assertThat(EventErrorCode.REGISTRATION_FAILED.getCode()).isEqualTo(2002);
            assertThat(EventErrorCode.DUPLICATE_LISTENER.getCode()).isEqualTo(2003);
            assertThat(EventErrorCode.INVALID_LISTENER_METHOD.getCode()).isEqualTo(2004);
        }

        @Test
        @DisplayName("存储错误码范围3xxx")
        void testStoreErrorCodeRange() {
            assertThat(EventErrorCode.STORE_ERROR.getCode()).isEqualTo(3001);
            assertThat(EventErrorCode.PERSIST_FAILED.getCode()).isEqualTo(3002);
            assertThat(EventErrorCode.REPLAY_FAILED.getCode()).isEqualTo(3003);
        }

        @Test
        @DisplayName("安全错误码范围4xxx")
        void testSecurityErrorCodeRange() {
            assertThat(EventErrorCode.VERIFICATION_FAILED.getCode()).isEqualTo(4001);
            assertThat(EventErrorCode.RATE_LIMITED.getCode()).isEqualTo(4002);
            assertThat(EventErrorCode.SECURITY_VIOLATION.getCode()).isEqualTo(4003);
        }
    }

    @Nested
    @DisplayName("getDescription() 测试")
    class GetDescriptionTests {

        @Test
        @DisplayName("返回英文描述")
        void testEnglishDescription() {
            assertThat(EventErrorCode.UNKNOWN.getDescription()).isEqualTo("Unknown error");
            assertThat(EventErrorCode.PUBLISH_FAILED.getDescription()).isEqualTo("Publish failed");
            assertThat(EventErrorCode.LISTENER_ERROR.getDescription()).isEqualTo("Listener error");
        }
    }

    @Nested
    @DisplayName("getDescriptionCn() 测试")
    class GetDescriptionCnTests {

        @Test
        @DisplayName("返回中文描述")
        void testChineseDescription() {
            assertThat(EventErrorCode.UNKNOWN.getDescriptionCn()).isEqualTo("未知错误");
            assertThat(EventErrorCode.PUBLISH_FAILED.getDescriptionCn()).isEqualTo("发布失败");
            assertThat(EventErrorCode.LISTENER_ERROR.getDescriptionCn()).isEqualTo("监听器错误");
        }
    }

    @Nested
    @DisplayName("fromException() 测试")
    class FromExceptionTests {

        @Test
        @DisplayName("null返回UNKNOWN")
        void testNullReturnsUnknown() {
            assertThat(EventErrorCode.fromException(null)).isEqualTo(EventErrorCode.UNKNOWN);
        }

        @Test
        @DisplayName("InterruptedException返回TIMEOUT")
        void testInterruptedExceptionReturnsTimeout() {
            InterruptedException ex = new InterruptedException("test");

            assertThat(EventErrorCode.fromException(ex)).isEqualTo(EventErrorCode.TIMEOUT);
        }

        @Test
        @DisplayName("其他异常返回UNKNOWN")
        void testOtherExceptionReturnsUnknown() {
            RuntimeException ex = new RuntimeException("test");

            assertThat(EventErrorCode.fromException(ex)).isEqualTo(EventErrorCode.UNKNOWN);
        }
    }
}
