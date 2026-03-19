package cloud.opencode.base.cache.internal.stats;

import cloud.opencode.base.cache.CacheStats;
import cloud.opencode.base.cache.spi.StatsCounter;

import java.util.concurrent.atomic.LongAdder;

/**
 * LongAdder Stats Counter - High-performance concurrent statistics counter
 * LongAdder 统计计数器 - 高性能并发统计计数器
 *
 * <p>Uses LongAdder for lock-free atomic operations with high concurrency.</p>
 * <p>使用 LongAdder 实现高并发下的无锁原子操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lock-free atomic operations - 无锁原子操作</li>
 *   <li>High throughput under contention - 高竞争下的高吞吐量</li>
 *   <li>Consistent statistics snapshots - 一致的统计快照</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for all operations - 时间复杂度: 所有操作 O(1)</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * StatsCounter counter = new LongAdderStatsCounter();
 * counter.recordHits(1);
 * counter.recordMisses(1);
 * CacheStats stats = counter.snapshot();
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class LongAdderStatsCounter implements StatsCounter {

    /** Creates a new LongAdderStatsCounter instance | 创建新的 LongAdderStatsCounter 实例 */
    public LongAdderStatsCounter() {}

    private final LongAdder hitCount = new LongAdder();
    private final LongAdder missCount = new LongAdder();
    private final LongAdder loadSuccessCount = new LongAdder();
    private final LongAdder loadFailureCount = new LongAdder();
    private final LongAdder totalLoadTime = new LongAdder();
    private final LongAdder evictionCount = new LongAdder();
    private final LongAdder evictionWeight = new LongAdder();

    @Override
    public void recordHits(int count) {
        hitCount.add(count);
    }

    @Override
    public void recordMisses(int count) {
        missCount.add(count);
    }

    @Override
    public void recordLoadSuccess(long loadTime) {
        loadSuccessCount.increment();
        totalLoadTime.add(loadTime);
    }

    @Override
    public void recordLoadFailure(long loadTime) {
        loadFailureCount.increment();
        totalLoadTime.add(loadTime);
    }

    @Override
    public void recordEviction(int weight) {
        evictionCount.increment();
        evictionWeight.add(weight);
    }

    @Override
    public CacheStats snapshot() {
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
    }
}
