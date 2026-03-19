package cloud.opencode.base.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Light Tree Node
 * 轻量级树节点
 *
 * <p>A lightweight tree node implementation using record.</p>
 * <p>使用record实现的轻量级树节点。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable record-based tree node - 基于record的不可变树节点</li>
 *   <li>Minimal memory footprint - 最小内存占用</li>
 *   <li>Factory methods for root and child nodes - 根节点和子节点的工厂方法</li>
 *   <li>Functional child addition via withChild - 通过withChild函数式添加子节点</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create root - 创建根节点
 * LightTreeNode<Long> root = LightTreeNode.root(1L, "Root");
 *
 * // Create child - 创建子节点
 * LightTreeNode<Long> child = LightTreeNode.of(2L, 1L, "Child");
 *
 * // Add child (returns new node) - 添加子节点（返回新节点）
 * LightTreeNode<Long> updated = root.withChild(child);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record, withChild returns new instance) - 线程安全: 是（不可变record，withChild返回新实例）</li>
 *   <li>Null-safe: Partial (children list defaults to empty if null) - 空值安全: 部分（子节点列表null时默认为空）</li>
 * </ul>
 *
 * @param <ID> the ID type | ID类型
 * @param id the node ID | 节点ID
 * @param parentId the parent ID | 父节点ID
 * @param name the node name | 节点名称
 * @param children the children list | 子节点列表
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public record LightTreeNode<ID>(
    ID id,
    ID parentId,
    String name,
    List<LightTreeNode<ID>> children
) {

    /**
     * Compact constructor
     * 紧凑构造函数
     */
    public LightTreeNode {
        children = children != null ? new ArrayList<>(children) : new ArrayList<>();
    }

    /**
     * Create node
     * 创建节点
     *
     * @param id the ID | ID
     * @param parentId the parent ID | 父节点ID
     * @param name the name | 名称
     * @param <ID> the ID type | ID类型
     * @return the node | 节点
     */
    public static <ID> LightTreeNode<ID> of(ID id, ID parentId, String name) {
        return new LightTreeNode<>(id, parentId, name, new ArrayList<>());
    }

    /**
     * Create root node
     * 创建根节点
     *
     * @param id the ID | ID
     * @param name the name | 名称
     * @param <ID> the ID type | ID类型
     * @return the node | 节点
     */
    public static <ID> LightTreeNode<ID> root(ID id, String name) {
        return new LightTreeNode<>(id, null, name, new ArrayList<>());
    }

    /**
     * Check if node is root
     * 检查是否为根节点
     *
     * @return true if root | 如果是根节点返回true
     */
    public boolean isRoot() {
        return parentId == null;
    }

    /**
     * Check if node is leaf
     * 检查是否为叶子节点
     *
     * @return true if leaf | 如果是叶子节点返回true
     */
    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    /**
     * Add child and return new node
     * 添加子节点并返回新节点
     *
     * @param child the child to add | 要添加的子节点
     * @return new node with child | 带子节点的新节点
     */
    public LightTreeNode<ID> withChild(LightTreeNode<ID> child) {
        List<LightTreeNode<ID>> newChildren = new ArrayList<>(children);
        newChildren.add(child);
        return new LightTreeNode<>(id, parentId, name, newChildren);
    }
}
