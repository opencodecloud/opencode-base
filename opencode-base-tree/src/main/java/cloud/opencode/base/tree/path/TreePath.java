package cloud.opencode.base.tree.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tree Path
 * 树路径
 *
 * <p>Represents a path from root to a node.</p>
 * <p>表示从根到节点的路径。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable path representation - 不可变路径表示</li>
 *   <li>Root, target, and parent access - 根、目标和父节点访问</li>
 *   <li>Sub-path and append operations - 子路径和追加操作</li>
 *   <li>String representation with separator - 带分隔符的字符串表示</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TreePath<MyNode> path = PathFinder.findPathById(roots, id).get();
 * MyNode root = path.getRoot();
 * MyNode target = path.getTarget();
 * int depth = path.length();
 * String display = path.toString(" / ");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 是（不可变记录）</li>
 *   <li>Null-safe: Yes (null nodes default to empty) - 是（null节点默认为空）</li>
 * </ul>
 * @param <T> the node type | 节点类型
 * @param nodes the path nodes from root to target | 从根到目标的路径节点
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public record TreePath<T>(List<T> nodes) {

    /**
     * Compact constructor
     * 紧凑构造函数
     */
    public TreePath {
        nodes = nodes != null ? List.copyOf(nodes) : List.of();
    }

    /**
     * Create empty path
     * 创建空路径
     *
     * @param <T> the node type | 节点类型
     * @return the empty path | 空路径
     */
    public static <T> TreePath<T> empty() {
        return new TreePath<>(List.of());
    }

    /**
     * Create path from nodes
     * 从节点创建路径
     *
     * @param nodes the nodes | 节点
     * @param <T> the node type | 节点类型
     * @return the path | 路径
     */
    @SafeVarargs
    public static <T> TreePath<T> of(T... nodes) {
        return new TreePath<>(List.of(nodes));
    }

    /**
     * Create path from list
     * 从列表创建路径
     *
     * @param nodes the nodes | 节点
     * @param <T> the node type | 节点类型
     * @return the path | 路径
     */
    public static <T> TreePath<T> of(List<T> nodes) {
        return new TreePath<>(nodes);
    }

    /**
     * Check if path is empty
     * 检查路径是否为空
     *
     * @return true if empty | 如果为空返回true
     */
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    /**
     * Get path length
     * 获取路径长度
     *
     * @return the length | 长度
     */
    public int length() {
        return nodes.size();
    }

    /**
     * Get root node
     * 获取根节点
     *
     * @return the root or null | 根节点或null
     */
    public T getRoot() {
        return nodes.isEmpty() ? null : nodes.getFirst();
    }

    /**
     * Get target node (last node)
     * 获取目标节点（最后一个节点）
     *
     * @return the target or null | 目标节点或null
     */
    public T getTarget() {
        return nodes.isEmpty() ? null : nodes.getLast();
    }

    /**
     * Get node at index
     * 获取指定索引的节点
     *
     * @param index the index | 索引
     * @return the node | 节点
     */
    public T get(int index) {
        return nodes.get(index);
    }

    /**
     * Get parent of target
     * 获取目标的父节点
     *
     * @return the parent or null | 父节点或null
     */
    public T getParent() {
        return nodes.size() > 1 ? nodes.get(nodes.size() - 2) : null;
    }

    /**
     * Get sub-path from start to end index
     * 获取从开始到结束索引的子路径
     *
     * @param start the start index | 开始索引
     * @param end the end index (exclusive) | 结束索引（不包含）
     * @return the sub-path | 子路径
     */
    public TreePath<T> subPath(int start, int end) {
        return new TreePath<>(nodes.subList(start, end));
    }

    /**
     * Append node to path
     * 向路径追加节点
     *
     * @param node the node to append | 要追加的节点
     * @return the new path | 新路径
     */
    public TreePath<T> append(T node) {
        List<T> newNodes = new ArrayList<>(nodes);
        newNodes.add(node);
        return new TreePath<>(newNodes);
    }

    /**
     * Get reversed path
     * 获取反转的路径
     *
     * @return the reversed path | 反转的路径
     */
    public TreePath<T> reverse() {
        List<T> reversed = new ArrayList<>(nodes);
        Collections.reverse(reversed);
        return new TreePath<>(reversed);
    }

    /**
     * Check if path contains node
     * 检查路径是否包含节点
     *
     * @param node the node to check | 要检查的节点
     * @return true if contains | 如果包含返回true
     */
    public boolean contains(T node) {
        return nodes.contains(node);
    }

    /**
     * Convert to string representation
     * 转换为字符串表示
     *
     * @param separator the separator | 分隔符
     * @return the string | 字符串
     */
    public String toString(String separator) {
        return nodes.stream()
            .map(Object::toString)
            .collect(Collectors.joining(separator));
    }

    @Override
    public String toString() {
        return toString(" -> ");
    }
}
