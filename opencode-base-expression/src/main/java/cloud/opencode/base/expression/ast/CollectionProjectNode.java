package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.EvaluationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Collection Project Node
 * 集合投影节点
 *
 * <p>Represents collection projection: users.![name]</p>
 * <p>表示集合投影：users.![name]</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Project/map a collection to extract a property from each element - 投影/映射集合以从每个元素提取属性</li>
 *   <li>Child context with #this binding per element - 每个元素绑定#this的子上下文</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Extract all user names: users.![name]
 * Node projection = CollectionProjectNode.of(usersNode, nameNode);
 * List<?> names = (List<?>) projection.evaluate(ctx);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record - 线程安全: 是，不可变记录</li>
 *   <li>Null-safe: Yes, null target returns empty list - 空值安全: 是，null目标返回空列表</li>
 * </ul>
 *
 * @param target the target collection node | 目标集合节点
 * @param projection the projection expression | 投影表达式
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public record CollectionProjectNode(Node target, Node projection) implements Node {

    public CollectionProjectNode {
        Objects.requireNonNull(target, "target cannot be null");
        Objects.requireNonNull(projection, "projection cannot be null");
    }

    /**
     * Create projection node
     * 创建投影节点
     *
     * @param target the target node | 目标节点
     * @param projection the projection expression | 投影表达式
     * @return the projection node | 投影节点
     */
    public static CollectionProjectNode of(Node target, Node projection) {
        return new CollectionProjectNode(target, projection);
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        Object targetObj = target.evaluate(context);

        if (targetObj == null) {
            return List.of();
        }

        if (!(targetObj instanceof Collection<?> collection)) {
            throw OpenExpressionException.typeError("collection", targetObj);
        }

        List<Object> result = new ArrayList<>(collection.size());

        for (Object element : collection) {
            // Create child context with #this bound to current element
            EvaluationContext childContext = context.createChild();
            childContext.setVariable("#this", element);

            Object projected = projection.evaluate(childContext);
            result.add(projected);
        }

        return result;
    }

    @Override
    public String toExpressionString() {
        return target.toExpressionString() + ".![" + projection.toExpressionString() + "]";
    }
}
