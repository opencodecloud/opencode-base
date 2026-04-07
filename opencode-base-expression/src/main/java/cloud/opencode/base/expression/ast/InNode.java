package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.EvaluationContext;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Membership Test Node
 * 成员测试节点
 *
 * <p>Represents the {@code in} operator which tests whether a value is contained
 * in a collection, array, or map key set.</p>
 * <p>表示 {@code in} 运算符，用于测试一个值是否包含在集合、数组或映射键集中。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Collection membership test via {@link Collection#contains} - 通过 Collection.contains 测试集合成员</li>
 *   <li>Array membership test via linear scan - 通过线性扫描测试数组成员</li>
 *   <li>Map key membership test via {@link Map#containsKey} - 通过 Map.containsKey 测试映射键成员</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // status in {'active', 'pending', 'approved'}
 * Node in = InNode.of(statusNode, collectionNode);
 * Object result = in.evaluate(ctx);  // true or false
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record - 线程安全: 是，不可变记录</li>
 *   <li>Null-safe: No, value and collection required non-null - 空值安全: 否，值和集合要求非空</li>
 * </ul>
 *
 * @param value the value to test | 要测试的值
 * @param collection the collection to test against | 要测试的集合
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.3
 */
public record InNode(Node value, Node collection) implements Node {

    public InNode {
        Objects.requireNonNull(value, "value cannot be null");
        Objects.requireNonNull(collection, "collection cannot be null");
    }

    /**
     * Create membership test node
     * 创建成员测试节点
     *
     * @param value the value to test | 要测试的值
     * @param collection the collection to test against | 要测试的集合
     * @return the membership test node | 成员测试节点
     */
    public static InNode of(Node value, Node collection) {
        return new InNode(value, collection);
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        Object val = value.evaluate(context);
        Object col = collection.evaluate(context);

        if (col == null) {
            return false;
        }

        if (col instanceof Collection<?> c) {
            return c.contains(val);
        }

        if (col instanceof Map<?, ?> m) {
            return m.containsKey(val);
        }

        if (col.getClass().isArray()) {
            int length = Array.getLength(col);
            for (int i = 0; i < length; i++) {
                if (Objects.equals(val, Array.get(col, i))) {
                    return true;
                }
            }
            return false;
        }

        throw OpenExpressionException.typeError("collection, array, or map", col);
    }

    @Override
    public String toExpressionString() {
        return "(" + value.toExpressionString() + " in " + collection.toExpressionString() + ")";
    }
}
