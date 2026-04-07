package cloud.opencode.base.timeseries.quality;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import cloud.opencode.base.timeseries.exception.TimeSeriesErrorCode;
import cloud.opencode.base.timeseries.exception.TimeSeriesException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Gap Detector for Time Series Data Quality
 * 时间序列数据质量间隙检测器
 *
 * <p>Detects gaps (missing data intervals) in time series data by comparing
 * consecutive point intervals against an expected interval with configurable tolerance.</p>
 * <p>通过将连续数据点间隔与预期间隔进行比较（可配置容差），检测时间序列数据中的间隙（缺失数据区间）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Gap detection with configurable tolerance factor - 可配置容差因子的间隙检测</li>
 *   <li>Data completeness calculation over a time range - 时间范围内的数据完整性计算</li>
 *   <li>Longest gap identification - 最长间隙识别</li>
 *   <li>Gap counting shorthand - 间隙计数快捷方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TimeSeries ts = new TimeSeries("metrics");
 * // ... add data points ...
 *
 * List<Gap> gaps = GapDetector.detectGaps(ts, Duration.ofMinutes(1));
 * double completeness = GapDetector.dataCompleteness(ts, Duration.ofMinutes(1), from, to);
 * Optional<Gap> longest = GapDetector.longestGap(ts);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (null arguments throw NullPointerException or TimeSeriesException) - 空值安全: 否</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for all operations - 时间复杂度: 所有操作均为 O(n)</li>
 *   <li>Space complexity: O(g) where g is the number of gaps - 空间复杂度: O(g)，g 为间隙数量</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.3
 */
public final class GapDetector {

    /** Default tolerance factor (1.5x expected interval) | 默认容差因子（预期间隔的 1.5 倍） */
    private static final double DEFAULT_TOLERANCE_FACTOR = 1.5;

    private GapDetector() {
        // Utility class
    }

    /**
     * Detect gaps where consecutive points are more than expectedInterval * 1.5 apart.
     * 检测连续数据点间隔超过预期间隔 1.5 倍的间隙。
     *
     * <p>Uses a default tolerance factor of 1.5 to avoid false positives from minor jitter.</p>
     * <p>使用默认容差因子 1.5 以避免因轻微抖动产生的误报。</p>
     *
     * @param ts               the time series to check | 要检查的时间序列
     * @param expectedInterval the expected interval between data points | 数据点之间的预期间隔
     * @return list of detected gaps | 检测到的间隙列表
     * @throws NullPointerException if ts is null | 如果 ts 为空抛出空指针异常
     * @throws TimeSeriesException  if expectedInterval is null or non-positive | 如果预期间隔为空或非正数抛出异常
     */
    public static List<Gap> detectGaps(TimeSeries ts, Duration expectedInterval) {
        return detectGaps(ts, expectedInterval, DEFAULT_TOLERANCE_FACTOR);
    }

    /**
     * Detect gaps with a custom tolerance factor.
     * 使用自定义容差因子检测间隙。
     *
     * <p>A gap is recorded when the interval between consecutive points exceeds
     * expectedInterval * toleranceFactor.</p>
     * <p>当连续数据点之间的间隔超过 expectedInterval * toleranceFactor 时记录间隙。</p>
     *
     * @param ts               the time series to check | 要检查的时间序列
     * @param expectedInterval the expected interval between data points | 数据点之间的预期间隔
     * @param toleranceFactor  the tolerance multiplier (must be {@literal >} 1.0) | 容差乘数（必须大于 1.0）
     * @return list of detected gaps | 检测到的间隙列表
     * @throws NullPointerException if ts is null | 如果 ts 为空抛出空指针异常
     * @throws TimeSeriesException  if expectedInterval is null or non-positive | 如果预期间隔为空或非正数抛出异常
     * @throws IllegalArgumentException if toleranceFactor is not {@literal >} 1.0 | 如果容差因子不大于 1.0 抛出异常
     */
    public static List<Gap> detectGaps(TimeSeries ts, Duration expectedInterval, double toleranceFactor) {
        Objects.requireNonNull(ts, "Time series must not be null");
        validateExpectedInterval(expectedInterval);
        if (toleranceFactor <= 1.0) {
            throw new IllegalArgumentException(
                    "Tolerance factor must be > 1.0, got: " + toleranceFactor);
        }

        List<DataPoint> points = ts.getPoints();
        if (points.size() < 2) {
            return Collections.emptyList();
        }

        double thresholdDouble = expectedInterval.toMillis() * toleranceFactor;
        long thresholdMillis = (thresholdDouble > Long.MAX_VALUE || thresholdDouble < 0)
                ? Long.MAX_VALUE : (long) thresholdDouble;
        List<Gap> gaps = new ArrayList<>();

        for (int i = 1; i < points.size(); i++) {
            DataPoint prev = points.get(i - 1);
            DataPoint curr = points.get(i);
            // Use primitive subtraction to avoid Duration object allocation in hot loop
            long intervalMillis = curr.epochMillis() - prev.epochMillis();

            if (intervalMillis > thresholdMillis) {
                gaps.add(new Gap(prev.timestamp(), curr.timestamp()));
            }
        }

        return Collections.unmodifiableList(gaps);
    }

    /**
     * Calculate data completeness as a percentage [0.0, 1.0].
     * 计算数据完整性百分比 [0.0, 1.0]。
     *
     * <p>Computes the ratio of actual data points present in [from, to] to the
     * expected number of points based on the expected interval.</p>
     * <p>计算 [from, to] 范围内实际数据点数与基于预期间隔的期望数据点数的比率。</p>
     *
     * @param ts               the time series | 时间序列
     * @param expectedInterval the expected interval between data points | 数据点之间的预期间隔
     * @param from             the start of the time range (inclusive) | 时间范围开始（包含）
     * @param to               the end of the time range (inclusive) | 时间范围结束（包含）
     * @return completeness ratio in [0.0, 1.0] | 完整性比率，范围 [0.0, 1.0]
     * @throws NullPointerException if any argument is null | 如果任何参数为空抛出空指针异常
     * @throws TimeSeriesException  if expectedInterval is null or non-positive | 如果预期间隔为空或非正数抛出异常
     */
    public static double dataCompleteness(TimeSeries ts, Duration expectedInterval, Instant from, Instant to) {
        Objects.requireNonNull(ts, "Time series must not be null");
        validateExpectedInterval(expectedInterval);
        Objects.requireNonNull(from, "From instant must not be null");
        Objects.requireNonNull(to, "To instant must not be null");

        long totalMillis = Duration.between(from, to).toMillis();
        if (totalMillis <= 0) {
            return 0.0;
        }

        long intervalMillis = expectedInterval.toMillis();
        double expectedPoints = (double) totalMillis / intervalMillis;
        if (expectedPoints <= 0) {
            return 0.0;
        }

        TimeSeries rangeTs = ts.range(from, to);
        long actualPoints = rangeTs.size();

        return Math.min(1.0, actualPoints / expectedPoints);
    }

    /**
     * Find the longest gap between consecutive data points.
     * 查找连续数据点之间的最长间隙。
     *
     * @param ts the time series | 时间序列
     * @return the longest gap, or empty if the series has fewer than 2 points | 最长间隙，如果序列少于两个点则为空
     * @throws NullPointerException if ts is null | 如果 ts 为空抛出空指针异常
     */
    public static Optional<Gap> longestGap(TimeSeries ts) {
        Objects.requireNonNull(ts, "Time series must not be null");

        List<DataPoint> points = ts.getPoints();
        if (points.size() < 2) {
            return Optional.empty();
        }

        Gap longest = null;
        long longestMillis = -1;

        for (int i = 1; i < points.size(); i++) {
            DataPoint prev = points.get(i - 1);
            DataPoint curr = points.get(i);
            long intervalMillis = curr.epochMillis() - prev.epochMillis();

            if (intervalMillis > longestMillis) {
                longestMillis = intervalMillis;
                longest = new Gap(prev.timestamp(), curr.timestamp());
            }
        }

        return Optional.ofNullable(longest);
    }

    /**
     * Count the number of gaps (shorthand for detectGaps().size()).
     * 计算间隙数量（detectGaps().size() 的快捷方法）。
     *
     * @param ts               the time series | 时间序列
     * @param expectedInterval the expected interval between data points | 数据点之间的预期间隔
     * @return the number of detected gaps | 检测到的间隙数量
     * @throws NullPointerException if ts is null | 如果 ts 为空抛出空指针异常
     * @throws TimeSeriesException  if expectedInterval is null or non-positive | 如果预期间隔为空或非正数抛出异常
     */
    public static int gapCount(TimeSeries ts, Duration expectedInterval) {
        return detectGaps(ts, expectedInterval).size();
    }

    /**
     * Validate expected interval parameter.
     * 验证预期间隔参数。
     */
    private static void validateExpectedInterval(Duration expectedInterval) {
        if (expectedInterval == null) {
            throw new TimeSeriesException(TimeSeriesErrorCode.INVALID_INTERVAL,
                    "Expected interval must not be null");
        }
        if (expectedInterval.isZero() || expectedInterval.isNegative()) {
            throw new TimeSeriesException(TimeSeriesErrorCode.INVALID_INTERVAL,
                    "Expected interval must be positive, got: " + expectedInterval);
        }
    }
}
