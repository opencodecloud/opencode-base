package cloud.opencode.base.json.internal;

import cloud.opencode.base.json.JsonConfig;
import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.TypeReference;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;
import cloud.opencode.base.json.spi.JsonFeature;
import cloud.opencode.base.json.spi.JsonProvider;
import cloud.opencode.base.json.stream.JsonReader;
import cloud.opencode.base.json.stream.JsonWriter;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Built-in JSON Provider - Zero-Dependency Lightweight JSON Provider
 * 内置JSON提供者 - 零依赖轻量级JSON提供者
 *
 * <p>A lightweight, zero-dependency JSON provider that handles core JSON operations
 * without requiring any third-party library. Supports JsonNode tree operations,
 * Map/List/primitive serialization/deserialization, and streaming API.</p>
 * <p>一个轻量级、零依赖的JSON提供者，无需第三方库即可处理核心JSON操作。
 * 支持JsonNode树操作、Map/List/基本类型序列化/反序列化和流式API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RFC 8259 compliant JSON parsing - 符合 RFC 8259 的JSON解析</li>
 *   <li>JsonNode tree model operations - JsonNode 树模型操作</li>
 *   <li>Map/List/primitive serialization - Map/List/基本类型序列化</li>
 *   <li>Streaming JsonReader/JsonWriter - 流式 JsonReader/JsonWriter</li>
 *   <li>Pretty printing support - 美化打印支持</li>
 *   <li>Automatic fallback when no third-party library available - 无第三方库时自动回退</li>
 * </ul>
 *
 * <p><strong>Limitations | 限制:</strong></p>
 * <ul>
 *   <li>No POJO bean mapping (use Jackson/Gson for that) - 无 POJO bean 映射</li>
 *   <li>Lower priority than third-party providers - 优先级低于第三方提供者</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless after configuration) - 线程安全: 是</li>
 *   <li>Nesting depth limited to 512 - 嵌套深度限制为512</li>
 * </ul>
 *
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // See class-level documentation for usage
 * // 参见类级文档了解用法
 * }</pre>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public final class BuiltinJsonProvider implements JsonProvider {

    private volatile boolean prettyPrint;

    /**
     * Constructs the built-in JSON provider
     * 构造内置JSON提供者
     */
    public BuiltinJsonProvider() {
        this.prettyPrint = false;
    }

    // ==================== Provider Information | 提供者信息 ====================

    @Override
    public String getName() {
        return "builtin";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    /**
     * Returns -100 (lowest priority, yields to Jackson/Gson/Fastjson2)
     * 返回 -100（最低优先级，让位于 Jackson/Gson/Fastjson2）
     *
     * @return -100
     */
    @Override
    public int getPriority() {
        return -100;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean supportsFeature(JsonFeature feature) {
        return feature == JsonFeature.PRETTY_PRINT
                || feature == JsonFeature.IGNORE_UNKNOWN_PROPERTIES
                || feature == JsonFeature.LIMIT_NESTING_DEPTH;
    }

    // ==================== Configuration | 配置 ====================

    @Override
    public void configure(JsonConfig config) {
        if (config != null) {
            this.prettyPrint = config.isEnabled(JsonFeature.PRETTY_PRINT);
        }
    }

    // ==================== Serialization | 序列化 ====================

    @Override
    public String toJson(Object obj) {
        return new JsonSerializer(prettyPrint).serialize(obj);
    }

    @Override
    public byte[] toJsonBytes(Object obj) {
        return toJson(obj).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void toJson(Object obj, OutputStream output) {
        try {
            output.write(toJsonBytes(obj));
        } catch (IOException e) {
            throw OpenJsonProcessingException.ioError("Failed to write JSON to output stream", e);
        }
    }

    @Override
    public void toJson(Object obj, Writer writer) {
        try {
            writer.write(toJson(obj));
        } catch (IOException e) {
            throw OpenJsonProcessingException.ioError("Failed to write JSON to writer", e);
        }
    }

    // ==================== Deserialization | 反序列化 ====================

    @Override
    @SuppressWarnings("unchecked")
    public <T> T fromJson(String json, Class<T> clazz) {
        Objects.requireNonNull(json, "json must not be null");
        Objects.requireNonNull(clazz, "clazz must not be null");

        JsonNode node = parseTree(json);

        return BeanMapper.fromTree(node, clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T fromJson(String json, Type type) {
        Objects.requireNonNull(json, "json must not be null");
        JsonNode node = parseTree(json);
        return BeanMapper.fromTree(node, type);
    }

    @Override
    public <T> T fromJson(String json, TypeReference<T> typeReference) {
        return fromJson(json, typeReference.getType());
    }

    @Override
    public <T> T fromJson(byte[] json, Class<T> clazz) {
        Objects.requireNonNull(json, "json must not be null");
        Objects.requireNonNull(clazz, "clazz must not be null");
        return fromJson(new String(json, StandardCharsets.UTF_8), clazz);
    }

    @Override
    public <T> T fromJson(byte[] json, TypeReference<T> typeReference) {
        Objects.requireNonNull(json, "json must not be null");
        Objects.requireNonNull(typeReference, "typeReference must not be null");
        return fromJson(new String(json, StandardCharsets.UTF_8), typeReference);
    }

    @Override
    public <T> T fromJson(InputStream input, Class<T> clazz) {
        try {
            return fromJson(new String(input.readAllBytes(), StandardCharsets.UTF_8), clazz);
        } catch (IOException e) {
            throw OpenJsonProcessingException.ioError("Failed to read JSON from input stream", e);
        }
    }

    @Override
    public <T> T fromJson(Reader reader, Class<T> clazz) {
        try {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[8192];
            int n;
            while ((n = reader.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
            return fromJson(sb.toString(), clazz);
        } catch (IOException e) {
            throw OpenJsonProcessingException.ioError("Failed to read JSON from reader", e);
        }
    }

    // ==================== Collection Deserialization | 集合反序列化 ====================

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> fromJsonArray(String json, Class<T> elementType) {
        JsonNode node = parseTree(json);
        if (!node.isArray()) {
            throw OpenJsonProcessingException.deserializationError("Expected JSON array", null);
        }
        List<T> result = new ArrayList<>();
        JsonNode.ArrayNode arr = (JsonNode.ArrayNode) node;
        for (int i = 0; i < arr.size(); i++) {
            result.add(BeanMapper.fromTree(arr.get(i), elementType));
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> fromJsonMap(String json, Class<K> keyType, Class<V> valueType) {
        JsonNode node = parseTree(json);
        if (!node.isObject()) {
            throw OpenJsonProcessingException.deserializationError("Expected JSON object", null);
        }
        Map<K, V> result = new LinkedHashMap<>();
        JsonNode.ObjectNode obj = (JsonNode.ObjectNode) node;
        for (String key : obj.keys()) {
            K k = (K) BeanMapper.fromTree(new JsonNode.StringNode(key), keyType);
            V v = BeanMapper.fromTree(obj.get(key), valueType);
            result.put(k, v);
        }
        return result;
    }

    // ==================== JsonNode Operations | JsonNode 操作 ====================

    @Override
    public JsonNode parseTree(String json) {
        Objects.requireNonNull(json, "json must not be null");
        return new JsonParser(json).parse();
    }

    @Override
    public JsonNode parseTree(byte[] json) {
        return parseTree(new String(json, StandardCharsets.UTF_8));
    }

    @Override
    public <T> T treeToValue(JsonNode node, Class<T> clazz) {
        return BeanMapper.fromTree(node, clazz);
    }

    @Override
    public JsonNode valueToTree(Object obj) {
        return BeanMapper.toTree(obj);
    }

    // ==================== Streaming API | 流式API ====================

    @Override
    public JsonReader createReader(InputStream input) {
        return new BuiltinJsonReader(new InputStreamReader(input, StandardCharsets.UTF_8));
    }

    @Override
    public JsonReader createReader(Reader reader) {
        return new BuiltinJsonReader(reader);
    }

    @Override
    public JsonWriter createWriter(OutputStream output) {
        return new BuiltinJsonWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
    }

    @Override
    public JsonWriter createWriter(Writer writer) {
        return new BuiltinJsonWriter(writer);
    }

    // ==================== Type Conversion | 类型转换 ====================

    @Override
    @SuppressWarnings("unchecked")
    public <T> T convertValue(Object obj, Class<T> clazz) {
        String json = toJson(obj);
        return fromJson(json, clazz);
    }

    @Override
    public <T> T convertValue(Object obj, TypeReference<T> typeReference) {
        String json = toJson(obj);
        return fromJson(json, typeReference);
    }

    // ==================== Utility Methods | 工具方法 ====================

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getUnderlyingProvider() {
        return (T) this;
    }

    @Override
    public JsonProvider copy() {
        BuiltinJsonProvider copy = new BuiltinJsonProvider();
        copy.prettyPrint = this.prettyPrint;
        return copy;
    }

}
