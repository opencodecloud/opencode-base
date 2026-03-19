package cloud.opencode.base.tree.diff;

import cloud.opencode.base.tree.Treeable;
import cloud.opencode.base.tree.operation.TreeUtil;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Tree Diff
 * 树差异计算
 *
 * <p>Compares two trees and finds differences.</p>
 * <p>比较两棵树并找出差异。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Compare two trees by node ID - 通过节点ID比较两棵树</li>
 *   <li>Custom equality checker support - 自定义相等性检查器支持</li>
 *   <li>Custom key extractor support - 自定义键提取器支持</li>
 *   <li>Categorized diff (added, removed, modified, unchanged) - 分类差异（新增、删除、修改、未变化）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TreeDiffResult<MyNode> diff = TreeDiff.diff(oldRoots, newRoots);
 * if (diff.hasChanges()) {
 *     System.out.println("Added: " + diff.getAddedCount());
 *     System.out.println("Removed: " + diff.getRemovedCount());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 否</li>
 *   <li>Null-safe: No (roots must not be null) - 否（根节点不能为null）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public final class TreeDiff {

    private TreeDiff() {
        // Utility class
    }

    /**
     * Compare two trees by ID
     * 通过ID比较两棵树
     *
     * @param oldRoots the old tree roots | 旧树根节点
     * @param newRoots the new tree roots | 新树根节点
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the diff result | 差异结果
     */
    public static <T extends Treeable<T, ID>, ID> TreeDiffResult<T> diff(
            List<T> oldRoots, List<T> newRoots) {
        return diff(oldRoots, newRoots, Objects::equals);
    }

    /**
     * Compare two trees with custom equality
     * 使用自定义相等性比较两棵树
     *
     * @param oldRoots the old tree roots | 旧树根节点
     * @param newRoots the new tree roots | 新树根节点
     * @param equalityChecker the equality checker | 相等性检查器
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the diff result | 差异结果
     */
    public static <T extends Treeable<T, ID>, ID> TreeDiffResult<T> diff(
            List<T> oldRoots,
            List<T> newRoots,
            BiPredicate<T, T> equalityChecker) {

        // Flatten both trees
        List<T> oldNodes = TreeUtil.flatten(oldRoots);
        List<T> newNodes = TreeUtil.flatten(newRoots);

        // Build ID maps
        Map<ID, T> oldMap = buildIdMap(oldNodes);
        Map<ID, T> newMap = buildIdMap(newNodes);

        List<T> added = new ArrayList<>();
        List<T> removed = new ArrayList<>();
        List<TreeDiffResult.ModifiedNode<T>> modified = new ArrayList<>();
        List<T> unchanged = new ArrayList<>();

        // Find removed and modified
        for (T oldNode : oldNodes) {
            ID id = oldNode.getId();
            T newNode = newMap.get(id);
            if (newNode == null) {
                removed.add(oldNode);
            } else if (!equalityChecker.test(oldNode, newNode)) {
                modified.add(new TreeDiffResult.ModifiedNode<>(oldNode, newNode));
            } else {
                unchanged.add(oldNode);
            }
        }

        // Find added
        for (T newNode : newNodes) {
            ID id = newNode.getId();
            if (!oldMap.containsKey(id)) {
                added.add(newNode);
            }
        }

        return new TreeDiffResult<>(added, removed, modified, unchanged);
    }

    /**
     * Compare using custom key extractor
     * 使用自定义键提取器比较
     *
     * @param oldRoots the old tree roots | 旧树根节点
     * @param newRoots the new tree roots | 新树根节点
     * @param keyExtractor the key extractor | 键提取器
     * @param equalityChecker the equality checker | 相等性检查器
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param <K> the key type | 键类型
     * @return the diff result | 差异结果
     */
    public static <T extends Treeable<T, ID>, ID, K> TreeDiffResult<T> diffByKey(
            List<T> oldRoots,
            List<T> newRoots,
            Function<T, K> keyExtractor,
            BiPredicate<T, T> equalityChecker) {

        List<T> oldNodes = TreeUtil.flatten(oldRoots);
        List<T> newNodes = TreeUtil.flatten(newRoots);

        Map<K, T> oldMap = new HashMap<>();
        Map<K, T> newMap = new HashMap<>();

        for (T node : oldNodes) {
            oldMap.put(keyExtractor.apply(node), node);
        }
        for (T node : newNodes) {
            newMap.put(keyExtractor.apply(node), node);
        }

        List<T> added = new ArrayList<>();
        List<T> removed = new ArrayList<>();
        List<TreeDiffResult.ModifiedNode<T>> modified = new ArrayList<>();
        List<T> unchanged = new ArrayList<>();

        for (T oldNode : oldNodes) {
            K key = keyExtractor.apply(oldNode);
            T newNode = newMap.get(key);
            if (newNode == null) {
                removed.add(oldNode);
            } else if (!equalityChecker.test(oldNode, newNode)) {
                modified.add(new TreeDiffResult.ModifiedNode<>(oldNode, newNode));
            } else {
                unchanged.add(oldNode);
            }
        }

        for (T newNode : newNodes) {
            K key = keyExtractor.apply(newNode);
            if (!oldMap.containsKey(key)) {
                added.add(newNode);
            }
        }

        return new TreeDiffResult<>(added, removed, modified, unchanged);
    }

    private static <T extends Treeable<T, ID>, ID> Map<ID, T> buildIdMap(List<T> nodes) {
        Map<ID, T> map = new HashMap<>();
        for (T node : nodes) {
            map.put(node.getId(), node);
        }
        return map;
    }
}
