package cloud.opencode.base.email.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailTemplateException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailTemplateException 测试")
class EmailTemplateExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用消息创建异常")
        void testConstructorWithMessage() {
            EmailTemplateException exception = new EmailTemplateException("Template not found");

            assertThat(exception.getRawMessage()).isEqualTo("Template not found");
            assertThat(exception.getEmailErrorCode()).isEqualTo(EmailErrorCode.TEMPLATE_ERROR);
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("使用消息和原因创建异常")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("Parse error");
            EmailTemplateException exception = new EmailTemplateException("Invalid template syntax", cause);

            assertThat(exception.getRawMessage()).isEqualTo("Invalid template syntax");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getEmailErrorCode()).isEqualTo(EmailErrorCode.TEMPLATE_ERROR);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("是EmailException的子类")
        void testIsEmailException() {
            EmailTemplateException exception = new EmailTemplateException("Test");
            assertThat(exception).isInstanceOf(EmailException.class);
        }

        @Test
        @DisplayName("是RuntimeException的子类")
        void testIsRuntimeException() {
            EmailTemplateException exception = new EmailTemplateException("Test");
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可以被捕获为EmailException")
        void testCanBeCaughtAsEmailException() {
            try {
                throw new EmailTemplateException("Template error");
            } catch (EmailException e) {
                assertThat(e).isInstanceOf(EmailTemplateException.class);
                assertThat(e.getEmailErrorCode()).isEqualTo(EmailErrorCode.TEMPLATE_ERROR);
            }
        }
    }

    @Nested
    @DisplayName("isRetryable() 测试")
    class IsRetryableTests {

        @Test
        @DisplayName("模板异常不可重试")
        void testIsNotRetryable() {
            EmailTemplateException exception = new EmailTemplateException("Template error");
            assertThat(exception.isRetryable()).isFalse();
        }
    }

    @Nested
    @DisplayName("模板场景测试")
    class TemplateScenarioTests {

        @Test
        @DisplayName("模板未找到")
        void testTemplateNotFound() {
            EmailTemplateException exception = new EmailTemplateException("Template not found: welcome.html");

            assertThat(exception.getMessage()).contains("welcome.html");
            assertThat(exception.getEmailErrorCode()).isEqualTo(EmailErrorCode.TEMPLATE_ERROR);
        }

        @Test
        @DisplayName("无效的模板语法")
        void testInvalidTemplateSyntax() {
            Throwable cause = new RuntimeException("Unclosed variable: ${name");
            EmailTemplateException exception = new EmailTemplateException("Invalid template syntax", cause);

            assertThat(exception.getMessage()).contains("syntax");
            assertThat(exception.getCause().getMessage()).contains("Unclosed");
        }

        @Test
        @DisplayName("缺少必需变量")
        void testMissingRequiredVariable() {
            EmailTemplateException exception = new EmailTemplateException("Missing required variable: userName");

            assertThat(exception.getMessage()).contains("userName");
            assertThat(exception.getEmailErrorCode()).isEqualTo(EmailErrorCode.TEMPLATE_ERROR);
        }

        @Test
        @DisplayName("变量类型不匹配")
        void testVariableTypeMismatch() {
            EmailTemplateException exception = new EmailTemplateException("Variable type mismatch: expected String, got Integer for 'count'");

            assertThat(exception.getMessage()).contains("type mismatch");
            assertThat(exception.getEmailErrorCode()).isEqualTo(EmailErrorCode.TEMPLATE_ERROR);
        }
    }

    @Nested
    @DisplayName("getEmail() 测试")
    class GetEmailTests {

        @Test
        @DisplayName("getEmail() 返回null")
        void testGetEmailReturnsNull() {
            EmailTemplateException exception = new EmailTemplateException("Test");
            assertThat(exception.getEmail()).isNull();
        }
    }
}
