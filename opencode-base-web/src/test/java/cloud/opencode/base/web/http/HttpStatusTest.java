package cloud.opencode.base.web.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("HttpStatus")
class HttpStatusTest {

    @Nested
    @DisplayName("getCode()")
    class GetCode {

        @Test
        @DisplayName("should return correct status codes")
        void shouldReturnCorrectCodes() {
            assertThat(HttpStatus.OK.getCode()).isEqualTo(200);
            assertThat(HttpStatus.CREATED.getCode()).isEqualTo(201);
            assertThat(HttpStatus.NOT_FOUND.getCode()).isEqualTo(404);
            assertThat(HttpStatus.INTERNAL_SERVER_ERROR.getCode()).isEqualTo(500);
            assertThat(HttpStatus.CONTINUE.getCode()).isEqualTo(100);
            assertThat(HttpStatus.MOVED_PERMANENTLY.getCode()).isEqualTo(301);
        }
    }

    @Nested
    @DisplayName("getReason()")
    class GetReason {

        @Test
        @DisplayName("should return correct reason phrases")
        void shouldReturnReasonPhrases() {
            assertThat(HttpStatus.OK.getReason()).isEqualTo("OK");
            assertThat(HttpStatus.NOT_FOUND.getReason()).isEqualTo("Not Found");
            assertThat(HttpStatus.INTERNAL_SERVER_ERROR.getReason()).isEqualTo("Internal Server Error");
            assertThat(HttpStatus.IM_A_TEAPOT.getReason()).isEqualTo("I'm a teapot");
        }
    }

    @Nested
    @DisplayName("isInformational()")
    class IsInformational {

        @Test
        @DisplayName("should return true for 1xx codes")
        void shouldReturnTrue() {
            assertThat(HttpStatus.CONTINUE.isInformational()).isTrue();
            assertThat(HttpStatus.SWITCHING_PROTOCOLS.isInformational()).isTrue();
        }

        @Test
        @DisplayName("should return false for non-1xx codes")
        void shouldReturnFalse() {
            assertThat(HttpStatus.OK.isInformational()).isFalse();
            assertThat(HttpStatus.NOT_FOUND.isInformational()).isFalse();
        }
    }

    @Nested
    @DisplayName("isSuccess()")
    class IsSuccess {

        @Test
        @DisplayName("should return true for 2xx codes")
        void shouldReturnTrue() {
            assertThat(HttpStatus.OK.isSuccess()).isTrue();
            assertThat(HttpStatus.CREATED.isSuccess()).isTrue();
            assertThat(HttpStatus.NO_CONTENT.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("should return false for non-2xx codes")
        void shouldReturnFalse() {
            assertThat(HttpStatus.NOT_FOUND.isSuccess()).isFalse();
            assertThat(HttpStatus.CONTINUE.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("isRedirection()")
    class IsRedirection {

        @Test
        @DisplayName("should return true for 3xx codes")
        void shouldReturnTrue() {
            assertThat(HttpStatus.MOVED_PERMANENTLY.isRedirection()).isTrue();
            assertThat(HttpStatus.FOUND.isRedirection()).isTrue();
            assertThat(HttpStatus.TEMPORARY_REDIRECT.isRedirection()).isTrue();
        }

        @Test
        @DisplayName("should return false for non-3xx codes")
        void shouldReturnFalse() {
            assertThat(HttpStatus.OK.isRedirection()).isFalse();
        }
    }

    @Nested
    @DisplayName("isClientError()")
    class IsClientError {

        @Test
        @DisplayName("should return true for 4xx codes")
        void shouldReturnTrue() {
            assertThat(HttpStatus.BAD_REQUEST.isClientError()).isTrue();
            assertThat(HttpStatus.UNAUTHORIZED.isClientError()).isTrue();
            assertThat(HttpStatus.FORBIDDEN.isClientError()).isTrue();
            assertThat(HttpStatus.NOT_FOUND.isClientError()).isTrue();
        }

        @Test
        @DisplayName("should return false for non-4xx codes")
        void shouldReturnFalse() {
            assertThat(HttpStatus.OK.isClientError()).isFalse();
            assertThat(HttpStatus.INTERNAL_SERVER_ERROR.isClientError()).isFalse();
        }
    }

    @Nested
    @DisplayName("isServerError()")
    class IsServerError {

        @Test
        @DisplayName("should return true for 5xx codes")
        void shouldReturnTrue() {
            assertThat(HttpStatus.INTERNAL_SERVER_ERROR.isServerError()).isTrue();
            assertThat(HttpStatus.BAD_GATEWAY.isServerError()).isTrue();
            assertThat(HttpStatus.SERVICE_UNAVAILABLE.isServerError()).isTrue();
        }

        @Test
        @DisplayName("should return false for non-5xx codes")
        void shouldReturnFalse() {
            assertThat(HttpStatus.OK.isServerError()).isFalse();
            assertThat(HttpStatus.NOT_FOUND.isServerError()).isFalse();
        }
    }

    @Nested
    @DisplayName("isError()")
    class IsError {

        @Test
        @DisplayName("should return true for 4xx and 5xx codes")
        void shouldReturnTrue() {
            assertThat(HttpStatus.BAD_REQUEST.isError()).isTrue();
            assertThat(HttpStatus.NOT_FOUND.isError()).isTrue();
            assertThat(HttpStatus.INTERNAL_SERVER_ERROR.isError()).isTrue();
        }

        @Test
        @DisplayName("should return false for 1xx, 2xx, 3xx codes")
        void shouldReturnFalse() {
            assertThat(HttpStatus.CONTINUE.isError()).isFalse();
            assertThat(HttpStatus.OK.isError()).isFalse();
            assertThat(HttpStatus.MOVED_PERMANENTLY.isError()).isFalse();
        }
    }

    @Nested
    @DisplayName("fromCode(int)")
    class FromCode {

        @Test
        @DisplayName("should return status for valid code")
        void shouldReturnForValid() {
            assertThat(HttpStatus.fromCode(200)).isEqualTo(HttpStatus.OK);
            assertThat(HttpStatus.fromCode(404)).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(HttpStatus.fromCode(500)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("should return null for unknown code")
        void shouldReturnNullForUnknown() {
            assertThat(HttpStatus.fromCode(999)).isNull();
            assertThat(HttpStatus.fromCode(0)).isNull();
        }
    }

    @Nested
    @DisplayName("valueOf(int)")
    class ValueOfInt {

        @Test
        @DisplayName("should return status for valid code")
        void shouldReturnForValid() {
            assertThat(HttpStatus.valueOf(200)).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("should throw for unknown code")
        void shouldThrowForUnknown() {
            assertThatThrownBy(() -> HttpStatus.valueOf(999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown HTTP status code");
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTest {

        @Test
        @DisplayName("should return code and reason")
        void shouldReturnCodeAndReason() {
            assertThat(HttpStatus.OK.toString()).isEqualTo("200 OK");
            assertThat(HttpStatus.NOT_FOUND.toString()).isEqualTo("404 Not Found");
        }
    }

    @Nested
    @DisplayName("All status codes")
    class AllStatusCodes {

        @Test
        @DisplayName("should have expected number of status codes")
        void shouldHaveExpectedCount() {
            assertThat(HttpStatus.values().length).isGreaterThan(40);
        }

        @Test
        @DisplayName("every status code should be unique")
        void shouldBeUnique() {
            HttpStatus[] values = HttpStatus.values();
            long distinctCodes = java.util.Arrays.stream(values)
                    .mapToInt(HttpStatus::getCode).distinct().count();
            assertThat(distinctCodes).isEqualTo(values.length);
        }
    }
}
