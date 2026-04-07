package cloud.opencode.base.collections.primitive;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.ObjDoubleConsumer;

/**
 * ObjectDoubleMap - Object Key to Primitive double Value Map
 * ObjectDoubleMap - 对象键到原始 double 值映射
 *
 * <p>A map with object keys and primitive double values to avoid value boxing overhead.</p>
 * <p>具有对象键和原始 double 值的映射，以避免值装箱开销。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>No value boxing overhead - 无值装箱开销</li>
 *   <li>Memory efficient - 内存高效</li>
 *   <li>Fast object key operations - 快速对象键操作</li>
 *   <li>addTo for atomic increment - addTo 原子增量操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ObjectDoubleMap<String> map = ObjectDoubleMap.create();
 * map.put("apple", 3.14);
 * map.put("banana", 2.71);
 *
 * double value = map.get("apple");  // 3.14
 * map.addTo("apple", 1.0);         // now 4.14
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
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Keys cannot be null - 键不能为null</li>
 * </ul>
 *
 * @param <K> key type | 键类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
public final class ObjectDoubleMap<K> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    private Object[] keys;
    private double[] values;
    private boolean[] used;
    private int size;
    private int threshold;

    // ==================== 构造方法 | Constructors ====================

    private ObjectDoubleMap(int initialCapacity) {
        int capacity = tableSizeFor(initialCapacity);
        this.keys = new Object[capacity];
        this.values = new double[capacity];
        this.used = new boolean[capacity];
        this.threshold = (int) (capacity * LOAD_FACTOR);
        this.size = 0;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty ObjectDoubleMap.
     * 创建空 ObjectDoubleMap。
     *
     * @param <K> key type | 键类型
     * @return new empty ObjectDoubleMap | 新空 ObjectDoubleMap
     */
    public static <K> ObjectDoubleMap<K> create() {
        return new ObjectDoubleMap<>(DEFAULT_CAPACITY);
    }

    /**
     * Create an ObjectDoubleMap with initial capacity.
     * 创建指定初始容量的 ObjectDoubleMap。
     *
     * @param <K>             key type | 键类型
     * @param initialCapacity initial capacity | 初始容量
     * @return new empty ObjectDoubleMap | 新空 ObjectDoubleMap
     */
    public static <K> ObjectDoubleMap<K> create(int initialCapacity) {
        return new ObjectDoubleMap<>(initialCapacity);
    }

    // ==================== 操作方法 | Operation Methods ====================

    /**
     * Put a key-value pair.
     * 放入键值对。
     *
     * @param key   the key (must not be null) | 键（不能为null）
     * @param value the value | 值
     */
    public void put(K key, double value) {
        Objects.requireNonNull(key, "Key cannot be null");
        if (size >= threshold) {
            resize();
        }

        int index = findIndex(key);
        if (used[index] && key.equals(keys[index])) {
            values[index] = value;
            return;
        }

        keys[index] = key;
        values[index] = value;
        used[index] = true;
        size++;
    }

    /**
     * Get the value for a key.
     * 获取键对应的值。
     *
     * @param key the key | 键
     * @return the value | 值
     * @throws NoSuchElementException if key not found | 如果键未找到
     */
    public double get(K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        int index = findIndex(key);
        if (!used[index] || !key.equals(keys[index])) {
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
    public double getOrDefault(K key, double defaultValue) {
        Objects.requireNonNull(key, "Key cannot be null");
        int index = findIndex(key);
        if (!used[index] || !key.equals(keys[index])) {
            return defaultValue;
        }
        return values[index];
    }

    /**
     * Remove a key-value pair.
     * 移除键值对。
     *
     * @param key the key | 键
     * @return the old value | 旧值
     * @throws NoSuchElementException if key not found | 如果键未找到
     */
    public double remove(K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        int index = findIndex(key);
        if (!used[index] || !key.equals(keys[index])) {
            throw new NoSuchElementException("Key not found: " + key);
        }

        double oldValue = values[index];
        used[index] = false;
        keys[index] = null;
        size--;

        // Rehash following elements
        rehashFrom(index);

        return oldValue;
    }

    /**
     * Check if this map contains the key.
     * 检查此映射是否包含该键。
     *
     * @param key the key | 键
     * @return true if contains | 如果包含则返回 true
     */
    public boolean containsKey(K key) {
        Objects.requireNonNull(key, "Key cannot be null");
        int index = findIndex(key);
        return used[index] && key.equals(keys[index]);
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
        Arrays.fill(keys, null);
        Arrays.fill(used, false);
        size = 0;
    }

    /**
     * Add increment to the value associated with key.
     * If the key does not exist, set it to increment.
     * 将增量添加到与键关联的值。如果键不存在，则设置为增量。
     *
     * @param key       the key | 键
     * @param increment the increment | 增量
     */
    public void addTo(K key, double increment) {
        Objects.requireNonNull(key, "Key cannot be null");
        if (size >= threshold) {
            resize();
        }

        int index = findIndex(key);
        if (used[index] && key.equals(keys[index])) {
            values[index] += increment;
        } else {
            keys[index] = key;
            values[index] = increment;
            used[index] = true;
            size++;
        }
    }

    /**
     * Apply action to each entry.
     * 对每个条目应用操作。
     *
     * @param action the action | 操作
     */
    @SuppressWarnings("unchecked")
    public void forEach(ObjDoubleConsumer<? super K> action) {
        for (int i = 0; i < keys.length; i++) {
            if (used[i]) {
                action.accept((K) keys[i], values[i]);
            }
        }
    }

    /**
     * Return all keys as an unmodifiable set.
     * 以不可修改集合形式返回所有键。
     *
     * @return the keys | 键集合
     */
    @SuppressWarnings("unchecked")
    public Set<K> keySet() {
        Set<K> set = new LinkedHashSet<>(size);
        for (int i = 0; i < keys.length; i++) {
            if (used[i]) {
                set.add((K) keys[i]);
            }
        }
        return Collections.unmodifiableSet(set);
    }

    /**
     * Return all values as a double array.
     * 以 double 数组形式返回所有值。
     *
     * @return the values | 值数组
     */
    public double[] toValueArray() {
        double[] result = new double[size];
        int index = 0;
        for (int i = 0; i < values.length; i++) {
            if (used[i]) {
                result[index++] = values[i];
            }
        }
        return result;
    }

    // ==================== 私有方法 | Private Methods ====================

    private int findIndex(Object key) {
        int maxProbes = keys.length;
        int index = hash(key) & (keys.length - 1);
        int probes = 0;
        while (used[index] && !key.equals(keys[index])) {
            if (++probes >= maxProbes) {
                throw new IllegalStateException("Hash table full");
            }
            index = (index + 1) & (keys.length - 1);
        }
        return index;
    }

    @SuppressWarnings("unchecked")
    private void rehashFrom(int removedIndex) {
        // Collect all displaced entries first, then re-insert.
        // This avoids a mid-loop resize() replacing this.keys while iterating.
        // 先收集所有需要重新哈希的条目，再重新插入。避免循环中 resize() 替换 this.keys。
        java.util.List<Object[]> displaced = new java.util.ArrayList<>();
        int next = (removedIndex + 1) & (keys.length - 1);
        while (used[next]) {
            displaced.add(new Object[]{keys[next], values[next]});
            used[next] = false;
            keys[next] = null;
            size--;
            next = (next + 1) & (keys.length - 1);
        }
        for (Object[] entry : displaced) {
            put((K) entry[0], (double) entry[1]);
        }
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Object[] oldKeys = keys;
        double[] oldValues = values;
        boolean[] oldUsed = used;

        int newCapacity = keys.length >= (1 << 30) ? keys.length : keys.length << 1;
        if (newCapacity == keys.length) return; // already at max capacity
        keys = new Object[newCapacity];
        values = new double[newCapacity];
        used = new boolean[newCapacity];
        threshold = (int) (newCapacity * LOAD_FACTOR);
        size = 0;

        for (int i = 0; i < oldKeys.length; i++) {
            if (oldUsed[i]) {
                put((K) oldKeys[i], oldValues[i]);
            }
        }
    }

    private static int hash(Object key) {
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    private static int tableSizeFor(int cap) {
        if (cap <= 1) return 2; // minimum useful capacity
        int n = -1 >>> Integer.numberOfLeadingZeros(cap - 1);
        return (n < 0) ? 1 : (n >= (1 << 30)) ? (1 << 30) : n + 1;
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ObjectDoubleMap<?> that)) return false;
        if (size != that.size) return false;
        @SuppressWarnings("unchecked")
        ObjectDoubleMap<K> other = (ObjectDoubleMap<K>) that;
        for (int i = 0; i < keys.length; i++) {
            if (used[i]) {
                @SuppressWarnings("unchecked")
                K key = (K) keys[i];
                if (!other.containsKey(key)
                        || Double.compare(other.get(key), values[i]) != 0) {
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
                result += keys[i].hashCode() ^ Long.hashCode(Double.doubleToLongBits(values[i]));
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
}
