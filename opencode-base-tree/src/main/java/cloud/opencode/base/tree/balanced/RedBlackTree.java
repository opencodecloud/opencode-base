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
 * Red-Black Tree - Self-balancing binary search tree
 * 红黑树 - 自平衡二叉搜索树
 *
 * <p>A Red-Black tree maintains balance using color properties:</p>
 * <ul>
 *   <li>Every node is either red or black</li>
 *   <li>The root is black</li>
 *   <li>All leaves (NIL) are black</li>
 *   <li>Red nodes have black children</li>
 *   <li>All paths from root to leaves have the same black count</li>
 * </ul>
 *
 * <p>红黑树使用颜色属性维持平衡：</p>
 * <ul>
 *   <li>每个节点是红色或黑色</li>
 *   <li>根节点是黑色</li>
 *   <li>所有叶子（NIL）是黑色</li>
 *   <li>红色节点的子节点是黑色</li>
 *   <li>从根到叶子的所有路径有相同的黑色节点数</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Self-balancing binary search tree with color properties - 使用颜色属性的自平衡二叉搜索树</li>
 *   <li>O(log n) insert, delete, and search - O(log n)插入、删除和搜索</li>
 *   <li>Range queries - 范围查询</li>
 *   <li>Multiple traversal orders - 多种遍历顺序</li>
 *   <li>Custom comparator support - 自定义比较器支持</li>
 *   <li>Black height tracking - 黑色高度跟踪</li>
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
 * // Create a Red-Black tree
 * RedBlackTree<Integer> tree = new RedBlackTree<>();
 *
 * // Insert elements
 * tree.insert(5, 3, 7, 2, 4, 6, 8);
 *
 * // Search
 * boolean found = tree.contains(5);  // true
 *
 * // Delete
 * tree.delete(5);
 *
 * // Get sorted elements
 * List<Integer> sorted = tree.toSortedList();
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
 * @see AvlTree
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public class RedBlackTree<T> {

    private static final boolean RED = true;
    private static final boolean BLACK = false;

    private Node<T> root;
    private final Comparator<? super T> comparator;
    private int size;

    // ==================== Constructors | 构造函数 ====================

    /**
     * Creates an empty Red-Black tree with natural ordering.
     * 使用自然顺序创建空的红黑树。
     */
    public RedBlackTree() {
        this(null);
    }

    /**
     * Creates an empty Red-Black tree with the specified comparator.
     * 使用指定的比较器创建空的红黑树。
     *
     * @param comparator the comparator to use - 要使用的比较器
     */
    public RedBlackTree(Comparator<? super T> comparator) {
        this.comparator = comparator;
        this.root = null;
        this.size = 0;
    }

    // ==================== Node Class | 节点类 ====================

    private static class Node<T> {
        T data;
        Node<T> left;
        Node<T> right;
        Node<T> parent;
        boolean color;

        Node(T data, boolean color, Node<T> parent) {
            this.data = data;
            this.color = color;
            this.parent = parent;
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
     * @return true if inserted - 如果插入成功返回 true
     */
    public boolean insert(T data) {
        if (root == null) {
            root = new Node<>(data, BLACK, null);
            size++;
            return true;
        }

        Node<T> parent = null;
        Node<T> current = root;
        int cmp = 0;

        while (current != null) {
            parent = current;
            cmp = compare(data, current.data);

            if (cmp < 0) {
                current = current.left;
            } else if (cmp > 0) {
                current = current.right;
            } else {
                // Duplicate - update value
                current.data = data;
                return false;
            }
        }

        Node<T> newNode = new Node<>(data, RED, parent);

        if (cmp < 0) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }

        fixAfterInsert(newNode);
        size++;
        return true;
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

    private void fixAfterInsert(Node<T> node) {
        while (node != root && isRed(node.parent)) {
            Node<T> parent = node.parent;
            Node<T> grandparent = parent.parent;

            if (grandparent == null) break;

            if (parent == grandparent.left) {
                Node<T> uncle = grandparent.right;

                if (isRed(uncle)) {
                    // Case 1: Uncle is red
                    setColor(parent, BLACK);
                    setColor(uncle, BLACK);
                    setColor(grandparent, RED);
                    node = grandparent;
                } else {
                    if (node == parent.right) {
                        // Case 2: Node is right child
                        node = parent;
                        rotateLeft(node);
                        parent = node.parent;
                        grandparent = parent.parent;
                    }
                    // Case 3: Node is left child
                    setColor(parent, BLACK);
                    setColor(grandparent, RED);
                    rotateRight(grandparent);
                }
            } else {
                Node<T> uncle = grandparent.left;

                if (isRed(uncle)) {
                    setColor(parent, BLACK);
                    setColor(uncle, BLACK);
                    setColor(grandparent, RED);
                    node = grandparent;
                } else {
                    if (node == parent.left) {
                        node = parent;
                        rotateRight(node);
                        parent = node.parent;
                        grandparent = parent.parent;
                    }
                    setColor(parent, BLACK);
                    setColor(grandparent, RED);
                    rotateLeft(grandparent);
                }
            }
        }
        root.color = BLACK;
    }

    // ==================== Delete Operations | 删除操作 ====================

    /**
     * Deletes an element from the tree.
     * 从树中删除元素。
     *
     * @param data the element to delete - 要删除的元素
     * @return true if deleted - 如果删除成功返回 true
     */
    public boolean delete(T data) {
        Node<T> node = findNode(data);
        if (node == null) {
            return false;
        }

        deleteNode(node);
        size--;
        return true;
    }

    private void deleteNode(Node<T> node) {
        // Find replacement node
        if (node.left != null && node.right != null) {
            Node<T> successor = findMinNode(node.right);
            node.data = successor.data;
            node = successor;
        }

        Node<T> replacement = (node.left != null) ? node.left : node.right;

        if (replacement != null) {
            replacement.parent = node.parent;

            if (node.parent == null) {
                root = replacement;
            } else if (node == node.parent.left) {
                node.parent.left = replacement;
            } else {
                node.parent.right = replacement;
            }

            node.left = node.right = node.parent = null;

            if (node.color == BLACK) {
                fixAfterDelete(replacement);
            }
        } else if (node.parent == null) {
            root = null;
        } else {
            if (node.color == BLACK) {
                fixAfterDelete(node);
            }

            if (node.parent != null) {
                if (node == node.parent.left) {
                    node.parent.left = null;
                } else {
                    node.parent.right = null;
                }
                node.parent = null;
            }
        }
    }

    private void fixAfterDelete(Node<T> node) {
        while (node != root && !isRed(node)) {
            if (node.parent == null) break;

            if (node == node.parent.left) {
                Node<T> sibling = node.parent.right;

                if (isRed(sibling)) {
                    setColor(sibling, BLACK);
                    setColor(node.parent, RED);
                    rotateLeft(node.parent);
                    sibling = node.parent.right;
                }

                if (sibling == null) break;

                if (!isRed(sibling.left) && !isRed(sibling.right)) {
                    setColor(sibling, RED);
                    node = node.parent;
                } else {
                    if (!isRed(sibling.right)) {
                        setColor(sibling.left, BLACK);
                        setColor(sibling, RED);
                        rotateRight(sibling);
                        sibling = node.parent.right;
                    }
                    if (sibling != null) {
                        setColor(sibling, node.parent.color);
                        setColor(node.parent, BLACK);
                        setColor(sibling.right, BLACK);
                    }
                    rotateLeft(node.parent);
                    node = root;
                }
            } else {
                Node<T> sibling = node.parent.left;

                if (isRed(sibling)) {
                    setColor(sibling, BLACK);
                    setColor(node.parent, RED);
                    rotateRight(node.parent);
                    sibling = node.parent.left;
                }

                if (sibling == null) break;

                if (!isRed(sibling.right) && !isRed(sibling.left)) {
                    setColor(sibling, RED);
                    node = node.parent;
                } else {
                    if (!isRed(sibling.left)) {
                        setColor(sibling.right, BLACK);
                        setColor(sibling, RED);
                        rotateLeft(sibling);
                        sibling = node.parent.left;
                    }
                    if (sibling != null) {
                        setColor(sibling, node.parent.color);
                        setColor(node.parent, BLACK);
                        setColor(sibling.left, BLACK);
                    }
                    rotateRight(node.parent);
                    node = root;
                }
            }
        }
        setColor(node, BLACK);
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
        return findNode(data) != null;
    }

    /**
     * Searches for an element in the tree.
     * 在树中搜索元素。
     *
     * @param data the element to search for - 要搜索的元素
     * @return the element if found - 如果找到返回元素
     */
    public Optional<T> search(T data) {
        Node<T> node = findNode(data);
        return node != null ? Optional.of(node.data) : Optional.empty();
    }

    private Node<T> findNode(T data) {
        Node<T> current = root;

        while (current != null) {
            int cmp = compare(data, current.data);

            if (cmp < 0) {
                current = current.left;
            } else if (cmp > 0) {
                current = current.right;
            } else {
                return current;
            }
        }

        return null;
    }

    /**
     * Returns the minimum element in the tree.
     * 返回树中的最小元素。
     */
    public Optional<T> min() {
        if (root == null) {
            return Optional.empty();
        }
        return Optional.of(findMinNode(root).data);
    }

    /**
     * Returns the maximum element in the tree.
     * 返回树中的最大元素。
     */
    public Optional<T> max() {
        if (root == null) {
            return Optional.empty();
        }
        return Optional.of(findMaxNode(root).data);
    }

    private Node<T> findMinNode(Node<T> node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    private Node<T> findMaxNode(Node<T> node) {
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
        if (node == null) return 0;
        return 1 + Math.max(height(node.left), height(node.right));
    }

    /**
     * Returns the black height (number of black nodes to any leaf).
     * 返回黑色高度（到任意叶子的黑色节点数）。
     */
    public int blackHeight() {
        int count = 0;
        Node<T> node = root;
        while (node != null) {
            if (!isRed(node)) count++;
            node = node.left;
        }
        return count;
    }

    // ==================== Helper Methods | 辅助方法 ====================

    private void rotateLeft(Node<T> node) {
        Node<T> right = node.right;
        node.right = right.left;

        if (right.left != null) {
            right.left.parent = node;
        }

        right.parent = node.parent;

        if (node.parent == null) {
            root = right;
        } else if (node == node.parent.left) {
            node.parent.left = right;
        } else {
            node.parent.right = right;
        }

        right.left = node;
        node.parent = right;
    }

    private void rotateRight(Node<T> node) {
        Node<T> left = node.left;
        node.left = left.right;

        if (left.right != null) {
            left.right.parent = node;
        }

        left.parent = node.parent;

        if (node.parent == null) {
            root = left;
        } else if (node == node.parent.right) {
            node.parent.right = left;
        } else {
            node.parent.left = left;
        }

        left.right = node;
        node.parent = left;
    }

    private boolean isRed(Node<T> node) {
        return node != null && node.color == RED;
    }

    private void setColor(Node<T> node, boolean color) {
        if (node != null) {
            node.color = color;
        }
    }

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
        return "RedBlackTree[size=" + size + ", height=" + height() + ", blackHeight=" + blackHeight() + "]";
    }
}
