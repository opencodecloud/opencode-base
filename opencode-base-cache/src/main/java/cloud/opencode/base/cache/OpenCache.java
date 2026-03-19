package cloud.opencode.base.cache;

import cloud.opencode.base.cache.config.CacheConfig;
import cloud.opencode.base.cache.config.CacheSpec;
import cloud.opencode.base.cache.internal.DefaultCache;
import cloud.opencode.base.cache.spi.EvictionPolicy;
import cloud.opencode.base.cache.spi.ExpiryPolicy;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * OpenCache Facade - Unified entry point for cache operations
 * OpenCache 门面 - 缓存操作的统一入口
 *
 * <p>Provides convenient static methods for cache creation and management.</p>
 * <p>提供便捷的静态方法用于缓存创建和管理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Cache creation with fluent API - 流式 API 创建缓存</li>
 *   <li>Named cache management - 命名缓存管理</li>
 *   <li>Policy factory methods - 策略工厂方法</li>
 *   <li>Global statistics - 全局统计</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create cache with configuration - 使用配置创建缓存
 * Cache<String, User> cache = OpenCache.getOrCreate("users", config -> config
 *     .maximumSize(10000)
 *     .expireAfterWrite(Duration.ofMinutes(30))
 *     .evictionPolicy(OpenCache.lru())
 *     .recordStats());
 *
 * // Build cache directly - 直接构建缓存
 * Cache<String, User> cache = OpenCache.<String, User>builder()
 *     .maximumSize(10000)
 *     .expireAfterWrite(Duration.ofMinutes(30))
 *     .build("myCache");
 *
 * // Use policy factories - 使用策略工厂
 * EvictionPolicy<K, V> policy = OpenCache.wTinyLfu();
 * ExpiryPolicy<K, V> expiry = OpenCache.ttl(Duration.ofHours(1));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class OpenCache {

    private OpenCache() {
    }

    // ==================== Cache Creation | 缓存创建 ====================

    /**
     * Get or create named cache
     * 获取或创建命名缓存
     *
     * @param name cache name | 缓存名称
     * @param <K>  key type | 键类型
     * @param <V>  value type | 值类型
     * @return cache instance | 缓存实例
     */
    public static <K, V> Cache<K, V> getOrCreate(String name) {
        return CacheManager.getInstance().getOrCreateCache(name);
    }

    /**
     * Get or create named cache with configuration
     * 获取或创建带配置的命名缓存
     *
     * @param name       cache name | 缓存名称
     * @param configurer configuration consumer | 配置消费者
     * @param <K>        key type | 键类型
     * @param <V>        value type | 值类型
     * @return cache instance | 缓存实例
     */
    public static <K, V> Cache<K, V> getOrCreate(String name, Consumer<CacheConfig.Builder<K, V>> configurer) {
        return CacheManager.getInstance().getOrCreateCache(name, configurer);
    }

    /**
     * Create cache from specification string
     * 从规范字符串创建缓存
     *
     * <p>Parses a comma-separated specification string to configure the cache.
     * This is convenient for loading configuration from properties files.</p>
     * <p>解析逗号分隔的规范字符串来配置缓存。便于从配置文件加载配置。</p>
     *
     * <p><strong>Supported options | 支持的选项:</strong></p>
     * <ul>
     *   <li>{@code maximumSize=<long>} - Maximum entries | 最大条目数</li>
     *   <li>{@code expireAfterWrite=<duration>} - TTL (e.g., 30m, 1h, 1d) | TTL 过期</li>
     *   <li>{@code expireAfterAccess=<duration>} - TTI | TTI 过期</li>
     *   <li>{@code evictionPolicy=<lru|lfu|fifo|wtinylfu>} - Eviction policy | 淘汰策略</li>
     *   <li>{@code recordStats} - Enable statistics | 启用统计</li>
     *   <li>{@code useVirtualThreads} - Enable virtual threads | 启用虚拟线程</li>
     * </ul>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * // From code | 代码中使用
     * Cache<String, User> cache = OpenCache.fromSpec("users",
     *     "maximumSize=10000,expireAfterWrite=30m,recordStats");
     *
     * // From properties | 从配置文件
     * String spec = props.getProperty("cache.users.spec");
     * Cache<String, User> cache = OpenCache.fromSpec("users", spec);
     * }</pre>
     *
     * @param name cache name | 缓存名称
     * @param spec specification string | 规范字符串
     * @param <K>  key type | 键类型
     * @param <V>  value type | 值类型
     * @return cache instance | 缓存实例
     * @throws cloud.opencode.base.cache.exception.OpenCacheException if spec is invalid | 规范无效时抛出异常
     * @since V2.1.0
     */
    public static <K, V> Cache<K, V> fromSpec(String name, String spec) {
        CacheConfig<K, V> config = CacheSpec.parse(spec);
        return CacheManager.getInstance().createCache(name, config);
    }

    /**
     * Create anonymous cache from specification string
     * 从规范字符串创建匿名缓存
     *
     * @param spec specification string | 规范字符串
     * @param <K>  key type | 键类型
     * @param <V>  value type | 值类型
     * @return cache instance | 缓存实例
     * @throws cloud.opencode.base.cache.exception.OpenCacheException if spec is invalid | 规范无效时抛出异常
     * @since V2.1.0
     */
    public static <K, V> Cache<K, V> fromSpec(String spec) {
        CacheConfig<K, V> config = CacheSpec.parse(spec);
        return new DefaultCache<>("anonymous-" + System.nanoTime(), config);
    }

    /**
     * Parse specification string to CacheConfig
     * 解析规范字符串为 CacheConfig
     *
     * @param spec specification string | 规范字符串
     * @param <K>  key type | 键类型
     * @param <V>  value type | 值类型
     * @return parsed CacheConfig | 解析后的 CacheConfig
     * @throws cloud.opencode.base.cache.exception.OpenCacheException if spec is invalid | 规范无效时抛出异常
     * @since V2.1.0
     */
    public static <K, V> CacheConfig<K, V> parseSpec(String spec) {
        return CacheSpec.parse(spec);
    }

    /**
     * Validate a specification string
     * 验证规范字符串
     *
     * @param spec specification string | 规范字符串
     * @return true if valid | 有效返回 true
     * @since V2.1.0
     */
    public static boolean isValidSpec(String spec) {
        return CacheSpec.isValid(spec);
    }

    /**
     * Create cache configuration builder
     * 创建缓存配置构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return configuration builder | 配置构建器
     */
    public static <K, V> CacheBuilder<K, V> builder() {
        return new CacheBuilder<>();
    }

    // ==================== Cache Query | 缓存查询 ====================

    /**
     * Get existing cache by name
     * 按名称获取已存在的缓存
     *
     * @param name cache name | 缓存名称
     * @param <K>  key type | 键类型
     * @param <V>  value type | 值类型
     * @return optional containing cache | 包含缓存的 Optional
     */
    public static <K, V> Optional<Cache<K, V>> get(String name) {
        return CacheManager.getInstance().getCache(name);
    }

    /**
     * Get all cache names
     * 获取所有缓存名称
     *
     * @return set of cache names | 缓存名称集合
     */
    public static Set<String> names() {
        return CacheManager.getInstance().getCacheNames();
    }

    // ==================== Cache Management | 缓存管理 ====================

    /**
     * Remove cache by name
     * 按名称移除缓存
     *
     * @param name cache name | 缓存名称
     */
    public static void remove(String name) {
        CacheManager.getInstance().removeCache(name);
    }

    /**
     * Clean up all caches
     * 清理所有缓存
     */
    public static void cleanUpAll() {
        CacheManager.getInstance().cleanUpAll();
    }

    /**
     * Get global statistics
     * 获取全局统计
     *
     * @return map of cache name to stats | 缓存名称到统计的映射
     */
    public static Map<String, CacheStats> stats() {
        return CacheManager.getInstance().getAllStats();
    }

    /**
     * Shutdown cache system
     * 关闭缓存系统
     */
    public static void shutdown() {
        CacheManager.getInstance().shutdown();
    }

    /**
     * Invalidate a single key in named cache
     * 使命名缓存中的单个键失效
     *
     * @param name cache name | 缓存名称
     * @param key  key to invalidate | 要失效的键
     * @param <K>  key type | 键类型
     * @param <V>  value type | 值类型
     */
    public static <K, V> void invalidate(String name, K key) {
        OpenCache.<K, V>get(name).ifPresent(cache -> cache.invalidate(key));
    }

    /**
     * Invalidate all entries in named cache
     * 使命名缓存中的所有条目失效
     *
     * @param name cache name | 缓存名称
     */
    public static void invalidateAll(String name) {
        get(name).ifPresent(Cache::invalidateAll);
    }

    /**
     * Check if named cache is empty
     * 检查命名缓存是否为空
     *
     * @param name cache name | 缓存名称
     * @return true if empty or not exists | 如果为空或不存在返回true
     */
    public static boolean isEmpty(String name) {
        return get(name).map(cache -> cache.estimatedSize() == 0).orElse(true);
    }

    /**
     * Get size of named cache
     * 获取命名缓存的大小
     *
     * @param name cache name | 缓存名称
     * @return cache size or 0 if not exists | 缓存大小，不存在返回0
     */
    public static long size(String name) {
        return get(name).map(Cache::estimatedSize).orElse(0L);
    }

    /**
     * Put value in named cache
     * 在命名缓存中放入值
     *
     * @param name  cache name | 缓存名称
     * @param key   the key | 键
     * @param value the value | 值
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     */
    public static <K, V> void put(String name, K key, V value) {
        Cache<K, V> cache = getOrCreate(name);
        cache.put(key, value);
    }

    /**
     * Get value from named cache
     * 从命名缓存中获取值
     *
     * @param name cache name | 缓存名称
     * @param key  the key | 键
     * @param <K>  key type | 键类型
     * @param <V>  value type | 值类型
     * @return optional containing value | 包含值的 Optional
     */
    public static <K, V> Optional<V> getValue(String name, K key) {
        return OpenCache.<K, V>get(name).map(cache -> cache.get(key));
    }

    // ==================== Quick Cache Factories | 快速缓存工厂 ====================

    /**
     * Create a simple LRU cache with given maximum size
     * 创建具有给定最大容量的简单 LRU 缓存
     *
     * @param maxSize maximum cache size | 最大缓存容量
     * @param <K>     key type | 键类型
     * @param <V>     value type | 值类型
     * @return LRU cache | LRU 缓存
     * @since V2.0.2
     */
    public static <K, V> Cache<K, V> lruCache(long maxSize) {
        return OpenCache.<K, V>builder()
                .maximumSize(maxSize)
                .evictionPolicy(EvictionPolicy.lru())
                .build();
    }

    /**
     * Create a simple LFU cache with given maximum size
     * 创建具有给定最大容量的简单 LFU 缓存
     *
     * @param maxSize maximum cache size | 最大缓存容量
     * @param <K>     key type | 键类型
     * @param <V>     value type | 值类型
     * @return LFU cache | LFU 缓存
     * @since V2.0.2
     */
    public static <K, V> Cache<K, V> lfuCache(long maxSize) {
        return OpenCache.<K, V>builder()
                .maximumSize(maxSize)
                .evictionPolicy(EvictionPolicy.lfu())
                .build();
    }

    /**
     * Create a simple FIFO cache with given maximum size
     * 创建具有给定最大容量的简单 FIFO 缓存
     *
     * @param maxSize maximum cache size | 最大缓存容量
     * @param <K>     key type | 键类型
     * @param <V>     value type | 值类型
     * @return FIFO cache | FIFO 缓存
     * @since V2.0.2
     */
    public static <K, V> Cache<K, V> fifoCache(long maxSize) {
        return OpenCache.<K, V>builder()
                .maximumSize(maxSize)
                .evictionPolicy(EvictionPolicy.fifo())
                .build();
    }

    /**
     * Create a TTL-based cache with given size and expiration
     * 创建具有给定容量和过期时间的 TTL 缓存
     *
     * @param maxSize  maximum cache size | 最大缓存容量
     * @param duration time-to-live | 存活时间
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return TTL cache | TTL 缓存
     * @since V2.0.2
     */
    public static <K, V> Cache<K, V> ttlCache(long maxSize, Duration duration) {
        return OpenCache.<K, V>builder()
                .maximumSize(maxSize)
                .expireAfterWrite(duration)
                .build();
    }

    /**
     * Create a TTI-based cache with given size and idle expiration
     * 创建具有给定容量和空闲过期时间的 TTI 缓存
     *
     * @param maxSize  maximum cache size | 最大缓存容量
     * @param duration time-to-idle | 空闲时间
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return TTI cache | TTI 缓存
     * @since V2.0.2
     */
    public static <K, V> Cache<K, V> ttiCache(long maxSize, Duration duration) {
        return OpenCache.<K, V>builder()
                .maximumSize(maxSize)
                .expireAfterAccess(duration)
                .build();
    }

    /**
     * Create a high-performance W-TinyLFU cache
     * 创建高性能 W-TinyLFU 缓存
     *
     * @param maxSize maximum cache size | 最大缓存容量
     * @param <K>     key type | 键类型
     * @param <V>     value type | 值类型
     * @return W-TinyLFU cache | W-TinyLFU 缓存
     * @since V2.0.2
     */
    public static <K, V> Cache<K, V> wTinyLfuCache(long maxSize) {
        return OpenCache.<K, V>builder()
                .maximumSize(maxSize)
                .evictionPolicy(EvictionPolicy.wTinyLfu())
                .build();
    }

    /**
     * Create a cache with both LRU and TTL
     * 创建同时具有 LRU 和 TTL 的缓存
     *
     * @param maxSize  maximum cache size | 最大缓存容量
     * @param duration time-to-live | 存活时间
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return LRU+TTL cache | LRU+TTL 缓存
     * @since V2.0.2
     */
    public static <K, V> Cache<K, V> lruTtlCache(long maxSize, Duration duration) {
        return OpenCache.<K, V>builder()
                .maximumSize(maxSize)
                .evictionPolicy(EvictionPolicy.lru())
                .expireAfterWrite(duration)
                .build();
    }

    /**
     * Create a loading cache with automatic value loading
     * 创建具有自动值加载功能的加载缓存
     *
     * @param name    cache name | 缓存名称
     * @param loader  the loader function | 加载函数
     * @param maxSize maximum cache size | 最大缓存容量
     * @param <K>     key type | 键类型
     * @param <V>     value type | 值类型
     * @return loading cache | 加载缓存
     * @since V2.0.3
     */
    public static <K, V> LoadingCache<K, V> loadingCache(String name,
                                                          java.util.function.Function<? super K, ? extends V> loader,
                                                          long maxSize) {
        return LoadingCache.create(name, loader, CacheConfig.<K, V>builder()
                .maximumSize(maxSize)
                .build());
    }

    /**
     * Create a loading cache with TTL
     * 创建具有 TTL 的加载缓存
     *
     * @param name     cache name | 缓存名称
     * @param loader   the loader function | 加载函数
     * @param maxSize  maximum cache size | 最大缓存容量
     * @param duration time-to-live | 存活时间
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return loading cache with TTL | 具有 TTL 的加载缓存
     * @since V2.0.3
     */
    public static <K, V> LoadingCache<K, V> loadingCache(String name,
                                                          java.util.function.Function<? super K, ? extends V> loader,
                                                          long maxSize,
                                                          Duration duration) {
        return LoadingCache.create(name, loader, CacheConfig.<K, V>builder()
                .maximumSize(maxSize)
                .expireAfterWrite(duration)
                .build());
    }

    /**
     * Start a decorator chain for the given cache
     * 为给定的缓存开始装饰器链
     *
     * @param cache the cache to decorate | 要装饰的缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return decorator chain builder | 装饰器链构建器
     * @since V2.0.2
     */
    public static <K, V> CacheDecorators.ChainBuilder<K, V> decorate(Cache<K, V> cache) {
        return CacheDecorators.chain(cache);
    }

    // ==================== Eviction Policy Factories | 淘汰策略工厂 ====================

    /**
     * Create LRU eviction policy
     * 创建 LRU 淘汰策略
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return LRU policy | LRU 策略
     */
    public static <K, V> EvictionPolicy<K, V> lru() {
        return EvictionPolicy.lru();
    }

    /**
     * Create LFU eviction policy
     * 创建 LFU 淘汰策略
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return LFU policy | LFU 策略
     */
    public static <K, V> EvictionPolicy<K, V> lfu() {
        return EvictionPolicy.lfu();
    }

    /**
     * Create FIFO eviction policy
     * 创建 FIFO 淘汰策略
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return FIFO policy | FIFO 策略
     */
    public static <K, V> EvictionPolicy<K, V> fifo() {
        return EvictionPolicy.fifo();
    }

    /**
     * Create W-TinyLFU eviction policy
     * 创建 W-TinyLFU 淘汰策略
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return W-TinyLFU policy | W-TinyLFU 策略
     */
    public static <K, V> EvictionPolicy<K, V> wTinyLfu() {
        return EvictionPolicy.wTinyLfu();
    }

    // ==================== Expiry Policy Factories | 过期策略工厂 ====================

    /**
     * Create TTL expiry policy
     * 创建 TTL 过期策略
     *
     * @param duration expiration duration | 过期时长
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return TTL policy | TTL 策略
     */
    public static <K, V> ExpiryPolicy<K, V> ttl(Duration duration) {
        return ExpiryPolicy.ttl(duration);
    }

    /**
     * Create TTI expiry policy
     * 创建 TTI 过期策略
     *
     * @param duration expiration duration | 过期时长
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return TTI policy | TTI 策略
     */
    public static <K, V> ExpiryPolicy<K, V> tti(Duration duration) {
        return ExpiryPolicy.tti(duration);
    }

    /**
     * Create combined TTL and TTI expiry policy
     * 创建组合 TTL 和 TTI 过期策略
     *
     * @param ttl TTL duration | TTL 时长
     * @param tti TTI duration | TTI 时长
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return combined policy | 组合策略
     */
    public static <K, V> ExpiryPolicy<K, V> combined(Duration ttl, Duration tti) {
        return ExpiryPolicy.combined(ttl, tti);
    }

    // ==================== Cache Builder | 缓存构建器 ====================

    /**
     * Cache Builder - Fluent API for building caches
     * 缓存构建器 - 用于构建缓存的流式 API
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    public static final class CacheBuilder<K, V> {

        /** Creates a new CacheBuilder instance | 创建新的 CacheBuilder 实例 */
        public CacheBuilder() {}
        private final CacheConfig.Builder<K, V> configBuilder = CacheConfig.builder();

        /**
         * maximumSize | maximumSize
         * @param size the size | size
         * @return the result | 结果
         */
        public CacheBuilder<K, V> maximumSize(long size) {
            configBuilder.maximumSize(size);
            return this;
        }

        /**
         * maximumWeight | maximumWeight
         * @param weight the weight | weight
         * @return the result | 结果
         */
        public CacheBuilder<K, V> maximumWeight(long weight) {
            configBuilder.maximumWeight(weight);
            return this;
        }

        /**
         * expireAfterWrite | expireAfterWrite
         * @param duration the duration | duration
         * @return the result | 结果
         */
        public CacheBuilder<K, V> expireAfterWrite(Duration duration) {
            configBuilder.expireAfterWrite(duration);
            return this;
        }

        /**
         * expireAfterAccess | expireAfterAccess
         * @param duration the duration | duration
         * @return the result | 结果
         */
        public CacheBuilder<K, V> expireAfterAccess(Duration duration) {
            configBuilder.expireAfterAccess(duration);
            return this;
        }

        /**
         * Sets the eviction policy | 设置淘汰策略
         *
         * @param policy the eviction policy | 淘汰策略
         * @return this builder | 此构建器
         */
        public CacheBuilder<K, V> evictionPolicy(EvictionPolicy<K, V> policy) {
            configBuilder.evictionPolicy(policy);
            return this;
        }

        /**
         * Sets the expiry policy | 设置过期策略
         *
         * @param policy the expiry policy | 过期策略
         * @return this builder | 此构建器
         */
        public CacheBuilder<K, V> expiryPolicy(ExpiryPolicy<K, V> policy) {
            configBuilder.expiryPolicy(policy);
            return this;
        }

        /**
         * recordStats | recordStats
         * @return the result | 结果
         */
        public CacheBuilder<K, V> recordStats() {
            configBuilder.recordStats();
            return this;
        }

        /**
         * useVirtualThreads | useVirtualThreads
         * @return the result | 结果
         */
        public CacheBuilder<K, V> useVirtualThreads() {
            configBuilder.useVirtualThreads();
            return this;
        }

        /**
         * Build cache with name
         * 构建命名缓存
         *
         * @param name cache name | 缓存名称
         * @return cache instance | 缓存实例
         */
        public Cache<K, V> build(String name) {
            return CacheManager.getInstance().createCache(name, configBuilder.build());
        }

        /**
         * Build anonymous cache
         * 构建匿名缓存
         *
         * @return cache instance | 缓存实例
         */
        public Cache<K, V> build() {
            return new DefaultCache<>("anonymous-" + System.nanoTime(), configBuilder.build());
        }
    }
}
