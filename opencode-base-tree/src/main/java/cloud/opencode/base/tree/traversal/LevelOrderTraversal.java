package cloud.opencode.base.tree.traversal;

import cloud.opencode.base.tree.Treeable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Level-Order Traversal
 * 层序遍历
 *
 * <p>Visits nodes level by level (breadth-first).</p>
 * <p>逐层访问节点（广度优先）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Breadth-first level-order traversal - 广度优先层序遍历</li>
 *   <li>Queue-based implementation - 基于队列的实现</li>
 *   <li>Singleton instance - 单例实例</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LevelOrderTraversal.getInstance().traverse(roots, (node, depth) -> {
 *     System.out.println("Level " + depth + ": " + node);
 *     return true;
 * });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless singleton) - 是（无状态单例）</li>
 *   <li>Null-safe: Yes (null roots are handled) - 是（处理null根节点）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public class LevelOrderTraversal implements TreeTraversal {

    private static final LevelOrderTraversal INSTANCE = new LevelOrderTraversal();

    public static LevelOrderTraversal getInstance() {
        return INSTANCE;
    }

    @Override
    public <T extends Treeable<T, ID>, ID> void traverse(List<T> roots, TreeVisitor<T> visitor) {
        if (roots == null || roots.isEmpty()) {
            return;
        }

        Deque<NodeWithDepth<T>> queue = new ArrayDeque<>();
        for (T root : roots) {
            queue.addLast(new NodeWithDepth<>(root, 0));
        }

        while (!queue.isEmpty()) {
            NodeWithDepth<T> current = queue.pollFirst();
            if (!visitor.visit(current.node, current.depth)) {
                return;
            }

            List<T> children = current.node.getChildren();
            if (children != null) {
                for (T child : children) {
                    queue.addLast(new NodeWithDepth<>(child, current.depth + 1));
                }
            }
        }
    }

    private record NodeWithDepth<T>(T node, int depth) {}
}
