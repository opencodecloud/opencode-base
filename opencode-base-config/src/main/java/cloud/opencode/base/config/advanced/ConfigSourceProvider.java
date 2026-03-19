package cloud.opencode.base.config.advanced;

import cloud.opencode.base.config.source.ConfigSource;
import java.util.Map;

/**
 * Configuration Source Provider SPI
 * 配置源提供者SPI
 *
 * <p>Service Provider Interface for custom configuration sources.</p>
 * <p>用于自定义配置源的服务提供者接口。</p>
 *
 * <p><strong>SPI Registration | SPI注册:</strong></p>
 * <pre>
 * # META-INF/services/cloud.opencode.base.config.advanced.ConfigSourceProvider
 * com.example.RedisConfigSourceProvider
 * com.example.ConsulConfigSourceProvider
 * </pre>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class RedisConfigSourceProvider implements ConfigSourceProvider {
 *     @Override
 *     public boolean supports(String uri) {
 *         return uri.startsWith("redis://");
 *     }
 *
 *     @Override
 *     public ConfigSource create(String uri, Map<String, Object> options) {
 *         return new RedisConfigSource(uri, options);
 *     }
 * }
 * }</pre>
 *
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core ConfigSourceProvider functionality - ConfigSourceProvider核心功能</li>
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
public interface ConfigSourceProvider {

    /**
     * Check if this provider supports the given URI
     * 检查此提供者是否支持给定的URI
     *
     * @param uri configuration source URI | 配置源URI
     * @return true if supported | 如果支持返回true
     */
    boolean supports(String uri);

    /**
     * Create configuration source for the given URI
     * 为给定的URI创建配置源
     *
     * @param uri configuration source URI | 配置源URI
     * @param options additional options | 附加选项
     * @return configuration source | 配置源
     */
    ConfigSource create(String uri, Map<String, Object> options);

    /**
     * Get provider priority (higher = checked first)
     * 获取提供者优先级(越高越先检查)
     *
     * @return priority value | 优先级值
     */
    default int priority() {
        return 0;
    }
}
