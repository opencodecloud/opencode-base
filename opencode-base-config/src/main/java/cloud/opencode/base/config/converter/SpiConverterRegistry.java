package cloud.opencode.base.config.converter;

import cloud.opencode.base.config.advanced.ConfigConverterProvider;
import java.util.*;

/**
 * SPI-based Converter Registry
 * 基于SPI的转换器注册表
 *
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core SpiConverterRegistry functionality - SpiConverterRegistry核心功能</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // See class-level documentation for usage
 * // 参见类级文档了解用法
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for SPI loading where n = providers - SPI加载 O(n), n为提供者数量</li>
 *   <li>Space complexity: O(n) for registered converters - 已注册转换器 O(n)</li>
 * </ul>
 *
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class SpiConverterRegistry extends ConverterRegistry {

    public static ConverterRegistry loadFromSpi() {
        ConverterRegistry registry = ConverterRegistry.defaults();

        ServiceLoader.load(ConfigConverterProvider.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .sorted(Comparator.comparingInt(ConfigConverterProvider::priority).reversed())
            .forEach(provider -> {
                @SuppressWarnings("unchecked")
                ConfigConverter<Object> converter = (ConfigConverter<Object>) provider.create();
                registry.register((Class<Object>) provider.supportedType(), converter);
            });

        return registry;
    }
}
