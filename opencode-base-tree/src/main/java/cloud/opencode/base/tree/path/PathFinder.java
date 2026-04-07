package cloud.opencode.base.tree.path;

import cloud.opencode.base.tree.Treeable;
import cloud.opencode.base.tree.exception.TreeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Path Finder
 * 路径查找器
 *
 * <p>Finds paths in tree structures.</p>
 * <p>在树结构中查找路径。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Find path to node by ID or predicate - 通过ID或谓词查找节点路径</li>
 *   <li>Find all paths to matching nodes - 查找到所有匹配节点的路径</li>
 *   <li>Find paths to leaf nodes - 查找到叶子节点的路径</li>
 *   <li>Get ancestor IDs and node depth - 获取祖先ID和节点深度</li>
 *   <li>Find lowest common ancestor of two nodes - 查找两个节点的最近公共祖先</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Optional<TreePath<MyNode>> path = PathFinder.findPathById(roots, targetId);
 * List<ID> ancestors = PathFinder.getAncestorIds(roots, targetId);
 * int depth = PathFinder.getDepth(roots, targetId);
 * Optional<MyNode> lca = PathFinder.findLowestCommonAncestor(roots, id1, id2);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 否</li>
 *   <li>Null-safe: No (roots and targetId must not be null) - 否（根节点和目标ID不能为null）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public final class PathFinder {

    private static final int MAX_DEPTH = 1000;

    private PathFinder() {
        // Utility class
    }

    /**
     * Find path to node by ID
     * 通过ID查找到节点的路径
     *
     * @param roots the root nodes | 根节点列表
     * @param targetId the target ID | 目标ID
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the path if found | 如果找到返回路径
     */
    public static <T extends Treeable<T, ID>, ID> Optional<TreePath<T>> findPathById(
            List<T> roots, ID targetId) {
        return findPath(roots, node -> targetId.equals(node.getId()));
    }

    /**
     * Find path to node matching predicate
     * 查找到匹配谓词的节点的路径
     *
     * @param roots the root nodes | 根节点列表
     * @param predicate the target predicate | 目标谓词
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the path if found | 如果找到返回路径
     */
    public static <T extends Treeable<T, ID>, ID> Optional<TreePath<T>> findPath(
            List<T> roots, Predicate<T> predicate) {
        for (T root : roots) {
            List<T> path = new ArrayList<>();
            if (findPathInNode(root, predicate, path, 0)) {
                return Optional.of(TreePath.of(path));
            }
        }
        return Optional.empty();
    }

    private static <T extends Treeable<T, ID>, ID> boolean findPathInNode(
            T node, Predicate<T> predicate, List<T> path, int depth) {
        if (depth > MAX_DEPTH) {
            throw TreeException.maxDepthExceeded(MAX_DEPTH);
        }
        path.add(node);

        if (predicate.test(node)) {
            return true;
        }

        List<T> children = node.getChildren();
        if (children != null) {
            for (T child : children) {
                if (findPathInNode(child, predicate, path, depth + 1)) {
                    return true;
                }
            }
        }

        path.removeLast();
        return false;
    }

    /**
     * Find all paths to matching nodes
     * 查找到所有匹配节点的路径
     *
     * @param roots the root nodes | 根节点列表
     * @param predicate the target predicate | 目标谓词
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the paths | 路径列表
     */
    public static <T extends Treeable<T, ID>, ID> List<TreePath<T>> findAllPaths(
            List<T> roots, Predicate<T> predicate) {
        List<TreePath<T>> results = new ArrayList<>();
        for (T root : roots) {
            findAllPathsInNode(root, predicate, new ArrayList<>(), results, 0);
        }
        return results;
    }

    private static <T extends Treeable<T, ID>, ID> void findAllPathsInNode(
            T node, Predicate<T> predicate, List<T> currentPath, List<TreePath<T>> results, int depth) {
        if (depth > MAX_DEPTH) {
            throw TreeException.maxDepthExceeded(MAX_DEPTH);
        }
        currentPath.add(node);

        if (predicate.test(node)) {
            results.add(TreePath.of(new ArrayList<>(currentPath)));
        }

        List<T> children = node.getChildren();
        if (children != null) {
            for (T child : children) {
                findAllPathsInNode(child, predicate, currentPath, results, depth + 1);
            }
        }

        currentPath.removeLast();
    }

    /**
     * Find paths to all leaf nodes
     * 查找到所有叶子节点的路径
     *
     * @param roots the root nodes | 根节点列表
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the paths to leaves | 到叶子节点的路径
     */
    public static <T extends Treeable<T, ID>, ID> List<TreePath<T>> findAllLeafPaths(List<T> roots) {
        return findAllPaths(roots, node -> node.getChildren() == null || node.getChildren().isEmpty());
    }

    /**
     * Get ancestor IDs for a node
     * 获取节点的祖先ID列表
     *
     * @param roots the root nodes | 根节点列表
     * @param targetId the target ID | 目标ID
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the ancestor IDs | 祖先ID列表
     */
    public static <T extends Treeable<T, ID>, ID> List<ID> getAncestorIds(List<T> roots, ID targetId) {
        return findPathById(roots, targetId)
            .map(path -> {
                List<ID> ids = new ArrayList<>();
                List<T> nodes = path.nodes();
                // Exclude target node itself
                for (int i = 0; i < nodes.size() - 1; i++) {
                    ids.add(nodes.get(i).getId());
                }
                return ids;
            })
            .orElse(List.of());
    }

    /**
     * Get depth of node
     * 获取节点深度
     *
     * @param roots the root nodes | 根节点列表
     * @param targetId the target ID | 目标ID
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the depth (-1 if not found) | 深度（未找到返回-1）
     */
    public static <T extends Treeable<T, ID>, ID> int getDepth(List<T> roots, ID targetId) {
        return findPathById(roots, targetId)
            .map(path -> path.length() - 1)
            .orElse(-1);
    }

    /**
     * Find lowest common ancestor of two nodes by ID
     * 通过ID查找两个节点的最近公共祖先
     *
     * <p>Finds the deepest node that is an ancestor of both target nodes.
     * The algorithm finds paths to both nodes, then walks the paths in parallel
     * to find the last common node.</p>
     * <p>查找同时是两个目标节点祖先的最深节点。
     * 算法先分别查找到两个节点的路径，然后并行遍历路径以找到最后一个公共节点。</p>
     *
     * <p><strong>Performance | 性能特性:</strong></p>
     * <ul>
     *   <li>Time complexity: O(n) where n is total node count - 时间复杂度: O(n)，n 为总节点数</li>
     *   <li>Space complexity: O(h) where h is tree height - 空间复杂度: O(h)，h 为树高</li>
     * </ul>
     *
     * @param roots the root nodes | 根节点列表
     * @param id1 the first node ID | 第一个节点ID
     * @param id2 the second node ID | 第二个节点ID
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the lowest common ancestor if both nodes exist | 如果两个节点都存在返回最近公共祖先
     * @since V1.0.3
     */
    public static <T extends Treeable<T, ID>, ID> Optional<T> findLowestCommonAncestor(
            List<T> roots, ID id1, ID id2) {
        Predicate<T> pred1 = node -> id1.equals(node.getId());
        Predicate<T> pred2 = node -> id2.equals(node.getId());
        return findLowestCommonAncestor(roots, pred1, pred2);
    }

    /**
     * Find lowest common ancestor of two nodes by predicates
     * 通过谓词查找两个节点的最近公共祖先
     *
     * <p>Finds the deepest node that is an ancestor of both target nodes.
     * The algorithm finds paths to both nodes, then walks the paths in parallel
     * to find the last common node.</p>
     * <p>查找同时是两个目标节点祖先的最深节点。
     * 算法先分别查找到两个节点的路径，然后并行遍历路径以找到最后一个公共节点。</p>
     *
     * <p><strong>Performance | 性能特性:</strong></p>
     * <ul>
     *   <li>Time complexity: O(n) where n is total node count - 时间复杂度: O(n)，n 为总节点数</li>
     *   <li>Space complexity: O(h) where h is tree height - 空间复杂度: O(h)，h 为树高</li>
     * </ul>
     *
     * @param roots the root nodes | 根节点列表
     * @param predicate1 the predicate for first node | 第一个节点的谓词
     * @param predicate2 the predicate for second node | 第二个节点的谓词
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the lowest common ancestor if both nodes exist | 如果两个节点都存在返回最近公共祖先
     * @since V1.0.3
     */
    public static <T extends Treeable<T, ID>, ID> Optional<T> findLowestCommonAncestor(
            List<T> roots, Predicate<T> predicate1, Predicate<T> predicate2) {
        Optional<TreePath<T>> path1 = findPath(roots, predicate1);
        Optional<TreePath<T>> path2 = findPath(roots, predicate2);

        if (path1.isEmpty() || path2.isEmpty()) {
            return Optional.empty();
        }

        List<T> nodes1 = path1.get().nodes();
        List<T> nodes2 = path2.get().nodes();
        int minLength = Math.min(nodes1.size(), nodes2.size());

        T lca = null;
        for (int i = 0; i < minLength; i++) {
            if (nodes1.get(i) == nodes2.get(i)) {
                lca = nodes1.get(i);
            } else {
                break;
            }
        }

        return Optional.ofNullable(lca);
    }
}
