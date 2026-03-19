package cloud.opencode.base.sms.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SmsSendExceptionTest Tests
 * SmsSendExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("SmsSendException 测试")
class SmsSendExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用错误码和手机号创建")
        void testCreateWithErrorCodeAndPhone() {
            SmsSendException exception = new SmsSendException(SmsErrorCode.SEND_FAILED, "13800138000");

            assertThat(exception.getErrorCode()).isEqualTo(SmsErrorCode.SEND_FAILED);
            assertThat(exception.getPhoneNumber()).isEqualTo("13800138000");
        }

        @Test
        @DisplayName("使用错误码、手机号和提供商代码创建")
        void testCreateWithProviderCode() {
            SmsSendException exception = new SmsSendException(
                    SmsErrorCode.PROVIDER_ERROR,
                    "13800138000",
                    "ALI_001"
            );

            assertThat(exception.getProviderCode()).isEqualTo("ALI_001");
        }

        @Test
        @DisplayName("使用错误码、手机号和原因创建")
        void testCreateWithCause() {
            Throwable cause = new RuntimeException("Network error");
            SmsSendException exception = new SmsSendException(SmsErrorCode.NETWORK_ERROR, "13800138000", cause);

            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getPhoneNumber()).isEqualTo("13800138000");
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("failed工厂方法")
        void testFailed() {
            Throwable cause = new RuntimeException("Error");
            SmsSendException exception = SmsSendException.failed("13800138000", cause);

            assertThat(exception.getErrorCode()).isEqualTo(SmsErrorCode.SEND_FAILED);
            assertThat(exception.getPhoneNumber()).isEqualTo("13800138000");
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("timeout工厂方法")
        void testTimeout() {
            SmsSendException exception = SmsSendException.timeout("13800138000");

            assertThat(exception.getErrorCode()).isEqualTo(SmsErrorCode.SEND_TIMEOUT);
            assertThat(exception.getPhoneNumber()).isEqualTo("13800138000");
        }

        @Test
        @DisplayName("rateLimited工厂方法")
        void testRateLimited() {
            SmsSendException exception = SmsSendException.rateLimited("13800138000");

            assertThat(exception.getErrorCode()).isEqualTo(SmsErrorCode.SEND_RATE_LIMITED);
            assertThat(exception.getPhoneNumber()).isEqualTo("13800138000");
        }
    }

    @Nested
    @DisplayName("getter方法测试")
    class GetterTests {

        @Test
        @DisplayName("getPhoneNumber返回手机号")
        void testGetPhoneNumber() {
            SmsSendException exception = new SmsSendException(SmsErrorCode.SEND_FAILED, "13800138000");

            assertThat(exception.getPhoneNumber()).isEqualTo("13800138000");
        }

        @Test
        @DisplayName("getProviderCode返回提供商代码")
        void testGetProviderCode() {
            SmsSendException exception = new SmsSendException(SmsErrorCode.PROVIDER_ERROR, "13800138000", "ERR_001");

            assertThat(exception.getProviderCode()).isEqualTo("ERR_001");
        }

        @Test
        @DisplayName("无提供商代码返回null")
        void testGetProviderCodeNull() {
            SmsSendException exception = new SmsSendException(SmsErrorCode.SEND_FAILED, "13800138000");

            assertThat(exception.getProviderCode()).isNull();
        }
    }
}
