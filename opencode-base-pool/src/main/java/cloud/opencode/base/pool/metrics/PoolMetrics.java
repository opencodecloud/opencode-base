package cloud.opencode.base.pool.metrics;

import java.time.Duration;

/**
 * PoolMetrics - Pool Metrics Interface
 * PoolMetrics - 池指标接口
 *
 * <p>Interface for accessing pool performance metrics and statistics.</p>
 * <p>访问池性能指标和统计信息的接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Borrow/return counters - 借用/归还计数器</li>
 *   <li>Create/destroy counters - 创建/销毁计数器</li>
 *   <li>Duration statistics - 时长统计</li>
 *   <li>Snapshot capability - 快照能力</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PoolMetrics metrics = pool.getMetrics();
 * System.out.println("Borrow count: " + metrics.getBorrowCount());
 * System.out.println("Avg wait: " + metrics.getAverageWaitDuration().toMillis() + "ms");
 *
 * MetricsSnapshot snapshot = metrics.snapshot();
 * // Export or log the snapshot
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public interface PoolMetrics {

    /**
     * Gets the total borrow count.
     * 获取总借用次数。
     *
     * @return the borrow count - 借用次数
     */
    long getBorrowCount();

    /**
     * Gets the total return count.
     * 获取总归还次数。
     *
     * @return the return count - 归还次数
     */
    long getReturnCount();

    /**
     * Gets the total created count.
     * 获取总创建次数。
     *
     * @return the created count - 创建次数
     */
    long getCreatedCount();

    /**
     * Gets the total destroyed count.
     * 获取总销毁次数。
     *
     * @return the destroyed count - 销毁次数
     */
    long getDestroyedCount();

    /**
     * Gets the average borrow duration.
     * 获取平均借用时长。
     *
     * @return the average borrow duration - 平均借用时长
     */
    Duration getAverageBorrowDuration();

    /**
     * Gets the maximum borrow duration.
     * 获取最大借用时长。
     *
     * @return the maximum borrow duration - 最大借用时长
     */
    Duration getMaxBorrowDuration();

    /**
     * Gets the average wait duration.
     * 获取平均等待时长。
     *
     * @return the average wait duration - 平均等待时长
     */
    Duration getAverageWaitDuration();

    /**
     * Creates a snapshot of current metrics.
     * 创建当前指标的快照。
     *
     * @return the metrics snapshot - 指标快照
     */
    MetricsSnapshot snapshot();
}
