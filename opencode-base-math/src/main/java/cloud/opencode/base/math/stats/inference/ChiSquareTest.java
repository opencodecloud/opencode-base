package cloud.opencode.base.math.stats.inference;

import cloud.opencode.base.math.exception.MathException;
import cloud.opencode.base.math.special.SpecialFunctions;

/**
 * Chi-squared test implementations for hypothesis testing.
 * 卡方检验实现，用于假设检验
 *
 * <p>Provides goodness-of-fit and independence tests.
 * All methods return a {@link TestResult} with p-values computed from the chi-squared distribution.</p>
 * <p>提供拟合优度检验和独立性检验。
 * 所有方法返回从卡方分布计算 p 值的 {@link TestResult}。</p>
 *
 * <p>All methods are stateless and thread-safe.</p>
 * <p>所有方法无状态且线程安全。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class ChiSquareTest {

    private ChiSquareTest() {
        throw new AssertionError("No instances");
    }

    /**
     * Performs a chi-squared goodness-of-fit test.
     * 执行卡方拟合优度检验
     *
     * <p>Tests whether observed frequencies differ significantly from expected frequencies.
     * Statistic = sum((O - E)^2 / E), df = k - 1.</p>
     * <p>检验观察频率与期望频率是否存在显著差异。
     * 统计量 = sum((O - E)^2 / E)，df = k - 1。</p>
     *
     * @param observed the observed frequencies (must all be non-negative) / 观察频率（必须全部非负）
     * @param expected the expected frequencies (must all be positive) / 期望频率（必须全部为正）
     * @return the test result / 检验结果
     * @throws MathException if arrays are null, different lengths, fewer than 2 categories,
     *                       or contain invalid values
     */
    public static TestResult goodnessOfFit(double[] observed, double[] expected) {
        requireNonNull(observed, "observed");
        requireNonNull(expected, "expected");
        if (observed.length != expected.length) {
            throw new MathException("observed and expected arrays must have the same length, got: "
                    + observed.length + " and " + expected.length);
        }
        if (observed.length < 2) {
            throw new MathException("Goodness-of-fit test requires at least 2 categories, got: "
                    + observed.length);
        }

        double chiSquare = 0.0;
        for (int i = 0; i < observed.length; i++) {
            if (!Double.isFinite(observed[i]) || observed[i] < 0) {
                throw new MathException("observed[" + i + "] must be non-negative and finite, got: " + observed[i]);
            }
            if (!Double.isFinite(expected[i]) || expected[i] <= 0) {
                throw new MathException("expected[" + i + "] must be positive and finite, got: " + expected[i]);
            }
            double diff = observed[i] - expected[i];
            chiSquare += (diff * diff) / expected[i];
        }

        double df = observed.length - 1.0;
        double p = chiSquaredPValue(chiSquare, df);

        return new TestResult("Chi-Squared Goodness-of-Fit", chiSquare, p, df);
    }

    /**
     * Performs a chi-squared test of independence on a contingency table.
     * 对列联表执行卡方独立性检验
     *
     * <p>Tests whether two categorical variables are independent.
     * df = (rows - 1) * (cols - 1).</p>
     * <p>检验两个分类变量是否独立。
     * df = (rows - 1) * (cols - 1)。</p>
     *
     * @param contingencyTable the contingency table (at least 2x2, all values non-negative) /
     *                         列联表（至少 2x2，所有值非负）
     * @return the test result / 检验结果
     * @throws MathException if table is null, too small, jagged, or contains invalid values
     */
    public static TestResult independence(double[][] contingencyTable) {
        if (contingencyTable == null) {
            throw new MathException("contingencyTable must not be null");
        }
        int rows = contingencyTable.length;
        if (rows < 2) {
            throw new MathException("Contingency table must have at least 2 rows, got: " + rows);
        }
        int cols = contingencyTable[0].length;
        if (cols < 2) {
            throw new MathException("Contingency table must have at least 2 columns, got: " + cols);
        }

        // Validate and compute totals
        double[] rowTotals = new double[rows];
        double[] colTotals = new double[cols];
        double grandTotal = 0.0;

        for (int i = 0; i < rows; i++) {
            if (contingencyTable[i] == null || contingencyTable[i].length != cols) {
                throw new MathException("Contingency table must be rectangular; row " + i
                        + " has " + (contingencyTable[i] == null ? "null" : contingencyTable[i].length)
                        + " columns, expected " + cols);
            }
            for (int j = 0; j < cols; j++) {
                double val = contingencyTable[i][j];
                if (!Double.isFinite(val) || val < 0) {
                    throw new MathException("contingencyTable[" + i + "][" + j
                            + "] must be non-negative and finite, got: " + val);
                }
                rowTotals[i] += val;
                colTotals[j] += val;
                grandTotal += val;
            }
        }

        if (grandTotal == 0.0) {
            throw new MathException("Contingency table grand total must be positive");
        }

        // Compute chi-squared statistic
        double chiSquare = 0.0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double expected = rowTotals[i] * colTotals[j] / grandTotal;
                if (expected == 0.0) {
                    // Skip cells where expected is zero (structural zero)
                    continue;
                }
                double diff = contingencyTable[i][j] - expected;
                chiSquare += (diff * diff) / expected;
            }
        }

        double df = (rows - 1.0) * (cols - 1.0);
        double p = chiSquaredPValue(chiSquare, df);

        return new TestResult("Chi-Squared Test of Independence", chiSquare, p, df);
    }

    // ==================== Chi-squared distribution CDF ====================

    /**
     * Computes p-value from chi-squared distribution: P(X > chiSq) = 1 - CDF(chiSq).
     * CDF = regularizedGammaP(df/2, x/2).
     */
    static double chiSquaredPValue(double chiSq, double df) {
        if (chiSq == 0.0) {
            return 1.0;
        }
        double cdf = SpecialFunctions.regularizedGammaP(df / 2.0, chiSq / 2.0);
        return 1.0 - cdf;
    }

    private static void requireNonNull(double[] array, String name) {
        if (array == null) {
            throw new MathException(name + " must not be null");
        }
    }
}
