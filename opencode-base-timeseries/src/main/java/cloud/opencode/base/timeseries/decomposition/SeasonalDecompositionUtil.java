/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.timeseries.decomposition;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;

import java.time.Instant;
import java.util.*;

/**
 * Seasonal Decomposition Util - Time series seasonal decomposition
 * 季节性分解工具 - 时间序列季节性分解
 *
 * <p>Decomposes time series into trend, seasonal, and residual components
 * using classical decomposition and STL-like approaches.</p>
 * <p>使用经典分解和类 STL 方法将时间序列分解为趋势、季节性和残差组件。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Classical additive and multiplicative decomposition - 经典加法和乘法分解</li>
 *   <li>STL decomposition with Loess smoothing - 基于 Loess 平滑的 STL 分解</li>
 *   <li>Automatic seasonality period detection via autocorrelation - 通过自相关自动检测季节周期</li>
 *   <li>Seasonal strength and trend strength metrics - 季节性强度和趋势强度指标</li>
 * </ul>
 *
 * <p><strong>Decomposition Models | 分解模型:</strong></p>
 * <ul>
 *   <li><strong>Additive</strong>: Y = Trend + Seasonal + Residual</li>
 *   <li><strong>Multiplicative</strong>: Y = Trend × Seasonal × Residual</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Perform additive decomposition with period 12 (monthly data, yearly seasonality)
 * DecompositionResult result = SeasonalDecompositionUtil.decompose(timeSeries, 12);
 *
 * // Get components
 * TimeSeries trend = result.trend();
 * TimeSeries seasonal = result.seasonal();
 * TimeSeries residual = result.residual();
 *
 * // Perform multiplicative decomposition
 * DecompositionResult result = SeasonalDecompositionUtil.decompose(
 *     timeSeries, 12, DecompositionModel.MULTIPLICATIVE);
 *
 * // Detect seasonality automatically
 * int detectedPeriod = SeasonalDecompositionUtil.detectSeasonalPeriod(timeSeries);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (null series throws IllegalArgumentException) - 空值安全: 否</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n*p) for classical decompose where p is the seasonal period; O(n*p*k) for STL with k iterations - 时间复杂度: 经典分解 O(n*p)，p 为季节周期；k 次迭代的 STL 为 O(n*p*k)</li>
 *   <li>Space complexity: O(n) - trend, seasonal, and residual arrays each proportional to series length - 空间复杂度: O(n) - 趋势、季节性和残差数组各与序列长度成正比</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see TimeSeries
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public final class SeasonalDecompositionUtil {

    /** Default number of iterations for STL */
    private static final int DEFAULT_STL_ITERATIONS = 2;

    private SeasonalDecompositionUtil() {
        // Utility class
    }

    // ==================== Decomposition Models | 分解模型 ====================

    /**
     * Decomposition model type.
     * 分解模型类型。
     */
    public enum DecompositionModel {
        /** Additive: Y = T + S + R */
        ADDITIVE,
        /** Multiplicative: Y = T × S × R */
        MULTIPLICATIVE
    }

    // ==================== Result Types | 结果类型 ====================

    /**
     * Decomposition result containing all components.
     * 包含所有组件的分解结果。
     *
     * @param original the original time series - 原始时间序列
     * @param trend the trend component - 趋势组件
     * @param seasonal the seasonal component - 季节性组件
     * @param residual the residual component - 残差组件
     * @param model the decomposition model used - 使用的分解模型
     * @param period the seasonal period - 季节周期
     */
    public record DecompositionResult(
            TimeSeries original,
            TimeSeries trend,
            TimeSeries seasonal,
            TimeSeries residual,
            DecompositionModel model,
            int period
    ) {
        /**
         * Reconstructs the time series from components.
         * 从组件重建时间序列。
         */
        public TimeSeries reconstruct() {
            TimeSeries result = new TimeSeries(original.getName() + "_reconstructed");
            List<DataPoint> trendPoints = trend.getPoints();
            List<DataPoint> seasonalPoints = seasonal.getPoints();
            List<DataPoint> residualPoints = residual.getPoints();

            int n = Math.min(Math.min(trendPoints.size(), seasonalPoints.size()), residualPoints.size());

            for (int i = 0; i < n; i++) {
                Instant timestamp = trendPoints.get(i).timestamp();
                double t = trendPoints.get(i).value();
                double s = seasonalPoints.get(i).value();
                double r = residualPoints.get(i).value();

                double value = switch (model) {
                    case ADDITIVE -> t + s + r;
                    case MULTIPLICATIVE -> t * s * r;
                };

                result.add(timestamp, value);
            }
            return result;
        }

        /**
         * Gets the seasonally adjusted series (trend + residual).
         * 获取季节性调整序列（趋势 + 残差）。
         */
        public TimeSeries seasonallyAdjusted() {
            TimeSeries result = new TimeSeries(original.getName() + "_adjusted");
            List<DataPoint> origPoints = original.getPoints();
            List<DataPoint> seasonalPoints = seasonal.getPoints();

            int n = Math.min(origPoints.size(), seasonalPoints.size());

            for (int i = 0; i < n; i++) {
                Instant timestamp = origPoints.get(i).timestamp();
                double orig = origPoints.get(i).value();
                double s = seasonalPoints.get(i).value();

                double adjusted = switch (model) {
                    case ADDITIVE -> orig - s;
                    case MULTIPLICATIVE -> orig / s;
                };

                result.add(timestamp, adjusted);
            }
            return result;
        }

        /**
         * Gets seasonal strength (0-1, higher means stronger seasonality).
         * 获取季节性强度（0-1，越高表示季节性越强）。
         */
        public double seasonalStrength() {
            double residualVar = variance(residual.getValues());
            double[] combined = addArrays(seasonal.getValues(), residual.getValues());
            double combinedVar = variance(combined);

            if (combinedVar == 0) return 0.0;
            return Math.max(0, 1 - residualVar / combinedVar);
        }

        /**
         * Gets trend strength (0-1, higher means stronger trend).
         * 获取趋势强度（0-1，越高表示趋势越强）。
         */
        public double trendStrength() {
            double residualVar = variance(residual.getValues());
            double[] combined = addArrays(trend.getValues(), residual.getValues());
            double combinedVar = variance(combined);

            if (combinedVar == 0) return 0.0;
            return Math.max(0, 1 - residualVar / combinedVar);
        }

        private static double[] addArrays(double[] a, double[] b) {
            int n = Math.min(a.length, b.length);
            double[] result = new double[n];
            for (int i = 0; i < n; i++) {
                result[i] = a[i] + b[i];
            }
            return result;
        }
    }

    // ==================== Decomposition Methods | 分解方法 ====================

    /**
     * Decomposes a time series using additive model.
     * 使用加法模型分解时间序列。
     *
     * @param series the time series - 时间序列
     * @param period the seasonal period - 季节周期
     * @return the decomposition result - 分解结果
     */
    public static DecompositionResult decompose(TimeSeries series, int period) {
        return decompose(series, period, DecompositionModel.ADDITIVE);
    }

    /**
     * Decomposes a time series.
     * 分解时间序列。
     *
     * @param series the time series - 时间序列
     * @param period the seasonal period - 季节周期
     * @param model the decomposition model - 分解模型
     * @return the decomposition result - 分解结果
     */
    public static DecompositionResult decompose(TimeSeries series, int period, DecompositionModel model) {
        if (series == null || series.size() < period * 2) {
            throw new IllegalArgumentException("Series must have at least 2 complete cycles for decomposition");
        }
        if (period < 2) {
            throw new IllegalArgumentException("Period must be at least 2");
        }

        List<DataPoint> points = series.getPoints();
        double[] values = series.getValues();
        Instant[] timestamps = series.getTimestamps();
        int n = values.length;

        // Step 1: Calculate trend using centered moving average
        double[] trendValues = calculateTrend(values, period);

        // Step 2: Calculate detrended series
        double[] detrended = new double[n];
        for (int i = 0; i < n; i++) {
            if (Double.isNaN(trendValues[i])) {
                detrended[i] = Double.NaN;
            } else {
                detrended[i] = switch (model) {
                    case ADDITIVE -> values[i] - trendValues[i];
                    case MULTIPLICATIVE -> values[i] / trendValues[i];
                };
            }
        }

        // Step 3: Calculate seasonal component
        double[] seasonalValues = calculateSeasonalComponent(detrended, period, model);

        // Step 4: Calculate residual
        double[] residualValues = new double[n];
        for (int i = 0; i < n; i++) {
            if (Double.isNaN(trendValues[i])) {
                residualValues[i] = Double.NaN;
            } else {
                residualValues[i] = switch (model) {
                    case ADDITIVE -> values[i] - trendValues[i] - seasonalValues[i];
                    case MULTIPLICATIVE -> {
                        double divisor = trendValues[i] * seasonalValues[i];
                        yield divisor == 0.0 ? Double.NaN : values[i] / divisor;
                    }
                };
            }
        }

        // Build result time series
        TimeSeries trend = new TimeSeries(series.getName() + "_trend");
        TimeSeries seasonal = new TimeSeries(series.getName() + "_seasonal");
        TimeSeries residual = new TimeSeries(series.getName() + "_residual");

        for (int i = 0; i < n; i++) {
            trend.add(timestamps[i], trendValues[i]);
            seasonal.add(timestamps[i], seasonalValues[i]);
            residual.add(timestamps[i], residualValues[i]);
        }

        return new DecompositionResult(series, trend, seasonal, residual, model, period);
    }

    /**
     * Decomposes using STL (Seasonal and Trend decomposition using Loess).
     * 使用 STL（基于 Loess 的季节性和趋势分解）进行分解。
     *
     * @param series the time series - 时间序列
     * @param period the seasonal period - 季节周期
     * @return the decomposition result - 分解结果
     */
    public static DecompositionResult stlDecompose(TimeSeries series, int period) {
        return stlDecompose(series, period, DEFAULT_STL_ITERATIONS);
    }

    /**
     * Decomposes using STL with custom iterations.
     * 使用自定义迭代次数的 STL 进行分解。
     *
     * @param series the time series - 时间序列
     * @param period the seasonal period - 季节周期
     * @param iterations number of STL iterations - STL 迭代次数
     * @return the decomposition result - 分解结果
     */
    public static DecompositionResult stlDecompose(TimeSeries series, int period, int iterations) {
        if (series == null || series.size() < period * 2) {
            throw new IllegalArgumentException("Series must have at least 2 complete cycles for decomposition");
        }

        double[] values = series.getValues();
        Instant[] timestamps = series.getTimestamps();
        int n = values.length;

        // Initialize components
        double[] seasonal = new double[n];
        double[] trend = new double[n];

        // STL inner loop
        for (int iter = 0; iter < iterations; iter++) {
            // Step 1: Detrend
            double[] detrended = new double[n];
            for (int i = 0; i < n; i++) {
                detrended[i] = values[i] - trend[i];
            }

            // Step 2: Cycle-subseries smoothing
            double[][] subseries = new double[period][];
            for (int j = 0; j < period; j++) {
                List<Double> subList = new ArrayList<>();
                for (int i = j; i < n; i += period) {
                    subList.add(detrended[i]);
                }
                subseries[j] = loessSmooth(listToArray(subList), Math.max(3, subList.size() / 2));
            }

            // Reconstruct seasonal
            for (int j = 0; j < period; j++) {
                int idx = 0;
                for (int i = j; i < n; i += period) {
                    seasonal[i] = subseries[j][idx++];
                }
            }

            // Center seasonal component
            double seasonalMean = mean(seasonal);
            for (int i = 0; i < n; i++) {
                seasonal[i] -= seasonalMean;
            }

            // Step 3: Deseasonalize
            double[] deseasonalized = new double[n];
            for (int i = 0; i < n; i++) {
                deseasonalized[i] = values[i] - seasonal[i];
            }

            // Step 4: Trend smoothing
            trend = loessSmooth(deseasonalized, Math.max(3, n / period));
        }

        // Calculate residual
        double[] residual = new double[n];
        for (int i = 0; i < n; i++) {
            residual[i] = values[i] - trend[i] - seasonal[i];
        }

        // Build result
        TimeSeries trendSeries = new TimeSeries(series.getName() + "_trend");
        TimeSeries seasonalSeries = new TimeSeries(series.getName() + "_seasonal");
        TimeSeries residualSeries = new TimeSeries(series.getName() + "_residual");

        for (int i = 0; i < n; i++) {
            trendSeries.add(timestamps[i], trend[i]);
            seasonalSeries.add(timestamps[i], seasonal[i]);
            residualSeries.add(timestamps[i], residual[i]);
        }

        return new DecompositionResult(series, trendSeries, seasonalSeries, residualSeries,
                DecompositionModel.ADDITIVE, period);
    }

    // ==================== Seasonality Detection | 季节性检测 ====================

    /**
     * Detects the seasonal period using autocorrelation.
     * 使用自相关检测季节周期。
     *
     * @param series the time series - 时间序列
     * @return the detected period or -1 if not found - 检测到的周期，未找到则返回 -1
     */
    public static int detectSeasonalPeriod(TimeSeries series) {
        return detectSeasonalPeriod(series, 2, series.size() / 2);
    }

    /**
     * Detects the seasonal period within a range.
     * 在范围内检测季节周期。
     *
     * @param series the time series - 时间序列
     * @param minPeriod minimum period to consider - 考虑的最小周期
     * @param maxPeriod maximum period to consider - 考虑的最大周期
     * @return the detected period or -1 if not found - 检测到的周期，未找到则返回 -1
     */
    public static int detectSeasonalPeriod(TimeSeries series, int minPeriod, int maxPeriod) {
        if (series == null || series.size() < minPeriod * 2) {
            return -1;
        }

        double[] values = series.getValues();
        int n = values.length;
        double mean = mean(values);

        // Calculate autocorrelation for different lags
        double bestCorrelation = 0.0;
        int bestPeriod = -1;

        for (int lag = minPeriod; lag <= Math.min(maxPeriod, n / 2); lag++) {
            double correlation = autocorrelation(values, mean, lag);

            // Look for significant peaks
            if (correlation > bestCorrelation && correlation > 0.5) {
                bestCorrelation = correlation;
                bestPeriod = lag;
            }
        }

        return bestPeriod;
    }

    /**
     * Calculates the seasonal indices.
     * 计算季节性指数。
     *
     * @param series the time series - 时间序列
     * @param period the seasonal period - 季节周期
     * @return array of seasonal indices - 季节性指数数组
     */
    public static double[] calculateSeasonalIndices(TimeSeries series, int period) {
        DecompositionResult result = decompose(series, period);
        double[] seasonal = result.seasonal().getValues();
        double[] indices = new double[period];

        int count = 0;
        for (int j = 0; j < period; j++) {
            double sum = 0;
            int cnt = 0;
            for (int i = j; i < seasonal.length; i += period) {
                if (!Double.isNaN(seasonal[i])) {
                    sum += seasonal[i];
                    cnt++;
                }
            }
            indices[j] = cnt > 0 ? sum / cnt : 0;
            count += cnt;
        }

        return indices;
    }

    // ==================== Private Helpers | 私有辅助方法 ====================

    private static double[] calculateTrend(double[] values, int period) {
        int n = values.length;
        double[] trend = new double[n];
        Arrays.fill(trend, Double.NaN);

        // Use centered moving average
        int halfPeriod = period / 2;
        boolean evenPeriod = (period % 2 == 0);

        for (int i = halfPeriod; i < n - halfPeriod; i++) {
            double sum = 0;
            int count = 0;

            if (evenPeriod) {
                // For even periods, use 2x period moving average
                for (int j = i - halfPeriod; j < i + halfPeriod; j++) {
                    sum += values[j];
                    count++;
                }
                // Add half weights at ends
                sum += values[i - halfPeriod] * 0.5;
                sum += values[i + halfPeriod] * 0.5;
                sum -= values[i - halfPeriod];
                trend[i] = sum / period;
            } else {
                for (int j = i - halfPeriod; j <= i + halfPeriod; j++) {
                    sum += values[j];
                    count++;
                }
                trend[i] = sum / count;
            }
        }

        // Extend trend to edges using linear extrapolation
        extendTrendEdges(trend, halfPeriod);

        return trend;
    }

    private static void extendTrendEdges(double[] trend, int halfPeriod) {
        int n = trend.length;

        // Find first valid value
        int firstValid = -1;
        for (int i = 0; i < n; i++) {
            if (!Double.isNaN(trend[i])) {
                firstValid = i;
                break;
            }
        }

        // Find last valid value
        int lastValid = -1;
        for (int i = n - 1; i >= 0; i--) {
            if (!Double.isNaN(trend[i])) {
                lastValid = i;
                break;
            }
        }

        if (firstValid < 0 || lastValid < 0 || firstValid == lastValid) {
            return;
        }

        // Calculate slope at start
        if (firstValid > 0 && firstValid + 1 < n && !Double.isNaN(trend[firstValid + 1])) {
            double slope = trend[firstValid + 1] - trend[firstValid];
            for (int i = firstValid - 1; i >= 0; i--) {
                trend[i] = trend[i + 1] - slope;
            }
        }

        // Calculate slope at end
        if (lastValid < n - 1 && lastValid - 1 >= 0 && !Double.isNaN(trend[lastValid - 1])) {
            double slope = trend[lastValid] - trend[lastValid - 1];
            for (int i = lastValid + 1; i < n; i++) {
                trend[i] = trend[i - 1] + slope;
            }
        }
    }

    private static double[] calculateSeasonalComponent(double[] detrended, int period, DecompositionModel model) {
        int n = detrended.length;
        double[] seasonal = new double[n];

        // Calculate average for each position in the period
        double[] periodAverages = new double[period];
        int[] counts = new int[period];

        for (int i = 0; i < n; i++) {
            if (!Double.isNaN(detrended[i])) {
                periodAverages[i % period] += detrended[i];
                counts[i % period]++;
            }
        }

        for (int j = 0; j < period; j++) {
            if (counts[j] > 0) {
                periodAverages[j] /= counts[j];
            }
        }

        // Center the seasonal component
        double avgSeasonal = mean(periodAverages);
        for (int j = 0; j < period; j++) {
            periodAverages[j] = switch (model) {
                case ADDITIVE -> periodAverages[j] - avgSeasonal;
                case MULTIPLICATIVE -> periodAverages[j] / avgSeasonal;
            };
        }

        // Apply to all positions
        for (int i = 0; i < n; i++) {
            seasonal[i] = periodAverages[i % period];
        }

        return seasonal;
    }

    private static double[] loessSmooth(double[] values, int bandwidth) {
        int n = values.length;
        double[] smoothed = new double[n];

        bandwidth = Math.min(bandwidth, n);

        for (int i = 0; i < n; i++) {
            // Define window
            int start = Math.max(0, i - bandwidth / 2);
            int end = Math.min(n, start + bandwidth);
            if (end - start < bandwidth) {
                start = Math.max(0, end - bandwidth);
            }

            // Calculate weighted average (simplified LOESS with tricube weights)
            double sumWeights = 0;
            double sumValues = 0;

            double maxDist = Math.max(i - start, end - 1 - i);
            if (maxDist == 0) maxDist = 1;

            for (int j = start; j < end; j++) {
                double dist = Math.abs(j - i) / maxDist;
                double weight = tricubeWeight(dist);
                sumWeights += weight;
                sumValues += weight * values[j];
            }

            smoothed[i] = sumWeights > 0 ? sumValues / sumWeights : values[i];
        }

        return smoothed;
    }

    private static double tricubeWeight(double u) {
        if (u >= 1) return 0;
        double t = 1 - u * u * u;
        return t * t * t;
    }

    private static double autocorrelation(double[] values, double mean, int lag) {
        int n = values.length;
        double numerator = 0;
        double denominator = 0;

        for (int i = 0; i < n; i++) {
            denominator += (values[i] - mean) * (values[i] - mean);
        }

        for (int i = 0; i < n - lag; i++) {
            numerator += (values[i] - mean) * (values[i + lag] - mean);
        }

        return denominator > 0 ? numerator / denominator : 0;
    }

    private static double mean(double[] values) {
        double sum = 0;
        int count = 0;
        for (double v : values) {
            if (!Double.isNaN(v)) {
                sum += v;
                count++;
            }
        }
        return count > 0 ? sum / count : 0;
    }

    private static double variance(double[] values) {
        double m = mean(values);
        double sumSq = 0;
        int count = 0;
        for (double v : values) {
            if (!Double.isNaN(v)) {
                sumSq += (v - m) * (v - m);
                count++;
            }
        }
        return count > 1 ? sumSq / (count - 1) : 0;
    }

    private static double[] listToArray(List<Double> list) {
        double[] arr = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }
}
