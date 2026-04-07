
package cloud.opencode.base.serialization;

import cloud.opencode.base.serialization.exception.OpenSerializationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Serializer - Core Serialization Interface
 * 序列化器 - 核心序列化接口
 *
 * <p>This interface defines the contract for all serializers in the OpenCode serialization framework.
 * Implementations should provide serialization and deserialization capabilities for specific formats.</p>
 * <p>此接口定义了 OpenCode 序列化框架中所有序列化器的契约。
 * 实现类应为特定格式提供序列化和反序列化能力。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Serialize objects to byte arrays - 将对象序列化为字节数组</li>
 *   <li>Deserialize byte arrays to objects - 将字节数组反序列化为对象</li>
 *   <li>Support for generic types via TypeReference - 通过 TypeReference 支持泛型类型</li>
 *   <li>Format and MIME type identification - 格式和 MIME 类型标识</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Serializer serializer = new JsonSerializer();
 *
 * // Serialize
 * byte[] data = serializer.serialize(user);
 *
 * // Deserialize
 * User restored = serializer.deserialize(data, User.class);
 *
 * // Deserialize generic type
 * List<User> users = serializer.deserialize(data, new TypeReference<List<User>>() {});
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
public interface Serializer {

    /**
     * Serializes an object to byte array.
     * 将对象序列化为字节数组。
     *
     * @param obj the object to serialize - 要序列化的对象
     * @return the serialized bytes - 序列化后的字节数组
     * @throws cloud.opencode.base.serialization.exception.OpenSerializationException if serialization fails - 如果序列化失败
     */
    byte[] serialize(Object obj);

    /**
     * Deserializes byte array to an object of the specified class.
     * 将字节数组反序列化为指定类的对象。
     *
     * @param data the serialized data - 序列化的数据
     * @param type the target class - 目标类
     * @param <T>  the target type - 目标类型
     * @return the deserialized object - 反序列化后的对象
     * @throws cloud.opencode.base.serialization.exception.OpenSerializationException if deserialization fails - 如果反序列化失败
     */
    <T> T deserialize(byte[] data, Class<T> type);

    /**
     * Deserializes byte array to a generic type using TypeReference.
     * 使用 TypeReference 将字节数组反序列化为泛型类型。
     *
     * <p>This method preserves generic type information that would otherwise be lost due to type erasure.</p>
     * <p>此方法保留了由于类型擦除而丢失的泛型类型信息。</p>
     *
     * @param data    the serialized data - 序列化的数据
     * @param typeRef the type reference - 类型引用
     * @param <T>     the target type - 目标类型
     * @return the deserialized object - 反序列化后的对象
     * @throws cloud.opencode.base.serialization.exception.OpenSerializationException if deserialization fails - 如果反序列化失败
     */
    <T> T deserialize(byte[] data, TypeReference<T> typeRef);

    /**
     * Deserializes byte array using a Type.
     * 使用 Type 反序列化字节数组。
     *
     * @param data the serialized data - 序列化的数据
     * @param type the target type - 目标类型
     * @param <T>  the target type - 目标类型
     * @return the deserialized object - 反序列化后的对象
     * @throws cloud.opencode.base.serialization.exception.OpenSerializationException if deserialization fails - 如果反序列化失败
     */
    <T> T deserialize(byte[] data, Type type);

    /**
     * Returns the format name of this serializer.
     * 返回此序列化器的格式名称。
     *
     * <p>Examples: "json", "xml", "kryo", "protobuf", "jdk"</p>
     * <p>示例: "json", "xml", "kryo", "protobuf", "jdk"</p>
     *
     * @return the format name - 格式名称
     */
    String getFormat();

    /**
     * Returns the MIME type for this serializer.
     * 返回此序列化器的 MIME 类型。
     *
     * @return the MIME type (default: "application/octet-stream") - MIME 类型
     */
    default String getMimeType() {
        return "application/octet-stream";
    }

    /**
     * Checks if this serializer supports the given type.
     * 检查此序列化器是否支持给定类型。
     *
     * <p>Some serializers have specific type requirements.</p>
     * <p>某些序列化器有特定的类型要求。</p>
     *
     * @param type the type to check - 要检查的类型
     * @return true if supported - 如果支持则返回 true
     */
    default boolean supports(Class<?> type) {
        return true;
    }

    /**
     * Returns whether this serializer produces text output.
     * 返回此序列化器是否产生文本输出。
     *
     * <p>Text-based serializers (JSON, XML) can be converted to String without data loss.</p>
     * <p>基于文本的序列化器（JSON、XML）可以无损转换为字符串。</p>
     *
     * @return true if text-based - 如果是基于文本的则返回 true
     */
    default boolean isTextBased() {
        return false;
    }

    // ==================== Streaming API | 流式 API ====================

    /**
     * Serializes an object to an output stream.
     * 将对象序列化到输出流。
     *
     * <p>Default implementation serializes to byte array then writes to stream.</p>
     * <p>默认实现先序列化为字节数组再写入流。</p>
     *
     * @param obj the object to serialize | 要序列化的对象
     * @param out the output stream | 输出流
     * @throws OpenSerializationException if serialization fails | 序列化失败时抛出
     * @since JDK 25, opencode-base-serialization V1.0.3
     */
    default void serialize(Object obj, OutputStream out) {
        Objects.requireNonNull(out, "OutputStream must not be null");
        try {
            byte[] data = serialize(obj);
            out.write(data);
            out.flush();
        } catch (IOException e) {
            throw new OpenSerializationException("Failed to write serialized data to stream", e);
        }
    }

    /**
     * Deserializes from an input stream.
     * 从输入流反序列化。
     *
     * <p>Default implementation reads all bytes then deserializes.</p>
     * <p>默认实现先读取所有字节再反序列化。</p>
     *
     * @param in   the input stream | 输入流
     * @param type the target class | 目标类
     * @param <T>  the target type | 目标类型
     * @return the deserialized object | 反序列化后的对象
     * @throws OpenSerializationException if deserialization fails | 反序列化失败时抛出
     * @since JDK 25, opencode-base-serialization V1.0.3
     */
    default <T> T deserialize(InputStream in, Class<T> type) {
        Objects.requireNonNull(in, "InputStream must not be null");
        Objects.requireNonNull(type, "Type must not be null");
        try {
            byte[] data = readLimited(in);
            return deserialize(data, type);
        } catch (IOException e) {
            throw new OpenSerializationException("Failed to read data from stream", e);
        }
    }

    /**
     * Deserializes a generic type from an input stream.
     * 从输入流反序列化泛型类型。
     *
     * @param in      the input stream | 输入流
     * @param typeRef the type reference | 类型引用
     * @param <T>     the target type | 目标类型
     * @return the deserialized object | 反序列化后的对象
     * @throws OpenSerializationException if deserialization fails | 反序列化失败时抛出
     * @since JDK 25, opencode-base-serialization V1.0.3
     */
    default <T> T deserialize(InputStream in, TypeReference<T> typeRef) {
        Objects.requireNonNull(in, "InputStream must not be null");
        Objects.requireNonNull(typeRef, "TypeReference must not be null");
        try {
            byte[] data = readLimited(in);
            return deserialize(data, typeRef);
        } catch (IOException e) {
            throw new OpenSerializationException("Failed to read data from stream", e);
        }
    }

    // ==================== Internal Helpers | 内部辅助 ====================

    /**
     * Reads from an InputStream with a size limit to prevent OOM DoS.
     * 从 InputStream 读取并限制大小，防止 OOM DoS。
     */
    private static byte[] readLimited(InputStream in) throws IOException {
        final int maxStreamSize = 256 * 1024 * 1024; // 256 MB
        ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
        byte[] buffer = new byte[8192];
        long totalRead = 0;
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            totalRead += bytesRead;
            if (totalRead > maxStreamSize) {
                throw new OpenSerializationException(
                        "Input stream exceeds maximum size limit of " + maxStreamSize + " bytes");
            }
            bos.write(buffer, 0, bytesRead);
        }
        return bos.toByteArray();
    }

    // ==================== Metadata API | 元数据 API ====================

    /**
     * Returns metadata information about this serializer.
     * 返回此序列化器的元数据信息。
     *
     * @return the serializer info | 序列化器信息
     * @since JDK 25, opencode-base-serialization V1.0.3
     */
    default SerializerInfo info() {
        return new SerializerInfo(getFormat(), getMimeType(), isTextBased(), false, false, getFormat() + " serializer");
    }
}
