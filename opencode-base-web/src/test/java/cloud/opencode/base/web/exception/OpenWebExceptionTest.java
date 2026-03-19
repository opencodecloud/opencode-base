package cloud.opencode.base.web.exception;

import cloud.opencode.base.web.CommonResultCode;
import cloud.opencode.base.web.ResultCode;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenWebExceptionTest Tests
 * OpenWebExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("OpenWebException Tests")
class OpenWebExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with message should use default code")
        void constructorWithMessageShouldUseDefaultCode() {
            OpenWebException exception = new OpenWebException("Test message");

            assertThat(exception.getMessage()).isEqualTo("Test message");
            assertThat(exception.getCode()).isEqualTo(CommonResultCode.INTERNAL_ERROR.getCode());
            assertThat(exception.getHttpStatus()).isEqualTo(CommonResultCode.INTERNAL_ERROR.getHttpStatus());
        }

        @Test
        @DisplayName("constructor with code and message should set both")
        void constructorWithCodeAndMessageShouldSetBoth() {
            OpenWebException exception = new OpenWebException("E001", "Custom error");

            assertThat(exception.getCode()).isEqualTo("E001");
            assertThat(exception.getMessage()).isEqualTo("Custom error");
            assertThat(exception.getHttpStatus()).isEqualTo(500);
        }

        @Test
        @DisplayName("constructor with code, message and HTTP status should set all")
        void constructorWithCodeMessageAndHttpStatusShouldSetAll() {
            OpenWebException exception = new OpenWebException("E001", "Custom error", 400);

            assertThat(exception.getCode()).isEqualTo("E001");
            assertThat(exception.getMessage()).isEqualTo("Custom error");
            assertThat(exception.getHttpStatus()).isEqualTo(400);
        }

        @Test
        @DisplayName("constructor with result code should use code values")
        void constructorWithResultCodeShouldUseCodeValues() {
            OpenWebException exception = new OpenWebException(CommonResultCode.BAD_REQUEST);

            assertThat(exception.getCode()).isEqualTo(CommonResultCode.BAD_REQUEST.getCode());
            assertThat(exception.getMessage()).isEqualTo(CommonResultCode.BAD_REQUEST.getMessage());
            assertThat(exception.getHttpStatus()).isEqualTo(CommonResultCode.BAD_REQUEST.getHttpStatus());
        }

        @Test
        @DisplayName("constructor with result code and custom message should use custom message")
        void constructorWithResultCodeAndCustomMessageShouldUseCustomMessage() {
            OpenWebException exception = new OpenWebException(CommonResultCode.BAD_REQUEST, "Custom message");

            assertThat(exception.getCode()).isEqualTo(CommonResultCode.BAD_REQUEST.getCode());
            assertThat(exception.getMessage()).isEqualTo("Custom message");
        }

        @Test
        @DisplayName("constructor with message and cause should set both")
        void constructorWithMessageAndCauseShouldSetBoth() {
            Exception cause = new RuntimeException("Root cause");
            OpenWebException exception = new OpenWebException("Test message", cause);

            assertThat(exception.getMessage()).isEqualTo("Test message");
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("constructor with result code and cause should set both")
        void constructorWithResultCodeAndCauseShouldSetBoth() {
            Exception cause = new RuntimeException("Root cause");
            OpenWebException exception = new OpenWebException(CommonResultCode.INTERNAL_ERROR, cause);

            assertThat(exception.getCode()).isEqualTo(CommonResultCode.INTERNAL_ERROR.getCode());
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("badRequest should create bad request exception")
        void badRequestShouldCreateBadRequestException() {
            OpenWebException exception = OpenWebException.badRequest("Invalid input");

            assertThat(exception.getCode()).isEqualTo(CommonResultCode.BAD_REQUEST.getCode());
            assertThat(exception.getMessage()).isEqualTo("Invalid input");
            assertThat(exception.getHttpStatus()).isEqualTo(400);
        }

        @Test
        @DisplayName("unauthorized should create unauthorized exception")
        void unauthorizedShouldCreateUnauthorizedException() {
            OpenWebException exception = OpenWebException.unauthorized("Please login");

            assertThat(exception.getCode()).isEqualTo(CommonResultCode.UNAUTHORIZED.getCode());
            assertThat(exception.getHttpStatus()).isEqualTo(401);
        }

        @Test
        @DisplayName("forbidden should create forbidden exception")
        void forbiddenShouldCreateForbiddenException() {
            OpenWebException exception = OpenWebException.forbidden("Access denied");

            assertThat(exception.getCode()).isEqualTo(CommonResultCode.FORBIDDEN.getCode());
            assertThat(exception.getHttpStatus()).isEqualTo(403);
        }

        @Test
        @DisplayName("notFound should create not found exception")
        void notFoundShouldCreateNotFoundException() {
            OpenWebException exception = OpenWebException.notFound("Resource not found");

            assertThat(exception.getCode()).isEqualTo(CommonResultCode.NOT_FOUND.getCode());
            assertThat(exception.getHttpStatus()).isEqualTo(404);
        }

        @Test
        @DisplayName("internalError should create internal error exception")
        void internalErrorShouldCreateInternalErrorException() {
            OpenWebException exception = OpenWebException.internalError("Server error");

            assertThat(exception.getCode()).isEqualTo(CommonResultCode.INTERNAL_ERROR.getCode());
            assertThat(exception.getHttpStatus()).isEqualTo(500);
        }

        @Test
        @DisplayName("internalError with cause should include cause")
        void internalErrorWithCauseShouldIncludeCause() {
            Exception cause = new RuntimeException("Root cause");
            OpenWebException exception = OpenWebException.internalError("Server error", cause);

            assertThat(exception.getMessage()).isEqualTo("Server error");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("should extend RuntimeException")
        void shouldExtendRuntimeException() {
            OpenWebException exception = new OpenWebException("Test");

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }
}
