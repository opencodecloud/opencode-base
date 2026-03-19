package cloud.opencode.base.collections.primitive;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * LongSet - Primitive long Set Implementation
 * LongSet - 原始 long 集合实现
 *
 * <p>A set specialized for primitive long values to avoid boxing overhead.</p>
 * <p>专门用于原始 long 值的集合，以避免装箱开销。</p>
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
 * LongSet set = LongSet.create();
 * set.add(1L);
 * set.add(2L);
 * set.add(3L);
 *
 * boolean contains = set.contains(2L);  // true
 * long[] array = set.toLongArray();     // [1, 2, 3]
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
 *   <li>Thread-safe: No - 否</li>
 *   <li>Null-safe: No (primitive values, no null) - 否（原始值，无null）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class LongSet implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;
    private static final long EMPTY = Long.MIN_VALUE;

    private long[] keys;
    private boolean[] used;
    private int size;
    private int threshold;

    // ==================== 构造方法 | Constructors ====================

    private LongSet(int initialCapacity) {
        int capacity = tableSizeFor(initialCapacity);
        this.keys = new long[capacity];
        this.used = new boolean[capacity];
        this.threshold = (int) (capacity * LOAD_FACTOR);
        this.size = 0;
        Arrays.fill(keys, EMPTY);
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty LongSet.
     * 创建空 LongSet。
     *
     * @return new empty LongSet | 新空 LongSet
     */
    public static LongSet create() {
        return new LongSet(DEFAULT_CAPACITY);
    }

    /**
     * Create a LongSet with initial capacity.
     * 创建指定初始容量的 LongSet。
     *
     * @param initialCapacity initial capacity | 初始容量
     * @return new empty LongSet | 新空 LongSet
     */
    public static LongSet create(int initialCapacity) {
        return new LongSet(initialCapacity);
    }

    /**
     * Create a LongSet from values.
     * 从值创建 LongSet。
     *
     * @param values the values | 值
     * @return new LongSet | 新 LongSet
     */
    public static LongSet of(long... values) {
        LongSet set = create(values.length);
        for (long value : values) {
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
    public boolean add(long value) {
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
    public boolean remove(long value) {
        int index = findIndex(value);
        if (!used[index] || keys[index] != value) {
            return false;
        }

        used[index] = false;
        size--;

        // Rehash following elements
        int next = (index + 1) & (keys.length - 1);
        while (used[next]) {
            long key = keys[next];
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
    public boolean contains(long value) {
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
    public long[] toLongArray() {
        long[] result = new long[size];
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
    public void forEach(LongConsumer action) {
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
    public PrimitiveIterator.OfLong iterator() {
        return new LongSetIterator();
    }

    // ==================== 私有方法 | Private Methods ====================

    private int findIndex(long value) {
        int index = hash(value) & (keys.length - 1);
        while (used[index] && keys[index] != value) {
            index = (index + 1) & (keys.length - 1);
        }
        return index;
    }

    private void resize() {
        long[] oldKeys = keys;
        boolean[] oldUsed = used;

        int newCapacity = keys.length << 1;
        keys = new long[newCapacity];
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

    private static int hash(long value) {
        return (int) (value ^ (value >>> 32));
    }

    private static int tableSizeFor(int cap) {
        int n = -1 >>> Integer.numberOfLeadingZeros(cap - 1);
        return (n < 0) ? 1 : (n >= (1 << 30)) ? (1 << 30) : n + 1;
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LongSet that)) return false;
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
        long result = 0;
        for (int i = 0; i < keys.length; i++) {
            if (used[i]) {
                result += keys[i];
            }
        }
        return (int) (result ^ (result >>> 32));
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
     * Functional interface for long consumer.
     * long 消费者函数式接口。
     */
    @FunctionalInterface
    public interface LongConsumer {
        void accept(long value);
    }

    private class LongSetIterator implements PrimitiveIterator.OfLong {
        private int index = 0;
        private int remaining = size;

        @Override
        public boolean hasNext() {
            return remaining > 0;
        }

        @Override
        public long nextLong() {
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
