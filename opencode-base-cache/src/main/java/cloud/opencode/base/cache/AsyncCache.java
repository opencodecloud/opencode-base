package cloud.opencode.base.cache;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

/**
 * Async Cache Interface - Non-blocking cache operations based on CompletableFuture
 * 异步缓存接口 - 基于 CompletableFuture 的非阻塞缓存操作
 *
 * <p>Provides asynchronous cache operations for high-concurrency scenarios.</p>
 * <p>为高并发场景提供异步缓存操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Async get/put/invalidate operations - 异步获取/放入/失效操作</li>
 *   <li>CompletableFuture based API - 基于 CompletableFuture 的 API</li>
 *   <li>Virtual thread support - 虚拟线程支持</li>
 *   <li>Sync view conversion - 同步视图转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AsyncCache<String, User> async = cache.async();
 *
 * // Async get - 异步获取
 * async.getAsync("user:1001")
 *     .thenAccept(user -> System.out.println(user));
 *
 * // Async get with loader - 异步获取带加载器
 * async.getAsync("user:1002", (key, executor) ->
 *     CompletableFuture.supplyAsync(() -> userService.findById(key), executor))
 *     .thenAccept(this::processUser);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial - 空值安全: 部分</li>
 * </ul>
 *
 * @param <K> the type of keys | 键类型
 * @param <V> the type of values | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public interface AsyncCache<K, V> {

    /**
     * Async get value by key
     * 异步获取值
     *
     * @param key the key | 键
     * @return future containing value or null | 包含值或 null 的 Future
     */
    CompletableFuture<V> getAsync(K key);

    /**
     * Async get with loader
     * 异步获取，不存在时加载
     *
     * @param key    the key | 键
     * @param loader the async loader | 异步加载函数
     * @return future containing value | 包含值的 Future
     */
    CompletableFuture<V> getAsync(K key,
                                   BiFunction<? super K, ? super Executor, ? extends CompletableFuture<V>> loader);

    /**
     * Async batch get
     * 异步批量获取
     *
     * @param keys the keys | 键集合
     * @return future containing map of entries | 包含条目 Map 的 Future
     */
    CompletableFuture<Map<K, V>> getAllAsync(Iterable<? extends K> keys);

    /**
     * Async put value
     * 异步放入值
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return future for completion | 完成信号 Future
     */
    CompletableFuture<Void> putAsync(K key, V value);

    /**
     * Async batch put
     * 异步批量放入
     *
     * @param map the key-value pairs | 键值对 Map
     * @return future for completion | 完成信号 Future
     */
    CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> map);

    /**
     * Async put with TTL
     * 异步放入带 TTL
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @param ttl   time-to-live | 存活时间
     * @return future for completion | 完成信号 Future
     * @since V2.0.2
     */
    default CompletableFuture<Void> putAsync(K key, V value, java.time.Duration ttl) {
        return CompletableFuture.runAsync(() -> sync().putWithTtl(key, value, ttl));
    }

    /**
     * Async batch put with TTL
     * 异步批量放入带 TTL
     *
     * @param map the key-value pairs | 键值对 Map
     * @param ttl time-to-live for all entries | 所有条目的存活时间
     * @return future for completion | 完成信号 Future
     * @since V2.0.2
     */
    default CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> map, java.time.Duration ttl) {
        return CompletableFuture.runAsync(() -> sync().putAllWithTtl(map, ttl));
    }

    /**
     * Async put if absent
     * 异步放入（如果不存在）
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return future containing true if put succeeded | 包含是否成功的 Future
     * @since V2.0.2
     */
    default CompletableFuture<Boolean> putIfAbsentAsync(K key, V value) {
        return CompletableFuture.supplyAsync(() -> sync().putIfAbsent(key, value));
    }

    /**
     * Async put if absent with TTL
     * 异步放入带 TTL（如果不存在）
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @param ttl   time-to-live | 存活时间
     * @return future containing true if put succeeded | 包含是否成功的 Future
     * @since V2.0.2
     */
    default CompletableFuture<Boolean> putIfAbsentAsync(K key, V value, java.time.Duration ttl) {
        return CompletableFuture.supplyAsync(() -> sync().putIfAbsentWithTtl(key, value, ttl));
    }

    /**
     * Async compute
     * 异步计算
     *
     * @param key               the key | 键
     * @param remappingFunction the function to compute value | 计算值的函数
     * @return future containing the new value | 包含新值的 Future
     * @since V2.0.2
     */
    default CompletableFuture<V> computeAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return CompletableFuture.supplyAsync(() -> sync().compute(key, remappingFunction));
    }

    /**
     * Async compute if present
     * 异步计算（如果存在）
     *
     * @param key               the key | 键
     * @param remappingFunction the function to compute value | 计算值的函数
     * @return future containing the new value or null | 包含新值或 null 的 Future
     * @since V2.0.2
     */
    default CompletableFuture<V> computeIfPresentAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return CompletableFuture.supplyAsync(() -> sync().computeIfPresent(key, remappingFunction));
    }

    /**
     * Async get and remove
     * 异步获取并删除
     *
     * @param key the key | 键
     * @return future containing the removed value | 包含被删除值的 Future
     * @since V2.0.2
     */
    default CompletableFuture<V> getAndRemoveAsync(K key) {
        return CompletableFuture.supplyAsync(() -> sync().getAndRemove(key));
    }

    /**
     * Async invalidate key
     * 异步失效
     *
     * @param key the key | 键
     * @return future for completion | 完成信号 Future
     */
    CompletableFuture<Void> invalidateAsync(K key);

    /**
     * Async batch invalidate
     * 异步批量失效
     *
     * @param keys the keys | 键集合
     * @return future for completion | 完成信号 Future
     */
    CompletableFuture<Void> invalidateAllAsync(Iterable<? extends K> keys);

    /**
     * Get sync view of this async cache
     * 获取同步视图
     *
     * @return sync cache view | 同步缓存视图
     */
    Cache<K, V> sync();
}
