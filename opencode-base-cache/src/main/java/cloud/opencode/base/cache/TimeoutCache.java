package cloud.opencode.base.cache;

import cloud.opencode.base.cache.exception.OpenCacheException;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Timeout Cache - Cache wrapper with operation timeout support
 * 超时缓存 - 支持操作超时控制的缓存包装器
 *
 * <p>Wraps an existing cache to add timeout capabilities to all operations.
 * This is particularly useful for cache loaders that may block indefinitely.</p>
 * <p>包装现有缓存以为所有操作添加超时功能。这对于可能无限阻塞的缓存加载器特别有用。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create timeout cache with 5 second default timeout
 * Cache<String, User> cache = OpenCache.getOrCreate("users");
 * TimeoutCache<String, User> timeoutCache = TimeoutCache.wrap(cache)
 *     .defaultTimeout(Duration.ofSeconds(5))
 *     .build();
 *
 * // Operations will timeout after 5 seconds
 * User user = timeoutCache.get("user:1", key -> slowDatabaseCall(key));
 *
 * // Override timeout for specific operation
 * User user2 = timeoutCache.getWithTimeout("user:2",
 *     key -> verySlowCall(key), Duration.ofSeconds(30));
 *
 * // Async operations with timeout
 * CompletableFuture<User> future = timeoutCache.async()
 *     .getAsync("user:3", (k, executor) -> loadAsync(k));
 * }</pre>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable default timeout - 可配置默认超时</li>
 *   <li>Per-operation timeout override - 每操作超时覆盖</li>
 *   <li>Virtual thread executor support - 虚拟线程执行器支持</li>
 *   <li>Transparent timeout wrapping - 透明超时包装</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (null values not allowed) - 空值安全: 部分（不允许 null 值）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.9.0
 */
public final class TimeoutCache<K, V> implements Cache<K, V> {

    private final Cache<K, V> delegate;
    private final Duration defaultTimeout;
    private final ExecutorService executor;
    private final boolean shutdownExecutorOnClose;

    private TimeoutCache(Builder<K, V> builder) {
        this.delegate = builder.delegate;
        this.defaultTimeout = builder.defaultTimeout;
        if (builder.executor != null) {
            this.executor = builder.executor;
            this.shutdownExecutorOnClose = false;
        } else {
            this.executor = Executors.newVirtualThreadPerTaskExecutor();
            this.shutdownExecutorOnClose = true;
        }
    }

    /**
     * Wrap a cache with timeout support
     * 使用超时支持包装缓存
     *
     * @param cache the cache to wrap | 要包装的缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> wrap(Cache<K, V> cache) {
        return new Builder<>(cache);
    }

    // ==================== Timeout Operations | 超时操作 ====================

    /**
     * Get with explicit timeout
     * 使用显式超时获取
     *
     * @param key     the key | 键
     * @param loader  value loader | 值加载器
     * @param timeout operation timeout | 操作超时
     * @return the value or null | 值或 null
     * @throws CacheTimeoutException if operation times out | 如果操作超时
     */
    public V getWithTimeout(K key, Function<? super K, ? extends V> loader, Duration timeout) {
        V value = delegate.get(key);
        if (value != null) {
            return value;
        }

        try {
            return CompletableFuture.supplyAsync(() -> delegate.get(key, loader), executor)
                    .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new CacheTimeoutException("Cache load timed out after " + timeout + " for key: " + key, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpenCacheException("Cache load interrupted for key: " + key, e);
        } catch (ExecutionException e) {
            throw new OpenCacheException("Cache load failed for key: " + key, e.getCause());
        }
    }

    /**
     * Get all with explicit timeout
     * 使用显式超时批量获取
     *
     * @param keys    the keys | 键集合
     * @param loader  batch loader | 批量加载器
     * @param timeout operation timeout | 操作超时
     * @return map of found values | 找到的值的映射
     * @throws CacheTimeoutException if operation times out | 如果操作超时
     */
    public Map<K, V> getAllWithTimeout(Iterable<? extends K> keys,
                                       Function<? super Set<? extends K>, ? extends Map<K, V>> loader,
                                       Duration timeout) {
        try {
            return CompletableFuture.supplyAsync(() -> delegate.getAll(keys, loader), executor)
                    .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new CacheTimeoutException("Batch cache load timed out after " + timeout, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpenCacheException("Batch cache load interrupted", e);
        } catch (ExecutionException e) {
            throw new OpenCacheException("Batch cache load failed", e.getCause());
        }
    }

    // ==================== Cache Interface Implementation ====================

    @Override
    public V get(K key) {
        return delegate.get(key);
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> loader) {
        return getWithTimeout(key, loader, defaultTimeout);
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys) {
        return delegate.getAll(keys);
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys,
                           Function<? super Set<? extends K>, ? extends Map<K, V>> loader) {
        return getAllWithTimeout(keys, loader, defaultTimeout);
    }

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
    public V getOrDefault(K key, V defaultValue) {
        return delegate.getOrDefault(key, defaultValue);
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
    }

    @Override
    public AsyncCache<K, V> async() {
        return new TimeoutAsyncCache<>(delegate.async(), defaultTimeout, executor);
    }

    @Override
    public String name() {
        return delegate.name();
    }

    /**
     * Get the underlying cache
     * 获取底层缓存
     *
     * @return delegate cache | 委托缓存
     */
    public Cache<K, V> getDelegate() {
        return delegate;
    }

    /**
     * Get the default timeout
     * 获取默认超时
     *
     * @return default timeout | 默认超时
     */
    public Duration getDefaultTimeout() {
        return defaultTimeout;
    }

    /**
     * Shutdown the executor if owned by this cache
     * 如果执行器由此缓存拥有则关闭
     */
    public void shutdown() {
        if (shutdownExecutorOnClose) {
            executor.shutdown();
        }
    }

    // ==================== Timeout Async Cache ====================

    private static class TimeoutAsyncCache<K, V> implements AsyncCache<K, V> {
        private final AsyncCache<K, V> delegate;
        private final Duration defaultTimeout;
        private final ExecutorService executor;

        TimeoutAsyncCache(AsyncCache<K, V> delegate, Duration defaultTimeout, ExecutorService executor) {
            this.delegate = delegate;
            this.defaultTimeout = defaultTimeout;
            this.executor = executor;
        }

        @Override
        public CompletableFuture<V> getAsync(K key) {
            return delegate.getAsync(key);
        }

        @Override
        public CompletableFuture<V> getAsync(K key,
                                             BiFunction<? super K, ? super Executor, ? extends CompletableFuture<V>> loader) {
            return delegate.getAsync(key, loader)
                    .orTimeout(defaultTimeout.toMillis(), TimeUnit.MILLISECONDS)
                    .exceptionally(ex -> {
                        if (ex instanceof TimeoutException) {
                            throw new CacheTimeoutException("Async cache load timed out for key: " + key, ex);
                        }
                        throw new OpenCacheException("Async cache load failed for key: " + key, ex);
                    });
        }

        @Override
        public CompletableFuture<Map<K, V>> getAllAsync(Iterable<? extends K> keys) {
            return delegate.getAllAsync(keys);
        }

        @Override
        public CompletableFuture<Void> putAsync(K key, V value) {
            return delegate.putAsync(key, value);
        }

        @Override
        public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> map) {
            return delegate.putAllAsync(map);
        }

        @Override
        public CompletableFuture<Void> invalidateAsync(K key) {
            return delegate.invalidateAsync(key);
        }

        @Override
        public CompletableFuture<Void> invalidateAllAsync(Iterable<? extends K> keys) {
            return delegate.invalidateAllAsync(keys);
        }

        @Override
        public Cache<K, V> sync() {
            return delegate.sync();
        }
    }

    // ==================== Builder ====================

    /**
     * Builder for TimeoutCache
     */
    public static class Builder<K, V> {
        private final Cache<K, V> delegate;
        private Duration defaultTimeout = Duration.ofSeconds(30);
        private ExecutorService executor;

        Builder(Cache<K, V> delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
        }

        /**
         * Set default timeout for operations
         * 设置操作的默认超时
         *
         * @param timeout default timeout | 默认超时
         * @return this builder | 此构建器
         */
        public Builder<K, V> defaultTimeout(Duration timeout) {
            this.defaultTimeout = Objects.requireNonNull(timeout, "timeout cannot be null");
            return this;
        }

        /**
         * Set custom executor for async operations
         * 设置异步操作的自定义执行器
         *
         * @param executor the executor | 执行器
         * @return this builder | 此构建器
         */
        public Builder<K, V> executor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Build the timeout cache
         * 构建超时缓存
         *
         * @return timeout cache | 超时缓存
         */
        public TimeoutCache<K, V> build() {
            return new TimeoutCache<>(this);
        }
    }

    // ==================== Exception ====================

    /**
     * Exception thrown when cache operation times out
     * 缓存操作超时时抛出的异常
     */
    public static class CacheTimeoutException extends OpenCacheException {
        public CacheTimeoutException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
