package cloud.opencode.base.lock.metrics;

import java.time.Duration;
import java.time.Instant;

/**
 * Lock Statistics Record - Immutable Snapshot of Lock Metrics
 * 锁统计记录 - 锁指标的不可变快照
 *
 * <p>An immutable snapshot of lock statistics at a point in time,
 * providing calculated rates for analysis.</p>
 * <p>在某个时间点锁统计的不可变快照，提供用于分析的计算率。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable statistics snapshot - 不可变统计快照</li>
 *   <li>Success rate calculation - 成功率计算</li>
 *   <li>Contention rate calculation - 竞争率计算</li>
 *   <li>Timeout rate calculation - 超时率计算</li>
 *   <li>Timestamp for tracking - 用于跟踪的时间戳</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LockStats stats = metrics.snapshot();
 *
 * // Check rates | 检查率
 * if (stats.getContentionRate() > 0.5) {
 *     System.out.println("High contention detected!");
 * }
 *
 * // Get timing info | 获取时间信息
 * System.out.println("Max wait: " + stats.maxWaitTime());
 * System.out.println("Snapshot at: " + stats.timestamp());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @param acquireCount     total number of lock acquisitions | 锁获取总次数
 * @param releaseCount     total number of lock releases | 锁释放总次数
 * @param timeoutCount     number of acquisition timeouts | 获取超时次数
 * @param contentionCount  number of times threads had to wait | 线程等待次数
 * @param averageWaitTime  average time spent waiting for lock | 等待锁的平均时间
 * @param maxWaitTime      maximum time spent waiting for lock | 等待锁的最大时间
 * @param currentHoldCount current number of holds | 当前持有次数
 * @param timestamp        time when snapshot was taken | 快照拍摄时间
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see LockMetrics
 * @see DefaultLockMetrics
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public record LockStats(
        long acquireCount,
        long releaseCount,
        long timeoutCount,
        long contentionCount,
        Duration averageWaitTime,
        Duration maxWaitTime,
        int currentHoldCount,
        Instant timestamp
) {
    /**
     * Calculates the lock acquisition success rate
     * 计算锁获取成功率
     *
     * @return success rate (0.0 to 1.0) | 成功率（0.0到1.0）
     */
    public double getSuccessRate() {
        long total = acquireCount + timeoutCount;
        if (total == 0) return 1.0;
        return (double) acquireCount / total;
    }

    /**
     * Calculates the lock contention rate
     * 计算锁竞争率
     *
     * @return contention rate (0.0 to 1.0) | 竞争率（0.0到1.0）
     */
    public double getContentionRate() {
        if (acquireCount == 0) return 0.0;
        return (double) contentionCount / acquireCount;
    }

    /**
     * Calculates the lock acquisition timeout rate
     * 计算锁获取超时率
     *
     * @return timeout rate (0.0 to 1.0) | 超时率（0.0到1.0）
     */
    public double getTimeoutRate() {
        long total = acquireCount + timeoutCount;
        if (total == 0) return 0.0;
        return (double) timeoutCount / total;
    }
}
