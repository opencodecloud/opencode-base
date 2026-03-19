package cloud.opencode.base.timeseries.sampling;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Downsampling Util
 * 降采样工具类
 *
 * <p>Advanced downsampling strategies for time series data.</p>
 * <p>时间序列数据的高级降采样策略。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>LTTB (Largest Triangle Three Buckets) | 最大三角形三桶算法</li>
 *   <li>M4 (Min-Max aggregation) | 最小-最大聚合</li>
 *   <li>Percentile-based | 基于百分位</li>
 *   <li>Variance-based | 基于方差</li>
 *   <li>Peak preservation | 峰值保持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // LTTB downsampling (preserves visual shape)
 * TimeSeries downsampled = DownsamplingUtil.lttb(series, 100);
 *
 * // M4 downsampling (preserves min/max in each bucket)
 * TimeSeries downsampled = DownsamplingUtil.m4(series, Duration.ofMinutes(5));
 *
 * // Peak preservation
 * TimeSeries downsampled = DownsamplingUtil.peakPreserving(series, 50);
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
 *   <li>Time complexity: O(n) for LTTB, M4, threshold; O(n log n) for percentile due to per-bucket sort - 时间复杂度: LTTB、M4、阈值法均为 O(n)；百分位法因桶内排序为 O(n log n)</li>
 *   <li>Space complexity: O(t) where t is the target output size - result series proportional to target point count - 空间复杂度: O(t)，t 为目标输出大小 - 结果序列与目标点数成正比</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public final class DownsamplingUtil {

    private DownsamplingUtil() {
        // Utility class
    }

    // ==================== LTTB Algorithm | LTTB 算法 ====================

    /**
     * Downsample using LTTB (Largest Triangle Three Buckets)
     * 使用LTTB算法（最大三角形三桶）进行降采样
     *
     * <p>This algorithm preserves the visual shape of the time series by selecting
     * points that maximize the triangle area with adjacent buckets.</p>
     * <p>此算法通过选择与相邻桶形成最大三角形面积的点来保持时间序列的视觉形状。</p>
     *
     * <p><strong>Time Complexity | 时间复杂度:</strong> O(n)</p>
     *
     * @param series the time series | 时间序列
     * @param targetSize the target number of points | 目标点数
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries lttb(TimeSeries series, int targetSize) {
        List<DataPoint> points = series.getPoints();

        if (targetSize >= points.size() || targetSize < 3) {
            return new TimeSeries(series.getName() + "_lttb", points);
        }

        List<DataPoint> result = new ArrayList<>(targetSize);

        // Always include first point
        result.add(points.getFirst());

        // Bucket size
        double bucketSize = (double) (points.size() - 2) / (targetSize - 2);

        int lastSelectedIndex = 0;

        for (int i = 0; i < targetSize - 2; i++) {
            // Calculate bucket boundaries
            int bucketStart = (int) Math.floor((i) * bucketSize) + 1;
            int bucketEnd = (int) Math.floor((i + 1) * bucketSize) + 1;
            bucketEnd = Math.min(bucketEnd, points.size() - 1);

            // Calculate next bucket average
            int nextBucketStart = (int) Math.floor((i + 1) * bucketSize) + 1;
            int nextBucketEnd = (int) Math.floor((i + 2) * bucketSize) + 1;
            nextBucketEnd = Math.min(nextBucketEnd, points.size());

            double avgX = 0, avgY = 0;
            int nextBucketCount = nextBucketEnd - nextBucketStart;
            if (nextBucketCount > 0) {
                for (int j = nextBucketStart; j < nextBucketEnd; j++) {
                    avgX += j;
                    avgY += points.get(j).value();
                }
                avgX /= nextBucketCount;
                avgY /= nextBucketCount;
            }

            // Find point with largest triangle area
            double maxArea = -1;
            int maxAreaIndex = bucketStart;

            DataPoint pointA = points.get(lastSelectedIndex);
            double aX = lastSelectedIndex;
            double aY = pointA.value();

            for (int j = bucketStart; j < bucketEnd; j++) {
                double bX = j;
                double bY = points.get(j).value();

                // Calculate triangle area
                double area = Math.abs((aX - avgX) * (bY - aY) - (aX - bX) * (avgY - aY)) / 2;

                if (area > maxArea) {
                    maxArea = area;
                    maxAreaIndex = j;
                }
            }

            result.add(points.get(maxAreaIndex));
            lastSelectedIndex = maxAreaIndex;
        }

        // Always include last point
        result.add(points.getLast());

        return new TimeSeries(series.getName() + "_lttb", result);
    }

    // ==================== M4 Algorithm | M4 算法 ====================

    /**
     * Downsample using M4 (Min-Max aggregation)
     * 使用M4算法（最小-最大聚合）进行降采样
     *
     * <p>Each bucket retains 4 points: first, min, max, and last.
     * This preserves the range of values in each time window.</p>
     * <p>每个桶保留4个点：第一个、最小、最大和最后一个。
     * 这保持了每个时间窗口中值的范围。</p>
     *
     * @param series the time series | 时间序列
     * @param bucketDuration the duration of each bucket | 每个桶的持续时间
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries m4(TimeSeries series, Duration bucketDuration) {
        List<DataPoint> points = series.getPoints();
        if (points.isEmpty()) {
            return new TimeSeries(series.getName() + "_m4");
        }

        long bucketMillis = bucketDuration.toMillis();
        Map<Long, M4Bucket> buckets = new TreeMap<>();

        for (DataPoint point : points) {
            long bucketKey = (point.epochMillis() / bucketMillis) * bucketMillis;
            buckets.computeIfAbsent(bucketKey, k -> new M4Bucket()).add(point);
        }

        List<DataPoint> result = new ArrayList<>();
        for (M4Bucket bucket : buckets.values()) {
            result.addAll(bucket.getPoints());
        }

        // Sort by timestamp
        result.sort(DataPoint::compareTo);

        return new TimeSeries(series.getName() + "_m4", result);
    }

    /**
     * Downsample using M4 with target point count
     * 使用M4算法按目标点数降采样
     *
     * @param series the time series | 时间序列
     * @param targetBuckets the target number of buckets | 目标桶数
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries m4(TimeSeries series, int targetBuckets) {
        List<DataPoint> points = series.getPoints();
        if (points.isEmpty() || targetBuckets <= 0) {
            return new TimeSeries(series.getName() + "_m4");
        }

        long totalMillis = points.getLast().epochMillis() - points.getFirst().epochMillis();
        long bucketMillis = Math.max(1, totalMillis / targetBuckets);

        return m4(series, Duration.ofMillis(bucketMillis));
    }

    // ==================== Percentile-Based Downsampling | 基于百分位的降采样 ====================

    /**
     * Downsample using percentile aggregation
     * 使用百分位聚合进行降采样
     *
     * <p>Each bucket is represented by its median (50th percentile) or
     * specified percentile value.</p>
     * <p>每个桶由其中位数（第50百分位）或指定的百分位值表示。</p>
     *
     * @param series the time series | 时间序列
     * @param bucketDuration the duration of each bucket | 每个桶的持续时间
     * @param percentile the percentile to use (0-100) | 使用的百分位（0-100）
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries percentile(TimeSeries series, Duration bucketDuration, int percentile) {
        if (percentile < 0 || percentile > 100) {
            throw new IllegalArgumentException("Percentile must be between 0 and 100");
        }

        List<DataPoint> points = series.getPoints();
        if (points.isEmpty()) {
            return new TimeSeries(series.getName() + "_percentile");
        }

        long bucketMillis = bucketDuration.toMillis();
        Map<Long, List<Double>> buckets = new TreeMap<>();

        for (DataPoint point : points) {
            long bucketKey = (point.epochMillis() / bucketMillis) * bucketMillis;
            buckets.computeIfAbsent(bucketKey, k -> new ArrayList<>()).add(point.value());
        }

        TimeSeries result = new TimeSeries(series.getName() + "_percentile");

        for (Map.Entry<Long, List<Double>> entry : buckets.entrySet()) {
            List<Double> values = entry.getValue();
            Collections.sort(values);

            int index = (int) Math.ceil(percentile / 100.0 * values.size()) - 1;
            index = Math.max(0, Math.min(index, values.size() - 1));

            Instant timestamp = Instant.ofEpochMilli(entry.getKey());
            result.add(DataPoint.of(timestamp, values.get(index)));
        }

        return result;
    }

    /**
     * Downsample using median (50th percentile)
     * 使用中位数（第50百分位）降采样
     *
     * @param series the time series | 时间序列
     * @param bucketDuration the duration of each bucket | 每个桶的持续时间
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries median(TimeSeries series, Duration bucketDuration) {
        return percentile(series, bucketDuration, 50);
    }

    // ==================== Variance-Based Downsampling | 基于方差的降采样 ====================

    /**
     * Downsample preserving high-variance regions
     * 保持高方差区域的降采样
     *
     * <p>Allocates more points to regions with higher variance.</p>
     * <p>为方差较高的区域分配更多点。</p>
     *
     * @param series the time series | 时间序列
     * @param targetSize the target number of points | 目标点数
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries variancePreserving(TimeSeries series, int targetSize) {
        List<DataPoint> points = series.getPoints();

        if (targetSize >= points.size()) {
            return new TimeSeries(series.getName() + "_variance", points);
        }

        // Calculate variance for each segment
        int numSegments = targetSize;
        int segmentSize = points.size() / numSegments;

        List<SegmentInfo> segments = new ArrayList<>();
        for (int i = 0; i < numSegments; i++) {
            int start = i * segmentSize;
            int end = (i == numSegments - 1) ? points.size() : (i + 1) * segmentSize;
            double variance = calculateVariance(points, start, end);
            segments.add(new SegmentInfo(start, end, variance));
        }

        // Sort by variance and allocate more points to higher variance segments
        double totalVariance = segments.stream().mapToDouble(s -> s.variance).sum();

        List<DataPoint> result = new ArrayList<>();
        for (SegmentInfo segment : segments) {
            // When totalVariance is zero (all values equal), distribute points equally
            int pointsToAllocate;
            if (totalVariance == 0) {
                pointsToAllocate = Math.max(1, targetSize / numSegments);
            } else {
                pointsToAllocate = (int) Math.max(1,
                    Math.round(segment.variance / totalVariance * targetSize));
            }

            // Select evenly spaced points from segment
            int segmentLength = segment.end - segment.start;
            int step = Math.max(1, segmentLength / pointsToAllocate);

            for (int j = segment.start; j < segment.end && result.size() < targetSize; j += step) {
                result.add(points.get(j));
            }
        }

        // Sort and remove duplicates
        result.sort(DataPoint::compareTo);

        return new TimeSeries(series.getName() + "_variance", result);
    }

    // ==================== Peak Preservation | 峰值保持 ====================

    /**
     * Downsample preserving local peaks and valleys
     * 保持局部峰值和谷值的降采样
     *
     * <p>Identifies and preserves local extrema in the data.</p>
     * <p>识别并保持数据中的局部极值。</p>
     *
     * @param series the time series | 时间序列
     * @param targetSize the target number of points | 目标点数
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries peakPreserving(TimeSeries series, int targetSize) {
        List<DataPoint> points = series.getPoints();

        if (targetSize >= points.size()) {
            return new TimeSeries(series.getName() + "_peaks", points);
        }

        // Find all peaks and valleys
        Set<Integer> extremaIndices = new TreeSet<>();
        extremaIndices.add(0); // First point
        extremaIndices.add(points.size() - 1); // Last point

        for (int i = 1; i < points.size() - 1; i++) {
            double prev = points.get(i - 1).value();
            double curr = points.get(i).value();
            double next = points.get(i + 1).value();

            if ((curr > prev && curr > next) || (curr < prev && curr < next)) {
                extremaIndices.add(i);
            }
        }

        List<DataPoint> result = new ArrayList<>();

        if (extremaIndices.size() >= targetSize) {
            // Too many extrema, select evenly spaced ones
            List<Integer> indices = new ArrayList<>(extremaIndices);
            int step = Math.max(1, indices.size() / targetSize);
            for (int i = 0; i < indices.size() && result.size() < targetSize; i += step) {
                result.add(points.get(indices.get(i)));
            }
        } else {
            // Include all extrema and fill remaining with evenly spaced points
            for (int idx : extremaIndices) {
                result.add(points.get(idx));
            }

            int remaining = targetSize - result.size();
            if (remaining > 0) {
                int step = points.size() / remaining;
                for (int i = 0; i < points.size() && result.size() < targetSize; i += step) {
                    if (!extremaIndices.contains(i)) {
                        result.add(points.get(i));
                    }
                }
            }
        }

        result.sort(DataPoint::compareTo);
        return new TimeSeries(series.getName() + "_peaks", result);
    }

    // ==================== Mode-Based Downsampling | 基于众数的降采样 ====================

    /**
     * Downsample using mode (most frequent value range)
     * 使用众数（最频繁的值范围）降采样
     *
     * @param series the time series | 时间序列
     * @param bucketDuration the duration of each bucket | 每个桶的持续时间
     * @param valueBins number of value bins | 值的分箱数
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries mode(TimeSeries series, Duration bucketDuration, int valueBins) {
        List<DataPoint> points = series.getPoints();
        if (points.isEmpty()) {
            return new TimeSeries(series.getName() + "_mode");
        }

        // Find value range
        double minVal = points.stream().mapToDouble(DataPoint::value).min().orElse(0);
        double maxVal = points.stream().mapToDouble(DataPoint::value).max().orElse(0);
        // Prevent division by zero when all values are equal
        double binWidth = (maxVal - minVal) / valueBins;
        if (binWidth == 0) {
            binWidth = 1.0;
        }

        long bucketMillis = bucketDuration.toMillis();
        Map<Long, List<Double>> buckets = new TreeMap<>();

        for (DataPoint point : points) {
            long bucketKey = (point.epochMillis() / bucketMillis) * bucketMillis;
            buckets.computeIfAbsent(bucketKey, k -> new ArrayList<>()).add(point.value());
        }

        TimeSeries result = new TimeSeries(series.getName() + "_mode");

        for (Map.Entry<Long, List<Double>> entry : buckets.entrySet()) {
            List<Double> values = entry.getValue();

            // Count values in each bin
            int[] binCounts = new int[valueBins];
            for (double value : values) {
                int bin = (int) ((value - minVal) / binWidth);
                bin = Math.min(bin, valueBins - 1);
                binCounts[bin]++;
            }

            // Find mode bin
            int modeBin = 0;
            for (int i = 1; i < valueBins; i++) {
                if (binCounts[i] > binCounts[modeBin]) {
                    modeBin = i;
                }
            }

            double modeValue = minVal + (modeBin + 0.5) * binWidth;
            Instant timestamp = Instant.ofEpochMilli(entry.getKey());
            result.add(DataPoint.of(timestamp, modeValue));
        }

        return result;
    }

    // ==================== Threshold-Based Downsampling | 基于阈值的降采样 ====================

    /**
     * Downsample by removing points within threshold of previous
     * 通过移除与前一个点相差在阈值内的点来降采样
     *
     * <p>Only keeps points that differ from the previous point by more than
     * the specified threshold.</p>
     * <p>只保留与前一个点相差超过指定阈值的点。</p>
     *
     * @param series the time series | 时间序列
     * @param threshold the minimum difference threshold | 最小差值阈值
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries threshold(TimeSeries series, double threshold) {
        List<DataPoint> points = series.getPoints();
        if (points.isEmpty()) {
            return new TimeSeries(series.getName() + "_threshold");
        }

        List<DataPoint> result = new ArrayList<>();
        result.add(points.getFirst());

        for (int i = 1; i < points.size(); i++) {
            double diff = Math.abs(points.get(i).value() - result.getLast().value());
            if (diff >= threshold) {
                result.add(points.get(i));
            }
        }

        // Always include last point
        if (result.size() > 1 && !result.getLast().equals(points.getLast())) {
            result.add(points.getLast());
        }

        return new TimeSeries(series.getName() + "_threshold", result);
    }

    /**
     * Downsample using percentage threshold
     * 使用百分比阈值降采样
     *
     * @param series the time series | 时间序列
     * @param percentThreshold the minimum percentage change | 最小百分比变化
     * @return the downsampled series | 降采样后的序列
     */
    public static TimeSeries percentageThreshold(TimeSeries series, double percentThreshold) {
        List<DataPoint> points = series.getPoints();
        if (points.isEmpty()) {
            return new TimeSeries(series.getName() + "_pct_threshold");
        }

        List<DataPoint> result = new ArrayList<>();
        result.add(points.getFirst());

        for (int i = 1; i < points.size(); i++) {
            double lastValue = result.getLast().value();
            double currentValue = points.get(i).value();

            if (lastValue == 0) {
                if (currentValue != 0) {
                    result.add(points.get(i));
                }
            } else {
                double percentChange = Math.abs((currentValue - lastValue) / lastValue) * 100;
                if (percentChange >= percentThreshold) {
                    result.add(points.get(i));
                }
            }
        }

        // Always include last point
        if (result.size() > 1 && !result.getLast().equals(points.getLast())) {
            result.add(points.getLast());
        }

        return new TimeSeries(series.getName() + "_pct_threshold", result);
    }

    // ==================== Helper Classes | 辅助类 ====================

    private static class M4Bucket {
        private DataPoint first;
        private DataPoint last;
        private DataPoint min;
        private DataPoint max;

        public void add(DataPoint point) {
            if (first == null) {
                first = last = min = max = point;
            } else {
                last = point;
                if (point.value() < min.value()) min = point;
                if (point.value() > max.value()) max = point;
            }
        }

        public List<DataPoint> getPoints() {
            if (first == null) return Collections.emptyList();

            Set<DataPoint> unique = new LinkedHashSet<>();
            unique.add(first);
            unique.add(min);
            unique.add(max);
            unique.add(last);

            List<DataPoint> result = new ArrayList<>(unique);
            result.sort(DataPoint::compareTo);
            return result;
        }
    }

    private record SegmentInfo(int start, int end, double variance) {}

    private static double calculateVariance(List<DataPoint> points, int start, int end) {
        if (start >= end) return 0;

        double sum = 0, sumSq = 0;
        int count = 0;

        for (int i = start; i < end; i++) {
            double value = points.get(i).value();
            sum += value;
            sumSq += value * value;
            count++;
        }

        double mean = sum / count;
        return (sumSq / count) - (mean * mean);
    }
}
