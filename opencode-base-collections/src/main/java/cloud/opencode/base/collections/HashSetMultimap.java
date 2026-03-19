package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;

import java.io.Serial;
import java.util.*;

/**
 * HashSetMultimap - Set-based Multimap Implementation
 * HashSetMultimap - 基于集合的多重映射实现
 *
 * <p>A Multimap implementation that uses HashSet for each key's values.
 * Duplicate values per key are not allowed.</p>
 * <p>使用 HashSet 存储每个键的值的 Multimap 实现。不允许每个键有重复值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>No duplicate values per key - 每个键无重复值</li>
 *   <li>O(1) contains check - O(1) 包含检查</li>
 *   <li>O(1) put operations - O(1) 放入操作</li>
 *   <li>Serializable - 可序列化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create empty - 创建空
 * HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
 *
 * // Create with capacity - 创建指定容量
 * HashSetMultimap<String, Integer> multimap = HashSetMultimap.create(16, 4);
 *
 * // Create from existing - 从现有创建
 * HashSetMultimap<String, Integer> multimap = HashSetMultimap.create(existingMultimap);
 *
 * // Operations - 操作
 * multimap.put("a", 1);
 * multimap.put("a", 2);
 * multimap.put("a", 1);  // ignored (duplicate)
 * Set<Integer> values = (Set<Integer>) multimap.get("a");  // [1, 2]
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>put: O(1) average - put: O(1) 平均</li>
 *   <li>get: O(1) - get: O(1)</li>
 *   <li>remove: O(1) average - remove: O(1) 平均</li>
 *   <li>containsEntry: O(1) average - containsEntry: O(1) 平均</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Yes (allows null keys and values) - 空值安全: 是（允许空键和值）</li>
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
public class HashSetMultimap<K, V> extends AbstractMultimap<K, V> {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_KEY_CAPACITY = 16;
    private static final int DEFAULT_VALUE_CAPACITY = 4;

    private final int expectedValuesPerKey;

    // ==================== 构造方法 | Constructors ====================

    /**
     * Private constructor.
     * 私有构造方法。
     *
     * @param expectedKeys       expected number of keys | 预期键数量
     * @param expectedValuesPerKey expected values per key | 预期每键值数量
     */
    private HashSetMultimap(int expectedKeys, int expectedValuesPerKey) {
        super(new HashMap<>(expectedKeys));
        this.expectedValuesPerKey = expectedValuesPerKey;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty HashSetMultimap.
     * 创建空 HashSetMultimap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new HashSetMultimap | 新的 HashSetMultimap
     */
    public static <K, V> HashSetMultimap<K, V> create() {
        return new HashSetMultimap<>(DEFAULT_KEY_CAPACITY, DEFAULT_VALUE_CAPACITY);
    }

    /**
     * Create an empty HashSetMultimap with expected sizes.
     * 创建指定预期大小的空 HashSetMultimap。
     *
     * @param <K>                  key type | 键类型
     * @param <V>                  value type | 值类型
     * @param expectedKeys         expected number of keys | 预期键数量
     * @param expectedValuesPerKey expected values per key | 预期每键值数量
     * @return new HashSetMultimap | 新的 HashSetMultimap
     */
    public static <K, V> HashSetMultimap<K, V> create(int expectedKeys, int expectedValuesPerKey) {
        if (expectedKeys < 0) {
            throw OpenCollectionException.illegalCapacity(expectedKeys);
        }
        if (expectedValuesPerKey < 0) {
            throw OpenCollectionException.illegalCapacity(expectedValuesPerKey);
        }
        return new HashSetMultimap<>(expectedKeys, expectedValuesPerKey);
    }

    /**
     * Create a HashSetMultimap from an existing multimap.
     * 从现有多重映射创建 HashSetMultimap。
     *
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @param multimap source multimap | 源多重映射
     * @return new HashSetMultimap | 新的 HashSetMultimap
     */
    public static <K, V> HashSetMultimap<K, V> create(Multimap<? extends K, ? extends V> multimap) {
        HashSetMultimap<K, V> result = create();
        result.putAll(multimap);
        return result;
    }

    // ==================== 抽象方法实现 | Abstract Method Implementation ====================

    @Override
    protected Collection<V> createCollection() {
        return new HashSet<>(expectedValuesPerKey);
    }

    // ==================== Set 特定方法 | Set-Specific Methods ====================

    /**
     * Get the set of values for a key.
     * 获取键的值集合。
     *
     * @param key the key | 键
     * @return values set | 值集合
     */
    public Set<V> getSet(K key) {
        Collection<V> collection = map.get(key);
        if (collection == null) {
            return new HashSet<>();
        }
        return (Set<V>) collection;
    }
}
