package cloud.opencode.base.collections.primitive;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * LongObjectMap - Primitive long Key to Object Value Map
 * LongObjectMap - 原始 long 键到对象值映射
 *
 * <p>A map with primitive long keys and object values to avoid key boxing overhead.</p>
 * <p>具有原始 long 键和对象值的映射，以避免键装箱开销。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>No key boxing overhead - 无键装箱开销</li>
 *   <li>Memory efficient - 内存高效</li>
 *   <li>Fast long key operations - 快速 long 键操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LongObjectMap<String> map = LongObjectMap.create();
 * map.put(1L, "one");
 * map.put(2L, "two");
 *
 * String value = map.get(1L);  // "one"
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
 *   <li>Null-safe: Partial (values can be null) - 部分（值可以为null）</li>
 * </ul>
 * @param <V> value type | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class LongObjectMap<V> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;
    private static final long EMPTY_KEY = Long.MIN_VALUE;

    private long[] keys;
    private Object[] values;
    private boolean[] used;
    private int size;
    private int threshold;

    // ==================== 构造方法 | Constructors ====================

    private LongObjectMap(int initialCapacity) {
        int capacity = tableSizeFor(initialCapacity);
        this.keys = new long[capacity];
        this.values = new Object[capacity];
        this.used = new boolean[capacity];
        this.threshold = (int) (capacity * LOAD_FACTOR);
        this.size = 0;
        Arrays.fill(keys, EMPTY_KEY);
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty LongObjectMap.
     * 创建空 LongObjectMap。
     *
     * @param <V> value type | 值类型
     * @return new empty LongObjectMap | 新空 LongObjectMap
     */
    public static <V> LongObjectMap<V> create() {
        return new LongObjectMap<>(DEFAULT_CAPACITY);
    }

    /**
     * Create a LongObjectMap with initial capacity.
     * 创建指定初始容量的 LongObjectMap。
     *
     * @param <V>             value type | 值类型
     * @param initialCapacity initial capacity | 初始容量
     * @return new empty LongObjectMap | 新空 LongObjectMap
     */
    public static <V> LongObjectMap<V> create(int initialCapacity) {
        return new LongObjectMap<>(initialCapacity);
    }

    // ==================== 操作方法 | Operation Methods ====================

    /**
     * Put a key-value pair.
     * 放入键值对。
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return the old value or null | 旧值或 null
     */
    @SuppressWarnings("unchecked")
    public V put(long key, V value) {
        Objects.requireNonNull(value, "Value cannot be null");
        if (size >= threshold) {
            resize();
        }

        int index = findIndex(key);
        if (used[index] && keys[index] == key) {
            V oldValue = (V) values[index];
            values[index] = value;
            return oldValue;
        }

        keys[index] = key;
        values[index] = value;
        used[index] = true;
        size++;
        return null;
    }

    /**
     * Get the value for a key.
     * 获取键对应的值。
     *
     * @param key the key | 键
     * @return the value or null | 值或 null
     */
    @SuppressWarnings("unchecked")
    public V get(long key) {
        int index = findIndex(key);
        if (!used[index] || keys[index] != key) {
            return null;
        }
        return (V) values[index];
    }

    /**
     * Get the value for a key or default.
     * 获取键对应的值或默认值。
     *
     * @param key          the key | 键
     * @param defaultValue the default value | 默认值
     * @return the value or default | 值或默认值
     */
    public V getOrDefault(long key, V defaultValue) {
        V value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Remove a key-value pair.
     * 移除键值对。
     *
     * @param key the key | 键
     * @return the old value or null | 旧值或 null
     */
    @SuppressWarnings("unchecked")
    public V remove(long key) {
        int index = findIndex(key);
        if (!used[index] || keys[index] != key) {
            return null;
        }

        V oldValue = (V) values[index];
        used[index] = false;
        values[index] = null;
        size--;

        // Rehash following elements
        int next = (index + 1) & (keys.length - 1);
        while (used[next]) {
            long k = keys[next];
            V v = (V) values[next];
            used[next] = false;
            values[next] = null;
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
    public boolean containsValue(Object value) {
        if (value == null) return false;
        for (int i = 0; i < values.length; i++) {
            if (used[i] && value.equals(values[i])) {
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
        Arrays.fill(values, null);
        Arrays.fill(used, false);
        size = 0;
    }

    /**
     * Return all keys as a LongSet.
     * 以 LongSet 形式返回所有键。
     *
     * @return the keys | 键
     */
    public LongSet keySet() {
        LongSet set = LongSet.create(size);
        for (int i = 0; i < keys.length; i++) {
            if (used[i]) {
                set.add(keys[i]);
            }
        }
        return set;
    }

    /**
     * Return all values as a collection.
     * 以集合形式返回所有值。
     *
     * @return the values | 值
     */
    @SuppressWarnings("unchecked")
    public Collection<V> values() {
        List<V> result = new ArrayList<>(size);
        for (int i = 0; i < values.length; i++) {
            if (used[i]) {
                result.add((V) values[i]);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Apply action to each entry.
     * 对每个条目应用操作。
     *
     * @param action the action | 操作
     */
    @SuppressWarnings("unchecked")
    public void forEach(LongObjectConsumer<? super V> action) {
        for (int i = 0; i < keys.length; i++) {
            if (used[i]) {
                action.accept(keys[i], (V) values[i]);
            }
        }
    }

    // ==================== 私有方法 | Private Methods ====================

    private int findIndex(long key) {
        int index = hash(key) & (keys.length - 1);
        while (used[index] && keys[index] != key) {
            index = (index + 1) & (keys.length - 1);
        }
        return index;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        long[] oldKeys = keys;
        Object[] oldValues = values;
        boolean[] oldUsed = used;

        int newCapacity = keys.length << 1;
        keys = new long[newCapacity];
        values = new Object[newCapacity];
        used = new boolean[newCapacity];
        Arrays.fill(keys, EMPTY_KEY);
        threshold = (int) (newCapacity * LOAD_FACTOR);
        size = 0;

        for (int i = 0; i < oldKeys.length; i++) {
            if (oldUsed[i]) {
                put(oldKeys[i], (V) oldValues[i]);
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
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LongObjectMap<?> that)) return false;
        if (size != that.size) return false;
        for (int i = 0; i < keys.length; i++) {
            if (used[i]) {
                if (!that.containsKey(keys[i]) || !Objects.equals(values[i], that.get(keys[i]))) {
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
                result += hash(keys[i]) ^ Objects.hashCode(values[i]);
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
     * Functional interface for long-object consumer.
     * long-object 消费者函数式接口。
     *
     * @param <V> value type | 值类型
     */
    @FunctionalInterface
    public interface LongObjectConsumer<V> {
        void accept(long key, V value);
    }
}
