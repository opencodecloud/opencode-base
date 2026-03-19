package cloud.opencode.base.tree.path;

import cloud.opencode.base.tree.Treeable;

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
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Optional<TreePath<MyNode>> path = PathFinder.findPathById(roots, targetId);
 * List<ID> ancestors = PathFinder.getAncestorIds(roots, targetId);
 * int depth = PathFinder.getDepth(roots, targetId);
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
            if (findPathInNode(root, predicate, path)) {
                return Optional.of(TreePath.of(path));
            }
        }
        return Optional.empty();
    }

    private static <T extends Treeable<T, ID>, ID> boolean findPathInNode(
            T node, Predicate<T> predicate, List<T> path) {
        path.add(node);

        if (predicate.test(node)) {
            return true;
        }

        List<T> children = node.getChildren();
        if (children != null) {
            for (T child : children) {
                if (findPathInNode(child, predicate, path)) {
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
            findAllPathsInNode(root, predicate, new ArrayList<>(), results);
        }
        return results;
    }

    private static <T extends Treeable<T, ID>, ID> void findAllPathsInNode(
            T node, Predicate<T> predicate, List<T> currentPath, List<TreePath<T>> results) {
        currentPath.add(node);

        if (predicate.test(node)) {
            results.add(TreePath.of(new ArrayList<>(currentPath)));
        }

        List<T> children = node.getChildren();
        if (children != null) {
            for (T child : children) {
                findAllPathsInNode(child, predicate, currentPath, results);
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
}
