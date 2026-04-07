package cloud.opencode.base.collections.immutable;

import cloud.opencode.base.collections.Range;
import cloud.opencode.base.collections.specialized.RangeMap;
import cloud.opencode.base.collections.specialized.TreeRangeMap;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * ImmutableRangeMap - Immutable Range Map
 * ImmutableRangeMap - 不可变范围映射
 *
 * <p>An immutable implementation of {@link RangeMap} that maps disjoint non-empty
 * ranges to values. All mutation methods throw {@link UnsupportedOperationException}.</p>
 * <p>{@link RangeMap} 的不可变实现，将不相交的非空范围映射到值。
 * 所有变更方法抛出 {@link UnsupportedOperationException}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable &amp; thread-safe - 不可变且线程安全</li>
 *   <li>Binary search for efficient key lookup - 二分搜索实现高效键查找</li>
 *   <li>Builder pattern using {@link TreeRangeMap} for range management - 使用 TreeRangeMap 管理范围的构建器模式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create via builder - 通过构建器创建
 * ImmutableRangeMap<Integer, String> rangeMap = ImmutableRangeMap.<Integer, String>builder()
 *     .put(Range.closed(1, 10), "small")
 *     .put(Range.closed(11, 100), "medium")
 *     .put(Range.closed(101, 1000), "large")
 *     .build();
 *
 * // Query - 查询
 * String value = rangeMap.get(5);   // "small"
 * String value2 = rangeMap.get(50); // "medium"
 *
 * // Get entry - 获取条目
 * Map.Entry<Range<Integer>, String> entry = rangeMap.getEntry(5);
 * // entry.getKey() = [1, 10], entry.getValue() = "small"
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>get / getEntry: O(log n) via binary search - O(log n) 通过二分搜索</li>
 *   <li>asMapOfRanges: O(n) - O(n)</li>
 *   <li>subRangeMap: O(n) - O(n)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: No (keys and values must not be null) - 空值安全: 否（键和值不能为null）</li>
 * </ul>
 *
 * @param <K> the type of range endpoints | 范围端点类型
 * @param <V> the type of values | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
public final class ImmutableRangeMap<K extends Comparable<? super K>, V> implements RangeMap<K, V>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String MUTATION_MSG = "ImmutableRangeMap does not support mutation";

    @SuppressWarnings("rawtypes")
    private static final ImmutableRangeMap EMPTY = new ImmutableRangeMap<>(List.of());

    /**
     * Sorted list of range-value entries, sorted by range lower bound.
     * 按范围下界排序的范围-值条目的有序列表。
     */
    private final List<RangeEntry<K, V>> entries;

    // ==================== 构造方法 | Constructors ====================

    private ImmutableRangeMap(List<RangeEntry<K, V>> entries) {
        this.entries = List.copyOf(entries);
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty immutable range map.
     * 创建空的不可变范围映射。
     *
     * @param <K> endpoint type | 端点类型
     * @param <V> value type | 值类型
     * @return empty immutable range map | 空的不可变范围映射
     */
    @SuppressWarnings("unchecked")
    public static <K extends Comparable<? super K>, V> ImmutableRangeMap<K, V> of() {
        return (ImmutableRangeMap<K, V>) EMPTY;
    }

    /**
     * Create an immutable range map with a single range-value mapping.
     * 创建包含单个范围-值映射的不可变范围映射。
     *
     * @param <K>   endpoint type | 端点类型
     * @param <V>   value type | 值类型
     * @param range the range | 范围
     * @param value the value | 值
     * @return immutable range map | 不可变范围映射
     */
    public static <K extends Comparable<? super K>, V> ImmutableRangeMap<K, V> of(Range<K> range, V value) {
        Objects.requireNonNull(range, "range must not be null");
        Objects.requireNonNull(value, "value must not be null");
        if (range.isEmpty()) {
            return of();
        }
        return new ImmutableRangeMap<>(List.of(new RangeEntry<>(range, value)));
    }

    /**
     * Create an immutable range map by copying from a {@link RangeMap}.
     * 通过复制 {@link RangeMap} 创建不可变范围映射。
     *
     * @param <K>      endpoint type | 端点类型
     * @param <V>      value type | 值类型
     * @param rangeMap the range map to copy | 要复制的范围映射
     * @return immutable range map | 不可变范围映射
     */
    public static <K extends Comparable<? super K>, V> ImmutableRangeMap<K, V> copyOf(RangeMap<K, V> rangeMap) {
        Objects.requireNonNull(rangeMap, "rangeMap must not be null");
        if (rangeMap instanceof ImmutableRangeMap<K, V> immutable) {
            return immutable;
        }
        if (rangeMap.isEmpty()) {
            return of();
        }
        // Route through TreeRangeMap to guarantee sorted entries
        TreeRangeMap<K, V> tree = TreeRangeMap.create();
        tree.putAll(rangeMap);
        List<RangeEntry<K, V>> entryList = new ArrayList<>();
        for (Map.Entry<Range<K>, V> entry : tree.asMapOfRanges().entrySet()) {
            entryList.add(new RangeEntry<>(entry.getKey(), entry.getValue()));
        }
        return new ImmutableRangeMap<>(entryList);
    }

    /**
     * Create a new builder for {@link ImmutableRangeMap}.
     * 创建 {@link ImmutableRangeMap} 的新构建器。
     *
     * @param <K> endpoint type | 端点类型
     * @param <V> value type | 值类型
     * @return new builder | 新构建器
     */
    public static <K extends Comparable<? super K>, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    // ==================== 查询方法 | Query Methods ====================

    @Override
    public V get(K key) {
        Map.Entry<Range<K>, V> entry = getEntry(key);
        return entry != null ? entry.getValue() : null;
    }

    @Override
    public Map.Entry<Range<K>, V> getEntry(K key) {
        Objects.requireNonNull(key, "key must not be null");
        int index = findCandidateIndex(key);
        if (index >= 0 && index < entries.size()) {
            RangeEntry<K, V> candidate = entries.get(index);
            if (candidate.range.contains(key)) {
                return Map.entry(candidate.range, candidate.value);
            }
        }
        // Also check the entry before the candidate — the candidate's open lower bound
        // may exclude the key while the preceding range's upper bound includes it.
        if (index > 0) {
            RangeEntry<K, V> before = entries.get(index - 1);
            if (before.range.contains(key)) {
                return Map.entry(before.range, before.value);
            }
        }
        return null;
    }

    @Override
    public Range<K> span() {
        if (isEmpty()) {
            throw new NoSuchElementException("RangeMap is empty");
        }
        RangeEntry<K, V> first = entries.getFirst();
        RangeEntry<K, V> last = entries.getLast();
        return first.range.span(last.range);
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public Map<Range<K>, V> asMapOfRanges() {
        Map<Range<K>, V> result = new LinkedHashMap<>();
        for (RangeEntry<K, V> entry : entries) {
            result.put(entry.range, entry.value);
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<Range<K>, V> asDescendingMapOfRanges() {
        Map<Range<K>, V> result = new LinkedHashMap<>();
        for (int i = entries.size() - 1; i >= 0; i--) {
            RangeEntry<K, V> entry = entries.get(i);
            result.put(entry.range, entry.value);
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public RangeMap<K, V> subRangeMap(Range<K> range) {
        Objects.requireNonNull(range, "range must not be null");
        if (range.isEmpty() || isEmpty()) {
            return of();
        }

        List<RangeEntry<K, V>> result = new ArrayList<>();
        for (RangeEntry<K, V> entry : entries) {
            if (entry.range.isConnected(range)) {
                Range<K> intersection = entry.range.intersection(range);
                if (!intersection.isEmpty()) {
                    result.add(new RangeEntry<>(intersection, entry.value));
                }
            }
        }

        return result.isEmpty() ? of() : new ImmutableRangeMap<>(result);
    }

    // ==================== 变更方法（不支持）| Mutation Methods (Unsupported) ====================

    /**
     * Not supported. Always throws {@link UnsupportedOperationException}.
     * 不支持。始终抛出 {@link UnsupportedOperationException}。
     *
     * @param range the range | 范围
     * @param value the value | 值
     * @throws UnsupportedOperationException always | 始终抛出
     */
    @Override
    public void put(Range<K> range, V value) {
        throw new UnsupportedOperationException(MUTATION_MSG);
    }

    /**
     * Not supported. Always throws {@link UnsupportedOperationException}.
     * 不支持。始终抛出 {@link UnsupportedOperationException}。
     *
     * @param range the range | 范围
     * @param value the value | 值
     * @throws UnsupportedOperationException always | 始终抛出
     */
    @Override
    public void putCoalescing(Range<K> range, V value) {
        throw new UnsupportedOperationException(MUTATION_MSG);
    }

    /**
     * Not supported. Always throws {@link UnsupportedOperationException}.
     * 不支持。始终抛出 {@link UnsupportedOperationException}。
     *
     * @param rangeMap the range map | 范围映射
     * @throws UnsupportedOperationException always | 始终抛出
     */
    @Override
    public void putAll(RangeMap<K, ? extends V> rangeMap) {
        throw new UnsupportedOperationException(MUTATION_MSG);
    }

    /**
     * Not supported. Always throws {@link UnsupportedOperationException}.
     * 不支持。始终抛出 {@link UnsupportedOperationException}。
     *
     * @param range the range | 范围
     * @throws UnsupportedOperationException always | 始终抛出
     */
    @Override
    public void remove(Range<K> range) {
        throw new UnsupportedOperationException(MUTATION_MSG);
    }

    /**
     * Not supported. Always throws {@link UnsupportedOperationException}.
     * 不支持。始终抛出 {@link UnsupportedOperationException}。
     *
     * @throws UnsupportedOperationException always | 始终抛出
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException(MUTATION_MSG);
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RangeMap<?, ?> that)) return false;
        return asMapOfRanges().equals(that.asMapOfRanges());
    }

    @Override
    public int hashCode() {
        return asMapOfRanges().hashCode();
    }

    @Override
    public String toString() {
        return asMapOfRanges().toString();
    }

    // ==================== 内部方法 | Internal Methods ====================

    /**
     * Binary search to find the index of the entry whose range lower endpoint
     * is less than or equal to the key.
     * 二分搜索查找范围下端点小于或等于键的条目索引。
     */
    private int findCandidateIndex(K key) {
        int lo = 0;
        int hi = entries.size() - 1;
        int result = -1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            Range<K> midRange = entries.get(mid).range;
            if (!midRange.hasLowerBound() || midRange.lowerEndpoint().compareTo(key) <= 0) {
                result = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return result;
    }

    // ==================== 内部类 | Internal Classes ====================

    /**
     * Internal record holding a range and its associated value.
     * 内部记录，持有范围及其关联的值。
     */
    private record RangeEntry<K extends Comparable<? super K>, V>(Range<K> range, V value) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    // ==================== 构建器 | Builder ====================

    /**
     * Builder for {@link ImmutableRangeMap}.
     * {@link ImmutableRangeMap} 的构建器。
     *
     * <p>Uses a {@link TreeRangeMap} internally for range management.</p>
     * <p>内部使用 {@link TreeRangeMap} 进行范围管理。</p>
     *
     * @param <K> endpoint type | 端点类型
     * @param <V> value type | 值类型
     */
    public static final class Builder<K extends Comparable<? super K>, V> {

        private final TreeRangeMap<K, V> rangeMap = TreeRangeMap.create();

        private Builder() {
        }

        /**
         * Put a range-value mapping into the builder.
         * 向构建器放入范围-值映射。
         *
         * @param range the range | 范围
         * @param value the value | 值
         * @return this builder | 此构建器
         */
        public Builder<K, V> put(Range<K> range, V value) {
            Objects.requireNonNull(range, "range must not be null");
            Objects.requireNonNull(value, "value must not be null");
            rangeMap.put(range, value);
            return this;
        }

        /**
         * Put all mappings from another range map.
         * 从另一个范围映射放入所有映射。
         *
         * @param other the other range map | 另一个范围映射
         * @return this builder | 此构建器
         */
        public Builder<K, V> putAll(RangeMap<K, ? extends V> other) {
            Objects.requireNonNull(other, "other must not be null");
            rangeMap.putAll(other);
            return this;
        }

        /**
         * Build the immutable range map.
         * 构建不可变范围映射。
         *
         * @return immutable range map | 不可变范围映射
         */
        public ImmutableRangeMap<K, V> build() {
            if (rangeMap.isEmpty()) {
                return of();
            }
            List<RangeEntry<K, V>> entryList = new ArrayList<>();
            for (Map.Entry<Range<K>, V> entry : rangeMap.asMapOfRanges().entrySet()) {
                entryList.add(new RangeEntry<>(entry.getKey(), entry.getValue()));
            }
            return new ImmutableRangeMap<>(entryList);
        }
    }
}
