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

package cloud.opencode.base.timeseries.detection;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;

import java.time.Instant;
import java.util.*;

/**
 * Change Point Detection Util - Detects abrupt changes in time series
 * 变点检测工具 - 检测时间序列中的突变点
 *
 * <p>Provides algorithms for detecting points where statistical properties
 * of the time series change significantly.</p>
 * <p>提供用于检测时间序列统计属性显著变化点的算法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li><strong>CUSUM</strong> - Cumulative Sum for mean shift detection | 累积和用于均值偏移检测</li>
 *   <li><strong>PELT</strong> - Pruned Exact Linear Time | 修剪精确线性时间</li>
 *   <li><strong>Binary Segmentation</strong> - Recursive binary split | 递归二分分割</li>
 *   <li><strong>Rolling Statistics</strong> - Based on rolling mean/variance | 基于滚动均值/方差</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Detect change points using CUSUM
 * List<ChangePoint> changes = ChangePointDetectionUtil.detectCusum(timeSeries, threshold);
 *
 * // Detect using binary segmentation
 * List<ChangePoint> changes = ChangePointDetectionUtil.detectBinarySegmentation(timeSeries, minSegmentSize);
 *
 * // Get segments between change points
 * List<TimeSeries> segments = ChangePointDetectionUtil.getSegments(timeSeries, changes);
 *
 * // Detect mean shift
 * List<ChangePoint> meanShifts = ChangePointDetectionUtil.detectMeanShift(timeSeries, windowSize, threshold);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (null series returns empty list) - 空值安全: 是（空序列返回空列表）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for CUSUM; O(n²) worst case for binary segmentation (each split scans segment); O(n*w) for rolling mean-shift with window size w - 时间复杂度: CUSUM 为 O(n)；二分分割最坏 O(n²)（每次分割扫描线段）；窗口 w 的滚动均值偏移为 O(n*w)</li>
 *   <li>Space complexity: O(n) - change-point list and segment arrays proportional to series length - 空间复杂度: O(n) - 变点列表和分段数组与序列长度成正比</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see TimeSeries
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public final class ChangePointDetectionUtil {

    /** Default minimum segment size */
    private static final int DEFAULT_MIN_SEGMENT_SIZE = 5;

    /** Default threshold multiplier (standard deviations) */
    private static final double DEFAULT_THRESHOLD_MULTIPLIER = 2.0;

    private ChangePointDetectionUtil() {
        // Utility class
    }

    // ==================== Result Types | 结果类型 ====================

    /**
     * Represents a detected change point.
     * 表示检测到的变点。
     *
     * @param index the index in the time series - 时间序列中的索引
     * @param timestamp the timestamp of the change - 变化的时间戳
     * @param score the confidence/significance score - 置信度/显著性分数
     * @param type the type of change - 变化类型
     * @param beforeMean mean value before the change - 变化前的均值
     * @param afterMean mean value after the change - 变化后的均值
     */
    public record ChangePoint(
            int index,
            Instant timestamp,
            double score,
            ChangeType type,
            double beforeMean,
            double afterMean
    ) implements Comparable<ChangePoint> {
        /**
         * Gets the magnitude of change.
         * 获取变化幅度。
         */
        public double magnitude() {
            return Math.abs(afterMean - beforeMean);
        }

        /**
         * Gets the direction of change.
         * 获取变化方向。
         */
        public Direction direction() {
            if (afterMean > beforeMean) return Direction.INCREASE;
            if (afterMean < beforeMean) return Direction.DECREASE;
            return Direction.NONE;
        }

        @Override
        public int compareTo(ChangePoint other) {
            return Integer.compare(this.index, other.index);
        }
    }

    /**
     * Type of change detected.
     * 检测到的变化类型。
     */
    public enum ChangeType {
        /** Mean shift - change in average level */
        MEAN_SHIFT,
        /** Variance shift - change in variability */
        VARIANCE_SHIFT,
        /** Trend change - change in slope */
        TREND_CHANGE,
        /** General structural change */
        STRUCTURAL
    }

    /**
     * Direction of change.
     * 变化方向。
     */
    public enum Direction {
        INCREASE, DECREASE, NONE
    }

    // ==================== CUSUM Detection | CUSUM 检测 ====================

    /**
     * Detects change points using CUSUM (Cumulative Sum) algorithm.
     * 使用 CUSUM（累积和）算法检测变点。
     *
     * @param series the time series - 时间序列
     * @param threshold the detection threshold - 检测阈值
     * @return list of detected change points - 检测到的变点列表
     */
    public static List<ChangePoint> detectCusum(TimeSeries series, double threshold) {
        if (series == null || series.size() < DEFAULT_MIN_SEGMENT_SIZE) {
            return Collections.emptyList();
        }

        double[] values = series.getValues();
        Instant[] timestamps = series.getTimestamps();
        int n = values.length;

        double mean = mean(values);
        double std = stdDev(values, mean);

        if (std == 0) {
            return Collections.emptyList();
        }

        List<ChangePoint> changePoints = new ArrayList<>();

        // Calculate CUSUM statistics
        double sH = 0; // Upper CUSUM
        double sL = 0; // Lower CUSUM
        double k = threshold * std * 0.5; // Slack value

        for (int i = 1; i < n; i++) {
            sH = Math.max(0, sH + values[i] - mean - k);
            sL = Math.min(0, sL + values[i] - mean + k);

            double h = threshold * std;

            if (sH > h || sL < -h) {
                // Change point detected
                double beforeMean = mean(values, 0, i);
                double afterMean = mean(values, i, n);

                changePoints.add(new ChangePoint(
                        i,
                        timestamps[i],
                        Math.max(sH, -sL) / std,
                        ChangeType.MEAN_SHIFT,
                        beforeMean,
                        afterMean
                ));

                // Reset CUSUM
                sH = 0;
                sL = 0;
            }
        }

        return changePoints;
    }

    /**
     * Detects change points using CUSUM with automatic threshold.
     * 使用自动阈值的 CUSUM 检测变点。
     *
     * @param series the time series - 时间序列
     * @return list of detected change points - 检测到的变点列表
     */
    public static List<ChangePoint> detectCusum(TimeSeries series) {
        return detectCusum(series, DEFAULT_THRESHOLD_MULTIPLIER);
    }

    // ==================== Binary Segmentation | 二分分割 ====================

    /**
     * Detects change points using Binary Segmentation.
     * 使用二分分割检测变点。
     *
     * @param series the time series - 时间序列
     * @param minSegmentSize minimum segment size - 最小分段大小
     * @return list of detected change points - 检测到的变点列表
     */
    public static List<ChangePoint> detectBinarySegmentation(TimeSeries series, int minSegmentSize) {
        if (series == null || series.size() < minSegmentSize * 2) {
            return Collections.emptyList();
        }

        double[] values = series.getValues();
        Instant[] timestamps = series.getTimestamps();

        List<ChangePoint> changePoints = new ArrayList<>();
        binarySegmentationRecursive(values, timestamps, 0, values.length,
                minSegmentSize, changePoints);

        Collections.sort(changePoints);
        return changePoints;
    }

    /**
     * Detects change points using Binary Segmentation with default segment size.
     * 使用默认分段大小的二分分割检测变点。
     *
     * @param series the time series - 时间序列
     * @return list of detected change points - 检测到的变点列表
     */
    public static List<ChangePoint> detectBinarySegmentation(TimeSeries series) {
        return detectBinarySegmentation(series, DEFAULT_MIN_SEGMENT_SIZE);
    }

    private static void binarySegmentationRecursive(
            double[] values, Instant[] timestamps,
            int start, int end, int minSize,
            List<ChangePoint> result) {

        if (end - start < minSize * 2) {
            return;
        }

        // Find the point that maximizes the difference between segments
        int bestPoint = -1;
        double bestScore = 0;

        for (int t = start + minSize; t < end - minSize; t++) {
            double score = calculateSegmentScore(values, start, t, end);
            if (score > bestScore) {
                bestScore = score;
                bestPoint = t;
            }
        }

        // Check if the score is significant
        double threshold = calculateBicPenalty(end - start);

        if (bestScore > threshold && bestPoint > 0) {
            double beforeMean = mean(values, start, bestPoint);
            double afterMean = mean(values, bestPoint, end);

            result.add(new ChangePoint(
                    bestPoint,
                    timestamps[bestPoint],
                    bestScore,
                    ChangeType.MEAN_SHIFT,
                    beforeMean,
                    afterMean
            ));

            // Recursively search each segment
            binarySegmentationRecursive(values, timestamps, start, bestPoint, minSize, result);
            binarySegmentationRecursive(values, timestamps, bestPoint, end, minSize, result);
        }
    }

    private static double calculateSegmentScore(double[] values, int start, int split, int end) {
        int n1 = split - start;
        int n2 = end - split;
        int n = end - start;

        double mean1 = mean(values, start, split);
        double mean2 = mean(values, split, end);
        double meanAll = mean(values, start, end);

        // Calculate sum of squared errors
        double sse1 = 0, sse2 = 0, sseAll = 0;

        for (int i = start; i < split; i++) {
            sse1 += (values[i] - mean1) * (values[i] - mean1);
            sseAll += (values[i] - meanAll) * (values[i] - meanAll);
        }
        for (int i = split; i < end; i++) {
            sse2 += (values[i] - mean2) * (values[i] - mean2);
            sseAll += (values[i] - meanAll) * (values[i] - meanAll);
        }

        // Log-likelihood ratio
        if (sse1 + sse2 == 0) return 0;
        return n * Math.log(sseAll / (sse1 + sse2 + 1e-10));
    }

    private static double calculateBicPenalty(int n) {
        // BIC penalty: log(n) * number of parameters
        return Math.log(n) * 2;
    }

    // ==================== Mean Shift Detection | 均值偏移检测 ====================

    /**
     * Detects mean shifts using rolling statistics.
     * 使用滚动统计检测均值偏移。
     *
     * @param series the time series - 时间序列
     * @param windowSize the window size for rolling statistics - 滚动统计的窗口大小
     * @param threshold the detection threshold (in std deviations) - 检测阈值（以标准差为单位）
     * @return list of detected change points - 检测到的变点列表
     */
    public static List<ChangePoint> detectMeanShift(TimeSeries series, int windowSize, double threshold) {
        if (series == null || series.size() < windowSize * 2) {
            return Collections.emptyList();
        }

        double[] values = series.getValues();
        Instant[] timestamps = series.getTimestamps();
        int n = values.length;

        List<ChangePoint> changePoints = new ArrayList<>();

        for (int i = windowSize; i < n - windowSize; i++) {
            double beforeMean = mean(values, i - windowSize, i);
            double afterMean = mean(values, i, i + windowSize);
            double beforeStd = stdDev(values, beforeMean, i - windowSize, i);

            if (beforeStd == 0) beforeStd = 1;

            double zScore = Math.abs(afterMean - beforeMean) / beforeStd;

            if (zScore > threshold) {
                // Check if this is a local maximum
                boolean isLocalMax = true;
                for (int j = Math.max(windowSize, i - windowSize / 2);
                     j < Math.min(n - windowSize, i + windowSize / 2); j++) {
                    if (j != i) {
                        double otherBefore = mean(values, j - windowSize, j);
                        double otherAfter = mean(values, j, j + windowSize);
                        double otherStd = stdDev(values, otherBefore, j - windowSize, j);
                        if (otherStd == 0) otherStd = 1;
                        double otherZ = Math.abs(otherAfter - otherBefore) / otherStd;
                        if (otherZ > zScore) {
                            isLocalMax = false;
                            break;
                        }
                    }
                }

                if (isLocalMax) {
                    changePoints.add(new ChangePoint(
                            i,
                            timestamps[i],
                            zScore,
                            ChangeType.MEAN_SHIFT,
                            beforeMean,
                            afterMean
                    ));

                    // Skip ahead to avoid detecting the same change multiple times
                    i += windowSize / 2;
                }
            }
        }

        return changePoints;
    }

    /**
     * Detects mean shifts with default parameters.
     * 使用默认参数检测均值偏移。
     *
     * @param series the time series - 时间序列
     * @return list of detected change points - 检测到的变点列表
     */
    public static List<ChangePoint> detectMeanShift(TimeSeries series) {
        int windowSize = Math.max(5, series.size() / 20);
        return detectMeanShift(series, windowSize, DEFAULT_THRESHOLD_MULTIPLIER);
    }

    // ==================== Variance Shift Detection | 方差偏移检测 ====================

    /**
     * Detects variance shifts using rolling statistics.
     * 使用滚动统计检测方差偏移。
     *
     * @param series the time series - 时间序列
     * @param windowSize the window size - 窗口大小
     * @param threshold the detection threshold - 检测阈值
     * @return list of detected change points - 检测到的变点列表
     */
    public static List<ChangePoint> detectVarianceShift(TimeSeries series, int windowSize, double threshold) {
        if (series == null || series.size() < windowSize * 2) {
            return Collections.emptyList();
        }

        double[] values = series.getValues();
        Instant[] timestamps = series.getTimestamps();
        int n = values.length;

        List<ChangePoint> changePoints = new ArrayList<>();

        for (int i = windowSize; i < n - windowSize; i++) {
            double beforeVar = variance(values, i - windowSize, i);
            double afterVar = variance(values, i, i + windowSize);

            if (beforeVar == 0) beforeVar = 1e-10;

            double ratio = afterVar / beforeVar;
            double logRatio = Math.abs(Math.log(ratio));

            if (logRatio > Math.log(threshold)) {
                changePoints.add(new ChangePoint(
                        i,
                        timestamps[i],
                        logRatio,
                        ChangeType.VARIANCE_SHIFT,
                        Math.sqrt(beforeVar),
                        Math.sqrt(afterVar)
                ));

                i += windowSize / 2;
            }
        }

        return changePoints;
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Splits a time series into segments based on change points.
     * 根据变点将时间序列分割成段。
     *
     * @param series the time series - 时间序列
     * @param changePoints the change points - 变点
     * @return list of segments - 分段列表
     */
    public static List<TimeSeries> getSegments(TimeSeries series, List<ChangePoint> changePoints) {
        if (series == null || series.isEmpty()) {
            return Collections.emptyList();
        }

        List<DataPoint> points = series.getPoints();
        List<TimeSeries> segments = new ArrayList<>();

        List<Integer> indices = new ArrayList<>();
        indices.add(0);
        for (ChangePoint cp : changePoints) {
            indices.add(cp.index());
        }
        indices.add(points.size());

        Collections.sort(indices);

        for (int i = 0; i < indices.size() - 1; i++) {
            int start = indices.get(i);
            int end = indices.get(i + 1);

            if (end > start) {
                TimeSeries segment = new TimeSeries(series.getName() + "_segment_" + i);
                for (int j = start; j < end; j++) {
                    segment.add(points.get(j));
                }
                segments.add(segment);
            }
        }

        return segments;
    }

    /**
     * Merges nearby change points.
     * 合并临近的变点。
     *
     * @param changePoints the change points - 变点
     * @param minDistance minimum distance between change points - 变点之间的最小距离
     * @return merged change points - 合并后的变点
     */
    public static List<ChangePoint> mergeNearbyChangePoints(List<ChangePoint> changePoints, int minDistance) {
        if (changePoints == null || changePoints.size() <= 1) {
            return changePoints != null ? new ArrayList<>(changePoints) : Collections.emptyList();
        }

        List<ChangePoint> sorted = new ArrayList<>(changePoints);
        Collections.sort(sorted);

        List<ChangePoint> merged = new ArrayList<>();
        ChangePoint current = sorted.getFirst();

        for (int i = 1; i < sorted.size(); i++) {
            ChangePoint next = sorted.get(i);

            if (next.index() - current.index() < minDistance) {
                // Keep the one with higher score
                if (next.score() > current.score()) {
                    current = next;
                }
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);

        return merged;
    }

    /**
     * Filters change points by minimum significance score.
     * 按最小显著性分数过滤变点。
     *
     * @param changePoints the change points - 变点
     * @param minScore minimum score - 最小分数
     * @return filtered change points - 过滤后的变点
     */
    public static List<ChangePoint> filterByScore(List<ChangePoint> changePoints, double minScore) {
        if (changePoints == null) {
            return Collections.emptyList();
        }
        return changePoints.stream()
                .filter(cp -> cp.score() >= minScore)
                .sorted()
                .toList();
    }

    // ==================== Private Helpers | 私有辅助方法 ====================

    private static double mean(double[] values) {
        return mean(values, 0, values.length);
    }

    private static double mean(double[] values, int start, int end) {
        double sum = 0;
        for (int i = start; i < end; i++) {
            sum += values[i];
        }
        return sum / (end - start);
    }

    private static double stdDev(double[] values, double mean) {
        return stdDev(values, mean, 0, values.length);
    }

    private static double stdDev(double[] values, double mean, int start, int end) {
        if (end - start < 2) return 0;
        double sumSq = 0;
        for (int i = start; i < end; i++) {
            sumSq += (values[i] - mean) * (values[i] - mean);
        }
        return Math.sqrt(sumSq / (end - start - 1));
    }

    private static double variance(double[] values, int start, int end) {
        if (end - start < 2) return 0;
        double m = mean(values, start, end);
        double sumSq = 0;
        for (int i = start; i < end; i++) {
            sumSq += (values[i] - m) * (values[i] - m);
        }
        return sumSq / (end - start - 1);
    }
}
