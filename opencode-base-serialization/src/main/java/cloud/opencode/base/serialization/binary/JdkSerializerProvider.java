
package cloud.opencode.base.serialization.binary;

import cloud.opencode.base.serialization.Serializer;
import cloud.opencode.base.serialization.spi.SerializerProvider;

/**
 * JdkSerializerProvider - SPI Provider for JDK Serializer
 * JDK 序列化器 SPI 提供者
 *
 * <p>Provides JdkSerializer instances through the SPI mechanism.</p>
 * <p>通过 SPI 机制提供 JdkSerializer 实例。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SPI provider for JDK native serialization - JDK原生序列化的SPI提供者</li>
 *   <li>Auto-discovery via ServiceLoader - 通过ServiceLoader自动发现</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Registered via META-INF/services
 * // 通过META-INF/services注册
 * // ServiceLoader discovers this provider automatically
 * // ServiceLoader自动发现此提供者
 * Serializer serializer = SerializerProvider.load("jdk");
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
public class JdkSerializerProvider implements SerializerProvider {

    @Override
    public Serializer create() {
        return new JdkSerializer();
    }

    @Override
    public int getPriority() {
        return 1000; // Low priority - fallback serializer
    }
}
