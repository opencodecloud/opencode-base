package cloud.opencode.base.classloader.loader;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Resource ClassLoader - Focused on resource loading
 * 资源类加载器 - 专注于资源加载
 *
 * <p>ClassLoader that focuses on resource loading from multiple paths.</p>
 * <p>专注于从多个路径加载资源的类加载器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple resource paths - 多资源路径</li>
 *   <li>Dynamic path management - 动态路径管理</li>
 *   <li>Resource refresh - 资源刷新</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ResourceClassLoader loader = ResourceClassLoader.create(Path.of("/resources"));
 * loader.addResourcePath(Path.of("/extra-resources"));
 * URL resource = loader.getResource("config.yml");
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
public class ResourceClassLoader extends ClassLoader {

    private final CopyOnWriteArrayList<Path> resourcePaths = new CopyOnWriteArrayList<>();

    /**
     * Create resource classloader with default parent
     * 使用默认父加载器创建资源类加载器
     */
    public ResourceClassLoader() {
        super(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Create resource classloader with specified parent
     * 使用指定父加载器创建资源类加载器
     *
     * @param parent parent classloader | 父类加载器
     */
    public ResourceClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * Create resource classloader with resource paths
     * 使用资源路径创建资源类加载器
     *
     * @param resourcePaths resource paths | 资源路径
     * @return new ResourceClassLoader | 新的资源类加载器
     */
    public static ResourceClassLoader create(Path... resourcePaths) {
        ResourceClassLoader loader = new ResourceClassLoader();
        for (Path path : resourcePaths) {
            loader.addResourcePath(path);
        }
        return loader;
    }

    /**
     * Create resource classloader with parent and resource paths
     * 使用父加载器和资源路径创建资源类加载器
     *
     * @param parent        parent classloader | 父类加载器
     * @param resourcePaths resource paths | 资源路径
     * @return new ResourceClassLoader | 新的资源类加载器
     */
    public static ResourceClassLoader create(ClassLoader parent, Path... resourcePaths) {
        ResourceClassLoader loader = new ResourceClassLoader(parent);
        for (Path path : resourcePaths) {
            loader.addResourcePath(path);
        }
        return loader;
    }

    // ==================== Path Management | 路径管理 ====================

    /**
     * Add resource path
     * 添加资源路径
     *
     * @param path resource path | 资源路径
     */
    public void addResourcePath(Path path) {
        Objects.requireNonNull(path, "Path must not be null");
        Path absolutePath = path.toAbsolutePath().normalize();
        if (!resourcePaths.contains(absolutePath)) {
            resourcePaths.add(absolutePath);
        }
    }

    /**
     * Remove resource path
     * 移除资源路径
     *
     * @param path resource path | 资源路径
     */
    public void removeResourcePath(Path path) {
        Objects.requireNonNull(path, "Path must not be null");
        resourcePaths.remove(path.toAbsolutePath().normalize());
    }

    /**
     * Get all resource paths
     * 获取所有资源路径
     *
     * @return list of resource paths | 资源路径列表
     */
    public List<Path> getResourcePaths() {
        return List.copyOf(resourcePaths);
    }

    /**
     * Clear all resource paths
     * 清除所有资源路径
     */
    public void clearResourcePaths() {
        resourcePaths.clear();
    }

    // ==================== Resource Loading | 资源加载 ====================

    /**
     * Safely resolve a resource path against a base path, preventing path traversal.
     * 安全地将资源路径解析到基路径，防止路径遍历。
     *
     * @param basePath base path | 基路径
     * @param name     resource name | 资源名
     * @return resolved path or null if path traversal detected | 解析后的路径，如果检测到路径遍历则返回 null
     */
    private Path safeResolve(Path basePath, String name) {
        Path resolved = basePath.resolve(name).normalize();
        if (!resolved.startsWith(basePath)) {
            // Path traversal attempt detected
            return null;
        }
        return resolved;
    }

    @Override
    public URL getResource(String name) {
        // Try custom paths first
        for (Path basePath : resourcePaths) {
            Path resourcePath = safeResolve(basePath, name);
            if (resourcePath != null && Files.exists(resourcePath)) {
                try {
                    return resourcePath.toUri().toURL();
                } catch (MalformedURLException e) {
                    // Continue to next path
                }
            }
        }
        // Fall back to parent
        return super.getResource(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        // Try custom paths first
        for (Path basePath : resourcePaths) {
            Path resourcePath = safeResolve(basePath, name);
            if (resourcePath != null && Files.exists(resourcePath)) {
                try {
                    return Files.newInputStream(resourcePath);
                } catch (IOException e) {
                    // Continue to next path
                }
            }
        }
        // Fall back to parent
        return super.getResourceAsStream(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> urls = new ArrayList<>();

        // Collect from custom paths
        for (Path basePath : resourcePaths) {
            Path resourcePath = safeResolve(basePath, name);
            if (resourcePath != null && Files.exists(resourcePath)) {
                urls.add(resourcePath.toUri().toURL());
            }
        }

        // Add parent resources
        Enumeration<URL> parentResources = super.getResources(name);
        while (parentResources.hasMoreElements()) {
            urls.add(parentResources.nextElement());
        }

        return Collections.enumeration(urls);
    }

    /**
     * Refresh resources (re-scan paths)
     * 刷新资源（重新扫描路径）
     */
    public void refresh() {
        // In this simple implementation, nothing to refresh
        // More complex implementations could cache resource listings
    }

    /**
     * Check if resource exists
     * 检查资源是否存在
     *
     * @param name resource name | 资源名
     * @return true if exists | 存在返回 true
     */
    public boolean resourceExists(String name) {
        return getResource(name) != null;
    }

    /**
     * List resources in a path
     * 列出路径中的资源
     *
     * @param path path relative to resource roots | 相对于资源根的路径
     * @return list of resource names | 资源名列表
     */
    public List<String> listResources(String path) {
        List<String> result = new ArrayList<>();
        for (Path basePath : resourcePaths) {
            Path fullPath = safeResolve(basePath, path);
            if (fullPath != null && Files.isDirectory(fullPath)) {
                try (var stream = Files.list(fullPath)) {
                    stream.map(p -> p.getFileName().toString())
                            .forEach(result::add);
                } catch (IOException ignored) {
                }
            }
        }
        return result;
    }
}
