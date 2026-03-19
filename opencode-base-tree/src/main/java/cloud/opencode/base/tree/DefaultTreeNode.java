package cloud.opencode.base.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default Tree Node
 * 默认树节点
 *
 * <p>Default implementation of Treeable interface.</p>
 * <p>Treeable接口的默认实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ID-based parent-child relationships - 基于ID的父子关系</li>
 *   <li>Extra attributes via Map - 通过Map存储额外属性</li>
 *   <li>Sortable with sort field - 可通过sort字段排序</li>
 *   <li>Root/leaf detection - 根节点/叶子节点检测</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create node - 创建节点
 * DefaultTreeNode<Long> node = new DefaultTreeNode<>(1L, 0L, "Root");
 * node.put("key", "value");
 *
 * // Add child - 添加子节点
 * node.addChild(new DefaultTreeNode<>(2L, 1L, "Child"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Partial (setExtra and setChildren handle null) - 空值安全: 部分（setExtra和setChildren处理null）</li>
 * </ul>
 *
 * @param <ID> the ID type | ID类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public class DefaultTreeNode<ID> implements Treeable<DefaultTreeNode<ID>, ID> {

    private ID id;
    private ID parentId;
    private String name;
    private int sort;
    private Map<String, Object> extra;
    private List<DefaultTreeNode<ID>> children;

    /**
     * Create empty node
     * 创建空节点
     */
    public DefaultTreeNode() {
        this.extra = new HashMap<>();
        this.children = new ArrayList<>();
    }

    /**
     * Create node with ID
     * 使用ID创建节点
     *
     * @param id the ID | ID
     */
    public DefaultTreeNode(ID id) {
        this();
        this.id = id;
    }

    /**
     * Create node with ID, parentId and name
     * 使用ID、父ID和名称创建节点
     *
     * @param id the ID | ID
     * @param parentId the parent ID | 父ID
     * @param name the name | 名称
     */
    public DefaultTreeNode(ID id, ID parentId, String name) {
        this();
        this.id = id;
        this.parentId = parentId;
        this.name = name;
    }

    @Override
    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    @Override
    public ID getParentId() {
        return parentId;
    }

    public void setParentId(ID parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra != null ? extra : new HashMap<>();
    }

    @Override
    public List<DefaultTreeNode<ID>> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<DefaultTreeNode<ID>> children) {
        this.children = children != null ? children : new ArrayList<>();
    }

    /**
     * Put extra attribute
     * 设置额外属性
     *
     * @param key the key | 键
     * @param value the value | 值
     * @return this node | 此节点
     */
    public DefaultTreeNode<ID> put(String key, Object value) {
        extra.put(key, value);
        return this;
    }

    /**
     * Get extra attribute
     * 获取额外属性
     *
     * @param key the key | 键
     * @param <V> the value type | 值类型
     * @return the value | 值
     */
    @SuppressWarnings("unchecked")
    public <V> V get(String key) {
        return (V) extra.get(key);
    }

    /**
     * Get extra attribute with default
     * 获取额外属性（带默认值）
     *
     * @param key the key | 键
     * @param defaultValue the default value | 默认值
     * @param <V> the value type | 值类型
     * @return the value or default | 值或默认值
     */
    @SuppressWarnings("unchecked")
    public <V> V get(String key, V defaultValue) {
        Object value = extra.get(key);
        return value != null ? (V) value : defaultValue;
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
     * Add child node
     * 添加子节点
     *
     * @param child the child node | 子节点
     * @return this node | 此节点
     */
    public DefaultTreeNode<ID> addChild(DefaultTreeNode<ID> child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
        return this;
    }

    @Override
    public String toString() {
        return "DefaultTreeNode{" +
            "id=" + id +
            ", parentId=" + parentId +
            ", name='" + name + '\'' +
            ", childCount=" + (children != null ? children.size() : 0) +
            '}';
    }
}
