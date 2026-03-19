
package cloud.opencode.base.json.spi;

import cloud.opencode.base.json.JsonConfig;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSON Provider Factory - Factory for Creating and Managing JSON Providers
 * JSON 提供者工厂 - 用于创建和管理 JSON 提供者的工厂
 *
 * <p>This factory discovers and manages JsonProvider implementations via
 * ServiceLoader mechanism. It supports auto-detection of available providers
 * and allows explicit provider selection.</p>
 * <p>此工厂通过 ServiceLoader 机制发现和管理 JsonProvider 实现。
 * 它支持自动检测可用提供者并允许显式选择提供者。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Get default provider (auto-detect)
 * JsonProvider provider = JsonProviderFactory.getProvider();
 *
 * // Get specific provider by name
 * JsonProvider jackson = JsonProviderFactory.getProvider("jackson");
 *
 * // List available providers
 * List<String> providers = JsonProviderFactory.getAvailableProviders();
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ServiceLoader-based provider discovery - 基于ServiceLoader的提供者发现</li>
 *   <li>Priority-based default provider selection - 基于优先级的默认提供者选择</li>
 *   <li>Thread-safe provider registration and lookup - 线程安全的提供者注册和查找</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public final class JsonProviderFactory {

    /**
     * Registered providers cache
     * 注册的提供者缓存
     */
    private static final Map<String, JsonProvider> PROVIDERS = new ConcurrentHashMap<>();

    /**
     * Default provider (highest priority)
     * 默认提供者（最高优先级）
     */
    private static volatile JsonProvider defaultProvider;

    /**
     * Lock for thread-safe initialization
     * 线程安全初始化的锁
     */
    private static final Object INIT_LOCK = new Object();

    /**
     * Whether providers have been loaded
     * 提供者是否已加载
     */
    private static volatile boolean initialized = false;

    private JsonProviderFactory() {
        // Utility class - 工具类
    }

    /**
     * Returns the default JSON provider.
     * 返回默认的 JSON 提供者。
     *
     * <p>Provider selection priority:</p>
     * <ol>
     *   <li>Explicitly set default provider - 显式设置的默认提供者</li>
     *   <li>Provider with highest priority from ServiceLoader - ServiceLoader 中优先级最高的提供者</li>
     *   <li>First available provider - 第一个可用的提供者</li>
     * </ol>
     *
     * @return the default provider - 默认提供者
     * @throws OpenJsonProcessingException if no provider is available - 如果没有可用的提供者
     */
    public static JsonProvider getProvider() {
        ensureInitialized();
        if (defaultProvider == null) {
            throw OpenJsonProcessingException.configError(
                    "No JSON provider available. Add Jackson, Gson, or Fastjson2 to classpath.");
        }
        return defaultProvider;
    }

    /**
     * Returns a provider by name.
     * 按名称返回提供者。
     *
     * @param name the provider name (e.g., "jackson", "gson") - 提供者名称
     * @return the provider - 提供者
     * @throws OpenJsonProcessingException if provider not found - 如果找不到提供者
     */
    public static JsonProvider getProvider(String name) {
        Objects.requireNonNull(name, "Provider name must not be null");
        ensureInitialized();

        JsonProvider provider = PROVIDERS.get(name.toLowerCase(Locale.ROOT));
        if (provider == null) {
            throw OpenJsonProcessingException.configError(
                    "JSON provider '" + name + "' not found. Available: " + getAvailableProviders());
        }
        return provider;
    }

    /**
     * Returns a configured provider instance.
     * 返回配置的提供者实例。
     *
     * @param config the configuration - 配置
     * @return a configured provider - 配置后的提供者
     */
    public static JsonProvider getProvider(JsonConfig config) {
        Objects.requireNonNull(config, "Config must not be null");
        JsonProvider provider = getProvider().copy();
        provider.configure(config);
        return provider;
    }

    /**
     * Returns a configured provider instance by name.
     * 按名称返回配置的提供者实例。
     *
     * @param name   the provider name - 提供者名称
     * @param config the configuration - 配置
     * @return a configured provider - 配置后的提供者
     */
    public static JsonProvider getProvider(String name, JsonConfig config) {
        Objects.requireNonNull(config, "Config must not be null");
        JsonProvider provider = getProvider(name).copy();
        provider.configure(config);
        return provider;
    }

    /**
     * Returns a list of available provider names.
     * 返回可用提供者名称列表。
     *
     * @return list of provider names - 提供者名称列表
     */
    public static List<String> getAvailableProviders() {
        ensureInitialized();
        return new ArrayList<>(PROVIDERS.keySet());
    }

    /**
     * Returns all registered providers.
     * 返回所有注册的提供者。
     *
     * @return unmodifiable collection of providers - 不可变的提供者集合
     */
    public static Collection<JsonProvider> getAllProviders() {
        ensureInitialized();
        return Collections.unmodifiableCollection(PROVIDERS.values());
    }

    /**
     * Checks if a provider is available by name.
     * 检查指定名称的提供者是否可用。
     *
     * @param name the provider name - 提供者名称
     * @return true if available - 如果可用则返回 true
     */
    public static boolean hasProvider(String name) {
        ensureInitialized();
        return PROVIDERS.containsKey(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Checks if any provider is available.
     * 检查是否有任何提供者可用。
     *
     * @return true if at least one provider is available - 如果至少有一个提供者可用则返回 true
     */
    public static boolean hasAnyProvider() {
        ensureInitialized();
        return !PROVIDERS.isEmpty();
    }

    /**
     * Sets the default provider.
     * 设置默认提供者。
     *
     * <p>This explicitly sets the default provider, bypassing priority-based selection.
     * The provider will remain the default until another explicit call to setDefaultProvider.</p>
     * <p>这会显式设置默认提供者，绕过基于优先级的选择。
     * 该提供者将保持为默认，直到另一个显式调用 setDefaultProvider。</p>
     *
     * @param provider the provider to set as default - 要设置为默认的提供者
     * @throws NullPointerException if provider is null - 如果提供者为 null
     */
    public static void setDefaultProvider(JsonProvider provider) {
        Objects.requireNonNull(provider, "Provider must not be null");
        synchronized (INIT_LOCK) {
            String name = provider.getName().toLowerCase(Locale.ROOT);
            PROVIDERS.put(name, provider);
            defaultProvider = provider;
        }
    }

    /**
     * Sets the default provider by name.
     * 按名称设置默认提供者。
     *
     * @param name the provider name - 提供者名称
     * @throws OpenJsonProcessingException if provider not found - 如果找不到提供者
     */
    public static void setDefaultProvider(String name) {
        defaultProvider = getProvider(name);
    }

    /**
     * Registers a provider.
     * 注册提供者。
     *
     * @param provider the provider to register - 要注册的提供者
     */
    public static void registerProvider(JsonProvider provider) {
        Objects.requireNonNull(provider, "Provider must not be null");
        String name = provider.getName().toLowerCase(Locale.ROOT);

        synchronized (INIT_LOCK) {
            PROVIDERS.put(name, provider);
            // Update default if this has higher priority
            if (defaultProvider == null ||
                    (provider.isAvailable() && provider.getPriority() > defaultProvider.getPriority())) {
                defaultProvider = provider;
            }
        }
    }

    /**
     * Unregisters a provider by name.
     * 按名称注销提供者。
     *
     * @param name the provider name - 提供者名称
     * @return the removed provider, or null if not found - 移除的提供者，如果未找到则返回 null
     */
    public static JsonProvider unregisterProvider(String name) {
        Objects.requireNonNull(name, "Provider name must not be null");
        synchronized (INIT_LOCK) {
            JsonProvider removed = PROVIDERS.remove(name.toLowerCase(Locale.ROOT));
            if (removed == defaultProvider) {
                // Select new default
                defaultProvider = selectDefaultProvider();
            }
            return removed;
        }
    }

    /**
     * Reloads providers from ServiceLoader.
     * 从 ServiceLoader 重新加载提供者。
     */
    public static void reload() {
        synchronized (INIT_LOCK) {
            PROVIDERS.clear();
            defaultProvider = null;
            initialized = false;
            ensureInitialized();
        }
    }

    /**
     * Ensures providers are initialized.
     * 确保提供者已初始化。
     */
    private static void ensureInitialized() {
        if (!initialized) {
            synchronized (INIT_LOCK) {
                if (!initialized) {
                    loadProviders();
                    initialized = true;
                }
            }
        }
    }

    /**
     * Loads providers from ServiceLoader.
     * 从 ServiceLoader 加载提供者。
     */
    private static void loadProviders() {
        ServiceLoader<JsonProvider> loader = ServiceLoader.load(JsonProvider.class);

        for (JsonProvider provider : loader) {
            if (provider.isAvailable()) {
                String name = provider.getName().toLowerCase(Locale.ROOT);
                PROVIDERS.put(name, provider);
            }
        }

        defaultProvider = selectDefaultProvider();
    }

    /**
     * Selects the default provider based on priority.
     * 根据优先级选择默认提供者。
     *
     * @return the default provider, or null if none available - 默认提供者，如果没有可用则返回 null
     */
    private static JsonProvider selectDefaultProvider() {
        return PROVIDERS.values().stream()
                .filter(JsonProvider::isAvailable)
                .max(Comparator.comparingInt(JsonProvider::getPriority))
                .orElse(null);
    }

    /**
     * Provider information record.
     * 提供者信息记录。
     */
    public record ProviderInfo(
            /**
             * Provider name
             * 提供者名称
             */
            String name,

            /**
             * Provider version
             * 提供者版本
             */
            String version,

            /**
             * Priority (higher = preferred)
             * 优先级（越高越优先）
             */
            int priority,

            /**
             * Whether this is the default provider
             * 是否为默认提供者
             */
            boolean isDefault
    ) {}

    /**
     * Returns information about all registered providers.
     * 返回所有注册提供者的信息。
     *
     * @return list of provider info - 提供者信息列表
     */
    public static List<ProviderInfo> getProviderInfo() {
        ensureInitialized();
        return PROVIDERS.values().stream()
                .map(p -> new ProviderInfo(
                        p.getName(),
                        p.getVersion(),
                        p.getPriority(),
                        p == defaultProvider
                ))
                .sorted(Comparator.comparingInt(ProviderInfo::priority).reversed())
                .toList();
    }
}
