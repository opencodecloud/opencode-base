package cloud.opencode.base.cache.dlq;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Dead Letter Queue - Tracks failed cache load operations
 * 死信队列 - 跟踪失败的缓存加载操作
 *
 * <p>Manages keys that failed to load, with support for retry scheduling,
 * exponential backoff, and failure analysis.</p>
 * <p>管理加载失败的键，支持重试调度、指数退避和故障分析。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Failed load tracking - 失败加载跟踪</li>
 *   <li>Automatic retry with backoff - 带退避的自动重试</li>
 *   <li>Failure pattern analysis - 故障模式分析</li>
 *   <li>Manual drain operations - 手动排空操作</li>
 *   <li>Alerting integration - 告警集成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create DLQ
 * DeadLetterQueue<String> dlq = DeadLetterQueue.<String>builder()
 *     .maxRetries(3)
 *     .initialBackoff(Duration.ofSeconds(1))
 *     .maxBackoff(Duration.ofMinutes(5))
 *     .retryLoader(key -> loadFromBackend(key))
 *     .build();
 *
 * // Add failed key
 * dlq.add("user:1001", new IOException("Connection refused"));
 *
 * // Process retries automatically
 * dlq.startRetryProcessor();
 *
 * // Or drain manually
 * List<FailedEntry<String>> failed = dlq.drain(10);
 *
 * // Get failure analysis
 * DlqAnalysis analysis = dlq.analyze();
 * }</pre>
 *
 * @param <K> key type | 键类型
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
public class DeadLetterQueue<K> implements AutoCloseable {

    private final ConcurrentHashMap<K, FailedEntry<K>> entries = new ConcurrentHashMap<>();
    private final int maxRetries;
    private final Duration initialBackoff;
    private final Duration maxBackoff;
    private final double backoffMultiplier;
    private final int maxQueueSize;
    private final Function<K, ?> retryLoader;
    private final DlqEventHandler<K> eventHandler;

    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicBoolean retryProcessorRunning = new AtomicBoolean(false);

    // Statistics
    private final AtomicLong totalAdded = new AtomicLong(0);
    private final AtomicLong totalRetried = new AtomicLong(0);
    private final AtomicLong totalRecovered = new AtomicLong(0);
    private final AtomicLong totalDiscarded = new AtomicLong(0);

    private DeadLetterQueue(int maxRetries, Duration initialBackoff, Duration maxBackoff,
                           double backoffMultiplier, int maxQueueSize,
                           Function<K, ?> retryLoader, DlqEventHandler<K> eventHandler) {
        this.maxRetries = maxRetries;
        this.initialBackoff = initialBackoff;
        this.maxBackoff = maxBackoff;
        this.backoffMultiplier = backoffMultiplier;
        this.maxQueueSize = maxQueueSize;
        this.retryLoader = retryLoader;
        this.eventHandler = eventHandler;

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "DLQ-Scheduler");
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
     * @return builder | 构建器
     */
    public static <K> Builder<K> builder() {
        return new Builder<>();
    }

    // ==================== Add Operations | 添加操作 ====================

    /**
     * Add a failed key to the DLQ
     * 将失败的键添加到 DLQ
     *
     * @param key   the failed key | 失败的键
     * @param error the error that caused failure | 导致失败的错误
     * @return true if added, false if max queue size reached | 如果添加成功返回 true，如果达到最大队列大小返回 false
     */
    public boolean add(K key, Throwable error) {
        if (closed.get()) {
            return false;
        }

        if (entries.size() >= maxQueueSize) {
            // Evict oldest entry if at capacity
            evictOldest();
        }

        FailedEntry<K> existing = entries.get(key);
        if (existing != null) {
            // Update existing entry
            existing.recordFailure(error);
        } else {
            entries.put(key, new FailedEntry<>(key, error, initialBackoff, backoffMultiplier, maxBackoff));
            totalAdded.incrementAndGet();
            eventHandler.onAdd(key, error);
        }

        return true;
    }

    /**
     * Add multiple failed keys
     * 添加多个失败的键
     *
     * @param keys  the failed keys | 失败的键
     * @param error the error | 错误
     */
    public void addAll(Iterable<? extends K> keys, Throwable error) {
        for (K key : keys) {
            add(key, error);
        }
    }

    private void evictOldest() {
        entries.entrySet().stream()
                .min(Comparator.comparing(e -> e.getValue().firstFailure))
                .ifPresent(oldest -> {
                    entries.remove(oldest.getKey());
                    totalDiscarded.incrementAndGet();
                    eventHandler.onDiscard(oldest.getKey(), DiscardReason.QUEUE_FULL);
                });
    }

    // ==================== Retry Operations | 重试操作 ====================

    /**
     * Start automatic retry processor
     * 启动自动重试处理器
     */
    public void startRetryProcessor() {
        if (retryProcessorRunning.compareAndSet(false, true)) {
            scheduler.scheduleWithFixedDelay(
                    this::processRetries,
                    initialBackoff.toMillis(),
                    initialBackoff.toMillis(),
                    TimeUnit.MILLISECONDS
            );
        }
    }

    /**
     * Stop automatic retry processor
     * 停止自动重试处理器
     */
    public void stopRetryProcessor() {
        retryProcessorRunning.set(false);
    }

    /**
     * Process pending retries
     * 处理待处理的重试
     *
     * @return number of entries processed | 处理的条目数
     */
    public int processRetries() {
        if (retryLoader == null) {
            return 0;
        }

        int processed = 0;
        Instant now = Instant.now();

        for (Map.Entry<K, FailedEntry<K>> entry : entries.entrySet()) {
            FailedEntry<K> failed = entry.getValue();
            if (failed.isReadyForRetry(now)) {
                processed++;
                retry(failed);
            }
        }

        return processed;
    }

    /**
     * Retry a single failed entry
     * 重试单个失败的条目
     *
     * @param key the key to retry | 要重试的键
     * @return true if recovered, false otherwise | 如果恢复返回 true，否则返回 false
     */
    public boolean retry(K key) {
        FailedEntry<K> entry = entries.get(key);
        if (entry == null) {
            return false;
        }
        return retry(entry);
    }

    private boolean retry(FailedEntry<K> entry) {
        if (retryLoader == null) {
            return false;
        }

        totalRetried.incrementAndGet();
        entry.recordRetryAttempt();

        try {
            retryLoader.apply(entry.key);
            // Success - remove from DLQ
            entries.remove(entry.key);
            totalRecovered.incrementAndGet();
            eventHandler.onRecovered(entry.key, entry.retryCount);
            return true;
        } catch (Exception e) {
            entry.recordFailure(e);
            eventHandler.onRetryFailed(entry.key, e, entry.retryCount);

            // Check if max retries exceeded
            if (entry.retryCount >= maxRetries) {
                entries.remove(entry.key);
                totalDiscarded.incrementAndGet();
                eventHandler.onDiscard(entry.key, DiscardReason.MAX_RETRIES);
            }
            return false;
        }
    }

    // ==================== Drain Operations | 排空操作 ====================

    /**
     * Drain up to N entries from the DLQ
     * 从 DLQ 排空最多 N 个条目
     *
     * @param maxEntries maximum entries to drain | 要排空的最大条目数
     * @return list of drained entries | 排空的条目列表
     */
    public List<FailedEntry<K>> drain(int maxEntries) {
        List<FailedEntry<K>> drained = new ArrayList<>();
        int count = 0;

        for (Iterator<Map.Entry<K, FailedEntry<K>>> it = entries.entrySet().iterator();
             it.hasNext() && count < maxEntries; ) {
            Map.Entry<K, FailedEntry<K>> entry = it.next();
            drained.add(entry.getValue());
            it.remove();
            count++;
        }

        return drained;
    }

    /**
     * Drain all entries
     * 排空所有条目
     *
     * @return list of all entries | 所有条目列表
     */
    public List<FailedEntry<K>> drainAll() {
        List<FailedEntry<K>> drained = new ArrayList<>(entries.values());
        entries.clear();
        return drained;
    }

    /**
     * Drain entries matching predicate
     * 排空匹配谓词的条目
     *
     * @param predicate filter predicate | 过滤谓词
     * @return list of matching entries | 匹配条目列表
     */
    public List<FailedEntry<K>> drainMatching(java.util.function.Predicate<FailedEntry<K>> predicate) {
        List<FailedEntry<K>> drained = new ArrayList<>();

        for (Iterator<Map.Entry<K, FailedEntry<K>>> it = entries.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<K, FailedEntry<K>> entry = it.next();
            if (predicate.test(entry.getValue())) {
                drained.add(entry.getValue());
                it.remove();
            }
        }

        return drained;
    }

    // ==================== Query Operations | 查询操作 ====================

    /**
     * Check if key is in DLQ
     * 检查键是否在 DLQ 中
     *
     * @param key the key | 键
     * @return true if in DLQ | 如果在 DLQ 中返回 true
     */
    public boolean contains(K key) {
        return entries.containsKey(key);
    }

    /**
     * Get failed entry for key
     * 获取键的失败条目
     *
     * @param key the key | 键
     * @return failed entry or null | 失败条目或 null
     */
    public FailedEntry<K> get(K key) {
        return entries.get(key);
    }

    /**
     * Get all failed keys
     * 获取所有失败的键
     *
     * @return set of failed keys | 失败键集合
     */
    public Set<K> getFailedKeys() {
        return new HashSet<>(entries.keySet());
    }

    /**
     * Get current queue size
     * 获取当前队列大小
     *
     * @return queue size | 队列大小
     */
    public int size() {
        return entries.size();
    }

    /**
     * Check if DLQ is empty
     * 检查 DLQ 是否为空
     *
     * @return true if empty | 如果为空返回 true
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    // ==================== Analysis | 分析 ====================

    /**
     * Analyze DLQ contents
     * 分析 DLQ 内容
     *
     * @return analysis result | 分析结果
     */
    public DlqAnalysis<K> analyze() {
        if (entries.isEmpty()) {
            return DlqAnalysis.empty();
        }

        Map<String, Long> errorTypeCounts = new HashMap<>();
        int totalRetryCount = 0;
        Instant oldestFailure = Instant.MAX;
        Instant newestFailure = Instant.MIN;
        List<K> frequentFailures = new ArrayList<>();

        for (FailedEntry<K> entry : entries.values()) {
            // Count error types
            String errorType = entry.lastError != null ?
                    entry.lastError.getClass().getSimpleName() : "Unknown";
            errorTypeCounts.merge(errorType, 1L, Long::sum);

            // Track retry counts
            totalRetryCount += entry.retryCount;

            // Track timestamps
            if (entry.firstFailure.isBefore(oldestFailure)) {
                oldestFailure = entry.firstFailure;
            }
            if (entry.lastFailure.isAfter(newestFailure)) {
                newestFailure = entry.lastFailure;
            }

            // Track frequent failures
            if (entry.failureCount > 3) {
                frequentFailures.add(entry.key);
            }
        }

        return new DlqAnalysis<>(
                entries.size(),
                totalRetryCount,
                (double) totalRetryCount / entries.size(),
                errorTypeCounts,
                oldestFailure,
                newestFailure,
                frequentFailures
        );
    }

    // ==================== Statistics | 统计 ====================

    /**
     * Get DLQ statistics
     * 获取 DLQ 统计
     *
     * @return statistics | 统计
     */
    public DlqStats getStats() {
        return new DlqStats(
                totalAdded.get(),
                totalRetried.get(),
                totalRecovered.get(),
                totalDiscarded.get(),
                entries.size()
        );
    }

    // ==================== Lifecycle | 生命周期 ====================

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
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

    // ==================== Inner Classes | 内部类 ====================

    /**
     * Failed entry record
     * 失败条目记录
     *
     * @param <K> key type | 键类型
     */
    public static class FailedEntry<K> {
        private final K key;
        private final Instant firstFailure;
        private final double backoffMultiplier;
        private final Duration maxBackoff;
        private volatile Instant lastFailure;
        private volatile Throwable lastError;
        private volatile int failureCount;
        private volatile int retryCount;
        private volatile Instant nextRetryTime;
        private volatile Duration currentBackoff;

        FailedEntry(K key, Throwable error, Duration initialBackoff, double backoffMultiplier, Duration maxBackoff) {
            this.key = key;
            this.firstFailure = Instant.now();
            this.lastFailure = this.firstFailure;
            this.lastError = error;
            this.failureCount = 1;
            this.retryCount = 0;
            this.backoffMultiplier = backoffMultiplier;
            this.maxBackoff = maxBackoff;
            this.currentBackoff = initialBackoff;
            this.nextRetryTime = this.firstFailure.plus(currentBackoff);
        }

        // Keep backward-compatible constructor for external use
        FailedEntry(K key, Throwable error) {
            this(key, error, Duration.ofSeconds(1), 2.0, Duration.ofMinutes(5));
        }

        synchronized void recordFailure(Throwable error) {
            this.lastFailure = Instant.now();
            this.lastError = error;
            this.failureCount++;
        }

        synchronized void recordRetryAttempt() {
            this.retryCount++;
            long newMillis = (long) (this.currentBackoff.toMillis() * backoffMultiplier);
            this.currentBackoff = Duration.ofMillis(newMillis);
            if (this.currentBackoff.compareTo(maxBackoff) > 0) {
                this.currentBackoff = maxBackoff;
            }
            this.nextRetryTime = Instant.now().plus(currentBackoff);
        }

        boolean isReadyForRetry(Instant now) {
            return now.isAfter(nextRetryTime);
        }

        /**
         * key | key
         * @return the result | 结果
         */
        public K key() { return key; }
        /**
         * firstFailure | firstFailure
         * @return the result | 结果
         */
        public Instant firstFailure() { return firstFailure; }
        /**
         * lastFailure | lastFailure
         * @return the result | 结果
         */
        public Instant lastFailure() { return lastFailure; }
        /**
         * lastError | lastError
         * @return the result | 结果
         */
        public Throwable lastError() { return lastError; }
        /**
         * failureCount | failureCount
         * @return the result | 结果
         */
        public int failureCount() { return failureCount; }
        /**
         * retryCount | retryCount
         * @return the result | 结果
         */
        public int retryCount() { return retryCount; }
        /**
         * nextRetryTime | nextRetryTime
         * @return the result | 结果
         */
        public Instant nextRetryTime() { return nextRetryTime; }
    }

    /**
     * DLQ analysis result
     * DLQ 分析结果
     *
     * @param totalEntries the total number of entries | 总条目数
     * @param totalRetries the total number of retries | 总重试数
     * @param avgRetriesPerEntry the average retries per entry | 每条目平均重试数
     * @param errorTypeCounts the error type counts | 错误类型计数
     * @param oldestFailure the oldest failure time | 最早失败时间
     * @param newestFailure the newest failure time | 最近失败时间
     * @param frequentFailures the frequently failing keys | 频繁失败的键
     * @param <K> the key type | 键类型
     */
    public record DlqAnalysis<K>(
            int totalEntries,
            int totalRetries,
            double avgRetriesPerEntry,
            Map<String, Long> errorTypeCounts,
            Instant oldestFailure,
            Instant newestFailure,
            List<K> frequentFailures
    ) {
        @SuppressWarnings("unchecked")
        static <K> DlqAnalysis<K> empty() {
            return new DlqAnalysis<>(0, 0, 0, Map.of(), null, null, List.of());
        }

        /**
         * mostCommonErrorType | mostCommonErrorType
         * @return the result | 结果
         */
        public String mostCommonErrorType() {
            return errorTypeCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("None");
        }
    }

    /**
     * DLQ statistics
     * DLQ 统计
     *
     * @param totalAdded total entries added | 总添加数
     * @param totalRetried total entries retried | 总重试数
     * @param totalRecovered total entries recovered | 总恢复数
     * @param totalDiscarded total entries discarded | 总丢弃数
     * @param currentSize current queue size | 当前队列大小
     */
    public record DlqStats(
            long totalAdded,
            long totalRetried,
            long totalRecovered,
            long totalDiscarded,
            int currentSize
    ) {
        /**
         * recoveryRate | recoveryRate
         * @return the result | 结果
         */
        public double recoveryRate() {
            return totalRetried > 0 ? (double) totalRecovered / totalRetried : 0;
        }
    }

    /**
     * Discard reason enumeration
     * 丢弃原因枚举
     */
    public enum DiscardReason {
        /** MAX_RETRIES, */
        MAX_RETRIES,
        /** QUEUE_FULL, */
        QUEUE_FULL,
        /** MANUAL */
        MANUAL
    }

    /**
     * DLQ event handler
     * DLQ 事件处理器
     *
     * @param <K> the key type | 键类型
     */
    public interface DlqEventHandler<K> {
        /**
         * onAdd | onAdd
         * @param key the key | key
         * @param error the error | error
         */
        default void onAdd(K key, Throwable error) {}
        /**
         * onRetryFailed | onRetryFailed
         * @param key the key | key
         * @param error the error | error
         * @param retryCount the retryCount | retryCount
         */
        default void onRetryFailed(K key, Throwable error, int retryCount) {}
        /**
         * onRecovered | onRecovered
         * @param key the key | key
         * @param retryCount the retryCount | retryCount
         */
        default void onRecovered(K key, int retryCount) {}
        /**
         * onDiscard | onDiscard
         * @param key the key | key
         * @param reason the reason | reason
         */
        default void onDiscard(K key, DiscardReason reason) {}

        /**
         * Returns a no-op handler | 返回空操作处理器
         *
         * @param <K> the key type | 键类型
         * @return a no-op handler | 空操作处理器
         */
        static <K> DlqEventHandler<K> noOp() { return new DlqEventHandler<>() {}; }

        /**
         * Returns a logging handler | 返回日志处理器
         *
         * @param <K> the key type | 键类型
         * @return a logging handler | 日志处理器
         */
        static <K> DlqEventHandler<K> logging() {
            System.Logger logger = System.getLogger(DeadLetterQueue.class.getName());
            return new DlqEventHandler<>() {
                @Override
                public void onAdd(K key, Throwable error) {
                    logger.log(System.Logger.Level.INFO, "DLQ: Added " + key + " - " + error.getMessage());
                }
                @Override
                public void onRecovered(K key, int retryCount) {
                    logger.log(System.Logger.Level.INFO, "DLQ: Recovered " + key + " after " + retryCount + " retries");
                }
                @Override
                public void onDiscard(K key, DiscardReason reason) {
                    logger.log(System.Logger.Level.INFO, "DLQ: Discarded " + key + " - " + reason);
                }
            };
        }
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for DeadLetterQueue
     * DeadLetterQueue 构建器
     *
     * @param <K> the key type | 键类型
     */
    public static class Builder<K> {

        /** Creates a new Builder instance | 创建新的 Builder 实例 */
        public Builder() {}

        private int maxRetries = 3;
        private Duration initialBackoff = Duration.ofSeconds(1);
        private Duration maxBackoff = Duration.ofMinutes(5);
        private double backoffMultiplier = 2.0;
        private int maxQueueSize = 10000;
        private Function<K, ?> retryLoader = null;
        private DlqEventHandler<K> eventHandler = DlqEventHandler.noOp();

        /**
         * maxRetries | maxRetries
         * @param maxRetries the maxRetries | maxRetries
         * @return the result | 结果
         */
        public Builder<K> maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * initialBackoff | initialBackoff
         * @param backoff the backoff | backoff
         * @return the result | 结果
         */
        public Builder<K> initialBackoff(Duration backoff) {
            this.initialBackoff = backoff;
            return this;
        }

        /**
         * maxBackoff | maxBackoff
         * @param backoff the backoff | backoff
         * @return the result | 结果
         */
        public Builder<K> maxBackoff(Duration backoff) {
            this.maxBackoff = backoff;
            return this;
        }

        /**
         * backoffMultiplier | backoffMultiplier
         * @param multiplier the multiplier | multiplier
         * @return the result | 结果
         */
        public Builder<K> backoffMultiplier(double multiplier) {
            this.backoffMultiplier = multiplier;
            return this;
        }

        /**
         * maxQueueSize | maxQueueSize
         * @param size the size | size
         * @return the result | 结果
         */
        public Builder<K> maxQueueSize(int size) {
            this.maxQueueSize = size;
            return this;
        }

        /**
         * Sets the retry loader | 设置重试加载器
         *
         * @param loader the retry loader function | 重试加载函数
         * @return this builder | 此构建器
         */
        public Builder<K> retryLoader(Function<K, ?> loader) {
            this.retryLoader = loader;
            return this;
        }

        /**
         * eventHandler | eventHandler
         * @param handler the handler | handler
         * @return the result | 结果
         */
        public Builder<K> eventHandler(DlqEventHandler<K> handler) {
            this.eventHandler = handler;
            return this;
        }

        /**
         * build | build
         * @return the result | 结果
         */
        public DeadLetterQueue<K> build() {
            return new DeadLetterQueue<>(maxRetries, initialBackoff, maxBackoff,
                    backoffMultiplier, maxQueueSize, retryLoader, eventHandler);
        }
    }
}
