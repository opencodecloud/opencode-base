package cloud.opencode.base.io.resource;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * File System Resource Implementation
 * 文件系统资源实现
 *
 * <p>Resource implementation for resources from the local file system.
 * Wraps java.nio.file.Path for file access.</p>
 * <p>用于从本地文件系统加载资源的资源实现。
 * 封装java.nio.file.Path进行文件访问。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Local file access - 本地文件访问</li>
 *   <li>NIO.2 Path based - 基于NIO.2 Path</li>
 *   <li>Immutable and thread-safe - 不可变且线程安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FileSystemResource resource = new FileSystemResource(Path.of("/etc/config.yaml"));
 * String content = resource.readString();
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public final class FileSystemResource implements Resource {

    private final Path path;

    /**
     * Creates a file system resource from path
     * 从路径创建文件系统资源
     *
     * @param path the file path | 文件路径
     */
    public FileSystemResource(Path path) {
        Objects.requireNonNull(path, "Path must not be null");
        this.path = path.toAbsolutePath().normalize();
    }

    /**
     * Creates a file system resource from string path
     * 从字符串路径创建文件系统资源
     *
     * @param path the file path string | 文件路径字符串
     */
    public FileSystemResource(String path) {
        this(Path.of(path));
    }

    @Override
    public boolean exists() {
        return Files.exists(path);
    }

    @Override
    public boolean isReadable() {
        return Files.isReadable(path);
    }

    @Override
    public InputStream getInputStream() {
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw OpenIOOperationException.readFailed(path, e);
        }
    }

    @Override
    public URL getURL() {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new OpenIOOperationException("Failed to convert path to URL: " + path, e);
        }
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public String getDescription() {
        return "file:" + path;
    }

    @Override
    public String getFilename() {
        Path fileName = path.getFileName();
        return fileName != null ? fileName.toString() : "";
    }

    @Override
    public long contentLength() {
        try {
            return Files.size(path);
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public long lastModified() {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public Resource createRelative(String relativePath) {
        Path parent = path.getParent();
        if (parent == null) {
            return new FileSystemResource(Path.of(relativePath));
        }
        return new FileSystemResource(parent.resolve(relativePath));
    }

    /**
     * Gets the file path
     * 获取文件路径
     *
     * @return path | 路径
     */
    public Path getFilePath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileSystemResource that)) return false;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
