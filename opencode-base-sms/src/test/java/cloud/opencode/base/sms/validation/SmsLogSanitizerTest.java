package cloud.opencode.base.sms.validation;

import cloud.opencode.base.sms.message.SmsMessage;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * SmsLogSanitizerTest Tests
 * SmsLogSanitizerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("SmsLogSanitizer 测试")
class SmsLogSanitizerTest {

    @Nested
    @DisplayName("sanitize方法测试")
    class SanitizeTests {

        @Test
        @DisplayName("遮盖SmsMessage中的手机号")
        void testSanitizeMessage() {
            SmsMessage message = SmsMessage.of("13800138000", "Test content");

            String sanitized = SmsLogSanitizer.sanitize(message);

            assertThat(sanitized).doesNotContain("13800138000");
            assertThat(sanitized).contains("***");
        }

        @Test
        @DisplayName("null消息返回null字符串")
        void testSanitizeNull() {
            String sanitized = SmsLogSanitizer.sanitize(null);

            assertThat(sanitized).isEqualTo("null");
        }

        @Test
        @DisplayName("内容被替换为[CONTENT]")
        void testSanitizeContentHidden() {
            SmsMessage message = SmsMessage.of("13800138000", "Sensitive content here");

            String sanitized = SmsLogSanitizer.sanitize(message);

            assertThat(sanitized).contains("[CONTENT]");
            assertThat(sanitized).doesNotContain("Sensitive content");
        }
    }

    @Nested
    @DisplayName("sanitizeConfig方法测试")
    class SanitizeConfigTests {

        @Test
        @DisplayName("遮盖密钥")
        void testSanitizeConfigSecret() {
            Map<String, String> config = Map.of(
                    "accessKey", "AKID123456789",
                    "secretKey", "topsecretkey123",
                    "region", "cn-hangzhou"
            );

            Map<String, String> sanitized = SmsLogSanitizer.sanitizeConfig(config);

            assertThat(sanitized.get("secretKey")).isEqualTo("***");
            assertThat(sanitized.get("region")).isEqualTo("cn-hangzhou");
        }

        @Test
        @DisplayName("null配置返回空Map")
        void testSanitizeConfigNull() {
            Map<String, String> sanitized = SmsLogSanitizer.sanitizeConfig(null);

            assertThat(sanitized).isEmpty();
        }

        @Test
        @DisplayName("遮盖包含敏感关键字的键")
        void testSanitizeConfigSensitiveKeys() {
            Map<String, String> config = Map.of(
                    "appKey", "value1",
                    "appSecret", "value2",
                    "password", "value3",
                    "normalKey", "value4"
            );

            Map<String, String> sanitized = SmsLogSanitizer.sanitizeConfig(config);

            assertThat(sanitized.get("appKey")).isEqualTo("***");
            assertThat(sanitized.get("appSecret")).isEqualTo("***");
            assertThat(sanitized.get("password")).isEqualTo("***");
            assertThat(sanitized.get("normalKey")).isEqualTo("value4");
        }
    }

    @Nested
    @DisplayName("sanitizeParams方法测试")
    class SanitizeParamsTests {

        @Test
        @DisplayName("遮盖敏感参数")
        void testSanitizeParams() {
            Map<String, String> params = Map.of(
                    "code", "123456",
                    "password", "secret123",
                    "templateId", "SMS_001"
            );

            Map<String, String> sanitized = SmsLogSanitizer.sanitizeParams(params);

            assertThat(sanitized.get("code")).contains("***");
            assertThat(sanitized.get("password")).contains("***");
            assertThat(sanitized.get("templateId")).isEqualTo("SMS_001");
        }

        @Test
        @DisplayName("null参数返回空Map")
        void testSanitizeParamsNull() {
            Map<String, String> sanitized = SmsLogSanitizer.sanitizeParams(null);

            assertThat(sanitized).isEmpty();
        }

        @Test
        @DisplayName("保留非敏感参数")
        void testSanitizeParamsKeepsNonSensitive() {
            Map<String, String> params = Map.of(
                    "templateId", "SMS_001",
                    "action", "send"
            );

            Map<String, String> sanitized = SmsLogSanitizer.sanitizeParams(params);

            assertThat(sanitized.get("templateId")).isEqualTo("SMS_001");
            assertThat(sanitized.get("action")).isEqualTo("send");
        }
    }

    @Nested
    @DisplayName("sanitizeError方法测试")
    class SanitizeErrorTests {

        @Test
        @DisplayName("遮盖错误消息中的手机号")
        void testSanitizeError() {
            String error = "Failed to send to 13800138000";

            String sanitized = SmsLogSanitizer.sanitizeError(error);

            assertThat(sanitized).doesNotContain("13800138000");
            assertThat(sanitized).contains("1**********");
        }

        @Test
        @DisplayName("null错误返回null")
        void testSanitizeErrorNull() {
            String sanitized = SmsLogSanitizer.sanitizeError(null);

            assertThat(sanitized).isNull();
        }

        @Test
        @DisplayName("无手机号保持原样")
        void testSanitizeErrorNoPhone() {
            String error = "Connection timeout";

            String sanitized = SmsLogSanitizer.sanitizeError(error);

            assertThat(sanitized).isEqualTo("Connection timeout");
        }
    }

    @Nested
    @DisplayName("format方法测试")
    class FormatTests {

        @Test
        @DisplayName("格式化日志消息")
        void testFormat() {
            String formatted = SmsLogSanitizer.format("SMS %s to %s: %s", "send", "13800138000", "success");

            assertThat(formatted).contains("SMS");
            assertThat(formatted).contains("send");
        }

        @Test
        @DisplayName("无参数返回模板")
        void testFormatNoArgs() {
            String formatted = SmsLogSanitizer.format("Static message");

            assertThat(formatted).isEqualTo("Static message");
        }

        @Test
        @DisplayName("SmsMessage参数被sanitize")
        void testFormatWithSmsMessage() {
            SmsMessage message = SmsMessage.of("13800138000", "Test");

            String formatted = SmsLogSanitizer.format("Message: %s", message);

            assertThat(formatted).doesNotContain("13800138000");
        }
    }
}
