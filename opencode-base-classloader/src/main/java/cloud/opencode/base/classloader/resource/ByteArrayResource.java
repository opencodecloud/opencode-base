package cloud.opencode.base.classloader.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

/**
 * ByteArray Resource - Resource from byte array
 * 字节数组资源 - 从字节数组加载的资源
 *
 * <p>Represents a resource backed by a byte array in memory.</p>
 * <p>表示由内存中字节数组支持的资源。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>In-memory resource - 内存资源</li>
 *   <li>No I/O operations - 无 I/O 操作</li>
 *   <li>Reusable content - 可重复使用的内容</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * byte[] data = "config content".getBytes();
 * Resource resource = new ByteArrayResource(data);
 * Resource resource = new ByteArrayResource(data, "config.yml");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
public class ByteArrayResource extends AbstractResource {

    private final byte[] byteArray;
    private final String description;

    /**
     * Create byte array resource
     * 创建字节数组资源
     *
     * @param byteArray byte array content | 字节数组内容
     */
    public ByteArrayResource(byte[] byteArray) {
        this(byteArray, "ByteArrayResource");
    }

    /**
     * Create byte array resource with description
     * 创建带描述的字节数组资源
     *
     * @param byteArray   byte array content | 字节数组内容
     * @param description resource description | 资源描述
     */
    public ByteArrayResource(byte[] byteArray, String description) {
        Objects.requireNonNull(byteArray, "Byte array must not be null");
        this.byteArray = byteArray.clone();
        this.description = description != null ? description : "ByteArrayResource";
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(byteArray);
    }

    @Override
    public URL getUrl() throws IOException {
        throw new IOException("ByteArrayResource does not have a URL");
    }

    @Override
    public long contentLength() throws IOException {
        return byteArray.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return byteArray.clone();
    }

    @Override
    public String getDescription() {
        return "bytes:" + description + " [" + byteArray.length + " bytes]";
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        throw new IOException("Cannot create relative resource from ByteArrayResource");
    }

    /**
     * Get the byte array content
     * 获取字节数组内容
     *
     * @return copy of byte array | 字节数组副本
     */
    public byte[] getByteArray() {
        return byteArray.clone();
    }
}
