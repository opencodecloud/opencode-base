package cloud.opencode.base.tree.traversal;

import cloud.opencode.base.tree.Treeable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Tree Traversal
 * 树遍历
 *
 * <p>Interface for tree traversal strategies.</p>
 * <p>树遍历策略接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Strategy interface for tree traversal - 树遍历策略接口</li>
 *   <li>Visitor-based traversal - 基于访问者的遍历</li>
 *   <li>Node collection support - 节点收集支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TreeTraversal traversal = PreOrderTraversal.getInstance();
 * traversal.traverse(roots, node -> System.out.println(node));
 * List<MyNode> all = traversal.collect(roots);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 取决于实现</li>
 *   <li>Null-safe: Implementation-dependent - 取决于实现</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public interface TreeTraversal {

    /**
     * Traverse tree nodes
     * 遍历树节点
     *
     * @param roots the root nodes | 根节点列表
     * @param visitor the visitor | 访问者
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     */
    <T extends Treeable<T, ID>, ID> void traverse(List<T> roots, TreeVisitor<T> visitor);

    /**
     * Traverse tree nodes with consumer
     * 使用消费者遍历树节点
     *
     * @param roots the root nodes | 根节点列表
     * @param consumer the consumer | 消费者
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     */
    default <T extends Treeable<T, ID>, ID> void traverse(List<T> roots, Consumer<T> consumer) {
        traverse(roots, TreeVisitor.of(consumer));
    }

    /**
     * Collect all nodes
     * 收集所有节点
     *
     * @param roots the root nodes | 根节点列表
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return all nodes | 所有节点
     */
    default <T extends Treeable<T, ID>, ID> List<T> collect(List<T> roots) {
        java.util.ArrayList<T> result = new java.util.ArrayList<>();
        traverse(roots, TreeVisitor.of(result::add));
        return result;
    }
}
