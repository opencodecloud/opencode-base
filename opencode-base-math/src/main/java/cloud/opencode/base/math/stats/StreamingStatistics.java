package cloud.opencode.base.math.stats;

import cloud.opencode.base.math.exception.MathException;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Mutable accumulator for computing streaming (online) statistics using Welford's algorithm.
 * 使用 Welford 算法计算流式（在线）统计的可变累加器
 *
 * <p>Computes count, mean, variance, standard deviation, min, max, and sum in a single pass.
 * Uses Welford's numerically stable online algorithm for variance computation.</p>
 * <p>在单次遍历中计算计数、均值、方差、标准差、最小值、最大值和总和。
 * 使用 Welford 的数值稳定在线算法计算方差。</p>
 *
 * <p><strong>NOT thread-safe.</strong> For parallel processing, create separate instances
 * and combine them using {@link #merge(StreamingStatistics)}.</p>
 * <p><strong>非线程安全。</strong>对于并行处理，请创建单独的实例并使用
 * {@link #merge(StreamingStatistics)} 合并。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class StreamingStatistics {

    private long count;
    private double mean;
    private double m2; // sum of squared deviations from mean
    private double min;
    private double max;
    private double sum;

    private StreamingStatistics() {
        this.count = 0;
        this.mean = 0.0;
        this.m2 = 0.0;
        this.min = Double.POSITIVE_INFINITY;
        this.max = Double.NEGATIVE_INFINITY;
        this.sum = 0.0;
    }

    private StreamingStatistics(long count, double mean, double m2, double min, double max, double sum) {
        this.count = count;
        this.mean = mean;
        this.m2 = m2;
        this.min = min;
        this.max = max;
        this.sum = sum;
    }

    /**
     * Creates a new empty streaming statistics accumulator.
     * 创建新的空流式统计累加器
     *
     * @return a new empty instance / 新的空实例
     */
    public static StreamingStatistics create() {
        return new StreamingStatistics();
    }

    /**
     * Adds a value to the accumulator using Welford's online algorithm.
     * 使用 Welford 在线算法向累加器添加一个值
     *
     * @param value the value to add (must be finite) / 要添加的值（必须是有限数）
     * @throws MathException if value is NaN or Infinity / 如果值为 NaN 或无穷大
     */
    public void add(double value) {
        if (!Double.isFinite(value)) {
            throw new MathException("Value must be finite, got: " + value);
        }
        count++;
        sum += value;

        // Welford's online algorithm
        double delta = value - mean;
        mean += delta / count;
        double delta2 = value - mean;
        m2 += delta * delta2;

        if (value < min) {
            min = value;
        }
        if (value > max) {
            max = value;
        }
    }

    /**
     * Returns the number of values added.
     * 返回已添加的值的数量
     *
     * @return the count / 计数
     */
    public long count() {
        return count;
    }

    /**
     * Returns the arithmetic mean of all added values.
     * 返回所有已添加值的算术平均值
     *
     * @return the mean / 平均值
     * @throws MathException if no values have been added / 如果没有添加任何值
     */
    public double mean() {
        requireNonEmpty();
        return mean;
    }

    /**
     * Returns the population variance (biased, divided by n).
     * 返回总体方差（有偏，除以 n）
     *
     * @return the population variance / 总体方差
     * @throws MathException if no values have been added / 如果没有添加任何值
     */
    public double variance() {
        requireNonEmpty();
        return m2 / count;
    }

    /**
     * Returns the sample variance (unbiased, divided by n-1).
     * 返回样本方差（无偏，除以 n-1）
     *
     * @return the sample variance / 样本方差
     * @throws MathException if fewer than 2 values have been added / 如果添加的值少于 2 个
     */
    public double sampleVariance() {
        if (count < 2) {
            throw new MathException("Sample variance requires at least 2 values, got: " + count);
        }
        return m2 / (count - 1);
    }

    /**
     * Returns the population standard deviation (sqrt of population variance).
     * 返回总体标准差（总体方差的平方根）
     *
     * @return the population standard deviation / 总体标准差
     * @throws MathException if no values have been added / 如果没有添加任何值
     */
    public double stdDev() {
        return Math.sqrt(variance());
    }

    /**
     * Returns the sample standard deviation (sqrt of sample variance).
     * 返回样本标准差（样本方差的平方根）
     *
     * @return the sample standard deviation / 样本标准差
     * @throws MathException if fewer than 2 values have been added / 如果添加的值少于 2 个
     */
    public double sampleStdDev() {
        return Math.sqrt(sampleVariance());
    }

    /**
     * Returns the minimum value added.
     * 返回已添加的最小值
     *
     * @return the minimum value / 最小值
     * @throws MathException if no values have been added / 如果没有添加任何值
     */
    public double min() {
        requireNonEmpty();
        return min;
    }

    /**
     * Returns the maximum value added.
     * 返回已添加的最大值
     *
     * @return the maximum value / 最大值
     * @throws MathException if no values have been added / 如果没有添加任何值
     */
    public double max() {
        requireNonEmpty();
        return max;
    }

    /**
     * Returns the sum of all added values.
     * 返回所有已添加值的总和
     *
     * @return the sum / 总和
     * @throws MathException if no values have been added / 如果没有添加任何值
     */
    public double sum() {
        requireNonEmpty();
        return sum;
    }

    /**
     * Merges this accumulator with another, returning a NEW instance.
     * Both input instances remain unchanged.
     * 将此累加器与另一个合并，返回新实例。两个输入实例保持不变。
     *
     * <p>Uses the parallel Welford merge formula for numerical stability.</p>
     * <p>使用并行 Welford 合并公式以保证数值稳定性。</p>
     *
     * @param other the other accumulator to merge / 要合并的另一个累加器
     * @return a new merged instance / 新的合并实例
     * @throws MathException if other is null / 如果 other 为 null
     */
    public StreamingStatistics merge(StreamingStatistics other) {
        if (other == null) {
            throw new MathException("other must not be null");
        }
        if (other.count == 0) {
            return new StreamingStatistics(count, mean, m2, min, max, sum);
        }
        if (this.count == 0) {
            return new StreamingStatistics(other.count, other.mean, other.m2, other.min, other.max, other.sum);
        }

        long newCount = Math.addExact(this.count, other.count);
        double delta = other.mean - this.mean;
        double newMean = (this.sum + other.sum) / newCount;
        // Parallel Welford merge: M2_combined = M2_a + M2_b + delta^2 * nA * nB / (nA + nB)
        double newM2 = this.m2 + other.m2 + delta * delta * ((double) this.count * other.count / newCount);
        double newMin = Math.min(this.min, other.min);
        double newMax = Math.max(this.max, other.max);
        double newSum = this.sum + other.sum;

        return new StreamingStatistics(newCount, newMean, newM2, newMin, newMax, newSum);
    }

    /**
     * Resets all accumulated state to empty.
     * 重置所有累积状态为空
     */
    public void reset() {
        count = 0;
        mean = 0.0;
        m2 = 0.0;
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
        sum = 0.0;
    }

    /**
     * Returns a {@link Collector} for use with {@link java.util.stream.Stream#collect(Collector)}.
     * 返回用于 {@link java.util.stream.Stream#collect(Collector)} 的 {@link Collector}
     *
     * <p>Example usage: {@code stream.collect(StreamingStatistics.collector())}</p>
     * <p>用法示例：{@code stream.collect(StreamingStatistics.collector())}</p>
     *
     * @return a collector that produces a StreamingStatistics / 生成 StreamingStatistics 的收集器
     */
    public static Collector<Double, StreamingStatistics, StreamingStatistics> collector() {
        return new Collector<>() {
            @Override
            public Supplier<StreamingStatistics> supplier() {
                return StreamingStatistics::create;
            }

            @Override
            public BiConsumer<StreamingStatistics, Double> accumulator() {
                return StreamingStatistics::add;
            }

            @Override
            public BinaryOperator<StreamingStatistics> combiner() {
                return StreamingStatistics::merge;
            }

            @Override
            public Function<StreamingStatistics, StreamingStatistics> finisher() {
                return Function.identity();
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.unmodifiableSet(Set.of(Characteristics.IDENTITY_FINISH));
            }
        };
    }

    @Override
    public String toString() {
        if (count == 0) {
            return "StreamingStatistics{count=0}";
        }
        return "StreamingStatistics{count=" + count
                + ", mean=" + mean
                + ", variance=" + variance()
                + ", min=" + min
                + ", max=" + max
                + ", sum=" + sum + "}";
    }

    private void requireNonEmpty() {
        if (count == 0) {
            throw new MathException("No values have been added");
        }
    }
}
