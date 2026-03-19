package cloud.opencode.base.io.resource;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Classpath Resource Implementation
 * 类路径资源实现
 *
 * <p>Resource implementation for resources loaded from classpath.
 * Uses ClassLoader to locate and load resources.</p>
 * <p>用于从类路径加载资源的资源实现。
 * 使用ClassLoader定位和加载资源。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Load resources from classpath - 从类路径加载资源</li>
 *   <li>Support custom ClassLoader - 支持自定义ClassLoader</li>
 *   <li>Immutable and thread-safe - 不可变且线程安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ClassPathResource resource = new ClassPathResource("config/app.properties");
 * String content = resource.readString();
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public final class ClassPathResource implements Resource {

    private final String path;
    private final ClassLoader classLoader;

    /**
     * Creates a classpath resource with default classloader
     * 使用默认类加载器创建类路径资源
     *
     * @param path the resource path | 资源路径
     */
    public ClassPathResource(String path) {
        this(path, getDefaultClassLoader());
    }

    /**
     * Creates a classpath resource with specific classloader
     * 使用指定类加载器创建类路径资源
     *
     * @param path        the resource path | 资源路径
     * @param classLoader the classloader | 类加载器
     */
    public ClassPathResource(String path, ClassLoader classLoader) {
        Objects.requireNonNull(path, "Path must not be null");
        this.path = normalizePath(path);
        this.classLoader = classLoader != null ? classLoader : getDefaultClassLoader();
    }

    private static String normalizePath(String path) {
        String normalized = path;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassPathResource.class.getClassLoader();
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }
        }
        return cl;
    }

    @Override
    public boolean exists() {
        return classLoader.getResource(path) != null;
    }

    @Override
    public boolean isReadable() {
        return exists();
    }

    @Override
    public InputStream getInputStream() {
        InputStream is = classLoader.getResourceAsStream(path);
        if (is == null) {
            throw OpenIOOperationException.resourceNotFound(path);
        }
        return is;
    }

    @Override
    public URL getURL() {
        URL url = classLoader.getResource(path);
        if (url == null) {
            throw OpenIOOperationException.resourceNotFound(path);
        }
        return url;
    }

    @Override
    public Path getPath() {
        try {
            URL url = getURL();
            if ("file".equals(url.getProtocol())) {
                return Path.of(url.toURI());
            }
        } catch (Exception e) {
            // Path not available for this resource
        }
        return null;
    }

    @Override
    public String getDescription() {
        return "classpath:" + path;
    }

    @Override
    public String getFilename() {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    @Override
    public long contentLength() {
        try (InputStream is = getInputStream()) {
            return is.available();
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public long lastModified() {
        try {
            URL url = getURL();
            java.net.URLConnection conn = url.openConnection();
            try {
                return conn.getLastModified();
            } finally {
                if (conn instanceof java.net.HttpURLConnection hc) {
                    hc.disconnect();
                } else {
                    try {
                        conn.getInputStream().close();
                    } catch (IOException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public Resource createRelative(String relativePath) {
        String newPath = resolvePath(path, relativePath);
        return new ClassPathResource(newPath, classLoader);
    }

    private String resolvePath(String base, String relative) {
        int lastSlash = base.lastIndexOf('/');
        if (lastSlash >= 0) {
            return base.substring(0, lastSlash + 1) + relative;
        }
        return relative;
    }

    /**
     * Gets the classpath
     * 获取类路径
     *
     * @return path | 路径
     */
    public String getClassPath() {
        return path;
    }

    /**
     * Gets the classloader
     * 获取类加载器
     *
     * @return classloader | 类加载器
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassPathResource that)) return false;
        return Objects.equals(path, that.path) && Objects.equals(classLoader, that.classLoader);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, classLoader);
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
