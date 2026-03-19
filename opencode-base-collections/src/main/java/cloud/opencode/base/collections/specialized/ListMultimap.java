package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Multimap;

import java.util.List;
import java.util.Map;

/**
 * ListMultimap - List-based Multimap Interface
 * ListMultimap - 基于列表的多值映射接口
 *
 * <p>A multimap that stores values in lists, preserving insertion order
 * and allowing duplicate key-value pairs.</p>
 * <p>将值存储在列表中的多值映射，保留插入顺序并允许重复的键值对。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Duplicate values per key - 每个键允许重复值</li>
 *   <li>Preserves insertion order - 保留插入顺序</li>
 *   <li>List access to values - 列表方式访问值</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ListMultimap<String, Integer> multimap = MultimapBuilder.linkedHashKeys()
 *     .arrayListValues()
 *     .build();
 *
 * multimap.put("a", 1);
 * multimap.put("a", 2);
 * multimap.put("a", 1); // duplicate allowed
 *
 * List<Integer> values = multimap.get("a"); // [1, 2, 1]
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>get: O(1) - get: O(1)</li>
 *   <li>put: O(1) amortized - put: O(1) 均摊</li>
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
public interface ListMultimap<K, V> extends Multimap<K, V> {

    /**
     * Get the list of values for a key.
     * 获取键对应的值列表。
     *
     * @param key the key | 键
     * @return the list of values | 值列表
     */
    @Override
    List<V> get(K key);

    /**
     * Remove all values for a key and return them as a list.
     * 移除键的所有值并以列表形式返回。
     *
     * @param key the key | 键
     * @return the removed values | 被移除的值
     */
    @Override
    List<V> removeAll(Object key);

    /**
     * Replace all values for a key and return the old values as a list.
     * 替换键的所有值并以列表形式返回旧值。
     *
     * @param key    the key | 键
     * @param values the new values | 新值
     * @return the old values | 旧值
     */
    @Override
    List<V> replaceValues(K key, Iterable<? extends V> values);

}
