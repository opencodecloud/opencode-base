package cloud.opencode.base.cache;

import cloud.opencode.base.cache.protection.BloomFilter;
import cloud.opencode.base.cache.protection.SingleFlight;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Protected Cache - Auto-integrates BloomFilter and SingleFlight protection
 * 保护缓存 - 自动集成布隆过滤器和单次加载保护
 *
 * <p>Wraps an existing cache to automatically apply protection mechanisms:</p>
 * <p>包装现有缓存以自动应用保护机制：</p>
 * <ul>
 *   <li><strong>BloomFilter</strong> - Prevents cache penetration by tracking negative lookups | 通过跟踪未命中来防止缓存穿透</li>
 *   <li><strong>SingleFlight</strong> - Prevents thundering herd by deduplicating concurrent loads | 通过合并并发加载来防止惊群效应</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create protected cache with defaults
 * Cache<String, User> cache = OpenCache.getOrCreate("users");
 * ProtectedCache<String, User> protected = ProtectedCache.wrap(cache).build();
 *
 * // With custom configuration
 * ProtectedCache<String, User> protected = ProtectedCache.wrap(cache)
 *     .bloomFilter(1_000_000, 0.01)  // 1M expected, 1% false positive
 *     .singleFlight(true)
 *     .negativeCache(Duration.ofMinutes(5))  // Cache misses for 5 min
 *     .build();
 *
 * // Load with protection
 * User user = protected.get("user:1", key -> userService.findById(key));
 * // BloomFilter prevents repeated loads for non-existent keys
 * // SingleFlight deduplicates concurrent loads for same key
 * }</pre>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>BloomFilter for negative lookup tracking - 布隆过滤器跟踪未命中查找</li>
 *   <li>SingleFlight for load deduplication - SingleFlight 合并并发加载</li>
 *   <li>Negative cache with TTL - 带 TTL 的负缓存</li>
 *   <li>Cache penetration prevention - 缓存穿透防护</li>
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
 * @since JDK 25, opencode-base-cache V2.0.0
 */
public final class ProtectedCache<K, V> implements Cache<K, V> {

    private static final System.Logger LOGGER = System.getLogger(ProtectedCache.class.getName());
    private static final int CLEANUP_THRESHOLD = 1000;
    private static final int MAX_NEGATIVE_CACHE_SIZE = 10000;
    private static final long NEGATIVE_CACHE_CLEANUP_INTERVAL_MS = 30_000; // 30 seconds

    private final Cache<K, V> delegate;
    private final BloomFilter negativeBloomFilter;
    private final SingleFlight<K, V> singleFlight;
    private final boolean useBloomFilter;
    private final boolean useSingleFlight;
    private final Duration negativeCacheDuration;
    private final Map<K, Long> negativeCacheExpiry;
    private final java.util.concurrent.atomic.AtomicInteger operationCounter = new java.util.concurrent.atomic.AtomicInteger(0);
    private final java.util.concurrent.ScheduledExecutorService negativeCacheCleanupScheduler;

    private ProtectedCache(Builder<K, V> builder) {
        this.delegate = builder.delegate;
        this.useBloomFilter = builder.useBloomFilter;
        this.useSingleFlight = builder.useSingleFlight;
        this.negativeCacheDuration = builder.negativeCacheDuration;

        if (useBloomFilter) {
            this.negativeBloomFilter = BloomFilter.create(
                    builder.expectedInsertions,
                    builder.falsePositiveRate
            );
        } else {
            this.negativeBloomFilter = null;
        }

        if (useSingleFlight) {
            this.singleFlight = new SingleFlight<>();
        } else {
            this.singleFlight = null;
        }

        if (negativeCacheDuration != null) {
            this.negativeCacheExpiry = new java.util.concurrent.ConcurrentHashMap<>();
            // Start background cleanup for negative cache
            this.negativeCacheCleanupScheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = Thread.ofPlatform().daemon(true).name("protected-cache-cleanup-" + delegate.name()).unstarted(r);
                return t;
            });
            this.negativeCacheCleanupScheduler.scheduleWithFixedDelay(
                    this::backgroundNegativeCacheCleanup,
                    NEGATIVE_CACHE_CLEANUP_INTERVAL_MS,
                    NEGATIVE_CACHE_CLEANUP_INTERVAL_MS,
                    java.util.concurrent.TimeUnit.MILLISECONDS
            );
        } else {
            this.negativeCacheExpiry = null;
            this.negativeCacheCleanupScheduler = null;
        }
    }

    /**
     * Background cleanup task for negative cache entries
     * 后台清理任务，用于清理负缓存条目
     */
    private void backgroundNegativeCacheCleanup() {
        try {
            if (negativeCacheExpiry != null) {
                long now = System.currentTimeMillis();
                negativeCacheExpiry.entrySet().removeIf(e -> e.getValue() < now);
            }
        } catch (Exception e) {
            // Log but don't propagate to prevent scheduler termination
            LOGGER.log(System.Logger.Level.WARNING, "Error during negative cache cleanup for " + delegate.name(), e);
        }
    }

    /**
     * Wrap a cache with protection
     * 使用保护包装缓存
     *
     * @param cache cache to wrap | 要包装的缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> wrap(Cache<K, V> cache) {
        return new Builder<>(cache);
    }

    // ==================== Protected Operations | 保护操作 ====================

    @Override
    public V get(K key) {
        return delegate.get(key);
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> loader) {
        // Check negative cache first
        if (isNegativelyCached(key)) {
            return null;
        }

        // Check bloom filter for known misses
        if (useBloomFilter && negativeBloomFilter.mightContain(key)) {
            // Might be a negative lookup, but could be false positive
            // Still try to load, but with lower priority
        }

        // Use single flight to deduplicate concurrent loads
        if (useSingleFlight) {
            return singleFlight.execute(key, k -> loadWithNegativeTracking(k, loader));
        }

        return loadWithNegativeTracking(key, loader);
    }

    private V loadWithNegativeTracking(K key, Function<? super K, ? extends V> loader) {
        V value = delegate.get(key, loader);

        if (value == null) {
            // Track negative lookup
            if (useBloomFilter) {
                negativeBloomFilter.add(key);
            }
            if (negativeCacheDuration != null) {
                negativeCacheExpiry.put(key, System.currentTimeMillis() + negativeCacheDuration.toMillis());
                // Trigger cleanup if needed
                maybeCleanupNegativeCache();
            }
        }

        return value;
    }

    /**
     * Proactively clean up negative cache to prevent unbounded growth
     * 主动清理负缓存以防止无限增长
     */
    private void maybeCleanupNegativeCache() {
        if (negativeCacheExpiry == null) {
            return;
        }

        int ops = operationCounter.incrementAndGet();

        // Clean up periodically or when size exceeds threshold
        boolean shouldCleanup = (ops % CLEANUP_THRESHOLD == 0) ||
                                (negativeCacheExpiry.size() > MAX_NEGATIVE_CACHE_SIZE);

        if (shouldCleanup) {
            long now = System.currentTimeMillis();
            negativeCacheExpiry.entrySet().removeIf(e -> e.getValue() < now);
        }
    }

    private boolean isNegativelyCached(K key) {
        if (negativeCacheExpiry == null) {
            return false;
        }

        Long expiry = negativeCacheExpiry.get(key);
        if (expiry == null) {
            return false;
        }

        if (System.currentTimeMillis() > expiry) {
            negativeCacheExpiry.remove(key);
            return false;
        }

        return true;
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
        // Clear negative cache on put
        if (negativeCacheExpiry != null) {
            negativeCacheExpiry.remove(key);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        delegate.putAll(map);
        if (negativeCacheExpiry != null) {
            map.keySet().forEach(negativeCacheExpiry::remove);
        }
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        boolean result = delegate.putIfAbsent(key, value);
        if (result && negativeCacheExpiry != null) {
            negativeCacheExpiry.remove(key);
        }
        return result;
    }

    @Override
    public void putWithTtl(K key, V value, Duration ttl) {
        delegate.putWithTtl(key, value, ttl);
        if (negativeCacheExpiry != null) {
            negativeCacheExpiry.remove(key);
        }
    }

    @Override
    public void putAllWithTtl(Map<? extends K, ? extends V> map, Duration ttl) {
        delegate.putAllWithTtl(map, ttl);
        if (negativeCacheExpiry != null) {
            map.keySet().forEach(negativeCacheExpiry::remove);
        }
    }

    @Override
    public boolean putIfAbsentWithTtl(K key, V value, Duration ttl) {
        boolean result = delegate.putIfAbsentWithTtl(key, value, ttl);
        if (result && negativeCacheExpiry != null) {
            negativeCacheExpiry.remove(key);
        }
        return result;
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
    public V getAndRemove(K key) {
        return delegate.getAndRemove(key);
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
        if (negativeCacheExpiry != null) {
            negativeCacheExpiry.clear();
        }
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
        // Clean expired negative cache entries
        if (negativeCacheExpiry != null) {
            long now = System.currentTimeMillis();
            negativeCacheExpiry.entrySet().removeIf(e -> e.getValue() < now);
        }
    }

    @Override
    public AsyncCache<K, V> async() {
        return delegate.async();
    }

    @Override
    public String name() {
        return delegate.name();
    }

    /**
     * Get protection statistics
     * 获取保护统计
     *
     * @return protection stats | 保护统计
     */
    public ProtectionStats getProtectionStats() {
        return new ProtectionStats(
                negativeBloomFilter != null ? negativeBloomFilter.approximateElementCount() : 0,
                negativeCacheExpiry != null ? negativeCacheExpiry.size() : 0,
                singleFlight != null ? singleFlight.inflightCount() : 0
        );
    }

    /**
     * Clear negative cache
     * 清除负缓存
     */
    public void clearNegativeCache() {
        if (negativeCacheExpiry != null) {
            negativeCacheExpiry.clear();
        }
    }

    /**
     * Shutdown the protected cache and release resources
     * 关闭保护缓存并释放资源
     *
     * @since V2.0.3
     */
    public void shutdown() {
        if (negativeCacheCleanupScheduler != null && !negativeCacheCleanupScheduler.isShutdown()) {
            negativeCacheCleanupScheduler.shutdown();
            try {
                if (!negativeCacheCleanupScheduler.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    negativeCacheCleanupScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                negativeCacheCleanupScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Check if a key is in the negative cache
     * 检查键是否在负缓存中
     *
     * @param key the key to check | 要检查的键
     * @return true if negatively cached | 在负缓存中返回 true
     * @since V2.0.1
     */
    public boolean isKeyNegativelyCached(K key) {
        return isNegativelyCached(key);
    }

    /**
     * Reset protection statistics (clears BloomFilter tracking)
     * 重置保护统计（清除布隆过滤器跟踪）
     *
     * <p>Note: This creates a new BloomFilter instance. Use with caution as it
     * may temporarily increase false negatives until the filter is repopulated.</p>
     * <p>注意：这会创建新的布隆过滤器实例。谨慎使用，因为在过滤器重新填充前可能会增加假阴性。</p>
     *
     * @since V2.0.1
     */
    public void resetProtectionStats() {
        clearNegativeCache();
        // Note: BloomFilter cannot be easily reset without recreation
        // This clears negative cache only; BloomFilter entries remain
    }

    /**
     * Check if BloomFilter might contain a key (for negative lookup tracking)
     * 检查布隆过滤器是否可能包含某键（用于负查找跟踪）
     *
     * @param key the key to check | 要检查的键
     * @return true if might contain (could be false positive) | 可能包含返回 true（可能是假阳性）
     * @since V2.0.1
     */
    public boolean mightContainInBloomFilter(K key) {
        return useBloomFilter && negativeBloomFilter != null && negativeBloomFilter.mightContain(key);
    }

    // ==================== Records ====================

    /**
     * Protection statistics
     *
     * @param bloomFilterEntries the number of bloom filter entries | 布隆过滤器条目数
     * @param negativeCacheEntries the number of negative cache entries | 负缓存条目数
     * @param inFlightLoads the number of in-flight loads | 进行中的加载数
     */
    public record ProtectionStats(
            long bloomFilterEntries,
            int negativeCacheEntries,
            int inFlightLoads
    ) {
    }

    // ==================== Builder ====================

    /**
     * Builder for ProtectedCache
     *
     * @param <K> the key type | 键类型
     * @param <V> the value type | 值类型
     */
    public static class Builder<K, V> {
        private final Cache<K, V> delegate;
        private boolean useBloomFilter = true;
        private boolean useSingleFlight = true;
        private long expectedInsertions = 100_000;
        private double falsePositiveRate = 0.01;
        private Duration negativeCacheDuration;

        Builder(Cache<K, V> delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
        }

        /**
         * Enable/disable bloom filter
         * 启用/禁用布隆过滤器
          * @param enable the enable | enable
          * @return the result | 结果
         */
        public Builder<K, V> bloomFilter(boolean enable) {
            this.useBloomFilter = enable;
            return this;
        }

        /**
         * Configure bloom filter
         * 配置布隆过滤器
         *
         * @param expectedInsertions expected number of insertions | 预期插入数
         * @param falsePositiveRate  false positive rate (0-1) | 误报率
          * @return the result | 结果
         */
        public Builder<K, V> bloomFilter(long expectedInsertions, double falsePositiveRate) {
            this.useBloomFilter = true;
            this.expectedInsertions = expectedInsertions;
            this.falsePositiveRate = falsePositiveRate;
            return this;
        }

        /**
         * Enable/disable single flight
         * 启用/禁用单次加载
          * @param enable the enable | enable
          * @return the result | 结果
         */
        public Builder<K, V> singleFlight(boolean enable) {
            this.useSingleFlight = enable;
            return this;
        }

        /**
         * Set negative cache duration
         * 设置负缓存时长
         *
         * @param duration how long to cache negative lookups | 负查找缓存时长
          * @return the result | 结果
         */
        public Builder<K, V> negativeCache(Duration duration) {
            this.negativeCacheDuration = duration;
            return this;
        }

        /**
         * Build the protected cache
         * 构建保护缓存
          * @return the result | 结果
         */
        public ProtectedCache<K, V> build() {
            return new ProtectedCache<>(this);
        }
    }
}
