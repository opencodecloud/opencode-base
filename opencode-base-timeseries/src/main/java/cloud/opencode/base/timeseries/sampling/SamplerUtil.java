package cloud.opencode.base.timeseries.sampling;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Sampler Util
 * 采样工具类
 *
 * <p>Utilities for downsampling and gap filling.</p>
 * <p>降采样和缺口填充的工具类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Downsampling with configurable aggregation - 可配置聚合的降采样</li>
 *   <li>Gap filling with multiple strategies (zero, previous, linear, NaN) - 多策略缺口填充</li>
 *   <li>Resampling, random sampling, and systematic sampling - 重采样、随机采样和系统采样</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TimeSeries ds = SamplerUtil.downsample(series, Duration.ofMinutes(5), AggregationType.AVG);
 * TimeSeries filled = SamplerUtil.fillGaps(series, Duration.ofSeconds(10), FillStrategy.LINEAR);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (null series may throw NullPointerException) - 空值安全: 否</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for downsample, fillGaps, systematicSample; O(n log n) for randomSample due to shuffle and sort - 时间复杂度: downsample、fillGaps、systematicSample 均为 O(n)；randomSample 因洗牌和排序为 O(n log n)</li>
 *   <li>Space complexity: O(n) - bucket maps and result series proportional to input series length - 空间复杂度: O(n) - 桶映射和结果序列与输入序列长度成正比</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public final class SamplerUtil {

    private SamplerUtil() {
        // Utility class
    }

    /**
     * Downsample time series
     * 降采样时间序列
     *
     * @param series the series | 序列
     * @param interval the interval | 间隔
     * @param aggregation the aggregation type | 聚合类型
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries downsample(TimeSeries series, Duration interval,
                                         AggregationType aggregation) {
        TimeSeries result = new TimeSeries(series.getName() + "_downsampled");
        List<DataPoint> points = series.getPoints();
        if (points.isEmpty()) {
            return result;
        }

        Map<Long, List<Double>> buckets = new TreeMap<>();
        long intervalMillis = interval.toMillis();

        for (DataPoint point : points) {
            long bucket = (point.epochMillis() / intervalMillis) * intervalMillis;
            buckets.computeIfAbsent(bucket, k -> new ArrayList<>()).add(point.value());
        }

        for (Map.Entry<Long, List<Double>> entry : buckets.entrySet()) {
            Instant timestamp = Instant.ofEpochMilli(entry.getKey());
            double value = aggregate(entry.getValue(), aggregation);
            result.add(DataPoint.of(timestamp, value));
        }

        return result;
    }

    /**
     * Downsample with default AVG aggregation
     * 使用默认AVG聚合进行降采样
     *
     * @param series the series | 序列
     * @param interval the interval | 间隔
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries downsample(TimeSeries series, Duration interval) {
        return downsample(series, interval, AggregationType.AVG);
    }

    /**
     * Fill gaps in time series
     * 填充时间序列中的缺口
     *
     * @param series the series | 序列
     * @param interval the expected interval | 期望的间隔
     * @param strategy the fill strategy | 填充策略
     * @return the filled series | 填充后的序列
     */
    public static TimeSeries fillGaps(TimeSeries series, Duration interval, FillStrategy strategy) {
        TimeSeries result = new TimeSeries(series.getName() + "_filled");
        List<DataPoint> points = series.getPoints();
        if (points.isEmpty()) {
            return result;
        }

        DataPoint prev = points.getFirst();
        result.add(prev);

        for (int i = 1; i < points.size(); i++) {
            DataPoint current = points.get(i);
            Duration gap = Duration.between(prev.timestamp(), current.timestamp());

            // Fill missing points if gap is larger than 2x interval
            if (gap.compareTo(interval.multipliedBy(2)) > 0) {
                Instant fillTime = prev.timestamp().plus(interval);
                while (fillTime.isBefore(current.timestamp())) {
                    double fillValue = calculateFillValue(prev, current, fillTime, strategy);
                    result.add(DataPoint.of(fillTime, fillValue));
                    fillTime = fillTime.plus(interval);
                }
            }

            result.add(current);
            prev = current;
        }

        return result;
    }

    /**
     * Upsample time series (increase frequency)
     * 上采样时间序列（增加频率）
     *
     * @param series the series | 序列
     * @param interval the target interval | 目标间隔
     * @param strategy the interpolation strategy | 插值策略
     * @return the upsampled series | 上采样后的序列
     */
    public static TimeSeries upsample(TimeSeries series, Duration interval, FillStrategy strategy) {
        return fillGaps(series, interval, strategy);
    }

    /**
     * Resample time series to regular intervals
     * 重采样时间序列为规则间隔
     *
     * @param series the series | 序列
     * @param interval the target interval | 目标间隔
     * @param aggregation the aggregation for downsampling | 降采样的聚合类型
     * @param fillStrategy the strategy for upsampling | 上采样的策略
     * @return the resampled series | 重采样后的序列
     */
    public static TimeSeries resample(TimeSeries series, Duration interval,
                                       AggregationType aggregation, FillStrategy fillStrategy) {
        // First downsample if needed
        TimeSeries downsampled = downsample(series, interval, aggregation);
        // Then fill any gaps
        return fillGaps(downsampled, interval, fillStrategy);
    }

    /**
     * Sample random points from series
     * 从序列中随机采样点
     *
     * @param series the series | 序列
     * @param sampleSize the sample size | 样本大小
     * @return the sampled series | 采样后的序列
     */
    public static TimeSeries randomSample(TimeSeries series, int sampleSize) {
        List<DataPoint> points = new ArrayList<>(series.getPoints());
        if (points.size() <= sampleSize) {
            return new TimeSeries(series.getName() + "_sampled", points);
        }

        Collections.shuffle(points);
        List<DataPoint> sampled = points.subList(0, sampleSize);
        sampled.sort(DataPoint::compareTo);

        return new TimeSeries(series.getName() + "_sampled", sampled);
    }

    /**
     * Systematic sampling (every nth point)
     * 系统采样（每第n个点）
     *
     * @param series the series | 序列
     * @param n the sampling interval | 采样间隔
     * @return the sampled series | 采样后的序列
     */
    public static TimeSeries systematicSample(TimeSeries series, int n) {
        List<DataPoint> points = series.getPoints();
        List<DataPoint> sampled = new ArrayList<>();

        for (int i = 0; i < points.size(); i += n) {
            sampled.add(points.get(i));
        }

        return new TimeSeries(series.getName() + "_sampled", sampled);
    }

    private static double aggregate(List<Double> values, AggregationType type) {
        return switch (type) {
            case SUM -> values.stream().mapToDouble(d -> d).sum();
            case AVG -> values.stream().mapToDouble(d -> d).average().orElse(0);
            case MIN -> values.stream().mapToDouble(d -> d).min().orElse(0);
            case MAX -> values.stream().mapToDouble(d -> d).max().orElse(0);
            case FIRST -> values.getFirst();
            case LAST -> values.getLast();
            case COUNT -> values.size();
        };
    }

    private static double calculateFillValue(DataPoint prev, DataPoint next,
                                              Instant fillTime, FillStrategy strategy) {
        return switch (strategy) {
            case ZERO -> 0;
            case PREVIOUS -> prev.value();
            case NEXT -> next.value();
            case LINEAR -> linearInterpolate(prev, next, fillTime);
            case AVERAGE -> (prev.value() + next.value()) / 2;
            case NAN -> Double.NaN;
        };
    }

    private static double linearInterpolate(DataPoint p1, DataPoint p2, Instant t) {
        long t1 = p1.timestamp().toEpochMilli();
        long t2 = p2.timestamp().toEpochMilli();
        long t0 = t.toEpochMilli();
        double ratio = (double) (t0 - t1) / (t2 - t1);
        return p1.value() + ratio * (p2.value() - p1.value());
    }
}
