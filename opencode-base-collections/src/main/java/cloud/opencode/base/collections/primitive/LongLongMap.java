package cloud.opencode.base.collections.primitive;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * LongLongMap - Primitive long-to-long Map Implementation
 * LongLongMap - 原始 long 到 long 映射实现
 *
 * <p>A map specialized for primitive long keys and values to avoid boxing overhead.</p>
 * <p>专门用于原始 long 键和值的映射，以避免装箱开销。</p>
 *
 * <p><strong>Note:</strong> {@code Long.MIN_VALUE} cannot be used as a value because it serves
 * as the internal sentinel for "no value". Attempting to store {@code Long.MIN_VALUE} will throw
 * {@link IllegalArgumentException}.</p>
 * <p><strong>注意:</strong> {@code Long.MIN_VALUE} 不能用作值，因为它用作内部"无值"哨兵。
 * 尝试存储 {@code Long.MIN_VALUE} 将抛出 {@link IllegalArgumentException}。</p>
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
 * LongLongMap map = LongLongMap.create();
 * map.put(1L, 100L);
 * map.put(2L, 200L);
 *
 * long value = map.get(1L);              // 100L
 * long def = map.getOrDefault(3L, -1L);  // -1L
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>put: O(1) average - put: O(1) 平均</li>
 *   <li>get: O(1) average - get: O(1) 平均</li>
 *   <li>remove: O(1) average - remove: O(1) 平均</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 否</li>
 *   <li>Null-safe: No (primitive values, no null) - 否（原始值，无null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
public final class LongLongMap implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;
    private static final long EMPTY_KEY = Long.MIN_VALUE;
    private static final long NO_VALUE = Long.MIN_VALUE;

    private long[] keys;
    private long[] values;
    private boolean[] used;
    private int size;
    private int threshold;

    // ==================== 构造方法 | Constructors ====================

    private LongLongMap(int initialCapacity) {
        int capacity = tableSizeFor(initialCapacity);
        this.keys = new long[capacity];
        this.values = new long[capacity];
        this.used = new boolean[capacity];
        this.threshold = (int) (capacity * LOAD_FACTOR);
        this.size = 0;
        Arrays.fill(keys, EMPTY_KEY);
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty LongLongMap.
     * 创建空 LongLongMap。
     *
     * @return new empty LongLongMap | 新空 LongLongMap
     */
    public static LongLongMap create() {
        return new LongLongMap(DEFAULT_CAPACITY);
    }

    /**
     * Create a LongLongMap with initial capacity.
     * 创建指定初始容量的 LongLongMap。
     *
     * @param initialCapacity initial capacity | 初始容量
     * @return new empty LongLongMap | 新空 LongLongMap
     */
    public static LongLongMap create(int initialCapacity) {
        return new LongLongMap(initialCapacity);
    }

    /**
     * Create a LongLongMap from key-value pairs.
     * 从键值对创建 LongLongMap。
     *
     * <p>Arguments must be provided in pairs: key1, value1, key2, value2, ...</p>
     * <p>参数必须成对提供：key1, value1, key2, value2, ...</p>
     *
     * @param kvPairs key-value pairs | 键值对
     * @return new LongLongMap | 新 LongLongMap
     * @throws IllegalArgumentException if odd number of arguments | 如果参数数量为奇数
     */
    public static LongLongMap of(long... kvPairs) {
        if (kvPairs.length % 2 != 0) {
            throw new IllegalArgumentException("Arguments must be in key-value pairs");
        }
        LongLongMap map = create(kvPairs.length / 2);
        for (int i = 0; i < kvPairs.length; i += 2) {
            map.put(kvPairs[i], kvPairs[i + 1]);
        }
        return map;
    }

    // ==================== 操作方法 | Operation Methods ====================

    /**
     * Put a key-value pair.
     * 放入键值对。
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return the old value or NO_VALUE | 旧值或 NO_VALUE
     */
    public long put(long key, long value) {
        if (value == NO_VALUE) {
            throw new IllegalArgumentException(
                    "Long.MIN_VALUE is reserved as a sentinel and cannot be stored as a value");
        }
        if (size >= threshold) {
            resize();
        }

        int index = findIndex(key);
        if (used[index] && keys[index] == key) {
            long oldValue = values[index];
            values[index] = value;
            return oldValue;
        }

        keys[index] = key;
        values[index] = value;
        used[index] = true;
        size++;
        return NO_VALUE;
    }

    /**
     * Get the value for a key.
     * 获取键对应的值。
     *
     * @param key the key | 键
     * @return the value | 值
     * @throws NoSuchElementException if key not found | 如果键未找到
     */
    public long get(long key) {
        int index = findIndex(key);
        if (!used[index] || keys[index] != key) {
            throw new NoSuchElementException("Key not found: " + key);
        }
        return values[index];
    }

    /**
     * Get the value for a key or default.
     * 获取键对应的值或默认值。
     *
     * @param key          the key | 键
     * @param defaultValue the default value | 默认值
     * @return the value or default | 值或默认值
     */
    public long getOrDefault(long key, long defaultValue) {
        int index = findIndex(key);
        if (!used[index] || keys[index] != key) {
            return defaultValue;
        }
        return values[index];
    }

    /**
     * Remove a key-value pair.
     * 移除键值对。
     *
     * @param key the key | 键
     * @return the old value or NO_VALUE | 旧值或 NO_VALUE
     */
    public long remove(long key) {
        int index = findIndex(key);
        if (!used[index] || keys[index] != key) {
            return NO_VALUE;
        }

        long oldValue = values[index];
        used[index] = false;
        size--;

        // Rehash following elements
        int next = (index + 1) & (keys.length - 1);
        while (used[next]) {
            long k = keys[next];
            long v = values[next];
            used[next] = false;
            size--;
            put(k, v);
            next = (next + 1) & (keys.length - 1);
        }

        return oldValue;
    }

    /**
     * Check if this map contains the key.
     * 检查此映射是否包含该键。
     *
     * @param key the key | 键
     * @return true if contains | 如果包含则返回 true
     */
    public boolean containsKey(long key) {
        int index = findIndex(key);
        return used[index] && keys[index] == key;
    }

    /**
     * Check if this map contains the value.
     * 检查此映射是否包含该值。
     *
     * @param value the value | 值
     * @return true if contains | 如果包含则返回 true
     */
    public boolean containsValue(long value) {
        for (int i = 0; i < values.length; i++) {
            if (used[i] && values[i] == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the size of this map.
     * 返回此映射的大小。
     *
     * @return the size | 大小
     */
    public int size() {
        return size;
    }

    /**
     * Check if this map is empty.
     * 检查此映射是否为空。
     *
     * @return true if empty | 如果为空则返回 true
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Clear this map.
     * 清空此映射。
     */
    public void clear() {
        Arrays.fill(keys, EMPTY_KEY);
        Arrays.fill(used, false);
        size = 0;
    }

    /**
     * Return all keys as a long array.
     * 以 long 数组形式返回所有键。
     *
     * @return the keys | 键数组
     */
    public long[] keys() {
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
     * Return all values as a long array.
     * 以 long 数组形式返回所有值。
     *
     * @return the values | 值数组
     */
    public long[] values() {
        long[] result = new long[size];
        int index = 0;
        for (int i = 0; i < values.length; i++) {
            if (used[i]) {
                result[index++] = values[i];
            }
        }
        return result;
    }

    /**
     * Apply action to each entry.
     * 对每个条目应用操作。
     *
     * @param action the action | 操作
     */
    public void forEach(LongLongConsumer action) {
        for (int i = 0; i < keys.length; i++) {
            if (used[i]) {
                action.accept(keys[i], values[i]);
            }
        }
    }

    // ==================== 私有方法 | Private Methods ====================

    private int findIndex(long key) {
        int maxProbes = keys.length;
        int index = hash(key) & (keys.length - 1);
        int probes = 0;
        while (used[index] && keys[index] != key) {
            if (++probes >= maxProbes) {
                throw new IllegalStateException("Hash table full");
            }
            index = (index + 1) & (keys.length - 1);
        }
        return index;
    }

    private void resize() {
        long[] oldKeys = keys;
        long[] oldValues = values;
        boolean[] oldUsed = used;

        int newCapacity = keys.length << 1;
        keys = new long[newCapacity];
        values = new long[newCapacity];
        used = new boolean[newCapacity];
        Arrays.fill(keys, EMPTY_KEY);
        threshold = (int) (newCapacity * LOAD_FACTOR);
        size = 0;

        for (int i = 0; i < oldKeys.length; i++) {
            if (oldUsed[i]) {
                put(oldKeys[i], oldValues[i]);
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
        if (!(o instanceof LongLongMap that)) return false;
        if (size != that.size) return false;
        for (int i = 0; i < keys.length; i++) {
            if (used[i]) {
                if (!that.containsKey(keys[i]) || that.get(keys[i]) != values[i]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (int i = 0; i < keys.length; i++) {
            if (used[i]) {
                result += hash(keys[i]) ^ hash(values[i]);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (int i = 0; i < keys.length; i++) {
            if (used[i]) {
                if (!first) sb.append(", ");
                sb.append(keys[i]).append("=").append(values[i]);
                first = false;
            }
        }
        sb.append("}");
        return sb.toString();
    }

    // ==================== 内部类 | Internal Classes ====================

    /**
     * Functional interface for long-long consumer.
     * long-long 消费者函数式接口。
     */
    @FunctionalInterface
    public interface LongLongConsumer {
        /**
         * Accept a long key and long value.
         * 接受一个 long 键和 long 值。
         *
         * @param key   the key | 键
         * @param value the value | 值
         */
        void accept(long key, long value);
    }
}
