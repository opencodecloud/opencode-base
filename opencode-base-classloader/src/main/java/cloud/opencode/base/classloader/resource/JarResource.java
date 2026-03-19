package cloud.opencode.base.classloader.resource;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * JAR Resource - Resource from JAR file
 * JAR 资源 - 从 JAR 文件加载的资源
 *
 * <p>Represents a resource that exists inside a JAR file.</p>
 * <p>表示存在于 JAR 文件内部的资源。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Load resources from JAR files - 从 JAR 文件加载资源</li>
 *   <li>Access JAR entry metadata - 访问 JAR 条目元数据</li>
 *   <li>Support nested JAR paths - 支持嵌套 JAR 路径</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Resource resource = new JarResource(Path.of("lib/app.jar"), "config/app.yml");
 * String content = resource.getString();
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
public class JarResource extends AbstractResource {

    private final Path jarPath;
    private final String entryPath;
    private final URL jarUrl;

    /**
     * Create JAR resource from JAR path and entry path
     * 从 JAR 路径和条目路径创建 JAR 资源
     *
     * @param jarPath   path to JAR file | JAR 文件路径
     * @param entryPath path within JAR | JAR 内路径
     */
    public JarResource(Path jarPath, String entryPath) {
        this.jarPath = Objects.requireNonNull(jarPath, "JAR path must not be null").toAbsolutePath();
        this.entryPath = cleanEntryPath(Objects.requireNonNull(entryPath, "Entry path must not be null"));
        try {
            this.jarUrl = new URL("jar:" + this.jarPath.toUri() + "!/" + this.entryPath);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid JAR URL", e);
        }
    }

    /**
     * Create JAR resource from JAR URL and entry path
     * 从 JAR URL 和条目路径创建 JAR 资源
     *
     * @param jarUrl    URL to JAR file | JAR 文件 URL
     * @param entryPath path within JAR | JAR 内路径
     */
    public JarResource(URL jarUrl, String entryPath) {
        Objects.requireNonNull(jarUrl, "JAR URL must not be null");
        this.entryPath = cleanEntryPath(Objects.requireNonNull(entryPath, "Entry path must not be null"));
        try {
            String urlString = jarUrl.toString();
            if (!urlString.startsWith("jar:")) {
                urlString = "jar:" + urlString;
            }
            if (!urlString.contains("!/")) {
                urlString = urlString + "!/" + this.entryPath;
            }
            this.jarUrl = new URL(urlString);
            // Extract jar path from URL
            String jarUrlStr = jarUrl.toString();
            if (jarUrlStr.startsWith("file:")) {
                this.jarPath = Path.of(URI.create(jarUrlStr));
            } else {
                this.jarPath = null;
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid JAR URL", e);
        }
    }

    private String cleanEntryPath(String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    @Override
    public boolean exists() {
        if (jarPath == null || !jarPath.toFile().exists()) {
            return false;
        }
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            return jarFile.getEntry(entryPath) != null;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (jarPath == null || !jarPath.toFile().exists()) {
            throw new OpenClassLoaderException("JAR file not found: " + jarPath);
        }
        JarFile jarFile = new JarFile(jarPath.toFile());
        try {
            JarEntry entry = jarFile.getJarEntry(entryPath);
            if (entry == null) {
                jarFile.close();
                throw new OpenClassLoaderException("Entry not found in JAR: " + entryPath);
            }
            InputStream entryStream = jarFile.getInputStream(entry);
            return new JarEntryInputStream(jarFile, entryStream);
        } catch (IOException | RuntimeException e) {
            // Close JarFile if any exception occurs during stream creation
            jarFile.close();
            throw e;
        }
    }

    @Override
    public URL getUrl() throws IOException {
        return jarUrl;
    }

    @Override
    public long contentLength() throws IOException {
        if (jarPath == null) {
            return -1;
        }
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            JarEntry entry = jarFile.getJarEntry(entryPath);
            return entry != null ? entry.getSize() : -1;
        }
    }

    @Override
    public long lastModified() throws IOException {
        if (jarPath == null) {
            return 0;
        }
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            JarEntry entry = jarFile.getJarEntry(entryPath);
            return entry != null ? entry.getTime() : 0;
        }
    }

    @Override
    public String getFilename() {
        int lastSlash = entryPath.lastIndexOf('/');
        return lastSlash != -1 ? entryPath.substring(lastSlash + 1) : entryPath;
    }

    @Override
    public String getDescription() {
        return "jar:" + (jarPath != null ? jarPath : "unknown") + "!/" + entryPath;
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        String pathToUse;
        int lastSlash = entryPath.lastIndexOf('/');
        if (lastSlash != -1) {
            pathToUse = entryPath.substring(0, lastSlash + 1) + relativePath;
        } else {
            pathToUse = relativePath;
        }
        if (jarPath != null) {
            return new JarResource(jarPath, pathToUse);
        }
        throw new IOException("Cannot create relative resource without JAR path");
    }

    /**
     * Get the JAR file path
     * 获取 JAR 文件路径
     *
     * @return JAR file path or null | JAR 文件路径或 null
     */
    public Path getJarPath() {
        return jarPath;
    }

    /**
     * Get the entry path within JAR
     * 获取 JAR 内的条目路径
     *
     * @return entry path | 条目路径
     */
    public String getEntryPath() {
        return entryPath;
    }

    /**
     * Input stream wrapper that closes the JAR file when the stream is closed
     */
    private static class JarEntryInputStream extends InputStream {
        private final JarFile jarFile;
        private final InputStream delegate;

        JarEntryInputStream(JarFile jarFile, InputStream delegate) {
            this.jarFile = jarFile;
            this.delegate = delegate;
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return delegate.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }

        @Override
        public void close() throws IOException {
            try {
                delegate.close();
            } finally {
                jarFile.close();
            }
        }
    }
}
