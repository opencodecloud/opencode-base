package cloud.opencode.base.feature.store;

import cloud.opencode.base.feature.Feature;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Cached Feature Store with Optional Cache Module Delegation
 * 支持可选缓存模块委托的缓存功能存储
 *
 * <p>Wraps another FeatureStore with caching for improved performance.
 * If the Cache module (opencode-base-cache) is available, it delegates to OpenCache
 * for high-performance caching. Otherwise, falls back to LruFeatureStore as cache.</p>
 * <p>用缓存包装另一个 FeatureStore 以提高性能。
 * 如果缓存模块可用，则委托给 OpenCache 进行高性能缓存。
 * 否则降级到使用 LruFeatureStore 作为缓存。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Wrap a remote store with caching
 * FeatureStore remoteStore = new RedisFeatureStore(redisClient);
 * FeatureStore cachedStore = CachedFeatureStore.wrap(remoteStore);
 *
 * // With custom TTL
 * FeatureStore cachedStore = CachedFeatureStore.wrap(remoteStore, Duration.ofMinutes(5));
 *
 * // Check if cache module is available
 * boolean hasCache = CachedFeatureStore.isCacheModuleAvailable();
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>TTL-based caching layer over any FeatureStore - 基于TTL的任意FeatureStore缓存层</li>
 *   <li>Automatic cache expiration and refresh - 自动缓存过期和刷新</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public final class CachedFeatureStore implements FeatureStore {

    /**
     * MethodHandle for OpenCache.getOrCreate(String, Consumer) - null if Cache module not available
     */
    private static final MethodHandle GET_OR_CREATE_HANDLE;

    /**
     * Cache interface class - null if Cache module not available
     */
    private static final Class<?> CACHE_CLASS;

    /**
     * MethodHandle for Cache.get(Object) - null if Cache module not available
     */
    private static final MethodHandle CACHE_GET_HANDLE;

    /**
     * MethodHandle for Cache.put(Object, Object) - null if Cache module not available
     */
    private static final MethodHandle CACHE_PUT_HANDLE;

    /**
     * MethodHandle for Cache.invalidate(Object) - null if Cache module not available
     */
    private static final MethodHandle CACHE_INVALIDATE_HANDLE;

    /**
     * MethodHandle for Cache.invalidateAll() - null if Cache module not available
     */
    private static final MethodHandle CACHE_INVALIDATE_ALL_HANDLE;

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);
    private static final int DEFAULT_MAX_SIZE = 1000;

    static {
        GET_OR_CREATE_HANDLE = initGetOrCreateHandle();
        CACHE_CLASS = loadCacheClass();
        CACHE_GET_HANDLE = initCacheMethodHandle("get", Object.class, Object.class);
        CACHE_PUT_HANDLE = initCacheVoidMethodHandle("put", Object.class, Object.class);
        CACHE_INVALIDATE_HANDLE = initCacheVoidMethodHandle("invalidate", Object.class);
        CACHE_INVALIDATE_ALL_HANDLE = initCacheVoidNoArgMethodHandle("invalidateAll");
    }

    private final FeatureStore delegate;
    private final Object cache; // Either OpenCache Cache instance or LruFeatureStore
    private final boolean usingOpenCache;

    /**
     * Creates a cached feature store.
     */
    private CachedFeatureStore(FeatureStore delegate, Duration ttl, int maxSize) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");

        if (GET_OR_CREATE_HANDLE != null && CACHE_CLASS != null) {
            this.cache = createOpenCache("feature-cache", ttl, maxSize);
            this.usingOpenCache = this.cache != null;
        } else {
            this.cache = new LruFeatureStore(maxSize);
            this.usingOpenCache = false;
        }
    }

    private static MethodHandle initGetOrCreateHandle() {
        try {
            Class<?> openCacheClass = Class.forName("cloud.opencode.base.cache.OpenCache");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            return lookup.findStatic(openCacheClass, "getOrCreate",
                    MethodType.methodType(loadCacheClass(), String.class, Consumer.class));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    private static Class<?> loadCacheClass() {
        try {
            return Class.forName("cloud.opencode.base.cache.Cache");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static MethodHandle initCacheMethodHandle(String name, Class<?> returnType, Class<?>... paramTypes) {
        try {
            if (CACHE_CLASS == null) return null;
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            return lookup.findVirtual(CACHE_CLASS, name, MethodType.methodType(returnType, paramTypes));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    private static MethodHandle initCacheVoidMethodHandle(String name, Class<?>... paramTypes) {
        try {
            if (CACHE_CLASS == null) return null;
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            return lookup.findVirtual(CACHE_CLASS, name, MethodType.methodType(void.class, paramTypes));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    private static MethodHandle initCacheVoidNoArgMethodHandle(String name) {
        try {
            if (CACHE_CLASS == null) return null;
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            return lookup.findVirtual(CACHE_CLASS, name, MethodType.methodType(void.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Object createOpenCache(String name, Duration ttl, int maxSize) {
        try {
            // Create configurer that sets TTL and max size
            Consumer<Object> configurer = builder -> {
                try {
                    Class<?> builderClass = builder.getClass();
                    // Set maximum size
                    var maxSizeMethod = builderClass.getMethod("maximumSize", int.class);
                    maxSizeMethod.invoke(builder, maxSize);
                    // Set TTL
                    var ttlMethod = builderClass.getMethod("expireAfterWrite", Duration.class);
                    ttlMethod.invoke(builder, ttl);
                } catch (Exception e) {
                    // Ignore configuration errors
                }
            };

            return GET_OR_CREATE_HANDLE.invoke(name, configurer);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * Checks if the Cache module is available.
     * 检查缓存模块是否可用
     *
     * @return true if Cache module is available | 如果缓存模块可用返回 true
     */
    public static boolean isCacheModuleAvailable() {
        return GET_OR_CREATE_HANDLE != null && CACHE_CLASS != null;
    }

    /**
     * Wraps a feature store with caching.
     * 用缓存包装功能存储
     *
     * @param delegate the underlying store | 底层存储
     * @return cached feature store | 缓存的功能存储
     */
    public static CachedFeatureStore wrap(FeatureStore delegate) {
        return new CachedFeatureStore(delegate, DEFAULT_TTL, DEFAULT_MAX_SIZE);
    }

    /**
     * Wraps a feature store with caching and custom TTL.
     * 用缓存和自定义 TTL 包装功能存储
     *
     * @param delegate the underlying store | 底层存储
     * @param ttl the cache TTL | 缓存 TTL
     * @return cached feature store | 缓存的功能存储
     */
    public static CachedFeatureStore wrap(FeatureStore delegate, Duration ttl) {
        return new CachedFeatureStore(delegate, ttl, DEFAULT_MAX_SIZE);
    }

    /**
     * Wraps a feature store with caching and custom settings.
     * 用缓存和自定义设置包装功能存储
     *
     * @param delegate the underlying store | 底层存储
     * @param ttl the cache TTL | 缓存 TTL
     * @param maxSize the maximum cache size | 最大缓存大小
     * @return cached feature store | 缓存的功能存储
     */
    public static CachedFeatureStore wrap(FeatureStore delegate, Duration ttl, int maxSize) {
        return new CachedFeatureStore(delegate, ttl, maxSize);
    }

    @Override
    public void save(Feature feature) {
        delegate.save(feature);
        cacheFeature(feature);
    }

    @Override
    public Optional<Feature> find(String key) {
        // Try cache first
        Feature cached = getCached(key);
        if (cached != null) {
            return Optional.of(cached);
        }

        // Load from delegate
        Optional<Feature> result = delegate.find(key);
        result.ifPresent(this::cacheFeature);
        return result;
    }

    @Override
    public List<Feature> findAll() {
        return delegate.findAll();
    }

    @Override
    public boolean delete(String key) {
        invalidateCache(key);
        return delegate.delete(key);
    }

    @Override
    public boolean exists(String key) {
        // Try cache first
        Feature cached = getCached(key);
        if (cached != null) {
            return true;
        }
        return delegate.exists(key);
    }

    @Override
    public int count() {
        return delegate.count();
    }

    @Override
    public void clear() {
        invalidateAllCache();
        delegate.clear();
    }

    /**
     * Checks if this store is using OpenCache.
     * 检查此存储是否使用 OpenCache
     *
     * @return true if using OpenCache | 如果使用 OpenCache 返回 true
     */
    public boolean isUsingOpenCache() {
        return usingOpenCache;
    }

    /**
     * Invalidates cache entry for a key.
     * 使键的缓存条目失效
     *
     * @param key the key to invalidate | 要使其失效的键
     */
    public void invalidate(String key) {
        invalidateCache(key);
    }

    /**
     * Invalidates all cache entries.
     * 使所有缓存条目失效
     */
    public void invalidateAll() {
        invalidateAllCache();
    }

    /**
     * Gets the underlying delegate store.
     * 获取底层委托存储
     *
     * @return the delegate store | 委托存储
     */
    public FeatureStore getDelegate() {
        return delegate;
    }

    // ==================== Internal Cache Operations ====================

    private void cacheFeature(Feature feature) {
        if (usingOpenCache && CACHE_PUT_HANDLE != null) {
            try {
                CACHE_PUT_HANDLE.invoke(cache, feature.key(), feature);
            } catch (Throwable e) {
                // Ignore cache errors
            }
        } else if (!usingOpenCache && cache instanceof LruFeatureStore lruCache) {
            lruCache.save(feature);
        }
    }

    private Feature getCached(String key) {
        if (usingOpenCache && CACHE_GET_HANDLE != null) {
            try {
                return (Feature) CACHE_GET_HANDLE.invoke(cache, key);
            } catch (Throwable e) {
                return null;
            }
        } else if (!usingOpenCache && cache instanceof LruFeatureStore lruCache) {
            return lruCache.find(key).orElse(null);
        }
        return null;
    }

    private void invalidateCache(String key) {
        if (usingOpenCache && CACHE_INVALIDATE_HANDLE != null) {
            try {
                CACHE_INVALIDATE_HANDLE.invoke(cache, key);
            } catch (Throwable e) {
                // Ignore cache errors
            }
        } else if (!usingOpenCache && cache instanceof LruFeatureStore lruCache) {
            lruCache.delete(key);
        }
    }

    private void invalidateAllCache() {
        if (usingOpenCache && CACHE_INVALIDATE_ALL_HANDLE != null) {
            try {
                CACHE_INVALIDATE_ALL_HANDLE.invoke(cache);
            } catch (Throwable e) {
                // Ignore cache errors
            }
        } else if (!usingOpenCache && cache instanceof LruFeatureStore lruCache) {
            lruCache.clear();
        }
    }
}
