
package cloud.opencode.base.json;

import cloud.opencode.base.json.path.JsonPath;
import cloud.opencode.base.json.path.JsonPointer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * JSON Node - Universal JSON Tree Representation
 * JSON 节点 - 通用 JSON 树表示
 *
 * <p>This class represents a JSON value in a tree model. It can hold
 * any JSON type: object, array, string, number, boolean, or null.</p>
 * <p>此类表示树模型中的 JSON 值。它可以保存任何 JSON 类型：
 * 对象、数组、字符串、数字、布尔值或 null。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Create nodes
 * JsonNode obj = JsonNode.object()
 *     .put("name", "John")
 *     .put("age", 30)
 *     .put("active", true);
 *
 * JsonNode arr = JsonNode.array()
 *     .add("one")
 *     .add(2)
 *     .add(true);
 *
 * // Access values
 * String name = obj.get("name").asString();
 * int age = obj.get("age").asInt();
 *
 * // Navigate with path
 * JsonNode value = obj.at("/name");
 * List<JsonNode> results = obj.select("$.name");
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sealed interface with six concrete types - 密封接口包含六种具体类型</li>
 *   <li>Tree model for JSON objects, arrays, strings, numbers, booleans, and nulls - JSON对象、数组、字符串、数字、布尔值和null的树模型</li>
 *   <li>Navigation via JSON Pointer and JSONPath - 通过JSON Pointer和JSONPath导航</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public sealed interface JsonNode permits JsonNode.ObjectNode, JsonNode.ArrayNode,
        JsonNode.StringNode, JsonNode.NumberNode, JsonNode.BooleanNode, JsonNode.NullNode {

    // ==================== Type Checking ====================

    /**
     * Returns whether this node is an object.
     * 返回此节点是否为对象。
     *
     * @return true if object - 如果是对象则返回 true
     */
    default boolean isObject() { return false; }

    /**
     * Returns whether this node is an array.
     * 返回此节点是否为数组。
     *
     * @return true if array - 如果是数组则返回 true
     */
    default boolean isArray() { return false; }

    /**
     * Returns whether this node is a string.
     * 返回此节点是否为字符串。
     *
     * @return true if string - 如果是字符串则返回 true
     */
    default boolean isString() { return false; }

    /**
     * Returns whether this node is a number.
     * 返回此节点是否为数字。
     *
     * @return true if number - 如果是数字则返回 true
     */
    default boolean isNumber() { return false; }

    /**
     * Returns whether this node is a boolean.
     * 返回此节点是否为布尔值。
     *
     * @return true if boolean - 如果是布尔值则返回 true
     */
    default boolean isBoolean() { return false; }

    /**
     * Returns whether this node is null.
     * 返回此节点是否为 null。
     *
     * @return true if null - 如果是 null 则返回 true
     */
    default boolean isNull() { return false; }

    /**
     * Returns whether this node represents a value (not container).
     * 返回此节点是否表示值（非容器）。
     *
     * @return true if value - 如果是值则返回 true
     */
    default boolean isValue() {
        return isString() || isNumber() || isBoolean() || isNull();
    }

    /**
     * Returns whether this node is a container (object or array).
     * 返回此节点是否为容器（对象或数组）。
     *
     * @return true if container - 如果是容器则返回 true
     */
    default boolean isContainer() {
        return isObject() || isArray();
    }

    // ==================== Value Access ====================

    /**
     * Returns this node's value as a string.
     * 返回此节点的值作为字符串。
     *
     * @return the string value - 字符串值
     */
    default String asString() { return null; }

    /**
     * Returns this node's value as an int.
     * 返回此节点的值作为 int。
     *
     * @return the int value - int 值
     */
    default int asInt() { return asInt(0); }

    /**
     * Returns this node's value as an int with default.
     * 返回此节点的值作为 int，带默认值。
     *
     * @param defaultValue the default value - 默认值
     * @return the int value - int 值
     */
    default int asInt(int defaultValue) { return defaultValue; }

    /**
     * Returns this node's value as a long.
     * 返回此节点的值作为 long。
     *
     * @return the long value - long 值
     */
    default long asLong() { return asLong(0L); }

    /**
     * Returns this node's value as a long with default.
     * 返回此节点的值作为 long，带默认值。
     *
     * @param defaultValue the default value - 默认值
     * @return the long value - long 值
     */
    default long asLong(long defaultValue) { return defaultValue; }

    /**
     * Returns this node's value as a double.
     * 返回此节点的值作为 double。
     *
     * @return the double value - double 值
     */
    default double asDouble() { return asDouble(0.0); }

    /**
     * Returns this node's value as a double with default.
     * 返回此节点的值作为 double，带默认值。
     *
     * @param defaultValue the default value - 默认值
     * @return the double value - double 值
     */
    default double asDouble(double defaultValue) { return defaultValue; }

    /**
     * Returns this node's value as a boolean.
     * 返回此节点的值作为布尔值。
     *
     * @return the boolean value - 布尔值
     */
    default boolean asBoolean() { return asBoolean(false); }

    /**
     * Returns this node's value as a boolean with default.
     * 返回此节点的值作为布尔值，带默认值。
     *
     * @param defaultValue the default value - 默认值
     * @return the boolean value - 布尔值
     */
    default boolean asBoolean(boolean defaultValue) { return defaultValue; }

    /**
     * Returns this node's value as a BigDecimal.
     * 返回此节点的值作为 BigDecimal。
     *
     * @return the BigDecimal value - BigDecimal 值
     */
    default BigDecimal asBigDecimal() { return null; }

    /**
     * Returns this node's value as a BigInteger.
     * 返回此节点的值作为 BigInteger。
     *
     * @return the BigInteger value - BigInteger 值
     */
    default BigInteger asBigInteger() { return null; }

    // ==================== Object/Array Access ====================

    /**
     * Gets a child node by property name (for objects).
     * 按属性名获取子节点（用于对象）。
     *
     * @param key the property name - 属性名
     * @return the child node, or null - 子节点，或 null
     */
    default JsonNode get(String key) { return null; }

    /**
     * Gets a child node by index (for arrays).
     * 按索引获取子节点（用于数组）。
     *
     * @param index the array index - 数组索引
     * @return the child node, or null - 子节点，或 null
     */
    default JsonNode get(int index) { return null; }

    /**
     * Returns whether this node has a property (for objects).
     * 返回此节点是否有属性（用于对象）。
     *
     * @param key the property name - 属性名
     * @return true if has property - 如果有属性则返回 true
     */
    default boolean has(String key) { return false; }

    /**
     * Returns the property names (for objects).
     * 返回属性名（用于对象）。
     *
     * @return the property names - 属性名
     */
    default Set<String> keys() { return Set.of(); }

    /**
     * Returns the number of children (for objects/arrays).
     * 返回子元素数量（用于对象/数组）。
     *
     * @return the size - 大小
     */
    default int size() { return 0; }

    /**
     * Returns whether this node is empty (for objects/arrays).
     * 返回此节点是否为空（用于对象/数组）。
     *
     * @return true if empty - 如果为空则返回 true
     */
    default boolean isEmpty() { return size() == 0; }

    // ==================== Path Navigation ====================

    /**
     * Navigates to a node using JSON Pointer (RFC 6901).
     * 使用 JSON Pointer (RFC 6901) 导航到节点。
     *
     * @param pointer the JSON Pointer string - JSON Pointer 字符串
     * @return the node at the pointer location - 指针位置的节点
     */
    default JsonNode at(String pointer) {
        return JsonPointer.parse(pointer).evaluateOrNull(this);
    }

    /**
     * Navigates to a node using JsonPointer.
     * 使用 JsonPointer 导航到节点。
     *
     * @param pointer the JSON Pointer - JSON Pointer
     * @return the node at the pointer location - 指针位置的节点
     */
    default JsonNode at(JsonPointer pointer) {
        return pointer.evaluateOrNull(this);
    }

    /**
     * Selects nodes using JSONPath expression.
     * 使用 JSONPath 表达式选择节点。
     *
     * @param path the JSONPath expression - JSONPath 表达式
     * @return matching nodes - 匹配的节点
     */
    default List<JsonNode> select(String path) {
        return JsonPath.read(this, path);
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a null node.
     * 创建 null 节点。
     *
     * @return the null node - null 节点
     */
    static JsonNode nullNode() {
        return NullNode.INSTANCE;
    }

    /**
     * Creates a node from a value.
     * 从值创建节点。
     *
     * @param value the value - 值
     * @return the node - 节点
     */
    static JsonNode of(Object value) {
        if (value == null) {
            return nullNode();
        }
        if (value instanceof JsonNode node) {
            return node;
        }
        if (value instanceof String s) {
            return new StringNode(s);
        }
        if (value instanceof Number n) {
            return new NumberNode(n);
        }
        if (value instanceof Boolean b) {
            return new BooleanNode(b);
        }
        throw new IllegalArgumentException("Cannot create JsonNode from: " + value.getClass());
    }

    /**
     * Creates a string node.
     * 创建字符串节点。
     *
     * @param value the string value - 字符串值
     * @return the node - 节点
     */
    static JsonNode of(String value) {
        return value == null ? nullNode() : new StringNode(value);
    }

    /**
     * Creates a number node.
     * 创建数字节点。
     *
     * @param value the number value - 数字值
     * @return the node - 节点
     */
    static JsonNode of(Number value) {
        return value == null ? nullNode() : new NumberNode(value);
    }

    /**
     * Creates a boolean node.
     * 创建布尔节点。
     *
     * @param value the boolean value - 布尔值
     * @return the node - 节点
     */
    static JsonNode of(boolean value) {
        return new BooleanNode(value);
    }

    /**
     * Creates an empty object node.
     * 创建空对象节点。
     *
     * @return the object node - 对象节点
     */
    static ObjectNode object() {
        return new ObjectNode();
    }

    /**
     * Creates an empty array node.
     * 创建空数组节点。
     *
     * @return the array node - 数组节点
     */
    static ArrayNode array() {
        return new ArrayNode();
    }

    // ==================== Node Implementations ====================

    /**
     * Object Node - Represents a JSON object.
     * 对象节点 - 表示 JSON 对象。
     */
    final class ObjectNode implements JsonNode {
        private final Map<String, JsonNode> properties = new LinkedHashMap<>();

        @Override public boolean isObject() { return true; }

        @Override
        public JsonNode get(String key) {
            return properties.get(key);
        }

        @Override
        public boolean has(String key) {
            return properties.containsKey(key);
        }

        @Override
        public Set<String> keys() {
            return Collections.unmodifiableSet(properties.keySet());
        }

        @Override
        public int size() {
            return properties.size();
        }

        public ObjectNode put(String key, JsonNode value) {
            properties.put(key, value != null ? value : nullNode());
            return this;
        }

        public ObjectNode put(String key, String value) {
            return put(key, of(value));
        }

        public ObjectNode put(String key, Number value) {
            return put(key, of(value));
        }

        public ObjectNode put(String key, boolean value) {
            return put(key, of(value));
        }

        public ObjectNode putNull(String key) {
            return put(key, nullNode());
        }

        public ObjectNode putObject(String key) {
            ObjectNode child = object();
            put(key, child);
            return child;
        }

        public ArrayNode putArray(String key) {
            ArrayNode child = array();
            put(key, child);
            return child;
        }

        public ObjectNode remove(String key) {
            properties.remove(key);
            return this;
        }

        public Map<String, JsonNode> toMap() {
            return new LinkedHashMap<>(properties);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ObjectNode that)) return false;
            return properties.equals(that.properties);
        }

        @Override
        public int hashCode() {
            return properties.hashCode();
        }

        @Override
        public String toString() {
            return properties.toString();
        }
    }

    /**
     * Array Node - Represents a JSON array.
     * 数组节点 - 表示 JSON 数组。
     */
    final class ArrayNode implements JsonNode, Iterable<JsonNode> {
        private final List<JsonNode> elements = new ArrayList<>();

        @Override public boolean isArray() { return true; }

        @Override
        public JsonNode get(int index) {
            if (index < 0 || index >= elements.size()) {
                return null;
            }
            return elements.get(index);
        }

        @Override
        public int size() {
            return elements.size();
        }

        public ArrayNode add(JsonNode value) {
            elements.add(value != null ? value : nullNode());
            return this;
        }

        public ArrayNode add(String value) {
            return add(of(value));
        }

        public ArrayNode add(Number value) {
            return add(of(value));
        }

        public ArrayNode add(boolean value) {
            return add(of(value));
        }

        public ArrayNode addNull() {
            return add(nullNode());
        }

        public ObjectNode addObject() {
            ObjectNode child = object();
            add(child);
            return child;
        }

        public ArrayNode addArray() {
            ArrayNode child = array();
            add(child);
            return child;
        }

        public ArrayNode set(int index, JsonNode value) {
            elements.set(index, value != null ? value : nullNode());
            return this;
        }

        public ArrayNode remove(int index) {
            elements.remove(index);
            return this;
        }

        public List<JsonNode> toList() {
            return new ArrayList<>(elements);
        }

        @Override
        public Iterator<JsonNode> iterator() {
            return Collections.unmodifiableList(elements).iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ArrayNode that)) return false;
            return elements.equals(that.elements);
        }

        @Override
        public int hashCode() {
            return elements.hashCode();
        }

        @Override
        public String toString() {
            return elements.toString();
        }
    }

    /**
     * String Node - Represents a JSON string.
     * 字符串节点 - 表示 JSON 字符串。
     */
    record StringNode(String value) implements JsonNode {
        @Override public boolean isString() { return true; }
        @Override public String asString() { return value; }
        @Override public int asInt(int defaultValue) {
            try { return Integer.parseInt(value); } catch (NumberFormatException e) { return defaultValue; }
        }
        @Override public long asLong(long defaultValue) {
            try { return Long.parseLong(value); } catch (NumberFormatException e) { return defaultValue; }
        }
        @Override public double asDouble(double defaultValue) {
            try { return Double.parseDouble(value); } catch (NumberFormatException e) { return defaultValue; }
        }
        @Override public boolean asBoolean(boolean defaultValue) {
            return Boolean.parseBoolean(value);
        }
    }

    /**
     * Number Node - Represents a JSON number.
     * 数字节点 - 表示 JSON 数字。
     */
    record NumberNode(Number value) implements JsonNode {
        @Override public boolean isNumber() { return true; }
        @Override public String asString() { return value.toString(); }
        @Override public int asInt(int defaultValue) { return value.intValue(); }
        @Override public long asLong(long defaultValue) { return value.longValue(); }
        @Override public double asDouble(double defaultValue) { return value.doubleValue(); }
        @Override public BigDecimal asBigDecimal() {
            if (value instanceof BigDecimal bd) return bd;
            if (value instanceof BigInteger bi) return new BigDecimal(bi);
            return new BigDecimal(value.toString());
        }
        @Override public BigInteger asBigInteger() {
            if (value instanceof BigInteger bi) return bi;
            if (value instanceof BigDecimal bd) return bd.toBigInteger();
            // Use BigDecimal as intermediary to avoid precision loss from longValue()
            // For example, a Double of 1e20 would overflow long
            return new BigDecimal(value.toString()).toBigInteger();
        }
    }

    /**
     * Boolean Node - Represents a JSON boolean.
     * 布尔节点 - 表示 JSON 布尔值。
     */
    record BooleanNode(boolean value) implements JsonNode {
        @Override public boolean isBoolean() { return true; }
        @Override public String asString() { return String.valueOf(value); }
        @Override public boolean asBoolean(boolean defaultValue) { return value; }
        @Override public int asInt(int defaultValue) { return value ? 1 : 0; }
    }

    /**
     * Null Node - Represents a JSON null.
     * Null 节点 - 表示 JSON null。
     */
    record NullNode() implements JsonNode {
        static final NullNode INSTANCE = new NullNode();
        @Override public boolean isNull() { return true; }
        @Override public String asString() { return null; }
    }
}
