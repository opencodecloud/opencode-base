package cloud.opencode.base.config.advanced;

import cloud.opencode.base.config.converter.ConfigConverter;

/**
 * Configuration Converter Provider SPI
 * 配置转换器提供者SPI
 *
 * <p>Service Provider Interface for custom type converters.</p>
 * <p>用于自定义类型转换器的服务提供者接口。</p>
 *
 * <p><strong>SPI Registration | SPI注册:</strong></p>
 * <pre>
 * # META-INF/services/cloud.opencode.base.config.advanced.ConfigConverterProvider
 * com.example.InetAddressConverterProvider
 * </pre>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class InetAddressConverterProvider implements ConfigConverterProvider {
 *     @Override
 *     public Class<?> supportedType() {
 *         return InetAddress.class;
 *     }
 *
 *     @Override
 *     public ConfigConverter<?> create() {
 *         return value -> InetAddress.getByName(value);
 *     }
 * }
 * }</pre>
 *
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core ConfigConverterProvider functionality - ConfigConverterProvider核心功能</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public interface ConfigConverterProvider {

    /**
     * Get the type this converter supports
     * 获取此转换器支持的类型
     *
     * @return supported type class | 支持的类型类
     */
    Class<?> supportedType();

    /**
     * Create the converter instance
     * 创建转换器实例
     *
     * @return converter instance | 转换器实例
     */
    ConfigConverter<?> create();

    /**
     * Get provider priority (higher = used first)
     * 获取提供者优先级(越高越先使用)
     *
     * @return priority value | 优先级值
     */
    default int priority() {
        return 0;
    }
}
