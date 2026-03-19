package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Range;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * TreeRangeSet - Tree-based Range Set Implementation
 * TreeRangeSet - 基于树的范围集合实现
 *
 * <p>A range set implementation using a tree structure for efficient range operations.</p>
 * <p>使用树结构进行高效范围操作的范围集合实现。</p>
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
 * TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
 * rangeSet.add(Range.closed(1, 10));
 * rangeSet.add(Range.closed(5, 15));  // coalesces to [1, 15]
 *
 * boolean contains = rangeSet.contains(5);  // true
 * Range<Integer> range = rangeSet.rangeContaining(5);  // [1, 15]
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>add: O(log n) - add: O(log n)</li>
 *   <li>remove: O(log n) - remove: O(log n)</li>
 *   <li>contains: O(log n) - contains: O(log n)</li>
 *   <li>rangeContaining: O(log n) - rangeContaining: O(log n)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: No (nulls not allowed) - 空值安全: 否（不允许空值）</li>
 * </ul>
 *
 * @param <C> the type of range endpoints | 范围端点类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class TreeRangeSet<C extends Comparable<? super C>> implements RangeSet<C>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final NavigableMap<Cut<C>, Range<C>> rangesByLowerBound;

    // ==================== 构造方法 | Constructors ====================

    private TreeRangeSet() {
        this.rangesByLowerBound = new TreeMap<>();
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty TreeRangeSet.
     * 创建空 TreeRangeSet。
     *
     * @param <C> endpoint type | 端点类型
     * @return new empty TreeRangeSet | 新空 TreeRangeSet
     */
    public static <C extends Comparable<? super C>> TreeRangeSet<C> create() {
        return new TreeRangeSet<>();
    }

    /**
     * Create a TreeRangeSet with the given ranges.
     * 使用给定范围创建 TreeRangeSet。
     *
     * @param <C>    endpoint type | 端点类型
     * @param ranges the ranges | 范围
     * @return new TreeRangeSet | 新 TreeRangeSet
     */
    public static <C extends Comparable<? super C>> TreeRangeSet<C> create(Iterable<Range<C>> ranges) {
        TreeRangeSet<C> rangeSet = create();
        for (Range<C> range : ranges) {
            rangeSet.add(range);
        }
        return rangeSet;
    }

    // ==================== RangeSet 实现 | RangeSet Implementation ====================

    @Override
    public boolean contains(C value) {
        return rangeContaining(value) != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Range<C> rangeContaining(C value) {
        Objects.requireNonNull(value);
        Cut<C> valueCut = Cut.belowValue(value);
        Map.Entry<Cut<C>, Range<C>> entry = rangesByLowerBound.floorEntry(valueCut);
        if (entry != null && entry.getValue().contains(value)) {
            return entry.getValue();
        }
        return null;
    }

    @Override
    public boolean encloses(Range<C> otherRange) {
        Objects.requireNonNull(otherRange);
        if (otherRange.isEmpty()) {
            return true;
        }
        Range<C> enclosing = rangeEnclosing(otherRange);
        return enclosing != null;
    }

    private Range<C> rangeEnclosing(Range<C> range) {
        Cut<C> lowerBound = Cut.belowValue(range.lowerEndpoint());
        Map.Entry<Cut<C>, Range<C>> entry = rangesByLowerBound.floorEntry(lowerBound);
        if (entry != null && entry.getValue().encloses(range)) {
            return entry.getValue();
        }
        return null;
    }

    @Override
    public boolean enclosesAll(RangeSet<C> other) {
        for (Range<C> range : other.asRanges()) {
            if (!encloses(range)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean intersects(Range<C> otherRange) {
        Objects.requireNonNull(otherRange);
        Cut<C> lowerBound = Cut.belowValue(otherRange.lowerEndpoint());
        Map.Entry<Cut<C>, Range<C>> entry = rangesByLowerBound.floorEntry(lowerBound);
        if (entry != null && entry.getValue().isConnected(otherRange) && !entry.getValue().intersection(otherRange).isEmpty()) {
            return true;
        }
        entry = rangesByLowerBound.ceilingEntry(lowerBound);
        return entry != null && entry.getValue().isConnected(otherRange) && !entry.getValue().intersection(otherRange).isEmpty();
    }

    @Override
    public boolean isEmpty() {
        return rangesByLowerBound.isEmpty();
    }

    @Override
    public Set<Range<C>> asRanges() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(rangesByLowerBound.values()));
    }

    @Override
    public Set<Range<C>> asDescendingSetOfRanges() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(rangesByLowerBound.descendingMap().values()));
    }

    @Override
    public RangeSet<C> complement() {
        TreeRangeSet<C> result = create();
        if (isEmpty()) {
            // Complement of empty set is unbounded - can't represent without infinity
            // Return empty since we can't represent (-∞, +∞)
            return result;
        }

        List<Range<C>> ranges = new ArrayList<>(asRanges());

        // For each gap between consecutive ranges, add that gap to the complement
        for (int i = 0; i < ranges.size() - 1; i++) {
            Range<C> current = ranges.get(i);
            Range<C> next = ranges.get(i + 1);

            // Create gap range between current upper and next lower
            Range.BoundType lowerType = current.upperBoundType() == Range.BoundType.CLOSED
                    ? Range.BoundType.OPEN : Range.BoundType.CLOSED;
            Range.BoundType upperType = next.lowerBoundType() == Range.BoundType.CLOSED
                    ? Range.BoundType.OPEN : Range.BoundType.CLOSED;

            Range<C> gap = Range.create(lowerType, current.upperEndpoint(),
                    upperType, next.lowerEndpoint());
            if (!gap.isEmpty()) {
                result.rangesByLowerBound.put(Cut.belowValue(gap.lowerEndpoint()), gap);
            }
        }

        return result;
    }

    @Override
    public RangeSet<C> subRangeSet(Range<C> view) {
        TreeRangeSet<C> result = create();
        for (Range<C> range : asRanges()) {
            if (range.isConnected(view)) {
                Range<C> intersection = range.intersection(view);
                if (!intersection.isEmpty()) {
                    result.rangesByLowerBound.put(Cut.belowValue(intersection.lowerEndpoint()), intersection);
                }
            }
        }
        return result;
    }

    @Override
    public Range<C> span() {
        if (isEmpty()) {
            throw new NoSuchElementException("RangeSet is empty");
        }
        Range<C> first = rangesByLowerBound.firstEntry().getValue();
        Range<C> last = rangesByLowerBound.lastEntry().getValue();
        return Range.create(first.lowerBoundType(), first.lowerEndpoint(),
                last.upperBoundType(), last.upperEndpoint());
    }

    @Override
    public void add(Range<C> range) {
        Objects.requireNonNull(range);
        if (range.isEmpty()) {
            return;
        }

        // Find and remove all ranges that will be coalesced
        C lower = range.lowerEndpoint();
        C upper = range.upperEndpoint();
        Range.BoundType lowerType = range.lowerBoundType();
        Range.BoundType upperType = range.upperBoundType();

        // Check for ranges that connect with this one
        Cut<C> lowerCut = Cut.belowValue(lower);
        Map.Entry<Cut<C>, Range<C>> floorEntry = rangesByLowerBound.floorEntry(lowerCut);
        if (floorEntry != null && floorEntry.getValue().isConnected(range)) {
            Range<C> floor = floorEntry.getValue();
            @SuppressWarnings("unchecked")
            int cmp = ((Comparable<C>) floor.lowerEndpoint()).compareTo(lower);
            if (cmp < 0 || (cmp == 0 && floor.lowerBoundType() == Range.BoundType.CLOSED)) {
                lower = floor.lowerEndpoint();
                lowerType = floor.lowerBoundType();
            }
            @SuppressWarnings("unchecked")
            int cmpUpper = ((Comparable<C>) floor.upperEndpoint()).compareTo(upper);
            if (cmpUpper > 0 || (cmpUpper == 0 && floor.upperBoundType() == Range.BoundType.CLOSED)) {
                upper = floor.upperEndpoint();
                upperType = floor.upperBoundType();
            }
            rangesByLowerBound.remove(floorEntry.getKey());
        }

        // Remove all ranges enclosed by the new range
        Cut<C> upperCut = Cut.belowValue(upper);
        NavigableMap<Cut<C>, Range<C>> subMap = rangesByLowerBound.subMap(lowerCut, false, upperCut, true);
        Iterator<Map.Entry<Cut<C>, Range<C>>> it = subMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Cut<C>, Range<C>> entry = it.next();
            Range<C> r = entry.getValue();
            @SuppressWarnings("unchecked")
            int cmpUpper = ((Comparable<C>) r.upperEndpoint()).compareTo(upper);
            if (cmpUpper > 0 || (cmpUpper == 0 && r.upperBoundType() == Range.BoundType.CLOSED && upperType == Range.BoundType.OPEN)) {
                upper = r.upperEndpoint();
                upperType = r.upperBoundType();
            }
            it.remove();
        }

        Range<C> newRange = Range.create(lowerType, lower, upperType, upper);
        rangesByLowerBound.put(Cut.belowValue(newRange.lowerEndpoint()), newRange);
    }

    @Override
    public void remove(Range<C> range) {
        Objects.requireNonNull(range);
        if (range.isEmpty()) {
            return;
        }

        Cut<C> lowerCut = Cut.belowValue(range.lowerEndpoint());
        Cut<C> upperCut = Cut.belowValue(range.upperEndpoint());

        // Check for a range that starts before and overlaps
        Map.Entry<Cut<C>, Range<C>> floorEntry = rangesByLowerBound.floorEntry(lowerCut);
        if (floorEntry != null) {
            Range<C> floor = floorEntry.getValue();
            if (floor.isConnected(range)) {
                rangesByLowerBound.remove(floorEntry.getKey());
                // Add back the portion before the removed range
                @SuppressWarnings("unchecked")
                int cmpLower = ((Comparable<C>) floor.lowerEndpoint()).compareTo(range.lowerEndpoint());
                if (cmpLower < 0) {
                    Range<C> before = Range.create(floor.lowerBoundType(), floor.lowerEndpoint(),
                            range.lowerBoundType() == Range.BoundType.CLOSED ? Range.BoundType.OPEN : Range.BoundType.CLOSED,
                            range.lowerEndpoint());
                    if (!before.isEmpty()) {
                        rangesByLowerBound.put(Cut.belowValue(before.lowerEndpoint()), before);
                    }
                }
                // Add back the portion after the removed range
                @SuppressWarnings("unchecked")
                int cmpUpper = ((Comparable<C>) floor.upperEndpoint()).compareTo(range.upperEndpoint());
                if (cmpUpper > 0) {
                    Range<C> after = Range.create(
                            range.upperBoundType() == Range.BoundType.CLOSED ? Range.BoundType.OPEN : Range.BoundType.CLOSED,
                            range.upperEndpoint(), floor.upperBoundType(), floor.upperEndpoint());
                    if (!after.isEmpty()) {
                        rangesByLowerBound.put(Cut.belowValue(after.lowerEndpoint()), after);
                    }
                }
            }
        }

        // Remove all ranges completely contained within the removed range
        NavigableMap<Cut<C>, Range<C>> subMap = rangesByLowerBound.subMap(lowerCut, false, upperCut, true);
        Iterator<Map.Entry<Cut<C>, Range<C>>> it = subMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Cut<C>, Range<C>> entry = it.next();
            Range<C> r = entry.getValue();
            if (range.encloses(r)) {
                it.remove();
            } else if (r.isConnected(range)) {
                it.remove();
                // Add back the portion after the removed range
                @SuppressWarnings("unchecked")
                int cmpUpper = ((Comparable<C>) r.upperEndpoint()).compareTo(range.upperEndpoint());
                if (cmpUpper > 0) {
                    Range<C> after = Range.create(
                            range.upperBoundType() == Range.BoundType.CLOSED ? Range.BoundType.OPEN : Range.BoundType.CLOSED,
                            range.upperEndpoint(), r.upperBoundType(), r.upperEndpoint());
                    if (!after.isEmpty()) {
                        rangesByLowerBound.put(Cut.belowValue(after.lowerEndpoint()), after);
                    }
                }
            }
        }
    }

    @Override
    public void addAll(RangeSet<C> other) {
        for (Range<C> range : other.asRanges()) {
            add(range);
        }
    }

    @Override
    public void removeAll(RangeSet<C> other) {
        for (Range<C> range : other.asRanges()) {
            remove(range);
        }
    }

    @Override
    public void clear() {
        rangesByLowerBound.clear();
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RangeSet<?> that)) return false;
        return asRanges().equals(that.asRanges());
    }

    @Override
    public int hashCode() {
        return asRanges().hashCode();
    }

    @Override
    public String toString() {
        return asRanges().toString();
    }

    // ==================== 内部类 | Internal Classes ====================

    /**
     * Internal representation of a cut point.
     */
    private static final class Cut<C extends Comparable<? super C>> implements Comparable<Cut<C>>, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private final C endpoint;
        private final boolean below;

        private Cut(C endpoint, boolean below) {
            this.endpoint = endpoint;
            this.below = below;
        }

        static <C extends Comparable<? super C>> Cut<C> belowValue(C value) {
            return new Cut<>(value, true);
        }

        @Override
        public int compareTo(Cut<C> other) {
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
