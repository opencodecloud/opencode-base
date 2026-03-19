package cloud.opencode.base.collections.transform;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * GroupingUtil - Collection Grouping Utilities
 * GroupingUtil - 集合分组工具
 *
 * <p>Provides utilities for grouping collection elements.</p>
 * <p>提供分组集合元素的工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Group by key function - 按键函数分组</li>
 *   <li>Multi-level grouping - 多级分组</li>
 *   <li>Index by key - 按键索引</li>
 *   <li>Unique index - 唯一索引</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<Person> people = ...;
 *
 * // Group by city - 按城市分组
 * Map<String, List<Person>> byCity = GroupingUtil.groupBy(people, Person::getCity);
 *
 * // Group by multiple keys - 按多个键分组
 * Map<String, Map<Integer, List<Person>>> byCityAndAge =
 *     GroupingUtil.groupBy(people, Person::getCity, Person::getAge);
 *
 * // Index by ID - 按 ID 索引
 * Map<Long, Person> byId = GroupingUtil.indexBy(people, Person::getId);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 是（无状态工具）</li>
 *   <li>Null-safe: No (input must not be null) - 否（输入不能为null）</li>
 * </ul>
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for groupBy, indexBy, and countBy where n is the number of elements - 时间复杂度: groupBy、indexBy、countBy 均为 O(n)，n为元素数量</li>
 *   <li>Space complexity: O(n) for the resulting map - 空间复杂度: O(n)，存储结果映射</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class GroupingUtil {

    private GroupingUtil() {
    }

    // ==================== 基本分组 | Basic Grouping ====================

    /**
     * Group items by a key function.
     * 按键函数分组项目。
     *
     * @param <T>         element type | 元素类型
     * @param <K>         key type | 键类型
     * @param items       the items | 项目
     * @param keyFunction the key function | 键函数
     * @return map of groups | 组映射
     */
    public static <T, K> Map<K, List<T>> groupBy(Iterable<T> items, Function<? super T, ? extends K> keyFunction) {
        Objects.requireNonNull(items);
        Objects.requireNonNull(keyFunction);

        Map<K, List<T>> result = new LinkedHashMap<>();
        for (T item : items) {
            K key = keyFunction.apply(item);
            result.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }
        return result;
    }

    /**
     * Group items by a key function with value transformation.
     * 按键函数分组项目并转换值。
     *
     * @param <T>           element type | 元素类型
     * @param <K>           key type | 键类型
     * @param <V>           value type | 值类型
     * @param items         the items | 项目
     * @param keyFunction   the key function | 键函数
     * @param valueFunction the value function | 值函数
     * @return map of groups | 组映射
     */
    public static <T, K, V> Map<K, List<V>> groupBy(Iterable<T> items,
                                                     Function<? super T, ? extends K> keyFunction,
                                                     Function<? super T, ? extends V> valueFunction) {
        Objects.requireNonNull(items);
        Objects.requireNonNull(keyFunction);
        Objects.requireNonNull(valueFunction);

        Map<K, List<V>> result = new LinkedHashMap<>();
        for (T item : items) {
            K key = keyFunction.apply(item);
            V value = valueFunction.apply(item);
            result.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return result;
    }

    /**
     * Group items by two levels of keys.
     * 按两级键分组项目。
     *
     * @param <T>          element type | 元素类型
     * @param <K1>         first key type | 第一个键类型
     * @param <K2>         second key type | 第二个键类型
     * @param items        the items | 项目
     * @param keyFunction1 the first key function | 第一个键函数
     * @param keyFunction2 the second key function | 第二个键函数
     * @return nested map of groups | 嵌套的组映射
     */
    public static <T, K1, K2> Map<K1, Map<K2, List<T>>> groupByNested(
            Iterable<T> items,
            Function<? super T, ? extends K1> keyFunction1,
            Function<? super T, ? extends K2> keyFunction2) {
        Objects.requireNonNull(items);
        Objects.requireNonNull(keyFunction1);
        Objects.requireNonNull(keyFunction2);

        Map<K1, Map<K2, List<T>>> result = new LinkedHashMap<>();
        for (T item : items) {
            K1 key1 = keyFunction1.apply(item);
            K2 key2 = keyFunction2.apply(item);
            result.computeIfAbsent(key1, k -> new LinkedHashMap<>())
                  .computeIfAbsent(key2, k -> new ArrayList<>())
                  .add(item);
        }
        return result;
    }

    // ==================== 索引 | Indexing ====================

    /**
     * Index items by a unique key.
     * 按唯一键索引项目。
     *
     * @param <T>         element type | 元素类型
     * @param <K>         key type | 键类型
     * @param items       the items | 项目
     * @param keyFunction the key function | 键函数
     * @return map of items by key | 按键的项目映射
     * @throws IllegalStateException if duplicate keys exist | 如果存在重复键则抛出异常
     */
    public static <T, K> Map<K, T> indexBy(Iterable<T> items, Function<? super T, ? extends K> keyFunction) {
        Objects.requireNonNull(items);
        Objects.requireNonNull(keyFunction);

        Map<K, T> result = new LinkedHashMap<>();
        for (T item : items) {
            K key = keyFunction.apply(item);
            T existing = result.put(key, item);
            if (existing != null) {
                throw new IllegalStateException("Duplicate key: " + key);
            }
        }
        return result;
    }

    /**
     * Index items by a key, keeping the first value for duplicates.
     * 按键索引项目，重复时保留第一个值。
     *
     * @param <T>         element type | 元素类型
     * @param <K>         key type | 键类型
     * @param items       the items | 项目
     * @param keyFunction the key function | 键函数
     * @return map of items by key | 按键的项目映射
     */
    public static <T, K> Map<K, T> indexByFirst(Iterable<T> items, Function<? super T, ? extends K> keyFunction) {
        Objects.requireNonNull(items);
        Objects.requireNonNull(keyFunction);

        Map<K, T> result = new LinkedHashMap<>();
        for (T item : items) {
            K key = keyFunction.apply(item);
            result.putIfAbsent(key, item);
        }
        return result;
    }

    /**
     * Index items by a key, keeping the last value for duplicates.
     * 按键索引项目，重复时保留最后一个值。
     *
     * @param <T>         element type | 元素类型
     * @param <K>         key type | 键类型
     * @param items       the items | 项目
     * @param keyFunction the key function | 键函数
     * @return map of items by key | 按键的项目映射
     */
    public static <T, K> Map<K, T> indexByLast(Iterable<T> items, Function<? super T, ? extends K> keyFunction) {
        Objects.requireNonNull(items);
        Objects.requireNonNull(keyFunction);

        Map<K, T> result = new LinkedHashMap<>();
        for (T item : items) {
            K key = keyFunction.apply(item);
            result.put(key, item);
        }
        return result;
    }

    // ==================== 计数分组 | Counting ====================

    /**
     * Count items by key.
     * 按键计数项目。
     *
     * @param <T>         element type | 元素类型
     * @param <K>         key type | 键类型
     * @param items       the items | 项目
     * @param keyFunction the key function | 键函数
     * @return map of counts | 计数映射
     */
    public static <T, K> Map<K, Long> countBy(Iterable<T> items, Function<? super T, ? extends K> keyFunction) {
        Objects.requireNonNull(items);
        Objects.requireNonNull(keyFunction);

        Map<K, Long> result = new LinkedHashMap<>();
        for (T item : items) {
            K key = keyFunction.apply(item);
            result.merge(key, 1L, Long::sum);
        }
        return result;
    }

    /**
     * Count items matching predicate.
     * 计数匹配谓词的项目。
     *
     * @param <T>       element type | 元素类型
     * @param items     the items | 项目
     * @param predicate the predicate | 谓词
     * @return count | 计数
     */
    public static <T> long count(Iterable<T> items, Predicate<? super T> predicate) {
        Objects.requireNonNull(items);
        Objects.requireNonNull(predicate);

        long count = 0;
        for (T item : items) {
            if (predicate.test(item)) {
                count++;
            }
        }
        return count;
    }

    // ==================== 频率分析 | Frequency Analysis ====================

    /**
     * Get frequency map of elements.
     * 获取元素的频率映射。
     *
     * @param <T>   element type | 元素类型
     * @param items the items | 项目
     * @return frequency map | 频率映射
     */
    public static <T> Map<T, Long> frequency(Iterable<T> items) {
        return countBy(items, Function.identity());
    }

    /**
     * Get most frequent element.
     * 获取最频繁的元素。
     *
     * @param <T>   element type | 元素类型
     * @param items the items | 项目
     * @return most frequent element or null | 最频繁的元素或 null
     */
    public static <T> T mostFrequent(Iterable<T> items) {
        Map<T, Long> freq = frequency(items);
        T result = null;
        long maxCount = 0;
        for (Map.Entry<T, Long> entry : freq.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                result = entry.getKey();
            }
        }
        return result;
    }

    /**
     * Get least frequent element.
     * 获取最不频繁的元素。
     *
     * @param <T>   element type | 元素类型
     * @param items the items | 项目
     * @return least frequent element or null | 最不频繁的元素或 null
     */
    public static <T> T leastFrequent(Iterable<T> items) {
        Map<T, Long> freq = frequency(items);
        T result = null;
        long minCount = Long.MAX_VALUE;
        for (Map.Entry<T, Long> entry : freq.entrySet()) {
            if (entry.getValue() < minCount) {
                minCount = entry.getValue();
                result = entry.getKey();
            }
        }
        return result;
    }

    // ==================== 集合分组 | Set Grouping ====================

    /**
     * Group items into sets by key.
     * 按键将项目分组为集合。
     *
     * @param <T>         element type | 元素类型
     * @param <K>         key type | 键类型
     * @param items       the items | 项目
     * @param keyFunction the key function | 键函数
     * @return map of sets | 集合映射
     */
    public static <T, K> Map<K, Set<T>> groupByToSet(Iterable<T> items, Function<? super T, ? extends K> keyFunction) {
        Objects.requireNonNull(items);
        Objects.requireNonNull(keyFunction);

        Map<K, Set<T>> result = new LinkedHashMap<>();
        for (T item : items) {
            K key = keyFunction.apply(item);
            result.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(item);
        }
        return result;
    }

    // ==================== 排序分组 | Sorted Grouping ====================

    /**
     * Group items by key with sorted keys.
     * 按键分组项目并排序键。
     *
     * @param <T>         element type | 元素类型
     * @param <K>         key type | 键类型
     * @param items       the items | 项目
     * @param keyFunction the key function | 键函数
     * @return sorted map of groups | 排序的组映射
     */
    public static <T, K extends Comparable<? super K>> Map<K, List<T>> groupBySorted(
            Iterable<T> items,
            Function<? super T, ? extends K> keyFunction) {
        Objects.requireNonNull(items);
        Objects.requireNonNull(keyFunction);

        Map<K, List<T>> result = new TreeMap<>();
        for (T item : items) {
            K key = keyFunction.apply(item);
            result.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }
        return result;
    }

    /**
     * Group items by key with custom sorted keys.
     * 按键分组项目并使用自定义排序。
     *
     * @param <T>         element type | 元素类型
     * @param <K>         key type | 键类型
     * @param items       the items | 项目
     * @param keyFunction the key function | 键函数
     * @param comparator  the comparator | 比较器
     * @return sorted map of groups | 排序的组映射
     */
    public static <T, K> Map<K, List<T>> groupBySorted(
            Iterable<T> items,
            Function<? super T, ? extends K> keyFunction,
            Comparator<? super K> comparator) {
        Objects.requireNonNull(items);
        Objects.requireNonNull(keyFunction);
        Objects.requireNonNull(comparator);

        Map<K, List<T>> result = new TreeMap<>(comparator);
        for (T item : items) {
            K key = keyFunction.apply(item);
            result.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }
        return result;
    }
}
