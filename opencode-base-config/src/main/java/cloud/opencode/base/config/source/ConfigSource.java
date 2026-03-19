package cloud.opencode.base.config.source;

import java.util.Map;

/**
 * Configuration Source Interface
 * 配置源接口
 *
 * <p>Represents a source of configuration properties. Implementations can load configuration
 * from various sources like files, environment variables, system properties, databases, etc.</p>
 * <p>表示配置属性的来源。实现类可以从文件、环境变量、系统属性、数据库等各种源加载配置。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configuration source abstraction - 配置源抽象</li>
 *   <li>Priority-based source ordering - 基于优先级的源排序</li>
 *   <li>Hot reload support - 热重载支持</li>
 *   <li>SPI extensibility - SPI扩展性</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Implement custom source
 * public class DatabaseConfigSource implements ConfigSource {
 *     @Override
 *     public String getName() {
 *         return "database";
 *     }
 *
 *     @Override
 *     public Map<String, String> getProperties() {
 *         return loadFromDatabase();
 *     }
 *
 *     @Override
 *     public int getPriority() {
 *         return 75; // Between properties and environment
 *     }
 * }
 *
 * // Use in builder
 * Config config = OpenConfig.builder()
 *     .addSource(new DatabaseConfigSource())
 *     .build();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>getProperties() should be cached - getProperties()应该被缓存</li>
 *   <li>Reload should check modification time - 重载应检查修改时间</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable properties map recommended - 推荐使用不可变属性映射</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public interface ConfigSource {

    /**
     * Get configuration source name
     * 获取配置源名称
     *
     * @return source name | 源名称
     */
    String getName();

    /**
     * Get all configuration properties
     * 获取所有配置属性
     *
     * <p><strong>Performance | 性能:</strong></p>
     * <p>This method should return a cached immutable map for best performance.</p>
     * <p>此方法应返回缓存的不可变映射以获得最佳性能。</p>
     *
     * @return configuration properties map | 配置属性映射
     */
    Map<String, String> getProperties();

    /**
     * Get configuration value by key
     * 根据键获取配置值
     *
     * @param key configuration key | 配置键
     * @return configuration value or null | 配置值或null
     */
    default String getProperty(String key) {
        return getProperties().get(key);
    }

    /**
     * Get configuration source priority (higher number = higher priority)
     * 获取配置源优先级(数值越大优先级越高)
     *
     * <p><strong>Default Priorities | 默认优先级:</strong></p>
     * <ul>
     *   <li>CommandLine: 200</li>
     *   <li>Environment: 100</li>
     *   <li>Properties/YAML: 50</li>
     *   <li>InMemory: 10</li>
     * </ul>
     *
     * @return priority value | 优先级值
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Check if source supports hot reload
     * 检查源是否支持热重载
     *
     * @return true if hot reload supported | 如果支持热重载返回true
     */
    default boolean supportsReload() {
        return false;
    }

    /**
     * Reload configuration from source
     * 从源重新加载配置
     *
     * <p>This method is called when hot reload is enabled.</p>
     * <p>启用热重载时会调用此方法。</p>
     */
    default void reload() {
        // Default: no-op
    }
}
