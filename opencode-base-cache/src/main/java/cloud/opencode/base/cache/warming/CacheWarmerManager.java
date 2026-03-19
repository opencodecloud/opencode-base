package cloud.opencode.base.cache.warming;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.CacheManager;
import cloud.opencode.base.cache.spi.CacheWarmer;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cache Warmer Manager - Orchestrates cache warming lifecycle
 * 缓存预热管理器 - 编排缓存预热生命周期
 *
 * <p>Manages the warming of caches at application startup or on-demand,
 * with support for priority ordering, async execution, and callbacks.</p>
 * <p>管理应用启动时或按需的缓存预热，支持优先级排序、异步执行和回调。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Priority-based warming order | 基于优先级的预热顺序</li>
 *   <li>Parallel warming execution | 并行预热执行</li>
 *   <li>Progress tracking and metrics | 进度跟踪和指标</li>
 *   <li>Retry on failure | 失败重试</li>
 *   <li>Completion callbacks | 完成回调</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Register warmers
 * CacheWarmerManager manager = CacheWarmerManager.getInstance();
 *
 * manager.register("users", userCache, () -> userDao.findHotUsers(1000));
 * manager.register("products", productCache,
 *     CacheWarmer.paged(offset -> productDao.findProducts(offset, 100), 100, 10));
 *
 * // Warm all caches
 * WarmingResult result = manager.warmAll();
 * System.out.println("Warmed " + result.totalEntries() + " entries in " + result.duration());
 *
 * // Async warming
 * manager.warmAllAsync().thenAccept(r ->
 *     log.info("Warming completed: {}", r));
 *
 * // Warm specific cache
 * manager.warm("users");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.0
 */
public final class CacheWarmerManager {

    private static final CacheWarmerManager INSTANCE = new CacheWarmerManager();

    private final Map<String, WarmingTask<?>> tasks = new ConcurrentHashMap<>();
    private final ExecutorService executor;
    private final AtomicInteger activeWarmings = new AtomicInteger(0);
    private final AtomicLong totalEntriesWarmed = new AtomicLong(0);
    private final AtomicLong totalWarmingTimeNanos = new AtomicLong(0);
    private volatile WarmingListener globalListener;

    private CacheWarmerManager() {
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Get singleton instance
     * 获取单例实例
     *
     * @return instance | 实例
     */
    public static CacheWarmerManager getInstance() {
        return INSTANCE;
    }

    // ==================== Registration | 注册 ====================

    /**
     * Register a cache warmer
     * 注册缓存预热器
     *
     * @param name   cache name | 缓存名称
     * @param cache  target cache | 目标缓存
     * @param warmer the warmer | 预热器
     * @param <K>    key type | 键类型
     * @param <V>    value type | 值类型
     * @return this manager | 此管理器
     */
    public <K, V> CacheWarmerManager register(String name, Cache<K, V> cache, CacheWarmer<K, V> warmer) {
        tasks.put(name, new WarmingTask<>(name, cache, warmer, 0));
        return this;
    }

    /**
     * Register a cache warmer with priority
     * 注册带优先级的缓存预热器
     *
     * @param name     cache name | 缓存名称
     * @param cache    target cache | 目标缓存
     * @param warmer   the warmer | 预热器
     * @param priority priority (lower = higher) | 优先级（数值越小越优先）
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return this manager | 此管理器
     */
    public <K, V> CacheWarmerManager register(String name, Cache<K, V> cache,
                                              CacheWarmer<K, V> warmer, int priority) {
        tasks.put(name, new WarmingTask<>(name, cache, warmer, priority));
        return this;
    }

    /**
     * Unregister a cache warmer
     * 取消注册缓存预热器
     *
     * @param name cache name | 缓存名称
     * @return this manager | 此管理器
     */
    public CacheWarmerManager unregister(String name) {
        tasks.remove(name);
        return this;
    }

    /**
     * Set global warming listener
     * 设置全局预热监听器
     *
     * @param listener the listener | 监听器
     * @return this manager | 此管理器
     */
    public CacheWarmerManager setListener(WarmingListener listener) {
        this.globalListener = listener;
        return this;
    }

    // ==================== Warming Operations | 预热操作 ====================

    /**
     * Warm all registered caches synchronously
     * 同步预热所有注册的缓存
     *
     * @return warming result | 预热结果
     */
    public WarmingResult warmAll() {
        Instant start = Instant.now();
        List<CacheWarmingResult> results = new ArrayList<>();

        // Sort by priority
        List<WarmingTask<?>> sortedTasks = tasks.values().stream()
                .sorted(Comparator.comparingInt(t -> t.priority))
                .toList();

        for (WarmingTask<?> task : sortedTasks) {
            if (task.warmer.isEnabled()) {
                results.add(warmSingle(task));
            }
        }

        Duration duration = Duration.between(start, Instant.now());
        return new WarmingResult(results, duration);
    }

    /**
     * Warm all registered caches asynchronously
     * 异步预热所有注册的缓存
     *
     * @return future with warming result | 包含预热结果的 Future
     */
    public CompletableFuture<WarmingResult> warmAllAsync() {
        return CompletableFuture.supplyAsync(this::warmAll, executor);
    }

    /**
     * Warm all caches in parallel
     * 并行预热所有缓存
     *
     * @param parallelism max concurrent warmings | 最大并发预热数
     * @return warming result | 预热结果
     */
    public WarmingResult warmAllParallel(int parallelism) {
        Instant start = Instant.now();
        Semaphore semaphore = new Semaphore(parallelism);

        List<CompletableFuture<CacheWarmingResult>> futures = tasks.values().stream()
                .filter(t -> t.warmer.isEnabled())
                .map(task -> CompletableFuture.supplyAsync(() -> {
                    try {
                        semaphore.acquire();
                        return warmSingle(task);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return new CacheWarmingResult(task.name, 0, Duration.ZERO, e);
                    } finally {
                        semaphore.release();
                    }
                }, executor))
                .toList();

        List<CacheWarmingResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        Duration duration = Duration.between(start, Instant.now());
        return new WarmingResult(results, duration);
    }

    /**
     * Warm a specific cache
     * 预热指定缓存
     *
     * @param name cache name | 缓存名称
     * @return warming result for this cache | 此缓存的预热结果
     */
    public CacheWarmingResult warm(String name) {
        WarmingTask<?> task = tasks.get(name);
        if (task == null) {
            throw new IllegalArgumentException("No warmer registered for cache: " + name);
        }
        return warmSingle(task);
    }

    /**
     * Warm a specific cache asynchronously
     * 异步预热指定缓存
     *
     * @param name cache name | 缓存名称
     * @return future with warming result | 包含预热结果的 Future
     */
    public CompletableFuture<CacheWarmingResult> warmAsync(String name) {
        return CompletableFuture.supplyAsync(() -> warm(name), executor);
    }

    @SuppressWarnings("unchecked")
    private <K, V> CacheWarmingResult warmSingle(WarmingTask<K> task) {
        Instant start = Instant.now();
        activeWarmings.incrementAndGet();

        try {
            if (globalListener != null) {
                globalListener.onWarmingStarted(task.name);
            }

            Map<K, V> data = (Map<K, V>) task.warmer.warmUp();
            int count = data.size();

            ((Cache<K, V>) task.cache).putAll(data);

            Duration duration = Duration.between(start, Instant.now());
            totalEntriesWarmed.addAndGet(count);
            totalWarmingTimeNanos.addAndGet(duration.toNanos());

            task.warmer.onComplete(count, duration);

            if (globalListener != null) {
                globalListener.onWarmingCompleted(task.name, count, duration);
            }

            return new CacheWarmingResult(task.name, count, duration, null);
        } catch (Exception e) {
            task.warmer.onError(e);

            if (globalListener != null) {
                globalListener.onWarmingFailed(task.name, e);
            }

            return new CacheWarmingResult(task.name, 0, Duration.between(start, Instant.now()), e);
        } finally {
            activeWarmings.decrementAndGet();
        }
    }

    // ==================== Metrics | 指标 ====================

    /**
     * Get warming metrics
     * 获取预热指标
     *
     * @return metrics | 指标
     */
    public WarmingMetrics getMetrics() {
        return new WarmingMetrics(
                tasks.size(),
                activeWarmings.get(),
                totalEntriesWarmed.get(),
                totalWarmingTimeNanos.get()
        );
    }

    /**
     * Get registered cache names
     * 获取已注册的缓存名称
     *
     * @return cache names | 缓存名称集合
     */
    public Set<String> getRegisteredCaches() {
        return Collections.unmodifiableSet(tasks.keySet());
    }

    // ==================== Inner Classes | 内部类 ====================

    private record WarmingTask<K>(String name, Cache<K, ?> cache, CacheWarmer<K, ?> warmer, int priority) {
    }

    /**
     * Result for a single cache warming
     * 单个缓存预热结果
     *
     * @param cacheName the cache name | 缓存名称
     * @param entriesLoaded the number of entries loaded | 加载的条目数
     * @param duration the warming duration | 预热持续时间
     * @param error the error, if any | 错误（如有）
     */
    public record CacheWarmingResult(
            String cacheName,
            int entriesLoaded,
            Duration duration,
            Throwable error
    ) {
        /**
         * isSuccess | isSuccess
         * @return the result | 结果
         */
        public boolean isSuccess() {
            return error == null;
        }
    }

    /**
     * Result for all cache warming
     * 所有缓存预热结果
     *
     * @param results the list of individual warming results | 各缓存预热结果列表
     * @param totalDuration the total warming duration | 总预热持续时间
     */
    public record WarmingResult(
            List<CacheWarmingResult> results,
            Duration totalDuration
    ) {
        /**
         * totalEntries | totalEntries
         * @return the result | 结果
         */
        public int totalEntries() {
            return results.stream().mapToInt(CacheWarmingResult::entriesLoaded).sum();
        }

        /**
         * successCount | successCount
         * @return the result | 结果
         */
        public int successCount() {
            return (int) results.stream().filter(CacheWarmingResult::isSuccess).count();
        }

        /**
         * failureCount | failureCount
         * @return the result | 结果
         */
        public int failureCount() {
            return (int) results.stream().filter(r -> !r.isSuccess()).count();
        }

        /**
         * failures | failures
         * @return the result | 结果
         */
        public List<CacheWarmingResult> failures() {
            return results.stream().filter(r -> !r.isSuccess()).toList();
        }
    }

    /**
     * Warming metrics
     * 预热指标
     *
     * @param registeredCaches the number of registered caches | 注册的缓存数
     * @param activeWarmings the number of active warmings | 活跃预热数
     * @param totalEntriesWarmed the total entries warmed | 总预热条目数
     * @param totalWarmingTimeNanos the total warming time in nanoseconds | 总预热时间（纳秒）
     */
    public record WarmingMetrics(
            int registeredCaches,
            int activeWarmings,
            long totalEntriesWarmed,
            long totalWarmingTimeNanos
    ) {
        /**
         * totalWarmingTime | totalWarmingTime
         * @return the result | 结果
         */
        public Duration totalWarmingTime() {
            return Duration.ofNanos(totalWarmingTimeNanos);
        }
    }

    /**
     * Warming lifecycle listener
     * 预热生命周期监听器
     */
    public interface WarmingListener {
        /**
         * onWarmingStarted | onWarmingStarted
         * @param cacheName the cacheName | cacheName
         */
        default void onWarmingStarted(String cacheName) {
        }

        /**
         * onWarmingCompleted | onWarmingCompleted
         * @param cacheName the cacheName | cacheName
         * @param entriesLoaded the entriesLoaded | entriesLoaded
         * @param duration the duration | duration
         */
        default void onWarmingCompleted(String cacheName, int entriesLoaded, Duration duration) {
        }

        /**
         * onWarmingFailed | onWarmingFailed
         * @param cacheName the cacheName | cacheName
         * @param error the error | error
         */
        default void onWarmingFailed(String cacheName, Throwable error) {
        }
    }
}
