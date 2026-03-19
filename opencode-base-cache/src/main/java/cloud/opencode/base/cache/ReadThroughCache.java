package cloud.opencode.base.cache;

import cloud.opencode.base.cache.config.CacheConfig;
import cloud.opencode.base.cache.internal.DefaultCache;
import cloud.opencode.base.cache.spi.CacheLoader;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Read-Through Cache - Automatic transparent loading from backend
 * 读穿透缓存 - 从后端自动透明加载
 *
 * <p>Automatically loads data from backend storage when cache miss occurs.
 * The loader is configured once and used for all get operations.</p>
 * <p>当缓存未命中时自动从后端存储加载数据。加载器配置一次后用于所有获取操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Transparent loading on miss - 未命中时透明加载</li>
 *   <li>Configurable loader - 可配置的加载器</li>
 *   <li>Batch loading support - 批量加载支持</li>
 *   <li>Fallback value support - 降级值支持</li>
 * </ul>
 *
 * <p><strong>Difference from LoadingCache | 与 LoadingCache 的区别:</strong></p>
 * <ul>
 *   <li>ReadThroughCache: Decorator pattern, wraps existing cache</li>
 *   <li>LoadingCache: Standalone implementation with loader</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create read-through cache wrapping existing cache
 * ReadThroughCache<String, User> cache = ReadThroughCache.wrap(baseCache)
 *     .loader(key -> userRepository.findById(key))
 *     .fallback(key -> User.ANONYMOUS)
 *     .build();
 *
 * // Get - automatically loads from DB if not in cache
 * User user = cache.get("user:1001");  // Transparent loading
 *
 * // Batch get with automatic loading
 * Map<String, User> users = cache.getAll(List.of("user:1", "user:2"));
 * }</pre>
 *
 * @param <K> the type of keys | 键类型
 * @param <V> the type of values | 值类型
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (delegates to thread-safe cache) - 线程安全: 是（委托给线程安全的缓存）</li>
 *   <li>Null-safe: Partial (null values not allowed) - 空值安全: 部分（不允许 null 值）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see LoadingCache
 * @see WriteThroughCache
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.4
 */
public class ReadThroughCache<K, V> implements Cache<K, V> {

    private static final System.Logger LOGGER = System.getLogger(ReadThroughCache.class.getName());

    private final Cache<K, V> delegate;
    private final Function<? super K, ? extends V> loader;
    private final Function<? super Set<? extends K>, ? extends Map<K, V>> batchLoader;
    private final Function<? super K, ? extends V> fallback;
    private final boolean cacheNullValues;

    private ReadThroughCache(Builder<K, V> builder) {
        this.delegate = builder.delegate;
        this.loader = builder.loader;
        this.batchLoader = builder.batchLoader;
        this.fallback = builder.fallback;
        this.cacheNullValues = builder.cacheNullValues;
    }

    /**
     * Wrap an existing cache with read-through behavior
     * 用读穿透行为包装现有缓存
     *
     * @param cache the cache to wrap | 要包装的缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> wrap(Cache<K, V> cache) {
        return new Builder<>(cache);
    }

    /**
     * Create a new read-through cache with the given configuration
     * 使用给定配置创建新的读穿透缓存
     *
     * @param name   cache name | 缓存名称
     * @param loader the loader function | 加载函数
     * @param config cache configuration | 缓存配置
     * @param <K>    key type | 键类型
     * @param <V>    value type | 值类型
     * @return read-through cache | 读穿透缓存
     */
    public static <K, V> ReadThroughCache<K, V> create(String name,
                                                        Function<? super K, ? extends V> loader,
                                                        CacheConfig<K, V> config) {
        Cache<K, V> baseCache = new DefaultCache<>(name, config);
        return wrap(baseCache).loader(loader).build();
    }

    @Override
    public V get(K key) {
        V value = delegate.get(key);
        if (value != null) {
            return value;
        }

        // Load from backend
        value = loadValue(key);

        if (value != null) {
            delegate.put(key, value);
        } else if (cacheNullValues) {
            // Cache null as a marker (using fallback if available)
            if (fallback != null) {
                value = fallback.apply(key);
                if (value != null) {
                    delegate.put(key, value);
                }
            }
        } else if (fallback != null) {
            value = fallback.apply(key);
        }

        return value;
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> customLoader) {
        return delegate.get(key, customLoader);
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys) {
        // First, get what's in cache
        Map<K, V> result = new java.util.LinkedHashMap<>();
        Set<K> missingKeys = new java.util.LinkedHashSet<>();

        for (K key : keys) {
            V value = delegate.get(key);
            if (value != null) {
                result.put(key, value);
            } else {
                missingKeys.add(key);
            }
        }

        // Load missing keys
        if (!missingKeys.isEmpty()) {
            Map<K, V> loaded = loadAllValues(missingKeys);
            if (loaded != null && !loaded.isEmpty()) {
                // Cache loaded values
                delegate.putAll(loaded);
                result.putAll(loaded);
            }

            // Apply fallback for still missing keys
            if (fallback != null) {
                for (K key : missingKeys) {
                    if (!result.containsKey(key)) {
                        V fallbackValue = fallback.apply(key);
                        if (fallbackValue != null) {
                            result.put(key, fallbackValue);
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys,
                           Function<? super Set<? extends K>, ? extends Map<K, V>> customLoader) {
        return delegate.getAll(keys, customLoader);
    }

    private V loadValue(K key) {
        if (loader == null) {
            return null;
        }
        try {
            return loader.apply(key);
        } catch (Exception e) {
            throw new CacheLoadException("Failed to load value for key: " + key, e);
        }
    }

    private Map<K, V> loadAllValues(Set<K> keys) {
        if (batchLoader != null) {
            try {
                return batchLoader.apply(keys);
            } catch (Exception e) {
                throw new CacheLoadException("Failed to batch load values", e);
            }
        } else if (loader != null) {
            // Fall back to single loading
            Map<K, V> result = new java.util.LinkedHashMap<>();
            for (K key : keys) {
                try {
                    V value = loader.apply(key);
                    if (value != null) {
                        result.put(key, value);
                    }
                } catch (Exception e) {
                    // Log and continue loading other keys
                    LOGGER.log(System.Logger.Level.WARNING, "Failed to load value for key: " + key, e);
                }
            }
            return result;
        }
        return java.util.Collections.emptyMap();
    }

    /**
     * Get the configured loader
     * 获取配置的加载器
     *
     * @return the loader | 加载器
     */
    public Function<? super K, ? extends V> loader() {
        return loader;
    }

    // ==================== Delegate Methods ====================

    @Override
    public void put(K key, V value) {
        delegate.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        delegate.putAll(map);
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        return delegate.putIfAbsent(key, value);
    }

    @Override
    public void putWithTtl(K key, V value, Duration ttl) {
        delegate.putWithTtl(key, value, ttl);
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
    public java.util.Collection<V> values() {
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

    // ==================== Builder ====================

    /**
     * Builder for ReadThroughCache
     * ReadThroughCache 构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    public static class Builder<K, V> {
        private final Cache<K, V> delegate;
        private Function<? super K, ? extends V> loader;
        private Function<? super Set<? extends K>, ? extends Map<K, V>> batchLoader;
        private Function<? super K, ? extends V> fallback;
        private boolean cacheNullValues = false;

        Builder(Cache<K, V> delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
        }

        /**
         * Set the loader function for single key loading
         * 设置单键加载函数
         *
         * @param loader the loader function | 加载函数
         * @return this builder | 此构建器
         */
        public Builder<K, V> loader(Function<? super K, ? extends V> loader) {
            this.loader = Objects.requireNonNull(loader, "loader cannot be null");
            return this;
        }

        /**
         * Set the loader using CacheLoader interface
         * 使用 CacheLoader 接口设置加载器
         *
         * @param loader the cache loader | 缓存加载器
         * @return this builder | 此构建器
         */
        public Builder<K, V> loader(CacheLoader<K, V> loader) {
            Objects.requireNonNull(loader, "loader cannot be null");
            this.loader = key -> {
                try {
                    return loader.load(key);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
            this.batchLoader = keys -> {
                try {
                    return loader.loadAll(keys);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
            return this;
        }

        /**
         * Set the batch loader function
         * 设置批量加载函数
         *
         * @param batchLoader the batch loader function | 批量加载函数
         * @return this builder | 此构建器
         */
        public Builder<K, V> batchLoader(Function<? super Set<? extends K>, ? extends Map<K, V>> batchLoader) {
            this.batchLoader = batchLoader;
            return this;
        }

        /**
         * Set the fallback function for when loading returns null
         * 设置加载返回 null 时的降级函数
         *
         * @param fallback the fallback function | 降级函数
         * @return this builder | 此构建器
         */
        public Builder<K, V> fallback(Function<? super K, ? extends V> fallback) {
            this.fallback = fallback;
            return this;
        }

        /**
         * Set a static fallback value
         * 设置静态降级值
         *
         * @param fallbackValue the fallback value | 降级值
         * @return this builder | 此构建器
         */
        public Builder<K, V> fallback(V fallbackValue) {
            this.fallback = key -> fallbackValue;
            return this;
        }

        /**
         * Enable caching of null values (using fallback as marker)
         * 启用缓存 null 值（使用降级值作为标记）
         *
         * @return this builder | 此构建器
         */
        public Builder<K, V> cacheNullValues() {
            this.cacheNullValues = true;
            return this;
        }

        /**
         * Build the read-through cache
         * 构建读穿透缓存
         *
         * @return read-through cache | 读穿透缓存
         */
        public ReadThroughCache<K, V> build() {
            if (loader == null) {
                throw new IllegalStateException("loader must be set");
            }
            return new ReadThroughCache<>(this);
        }
    }

    // ==================== Exception ====================

    /**
     * Exception thrown when cache loading fails
     * 缓存加载失败时抛出的异常
     */
    public static class CacheLoadException extends RuntimeException {
        public CacheLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
