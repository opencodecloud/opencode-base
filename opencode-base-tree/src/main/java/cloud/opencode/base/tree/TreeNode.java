package cloud.opencode.base.tree;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Tree Node
 * 树节点
 *
 * <p>A generic tree node implementation.</p>
 * <p>通用树节点实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Parent-child relationships with back-references - 带反向引用的父子关系</li>
 *   <li>Pre-order, post-order, breadth-first traversal - 前序、后序、广度优先遍历</li>
 *   <li>Search, filter, and map operations - 搜索、过滤和映射操作</li>
 *   <li>Ancestor, descendant, and sibling queries - 祖先、后代和兄弟查询</li>
 *   <li>Depth, height, and size calculations - 深度、高度和大小计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create tree - 创建树
 * TreeNode<String> root = new TreeNode<>("root");
 * TreeNode<String> child = root.addChild("child");
 *
 * // Search - 搜索
 * Optional<TreeNode<String>> found = root.find(s -> s.equals("child"));
 *
 * // Traverse - 遍历
 * root.forEachPreOrder(node -> System.out.println(node.getData()));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: No (null data is allowed but not checked) - 空值安全: 否（允许null数据但不做检查）</li>
 * </ul>
 *
 * @param <T> the data type | 数据类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public class TreeNode<T> {

    private T data;
    private TreeNode<T> parent;
    private final List<TreeNode<T>> children;

    public TreeNode(T data) {
        this.data = data;
        this.children = new ArrayList<>();
    }

    // === Basic operations ===

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public TreeNode<T> getParent() { return parent; }
    public List<TreeNode<T>> getChildren() { return Collections.unmodifiableList(children); }

    public TreeNode<T> addChild(T data) {
        TreeNode<T> child = new TreeNode<>(data);
        child.parent = this;
        children.add(child);
        return child;
    }

    public TreeNode<T> addChild(TreeNode<T> child) {
        child.parent = this;
        children.add(child);
        return child;
    }

    public boolean removeChild(TreeNode<T> child) {
        if (children.remove(child)) {
            child.parent = null;
            return true;
        }
        return false;
    }

    public void clearChildren() {
        for (TreeNode<T> child : children) {
            child.parent = null;
        }
        children.clear();
    }

    // === Query methods ===

    public boolean isRoot() { return parent == null; }
    public boolean isLeaf() { return children.isEmpty(); }
    public int getChildCount() { return children.size(); }
    public boolean hasChildren() { return !children.isEmpty(); }

    public int getDepth() {
        int depth = 0;
        TreeNode<T> current = this;
        while (current.parent != null) {
            depth++;
            current = current.parent;
        }
        return depth;
    }

    public int getHeight() {
        if (isLeaf()) return 0;
        int maxDepth = 0;
        Deque<TreeNode<T>> nodeStack = new ArrayDeque<>();
        Deque<Integer> depthStack = new ArrayDeque<>();
        nodeStack.push(this);
        depthStack.push(0);
        while (!nodeStack.isEmpty()) {
            TreeNode<T> node = nodeStack.pop();
            int depth = depthStack.pop();
            if (node.isLeaf()) {
                maxDepth = Math.max(maxDepth, depth);
            } else {
                for (TreeNode<T> child : node.children) {
                    nodeStack.push(child);
                    depthStack.push(depth + 1);
                }
            }
        }
        return maxDepth;
    }

    public TreeNode<T> getRoot() {
        Set<TreeNode<T>> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        TreeNode<T> current = this;
        visited.add(current);
        while (current.parent != null) {
            current = current.parent;
            if (!visited.add(current)) {
                // Cycle detected — return the last non-cyclic node
                break;
            }
        }
        return current;
    }

    public List<TreeNode<T>> getSiblings() {
        if (parent == null) return List.of();
        return parent.children.stream()
            .filter(c -> c != this)
            .toList();
    }

    public List<TreeNode<T>> getAncestors() {
        List<TreeNode<T>> ancestors = new ArrayList<>();
        TreeNode<T> current = parent;
        while (current != null) {
            ancestors.add(current);
            current = current.parent;
        }
        return ancestors;
    }

    public List<TreeNode<T>> getDescendants() {
        List<TreeNode<T>> descendants = new ArrayList<>();
        collectDescendants(this, descendants);
        return descendants;
    }

    private void collectDescendants(TreeNode<T> node, List<TreeNode<T>> list) {
        Deque<TreeNode<T>> stack = new ArrayDeque<>();
        for (int i = node.children.size() - 1; i >= 0; i--) {
            stack.push(node.children.get(i));
        }
        while (!stack.isEmpty()) {
            TreeNode<T> current = stack.pop();
            list.add(current);
            for (int i = current.children.size() - 1; i >= 0; i--) {
                stack.push(current.children.get(i));
            }
        }
    }

    public List<TreeNode<T>> getLeaves() {
        List<TreeNode<T>> leaves = new ArrayList<>();
        collectLeaves(this, leaves);
        return leaves;
    }

    private void collectLeaves(TreeNode<T> node, List<TreeNode<T>> list) {
        Deque<TreeNode<T>> stack = new ArrayDeque<>();
        stack.push(node);
        while (!stack.isEmpty()) {
            TreeNode<T> current = stack.pop();
            if (current.isLeaf()) {
                list.add(current);
            } else {
                for (int i = current.children.size() - 1; i >= 0; i--) {
                    stack.push(current.children.get(i));
                }
            }
        }
    }

    // === Search methods ===

    public Optional<TreeNode<T>> find(Predicate<T> predicate) {
        Deque<TreeNode<T>> stack = new ArrayDeque<>();
        stack.push(this);
        while (!stack.isEmpty()) {
            TreeNode<T> node = stack.pop();
            if (predicate.test(node.data)) return Optional.of(node);
            for (int i = node.children.size() - 1; i >= 0; i--) {
                stack.push(node.children.get(i));
            }
        }
        return Optional.empty();
    }

    public List<TreeNode<T>> findAll(Predicate<T> predicate) {
        List<TreeNode<T>> results = new ArrayList<>();
        findAll(predicate, results);
        return results;
    }

    private void findAll(Predicate<T> predicate, List<TreeNode<T>> results) {
        Deque<TreeNode<T>> stack = new ArrayDeque<>();
        stack.push(this);
        while (!stack.isEmpty()) {
            TreeNode<T> node = stack.pop();
            if (predicate.test(node.data)) results.add(node);
            for (int i = node.children.size() - 1; i >= 0; i--) {
                stack.push(node.children.get(i));
            }
        }
    }

    // === Traversal methods ===

    public void forEachPreOrder(Consumer<TreeNode<T>> action) {
        Deque<TreeNode<T>> stack = new ArrayDeque<>();
        stack.push(this);
        while (!stack.isEmpty()) {
            TreeNode<T> node = stack.pop();
            action.accept(node);
            for (int i = node.children.size() - 1; i >= 0; i--) {
                stack.push(node.children.get(i));
            }
        }
    }

    public void forEachPostOrder(Consumer<TreeNode<T>> action) {
        Deque<TreeNode<T>> stack = new ArrayDeque<>();
        Deque<TreeNode<T>> output = new ArrayDeque<>();
        stack.push(this);
        while (!stack.isEmpty()) {
            TreeNode<T> node = stack.pop();
            output.push(node);
            for (TreeNode<T> child : node.children) {
                stack.push(child);
            }
        }
        while (!output.isEmpty()) {
            action.accept(output.pop());
        }
    }

    public void forEachBreadthFirst(Consumer<TreeNode<T>> action) {
        Deque<TreeNode<T>> queue = new ArrayDeque<>();
        queue.add(this);
        while (!queue.isEmpty()) {
            TreeNode<T> node = queue.poll();
            action.accept(node);
            queue.addAll(node.children);
        }
    }

    // === Transform methods ===

    public <R> TreeNode<R> map(Function<T, R> mapper) {
        // Iterative BFS to preserve child order
        TreeNode<R> mappedRoot = new TreeNode<>(mapper.apply(data));
        Deque<TreeNode<T>> sourceQueue = new ArrayDeque<>();
        Deque<TreeNode<R>> targetQueue = new ArrayDeque<>();
        sourceQueue.offer(this);
        targetQueue.offer(mappedRoot);
        while (!sourceQueue.isEmpty()) {
            TreeNode<T> sourceNode = sourceQueue.poll();
            TreeNode<R> targetNode = targetQueue.poll();
            for (TreeNode<T> sourceChild : sourceNode.children) {
                TreeNode<R> mappedChild = targetNode.addChild(mapper.apply(sourceChild.data));
                sourceQueue.offer(sourceChild);
                targetQueue.offer(mappedChild);
            }
        }
        return mappedRoot;
    }

    public TreeNode<T> filter(Predicate<T> predicate) {
        if (!predicate.test(data)) return null;
        // Iterative BFS to preserve child order
        TreeNode<T> filteredRoot = new TreeNode<>(data);
        Deque<TreeNode<T>> sourceQueue = new ArrayDeque<>();
        Deque<TreeNode<T>> targetQueue = new ArrayDeque<>();
        sourceQueue.offer(this);
        targetQueue.offer(filteredRoot);
        while (!sourceQueue.isEmpty()) {
            TreeNode<T> sourceNode = sourceQueue.poll();
            TreeNode<T> targetNode = targetQueue.poll();
            for (TreeNode<T> sourceChild : sourceNode.children) {
                if (predicate.test(sourceChild.data)) {
                    TreeNode<T> filteredChild = targetNode.addChild(sourceChild.data);
                    sourceQueue.offer(sourceChild);
                    targetQueue.offer(filteredChild);
                }
            }
        }
        return filteredRoot;
    }

    public int size() {
        int count = 0;
        Deque<TreeNode<T>> queue = new ArrayDeque<>();
        queue.add(this);
        while (!queue.isEmpty()) {
            TreeNode<T> node = queue.poll();
            count++;
            queue.addAll(node.children);
        }
        return count;
    }

    @Override
    public String toString() {
        return String.format("TreeNode[%s, children=%d]", data, children.size());
    }
}
