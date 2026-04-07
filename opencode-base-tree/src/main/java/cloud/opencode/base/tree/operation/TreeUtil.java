package cloud.opencode.base.tree.operation;

import cloud.opencode.base.tree.Treeable;
import cloud.opencode.base.tree.traversal.PreOrderTraversal;
import cloud.opencode.base.tree.traversal.TreeVisitor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Tree Util
 * 树工具类
 *
 * <p>Utility methods for tree operations.</p>
 * <p>树操作的工具方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Find nodes by ID or predicate - 通过ID或谓词查找节点</li>
 *   <li>Flatten tree to list - 将树扁平化为列表</li>
 *   <li>Count nodes and get depth - 统计节点和获取深度</li>
 *   <li>Get leaf nodes - 获取叶子节点</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Optional<MyNode> node = TreeUtil.findById(roots, targetId);
 * List<MyNode> flat = TreeUtil.flatten(roots);
 * int count = TreeUtil.count(roots);
 * int depth = TreeUtil.getMaxDepth(roots);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 否</li>
 *   <li>Null-safe: No (roots must not be null) - 否（根节点不能为null）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) - flatten/count/depth visit all nodes; find exits early on match - 时间复杂度: O(n) - flatten/count/depth 遍历全部节点；find 在匹配时提前退出</li>
 *   <li>Space complexity: O(n) for flatten result; O(h) recursion stack where h is tree height - 空间复杂度: flatten 结果 O(n)；递归栈 O(h)，h 为树高</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public final class TreeUtil {

    private TreeUtil() {
        // Utility class
    }

    /**
     * Find node by ID
     * 通过ID查找节点
     *
     * @param roots the root nodes | 根节点列表
     * @param id the ID to find | 要查找的ID
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the found node | 找到的节点
     */
    public static <T extends Treeable<T, ID>, ID> Optional<T> findById(List<T> roots, ID id) {
        Objects.requireNonNull(id, "id must not be null");
        return find(roots, node -> id.equals(node.getId()));
    }

    /**
     * Find node by predicate
     * 通过谓词查找节点
     *
     * @param roots the root nodes | 根节点列表
     * @param predicate the predicate | 谓词
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the found node | 找到的节点
     */
    public static <T extends Treeable<T, ID>, ID> Optional<T> find(List<T> roots, Predicate<T> predicate) {
        for (T root : roots) {
            Optional<T> found = findInNode(root, predicate);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    private static <T extends Treeable<T, ID>, ID> Optional<T> findInNode(T node, Predicate<T> predicate) {
        // Iterative DFS to prevent StackOverflow on deep trees
        Deque<T> stack = new ArrayDeque<>();
        stack.push(node);
        while (!stack.isEmpty()) {
            T current = stack.pop();
            if (predicate.test(current)) {
                return Optional.of(current);
            }
            List<T> children = current.getChildren();
            if (children != null) {
                for (int i = children.size() - 1; i >= 0; i--) {
                    stack.push(children.get(i));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Find all nodes matching predicate
     * 查找所有匹配谓词的节点
     *
     * @param roots the root nodes | 根节点列表
     * @param predicate the predicate | 谓词
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the matching nodes | 匹配的节点
     */
    public static <T extends Treeable<T, ID>, ID> List<T> findAll(List<T> roots, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        PreOrderTraversal.getInstance().traverse(roots, TreeVisitor.of(node -> {
            if (predicate.test(node)) {
                result.add(node);
            }
        }));
        return result;
    }

    /**
     * Flatten tree to list
     * 将树扁平化为列表
     *
     * @param roots the root nodes | 根节点列表
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the flattened list | 扁平化的列表
     */
    public static <T extends Treeable<T, ID>, ID> List<T> flatten(List<T> roots) {
        return PreOrderTraversal.getInstance().collect(roots);
    }

    /**
     * Count all nodes
     * 统计所有节点
     *
     * @param roots the root nodes | 根节点列表
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the node count | 节点数量
     */
    public static <T extends Treeable<T, ID>, ID> int count(List<T> roots) {
        int[] count = {0};
        PreOrderTraversal.getInstance().traverse(roots, (node, depth) -> {
            count[0]++;
            return true;
        });
        return count[0];
    }

    /**
     * Get max depth
     * 获取最大深度
     *
     * @param roots the root nodes | 根节点列表
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the max depth | 最大深度
     */
    public static <T extends Treeable<T, ID>, ID> int getMaxDepth(List<T> roots) {
        int[] maxDepth = {0};
        PreOrderTraversal.getInstance().traverse(roots, (node, depth) -> {
            maxDepth[0] = Math.max(maxDepth[0], depth);
            return true;
        });
        return maxDepth[0];
    }

    /**
     * Get all leaf nodes
     * 获取所有叶子节点
     *
     * @param roots the root nodes | 根节点列表
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the leaf nodes | 叶子节点
     */
    public static <T extends Treeable<T, ID>, ID> List<T> getLeaves(List<T> roots) {
        return findAll(roots, node -> {
            List<T> children = node.getChildren();
            return children == null || children.isEmpty();
        });
    }

    /**
     * Check if tree contains node with ID
     * 检查树是否包含指定ID的节点
     *
     * @param roots the root nodes | 根节点列表
     * @param id the ID to check | 要检查的ID
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return true if contains | 如果包含返回true
     */
    public static <T extends Treeable<T, ID>, ID> boolean contains(List<T> roots, ID id) {
        return findById(roots, id).isPresent();
    }

    /**
     * Extract the subtree rooted at the node with the given ID
     * 提取以给定ID节点为根的子树
     *
     * <p>Returns the node and its entire subtree. This does NOT clone — it returns
     * the original node reference with its existing children.</p>
     * <p>返回该节点及其完整子树。不进行克隆——返回原始节点引用及其现有子节点。</p>
     *
     * @param roots the root nodes | 根节点列表
     * @param id the target node ID | 目标节点ID
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the subtree rooted at the node, or empty if not found | 以该节点为根的子树，未找到返回空
     * @since V1.0.3
     */
    public static <T extends Treeable<T, ID>, ID> Optional<T> extractSubtree(List<T> roots, ID id) {
        return findById(roots, id);
    }

    /**
     * Get sibling nodes of the node with the given ID
     * 获取给定ID节点的兄弟节点
     *
     * <p>Returns other children of the same parent, excluding the target node itself.
     * Returns empty list if the node is a root or not found.</p>
     * <p>返回同一父节点的其他子节点，不包括目标节点本身。
     * 如果节点是根节点或未找到则返回空列表。</p>
     *
     * @param roots the root nodes | 根节点列表
     * @param id the target node ID | 目标节点ID
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the sibling nodes | 兄弟节点列表
     * @since V1.0.3
     */
    public static <T extends Treeable<T, ID>, ID> List<T> getSiblings(List<T> roots, ID id) {
        // Find the parent of the target node
        for (T root : roots) {
            List<T> siblings = findSiblingsInNode(root, id);
            if (siblings != null) {
                return siblings;
            }
        }
        return List.of();
    }

    private static <T extends Treeable<T, ID>, ID> List<T> findSiblingsInNode(T node, ID targetId) {
        // Iterative BFS to prevent StackOverflow on deep trees
        Deque<T> queue = new ArrayDeque<>();
        queue.offer(node);
        while (!queue.isEmpty()) {
            T current = queue.poll();
            List<T> children = current.getChildren();
            if (children == null || children.isEmpty()) {
                continue;
            }
            for (T child : children) {
                if (targetId.equals(child.getId())) {
                    // Found: return siblings (other children excluding this one)
                    List<T> siblings = new ArrayList<>();
                    for (T sibling : children) {
                        if (!targetId.equals(sibling.getId())) {
                            siblings.add(sibling);
                        }
                    }
                    return siblings;
                }
            }
            // Not found at this level, enqueue children for deeper search
            queue.addAll(children);
        }
        return null;
    }
}
