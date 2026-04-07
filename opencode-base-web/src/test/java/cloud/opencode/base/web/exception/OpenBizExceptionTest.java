package cloud.opencode.base.web.exception;

import cloud.opencode.base.core.exception.OpenException;
import cloud.opencode.base.web.CommonResultCode;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenBizExceptionTest Tests
 * OpenBizExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("OpenBizException Tests")
class OpenBizExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with message should use business error code")
        void constructorWithMessageShouldUseBusinessErrorCode() {
            OpenBizException exception = new OpenBizException("Business logic error");

            assertThat(exception.getMessage()).isEqualTo("Business logic error");
            assertThat(exception.getCode()).isEqualTo(CommonResultCode.BUSINESS_ERROR.getCode());
            assertThat(exception.getData()).isNull();
        }

        @Test
        @DisplayName("constructor with code and message should set both")
        void constructorWithCodeAndMessageShouldSetBoth() {
            OpenBizException exception = new OpenBizException("BIZ001", "Custom business error");

            assertThat(exception.getCode()).isEqualTo("BIZ001");
            assertThat(exception.getMessage()).isEqualTo("Custom business error");
        }

        @Test
        @DisplayName("constructor with result code should use code values")
        void constructorWithResultCodeShouldUseCodeValues() {
            OpenBizException exception = new OpenBizException(CommonResultCode.DATA_NOT_FOUND);

            assertThat(exception.getCode()).isEqualTo(CommonResultCode.DATA_NOT_FOUND.getCode());
            assertThat(exception.getMessage()).isEqualTo(CommonResultCode.DATA_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("constructor with result code and custom message should use custom message")
        void constructorWithResultCodeAndCustomMessageShouldUseCustomMessage() {
            OpenBizException exception = new OpenBizException(CommonResultCode.DATA_NOT_FOUND, "User not found");

            assertThat(exception.getMessage()).isEqualTo("User not found");
        }

        @Test
        @DisplayName("constructor with result code and data should set data")
        void constructorWithResultCodeAndDataShouldSetData() {
            Object errorData = new Object();
            OpenBizException exception = new OpenBizException(CommonResultCode.VALIDATION_ERROR, errorData);

            assertThat(exception.getData()).isEqualTo(errorData);
        }

        @Test
        @DisplayName("constructor with code, message and data should set all")
        void constructorWithCodeMessageAndDataShouldSetAll() {
            Object errorData = java.util.Map.of("field", "error");
            OpenBizException exception = new OpenBizException("VAL001", "Validation failed", errorData);

            assertThat(exception.getCode()).isEqualTo("VAL001");
            assertThat(exception.getMessage()).isEqualTo("Validation failed");
            assertThat(exception.getData()).isEqualTo(errorData);
        }

        @Test
        @DisplayName("constructor with message and cause should set both")
        void constructorWithMessageAndCauseShouldSetBoth() {
            Exception cause = new RuntimeException("Root cause");
            OpenBizException exception = new OpenBizException("Business error", cause);

            assertThat(exception.getMessage()).isEqualTo("Business error");
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("constructor with result code and cause should set both")
        void constructorWithResultCodeAndCauseShouldSetBoth() {
            Exception cause = new RuntimeException("Root cause");
            OpenBizException exception = new OpenBizException(CommonResultCode.OPERATION_FAILED, cause);

            assertThat(exception.getCode()).isEqualTo(CommonResultCode.OPERATION_FAILED.getCode());
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("dataNotFound should create data not found exception")
        void dataNotFoundShouldCreateDataNotFoundException() {
            OpenBizException exception = OpenBizException.dataNotFound("User not found");

            assertThat(exception.getCode()).isEqualTo(CommonResultCode.DATA_NOT_FOUND.getCode());
            assertThat(exception.getMessage()).isEqualTo("User not found");
        }

        @Test
        @DisplayName("dataDuplicate should create data duplicate exception")
        void dataDuplicateShouldCreateDataDuplicateException() {
            OpenBizException exception = OpenBizException.dataDuplicate("Email already exists");

            assertThat(exception.getCode()).isEqualTo(CommonResultCode.DATA_DUPLICATE.getCode());
            assertThat(exception.getMessage()).isEqualTo("Email already exists");
        }

        @Test
        @DisplayName("operationFailed should create operation failed exception")
        void operationFailedShouldCreateOperationFailedException() {
            OpenBizException exception = OpenBizException.operationFailed("Save failed");

            assertThat(exception.getCode()).isEqualTo(CommonResultCode.OPERATION_FAILED.getCode());
            assertThat(exception.getMessage()).isEqualTo("Save failed");
        }

        @Test
        @DisplayName("validationError should create validation error exception")
        void validationErrorShouldCreateValidationErrorException() {
            OpenBizException exception = OpenBizException.validationError("Invalid data");

            assertThat(exception.getCode()).isEqualTo(CommonResultCode.VALIDATION_ERROR.getCode());
            assertThat(exception.getMessage()).isEqualTo("Invalid data");
        }

        @Test
        @DisplayName("validationError with data should include error data")
        void validationErrorWithDataShouldIncludeErrorData() {
            var errors = java.util.List.of("Field A is required", "Field B is invalid");
            OpenBizException exception = OpenBizException.validationError("Validation failed", errors);

            assertThat(exception.getCode()).isEqualTo(CommonResultCode.VALIDATION_ERROR.getCode());
            assertThat(exception.getData()).isEqualTo(errors);
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("should extend OpenWebException")
        void shouldExtendOpenWebException() {
            OpenBizException exception = new OpenBizException("Test");

            assertThat(exception).isInstanceOf(OpenWebException.class);
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should extend OpenException")
        void shouldExtendOpenException() {
            OpenBizException exception = new OpenBizException("Test");

            assertThat(exception).isInstanceOf(OpenException.class);
        }
    }
}
