/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.AbstractMultimap;
import cloud.opencode.base.collections.Multimap;
import cloud.opencode.base.collections.exception.OpenCollectionException;

import java.io.Serial;
import java.util.*;

/**
 * TreeSetMultimap - SortedSetMultimap Implementation using TreeSet
 * TreeSetMultimap - 使用 TreeSet 的 SortedSetMultimap 实现
 *
 * <p>A Multimap implementation that uses TreeSet for each key's values,
 * maintaining elements in sorted order. Duplicate values per key are not allowed.</p>
 * <p>使用 TreeSet 存储每个键的值的 Multimap 实现，保持元素的排序顺序。
 * 不允许每个键有重复值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sorted values per key - 每个键的值有序</li>
 *   <li>No duplicate values - 无重复值</li>
 *   <li>Natural or custom ordering - 自然排序或自定义排序</li>
 *   <li>Serializable - 可序列化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create with natural ordering
 * TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
 * multimap.put("numbers", 3);
 * multimap.put("numbers", 1);
 * multimap.put("numbers", 2);
 * SortedSet<Integer> values = multimap.get("numbers"); // [1, 2, 3]
 *
 * // Create with custom comparator (reverse order)
 * TreeSetMultimap<String, Integer> reverse = TreeSetMultimap.create(
 *     Comparator.reverseOrder());
 * reverse.put("numbers", 3);
 * reverse.put("numbers", 1);
 * reverse.put("numbers", 2);
 * SortedSet<Integer> reverseValues = reverse.get("numbers"); // [3, 2, 1]
 *
 * // Create with sorted keys and values
 * TreeSetMultimap<String, Integer> sortedBoth = TreeSetMultimap.create(
 *     String.CASE_INSENSITIVE_ORDER,
 *     Comparator.naturalOrder());
 *
 * // Get first/last values
 * Integer first = multimap.get("numbers").first(); // 1
 * Integer last = multimap.get("numbers").last();   // 3
 *
 * // Subset operations
 * SortedSet<Integer> headSet = multimap.get("numbers").headSet(2); // [1]
 * SortedSet<Integer> tailSet = multimap.get("numbers").tailSet(2); // [2, 3]
 * SortedSet<Integer> subSet = multimap.get("numbers").subSet(1, 3); // [1, 2]
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>put: O(log n) - put: O(log n)</li>
 *   <li>get: O(1) for key lookup - get: O(1) 键查找</li>
 *   <li>contains: O(log n) - contains: O(log n)</li>
 *   <li>remove: O(log n) - remove: O(log n)</li>
 *   <li>first/last: O(log n) - first/last: O(log n)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Depends on comparator - 空值安全: 取决于比较器</li>
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
public class TreeSetMultimap<K, V> extends AbstractMultimap<K, V> implements SortedSetMultimap<K, V> {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Comparator<? super V> valueComparator;

    // ==================== Constructors | 构造方法 ====================

    /**
     * Private constructor with HashMap backing.
     * 使用 HashMap 后备的私有构造方法。
     *
     * @param valueComparator the comparator for values | 值的比较器
     */
    private TreeSetMultimap(Comparator<? super V> valueComparator) {
        super(new HashMap<>());
        this.valueComparator = valueComparator;
    }

    /**
     * Private constructor with TreeMap backing for sorted keys.
     * 使用 TreeMap 后备以支持排序键的私有构造方法。
     *
     * @param keyComparator   the comparator for keys | 键的比较器
     * @param valueComparator the comparator for values | 值的比较器
     */
    private TreeSetMultimap(Comparator<? super K> keyComparator, Comparator<? super V> valueComparator) {
        super(new TreeMap<>(keyComparator));
        this.valueComparator = valueComparator;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create an empty TreeSetMultimap with natural ordering for values.
     * 创建具有值自然排序的空 TreeSetMultimap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type (must be Comparable) | 值类型（必须是 Comparable）
     * @return new TreeSetMultimap | 新的 TreeSetMultimap
     */
    public static <K, V extends Comparable<? super V>> TreeSetMultimap<K, V> create() {
        return new TreeSetMultimap<>(null);
    }

    /**
     * Create an empty TreeSetMultimap with a custom value comparator.
     * 创建具有自定义值比较器的空 TreeSetMultimap。
     *
     * @param <K>             key type | 键类型
     * @param <V>             value type | 值类型
     * @param valueComparator the comparator for values | 值的比较器
     * @return new TreeSetMultimap | 新的 TreeSetMultimap
     */
    public static <K, V> TreeSetMultimap<K, V> create(Comparator<? super V> valueComparator) {
        return new TreeSetMultimap<>(valueComparator);
    }

    /**
     * Create an empty TreeSetMultimap with sorted keys and values.
     * 创建具有排序键和值的空 TreeSetMultimap。
     *
     * @param <K>             key type | 键类型
     * @param <V>             value type | 值类型
     * @param keyComparator   the comparator for keys (null for natural order) | 键的比较器（null 表示自然顺序）
     * @param valueComparator the comparator for values (null for natural order) | 值的比较器（null 表示自然顺序）
     * @return new TreeSetMultimap | 新的 TreeSetMultimap
     */
    public static <K, V> TreeSetMultimap<K, V> create(Comparator<? super K> keyComparator,
                                                       Comparator<? super V> valueComparator) {
        return new TreeSetMultimap<>(keyComparator, valueComparator);
    }

    /**
     * Create a TreeSetMultimap from an existing multimap.
     * 从现有多重映射创建 TreeSetMultimap。
     *
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @param multimap source multimap | 源多重映射
     * @return new TreeSetMultimap | 新的 TreeSetMultimap
     */
    public static <K, V extends Comparable<? super V>> TreeSetMultimap<K, V> create(
            Multimap<? extends K, ? extends V> multimap) {
        TreeSetMultimap<K, V> result = create();
        result.putAll(multimap);
        return result;
    }

    /**
     * Create a TreeSetMultimap from an existing multimap with custom comparator.
     * 从现有多重映射创建具有自定义比较器的 TreeSetMultimap。
     *
     * @param <K>             key type | 键类型
     * @param <V>             value type | 值类型
     * @param multimap        source multimap | 源多重映射
     * @param valueComparator the comparator for values | 值的比较器
     * @return new TreeSetMultimap | 新的 TreeSetMultimap
     */
    public static <K, V> TreeSetMultimap<K, V> create(Multimap<? extends K, ? extends V> multimap,
                                                       Comparator<? super V> valueComparator) {
        TreeSetMultimap<K, V> result = create(valueComparator);
        result.putAll(multimap);
        return result;
    }

    // ==================== Abstract Method Implementation | 抽象方法实现 ====================

    @Override
    protected Collection<V> createCollection() {
        return new TreeSet<>(valueComparator);
    }

    // ==================== SortedSetMultimap Implementation | SortedSetMultimap 实现 ====================

    @Override
    public SortedSet<V> get(K key) {
        Collection<V> collection = map.get(key);
        if (collection == null) {
            return new TreeSet<>(valueComparator);
        }
        return (SortedSet<V>) collection;
    }

    @Override
    public SortedSet<V> removeAll(Object key) {
        Collection<V> collection = map.remove(key);
        SortedSet<V> result = new TreeSet<>(valueComparator);
        if (collection != null) {
            result.addAll(collection);
        }
        return result;
    }

    @Override
    public SortedSet<V> replaceValues(K key, Iterable<? extends V> values) {
        SortedSet<V> oldValues = removeAll(key);
        if (values != null) {
            putAll(key, values);
        }
        return oldValues;
    }

    @Override
    public Set<Map.Entry<K, V>> entries() {
        Set<Map.Entry<K, V>> entries = new LinkedHashSet<>();
        for (Map.Entry<K, Collection<V>> entry : map.entrySet()) {
            K key = entry.getKey();
            for (V value : entry.getValue()) {
                entries.add(new AbstractMap.SimpleImmutableEntry<>(key, value));
            }
        }
        return Collections.unmodifiableSet(entries);
    }

    @Override
    public Comparator<? super V> valueComparator() {
        return valueComparator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<K, Collection<V>> asMap() {
        return Collections.unmodifiableMap((Map<K, Collection<V>>) (Map<?, ?>) map);
    }

    /**
     * Returns a view of this multimap as a map from keys to sorted sets of values.
     * 返回此多值映射作为从键到排序集合值的映射的视图。
     *
     * @return a map view with SortedSet values | 具有 SortedSet 值的映射视图
     */
    @SuppressWarnings("unchecked")
    public Map<K, SortedSet<V>> asMapOfSortedSets() {
        return Collections.unmodifiableMap((Map<K, SortedSet<V>>) (Map<?, ?>) map);
    }

    // ==================== Convenience Methods | 便利方法 ====================

    /**
     * Returns the first (lowest) value for the given key.
     * 返回给定键的第一个（最小）值。
     *
     * @param key the key | 键
     * @return the first value, or empty if no values | 第一个值，如果没有值则为空
     */
    public Optional<V> first(K key) {
        SortedSet<V> values = get(key);
        return values.isEmpty() ? Optional.empty() : Optional.of(values.first());
    }

    /**
     * Returns the last (highest) value for the given key.
     * 返回给定键的最后一个（最大）值。
     *
     * @param key the key | 键
     * @return the last value, or empty if no values | 最后一个值，如果没有值则为空
     */
    public Optional<V> last(K key) {
        SortedSet<V> values = get(key);
        return values.isEmpty() ? Optional.empty() : Optional.of(values.last());
    }

    /**
     * Returns a view of values strictly less than the given value.
     * 返回严格小于给定值的值视图。
     *
     * @param key      the key | 键
     * @param toValue  high endpoint (exclusive) | 上限端点（不包含）
     * @return the head set | 头集合
     */
    public SortedSet<V> headSet(K key, V toValue) {
        return get(key).headSet(toValue);
    }

    /**
     * Returns a view of values greater than or equal to the given value.
     * 返回大于或等于给定值的值视图。
     *
     * @param key       the key | 键
     * @param fromValue low endpoint (inclusive) | 下限端点（包含）
     * @return the tail set | 尾集合
     */
    public SortedSet<V> tailSet(K key, V fromValue) {
        return get(key).tailSet(fromValue);
    }

    /**
     * Returns a view of values in the given range.
     * 返回给定范围内的值视图。
     *
     * @param key       the key | 键
     * @param fromValue low endpoint (inclusive) | 下限端点（包含）
     * @param toValue   high endpoint (exclusive) | 上限端点（不包含）
     * @return the sub set | 子集合
     */
    public SortedSet<V> subSet(K key, V fromValue, V toValue) {
        return get(key).subSet(fromValue, toValue);
    }
}
