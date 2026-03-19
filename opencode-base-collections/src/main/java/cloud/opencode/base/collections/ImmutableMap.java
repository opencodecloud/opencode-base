package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * ImmutableMap - Immutable Map Implementation
 * ImmutableMap - 不可变映射实现
 *
 * <p>A map that cannot be modified after creation. Any attempt to modify
 * the map will throw an exception.</p>
 * <p>创建后不能修改的映射。任何修改映射的尝试都会抛出异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable - 不可变</li>
 *   <li>Thread-safe - 线程安全</li>
 *   <li>Null-safe (nulls not allowed) - 空值安全（不允许空值）</li>
 *   <li>O(1) contains check - O(1) 包含检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create from key-value pairs - 从键值对创建
 * ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1, "b", 2);
 *
 * // Create from map - 从映射创建
 * ImmutableMap<String, Integer> map = ImmutableMap.copyOf(existingMap);
 *
 * // Use builder - 使用构建器
 * ImmutableMap<String, Integer> map = ImmutableMap.<String, Integer>builder()
 *     .put("a", 1)
 *     .put("b", 2)
 *     .build();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>get: O(1) average - get: O(1) 平均</li>
 *   <li>containsKey: O(1) average - containsKey: O(1) 平均</li>
 *   <li>containsValue: O(n) - containsValue: O(n)</li>
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
public final class ImmutableMap<K, V> extends AbstractMap<K, V> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final ImmutableMap<?, ?> EMPTY = new ImmutableMap<>(Map.of());

    private final Map<K, V> delegate;
    private transient Set<Entry<K, V>> entrySet;

    // ==================== 构造方法 | Constructors ====================

    /**
     * Private constructor.
     * 私有构造方法。
     *
     * @param delegate the delegate map | 委托映射
     */
    private ImmutableMap(Map<K, V> delegate) {
        this.delegate = delegate;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Return an empty immutable map.
     * 返回空不可变映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return empty immutable map | 空不可变映射
     */
    @SuppressWarnings("unchecked")
    public static <K, V> ImmutableMap<K, V> of() {
        return (ImmutableMap<K, V>) EMPTY;
    }

    /**
     * Return an immutable map containing the given entry.
     * 返回包含给定条目的不可变映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param k1  the key | 键
     * @param v1  the value | 值
     * @return immutable map | 不可变映射
     */
    public static <K, V> ImmutableMap<K, V> of(K k1, V v1) {
        return new ImmutableMap<>(Map.of(
                Objects.requireNonNull(k1),
                Objects.requireNonNull(v1)
        ));
    }

    /**
     * Return an immutable map containing the given entries.
     * 返回包含给定条目的不可变映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param k1  first key | 第一个键
     * @param v1  first value | 第一个值
     * @param k2  second key | 第二个键
     * @param v2  second value | 第二个值
     * @return immutable map | 不可变映射
     */
    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2) {
        Map<K, V> map = new LinkedHashMap<>(2);
        map.put(Objects.requireNonNull(k1), Objects.requireNonNull(v1));
        map.put(Objects.requireNonNull(k2), Objects.requireNonNull(v2));
        return new ImmutableMap<>(map);
    }

    /**
     * Return an immutable map containing the given entries.
     * 返回包含给定条目的不可变映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param k1  first key | 第一个键
     * @param v1  first value | 第一个值
     * @param k2  second key | 第二个键
     * @param v2  second value | 第二个值
     * @param k3  third key | 第三个键
     * @param v3  third value | 第三个值
     * @return immutable map | 不可变映射
     */
    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = new LinkedHashMap<>(3);
        map.put(Objects.requireNonNull(k1), Objects.requireNonNull(v1));
        map.put(Objects.requireNonNull(k2), Objects.requireNonNull(v2));
        map.put(Objects.requireNonNull(k3), Objects.requireNonNull(v3));
        return new ImmutableMap<>(map);
    }

    /**
     * Return an immutable map containing the given entries.
     * 返回包含给定条目的不可变映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param k1  first key | 第一个键
     * @param v1  first value | 第一个值
     * @param k2  second key | 第二个键
     * @param v2  second value | 第二个值
     * @param k3  third key | 第三个键
     * @param v3  third value | 第三个值
     * @param k4  fourth key | 第四个键
     * @param v4  fourth value | 第四个值
     * @return immutable map | 不可变映射
     */
    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        Map<K, V> map = new LinkedHashMap<>(4);
        map.put(Objects.requireNonNull(k1), Objects.requireNonNull(v1));
        map.put(Objects.requireNonNull(k2), Objects.requireNonNull(v2));
        map.put(Objects.requireNonNull(k3), Objects.requireNonNull(v3));
        map.put(Objects.requireNonNull(k4), Objects.requireNonNull(v4));
        return new ImmutableMap<>(map);
    }

    /**
     * Return an immutable map containing the given entries.
     * 返回包含给定条目的不可变映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param k1  first key | 第一个键
     * @param v1  first value | 第一个值
     * @param k2  second key | 第二个键
     * @param v2  second value | 第二个值
     * @param k3  third key | 第三个键
     * @param v3  third value | 第三个值
     * @param k4  fourth key | 第四个键
     * @param v4  fourth value | 第四个值
     * @param k5  fifth key | 第五个键
     * @param v5  fifth value | 第五个值
     * @return immutable map | 不可变映射
     */
    public static <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        Map<K, V> map = new LinkedHashMap<>(5);
        map.put(Objects.requireNonNull(k1), Objects.requireNonNull(v1));
        map.put(Objects.requireNonNull(k2), Objects.requireNonNull(v2));
        map.put(Objects.requireNonNull(k3), Objects.requireNonNull(v3));
        map.put(Objects.requireNonNull(k4), Objects.requireNonNull(v4));
        map.put(Objects.requireNonNull(k5), Objects.requireNonNull(v5));
        return new ImmutableMap<>(map);
    }

    /**
     * Return an immutable map containing the elements of the given map.
     * 返回包含给定映射元素的不可变映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param map the map | 映射
     * @return immutable map | 不可变映射
     */
    public static <K, V> ImmutableMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
        if (map == null || map.isEmpty()) {
            return of();
        }
        if (map instanceof ImmutableMap) {
            @SuppressWarnings("unchecked")
            ImmutableMap<K, V> result = (ImmutableMap<K, V>) map;
            return result;
        }
        Map<K, V> copy = new LinkedHashMap<>(map.size());
        for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
            copy.put(
                    Objects.requireNonNull(entry.getKey(), "Null key"),
                    Objects.requireNonNull(entry.getValue(), "Null value")
            );
        }
        return new ImmutableMap<>(copy);
    }

    /**
     * Return a new builder.
     * 返回新构建器。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    // ==================== Map 实现 | Map Implementation ====================

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return key != null && delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return value != null && delegate.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return delegate.get(key);
    }

    @Override
    public Set<K> keySet() {
        return ImmutableSet.copyOf(delegate.keySet());
    }

    @Override
    public Collection<V> values() {
        return ImmutableList.copyOf(delegate.values());
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new ImmutableEntrySet();
        }
        return entrySet;
    }

    // ==================== 不可变保护 | Immutability Protection ====================

    @Override
    public V put(K key, V value) {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public V remove(Object key) {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public void clear() {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public V putIfAbsent(K key, V value) {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public boolean remove(Object key, Object value) {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public V replace(K key, V value) {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        throw OpenCollectionException.immutableCollection();
    }

    // ==================== 内部类 | Internal Classes ====================

    /**
     * Immutable entry set
     */
    private class ImmutableEntrySet extends AbstractSet<Entry<K, V>> {
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new ImmutableEntryIterator();
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Entry<?, ?> entry)) {
                return false;
            }
            V value = delegate.get(entry.getKey());
            return value != null && value.equals(entry.getValue());
        }
    }

    /**
     * Immutable entry iterator
     */
    private class ImmutableEntryIterator implements Iterator<Entry<K, V>> {
        private final Iterator<Entry<K, V>> delegate = ImmutableMap.this.delegate.entrySet().iterator();

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            Entry<K, V> entry = delegate.next();
            return new ImmutableEntry<>(entry.getKey(), entry.getValue());
        }

        @Override
        public void remove() {
            throw OpenCollectionException.immutableCollection();
        }
    }

    /**
     * Immutable entry
     */
    private record ImmutableEntry<K, V>(K key, V value) implements Entry<K, V> {

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw OpenCollectionException.immutableCollection();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Entry<?, ?> that)) {
                return false;
            }
            return Objects.equals(key, that.getKey()) && Objects.equals(value, that.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }

    // ==================== 构建器 | Builder ====================

    /**
     * Builder for ImmutableMap
     * ImmutableMap 构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    public static final class Builder<K, V> {
        private final Map<K, V> map = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Put an entry.
         * 放入条目。
         *
         * @param key   the key | 键
         * @param value the value | 值
         * @return this builder | 此构建器
         */
        public Builder<K, V> put(K key, V value) {
            map.put(Objects.requireNonNull(key), Objects.requireNonNull(value));
            return this;
        }

        /**
         * Put an entry.
         * 放入条目。
         *
         * @param entry the entry | 条目
         * @return this builder | 此构建器
         */
        public Builder<K, V> put(Entry<? extends K, ? extends V> entry) {
            return put(entry.getKey(), entry.getValue());
        }

        /**
         * Put all entries from a map.
         * 从映射放入所有条目。
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
         * Build the immutable map.
         * 构建不可变映射。
         *
         * @return immutable map | 不可变映射
         */
        public ImmutableMap<K, V> build() {
            if (map.isEmpty()) {
                return of();
            }
            return new ImmutableMap<>(map);
        }
    }
}
