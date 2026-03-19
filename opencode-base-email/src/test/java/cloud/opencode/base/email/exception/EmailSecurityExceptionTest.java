package cloud.opencode.base.email.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailSecurityException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailSecurityException 测试")
class EmailSecurityExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用消息创建异常")
        void testConstructorWithMessage() {
            EmailSecurityException exception = new EmailSecurityException("Header injection detected");

            assertThat(exception.getMessage()).isEqualTo("Header injection detected");
            assertThat(exception.getErrorCode()).isEqualTo(EmailErrorCode.HEADER_INJECTION);
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("使用消息和错误码创建异常")
        void testConstructorWithMessageAndErrorCode() {
            EmailSecurityException exception = new EmailSecurityException("Invalid attachment", EmailErrorCode.INVALID_ATTACHMENT);

            assertThat(exception.getMessage()).isEqualTo("Invalid attachment");
            assertThat(exception.getErrorCode()).isEqualTo(EmailErrorCode.INVALID_ATTACHMENT);
        }

        @Test
        @DisplayName("使用消息和原因创建异常")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("Malicious content");
            EmailSecurityException exception = new EmailSecurityException("Security violation", cause);

            assertThat(exception.getMessage()).isEqualTo("Security violation");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getErrorCode()).isEqualTo(EmailErrorCode.HEADER_INJECTION);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("是EmailException的子类")
        void testIsEmailException() {
            EmailSecurityException exception = new EmailSecurityException("Test");
            assertThat(exception).isInstanceOf(EmailException.class);
        }

        @Test
        @DisplayName("是RuntimeException的子类")
        void testIsRuntimeException() {
            EmailSecurityException exception = new EmailSecurityException("Test");
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可以被捕获为EmailException")
        void testCanBeCaughtAsEmailException() {
            try {
                throw new EmailSecurityException("Security error");
            } catch (EmailException e) {
                assertThat(e).isInstanceOf(EmailSecurityException.class);
            }
        }
    }

    @Nested
    @DisplayName("isRetryable() 测试")
    class IsRetryableTests {

        @Test
        @DisplayName("头注入异常不可重试")
        void testHeaderInjectionNotRetryable() {
            EmailSecurityException exception = new EmailSecurityException("Header injection");
            assertThat(exception.isRetryable()).isFalse();
        }

        @Test
        @DisplayName("无效附件异常不可重试")
        void testInvalidAttachmentNotRetryable() {
            EmailSecurityException exception = new EmailSecurityException("Invalid attachment", EmailErrorCode.INVALID_ATTACHMENT);
            assertThat(exception.isRetryable()).isFalse();
        }
    }

    @Nested
    @DisplayName("安全场景测试")
    class SecurityScenarioTests {

        @Test
        @DisplayName("检测换行符注入")
        void testNewlineInjection() {
            String maliciousSubject = "Normal Subject\r\nBcc: attacker@evil.com";
            EmailSecurityException exception = new EmailSecurityException(
                    "Header injection detected in subject: " + maliciousSubject
            );

            assertThat(exception.getMessage()).contains("Header injection");
            assertThat(exception.getErrorCode()).isEqualTo(EmailErrorCode.HEADER_INJECTION);
        }

        @Test
        @DisplayName("检测可执行附件")
        void testExecutableAttachment() {
            EmailSecurityException exception = new EmailSecurityException(
                    "Executable attachment not allowed: virus.exe",
                    EmailErrorCode.INVALID_ATTACHMENT
            );

            assertThat(exception.getMessage()).contains("virus.exe");
            assertThat(exception.getErrorCode()).isEqualTo(EmailErrorCode.INVALID_ATTACHMENT);
        }

        @Test
        @DisplayName("检测附件大小超限")
        void testAttachmentSizeExceeded() {
            EmailSecurityException exception = new EmailSecurityException(
                    "Attachment size exceeds limit: 100MB > 25MB",
                    EmailErrorCode.INVALID_ATTACHMENT
            );

            assertThat(exception.getMessage()).contains("100MB");
            assertThat(exception.getErrorCode()).isEqualTo(EmailErrorCode.INVALID_ATTACHMENT);
        }
    }

    @Nested
    @DisplayName("getEmail() 测试")
    class GetEmailTests {

        @Test
        @DisplayName("getEmail() 返回null")
        void testGetEmailReturnsNull() {
            EmailSecurityException exception = new EmailSecurityException("Test");
            assertThat(exception.getEmail()).isNull();
        }
    }
}
