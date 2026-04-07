/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.core;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Range - A contiguous span of values
 * 范围 - 连续的值域
 *
 * <p>Represents a range of comparable values with configurable bounds.</p>
 * <p>表示具有可配置边界的可比较值的范围。</p>
 *
 * <p><strong>Bound Types | 边界类型:</strong></p>
 * <ul>
 *   <li>[a, b] - closed (includes both endpoints) | 闭区间（包含两端点）</li>
 *   <li>(a, b) - open (excludes both endpoints) | 开区间（不包含两端点）</li>
 *   <li>[a, b) - closedOpen (includes lower, excludes upper) | 左闭右开</li>
 *   <li>(a, b] - openClosed (excludes lower, includes upper) | 左开右闭</li>
 *   <li>[a, +∞) - atLeast (includes lower, no upper) | 大于等于</li>
 *   <li>(a, +∞) - greaterThan (excludes lower, no upper) | 大于</li>
 *   <li>(-∞, b] - atMost (no lower, includes upper) | 小于等于</li>
 *   <li>(-∞, b) - lessThan (no lower, excludes upper) | 小于</li>
 *   <li>(-∞, +∞) - all (no bounds) | 全部</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create ranges
 * Range<Integer> closed = Range.closed(1, 10);      // [1, 10]
 * Range<Integer> open = Range.open(1, 10);          // (1, 10)
 * Range<Integer> atLeast = Range.atLeast(5);        // [5, +∞)
 * Range<Integer> lessThan = Range.lessThan(100);    // (-∞, 100)
 *
 * // Check containment
 * closed.contains(5);       // true
 * closed.contains(0);       // false
 *
 * // Stream filtering | 流过滤
 * Range<Integer> range = Range.closed(1, 100);
 * List<Integer> inRange = numbers.stream().filter(range).toList();
 *
 * // Check enclosure
 * Range.closed(1, 10).encloses(Range.closed(3, 7));  // true
 *
 * // Intersection and span
 * Range<Integer> intersection = closed.intersection(Range.closed(5, 15));  // [5, 10]
 * Range<Integer> span = closed.span(Range.closed(15, 20));  // [1, 20]
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Open, closed, and half-open intervals - 开区间、闭区间和半开区间</li>
 *   <li>Containment checking - 包含检查</li>
 *   <li>Intersection, union, and span operations - 交集、并集和跨度操作</li>
 *   <li>Stream support for integer ranges - 整数范围的流支持</li>
 *   <li>Predicate support for stream filtering (test) - 支持流过滤的 Predicate</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after creation) - 线程安全: 是（创建后不可变）</li>
 *   <li>Null-safe: No, endpoints must not be null - 空值安全: 否，端点不可为null</li>
 * </ul>
 *
 * @param <C> the comparable type - 可比较类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class Range<C extends Comparable<? super C>> implements Predicate<C> {

    private final Bound<C> lowerBound;
    private final Bound<C> upperBound;

    private Range(Bound<C> lowerBound, Bound<C> upperBound) {
        this.lowerBound = Objects.requireNonNull(lowerBound);
        this.upperBound = Objects.requireNonNull(upperBound);

        // Validate bounds
        if (lowerBound.endpoint() != null && upperBound.endpoint() != null) {
            int cmp = lowerBound.endpoint().compareTo(upperBound.endpoint());
            if (cmp > 0) {
                throw new IllegalArgumentException("Lower bound must be <= upper bound");
            }
            if (cmp == 0 && (lowerBound.type() == BoundType.OPEN || upperBound.type() == BoundType.OPEN)) {
                throw new IllegalArgumentException("Range cannot have equal open bounds");
            }
        }
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a closed range [lower, upper].
     * 创建闭区间 [lower, upper]。
     */
    public static <C extends Comparable<? super C>> Range<C> closed(C lower, C upper) {
        return new Range<>(Bound.closed(lower), Bound.closed(upper));
    }

    /**
     * Creates an open range (lower, upper).
     * 创建开区间 (lower, upper)。
     */
    public static <C extends Comparable<? super C>> Range<C> open(C lower, C upper) {
        return new Range<>(Bound.open(lower), Bound.open(upper));
    }

    /**
     * Creates a closed-open range [lower, upper).
     * 创建左闭右开区间 [lower, upper)。
     */
    public static <C extends Comparable<? super C>> Range<C> closedOpen(C lower, C upper) {
        return new Range<>(Bound.closed(lower), Bound.open(upper));
    }

    /**
     * Creates an open-closed range (lower, upper].
     * 创建左开右闭区间 (lower, upper]。
     */
    public static <C extends Comparable<? super C>> Range<C> openClosed(C lower, C upper) {
        return new Range<>(Bound.open(lower), Bound.closed(upper));
    }

    /**
     * Creates a range with no lower bound (-∞, upper].
     * 创建无下界范围 (-∞, upper]。
     */
    public static <C extends Comparable<? super C>> Range<C> atMost(C upper) {
        return new Range<>(Bound.unbounded(), Bound.closed(upper));
    }

    /**
     * Creates a range with no lower bound (-∞, upper).
     * 创建无下界范围 (-∞, upper)。
     */
    public static <C extends Comparable<? super C>> Range<C> lessThan(C upper) {
        return new Range<>(Bound.unbounded(), Bound.open(upper));
    }

    /**
     * Creates a range with no upper bound [lower, +∞).
     * 创建无上界范围 [lower, +∞)。
     */
    public static <C extends Comparable<? super C>> Range<C> atLeast(C lower) {
        return new Range<>(Bound.closed(lower), Bound.unbounded());
    }

    /**
     * Creates a range with no upper bound (lower, +∞).
     * 创建无上界范围 (lower, +∞)。
     */
    public static <C extends Comparable<? super C>> Range<C> greaterThan(C lower) {
        return new Range<>(Bound.open(lower), Bound.unbounded());
    }

    /**
     * Creates a range containing all values (-∞, +∞).
     * 创建包含所有值的范围 (-∞, +∞)。
     */
    @SuppressWarnings("unchecked")
    public static <C extends Comparable<? super C>> Range<C> all() {
        Bound<C> lower = (Bound<C>) Bound.unbounded();
        Bound<C> upper = (Bound<C>) Bound.unbounded();
        return new Range<>(lower, upper);
    }

    /**
     * Creates a singleton range [value, value].
     * 创建单值范围 [value, value]。
     */
    public static <C extends Comparable<? super C>> Range<C> singleton(C value) {
        return closed(value, value);
    }

    /**
     * Creates the minimal range enclosing all given values.
     * 创建包含所有给定值的最小范围。
     */
    @SafeVarargs
    public static <C extends Comparable<? super C>> Range<C> encloseAll(C... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Must provide at least one value");
        }
        C min = values[0];
        C max = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i].compareTo(min) < 0) min = values[i];
            if (values[i].compareTo(max) > 0) max = values[i];
        }
        return closed(min, max);
    }

    /**
     * Creates the minimal range enclosing all values in the iterable.
     * 创建包含可迭代对象中所有值的最小范围。
     */
    public static <C extends Comparable<? super C>> Range<C> encloseAll(Iterable<C> values) {
        Iterator<C> iter = values.iterator();
        if (!iter.hasNext()) {
            throw new IllegalArgumentException("Must provide at least one value");
        }
        C min = iter.next();
        C max = min;
        while (iter.hasNext()) {
            C value = iter.next();
            if (value.compareTo(min) < 0) min = value;
            if (value.compareTo(max) > 0) max = value;
        }
        return closed(min, max);
    }

    // ==================== Query Methods | 查询方法 ====================

    /**
     * Returns true if this range has a lower bound.
     * 如果此范围有下界返回 true。
     */
    public boolean hasLowerBound() {
        return lowerBound.endpoint() != null;
    }

    /**
     * Returns true if this range has an upper bound.
     * 如果此范围有上界返回 true。
     */
    public boolean hasUpperBound() {
        return upperBound.endpoint() != null;
    }

    /**
     * Returns the lower endpoint if present.
     * 返回下界端点（如果存在）。
     */
    public Optional<C> lowerEndpoint() {
        return Optional.ofNullable(lowerBound.endpoint());
    }

    /**
     * Returns the upper endpoint if present.
     * 返回上界端点（如果存在）。
     */
    public Optional<C> upperEndpoint() {
        return Optional.ofNullable(upperBound.endpoint());
    }

    /**
     * Returns the lower bound type.
     * 返回下界类型。
     */
    public BoundType lowerBoundType() {
        return lowerBound.type();
    }

    /**
     * Returns the upper bound type.
     * 返回上界类型。
     */
    public BoundType upperBoundType() {
        return upperBound.type();
    }

    /**
     * Returns true if this range is empty.
     * 如果此范围为空返回 true。
     */
    public boolean isEmpty() {
        if (lowerBound.endpoint() == null || upperBound.endpoint() == null) {
            return false;
        }
        int cmp = lowerBound.endpoint().compareTo(upperBound.endpoint());
        return cmp == 0 && (lowerBound.type() == BoundType.OPEN || upperBound.type() == BoundType.OPEN);
    }

    // ==================== Containment Methods | 包含方法 ====================

    /**
     * Returns true if this range contains the given value.
     * 如果此范围包含给定值返回 true。
     */
    public boolean contains(C value) {
        Objects.requireNonNull(value);
        return lowerBound.isBelow(value) && upperBound.isAbove(value);
    }

    /**
     * Tests if the given value is contained in this range (Predicate support).
     * 测试给定值是否包含在此范围内（Predicate 支持）。
     *
     * <p>Enables using Range directly in stream filters and other Predicate-accepting APIs:</p>
     * <p>使 Range 可以直接用于流过滤和其他接受 Predicate 的 API：</p>
     * <pre>{@code
     * Range<Integer> range = Range.closed(1, 10);
     * List<Integer> filtered = list.stream().filter(range).toList();
     * }</pre>
     *
     * @param value the value to test | 待测试的值
     * @return true if the value is contained in this range | 如果值在范围内返回 true
     */
    @Override
    public boolean test(C value) {
        return contains(value);
    }

    /**
     * Returns true if this range contains all given values.
     * 如果此范围包含所有给定值返回 true。
     */
    @SafeVarargs
    public final boolean containsAll(C... values) {
        for (C value : values) {
            if (!contains(value)) return false;
        }
        return true;
    }

    /**
     * Returns true if this range contains all values in the iterable.
     * 如果此范围包含可迭代对象中的所有值返回 true。
     */
    public boolean containsAll(Iterable<C> values) {
        for (C value : values) {
            if (!contains(value)) return false;
        }
        return true;
    }

    /**
     * Returns true if this range encloses another range.
     * 如果此范围包含另一个范围返回 true。
     */
    public boolean encloses(Range<C> other) {
        return lowerBound.compareTo(other.lowerBound) <= 0
                && upperBound.compareAsUpperBound(other.upperBound) >= 0;
    }

    /**
     * Returns true if this range is connected to another range.
     * 如果此范围与另一个范围相连返回 true。
     */
    public boolean isConnected(Range<C> other) {
        return compareLowerToUpper(lowerBound, other.upperBound) <= 0
                && compareLowerToUpper(other.lowerBound, upperBound) <= 0;
    }

    /**
     * Compare a lower bound against an upper bound for connectivity check.
     * 比较下界与上界（用于连通性检查）。
     *
     * <p>Unbounded lower (-∞) is always &le; any upper bound,
     * and unbounded upper (+∞) is always &ge; any lower bound.</p>
     */
    private static <C extends Comparable<? super C>> int compareLowerToUpper(
            Bound<C> lower, Bound<C> upper) {
        if (lower.endpoint() == null || upper.endpoint() == null) {
            return -1; // -∞ ≤ anything, or anything ≤ +∞
        }
        int cmp = lower.endpoint().compareTo(upper.endpoint());
        if (cmp != 0) return cmp;
        // Same endpoint: closed+closed → 0 (connected), any open → 1 (gap)
        return (lower.type() == BoundType.OPEN || upper.type() == BoundType.OPEN) ? 1 : 0;
    }

    // ==================== Operations | 操作 ====================

    /**
     * Returns the intersection of this range with another.
     * 返回此范围与另一个范围的交集。
     */
    public Range<C> intersection(Range<C> other) {
        if (!isConnected(other)) {
            throw new IllegalArgumentException("Ranges are not connected: " + this + " and " + other);
        }

        Bound<C> newLower = lowerBound.compareTo(other.lowerBound) >= 0 ? lowerBound : other.lowerBound;
        Bound<C> newUpper = upperBound.compareAsUpperBound(other.upperBound) <= 0 ? upperBound : other.upperBound;

        return new Range<>(newLower, newUpper);
    }

    /**
     * Returns the minimal range enclosing both this range and another.
     * 返回包含此范围和另一个范围的最小范围。
     */
    public Range<C> span(Range<C> other) {
        Bound<C> newLower = lowerBound.compareTo(other.lowerBound) <= 0 ? lowerBound : other.lowerBound;
        Bound<C> newUpper = upperBound.compareAsUpperBound(other.upperBound) >= 0 ? upperBound : other.upperBound;
        return new Range<>(newLower, newUpper);
    }

    /**
     * Returns the gap between this range and another, if any.
     * 返回此范围与另一个范围之间的间隙（如果有）。
     */
    public Optional<Range<C>> gap(Range<C> other) {
        if (isConnected(other)) {
            return Optional.empty();
        }

        Range<C> lower = compareLowerToUpper(other.lowerBound, upperBound) > 0 ? this : other;
        Range<C> higher = lower == this ? other : this;

        Bound<C> gapLower = lower.upperBound.flip();
        Bound<C> gapUpper = higher.lowerBound.flip();

        return Optional.of(new Range<>(gapLower, gapUpper));
    }

    /**
     * Returns a canonical form of this range using the given domain.
     * 使用给定域返回此范围的规范形式。
     */
    public Range<C> canonical(Function<C, C> nextValue) {
        C lower = lowerBound.endpoint();
        C upper = upperBound.endpoint();

        if (lower != null && lowerBound.type() == BoundType.OPEN) {
            lower = nextValue.apply(lower);
        }
        if (upper != null && upperBound.type() == BoundType.CLOSED) {
            upper = nextValue.apply(upper);
        }

        Bound<C> newLower = lower != null ? Bound.closed(lower) : Bound.unbounded();
        Bound<C> newUpper = upper != null ? Bound.open(upper) : Bound.unbounded();

        return new Range<>(newLower, newUpper);
    }

    // ==================== Object Methods | 对象方法 ====================

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
        StringBuilder sb = new StringBuilder();

        if (lowerBound.endpoint() == null) {
            sb.append("(-∞");
        } else {
            sb.append(lowerBound.type() == BoundType.CLOSED ? "[" : "(");
            sb.append(lowerBound.endpoint());
        }

        sb.append("..");

        if (upperBound.endpoint() == null) {
            sb.append("+∞)");
        } else {
            sb.append(upperBound.endpoint());
            sb.append(upperBound.type() == BoundType.CLOSED ? "]" : ")");
        }

        return sb.toString();
    }

    // ==================== Bound Types | 边界类型 ====================

    /**
     * Bound type enum.
     * 边界类型枚举。
     */
    public enum BoundType {
        OPEN, CLOSED
    }

    /**
     * Represents a bound with an endpoint and type.
     * 表示具有端点和类型的边界。
     */
    private record Bound<C extends Comparable<? super C>>(C endpoint, BoundType type)
            implements Comparable<Bound<C>> {

        static <C extends Comparable<? super C>> Bound<C> closed(C endpoint) {
            return new Bound<>(Objects.requireNonNull(endpoint), BoundType.CLOSED);
        }

        static <C extends Comparable<? super C>> Bound<C> open(C endpoint) {
            return new Bound<>(Objects.requireNonNull(endpoint), BoundType.OPEN);
        }

        static <C extends Comparable<? super C>> Bound<C> unbounded() {
            return new Bound<>(null, BoundType.OPEN);
        }

        boolean isBelow(C value) {
            if (endpoint == null) return true;
            int cmp = endpoint.compareTo(value);
            return type == BoundType.CLOSED ? cmp <= 0 : cmp < 0;
        }

        boolean isAbove(C value) {
            if (endpoint == null) return true;
            int cmp = endpoint.compareTo(value);
            return type == BoundType.CLOSED ? cmp >= 0 : cmp > 0;
        }

        Bound<C> flip() {
            if (endpoint == null) return unbounded();
            return type == BoundType.CLOSED ? open(endpoint) : closed(endpoint);
        }

        @Override
        public int compareTo(Bound<C> other) {
            if (endpoint == null && other.endpoint == null) return 0;
            if (endpoint == null) return -1;
            if (other.endpoint == null) return 1;

            int cmp = endpoint.compareTo(other.endpoint);
            if (cmp != 0) return cmp;

            // Same endpoint - closed < open for lower bounds
            return type == other.type ? 0 : (type == BoundType.CLOSED ? -1 : 1);
        }

        int compareAsUpperBound(Bound<C> other) {
            if (endpoint == null && other.endpoint == null) return 0;
            if (endpoint == null) return 1;
            if (other.endpoint == null) return -1;

            int cmp = endpoint.compareTo(other.endpoint);
            if (cmp != 0) return cmp;

            // Same endpoint - closed > open for upper bounds
            return type == other.type ? 0 : (type == BoundType.CLOSED ? 1 : -1);
        }
    }
}
