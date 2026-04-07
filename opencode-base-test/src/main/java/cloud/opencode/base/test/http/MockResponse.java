package cloud.opencode.base.test.http;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mock HTTP Response — a response to be returned by {@link TestHttpServer} for a matched route.
 * 模拟 HTTP 响应 — {@link TestHttpServer} 为匹配路由返回的响应。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable HTTP response definition - 不可变HTTP响应定义</li>
 *   <li>Builder-style response construction - 构建器风格的响应构造</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MockResponse response = MockResponse.ok("{\"status\": \"ok\"}")
 *     .withHeader("Content-Type", "application/json");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class MockResponse {

    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;

    private MockResponse(int statusCode, String body, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.body = body != null ? body : "";
        this.headers = new LinkedHashMap<>(headers);
    }

    public static MockResponse ok(String body) { return new MockResponse(200, body, Map.of()); }
    public static MockResponse ok() { return new MockResponse(200, "", Map.of()); }
    public static MockResponse notFound() { return new MockResponse(404, "Not Found", Map.of()); }
    public static MockResponse notFound(String body) { return new MockResponse(404, body, Map.of()); }
    public static MockResponse serverError() { return new MockResponse(500, "Internal Server Error", Map.of()); }
    public static MockResponse serverError(String body) { return new MockResponse(500, body, Map.of()); }
    public static MockResponse withStatus(int statusCode) { return new MockResponse(statusCode, "", Map.of()); }
    public static MockResponse withStatus(int statusCode, String body) { return new MockResponse(statusCode, body, Map.of()); }

    public MockResponse withHeader(String name, String value) {
        java.util.Objects.requireNonNull(name, "Header name must not be null");
        if (name.chars().anyMatch(c -> c == '\r' || c == '\n')) {
            throw new IllegalArgumentException("Header name must not contain CR or LF");
        }
        if (value != null && value.chars().anyMatch(c -> c == '\r' || c == '\n')) {
            throw new IllegalArgumentException("Header value must not contain CR or LF");
        }
        Map<String, String> newHeaders = new LinkedHashMap<>(this.headers);
        newHeaders.put(name, value);
        return new MockResponse(this.statusCode, this.body, newHeaders);
    }

    public MockResponse withBody(String body) {
        return new MockResponse(this.statusCode, body, this.headers);
    }

    public int statusCode() { return statusCode; }
    public String body() { return body; }
    public Map<String, String> headers() { return Map.copyOf(headers); }

    @Override
    public String toString() {
        return "MockResponse{statusCode=" + statusCode + ", body=" + body + "}";
    }
}
