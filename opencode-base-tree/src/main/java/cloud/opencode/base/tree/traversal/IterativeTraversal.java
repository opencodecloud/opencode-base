package cloud.opencode.base.tree.traversal;

import cloud.opencode.base.tree.Treeable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Iterative Traversal
 * 迭代式遍历
 *
 * <p>Stack-based iterative traversal to prevent stack overflow.</p>
 * <p>基于栈的迭代遍历，防止栈溢出。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Stack-based iterative pre-order traversal - 基于栈的迭代先序遍历</li>
 *   <li>No stack overflow risk - 无栈溢出风险</li>
 *   <li>Singleton instance - 单例实例</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * IterativeTraversal.getInstance().traverse(roots, (node, depth) -> {
 *     System.out.println(node);
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
public class IterativeTraversal implements TreeTraversal {

    private static final IterativeTraversal INSTANCE = new IterativeTraversal();

    public static IterativeTraversal getInstance() {
        return INSTANCE;
    }

    @Override
    public <T extends Treeable<T, ID>, ID> void traverse(List<T> roots, TreeVisitor<T> visitor) {
        if (roots == null || roots.isEmpty()) {
            return;
        }

        Deque<NodeWithDepth<T>> stack = new ArrayDeque<>();

        // Add roots in reverse order for correct pre-order traversal
        for (int i = roots.size() - 1; i >= 0; i--) {
            stack.push(new NodeWithDepth<>(roots.get(i), 0));
        }

        while (!stack.isEmpty()) {
            NodeWithDepth<T> current = stack.pop();
            if (!visitor.visit(current.node, current.depth)) {
                return;
            }

            List<T> children = current.node.getChildren();
            if (children != null) {
                // Add children in reverse order
                for (int i = children.size() - 1; i >= 0; i--) {
                    stack.push(new NodeWithDepth<>(children.get(i), current.depth + 1));
                }
            }
        }
    }

    private record NodeWithDepth<T>(T node, int depth) {}
}
