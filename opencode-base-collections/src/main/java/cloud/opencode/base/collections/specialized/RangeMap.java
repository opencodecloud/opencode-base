package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Range;

import java.util.Map;

/**
 * RangeMap - Range Map Interface
 * RangeMap - 范围映射接口
 *
 * <p>A mapping from disjoint non-empty ranges to values.</p>
 * <p>从不相交的非空范围到值的映射。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Automatic range coalescing - 自动范围合并</li>
 *   <li>Efficient range-based lookup - 高效的基于范围的查找</li>
 *   <li>Range-value association - 范围-值关联</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RangeMap<Integer, String> rangeMap = TreeRangeMap.create();
 * rangeMap.put(Range.closed(1, 10), "small");
 * rangeMap.put(Range.closed(11, 100), "medium");
 * rangeMap.put(Range.closed(101, 1000), "large");
 *
 * String value = rangeMap.get(5);   // "small"
 * String value2 = rangeMap.get(50); // "medium"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (interface, implementation-dependent) - 否（接口，取决于实现）</li>
 *   <li>Null-safe: No (key must not be null) - 否（键不能为null）</li>
 * </ul>
 * @param <K> the type of range endpoints | 范围端点类型
 * @param <V> the type of values | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public interface RangeMap<K extends Comparable<? super K>, V> {

    // ==================== 查询方法 | Query Methods ====================

    /**
     * Get the value associated with the key.
     * 获取与键关联的值。
     *
     * @param key the key | 键
     * @return the value, or null if not found | 值，如果未找到则返回 null
     */
    V get(K key);

    /**
     * Get the entry (range and value) containing the key.
     * 获取包含键的条目（范围和值）。
     *
     * @param key the key | 键
     * @return the entry, or null if not found | 条目，如果未找到则返回 null
     */
    Map.Entry<Range<K>, V> getEntry(K key);

    /**
     * Return the minimal range that encloses all ranges in this map.
     * 返回包含此映射中所有范围的最小范围。
     *
     * @return the span | 跨度
     */
    Range<K> span();

    /**
     * Check if this range map is empty.
     * 检查此范围映射是否为空。
     *
     * @return true if empty | 如果为空则返回 true
     */
    boolean isEmpty();

    // ==================== 视图方法 | View Methods ====================

    /**
     * Return all range-value mappings as a map.
     * 以映射形式返回所有范围-值映射。
     *
     * @return the map | 映射
     */
    Map<Range<K>, V> asMapOfRanges();

    /**
     * Return all range-value mappings in descending order.
     * 以降序返回所有范围-值映射。
     *
     * @return the descending map | 降序映射
     */
    Map<Range<K>, V> asDescendingMapOfRanges();

    /**
     * Return a view of the portion of this map within the given range.
     * 返回此映射在给定范围内的部分的视图。
     *
     * @param range the range | 范围
     * @return the sub range map | 子范围映射
     */
    RangeMap<K, V> subRangeMap(Range<K> range);

    // ==================== 修改方法 | Modification Methods ====================

    /**
     * Associate a range with a value.
     * 将范围与值关联。
     *
     * @param range the range | 范围
     * @param value the value | 值
     */
    void put(Range<K> range, V value);

    /**
     * Associate a range with a value, coalescing with adjacent ranges with the same value.
     * 将范围与值关联，与具有相同值的相邻范围合并。
     *
     * @param range the range | 范围
     * @param value the value | 值
     */
    void putCoalescing(Range<K> range, V value);

    /**
     * Put all mappings from another range map.
     * 从另一个范围映射放入所有映射。
     *
     * @param rangeMap the other range map | 另一个范围映射
     */
    void putAll(RangeMap<K, ? extends V> rangeMap);

    /**
     * Remove a range from this map.
     * 从此映射移除范围。
     *
     * @param range the range to remove | 要移除的范围
     */
    void remove(Range<K> range);

    /**
     * Clear all mappings.
     * 清除所有映射。
     */
    void clear();
}
