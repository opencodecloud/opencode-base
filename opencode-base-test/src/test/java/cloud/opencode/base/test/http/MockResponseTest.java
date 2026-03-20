package cloud.opencode.base.test.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MockResponse")
class MockResponseTest {

    @Nested
    @DisplayName("ok factory methods")
    class OkFactory {

        @Test
        @DisplayName("should create 200 response with body")
        void shouldCreateOkWithBody() {
            MockResponse response = MockResponse.ok("hello");
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isEqualTo("hello");
        }

        @Test
        @DisplayName("should create 200 response with empty body")
        void shouldCreateOkEmpty() {
            MockResponse response = MockResponse.ok();
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isEmpty();
        }
    }

    @Nested
    @DisplayName("notFound factory methods")
    class NotFoundFactory {

        @Test
        @DisplayName("should create 404 response")
        void shouldCreateNotFound() {
            MockResponse response = MockResponse.notFound();
            assertThat(response.statusCode()).isEqualTo(404);
            assertThat(response.body()).isEqualTo("Not Found");
        }

        @Test
        @DisplayName("should create 404 response with custom body")
        void shouldCreateNotFoundWithBody() {
            MockResponse response = MockResponse.notFound("custom 404");
            assertThat(response.statusCode()).isEqualTo(404);
            assertThat(response.body()).isEqualTo("custom 404");
        }
    }

    @Nested
    @DisplayName("serverError factory methods")
    class ServerErrorFactory {

        @Test
        @DisplayName("should create 500 response")
        void shouldCreateServerError() {
            MockResponse response = MockResponse.serverError();
            assertThat(response.statusCode()).isEqualTo(500);
            assertThat(response.body()).isEqualTo("Internal Server Error");
        }

        @Test
        @DisplayName("should create 500 response with custom body")
        void shouldCreateServerErrorWithBody() {
            MockResponse response = MockResponse.serverError("oops");
            assertThat(response.statusCode()).isEqualTo(500);
            assertThat(response.body()).isEqualTo("oops");
        }
    }

    @Nested
    @DisplayName("withStatus factory methods")
    class WithStatusFactory {

        @Test
        @DisplayName("should create response with custom status")
        void shouldCreateWithStatus() {
            MockResponse response = MockResponse.withStatus(201);
            assertThat(response.statusCode()).isEqualTo(201);
            assertThat(response.body()).isEmpty();
        }

        @Test
        @DisplayName("should create response with custom status and body")
        void shouldCreateWithStatusAndBody() {
            MockResponse response = MockResponse.withStatus(301, "redirect");
            assertThat(response.statusCode()).isEqualTo(301);
            assertThat(response.body()).isEqualTo("redirect");
        }
    }

    @Nested
    @DisplayName("withHeader")
    class WithHeader {

        @Test
        @DisplayName("should add header and return new instance")
        void shouldAddHeader() {
            MockResponse original = MockResponse.ok("body");
            MockResponse withHeader = original.withHeader("Content-Type", "application/json");
            assertThat(withHeader.headers()).containsEntry("Content-Type", "application/json");
            // Original should be unchanged
            assertThat(original.headers()).doesNotContainKey("Content-Type");
        }

        @Test
        @DisplayName("should chain multiple headers")
        void shouldChainHeaders() {
            MockResponse response = MockResponse.ok()
                    .withHeader("X-A", "1")
                    .withHeader("X-B", "2");
            assertThat(response.headers()).containsEntry("X-A", "1")
                    .containsEntry("X-B", "2");
        }
    }

    @Nested
    @DisplayName("withBody")
    class WithBody {

        @Test
        @DisplayName("should replace body and return new instance")
        void shouldReplaceBody() {
            MockResponse original = MockResponse.ok("old");
            MockResponse replaced = original.withBody("new");
            assertThat(replaced.body()).isEqualTo("new");
            assertThat(original.body()).isEqualTo("old");
        }
    }

    @Nested
    @DisplayName("headers")
    class Headers {

        @Test
        @DisplayName("should return immutable copy of headers")
        void shouldReturnImmutableHeaders() {
            MockResponse response = MockResponse.ok().withHeader("X-Test", "value");
            var headers = response.headers();
            assertThat(headers).containsEntry("X-Test", "value");
        }

        @Test
        @DisplayName("should return empty map when no headers")
        void shouldReturnEmptyHeaders() {
            MockResponse response = MockResponse.ok();
            assertThat(response.headers()).isEmpty();
        }
    }

    @Nested
    @DisplayName("null body handling")
    class NullBody {

        @Test
        @DisplayName("should treat null body as empty string")
        void shouldTreatNullAsEmpty() {
            MockResponse response = MockResponse.ok(null);
            assertThat(response.body()).isEmpty();
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("should contain status code and body")
        void shouldContainInfo() {
            MockResponse response = MockResponse.ok("test");
            assertThat(response.toString()).contains("200").contains("test");
        }
    }
}
