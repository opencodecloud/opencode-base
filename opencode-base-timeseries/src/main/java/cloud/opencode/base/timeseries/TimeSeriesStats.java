package cloud.opencode.base.timeseries;

/**
 * Time Series Stats
 * 时间序列统计
 *
 * <p>Statistics for a time series.</p>
 * <p>时间序列的统计信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable record with count, sum, average, min, max, stdDev - 不可变记录，包含计数、总和、平均、最小、最大、标准差</li>
 *   <li>Derived metrics: range and variance - 派生指标：范围和方差</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TimeSeriesStats stats = timeSeries.stats();
 * double avg = stats.average();
 * double range = stats.range();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: N/A (primitive fields) - 空值安全: 不适用（基本类型字段）</li>
 * </ul>
 *
 * @param count the number of data points | 数据点数量
 * @param sum the sum of values | 值的总和
 * @param average the average value | 平均值
 * @param min the minimum value | 最小值
 * @param max the maximum value | 最大值
 * @param stdDev the standard deviation | 标准差
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public record TimeSeriesStats(
    long count,
    double sum,
    double average,
    double min,
    double max,
    double stdDev
) {

    /**
     * Create empty stats
     * 创建空统计
     *
     * @return empty stats | 空统计
     */
    public static TimeSeriesStats empty() {
        return new TimeSeriesStats(0, 0, 0, 0, 0, 0);
    }

    /**
     * Check if stats are empty
     * 检查统计是否为空
     *
     * @return true if empty | 如果为空返回true
     */
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * Get the range (max - min)
     * 获取范围（最大值-最小值）
     *
     * @return the range | 范围
     */
    public double range() {
        return max - min;
    }

    /**
     * Get variance (stdDev^2)
     * 获取方差（标准差的平方）
     *
     * @return the variance | 方差
     */
    public double variance() {
        return stdDev * stdDev;
    }

    @Override
    public String toString() {
        return String.format(
            "TimeSeriesStats[count=%d, sum=%.2f, avg=%.2f, min=%.2f, max=%.2f, stdDev=%.2f]",
            count, sum, average, min, max, stdDev
        );
    }
}
