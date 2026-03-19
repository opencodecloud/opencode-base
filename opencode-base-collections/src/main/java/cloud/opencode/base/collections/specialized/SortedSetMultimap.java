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

import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;

/**
 * SortedSetMultimap - Multimap with Sorted Set Values
 * SortedSetMultimap - 具有排序集合值的多重映射
 *
 * <p>A SetMultimap whose values for each key are stored in a SortedSet,
 * maintaining elements in sorted order.</p>
 * <p>一种 SetMultimap，其每个键的值存储在 SortedSet 中，保持元素的排序顺序。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sorted values per key - 每个键的值有序</li>
 *   <li>No duplicate values - 无重复值</li>
 *   <li>Natural or custom ordering - 自然排序或自定义排序</li>
 *   <li>Subset views - 子集视图</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create with natural ordering
 * SortedSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
 *
 * // Create with custom comparator
 * SortedSetMultimap<String, String> customMap = TreeSetMultimap.create(
 *     String.CASE_INSENSITIVE_ORDER);
 *
 * // Add values
 * multimap.put("numbers", 3);
 * multimap.put("numbers", 1);
 * multimap.put("numbers", 2);
 *
 * // Values are automatically sorted
 * SortedSet<Integer> values = multimap.get("numbers"); // [1, 2, 3]
 *
 * // Get first/last
 * Integer first = multimap.get("numbers").first(); // 1
 * Integer last = multimap.get("numbers").last();   // 3
 *
 * // Subset views
 * SortedSet<Integer> headSet = multimap.get("numbers").headSet(2); // [1]
 * SortedSet<Integer> tailSet = multimap.get("numbers").tailSet(2); // [2, 3]
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>get: O(1) for key lookup, O(log n) for set operations - get: O(1) 键查找，O(log n) 集合操作</li>
 *   <li>put: O(log n) per value - put: 每个值 O(log n)</li>
 *   <li>contains: O(log n) - contains: O(log n)</li>
 *   <li>first/last: O(log n) - first/last: O(log n)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (interface, implementation-dependent) - 否（接口，取决于实现）</li>
 *   <li>Null-safe: No - 否</li>
 * </ul>
 * @param <K> key type | 键类型
 * @param <V> value type (must be Comparable or use custom Comparator) | 值类型（必须是 Comparable 或使用自定义 Comparator）
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public interface SortedSetMultimap<K, V> extends SetMultimap<K, V> {

    /**
     * Get the sorted set of values for a key.
     * 获取键对应的排序集合值。
     *
     * @param key the key | 键
     * @return the sorted set of values (never null, may be empty) | 值的排序集合（不为 null，可能为空）
     */
    @Override
    SortedSet<V> get(K key);

    /**
     * Remove all values for a key and return them as a sorted set.
     * 移除键的所有值并以排序集合形式返回。
     *
     * @param key the key | 键
     * @return the removed values as a sorted set | 以排序集合形式返回被移除的值
     */
    @Override
    SortedSet<V> removeAll(Object key);

    /**
     * Replace all values for a key and return the old values as a sorted set.
     * 替换键的所有值并以排序集合形式返回旧值。
     *
     * @param key    the key | 键
     * @param values the new values | 新值
     * @return the old values as a sorted set | 以排序集合形式返回旧值
     */
    @Override
    SortedSet<V> replaceValues(K key, Iterable<? extends V> values);

    /**
     * Returns the comparator used to order values in each key's collection.
     * 返回用于对每个键的集合中的值进行排序的比较器。
     *
     * <p>Returns null if natural ordering is used.</p>
     * <p>如果使用自然排序则返回 null。</p>
     *
     * @return the comparator, or null if natural ordering | 比较器，如果使用自然排序则为 null
     */
    Comparator<? super V> valueComparator();

    /**
     * Returns a map view where each key maps to a SortedSet of values.
     * 返回每个键映射到值的 SortedSet 的映射视图。
     *
     * <p>Unlike the standard {@code asMap()}, this method returns a map
     * with SortedSet values, preserving the sorted nature of the values.</p>
     * <p>与标准的 {@code asMap()} 不同，此方法返回具有 SortedSet 值的映射，
     * 保留值的排序特性。</p>
     *
     * @return the map view with SortedSet values | 具有 SortedSet 值的映射视图
     */
    Map<K, SortedSet<V>> asMapOfSortedSets();
}
