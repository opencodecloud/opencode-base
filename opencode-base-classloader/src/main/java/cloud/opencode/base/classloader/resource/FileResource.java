package cloud.opencode.base.classloader.resource;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * File Resource - Resource from file system
 * 文件资源 - 从文件系统加载的资源
 *
 * <p>Represents a resource that exists on the file system.</p>
 * <p>表示存在于文件系统上的资源。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Load resources from file system - 从文件系统加载资源</li>
 *   <li>Support File and Path - 支持 File 和 Path</li>
 *   <li>File metadata access - 文件元数据访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Resource resource = new FileResource("/etc/config.yml");
 * Resource resource = new FileResource(new File("/etc/config.yml"));
 * Resource resource = new FileResource(Path.of("/etc/config.yml"));
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
public class FileResource extends AbstractResource {

    private final Path path;

    /**
     * Create file resource from path string
     * 从路径字符串创建文件资源
     *
     * @param path file path string | 文件路径字符串
     */
    public FileResource(String path) {
        this(Path.of(Objects.requireNonNull(path, "Path must not be null")));
    }

    /**
     * Create file resource from File
     * 从 File 创建文件资源
     *
     * @param file file object | 文件对象
     */
    public FileResource(File file) {
        this(Objects.requireNonNull(file, "File must not be null").toPath());
    }

    /**
     * Create file resource from Path
     * 从 Path 创建文件资源
     *
     * @param path path object | 路径对象
     */
    public FileResource(Path path) {
        this.path = Objects.requireNonNull(path, "Path must not be null").toAbsolutePath().normalize();
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
    public boolean isFile() {
        return Files.isRegularFile(path);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (!exists()) {
            throw new OpenClassLoaderException("File not found: " + path);
        }
        return Files.newInputStream(path);
    }

    @Override
    public URL getUrl() throws IOException {
        return path.toUri().toURL();
    }

    @Override
    public URI getUri() throws IOException {
        return path.toUri();
    }

    @Override
    public Optional<File> getFile() {
        return Optional.of(path.toFile());
    }

    @Override
    public Optional<Path> getPath() {
        return Optional.of(path);
    }

    @Override
    public long contentLength() throws IOException {
        return Files.size(path);
    }

    @Override
    public long lastModified() throws IOException {
        return Files.getLastModifiedTime(path).toMillis();
    }

    @Override
    public String getFilename() {
        Path fileName = path.getFileName();
        return fileName != null ? fileName.toString() : null;
    }

    @Override
    public String getDescription() {
        return "file:" + path;
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        Path parent = path.getParent();
        if (parent == null) {
            return new FileResource(relativePath);
        }
        return new FileResource(parent.resolve(relativePath));
    }

    /**
     * Get the file path
     * 获取文件路径
     *
     * @return file path | 文件路径
     */
    public Path getFilePath() {
        return path;
    }
}
