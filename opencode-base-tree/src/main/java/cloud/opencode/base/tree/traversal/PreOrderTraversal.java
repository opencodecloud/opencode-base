package cloud.opencode.base.tree.traversal;

import cloud.opencode.base.tree.Treeable;

import java.util.List;

/**
 * Pre-Order Traversal
 * 先序遍历
 *
 * <p>Visits node before its children (root-left-right).</p>
 * <p>在子节点之前访问节点（根-左-右）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pre-order traversal (parent before children) - 先序遍历（父节点在子节点之前）</li>
 *   <li>Recursive implementation - 递归实现</li>
 *   <li>Singleton instance - 单例实例</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PreOrderTraversal.getInstance().traverse(roots, (node, depth) -> {
 *     System.out.println(node); // parent visited first
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
public class PreOrderTraversal implements TreeTraversal {

    private static final PreOrderTraversal INSTANCE = new PreOrderTraversal();

    public static PreOrderTraversal getInstance() {
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
        if (!visitor.visit(node, depth)) {
            return false;
        }

        List<T> children = node.getChildren();
        if (children != null) {
            for (T child : children) {
                if (!traverseNode(child, visitor, depth + 1)) {
                    return false;
                }
            }
        }
        return true;
    }
}
