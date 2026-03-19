/*
 * Copyright 2025 OpenCode Cloud Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.yml.snakeyaml;

import cloud.opencode.base.yml.OpenYml;
import cloud.opencode.base.yml.YmlNode;
import cloud.opencode.base.yml.path.PathResolver;
import cloud.opencode.base.yml.path.YmlPath;

import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.*;

/**
 * SnakeYAML Node Implementation - Wraps SnakeYAML Node for YmlNode interface
 * SnakeYAML 节点实现 - 将 SnakeYAML 节点包装为 YmlNode 接口
 *
 * <p>This class provides a YmlNode implementation that wraps SnakeYAML's internal
 * Node representation, providing direct access to YAML parsing metadata like
 * tags, anchors, and line numbers.</p>
 * <p>此类提供 YmlNode 实现，包装 SnakeYAML 的内部 Node 表示，
 * 提供对 YAML 解析元数据（如标签、锚点和行号）的直接访问。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Access SnakeYAML metadata (tags, anchors, line numbers) - 访问 SnakeYAML 元数据（标签、锚点、行号）</li>
 *   <li>Full YmlNode interface implementation - 完整的 YmlNode 接口实现</li>
 *   <li>Line/column tracking for error reporting - 用于错误报告的行/列跟踪</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create from SnakeYAML node
 * Node snakeNode = yaml.compose(new StringReader(yamlStr));
 * SnakeYmlNode node = SnakeYmlNode.of(snakeNode);
 *
 * // Access values
 * String value = node.asText();
 *
 * // Get original SnakeYAML node
 * Node original = node.getSnakeNode();
 *
 * // Get line number (for error reporting)
 * int line = node.getStartLine();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (wraps mutable SnakeYAML nodes) - 线程安全: 否（包装可变的 SnakeYAML 节点）</li>
 *   <li>Null-safe: Yes (returns NULL_NODE for missing keys) - 空值安全: 是（缺失键返回空节点）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see YmlNode
 * @see SnakeYamlProvider
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public final class SnakeYmlNode implements YmlNode {

    private static final SnakeYmlNode NULL_NODE = new SnakeYmlNode(null, null);

    private final Node snakeNode;
    private final Object value;

    private SnakeYmlNode(Node snakeNode, Object value) {
        this.snakeNode = snakeNode;
        this.value = value;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a SnakeYmlNode from a SnakeYAML Node.
     * 从 SnakeYAML Node 创建 SnakeYmlNode。
     *
     * @param node the SnakeYAML node | SnakeYAML 节点
     * @return the wrapped node | 包装的节点
     */
    public static SnakeYmlNode of(Node node) {
        if (node == null) {
            return NULL_NODE;
        }
        Object value = extractValue(node);
        return new SnakeYmlNode(node, value);
    }

    /**
     * Creates a SnakeYmlNode from a raw value.
     * 从原始值创建 SnakeYmlNode。
     *
     * @param value the raw value | 原始值
     * @return the node | 节点
     */
    public static SnakeYmlNode ofValue(Object value) {
        if (value == null) {
            return NULL_NODE;
        }
        return new SnakeYmlNode(null, value);
    }

    /**
     * Returns the null node instance.
     * 返回空节点实例。
     *
     * @return null node | 空节点
     */
    public static SnakeYmlNode nullNode() {
        return NULL_NODE;
    }

    // ==================== SnakeYAML Specific Methods | SnakeYAML 特定方法 ====================

    /**
     * Gets the underlying SnakeYAML Node.
     * 获取底层 SnakeYAML 节点。
     *
     * @return the SnakeYAML node, or null if created from value | SnakeYAML 节点，如果从值创建则为 null
     */
    public Node getSnakeNode() {
        return snakeNode;
    }

    /**
     * Gets the YAML tag of this node.
     * 获取此节点的 YAML 标签。
     *
     * @return the tag, or null | 标签，或 null
     */
    public Tag getTag() {
        return snakeNode != null ? snakeNode.getTag() : null;
    }

    /**
     * Gets the tag string of this node.
     * 获取此节点的标签字符串。
     *
     * @return the tag string, or null | 标签字符串，或 null
     */
    public String getTagString() {
        Tag tag = getTag();
        return tag != null ? tag.getValue() : null;
    }

    /**
     * Gets the anchor of this node.
     * 获取此节点的锚点。
     *
     * @return the anchor, or null | 锚点，或 null
     */
    public String getAnchor() {
        return snakeNode != null ? snakeNode.getAnchor() : null;
    }

    /**
     * Gets the start line number (1-based).
     * 获取开始行号（从1开始）。
     *
     * @return the start line, or -1 if unknown | 开始行号，如果未知则为 -1
     */
    public int getStartLine() {
        if (snakeNode != null && snakeNode.getStartMark() != null) {
            return snakeNode.getStartMark().getLine() + 1;
        }
        return -1;
    }

    /**
     * Gets the start column number (1-based).
     * 获取开始列号（从1开始）。
     *
     * @return the start column, or -1 if unknown | 开始列号，如果未知则为 -1
     */
    public int getStartColumn() {
        if (snakeNode != null && snakeNode.getStartMark() != null) {
            return snakeNode.getStartMark().getColumn() + 1;
        }
        return -1;
    }

    /**
     * Gets the end line number (1-based).
     * 获取结束行号（从1开始）。
     *
     * @return the end line, or -1 if unknown | 结束行号，如果未知则为 -1
     */
    public int getEndLine() {
        if (snakeNode != null && snakeNode.getEndMark() != null) {
            return snakeNode.getEndMark().getLine() + 1;
        }
        return -1;
    }

    /**
     * Checks if this node has an anchor.
     * 检查此节点是否有锚点。
     *
     * @return true if has anchor | 如果有锚点则返回 true
     */
    public boolean hasAnchor() {
        return getAnchor() != null;
    }

    // ==================== YmlNode Implementation | YmlNode 实现 ====================

    @Override
    public NodeType getType() {
        if (value == null) {
            return NodeType.NULL;
        }
        if (value instanceof Map<?, ?>) {
            return NodeType.MAPPING;
        }
        if (value instanceof List<?>) {
            return NodeType.SEQUENCE;
        }
        return NodeType.SCALAR;
    }

    @Override
    public String asText() {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    @Override
    public String asText(String defaultValue) {
        String text = asText();
        return text != null ? text : defaultValue;
    }

    @Override
    public int asInt() {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(value.toString());
    }

    @Override
    public int asInt(int defaultValue) {
        try {
            return asInt();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public long asLong() {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(value.toString());
    }

    @Override
    public long asLong(long defaultValue) {
        try {
            return asLong();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public boolean asBoolean() {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        return Boolean.parseBoolean(value.toString());
    }

    @Override
    public boolean asBoolean(boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return asBoolean();
    }

    @Override
    public double asDouble() {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

    @Override
    public double asDouble(double defaultValue) {
        try {
            return asDouble();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public YmlNode get(String key) {
        if (value instanceof Map<?, ?> map) {
            Object child = ((Map<String, Object>) map).get(key);
            if (child == null) {
                return NULL_NODE;
            }
            // Try to get corresponding SnakeYAML node
            if (snakeNode instanceof MappingNode mappingNode) {
                for (NodeTuple tuple : mappingNode.getValue()) {
                    if (tuple.getKeyNode() instanceof ScalarNode keyNode) {
                        if (key.equals(keyNode.getValue())) {
                            return of(tuple.getValueNode());
                        }
                    }
                }
            }
            return ofValue(child);
        }
        return NULL_NODE;
    }

    @Override
    public YmlNode get(int index) {
        if (value instanceof List<?> list) {
            if (index >= 0 && index < list.size()) {
                Object child = list.get(index);
                // Try to get corresponding SnakeYAML node
                if (snakeNode instanceof SequenceNode sequenceNode) {
                    List<Node> nodes = sequenceNode.getValue();
                    if (index < nodes.size()) {
                        return of(nodes.get(index));
                    }
                }
                return ofValue(child);
            }
        }
        return NULL_NODE;
    }

    @Override
    public YmlNode at(String path) {
        Object result = PathResolver.resolve(value, YmlPath.of(path));
        return ofValue(result);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean has(String key) {
        if (value instanceof Map<?, ?> map) {
            return ((Map<String, Object>) map).containsKey(key);
        }
        return false;
    }

    @Override
    public int size() {
        if (value instanceof Map<?, ?> map) {
            return map.size();
        }
        if (value instanceof List<?> list) {
            return list.size();
        }
        return 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> keys() {
        if (value instanceof Map<?, ?> map) {
            return new LinkedHashSet<>(((Map<String, Object>) map).keySet());
        }
        return Collections.emptySet();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<YmlNode> values() {
        if (value instanceof Map<?, ?> map) {
            List<YmlNode> nodes = new ArrayList<>();
            if (snakeNode instanceof MappingNode mappingNode) {
                for (NodeTuple tuple : mappingNode.getValue()) {
                    nodes.add(of(tuple.getValueNode()));
                }
            } else {
                for (Object v : ((Map<String, Object>) map).values()) {
                    nodes.add(ofValue(v));
                }
            }
            return nodes;
        }
        if (value instanceof List<?> list) {
            List<YmlNode> nodes = new ArrayList<>();
            if (snakeNode instanceof SequenceNode sequenceNode) {
                for (Node node : sequenceNode.getValue()) {
                    nodes.add(of(node));
                }
            } else {
                for (Object v : list) {
                    nodes.add(ofValue(v));
                }
            }
            return nodes;
        }
        return Collections.emptyList();
    }

    @Override
    public <T> T toObject(Class<T> clazz) {
        return cloud.opencode.base.yml.bind.YmlBinder.bind(value, clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap() {
        if (value instanceof Map<?, ?> map) {
            return new LinkedHashMap<>((Map<String, Object>) map);
        }
        return Collections.emptyMap();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> toList() {
        if (value instanceof List<?> list) {
            return new ArrayList<>((List<Object>) list);
        }
        return Collections.emptyList();
    }

    @Override
    public String toYaml() {
        return OpenYml.dump(value);
    }

    @Override
    public Object getRawValue() {
        return value;
    }

    @Override
    public Iterator<YmlNode> iterator() {
        return values().iterator();
    }

    // ==================== Helper Methods | 辅助方法 ====================

    @SuppressWarnings("unchecked")
    private static Object extractValue(Node node) {
        if (node instanceof ScalarNode scalarNode) {
            return parseScalarValue(scalarNode);
        }
        if (node instanceof SequenceNode sequenceNode) {
            List<Object> list = new ArrayList<>();
            for (Node item : sequenceNode.getValue()) {
                list.add(extractValue(item));
            }
            return list;
        }
        if (node instanceof MappingNode mappingNode) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (NodeTuple tuple : mappingNode.getValue()) {
                String key = ((ScalarNode) tuple.getKeyNode()).getValue();
                Object value = extractValue(tuple.getValueNode());
                map.put(key, value);
            }
            return map;
        }
        return null;
    }

    private static Object parseScalarValue(ScalarNode node) {
        String value = node.getValue();
        Tag tag = node.getTag();

        if (Tag.NULL.equals(tag) || "null".equalsIgnoreCase(value)) {
            return null;
        }
        if (Tag.BOOL.equals(tag)) {
            return Boolean.parseBoolean(value);
        }
        if (Tag.INT.equals(tag)) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                return value;
            }
        }
        if (Tag.FLOAT.equals(tag)) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return value;
            }
        }
        return value;
    }

    @Override
    public String toString() {
        return "SnakeYmlNode{" +
               "type=" + getType() +
               ", value=" + value +
               (snakeNode != null ? ", line=" + getStartLine() : "") +
               "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnakeYmlNode that = (SnakeYmlNode) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
