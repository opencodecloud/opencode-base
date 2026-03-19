package cloud.opencode.base.collections;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Range - A contiguous range of Comparable values
 * Range - 可比较值的连续范围
 *
 * <p>Represents a contiguous range of values with configurable endpoint behavior
 * (open or closed). Immutable and thread-safe.</p>
 * <p>表示具有可配置端点行为（开放或关闭）的连续值范围。不可变且线程安全。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Open/closed endpoints - 开放/关闭端点</li>
 *   <li>Range operations (contains, intersection, union) - 范围操作（包含、交集、并集）</li>
 *   <li>Immutable - 不可变</li>
 *   <li>Thread-safe - 线程安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Closed range [1, 10] - 闭区间
 * Range<Integer> closed = Range.closed(1, 10);
 *
 * // Open range (1, 10) - 开区间
 * Range<Integer> open = Range.open(1, 10);
 *
 * // Half-open [1, 10) - 半开区间
 * Range<Integer> closedOpen = Range.closedOpen(1, 10);
 *
 * // Check contains - 检查包含
 * boolean contains = Range.closed(1, 10).contains(5);  // true
 *
 * // Check intersection - 检查交集
 * boolean intersects = Range.closed(1, 5).isConnected(Range.closed(3, 8));
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>contains: O(1) - contains: O(1)</li>
 *   <li>intersection: O(1) - intersection: O(1)</li>
 *   <li>encloses: O(1) - encloses: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: No (nulls not allowed) - 空值安全: 否（不允许空值）</li>
 * </ul>
 *
 * @param <C> Comparable type | 可比较类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class Range<C extends Comparable<? super C>> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Cut<C> lowerBound;
    private final Cut<C> upperBound;

    // ==================== 构造方法 | Constructors ====================

    private Range(Cut<C> lowerBound, Cut<C> upperBound) {
        this.lowerBound = Objects.requireNonNull(lowerBound);
        this.upperBound = Objects.requireNonNull(upperBound);
        if (lowerBound.compareTo(upperBound) > 0) {
            throw new IllegalArgumentException("Invalid range: " + toString(lowerBound, upperBound));
        }
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create a closed range [lower, upper].
     * 创建闭区间 [lower, upper]。
     *
     * @param <C>   Comparable type | 可比较类型
     * @param lower lower endpoint | 下端点
     * @param upper upper endpoint | 上端点
     * @return closed range | 闭区间
     */
    public static <C extends Comparable<? super C>> Range<C> closed(C lower, C upper) {
        return new Range<>(Cut.belowValue(lower), Cut.aboveValue(upper));
    }

    /**
     * Create an open range (lower, upper).
     * 创建开区间 (lower, upper)。
     *
     * @param <C>   Comparable type | 可比较类型
     * @param lower lower endpoint | 下端点
     * @param upper upper endpoint | 上端点
     * @return open range | 开区间
     */
    public static <C extends Comparable<? super C>> Range<C> open(C lower, C upper) {
        return new Range<>(Cut.aboveValue(lower), Cut.belowValue(upper));
    }

    /**
     * Create a closed-open range [lower, upper).
     * 创建左闭右开区间 [lower, upper)。
     *
     * @param <C>   Comparable type | 可比较类型
     * @param lower lower endpoint | 下端点
     * @param upper upper endpoint | 上端点
     * @return closed-open range | 左闭右开区间
     */
    public static <C extends Comparable<? super C>> Range<C> closedOpen(C lower, C upper) {
        return new Range<>(Cut.belowValue(lower), Cut.belowValue(upper));
    }

    /**
     * Create an open-closed range (lower, upper].
     * 创建左开右闭区间 (lower, upper]。
     *
     * @param <C>   Comparable type | 可比较类型
     * @param lower lower endpoint | 下端点
     * @param upper upper endpoint | 上端点
     * @return open-closed range | 左开右闭区间
     */
    public static <C extends Comparable<? super C>> Range<C> openClosed(C lower, C upper) {
        return new Range<>(Cut.aboveValue(lower), Cut.aboveValue(upper));
    }

    /**
     * Create a range with no lower bound (-∞, upper].
     * 创建无下界的范围 (-∞, upper]。
     *
     * @param <C>   Comparable type | 可比较类型
     * @param upper upper endpoint | 上端点
     * @return range | 范围
     */
    public static <C extends Comparable<? super C>> Range<C> atMost(C upper) {
        return new Range<>(Cut.belowAll(), Cut.aboveValue(upper));
    }

    /**
     * Create a range with no lower bound (-∞, upper).
     * 创建无下界的范围 (-∞, upper)。
     *
     * @param <C>   Comparable type | 可比较类型
     * @param upper upper endpoint | 上端点
     * @return range | 范围
     */
    public static <C extends Comparable<? super C>> Range<C> lessThan(C upper) {
        return new Range<>(Cut.belowAll(), Cut.belowValue(upper));
    }

    /**
     * Create a range with no upper bound [lower, +∞).
     * 创建无上界的范围 [lower, +∞)。
     *
     * @param <C>   Comparable type | 可比较类型
     * @param lower lower endpoint | 下端点
     * @return range | 范围
     */
    public static <C extends Comparable<? super C>> Range<C> atLeast(C lower) {
        return new Range<>(Cut.belowValue(lower), Cut.aboveAll());
    }

    /**
     * Create a range with no upper bound (lower, +∞).
     * 创建无上界的范围 (lower, +∞)。
     *
     * @param <C>   Comparable type | 可比较类型
     * @param lower lower endpoint | 下端点
     * @return range | 范围
     */
    public static <C extends Comparable<? super C>> Range<C> greaterThan(C lower) {
        return new Range<>(Cut.aboveValue(lower), Cut.aboveAll());
    }

    /**
     * Create a range containing all values (-∞, +∞).
     * 创建包含所有值的范围 (-∞, +∞)。
     *
     * @param <C> Comparable type | 可比较类型
     * @return all range | 全范围
     */
    public static <C extends Comparable<? super C>> Range<C> all() {
        return new Range<>(Cut.<C>belowAll(), Cut.<C>aboveAll());
    }

    /**
     * Create a singleton range [value, value].
     * 创建单值范围 [value, value]。
     *
     * @param <C>   Comparable type | 可比较类型
     * @param value the value | 值
     * @return singleton range | 单值范围
     */
    public static <C extends Comparable<? super C>> Range<C> singleton(C value) {
        return closed(value, value);
    }

    /**
     * Create a range with explicit bound types.
     * 使用显式边界类型创建范围。
     *
     * @param <C>            Comparable type | 可比较类型
     * @param lowerBoundType lower bound type | 下界类型
     * @param lower          lower endpoint | 下端点
     * @param upperBoundType upper bound type | 上界类型
     * @param upper          upper endpoint | 上端点
     * @return the range | 范围
     */
    public static <C extends Comparable<? super C>> Range<C> create(
            BoundType lowerBoundType, C lower, BoundType upperBoundType, C upper) {
        Cut<C> lowerCut = (lowerBoundType == BoundType.CLOSED) ? Cut.belowValue(lower) : Cut.aboveValue(lower);
        Cut<C> upperCut = (upperBoundType == BoundType.CLOSED) ? Cut.aboveValue(upper) : Cut.belowValue(upper);
        return new Range<>(lowerCut, upperCut);
    }

    // ==================== 查询方法 | Query Methods ====================

    /**
     * Check if value is contained in this range.
     * 检查值是否在此范围内。
     *
     * @param value the value | 值
     * @return true if contains | 如果包含则返回 true
     */
    public boolean contains(C value) {
        Objects.requireNonNull(value);
        return lowerBound.isLessThan(value) && !upperBound.isLessThan(value);
    }

    /**
     * Check if this range has a lower endpoint.
     * 检查此范围是否有下端点。
     *
     * @return true if has lower | 如果有下端点则返回 true
     */
    public boolean hasLowerBound() {
        return lowerBound != Cut.belowAll();
    }

    /**
     * Check if this range has an upper endpoint.
     * 检查此范围是否有上端点。
     *
     * @return true if has upper | 如果有上端点则返回 true
     */
    public boolean hasUpperBound() {
        return upperBound != Cut.aboveAll();
    }

    /**
     * Return the lower endpoint.
     * 返回下端点。
     *
     * @return lower endpoint | 下端点
     * @throws IllegalStateException if no lower bound | 如果无下界
     */
    public C lowerEndpoint() {
        if (!hasLowerBound()) {
            throw new IllegalStateException("Range has no lower bound");
        }
        return lowerBound.endpoint();
    }

    /**
     * Return the upper endpoint.
     * 返回上端点。
     *
     * @return upper endpoint | 上端点
     * @throws IllegalStateException if no upper bound | 如果无上界
     */
    public C upperEndpoint() {
        if (!hasUpperBound()) {
            throw new IllegalStateException("Range has no upper bound");
        }
        return upperBound.endpoint();
    }

    /**
     * Check if lower bound is closed.
     * 检查下界是否闭合。
     *
     * @return true if closed | 如果闭合则返回 true
     */
    public boolean isLowerBoundClosed() {
        return hasLowerBound() && lowerBound.typeAsLowerBound() == BoundType.CLOSED;
    }

    /**
     * Check if upper bound is closed.
     * 检查上界是否闭合。
     *
     * @return true if closed | 如果闭合则返回 true
     */
    public boolean isUpperBoundClosed() {
        return hasUpperBound() && upperBound.typeAsUpperBound() == BoundType.CLOSED;
    }

    /**
     * Get the lower bound type.
     * 获取下界类型。
     *
     * @return lower bound type | 下界类型
     * @throws IllegalStateException if no lower bound | 如果无下界
     */
    public BoundType lowerBoundType() {
        if (!hasLowerBound()) {
            throw new IllegalStateException("Range has no lower bound");
        }
        return lowerBound.typeAsLowerBound();
    }

    /**
     * Get the upper bound type.
     * 获取上界类型。
     *
     * @return upper bound type | 上界类型
     * @throws IllegalStateException if no upper bound | 如果无上界
     */
    public BoundType upperBoundType() {
        if (!hasUpperBound()) {
            throw new IllegalStateException("Range has no upper bound");
        }
        return upperBound.typeAsUpperBound();
    }

    /**
     * Check if this range is empty.
     * 检查此范围是否为空。
     *
     * @return true if empty | 如果为空则返回 true
     */
    public boolean isEmpty() {
        return lowerBound.equals(upperBound);
    }

    // ==================== 范围操作 | Range Operations ====================

    /**
     * Check if this range encloses another range.
     * 检查此范围是否包含另一个范围。
     *
     * @param other other range | 另一个范围
     * @return true if encloses | 如果包含则返回 true
     */
    public boolean encloses(Range<C> other) {
        return lowerBound.compareTo(other.lowerBound) <= 0
                && upperBound.compareTo(other.upperBound) >= 0;
    }

    /**
     * Check if this range is connected to another (can form contiguous range).
     * 检查此范围是否与另一个连接（可以形成连续范围）。
     *
     * @param other other range | 另一个范围
     * @return true if connected | 如果连接则返回 true
     */
    public boolean isConnected(Range<C> other) {
        return lowerBound.compareTo(other.upperBound) <= 0
                && other.lowerBound.compareTo(upperBound) <= 0;
    }

    /**
     * Return the intersection with another range.
     * 返回与另一个范围的交集。
     *
     * @param other other range | 另一个范围
     * @return intersection | 交集
     * @throws IllegalArgumentException if not connected | 如果不连接
     */
    public Range<C> intersection(Range<C> other) {
        if (!isConnected(other)) {
            throw new IllegalArgumentException("Ranges are not connected: " + this + ", " + other);
        }
        Cut<C> newLower = lowerBound.compareTo(other.lowerBound) >= 0 ? lowerBound : other.lowerBound;
        Cut<C> newUpper = upperBound.compareTo(other.upperBound) <= 0 ? upperBound : other.upperBound;
        return new Range<>(newLower, newUpper);
    }

    /**
     * Return the span (smallest range enclosing both).
     * 返回跨度（包含两者的最小范围）。
     *
     * @param other other range | 另一个范围
     * @return span | 跨度
     */
    public Range<C> span(Range<C> other) {
        Cut<C> newLower = lowerBound.compareTo(other.lowerBound) <= 0 ? lowerBound : other.lowerBound;
        Cut<C> newUpper = upperBound.compareTo(other.upperBound) >= 0 ? upperBound : other.upperBound;
        return new Range<>(newLower, newUpper);
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Range<?> range)) return false;
        return lowerBound.equals(range.lowerBound) && upperBound.equals(range.upperBound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowerBound, upperBound);
    }

    @Override
    public String toString() {
        return toString(lowerBound, upperBound);
    }

    private String toString(Cut<C> lower, Cut<C> upper) {
        StringBuilder sb = new StringBuilder();
        lower.describeAsLowerBound(sb);
        sb.append("..");
        upper.describeAsUpperBound(sb);
        return sb.toString();
    }

    // ==================== 内部类 | Internal Classes ====================

    /**
     * Bound type enum
     */
    public enum BoundType {
        OPEN,
        CLOSED
    }

    /**
     * Cut - represents an endpoint
     */
    private static sealed abstract class Cut<C extends Comparable<? super C>>
            implements Comparable<Cut<C>>, Serializable
            permits BelowAll, AboveAll, BelowValue, AboveValue {
        @Serial
        private static final long serialVersionUID = 1L;

        abstract C endpoint();
        abstract boolean isLessThan(C value);
        abstract BoundType typeAsLowerBound();
        abstract BoundType typeAsUpperBound();
        abstract void describeAsLowerBound(StringBuilder sb);
        abstract void describeAsUpperBound(StringBuilder sb);

        @SuppressWarnings("unchecked")
        static <C extends Comparable<? super C>> Cut<C> belowAll() {
            return (Cut<C>) BelowAll.INSTANCE;
        }

        @SuppressWarnings("unchecked")
        static <C extends Comparable<? super C>> Cut<C> aboveAll() {
            return (Cut<C>) AboveAll.INSTANCE;
        }

        static <C extends Comparable<? super C>> Cut<C> belowValue(C value) {
            return new BelowValue<>(value);
        }

        static <C extends Comparable<? super C>> Cut<C> aboveValue(C value) {
            return new AboveValue<>(value);
        }
    }

    private static final class BelowAll<C extends Comparable<? super C>> extends Cut<C> {
        static final BelowAll<?> INSTANCE = new BelowAll<>();

        @Override C endpoint() { throw new IllegalStateException(); }
        @Override boolean isLessThan(C value) { return true; }
        @Override BoundType typeAsLowerBound() { throw new IllegalStateException(); }
        @Override BoundType typeAsUpperBound() { throw new AssertionError(); }
        @Override void describeAsLowerBound(StringBuilder sb) { sb.append("(-∞"); }
        @Override void describeAsUpperBound(StringBuilder sb) { throw new AssertionError(); }

        @Override
        public int compareTo(Cut<C> o) {
            return (o == this) ? 0 : -1;
        }
    }

    private static final class AboveAll<C extends Comparable<? super C>> extends Cut<C> {
        static final AboveAll<?> INSTANCE = new AboveAll<>();

        @Override C endpoint() { throw new IllegalStateException(); }
        @Override boolean isLessThan(C value) { return false; }
        @Override BoundType typeAsLowerBound() { throw new AssertionError(); }
        @Override BoundType typeAsUpperBound() { throw new IllegalStateException(); }
        @Override void describeAsLowerBound(StringBuilder sb) { throw new AssertionError(); }
        @Override void describeAsUpperBound(StringBuilder sb) { sb.append("+∞)"); }

        @Override
        public int compareTo(Cut<C> o) {
            return (o == this) ? 0 : 1;
        }
    }

    private static final class BelowValue<C extends Comparable<? super C>> extends Cut<C> {
        private final C endpoint;

        BelowValue(C endpoint) {
            this.endpoint = Objects.requireNonNull(endpoint);
        }

        @Override C endpoint() { return endpoint; }
        @Override boolean isLessThan(C value) { return endpoint.compareTo(value) <= 0; }
        @Override BoundType typeAsLowerBound() { return BoundType.CLOSED; }
        @Override BoundType typeAsUpperBound() { return BoundType.OPEN; }
        @Override void describeAsLowerBound(StringBuilder sb) { sb.append('[').append(endpoint); }
        @Override void describeAsUpperBound(StringBuilder sb) { sb.append(endpoint).append(')'); }

        @Override
        public int compareTo(Cut<C> o) {
            if (o == Cut.belowAll()) return 1;
            if (o == Cut.aboveAll()) return -1;
            int cmp = endpoint.compareTo(o.endpoint());
            if (cmp != 0) return cmp;
            return (o instanceof BelowValue) ? 0 : -1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BelowValue<?> that)) return false;
            return endpoint.equals(that.endpoint);
        }

        @Override
        public int hashCode() {
            return endpoint.hashCode();
        }
    }

    private static final class AboveValue<C extends Comparable<? super C>> extends Cut<C> {
        private final C endpoint;

        AboveValue(C endpoint) {
            this.endpoint = Objects.requireNonNull(endpoint);
        }

        @Override C endpoint() { return endpoint; }
        @Override boolean isLessThan(C value) { return endpoint.compareTo(value) < 0; }
        @Override BoundType typeAsLowerBound() { return BoundType.OPEN; }
        @Override BoundType typeAsUpperBound() { return BoundType.CLOSED; }
        @Override void describeAsLowerBound(StringBuilder sb) { sb.append('(').append(endpoint); }
        @Override void describeAsUpperBound(StringBuilder sb) { sb.append(endpoint).append(']'); }

        @Override
        public int compareTo(Cut<C> o) {
            if (o == Cut.belowAll()) return 1;
            if (o == Cut.aboveAll()) return -1;
            int cmp = endpoint.compareTo(o.endpoint());
            if (cmp != 0) return cmp;
            return (o instanceof AboveValue) ? 0 : 1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AboveValue<?> that)) return false;
            return endpoint.equals(that.endpoint);
        }

        @Override
        public int hashCode() {
            return ~endpoint.hashCode();
        }
    }
}
