package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.EvaluationContext;
import cloud.opencode.base.expression.eval.TypeCoercion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Collection Filter Node
 * 集合过滤节点
 *
 * <p>Represents collection filtering: users.?[age > 18]</p>
 * <p>表示集合过滤：users.?[age > 18]</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Filter all matching elements (.?[]) - 过滤所有匹配元素</li>
 *   <li>Select first matching element (.^[]) - 选择第一个匹配元素</li>
 *   <li>Select last matching element (.$[]) - 选择最后一个匹配元素</li>
 *   <li>Child context with #this binding per element - 每个元素绑定#this的子上下文</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Filter users older than 18
 * Node filter = CollectionFilterNode.all(usersNode, predicateNode);
 * List<?> result = (List<?>) filter.evaluate(ctx);
 *
 * // Get first match
 * Node first = CollectionFilterNode.first(usersNode, predicateNode);
 * Object firstMatch = first.evaluate(ctx);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record - 线程安全: 是，不可变记录</li>
 *   <li>Null-safe: Yes, null target returns empty list or null - 空值安全: 是，null目标返回空列表或null</li>
 * </ul>
 *
 * @param target the target collection node | 目标集合节点
 * @param predicate the filter predicate | 过滤谓词
 * @param mode the filter mode (ALL, FIRST, LAST) | 过滤模式
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public record CollectionFilterNode(Node target, Node predicate, FilterMode mode) implements Node {

    /**
     * Filter mode enumeration
     * 过滤模式枚举
     */
    public enum FilterMode {
        /** Select all matching elements | 选择所有匹配元素 */
        ALL,
        /** Select first matching element | 选择第一个匹配元素 */
        FIRST,
        /** Select last matching element | 选择最后一个匹配元素 */
        LAST
    }

    public CollectionFilterNode {
        Objects.requireNonNull(target, "target cannot be null");
        Objects.requireNonNull(predicate, "predicate cannot be null");
        if (mode == null) {
            mode = FilterMode.ALL;
        }
    }

    /**
     * Create filter node (default: all matching elements)
     * 创建过滤节点（默认：所有匹配元素）
     *
     * @param target the target node | 目标节点
     * @param predicate the predicate | 谓词
     * @return the filter node | 过滤节点
     */
    public static CollectionFilterNode of(Node target, Node predicate) {
        return new CollectionFilterNode(target, predicate, FilterMode.ALL);
    }

    /**
     * Create filter node for all matching elements
     * 创建选择所有匹配元素的过滤节点
     *
     * @param target the target node | 目标节点
     * @param predicate the predicate | 谓词
     * @return the filter node | 过滤节点
     */
    public static CollectionFilterNode all(Node target, Node predicate) {
        return new CollectionFilterNode(target, predicate, FilterMode.ALL);
    }

    /**
     * Create filter node for first matching element
     * 创建选择第一个匹配元素的过滤节点
     *
     * @param target the target node | 目标节点
     * @param predicate the predicate | 谓词
     * @return the filter node | 过滤节点
     */
    public static CollectionFilterNode first(Node target, Node predicate) {
        return new CollectionFilterNode(target, predicate, FilterMode.FIRST);
    }

    /**
     * Create filter node for last matching element
     * 创建选择最后一个匹配元素的过滤节点
     *
     * @param target the target node | 目标节点
     * @param predicate the predicate | 谓词
     * @return the filter node | 过滤节点
     */
    public static CollectionFilterNode last(Node target, Node predicate) {
        return new CollectionFilterNode(target, predicate, FilterMode.LAST);
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        Object targetObj = target.evaluate(context);

        if (targetObj == null) {
            return mode == FilterMode.ALL ? List.of() : null;
        }

        if (!(targetObj instanceof Collection<?> collection)) {
            throw OpenExpressionException.typeError("collection", targetObj);
        }

        List<Object> result = new ArrayList<>();
        Object lastMatch = null;

        for (Object element : collection) {
            // Create child context with #this bound to current element
            EvaluationContext childContext = context.createChild();
            childContext.setVariable("#this", element);

            Object predicateResult = predicate.evaluate(childContext);
            if (TypeCoercion.toBoolean(predicateResult)) {
                if (mode == FilterMode.FIRST) {
                    return element;
                }
                lastMatch = element;
                if (mode == FilterMode.ALL) {
                    result.add(element);
                }
            }
        }

        return switch (mode) {
            case ALL -> result;
            case FIRST -> null;
            case LAST -> lastMatch;
        };
    }

    @Override
    public String toExpressionString() {
        String symbol = switch (mode) {
            case ALL -> ".?";
            case FIRST -> ".^";
            case LAST -> ".$";
        };
        return target.toExpressionString() + symbol + "[" + predicate.toExpressionString() + "]";
    }
}
