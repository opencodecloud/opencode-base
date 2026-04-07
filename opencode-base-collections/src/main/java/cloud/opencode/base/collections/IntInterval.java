package cloud.opencode.base.collections;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

/**
 * IntInterval - Immutable, zero-allocation integer range with O(1) random access
 * IntInterval - 零分配的不可变整数区间，支持 O(1) 随机访问
 *
 * <p>Represents a closed range of integers [from, to] with a given step,
 * similar to Python's {@code range()} but with inclusive endpoints.
 * No backing array is allocated; all values are computed on demand.</p>
 * <p>表示一个闭区间整数范围 [from, to]，具有给定步长，
 * 类似于 Python 的 {@code range()} 但端点均包含在内。
 * 不分配底层数组，所有值按需计算。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Zero-allocation: no backing array - 零分配：无底层数组</li>
 *   <li>O(1) size, get, contains - O(1) 的 size、get、contains</li>
 *   <li>Inclusive endpoints (like Eclipse Collections) - 闭区间端点（类似 Eclipse Collections）</li>
 *   <li>Auto-detected direction for ascending/descending ranges - 自动检测升序/降序方向</li>
 *   <li>IntStream integration - IntStream 集成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Ascending range - 升序区间
 * IntInterval range = IntInterval.fromTo(1, 5); // [1, 2, 3, 4, 5]
 *
 * // Descending range (auto-detected) - 降序区间（自动检测）
 * IntInterval desc = IntInterval.fromTo(5, 1); // [5, 4, 3, 2, 1]
 *
 * // Custom step - 自定义步长
 * IntInterval stepped = IntInterval.fromToBy(1, 10, 3); // [1, 4, 7, 10]
 *
 * // For-each loop - for-each 循环
 * for (int i : IntInterval.oneTo(10)) {
 *     System.out.println(i);
 * }
 *
 * // IntStream - IntStream
 * int sum = IntInterval.oneTo(100).stream().sum();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>size: O(1) - size: O(1)</li>
 *   <li>get: O(1) - get: O(1)</li>
 *   <li>contains: O(1) - contains: O(1)</li>
 *   <li>toArray / toList: O(n) - toArray / toList: O(n)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Overflow-safe: Uses long arithmetic internally - 溢出安全：内部使用 long 运算</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
public final class IntInterval implements Iterable<Integer>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int from;
    private final int to;
    private final int step;

    /**
     * Cached size (computed once in constructor using long arithmetic).
     * 缓存的大小（在构造函数中使用 long 运算计算一次）。
     */
    private final int size;

    private IntInterval(int from, int to, int step) {
        this.from = from;
        this.to = to;
        this.step = step;
        this.size = computeSize(from, to, step);
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an interval from {@code from} to {@code to} (inclusive) with auto-detected step.
     * 创建从 {@code from} 到 {@code to}（闭区间）的区间，自动检测步长。
     *
     * <p>Step is {@code 1} if {@code from <= to}, otherwise {@code -1}.</p>
     * <p>如果 {@code from <= to} 则步长为 {@code 1}，否则为 {@code -1}。</p>
     *
     * @param from the start value (inclusive) | 起始值（包含）
     * @param to   the end value (inclusive) | 结束值（包含）
     * @return the interval | 区间
     */
    public static IntInterval fromTo(int from, int to) {
        int step = from <= to ? 1 : -1;
        return new IntInterval(from, to, step);
    }

    /**
     * Create an interval from {@code from} to {@code to} (inclusive) with an explicit step.
     * 创建从 {@code from} 到 {@code to}（闭区间）的区间，使用显式步长。
     *
     * @param from the start value (inclusive) | 起始值（包含）
     * @param to   the end value (inclusive) | 结束值（包含）
     * @param step the step (must not be zero, must match direction) | 步长（不能为零，必须与方向匹配）
     * @return the interval | 区间
     * @throws IllegalArgumentException if step is zero or conflicts with direction | 如果步长为零或与方向冲突
     */
    public static IntInterval fromToBy(int from, int to, int step) {
        if (step == 0) {
            throw new IllegalArgumentException("Step must not be zero");
        }
        if (from < to && step < 0) {
            throw new IllegalArgumentException(
                    "Step must be positive when from (" + from + ") < to (" + to + "), but was " + step);
        }
        if (from > to && step > 0) {
            throw new IllegalArgumentException(
                    "Step must be negative when from (" + from + ") > to (" + to + "), but was " + step);
        }
        return new IntInterval(from, to, step);
    }

    /**
     * Create an interval from 0 to {@code to} (inclusive).
     * 创建从 0 到 {@code to}（闭区间）的区间。
     *
     * @param to the end value (inclusive) | 结束值（包含）
     * @return the interval [0..to] | 区间 [0..to]
     */
    public static IntInterval zeroTo(int to) {
        return fromTo(0, to);
    }

    /**
     * Create an interval from 1 to {@code to} (inclusive).
     * 创建从 1 到 {@code to}（闭区间）的区间。
     *
     * @param to the end value (inclusive) | 结束值（包含）
     * @return the interval [1..to] | 区间 [1..to]
     */
    public static IntInterval oneTo(int to) {
        return fromTo(1, to);
    }

    // ==================== 查询操作 | Query Operations ====================

    /**
     * Check if this interval contains the given value.
     * 检查此区间是否包含给定值。
     *
     * @param value the value to check | 要检查的值
     * @return true if the value is in this interval | 如果值在此区间中则返回 true
     */
    public boolean contains(int value) {
        if (size == 0) {
            return false;
        }
        // Check value is in range [from, to] (direction-aware)
        // 检查值是否在 [from, to] 范围内（方向感知）
        if (step > 0) {
            if (value < from || value > to) {
                return false;
            }
        } else {
            if (value > from || value < to) {
                return false;
            }
        }
        // Check alignment with step using long to avoid overflow
        // 使用 long 检查步长对齐以避免溢出
        long diff = (long) value - (long) from;
        return diff % step == 0;
    }

    /**
     * Return the number of elements in this interval.
     * 返回此区间中的元素数量。
     *
     * @return the size | 大小
     */
    public int size() {
        return size;
    }

    /**
     * Return the element at the given index (O(1)).
     * 返回给定索引处的元素 (O(1))。
     *
     * @param index the zero-based index | 从零开始的索引
     * @return the element at the index | 索引处的元素
     * @throws IndexOutOfBoundsException if index is out of bounds | 如果索引越界
     */
    public int get(int index) {
        Objects.checkIndex(index, size);
        return from + (int) ((long) step * index);
    }

    /**
     * Return the first element of this interval.
     * 返回此区间的第一个元素。
     *
     * @return the first element | 第一个元素
     * @throws NoSuchElementException if the interval is empty | 如果区间为空
     */
    public int getFirst() {
        if (size == 0) {
            throw new NoSuchElementException("Interval is empty");
        }
        return from;
    }

    /**
     * Return the last element of this interval.
     * 返回此区间的最后一个元素。
     *
     * <p>The last element is the largest value {@code v} such that
     * {@code v = from + k * step} and {@code v} does not exceed {@code to}.</p>
     * <p>最后一个元素是满足 {@code v = from + k * step} 且
     * {@code v} 不超过 {@code to} 的最大值。</p>
     *
     * @return the last element | 最后一个元素
     * @throws NoSuchElementException if the interval is empty | 如果区间为空
     */
    public int getLast() {
        if (size == 0) {
            throw new NoSuchElementException("Interval is empty");
        }
        return get(size - 1);
    }

    /**
     * Check if this interval is empty.
     * 检查此区间是否为空。
     *
     * @return true if the interval has no elements | 如果区间没有元素则返回 true
     */
    public boolean isEmpty() {
        return size == 0;
    }

    // ==================== 派生区间 | Derived Intervals ====================

    /**
     * Return a new interval with the same range but a different step.
     * 返回具有相同范围但不同步长的新区间。
     *
     * @param newStep the new step | 新步长
     * @return a new interval with the new step | 使用新步长的新区间
     * @throws IllegalArgumentException if the new step is zero or conflicts with direction | 如果新步长为零或与方向冲突
     */
    public IntInterval by(int newStep) {
        return fromToBy(from, to, newStep);
    }

    /**
     * Return a reversed interval.
     * 返回反转的区间。
     *
     * <p>The reversed interval starts at {@code getLast()} and ends at {@code from},
     * with the negated step.</p>
     * <p>反转区间从 {@code getLast()} 开始到 {@code from} 结束，步长取反。</p>
     *
     * @return the reversed interval | 反转的区间
     */
    public IntInterval reversed() {
        if (size == 0) {
            return this;
        }
        return new IntInterval(getLast(), from, -step);
    }

    // ==================== 转换操作 | Conversion Operations ====================

    /**
     * Convert this interval to an {@code int} array.
     * 将此区间转换为 {@code int} 数组。
     *
     * @return an array containing all elements | 包含所有元素的数组
     */
    public int[] toArray() {
        int[] result = new int[size];
        long current = from;
        for (int i = 0; i < size; i++) {
            result[i] = (int) current;
            current += step;
        }
        return result;
    }

    /**
     * Convert this interval to a {@link List} of {@link Integer}.
     * 将此区间转换为 {@link Integer} 的 {@link List}。
     *
     * @return an unmodifiable list containing all elements | 包含所有元素的不可修改列表
     */
    public List<Integer> toList() {
        List<Integer> result = new ArrayList<>(size);
        long current = from;
        for (int i = 0; i < size; i++) {
            result.add((int) current);
            current += step;
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Return an {@link IntStream} over the elements of this interval.
     * 返回此区间元素上的 {@link IntStream}。
     *
     * @return an IntStream | IntStream
     */
    public IntStream stream() {
        return IntStream.range(0, size).map(this::get);
    }

    // ==================== 迭代 | Iteration ====================

    /**
     * Return a {@link PrimitiveIterator.OfInt} over the elements.
     * 返回元素上的 {@link PrimitiveIterator.OfInt}。
     *
     * @return a primitive int iterator | 原始 int 迭代器
     */
    @Override
    public Iterator<Integer> iterator() {
        return new PrimitiveIterator.OfInt() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public int nextInt() {
                if (index >= size) {
                    throw new NoSuchElementException();
                }
                return get(index++);
            }
        };
    }

    /**
     * Perform the given action for each element (primitive, no boxing).
     * 对每个元素执行给定操作（原始类型，无装箱）。
     *
     * @param action the action to perform | 要执行的操作
     */
    public void forEach(IntConsumer action) {
        Objects.requireNonNull(action, "Action must not be null");
        long current = from;
        for (int i = 0; i < size; i++) {
            action.accept((int) current);
            current += step;
        }
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IntInterval other)) {
            return false;
        }
        return this.from == other.from
                && this.to == other.to
                && this.step == other.step;
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(from);
        result = 31 * result + Integer.hashCode(to);
        result = 31 * result + Integer.hashCode(step);
        return result;
    }

    /**
     * Return a string representation such as {@code "IntInterval[1..10 step 1]"}.
     * 返回字符串表示，如 {@code "IntInterval[1..10 step 1]"}。
     *
     * @return string representation | 字符串表示
     */
    @Override
    public String toString() {
        return "IntInterval[" + from + ".." + to + " step " + step + "]";
    }

    // ==================== 内部方法 | Internal Methods ====================

    /**
     * Compute the number of elements using long arithmetic to avoid overflow.
     * 使用 long 运算计算元素数量以避免溢出。
     */
    private static int computeSize(int from, int to, int step) {
        long range = (long) to - (long) from;
        if (step > 0 && range < 0) {
            return 0;
        }
        if (step < 0 && range > 0) {
            return 0;
        }
        if (range == 0) {
            return 1;
        }
        long count = range / step + 1;
        if (count > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Interval size exceeds Integer.MAX_VALUE: from=" + from + " to=" + to + " step=" + step);
        }
        return (int) count;
    }
}
