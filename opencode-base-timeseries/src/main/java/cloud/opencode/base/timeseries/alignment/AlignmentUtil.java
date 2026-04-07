package cloud.opencode.base.timeseries.alignment;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import cloud.opencode.base.timeseries.exception.TimeSeriesErrorCode;
import cloud.opencode.base.timeseries.exception.TimeSeriesException;
import cloud.opencode.base.timeseries.sampling.AggregationType;
import cloud.opencode.base.timeseries.sampling.FillStrategy;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Alignment Utility - Time series alignment and resampling operations
 * 对齐工具 - 时间序列对齐和重采样操作
 *
 * <p>Provides static utility methods for aligning time series to regular grids,
 * resampling at fixed intervals, and snapping timestamps to grid boundaries
 * with configurable aggregation.</p>
 * <p>提供静态工具方法，用于将时间序列对齐到规则网格、按固定间隔重采样、
 * 以及将时间戳对齐到网格边界并进行可配置聚合。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Align two series to a common regular time grid - 将两个序列对齐到公共规则时间网格</li>
 *   <li>Resample a single series to regular intervals - 将单个序列重采样为规则间隔</li>
 *   <li>Snap timestamps to grid boundaries with aggregation - 将时间戳对齐到网格边界并聚合</li>
 *   <li>Multiple fill strategies (ZERO, PREVIOUS, LINEAR, NAN, NEXT, AVERAGE) - 多种填充策略</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TimeSeries a = new TimeSeries("cpu");
 * TimeSeries b = new TimeSeries("mem");
 * // Align both series to 1-minute intervals with linear interpolation
 * TimeSeries[] aligned = AlignmentUtil.align(a, b, Duration.ofMinutes(1), FillStrategy.LINEAR);
 *
 * // Resample a single series to 5-second intervals
 * TimeSeries resampled = AlignmentUtil.resample(a, Duration.ofSeconds(5), FillStrategy.PREVIOUS);
 *
 * // Snap to 1-minute grid and average within each bucket
 * TimeSeries gridded = AlignmentUtil.alignToGrid(a, Duration.ofMinutes(1), AggregationType.AVG);
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
public final class AlignmentUtil {

    private AlignmentUtil() {
        // Utility class - no instantiation
    }

    /**
     * Align two time series to a common regular time grid
     * 将两个时间序列对齐到公共规则时间网格
     *
     * <p>Determines the overlapping time range of both series and creates
     * a regular grid of timestamps at the specified interval. Each series
     * is resampled to this grid using the given fill strategy.</p>
     * <p>确定两个序列的重叠时间范围，并以指定间隔创建规则时间戳网格。
     * 每个序列使用给定的填充策略重采样到该网格。</p>
     *
     * @param a the first time series | 第一个时间序列
     * @param b the second time series | 第二个时间序列
     * @param interval the regular interval between grid points | 网格点之间的规则间隔
     * @param fill the fill strategy for missing values | 缺失值的填充策略
     * @return array of two aligned TimeSeries | 两个对齐的 TimeSeries 数组
     * @throws TimeSeriesException if interval is non-positive | 如果间隔为非正数
     */
    public static TimeSeries[] align(TimeSeries a, TimeSeries b, Duration interval, FillStrategy fill) {
        validateInterval(interval);

        if (a.isEmpty() || b.isEmpty()) {
            return new TimeSeries[]{
                    new TimeSeries(a.getName() + "_aligned"),
                    new TimeSeries(b.getName() + "_aligned")
            };
        }

        // Determine overlapping range: max of starts, min of ends
        Instant startA = a.getFirst().orElseThrow().timestamp();
        Instant endA = a.getLast().orElseThrow().timestamp();
        Instant startB = b.getFirst().orElseThrow().timestamp();
        Instant endB = b.getLast().orElseThrow().timestamp();

        Instant overlapStart = startA.isAfter(startB) ? startA : startB;
        Instant overlapEnd = endA.isBefore(endB) ? endA : endB;

        if (overlapStart.isAfter(overlapEnd)) {
            // No overlap
            return new TimeSeries[]{
                    new TimeSeries(a.getName() + "_aligned"),
                    new TimeSeries(b.getName() + "_aligned")
            };
        }

        // Generate regular grid
        List<Instant> grid = generateGrid(overlapStart, overlapEnd, interval);

        // Build navigable maps for efficient lookup
        NavigableMap<Instant, DataPoint> mapA = buildNavigableMap(a);
        NavigableMap<Instant, DataPoint> mapB = buildNavigableMap(b);

        // Resample both series to the grid
        TimeSeries alignedA = new TimeSeries(a.getName() + "_aligned");
        TimeSeries alignedB = new TimeSeries(b.getName() + "_aligned");

        for (Instant t : grid) {
            alignedA.add(t, fillValue(mapA, t, fill));
            alignedB.add(t, fillValue(mapB, t, fill));
        }

        return new TimeSeries[]{alignedA, alignedB};
    }

    /**
     * Resample a single time series to regular intervals
     * 将单个时间序列重采样为规则间隔
     *
     * <p>Generates a regular grid from the first to the last timestamp of the series
     * and fills values using the specified fill strategy.</p>
     * <p>从序列的第一个时间戳到最后一个时间戳生成规则网格，
     * 并使用指定的填充策略填充值。</p>
     *
     * @param ts the time series to resample | 要重采样的时间序列
     * @param interval the regular interval between grid points | 网格点之间的规则间隔
     * @param fill the fill strategy for missing values | 缺失值的填充策略
     * @return a new resampled TimeSeries | 新的重采样 TimeSeries
     * @throws TimeSeriesException if interval is non-positive | 如果间隔为非正数
     */
    public static TimeSeries resample(TimeSeries ts, Duration interval, FillStrategy fill) {
        validateInterval(interval);

        if (ts.isEmpty()) {
            return new TimeSeries(ts.getName() + "_resampled");
        }

        Instant start = ts.getFirst().orElseThrow().timestamp();
        Instant end = ts.getLast().orElseThrow().timestamp();

        List<Instant> grid = generateGrid(start, end, interval);
        NavigableMap<Instant, DataPoint> map = buildNavigableMap(ts);

        TimeSeries result = new TimeSeries(ts.getName() + "_resampled");
        for (Instant t : grid) {
            result.add(t, fillValue(map, t, fill));
        }
        return result;
    }

    /**
     * Snap timestamps to grid boundaries and aggregate within each bucket
     * 将时间戳对齐到网格边界并在每个桶内聚合
     *
     * <p>For each interval bucket [bucketStart, bucketStart + interval), collects
     * all data points that fall within and aggregates them using the specified
     * aggregation type.</p>
     * <p>对于每个间隔桶 [bucketStart, bucketStart + interval)，收集所有落入其中的
     * 数据点，并使用指定的聚合类型进行聚合。</p>
     *
     * @param ts the time series to align | 要对齐的时间序列
     * @param interval the grid interval (bucket width) | 网格间隔（桶宽度）
     * @param agg the aggregation type for each bucket | 每个桶的聚合类型
     * @return a new TimeSeries with aligned timestamps | 带有对齐时间戳的新 TimeSeries
     * @throws TimeSeriesException if interval is non-positive | 如果间隔为非正数
     */
    public static TimeSeries alignToGrid(TimeSeries ts, Duration interval, AggregationType agg) {
        validateInterval(interval);

        if (ts.isEmpty()) {
            return new TimeSeries(ts.getName() + "_grid");
        }

        Instant start = ts.getFirst().orElseThrow().timestamp();
        Instant end = ts.getLast().orElseThrow().timestamp();
        long intervalMillis = interval.toMillis();

        // Snap start to grid boundary
        long startMillis = start.toEpochMilli();
        long mod = startMillis % intervalMillis;
        long offset = (mod < 0) ? (mod + intervalMillis) : mod;
        long gridStartMillis = startMillis - offset;

        TimeSeries result = new TimeSeries(ts.getName() + "_grid");
        List<DataPoint> allPoints = ts.getPoints();

        int pointIndex = 0;
        long bucketStartMillis = gridStartMillis;
        long endMillis = end.toEpochMilli();

        while (bucketStartMillis <= endMillis) {
            // Overflow-safe bucket end calculation
            boolean lastBucket = bucketStartMillis > Long.MAX_VALUE - intervalMillis;
            long bucketEndMillis = lastBucket ? Long.MAX_VALUE : bucketStartMillis + intervalMillis;
            List<Double> bucketValues = new ArrayList<>();

            // Collect points in [bucketStart, bucketEnd)
            while (pointIndex < allPoints.size()) {
                long ptMillis = allPoints.get(pointIndex).epochMillis();
                if (ptMillis < bucketStartMillis) {
                    pointIndex++;
                    continue;
                }
                if (ptMillis >= bucketEndMillis) {
                    break;
                }
                bucketValues.add(allPoints.get(pointIndex).value());
                pointIndex++;
            }

            if (!bucketValues.isEmpty()) {
                double aggregated = aggregate(bucketValues, agg);
                result.add(Instant.ofEpochMilli(bucketStartMillis), aggregated);
            }

            if (lastBucket) {
                break; // Prevent infinite loop when bucket end overflows
            }
            bucketStartMillis = bucketEndMillis;
        }

        return result;
    }

    // ==================== Private helpers ====================

    /**
     * Validate that the interval is positive
     */
    private static void validateInterval(Duration interval) {
        if (interval == null || interval.isZero() || interval.isNegative()) {
            throw new TimeSeriesException(
                    TimeSeriesErrorCode.INVALID_INTERVAL,
                    "Interval must be positive, got: " + interval
            );
        }
    }

    /** Maximum number of grid points to prevent OOM | 最大网格点数以防止内存溢出 */
    private static final int MAX_GRID_POINTS = 10_000_000;

    /**
     * Generate a regular grid of timestamps from start to end (inclusive)
     */
    private static List<Instant> generateGrid(Instant start, Instant end, Duration interval) {
        long rangeMillis = Duration.between(start, end).toMillis();
        long intervalMillis = interval.toMillis();
        long estimatedPoints = (intervalMillis > 0) ? (rangeMillis / intervalMillis) + 1 : 1;
        if (estimatedPoints > MAX_GRID_POINTS) {
            throw new TimeSeriesException(
                    TimeSeriesErrorCode.CAPACITY_EXCEEDED,
                    "Grid would produce " + estimatedPoints + " points (max " + MAX_GRID_POINTS + ")"
            );
        }

        long startMillis = start.toEpochMilli();
        long endMillis = end.toEpochMilli();
        List<Instant> grid = new ArrayList<>((int) estimatedPoints);
        // Use primitive long arithmetic instead of Instant.plus(Duration) to avoid object allocation
        for (long ms = startMillis; ms <= endMillis; ms += intervalMillis) {
            grid.add(Instant.ofEpochMilli(ms));
        }
        return grid;
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
     * Fill a value at the given timestamp using the specified strategy
     */
    private static double fillValue(NavigableMap<Instant, DataPoint> map, Instant t, FillStrategy fill) {
        // Check for exact match
        DataPoint exact = map.get(t);
        if (exact != null) {
            return exact.value();
        }

        Map.Entry<Instant, DataPoint> floor = map.floorEntry(t);
        Map.Entry<Instant, DataPoint> ceiling = map.ceilingEntry(t);

        return switch (fill) {
            case ZERO -> 0.0;
            case NAN -> Double.NaN;
            case PREVIOUS -> floor != null ? floor.getValue().value() : (ceiling != null ? ceiling.getValue().value() : Double.NaN);
            case NEXT -> ceiling != null ? ceiling.getValue().value() : (floor != null ? floor.getValue().value() : Double.NaN);
            case LINEAR -> linearInterpolate(floor, ceiling, t);
            case AVERAGE -> averageNeighbors(floor, ceiling);
        };
    }

    /**
     * Linear interpolation between two surrounding points
     */
    private static double linearInterpolate(
            Map.Entry<Instant, DataPoint> floor,
            Map.Entry<Instant, DataPoint> ceiling,
            Instant t) {
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
     * Average of floor and ceiling neighbors
     */
    private static double averageNeighbors(
            Map.Entry<Instant, DataPoint> floor,
            Map.Entry<Instant, DataPoint> ceiling) {
        if (floor == null && ceiling == null) {
            return Double.NaN;
        }
        if (floor == null) {
            return ceiling.getValue().value();
        }
        if (ceiling == null) {
            return floor.getValue().value();
        }
        return (floor.getValue().value() + ceiling.getValue().value()) / 2.0;
    }

    /**
     * Aggregate a list of values using the given aggregation type
     */
    private static double aggregate(List<Double> values, AggregationType agg) {
        return switch (agg) {
            case SUM -> {
                double sum = 0;
                for (double v : values) {
                    sum += v;
                }
                yield sum;
            }
            case AVG -> {
                double sum = 0;
                for (double v : values) {
                    sum += v;
                }
                yield sum / values.size();
            }
            case MIN -> {
                double min = Double.MAX_VALUE;
                for (double v : values) {
                    if (v < min) {
                        min = v;
                    }
                }
                yield min;
            }
            case MAX -> {
                double max = -Double.MAX_VALUE;
                for (double v : values) {
                    if (v > max) {
                        max = v;
                    }
                }
                yield max;
            }
            case FIRST -> values.getFirst();
            case LAST -> values.getLast();
            case COUNT -> (double) values.size();
        };
    }
}
