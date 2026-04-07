package cloud.opencode.base.cache;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Tagged Cache - Cache decorator with tag-based batch invalidation
 * 标签缓存 - 支持基于标签批量失效的缓存装饰器
 *
 * <p>Allows associating cache entries with one or more tags, and invalidating
 * all entries associated with a given tag in a single operation.</p>
 * <p>允许将缓存条目与一个或多个标签关联，并在单次操作中失效给定标签下的所有条目。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Tag-based batch invalidation - 基于标签的批量失效</li>
 *   <li>Multi-tag support per entry - 每个条目支持多标签</li>
 *   <li>Bidirectional tag-key index - 双向标签-键索引</li>
 *   <li>Automatic index cleanup on invalidation - 失效时自动清理索引</li>
 *   <li>Thread-safe concurrent operations - 线程安全的并发操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create tagged cache
 * Cache<String, String> base = OpenCache.<String, String>builder()
 *         .maximumSize(1000)
 *         .build();
 * TaggedCache<String, String> cache = TaggedCache.wrap(base);
 *
 * // Put with tags
 * cache.put("user:1", "Alice", "role:admin", "dept:eng");
 * cache.put("user:2", "Bob", "role:user", "dept:eng");
 * cache.put("user:3", "Carol", "role:admin", "dept:hr");
 *
 * // Query by tag
 * Set<String> admins = cache.getKeysByTag("role:admin");  // [user:1, user:3]
 * Set<String> tags = cache.getTags("user:1");             // [role:admin, dept:eng]
 *
 * // Invalidate by tag — removes all entries tagged with "dept:eng"
 * cache.invalidateByTag("dept:eng");  // removes user:1 and user:2
 *
 * // Invalidate by multiple tags
 * cache.invalidateByTags("role:admin", "role:user");
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>put/get/invalidate: O(1) delegate + O(tags) index maintenance - O(1) 委托 + O(标签数) 索引维护</li>
 *   <li>invalidateByTag: O(keys in tag) - O(标签下的键数)</li>
 *   <li>Memory overhead: two ConcurrentHashMap indexes - 内存开销: 两个 ConcurrentHashMap 索引</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap + atomic operations) - 线程安全: 是（ConcurrentHashMap + 原子操作）</li>
 *   <li>Null-safe: Rejects null tags - 空值安全: 拒绝 null 标签</li>
 * </ul>
 *
 * @param <K> the type of keys | 键类型
 * @param <V> the type of values | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.3
 */
public final class TaggedCache<K, V> implements Cache<K, V> {

    private final Cache<K, V> delegate;

    /**
     * Tag to keys index: tag -> Set of keys associated with that tag
     * 标签到键索引: 标签 -> 关联该标签的键集合
     */
    private final ConcurrentHashMap<String, ConcurrentHashMap.KeySetView<K, Boolean>> tagToKeys =
            new ConcurrentHashMap<>();

    /**
     * Key to tags index: key -> Set of tags associated with that key
     * 键到标签索引: 键 -> 关联该键的标签集合
     */
    private final ConcurrentHashMap<K, ConcurrentHashMap.KeySetView<String, Boolean>> keyToTags =
            new ConcurrentHashMap<>();

    private TaggedCache(Cache<K, V> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate cache cannot be null");
    }

    // ==================== Factory Method | 工厂方法 ====================

    /**
     * Wrap an existing cache with tag support
     * 用标签支持包装现有缓存
     *
     * @param cache the cache to wrap | 要包装的缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return tagged cache decorator | 标签缓存装饰器
     */
    public static <K, V> TaggedCache<K, V> wrap(Cache<K, V> cache) {
        Objects.requireNonNull(cache, "cache cannot be null");
        if (cache instanceof TaggedCache<K, V> tagged) {
            return tagged;
        }
        return new TaggedCache<>(cache);
    }

    // ==================== Tagged Operations | 标签操作 ====================

    /**
     * Put a key-value pair with associated tags
     * 放入带标签的键值对
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @param tags  the tags to associate | 关联的标签
     */
    public void put(K key, V value, String... tags) {
        delegate.put(key, value);
        if (tags != null && tags.length > 0) {
            addTagsInternal(key, tags);
        }
    }

    /**
     * Put a key-value pair with TTL and associated tags
     * 放入带 TTL 和标签的键值对
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @param ttl   the time-to-live | 存活时间
     * @param tags  the tags to associate | 关联的标签
     */
    public void putWithTtl(K key, V value, Duration ttl, String... tags) {
        delegate.putWithTtl(key, value, ttl);
        if (tags != null && tags.length > 0) {
            addTagsInternal(key, tags);
        }
    }

    /**
     * Add tags to a key in the tag index
     * 给键追加标签到标签索引
     *
     * <p>Tags are always added to the index. If the key is not currently in the cache,
     * stale tag entries will be cleaned up automatically during the next
     * {@link #invalidate} or {@link #invalidateByTag} operation.</p>
     * <p>标签始终会被添加到索引中。如果键当前不在缓存中，过期的标签条目将在下次
     * {@link #invalidate} 或 {@link #invalidateByTag} 操作时自动清理。</p>
     *
     * @param key  the key | 键
     * @param tags the tags to add | 要追加的标签
     */
    public void addTags(K key, String... tags) {
        Objects.requireNonNull(key, "key cannot be null");
        if (tags == null || tags.length == 0) {
            return;
        }
        addTagsInternal(key, tags);
    }

    /**
     * Get all keys associated with a tag
     * 获取标签下所有键
     *
     * @param tag the tag | 标签
     * @return unmodifiable set of keys, empty if tag not found | 不可变键集合，标签不存在则返回空集
     */
    public Set<K> getKeysByTag(String tag) {
        Objects.requireNonNull(tag, "tag cannot be null");
        ConcurrentHashMap.KeySetView<K, Boolean> keys = tagToKeys.get(tag);
        if (keys == null || keys.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(keys);
    }

    /**
     * Get all tags associated with a key
     * 获取键的所有标签
     *
     * @param key the key | 键
     * @return unmodifiable set of tags, empty if key has no tags | 不可变标签集合，无标签则返回空集
     */
    public Set<String> getTags(K key) {
        Objects.requireNonNull(key, "key cannot be null");
        ConcurrentHashMap.KeySetView<String, Boolean> tags = keyToTags.get(key);
        if (tags == null || tags.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Invalidate all entries associated with the given tag
     * 按标签批量失效所有关联条目
     *
     * @param tag the tag | 标签
     */
    public void invalidateByTag(String tag) {
        Objects.requireNonNull(tag, "tag cannot be null");
        ConcurrentHashMap.KeySetView<K, Boolean> keys = tagToKeys.remove(tag);
        if (keys == null) {
            return;
        }
        for (K key : keys) {
            delegate.invalidate(key);
            // Clean up all tags for this key (not just the triggering tag)
            cleanupTagIndex(key);
        }
    }

    /**
     * Invalidate all entries associated with any of the given tags
     * 按多个标签批量失效所有关联条目
     *
     * @param tags the tags | 标签数组
     */
    public void invalidateByTags(String... tags) {
        if (tags == null) {
            return;
        }
        for (String tag : tags) {
            invalidateByTag(tag);
        }
    }

    /**
     * Get all known tags
     * 获取所有已知标签
     *
     * @return unmodifiable set of all tags | 不可变的所有标签集合
     */
    public Set<String> getAllTags() {
        return Collections.unmodifiableSet(tagToKeys.keySet());
    }

    /**
     * Get the count of entries associated with a tag
     * 获取标签下的条目数
     *
     * @param tag the tag | 标签
     * @return count of entries | 条目数
     */
    public int getTagSize(String tag) {
        Objects.requireNonNull(tag, "tag cannot be null");
        ConcurrentHashMap.KeySetView<K, Boolean> keys = tagToKeys.get(tag);
        return keys == null ? 0 : keys.size();
    }

    // ==================== Cache Interface Delegation | 缓存接口委托 ====================

    @Override
    public V get(K key) {
        return delegate.get(key);
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> loader) {
        return delegate.get(key, loader);
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys) {
        return delegate.getAll(keys);
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys,
                            Function<? super Set<? extends K>, ? extends Map<K, V>> loader) {
        return delegate.getAll(keys, loader);
    }

    @Override
    public void put(K key, V value) {
        delegate.put(key, value);
        // Existing tags for this key are preserved
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        delegate.putAll(map);
        // Existing tags for these keys are preserved
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        return delegate.putIfAbsent(key, value);
    }

    @Override
    public void putWithTtl(K key, V value, Duration ttl) {
        delegate.putWithTtl(key, value, ttl);
        // Existing tags for this key are preserved
    }

    @Override
    public void putAllWithTtl(Map<? extends K, ? extends V> map, Duration ttl) {
        delegate.putAllWithTtl(map, ttl);
    }

    @Override
    public boolean putIfAbsentWithTtl(K key, V value, Duration ttl) {
        return delegate.putIfAbsentWithTtl(key, value, ttl);
    }

    @Override
    public void invalidate(K key) {
        delegate.invalidate(key);
        cleanupTagIndex(key);
    }

    @Override
    public void invalidateAll(Iterable<? extends K> keys) {
        for (K key : keys) {
            invalidate(key);
        }
    }

    @Override
    public void invalidateAll() {
        delegate.invalidateAll();
        tagToKeys.clear();
        keyToTags.clear();
    }

    @Override
    public boolean containsKey(K key) {
        return delegate.containsKey(key);
    }

    @Override
    public long size() {
        return delegate.size();
    }

    @Override
    public long estimatedSize() {
        return delegate.estimatedSize();
    }

    @Override
    public Set<K> keys() {
        return delegate.keys();
    }

    @Override
    public Collection<V> values() {
        return delegate.values();
    }

    @Override
    public Set<Map.Entry<K, V>> entries() {
        return delegate.entries();
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        return delegate.asMap();
    }

    @Override
    public CacheStats stats() {
        return delegate.stats();
    }

    @Override
    public CacheMetrics metrics() {
        return delegate.metrics();
    }

    @Override
    public void cleanUp() {
        delegate.cleanUp();
    }

    @Override
    public AsyncCache<K, V> async() {
        return delegate.async();
    }

    @Override
    public String name() {
        return delegate.name();
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return delegate.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return delegate.compute(key, remappingFunction);
    }

    @Override
    public V replace(K key, V value) {
        return delegate.replace(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return delegate.replace(key, oldValue, newValue);
    }

    @Override
    public V getAndRemove(K key) {
        V value = delegate.getAndRemove(key);
        cleanupTagIndex(key);
        return value;
    }

    // ==================== Internal Methods | 内部方法 ====================

    /**
     * Add tags to a key in both indexes
     * 在双向索引中添加标签
     */
    private void addTagsInternal(K key, String[] tags) {
        ConcurrentHashMap.KeySetView<String, Boolean> tagSet =
                keyToTags.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());
        for (String tag : tags) {
            Objects.requireNonNull(tag, "tag cannot be null");
            tagSet.add(tag);
            ConcurrentHashMap.KeySetView<K, Boolean> keySet =
                    tagToKeys.computeIfAbsent(tag, t -> ConcurrentHashMap.newKeySet());
            keySet.add(key);
        }
    }

    /**
     * Remove a key from all tag indexes
     * 从所有标签索引中移除一个键
     */
    private void cleanupTagIndex(K key) {
        ConcurrentHashMap.KeySetView<String, Boolean> tags = keyToTags.remove(key);
        if (tags == null) {
            return;
        }
        for (String tag : tags) {
            ConcurrentHashMap.KeySetView<K, Boolean> keys = tagToKeys.get(tag);
            if (keys != null) {
                keys.remove(key);
                // Atomically remove the tag entry only if the key set is still empty,
                // preventing ABA race where another thread adds a new key between
                // isEmpty check and remove.
                tagToKeys.computeIfPresent(tag, (t, ks) -> ks.isEmpty() ? null : ks);
            }
        }
    }
}
