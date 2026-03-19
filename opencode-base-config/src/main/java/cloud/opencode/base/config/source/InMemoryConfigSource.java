package cloud.opencode.base.config.source;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory Configuration Source
 * 内存配置源
 *
 * <p>Stores configuration in memory with support for runtime updates and programmatic configuration.</p>
 * <p>在内存中存储配置,支持运行时更新和编程配置。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Programmatic configuration - 编程配置</li>
 *   <li>Runtime updates - 运行时更新</li>
 *   <li>Thread-safe mutations - 线程安全的修改</li>
 *   <li>Low priority (10) - 低优先级(10)</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // From map
 * Map<String, String> props = Map.of(
 *     "app.name", "MyApp",
 *     "server.port", "8080"
 * );
 * InMemoryConfigSource source = new InMemoryConfigSource(props);
 *
 * // Runtime updates
 * source.setProperty("server.port", "9090");
 * source.removeProperty("old.key");
 *
 * // Bulk update
 * source.setProperties(Map.of("key1", "val1", "key2", "val2"));
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for get/set - 时间复杂度: 获取/设置为O(1)</li>
 *   <li>Thread-safe concurrent access - 线程安全的并发访问</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Mutable source - 可变源</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class InMemoryConfigSource implements ConfigSource {

    private final String name;
    private final ConcurrentHashMap<String, String> properties;

    /**
     * Create empty in-memory config source
     * 创建空的内存配置源
     */
    public InMemoryConfigSource() {
        this("in-memory", new HashMap<>());
    }

    /**
     * Create in-memory config source with initial properties
     * 创建带初始属性的内存配置源
     *
     * @param properties initial properties | 初始属性
     */
    public InMemoryConfigSource(Map<String, String> properties) {
        this("in-memory", properties);
    }

    /**
     * Create named in-memory config source
     * 创建命名的内存配置源
     *
     * @param name source name | 源名称
     * @param properties initial properties | 初始属性
     */
    public InMemoryConfigSource(String name, Map<String, String> properties) {
        this.name = name;
        this.properties = new ConcurrentHashMap<>(properties);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, String> getProperties() {
        return Map.copyOf(properties);
    }

    @Override
    public int getPriority() {
        return 10; // Lowest priority - used as fallback defaults
    }

    /**
     * Set property value
     * 设置属性值
     *
     * @param key property key | 属性键
     * @param value property value | 属性值
     */
    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    /**
     * Remove property
     * 移除属性
     *
     * @param key property key | 属性键
     * @return previous value or null | 之前的值或null
     */
    public String removeProperty(String key) {
        return properties.remove(key);
    }

    /**
     * Set multiple properties
     * 设置多个属性
     *
     * @param props properties to set | 要设置的属性
     */
    public void setProperties(Map<String, String> props) {
        properties.putAll(props);
    }

    /**
     * Clear all properties
     * 清除所有属性
     */
    public void clear() {
        properties.clear();
    }
}
