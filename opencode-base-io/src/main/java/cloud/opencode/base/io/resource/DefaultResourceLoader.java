package cloud.opencode.base.io.resource;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Default Resource Loader Implementation
 * 默认资源加载器实现
 *
 * <p>Default implementation of ResourceLoader that intelligently loads
 * resources based on location prefix.</p>
 * <p>ResourceLoader的默认实现，根据位置前缀智能加载资源。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Automatic prefix detection - 自动前缀检测</li>
 *   <li>Support classpath, file, URL - 支持类路径、文件、URL</li>
 *   <li>Thread-safe singleton - 线程安全单例</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ResourceLoader loader = DefaultResourceLoader.INSTANCE;
 * Resource resource = loader.getResource("classpath:config.properties");
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public final class DefaultResourceLoader implements ResourceLoader {

    /**
     * Singleton instance
     * 单例实例
     */
    public static final DefaultResourceLoader INSTANCE = new DefaultResourceLoader();

    private final ClassLoader classLoader;

    /**
     * Creates a default resource loader
     * 创建默认资源加载器
     */
    public DefaultResourceLoader() {
        this(getDefaultClassLoader());
    }

    /**
     * Creates a resource loader with specific classloader
     * 使用指定类加载器创建资源加载器
     *
     * @param classLoader the classloader | 类加载器
     */
    public DefaultResourceLoader(ClassLoader classLoader) {
        this.classLoader = classLoader != null ? classLoader : getDefaultClassLoader();
    }

    private static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = DefaultResourceLoader.class.getClassLoader();
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }
        }
        return cl;
    }

    @Override
    public Resource getResource(String location) {
        Objects.requireNonNull(location, "Location must not be null");

        // Classpath resource
        if (location.startsWith(CLASSPATH_PREFIX)) {
            String path = location.substring(CLASSPATH_PREFIX.length());
            return new ClassPathResource(path, classLoader);
        }

        // File resource
        if (location.startsWith(FILE_PREFIX)) {
            String path = location.substring(FILE_PREFIX.length());
            return new FileSystemResource(Path.of(path));
        }

        // URL resource (http, https, ftp, etc.)
        if (isUrl(location)) {
            return new UrlResource(location);
        }

        // Default: try classpath first, then file
        URL url = classLoader.getResource(location);
        if (url != null) {
            return new ClassPathResource(location, classLoader);
        }

        // Treat as file path
        return new FileSystemResource(Path.of(location));
    }

    private boolean isUrl(String location) {
        if (location.startsWith("http://") || location.startsWith("https://") ||
                location.startsWith("ftp://") || location.startsWith("jar:")) {
            return true;
        }
        try {
            new URL(location);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public String toString() {
        return "DefaultResourceLoader[classLoader=" + classLoader + "]";
    }
}
