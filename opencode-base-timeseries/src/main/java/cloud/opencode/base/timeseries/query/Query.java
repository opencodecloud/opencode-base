package cloud.opencode.base.timeseries.query;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import cloud.opencode.base.timeseries.sampling.AggregationType;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

/**
 * Query
 * 查询构建器
 *
 * <p>Fluent query builder for time series data.</p>
 * <p>时间序列数据的流式查询构建器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent API for time range, filter, limit, aggregation, groupBy - 流式 API</li>
 *   <li>Value range and tag-based filtering - 值范围和标签过滤</li>
 *   <li>Scalar and list result execution - 标量和列表结果执行</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<DataPoint> result = Query.from(series)
 *     .last(Duration.ofHours(1))
 *     .limit(100)
 *     .execute();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable builder, not shared across threads) - 线程安全: 否</li>
 *   <li>Null-safe: No (null series throws NullPointerException) - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public final class Query {

    private final TimeSeries series;
    private TimeRange timeRange;
    private int limit = Integer.MAX_VALUE;
    private Predicate<DataPoint> filter;
    private AggregationType aggregation;
    private Duration groupBy;

    private Query(TimeSeries series) {
        this.series = Objects.requireNonNull(series, "series cannot be null");
    }

    /**
     * Create query for series
     * 为序列创建查询
     *
     * @param series the time series | 时间序列
     * @return the query builder | 查询构建器
     */
    public static Query from(TimeSeries series) {
        return new Query(series);
    }

    /**
     * Set time range
     * 设置时间范围
     *
     * @param from the start time | 开始时间
     * @param to the end time | 结束时间
     * @return this builder | 此构建器
     */
    public Query range(Instant from, Instant to) {
        this.timeRange = TimeRange.of(from, to);
        return this;
    }

    /**
     * Set time range
     * 设置时间范围
     *
     * @param range the time range | 时间范围
     * @return this builder | 此构建器
     */
    public Query range(TimeRange range) {
        this.timeRange = range;
        return this;
    }

    /**
     * Set last duration
     * 设置最近的时长
     *
     * @param duration the duration | 时长
     * @return this builder | 此构建器
     */
    public Query last(Duration duration) {
        this.timeRange = TimeRange.last(duration);
        return this;
    }

    /**
     * Set result limit
     * 设置结果限制
     *
     * @param limit the max results | 最大结果数
     * @return this builder | 此构建器
     */
    public Query limit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Set filter predicate
     * 设置过滤条件
     *
     * @param filter the filter | 过滤条件
     * @return this builder | 此构建器
     */
    public Query filter(Predicate<DataPoint> filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Filter by value range
     * 按值范围过滤
     *
     * @param min the minimum value | 最小值
     * @param max the maximum value | 最大值
     * @return this builder | 此构建器
     */
    public Query valueRange(double min, double max) {
        this.filter = p -> p.value() >= min && p.value() <= max;
        return this;
    }

    /**
     * Filter by tag
     * 按标签过滤
     *
     * @param key the tag key | 标签键
     * @param value the tag value | 标签值
     * @return this builder | 此构建器
     */
    public Query tag(String key, String value) {
        this.filter = p -> value.equals(p.getTag(key));
        return this;
    }

    /**
     * Set aggregation type
     * 设置聚合类型
     *
     * @param aggregation the aggregation type | 聚合类型
     * @return this builder | 此构建器
     */
    public Query aggregate(AggregationType aggregation) {
        this.aggregation = aggregation;
        return this;
    }

    /**
     * Set group by interval
     * 设置分组间隔
     *
     * @param interval the interval | 间隔
     * @return this builder | 此构建器
     */
    public Query groupBy(Duration interval) {
        this.groupBy = interval;
        return this;
    }

    /**
     * Execute query and return data points
     * 执行查询并返回数据点
     *
     * @return the data points | 数据点
     */
    public List<DataPoint> execute() {
        List<DataPoint> points = series.getPoints();

        // Apply time range filter
        if (timeRange != null) {
            QueryLimiter.validateRange(timeRange);
            points = points.stream()
                .filter(p -> timeRange.contains(p.timestamp()))
                .toList();
        }

        // Apply custom filter
        if (filter != null) {
            points = points.stream()
                .filter(filter)
                .toList();
        }

        // Apply grouping and aggregation
        if (groupBy != null && aggregation != null) {
            points = groupAndAggregate(points);
        }

        // Apply limit
        if (points.size() > limit) {
            points = points.subList(0, limit);
        }

        return QueryLimiter.limitResult(points);
    }

    /**
     * Execute query and return single value
     * 执行查询并返回单个值
     *
     * @return the aggregated value | 聚合值
     */
    public OptionalDouble executeScalar() {
        List<DataPoint> points = execute();
        if (points.isEmpty()) {
            return OptionalDouble.empty();
        }

        if (aggregation == null) {
            return OptionalDouble.of(points.getLast().value());
        }

        return OptionalDouble.of(aggregate(points, aggregation));
    }

    private List<DataPoint> groupAndAggregate(List<DataPoint> points) {
        if (points.isEmpty()) {
            return points;
        }

        Map<Long, List<DataPoint>> buckets = new TreeMap<>();
        long intervalMillis = groupBy.toMillis();

        for (DataPoint point : points) {
            long bucket = (point.epochMillis() / intervalMillis) * intervalMillis;
            buckets.computeIfAbsent(bucket, k -> new ArrayList<>()).add(point);
        }

        List<DataPoint> result = new ArrayList<>();
        for (Map.Entry<Long, List<DataPoint>> entry : buckets.entrySet()) {
            Instant timestamp = Instant.ofEpochMilli(entry.getKey());
            double value = aggregate(entry.getValue(), aggregation);
            result.add(DataPoint.of(timestamp, value));
        }

        return result;
    }

    private double aggregate(List<DataPoint> points, AggregationType type) {
        return switch (type) {
            case SUM -> points.stream().mapToDouble(DataPoint::value).sum();
            case AVG -> points.stream().mapToDouble(DataPoint::value).average().orElse(0);
            case MIN -> points.stream().mapToDouble(DataPoint::value).min().orElse(0);
            case MAX -> points.stream().mapToDouble(DataPoint::value).max().orElse(0);
            case COUNT -> points.size();
            case FIRST -> points.getFirst().value();
            case LAST -> points.getLast().value();
        };
    }
}
