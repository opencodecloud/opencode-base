package cloud.opencode.base.collections;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Set;

/**
 * SetView - Abstract Set View for Lazy Set Operations
 * SetView - 惰性集合运算的抽象集合视图
 *
 * <p>Provides a read-only view of set operations without creating new collections.
 * The actual computation is deferred until iteration.</p>
 * <p>提供集合运算结果的只读视图，不创建新集合。实际计算延迟到迭代时。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lazy computation - 惰性计算</li>
 *   <li>Read-only view - 只读视图</li>
 *   <li>Copy to mutable/immutable collections - 复制到可变/不可变集合</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Union view - 并集视图
 * SetView<String> union = SetUtil.union(set1, set2);
 *
 * // Copy to new HashSet - 复制到新 HashSet
 * Set<String> copy = union.copyInto(new HashSet<>());
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Creation: O(1) - 创建: O(1)</li>
 *   <li>Iteration: O(n) - 迭代: O(n)</li>
 *   <li>contains(): varies by operation - contains(): 因操作而异</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (depends on underlying sets) - 线程安全: 否（取决于底层集合）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @param <E> element type | 元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public abstract class SetView<E> extends AbstractSet<E> {

    /**
     * Package-private constructor to prevent external instantiation
     * 包内私有构造函数，防止外部实例化
     */
    SetView() {
    }

    /**
     * Copy this view into the specified set
     * 复制到指定集合
     *
     * @param <S> set type | 集合类型
     * @param set the target set | 目标集合
     * @return the set with elements added | 添加元素后的集合
     */
    public <S extends Set<E>> S copyInto(S set) {
        set.addAll(this);
        return set;
    }

    /**
     * Copy this view to a new HashSet
     * 复制到新的 HashSet
     *
     * @return new HashSet containing all elements | 包含所有元素的新 HashSet
     */
    public Set<E> toSet() {
        return copyInto(new HashSet<>());
    }

    /**
     * This operation is not supported on SetView
     * SetView 不支持添加操作
     *
     * @param e element to add | 要添加的元素
     * @return never returns | 永不返回
     * @throws UnsupportedOperationException always | 总是抛出
     */
    @Override
    public final boolean add(E e) {
        throw new UnsupportedOperationException("SetView is read-only");
    }

    /**
     * This operation is not supported on SetView
     * SetView 不支持移除操作
     *
     * @param o element to remove | 要移除的元素
     * @return never returns | 永不返回
     * @throws UnsupportedOperationException always | 总是抛出
     */
    @Override
    public final boolean remove(Object o) {
        throw new UnsupportedOperationException("SetView is read-only");
    }

    /**
     * This operation is not supported on SetView
     * SetView 不支持清空操作
     *
     * @throws UnsupportedOperationException always | 总是抛出
     */
    @Override
    public final void clear() {
        throw new UnsupportedOperationException("SetView is read-only");
    }
}
