package cloud.opencode.base.observability.metric;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Timer - A metric for measuring duration of operations
 * Timer - 用于测量操作持续时间的指标
 *
 * <p>Timers record duration values and provide aggregated statistics
 * such as count, total time, max, and mean.</p>
 * <p>计时器记录持续时间值并提供聚合统计信息，如计数、总时间、最大值和均值。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
public interface Timer {

    /**
     * Records the given duration.
     * 记录给定的持续时间。
     *
     * @param duration the duration to record | 要记录的持续时间
     * @throws cloud.opencode.base.observability.exception.ObservabilityException if duration is negative | 如果持续时间为负
     */
    void record(Duration duration);

    /**
     * Times the given task and records its duration.
     * 计时给定任务并记录其持续时间。
     *
     * @param task the task to time | 要计时的任务
     * @throws cloud.opencode.base.observability.exception.ObservabilityException if task is null | 如果任务为 null
     */
    void time(Runnable task);

    /**
     * Times the given callable task and records its duration.
     * 计时给定的可调用任务并记录其持续时间。
     *
     * @param <T>  the return type | 返回类型
     * @param task the task to time | 要计时的任务
     * @return the task result | 任务结果
     * @throws cloud.opencode.base.observability.exception.ObservabilityException if task is null or throws a checked exception | 如果任务为 null 或抛出受检异常
     */
    <T> T time(Callable<T> task);

    /**
     * Returns the number of recorded durations.
     * 返回已记录的持续时间数量。
     *
     * @return the count | 计数
     */
    long count();

    /**
     * Returns the total recorded time.
     * 返回总记录时间。
     *
     * @return the total time | 总时间
     */
    Duration totalTime();

    /**
     * Returns the maximum recorded duration.
     * 返回最大已记录持续时间。
     *
     * @return the max duration | 最大持续时间
     */
    Duration max();

    /**
     * Returns the mean recorded duration.
     * 返回平均已记录持续时间。
     *
     * @return the mean duration | 平均持续时间
     */
    Duration mean();

    /**
     * Returns the metric identifier.
     * 返回指标标识符。
     *
     * @return the MetricId | 指标 ID
     */
    MetricId id();
}
