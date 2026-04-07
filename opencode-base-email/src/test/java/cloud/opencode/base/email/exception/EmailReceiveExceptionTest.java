package cloud.opencode.base.email.exception;

import cloud.opencode.base.email.ReceivedEmail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailReceiveException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailReceiveException 测试")
class EmailReceiveExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用消息创建异常")
        void testConstructorWithMessage() {
            EmailReceiveException exception = new EmailReceiveException("Receive failed");

            assertThat(exception.getRawMessage()).isEqualTo("Receive failed");
            assertThat(exception.getEmailErrorCode()).isEqualTo(EmailErrorCode.UNKNOWN);
            assertThat(exception.getReceivedEmail()).isNull();
            assertThat(exception.getFolder()).isNull();
            assertThat(exception.getMessageId()).isNull();
        }

        @Test
        @DisplayName("使用消息和错误码创建异常")
        void testConstructorWithMessageAndErrorCode() {
            EmailReceiveException exception = new EmailReceiveException("Folder not found", EmailErrorCode.FOLDER_NOT_FOUND);

            assertThat(exception.getRawMessage()).isEqualTo("Folder not found");
            assertThat(exception.getEmailErrorCode()).isEqualTo(EmailErrorCode.FOLDER_NOT_FOUND);
        }

        @Test
        @DisplayName("使用消息和原因创建异常")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("Connection reset");
            EmailReceiveException exception = new EmailReceiveException("Receive failed", cause);

            assertThat(exception.getRawMessage()).isEqualTo("Receive failed");
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("使用消息、原因和错误码创建异常")
        void testConstructorWithMessageCauseAndErrorCode() {
            Throwable cause = new RuntimeException("Timeout");
            EmailReceiveException exception = new EmailReceiveException("Receive timeout", cause, EmailErrorCode.RECEIVE_TIMEOUT);

            assertThat(exception.getRawMessage()).isEqualTo("Receive timeout");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getEmailErrorCode()).isEqualTo(EmailErrorCode.RECEIVE_TIMEOUT);
        }

        @Test
        @DisplayName("使用完整上下文创建异常")
        void testConstructorWithFullContext() {
            Throwable cause = new RuntimeException("Parse error");
            ReceivedEmail email = ReceivedEmail.builder()
                    .messageId("<123@test.com>")
                    .subject("Test")
                    .build();

            EmailReceiveException exception = new EmailReceiveException(
                    "Parse failed",
                    cause,
                    EmailErrorCode.MESSAGE_PARSE_FAILED,
                    "INBOX",
                    "<123@test.com>",
                    email
            );

            assertThat(exception.getRawMessage()).isEqualTo("Parse failed");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getEmailErrorCode()).isEqualTo(EmailErrorCode.MESSAGE_PARSE_FAILED);
            assertThat(exception.getFolder()).isEqualTo("INBOX");
            assertThat(exception.getMessageId()).isEqualTo("<123@test.com>");
            assertThat(exception.getReceivedEmail()).isEqualTo(email);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("folderNotFound() 创建文件夹未找到异常")
        void testFolderNotFound() {
            EmailReceiveException exception = EmailReceiveException.folderNotFound("SENT");

            assertThat(exception.getMessage()).contains("SENT");
            assertThat(exception.getEmailErrorCode()).isEqualTo(EmailErrorCode.FOLDER_NOT_FOUND);
            assertThat(exception.getFolder()).isEqualTo("SENT");
            assertThat(exception.getMessageId()).isNull();
            assertThat(exception.isRetryable()).isFalse();
        }

        @Test
        @DisplayName("messageNotFound() 创建消息未找到异常")
        void testMessageNotFound() {
            EmailReceiveException exception = EmailReceiveException.messageNotFound("<abc123@example.com>");

            assertThat(exception.getMessage()).contains("<abc123@example.com>");
            assertThat(exception.getEmailErrorCode()).isEqualTo(EmailErrorCode.MESSAGE_NOT_FOUND);
            assertThat(exception.getMessageId()).isEqualTo("<abc123@example.com>");
            assertThat(exception.getFolder()).isNull();
            assertThat(exception.isRetryable()).isFalse();
        }

        @Test
        @DisplayName("connectionLost() 创建连接丢失异常")
        void testConnectionLost() {
            Throwable cause = new RuntimeException("Connection reset by peer");
            EmailReceiveException exception = EmailReceiveException.connectionLost(cause);

            assertThat(exception.getMessage()).contains("Connection lost");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getEmailErrorCode()).isEqualTo(EmailErrorCode.CONNECTION_LOST);
            assertThat(exception.isRetryable()).isTrue();
        }

        @Test
        @DisplayName("timeout() 创建超时异常")
        void testTimeout() {
            EmailReceiveException exception = EmailReceiveException.timeout();

            assertThat(exception.getMessage()).contains("timed out");
            assertThat(exception.getEmailErrorCode()).isEqualTo(EmailErrorCode.RECEIVE_TIMEOUT);
            assertThat(exception.isRetryable()).isTrue();
        }

        @Test
        @DisplayName("parseFailed() 创建解析失败异常")
        void testParseFailed() {
            Throwable cause = new RuntimeException("Invalid MIME structure");
            EmailReceiveException exception = EmailReceiveException.parseFailed("<msg@test.com>", cause);

            assertThat(exception.getMessage()).contains("<msg@test.com>");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getEmailErrorCode()).isEqualTo(EmailErrorCode.MESSAGE_PARSE_FAILED);
            assertThat(exception.getMessageId()).isEqualTo("<msg@test.com>");
            assertThat(exception.isRetryable()).isFalse();
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("是EmailException的子类")
        void testIsEmailException() {
            EmailReceiveException exception = new EmailReceiveException("Test");
            assertThat(exception).isInstanceOf(EmailException.class);
        }

        @Test
        @DisplayName("可以被捕获为EmailException")
        void testCanBeCaughtAsEmailException() {
            try {
                throw new EmailReceiveException("Receive error");
            } catch (EmailException e) {
                assertThat(e).isInstanceOf(EmailReceiveException.class);
            }
        }
    }

    @Nested
    @DisplayName("isRetryable() 测试")
    class IsRetryableTests {

        @Test
        @DisplayName("接收超时可重试")
        void testReceiveTimeoutIsRetryable() {
            EmailReceiveException exception = new EmailReceiveException("Timeout", EmailErrorCode.RECEIVE_TIMEOUT);
            assertThat(exception.isRetryable()).isTrue();
        }

        @Test
        @DisplayName("连接丢失可重试")
        void testConnectionLostIsRetryable() {
            EmailReceiveException exception = EmailReceiveException.connectionLost(new RuntimeException());
            assertThat(exception.isRetryable()).isTrue();
        }

        @Test
        @DisplayName("附件下载失败可重试")
        void testAttachmentDownloadFailedIsRetryable() {
            EmailReceiveException exception = new EmailReceiveException("Download failed", EmailErrorCode.ATTACHMENT_DOWNLOAD_FAILED);
            assertThat(exception.isRetryable()).isTrue();
        }

        @Test
        @DisplayName("文件夹未找到不可重试")
        void testFolderNotFoundNotRetryable() {
            EmailReceiveException exception = EmailReceiveException.folderNotFound("NONEXISTENT");
            assertThat(exception.isRetryable()).isFalse();
        }

        @Test
        @DisplayName("消息未找到不可重试")
        void testMessageNotFoundNotRetryable() {
            EmailReceiveException exception = EmailReceiveException.messageNotFound("<123>");
            assertThat(exception.isRetryable()).isFalse();
        }

        @Test
        @DisplayName("消息解析失败不可重试")
        void testParseFailedNotRetryable() {
            EmailReceiveException exception = EmailReceiveException.parseFailed("<123>", new RuntimeException());
            assertThat(exception.isRetryable()).isFalse();
        }

        @Test
        @DisplayName("文件夹访问被拒绝不可重试")
        void testFolderAccessDeniedNotRetryable() {
            EmailReceiveException exception = new EmailReceiveException("Access denied", EmailErrorCode.FOLDER_ACCESS_DENIED);
            assertThat(exception.isRetryable()).isFalse();
        }

        @Test
        @DisplayName("IDLE不支持不可重试")
        void testIdleNotSupportedNotRetryable() {
            EmailReceiveException exception = new EmailReceiveException("IDLE not supported", EmailErrorCode.IDLE_NOT_SUPPORTED);
            assertThat(exception.isRetryable()).isFalse();
        }

        @Test
        @DisplayName("协议不支持不可重试")
        void testProtocolNotSupportedNotRetryable() {
            EmailReceiveException exception = new EmailReceiveException("Protocol not supported", EmailErrorCode.PROTOCOL_NOT_SUPPORTED);
            assertThat(exception.isRetryable()).isFalse();
        }
    }

    @Nested
    @DisplayName("getEmail() 测试")
    class GetEmailTests {

        @Test
        @DisplayName("getEmail() 返回null（使用父类方法）")
        void testGetEmailReturnsNull() {
            EmailReceiveException exception = new EmailReceiveException("Test");
            assertThat(exception.getEmail()).isNull();
        }
    }
}
