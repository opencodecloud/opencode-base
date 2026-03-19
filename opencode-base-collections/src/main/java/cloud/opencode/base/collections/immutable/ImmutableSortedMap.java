package cloud.opencode.base.collections.immutable;

import cloud.opencode.base.collections.ImmutableMap;
import cloud.opencode.base.collections.ImmutableSet;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * ImmutableSortedMap - Immutable Sorted Map Implementation
 * ImmutableSortedMap - 不可变有序映射实现
 *
 * <p>A sorted map that cannot be modified after creation. Entries are stored
 * in sorted order by key.</p>
 * <p>创建后不能修改的有序映射。条目按键排序存储。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable - 不可变</li>
 *   <li>Thread-safe - 线程安全</li>
 *   <li>Sorted by key - 按键排序</li>
 *   <li>NavigableMap operations - NavigableMap 操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create from entries - 从条目创建
 * ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.of("c", 3, "a", 1, "b", 2);
 * // Keys in order: [a, b, c]
 *
 * // Create with builder - 使用构建器创建
 * ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.<String, Integer>naturalOrder()
 *     .put("c", 3)
 *     .put("a", 1)
 *     .build();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>get: O(log n) - get: O(log n)</li>
 *   <li>containsKey: O(log n) - containsKey: O(log n)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Yes (nulls not allowed) - 空值安全: 是（不允许空值）</li>
 * </ul>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class ImmutableSortedMap<K, V> extends AbstractMap<K, V>
        implements NavigableMap<K, V>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    private static final ImmutableSortedMap EMPTY = new ImmutableSortedMap<>(
            new Object[0], new Object[0], null);

    private final Object[] keys;
    private final Object[] values;
    private final Comparator<? super K> comparator;
    private transient Set<Entry<K, V>> entrySet;

    // ==================== 构造方法 | Constructors ====================

    private ImmutableSortedMap(Object[] keys, Object[] values, Comparator<? super K> comparator) {
        this.keys = keys;
        this.values = values;
        this.comparator = comparator;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Return an empty immutable sorted map.
     * 返回空不可变有序映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return empty immutable sorted map | 空不可变有序映射
     */
    @SuppressWarnings("unchecked")
    public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> of() {
        return (ImmutableSortedMap<K, V>) EMPTY;
    }

    /**
     * Return an immutable sorted map with one entry.
     * 返回包含一个条目的不可变有序映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param k1  key | 键
     * @param v1  value | 值
     * @return immutable sorted map | 不可变有序映射
     */
    public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> of(K k1, V v1) {
        Objects.requireNonNull(k1);
        Objects.requireNonNull(v1);
        return new ImmutableSortedMap<>(new Object[]{k1}, new Object[]{v1}, null);
    }

    /**
     * Return an immutable sorted map with two entries.
     * 返回包含两个条目的不可变有序映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param k1  first key | 第一个键
     * @param v1  first value | 第一个值
     * @param k2  second key | 第二个键
     * @param v2  second value | 第二个值
     * @return immutable sorted map | 不可变有序映射
     */
    public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> of(
            K k1, V v1, K k2, V v2) {
        return ImmutableSortedMap.<K, V>naturalOrder()
                .put(k1, v1)
                .put(k2, v2)
                .build();
    }

    /**
     * Return an immutable sorted map with three entries.
     * 返回包含三个条目的不可变有序映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param k1  first key | 第一个键
     * @param v1  first value | 第一个值
     * @param k2  second key | 第二个键
     * @param v2  second value | 第二个值
     * @param k3  third key | 第三个键
     * @param v3  third value | 第三个值
     * @return immutable sorted map | 不可变有序映射
     */
    public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> of(
            K k1, V v1, K k2, V v2, K k3, V v3) {
        return ImmutableSortedMap.<K, V>naturalOrder()
                .put(k1, v1)
                .put(k2, v2)
                .put(k3, v3)
                .build();
    }

    /**
     * Copy from a map.
     * 从映射复制。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param map the map | 映射
     * @return immutable sorted map | 不可变有序映射
     */
    public static <K extends Comparable<? super K>, V> ImmutableSortedMap<K, V> copyOf(
            Map<? extends K, ? extends V> map) {
        return copyOf(map, null);
    }

    /**
     * Copy from a map with comparator.
     * 使用比较器从映射复制。
     *
     * @param <K>        key type | 键类型
     * @param <V>        value type | 值类型
     * @param map        the map | 映射
     * @param comparator the comparator | 比较器
     * @return immutable sorted map | 不可变有序映射
     */
    @SuppressWarnings("unchecked")
    public static <K, V> ImmutableSortedMap<K, V> copyOf(Map<? extends K, ? extends V> map,
                                                          Comparator<? super K> comparator) {
        if (map.isEmpty()) {
            return (ImmutableSortedMap<K, V>) EMPTY;
        }
        TreeMap<K, V> sorted = comparator != null
                ? new TreeMap<>(comparator)
                : new TreeMap<>();
        for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "null key");
            Objects.requireNonNull(entry.getValue(), "null value");
            sorted.put(entry.getKey(), entry.getValue());
        }
        Object[] keys = new Object[sorted.size()];
        Object[] values = new Object[sorted.size()];
        int i = 0;
        for (Entry<K, V> entry : sorted.entrySet()) {
            keys[i] = entry.getKey();
            values[i] = entry.getValue();
            i++;
        }
        return new ImmutableSortedMap<>(keys, values, comparator);
    }

    /**
     * Create a builder with natural ordering.
     * 创建使用自然顺序的构建器。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return builder | 构建器
     */
    public static <K extends Comparable<? super K>, V> Builder<K, V> naturalOrder() {
        return new Builder<>(null);
    }

    /**
     * Create a builder with specified comparator.
     * 创建使用指定比较器的构建器。
     *
     * @param <K>        key type | 键类型
     * @param <V>        value type | 值类型
     * @param comparator the comparator | 比较器
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> orderedBy(Comparator<? super K> comparator) {
        return new Builder<>(comparator);
    }

    // ==================== Map 方法 | Map Methods ====================

    @Override
    public int size() {
        return keys.length;
    }

    @Override
    public boolean isEmpty() {
        return keys.length == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return key != null && binarySearch(key) >= 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        if (key == null) {
            return null;
        }
        int index = binarySearch(key);
        return index >= 0 ? (V) values[index] : null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    // ==================== NavigableMap 方法 | NavigableMap Methods ====================

    @Override
    public Comparator<? super K> comparator() {
        return comparator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public K firstKey() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return (K) keys[0];
    }

    @Override
    @SuppressWarnings("unchecked")
    public K lastKey() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return (K) keys[keys.length - 1];
    }

    @Override
    public Entry<K, V> lowerEntry(K key) {
        int index = lowerIndex(key);
        return index >= 0 ? entryAt(index) : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public K lowerKey(K key) {
        int index = lowerIndex(key);
        return index >= 0 ? (K) keys[index] : null;
    }

    @Override
    public Entry<K, V> floorEntry(K key) {
        int index = floorIndex(key);
        return index >= 0 ? entryAt(index) : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public K floorKey(K key) {
        int index = floorIndex(key);
        return index >= 0 ? (K) keys[index] : null;
    }

    @Override
    public Entry<K, V> ceilingEntry(K key) {
        int index = ceilingIndex(key);
        return index < keys.length ? entryAt(index) : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public K ceilingKey(K key) {
        int index = ceilingIndex(key);
        return index < keys.length ? (K) keys[index] : null;
    }

    @Override
    public Entry<K, V> higherEntry(K key) {
        int index = higherIndex(key);
        return index < keys.length ? entryAt(index) : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public K higherKey(K key) {
        int index = higherIndex(key);
        return index < keys.length ? (K) keys[index] : null;
    }

    @Override
    public Entry<K, V> firstEntry() {
        return isEmpty() ? null : entryAt(0);
    }

    @Override
    public Entry<K, V> lastEntry() {
        return isEmpty() ? null : entryAt(keys.length - 1);
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        throw new UnsupportedOperationException("ImmutableSortedMap is immutable");
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        throw new UnsupportedOperationException("ImmutableSortedMap is immutable");
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
        throw new UnsupportedOperationException("descendingMap not yet implemented");
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        return ImmutableSortedSet.copyOf(keySet(), comparator);
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return navigableKeySet().descendingSet();
    }

    @Override
    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        int from = fromInclusive ? ceilingIndex(fromKey) : higherIndex(fromKey);
        int to = toInclusive ? floorIndex(toKey) : lowerIndex(toKey);
        return subMapByIndex(from, to + 1);
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        int to = inclusive ? floorIndex(toKey) : lowerIndex(toKey);
        return subMapByIndex(0, to + 1);
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        int from = inclusive ? ceilingIndex(fromKey) : higherIndex(fromKey);
        return subMapByIndex(from, keys.length);
    }

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return subMap(fromKey, true, toKey, false);
    }

    @Override
    public SortedMap<K, V> headMap(K toKey) {
        return headMap(toKey, false);
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        return tailMap(fromKey, true);
    }

    // ==================== 辅助方法 | Helper Methods ====================

    @SuppressWarnings({"unchecked", "rawtypes"})
    private int binarySearch(Object key) {
        Comparator cmp = comparator != null
                ? comparator
                : Comparator.naturalOrder();
        return Arrays.binarySearch(keys, key, cmp);
    }

    private int lowerIndex(K key) {
        int index = binarySearch(key);
        return index >= 0 ? index - 1 : -index - 2;
    }

    private int floorIndex(K key) {
        int index = binarySearch(key);
        return index >= 0 ? index : -index - 2;
    }

    private int ceilingIndex(K key) {
        int index = binarySearch(key);
        return index >= 0 ? index : -index - 1;
    }

    private int higherIndex(K key) {
        int index = binarySearch(key);
        return index >= 0 ? index + 1 : -index - 1;
    }

    @SuppressWarnings("unchecked")
    private Entry<K, V> entryAt(int index) {
        return Map.entry((K) keys[index], (V) values[index]);
    }

    @SuppressWarnings("unchecked")
    private ImmutableSortedMap<K, V> subMapByIndex(int from, int to) {
        if (from >= to || from >= keys.length) {
            return (ImmutableSortedMap<K, V>) EMPTY;
        }
        from = Math.max(0, from);
        to = Math.min(to, keys.length);
        return new ImmutableSortedMap<>(
                Arrays.copyOfRange(keys, from, to),
                Arrays.copyOfRange(values, from, to),
                comparator
        );
    }

    // ==================== 内部类 | Inner Classes ====================

    /**
     * Builder for ImmutableSortedMap.
     * ImmutableSortedMap 构建器。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    public static final class Builder<K, V> {
        private final TreeMap<K, V> entries;

        Builder(Comparator<? super K> comparator) {
            this.entries = comparator != null ? new TreeMap<>(comparator) : new TreeMap<>();
        }

        /**
         * Put entry.
         * 放入条目。
         *
         * @param key   the key | 键
         * @param value the value | 值
         * @return this builder | 此构建器
         */
        public Builder<K, V> put(K key, V value) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
            entries.put(key, value);
            return this;
        }

        /**
         * Put all entries.
         * 放入所有条目。
         *
         * @param map the map | 映射
         * @return this builder | 此构建器
         */
        public Builder<K, V> putAll(Map<? extends K, ? extends V> map) {
            for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
            return this;
        }

        /**
         * Build the immutable sorted map.
         * 构建不可变有序映射。
         *
         * @return immutable sorted map | 不可变有序映射
         */
        @SuppressWarnings("unchecked")
        public ImmutableSortedMap<K, V> build() {
            if (entries.isEmpty()) {
                return (ImmutableSortedMap<K, V>) EMPTY;
            }
            Object[] keys = new Object[entries.size()];
            Object[] values = new Object[entries.size()];
            int i = 0;
            for (Entry<K, V> entry : entries.entrySet()) {
                keys[i] = entry.getKey();
                values[i] = entry.getValue();
                i++;
            }
            return new ImmutableSortedMap<>(keys, values, entries.comparator());
        }
    }

    private final class EntrySet extends AbstractSet<Entry<K, V>> {
        @Override
        public int size() {
            return keys.length;
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new Iterator<>() {
                private int index = 0;

                @Override
                public boolean hasNext() {
                    return index < keys.length;
                }

                @Override
                public Entry<K, V> next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return entryAt(index++);
                }
            };
        }

        @Override
        public boolean contains(Object o) {
            if (o instanceof Entry<?, ?> entry) {
                V value = get(entry.getKey());
                return value != null && value.equals(entry.getValue());
            }
            return false;
        }
    }
}
