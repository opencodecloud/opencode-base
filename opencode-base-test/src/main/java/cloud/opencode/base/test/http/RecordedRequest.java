package cloud.opencode.base.test.http;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Recorded HTTP Request — a snapshot of an HTTP request received by {@link TestHttpServer}.
 * 记录的 HTTP 请求 — 由 {@link TestHttpServer} 接收的 HTTP 请求快照。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>HTTP request snapshot capture - HTTP请求快照捕获</li>
 *   <li>Header and body access - 请求头和请求体访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RecordedRequest req = server.recordedRequest("/api/users");
 * String body = req.bodyAsString();
 * String auth = req.header("Authorization");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 *
 * @param method  the HTTP method - HTTP 方法
 * @param path    the request path including query string - 包含查询字符串的请求路径
 * @param headers the request headers (lowercase keys) - 请求头（键为小写）
 * @param body    the request body bytes - 请求体字节
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public record RecordedRequest(
        String method,
        String path,
        Map<String, String> headers,
        byte[] body) {

    public String bodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    public String header(String name) {
        return headers.get(name.toLowerCase());
    }
}
