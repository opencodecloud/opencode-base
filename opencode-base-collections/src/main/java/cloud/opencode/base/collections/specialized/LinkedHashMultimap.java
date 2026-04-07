package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.AbstractMultimap;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * LinkedHashMultimap - Insertion-Ordered Multimap Implementation
 * LinkedHashMultimap - 保持插入顺序的多重映射实现
 *
 * <p>A Multimap that preserves the insertion order of both keys and values.
 * Uses LinkedHashMap for keys and LinkedHashSet for values, ensuring
 * iteration order matches insertion order while preventing duplicate values per key.</p>
 * <p>保持键和值插入顺序的多重映射。使用 LinkedHashMap 存储键，LinkedHashSet 存储值，
 * 确保迭代顺序与插入顺序一致，同时防止每个键有重复值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Preserves insertion order of keys - 保持键的插入顺序</li>
 *   <li>Preserves insertion order of values per key - 保持每个键的值的插入顺序</li>
 *   <li>No duplicate values per key (Set semantics) - 每个键无重复值（集合语义）</li>
 *   <li>O(1) put and contains operations - O(1) 放入和包含操作</li>
 *   <li>Serializable - 可序列化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create empty - 创建空映射
 * LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();
 *
 * // Create with expected key count - 创建指定预期键数
 * LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create(16);
 *
 * // Operations - 操作
 * multimap.put("b", 2);
 * multimap.put("a", 1);
 * multimap.put("a", 3);
 * multimap.put("a", 1);  // ignored (duplicate)
 *
 * // Iteration preserves insertion order
 * // keySet: [b, a]
 * // get("a"): [1, 3]
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>put: O(1) average - put: O(1) 平均</li>
 *   <li>get: O(1) - get: O(1)</li>
 *   <li>remove: O(1) average - remove: O(1) 平均</li>
 *   <li>containsEntry: O(1) average - containsEntry: O(1) 平均</li>
 *   <li>Iteration: insertion order - 迭代: 插入顺序</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Allows null keys and values - 空值安全: 允许空键和空值</li>
 * </ul>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
public final class LinkedHashMultimap<K, V> extends AbstractMultimap<K, V> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==================== 构造方法 | Constructors ====================

    /**
     * Private constructor with default capacity.
     * 默认容量的私有构造方法。
     */
    private LinkedHashMultimap() {
        super(new LinkedHashMap<>());
    }

    /**
     * Private constructor with expected key count.
     * 指定预期键数的私有构造方法。
     *
     * @param expectedKeys expected number of keys | 预期键数
     */
    private LinkedHashMultimap(int expectedKeys) {
        super(new LinkedHashMap<>((int) Math.ceil(expectedKeys / 0.75)));
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create a new empty LinkedHashMultimap.
     * 创建新的空 LinkedHashMultimap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new empty multimap | 新的空多重映射
     */
    public static <K, V> LinkedHashMultimap<K, V> create() {
        return new LinkedHashMultimap<>();
    }

    /**
     * Create a new LinkedHashMultimap with expected key count.
     * 创建指定预期键数的新 LinkedHashMultimap。
     *
     * @param <K>          key type | 键类型
     * @param <V>          value type | 值类型
     * @param expectedKeys expected number of keys | 预期键数
     * @return new multimap | 新的多重映射
     * @throws IllegalArgumentException if expectedKeys is negative | 如果预期键数为负
     */
    public static <K, V> LinkedHashMultimap<K, V> create(int expectedKeys) {
        if (expectedKeys < 0) {
            throw new IllegalArgumentException("expectedKeys must not be negative: " + expectedKeys);
        }
        return new LinkedHashMultimap<>(expectedKeys);
    }

    // ==================== 抽象方法实现 | Abstract Method Implementation ====================

    @Override
    protected Collection<V> createCollection() {
        return new LinkedHashSet<>();
    }
}
