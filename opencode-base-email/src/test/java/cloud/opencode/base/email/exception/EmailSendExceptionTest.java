package cloud.opencode.base.email.exception;

import cloud.opencode.base.email.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailSendException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailSendException 测试")
class EmailSendExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用消息创建异常")
        void testConstructorWithMessage() {
            EmailSendException exception = new EmailSendException("Send failed");

            assertThat(exception.getRawMessage()).isEqualTo("Send failed");
            assertThat(exception.getEmailErrorCode()).isEqualTo(EmailErrorCode.UNKNOWN);
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getEmail()).isNull();
        }

        @Test
        @DisplayName("使用消息和原因创建异常")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("Connection refused");
            EmailSendException exception = new EmailSendException("Send failed", cause);

            assertThat(exception.getRawMessage()).isEqualTo("Send failed");
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("使用所有参数创建异常")
        void testConstructorWithAllParameters() {
            Throwable cause = new RuntimeException("Timeout");
            Email email = Email.builder().to("test@example.com").subject("Test").build();

            EmailSendException exception = new EmailSendException(
                    "Send timeout",
                    cause,
                    email,
                    EmailErrorCode.SEND_TIMEOUT
            );

            assertThat(exception.getRawMessage()).isEqualTo("Send timeout");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getEmail()).isEqualTo(email);
            assertThat(exception.getEmailErrorCode()).isEqualTo(EmailErrorCode.SEND_TIMEOUT);
        }

        @Test
        @DisplayName("使用邮件和错误码创建异常")
        void testConstructorWithEmailAndErrorCode() {
            Email email = Email.builder().to("test@example.com").subject("Test").build();

            EmailSendException exception = new EmailSendException(
                    "Recipient rejected",
                    email,
                    EmailErrorCode.RECIPIENT_REJECTED
            );

            assertThat(exception.getRawMessage()).isEqualTo("Recipient rejected");
            assertThat(exception.getEmail()).isEqualTo(email);
            assertThat(exception.getEmailErrorCode()).isEqualTo(EmailErrorCode.RECIPIENT_REJECTED);
            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("是EmailException的子类")
        void testIsEmailException() {
            EmailSendException exception = new EmailSendException("Test");
            assertThat(exception).isInstanceOf(EmailException.class);
        }

        @Test
        @DisplayName("可以被捕获为EmailException")
        void testCanBeCaughtAsEmailException() {
            try {
                throw new EmailSendException("Send error");
            } catch (EmailException e) {
                assertThat(e).isInstanceOf(EmailSendException.class);
            }
        }
    }

    @Nested
    @DisplayName("isRetryable() 测试")
    class IsRetryableTests {

        @Test
        @DisplayName("超时错误可重试")
        void testTimeoutIsRetryable() {
            Email email = Email.builder().to("test@example.com").subject("Test").build();
            EmailSendException exception = new EmailSendException("Timeout", email, EmailErrorCode.SEND_TIMEOUT);
            assertThat(exception.isRetryable()).isTrue();
        }

        @Test
        @DisplayName("连接失败可重试")
        void testConnectionFailedIsRetryable() {
            Email email = Email.builder().to("test@example.com").subject("Test").build();
            EmailSendException exception = new EmailSendException("Connection failed", email, EmailErrorCode.CONNECTION_FAILED);
            assertThat(exception.isRetryable()).isTrue();
        }

        @Test
        @DisplayName("收件人被拒绝不可重试")
        void testRecipientRejectedNotRetryable() {
            Email email = Email.builder().to("invalid@example.com").subject("Test").build();
            EmailSendException exception = new EmailSendException("Rejected", email, EmailErrorCode.RECIPIENT_REJECTED);
            assertThat(exception.isRetryable()).isFalse();
        }

        @Test
        @DisplayName("邮件被拒绝不可重试")
        void testMessageRejectedNotRetryable() {
            Email email = Email.builder().to("test@example.com").subject("Test").build();
            EmailSendException exception = new EmailSendException("Message rejected", email, EmailErrorCode.MESSAGE_REJECTED);
            assertThat(exception.isRetryable()).isFalse();
        }

        @Test
        @DisplayName("速率限制可重试")
        void testRateLimitedIsRetryable() {
            Email email = Email.builder().to("test@example.com").subject("Test").build();
            EmailSendException exception = new EmailSendException("Rate limited", email, EmailErrorCode.RATE_LIMITED);
            assertThat(exception.isRetryable()).isTrue();
        }

        @Test
        @DisplayName("邮箱已满可重试")
        void testMailboxFullIsRetryable() {
            Email email = Email.builder().to("test@example.com").subject("Test").build();
            EmailSendException exception = new EmailSendException("Mailbox full", email, EmailErrorCode.MAILBOX_FULL);
            assertThat(exception.isRetryable()).isTrue();
        }
    }

    @Nested
    @DisplayName("getEmail() 测试")
    class GetEmailTests {

        @Test
        @DisplayName("能够获取相关邮件")
        void testGetEmail() {
            Email email = Email.builder()
                    .to("test@example.com")
                    .subject("Test Subject")
                    .text("Test content")
                    .build();

            EmailSendException exception = new EmailSendException("Send failed", email, EmailErrorCode.UNKNOWN);

            assertThat(exception.getEmail()).isNotNull();
            assertThat(exception.getEmail().to()).contains("test@example.com");
            assertThat(exception.getEmail().subject()).isEqualTo("Test Subject");
        }
    }
}
