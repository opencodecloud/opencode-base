package cloud.opencode.base.yml;

import java.util.*;

/**
 * YAML Node Interface - Represents a node in YAML document tree
 * YAML 节点接口 - 表示 YAML 文档树中的节点
 *
 * <p>This interface represents a node in the YAML document tree structure.
 * A node can be a scalar (string, number, boolean), sequence (list), or mapping (map).</p>
 * <p>此接口表示 YAML 文档树结构中的节点。
 * 节点可以是标量（字符串、数字、布尔值）、序列（列表）或映射（映射）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Typed value access (scalar, sequence, mapping, null) - 类型化值访问（标量、序列、映射、空）</li>
 *   <li>Dot-notation path navigation - 点号路径导航</li>
 *   <li>Object binding support - 对象绑定支持</li>
 *   <li>Iterable for sequence/mapping traversal - 可迭代的序列/映射遍历</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * YmlNode root = YmlDocument.parse(yaml).getRoot();
 *
 * // Check node type
 * if (root.isMapping()) {
 *     String name = root.get("name").asText();
 * }
 *
 * // Use path access
 * String city = root.at("address.city").asText();
 *
 * // Iterate sequence
 * for (YmlNode item : root.get("items")) {
 *     System.out.println(item.asText());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Yes (returns null node for missing keys) - 空值安全: 是（缺失键返回空节点）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public interface YmlNode extends Iterable<YmlNode> {

    /**
     * Creates a YmlNode from the given value.
     * 从给定值创建 YmlNode。
     *
     * @param value the raw value | 原始值
     * @return the node | 节点
     */
    static YmlNode of(Object value) {
        return DefaultYmlNode.of(value);
    }

    /**
     * Gets the node type.
     * 获取节点类型。
     *
     * @return the node type | 节点类型
     */
    NodeType getType();

    /**
     * Checks if this is a scalar node.
     * 检查是否为标量节点。
     *
     * @return true if scalar | 如果是标量则返回 true
     */
    default boolean isScalar() {
        return getType() == NodeType.SCALAR;
    }

    /**
     * Checks if this is a sequence node.
     * 检查是否为序列节点。
     *
     * @return true if sequence | 如果是序列则返回 true
     */
    default boolean isSequence() {
        return getType() == NodeType.SEQUENCE;
    }

    /**
     * Checks if this is a mapping node.
     * 检查是否为映射节点。
     *
     * @return true if mapping | 如果是映射则返回 true
     */
    default boolean isMapping() {
        return getType() == NodeType.MAPPING;
    }

    /**
     * Checks if this is a null node.
     * 检查是否为空节点。
     *
     * @return true if null | 如果是空则返回 true
     */
    default boolean isNull() {
        return getType() == NodeType.NULL;
    }

    // ==================== Value Access | 值访问 ====================

    /**
     * Gets the string value.
     * 获取字符串值。
     *
     * @return the string value | 字符串值
     */
    String asText();

    /**
     * Gets the string value with default.
     * 获取字符串值（带默认值）。
     *
     * @param defaultValue the default value | 默认值
     * @return the string value or default | 字符串值或默认值
     */
    String asText(String defaultValue);

    /**
     * Gets the integer value.
     * 获取整数值。
     *
     * @return the integer value | 整数值
     */
    int asInt();

    /**
     * Gets the integer value with default.
     * 获取整数值（带默认值）。
     *
     * @param defaultValue the default value | 默认值
     * @return the integer value or default | 整数值或默认值
     */
    int asInt(int defaultValue);

    /**
     * Gets the long value.
     * 获取长整数值。
     *
     * @return the long value | 长整数值
     */
    long asLong();

    /**
     * Gets the long value with default.
     * 获取长整数值（带默认值）。
     *
     * @param defaultValue the default value | 默认值
     * @return the long value or default | 长整数值或默认值
     */
    long asLong(long defaultValue);

    /**
     * Gets the boolean value.
     * 获取布尔值。
     *
     * @return the boolean value | 布尔值
     */
    boolean asBoolean();

    /**
     * Gets the boolean value with default.
     * 获取布尔值（带默认值）。
     *
     * @param defaultValue the default value | 默认值
     * @return the boolean value or default | 布尔值或默认值
     */
    boolean asBoolean(boolean defaultValue);

    /**
     * Gets the double value.
     * 获取双精度值。
     *
     * @return the double value | 双精度值
     */
    double asDouble();

    /**
     * Gets the double value with default.
     * 获取双精度值（带默认值）。
     *
     * @param defaultValue the default value | 默认值
     * @return the double value or default | 双精度值或默认值
     */
    double asDouble(double defaultValue);

    // ==================== Child Access | 子节点访问 ====================

    /**
     * Gets a child node by key (for mapping nodes).
     * 通过键获取子节点（用于映射节点）。
     *
     * @param key the key | 键
     * @return the child node, or null node if not found | 子节点，如果未找到则返回空节点
     */
    YmlNode get(String key);

    /**
     * Gets a child node by index (for sequence nodes).
     * 通过索引获取子节点（用于序列节点）。
     *
     * @param index the index | 索引
     * @return the child node | 子节点
     */
    YmlNode get(int index);

    /**
     * Gets a node using dot-notation path.
     * 使用点号路径获取节点。
     *
     * @param path the path (e.g., "a.b.c" or "items[0].name") | 路径（如："a.b.c" 或 "items[0].name"）
     * @return the node at path, or null node if not found | 路径处的节点，如果未找到则返回空节点
     */
    YmlNode at(String path);

    /**
     * Checks if the mapping contains a key.
     * 检查映射是否包含键。
     *
     * @param key the key | 键
     * @return true if contains | 如果包含则返回 true
     */
    boolean has(String key);

    /**
     * Gets the size (for sequence or mapping nodes).
     * 获取大小（用于序列或映射节点）。
     *
     * @return the size | 大小
     */
    int size();

    /**
     * Gets all keys (for mapping nodes).
     * 获取所有键（用于映射节点）。
     *
     * @return the keys | 键集合
     */
    Set<String> keys();

    /**
     * Gets all child nodes.
     * 获取所有子节点。
     *
     * @return the child nodes | 子节点列表
     */
    List<YmlNode> values();

    // ==================== Conversion | 转换 ====================

    /**
     * Converts to a Java object.
     * 转换为 Java 对象。
     *
     * @param clazz the target type | 目标类型
     * @param <T>   the type parameter | 类型参数
     * @return the object | 对象
     */
    <T> T toObject(Class<T> clazz);

    /**
     * Converts to a Map.
     * 转换为 Map。
     *
     * @return the Map | Map
     */
    Map<String, Object> toMap();

    /**
     * Converts to a List.
     * 转换为 List。
     *
     * @return the List | List
     */
    List<Object> toList();

    /**
     * Converts to a YAML string.
     * 转换为 YAML 字符串。
     *
     * @return the YAML string | YAML 字符串
     */
    String toYaml();

    /**
     * Gets the raw underlying value.
     * 获取原始底层值。
     *
     * @return the raw value | 原始值
     */
    Object getRawValue();

    // ==================== Node Type | 节点类型 ====================

    /**
     * Node Type - Types of YAML nodes
     * 节点类型 - YAML 节点的类型
     */
    enum NodeType {
        /**
         * Scalar node (string, number, boolean)
         * 标量节点（字符串、数字、布尔值）
         */
        SCALAR,

        /**
         * Sequence node (list)
         * 序列节点（列表）
         */
        SEQUENCE,

        /**
         * Mapping node (map)
         * 映射节点（映射）
         */
        MAPPING,

        /**
         * Null node
         * 空节点
         */
        NULL
    }
}
