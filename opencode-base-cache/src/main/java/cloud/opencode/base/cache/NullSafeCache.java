package cloud.opencode.base.cache;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Null-Safe Cache - Cache that can store and distinguish null values
 * 空值安全缓存 - 可以存储和区分 null 值的缓存
 *
 * <p>Wraps null values in a sentinel object to distinguish between
 * "key not present" and "key present with null value".</p>
 * <p>将 null 值包装在哨兵对象中，以区分"键不存在"和"键存在但值为 null"。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Store null values - 存储 null 值</li>
 *   <li>Distinguish miss from null - 区分未命中和 null</li>
 *   <li>Optional-based API - 基于 Optional 的 API</li>
 *   <li>Transparent wrapping/unwrapping - 透明包装/解包</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create null-safe cache
 * NullSafeCache<String, User> cache = NullSafeCache.wrap(baseCache);
 *
 * // Store null value
 * cache.put("user:deleted", null);  // Stores null as sentinel
 *
 * // Distinguish between miss and null
 * Optional<User> result = cache.getIfPresent("user:deleted");
 * if (result == null) {
 *     // Key not in cache
 * } else if (result.isEmpty()) {
 *     // Key present with null value
 * } else {
 *     // Key present with actual value
 *     User user = result.get();
 * }
 *
 * // Or use containsKey first
 * if (cache.containsKey("user:deleted")) {
 *     User user = cache.get("user:deleted");  // May return null
 * }
 * }</pre>
 *
 * @param <K> the type of keys | 键类型
 * @param <V> the type of values | 值类型
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (delegates to thread-safe cache) - 线程安全: 是（委托给线程安全的缓存）</li>
 *   <li>Null-safe: Yes (primary purpose) - 空值安全: 是（主要功能）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.4
 */
public class NullSafeCache<K, V> implements Cache<K, V> {

    /**
     * Sentinel object representing a cached null value
     * 表示缓存的 null 值的哨兵对象
     */
    private static final Object NULL_SENTINEL = new Object() {
        @Override
        public String toString() {
            return "NullSafeCache.NULL_SENTINEL";
        }
    };

    private final Cache<K, Object> delegate;

    @SuppressWarnings("unchecked")
    private NullSafeCache(Cache<K, V> delegate) {
        this.delegate = (Cache<K, Object>) delegate;
    }

    /**
     * Wrap an existing cache with null-safe behavior
     * 用空值安全行为包装现有缓存
     *
     * @param cache the cache to wrap | 要包装的缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return null-safe cache | 空值安全缓存
     */
    public static <K, V> NullSafeCache<K, V> wrap(Cache<K, V> cache) {
        Objects.requireNonNull(cache, "cache cannot be null");
        if (cache instanceof NullSafeCache) {
            @SuppressWarnings("unchecked")
            NullSafeCache<K, V> nullSafe = (NullSafeCache<K, V>) cache;
            return nullSafe;
        }
        return new NullSafeCache<>(cache);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(K key) {
        Object value = delegate.get(key);
        return unwrap(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(K key, Function<? super K, ? extends V> loader) {
        Object value = delegate.get(key, k -> wrap(loader.apply(k)));
        return unwrap(value);
    }

    /**
     * Get value as Optional, distinguishing null from absent
     * 以 Optional 形式获取值，区分 null 和不存在
     *
     * <p>Returns:</p>
     * <ul>
     *   <li>null - key not present in cache</li>
     *   <li>Optional.empty() - key present with null value</li>
     *   <li>Optional.of(value) - key present with non-null value</li>
     * </ul>
     *
     * @param key the key | 键
     * @return Optional containing value, empty Optional for cached null, or null if not present
     */
    public Optional<V> getIfPresent(K key) {
        // Single atomic read to avoid TOCTOU race between containsKey and get
        Object value = delegate.get(key);
        if (value == null) {
            // Key not in cache (or mapped to a real null, which shouldn't happen
            // since we wrap nulls as NULL_SENTINEL)
            return null;  // Not in cache
        }
        if (value == NULL_SENTINEL) {
            return Optional.empty();  // Cached null
        }
        @SuppressWarnings("unchecked")
        V typedValue = (V) value;
        return Optional.ofNullable(typedValue);
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys) {
        Map<K, Object> raw = delegate.getAll(keys);
        Map<K, V> result = new java.util.LinkedHashMap<>();
        for (Map.Entry<K, Object> entry : raw.entrySet()) {
            result.put(entry.getKey(), unwrap(entry.getValue()));
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<K, V> getAll(Iterable<? extends K> keys,
                           Function<? super Set<? extends K>, ? extends Map<K, V>> loader) {
        Map<K, Object> raw = delegate.getAll(keys, missingKeys -> {
            Map<K, V> loaded = loader.apply(missingKeys);
            Map<K, Object> wrapped = new java.util.LinkedHashMap<>();
            for (Map.Entry<K, V> entry : loaded.entrySet()) {
                wrapped.put(entry.getKey(), wrap(entry.getValue()));
            }
            return wrapped;
        });
        Map<K, V> result = new java.util.LinkedHashMap<>();
        for (Map.Entry<K, Object> entry : raw.entrySet()) {
            result.put(entry.getKey(), unwrap(entry.getValue()));
        }
        return result;
    }

    @Override
    public void put(K key, V value) {
        delegate.put(key, wrap(value));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        Map<K, Object> wrapped = new java.util.LinkedHashMap<>();
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            wrapped.put(entry.getKey(), wrap(entry.getValue()));
        }
        delegate.putAll(wrapped);
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        return delegate.putIfAbsent(key, wrap(value));
    }

    @Override
    public void putWithTtl(K key, V value, Duration ttl) {
        delegate.putWithTtl(key, wrap(value), ttl);
    }

    @Override
    public void putAllWithTtl(Map<? extends K, ? extends V> map, Duration ttl) {
        Map<K, Object> wrapped = new java.util.LinkedHashMap<>();
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            wrapped.put(entry.getKey(), wrap(entry.getValue()));
        }
        delegate.putAllWithTtl(wrapped, ttl);
    }

    @Override
    public boolean putIfAbsentWithTtl(K key, V value, Duration ttl) {
        return delegate.putIfAbsentWithTtl(key, wrap(value), ttl);
    }

    @Override
    public void invalidate(K key) {
        delegate.invalidate(key);
    }

    @Override
    public void invalidateAll(Iterable<? extends K> keys) {
        delegate.invalidateAll(keys);
    }

    @Override
    public void invalidateAll() {
        delegate.invalidateAll();
    }

    @Override
    public boolean containsKey(K key) {
        return delegate.containsKey(key);
    }

    /**
     * Check if the key is present with a null value
     * 检查键是否存在且值为 null
     *
     * @param key the key | 键
     * @return true if key is present with null value | 键存在且值为 null 返回 true
     */
    public boolean containsNullValue(K key) {
        if (!delegate.containsKey(key)) {
            return false;
        }
        Object value = delegate.get(key);
        return value == NULL_SENTINEL;
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
    @SuppressWarnings("unchecked")
    public Set<K> keys() {
        return delegate.keys();
    }

    @Override
    @SuppressWarnings("unchecked")
    public java.util.Collection<V> values() {
        java.util.List<V> result = new java.util.ArrayList<>();
        for (Object value : delegate.values()) {
            result.add(unwrap(value));
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Map.Entry<K, V>> entries() {
        Set<Map.Entry<K, V>> result = new java.util.LinkedHashSet<>();
        for (Map.Entry<K, Object> entry : delegate.entries()) {
            result.add(new java.util.AbstractMap.SimpleImmutableEntry<>(
                    entry.getKey(), unwrap(entry.getValue())));
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConcurrentMap<K, V> asMap() {
        // Return a view that unwraps values
        return new NullSafeMapView<>(delegate.asMap());
    }

    @Override
    @SuppressWarnings("unchecked")
    public CacheStats stats() {
        return delegate.stats();
    }

    @Override
    @SuppressWarnings("unchecked")
    public CacheMetrics metrics() {
        return delegate.metrics();
    }

    @Override
    public void cleanUp() {
        delegate.cleanUp();
    }

    @Override
    @SuppressWarnings("unchecked")
    public AsyncCache<K, V> async() {
        return (AsyncCache<K, V>) delegate.async();
    }

    @Override
    public String name() {
        return delegate.name();
    }

    // ==================== Helper Methods ====================

    private Object wrap(V value) {
        return value == null ? NULL_SENTINEL : value;
    }

    @SuppressWarnings("unchecked")
    private V unwrap(Object value) {
        if (value == NULL_SENTINEL) {
            return null;
        }
        return (V) value;
    }

    // ==================== Map View ====================

    private class NullSafeMapView<K, V> implements ConcurrentMap<K, V> {
        private final ConcurrentMap<K, Object> delegate;

        NullSafeMapView(ConcurrentMap<K, Object> delegate) {
            this.delegate = delegate;
        }

        @Override
        @SuppressWarnings("unchecked")
        public V get(Object key) {
            Object value = delegate.get(key);
            return value == NULL_SENTINEL ? null : (V) value;
        }

        @Override
        @SuppressWarnings("unchecked")
        public V put(K key, V value) {
            Object old = delegate.put(key, value == null ? NULL_SENTINEL : value);
            return old == NULL_SENTINEL ? null : (V) old;
        }

        @Override
        @SuppressWarnings("unchecked")
        public V remove(Object key) {
            Object old = delegate.remove(key);
            return old == NULL_SENTINEL ? null : (V) old;
        }

        @Override
        public boolean containsKey(Object key) {
            return delegate.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return delegate.containsValue(value == null ? NULL_SENTINEL : value);
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public Set<K> keySet() {
            return delegate.keySet();
        }

        @Override
        @SuppressWarnings("unchecked")
        public java.util.Collection<V> values() {
            java.util.List<V> result = new java.util.ArrayList<>();
            for (Object value : delegate.values()) {
                result.add(value == NULL_SENTINEL ? null : (V) value);
            }
            return result;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Set<Map.Entry<K, V>> entrySet() {
            Set<Map.Entry<K, V>> result = new java.util.LinkedHashSet<>();
            for (Map.Entry<K, Object> entry : delegate.entrySet()) {
                V value = entry.getValue() == NULL_SENTINEL ? null : (V) entry.getValue();
                result.add(new java.util.AbstractMap.SimpleImmutableEntry<>(entry.getKey(), value));
            }
            return result;
        }

        @Override
        @SuppressWarnings("unchecked")
        public V putIfAbsent(K key, V value) {
            Object old = delegate.putIfAbsent(key, value == null ? NULL_SENTINEL : value);
            return old == NULL_SENTINEL ? null : (V) old;
        }

        @Override
        public boolean remove(Object key, Object value) {
            return delegate.remove(key, value == null ? NULL_SENTINEL : value);
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            return delegate.replace(key,
                    oldValue == null ? NULL_SENTINEL : oldValue,
                    newValue == null ? NULL_SENTINEL : newValue);
        }

        @Override
        @SuppressWarnings("unchecked")
        public V replace(K key, V value) {
            Object old = delegate.replace(key, value == null ? NULL_SENTINEL : value);
            return old == NULL_SENTINEL ? null : (V) old;
        }
    }
}
