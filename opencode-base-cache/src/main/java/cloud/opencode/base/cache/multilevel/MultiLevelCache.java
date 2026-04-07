package cloud.opencode.base.cache.multilevel;

import cloud.opencode.base.cache.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Multi-Level Cache - Supports 3+ levels with flexible configuration
 * 多级缓存 - 支持 3 个以上级别的灵活配置
 *
 * <p>A flexible multi-level cache implementation that supports arbitrary
 * number of cache levels with individual configurations.</p>
 * <p>灵活的多级缓存实现，支持任意数量的缓存级别，各级别可独立配置。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unlimited cache levels - 无限缓存级别</li>
 *   <li>Per-level TTL - 每级 TTL</li>
 *   <li>Automatic promotion - 自动提升</li>
 *   <li>Spillover handling - 溢出处理</li>
 *   <li>Level-specific metrics - 级别特定指标</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create 3-level cache: L1 (hot) -> L2 (warm) -> L3 (cold/remote)
 * MultiLevelCache<String, User> cache = MultiLevelCache.<String, User>builder()
 *     .level(LevelConfig.<String, User>builder()
 *         .name("L1-hot")
 *         .cache(hotCache)
 *         .ttl(Duration.ofMinutes(5))
 *         .promoteOnHit(true)
 *         .build())
 *     .level(LevelConfig.<String, User>builder()
 *         .name("L2-warm")
 *         .cache(warmCache)
 *         .ttl(Duration.ofMinutes(30))
 *         .promoteOnHit(true)
 *         .build())
 *     .level(LevelConfig.<String, User>builder()
 *         .name("L3-cold")
 *         .cache(coldCache)
 *         .ttl(Duration.ofHours(24))
 *         .build())
 *     .build();
 *
 * // Use like normal cache
 * User user = cache.get("user:1001");  // Checks L1, L2, L3 in order
 * }</pre>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (null values not allowed) - 空值安全: 部分（不允许 null 值）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.5
 */
public class MultiLevelCache<K, V> implements Cache<K, V> {

    private final List<CacheLevel<K, V>> levels;
    private final String name;
    private final WritePolicy writePolicy;
    private final InvalidationPolicy invalidationPolicy;

    // Metrics per level
    private final List<LevelMetrics> levelMetrics;

    private MultiLevelCache(String name, List<LevelConfig<K, V>> levelConfigs,
                           WritePolicy writePolicy, InvalidationPolicy invalidationPolicy) {
        this.name = name;
        this.writePolicy = writePolicy;
        this.invalidationPolicy = invalidationPolicy;

        this.levels = new ArrayList<>();
        this.levelMetrics = new ArrayList<>();

        for (int i = 0; i < levelConfigs.size(); i++) {
            LevelConfig<K, V> config = levelConfigs.get(i);
            levels.add(new CacheLevel<>(i, config));
            levelMetrics.add(new LevelMetrics(config.name()));
        }
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a builder
     * 创建构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    /**
     * Creates a two-level cache as a convenient replacement for
     * {@link cloud.opencode.base.cache.LayeredCache}.
     * 创建两级缓存，作为 {@link cloud.opencode.base.cache.LayeredCache} 的便捷替代。
     *
     * <p>The returned cache uses {@link WritePolicy#WRITE_ALL} and
     * {@link InvalidationPolicy#INVALIDATE_ALL}, with L2 promotion on hit enabled.</p>
     * <p>返回的缓存使用 {@link WritePolicy#WRITE_ALL} 和
     * {@link InvalidationPolicy#INVALIDATE_ALL}，并在 L2 命中时启用提升。</p>
     *
     * @param l1  L1 cache (fast, small) | L1 缓存（快速、小）
     * @param l2  L2 cache (slower, large) | L2 缓存（较慢、大）
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return a two-level multi-level cache | 两级多级缓存
     */
    public static <K, V> MultiLevelCache<K, V> ofTwoLevel(Cache<K, V> l1, Cache<K, V> l2) {
        return MultiLevelCache.<K, V>builder()
                .name("two-level-" + l1.name() + "-" + l2.name())
                .level(LevelConfig.<K, V>builder()
                        .name("L1")
                        .cache(l1)
                        .promoteOnHit(false)
                        .build())
                .level(LevelConfig.<K, V>builder()
                        .name("L2")
                        .cache(l2)
                        .promoteOnHit(true)
                        .build())
                .writePolicy(WritePolicy.WRITE_ALL)
                .invalidationPolicy(InvalidationPolicy.INVALIDATE_ALL)
                .build();
    }

    // ==================== Cache Operations | 缓存操作 ====================

    @Override
    public V get(K key) {
        for (int i = 0; i < levels.size(); i++) {
            CacheLevel<K, V> level = levels.get(i);
            V value = level.get(key);

            if (value != null) {
                levelMetrics.get(i).recordHit();

                // Promote to higher levels if configured
                if (level.config.promoteOnHit() && i > 0) {
                    promoteToLevel(key, value, i - 1);
                }

                return value;
            } else {
                levelMetrics.get(i).recordMiss();
            }
        }
        return null;
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> loader) {
        V value = get(key);
        if (value != null) {
            return value;
        }

        // Load and store in all write levels
        value = loader.apply(key);
        if (value != null) {
            put(key, value);
        }
        return value;
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys) {
        Map<K, V> result = new LinkedHashMap<>();
        Set<K> remaining = new LinkedHashSet<>();

        for (K key : keys) {
            remaining.add(key);
        }

        // Check each level
        for (int i = 0; i < levels.size() && !remaining.isEmpty(); i++) {
            CacheLevel<K, V> level = levels.get(i);
            Map<K, V> found = level.getAll(remaining);

            for (Map.Entry<K, V> entry : found.entrySet()) {
                if (entry.getValue() != null) {
                    result.put(entry.getKey(), entry.getValue());
                    remaining.remove(entry.getKey());
                    levelMetrics.get(i).recordHit();

                    // Promote if configured
                    if (level.config.promoteOnHit() && i > 0) {
                        promoteToLevel(entry.getKey(), entry.getValue(), i - 1);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys,
                           Function<? super Set<? extends K>, ? extends Map<K, V>> loader) {
        Map<K, V> result = new LinkedHashMap<>(getAll(keys));

        Set<K> missing = new LinkedHashSet<>();
        for (K key : keys) {
            if (!result.containsKey(key)) {
                missing.add(key);
            }
        }

        if (!missing.isEmpty()) {
            @SuppressWarnings("unchecked")
            Map<K, V> loaded = loader.apply((Set<? extends K>) missing);
            putAll(loaded);
            result.putAll(loaded);
        }

        return result;
    }

    @Override
    public void put(K key, V value) {
        switch (writePolicy) {
            case WRITE_ALL -> {
                for (CacheLevel<K, V> level : levels) {
                    level.put(key, value);
                }
            }
            case WRITE_FIRST -> {
                if (!levels.isEmpty()) {
                    levels.getFirst().put(key, value);
                }
            }
            case WRITE_THROUGH -> {
                // Write to all levels synchronously
                for (CacheLevel<K, V> level : levels) {
                    level.put(key, value);
                }
            }
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        // Delegate to the primary (first level) cache's putIfAbsent atomically
        if (levels.isEmpty()) {
            return false;
        }
        boolean inserted = levels.getFirst().cache.putIfAbsent(key, value);
        if (!inserted) {
            return false;
        }
        // Propagate to other levels on success
        for (int i = 1; i < levels.size(); i++) {
            levels.get(i).put(key, value);
        }
        return true;
    }

    @Override
    public void putWithTtl(K key, V value, Duration ttl) {
        for (CacheLevel<K, V> level : levels) {
            // Use min of provided TTL and level's configured TTL
            Duration levelTtl = level.config.ttl();
            Duration effectiveTtl = levelTtl != null && levelTtl.compareTo(ttl) < 0 ? levelTtl : ttl;
            level.putWithTtl(key, value, effectiveTtl);
        }
    }

    @Override
    public void putAllWithTtl(Map<? extends K, ? extends V> map, Duration ttl) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            putWithTtl(entry.getKey(), entry.getValue(), ttl);
        }
    }

    @Override
    public boolean putIfAbsentWithTtl(K key, V value, Duration ttl) {
        // Delegate to the primary (first level) cache's putIfAbsentWithTtl atomically
        if (levels.isEmpty()) {
            return false;
        }
        Duration levelTtl = levels.getFirst().config.ttl();
        Duration effectiveTtl = levelTtl != null && levelTtl.compareTo(ttl) < 0 ? levelTtl : ttl;
        boolean inserted = levels.getFirst().cache.putIfAbsentWithTtl(key, value, effectiveTtl);
        if (!inserted) {
            return false;
        }
        // Propagate to other levels on success
        for (int i = 1; i < levels.size(); i++) {
            CacheLevel<K, V> level = levels.get(i);
            Duration lTtl = level.config.ttl();
            Duration eTtl = lTtl != null && lTtl.compareTo(ttl) < 0 ? lTtl : ttl;
            level.putWithTtl(key, value, eTtl);
        }
        return true;
    }

    @Override
    public void invalidate(K key) {
        switch (invalidationPolicy) {
            case INVALIDATE_ALL -> {
                for (CacheLevel<K, V> level : levels) {
                    level.invalidate(key);
                }
            }
            case INVALIDATE_FIRST -> {
                if (!levels.isEmpty()) {
                    levels.getFirst().invalidate(key);
                }
            }
            case CASCADE_DOWN -> {
                for (CacheLevel<K, V> level : levels) {
                    level.invalidate(key);
                }
            }
        }
    }

    @Override
    public void invalidateAll(Iterable<? extends K> keys) {
        for (K key : keys) {
            invalidate(key);
        }
    }

    @Override
    public void invalidateAll() {
        for (CacheLevel<K, V> level : levels) {
            level.invalidateAll();
        }
    }

    @Override
    public boolean containsKey(K key) {
        for (CacheLevel<K, V> level : levels) {
            if (level.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long size() {
        // Return size of first level (most accurate)
        return levels.isEmpty() ? 0 : levels.getFirst().size();
    }

    @Override
    public long estimatedSize() {
        return levels.stream().mapToLong(CacheLevel::estimatedSize).sum();
    }

    @Override
    public Set<K> keys() {
        Set<K> allKeys = new LinkedHashSet<>();
        for (CacheLevel<K, V> level : levels) {
            allKeys.addAll(level.keys());
        }
        return allKeys;
    }

    @Override
    public Collection<V> values() {
        // Return from first level
        return levels.isEmpty() ? List.of() : levels.getFirst().values();
    }

    @Override
    public Set<Map.Entry<K, V>> entries() {
        return levels.isEmpty() ? Set.of() : levels.getFirst().entries();
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        return levels.isEmpty() ? new java.util.concurrent.ConcurrentHashMap<>() : levels.getFirst().asMap();
    }

    @Override
    public CacheStats stats() {
        // Aggregate stats from all levels
        return levels.isEmpty() ? CacheStats.empty() : levels.getFirst().stats();
    }

    @Override
    public CacheMetrics metrics() {
        // Return metrics from the first level, or a fresh empty metrics instance
        // to avoid callers getting NPE when calling methods on the returned value
        if (levels.isEmpty()) {
            return CacheMetrics.create();
        }
        CacheMetrics m = levels.getFirst().metrics();
        return m != null ? m : CacheMetrics.create();
    }

    @Override
    public void cleanUp() {
        for (CacheLevel<K, V> level : levels) {
            level.cleanUp();
        }
    }

    @Override
    public AsyncCache<K, V> async() {
        return levels.isEmpty() ? null : levels.getFirst().async();
    }

    @Override
    public String name() {
        return name;
    }

    // ==================== Level Operations | 级别操作 ====================

    private void promoteToLevel(K key, V value, int targetLevel) {
        if (targetLevel >= 0 && targetLevel < levels.size()) {
            CacheLevel<K, V> level = levels.get(targetLevel);
            level.put(key, value);
            levelMetrics.get(targetLevel).recordPromotion();
        }
    }

    /**
     * Get number of cache levels
     * 获取缓存级别数
     *
     * @return level count | 级别数
     */
    public int levelCount() {
        return levels.size();
    }

    /**
     * Get cache at specific level
     * 获取特定级别的缓存
     *
     * @param level level index (0-based) | 级别索引（从 0 开始）
     * @return cache at level | 该级别的缓存
     */
    public Cache<K, V> getLevel(int level) {
        return levels.get(level).cache;
    }

    /**
     * Get metrics for all levels
     * 获取所有级别的指标
     *
     * @return list of level metrics | 级别指标列表
     */
    public List<LevelMetrics> getLevelMetrics() {
        return new ArrayList<>(levelMetrics);
    }

    /**
     * Get aggregate statistics
     * 获取聚合统计
     *
     * @return aggregate stats | 聚合统计
     */
    public MultiLevelStats getMultiLevelStats() {
        long totalHits = levelMetrics.stream().mapToLong(m -> m.hits.get()).sum();
        long totalMisses = levelMetrics.stream().mapToLong(m -> m.misses.get()).sum();
        long totalPromotions = levelMetrics.stream().mapToLong(m -> m.promotions.get()).sum();

        Map<String, LevelMetrics.Snapshot> levelSnapshots = new LinkedHashMap<>();
        for (LevelMetrics lm : levelMetrics) {
            levelSnapshots.put(lm.levelName, lm.snapshot());
        }

        return new MultiLevelStats(totalHits, totalMisses, totalPromotions, levelSnapshots);
    }

    // ==================== Inner Classes | 内部类 ====================

    /**
     * Cache level wrapper
     * 缓存级别包装器
     */
    private static class CacheLevel<K, V> {
        final int index;
        final LevelConfig<K, V> config;
        final Cache<K, V> cache;

        CacheLevel(int index, LevelConfig<K, V> config) {
            this.index = index;
            this.config = config;
            this.cache = config.cache();
        }

        V get(K key) {
            return cache.get(key);
        }

        Map<K, V> getAll(Set<K> keys) {
            return cache.getAll(keys);
        }

        void put(K key, V value) {
            if (config.ttl() != null) {
                cache.putWithTtl(key, value, config.ttl());
            } else {
                cache.put(key, value);
            }
        }

        void putWithTtl(K key, V value, Duration ttl) {
            cache.putWithTtl(key, value, ttl);
        }

        void invalidate(K key) {
            cache.invalidate(key);
        }

        void invalidateAll() {
            cache.invalidateAll();
        }

        boolean containsKey(K key) {
            return cache.containsKey(key);
        }

        long size() {
            return cache.size();
        }

        long estimatedSize() {
            return cache.estimatedSize();
        }

        Set<K> keys() {
            return cache.keys();
        }

        Collection<V> values() {
            return cache.values();
        }

        Set<Map.Entry<K, V>> entries() {
            return cache.entries();
        }

        ConcurrentMap<K, V> asMap() {
            return cache.asMap();
        }

        CacheStats stats() {
            return cache.stats();
        }

        CacheMetrics metrics() {
            return cache.metrics();
        }

        void cleanUp() {
            cache.cleanUp();
        }

        AsyncCache<K, V> async() {
            return cache.async();
        }
    }

    /**
     * Level configuration
     * 级别配置
     *
     * @param name the level name | 级别名称
     * @param cache the cache instance for this level | 此级别的缓存实例
     * @param ttl the time-to-live for this level | 此级别的过期时间
     * @param promoteOnHit whether to promote entries on hit | 命中时是否提升条目
     * @param writeEnabled whether writes are enabled for this level | 此级别是否启用写入
     * @param <K> the key type | 键类型
     * @param <V> the value type | 值类型
     */
    public record LevelConfig<K, V>(
            String name,
            Cache<K, V> cache,
            Duration ttl,
            boolean promoteOnHit,
            boolean writeEnabled
    ) {
        /**
         * Creates a new level config builder | 创建新的级别配置构建器
         *
         * @param <K> the key type | 键类型
         * @param <V> the value type | 值类型
         * @return a new builder | 新的构建器
         */
        public static <K, V> LevelConfigBuilder<K, V> builder() {
            return new LevelConfigBuilder<>();
        }
    }

    /**
     * Level configuration builder
     * 级别配置构建器
     *
     * @param <K> the key type | 键类型
     * @param <V> the value type | 值类型
     */
    public static class LevelConfigBuilder<K, V> {

        /** Creates a new LevelConfigBuilder instance | 创建新的 LevelConfigBuilder 实例 */
        public LevelConfigBuilder() {}
        private String name = "unnamed";
        private Cache<K, V> cache;
        private Duration ttl;
        private boolean promoteOnHit = false;
        private boolean writeEnabled = true;

        /**
         * name | name
         * @param name the name | name
         * @return the result | 结果
         */
        public LevelConfigBuilder<K, V> name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the cache for this level | 设置此级别的缓存
         *
         * @param cache the cache instance | 缓存实例
         * @return this builder | 此构建器
         */
        public LevelConfigBuilder<K, V> cache(Cache<K, V> cache) {
            this.cache = cache;
            return this;
        }

        /**
         * ttl | ttl
         * @param ttl the ttl | ttl
         * @return the result | 结果
         */
        public LevelConfigBuilder<K, V> ttl(Duration ttl) {
            this.ttl = ttl;
            return this;
        }

        /**
         * promoteOnHit | promoteOnHit
         * @param promote the promote | promote
         * @return the result | 结果
         */
        public LevelConfigBuilder<K, V> promoteOnHit(boolean promote) {
            this.promoteOnHit = promote;
            return this;
        }

        /**
         * writeEnabled | writeEnabled
         * @param enabled the enabled | enabled
         * @return the result | 结果
         */
        public LevelConfigBuilder<K, V> writeEnabled(boolean enabled) {
            this.writeEnabled = enabled;
            return this;
        }

        /**
         * build | build
         * @return the result | 结果
         */
        public LevelConfig<K, V> build() {
            Objects.requireNonNull(cache, "cache cannot be null");
            return new LevelConfig<>(name, cache, ttl, promoteOnHit, writeEnabled);
        }
    }

    /**
     * Level metrics
     * 级别指标
     */
    public static class LevelMetrics {
        private final String levelName;
        private final AtomicLong hits = new AtomicLong(0);
        private final AtomicLong misses = new AtomicLong(0);
        private final AtomicLong promotions = new AtomicLong(0);

        LevelMetrics(String levelName) {
            this.levelName = levelName;
        }

        void recordHit() { hits.incrementAndGet(); }
        void recordMiss() { misses.incrementAndGet(); }
        void recordPromotion() { promotions.incrementAndGet(); }

        /**
         * snapshot | snapshot
         * @return the result | 结果
         */
        public Snapshot snapshot() {
            return new Snapshot(levelName, hits.get(), misses.get(), promotions.get());
        }

        /**
         * Level metrics snapshot
         * 级别指标快照
         *
         * @param levelName the level name | 级别名称
         * @param hits the hit count | 命中数
         * @param misses the miss count | 未命中数
         * @param promotions the promotion count | 提升数
         */
        public record Snapshot(String levelName, long hits, long misses, long promotions) {
            /**
             * hitRate | hitRate
             * @return the result | 结果
             */
            public double hitRate() {
                long total = hits + misses;
                return total > 0 ? (double) hits / total : 0;
            }
        }
    }

    /**
     * Multi-level statistics
     * 多级统计
     *
     * @param totalHits the total number of hits across all levels | 所有级别的总命中数
     * @param totalMisses the total number of misses | 总未命中数
     * @param totalPromotions the total number of promotions | 总提升数
     * @param levelStats per-level statistics snapshots | 每级统计快照
     */
    public record MultiLevelStats(
            long totalHits,
            long totalMisses,
            long totalPromotions,
            Map<String, LevelMetrics.Snapshot> levelStats
    ) {
        /**
         * overallHitRate | overallHitRate
         * @return the result | 结果
         */
        public double overallHitRate() {
            long total = totalHits + totalMisses;
            return total > 0 ? (double) totalHits / total : 0;
        }
    }

    /**
     * Write policy enumeration
     * 写策略枚举
     */
    public enum WritePolicy {
        /** Write to all levels */
        WRITE_ALL,
        /** Write only to first level */
        WRITE_FIRST,
        /** Write through all levels synchronously */
        WRITE_THROUGH
    }

    /**
     * Invalidation policy enumeration
     * 失效策略枚举
     */
    public enum InvalidationPolicy {
        /** Invalidate from all levels */
        INVALIDATE_ALL,
        /** Invalidate only from first level */
        INVALIDATE_FIRST,
        /** Cascade invalidation down through levels */
        CASCADE_DOWN
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for MultiLevelCache
     * MultiLevelCache 构建器
     *
     * @param <K> the key type | 键类型
     * @param <V> the value type | 值类型
     */
    public static class Builder<K, V> {

        /** Creates a new Builder instance | 创建新的 Builder 实例 */
        public Builder() {}
        private String name = "multi-level-cache";
        private final List<LevelConfig<K, V>> levels = new ArrayList<>();
        private WritePolicy writePolicy = WritePolicy.WRITE_ALL;
        private InvalidationPolicy invalidationPolicy = InvalidationPolicy.INVALIDATE_ALL;

        /**
         * name | name
         * @param name the name | name
         * @return the result | 结果
         */
        public Builder<K, V> name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Adds a cache level | 添加缓存级别
         *
         * @param config the level configuration | 级别配置
         * @return this builder | 此构建器
         */
        public Builder<K, V> level(LevelConfig<K, V> config) {
            this.levels.add(config);
            return this;
        }

        /**
         * writePolicy | writePolicy
         * @param policy the policy | policy
         * @return the result | 结果
         */
        public Builder<K, V> writePolicy(WritePolicy policy) {
            this.writePolicy = policy;
            return this;
        }

        /**
         * invalidationPolicy | invalidationPolicy
         * @param policy the policy | policy
         * @return the result | 结果
         */
        public Builder<K, V> invalidationPolicy(InvalidationPolicy policy) {
            this.invalidationPolicy = policy;
            return this;
        }

        /**
         * build | build
         * @return the result | 结果
         */
        public MultiLevelCache<K, V> build() {
            if (levels.isEmpty()) {
                throw new IllegalArgumentException("At least one level is required");
            }
            return new MultiLevelCache<>(name, levels, writePolicy, invalidationPolicy);
        }
    }
}
