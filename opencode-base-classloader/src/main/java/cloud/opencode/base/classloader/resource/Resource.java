package cloud.opencode.base.classloader.resource;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Resource Interface - Unified resource access abstraction
 * 资源接口 - 统一的资源访问抽象
 *
 * <p>Abstract interface for resource access from different sources (classpath, file, URL, JAR).</p>
 * <p>用于从不同来源（classpath、文件、URL、JAR）访问资源的抽象接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Existence check - 存在性检查</li>
 *   <li>Content reading - 内容读取</li>
 *   <li>Metadata access - 元数据访问</li>
 *   <li>Relative resource creation - 相对资源创建</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Resource resource = new ClassPathResource("config.yml");
 * if (resource.exists()) {
 *     String content = resource.getString();
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
public interface Resource {

    // ==================== Existence & Accessibility | 存在性与可访问性 ====================

    /**
     * Check if resource exists
     * 检查资源是否存在
     *
     * @return true if exists | 存在返回 true
     */
    boolean exists();

    /**
     * Check if resource is readable
     * 检查资源是否可读
     *
     * @return true if readable | 可读返回 true
     */
    boolean isReadable();

    /**
     * Check if resource is a file
     * 检查资源是否为文件
     *
     * @return true if is file | 是文件返回 true
     */
    boolean isFile();

    // ==================== Content Access | 内容访问 ====================

    /**
     * Get input stream for resource
     * 获取资源的输入流
     *
     * @return input stream | 输入流
     * @throws IOException if cannot open stream | 无法打开流时抛出
     */
    InputStream getInputStream() throws IOException;

    /**
     * Get resource URL
     * 获取资源 URL
     *
     * @return resource URL | 资源 URL
     * @throws IOException if cannot get URL | 无法获取 URL 时抛出
     */
    URL getUrl() throws IOException;

    /**
     * Get resource URI
     * 获取资源 URI
     *
     * @return resource URI | 资源 URI
     * @throws IOException if cannot get URI | 无法获取 URI 时抛出
     */
    URI getUri() throws IOException;

    /**
     * Get file if resource is file-based
     * 获取文件（如果是文件资源）
     *
     * @return optional file | 可选的文件
     */
    Optional<File> getFile();

    /**
     * Get path if resource is file-based
     * 获取路径（如果是文件资源）
     *
     * @return optional path | 可选的路径
     */
    Optional<Path> getPath();

    // ==================== Metadata | 元数据 ====================

    /**
     * Get content length in bytes
     * 获取内容长度（字节）
     *
     * @return content length or -1 if unknown | 内容长度，未知时返回 -1
     * @throws IOException if cannot determine length | 无法确定长度时抛出
     */
    long contentLength() throws IOException;

    /**
     * Get last modified timestamp
     * 获取最后修改时间戳
     *
     * @return last modified time in milliseconds or 0 if unknown | 最后修改时间（毫秒），未知时返回 0
     * @throws IOException if cannot determine time | 无法确定时间时抛出
     */
    long lastModified() throws IOException;

    /**
     * Get filename
     * 获取文件名
     *
     * @return filename or null | 文件名或 null
     */
    String getFilename();

    /**
     * Get resource description
     * 获取资源描述
     *
     * @return description | 描述
     */
    String getDescription();

    // ==================== Convenience Methods | 便捷方法 ====================

    /**
     * Read resource as byte array
     * 读取资源为字节数组
     *
     * @return byte array | 字节数组
     * @throws IOException if read fails | 读取失败时抛出
     */
    default byte[] getBytes() throws IOException {
        try (InputStream is = getInputStream()) {
            return is.readAllBytes();
        }
    }

    /**
     * Read resource as string (UTF-8)
     * 读取资源为字符串（UTF-8）
     *
     * @return content as string | 字符串内容
     * @throws IOException if read fails | 读取失败时抛出
     */
    default String getString() throws IOException {
        return getString(StandardCharsets.UTF_8);
    }

    /**
     * Read resource as string with specified charset
     * 使用指定字符集读取资源为字符串
     *
     * @param charset character set | 字符集
     * @return content as string | 字符串内容
     * @throws IOException if read fails | 读取失败时抛出
     */
    default String getString(Charset charset) throws IOException {
        return new String(getBytes(), charset);
    }

    /**
     * Read all lines (UTF-8)
     * 读取所有行（UTF-8）
     *
     * @return list of lines | 行列表
     * @throws IOException if read fails | 读取失败时抛出
     */
    default List<String> readLines() throws IOException {
        return readLines(StandardCharsets.UTF_8);
    }

    /**
     * Read all lines with specified charset
     * 使用指定字符集读取所有行
     *
     * @param charset character set | 字符集
     * @return list of lines | 行列表
     * @throws IOException if read fails | 读取失败时抛出
     */
    default List<String> readLines(Charset charset) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getInputStream(), charset))) {
            return reader.lines().toList();
        }
    }

    /**
     * Create relative resource
     * 创建相对资源
     *
     * @param relativePath relative path | 相对路径
     * @return relative resource | 相对资源
     * @throws IOException if cannot create relative resource | 无法创建相对资源时抛出
     */
    Resource createRelative(String relativePath) throws IOException;
}
