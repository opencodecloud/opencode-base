package cloud.opencode.base.timeseries.detection;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;

import java.util.ArrayList;
import java.util.List;

/**
 * Anomaly Detector Util
 * 异常检测工具类
 *
 * <p>Utilities for detecting anomalies in time series data.</p>
 * <p>检测时间序列数据中异常的工具类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Z-Score and IQR anomaly detection - Z 分数和 IQR 异常检测</li>
 *   <li>Moving average deviation detection - 移动平均偏差检测</li>
 *   <li>Spike detection and out-of-range detection - 尖峰检测和范围外检测</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<DataPoint> anomalies = AnomalyDetectorUtil.detectByZScore(series, 2.5);
 * List<DataPoint> outliers = AnomalyDetectorUtil.detectByIQR(series, 1.5);
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
 *   <li>Time complexity: O(n) for Z-Score/moving-average/spike detection; O(n log n) for IQR due to sorting - 时间复杂度: Z-Score/移动平均/尖峰检测为 O(n)；IQR 因排序为 O(n log n)</li>
 *   <li>Space complexity: O(n) - result anomaly list proportional to series length in the worst case - 空间复杂度: O(n) - 结果异常列表在最坏情况下与序列长度成正比</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public final class AnomalyDetectorUtil {

    private AnomalyDetectorUtil() {
        // Utility class
    }

    /**
     * Detect anomalies using Z-Score
     * 使用Z-Score检测异常
     *
     * @param series the time series | 时间序列
     * @param threshold the z-score threshold (typically 2-3) | Z分数阈值（通常2-3）
     * @return the anomaly data points | 异常数据点
     */
    public static List<DataPoint> detectByZScore(TimeSeries series, double threshold) {
        double avg = series.average().orElse(0);
        double stdDev = series.stdDev();

        if (stdDev == 0) {
            return List.of();
        }

        return series.getPoints().stream()
            .filter(p -> Math.abs((p.value() - avg) / stdDev) > threshold)
            .toList();
    }

    /**
     * Detect anomalies using IQR (Interquartile Range)
     * 使用IQR（四分位距）检测异常
     *
     * @param series the time series | 时间序列
     * @param multiplier the IQR multiplier (typically 1.5) | IQR乘数（通常1.5）
     * @return the anomaly data points | 异常数据点
     */
    public static List<DataPoint> detectByIQR(TimeSeries series, double multiplier) {
        double q1 = percentile(series, 25);
        double q3 = percentile(series, 75);
        double iqr = q3 - q1;

        double lower = q1 - multiplier * iqr;
        double upper = q3 + multiplier * iqr;

        return series.getPoints().stream()
            .filter(p -> p.value() < lower || p.value() > upper)
            .toList();
    }

    /**
     * Detect anomalies using moving average deviation
     * 使用移动平均偏差检测异常
     *
     * @param series the time series | 时间序列
     * @param windowSize the window size | 窗口大小
     * @param threshold the deviation threshold | 偏差阈值
     * @return the anomaly data points | 异常数据点
     */
    public static List<DataPoint> detectByMovingAverage(TimeSeries series,
                                                         int windowSize, double threshold) {
        List<DataPoint> points = series.getPoints();
        if (points.size() < windowSize) {
            return List.of();
        }

        // Calculate moving averages
        List<Double> movingAverages = new ArrayList<>();
        double sum = 0;

        for (int i = 0; i < points.size(); i++) {
            sum += points.get(i).value();
            if (i >= windowSize) {
                sum -= points.get(i - windowSize).value();
            }
            if (i >= windowSize - 1) {
                movingAverages.add(sum / windowSize);
            }
        }

        // Find anomalies
        List<DataPoint> anomalies = new ArrayList<>();
        for (int i = 0; i < movingAverages.size(); i++) {
            int origIndex = i + windowSize - 1;
            DataPoint point = points.get(origIndex);
            double ma = movingAverages.get(i);
            if (Math.abs(point.value() - ma) > threshold) {
                anomalies.add(point);
            }
        }

        return anomalies;
    }

    /**
     * Detect anomalies using standard deviation threshold
     * 使用标准差阈值检测异常
     *
     * @param series the time series | 时间序列
     * @param sigmas the number of standard deviations | 标准差的倍数
     * @return the anomaly data points | 异常数据点
     */
    public static List<DataPoint> detectByStdDev(TimeSeries series, double sigmas) {
        return detectByZScore(series, sigmas);
    }

    /**
     * Detect sudden changes (spikes)
     * 检测突变（尖峰）
     *
     * @param series the time series | 时间序列
     * @param changeThreshold the change threshold percentage (0-1) | 变化阈值百分比（0-1）
     * @return the spike data points | 尖峰数据点
     */
    public static List<DataPoint> detectSpikes(TimeSeries series, double changeThreshold) {
        List<DataPoint> points = series.getPoints();
        if (points.size() < 2) {
            return List.of();
        }

        List<DataPoint> spikes = new ArrayList<>();
        for (int i = 1; i < points.size(); i++) {
            double prev = points.get(i - 1).value();
            double curr = points.get(i).value();

            if (prev != 0) {
                double change = Math.abs((curr - prev) / prev);
                if (change > changeThreshold) {
                    spikes.add(points.get(i));
                }
            }
        }

        return spikes;
    }

    /**
     * Detect values outside range
     * 检测范围外的值
     *
     * @param series the time series | 时间序列
     * @param min the minimum value | 最小值
     * @param max the maximum value | 最大值
     * @return the out-of-range data points | 范围外的数据点
     */
    public static List<DataPoint> detectOutOfRange(TimeSeries series, double min, double max) {
        return series.getPoints().stream()
            .filter(p -> p.value() < min || p.value() > max)
            .toList();
    }

    /**
     * Get anomaly summary
     * 获取异常摘要
     *
     * @param anomalies the anomaly list | 异常列表
     * @param totalPoints the total points | 总点数
     * @return the summary string | 摘要字符串
     */
    public static String getSummary(List<DataPoint> anomalies, int totalPoints) {
        if (anomalies.isEmpty()) {
            return "No anomalies detected";
        }

        double percentage = (double) anomalies.size() / totalPoints * 100;
        double avgValue = anomalies.stream()
            .mapToDouble(DataPoint::value)
            .average()
            .orElse(0);

        return String.format(
            "Found %d anomalies (%.2f%%), avg value: %.2f",
            anomalies.size(), percentage, avgValue
        );
    }

    private static double percentile(TimeSeries series, int p) {
        List<Double> values = series.getPoints().stream()
            .map(DataPoint::value)
            .sorted()
            .toList();

        if (values.isEmpty()) {
            return 0;
        }

        int index = (int) Math.ceil(p / 100.0 * values.size()) - 1;
        return values.get(Math.max(0, index));
    }
}
