package cloud.opencode.base.test.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RequestMatcher")
class RequestMatcherTest {

    private RecordedRequest request(String method, String path) {
        return new RecordedRequest(method, path, Map.of(), new byte[0]);
    }

    @Nested
    @DisplayName("get")
    class GetMatcher {

        @Test
        @DisplayName("should match GET request with correct path")
        void shouldMatchGet() {
            RequestMatcher matcher = RequestMatcher.get("/api/users");
            assertThat(matcher.matches(request("GET", "/api/users"))).isTrue();
        }

        @Test
        @DisplayName("should not match POST request")
        void shouldNotMatchPost() {
            RequestMatcher matcher = RequestMatcher.get("/api/users");
            assertThat(matcher.matches(request("POST", "/api/users"))).isFalse();
        }

        @Test
        @DisplayName("should not match different path")
        void shouldNotMatchDifferentPath() {
            RequestMatcher matcher = RequestMatcher.get("/api/users");
            assertThat(matcher.matches(request("GET", "/api/orders"))).isFalse();
        }

        @Test
        @DisplayName("should match path ignoring query string")
        void shouldMatchIgnoringQuery() {
            RequestMatcher matcher = RequestMatcher.get("/api/users");
            assertThat(matcher.matches(request("GET", "/api/users?page=1"))).isTrue();
        }
    }

    @Nested
    @DisplayName("post")
    class PostMatcher {

        @Test
        @DisplayName("should match POST request")
        void shouldMatchPost() {
            RequestMatcher matcher = RequestMatcher.post("/api/users");
            assertThat(matcher.matches(request("POST", "/api/users"))).isTrue();
        }

        @Test
        @DisplayName("should not match GET request")
        void shouldNotMatchGet() {
            RequestMatcher matcher = RequestMatcher.post("/api/users");
            assertThat(matcher.matches(request("GET", "/api/users"))).isFalse();
        }
    }

    @Nested
    @DisplayName("put")
    class PutMatcher {

        @Test
        @DisplayName("should match PUT request")
        void shouldMatchPut() {
            RequestMatcher matcher = RequestMatcher.put("/api/users");
            assertThat(matcher.matches(request("PUT", "/api/users"))).isTrue();
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteMatcher {

        @Test
        @DisplayName("should match DELETE request")
        void shouldMatchDelete() {
            RequestMatcher matcher = RequestMatcher.delete("/api/users");
            assertThat(matcher.matches(request("DELETE", "/api/users"))).isTrue();
        }
    }

    @Nested
    @DisplayName("any")
    class AnyMatcher {

        @Test
        @DisplayName("should match any method with correct path")
        void shouldMatchAnyMethod() {
            RequestMatcher matcher = RequestMatcher.any("/api/users");
            assertThat(matcher.matches(request("GET", "/api/users"))).isTrue();
            assertThat(matcher.matches(request("POST", "/api/users"))).isTrue();
            assertThat(matcher.matches(request("PUT", "/api/users"))).isTrue();
            assertThat(matcher.matches(request("DELETE", "/api/users"))).isTrue();
        }

        @Test
        @DisplayName("should not match different path")
        void shouldNotMatchDifferentPath() {
            RequestMatcher matcher = RequestMatcher.any("/api/users");
            assertThat(matcher.matches(request("GET", "/api/orders"))).isFalse();
        }
    }

    @Nested
    @DisplayName("method")
    class MethodMatcher {

        @Test
        @DisplayName("should match custom method case-insensitively")
        void shouldMatchCaseInsensitive() {
            RequestMatcher matcher = RequestMatcher.method("patch", "/api/users");
            assertThat(matcher.matches(request("PATCH", "/api/users"))).isTrue();
        }
    }

    @Nested
    @DisplayName("functional interface")
    class FunctionalInterface {

        @Test
        @DisplayName("should work as lambda")
        void shouldWorkAsLambda() {
            RequestMatcher matcher = r -> r.path().startsWith("/api");
            assertThat(matcher.matches(request("GET", "/api/test"))).isTrue();
            assertThat(matcher.matches(request("GET", "/other"))).isFalse();
        }
    }

    @Nested
    @DisplayName("null path handling")
    class NullPathHandling {

        @Test
        @DisplayName("should handle null actual path")
        void shouldHandleNullActualPath() {
            RequestMatcher matcher = RequestMatcher.get("/api");
            RecordedRequest req = new RecordedRequest("GET", null, Map.of(), new byte[0]);
            assertThat(matcher.matches(req)).isFalse();
        }
    }
}
