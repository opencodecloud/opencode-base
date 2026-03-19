package cloud.opencode.base.core.builder;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Map Builder - Fluent builder for Map instances
 * Map 构建器 - Map 实例的流式构建器
 *
 * <p>Creates Map instances with fluent API supporting various Map implementations.</p>
 * <p>使用流式 API 创建 Map 实例，支持多种 Map 实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple Map types (HashMap, LinkedHashMap, TreeMap) - 多种 Map 类型</li>
 *   <li>Conditional put (putIfNotNull, putIf) - 条件添加</li>
 *   <li>Batch operations (putAll) - 批量操作</li>
 *   <li>Unmodifiable result (unmodifiable) - 不可变结果</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Map<String, Object> map = MapBuilder.<String, Object>hashMap()
 *     .put("key1", "value1")
 *     .put("key2", "value2")
 *     .putIfNotNull("key3", nullableValue)
 *     .build();
 *
 * Map<String, Integer> immutable = MapBuilder.<String, Integer>of()
 *     .put("a", 1)
 *     .unmodifiable()
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (builder instance not thread-safe) - 线程安全: 否</li>
 *   <li>Null-safe: Yes (values can be null) - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) amortized per put - 每次put均摊 O(1)</li>
 *   <li>Space complexity: O(n) where n = number of entries - O(n), n为条目数</li>
 * </ul>
 *
 * @param <K> Key type - Key 类型
 * @param <V> Value type - Value 类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public class MapBuilder<K, V> implements Builder<Map<K, V>> {

    private final Supplier<Map<K, V>> mapSupplier;
    private final Map<K, V> map;
    private boolean unmodifiable = false;

    public MapBuilder() {
        this(LinkedHashMap::new);
    }

    public MapBuilder(Supplier<Map<K, V>> mapSupplier) {
        this.mapSupplier = mapSupplier;
        this.map = mapSupplier.get();
    }

    /**
     * Creates
     * 创建构建器
     */
    public static <K, V> MapBuilder<K, V> of() {
        return new MapBuilder<>();
    }

    /**
     * Creates
     * 创建构建器（指定 Map 实现）
     */
    public static <K, V> MapBuilder<K, V> of(Supplier<Map<K, V>> mapSupplier) {
        return new MapBuilder<>(mapSupplier);
    }

    /**
     * Creates
     * 创建 HashMap 构建器
     */
    public static <K, V> MapBuilder<K, V> hashMap() {
        return new MapBuilder<>(HashMap::new);
    }

    /**
     * Creates
     * 创建 LinkedHashMap 构建器
     */
    public static <K, V> MapBuilder<K, V> linkedHashMap() {
        return new MapBuilder<>(LinkedHashMap::new);
    }

    /**
     * Creates
     * 创建 TreeMap 构建器
     */
    public static <K extends Comparable<K>, V> MapBuilder<K, V> treeMap() {
        return new MapBuilder<>(TreeMap::new);
    }

    /**
     * Adds
     * 添加键值对
     */
    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    /**
     * Conditionally adds a key-value pair (adds when non-null)
     * 条件添加键值对（非 null 时添加）
     */
    public MapBuilder<K, V> putIfNotNull(K key, V value) {
        if (value != null) {
            map.put(key, value);
        }
        return this;
    }

    /**
     * Conditionally adds a key-value pair
     * 条件添加键值对
     */
    public MapBuilder<K, V> putIf(boolean condition, K key, V value) {
        if (condition) {
            map.put(key, value);
        }
        return this;
    }

    /**
     * Adds in batch
     * 批量添加
     */
    public MapBuilder<K, V> putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
        return this;
    }

    /**
     * Removes a key
     * 移除键
     */
    public MapBuilder<K, V> remove(K key) {
        map.remove(key);
        return this;
    }

    /**
     * Sets
     * 设置为不可变
     */
    public MapBuilder<K, V> unmodifiable() {
        this.unmodifiable = true;
        return this;
    }

    /**
     * Configuration callback
     * 配置回调
     */
    public MapBuilder<K, V> configure(Consumer<MapBuilder<K, V>> consumer) {
        consumer.accept(this);
        return this;
    }

    @Override
    public Map<K, V> build() {
        if (unmodifiable) {
            return Collections.unmodifiableMap(new LinkedHashMap<>(map));
        }
        Map<K, V> result = mapSupplier.get();
        result.putAll(map);
        return result;
    }

    /**
     * Gets
     * 获取当前大小
     */
    public int size() {
        return map.size();
    }

    /**
     * Checks
     * 检查是否包含键
     */
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }
}
