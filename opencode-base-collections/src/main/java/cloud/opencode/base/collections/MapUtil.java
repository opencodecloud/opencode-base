package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * MapUtil - Map Utility Class
 * MapUtil - 映射工具类
 *
 * <p>Provides factory methods and operations for Map implementations including
 * HashMap, LinkedHashMap, TreeMap, and ConcurrentMap.</p>
 * <p>提供 Map 实现的工厂方法和操作，包括 HashMap、LinkedHashMap、TreeMap 和 ConcurrentMap。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Factory methods for map creation - Map 创建工厂方法</li>
 *   <li>Map difference calculation - Map 差异计算</li>
 *   <li>Transformation views - 转换视图</li>
 *   <li>Filtering views - 过滤视图</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create HashMap - 创建 HashMap
 * Map<String, Integer> map = MapUtil.newHashMap();
 *
 * // Unique index - 唯一索引
 * Map<String, User> byId = MapUtil.uniqueIndex(users, User::getId);
 *
 * // Map difference - Map 差异
 * MapDifference<String, Integer> diff = MapUtil.difference(map1, map2);
 *
 * // Transform values - 转换值
 * Map<String, String> stringValues = MapUtil.transformValues(map, Object::toString);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Factory methods: O(1) or O(n) - 工厂方法: O(1) 或 O(n)</li>
 *   <li>uniqueIndex: O(n) - uniqueIndex: O(n)</li>
 *   <li>difference: O(n + m) - difference: O(n + m)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (except ConcurrentMap) - 线程安全: 否（ConcurrentMap 除外）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class MapUtil {

    private MapUtil() {
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create a new HashMap
     * 创建 HashMap
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new HashMap | 新的 HashMap
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }

    /**
     * Create a new HashMap from an existing Map
     * 从 Map 创建 HashMap
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param map source map | 源 Map
     * @return new HashMap | 新的 HashMap
     */
    public static <K, V> HashMap<K, V> newHashMap(Map<? extends K, ? extends V> map) {
        return map == null ? new HashMap<>() : new HashMap<>(map);
    }

    /**
     * Create a new HashMap with expected size
     * 创建指定容量的 HashMap
     *
     * @param <K>          key type | 键类型
     * @param <V>          value type | 值类型
     * @param expectedSize expected size | 预期大小
     * @return new HashMap | 新的 HashMap
     */
    public static <K, V> HashMap<K, V> newHashMapWithExpectedSize(int expectedSize) {
        if (expectedSize < 0) {
            throw new IllegalArgumentException("Expected size cannot be negative: " + expectedSize);
        }
        return HashMap.newHashMap(expectedSize);
    }

    /**
     * Create a new LinkedHashMap
     * 创建 LinkedHashMap
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new LinkedHashMap | 新的 LinkedHashMap
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    /**
     * Create a new LinkedHashMap with expected size
     * 创建指定容量的 LinkedHashMap
     *
     * @param <K>          key type | 键类型
     * @param <V>          value type | 值类型
     * @param expectedSize expected size | 预期大小
     * @return new LinkedHashMap | 新的 LinkedHashMap
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMapWithExpectedSize(int expectedSize) {
        if (expectedSize < 0) {
            throw new IllegalArgumentException("Expected size cannot be negative: " + expectedSize);
        }
        return LinkedHashMap.newLinkedHashMap(expectedSize);
    }

    /**
     * Create a new TreeMap
     * 创建 TreeMap
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new TreeMap | 新的 TreeMap
     */
    public static <K extends Comparable<K>, V> TreeMap<K, V> newTreeMap() {
        return new TreeMap<>();
    }

    /**
     * Create a new TreeMap with comparator
     * 带比较器的 TreeMap
     *
     * @param <K>        key type | 键类型
     * @param <V>        value type | 值类型
     * @param comparator the comparator | 比较器
     * @return new TreeMap | 新的 TreeMap
     */
    public static <K, V> TreeMap<K, V> newTreeMap(Comparator<? super K> comparator) {
        return new TreeMap<>(comparator);
    }

    /**
     * Create a new ConcurrentMap
     * 创建 ConcurrentMap
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new ConcurrentMap | 新的 ConcurrentMap
     */
    public static <K, V> ConcurrentMap<K, V> newConcurrentMap() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Create a new IdentityHashMap
     * 创建 IdentityHashMap
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new IdentityHashMap | 新的 IdentityHashMap
     */
    public static <K, V> IdentityHashMap<K, V> newIdentityHashMap() {
        return new IdentityHashMap<>();
    }

    // ==================== 唯一索引 | Unique Index ====================

    /**
     * Create a map indexed by unique keys extracted from values
     * 按唯一键索引
     *
     * @param <K>         key type | 键类型
     * @param <V>         value type | 值类型
     * @param values      the values | 值
     * @param keyFunction the key extractor | 键提取器
     * @return indexed map | 索引后的 Map
     * @throws OpenCollectionException if duplicate keys | 如果有重复键
     */
    public static <K, V> Map<K, V> uniqueIndex(Iterable<V> values,
                                                Function<? super V, K> keyFunction) {
        if (values == null || keyFunction == null) {
            return Collections.emptyMap();
        }
        Map<K, V> result = new LinkedHashMap<>();
        for (V value : values) {
            K key = keyFunction.apply(value);
            if (result.containsKey(key)) {
                throw new OpenCollectionException("Duplicate key: " + key);
            }
            result.put(key, value);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Create a map indexed by unique keys extracted from values
     * 按唯一键索引（Iterator）
     *
     * @param <K>         key type | 键类型
     * @param <V>         value type | 值类型
     * @param values      the values | 值
     * @param keyFunction the key extractor | 键提取器
     * @return indexed map | 索引后的 Map
     * @throws OpenCollectionException if duplicate keys | 如果有重复键
     */
    public static <K, V> Map<K, V> uniqueIndex(Iterator<V> values,
                                                Function<? super V, K> keyFunction) {
        if (values == null || keyFunction == null) {
            return Collections.emptyMap();
        }
        Map<K, V> result = new LinkedHashMap<>();
        while (values.hasNext()) {
            V value = values.next();
            K key = keyFunction.apply(value);
            if (result.containsKey(key)) {
                throw new OpenCollectionException("Duplicate key: " + key);
            }
            result.put(key, value);
        }
        return Collections.unmodifiableMap(result);
    }

    // ==================== Map 差异 | Map Difference ====================

    /**
     * Compute the difference between two maps
     * 计算两个 Map 的差异
     *
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @param left  left map | 左 Map
     * @param right right map | 右 Map
     * @return map difference | Map 差异
     */
    public static <K, V> MapDifference<K, V> difference(Map<? extends K, ? extends V> left,
                                                         Map<? extends K, ? extends V> right) {
        Map<K, V> onlyOnLeft = new LinkedHashMap<>();
        Map<K, V> onlyOnRight = new LinkedHashMap<>();
        Map<K, V> inCommon = new LinkedHashMap<>();
        Map<K, MapDifference.ValueDifference<V>> differing = new LinkedHashMap<>();

        if (left == null) {
            left = Collections.emptyMap();
        }
        if (right == null) {
            right = Collections.emptyMap();
        }

        for (Map.Entry<? extends K, ? extends V> entry : left.entrySet()) {
            K key = entry.getKey();
            V leftValue = entry.getValue();
            if (right.containsKey(key)) {
                V rightValue = right.get(key);
                if (Objects.equals(leftValue, rightValue)) {
                    inCommon.put(key, leftValue);
                } else {
                    differing.put(key, new ValueDifferenceImpl<>(leftValue, rightValue));
                }
            } else {
                onlyOnLeft.put(key, leftValue);
            }
        }

        for (Map.Entry<? extends K, ? extends V> entry : right.entrySet()) {
            K key = entry.getKey();
            if (!left.containsKey(key)) {
                onlyOnRight.put(key, entry.getValue());
            }
        }

        return new MapDifferenceImpl<>(onlyOnLeft, onlyOnRight, inCommon, differing);
    }

    // ==================== 转换视图 | Transformation Views ====================

    /**
     * Return a view with transformed values
     * 转换值视图
     *
     * @param <K>      key type | 键类型
     * @param <V1>     original value type | 原值类型
     * @param <V2>     transformed value type | 转换后值类型
     * @param fromMap  source map | 源 Map
     * @param function transform function | 转换函数
     * @return transformed view | 转换视图
     */
    public static <K, V1, V2> Map<K, V2> transformValues(Map<K, V1> fromMap,
                                                          Function<? super V1, V2> function) {
        if (fromMap == null || function == null) {
            return Collections.emptyMap();
        }
        return new TransformedValuesMap<>(fromMap, function);
    }

    /**
     * Return a view with transformed entries
     * 转换条目视图
     *
     * @param <K>         key type | 键类型
     * @param <V1>        original value type | 原值类型
     * @param <V2>        transformed value type | 转换后值类型
     * @param fromMap     source map | 源 Map
     * @param transformer entry transformer | 条目转换器
     * @return transformed view | 转换视图
     */
    public static <K, V1, V2> Map<K, V2> transformEntries(Map<K, V1> fromMap,
                                                           EntryTransformer<? super K, ? super V1, V2> transformer) {
        if (fromMap == null || transformer == null) {
            return Collections.emptyMap();
        }
        return new TransformedEntriesMap<>(fromMap, transformer);
    }

    // ==================== 过滤视图 | Filtering Views ====================

    /**
     * Return a view filtered by keys
     * 过滤键视图
     *
     * @param <K>          key type | 键类型
     * @param <V>          value type | 值类型
     * @param unfiltered   source map | 源 Map
     * @param keyPredicate key predicate | 键谓词
     * @return filtered view | 过滤视图
     */
    public static <K, V> Map<K, V> filterKeys(Map<K, V> unfiltered, Predicate<? super K> keyPredicate) {
        if (unfiltered == null || keyPredicate == null) {
            return Collections.emptyMap();
        }
        return filterEntries(unfiltered, entry -> keyPredicate.test(entry.getKey()));
    }

    /**
     * Return a view filtered by values
     * 过滤值视图
     *
     * @param <K>            key type | 键类型
     * @param <V>            value type | 值类型
     * @param unfiltered     source map | 源 Map
     * @param valuePredicate value predicate | 值谓词
     * @return filtered view | 过滤视图
     */
    public static <K, V> Map<K, V> filterValues(Map<K, V> unfiltered, Predicate<? super V> valuePredicate) {
        if (unfiltered == null || valuePredicate == null) {
            return Collections.emptyMap();
        }
        return filterEntries(unfiltered, entry -> valuePredicate.test(entry.getValue()));
    }

    /**
     * Return a view filtered by entries
     * 过滤条目视图
     *
     * @param <K>            key type | 键类型
     * @param <V>            value type | 值类型
     * @param unfiltered     source map | 源 Map
     * @param entryPredicate entry predicate | 条目谓词
     * @return filtered view | 过滤视图
     */
    public static <K, V> Map<K, V> filterEntries(Map<K, V> unfiltered,
                                                  Predicate<? super Map.Entry<K, V>> entryPredicate) {
        if (unfiltered == null || entryPredicate == null) {
            return Collections.emptyMap();
        }
        return new FilteredMap<>(unfiltered, entryPredicate);
    }

    // ==================== 辅助方法 | Helper Methods ====================

    /**
     * Create a Properties object to Map
     * 从属性创建 Map
     *
     * @param properties the properties | 属性
     * @return map | Map
     */
    public static Map<String, String> fromProperties(Properties properties) {
        if (properties == null) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (String name : properties.stringPropertyNames()) {
            result.put(name, properties.getProperty(name));
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Create an immutable entry
     * 创建不可变条目
     *
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @param key   the key | 键
     * @param value the value | 值
     * @return immutable entry | 不可变条目
     */
    public static <K, V> Map.Entry<K, V> immutableEntry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    /**
     * Calculate capacity for expected size
     * 计算 HashMap 所需容量
     *
     * @param expectedSize expected size | 预期大小
     * @return capacity | 容量
     */
    public static int capacity(int expectedSize) {
        if (expectedSize < 3) {
            return expectedSize + 1;
        }
        return (int) Math.ceil(expectedSize / 0.75);
    }

    // ==================== BiMap 包装方法 | BiMap Wrapper Methods ====================

    /**
     * Create a synchronized BiMap wrapper.
     * 创建同步的 BiMap 包装器。
     *
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @param bimap the BiMap to synchronize | 要同步的 BiMap
     * @return synchronized BiMap | 同步的 BiMap
     */
    public static <K, V> BiMap<K, V> synchronizedBiMap(BiMap<K, V> bimap) {
        return new SynchronizedBiMap<>(bimap);
    }

    /**
     * Create an unmodifiable BiMap wrapper.
     * 创建不可修改的 BiMap 包装器。
     *
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @param bimap the BiMap to wrap | 要包装的 BiMap
     * @return unmodifiable BiMap | 不可修改的 BiMap
     */
    public static <K, V> BiMap<K, V> unmodifiableBiMap(BiMap<? extends K, ? extends V> bimap) {
        return new UnmodifiableBiMap<>(bimap);
    }

    /**
     * Calculate the difference between two maps using custom value equivalence.
     * 使用自定义值等价关系计算两个 Map 的差异。
     *
     * @param <K>              key type | 键类型
     * @param <V>              value type | 值类型
     * @param left             left map | 左 Map
     * @param right            right map | 右 Map
     * @param valueEquivalence value equivalence | 值等价关系
     * @return map difference | Map 差异
     */
    public static <K, V> MapDifference<K, V> difference(Map<? extends K, ? extends V> left,
                                                         Map<? extends K, ? extends V> right,
                                                         Equivalence<? super V> valueEquivalence) {
        Map<K, V> onlyOnLeft = new LinkedHashMap<>();
        Map<K, V> onlyOnRight = new LinkedHashMap<>(right);
        Map<K, V> inCommon = new LinkedHashMap<>();
        Map<K, MapDifference.ValueDifference<V>> differing = new LinkedHashMap<>();

        for (Map.Entry<? extends K, ? extends V> entry : left.entrySet()) {
            K key = entry.getKey();
            V leftValue = entry.getValue();

            if (right.containsKey(key)) {
                @SuppressWarnings("unchecked")
                V rightValue = (V) right.get(key);
                onlyOnRight.remove(key);

                if (valueEquivalence.equivalent(leftValue, rightValue)) {
                    inCommon.put(key, leftValue);
                } else {
                    differing.put(key, new ValueDifferenceImpl<>(leftValue, rightValue));
                }
            } else {
                onlyOnLeft.put(key, leftValue);
            }
        }

        return new MapDifferenceImpl<>(
                Collections.unmodifiableMap(onlyOnLeft),
                Collections.unmodifiableMap(onlyOnRight),
                Collections.unmodifiableMap(inCommon),
                Collections.unmodifiableMap(differing)
        );
    }

    // ==================== 接口和内部类 | Interfaces and Internal Classes ====================

    /**
     * Entry transformer interface
     * 条目转换器
     *
     * @param <K>  key type | 键类型
     * @param <V1> original value type | 原值类型
     * @param <V2> transformed value type | 转换后值类型
     */
    @FunctionalInterface
    public interface EntryTransformer<K, V1, V2> {
        /**
         * Transform an entry
         * 转换条目
         *
         * @param key   the key | 键
         * @param value the value | 值
         * @return transformed value | 转换后的值
         */
        V2 transformEntry(K key, V1 value);
    }

    /**
     * MapDifference implementation
     */
    private record MapDifferenceImpl<K, V>(
            Map<K, V> onlyOnLeft,
            Map<K, V> onlyOnRight,
            Map<K, V> inCommon,
            Map<K, MapDifference.ValueDifference<V>> differing
    ) implements MapDifference<K, V> {

        MapDifferenceImpl {
            onlyOnLeft = Collections.unmodifiableMap(onlyOnLeft);
            onlyOnRight = Collections.unmodifiableMap(onlyOnRight);
            inCommon = Collections.unmodifiableMap(inCommon);
            differing = Collections.unmodifiableMap(differing);
        }

        @Override
        public boolean areEqual() {
            return onlyOnLeft.isEmpty() && onlyOnRight.isEmpty() && differing.isEmpty();
        }

        @Override
        public Map<K, V> entriesOnlyOnLeft() {
            return onlyOnLeft;
        }

        @Override
        public Map<K, V> entriesOnlyOnRight() {
            return onlyOnRight;
        }

        @Override
        public Map<K, V> entriesInCommon() {
            return inCommon;
        }

        @Override
        public Map<K, MapDifference.ValueDifference<V>> entriesDiffering() {
            return differing;
        }
    }

    /**
     * ValueDifference implementation
     */
    private record ValueDifferenceImpl<V>(V leftValue, V rightValue) implements MapDifference.ValueDifference<V> {
    }

    /**
     * Transformed values map view
     */
    private static class TransformedValuesMap<K, V1, V2> extends AbstractMap<K, V2> {
        private final Map<K, V1> fromMap;
        private final Function<? super V1, V2> function;

        TransformedValuesMap(Map<K, V1> fromMap, Function<? super V1, V2> function) {
            this.fromMap = fromMap;
            this.function = function;
        }

        @Override
        public Set<Entry<K, V2>> entrySet() {
            return new AbstractSet<>() {
                @Override
                public Iterator<Entry<K, V2>> iterator() {
                    return IteratorUtil.transform(fromMap.entrySet().iterator(),
                            entry -> immutableEntry(entry.getKey(), function.apply(entry.getValue())));
                }

                @Override
                public int size() {
                    return fromMap.size();
                }
            };
        }

        @Override
        public V2 get(Object key) {
            V1 value = fromMap.get(key);
            return value == null && !fromMap.containsKey(key) ? null : function.apply(value);
        }

        @Override
        public boolean containsKey(Object key) {
            return fromMap.containsKey(key);
        }

        @Override
        public int size() {
            return fromMap.size();
        }
    }

    /**
     * Transformed entries map view
     */
    private static class TransformedEntriesMap<K, V1, V2> extends AbstractMap<K, V2> {
        private final Map<K, V1> fromMap;
        private final EntryTransformer<? super K, ? super V1, V2> transformer;

        TransformedEntriesMap(Map<K, V1> fromMap, EntryTransformer<? super K, ? super V1, V2> transformer) {
            this.fromMap = fromMap;
            this.transformer = transformer;
        }

        @Override
        public Set<Entry<K, V2>> entrySet() {
            return new AbstractSet<>() {
                @Override
                public Iterator<Entry<K, V2>> iterator() {
                    return IteratorUtil.transform(fromMap.entrySet().iterator(),
                            entry -> immutableEntry(entry.getKey(),
                                    transformer.transformEntry(entry.getKey(), entry.getValue())));
                }

                @Override
                public int size() {
                    return fromMap.size();
                }
            };
        }

        @Override
        @SuppressWarnings("unchecked")
        public V2 get(Object key) {
            V1 value = fromMap.get(key);
            return value == null && !fromMap.containsKey(key) ? null :
                    transformer.transformEntry((K) key, value);
        }

        @Override
        public boolean containsKey(Object key) {
            return fromMap.containsKey(key);
        }

        @Override
        public int size() {
            return fromMap.size();
        }
    }

    /**
     * Filtered map view
     */
    private static class FilteredMap<K, V> extends AbstractMap<K, V> {
        private final Map<K, V> unfiltered;
        private final Predicate<? super Entry<K, V>> predicate;

        FilteredMap(Map<K, V> unfiltered, Predicate<? super Entry<K, V>> predicate) {
            this.unfiltered = unfiltered;
            this.predicate = predicate;
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return new AbstractSet<>() {
                @Override
                public Iterator<Entry<K, V>> iterator() {
                    return IteratorUtil.filter(unfiltered.entrySet().iterator(), predicate);
                }

                @Override
                public int size() {
                    int size = 0;
                    for (Entry<K, V> entry : unfiltered.entrySet()) {
                        if (predicate.test(entry)) {
                            size++;
                        }
                    }
                    return size;
                }
            };
        }

        @Override
        public V get(Object key) {
            V value = unfiltered.get(key);
            if (value == null && !unfiltered.containsKey(key)) {
                return null;
            }
            @SuppressWarnings("unchecked")
            Entry<K, V> entry = immutableEntry((K) key, value);
            return predicate.test(entry) ? value : null;
        }

        @Override
        public boolean containsKey(Object key) {
            if (!unfiltered.containsKey(key)) {
                return false;
            }
            V value = unfiltered.get(key);
            @SuppressWarnings("unchecked")
            Entry<K, V> entry = immutableEntry((K) key, value);
            return predicate.test(entry);
        }

        @Override
        public int size() {
            return entrySet().size();
        }
    }

    /**
     * Synchronized BiMap wrapper
     */
    private static class SynchronizedBiMap<K, V> implements BiMap<K, V> {
        private final BiMap<K, V> delegate;
        private final Object mutex;
        private volatile BiMap<V, K> inverse;

        SynchronizedBiMap(BiMap<K, V> delegate) {
            this.delegate = Objects.requireNonNull(delegate);
            this.mutex = this;
        }

        @Override
        public V put(K key, V value) {
            synchronized (mutex) {
                return delegate.put(key, value);
            }
        }

        @Override
        public V forcePut(K key, V value) {
            synchronized (mutex) {
                return delegate.forcePut(key, value);
            }
        }

        @Override
        public BiMap<V, K> inverse() {
            if (inverse == null) {
                synchronized (mutex) {
                    if (inverse == null) {
                        inverse = new SynchronizedBiMap<>(delegate.inverse());
                    }
                }
            }
            return inverse;
        }

        @Override
        public int size() {
            synchronized (mutex) {
                return delegate.size();
            }
        }

        @Override
        public boolean isEmpty() {
            synchronized (mutex) {
                return delegate.isEmpty();
            }
        }

        @Override
        public boolean containsKey(Object key) {
            synchronized (mutex) {
                return delegate.containsKey(key);
            }
        }

        @Override
        public boolean containsValue(Object value) {
            synchronized (mutex) {
                return delegate.containsValue(value);
            }
        }

        @Override
        public V get(Object key) {
            synchronized (mutex) {
                return delegate.get(key);
            }
        }

        @Override
        public V remove(Object key) {
            synchronized (mutex) {
                return delegate.remove(key);
            }
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            synchronized (mutex) {
                delegate.putAll(m);
            }
        }

        @Override
        public void clear() {
            synchronized (mutex) {
                delegate.clear();
            }
        }

        @Override
        public Set<K> keySet() {
            synchronized (mutex) {
                return Collections.synchronizedSet(delegate.keySet());
            }
        }

        @Override
        public Set<V> values() {
            synchronized (mutex) {
                return Collections.synchronizedSet(delegate.values());
            }
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            synchronized (mutex) {
                return Collections.synchronizedSet(delegate.entrySet());
            }
        }
    }

    /**
     * Unmodifiable BiMap wrapper
     */
    private static class UnmodifiableBiMap<K, V> implements BiMap<K, V> {
        private final BiMap<? extends K, ? extends V> delegate;
        private volatile BiMap<V, K> inverse;

        @SuppressWarnings("unchecked")
        UnmodifiableBiMap(BiMap<? extends K, ? extends V> delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        @Override
        public V put(K key, V value) {
            throw new UnsupportedOperationException("Unmodifiable BiMap");
        }

        @Override
        public V forcePut(K key, V value) {
            throw new UnsupportedOperationException("Unmodifiable BiMap");
        }

        @Override
        @SuppressWarnings("unchecked")
        public BiMap<V, K> inverse() {
            if (inverse == null) {
                inverse = new UnmodifiableBiMap<>((BiMap<V, K>) ((BiMap<?, ?>) delegate).inverse());
            }
            return inverse;
        }

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
            return delegate.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return delegate.containsValue(value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public V get(Object key) {
            return (V) delegate.get(key);
        }

        @Override
        public V remove(Object key) {
            throw new UnsupportedOperationException("Unmodifiable BiMap");
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            throw new UnsupportedOperationException("Unmodifiable BiMap");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Unmodifiable BiMap");
        }

        @Override
        @SuppressWarnings("unchecked")
        public Set<K> keySet() {
            return Collections.unmodifiableSet((Set<K>) delegate.keySet());
        }

        @Override
        @SuppressWarnings("unchecked")
        public Set<V> values() {
            return Collections.unmodifiableSet((Set<V>) delegate.values());
        }

        @Override
        @SuppressWarnings("unchecked")
        public Set<Entry<K, V>> entrySet() {
            return Collections.unmodifiableSet((Set<Entry<K, V>>) (Set<?>) delegate.entrySet());
        }
    }
}
