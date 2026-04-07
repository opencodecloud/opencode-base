
package cloud.opencode.base.json;

import cloud.opencode.base.json.adapter.JsonAdapterRegistry;
import cloud.opencode.base.json.adapter.JsonTypeAdapter;
import cloud.opencode.base.json.diff.JsonDiff;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;
import cloud.opencode.base.json.patch.JsonMergePatch;
import cloud.opencode.base.json.patch.JsonPatch;
import cloud.opencode.base.json.path.JsonPath;
import cloud.opencode.base.json.path.JsonPointer;
import cloud.opencode.base.json.schema.JsonSchemaValidator;
import cloud.opencode.base.json.security.JsonSecurity;
import cloud.opencode.base.json.spi.JsonModule;
import cloud.opencode.base.json.spi.JsonProvider;
import cloud.opencode.base.json.spi.JsonProviderFactory;
import cloud.opencode.base.json.stream.JsonReader;
import cloud.opencode.base.json.stream.JsonWriter;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OpenJson - Unified JSON Processing Facade
 * OpenJson - 统一 JSON 处理门面
 *
 * <p>This is the main entry point for all JSON operations in the OpenCode JSON library.
 * It provides a unified API that delegates to pluggable JSON provider implementations
 * (Jackson, Gson, Fastjson2, etc.).</p>
 * <p>这是 OpenCode JSON 库中所有 JSON 操作的主入口点。
 * 它提供统一的 API，委托给可插拔的 JSON 提供者实现（Jackson、Gson、Fastjson2 等）。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Serialization/Deserialization - 序列化/反序列化</li>
 *   <li>Tree Model (JsonNode) - 树模型</li>
 *   <li>Streaming API - 流式 API</li>
 *   <li>JSONPath &amp; JSON Pointer - JSONPath 和 JSON Pointer</li>
 *   <li>JSON Patch &amp; Merge Patch - JSON Patch 和 Merge Patch</li>
 *   <li>JSON Schema Validation - JSON Schema 验证</li>
 *   <li>Custom Type Adapters - 自定义类型适配器</li>
 *   <li>Security Features - 安全特性</li>
 * </ul>
 *
 * <p><strong>Basic Usage | 基本用法:</strong></p>
 * <pre>{@code
 * // Serialize to JSON
 * String json = OpenJson.toJson(user);
 *
 * // Deserialize from JSON
 * User user = OpenJson.fromJson(json, User.class);
 *
 * // Parse to tree
 * JsonNode node = OpenJson.parse(json);
 *
 * // Query with JSONPath
 * List<JsonNode> results = OpenJson.select(node, "$.users[*].name");
 *
 * // Custom configuration
 * OpenJson json = OpenJson.withConfig(config);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unified facade for all JSON operations - 所有JSON操作的统一门面</li>
 *   <li>Pluggable provider architecture (Jackson, Gson, Fastjson2) - 可插拔提供者架构</li>
 *   <li>Static and instance API support - 支持静态和实例API</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // See class-level documentation for usage
 * // 参见类级文档了解用法
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public final class OpenJson {

    /**
     * Default instance using default configuration
     * 使用默认配置的默认实例
     */
    private static final OpenJson DEFAULT = new OpenJson(JsonConfig.DEFAULT);

    /**
     * The JSON configuration
     * JSON 配置
     */
    private final JsonConfig config;

    /**
     * The JSON provider
     * JSON 提供者
     */
    private final JsonProvider provider;

    /**
     * Registered modules
     * 注册的模块
     */
    private final List<JsonModule> modules = new ArrayList<>();

    /**
     * Mixin source for annotation mixing
     * 注解混入的混入源
     */
    private final MixinSource mixinSource = new MixinSource();

    /**
     * Named property filters
     * 命名属性过滤器
     */
    private final Map<String, PropertyFilter> propertyFilters = new ConcurrentHashMap<>();

    private OpenJson(JsonConfig config) {
        this.config = config;
        this.provider = JsonProviderFactory.getProvider(config);
    }

    private OpenJson(JsonConfig config, JsonProvider provider) {
        this.config = config;
        this.provider = provider;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a new OpenJson instance with the specified configuration.
     * 使用指定配置创建新的 OpenJson 实例。
     *
     * @param config the configuration - 配置
     * @return the OpenJson instance - OpenJson 实例
     */
    public static OpenJson withConfig(JsonConfig config) {
        return new OpenJson(config);
    }

    /**
     * Creates a new OpenJson instance with the specified provider.
     * 使用指定提供者创建新的 OpenJson 实例。
     *
     * @param providerName the provider name (e.g., "jackson", "gson") - 提供者名称
     * @return the OpenJson instance - OpenJson 实例
     */
    public static OpenJson withProvider(String providerName) {
        return new OpenJson(JsonConfig.DEFAULT, JsonProviderFactory.getProvider(providerName));
    }

    /**
     * Creates a new OpenJson instance with configuration and provider.
     * 使用配置和提供者创建新的 OpenJson 实例。
     *
     * @param config       the configuration - 配置
     * @param providerName the provider name - 提供者名称
     * @return the OpenJson instance - OpenJson 实例
     */
    public static OpenJson withConfigAndProvider(JsonConfig config, String providerName) {
        return new OpenJson(config, JsonProviderFactory.getProvider(providerName, config));
    }

    // ==================== Static Serialization ====================

    /**
     * Serializes an object to JSON string.
     * 将对象序列化为 JSON 字符串。
     *
     * @param obj the object to serialize - 要序列化的对象
     * @return the JSON string - JSON 字符串
     */
    public static String toJson(Object obj) {
        return DEFAULT.serialize(obj);
    }

    /**
     * Serializes an object to JSON bytes.
     * 将对象序列化为 JSON 字节数组。
     *
     * @param obj the object to serialize - 要序列化的对象
     * @return the JSON bytes - JSON 字节数组
     */
    public static byte[] toJsonBytes(Object obj) {
        return DEFAULT.serializeToBytes(obj);
    }

    /**
     * Serializes an object to an OutputStream.
     * 将对象序列化到输出流。
     *
     * @param obj    the object to serialize - 要序列化的对象
     * @param output the output stream - 输出流
     */
    public static void toJson(Object obj, OutputStream output) {
        DEFAULT.serialize(obj, output);
    }

    /**
     * Serializes an object to a Writer.
     * 将对象序列化到 Writer。
     *
     * @param obj    the object to serialize - 要序列化的对象
     * @param writer the writer - Writer
     */
    public static void toJson(Object obj, Writer writer) {
        DEFAULT.serialize(obj, writer);
    }

    /**
     * Serializes an object to pretty-printed JSON.
     * 将对象序列化为美化的 JSON。
     *
     * @param obj the object to serialize - 要序列化的对象
     * @return the pretty JSON string - 美化的 JSON 字符串
     */
    public static String toPrettyJson(Object obj) {
        return withConfig(JsonConfig.builder().prettyPrint().build()).serialize(obj);
    }

    // ==================== Static Deserialization ====================

    /**
     * Deserializes JSON string to an object.
     * 将 JSON 字符串反序列化为对象。
     *
     * @param json  the JSON string - JSON 字符串
     * @param clazz the target class - 目标类
     * @param <T>   the target type - 目标类型
     * @return the deserialized object - 反序列化的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return DEFAULT.deserialize(json, clazz);
    }

    /**
     * Deserializes JSON string using TypeReference.
     * 使用 TypeReference 反序列化 JSON 字符串。
     *
     * @param json          the JSON string - JSON 字符串
     * @param typeReference the type reference - 类型引用
     * @param <T>           the target type - 目标类型
     * @return the deserialized object - 反序列化的对象
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        return DEFAULT.deserialize(json, typeReference);
    }

    /**
     * Deserializes JSON bytes to an object.
     * 将 JSON 字节数组反序列化为对象。
     *
     * @param json  the JSON bytes - JSON 字节数组
     * @param clazz the target class - 目标类
     * @param <T>   the target type - 目标类型
     * @return the deserialized object - 反序列化的对象
     */
    public static <T> T fromJson(byte[] json, Class<T> clazz) {
        return DEFAULT.deserialize(json, clazz);
    }

    /**
     * Deserializes JSON bytes using TypeReference.
     * 使用 TypeReference 反序列化 JSON 字节数组。
     *
     * @param json          the JSON bytes - JSON 字节数组
     * @param typeReference the type reference - 类型引用
     * @param <T>           the target type - 目标类型
     * @return the deserialized object - 反序列化的对象
     */
    public static <T> T fromJson(byte[] json, TypeReference<T> typeReference) {
        return DEFAULT.deserialize(json, typeReference);
    }

    /**
     * Deserializes from InputStream.
     * 从输入流反序列化。
     *
     * @param input the input stream - 输入流
     * @param clazz the target class - 目标类
     * @param <T>   the target type - 目标类型
     * @return the deserialized object - 反序列化的对象
     */
    public static <T> T fromJson(InputStream input, Class<T> clazz) {
        return DEFAULT.deserialize(input, clazz);
    }

    /**
     * Deserializes from Reader.
     * 从 Reader 反序列化。
     *
     * @param reader the reader - Reader
     * @param clazz  the target class - 目标类
     * @param <T>    the target type - 目标类型
     * @return the deserialized object - 反序列化的对象
     */
    public static <T> T fromJson(Reader reader, Class<T> clazz) {
        return DEFAULT.deserialize(reader, clazz);
    }

    /**
     * Deserializes JSON array to List.
     * 将 JSON 数组反序列化为 List。
     *
     * @param json        the JSON string - JSON 字符串
     * @param elementType the element type - 元素类型
     * @param <T>         the element type - 元素类型
     * @return the list - 列表
     */
    public static <T> List<T> fromJsonArray(String json, Class<T> elementType) {
        return DEFAULT.deserializeArray(json, elementType);
    }

    /**
     * Deserializes JSON object to Map.
     * 将 JSON 对象反序列化为 Map。
     *
     * @param json      the JSON string - JSON 字符串
     * @param keyType   the key type - 键类型
     * @param valueType the value type - 值类型
     * @param <K>       the key type - 键类型
     * @param <V>       the value type - 值类型
     * @return the map - Map
     */
    public static <K, V> Map<K, V> fromJsonMap(String json, Class<K> keyType, Class<V> valueType) {
        return DEFAULT.deserializeMap(json, keyType, valueType);
    }

    // ==================== Static Tree Operations ====================

    /**
     * Parses JSON string to JsonNode tree.
     * 将 JSON 字符串解析为 JsonNode 树。
     *
     * @param json the JSON string - JSON 字符串
     * @return the root node - 根节点
     */
    public static JsonNode parse(String json) {
        return DEFAULT.parseTree(json);
    }

    /**
     * Parses JSON bytes to JsonNode tree.
     * 将 JSON 字节数组解析为 JsonNode 树。
     *
     * @param json the JSON bytes - JSON 字节数组
     * @return the root node - 根节点
     */
    public static JsonNode parse(byte[] json) {
        return DEFAULT.parseTree(json);
    }

    /**
     * Converts object to JsonNode tree.
     * 将对象转换为 JsonNode 树。
     *
     * @param obj the object - 对象
     * @return the JSON node - JSON 节点
     */
    public static JsonNode toTree(Object obj) {
        return DEFAULT.valueToTree(obj);
    }

    /**
     * Converts JsonNode to object.
     * 将 JsonNode 转换为对象。
     *
     * @param node  the JSON node - JSON 节点
     * @param clazz the target class - 目标类
     * @param <T>   the target type - 目标类型
     * @return the object - 对象
     */
    public static <T> T treeToValue(JsonNode node, Class<T> clazz) {
        return DEFAULT.treeToObject(node, clazz);
    }

    // ==================== Static Path Operations ====================

    /**
     * Evaluates a JSON Pointer against a node.
     * 对节点求值 JSON Pointer。
     *
     * @param node    the JSON node - JSON 节点
     * @param pointer the JSON Pointer string - JSON Pointer 字符串
     * @return the value at the pointer location - 指针位置的值
     */
    public static JsonNode at(JsonNode node, String pointer) {
        return JsonPointer.parse(pointer).evaluateOrNull(node);
    }

    /**
     * Selects nodes using JSONPath.
     * 使用 JSONPath 选择节点。
     *
     * @param node the JSON node - JSON 节点
     * @param path the JSONPath expression - JSONPath 表达式
     * @return matching nodes - 匹配的节点
     */
    public static List<JsonNode> select(JsonNode node, String path) {
        return JsonPath.read(node, path);
    }

    /**
     * Selects first matching node using JSONPath.
     * 使用 JSONPath 选择第一个匹配的节点。
     *
     * @param node the JSON node - JSON 节点
     * @param path the JSONPath expression - JSONPath 表达式
     * @return the first match, or null - 第一个匹配，或 null
     */
    public static JsonNode selectFirst(JsonNode node, String path) {
        return JsonPath.readFirst(node, path);
    }

    // ==================== Static Diff & Patch ====================

    /**
     * Computes diff between two JSON documents.
     * 计算两个 JSON 文档之间的差异。
     *
     * @param source the source document - 源文档
     * @param target the target document - 目标文档
     * @return the diff result - 差异结果
     */
    public static JsonDiff.DiffResult diff(JsonNode source, JsonNode target) {
        return JsonDiff.diff(source, target);
    }

    /**
     * Applies a JSON Patch to a document.
     * 将 JSON Patch 应用于文档。
     *
     * @param target the target document - 目标文档
     * @param patch  the patch - 补丁
     * @return the patched document - 打补丁后的文档
     */
    public static JsonNode patch(JsonNode target, JsonPatch patch) {
        return patch.apply(target);
    }

    /**
     * Applies a JSON Merge Patch to a document.
     * 将 JSON Merge Patch 应用于文档。
     *
     * @param target the target document - 目标文档
     * @param patch  the merge patch document - 合并补丁文档
     * @return the merged document - 合并后的文档
     */
    public static JsonNode mergePatch(JsonNode target, JsonNode patch) {
        return JsonMergePatch.apply(target, patch);
    }

    // ==================== Static Schema Validation ====================

    /**
     * Validates JSON against a schema.
     * 根据 schema 验证 JSON。
     *
     * @param data   the data to validate - 要验证的数据
     * @param schema the JSON Schema - JSON Schema
     * @return the validation result - 验证结果
     */
    public static JsonSchemaValidator.ValidationResult validate(JsonNode data, JsonNode schema) {
        return JsonSchemaValidator.validate(data, schema);
    }

    /**
     * Validates and throws if invalid.
     * 验证，如果无效则抛出异常。
     *
     * @param data   the data to validate - 要验证的数据
     * @param schema the JSON Schema - JSON Schema
     */
    public static void validateOrThrow(JsonNode data, JsonNode schema) {
        JsonSchemaValidator.validateOrThrow(data, schema);
    }

    // ==================== Static Utility Methods ====================

    /**
     * Checks whether the given string is valid JSON.
     * 检查给定字符串是否为合法 JSON。
     *
     * @param json the string to check - 要检查的字符串
     * @return true if valid JSON - 如果是合法 JSON 则返回 true
     */
    public static boolean isValid(String json) {
        return cloud.opencode.base.json.util.JsonStrings.isValid(json);
    }

    /**
     * Minifies a JSON string by removing unnecessary whitespace.
     * 通过移除不必要的空白来压缩 JSON 字符串。
     *
     * @param json the JSON string - JSON 字符串
     * @return the minified JSON - 压缩后的 JSON
     */
    public static String minify(String json) {
        return cloud.opencode.base.json.util.JsonStrings.minify(json);
    }

    /**
     * Pretty-prints a JSON string with standard 2-space indentation.
     * 使用标准 2 空格缩进美化 JSON 字符串。
     *
     * @param json the JSON string - JSON 字符串
     * @return the formatted JSON - 格式化后的 JSON
     */
    public static String prettyPrint(String json) {
        return cloud.opencode.base.json.util.JsonStrings.prettyPrint(json);
    }

    /**
     * Compares two JSON nodes for structural equality (ignoring object key order).
     * 比较两个 JSON 节点的结构相等性（忽略对象键顺序）。
     *
     * @param a the first node - 第一个节点
     * @param b the second node - 第二个节点
     * @return true if structurally equal - 如果结构相等则返回 true
     */
    public static boolean structuralEquals(JsonNode a, JsonNode b) {
        return cloud.opencode.base.json.util.JsonEquals.equals(a, b);
    }

    /**
     * Flattens a nested JSON node into a flat key-value map using dot notation.
     * 使用点号分隔将嵌套 JSON 节点扁平化为键值对 Map。
     *
     * @param node the JSON node to flatten - 要扁平化的 JSON 节点
     * @return the flattened map - 扁平化后的 Map
     */
    public static java.util.Map<String, JsonNode> flatten(JsonNode node) {
        return cloud.opencode.base.json.util.JsonFlattener.flatten(node);
    }

    /**
     * Restores a flattened map back to a nested JSON node.
     * 将扁平化的 Map 还原为嵌套 JSON 节点。
     *
     * @param map the flattened map - 扁平化的 Map
     * @return the nested JSON node - 嵌套的 JSON 节点
     */
    public static JsonNode unflatten(java.util.Map<String, JsonNode> map) {
        return cloud.opencode.base.json.util.JsonFlattener.unflatten(map);
    }

    /**
     * Produces RFC 8785 canonical JSON output for the given node.
     * 为给定节点生成 RFC 8785 规范化 JSON 输出。
     *
     * @param node the JSON node - JSON 节点
     * @return the canonical JSON string - 规范化 JSON 字符串
     */
    public static String canonicalize(JsonNode node) {
        return cloud.opencode.base.json.util.JsonCanonicalizer.canonicalize(node);
    }

    /**
     * Truncates a JSON string to the specified maximum length for logging.
     * 将 JSON 字符串截断到指定最大长度，用于日志记录。
     *
     * @param json      the JSON string - JSON 字符串
     * @param maxLength the maximum length - 最大长度
     * @return the truncated JSON - 截断后的 JSON
     */
    public static String truncate(String json, int maxLength) {
        return cloud.opencode.base.json.util.JsonTruncator.truncate(json, maxLength);
    }

    // ==================== Static Streaming ====================

    /**
     * Creates a JsonReader for an InputStream.
     * 为输入流创建 JsonReader。
     *
     * @param input the input stream - 输入流
     * @return the JSON reader - JSON 读取器
     */
    public static JsonReader createReader(InputStream input) {
        return DEFAULT.provider.createReader(input);
    }

    /**
     * Creates a JsonReader for a Reader.
     * 为 Reader 创建 JsonReader。
     *
     * @param reader the reader - Reader
     * @return the JSON reader - JSON 读取器
     */
    public static JsonReader createReader(Reader reader) {
        return DEFAULT.provider.createReader(reader);
    }

    /**
     * Creates a JsonWriter for an OutputStream.
     * 为输出流创建 JsonWriter。
     *
     * @param output the output stream - 输出流
     * @return the JSON writer - JSON 写入器
     */
    public static JsonWriter createWriter(OutputStream output) {
        return DEFAULT.provider.createWriter(output);
    }

    /**
     * Creates a JsonWriter for a Writer.
     * 为 Writer 创建 JsonWriter。
     *
     * @param writer the writer - Writer
     * @return the JSON writer - JSON 写入器
     */
    public static JsonWriter createWriter(Writer writer) {
        return DEFAULT.provider.createWriter(writer);
    }

    // ==================== Instance Methods ====================

    /**
     * Serializes an object to JSON string.
     * 将对象序列化为 JSON 字符串。
     *
     * @param obj the object - 对象
     * @return the JSON string - JSON 字符串
     */
    public String serialize(Object obj) {
        return provider.toJson(obj);
    }

    /**
     * Serializes an object to JSON bytes.
     * 将对象序列化为 JSON 字节数组。
     *
     * @param obj the object - 对象
     * @return the JSON bytes - JSON 字节数组
     */
    public byte[] serializeToBytes(Object obj) {
        return provider.toJsonBytes(obj);
    }

    /**
     * Serializes an object to an OutputStream.
     * 将对象序列化到输出流。
     *
     * @param obj    the object - 对象
     * @param output the output stream - 输出流
     */
    public void serialize(Object obj, OutputStream output) {
        provider.toJson(obj, output);
    }

    /**
     * Serializes an object to a Writer.
     * 将对象序列化到 Writer。
     *
     * @param obj    the object - 对象
     * @param writer the writer - Writer
     */
    public void serialize(Object obj, Writer writer) {
        provider.toJson(obj, writer);
    }

    /**
     * Deserializes JSON to an object.
     * 将 JSON 反序列化为对象。
     *
     * @param json  the JSON string - JSON 字符串
     * @param clazz the target class - 目标类
     * @param <T>   the target type - 目标类型
     * @return the object - 对象
     */
    public <T> T deserialize(String json, Class<T> clazz) {
        return provider.fromJson(json, clazz);
    }

    /**
     * Deserializes JSON using TypeReference.
     * 使用 TypeReference 反序列化 JSON。
     *
     * @param json          the JSON string - JSON 字符串
     * @param typeReference the type reference - 类型引用
     * @param <T>           the target type - 目标类型
     * @return the object - 对象
     */
    public <T> T deserialize(String json, TypeReference<T> typeReference) {
        return provider.fromJson(json, typeReference);
    }

    /**
     * Deserializes JSON bytes.
     * 反序列化 JSON 字节数组。
     *
     * @param json  the JSON bytes - JSON 字节数组
     * @param clazz the target class - 目标类
     * @param <T>   the target type - 目标类型
     * @return the object - 对象
     */
    public <T> T deserialize(byte[] json, Class<T> clazz) {
        return provider.fromJson(json, clazz);
    }

    /**
     * Deserializes JSON bytes using TypeReference.
     * 使用 TypeReference 反序列化 JSON 字节数组。
     *
     * @param json          the JSON bytes - JSON 字节数组
     * @param typeReference the type reference - 类型引用
     * @param <T>           the target type - 目标类型
     * @return the object - 对象
     */
    public <T> T deserialize(byte[] json, TypeReference<T> typeReference) {
        return provider.fromJson(json, typeReference);
    }

    /**
     * Deserializes from InputStream.
     * 从输入流反序列化。
     *
     * @param input the input stream - 输入流
     * @param clazz the target class - 目标类
     * @param <T>   the target type - 目标类型
     * @return the object - 对象
     */
    public <T> T deserialize(InputStream input, Class<T> clazz) {
        return provider.fromJson(input, clazz);
    }

    /**
     * Deserializes from Reader.
     * 从 Reader 反序列化。
     *
     * @param reader the reader - Reader
     * @param clazz  the target class - 目标类
     * @param <T>    the target type - 目标类型
     * @return the object - 对象
     */
    public <T> T deserialize(Reader reader, Class<T> clazz) {
        return provider.fromJson(reader, clazz);
    }

    /**
     * Deserializes JSON array to List.
     * 将 JSON 数组反序列化为 List。
     *
     * @param json        the JSON string - JSON 字符串
     * @param elementType the element type - 元素类型
     * @param <T>         the element type - 元素类型
     * @return the list - 列表
     */
    public <T> List<T> deserializeArray(String json, Class<T> elementType) {
        return provider.fromJsonArray(json, elementType);
    }

    /**
     * Deserializes JSON object to Map.
     * 将 JSON 对象反序列化为 Map。
     *
     * @param json      the JSON string - JSON 字符串
     * @param keyType   the key type - 键类型
     * @param valueType the value type - 值类型
     * @param <K>       the key type - 键类型
     * @param <V>       the value type - 值类型
     * @return the map - Map
     */
    public <K, V> Map<K, V> deserializeMap(String json, Class<K> keyType, Class<V> valueType) {
        return provider.fromJsonMap(json, keyType, valueType);
    }

    /**
     * Parses JSON to tree.
     * 将 JSON 解析为树。
     *
     * @param json the JSON string - JSON 字符串
     * @return the root node - 根节点
     */
    public JsonNode parseTree(String json) {
        return provider.parseTree(json);
    }

    /**
     * Parses JSON bytes to tree.
     * 将 JSON 字节数组解析为树。
     *
     * @param json the JSON bytes - JSON 字节数组
     * @return the root node - 根节点
     */
    public JsonNode parseTree(byte[] json) {
        return provider.parseTree(json);
    }

    /**
     * Converts object to tree.
     * 将对象转换为树。
     *
     * @param obj the object - 对象
     * @return the JSON node - JSON 节点
     */
    public JsonNode valueToTree(Object obj) {
        return provider.valueToTree(obj);
    }

    /**
     * Converts tree to object.
     * 将树转换为对象。
     *
     * @param node  the JSON node - JSON 节点
     * @param clazz the target class - 目标类
     * @param <T>   the target type - 目标类型
     * @return the object - 对象
     */
    public <T> T treeToObject(JsonNode node, Class<T> clazz) {
        return provider.treeToValue(node, clazz);
    }

    // ==================== Module & Mixin & Filter ====================

    /**
     * Registers a JSON module with this instance.
     * 向此实例注册 JSON 模块。
     *
     * <p>The module's {@link JsonModule#setupModule(JsonModule.SetupContext)}
     * method is called immediately to register all components.</p>
     * <p>模块的 {@link JsonModule#setupModule(JsonModule.SetupContext)}
     * 方法会被立即调用以注册所有组件。</p>
     *
     * @param module the module to register - 要注册的模块
     * @return this instance for chaining - 此实例，用于链式调用
     */
    public OpenJson registerModule(JsonModule module) {
        Objects.requireNonNull(module, "Module must not be null");
        modules.add(module);
        module.setupModule(new JsonModule.SetupContext() {
            @Override
            public void addTypeAdapter(cloud.opencode.base.json.adapter.JsonTypeAdapter<?> adapter) {
                JsonAdapterRegistry.register(adapter);
            }

            @Override
            public void addTypeAdapterFactory(cloud.opencode.base.json.adapter.JsonTypeAdapterFactory factory) {
                JsonAdapterRegistry.registerFactory(type -> {
                    if (type instanceof Class<?> clazz) {
                        return factory.create(clazz);
                    }
                    return null;
                });
            }

            @Override
            public void addMixin(Class<?> target, Class<?> mixin) {
                mixinSource.addMixin(target, mixin);
            }

            @Override
            @SuppressWarnings("unchecked")
            public void addKeyAdapter(Class<?> type, cloud.opencode.base.json.adapter.JsonTypeAdapter<?> adapter) {
                registerKeyAdapter((Class<Object>) type, (cloud.opencode.base.json.adapter.JsonTypeAdapter<Object>) adapter);
            }

            private <T> void registerKeyAdapter(Class<T> type, cloud.opencode.base.json.adapter.JsonTypeAdapter<T> adapter) {
                JsonAdapterRegistry.register(type, adapter);
            }

            @Override
            public JsonConfig getConfig() {
                return config;
            }
        });
        return this;
    }

    /**
     * Adds a mixin annotation class for a target type.
     * 为目标类型添加混入注解类。
     *
     * @param target the target class - 目标类
     * @param mixin  the mixin class containing annotations - 包含注解的混入类
     * @return this instance for chaining - 此实例，用于链式调用
     */
    public OpenJson addMixin(Class<?> target, Class<?> mixin) {
        mixinSource.addMixin(target, mixin);
        return this;
    }

    /**
     * Registers a named property filter.
     * 注册命名属性过滤器。
     *
     * @param filterId the filter identifier - 过滤器标识符
     * @param filter   the property filter - 属性过滤器
     * @return this instance for chaining - 此实例，用于链式调用
     */
    public OpenJson setPropertyFilter(String filterId, PropertyFilter filter) {
        Objects.requireNonNull(filterId, "Filter ID must not be null");
        Objects.requireNonNull(filter, "Filter must not be null");
        propertyFilters.put(filterId, filter);
        return this;
    }

    /**
     * Returns the mixin source.
     * 返回混入源。
     *
     * @return the mixin source - 混入源
     */
    public MixinSource getMixinSource() {
        return mixinSource;
    }

    /**
     * Returns the property filter for the given ID.
     * 返回给定 ID 的属性过滤器。
     *
     * @param filterId the filter identifier - 过滤器标识符
     * @return the filter, or null if not found - 过滤器，如果未找到则返回 null
     */
    public PropertyFilter getPropertyFilter(String filterId) {
        return propertyFilters.get(filterId);
    }

    /**
     * Returns the registered modules.
     * 返回注册的模块。
     *
     * @return unmodifiable list of modules - 不可修改的模块列表
     */
    public List<JsonModule> getModules() {
        return Collections.unmodifiableList(modules);
    }

    /**
     * Returns the configuration.
     * 返回配置。
     *
     * @return the config - 配置
     */
    public JsonConfig getConfig() {
        return config;
    }

    /**
     * Returns the provider.
     * 返回提供者。
     *
     * @return the provider - 提供者
     */
    public JsonProvider getProvider() {
        return provider;
    }
}
