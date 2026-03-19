package cloud.opencode.base.cache;

import cloud.opencode.base.cache.config.CacheConfig;
import cloud.opencode.base.cache.internal.DefaultCache;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Cache Manager - Global cache instance management
 * 缓存管理器 - 全局缓存实例管理
 *
 * <p>Singleton manager for creating, retrieving, and managing cache instances.</p>
 * <p>用于创建、获取和管理缓存实例的单例管理器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Cache creation and registration - 缓存创建和注册</li>
 *   <li>Named cache retrieval - 命名缓存获取</li>
 *   <li>Global statistics - 全局统计</li>
 *   <li>Bulk operations (cleanUp, shutdown) - 批量操作（清理、关闭）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CacheManager manager = CacheManager.getInstance();
 *
 * // Create or get cache - 创建或获取缓存
 * Cache<String, User> cache = manager.getOrCreateCache("users", config -> config
 *     .maximumSize(10000)
 *     .expireAfterWrite(Duration.ofMinutes(30)));
 *
 * // Get existing cache - 获取已存在的缓存
 * Optional<Cache<String, User>> existing = manager.getCache("users");
 *
 * // Get all cache names - 获取所有缓存名称
 * Set<String> names = manager.getCacheNames();
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
public final class CacheManager {

    private static final CacheManager INSTANCE = new CacheManager();

    private final ConcurrentHashMap<String, Cache<?, ?>> caches = new ConcurrentHashMap<>();
    private volatile boolean shutdown = false;

    private CacheManager() {
    }

    /**
     * Get singleton instance
     * 获取单例实例
     *
     * @return cache manager instance | 缓存管理器实例
     */
    public static CacheManager getInstance() {
        return INSTANCE;
    }

    /**
     * Get or create cache with configuration
     * 获取或创建带配置的缓存
     *
     * @param name       cache name | 缓存名称
     * @param configurer configuration consumer | 配置消费者
     * @param <K>        key type | 键类型
     * @param <V>        value type | 值类型
     * @return cache instance | 缓存实例
     */
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getOrCreateCache(String name, Consumer<CacheConfig.Builder<K, V>> configurer) {
        checkNotShutdown();
        return (Cache<K, V>) caches.computeIfAbsent(name, n -> {
            CacheConfig.Builder<K, V> builder = CacheConfig.builder();
            if (configurer != null) {
                configurer.accept(builder);
            }
            return new DefaultCache<>(name, builder.build());
        });
    }

    /**
     * Get or create cache with default configuration
     * 获取或创建默认配置的缓存
     *
     * @param name cache name | 缓存名称
     * @param <K>  key type | 键类型
     * @param <V>  value type | 值类型
     * @return cache instance | 缓存实例
     */
    public <K, V> Cache<K, V> getOrCreateCache(String name) {
        return getOrCreateCache(name, null);
    }

    /**
     * Create cache with configuration (always creates new)
     * 创建带配置的缓存（总是创建新的）
     *
     * @param name   cache name | 缓存名称
     * @param config cache configuration | 缓存配置
     * @param <K>    key type | 键类型
     * @param <V>    value type | 值类型
     * @return cache instance | 缓存实例
     */
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> createCache(String name, CacheConfig<K, V> config) {
        checkNotShutdown();
        Cache<K, V> cache = new DefaultCache<>(name, config);
        Cache<?, ?> existing = caches.put(name, cache);
        if (existing != null) {
            existing.invalidateAll();
        }
        return cache;
    }

    /**
     * Get existing cache
     * 获取已存在的缓存
     *
     * @param name cache name | 缓存名称
     * @param <K>  key type | 键类型
     * @param <V>  value type | 值类型
     * @return optional containing cache if exists | 包含缓存的 Optional（如存在）
     */
    @SuppressWarnings("unchecked")
    public <K, V> Optional<Cache<K, V>> getCache(String name) {
        return Optional.ofNullable((Cache<K, V>) caches.get(name));
    }

    /**
     * Get all cache names
     * 获取所有缓存名称
     *
     * @return set of cache names | 缓存名称集合
     */
    public Set<String> getCacheNames() {
        return Set.copyOf(caches.keySet());
    }

    /**
     * Remove cache by name
     * 按名称移除缓存
     *
     * @param name cache name | 缓存名称
     */
    public void removeCache(String name) {
        Cache<?, ?> removed = caches.remove(name);
        if (removed != null) {
            removed.invalidateAll();
        }
    }

    /**
     * Get statistics for all caches
     * 获取所有缓存的统计信息
     *
     * @return map of cache name to stats | 缓存名称到统计的映射
     */
    public Map<String, CacheStats> getAllStats() {
        return caches.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stats()
                ));
    }

    /**
     * Get combined statistics
     * 获取合并的统计信息
     *
     * @return combined stats | 合并的统计
     */
    public CacheStats getCombinedStats() {
        return caches.values().stream()
                .map(Cache::stats)
                .reduce(CacheStats.empty(), CacheStats::plus);
    }

    /**
     * Get statistics for a specific cache by name
     * 按名称获取特定缓存的统计信息
     *
     * @param name cache name | 缓存名称
     * @return cache stats or empty if not found | 缓存统计，不存在返回空统计
     * @since V2.0.1
     */
    public CacheStats getCacheStats(String name) {
        Cache<?, ?> cache = caches.get(name);
        return cache != null ? cache.stats() : CacheStats.empty();
    }

    /**
     * Get caches matching a name pattern (supports * and ? wildcards)
     * 获取匹配名称模式的缓存（支持 * 和 ? 通配符）
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * // Get all user-related caches
     * Map<String, Cache> userCaches = manager.getCachesByPattern("user:*");
     *
     * // Get all caches ending with "-cache"
     * Map<String, Cache> caches = manager.getCachesByPattern("*-cache");
     * }</pre>
     *
     * @param pattern name pattern | 名称模式
     * @param <K>     key type | 键类型
     * @param <V>     value type | 值类型
     * @return map of matching caches | 匹配的缓存 Map
     * @since V2.0.1
     */
    @SuppressWarnings("unchecked")
    public <K, V> java.util.Map<String, Cache<K, V>> getCachesByPattern(String pattern) {
        String regex = pattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".");
        java.util.regex.Pattern compiled = java.util.regex.Pattern.compile("^" + regex + "$");

        java.util.Map<String, Cache<K, V>> result = new java.util.LinkedHashMap<>();
        for (java.util.Map.Entry<String, Cache<?, ?>> entry : caches.entrySet()) {
            if (compiled.matcher(entry.getKey()).matches()) {
                result.put(entry.getKey(), (Cache<K, V>) entry.getValue());
            }
        }
        return result;
    }

    /**
     * Invalidate all entries in caches matching a name pattern
     * 使匹配名称模式的缓存中的所有条目失效
     *
     * @param pattern name pattern | 名称模式
     * @return count of invalidated caches | 失效的缓存数量
     * @since V2.0.1
     */
    @SuppressWarnings("unchecked")
    public int invalidateByPattern(String pattern) {
        java.util.Map<String, Cache<Object, Object>> matching = getCachesByPattern(pattern);
        matching.values().forEach(Cache::invalidateAll);
        return matching.size();
    }

    /**
     * Clean up all caches
     * 清理所有缓存
     */
    public void cleanUpAll() {
        caches.values().forEach(Cache::cleanUp);
    }

    /**
     * Invalidate all entries in all caches
     * 使所有缓存中的所有条目失效
     */
    public void invalidateAll() {
        caches.values().forEach(Cache::invalidateAll);
    }

    /**
     * Shutdown the cache manager, closing all managed caches and releasing resources.
     * 关闭缓存管理器，关闭所有托管缓存并释放资源。
     *
     * <p>This method invalidates all cache entries and attempts to shut down
     * internal executors and schedulers of each managed cache. Caches that
     * implement {@link AutoCloseable} will have their {@code close()} method
     * called. {@link cloud.opencode.base.cache.internal.DefaultCache} instances
     * will have their {@code shutdown()} method called to release background
     * cleanup schedulers and internally-created executors.</p>
     */
    public void shutdown() {
        shutdown = true;
        for (Cache<?, ?> cache : caches.values()) {
            try {
                cache.invalidateAll();
            } catch (Exception e) {
                // Best-effort invalidation; continue shutting down remaining caches
            }
            // Shut down DefaultCache executors and schedulers
            if (cache instanceof cloud.opencode.base.cache.internal.DefaultCache<?, ?> defaultCache) {
                try {
                    defaultCache.shutdown();
                } catch (Exception e) {
                    // Best-effort shutdown; continue with remaining caches
                }
            }
            // Close caches that implement AutoCloseable
            if (cache instanceof AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    // Best-effort close; continue with remaining caches
                }
            }
        }
        caches.clear();
    }

    /**
     * Check if manager is shut down
     * 检查管理器是否已关闭
     *
     * @return true if shut down | 已关闭返回 true
     */
    public boolean isShutdown() {
        return shutdown;
    }

    /**
     * Reset manager (for testing)
     * 重置管理器（用于测试）
     */
    public void reset() {
        // Shut down all caches before clearing to prevent resource leaks
        for (Cache<?, ?> cache : caches.values()) {
            try {
                cache.invalidateAll();
            } catch (Exception e) {
                // Best-effort invalidation
            }
            if (cache instanceof cloud.opencode.base.cache.internal.DefaultCache<?, ?> defaultCache) {
                try {
                    defaultCache.shutdown();
                } catch (Exception e) {
                    // Best-effort shutdown
                }
            }
            if (cache instanceof AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    // Best-effort close
                }
            }
        }
        caches.clear();
        shutdown = false;
    }

    private void checkNotShutdown() {
        if (shutdown) {
            throw new IllegalStateException("CacheManager has been shut down");
        }
    }
}
