package cloud.opencode.base.lock.metrics;

import java.time.Duration;

/**
 * Lock Metrics Interface for Lock Usage Statistics
 * 锁使用统计的锁指标接口
 *
 * <p>Provides metrics about lock usage including acquire/release counts,
 * contention, and timing information.</p>
 * <p>提供有关锁使用的指标，包括获取/释放次数、竞争和时间信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Acquire/release counting - 获取/释放计数</li>
 *   <li>Timeout tracking - 超时跟踪</li>
 *   <li>Contention detection - 竞争检测</li>
 *   <li>Wait time statistics - 等待时间统计</li>
 *   <li>Snapshot support - 快照支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Lock<Long> lock = OpenLock.lock(LockConfig.builder()
 *     .enableMetrics(true)
 *     .build());
 *
 * // Get metrics | 获取指标
 * LockMetrics metrics = ((LocalLock) lock).getMetrics().orElseThrow();
 *
 * // Check contention | 检查竞争
 * if (metrics.getContentionRate() > 0.5) {
 *     // High contention | 高竞争
 * }
 *
 * // Get snapshot | 获取快照
 * LockStats stats = metrics.snapshot();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 依赖实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see DefaultLockMetrics
 * @see LockStats
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public interface LockMetrics {

    /**
     * Gets the total number of successful lock acquisitions
     * 获取成功锁获取的总次数
     *
     * @return total acquire count | 总获取次数
     */
    long getAcquireCount();

    /**
     * Gets the total number of lock releases
     * 获取锁释放的总次数
     *
     * @return total release count | 总释放次数
     */
    long getReleaseCount();

    /**
     * Gets the number of lock acquisition timeouts
     * 获取锁获取超时次数
     *
     * @return timeout count | 超时次数
     */
    long getTimeoutCount();

    /**
     * Gets the number of times threads had to wait for the lock
     * 获取线程必须等待锁的次数
     *
     * @return contention count (times waiting for lock) | 竞争次数（等待锁的次数）
     */
    long getContentionCount();

    /**
     * Gets the average time spent waiting for the lock
     * 获取等待锁的平均时间
     *
     * @return average wait time | 平均等待时间
     */
    Duration getAverageWaitTime();

    /**
     * Gets the maximum time any thread spent waiting for the lock
     * 获取任何线程等待锁的最大时间
     *
     * @return max wait time | 最大等待时间
     */
    Duration getMaxWaitTime();

    /**
     * Gets the current number of threads holding the lock
     * 获取当前持有锁的线程数
     *
     * @return current hold count (if available) | 当前持有次数（如果可用）
     */
    int getCurrentHoldCount();

    /**
     * Gets an immutable snapshot of the current metrics
     * 获取当前指标的不可变快照
     *
     * @return metrics snapshot | 指标快照
     */
    LockStats snapshot();

    /**
     * Resets all metrics to their initial values
     * 重置所有指标为初始值
     */
    void reset();
}
