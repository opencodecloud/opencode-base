package cloud.opencode.base.cache.internal.stats;

import cloud.opencode.base.cache.CacheStats;
import cloud.opencode.base.cache.spi.StatsCounter;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.LongAdder;

/**
 * Sampling Stats Counter - High-performance statistics with probabilistic sampling
 * 采样统计计数器 - 高性能概率采样统计
 *
 * <p>Uses probabilistic sampling to reduce overhead while maintaining statistical accuracy.
 * Ideal for extremely high-throughput caches where full counting would be too expensive.</p>
 * <p>使用概率采样来降低开销，同时保持统计准确性。
 * 适用于完全计数代价过高的超高吞吐量缓存。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable sampling rate - 可配置的采样率</li>
 *   <li>Lock-free atomic operations - 无锁原子操作</li>
 *   <li>Automatic extrapolation - 自动外推</li>
 *   <li>Near-zero overhead at low sampling rates - 低采样率时近乎零开销</li>
 * </ul>
 *
 * <p><strong>Accuracy Trade-offs | 准确性权衡:</strong></p>
 * <ul>
 *   <li>1% sampling: ~10% error margin, minimal overhead - 1% 采样：约 10% 误差，最小开销</li>
 *   <li>10% sampling: ~3% error margin, low overhead - 10% 采样：约 3% 误差，低开销</li>
 *   <li>100% sampling: exact counts (same as LongAdderStatsCounter) - 100% 采样：精确计数</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // 10% sampling rate - 10% 采样率
 * StatsCounter counter = SamplingStatsCounter.withRate(0.1);
 *
 * // 1% sampling for very high throughput - 超高吞吐量使用 1% 采样
 * StatsCounter counter = SamplingStatsCounter.withRate(0.01);
 *
 * // Full counting (100%) - 完全计数
 * StatsCounter counter = SamplingStatsCounter.withRate(1.0);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses LongAdder and ThreadLocalRandom) - 线程安全: 是（使用 LongAdder 和 ThreadLocalRandom）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.0
 */
public final class SamplingStatsCounter implements StatsCounter {

    private final double sampleRate;
    private final double inverseRate;
    private final int threshold;

    private final LongAdder hitCount = new LongAdder();
    private final LongAdder missCount = new LongAdder();
    private final LongAdder loadSuccessCount = new LongAdder();
    private final LongAdder loadFailureCount = new LongAdder();
    private final LongAdder totalLoadTime = new LongAdder();
    private final LongAdder evictionCount = new LongAdder();
    private final LongAdder evictionWeight = new LongAdder();

    // Counters for actual samples taken (for accurate extrapolation)
    private final LongAdder hitSamples = new LongAdder();
    private final LongAdder missSamples = new LongAdder();

    /**
     * Create sampling counter with specified rate
     * 创建指定采样率的采样计数器
     *
     * @param sampleRate sampling rate (0.0 to 1.0) | 采样率（0.0 到 1.0）
     */
    private SamplingStatsCounter(double sampleRate) {
        if (sampleRate <= 0 || sampleRate > 1.0) {
            throw new IllegalArgumentException("Sample rate must be between 0 (exclusive) and 1 (inclusive)");
        }
        this.sampleRate = sampleRate;
        this.inverseRate = 1.0 / sampleRate;
        // Convert to integer threshold for faster comparison
        this.threshold = (int) (sampleRate * Integer.MAX_VALUE);
    }

    /**
     * Create a sampling counter with specified rate
     * 创建指定采样率的采样计数器
     *
     * @param sampleRate sampling rate (0.0 to 1.0) | 采样率
     * @return sampling counter | 采样计数器
     */
    public static SamplingStatsCounter withRate(double sampleRate) {
        return new SamplingStatsCounter(sampleRate);
    }

    /**
     * Create a sampling counter optimized for high throughput (1% sampling)
     * 创建高吞吐量优化的采样计数器（1% 采样）
     *
     * @return sampling counter | 采样计数器
     */
    public static SamplingStatsCounter highThroughput() {
        return new SamplingStatsCounter(0.01);
    }

    /**
     * Create a sampling counter with balanced accuracy/performance (10% sampling)
     * 创建准确性/性能平衡的采样计数器（10% 采样）
     *
     * @return sampling counter | 采样计数器
     */
    public static SamplingStatsCounter balanced() {
        return new SamplingStatsCounter(0.1);
    }

    private boolean shouldSample() {
        // Fast path: always sample at 100%
        if (sampleRate >= 1.0) {
            return true;
        }
        // Use ThreadLocalRandom for fast, thread-safe random generation
        return ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE) < threshold;
    }

    @Override
    public void recordHits(int count) {
        if (shouldSample()) {
            hitCount.add(count);
            hitSamples.increment();
        }
    }

    @Override
    public void recordMisses(int count) {
        if (shouldSample()) {
            missCount.add(count);
            missSamples.increment();
        }
    }

    @Override
    public void recordLoadSuccess(long loadTime) {
        // Always record load times (relatively infrequent)
        loadSuccessCount.increment();
        totalLoadTime.add(loadTime);
    }

    @Override
    public void recordLoadFailure(long loadTime) {
        // Always record load failures (important for monitoring)
        loadFailureCount.increment();
        totalLoadTime.add(loadTime);
    }

    @Override
    public void recordEviction(int weight) {
        // Always record evictions (important for cache tuning)
        evictionCount.increment();
        evictionWeight.add(weight);
    }

    @Override
    public CacheStats snapshot() {
        // Extrapolate hit/miss counts based on sampling rate
        long sampledHits = hitCount.sum();
        long sampledMisses = missCount.sum();

        // Use inverse rate to extrapolate
        long extrapolatedHits = Math.round(sampledHits * inverseRate);
        long extrapolatedMisses = Math.round(sampledMisses * inverseRate);

        return CacheStats.of(
                extrapolatedHits,
                extrapolatedMisses,
                loadSuccessCount.sum(),
                loadFailureCount.sum(),
                totalLoadTime.sum(),
                evictionCount.sum(),
                evictionWeight.sum()
        );
    }

    /**
     * Get raw (non-extrapolated) statistics
     * 获取原始（未外推）统计
     *
     * @return raw statistics | 原始统计
     */
    public CacheStats rawSnapshot() {
        return CacheStats.of(
                hitCount.sum(),
                missCount.sum(),
                loadSuccessCount.sum(),
                loadFailureCount.sum(),
                totalLoadTime.sum(),
                evictionCount.sum(),
                evictionWeight.sum()
        );
    }

    /**
     * Get the sampling rate
     * 获取采样率
     *
     * @return sampling rate | 采样率
     */
    public double getSampleRate() {
        return sampleRate;
    }

    /**
     * Get the number of hit samples taken
     * 获取命中采样数
     *
     * @return hit sample count | 命中采样数
     */
    public long getHitSampleCount() {
        return hitSamples.sum();
    }

    /**
     * Get the number of miss samples taken
     * 获取未命中采样数
     *
     * @return miss sample count | 未命中采样数
     */
    public long getMissSampleCount() {
        return missSamples.sum();
    }

    /**
     * Reset all counters
     * 重置所有计数器
     */
    public void reset() {
        hitCount.reset();
        missCount.reset();
        loadSuccessCount.reset();
        loadFailureCount.reset();
        totalLoadTime.reset();
        evictionCount.reset();
        evictionWeight.reset();
        hitSamples.reset();
        missSamples.reset();
    }

    /**
     * Get sampling statistics
     * 获取采样统计
     *
     * @return sampling stats | 采样统计
     */
    public SamplingStats getSamplingStats() {
        return new SamplingStats(
                sampleRate,
                hitSamples.sum(),
                missSamples.sum(),
                hitCount.sum(),
                missCount.sum()
        );
    }

    /**
     * Sampling statistics record
     * 采样统计记录
     */
    public record SamplingStats(
            double sampleRate,
            long hitSamples,
            long missSamples,
            long rawHitCount,
            long rawMissCount
    ) {
        /**
         * Get estimated error margin based on sample size
         * 基于样本大小获取估计误差范围
         *
         * @return error margin (0.0 to 1.0) | 误差范围
         */
        public double estimatedErrorMargin() {
            long totalSamples = hitSamples + missSamples;
            if (totalSamples == 0) {
                return 1.0; // No data
            }
            // Standard error approximation: 1/sqrt(n)
            return 1.0 / Math.sqrt(totalSamples);
        }
    }
}
