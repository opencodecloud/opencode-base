package cloud.opencode.base.collections.primitive;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.DoubleConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

/**
 * DoubleSet - Primitive double Set Implementation
 * DoubleSet - 原始 double 集合实现
 *
 * <p>A set specialized for primitive double values to avoid boxing overhead.</p>
 * <p>专门用于原始 double 值的集合，以避免装箱开销。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>No boxing overhead - 无装箱开销</li>
 *   <li>Memory efficient - 内存高效</li>
 *   <li>Fast operations - 快速操作</li>
 *   <li>Correct NaN and -0.0 handling - 正确处理 NaN 和 -0.0</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DoubleSet set = DoubleSet.create();
 * set.add(1.0);
 * set.add(2.5);
 * set.add(3.14);
 *
 * boolean contains = set.contains(2.5);    // true
 * double[] array = set.toDoubleArray();     // [1.0, 2.5, 3.14]
 *
 * // Iteration - 迭代
 * set.forEach(value -> System.out.println(value));
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>add: O(1) average - add: O(1) 平均</li>
 *   <li>remove: O(1) average - remove: O(1) 平均</li>
 *   <li>contains: O(1) average - contains: O(1) 平均</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
public final class DoubleSet implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    private double[] keys;
    private boolean[] used;
    private int size;
    private int threshold;

    // ==================== 构造方法 | Constructors ====================

    private DoubleSet(int initialCapacity) {
        int capacity = tableSizeFor(initialCapacity);
        this.keys = new double[capacity];
        this.used = new boolean[capacity];
        this.threshold = (int) (capacity * LOAD_FACTOR);
        this.size = 0;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty DoubleSet.
     * 创建空 DoubleSet。
     *
     * @return new empty DoubleSet | 新空 DoubleSet
     */
    public static DoubleSet create() {
        return new DoubleSet(DEFAULT_CAPACITY);
    }

    /**
     * Create a DoubleSet with initial capacity.
     * 创建指定初始容量的 DoubleSet。
     *
     * @param initialCapacity initial capacity | 初始容量
     * @return new empty DoubleSet | 新空 DoubleSet
     */
    public static DoubleSet create(int initialCapacity) {
        return new DoubleSet(initialCapacity);
    }

    /**
     * Create a DoubleSet from values.
     * 从值创建 DoubleSet。
     *
     * @param values the values | 值
     * @return new DoubleSet | 新 DoubleSet
     */
    public static DoubleSet of(double... values) {
        DoubleSet set = create(values.length);
        for (double value : values) {
            set.add(value);
        }
        return set;
    }

    /**
     * Create a DoubleSet from a double array.
     * 从 double 数组创建 DoubleSet。
     *
     * @param array the source array | 源数组
     * @return new DoubleSet | 新 DoubleSet
     */
    public static DoubleSet from(double[] array) {
        return of(array);
    }

    /**
     * Create a DoubleSet from a Collection of Double.
     * 从 Double 集合创建 DoubleSet。
     *
     * @param collection the source collection | 源集合
     * @return new DoubleSet | 新 DoubleSet
     * @throws NullPointerException if collection contains null elements | 如果集合包含 null 元素
     */
    public static DoubleSet from(Collection<Double> collection) {
        DoubleSet set = create(collection.size());
        for (Double value : collection) {
            set.add(value);
        }
        return set;
    }

    // ==================== 操作方法 | Operation Methods ====================

    /**
     * Add a value to this set.
     * 向此集合添加值。
     *
     * @param value the value | 值
     * @return true if added | 如果添加则返回 true
     */
    public boolean add(double value) {
        if (size >= threshold) {
            resize();
        }

        int index = findIndex(value);
        if (used[index] && Double.compare(keys[index], value) == 0) {
            return false;
        }

        keys[index] = value;
        used[index] = true;
        size++;
        return true;
    }

    /**
     * Add all values to this set.
     * 向此集合添加所有值。
     *
     * @param values the values | 值
     */
    public void addAll(double... values) {
        for (double value : values) {
            add(value);
        }
    }

    /**
     * Remove a value from this set.
     * 从此集合移除值。
     *
     * @param value the value | 值
     * @return true if removed | 如果移除则返回 true
     */
    public boolean remove(double value) {
        int index = findIndex(value);
        if (!used[index] || Double.compare(keys[index], value) != 0) {
            return false;
        }

        used[index] = false;
        size--;

        // Rehash following elements
        int next = (index + 1) & (keys.length - 1);
        while (used[next]) {
            double key = keys[next];
            used[next] = false;
            size--;
            add(key);
            next = (next + 1) & (keys.length - 1);
        }

        return true;
    }

    /**
     * Check if this set contains the value.
     * 检查此集合是否包含该值。
     *
     * @param value the value | 值
     * @return true if contains | 如果包含则返回 true
     */
    public boolean contains(double value) {
        int index = findIndex(value);
        return used[index] && Double.compare(keys[index], value) == 0;
    }

    /**
     * Return the size of this set.
     * 返回此集合的大小。
     *
     * @return the size | 大小
     */
    public int size() {
        return size;
    }

    /**
     * Check if this set is empty.
     * 检查此集合是否为空。
     *
     * @return true if empty | 如果为空则返回 true
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Clear this set.
     * 清空此集合。
     */
    public void clear() {
        Arrays.fill(used, false);
        size = 0;
    }

    /**
     * Return all values as a double array.
     * 以 double 数组形式返回所有值。
     *
     * @return the values array | 值数组
     */
    public double[] toDoubleArray() {
        double[] result = new double[size];
        int index = 0;
        for (int i = 0; i < keys.length; i++) {
            if (used[i]) {
                result[index++] = keys[i];
            }
        }
        return result;
    }

    /**
     * Return all values as a boxed Set.
     * 以装箱 Set 形式返回所有值。
     *
     * @return the boxed set | 装箱集合
     */
    public Set<Double> toSet() {
        Set<Double> result = new HashSet<>(size);
        for (int i = 0; i < keys.length; i++) {
            if (used[i]) {
                result.add(keys[i]);
            }
        }
        return result;
    }

    /**
     * Apply action to each value.
     * 对每个值应用操作。
     *
     * @param action the action | 操作
     */
    public void forEach(DoubleConsumer action) {
        for (int i = 0; i < keys.length; i++) {
            if (used[i]) {
                action.accept(keys[i]);
            }
        }
    }

    /**
     * Return an iterator over the values.
     * 返回值的迭代器。
     *
     * @return the iterator | 迭代器
     */
    public PrimitiveIterator.OfDouble iterator() {
        return new DoubleSetIterator();
    }

    /**
     * Return a DoubleStream of all values.
     * 返回所有值的 DoubleStream。
     *
     * @return the stream | 流
     */
    public DoubleStream stream() {
        return StreamSupport.doubleStream(
                Spliterators.spliterator(toDoubleArray(), Spliterator.DISTINCT),
                false
        );
    }

    // ==================== 集合操作 | Set Operations ====================

    /**
     * Add all values from another set.
     * 从另一个集合添加所有值。
     *
     * @param other the other set | 另一个集合
     */
    public void addAll(DoubleSet other) {
        other.forEach(this::add);
    }

    /**
     * Remove all values in another set.
     * 移除另一个集合中的所有值。
     *
     * @param other the other set | 另一个集合
     */
    public void removeAll(DoubleSet other) {
        other.forEach(this::remove);
    }

    /**
     * Retain only values in another set.
     * 仅保留另一个集合中的值。
     *
     * @param other the other set | 另一个集合
     */
    public void retainAll(DoubleSet other) {
        double[] toRemove = new double[size];
        int removeCount = 0;
        for (int i = 0; i < keys.length; i++) {
            if (used[i] && !other.contains(keys[i])) {
                toRemove[removeCount++] = keys[i];
            }
        }
        for (int i = 0; i < removeCount; i++) {
            remove(toRemove[i]);
        }
    }

    // ==================== 私有方法 | Private Methods ====================

    private int findIndex(double value) {
        int maxProbes = keys.length;
        int index = hash(value) & (keys.length - 1);
        int probes = 0;
        while (used[index] && Double.compare(keys[index], value) != 0) {
            if (++probes >= maxProbes) {
                throw new IllegalStateException("Hash table full");
            }
            index = (index + 1) & (keys.length - 1);
        }
        return index;
    }

    private void resize() {
        double[] oldKeys = keys;
        boolean[] oldUsed = used;

        int newCapacity = keys.length << 1;
        keys = new double[newCapacity];
        used = new boolean[newCapacity];
        threshold = (int) (newCapacity * LOAD_FACTOR);
        size = 0;

        for (int i = 0; i < oldKeys.length; i++) {
            if (oldUsed[i]) {
                add(oldKeys[i]);
            }
        }
    }

    private static int hash(double value) {
        long bits = Double.doubleToLongBits(value);
        return (int) (bits ^ (bits >>> 32));
    }

    private static int tableSizeFor(int cap) {
        int n = -1 >>> Integer.numberOfLeadingZeros(cap - 1);
        return (n < 0) ? 1 : (n >= (1 << 30)) ? (1 << 30) : n + 1;
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DoubleSet that)) return false;
        if (size != that.size) return false;
        for (int i = 0; i < keys.length; i++) {
            if (used[i] && !that.contains(keys[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (int i = 0; i < keys.length; i++) {
            if (used[i]) {
                long bits = Double.doubleToLongBits(keys[i]);
                result += (int) (bits ^ (bits >>> 32));
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (int i = 0; i < keys.length; i++) {
            if (used[i]) {
                if (!first) sb.append(", ");
                sb.append(keys[i]);
                first = false;
            }
        }
        sb.append("]");
        return sb.toString();
    }

    // ==================== 内部类 | Internal Classes ====================

    private class DoubleSetIterator implements PrimitiveIterator.OfDouble {
        private int index = 0;
        private int remaining = size;

        @Override
        public boolean hasNext() {
            return remaining > 0;
        }

        @Override
        public double nextDouble() {
            if (remaining <= 0) {
                throw new NoSuchElementException();
            }
            while (index < keys.length && !used[index]) {
                index++;
            }
            if (index >= keys.length) {
                throw new NoSuchElementException("Iterator exhausted");
            }
            remaining--;
            return keys[index++];
        }
    }
}
