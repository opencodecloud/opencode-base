package cloud.opencode.base.web.problem;

import cloud.opencode.base.web.exception.OpenWebException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProblemDetail")
class ProblemDetailTest {

    @Nested
    @DisplayName("of(int, String, String)")
    class OfStatusTitleDetail {

        @Test
        @DisplayName("should create with explicit title")
        void shouldCreateWithExplicitTitle() {
            ProblemDetail pd = ProblemDetail.of(404, "Resource Missing", "User 42 not found");
            assertThat(pd.type()).isEqualTo("about:blank");
            assertThat(pd.title()).isEqualTo("Resource Missing");
            assertThat(pd.status()).isEqualTo(404);
            assertThat(pd.detail()).isEqualTo("User 42 not found");
            assertThat(pd.instance()).isNull();
            assertThat(pd.extensions()).isEmpty();
        }
    }

    @Nested
    @DisplayName("of(int, String)")
    class OfStatusDetail {

        @Test
        @DisplayName("should auto-derive title from status 400")
        void shouldDeriveTitleFrom400() {
            ProblemDetail pd = ProblemDetail.of(400, "Invalid email format");
            assertThat(pd.title()).isEqualTo("Bad Request");
            assertThat(pd.detail()).isEqualTo("Invalid email format");
        }

        @Test
        @DisplayName("should auto-derive title from status 401")
        void shouldDeriveTitleFrom401() {
            ProblemDetail pd = ProblemDetail.of(401, "Token expired");
            assertThat(pd.title()).isEqualTo("Unauthorized");
        }

        @Test
        @DisplayName("should auto-derive title from status 403")
        void shouldDeriveTitleFrom403() {
            ProblemDetail pd = ProblemDetail.of(403, "Access denied");
            assertThat(pd.title()).isEqualTo("Forbidden");
        }

        @Test
        @DisplayName("should auto-derive title from status 404")
        void shouldDeriveTitleFrom404() {
            ProblemDetail pd = ProblemDetail.of(404, "Not found");
            assertThat(pd.title()).isEqualTo("Not Found");
        }

        @Test
        @DisplayName("should auto-derive title from status 405")
        void shouldDeriveTitleFrom405() {
            ProblemDetail pd = ProblemDetail.of(405, "POST not allowed");
            assertThat(pd.title()).isEqualTo("Method Not Allowed");
        }

        @Test
        @DisplayName("should auto-derive title from status 409")
        void shouldDeriveTitleFrom409() {
            ProblemDetail pd = ProblemDetail.of(409, "Version conflict");
            assertThat(pd.title()).isEqualTo("Conflict");
        }

        @Test
        @DisplayName("should auto-derive title from status 422")
        void shouldDeriveTitleFrom422() {
            ProblemDetail pd = ProblemDetail.of(422, "Invalid entity");
            assertThat(pd.title()).isEqualTo("Unprocessable Entity");
        }

        @Test
        @DisplayName("should auto-derive title from status 429")
        void shouldDeriveTitleFrom429() {
            ProblemDetail pd = ProblemDetail.of(429, "Rate limit exceeded");
            assertThat(pd.title()).isEqualTo("Too Many Requests");
        }

        @Test
        @DisplayName("should auto-derive title from status 500")
        void shouldDeriveTitleFrom500() {
            ProblemDetail pd = ProblemDetail.of(500, "Something went wrong");
            assertThat(pd.title()).isEqualTo("Internal Server Error");
        }

        @Test
        @DisplayName("should auto-derive title from status 502")
        void shouldDeriveTitleFrom502() {
            ProblemDetail pd = ProblemDetail.of(502, "Upstream failed");
            assertThat(pd.title()).isEqualTo("Bad Gateway");
        }

        @Test
        @DisplayName("should auto-derive title from status 503")
        void shouldDeriveTitleFrom503() {
            ProblemDetail pd = ProblemDetail.of(503, "Temporarily unavailable");
            assertThat(pd.title()).isEqualTo("Service Unavailable");
        }

        @Test
        @DisplayName("should auto-derive title from status 504")
        void shouldDeriveTitleFrom504() {
            ProblemDetail pd = ProblemDetail.of(504, "Upstream timeout");
            assertThat(pd.title()).isEqualTo("Gateway Timeout");
        }

        @Test
        @DisplayName("should use Unknown Error for unmapped status")
        void shouldUseUnknownForUnmappedStatus() {
            ProblemDetail pd = ProblemDetail.of(418, "I'm a teapot");
            assertThat(pd.title()).isEqualTo("Unknown Error");
        }
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTest {

        @Test
        @DisplayName("should build with all fields")
        void shouldBuildWithAllFields() {
            ProblemDetail pd = ProblemDetail.builder()
                    .type("https://example.com/probs/out-of-credit")
                    .title("You do not have enough credit.")
                    .status(403)
                    .detail("Your current balance is 30, but that costs 50.")
                    .instance("/account/12345/msgs/abc")
                    .extension("balance", 30)
                    .extension("accounts", new String[]{"/account/12345", "/account/67890"})
                    .build();

            assertThat(pd.type()).isEqualTo("https://example.com/probs/out-of-credit");
            assertThat(pd.title()).isEqualTo("You do not have enough credit.");
            assertThat(pd.status()).isEqualTo(403);
            assertThat(pd.detail()).isEqualTo("Your current balance is 30, but that costs 50.");
            assertThat(pd.instance()).isEqualTo("/account/12345/msgs/abc");
            assertThat(pd.extensions()).containsKey("balance");
            assertThat(pd.extensions().get("balance")).isEqualTo(30);
            assertThat(pd.extensions()).containsKey("accounts");
        }

        @Test
        @DisplayName("should default type to about:blank")
        void shouldDefaultType() {
            ProblemDetail pd = ProblemDetail.builder()
                    .status(500)
                    .detail("Error")
                    .build();
            assertThat(pd.type()).isEqualTo("about:blank");
        }

        @Test
        @DisplayName("should reject null extension key")
        void shouldRejectNullExtensionKey() {
            assertThatThrownBy(() -> ProblemDetail.builder().extension(null, "v"))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("fromException")
    class FromExceptionTest {

        @Test
        @DisplayName("should create from OpenWebException without internal code by default")
        void shouldCreateFromException() {
            OpenWebException ex = OpenWebException.notFound("User not found");
            ProblemDetail pd = ProblemDetail.fromException(ex);

            assertThat(pd.status()).isEqualTo(404);
            assertThat(pd.title()).isEqualTo("Not Found");
            assertThat(pd.detail()).isEqualTo("User not found");
            assertThat(pd.type()).isEqualTo("about:blank");
            assertThat(pd.extensions()).isEmpty();
        }

        @Test
        @DisplayName("should include internal code when explicitly requested")
        void shouldIncludeCodeWhenRequested() {
            OpenWebException ex = OpenWebException.notFound("User not found");
            ProblemDetail pd = ProblemDetail.fromException(ex, true);

            assertThat(pd.extensions()).containsEntry("code", ex.getCode());
        }

        @Test
        @DisplayName("should create from OpenWebException with 500 status")
        void shouldCreateFrom500Exception() {
            OpenWebException ex = OpenWebException.internalError("Server failure");
            ProblemDetail pd = ProblemDetail.fromException(ex);

            assertThat(pd.status()).isEqualTo(500);
            assertThat(pd.title()).isEqualTo("Internal Server Error");
            assertThat(pd.detail()).isEqualTo("Server failure");
        }

        @Test
        @DisplayName("should reject null exception")
        void shouldRejectNullException() {
            assertThatThrownBy(() -> ProblemDetail.fromException(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("hasExtensions")
    class HasExtensionsTest {

        @Test
        @DisplayName("should return false when no extensions")
        void shouldReturnFalseWhenEmpty() {
            ProblemDetail pd = ProblemDetail.of(404, "Not found");
            assertThat(pd.hasExtensions()).isFalse();
        }

        @Test
        @DisplayName("should return true when extensions present")
        void shouldReturnTrueWhenPresent() {
            ProblemDetail pd = ProblemDetail.builder()
                    .status(400)
                    .extension("field", "email")
                    .build();
            assertThat(pd.hasExtensions()).isTrue();
        }
    }

    @Nested
    @DisplayName("getContentType")
    class GetContentTypeTest {

        @Test
        @DisplayName("should return application/problem+json")
        void shouldReturnProblemJson() {
            ProblemDetail pd = ProblemDetail.of(400, "Error");
            assertThat(pd.getContentType()).isEqualTo("application/problem+json");
        }
    }

    @Nested
    @DisplayName("CONTENT_TYPE constant")
    class ContentTypeConstant {

        @Test
        @DisplayName("should be application/problem+json")
        void shouldBeCorrect() {
            assertThat(ProblemDetail.CONTENT_TYPE).isEqualTo("application/problem+json");
        }
    }

    @Nested
    @DisplayName("Immutability")
    class ImmutabilityTest {

        @Test
        @DisplayName("extensions map should be unmodifiable")
        void extensionsShouldBeUnmodifiable() {
            ProblemDetail pd = ProblemDetail.builder()
                    .status(400)
                    .extension("key", "value")
                    .build();
            assertThatThrownBy(() -> pd.extensions().put("another", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("null extensions should produce empty unmodifiable map")
        void nullExtensionsShouldBeEmptyMap() {
            ProblemDetail pd = new ProblemDetail("about:blank", "Test", 400, "detail", null, null);
            assertThat(pd.extensions()).isEmpty();
            assertThatThrownBy(() -> pd.extensions().put("k", "v"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Type defaults")
    class TypeDefaultsTest {

        @Test
        @DisplayName("null type should default to about:blank")
        void nullTypeShouldDefault() {
            ProblemDetail pd = new ProblemDetail(null, "Test", 400, "detail", null, null);
            assertThat(pd.type()).isEqualTo("about:blank");
        }

        @Test
        @DisplayName("blank type should default to about:blank")
        void blankTypeShouldDefault() {
            ProblemDetail pd = new ProblemDetail("  ", "Test", 400, "detail", null, null);
            assertThat(pd.type()).isEqualTo("about:blank");
        }
    }

    @Nested
    @DisplayName("Record equality")
    class EqualityTest {

        @Test
        @DisplayName("equal ProblemDetails should be equal")
        void shouldBeEqual() {
            ProblemDetail a = ProblemDetail.of(404, "Not Found", "gone");
            ProblemDetail b = ProblemDetail.of(404, "Not Found", "gone");
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("different ProblemDetails should not be equal")
        void shouldNotBeEqual() {
            ProblemDetail a = ProblemDetail.of(404, "gone");
            ProblemDetail b = ProblemDetail.of(500, "error");
            assertThat(a).isNotEqualTo(b);
        }
    }
}
