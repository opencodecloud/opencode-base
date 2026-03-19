package cloud.opencode.base.io.resource;

/**
 * Resource Loader Interface
 * 资源加载器接口
 *
 * <p>Interface for loading resources with location prefix support.
 * Automatically detects resource type based on location prefix.</p>
 * <p>支持位置前缀的资源加载接口。
 * 根据位置前缀自动检测资源类型。</p>
 *
 * <p><strong>Supported Prefixes | 支持的前缀:</strong></p>
 * <ul>
 *   <li>classpath: - classpath resource | 类路径资源</li>
 *   <li>file: - filesystem resource | 文件系统资源</li>
 *   <li>http:/https: - URL resource | URL资源</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ResourceLoader loader = ResourceLoader.getDefault();
 * Resource r1 = loader.getResource("classpath:config.yaml");
 * Resource r2 = loader.getResource("file:/etc/app/config.yaml");
 * Resource r3 = loader.getResource("https://server/config.yaml");
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unified resource loading with location prefix detection - 统一资源加载，支持位置前缀检测</li>
 *   <li>Support for classpath:, file:, http/https: prefixes - 支持classpath:、file:、http/https:前缀</li>
 *   <li>Default implementation via getDefault() - 通过getDefault()获取默认实现</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No, location must not be null - 空值安全: 否，位置不可为null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public interface ResourceLoader {

    /**
     * Classpath prefix
     * 类路径前缀
     */
    String CLASSPATH_PREFIX = "classpath:";

    /**
     * File prefix
     * 文件前缀
     */
    String FILE_PREFIX = "file:";

    /**
     * Loads a resource from the given location
     * 从给定位置加载资源
     *
     * @param location the resource location | 资源位置
     * @return resource | 资源
     */
    Resource getResource(String location);

    /**
     * Gets the ClassLoader used by this loader
     * 获取此加载器使用的ClassLoader
     *
     * @return classloader | 类加载器
     */
    ClassLoader getClassLoader();

    /**
     * Gets the default resource loader
     * 获取默认资源加载器
     *
     * @return default loader | 默认加载器
     */
    static ResourceLoader getDefault() {
        return DefaultResourceLoader.INSTANCE;
    }
}
