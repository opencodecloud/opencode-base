package cloud.opencode.base.test.http;

import cloud.opencode.base.test.exception.AssertionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RequestVerification")
class RequestVerificationTest {

    // ==================== Helpers ====================

    private RecordedRequest request(String method, String path) {
        return new RecordedRequest(method, path, Map.of(), new byte[0]);
    }

    private RecordedRequest requestWithBody(String method, String path, String body) {
        return new RecordedRequest(method, path, Map.of(),
                body.getBytes(StandardCharsets.UTF_8));
    }

    private RecordedRequest requestWithHeaders(String method, String path,
                                                Map<String, String> headers) {
        return new RecordedRequest(method, path, headers, new byte[0]);
    }

    private RequestVerification verification(RecordedRequest... requests) {
        return new RequestVerification(List.of(requests));
    }

    // ==================== Tests ====================

    @Nested
    @DisplayName("wasCalled()")
    class WasCalled {

        @Test
        @DisplayName("should pass when at least one matching request exists")
        void passWhenCalled() {
            var v = verification(request("GET", "/api/users"));

            assertThatCode(() -> v.that(RequestMatcher.get("/api/users")).wasCalled())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should fail when no matching requests exist")
        void failWhenNotCalled() {
            var v = verification(request("POST", "/api/users"));

            assertThatThrownBy(() -> v.that(RequestMatcher.get("/api/users")).wasCalled())
                    .isInstanceOf(AssertionException.class)
                    .hasMessageContaining("at least 1");
        }

        @Test
        @DisplayName("should fail when request list is empty")
        void failWhenEmpty() {
            var v = verification();

            assertThatThrownBy(() -> v.wasCalled())
                    .isInstanceOf(AssertionException.class);
        }
    }

    @Nested
    @DisplayName("wasCalled(int)")
    class WasCalledN {

        @Test
        @DisplayName("should pass when exact count matches")
        void passWhenExactMatch() {
            var v = verification(
                    request("GET", "/api/users"),
                    request("GET", "/api/users"),
                    request("POST", "/api/users"));

            assertThatCode(() -> v.that(RequestMatcher.get("/api/users")).wasCalled(2))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should fail when count does not match")
        void failWhenCountMismatch() {
            var v = verification(request("GET", "/api/users"));

            assertThatThrownBy(() -> v.that(RequestMatcher.get("/api/users")).wasCalled(2))
                    .isInstanceOf(AssertionException.class)
                    .hasMessageContaining("2")
                    .hasMessageContaining("1");
        }

        @Test
        @DisplayName("should pass when called 0 times and no matches")
        void passWhenZeroAndNone() {
            var v = verification(request("POST", "/api/users"));

            assertThatCode(() -> v.that(RequestMatcher.get("/api/users")).wasCalled(0))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should reject negative times")
        void rejectNegative() {
            var v = verification();

            assertThatThrownBy(() -> v.wasCalled(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("wasNeverCalled()")
    class WasNeverCalled {

        @Test
        @DisplayName("should pass when no matching requests")
        void passWhenNoMatches() {
            var v = verification(request("GET", "/api/users"));

            assertThatCode(() -> v.that(RequestMatcher.delete("/api/admin")).wasNeverCalled())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should fail when matching requests exist")
        void failWhenMatches() {
            var v = verification(request("DELETE", "/api/admin"));

            assertThatThrownBy(() -> v.that(RequestMatcher.delete("/api/admin")).wasNeverCalled())
                    .isInstanceOf(AssertionException.class)
                    .hasMessageContaining("0");
        }
    }

    @Nested
    @DisplayName("wasCalledAtLeast(int)")
    class WasCalledAtLeast {

        @Test
        @DisplayName("should pass when count >= minimum")
        void passWhenAboveMinimum() {
            var v = verification(
                    request("GET", "/api"),
                    request("GET", "/api"),
                    request("GET", "/api"));

            assertThatCode(() -> v.that(RequestMatcher.get("/api")).wasCalledAtLeast(2))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should pass when count == minimum")
        void passWhenExactMinimum() {
            var v = verification(request("GET", "/api"));

            assertThatCode(() -> v.that(RequestMatcher.get("/api")).wasCalledAtLeast(1))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should fail when count < minimum")
        void failWhenBelowMinimum() {
            var v = verification(request("GET", "/api"));

            assertThatThrownBy(() -> v.that(RequestMatcher.get("/api")).wasCalledAtLeast(3))
                    .isInstanceOf(AssertionException.class)
                    .hasMessageContaining("at least 3");
        }
    }

    @Nested
    @DisplayName("wasCalledAtMost(int)")
    class WasCalledAtMost {

        @Test
        @DisplayName("should pass when count <= maximum")
        void passWhenBelowMaximum() {
            var v = verification(request("GET", "/api"));

            assertThatCode(() -> v.that(RequestMatcher.get("/api")).wasCalledAtMost(3))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should pass when count == maximum")
        void passWhenExactMaximum() {
            var v = verification(request("GET", "/api"));

            assertThatCode(() -> v.that(RequestMatcher.get("/api")).wasCalledAtMost(1))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should fail when count > maximum")
        void failWhenAboveMaximum() {
            var v = verification(
                    request("GET", "/api"),
                    request("GET", "/api"),
                    request("GET", "/api"));

            assertThatThrownBy(() -> v.that(RequestMatcher.get("/api")).wasCalledAtMost(1))
                    .isInstanceOf(AssertionException.class)
                    .hasMessageContaining("at most 1");
        }
    }

    @Nested
    @DisplayName("withBody(String)")
    class WithBody {

        @Test
        @DisplayName("should pass when body matches exactly")
        void passWhenBodyMatches() {
            var v = verification(requestWithBody("POST", "/api", "{\"name\":\"Alice\"}"));

            assertThatCode(() -> v.that(RequestMatcher.post("/api"))
                    .wasCalled()
                    .withBody("{\"name\":\"Alice\"}"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should fail when body does not match")
        void failWhenBodyMismatch() {
            var v = verification(requestWithBody("POST", "/api", "{\"name\":\"Bob\"}"));

            assertThatThrownBy(() -> v.that(RequestMatcher.post("/api"))
                    .wasCalled()
                    .withBody("{\"name\":\"Alice\"}"))
                    .isInstanceOf(AssertionException.class)
                    .hasMessageContaining("Alice")
                    .hasMessageContaining("Bob");
        }

        @Test
        @DisplayName("should fail when no matching requests")
        void failWhenNoMatches() {
            var v = verification();

            assertThatThrownBy(() -> v.withBody("anything"))
                    .isInstanceOf(AssertionException.class)
                    .hasMessageContaining("no matching requests");
        }

        @Test
        @DisplayName("should reject null body")
        void rejectNull() {
            var v = verification(request("POST", "/api"));

            assertThatThrownBy(() -> v.that(RequestMatcher.post("/api")).withBody(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("withBodyContaining(String)")
    class WithBodyContaining {

        @Test
        @DisplayName("should pass when body contains substring")
        void passWhenContains() {
            var v = verification(requestWithBody("POST", "/api", "{\"name\":\"Alice\",\"age\":30}"));

            assertThatCode(() -> v.that(RequestMatcher.post("/api"))
                    .wasCalled()
                    .withBodyContaining("Alice"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should fail when body does not contain substring")
        void failWhenNotContains() {
            var v = verification(requestWithBody("POST", "/api", "{\"name\":\"Bob\"}"));

            assertThatThrownBy(() -> v.that(RequestMatcher.post("/api"))
                    .wasCalled()
                    .withBodyContaining("Alice"))
                    .isInstanceOf(AssertionException.class)
                    .hasMessageContaining("Alice");
        }

        @Test
        @DisplayName("should fail when no matching requests")
        void failWhenNoMatches() {
            var v = verification();

            assertThatThrownBy(() -> v.withBodyContaining("anything"))
                    .isInstanceOf(AssertionException.class)
                    .hasMessageContaining("no matching requests");
        }

        @Test
        @DisplayName("should reject null substring")
        void rejectNull() {
            var v = verification(request("POST", "/api"));

            assertThatThrownBy(() -> v.that(RequestMatcher.post("/api")).withBodyContaining(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("withHeader(String, String)")
    class WithHeader {

        @Test
        @DisplayName("should pass when header matches")
        void passWhenHeaderMatches() {
            var v = verification(requestWithHeaders("GET", "/api",
                    Map.of("authorization", "Bearer token123")));

            assertThatCode(() -> v.that(RequestMatcher.get("/api"))
                    .wasCalled()
                    .withHeader("Authorization", "Bearer token123"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should fail when header value does not match")
        void failWhenHeaderMismatch() {
            var v = verification(requestWithHeaders("GET", "/api",
                    Map.of("authorization", "Bearer old-token")));

            assertThatThrownBy(() -> v.that(RequestMatcher.get("/api"))
                    .wasCalled()
                    .withHeader("Authorization", "Bearer new-token"))
                    .isInstanceOf(AssertionException.class)
                    .hasMessageContaining("new-token")
                    .hasMessageContaining("old-token");
        }

        @Test
        @DisplayName("should fail when header is missing")
        void failWhenHeaderMissing() {
            var v = verification(request("GET", "/api"));

            assertThatThrownBy(() -> v.that(RequestMatcher.get("/api"))
                    .wasCalled()
                    .withHeader("Authorization", "Bearer token"))
                    .isInstanceOf(AssertionException.class);
        }

        @Test
        @DisplayName("should fail when no matching requests")
        void failWhenNoMatches() {
            var v = verification();

            assertThatThrownBy(() -> v.withHeader("X-Key", "val"))
                    .isInstanceOf(AssertionException.class)
                    .hasMessageContaining("no matching requests");
        }

        @Test
        @DisplayName("should reject null header name")
        void rejectNullName() {
            var v = verification(request("GET", "/api"));

            assertThatThrownBy(() -> v.withHeader(null, "value"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject null header value")
        void rejectNullValue() {
            var v = verification(request("GET", "/api"));

            assertThatThrownBy(() -> v.withHeader("name", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("that(RequestMatcher)")
    class ThatMatcher {

        @Test
        @DisplayName("should reject null matcher")
        void rejectNullMatcher() {
            var v = verification(request("GET", "/api"));

            assertThatThrownBy(() -> v.that(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should filter requests correctly")
        void filterRequests() {
            var v = verification(
                    request("GET", "/api/a"),
                    request("POST", "/api/b"),
                    request("GET", "/api/a"));

            v.that(RequestMatcher.get("/api/a"));

            assertThat(v.getMatchedRequests()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getMatchedRequests()")
    class GetMatchedRequests {

        @Test
        @DisplayName("should return all requests when no filter applied")
        void allRequestsWhenNoFilter() {
            var v = verification(
                    request("GET", "/a"),
                    request("POST", "/b"));

            assertThat(v.getMatchedRequests()).hasSize(2);
        }

        @Test
        @DisplayName("should return unmodifiable list")
        void unmodifiableList() {
            var v = verification(request("GET", "/api"));

            assertThatThrownBy(() -> v.getMatchedRequests().add(request("POST", "/x")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Fluent chaining")
    class FluentChaining {

        @Test
        @DisplayName("should support chaining wasCalled with withBody and withHeader")
        void chainMultipleAssertions() {
            var v = verification(new RecordedRequest(
                    "POST", "/api/users",
                    Map.of("content-type", "application/json"),
                    "{\"name\":\"Alice\"}".getBytes(StandardCharsets.UTF_8)));

            assertThatCode(() -> v.that(RequestMatcher.post("/api/users"))
                    .wasCalled(1)
                    .withBody("{\"name\":\"Alice\"}")
                    .withHeader("Content-Type", "application/json"))
                    .doesNotThrowAnyException();
        }
    }
}
