package cloud.opencode.base.classloader.resource;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Nested JAR Resource - Represents a single resource entry inside a nested JAR
 * 嵌套 JAR 资源 - 表示嵌套 JAR 中的单个资源条目
 *
 * <p>Provides access to resources that reside within a JAR that is itself embedded
 * inside another (outer) JAR, such as those found in Spring Boot fat JARs.</p>
 * <p>提供对嵌套在外层 JAR 内部的 JAR 中资源的访问，例如 Spring Boot fat JAR 中的资源。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Access resources in nested JARs - 访问嵌套 JAR 中的资源</li>
 *   <li>Lazy extraction via NestedJarHandler - 通过 NestedJarHandler 延迟解压</li>
 *   <li>Standard Resource interface compliance - 符合标准 Resource 接口</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * NestedJarHandler handler = NestedJarHandler.builder().build();
 * NestedJarResource res = new NestedJarResource(
 *     handler, outerJar, "BOOT-INF/lib/inner.jar", "com/example/App.class");
 * if (res.exists()) {
 *     byte[] bytes = res.getBytes();
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public class NestedJarResource extends AbstractResource {

    private final NestedJarHandler handler;
    private final Path outerJarPath;
    private final String nestedJarEntry;
    private final String resourceEntry;

    /**
     * Create a nested JAR resource
     * 创建嵌套 JAR 资源
     *
     * @param handler        the handler used to extract nested JARs | 用于解压嵌套 JAR 的处理器
     * @param outerJarPath   path to the outer JAR file | 外层 JAR 文件路径
     * @param nestedJarEntry entry name of the nested JAR inside the outer JAR | 内层 JAR 在外层 JAR 中的条目名
     * @param resourceEntry  entry name of the resource inside the nested JAR | 资源在内层 JAR 中的条目名
     */
    public NestedJarResource(NestedJarHandler handler,
                             Path outerJarPath,
                             String nestedJarEntry,
                             String resourceEntry) {
        this.handler = Objects.requireNonNull(handler, "handler must not be null");
        this.outerJarPath = Objects.requireNonNull(outerJarPath, "outerJarPath must not be null").toAbsolutePath();
        this.nestedJarEntry = cleanPath(Objects.requireNonNull(nestedJarEntry, "nestedJarEntry must not be null"));
        this.resourceEntry = cleanPath(Objects.requireNonNull(resourceEntry, "resourceEntry must not be null"));
    }

    // ==================== Resource interface ====================

    @Override
    public boolean exists() {
        // Check directly in the outer JAR without extracting to avoid
        // defeating the reference-counting mechanism
        try (JarFile outerJar = new JarFile(outerJarPath.toFile())) {
            var nestedEntry = outerJar.getEntry(nestedJarEntry);
            if (nestedEntry == null) {
                return false;
            }
            // Read the nested JAR from the outer JAR stream and check for the resource
            try (InputStream nis = outerJar.getInputStream(nestedEntry);
                 java.util.jar.JarInputStream jis = new java.util.jar.JarInputStream(nis)) {
                java.util.jar.JarEntry entry;
                while ((entry = jis.getNextJarEntry()) != null) {
                    if (entry.getName().equals(resourceEntry)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        Path extracted = handler.extractNestedJar(outerJarPath, nestedJarEntry);
        JarFile jarFile = new JarFile(extracted.toFile());
        try {
            JarEntry entry = jarFile.getJarEntry(resourceEntry);
            if (entry == null) {
                jarFile.close();
                handler.release(outerJarPath, nestedJarEntry);
                throw new OpenClassLoaderException(
                        "Resource entry not found in nested JAR: " + resourceEntry);
            }
            InputStream delegate = jarFile.getInputStream(entry);
            // Return a wrapper that closes the JarFile and releases the handler reference on close
            return new NestedJarInputStream(jarFile, delegate, handler, outerJarPath, nestedJarEntry);
        } catch (IOException | RuntimeException e) {
            jarFile.close();
            handler.release(outerJarPath, nestedJarEntry);
            throw e;
        }
    }

    @Override
    public URL getUrl() throws IOException {
        try {
            return new URL("jar:" + outerJarPath.toUri() + "!/" + nestedJarEntry + "!/" + resourceEntry);
        } catch (MalformedURLException e) {
            throw new IOException("Cannot construct URL for nested JAR resource", e);
        }
    }

    @Override
    public String getFilename() {
        int lastSlash = resourceEntry.lastIndexOf('/');
        return lastSlash != -1 ? resourceEntry.substring(lastSlash + 1) : resourceEntry;
    }

    @Override
    public String getDescription() {
        return "nested jar resource [" + outerJarPath.getFileName()
                + "!/" + nestedJarEntry
                + "!/" + resourceEntry + "]";
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        String base;
        int lastSlash = resourceEntry.lastIndexOf('/');
        if (lastSlash != -1) {
            base = resourceEntry.substring(0, lastSlash + 1) + relativePath;
        } else {
            base = relativePath;
        }
        // Normalize and reject path traversal attempts
        String normalized = java.nio.file.Path.of(base).normalize().toString();
        if (normalized.startsWith("..") || normalized.startsWith("/")) {
            throw new IOException("Relative path escapes nested JAR root: " + relativePath);
        }
        return new NestedJarResource(handler, outerJarPath, nestedJarEntry, normalized);
    }

    // ==================== Accessors ====================

    /**
     * Get the outer JAR path
     * 获取外层 JAR 路径
     *
     * @return outer JAR path | 外层 JAR 路径
     */
    public Path getOuterJarPath() {
        return outerJarPath;
    }

    /**
     * Get the nested JAR entry name
     * 获取嵌套 JAR 条目名
     *
     * @return nested JAR entry name | 嵌套 JAR 条目名
     */
    public String getNestedJarEntry() {
        return nestedJarEntry;
    }

    /**
     * Get the resource entry name inside the nested JAR
     * 获取嵌套 JAR 中的资源条目名
     *
     * @return resource entry name | 资源条目名
     */
    public String getResourceEntry() {
        return resourceEntry;
    }

    // ==================== Internal ====================

    private static String cleanPath(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }

    /**
     * Input stream wrapper that closes the JarFile and releases the handler reference on close.
     */
    private static final class NestedJarInputStream extends InputStream {
        private final JarFile jarFile;
        private final InputStream delegate;
        private final NestedJarHandler handler;
        private final Path outerJar;
        private final String nestedEntry;

        NestedJarInputStream(JarFile jarFile, InputStream delegate,
                             NestedJarHandler handler, Path outerJar, String nestedEntry) {
            this.jarFile = jarFile;
            this.delegate = delegate;
            this.handler = handler;
            this.outerJar = outerJar;
            this.nestedEntry = nestedEntry;
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
        public int available() throws IOException {
            return delegate.available();
        }

        @Override
        public long skip(long n) throws IOException {
            return delegate.skip(n);
        }

        @Override
        public void close() throws IOException {
            try {
                delegate.close();
            } finally {
                try {
                    jarFile.close();
                } finally {
                    handler.release(outerJar, nestedEntry);
                }
            }
        }
    }
}
