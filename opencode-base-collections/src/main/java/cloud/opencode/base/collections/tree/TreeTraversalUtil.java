package cloud.opencode.base.collections.tree;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * TreeTraversalUtil - Tree Traversal Utilities
 * TreeTraversalUtil - 树遍历工具
 *
 * <p>Provides utilities for traversing tree structures.</p>
 * <p>提供遍历树结构的工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pre-order traversal - 前序遍历</li>
 *   <li>Post-order traversal - 后序遍历</li>
 *   <li>Level-order (BFS) traversal - 层序（广度优先）遍历</li>
 *   <li>Generic tree support - 通用树支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Define a tree node with children getter - 定义获取子节点的树节点
 * TreeTraversalUtil.preOrder(rootNode, Node::getChildren, node -> {
 *     System.out.println(node.getValue());
 * });
 *
 * // Level-order traversal - 层序遍历
 * TreeTraversalUtil.levelOrder(rootNode, Node::getChildren, node -> {
 *     System.out.println(node.getValue());
 * });
 *
 * // Collect all nodes - 收集所有节点
 * List<Node> allNodes = TreeTraversalUtil.collectPreOrder(rootNode, Node::getChildren);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 是（无状态工具）</li>
 *   <li>Null-safe: No (root must not be null) - 否（根节点不能为null）</li>
 * </ul>
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for all traversals where n is the number of nodes - 时间复杂度: 所有遍历均为 O(n)，n为节点数量</li>
 *   <li>Space complexity: O(h) for pre/post-order where h is the tree height; O(n) for level-order queue - 空间复杂度: 前/后序遍历为 O(h)，h为树高；层序遍历队列为 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class TreeTraversalUtil {

    private TreeTraversalUtil() {
    }

    // ==================== 前序遍历 | Pre-order Traversal ====================

    /**
     * Perform pre-order traversal.
     * 执行前序遍历。
     *
     * @param <T>             node type | 节点类型
     * @param root            the root node | 根节点
     * @param childrenGetter  function to get children | 获取子节点的函数
     * @param action          action to perform on each node | 对每个节点执行的操作
     */
    public static <T> void preOrder(T root, Function<T, ? extends Iterable<T>> childrenGetter, Consumer<T> action) {
        if (root == null) return;
        action.accept(root);
        Iterable<T> children = childrenGetter.apply(root);
        if (children != null) {
            for (T child : children) {
                preOrder(child, childrenGetter, action);
            }
        }
    }

    /**
     * Perform pre-order traversal iteratively (non-recursive).
     * 执行迭代式（非递归）前序遍历。
     *
     * @param <T>             node type | 节点类型
     * @param root            the root node | 根节点
     * @param childrenGetter  function to get children | 获取子节点的函数
     * @param action          action to perform on each node | 对每个节点执行的操作
     */
    public static <T> void preOrderIterative(T root, Function<T, ? extends Iterable<T>> childrenGetter, Consumer<T> action) {
        if (root == null) return;
        Deque<T> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            T node = stack.pop();
            action.accept(node);
            Iterable<T> children = childrenGetter.apply(node);
            if (children != null) {
                List<T> childList = new ArrayList<>();
                for (T child : children) {
                    childList.add(child);
                }
                // Add in reverse order to process left-to-right
                for (int i = childList.size() - 1; i >= 0; i--) {
                    stack.push(childList.get(i));
                }
            }
        }
    }

    /**
     * Collect all nodes in pre-order.
     * 按前序收集所有节点。
     *
     * @param <T>             node type | 节点类型
     * @param root            the root node | 根节点
     * @param childrenGetter  function to get children | 获取子节点的函数
     * @return list of nodes in pre-order | 前序节点列表
     */
    public static <T> List<T> collectPreOrder(T root, Function<T, ? extends Iterable<T>> childrenGetter) {
        List<T> result = new ArrayList<>();
        preOrder(root, childrenGetter, result::add);
        return result;
    }

    // ==================== 后序遍历 | Post-order Traversal ====================

    /**
     * Perform post-order traversal.
     * 执行后序遍历。
     *
     * @param <T>             node type | 节点类型
     * @param root            the root node | 根节点
     * @param childrenGetter  function to get children | 获取子节点的函数
     * @param action          action to perform on each node | 对每个节点执行的操作
     */
    public static <T> void postOrder(T root, Function<T, ? extends Iterable<T>> childrenGetter, Consumer<T> action) {
        if (root == null) return;
        Iterable<T> children = childrenGetter.apply(root);
        if (children != null) {
            for (T child : children) {
                postOrder(child, childrenGetter, action);
            }
        }
        action.accept(root);
    }

    /**
     * Collect all nodes in post-order.
     * 按后序收集所有节点。
     *
     * @param <T>             node type | 节点类型
     * @param root            the root node | 根节点
     * @param childrenGetter  function to get children | 获取子节点的函数
     * @return list of nodes in post-order | 后序节点列表
     */
    public static <T> List<T> collectPostOrder(T root, Function<T, ? extends Iterable<T>> childrenGetter) {
        List<T> result = new ArrayList<>();
        postOrder(root, childrenGetter, result::add);
        return result;
    }

    // ==================== 层序遍历 | Level-order (BFS) Traversal ====================

    /**
     * Perform level-order (BFS) traversal.
     * 执行层序（广度优先）遍历。
     *
     * @param <T>             node type | 节点类型
     * @param root            the root node | 根节点
     * @param childrenGetter  function to get children | 获取子节点的函数
     * @param action          action to perform on each node | 对每个节点执行的操作
     */
    public static <T> void levelOrder(T root, Function<T, ? extends Iterable<T>> childrenGetter, Consumer<T> action) {
        if (root == null) return;
        Queue<T> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            T node = queue.poll();
            action.accept(node);
            Iterable<T> children = childrenGetter.apply(node);
            if (children != null) {
                for (T child : children) {
                    queue.offer(child);
                }
            }
        }
    }

    /**
     * Collect all nodes in level-order.
     * 按层序收集所有节点。
     *
     * @param <T>             node type | 节点类型
     * @param root            the root node | 根节点
     * @param childrenGetter  function to get children | 获取子节点的函数
     * @return list of nodes in level-order | 层序节点列表
     */
    public static <T> List<T> collectLevelOrder(T root, Function<T, ? extends Iterable<T>> childrenGetter) {
        List<T> result = new ArrayList<>();
        levelOrder(root, childrenGetter, result::add);
        return result;
    }

    /**
     * Collect nodes by level.
     * 按层收集节点。
     *
     * @param <T>             node type | 节点类型
     * @param root            the root node | 根节点
     * @param childrenGetter  function to get children | 获取子节点的函数
     * @return list of levels, each containing nodes at that level | 层列表，每层包含该层的节点
     */
    public static <T> List<List<T>> collectByLevel(T root, Function<T, ? extends Iterable<T>> childrenGetter) {
        if (root == null) return Collections.emptyList();
        List<List<T>> result = new ArrayList<>();
        Queue<T> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            List<T> level = new ArrayList<>(levelSize);
            for (int i = 0; i < levelSize; i++) {
                T node = queue.poll();
                level.add(node);
                Iterable<T> children = childrenGetter.apply(node);
                if (children != null) {
                    for (T child : children) {
                        queue.offer(child);
                    }
                }
            }
            result.add(level);
        }
        return result;
    }

    // ==================== 工具方法 | Utility Methods ====================

    /**
     * Calculate the depth (height) of the tree.
     * 计算树的深度（高度）。
     *
     * @param <T>             node type | 节点类型
     * @param root            the root node | 根节点
     * @param childrenGetter  function to get children | 获取子节点的函数
     * @return the depth of the tree | 树的深度
     */
    public static <T> int depth(T root, Function<T, ? extends Iterable<T>> childrenGetter) {
        if (root == null) return 0;
        int maxChildDepth = 0;
        Iterable<T> children = childrenGetter.apply(root);
        if (children != null) {
            for (T child : children) {
                maxChildDepth = Math.max(maxChildDepth, depth(child, childrenGetter));
            }
        }
        return 1 + maxChildDepth;
    }

    /**
     * Count the total number of nodes.
     * 计算节点总数。
     *
     * @param <T>             node type | 节点类型
     * @param root            the root node | 根节点
     * @param childrenGetter  function to get children | 获取子节点的函数
     * @return the total number of nodes | 节点总数
     */
    public static <T> int count(T root, Function<T, ? extends Iterable<T>> childrenGetter) {
        if (root == null) return 0;
        int count = 1;
        Iterable<T> children = childrenGetter.apply(root);
        if (children != null) {
            for (T child : children) {
                count += count(child, childrenGetter);
            }
        }
        return count;
    }

    /**
     * Find a node by predicate.
     * 根据谓词查找节点。
     *
     * @param <T>             node type | 节点类型
     * @param root            the root node | 根节点
     * @param childrenGetter  function to get children | 获取子节点的函数
     * @param predicate       the predicate | 谓词
     * @return the found node or null | 找到的节点或 null
     */
    public static <T> T find(T root, Function<T, ? extends Iterable<T>> childrenGetter, java.util.function.Predicate<T> predicate) {
        if (root == null) return null;
        if (predicate.test(root)) return root;
        Iterable<T> children = childrenGetter.apply(root);
        if (children != null) {
            for (T child : children) {
                T found = find(child, childrenGetter, predicate);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * Find all nodes matching predicate.
     * 查找所有匹配谓词的节点。
     *
     * @param <T>             node type | 节点类型
     * @param root            the root node | 根节点
     * @param childrenGetter  function to get children | 获取子节点的函数
     * @param predicate       the predicate | 谓词
     * @return list of matching nodes | 匹配节点列表
     */
    public static <T> List<T> findAll(T root, Function<T, ? extends Iterable<T>> childrenGetter, java.util.function.Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        preOrder(root, childrenGetter, node -> {
            if (predicate.test(node)) {
                result.add(node);
            }
        });
        return result;
    }

    /**
     * Get the path from root to a node.
     * 获取从根到节点的路径。
     *
     * @param <T>             node type | 节点类型
     * @param root            the root node | 根节点
     * @param target          the target node | 目标节点
     * @param childrenGetter  function to get children | 获取子节点的函数
     * @return the path from root to target, or empty list if not found | 从根到目标的路径，如果未找到则为空列表
     */
    public static <T> List<T> pathTo(T root, T target, Function<T, ? extends Iterable<T>> childrenGetter) {
        List<T> path = new ArrayList<>();
        if (findPath(root, target, childrenGetter, path)) {
            return path;
        }
        return Collections.emptyList();
    }

    private static <T> boolean findPath(T node, T target, Function<T, ? extends Iterable<T>> childrenGetter, List<T> path) {
        if (node == null) return false;
        path.add(node);
        if (node.equals(target)) return true;
        Iterable<T> children = childrenGetter.apply(node);
        if (children != null) {
            for (T child : children) {
                if (findPath(child, target, childrenGetter, path)) {
                    return true;
                }
            }
        }
        path.remove(path.size() - 1);
        return false;
    }
}
