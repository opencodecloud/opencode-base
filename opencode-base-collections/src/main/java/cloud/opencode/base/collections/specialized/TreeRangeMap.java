package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Range;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * TreeRangeMap - Tree-based Range Map Implementation
 * TreeRangeMap - 基于树的范围映射实现
 *
 * <p>A range map implementation using a tree structure for efficient range operations.</p>
 * <p>使用树结构进行高效范围操作的范围映射实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Automatic range coalescing - 自动范围合并</li>
 *   <li>O(log n) operations - O(log n) 操作</li>
 *   <li>Efficient range queries - 高效范围查询</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TreeRangeMap<Integer, String> rangeMap = TreeRangeMap.create();
 * rangeMap.put(Range.closed(1, 10), "small");
 * rangeMap.put(Range.closed(11, 100), "medium");
 *
 * String value = rangeMap.get(5);  // "small"
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>get: O(log n) - get: O(log n)</li>
 *   <li>put: O(log n + k) where k is removed ranges - put: O(log n + k) 其中 k 是移除的范围数</li>
 *   <li>remove: O(log n + k) - remove: O(log n + k)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Values cannot be null - 空值安全: 值不能为 null</li>
 * </ul>
 *
 * @param <K> the type of range endpoints | 范围端点类型
 * @param <V> the type of values | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class TreeRangeMap<K extends Comparable<? super K>, V> implements RangeMap<K, V>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final NavigableMap<Cut<K>, RangeMapEntry<K, V>> entriesByLowerBound;

    // ==================== 构造方法 | Constructors ====================

    private TreeRangeMap() {
        this.entriesByLowerBound = new TreeMap<>();
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty TreeRangeMap.
     * 创建空 TreeRangeMap。
     *
     * @param <K> endpoint type | 端点类型
     * @param <V> value type | 值类型
     * @return new empty TreeRangeMap | 新空 TreeRangeMap
     */
    public static <K extends Comparable<? super K>, V> TreeRangeMap<K, V> create() {
        return new TreeRangeMap<>();
    }

    // ==================== RangeMap 实现 | RangeMap Implementation ====================

    @Override
    public V get(K key) {
        Map.Entry<Range<K>, V> entry = getEntry(key);
        return entry != null ? entry.getValue() : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map.Entry<Range<K>, V> getEntry(K key) {
        Objects.requireNonNull(key);
        Cut<K> keyCut = Cut.belowValue(key);
        Map.Entry<Cut<K>, RangeMapEntry<K, V>> entry = entriesByLowerBound.floorEntry(keyCut);
        if (entry != null && entry.getValue().range.contains(key)) {
            return Map.entry(entry.getValue().range, entry.getValue().value);
        }
        return null;
    }

    @Override
    public Range<K> span() {
        if (isEmpty()) {
            throw new NoSuchElementException("RangeMap is empty");
        }
        RangeMapEntry<K, V> first = entriesByLowerBound.firstEntry().getValue();
        RangeMapEntry<K, V> last = entriesByLowerBound.lastEntry().getValue();
        return Range.create(first.range.lowerBoundType(), first.range.lowerEndpoint(),
                last.range.upperBoundType(), last.range.upperEndpoint());
    }

    @Override
    public boolean isEmpty() {
        return entriesByLowerBound.isEmpty();
    }

    @Override
    public Map<Range<K>, V> asMapOfRanges() {
        Map<Range<K>, V> result = new LinkedHashMap<>();
        for (RangeMapEntry<K, V> entry : entriesByLowerBound.values()) {
            result.put(entry.range, entry.value);
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<Range<K>, V> asDescendingMapOfRanges() {
        Map<Range<K>, V> result = new LinkedHashMap<>();
        for (RangeMapEntry<K, V> entry : entriesByLowerBound.descendingMap().values()) {
            result.put(entry.range, entry.value);
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public RangeMap<K, V> subRangeMap(Range<K> range) {
        TreeRangeMap<K, V> result = create();
        for (RangeMapEntry<K, V> entry : entriesByLowerBound.values()) {
            if (entry.range.isConnected(range)) {
                Range<K> intersection = entry.range.intersection(range);
                if (!intersection.isEmpty()) {
                    result.entriesByLowerBound.put(Cut.belowValue(intersection.lowerEndpoint()),
                            new RangeMapEntry<>(intersection, entry.value));
                }
            }
        }
        return result;
    }

    @Override
    public void put(Range<K> range, V value) {
        Objects.requireNonNull(range);
        Objects.requireNonNull(value);
        if (range.isEmpty()) {
            return;
        }

        // Remove all overlapping ranges
        remove(range);

        // Add the new entry
        entriesByLowerBound.put(Cut.belowValue(range.lowerEndpoint()),
                new RangeMapEntry<>(range, value));
    }

    @Override
    public void putCoalescing(Range<K> range, V value) {
        Objects.requireNonNull(range);
        Objects.requireNonNull(value);
        if (range.isEmpty()) {
            return;
        }

        // Find adjacent ranges with the same value and coalesce
        Range<K> newRange = range;

        // Check for adjacent range before
        Cut<K> lowerCut = Cut.belowValue(range.lowerEndpoint());
        Map.Entry<Cut<K>, RangeMapEntry<K, V>> floorEntry = entriesByLowerBound.floorEntry(lowerCut);
        if (floorEntry != null && floorEntry.getValue().value.equals(value)) {
            Range<K> floor = floorEntry.getValue().range;
            if (floor.isConnected(newRange)) {
                @SuppressWarnings("unchecked")
                int cmpLower = ((Comparable<K>) floor.lowerEndpoint()).compareTo(newRange.lowerEndpoint());
                if (cmpLower <= 0) {
                    @SuppressWarnings("unchecked")
                    int cmpUpper = ((Comparable<K>) floor.upperEndpoint()).compareTo(newRange.upperEndpoint());
                    newRange = Range.create(floor.lowerBoundType(), floor.lowerEndpoint(),
                            cmpUpper >= 0 ? floor.upperBoundType() : newRange.upperBoundType(),
                            cmpUpper >= 0 ? floor.upperEndpoint() : newRange.upperEndpoint());
                    entriesByLowerBound.remove(floorEntry.getKey());
                }
            }
        }

        // Check for adjacent ranges after and coalesce
        while (true) {
            Cut<K> upperCut = Cut.belowValue(newRange.upperEndpoint());
            Map.Entry<Cut<K>, RangeMapEntry<K, V>> ceilingEntry = entriesByLowerBound.ceilingEntry(upperCut);
            if (ceilingEntry == null) break;

            RangeMapEntry<K, V> ceiling = ceilingEntry.getValue();
            if (!ceiling.range.isConnected(newRange) || !ceiling.value.equals(value)) break;

            @SuppressWarnings("unchecked")
            int cmpUpper = ((Comparable<K>) ceiling.range.upperEndpoint()).compareTo(newRange.upperEndpoint());
            newRange = Range.create(newRange.lowerBoundType(), newRange.lowerEndpoint(),
                    cmpUpper >= 0 ? ceiling.range.upperBoundType() : newRange.upperBoundType(),
                    cmpUpper >= 0 ? ceiling.range.upperEndpoint() : newRange.upperEndpoint());
            entriesByLowerBound.remove(ceilingEntry.getKey());
        }

        // Remove any remaining overlapping ranges and put the coalesced range
        remove(newRange);
        entriesByLowerBound.put(Cut.belowValue(newRange.lowerEndpoint()),
                new RangeMapEntry<>(newRange, value));
    }

    @Override
    public void putAll(RangeMap<K, ? extends V> rangeMap) {
        for (Map.Entry<Range<K>, ? extends V> entry : rangeMap.asMapOfRanges().entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void remove(Range<K> range) {
        Objects.requireNonNull(range);
        if (range.isEmpty()) {
            return;
        }

        Cut<K> lowerCut = Cut.belowValue(range.lowerEndpoint());

        // Handle floor entry that may overlap
        Map.Entry<Cut<K>, RangeMapEntry<K, V>> floorEntry = entriesByLowerBound.floorEntry(lowerCut);
        if (floorEntry != null) {
            RangeMapEntry<K, V> floor = floorEntry.getValue();
            if (floor.range.isConnected(range)) {
                Range<K> intersection = floor.range.intersection(range);
                if (!intersection.isEmpty()) {
                    entriesByLowerBound.remove(floorEntry.getKey());

                    // Add back the portion before the removed range
                    int cmpLower = ((Comparable<K>) floor.range.lowerEndpoint()).compareTo(range.lowerEndpoint());
                    if (cmpLower < 0) {
                        Range<K> before = Range.create(floor.range.lowerBoundType(), floor.range.lowerEndpoint(),
                                range.lowerBoundType() == Range.BoundType.CLOSED ? Range.BoundType.OPEN : Range.BoundType.CLOSED,
                                range.lowerEndpoint());
                        if (!before.isEmpty()) {
                            entriesByLowerBound.put(Cut.belowValue(before.lowerEndpoint()),
                                    new RangeMapEntry<>(before, floor.value));
                        }
                    }

                    // Add back the portion after the removed range
                    int cmpUpper = ((Comparable<K>) floor.range.upperEndpoint()).compareTo(range.upperEndpoint());
                    if (cmpUpper > 0) {
                        Range<K> after = Range.create(
                                range.upperBoundType() == Range.BoundType.CLOSED ? Range.BoundType.OPEN : Range.BoundType.CLOSED,
                                range.upperEndpoint(), floor.range.upperBoundType(), floor.range.upperEndpoint());
                        if (!after.isEmpty()) {
                            entriesByLowerBound.put(Cut.belowValue(after.lowerEndpoint()),
                                    new RangeMapEntry<>(after, floor.value));
                        }
                    }
                }
            }
        }

        // Remove all entries completely contained in the removed range
        Cut<K> upperCut = Cut.belowValue(range.upperEndpoint());
        NavigableMap<Cut<K>, RangeMapEntry<K, V>> subMap = entriesByLowerBound.subMap(lowerCut, false, upperCut, true);
        Iterator<Map.Entry<Cut<K>, RangeMapEntry<K, V>>> it = subMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Cut<K>, RangeMapEntry<K, V>> entry = it.next();
            RangeMapEntry<K, V> rme = entry.getValue();
            if (range.encloses(rme.range)) {
                it.remove();
            } else if (rme.range.isConnected(range)) {
                it.remove();
                // Add back the portion after the removed range
                int cmpUpper = ((Comparable<K>) rme.range.upperEndpoint()).compareTo(range.upperEndpoint());
                if (cmpUpper > 0) {
                    Range<K> after = Range.create(
                            range.upperBoundType() == Range.BoundType.CLOSED ? Range.BoundType.OPEN : Range.BoundType.CLOSED,
                            range.upperEndpoint(), rme.range.upperBoundType(), rme.range.upperEndpoint());
                    if (!after.isEmpty()) {
                        entriesByLowerBound.put(Cut.belowValue(after.lowerEndpoint()),
                                new RangeMapEntry<>(after, rme.value));
                    }
                }
            }
        }
    }

    @Override
    public void clear() {
        entriesByLowerBound.clear();
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

    // ==================== 内部类 | Internal Classes ====================

    private record RangeMapEntry<K extends Comparable<? super K>, V>(Range<K> range, V value) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    private static final class Cut<K extends Comparable<? super K>> implements Comparable<Cut<K>>, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private final K endpoint;
        private final boolean below;

        private Cut(K endpoint, boolean below) {
            this.endpoint = endpoint;
            this.below = below;
        }

        static <K extends Comparable<? super K>> Cut<K> belowValue(K value) {
            return new Cut<>(value, true);
        }

        @Override
        public int compareTo(Cut<K> other) {
            int cmp = endpoint.compareTo(other.endpoint);
            if (cmp != 0) return cmp;
            return Boolean.compare(below, other.below);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Cut<?> cut)) return false;
            return below == cut.below && Objects.equals(endpoint, cut.endpoint);
        }

        @Override
        public int hashCode() {
            return Objects.hash(endpoint, below);
        }
    }
}
