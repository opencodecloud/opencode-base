package cloud.opencode.base.log.spi;

import cloud.opencode.base.log.exception.OpenLogException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Log Provider Factory - Provider Discovery and Management
 * 日志提供者工厂 - 提供者发现和管理
 *
 * <p>This class manages the discovery and selection of log providers using
 * the Java ServiceLoader mechanism.</p>
 * <p>此类使用 Java ServiceLoader 机制管理日志提供者的发现和选择。</p>
 *
 * <p><strong>Provider Selection | 提供者选择:</strong></p>
 * <ol>
 *   <li>Manually set provider (highest priority) - 手动设置的提供者（最高优先级）</li>
 *   <li>ServiceLoader discovered provider (by priority) - ServiceLoader 发现的提供者（按优先级）</li>
 *   <li>Default fallback provider - 默认回退提供者</li>
 * </ol>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ServiceLoader-based provider discovery - 基于 ServiceLoader 的提供者发现</li>
 *   <li>Priority-based provider selection - 基于优先级的提供者选择</li>
 *   <li>Manual provider override - 手动提供者覆盖</li>
 *   <li>Automatic fallback to console provider - 自动回退到控制台提供者</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get current provider
 * LogProvider provider = LogProviderFactory.getProvider();
 * 
 * // Set custom provider
 * LogProviderFactory.setProvider(new MyCustomProvider());
 * 
 * // Check available providers
 * List<String> providers = LogProviderFactory.getAvailableProviders();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (synchronized init + AtomicReference) - 线程安全: 是（同步初始化 + AtomicReference）</li>
 *   <li>Null-safe: No (throws on null provider) - 空值安全: 否（null 提供者抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public final class LogProviderFactory {

    private static final AtomicReference<LogProvider> CURRENT_PROVIDER = new AtomicReference<>();
    private static final Map<String, LogProvider> PROVIDERS = new ConcurrentHashMap<>();
    private static volatile boolean initialized = false;

    private LogProviderFactory() {
        // Utility class
    }

    /**
     * Returns the current log provider.
     * 返回当前日志提供者。
     *
     * @return the current provider - 当前提供者
     */
    public static LogProvider getProvider() {
        if (!initialized) {
            synchronized (LogProviderFactory.class) {
                if (!initialized) {
                    initialize();
                }
            }
        }
        LogProvider provider = CURRENT_PROVIDER.get();
        if (provider == null) {
            throw OpenLogException.providerNotFound();
        }
        return provider;
    }

    /**
     * Returns a provider by name.
     * 按名称返回提供者。
     *
     * @param name the provider name - 提供者名称
     * @return the provider - 提供者
     * @throws OpenLogException if provider not found - 如果提供者未找到
     */
    public static LogProvider getProvider(String name) {
        if (!initialized) {
            synchronized (LogProviderFactory.class) {
                if (!initialized) {
                    initialize();
                }
            }
        }
        LogProvider provider = PROVIDERS.get(name.toLowerCase());
        if (provider == null) {
            throw new OpenLogException("Log provider not found: " + name);
        }
        return provider;
    }

    /**
     * Sets the current log provider.
     * 设置当前日志提供者。
     *
     * @param provider the provider to set - 要设置的提供者
     */
    public static void setProvider(LogProvider provider) {
        Objects.requireNonNull(provider, "Provider must not be null");
        CURRENT_PROVIDER.set(provider);
        PROVIDERS.put(provider.getName().toLowerCase(), provider);
    }

    /**
     * Registers a log provider.
     * 注册日志提供者。
     *
     * @param provider the provider to register - 要注册的提供者
     */
    public static void registerProvider(LogProvider provider) {
        Objects.requireNonNull(provider, "Provider must not be null");
        PROVIDERS.put(provider.getName().toLowerCase(), provider);
    }

    /**
     * Returns a list of available provider names.
     * 返回可用提供者名称列表。
     *
     * @return list of provider names - 提供者名称列表
     */
    public static List<String> getAvailableProviders() {
        if (!initialized) {
            synchronized (LogProviderFactory.class) {
                if (!initialized) {
                    initialize();
                }
            }
        }
        return new ArrayList<>(PROVIDERS.keySet());
    }

    /**
     * Checks if a provider is available.
     * 检查提供者是否可用。
     *
     * @param name the provider name - 提供者名称
     * @return true if available - 如果可用返回 true
     */
    public static boolean hasProvider(String name) {
        if (!initialized) {
            synchronized (LogProviderFactory.class) {
                if (!initialized) {
                    initialize();
                }
            }
        }
        return PROVIDERS.containsKey(name.toLowerCase());
    }

    /**
     * Initializes the provider factory.
     * 初始化提供者工厂。
     */
    private static void initialize() {
        // Discover providers via ServiceLoader
        ServiceLoader<LogProvider> loader = ServiceLoader.load(LogProvider.class);
        List<LogProvider> availableProviders = new ArrayList<>();

        for (LogProvider provider : loader) {
            try {
                if (provider.isAvailable()) {
                    provider.initialize();
                    PROVIDERS.put(provider.getName().toLowerCase(), provider);
                    availableProviders.add(provider);
                }
            } catch (Exception e) {
                // Skip providers that fail to initialize
            }
        }

        // Select provider with lowest priority (highest precedence)
        if (!availableProviders.isEmpty()) {
            availableProviders.sort(Comparator.comparingInt(LogProvider::getPriority));
            CURRENT_PROVIDER.set(availableProviders.getFirst());
        } else {
            // Use default fallback provider
            LogProvider fallback = new DefaultLogProvider();
            PROVIDERS.put(fallback.getName().toLowerCase(), fallback);
            CURRENT_PROVIDER.set(fallback);
        }

        initialized = true;

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(LogProviderFactory::shutdown));
    }

    /**
     * Shuts down all providers.
     * 关闭所有提供者。
     */
    private static void shutdown() {
        for (LogProvider provider : PROVIDERS.values()) {
            try {
                provider.shutdown();
            } catch (Exception e) {
                // Ignore shutdown errors
            }
        }
    }

    /**
     * Resets the factory (for testing).
     * 重置工厂（用于测试）。
     */
    static void reset() {
        PROVIDERS.clear();
        CURRENT_PROVIDER.set(null);
        initialized = false;
    }
}
