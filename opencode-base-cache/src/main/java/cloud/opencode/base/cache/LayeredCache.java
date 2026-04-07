package cloud.opencode.base.cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * LayeredCache - Multi-level cache implementation (L1/L2)
 * 分层缓存 - 多级缓存实现 (L1/L2)
 *
 * <p>Implements a two-level cache strategy where L1 (fast, smaller) is checked first,
 * then L2 (slower, larger) if not found. Write-through or write-back strategies
 * can be configured.</p>
 * <p>实现两级缓存策略，首先检查 L1（快速、较小），如果未找到则检查 L2（较慢、较大）。
 * 可配置写穿或写回策略。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>L1 (local) + L2 (shared) caching - L1（本地）+ L2（共享）缓存</li>
 *   <li>Write-through strategy - 写穿策略</li>
 *   <li>Write-back strategy - 写回策略</li>
 *   <li>Automatic L1 population on L2 hit - L2命中时自动填充L1</li>
 *   <li>Configurable promotion policies - 可配置的提升策略</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create layered cache - 创建分层缓存
 * Cache<String, User> l1 = OpenCache.<String, User>builder()
 *     .maxSize(1000)
 *     .expireAfterWrite(Duration.ofMinutes(5))
 *     .build("l1-users");
 *
 * Cache<String, User> l2 = OpenCache.<String, User>builder()
 *     .maxSize(10000)
 *     .expireAfterWrite(Duration.ofHours(1))
 *     .build("l2-users");
 *
 * LayeredCache<String, User> cache = LayeredCache.of(l1, l2);
 *
 * // Get - checks L1, then L2, promotes to L1 if found in L2
 * User user = cache.get("user:1001");
 *
 * // Put - writes to both L1 and L2
 * cache.put("user:1001", user);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>L1 hit: O(1) - L1命中: O(1)</li>
 *   <li>L2 hit: O(1) + promotion cost - L2命中: O(1) + 提升成本</li>
 *   <li>Miss: L1 miss + L2 miss + optional load - 未命中: L1未命中 + L2未命中 + 可选加载</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (null values not allowed) - 空值安全: 部分（不允许null值）</li>
 * </ul>
 *
 * @param <K> the type of keys | 键类型
 * @param <V> the type of values | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 * @deprecated Use {@link cloud.opencode.base.cache.multilevel.MultiLevelCache} instead.
 * This class will be removed in a future version.
 * 已废弃，请使用 {@link cloud.opencode.base.cache.multilevel.MultiLevelCache} 替代。
 * 此类将在未来版本中移除。
 */
@Deprecated(since = "1.0.3", forRemoval = true)
public class LayeredCache<K, V> implements Cache<K, V> {

    private final Cache<K, V> l1;
    private final Cache<K, V> l2;
    private final WriteStrategy writeStrategy;
    private final boolean promoteOnL2Hit;
    private final String name;

    /**
     * Write strategy for layered cache
     * 分层缓存的写策略
     */
    public enum WriteStrategy {
        /**
         * Write to both L1 and L2 on put
         * 放入时同时写入L1和L2
         */
        WRITE_THROUGH,

        /**
         * Write to L1 only, L2 updated on eviction
         * 仅写入L1，驱逐时更新L2
         */
        WRITE_BACK,

        /**
         * Write to L1 only, L2 is read-only
         * 仅写入L1，L2只读
         */
        L1_ONLY
    }

    private LayeredCache(Cache<K, V> l1, Cache<K, V> l2, WriteStrategy writeStrategy,
                         boolean promoteOnL2Hit, String name) {
        this.l1 = Objects.requireNonNull(l1, "L1 cache must not be null");
        this.l2 = Objects.requireNonNull(l2, "L2 cache must not be null");
        this.writeStrategy = writeStrategy;
        this.promoteOnL2Hit = promoteOnL2Hit;
        this.name = name != null ? name : "layered-" + l1.name() + "-" + l2.name();
    }

    /**
     * Creates a layered cache with default settings
     * 使用默认设置创建分层缓存
     *
     * @param l1   L1 cache (fast, small) | L1缓存（快速、小）
     * @param l2   L2 cache (slower, large) | L2缓存（较慢、大）
     * @param <K>  key type | 键类型
     * @param <V>  value type | 值类型
     * @return layered cache | 分层缓存
     */
    public static <K, V> LayeredCache<K, V> of(Cache<K, V> l1, Cache<K, V> l2) {
        return builder(l1, l2).build();
    }

    /**
     * Creates a builder for layered cache
     * 创建分层缓存构建器
     *
     * @param l1   L1 cache | L1缓存
     * @param l2   L2 cache | L2缓存
     * @param <K>  key type | 键类型
     * @param <V>  value type | 值类型
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> builder(Cache<K, V> l1, Cache<K, V> l2) {
        return new Builder<>(l1, l2);
    }

    /**
     * Builder for LayeredCache
     * LayeredCache构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    public static class Builder<K, V> {
        private final Cache<K, V> l1;
        private final Cache<K, V> l2;
        private WriteStrategy writeStrategy = WriteStrategy.WRITE_THROUGH;
        private boolean promoteOnL2Hit = true;
        private String name;

        private Builder(Cache<K, V> l1, Cache<K, V> l2) {
            this.l1 = l1;
            this.l2 = l2;
        }

        /**
         * Sets the write strategy
         * 设置写策略
         *
         * @param strategy write strategy | 写策略
         * @return this builder | 此构建器
         */
        public Builder<K, V> writeStrategy(WriteStrategy strategy) {
            this.writeStrategy = strategy;
            return this;
        }

        /**
         * Sets whether to promote to L1 on L2 hit
         * 设置是否在L2命中时提升到L1
         *
         * @param promote true to promote | true表示提升
         * @return this builder | 此构建器
         */
        public Builder<K, V> promoteOnL2Hit(boolean promote) {
            this.promoteOnL2Hit = promote;
            return this;
        }

        /**
         * Sets the cache name
         * 设置缓存名称
         *
         * @param name cache name | 缓存名称
         * @return this builder | 此构建器
         */
        public Builder<K, V> name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Builds the layered cache
         * 构建分层缓存
         *
         * @return layered cache | 分层缓存
         */
        public LayeredCache<K, V> build() {
            return new LayeredCache<>(l1, l2, writeStrategy, promoteOnL2Hit, name);
        }
    }

    // ==================== Basic Operations | 基本操作 ====================

    @Override
    public V get(K key) {
        // Check L1 first
        V value = l1.get(key);
        if (value != null) {
            return value;
        }

        // Check L2
        value = l2.get(key);
        if (value != null && promoteOnL2Hit) {
            // Promote to L1
            l1.put(key, value);
        }
        return value;
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> loader) {
        // Check L1 first
        V value = l1.get(key);
        if (value != null) {
            return value;
        }

        // Check L2
        value = l2.get(key);
        if (value != null) {
            if (promoteOnL2Hit) {
                l1.put(key, value);
            }
            return value;
        }

        // Load from source
        value = loader.apply(key);
        if (value != null) {
            put(key, value);
        }
        return value;
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys) {
        Map<K, V> result = new HashMap<>();
        Set<K> missingFromL1 = new HashSet<>();

        // First, get from L1
        for (K key : keys) {
            V value = l1.get(key);
            if (value != null) {
                result.put(key, value);
            } else {
                missingFromL1.add(key);
            }
        }

        // Then, get missing from L2
        if (!missingFromL1.isEmpty()) {
            Map<K, V> l2Results = l2.getAll(missingFromL1);
            result.putAll(l2Results);

            // Promote to L1
            if (promoteOnL2Hit && !l2Results.isEmpty()) {
                l1.putAll(l2Results);
            }
        }

        return result;
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys,
                           Function<? super Set<? extends K>, ? extends Map<K, V>> loader) {
        Map<K, V> result = new HashMap<>();
        Set<K> missingFromL1 = new HashSet<>();

        // First, get from L1
        for (K key : keys) {
            V value = l1.get(key);
            if (value != null) {
                result.put(key, value);
            } else {
                missingFromL1.add(key);
            }
        }

        // Then, get missing from L2
        Set<K> missingFromL2 = new HashSet<>();
        if (!missingFromL1.isEmpty()) {
            Map<K, V> l2Results = l2.getAll(missingFromL1);
            result.putAll(l2Results);

            if (promoteOnL2Hit && !l2Results.isEmpty()) {
                l1.putAll(l2Results);
            }

            // Determine what's still missing
            for (K key : missingFromL1) {
                if (!l2Results.containsKey(key)) {
                    missingFromL2.add(key);
                }
            }
        }

        // Load missing from source
        if (!missingFromL2.isEmpty()) {
            Map<K, V> loaded = loader.apply(missingFromL2);
            if (loaded != null && !loaded.isEmpty()) {
                result.putAll(loaded);
                putAll(loaded);
            }
        }

        return result;
    }

    @Override
    public void put(K key, V value) {
        switch (writeStrategy) {
            case WRITE_THROUGH -> {
                l1.put(key, value);
                l2.put(key, value);
            }
            case WRITE_BACK, L1_ONLY -> l1.put(key, value);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        switch (writeStrategy) {
            case WRITE_THROUGH -> {
                l1.putAll(map);
                l2.putAll(map);
            }
            case WRITE_BACK, L1_ONLY -> l1.putAll(map);
        }
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        boolean result = l1.putIfAbsent(key, value);
        if (result && writeStrategy == WriteStrategy.WRITE_THROUGH) {
            l2.putIfAbsent(key, value);
        }
        return result;
    }

    @Override
    public void putWithTtl(K key, V value, java.time.Duration ttl) {
        switch (writeStrategy) {
            case WRITE_THROUGH -> {
                l1.putWithTtl(key, value, ttl);
                l2.putWithTtl(key, value, ttl);
            }
            case WRITE_BACK, L1_ONLY -> l1.putWithTtl(key, value, ttl);
        }
    }

    @Override
    public void putAllWithTtl(Map<? extends K, ? extends V> map, java.time.Duration ttl) {
        switch (writeStrategy) {
            case WRITE_THROUGH -> {
                l1.putAllWithTtl(map, ttl);
                l2.putAllWithTtl(map, ttl);
            }
            case WRITE_BACK, L1_ONLY -> l1.putAllWithTtl(map, ttl);
        }
    }

    @Override
    public boolean putIfAbsentWithTtl(K key, V value, java.time.Duration ttl) {
        boolean result = l1.putIfAbsentWithTtl(key, value, ttl);
        if (result && writeStrategy == WriteStrategy.WRITE_THROUGH) {
            l2.putIfAbsentWithTtl(key, value, ttl);
        }
        return result;
    }

    // ==================== Invalidation | 失效操作 ====================

    @Override
    public void invalidate(K key) {
        l1.invalidate(key);
        if (writeStrategy != WriteStrategy.L1_ONLY) {
            l2.invalidate(key);
        }
    }

    @Override
    public void invalidateAll(Iterable<? extends K> keys) {
        l1.invalidateAll(keys);
        if (writeStrategy != WriteStrategy.L1_ONLY) {
            l2.invalidateAll(keys);
        }
    }

    @Override
    public void invalidateAll() {
        l1.invalidateAll();
        if (writeStrategy != WriteStrategy.L1_ONLY) {
            l2.invalidateAll();
        }
    }

    // ==================== Query Operations | 查询操作 ====================

    @Override
    public boolean containsKey(K key) {
        return l1.containsKey(key) || l2.containsKey(key);
    }

    @Override
    public long size() {
        // Return unique key count by computing the union of keys
        return keys().size();
    }

    @Override
    public long estimatedSize() {
        // Approximate unique size: use keys() union for accuracy
        return keys().size();
    }

    @Override
    public Set<K> keys() {
        Set<K> allKeys = new HashSet<>(l1.keys());
        allKeys.addAll(l2.keys());
        return Collections.unmodifiableSet(allKeys);
    }

    @Override
    public Collection<V> values() {
        // Get all unique values
        Map<K, V> allEntries = new HashMap<>();
        for (Map.Entry<K, V> entry : l1.entries()) {
            allEntries.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<K, V> entry : l2.entries()) {
            allEntries.putIfAbsent(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableCollection(allEntries.values());
    }

    @Override
    public Set<Map.Entry<K, V>> entries() {
        Map<K, V> allEntries = new HashMap<>();
        for (Map.Entry<K, V> entry : l2.entries()) {
            allEntries.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<K, V> entry : l1.entries()) {
            allEntries.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableSet(allEntries.entrySet());
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        ConcurrentMap<K, V> map = new ConcurrentHashMap<>();
        map.putAll(l2.asMap());
        map.putAll(l1.asMap());
        return map;
    }

    // ==================== Statistics & Management | 统计与管理 ====================

    @Override
    public CacheStats stats() {
        CacheStats l1Stats = l1.stats();
        CacheStats l2Stats = l2.stats();

        // Combine statistics
        return CacheStats.of(
            l1Stats.hitCount() + l2Stats.hitCount(),
            l1Stats.missCount(),  // Only L1 misses matter for total
            l1Stats.loadSuccessCount() + l2Stats.loadSuccessCount(),
            l1Stats.loadFailureCount() + l2Stats.loadFailureCount(),
            l1Stats.totalLoadTime() + l2Stats.totalLoadTime(),
            l1Stats.evictionCount() + l2Stats.evictionCount(),
            l1Stats.evictionWeight() + l2Stats.evictionWeight()
        );
    }

    @Override
    public void cleanUp() {
        l1.cleanUp();
        l2.cleanUp();
    }

    @Override
    public AsyncCache<K, V> async() {
        // Return async view that delegates to this layered cache
        return new LayeredAsyncCache<>(this);
    }

    @Override
    public String name() {
        return name;
    }

    // ==================== Layer Access | 层访问 ====================

    /**
     * Gets the L1 cache
     * 获取L1缓存
     *
     * @return L1 cache | L1缓存
     */
    public Cache<K, V> l1() {
        return l1;
    }

    /**
     * Gets the L2 cache
     * 获取L2缓存
     *
     * @return L2 cache | L2缓存
     */
    public Cache<K, V> l2() {
        return l2;
    }

    /**
     * Gets the write strategy
     * 获取写策略
     *
     * @return write strategy | 写策略
     */
    public WriteStrategy writeStrategy() {
        return writeStrategy;
    }

    /**
     * Checks if promotion on L2 hit is enabled
     * 检查是否启用L2命中时提升
     *
     * @return true if enabled | 如果启用则为true
     */
    public boolean isPromoteOnL2Hit() {
        return promoteOnL2Hit;
    }

    /**
     * Flushes L1 to L2 (for write-back strategy)
     * 将L1刷新到L2（用于写回策略）
     *
     * @throws RuntimeException if flush fails | 刷新失败时抛出运行时异常
     */
    public void flush() {
        if (writeStrategy == WriteStrategy.WRITE_BACK) {
            try {
                l2.putAll(l1.asMap());
            } catch (Exception e) {
                throw new RuntimeException("Failed to flush L1 to L2 cache '" + name + "': " + e.getMessage(), e);
            }
        }
    }

    /**
     * Warms up L1 from L2 with specified keys
     * 使用指定键从L2预热L1
     *
     * @param keys keys to warm up | 要预热的键
     */
    public void warmUp(Iterable<? extends K> keys) {
        Map<K, V> l2Values = l2.getAll(keys);
        l1.putAll(l2Values);
    }

    /**
     * Gets combined statistics for both layers
     * 获取两层的组合统计信息
     *
     * @return combined stats | 组合统计
     */
    public LayeredCacheStats layeredStats() {
        return new LayeredCacheStats(l1.stats(), l2.stats());
    }

    /**
     * Layered cache statistics
     * 分层缓存统计
     *
     * @param l1Stats L1 statistics | L1统计
     * @param l2Stats L2 statistics | L2统计
     */
    public record LayeredCacheStats(CacheStats l1Stats, CacheStats l2Stats) {

        /**
         * Gets L1 hit rate
         * 获取L1命中率
         *
         * @return L1 hit rate | L1命中率
         */
        public double l1HitRate() {
            return l1Stats.hitRate();
        }

        /**
         * Gets L2 hit rate
         * 获取L2命中率
         *
         * @return L2 hit rate | L2命中率
         */
        public double l2HitRate() {
            return l2Stats.hitRate();
        }

        /**
         * Gets overall hit rate
         * 获取总体命中率
         *
         * @return overall hit rate | 总体命中率
         */
        public double overallHitRate() {
            long totalRequests = l1Stats.requestCount();
            if (totalRequests == 0) {
                return 0.0;
            }
            long l1Hits = l1Stats.hitCount();
            long l2Hits = l2Stats.hitCount();
            return (double) (l1Hits + l2Hits) / totalRequests;
        }
    }

    /**
     * Async cache wrapper for layered cache
     */
    private static class LayeredAsyncCache<K, V> implements AsyncCache<K, V> {
        private final LayeredCache<K, V> cache;

        LayeredAsyncCache(LayeredCache<K, V> cache) {
            this.cache = cache;
        }

        @Override
        public java.util.concurrent.CompletableFuture<V> getAsync(K key) {
            return java.util.concurrent.CompletableFuture.supplyAsync(() -> cache.get(key));
        }

        @Override
        public java.util.concurrent.CompletableFuture<V> getAsync(K key,
                java.util.function.BiFunction<? super K, ? super java.util.concurrent.Executor,
                        ? extends java.util.concurrent.CompletableFuture<V>> loader) {
            return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                V value = cache.get(key);
                if (value != null) {
                    return value;
                }
                return loader.apply(key, java.util.concurrent.ForkJoinPool.commonPool()).join();
            });
        }

        @Override
        public java.util.concurrent.CompletableFuture<Map<K, V>> getAllAsync(Iterable<? extends K> keys) {
            return java.util.concurrent.CompletableFuture.supplyAsync(() -> cache.getAll(keys));
        }

        @Override
        public java.util.concurrent.CompletableFuture<Void> putAsync(K key, V value) {
            return java.util.concurrent.CompletableFuture.runAsync(() -> cache.put(key, value));
        }

        @Override
        public java.util.concurrent.CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> map) {
            return java.util.concurrent.CompletableFuture.runAsync(() -> cache.putAll(map));
        }

        @Override
        public java.util.concurrent.CompletableFuture<Void> invalidateAsync(K key) {
            return java.util.concurrent.CompletableFuture.runAsync(() -> cache.invalidate(key));
        }

        @Override
        public java.util.concurrent.CompletableFuture<Void> invalidateAllAsync(Iterable<? extends K> keys) {
            return java.util.concurrent.CompletableFuture.runAsync(() -> cache.invalidateAll(keys));
        }

        @Override
        public Cache<K, V> sync() {
            return cache;
        }
    }
}
