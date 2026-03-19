
package cloud.opencode.base.serialization.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;
import java.lang.reflect.Type;

/**
 * OpenSerializationException - Serialization Exception
 * 序列化异常
 *
 * <p>Exception thrown when serialization or deserialization operations fail.</p>
 * <p>当序列化或反序列化操作失败时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Serialize failure handling - 序列化失败处理</li>
 *   <li>Deserialize failure handling - 反序列化失败处理</li>
 *   <li>Serializer not found handling - 序列化器未找到处理</li>
 *   <li>Unsupported type handling - 不支持的类型处理</li>
 *   <li>Compression/Decompression failure handling - 压缩/解压失败处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw OpenSerializationException.serializeFailed(obj, cause);
 * throw OpenSerializationException.deserializeFailed(data, type, cause);
 * throw OpenSerializationException.serializerNotFound("msgpack");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Serializable: Yes - 可序列化: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
public class OpenSerializationException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT = "SERIALIZATION";

    /**
     * The serialization format (e.g., "json", "kryo")
     * 序列化格式
     */
    private final String format;

    /**
     * The target type for deserialization
     * 反序列化目标类型
     */
    private final Class<?> targetType;

    // ==================== Constructors | 构造方法 ====================

    /**
     * Creates a serialization exception with message.
     * 创建带消息的序列化异常。
     *
     * @param message the error message - 错误消息
     */
    public OpenSerializationException(String message) {
        super(COMPONENT, null, message);
        this.format = null;
        this.targetType = null;
    }

    /**
     * Creates a serialization exception with message and cause.
     * 创建带消息和原因的序列化异常。
     *
     * @param message the error message - 错误消息
     * @param cause   the cause - 原因
     */
    public OpenSerializationException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.format = null;
        this.targetType = null;
    }

    /**
     * Creates a serialization exception with full details.
     * 创建带完整详情的序列化异常。
     *
     * @param message    the error message - 错误消息
     * @param format     the serialization format - 序列化格式
     * @param targetType the target type - 目标类型
     * @param cause      the cause - 原因
     */
    public OpenSerializationException(String message, String format, Class<?> targetType, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.format = format;
        this.targetType = targetType;
    }

    // ==================== Getters | 获取方法 ====================

    /**
     * Returns the serialization format.
     * 返回序列化格式。
     *
     * @return the format, may be null - 格式，可能为 null
     */
    public String getFormat() {
        return format;
    }

    /**
     * Returns the target type.
     * 返回目标类型。
     *
     * @return the target type, may be null - 目标类型，可能为 null
     */
    public Class<?> getTargetType() {
        return targetType;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates exception for serialization failure.
     * 创建序列化失败异常。
     *
     * @param obj   the object that failed to serialize - 序列化失败的对象
     * @param cause the cause - 原因
     * @return the exception - 异常
     */
    public static OpenSerializationException serializeFailed(Object obj, Throwable cause) {
        String typeName = obj != null ? obj.getClass().getName() : "null";
        return new OpenSerializationException(
                "Failed to serialize object of type: " + typeName,
                null,
                obj != null ? obj.getClass() : null,
                cause
        );
    }

    /**
     * Creates exception for serialization failure with format.
     * 创建带格式的序列化失败异常。
     *
     * @param obj    the object that failed to serialize - 序列化失败的对象
     * @param format the serialization format - 序列化格式
     * @param cause  the cause - 原因
     * @return the exception - 异常
     */
    public static OpenSerializationException serializeFailed(Object obj, String format, Throwable cause) {
        String typeName = obj != null ? obj.getClass().getName() : "null";
        return new OpenSerializationException(
                "Failed to serialize object of type '" + typeName + "' using format '" + format + "'",
                format,
                obj != null ? obj.getClass() : null,
                cause
        );
    }

    /**
     * Creates exception for deserialization failure.
     * 创建反序列化失败异常。
     *
     * @param data  the data that failed to deserialize - 反序列化失败的数据
     * @param type  the target type - 目标类型
     * @param cause the cause - 原因
     * @return the exception - 异常
     */
    public static OpenSerializationException deserializeFailed(byte[] data, Class<?> type, Throwable cause) {
        int size = data != null ? data.length : 0;
        return new OpenSerializationException(
                "Failed to deserialize to type: " + type.getName() + ", data size: " + size + " bytes",
                null,
                type,
                cause
        );
    }

    /**
     * Creates exception for deserialization failure with format.
     * 创建带格式的反序列化失败异常。
     *
     * @param data   the data that failed to deserialize - 反序列化失败的数据
     * @param type   the target type - 目标类型
     * @param format the serialization format - 序列化格式
     * @param cause  the cause - 原因
     * @return the exception - 异常
     */
    public static OpenSerializationException deserializeFailed(byte[] data, Class<?> type, String format, Throwable cause) {
        int size = data != null ? data.length : 0;
        return new OpenSerializationException(
                "Failed to deserialize to type '" + type.getName() + "' using format '" + format + "', data size: " + size + " bytes",
                format,
                type,
                cause
        );
    }

    /**
     * Creates exception when serializer is not found.
     * 创建序列化器未找到异常。
     *
     * @param format the requested format - 请求的格式
     * @return the exception - 异常
     */
    public static OpenSerializationException serializerNotFound(String format) {
        return new OpenSerializationException(
                "No serializer found for format: '" + format + "'. Available formats can be listed via OpenSerializer.getFormats()",
                format,
                null,
                null
        );
    }

    /**
     * Creates exception for unsupported type.
     * 创建不支持类型异常。
     *
     * @param type   the unsupported type - 不支持的类型
     * @param format the serialization format - 序列化格式
     * @return the exception - 异常
     */
    public static OpenSerializationException unsupportedType(Type type, String format) {
        Class<?> clazz = type instanceof Class<?> c ? c : null;
        return new OpenSerializationException(
                "Type not supported by '" + format + "' serializer: " + type.getTypeName(),
                format,
                clazz,
                null
        );
    }

    /**
     * Creates exception for unsupported type.
     * 创建不支持类型异常。
     *
     * @param type   the unsupported type - 不支持的类型
     * @param format the serialization format - 序列化格式
     * @return the exception - 异常
     */
    public static OpenSerializationException unsupportedType(Class<?> type, String format) {
        return new OpenSerializationException(
                "Type not supported by '" + format + "' serializer: " + type.getName(),
                format,
                type,
                null
        );
    }

    /**
     * Creates exception for compression failure.
     * 创建压缩失败异常。
     *
     * @param cause the cause - 原因
     * @return the exception - 异常
     */
    public static OpenSerializationException compressionFailed(Throwable cause) {
        return new OpenSerializationException("Compression failed", cause);
    }

    /**
     * Creates exception for compression failure with algorithm.
     * 创建带算法的压缩失败异常。
     *
     * @param algorithm the compression algorithm - 压缩算法
     * @param cause     the cause - 原因
     * @return the exception - 异常
     */
    public static OpenSerializationException compressionFailed(String algorithm, Throwable cause) {
        return new OpenSerializationException("Compression failed using algorithm: " + algorithm, cause);
    }

    /**
     * Creates exception for decompression failure.
     * 创建解压失败异常。
     *
     * @param cause the cause - 原因
     * @return the exception - 异常
     */
    public static OpenSerializationException decompressionFailed(Throwable cause) {
        return new OpenSerializationException("Decompression failed", cause);
    }

    /**
     * Creates exception for decompression failure with algorithm.
     * 创建带算法的解压失败异常。
     *
     * @param algorithm the compression algorithm - 压缩算法
     * @param cause     the cause - 原因
     * @return the exception - 异常
     */
    public static OpenSerializationException decompressionFailed(String algorithm, Throwable cause) {
        return new OpenSerializationException("Decompression failed using algorithm: " + algorithm, cause);
    }

    /**
     * Creates exception for missing dependency.
     * 创建缺少依赖异常。
     *
     * @param format     the format requiring the dependency - 需要依赖的格式
     * @param dependency the missing dependency - 缺少的依赖
     * @return the exception - 异常
     */
    public static OpenSerializationException missingDependency(String format, String dependency) {
        return new OpenSerializationException(
                "Serializer '" + format + "' requires dependency '" + dependency + "' which is not available on classpath",
                format,
                null,
                null
        );
    }
}
