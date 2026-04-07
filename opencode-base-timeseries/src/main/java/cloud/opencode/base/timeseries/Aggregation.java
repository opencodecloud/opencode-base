package cloud.opencode.base.timeseries;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Aggregation
 * 聚合
 *
 * <p>Time series aggregation utilities.</p>
 * <p>时间序列聚合工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Downsample with SUM, AVG, MIN, MAX, COUNT, FIRST, LAST - 使用多种聚合函数降采样</li>
 *   <li>Moving average and exponential moving average - 移动平均和指数移动平均</li>
 *   <li>Rolling statistics (min, max, avg, std) - 滚动统计</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TimeSeries ma = Aggregation.movingAverage(series, 7);
 * TimeSeries ema = Aggregation.exponentialMovingAverage(series, 0.3);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (null series will throw NullPointerException) - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public final class Aggregation {

    private Aggregation() {
        // Utility class
    }

    /**
     * Aggregation function
     * 聚合函数
     */
    public enum Function {
        SUM, AVG, MIN, MAX, COUNT, FIRST, LAST
    }

    /**
     * Downsample time series by aggregating points
     * 通过聚合点对时间序列进行降采样
     *
     * @param series the series | 序列
     * @param interval the interval | 间隔
     * @param function the aggregation function | 聚合函数
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries downsample(TimeSeries series, Duration interval, Function function) {
        if (series.isEmpty()) {
            return new TimeSeries(series.getName() + "_downsampled");
        }

        Map<Long, List<DataPoint>> buckets = new TreeMap<>();
        long intervalMillis = interval.toMillis();

        for (DataPoint point : series.getPoints()) {
            long bucket = (point.epochMillis() / intervalMillis) * intervalMillis;
            buckets.computeIfAbsent(bucket, k -> new ArrayList<>()).add(point);
        }

        List<DataPoint> result = new ArrayList<>();
        for (Map.Entry<Long, List<DataPoint>> entry : buckets.entrySet()) {
            Instant timestamp = Instant.ofEpochMilli(entry.getKey());
            double value = aggregate(entry.getValue(), function);
            result.add(DataPoint.of(timestamp, value));
        }

        return new TimeSeries(series.getName() + "_downsampled", result);
    }

    /**
     * Calculate moving average
     * 计算移动平均
     *
     * @param series the series | 序列
     * @param window the window size | 窗口大小
     * @return the moving average series | 移动平均序列
     */
    public static TimeSeries movingAverage(TimeSeries series, int window) {
        List<DataPoint> points = series.getPoints();
        if (points.size() < window) {
            return new TimeSeries(series.getName() + "_ma");
        }

        List<DataPoint> result = new ArrayList<>();
        double sum = 0;

        for (int i = 0; i < points.size(); i++) {
            sum += points.get(i).value();
            if (i >= window) {
                sum -= points.get(i - window).value();
            }
            if (i >= window - 1) {
                result.add(DataPoint.of(points.get(i).timestamp(), sum / window));
            }
        }

        return new TimeSeries(series.getName() + "_ma", result);
    }

    /**
     * Calculate exponential moving average
     * 计算指数移动平均
     *
     * @param series the series | 序列
     * @param alpha the smoothing factor (0-1) | 平滑因子
     * @return the EMA series | EMA序列
     */
    public static TimeSeries exponentialMovingAverage(TimeSeries series, double alpha) {
        List<DataPoint> points = series.getPoints();
        if (points.isEmpty()) {
            return new TimeSeries(series.getName() + "_ema");
        }

        List<DataPoint> result = new ArrayList<>();
        double ema = points.get(0).value();

        for (DataPoint point : points) {
            ema = alpha * point.value() + (1 - alpha) * ema;
            result.add(DataPoint.of(point.timestamp(), ema));
        }

        return new TimeSeries(series.getName() + "_ema", result);
    }

    /**
     * Calculate rolling statistics
     * 计算滚动统计
     *
     * @param series the series | 序列
     * @param window the window size | 窗口大小
     * @return map of statistic name to series | 统计名称到序列的映射
     */
    public static Map<String, TimeSeries> rollingStats(TimeSeries series, int window) {
        List<DataPoint> points = series.getPoints();

        List<DataPoint> minPoints = new ArrayList<>();
        List<DataPoint> maxPoints = new ArrayList<>();
        List<DataPoint> avgPoints = new ArrayList<>();
        List<DataPoint> stdPoints = new ArrayList<>();

        for (int i = window - 1; i < points.size(); i++) {
            double sum = 0, min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
            for (int j = i - window + 1; j <= i; j++) {
                double v = points.get(j).value();
                sum += v;
                min = Math.min(min, v);
                max = Math.max(max, v);
            }
            double avg = sum / window;

            double variance = 0;
            for (int j = i - window + 1; j <= i; j++) {
                double diff = points.get(j).value() - avg;
                variance += diff * diff;
            }
            double std = Math.sqrt(variance / window);

            Instant timestamp = points.get(i).timestamp();
            minPoints.add(DataPoint.of(timestamp, min));
            maxPoints.add(DataPoint.of(timestamp, max));
            avgPoints.add(DataPoint.of(timestamp, avg));
            stdPoints.add(DataPoint.of(timestamp, std));
        }

        return Map.of(
            "min", new TimeSeries(series.getName() + "_min", minPoints),
            "max", new TimeSeries(series.getName() + "_max", maxPoints),
            "avg", new TimeSeries(series.getName() + "_avg", avgPoints),
            "std", new TimeSeries(series.getName() + "_std", stdPoints)
        );
    }

    private static double aggregate(List<DataPoint> points, Function function) {
        return switch (function) {
            case SUM -> points.stream().mapToDouble(DataPoint::value).sum();
            case AVG -> points.stream().mapToDouble(DataPoint::value).average().orElse(0);
            case MIN -> points.stream().mapToDouble(DataPoint::value).min().orElse(0);
            case MAX -> points.stream().mapToDouble(DataPoint::value).max().orElse(0);
            case COUNT -> points.size();
            case FIRST -> points.get(0).value();
            case LAST -> points.get(points.size() - 1).value();
        };
    }
}
