package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.EvaluationContext;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Index Access Node
 * 索引访问节点
 *
 * <p>Represents index access: list[0], array[i], map["key"]</p>
 * <p>表示索引访问：list[0], array[i], map["key"]</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>List and array index access - 列表和数组索引访问</li>
 *   <li>Map key access - Map键访问</li>
 *   <li>String charAt access - 字符串字符访问</li>
 *   <li>Null-safe access mode (?[]) - 空安全访问模式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Node list = IdentifierNode.of("items");
 * Node index = LiteralNode.ofInt(0);
 * Node access = IndexAccessNode.of(list, index);
 * Object first = access.evaluate(ctx);
 *
 * // Null-safe access
 * Node safe = IndexAccessNode.nullSafe(list, index);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record - 线程安全: 是，不可变记录</li>
 *   <li>Null-safe: Optional via nullSafe mode - 空值安全: 通过nullSafe模式可选</li>
 * </ul>
 *
 * @param target the target object node | 目标对象节点
 * @param index the index expression | 索引表达式
 * @param nullSafe whether to use null-safe access | 是否使用空安全访问
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public record IndexAccessNode(Node target, Node index, boolean nullSafe) implements Node {

    public IndexAccessNode {
        Objects.requireNonNull(target, "target cannot be null");
        Objects.requireNonNull(index, "index cannot be null");
    }

    /**
     * Create standard index access
     * 创建标准索引访问
     *
     * @param target the target node | 目标节点
     * @param index the index node | 索引节点
     * @return the index access node | 索引访问节点
     */
    public static IndexAccessNode of(Node target, Node index) {
        return new IndexAccessNode(target, index, false);
    }

    /**
     * Create null-safe index access
     * 创建空安全索引访问
     *
     * @param target the target node | 目标节点
     * @param index the index node | 索引节点
     * @return the null-safe index access node | 空安全索引访问节点
     */
    public static IndexAccessNode nullSafe(Node target, Node index) {
        return new IndexAccessNode(target, index, true);
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        Object targetObj = target.evaluate(context);

        if (targetObj == null) {
            if (nullSafe) {
                return null;
            }
            throw OpenExpressionException.nullPointer("index access on null");
        }

        Object indexVal = index.evaluate(context);

        // List access
        if (targetObj instanceof List<?> list) {
            int idx = toIndex(indexVal);
            if (idx < 0 || idx >= list.size()) {
                if (nullSafe) {
                    return null;
                }
                throw OpenExpressionException.evaluationError(
                    "Index out of bounds: " + idx + " for list of size " + list.size());
            }
            return list.get(idx);
        }

        // Array access
        if (targetObj.getClass().isArray()) {
            int idx = toIndex(indexVal);
            int length = Array.getLength(targetObj);
            if (idx < 0 || idx >= length) {
                if (nullSafe) {
                    return null;
                }
                throw OpenExpressionException.evaluationError(
                    "Index out of bounds: " + idx + " for array of length " + length);
            }
            return Array.get(targetObj, idx);
        }

        // Map access
        if (targetObj instanceof Map<?, ?> map) {
            return map.get(indexVal);
        }

        // String access (charAt)
        if (targetObj instanceof String str) {
            int idx = toIndex(indexVal);
            if (idx < 0 || idx >= str.length()) {
                if (nullSafe) {
                    return null;
                }
                throw OpenExpressionException.evaluationError(
                    "Index out of bounds: " + idx + " for string of length " + str.length());
            }
            return str.charAt(idx);
        }

        throw OpenExpressionException.typeError("indexable (list, array, map, string)", targetObj);
    }

    private int toIndex(Object indexVal) {
        if (indexVal instanceof Number n) {
            return n.intValue();
        }
        if (indexVal instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw OpenExpressionException.typeError("integer index", indexVal);
            }
        }
        throw OpenExpressionException.typeError("integer index", indexVal);
    }

    @Override
    public String toExpressionString() {
        return target.toExpressionString() + "[" + index.toExpressionString() + "]";
    }
}
