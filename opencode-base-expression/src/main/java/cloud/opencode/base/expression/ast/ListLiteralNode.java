package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.context.EvaluationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * List Literal Node
 * 列表字面量节点
 *
 * <p>Represents a list literal like {1, 2, 3}.</p>
 * <p>表示列表字面量，如{1, 2, 3}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Inline list construction in expressions - 表达式中的内联列表构造</li>
 *   <li>Support nested expressions as elements - 支持嵌套表达式作为元素</li>
 *   <li>Empty list creation - 空列表创建</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Node list = ListLiteralNode.of(
 *     LiteralNode.ofInt(1),
 *     LiteralNode.ofInt(2),
 *     LiteralNode.ofInt(3)
 * );
 * List<?> result = (List<?>) list.evaluate(ctx);  // [1, 2, 3]
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record with defensive copy - 线程安全: 是，不可变记录，防御性拷贝</li>
 *   <li>Null-safe: No, null elements list rejected - 空值安全: 否，null元素列表被拒绝</li>
 * </ul>
 *
 * @param elements the list elements | 列表元素
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public record ListLiteralNode(List<Node> elements) implements Node {

    public ListLiteralNode {
        Objects.requireNonNull(elements, "elements cannot be null");
        elements = List.copyOf(elements);
    }

    /**
     * Create list literal node
     * 创建列表字面量节点
     *
     * @param elements the elements | 元素
     * @return the list node | 列表节点
     */
    public static ListLiteralNode of(List<Node> elements) {
        return new ListLiteralNode(elements);
    }

    /**
     * Create list literal node from varargs
     * 从可变参数创建列表字面量节点
     *
     * @param elements the elements | 元素
     * @return the list node | 列表节点
     */
    public static ListLiteralNode of(Node... elements) {
        return new ListLiteralNode(List.of(elements));
    }

    /**
     * Create empty list literal node
     * 创建空列表字面量节点
     *
     * @return the empty list node | 空列表节点
     */
    public static ListLiteralNode empty() {
        return new ListLiteralNode(List.of());
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        List<Object> result = new ArrayList<>(elements.size());
        for (Node element : elements) {
            result.add(element.evaluate(context));
        }
        return result;
    }

    @Override
    public String toExpressionString() {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(elements.get(i).toExpressionString());
        }
        return sb.append("}").toString();
    }
}
