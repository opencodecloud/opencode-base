package cloud.opencode.base.config.source;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * System Properties Configuration Source
 * 系统属性配置源
 *
 * <p>Loads configuration from Java system properties (System.getProperties()).</p>
 * <p>从Java系统属性加载配置(System.getProperties())。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Java system properties access - Java系统属性访问</li>
 *   <li>JVM arguments support (-Dkey=value) - JVM参数支持</li>
 *   <li>Medium priority (50) - 中等优先级(50)</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create source
 * ConfigSource source = new SystemPropertiesConfigSource();
 *
 * // Set via JVM args
 * // java -Dapp.name=MyApp -Dserver.port=8080 Main
 *
 * // Or programmatically
 * System.setProperty("app.name", "MyApp");
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for loading - 时间复杂度: 加载为O(n)</li>
 *   <li>Properties snapshot at creation - 创建时的属性快照</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable snapshot - 不可变快照</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class SystemPropertiesConfigSource implements ConfigSource {

    private final Map<String, String> properties;

    /**
     * Create system properties config source
     * 创建系统属性配置源
     */
    public SystemPropertiesConfigSource() {
        this.properties = loadProperties();
    }

    @Override
    public String getName() {
        return "system-properties";
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public int getPriority() {
        return 50; // Same as properties files
    }

    /**
     * Load properties from system properties
     * 从系统属性加载属性
     */
    private Map<String, String> loadProperties() {
        return System.getProperties().entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(
                e -> e.getKey().toString(),
                e -> e.getValue().toString()
            ));
    }
}
