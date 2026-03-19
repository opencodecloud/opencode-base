package cloud.opencode.base.cache.warming;

import cloud.opencode.base.cache.Cache;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Cache Warmer - Advanced cache warming with batch loading and progress tracking
 * 缓存预热器 - 具有批量加载和进度跟踪的高级缓存预热
 *
 * <p>Provides sophisticated cache warming capabilities including batch loading,
 * priority-based warming, scheduled warming, and detailed progress monitoring.</p>
 * <p>提供复杂的缓存预热功能，包括批量加载、基于优先级的预热、
 * 定时预热和详细的进度监控。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Batch preloading - 批量预加载</li>
 *   <li>Priority-based warming - 基于优先级的预热</li>
 *   <li>Scheduled warming - 定时预热</li>
 *   <li>Progress tracking - 进度跟踪</li>
 *   <li>Incremental warming - 增量预热</li>
 *   <li>Parallel loading - 并行加载</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create warmer
 * CacheWarmer<String, User> warmer = CacheWarmer.<String, User>builder()
 *     .cache(userCache)
 *     .loader(userId -> userRepository.findById(userId))
 *     .batchSize(100)
 *     .parallelism(4)
 *     .build();
 *
 * // Warm with keys
 * WarmingResult result = warmer.warm(userIds);
 *
 * // Warm with progress callback
 * warmer.warmAsync(userIds, progress -> {
 *     System.out.println("Progress: " + progress.percentComplete() + "%");
 * });
 *
 * // Schedule periodic warming
 * warmer.scheduleWarming(Duration.ofHours(1), () -> getHotKeys());
 * }</pre>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.5
 */
public class CacheWarmer<K, V> implements AutoCloseable {

    private static final System.Logger LOGGER = System.getLogger(CacheWarmer.class.getName());

    private final Cache<K, V> cache;
    private final Function<K, V> loader;
    private final Function<Set<K>, Map<K, V>> batchLoader;
    private final int batchSize;
    private final int parallelism;
    private final WarmingEventHandler<K, V> eventHandler;

    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    // Statistics
    private final AtomicLong totalWarmed = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);
    private final AtomicLong totalSkipped = new AtomicLong(0);

    private CacheWarmer(Cache<K, V> cache, Function<K, V> loader,
                       Function<Set<K>, Map<K, V>> batchLoader,
                       int batchSize, int parallelism,
                       WarmingEventHandler<K, V> eventHandler) {
        this.cache = cache;
        this.loader = loader;
        this.batchLoader = batchLoader;
        this.batchSize = batchSize;
        this.parallelism = parallelism;
        this.eventHandler = eventHandler;

        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "CacheWarmer-Scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a builder
     * 创建构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    // ==================== Warming Operations | 预热操作 ====================

    /**
     * Warm cache with provided keys (synchronous)
     * 用提供的键预热缓存（同步）
     *
     * @param keys keys to warm | 要预热的键
     * @return warming result | 预热结果
     */
    public WarmingResult<K> warm(Iterable<? extends K> keys) {
        return warm(keys, WarmingOptions.defaults());
    }

    /**
     * Warm cache with options
     * 使用选项预热缓存
     *
     * @param keys    keys to warm | 要预热的键
     * @param options warming options | 预热选项
     * @return warming result | 预热结果
     */
    public WarmingResult<K> warm(Iterable<? extends K> keys, WarmingOptions options) {
        List<K> keyList = new ArrayList<>();
        for (K key : keys) {
            keyList.add(key);
        }

        WarmingProgress<K> progress = new WarmingProgress<>(keyList.size());
        eventHandler.onStart(keyList.size());

        try {
            if (batchLoader != null && keyList.size() > batchSize) {
                warmBatch(keyList, options, progress);
            } else {
                warmIndividual(keyList, options, progress);
            }
        } finally {
            eventHandler.onComplete(progress.toResult());
        }

        return progress.toResult();
    }

    /**
     * Warm cache asynchronously
     * 异步预热缓存
     *
     * @param keys             keys to warm | 要预热的键
     * @param progressCallback callback for progress updates | 进度更新回调
     * @return future with result | 带结果的 Future
     */
    public CompletableFuture<WarmingResult<K>> warmAsync(
            Iterable<? extends K> keys,
            ProgressCallback<K> progressCallback) {

        return CompletableFuture.supplyAsync(() -> {
            List<K> keyList = new ArrayList<>();
            for (K key : keys) {
                keyList.add(key);
            }

            WarmingProgress<K> progress = new WarmingProgress<>(keyList.size());
            progress.setCallback(progressCallback);
            eventHandler.onStart(keyList.size());

            try {
                if (batchLoader != null && keyList.size() > batchSize) {
                    warmBatch(keyList, WarmingOptions.defaults(), progress);
                } else {
                    warmIndividual(keyList, WarmingOptions.defaults(), progress);
                }
            } finally {
                eventHandler.onComplete(progress.toResult());
            }

            return progress.toResult();
        }, executor);
    }

    private void warmIndividual(List<K> keys, WarmingOptions options, WarmingProgress<K> progress) {
        for (K key : keys) {
            if (closed.get()) {
                break;
            }

            // Skip if already cached and not forcing refresh
            if (!options.forceRefresh() && cache.containsKey(key)) {
                progress.recordSkipped(key);
                totalSkipped.incrementAndGet();
                continue;
            }

            try {
                V value = loader.apply(key);
                if (value != null) {
                    cache.put(key, value);
                    progress.recordSuccess(key);
                    totalWarmed.incrementAndGet();
                    eventHandler.onKeyWarmed(key, value);
                } else {
                    progress.recordSkipped(key);
                    totalSkipped.incrementAndGet();
                }
            } catch (Exception e) {
                progress.recordFailure(key, e);
                totalFailed.incrementAndGet();
                eventHandler.onKeyFailed(key, e);
            }
        }
    }

    private void warmBatch(List<K> keys, WarmingOptions options, WarmingProgress<K> progress) {
        // Split into batches
        List<List<K>> batches = new ArrayList<>();
        for (int i = 0; i < keys.size(); i += batchSize) {
            batches.add(keys.subList(i, Math.min(i + batchSize, keys.size())));
        }

        // Process batches in parallel
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (List<K> batch : batches) {
            if (closed.get()) {
                break;
            }

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // Filter out already cached keys unless forcing refresh
                    Set<K> keysToLoad = new LinkedHashSet<>();
                    for (K key : batch) {
                        if (options.forceRefresh() || !cache.containsKey(key)) {
                            keysToLoad.add(key);
                        } else {
                            progress.recordSkipped(key);
                            totalSkipped.incrementAndGet();
                        }
                    }

                    if (keysToLoad.isEmpty()) {
                        return;
                    }

                    Map<K, V> loaded = batchLoader.apply(keysToLoad);
                    for (K key : keysToLoad) {
                        V value = loaded.get(key);
                        if (value != null) {
                            cache.put(key, value);
                            progress.recordSuccess(key);
                            totalWarmed.incrementAndGet();
                            eventHandler.onKeyWarmed(key, value);
                        } else {
                            progress.recordSkipped(key);
                            totalSkipped.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    for (K key : batch) {
                        progress.recordFailure(key, e);
                        totalFailed.incrementAndGet();
                        eventHandler.onKeyFailed(key, e);
                    }
                }
            }, executor);

            futures.add(future);

            // Limit parallel batches
            if (futures.size() >= parallelism) {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                futures.clear();
            }
        }

        // Wait for remaining
        if (!futures.isEmpty()) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }

    // ==================== Scheduled Warming | 定时预热 ====================

    /**
     * Schedule periodic warming
     * 安排定期预热
     *
     * @param interval     warming interval | 预热间隔
     * @param keySupplier  supplier for keys to warm | 要预热的键的供应者
     * @return scheduled task handle | 定时任务句柄
     */
    public ScheduledFuture<?> scheduleWarming(Duration interval, Supplier<Iterable<? extends K>> keySupplier) {
        return scheduler.scheduleAtFixedRate(() -> {
            try {
                Iterable<? extends K> keys = keySupplier.get();
                warm(keys);
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING, "Scheduled warming failed: " + e.getMessage(), e);
            }
        }, 0, interval.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Schedule warming with cron-like expression
     * 使用类似 cron 的表达式安排预热
     *
     * @param initialDelay initial delay | 初始延迟
     * @param interval     interval between runs | 运行间隔
     * @param keySupplier  supplier for keys | 键的供应者
     * @return scheduled task handle | 定时任务句柄
     */
    public ScheduledFuture<?> scheduleWarming(Duration initialDelay, Duration interval,
                                              Supplier<Iterable<? extends K>> keySupplier) {
        return scheduler.scheduleAtFixedRate(() -> {
            try {
                Iterable<? extends K> keys = keySupplier.get();
                warm(keys);
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING, "Scheduled warming failed: " + e.getMessage(), e);
            }
        }, initialDelay.toMillis(), interval.toMillis(), TimeUnit.MILLISECONDS);
    }

    // ==================== Priority Warming | 优先级预热 ====================

    /**
     * Warm with priority queue (high priority keys first)
     * 使用优先级队列预热（先预热高优先级键）
     *
     * @param priorityKeys keys with priorities | 带优先级的键
     * @return warming result | 预热结果
     */
    public WarmingResult<K> warmWithPriority(List<PriorityKey<K>> priorityKeys) {
        // Sort by priority (higher first)
        List<PriorityKey<K>> sorted = new ArrayList<>(priorityKeys);
        sorted.sort(Comparator.comparingInt(PriorityKey<K>::priority).reversed());

        List<K> orderedKeys = sorted.stream().map(PriorityKey::key).toList();
        return warm(orderedKeys);
    }

    // ==================== Statistics | 统计 ====================

    /**
     * Get warming statistics
     * 获取预热统计
     *
     * @return statistics | 统计
     */
    public WarmingStats getStats() {
        return new WarmingStats(
                totalWarmed.get(),
                totalFailed.get(),
                totalSkipped.get()
        );
    }

    /**
     * Reset statistics
     * 重置统计
     */
    public void resetStats() {
        totalWarmed.set(0);
        totalFailed.set(0);
        totalSkipped.set(0);
    }

    // ==================== Lifecycle | 生命周期 ====================

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            scheduler.shutdown();
            executor.shutdown();
            try {
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ==================== Inner Classes | 内部类 ====================

    /**
     * Key with priority for priority-based warming
     * 用于基于优先级预热的带优先级的键
     *
     * @param key the cache key | 缓存键
     * @param priority the warming priority | 预热优先级
     * @param <K> the key type | 键类型
     */
    public record PriorityKey<K>(K key, int priority) {
        /**
         * Creates a PriorityKey | 创建优先级键
         *
         * @param key the key | 键
         * @param priority the priority | 优先级
         * @param <K> the key type | 键类型
         * @return the priority key | 优先级键
         */
        public static <K> PriorityKey<K> of(K key, int priority) {
            return new PriorityKey<>(key, priority);
        }
    }

    /**
     * Warming options
     * 预热选项
     *
     * @param forceRefresh whether to force refresh existing entries | 是否强制刷新现有条目
     * @param stopOnError whether to stop on first error | 是否在首个错误时停止
     * @param timeout the warming timeout | 预热超时时间
     */
    public record WarmingOptions(
            boolean forceRefresh,
            boolean stopOnError,
            Duration timeout
    ) {
        /**
         * Returns default warming options | 返回默认预热选项
         * @return default options | 默认选项
         */
        public static WarmingOptions defaults() {
            return new WarmingOptions(false, false, Duration.ofMinutes(30));
        }

        /**
         * Creates a new Builder | 创建新的构建器
         * @return builder | 构建器
         */
        public static Builder builder() {
            return new Builder();
        }

        /** Builder for WarmingOptions | WarmingOptions 构建器 */
        public static class Builder {

            /** Creates a new Builder instance | 创建新的构建器实例 */
            public Builder() {}

            private boolean forceRefresh = false;
            private boolean stopOnError = false;
            private Duration timeout = Duration.ofMinutes(30);

            /**
             * Sets force refresh | 设置强制刷新
             * @param force true to force | true 为强制
             * @return this builder | 此构建器
             */
            public Builder forceRefresh(boolean force) {
                this.forceRefresh = force;
                return this;
            }

            /**
             * Sets stop on error | 设置出错停止
             * @param stop true to stop | true 为停止
             * @return this builder | 此构建器
             */
            public Builder stopOnError(boolean stop) {
                this.stopOnError = stop;
                return this;
            }

            /**
             * Sets the timeout | 设置超时时间
             * @param timeout the timeout | 超时时间
             * @return this builder | 此构建器
             */
            public Builder timeout(Duration timeout) {
                this.timeout = timeout;
                return this;
            }

            /**
             * Builds the WarmingOptions | 构建预热选项
             * @return warming options | 预热选项
             */
            public WarmingOptions build() {
                return new WarmingOptions(forceRefresh, stopOnError, timeout);
            }
        }
    }

    /**
     * Warming result
     * 预热结果
     *
     * @param totalKeys the total number of keys | 总键数
     * @param warmedCount the number of successfully warmed keys | 成功预热的键数
     * @param failedCount the number of failed keys | 失败的键数
     * @param skippedCount the number of skipped keys | 跳过的键数
     * @param failedKeys the set of failed keys | 失败的键集合
     * @param duration the warming duration | 预热持续时间
     * @param <K> the key type | 键类型
     */
    public record WarmingResult<K>(
            int totalKeys,
            int warmedCount,
            int failedCount,
            int skippedCount,
            Set<K> failedKeys,
            Duration duration
    ) {
        /**
         * Returns the success rate | 返回成功率
         * @return success rate (0.0 to 1.0) | 成功率
         */
        public double successRate() {
            int attempted = warmedCount + failedCount;
            return attempted > 0 ? (double) warmedCount / attempted : 1.0;
        }

        /**
         * Returns whether warming is complete | 返回预热是否完成
         * @return true if complete | 完成返回 true
         */
        public boolean isComplete() {
            return failedCount == 0;
        }

        /**
         * Returns the completion percentage | 返回完成百分比
         * @return percent complete | 完成百分比
         */
        public double percentComplete() {
            return totalKeys > 0 ? (double) (warmedCount + skippedCount) / totalKeys * 100 : 100;
        }
    }

    /**
     * Warming progress tracker
     * 预热进度跟踪器
     */
    private static class WarmingProgress<K> {
        private final int totalKeys;
        private final AtomicInteger warmedCount = new AtomicInteger(0);
        private final AtomicInteger failedCount = new AtomicInteger(0);
        private final AtomicInteger skippedCount = new AtomicInteger(0);
        private final Set<K> failedKeys = ConcurrentHashMap.newKeySet();
        private final Instant startTime = Instant.now();
        private volatile ProgressCallback<K> callback;

        WarmingProgress(int totalKeys) {
            this.totalKeys = totalKeys;
        }

        void setCallback(ProgressCallback<K> callback) {
            this.callback = callback;
        }

        void recordSuccess(K key) {
            warmedCount.incrementAndGet();
            notifyProgress();
        }

        void recordFailure(K key, Exception e) {
            failedCount.incrementAndGet();
            failedKeys.add(key);
            notifyProgress();
        }

        void recordSkipped(K key) {
            skippedCount.incrementAndGet();
            notifyProgress();
        }

        private void notifyProgress() {
            if (callback != null) {
                callback.onProgress(new ProgressSnapshot<>(
                        totalKeys,
                        warmedCount.get(),
                        failedCount.get(),
                        skippedCount.get(),
                        Duration.between(startTime, Instant.now())
                ));
            }
        }

        WarmingResult<K> toResult() {
            return new WarmingResult<>(
                    totalKeys,
                    warmedCount.get(),
                    failedCount.get(),
                    skippedCount.get(),
                    new HashSet<>(failedKeys),
                    Duration.between(startTime, Instant.now())
            );
        }
    }

    /**
     * Progress snapshot for callbacks
     * 用于回调的进度快照
     *
     * @param totalKeys the total number of keys | 总键数
     * @param warmedCount the number of warmed keys | 已预热的键数
     * @param failedCount the number of failed keys | 失败的键数
     * @param skippedCount the number of skipped keys | 跳过的键数
     * @param elapsed the elapsed time | 已用时间
     * @param <K> the key type | 键类型
     */
    public record ProgressSnapshot<K>(
            int totalKeys,
            int warmedCount,
            int failedCount,
            int skippedCount,
            Duration elapsed
    ) {
        /**
         * Returns the completion percentage | 返回完成百分比
         * @return percent complete | 完成百分比
         */
        public double percentComplete() {
            return totalKeys > 0 ? (double) (warmedCount + skippedCount) / totalKeys * 100 : 100;
        }

        /**
         * Returns the remaining count | 返回剩余数
         * @return remaining keys | 剩余键数
         */
        public int remaining() {
            return totalKeys - warmedCount - failedCount - skippedCount;
        }
    }

    /**
     * Progress callback interface
     * 进度回调接口
     *
     * @param <K> the key type | 键类型
     */
    @FunctionalInterface
    public interface ProgressCallback<K> {
        /**
         * Called on warming progress update | 预热进度更新时调用
         * @param progress the progress snapshot | 进度快照
         */
        void onProgress(ProgressSnapshot<K> progress);
    }

    /**
     * Warming statistics
     * 预热统计
     *
     * @param totalWarmed total number of warmed entries | 总预热条目数
     * @param totalFailed total number of failed entries | 总失败条目数
     * @param totalSkipped total number of skipped entries | 总跳过条目数
     */
    public record WarmingStats(
            long totalWarmed,
            long totalFailed,
            long totalSkipped
    ) {
        /**
         * Returns the total count | 返回总数
         * @return total count | 总数
         */
        public long total() {
            return totalWarmed + totalFailed + totalSkipped;
        }

        /**
         * Returns the success rate | 返回成功率
         * @return success rate (0.0 to 1.0) | 成功率
         */
        public double successRate() {
            long attempted = totalWarmed + totalFailed;
            return attempted > 0 ? (double) totalWarmed / attempted : 1.0;
        }
    }

    /**
     * Warming event handler
     * 预热事件处理器
     *
     * @param <K> the key type | 键类型
     * @param <V> the value type | 值类型
     */
    public interface WarmingEventHandler<K, V> {
        /**
         * Called when warming starts | 预热开始时调用
         * @param totalKeys total keys to warm | 要预热的总键数
         */
        default void onStart(int totalKeys) {}
        /**
         * Called when a key is warmed | 键预热完成时调用
         * @param key the key | 键
         * @param value the loaded value | 加载的值
         */
        default void onKeyWarmed(K key, V value) {}
        /**
         * Called when a key fails to warm | 键预热失败时调用
         * @param key the key | 键
         * @param error the error | 错误
         */
        default void onKeyFailed(K key, Exception error) {}
        /**
         * Called when warming completes | 预热完成时调用
         * @param result the warming result | 预热结果
         */
        default void onComplete(WarmingResult<K> result) {}

        /**
         * Returns a no-op event handler | 返回空操作事件处理器
         *
         * @param <K> the key type | 键类型
         * @param <V> the value type | 值类型
         * @return no-op handler | 空操作处理器
         */
        static <K, V> WarmingEventHandler<K, V> noOp() { return new WarmingEventHandler<>() {}; }
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for CacheWarmer
     * CacheWarmer 构建器
     *
     * @param <K> the key type | 键类型
     * @param <V> the value type | 值类型
     */
    public static class Builder<K, V> {

        /** Creates a new Builder instance | 创建新的构建器实例 */
        public Builder() {}

        private Cache<K, V> cache;
        private Function<K, V> loader;
        private Function<Set<K>, Map<K, V>> batchLoader;
        private int batchSize = 100;
        private int parallelism = 4;
        private WarmingEventHandler<K, V> eventHandler = WarmingEventHandler.noOp();

        /**
         * Sets the cache | 设置缓存
         * @param cache the cache | 缓存
         * @return this builder | 此构建器
         */
        public Builder<K, V> cache(Cache<K, V> cache) {
            this.cache = cache;
            return this;
        }

        /**
         * Sets the loader function | 设置加载函数
         * @param loader the loader | 加载器
         * @return this builder | 此构建器
         */
        public Builder<K, V> loader(Function<K, V> loader) {
            this.loader = loader;
            return this;
        }

        /**
         * Sets the batch loader function | 设置批量加载函数
         * @param batchLoader the batch loader | 批量加载器
         * @return this builder | 此构建器
         */
        public Builder<K, V> batchLoader(Function<Set<K>, Map<K, V>> batchLoader) {
            this.batchLoader = batchLoader;
            return this;
        }

        /**
         * Sets the batch size | 设置批量大小
         * @param size the batch size | 批量大小
         * @return this builder | 此构建器
         */
        public Builder<K, V> batchSize(int size) {
            this.batchSize = size;
            return this;
        }

        /**
         * Sets the parallelism level | 设置并行度
         * @param parallelism the parallelism | 并行度
         * @return this builder | 此构建器
         */
        public Builder<K, V> parallelism(int parallelism) {
            this.parallelism = parallelism;
            return this;
        }

        /**
         * Sets the event handler | 设置事件处理器
         * @param handler the handler | 处理器
         * @return this builder | 此构建器
         */
        public Builder<K, V> eventHandler(WarmingEventHandler<K, V> handler) {
            this.eventHandler = handler;
            return this;
        }

        /**
         * Builds the CacheWarmer | 构建缓存预热器
         * @return the cache warmer | 缓存预热器
         */
        public CacheWarmer<K, V> build() {
            Objects.requireNonNull(cache, "cache cannot be null");
            Objects.requireNonNull(loader, "loader cannot be null");
            return new CacheWarmer<>(cache, loader, batchLoader, batchSize, parallelism, eventHandler);
        }
    }
}
