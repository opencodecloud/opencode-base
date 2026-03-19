package cloud.opencode.base.classloader.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * InputStream Resource - Resource from input stream
 * 输入流资源 - 从输入流加载的资源
 *
 * <p>Represents a resource backed by an input stream. Note that the stream can only be read once.</p>
 * <p>表示由输入流支持的资源。注意：流只能读取一次。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Wrap existing input stream - 包装现有输入流</li>
 *   <li>Single-read resource - 单次读取资源</li>
 *   <li>Streaming support - 流式支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * InputStream is = new FileInputStream("config.yml");
 * Resource resource = new InputStreamResource(is);
 * Resource resource = new InputStreamResource(is, "config.yml");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (single use) - 线程安全: 否 (单次使用)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
public class InputStreamResource extends AbstractResource {

    private final InputStream inputStream;
    private final String description;
    private final AtomicBoolean read = new AtomicBoolean(false);

    /**
     * Create input stream resource
     * 创建输入流资源
     *
     * @param inputStream input stream | 输入流
     */
    public InputStreamResource(InputStream inputStream) {
        this(inputStream, "InputStreamResource");
    }

    /**
     * Create input stream resource with description
     * 创建带描述的输入流资源
     *
     * @param inputStream input stream | 输入流
     * @param description resource description | 资源描述
     */
    public InputStreamResource(InputStream inputStream, String description) {
        this.inputStream = Objects.requireNonNull(inputStream, "InputStream must not be null");
        this.description = description != null ? description : "InputStreamResource";
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean isReadable() {
        return !read.get();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (read.getAndSet(true)) {
            throw new IOException("InputStream has already been read - InputStreamResource is single-use only");
        }
        return inputStream;
    }

    @Override
    public URL getUrl() throws IOException {
        throw new IOException("InputStreamResource does not have a URL");
    }

    @Override
    public String getDescription() {
        return "stream:" + description;
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        throw new IOException("Cannot create relative resource from InputStreamResource");
    }

    /**
     * Check if the input stream has been read
     * 检查输入流是否已读取
     *
     * @return true if read | 已读取返回 true
     */
    public boolean isRead() {
        return read.get();
    }
}
