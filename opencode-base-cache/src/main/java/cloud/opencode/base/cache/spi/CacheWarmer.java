package cloud.opencode.base.cache.spi;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Cache Warmer SPI - Cache pre-warming interface
 * 缓存预热器 SPI - 缓存预热接口
 *
 * <p>Provides interface for pre-loading cache data at application startup.</p>
 * <p>提供应用启动时预加载缓存数据的接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sync/Async warming - 同步/异步预热</li>
 *   <li>Priority-based loading - 基于优先级的加载</li>
 *   <li>Completion callbacks - 完成回调</li>
 *   <li>Paged loading support - 分页加载支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CacheWarmer<Long, Product> warmer = () -> {
 *     return productDao.findHotProducts(1000).stream()
 *         .collect(Collectors.toMap(Product::getId, p -> p));
 * };
 *
 * // With paged loading - 分页加载
 * CacheWarmer<Long, Product> pagedWarmer = CacheWarmer.paged(
 *     offset -> productDao.findProducts(offset, 100),
 *     100, 10);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
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
@FunctionalInterface
public interface CacheWarmer<K, V> {

    /**
     * Perform warm-up and return data to load
     * 执行预热并返回要加载的数据
     *
     * @return map of key-value pairs to load | 要加载的键值对 Map
     */
    Map<K, V> warmUp();

    /**
     * Async warm-up
     * 异步预热
     *
     * @param executor the executor | 执行器
     * @return future containing data | 包含数据的 Future
     */
    default CompletableFuture<Map<K, V>> warmUpAsync(Executor executor) {
        return CompletableFuture.supplyAsync(this::warmUp, executor);
    }

    /**
     * Get warm-up priority (lower = higher priority)
     * 获取预热优先级（数值越小优先级越高）
     *
     * @return priority | 优先级
     */
    default int priority() {
        return 0;
    }

    /**
     * Check if warming is enabled
     * 检查是否启用预热
     *
     * @return true if enabled | 启用返回 true
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Called when warm-up completes
     * 预热完成时调用
     *
     * @param loadedCount number of entries loaded | 加载的条目数
     * @param duration    warm-up duration | 预热耗时
     */
    default void onComplete(int loadedCount, Duration duration) {
    }

    /**
     * Called when warm-up fails
     * 预热失败时调用
     *
     * @param cause failure cause | 失败原因
     */
    default void onError(Throwable cause) {
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create warmer from supplier
     * 从 Supplier 创建预热器
     *
     * @param supplier data supplier | 数据供应器
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return warmer | 预热器
     */
    static <K, V> CacheWarmer<K, V> from(Supplier<Map<K, V>> supplier) {
        return supplier::get;
    }

    /**
     * Create paged loading warmer
     * 创建分页加载预热器
     *
     * @param pageLoader page loader function | 分页加载函数
     * @param pageSize   page size | 页大小
     * @param maxPages   max pages to load | 最大加载页数
     * @param <K>        key type | 键类型
     * @param <V>        value type | 值类型
     * @return warmer | 预热器
     */
    static <K, V> CacheWarmer<K, V> paged(
            Function<Integer, Map<K, V>> pageLoader,
            int pageSize,
            int maxPages) {
        return () -> {
            Map<K, V> result = new HashMap<>();
            for (int i = 0; i < maxPages; i++) {
                Map<K, V> page = pageLoader.apply(i * pageSize);
                if (page.isEmpty()) {
                    break;
                }
                result.putAll(page);
            }
            return result;
        };
    }

    /**
     * Create empty warmer
     * 创建空预热器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return empty warmer | 空预热器
     */
    static <K, V> CacheWarmer<K, V> empty() {
        return Map::of;
    }
}
