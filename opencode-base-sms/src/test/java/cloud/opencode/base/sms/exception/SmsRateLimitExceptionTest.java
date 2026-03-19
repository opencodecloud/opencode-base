package cloud.opencode.base.sms.exception;

import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * SmsRateLimitExceptionTest Tests
 * SmsRateLimitExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("SmsRateLimitException 测试")
class SmsRateLimitExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建频率限制异常")
        void testCreate() {
            SmsRateLimitException exception = new SmsRateLimitException("13800138000", Duration.ofMinutes(1));

            assertThat(exception.getErrorCode()).isEqualTo(SmsErrorCode.SEND_RATE_LIMITED);
            assertThat(exception.getPhone()).isEqualTo("13800138000");
            assertThat(exception.getRetryAfter()).isEqualTo(Duration.ofMinutes(1));
        }

        @Test
        @DisplayName("消息包含脱敏手机号")
        void testMessageContainsMaskedPhone() {
            SmsRateLimitException exception = new SmsRateLimitException("13800138000", Duration.ofMinutes(1));

            assertThat(exception.getMessage()).contains("***");
            assertThat(exception.getMessage()).doesNotContain("13800138000");
        }
    }

    @Nested
    @DisplayName("getter方法测试")
    class GetterTests {

        @Test
        @DisplayName("getPhone返回手机号")
        void testGetPhone() {
            SmsRateLimitException exception = new SmsRateLimitException("13800138000", Duration.ofSeconds(30));

            assertThat(exception.getPhone()).isEqualTo("13800138000");
        }

        @Test
        @DisplayName("getMaskedPhone返回脱敏手机号")
        void testGetMaskedPhone() {
            SmsRateLimitException exception = new SmsRateLimitException("13800138000", Duration.ofSeconds(30));

            String masked = exception.getMaskedPhone();

            assertThat(masked).contains("***");
            assertThat(masked).doesNotContain("0013800");
        }

        @Test
        @DisplayName("getRetryAfter返回等待时间")
        void testGetRetryAfter() {
            Duration retryAfter = Duration.ofMinutes(5);
            SmsRateLimitException exception = new SmsRateLimitException("13800138000", retryAfter);

            assertThat(exception.getRetryAfter()).isEqualTo(retryAfter);
        }

        @Test
        @DisplayName("getRetryAfterSeconds返回秒数")
        void testGetRetryAfterSeconds() {
            SmsRateLimitException exception = new SmsRateLimitException("13800138000", Duration.ofMinutes(2));

            assertThat(exception.getRetryAfterSeconds()).isEqualTo(120);
        }
    }
}
