package cloud.opencode.base.tree.serialization;

import cloud.opencode.base.tree.Treeable;
import cloud.opencode.base.tree.TreeNode;
import cloud.opencode.base.tree.exception.TreeException;

import java.util.*;
import java.util.function.Function;

/**
 * Tree Serializer
 * 树序列化器
 *
 * <p>Utility for serializing trees to JSON and XML formats without external dependencies.</p>
 * <p>无外部依赖的树序列化工具，支持JSON和XML格式。</p>
 *
 * <p><strong>Key Features | 主要功能:</strong></p>
 * <ul>
 *   <li>JSON export - Serialize to JSON format - JSON导出</li>
 *   <li>XML export - Serialize to XML format - XML导出</li>
 *   <li>Map conversion - Convert to/from Map - Map转换</li>
 *   <li>Custom field mapping - Configure field names - 自定义字段映射</li>
 *   <li>Pretty print - Formatted output - 格式化输出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Serialize to JSON - 序列化为JSON
 * String json = TreeSerializer.toJson(roots);
 *
 * // Serialize to XML - 序列化为XML
 * String xml = TreeSerializer.toXml(roots);
 *
 * // Convert to Maps - 转换为Map列表
 * List<Map<String, Object>> maps = TreeSerializer.toMaps(roots);
 *
 * // Custom config - 自定义配置
 * SerializerConfig config = SerializerConfig.builder()
 *     .prettyPrint(true)
 *     .childrenField("items")
 *     .build();
 * String json = TreeSerializer.toJson(roots, config);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Partial (null values serialized as "null") - 空值安全: 部分（null值序列化为"null"）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) - each node visited exactly once during recursive traversal - 时间复杂度: O(n) - 递归遍历中每个节点恰好访问一次</li>
 *   <li>Space complexity: O(n) - output StringBuilder and result structures proportional to total node count - 空间复杂度: O(n) - 输出 StringBuilder 和结果结构与总节点数成正比</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public final class TreeSerializer {

    private static final int MAX_DEPTH = 1000;
    private static final int MAX_VALUE_DEPTH = 32;

    private TreeSerializer() {
        // Utility class
    }

    // ==================== JSON Serialization | JSON序列化 ====================

    /**
     * Serialize tree to JSON
     * 将树序列化为JSON
     *
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param roots the root nodes | 根节点
     * @return the JSON string | JSON字符串
     */
    public static <T extends Treeable<T, ID>, ID> String toJson(List<T> roots) {
        return toJson(roots, SerializerConfig.defaultConfig());
    }

    /**
     * Serialize tree to JSON with config
     * 使用配置将树序列化为JSON
     *
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param roots the root nodes | 根节点
     * @param config the serializer config | 序列化配置
     * @return the JSON string | JSON字符串
     */
    public static <T extends Treeable<T, ID>, ID> String toJson(List<T> roots, SerializerConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < roots.size(); i++) {
            if (i > 0) sb.append(",");
            if (config.prettyPrint()) sb.append("\n");
            nodeToJson(roots.get(i), sb, config, config.prettyPrint() ? 1 : 0);
        }

        if (config.prettyPrint() && !roots.isEmpty()) sb.append("\n");
        sb.append("]");
        return sb.toString();
    }

    /**
     * Serialize single tree node to JSON
     * 将单个树节点序列化为JSON
     *
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param root the root node | 根节点
     * @return the JSON string | JSON字符串
     */
    public static <T extends Treeable<T, ID>, ID> String toJsonSingle(T root) {
        return toJsonSingle(root, SerializerConfig.defaultConfig());
    }

    /**
     * Serialize single tree node to JSON with config
     * 使用配置将单个树节点序列化为JSON
     *
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param root the root node | 根节点
     * @param config the serializer config | 序列化配置
     * @return the JSON string | JSON字符串
     */
    public static <T extends Treeable<T, ID>, ID> String toJsonSingle(T root, SerializerConfig config) {
        StringBuilder sb = new StringBuilder();
        nodeToJson(root, sb, config, 0);
        return sb.toString();
    }

    /**
     * Serialize TreeNode to JSON
     * 将TreeNode序列化为JSON
     *
     * @param <T> the data type | 数据类型
     * @param root the root node | 根节点
     * @param dataSerializer function to serialize data | 数据序列化函数
     * @return the JSON string | JSON字符串
     */
    public static <T> String treeNodeToJson(TreeNode<T> root, Function<T, Map<String, Object>> dataSerializer) {
        return treeNodeToJson(root, dataSerializer, SerializerConfig.defaultConfig());
    }

    /**
     * Serialize TreeNode to JSON with config
     * 使用配置将TreeNode序列化为JSON
     *
     * @param <T> the data type | 数据类型
     * @param root the root node | 根节点
     * @param dataSerializer function to serialize data | 数据序列化函数
     * @param config the serializer config | 序列化配置
     * @return the JSON string | JSON字符串
     */
    public static <T> String treeNodeToJson(TreeNode<T> root, Function<T, Map<String, Object>> dataSerializer,
                                            SerializerConfig config) {
        StringBuilder sb = new StringBuilder();
        treeNodeToJsonRecursive(root, dataSerializer, sb, config, 0);
        return sb.toString();
    }

    private static <T extends Treeable<T, ID>, ID> void nodeToJson(T node, StringBuilder sb,
                                                                    SerializerConfig config, int depth) {
        if (depth > MAX_DEPTH) {
            throw TreeException.maxDepthExceeded(MAX_DEPTH);
        }
        String indent = config.prettyPrint() ? getIndent(depth, config.indentSize()) : "";
        String childIndent = config.prettyPrint() ? getIndent(depth + 1, config.indentSize()) : "";
        String newline = config.prettyPrint() ? "\n" : "";

        sb.append(indent).append("{").append(newline);

        // ID field
        sb.append(childIndent).append("\"").append(config.idField()).append("\": ");
        appendValue(sb, node.getId());

        // Parent ID field
        if (config.includeParentId() && node.getParentId() != null) {
            sb.append(",").append(newline);
            sb.append(childIndent).append("\"").append(config.parentIdField()).append("\": ");
            appendValue(sb, node.getParentId());
        }

        // Custom fields extractor
        if (config.fieldExtractor() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> extra = ((Function<Object, Map<String, Object>>) config.fieldExtractor()).apply(node);
            if (extra != null && !extra.isEmpty()) {
                for (Map.Entry<String, Object> entry : extra.entrySet()) {
                    sb.append(",").append(newline);
                    sb.append(childIndent).append("\"").append(escapeJson(entry.getKey())).append("\": ");
                    appendValue(sb, entry.getValue());
                }
            }
        }

        // Children
        List<T> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            sb.append(",").append(newline);
            sb.append(childIndent).append("\"").append(config.childrenField()).append("\": [").append(newline);

            for (int i = 0; i < children.size(); i++) {
                if (i > 0) sb.append(",").append(newline);
                nodeToJson(children.get(i), sb, config, depth + 2);
            }

            sb.append(newline).append(childIndent).append("]");
        } else if (config.includeEmptyChildren()) {
            sb.append(",").append(newline);
            sb.append(childIndent).append("\"").append(config.childrenField()).append("\": []");
        }

        sb.append(newline).append(indent).append("}");
    }

    private static <T> void treeNodeToJsonRecursive(TreeNode<T> node, Function<T, Map<String, Object>> dataSerializer,
                                                     StringBuilder sb, SerializerConfig config, int depth) {
        if (depth > MAX_DEPTH) {
            throw TreeException.maxDepthExceeded(MAX_DEPTH);
        }
        String indent = config.prettyPrint() ? getIndent(depth, config.indentSize()) : "";
        String childIndent = config.prettyPrint() ? getIndent(depth + 1, config.indentSize()) : "";
        String newline = config.prettyPrint() ? "\n" : "";

        sb.append(indent).append("{").append(newline);

        // Data fields
        Map<String, Object> dataMap = dataSerializer.apply(node.getData());
        if (dataMap != null) {
            boolean first = true;
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                if (!first) sb.append(",").append(newline);
                first = false;
                sb.append(childIndent).append("\"").append(escapeJson(entry.getKey())).append("\": ");
                appendValue(sb, entry.getValue());
            }
        }

        // Children
        List<TreeNode<T>> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            if (dataMap != null && !dataMap.isEmpty()) sb.append(",").append(newline);
            sb.append(childIndent).append("\"").append(config.childrenField()).append("\": [").append(newline);

            for (int i = 0; i < children.size(); i++) {
                if (i > 0) sb.append(",").append(newline);
                treeNodeToJsonRecursive(children.get(i), dataSerializer, sb, config, depth + 2);
            }

            sb.append(newline).append(childIndent).append("]");
        }

        sb.append(newline).append(indent).append("}");
    }

    // ==================== XML Serialization | XML序列化 ====================

    /**
     * Serialize tree to XML
     * 将树序列化为XML
     *
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param roots the root nodes | 根节点
     * @return the XML string | XML字符串
     */
    public static <T extends Treeable<T, ID>, ID> String toXml(List<T> roots) {
        return toXml(roots, SerializerConfig.defaultConfig());
    }

    /**
     * Serialize tree to XML with config
     * 使用配置将树序列化为XML
     *
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param roots the root nodes | 根节点
     * @param config the serializer config | 序列化配置
     * @return the XML string | XML字符串
     */
    public static <T extends Treeable<T, ID>, ID> String toXml(List<T> roots, SerializerConfig config) {
        StringBuilder sb = new StringBuilder();

        if (config.includeXmlDeclaration()) {
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            if (config.prettyPrint()) sb.append("\n");
        }

        sb.append("<").append(config.rootElement()).append(">");
        if (config.prettyPrint()) sb.append("\n");

        for (T root : roots) {
            nodeToXml(root, sb, config, config.prettyPrint() ? 1 : 0);
        }

        sb.append("</").append(config.rootElement()).append(">");
        return sb.toString();
    }

    /**
     * Serialize single tree node to XML
     * 将单个树节点序列化为XML
     *
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param root the root node | 根节点
     * @return the XML string | XML字符串
     */
    public static <T extends Treeable<T, ID>, ID> String toXmlSingle(T root) {
        return toXmlSingle(root, SerializerConfig.defaultConfig());
    }

    /**
     * Serialize single tree node to XML with config
     * 使用配置将单个树节点序列化为XML
     *
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param root the root node | 根节点
     * @param config the serializer config | 序列化配置
     * @return the XML string | XML字符串
     */
    public static <T extends Treeable<T, ID>, ID> String toXmlSingle(T root, SerializerConfig config) {
        StringBuilder sb = new StringBuilder();

        if (config.includeXmlDeclaration()) {
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            if (config.prettyPrint()) sb.append("\n");
        }

        nodeToXml(root, sb, config, 0);
        return sb.toString();
    }

    /**
     * Serialize TreeNode to XML
     * 将TreeNode序列化为XML
     *
     * @param <T> the data type | 数据类型
     * @param root the root node | 根节点
     * @param dataSerializer function to serialize data | 数据序列化函数
     * @return the XML string | XML字符串
     */
    public static <T> String treeNodeToXml(TreeNode<T> root, Function<T, Map<String, Object>> dataSerializer) {
        return treeNodeToXml(root, dataSerializer, SerializerConfig.defaultConfig());
    }

    /**
     * Serialize TreeNode to XML with config
     * 使用配置将TreeNode序列化为XML
     *
     * @param <T> the data type | 数据类型
     * @param root the root node | 根节点
     * @param dataSerializer function to serialize data | 数据序列化函数
     * @param config the serializer config | 序列化配置
     * @return the XML string | XML字符串
     */
    public static <T> String treeNodeToXml(TreeNode<T> root, Function<T, Map<String, Object>> dataSerializer,
                                           SerializerConfig config) {
        StringBuilder sb = new StringBuilder();

        if (config.includeXmlDeclaration()) {
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            if (config.prettyPrint()) sb.append("\n");
        }

        treeNodeToXmlRecursive(root, dataSerializer, sb, config, 0);
        return sb.toString();
    }

    private static <T extends Treeable<T, ID>, ID> void nodeToXml(T node, StringBuilder sb,
                                                                   SerializerConfig config, int depth) {
        if (depth > MAX_DEPTH) {
            throw TreeException.maxDepthExceeded(MAX_DEPTH);
        }
        String indent = config.prettyPrint() ? getIndent(depth, config.indentSize()) : "";
        String childIndent = config.prettyPrint() ? getIndent(depth + 1, config.indentSize()) : "";
        String newline = config.prettyPrint() ? "\n" : "";

        sb.append(indent).append("<").append(config.nodeElement()).append(">").append(newline);

        // ID
        sb.append(childIndent).append("<").append(config.idField()).append(">");
        sb.append(escapeXml(String.valueOf(node.getId())));
        sb.append("</").append(config.idField()).append(">").append(newline);

        // Parent ID
        if (config.includeParentId() && node.getParentId() != null) {
            sb.append(childIndent).append("<").append(config.parentIdField()).append(">");
            sb.append(escapeXml(String.valueOf(node.getParentId())));
            sb.append("</").append(config.parentIdField()).append(">").append(newline);
        }

        // Custom fields
        if (config.fieldExtractor() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> extra = ((Function<Object, Map<String, Object>>) config.fieldExtractor()).apply(node);
            if (extra != null) {
                for (Map.Entry<String, Object> entry : extra.entrySet()) {
                    String xmlName = sanitizeXmlName(entry.getKey());
                    sb.append(childIndent).append("<").append(xmlName).append(">");
                    sb.append(escapeXml(String.valueOf(entry.getValue())));
                    sb.append("</").append(xmlName).append(">").append(newline);
                }
            }
        }

        // Children
        List<T> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            sb.append(childIndent).append("<").append(config.childrenField()).append(">").append(newline);
            for (T child : children) {
                nodeToXml(child, sb, config, depth + 2);
            }
            sb.append(childIndent).append("</").append(config.childrenField()).append(">").append(newline);
        } else if (config.includeEmptyChildren()) {
            sb.append(childIndent).append("<").append(config.childrenField()).append("/>").append(newline);
        }

        sb.append(indent).append("</").append(config.nodeElement()).append(">").append(newline);
    }

    private static <T> void treeNodeToXmlRecursive(TreeNode<T> node, Function<T, Map<String, Object>> dataSerializer,
                                                    StringBuilder sb, SerializerConfig config, int depth) {
        if (depth > MAX_DEPTH) {
            throw TreeException.maxDepthExceeded(MAX_DEPTH);
        }
        String indent = config.prettyPrint() ? getIndent(depth, config.indentSize()) : "";
        String childIndent = config.prettyPrint() ? getIndent(depth + 1, config.indentSize()) : "";
        String newline = config.prettyPrint() ? "\n" : "";

        sb.append(indent).append("<").append(config.nodeElement()).append(">").append(newline);

        // Data fields
        Map<String, Object> dataMap = dataSerializer.apply(node.getData());
        if (dataMap != null) {
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                String xmlName = sanitizeXmlName(entry.getKey());
                sb.append(childIndent).append("<").append(xmlName).append(">");
                sb.append(escapeXml(String.valueOf(entry.getValue())));
                sb.append("</").append(xmlName).append(">").append(newline);
            }
        }

        // Children
        List<TreeNode<T>> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            sb.append(childIndent).append("<").append(config.childrenField()).append(">").append(newline);
            for (TreeNode<T> child : children) {
                treeNodeToXmlRecursive(child, dataSerializer, sb, config, depth + 2);
            }
            sb.append(childIndent).append("</").append(config.childrenField()).append(">").append(newline);
        }

        sb.append(indent).append("</").append(config.nodeElement()).append(">").append(newline);
    }

    // ==================== Map Conversion | Map转换 ====================

    /**
     * Convert tree to list of maps
     * 将树转换为Map列表
     *
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param roots the root nodes | 根节点
     * @return the map list | Map列表
     */
    public static <T extends Treeable<T, ID>, ID> List<Map<String, Object>> toMaps(List<T> roots) {
        return toMaps(roots, SerializerConfig.defaultConfig());
    }

    /**
     * Convert tree to list of maps with config
     * 使用配置将树转换为Map列表
     *
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param roots the root nodes | 根节点
     * @param config the serializer config | 序列化配置
     * @return the map list | Map列表
     */
    public static <T extends Treeable<T, ID>, ID> List<Map<String, Object>> toMaps(List<T> roots,
                                                                                    SerializerConfig config) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (T root : roots) {
            result.add(nodeToMap(root, config));
        }
        return result;
    }

    /**
     * Convert single tree node to map
     * 将单个树节点转换为Map
     *
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param node the node | 节点
     * @return the map | Map
     */
    public static <T extends Treeable<T, ID>, ID> Map<String, Object> toMap(T node) {
        return nodeToMap(node, SerializerConfig.defaultConfig());
    }

    private static <T extends Treeable<T, ID>, ID> Map<String, Object> nodeToMap(T node, SerializerConfig config) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put(config.idField(), node.getId());

        if (config.includeParentId() && node.getParentId() != null) {
            map.put(config.parentIdField(), node.getParentId());
        }

        // Custom fields
        if (config.fieldExtractor() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> extra = ((Function<Object, Map<String, Object>>) config.fieldExtractor()).apply(node);
            if (extra != null) {
                map.putAll(extra);
            }
        }

        // Children
        List<T> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            List<Map<String, Object>> childMaps = new ArrayList<>();
            for (T child : children) {
                childMaps.add(nodeToMap(child, config));
            }
            map.put(config.childrenField(), childMaps);
        } else if (config.includeEmptyChildren()) {
            map.put(config.childrenField(), List.of());
        }

        return map;
    }

    /**
     * Flatten tree to list of maps (without hierarchy)
     * 将树扁平化为Map列表（无层级）
     *
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param roots the root nodes | 根节点
     * @return the flat map list | 扁平Map列表
     */
    public static <T extends Treeable<T, ID>, ID> List<Map<String, Object>> toFlatMaps(List<T> roots) {
        return toFlatMaps(roots, SerializerConfig.defaultConfig());
    }

    /**
     * Flatten tree to list of maps with config
     * 使用配置将树扁平化为Map列表
     *
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param roots the root nodes | 根节点
     * @param config the serializer config | 序列化配置
     * @return the flat map list | 扁平Map列表
     */
    public static <T extends Treeable<T, ID>, ID> List<Map<String, Object>> toFlatMaps(List<T> roots,
                                                                                        SerializerConfig config) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (T root : roots) {
            flattenToMaps(root, result, config);
        }
        return result;
    }

    private static <T extends Treeable<T, ID>, ID> void flattenToMaps(T node, List<Map<String, Object>> result,
                                                                       SerializerConfig config) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(config.idField(), node.getId());
        map.put(config.parentIdField(), node.getParentId());

        if (config.fieldExtractor() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> extra = ((Function<Object, Map<String, Object>>) config.fieldExtractor()).apply(node);
            if (extra != null) {
                map.putAll(extra);
            }
        }

        result.add(map);

        List<T> children = node.getChildren();
        if (children != null) {
            for (T child : children) {
                flattenToMaps(child, result, config);
            }
        }
    }

    // ==================== Utility Methods | 工具方法 ====================

    private static String getIndent(int depth, int indentSize) {
        return " ".repeat(depth * indentSize);
    }

    private static void appendValue(StringBuilder sb, Object value) {
        appendValue(sb, value, 0);
    }

    private static void appendValue(StringBuilder sb, Object value, int depth) {
        if (depth > MAX_VALUE_DEPTH) {
            sb.append("\"[max depth exceeded]\"");
            return;
        }
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            sb.append("\"").append(escapeJson((String) value)).append("\"");
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof Collection<?> collection) {
            sb.append("[");
            boolean first = true;
            for (Object item : collection) {
                if (!first) sb.append(",");
                first = false;
                appendValue(sb, item, depth + 1);
            }
            sb.append("]");
        } else if (value instanceof Map<?, ?> map) {
            sb.append("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escapeJson(String.valueOf(entry.getKey()))).append("\":");
                appendValue(sb, entry.getValue(), depth + 1);
            }
            sb.append("}");
        } else {
            sb.append("\"").append(escapeJson(String.valueOf(value))).append("\"");
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        // Fast path: scan for chars that need escaping; return original if clean
        boolean needsEscape = false;
        for (int i = 0, len = s.length(); i < len; i++) {
            char c = s.charAt(i);
            if (c < 0x20 || c == '"' || c == '\\') {
                needsEscape = true;
                break;
            }
        }
        if (!needsEscape) {
            return s;
        }
        // Slow path: build escaped string
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0, len = s.length(); i < len; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /**
     * XML element name pattern: must start with letter or underscore, followed by letters, digits, hyphens, dots, or underscores
     * XML 元素名称模式：必须以字母或下划线开头，后跟字母、数字、连字符、点或下划线
     */
    private static final java.util.regex.Pattern XML_NAME_PATTERN =
            java.util.regex.Pattern.compile("[a-zA-Z_][a-zA-Z0-9_.\\-]*");

    /**
     * Sanitize a string for use as an XML element name
     * 清理字符串以用作 XML 元素名称
     *
     * @param name the proposed element name | 建议的元素名称
     * @return the sanitized name | 清理后的名称
     */
    private static String sanitizeXmlName(String name) {
        if (name == null || name.isEmpty()) {
            return "field";
        }
        if (XML_NAME_PATTERN.matcher(name).matches()) {
            return name;
        }
        // Replace invalid chars with underscore
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i == 0) {
                sb.append(Character.isLetter(c) || c == '_' ? c : '_');
            } else {
                sb.append(Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.' ? c : '_');
            }
        }
        return sb.toString();
    }

    // ==================== Serializer Configuration | 序列化配置 ====================

    /**
     * Serializer Configuration
     * 序列化配置
     */
    public record SerializerConfig(
            String idField,
            String parentIdField,
            String childrenField,
            String nodeElement,
            String rootElement,
            boolean prettyPrint,
            int indentSize,
            boolean includeParentId,
            boolean includeEmptyChildren,
            boolean includeXmlDeclaration,
            Function<?, Map<String, Object>> fieldExtractor
    ) {
        /**
         * Compact constructor - sanitize XML element names to prevent injection
         * 紧凑构造器 - 清理 XML 元素名称以防注入
         */
        public SerializerConfig {
            idField = sanitizeXmlName(idField);
            parentIdField = sanitizeXmlName(parentIdField);
            childrenField = sanitizeXmlName(childrenField);
            nodeElement = sanitizeXmlName(nodeElement);
            rootElement = sanitizeXmlName(rootElement);
        }
        /**
         * Create default configuration
         * 创建默认配置
         *
         * @return the config | 配置
         */
        public static SerializerConfig defaultConfig() {
            return new SerializerConfig(
                    "id", "parentId", "children", "node", "tree",
                    true, 2, true, false, true, null
            );
        }

        /**
         * Create builder for configuration
         * 创建配置构建器
         *
         * @return the builder | 构建器
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * Configuration Builder
         * 配置构建器
         */
        public static class Builder {
            private String idField = "id";
            private String parentIdField = "parentId";
            private String childrenField = "children";
            private String nodeElement = "node";
            private String rootElement = "tree";
            private boolean prettyPrint = true;
            private int indentSize = 2;
            private boolean includeParentId = true;
            private boolean includeEmptyChildren = false;
            private boolean includeXmlDeclaration = true;
            private Function<?, Map<String, Object>> fieldExtractor;

            public Builder idField(String idField) {
                this.idField = idField;
                return this;
            }

            public Builder parentIdField(String parentIdField) {
                this.parentIdField = parentIdField;
                return this;
            }

            public Builder childrenField(String childrenField) {
                this.childrenField = childrenField;
                return this;
            }

            public Builder nodeElement(String nodeElement) {
                this.nodeElement = nodeElement;
                return this;
            }

            public Builder rootElement(String rootElement) {
                this.rootElement = rootElement;
                return this;
            }

            public Builder prettyPrint(boolean prettyPrint) {
                this.prettyPrint = prettyPrint;
                return this;
            }

            public Builder indentSize(int indentSize) {
                this.indentSize = indentSize;
                return this;
            }

            public Builder includeParentId(boolean includeParentId) {
                this.includeParentId = includeParentId;
                return this;
            }

            public Builder includeEmptyChildren(boolean includeEmptyChildren) {
                this.includeEmptyChildren = includeEmptyChildren;
                return this;
            }

            public Builder includeXmlDeclaration(boolean includeXmlDeclaration) {
                this.includeXmlDeclaration = includeXmlDeclaration;
                return this;
            }

            public <T> Builder fieldExtractor(Function<T, Map<String, Object>> fieldExtractor) {
                this.fieldExtractor = fieldExtractor;
                return this;
            }

            public SerializerConfig build() {
                return new SerializerConfig(
                        idField, parentIdField, childrenField, nodeElement, rootElement,
                        prettyPrint, indentSize, includeParentId, includeEmptyChildren,
                        includeXmlDeclaration, fieldExtractor
                );
            }
        }
    }
}
