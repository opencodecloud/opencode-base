
package cloud.opencode.base.serialization.binary;

import cloud.opencode.base.serialization.Serializer;
import cloud.opencode.base.serialization.TypeReference;
import cloud.opencode.base.serialization.exception.OpenSerializationException;
import com.google.protobuf.Message;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * ProtobufSerializer - Google Protocol Buffers Serialization
 * Protobuf 序列化器
 *
 * <p>Uses Google Protocol Buffers for cross-language high-performance serialization.
 * Only supports objects that are Protobuf Message types.</p>
 * <p>使用 Google Protocol Buffers 进行跨语言高性能序列化。
 * 仅支持 Protobuf Message 类型的对象。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Cross-language compatibility - 跨语言兼容性</li>
 *   <li>High performance - 高性能</li>
 *   <li>Schema evolution support - Schema 演进支持</li>
 *   <li>Compact binary format - 紧凑的二进制格式</li>
 * </ul>
 *
 * <p><strong>Limitations | 限制:</strong></p>
 * <ul>
 *   <li>Only supports Protobuf Message types - 仅支持 Protobuf Message 类型</li>
 *   <li>Requires .proto files and code generation - 需要 .proto 文件和代码生成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ProtobufSerializer serializer = new ProtobufSerializer();
 *
 * // Serialize (object must be a Protobuf Message)
 * byte[] data = serializer.serialize(userProto);
 *
 * // Deserialize
 * UserProto restored = serializer.deserialize(data, UserProto.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = message size - O(n), n为消息大小</li>
 *   <li>Space complexity: O(n) for serialized bytes - 序列化字节 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
public class ProtobufSerializer implements Serializer {

    /**
     * Format name
     * 格式名称
     */
    public static final String FORMAT = "protobuf";

    // ==================== Serializer Implementation | 序列化器实现 ====================

    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            return new byte[0];
        }

        if (!(obj instanceof Message message)) {
            throw OpenSerializationException.unsupportedType(obj.getClass(), FORMAT);
        }

        return message.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> type) {
        if (data == null || data.length == 0) {
            return null;
        }

        if (!Message.class.isAssignableFrom(type)) {
            throw OpenSerializationException.unsupportedType(type, FORMAT);
        }

        try {
            Method parseFrom = type.getMethod("parseFrom", byte[].class);
            @SuppressWarnings("unchecked")
            T result = (T) parseFrom.invoke(null, data);
            return result;
        } catch (Exception e) {
            throw OpenSerializationException.deserializeFailed(data, type, FORMAT, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data, TypeReference<T> typeRef) {
        Class<?> rawType = typeRef.getRawType();
        return (T) deserialize(data, rawType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data, Type type) {
        if (type instanceof Class<?> clazz) {
            return (T) deserialize(data, clazz);
        }
        throw OpenSerializationException.unsupportedType(type, FORMAT);
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public String getMimeType() {
        return "application/x-protobuf";
    }

    @Override
    public boolean supports(Class<?> type) {
        return Message.class.isAssignableFrom(type);
    }
}
