/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.tree.balanced;

import java.util.*;
import java.util.function.Consumer;

/**
 * Balanced Tree Util - Utilities for balanced binary search trees
 * 平衡树工具 - 平衡二叉搜索树工具类
 *
 * <p>Provides factory methods, conversion utilities, and common operations
 * for AVL trees and Red-Black trees.</p>
 * <p>为 AVL 树和红黑树提供工厂方法、转换工具和常用操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Factory methods for AVL and Red-Black trees - AVL和红黑树的工厂方法</li>
 *   <li>Optimal construction from sorted data - 从排序数据最优构建</li>
 *   <li>Tree merge, intersection, union, difference - 树合并、交集、并集、差集</li>
 *   <li>Order statistics (kth smallest/largest, median) - 顺序统计（第k小/大、中位数）</li>
 *   <li>Floor/ceiling operations - 下限/上限操作</li>
 *   <li>BST validation and statistics - BST验证和统计</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create AVL tree from collection
 * AvlTree<Integer> avl = BalancedTreeUtil.avlTreeOf(1, 2, 3, 4, 5);
 *
 * // Create Red-Black tree from collection
 * RedBlackTree<String> rb = BalancedTreeUtil.redBlackTreeOf("a", "b", "c");
 *
 * // Create from sorted array (optimal balance)
 * AvlTree<Integer> optimal = BalancedTreeUtil.fromSortedArray(sortedArray);
 *
 * // Merge two trees
 * AvlTree<Integer> merged = BalancedTreeUtil.merge(tree1, tree2);
 *
 * // Find kth smallest element
 * int kth = BalancedTreeUtil.kthSmallest(tree, k);
 *
 * // Find floor/ceiling
 * Integer floor = BalancedTreeUtil.floor(tree, value);
 * Integer ceiling = BalancedTreeUtil.ceiling(tree, value);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (null elements will cause NullPointerException) - 空值安全: 否（null元素会导致空指针异常）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n log n) for merge/union; O(n) for kth/median/floor/ceiling via in-order traversal - 时间复杂度: 合并/并集 O(n log n)；第k小/中位数/下限/上限通过中序遍历 O(n)</li>
 *   <li>Space complexity: O(n) - result tree and intermediate collections proportional to input size - 空间复杂度: O(n) - 结果树和中间集合与输入规模成正比</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see AvlTree
 * @see RedBlackTree
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public final class BalancedTreeUtil {

    private BalancedTreeUtil() {
        // Utility class
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates an AVL tree with the given elements.
     * 使用给定元素创建 AVL 树。
     *
     * @param <T> the element type - 元素类型
     * @param elements the elements - 元素
     * @return the AVL tree - AVL 树
     */
    @SafeVarargs
    public static <T extends Comparable<T>> AvlTree<T> avlTreeOf(T... elements) {
        AvlTree<T> tree = new AvlTree<>();
        tree.insert(elements);
        return tree;
    }

    /**
     * Creates an AVL tree from a collection.
     * 从集合创建 AVL 树。
     *
     * @param <T> the element type - 元素类型
     * @param elements the elements - 元素
     * @return the AVL tree - AVL 树
     */
    public static <T extends Comparable<T>> AvlTree<T> avlTreeFrom(Collection<T> elements) {
        AvlTree<T> tree = new AvlTree<>();
        tree.insertAll(elements);
        return tree;
    }

    /**
     * Creates an AVL tree with a custom comparator.
     * 使用自定义比较器创建 AVL 树。
     *
     * @param <T> the element type - 元素类型
     * @param comparator the comparator - 比较器
     * @param elements the elements - 元素
     * @return the AVL tree - AVL 树
     */
    @SafeVarargs
    public static <T> AvlTree<T> avlTreeOf(Comparator<T> comparator, T... elements) {
        AvlTree<T> tree = new AvlTree<>(comparator);
        tree.insert(elements);
        return tree;
    }

    /**
     * Creates a Red-Black tree with the given elements.
     * 使用给定元素创建红黑树。
     *
     * @param <T> the element type - 元素类型
     * @param elements the elements - 元素
     * @return the Red-Black tree - 红黑树
     */
    @SafeVarargs
    public static <T extends Comparable<T>> RedBlackTree<T> redBlackTreeOf(T... elements) {
        RedBlackTree<T> tree = new RedBlackTree<>();
        tree.insert(elements);
        return tree;
    }

    /**
     * Creates a Red-Black tree from a collection.
     * 从集合创建红黑树。
     *
     * @param <T> the element type - 元素类型
     * @param elements the elements - 元素
     * @return the Red-Black tree - 红黑树
     */
    public static <T extends Comparable<T>> RedBlackTree<T> redBlackTreeFrom(Collection<T> elements) {
        RedBlackTree<T> tree = new RedBlackTree<>();
        tree.insertAll(elements);
        return tree;
    }

    /**
     * Creates a Red-Black tree with a custom comparator.
     * 使用自定义比较器创建红黑树。
     *
     * @param <T> the element type - 元素类型
     * @param comparator the comparator - 比较器
     * @param elements the elements - 元素
     * @return the Red-Black tree - 红黑树
     */
    @SafeVarargs
    public static <T> RedBlackTree<T> redBlackTreeOf(Comparator<T> comparator, T... elements) {
        RedBlackTree<T> tree = new RedBlackTree<>(comparator);
        tree.insert(elements);
        return tree;
    }

    // ==================== Optimal Construction | 最优构建 ====================

    /**
     * Creates an AVL tree from a sorted array with optimal balance.
     * 从排序数组创建具有最优平衡的 AVL 树。
     *
     * <p>This method creates a perfectly balanced tree in O(n) time.</p>
     * <p>此方法在 O(n) 时间内创建完美平衡的树。</p>
     *
     * @param <T> the element type - 元素类型
     * @param sortedArray the sorted array - 排序数组
     * @return the AVL tree - AVL 树
     */
    public static <T extends Comparable<T>> AvlTree<T> fromSortedArray(T[] sortedArray) {
        return fromSortedList(Arrays.asList(sortedArray));
    }

    /**
     * Creates an AVL tree from a sorted list with optimal balance.
     * 从排序列表创建具有最优平衡的 AVL 树。
     *
     * @param <T> the element type - 元素类型
     * @param sortedList the sorted list - 排序列表
     * @return the AVL tree - AVL 树
     */
    public static <T extends Comparable<T>> AvlTree<T> fromSortedList(List<T> sortedList) {
        if (sortedList.isEmpty()) {
            return new AvlTree<>();
        }

        // Insert in optimal order (middle first, then recursively)
        AvlTree<T> tree = new AvlTree<>();
        insertBalanced(tree, sortedList, 0, sortedList.size() - 1);
        return tree;
    }

    private static <T extends Comparable<T>> void insertBalanced(
            AvlTree<T> tree, List<T> list, int start, int end) {
        if (start > end) return;

        int mid = start + (end - start) / 2;
        tree.insert(list.get(mid));

        insertBalanced(tree, list, start, mid - 1);
        insertBalanced(tree, list, mid + 1, end);
    }

    // ==================== Merge Operations | 合并操作 ====================

    /**
     * Merges two AVL trees into a new tree.
     * 将两个 AVL 树合并成一个新树。
     *
     * @param <T> the element type - 元素类型
     * @param tree1 the first tree - 第一棵树
     * @param tree2 the second tree - 第二棵树
     * @return the merged tree - 合并后的树
     */
    public static <T extends Comparable<T>> AvlTree<T> merge(AvlTree<T> tree1, AvlTree<T> tree2) {
        List<T> merged = new ArrayList<>(tree1.size() + tree2.size());
        merged.addAll(tree1.toSortedList());
        merged.addAll(tree2.toSortedList());
        Collections.sort(merged);

        // Remove duplicates
        List<T> unique = merged.stream().distinct().toList();

        return fromSortedList(unique);
    }

    /**
     * Merges two Red-Black trees into a new tree.
     * 将两个红黑树合并成一个新树。
     *
     * @param <T> the element type - 元素类型
     * @param tree1 the first tree - 第一棵树
     * @param tree2 the second tree - 第二棵树
     * @return the merged tree - 合并后的树
     */
    public static <T extends Comparable<T>> RedBlackTree<T> merge(
            RedBlackTree<T> tree1, RedBlackTree<T> tree2) {
        RedBlackTree<T> result = new RedBlackTree<>();
        tree1.inOrderTraversal(result::insert);
        tree2.inOrderTraversal(result::insert);
        return result;
    }

    // ==================== Order Statistics | 顺序统计 ====================

    /**
     * Finds the kth smallest element in the tree.
     * 查找树中第 k 小的元素。
     *
     * @param <T> the element type - 元素类型
     * @param tree the tree - 树
     * @param k the rank (1-based) - 排名（从 1 开始）
     * @return the kth smallest element - 第 k 小的元素
     */
    public static <T> Optional<T> kthSmallest(AvlTree<T> tree, int k) {
        if (k < 1 || k > tree.size()) {
            return Optional.empty();
        }

        int[] count = {0};
        Object[] result = {null};

        tree.inOrderTraversal(element -> {
            count[0]++;
            if (count[0] == k && result[0] == null) {
                result[0] = element;
            }
        });

        @SuppressWarnings("unchecked")
        T value = (T) result[0];
        return Optional.ofNullable(value);
    }

    /**
     * Finds the kth largest element in the tree.
     * 查找树中第 k 大的元素。
     *
     * @param <T> the element type - 元素类型
     * @param tree the tree - 树
     * @param k the rank (1-based) - 排名（从 1 开始）
     * @return the kth largest element - 第 k 大的元素
     */
    public static <T> Optional<T> kthLargest(AvlTree<T> tree, int k) {
        return kthSmallest(tree, tree.size() - k + 1);
    }

    /**
     * Finds the median element in the tree.
     * 查找树中的中位数元素。
     *
     * @param <T> the element type - 元素类型
     * @param tree the tree - 树
     * @return the median element - 中位数元素
     */
    public static <T> Optional<T> median(AvlTree<T> tree) {
        if (tree.isEmpty()) {
            return Optional.empty();
        }
        return kthSmallest(tree, (tree.size() + 1) / 2);
    }

    // ==================== Floor/Ceiling Operations | 下限/上限操作 ====================

    /**
     * Finds the largest element less than or equal to the given value.
     * 查找小于或等于给定值的最大元素。
     *
     * @param <T> the element type - 元素类型
     * @param tree the tree - 树
     * @param value the value - 值
     * @return the floor element - 下限元素
     */
    public static <T extends Comparable<T>> Optional<T> floor(AvlTree<T> tree, T value) {
        Object[] result = {null};

        tree.inOrderTraversal(element -> {
            if (element.compareTo(value) <= 0) {
                result[0] = element;
            }
        });

        @SuppressWarnings("unchecked")
        T floor = (T) result[0];
        return Optional.ofNullable(floor);
    }

    /**
     * Finds the smallest element greater than or equal to the given value.
     * 查找大于或等于给定值的最小元素。
     *
     * @param <T> the element type - 元素类型
     * @param tree the tree - 树
     * @param value the value - 值
     * @return the ceiling element - 上限元素
     */
    public static <T extends Comparable<T>> Optional<T> ceiling(AvlTree<T> tree, T value) {
        Object[] result = {null};

        tree.inOrderTraversal(element -> {
            if (result[0] == null && element.compareTo(value) >= 0) {
                result[0] = element;
            }
        });

        @SuppressWarnings("unchecked")
        T ceiling = (T) result[0];
        return Optional.ofNullable(ceiling);
    }

    // ==================== Set Operations | 集合操作 ====================

    /**
     * Computes the intersection of two trees.
     * 计算两棵树的交集。
     *
     * @param <T> the element type - 元素类型
     * @param tree1 the first tree - 第一棵树
     * @param tree2 the second tree - 第二棵树
     * @return a new tree with common elements - 包含公共元素的新树
     */
    public static <T extends Comparable<T>> AvlTree<T> intersection(
            AvlTree<T> tree1, AvlTree<T> tree2) {
        AvlTree<T> result = new AvlTree<>();

        tree1.inOrderTraversal(element -> {
            if (tree2.contains(element)) {
                result.insert(element);
            }
        });

        return result;
    }

    /**
     * Computes the union of two trees.
     * 计算两棵树的并集。
     *
     * @param <T> the element type - 元素类型
     * @param tree1 the first tree - 第一棵树
     * @param tree2 the second tree - 第二棵树
     * @return a new tree with all elements - 包含所有元素的新树
     */
    public static <T extends Comparable<T>> AvlTree<T> union(
            AvlTree<T> tree1, AvlTree<T> tree2) {
        return merge(tree1, tree2);
    }

    /**
     * Computes the difference of two trees (tree1 - tree2).
     * 计算两棵树的差集（tree1 - tree2）。
     *
     * @param <T> the element type - 元素类型
     * @param tree1 the first tree - 第一棵树
     * @param tree2 the second tree - 第二棵树
     * @return a new tree with elements in tree1 but not in tree2 - 包含在 tree1 中但不在 tree2 中的元素的新树
     */
    public static <T extends Comparable<T>> AvlTree<T> difference(
            AvlTree<T> tree1, AvlTree<T> tree2) {
        AvlTree<T> result = new AvlTree<>();

        tree1.inOrderTraversal(element -> {
            if (!tree2.contains(element)) {
                result.insert(element);
            }
        });

        return result;
    }

    // ==================== Validation | 验证 ====================

    /**
     * Checks if the tree is a valid binary search tree.
     * 检查树是否是有效的二叉搜索树。
     *
     * @param <T> the element type - 元素类型
     * @param tree the tree - 树
     * @return true if valid BST - 如果是有效的 BST 返回 true
     */
    public static <T extends Comparable<T>> boolean isValidBst(AvlTree<T> tree) {
        List<T> sorted = tree.toSortedList();

        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i - 1).compareTo(sorted.get(i)) >= 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns tree statistics.
     * 返回树的统计信息。
     *
     * @param <T> the element type - 元素类型
     * @param tree the tree - 树
     * @return the statistics record - 统计信息记录
     */
    public static <T> TreeStats stats(AvlTree<T> tree) {
        return new TreeStats(tree.size(), tree.height(), tree.isBalanced());
    }

    /**
     * Returns tree statistics for Red-Black tree.
     * 返回红黑树的统计信息。
     *
     * @param <T> the element type - 元素类型
     * @param tree the tree - 树
     * @return the statistics record - 统计信息记录
     */
    public static <T> TreeStats stats(RedBlackTree<T> tree) {
        return new TreeStats(tree.size(), tree.height(), true); // RB trees are always balanced
    }

    /**
     * Tree statistics record.
     * 树统计信息记录。
     *
     * @param size number of elements - 元素数量
     * @param height tree height - 树高度
     * @param balanced whether tree is balanced - 树是否平衡
     */
    public record TreeStats(int size, int height, boolean balanced) {
        /**
         * Returns the theoretical minimum height for the given size.
         * 返回给定大小的理论最小高度。
         */
        public int minPossibleHeight() {
            if (size == 0) return 0;
            return (int) Math.ceil(Math.log(size + 1) / Math.log(2));
        }

        /**
         * Returns balance efficiency (1.0 = optimal).
         * 返回平衡效率（1.0 = 最优）。
         */
        public double balanceEfficiency() {
            if (height == 0) return 1.0;
            return (double) minPossibleHeight() / height;
        }
    }
}
