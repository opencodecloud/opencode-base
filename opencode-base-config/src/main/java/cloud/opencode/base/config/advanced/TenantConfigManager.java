package cloud.opencode.base.config.advanced;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.OpenConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Multi-Tenant Configuration Manager
 * 多租户配置管理器
 *
 * <p>Manages configuration for multiple tenants with inheritance from base configuration.
 * Each tenant can have its own configuration that overrides the base configuration.</p>
 * <p>管理多租户配置，支持从基础配置继承。每个租户可以有自己的配置覆盖基础配置。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Tenant-specific configuration - 租户特定配置</li>
 *   <li>Base configuration inheritance - 基础配置继承</li>
 *   <li>Lazy loading per tenant - 每个租户的懒加载</li>
 *   <li>Thread-safe tenant cache - 线程安全的租户缓存</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TenantConfigManager manager = new TenantConfigManager(OpenConfig.getGlobal());
 *
 * // Get tenant-specific configuration
 * Config tenantConfig = manager.getConfig("tenant-123");
 * String apiKey = tenantConfig.getString("api.key");
 *
 * // Or directly get typed value
 * String apiKey = manager.get("tenant-123", "api.key", String.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Tenant isolation: Configuration level - 租户隔离: 配置级别</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class TenantConfigManager {

    /**
     * Pattern for valid tenant IDs. Only allows alphanumeric characters, hyphens, and underscores.
     * 有效租户ID模式。仅允许字母数字字符、连字符和下划线。
     */
    private static final Pattern VALID_TENANT_ID = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private final Config baseConfig;
    private final Map<String, Config> tenantConfigs = new ConcurrentHashMap<>();

    /**
     * Create tenant config manager with base configuration
     * 使用基础配置创建租户配置管理器
     *
     * @param baseConfig base configuration | 基础配置
     */
    public TenantConfigManager(Config baseConfig) {
        this.baseConfig = baseConfig;
    }

    /**
     * Get configuration for tenant
     * 获取租户的配置
     *
     * <p>Loads tenant-specific configuration from classpath and merges with base configuration.</p>
     * <p>从类路径加载租户特定配置并与基础配置合并。</p>
     *
     * @param tenantId tenant identifier | 租户标识符
     * @return tenant configuration | 租户配置
     */
    public Config getConfig(String tenantId) {
        validateTenantId(tenantId);
        return tenantConfigs.computeIfAbsent(tenantId, id -> {
            String tenantConfigPath = "tenants/" + id + "/config.properties";
            // Get all base config properties
            Map<String, String> baseProperties = baseConfig.getByPrefix("");
            return OpenConfig.builder()
                .addProperties(baseProperties)  // Base config with lower priority
                .addClasspathResource(tenantConfigPath)  // Tenant config overrides
                .build();
        });
    }

    /**
     * Get typed configuration value for tenant
     * 获取租户的类型化配置值
     *
     * @param <T> value type | 值类型
     * @param tenantId tenant identifier | 租户标识符
     * @param key configuration key | 配置键
     * @param type value type class | 值类型类
     * @return configuration value | 配置值
     */
    public <T> T get(String tenantId, String key, Class<T> type) {
        // getConfig already validates tenantId
        return getConfig(tenantId).get(key, type);
    }

    /**
     * Clear cached configuration for tenant
     * 清除租户的缓存配置
     *
     * @param tenantId tenant identifier | 租户标识符
     */
    public void clearCache(String tenantId) {
        validateTenantId(tenantId);
        tenantConfigs.remove(tenantId);
    }

    /**
     * Clear all cached configurations
     * 清除所有缓存配置
     */
    public void clearAllCaches() {
        tenantConfigs.clear();
    }

    /**
     * Validates a tenant ID to prevent path traversal attacks.
     * 验证租户ID以防止路径遍历攻击。
     *
     * <p>Rejects tenant IDs containing "..", "/", "\\" or any characters
     * outside the safe set [a-zA-Z0-9_-].</p>
     * <p>拒绝包含 ".."、"/"、"\\" 或安全字符集 [a-zA-Z0-9_-] 之外任何字符的租户ID。</p>
     *
     * @param tenantId the tenant identifier to validate | 要验证的租户标识符
     * @throws IllegalArgumentException if the tenant ID is invalid | 如果租户ID无效
     */
    private static void validateTenantId(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            throw new IllegalArgumentException("Tenant ID must not be null or empty");
        }
        if (!VALID_TENANT_ID.matcher(tenantId).matches()) {
            throw new IllegalArgumentException(
                "Invalid tenant ID: '" + tenantId + "'. " +
                "Only alphanumeric characters, hyphens, and underscores are allowed.");
        }
    }
}
