
package cloud.opencode.base.json.adapter;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.stream.JsonReader;
import cloud.opencode.base.json.stream.JsonWriter;

import java.lang.reflect.Type;

/**
 * JSON Type Adapter - Custom Serialization/Deserialization
 * JSON 类型适配器 - 自定义序列化/反序列化
 *
 * <p>This interface defines custom serialization and deserialization
 * logic for specific types. Implementations can be registered with
 * the JsonAdapterRegistry.</p>
 * <p>此接口定义特定类型的自定义序列化和反序列化逻辑。
 * 实现可以注册到 JsonAdapterRegistry。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public class MoneyAdapter implements JsonTypeAdapter<Money> {
 *     @Override
 *     public Class<Money> getType() {
 *         return Money.class;
 *     }
 *
 *     @Override
 *     public JsonNode toJson(Money value) {
 *         return JsonNode.object()
 *             .put("amount", value.getAmount())
 *             .put("currency", value.getCurrency());
 *     }
 *
 *     @Override
 *     public Money fromJson(JsonNode node) {
 *         return new Money(
 *             node.get("amount").asBigDecimal(),
 *             node.get("currency").asString()
 *         );
 *     }
 * }
 *
 * // Register
 * JsonAdapterRegistry.register(new MoneyAdapter());
 * }</pre>
 *
 * @param <T> the type this adapter handles - 此适配器处理的类型
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Custom serialization/deserialization for specific types - 特定类型的自定义序列化/反序列化</li>
 *   <li>Optional streaming read/write support - 可选的流式读/写支持</li>
 *   <li>Lambda-based adapter creation via factory methods - 通过工厂方法的Lambda适配器创建</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public interface JsonTypeAdapter<T> {

    /**
     * Returns the type this adapter handles.
     * 返回此适配器处理的类型。
     *
     * @return the type class - 类型类
     */
    Class<T> getType();

    /**
     * Returns the generic type if applicable.
     * 返回泛型类型（如果适用）。
     *
     * @return the generic type, or null - 泛型类型，或 null
     */
    default Type getGenericType() {
        return getType();
    }

    /**
     * Serializes a value to a JsonNode.
     * 将值序列化为 JsonNode。
     *
     * @param value the value to serialize - 要序列化的值
     * @return the JSON node - JSON 节点
     */
    JsonNode toJson(T value);

    /**
     * Deserializes a JsonNode to a value.
     * 将 JsonNode 反序列化为值。
     *
     * @param node the JSON node - JSON 节点
     * @return the deserialized value - 反序列化的值
     */
    T fromJson(JsonNode node);

    /**
     * Writes the value directly to a JsonWriter (optional).
     * 直接将值写入 JsonWriter（可选）。
     *
     * @param writer the JSON writer - JSON 写入器
     * @param value  the value to write - 要写入的值
     */
    default void write(JsonWriter writer, T value) {
        // Default implementation uses toJson
        throw new UnsupportedOperationException(
                "Streaming write not supported by this adapter");
    }

    /**
     * Reads the value directly from a JsonReader (optional).
     * 直接从 JsonReader 读取值（可选）。
     *
     * @param reader the JSON reader - JSON 读取器
     * @return the read value - 读取的值
     */
    default T read(JsonReader reader) {
        // Default implementation requires subclass override
        throw new UnsupportedOperationException(
                "Streaming read not supported by this adapter");
    }

    /**
     * Returns whether this adapter supports streaming.
     * 返回此适配器是否支持流式处理。
     *
     * @return true if streaming is supported - 如果支持流式处理则返回 true
     */
    default boolean supportsStreaming() {
        return false;
    }

    /**
     * Returns whether null values should use this adapter.
     * 返回是否应为 null 值使用此适配器。
     *
     * @return true to handle nulls - 如果处理 null 则返回 true
     */
    default boolean handlesNull() {
        return false;
    }

    /**
     * Creates a simple adapter from lambdas.
     * 从 lambda 创建简单适配器。
     *
     * @param type       the type class - 类型类
     * @param serializer the serialization function - 序列化函数
     * @param deserializer the deserialization function - 反序列化函数
     * @param <T>        the type - 类型
     * @return the adapter - 适配器
     */
    static <T> JsonTypeAdapter<T> of(
            Class<T> type,
            java.util.function.Function<T, JsonNode> serializer,
            java.util.function.Function<JsonNode, T> deserializer) {
        return new JsonTypeAdapter<>() {
            @Override
            public Class<T> getType() {
                return type;
            }

            @Override
            public JsonNode toJson(T value) {
                return serializer.apply(value);
            }

            @Override
            public T fromJson(JsonNode node) {
                return deserializer.apply(node);
            }
        };
    }

    /**
     * Creates an adapter for simple string conversion.
     * 创建简单字符串转换的适配器。
     *
     * @param type       the type class - 类型类
     * @param toString   convert to string - 转换为字符串
     * @param fromString convert from string - 从字符串转换
     * @param <T>        the type - 类型
     * @return the adapter - 适配器
     */
    static <T> JsonTypeAdapter<T> ofString(
            Class<T> type,
            java.util.function.Function<T, String> toString,
            java.util.function.Function<String, T> fromString) {
        return of(type,
                v -> JsonNode.of(toString.apply(v)),
                n -> fromString.apply(n.asString()));
    }
}
