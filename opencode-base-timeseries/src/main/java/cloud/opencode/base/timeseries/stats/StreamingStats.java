package cloud.opencode.base.timeseries.stats;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import cloud.opencode.base.timeseries.TimeSeriesStats;

/**
 * Streaming Statistics Calculator using Welford's Online Algorithm
 * 基于 Welford 在线算法的流式统计计算器
 *
 * <p>A mutable, stateful object that computes running statistics (count, sum, mean,
 * min, max, variance, standard deviation) in a single pass with O(1) per-update cost
 * and numerically stable variance computation.</p>
 * <p>一个可变的有状态对象，使用单次遍历以 O(1) 每次更新的代价计算运行统计量
 * （计数、总和、均值、最小值、最大值、方差、标准差），方差计算数值稳定。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Welford's algorithm for numerically stable online variance - Welford 算法实现数值稳定的在线方差</li>
 *   <li>O(1) per-update for all statistics - 所有统计量每次更新 O(1)</li>
 *   <li>Parallel merge support for combining partial results - 支持合并部分结果的并行合并</li>
 *   <li>Snapshot to immutable TimeSeriesStats record - 快照为不可变 TimeSeriesStats 记录</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * StreamingStats stats = new StreamingStats();
 * stats.add(10.0);
 * stats.add(20.0);
 * stats.add(30.0);
 *
 * double avg = stats.mean();      // 20.0
 * double std = stats.stdDev();    // ~10.0
 *
 * // Merge partial results
 * StreamingStats other = new StreamingStats();
 * other.addAll(anotherSeries);
 * stats.merge(other);
 *
 * // Snapshot to immutable record
 * TimeSeriesStats snapshot = stats.snapshot();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable state; synchronize externally if needed) - 线程安全: 否（可变状态；如需线程安全请外部同步）</li>
 *   <li>Null-safe: No (null DataPoint throws NullPointerException) - 空值安全: 否</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per add, O(n) for addAll, O(1) for merge - 时间复杂度: add O(1)，addAll O(n)，merge O(1)</li>
 *   <li>Space complexity: O(1) - constant state regardless of data size - 空间复杂度: O(1) - 与数据量无关的常量状态</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.3
 */
public class StreamingStats {

    private long count;
    private double sum;
    private double min;
    private double max;
    private double mean;
    private double m2;

    /**
     * Create a new empty StreamingStats.
     * 创建一个新的空 StreamingStats。
     */
    public StreamingStats() {
        reset();
    }

    /**
     * Add a single value to the running statistics.
     * 向运行统计量中添加单个值。
     *
     * <p>Updates count, sum, min, max, mean, and M2 (sum of squared deviations)
     * using Welford's online algorithm.</p>
     * <p>使用 Welford 在线算法更新计数、总和、最小值、最大值、均值和 M2（偏差平方和）。</p>
     *
     * @param value the value to add (must be finite) | 要添加的值（必须为有限数）
     * @throws IllegalArgumentException if value is NaN or Infinite | 如果值为 NaN 或无穷大抛出异常
     */
    public void add(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("Value must be finite, got: " + value);
        }

        count++;
        sum += value;
        min = Math.min(min, value);
        max = Math.max(max, value);

        double delta = value - mean;
        mean += delta / count;
        double delta2 = value - mean;
        m2 += delta * delta2;
    }

    /**
     * Add a data point's value to the running statistics.
     * 向运行统计量中添加数据点的值。
     *
     * @param point the data point | 数据点
     * @throws NullPointerException     if point is null | 如果数据点为空抛出空指针异常
     * @throws IllegalArgumentException if value is NaN or Infinite | 如果值为 NaN 或无穷大抛出异常
     */
    public void add(DataPoint point) {
        add(point.value());
    }

    /**
     * Add all data points from a time series.
     * 添加时间序列中的所有数据点。
     *
     * @param ts the time series | 时间序列
     * @throws NullPointerException if ts is null | 如果时间序列为空抛出空指针异常
     */
    public void addAll(TimeSeries ts) {
        for (DataPoint point : ts.getPoints()) {
            add(point.value());
        }
    }

    /**
     * Get the number of values added.
     * 获取已添加的值数量。
     *
     * @return the count | 计数
     */
    public long count() {
        return count;
    }

    /**
     * Get the sum of all values.
     * 获取所有值的总和。
     *
     * @return the sum, or 0.0 if empty | 总和，如果为空返回 0.0
     */
    public double sum() {
        return count == 0 ? 0.0 : sum;
    }

    /**
     * Get the arithmetic mean of all values.
     * 获取所有值的算术平均值。
     *
     * @return the mean, or NaN if empty | 均值，如果为空返回 NaN
     */
    public double mean() {
        return count == 0 ? Double.NaN : mean;
    }

    /**
     * Get the minimum value.
     * 获取最小值。
     *
     * @return the minimum, or NaN if empty | 最小值，如果为空返回 NaN
     */
    public double min() {
        return count == 0 ? Double.NaN : min;
    }

    /**
     * Get the maximum value.
     * 获取最大值。
     *
     * @return the maximum, or NaN if empty | 最大值，如果为空返回 NaN
     */
    public double max() {
        return count == 0 ? Double.NaN : max;
    }

    /**
     * Get the sample variance (Bessel's correction: divided by n-1).
     * 获取样本方差（贝塞尔校正：除以 n-1）。
     *
     * @return the variance, or 0.0 if fewer than 2 values | 方差，如果少于两个值返回 0.0
     */
    public double variance() {
        return count >= 2 ? m2 / (count - 1) : 0.0;
    }

    /**
     * Get the sample standard deviation.
     * 获取样本标准差。
     *
     * @return the standard deviation, or 0.0 if fewer than 2 values | 标准差，如果少于两个值返回 0.0
     */
    public double stdDev() {
        return Math.sqrt(variance());
    }

    /**
     * Create an immutable snapshot as a TimeSeriesStats record.
     * 创建不可变快照为 TimeSeriesStats 记录。
     *
     * @return the statistics snapshot | 统计快照
     */
    public TimeSeriesStats snapshot() {
        if (count == 0) {
            return TimeSeriesStats.empty();
        }
        return new TimeSeriesStats(count, sum, mean, min, max, stdDev());
    }

    /**
     * Merge another StreamingStats into this one using parallel Welford merge.
     * 使用并行 Welford 合并将另一个 StreamingStats 合并到当前实例。
     *
     * <p>Combines two independently computed partial statistics into a single result.
     * This enables parallel/distributed computation patterns.</p>
     * <p>将两个独立计算的部分统计量合并为单个结果。支持并行/分布式计算模式。</p>
     *
     * @param other the other stats to merge | 要合并的另一个统计量
     * @throws NullPointerException if other is null | 如果参数为空抛出空指针异常
     */
    public void merge(StreamingStats other) {
        if (other == null) {
            throw new NullPointerException("Other stats must not be null");
        }
        if (other.count == 0) {
            return;
        }
        if (this.count == 0) {
            this.count = other.count;
            this.sum = other.sum;
            this.min = other.min;
            this.max = other.max;
            this.mean = other.mean;
            this.m2 = other.m2;
            return;
        }

        long combinedCount = this.count + other.count;
        double delta = other.mean - this.mean;
        double combinedMean = this.mean + delta * other.count / combinedCount;
        double combinedM2 = this.m2 + other.m2
                + delta * delta * this.count * other.count / combinedCount;

        this.count = combinedCount;
        this.sum += other.sum;
        this.min = Math.min(this.min, other.min);
        this.max = Math.max(this.max, other.max);
        this.mean = combinedMean;
        this.m2 = combinedM2;
    }

    /**
     * Reset to initial empty state.
     * 重置为初始空状态。
     */
    public void reset() {
        this.count = 0;
        this.sum = 0.0;
        this.min = Double.MAX_VALUE;
        this.max = -Double.MAX_VALUE;
        this.mean = 0.0;
        this.m2 = 0.0;
    }

    @Override
    public String toString() {
        if (count == 0) {
            return "StreamingStats[empty]";
        }
        return String.format(
                "StreamingStats[count=%d, sum=%.2f, mean=%.2f, min=%.2f, max=%.2f, stdDev=%.2f]",
                count, sum, mean, min, max, stdDev());
    }
}
