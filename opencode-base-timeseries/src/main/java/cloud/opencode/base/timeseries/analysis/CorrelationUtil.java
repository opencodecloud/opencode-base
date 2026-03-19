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

package cloud.opencode.base.timeseries.analysis;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;

/**
 * CorrelationUtil - Time Series Correlation Analysis
 * CorrelationUtil - 时间序列相关性分析
 *
 * <p>Provides correlation analysis tools for time series data including
 * Pearson correlation, cross-correlation, autocorrelation, and lag analysis.</p>
 * <p>为时间序列数据提供相关性分析工具，包括皮尔逊相关、互相关、自相关和滞后分析。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pearson correlation coefficient - 皮尔逊相关系数</li>
 *   <li>Spearman rank correlation - 斯皮尔曼等级相关</li>
 *   <li>Cross-correlation - 互相关</li>
 *   <li>Autocorrelation (ACF) - 自相关函数</li>
 *   <li>Partial autocorrelation (PACF) - 偏自相关函数</li>
 *   <li>Lag analysis - 滞后分析</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Pearson correlation
 * double corr = CorrelationUtil.pearson(series1, series2);
 *
 * // Cross-correlation with lag
 * CrossCorrelationResult ccf = CorrelationUtil.crossCorrelation(series1, series2, 20);
 * int bestLag = ccf.bestLag();
 * double bestCorr = ccf.bestCorrelation();
 *
 * // Autocorrelation
 * double[] acf = CorrelationUtil.autocorrelation(series, 20);
 *
 * // Find optimal lag
 * int optimalLag = CorrelationUtil.findOptimalLag(series1, series2, 30);
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
 *   <li>Time complexity: O(n) for pearson/spearman; O(n*L) for cross-correlation with max lag L; O(L²) for PACF via Durbin-Levinson - 时间复杂度: pearson/spearman 为 O(n)；最大滞后 L 的互相关为 O(n*L)；Durbin-Levinson PACF 为 O(L²)</li>
 *   <li>Space complexity: O(n) - aligned value arrays and correlation result arrays proportional to series length - 空间复杂度: O(n) - 对齐值数组和相关结果数组与序列长度成正比</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public final class CorrelationUtil {

    private CorrelationUtil() {
    }

    // ==================== Pearson Correlation | 皮尔逊相关 ====================

    /**
     * Calculate Pearson correlation coefficient between two series.
     * 计算两个序列之间的皮尔逊相关系数。
     *
     * @param series1 first time series | 第一个时间序列
     * @param series2 second time series | 第二个时间序列
     * @return correlation coefficient (-1 to 1) | 相关系数 (-1 到 1)
     */
    public static double pearson(TimeSeries series1, TimeSeries series2) {
        double[] x = extractAlignedValues(series1, series2)[0];
        double[] y = extractAlignedValues(series1, series2)[1];
        return pearson(x, y);
    }

    /**
     * Calculate Pearson correlation coefficient between two arrays.
     * 计算两个数组之间的皮尔逊相关系数。
     *
     * @param x first array | 第一个数组
     * @param y second array | 第二个数组
     * @return correlation coefficient | 相关系数
     */
    public static double pearson(double[] x, double[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }
        if (x.length < 2) {
            throw new IllegalArgumentException("Arrays must have at least 2 elements");
        }

        int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
            sumY2 += y[i] * y[i];
        }

        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));

        if (denominator == 0) {
            return 0; // No variance
        }

        return numerator / denominator;
    }

    // ==================== Spearman Correlation | 斯皮尔曼相关 ====================

    /**
     * Calculate Spearman rank correlation coefficient.
     * 计算斯皮尔曼等级相关系数。
     *
     * @param series1 first time series | 第一个时间序列
     * @param series2 second time series | 第二个时间序列
     * @return correlation coefficient | 相关系数
     */
    public static double spearman(TimeSeries series1, TimeSeries series2) {
        double[] x = extractAlignedValues(series1, series2)[0];
        double[] y = extractAlignedValues(series1, series2)[1];
        return spearman(x, y);
    }

    /**
     * Calculate Spearman rank correlation coefficient.
     * 计算斯皮尔曼等级相关系数。
     *
     * @param x first array | 第一个数组
     * @param y second array | 第二个数组
     * @return correlation coefficient | 相关系数
     */
    public static double spearman(double[] x, double[] y) {
        double[] rankX = rank(x);
        double[] rankY = rank(y);
        return pearson(rankX, rankY);
    }

    // ==================== Cross-Correlation | 互相关 ====================

    /**
     * Calculate cross-correlation between two series at different lags.
     * 计算两个序列在不同滞后下的互相关。
     *
     * @param series1 first time series | 第一个时间序列
     * @param series2 second time series | 第二个时间序列
     * @param maxLag  maximum lag to compute | 要计算的最大滞后
     * @return cross-correlation result | 互相关结果
     */
    public static CrossCorrelationResult crossCorrelation(TimeSeries series1, TimeSeries series2, int maxLag) {
        double[] x = extractAlignedValues(series1, series2)[0];
        double[] y = extractAlignedValues(series1, series2)[1];
        return crossCorrelation(x, y, maxLag);
    }

    /**
     * Calculate cross-correlation between two arrays at different lags.
     * 计算两个数组在不同滞后下的互相关。
     *
     * @param x      first array | 第一个数组
     * @param y      second array | 第二个数组
     * @param maxLag maximum lag to compute | 要计算的最大滞后
     * @return cross-correlation result | 互相关结果
     */
    public static CrossCorrelationResult crossCorrelation(double[] x, double[] y, int maxLag) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }

        int n = x.length;
        double[] correlations = new double[2 * maxLag + 1];
        int[] lags = new int[2 * maxLag + 1];

        // Normalize arrays
        double[] xNorm = normalize(x);
        double[] yNorm = normalize(y);

        int bestLag = 0;
        double bestCorr = 0;

        for (int lag = -maxLag; lag <= maxLag; lag++) {
            double corr = correlationAtLag(xNorm, yNorm, lag);
            int idx = lag + maxLag;
            correlations[idx] = corr;
            lags[idx] = lag;

            if (Math.abs(corr) > Math.abs(bestCorr)) {
                bestCorr = corr;
                bestLag = lag;
            }
        }

        return new CrossCorrelationResult(lags, correlations, bestLag, bestCorr);
    }

    // ==================== Autocorrelation | 自相关 ====================

    /**
     * Calculate autocorrelation function (ACF).
     * 计算自相关函数 (ACF)。
     *
     * @param series the time series | 时间序列
     * @param maxLag maximum lag to compute | 要计算的最大滞后
     * @return autocorrelation values for lags 0 to maxLag | 滞后 0 到 maxLag 的自相关值
     */
    public static double[] autocorrelation(TimeSeries series, int maxLag) {
        double[] values = series.getPoints().stream().mapToDouble(DataPoint::value).toArray();
        return autocorrelation(values, maxLag);
    }

    /**
     * Calculate autocorrelation function (ACF).
     * 计算自相关函数 (ACF)。
     *
     * @param values the values | 值
     * @param maxLag maximum lag | 最大滞后
     * @return autocorrelation values | 自相关值
     */
    public static double[] autocorrelation(double[] values, int maxLag) {
        double[] acf = new double[maxLag + 1];
        double[] norm = normalize(values);

        for (int lag = 0; lag <= maxLag; lag++) {
            acf[lag] = correlationAtLag(norm, norm, lag);
        }

        return acf;
    }

    /**
     * Calculate partial autocorrelation function (PACF).
     * 计算偏自相关函数 (PACF)。
     *
     * @param series the time series | 时间序列
     * @param maxLag maximum lag | 最大滞后
     * @return partial autocorrelation values | 偏自相关值
     */
    public static double[] partialAutocorrelation(TimeSeries series, int maxLag) {
        double[] values = series.getPoints().stream().mapToDouble(DataPoint::value).toArray();
        return partialAutocorrelation(values, maxLag);
    }

    /**
     * Calculate partial autocorrelation function (PACF) using Durbin-Levinson recursion.
     * 使用 Durbin-Levinson 递归计算偏自相关函数 (PACF)。
     *
     * @param values the values | 值
     * @param maxLag maximum lag | 最大滞后
     * @return partial autocorrelation values | 偏自相关值
     */
    public static double[] partialAutocorrelation(double[] values, int maxLag) {
        double[] acf = autocorrelation(values, maxLag);
        double[] pacf = new double[maxLag + 1];
        pacf[0] = 1.0;

        if (maxLag == 0) return pacf;

        // Durbin-Levinson algorithm
        double[][] phi = new double[maxLag + 1][maxLag + 1];
        phi[1][1] = acf[1];
        pacf[1] = acf[1];

        for (int k = 2; k <= maxLag; k++) {
            double num = acf[k];
            double den = 1.0;

            for (int j = 1; j < k; j++) {
                num -= phi[k - 1][j] * acf[k - j];
                den -= phi[k - 1][j] * acf[j];
            }

            if (Math.abs(den) < 1e-10) {
                pacf[k] = 0;
            } else {
                phi[k][k] = num / den;
                pacf[k] = phi[k][k];
            }

            for (int j = 1; j < k; j++) {
                phi[k][j] = phi[k - 1][j] - phi[k][k] * phi[k - 1][k - j];
            }
        }

        return pacf;
    }

    // ==================== Lag Analysis | 滞后分析 ====================

    /**
     * Find the optimal lag between two series.
     * 找到两个序列之间的最佳滞后。
     *
     * @param series1 first series | 第一个序列
     * @param series2 second series | 第二个序列
     * @param maxLag  maximum lag to search | 搜索的最大滞后
     * @return optimal lag | 最佳滞后
     */
    public static int findOptimalLag(TimeSeries series1, TimeSeries series2, int maxLag) {
        return crossCorrelation(series1, series2, maxLag).bestLag();
    }

    /**
     * Calculate correlation with time lag applied.
     * 计算应用时间滞后的相关性。
     *
     * @param series1 first series | 第一个序列
     * @param series2 second series | 第二个序列
     * @param lag     lag to apply to series2 | 应用于 series2 的滞后
     * @return correlation at lag | 滞后时的相关性
     */
    public static double correlationAtLag(TimeSeries series1, TimeSeries series2, int lag) {
        double[] x = extractAlignedValues(series1, series2)[0];
        double[] y = extractAlignedValues(series1, series2)[1];
        return correlationAtLag(normalize(x), normalize(y), lag);
    }

    // ==================== Rolling Correlation | 滚动相关 ====================

    /**
     * Calculate rolling correlation between two series.
     * 计算两个序列之间的滚动相关。
     *
     * @param series1    first series | 第一个序列
     * @param series2    second series | 第二个序列
     * @param windowSize rolling window size | 滚动窗口大小
     * @return array of rolling correlations | 滚动相关数组
     */
    public static double[] rollingCorrelation(TimeSeries series1, TimeSeries series2, int windowSize) {
        double[] x = extractAlignedValues(series1, series2)[0];
        double[] y = extractAlignedValues(series1, series2)[1];
        return rollingCorrelation(x, y, windowSize);
    }

    /**
     * Calculate rolling correlation between two arrays.
     * 计算两个数组之间的滚动相关。
     *
     * @param x          first array | 第一个数组
     * @param y          second array | 第二个数组
     * @param windowSize rolling window size | 滚动窗口大小
     * @return array of rolling correlations | 滚动相关数组
     */
    public static double[] rollingCorrelation(double[] x, double[] y, int windowSize) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }
        if (windowSize < 2) {
            throw new IllegalArgumentException("Window size must be at least 2");
        }

        int n = x.length;
        int resultSize = n - windowSize + 1;
        double[] result = new double[resultSize];

        for (int i = 0; i < resultSize; i++) {
            double[] windowX = Arrays.copyOfRange(x, i, i + windowSize);
            double[] windowY = Arrays.copyOfRange(y, i, i + windowSize);
            result[i] = pearson(windowX, windowY);
        }

        return result;
    }

    // ==================== Helper Methods | 辅助方法 ====================

    private static double[][] extractAlignedValues(TimeSeries s1, TimeSeries s2) {
        List<DataPoint> points1 = s1.getPoints();
        List<DataPoint> points2 = s2.getPoints();

        // Build timestamp to value map for series 2
        Map<Long, Double> map2 = new HashMap<>();
        for (DataPoint p : points2) {
            map2.put(p.epochMillis(), p.value());
        }

        // Extract aligned values
        List<Double> values1 = new ArrayList<>();
        List<Double> values2 = new ArrayList<>();

        for (DataPoint p : points1) {
            Double v2 = map2.get(p.epochMillis());
            if (v2 != null) {
                values1.add(p.value());
                values2.add(v2);
            }
        }

        return new double[][]{
                values1.stream().mapToDouble(Double::doubleValue).toArray(),
                values2.stream().mapToDouble(Double::doubleValue).toArray()
        };
    }

    private static double[] normalize(double[] values) {
        double mean = Arrays.stream(values).average().orElse(0);
        double std = standardDeviation(values);

        if (std == 0) {
            return new double[values.length]; // Return zeros if no variance
        }

        return Arrays.stream(values).map(v -> (v - mean) / std).toArray();
    }

    private static double standardDeviation(double[] values) {
        if (values.length == 0) {
            return 0.0;
        }
        double mean = Arrays.stream(values).average().orElse(0);
        double sumSq = Arrays.stream(values).map(v -> (v - mean) * (v - mean)).sum();
        return Math.sqrt(sumSq / values.length);
    }

    private static double correlationAtLag(double[] x, double[] y, int lag) {
        int n = x.length;
        int effectiveN = n - Math.abs(lag);

        if (effectiveN < 2) {
            return 0;
        }

        double sum = 0;
        int startX = lag >= 0 ? 0 : -lag;
        int startY = lag >= 0 ? lag : 0;

        for (int i = 0; i < effectiveN; i++) {
            sum += x[startX + i] * y[startY + i];
        }

        return sum / effectiveN;
    }

    private static double[] rank(double[] values) {
        int n = values.length;
        Integer[] indices = IntStream.range(0, n).boxed().toArray(Integer[]::new);
        Arrays.sort(indices, (i, j) -> Double.compare(values[i], values[j]));

        double[] ranks = new double[n];
        for (int i = 0; i < n; i++) {
            ranks[indices[i]] = i + 1;
        }

        return ranks;
    }

    // ==================== Result Classes | 结果类 ====================

    /**
     * Cross-correlation result.
     * 互相关结果。
     */
    public record CrossCorrelationResult(
            int[] lags,
            double[] correlations,
            int bestLag,
            double bestCorrelation
    ) {
        /**
         * Get correlation at specific lag.
         * 获取特定滞后的相关性。
         */
        public double correlationAt(int lag) {
            int idx = lag + (lags.length / 2);
            if (idx < 0 || idx >= correlations.length) {
                throw new IllegalArgumentException("Lag out of range");
            }
            return correlations[idx];
        }

        /**
         * Check if correlation is statistically significant at 95% confidence.
         * 检查相关性在 95% 置信度下是否具有统计显著性。
         */
        public boolean isSignificant(int sampleSize) {
            double threshold = 1.96 / Math.sqrt(sampleSize);
            return Math.abs(bestCorrelation) > threshold;
        }
    }
}
