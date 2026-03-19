
package cloud.opencode.base.serialization.binary;

import cloud.opencode.base.serialization.Serializer;
import cloud.opencode.base.serialization.spi.SerializerProvider;

/**
 * KryoSerializerProvider - SPI Provider for Kryo Serializer
 * Kryo 序列化器 SPI 提供者
 *
 * <p>Provides KryoSerializer instances through the SPI mechanism.
 * Only available when Kryo dependency is present.</p>
 * <p>通过 SPI 机制提供 KryoSerializer 实例。
 * 仅当 Kryo 依赖存在时可用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SPI provider for Kryo serialization - Kryo序列化的SPI提供者</li>
 *   <li>Auto-discovery via ServiceLoader - 通过ServiceLoader自动发现</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Registered via META-INF/services
 * // 通过META-INF/services注册
 * Serializer serializer = SerializerProvider.load("kryo");
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
public class KryoSerializerProvider implements SerializerProvider {

    @Override
    public Serializer create() {
        return new KryoSerializer();
    }

    @Override
    public int getPriority() {
        return 50; // Medium-high priority for binary serialization
    }

    @Override
    public boolean isAvailable() {
        try {
            Class.forName("com.esotericsoftware.kryo.Kryo");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
