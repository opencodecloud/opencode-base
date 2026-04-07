package cloud.opencode.base.timeseries.math;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import cloud.opencode.base.timeseries.exception.TimeSeriesErrorCode;
import cloud.opencode.base.timeseries.exception.TimeSeriesException;

import java.util.ArrayList;
import java.util.List;

/**
 * Math Utility for Time Series
 * 时间序列数学工具类
 *
 * <p>Provides mathematical transformation and statistical operations on time series data.</p>
 * <p>提供对时间序列数据的数学变换和统计运算。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Normalization: min-max, z-score standardization - 归一化：最小-最大、Z 分数标准化</li>
 *   <li>Element-wise math: log, log10, exp, abs, scale, offset, power - 逐元素数学运算</li>
 *   <li>Rolling statistics: rolling standard deviation - 滚动统计：滚动标准差</li>
 *   <li>Technical indicators: Bollinger Bands - 技术指标：布林带</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TimeSeries normalized = MathUtil.normalize(series);
 * TimeSeries zScored = MathUtil.zScore(series);
 * TimeSeries scaled = MathUtil.scale(series, 2.0);
 * MathUtil.BollingerBands bands = MathUtil.bollingerBands(series, 20, 2.0);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class, all methods are pure functions) - 线程安全: 是（无状态工具类，所有方法均为纯函数）</li>
 *   <li>Null-safe: No (null TimeSeries arguments will throw NullPointerException) - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.3
 */
public final class MathUtil {

    private MathUtil() {
        // Utility class, no instantiation
    }

    /**
     * Bollinger Bands result
     * 布林带结果
     *
     * @param upper the upper band | 上轨
     * @param middle the middle band (SMA) | 中轨（简单移动平均）
     * @param lower the lower band | 下轨
     */
    public record BollingerBands(TimeSeries upper, TimeSeries middle, TimeSeries lower) {}

    /**
     * Min-max normalization to [0, 1]
     * 最小-最大归一化到 [0, 1]
     *
     * <p>For each point: {@code (value - min) / (max - min)}.
     * If min equals max, all values become 0.0.</p>
     * <p>对每个点：{@code (value - min) / (max - min)}。
     * 若最小值等于最大值，则所有值变为 0.0。</p>
     *
     * @param ts the input time series | 输入时间序列
     * @return normalized time series with values in [0, 1] | 归一化后的时间序列，值在 [0, 1]
     * @throws TimeSeriesException if the series is empty | 若序列为空则抛出异常
     */
    public static TimeSeries normalize(TimeSeries ts) {
        if (ts.isEmpty()) {
            throw new TimeSeriesException(TimeSeriesErrorCode.EMPTY_SERIES);
        }
        // Single pass: compute min/max while collecting points
        List<DataPoint> points = ts.getPoints();
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (DataPoint p : points) {
            double v = p.value();
            if (v < min) min = v;
            if (v > max) max = v;
        }
        double range = max - min;

        List<DataPoint> result = new ArrayList<>(points.size());
        for (DataPoint p : points) {
            double normalized = (range == 0) ? 0.0 : (p.value() - min) / range;
            result.add(DataPoint.of(p.timestamp(), normalized, p.tags()));
        }
        return new TimeSeries(ts.getName() + "_normalized", result);
    }

    /**
     * Z-score standardization
     * Z 分数标准化
     *
     * <p>For each point: {@code (value - mean) / stdDev}.
     * If stdDev is 0, all values become 0.0.</p>
     * <p>对每个点：{@code (value - mean) / stdDev}。
     * 若标准差为 0，则所有值变为 0.0。</p>
     *
     * @param ts the input time series | 输入时间序列
     * @return z-score standardized time series | Z 分数标准化后的时间序列
     * @throws TimeSeriesException if the series is empty | 若序列为空则抛出异常
     */
    public static TimeSeries zScore(TimeSeries ts) {
        if (ts.isEmpty()) {
            throw new TimeSeriesException(TimeSeriesErrorCode.EMPTY_SERIES);
        }
        // Single pass for mean + variance using Welford's algorithm (instead of 3+ separate passes)
        List<DataPoint> points = ts.getPoints();
        long n = 0;
        double mean = 0;
        double m2 = 0;
        for (DataPoint p : points) {
            n++;
            double delta = p.value() - mean;
            mean += delta / n;
            double delta2 = p.value() - mean;
            m2 += delta * delta2;
        }
        double std = (n >= 2) ? Math.sqrt(m2 / (n - 1)) : 0;

        List<DataPoint> result = new ArrayList<>(points.size());
        for (DataPoint p : points) {
            double z = (std == 0) ? 0.0 : (p.value() - mean) / std;
            result.add(DataPoint.of(p.timestamp(), z, p.tags()));
        }
        return new TimeSeries(ts.getName() + "_zscore", result);
    }

    /**
     * Natural logarithm transform
     * 自然对数变换
     *
     * <p>For each point: {@code Math.log(value)}.
     * Values &lt;= 0 produce {@code NaN}.</p>
     * <p>对每个点：{@code Math.log(value)}。
     * 值 &lt;= 0 产生 {@code NaN}。</p>
     *
     * @param ts the input time series | 输入时间序列
     * @return log-transformed time series | 对数变换后的时间序列
     * @throws TimeSeriesException if the series is empty | 若序列为空则抛出异常
     */
    public static TimeSeries log(TimeSeries ts) {
        if (ts.isEmpty()) {
            throw new TimeSeriesException(TimeSeriesErrorCode.EMPTY_SERIES);
        }
        return ts.map(Math::log);
    }

    /**
     * Base-10 logarithm transform
     * 以 10 为底的对数变换
     *
     * <p>For each point: {@code Math.log10(value)}.
     * Values &lt;= 0 produce {@code NaN}.</p>
     * <p>对每个点：{@code Math.log10(value)}。
     * 值 &lt;= 0 产生 {@code NaN}。</p>
     *
     * @param ts the input time series | 输入时间序列
     * @return log10-transformed time series | 以 10 为底对数变换后的时间序列
     * @throws TimeSeriesException if the series is empty | 若序列为空则抛出异常
     */
    public static TimeSeries log10(TimeSeries ts) {
        if (ts.isEmpty()) {
            throw new TimeSeriesException(TimeSeriesErrorCode.EMPTY_SERIES);
        }
        return ts.map(Math::log10);
    }

    /**
     * Exponential transform
     * 指数变换
     *
     * <p>For each point: {@code Math.exp(value)}.</p>
     * <p>对每个点：{@code Math.exp(value)}。</p>
     *
     * @param ts the input time series | 输入时间序列
     * @return exponential-transformed time series | 指数变换后的时间序列
     * @throws TimeSeriesException if the series is empty | 若序列为空则抛出异常
     */
    public static TimeSeries exp(TimeSeries ts) {
        if (ts.isEmpty()) {
            throw new TimeSeriesException(TimeSeriesErrorCode.EMPTY_SERIES);
        }
        return ts.map(Math::exp);
    }

    /**
     * Absolute value transform
     * 绝对值变换
     *
     * <p>For each point: {@code Math.abs(value)}.</p>
     * <p>对每个点：{@code Math.abs(value)}。</p>
     *
     * @param ts the input time series | 输入时间序列
     * @return absolute value time series | 绝对值时间序列
     * @throws TimeSeriesException if the series is empty | 若序列为空则抛出异常
     */
    public static TimeSeries abs(TimeSeries ts) {
        if (ts.isEmpty()) {
            throw new TimeSeriesException(TimeSeriesErrorCode.EMPTY_SERIES);
        }
        return ts.map(Math::abs);
    }

    /**
     * Scale all values by a factor
     * 按因子缩放所有值
     *
     * <p>For each point: {@code value * factor}.</p>
     * <p>对每个点：{@code value * factor}。</p>
     *
     * @param ts the input time series | 输入时间序列
     * @param factor the scaling factor | 缩放因子
     * @return scaled time series | 缩放后的时间序列
     * @throws TimeSeriesException if the series is empty | 若序列为空则抛出异常
     */
    public static TimeSeries scale(TimeSeries ts, double factor) {
        if (ts.isEmpty()) {
            throw new TimeSeriesException(TimeSeriesErrorCode.EMPTY_SERIES);
        }
        return ts.map(v -> v * factor);
    }

    /**
     * Offset all values by a delta
     * 将所有值偏移一个增量
     *
     * <p>For each point: {@code value + delta}.</p>
     * <p>对每个点：{@code value + delta}。</p>
     *
     * @param ts the input time series | 输入时间序列
     * @param delta the offset value | 偏移量
     * @return offset time series | 偏移后的时间序列
     * @throws TimeSeriesException if the series is empty | 若序列为空则抛出异常
     */
    public static TimeSeries offset(TimeSeries ts, double delta) {
        if (ts.isEmpty()) {
            throw new TimeSeriesException(TimeSeriesErrorCode.EMPTY_SERIES);
        }
        return ts.map(v -> v + delta);
    }

    /**
     * Power transform
     * 幂变换
     *
     * <p>For each point: {@code Math.pow(value, exponent)}.</p>
     * <p>对每个点：{@code Math.pow(value, exponent)}。</p>
     *
     * @param ts the input time series | 输入时间序列
     * @param exponent the exponent | 指数
     * @return power-transformed time series | 幂变换后的时间序列
     * @throws TimeSeriesException if the series is empty | 若序列为空则抛出异常
     */
    public static TimeSeries power(TimeSeries ts, double exponent) {
        if (ts.isEmpty()) {
            throw new TimeSeriesException(TimeSeriesErrorCode.EMPTY_SERIES);
        }
        return ts.map(v -> Math.pow(v, exponent));
    }

    /**
     * Rolling standard deviation using Welford's algorithm
     * 使用 Welford 算法计算滚动标准差
     *
     * <p>Computes the sample standard deviation over a sliding window of the given size.
     * Returns a new series with one value per window position.</p>
     * <p>在给定大小的滑动窗口上计算样本标准差。
     * 返回每个窗口位置一个值的新序列。</p>
     *
     * @param ts the input time series | 输入时间序列
     * @param window the window size | 窗口大小
     * @return rolling standard deviation series | 滚动标准差序列
     * @throws TimeSeriesException if window &lt;= 0 or window &gt; series size | 若窗口 &lt;= 0 或窗口 &gt; 序列大小则抛出异常
     */
    public static TimeSeries rollingStdDev(TimeSeries ts, int window) {
        if (window <= 0) {
            throw new TimeSeriesException(TimeSeriesErrorCode.WINDOW_SIZE_INVALID,
                    "window must be > 0, got: " + window);
        }
        if (window > ts.size()) {
            throw new TimeSeriesException(TimeSeriesErrorCode.WINDOW_SIZE_INVALID,
                    "window (" + window + ") must be <= series size (" + ts.size() + ")");
        }

        List<DataPoint> points = ts.getPoints();
        List<DataPoint> result = new ArrayList<>(points.size() - window + 1);

        // Initialize Welford's for first window
        double mean = 0;
        double m2 = 0;
        for (int i = 0; i < window; i++) {
            double val = points.get(i).value();
            double delta = val - mean;
            mean += delta / (i + 1);
            double delta2 = val - mean;
            m2 += delta * delta2;
        }
        double variance = (window > 1) ? m2 / (window - 1) : 0;
        result.add(DataPoint.of(points.get(window - 1).timestamp(), Math.sqrt(variance)));

        // Slide the window: remove oldest, add newest
        for (int i = window; i < points.size(); i++) {
            double oldVal = points.get(i - window).value();
            double newVal = points.get(i).value();

            // Remove old value from Welford's state
            double oldDelta = oldVal - mean;
            mean = (mean * window - oldVal + newVal) / window;
            double newDelta = newVal - mean;
            m2 += (newVal - oldVal) * (oldDelta + newDelta);

            variance = (window > 1) ? m2 / (window - 1) : 0;
            // Guard against floating-point drift producing tiny negative values
            result.add(DataPoint.of(points.get(i).timestamp(), Math.sqrt(Math.max(0, variance))));
        }

        return new TimeSeries(ts.getName() + "_rollingStdDev", result);
    }

    /**
     * Calculate Bollinger Bands
     * 计算布林带
     *
     * <p>Bollinger Bands consist of a middle band (SMA), an upper band (SMA + k * stdDev),
     * and a lower band (SMA - k * stdDev).</p>
     * <p>布林带由中轨（SMA）、上轨（SMA + k * 标准差）和下轨（SMA - k * 标准差）组成。</p>
     *
     * @param ts the input time series | 输入时间序列
     * @param window the moving average window size | 移动平均窗口大小
     * @param numStdDev the number of standard deviations for bands | 带宽的标准差倍数
     * @return Bollinger Bands (upper, middle, lower) | 布林带（上轨、中轨、下轨）
     * @throws TimeSeriesException if window is invalid | 若窗口无效则抛出异常
     */
    public static BollingerBands bollingerBands(TimeSeries ts, int window, double numStdDev) {
        TimeSeries middle = ts.movingAverage(window);
        TimeSeries rollStd = rollingStdDev(ts, window);

        List<DataPoint> middlePoints = middle.getPoints();
        List<DataPoint> stdPoints = rollStd.getPoints();

        List<DataPoint> upperPoints = new ArrayList<>(middlePoints.size());
        List<DataPoint> lowerPoints = new ArrayList<>(middlePoints.size());

        for (int i = 0; i < middlePoints.size(); i++) {
            DataPoint mp = middlePoints.get(i);
            double std = stdPoints.get(i).value();
            upperPoints.add(DataPoint.of(mp.timestamp(), mp.value() + numStdDev * std));
            lowerPoints.add(DataPoint.of(mp.timestamp(), mp.value() - numStdDev * std));
        }

        return new BollingerBands(
                new TimeSeries(ts.getName() + "_bb_upper", upperPoints),
                middle,
                new TimeSeries(ts.getName() + "_bb_lower", lowerPoints)
        );
    }
}
