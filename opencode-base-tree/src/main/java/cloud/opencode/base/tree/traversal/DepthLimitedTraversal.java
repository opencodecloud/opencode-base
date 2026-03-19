package cloud.opencode.base.tree.traversal;

import cloud.opencode.base.tree.Treeable;

import java.util.List;

/**
 * Depth-Limited Traversal
 * 深度限制遍历
 *
 * <p>Traversal with maximum depth limit.</p>
 * <p>带最大深度限制的遍历。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pre-order traversal with depth limit - 带深度限制的先序遍历</li>
 *   <li>Configurable maximum depth - 可配置最大深度</li>
 *   <li>Early termination via visitor - 通过访问者提前终止</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DepthLimitedTraversal traversal = DepthLimitedTraversal.of(3);
 * traversal.traverse(roots, (node, depth) -> {
 *     System.out.println("  ".repeat(depth) + node);
 *     return true;
 * });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 是（构造后不可变）</li>
 *   <li>Null-safe: No (roots must not be null) - 否（根节点不能为null）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public class DepthLimitedTraversal implements TreeTraversal {

    private final int maxDepth;

    public DepthLimitedTraversal(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public static DepthLimitedTraversal of(int maxDepth) {
        return new DepthLimitedTraversal(maxDepth);
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
        if (depth > maxDepth) {
            return true; // Skip but continue
        }

        if (!visitor.visit(node, depth)) {
            return false;
        }

        if (depth < maxDepth) {
            List<T> children = node.getChildren();
            if (children != null) {
                for (T child : children) {
                    if (!traverseNode(child, visitor, depth + 1)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public int getMaxDepth() {
        return maxDepth;
    }
}
