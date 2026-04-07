package cloud.opencode.base.email.exception;

import cloud.opencode.base.email.protocol.ProtocolException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.*;

/**
 * EmailErrorCode test class
 * EmailErrorCode 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailErrorCode 测试")
class EmailErrorCodeTest {

    @Nested
    @DisplayName("错误码范围测试")
    class ErrorCodeRangeTests {

        @Test
        @DisplayName("UNKNOWN错误码为0")
        void testUnknownCode() {
            assertThat(EmailErrorCode.UNKNOWN.getCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("配置错误码在1xxx范围")
        void testConfigErrorCodeRange() {
            assertThat(EmailErrorCode.CONFIG_INVALID.getCode()).isBetween(1000, 1999);
            assertThat(EmailErrorCode.AUTH_FAILED.getCode()).isBetween(1000, 1999);
        }

        @Test
        @DisplayName("连接错误码在2xxx范围")
        void testConnectionErrorCodeRange() {
            assertThat(EmailErrorCode.CONNECTION_FAILED.getCode()).isBetween(2000, 2999);
            assertThat(EmailErrorCode.CONNECTION_TIMEOUT.getCode()).isBetween(2000, 2999);
            assertThat(EmailErrorCode.SEND_TIMEOUT.getCode()).isBetween(2000, 2999);
        }

        @Test
        @DisplayName("发送错误码在3xxx范围")
        void testSendErrorCodeRange() {
            assertThat(EmailErrorCode.RECIPIENT_REJECTED.getCode()).isBetween(3000, 3999);
            assertThat(EmailErrorCode.MESSAGE_REJECTED.getCode()).isBetween(3000, 3999);
            assertThat(EmailErrorCode.MAILBOX_FULL.getCode()).isBetween(3000, 3999);
            assertThat(EmailErrorCode.RATE_LIMITED.getCode()).isBetween(3000, 3999);
        }

        @Test
        @DisplayName("安全错误码在4xxx范围")
        void testSecurityErrorCodeRange() {
            assertThat(EmailErrorCode.HEADER_INJECTION.getCode()).isBetween(4000, 4999);
            assertThat(EmailErrorCode.INVALID_ATTACHMENT.getCode()).isBetween(4000, 4999);
        }

        @Test
        @DisplayName("模板错误码在5xxx范围")
        void testTemplateErrorCodeRange() {
            assertThat(EmailErrorCode.TEMPLATE_ERROR.getCode()).isBetween(5000, 5999);
        }

        @Test
        @DisplayName("接收错误码在6xxx范围")
        void testReceiveErrorCodeRange() {
            assertThat(EmailErrorCode.FOLDER_NOT_FOUND.getCode()).isBetween(6000, 6999);
            assertThat(EmailErrorCode.MESSAGE_NOT_FOUND.getCode()).isBetween(6000, 6999);
            assertThat(EmailErrorCode.RECEIVE_TIMEOUT.getCode()).isBetween(6000, 6999);
            assertThat(EmailErrorCode.FOLDER_ACCESS_DENIED.getCode()).isBetween(6000, 6999);
            assertThat(EmailErrorCode.IDLE_NOT_SUPPORTED.getCode()).isBetween(6000, 6999);
            assertThat(EmailErrorCode.PROTOCOL_NOT_SUPPORTED.getCode()).isBetween(6000, 6999);
            assertThat(EmailErrorCode.ATTACHMENT_DOWNLOAD_FAILED.getCode()).isBetween(6000, 6999);
            assertThat(EmailErrorCode.MESSAGE_PARSE_FAILED.getCode()).isBetween(6000, 6999);
            assertThat(EmailErrorCode.CONNECTION_LOST.getCode()).isBetween(6000, 6999);
        }
    }

    @Nested
    @DisplayName("描述测试")
    class DescriptionTests {

        @ParameterizedTest
        @EnumSource(EmailErrorCode.class)
        @DisplayName("所有错误码都有英文描述")
        void testAllHaveDescription(EmailErrorCode code) {
            assertThat(code.getDescription()).isNotNull().isNotBlank();
        }

        @ParameterizedTest
        @EnumSource(EmailErrorCode.class)
        @DisplayName("所有错误码都有中文描述")
        void testAllHaveChineseDescription(EmailErrorCode code) {
            assertThat(code.getDescriptionCn()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("UNKNOWN描述正确")
        void testUnknownDescription() {
            assertThat(EmailErrorCode.UNKNOWN.getDescription()).isEqualTo("Unknown error");
            assertThat(EmailErrorCode.UNKNOWN.getDescriptionCn()).isEqualTo("未知错误");
        }

        @Test
        @DisplayName("CONNECTION_FAILED描述正确")
        void testConnectionFailedDescription() {
            assertThat(EmailErrorCode.CONNECTION_FAILED.getDescription()).isEqualTo("Connection failed");
            assertThat(EmailErrorCode.CONNECTION_FAILED.getDescriptionCn()).isEqualTo("连接失败");
        }
    }

    @Nested
    @DisplayName("可重试标志测试")
    class RetryableTests {

        @Test
        @DisplayName("连接相关错误可重试")
        void testConnectionErrorsRetryable() {
            assertThat(EmailErrorCode.CONNECTION_FAILED.isRetryable()).isTrue();
            assertThat(EmailErrorCode.CONNECTION_TIMEOUT.isRetryable()).isTrue();
            assertThat(EmailErrorCode.SEND_TIMEOUT.isRetryable()).isTrue();
            assertThat(EmailErrorCode.CONNECTION_LOST.isRetryable()).isTrue();
        }

        @Test
        @DisplayName("临时错误可重试")
        void testTemporaryErrorsRetryable() {
            assertThat(EmailErrorCode.MAILBOX_FULL.isRetryable()).isTrue();
            assertThat(EmailErrorCode.RATE_LIMITED.isRetryable()).isTrue();
            assertThat(EmailErrorCode.RECEIVE_TIMEOUT.isRetryable()).isTrue();
            assertThat(EmailErrorCode.ATTACHMENT_DOWNLOAD_FAILED.isRetryable()).isTrue();
        }

        @Test
        @DisplayName("永久错误不可重试")
        void testPermanentErrorsNotRetryable() {
            assertThat(EmailErrorCode.CONFIG_INVALID.isRetryable()).isFalse();
            assertThat(EmailErrorCode.AUTH_FAILED.isRetryable()).isFalse();
            assertThat(EmailErrorCode.RECIPIENT_REJECTED.isRetryable()).isFalse();
            assertThat(EmailErrorCode.MESSAGE_REJECTED.isRetryable()).isFalse();
            assertThat(EmailErrorCode.HEADER_INJECTION.isRetryable()).isFalse();
            assertThat(EmailErrorCode.INVALID_ATTACHMENT.isRetryable()).isFalse();
            assertThat(EmailErrorCode.TEMPLATE_ERROR.isRetryable()).isFalse();
            assertThat(EmailErrorCode.FOLDER_NOT_FOUND.isRetryable()).isFalse();
            assertThat(EmailErrorCode.MESSAGE_NOT_FOUND.isRetryable()).isFalse();
            assertThat(EmailErrorCode.FOLDER_ACCESS_DENIED.isRetryable()).isFalse();
            assertThat(EmailErrorCode.IDLE_NOT_SUPPORTED.isRetryable()).isFalse();
            assertThat(EmailErrorCode.PROTOCOL_NOT_SUPPORTED.isRetryable()).isFalse();
            assertThat(EmailErrorCode.MESSAGE_PARSE_FAILED.isRetryable()).isFalse();
            assertThat(EmailErrorCode.UNKNOWN.isRetryable()).isFalse();
        }
    }

    @Nested
    @DisplayName("fromException() 测试")
    class FromExceptionTests {

        @Test
        @DisplayName("null异常返回UNKNOWN")
        void testNullException() {
            assertThat(EmailErrorCode.fromException(null)).isEqualTo(EmailErrorCode.UNKNOWN);
        }

        @Test
        @DisplayName("ProtocolException with auth failure返回AUTH_FAILED")
        void testProtocolExceptionAuthFailure() {
            ProtocolException e = new ProtocolException("auth failed", 535);
            assertThat(EmailErrorCode.fromException(e)).isEqualTo(EmailErrorCode.AUTH_FAILED);
        }

        @Test
        @DisplayName("ProtocolException with code 534返回AUTH_FAILED")
        void testProtocolExceptionAuthFailure534() {
            ProtocolException e = new ProtocolException("authentication required", 534);
            assertThat(EmailErrorCode.fromException(e)).isEqualTo(EmailErrorCode.AUTH_FAILED);
        }

        @Test
        @DisplayName("SocketTimeoutException返回CONNECTION_TIMEOUT")
        void testSocketTimeoutException() {
            SocketTimeoutException e = new SocketTimeoutException("Timeout");
            assertThat(EmailErrorCode.fromException(e)).isEqualTo(EmailErrorCode.CONNECTION_TIMEOUT);
        }

        @Test
        @DisplayName("ConnectException返回CONNECTION_FAILED")
        void testConnectException() {
            ConnectException e = new ConnectException("Connection refused");
            assertThat(EmailErrorCode.fromException(e)).isEqualTo(EmailErrorCode.CONNECTION_FAILED);
        }

        @Test
        @DisplayName("其他异常返回UNKNOWN")
        void testOtherException() {
            RuntimeException e = new RuntimeException("Unknown");
            assertThat(EmailErrorCode.fromException(e)).isEqualTo(EmailErrorCode.UNKNOWN);
        }

        @Test
        @DisplayName("IOException返回UNKNOWN")
        void testIOException() {
            java.io.IOException e = new java.io.IOException("IO error");
            assertThat(EmailErrorCode.fromException(e)).isEqualTo(EmailErrorCode.UNKNOWN);
        }

        @Test
        @DisplayName("ProtocolException with timeout cause返回CONNECTION_TIMEOUT")
        void testProtocolExceptionWithTimeoutCause() {
            SocketTimeoutException cause = new SocketTimeoutException("timed out");
            ProtocolException e = new ProtocolException("connection timeout", cause);
            assertThat(EmailErrorCode.fromException(e)).isEqualTo(EmailErrorCode.CONNECTION_TIMEOUT);
        }

        @Test
        @DisplayName("ProtocolException with connection failure cause返回CONNECTION_FAILED")
        void testProtocolExceptionWithConnectCause() {
            ConnectException cause = new ConnectException("refused");
            ProtocolException e = new ProtocolException("connection failed", cause);
            assertThat(EmailErrorCode.fromException(e)).isEqualTo(EmailErrorCode.CONNECTION_FAILED);
        }
    }

    @Nested
    @DisplayName("枚举完整性测试")
    class EnumCompletenessTests {

        @Test
        @DisplayName("所有枚举值都可以通过valueOf获取")
        void testValuesAccessible() {
            assertThat(EmailErrorCode.valueOf("UNKNOWN")).isEqualTo(EmailErrorCode.UNKNOWN);
            assertThat(EmailErrorCode.valueOf("CONFIG_INVALID")).isEqualTo(EmailErrorCode.CONFIG_INVALID);
            assertThat(EmailErrorCode.valueOf("CONNECTION_FAILED")).isEqualTo(EmailErrorCode.CONNECTION_FAILED);
        }

        @Test
        @DisplayName("values()返回所有枚举值")
        void testValuesMethod() {
            EmailErrorCode[] codes = EmailErrorCode.values();
            assertThat(codes).hasSizeGreaterThanOrEqualTo(19); // At least 19 error codes
        }

        @ParameterizedTest
        @EnumSource(EmailErrorCode.class)
        @DisplayName("所有错误码都有唯一的code值")
        void testUniqueCodeValues(EmailErrorCode code) {
            long count = java.util.Arrays.stream(EmailErrorCode.values())
                    .filter(c -> c.getCode() == code.getCode())
                    .count();
            assertThat(count).isEqualTo(1);
        }
    }
}
