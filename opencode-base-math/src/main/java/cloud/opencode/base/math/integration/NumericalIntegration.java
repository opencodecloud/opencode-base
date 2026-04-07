package cloud.opencode.base.math.integration;

import cloud.opencode.base.math.exception.MathException;

import java.util.function.DoubleUnaryOperator;

/**
 * Numerical integration utility class providing common quadrature algorithms.
 * 数值积分工具类，提供常见求积算法。
 *
 * <p>All methods are stateless and thread-safe. Supported algorithms:</p>
 * <p>所有方法无状态且线程安全。支持的算法：</p>
 * <ul>
 *   <li>Composite trapezoidal rule — 复合梯形法</li>
 *   <li>Composite Simpson's 1/3 rule — 复合辛普森 1/3 法</li>
 *   <li>Simpson's 3/8 rule — 辛普森 3/8 法</li>
 *   <li>Romberg integration — 龙贝格积分</li>
 *   <li>Gauss-Legendre quadrature (2–5 points) — 高斯-勒让德求积（2–5 点）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class NumericalIntegration {

    private NumericalIntegration() {
        throw new AssertionError("No instances");
    }

    // ---- Gauss-Legendre nodes and weights on [-1, 1] ----

    private static final double[][] GL_NODES = {
            // 2-point
            {-1.0 / Math.sqrt(3.0), 1.0 / Math.sqrt(3.0)},
            // 3-point
            {-Math.sqrt(3.0 / 5.0), 0.0, Math.sqrt(3.0 / 5.0)},
            // 4-point
            {
                    -Math.sqrt((3.0 + 2.0 * Math.sqrt(6.0 / 5.0)) / 7.0),
                    -Math.sqrt((3.0 - 2.0 * Math.sqrt(6.0 / 5.0)) / 7.0),
                    Math.sqrt((3.0 - 2.0 * Math.sqrt(6.0 / 5.0)) / 7.0),
                    Math.sqrt((3.0 + 2.0 * Math.sqrt(6.0 / 5.0)) / 7.0)
            },
            // 5-point
            {
                    -Math.sqrt(5.0 + 2.0 * Math.sqrt(10.0 / 7.0)) / 3.0,
                    -Math.sqrt(5.0 - 2.0 * Math.sqrt(10.0 / 7.0)) / 3.0,
                    0.0,
                    Math.sqrt(5.0 - 2.0 * Math.sqrt(10.0 / 7.0)) / 3.0,
                    Math.sqrt(5.0 + 2.0 * Math.sqrt(10.0 / 7.0)) / 3.0
            }
    };

    private static final double[][] GL_WEIGHTS;

    static {
        double w4a = (18.0 - Math.sqrt(30.0)) / 36.0;
        double w4b = (18.0 + Math.sqrt(30.0)) / 36.0;

        double w5a = (322.0 - 13.0 * Math.sqrt(70.0)) / 900.0;
        double w5b = (322.0 + 13.0 * Math.sqrt(70.0)) / 900.0;
        double w5c = 128.0 / 225.0;

        GL_WEIGHTS = new double[][]{
                // 2-point
                {1.0, 1.0},
                // 3-point
                {5.0 / 9.0, 8.0 / 9.0, 5.0 / 9.0},
                // 4-point
                {w4a, w4b, w4b, w4a},
                // 5-point
                {w5a, w5b, w5c, w5b, w5a}
        };
    }

    /**
     * Composite trapezoidal rule.
     * 复合梯形法。
     *
     * <p>Approximates the definite integral of f over [a, b] using n equal subintervals.
     * If a &gt; b, the result is negated (orientation convention).</p>
     * <p>使用 n 个等距子区间近似 f 在 [a, b] 上的定积分。
     * 若 a &gt; b，结果取反（方向约定）。</p>
     *
     * @param f the integrand — 被积函数
     * @param a the lower bound — 积分下限
     * @param b the upper bound — 积分上限
     * @param n the number of subintervals (&ge; 1) — 子区间数（&ge; 1）
     * @return the approximate integral value — 近似积分值
     * @throws MathException if inputs are invalid — 输入无效时抛出
     */
    public static double trapezoid(DoubleUnaryOperator f, double a, double b, int n) {
        validateFunction(f);
        validateBounds(a, b);
        validateSubintervals(n, 1);
        if (Double.compare(a, b) == 0) {
            return 0.0;
        }

        boolean negated = false;
        double lo = a, hi = b;
        if (a > b) {
            lo = b;
            hi = a;
            negated = true;
        }

        double h = (hi - lo) / n;
        double sum = 0.5 * (f.applyAsDouble(lo) + f.applyAsDouble(hi));
        for (int i = 1; i < n; i++) {
            sum += f.applyAsDouble(lo + i * h);
        }
        double result = sum * h;
        return negated ? -result : result;
    }

    /**
     * Composite Simpson's 1/3 rule.
     * 复合辛普森 1/3 法。
     *
     * <p>Approximates the definite integral using n subintervals.
     * n must be even. Exact for polynomials up to degree 3.</p>
     * <p>使用 n 个子区间近似定积分。n 必须为偶数。对 3 阶及以下多项式精确。</p>
     *
     * @param f the integrand — 被积函数
     * @param a the lower bound — 积分下限
     * @param b the upper bound — 积分上限
     * @param n the number of subintervals (must be even, &ge; 2) — 子区间数（必须为偶数，&ge; 2）
     * @return the approximate integral value — 近似积分值
     * @throws MathException if inputs are invalid — 输入无效时抛出
     */
    public static double simpson(DoubleUnaryOperator f, double a, double b, int n) {
        validateFunction(f);
        validateBounds(a, b);
        validateSubintervals(n, 2);
        if (n % 2 != 0) {
            throw new MathException(
                    "n must be even for Simpson's 1/3 rule, got " + n
                            + " / 辛普森 1/3 法要求 n 为偶数，实际 " + n
            );
        }
        if (Double.compare(a, b) == 0) {
            return 0.0;
        }

        boolean negated = false;
        double lo = a, hi = b;
        if (a > b) {
            lo = b;
            hi = a;
            negated = true;
        }

        double h = (hi - lo) / n;
        double sum = f.applyAsDouble(lo) + f.applyAsDouble(hi);

        // Unrolled loop: step by 2 to avoid modulo check per iteration
        for (int i = 1; i < n; i += 2) {
            sum += 4.0 * f.applyAsDouble(lo + i * h);
            if (i + 1 < n) {
                sum += 2.0 * f.applyAsDouble(lo + (i + 1) * h);
            }
        }
        double result = sum * h / 3.0;
        return negated ? -result : result;
    }

    /**
     * Simpson's 3/8 rule (composite).
     * 辛普森 3/8 法（复合）。
     *
     * <p>Approximates the definite integral using n subintervals.
     * n must be divisible by 3.</p>
     * <p>使用 n 个子区间近似定积分。n 必须能被 3 整除。</p>
     *
     * @param f the integrand — 被积函数
     * @param a the lower bound — 积分下限
     * @param b the upper bound — 积分上限
     * @param n the number of subintervals (must be divisible by 3, &ge; 3) — 子区间数（必须能被 3 整除，&ge; 3）
     * @return the approximate integral value — 近似积分值
     * @throws MathException if inputs are invalid — 输入无效时抛出
     */
    public static double simpsonThreeEighths(DoubleUnaryOperator f, double a, double b, int n) {
        validateFunction(f);
        validateBounds(a, b);
        validateSubintervals(n, 3);
        if (n % 3 != 0) {
            throw new MathException(
                    "n must be divisible by 3 for Simpson's 3/8 rule, got " + n
                            + " / 辛普森 3/8 法要求 n 能被 3 整除，实际 " + n
            );
        }
        if (Double.compare(a, b) == 0) {
            return 0.0;
        }

        boolean negated = false;
        double lo = a, hi = b;
        if (a > b) {
            lo = b;
            hi = a;
            negated = true;
        }

        double h = (hi - lo) / n;
        double sum = f.applyAsDouble(lo) + f.applyAsDouble(hi);

        // Unrolled loop: step by 3 to avoid modulo check per iteration
        for (int i = 1; i < n; i += 3) {
            sum += 3.0 * f.applyAsDouble(lo + i * h);
            sum += 3.0 * f.applyAsDouble(lo + (i + 1) * h);
            if (i + 2 < n) {
                sum += 2.0 * f.applyAsDouble(lo + (i + 2) * h);
            }
        }
        double result = sum * 3.0 * h / 8.0;
        return negated ? -result : result;
    }

    /**
     * Romberg integration using Richardson extrapolation.
     * 基于理查森外推的龙贝格积分。
     *
     * <p>Iteratively refines the trapezoidal rule estimate using Richardson extrapolation
     * until the desired tolerance is achieved or maxIterations is reached.</p>
     * <p>通过理查森外推迭代改进梯形法估计，直到达到所需精度或最大迭代次数。</p>
     *
     * @param f             the integrand — 被积函数
     * @param a             the lower bound — 积分下限
     * @param b             the upper bound — 积分上限
     * @param maxIterations maximum number of Romberg iterations (&ge; 1) — 最大迭代次数（&ge; 1）
     * @param tolerance     convergence tolerance (&gt; 0) — 收敛容差（&gt; 0）
     * @return the approximate integral value — 近似积分值
     * @throws MathException if inputs are invalid — 输入无效时抛出
     */
    public static double romberg(DoubleUnaryOperator f, double a, double b,
                                 int maxIterations, double tolerance) {
        validateFunction(f);
        validateBounds(a, b);
        if (maxIterations < 1) {
            throw new MathException(
                    "maxIterations must be >= 1, got " + maxIterations
                            + " / maxIterations 必须 >= 1，实际 " + maxIterations
            );
        }
        if (tolerance <= 0 || Double.isNaN(tolerance)) {
            throw new MathException(
                    "tolerance must be > 0, got " + tolerance
                            + " / tolerance 必须 > 0，实际 " + tolerance
            );
        }
        if (Double.compare(a, b) == 0) {
            return 0.0;
        }

        boolean negated = false;
        double lo = a, hi = b;
        if (a > b) {
            lo = b;
            hi = a;
            negated = true;
        }

        // Cap maxIterations to prevent 1<<i overflow (int: max 30)
        int safeMaxIter = Math.min(maxIterations, 30);

        // Rolling 2-row Romberg table to save memory
        double[] prevRow = new double[safeMaxIter];
        double[] currRow = new double[safeMaxIter];

        // R[0][0] = basic trapezoidal rule with 1 interval
        prevRow[0] = 0.5 * (hi - lo) * (f.applyAsDouble(lo) + f.applyAsDouble(hi));

        for (int i = 1; i < safeMaxIter; i++) {
            // Composite trapezoidal with 2^i subintervals
            int segments = 1 << i;
            double h = (hi - lo) / segments;

            // Add new midpoints: only the odd-indexed points are new
            double sum = 0.0;
            for (int k = 1; k <= segments; k += 2) {
                sum += f.applyAsDouble(lo + k * h);
            }
            currRow[0] = 0.5 * prevRow[0] + h * sum;

            // Richardson extrapolation (iterative 4^j to avoid expensive Math.pow)
            double fourJ = 4.0;
            for (int j = 1; j <= i; j++) {
                currRow[j] = currRow[j - 1] + (currRow[j - 1] - prevRow[j - 1]) / (fourJ - 1);
                fourJ *= 4.0;
            }

            // Check convergence
            if (Math.abs(currRow[i] - prevRow[i - 1]) < tolerance) {
                double result = currRow[i];
                return negated ? -result : result;
            }

            // Swap rows
            double[] tmp = prevRow;
            prevRow = currRow;
            currRow = tmp;
        }

        double result = prevRow[safeMaxIter - 1];
        return negated ? -result : result;
    }

    /**
     * Gauss-Legendre quadrature.
     * 高斯-勒让德求积。
     *
     * <p>Uses pre-computed nodes and weights for 2, 3, 4, or 5 point Gauss-Legendre
     * quadrature on the interval [a, b]. A 2n-point rule is exact for polynomials
     * up to degree 2n-1.</p>
     * <p>使用预计算的 2、3、4 或 5 点高斯-勒让德求积节点和权重在 [a, b] 上积分。
     * n 点规则对 2n-1 阶及以下多项式精确。</p>
     *
     * @param f      the integrand — 被积函数
     * @param a      the lower bound — 积分下限
     * @param b      the upper bound — 积分上限
     * @param points number of quadrature points (2, 3, 4, or 5) — 求积点数（2、3、4 或 5）
     * @return the approximate integral value — 近似积分值
     * @throws MathException if inputs are invalid — 输入无效时抛出
     */
    public static double gaussLegendre(DoubleUnaryOperator f, double a, double b, int points) {
        validateFunction(f);
        if (points < 2 || points > 5) {
            throw new MathException(
                    "Gauss-Legendre supports 2-5 points, got " + points
                            + " / 高斯-勒让德求积支持 2-5 点，实际 " + points
            );
        }
        if (Double.isNaN(a) || Double.isInfinite(a)) {
            throw new MathException("Lower bound a must be finite / 积分下限 a 必须有限");
        }
        if (Double.isNaN(b) || Double.isInfinite(b)) {
            throw new MathException("Upper bound b must be finite / 积分上限 b 必须有限");
        }
        if (Double.compare(a, b) == 0) {
            return 0.0;
        }

        boolean negated = false;
        double lo = a, hi = b;
        if (a > b) {
            lo = b;
            hi = a;
            negated = true;
        }

        double[] nodes = GL_NODES[points - 2];
        double[] weights = GL_WEIGHTS[points - 2];

        // Transform from [-1, 1] to [lo, hi]:
        // x = ((hi - lo) * t + (hi + lo)) / 2
        // dx = (hi - lo) / 2 * dt
        double halfLen = (hi - lo) / 2.0;
        double midpoint = (hi + lo) / 2.0;

        double sum = 0.0;
        for (int i = 0; i < points; i++) {
            double xi = halfLen * nodes[i] + midpoint;
            sum += weights[i] * f.applyAsDouble(xi);
        }
        double result = halfLen * sum;
        return negated ? -result : result;
    }

    // ---- Validation helpers ----

    private static void validateFunction(DoubleUnaryOperator f) {
        if (f == null) {
            throw new MathException("Function must not be null / 函数不能为 null");
        }
    }

    private static void validateBounds(double a, double b) {
        if (Double.isNaN(a) || Double.isInfinite(a)) {
            throw new MathException("Lower bound must be finite, got: " + a);
        }
        if (Double.isNaN(b) || Double.isInfinite(b)) {
            throw new MathException("Upper bound must be finite, got: " + b);
        }
    }

    private static final int MAX_SUBINTERVALS = 10_000_000;

    private static void validateSubintervals(int n, int min) {
        if (n < min) {
            throw new MathException(
                    "Number of subintervals must be >= " + min + ", got " + n
                            + " / 子区间数必须 >= " + min + "，实际 " + n
            );
        }
        if (n > MAX_SUBINTERVALS) {
            throw new MathException(
                    "Number of subintervals must be <= " + MAX_SUBINTERVALS + ", got " + n
                            + " / 子区间数必须 <= " + MAX_SUBINTERVALS + "，实际 " + n
            );
        }
    }
}
