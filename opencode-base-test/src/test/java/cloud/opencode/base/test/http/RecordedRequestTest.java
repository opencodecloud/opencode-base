package cloud.opencode.base.test.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RecordedRequest")
class RecordedRequestTest {

    @Nested
    @DisplayName("record accessors")
    class RecordAccessors {

        @Test
        @DisplayName("should return method")
        void shouldReturnMethod() {
            RecordedRequest req = new RecordedRequest("GET", "/api", Map.of(), new byte[0]);
            assertThat(req.method()).isEqualTo("GET");
        }

        @Test
        @DisplayName("should return path")
        void shouldReturnPath() {
            RecordedRequest req = new RecordedRequest("GET", "/api/users?id=1", Map.of(), new byte[0]);
            assertThat(req.path()).isEqualTo("/api/users?id=1");
        }

        @Test
        @DisplayName("should return headers")
        void shouldReturnHeaders() {
            Map<String, String> headers = Map.of("content-type", "application/json");
            RecordedRequest req = new RecordedRequest("POST", "/api", headers, new byte[0]);
            assertThat(req.headers()).containsEntry("content-type", "application/json");
        }

        @Test
        @DisplayName("should return body bytes")
        void shouldReturnBody() {
            byte[] body = "hello".getBytes(StandardCharsets.UTF_8);
            RecordedRequest req = new RecordedRequest("POST", "/api", Map.of(), body);
            assertThat(req.body()).isEqualTo(body);
        }
    }

    @Nested
    @DisplayName("bodyAsString")
    class BodyAsString {

        @Test
        @DisplayName("should convert body to UTF-8 string")
        void shouldConvertToString() {
            byte[] body = "test body".getBytes(StandardCharsets.UTF_8);
            RecordedRequest req = new RecordedRequest("POST", "/api", Map.of(), body);
            assertThat(req.bodyAsString()).isEqualTo("test body");
        }

        @Test
        @DisplayName("should return empty string for empty body")
        void shouldReturnEmptyForEmptyBody() {
            RecordedRequest req = new RecordedRequest("GET", "/api", Map.of(), new byte[0]);
            assertThat(req.bodyAsString()).isEmpty();
        }
    }

    @Nested
    @DisplayName("header")
    class Header {

        @Test
        @DisplayName("should look up header by lowercase name")
        void shouldLookupByLowercase() {
            Map<String, String> headers = Map.of("authorization", "Bearer token123");
            RecordedRequest req = new RecordedRequest("GET", "/api", headers, new byte[0]);
            assertThat(req.header("Authorization")).isEqualTo("Bearer token123");
        }

        @Test
        @DisplayName("should return null for missing header")
        void shouldReturnNullForMissing() {
            RecordedRequest req = new RecordedRequest("GET", "/api", Map.of(), new byte[0]);
            assertThat(req.header("X-Missing")).isNull();
        }

        @Test
        @DisplayName("should handle case-insensitive lookup")
        void shouldHandleCaseInsensitive() {
            Map<String, String> headers = Map.of("content-type", "text/plain");
            RecordedRequest req = new RecordedRequest("GET", "/api", headers, new byte[0]);
            assertThat(req.header("Content-Type")).isEqualTo("text/plain");
            assertThat(req.header("CONTENT-TYPE")).isEqualTo("text/plain");
        }
    }
}
