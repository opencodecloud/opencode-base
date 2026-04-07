package cloud.opencode.base.tree.operation;

import cloud.opencode.base.tree.Treeable;
import cloud.opencode.base.tree.exception.TreeException;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Tree Sorter - Recursively sort children at every level
 * 树排序器 - 递归排序每一层子节点
 *
 * <p>Recursively sorts children at every level of a tree in-place.</p>
 * <p>递归地对树的每一层子节点进行原地排序。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sort by custom comparator - 按自定义比较器排序</li>
 *   <li>Sort by extracted comparable key - 按提取的可比较键排序</li>
 *   <li>Reversed sort order - 反向排序</li>
 *   <li>Check if tree is sorted at all levels - 检查树在所有层级是否已排序</li>
 *   <li>Max depth protection (1000) - 最大深度保护（1000）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Sort by name - 按名称排序
 * TreeSorter.sort(roots, Comparator.comparing(Node::getName));
 *
 * // Sort by extracted key - 按提取键排序
 * TreeSorter.sortBy(roots, Node::getOrder);
 *
 * // Sort reversed - 反向排序
 * TreeSorter.sortReversed(roots, Comparator.comparing(Node::getName));
 *
 * // Check if sorted - 检查是否已排序
 * boolean sorted = TreeSorter.isSorted(roots, Comparator.comparing(Node::getName));
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n log k) where k is average children count - 时间复杂度: O(n log k)</li>
 *   <li>Space complexity: O(n) for iterative stack - 空间复杂度: O(n) 迭代栈</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 是（无状态工具类）</li>
 *   <li>Null-safe: No (roots and comparator must not be null) - 否（根节点和比较器不能为null）</li>
 *   <li>Depth-safe: Max depth 1000 to prevent stack overflow - 最大深度1000防止栈溢出</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.3
 */
public final class TreeSorter {

    private static final int MAX_DEPTH = 1000;

    private TreeSorter() {
        // Utility class
    }

    /**
     * Sort children in-place recursively at every level using the given comparator.
     * 使用给定的比较器递归地对每一层的子节点进行原地排序。
     *
     * <p>Uses an iterative depth-tracking approach with a Deque to avoid stack overflow.</p>
     * <p>使用基于Deque的迭代深度跟踪方式以避免栈溢出。</p>
     *
     * @param roots the root nodes | 根节点列表
     * @param comparator the comparator for ordering | 排序比较器
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @throws NullPointerException if roots or comparator is null | 如果根节点或比较器为null
     * @throws TreeException if tree depth exceeds 1000 | 如果树深度超过1000
     */
    public static <T extends Treeable<T, ID>, ID> void sort(List<T> roots, Comparator<T> comparator) {
        Objects.requireNonNull(roots, "roots must not be null");
        Objects.requireNonNull(comparator, "comparator must not be null");

        if (roots.isEmpty()) {
            return;
        }

        roots.sort(comparator);

        Deque<NodeWithDepth<T>> stack = new ArrayDeque<>();
        for (T root : roots) {
            stack.push(new NodeWithDepth<>(root, 1));
        }

        while (!stack.isEmpty()) {
            NodeWithDepth<T> entry = stack.pop();
            T node = entry.node;
            int depth = entry.depth;

            List<T> children = node.getChildren();
            if (children == null || children.isEmpty()) {
                continue;
            }

            if (depth >= MAX_DEPTH) {
                throw TreeException.maxDepthExceeded(MAX_DEPTH);
            }

            children.sort(comparator);

            for (T child : children) {
                stack.push(new NodeWithDepth<>(child, depth + 1));
            }
        }
    }

    /**
     * Sort children in-place recursively by an extracted comparable key.
     * 按提取的可比较键递归地对子节点进行原地排序。
     *
     * @param roots the root nodes | 根节点列表
     * @param keyExtractor the function to extract the sort key | 提取排序键的函数
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @param <U> the comparable key type | 可比较键类型
     * @throws NullPointerException if roots or keyExtractor is null | 如果根节点或键提取器为null
     * @throws TreeException if tree depth exceeds 1000 | 如果树深度超过1000
     */
    public static <T extends Treeable<T, ID>, ID, U extends Comparable<? super U>> void sortBy(
            List<T> roots, Function<T, U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor must not be null");
        sort(roots, Comparator.comparing(keyExtractor));
    }

    /**
     * Sort children in-place recursively in reversed order.
     * 按反向顺序递归地对子节点进行原地排序。
     *
     * @param roots the root nodes | 根节点列表
     * @param comparator the comparator for ordering (will be reversed) | 排序比较器（将被反转）
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @throws NullPointerException if roots or comparator is null | 如果根节点或比较器为null
     * @throws TreeException if tree depth exceeds 1000 | 如果树深度超过1000
     */
    public static <T extends Treeable<T, ID>, ID> void sortReversed(
            List<T> roots, Comparator<T> comparator) {
        Objects.requireNonNull(comparator, "comparator must not be null");
        sort(roots, comparator.reversed());
    }

    /**
     * Check if the tree is sorted at all levels according to the given comparator.
     * 检查树在所有层级是否按给定的比较器排序。
     *
     * @param roots the root nodes | 根节点列表
     * @param comparator the comparator to check ordering against | 用于检查顺序的比较器
     * @param <T> the node type | 节点类型
     * @param <ID> the ID type | ID类型
     * @return true if all levels are sorted, false otherwise | 如果所有层级已排序返回true，否则返回false
     * @throws NullPointerException if roots or comparator is null | 如果根节点或比较器为null
     * @throws TreeException if tree depth exceeds 1000 | 如果树深度超过1000
     */
    public static <T extends Treeable<T, ID>, ID> boolean isSorted(
            List<T> roots, Comparator<T> comparator) {
        Objects.requireNonNull(roots, "roots must not be null");
        Objects.requireNonNull(comparator, "comparator must not be null");

        if (!isListSorted(roots, comparator)) {
            return false;
        }

        Deque<NodeWithDepth<T>> stack = new ArrayDeque<>();
        for (T root : roots) {
            stack.push(new NodeWithDepth<>(root, 1));
        }

        while (!stack.isEmpty()) {
            NodeWithDepth<T> entry = stack.pop();
            T node = entry.node;
            int depth = entry.depth;

            List<T> children = node.getChildren();
            if (children == null || children.isEmpty()) {
                continue;
            }

            if (depth >= MAX_DEPTH) {
                throw TreeException.maxDepthExceeded(MAX_DEPTH);
            }

            if (!isListSorted(children, comparator)) {
                return false;
            }

            for (T child : children) {
                stack.push(new NodeWithDepth<>(child, depth + 1));
            }
        }

        return true;
    }

    private static <T> boolean isListSorted(List<T> list, Comparator<T> comparator) {
        for (int i = 1; i < list.size(); i++) {
            if (comparator.compare(list.get(i - 1), list.get(i)) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Typed stack entry to avoid Object[] allocation and int boxing
     */
    private record NodeWithDepth<T>(T node, int depth) {}
}
