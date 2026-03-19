
package cloud.opencode.base.serialization.json;

import cloud.opencode.base.json.OpenJson;
import cloud.opencode.base.serialization.Serializer;
import cloud.opencode.base.serialization.TypeReference;
import cloud.opencode.base.serialization.exception.OpenSerializationException;

import java.lang.reflect.Type;

/**
 * JsonSerializer - JSON Serialization (delegates to OpenJson)
 * JSON 序列化器（委托给 OpenJson）
 *
 * <p>Provides JSON serialization by delegating to the opencode-base-json component.
 * This ensures consistent JSON handling across the library.</p>
 * <p>通过委托给 opencode-base-json 组件提供 JSON 序列化。
 * 这确保了整个库中一致的 JSON 处理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Human-readable output - 人类可读的输出</li>
 *   <li>Full generic type support - 完整的泛型类型支持</li>
 *   <li>Cross-platform compatibility - 跨平台兼容性</li>
 *   <li>Text-based format - 基于文本的格式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * JsonSerializer serializer = new JsonSerializer();
 *
 * // Serialize
 * byte[] data = serializer.serialize(user);
 * String json = new String(data, StandardCharsets.UTF_8);
 *
 * // Deserialize
 * User restored = serializer.deserialize(data, User.class);
 *
 * // Generic types
 * List<User> users = serializer.deserialize(data, new TypeReference<List<User>>() {});
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (delegates to thread-safe OpenJson) - 线程安全: 是（委托给线程安全的 OpenJson）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = object graph size - O(n), n为对象图大小</li>
 *   <li>Space complexity: O(n) for JSON string - JSON字符串 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
public class JsonSerializer implements Serializer {

    /**
     * Format name
     * 格式名称
     */
    public static final String FORMAT = "json";

    // ==================== Serializer Implementation | 序列化器实现 ====================

    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            return "null".getBytes();
        }

        try {
            return OpenJson.toJsonBytes(obj);
        } catch (Exception e) {
            throw OpenSerializationException.serializeFailed(obj, FORMAT, e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> type) {
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            return OpenJson.fromJson(data, type);
        } catch (Exception e) {
            throw OpenSerializationException.deserializeFailed(data, type, FORMAT, e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, TypeReference<T> typeRef) {
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            // Convert to OpenJson TypeReference
            cloud.opencode.base.json.TypeReference<T> jsonTypeRef =
                    cloud.opencode.base.json.TypeReference.of(typeRef.getType());
            return OpenJson.fromJson(new String(data), jsonTypeRef);
        } catch (Exception e) {
            throw OpenSerializationException.deserializeFailed(data, typeRef.getRawType(), FORMAT, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data, Type type) {
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            if (type instanceof Class<?> clazz) {
                return (T) OpenJson.fromJson(data, clazz);
            }
            cloud.opencode.base.json.TypeReference<T> jsonTypeRef =
                    cloud.opencode.base.json.TypeReference.of(type);
            return OpenJson.fromJson(new String(data), jsonTypeRef);
        } catch (Exception e) {
            Class<?> targetType = type instanceof Class<?> c ? c : Object.class;
            throw OpenSerializationException.deserializeFailed(data, targetType, FORMAT, e);
        }
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public String getMimeType() {
        return "application/json";
    }

    @Override
    public boolean isTextBased() {
        return true;
    }
}
