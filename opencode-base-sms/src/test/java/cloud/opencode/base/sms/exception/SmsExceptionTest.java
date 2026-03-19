package cloud.opencode.base.sms.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SmsExceptionTest Tests
 * SmsExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("SmsException 测试")
class SmsExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用错误码创建")
        void testCreateWithErrorCode() {
            SmsException exception = new SmsException(SmsErrorCode.SEND_FAILED);

            assertThat(exception.getErrorCode()).isEqualTo(SmsErrorCode.SEND_FAILED);
            assertThat(exception.getMessage()).isEqualTo(SmsErrorCode.SEND_FAILED.getMessage());
        }

        @Test
        @DisplayName("使用错误码和消息创建")
        void testCreateWithErrorCodeAndMessage() {
            SmsException exception = new SmsException(SmsErrorCode.SEND_FAILED, "Custom message");

            assertThat(exception.getErrorCode()).isEqualTo(SmsErrorCode.SEND_FAILED);
            assertThat(exception.getMessage()).isEqualTo("Custom message");
        }

        @Test
        @DisplayName("使用错误码和原因创建")
        void testCreateWithErrorCodeAndCause() {
            Throwable cause = new RuntimeException("Original error");
            SmsException exception = new SmsException(SmsErrorCode.NETWORK_ERROR, cause);

            assertThat(exception.getErrorCode()).isEqualTo(SmsErrorCode.NETWORK_ERROR);
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("使用错误码、消息和原因创建")
        void testCreateWithAll() {
            Throwable cause = new RuntimeException("Original error");
            SmsException exception = new SmsException(SmsErrorCode.SEND_TIMEOUT, "Timeout occurred", cause);

            assertThat(exception.getErrorCode()).isEqualTo(SmsErrorCode.SEND_TIMEOUT);
            assertThat(exception.getMessage()).isEqualTo("Timeout occurred");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("getCode方法测试")
    class GetCodeTests {

        @Test
        @DisplayName("返回错误码值")
        void testGetCode() {
            SmsException exception = new SmsException(SmsErrorCode.SEND_FAILED);

            assertThat(exception.getCode()).isEqualTo(SmsErrorCode.SEND_FAILED.getCode());
        }
    }

    @Nested
    @DisplayName("getErrorCode方法测试")
    class GetErrorCodeTests {

        @Test
        @DisplayName("返回错误码枚举")
        void testGetErrorCode() {
            SmsException exception = new SmsException(SmsErrorCode.INVALID_PHONE_NUMBER);

            assertThat(exception.getErrorCode()).isEqualTo(SmsErrorCode.INVALID_PHONE_NUMBER);
        }
    }
}
