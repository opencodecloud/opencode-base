package cloud.opencode.base.email.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailConfigException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailConfigException 测试")
class EmailConfigExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用消息创建异常")
        void testConstructorWithMessage() {
            EmailConfigException exception = new EmailConfigException("Invalid SMTP host");

            assertThat(exception.getRawMessage()).isEqualTo("Invalid SMTP host");
            assertThat(exception.getEmailErrorCode()).isEqualTo(EmailErrorCode.CONFIG_INVALID);
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("使用消息和原因创建异常")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new IllegalArgumentException("Port out of range");
            EmailConfigException exception = new EmailConfigException("Invalid port", cause);

            assertThat(exception.getRawMessage()).isEqualTo("Invalid port");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getEmailErrorCode()).isEqualTo(EmailErrorCode.CONFIG_INVALID);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("是EmailException的子类")
        void testIsEmailException() {
            EmailConfigException exception = new EmailConfigException("Test");
            assertThat(exception).isInstanceOf(EmailException.class);
        }

        @Test
        @DisplayName("是RuntimeException的子类")
        void testIsRuntimeException() {
            EmailConfigException exception = new EmailConfigException("Test");
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可以被捕获为EmailException")
        void testCanBeCaughtAsEmailException() {
            try {
                throw new EmailConfigException("Config error");
            } catch (EmailException e) {
                assertThat(e).isInstanceOf(EmailConfigException.class);
                assertThat(e.getEmailErrorCode()).isEqualTo(EmailErrorCode.CONFIG_INVALID);
            }
        }
    }

    @Nested
    @DisplayName("isRetryable() 测试")
    class IsRetryableTests {

        @Test
        @DisplayName("配置异常不可重试")
        void testIsNotRetryable() {
            EmailConfigException exception = new EmailConfigException("Invalid config");
            assertThat(exception.isRetryable()).isFalse();
        }
    }

    @Nested
    @DisplayName("getEmail() 测试")
    class GetEmailTests {

        @Test
        @DisplayName("getEmail() 返回null")
        void testGetEmailReturnsNull() {
            EmailConfigException exception = new EmailConfigException("Test");
            assertThat(exception.getEmail()).isNull();
        }
    }
}
