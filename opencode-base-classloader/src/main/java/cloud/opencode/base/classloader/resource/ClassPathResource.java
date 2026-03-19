package cloud.opencode.base.classloader.resource;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

/**
 * ClassPath Resource - Resource loaded from classpath
 * ClassPath 资源 - 从 classpath 加载的资源
 *
 * <p>Represents a resource that can be loaded from the classpath.</p>
 * <p>表示可以从 classpath 加载的资源。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Load resources from classpath - 从 classpath 加载资源</li>
 *   <li>Support custom ClassLoader - 支持自定义 ClassLoader</li>
 *   <li>Relative resource creation - 相对资源创建</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Resource resource = new ClassPathResource("config.yml");
 * Resource resource = new ClassPathResource("config.yml", customClassLoader);
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
public class ClassPathResource extends AbstractResource {

    private final String path;
    private final ClassLoader classLoader;

    /**
     * Create classpath resource with default classloader
     * 使用默认类加载器创建 classpath 资源
     *
     * @param path resource path | 资源路径
     */
    public ClassPathResource(String path) {
        this(path, null);
    }

    /**
     * Create classpath resource with specified classloader
     * 使用指定类加载器创建 classpath 资源
     *
     * @param path        resource path | 资源路径
     * @param classLoader class loader | 类加载器
     */
    public ClassPathResource(String path, ClassLoader classLoader) {
        Objects.requireNonNull(path, "Path must not be null");
        this.path = cleanPath(path);
        this.classLoader = classLoader != null ? classLoader : getDefaultClassLoader();
    }

    private String cleanPath(String path) {
        String cleaned = path;
        if (cleaned.startsWith("/")) {
            cleaned = cleaned.substring(1);
        }
        return cleaned;
    }

    private ClassLoader getDefaultClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassPathResource.class.getClassLoader();
        }
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        return cl;
    }

    @Override
    public boolean exists() {
        return classLoader.getResource(path) != null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream is = classLoader.getResourceAsStream(path);
        if (is == null) {
            throw new OpenClassLoaderException("Resource not found: " + path);
        }
        return is;
    }

    @Override
    public URL getUrl() throws IOException {
        URL url = classLoader.getResource(path);
        if (url == null) {
            throw new OpenClassLoaderException("Resource not found: " + path);
        }
        return url;
    }

    @Override
    public String getFilename() {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash != -1 ? path.substring(lastSlash + 1) : path;
    }

    @Override
    public String getDescription() {
        return "classpath:" + path;
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        String pathToUse;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1) {
            pathToUse = path.substring(0, lastSlash + 1) + relativePath;
        } else {
            pathToUse = relativePath;
        }
        return new ClassPathResource(pathToUse, classLoader);
    }

    /**
     * Get the resource path
     * 获取资源路径
     *
     * @return resource path | 资源路径
     */
    public String getResourcePath() {
        return path;
    }

    /**
     * Get the class loader
     * 获取类加载器
     *
     * @return class loader | 类加载器
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
