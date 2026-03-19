

/**
 * OpenCode Base Serialization - Unified Serialization Framework
 * OpenCode Base 序列化 - 统一序列化框架
 *
 * <p>This package provides a unified serialization/deserialization API supporting
 * multiple formats (JSON, XML, Kryo, Protobuf) through SPI mechanism.</p>
 * <p>此包通过 SPI 机制提供支持多种格式（JSON、XML、Kryo、Protobuf）的统一序列化/反序列化 API。</p>
 *
 * <p><strong>Core Classes | 核心类:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.serialization.OpenSerializer} - Main entry point facade - 主入口门面</li>
 *   <li>{@link cloud.opencode.base.serialization.Serializer} - Serializer interface - 序列化器接口</li>
 *   <li>{@link cloud.opencode.base.serialization.TypeReference} - Generic type reference - 泛型类型引用</li>
 *   <li>{@link cloud.opencode.base.serialization.SerializerConfig} - Configuration - 配置</li>
 * </ul>
 *
 * <p><strong>Quick Start | 快速开始:</strong></p>
 * <pre>{@code
 * // Serialize
 * byte[] data = OpenSerializer.serialize(user);
 *
 * // Deserialize
 * User restored = OpenSerializer.deserialize(data, User.class);
 *
 * // Generic types
 * List<User> users = OpenSerializer.deserialize(data, new TypeReference<List<User>>() {});
 *
 * // Deep copy
 * User copy = OpenSerializer.deepCopy(user);
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
package cloud.opencode.base.serialization;
