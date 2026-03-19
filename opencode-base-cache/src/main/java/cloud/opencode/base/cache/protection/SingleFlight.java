package cloud.opencode.base.cache.protection;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * SingleFlight - Request coalescing for cache stampede prevention
 * SingleFlight - 请求合并，防止缓存击穿
 *
 * <p>Ensures only one loading request is in-flight for each key.</p>
 * <p>确保每个键只有一个加载请求正在进行。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Request deduplication - 请求去重</li>
 *   <li>Concurrent request coalescing - 并发请求合并</li>
 *   <li>Timeout support - 超时支持</li>
 *   <li>Cancellation support - 取消支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SingleFlight<String, User> flight = new SingleFlight<>();
 *
 * // Multiple concurrent requests for same key will share one load
 * // 同一键的多个并发请求将共享一次加载
 * User user = flight.execute("user:1001", key -> loadFromDb(key));
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for check, O(loader) for load - 时间复杂度: 检查 O(1)，加载 O(loader)</li>
 *   <li>Space complexity: O(n) where n is in-flight count - 空间复杂度: O(n) n 为进行中请求数</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
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
public class SingleFlight<K, V> {

    private final ConcurrentHashMap<K, CompletableFuture<V>> flights = new ConcurrentHashMap<>();

    /**
     * Execute loader, coalescing concurrent requests for the same key
     * 执行加载器，合并相同键的并发请求
     *
     * @param key    the key | 键
     * @param loader the loader function | 加载函数
     * @return the loaded value | 加载的值
     */
    public V execute(K key, Function<K, V> loader) {
        CompletableFuture<V> future = flights.computeIfAbsent(key, k ->
                CompletableFuture.supplyAsync(() -> loader.apply(k))
        );

        try {
            return future.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(e.getCause());
        } finally {
            // Remove after join completes to avoid recursive update
            flights.remove(key, future);
        }
    }

    /**
     * Execute loader with timeout
     * 带超时执行加载器
     *
     * @param key     the key | 键
     * @param loader  the loader function | 加载函数
     * @param timeout max wait time | 最大等待时间
     * @return the loaded value | 加载的值
     * @throws TimeoutException if timeout exceeded | 超时时抛出异常
     */
    public V execute(K key, Function<K, V> loader, Duration timeout) throws TimeoutException {
        CompletableFuture<V> future = flights.computeIfAbsent(key, k ->
                CompletableFuture.supplyAsync(() -> loader.apply(k))
        );

        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            throw new TimeoutException("Loading timed out for key: " + key);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Loading interrupted for key: " + key, e);
        } finally {
            // Remove after get completes to avoid recursive update
            flights.remove(key, future);
        }
    }

    /**
     * Execute async loader
     * 异步执行加载器
     *
     * @param key    the key | 键
     * @param loader the async loader function | 异步加载函数
     * @return future containing value | 包含值的 Future
     */
    public CompletableFuture<V> executeAsync(K key, Function<K, CompletableFuture<V>> loader) {
        CompletableFuture<V> future = flights.computeIfAbsent(key, k ->
                loader.apply(k)
        );
        // Schedule removal asynchronously to avoid recursive update
        future.whenCompleteAsync((v, ex) -> flights.remove(key, future));
        return future;
    }

    /**
     * Get count of in-flight requests
     * 获取进行中请求数
     *
     * @return in-flight count | 进行中请求数
     */
    public int inflightCount() {
        return flights.size();
    }

    /**
     * Check if a key is currently loading
     * 检查键是否正在加载
     *
     * @param key the key | 键
     * @return true if loading | 正在加载返回 true
     */
    public boolean isLoading(K key) {
        return flights.containsKey(key);
    }

    /**
     * Cancel waiting for a key
     * 取消等待某个键
     *
     * @param key the key | 键
     * @return true if was loading | 正在加载返回 true
     */
    public boolean cancel(K key) {
        CompletableFuture<V> future = flights.remove(key);
        if (future != null) {
            future.cancel(true);
            return true;
        }
        return false;
    }

    /**
     * Cancel all in-flight requests
     * 取消所有进行中请求
     */
    public void cancelAll() {
        flights.forEach((k, f) -> f.cancel(true));
        flights.clear();
    }

    /**
     * Timeout exception for SingleFlight operations
     * SingleFlight 操作的超时异常
     */
    public static class TimeoutException extends Exception {
        public TimeoutException(String message) {
            super(message);
        }
    }
}
