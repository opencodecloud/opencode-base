package cloud.opencode.base.oauth2.provider;

import cloud.opencode.base.oauth2.exception.OAuth2ErrorCode;
import cloud.opencode.base.oauth2.exception.OAuth2Exception;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OAuth2 Provider Registry
 * OAuth2 Provider 注册表
 *
 * <p>Registry for managing OAuth2 providers.</p>
 * <p>用于管理 OAuth2 提供者的注册表。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Provider registration - Provider 注册</li>
 *   <li>Provider lookup - Provider 查找</li>
 *   <li>Built-in providers - 内置 Provider</li>
 *   <li>Custom provider support - 自定义 Provider 支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get the global registry
 * ProviderRegistry registry = ProviderRegistry.global();
 *
 * // Register a custom provider
 * registry.register(new CustomProvider.Builder()
 *     .name("MyProvider")
 *     .authorizationEndpoint("https://auth.example.com/authorize")
 *     .tokenEndpoint("https://auth.example.com/token")
 *     .build());
 *
 * // Get a provider
 * OAuth2Provider provider = registry.get("Google");
 *
 * // List all providers
 * Set<String> names = registry.names();
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This class is thread-safe.</p>
 * <p>此类是线程安全的。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public class ProviderRegistry {

    private static final ProviderRegistry GLOBAL = new ProviderRegistry();

    static {
        // Register built-in providers
        GLOBAL.register(Providers.GOOGLE);
        GLOBAL.register(Providers.MICROSOFT);
        GLOBAL.register(Providers.GITHUB);
        GLOBAL.register(Providers.APPLE);
        GLOBAL.register(Providers.FACEBOOK);
    }

    private final ConcurrentHashMap<String, OAuth2Provider> providers;

    /**
     * Create a new empty registry
     * 创建新的空注册表
     */
    public ProviderRegistry() {
        this.providers = new ConcurrentHashMap<>();
    }

    /**
     * Get the global registry with built-in providers
     * 获取带有内置提供者的全局注册表
     *
     * @return the global registry | 全局注册表
     */
    public static ProviderRegistry global() {
        return GLOBAL;
    }

    /**
     * Create a new registry with built-in providers
     * 创建带有内置提供者的新注册表
     *
     * @return the new registry | 新注册表
     */
    public static ProviderRegistry withBuiltins() {
        ProviderRegistry registry = new ProviderRegistry();
        registry.register(Providers.GOOGLE);
        registry.register(Providers.MICROSOFT);
        registry.register(Providers.GITHUB);
        registry.register(Providers.APPLE);
        registry.register(Providers.FACEBOOK);
        return registry;
    }

    /**
     * Register a provider
     * 注册 Provider
     *
     * @param provider the provider to register | 要注册的 Provider
     * @return this registry | 此注册表
     */
    public ProviderRegistry register(OAuth2Provider provider) {
        Objects.requireNonNull(provider, "provider cannot be null");
        Objects.requireNonNull(provider.name(), "provider name cannot be null");
        providers.put(provider.name().toLowerCase(Locale.ROOT), provider);
        return this;
    }

    /**
     * Register a provider with a custom name
     * 使用自定义名称注册 Provider
     *
     * @param name     the name to register under | 注册的名称
     * @param provider the provider to register | 要注册的 Provider
     * @return this registry | 此注册表
     */
    public ProviderRegistry register(String name, OAuth2Provider provider) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(provider, "provider cannot be null");
        providers.put(name.toLowerCase(Locale.ROOT), provider);
        return this;
    }

    /**
     * Get a provider by name
     * 按名称获取 Provider
     *
     * @param name the provider name (case-insensitive) | Provider 名称（不区分大小写）
     * @return the provider | Provider
     * @throws OAuth2Exception if provider not found | 如果 Provider 未找到
     */
    public OAuth2Provider get(String name) {
        return find(name).orElseThrow(() ->
                new OAuth2Exception(OAuth2ErrorCode.PROVIDER_NOT_FOUND, "Provider not found: " + name));
    }

    /**
     * Find a provider by name
     * 按名称查找 Provider
     *
     * @param name the provider name (case-insensitive) | Provider 名称（不区分大小写）
     * @return the provider if found | 找到的 Provider
     */
    public Optional<OAuth2Provider> find(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(providers.get(name.toLowerCase(Locale.ROOT)));
    }

    /**
     * Check if a provider is registered
     * 检查 Provider 是否已注册
     *
     * @param name the provider name | Provider 名称
     * @return true if registered | 如果已注册返回 true
     */
    public boolean contains(String name) {
        return name != null && providers.containsKey(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Remove a provider
     * 移除 Provider
     *
     * @param name the provider name | Provider 名称
     * @return the removed provider if found | 移除的 Provider
     */
    public Optional<OAuth2Provider> remove(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(providers.remove(name.toLowerCase(Locale.ROOT)));
    }

    /**
     * Get all registered provider names
     * 获取所有已注册的 Provider 名称
     *
     * @return the provider names | Provider 名称
     */
    public Set<String> names() {
        return Set.copyOf(providers.keySet());
    }

    /**
     * Get all registered providers
     * 获取所有已注册的 Provider
     *
     * @return the providers | Provider 列表
     */
    public Collection<OAuth2Provider> all() {
        return List.copyOf(providers.values());
    }

    /**
     * Get the number of registered providers
     * 获取已注册的 Provider 数量
     *
     * @return the count | 数量
     */
    public int size() {
        return providers.size();
    }

    /**
     * Check if the registry is empty
     * 检查注册表是否为空
     *
     * @return true if empty | 如果为空返回 true
     */
    public boolean isEmpty() {
        return providers.isEmpty();
    }

    /**
     * Clear all providers
     * 清除所有 Provider
     */
    public void clear() {
        providers.clear();
    }
}
