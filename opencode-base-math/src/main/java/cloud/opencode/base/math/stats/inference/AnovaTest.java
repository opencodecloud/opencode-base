package cloud.opencode.base.math.stats.inference;

import cloud.opencode.base.math.exception.MathException;
import cloud.opencode.base.math.special.SpecialFunctions;

/**
 * One-way Analysis of Variance (ANOVA) F-test implementation.
 * 单因素方差分析（ANOVA）F 检验实现
 *
 * <p>Tests whether the means of two or more groups are equal.
 * Returns a {@link TestResult} with the F-statistic and p-value from the F-distribution.</p>
 * <p>检验两个或多个组的均值是否相等。
 * 返回包含 F 统计量和来自 F 分布 p 值的 {@link TestResult}。</p>
 *
 * <p>All methods are stateless and thread-safe.</p>
 * <p>所有方法无状态且线程安全。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class AnovaTest {

    private AnovaTest() {
        throw new AssertionError("No instances");
    }

    /**
     * Performs a one-way ANOVA F-test.
     * 执行单因素方差分析 F 检验
     *
     * <p>H0: all group means are equal. H1: at least one group mean differs.</p>
     * <p>H0: 所有组均值相等。H1: 至少一个组均值不同。</p>
     *
     * <p>F = MSB / MSW where MSB = SSB / (k-1) and MSW = SSW / (N-k).
     * p-value is computed from the F-distribution with df1 = k-1 and df2 = N-k.</p>
     * <p>F = MSB / MSW，其中 MSB = SSB / (k-1)，MSW = SSW / (N-k)。
     * p 值由 df1 = k-1、df2 = N-k 的 F 分布计算。</p>
     *
     * @param groups two or more groups, each with at least 2 elements / 两个或更多组，每组至少 2 个元素
     * @return the test result / 检验结果
     * @throws MathException if fewer than 2 groups, any group has fewer than 2 elements,
     *                       or data contains NaN/Infinity
     */
    public static TestResult oneWay(double[]... groups) {
        if (groups == null) {
            throw new MathException("groups must not be null");
        }
        int k = groups.length;
        if (k < 2) {
            throw new MathException("ANOVA requires at least 2 groups, got: " + k);
        }

        int totalN = 0;
        double grandSum = 0.0;

        // Validate all groups and compute grand sum
        for (int g = 0; g < k; g++) {
            if (groups[g] == null) {
                throw new MathException("groups[" + g + "] must not be null");
            }
            if (groups[g].length < 2) {
                throw new MathException("groups[" + g + "] requires at least 2 elements, got: "
                        + groups[g].length);
            }
            for (int i = 0; i < groups[g].length; i++) {
                if (!Double.isFinite(groups[g][i])) {
                    throw new MathException("groups[" + g + "][" + i + "] is not finite: " + groups[g][i]);
                }
                grandSum += groups[g][i];
            }
            totalN += groups[g].length;
        }

        double grandMean = grandSum / totalN;

        // Compute SSB (between-group sum of squares) and SSW (within-group sum of squares)
        // Uses two-pass algorithm per group for numerical stability:
        // Pass 1 computes groupMean, Pass 2 computes sum of squared deviations.
        // NOTE: The naive one-pass formula (Σv² - n·mean²) suffers from catastrophic
        // cancellation when values are large with small variance — do NOT use it here.
        double ssb = 0.0;
        double ssw = 0.0;

        for (double[] group : groups) {
            double groupSum = 0.0;
            for (double v : group) {
                groupSum += v;
            }
            double groupMean = groupSum / group.length;
            double diffMean = groupMean - grandMean;
            ssb += group.length * diffMean * diffMean;

            for (double v : group) {
                double diff = v - groupMean;
                ssw += diff * diff;
            }
        }

        double df1 = k - 1.0;
        double df2 = totalN - k;

        if (df2 <= 0) {
            throw new MathException("Insufficient total observations for ANOVA (N - k must be > 0)");
        }

        double msb = ssb / df1;
        double msw = ssw / df2;

        if (msw == 0.0) {
            // All values within each group are identical
            double f = (ssb == 0.0) ? 0.0 : Double.POSITIVE_INFINITY;
            double p = (ssb == 0.0) ? 1.0 : 0.0;
            return new TestResult("One-Way ANOVA F-Test", f, p, df1);
        }

        double f = msb / msw;
        double p = fPValue(f, df1, df2);

        return new TestResult("One-Way ANOVA F-Test", f, p, df1);
    }

    // ==================== F-distribution CDF ====================

    /**
     * Computes p-value from the F-distribution: P(X > f) = 1 - CDF(f).
     * CDF(f) = regularizedBeta(df1*f/(df1*f+df2), df1/2, df2/2).
     */
    static double fPValue(double f, double df1, double df2) {
        if (f <= 0.0) {
            return 1.0;
        }
        double x = df1 * f / (df1 * f + df2);
        double cdf = SpecialFunctions.regularizedBeta(x, df1 / 2.0, df2 / 2.0);
        return 1.0 - cdf;
    }
}
