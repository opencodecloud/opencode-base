package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Multimap;

import java.util.Map;
import java.util.Set;

/**
 * SetMultimap - Set-based Multimap Interface
 * SetMultimap - 基于集合的多值映射接口
 *
 * <p>A multimap that stores values in sets, not allowing duplicate
 * key-value pairs.</p>
 * <p>将值存储在集合中的多值映射，不允许重复的键值对。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>No duplicate values per key - 每个键不允许重复值</li>
 *   <li>Set semantics - 集合语义</li>
 *   <li>Efficient contains check - 高效的包含检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SetMultimap<String, Integer> multimap = MultimapBuilder.linkedHashKeys()
 *     .linkedHashSetValues()
 *     .build();
 *
 * multimap.put("a", 1);
 * multimap.put("a", 2);
 * multimap.put("a", 1); // duplicate ignored
 *
 * Set<Integer> values = multimap.get("a"); // [1, 2]
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>get: O(1) - get: O(1)</li>
 *   <li>put: O(1) average - put: O(1) 平均</li>
 *   <li>contains: O(1) - contains: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (interface, implementation-dependent) - 否（接口，取决于实现）</li>
 *   <li>Null-safe: No - 否</li>
 * </ul>
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public interface SetMultimap<K, V> extends Multimap<K, V> {

    /**
     * Get the set of values for a key.
     * 获取键对应的值集合。
     *
     * @param key the key | 键
     * @return the set of values | 值集合
     */
    @Override
    Set<V> get(K key);

    /**
     * Remove all values for a key and return them as a set.
     * 移除键的所有值并以集合形式返回。
     *
     * @param key the key | 键
     * @return the removed values | 被移除的值
     */
    @Override
    Set<V> removeAll(Object key);

    /**
     * Replace all values for a key and return the old values as a set.
     * 替换键的所有值并以集合形式返回旧值。
     *
     * @param key    the key | 键
     * @param values the new values | 新值
     * @return the old values | 旧值
     */
    @Override
    Set<V> replaceValues(K key, Iterable<? extends V> values);

    /**
     * Return all entries as a set.
     * 以集合形式返回所有条目。
     *
     * @return the entries set | 条目集合
     */
    @Override
    Set<Map.Entry<K, V>> entries();
}
