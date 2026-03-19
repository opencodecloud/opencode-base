package cloud.opencode.base.collections.immutable;

import cloud.opencode.base.collections.ImmutableList;
import cloud.opencode.base.collections.ImmutableSet;
import cloud.opencode.base.collections.exception.OpenCollectionException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ImmutableCollectionUtil - Utility Class for Immutable Collections
 * ImmutableCollectionUtil - 不可变集合工具类
 *
 * <p>Provides utility methods for working with immutable collections, including
 * transformation, combination, and conversion operations.</p>
 * <p>提供用于处理不可变集合的工具方法，包括转换、组合和转换操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Collection transformation - 集合转换</li>
 *   <li>Collection combination - 集合组合</li>
 *   <li>Type conversion - 类型转换</li>
 *   <li>Filtering and mapping - 过滤和映射</li>
 *   <li>Null-safe operations - 空值安全操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Transform list - 转换列表
 * ImmutableList<String> strings = ImmutableList.of("1", "2", "3");
 * ImmutableList<Integer> integers = ImmutableCollectionUtil.transform(strings, Integer::parseInt);
 *
 * // Combine lists - 组合列表
 * ImmutableList<String> list1 = ImmutableList.of("a", "b");
 * ImmutableList<String> list2 = ImmutableList.of("c", "d");
 * ImmutableList<String> combined = ImmutableCollectionUtil.concat(list1, list2);
 *
 * // Convert to multiset - 转换为多重集
 * ImmutableList<String> list = ImmutableList.of("a", "b", "a", "c");
 * ImmutableMultiset<String> multiset = ImmutableCollectionUtil.toMultiset(list);
 *
 * // Create BiMap from map - 从映射创建双向映射
 * Map<String, Integer> map = Map.of("one", 1, "two", 2);
 * ImmutableBiMap<String, Integer> bimap = ImmutableCollectionUtil.toBiMap(map);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Most operations: O(n) where n is input size - 大多数操作: O(n)，其中n是输入大小</li>
 *   <li>Null checks: O(1) - 空值检查: O(1)</li>
 *   <li>Type conversions: O(n) - 类型转换: O(n)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (all methods) - 线程安全: 是（所有方法）</li>
 *   <li>Null-safe: Yes (all methods) - 空值安全: 是（所有方法）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class ImmutableCollectionUtil {

    // ==================== 构造方法 | Constructor ====================

    /**
     * Private constructor to prevent instantiation.
     * 私有构造方法以防止实例化。
     */
    private ImmutableCollectionUtil() {
        throw new AssertionError("No ImmutableCollectionUtil instances for you!");
    }

    // ==================== List 操作 | List Operations ====================

    /**
     * Transform a list using a mapping function.
     * 使用映射函数转换列表。
     *
     * @param <T>      source element type | 源元素类型
     * @param <R>      target element type | 目标元素类型
     * @param list     the list to transform | 要转换的列表
     * @param function the mapping function | 映射函数
     * @return transformed immutable list | 转换后的不可变列表
     */
    public static <T, R> ImmutableList<R> transform(ImmutableList<T> list, Function<? super T, ? extends R> function) {
        Objects.requireNonNull(function, "Function cannot be null");
        if (list == null || list.isEmpty()) {
            return ImmutableList.of();
        }
        return ImmutableList.copyOf(
                list.stream()
                        .map(function)
                        .toList()
        );
    }

    /**
     * Concatenate multiple lists into one.
     * 将多个列表连接为一个。
     *
     * @param <E>   element type | 元素类型
     * @param lists the lists to concatenate | 要连接的列表
     * @return concatenated immutable list | 连接后的不可变列表
     */
    @SafeVarargs
    public static <E> ImmutableList<E> concat(ImmutableList<? extends E>... lists) {
        if (lists == null || lists.length == 0) {
            return ImmutableList.of();
        }
        ImmutableList.Builder<E> builder = ImmutableList.builder();
        for (ImmutableList<? extends E> list : lists) {
            if (list != null && !list.isEmpty()) {
                builder.addAll(list);
            }
        }
        return builder.build();
    }

    /**
     * Reverse a list.
     * 反转列表。
     *
     * @param <E>  element type | 元素类型
     * @param list the list to reverse | 要反转的列表
     * @return reversed immutable list | 反转后的不可变列表
     */
    public static <E> ImmutableList<E> reverse(ImmutableList<E> list) {
        if (list == null || list.size() <= 1) {
            return list == null ? ImmutableList.of() : list;
        }
        List<E> reversed = new ArrayList<>(list);
        Collections.reverse(reversed);
        return ImmutableList.copyOf(reversed);
    }

    /**
     * Get a sublist.
     * 获取子列表。
     *
     * @param <E>       element type | 元素类型
     * @param list      the list | 列表
     * @param fromIndex the start index (inclusive) | 起始索引（包含）
     * @param toIndex   the end index (exclusive) | 结束索引（不包含）
     * @return sublist as immutable list | 作为不可变列表的子列表
     */
    public static <E> ImmutableList<E> subList(ImmutableList<E> list, int fromIndex, int toIndex) {
        if (list == null || list.isEmpty()) {
            return ImmutableList.of();
        }
        return ImmutableList.copyOf(list.subList(fromIndex, toIndex));
    }

    // ==================== Set 操作 | Set Operations ====================

    /**
     * Transform a set using a mapping function.
     * 使用映射函数转换集合。
     *
     * @param <T>      source element type | 源元素类型
     * @param <R>      target element type | 目标元素类型
     * @param set      the set to transform | 要转换的集合
     * @param function the mapping function | 映射函数
     * @return transformed immutable set | 转换后的不可变集合
     */
    public static <T, R> ImmutableSet<R> transform(ImmutableSet<T> set, Function<? super T, ? extends R> function) {
        Objects.requireNonNull(function, "Function cannot be null");
        if (set == null || set.isEmpty()) {
            return ImmutableSet.of();
        }
        return ImmutableSet.copyOf(
                set.stream()
                        .map(function)
                        .collect(Collectors.toSet())
        );
    }

    /**
     * Union of multiple sets.
     * 多个集合的并集。
     *
     * @param <E>  element type | 元素类型
     * @param sets the sets to union | 要合并的集合
     * @return union as immutable set | 作为不可变集合的并集
     */
    @SafeVarargs
    public static <E> ImmutableSet<E> union(ImmutableSet<? extends E>... sets) {
        if (sets == null || sets.length == 0) {
            return ImmutableSet.of();
        }
        ImmutableSet.Builder<E> builder = ImmutableSet.builder();
        for (ImmutableSet<? extends E> set : sets) {
            if (set != null && !set.isEmpty()) {
                builder.addAll(set);
            }
        }
        return builder.build();
    }

    /**
     * Intersection of two sets.
     * 两个集合的交集。
     *
     * @param <E>  element type | 元素类型
     * @param set1 the first set | 第一个集合
     * @param set2 the second set | 第二个集合
     * @return intersection as immutable set | 作为不可变集合的交集
     */
    public static <E> ImmutableSet<E> intersection(ImmutableSet<E> set1, ImmutableSet<E> set2) {
        if (set1 == null || set2 == null || set1.isEmpty() || set2.isEmpty()) {
            return ImmutableSet.of();
        }
        return ImmutableSet.copyOf(
                set1.stream()
                        .filter(set2::contains)
                        .collect(Collectors.toSet())
        );
    }

    /**
     * Difference of two sets (elements in set1 but not in set2).
     * 两个集合的差集（在set1中但不在set2中的元素）。
     *
     * @param <E>  element type | 元素类型
     * @param set1 the first set | 第一个集合
     * @param set2 the second set | 第二个集合
     * @return difference as immutable set | 作为不可变集合的差集
     */
    public static <E> ImmutableSet<E> difference(ImmutableSet<E> set1, ImmutableSet<E> set2) {
        if (set1 == null || set1.isEmpty()) {
            return ImmutableSet.of();
        }
        if (set2 == null || set2.isEmpty()) {
            return set1;
        }
        return ImmutableSet.copyOf(
                set1.stream()
                        .filter(e -> !set2.contains(e))
                        .collect(Collectors.toSet())
        );
    }

    // ==================== Multiset 操作 | Multiset Operations ====================

    /**
     * Convert a collection to an immutable multiset.
     * 将集合转换为不可变多重集。
     *
     * @param <E>        element type | 元素类型
     * @param collection the collection | 集合
     * @return immutable multiset | 不可变多重集
     */
    public static <E> ImmutableMultiset<E> toMultiset(Collection<? extends E> collection) {
        return ImmutableMultiset.copyOf(collection);
    }

    /**
     * Transform a multiset using a mapping function.
     * 使用映射函数转换多重集。
     *
     * @param <T>      source element type | 源元素类型
     * @param <R>      target element type | 目标元素类型
     * @param multiset the multiset to transform | 要转换的多重集
     * @param function the mapping function | 映射函数
     * @return transformed immutable multiset | 转换后的不可变多重集
     */
    public static <T, R> ImmutableMultiset<R> transform(ImmutableMultiset<T> multiset, Function<? super T, ? extends R> function) {
        Objects.requireNonNull(function, "Function cannot be null");
        if (multiset == null || multiset.isEmpty()) {
            return ImmutableMultiset.of();
        }
        ImmutableMultiset.Builder<R> builder = ImmutableMultiset.builder();
        for (ImmutableMultiset.Entry<T> entry : multiset.entrySet()) {
            R mapped = function.apply(entry.getElement());
            builder.add(mapped, entry.getCount());
        }
        return builder.build();
    }

    /**
     * Combine multiple multisets.
     * 组合多个多重集。
     *
     * @param <E>       element type | 元素类型
     * @param multisets the multisets to combine | 要组合的多重集
     * @return combined immutable multiset | 组合后的不可变多重集
     */
    @SafeVarargs
    public static <E> ImmutableMultiset<E> combine(ImmutableMultiset<? extends E>... multisets) {
        if (multisets == null || multisets.length == 0) {
            return ImmutableMultiset.of();
        }
        ImmutableMultiset.Builder<E> builder = ImmutableMultiset.builder();
        for (ImmutableMultiset<? extends E> multiset : multisets) {
            if (multiset != null && !multiset.isEmpty()) {
                for (ImmutableMultiset.Entry<? extends E> entry : multiset.entrySet()) {
                    builder.add(entry.getElement(), entry.getCount());
                }
            }
        }
        return builder.build();
    }

    // ==================== BiMap 操作 | BiMap Operations ====================

    /**
     * Convert a map to an immutable bimap.
     * 将映射转换为不可变双向映射。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param map the map | 映射
     * @return immutable bimap | 不可变双向映射
     * @throws OpenCollectionException if duplicate values are found | 如果找到重复值
     */
    public static <K, V> ImmutableBiMap<K, V> toBiMap(Map<? extends K, ? extends V> map) {
        return ImmutableBiMap.copyOf(map);
    }

    /**
     * Create a bimap from two lists (keys and values).
     * 从两个列表（键和值）创建双向映射。
     *
     * @param <K>    key type | 键类型
     * @param <V>    value type | 值类型
     * @param keys   the keys | 键
     * @param values the values | 值
     * @return immutable bimap | 不可变双向映射
     * @throws OpenCollectionException if lists have different sizes or duplicate keys/values | 如果列表大小不同或有重复的键/值
     */
    public static <K, V> ImmutableBiMap<K, V> zipToBiMap(List<? extends K> keys, List<? extends V> values) {
        Objects.requireNonNull(keys, "Keys cannot be null");
        Objects.requireNonNull(values, "Values cannot be null");
        if (keys.size() != values.size()) {
            throw new OpenCollectionException("Keys and values must have the same size");
        }
        ImmutableBiMap.Builder<K, V> builder = ImmutableBiMap.builder();
        for (int i = 0; i < keys.size(); i++) {
            builder.put(keys.get(i), values.get(i));
        }
        return builder.build();
    }

    // ==================== Table 操作 | Table Operations ====================

    /**
     * Create a table from a nested map.
     * 从嵌套映射创建表格。
     *
     * @param <R>      row key type | 行键类型
     * @param <C>      column key type | 列键类型
     * @param <V>      value type | 值类型
     * @param nestedMap the nested map (row -> column -> value) | 嵌套映射（行 -> 列 -> 值）
     * @return immutable table | 不可变表格
     */
    public static <R, C, V> ImmutableTable<R, C, V> toTable(Map<? extends R, ? extends Map<? extends C, ? extends V>> nestedMap) {
        if (nestedMap == null || nestedMap.isEmpty()) {
            return ImmutableTable.of();
        }
        ImmutableTable.Builder<R, C, V> builder = ImmutableTable.builder();
        for (Map.Entry<? extends R, ? extends Map<? extends C, ? extends V>> rowEntry : nestedMap.entrySet()) {
            R rowKey = rowEntry.getKey();
            Map<? extends C, ? extends V> row = rowEntry.getValue();
            if (row != null) {
                for (Map.Entry<? extends C, ? extends V> cellEntry : row.entrySet()) {
                    builder.put(rowKey, cellEntry.getKey(), cellEntry.getValue());
                }
            }
        }
        return builder.build();
    }

    /**
     * Transpose a table (swap rows and columns).
     * 转置表格（交换行和列）。
     *
     * @param <R>   row key type | 行键类型
     * @param <C>   column key type | 列键类型
     * @param <V>   value type | 值类型
     * @param table the table to transpose | 要转置的表格
     * @return transposed immutable table | 转置后的不可变表格
     */
    public static <R, C, V> ImmutableTable<C, R, V> transpose(ImmutableTable<R, C, V> table) {
        if (table == null || table.isEmpty()) {
            return ImmutableTable.of();
        }
        ImmutableTable.Builder<C, R, V> builder = ImmutableTable.builder();
        for (ImmutableTable.Cell<R, C, V> cell : table.cellSet()) {
            builder.put(cell.getColumnKey(), cell.getRowKey(), cell.getValue());
        }
        return builder.build();
    }

    // ==================== 转换操作 | Conversion Operations ====================

    /**
     * Convert an immutable list to an immutable set.
     * 将不可变列表转换为不可变集合。
     *
     * @param <E>  element type | 元素类型
     * @param list the list | 列表
     * @return immutable set | 不可变集合
     */
    public static <E> ImmutableSet<E> toSet(ImmutableList<E> list) {
        return ImmutableSet.copyOf(list);
    }

    /**
     * Convert an immutable set to an immutable list.
     * 将不可变集合转换为不可变列表。
     *
     * @param <E> element type | 元素类型
     * @param set the set | 集合
     * @return immutable list | 不可变列表
     */
    public static <E> ImmutableList<E> toList(ImmutableSet<E> set) {
        return ImmutableList.copyOf(set);
    }

    /**
     * Convert an immutable multiset to an immutable list (preserving duplicates).
     * 将不可变多重集转换为不可变列表（保留重复项）。
     *
     * @param <E>      element type | 元素类型
     * @param multiset the multiset | 多重集
     * @return immutable list with duplicates | 带重复项的不可变列表
     */
    public static <E> ImmutableList<E> toList(ImmutableMultiset<E> multiset) {
        if (multiset == null || multiset.isEmpty()) {
            return ImmutableList.of();
        }
        ImmutableList.Builder<E> builder = ImmutableList.builder();
        for (E element : multiset) {
            builder.add(element);
        }
        return builder.build();
    }

    // ==================== 过滤操作 | Filtering Operations ====================

    /**
     * Filter a list based on a predicate.
     * 基于谓词过滤列表。
     *
     * @param <E>       element type | 元素类型
     * @param list      the list to filter | 要过滤的列表
     * @param predicate the filter predicate | 过滤谓词
     * @return filtered immutable list | 过滤后的不可变列表
     */
    public static <E> ImmutableList<E> filter(ImmutableList<E> list, java.util.function.Predicate<? super E> predicate) {
        Objects.requireNonNull(predicate, "Predicate cannot be null");
        if (list == null || list.isEmpty()) {
            return ImmutableList.of();
        }
        return ImmutableList.copyOf(
                list.stream()
                        .filter(predicate)
                        .toList()
        );
    }

    /**
     * Filter a set based on a predicate.
     * 基于谓词过滤集合。
     *
     * @param <E>       element type | 元素类型
     * @param set       the set to filter | 要过滤的集合
     * @param predicate the filter predicate | 过滤谓词
     * @return filtered immutable set | 过滤后的不可变集合
     */
    public static <E> ImmutableSet<E> filter(ImmutableSet<E> set, java.util.function.Predicate<? super E> predicate) {
        Objects.requireNonNull(predicate, "Predicate cannot be null");
        if (set == null || set.isEmpty()) {
            return ImmutableSet.of();
        }
        return ImmutableSet.copyOf(
                set.stream()
                        .filter(predicate)
                        .collect(Collectors.toSet())
        );
    }

    // ==================== 空值检查 | Null Checks ====================

    /**
     * Check if a collection is null or empty.
     * 检查集合是否为空或为null。
     *
     * @param collection the collection | 集合
     * @return true if null or empty | 如果为null或为空则返回true
     */
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Check if a map is null or empty.
     * 检查映射是否为空或为null。
     *
     * @param map the map | 映射
     * @return true if null or empty | 如果为null或为空则返回true
     */
    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Check if a table is null or empty.
     * 检查表格是否为空或为null。
     *
     * @param table the table | 表格
     * @return true if null or empty | 如果为null或为空则返回true
     */
    public static boolean isNullOrEmpty(ImmutableTable<?, ?, ?> table) {
        return table == null || table.isEmpty();
    }
}
