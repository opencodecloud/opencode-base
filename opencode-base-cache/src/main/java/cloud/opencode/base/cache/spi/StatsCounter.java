package cloud.opencode.base.cache.spi;

import cloud.opencode.base.cache.CacheStats;

/**
 * Stats Counter SPI - Cache statistics counter interface
 * 统计计数器 SPI - 缓存统计计数器接口
 *
 * <p>Provides interface for recording cache statistics.</p>
 * <p>提供记录缓存统计信息的接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Hit/Miss recording - 命中/未命中记录</li>
 *   <li>Load time recording - 加载时间记录</li>
 *   <li>Eviction recording - 淘汰记录</li>
 *   <li>Statistics snapshot - 统计快照</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * StatsCounter counter = StatsCounter.concurrent();
 * counter.recordHits(1);
 * counter.recordMisses(1);
 * CacheStats stats = counter.snapshot();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public interface StatsCounter {

    /**
     * Record cache hits
     * 记录缓存命中
     *
     * @param count number of hits | 命中次数
     */
    void recordHits(int count);

    /**
     * Record cache misses
     * 记录缓存未命中
     *
     * @param count number of misses | 未命中次数
     */
    void recordMisses(int count);

    /**
     * Record successful load
     * 记录成功加载
     *
     * @param loadTime load time in nanoseconds | 加载时间（纳秒）
     */
    void recordLoadSuccess(long loadTime);

    /**
     * Record failed load
     * 记录失败加载
     *
     * @param loadTime load time in nanoseconds | 加载时间（纳秒）
     */
    void recordLoadFailure(long loadTime);

    /**
     * Record eviction
     * 记录淘汰
     *
     * @param weight evicted entry weight | 被淘汰条目的权重
     */
    void recordEviction(int weight);

    /**
     * Get statistics snapshot
     * 获取统计快照
     *
     * @return statistics snapshot | 统计快照
     */
    CacheStats snapshot();

    /**
     * Reset all statistics counters
     * 重置所有统计计数器
     *
     * <p>Clears all hit, miss, load, and eviction counters to zero.</p>
     * <p>将所有命中、未命中、加载和淘汰计数器清零。</p>
     *
     * @since V2.0.3
     */
    default void reset() {
        // Default no-op, implementations should override
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create disabled counter (no-op)
     * 创建禁用的计数器（空操作）
     *
     * @return disabled counter | 禁用的计数器
     */
    static StatsCounter disabled() {
        return DisabledStatsCounter.INSTANCE;
    }

    /**
     * Create concurrent counter using LongAdder
     * 创建使用 LongAdder 的并发计数器
     *
     * @return concurrent counter | 并发计数器
     */
    static StatsCounter concurrent() {
        return new cloud.opencode.base.cache.internal.stats.LongAdderStatsCounter();
    }

    /**
     * Create sampling counter for high-throughput scenarios
     * 创建高吞吐量场景的采样计数器
     *
     * <p>Uses probabilistic sampling to reduce overhead while maintaining statistical accuracy.</p>
     * <p>使用概率采样来降低开销，同时保持统计准确性。</p>
     *
     * @param sampleRate sampling rate (0.0 to 1.0), e.g., 0.1 for 10% sampling | 采样率
     * @return sampling counter | 采样计数器
     */
    static StatsCounter sampling(double sampleRate) {
        return cloud.opencode.base.cache.internal.stats.SamplingStatsCounter.withRate(sampleRate);
    }

    /**
     * Create sampling counter optimized for very high throughput (1% sampling)
     * 创建超高吞吐量优化的采样计数器（1% 采样）
     *
     * @return high-throughput sampling counter | 高吞吐量采样计数器
     */
    static StatsCounter samplingHighThroughput() {
        return cloud.opencode.base.cache.internal.stats.SamplingStatsCounter.highThroughput();
    }

    /**
     * Create sampling counter with balanced accuracy/performance (10% sampling)
     * 创建准确性/性能平衡的采样计数器（10% 采样）
     *
     * @return balanced sampling counter | 平衡采样计数器
     */
    static StatsCounter samplingBalanced() {
        return cloud.opencode.base.cache.internal.stats.SamplingStatsCounter.balanced();
    }
}

/**
 * Disabled stats counter implementation
 * 禁用的统计计数器实现
 */
enum DisabledStatsCounter implements StatsCounter {
    INSTANCE;

    @Override
    public void recordHits(int count) {
    }

    @Override
    public void recordMisses(int count) {
    }

    @Override
    public void recordLoadSuccess(long loadTime) {
    }

    @Override
    public void recordLoadFailure(long loadTime) {
    }

    @Override
    public void recordEviction(int weight) {
    }

    @Override
    public CacheStats snapshot() {
        return CacheStats.empty();
    }
}
