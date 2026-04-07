package cloud.opencode.base.timeseries.interpolation;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import cloud.opencode.base.timeseries.exception.TimeSeriesErrorCode;
import cloud.opencode.base.timeseries.exception.TimeSeriesException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Interpolation Utility - Time series interpolation operations
 * 插值工具 - 时间序列插值操作
 *
 * <p>Provides static utility methods for interpolating time series data using
 * various algorithms: linear, step (LOCF), and natural cubic spline.</p>
 * <p>提供静态工具方法，使用多种算法对时间序列数据进行插值：
 * 线性、阶梯（LOCF）和自然三次样条。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Linear interpolation at regular intervals - 规则间隔的线性插值</li>
 *   <li>Step interpolation (LOCF) at regular intervals - 规则间隔的阶梯插值（LOCF）</li>
 *   <li>Natural cubic spline interpolation - 自然三次样条插值</li>
 *   <li>Point-wise linear interpolation at arbitrary timestamps - 任意时间戳的逐点线性插值</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TimeSeries ts = new TimeSeries("sensor");
 * ts.add(Instant.parse("2026-01-01T00:00:00Z"), 10.0);
 * ts.add(Instant.parse("2026-01-01T00:10:00Z"), 20.0);
 * ts.add(Instant.parse("2026-01-01T00:20:00Z"), 15.0);
 *
 * // Linear interpolation at 1-minute intervals
 * TimeSeries linear = InterpolationUtil.linear(ts, Duration.ofMinutes(1));
 *
 * // Step interpolation (LOCF)
 * TimeSeries stepped = InterpolationUtil.step(ts, Duration.ofMinutes(1));
 *
 * // Natural cubic spline interpolation
 * TimeSeries splined = InterpolationUtil.spline(ts, Duration.ofMinutes(1));
 *
 * // Interpolate at specific timestamps
 * TimeSeries atPoints = InterpolationUtil.interpolateAt(ts,
 *         Instant.parse("2026-01-01T00:05:00Z"),
 *         Instant.parse("2026-01-01T00:15:00Z"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (null arguments throw NullPointerException) - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.3
 */
public final class InterpolationUtil {

    private InterpolationUtil() {
        // Utility class - no instantiation
    }

    /**
     * Linear interpolation at regular intervals
     * 规则间隔的线性插值
     *
     * <p>For each grid point between the first and last timestamp, finds the
     * floor and ceiling entries in the original series and linearly interpolates.
     * If an exact match exists, that value is used directly.</p>
     * <p>对于第一个和最后一个时间戳之间的每个网格点，找到原始序列中的前后条目并进行线性插值。
     * 如果存在精确匹配，则直接使用该值。</p>
     *
     * @param ts the time series to interpolate | 要插值的时间序列
     * @param interval the interval between interpolated points | 插值点之间的间隔
     * @return a new TimeSeries with linearly interpolated values | 带有线性插值值的新 TimeSeries
     * @throws TimeSeriesException if interval is non-positive or series has fewer than 2 points
     *         | 如果间隔为非正数或序列少于2个点
     */
    public static TimeSeries linear(TimeSeries ts, Duration interval) {
        validateInterval(interval);
        if (ts.size() < 2) {
            throw new TimeSeriesException(
                    TimeSeriesErrorCode.INSUFFICIENT_POINTS,
                    "Linear interpolation requires at least 2 points, got: " + ts.size()
            );
        }

        NavigableMap<Instant, DataPoint> map = buildNavigableMap(ts);
        Instant start = map.firstKey();
        Instant end = map.lastKey();
        validateGridSize(start, end, interval);

        TimeSeries result = new TimeSeries(ts.getName() + "_linear");
        long startMillis = start.toEpochMilli();
        long endMillis = end.toEpochMilli();
        long intervalMillis = interval.toMillis();
        for (long ms = startMillis; ms <= endMillis; ms += intervalMillis) {
            Instant t = Instant.ofEpochMilli(ms);
            result.add(t, linearValueAt(map, t));
        }
        return result;
    }

    /**
     * Step interpolation (Last Observation Carried Forward - LOCF) at regular intervals
     * 规则间隔的阶梯插值（末次观测结转 - LOCF）
     *
     * <p>For each grid point, uses the value of the most recent prior data point.
     * If no prior point exists, uses the first available (ceiling) value.</p>
     * <p>对于每个网格点，使用最近先前数据点的值。
     * 如果没有先前点，则使用第一个可用的（上界）值。</p>
     *
     * @param ts the time series to interpolate | 要插值的时间序列
     * @param interval the interval between interpolated points | 插值点之间的间隔
     * @return a new TimeSeries with step-interpolated values | 带有阶梯插值值的新 TimeSeries
     * @throws TimeSeriesException if interval is non-positive | 如果间隔为非正数
     */
    public static TimeSeries step(TimeSeries ts, Duration interval) {
        validateInterval(interval);

        if (ts.isEmpty()) {
            return new TimeSeries(ts.getName() + "_step");
        }

        NavigableMap<Instant, DataPoint> map = buildNavigableMap(ts);
        Instant start = map.firstKey();
        Instant end = map.lastKey();
        validateGridSize(start, end, interval);

        TimeSeries result = new TimeSeries(ts.getName() + "_step");
        long startMillis = start.toEpochMilli();
        long endMillis = end.toEpochMilli();
        long intervalMillis = interval.toMillis();
        for (long ms = startMillis; ms <= endMillis; ms += intervalMillis) {
            Instant t = Instant.ofEpochMilli(ms);
            Map.Entry<Instant, DataPoint> floor = map.floorEntry(t);
            if (floor != null) {
                result.add(t, floor.getValue().value());
            } else {
                Map.Entry<Instant, DataPoint> ceiling = map.ceilingEntry(t);
                if (ceiling != null) {
                    result.add(t, ceiling.getValue().value());
                }
            }
        }
        return result;
    }

    /**
     * Natural cubic spline interpolation at regular intervals
     * 规则间隔的自然三次样条插值
     *
     * <p>Computes natural cubic spline coefficients using the Thomas algorithm
     * (tridiagonal matrix solver) with natural boundary conditions S''(x0) = S''(xn) = 0.
     * Evaluates the spline at each grid point.</p>
     * <p>使用 Thomas 算法（三对角矩阵求解器）计算自然三次样条系数，
     * 自然边界条件 S''(x0) = S''(xn) = 0。在每个网格点评估样条。</p>
     *
     * @param ts the time series to interpolate | 要插值的时间序列
     * @param interval the interval between interpolated points | 插值点之间的间隔
     * @return a new TimeSeries with spline-interpolated values | 带有样条插值值的新 TimeSeries
     * @throws TimeSeriesException if interval is non-positive or series has fewer than 3 points
     *         | 如果间隔为非正数或序列少于3个点
     */
    public static TimeSeries spline(TimeSeries ts, Duration interval) {
        validateInterval(interval);
        if (ts.size() < 3) {
            throw new TimeSeriesException(
                    TimeSeriesErrorCode.INSUFFICIENT_POINTS,
                    "Spline interpolation requires at least 3 points, got: " + ts.size()
            );
        }

        List<DataPoint> points = ts.getPoints();
        int n = points.size();

        // Convert to double arrays: x = time in millis, y = values
        double[] x = new double[n];
        double[] y = new double[n];
        double baseMillis = points.getFirst().epochMillis();
        for (int i = 0; i < n; i++) {
            x[i] = points.get(i).epochMillis() - baseMillis;
            y[i] = points.get(i).value();
        }

        // Compute spline coefficients
        SplineCoefficients coeffs = computeNaturalSpline(x, y);

        // Evaluate at grid points
        Instant start = points.getFirst().timestamp();
        Instant end = points.getLast().timestamp();
        validateGridSize(start, end, interval);

        TimeSeries result = new TimeSeries(ts.getName() + "_spline");
        long startMillis = start.toEpochMilli();
        long endMillis = end.toEpochMilli();
        long intervalMillis = interval.toMillis();
        for (long ms = startMillis; ms <= endMillis; ms += intervalMillis) {
            double xVal = ms - baseMillis;
            double yVal = evaluateSpline(coeffs, x, xVal);
            result.add(Instant.ofEpochMilli(ms), yVal);
        }
        return result;
    }

    /**
     * Linearly interpolate at specific target timestamps
     * 在特定目标时间戳处进行线性插值
     *
     * <p>For each target timestamp, finds the surrounding data points and linearly
     * interpolates. If the target is before the first or after the last data point,
     * extrapolates using the nearest two points.</p>
     * <p>对于每个目标时间戳，找到周围的数据点并进行线性插值。
     * 如果目标在第一个之前或最后一个之后，则使用最近的两个点进行外推。</p>
     *
     * @param ts the time series to interpolate | 要插值的时间序列
     * @param targets the target timestamps | 目标时间戳
     * @return a new TimeSeries with interpolated values at the target timestamps
     *         | 带有目标时间戳处插值值的新 TimeSeries
     * @throws TimeSeriesException if series has fewer than 2 points
     *         | 如果序列少于2个点
     */
    public static TimeSeries interpolateAt(TimeSeries ts, Instant... targets) {
        if (ts.size() < 2) {
            throw new TimeSeriesException(
                    TimeSeriesErrorCode.INSUFFICIENT_POINTS,
                    "interpolateAt requires at least 2 points, got: " + ts.size()
            );
        }
        if (targets == null || targets.length == 0) {
            return new TimeSeries(ts.getName() + "_interpolated");
        }

        NavigableMap<Instant, DataPoint> map = buildNavigableMap(ts);
        TimeSeries result = new TimeSeries(ts.getName() + "_interpolated");

        for (Instant target : targets) {
            double value = interpolateOrExtrapolate(map, target);
            result.add(target, value);
        }
        return result;
    }

    // ==================== Private helpers ====================

    /** Maximum grid points to prevent OOM | 最大网格点数以防止内存溢出 */
    private static final int MAX_GRID_POINTS = 10_000_000;

    /**
     * Validate that the interval is positive and grid size is bounded
     */
    private static void validateInterval(Duration interval) {
        if (interval == null || interval.isZero() || interval.isNegative()) {
            throw new TimeSeriesException(
                    TimeSeriesErrorCode.INVALID_INTERVAL,
                    "Interval must be positive, got: " + interval
            );
        }
    }

    /**
     * Validate estimated grid size to prevent OOM
     */
    private static void validateGridSize(Instant start, Instant end, Duration interval) {
        long rangeMillis = Duration.between(start, end).toMillis();
        long intervalMillis = interval.toMillis();
        if (intervalMillis > 0) {
            long estimated = (rangeMillis / intervalMillis) + 1;
            if (estimated > MAX_GRID_POINTS) {
                throw new TimeSeriesException(
                        TimeSeriesErrorCode.CAPACITY_EXCEEDED,
                        "Interpolation would produce " + estimated + " points (max " + MAX_GRID_POINTS + ")"
                );
            }
        }
    }

    /**
     * Build a NavigableMap from a TimeSeries for O(log n) lookups
     */
    private static NavigableMap<Instant, DataPoint> buildNavigableMap(TimeSeries ts) {
        NavigableMap<Instant, DataPoint> map = new TreeMap<>();
        for (DataPoint p : ts.getPoints()) {
            map.put(p.timestamp(), p);
        }
        return map;
    }

    /**
     * Get the linearly interpolated value at the given timestamp
     */
    private static double linearValueAt(NavigableMap<Instant, DataPoint> map, Instant t) {
        DataPoint exact = map.get(t);
        if (exact != null) {
            return exact.value();
        }

        Map.Entry<Instant, DataPoint> floor = map.floorEntry(t);
        Map.Entry<Instant, DataPoint> ceiling = map.ceilingEntry(t);

        if (floor == null && ceiling == null) {
            return Double.NaN;
        }
        if (floor == null) {
            return ceiling.getValue().value();
        }
        if (ceiling == null) {
            return floor.getValue().value();
        }

        double v1 = floor.getValue().value();
        double v2 = ceiling.getValue().value();
        long t1 = floor.getKey().toEpochMilli();
        long t2 = ceiling.getKey().toEpochMilli();
        long tMillis = t.toEpochMilli();

        if (t1 == t2) {
            return v1;
        }

        return v1 + (v2 - v1) * (double) (tMillis - t1) / (double) (t2 - t1);
    }

    /**
     * Interpolate or extrapolate at a target timestamp
     */
    private static double interpolateOrExtrapolate(NavigableMap<Instant, DataPoint> map, Instant target) {
        DataPoint exact = map.get(target);
        if (exact != null) {
            return exact.value();
        }

        Map.Entry<Instant, DataPoint> floor = map.floorEntry(target);
        Map.Entry<Instant, DataPoint> ceiling = map.ceilingEntry(target);

        // Interior interpolation
        if (floor != null && ceiling != null) {
            return linearBetween(floor, ceiling, target);
        }

        // Extrapolation: target is before first point
        if (floor == null) {
            Map.Entry<Instant, DataPoint> first = map.firstEntry();
            Map.Entry<Instant, DataPoint> second = map.higherEntry(first.getKey());
            if (second == null) {
                return first.getValue().value();
            }
            return linearBetween(first, second, target);
        }

        // Extrapolation: target is after last point
        Map.Entry<Instant, DataPoint> last = map.lastEntry();
        Map.Entry<Instant, DataPoint> secondToLast = map.lowerEntry(last.getKey());
        if (secondToLast == null) {
            return last.getValue().value();
        }
        return linearBetween(secondToLast, last, target);
    }

    /**
     * Linear interpolation/extrapolation between two entries at a target time
     */
    private static double linearBetween(
            Map.Entry<Instant, DataPoint> e1,
            Map.Entry<Instant, DataPoint> e2,
            Instant target) {
        double v1 = e1.getValue().value();
        double v2 = e2.getValue().value();
        long t1 = e1.getKey().toEpochMilli();
        long t2 = e2.getKey().toEpochMilli();
        long tMillis = target.toEpochMilli();

        if (t1 == t2) {
            return v1;
        }

        return v1 + (v2 - v1) * (double) (tMillis - t1) / (double) (t2 - t1);
    }

    // ==================== Spline implementation ====================

    /**
     * Spline coefficients: a, b, c, d for each segment
     * S_i(x) = a[i] + b[i]*(x - x[i]) + c[i]*(x - x[i])^2 + d[i]*(x - x[i])^3
     */
    private record SplineCoefficients(double[] a, double[] b, double[] c, double[] d) {
    }

    /**
     * Compute natural cubic spline coefficients using the Thomas algorithm.
     * Natural boundary conditions: S''(x0) = 0, S''(xn) = 0.
     */
    private static SplineCoefficients computeNaturalSpline(double[] x, double[] y) {
        int n = x.length;
        int segments = n - 1;

        double[] h = new double[segments];
        for (int i = 0; i < segments; i++) {
            h[i] = x[i + 1] - x[i];
            if (h[i] <= 0) {
                throw new TimeSeriesException(
                        TimeSeriesErrorCode.INTERPOLATION_FAILED,
                        "Non-positive interval h[" + i + "]=" + h[i]
                                + " (duplicate or non-monotonic timestamps at millisecond precision)"
                );
            }
        }

        // Set up tridiagonal system for second derivatives (c values)
        // Natural boundary: c[0] = 0, c[n-1] = 0
        // For interior points (1..n-2):
        // h[i-1]*c[i-1] + 2*(h[i-1]+h[i])*c[i] + h[i]*c[i+1] = 3*((y[i+1]-y[i])/h[i] - (y[i]-y[i-1])/h[i-1])

        double[] c = new double[n];
        if (n > 2) {
            int m = n - 2; // Number of interior points
            double[] alpha = new double[m]; // sub-diagonal
            double[] beta = new double[m];  // main diagonal
            double[] gamma = new double[m]; // super-diagonal
            double[] rhs = new double[m];

            for (int i = 0; i < m; i++) {
                int idx = i + 1; // index in original arrays
                alpha[i] = (i > 0) ? h[idx - 1] : 0;
                beta[i] = 2.0 * (h[idx - 1] + h[idx]);
                gamma[i] = (i < m - 1) ? h[idx] : 0;
                rhs[i] = 3.0 * ((y[idx + 1] - y[idx]) / h[idx] - (y[idx] - y[idx - 1]) / h[idx - 1]);
            }

            // Thomas algorithm: forward sweep
            for (int i = 1; i < m; i++) {
                if (Math.abs(beta[i - 1]) < 1e-12) {
                    throw new TimeSeriesException(
                            TimeSeriesErrorCode.INTERPOLATION_FAILED,
                            "Singular tridiagonal matrix in spline interpolation"
                    );
                }
                double w = alpha[i] / beta[i - 1];
                beta[i] -= w * gamma[i - 1];
                rhs[i] -= w * rhs[i - 1];
            }

            // Back substitution
            double[] solution = new double[m];
            if (Math.abs(beta[m - 1]) < 1e-12) {
                throw new TimeSeriesException(
                        TimeSeriesErrorCode.INTERPOLATION_FAILED,
                        "Singular tridiagonal matrix in spline back-substitution"
                );
            }
            solution[m - 1] = rhs[m - 1] / beta[m - 1];
            for (int i = m - 2; i >= 0; i--) {
                if (Math.abs(beta[i]) < 1e-12) {
                    throw new TimeSeriesException(
                            TimeSeriesErrorCode.INTERPOLATION_FAILED,
                            "Singular tridiagonal matrix in spline back-substitution at index " + i
                    );
                }
                solution[i] = (rhs[i] - gamma[i] * solution[i + 1]) / beta[i];
            }

            // Map solution to c array
            c[0] = 0; // Natural boundary
            for (int i = 0; i < m; i++) {
                c[i + 1] = solution[i];
            }
            c[n - 1] = 0; // Natural boundary
        }

        // Compute a, b, d coefficients
        double[] a = new double[segments];
        double[] b = new double[segments];
        double[] d = new double[segments];

        for (int i = 0; i < segments; i++) {
            a[i] = y[i];
            d[i] = (c[i + 1] - c[i]) / (3.0 * h[i]);
            b[i] = (y[i + 1] - y[i]) / h[i] - h[i] * (2.0 * c[i] + c[i + 1]) / 3.0;
        }

        return new SplineCoefficients(a, b, c, d);
    }

    /**
     * Evaluate the spline at a given x value
     */
    private static double evaluateSpline(SplineCoefficients coeffs, double[] x, double xVal) {
        int n = x.length;
        int segments = n - 1;

        // Find the correct segment using binary search
        int i = 0;
        if (xVal <= x[0]) {
            i = 0;
        } else if (xVal >= x[n - 1]) {
            i = segments - 1;
        } else {
            int lo = 0;
            int hi = segments - 1;
            while (lo <= hi) {
                int mid = (lo + hi) >>> 1;
                if (xVal < x[mid]) {
                    hi = mid - 1;
                } else if (xVal >= x[mid + 1]) {
                    lo = mid + 1;
                } else {
                    i = mid;
                    break;
                }
            }
            if (lo > hi) {
                i = Math.max(0, Math.min(lo, segments - 1));
            }
        }

        double dx = xVal - x[i];
        return coeffs.a[i] + coeffs.b[i] * dx + coeffs.c[i] * dx * dx + coeffs.d[i] * dx * dx * dx;
    }
}
