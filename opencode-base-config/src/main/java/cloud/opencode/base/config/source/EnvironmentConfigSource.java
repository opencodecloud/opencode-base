package cloud.opencode.base.config.source;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Environment Variables Configuration Source
 * 环境变量配置源
 *
 * <p>Loads configuration from system environment variables with optional prefix filtering
 * and automatic key name conversion.</p>
 * <p>从系统环境变量加载配置,支持可选的前缀过滤和自动键名转换。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Environment variable to property key conversion - 环境变量到属性键的转换</li>
 *   <li>Prefix-based filtering - 基于前缀的过滤</li>
 *   <li>Automatic case conversion (UPPER_SNAKE to lower.dot) - 自动大小写转换</li>
 *   <li>High priority (100) - 高优先级(100)</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // All environment variables
 * ConfigSource source = new EnvironmentConfigSource();
 * // DATABASE_URL -> database.url
 *
 * // With prefix filter
 * ConfigSource source = new EnvironmentConfigSource("APP_");
 * // APP_DATABASE_URL -> database.url
 * // APP_SERVER_PORT -> server.port
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for initialization - 时间复杂度: 初始化为O(n)</li>
 *   <li>Environment variables cached at creation - 环境变量在创建时缓存</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable properties - 不可变属性</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class EnvironmentConfigSource implements ConfigSource {

    private final String prefix;
    private final Map<String, String> properties;

    /**
     * Create environment config source without prefix
     * 创建无前缀的环境配置源
     */
    public EnvironmentConfigSource() {
        this(null);
    }

    /**
     * Create environment config source with prefix
     * 创建带前缀的环境配置源
     *
     * @param prefix environment variable prefix | 环境变量前缀
     */
    public EnvironmentConfigSource(String prefix) {
        this.prefix = prefix;
        this.properties = loadProperties();
    }

    @Override
    public String getName() {
        return "environment" + (prefix != null ? "[" + prefix + "]" : "");
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public int getPriority() {
        return 100; // Environment variables have high priority
    }

    /**
     * Load properties from environment variables
     * 从环境变量加载属性
     */
    private Map<String, String> loadProperties() {
        Map<String, String> env = System.getenv();

        if (prefix == null) {
            return convertKeys(env);
        }

        String upperPrefix = prefix.toUpperCase().replace(".", "_");
        if (!upperPrefix.endsWith("_")) {
            upperPrefix += "_";
        }

        final String finalPrefix = upperPrefix;
        return env.entrySet().stream()
            .filter(e -> e.getKey().startsWith(finalPrefix))
            .collect(Collectors.toUnmodifiableMap(
                e -> convertKey(e.getKey().substring(finalPrefix.length())),
                Map.Entry::getValue
            ));
    }

    /**
     * Convert all environment keys to property keys
     * 将所有环境键转换为属性键
     */
    private Map<String, String> convertKeys(Map<String, String> env) {
        return env.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(
                e -> convertKey(e.getKey()),
                Map.Entry::getValue
            ));
    }

    /**
     * Convert environment variable name to property key
     * 将环境变量名转换为属性键
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * DATABASE_URL      -> database.url
     * SERVER_PORT       -> server.port
     * APP_NAME          -> app.name
     * MY_CUSTOM_VALUE   -> my.custom.value
     * </pre>
     *
     * @param envKey environment variable name | 环境变量名
     * @return property key | 属性键
     */
    private String convertKey(String envKey) {
        return envKey.toLowerCase().replace("_", ".");
    }
}
