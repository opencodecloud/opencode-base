package cloud.opencode.base.timeseries;

import cloud.opencode.base.timeseries.alignment.AlignmentUtil;
import cloud.opencode.base.timeseries.sampling.FillStrategy;
import cloud.opencode.base.timeseries.detection.AnomalyDetectorUtil;
import cloud.opencode.base.timeseries.forecast.ForecastUtil;
import cloud.opencode.base.timeseries.interpolation.InterpolationUtil;
import cloud.opencode.base.timeseries.math.MathUtil;
import cloud.opencode.base.timeseries.quality.Gap;
import cloud.opencode.base.timeseries.quality.GapDetector;
import cloud.opencode.base.timeseries.rate.RateUtil;
import cloud.opencode.base.timeseries.sampling.AggregationType;
import cloud.opencode.base.timeseries.sampling.DownsamplingUtil;
import cloud.opencode.base.timeseries.sampling.SamplerUtil;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Open Time Series
 * 开放时间序列
 *
 * <p>Main facade for time series operations with global store.</p>
 * <p>时间序列操作的主要门面，带全局存储。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Global named time series store with ConcurrentHashMap - 使用 ConcurrentHashMap 的全局命名时间序列存储</li>
 *   <li>Shortcut methods for recording, querying, and statistics - 记录、查询和统计的快捷方法</li>
 *   <li>Aggregation, forecasting, downsampling, and anomaly detection - 聚合、预测、降采样和异常检测</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * OpenTimeSeries.record("cpu", 75.5);
 * List<DataPoint> recent = OpenTimeSeries.query("cpu", Duration.ofHours(1));
 * TimeSeriesStats stats = OpenTimeSeries.stats("cpu");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (global store uses ConcurrentHashMap) - 线程安全: 是（全局存储使用 ConcurrentHashMap）</li>
 *   <li>Null-safe: No (null series name may cause NullPointerException) - 空值安全: 否（空序列名可能导致异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public final class OpenTimeSeries {

    private static final Map<String, TimeSeries> store = new ConcurrentHashMap<>();

    private OpenTimeSeries() {
        // Utility class
    }

    // === Global store methods ===

    /**
     * Get or create time series
     * 获取或创建时间序列
     *
     * @param name the series name | 序列名称
     * @return the time series | 时间序列
     */
    public static TimeSeries get(String name) {
        return store.computeIfAbsent(name, TimeSeries::new);
    }

    /**
     * Record data point now
     * 记录当前数据点
     *
     * @param name the series name | 序列名称
     * @param value the value | 值
     */
    public static void record(String name, double value) {
        get(name).addNow(value);
    }

    /**
     * Record data point with timestamp
     * 记录带时间戳的数据点
     *
     * @param name the series name | 序列名称
     * @param timestamp the timestamp | 时间戳
     * @param value the value | 值
     */
    public static void record(String name, Instant timestamp, double value) {
        get(name).add(timestamp, value);
    }

    /**
     * Query range data
     * 查询范围数据
     *
     * @param name the series name | 序列名称
     * @param duration the duration | 时长
     * @return the data points | 数据点
     */
    public static List<DataPoint> query(String name, Duration duration) {
        return get(name).last(duration).getPoints();
    }

    /**
     * Get statistics
     * 获取统计信息
     *
     * @param name the series name | 序列名称
     * @return the statistics | 统计信息
     */
    public static TimeSeriesStats stats(String name) {
        return get(name).stats();
    }

    /**
     * Remove time series
     * 删除时间序列
     *
     * @param name the series name | 序列名称
     */
    public static void remove(String name) {
        store.remove(name);
    }

    /**
     * Check if series exists
     * 检查序列是否存在
     *
     * @param name the series name | 序列名称
     * @return true if exists | 如果存在返回true
     */
    public static boolean exists(String name) {
        return store.containsKey(name);
    }

    /**
     * Cleanup old data across all series
     * 清理所有序列中的旧数据
     *
     * @param retention the retention duration | 保留时长
     */
    public static void cleanup(Duration retention) {
        store.values().forEach(ts -> ts.retain(retention));
    }

    /**
     * Get all series names
     * 获取所有序列名称
     *
     * @return the series names | 序列名称
     */
    public static List<String> getSeriesNames() {
        return List.copyOf(store.keySet());
    }

    /**
     * Clear all series
     * 清除所有序列
     */
    public static void clearAll() {
        store.clear();
    }

    // === Creation methods ===

    /**
     * Create new time series (not stored)
     * 创建新的时间序列（不存储）
     *
     * @param name the series name | 序列名称
     * @return the time series | 时间序列
     */
    public static TimeSeries create(String name) {
        return new TimeSeries(name);
    }

    /**
     * Create time series with points (not stored)
     * 使用点创建时间序列（不存储）
     *
     * @param name the series name | 序列名称
     * @param points the initial points | 初始点
     * @return the time series | 时间序列
     */
    public static TimeSeries of(String name, Collection<DataPoint> points) {
        return new TimeSeries(name, points);
    }

    /**
     * Create bounded time series
     * 创建有界时间序列
     *
     * @param name the series name | 序列名称
     * @param maxSize the maximum size | 最大容量
     * @param maxAge the maximum age | 最大时间
     * @return the bounded time series | 有界时间序列
     */
    public static BoundedTimeSeries bounded(String name, int maxSize, Duration maxAge) {
        return BoundedTimeSeries.of(name, maxSize, maxAge);
    }

    /**
     * Create data point
     * 创建数据点
     *
     * @param timestamp the timestamp | 时间戳
     * @param value the value | 值
     * @return the data point | 数据点
     */
    public static DataPoint point(Instant timestamp, double value) {
        return DataPoint.of(timestamp, value);
    }

    /**
     * Create data point now
     * 创建当前数据点
     *
     * @param value the value | 值
     * @return the data point | 数据点
     */
    public static DataPoint point(double value) {
        return DataPoint.now(value);
    }

    // === Aggregation shortcuts ===

    /**
     * Downsample series
     * 降采样序列
     *
     * @param series the series | 序列
     * @param interval the interval | 间隔
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries downsample(TimeSeries series, Duration interval) {
        return SamplerUtil.downsample(series, interval, AggregationType.AVG);
    }

    /**
     * Downsample series with aggregation type
     * 使用聚合类型降采样序列
     *
     * @param series the series | 序列
     * @param interval the interval | 间隔
     * @param aggregation the aggregation type | 聚合类型
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries downsample(TimeSeries series, Duration interval, AggregationType aggregation) {
        return SamplerUtil.downsample(series, interval, aggregation);
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
        return Aggregation.movingAverage(series, window);
    }

    /**
     * Calculate exponential moving average
     * 计算指数移动平均
     *
     * @param series the series | 序列
     * @param alpha the smoothing factor | 平滑因子
     * @return the EMA series | EMA序列
     */
    public static TimeSeries ema(TimeSeries series, double alpha) {
        return Aggregation.exponentialMovingAverage(series, alpha);
    }

    /**
     * Calculate rolling statistics
     * 计算滚动统计
     *
     * @param series the series | 序列
     * @param window the window size | 窗口大小
     * @return the rolling stats map | 滚动统计映射
     */
    public static Map<String, TimeSeries> rollingStats(TimeSeries series, int window) {
        return Aggregation.rollingStats(series, window);
    }

    // === Anomaly detection shortcuts ===

    /**
     * Detect anomalies by Z-Score
     * 使用Z-Score检测异常
     *
     * @param series the series | 序列
     * @param threshold the threshold | 阈值
     * @return the anomalies | 异常点
     */
    public static List<DataPoint> detectAnomalies(TimeSeries series, double threshold) {
        return AnomalyDetectorUtil.detectByZScore(series, threshold);
    }

    // === Utility methods ===

    /**
     * Merge multiple series
     * 合并多个序列
     *
     * @param series the series to merge | 要合并的序列
     * @return the merged series | 合并后的序列
     */
    public static TimeSeries merge(TimeSeries... series) {
        if (series.length == 0) {
            return create("merged");
        }
        TimeSeries result = create("merged");
        for (TimeSeries s : series) {
            result.addAll(s.getPoints());
        }
        return result;
    }

    /**
     * Fill NaN values
     * 填充NaN值
     *
     * @param series the series | 序列
     * @param fillValue the fill value | 填充值
     * @return the filled series | 填充后的序列
     */
    public static TimeSeries fill(TimeSeries series, double fillValue) {
        return series.map(v -> Double.isNaN(v) ? fillValue : v);
    }

    /**
     * Min-max normalize series to [0,1]
     * 最小-最大归一化到[0,1]
     *
     * @param series the series | 序列
     * @return the normalized series | 归一化后的序列
     */
    public static TimeSeries normalize(TimeSeries series) {
        return MathUtil.normalize(series);
    }

    /**
     * Z-score standardize series
     * Z-score标准化
     *
     * @param series the series | 序列
     * @return the standardized series | 标准化后的序列
     */
    public static TimeSeries standardize(TimeSeries series) {
        return MathUtil.zScore(series);
    }

    // ==================== Forecast shortcuts | 预测快捷方法 ====================

    /**
     * Forecast using Simple Moving Average
     * 使用简单移动平均进行预测
     *
     * @param series the series | 序列
     * @param windowSize the window size | 窗口大小
     * @param steps number of steps to forecast | 预测步数
     * @return the forecasted series | 预测序列
     */
    public static TimeSeries smaForecast(TimeSeries series, int windowSize, int steps) {
        return ForecastUtil.smaForecast(series, windowSize, steps);
    }

    /**
     * Forecast using Exponential Moving Average
     * 使用指数移动平均进行预测
     *
     * @param series the series | 序列
     * @param alpha the smoothing factor (0-1) | 平滑因子（0-1）
     * @param steps number of steps to forecast | 预测步数
     * @return the forecasted series | 预测序列
     */
    public static TimeSeries emaForecast(TimeSeries series, double alpha, int steps) {
        return ForecastUtil.emaForecast(series, alpha, steps);
    }

    /**
     * Forecast using Linear Regression
     * 使用线性回归进行预测
     *
     * @param series the series | 序列
     * @param steps number of steps to forecast | 预测步数
     * @return the forecasted series | 预测序列
     */
    public static TimeSeries linearForecast(TimeSeries series, int steps) {
        return ForecastUtil.linearForecast(series, steps);
    }

    /**
     * Forecast using Double Exponential Smoothing (Holt's method)
     * 使用双指数平滑（Holt方法）进行预测
     *
     * @param series the series | 序列
     * @param alpha the level smoothing factor | 水平平滑因子
     * @param beta the trend smoothing factor | 趋势平滑因子
     * @param steps number of steps to forecast | 预测步数
     * @return the forecasted series | 预测序列
     */
    public static TimeSeries holtForecast(TimeSeries series, double alpha, double beta, int steps) {
        return ForecastUtil.holtForecast(series, alpha, beta, steps);
    }

    /**
     * Forecast with confidence bounds using SMA
     * 使用SMA预测并带置信边界
     *
     * @param series the series | 序列
     * @param windowSize the window size | 窗口大小
     * @param steps number of steps to forecast | 预测步数
     * @param confidence confidence level (0-1) | 置信水平（0-1）
     * @return the forecast result with bounds | 带边界的预测结果
     */
    public static ForecastUtil.ForecastResult forecastWithBounds(TimeSeries series, int windowSize,
                                                                   int steps, double confidence) {
        return ForecastUtil.smaForecastWithBounds(series, windowSize, steps, confidence);
    }

    // ==================== Advanced downsampling shortcuts | 高级降采样快捷方法 ====================

    /**
     * Downsample using LTTB algorithm (preserves visual shape)
     * 使用LTTB算法降采样（保持视觉形状）
     *
     * @param series the series | 序列
     * @param targetSize the target number of points | 目标点数
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries lttb(TimeSeries series, int targetSize) {
        return DownsamplingUtil.lttb(series, targetSize);
    }

    /**
     * Downsample using M4 algorithm (preserves min/max)
     * 使用M4算法降采样（保持最小/最大值）
     *
     * @param series the series | 序列
     * @param bucketDuration the duration of each bucket | 每个桶的持续时间
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries m4(TimeSeries series, Duration bucketDuration) {
        return DownsamplingUtil.m4(series, bucketDuration);
    }

    /**
     * Downsample preserving peaks and valleys
     * 保持峰值和谷值的降采样
     *
     * @param series the series | 序列
     * @param targetSize the target number of points | 目标点数
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries peakPreserving(TimeSeries series, int targetSize) {
        return DownsamplingUtil.peakPreserving(series, targetSize);
    }

    /**
     * Downsample using percentile (e.g., median)
     * 使用百分位降采样（例如中位数）
     *
     * @param series the series | 序列
     * @param bucketDuration the duration of each bucket | 每个桶的持续时间
     * @param percentile the percentile (0-100) | 百分位（0-100）
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries percentile(TimeSeries series, Duration bucketDuration, int percentile) {
        return DownsamplingUtil.percentile(series, bucketDuration, percentile);
    }

    /**
     * Downsample using threshold (only keep significant changes)
     * 使用阈值降采样（只保留显著变化）
     *
     * @param series the series | 序列
     * @param threshold the minimum change threshold | 最小变化阈值
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries thresholdDownsample(TimeSeries series, double threshold) {
        return DownsamplingUtil.threshold(series, threshold);
    }

    // ==================== Alignment shortcuts | 对齐快捷方法 ====================

    /**
     * Align two series to common time grid
     * 对齐两个序列到共同时间网格
     *
     * @param a the first series | 第一个序列
     * @param b the second series | 第二个序列
     * @param interval the interval | 间隔
     * @param fill the fill strategy | 填充策略
     * @return the aligned series pair | 对齐后的序列对
     */
    public static TimeSeries[] align(TimeSeries a, TimeSeries b, Duration interval, FillStrategy fill) {
        return AlignmentUtil.align(a, b, interval, fill);
    }

    /**
     * Resample series to regular intervals
     * 重采样序列到规则间隔
     *
     * @param ts the series | 序列
     * @param interval the interval | 间隔
     * @param fill the fill strategy | 填充策略
     * @return the resampled series | 重采样后的序列
     */
    public static TimeSeries resample(TimeSeries ts, Duration interval, FillStrategy fill) {
        return AlignmentUtil.resample(ts, interval, fill);
    }

    // ==================== Rate shortcuts | 速率快捷方法 ====================

    /**
     * Calculate per-second rate from counter
     * 从计数器计算每秒速率
     *
     * @param counter the counter series | 计数器序列
     * @param window the window duration | 窗口时长
     * @return the rate series | 速率序列
     */
    public static TimeSeries rate(TimeSeries counter, Duration window) {
        return RateUtil.rate(counter, window);
    }

    /**
     * Calculate instantaneous rate from counter
     * 计算瞬时速率
     *
     * @param counter the counter series | 计数器序列
     * @return the instantaneous rate series | 瞬时速率序列
     */
    public static TimeSeries irate(TimeSeries counter) {
        return RateUtil.irate(counter);
    }

    // ==================== Interpolation shortcuts | 插值快捷方法 ====================

    /**
     * Linear interpolation at regular intervals
     * 等间隔线性插值
     *
     * @param ts the series | 序列
     * @param interval the interval | 间隔
     * @return the interpolated series | 插值后的序列
     */
    public static TimeSeries interpolate(TimeSeries ts, Duration interval) {
        return InterpolationUtil.linear(ts, interval);
    }

    // ==================== Quality shortcuts | 质量快捷方法 ====================

    /**
     * Detect gaps in time series data
     * 检测时间序列数据中的缺口
     *
     * @param ts the series | 序列
     * @param expectedInterval the expected interval | 期望间隔
     * @return the list of gaps | 缺口列表
     */
    public static List<Gap> detectGaps(TimeSeries ts, Duration expectedInterval) {
        return GapDetector.detectGaps(ts, expectedInterval);
    }

    /**
     * Calculate data completeness percentage
     * 计算数据完整性百分比
     *
     * @param ts the series | 序列
     * @param expectedInterval the expected interval | 期望间隔
     * @param from the start time | 开始时间
     * @param to the end time | 结束时间
     * @return the completeness percentage (0-1) | 完整性百分比（0-1）
     */
    public static double dataCompleteness(TimeSeries ts, Duration expectedInterval, Instant from, Instant to) {
        return GapDetector.dataCompleteness(ts, expectedInterval, from, to);
    }
}
