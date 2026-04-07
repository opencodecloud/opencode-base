
package cloud.opencode.base.json.spi;

import cloud.opencode.base.json.JsonConfig;
import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.TypeReference;
import cloud.opencode.base.json.stream.JsonReader;
import cloud.opencode.base.json.stream.JsonWriter;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * JSON Provider - SPI Interface for JSON Processing Engines
 * JSON 提供者 - JSON 处理引擎的 SPI 接口
 *
 * <p>This interface defines the contract for JSON processing implementations.
 * Implementations can wrap Jackson, Gson, Fastjson2, or other JSON libraries.</p>
 * <p>此接口定义 JSON 处理实现的契约。实现可以包装 Jackson、Gson、Fastjson2 或其他 JSON 库。</p>
 *
 * <p><strong>Implementation Notes | 实现注意事项:</strong></p>
 * <ul>
 *   <li>Implementations must be thread-safe - 实现必须是线程安全的</li>
 *   <li>All methods should respect the provided JsonConfig - 所有方法应遵循提供的 JsonConfig</li>
 *   <li>Implementations should register via ServiceLoader - 实现应通过 ServiceLoader 注册</li>
 * </ul>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SPI interface for pluggable JSON engines - 可插拔JSON引擎的SPI接口</li>
 *   <li>Full serialization, deserialization, tree model, and streaming API - 完整的序列化、反序列化、树模型和流式API</li>
 *   <li>Provider priority and availability management - 提供者优先级和可用性管理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Implement the JsonProvider interface
 * // 实现 JsonProvider 接口
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public interface JsonProvider {

    // ==================== Provider Information ====================

    /**
     * Returns the provider name (e.g., "jackson", "gson", "fastjson2").
     * 返回提供者名称（如 "jackson"、"gson"、"fastjson2"）。
     *
     * @return the provider name - 提供者名称
     */
    String getName();

    /**
     * Returns the provider version.
     * 返回提供者版本。
     *
     * @return the version string - 版本字符串
     */
    String getVersion();

    /**
     * Returns the priority of this provider (higher = preferred).
     * 返回此提供者的优先级（越高越优先）。
     *
     * @return the priority (default 0) - 优先级（默认0）
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Returns whether this provider is available.
     * 返回此提供者是否可用。
     *
     * @return true if available - 如果可用则返回 true
     */
    default boolean isAvailable() {
        return true;
    }

    // ==================== Configuration ====================

    /**
     * Configures this provider with the given configuration.
     * 使用给定配置配置此提供者。
     *
     * @param config the configuration - 配置
     */
    void configure(JsonConfig config);

    /**
     * Returns whether a feature is supported by this provider.
     * 返回此提供者是否支持某特性。
     *
     * @param feature the feature to check - 要检查的特性
     * @return true if supported - 如果支持则返回 true
     */
    boolean supportsFeature(JsonFeature feature);

    // ==================== Serialization ====================

    /**
     * Serializes an object to a JSON string.
     * 将对象序列化为 JSON 字符串。
     *
     * @param obj the object to serialize - 要序列化的对象
     * @return the JSON string - JSON 字符串
     */
    String toJson(Object obj);

    /**
     * Serializes an object to a JSON byte array.
     * 将对象序列化为 JSON 字节数组。
     *
     * @param obj the object to serialize - 要序列化的对象
     * @return the JSON bytes - JSON 字节数组
     */
    byte[] toJsonBytes(Object obj);

    /**
     * Serializes an object to an OutputStream.
     * 将对象序列化到输出流。
     *
     * @param obj    the object to serialize - 要序列化的对象
     * @param output the output stream - 输出流
     */
    void toJson(Object obj, OutputStream output);

    /**
     * Serializes an object to a Writer.
     * 将对象序列化到 Writer。
     *
     * @param obj    the object to serialize - 要序列化的对象
     * @param writer the writer - Writer
     */
    void toJson(Object obj, Writer writer);

    // ==================== Deserialization ====================

    /**
     * Deserializes a JSON string to an object of the specified class.
     * 将 JSON 字符串反序列化为指定类的对象。
     *
     * @param json  the JSON string - JSON 字符串
     * @param clazz the target class - 目标类
     * @param <T>   the target type - 目标类型
     * @return the deserialized object - 反序列化的对象
     */
    <T> T fromJson(String json, Class<T> clazz);

    /**
     * Deserializes a JSON string to an object of the specified type.
     * 将 JSON 字符串反序列化为指定类型的对象。
     *
     * @param json the JSON string - JSON 字符串
     * @param type the target type - 目标类型
     * @param <T>  the target type - 目标类型
     * @return the deserialized object - 反序列化的对象
     */
    <T> T fromJson(String json, Type type);

    /**
     * Deserializes a JSON string using a TypeReference.
     * 使用 TypeReference 反序列化 JSON 字符串。
     *
     * @param json          the JSON string - JSON 字符串
     * @param typeReference the type reference - 类型引用
     * @param <T>           the target type - 目标类型
     * @return the deserialized object - 反序列化的对象
     */
    <T> T fromJson(String json, TypeReference<T> typeReference);

    /**
     * Deserializes JSON bytes to an object of the specified class.
     * 将 JSON 字节数组反序列化为指定类的对象。
     *
     * @param json  the JSON bytes - JSON 字节数组
     * @param clazz the target class - 目标类
     * @param <T>   the target type - 目标类型
     * @return the deserialized object - 反序列化的对象
     */
    <T> T fromJson(byte[] json, Class<T> clazz);

    /**
     * Deserializes JSON bytes using a TypeReference.
     * 使用 TypeReference 反序列化 JSON 字节数组。
     *
     * @param json          the JSON bytes - JSON 字节数组
     * @param typeReference the type reference - 类型引用
     * @param <T>           the target type - 目标类型
     * @return the deserialized object - 反序列化的对象
     */
    <T> T fromJson(byte[] json, TypeReference<T> typeReference);

    /**
     * Deserializes from an InputStream to an object of the specified class.
     * 从输入流反序列化为指定类的对象。
     *
     * @param input the input stream - 输入流
     * @param clazz the target class - 目标类
     * @param <T>   the target type - 目标类型
     * @return the deserialized object - 反序列化的对象
     */
    <T> T fromJson(InputStream input, Class<T> clazz);

    /**
     * Deserializes from a Reader to an object of the specified class.
     * 从 Reader 反序列化为指定类的对象。
     *
     * @param reader the reader - Reader
     * @param clazz  the target class - 目标类
     * @param <T>    the target type - 目标类型
     * @return the deserialized object - 反序列化的对象
     */
    <T> T fromJson(Reader reader, Class<T> clazz);

    // ==================== Collection Deserialization ====================

    /**
     * Deserializes a JSON array to a List.
     * 将 JSON 数组反序列化为 List。
     *
     * @param json        the JSON string - JSON 字符串
     * @param elementType the element type - 元素类型
     * @param <T>         the element type - 元素类型
     * @return the list - 列表
     */
    <T> List<T> fromJsonArray(String json, Class<T> elementType);

    /**
     * Deserializes a JSON object to a Map.
     * 将 JSON 对象反序列化为 Map。
     *
     * @param json      the JSON string - JSON 字符串
     * @param keyType   the key type - 键类型
     * @param valueType the value type - 值类型
     * @param <K>       the key type - 键类型
     * @param <V>       the value type - 值类型
     * @return the map - Map
     */
    <K, V> Map<K, V> fromJsonMap(String json, Class<K> keyType, Class<V> valueType);

    // ==================== JsonNode Operations ====================

    /**
     * Parses a JSON string to a JsonNode tree.
     * 将 JSON 字符串解析为 JsonNode 树。
     *
     * @param json the JSON string - JSON 字符串
     * @return the root node - 根节点
     */
    JsonNode parseTree(String json);

    /**
     * Parses JSON bytes to a JsonNode tree.
     * 将 JSON 字节数组解析为 JsonNode 树。
     *
     * @param json the JSON bytes - JSON 字节数组
     * @return the root node - 根节点
     */
    JsonNode parseTree(byte[] json);

    /**
     * Converts a JsonNode to an object of the specified class.
     * 将 JsonNode 转换为指定类的对象。
     *
     * @param node  the JSON node - JSON 节点
     * @param clazz the target class - 目标类
     * @param <T>   the target type - 目标类型
     * @return the converted object - 转换后的对象
     */
    <T> T treeToValue(JsonNode node, Class<T> clazz);

    /**
     * Converts an object to a JsonNode tree.
     * 将对象转换为 JsonNode 树。
     *
     * @param obj the object - 对象
     * @return the JSON node - JSON 节点
     */
    JsonNode valueToTree(Object obj);

    // ==================== Streaming API ====================

    /**
     * Creates a JsonReader for the given input stream.
     * 为给定输入流创建 JsonReader。
     *
     * @param input the input stream - 输入流
     * @return the JSON reader - JSON 读取器
     */
    JsonReader createReader(InputStream input);

    /**
     * Creates a JsonReader for the given reader.
     * 为给定 Reader 创建 JsonReader。
     *
     * @param reader the reader - Reader
     * @return the JSON reader - JSON 读取器
     */
    JsonReader createReader(Reader reader);

    /**
     * Creates a JsonWriter for the given output stream.
     * 为给定输出流创建 JsonWriter。
     *
     * @param output the output stream - 输出流
     * @return the JSON writer - JSON 写入器
     */
    JsonWriter createWriter(OutputStream output);

    /**
     * Creates a JsonWriter for the given writer.
     * 为给定 Writer 创建 JsonWriter。
     *
     * @param writer the writer - Writer
     * @return the JSON writer - JSON 写入器
     */
    JsonWriter createWriter(Writer writer);

    // ==================== Type Conversion ====================

    /**
     * Converts an object to another type.
     * 将对象转换为另一种类型。
     *
     * @param obj   the source object - 源对象
     * @param clazz the target class - 目标类
     * @param <T>   the target type - 目标类型
     * @return the converted object - 转换后的对象
     */
    <T> T convertValue(Object obj, Class<T> clazz);

    /**
     * Converts an object to another type using a TypeReference.
     * 使用 TypeReference 将对象转换为另一种类型。
     *
     * @param obj           the source object - 源对象
     * @param typeReference the type reference - 类型引用
     * @param <T>           the target type - 目标类型
     * @return the converted object - 转换后的对象
     */
    <T> T convertValue(Object obj, TypeReference<T> typeReference);

    // ==================== Utility Methods ====================

    /**
     * Returns the underlying JSON library object (e.g., ObjectMapper for Jackson).
     * 返回底层 JSON 库对象（如 Jackson 的 ObjectMapper）。
     *
     * @param <T> the expected type - 预期类型
     * @return the underlying object - 底层对象
     */
    <T> T getUnderlyingProvider();

    /**
     * Creates a copy of this provider with independent configuration.
     * 创建具有独立配置的此提供者的副本。
     *
     * @return a new provider instance - 新提供者实例
     */
    JsonProvider copy();
}
