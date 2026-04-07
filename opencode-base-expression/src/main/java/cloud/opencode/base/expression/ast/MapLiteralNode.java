package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.context.EvaluationContext;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Map Literal Node
 * Map 字面量节点
 *
 * <p>Represents a map literal like {@code #{key: value, key2: value2}}.
 * Both keys and values are expression nodes that are evaluated at runtime.</p>
 * <p>表示 Map 字面量，如 {@code #{key: value, key2: value2}}。
 * 键和值都是在运行时求值的表达式节点。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Inline map construction in expressions - 表达式中的内联 Map 构造</li>
 *   <li>Preserves insertion order using LinkedHashMap - 使用 LinkedHashMap 保持插入顺序</li>
 *   <li>Support nested expressions as keys and values - 支持嵌套表达式作为键和值</li>
 *   <li>Empty map creation - 空 Map 创建</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Node map = MapLiteralNode.of(List.of(
 *     Map.entry(LiteralNode.ofString("name"), LiteralNode.ofString("John")),
 *     Map.entry(LiteralNode.ofString("age"), LiteralNode.ofInt(30))
 * ));
 * Map<?, ?> result = (Map<?, ?>) map.evaluate(ctx);
 * // {"name": "John", "age": 30}
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record with defensive copy - 线程安全: 是，不可变记录，防御性拷贝</li>
 *   <li>Null-safe: No, null entries list rejected - 空值安全: 否，null 条目列表被拒绝</li>
 * </ul>
 *
 * @param entries the key-value pair list | 键值对列表
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.3
 */
public record MapLiteralNode(List<Map.Entry<Node, Node>> entries) implements Node {

    public MapLiteralNode {
        Objects.requireNonNull(entries, "entries cannot be null");
        entries = entries.stream()
                .map(e -> (Map.Entry<Node, Node>) new SimpleImmutableEntry<>(e.getKey(), e.getValue()))
                .toList();
    }

    /**
     * Create map literal node
     * 创建 Map 字面量节点
     *
     * @param entries the key-value entries | 键值条目
     * @return the map literal node | Map 字面量节点
     */
    public static MapLiteralNode of(List<Map.Entry<Node, Node>> entries) {
        return new MapLiteralNode(entries);
    }

    /**
     * Create empty map literal node
     * 创建空 Map 字面量节点
     *
     * @return the empty map node | 空 Map 节点
     */
    public static MapLiteralNode empty() {
        return new MapLiteralNode(List.of());
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        LinkedHashMap<Object, Object> result = new LinkedHashMap<>(entries.size());
        for (Map.Entry<Node, Node> entry : entries) {
            Object key = entry.getKey().evaluate(context);
            Object value = entry.getValue().evaluate(context);
            result.put(key, value);
        }
        return result;
    }

    @Override
    public String toExpressionString() {
        StringBuilder sb = new StringBuilder("#{");
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(entries.get(i).getKey().toExpressionString());
            sb.append(": ");
            sb.append(entries.get(i).getValue().toExpressionString());
        }
        return sb.append("}").toString();
    }
}
