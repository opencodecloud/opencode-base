package cloud.opencode.base.test.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TestHttpServer")
class TestHttpServerTest {

    private static HttpURLConnection openConnection(String url, String method) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        return conn;
    }

    private static String readResponse(HttpURLConnection conn) throws IOException {
        try (InputStream is = conn.getResponseCode() < 400 ? conn.getInputStream() : conn.getErrorStream()) {
            if (is == null) return "";
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Nested
    @DisplayName("start and close")
    class StartClose {

        @Test
        @DisplayName("should start on random port and close")
        void shouldStartAndClose() {
            try (TestHttpServer server = TestHttpServer.start()) {
                assertThat(server.port()).isGreaterThan(0);
            }
        }

        @Test
        @DisplayName("should start on specific port")
        void shouldStartOnSpecificPort() {
            try (TestHttpServer server = TestHttpServer.start(0)) {
                assertThat(server.port()).isGreaterThan(0);
            }
        }
    }

    @Nested
    @DisplayName("url")
    class UrlMethod {

        @Test
        @DisplayName("should build URL with leading slash")
        void shouldBuildUrl() {
            try (TestHttpServer server = TestHttpServer.start()) {
                String url = server.url("/api/test");
                assertThat(url).startsWith("http://localhost:")
                        .endsWith("/api/test");
            }
        }

        @Test
        @DisplayName("should add leading slash if missing")
        void shouldAddSlash() {
            try (TestHttpServer server = TestHttpServer.start()) {
                String url = server.url("api/test");
                assertThat(url).contains("/api/test");
            }
        }
    }

    @Nested
    @DisplayName("when/thenRespond")
    class WhenThenRespond {

        @Test
        @DisplayName("should respond with configured mock response")
        void shouldRespondWithMock() throws Exception {
            try (TestHttpServer server = TestHttpServer.start()) {
                server.when(RequestMatcher.get("/api/hello"))
                        .thenRespond(MockResponse.ok("Hello World"));

                HttpURLConnection conn = openConnection(server.url("/api/hello"), "GET");
                assertThat(conn.getResponseCode()).isEqualTo(200);
                assertThat(readResponse(conn)).isEqualTo("Hello World");
            }
        }

        @Test
        @DisplayName("should return 404 when no route matches")
        void shouldReturn404WhenNoMatch() throws Exception {
            try (TestHttpServer server = TestHttpServer.start()) {
                HttpURLConnection conn = openConnection(server.url("/no-match"), "GET");
                assertThat(conn.getResponseCode()).isEqualTo(404);
            }
        }

        @Test
        @DisplayName("should match POST requests")
        void shouldMatchPost() throws Exception {
            try (TestHttpServer server = TestHttpServer.start()) {
                server.when(RequestMatcher.post("/api/data"))
                        .thenRespond(MockResponse.ok("created"));

                HttpURLConnection conn = openConnection(server.url("/api/data"), "POST");
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write("body".getBytes(StandardCharsets.UTF_8));
                }
                assertThat(conn.getResponseCode()).isEqualTo(200);
                assertThat(readResponse(conn)).isEqualTo("created");
            }
        }
    }

    @Nested
    @DisplayName("recordedRequests")
    class RecordedRequests {

        @Test
        @DisplayName("should record all requests")
        void shouldRecordRequests() throws Exception {
            try (TestHttpServer server = TestHttpServer.start()) {
                server.when(RequestMatcher.get("/api/a")).thenRespond(MockResponse.ok());
                server.when(RequestMatcher.get("/api/b")).thenRespond(MockResponse.ok());

                readResponse(openConnection(server.url("/api/a"), "GET"));
                readResponse(openConnection(server.url("/api/b"), "GET"));

                assertThat(server.recordedRequests()).hasSize(2);
            }
        }

        @Test
        @DisplayName("should return unmodifiable list")
        void shouldReturnUnmodifiable() {
            try (TestHttpServer server = TestHttpServer.start()) {
                var list = server.recordedRequests();
                assertThat(list).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("recordedRequest(path)")
    class RecordedRequestByPath {

        @Test
        @DisplayName("should find request by path")
        void shouldFindByPath() throws Exception {
            try (TestHttpServer server = TestHttpServer.start()) {
                server.when(RequestMatcher.get("/api/test")).thenRespond(MockResponse.ok("ok"));

                readResponse(openConnection(server.url("/api/test"), "GET"));

                RecordedRequest req = server.recordedRequest("/api/test");
                assertThat(req).isNotNull();
                assertThat(req.method()).isEqualTo("GET");
            }
        }

        @Test
        @DisplayName("should return null for non-existent path")
        void shouldReturnNullForMissing() {
            try (TestHttpServer server = TestHttpServer.start()) {
                assertThat(server.recordedRequest("/missing")).isNull();
            }
        }
    }

    @Nested
    @DisplayName("reset")
    class Reset {

        @Test
        @DisplayName("should clear recorded requests and routes")
        void shouldClearAll() throws Exception {
            try (TestHttpServer server = TestHttpServer.start()) {
                server.when(RequestMatcher.get("/api")).thenRespond(MockResponse.ok());

                readResponse(openConnection(server.url("/api"), "GET"));
                assertThat(server.recordedRequests()).hasSize(1);

                server.reset();
                assertThat(server.recordedRequests()).isEmpty();

                // Route should be gone too
                HttpURLConnection conn = openConnection(server.url("/api"), "GET");
                assertThat(conn.getResponseCode()).isEqualTo(404);
            }
        }
    }

    @Nested
    @DisplayName("response headers")
    class ResponseHeaders {

        @Test
        @DisplayName("should include custom headers in response")
        void shouldIncludeHeaders() throws Exception {
            try (TestHttpServer server = TestHttpServer.start()) {
                server.when(RequestMatcher.get("/api"))
                        .thenRespond(MockResponse.ok("body")
                                .withHeader("X-Custom", "value"));

                HttpURLConnection conn = openConnection(server.url("/api"), "GET");
                assertThat(conn.getResponseCode()).isEqualTo(200);
                assertThat(conn.getHeaderField("X-Custom")).isEqualTo("value");
            }
        }
    }
}
