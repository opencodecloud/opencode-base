package cloud.opencode.base.tree.traversal;

import cloud.opencode.base.tree.Treeable;

import java.util.List;

/**
 * Post-Order Traversal
 * 后序遍历
 *
 * <p>Visits children before node (left-right-root).</p>
 * <p>在节点之前访问子节点（左-右-根）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Post-order traversal (children before parent) - 后序遍历（子节点在父节点之前）</li>
 *   <li>Recursive implementation - 递归实现</li>
 *   <li>Singleton instance - 单例实例</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PostOrderTraversal.getInstance().traverse(roots, (node, depth) -> {
 *     System.out.println(node); // children visited first
 *     return true;
 * });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless singleton) - 是（无状态单例）</li>
 *   <li>Null-safe: No (roots must not be null) - 否（根节点不能为null）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public class PostOrderTraversal implements TreeTraversal {

    private static final PostOrderTraversal INSTANCE = new PostOrderTraversal();

    public static PostOrderTraversal getInstance() {
        return INSTANCE;
    }

    @Override
    public <T extends Treeable<T, ID>, ID> void traverse(List<T> roots, TreeVisitor<T> visitor) {
        for (T root : roots) {
            if (!traverseNode(root, visitor, 0)) {
                break;
            }
        }
    }

    private <T extends Treeable<T, ID>, ID> boolean traverseNode(T node, TreeVisitor<T> visitor, int depth) {
        List<T> children = node.getChildren();
        if (children != null) {
            for (T child : children) {
                if (!traverseNode(child, visitor, depth + 1)) {
                    return false;
                }
            }
        }

        return visitor.visit(node, depth);
    }
}
