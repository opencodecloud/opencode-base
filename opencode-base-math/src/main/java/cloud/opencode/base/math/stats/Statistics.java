package cloud.opencode.base.math.stats;

import java.util.Arrays;

/**
 * Statistical utility methods for descriptive statistics.
 * 描述性统计工具方法集合
 *
 * <p>All methods are stateless and thread-safe. Input arrays are never modified.
 * Null or empty arrays cause {@link IllegalArgumentException}.</p>
 * <p>所有方法无状态且线程安全。输入数组不会被修改。
 * 空值或空数组将抛出 {@link IllegalArgumentException}。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class Statistics {

    private Statistics() {
        throw new AssertionError("No instances");
    }

    /**
     * Computes the p-th percentile using linear interpolation.
     * 使用线性插值法计算第 p 百分位数
     *
     * @param data the input data array / 输入数据数组
     * @param p    percentile in [0, 100] / 百分位数，范围 [0, 100]
     * @return the p-th percentile value / 第 p 百分位数的值
     * @throws IllegalArgumentException if data is null/empty or p is out of range
     */
    public static double percentile(double[] data, double p) {
        requireNonEmpty(data, "data");
        if (Double.isNaN(p) || p < 0 || p > 100) {
            throw new IllegalArgumentException("Percentile p must be in [0, 100], got: " + p);
        }
        double[] sorted = sortedCopy(data);
        return interpolatedPercentile(sorted, p);
    }

    /**
     * Returns all mode values (most frequent values) in the data.
     * 返回数据中的所有众数（出现频率最高的值）
     *
     * @param data the input data array / 输入数据数组
     * @return array of mode values / 众数数组
     * @throws IllegalArgumentException if data is null or empty
     */
    public static double[] mode(double[] data) {
        requireNonEmpty(data, "data");
        // Sort-based approach: no boxing, no HashMap
        double[] sorted = data.clone();
        Arrays.sort(sorted);

        int maxCount = 0;
        int modeCount = 0;
        int currentCount = 1;

        // First pass: find max frequency
        for (int i = 1; i < sorted.length; i++) {
            if (Double.compare(sorted[i], sorted[i - 1]) == 0) {
                currentCount++;
            } else {
                if (currentCount > maxCount) {
                    maxCount = currentCount;
                    modeCount = 1;
                } else if (currentCount == maxCount) {
                    modeCount++;
                }
                currentCount = 1;
            }
        }
        // Handle last run
        if (currentCount > maxCount) {
            maxCount = currentCount;
            modeCount = 1;
        } else if (currentCount == maxCount) {
            modeCount++;
        }

        // Second pass: collect modes
        double[] result = new double[modeCount];
        int idx = 0;
        currentCount = 1;
        for (int i = 1; i < sorted.length; i++) {
            if (Double.compare(sorted[i], sorted[i - 1]) == 0) {
                currentCount++;
            } else {
                if (currentCount == maxCount) {
                    result[idx++] = sorted[i - 1];
                }
                currentCount = 1;
            }
        }
        if (currentCount == maxCount) {
            result[idx++] = sorted[sorted.length - 1];
        }
        return result;
    }

    /**
     * Computes the sample skewness (Fisher's definition).
     * 计算样本偏度（Fisher 定义）
     *
     * <p>Requires at least 3 data points.</p>
     * <p>需要至少 3 个数据点。</p>
     *
     * @param data the input data array / 输入数据数组
     * @return the sample skewness / 样本偏度
     * @throws IllegalArgumentException if data is null, empty, or has fewer than 3 elements
     */
    public static double skewness(double[] data) {
        validateNonNullNonEmpty(data, "data");
        int n = data.length;
        if (n < 3) {
            throw new IllegalArgumentException("Skewness requires at least 3 data points, got: " + n);
        }
        double mean = checkedMean(data, "data");
        double m2 = 0.0;
        double m3 = 0.0;
        for (double v : data) {
            double d = v - mean;
            m2 += d * d;
            m3 += d * d * d;
        }
        m2 /= n;
        m3 /= n;
        double s = Math.sqrt(m2);
        if (s == 0.0) {
            return 0.0;
        }
        double g1 = m3 / (s * s * s);
        // Fisher's adjustment: G1 = g1 * sqrt(n*(n-1)) / (n-2)
        return g1 * Math.sqrt((double) n * (n - 1)) / (n - 2);
    }

    /**
     * Computes the excess kurtosis of the data.
     * 计算数据的超额峰度
     *
     * <p>Requires at least 4 data points. Uses the sample excess kurtosis formula.</p>
     * <p>需要至少 4 个数据点。使用样本超额峰度公式。</p>
     *
     * @param data the input data array / 输入数据数组
     * @return the excess kurtosis / 超额峰度
     * @throws IllegalArgumentException if data is null, empty, or has fewer than 4 elements
     */
    public static double kurtosis(double[] data) {
        validateNonNullNonEmpty(data, "data");
        int n = data.length;
        if (n < 4) {
            throw new IllegalArgumentException("Kurtosis requires at least 4 data points, got: " + n);
        }
        double mean = checkedMean(data, "data");
        double m2 = 0.0;
        double m4 = 0.0;
        for (double v : data) {
            double d = v - mean;
            double d2 = d * d;
            m2 += d2;
            m4 += d2 * d2;
        }
        m2 /= n;
        m4 /= n;
        if (m2 == 0.0) {
            return 0.0;
        }
        double g2 = m4 / (m2 * m2) - 3.0;
        // Sample excess kurtosis adjustment
        double n1 = n - 1;
        double n2 = n - 2;
        double n3 = n - 3;
        return (n1 / (n2 * n3)) * ((n + 1) * g2 + 6.0);
    }

    /**
     * Computes the sample covariance between two arrays.
     * 计算两个数组的样本协方差
     *
     * @param x the first data array / 第一个数据数组
     * @param y the second data array / 第二个数据数组
     * @return the sample covariance / 样本协方差
     * @throws IllegalArgumentException if arrays are null, empty, different lengths, or have fewer than 2 elements
     */
    public static double covariance(double[] x, double[] y) {
        validateNonNullNonEmpty(x, "x");
        validateNonNullNonEmpty(y, "y");
        requireSameLength(x, y);
        int n = x.length;
        if (n < 2) {
            throw new IllegalArgumentException("Covariance requires at least 2 data points, got: " + n);
        }
        // Fused mean computation with inline finite check
        double sumX = 0.0, sumY = 0.0;
        for (int i = 0; i < n; i++) {
            requireFiniteElement(x[i], "x", i);
            requireFiniteElement(y[i], "y", i);
            sumX += x[i];
            sumY += y[i];
        }
        double meanX = sumX / n;
        double meanY = sumY / n;
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += (x[i] - meanX) * (y[i] - meanY);
        }
        return sum / (n - 1);
    }

    /**
     * Computes the Pearson correlation coefficient between two arrays.
     * 计算两个数组的皮尔逊相关系数
     *
     * @param x the first data array / 第一个数据数组
     * @param y the second data array / 第二个数据数组
     * @return the Pearson correlation coefficient in [-1, 1] / 皮尔逊相关系数，范围 [-1, 1]
     * @throws IllegalArgumentException if arrays are null, empty, different lengths, or have fewer than 2 elements
     */
    public static double correlation(double[] x, double[] y) {
        validateNonNullNonEmpty(x, "x");
        validateNonNullNonEmpty(y, "y");
        requireSameLength(x, y);
        int n = x.length;
        if (n < 2) {
            throw new IllegalArgumentException("Correlation requires at least 2 data points, got: " + n);
        }
        // Fused mean computation with inline finite check
        double sx = 0.0, sy = 0.0;
        for (int i = 0; i < n; i++) {
            requireFiniteElement(x[i], "x", i);
            requireFiniteElement(y[i], "y", i);
            sx += x[i];
            sy += y[i];
        }
        double meanX = sx / n;
        double meanY = sy / n;
        double sumXY = 0.0;
        double sumX2 = 0.0;
        double sumY2 = 0.0;
        for (int i = 0; i < n; i++) {
            double dx = x[i] - meanX;
            double dy = y[i] - meanY;
            sumXY += dx * dy;
            sumX2 += dx * dx;
            sumY2 += dy * dy;
        }
        double denom = Math.sqrt(sumX2 * sumY2);
        if (denom == 0.0) {
            return 0.0;
        }
        return sumXY / denom;
    }

    /**
     * Computes the weighted arithmetic mean.
     * 计算加权算术平均值
     *
     * @param values  the data values / 数据值
     * @param weights the weights (must all be non-negative, at least one positive) / 权重（必须非负，至少一个正值）
     * @return the weighted mean / 加权平均值
     * @throws IllegalArgumentException if arrays are null, empty, different lengths, or weights are invalid
     */
    public static double weightedMean(double[] values, double[] weights) {
        validateNonNullNonEmpty(values, "values");
        validateNonNullNonEmpty(weights, "weights");
        requireSameLength(values, weights);
        double sumW = 0.0;
        double sumVW = 0.0;
        for (int i = 0; i < values.length; i++) {
            requireFiniteElement(values[i], "values", i);
            if (!(weights[i] >= 0) || Double.isInfinite(weights[i])) {
                throw new IllegalArgumentException(
                        "Weight must be non-negative and finite, got: " + weights[i] + " at index " + i);
            }
            sumW += weights[i];
            sumVW += values[i] * weights[i];
        }
        if (sumW == 0.0) {
            throw new IllegalArgumentException("Sum of weights must be positive");
        }
        return sumVW / sumW;
    }

    /**
     * Computes the geometric mean. All values must be positive.
     * 计算几何平均值。所有值必须为正数。
     *
     * @param values the data values (all must be &gt; 0) / 数据值（必须全部大于 0）
     * @return the geometric mean / 几何平均值
     * @throws IllegalArgumentException if values is null, empty, or contains non-positive values
     */
    public static double geometricMean(double[] values) {
        validateNonNullNonEmpty(values, "values");
        double sumLog = 0.0;
        for (int i = 0; i < values.length; i++) {
            requireFiniteElement(values[i], "values", i);
            if (values[i] <= 0) {
                throw new IllegalArgumentException("All values must be positive for geometric mean, got: " + values[i] + " at index " + i);
            }
            sumLog += Math.log(values[i]);
        }
        return Math.exp(sumLog / values.length);
    }

    /**
     * Computes the harmonic mean. All values must be positive.
     * 计算调和平均值。所有值必须为正数。
     *
     * @param values the data values (all must be &gt; 0) / 数据值（必须全部大于 0）
     * @return the harmonic mean / 调和平均值
     * @throws IllegalArgumentException if values is null, empty, or contains non-positive values
     */
    public static double harmonicMean(double[] values) {
        validateNonNullNonEmpty(values, "values");
        double sumReciprocal = 0.0;
        for (int i = 0; i < values.length; i++) {
            requireFiniteElement(values[i], "values", i);
            if (values[i] <= 0) {
                throw new IllegalArgumentException("All values must be positive for harmonic mean, got: " + values[i] + " at index " + i);
            }
            sumReciprocal += 1.0 / values[i];
        }
        return values.length / sumReciprocal;
    }

    /**
     * Computes the range (max - min) of the data.
     * 计算数据的极差（最大值 - 最小值）
     *
     * @param data the input data array / 输入数据数组
     * @return the range / 极差
     * @throws IllegalArgumentException if data is null or empty
     */
    public static double range(double[] data) {
        validateNonNullNonEmpty(data, "data");
        requireFiniteElement(data[0], "data", 0);
        double min = data[0];
        double max = data[0];
        for (int i = 1; i < data.length; i++) {
            requireFiniteElement(data[i], "data", i);
            if (data[i] < min) min = data[i];
            if (data[i] > max) max = data[i];
        }
        return max - min;
    }

    /**
     * Computes the interquartile range (Q3 - Q1).
     * 计算四分位距（Q3 - Q1）
     *
     * @param data the input data array / 输入数据数组
     * @return the interquartile range / 四分位距
     * @throws IllegalArgumentException if data is null or empty
     */
    public static double interquartileRange(double[] data) {
        requireNonEmpty(data, "data");
        double[] sorted = sortedCopy(data);
        return interpolatedPercentile(sorted, 75.0) - interpolatedPercentile(sorted, 25.0);
    }

    /**
     * Computes Spearman's rank correlation coefficient between two arrays.
     * 计算两个数组的斯皮尔曼等级相关系数
     *
     * <p>Converts both arrays to fractional ranks (averaging tied ranks),
     * then computes the Pearson correlation on the ranks.</p>
     * <p>将两个数组转换为分数秩（平均处理并列秩），
     * 然后对秩计算皮尔逊相关系数。</p>
     *
     * @param x the first data array / 第一个数据数组
     * @param y the second data array / 第二个数据数组
     * @return Spearman's rho in [-1, 1] / 斯皮尔曼 rho，范围 [-1, 1]
     * @throws IllegalArgumentException if arrays are null, empty, different lengths,
     *                                  have fewer than 2 elements, or contain NaN/Infinity
     */
    public static double spearmanCorrelation(double[] x, double[] y) {
        requireNonEmpty(x, "x");
        requireNonEmpty(y, "y");
        requireSameLength(x, y);
        int n = x.length;
        if (n < 2) {
            throw new IllegalArgumentException("Spearman correlation requires at least 2 data points, got: " + n);
        }
        double[] rankX = fractionalRanks(x);
        double[] rankY = fractionalRanks(y);
        return correlation(rankX, rankY);
    }

    /**
     * Computes Kendall's tau-b correlation coefficient between two arrays.
     * 计算两个数组的肯德尔 tau-b 相关系数
     *
     * <p>Uses a merge-sort based O(n log n) algorithm (Knight 2006) with proper
     * tie correction (tau-b formula).</p>
     * <p>使用基于归并排序的 O(n log n) 算法（Knight 2006），
     * 并使用正确的并列校正（tau-b 公式）。</p>
     *
     * @param x the first data array / 第一个数据数组
     * @param y the second data array / 第二个数据数组
     * @return Kendall's tau-b in [-1, 1] / 肯德尔 tau-b，范围 [-1, 1]
     * @throws IllegalArgumentException if arrays are null, empty, different lengths,
     *                                  have fewer than 2 elements, or contain NaN/Infinity
     */
    public static double kendallCorrelation(double[] x, double[] y) {
        requireNonEmpty(x, "x");
        requireNonEmpty(y, "y");
        requireSameLength(x, y);
        int n = x.length;
        if (n < 2) {
            throw new IllegalArgumentException("Kendall correlation requires at least 2 data points, got: " + n);
        }

        // Sort pairs by x, breaking ties by y
        int[] idx = sortedIndices(x, y, n);

        long totalPairs = (long) n * (n - 1) / 2;

        // Count ties in x
        long tiedX = 0;
        long tiedXY = 0;
        int i = 0;
        while (i < n) {
            int j = i;
            while (j < n && Double.compare(x[idx[j]], x[idx[i]]) == 0) {
                j++;
            }
            long groupSize = j - i;
            tiedX += groupSize * (groupSize - 1) / 2;
            // Count ties in both x and y within this group
            int k = i;
            while (k < j) {
                int l = k;
                while (l < j && Double.compare(y[idx[l]], y[idx[k]]) == 0) {
                    l++;
                }
                long subSize = l - k;
                tiedXY += subSize * (subSize - 1) / 2;
                k = l;
            }
            i = j;
        }

        // Extract y values in x-sorted order, then merge-sort count
        double[] yArr = new double[n];
        for (int p = 0; p < n; p++) {
            yArr[p] = y[idx[p]];
        }

        // Count ties in y (from original array)
        long tiedY = countTiedPairs(y);

        // Merge-sort count of discordant pairs (inversions in y)
        long swaps = mergeSort(yArr, new double[n], 0, n - 1);
        long concordant = totalPairs - tiedX - tiedY + tiedXY - swaps;
        long discordant = swaps;

        // Recalculate: concordant + discordant + tiedX-only + tiedY-only + tiedXY = totalPairs
        // concordant = totalPairs - tiedX - tiedY + tiedXY - discordant
        // (already computed above)

        double denomX = totalPairs - tiedX;
        double denomY = totalPairs - tiedY;
        double denom = Math.sqrt(denomX * denomY);
        if (denom == 0.0) {
            return 0.0;
        }
        return (concordant - discordant) / denom;
    }

    private static int[] sortedIndices(double[] x, double[] y, int n) {
        int[] indices = new int[n];
        for (int i = 0; i < n; i++) {
            indices[i] = i;
        }
        dualKeyMergeSort(x, y, indices, new int[n], 0, n - 1);
        return indices;
    }

    private static void dualKeyMergeSort(double[] x, double[] y, int[] idx, int[] buf, int lo, int hi) {
        if (lo >= hi) {
            return;
        }
        int mid = lo + (hi - lo) / 2;
        dualKeyMergeSort(x, y, idx, buf, lo, mid);
        dualKeyMergeSort(x, y, idx, buf, mid + 1, hi);
        System.arraycopy(idx, lo, buf, lo, hi - lo + 1);
        int i = lo, j = mid + 1, k = lo;
        while (i <= mid && j <= hi) {
            int cmp = Double.compare(x[buf[i]], x[buf[j]]);
            if (cmp < 0 || (cmp == 0 && Double.compare(y[buf[i]], y[buf[j]]) <= 0)) {
                idx[k++] = buf[i++];
            } else {
                idx[k++] = buf[j++];
            }
        }
        while (i <= mid) {
            idx[k++] = buf[i++];
        }
        while (j <= hi) {
            idx[k++] = buf[j++];
        }
    }

    private static long countTiedPairs(double[] arr) {
        double[] sorted = arr.clone();
        Arrays.sort(sorted);
        long tied = 0;
        int i = 0;
        while (i < sorted.length) {
            int j = i;
            while (j < sorted.length && Double.compare(sorted[j], sorted[i]) == 0) {
                j++;
            }
            long groupSize = j - i;
            tied += groupSize * (groupSize - 1) / 2;
            i = j;
        }
        return tied;
    }

    private static long mergeSort(double[] arr, double[] buf, int lo, int hi) {
        if (lo >= hi) {
            return 0;
        }
        int mid = lo + (hi - lo) / 2;
        long swaps = mergeSort(arr, buf, lo, mid) + mergeSort(arr, buf, mid + 1, hi);
        // Merge
        System.arraycopy(arr, lo, buf, lo, hi - lo + 1);
        int i = lo, j = mid + 1, k = lo;
        while (i <= mid && j <= hi) {
            if (Double.compare(buf[i], buf[j]) <= 0) {
                arr[k++] = buf[i++];
            } else {
                arr[k++] = buf[j++];
                swaps += (mid - i + 1);
            }
        }
        while (i <= mid) {
            arr[k++] = buf[i++];
        }
        while (j <= hi) {
            arr[k++] = buf[j++];
        }
        return swaps;
    }

    // ==================== Internal Helpers ====================

    static double mean(double[] data) {
        double sum = 0.0;
        for (double v : data) {
            sum += v;
        }
        return sum / data.length;
    }

    /**
     * Computes mean with inline finite check — avoids a separate O(n) validation pass.
     */
    private static double checkedMean(double[] data, String name) {
        double sum = 0.0;
        for (int i = 0; i < data.length; i++) {
            requireFiniteElement(data[i], name, i);
            sum += data[i];
        }
        return sum / data.length;
    }

    private static double[] sortedCopy(double[] data) {
        double[] copy = Arrays.copyOf(data, data.length);
        Arrays.sort(copy);
        return copy;
    }

    /**
     * Linear interpolation percentile on a pre-sorted array.
     * p is in [0, 100].
     */
    static double interpolatedPercentile(double[] sorted, double p) {
        int n = sorted.length;
        if (n == 1) {
            return sorted[0];
        }
        // Map p to index using the C = 1 method (exclusive):
        // rank = p/100 * (n + 1), then 1-based index
        // Alternatively, the standard linear interpolation method:
        double rank = p / 100.0 * (n - 1);
        int lower = (int) Math.floor(rank);
        int upper = (int) Math.ceil(rank);
        if (lower < 0) lower = 0;
        if (upper >= n) upper = n - 1;
        if (lower == upper) {
            return sorted[lower];
        }
        double frac = rank - lower;
        return sorted[lower] + frac * (sorted[upper] - sorted[lower]);
    }

    /**
     * Null + empty check only. Used by methods that inline finite checks into computation loops.
     */
    private static void validateNonNullNonEmpty(double[] array, String name) {
        if (array == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        if (array.length == 0) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
    }

    /**
     * Inline finite element check — called inside computation loops to avoid a separate O(n) pass.
     */
    private static void requireFiniteElement(double value, String name, int index) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(
                    name + "[" + index + "] is not finite: " + value);
        }
    }

    /**
     * Full validation: null + empty + all-finite. Used by sort-dependent methods where
     * NaN/Infinity must be caught before sorting (NaN breaks sort ordering).
     */
    private static void requireNonEmpty(double[] array, String name) {
        if (array == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        if (array.length == 0) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        for (int i = 0; i < array.length; i++) {
            if (!Double.isFinite(array[i])) {
                throw new IllegalArgumentException(
                        name + "[" + i + "] is not finite: " + array[i]);
            }
        }
    }

    private static void requireSameLength(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException(
                    "Arrays must have the same length, got: " + a.length + " and " + b.length);
        }
    }

    /**
     * Computes fractional (average) ranks for the given data.
     * Ties receive the average of the ranks they would occupy.
     */
    private static double[] fractionalRanks(double[] data) {
        int n = data.length;
        // Use int[] + indirect sort to avoid Integer[] boxing overhead
        int[] indices = indirectSort(data, n);

        double[] ranks = new double[n];
        int i = 0;
        while (i < n) {
            int j = i;
            while (j < n - 1 && Double.compare(data[indices[j]], data[indices[j + 1]]) == 0) {
                j++;
            }
            double avgRank = (i + j) / 2.0 + 1.0;
            for (int k = i; k <= j; k++) {
                ranks[indices[k]] = avgRank;
            }
            i = j + 1;
        }
        return ranks;
    }

    private static int[] indirectSort(double[] data, int n) {
        int[] indices = new int[n];
        for (int i = 0; i < n; i++) {
            indices[i] = i;
        }
        indirectMergeSort(data, indices, new int[n], 0, n - 1);
        return indices;
    }

    private static void indirectMergeSort(double[] data, int[] idx, int[] buf, int lo, int hi) {
        if (lo >= hi) {
            return;
        }
        int mid = lo + (hi - lo) / 2;
        indirectMergeSort(data, idx, buf, lo, mid);
        indirectMergeSort(data, idx, buf, mid + 1, hi);
        System.arraycopy(idx, lo, buf, lo, hi - lo + 1);
        int i = lo, j = mid + 1, k = lo;
        while (i <= mid && j <= hi) {
            if (Double.compare(data[buf[i]], data[buf[j]]) <= 0) {
                idx[k++] = buf[i++];
            } else {
                idx[k++] = buf[j++];
            }
        }
        while (i <= mid) {
            idx[k++] = buf[i++];
        }
        while (j <= hi) {
            idx[k++] = buf[j++];
        }
    }
}
