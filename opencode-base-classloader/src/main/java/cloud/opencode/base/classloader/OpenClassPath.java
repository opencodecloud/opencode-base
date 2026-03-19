package cloud.opencode.base.classloader;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

/**
 * OpenClassPath - ClassPath Utilities Facade
 * OpenClassPath - ClassPath 工具门面
 *
 * <p>Utilities for working with classpath.</p>
 * <p>用于操作 classpath 的工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Get classpath URLs - 获取 classpath URL</li>
 *   <li>Convert class name to resource path - 类名转资源路径</li>
 *   <li>Find resources - 查找资源</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get classpath URLs
 * List<URL> urls = OpenClassPath.getClassPathUrls();
 *
 * // Convert class name to path
 * String path = OpenClassPath.classNameToResourcePath("com.example.MyClass");
 * // Returns: "com/example/MyClass.class"
 *
 * // Find resource
 * Optional<URL> url = OpenClassPath.findResource("config.yml");
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
public final class OpenClassPath {

    private OpenClassPath() {
        // Utility class
    }

    // ==================== ClassPath Access | ClassPath 访问 ====================

    /**
     * Get all classpath URLs
     * 获取所有 classpath URL
     *
     * @return list of classpath URLs | classpath URL 列表
     */
    public static List<URL> getClassPathUrls() {
        List<URL> urls = new ArrayList<>();
        String classpath = System.getProperty("java.class.path");
        if (classpath != null) {
            for (String path : classpath.split(System.getProperty("path.separator"))) {
                try {
                    urls.add(Path.of(path).toUri().toURL());
                } catch (Exception ignored) {
                }
            }
        }
        return urls;
    }

    /**
     * Get all classpath entries as paths
     * 获取所有 classpath 条目作为路径
     *
     * @return list of classpath paths | classpath 路径列表
     */
    public static List<Path> getClassPathEntries() {
        List<Path> entries = new ArrayList<>();
        String classpath = System.getProperty("java.class.path");
        if (classpath != null) {
            for (String path : classpath.split(System.getProperty("path.separator"))) {
                entries.add(Path.of(path));
            }
        }
        return entries;
    }

    /**
     * Get runtime classpath string
     * 获取运行时 classpath 字符串
     *
     * @return classpath string | classpath 字符串
     */
    public static String getClassPath() {
        return System.getProperty("java.class.path", "");
    }

    // ==================== Path Conversion | 路径转换 ====================

    /**
     * Convert class name to resource path
     * 将类名转换为资源路径
     *
     * @param className fully qualified class name | 完全限定类名
     * @return resource path | 资源路径
     */
    public static String classNameToResourcePath(String className) {
        Objects.requireNonNull(className, "Class name must not be null");
        return className.replace('.', '/') + ".class";
    }

    /**
     * Convert resource path to class name
     * 将资源路径转换为类名
     *
     * @param resourcePath resource path | 资源路径
     * @return class name | 类名
     */
    public static String resourcePathToClassName(String resourcePath) {
        Objects.requireNonNull(resourcePath, "Resource path must not be null");
        String path = resourcePath;
        if (path.endsWith(".class")) {
            path = path.substring(0, path.length() - 6);
        }
        return path.replace('/', '.');
    }

    /**
     * Convert package name to resource path
     * 将包名转换为资源路径
     *
     * @param packageName package name | 包名
     * @return resource path | 资源路径
     */
    public static String packageNameToResourcePath(String packageName) {
        Objects.requireNonNull(packageName, "Package name must not be null");
        return packageName.replace('.', '/');
    }

    // ==================== Resource Finding | 资源查找 ====================

    /**
     * Get all resource URLs for a package
     * 获取包的所有资源 URL
     *
     * @param packageName package name | 包名
     * @return list of URLs | URL 列表
     */
    public static List<URL> getPackageResources(String packageName) {
        Objects.requireNonNull(packageName, "Package name must not be null");
        List<URL> urls = new ArrayList<>();
        String resourcePath = packageNameToResourcePath(packageName);

        try {
            ClassLoader cl = OpenClassLoader.getDefaultClassLoader();
            Enumeration<URL> resources = cl.getResources(resourcePath);
            while (resources.hasMoreElements()) {
                urls.add(resources.nextElement());
            }
        } catch (IOException ignored) {
        }

        return urls;
    }

    /**
     * Find single resource
     * 查找单个资源
     *
     * @param resourceName resource name | 资源名
     * @return optional URL | 可选的 URL
     */
    public static Optional<URL> findResource(String resourceName) {
        Objects.requireNonNull(resourceName, "Resource name must not be null");
        URL url = OpenClassLoader.getDefaultClassLoader().getResource(resourceName);
        return Optional.ofNullable(url);
    }

    /**
     * Find all matching resources
     * 查找所有匹配的资源
     *
     * @param resourceName resource name | 资源名
     * @return list of URLs | URL 列表
     */
    public static List<URL> findResources(String resourceName) {
        Objects.requireNonNull(resourceName, "Resource name must not be null");
        List<URL> urls = new ArrayList<>();

        try {
            Enumeration<URL> resources = OpenClassLoader.getDefaultClassLoader().getResources(resourceName);
            while (resources.hasMoreElements()) {
                urls.add(resources.nextElement());
            }
        } catch (IOException ignored) {
        }

        return urls;
    }

    /**
     * Check if resource exists on classpath
     * 检查资源是否存在于 classpath
     *
     * @param resourceName resource name | 资源名
     * @return true if exists | 存在返回 true
     */
    public static boolean resourceExists(String resourceName) {
        return findResource(resourceName).isPresent();
    }

    /**
     * Check if class exists on classpath
     * 检查类是否存在于 classpath
     *
     * @param className class name | 类名
     * @return true if exists | 存在返回 true
     */
    public static boolean classExists(String className) {
        return resourceExists(classNameToResourcePath(className));
    }
}
