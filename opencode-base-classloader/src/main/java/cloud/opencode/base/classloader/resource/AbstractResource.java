package cloud.opencode.base.classloader.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Abstract Resource - Base implementation for Resource interface
 * 抽象资源 - Resource 接口的基础实现
 *
 * <p>Provides common default implementations for Resource interface methods.</p>
 * <p>为 Resource 接口方法提供通用的默认实现。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Base implementation for resource abstraction - 资源抽象的基础实现</li>
 *   <li>Common resource operations (exists, size, last modified) - 通用资源操作（存在性、大小、最后修改时间）</li>
 *   <li>Template method pattern for concrete implementations - 模板方法模式用于具体实现</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Extend AbstractResource for custom resource types
 * // 扩展AbstractResource以实现自定义资源类型
 * public class MyResource extends AbstractResource {
 *     @Override
 *     public InputStream getInputStream() {
 *         return new ByteArrayInputStream(data);
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
public abstract class AbstractResource implements Resource {

    @Override
    public boolean exists() {
        try (InputStream is = getInputStream()) {
            return is != null;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean isReadable() {
        return exists();
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public URI getUri() throws IOException {
        URL url = getUrl();
        try {
            return url.toURI();
        } catch (Exception e) {
            throw new IOException("Cannot convert URL to URI: " + url, e);
        }
    }

    @Override
    public Optional<File> getFile() {
        return Optional.empty();
    }

    @Override
    public Optional<Path> getPath() {
        return Optional.empty();
    }

    @Override
    public long contentLength() throws IOException {
        return getBytes().length;
    }

    @Override
    public long lastModified() throws IOException {
        return 0;
    }

    @Override
    public String getFilename() {
        return null;
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Resource other)) return false;
        return getDescription().equals(other.getDescription());
    }

    @Override
    public int hashCode() {
        return getDescription().hashCode();
    }
}
