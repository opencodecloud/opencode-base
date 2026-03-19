package cloud.opencode.base.pool.metrics;

import java.time.Duration;
import java.time.Instant;

/**
 * MetricsSnapshot - Metrics Snapshot Record (JDK 25 Record)
 * MetricsSnapshot - 指标快照记录 (JDK 25 Record)
 *
 * <p>Immutable snapshot of pool metrics at a point in time.</p>
 * <p>某一时刻的池指标不可变快照。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Point-in-time metrics capture - 时间点指标捕获</li>
 *   <li>Immutable for thread-safety - 不可变保证线程安全</li>
 *   <li>Comprehensive pool statistics - 全面的池统计</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MetricsSnapshot snapshot = pool.getMetrics().snapshot();
 * System.out.println("Borrow count: " + snapshot.borrowCount());
 * System.out.println("Active: " + snapshot.currentActive());
 * System.out.println("Avg wait: " + snapshot.avgWaitDuration().toMillis() + "ms");
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 * @param borrowCount       total borrow count - 总借用次数
 * @param returnCount       total return count - 总归还次数
 * @param createdCount      total created count - 总创建次数
 * @param destroyedCount    total destroyed count - 总销毁次数
 * @param currentActive     current active objects - 当前活跃对象数
 * @param currentIdle       current idle objects - 当前空闲对象数
 * @param avgBorrowDuration average borrow duration - 平均借用时长
 * @param maxBorrowDuration maximum borrow duration - 最大借用时长
 * @param avgWaitDuration   average wait duration - 平均等待时长
 * @param timestamp         snapshot timestamp - 快照时间戳
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public record MetricsSnapshot(
        long borrowCount,
        long returnCount,
        long createdCount,
        long destroyedCount,
        int currentActive,
        int currentIdle,
        Duration avgBorrowDuration,
        Duration maxBorrowDuration,
        Duration avgWaitDuration,
        Instant timestamp
) {

    /**
     * Gets the total object count.
     * 获取对象总数。
     *
     * @return the total count - 总数
     */
    public int totalCount() {
        return currentActive + currentIdle;
    }

    /**
     * Gets the utilization rate (active/total).
     * 获取利用率（活跃/总数）。
     *
     * @return the utilization rate (0.0 to 1.0) - 利用率 (0.0到1.0)
     */
    public double utilizationRate() {
        int total = totalCount();
        return total == 0 ? 0.0 : (double) currentActive / total;
    }

    /**
     * Gets the hit rate (returns/borrows).
     * 获取命中率（归还/借用）。
     *
     * @return the hit rate - 命中率
     */
    public double hitRate() {
        return borrowCount == 0 ? 0.0 : (double) returnCount / borrowCount;
    }

    /**
     * Gets the creation rate (created/borrowed).
     * 获取创建率（创建/借用）。
     *
     * @return the creation rate - 创建率
     */
    public double creationRate() {
        return borrowCount == 0 ? 0.0 : (double) createdCount / borrowCount;
    }
}
