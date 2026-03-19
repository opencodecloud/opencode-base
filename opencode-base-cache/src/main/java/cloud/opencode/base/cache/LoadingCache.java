package cloud.opencode.base.cache;

import cloud.opencode.base.cache.config.CacheConfig;
import cloud.opencode.base.cache.internal.DefaultCache;
import cloud.opencode.base.cache.spi.CacheLoader;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Loading Cache Interface - Cache with automatic value loading
 * 加载缓存接口 - 具有自动值加载功能的缓存
 *
 * <p>Extends {@link Cache} with automatic loading capabilities. When a key is
 * not present, the configured loader is automatically called to load the value.</p>
 * <p>扩展 {@link Cache}，具有自动加载功能。当键不存在时，自动调用配置的加载器加载值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Automatic loading on get - 获取时自动加载</li>
 *   <li>Batch loading support - 批量加载支持</li>
 *   <li>Refresh support - 刷新支持</li>
 *   <li>Async loading - 异步加载</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create loading cache
 * LoadingCache<String, User> cache = LoadingCache.create(
 *     "users",
 *     key -> userService.findById(key),
 *     CacheConfig.<String, User>builder()
 *         .maximumSize(10000)
 *         .expireAfterWrite(Duration.ofMinutes(30))
 *         .build()
 * );
 *
 * // Get value - automatically loads if not present
 * User user = cache.get("user:1001");
 *
 * // Refresh value
 * cache.refresh("user:1001");
 * }</pre>
 *
 * @param <K> the type of keys | 键类型
 * @param <V> the type of values | 值类型
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
 * @since JDK 25, opencode-base-cache V2.0.3
 */
public interface LoadingCache<K, V> extends Cache<K, V> {

    /**
     * Get value, automatically loading if not present
     * 获取值，不存在时自动加载
     *
     * <p>Unlike {@link Cache#get(Object)}, this method never returns null
     * for non-null keys if the loader can provide a value.</p>
     * <p>与 {@link Cache#get(Object)} 不同，如果加载器能提供值，此方法对非 null 键永不返回 null。</p>
     *
     * @param key the key | 键
     * @return the value | 值
     * @throws RuntimeException if loading fails | 加载失败时抛出异常
     */
    @Override
    V get(K key);

    /**
     * Get all values for given keys, loading missing entries
     * 获取所有键的值，加载缺失的条目
     *
     * @param keys the keys | 键集合
     * @return map of all entries | 所有条目 Map
     */
    @Override
    Map<K, V> getAll(Iterable<? extends K> keys);

    /**
     * Refresh value for given key
     * 刷新给定键的值
     *
     * <p>Asynchronously reloads the value for the key. If the key is not present,
     * it will be loaded.</p>
     * <p>异步重新加载键的值。如果键不存在，将被加载。</p>
     *
     * @param key the key to refresh | 要刷新的键
     * @return future that completes when refresh is done | 刷新完成时完成的 Future
     */
    CompletableFuture<V> refresh(K key);

    /**
     * Refresh all entries matching the predicate
     * 刷新所有匹配条件的条目
     *
     * @param predicate the predicate to match keys | 匹配键的条件
     * @return count of refreshed entries | 刷新的条目数
     */
    default int refreshAll(java.util.function.Predicate<K> predicate) {
        int count = 0;
        for (K key : keys()) {
            if (predicate.test(key)) {
                refresh(key);
                count++;
            }
        }
        return count;
    }

    /**
     * Get the loader function
     * 获取加载器函数
     *
     * @return the loader | 加载器
     */
    Function<? super K, ? extends V> loader();

    /**
     * Get async view of this loading cache
     * 获取加载缓存的异步视图
     *
     * @return async loading cache view | 异步加载缓存视图
     */
    AsyncLoadingCache<K, V> asyncLoading();

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a loading cache with the given loader
     * 使用给定的加载器创建加载缓存
     *
     * @param name   cache name | 缓存名称
     * @param loader the loader function | 加载函数
     * @param config cache configuration | 缓存配置
     * @param <K>    key type | 键类型
     * @param <V>    value type | 值类型
     * @return loading cache | 加载缓存
     */
    static <K, V> LoadingCache<K, V> create(String name,
                                             Function<? super K, ? extends V> loader,
                                             CacheConfig<K, V> config) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(loader, "loader cannot be null");
        Objects.requireNonNull(config, "config cannot be null");
        return new DefaultLoadingCache<>(name, loader, config);
    }

    /**
     * Create a loading cache with default configuration
     * 使用默认配置创建加载缓存
     *
     * @param name   cache name | 缓存名称
     * @param loader the loader function | 加载函数
     * @param <K>    key type | 键类型
     * @param <V>    value type | 值类型
     * @return loading cache | 加载缓存
     */
    static <K, V> LoadingCache<K, V> create(String name,
                                             Function<? super K, ? extends V> loader) {
        return create(name, loader, CacheConfig.defaultConfig());
    }

    /**
     * Create a loading cache with CacheLoader
     * 使用 CacheLoader 创建加载缓存
     *
     * @param name   cache name | 缓存名称
     * @param loader the cache loader | 缓存加载器
     * @param config cache configuration | 缓存配置
     * @param <K>    key type | 键类型
     * @param <V>    value type | 值类型
     * @return loading cache | 加载缓存
     */
    static <K, V> LoadingCache<K, V> createWithLoader(String name,
                                                       CacheLoader<K, V> loader,
                                                       CacheConfig<K, V> config) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(loader, "loader cannot be null");
        Objects.requireNonNull(config, "config cannot be null");
        return new DefaultLoadingCache<>(name, key -> {
            try {
                return loader.load(key);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, config);
    }
}

/**
 * Async Loading Cache Interface
 * 异步加载缓存接口
 */
interface AsyncLoadingCache<K, V> extends AsyncCache<K, V> {

    /**
     * Get value asynchronously, loading if not present
     * 异步获取值，不存在时加载
     *
     * @param key the key | 键
     * @return future containing the value | 包含值的 Future
     */
    @Override
    CompletableFuture<V> getAsync(K key);

    /**
     * Get all values asynchronously, loading missing entries
     * 异步获取所有值，加载缺失的条目
     *
     * @param keys the keys | 键集合
     * @return future containing map of entries | 包含条目 Map 的 Future
     */
    @Override
    CompletableFuture<Map<K, V>> getAllAsync(Iterable<? extends K> keys);

    /**
     * Refresh value asynchronously
     * 异步刷新值
     *
     * @param key the key | 键
     * @return future that completes with the new value | 完成时包含新值的 Future
     */
    CompletableFuture<V> refreshAsync(K key);

    /**
     * Get sync view
     * 获取同步视图
     *
     * @return loading cache sync view | 加载缓存同步视图
     */
    @Override
    LoadingCache<K, V> sync();
}

/**
 * Default LoadingCache implementation
 * 默认 LoadingCache 实现
 */
class DefaultLoadingCache<K, V> implements LoadingCache<K, V> {

    private static final java.util.concurrent.Executor REFRESH_EXECUTOR =
            java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor();

    private final Cache<K, V> delegate;
    private final Function<? super K, ? extends V> loader;
    private final DefaultAsyncLoadingCache<K, V> asyncView;

    DefaultLoadingCache(String name, Function<? super K, ? extends V> loader, CacheConfig<K, V> config) {
        this.delegate = new DefaultCache<>(name, config);
        this.loader = Objects.requireNonNull(loader, "loader cannot be null");
        this.asyncView = new DefaultAsyncLoadingCache<>(this);
    }

    @Override
    public V get(K key) {
        return delegate.get(key, loader);
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> customLoader) {
        return delegate.get(key, customLoader);
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys) {
        return delegate.getAll(keys, missingKeys -> {
            Map<K, V> result = new java.util.LinkedHashMap<>();
            for (K key : missingKeys) {
                V value = loader.apply(key);
                if (value != null) {
                    result.put(key, value);
                }
            }
            return result;
        });
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys,
                            Function<? super Set<? extends K>, ? extends Map<K, V>> customLoader) {
        return delegate.getAll(keys, customLoader);
    }

    @Override
    public CompletableFuture<V> refresh(K key) {
        return CompletableFuture.supplyAsync(() -> {
            V value = loader.apply(key);
            if (value != null) {
                delegate.put(key, value);
            }
            return value;
        }, REFRESH_EXECUTOR);
    }

    @Override
    public Function<? super K, ? extends V> loader() {
        return loader;
    }

    @Override
    public AsyncLoadingCache<K, V> asyncLoading() {
        return asyncView;
    }

    // ==================== Delegate Methods ====================

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
    public void putWithTtl(K key, V value, java.time.Duration ttl) {
        delegate.putWithTtl(key, value, ttl);
    }

    @Override
    public void putAllWithTtl(Map<? extends K, ? extends V> map, java.time.Duration ttl) {
        delegate.putAllWithTtl(map, ttl);
    }

    @Override
    public boolean putIfAbsentWithTtl(K key, V value, java.time.Duration ttl) {
        return delegate.putIfAbsentWithTtl(key, value, ttl);
    }

    @Override
    public V computeIfPresent(K key, java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return delegate.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
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
    public java.util.Collection<V> values() {
        return delegate.values();
    }

    @Override
    public Set<Map.Entry<K, V>> entries() {
        return delegate.entries();
    }

    @Override
    public java.util.concurrent.ConcurrentMap<K, V> asMap() {
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
        return asyncView;
    }

    @Override
    public String name() {
        return delegate.name();
    }
}

/**
 * Default AsyncLoadingCache implementation
 * 默认 AsyncLoadingCache 实现
 */
class DefaultAsyncLoadingCache<K, V> implements AsyncLoadingCache<K, V> {

    private static final java.util.concurrent.Executor VIRTUAL_EXECUTOR =
            java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor();

    private final DefaultLoadingCache<K, V> sync;

    DefaultAsyncLoadingCache(DefaultLoadingCache<K, V> sync) {
        this.sync = sync;
    }

    @Override
    public CompletableFuture<V> getAsync(K key) {
        return CompletableFuture.supplyAsync(() -> sync.get(key), VIRTUAL_EXECUTOR);
    }

    @Override
    public CompletableFuture<V> getAsync(K key,
                                          java.util.function.BiFunction<? super K, ? super java.util.concurrent.Executor, ? extends CompletableFuture<V>> loader) {
        return CompletableFuture.supplyAsync(() -> sync.get(key), VIRTUAL_EXECUTOR)
                .thenCompose(value -> {
                    if (value != null) {
                        return CompletableFuture.completedFuture(value);
                    }
                    return loader.apply(key, VIRTUAL_EXECUTOR)
                            .thenApply(loaded -> {
                                if (loaded != null) {
                                    sync.put(key, loaded);
                                }
                                return loaded;
                            });
                });
    }

    @Override
    public CompletableFuture<Map<K, V>> getAllAsync(Iterable<? extends K> keys) {
        return CompletableFuture.supplyAsync(() -> sync.getAll(keys), VIRTUAL_EXECUTOR);
    }

    @Override
    public CompletableFuture<V> refreshAsync(K key) {
        return sync.refresh(key);
    }

    @Override
    public CompletableFuture<Void> putAsync(K key, V value) {
        return CompletableFuture.runAsync(() -> sync.put(key, value), VIRTUAL_EXECUTOR);
    }

    @Override
    public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> map) {
        return CompletableFuture.runAsync(() -> sync.putAll(map), VIRTUAL_EXECUTOR);
    }

    @Override
    public CompletableFuture<Void> invalidateAsync(K key) {
        return CompletableFuture.runAsync(() -> sync.invalidate(key), VIRTUAL_EXECUTOR);
    }

    @Override
    public CompletableFuture<Void> invalidateAllAsync(Iterable<? extends K> keys) {
        return CompletableFuture.runAsync(() -> sync.invalidateAll(keys), VIRTUAL_EXECUTOR);
    }

    @Override
    public LoadingCache<K, V> sync() {
        return sync;
    }
}
