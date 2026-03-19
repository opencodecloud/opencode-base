package cloud.opencode.base.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Multimap - Map with multiple values per key
 * Multimap - 每个键有多个值的映射
 *
 * <p>A map-like collection where each key can be associated with multiple values.
 * The collection of values for each key can be a List, Set, or other Collection type.</p>
 * <p>类似映射的集合，每个键可以关联多个值。每个键的值集合可以是 List、Set 或其他 Collection 类型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple values per key - 每个键多个值</li>
 *   <li>Key set view - 键集合视图</li>
 *   <li>Values collection view - 值集合视图</li>
 *   <li>Entries collection view - 条目集合视图</li>
 *   <li>Map view (key to collection) - 映射视图（键到集合）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create Multimap - 创建 Multimap
 * Multimap<String, Integer> multimap = ArrayListMultimap.create();
 *
 * // Put values - 放入值
 * multimap.put("a", 1);
 * multimap.put("a", 2);
 * multimap.put("b", 3);
 *
 * // Get values - 获取值
 * Collection<Integer> values = multimap.get("a");  // [1, 2]
 *
 * // Check contains - 检查包含
 * boolean hasKey = multimap.containsKey("a");      // true
 * boolean hasValue = multimap.containsValue(1);    // true
 * boolean hasEntry = multimap.containsEntry("a", 1); // true
 *
 * // Remove - 移除
 * multimap.remove("a", 1);  // removes single entry
 * multimap.removeAll("a");  // removes all values for key
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>put: O(1) for hash-based - put: O(1) 基于哈希</li>
 *   <li>get: O(1) for hash-based - get: O(1) 基于哈希</li>
 *   <li>containsKey: O(1) for hash-based - containsKey: O(1) 基于哈希</li>
 *   <li>containsValue: O(n) - containsValue: O(n)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Implementation dependent - 空值安全: 取决于实现</li>
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
public interface Multimap<K, V> {

    // ==================== 基本操作 | Basic Operations ====================

    /**
     * Return the total number of key-value pairs.
     * 返回键值对的总数。
     *
     * @return size | 大小
     */
    int size();

    /**
     * Check if the multimap is empty.
     * 检查多重映射是否为空。
     *
     * @return true if empty | 如果为空则返回 true
     */
    boolean isEmpty();

    /**
     * Check if the multimap contains the key.
     * 检查多重映射是否包含键。
     *
     * @param key the key | 键
     * @return true if contains | 如果包含则返回 true
     */
    boolean containsKey(Object key);

    /**
     * Check if the multimap contains the value.
     * 检查多重映射是否包含值。
     *
     * @param value the value | 值
     * @return true if contains | 如果包含则返回 true
     */
    boolean containsValue(Object value);

    /**
     * Check if the multimap contains the key-value pair.
     * 检查多重映射是否包含键值对。
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return true if contains | 如果包含则返回 true
     */
    boolean containsEntry(Object key, Object value);

    // ==================== 放入操作 | Put Operations ====================

    /**
     * Store a key-value pair.
     * 存储键值对。
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return true if changed | 如果发生更改则返回 true
     */
    boolean put(K key, V value);

    /**
     * Store key-value pairs from a map.
     * 从映射存储键值对。
     *
     * @param map the map | 映射
     */
    default void putAll(Map<? extends K, ? extends V> map) {
        if (map != null) {
            for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Store all key-value pairs from another multimap.
     * 从另一个多重映射存储所有键值对。
     *
     * @param multimap the multimap | 多重映射
     */
    default void putAll(Multimap<? extends K, ? extends V> multimap) {
        if (multimap != null) {
            for (Map.Entry<? extends K, ? extends V> entry : multimap.entries()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Store multiple values for a key.
     * 为键存储多个值。
     *
     * @param key    the key | 键
     * @param values the values | 值
     * @return true if changed | 如果发生更改则返回 true
     */
    boolean putAll(K key, Iterable<? extends V> values);

    // ==================== 获取操作 | Get Operations ====================

    /**
     * Return the collection of values for the key.
     * 返回键的值集合。
     *
     * @param key the key | 键
     * @return values collection (never null, may be empty) | 值集合（不为 null，可能为空）
     */
    Collection<V> get(K key);

    // ==================== 移除操作 | Remove Operations ====================

    /**
     * Remove a single key-value pair.
     * 移除单个键值对。
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return true if removed | 如果移除则返回 true
     */
    boolean remove(Object key, Object value);

    /**
     * Remove all values for a key.
     * 移除键的所有值。
     *
     * @param key the key | 键
     * @return removed values | 移除的值
     */
    Collection<V> removeAll(Object key);

    /**
     * Replace all values for a key.
     * 替换键的所有值。
     *
     * @param key    the key | 键
     * @param values the new values | 新值
     * @return previous values | 之前的值
     */
    Collection<V> replaceValues(K key, Iterable<? extends V> values);

    /**
     * Clear all entries.
     * 清除所有条目。
     */
    void clear();

    // ==================== 视图 | Views ====================

    /**
     * Return a set view of the keys.
     * 返回键的集合视图。
     *
     * @return key set | 键集合
     */
    Set<K> keySet();

    /**
     * Return a multiset view of the keys (includes duplicates by value count).
     * 返回键的多重集视图（按值计数包含重复）。
     *
     * @return key multiset | 键多重集
     */
    Multiset<K> keys();

    /**
     * Return a collection view of all values.
     * 返回所有值的集合视图。
     *
     * @return values collection | 值集合
     */
    Collection<V> values();

    /**
     * Return a collection view of all key-value pairs.
     * 返回所有键值对的集合视图。
     *
     * @return entries collection | 条目集合
     */
    Collection<Map.Entry<K, V>> entries();

    /**
     * Return a map view with key to collection mapping.
     * 返回键到集合映射的视图。
     *
     * @return map view | 映射视图
     */
    Map<K, ? extends Collection<V>> asMap();
}
