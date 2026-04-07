package cloud.opencode.base.math.stats;

import java.util.Arrays;

/**
 * Dedicated percentile calculator with configurable interpolation strategy.
 * 支持可配置插值策略的专用百分位数计算器
 *
 * <p>Instances are immutable and thread-safe. The input data is copied and sorted upon creation.</p>
 * <p>实例不可变且线程安全。输入数据在创建时被复制并排序。</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * Percentile p = Percentile.of(new double[]{1, 2, 3, 4, 5});
 * double median = p.value(50);
 * double q1 = p.quartile(1);
 * double q3withMethod = p.value(75, Percentile.Method.HIGHER);
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class Percentile {

    private final double[] sorted;

    private Percentile(double[] sorted) {
        this.sorted = sorted;
    }

    /**
     * Creates a percentile calculator from the given data.
     * 根据给定数据创建百分位数计算器
     *
     * @param data the input data array (will be copied) / 输入数据数组（将被复制）
     * @return a new {@link Percentile} instance / 新的百分位数计算器实例
     * @throws IllegalArgumentException if data is null or empty
     */
    public static Percentile of(double[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null");
        }
        if (data.length == 0) {
            throw new IllegalArgumentException("data must not be empty");
        }
        for (int i = 0; i < data.length; i++) {
            if (!Double.isFinite(data[i])) {
                throw new IllegalArgumentException("data[" + i + "] is not finite: " + data[i]);
            }
        }
        double[] copy = Arrays.copyOf(data, data.length);
        Arrays.sort(copy);
        return new Percentile(copy);
    }

    /**
     * Computes the p-th percentile using the default LINEAR interpolation method.
     * 使用默认的 LINEAR 插值方法计算第 p 百分位数
     *
     * @param p the percentile in [0, 100] / 百分位数，范围 [0, 100]
     * @return the percentile value / 百分位数值
     * @throws IllegalArgumentException if p is out of range
     */
    public double value(double p) {
        return value(p, Method.LINEAR);
    }

    /**
     * Computes the p-th percentile using the specified interpolation method.
     * 使用指定的插值方法计算第 p 百分位数
     *
     * @param p      the percentile in [0, 100] / 百分位数，范围 [0, 100]
     * @param method the interpolation method / 插值方法
     * @return the percentile value / 百分位数值
     * @throws IllegalArgumentException if p is out of range or method is null
     */
    public double value(double p, Method method) {
        if (Double.isNaN(p) || p < 0 || p > 100) {
            throw new IllegalArgumentException("Percentile p must be in [0, 100], got: " + p);
        }
        if (method == null) {
            throw new IllegalArgumentException("method must not be null");
        }
        int n = sorted.length;
        if (n == 1) {
            return sorted[0];
        }
        double rank = p / 100.0 * (n - 1);
        int lower = (int) Math.floor(rank);
        int upper = (int) Math.ceil(rank);
        if (lower < 0) lower = 0;
        if (upper >= n) upper = n - 1;

        return switch (method) {
            case LINEAR -> {
                if (lower == upper) {
                    yield sorted[lower];
                }
                double frac = rank - lower;
                yield sorted[lower] + frac * (sorted[upper] - sorted[lower]);
            }
            case LOWER -> sorted[lower];
            case HIGHER -> sorted[upper];
            case NEAREST -> {
                double frac = rank - lower;
                yield frac <= 0.5 ? sorted[lower] : sorted[upper];
            }
            case MIDPOINT -> (sorted[lower] + sorted[upper]) / 2.0;
        };
    }

    /**
     * Computes a quartile value (Q1, Q2, or Q3).
     * 计算四分位数值（Q1、Q2 或 Q3）
     *
     * @param q the quartile number: 1 (Q1=25th), 2 (Q2=50th median), or 3 (Q3=75th) / 四分位数编号
     * @return the quartile value / 四分位数值
     * @throws IllegalArgumentException if q is not 1, 2, or 3
     */
    public double quartile(int q) {
        if (q < 1 || q > 3) {
            throw new IllegalArgumentException("Quartile q must be 1, 2, or 3, got: " + q);
        }
        return value(q * 25.0);
    }

    /**
     * Interpolation method for percentile calculation.
     * 百分位数计算的插值方法
     *
     * @author Leon Soo
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-math V1.0.3
     */
    public enum Method {
        /**
         * Linear interpolation between adjacent data points.
         * 相邻数据点之间的线性插值
         */
        LINEAR,
        /**
         * Returns the lower of the two adjacent data points.
         * 返回两个相邻数据点中较低的值
         */
        LOWER,
        /**
         * Returns the higher of the two adjacent data points.
         * 返回两个相邻数据点中较高的值
         */
        HIGHER,
        /**
         * Returns the nearest data point (lower on tie).
         * 返回最近的数据点（相等时取较低值）
         */
        NEAREST,
        /**
         * Returns the midpoint of the two adjacent data points.
         * 返回两个相邻数据点的中点
         */
        MIDPOINT
    }
}
