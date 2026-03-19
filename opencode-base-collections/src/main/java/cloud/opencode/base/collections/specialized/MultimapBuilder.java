package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.HashMultiset;
import cloud.opencode.base.collections.Multimap;
import cloud.opencode.base.collections.Multiset;

import java.util.*;

/**
 * MultimapBuilder - Fluent Multimap Builder
 * MultimapBuilder - 流式多值映射构建器
 *
 * <p>A fluent builder for creating multimaps with configurable key and value collection types.</p>
 * <p>用于创建具有可配置键和值集合类型的多值映射的流式构建器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent API - 流式API</li>
 *   <li>Key ordering options - 键排序选项</li>
 *   <li>Value collection options - 值集合选项</li>
 *   <li>Thread-safe options - 线程安全选项</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Hash keys with array list values - 哈希键与数组列表值
 * ListMultimap<String, Integer> listMultimap = MultimapBuilder
 *     .hashKeys()
 *     .arrayListValues()
 *     .build();
 *
 * // Linked hash keys with linked hash set values - 链式哈希键与链式哈希集合值
 * SetMultimap<String, Integer> setMultimap = MultimapBuilder
 *     .linkedHashKeys()
 *     .linkedHashSetValues()
 *     .build();
 *
 * // Tree keys with tree set values - 树形键与树形集合值
 * SetMultimap<String, Integer> sortedMultimap = MultimapBuilder
 *     .treeKeys()
 *     .treeSetValues()
 *     .build();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: build() O(1); put() O(1) amortized for hash keys, O(log k) for tree keys - 时间复杂度: build() O(1)；hash键put() O(1) 均摊，tree键 O(log k)</li>
 *   <li>Space complexity: O(k) where k is the number of distinct keys - 空间复杂度: O(k)，k为不同键的数量</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class MultimapBuilder {

    private MultimapBuilder() {
    }

    // ==================== 键构建器工厂方法 | Key Builder Factory Methods ====================

    /**
     * Use hash map for keys.
     * 使用哈希映射存储键。
     *
     * @return key builder | 键构建器
     */
    public static KeyBuilder hashKeys() {
        return new KeyBuilder(KeyType.HASH);
    }

    /**
     * Use hash map for keys with expected size.
     * 使用指定预期大小的哈希映射存储键。
     *
     * @param expectedKeys expected number of keys | 预期键数量
     * @return key builder | 键构建器
     */
    public static KeyBuilder hashKeys(int expectedKeys) {
        return new KeyBuilder(KeyType.HASH, expectedKeys);
    }

    /**
     * Use linked hash map for keys.
     * 使用链式哈希映射存储键。
     *
     * @return key builder | 键构建器
     */
    public static KeyBuilder linkedHashKeys() {
        return new KeyBuilder(KeyType.LINKED_HASH);
    }

    /**
     * Use linked hash map for keys with expected size.
     * 使用指定预期大小的链式哈希映射存储键。
     *
     * @param expectedKeys expected number of keys | 预期键数量
     * @return key builder | 键构建器
     */
    public static KeyBuilder linkedHashKeys(int expectedKeys) {
        return new KeyBuilder(KeyType.LINKED_HASH, expectedKeys);
    }

    /**
     * Use tree map for keys (natural ordering).
     * 使用树形映射存储键（自然排序）。
     *
     * @return key builder | 键构建器
     */
    public static KeyBuilder treeKeys() {
        return new KeyBuilder(KeyType.TREE);
    }

    /**
     * Use tree map for keys with custom comparator.
     * 使用自定义比较器的树形映射存储键。
     *
     * @param <K>        key type | 键类型
     * @param comparator the comparator | 比较器
     * @return key builder | 键构建器
     */
    public static <K> KeyBuilder treeKeys(Comparator<? super K> comparator) {
        return new KeyBuilder(KeyType.TREE, comparator);
    }

    // ==================== 键类型枚举 | Key Type Enum ====================

    private enum KeyType {
        HASH, LINKED_HASH, TREE
    }

    // ==================== 值类型枚举 | Value Type Enum ====================

    private enum ValueType {
        ARRAY_LIST, LINKED_LIST, HASH_SET, LINKED_HASH_SET, TREE_SET
    }

    // ==================== 键构建器 | Key Builder ====================

    /**
     * Key builder for configuring key storage.
     * 用于配置键存储的键构建器。
     */
    public static final class KeyBuilder {
        private final KeyType keyType;
        private final int expectedKeys;
        private final Comparator<?> keyComparator;

        KeyBuilder(KeyType keyType) {
            this(keyType, 16, null);
        }

        KeyBuilder(KeyType keyType, int expectedKeys) {
            this(keyType, expectedKeys, null);
        }

        KeyBuilder(KeyType keyType, Comparator<?> keyComparator) {
            this(keyType, 16, keyComparator);
        }

        KeyBuilder(KeyType keyType, int expectedKeys, Comparator<?> keyComparator) {
            this.keyType = keyType;
            this.expectedKeys = expectedKeys;
            this.keyComparator = keyComparator;
        }

        /**
         * Use array list for values.
         * 使用数组列表存储值。
         *
         * @return list multimap builder | 列表多值映射构建器
         */
        public ListMultimapBuilder arrayListValues() {
            return new ListMultimapBuilder(this, ValueType.ARRAY_LIST, 3);
        }

        /**
         * Use array list for values with expected size.
         * 使用指定预期大小的数组列表存储值。
         *
         * @param expectedValuesPerKey expected values per key | 每个键的预期值数量
         * @return list multimap builder | 列表多值映射构建器
         */
        public ListMultimapBuilder arrayListValues(int expectedValuesPerKey) {
            return new ListMultimapBuilder(this, ValueType.ARRAY_LIST, expectedValuesPerKey);
        }

        /**
         * Use linked list for values.
         * 使用链表存储值。
         *
         * @return list multimap builder | 列表多值映射构建器
         */
        public ListMultimapBuilder linkedListValues() {
            return new ListMultimapBuilder(this, ValueType.LINKED_LIST, 0);
        }

        /**
         * Use hash set for values.
         * 使用哈希集合存储值。
         *
         * @return set multimap builder | 集合多值映射构建器
         */
        public SetMultimapBuilder hashSetValues() {
            return new SetMultimapBuilder(this, ValueType.HASH_SET, 3);
        }

        /**
         * Use hash set for values with expected size.
         * 使用指定预期大小的哈希集合存储值。
         *
         * @param expectedValuesPerKey expected values per key | 每个键的预期值数量
         * @return set multimap builder | 集合多值映射构建器
         */
        public SetMultimapBuilder hashSetValues(int expectedValuesPerKey) {
            return new SetMultimapBuilder(this, ValueType.HASH_SET, expectedValuesPerKey);
        }

        /**
         * Use linked hash set for values.
         * 使用链式哈希集合存储值。
         *
         * @return set multimap builder | 集合多值映射构建器
         */
        public SetMultimapBuilder linkedHashSetValues() {
            return new SetMultimapBuilder(this, ValueType.LINKED_HASH_SET, 3);
        }

        /**
         * Use linked hash set for values with expected size.
         * 使用指定预期大小的链式哈希集合存储值。
         *
         * @param expectedValuesPerKey expected values per key | 每个键的预期值数量
         * @return set multimap builder | 集合多值映射构建器
         */
        public SetMultimapBuilder linkedHashSetValues(int expectedValuesPerKey) {
            return new SetMultimapBuilder(this, ValueType.LINKED_HASH_SET, expectedValuesPerKey);
        }

        /**
         * Use tree set for values (natural ordering).
         * 使用树形集合存储值（自然排序）。
         *
         * @return set multimap builder | 集合多值映射构建器
         */
        public SetMultimapBuilder treeSetValues() {
            return new SetMultimapBuilder(this, ValueType.TREE_SET, 0);
        }

        @SuppressWarnings("unchecked")
        <K, V> Map<K, Collection<V>> createMap() {
            return switch (keyType) {
                case HASH -> new HashMap<>(expectedKeys);
                case LINKED_HASH -> new LinkedHashMap<>(expectedKeys);
                case TREE -> keyComparator != null ?
                        new TreeMap<>((Comparator<? super K>) keyComparator) :
                        new TreeMap<>();
            };
        }
    }

    // ==================== 列表多值映射构建器 | List Multimap Builder ====================

    /**
     * Builder for ListMultimap.
     * ListMultimap 构建器。
     */
    public static final class ListMultimapBuilder {
        private final KeyBuilder keyBuilder;
        private final ValueType valueType;
        private final int expectedValuesPerKey;

        ListMultimapBuilder(KeyBuilder keyBuilder, ValueType valueType, int expectedValuesPerKey) {
            this.keyBuilder = keyBuilder;
            this.valueType = valueType;
            this.expectedValuesPerKey = expectedValuesPerKey;
        }

        /**
         * Build the list multimap.
         * 构建列表多值映射。
         *
         * @param <K> key type | 键类型
         * @param <V> value type | 值类型
         * @return list multimap | 列表多值映射
         */
        public <K, V> ListMultimap<K, V> build() {
            return new ArrayListMultimap<>(keyBuilder.createMap(), this::createValueList);
        }

        @SuppressWarnings("unchecked")
        private <V> List<V> createValueList() {
            return switch (valueType) {
                case ARRAY_LIST -> new ArrayList<>(expectedValuesPerKey);
                case LINKED_LIST -> new LinkedList<>();
                default -> throw new IllegalStateException("Invalid value type for ListMultimap: " + valueType);
            };
        }
    }

    // ==================== 集合多值映射构建器 | Set Multimap Builder ====================

    /**
     * Builder for SetMultimap.
     * SetMultimap 构建器。
     */
    public static final class SetMultimapBuilder {
        private final KeyBuilder keyBuilder;
        private final ValueType valueType;
        private final int expectedValuesPerKey;

        SetMultimapBuilder(KeyBuilder keyBuilder, ValueType valueType, int expectedValuesPerKey) {
            this.keyBuilder = keyBuilder;
            this.valueType = valueType;
            this.expectedValuesPerKey = expectedValuesPerKey;
        }

        /**
         * Build the set multimap.
         * 构建集合多值映射。
         *
         * @param <K> key type | 键类型
         * @param <V> value type | 值类型
         * @return set multimap | 集合多值映射
         */
        public <K, V> SetMultimap<K, V> build() {
            return new LinkedHashSetMultimap<>(keyBuilder.createMap(), this::createValueSet);
        }

        @SuppressWarnings("unchecked")
        private <V> Set<V> createValueSet() {
            return switch (valueType) {
                case HASH_SET -> new HashSet<>(expectedValuesPerKey);
                case LINKED_HASH_SET -> new LinkedHashSet<>(expectedValuesPerKey);
                case TREE_SET -> new TreeSet<>();
                default -> throw new IllegalStateException("Invalid value type for SetMultimap: " + valueType);
            };
        }
    }

    // ==================== 内部实现 | Internal Implementations ====================

    /**
     * Array list based ListMultimap implementation.
     */
    private static final class ArrayListMultimap<K, V> extends AbstractMultimap<K, V, List<V>> implements ListMultimap<K, V> {

        ArrayListMultimap(Map<K, Collection<V>> map, java.util.function.Supplier<List<V>> valueSupplier) {
            super(map, valueSupplier);
        }

        @Override
        public List<V> get(K key) {
            return getOrCreate(key);
        }

        @Override
        public List<V> removeAll(Object key) {
            List<V> removed = (List<V>) map.remove(key);
            return removed != null ? removed : new ArrayList<>();
        }

        @Override
        public List<V> replaceValues(K key, Iterable<? extends V> values) {
            List<V> old = removeAll(key);
            for (V value : values) {
                put(key, value);
            }
            return old;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Map<K, ? extends Collection<V>> asMap() {
            Map<K, List<V>> result = new LinkedHashMap<>();
            for (Map.Entry<K, Collection<V>> entry : map.entrySet()) {
                result.put(entry.getKey(), Collections.unmodifiableList((List<V>) entry.getValue()));
            }
            return Collections.unmodifiableMap(result);
        }
    }

    /**
     * Linked hash set based SetMultimap implementation.
     */
    private static final class LinkedHashSetMultimap<K, V> extends AbstractMultimap<K, V, Set<V>> implements SetMultimap<K, V> {

        LinkedHashSetMultimap(Map<K, Collection<V>> map, java.util.function.Supplier<Set<V>> valueSupplier) {
            super(map, valueSupplier);
        }

        @Override
        public Set<V> get(K key) {
            return getOrCreate(key);
        }

        @Override
        public Set<V> removeAll(Object key) {
            Set<V> removed = (Set<V>) map.remove(key);
            return removed != null ? removed : new LinkedHashSet<>();
        }

        @Override
        public Set<V> replaceValues(K key, Iterable<? extends V> values) {
            Set<V> old = removeAll(key);
            for (V value : values) {
                put(key, value);
            }
            return old;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Map<K, ? extends Collection<V>> asMap() {
            Map<K, Set<V>> result = new LinkedHashMap<>();
            for (Map.Entry<K, Collection<V>> entry : map.entrySet()) {
                result.put(entry.getKey(), Collections.unmodifiableSet((Set<V>) entry.getValue()));
            }
            return Collections.unmodifiableMap(result);
        }

        @Override
        public Set<Map.Entry<K, V>> entries() {
            Set<Map.Entry<K, V>> result = new LinkedHashSet<>();
            for (Map.Entry<K, Collection<V>> entry : map.entrySet()) {
                for (V value : entry.getValue()) {
                    result.add(Map.entry(entry.getKey(), value));
                }
            }
            return Collections.unmodifiableSet(result);
        }
    }

    /**
     * Abstract base multimap implementation.
     */
    private abstract static class AbstractMultimap<K, V, C extends Collection<V>> implements Multimap<K, V> {
        protected final Map<K, Collection<V>> map;
        private final java.util.function.Supplier<? extends C> valueSupplier;

        AbstractMultimap(Map<K, Collection<V>> map, java.util.function.Supplier<? extends C> valueSupplier) {
            this.map = map;
            this.valueSupplier = valueSupplier;
        }

        @SuppressWarnings("unchecked")
        protected C getOrCreate(K key) {
            return (C) map.computeIfAbsent(key, k -> valueSupplier.get());
        }

        @Override
        public int size() {
            int size = 0;
            for (Collection<V> values : map.values()) {
                size += values.size();
            }
            return size;
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return map.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            for (Collection<V> values : map.values()) {
                if (values.contains(value)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean containsEntry(Object key, Object value) {
            Collection<V> values = map.get(key);
            return values != null && values.contains(value);
        }

        @Override
        public boolean put(K key, V value) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
            return getOrCreate(key).add(value);
        }

        @Override
        public boolean remove(Object key, Object value) {
            Collection<V> values = map.get(key);
            if (values != null && values.remove(value)) {
                if (values.isEmpty()) {
                    map.remove(key);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean putAll(K key, Iterable<? extends V> values) {
            Objects.requireNonNull(key);
            boolean changed = false;
            for (V value : values) {
                changed |= put(key, value);
            }
            return changed;
        }

        @Override
        public void putAll(Multimap<? extends K, ? extends V> multimap) {
            for (Map.Entry<? extends K, ? extends V> entry : multimap.entries()) {
                put(entry.getKey(), entry.getValue());
            }
        }

        @Override
        public void clear() {
            map.clear();
        }

        @Override
        public Set<K> keySet() {
            return Collections.unmodifiableSet(map.keySet());
        }

        @Override
        public Multiset<K> keys() {
            Multiset<K> result = HashMultiset.create();
            for (Map.Entry<K, Collection<V>> entry : map.entrySet()) {
                result.add(entry.getKey(), entry.getValue().size());
            }
            return result;
        }

        @Override
        public Collection<V> values() {
            List<V> result = new ArrayList<>();
            for (Collection<V> values : map.values()) {
                result.addAll(values);
            }
            return Collections.unmodifiableList(result);
        }

        @Override
        public Collection<Map.Entry<K, V>> entries() {
            List<Map.Entry<K, V>> result = new ArrayList<>();
            for (Map.Entry<K, Collection<V>> entry : map.entrySet()) {
                for (V value : entry.getValue()) {
                    result.add(Map.entry(entry.getKey(), value));
                }
            }
            return Collections.unmodifiableList(result);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Map<K, ? extends Collection<V>> asMap() {
            Map<K, Collection<V>> result = new LinkedHashMap<>();
            for (Map.Entry<K, Collection<V>> entry : map.entrySet()) {
                result.put(entry.getKey(), Collections.unmodifiableCollection(entry.getValue()));
            }
            return Collections.unmodifiableMap(result);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Multimap<?, ?> that)) return false;
            return asMap().equals(that.asMap());
        }

        @Override
        public int hashCode() {
            return map.hashCode();
        }

        @Override
        public String toString() {
            return map.toString();
        }
    }
}
