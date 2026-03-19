package cloud.opencode.base.tree.diff;

import java.util.List;

/**
 * Tree Diff Result
 * 树差异结果
 *
 * <p>Result of comparing two trees.</p>
 * <p>比较两棵树的结果。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable diff result record - 不可变差异结果记录</li>
 *   <li>Change count statistics - 变化数量统计</li>
 *   <li>Summary string generation - 摘要字符串生成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TreeDiffResult<MyNode> result = TreeDiff.diff(oldRoots, newRoots);
 * boolean equal = result.isEqual();
 * int totalChanges = result.getTotalChanges();
 * String summary = result.getSummary();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 是（不可变记录）</li>
 *   <li>Null-safe: Yes (null lists default to empty) - 是（null列表默认为空）</li>
 * </ul>
 * @param <T> the node type | 节点类型
 * @param added the added nodes | 新增的节点
 * @param removed the removed nodes | 删除的节点
 * @param modified the modified nodes | 修改的节点
 * @param unchanged the unchanged nodes | 未变化的节点
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
public record TreeDiffResult<T>(
    List<T> added,
    List<T> removed,
    List<ModifiedNode<T>> modified,
    List<T> unchanged
) {

    /**
     * Compact constructor
     * 紧凑构造函数
     */
    public TreeDiffResult {
        added = added != null ? List.copyOf(added) : List.of();
        removed = removed != null ? List.copyOf(removed) : List.of();
        modified = modified != null ? List.copyOf(modified) : List.of();
        unchanged = unchanged != null ? List.copyOf(unchanged) : List.of();
    }

    /**
     * Modified Node
     * 修改的节点
     *
     * @param <T> the node type | 节点类型
     * @param oldNode the old node | 旧节点
     * @param newNode the new node | 新节点
     */
    public record ModifiedNode<T>(T oldNode, T newNode) {}

    /**
     * Create empty result
     * 创建空结果
     *
     * @param <T> the node type | 节点类型
     * @return the empty result | 空结果
     */
    public static <T> TreeDiffResult<T> empty() {
        return new TreeDiffResult<>(List.of(), List.of(), List.of(), List.of());
    }

    /**
     * Check if trees are equal
     * 检查两棵树是否相等
     *
     * @return true if equal | 如果相等返回true
     */
    public boolean isEqual() {
        return added.isEmpty() && removed.isEmpty() && modified.isEmpty();
    }

    /**
     * Check if has changes
     * 检查是否有变化
     *
     * @return true if has changes | 如果有变化返回true
     */
    public boolean hasChanges() {
        return !isEqual();
    }

    /**
     * Get total change count
     * 获取总变化数量
     *
     * @return the total changes | 总变化数量
     */
    public int getTotalChanges() {
        return added.size() + removed.size() + modified.size();
    }

    /**
     * Get added count
     * 获取新增数量
     *
     * @return the added count | 新增数量
     */
    public int getAddedCount() {
        return added.size();
    }

    /**
     * Get removed count
     * 获取删除数量
     *
     * @return the removed count | 删除数量
     */
    public int getRemovedCount() {
        return removed.size();
    }

    /**
     * Get modified count
     * 获取修改数量
     *
     * @return the modified count | 修改数量
     */
    public int getModifiedCount() {
        return modified.size();
    }

    /**
     * Get summary string
     * 获取摘要字符串
     *
     * @return the summary | 摘要
     */
    public String getSummary() {
        return String.format("TreeDiff: +%d, -%d, ~%d, =%d",
            added.size(), removed.size(), modified.size(), unchanged.size());
    }
}
