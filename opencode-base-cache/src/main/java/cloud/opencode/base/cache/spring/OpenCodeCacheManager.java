package cloud.opencode.base.cache.spring;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.CacheManager;
import cloud.opencode.base.cache.config.CacheConfig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * OpenCode Cache Manager for Spring Cache Abstraction
 * OpenCode 缓存管理器 - 用于 Spring Cache 抽象
 *
 * <p>Implements Spring's CacheManager interface to integrate OpenCode Cache
 * with Spring's {@code @Cacheable}, {@code @CachePut}, {@code @CacheEvict} annotations.</p>
 * <p>实现 Spring 的 CacheManager 接口，将 OpenCode Cache 与 Spring 的
 * {@code @Cacheable}、{@code @CachePut}、{@code @CacheEvict} 注解集成。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Configuration class
 * @Configuration
 * @EnableCaching
 * public class CacheConfiguration {
 *
 *     @Bean
 *     public CacheManager cacheManager() {
 *         return OpenCodeCacheManager.builder()
 *             .defaultConfig(config -> config
 *                 .maximumSize(10000)
 *                 .expireAfterWrite(Duration.ofMinutes(30))
 *                 .recordStats())
 *             .cache("users", config -> config
 *                 .maximumSize(5000)
 *                 .expireAfterAccess(Duration.ofMinutes(10)))
 *             .cache("products", config -> config
 *                 .maximumSize(50000)
 *                 .expireAfterWrite(Duration.ofHours(1)))
 *             .build();
 *     }
 * }
 *
 * // Service class
 * @Service
 * public class UserService {
 *
 *     @Cacheable("users")
 *     public User findById(Long id) {
 *         return userRepository.findById(id);
 *     }
 *
 *     @CachePut(value = "users", key = "#user.id")
 *     public User save(User user) {
 *         return userRepository.save(user);
 *     }
 *
 *     @CacheEvict("users")
 *     public void delete(Long id) {
 *         userRepository.deleteById(id);
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Spring Cache abstraction integration - Spring Cache 抽象集成</li>
 *   <li>Per-cache configuration - 每缓存配置</li>
 *   <li>Default configuration support - 默认配置支持</li>
 *   <li>Dynamic cache creation - 动态缓存创建</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public class OpenCodeCacheManager implements SpringCacheManager {

    private final CacheManager delegate;
    private final Map<String, Consumer<CacheConfig.Builder<Object, Object>>> cacheConfigs;
    private final Consumer<CacheConfig.Builder<Object, Object>> defaultConfig;
    private final Map<String, SpringCache> springCaches = new ConcurrentHashMap<>();
    private final boolean allowNullValues;

    private OpenCodeCacheManager(Builder builder) {
        this.delegate = CacheManager.getInstance();
        this.cacheConfigs = builder.cacheConfigs;
        this.defaultConfig = builder.defaultConfig;
        this.allowNullValues = builder.allowNullValues;

        // Pre-create configured caches
        for (String name : cacheConfigs.keySet()) {
            getCache(name);
        }
    }

    /**
     * Create builder
     * 创建构建器
     *
     * @return builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create with default settings
     * 使用默认设置创建
     *
     * @return cache manager | 缓存管理器
     */
    public static OpenCodeCacheManager create() {
        return builder().build();
    }

    @Override
    public SpringCache getCache(String name) {
        return springCaches.computeIfAbsent(name, this::createSpringCache);
    }

    @Override
    public Collection<String> getCacheNames() {
        Set<String> names = new LinkedHashSet<>(cacheConfigs.keySet());
        names.addAll(delegate.getCacheNames());
        return Collections.unmodifiableSet(names);
    }

    private SpringCache createSpringCache(String name) {
        Consumer<CacheConfig.Builder<Object, Object>> configurer = cacheConfigs.getOrDefault(name, defaultConfig);

        Cache<Object, Object> cache = delegate.getOrCreateCache(name, builder -> {
            if (configurer != null) {
                configurer.accept(builder);
            }
        });

        return new OpenCodeSpringCache(name, cache, allowNullValues);
    }

    /**
     * Get underlying OpenCode CacheManager
     * 获取底层 OpenCode CacheManager
     *
     * @return cache manager | 缓存管理器
     */
    public CacheManager getDelegate() {
        return delegate;
    }

    /**
     * Get statistics for all managed caches
     * 获取所有管理缓存的统计信息
     *
     * @return map of cache name to stats | 缓存名称到统计的映射
     * @since V2.0.1
     */
    public java.util.Map<String, cloud.opencode.base.cache.CacheStats> getAllStats() {
        return delegate.getAllStats();
    }

    /**
     * Get statistics for a specific cache
     * 获取特定缓存的统计信息
     *
     * @param name cache name | 缓存名称
     * @return cache stats or empty stats if not found | 缓存统计，不存在返回空统计
     * @since V2.0.1
     */
    public cloud.opencode.base.cache.CacheStats getCacheStats(String name) {
        return delegate.getCacheStats(name);
    }

    /**
     * Get combined statistics across all caches
     * 获取所有缓存的合并统计信息
     *
     * @return combined stats | 合并的统计
     * @since V2.0.1
     */
    public cloud.opencode.base.cache.CacheStats getCombinedStats() {
        return delegate.getCombinedStats();
    }

    /**
     * Get metrics for a specific cache
     * 获取特定缓存的指标
     *
     * @param name cache name | 缓存名称
     * @return cache metrics or null if cache not found | 缓存指标，缓存不存在返回 null
     * @since V2.0.1
     */
    public cloud.opencode.base.cache.CacheMetrics getCacheMetrics(String name) {
        return delegate.<Object, Object>getCache(name)
                .map(Cache::metrics)
                .orElse(null);
    }

    /**
     * Invalidate all entries in all managed caches
     * 使所有管理缓存中的所有条目失效
     *
     * @since V2.0.1
     */
    public void invalidateAll() {
        delegate.invalidateAll();
        springCaches.clear();
    }

    /**
     * Clean up all managed caches
     * 清理所有管理缓存
     *
     * @since V2.0.1
     */
    public void cleanUpAll() {
        delegate.cleanUpAll();
    }

    // ==================== Builder ====================

    /**
     * Builder for OpenCodeCacheManager
     */
    public static class Builder {

        /** Creates a new Builder instance | 创建新的 Builder 实例 */
        public Builder() {}
        private Consumer<CacheConfig.Builder<Object, Object>> defaultConfig;
        private final Map<String, Consumer<CacheConfig.Builder<Object, Object>>> cacheConfigs = new LinkedHashMap<>();
        private boolean allowNullValues = true;

        /**
         * Set default cache configuration
         * 设置默认缓存配置
         *
         * @param configurer configuration consumer | 配置消费者
         * @return this builder | 此构建器
         */
        public Builder defaultConfig(Consumer<CacheConfig.Builder<Object, Object>> configurer) {
            this.defaultConfig = configurer;
            return this;
        }

        /**
         * Configure a named cache
         * 配置命名缓存
         *
         * @param name       cache name | 缓存名称
         * @param configurer configuration consumer | 配置消费者
         * @return this builder | 此构建器
         */
        public Builder cache(String name, Consumer<CacheConfig.Builder<Object, Object>> configurer) {
            cacheConfigs.put(name, configurer);
            return this;
        }

        /**
         * Set whether to allow null values
         * 设置是否允许 null 值
         *
         * @param allowNullValues true to allow null | 允许 null 为 true
         * @return this builder | 此构建器
         */
        public Builder allowNullValues(boolean allowNullValues) {
            this.allowNullValues = allowNullValues;
            return this;
        }

        /**
         * Build the cache manager
         * 构建缓存管理器
         *
         * @return cache manager | 缓存管理器
         */
        public OpenCodeCacheManager build() {
            return new OpenCodeCacheManager(this);
        }
    }
}

/**
 * Spring CacheManager interface abstraction
 * Spring CacheManager 接口抽象
 *
 * <p>This interface mirrors org.springframework.cache.CacheManager to avoid
 * compile-time dependency on Spring.</p>
 */
interface SpringCacheManager {
    SpringCache getCache(String name);
    Collection<String> getCacheNames();
}

/**
 * Spring Cache interface abstraction
 * Spring Cache 接口抽象
 */
interface SpringCache {
    String getName();
    Object getNativeCache();
    ValueWrapper get(Object key);
    <T> T get(Object key, Class<T> type);
    <T> T get(Object key, java.util.concurrent.Callable<T> valueLoader);
    void put(Object key, Object value);
    default ValueWrapper putIfAbsent(Object key, Object value) {
        ValueWrapper existing = get(key);
        if (existing == null) {
            put(key, value);
        }
        return existing;
    }
    void evict(Object key);
    default boolean evictIfPresent(Object key) {
        evict(key);
        return false;
    }
    void clear();
    default boolean invalidate() {
        clear();
        return false;
    }

    interface ValueWrapper {
        Object get();
    }
}

/**
 * OpenCode implementation of Spring Cache
 * OpenCode 的 Spring Cache 实现
 */
class OpenCodeSpringCache implements SpringCache {

    private static final Object NULL_VALUE = new Object();

    private final String name;
    private final Cache<Object, Object> cache;
    private final boolean allowNullValues;

    OpenCodeSpringCache(String name, Cache<Object, Object> cache, boolean allowNullValues) {
        this.name = name;
        this.cache = cache;
        this.allowNullValues = allowNullValues;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return cache;
    }

    @Override
    public ValueWrapper get(Object key) {
        Object value = cache.get(key);
        if (value == null) {
            return null;
        }
        return new SimpleValueWrapper(fromStoreValue(value));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Class<T> type) {
        Object value = cache.get(key);
        if (value == null) {
            return null;
        }
        value = fromStoreValue(value);
        if (value != null && type != null && !type.isInstance(value)) {
            throw new IllegalStateException("Cached value is not of required type [" + type.getName() + "]: " + value);
        }
        return (T) value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, java.util.concurrent.Callable<T> valueLoader) {
        Object value = cache.get(key, k -> {
            try {
                T loaded = valueLoader.call();
                return toStoreValue(loaded);
            } catch (Exception e) {
                throw new ValueRetrievalException(key, valueLoader, e);
            }
        });
        return (T) fromStoreValue(value);
    }

    @Override
    public void put(Object key, Object value) {
        cache.put(key, toStoreValue(value));
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        Object existing = cache.get(key);
        if (existing == null) {
            cache.putIfAbsent(key, toStoreValue(value));
            return null;
        }
        return new SimpleValueWrapper(fromStoreValue(existing));
    }

    @Override
    public void evict(Object key) {
        cache.invalidate(key);
    }

    @Override
    public boolean evictIfPresent(Object key) {
        boolean existed = cache.containsKey(key);
        cache.invalidate(key);
        return existed;
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }

    @Override
    public boolean invalidate() {
        boolean hadEntries = cache.estimatedSize() > 0;
        cache.invalidateAll();
        return hadEntries;
    }

    private Object toStoreValue(Object value) {
        if (allowNullValues && value == null) {
            return NULL_VALUE;
        }
        if (value == null) {
            throw new IllegalArgumentException("Cache '" + name + "' does not allow null values");
        }
        return value;
    }

    private Object fromStoreValue(Object value) {
        if (value == NULL_VALUE) {
            return null;
        }
        return value;
    }

    private record SimpleValueWrapper(Object value) implements ValueWrapper {
        @Override
        public Object get() {
            return value;
        }
    }

    static class ValueRetrievalException extends RuntimeException {
        private final Object key;

        ValueRetrievalException(Object key, java.util.concurrent.Callable<?> loader, Throwable cause) {
            super("Value retrieval failed for key: " + key, cause);
            this.key = key;
        }

        public Object getKey() {
            return key;
        }
    }
}
