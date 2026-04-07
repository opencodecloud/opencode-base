package cloud.opencode.base.observability.metric;

/**
 * Counter - A monotonically increasing counter metric
 * Counter - 单调递增的计数器指标
 *
 * <p>Counters track cumulative values that only increase (or reset).
 * Useful for counting requests, errors, or completed tasks.</p>
 * <p>计数器跟踪仅增加（或重置）的累计值。
 * 适用于计数请求、错误或已完成的任务。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
public interface Counter {

    /**
     * Increments the counter by one.
     * 将计数器加一。
     */
    void increment();

    /**
     * Increments the counter by the given amount.
     * 将计数器增加指定的数量。
     *
     * @param amount the amount to add, must be non-negative | 增加的数量，不能为负
     * @throws cloud.opencode.base.observability.exception.ObservabilityException if amount is negative | 如果数量为负
     */
    void increment(long amount);

    /**
     * Returns the current count.
     * 返回当前计数值。
     *
     * @return the current count | 当前计数
     */
    long count();

    /**
     * Resets the counter to zero.
     * 将计数器重置为零。
     *
     * <p><strong>Concurrency note | 并发注意事项:</strong> Reset is NOT atomic with respect
     * to concurrent {@link #increment()} calls. An increment racing with reset may be lost.
     * This is acceptable for observability counters where approximate values are sufficient.</p>
     * <p>重置操作与并发 {@link #increment()} 调用不是原子的。
     * 与重置竞争的增量可能会丢失。对于近似值足够的可观测性计数器，这是可接受的。</p>
     */
    void reset();

    /**
     * Returns the metric identifier.
     * 返回指标标识符。
     *
     * @return the MetricId | 指标 ID
     */
    MetricId id();
}
