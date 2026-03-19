package cloud.opencode.base.collections.immutable;

import cloud.opencode.base.collections.exception.OpenCollectionException;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * ImmutableBiMap - Immutable Bidirectional Map Implementation
 * ImmutableBiMap - 不可变双向映射实现
 *
 * <p>A bidirectional map that maintains a one-to-one correspondence between keys and values.
 * Both keys and values must be unique. Cannot be modified after creation.</p>
 * <p>维护键和值之间一对一对应关系的双向映射。键和值都必须唯一。创建后不能修改。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable - 不可变</li>
 *   <li>Thread-safe - 线程安全</li>
 *   <li>Null-safe (nulls not allowed) - 空值安全（不允许空值）</li>
 *   <li>Bidirectional lookup - 双向查找</li>
 *   <li>Unique keys and values - 唯一的键和值</li>
 *   <li>O(1) lookup in both directions - O(1) 双向查找</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create from entries - 从条目创建
 * ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.<String, Integer>builder()
 *     .put("one", 1)
 *     .put("two", 2)
 *     .put("three", 3)
 *     .build();
 *
 * // Forward lookup - 正向查找
 * Integer value = bimap.get("one"); // Returns 1 - 返回 1
 *
 * // Reverse lookup - 反向查找
 * ImmutableBiMap<Integer, String> inverse = bimap.inverse();
 * String key = inverse.get(1); // Returns "one" - 返回 "one"
 *
 * // Create from map - 从映射创建
 * Map<String, Integer> map = Map.of("a", 1, "b", 2);
 * ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.copyOf(map);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>get: O(1) - get: O(1)</li>
 *   <li>containsKey: O(1) - containsKey: O(1)</li>
 *   <li>containsValue: O(1) - containsValue: O(1)</li>
 *   <li>inverse: O(1) (cached) - inverse: O(1)（缓存）</li>
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
public final class ImmutableBiMap<K, V> extends AbstractMap<K, V> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final ImmutableBiMap<?, ?> EMPTY = new ImmutableBiMap<>(Map.of(), Map.of(), null);

    private final Map<K, V> forwardMap;
    private final Map<V, K> reverseMap;
    private transient ImmutableBiMap<V, K> inverse;

    // ==================== 构造方法 | Constructors ====================

    /**
     * Private constructor.
     * 私有构造方法。
     *
     * @param forwardMap the forward map | 正向映射
     * @param reverseMap the reverse map | 反向映射
     * @param inverse    the inverse bimap | 反向双向映射
     */
    private ImmutableBiMap(Map<K, V> forwardMap, Map<V, K> reverseMap, ImmutableBiMap<V, K> inverse) {
        this.forwardMap = forwardMap;
        this.reverseMap = reverseMap;
        this.inverse = inverse;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Return an empty immutable bimap.
     * 返回空不可变双向映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return empty immutable bimap | 空不可变双向映射
     */
    @SuppressWarnings("unchecked")
    public static <K, V> ImmutableBiMap<K, V> of() {
        return (ImmutableBiMap<K, V>) EMPTY;
    }

    /**
     * Return an immutable bimap containing the given entry.
     * 返回包含给定条目的不可变双向映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param k1  the key | 键
     * @param v1  the value | 值
     * @return immutable bimap | 不可变双向映射
     */
    public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1) {
        Objects.requireNonNull(k1);
        Objects.requireNonNull(v1);
        Map<K, V> forward = Map.of(k1, v1);
        Map<V, K> reverse = Map.of(v1, k1);
        ImmutableBiMap<V, K> inverse = new ImmutableBiMap<>(reverse, forward, null);
        ImmutableBiMap<K, V> bimap = new ImmutableBiMap<>(forward, reverse, inverse);
        inverse.inverse = bimap;
        return bimap;
    }

    /**
     * Return an immutable bimap containing the given entries.
     * 返回包含给定条目的不可变双向映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param k1  first key | 第一个键
     * @param v1  first value | 第一个值
     * @param k2  second key | 第二个键
     * @param v2  second value | 第二个值
     * @return immutable bimap | 不可变双向映射
     */
    public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2) {
        return ImmutableBiMap.<K, V>builder()
                .put(k1, v1)
                .put(k2, v2)
                .build();
    }

    /**
     * Return an immutable bimap containing the entries of the given map.
     * 返回包含给定映射条目的不可变双向映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param map the map | 映射
     * @return immutable bimap | 不可变双向映射
     * @throws OpenCollectionException if duplicate values are found | 如果找到重复值
     */
    public static <K, V> ImmutableBiMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
        if (map == null || map.isEmpty()) {
            return of();
        }
        if (map instanceof ImmutableBiMap) {
            @SuppressWarnings("unchecked")
            ImmutableBiMap<K, V> result = (ImmutableBiMap<K, V>) map;
            return result;
        }
        return ImmutableBiMap.<K, V>builder().putAll(map).build();
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

    // ==================== BiMap 特有方法 | BiMap-Specific Methods ====================

    /**
     * Return the inverse view of this bimap.
     * 返回此双向映射的反向视图。
     *
     * <p>The inverse is cached and returns the same instance on repeated calls.</p>
     * <p>反向视图被缓存，在重复调用时返回相同实例。</p>
     *
     * @return the inverse bimap | 反向双向映射
     */
    public ImmutableBiMap<V, K> inverse() {
        if (inverse == null) {
            inverse = new ImmutableBiMap<>(reverseMap, forwardMap, this);
        }
        return inverse;
    }

    /**
     * Return the key mapped to the given value.
     * 返回映射到给定值的键。
     *
     * @param value the value | 值
     * @return the key, or null if not found | 键，如果未找到则返回 null
     */
    public K getKey(V value) {
        return reverseMap.get(value);
    }

    // ==================== Map 实现 | Map Implementation ====================

    @Override
    public Set<Entry<K, V>> entrySet() {
        return Collections.unmodifiableSet(forwardMap.entrySet());
    }

    @Override
    public int size() {
        return forwardMap.size();
    }

    @Override
    public boolean isEmpty() {
        return forwardMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return key != null && forwardMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return value != null && reverseMap.containsKey(value);
    }

    @Override
    public V get(Object key) {
        return forwardMap.get(key);
    }

    @Override
    public Set<K> keySet() {
        return Collections.unmodifiableSet(forwardMap.keySet());
    }

    @Override
    public Collection<V> values() {
        return Collections.unmodifiableSet(reverseMap.keySet());
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

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Map<?, ?> map)) return false;
        return forwardMap.equals(map);
    }

    @Override
    public int hashCode() {
        return forwardMap.hashCode();
    }

    @Override
    public String toString() {
        return forwardMap.toString();
    }

    // ==================== 构建器 | Builder ====================

    /**
     * Builder for ImmutableBiMap
     * ImmutableBiMap 构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    public static final class Builder<K, V> {
        private final Map<K, V> forwardMap = new LinkedHashMap<>();
        private final Map<V, K> reverseMap = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Add an entry.
         * 添加条目。
         *
         * @param key   the key | 键
         * @param value the value | 值
         * @return this builder | 此构建器
         * @throws OpenCollectionException if key or value already exists | 如果键或值已存在
         */
        public Builder<K, V> put(K key, V value) {
            Objects.requireNonNull(key, "Key cannot be null");
            Objects.requireNonNull(value, "Value cannot be null");

            if (forwardMap.containsKey(key)) {
                throw OpenCollectionException.duplicateKey(key);
            }
            if (reverseMap.containsKey(value)) {
                throw OpenCollectionException.duplicateValue(value);
            }

            forwardMap.put(key, value);
            reverseMap.put(value, key);
            return this;
        }

        /**
         * Add all entries from a map.
         * 从映射添加所有条目。
         *
         * @param map the map | 映射
         * @return this builder | 此构建器
         * @throws OpenCollectionException if duplicate keys or values are found | 如果找到重复的键或值
         */
        public Builder<K, V> putAll(Map<? extends K, ? extends V> map) {
            for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
            return this;
        }

        /**
         * Build the immutable bimap.
         * 构建不可变双向映射。
         *
         * @return immutable bimap | 不可变双向映射
         */
        public ImmutableBiMap<K, V> build() {
            if (forwardMap.isEmpty()) {
                return of();
            }

            Map<K, V> forward = new LinkedHashMap<>(forwardMap);
            Map<V, K> reverse = new LinkedHashMap<>(reverseMap);

            ImmutableBiMap<V, K> inverse = new ImmutableBiMap<>(reverse, forward, null);
            ImmutableBiMap<K, V> bimap = new ImmutableBiMap<>(forward, reverse, inverse);
            inverse.inverse = bimap;

            return bimap;
        }
    }
}
