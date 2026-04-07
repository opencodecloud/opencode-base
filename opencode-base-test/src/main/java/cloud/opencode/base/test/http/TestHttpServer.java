package cloud.opencode.base.test.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

/**
 * Test HTTP Server — Lightweight in-process HTTP server for unit testing.
 * 测试 HTTP 服务器 — 用于单元测试的轻量级进程内 HTTP 服务器。
 *
 * <p>Uses {@code jdk.httpserver} (included in JDK), no external dependencies.
 * Implements {@link AutoCloseable} for use in try-with-resources.</p>
 * <p>使用 {@code jdk.httpserver}（JDK 内置），无外部依赖。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>In-process HTTP server for testing - 用于测试的进程内HTTP服务器</li>
 *   <li>Request recording and route matching - 请求录制和路由匹配</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try (TestHttpServer server = TestHttpServer.start()) {
 *     server.when(RequestMatcher.get("/api")).thenRespond(MockResponse.ok());
 *     String url = server.url("/api");
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (concurrent data structures) - 线程安全: 是（并发数据结构）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class TestHttpServer implements AutoCloseable {

    private final HttpServer server;
    private final int port;
    private final List<RecordedRequest> recordedRequests = Collections.synchronizedList(new ArrayList<>());
    private final List<RouteEntry> routes = new CopyOnWriteArrayList<>();

    private TestHttpServer(int port) {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
            this.server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            this.server.createContext("/", this::handleRequest);
            this.server.start();
            this.port = this.server.getAddress().getPort();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to start TestHttpServer on port " + port, e);
        }
    }

    public static TestHttpServer start() { return new TestHttpServer(0); }
    public static TestHttpServer start(int port) { return new TestHttpServer(port); }

    public WhenBuilder when(RequestMatcher matcher) {
        return new WhenBuilder(matcher);
    }

    public final class WhenBuilder {
        private final RequestMatcher matcher;
        private WhenBuilder(RequestMatcher matcher) { this.matcher = matcher; }

        public TestHttpServer thenRespond(MockResponse response) {
            routes.add(0, new RouteEntry(matcher, response));
            return TestHttpServer.this;
        }
    }

    public String url(String path) {
        Objects.requireNonNull(path, "path must not be null");
        String p = path.startsWith("/") ? path : "/" + path;
        return "http://localhost:" + port + p;
    }

    public int port() { return port; }

    /**
     * Creates a request verification builder for asserting recorded requests.
     * 创建请求验证构建器，用于断言已录制的请求。
     *
     * <p><strong>Usage Examples | 使用示例:</strong></p>
     * <pre>{@code
     * server.verify().that(RequestMatcher.get("/api")).wasCalled(2);
     * server.verify().that(RequestMatcher.post("/api")).wasNeverCalled();
     * }</pre>
     *
     * @return a new request verification builder | 新的请求验证构建器
     * @since V1.0.3
     */
    public RequestVerification verify() {
        return new RequestVerification(recordedRequests);
    }

    public List<RecordedRequest> recordedRequests() {
        return Collections.unmodifiableList(new ArrayList<>(recordedRequests));
    }

    public RecordedRequest recordedRequest(String path) {
        List<RecordedRequest> all = new ArrayList<>(recordedRequests);
        for (int i = all.size() - 1; i >= 0; i--) {
            RecordedRequest r = all.get(i);
            String rPath = r.path().contains("?")
                    ? r.path().substring(0, r.path().indexOf('?'))
                    : r.path();
            if (rPath.equals(path)) return r;
        }
        return null;
    }

    public void reset() {
        recordedRequests.clear();
        routes.clear();
    }

    @Override
    public void close() { server.stop(0); }

    private void handleRequest(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().toString();
            Map<String, String> headers = new LinkedHashMap<>();
            exchange.getRequestHeaders().forEach((name, values) -> {
                if (!values.isEmpty()) headers.put(name.toLowerCase(), values.get(0));
            });
            byte[] body;
            try (InputStream is = exchange.getRequestBody()) {
                body = is.readAllBytes();
            }
            RecordedRequest recorded = new RecordedRequest(method, path, Map.copyOf(headers), body);
            recordedRequests.add(recorded);

            MockResponse response = findResponse(recorded);
            byte[] responseBody = response.body().getBytes(StandardCharsets.UTF_8);
            response.headers().forEach((name, value) ->
                    exchange.getResponseHeaders().set(name, value));
            exchange.sendResponseHeaders(response.statusCode(), responseBody.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBody);
            }
        } catch (IOException e) {
            System.err.println("TestHttpServer: error handling request: " + e.getMessage());
            try { exchange.sendResponseHeaders(500, 0); } catch (IOException ignored) {}
        } finally {
            exchange.close();
        }
    }

    private MockResponse findResponse(RecordedRequest request) {
        for (RouteEntry entry : routes) {
            if (entry.matcher().matches(request)) return entry.response();
        }
        return MockResponse.notFound("No route matched: " + request.method() + " " + request.path());
    }

    private record RouteEntry(RequestMatcher matcher, MockResponse response) {}
}
