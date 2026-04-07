package cloud.opencode.base.timeseries.bucket;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import cloud.opencode.base.timeseries.exception.TimeSeriesErrorCode;
import cloud.opencode.base.timeseries.exception.TimeSeriesException;
import cloud.opencode.base.timeseries.sampling.AggregationType;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Time Bucket Utility
 * 时间分桶工具类
 *
 * <p>Provides calendar-aware and fixed-duration bucketing with aggregation for time series data.</p>
 * <p>提供日历感知和固定时长分桶聚合功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Calendar-aware bucketing: SECOND to YEAR with timezone support - 日历感知分桶：秒到年，支持时区</li>
 *   <li>Fixed-duration bucketing with custom origin - 固定时长分桶，支持自定义起点</li>
 *   <li>All standard aggregation types: SUM, AVG, MIN, MAX, FIRST, LAST, COUNT - 全部标准聚合类型</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Calendar-aware daily bucketing
 * TimeSeries daily = TimeBucketUtil.bucket(series, TimeBucket.DAY, ZoneId.of("UTC"), AggregationType.AVG);
 *
 * // Fixed 5-minute intervals
 * TimeSeries fiveMin = TimeBucketUtil.bucket(series, Duration.ofMinutes(5), Instant.EPOCH, AggregationType.SUM);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class, all methods are pure functions) - 线程安全: 是（无状态工具类，所有方法均为纯函数）</li>
 *   <li>Null-safe: No (null arguments will throw NullPointerException) - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.3
 */
public final class TimeBucketUtil {

    private TimeBucketUtil() {
        // Utility class, no instantiation
    }

    /**
     * Calendar-aware time bucketing
     * 日历感知时间分桶
     *
     * <p>Groups data points into calendar-aligned buckets and aggregates each bucket.</p>
     * <p>将数据点分组到日历对齐的桶中，并对每个桶进行聚合。</p>
     *
     * @param ts the input time series | 输入时间序列
     * @param bucket the bucket granularity | 桶粒度
     * @param zone the time zone for calendar alignment | 用于日历对齐的时区
     * @param agg the aggregation type | 聚合类型
     * @return bucketed and aggregated time series | 分桶聚合后的时间序列
     * @throws TimeSeriesException if the series is empty | 若序列为空则抛出异常
     */
    public static TimeSeries bucket(TimeSeries ts, TimeBucket bucket, ZoneId zone, AggregationType agg) {
        Objects.requireNonNull(ts, "ts must not be null");
        Objects.requireNonNull(bucket, "bucket must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
        Objects.requireNonNull(agg, "agg must not be null");

        if (ts.isEmpty()) {
            throw new TimeSeriesException(TimeSeriesErrorCode.EMPTY_SERIES);
        }

        Map<Instant, List<DataPoint>> groups = new TreeMap<>();
        for (DataPoint point : ts.getPoints()) {
            Instant bucketKey = truncate(point.timestamp(), bucket, zone);
            groups.computeIfAbsent(bucketKey, _ -> new ArrayList<>()).add(point);
        }

        return aggregateGroups(ts.getName() + "_bucketed", groups, agg);
    }

    /**
     * Fixed-duration bucketing with custom origin
     * 固定时长分桶，支持自定义起点
     *
     * <p>Groups data points into fixed-size duration buckets starting from the given origin.</p>
     * <p>将数据点分组到从给定起点开始的固定大小时长桶中。</p>
     *
     * @param ts the input time series | 输入时间序列
     * @param interval the bucket interval duration | 桶间隔时长
     * @param origin the origin instant for bucket alignment | 桶对齐的起始时刻
     * @param agg the aggregation type | 聚合类型
     * @return bucketed and aggregated time series | 分桶聚合后的时间序列
     * @throws TimeSeriesException if the series is empty or interval is invalid | 若序列为空或间隔无效则抛出异常
     */
    public static TimeSeries bucket(TimeSeries ts, Duration interval, Instant origin, AggregationType agg) {
        Objects.requireNonNull(ts, "ts must not be null");
        Objects.requireNonNull(interval, "interval must not be null");
        Objects.requireNonNull(origin, "origin must not be null");
        Objects.requireNonNull(agg, "agg must not be null");

        if (ts.isEmpty()) {
            throw new TimeSeriesException(TimeSeriesErrorCode.EMPTY_SERIES);
        }
        if (interval.isZero() || interval.isNegative()) {
            throw new TimeSeriesException(TimeSeriesErrorCode.INVALID_INTERVAL,
                    "interval must be positive, got: " + interval);
        }

        long intervalMillis = interval.toMillis();
        long originMillis = origin.toEpochMilli();
        Map<Instant, List<DataPoint>> groups = new TreeMap<>();

        for (DataPoint point : ts.getPoints()) {
            // Use primitive subtraction to avoid Duration.between() object allocation
            long offsetMillis = point.epochMillis() - originMillis;
            long bucketIndex = Math.floorDiv(offsetMillis, intervalMillis);
            long bucketMillis;
            try {
                bucketMillis = Math.multiplyExact(bucketIndex, intervalMillis);
            } catch (ArithmeticException e) {
                throw new TimeSeriesException(TimeSeriesErrorCode.CAPACITY_EXCEEDED,
                        "Bucket index overflow: origin too far from data point");
            }
            Instant bucketKey = origin.plusMillis(bucketMillis);
            groups.computeIfAbsent(bucketKey, _ -> new ArrayList<>()).add(point);
        }

        return aggregateGroups(ts.getName() + "_bucketed", groups, agg);
    }

    /**
     * Truncate timestamp to bucket boundary
     * 将时间戳截断到桶边界
     */
    private static Instant truncate(Instant timestamp, TimeBucket bucket, ZoneId zone) {
        ZonedDateTime zdt = timestamp.atZone(zone);
        return switch (bucket) {
            case SECOND -> zdt.truncatedTo(ChronoUnit.SECONDS).toInstant();
            case MINUTE -> zdt.truncatedTo(ChronoUnit.MINUTES).toInstant();
            case HOUR -> zdt.truncatedTo(ChronoUnit.HOURS).toInstant();
            case DAY -> zdt.toLocalDate().atStartOfDay(zone).toInstant();
            case WEEK -> zdt.toLocalDate()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .atStartOfDay(zone).toInstant();
            case MONTH -> zdt.toLocalDate()
                    .withDayOfMonth(1)
                    .atStartOfDay(zone).toInstant();
            case QUARTER -> {
                int month = zdt.getMonthValue();
                int quarterStart = ((month - 1) / 3) * 3 + 1;
                yield zdt.toLocalDate()
                        .withMonth(quarterStart)
                        .withDayOfMonth(1)
                        .atStartOfDay(zone).toInstant();
            }
            case YEAR -> zdt.toLocalDate()
                    .withDayOfYear(1)
                    .atStartOfDay(zone).toInstant();
        };
    }

    /**
     * Aggregate grouped data points into a time series
     * 将分组的数据点聚合为时间序列
     */
    private static TimeSeries aggregateGroups(String name, Map<Instant, List<DataPoint>> groups, AggregationType agg) {
        List<DataPoint> result = new ArrayList<>(groups.size());
        for (Map.Entry<Instant, List<DataPoint>> entry : groups.entrySet()) {
            double aggregated = aggregate(entry.getValue(), agg);
            result.add(DataPoint.of(entry.getKey(), aggregated));
        }
        return new TimeSeries(name, result);
    }

    /**
     * Aggregate a list of data points
     * 聚合数据点列表
     */
    /**
     * Aggregate using loops instead of streams for better performance on small buckets
     */
    private static double aggregate(List<DataPoint> points, AggregationType agg) {
        return switch (agg) {
            case SUM -> {
                double sum = 0;
                for (DataPoint p : points) sum += p.value();
                yield sum;
            }
            case AVG -> {
                double sum = 0;
                for (DataPoint p : points) sum += p.value();
                yield sum / points.size();
            }
            case MIN -> {
                double min = Double.MAX_VALUE;
                for (DataPoint p : points) { if (p.value() < min) min = p.value(); }
                yield min;
            }
            case MAX -> {
                double max = -Double.MAX_VALUE;
                for (DataPoint p : points) { if (p.value() > max) max = p.value(); }
                yield max;
            }
            case FIRST -> {
                DataPoint first = points.getFirst();
                for (DataPoint p : points) { if (p.timestamp().isBefore(first.timestamp())) first = p; }
                yield first.value();
            }
            case LAST -> {
                DataPoint last = points.getFirst();
                for (DataPoint p : points) { if (p.timestamp().isAfter(last.timestamp())) last = p; }
                yield last.value();
            }
            case COUNT -> (double) points.size();
        };
    }
}
