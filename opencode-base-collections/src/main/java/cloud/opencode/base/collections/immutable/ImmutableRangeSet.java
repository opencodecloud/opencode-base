package cloud.opencode.base.collections.immutable;

import cloud.opencode.base.collections.Range;
import cloud.opencode.base.collections.specialized.RangeSet;
import cloud.opencode.base.collections.specialized.TreeRangeSet;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * ImmutableRangeSet - Immutable Range Set
 * ImmutableRangeSet - 不可变范围集合
 *
 * <p>An immutable implementation of {@link RangeSet} that stores non-overlapping,
 * non-adjacent ranges sorted by lower bound. All mutation methods throw
 * {@link UnsupportedOperationException}.</p>
 * <p>{@link RangeSet} 的不可变实现，存储按下界排序的非重叠、非相邻范围。
 * 所有变更方法抛出 {@link UnsupportedOperationException}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable &amp; thread-safe - 不可变且线程安全</li>
 *   <li>Binary search for efficient queries - 二分搜索实现高效查询</li>
 *   <li>Set operations (union, intersection, difference) - 集合操作（并集、交集、差集）</li>
 *   <li>Builder pattern using {@link TreeRangeSet} for coalescing - 使用 TreeRangeSet 合并的构建器模式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create via builder - 通过构建器创建
 * ImmutableRangeSet<Integer> rangeSet = ImmutableRangeSet.<Integer>builder()
 *     .add(Range.closed(1, 10))
 *     .add(Range.closed(20, 30))
 *     .build();
 *
 * // Query - 查询
 * boolean contains = rangeSet.contains(5);  // true
 * Range<Integer> range = rangeSet.rangeContaining(5);  // [1, 10]
 *
 * // Set operations - 集合操作
 * ImmutableRangeSet<Integer> other = ImmutableRangeSet.of(Range.closed(5, 25));
 * ImmutableRangeSet<Integer> union = rangeSet.union(other);
 * ImmutableRangeSet<Integer> intersection = rangeSet.intersection(other);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>contains / rangeContaining: O(log n) via binary search - O(log n) 通过二分搜索</li>
 *   <li>encloses / intersects: O(log n) - O(log n)</li>
 *   <li>complement / subRangeSet: O(n) - O(n)</li>
 *   <li>union / intersection / difference: O(n + m) - O(n + m)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: No (nulls not allowed) - 空值安全: 否（不允许空值）</li>
 * </ul>
 *
 * @param <C> the type of range endpoints | 范围端点类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
public final class ImmutableRangeSet<C extends Comparable<? super C>> implements RangeSet<C>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String MUTATION_MSG = "ImmutableRangeSet does not support mutation";

    @SuppressWarnings("rawtypes")
    private static final ImmutableRangeSet EMPTY = new ImmutableRangeSet<>(List.of());

    private final List<Range<C>> ranges;

    // ==================== 构造方法 | Constructors ====================

    private ImmutableRangeSet(List<Range<C>> ranges) {
        this.ranges = List.copyOf(ranges);
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty immutable range set.
     * 创建空的不可变范围集合。
     *
     * @param <C> endpoint type | 端点类型
     * @return empty immutable range set | 空的不可变范围集合
     */
    @SuppressWarnings("unchecked")
    public static <C extends Comparable<? super C>> ImmutableRangeSet<C> of() {
        return (ImmutableRangeSet<C>) EMPTY;
    }

    /**
     * Create an immutable range set containing a single range.
     * 创建包含单个范围的不可变范围集合。
     *
     * @param <C>   endpoint type | 端点类型
     * @param range the range | 范围
     * @return immutable range set | 不可变范围集合
     */
    public static <C extends Comparable<? super C>> ImmutableRangeSet<C> of(Range<C> range) {
        Objects.requireNonNull(range, "range must not be null");
        if (range.isEmpty()) {
            return of();
        }
        return new ImmutableRangeSet<>(List.of(range));
    }

    /**
     * Create an immutable range set by copying from a {@link RangeSet}.
     * 通过复制 {@link RangeSet} 创建不可变范围集合。
     *
     * @param <C>      endpoint type | 端点类型
     * @param rangeSet the range set to copy | 要复制的范围集合
     * @return immutable range set | 不可变范围集合
     */
    public static <C extends Comparable<? super C>> ImmutableRangeSet<C> copyOf(RangeSet<C> rangeSet) {
        Objects.requireNonNull(rangeSet, "rangeSet must not be null");
        if (rangeSet instanceof ImmutableRangeSet<C> immutable) {
            return immutable;
        }
        if (rangeSet.isEmpty()) {
            return of();
        }
        // Route through TreeRangeSet to guarantee sorted, coalesced ranges
        TreeRangeSet<C> tree = TreeRangeSet.create();
        tree.addAll(rangeSet);
        return new ImmutableRangeSet<>(new ArrayList<>(tree.asRanges()));
    }

    /**
     * Create an immutable range set by copying from an iterable of ranges.
     * Ranges are coalesced using a {@link TreeRangeSet}.
     * 通过复制范围的可迭代对象创建不可变范围集合。使用 {@link TreeRangeSet} 合并范围。
     *
     * @param <C>    endpoint type | 端点类型
     * @param ranges the ranges | 范围
     * @return immutable range set | 不可变范围集合
     */
    public static <C extends Comparable<? super C>> ImmutableRangeSet<C> copyOf(Iterable<Range<C>> ranges) {
        Objects.requireNonNull(ranges, "ranges must not be null");
        TreeRangeSet<C> treeRangeSet = TreeRangeSet.create();
        for (Range<C> range : ranges) {
            treeRangeSet.add(range);
        }
        if (treeRangeSet.isEmpty()) {
            return of();
        }
        return new ImmutableRangeSet<>(new ArrayList<>(treeRangeSet.asRanges()));
    }

    /**
     * Create a new builder for {@link ImmutableRangeSet}.
     * 创建 {@link ImmutableRangeSet} 的新构建器。
     *
     * @param <C> endpoint type | 端点类型
     * @return new builder | 新构建器
     */
    public static <C extends Comparable<? super C>> Builder<C> builder() {
        return new Builder<>();
    }

    // ==================== 查询方法 | Query Methods ====================

    @Override
    public boolean contains(C value) {
        return rangeContaining(value) != null;
    }

    @Override
    public Range<C> rangeContaining(C value) {
        Objects.requireNonNull(value, "value must not be null");
        int index = findCandidateIndex(value);
        if (index >= 0 && index < ranges.size()) {
            Range<C> candidate = ranges.get(index);
            if (candidate.contains(value)) {
                return candidate;
            }
        }
        // Also check the range before the candidate — the candidate's open lower bound
        // may exclude the value while the preceding range's upper bound includes it.
        if (index > 0) {
            Range<C> before = ranges.get(index - 1);
            if (before.contains(value)) {
                return before;
            }
        }
        return null;
    }

    @Override
    public boolean encloses(Range<C> otherRange) {
        Objects.requireNonNull(otherRange, "otherRange must not be null");
        if (otherRange.isEmpty()) {
            return true;
        }
        if (!otherRange.hasLowerBound()) {
            // Only a range with no lower bound can enclose this — linear scan required
            for (Range<C> r : ranges) {
                if (r.encloses(otherRange)) return true;
            }
            return false;
        }
        int index = findCandidateIndex(otherRange.lowerEndpoint());
        if (index >= 0 && index < ranges.size()) {
            return ranges.get(index).encloses(otherRange);
        }
        return false;
    }

    @Override
    public boolean enclosesAll(RangeSet<C> other) {
        Objects.requireNonNull(other, "other must not be null");
        for (Range<C> range : other.asRanges()) {
            if (!encloses(range)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean intersects(Range<C> otherRange) {
        Objects.requireNonNull(otherRange, "otherRange must not be null");
        if (otherRange.isEmpty()) {
            return false;
        }
        if (!otherRange.hasLowerBound()) {
            // Unbounded lower — linear scan required
            for (Range<C> r : ranges) {
                if (r.isConnected(otherRange)) {
                    Range<C> inter = r.intersection(otherRange);
                    if (!inter.isEmpty()) return true;
                }
            }
            return false;
        }
        int index = findCandidateIndex(otherRange.lowerEndpoint());
        // Check candidate and the range after it
        for (int i = Math.max(0, index); i < ranges.size(); i++) {
            Range<C> r = ranges.get(i);
            if (r.hasLowerBound() && otherRange.hasUpperBound()) {
                if (r.lowerEndpoint().compareTo(otherRange.upperEndpoint()) > 0) {
                    break;
                }
            }
            if (r.isConnected(otherRange)) {
                Range<C> intersection = r.intersection(otherRange);
                if (!intersection.isEmpty()) {
                    return true;
                }
            }
        }
        // Also check the range before the candidate
        if (index > 0) {
            Range<C> before = ranges.get(index - 1);
            if (before.isConnected(otherRange)) {
                Range<C> intersection = before.intersection(otherRange);
                if (!intersection.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return ranges.isEmpty();
    }

    @Override
    public Set<Range<C>> asRanges() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(ranges));
    }

    @Override
    public Set<Range<C>> asDescendingSetOfRanges() {
        List<Range<C>> reversed = new ArrayList<>(ranges);
        Collections.reverse(reversed);
        return Collections.unmodifiableSet(new LinkedHashSet<>(reversed));
    }

    @Override
    public RangeSet<C> complement() {
        if (isEmpty()) {
            // Complement of empty set is all values
            return new ImmutableRangeSet<>(List.of(Range.<C>all()));
        }

        List<Range<C>> complementRanges = new ArrayList<>();

        // Head: (-∞, first.lowerEndpoint)
        Range<C> first = ranges.getFirst();
        if (first.hasLowerBound()) {
            if (first.isLowerBoundClosed()) {
                complementRanges.add(Range.lessThan(first.lowerEndpoint()));
            } else {
                complementRanges.add(Range.atMost(first.lowerEndpoint()));
            }
        }

        // Gaps between consecutive ranges
        for (int i = 0; i < ranges.size() - 1; i++) {
            Range<C> current = ranges.get(i);
            Range<C> next = ranges.get(i + 1);

            Range.BoundType lowerType = current.upperBoundType() == Range.BoundType.CLOSED
                    ? Range.BoundType.OPEN : Range.BoundType.CLOSED;
            Range.BoundType upperType = next.lowerBoundType() == Range.BoundType.CLOSED
                    ? Range.BoundType.OPEN : Range.BoundType.CLOSED;

            Range<C> gap = Range.create(lowerType, current.upperEndpoint(),
                    upperType, next.lowerEndpoint());
            if (!gap.isEmpty()) {
                complementRanges.add(gap);
            }
        }

        // Tail: (last.upperEndpoint, +∞)
        Range<C> last = ranges.getLast();
        if (last.hasUpperBound()) {
            if (last.isUpperBoundClosed()) {
                complementRanges.add(Range.greaterThan(last.upperEndpoint()));
            } else {
                complementRanges.add(Range.atLeast(last.upperEndpoint()));
            }
        }

        return new ImmutableRangeSet<>(complementRanges);
    }

    @Override
    public RangeSet<C> subRangeSet(Range<C> view) {
        Objects.requireNonNull(view, "view must not be null");
        if (view.isEmpty() || isEmpty()) {
            return of();
        }

        List<Range<C>> result = new ArrayList<>();
        for (Range<C> range : ranges) {
            if (range.isConnected(view)) {
                Range<C> intersection = range.intersection(view);
                if (!intersection.isEmpty()) {
                    result.add(intersection);
                }
            }
        }

        return new ImmutableRangeSet<>(result);
    }

    @Override
    public Range<C> span() {
        if (isEmpty()) {
            throw new NoSuchElementException("RangeSet is empty");
        }
        Range<C> first = ranges.getFirst();
        Range<C> last = ranges.getLast();
        return first.span(last);
    }

    // ==================== 变更方法（不支持）| Mutation Methods (Unsupported) ====================

    /**
     * Not supported. Always throws {@link UnsupportedOperationException}.
     * 不支持。始终抛出 {@link UnsupportedOperationException}。
     *
     * @param range the range | 范围
     * @throws UnsupportedOperationException always | 始终抛出
     */
    @Override
    public void add(Range<C> range) {
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
    public void remove(Range<C> range) {
        throw new UnsupportedOperationException(MUTATION_MSG);
    }

    /**
     * Not supported. Always throws {@link UnsupportedOperationException}.
     * 不支持。始终抛出 {@link UnsupportedOperationException}。
     *
     * @param other the other range set | 另一个范围集合
     * @throws UnsupportedOperationException always | 始终抛出
     */
    @Override
    public void addAll(RangeSet<C> other) {
        throw new UnsupportedOperationException(MUTATION_MSG);
    }

    /**
     * Not supported. Always throws {@link UnsupportedOperationException}.
     * 不支持。始终抛出 {@link UnsupportedOperationException}。
     *
     * @param other the other range set | 另一个范围集合
     * @throws UnsupportedOperationException always | 始终抛出
     */
    @Override
    public void removeAll(RangeSet<C> other) {
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

    // ==================== 集合操作 | Set Operations ====================

    /**
     * Return a new immutable range set representing the union of this set and the other.
     * 返回表示此集合与另一个集合并集的新不可变范围集合。
     *
     * @param other the other immutable range set | 另一个不可变范围集合
     * @return union of both sets | 两个集合的并集
     */
    public ImmutableRangeSet<C> union(ImmutableRangeSet<C> other) {
        Objects.requireNonNull(other, "other must not be null");
        if (isEmpty()) {
            return other;
        }
        if (other.isEmpty()) {
            return this;
        }
        TreeRangeSet<C> result = TreeRangeSet.create();
        for (Range<C> range : this.ranges) {
            result.add(range);
        }
        for (Range<C> range : other.ranges) {
            result.add(range);
        }
        return new ImmutableRangeSet<>(new ArrayList<>(result.asRanges()));
    }

    /**
     * Return a new immutable range set representing the intersection of this set and the other.
     * 返回表示此集合与另一个集合交集的新不可变范围集合。
     *
     * @param other the other immutable range set | 另一个不可变范围集合
     * @return intersection of both sets | 两个集合的交集
     */
    public ImmutableRangeSet<C> intersection(ImmutableRangeSet<C> other) {
        Objects.requireNonNull(other, "other must not be null");
        if (isEmpty() || other.isEmpty()) {
            return of();
        }
        List<Range<C>> result = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < this.ranges.size() && j < other.ranges.size()) {
            Range<C> a = this.ranges.get(i);
            Range<C> b = other.ranges.get(j);
            if (a.isConnected(b)) {
                Range<C> inter = a.intersection(b);
                if (!inter.isEmpty()) {
                    result.add(inter);
                }
            }
            // Advance the range with the smaller upper endpoint
            if (compareUpperBound(a, b) <= 0) {
                i++;
            } else {
                j++;
            }
        }
        if (result.isEmpty()) return of();
        TreeRangeSet<C> coalesced = TreeRangeSet.create();
        for (Range<C> r : result) {
            coalesced.add(r);
        }
        return new ImmutableRangeSet<>(new ArrayList<>(coalesced.asRanges()));
    }

    /**
     * Return a new immutable range set representing the difference (this minus other).
     * 返回表示差集（此集合减去另一个集合）的新不可变范围集合。
     *
     * @param other the other immutable range set | 另一个不可变范围集合
     * @return difference of both sets | 两个集合的差集
     */
    public ImmutableRangeSet<C> difference(ImmutableRangeSet<C> other) {
        Objects.requireNonNull(other, "other must not be null");
        if (isEmpty() || other.isEmpty()) {
            return this;
        }
        TreeRangeSet<C> result = TreeRangeSet.create();
        for (Range<C> range : this.ranges) {
            result.add(range);
        }
        for (Range<C> range : other.ranges) {
            result.remove(range);
        }
        if (result.isEmpty()) {
            return of();
        }
        return new ImmutableRangeSet<>(new ArrayList<>(result.asRanges()));
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
        return ranges.toString();
    }

    // ==================== 内部方法 | Internal Methods ====================

    /**
     * Binary search to find the index of the range whose lower endpoint is less than or equal to the value.
     * 二分搜索查找下端点小于或等于该值的范围索引。
     */
    private int findCandidateIndex(C value) {
        int lo = 0;
        int hi = ranges.size() - 1;
        int result = -1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            Range<C> midRange = ranges.get(mid);
            if (!midRange.hasLowerBound() || midRange.lowerEndpoint().compareTo(value) <= 0) {
                result = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return result;
    }

    /**
     * Compare upper bounds of two ranges. Returns negative if a's upper is less than b's.
     */
    private static <C extends Comparable<? super C>> int compareUpperBound(Range<C> a, Range<C> b) {
        if (!a.hasUpperBound()) return 1;
        if (!b.hasUpperBound()) return -1;
        int cmp = a.upperEndpoint().compareTo(b.upperEndpoint());
        if (cmp != 0) return cmp;
        // Both closed < one open
        if (a.upperBoundType() == Range.BoundType.CLOSED && b.upperBoundType() == Range.BoundType.OPEN) {
            return -1;
        }
        if (a.upperBoundType() == Range.BoundType.OPEN && b.upperBoundType() == Range.BoundType.CLOSED) {
            return 1;
        }
        return 0;
    }

    // ==================== 构建器 | Builder ====================

    /**
     * Builder for {@link ImmutableRangeSet}.
     * {@link ImmutableRangeSet} 的构建器。
     *
     * <p>Uses a {@link TreeRangeSet} internally for automatic range coalescing.</p>
     * <p>内部使用 {@link TreeRangeSet} 自动合并范围。</p>
     *
     * @param <C> endpoint type | 端点类型
     */
    public static final class Builder<C extends Comparable<? super C>> {

        private final TreeRangeSet<C> rangeSet = TreeRangeSet.create();

        private Builder() {
        }

        /**
         * Add a range to the builder.
         * 向构建器添加范围。
         *
         * @param range the range to add | 要添加的范围
         * @return this builder | 此构建器
         */
        public Builder<C> add(Range<C> range) {
            Objects.requireNonNull(range, "range must not be null");
            rangeSet.add(range);
            return this;
        }

        /**
         * Add all ranges from another range set.
         * 从另一个范围集合添加所有范围。
         *
         * @param other the other range set | 另一个范围集合
         * @return this builder | 此构建器
         */
        public Builder<C> addAll(RangeSet<C> other) {
            Objects.requireNonNull(other, "other must not be null");
            rangeSet.addAll(other);
            return this;
        }

        /**
         * Build the immutable range set.
         * 构建不可变范围集合。
         *
         * @return immutable range set | 不可变范围集合
         */
        public ImmutableRangeSet<C> build() {
            if (rangeSet.isEmpty()) {
                return of();
            }
            return new ImmutableRangeSet<>(new ArrayList<>(rangeSet.asRanges()));
        }
    }
}
