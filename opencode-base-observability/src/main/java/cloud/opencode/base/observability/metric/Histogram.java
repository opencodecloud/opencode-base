package cloud.opencode.base.observability.metric;

/**
 * Histogram - A metric for recording value distributions
 * Histogram - 用于记录值分布的指标
 *
 * <p>Histograms track the distribution of observed values, providing
 * count, sum, max, mean, and percentile calculations.</p>
 * <p>直方图跟踪观察值的分布，提供计数、总和、最大值、均值和百分位数计算。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
public interface Histogram {

    /**
     * Records a value in the histogram.
     * 在直方图中记录一个值。
     *
     * @param value the value to record | 要记录的值
     */
    void record(double value);

    /**
     * Returns the number of recorded values.
     * 返回已记录值的数量。
     *
     * @return the count | 计数
     */
    long count();

    /**
     * Returns the total sum of all recorded values.
     * 返回所有已记录值的总和。
     *
     * @return the total amount | 总量
     */
    double totalAmount();

    /**
     * Returns the maximum recorded value.
     * 返回最大已记录值。
     *
     * @return the max value | 最大值
     */
    double max();

    /**
     * Returns the mean of all recorded values.
     * 返回所有已记录值的均值。
     *
     * @return the mean value | 均值
     */
    double mean();

    /**
     * Returns the value at the given percentile.
     * 返回给定百分位数处的值。
     *
     * @param p the percentile in range [0.0, 1.0] | 百分位数，范围 [0.0, 1.0]
     * @return the value at that percentile | 该百分位数处的值
     * @throws cloud.opencode.base.observability.exception.ObservabilityException if p is outside [0.0, 1.0] | 如果 p 不在 [0.0, 1.0] 范围内
     */
    double percentile(double p);

    /**
     * Returns the metric identifier.
     * 返回指标标识符。
     *
     * @return the MetricId | 指标 ID
     */
    MetricId id();
}
