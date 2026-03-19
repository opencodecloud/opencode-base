package cloud.opencode.base.timeseries.forecast;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Forecast Util
 * 预测工具类
 *
 * <p>Utilities for time series forecasting and prediction.</p>
 * <p>时间序列预测的工具类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Simple Moving Average (SMA) | 简单移动平均</li>
 *   <li>Weighted Moving Average (WMA) | 加权移动平均</li>
 *   <li>Exponential Moving Average (EMA) | 指数移动平均</li>
 *   <li>Double Exponential Smoothing (Holt) | 双指数平滑（Holt）</li>
 *   <li>Linear Trend | 线性趋势</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple moving average forecast
 * TimeSeries forecast = ForecastUtil.smaForecast(series, 7, 3);
 *
 * // Exponential smoothing forecast
 * TimeSeries forecast = ForecastUtil.emaForecast(series, 0.3, 5);
 *
 * // Linear trend forecast
 * TimeSeries forecast = ForecastUtil.linearForecast(series, 10);
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
 *   <li>Time complexity: O(n) for all methods - SMA/WMA/EMA/Holt/linear each make a single pass over the series - 时间复杂度: 所有方法均为 O(n) - SMA/WMA/EMA/Holt/线性回归均对序列进行单次遍历</li>
 *   <li>Space complexity: O(s) where s is the number of forecast steps - forecast output series proportional to steps - 空间复杂度: O(s)，s 为预测步数 - 预测输出序列与步数成正比</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public final class ForecastUtil {

    private ForecastUtil() {
        // Utility class
    }

    // ==================== Simple Moving Average Forecast | 简单移动平均预测 ====================

    /**
     * Forecast using Simple Moving Average (SMA)
     * 使用简单移动平均进行预测
     *
     * <p>The SMA forecast uses the average of the last N points as the predicted value.</p>
     * <p>简单移动平均使用最后N个点的平均值作为预测值。</p>
     *
     * @param series the time series | 时间序列
     * @param windowSize the moving average window size | 移动平均窗口大小
     * @param steps number of steps to forecast | 预测步数
     * @return the forecasted series | 预测的序列
     */
    public static TimeSeries smaForecast(TimeSeries series, int windowSize, int steps) {
        if (windowSize <= 0) {
            throw new IllegalArgumentException("Window size must be positive");
        }
        if (steps <= 0) {
            throw new IllegalArgumentException("Steps must be positive");
        }

        List<DataPoint> points = series.getPoints();
        if (points.size() < windowSize) {
            return new TimeSeries(series.getName() + "_forecast");
        }

        // Calculate interval between points
        Duration interval = calculateInterval(points);

        // Calculate SMA of last windowSize points
        double sma = calculateSMA(points, windowSize);

        // Generate forecast points
        TimeSeries forecast = new TimeSeries(series.getName() + "_forecast");
        Instant lastTime = points.getLast().timestamp();

        for (int i = 1; i <= steps; i++) {
            Instant forecastTime = lastTime.plus(interval.multipliedBy(i));
            forecast.add(DataPoint.of(forecastTime, sma));
        }

        return forecast;
    }

    /**
     * Forecast using Simple Moving Average with confidence interval
     * 使用简单移动平均预测并带置信区间
     *
     * @param series the time series | 时间序列
     * @param windowSize the moving average window size | 移动平均窗口大小
     * @param steps number of steps to forecast | 预测步数
     * @param confidenceLevel confidence level (0-1) | 置信水平（0-1）
     * @return the forecast result with bounds | 带边界的预测结果
     */
    public static ForecastResult smaForecastWithBounds(TimeSeries series, int windowSize,
                                                        int steps, double confidenceLevel) {
        List<DataPoint> points = series.getPoints();
        if (points.size() < windowSize) {
            return ForecastResult.empty(series.getName());
        }

        Duration interval = calculateInterval(points);
        double sma = calculateSMA(points, windowSize);
        double stdDev = calculateStdDev(points, windowSize);

        // Z-score for confidence level (simplified)
        double z = getZScore(confidenceLevel);
        double margin = z * stdDev;

        TimeSeries forecast = new TimeSeries(series.getName() + "_forecast");
        TimeSeries lower = new TimeSeries(series.getName() + "_lower");
        TimeSeries upper = new TimeSeries(series.getName() + "_upper");

        Instant lastTime = points.getLast().timestamp();

        for (int i = 1; i <= steps; i++) {
            Instant forecastTime = lastTime.plus(interval.multipliedBy(i));
            forecast.add(DataPoint.of(forecastTime, sma));
            lower.add(DataPoint.of(forecastTime, sma - margin));
            upper.add(DataPoint.of(forecastTime, sma + margin));
        }

        return new ForecastResult(forecast, lower, upper, confidenceLevel);
    }

    // ==================== Weighted Moving Average Forecast | 加权移动平均预测 ====================

    /**
     * Forecast using Weighted Moving Average (WMA)
     * 使用加权移动平均进行预测
     *
     * <p>Recent values are weighted more heavily than older values.</p>
     * <p>较近的值比较旧的值有更高的权重。</p>
     *
     * @param series the time series | 时间序列
     * @param windowSize the moving average window size | 移动平均窗口大小
     * @param steps number of steps to forecast | 预测步数
     * @return the forecasted series | 预测的序列
     */
    public static TimeSeries wmaForecast(TimeSeries series, int windowSize, int steps) {
        if (windowSize <= 0) {
            throw new IllegalArgumentException("Window size must be positive");
        }

        List<DataPoint> points = series.getPoints();
        if (points.size() < windowSize) {
            return new TimeSeries(series.getName() + "_forecast");
        }

        Duration interval = calculateInterval(points);
        double wma = calculateWMA(points, windowSize);

        TimeSeries forecast = new TimeSeries(series.getName() + "_forecast");
        Instant lastTime = points.getLast().timestamp();

        for (int i = 1; i <= steps; i++) {
            Instant forecastTime = lastTime.plus(interval.multipliedBy(i));
            forecast.add(DataPoint.of(forecastTime, wma));
        }

        return forecast;
    }

    // ==================== Exponential Moving Average Forecast | 指数移动平均预测 ====================

    /**
     * Forecast using Exponential Moving Average (EMA)
     * 使用指数移动平均进行预测
     *
     * <p>Uses exponential smoothing where recent values have higher weights.</p>
     * <p>使用指数平滑，较近的值有更高的权重。</p>
     *
     * @param series the time series | 时间序列
     * @param alpha the smoothing factor (0-1) | 平滑因子（0-1）
     * @param steps number of steps to forecast | 预测步数
     * @return the forecasted series | 预测的序列
     */
    public static TimeSeries emaForecast(TimeSeries series, double alpha, int steps) {
        if (alpha <= 0 || alpha > 1) {
            throw new IllegalArgumentException("Alpha must be between 0 (exclusive) and 1 (inclusive)");
        }

        List<DataPoint> points = series.getPoints();
        if (points.isEmpty()) {
            return new TimeSeries(series.getName() + "_forecast");
        }

        Duration interval = calculateInterval(points);
        double ema = calculateEMA(points, alpha);

        TimeSeries forecast = new TimeSeries(series.getName() + "_forecast");
        Instant lastTime = points.getLast().timestamp();

        for (int i = 1; i <= steps; i++) {
            Instant forecastTime = lastTime.plus(interval.multipliedBy(i));
            forecast.add(DataPoint.of(forecastTime, ema));
        }

        return forecast;
    }

    // ==================== Double Exponential Smoothing (Holt) | 双指数平滑（Holt） ====================

    /**
     * Forecast using Double Exponential Smoothing (Holt's method)
     * 使用双指数平滑（Holt方法）进行预测
     *
     * <p>Accounts for both level and trend in the data.</p>
     * <p>考虑数据中的水平和趋势。</p>
     *
     * @param series the time series | 时间序列
     * @param alpha the level smoothing factor (0-1) | 水平平滑因子（0-1）
     * @param beta the trend smoothing factor (0-1) | 趋势平滑因子（0-1）
     * @param steps number of steps to forecast | 预测步数
     * @return the forecasted series | 预测的序列
     */
    public static TimeSeries holtForecast(TimeSeries series, double alpha, double beta, int steps) {
        if (alpha <= 0 || alpha > 1) {
            throw new IllegalArgumentException("Alpha must be between 0 (exclusive) and 1 (inclusive)");
        }
        if (beta <= 0 || beta > 1) {
            throw new IllegalArgumentException("Beta must be between 0 (exclusive) and 1 (inclusive)");
        }

        List<DataPoint> points = series.getPoints();
        if (points.size() < 2) {
            return new TimeSeries(series.getName() + "_forecast");
        }

        Duration interval = calculateInterval(points);

        // Initialize level and trend
        double level = points.getFirst().value();
        double trend = points.get(1).value() - points.getFirst().value();

        // Apply Holt's method
        for (int i = 1; i < points.size(); i++) {
            double value = points.get(i).value();
            double newLevel = alpha * value + (1 - alpha) * (level + trend);
            double newTrend = beta * (newLevel - level) + (1 - beta) * trend;
            level = newLevel;
            trend = newTrend;
        }

        TimeSeries forecast = new TimeSeries(series.getName() + "_forecast");
        Instant lastTime = points.getLast().timestamp();

        for (int i = 1; i <= steps; i++) {
            double forecastValue = level + i * trend;
            Instant forecastTime = lastTime.plus(interval.multipliedBy(i));
            forecast.add(DataPoint.of(forecastTime, forecastValue));
        }

        return forecast;
    }

    // ==================== Linear Trend Forecast | 线性趋势预测 ====================

    /**
     * Forecast using Linear Regression
     * 使用线性回归进行预测
     *
     * <p>Fits a linear trend line to the data and extrapolates.</p>
     * <p>对数据拟合线性趋势线并进行外推。</p>
     *
     * @param series the time series | 时间序列
     * @param steps number of steps to forecast | 预测步数
     * @return the forecasted series | 预测的序列
     */
    public static TimeSeries linearForecast(TimeSeries series, int steps) {
        List<DataPoint> points = series.getPoints();
        if (points.size() < 2) {
            return new TimeSeries(series.getName() + "_forecast");
        }

        Duration interval = calculateInterval(points);

        // Calculate linear regression coefficients
        LinearCoefficients coeffs = calculateLinearCoefficients(points);

        TimeSeries forecast = new TimeSeries(series.getName() + "_forecast");
        Instant lastTime = points.getLast().timestamp();
        int n = points.size();

        for (int i = 1; i <= steps; i++) {
            double x = n + i; // Future x value
            double forecastValue = coeffs.intercept() + coeffs.slope() * x;
            Instant forecastTime = lastTime.plus(interval.multipliedBy(i));
            forecast.add(DataPoint.of(forecastTime, forecastValue));
        }

        return forecast;
    }

    /**
     * Forecast using Linear Regression with confidence bounds
     * 使用线性回归预测并带置信边界
     *
     * @param series the time series | 时间序列
     * @param steps number of steps to forecast | 预测步数
     * @param confidenceLevel confidence level (0-1) | 置信水平（0-1）
     * @return the forecast result with bounds | 带边界的预测结果
     */
    public static ForecastResult linearForecastWithBounds(TimeSeries series, int steps,
                                                           double confidenceLevel) {
        List<DataPoint> points = series.getPoints();
        if (points.size() < 2) {
            return ForecastResult.empty(series.getName());
        }

        Duration interval = calculateInterval(points);
        LinearCoefficients coeffs = calculateLinearCoefficients(points);
        double residualStdDev = calculateResidualStdDev(points, coeffs);
        double z = getZScore(confidenceLevel);

        TimeSeries forecast = new TimeSeries(series.getName() + "_forecast");
        TimeSeries lower = new TimeSeries(series.getName() + "_lower");
        TimeSeries upper = new TimeSeries(series.getName() + "_upper");

        Instant lastTime = points.getLast().timestamp();
        int n = points.size();

        for (int i = 1; i <= steps; i++) {
            double x = n + i;
            double forecastValue = coeffs.intercept() + coeffs.slope() * x;
            double margin = z * residualStdDev * Math.sqrt(1 + 1.0 / n);

            Instant forecastTime = lastTime.plus(interval.multipliedBy(i));
            forecast.add(DataPoint.of(forecastTime, forecastValue));
            lower.add(DataPoint.of(forecastTime, forecastValue - margin));
            upper.add(DataPoint.of(forecastTime, forecastValue + margin));
        }

        return new ForecastResult(forecast, lower, upper, confidenceLevel);
    }

    // ==================== Naive Forecast | 朴素预测 ====================

    /**
     * Naive forecast (last value repeated)
     * 朴素预测（重复最后一个值）
     *
     * @param series the time series | 时间序列
     * @param steps number of steps to forecast | 预测步数
     * @return the forecasted series | 预测的序列
     */
    public static TimeSeries naiveForecast(TimeSeries series, int steps) {
        List<DataPoint> points = series.getPoints();
        if (points.isEmpty()) {
            return new TimeSeries(series.getName() + "_forecast");
        }

        Duration interval = calculateInterval(points);
        double lastValue = points.getLast().value();

        TimeSeries forecast = new TimeSeries(series.getName() + "_forecast");
        Instant lastTime = points.getLast().timestamp();

        for (int i = 1; i <= steps; i++) {
            Instant forecastTime = lastTime.plus(interval.multipliedBy(i));
            forecast.add(DataPoint.of(forecastTime, lastValue));
        }

        return forecast;
    }

    /**
     * Seasonal naive forecast (repeat values from same period)
     * 季节性朴素预测（重复相同周期的值）
     *
     * @param series the time series | 时间序列
     * @param seasonLength the season length in points | 季节长度（点数）
     * @param steps number of steps to forecast | 预测步数
     * @return the forecasted series | 预测的序列
     */
    public static TimeSeries seasonalNaiveForecast(TimeSeries series, int seasonLength, int steps) {
        List<DataPoint> points = series.getPoints();
        if (points.size() < seasonLength) {
            return new TimeSeries(series.getName() + "_forecast");
        }

        Duration interval = calculateInterval(points);

        TimeSeries forecast = new TimeSeries(series.getName() + "_forecast");
        Instant lastTime = points.getLast().timestamp();
        int n = points.size();

        for (int i = 1; i <= steps; i++) {
            int seasonIndex = (n - seasonLength + (i - 1) % seasonLength) % n;
            double forecastValue = points.get(seasonIndex).value();
            Instant forecastTime = lastTime.plus(interval.multipliedBy(i));
            forecast.add(DataPoint.of(forecastTime, forecastValue));
        }

        return forecast;
    }

    // ==================== Drift Forecast | 漂移预测 ====================

    /**
     * Drift forecast (extrapolate average change)
     * 漂移预测（外推平均变化）
     *
     * @param series the time series | 时间序列
     * @param steps number of steps to forecast | 预测步数
     * @return the forecasted series | 预测的序列
     */
    public static TimeSeries driftForecast(TimeSeries series, int steps) {
        List<DataPoint> points = series.getPoints();
        if (points.size() < 2) {
            return new TimeSeries(series.getName() + "_forecast");
        }

        Duration interval = calculateInterval(points);
        double firstValue = points.getFirst().value();
        double lastValue = points.getLast().value();
        double drift = (lastValue - firstValue) / (points.size() - 1);

        TimeSeries forecast = new TimeSeries(series.getName() + "_forecast");
        Instant lastTime = points.getLast().timestamp();

        for (int i = 1; i <= steps; i++) {
            double forecastValue = lastValue + i * drift;
            Instant forecastTime = lastTime.plus(interval.multipliedBy(i));
            forecast.add(DataPoint.of(forecastTime, forecastValue));
        }

        return forecast;
    }

    // ==================== Helper Methods | 辅助方法 ====================

    private static Duration calculateInterval(List<DataPoint> points) {
        if (points.size() < 2) {
            return Duration.ofMinutes(1); // Default
        }
        // Calculate average interval
        long totalMillis = points.getLast().epochMillis() - points.getFirst().epochMillis();
        return Duration.ofMillis(totalMillis / (points.size() - 1));
    }

    private static double calculateSMA(List<DataPoint> points, int windowSize) {
        int start = Math.max(0, points.size() - windowSize);
        double sum = 0;
        int count = 0;
        for (int i = start; i < points.size(); i++) {
            sum += points.get(i).value();
            count++;
        }
        return sum / count;
    }

    private static double calculateWMA(List<DataPoint> points, int windowSize) {
        int start = Math.max(0, points.size() - windowSize);
        double weightedSum = 0;
        double weightSum = 0;
        int weight = 1;

        for (int i = start; i < points.size(); i++) {
            weightedSum += points.get(i).value() * weight;
            weightSum += weight;
            weight++;
        }

        return weightedSum / weightSum;
    }

    private static double calculateEMA(List<DataPoint> points, double alpha) {
        double ema = points.getFirst().value();
        for (int i = 1; i < points.size(); i++) {
            ema = alpha * points.get(i).value() + (1 - alpha) * ema;
        }
        return ema;
    }

    private static double calculateStdDev(List<DataPoint> points, int windowSize) {
        int start = Math.max(0, points.size() - windowSize);
        double mean = calculateSMA(points, windowSize);
        double sumSquares = 0;
        int count = 0;

        for (int i = start; i < points.size(); i++) {
            double diff = points.get(i).value() - mean;
            sumSquares += diff * diff;
            count++;
        }

        return Math.sqrt(sumSquares / count);
    }

    private static LinearCoefficients calculateLinearCoefficients(List<DataPoint> points) {
        int n = points.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            double x = i + 1;
            double y = points.get(i).value();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        return new LinearCoefficients(intercept, slope);
    }

    private static double calculateResidualStdDev(List<DataPoint> points, LinearCoefficients coeffs) {
        int n = points.size();
        double sumResidualSquares = 0;

        for (int i = 0; i < n; i++) {
            double x = i + 1;
            double predicted = coeffs.intercept() + coeffs.slope() * x;
            double residual = points.get(i).value() - predicted;
            sumResidualSquares += residual * residual;
        }

        return Math.sqrt(sumResidualSquares / (n - 2));
    }

    private static double getZScore(double confidenceLevel) {
        // Simplified z-score lookup
        if (confidenceLevel >= 0.99) return 2.576;
        if (confidenceLevel >= 0.95) return 1.96;
        if (confidenceLevel >= 0.90) return 1.645;
        if (confidenceLevel >= 0.80) return 1.28;
        return 1.0;
    }

    // ==================== Result Types | 结果类型 ====================

    /**
     * Linear regression coefficients
     * 线性回归系数
     */
    public record LinearCoefficients(double intercept, double slope) {
        /**
         * Predict value at x
         * 在x处预测值
         *
         * @param x the x value | x值
         * @return the predicted value | 预测值
         */
        public double predict(double x) {
            return intercept + slope * x;
        }
    }

    /**
     * Forecast result with prediction interval
     * 带预测区间的预测结果
     *
     * @param forecast the forecasted series | 预测序列
     * @param lower the lower bound series | 下界序列
     * @param upper the upper bound series | 上界序列
     * @param confidenceLevel the confidence level | 置信水平
     */
    public record ForecastResult(
            TimeSeries forecast,
            TimeSeries lower,
            TimeSeries upper,
            double confidenceLevel) {

        /**
         * Create empty result
         * 创建空结果
         *
         * @param name the series name | 序列名称
         * @return empty result | 空结果
         */
        public static ForecastResult empty(String name) {
            return new ForecastResult(
                    new TimeSeries(name + "_forecast"),
                    new TimeSeries(name + "_lower"),
                    new TimeSeries(name + "_upper"),
                    0.0
            );
        }

        /**
         * Check if result is empty
         * 检查结果是否为空
         *
         * @return true if empty | 如果为空返回true
         */
        public boolean isEmpty() {
            return forecast.isEmpty();
        }

        /**
         * Get number of forecast points
         * 获取预测点数
         *
         * @return forecast size | 预测大小
         */
        public int size() {
            return forecast.size();
        }
    }
}
