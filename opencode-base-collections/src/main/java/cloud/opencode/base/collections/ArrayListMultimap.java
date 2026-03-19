package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;

import java.io.Serial;
import java.util.*;

/**
 * ArrayListMultimap - List-based Multimap Implementation
 * ArrayListMultimap - 基于列表的多重映射实现
 *
 * <p>A Multimap implementation that uses ArrayList for each key's values.
 * Values are stored in insertion order and duplicates are allowed.</p>
 * <p>使用 ArrayList 存储每个键的值的 Multimap 实现。值按插入顺序存储，允许重复。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Preserves insertion order - 保留插入顺序</li>
 *   <li>Allows duplicate values - 允许重复值</li>
 *   <li>O(1) put operations - O(1) 放入操作</li>
 *   <li>Serializable - 可序列化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create empty - 创建空
 * ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
 *
 * // Create with capacity - 创建指定容量
 * ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create(16, 4);
 *
 * // Create from existing - 从现有创建
 * ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create(existingMultimap);
 *
 * // Operations - 操作
 * multimap.put("a", 1);
 * multimap.put("a", 2);
 * multimap.put("a", 1);  // duplicate allowed
 * List<Integer> values = (List<Integer>) multimap.get("a");  // [1, 2, 1]
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>put: O(1) amortized - put: O(1) 均摊</li>
 *   <li>get: O(1) - get: O(1)</li>
 *   <li>remove: O(n) for value in list - remove: O(n) 对于列表中的值</li>
 *   <li>containsEntry: O(n) - containsEntry: O(n)</li>
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
public class ArrayListMultimap<K, V> extends AbstractMultimap<K, V> {

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
    private ArrayListMultimap(int expectedKeys, int expectedValuesPerKey) {
        super(new HashMap<>(expectedKeys));
        this.expectedValuesPerKey = expectedValuesPerKey;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty ArrayListMultimap.
     * 创建空 ArrayListMultimap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new ArrayListMultimap | 新的 ArrayListMultimap
     */
    public static <K, V> ArrayListMultimap<K, V> create() {
        return new ArrayListMultimap<>(DEFAULT_KEY_CAPACITY, DEFAULT_VALUE_CAPACITY);
    }

    /**
     * Create an empty ArrayListMultimap with expected sizes.
     * 创建指定预期大小的空 ArrayListMultimap。
     *
     * @param <K>                  key type | 键类型
     * @param <V>                  value type | 值类型
     * @param expectedKeys         expected number of keys | 预期键数量
     * @param expectedValuesPerKey expected values per key | 预期每键值数量
     * @return new ArrayListMultimap | 新的 ArrayListMultimap
     */
    public static <K, V> ArrayListMultimap<K, V> create(int expectedKeys, int expectedValuesPerKey) {
        if (expectedKeys < 0) {
            throw OpenCollectionException.illegalCapacity(expectedKeys);
        }
        if (expectedValuesPerKey < 0) {
            throw OpenCollectionException.illegalCapacity(expectedValuesPerKey);
        }
        return new ArrayListMultimap<>(expectedKeys, expectedValuesPerKey);
    }

    /**
     * Create an ArrayListMultimap from an existing multimap.
     * 从现有多重映射创建 ArrayListMultimap。
     *
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @param multimap source multimap | 源多重映射
     * @return new ArrayListMultimap | 新的 ArrayListMultimap
     */
    public static <K, V> ArrayListMultimap<K, V> create(Multimap<? extends K, ? extends V> multimap) {
        ArrayListMultimap<K, V> result = create();
        result.putAll(multimap);
        return result;
    }

    // ==================== 抽象方法实现 | Abstract Method Implementation ====================

    @Override
    protected Collection<V> createCollection() {
        return new ArrayList<>(expectedValuesPerKey);
    }

    // ==================== List 特定方法 | List-Specific Methods ====================

    /**
     * Get the list of values for a key.
     * 获取键的值列表。
     *
     * @param key the key | 键
     * @return values list | 值列表
     */
    public List<V> getList(K key) {
        Collection<V> collection = map.get(key);
        if (collection == null) {
            return new ArrayList<>();
        }
        return (List<V>) collection;
    }
}
