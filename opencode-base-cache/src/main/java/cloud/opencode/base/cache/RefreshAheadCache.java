package cloud.opencode.base.cache;

import cloud.opencode.base.cache.spi.RefreshAheadPolicy;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Refresh Ahead Cache - Proactive cache refresh decorator
 * 提前刷新缓存 - 主动缓存刷新装饰器
 *
 * <p>Wraps an existing cache to automatically refresh entries before they expire,
 * ensuring cache hits even during refresh. This prevents cache stampede
 * and improves latency by refreshing entries in the background.</p>
 * <p>包装现有缓存以在条目过期前自动刷新，确保在刷新期间仍能命中缓存。
 * 这可以防止缓存击穿并通过后台刷新提高延迟性能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Proactive refresh before expiration - 过期前主动刷新</li>
 *   <li>Background async refresh - 后台异步刷新</li>
 *   <li>Stale-while-revalidate pattern - 验证时提供过期数据模式</li>
 *   <li>Configurable refresh policy - 可配置的刷新策略</li>
 *   <li>Single-flight refresh prevention - 单次刷新防止重复</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create cache with refresh-ahead
 * Cache<String, User> cache = OpenCache.getOrCreate("users");
 * RefreshAheadCache<String, User> refreshCache = RefreshAheadCache.wrap(cache)
 *     .refreshPolicy(RefreshAheadPolicy.percentageOfTtl(0.8))
 *     .loader(key -> userService.findById(key))
 *     .ttl(Duration.ofMinutes(30))
 *     .build();
 *
 * // Value will be refreshed in background when 80% of TTL has elapsed
 * User user = refreshCache.get("user:1");
 *
 * // With custom executor
 * RefreshAheadCache<String, User> refreshCache = RefreshAheadCache.wrap(cache)
 *     .refreshPolicy(RefreshAheadPolicy.beforeExpiration(Duration.ofSeconds(30)))
 *     .loader(key -> userService.findById(key))
 *     .executor(Executors.newVirtualThreadPerTaskExecutor())
 *     .build();
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
 * @since JDK 25, opencode-base-cache V2.0.0
 */
public final class RefreshAheadCache<K, V> implements Cache<K, V> {

    private final Cache<K, V> delegate;
    private final RefreshAheadPolicy<K, V> policy;
    private final Function<? super K, ? extends V> loader;
    private final Executor executor;
    private final boolean ownsExecutor; // true if we created the executor (need to shut it down)
    private final Duration ttl;

    // Track entry creation times for refresh calculation
    private final ConcurrentMap<K, Long> entryTimestamps = new ConcurrentHashMap<>();
    // Track in-flight refreshes to prevent thundering herd
    private final ConcurrentMap<K, CompletableFuture<V>> inFlightRefreshes = new ConcurrentHashMap<>();

    private RefreshAheadCache(Builder<K, V> builder) {
        this.delegate = builder.delegate;
        this.policy = builder.policy != null ? builder.policy : RefreshAheadPolicy.percentageOfTtl(0.8);
        this.loader = Objects.requireNonNull(builder.loader, "loader is required for refresh-ahead");
        this.ownsExecutor = builder.executor == null;
        this.executor = builder.executor != null ? builder.executor :
                Executors.newVirtualThreadPerTaskExecutor();
        this.ttl = builder.ttl != null ? builder.ttl : Duration.ofMinutes(30);
    }

    /**
     * Wrap a cache with refresh-ahead behavior
     * 使用提前刷新行为包装缓存
     *
     * @param cache cache to wrap | 要包装的缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> wrap(Cache<K, V> cache) {
        return new Builder<>(cache);
    }

    // ==================== Core Get with Refresh-Ahead ====================

    @Override
    public V get(K key) {
        V value = delegate.get(key);
        if (value != null) {
            checkAndRefreshAsync(key);
        } else {
            // Entry was evicted/expired from delegate; clean up stale timestamp
            entryTimestamps.remove(key);
        }
        return value;
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> loader) {
        V value = delegate.get(key);
        if (value != null) {
            checkAndRefreshAsync(key);
            return value;
        }

        // First load
        value = loader.apply(key);
        if (value != null) {
            delegate.put(key, value);
            entryTimestamps.put(key, System.currentTimeMillis());
        }
        return value;
    }

    @Override
    public V getOrDefault(K key, V defaultValue) {
        V value = delegate.get(key);
        if (value != null) {
            checkAndRefreshAsync(key);
            return value;
        }
        // Entry was evicted/expired from delegate; clean up stale timestamp
        entryTimestamps.remove(key);
        return defaultValue;
    }

    /**
     * Check if entry needs refresh and trigger async refresh if needed
     * 检查条目是否需要刷新，如果需要则触发异步刷新
     */
    private void checkAndRefreshAsync(K key) {
        Long createTime = entryTimestamps.get(key);
        if (createTime == null) {
            // Entry exists but no timestamp (created before wrapping)
            entryTimestamps.put(key, System.currentTimeMillis());
            return;
        }

        long ageMillis = System.currentTimeMillis() - createTime;
        long ttlMillis = ttl.toMillis();

        if (policy.shouldRefresh(key, ageMillis, ttlMillis)) {
            triggerAsyncRefresh(key);
        }
    }

    /**
     * Trigger async refresh with single-flight protection
     * 使用单次刷新保护触发异步刷新
     */
    private void triggerAsyncRefresh(K key) {
        // Use computeIfAbsent to ensure single-flight
        inFlightRefreshes.computeIfAbsent(key, k -> {
            CompletableFuture<V> future = CompletableFuture.supplyAsync(() -> {
                V oldValue = delegate.get(key);
                V newValue = loader.apply(key);

                if (newValue != null) {
                    delegate.put(key, newValue);
                    entryTimestamps.put(key, System.currentTimeMillis());
                    policy.onRefreshSuccess(key, oldValue, newValue);
                }
                return newValue;
            }, executor).whenComplete((result, error) -> {
                // Handle failure notification
                if (error != null) {
                    policy.onRefreshFailure(key, delegate.get(key), error);
                }
                // Clean up after a small delay to prevent immediate re-refresh
                // Use try-catch to ensure cleanup even if delayed executor fails
                try {
                    CompletableFuture.delayedExecutor(100, TimeUnit.MILLISECONDS, executor)
                            .execute(() -> inFlightRefreshes.remove(key));
                } catch (Exception e) {
                    // Fallback: immediate removal if delayed executor fails
                    inFlightRefreshes.remove(key);
                }
            });

            return future;
        });
    }

    // ==================== Write Operations ====================

    @Override
    public void put(K key, V value) {
        delegate.put(key, value);
        entryTimestamps.put(key, System.currentTimeMillis());
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        delegate.putAll(map);
        long now = System.currentTimeMillis();
        map.keySet().forEach(k -> entryTimestamps.put(k, now));
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        boolean result = delegate.putIfAbsent(key, value);
        if (result) {
            entryTimestamps.put(key, System.currentTimeMillis());
        }
        return result;
    }

    @Override
    public void putWithTtl(K key, V value, Duration ttl) {
        delegate.putWithTtl(key, value, ttl);
        entryTimestamps.put(key, System.currentTimeMillis());
    }

    @Override
    public void putAllWithTtl(Map<? extends K, ? extends V> map, Duration ttl) {
        delegate.putAllWithTtl(map, ttl);
        long now = System.currentTimeMillis();
        map.keySet().forEach(k -> entryTimestamps.put(k, now));
    }

    @Override
    public boolean putIfAbsentWithTtl(K key, V value, Duration ttl) {
        boolean result = delegate.putIfAbsentWithTtl(key, value, ttl);
        if (result) {
            entryTimestamps.put(key, System.currentTimeMillis());
        }
        return result;
    }

    // ==================== Batch Read Operations ====================

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys) {
        Map<K, V> result = delegate.getAll(keys);
        result.keySet().forEach(this::checkAndRefreshAsync);
        return result;
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys,
                           Function<? super Set<? extends K>, ? extends Map<K, V>> loader) {
        Map<K, V> result = delegate.getAll(keys, loader);
        long now = System.currentTimeMillis();

        // Track new entries
        for (K key : keys) {
            if (!entryTimestamps.containsKey(key) && result.containsKey(key)) {
                entryTimestamps.put(key, now);
            }
        }

        // Check existing entries for refresh
        result.keySet().forEach(this::checkAndRefreshAsync);
        return result;
    }

    // ==================== Compute Operations ====================

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        V result = delegate.computeIfPresent(key, remappingFunction);
        if (result != null) {
            entryTimestamps.put(key, System.currentTimeMillis());
        }
        return result;
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        V result = delegate.compute(key, remappingFunction);
        if (result != null) {
            entryTimestamps.put(key, System.currentTimeMillis());
        } else {
            entryTimestamps.remove(key);
        }
        return result;
    }

    @Override
    public V getAndRemove(K key) {
        V result = delegate.getAndRemove(key);
        entryTimestamps.remove(key);
        inFlightRefreshes.remove(key);
        return result;
    }

    @Override
    public V replace(K key, V value) {
        V result = delegate.replace(key, value);
        if (result != null) {
            entryTimestamps.put(key, System.currentTimeMillis());
        }
        return result;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        boolean result = delegate.replace(key, oldValue, newValue);
        if (result) {
            entryTimestamps.put(key, System.currentTimeMillis());
        }
        return result;
    }

    // ==================== Invalidation ====================

    @Override
    public void invalidate(K key) {
        delegate.invalidate(key);
        entryTimestamps.remove(key);
        inFlightRefreshes.remove(key);
    }

    @Override
    public void invalidateAll(Iterable<? extends K> keys) {
        delegate.invalidateAll(keys);
        for (K key : keys) {
            entryTimestamps.remove(key);
            inFlightRefreshes.remove(key);
        }
    }

    @Override
    public void invalidateAll() {
        delegate.invalidateAll();
        entryTimestamps.clear();
        inFlightRefreshes.clear();
    }

    // ==================== Pass-through Operations ====================

    @Override
    public boolean containsKey(K key) {
        boolean exists = delegate.containsKey(key);
        if (!exists) {
            // Entry was evicted/expired from delegate; clean up stale timestamp
            entryTimestamps.remove(key);
        }
        return exists;
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
        // Clean up timestamps for removed entries
        Set<K> validKeys = delegate.keys();
        entryTimestamps.keySet().retainAll(validKeys);
    }

    @Override
    public AsyncCache<K, V> async() {
        return delegate.async();
    }

    @Override
    public String name() {
        return delegate.name();
    }

    // ==================== Refresh Statistics ====================

    /**
     * Get refresh statistics
     * 获取刷新统计
     *
     * @return refresh stats | 刷新统计
     */
    public RefreshStats getRefreshStats() {
        return new RefreshStats(
                entryTimestamps.size(),
                inFlightRefreshes.size()
        );
    }

    /**
     * Force refresh a specific key
     * 强制刷新特定键
     *
     * @param key the key to refresh | 要刷新的键
     * @return future with new value | 包含新值的 Future
     */
    public CompletableFuture<V> forceRefresh(K key) {
        // Cancel any existing in-flight refresh for this key
        CompletableFuture<V> existing = inFlightRefreshes.remove(key);
        if (existing != null) {
            existing.cancel(false);
        }

        // Use array to hold future reference for cleanup callback
        @SuppressWarnings("unchecked")
        CompletableFuture<V>[] futureHolder = new CompletableFuture[1];

        CompletableFuture<V> future = CompletableFuture.supplyAsync(() -> {
            V newValue = loader.apply(key);
            if (newValue != null) {
                delegate.put(key, newValue);
                entryTimestamps.put(key, System.currentTimeMillis());
            }
            return newValue;
        }, executor).whenComplete((result, error) -> {
            // Always remove from in-flight map when done
            if (futureHolder[0] != null) {
                inFlightRefreshes.remove(key, futureHolder[0]);
            }
        });

        futureHolder[0] = future;
        // Track this force refresh
        inFlightRefreshes.put(key, future);
        return future;
    }

    /**
     * Cancel a pending refresh for a specific key
     * 取消特定键的待处理刷新
     *
     * @param key the key to cancel refresh for | 要取消刷新的键
     * @return true if a refresh was cancelled | 如果取消了刷新返回 true
     * @since V2.0.1
     */
    public boolean cancelPendingRefresh(K key) {
        CompletableFuture<V> future = inFlightRefreshes.remove(key);
        if (future != null) {
            future.cancel(false);
            return true;
        }
        return false;
    }

    /**
     * Cancel all pending refreshes
     * 取消所有待处理的刷新
     *
     * @return count of cancelled refreshes | 取消的刷新数量
     * @since V2.0.1
     */
    public int cancelAllPendingRefreshes() {
        int count = inFlightRefreshes.size();
        inFlightRefreshes.values().forEach(f -> f.cancel(false));
        inFlightRefreshes.clear();
        return count;
    }

    /**
     * Shutdown the refresh-ahead cache, cancelling pending refreshes and releasing resources
     * 关闭提前刷新缓存，取消待处理的刷新并释放资源
     *
     * @since V2.0.6
     */
    public void shutdown() {
        // Cancel all in-flight refreshes
        cancelAllPendingRefreshes();
        // Shut down internally-created executor
        if (ownsExecutor && executor instanceof ExecutorService es) {
            es.shutdown();
        }
    }

    /**
     * Check if a refresh is in progress for a specific key
     * 检查特定键是否正在刷新
     *
     * @param key the key to check | 要检查的键
     * @return true if refresh is in progress | 正在刷新返回 true
     * @since V2.0.1
     */
    public boolean isRefreshInProgress(K key) {
        CompletableFuture<V> future = inFlightRefreshes.get(key);
        return future != null && !future.isDone();
    }

    /**
     * Get count of in-flight refreshes
     * 获取正在进行的刷新数量
     *
     * @return count of in-flight refreshes | 进行中的刷新数量
     * @since V2.0.1
     */
    public int getInFlightRefreshCount() {
        return (int) inFlightRefreshes.values().stream()
                .filter(f -> !f.isDone())
                .count();
    }

    /**
     * Refresh statistics record
     * 刷新统计记录
     */
    public record RefreshStats(
            int trackedEntries,
            int inFlightRefreshes
    ) {
    }

    // ==================== Builder ====================

    /**
     * Builder for RefreshAheadCache
     */
    public static class Builder<K, V> {
        private final Cache<K, V> delegate;
        private RefreshAheadPolicy<K, V> policy;
        private Function<? super K, ? extends V> loader;
        private Executor executor;
        private Duration ttl;

        Builder(Cache<K, V> delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
        }

        /**
         * Set refresh policy
         * 设置刷新策略
         *
         * @param policy the refresh policy | 刷新策略
         * @return this builder | 此构建器
         */
        public Builder<K, V> refreshPolicy(RefreshAheadPolicy<K, V> policy) {
            this.policy = policy;
            return this;
        }

        /**
         * Set loader function for refreshing values
         * 设置用于刷新值的加载函数
         *
         * @param loader the loader | 加载器
         * @return this builder | 此构建器
         */
        public Builder<K, V> loader(Function<? super K, ? extends V> loader) {
            this.loader = loader;
            return this;
        }

        /**
         * Set executor for async refresh
         * 设置异步刷新执行器
         *
         * @param executor the executor | 执行器
         * @return this builder | 此构建器
         */
        public Builder<K, V> executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Set TTL for refresh calculation
         * 设置刷新计算的 TTL
         *
         * @param ttl the TTL | TTL
         * @return this builder | 此构建器
         */
        public Builder<K, V> ttl(Duration ttl) {
            this.ttl = ttl;
            return this;
        }

        /**
         * Build the refresh-ahead cache
         * 构建提前刷新缓存
         *
         * @return refresh-ahead cache | 提前刷新缓存
         */
        public RefreshAheadCache<K, V> build() {
            return new RefreshAheadCache<>(this);
        }
    }
}
