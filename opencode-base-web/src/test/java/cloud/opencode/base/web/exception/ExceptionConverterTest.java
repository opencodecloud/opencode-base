package cloud.opencode.base.web.exception;

import cloud.opencode.base.web.CommonResultCode;
import cloud.opencode.base.web.Result;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ExceptionConverterTest Tests
 * ExceptionConverterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("ExceptionConverter Tests")
class ExceptionConverterTest {

    @Nested
    @DisplayName("To Result Tests")
    class ToResultTests {

        @Test
        @DisplayName("toResult with null should return unknown error")
        void toResultWithNullShouldReturnUnknownError() {
            Result<Object> result = ExceptionConverter.toResult(null);

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo(CommonResultCode.INTERNAL_ERROR.getCode());
            assertThat(result.message()).isEqualTo("Unknown error");
        }

        @Test
        @DisplayName("toResult with OpenWebException should use exception values")
        void toResultWithOpenWebExceptionShouldUseExceptionValues() {
            OpenWebException exception = new OpenWebException("E001", "Custom error", 400);

            Result<Object> result = ExceptionConverter.toResult(exception);

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo("E001");
            assertThat(result.message()).isEqualTo("Custom error");
        }

        @Test
        @DisplayName("toResult with OpenBizException should use exception values")
        void toResultWithOpenBizExceptionShouldUseExceptionValues() {
            OpenBizException exception = new OpenBizException("VAL001", "Validation failed");

            Result<Object> result = ExceptionConverter.toResult(exception);

            assertThat(result.success()).isFalse();
            assertThat(result.code()).isEqualTo("VAL001");
            assertThat(result.message()).isEqualTo("Validation failed");
        }

        @Test
        @DisplayName("toResult with IllegalArgumentException should use param invalid code")
        void toResultWithIllegalArgumentExceptionShouldUseParamInvalidCode() {
            IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

            Result<Object> result = ExceptionConverter.toResult(exception);

            assertThat(result.code()).isEqualTo(CommonResultCode.PARAM_INVALID.getCode());
            assertThat(result.message()).isEqualTo("Invalid argument");
        }

        @Test
        @DisplayName("toResult with IllegalStateException should use business error code")
        void toResultWithIllegalStateExceptionShouldUseBusinessErrorCode() {
            IllegalStateException exception = new IllegalStateException("Invalid state");

            Result<Object> result = ExceptionConverter.toResult(exception);

            assertThat(result.code()).isEqualTo(CommonResultCode.BUSINESS_ERROR.getCode());
        }

        @Test
        @DisplayName("toResult with NullPointerException should use param missing code")
        void toResultWithNullPointerExceptionShouldUseParamMissingCode() {
            NullPointerException exception = new NullPointerException();

            Result<Object> result = ExceptionConverter.toResult(exception);

            assertThat(result.code()).isEqualTo(CommonResultCode.PARAM_MISSING.getCode());
            assertThat(result.message()).isEqualTo("Required parameter is null");
        }

        @Test
        @DisplayName("toResult with SecurityException should use forbidden code")
        void toResultWithSecurityExceptionShouldUseForbiddenCode() {
            SecurityException exception = new SecurityException("Access denied");

            Result<Object> result = ExceptionConverter.toResult(exception);

            assertThat(result.code()).isEqualTo(CommonResultCode.FORBIDDEN.getCode());
        }

        @Test
        @DisplayName("toResult with generic exception should use internal error code and generic message")
        void toResultWithGenericExceptionShouldUseInternalErrorCode() {
            Exception exception = new Exception("Something went wrong");

            Result<Object> result = ExceptionConverter.toResult(exception);

            assertThat(result.code()).isEqualTo(CommonResultCode.INTERNAL_ERROR.getCode());
            // Generic message to prevent internal info leakage
            assertThat(result.message()).isEqualTo("Internal server error");
        }

        @Test
        @DisplayName("toResult with exception without message should use default message")
        void toResultWithExceptionWithoutMessageShouldUseDefaultMessage() {
            Exception exception = new Exception();

            Result<Object> result = ExceptionConverter.toResult(exception);

            assertThat(result.message()).isEqualTo("Internal server error");
        }
    }

    @Nested
    @DisplayName("Get Code Tests")
    class GetCodeTests {

        @Test
        @DisplayName("getCode should return code from OpenWebException")
        void getCodeShouldReturnCodeFromOpenWebException() {
            OpenWebException exception = new OpenWebException("E001", "Error");

            assertThat(ExceptionConverter.getCode(exception)).isEqualTo("E001");
        }

        @Test
        @DisplayName("getCode should return param invalid for IllegalArgumentException")
        void getCodeShouldReturnParamInvalidForIllegalArgumentException() {
            assertThat(ExceptionConverter.getCode(new IllegalArgumentException()))
                .isEqualTo(CommonResultCode.PARAM_INVALID.getCode());
        }

        @Test
        @DisplayName("getCode should return business error for IllegalStateException")
        void getCodeShouldReturnBusinessErrorForIllegalStateException() {
            assertThat(ExceptionConverter.getCode(new IllegalStateException()))
                .isEqualTo(CommonResultCode.BUSINESS_ERROR.getCode());
        }

        @Test
        @DisplayName("getCode should return param missing for NullPointerException")
        void getCodeShouldReturnParamMissingForNullPointerException() {
            assertThat(ExceptionConverter.getCode(new NullPointerException()))
                .isEqualTo(CommonResultCode.PARAM_MISSING.getCode());
        }

        @Test
        @DisplayName("getCode should return forbidden for SecurityException")
        void getCodeShouldReturnForbiddenForSecurityException() {
            assertThat(ExceptionConverter.getCode(new SecurityException()))
                .isEqualTo(CommonResultCode.FORBIDDEN.getCode());
        }

        @Test
        @DisplayName("getCode should return internal error for other exceptions")
        void getCodeShouldReturnInternalErrorForOtherExceptions() {
            assertThat(ExceptionConverter.getCode(new RuntimeException()))
                .isEqualTo(CommonResultCode.INTERNAL_ERROR.getCode());
        }
    }

    @Nested
    @DisplayName("Get HTTP Status Tests")
    class GetHttpStatusTests {

        @Test
        @DisplayName("getHttpStatus should return status from OpenWebException")
        void getHttpStatusShouldReturnStatusFromOpenWebException() {
            OpenWebException exception = new OpenWebException("E001", "Error", 403);

            assertThat(ExceptionConverter.getHttpStatus(exception)).isEqualTo(403);
        }

        @Test
        @DisplayName("getHttpStatus should return 400 for IllegalArgumentException")
        void getHttpStatusShouldReturn400ForIllegalArgumentException() {
            assertThat(ExceptionConverter.getHttpStatus(new IllegalArgumentException()))
                .isEqualTo(CommonResultCode.PARAM_INVALID.getHttpStatus());
        }

        @Test
        @DisplayName("getHttpStatus should return 500 for other exceptions")
        void getHttpStatusShouldReturn500ForOtherExceptions() {
            assertThat(ExceptionConverter.getHttpStatus(new RuntimeException()))
                .isEqualTo(CommonResultCode.INTERNAL_ERROR.getHttpStatus());
        }
    }
}
