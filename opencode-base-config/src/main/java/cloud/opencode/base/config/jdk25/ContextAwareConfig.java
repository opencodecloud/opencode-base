package cloud.opencode.base.config.jdk25;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.ConfigListener;

import java.time.Duration;
import java.util.*;

/**
 * Context-Aware Configuration
 * 上下文感知配置
 *
 * <p>Configuration wrapper that provides context-aware value resolution.
 * Supports tenant isolation and request-scoped configuration overrides
 * using JDK 25 ScopedValue.</p>
 * <p>提供上下文感知值解析的配置包装器。使用JDK 25 ScopedValue支持租户隔离和请求级配置覆盖。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Tenant-specific configuration - 租户特定配置</li>
 *   <li>Request-scoped overrides - 请求级别覆盖</li>
 *   <li>Transparent delegation - 透明委托</li>
 *   <li>ScopedValue integration - ScopedValue集成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Config baseConfig = OpenConfig.load();
 * ContextAwareConfig config = new ContextAwareConfig(baseConfig);
 *
 * // Multi-tenant access
 * ConfigContext.withTenant("tenant-1", () -> {
 *     // Gets tenants.tenant-1.db.url or falls back to db.url
 *     String dbUrl = config.getString("db.url");
 * });
 *
 * // Request-scoped overrides
 * ConfigContext.withOverrides(Map.of("log.level", "DEBUG"), () -> {
 *     String level = config.getString("log.level"); // -> "DEBUG"
 * });
 * }</pre>
 *
 * <p><strong>Resolution Order | 解析顺序:</strong></p>
 * <ol>
 *   <li>Request-scoped overrides - 请求级覆盖</li>
 *   <li>Tenant-specific configuration - 租户特定配置</li>
 *   <li>Base configuration - 基础配置</li>
 * </ol>
 *
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class ContextAwareConfig implements Config {

    private final Config delegate;

    public ContextAwareConfig(Config delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getString(String key) {
        Optional<Map<String, String>> overrides = ConfigContext.currentOverrides();
        if (overrides.isPresent() && overrides.get().containsKey(key)) {
            return overrides.get().get(key);
        }

        Optional<String> tenant = ConfigContext.currentTenant();
        if (tenant.isPresent()) {
            String tenantKey = "tenants." + tenant.get() + "." + key;
            String value = delegate.getString(tenantKey, null);
            if (value != null) return value;
        }

        return delegate.getString(key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        try {
            return getString(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // Delegate all other methods
    @Override public int getInt(String key) { return delegate.getInt(key); }
    @Override public int getInt(String key, int defaultValue) { return delegate.getInt(key, defaultValue); }
    @Override public long getLong(String key) { return delegate.getLong(key); }
    @Override public long getLong(String key, long defaultValue) { return delegate.getLong(key, defaultValue); }
    @Override public double getDouble(String key) { return delegate.getDouble(key); }
    @Override public double getDouble(String key, double defaultValue) { return delegate.getDouble(key, defaultValue); }
    @Override public boolean getBoolean(String key) { return delegate.getBoolean(key); }
    @Override public boolean getBoolean(String key, boolean defaultValue) { return delegate.getBoolean(key, defaultValue); }
    @Override public Duration getDuration(String key) { return delegate.getDuration(key); }
    @Override public Duration getDuration(String key, Duration defaultValue) { return delegate.getDuration(key, defaultValue); }
    @Override public <T> T get(String key, Class<T> type) { return delegate.get(key, type); }
    @Override public <T> T get(String key, Class<T> type, T defaultValue) { return delegate.get(key, type, defaultValue); }
    @Override public <T> List<T> getList(String key, Class<T> elementType) { return delegate.getList(key, elementType); }
    @Override public <K, V> Map<K, V> getMap(String key, Class<K> keyType, Class<V> valueType) { return delegate.getMap(key, keyType, valueType); }
    @Override public Optional<String> getOptional(String key) { return delegate.getOptional(key); }
    @Override public <T> Optional<T> getOptional(String key, Class<T> type) { return delegate.getOptional(key, type); }
    @Override public Config getSubConfig(String prefix) { return delegate.getSubConfig(prefix); }
    @Override public Map<String, String> getByPrefix(String prefix) { return delegate.getByPrefix(prefix); }
    @Override public boolean hasKey(String key) { return delegate.hasKey(key); }
    @Override public Set<String> getKeys() { return delegate.getKeys(); }
    @Override public void addListener(ConfigListener listener) { delegate.addListener(listener); }
    @Override public void addListener(String key, ConfigListener listener) { delegate.addListener(key, listener); }
    @Override public void removeListener(ConfigListener listener) { delegate.removeListener(listener); }
    @Override public <T> T bind(String prefix, Class<T> type) { return delegate.bind(prefix, type); }
    @Override public <T> void bindTo(String prefix, T target) { delegate.bindTo(prefix, target); }
}
