package cloud.opencode.base.cache.spi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Async Cache Loader SPI - Asynchronous cache value loader interface
 * 异步缓存加载器 SPI - 异步缓存值加载接口
 *
 * <p>Provides interface for asynchronously loading cache values when not present.</p>
 * <p>提供缓存值不存在时的异步加载接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Async single value loading - 异步单值加载</li>
 *   <li>Async batch loading - 异步批量加载</li>
 *   <li>Async reload/refresh - 异步重新加载/刷新</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AsyncCacheLoader<String, User> loader = (key, executor) ->
 *     CompletableFuture.supplyAsync(() -> userDao.findById(key), executor);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Future may complete with null - 空值安全: Future 可能以 null 完成</li>
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
@FunctionalInterface
public interface AsyncCacheLoader<K, V> {

    /**
     * Async load value for single key
     * 异步加载单个键的值
     *
     * @param key      the key | 键
     * @param executor the executor | 执行器
     * @return future containing value | 包含值的 Future
     */
    CompletableFuture<V> asyncLoad(K key, Executor executor);

    /**
     * Async batch load values
     * 异步批量加载值
     *
     * @param keys     the keys | 键集合
     * @param executor the executor | 执行器
     * @return future containing map of values | 包含值 Map 的 Future
     */
    default CompletableFuture<Map<K, V>> asyncLoadAll(Set<? extends K> keys, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<K, V> result = new HashMap<>();
            List<CompletableFuture<Void>> futures = keys.stream()
                    .map(key -> asyncLoad(key, executor)
                            .thenAccept(v -> {
                                if (v != null) {
                                    synchronized (result) {
                                        result.put(key, v);
                                    }
                                }
                            }))
                    .toList();
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
            return result;
        }, executor);
    }

    /**
     * Async reload value
     * 异步重新加载值
     *
     * @param key      the key | 键
     * @param oldValue the old value | 旧值
     * @param executor the executor | 执行器
     * @return future containing new value | 包含新值的 Future
     */
    default CompletableFuture<V> asyncReload(K key, V oldValue, Executor executor) {
        return asyncLoad(key, executor);
    }

    /**
     * Create from sync loader
     * 从同步加载器创建
     *
     * @param loader sync loader | 同步加载器
     * @param <K>    key type | 键类型
     * @param <V>    value type | 值类型
     * @return async loader | 异步加载器
     */
    static <K, V> AsyncCacheLoader<K, V> from(CacheLoader<K, V> loader) {
        return (key, executor) -> CompletableFuture.supplyAsync(() -> {
            try {
                return loader.load(key);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }
}
