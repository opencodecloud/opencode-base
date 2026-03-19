package cloud.opencode.base.cache.internal;

import cloud.opencode.base.cache.Cache;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Expiration Scheduler - Background cleanup for expired cache entries
 * 过期调度器 - 后台清理过期缓存条目
 *
 * <p>Provides efficient background expiration cleanup using a single shared
 * scheduler thread. Uses weak references to prevent memory leaks when caches
 * are garbage collected.</p>
 * <p>使用单个共享调度线程提供高效的后台过期清理。使用弱引用防止缓存被垃圾回收时的内存泄漏。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lazy initialization - 延迟初始化</li>
 *   <li>Configurable cleanup interval - 可配置清理间隔</li>
 *   <li>Automatic deregistration on cache GC - 缓存 GC 时自动取消注册</li>
 *   <li>Graceful shutdown - 优雅关闭</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Register a cache for expiration cleanup
 * ExpirationScheduler.getInstance().register(cache);
 *
 * // Unregister when cache is no longer needed
 * ExpirationScheduler.getInstance().unregister(cache.name());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (singleton with ConcurrentHashMap) - 线程安全: 是（单例模式，使用 ConcurrentHashMap）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class ExpirationScheduler {

    private static final System.Logger LOGGER = System.getLogger(ExpirationScheduler.class.getName());
    private static final Duration DEFAULT_CLEANUP_INTERVAL = Duration.ofSeconds(1);
    private static final ExpirationScheduler INSTANCE = new ExpirationScheduler();

    private final Map<String, CacheCleanupTask<?>> tasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    // Metrics
    private final AtomicLong totalCleanupRuns = new AtomicLong(0);
    private final AtomicLong totalEntriesCleaned = new AtomicLong(0);

    private ExpirationScheduler() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = Thread.ofVirtual().name("cache-expiration-scheduler").unstarted(r);
            return t;
        });

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "cache-scheduler-shutdown"));
    }

    /**
     * Get the singleton instance
     * 获取单例实例
     *
     * @return scheduler instance | 调度器实例
     */
    public static ExpirationScheduler getInstance() {
        return INSTANCE;
    }

    /**
     * Register a cache for background cleanup
     * 注册缓存进行后台清理
     *
     * @param cache    the cache to register | 要注册的缓存
     * @param interval cleanup interval | 清理间隔
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     */
    public <K, V> void register(Cache<K, V> cache, Duration interval) {
        Objects.requireNonNull(cache, "cache cannot be null");
        Objects.requireNonNull(interval, "interval cannot be null");

        if (shutdown.get()) {
            throw new IllegalStateException("Scheduler has been shut down");
        }

        String cacheName = cache.name();
        if (tasks.containsKey(cacheName)) {
            return; // Already registered
        }

        CacheCleanupTask<V> task = new CacheCleanupTask<>(cache, this);
        tasks.put(cacheName, task);

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                task,
                interval.toMillis(),
                interval.toMillis(),
                TimeUnit.MILLISECONDS
        );
        task.setFuture(future);
    }

    /**
     * Register a cache with default cleanup interval
     * 使用默认清理间隔注册缓存
     *
     * @param cache the cache to register | 要注册的缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     */
    public <K, V> void register(Cache<K, V> cache) {
        register(cache, DEFAULT_CLEANUP_INTERVAL);
    }

    /**
     * Unregister a cache from background cleanup
     * 从后台清理中取消注册缓存
     *
     * @param cacheName the cache name | 缓存名称
     */
    public void unregister(String cacheName) {
        CacheCleanupTask<?> task = tasks.remove(cacheName);
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Check if a cache is registered
     * 检查缓存是否已注册
     *
     * @param cacheName the cache name | 缓存名称
     * @return true if registered | 如果已注册返回 true
     */
    public boolean isRegistered(String cacheName) {
        return tasks.containsKey(cacheName);
    }

    /**
     * Get scheduler metrics
     * 获取调度器指标
     *
     * @return metrics | 指标
     */
    public Metrics getMetrics() {
        return new Metrics(
                tasks.size(),
                totalCleanupRuns.get(),
                totalEntriesCleaned.get()
        );
    }

    /**
     * Shutdown the scheduler
     * 关闭调度器
     */
    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            tasks.values().forEach(CacheCleanupTask::cancel);
            tasks.clear();
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    void recordCleanup(long entriesCleaned) {
        totalCleanupRuns.incrementAndGet();
        totalEntriesCleaned.addAndGet(entriesCleaned);
    }

    void onCacheGarbageCollected(String cacheName) {
        tasks.remove(cacheName);
    }

    /**
     * Scheduler metrics
     * 调度器指标
     */
    public record Metrics(
            int registeredCaches,
            long totalCleanupRuns,
            long totalEntriesCleaned
    ) {
        /**
         * Average entries cleaned per run
         * 每次运行平均清理的条目数
         *
         * @return average | 平均值
         */
        public double averageEntriesPerRun() {
            return totalCleanupRuns == 0 ? 0.0 : (double) totalEntriesCleaned / totalCleanupRuns;
        }
    }

    /**
     * Cache cleanup task
     */
    private static class CacheCleanupTask<V> implements Runnable {
        private final WeakReference<Cache<?, V>> cacheRef;
        private final String cacheName;
        private final ExpirationScheduler scheduler;
        private volatile ScheduledFuture<?> future;

        CacheCleanupTask(Cache<?, V> cache, ExpirationScheduler scheduler) {
            this.cacheRef = new WeakReference<>(cache);
            this.cacheName = cache.name();
            this.scheduler = scheduler;
        }

        void setFuture(ScheduledFuture<?> future) {
            this.future = future;
        }

        void cancel() {
            if (future != null) {
                future.cancel(false);
            }
        }

        @Override
        public void run() {
            Cache<?, V> cache = cacheRef.get();
            if (cache == null) {
                // Cache has been garbage collected
                cancel();
                scheduler.onCacheGarbageCollected(cacheName);
                return;
            }

            try {
                long sizeBefore = cache.estimatedSize();
                cache.cleanUp();
                long sizeAfter = cache.estimatedSize();
                long cleaned = Math.max(0, sizeBefore - sizeAfter);
                scheduler.recordCleanup(cleaned);
            } catch (Exception e) {
                // Log but don't propagate - don't want to kill the scheduler
                LOGGER.log(System.Logger.Level.WARNING, "Error during cache cleanup for " + cacheName + ": " + e.getMessage(), e);
            }
        }
    }
}
