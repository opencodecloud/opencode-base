package cloud.opencode.base.web;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CommonResultCodeTest Tests
 * CommonResultCodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("CommonResultCode Tests")
class CommonResultCodeTest {

    @Nested
    @DisplayName("Success Codes Tests")
    class SuccessCodesTests {

        @Test
        @DisplayName("SUCCESS should have correct values")
        void successShouldHaveCorrectValues() {
            assertThat(CommonResultCode.SUCCESS.getCode()).isEqualTo("00000");
            assertThat(CommonResultCode.SUCCESS.getHttpStatus()).isEqualTo(200);
            assertThat(CommonResultCode.SUCCESS.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("CREATED should have correct values")
        void createdShouldHaveCorrectValues() {
            assertThat(CommonResultCode.CREATED.getCode()).isEqualTo("00001");
            assertThat(CommonResultCode.CREATED.getHttpStatus()).isEqualTo(201);
            assertThat(CommonResultCode.CREATED.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("NO_CONTENT should have correct values")
        void noContentShouldHaveCorrectValues() {
            assertThat(CommonResultCode.NO_CONTENT.getCode()).isEqualTo("00005");
            assertThat(CommonResultCode.NO_CONTENT.getHttpStatus()).isEqualTo(204);
            assertThat(CommonResultCode.NO_CONTENT.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("Client Error Codes Tests")
    class ClientErrorCodesTests {

        @Test
        @DisplayName("BAD_REQUEST should have correct values")
        void badRequestShouldHaveCorrectValues() {
            assertThat(CommonResultCode.BAD_REQUEST.getCode()).isEqualTo("A0400");
            assertThat(CommonResultCode.BAD_REQUEST.getHttpStatus()).isEqualTo(400);
            assertThat(CommonResultCode.BAD_REQUEST.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("UNAUTHORIZED should have correct values")
        void unauthorizedShouldHaveCorrectValues() {
            assertThat(CommonResultCode.UNAUTHORIZED.getCode()).isEqualTo("A0401");
            assertThat(CommonResultCode.UNAUTHORIZED.getHttpStatus()).isEqualTo(401);
            assertThat(CommonResultCode.UNAUTHORIZED.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("FORBIDDEN should have correct values")
        void forbiddenShouldHaveCorrectValues() {
            assertThat(CommonResultCode.FORBIDDEN.getCode()).isEqualTo("A0403");
            assertThat(CommonResultCode.FORBIDDEN.getHttpStatus()).isEqualTo(403);
            assertThat(CommonResultCode.FORBIDDEN.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("NOT_FOUND should have correct values")
        void notFoundShouldHaveCorrectValues() {
            assertThat(CommonResultCode.NOT_FOUND.getCode()).isEqualTo("A0404");
            assertThat(CommonResultCode.NOT_FOUND.getHttpStatus()).isEqualTo(404);
            assertThat(CommonResultCode.NOT_FOUND.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("VALIDATION_ERROR should have correct values")
        void validationErrorShouldHaveCorrectValues() {
            assertThat(CommonResultCode.VALIDATION_ERROR.getCode()).isEqualTo("A0422");
            assertThat(CommonResultCode.VALIDATION_ERROR.getHttpStatus()).isEqualTo(422);
        }

        @Test
        @DisplayName("TOO_MANY_REQUESTS should have correct values")
        void tooManyRequestsShouldHaveCorrectValues() {
            assertThat(CommonResultCode.TOO_MANY_REQUESTS.getCode()).isEqualTo("A0429");
            assertThat(CommonResultCode.TOO_MANY_REQUESTS.getHttpStatus()).isEqualTo(429);
        }
    }

    @Nested
    @DisplayName("Parameter Error Codes Tests")
    class ParameterErrorCodesTests {

        @Test
        @DisplayName("PARAM_MISSING should have correct values")
        void paramMissingShouldHaveCorrectValues() {
            assertThat(CommonResultCode.PARAM_MISSING.getCode()).isEqualTo("A1001");
            assertThat(CommonResultCode.PARAM_MISSING.getHttpStatus()).isEqualTo(400);
        }

        @Test
        @DisplayName("PARAM_INVALID should have correct values")
        void paramInvalidShouldHaveCorrectValues() {
            assertThat(CommonResultCode.PARAM_INVALID.getCode()).isEqualTo("A1002");
            assertThat(CommonResultCode.PARAM_INVALID.getHttpStatus()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("Auth Error Codes Tests")
    class AuthErrorCodesTests {

        @Test
        @DisplayName("TOKEN_EXPIRED should have correct values")
        void tokenExpiredShouldHaveCorrectValues() {
            assertThat(CommonResultCode.TOKEN_EXPIRED.getCode()).isEqualTo("A2001");
            assertThat(CommonResultCode.TOKEN_EXPIRED.getHttpStatus()).isEqualTo(401);
        }

        @Test
        @DisplayName("PERMISSION_DENIED should have correct values")
        void permissionDeniedShouldHaveCorrectValues() {
            assertThat(CommonResultCode.PERMISSION_DENIED.getCode()).isEqualTo("A2003");
            assertThat(CommonResultCode.PERMISSION_DENIED.getHttpStatus()).isEqualTo(403);
        }
    }

    @Nested
    @DisplayName("Server Error Codes Tests")
    class ServerErrorCodesTests {

        @Test
        @DisplayName("INTERNAL_ERROR should have correct values")
        void internalErrorShouldHaveCorrectValues() {
            assertThat(CommonResultCode.INTERNAL_ERROR.getCode()).isEqualTo("B0500");
            assertThat(CommonResultCode.INTERNAL_ERROR.getHttpStatus()).isEqualTo(500);
            assertThat(CommonResultCode.INTERNAL_ERROR.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("SERVICE_UNAVAILABLE should have correct values")
        void serviceUnavailableShouldHaveCorrectValues() {
            assertThat(CommonResultCode.SERVICE_UNAVAILABLE.getCode()).isEqualTo("B0503");
            assertThat(CommonResultCode.SERVICE_UNAVAILABLE.getHttpStatus()).isEqualTo(503);
        }
    }

    @Nested
    @DisplayName("Business Error Codes Tests")
    class BusinessErrorCodesTests {

        @Test
        @DisplayName("BUSINESS_ERROR should have correct values")
        void businessErrorShouldHaveCorrectValues() {
            assertThat(CommonResultCode.BUSINESS_ERROR.getCode()).isEqualTo("B1001");
            assertThat(CommonResultCode.BUSINESS_ERROR.getHttpStatus()).isEqualTo(500);
        }

        @Test
        @DisplayName("DATA_NOT_FOUND should have correct values")
        void dataNotFoundShouldHaveCorrectValues() {
            assertThat(CommonResultCode.DATA_NOT_FOUND.getCode()).isEqualTo("B1002");
            assertThat(CommonResultCode.DATA_NOT_FOUND.getHttpStatus()).isEqualTo(404);
        }

        @Test
        @DisplayName("DATA_DUPLICATE should have correct values")
        void dataDuplicateShouldHaveCorrectValues() {
            assertThat(CommonResultCode.DATA_DUPLICATE.getCode()).isEqualTo("B1003");
            assertThat(CommonResultCode.DATA_DUPLICATE.getHttpStatus()).isEqualTo(409);
        }
    }

    @Nested
    @DisplayName("Third Party Error Codes Tests")
    class ThirdPartyErrorCodesTests {

        @Test
        @DisplayName("THIRD_PARTY_ERROR should have correct values")
        void thirdPartyErrorShouldHaveCorrectValues() {
            assertThat(CommonResultCode.THIRD_PARTY_ERROR.getCode()).isEqualTo("C0001");
            assertThat(CommonResultCode.THIRD_PARTY_ERROR.getHttpStatus()).isEqualTo(502);
        }

        @Test
        @DisplayName("EXTERNAL_TIMEOUT should have correct values")
        void externalTimeoutShouldHaveCorrectValues() {
            assertThat(CommonResultCode.EXTERNAL_TIMEOUT.getCode()).isEqualTo("C0005");
            assertThat(CommonResultCode.EXTERNAL_TIMEOUT.getHttpStatus()).isEqualTo(504);
        }
    }

    @Nested
    @DisplayName("Lookup Methods Tests")
    class LookupMethodsTests {

        @Test
        @DisplayName("fromCode should return correct result code")
        void fromCodeShouldReturnCorrectResultCode() {
            CommonResultCode result = CommonResultCode.fromCode("00000");

            assertThat(result).isEqualTo(CommonResultCode.SUCCESS);
        }

        @Test
        @DisplayName("fromCode should return null for unknown code")
        void fromCodeShouldReturnNullForUnknownCode() {
            CommonResultCode result = CommonResultCode.fromCode("UNKNOWN");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("fromCode with default should return default for unknown code")
        void fromCodeWithDefaultShouldReturnDefaultForUnknownCode() {
            CommonResultCode result = CommonResultCode.fromCode("UNKNOWN", CommonResultCode.INTERNAL_ERROR);

            assertThat(result).isEqualTo(CommonResultCode.INTERNAL_ERROR);
        }

        @Test
        @DisplayName("fromCode with default should return found code for known code")
        void fromCodeWithDefaultShouldReturnFoundCodeForKnownCode() {
            CommonResultCode result = CommonResultCode.fromCode("A0400", CommonResultCode.INTERNAL_ERROR);

            assertThat(result).isEqualTo(CommonResultCode.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("values should return all result codes")
        void valuesShouldReturnAllResultCodes() {
            CommonResultCode[] values = CommonResultCode.values();

            assertThat(values).isNotEmpty();
            assertThat(values).contains(CommonResultCode.SUCCESS, CommonResultCode.BAD_REQUEST, CommonResultCode.INTERNAL_ERROR);
        }

        @Test
        @DisplayName("valueOf should return correct enum constant")
        void valueOfShouldReturnCorrectEnumConstant() {
            CommonResultCode result = CommonResultCode.valueOf("SUCCESS");

            assertThat(result).isEqualTo(CommonResultCode.SUCCESS);
        }
    }

    @Nested
    @DisplayName("Message Tests")
    class MessageTests {

        @Test
        @DisplayName("getMessage should return message")
        void getMessageShouldReturnMessage() {
            assertThat(CommonResultCode.SUCCESS.getMessage()).isNotBlank();
            assertThat(CommonResultCode.BAD_REQUEST.getMessage()).isNotBlank();
        }
    }
}
