
package cloud.opencode.base.serialization.json;

import cloud.opencode.base.serialization.Serializer;
import cloud.opencode.base.serialization.spi.SerializerProvider;

/**
 * JsonSerializerProvider - SPI Provider for JSON Serializer
 * JSON 序列化器 SPI 提供者
 *
 * <p>Provides JsonSerializer instances through the SPI mechanism.
 * This is the default serializer with highest priority.</p>
 * <p>通过 SPI 机制提供 JsonSerializer 实例。
 * 这是具有最高优先级的默认序列化器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SPI provider for JSON serialization - JSON序列化的SPI提供者</li>
 *   <li>Auto-discovery via ServiceLoader - 通过ServiceLoader自动发现</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Registered via META-INF/services
 * // 通过META-INF/services注册
 * Serializer serializer = SerializerProvider.load("json");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for provider creation - 提供者创建 O(1)</li>
 *   <li>Space complexity: O(1) - O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
public class JsonSerializerProvider implements SerializerProvider {

    @Override
    public Serializer create() {
        return new JsonSerializer();
    }

    @Override
    public int getPriority() {
        return 10; // Highest priority - default serializer
    }
}
