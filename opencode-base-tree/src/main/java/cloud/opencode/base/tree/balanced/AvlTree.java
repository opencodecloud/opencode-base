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
 * AVL Tree - Self-balancing binary search tree
 * AVL 树 - 自平衡二叉搜索树
 *
 * <p>An AVL tree maintains balance by ensuring that the heights of the
 * two child subtrees of any node differ by at most one.</p>
 * <p>AVL 树通过确保任何节点的两个子树的高度差最多为一来保持平衡。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Self-balancing binary search tree - 自平衡二叉搜索树</li>
 *   <li>O(log n) insert, delete, and search - O(log n)插入、删除和搜索</li>
 *   <li>Range queries - 范围查询</li>
 *   <li>Multiple traversal orders (in-order, pre-order, post-order, level-order) - 多种遍历顺序</li>
 *   <li>Custom comparator support - 自定义比较器支持</li>
 *   <li>Min/max element access - 最小/最大元素访问</li>
 * </ul>
 *
 * <p><strong>Time Complexity | 时间复杂度:</strong></p>
 * <ul>
 *   <li>Search: O(log n)</li>
 *   <li>Insert: O(log n)</li>
 *   <li>Delete: O(log n)</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create an AVL tree with natural ordering
 * AvlTree<Integer> tree = new AvlTree<>();
 *
 * // Insert elements
 * tree.insert(5, 3, 7, 2, 4, 6, 8);
 *
 * // Search
 * boolean found = tree.contains(5);  // true
 *
 * // In-order traversal (sorted)
 * tree.inOrderTraversal(System.out::println);  // 2, 3, 4, 5, 6, 7, 8
 *
 * // Delete
 * tree.delete(5);
 *
 * // Create with custom comparator
 * AvlTree<String> stringTree = new AvlTree<>(String.CASE_INSENSITIVE_ORDER);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: No (null elements will cause NullPointerException during comparison) - 空值安全: 否（null元素在比较时会导致空指针异常）</li>
 * </ul>
 *
 * @param <T> the element type - 元素类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see RedBlackTree
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public class AvlTree<T> {

    private Node<T> root;
    private final Comparator<? super T> comparator;
    private int size;

    // ==================== Constructors | 构造函数 ====================

    /**
     * Creates an empty AVL tree with natural ordering.
     * 使用自然顺序创建空的 AVL 树。
     */
    public AvlTree() {
        this(null);
    }

    /**
     * Creates an empty AVL tree with the specified comparator.
     * 使用指定的比较器创建空的 AVL 树。
     *
     * @param comparator the comparator to use - 要使用的比较器
     */
    public AvlTree(Comparator<? super T> comparator) {
        this.comparator = comparator;
        this.root = null;
        this.size = 0;
    }

    // ==================== Node Class | 节点类 ====================

    private static class Node<T> {
        T data;
        Node<T> left;
        Node<T> right;
        int height;

        Node(T data) {
            this.data = data;
            this.height = 1;
        }
    }

    // ==================== Basic Operations | 基本操作 ====================

    /**
     * Returns the number of elements in the tree.
     * 返回树中的元素数量。
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if the tree is empty.
     * 如果树为空返回 true。
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Clears all elements from the tree.
     * 清除树中的所有元素。
     */
    public void clear() {
        root = null;
        size = 0;
    }

    // ==================== Insert Operations | 插入操作 ====================

    /**
     * Inserts an element into the tree.
     * 向树中插入元素。
     *
     * @param data the element to insert - 要插入的元素
     * @return true if inserted (element was not present) - 如果插入成功返回 true
     */
    public boolean insert(T data) {
        int oldSize = size;
        root = insert(root, data);
        return size > oldSize;
    }

    /**
     * Inserts multiple elements into the tree.
     * 向树中插入多个元素。
     *
     * @param elements the elements to insert - 要插入的元素
     */
    @SafeVarargs
    public final void insert(T... elements) {
        for (T element : elements) {
            insert(element);
        }
    }

    /**
     * Inserts all elements from a collection.
     * 插入集合中的所有元素。
     *
     * @param elements the elements to insert - 要插入的元素
     */
    public void insertAll(Collection<? extends T> elements) {
        for (T element : elements) {
            insert(element);
        }
    }

    private Node<T> insert(Node<T> node, T data) {
        if (node == null) {
            size++;
            return new Node<>(data);
        }

        int cmp = compare(data, node.data);

        if (cmp < 0) {
            node.left = insert(node.left, data);
        } else if (cmp > 0) {
            node.right = insert(node.right, data);
        } else {
            // Duplicate - update value
            node.data = data;
            return node;
        }

        return balance(node);
    }

    // ==================== Delete Operations | 删除操作 ====================

    /**
     * Deletes an element from the tree.
     * 从树中删除元素。
     *
     * @param data the element to delete - 要删除的元素
     * @return true if deleted (element was present) - 如果删除成功返回 true
     */
    public boolean delete(T data) {
        int oldSize = size;
        root = delete(root, data);
        return size < oldSize;
    }

    private Node<T> delete(Node<T> node, T data) {
        return delete(node, data, true);
    }

    private Node<T> delete(Node<T> node, T data, boolean updateSize) {
        if (node == null) {
            return null;
        }

        int cmp = compare(data, node.data);

        if (cmp < 0) {
            node.left = delete(node.left, data, updateSize);
        } else if (cmp > 0) {
            node.right = delete(node.right, data, updateSize);
        } else {
            // Found node to delete
            if (updateSize) {
                size--;
            }

            if (node.left == null) {
                return node.right;
            } else if (node.right == null) {
                return node.left;
            }

            // Node has two children - replace with in-order successor
            Node<T> successor = findMin(node.right);
            node.data = successor.data;
            node.right = delete(node.right, successor.data, false);
        }

        return balance(node);
    }

    /**
     * Removes the minimum element from the tree.
     * 从树中删除最小元素。
     *
     * @return the removed minimum element - 删除的最小元素
     */
    public Optional<T> deleteMin() {
        if (root == null) {
            return Optional.empty();
        }
        T min = findMin(root).data;
        root = deleteMin(root);
        size--;
        return Optional.of(min);
    }

    private Node<T> deleteMin(Node<T> node) {
        if (node.left == null) {
            return node.right;
        }
        node.left = deleteMin(node.left);
        return balance(node);
    }

    /**
     * Removes the maximum element from the tree.
     * 从树中删除最大元素。
     *
     * @return the removed maximum element - 删除的最大元素
     */
    public Optional<T> deleteMax() {
        if (root == null) {
            return Optional.empty();
        }
        T max = findMax(root).data;
        root = deleteMax(root);
        size--;
        return Optional.of(max);
    }

    private Node<T> deleteMax(Node<T> node) {
        if (node.right == null) {
            return node.left;
        }
        node.right = deleteMax(node.right);
        return balance(node);
    }

    // ==================== Search Operations | 搜索操作 ====================

    /**
     * Returns true if the tree contains the specified element.
     * 如果树包含指定元素返回 true。
     *
     * @param data the element to search for - 要搜索的元素
     * @return true if found - 如果找到返回 true
     */
    public boolean contains(T data) {
        return search(root, data) != null;
    }

    /**
     * Searches for an element in the tree.
     * 在树中搜索元素。
     *
     * @param data the element to search for - 要搜索的元素
     * @return the element if found - 如果找到返回元素
     */
    public Optional<T> search(T data) {
        Node<T> node = search(root, data);
        return node != null ? Optional.of(node.data) : Optional.empty();
    }

    private Node<T> search(Node<T> node, T data) {
        if (node == null) {
            return null;
        }

        int cmp = compare(data, node.data);

        if (cmp < 0) {
            return search(node.left, data);
        } else if (cmp > 0) {
            return search(node.right, data);
        } else {
            return node;
        }
    }

    /**
     * Returns the minimum element in the tree.
     * 返回树中的最小元素。
     */
    public Optional<T> min() {
        if (root == null) {
            return Optional.empty();
        }
        return Optional.of(findMin(root).data);
    }

    /**
     * Returns the maximum element in the tree.
     * 返回树中的最大元素。
     */
    public Optional<T> max() {
        if (root == null) {
            return Optional.empty();
        }
        return Optional.of(findMax(root).data);
    }

    private Node<T> findMin(Node<T> node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    private Node<T> findMax(Node<T> node) {
        while (node.right != null) {
            node = node.right;
        }
        return node;
    }

    // ==================== Traversal Operations | 遍历操作 ====================

    /**
     * Performs in-order traversal (sorted order).
     * 执行中序遍历（排序顺序）。
     *
     * @param action the action to perform - 要执行的操作
     */
    public void inOrderTraversal(Consumer<T> action) {
        inOrder(root, action);
    }

    private void inOrder(Node<T> node, Consumer<T> action) {
        if (node != null) {
            inOrder(node.left, action);
            action.accept(node.data);
            inOrder(node.right, action);
        }
    }

    /**
     * Performs pre-order traversal.
     * 执行前序遍历。
     *
     * @param action the action to perform - 要执行的操作
     */
    public void preOrderTraversal(Consumer<T> action) {
        preOrder(root, action);
    }

    private void preOrder(Node<T> node, Consumer<T> action) {
        if (node != null) {
            action.accept(node.data);
            preOrder(node.left, action);
            preOrder(node.right, action);
        }
    }

    /**
     * Performs post-order traversal.
     * 执行后序遍历。
     *
     * @param action the action to perform - 要执行的操作
     */
    public void postOrderTraversal(Consumer<T> action) {
        postOrder(root, action);
    }

    private void postOrder(Node<T> node, Consumer<T> action) {
        if (node != null) {
            postOrder(node.left, action);
            postOrder(node.right, action);
            action.accept(node.data);
        }
    }

    /**
     * Performs level-order (breadth-first) traversal.
     * 执行层序（广度优先）遍历。
     *
     * @param action the action to perform - 要执行的操作
     */
    public void levelOrderTraversal(Consumer<T> action) {
        if (root == null) return;

        Queue<Node<T>> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Node<T> node = queue.poll();
            action.accept(node.data);

            if (node.left != null) queue.add(node.left);
            if (node.right != null) queue.add(node.right);
        }
    }

    /**
     * Returns a sorted list of all elements.
     * 返回所有元素的排序列表。
     */
    public List<T> toSortedList() {
        List<T> result = new ArrayList<>(size);
        inOrderTraversal(result::add);
        return result;
    }

    // ==================== Range Operations | 范围操作 ====================

    /**
     * Returns all elements in the given range.
     * 返回给定范围内的所有元素。
     *
     * @param from the lower bound (inclusive) - 下界（包含）
     * @param to the upper bound (inclusive) - 上界（包含）
     * @return elements in range - 范围内的元素
     */
    public List<T> range(T from, T to) {
        List<T> result = new ArrayList<>();
        rangeSearch(root, from, to, result);
        return result;
    }

    private void rangeSearch(Node<T> node, T from, T to, List<T> result) {
        if (node == null) return;

        int cmpFrom = compare(from, node.data);
        int cmpTo = compare(to, node.data);

        if (cmpFrom < 0) {
            rangeSearch(node.left, from, to, result);
        }

        if (cmpFrom <= 0 && cmpTo >= 0) {
            result.add(node.data);
        }

        if (cmpTo > 0) {
            rangeSearch(node.right, from, to, result);
        }
    }

    // ==================== Tree Info | 树信息 ====================

    /**
     * Returns the height of the tree.
     * 返回树的高度。
     */
    public int height() {
        return height(root);
    }

    private int height(Node<T> node) {
        return node == null ? 0 : node.height;
    }

    /**
     * Checks if the tree is balanced.
     * 检查树是否平衡。
     */
    public boolean isBalanced() {
        return isBalanced(root);
    }

    private boolean isBalanced(Node<T> node) {
        if (node == null) return true;
        int balance = getBalance(node);
        return Math.abs(balance) <= 1 && isBalanced(node.left) && isBalanced(node.right);
    }

    // ==================== Balancing | 平衡 ====================

    private Node<T> balance(Node<T> node) {
        updateHeight(node);

        int balance = getBalance(node);

        // Left heavy
        if (balance > 1) {
            if (getBalance(node.left) < 0) {
                node.left = rotateLeft(node.left);
            }
            return rotateRight(node);
        }

        // Right heavy
        if (balance < -1) {
            if (getBalance(node.right) > 0) {
                node.right = rotateRight(node.right);
            }
            return rotateLeft(node);
        }

        return node;
    }

    private int getBalance(Node<T> node) {
        return node == null ? 0 : height(node.left) - height(node.right);
    }

    private void updateHeight(Node<T> node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    private Node<T> rotateRight(Node<T> y) {
        Node<T> x = y.left;
        Node<T> T2 = x.right;

        x.right = y;
        y.left = T2;

        updateHeight(y);
        updateHeight(x);

        return x;
    }

    private Node<T> rotateLeft(Node<T> x) {
        Node<T> y = x.right;
        Node<T> T2 = y.left;

        y.left = x;
        x.right = T2;

        updateHeight(x);
        updateHeight(y);

        return y;
    }

    // ==================== Comparison | 比较 ====================

    @SuppressWarnings("unchecked")
    private int compare(T a, T b) {
        if (comparator != null) {
            return comparator.compare(a, b);
        }
        return ((Comparable<? super T>) a).compareTo(b);
    }

    // ==================== To String | 字符串表示 ====================

    @Override
    public String toString() {
        return "AvlTree[size=" + size + ", height=" + height() + "]";
    }
}
