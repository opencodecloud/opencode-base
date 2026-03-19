package cloud.opencode.base.classloader;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;
import cloud.opencode.base.classloader.resource.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * OpenResource - Resource Loading Facade
 * OpenResource - 资源加载门面
 *
 * <p>Unified entry point for resource loading operations.</p>
 * <p>资源加载操作的统一入口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Load single resource - 加载单个资源</li>
 *   <li>Load multiple resources - 加载多个资源</li>
 *   <li>Quick read methods - 快速读取方法</li>
 *   <li>Resource existence check - 资源存在性检查</li>
 * </ul>
 *
 * <p><strong>Supported Protocols | 支持的协议:</strong></p>
 * <ul>
 *   <li>classpath: - classpath resource | classpath 资源</li>
 *   <li>classpath*: - all matching classpath resources | 所有匹配的 classpath 资源</li>
 *   <li>file: - file system resource | 文件系统资源</li>
 *   <li>url: - URL resource | URL 资源</li>
 *   <li>jar: - JAR resource | JAR 资源</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Load single resource
 * Resource config = OpenResource.get("classpath:config.yml");
 * String content = config.getString();
 *
 * // Quick read
 * String content = OpenResource.readString("classpath:config.yml");
 *
 * // Load multiple resources
 * List<Resource> xmlFiles = OpenResource.getAll("classpath*:META-INF/*.xml");
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
public final class OpenResource {

    private static final ResourceLoader DEFAULT_LOADER = ResourceLoader.create();

    private OpenResource() {
        // Utility class
    }

    // ==================== Single Resource | 单个资源 ====================

    /**
     * Get single resource by location
     * 按位置获取单个资源
     *
     * @param location resource location | 资源位置
     * @return resource | 资源
     */
    public static Resource get(String location) {
        Objects.requireNonNull(location, "Location must not be null");
        return DEFAULT_LOADER.load(location);
    }

    /**
     * Get classpath resource
     * 获取 classpath 资源
     *
     * @param path resource path | 资源路径
     * @return resource | 资源
     */
    public static Resource classpath(String path) {
        return new ClassPathResource(path);
    }

    /**
     * Get file resource
     * 获取文件资源
     *
     * @param path file path | 文件路径
     * @return resource | 资源
     */
    public static Resource file(String path) {
        return new FileResource(path);
    }

    /**
     * Get URL resource
     * 获取 URL 资源
     *
     * @param url URL string | URL 字符串
     * @return resource | 资源
     */
    public static Resource url(String url) {
        try {
            return new UrlResource(url);
        } catch (Exception e) {
            throw new OpenClassLoaderException("Invalid URL: " + url, e);
        }
    }

    /**
     * Create byte array resource
     * 创建字节数组资源
     *
     * @param content byte array content | 字节数组内容
     * @return resource | 资源
     */
    public static Resource bytes(byte[] content) {
        return new ByteArrayResource(content);
    }

    /**
     * Create byte array resource with description
     * 创建带描述的字节数组资源
     *
     * @param content     byte array content | 字节数组内容
     * @param description description | 描述
     * @return resource | 资源
     */
    public static Resource bytes(byte[] content, String description) {
        return new ByteArrayResource(content, description);
    }

    // ==================== Multiple Resources | 多个资源 ====================

    /**
     * Get all resources matching location pattern
     * 获取所有匹配位置模式的资源
     *
     * @param locationPattern location pattern | 位置模式
     * @return list of resources | 资源列表
     */
    public static List<Resource> getAll(String locationPattern) {
        Objects.requireNonNull(locationPattern, "Location pattern must not be null");
        return DEFAULT_LOADER.loadAll(locationPattern);
    }

    // ==================== Existence Check | 存在性检查 ====================

    /**
     * Check if resource exists
     * 检查资源是否存在
     *
     * @param location resource location | 资源位置
     * @return true if exists | 存在返回 true
     */
    public static boolean exists(String location) {
        try {
            return get(location).exists();
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Quick Read | 快速读取 ====================

    /**
     * Read resource as string (UTF-8)
     * 读取资源为字符串（UTF-8）
     *
     * @param location resource location | 资源位置
     * @return content as string | 字符串内容
     * @throws IOException if read fails | 读取失败时抛出
     */
    public static String readString(String location) throws IOException {
        return get(location).getString();
    }

    /**
     * Read resource as byte array
     * 读取资源为字节数组
     *
     * @param location resource location | 资源位置
     * @return content as bytes | 字节内容
     * @throws IOException if read fails | 读取失败时抛出
     */
    public static byte[] readBytes(String location) throws IOException {
        return get(location).getBytes();
    }

    /**
     * Read resource as lines
     * 读取资源为行列表
     *
     * @param location resource location | 资源位置
     * @return list of lines | 行列表
     * @throws IOException if read fails | 读取失败时抛出
     */
    public static List<String> readLines(String location) throws IOException {
        return get(location).readLines();
    }

    // ==================== Loader | 加载器 ====================

    /**
     * Create resource loader
     * 创建资源加载器
     *
     * @return resource loader | 资源加载器
     */
    public static ResourceLoader loader() {
        return ResourceLoader.create();
    }

    /**
     * Create resource loader with classloader
     * 使用类加载器创建资源加载器
     *
     * @param classLoader class loader | 类加载器
     * @return resource loader | 资源加载器
     */
    public static ResourceLoader loader(ClassLoader classLoader) {
        return ResourceLoader.create(classLoader);
    }
}
