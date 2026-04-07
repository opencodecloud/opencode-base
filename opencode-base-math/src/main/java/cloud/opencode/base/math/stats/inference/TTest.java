package cloud.opencode.base.math.stats.inference;

import cloud.opencode.base.math.exception.MathException;
import cloud.opencode.base.math.special.SpecialFunctions;

/**
 * Student's t-test implementations for hypothesis testing.
 * Student t 检验实现，用于假设检验
 *
 * <p>Provides one-sample, independent two-sample (Welch's), and paired t-tests.
 * All methods return a {@link TestResult} with two-tailed p-values.</p>
 * <p>提供单样本、独立双样本（Welch）和配对 t 检验。
 * 所有方法返回包含双尾 p 值的 {@link TestResult}。</p>
 *
 * <p>All methods are stateless and thread-safe.</p>
 * <p>所有方法无状态且线程安全。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class TTest {

    private TTest() {
        throw new AssertionError("No instances");
    }

    /**
     * Performs a one-sample t-test (H0: population mean = mu0).
     * 执行单样本 t 检验（H0: 总体均值 = mu0）
     *
     * <p>Computes t = (mean - mu0) / (s / sqrt(n)), df = n - 1, two-tailed p-value.</p>
     * <p>计算 t = (mean - mu0) / (s / sqrt(n))，df = n - 1，双尾 p 值。</p>
     *
     * @param data the sample data (at least 2 elements) / 样本数据（至少 2 个元素）
     * @param mu0  the hypothesized population mean / 假设的总体均值
     * @return the test result / 检验结果
     * @throws MathException if data is null, has fewer than 2 elements, or contains NaN/Infinity
     */
    public static TestResult oneSample(double[] data, double mu0) {
        validateArrayBasic(data, "data", 2);
        requireFinite(mu0, "mu0");

        int n = data.length;
        // Single-pass mean + variance (fused validation + Welford)
        double mean = 0.0;
        double m2 = 0.0;
        for (int i = 0; i < n; i++) {
            requireFiniteElement(data[i], "data", i);
            double delta = data[i] - mean;
            mean += delta / (i + 1);
            m2 += delta * (data[i] - mean);
        }
        double sampleVar = m2 / (n - 1);
        double s = Math.sqrt(sampleVar);

        if (s == 0.0) {
            double t = (mean == mu0) ? 0.0 : Double.POSITIVE_INFINITY * Math.signum(mean - mu0);
            double p = (mean == mu0) ? 1.0 : 0.0;
            return new TestResult("One-Sample T-Test", t, p, n - 1.0);
        }

        double t = (mean - mu0) / (s / Math.sqrt(n));
        double df = n - 1.0;
        double p = twoTailedPValue(t, df);

        return new TestResult("One-Sample T-Test", t, p, df);
    }

    /**
     * Performs an independent two-sample t-test using Welch's method (unequal variances).
     * 使用 Welch 方法执行独立双样本 t 检验（不等方差）
     *
     * <p>Computes t = (meanX - meanY) / sqrt(sX^2/nX + sY^2/nY),
     * with Welch-Satterthwaite degrees of freedom approximation.</p>
     * <p>计算 t = (meanX - meanY) / sqrt(sX^2/nX + sY^2/nY)，
     * 使用 Welch-Satterthwaite 自由度近似。</p>
     *
     * @param x first sample data (at least 2 elements) / 第一组样本数据（至少 2 个元素）
     * @param y second sample data (at least 2 elements) / 第二组样本数据（至少 2 个元素）
     * @return the test result / 检验结果
     * @throws MathException if data is null, has fewer than 2 elements, or contains NaN/Infinity
     */
    public static TestResult twoSample(double[] x, double[] y) {
        validateArrayBasic(x, "x", 2);
        validateArrayBasic(y, "y", 2);

        // Single-pass mean + variance for each array (fused validation + Welford)
        int nX = x.length;
        int nY = y.length;
        double meanX = 0.0, m2X = 0.0;
        for (int i = 0; i < nX; i++) {
            requireFiniteElement(x[i], "x", i);
            double delta = x[i] - meanX;
            meanX += delta / (i + 1);
            m2X += delta * (x[i] - meanX);
        }
        double varX = m2X / (nX - 1);

        double meanY = 0.0, m2Y = 0.0;
        for (int i = 0; i < nY; i++) {
            requireFiniteElement(y[i], "y", i);
            double delta = y[i] - meanY;
            meanY += delta / (i + 1);
            m2Y += delta * (y[i] - meanY);
        }
        double varY = m2Y / (nY - 1);

        double sErrSq = varX / nX + varY / nY;

        if (sErrSq == 0.0) {
            double t = (meanX == meanY) ? 0.0 : Double.POSITIVE_INFINITY * Math.signum(meanX - meanY);
            double p = (meanX == meanY) ? 1.0 : 0.0;
            return new TestResult("Two-Sample T-Test (Welch)", t, p, Double.NaN);
        }

        double t = (meanX - meanY) / Math.sqrt(sErrSq);

        // Welch-Satterthwaite degrees of freedom
        double vxn = varX / nX;
        double vyn = varY / nY;
        double numerator = (vxn + vyn) * (vxn + vyn);
        double denominator = (vxn * vxn) / (nX - 1) + (vyn * vyn) / (nY - 1);
        double df = numerator / denominator;

        double p = twoTailedPValue(t, df);

        return new TestResult("Two-Sample T-Test (Welch)", t, p, df);
    }

    /**
     * Performs a paired t-test (H0: mean difference = 0).
     * 执行配对 t 检验（H0: 平均差值 = 0）
     *
     * <p>Computes differences d = x - y, then performs a one-sample t-test on d with mu0 = 0.</p>
     * <p>计算差值 d = x - y，然后对 d 执行 mu0 = 0 的单样本 t 检验。</p>
     *
     * @param x first sample data / 第一组样本数据
     * @param y second sample data (same length as x) / 第二组样本数据（与 x 等长）
     * @return the test result / 检验结果
     * @throws MathException if arrays are null, different lengths, have fewer than 2 elements, or contain NaN/Infinity
     */
    public static TestResult paired(double[] x, double[] y) {
        validateArrayBasic(x, "x", 2);
        validateArrayBasic(y, "y", 2);
        if (x.length != y.length) {
            throw new MathException("Paired t-test requires equal-length arrays, got: "
                    + x.length + " and " + y.length);
        }

        // Single-pass Welford on differences (no temp array, no re-validation)
        int n = x.length;
        double mean = 0.0;
        double m2 = 0.0;
        for (int i = 0; i < n; i++) {
            requireFiniteElement(x[i], "x", i);
            requireFiniteElement(y[i], "y", i);
            double di = x[i] - y[i];
            double delta = di - mean;
            mean += delta / (i + 1);
            m2 += delta * (di - mean);
        }
        double sampleVar = m2 / (n - 1);
        double s = Math.sqrt(sampleVar);

        if (s == 0.0) {
            double t = (mean == 0.0) ? 0.0 : Double.POSITIVE_INFINITY * Math.signum(mean);
            double p = (mean == 0.0) ? 1.0 : 0.0;
            return new TestResult("Paired T-Test", t, p, n - 1.0);
        }

        double t = mean / (s / Math.sqrt(n));
        double df = n - 1.0;
        double p = twoTailedPValue(t, df);
        return new TestResult("Paired T-Test", t, p, df);
    }

    // ==================== CDF for t-distribution ====================

    /**
     * Computes the CDF of the t-distribution using regularized beta function.
     * Uses: cdf(t, df) = 1 - 0.5 * I(df/(df+t^2), df/2, 0.5) for t >= 0
     *       cdf(t, df) = 0.5 * I(df/(df+t^2), df/2, 0.5) for t < 0
     */
    static double tCdf(double t, double df) {
        if (df <= 0) {
            throw new MathException("Degrees of freedom must be positive, got: " + df);
        }
        if (t == 0.0) {
            return 0.5;
        }
        double x = df / (df + t * t);
        double iBeta = SpecialFunctions.regularizedBeta(x, df / 2.0, 0.5);
        return t >= 0 ? 1.0 - 0.5 * iBeta : 0.5 * iBeta;
    }

    /**
     * Computes a two-tailed p-value from the t-distribution.
     */
    static double twoTailedPValue(double t, double df) {
        double cdf = tCdf(Math.abs(t), df);
        return 2.0 * (1.0 - cdf);
    }

    // ==================== Internal helpers ====================

    /**
     * Null + length check only. Finite checks are inlined into computation loops.
     */
    private static void validateArrayBasic(double[] array, String name, int minLength) {
        if (array == null) {
            throw new MathException(name + " must not be null");
        }
        if (array.length < minLength) {
            throw new MathException(name + " requires at least " + minLength
                    + " elements, got: " + array.length);
        }
    }

    private static void requireFiniteElement(double value, String name, int index) {
        if (!Double.isFinite(value)) {
            throw new MathException(name + "[" + index + "] is not finite: " + value);
        }
    }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new MathException(name + " must be finite, got: " + value);
        }
    }
}
