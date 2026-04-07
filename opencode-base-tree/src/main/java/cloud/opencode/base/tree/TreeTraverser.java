package cloud.opencode.base.tree;

import cloud.opencode.base.tree.exception.TreeException;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Tree Traverser - Advanced tree traversal utilities
 * 树遍历器 - 高级树遍历工具
 *
 * <p>Provides comprehensive traversal operations including controlled traversal,
 * transformation, reduction, and stream-based operations.</p>
 * <p>提供全面的遍历操作，包括受控遍历、转换、归约和基于流的操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Controlled traversal (skip, stop) - 受控遍历（跳过、停止）</li>
 *   <li>Parallel traversal - 并行遍历</li>
 *   <li>Stream-based operations - 基于流的操作</li>
 *   <li>Transform/reduce operations - 转换/归约操作</li>
 *   <li>Iterator support - 迭代器支持</li>
 *   <li>Ancestor/descendant queries - 祖先/后代查询</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Stream all nodes - 流式处理所有节点
 * TreeTraverser.stream(root)
 *     .filter(node -> node.getData() != null)
 *     .forEach(System.out::println);
 *
 * // Controlled traversal - 受控遍历
 * TreeTraverser.traverse(root, node -> {
 *     if (shouldSkip(node)) return TraversalControl.SKIP_SUBTREE;
 *     if (shouldStop(node)) return TraversalControl.STOP;
 *     return TraversalControl.CONTINUE;
 * });
 *
 * // Transform tree - 转换树
 * TreeNode<String> transformed = TreeTraverser.map(root, node -> node.toString());
 *
 * // Reduce tree - 归约树
 * int sum = TreeTraverser.reduce(root, 0, (acc, node) -> acc + node.getValue());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (null root will cause NullPointerException) - 空值安全: 否（null根节点会导致空指针异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public final class TreeTraverser {

    private static final int MAX_DEPTH = 1000;

    private TreeTraverser() {}

    // ==================== Traversal Control | 遍历控制 ====================

    /**
     * Traversal control enum
     * 遍历控制枚举
     */
    public enum TraversalControl {
        /** Continue to children | 继续到子节点 */
        CONTINUE,
        /** Skip children, continue siblings | 跳过子节点，继续兄弟节点 */
        SKIP_SUBTREE,
        /** Stop traversal entirely | 完全停止遍历 */
        STOP
    }

    /**
     * Controlled traversal visitor
     * 受控遍历访问者
     *
     * @param <T> data type | 数据类型
     */
    @FunctionalInterface
    public interface ControlledVisitor<T> {
        TraversalControl visit(TreeNode<T> node);
    }

    /**
     * Traverses tree with control
     * 带控制的树遍历
     *
     * @param root root node | 根节点
     * @param visitor visitor with control | 带控制的访问者
     * @param <T> data type | 数据类型
     * @return true if completed without stop | 如果完成且未停止则为true
     */
    public static <T> boolean traverse(TreeNode<T> root, ControlledVisitor<T> visitor) {
        // Iterative DFS with traversal control
        Deque<TreeNode<T>> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            TreeNode<T> node = stack.pop();
            TraversalControl control = visitor.visit(node);
            switch (control) {
                case STOP -> { return false; }
                case SKIP_SUBTREE -> { /* skip children */ }
                case CONTINUE -> {
                    List<TreeNode<T>> children = node.getChildren();
                    for (int i = children.size() - 1; i >= 0; i--) {
                        stack.push(children.get(i));
                    }
                }
            }
        }
        return true;
    }

    /**
     * Traverses Treeable nodes with control
     * 带控制的Treeable节点遍历
     *
     * @param roots root nodes | 根节点列表
     * @param visitor visitor with control | 带控制的访问者
     * @param <T> node type | 节点类型
     * @param <ID> ID type | ID类型
     * @return true if completed without stop | 如果完成且未停止则为true
     */
    public static <T extends Treeable<T, ID>, ID> boolean traverse(
            List<T> roots, Function<T, TraversalControl> visitor) {
        // Iterative DFS with traversal control
        Deque<T> stack = new ArrayDeque<>();
        for (int i = roots.size() - 1; i >= 0; i--) {
            stack.push(roots.get(i));
        }
        while (!stack.isEmpty()) {
            T node = stack.pop();
            TraversalControl control = visitor.apply(node);
            switch (control) {
                case STOP -> { return false; }
                case SKIP_SUBTREE -> { /* skip children */ }
                case CONTINUE -> {
                    List<T> children = node.getChildren();
                    if (children != null) {
                        for (int i = children.size() - 1; i >= 0; i--) {
                            stack.push(children.get(i));
                        }
                    }
                }
            }
        }
        return true;
    }

    // ==================== Stream Operations | 流操作 ====================

    /**
     * Creates a pre-order stream of TreeNode
     * 创建TreeNode的前序流
     *
     * @param root root node | 根节点
     * @param <T> data type | 数据类型
     * @return stream of nodes | 节点流
     */
    public static <T> Stream<TreeNode<T>> stream(TreeNode<T> root) {
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                preOrderIterator(root), Spliterator.ORDERED), false);
    }

    /**
     * Creates a pre-order stream of Treeable nodes
     * 创建Treeable节点的前序流
     *
     * @param roots root nodes | 根节点列表
     * @param <T> node type | 节点类型
     * @param <ID> ID type | ID类型
     * @return stream of nodes | 节点流
     */
    public static <T extends Treeable<T, ID>, ID> Stream<T> stream(List<T> roots) {
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                preOrderIterator(roots), Spliterator.ORDERED), false);
    }

    /**
     * Creates a parallel stream
     * 创建并行流
     *
     * @param root root node | 根节点
     * @param <T> data type | 数据类型
     * @return parallel stream of nodes | 节点并行流
     */
    public static <T> Stream<TreeNode<T>> parallelStream(TreeNode<T> root) {
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                preOrderIterator(root), Spliterator.ORDERED), true);
    }

    /**
     * Creates a breadth-first stream
     * 创建广度优先流
     *
     * @param root root node | 根节点
     * @param <T> data type | 数据类型
     * @return stream of nodes | 节点流
     */
    public static <T> Stream<TreeNode<T>> breadthFirstStream(TreeNode<T> root) {
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                breadthFirstIterator(root), Spliterator.ORDERED), false);
    }

    /**
     * Creates a post-order stream
     * 创建后序流
     *
     * @param root root node | 根节点
     * @param <T> data type | 数据类型
     * @return stream of nodes | 节点流
     */
    public static <T> Stream<TreeNode<T>> postOrderStream(TreeNode<T> root) {
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                postOrderIterator(root), Spliterator.ORDERED), false);
    }

    // ==================== Iterators | 迭代器 ====================

    /**
     * Creates a pre-order iterator
     * 创建前序迭代器
     *
     * @param root root node | 根节点
     * @param <T> data type | 数据类型
     * @return iterator | 迭代器
     */
    public static <T> Iterator<TreeNode<T>> preOrderIterator(TreeNode<T> root) {
        return new PreOrderIterator<>(root);
    }

    /**
     * Creates a pre-order iterator for Treeable
     * 创建Treeable的前序迭代器
     *
     * @param roots root nodes | 根节点列表
     * @param <T> node type | 节点类型
     * @param <ID> ID type | ID类型
     * @return iterator | 迭代器
     */
    public static <T extends Treeable<T, ID>, ID> Iterator<T> preOrderIterator(List<T> roots) {
        return new TreeablePreOrderIterator<>(roots);
    }

    /**
     * Creates a breadth-first iterator
     * 创建广度优先迭代器
     *
     * @param root root node | 根节点
     * @param <T> data type | 数据类型
     * @return iterator | 迭代器
     */
    public static <T> Iterator<TreeNode<T>> breadthFirstIterator(TreeNode<T> root) {
        return new BreadthFirstIterator<>(root);
    }

    /**
     * Creates a post-order iterator
     * 创建后序迭代器
     *
     * @param root root node | 根节点
     * @param <T> data type | 数据类型
     * @return iterator | 迭代器
     */
    public static <T> Iterator<TreeNode<T>> postOrderIterator(TreeNode<T> root) {
        return new PostOrderIterator<>(root);
    }

    // ==================== Transformation | 转换 ====================

    /**
     * Maps tree to new data type
     * 将树映射到新数据类型
     *
     * @param root root node | 根节点
     * @param mapper data mapper | 数据映射器
     * @param <T> source type | 源类型
     * @param <R> result type | 结果类型
     * @return mapped tree | 映射后的树
     */
    public static <T, R> TreeNode<R> map(TreeNode<T> root, Function<T, R> mapper) {
        // Iterative BFS mapping
        TreeNode<R> mappedRoot = new TreeNode<>(mapper.apply(root.getData()));
        Deque<TreeNode<T>> sourceQueue = new ArrayDeque<>();
        Deque<TreeNode<R>> targetQueue = new ArrayDeque<>();
        sourceQueue.offer(root);
        targetQueue.offer(mappedRoot);
        while (!sourceQueue.isEmpty()) {
            TreeNode<T> src = sourceQueue.poll();
            TreeNode<R> tgt = targetQueue.poll();
            for (TreeNode<T> child : src.getChildren()) {
                TreeNode<R> mc = tgt.addChild(mapper.apply(child.getData()));
                sourceQueue.offer(child);
                targetQueue.offer(mc);
            }
        }
        return mappedRoot;
    }

    /**
     * Maps tree with node context
     * 带节点上下文的树映射
     *
     * @param root root node | 根节点
     * @param mapper node mapper | 节点映射器
     * @param <T> source type | 源类型
     * @param <R> result type | 结果类型
     * @return mapped tree | 映射后的树
     */
    public static <T, R> TreeNode<R> mapNode(TreeNode<T> root, Function<TreeNode<T>, R> mapper) {
        TreeNode<R> mappedRoot = new TreeNode<>(mapper.apply(root));
        Deque<TreeNode<T>> sourceQueue = new ArrayDeque<>();
        Deque<TreeNode<R>> targetQueue = new ArrayDeque<>();
        sourceQueue.offer(root);
        targetQueue.offer(mappedRoot);
        while (!sourceQueue.isEmpty()) {
            TreeNode<T> src = sourceQueue.poll();
            TreeNode<R> tgt = targetQueue.poll();
            for (TreeNode<T> child : src.getChildren()) {
                TreeNode<R> mc = tgt.addChild(mapper.apply(child));
                sourceQueue.offer(child);
                targetQueue.offer(mc);
            }
        }
        return mappedRoot;
    }

    /**
     * Flat maps tree to stream
     * 扁平映射树到流
     *
     * @param root root node | 根节点
     * @param mapper node to stream mapper | 节点到流映射器
     * @param <T> source type | 源类型
     * @param <R> result type | 结果类型
     * @return flattened stream | 扁平化流
     */
    public static <T, R> Stream<R> flatMap(TreeNode<T> root, Function<TreeNode<T>, Stream<R>> mapper) {
        return stream(root).flatMap(mapper);
    }

    // ==================== Reduction | 归约 ====================

    /**
     * Reduces tree to single value
     * 将树归约为单个值
     *
     * @param root root node | 根节点
     * @param identity initial value | 初始值
     * @param accumulator accumulator | 累加器
     * @param <T> data type | 数据类型
     * @param <R> result type | 结果类型
     * @return reduced value | 归约值
     */
    public static <T, R> R reduce(TreeNode<T> root, R identity, BiFunction<R, T, R> accumulator) {
        // Iterative pre-order reduction
        R result = identity;
        Deque<TreeNode<T>> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            TreeNode<T> node = stack.pop();
            result = accumulator.apply(result, node.getData());
            List<TreeNode<T>> children = node.getChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.push(children.get(i));
            }
        }
        return result;
    }

    /**
     * Reduces tree with combiner (for parallel)
     * 带组合器的树归约（用于并行）
     *
     * @param root root node | 根节点
     * @param identity initial value | 初始值
     * @param accumulator accumulator | 累加器
     * @param combiner combiner | 组合器
     * @param <T> data type | 数据类型
     * @param <R> result type | 结果类型
     * @return reduced value | 归约值
     */
    public static <T, R> R reduce(TreeNode<T> root, R identity,
                                   BiFunction<R, T, R> accumulator,
                                   BinaryOperator<R> combiner) {
        // For combiner-based reduce, use iterative pre-order (same semantics for commutative combiners)
        R result = identity;
        Deque<TreeNode<T>> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            TreeNode<T> node = stack.pop();
            R nodeResult = accumulator.apply(identity, node.getData());
            result = combiner.apply(result, nodeResult);
            List<TreeNode<T>> children = node.getChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.push(children.get(i));
            }
        }
        return result;
    }

    /**
     * Folds tree bottom-up
     * 自底向上折叠树
     *
     * @param root root node | 根节点
     * @param leafMapper leaf node mapper | 叶节点映射器
     * @param branchFolder branch folder | 分支折叠器
     * @param <T> data type | 数据类型
     * @param <R> result type | 结果类型
     * @return folded value | 折叠值
     */
    public static <T, R> R foldBottomUp(TreeNode<T> root,
                                         Function<T, R> leafMapper,
                                         BiFunction<T, List<R>, R> branchFolder) {
        // Iterative post-order fold using two passes
        // 1. Collect post-order
        List<TreeNode<T>> postOrder = new ArrayList<>();
        Deque<TreeNode<T>> stack = new ArrayDeque<>();
        Deque<TreeNode<T>> output = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            TreeNode<T> node = stack.pop();
            output.push(node);
            for (TreeNode<T> child : node.getChildren()) {
                stack.push(child);
            }
        }
        while (!output.isEmpty()) {
            postOrder.add(output.pop());
        }
        // 2. Compute results bottom-up
        IdentityHashMap<TreeNode<T>, R> results = new IdentityHashMap<>();
        for (TreeNode<T> node : postOrder) {
            if (node.getChildren().isEmpty()) {
                results.put(node, leafMapper.apply(node.getData()));
            } else {
                List<R> childResults = new ArrayList<>();
                for (TreeNode<T> child : node.getChildren()) {
                    childResults.add(results.get(child));
                }
                results.put(node, branchFolder.apply(node.getData(), childResults));
            }
        }
        return results.get(root);
    }

    // ==================== Ancestor/Descendant Queries | 祖先/后代查询 ====================

    /**
     * Gets all descendants
     * 获取所有后代
     *
     * @param node node | 节点
     * @param <T> data type | 数据类型
     * @return list of descendants | 后代列表
     */
    public static <T> List<TreeNode<T>> getDescendants(TreeNode<T> node) {
        List<TreeNode<T>> descendants = new ArrayList<>();
        collectDescendants(node, descendants);
        return descendants;
    }

    private static <T> void collectDescendants(TreeNode<T> node, List<TreeNode<T>> list) {
        Deque<TreeNode<T>> stack = new ArrayDeque<>();
        for (TreeNode<T> child : node.getChildren()) {
            stack.push(child);
        }
        while (!stack.isEmpty()) {
            TreeNode<T> current = stack.pop();
            list.add(current);
            List<TreeNode<T>> children = current.getChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.push(children.get(i));
            }
        }
    }

    /**
     * Gets all siblings
     * 获取所有兄弟节点
     *
     * @param node node | 节点
     * @param <T> data type | 数据类型
     * @return list of siblings | 兄弟节点列表
     */
    public static <T> List<TreeNode<T>> getSiblings(TreeNode<T> node) {
        TreeNode<T> parent = node.getParent();
        if (parent == null) {
            return List.of();
        }
        List<TreeNode<T>> siblings = new ArrayList<>();
        for (TreeNode<T> child : parent.getChildren()) {
            if (child != node) {
                siblings.add(child);
            }
        }
        return siblings;
    }

    /**
     * Gets ancestors from parent to root
     * 获取从父节点到根节点的祖先
     *
     * @param node node | 节点
     * @param <T> data type | 数据类型
     * @return list of ancestors | 祖先列表
     */
    public static <T> List<TreeNode<T>> getAncestors(TreeNode<T> node) {
        List<TreeNode<T>> ancestors = new ArrayList<>();
        TreeNode<T> current = node.getParent();
        while (current != null) {
            ancestors.add(current);
            current = current.getParent();
        }
        return ancestors;
    }

    /**
     * Gets the depth of a node
     * 获取节点深度
     *
     * @param node node | 节点
     * @param <T> data type | 数据类型
     * @return depth (0 for root) | 深度（根节点为0）
     */
    public static <T> int getDepth(TreeNode<T> node) {
        int depth = 0;
        TreeNode<T> current = node.getParent();
        while (current != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }

    /**
     * Finds lowest common ancestor
     * 查找最近公共祖先
     *
     * @param node1 first node | 第一个节点
     * @param node2 second node | 第二个节点
     * @param <T> data type | 数据类型
     * @return lowest common ancestor or null | 最近公共祖先或null
     */
    public static <T> TreeNode<T> findLowestCommonAncestor(TreeNode<T> node1, TreeNode<T> node2) {
        Set<TreeNode<T>> ancestors1 = new HashSet<>(getAncestors(node1));
        ancestors1.add(node1);

        TreeNode<T> current = node2;
        while (current != null) {
            if (ancestors1.contains(current)) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }

    // ==================== Iterator Implementations | 迭代器实现 ====================

    private static class PreOrderIterator<T> implements Iterator<TreeNode<T>> {
        private final Deque<TreeNode<T>> stack = new ArrayDeque<>();

        PreOrderIterator(TreeNode<T> root) {
            if (root != null) {
                stack.push(root);
            }
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public TreeNode<T> next() {
            if (!hasNext()) throw new NoSuchElementException();
            TreeNode<T> node = stack.pop();
            List<TreeNode<T>> children = node.getChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.push(children.get(i));
            }
            return node;
        }
    }

    private static class TreeablePreOrderIterator<T extends Treeable<T, ID>, ID> implements Iterator<T> {
        private final Deque<T> stack = new ArrayDeque<>();

        TreeablePreOrderIterator(List<T> roots) {
            for (int i = roots.size() - 1; i >= 0; i--) {
                stack.push(roots.get(i));
            }
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            T node = stack.pop();
            List<T> children = node.getChildren();
            if (children != null) {
                for (int i = children.size() - 1; i >= 0; i--) {
                    stack.push(children.get(i));
                }
            }
            return node;
        }
    }

    private static class BreadthFirstIterator<T> implements Iterator<TreeNode<T>> {
        private final Deque<TreeNode<T>> queue = new ArrayDeque<>();

        BreadthFirstIterator(TreeNode<T> root) {
            if (root != null) {
                queue.offer(root);
            }
        }

        @Override
        public boolean hasNext() {
            return !queue.isEmpty();
        }

        @Override
        public TreeNode<T> next() {
            if (!hasNext()) throw new NoSuchElementException();
            TreeNode<T> node = queue.poll();
            queue.addAll(node.getChildren());
            return node;
        }
    }

    private static class PostOrderIterator<T> implements Iterator<TreeNode<T>> {
        private final List<TreeNode<T>> nodes;
        private int index = 0;

        PostOrderIterator(TreeNode<T> root) {
            nodes = new ArrayList<>();
            if (root != null) {
                collectPostOrder(root);
            }
        }

        private void collectPostOrder(TreeNode<T> root) {
            // Iterative post-order using two stacks
            Deque<TreeNode<T>> stack = new ArrayDeque<>();
            Deque<TreeNode<T>> output = new ArrayDeque<>();
            stack.push(root);
            while (!stack.isEmpty()) {
                TreeNode<T> node = stack.pop();
                output.push(node);
                for (TreeNode<T> child : node.getChildren()) {
                    stack.push(child);
                }
            }
            while (!output.isEmpty()) {
                nodes.add(output.pop());
            }
        }

        @Override
        public boolean hasNext() {
            return index < nodes.size();
        }

        @Override
        public TreeNode<T> next() {
            if (!hasNext()) throw new NoSuchElementException();
            return nodes.get(index++);
        }
    }
}
