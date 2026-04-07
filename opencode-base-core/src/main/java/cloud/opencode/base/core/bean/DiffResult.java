package cloud.opencode.base.core.bean;

import java.util.List;
import java.util.Objects;

/**
 * Diff Result Record - Contains the complete comparison result between two objects
 * 差异结果记录 - 包含两个对象之间的完整比较结果
 *
 * <p>Provides convenience methods to filter diffs by change type.</p>
 * <p>提供按变更类型过滤差异的便捷方法。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DiffResult<User> result = ObjectDiff.compare(oldUser, newUser);
 * boolean changed = result.hasDiffs();
 * List<Diff<?>> modified = result.getModified();
 * List<Diff<?>> added = result.getAdded();
 * List<Diff<?>> removed = result.getRemoved();
 * }</pre>
 *
 * @param type  the class of the compared objects | 比较对象的类
 * @param diffs the list of property diffs | 属性差异列表
 * @param <T>   the object type | 对象类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.3
 */
public record DiffResult<T>(
        Class<T> type,
        List<Diff<?>> diffs
) {

    /**
     * Creates a DiffResult with validation and defensive copy
     * 创建 DiffResult（带验证和防御性拷贝）
     *
     * @param type  the class of the compared objects | 比较对象的类
     * @param diffs the list of property diffs | 属性差异列表
     */
    public DiffResult {
        Objects.requireNonNull(type, "type must not be null");
        diffs = List.copyOf(diffs);
    }

    /**
     * Returns true if there are any non-UNCHANGED diffs
     * 是否存在非 UNCHANGED 的差异
     *
     * @return true if any property changed | 如有属性变更返回 true
     */
    public boolean hasDiffs() {
        return diffs.stream().anyMatch(d -> d.changeType() != ChangeType.UNCHANGED);
    }

    /**
     * Returns only MODIFIED diffs
     * 返回仅 MODIFIED 类型的差异
     *
     * @return the list of modified diffs | MODIFIED 差异列表
     */
    public List<Diff<?>> getModified() {
        return diffs.stream()
                .filter(d -> d.changeType() == ChangeType.MODIFIED)
                .toList();
    }

    /**
     * Returns only ADDED diffs
     * 返回仅 ADDED 类型的差异
     *
     * @return the list of added diffs | ADDED 差异列表
     */
    public List<Diff<?>> getAdded() {
        return diffs.stream()
                .filter(d -> d.changeType() == ChangeType.ADDED)
                .toList();
    }

    /**
     * Returns only REMOVED diffs
     * 返回仅 REMOVED 类型的差异
     *
     * @return the list of removed diffs | REMOVED 差异列表
     */
    public List<Diff<?>> getRemoved() {
        return diffs.stream()
                .filter(d -> d.changeType() == ChangeType.REMOVED)
                .toList();
    }
}
