package cloud.opencode.base.cache;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Cache Metrics - Detailed latency tracking with percentile calculations
 * 缓存指标 - 带百分位数计算的详细延迟跟踪
 *
 * <p>Provides comprehensive metrics including P50, P95, P99 latency tracking
 * for cache operations using HDR histogram approximation.</p>
 * <p>使用 HDR 直方图近似提供包括 P50、P95、P99 延迟跟踪在内的综合指标。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Percentile latency tracking (P50/P95/P99) - 百分位延迟跟踪</li>
 *   <li>Min/Max/Mean latency - 最小/最大/平均延迟</li>
 *   <li>Throughput calculation - 吞吐量计算</li>
 *   <li>Operation-specific metrics - 操作特定指标</li>
 *   <li>Thread-safe concurrent recording - 线程安全并发记录</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CacheMetrics metrics = CacheMetrics.create();
 *
 * // Record operation latency - 记录操作延迟
 * long start = System.nanoTime();
 * cache.get(key);
 * metrics.recordGetLatency(System.nanoTime() - start);
 *
 * // Get percentiles - 获取百分位数
 * long p50 = metrics.getLatencyP50();
 * long p99 = metrics.getLatencyP99();
 *
 * // Get snapshot - 获取快照
 * MetricsSnapshot snapshot = metrics.snapshot();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Memory-bounded: Yes (fixed histogram size) - 内存有界: 是（固定直方图大小）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class CacheMetrics {

    // Histogram buckets for latency distribution (logarithmic scale)
    // 延迟分布的直方图桶（对数刻度）
    private static final int BUCKET_COUNT = 64;
    private static final long[] BUCKET_BOUNDARIES = createBucketBoundaries();

    // Operation counters
    private final LongAdder getCount = new LongAdder();
    private final LongAdder putCount = new LongAdder();
    private final LongAdder loadCount = new LongAdder();
    private final LongAdder evictionCount = new LongAdder();

    // Latency tracking
    private final LongAdder[] getLatencyBuckets = createBuckets();
    private final LongAdder[] putLatencyBuckets = createBuckets();
    private final LongAdder[] loadLatencyBuckets = createBuckets();

    private final LongAdder totalGetLatency = new LongAdder();
    private final LongAdder totalPutLatency = new LongAdder();
    private final LongAdder totalLoadLatency = new LongAdder();

    private final AtomicLong minGetLatency = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxGetLatency = new AtomicLong(0);
    private final AtomicLong minPutLatency = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxPutLatency = new AtomicLong(0);
    private final AtomicLong minLoadLatency = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxLoadLatency = new AtomicLong(0);

    // Timestamp for throughput calculation
    private final long startTimeMillis = System.currentTimeMillis();

    private CacheMetrics() {
    }

    /**
     * Create new metrics instance
     * 创建新的指标实例
     *
     * @return new metrics | 新指标
     */
    public static CacheMetrics create() {
        return new CacheMetrics();
    }

    // ==================== Recording Methods | 记录方法 ====================

    /**
     * Record get operation latency
     * 记录获取操作延迟
     *
     * @param latencyNanos latency in nanoseconds | 纳秒延迟
     */
    public void recordGetLatency(long latencyNanos) {
        getCount.increment();
        totalGetLatency.add(latencyNanos);
        recordToBucket(getLatencyBuckets, latencyNanos);
        updateMinMax(minGetLatency, maxGetLatency, latencyNanos);
    }

    /**
     * Record put operation latency
     * 记录放入操作延迟
     *
     * @param latencyNanos latency in nanoseconds | 纳秒延迟
     */
    public void recordPutLatency(long latencyNanos) {
        putCount.increment();
        totalPutLatency.add(latencyNanos);
        recordToBucket(putLatencyBuckets, latencyNanos);
        updateMinMax(minPutLatency, maxPutLatency, latencyNanos);
    }

    /**
     * Record load operation latency
     * 记录加载操作延迟
     *
     * @param latencyNanos latency in nanoseconds | 纳秒延迟
     */
    public void recordLoadLatency(long latencyNanos) {
        loadCount.increment();
        totalLoadLatency.add(latencyNanos);
        recordToBucket(loadLatencyBuckets, latencyNanos);
        updateMinMax(minLoadLatency, maxLoadLatency, latencyNanos);
    }

    /**
     * Record eviction
     * 记录淘汰
     */
    public void recordEviction() {
        evictionCount.increment();
    }

    // ==================== Query Methods | 查询方法 ====================

    /**
     * Get total get operation count
     * 获取总获取操作数
     *
     * @return get count | 获取数
     */
    public long getGetCount() {
        return getCount.sum();
    }

    /**
     * Get total put operation count
     * 获取总放入操作数
     *
     * @return put count | 放入数
     */
    public long getPutCount() {
        return putCount.sum();
    }

    /**
     * Get total load operation count
     * 获取总加载操作数
     *
     * @return load count | 加载数
     */
    public long getLoadCount() {
        return loadCount.sum();
    }

    /**
     * Get total eviction count
     * 获取总淘汰数
     *
     * @return eviction count | 淘汰数
     */
    public long getEvictionCount() {
        return evictionCount.sum();
    }

    // ==================== Latency Percentiles | 延迟百分位数 ====================

    /**
     * Get P50 (median) get latency in nanoseconds
     * 获取 P50（中位数）获取延迟（纳秒）
     *
     * @return P50 latency | P50 延迟
     */
    public long getGetLatencyP50() {
        return getPercentile(getLatencyBuckets, 50);
    }

    /**
     * Get P95 get latency in nanoseconds
     * 获取 P95 获取延迟（纳秒）
     *
     * @return P95 latency | P95 延迟
     */
    public long getGetLatencyP95() {
        return getPercentile(getLatencyBuckets, 95);
    }

    /**
     * Get P99 get latency in nanoseconds
     * 获取 P99 获取延迟（纳秒）
     *
     * @return P99 latency | P99 延迟
     */
    public long getGetLatencyP99() {
        return getPercentile(getLatencyBuckets, 99);
    }

    /**
     * Get P50 (median) put latency in nanoseconds
     * 获取 P50（中位数）放入延迟（纳秒）
     *
     * @return P50 latency | P50 延迟
     */
    public long getPutLatencyP50() {
        return getPercentile(putLatencyBuckets, 50);
    }

    /**
     * Get P95 put latency in nanoseconds
     * 获取 P95 放入延迟（纳秒）
     *
     * @return P95 latency | P95 延迟
     */
    public long getPutLatencyP95() {
        return getPercentile(putLatencyBuckets, 95);
    }

    /**
     * Get P99 put latency in nanoseconds
     * 获取 P99 放入延迟（纳秒）
     *
     * @return P99 latency | P99 延迟
     */
    public long getPutLatencyP99() {
        return getPercentile(putLatencyBuckets, 99);
    }

    /**
     * Get P50 (median) load latency in nanoseconds
     * 获取 P50（中位数）加载延迟（纳秒）
     *
     * @return P50 latency | P50 延迟
     */
    public long getLoadLatencyP50() {
        return getPercentile(loadLatencyBuckets, 50);
    }

    /**
     * Get P95 load latency in nanoseconds
     * 获取 P95 加载延迟（纳秒）
     *
     * @return P95 latency | P95 延迟
     */
    public long getLoadLatencyP95() {
        return getPercentile(loadLatencyBuckets, 95);
    }

    /**
     * Get P99 load latency in nanoseconds
     * 获取 P99 加载延迟（纳秒）
     *
     * @return P99 latency | P99 延迟
     */
    public long getLoadLatencyP99() {
        return getPercentile(loadLatencyBuckets, 99);
    }

    // ==================== Average/Min/Max Latency | 平均/最小/最大延迟 ====================

    /**
     * Get average get latency in nanoseconds
     * 获取平均获取延迟（纳秒）
     *
     * @return average latency | 平均延迟
     */
    public double getAverageGetLatency() {
        long count = getCount.sum();
        return count == 0 ? 0.0 : (double) totalGetLatency.sum() / count;
    }

    /**
     * Get average put latency in nanoseconds
     * 获取平均放入延迟（纳秒）
     *
     * @return average latency | 平均延迟
     */
    public double getAveragePutLatency() {
        long count = putCount.sum();
        return count == 0 ? 0.0 : (double) totalPutLatency.sum() / count;
    }

    /**
     * Get average load latency in nanoseconds
     * 获取平均加载延迟（纳秒）
     *
     * @return average latency | 平均延迟
     */
    public double getAverageLoadLatency() {
        long count = loadCount.sum();
        return count == 0 ? 0.0 : (double) totalLoadLatency.sum() / count;
    }

    /**
     * Get minimum get latency in nanoseconds
     * 获取最小获取延迟（纳秒）
     *
     * @return min latency | 最小延迟
     */
    public long getMinGetLatency() {
        long val = minGetLatency.get();
        return val == Long.MAX_VALUE ? 0 : val;
    }

    /**
     * Get maximum get latency in nanoseconds
     * 获取最大获取延迟（纳秒）
     *
     * @return max latency | 最大延迟
     */
    public long getMaxGetLatency() {
        return maxGetLatency.get();
    }

    // ==================== Throughput | 吞吐量 ====================

    /**
     * Get operations per second for get operations
     * 获取获取操作的每秒操作数
     *
     * @return ops/sec | 操作/秒
     */
    public double getGetThroughput() {
        long elapsedMs = System.currentTimeMillis() - startTimeMillis;
        if (elapsedMs <= 0) return 0.0;
        return (double) getCount.sum() * 1000 / elapsedMs;
    }

    /**
     * Get operations per second for put operations
     * 获取放入操作的每秒操作数
     *
     * @return ops/sec | 操作/秒
     */
    public double getPutThroughput() {
        long elapsedMs = System.currentTimeMillis() - startTimeMillis;
        if (elapsedMs <= 0) return 0.0;
        return (double) putCount.sum() * 1000 / elapsedMs;
    }

    /**
     * Get uptime duration
     * 获取运行时间
     *
     * @return uptime | 运行时间
     */
    public Duration getUptime() {
        return Duration.ofMillis(System.currentTimeMillis() - startTimeMillis);
    }

    // ==================== Snapshot | 快照 ====================

    /**
     * Create immutable snapshot of current metrics
     * 创建当前指标的不可变快照
     *
     * @return metrics snapshot | 指标快照
     */
    public MetricsSnapshot snapshot() {
        return new MetricsSnapshot(
                getCount.sum(),
                putCount.sum(),
                loadCount.sum(),
                evictionCount.sum(),
                getGetLatencyP50(),
                getGetLatencyP95(),
                getGetLatencyP99(),
                getAverageGetLatency(),
                getMinGetLatency(),
                maxGetLatency.get(),
                getPutLatencyP50(),
                getPutLatencyP95(),
                getPutLatencyP99(),
                getAveragePutLatency(),
                getLoadLatencyP50(),
                getLoadLatencyP95(),
                getLoadLatencyP99(),
                getAverageLoadLatency(),
                getGetThroughput(),
                getPutThroughput(),
                getUptime()
        );
    }

    /**
     * Reset all metrics
     * 重置所有指标
     */
    public void reset() {
        getCount.reset();
        putCount.reset();
        loadCount.reset();
        evictionCount.reset();
        totalGetLatency.reset();
        totalPutLatency.reset();
        totalLoadLatency.reset();
        minGetLatency.set(Long.MAX_VALUE);
        maxGetLatency.set(0);
        minPutLatency.set(Long.MAX_VALUE);
        maxPutLatency.set(0);
        minLoadLatency.set(Long.MAX_VALUE);
        maxLoadLatency.set(0);
        // Reset buckets by resetting each LongAdder in place rather than nulling
        // and recreating, which would cause NPE in concurrent recordToBucket() calls
        for (int i = 0; i < BUCKET_COUNT; i++) {
            getLatencyBuckets[i].reset();
            putLatencyBuckets[i].reset();
            loadLatencyBuckets[i].reset();
        }
    }

    // ==================== Private Helper Methods | 私有辅助方法 ====================

    private static long[] createBucketBoundaries() {
        long[] boundaries = new long[BUCKET_COUNT];
        // Logarithmic scale from 100ns to ~1 hour
        for (int i = 0; i < BUCKET_COUNT; i++) {
            boundaries[i] = (long) Math.pow(2, i) * 100; // 100ns, 200ns, 400ns, ...
        }
        return boundaries;
    }

    private static LongAdder[] createBuckets() {
        LongAdder[] buckets = new LongAdder[BUCKET_COUNT];
        for (int i = 0; i < BUCKET_COUNT; i++) {
            buckets[i] = new LongAdder();
        }
        return buckets;
    }

    private void recordToBucket(LongAdder[] buckets, long value) {
        int index = findBucket(value);
        buckets[index].increment();
    }

    private int findBucket(long value) {
        for (int i = 0; i < BUCKET_COUNT - 1; i++) {
            if (value <= BUCKET_BOUNDARIES[i]) {
                return i;
            }
        }
        return BUCKET_COUNT - 1;
    }

    private long getPercentile(LongAdder[] buckets, int percentile) {
        long total = 0;
        for (LongAdder bucket : buckets) {
            total += bucket.sum();
        }

        if (total == 0) {
            return 0;
        }

        long threshold = (total * percentile + 99) / 100;
        long cumulative = 0;

        for (int i = 0; i < BUCKET_COUNT; i++) {
            cumulative += buckets[i].sum();
            if (cumulative >= threshold) {
                return BUCKET_BOUNDARIES[i];
            }
        }

        return BUCKET_BOUNDARIES[BUCKET_COUNT - 1];
    }

    private void updateMinMax(AtomicLong min, AtomicLong max, long value) {
        long currentMin;
        do {
            currentMin = min.get();
            if (value >= currentMin) break;
        } while (!min.compareAndSet(currentMin, value));

        long currentMax;
        do {
            currentMax = max.get();
            if (value <= currentMax) break;
        } while (!max.compareAndSet(currentMax, value));
    }

    // ==================== Metrics Snapshot Record | 指标快照记录 ====================

    /**
     * Immutable metrics snapshot
     * 不可变指标快照
     *
     * @param getCount        total get operations | 总获取操作数
     * @param putCount        total put operations | 总放入操作数
     * @param loadCount       total load operations | 总加载操作数
     * @param evictionCount   total evictions | 总淘汰数
     * @param getLatencyP50   P50 get latency (ns) | P50 获取延迟（纳秒）
     * @param getLatencyP95   P95 get latency (ns) | P95 获取延迟（纳秒）
     * @param getLatencyP99   P99 get latency (ns) | P99 获取延迟（纳秒）
     * @param avgGetLatency   average get latency (ns) | 平均获取延迟（纳秒）
     * @param minGetLatency   min get latency (ns) | 最小获取延迟（纳秒）
     * @param maxGetLatency   max get latency (ns) | 最大获取延迟（纳秒）
     * @param putLatencyP50   P50 put latency (ns) | P50 放入延迟（纳秒）
     * @param putLatencyP95   P95 put latency (ns) | P95 放入延迟（纳秒）
     * @param putLatencyP99   P99 put latency (ns) | P99 放入延迟（纳秒）
     * @param avgPutLatency   average put latency (ns) | 平均放入延迟（纳秒）
     * @param loadLatencyP50  P50 load latency (ns) | P50 加载延迟（纳秒）
     * @param loadLatencyP95  P95 load latency (ns) | P95 加载延迟（纳秒）
     * @param loadLatencyP99  P99 load latency (ns) | P99 加载延迟（纳秒）
     * @param avgLoadLatency  average load latency (ns) | 平均加载延迟（纳秒）
     * @param getThroughput   get ops/sec | 获取操作/秒
     * @param putThroughput   put ops/sec | 放入操作/秒
     * @param uptime          uptime duration | 运行时间
     */
    public record MetricsSnapshot(
            long getCount,
            long putCount,
            long loadCount,
            long evictionCount,
            long getLatencyP50,
            long getLatencyP95,
            long getLatencyP99,
            double avgGetLatency,
            long minGetLatency,
            long maxGetLatency,
            long putLatencyP50,
            long putLatencyP95,
            long putLatencyP99,
            double avgPutLatency,
            long loadLatencyP50,
            long loadLatencyP95,
            long loadLatencyP99,
            double avgLoadLatency,
            double getThroughput,
            double putThroughput,
            Duration uptime
    ) {

        /**
         * Format as human-readable string
         * 格式化为可读字符串
         *
         * @return formatted string | 格式化字符串
         */
        public String format() {
            return String.format("""
                    Cache Metrics (uptime: %s)
                    ├── Operations: get=%d, put=%d, load=%d, eviction=%d
                    ├── Get Latency: P50=%.2fμs, P95=%.2fμs, P99=%.2fμs, avg=%.2fμs
                    ├── Put Latency: P50=%.2fμs, P95=%.2fμs, P99=%.2fμs, avg=%.2fμs
                    ├── Load Latency: P50=%.2fμs, P95=%.2fμs, P99=%.2fμs, avg=%.2fμs
                    └── Throughput: get=%.2f ops/s, put=%.2f ops/s
                    """,
                    uptime,
                    getCount, putCount, loadCount, evictionCount,
                    getLatencyP50 / 1000.0, getLatencyP95 / 1000.0, getLatencyP99 / 1000.0, avgGetLatency / 1000.0,
                    putLatencyP50 / 1000.0, putLatencyP95 / 1000.0, putLatencyP99 / 1000.0, avgPutLatency / 1000.0,
                    loadLatencyP50 / 1000.0, loadLatencyP95 / 1000.0, loadLatencyP99 / 1000.0, avgLoadLatency / 1000.0,
                    getThroughput, putThroughput
            );
        }
    }
}
