package cloud.opencode.base.tree.operation;

import cloud.opencode.base.tree.Treeable;
import cloud.opencode.base.tree.exception.TreeException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Tree Merger - Merges two tree forests by matching node IDs
 * 树合并器 - 通过匹配节点ID合并两棵树森林
 *
 * <p>Merges two tree forests (lists of root nodes) into a single forest by matching
 * nodes based on their IDs and applying a configurable merge strategy to resolve
 * conflicts when the same node exists in both forests.</p>
 * <p>通过匹配节点ID将两棵树森林（根节点列表）合并为一棵，当相同节点存在于两棵森林中时，
 * 使用可配置的合并策略来解决冲突。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Merge two tree forests by ID matching - 按ID匹配合并两棵树森林</li>
 *   <li>Configurable merge strategy (KEEP_LEFT, KEEP_RIGHT, custom) - 可配置合并策略</li>
 *   <li>Recursive children merging - 递归合并子节点</li>
 *   <li>Nodes only in one side are included as-is - 仅在一侧的节点原样包含</li>
 *   <li>Max depth protection (1000) to prevent stack overflow - 最大深度保护防止栈溢出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Merge keeping left nodes on conflict
 * // 冲突时保留左侧节点
 * List<MyNode> merged = TreeMerger.mergeKeepLeft(leftRoots, rightRoots);
 *
 * // Merge keeping right nodes on conflict
 * // 冲突时保留右侧节点
 * List<MyNode> merged = TreeMerger.mergeKeepRight(leftRoots, rightRoots);
 *
 * // Merge with custom strategy
 * // 使用自定义策略合并
 * List<MyNode> merged = TreeMerger.merge(leftRoots, rightRoots, (left, right) -> {
 *     left.setName(right.getName()); // take name from right
 *     return left;
 * });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 是（无状态工具类）</li>
 *   <li>Null-safe: No (parameters must not be null) - 否（参数不能为null）</li>
 *   <li>Depth-limited: Max 1000 levels to prevent stack overflow - 深度限制1000层防止栈溢出</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.3
 */
public final class TreeMerger {

    /**
     * Maximum allowed tree depth for merge operations.
     * 合并操作允许的最大树深度。
     */
    private static final int MAX_DEPTH = 1000;

    private TreeMerger() {
        // Utility class
    }

    /**
     * Functional interface for resolving merge conflicts when a node exists in both forests.
     * 当节点同时存在于两棵森林中时，用于解决合并冲突的函数式接口。
     *
     * @param <T> the node type | 节点类型
     */
    @FunctionalInterface
    public interface MergeStrategy<T> {

        /**
         * Resolves a conflict between left and right nodes with the same ID.
         * 解决具有相同ID的左右节点之间的冲突。
         *
         * @param left the node from the left forest | 左侧森林的节点
         * @param right the node from the right forest | 右侧森林的节点
         * @return the resolved node | 解决后的节点
         */
        T resolve(T left, T right);
    }

    /**
     * Merges two tree forests using the specified merge strategy.
     * 使用指定的合并策略合并两棵树森林。
     *
     * <p>Nodes present in both forests are resolved using the provided strategy.
     * Nodes present only in one forest are included as-is. Children of resolved
     * nodes are always recursively merged from both original sides (the strategy
     * resolves node data only; children are merged structurally, not taken from
     * the resolved node). Nodes with null IDs are skipped.</p>
     * <p>同时存在于两棵森林中的节点使用提供的策略解决。仅存在于一棵森林中的节点
     * 原样包含。已解决节点的子节点始终从两棵原始树递归合并（策略仅解决节点数据；
     * 子节点按结构合并，不取自已解决节点）。ID为null的节点将被跳过。</p>
     *
     * @param left the left forest (list of root nodes) | 左侧森林（根节点列表）
     * @param right the right forest (list of root nodes) | 右侧森林（根节点列表）
     * @param strategy the merge strategy for conflict resolution | 冲突解决的合并策略
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the merged forest | 合并后的森林
     * @throws NullPointerException if any parameter is null | 如果任何参数为null
     * @throws TreeException if max depth (1000) is exceeded | 如果超过最大深度（1000）
     */
    public static <T extends Treeable<T, ID>, ID> List<T> merge(
            List<T> left, List<T> right, MergeStrategy<T> strategy) {
        Objects.requireNonNull(left, "left forest must not be null");
        Objects.requireNonNull(right, "right forest must not be null");
        Objects.requireNonNull(strategy, "merge strategy must not be null");

        return mergeForest(left, right, strategy, 0);
    }

    /**
     * Merges two tree forests, keeping the left node on conflict.
     * 合并两棵树森林，冲突时保留左侧节点。
     *
     * <p>When a node with the same ID exists in both forests, the left node is kept
     * and its children are recursively merged with the right node's children.</p>
     * <p>当相同ID的节点存在于两棵森林中时，保留左侧节点，并递归合并其子节点与右侧节点的子节点。</p>
     *
     * @param left the left forest (list of root nodes) | 左侧森林（根节点列表）
     * @param right the right forest (list of root nodes) | 右侧森林（根节点列表）
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the merged forest | 合并后的森林
     * @throws NullPointerException if any parameter is null | 如果任何参数为null
     * @throws TreeException if max depth (1000) is exceeded | 如果超过最大深度（1000）
     */
    public static <T extends Treeable<T, ID>, ID> List<T> mergeKeepLeft(
            List<T> left, List<T> right) {
        return merge(left, right, (l, r) -> l);
    }

    /**
     * Merges two tree forests, keeping the right node on conflict.
     * 合并两棵树森林，冲突时保留右侧节点。
     *
     * <p>When a node with the same ID exists in both forests, the right node is kept
     * and its children are recursively merged with the left node's children.</p>
     * <p>当相同ID的节点存在于两棵森林中时，保留右侧节点，并递归合并其子节点与左侧节点的子节点。</p>
     *
     * @param left the left forest (list of root nodes) | 左侧森林（根节点列表）
     * @param right the right forest (list of root nodes) | 右侧森林（根节点列表）
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the merged forest | 合并后的森林
     * @throws NullPointerException if any parameter is null | 如果任何参数为null
     * @throws TreeException if max depth (1000) is exceeded | 如果超过最大深度（1000）
     */
    public static <T extends Treeable<T, ID>, ID> List<T> mergeKeepRight(
            List<T> left, List<T> right) {
        return merge(left, right, (l, r) -> r);
    }

    /**
     * Merges two forests at the given depth level.
     * 在给定深度级别合并两棵森林。
     *
     * @param left the left roots | 左侧根节点
     * @param right the right roots | 右侧根节点
     * @param strategy the merge strategy | 合并策略
     * @param depth the current depth | 当前深度
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the merged list | 合并后的列表
     */
    private static <T extends Treeable<T, ID>, ID> List<T> mergeForest(
            List<T> left, List<T> right, MergeStrategy<T> strategy, int depth) {
        if (depth > MAX_DEPTH) {
            throw TreeException.maxDepthExceeded(MAX_DEPTH);
        }

        // Build ID → node maps preserving insertion order
        Map<ID, T> leftMap = buildIdMap(left);
        Map<ID, T> rightMap = buildIdMap(right);

        // Collect all IDs preserving left-first order, then right-only
        Set<ID> allIds = new LinkedHashSet<>(leftMap.keySet());
        allIds.addAll(rightMap.keySet());

        List<T> result = new ArrayList<>();

        for (ID id : allIds) {
            T leftNode = leftMap.get(id);
            T rightNode = rightMap.get(id);

            if (leftNode != null && rightNode != null) {
                // Node exists in both: resolve with strategy and merge children
                T resolved = strategy.resolve(leftNode, rightNode);
                Objects.requireNonNull(resolved,
                        "MergeStrategy.resolve must not return null for ID: " + id);
                List<T> mergedChildren = mergeChildren(
                        leftNode, rightNode, strategy, depth);
                resolved.setChildren(mergedChildren);
                result.add(resolved);
            } else if (leftNode != null) {
                // Node only in left: include as-is
                result.add(leftNode);
            } else {
                // Node only in right: include as-is
                result.add(rightNode);
            }
        }

        return result;
    }

    /**
     * Merges children of two conflicting nodes.
     * 合并两个冲突节点的子节点。
     *
     * <p>Children are always sourced from the original leftNode and rightNode,
     * not from the resolved node. The strategy resolves node data only.</p>
     * <p>子节点始终取自原始的 leftNode 和 rightNode，不取自已解决的节点。策略仅解决节点数据。</p>
     *
     * @param leftNode the left node | 左侧节点
     * @param rightNode the right node | 右侧节点
     * @param strategy the merge strategy | 合并策略
     * @param depth the current depth | 当前深度
     * @param <T> the node type | 节点���型
     * @param <ID> the ID type | ID类型
     * @return the merged children list | ���并后的子节点列表
     */
    private static <T extends Treeable<T, ID>, ID> List<T> mergeChildren(
            T leftNode, T rightNode, MergeStrategy<T> strategy, int depth) {
        List<T> leftChildren = leftNode.getChildren();
        List<T> rightChildren = rightNode.getChildren();

        if (leftChildren == null || leftChildren.isEmpty()) {
            // No left children: use right children (or empty)
            return rightChildren != null ? new ArrayList<>(rightChildren) : new ArrayList<>();
        }
        if (rightChildren == null || rightChildren.isEmpty()) {
            // No right children: use left children
            return new ArrayList<>(leftChildren);
        }

        // Both have children: recursively merge
        return mergeForest(leftChildren, rightChildren, strategy, depth + 1);
    }

    /**
     * Builds an ID-to-node map from a list of nodes, preserving insertion order.
     * 从节点列表构建ID到节点的映射，保持插入顺序。
     *
     * <p>Uses iterative traversal to collect only the top-level (root) nodes.
     * Nodes with null IDs are skipped.</p>
     * <p>使用迭代遍历仅收集顶层（根）节点。跳过ID为null的节点。</p>
     *
     * @param nodes the list of nodes | 节点列表
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return the ID-to-node map | ID到节点的映射
     */
    private static <T extends Treeable<T, ID>, ID> Map<ID, T> buildIdMap(List<T> nodes) {
        Map<ID, T> map = new LinkedHashMap<>();
        if (nodes == null || nodes.isEmpty()) {
            return map;
        }
        for (T node : nodes) {
            if (node != null) {
                ID id = node.getId();
                if (id != null) {
                    map.putIfAbsent(id, node);
                }
            }
        }
        return map;
    }
}
