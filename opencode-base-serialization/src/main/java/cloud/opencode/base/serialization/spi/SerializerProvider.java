
package cloud.opencode.base.serialization.spi;

import cloud.opencode.base.serialization.Serializer;

/**
 * Serializer Provider - SPI interface for serializer discovery
 * 序列化器提供者 - 用于序列化器发现的 SPI 接口
 *
 * <p>This interface is used by the SPI mechanism to discover and register serializers automatically.
 * Implementations should be registered in META-INF/services/cloud.opencode.base.serialization.spi.SerializerProvider.</p>
 * <p>此接口用于 SPI 机制自动发现和注册序列化器。
 * 实现类应在 META-INF/services/cloud.opencode.base.serialization.spi.SerializerProvider 中注册。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Create serializer instances - 创建序列化器实例</li>
 *   <li>Priority-based ordering - 基于优先级的排序</li>
 *   <li>Automatic discovery via ServiceLoader - 通过 ServiceLoader 自动发现</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class JsonSerializerProvider implements SerializerProvider {
 *     @Override
 *     public Serializer create() {
 *         return new JsonSerializer();
 *     }
 *
 *     @Override
 *     public int getPriority() {
 *         return 10; // Higher priority (lower number)
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
public interface SerializerProvider {

    /**
     * Creates a new serializer instance.
     * 创建新的序列化器实例。
     *
     * @return the serializer instance - 序列化器实例
     */
    Serializer create();

    /**
     * Returns the priority of this provider.
     * 返回此提供者的优先级。
     *
     * <p>Lower values indicate higher priority. When multiple providers
     * are available, the one with the lowest priority value becomes the default.</p>
     * <p>较小的值表示较高的优先级。当有多个提供者可用时，
     * 具有最小优先级值的提供者成为默认值。</p>
     *
     * @return the priority (default is 100) - 优先级（默认为 100）
     */
    default int getPriority() {
        return 100;
    }

    /**
     * Returns whether this provider is available.
     * 返回此提供者是否可用。
     *
     * <p>Providers may check for required dependencies and return false
     * if they are not available.</p>
     * <p>提供者可以检查所需依赖项，如果不可用则返回 false。</p>
     *
     * @return true if available - 如果可用则返回 true
     */
    default boolean isAvailable() {
        return true;
    }
}
