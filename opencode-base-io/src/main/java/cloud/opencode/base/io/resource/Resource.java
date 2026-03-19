package cloud.opencode.base.io.resource;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Resource Abstraction Interface
 * 资源抽象接口
 *
 * <p>Unified interface for accessing resources from different sources
 * including classpath, filesystem, and URLs.</p>
 * <p>统一接口用于访问来自不同来源的资源，包括类路径、文件系统和URL。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unified resource access - 统一资源访问</li>
 *   <li>Existence and readability checks - 存在性和可读性检查</li>
 *   <li>Content reading methods - 内容读取方法</li>
 *   <li>Relative resource creation - 相对资源创建</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Resource resource = OpenResource.classPathResource("config.properties");
 * if (resource.exists()) {
 *     String content = resource.readString(StandardCharsets.UTF_8);
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Depends on implementation - 空值安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public interface Resource {

    /**
     * Checks if the resource exists
     * 检查资源是否存在
     *
     * @return true if exists | 如果存在返回true
     */
    boolean exists();

    /**
     * Checks if the resource is readable
     * 检查资源是否可读
     *
     * @return true if readable | 如果可读返回true
     */
    boolean isReadable();

    /**
     * Gets the input stream for reading the resource
     * 获取用于读取资源的输入流
     *
     * @return input stream | 输入流
     * @throws OpenIOOperationException if the stream cannot be opened | 如果流无法打开
     */
    InputStream getInputStream();

    /**
     * Gets the URL of the resource
     * 获取资源的URL
     *
     * @return URL | URL
     * @throws OpenIOOperationException if URL is not available | 如果URL不可用
     */
    URL getURL();

    /**
     * Gets the file path if available
     * 获取文件路径（如果可用）
     *
     * @return path or null if not supported | 路径，如果不支持则返回null
     */
    Path getPath();

    /**
     * Gets a description of the resource
     * 获取资源描述
     *
     * @return description | 描述
     */
    String getDescription();

    /**
     * Gets the filename of the resource
     * 获取资源的文件名
     *
     * @return filename | 文件名
     */
    String getFilename();

    /**
     * Gets the content length in bytes
     * 获取内容长度（字节）
     *
     * @return length or -1 if unknown | 长度，如果未知返回-1
     */
    long contentLength();

    /**
     * Gets the last modified time
     * 获取最后修改时间
     *
     * @return timestamp or -1 if unknown | 时间戳，如果未知返回-1
     */
    long lastModified();

    /**
     * Creates a relative resource
     * 创建相对资源
     *
     * @param relativePath the relative path | 相对路径
     * @return new resource | 新资源
     * @throws OpenIOOperationException if creation fails | 如果创建失败
     */
    Resource createRelative(String relativePath);

    /**
     * Reads the resource as byte array
     * 读取资源为字节数组
     *
     * @return byte array | 字节数组
     * @throws OpenIOOperationException if reading fails | 如果读取失败
     */
    default byte[] readBytes() {
        try (InputStream is = getInputStream()) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new OpenIOOperationException("Failed to read resource bytes: " + getDescription(), e);
        }
    }

    /**
     * Reads the resource as string with specified charset
     * 使用指定字符集读取资源为字符串
     *
     * @param charset the charset | 字符集
     * @return content string | 内容字符串
     * @throws OpenIOOperationException if reading fails | 如果读取失败
     */
    default String readString(Charset charset) {
        return new String(readBytes(), charset);
    }

    /**
     * Reads the resource as string with UTF-8
     * 使用UTF-8读取资源为字符串
     *
     * @return content string | 内容字符串
     * @throws OpenIOOperationException if reading fails | 如果读取失败
     */
    default String readString() {
        return readString(StandardCharsets.UTF_8);
    }
}
