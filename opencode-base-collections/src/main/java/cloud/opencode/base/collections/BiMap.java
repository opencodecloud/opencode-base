package cloud.opencode.base.collections;

import java.util.Map;
import java.util.Set;

/**
 * BiMap - Bidirectional Map Interface
 * BiMap - 双向映射接口
 *
 * <p>A bidirectional map where both keys and values must be unique.
 * Provides inverse view for value-to-key lookups.</p>
 * <p>双向映射，键和值都必须唯一。提供反向视图用于值到键的查找。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Bidirectional mapping - 双向映射</li>
 *   <li>Unique keys and values - 唯一的键和值</li>
 *   <li>Inverse view - 反向视图</li>
 *   <li>Force put operation - 强制放入操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create BiMap - 创建 BiMap
 * BiMap<String, Integer> bimap = HashBiMap.create();
 *
 * // Put entries - 放入条目
 * bimap.put("one", 1);
 * bimap.put("two", 2);
 *
 * // Get by key - 通过键获取
 * Integer value = bimap.get("one"); // 1
 *
 * // Get inverse view - 获取反向视图
 * BiMap<Integer, String> inverse = bimap.inverse();
 * String key = inverse.get(1); // "one"
 *
 * // Force put (replaces existing mapping) - 强制放入
 * bimap.forcePut("three", 1); // removes "one" -> 1
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>get: O(1) for hash-based - get: O(1) 基于哈希</li>
 *   <li>put: O(1) for hash-based - put: O(1) 基于哈希</li>
 *   <li>inverse: O(1) - inverse: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (nulls not allowed) - 空值安全: 否（不允许空值）</li>
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
public interface BiMap<K, V> extends Map<K, V> {

    // ==================== 核心操作 | Core Operations ====================

    /**
     * Associates the specified value with the specified key.
     * Throws exception if value already exists.
     * 将指定值与指定键关联。如果值已存在则抛出异常。
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return the previous value, or null | 之前的值，或 null
     * @throws IllegalArgumentException if value already present | 如果值已存在
     */
    @Override
    V put(K key, V value);

    /**
     * Force put: removes any existing entry with the same value before inserting.
     * 强制放入：在插入前移除任何具有相同值的现有条目。
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return the previous value for the key, or null | 该键之前的值，或 null
     */
    V forcePut(K key, V value);

    // ==================== 反向视图 | Inverse View ====================

    /**
     * Return the inverse view of this bimap.
     * 返回此双向映射的反向视图。
     *
     * @return inverse bimap | 反向双向映射
     */
    BiMap<V, K> inverse();

    // ==================== 集合视图 | Set Views ====================

    /**
     * Return a set view of the values.
     * 返回值的集合视图。
     *
     * @return value set | 值集合
     */
    @Override
    Set<V> values();
}
