package cloud.opencode.base.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Cache Core Interface - High-performance local cache with synchronous API
 * 缓存核心接口 - 高性能本地缓存同步 API
 *
 * <p>Provides comprehensive cache operations including get, put, invalidate and statistics.</p>
 * <p>提供完整的缓存操作，包括获取、放入、失效和统计功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Basic operations (get, put, invalidate) - 基本操作（获取、放入、失效）</li>
 *   <li>Batch operations (getAll, putAll, invalidateAll) - 批量操作</li>
 *   <li>Compute if absent with loader - 不存在时加载</li>
 *   <li>Statistics and monitoring - 统计与监控</li>
 *   <li>Async view support - 异步视图支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get value - 获取值
 * User user = cache.get("user:1001");
 *
 * // Get or load - 获取或加载
 * User user = cache.get("user:1001", key -> userService.findById(key));
 *
 * // Put value - 放入值
 * cache.put("user:1001", user);
 *
 * // Invalidate - 失效
 * cache.invalidate("user:1001");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (null values not allowed) - 空值安全: 部分（不允许 null 值）</li>
 * </ul>
 *
 * @param <K> the type of keys | 键类型
 * @param <V> the type of values | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public interface Cache<K, V> {

    // ==================== Basic Operations | 基本操作 ====================

    /**
     * Get value by key, returns null if not present
     * 根据键获取值，不存在返回 null
     *
     * @param key the key | 键
     * @return the value or null | 值或 null
     */
    V get(K key);

    /**
     * Get value by key, load using loader if not present
     * 根据键获取值，不存在时通过 loader 加载
     *
     * @param key    the key | 键
     * @param loader the loader function | 加载函数
     * @return the value | 值
     */
    V get(K key, Function<? super K, ? extends V> loader);

    /**
     * Get all values for given keys, returns only existing entries
     * 批量获取，仅返回存在的条目
     *
     * @param keys the keys | 键集合
     * @return map of existing entries | 存在的条目 Map
     */
    Map<K, V> getAll(Iterable<? extends K> keys);

    /**
     * Get all values, load missing keys using loader
     * 批量获取，缺失的键通过 loader 加载
     *
     * @param keys   the keys | 键集合
     * @param loader the batch loader function | 批量加载函数
     * @return map of all entries | 所有条目 Map
     */
    Map<K, V> getAll(Iterable<? extends K> keys,
                     Function<? super Set<? extends K>, ? extends Map<K, V>> loader);

    /**
     * Put a key-value pair into cache
     * 放入键值对
     *
     * @param key   the key | 键
     * @param value the value | 值
     */
    void put(K key, V value);

    /**
     * Put all key-value pairs into cache
     * 批量放入
     *
     * @param map the key-value pairs | 键值对 Map
     */
    void putAll(Map<? extends K, ? extends V> map);

    /**
     * Put if the key is absent, returns true if successful
     * 不存在时放入，成功返回 true
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return true if put successfully | 成功放入返回 true
     */
    boolean putIfAbsent(K key, V value);

    /**
     * Put a key-value pair with custom TTL (overrides default expiration)
     * 放入键值对并指定自定义 TTL（覆盖默认过期时间）
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @param ttl   the time-to-live for this entry | 此条目的存活时间
     */
    void putWithTtl(K key, V value, java.time.Duration ttl);

    /**
     * Put all entries with custom TTL
     * 批量放入并指定自定义 TTL
     *
     * @param map the key-value pairs | 键值对 Map
     * @param ttl the time-to-live for all entries | 所有条目的存活时间
     */
    void putAllWithTtl(Map<? extends K, ? extends V> map, java.time.Duration ttl);

    /**
     * Put if absent with custom TTL
     * 不存在时放入并指定自定义 TTL
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @param ttl   the time-to-live | 存活时间
     * @return true if put successfully | 成功放入返回 true
     */
    boolean putIfAbsentWithTtl(K key, V value, java.time.Duration ttl);

    // ==================== Compute Operations | 计算操作 ====================

    /**
     * Get value or return default if not present
     * 获取值，不存在则返回默认值
     *
     * @param key          the key | 键
     * @param defaultValue the default value | 默认值
     * @return the value or default | 值或默认值
     */
    default V getOrDefault(K key, V defaultValue) {
        V value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Compute a new value if key is present
     * 如果键存在则计算新值
     *
     * <p>If the key is present and the remapping function returns non-null,
     * the value is replaced. If the function returns null, the entry is removed.</p>
     * <p>如果键存在且映射函数返回非 null，则替换值。如果函数返回 null，则删除条目。</p>
     *
     * @param key               the key | 键
     * @param remappingFunction the function to compute new value | 计算新值的函数
     * @return the new value or null if removed/absent | 新值，如果被删除或不存在则返回 null
     */
    default V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return asMap().computeIfPresent(key, remappingFunction);
    }

    /**
     * Compute a new value for the key
     * 计算键的新值
     *
     * <p>Atomically computes a new value. If the key is absent and the function
     * returns non-null, a new entry is created. If the key is present and the
     * function returns null, the entry is removed.</p>
     * <p>原子地计算新值。如果键不存在且函数返回非 null，则创建新条目。
     * 如果键存在且函数返回 null，则删除条目。</p>
     *
     * @param key               the key | 键
     * @param remappingFunction the function to compute value | 计算值的函数
     * @return the new value or null if removed | 新值，如果被删除则返回 null
     */
    default V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return asMap().compute(key, remappingFunction);
    }

    /**
     * Get value and remove the entry atomically
     * 原子地获取值并删除条目
     *
     * @param key the key | 键
     * @return the value or null if not present | 值，不存在则返回 null
     */
    default V getAndRemove(K key) {
        return asMap().remove(key);
    }

    /**
     * Replace value only if key is present
     * 仅在键存在时替换值
     *
     * @param key   the key | 键
     * @param value the new value | 新值
     * @return the old value or null if not present | 旧值，不存在则返回 null
     */
    default V replace(K key, V value) {
        return asMap().replace(key, value);
    }

    /**
     * Replace value only if current value equals expected value
     * 仅在当前值等于期望值时替换
     *
     * @param key      the key | 键
     * @param oldValue the expected current value | 期望的当前值
     * @param newValue the new value | 新值
     * @return true if replaced | 替换成功返回 true
     */
    default boolean replace(K key, V oldValue, V newValue) {
        return asMap().replace(key, oldValue, newValue);
    }

    /**
     * Get value as Optional, distinguishing "not present" from "null value"
     * 以 Optional 形式获取值，区分"不存在"和"值为 null"
     *
     * <p>Unlike {@link #get(Object)}, this method can distinguish between a key
     * that is not present and a key that is present with a null value.</p>
     * <p>与 {@link #get(Object)} 不同，此方法可以区分键不存在和键存在但值为 null 的情况。</p>
     *
     * @param key the key | 键
     * @return Optional containing the value, or empty if key not present | 包含值的 Optional，键不存在则返回空
     * @since V2.0.2
     */
    default java.util.Optional<V> getOptional(K key) {
        if (containsKey(key)) {
            return java.util.Optional.ofNullable(get(key));
        }
        return java.util.Optional.empty();
    }

    /**
     * Get value only if present, without triggering loader
     * 仅在存在时获取值，不触发加载器
     *
     * <p>This is useful when you want to check the cache without side effects.</p>
     * <p>当您想检查缓存而不产生副作用时，此方法很有用。</p>
     *
     * @param key the key | 键
     * @return Optional containing the value, or empty if not present | 包含值的 Optional，不存在则返回空
     * @since V2.0.2
     */
    default java.util.Optional<V> getIfPresent(K key) {
        return getOptional(key);
    }

    /**
     * Merge a value with existing value using the given function
     * 使用给定函数合并值与现有值
     *
     * <p>If the key is absent, the provided value is used. If the key is present,
     * the merge function is called with the old and new values.</p>
     * <p>如果键不存在，使用提供的值。如果键存在，使用旧值和新值调用合并函数。</p>
     *
     * @param key           the key | 键
     * @param value         the value to merge | 要合并的值
     * @param mergeFunction the function to merge values | 合并函数
     * @return the new value | 新值
     * @since V2.0.2
     */
    default V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> mergeFunction) {
        V oldValue = get(key);
        V newValue = (oldValue == null) ? value : mergeFunction.apply(oldValue, value);
        if (newValue == null) {
            invalidate(key);
        } else {
            put(key, newValue);
        }
        return newValue;
    }

    /**
     * Compute value if absent (alias for get with loader for Map API compatibility)
     * 不存在时计算值（为 Map API 兼容性提供的 get with loader 别名）
     *
     * @param key             the key | 键
     * @param mappingFunction the function to compute value | 计算值的函数
     * @return the value | 值
     * @since V2.0.2
     */
    default V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return get(key, mappingFunction);
    }

    /**
     * Put value only if key is present (update existing entry only)
     * 仅在键存在时放入值（仅更新现有条目）
     *
     * <p>Unlike {@link #put(Object, Object)}, this does not create a new entry
     * if the key is not present.</p>
     * <p>与 {@link #put(Object, Object)} 不同，如果键不存在则不创建新条目。</p>
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return true if updated, false if key was not present | 更新成功返回 true，键不存在返回 false
     * @since V2.0.3
     */
    default boolean putIfPresent(K key, V value) {
        if (containsKey(key)) {
            put(key, value);
            return true;
        }
        return false;
    }

    /**
     * Check if cache contains a specific value
     * 检查缓存是否包含特定值
     *
     * <p>Warning: This operation is O(n) as it scans all values.</p>
     * <p>警告：此操作为 O(n)，因为它扫描所有值。</p>
     *
     * @param value the value to check | 要检查的值
     * @return true if value exists | 值存在返回 true
     * @since V2.0.3
     */
    default boolean containsValue(V value) {
        if (value == null) {
            return false;
        }
        for (V v : values()) {
            if (value.equals(v)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get value or null if not present (explicit null return)
     * 获取值，不存在时显式返回 null
     *
     * <p>This is semantically equivalent to {@link #get(Object)} but makes
     * the null return value explicit in the method name.</p>
     * <p>这在语义上等同于 {@link #get(Object)}，但在方法名中明确表示返回 null。</p>
     *
     * @param key the key | 键
     * @return the value or null | 值或 null
     * @since V2.0.3
     */
    default V getOrNull(K key) {
        return get(key);
    }

    /**
     * Replace all values using the given function
     * 使用给定函数替换所有值
     *
     * @param function the function to transform values | 转换值的函数
     * @return count of replaced entries | 替换的条目数
     * @since V2.0.3
     */
    default int replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        int count = 0;
        for (Map.Entry<K, V> entry : entries()) {
            V newValue = function.apply(entry.getKey(), entry.getValue());
            if (newValue != null) {
                put(entry.getKey(), newValue);
                count++;
            } else {
                invalidate(entry.getKey());
                count++;
            }
        }
        return count;
    }

    /**
     * Iterate over all values with a consumer
     * 使用消费者遍历所有值
     *
     * @param action the action to perform on each value | 对每个值执行的操作
     * @since V2.0.3
     */
    default void forEachValue(java.util.function.Consumer<? super V> action) {
        for (V value : values()) {
            action.accept(value);
        }
    }

    /**
     * Iterate over all keys with a consumer
     * 使用消费者遍历所有键
     *
     * @param action the action to perform on each key | 对每个键执行的操作
     * @since V2.0.3
     */
    default void forEachKey(java.util.function.Consumer<? super K> action) {
        for (K key : keys()) {
            action.accept(key);
        }
    }

    /**
     * Remove entry only if key is present and return success status
     * 仅在键存在时移除条目，并返回成功状态
     *
     * @param key the key | 键
     * @return true if removed, false if key was not present | 移除成功返回 true，键不存在返回 false
     * @since V2.0.3
     */
    default boolean removeIfPresent(K key) {
        if (containsKey(key)) {
            invalidate(key);
            return true;
        }
        return false;
    }

    /**
     * Remove entry only if value matches the expected value
     * 仅在值与期望值匹配时移除条目
     *
     * @param key   the key | 键
     * @param value the expected value | 期望的值
     * @return true if removed | 移除成功返回 true
     * @since V2.0.3
     */
    default boolean removeIfEquals(K key, V value) {
        V current = get(key);
        if (current != null && current.equals(value)) {
            invalidate(key);
            return true;
        }
        return false;
    }

    // ==================== Batch Operations | 批量操作 ====================

    /**
     * Get entries matching a key pattern
     * 获取匹配键模式的条目
     *
     * <p>Pattern syntax:</p>
     * <ul>
     *   <li>{@code *} - matches any sequence of characters</li>
     *   <li>{@code ?} - matches any single character</li>
     * </ul>
     *
     * @param pattern the pattern to match | 匹配模式
     * @return map of matching entries | 匹配的条目 Map
     * @since V2.0.2
     */
    default Map<K, V> getByPattern(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return Map.of();
        }
        String regex = globToRegex(pattern);
        java.util.regex.Pattern compiledPattern = java.util.regex.Pattern.compile(regex);
        java.util.Map<K, V> result = new java.util.LinkedHashMap<>();
        for (Map.Entry<K, V> entry : entries()) {
            if (entry.getKey() != null && compiledPattern.matcher(entry.getKey().toString()).matches()) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Put all entries if their keys are absent
     * 批量放入不存在的键的条目
     *
     * @param map the entries to put | 要放入的条目
     * @return count of entries actually put | 实际放入的条目数
     * @since V2.0.2
     */
    default int putAllIfAbsent(Map<? extends K, ? extends V> map) {
        int count = 0;
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            if (putIfAbsent(entry.getKey(), entry.getValue())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Update only existing entries (does not insert new ones)
     * 仅更新现有条目（不插入新条目）
     *
     * @param map the entries to update | 要更新的条目
     * @return count of entries actually updated | 实际更新的条目数
     * @since V2.0.2
     */
    default int updateAll(Map<? extends K, ? extends V> map) {
        int count = 0;
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            if (containsKey(entry.getKey())) {
                put(entry.getKey(), entry.getValue());
                count++;
            }
        }
        return count;
    }

    // ==================== Invalidation | 失效操作 ====================

    /**
     * Invalidate a single key
     * 使单个键失效
     *
     * @param key the key to invalidate | 要失效的键
     */
    void invalidate(K key);

    /**
     * Invalidate multiple keys
     * 批量失效
     *
     * @param keys the keys to invalidate | 要失效的键集合
     */
    void invalidateAll(Iterable<? extends K> keys);

    /**
     * Invalidate all entries
     * 清空所有缓存
     */
    void invalidateAll();

    /**
     * Clear all entries (alias for invalidateAll, for Map API compatibility)
     * 清空所有条目（invalidateAll 的别名，兼容 Map API）
     *
     * @since V2.0.1
     */
    default void clear() {
        invalidateAll();
    }

    /**
     * Invalidate entries matching a pattern (supports * and ? wildcards)
     * 使匹配模式的条目失效（支持 * 和 ? 通配符）
     *
     * <p>Pattern syntax:</p>
     * <ul>
     *   <li>{@code *} - matches any sequence of characters</li>
     *   <li>{@code ?} - matches any single character</li>
     * </ul>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * cache.invalidateByPattern("user:*");     // Invalidate all user entries
     * cache.invalidateByPattern("*.temp");     // Invalidate entries ending with .temp
     * cache.invalidateByPattern("session:???"); // Invalidate 3-char session IDs
     * }</pre>
     *
     * @param pattern the pattern to match (supports * and ? wildcards) | 匹配模式
     * @return count of invalidated entries | 失效的条目数
     * @since V1.9.0
     */
    default long invalidateByPattern(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return 0;
        }

        // Convert glob pattern to regex
        String regex = globToRegex(pattern);
        java.util.regex.Pattern compiledPattern = java.util.regex.Pattern.compile(regex);

        long count = 0;
        for (K key : keys()) {
            if (key != null && compiledPattern.matcher(key.toString()).matches()) {
                invalidate(key);
                count++;
            }
        }
        return count;
    }

    /**
     * Invalidate entries matching a predicate
     * 使满足条件的条目失效
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * // Invalidate all entries with keys starting with "temp:"
     * cache.invalidateIf(key -> key.toString().startsWith("temp:"));
     *
     * // Invalidate entries with numeric keys greater than 1000
     * cache.invalidateIf(key -> ((Integer)key) > 1000);
     * }</pre>
     *
     * @param predicate the predicate to test keys | 键测试条件
     * @return count of invalidated entries | 失效的条目数
     * @since V1.9.0
     */
    default long invalidateIf(java.util.function.Predicate<K> predicate) {
        if (predicate == null) {
            return 0;
        }

        long count = 0;
        for (K key : keys()) {
            if (key != null && predicate.test(key)) {
                invalidate(key);
                count++;
            }
        }
        return count;
    }

    /**
     * Invalidate entries matching a value predicate
     * 使值满足条件的条目失效
     *
     * @param predicate the predicate to test values | 值测试条件
     * @return count of invalidated entries | 失效的条目数
     * @since V1.9.0
     */
    default long invalidateByValue(java.util.function.Predicate<V> predicate) {
        if (predicate == null) {
            return 0;
        }

        long count = 0;
        for (Map.Entry<K, V> entry : entries()) {
            if (entry.getValue() != null && predicate.test(entry.getValue())) {
                invalidate(entry.getKey());
                count++;
            }
        }
        return count;
    }

    /**
     * Convert glob pattern to regex
     * 将 glob 模式转换为正则表达式
     */
    private static String globToRegex(String glob) {
        StringBuilder regex = new StringBuilder("^");
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            switch (c) {
                case '*' -> regex.append(".*");
                case '?' -> regex.append(".");
                case '.' -> regex.append("\\.");
                case '\\' -> regex.append("\\\\");
                case '[', ']', '(', ')', '{', '}', '^', '$', '|', '+' -> regex.append("\\").append(c);
                default -> regex.append(c);
            }
        }
        regex.append("$");
        return regex.toString();
    }

    // ==================== Query Operations | 查询操作 ====================

    /**
     * Check if key exists
     * 检查键是否存在
     *
     * @param key the key | 键
     * @return true if exists | 存在返回 true
     */
    boolean containsKey(K key);

    /**
     * Get exact entry count (may be slow)
     * 获取精确条目数（可能较慢）
     *
     * @return entry count | 条目数
     */
    long size();

    /**
     * Get estimated entry count (fast)
     * 获取估算条目数（快速）
     *
     * @return estimated count | 估算数量
     */
    long estimatedSize();

    /**
     * Check if cache is empty
     * 检查缓存是否为空
     *
     * @return true if empty | 为空返回 true
     * @since V2.0.1
     */
    default boolean isEmpty() {
        return estimatedSize() == 0;
    }

    /**
     * Get snapshot of all keys
     * 获取所有键的快照
     *
     * @return set of keys | 键集合
     */
    Set<K> keys();

    /**
     * Get snapshot of all values
     * 获取所有值的快照
     *
     * @return collection of values | 值集合
     */
    Collection<V> values();

    /**
     * Get snapshot of all entries
     * 获取所有条目的快照
     *
     * @return set of entries | 条目集合
     */
    Set<Map.Entry<K, V>> entries();

    /**
     * Get a concurrent map view of this cache
     * 获取 ConcurrentMap 视图
     *
     * @return concurrent map view | ConcurrentMap 视图
     */
    ConcurrentMap<K, V> asMap();

    // ==================== Lazy Iteration | 惰性迭代 ====================

    /**
     * Get a lazy iterator over keys (memory efficient)
     * 获取键的惰性迭代器（内存高效）
     *
     * <p>Unlike {@link #keys()}, this does not create a snapshot copy.</p>
     * <p>与 {@link #keys()} 不同，这不会创建快照副本。</p>
     *
     * @return lazy iterator over keys | 键的惰性迭代器
     * @since V1.9.0
     */
    default java.util.Iterator<K> keyIterator() {
        return asMap().keySet().iterator();
    }

    /**
     * Get a lazy iterator over entries (memory efficient)
     * 获取条目的惰性迭代器（内存高效）
     *
     * @return lazy iterator over entries | 条目的惰性迭代器
     * @since V1.9.0
     */
    default java.util.Iterator<Map.Entry<K, V>> entryIterator() {
        return asMap().entrySet().iterator();
    }

    /**
     * Iterate over all entries with a consumer (memory efficient)
     * 使用消费者遍历所有条目（内存高效）
     *
     * @param action the action to perform | 要执行的操作
     * @since V1.9.0
     */
    default void forEach(java.util.function.BiConsumer<? super K, ? super V> action) {
        asMap().forEach(action);
    }

    // ==================== Lazy Stream API | 惰性流 API ====================

    /**
     * Get a lazy stream over keys (memory efficient)
     * 获取键的惰性流（内存高效）
     *
     * <p>Creates a stream directly from the underlying map without creating intermediate collections.
     * Ideal for large caches where collecting all keys would be expensive.</p>
     * <p>直接从底层映射创建流，不创建中间集合。适用于收集所有键成本较高的大型缓存。</p>
     *
     * @return lazy stream over keys | 键的惰性流
     * @since V2.0.0
     */
    default java.util.stream.Stream<K> keyStream() {
        return java.util.stream.StreamSupport.stream(
                java.util.Spliterators.spliteratorUnknownSize(keyIterator(), java.util.Spliterator.NONNULL),
                false
        );
    }

    /**
     * Get a lazy stream over values (memory efficient)
     * 获取值的惰性流（内存高效）
     *
     * @return lazy stream over values | 值的惰性流
     * @since V2.0.0
     */
    default java.util.stream.Stream<V> valueStream() {
        return entryStream().map(Map.Entry::getValue);
    }

    /**
     * Get a lazy stream over entries (memory efficient)
     * 获取条目的惰性流（内存高效）
     *
     * @return lazy stream over entries | 条目的惰性流
     * @since V2.0.0
     */
    default java.util.stream.Stream<Map.Entry<K, V>> entryStream() {
        return java.util.stream.StreamSupport.stream(
                java.util.Spliterators.spliteratorUnknownSize(entryIterator(), java.util.Spliterator.NONNULL),
                false
        );
    }

    /**
     * Get a parallel stream over keys
     * 获取键的并行流
     *
     * <p>Use for CPU-intensive operations on large caches.</p>
     * <p>用于大型缓存上的 CPU 密集型操作。</p>
     *
     * @return parallel stream over keys | 键的并行流
     * @since V2.0.0
     */
    default java.util.stream.Stream<K> keyParallelStream() {
        return keyStream().parallel();
    }

    /**
     * Get a parallel stream over values
     * 获取值的并行流
     *
     * @return parallel stream over values | 值的并行流
     * @since V2.0.0
     */
    default java.util.stream.Stream<V> valueParallelStream() {
        return valueStream().parallel();
    }

    /**
     * Get a parallel stream over entries
     * 获取条目的并行流
     *
     * @return parallel stream over entries | 条目的并行流
     * @since V2.0.0
     */
    default java.util.stream.Stream<Map.Entry<K, V>> entryParallelStream() {
        return entryStream().parallel();
    }

    // ==================== Batch TTL Operations | 批量 TTL 操作 ====================

    /**
     * Update TTL for existing entries matching predicate
     * 更新匹配条件的现有条目的 TTL
     *
     * <p>Note: This re-puts entries with new TTL. Original values are preserved.</p>
     * <p>注意：这会用新的 TTL 重新放入条目。原始值保持不变。</p>
     *
     * @param predicate condition for keys to update | 要更新的键条件
     * @param ttl       new TTL | 新的 TTL
     * @return count of updated entries | 更新的条目数
     * @since V1.9.0
     */
    default long updateTtl(java.util.function.Predicate<K> predicate, java.time.Duration ttl) {
        long count = 0;
        for (Map.Entry<K, V> entry : entries()) {
            if (predicate.test(entry.getKey())) {
                putWithTtl(entry.getKey(), entry.getValue(), ttl);
                count++;
            }
        }
        return count;
    }

    /**
     * Update TTL for all entries
     * 更新所有条目的 TTL
     *
     * @param ttl new TTL | 新的 TTL
     * @return count of updated entries | 更新的条目数
     * @since V1.9.0
     */
    default long updateTtlAll(java.time.Duration ttl) {
        return updateTtl(k -> true, ttl);
    }

    /**
     * Update TTL for specific keys
     * 更新指定键的 TTL
     *
     * @param keys keys to update | 要更新的键
     * @param ttl  new TTL | 新的 TTL
     * @return count of updated entries | 更新的条目数
     * @since V1.9.0
     */
    default long updateTtl(Iterable<? extends K> keys, java.time.Duration ttl) {
        long count = 0;
        for (K key : keys) {
            V value = get(key);
            if (value != null) {
                putWithTtl(key, value, ttl);
                count++;
            }
        }
        return count;
    }

    // ==================== Statistics & Management | 统计与管理 ====================

    /**
     * Get cache statistics
     * 获取缓存统计信息
     *
     * @return cache statistics | 缓存统计
     */
    CacheStats stats();

    /**
     * Get detailed cache metrics with latency percentiles
     * 获取带延迟百分位数的详细缓存指标
     *
     * @return cache metrics or null if not enabled | 缓存指标，未启用则返回 null
     */
    default CacheMetrics metrics() {
        return null;
    }

    /**
     * Reset statistics counters
     * 重置统计计数器
     *
     * <p>Clears all hit, miss, load, and eviction counters to zero.
     * Also resets metrics if enabled.</p>
     * <p>将所有命中、未命中、加载和淘汰计数器清零。如果启用，也重置指标。</p>
     *
     * @since V2.0.3
     */
    default void resetStats() {
        // Default no-op - implementations should override
        CacheMetrics m = metrics();
        if (m != null) {
            m.reset();
        }
    }

    /**
     * Perform cleanup (expired entries, etc.)
     * 执行清理（过期条目等）
     */
    void cleanUp();

    /**
     * Get async view of this cache
     * 获取异步视图
     *
     * @return async cache view | 异步缓存视图
     */
    AsyncCache<K, V> async();

    /**
     * Get cache name
     * 获取缓存名称
     *
     * @return cache name | 缓存名称
     */
    String name();
}
