package cloud.opencode.base.yml.spi;

import cloud.opencode.base.yml.exception.OpenYmlException;

import java.util.*;

/**
 * YAML Provider Factory - Factory for loading YAML providers via SPI
 * YAML 提供者工厂 - 通过 SPI 加载 YAML 提供者的工厂
 *
 * <p>This factory uses Java ServiceLoader to discover and load YAML providers.</p>
 * <p>此工厂使用 Java ServiceLoader 来发现和加载 YAML 提供者。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Auto-discovery of YAML providers via Java ServiceLoader - 通过 Java ServiceLoader 自动发现 YAML 提供者</li>
 *   <li>Priority-based provider selection - 基于优先级的提供者选择</li>
 *   <li>Named provider lookup - 命名提供者查找</li>
 *   <li>Default provider override support - 默认提供者覆盖支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get default provider (highest priority)
 * YmlProvider provider = YmlProviderFactory.getProvider();
 *
 * // Get provider by name
 * YmlProvider snake = YmlProviderFactory.getProvider("snakeyaml");
 *
 * // List all available providers
 * List<YmlProvider> all = YmlProviderFactory.getAvailableProviders();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses volatile + double-checked locking) - 线程安全: 是（使用 volatile + 双重检查锁定）</li>
 *   <li>Null-safe: No (throws if no provider found) - 空值安全: 否（无提供者时抛出异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public final class YmlProviderFactory {

    private static volatile YmlProvider defaultProvider;
    private static final Object LOCK = new Object();

    private YmlProviderFactory() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Gets the default YAML provider.
     * 获取默认 YAML 提供者。
     *
     * <p>The provider is selected by priority (highest first), then by availability.</p>
     * <p>提供者按优先级选择（最高优先），然后按可用性选择。</p>
     *
     * @return the default provider | 默认提供者
     * @throws OpenYmlException if no provider is available | 如果没有可用的提供者
     */
    public static YmlProvider getProvider() {
        if (defaultProvider == null) {
            synchronized (LOCK) {
                if (defaultProvider == null) {
                    defaultProvider = loadDefaultProvider();
                }
            }
        }
        return defaultProvider;
    }

    /**
     * Gets a provider by name.
     * 按名称获取提供者。
     *
     * @param name the provider name | 提供者名称
     * @return the provider | 提供者
     * @throws OpenYmlException if provider is not found | 如果未找到提供者
     */
    public static YmlProvider getProvider(String name) {
        for (YmlProvider provider : loadProviders()) {
            if (provider.getName().equalsIgnoreCase(name) && provider.isAvailable()) {
                return provider;
            }
        }
        throw new OpenYmlException("No YAML provider found with name: " + name);
    }

    /**
     * Gets all available providers.
     * 获取所有可用的提供者。
     *
     * @return list of providers sorted by priority | 按优先级排序的提供者列表
     */
    public static List<YmlProvider> getAvailableProviders() {
        List<YmlProvider> available = new ArrayList<>();
        for (YmlProvider provider : loadProviders()) {
            if (provider.isAvailable()) {
                available.add(provider);
            }
        }
        available.sort(Comparator.comparingInt(YmlProvider::getPriority).reversed());
        return Collections.unmodifiableList(available);
    }

    /**
     * Sets the default provider.
     * 设置默认提供者。
     *
     * @param provider the provider to set as default | 要设置为默认的提供者
     */
    public static void setDefaultProvider(YmlProvider provider) {
        synchronized (LOCK) {
            defaultProvider = provider;
        }
    }

    /**
     * Resets the default provider (forces re-discovery).
     * 重置默认提供者（强制重新发现）。
     */
    public static void reset() {
        synchronized (LOCK) {
            defaultProvider = null;
        }
    }

    /**
     * Checks if any provider is available.
     * 检查是否有可用的提供者。
     *
     * @return true if at least one provider is available | 如果至少有一个提供者可用则返回 true
     */
    public static boolean hasProvider() {
        for (YmlProvider provider : loadProviders()) {
            if (provider.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    private static YmlProvider loadDefaultProvider() {
        List<YmlProvider> providers = getAvailableProviders();
        if (providers.isEmpty()) {
            throw new OpenYmlException(
                "No YAML provider found. Please add a provider implementation to classpath, " +
                "e.g., org.yaml:snakeyaml");
        }
        return providers.getFirst();
    }

    private static Iterable<YmlProvider> loadProviders() {
        return ServiceLoader.load(YmlProvider.class);
    }
}
