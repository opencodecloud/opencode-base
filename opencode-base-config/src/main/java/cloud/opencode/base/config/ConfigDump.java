package cloud.opencode.base.config;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Configuration export with sensitive value masking
 * 配置导出与敏感值掩码
 *
 * <p>Provides utilities to export configuration key-value pairs with automatic masking
 * of sensitive values such as passwords, secrets, tokens, and API keys.</p>
 * <p>提供配置键值对导出工具，自动掩码敏感值如密码、密钥、令牌和API密钥。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Dump all configuration entries - 导出所有配置条目</li>
 *   <li>Sensitive value masking with configurable patterns - 可配置模式的敏感值掩码</li>
 *   <li>Formatted string output sorted alphabetically - 按字母排序的格式化字符串输出</li>
 *   <li>Case-insensitive key segment matching - 不区分大小写的键段匹配</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Config config = OpenConfig.builder()
 *     .addProperties(Map.of("db.url", "jdbc:mysql://...",
 *                           "db.password", "secret123"))
 *     .build();
 *
 * // Dump with default sensitive patterns
 * Map<String, String> dump = ConfigDump.dump(config);
 * // db.password → "***", db.url → "jdbc:mysql://..."
 *
 * // Dump as formatted string
 * String output = ConfigDump.dumpToString(config);
 * // db.password = ***
 * // db.url = jdbc:mysql://...
 *
 * // Custom sensitive patterns
 * Map<String, String> custom = ConfigDump.dump(config, Set.of("url"));
 * // db.url → "***", db.password → "secret123"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Default patterns cover common sensitive keys - 默认模式覆盖常见敏感键</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.3
 */
public final class ConfigDump {

    private ConfigDump() {
        // Prevent instantiation
    }

    /**
     * Default sensitive key patterns (case-insensitive substring match on key segments)
     * 默认敏感键模式（对键段进行不区分大小写的子字符串匹配）
     */
    private static final Set<String> DEFAULT_SENSITIVE_PATTERNS = Set.of(
            "password", "secret", "token", "credential", "api-key", "apikey",
            "secretkey", "privatekey", "passphrase", "key", "auth", "bearer"
    );

    /**
     * Mask replacement string
     * 掩码替换字符串
     */
    private static final String MASK = "***";

    // ==================== Dump Methods | 导出方法 ====================

    /**
     * Dump all configuration entries with default sensitive patterns
     * 使用默认敏感模式导出所有配置条目
     *
     * <p>Returns a sorted map where sensitive values are replaced by "***".</p>
     * <p>返回一个排序映射，其中敏感值被替换为"***"。</p>
     *
     * @param config configuration instance | 配置实例
     * @return sorted map of key-value pairs with sensitive values masked | 敏感值已掩码的排序键值对映射
     * @throws NullPointerException if config is null | 如果config为null
     */
    public static Map<String, String> dump(Config config) {
        Objects.requireNonNull(config, "config must not be null");
        return dump(config, DEFAULT_SENSITIVE_PATTERNS);
    }

    /**
     * Dump all configuration entries with custom sensitive patterns
     * 使用自定义敏感模式导出所有配置条目
     *
     * <p>Returns a sorted map where values matching the sensitive patterns are replaced by "***".</p>
     * <p>返回一个排序映射，其中匹配敏感模式的值被替换为"***"。</p>
     *
     * @param config            configuration instance | 配置实例
     * @param sensitivePatterns set of sensitive key patterns | 敏感键模式集合
     * @return sorted map of key-value pairs with sensitive values masked | 敏感值已掩码的排序键值对映射
     * @throws NullPointerException if config or sensitivePatterns is null | 如果config或sensitivePatterns为null
     */
    public static Map<String, String> dump(Config config, Set<String> sensitivePatterns) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(sensitivePatterns, "sensitivePatterns must not be null");

        Map<String, String> result = new TreeMap<>();
        for (String key : config.getKeys()) {
            // Use default value to avoid exception if key is removed during hot-reload
            String value = config.getString(key, null);
            result.put(key, isSensitive(key, sensitivePatterns) ? MASK : value);
        }
        return result;
    }

    // ==================== DumpToString Methods | 导出为字符串方法 ====================

    /**
     * Dump all configuration as a formatted string with default sensitive patterns
     * 使用默认敏感模式将所有配置导出为格式化字符串
     *
     * <p>Each line contains "key=value", sorted alphabetically by key.</p>
     * <p>每行包含"key=value"，按键的字母顺序排序。</p>
     *
     * @param config configuration instance | 配置实例
     * @return formatted string representation | 格式化字符串表示
     * @throws NullPointerException if config is null | 如果config为null
     */
    public static String dumpToString(Config config) {
        Objects.requireNonNull(config, "config must not be null");
        return dumpToString(config, DEFAULT_SENSITIVE_PATTERNS);
    }

    /**
     * Dump all configuration as a formatted string with custom sensitive patterns
     * 使用自定义敏感模式将所有配置导出为格式化字符串
     *
     * <p>Each line contains "key=value", sorted alphabetically by key.</p>
     * <p>每行包含"key=value"，按键的字母顺序排序。</p>
     *
     * @param config            configuration instance | 配置实例
     * @param sensitivePatterns set of sensitive key patterns | 敏感键模式集合
     * @return formatted string representation | 格式化字符串表示
     * @throws NullPointerException if config or sensitivePatterns is null | 如果config或sensitivePatterns为null
     */
    public static String dumpToString(Config config, Set<String> sensitivePatterns) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(sensitivePatterns, "sensitivePatterns must not be null");

        Map<String, String> entries = dump(config, sensitivePatterns);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            if (!first) {
                sb.append('\n');
            }
            first = false;
            sb.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return sb.toString();
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Check if a key matches any sensitive pattern
     * 检查键是否匹配任何敏感模式
     *
     * <p>Splits the key by "." into segments and checks if any segment contains
     * (case-insensitive) any pattern from the sensitive patterns set. Also checks
     * the full key.</p>
     * <p>按"."将键拆分为段，检查是否有任何段包含（不区分大小写）敏感模式集合中的任何模式。
     * 同时检查完整键。</p>
     *
     * @param key               configuration key | 配置键
     * @param sensitivePatterns set of sensitive key patterns | 敏感键模式集合
     * @return true if the key matches any sensitive pattern | 如果键匹配任何敏感模式返回true
     * @throws NullPointerException if key or sensitivePatterns is null | 如果key或sensitivePatterns为null
     */
    public static boolean isSensitive(String key, Set<String> sensitivePatterns) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(sensitivePatterns, "sensitivePatterns must not be null");

        String[] segments = key.split("\\.");
        for (String segment : segments) {
            String lowerSegment = segment.toLowerCase(java.util.Locale.ROOT);
            for (String pattern : sensitivePatterns) {
                if (lowerSegment.contains(pattern.toLowerCase(java.util.Locale.ROOT))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Mask a value
     * 掩码一个值
     *
     * <p>Returns the mask string "***" regardless of the input value.</p>
     * <p>无论输入值是什么，都返回掩码字符串"***"。</p>
     *
     * @param value the value to mask | 要掩码的值
     * @return masked value "***" | 掩码值"***"
     */
    public static String mask(String value) {
        return MASK;
    }
}
