package cloud.opencode.base.pool.metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.IntSupplier;

/**
 * DefaultPoolMetrics - Default Pool Metrics Implementation
 * DefaultPoolMetrics - 默认池指标实现
 *
 * <p>Thread-safe implementation of PoolMetrics using lock-free counters.</p>
 * <p>使用无锁计数器的线程安全PoolMetrics实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>LongAdder for high-throughput counting - LongAdder实现高吞吐量计数</li>
 *   <li>AtomicLong for max tracking - AtomicLong实现最大值追踪</li>
 *   <li>Lock-free operations - 无锁操作</li>
 *   <li>Configurable state suppliers - 可配置的状态供应器</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Counter increment: O(1) - 计数器增加: O(1)</li>
 *   <li>Counter read: O(cells) for LongAdder - 计数器读取: O(cells) LongAdder</li>
 *   <li>No blocking - 无阻塞</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Lock-free: Yes - 无锁: 是</li>
 * </ul>
 *
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DefaultPoolMetrics metrics = new DefaultPoolMetrics();
 * metrics.recordBorrow();
 * metrics.recordReturn();
 * MetricsSnapshot snapshot = metrics.snapshot();
 * }</pre>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public class DefaultPoolMetrics implements PoolMetrics {

    private final LongAdder borrowCount = new LongAdder();
    private final LongAdder returnCount = new LongAdder();
    private final LongAdder createdCount = new LongAdder();
    private final LongAdder destroyedCount = new LongAdder();
    private final LongAdder totalBorrowNanos = new LongAdder();
    private final AtomicLong maxBorrowNanos = new AtomicLong(0);
    private final LongAdder totalWaitNanos = new LongAdder();

    private IntSupplier activeSupplier = () -> 0;
    private IntSupplier idleSupplier = () -> 0;

    /**
     * Creates a new metrics instance.
     * 创建新的指标实例。
     */
    public DefaultPoolMetrics() {
    }

    /**
     * Sets the active count supplier.
     * 设置活跃数量供应器。
     *
     * @param supplier the supplier - 供应器
     */
    public void setActiveSupplier(IntSupplier supplier) {
        this.activeSupplier = supplier;
    }

    /**
     * Sets the idle count supplier.
     * 设置空闲数量供应器。
     *
     * @param supplier the supplier - 供应器
     */
    public void setIdleSupplier(IntSupplier supplier) {
        this.idleSupplier = supplier;
    }

    /**
     * Records a borrow operation.
     * 记录借用操作。
     */
    public void recordBorrow() {
        borrowCount.increment();
    }

    /**
     * Records a return operation.
     * 记录归还操作。
     */
    public void recordReturn() {
        returnCount.increment();
    }

    /**
     * Records an object creation.
     * 记录对象创建。
     */
    public void recordCreate() {
        createdCount.increment();
    }

    /**
     * Records an object destruction.
     * 记录对象销毁。
     */
    public void recordDestroy() {
        destroyedCount.increment();
    }

    /**
     * Records a borrow duration.
     * 记录借用时长。
     *
     * @param duration the duration - 时长
     */
    public void recordBorrowDuration(Duration duration) {
        long nanos = duration.toNanos();
        totalBorrowNanos.add(nanos);
        maxBorrowNanos.updateAndGet(current -> Math.max(current, nanos));
    }

    /**
     * Records a wait duration.
     * 记录等待时长。
     *
     * @param duration the duration - 时长
     */
    public void recordWaitDuration(Duration duration) {
        totalWaitNanos.add(duration.toNanos());
    }

    /**
     * Records a wait duration in nanoseconds (zero-allocation fast path).
     * 以纳秒记录等待时长（零分配快速路径）。
     *
     * @param nanos the wait duration in nanoseconds - 等待时长（纳秒）
     */
    public void recordWaitNanos(long nanos) {
        totalWaitNanos.add(nanos);
    }

    @Override
    public long getBorrowCount() {
        return borrowCount.sum();
    }

    @Override
    public long getReturnCount() {
        return returnCount.sum();
    }

    @Override
    public long getCreatedCount() {
        return createdCount.sum();
    }

    @Override
    public long getDestroyedCount() {
        return destroyedCount.sum();
    }

    @Override
    public Duration getAverageBorrowDuration() {
        long count = borrowCount.sum();
        if (count == 0) return Duration.ZERO;
        return Duration.ofNanos(totalBorrowNanos.sum() / count);
    }

    @Override
    public Duration getMaxBorrowDuration() {
        return Duration.ofNanos(maxBorrowNanos.get());
    }

    @Override
    public Duration getAverageWaitDuration() {
        long count = borrowCount.sum();
        if (count == 0) return Duration.ZERO;
        return Duration.ofNanos(totalWaitNanos.sum() / count);
    }

    @Override
    public MetricsSnapshot snapshot() {
        return new MetricsSnapshot(
                getBorrowCount(),
                getReturnCount(),
                getCreatedCount(),
                getDestroyedCount(),
                activeSupplier.getAsInt(),
                idleSupplier.getAsInt(),
                getAverageBorrowDuration(),
                getMaxBorrowDuration(),
                getAverageWaitDuration(),
                Instant.now()
        );
    }

    /**
     * Resets all counters to zero.
     * 重置所有计数器为零。
     */
    public void reset() {
        borrowCount.reset();
        returnCount.reset();
        createdCount.reset();
        destroyedCount.reset();
        totalBorrowNanos.reset();
        maxBorrowNanos.set(0);
        totalWaitNanos.reset();
    }
}
