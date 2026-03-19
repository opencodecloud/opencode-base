package cloud.opencode.base.sms.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SmsTemplateExceptionTest Tests
 * SmsTemplateExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("SmsTemplateException 测试")
class SmsTemplateExceptionTest {

    @Nested
    @DisplayName("notFound工厂方法测试")
    class NotFoundTests {

        @Test
        @DisplayName("创建模板未找到异常")
        void testNotFound() {
            SmsTemplateException exception = SmsTemplateException.notFound("verify-code");

            assertThat(exception.getErrorCode()).isEqualTo(SmsErrorCode.TEMPLATE_NOT_FOUND);
            assertThat(exception.getTemplateId()).isEqualTo("verify-code");
        }
    }

    @Nested
    @DisplayName("invalid工厂方法测试")
    class InvalidTests {

        @Test
        @DisplayName("创建模板无效异常")
        void testInvalid() {
            SmsTemplateException exception = SmsTemplateException.invalid("broken-template");

            assertThat(exception.getErrorCode()).isEqualTo(SmsErrorCode.TEMPLATE_INVALID);
            assertThat(exception.getTemplateId()).isEqualTo("broken-template");
        }
    }

    @Nested
    @DisplayName("variableMissing工厂方法测试")
    class VariableMissingTests {

        @Test
        @DisplayName("创建变量缺失异常")
        void testVariableMissing() {
            SmsTemplateException exception = SmsTemplateException.variableMissing("verify", "code");

            assertThat(exception.getErrorCode()).isEqualTo(SmsErrorCode.TEMPLATE_VARIABLE_MISSING);
            assertThat(exception.getMessage()).contains("verify");
            assertThat(exception.getMessage()).contains("code");
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("是SmsException的子类")
        void testInheritance() {
            SmsTemplateException exception = SmsTemplateException.notFound("test");

            assertThat(exception).isInstanceOf(SmsException.class);
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }
}
