package cloud.opencode.base.timeseries.rate;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import cloud.opencode.base.timeseries.exception.TimeSeriesErrorCode;
import cloud.opencode.base.timeseries.exception.TimeSeriesException;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Rate Calculation Utility
 * 速率计算工具类
 *
 * <p>Calculates rates from monotonic counters, handling counter resets.
 * Inspired by Prometheus rate()/irate()/increase() functions.</p>
 * <p>从单调递增计数器计算速率，处理计数器重置。
 * 灵感来源于 Prometheus 的 rate()/irate()/increase() 函数。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Per-second rate over sliding window with counter reset handling - 滑动窗口内的每秒速率，处理计数器重置</li>
 *   <li>Instantaneous rate from last two data points - 最后两个数据点的瞬时速率</li>
 *   <li>Total increase over window duration - 窗口时长内的总增量</li>
 *   <li>Counter reset detection and counting - 计数器重置检测与计数</li>
 *   <li>Non-negative derivative calculation - 非负导数计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TimeSeries counter = new TimeSeries("http_requests_total");
 * counter.add(Instant.now(), 100);
 * counter.add(Instant.now().plusSeconds(10), 150);
 *
 * TimeSeries rateTs = RateUtil.rate(counter, Duration.ofMinutes(5));
 * TimeSeries irateTs = RateUtil.irate(counter);
 * int resetCount = RateUtil.resets(counter);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (null arguments throw NullPointerException) - 空值安全: 否（空参数抛出空指针异常）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for all operations - 时间复杂度: 所有操作均为 O(n)</li>
 *   <li>Space complexity: O(n) for result series - 空间复杂度: 结果序列为 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.3
 */
public final class RateUtil {

    private RateUtil() {
        // Utility class
    }

    /**
     * Calculate per-second rate between consecutive points, handling counter resets.
     * 计算连续数据点之间的每秒速率，处理计数器重置。
     *
     * <p>For each consecutive pair of points, computes the per-second rate of increase.
     * Counter resets (where value decreases) are handled by assuming the counter
     * wrapped from zero. The window parameter is reserved for future windowed-rate
     * support and is currently used only for validation.</p>
     * <p>对每对连续数据点计算每秒增长速率。计数器重置（值减少时）假设计数器从零开始重新计数。
     * window 参数为未来窗口化速率保留，当前仅用于验证。</p>
     *
     * @param counter the monotonic counter series | 单调递增计数器序列
     * @param window  the minimum expected window duration (must be positive) | 最小预期窗口时长（必须为正数）
     * @return a new TimeSeries containing per-second rate values | 包含每秒速率值的新时间序列
     * @throws NullPointerException if counter or window is null | 如果参数为空抛出空指针异常
     * @throws TimeSeriesException  if window duration is non-positive | 如果窗口时长非正数抛出异常
     */
    public static TimeSeries rate(TimeSeries counter, Duration window) {
        Objects.requireNonNull(counter, "Counter series must not be null");
        Objects.requireNonNull(window, "Window duration must not be null");
        if (window.isZero() || window.isNegative()) {
            throw new TimeSeriesException(TimeSeriesErrorCode.WINDOW_SIZE_INVALID,
                    "Window duration must be positive, got: " + window);
        }

        List<DataPoint> points = counter.getPoints();
        TimeSeries result = new TimeSeries(counter.getName() + "_rate");

        if (points.size() < 2) {
            return result;
        }

        for (int i = 1; i < points.size(); i++) {
            DataPoint prev = points.get(i - 1);
            DataPoint curr = points.get(i);

            double delta = curr.value() - prev.value();
            if (delta < 0) {
                delta = curr.value();
            }

            // Use primitive subtraction instead of Duration.between() to avoid object allocation
            double timeDeltaSeconds = (curr.epochMillis() - prev.epochMillis()) / 1000.0;
            if (timeDeltaSeconds > 0) {
                result.add(DataPoint.of(curr.timestamp(), delta / timeDeltaSeconds));
            }
        }

        return result;
    }

    /**
     * Calculate instantaneous rate from the last two data points.
     * 从最后两个数据点计算瞬时速率。
     *
     * <p>Takes the last two data points and computes the per-second rate between them,
     * handling counter resets. Returns a single-point series at the last timestamp.</p>
     * <p>取最后两个数据点，计算它们之间的每秒速率，处理计数器重置。
     * 返回最后一个时间戳处的单点序列。</p>
     *
     * @param counter the monotonic counter series | 单调递增计数器序列
     * @return a new TimeSeries with a single rate point | 包含单个速率点的新时间序列
     * @throws NullPointerException if counter is null | 如果参数为空抛出空指针异常
     * @throws TimeSeriesException  if series has fewer than 2 points | 如果序列少于两个点抛出异常
     */
    public static TimeSeries irate(TimeSeries counter) {
        Objects.requireNonNull(counter, "Counter series must not be null");

        List<DataPoint> points = counter.getPoints();
        if (points.size() < 2) {
            throw new TimeSeriesException(TimeSeriesErrorCode.INSUFFICIENT_DATA,
                    "irate requires at least 2 data points, got: " + points.size());
        }

        DataPoint prev = points.get(points.size() - 2);
        DataPoint curr = points.get(points.size() - 1);

        double delta = curr.value() - prev.value();
        if (delta < 0) {
            delta = curr.value();
        }

        double timeDeltaSeconds = (curr.epochMillis() - prev.epochMillis()) / 1000.0;
        TimeSeries result = new TimeSeries(counter.getName() + "_irate");
        if (timeDeltaSeconds > 0) {
            result.add(DataPoint.of(curr.timestamp(), delta / timeDeltaSeconds));
        }
        return result;
    }

    /**
     * Calculate total increase over window duration, handling counter resets.
     * 计算窗口时长内的总增量，处理计数器重置。
     *
     * <p>For each consecutive pair, sums positive deltas and handles resets
     * (negative delta treated as current value). Equivalent to rate * window_seconds.</p>
     * <p>对每对连续数据点，累加正增量并处理重置（负增量视为当前值）。
     * 等价于速率乘以窗口秒数。</p>
     *
     * @param counter the monotonic counter series | 单调递增计数器序列
     * @param window  the window duration | 窗口时长
     * @return a new TimeSeries containing increase values | 包含增量值的新时间序列
     * @throws NullPointerException if counter or window is null | 如果参数为空抛出空指针异常
     * @throws TimeSeriesException  if window duration is non-positive | 如果窗口时长非正数抛出异常
     */
    public static TimeSeries increase(TimeSeries counter, Duration window) {
        Objects.requireNonNull(counter, "Counter series must not be null");
        Objects.requireNonNull(window, "Window duration must not be null");
        if (window.isZero() || window.isNegative()) {
            throw new TimeSeriesException(TimeSeriesErrorCode.WINDOW_SIZE_INVALID,
                    "Window duration must be positive, got: " + window);
        }

        List<DataPoint> points = counter.getPoints();
        TimeSeries result = new TimeSeries(counter.getName() + "_increase");

        if (points.size() < 2) {
            return result;
        }

        for (int i = 1; i < points.size(); i++) {
            DataPoint prev = points.get(i - 1);
            DataPoint curr = points.get(i);

            double delta = curr.value() - prev.value();
            if (delta < 0) {
                delta = curr.value();
            }

            result.add(DataPoint.of(curr.timestamp(), delta));
        }

        return result;
    }

    /**
     * Count the number of counter resets in the series.
     * 计算序列中计数器重置的次数。
     *
     * <p>A counter reset is detected when the current value is less than the previous value.</p>
     * <p>当当前值小于前一个值时检测到计数器重置。</p>
     *
     * @param counter the monotonic counter series | 单调递增计数器序列
     * @return the number of resets | 重置次数
     * @throws NullPointerException if counter is null | 如果参数为空抛出空指针异常
     */
    public static int resets(TimeSeries counter) {
        Objects.requireNonNull(counter, "Counter series must not be null");

        List<DataPoint> points = counter.getPoints();
        int resetCount = 0;

        for (int i = 1; i < points.size(); i++) {
            if (points.get(i).value() < points.get(i - 1).value()) {
                resetCount++;
            }
        }

        return resetCount;
    }

    /**
     * Calculate non-negative derivative of the series.
     * 计算序列的非负导数。
     *
     * <p>Like a standard derivative but treats negative deltas as counter resets:
     * when the value decreases, the increase is assumed to be the current value
     * (counter reset from zero).</p>
     * <p>类似标准导数但将负增量视为计数器重置：当值减少时，增量假设为当前值
     * （计数器从零重置）。</p>
     *
     * @param ts the time series | 时间序列
     * @return a new TimeSeries with non-negative derivative values (per second) | 包含非负导数值（每秒）的新时间序列
     * @throws NullPointerException if ts is null | 如果参数为空抛出空指针异常
     */
    public static TimeSeries nonNegativeDerivative(TimeSeries ts) {
        Objects.requireNonNull(ts, "Time series must not be null");

        List<DataPoint> points = ts.getPoints();
        TimeSeries result = new TimeSeries(ts.getName() + "_nnderiv");

        if (points.size() < 2) {
            return result;
        }

        for (int i = 1; i < points.size(); i++) {
            DataPoint prev = points.get(i - 1);
            DataPoint curr = points.get(i);

            double delta = curr.value() - prev.value();
            if (delta < 0) {
                // Counter reset: treat increase as current value
                delta = curr.value();
            }

            double timeDeltaSeconds = (curr.epochMillis() - prev.epochMillis()) / 1000.0;
            if (timeDeltaSeconds > 0) {
                result.add(DataPoint.of(curr.timestamp(), delta / timeDeltaSeconds));
            }
        }

        return result;
    }
}
