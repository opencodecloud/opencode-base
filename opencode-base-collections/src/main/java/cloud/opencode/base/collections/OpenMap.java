package cloud.opencode.base.collections;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * OpenMap - Map Facade Utility Class
 * OpenMap - Map 门面工具类
 *
 * <p>Provides comprehensive map operations including creation, transformation,
 * filtering, and querying.</p>
 * <p>提供全面的 Map 操作，包括创建、转换、过滤和查询。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Factory methods for map creation - Map 创建工厂方法</li>
 *   <li>Map transformation - Map 转换</li>
 *   <li>Map filtering - Map 过滤</li>
 *   <li>Map difference computation - Map 差异计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create map - 创建 Map
 * Map<String, Integer> map = OpenMap.of("a", 1, "b", 2);
 *
 * // Filter by keys - 按键过滤
 * Map<String, Integer> filtered = OpenMap.filterKeys(map, k -> k.startsWith("a"));
 *
 * // Transform values - 转换值
 * Map<String, String> transformed = OpenMap.transformValues(map, Object::toString);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Most operations: O(n) - 大多数操作: O(n)</li>
 *   <li>Single key operations: O(1) - 单键操作: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (use ConcurrentMap methods for thread safety) - 线程安全: 否</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class OpenMap {

    private OpenMap() {
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty HashMap.
     * 创建空的 HashMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new HashMap | 新的 HashMap
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }

    /**
     * Create a HashMap with one entry.
     * 创建包含一个条目的 HashMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param k1  key | 键
     * @param v1  value | 值
     * @return new HashMap | 新的 HashMap
     */
    public static <K, V> HashMap<K, V> of(K k1, V v1) {
        HashMap<K, V> map = new HashMap<>(2);
        map.put(k1, v1);
        return map;
    }

    /**
     * Create a HashMap with two entries.
     * 创建包含两个条目的 HashMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param k1  first key | 第一个键
     * @param v1  first value | 第一个值
     * @param k2  second key | 第二个键
     * @param v2  second value | 第二个值
     * @return new HashMap | 新的 HashMap
     */
    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2) {
        HashMap<K, V> map = new HashMap<>(3);
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    /**
     * Create a HashMap with three entries.
     * 创建包含三个条目的 HashMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param k1  first key | 第一个键
     * @param v1  first value | 第一个值
     * @param k2  second key | 第二个键
     * @param v2  second value | 第二个值
     * @param k3  third key | 第三个键
     * @param v3  third value | 第三个值
     * @return new HashMap | 新的 HashMap
     */
    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        HashMap<K, V> map = new HashMap<>(4);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }

    /**
     * Create a HashMap from another map.
     * 从另一个 Map 创建 HashMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param map source map | 源 Map
     * @return new HashMap | 新的 HashMap
     */
    public static <K, V> HashMap<K, V> from(Map<? extends K, ? extends V> map) {
        return map == null ? new HashMap<>() : new HashMap<>(map);
    }

    /**
     * Create a HashMap with expected size.
     * 创建具有预期大小的 HashMap。
     *
     * @param <K>          key type | 键类型
     * @param <V>          value type | 值类型
     * @param expectedSize expected size | 预期大小
     * @return new HashMap | 新的 HashMap
     */
    public static <K, V> HashMap<K, V> withExpectedSize(int expectedSize) {
        return new HashMap<>(MapUtil.capacity(expectedSize));
    }

    /**
     * Create an empty LinkedHashMap.
     * 创建空的 LinkedHashMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new LinkedHashMap | 新的 LinkedHashMap
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    /**
     * Create an empty TreeMap.
     * 创建空的 TreeMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new TreeMap | 新的 TreeMap
     */
    public static <K extends Comparable<K>, V> TreeMap<K, V> newTreeMap() {
        return new TreeMap<>();
    }

    /**
     * Create a TreeMap with comparator.
     * 创建具有比较器的 TreeMap。
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
     * Create an empty ConcurrentHashMap.
     * 创建空的 ConcurrentHashMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new ConcurrentHashMap | 新的 ConcurrentHashMap
     */
    public static <K, V> ConcurrentMap<K, V> newConcurrentMap() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Create an empty IdentityHashMap.
     * 创建空的 IdentityHashMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new IdentityHashMap | 新的 IdentityHashMap
     */
    public static <K, V> IdentityHashMap<K, V> newIdentityHashMap() {
        return new IdentityHashMap<>();
    }

    // ==================== 转换操作 | Transformation Operations ====================

    /**
     * Transform values in the map.
     * 转换 Map 中的值。
     *
     * @param <K>      key type | 键类型
     * @param <V1>     input value type | 输入值类型
     * @param <V2>     output value type | 输出值类型
     * @param map      the map | Map
     * @param function the transform function | 转换函数
     * @return new map with transformed values | 包含转换后值的新 Map
     */
    public static <K, V1, V2> Map<K, V2> transformValues(Map<K, V1> map,
                                                          Function<? super V1, V2> function) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<K, V2> result = new LinkedHashMap<>(map.size());
        for (Map.Entry<K, V1> entry : map.entrySet()) {
            result.put(entry.getKey(), function.apply(entry.getValue()));
        }
        return result;
    }

    /**
     * Transform entries in the map.
     * 转换 Map 中的条目。
     *
     * @param <K>         key type | 键类型
     * @param <V1>        input value type | 输入值类型
     * @param <V2>        output value type | 输出值类型
     * @param map         the map | Map
     * @param transformer the entry transformer | 条目转换器
     * @return new map with transformed entries | 包含转换后条目的新 Map
     */
    public static <K, V1, V2> Map<K, V2> transformEntries(Map<K, V1> map,
                                                           EntryTransformer<? super K, ? super V1, V2> transformer) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<K, V2> result = new LinkedHashMap<>(map.size());
        for (Map.Entry<K, V1> entry : map.entrySet()) {
            result.put(entry.getKey(), transformer.transformEntry(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    // ==================== 过滤操作 | Filter Operations ====================

    /**
     * Filter map by keys.
     * 按键过滤 Map。
     *
     * @param <K>       key type | 键类型
     * @param <V>       value type | 值类型
     * @param map       the map | Map
     * @param predicate the key predicate | 键谓词
     * @return filtered map | 过滤后的 Map
     */
    public static <K, V> Map<K, V> filterKeys(Map<K, V> map, Predicate<? super K> predicate) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (predicate.test(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Filter map by values.
     * 按值过滤 Map。
     *
     * @param <K>       key type | 键类型
     * @param <V>       value type | 值类型
     * @param map       the map | Map
     * @param predicate the value predicate | 值谓词
     * @return filtered map | 过滤后的 Map
     */
    public static <K, V> Map<K, V> filterValues(Map<K, V> map, Predicate<? super V> predicate) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (predicate.test(entry.getValue())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Filter map by entries.
     * 按条目过滤 Map。
     *
     * @param <K>       key type | 键类型
     * @param <V>       value type | 值类型
     * @param map       the map | Map
     * @param predicate the entry predicate | 条目谓词
     * @return filtered map | 过滤后的 Map
     */
    public static <K, V> Map<K, V> filterEntries(Map<K, V> map,
                                                  Predicate<? super Map.Entry<K, V>> predicate) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (predicate.test(entry)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    // ==================== 索引操作 | Index Operations ====================

    /**
     * Create a map indexed by unique key.
     * 按唯一键创建索引 Map。
     *
     * @param <K>         key type | 键类型
     * @param <V>         value type | 值类型
     * @param values      the values | 值
     * @param keyFunction the key function | 键函数
     * @return indexed map | 索引 Map
     * @throws IllegalArgumentException if duplicate keys | 如果有重复键
     */
    public static <K, V> Map<K, V> uniqueIndex(Iterable<V> values,
                                                Function<? super V, K> keyFunction) {
        Map<K, V> result = new LinkedHashMap<>();
        for (V value : values) {
            K key = keyFunction.apply(value);
            V existing = result.put(key, value);
            if (existing != null) {
                throw new IllegalArgumentException("Duplicate key: " + key);
            }
        }
        return result;
    }

    // ==================== 查询操作 | Query Operations ====================

    /**
     * Get value safely with default.
     * 安全获取值，带默认值。
     *
     * @param <K>          key type | 键类型
     * @param <V>          value type | 值类型
     * @param map          the map | Map
     * @param key          the key | 键
     * @param defaultValue default value | 默认值
     * @return value or default | 值或默认值
     */
    public static <K, V> V getOrDefault(Map<K, V> map, K key, V defaultValue) {
        if (map == null) {
            return defaultValue;
        }
        return map.getOrDefault(key, defaultValue);
    }

    /**
     * Check if map contains all keys.
     * 检查 Map 是否包含所有键。
     *
     * @param <K>  key type | 键类型
     * @param map  the map | Map
     * @param keys the keys | 键
     * @return true if all keys present | 如果所有键都存在则返回 true
     */
    @SafeVarargs
    public static <K> boolean containsAllKeys(Map<K, ?> map, K... keys) {
        if (map == null) {
            return false;
        }
        for (K key : keys) {
            if (!map.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create an immutable entry.
     * 创建不可变条目。
     *
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @param key   the key | 键
     * @param value the value | 值
     * @return immutable entry | 不可变条目
     */
    public static <K, V> Map.Entry<K, V> immutableEntry(K key, V value) {
        return Map.entry(key, value);
    }
}
