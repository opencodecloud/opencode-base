package cloud.opencode.base.web.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("HttpMethod")
class HttpMethodTest {

    @Nested
    @DisplayName("hasRequestBody()")
    class HasRequestBody {

        @Test
        @DisplayName("POST, PUT, PATCH should have request body")
        void shouldHaveBody() {
            assertThat(HttpMethod.POST.hasRequestBody()).isTrue();
            assertThat(HttpMethod.PUT.hasRequestBody()).isTrue();
            assertThat(HttpMethod.PATCH.hasRequestBody()).isTrue();
        }

        @Test
        @DisplayName("GET, DELETE, HEAD, OPTIONS, TRACE should not have request body")
        void shouldNotHaveBody() {
            assertThat(HttpMethod.GET.hasRequestBody()).isFalse();
            assertThat(HttpMethod.DELETE.hasRequestBody()).isFalse();
            assertThat(HttpMethod.HEAD.hasRequestBody()).isFalse();
            assertThat(HttpMethod.OPTIONS.hasRequestBody()).isFalse();
            assertThat(HttpMethod.TRACE.hasRequestBody()).isFalse();
            assertThat(HttpMethod.CONNECT.hasRequestBody()).isFalse();
        }
    }

    @Nested
    @DisplayName("isIdempotent()")
    class IsIdempotent {

        @Test
        @DisplayName("GET, PUT, DELETE, HEAD, OPTIONS, TRACE should be idempotent")
        void shouldBeIdempotent() {
            assertThat(HttpMethod.GET.isIdempotent()).isTrue();
            assertThat(HttpMethod.PUT.isIdempotent()).isTrue();
            assertThat(HttpMethod.DELETE.isIdempotent()).isTrue();
            assertThat(HttpMethod.HEAD.isIdempotent()).isTrue();
            assertThat(HttpMethod.OPTIONS.isIdempotent()).isTrue();
            assertThat(HttpMethod.TRACE.isIdempotent()).isTrue();
        }

        @Test
        @DisplayName("POST, PATCH, CONNECT should not be idempotent")
        void shouldNotBeIdempotent() {
            assertThat(HttpMethod.POST.isIdempotent()).isFalse();
            assertThat(HttpMethod.PATCH.isIdempotent()).isFalse();
            assertThat(HttpMethod.CONNECT.isIdempotent()).isFalse();
        }
    }

    @Nested
    @DisplayName("isSafe()")
    class IsSafe {

        @Test
        @DisplayName("GET, HEAD, OPTIONS, TRACE should be safe")
        void shouldBeSafe() {
            assertThat(HttpMethod.GET.isSafe()).isTrue();
            assertThat(HttpMethod.HEAD.isSafe()).isTrue();
            assertThat(HttpMethod.OPTIONS.isSafe()).isTrue();
            assertThat(HttpMethod.TRACE.isSafe()).isTrue();
        }

        @Test
        @DisplayName("POST, PUT, DELETE, PATCH, CONNECT should not be safe")
        void shouldNotBeSafe() {
            assertThat(HttpMethod.POST.isSafe()).isFalse();
            assertThat(HttpMethod.PUT.isSafe()).isFalse();
            assertThat(HttpMethod.DELETE.isSafe()).isFalse();
            assertThat(HttpMethod.PATCH.isSafe()).isFalse();
            assertThat(HttpMethod.CONNECT.isSafe()).isFalse();
        }
    }

    @Nested
    @DisplayName("fromString(String)")
    class FromString {

        @Test
        @DisplayName("should parse uppercase method name")
        void shouldParseUppercase() {
            assertThat(HttpMethod.fromString("GET")).isEqualTo(HttpMethod.GET);
            assertThat(HttpMethod.fromString("POST")).isEqualTo(HttpMethod.POST);
        }

        @Test
        @DisplayName("should parse lowercase method name")
        void shouldParseLowercase() {
            assertThat(HttpMethod.fromString("get")).isEqualTo(HttpMethod.GET);
            assertThat(HttpMethod.fromString("post")).isEqualTo(HttpMethod.POST);
        }

        @Test
        @DisplayName("should parse mixed case")
        void shouldParseMixedCase() {
            assertThat(HttpMethod.fromString("Get")).isEqualTo(HttpMethod.GET);
        }

        @Test
        @DisplayName("should throw for null")
        void shouldThrowForNull() {
            assertThatThrownBy(() -> HttpMethod.fromString(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must not be blank");
        }

        @Test
        @DisplayName("should throw for blank string")
        void shouldThrowForBlank() {
            assertThatThrownBy(() -> HttpMethod.fromString("  "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw for invalid method")
        void shouldThrowForInvalid() {
            assertThatThrownBy(() -> HttpMethod.fromString("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("isValid(String)")
    class IsValid {

        @Test
        @DisplayName("should return true for valid methods")
        void shouldReturnTrue() {
            assertThat(HttpMethod.isValid("GET")).isTrue();
            assertThat(HttpMethod.isValid("post")).isTrue();
            assertThat(HttpMethod.isValid("Delete")).isTrue();
        }

        @Test
        @DisplayName("should return false for invalid methods")
        void shouldReturnFalse() {
            assertThat(HttpMethod.isValid("INVALID")).isFalse();
            assertThat(HttpMethod.isValid(null)).isFalse();
            assertThat(HttpMethod.isValid("")).isFalse();
            assertThat(HttpMethod.isValid("  ")).isFalse();
        }
    }

    @Nested
    @DisplayName("All enum values")
    class AllValues {

        @Test
        @DisplayName("should have 9 HTTP methods")
        void shouldHave9Methods() {
            assertThat(HttpMethod.values()).hasSize(9);
        }
    }
}
