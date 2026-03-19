package cloud.opencode.base.email.exception;

import cloud.opencode.base.email.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailException 测试")
class EmailExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅使用消息创建异常")
        void testConstructorWithMessage() {
            EmailException exception = new EmailException("Test error");

            assertThat(exception.getMessage()).isEqualTo("Test error");
            assertThat(exception.getErrorCode()).isEqualTo(EmailErrorCode.UNKNOWN);
            assertThat(exception.getEmail()).isNull();
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("使用消息和原因创建异常")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("Root cause");
            EmailException exception = new EmailException("Test error", cause);

            assertThat(exception.getMessage()).isEqualTo("Test error");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getErrorCode()).isEqualTo(EmailErrorCode.UNKNOWN);
        }

        @Test
        @DisplayName("使用消息和错误码创建异常")
        void testConstructorWithMessageAndErrorCode() {
            EmailException exception = new EmailException("Connection failed", EmailErrorCode.CONNECTION_FAILED);

            assertThat(exception.getMessage()).isEqualTo("Connection failed");
            assertThat(exception.getErrorCode()).isEqualTo(EmailErrorCode.CONNECTION_FAILED);
            assertThat(exception.getEmail()).isNull();
        }

        @Test
        @DisplayName("使用所有参数创建异常")
        void testConstructorWithAllParameters() {
            Throwable cause = new RuntimeException("Root cause");
            Email email = Email.builder().to("test@example.com").subject("Test").build();

            EmailException exception = new EmailException("Send failed", cause, email, EmailErrorCode.SEND_TIMEOUT);

            assertThat(exception.getMessage()).isEqualTo("Send failed");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getEmail()).isEqualTo(email);
            assertThat(exception.getErrorCode()).isEqualTo(EmailErrorCode.SEND_TIMEOUT);
        }

        @Test
        @DisplayName("空错误码使用UNKNOWN")
        void testNullErrorCodeUsesUnknown() {
            EmailException exception = new EmailException("Test", null, null, null);

            assertThat(exception.getErrorCode()).isEqualTo(EmailErrorCode.UNKNOWN);
        }
    }

    @Nested
    @DisplayName("isRetryable() 测试")
    class IsRetryableTests {

        @Test
        @DisplayName("连接失败错误可重试")
        void testConnectionFailedIsRetryable() {
            EmailException exception = new EmailException("Connection failed", EmailErrorCode.CONNECTION_FAILED);
            assertThat(exception.isRetryable()).isTrue();
        }

        @Test
        @DisplayName("连接超时错误可重试")
        void testConnectionTimeoutIsRetryable() {
            EmailException exception = new EmailException("Timeout", EmailErrorCode.CONNECTION_TIMEOUT);
            assertThat(exception.isRetryable()).isTrue();
        }

        @Test
        @DisplayName("认证失败错误不可重试")
        void testAuthFailedIsNotRetryable() {
            EmailException exception = new EmailException("Auth failed", EmailErrorCode.AUTH_FAILED);
            assertThat(exception.isRetryable()).isFalse();
        }

        @Test
        @DisplayName("配置无效错误不可重试")
        void testConfigInvalidIsNotRetryable() {
            EmailException exception = new EmailException("Config invalid", EmailErrorCode.CONFIG_INVALID);
            assertThat(exception.isRetryable()).isFalse();
        }

        @Test
        @DisplayName("未知错误不可重试")
        void testUnknownIsNotRetryable() {
            EmailException exception = new EmailException("Unknown error");
            assertThat(exception.isRetryable()).isFalse();
        }
    }

    @Nested
    @DisplayName("继承RuntimeException测试")
    class InheritanceTests {

        @Test
        @DisplayName("是RuntimeException的子类")
        void testIsRuntimeException() {
            EmailException exception = new EmailException("Test");
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可以被捕获为Exception")
        void testCanBeCaughtAsException() {
            try {
                throw new EmailException("Test error");
            } catch (Exception e) {
                assertThat(e).isInstanceOf(EmailException.class);
                assertThat(e.getMessage()).isEqualTo("Test error");
            }
        }
    }
}
