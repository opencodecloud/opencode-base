package cloud.opencode.base.lock.metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Default Lock Metrics Implementation with Lock-Free Statistics
 * 使用无锁统计的默认锁指标实现
 *
 * <p>A thread-safe implementation using atomic operations and LongAdder
 * for high-performance lock-free statistics collection.</p>
 * <p>使用原子操作和LongAdder的线程安全实现，用于高性能无锁统计收集。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lock-free statistics - 无锁统计</li>
 *   <li>High throughput with LongAdder - 使用LongAdder实现高吞吐量</li>
 *   <li>Accurate contention tracking - 精确竞争跟踪</li>
 *   <li>Max wait time tracking - 最大等待时间跟踪</li>
 *   <li>Snapshot support - 快照支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DefaultLockMetrics metrics = new DefaultLockMetrics();
 *
 * // Record lock acquisition | 记录锁获取
 * metrics.recordAcquire(Duration.ofMillis(5));
 *
 * // Record lock release | 记录锁释放
 * metrics.recordRelease();
 *
 * // Get statistics | 获取统计
 * LockStats stats = metrics.snapshot();
 * System.out.println("Contention rate: " + stats.getContentionRate());
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Uses LongAdder for high-throughput counting - 使用LongAdder实现高吞吐量计数</li>
 *   <li>Minimal impact on lock performance - 对锁性能影响最小</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (lock-free) - 线程安全: 是（无锁）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see LockMetrics
 * @see LockStats
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public class DefaultLockMetrics implements LockMetrics {

    private final LongAdder acquireCount = new LongAdder();
    private final LongAdder releaseCount = new LongAdder();
    private final LongAdder timeoutCount = new LongAdder();
    private final LongAdder contentionCount = new LongAdder();
    private final LongAdder totalWaitNanos = new LongAdder();
    private final AtomicLong maxWaitNanos = new AtomicLong(0);
    private final AtomicInteger currentHoldCount = new AtomicInteger(0);

    /**
     * Records a successful lock acquisition
     * 记录成功的锁获取
     *
     * @param waitTime time spent waiting for the lock | 等待锁的时间
     */
    public void recordAcquire(Duration waitTime) {
        acquireCount.increment();
        currentHoldCount.incrementAndGet();

        long nanos = waitTime.toNanos();
        if (nanos > 0) {
            contentionCount.increment();
            totalWaitNanos.add(nanos);
            maxWaitNanos.updateAndGet(current -> Math.max(current, nanos));
        }
    }

    /**
     * Records a lock release
     * 记录锁释放
     */
    public void recordRelease() {
        releaseCount.increment();
        currentHoldCount.decrementAndGet();
    }

    /**
     * Records a lock acquisition timeout
     * 记录锁获取超时
     */
    public void recordTimeout() {
        timeoutCount.increment();
    }

    @Override
    public long getAcquireCount() {
        return acquireCount.sum();
    }

    @Override
    public long getReleaseCount() {
        return releaseCount.sum();
    }

    @Override
    public long getTimeoutCount() {
        return timeoutCount.sum();
    }

    @Override
    public long getContentionCount() {
        return contentionCount.sum();
    }

    @Override
    public Duration getAverageWaitTime() {
        long count = contentionCount.sum();
        if (count == 0) return Duration.ZERO;
        return Duration.ofNanos(totalWaitNanos.sum() / count);
    }

    @Override
    public Duration getMaxWaitTime() {
        return Duration.ofNanos(maxWaitNanos.get());
    }

    @Override
    public int getCurrentHoldCount() {
        return currentHoldCount.get();
    }

    @Override
    public LockStats snapshot() {
        return new LockStats(
                getAcquireCount(),
                getReleaseCount(),
                getTimeoutCount(),
                getContentionCount(),
                getAverageWaitTime(),
                getMaxWaitTime(),
                getCurrentHoldCount(),
                Instant.now()
        );
    }

    @Override
    public void reset() {
        acquireCount.reset();
        releaseCount.reset();
        timeoutCount.reset();
        contentionCount.reset();
        totalWaitNanos.reset();
        maxWaitNanos.set(0);
    }
}
