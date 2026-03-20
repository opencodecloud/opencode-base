package cloud.opencode.base.cache.internal;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.AsyncCache;
import cloud.opencode.base.cache.CacheMetrics;
import cloud.opencode.base.cache.CacheStats;
import cloud.opencode.base.cache.config.CacheConfig;
import cloud.opencode.base.cache.model.CacheEntry;
import cloud.opencode.base.cache.model.RemovalCause;
import cloud.opencode.base.cache.spi.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Default Cache Implementation - High-performance concurrent local cache
 * 默认缓存实现 - 高性能并发本地缓存
 *
 * <p>Thread-safe local cache implementation based on ConcurrentHashMap.</p>
 * <p>基于 ConcurrentHashMap 的线程安全本地缓存实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-safe operations - 线程安全操作</li>
 *   <li>Configurable eviction policies - 可配置淘汰策略</li>
 *   <li>TTL/TTI expiration - TTL/TTI 过期</li>
 *   <li>Statistics collection - 统计收集</li>
 *   <li>Async API support - 异步 API 支持</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) average - 时间复杂度: 平均 O(1)</li>
 *   <li>Space complexity: O(n) - 空间复杂度: O(n)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (null values not allowed) - 空值安全: 部分（不允许 null 值）</li>
 * </ul>
 *
 * @param <K> the type of keys | 键类型
 * @param <V> the type of values | 值类型
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Created internally by CacheManager or OpenCache
 * CacheConfig<String, User> config = CacheConfig.<String, User>builder()
 *     .maximumSize(10000)
 *     .expireAfterWrite(Duration.ofMinutes(30))
 *     .build();
 * Cache<String, User> cache = new DefaultCache<>("users", config);
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class DefaultCache<K, V> implements Cache<K, V>, AutoCloseable {

    private final String name;
    private final ConcurrentHashMap<K, ValueHolder<V>> store;
    private final CacheConfig<K, V> config;
    private final StatsCounter statsCounter;
    private final EvictionPolicy<K, V> evictionPolicy;
    private final Executor executor;
    private final boolean ownsExecutor; // true if we created the executor (need to shut it down)
    private final DefaultAsyncCache<K, V> asyncView;
    private final CacheMetrics cacheMetrics;

    // For expiration tracking - using single map with holder for atomicity
    private final ConcurrentHashMap<K, ExpirationInfo> expirationInfo = new ConcurrentHashMap<>();

    // Background cleanup scheduler
    private final java.util.concurrent.ScheduledExecutorService cleanupScheduler;
    private static final long CLEANUP_INTERVAL_MS = 60_000; // 1 minute

    /**
     * Expiration info holder for atomic operations
     * 过期信息持有者，用于原子操作
     */
    private record ExpirationInfo(long expirationTime, long customTtlMs, long creationTime) {
        static ExpirationInfo of(long expirationTime) {
            return new ExpirationInfo(expirationTime, -1, System.currentTimeMillis());
        }

        static ExpirationInfo ofWithCreation(long expirationTime, long creationTime) {
            return new ExpirationInfo(expirationTime, -1, creationTime);
        }

        static ExpirationInfo ofCustomTtl(long expirationTime, long customTtlMs) {
            return new ExpirationInfo(expirationTime, customTtlMs, System.currentTimeMillis());
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }

        boolean hasCustomTtl() {
            return customTtlMs > 0;
        }
    }

    /**
     * DefaultCache | DefaultCache
     * @param name the name | name
     * @param CacheConfig<K the CacheConfig<K | CacheConfig<K
     * @param config the config | config
     */
    public DefaultCache(String name, CacheConfig<K, V> config) {
        this.name = name;
        this.config = config;
        this.store = new ConcurrentHashMap<>(config.initialCapacity(), 0.75f, config.concurrencyLevel());
        this.statsCounter = config.recordStats() ? StatsCounter.concurrent() : StatsCounter.disabled();
        this.evictionPolicy = config.evictionPolicy() != null ? config.evictionPolicy() : EvictionPolicy.lru();
        this.ownsExecutor = config.executor() == null && config.useVirtualThreads();
        this.executor = createExecutor(config);
        this.asyncView = new DefaultAsyncCache<>(this, executor);
        this.cacheMetrics = config.recordStats() ? CacheMetrics.create() : null;

        // Initialize background cleanup scheduler
        this.cleanupScheduler = createCleanupScheduler();
    }

    private java.util.concurrent.ScheduledExecutorService createCleanupScheduler() {
        java.util.concurrent.ScheduledExecutorService scheduler =
                java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = config.useVirtualThreads()
                            ? Thread.ofVirtual().name("cache-cleanup-" + name).unstarted(r)
                            : Thread.ofPlatform().daemon(true).name("cache-cleanup-" + name).unstarted(r);
                    return t;
                });
        scheduler.scheduleWithFixedDelay(
                this::backgroundCleanup,
                CLEANUP_INTERVAL_MS,
                CLEANUP_INTERVAL_MS,
                java.util.concurrent.TimeUnit.MILLISECONDS
        );
        return scheduler;
    }

    private static final System.Logger LOGGER = System.getLogger(DefaultCache.class.getName());

    /**
     * Background cleanup task for expired entries
     * 后台清理任务，用于清理过期条目
     */
    private void backgroundCleanup() {
        try {
            long now = System.currentTimeMillis();
            int cleaned = 0;
            for (Map.Entry<K, ExpirationInfo> entry : expirationInfo.entrySet()) {
                if (entry.getValue().isExpired()) {
                    remove(entry.getKey(), RemovalCause.EXPIRED);
                    cleaned++;
                }
            }
            // Log if significant cleanup occurred
            if (cleaned > 0) {
                LOGGER.log(System.Logger.Level.DEBUG, "Cache cleanup: removed {0} expired entries", cleaned);
            }
        } catch (Exception e) {
            // Log exception but don't propagate to prevent scheduler termination
            LOGGER.log(System.Logger.Level.WARNING, "Cache background cleanup failed: " + e.getMessage(), e);
        }
    }

    private Executor createExecutor(CacheConfig<K, V> config) {
        if (config.executor() != null) {
            return config.executor();
        }
        if (config.useVirtualThreads()) {
            return Executors.newVirtualThreadPerTaskExecutor();
        }
        return ForkJoinPool.commonPool();
    }

    // ==================== Basic Operations ====================

    @Override
    public V get(K key) {
        Objects.requireNonNull(key, "key cannot be null");
        long startTime = cacheMetrics != null ? System.nanoTime() : 0;

        ValueHolder<V> holder = store.get(key);
        if (holder == null) {
            statsCounter.recordMisses(1);
            if (cacheMetrics != null) {
                cacheMetrics.recordGetLatency(System.nanoTime() - startTime);
            }
            return null;
        }

        if (isExpired(key, holder)) {
            remove(key, RemovalCause.EXPIRED);
            statsCounter.recordMisses(1);
            if (cacheMetrics != null) {
                cacheMetrics.recordGetLatency(System.nanoTime() - startTime);
            }
            return null;
        }

        recordAccess(key, holder);
        statsCounter.recordHits(1);
        if (cacheMetrics != null) {
            cacheMetrics.recordGetLatency(System.nanoTime() - startTime);
        }
        return holder.value;
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> loader) {
        V value = get(key);
        if (value != null) {
            return value;
        }

        long startTime = System.nanoTime();
        try {
            value = loader.apply(key);
            long loadTime = System.nanoTime() - startTime;
            if (value != null) {
                put(key, value);
                statsCounter.recordLoadSuccess(loadTime);
                if (cacheMetrics != null) {
                    cacheMetrics.recordLoadLatency(loadTime);
                }
            }
            return value;
        } catch (Exception e) {
            long loadTime = System.nanoTime() - startTime;
            statsCounter.recordLoadFailure(loadTime);
            if (cacheMetrics != null) {
                cacheMetrics.recordLoadLatency(loadTime);
            }
            throw e;
        }
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys) {
        Map<K, V> result = new LinkedHashMap<>();
        for (K key : keys) {
            V value = get(key);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys,
                            Function<? super Set<? extends K>, ? extends Map<K, V>> loader) {
        Map<K, V> result = new LinkedHashMap<>();
        Set<K> missingKeys = new LinkedHashSet<>();

        for (K key : keys) {
            V value = get(key);
            if (value != null) {
                result.put(key, value);
            } else {
                missingKeys.add(key);
            }
        }

        if (!missingKeys.isEmpty()) {
            Map<K, V> loaded = loader.apply(missingKeys);
            putAll(loaded);
            result.putAll(loaded);
        }

        return result;
    }

    @Override
    public void put(K key, V value) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null");

        long startTime = cacheMetrics != null ? System.nanoTime() : 0;

        ensureCapacity();

        ValueHolder<V> oldHolder = store.put(key, new ValueHolder<>(value));
        recordWrite(key, value);
        updateExpiration(key);

        if (oldHolder != null && config.removalListener() != null) {
            notifyRemovalListenerSafely(key, oldHolder.value, RemovalCause.REPLACED);
        }

        if (cacheMetrics != null) {
            cacheMetrics.recordPutLatency(System.nanoTime() - startTime);
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
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null");

        ensureCapacity();

        ValueHolder<V> existing = store.putIfAbsent(key, new ValueHolder<>(value));
        if (existing == null) {
            recordWrite(key, value);
            updateExpiration(key);
            return true;
        }
        return false;
    }

    @Override
    public void putWithTtl(K key, V value, Duration ttl) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null");
        Objects.requireNonNull(ttl, "ttl cannot be null");

        long startTime = cacheMetrics != null ? System.nanoTime() : 0;

        ensureCapacity();

        ValueHolder<V> oldHolder = store.put(key, new ValueHolder<>(value));
        recordWrite(key, value);

        // Set custom TTL for this entry atomically
        expirationInfo.put(key, ExpirationInfo.ofCustomTtl(
                System.currentTimeMillis() + ttl.toMillis(), ttl.toMillis()));

        if (oldHolder != null && config.removalListener() != null) {
            notifyRemovalListenerSafely(key, oldHolder.value, RemovalCause.REPLACED);
        }

        if (cacheMetrics != null) {
            cacheMetrics.recordPutLatency(System.nanoTime() - startTime);
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
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null");
        Objects.requireNonNull(ttl, "ttl cannot be null");

        ensureCapacity();

        ValueHolder<V> existing = store.putIfAbsent(key, new ValueHolder<>(value));
        if (existing == null) {
            recordWrite(key, value);
            expirationInfo.put(key, ExpirationInfo.ofCustomTtl(
                    System.currentTimeMillis() + ttl.toMillis(), ttl.toMillis()));
            return true;
        }
        return false;
    }

    // ==================== Compute Operations ====================

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(remappingFunction, "remappingFunction cannot be null");

        // Use ConcurrentHashMap.compute() for atomic check-and-act to avoid TOCTOU race
        @SuppressWarnings("unchecked")
        V[] result = (V[]) new Object[1];

        store.compute(key, (k, holder) -> {
            if (holder == null || isExpired(k, holder)) {
                if (holder != null) {
                    handleExpiredEntry(k, holder);
                }
                result[0] = null;
                return null;
            }

            V oldValue = holder.value;
            V newValue = remappingFunction.apply(k, oldValue);

            if (newValue == null) {
                handleRemovedEntry(k, oldValue, RemovalCause.EXPLICIT);
                result[0] = null;
                return null;
            }

            // Update with new value
            recordWrite(k, newValue);
            updateExpiration(k);
            if (config.removalListener() != null) {
                notifyRemovalListenerSafely(k, oldValue, RemovalCause.REPLACED);
            }
            result[0] = newValue;
            return new ValueHolder<>(newValue);
        });

        return result[0];
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(remappingFunction, "remappingFunction cannot be null");

        // Use ConcurrentHashMap.compute() for atomic check-and-act to avoid TOCTOU race
        @SuppressWarnings("unchecked")
        V[] result = (V[]) new Object[1];

        store.compute(key, (k, holder) -> {
            V oldValue = null;

            if (holder != null && !isExpired(k, holder)) {
                oldValue = holder.value;
            } else if (holder != null) {
                handleExpiredEntry(k, holder);
            }

            V newValue = remappingFunction.apply(k, oldValue);

            if (newValue == null) {
                if (oldValue != null) {
                    handleRemovedEntry(k, oldValue, RemovalCause.EXPLICIT);
                }
                result[0] = null;
                return null;
            }

            ensureCapacity();
            recordWrite(k, newValue);
            updateExpiration(k);
            if (holder != null && oldValue != null && config.removalListener() != null) {
                notifyRemovalListenerSafely(k, oldValue, RemovalCause.REPLACED);
            }
            result[0] = newValue;
            return new ValueHolder<>(newValue);
        });

        return result[0];
    }

    @Override
    public V getAndRemove(K key) {
        Objects.requireNonNull(key, "key cannot be null");

        // Use atomic remove to avoid TOCTOU race between get and remove
        ValueHolder<V> holder = store.remove(key);
        if (holder == null) {
            return null;
        }

        // Clean up associated state
        expirationInfo.remove(key);
        evictionPolicy.onRemoval(key);

        if (isExpiredHolder(holder, key)) {
            if (config.removalListener() != null) {
                notifyRemovalListenerSafely(key, holder.value, RemovalCause.EXPIRED);
            }
            return null;
        }

        if (config.removalListener() != null) {
            notifyRemovalListenerSafely(key, holder.value, RemovalCause.EXPLICIT);
        }
        return holder.value;
    }

    @Override
    public V replace(K key, V value) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null");

        // Use ConcurrentHashMap.compute() for atomic check-and-replace to avoid TOCTOU race
        @SuppressWarnings("unchecked")
        V[] result = (V[]) new Object[1];

        store.compute(key, (k, holder) -> {
            if (holder == null || isExpired(k, holder)) {
                if (holder != null) {
                    handleExpiredEntry(k, holder);
                }
                result[0] = null;
                return null;
            }

            V oldValue = holder.value;
            recordWrite(k, value);
            updateExpiration(k);
            if (config.removalListener() != null) {
                notifyRemovalListenerSafely(k, oldValue, RemovalCause.REPLACED);
            }
            result[0] = oldValue;
            return new ValueHolder<>(value);
        });

        return result[0];
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(oldValue, "oldValue cannot be null");
        Objects.requireNonNull(newValue, "newValue cannot be null");

        // Use ConcurrentHashMap.compute() for atomic compare-and-replace to avoid TOCTOU race
        boolean[] replaced = new boolean[]{false};

        store.compute(key, (k, holder) -> {
            if (holder == null || isExpired(k, holder)) {
                if (holder != null) {
                    handleExpiredEntry(k, holder);
                }
                replaced[0] = false;
                return null;
            }

            if (Objects.equals(holder.value, oldValue)) {
                recordWrite(k, newValue);
                updateExpiration(k);
                if (config.removalListener() != null) {
                    notifyRemovalListenerSafely(k, holder.value, RemovalCause.REPLACED);
                }
                replaced[0] = true;
                return new ValueHolder<>(newValue);
            }
            replaced[0] = false;
            return holder; // Keep existing value
        });

        return replaced[0];
    }

    // ==================== Invalidation ====================

    @Override
    public void invalidate(K key) {
        remove(key, RemovalCause.EXPLICIT);
    }

    @Override
    public void invalidateAll(Iterable<? extends K> keys) {
        for (K key : keys) {
            invalidate(key);
        }
    }

    @Override
    public void invalidateAll() {
        for (K key : store.keySet()) {
            invalidate(key);
        }
    }

    // ==================== Query Operations ====================

    @Override
    public boolean containsKey(K key) {
        ValueHolder<V> holder = store.get(key);
        if (holder == null) {
            return false;
        }
        if (isExpired(key, holder)) {
            remove(key, RemovalCause.EXPIRED);
            return false;
        }
        return true;
    }

    @Override
    public long size() {
        cleanUp();
        return store.size();
    }

    @Override
    public long estimatedSize() {
        return store.size();
    }

    @Override
    public Set<K> keys() {
        return new LinkedHashSet<>(store.keySet());
    }

    @Override
    public Collection<V> values() {
        List<V> result = new ArrayList<>();
        for (Map.Entry<K, ValueHolder<V>> entry : store.entrySet()) {
            if (!isExpired(entry.getKey(), entry.getValue())) {
                result.add(entry.getValue().value);
            }
        }
        return result;
    }

    @Override
    public Set<Map.Entry<K, V>> entries() {
        Set<Map.Entry<K, V>> result = new LinkedHashSet<>();
        for (Map.Entry<K, ValueHolder<V>> entry : store.entrySet()) {
            if (!isExpired(entry.getKey(), entry.getValue())) {
                result.add(Map.entry(entry.getKey(), entry.getValue().value));
            }
        }
        return result;
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        return new ConcurrentMapView();
    }

    // ==================== Statistics & Management ====================

    @Override
    public CacheStats stats() {
        return statsCounter.snapshot();
    }

    @Override
    public CacheMetrics metrics() {
        return cacheMetrics;
    }

    @Override
    public void resetStats() {
        statsCounter.reset();
        if (cacheMetrics != null) {
            cacheMetrics.reset();
        }
    }

    @Override
    public void cleanUp() {
        long now = System.currentTimeMillis();
        List<K> expiredKeys = new java.util.ArrayList<>();
        for (Map.Entry<K, ExpirationInfo> entry : expirationInfo.entrySet()) {
            if (entry.getValue().expirationTime() <= now) {
                expiredKeys.add(entry.getKey());
            }
        }
        for (K key : expiredKeys) {
            remove(key, RemovalCause.EXPIRED);
        }
    }

    @Override
    public AsyncCache<K, V> async() {
        return asyncView;
    }

    @Override
    public String name() {
        return name;
    }

    // ==================== Private Helper Methods ====================

    /**
     * Handle an expired entry found during a compute/replace operation.
     * Cleans up expiration info, notifies eviction policy, and notifies listeners.
     * 处理在 compute/replace 操作中发现的过期条目。清理过期信息、通知淘汰策略并通知监听器。
     *
     * @param key the key of the expired entry | 过期条目的键
     * @param holder the value holder of the expired entry | 过期条目的值持有者
     */
    private void handleExpiredEntry(K key, ValueHolder<V> holder) {
        expirationInfo.remove(key);
        evictionPolicy.onRemoval(key);
        if (config.removalListener() != null) {
            notifyRemovalListenerSafely(key, holder.value, RemovalCause.EXPIRED);
        }
    }

    /**
     * Handle a removed entry during a compute/replace operation.
     * Cleans up expiration info, notifies eviction policy, and notifies listeners.
     * 处理在 compute/replace 操作中移除的条目。清理过期信息、通知淘汰策略并通知监听器。
     *
     * @param key the key of the removed entry | 移除条目的键
     * @param oldValue the old value of the removed entry | 移除条目的旧值
     * @param cause the removal cause | 移除原因
     */
    private void handleRemovedEntry(K key, V oldValue, RemovalCause cause) {
        expirationInfo.remove(key);
        evictionPolicy.onRemoval(key);
        if (config.removalListener() != null) {
            notifyRemovalListenerSafely(key, oldValue, cause);
        }
    }

    private void remove(K key, RemovalCause cause) {
        ValueHolder<V> removed = store.remove(key);
        expirationInfo.remove(key);
        evictionPolicy.onRemoval(key);

        if (removed != null && config.removalListener() != null) {
            notifyRemovalListenerSafely(key, removed.value, cause);
        }

        if (cause == RemovalCause.SIZE) {
            statsCounter.recordEviction(1);
            if (cacheMetrics != null) {
                cacheMetrics.recordEviction();
            }
        }
    }

    /**
     * Safely notify removal listener with exception handling (V2.0.4)
     * 安全地通知移除监听器，带异常处理
     */
    private void notifyRemovalListenerSafely(K key, V value, RemovalCause cause) {
        try {
            config.removalListener().onRemoval(key, value, cause);
        } catch (Exception e) {
            // Log but don't propagate - listener errors should not affect cache operations
            LOGGER.log(System.Logger.Level.WARNING,
                    "RemovalListener error for key={0}, cause={1}: {2}", key, cause, e.getMessage(), e);
        }
    }

    private void ensureCapacity() {
        if (config.maximumSize() > 0 && store.size() >= config.maximumSize()) {
            evictOne();
        }
    }

    private void evictOne() {
        // Use lazy iterator to avoid creating temporary HashMap
        // This creates CacheEntry on-demand during iteration
        Iterable<Map.Entry<K, CacheEntry<K, V>>> lazyEntries = () -> store.entrySet().stream()
                .map(e -> Map.entry(e.getKey(),
                        new CacheEntry<>(e.getKey(), e.getValue().value,
                                e.getValue().createTime, e.getValue().lastAccessTime,
                                e.getValue().accessCount.get(), 1)))
                .iterator();

        // Create lazy map view that doesn't materialize all entries
        Map<K, CacheEntry<K, V>> lazyMap = new LazyEntryMap<K, V>(lazyEntries, store.size());
        evictionPolicy.selectVictim(lazyMap).ifPresent(key -> remove(key, RemovalCause.SIZE));
    }

    /**
     * Lazy map implementation that iterates on-demand
     * Avoids creating full HashMap for eviction selection
     */
    private static class LazyEntryMap<K, V> extends AbstractMap<K, CacheEntry<K, V>> {
        private final Iterable<Map.Entry<K, CacheEntry<K, V>>> entries;
        private final int knownSize;

        LazyEntryMap(Iterable<Map.Entry<K, CacheEntry<K, V>>> entries, int knownSize) {
            this.entries = entries;
            this.knownSize = knownSize;
        }

        @Override
        public Set<Map.Entry<K, CacheEntry<K, V>>> entrySet() {
            return new AbstractSet<>() {
                @Override
                public Iterator<Map.Entry<K, CacheEntry<K, V>>> iterator() {
                    return entries.iterator();
                }

                @Override
                public int size() {
                    return knownSize;
                }
            };
        }
    }

    private void recordAccess(K key, ValueHolder<V> holder) {
        holder.lastAccessTime = System.currentTimeMillis();
        holder.accessCount.incrementAndGet();

        CacheEntry<K, V> entry = new CacheEntry<>(key, holder.value,
                holder.createTime, holder.lastAccessTime,
                holder.accessCount.get(), 1);
        evictionPolicy.recordAccess(entry);

        // Update TTI expiration if configured (access-only, not a write)
        if (config.expireAfterAccess() != null) {
            updateExpiration(key, false);
        }
    }

    private void recordWrite(K key, V value) {
        CacheEntry<K, V> entry = new CacheEntry<>(key, value);
        evictionPolicy.recordWrite(entry);
    }

    /**
     * Update expiration for a new write (put/replace/compute).
     * Resets creation time to now.
     */
    private void updateExpiration(K key) {
        updateExpiration(key, true);
    }

    /**
     * Update expiration, optionally resetting creation time.
     * @param key the cache key
     * @param isWrite true if this is a write (put/replace), false if access-only (get)
     */
    private void updateExpiration(K key, boolean isWrite) {
        Duration ttl = config.expireAfterWrite();
        Duration tti = config.expireAfterAccess();

        if (ttl == null && tti == null) {
            return;
        }

        long now = System.currentTimeMillis();

        if (ttl != null && tti != null) {
            // When both TTL and TTI are configured:
            // - TTL is absolute from creation time (not reset on access)
            // - TTI is relative to current access time
            // Take the minimum of the two absolute expiration times.
            long creationTime;
            if (isWrite) {
                creationTime = now;
            } else {
                ExpirationInfo existing = expirationInfo.get(key);
                creationTime = (existing != null) ? existing.creationTime() : now;
            }
            long ttlExpiration = creationTime + ttl.toMillis();
            long ttiExpiration = now + tti.toMillis();
            long expirationTime = Math.min(ttlExpiration, ttiExpiration);
            expirationInfo.put(key, ExpirationInfo.ofWithCreation(expirationTime, creationTime));
        } else if (ttl != null) {
            // TTL only: set from now on writes (this path is only reached on writes
            // since recordAccess only calls updateExpiration when TTI is configured)
            expirationInfo.put(key, ExpirationInfo.of(now + ttl.toMillis()));
        } else {
            // TTI only: always relative to current access time
            expirationInfo.put(key, ExpirationInfo.of(now + tti.toMillis()));
        }
    }

    private boolean isExpired(K key, ValueHolder<V> holder) {
        ExpirationInfo info = expirationInfo.get(key);
        if (info == null) {
            return false;
        }
        return info.isExpired();
    }

    /**
     * Check expiration using a pre-fetched ExpirationInfo (for use after atomic remove)
     * 使用预取的ExpirationInfo检查过期（用于原子删除后）
     */
    private boolean isExpiredHolder(ValueHolder<V> holder, K key) {
        // Expiration info was already removed, so check based on TTL config
        // For getAndRemove, we need to check if the holder was expired before removal.
        // Since expirationInfo was already removed, we check the holder's timestamps directly.
        Duration ttl = config.expireAfterWrite();
        Duration tti = config.expireAfterAccess();
        if (ttl == null && tti == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (ttl != null && now - holder.createTime > ttl.toMillis()) {
            return true;
        }
        if (tti != null && now - holder.lastAccessTime > tti.toMillis()) {
            return true;
        }
        return false;
    }

    /**
     * Shutdown the cache and release resources
     * 关闭缓存并释放资源
     *
     * @since V2.0.3
     */
    public void shutdown() {
        if (cleanupScheduler != null && !cleanupScheduler.isShutdown()) {
            cleanupScheduler.shutdown();
            try {
                if (!cleanupScheduler.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    cleanupScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                cleanupScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        // Shut down internally-created executor (e.g., virtual thread executor)
        if (ownsExecutor && executor instanceof java.util.concurrent.ExecutorService es) {
            es.shutdown();
        }
    }

    /**
     * Closes this cache, delegating to {@link #shutdown()}.
     * 关闭此缓存，委托给 {@link #shutdown()}。
     *
     * @since V2.0.4
     */
    @Override
    public void close() {
        shutdown();
    }

    // ==================== Inner Classes ====================

    private static class ValueHolder<V> {
        final V value;
        final long createTime;
        volatile long lastAccessTime;
        final java.util.concurrent.atomic.AtomicLong accessCount;

        ValueHolder(V value) {
            this.value = value;
            this.createTime = System.currentTimeMillis();
            this.lastAccessTime = this.createTime;
            this.accessCount = new java.util.concurrent.atomic.AtomicLong(0);
        }
    }

    private class ConcurrentMapView extends AbstractMap<K, V> implements ConcurrentMap<K, V> {
        @Override
        public V get(Object key) {
            @SuppressWarnings("unchecked")
            K k = (K) key;
            return DefaultCache.this.get(k);
        }

        @Override
        public V put(K key, V value) {
            // Use compute for atomicity to capture old value and set new in one step
            @SuppressWarnings("unchecked")
            V[] old = (V[]) new Object[1];
            DefaultCache.this.compute(key, (k, existing) -> {
                old[0] = existing;
                return value;
            });
            return old[0];
        }

        @Override
        public V remove(Object key) {
            @SuppressWarnings("unchecked")
            K k = (K) key;
            // Use atomic getAndRemove to avoid TOCTOU race between get and invalidate
            return DefaultCache.this.getAndRemove(k);
        }

        @Override
        public boolean containsKey(Object key) {
            @SuppressWarnings("unchecked")
            K k = (K) key;
            return DefaultCache.this.containsKey(k);
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return DefaultCache.this.entries();
        }

        @Override
        public V putIfAbsent(K key, V value) {
            // Use compute for atomicity to avoid TOCTOU race
            @SuppressWarnings("unchecked")
            V[] result = (V[]) new Object[1];
            DefaultCache.this.compute(key, (k, existing) -> {
                if (existing != null) {
                    result[0] = existing;
                    return existing;
                }
                result[0] = null;
                return value;
            });
            return result[0];
        }

        @Override
        public boolean remove(Object key, Object value) {
            // Use compute for atomic compare-and-remove to avoid TOCTOU race
            @SuppressWarnings("unchecked")
            K k = (K) key;
            boolean[] removed = new boolean[]{false};
            DefaultCache.this.compute(k, (kk, existing) -> {
                if (Objects.equals(existing, value)) {
                    removed[0] = true;
                    return null;
                }
                removed[0] = false;
                return existing;
            });
            return removed[0];
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            // Delegate to the atomic replace in DefaultCache
            return DefaultCache.this.replace(key, oldValue, newValue);
        }

        @Override
        public V replace(K key, V value) {
            // Delegate to the atomic replace in DefaultCache
            return DefaultCache.this.replace(key, value);
        }
    }
}

/**
 * Default Async Cache implementation
 */
class DefaultAsyncCache<K, V> implements AsyncCache<K, V> {
    private final Cache<K, V> sync;
    private final Executor executor;

    DefaultAsyncCache(Cache<K, V> sync, Executor executor) {
        this.sync = sync;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<V> getAsync(K key) {
        return CompletableFuture.supplyAsync(() -> sync.get(key), executor);
    }

    @Override
    public CompletableFuture<V> getAsync(K key,
                                          BiFunction<? super K, ? super Executor, ? extends CompletableFuture<V>> loader) {
        return CompletableFuture.supplyAsync(() -> sync.get(key), executor)
                .thenCompose(value -> {
                    if (value != null) {
                        return CompletableFuture.completedFuture(value);
                    }
                    return loader.apply(key, executor).thenApply(loaded -> {
                        if (loaded != null) {
                            sync.put(key, loaded);
                        }
                        return loaded;
                    });
                });
    }

    @Override
    public CompletableFuture<Map<K, V>> getAllAsync(Iterable<? extends K> keys) {
        return CompletableFuture.supplyAsync(() -> sync.getAll(keys), executor);
    }

    @Override
    public CompletableFuture<Void> putAsync(K key, V value) {
        return CompletableFuture.runAsync(() -> sync.put(key, value), executor);
    }

    @Override
    public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> map) {
        return CompletableFuture.runAsync(() -> sync.putAll(map), executor);
    }

    @Override
    public CompletableFuture<Void> invalidateAsync(K key) {
        return CompletableFuture.runAsync(() -> sync.invalidate(key), executor);
    }

    @Override
    public CompletableFuture<Void> invalidateAllAsync(Iterable<? extends K> keys) {
        return CompletableFuture.runAsync(() -> sync.invalidateAll(keys), executor);
    }

    @Override
    public Cache<K, V> sync() {
        return sync;
    }
}
