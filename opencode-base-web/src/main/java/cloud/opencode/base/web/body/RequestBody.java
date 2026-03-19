package cloud.opencode.base.web.body;

import cloud.opencode.base.web.http.ContentType;

import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Request Body - HTTP Request Body Interface
 * 请求体 - HTTP 请求体接口
 *
 * <p>Defines the contract for HTTP request bodies with content type and body publisher support.</p>
 * <p>定义 HTTP 请求体的契约，支持内容类型和 body publisher。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Content type abstraction - 内容类型抽象</li>
 *   <li>JDK HttpClient BodyPublisher integration - JDK HttpClient BodyPublisher 集成</li>
 *   <li>Factory methods for common body types - 常见请求体类型的工厂方法</li>
 *   <li>Support for JSON, XML, text, and file bodies - 支持 JSON、XML、文本和文件请求体</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // JSON body
 * RequestBody body = RequestBody.json("{\"key\":\"value\"}");
 *
 * // Text body
 * RequestBody body = RequestBody.text("Hello World");
 *
 * // File body
 * RequestBody body = RequestBody.file(Path.of("data.csv"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (implementations should be immutable) - 线程安全: 是（实现应为不可变）</li>
 *   <li>Null-safe: No (content must not be null) - 空值安全: 否（内容不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public interface RequestBody {

    String getContentType();

    BodyPublisher getBodyPublisher();

    default long getContentLength() {
        return -1;
    }

    static RequestBody empty() {
        return new SimpleBody(null, BodyPublishers.noBody());
    }

    static RequestBody of(String content, String contentType) {
        return of(content, contentType, StandardCharsets.UTF_8);
    }

    static RequestBody of(String content, String contentType, Charset charset) {
        byte[] bytes = content.getBytes(charset);
        return new SimpleBody(contentType, BodyPublishers.ofByteArray(bytes), bytes.length);
    }

    static RequestBody of(byte[] bytes, String contentType) {
        return new SimpleBody(contentType, BodyPublishers.ofByteArray(bytes), bytes.length);
    }

    static RequestBody json(String json) {
        return of(json, ContentType.APPLICATION_JSON + "; charset=utf-8");
    }

    static RequestBody text(String text) {
        return of(text, ContentType.TEXT_PLAIN + "; charset=utf-8");
    }

    static RequestBody xml(String xml) {
        return of(xml, ContentType.APPLICATION_XML + "; charset=utf-8");
    }

    static RequestBody file(Path file) {
        return FileBody.of(file);
    }

    static RequestBody file(Path file, String contentType) {
        return FileBody.of(file, contentType);
    }

    record SimpleBody(String contentType, BodyPublisher publisher, long length) implements RequestBody {
        public SimpleBody(String contentType, BodyPublisher publisher) {
            this(contentType, publisher, -1);
        }

        @Override
        public String getContentType() { return contentType; }

        @Override
        public BodyPublisher getBodyPublisher() { return publisher; }

        @Override
        public long getContentLength() { return length; }
    }
}
