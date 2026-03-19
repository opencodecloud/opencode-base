package cloud.opencode.base.timeseries;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

/**
 * Time Series
 * 时间序列
 *
 * <p>A sequence of data points indexed by time.</p>
 * <p>按时间索引的数据点序列。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ConcurrentSkipListMap-backed ordered data points - 基于 ConcurrentSkipListMap 的有序数据点</li>
 *   <li>Range queries, aggregation (sum, avg, min, max, variance, stdDev) - 范围查询、聚合</li>
 *   <li>Transform operations (map, diff, cumSum, derivative) - 变换操作</li>
 *   <li>Moving average, EMA, percentile calculation - 移动平均、EMA、百分位计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TimeSeries ts = new TimeSeries("cpu");
 * ts.addNow(75.5);
 * double avg = ts.average().orElse(0);
 * TimeSeries last5m = ts.last(Duration.ofMinutes(5));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ConcurrentSkipListMap and ConcurrentHashMap) - 线程安全: 是</li>
 *   <li>Null-safe: No (null timestamps are rejected by DataPoint) - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public class TimeSeries {

    private final String name;
    private final NavigableMap<Instant, DataPoint> points;
    private final Map<String, String> metadata;

    /**
     * Create time series
     * 创建时间序列
     *
     * @param name the series name | 序列名称
     */
    public TimeSeries(String name) {
        this.name = name;
        this.points = new ConcurrentSkipListMap<>();
        this.metadata = new ConcurrentHashMap<>();
    }

    /**
     * Create time series with initial points
     * 使用初始点创建时间序列
     *
     * @param name the series name | 序列名称
     * @param points the initial points | 初始点
     */
    public TimeSeries(String name, Collection<DataPoint> points) {
        this.name = name;
        this.points = new ConcurrentSkipListMap<>();
        this.metadata = new ConcurrentHashMap<>();
        points.forEach(p -> this.points.put(p.timestamp(), p));
    }

    // === Add methods ===

    public TimeSeries add(DataPoint point) {
        points.put(point.timestamp(), point);
        return this;
    }

    public TimeSeries add(Instant timestamp, double value) {
        return add(DataPoint.of(timestamp, value));
    }

    public TimeSeries addNow(double value) {
        return add(DataPoint.now(value));
    }

    public TimeSeries addAll(Collection<DataPoint> points) {
        points.forEach(this::add);
        return this;
    }

    // === Remove methods ===

    public void remove(Instant timestamp) {
        points.remove(timestamp);
    }

    // === Query methods ===

    public String getName() { return name; }
    public int size() { return points.size(); }
    public boolean isEmpty() { return points.isEmpty(); }

    /**
     * Get count as double
     * 获取计数（双精度）
     *
     * @return the count | 计数
     */
    public double count() {
        return points.size();
    }

    /**
     * Get count in time range
     * 获取时间范围内的计数
     *
     * @param from start time (inclusive) | 开始时间（包含）
     * @param to   end time (inclusive) | 结束时间（包含）
     * @return the count | 计数
     */
    public double count(Instant from, Instant to) {
        return range(from, to).size();
    }

    public Optional<DataPoint> get(Instant timestamp) {
        return Optional.ofNullable(points.get(timestamp));
    }

    public Optional<DataPoint> getFirst() {
        return points.isEmpty() ? Optional.empty() : Optional.of(points.firstEntry().getValue());
    }

    public Optional<DataPoint> getLast() {
        return points.isEmpty() ? Optional.empty() : Optional.of(points.lastEntry().getValue());
    }

    /**
     * Get latest data point (alias for getLast)
     * 获取最新的数据点（getLast的别名）
     *
     * @return the latest data point | 最新数据点
     */
    public DataPoint getLatest() {
        return getLast().orElse(null);
    }

    public List<DataPoint> getPoints() {
        return List.copyOf(points.values());
    }

    public double[] getValues() {
        return points.values().stream().mapToDouble(DataPoint::value).toArray();
    }

    public Instant[] getTimestamps() {
        return points.keySet().toArray(Instant[]::new);
    }

    // === Range queries ===

    public TimeSeries range(Instant from, Instant to) {
        NavigableMap<Instant, DataPoint> subMap = points.subMap(from, true, to, true);
        return new TimeSeries(name + "_range", subMap.values());
    }

    public TimeSeries last(Duration duration) {
        if (points.isEmpty()) return new TimeSeries(name + "_last");
        Instant from = points.lastKey().minus(duration);
        return range(from, points.lastKey());
    }

    public TimeSeries head(int n) {
        List<DataPoint> headPoints = points.values().stream().limit(n).toList();
        return new TimeSeries(name + "_head", headPoints);
    }

    public TimeSeries tail(int n) {
        if (points.isEmpty() || n <= 0) return new TimeSeries(name + "_tail");
        List<DataPoint> tailPoints = points.values().stream()
            .skip(Math.max(0, points.size() - n))
            .toList();
        return new TimeSeries(name + "_tail", tailPoints);
    }

    // === Aggregation methods ===

    public double sum() {
        return points.values().stream().mapToDouble(DataPoint::value).sum();
    }

    /**
     * Get sum in time range
     * 获取时间范围内的和
     *
     * @param from start time (inclusive) | 开始时间（包含）
     * @param to   end time (inclusive) | 结束时间（包含）
     * @return the sum | 和
     */
    public double sum(Instant from, Instant to) {
        return range(from, to).sum();
    }

    public OptionalDouble average() {
        return points.values().stream().mapToDouble(DataPoint::value).average();
    }

    /**
     * Get average in time range
     * 获取时间范围内的平均值
     *
     * @param from start time (inclusive) | 开始时间（包含）
     * @param to   end time (inclusive) | 结束时间（包含）
     * @return the average | 平均值
     */
    public double average(Instant from, Instant to) {
        return range(from, to).average().orElse(0);
    }

    public OptionalDouble min() {
        return points.values().stream().mapToDouble(DataPoint::value).min();
    }

    public OptionalDouble max() {
        return points.values().stream().mapToDouble(DataPoint::value).max();
    }

    public double variance() {
        double[] values = getValues();
        if (values.length < 2) return 0;
        double mean = average().orElse(0);
        double sumSq = 0;
        for (double v : values) {
            sumSq += (v - mean) * (v - mean);
        }
        return sumSq / (values.length - 1);
    }

    public double stdDev() {
        return Math.sqrt(variance());
    }

    /**
     * Get standard deviation (alias for stdDev)
     * 获取标准差（stdDev的别名）
     *
     * @return the standard deviation | 标准差
     */
    public double standardDeviation() {
        return stdDev();
    }

    // === Transform methods ===

    public TimeSeries map(DoubleUnaryOperator operator) {
        List<DataPoint> mapped = points.values().stream()
            .map(p -> DataPoint.of(p.timestamp(), operator.applyAsDouble(p.value()), p.tags()))
            .toList();
        return new TimeSeries(name + "_mapped", mapped);
    }

    public TimeSeries diff() {
        if (size() < 2) return new TimeSeries(name + "_diff");
        List<DataPoint> diffs = new ArrayList<>();
        DataPoint prev = null;
        for (DataPoint p : points.values()) {
            if (prev != null) {
                diffs.add(DataPoint.of(p.timestamp(), p.value() - prev.value()));
            }
            prev = p;
        }
        return new TimeSeries(name + "_diff", diffs);
    }

    public TimeSeries cumSum() {
        List<DataPoint> cumulative = new ArrayList<>();
        double sum = 0;
        for (DataPoint p : points.values()) {
            sum += p.value();
            cumulative.add(DataPoint.of(p.timestamp(), sum));
        }
        return new TimeSeries(name + "_cumsum", cumulative);
    }

    // === Combine methods ===

    public TimeSeries combine(TimeSeries other, DoubleBinaryOperator operator) {
        TimeSeries result = new TimeSeries(name + "_combined");
        Set<Instant> allTimes = new TreeSet<>();
        allTimes.addAll(this.points.keySet());
        allTimes.addAll(other.points.keySet());

        for (Instant t : allTimes) {
            DataPoint p1 = this.points.get(t);
            DataPoint p2 = other.points.get(t);
            if (p1 != null && p2 != null) {
                result.add(DataPoint.of(t, operator.applyAsDouble(p1.value(), p2.value())));
            }
        }
        return result;
    }

    // === Statistics methods ===

    /**
     * Get statistics
     * 获取统计信息
     *
     * @return the statistics | 统计信息
     */
    public TimeSeriesStats stats() {
        DoubleSummaryStatistics stats = points.values().stream()
            .mapToDouble(DataPoint::value)
            .summaryStatistics();

        if (stats.getCount() == 0) {
            return TimeSeriesStats.empty();
        }

        return new TimeSeriesStats(
            stats.getCount(),
            stats.getSum(),
            stats.getAverage(),
            stats.getMin(),
            stats.getMax(),
            stdDev()
        );
    }

    /**
     * Calculate percentile
     * 计算百分位数
     *
     * @param p the percentile (0-100) | 百分位（0-100）
     * @return the percentile value | 百分位值
     */
    public double percentile(int p) {
        if (p < 0 || p > 100) {
            throw new IllegalArgumentException("Percentile must be between 0 and 100, got: " + p);
        }
        double[] values = points.values().stream()
            .mapToDouble(DataPoint::value)
            .toArray();
        if (values.length == 0) {
            return 0;
        }
        Arrays.sort(values);
        int index = (int) Math.ceil(p / 100.0 * values.length) - 1;
        return values[Math.max(0, index)];
    }

    // === Transform methods (continued) ===

    /**
     * Calculate derivative (rate of change)
     * 计算导数（变化率）
     *
     * @return the derivative series | 导数序列
     */
    public TimeSeries derivative() {
        TimeSeries result = new TimeSeries(name + "_derivative");
        DataPoint prev = null;
        for (DataPoint point : points.values()) {
            if (prev != null) {
                long deltaTime = Duration.between(prev.timestamp(), point.timestamp()).toMillis();
                if (deltaTime > 0) {
                    double derivative = (point.value() - prev.value()) / deltaTime * 1000;
                    result.add(point.timestamp(), derivative);
                }
            }
            prev = point;
        }
        return result;
    }

    /**
     * Calculate moving average
     * 计算移动平均
     *
     * @param windowSize the window size | 窗口大小
     * @return the moving average series | 移动平均序列
     */
    public TimeSeries movingAverage(int windowSize) {
        if (windowSize <= 0) {
            throw new IllegalArgumentException("Window size must be > 0, got: " + windowSize);
        }
        if (windowSize > points.size()) {
            throw new IllegalArgumentException(
                "Window size (" + windowSize + ") must be <= data size (" + points.size() + ")");
        }
        TimeSeries result = new TimeSeries(name + "_ma" + windowSize);
        List<DataPoint> list = new ArrayList<>(points.values());

        // Compute initial window sum
        double windowSum = 0;
        for (int i = 0; i < windowSize; i++) {
            windowSum += list.get(i).value();
        }
        result.add(list.get(windowSize - 1).timestamp(), windowSum / windowSize);
        // Slide the window
        for (int i = windowSize; i < list.size(); i++) {
            windowSum += list.get(i).value() - list.get(i - windowSize).value();
            result.add(list.get(i).timestamp(), windowSum / windowSize);
        }
        return result;
    }

    /**
     * Calculate exponential moving average
     * 计算指数移动平均
     *
     * @param alpha the smoothing factor (0-1) | 平滑因子（0-1）
     * @return the EMA series | EMA序列
     */
    public TimeSeries exponentialMovingAverage(double alpha) {
        if (alpha <= 0 || alpha > 1) {
            throw new IllegalArgumentException("Alpha must be in (0, 1], got: " + alpha);
        }
        TimeSeries result = new TimeSeries(name + "_ema");
        double ema = 0;
        boolean first = true;

        for (DataPoint point : points.values()) {
            if (first) {
                ema = point.value();
                first = false;
            } else {
                ema = alpha * point.value() + (1 - alpha) * ema;
            }
            result.add(point.timestamp(), ema);
        }
        return result;
    }

    // === Metadata methods ===

    /**
     * Set metadata
     * 设置元数据
     *
     * @param key the key | 键
     * @param value the value | 值
     */
    public void setMetadata(String key, String value) {
        metadata.put(key, value);
    }

    /**
     * Get metadata
     * 获取元数据
     *
     * @param key the key | 键
     * @return the value | 值
     */
    public String getMetadata(String key) {
        return metadata.get(key);
    }

    /**
     * Get all metadata
     * 获取所有元数据
     *
     * @return the metadata map | 元数据映射
     */
    public Map<String, String> getAllMetadata() {
        return Map.copyOf(metadata);
    }

    // === Cleanup methods ===

    /**
     * Retain data within duration
     * 保留指定时长内的数据
     *
     * @param duration the duration to retain | 保留的时长
     */
    public void retain(Duration duration) {
        Instant cutoff = Instant.now().minus(duration);
        points.headMap(cutoff).clear();
    }

    /**
     * Clear all data
     * 清除所有数据
     */
    public void clear() {
        points.clear();
    }

    /**
     * Get all data points
     * 获取所有数据点
     *
     * @return the data points list | 数据点列表
     */
    public List<DataPoint> all() {
        return getPoints();
    }

    @Override
    public String toString() {
        return String.format("TimeSeries[%s, size=%d]", name, size());
    }
}
