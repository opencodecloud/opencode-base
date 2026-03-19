package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Range;

import java.util.Set;

/**
 * RangeSet - Range Set Interface
 * RangeSet - 范围集合接口
 *
 * <p>A set of ranges that automatically coalesces overlapping and adjacent ranges.</p>
 * <p>自动合并重叠和相邻范围的范围集合。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Automatic range coalescing - 自动范围合并</li>
 *   <li>Efficient range queries - 高效范围查询</li>
 *   <li>Complement and intersection operations - 补集和交集操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RangeSet<Integer> rangeSet = TreeRangeSet.create();
 * rangeSet.add(Range.closed(1, 10));   // {[1, 10]}
 * rangeSet.add(Range.closed(5, 15));   // {[1, 15]} (coalesced)
 * rangeSet.add(Range.closed(20, 30));  // {[1, 15], [20, 30]}
 *
 * boolean contains = rangeSet.contains(5);  // true
 * Range<Integer> enclosing = rangeSet.rangeContaining(5);  // [1, 15]
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (interface, implementation-dependent) - 否（接口，取决于实现）</li>
 *   <li>Null-safe: No (range must not be null) - 否（范围不能为null）</li>
 * </ul>
 * @param <C> the type of range endpoints | 范围端点类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public interface RangeSet<C extends Comparable<? super C>> {

    // ==================== 查询方法 | Query Methods ====================

    /**
     * Check if the value is contained in any range.
     * 检查值是否包含在任何范围中。
     *
     * @param value the value | 值
     * @return true if contained | 如果包含则返回 true
     */
    boolean contains(C value);

    /**
     * Return the range containing the value, or null.
     * 返回包含该值的范围，如果不存在则返回 null。
     *
     * @param value the value | 值
     * @return the containing range, or null | 包含的范围，或 null
     */
    Range<C> rangeContaining(C value);

    /**
     * Check if this range set encloses the given range.
     * 检查此范围集合是否完全包含给定范围。
     *
     * @param otherRange the range to check | 要检查的范围
     * @return true if enclosed | 如果完全包含则返回 true
     */
    boolean encloses(Range<C> otherRange);

    /**
     * Check if this range set encloses all ranges in the other set.
     * 检查此范围集合是否完全包含另一个集合中的所有范围。
     *
     * @param other the other range set | 另一个范围集合
     * @return true if all enclosed | 如果全部包含则返回 true
     */
    boolean enclosesAll(RangeSet<C> other);

    /**
     * Check if this range set intersects the given range.
     * 检查此范围集合是否与给定范围相交。
     *
     * @param otherRange the range to check | 要检查的范围
     * @return true if intersects | 如果相交则返回 true
     */
    boolean intersects(Range<C> otherRange);

    /**
     * Check if this range set is empty.
     * 检查此范围集合是否为空。
     *
     * @return true if empty | 如果为空则返回 true
     */
    boolean isEmpty();

    // ==================== 视图方法 | View Methods ====================

    /**
     * Return all ranges as a set.
     * 以集合形式返回所有范围。
     *
     * @return the set of ranges | 范围集合
     */
    Set<Range<C>> asRanges();

    /**
     * Return all ranges as a descending set.
     * 以降序集合形式返回所有范围。
     *
     * @return the descending set of ranges | 降序范围集合
     */
    Set<Range<C>> asDescendingSetOfRanges();

    /**
     * Return the complement of this range set.
     * 返回此范围集合的补集。
     *
     * @return the complement | 补集
     */
    RangeSet<C> complement();

    /**
     * Return a sub range set within the given range.
     * 返回给定范围内的子范围集合。
     *
     * @param view the viewing range | 视图范围
     * @return the sub range set | 子范围集合
     */
    RangeSet<C> subRangeSet(Range<C> view);

    /**
     * Return the minimal range that encloses all ranges.
     * 返回包含所有范围的最小范围。
     *
     * @return the span | 跨度
     */
    Range<C> span();

    // ==================== 修改方法 | Modification Methods ====================

    /**
     * Add a range to this set.
     * 向此集合添加范围。
     *
     * @param range the range to add | 要添加的范围
     */
    void add(Range<C> range);

    /**
     * Remove a range from this set.
     * 从此集合移除范围。
     *
     * @param range the range to remove | 要移除的范围
     */
    void remove(Range<C> range);

    /**
     * Add all ranges from another range set.
     * 从另一个范围集合添加所有范围。
     *
     * @param other the other range set | 另一个范围集合
     */
    void addAll(RangeSet<C> other);

    /**
     * Remove all ranges in another range set.
     * 移除另一个范围集合中的所有范围。
     *
     * @param other the other range set | 另一个范围集合
     */
    void removeAll(RangeSet<C> other);

    /**
     * Clear all ranges.
     * 清除所有范围。
     */
    void clear();
}
