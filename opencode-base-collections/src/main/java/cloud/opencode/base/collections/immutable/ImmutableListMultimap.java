package cloud.opencode.base.collections.immutable;

import cloud.opencode.base.collections.ImmutableList;
import cloud.opencode.base.collections.ImmutableMap;
import cloud.opencode.base.collections.Multimap;

import java.io.Serial;
import java.util.*;

/**
 * ImmutableListMultimap - Immutable List Multimap Implementation
 * ImmutableListMultimap - 不可变列表多值映射实现
 *
 * <p>A multimap that stores values in lists, preserving insertion order and
 * allowing duplicates.</p>
 * <p>将值存储在列表中的多值映射，保留插入顺序并允许重复。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable - 不可变</li>
 *   <li>Thread-safe - 线程安全</li>
 *   <li>Preserves insertion order - 保留插入顺序</li>
 *   <li>Allows duplicate values - 允许重复值</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ImmutableListMultimap<String, Integer> multimap = ImmutableListMultimap.<String, Integer>builder()
 *     .put("a", 1)
 *     .put("a", 2)
 *     .put("a", 1) // duplicate allowed
 *     .build();
 *
 * ImmutableList<Integer> values = multimap.get("a"); // [1, 2, 1]
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>get: O(1) - get: O(1)</li>
 *   <li>containsKey: O(1) - containsKey: O(1)</li>
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
public final class ImmutableListMultimap<K, V> extends ImmutableMultimap<K, V> {

    @Serial
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    private static final ImmutableListMultimap EMPTY = new ImmutableListMultimap<>(Map.of(), 0);

    // ==================== 构造方法 | Constructors ====================

    private ImmutableListMultimap(Map<K, ImmutableList<V>> map, int size) {
        super(map, size);
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Return an empty immutable list multimap.
     * 返回空不可变列表多值映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return empty immutable list multimap | 空不可变列表多值映射
     */
    @SuppressWarnings("unchecked")
    public static <K, V> ImmutableListMultimap<K, V> of() {
        return (ImmutableListMultimap<K, V>) EMPTY;
    }

    /**
     * Return an immutable list multimap with one entry.
     * 返回包含一个条目的不可变列表多值映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param k1  key | 键
     * @param v1  value | 值
     * @return immutable list multimap | 不可变列表多值映射
     */
    public static <K, V> ImmutableListMultimap<K, V> of(K k1, V v1) {
        Objects.requireNonNull(k1);
        Objects.requireNonNull(v1);
        return new ImmutableListMultimap<>(Map.of(k1, ImmutableList.of(v1)), 1);
    }

    /**
     * Return an immutable list multimap with two entries.
     * 返回包含两个条目的不可变列表多值映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param k1  first key | 第一个键
     * @param v1  first value | 第一个值
     * @param k2  second key | 第二个键
     * @param v2  second value | 第二个值
     * @return immutable list multimap | 不可变列表多值映射
     */
    public static <K, V> ImmutableListMultimap<K, V> of(K k1, V v1, K k2, V v2) {
        return ImmutableListMultimap.<K, V>builder()
                .put(k1, v1)
                .put(k2, v2)
                .build();
    }

    /**
     * Copy from a multimap.
     * 从多值映射复制。
     *
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @param multimap the multimap | 多值映射
     * @return immutable list multimap | 不可变列表多值映射
     */
    public static <K, V> ImmutableListMultimap<K, V> copyOf(Multimap<? extends K, ? extends V> multimap) {
        if (multimap instanceof ImmutableListMultimap<?, ?>) {
            @SuppressWarnings("unchecked")
            ImmutableListMultimap<K, V> result = (ImmutableListMultimap<K, V>) multimap;
            return result;
        }
        Builder<K, V> builder = builder();
        for (Map.Entry<? extends K, ? extends V> entry : multimap.entries()) {
            builder.put(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    /**
     * Create a builder.
     * 创建构建器。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    // ==================== 实现方法 | Implementation Methods ====================

    @Override
    @SuppressWarnings("unchecked")
    public ImmutableList<V> get(K key) {
        Collection<V> values = map.get(key);
        return values != null ? (ImmutableList<V>) values : ImmutableList.of();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<K, ? extends Collection<V>> asMap() {
        Map<K, Collection<V>> result = new LinkedHashMap<>();
        for (Map.Entry<K, ? extends Collection<V>> entry : map.entrySet()) {
            result.put(entry.getKey(), (Collection<V>) entry.getValue());
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Return the inverse multimap.
     * 返回逆多值映射。
     *
     * @return inverse multimap | 逆多值映射
     */
    public ImmutableListMultimap<V, K> inverse() {
        Builder<V, K> builder = builder();
        for (Map.Entry<K, V> entry : entries()) {
            builder.put(entry.getValue(), entry.getKey());
        }
        return builder.build();
    }

    // ==================== 内部类 | Inner Classes ====================

    /**
     * Builder for ImmutableListMultimap.
     * ImmutableListMultimap 构建器。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    public static final class Builder<K, V> {
        private final Map<K, List<V>> entries = new LinkedHashMap<>();
        private int size = 0;

        Builder() {
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
            entries.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            size++;
            return this;
        }

        /**
         * Put all values for a key.
         * 为键放入所有值。
         *
         * @param key    the key | 键
         * @param values the values | 值
         * @return this builder | 此构建器
         */
        public Builder<K, V> putAll(K key, Iterable<? extends V> values) {
            Objects.requireNonNull(key);
            for (V value : values) {
                put(key, value);
            }
            return this;
        }

        /**
         * Put all values for a key.
         * 为键放入所有值。
         *
         * @param key    the key | 键
         * @param values the values | 值
         * @return this builder | 此构建器
         */
        @SafeVarargs
        public final Builder<K, V> putAll(K key, V... values) {
            return putAll(key, Arrays.asList(values));
        }

        /**
         * Put all entries from a multimap.
         * 从多值映射放入所有条目。
         *
         * @param multimap the multimap | 多值映射
         * @return this builder | 此构建器
         */
        public Builder<K, V> putAll(Multimap<? extends K, ? extends V> multimap) {
            for (Map.Entry<? extends K, ? extends V> entry : multimap.entries()) {
                put(entry.getKey(), entry.getValue());
            }
            return this;
        }

        /**
         * Build the immutable list multimap.
         * 构建不可变列表多值映射。
         *
         * @return immutable list multimap | 不可变列表多值映射
         */
        public ImmutableListMultimap<K, V> build() {
            if (entries.isEmpty()) {
                return of();
            }
            Map<K, ImmutableList<V>> immutableMap = new LinkedHashMap<>();
            for (Map.Entry<K, List<V>> entry : entries.entrySet()) {
                immutableMap.put(entry.getKey(), ImmutableList.copyOf(entry.getValue()));
            }
            return new ImmutableListMultimap<>(immutableMap, size);
        }
    }
}
