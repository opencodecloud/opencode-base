package cloud.opencode.base.collections.primitive;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * IntSet - Primitive int Set Implementation
 * IntSet - 原始 int 集合实现
 *
 * <p>A set specialized for primitive int values to avoid boxing overhead.</p>
 * <p>专门用于原始 int 值的集合，以避免装箱开销。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>No boxing overhead - 无装箱开销</li>
 *   <li>Memory efficient - 内存高效</li>
 *   <li>Fast operations - 快速操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * IntSet set = IntSet.create();
 * set.add(1);
 * set.add(2);
 * set.add(3);
 *
 * boolean contains = set.contains(2);  // true
 * int[] array = set.toIntArray();      // [1, 2, 3]
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
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class IntSet implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;
    private static final int EMPTY = Integer.MIN_VALUE;

    private int[] keys;
    private boolean[] used;
    private int size;
    private int threshold;

    // ==================== 构造方法 | Constructors ====================

    private IntSet(int initialCapacity) {
        int capacity = tableSizeFor(initialCapacity);
        this.keys = new int[capacity];
        this.used = new boolean[capacity];
        this.threshold = (int) (capacity * LOAD_FACTOR);
        this.size = 0;
        Arrays.fill(keys, EMPTY);
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty IntSet.
     * 创建空 IntSet。
     *
     * @return new empty IntSet | 新空 IntSet
     */
    public static IntSet create() {
        return new IntSet(DEFAULT_CAPACITY);
    }

    /**
     * Create an IntSet with initial capacity.
     * 创建指定初始容量的 IntSet。
     *
     * @param initialCapacity initial capacity | 初始容量
     * @return new empty IntSet | 新空 IntSet
     */
    public static IntSet create(int initialCapacity) {
        return new IntSet(initialCapacity);
    }

    /**
     * Create an IntSet from values.
     * 从值创建 IntSet。
     *
     * @param values the values | 值
     * @return new IntSet | 新 IntSet
     */
    public static IntSet of(int... values) {
        IntSet set = create(values.length);
        for (int value : values) {
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
    public boolean add(int value) {
        if (size >= threshold) {
            resize();
        }

        int index = findIndex(value);
        if (used[index] && keys[index] == value) {
            return false;
        }

        keys[index] = value;
        used[index] = true;
        size++;
        return true;
    }

    /**
     * Remove a value from this set.
     * 从此集合移除值。
     *
     * @param value the value | 值
     * @return true if removed | 如果移除则返回 true
     */
    public boolean remove(int value) {
        int index = findIndex(value);
        if (!used[index] || keys[index] != value) {
            return false;
        }

        used[index] = false;
        size--;

        // Rehash following elements
        int next = (index + 1) & (keys.length - 1);
        while (used[next]) {
            int key = keys[next];
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
    public boolean contains(int value) {
        int index = findIndex(value);
        return used[index] && keys[index] == value;
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
        Arrays.fill(keys, EMPTY);
        Arrays.fill(used, false);
        size = 0;
    }

    /**
     * Return all values as an array.
     * 以数组形式返回所有值。
     *
     * @return the values array | 值数组
     */
    public int[] toIntArray() {
        int[] result = new int[size];
        int index = 0;
        for (int i = 0; i < keys.length; i++) {
            if (used[i]) {
                result[index++] = keys[i];
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
    public void forEach(IntConsumer action) {
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
    public PrimitiveIterator.OfInt iterator() {
        return new IntSetIterator();
    }

    // ==================== 集合操作 | Set Operations ====================

    /**
     * Add all values from another set.
     * 从另一个集合添加所有值。
     *
     * @param other the other set | 另一个集合
     */
    public void addAll(IntSet other) {
        other.forEach(this::add);
    }

    /**
     * Remove all values in another set.
     * 移除另一个集合中的所有值。
     *
     * @param other the other set | 另一个集合
     */
    public void removeAll(IntSet other) {
        other.forEach(this::remove);
    }

    /**
     * Retain only values in another set.
     * 仅保留另一个集合中的值。
     *
     * @param other the other set | 另一个集合
     */
    public void retainAll(IntSet other) {
        int[] toRemove = new int[size];
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

    private int findIndex(int value) {
        int index = hash(value) & (keys.length - 1);
        while (used[index] && keys[index] != value) {
            index = (index + 1) & (keys.length - 1);
        }
        return index;
    }

    private void resize() {
        int[] oldKeys = keys;
        boolean[] oldUsed = used;

        int newCapacity = keys.length << 1;
        keys = new int[newCapacity];
        used = new boolean[newCapacity];
        Arrays.fill(keys, EMPTY);
        threshold = (int) (newCapacity * LOAD_FACTOR);
        size = 0;

        for (int i = 0; i < oldKeys.length; i++) {
            if (oldUsed[i]) {
                add(oldKeys[i]);
            }
        }
    }

    private static int hash(int value) {
        return value ^ (value >>> 16);
    }

    private static int tableSizeFor(int cap) {
        int n = -1 >>> Integer.numberOfLeadingZeros(cap - 1);
        return (n < 0) ? 1 : (n >= (1 << 30)) ? (1 << 30) : n + 1;
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntSet that)) return false;
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
                result += keys[i];
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

    /**
     * Functional interface for int consumer.
     * int 消费者函数式接口。
     */
    @FunctionalInterface
    public interface IntConsumer {
        void accept(int value);
    }

    private class IntSetIterator implements PrimitiveIterator.OfInt {
        private int index = 0;
        private int remaining = size;

        @Override
        public boolean hasNext() {
            return remaining > 0;
        }

        @Override
        public int nextInt() {
            if (remaining <= 0) {
                throw new NoSuchElementException();
            }
            while (!used[index]) {
                index++;
            }
            remaining--;
            return keys[index++];
        }
    }
}
