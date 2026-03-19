package cloud.opencode.base.test.http;

/**
 * Request Matcher — predicate for matching incoming HTTP requests in {@link TestHttpServer}.
 * 请求匹配器 — 用于在 {@link TestHttpServer} 中匹配传入 HTTP 请求的谓词。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>HTTP request matching predicates - HTTP请求匹配谓词</li>
 *   <li>Method and path matching - 方法和路径匹配</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RequestMatcher matcher = RequestMatcher.get("/api/users");
 * server.when(matcher).thenRespond(MockResponse.ok());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@FunctionalInterface
public interface RequestMatcher {

    boolean matches(RecordedRequest request);

    static RequestMatcher get(String path) {
        return r -> "GET".equals(r.method()) && matchPath(r.path(), path);
    }

    static RequestMatcher post(String path) {
        return r -> "POST".equals(r.method()) && matchPath(r.path(), path);
    }

    static RequestMatcher put(String path) {
        return r -> "PUT".equals(r.method()) && matchPath(r.path(), path);
    }

    static RequestMatcher delete(String path) {
        return r -> "DELETE".equals(r.method()) && matchPath(r.path(), path);
    }

    static RequestMatcher any(String path) {
        return r -> matchPath(r.path(), path);
    }

    static RequestMatcher method(String method, String path) {
        return r -> method.equalsIgnoreCase(r.method()) && matchPath(r.path(), path);
    }

    private static boolean matchPath(String actualPath, String expectedPath) {
        if (actualPath == null) return expectedPath == null;
        String comparePath = actualPath.contains("?") && !expectedPath.contains("?")
                ? actualPath.substring(0, actualPath.indexOf('?'))
                : actualPath;
        return comparePath.equals(expectedPath);
    }
}
