package cloud.opencode.base.sms.provider;

import cloud.opencode.base.sms.config.SmsConfig;
import cloud.opencode.base.sms.config.SmsProviderType;
import cloud.opencode.base.sms.exception.SmsErrorCode;
import cloud.opencode.base.sms.exception.SmsException;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * SMS Provider Factory
 * 短信提供商工厂
 *
 * <p>Factory for creating SMS providers.</p>
 * <p>创建短信提供商的工厂。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Config-based provider creation - 基于配置的提供商创建</li>
 *   <li>Custom provider registration - 自定义提供商注册</li>
 *   <li>SPI (ServiceLoader) discovery fallback - SPI服务发现回退</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SmsProvider provider = SmsProviderFactory.create(config);
 * SmsProvider console = SmsProviderFactory.console();
 * SmsProviderFactory.registerProvider(SmsProviderType.CUSTOM, MyProvider::new);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap for provider registry) - 线程安全: 是（ConcurrentHashMap注册表）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public final class SmsProviderFactory {

    private static final Map<SmsProviderType, Function<SmsConfig, SmsProvider>> PROVIDERS = new ConcurrentHashMap<>();

    static {
        // Register built-in providers
        registerProvider(SmsProviderType.CONSOLE, ConsoleSmsProvider::new);
    }

    private SmsProviderFactory() {
        // Utility class
    }

    /**
     * Create provider from config
     * 从配置创建提供商
     *
     * @param config the config | 配置
     * @return the provider | 提供商
     */
    public static SmsProvider create(SmsConfig config) {
        if (config == null || config.providerType() == null) {
            throw new SmsException(SmsErrorCode.PROVIDER_NOT_CONFIGURED);
        }

        Function<SmsConfig, SmsProvider> factory = PROVIDERS.get(config.providerType());
        if (factory != null) {
            return factory.apply(config);
        }

        // Try SPI
        SmsProvider spiProvider = loadFromSpi(config);
        if (spiProvider != null) {
            return spiProvider;
        }

        throw new SmsException(SmsErrorCode.PROVIDER_NOT_CONFIGURED,
            "No provider found for type: " + config.providerType());
    }

    /**
     * Register custom provider factory
     * 注册自定义提供商工厂
     *
     * @param type the provider type | 提供商类型
     * @param factory the factory function | 工厂函数
     */
    public static void registerProvider(SmsProviderType type, Function<SmsConfig, SmsProvider> factory) {
        PROVIDERS.put(type, factory);
    }

    /**
     * Unregister provider factory
     * 取消注册提供商工厂
     *
     * @param type the provider type | 提供商类型
     */
    public static void unregisterProvider(SmsProviderType type) {
        PROVIDERS.remove(type);
    }

    /**
     * Get console provider
     * 获取控制台提供商
     *
     * @return the console provider | 控制台提供商
     */
    public static SmsProvider console() {
        return new ConsoleSmsProvider();
    }

    private static SmsProvider loadFromSpi(SmsConfig config) {
        ServiceLoader<SmsProviderFactory.SpiProvider> loader = ServiceLoader.load(SpiProvider.class);
        for (SpiProvider spi : loader) {
            if (spi.supports(config.providerType())) {
                return spi.create(config);
            }
        }
        return null;
    }

    /**
     * SPI interface for custom providers
     * 自定义提供商的SPI接口
     */
    public interface SpiProvider {
        boolean supports(SmsProviderType type);
        SmsProvider create(SmsConfig config);
    }
}
