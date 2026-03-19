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
        int maxHeight = 0;
        for (TreeNode<T> child : children) {
            maxHeight = Math.max(maxHeight, child.getHeight());
        }
        return maxHeight + 1;
    }

    public TreeNode<T> getRoot() {
        TreeNode<T> current = this;
        while (current.parent != null) {
            current = current.parent;
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
        for (TreeNode<T> child : node.children) {
            list.add(child);
            collectDescendants(child, list);
        }
    }

    public List<TreeNode<T>> getLeaves() {
        List<TreeNode<T>> leaves = new ArrayList<>();
        collectLeaves(this, leaves);
        return leaves;
    }

    private void collectLeaves(TreeNode<T> node, List<TreeNode<T>> list) {
        if (node.isLeaf()) {
            list.add(node);
        } else {
            for (TreeNode<T> child : node.children) {
                collectLeaves(child, list);
            }
        }
    }

    // === Search methods ===

    public Optional<TreeNode<T>> find(Predicate<T> predicate) {
        if (predicate.test(data)) return Optional.of(this);
        for (TreeNode<T> child : children) {
            Optional<TreeNode<T>> found = child.find(predicate);
            if (found.isPresent()) return found;
        }
        return Optional.empty();
    }

    public List<TreeNode<T>> findAll(Predicate<T> predicate) {
        List<TreeNode<T>> results = new ArrayList<>();
        findAll(predicate, results);
        return results;
    }

    private void findAll(Predicate<T> predicate, List<TreeNode<T>> results) {
        if (predicate.test(data)) results.add(this);
        for (TreeNode<T> child : children) {
            child.findAll(predicate, results);
        }
    }

    // === Traversal methods ===

    public void forEachPreOrder(Consumer<TreeNode<T>> action) {
        action.accept(this);
        for (TreeNode<T> child : children) {
            child.forEachPreOrder(action);
        }
    }

    public void forEachPostOrder(Consumer<TreeNode<T>> action) {
        for (TreeNode<T> child : children) {
            child.forEachPostOrder(action);
        }
        action.accept(this);
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
        TreeNode<R> mapped = new TreeNode<>(mapper.apply(data));
        for (TreeNode<T> child : children) {
            mapped.addChild(child.map(mapper));
        }
        return mapped;
    }

    public TreeNode<T> filter(Predicate<T> predicate) {
        if (!predicate.test(data)) return null;
        TreeNode<T> filtered = new TreeNode<>(data);
        for (TreeNode<T> child : children) {
            TreeNode<T> filteredChild = child.filter(predicate);
            if (filteredChild != null) {
                filtered.addChild(filteredChild);
            }
        }
        return filtered;
    }

    public int size() {
        int count = 1;
        for (TreeNode<T> child : children) {
            count += child.size();
        }
        return count;
    }

    @Override
    public String toString() {
        return String.format("TreeNode[%s, children=%d]", data, children.size());
    }
}
