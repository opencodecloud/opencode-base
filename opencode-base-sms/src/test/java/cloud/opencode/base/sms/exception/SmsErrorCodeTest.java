package cloud.opencode.base.sms.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SmsErrorCodeTest Tests
 * SmsErrorCodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("SmsErrorCode 测试")
class SmsErrorCodeTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("包含所有错误码")
        void testAllErrorCodes() {
            SmsErrorCode[] codes = SmsErrorCode.values();

            assertThat(codes).contains(
                    SmsErrorCode.SEND_FAILED,
                    SmsErrorCode.SEND_TIMEOUT,
                    SmsErrorCode.SEND_RATE_LIMITED,
                    SmsErrorCode.INVALID_PHONE_NUMBER,
                    SmsErrorCode.PHONE_NUMBER_BLOCKED,
                    SmsErrorCode.UNSUPPORTED_COUNTRY,
                    SmsErrorCode.MESSAGE_EMPTY,
                    SmsErrorCode.MESSAGE_TOO_LONG,
                    SmsErrorCode.INVALID_ENCODING,
                    SmsErrorCode.TEMPLATE_NOT_FOUND,
                    SmsErrorCode.TEMPLATE_INVALID,
                    SmsErrorCode.TEMPLATE_VARIABLE_MISSING,
                    SmsErrorCode.NETWORK_ERROR,
                    SmsErrorCode.CONNECTION_TIMEOUT,
                    SmsErrorCode.READ_TIMEOUT,
                    SmsErrorCode.PROVIDER_NOT_CONFIGURED,
                    SmsErrorCode.PROVIDER_ERROR,
                    SmsErrorCode.PROVIDER_UNAVAILABLE
            );
        }

        @Test
        @DisplayName("valueOf返回正确枚举")
        void testValueOf() {
            assertThat(SmsErrorCode.valueOf("SEND_FAILED")).isEqualTo(SmsErrorCode.SEND_FAILED);
            assertThat(SmsErrorCode.valueOf("NETWORK_ERROR")).isEqualTo(SmsErrorCode.NETWORK_ERROR);
        }
    }

    @Nested
    @DisplayName("getCode方法测试")
    class GetCodeTests {

        @Test
        @DisplayName("返回正确的代码")
        void testGetCode() {
            assertThat(SmsErrorCode.SEND_FAILED.getCode()).isEqualTo(1001);
            assertThat(SmsErrorCode.SEND_TIMEOUT.getCode()).isEqualTo(1002);
            assertThat(SmsErrorCode.SEND_RATE_LIMITED.getCode()).isEqualTo(1003);
        }

        @Test
        @DisplayName("每个错误码有唯一代码")
        void testUniqueCode() {
            SmsErrorCode[] codes = SmsErrorCode.values();

            long distinctCount = java.util.Arrays.stream(codes)
                    .map(SmsErrorCode::getCode)
                    .distinct()
                    .count();

            assertThat(distinctCount).isEqualTo(codes.length);
        }
    }

    @Nested
    @DisplayName("getMessage方法测试")
    class GetMessageTests {

        @Test
        @DisplayName("返回正确的消息")
        void testGetMessage() {
            assertThat(SmsErrorCode.SEND_FAILED.getMessage()).isNotBlank();
            assertThat(SmsErrorCode.INVALID_PHONE_NUMBER.getMessage()).isNotBlank();
        }

        @Test
        @DisplayName("每个错误码有消息")
        void testAllHaveMessages() {
            for (SmsErrorCode code : SmsErrorCode.values()) {
                assertThat(code.getMessage()).isNotNull();
                assertThat(code.getMessage()).isNotEmpty();
            }
        }
    }
}
