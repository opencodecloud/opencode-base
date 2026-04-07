package cloud.opencode.base.math.interpolation;

import cloud.opencode.base.math.exception.MathException;

import java.util.Arrays;

/**
 * Interpolation utility class providing common interpolation algorithms.
 * 插值工具类，提供常见插值算法。
 *
 * <p>All methods are stateless and thread-safe. Supported algorithms:</p>
 * <p>所有方法无状态且线程安全。支持的算法：</p>
 * <ul>
 *   <li>Piecewise linear interpolation — 分段线性插值</li>
 *   <li>Lagrange polynomial interpolation — 拉格朗日多项式插值</li>
 *   <li>Newton's divided difference interpolation — 牛顿差商插值</li>
 *   <li>Natural cubic spline interpolation — 自然三次样条插值</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class Interpolation {

    private Interpolation() {
        throw new AssertionError("No instances");
    }

    /**
     * Piecewise linear interpolation using binary search.
     * 使用二分查找的分段线性插值。
     *
     * <p>The x array must be sorted in strictly ascending order and xi must lie
     * within [{@code x[0]}, {@code x[x.length - 1]}].</p>
     * <p>x 数组必须严格升序排列，xi 必须在 [{@code x[0]}, {@code x[x.length - 1]}] 范围内。</p>
     *
     * @param x  the x-coordinates of the data points (sorted ascending) — 数据点的 x 坐标（升序）
     * @param y  the y-coordinates of the data points — 数据点的 y 坐标
     * @param xi the x value at which to interpolate — 待插值的 x 值
     * @return the interpolated y value — 插值后的 y 值
     * @throws MathException if inputs are invalid — 输入无效时抛出
     */
    public static double linear(double[] x, double[] y, double xi) {
        validateInputs(x, y, 2);
        validateSorted(x);
        if (xi < x[0] || xi > x[x.length - 1]) {
            throw new MathException(
                    "xi=" + xi + " is out of range [" + x[0] + ", " + x[x.length - 1] + "]"
            );
        }

        // Exact match at endpoints
        if (Double.compare(xi, x[0]) == 0) {
            return y[0];
        }
        if (Double.compare(xi, x[x.length - 1]) == 0) {
            return y[x.length - 1];
        }

        int idx = Arrays.binarySearch(x, xi);
        if (idx >= 0) {
            return y[idx];
        }

        // insertion point: -(idx) - 1, so the interval is [insertionPoint-1, insertionPoint]
        int i = -(idx + 1) - 1;
        double t = (xi - x[i]) / (x[i + 1] - x[i]);
        return y[i] + t * (y[i + 1] - y[i]);
    }

    /**
     * Lagrange polynomial interpolation.
     * 拉格朗日多项式插值。
     *
     * <p>The x values must all be distinct. The interpolating polynomial passes through
     * all data points and is exact for polynomials of degree &le; n-1.</p>
     * <p>x 值必须互不相同。插值多项式通过所有数据点，对 &le; n-1 阶多项式精确。</p>
     *
     * @param x  the x-coordinates of the data points (must be distinct) — 数据点的 x 坐标（必须互不相同）
     * @param y  the y-coordinates of the data points — 数据点的 y 坐标
     * @param xi the x value at which to interpolate — 待插值的 x 值
     * @return the interpolated y value — 插值后的 y 值
     * @throws MathException if inputs are invalid — 输入无效时抛出
     */
    public static double lagrange(double[] x, double[] y, double xi) {
        validateInputs(x, y, 2);
        validateDistinct(x);

        int n = x.length;

        // Barycentric interpolation: O(n) precompute weights, O(n) per evaluation
        // w[i] = 1 / product_{j≠i} (x[i] - x[j])
        double[] w = new double[n];
        for (int i = 0; i < n; i++) {
            double product = 1.0;
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    product *= (x[i] - x[j]);
                }
            }
            w[i] = 1.0 / product;
        }

        // Evaluate using barycentric formula (second form):
        // f(xi) = sum(w[i]*y[i]/(xi-x[i])) / sum(w[i]/(xi-x[i]))
        double numerator = 0.0;
        double denominator = 0.0;
        for (int i = 0; i < n; i++) {
            double diff = xi - x[i];
            if (diff == 0.0) {
                return y[i]; // exact match at node
            }
            double term = w[i] / diff;
            numerator += term * y[i];
            denominator += term;
        }
        return numerator / denominator;
    }

    /**
     * Newton's divided difference interpolation.
     * 牛顿差商插值。
     *
     * <p>Constructs the divided difference table and evaluates using the Newton form.
     * The x values must all be distinct.</p>
     * <p>构建差商表并使用牛顿形式求值。x 值必须互不相同。</p>
     *
     * @param x  the x-coordinates of the data points (must be distinct) — 数据点的 x 坐标（必须互不相同）
     * @param y  the y-coordinates of the data points — 数据点的 y 坐标
     * @param xi the x value at which to interpolate — 待插值的 x 值
     * @return the interpolated y value — 插值后的 y 值
     * @throws MathException if inputs are invalid — 输入无效时抛出
     */
    public static double newtonDividedDifference(double[] x, double[] y, double xi) {
        validateInputs(x, y, 2);
        validateDistinct(x);

        int n = x.length;

        // Build divided difference table (copy y to avoid mutation)
        double[] coeff = y.clone();
        for (int j = 1; j < n; j++) {
            for (int i = n - 1; i >= j; i--) {
                coeff[i] = (coeff[i] - coeff[i - 1]) / (x[i] - x[i - j]);
            }
        }

        // Evaluate using Horner's method (nested form)
        double result = coeff[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            result = result * (xi - x[i]) + coeff[i];
        }
        return result;
    }

    /**
     * Precomputed cubic spline coefficients for efficient multi-point evaluation.
     * 预计算的三次样条系数，用于高效多点求值。
     *
     * @param x     the x-coordinates / x 坐标
     * @param h     the interval widths / 区间宽度
     * @param a     coefficient a (y values) / 系数 a（y 值）
     * @param b     coefficient b / 系数 b
     * @param c     coefficient c / 系数 c
     * @param d     coefficient d / 系数 d
     * @since JDK 25, opencode-base-math V1.0.3
     */
    public record SplineCoefficients(double[] x, double[] h, double[] a,
                                     double[] b, double[] c, double[] d) {

        /**
         * Evaluates the precomputed spline at the given x value.
         * 在给定 x 值处求值预计算的样条。
         *
         * @param xi the x value at which to evaluate — 待求值的 x 值
         * @return the interpolated y value — 插值后的 y 值
         * @throws MathException if xi is out of range — xi 超出范围时抛出
         */
        public double evaluate(double xi) {
            if (xi < x[0] || xi > x[x.length - 1]) {
                throw new MathException(
                        "xi=" + xi + " is out of range [" + x[0] + ", " + x[x.length - 1] + "]"
                );
            }
            int m = x.length - 1;
            int idx = Arrays.binarySearch(x, xi);
            int i;
            if (idx >= 0) {
                i = Math.min(idx, m - 1);
            } else {
                i = -(idx + 1) - 1;
                i = Math.max(0, Math.min(i, m - 1));
            }
            double dx = xi - x[i];
            return a[i] + dx * (b[i] + dx * (c[i] + dx * d[i]));
        }
    }

    /**
     * Precomputes cubic spline coefficients for efficient multi-point evaluation.
     * 预计算三次样条系数，用于高效多点求值。
     *
     * <p>Call this once per dataset, then use {@link SplineCoefficients#evaluate(double)}
     * for each query point. This avoids redundant O(n) precomputation per query.</p>
     * <p>对每个数据集调用一次，然后对每个查询点使用 {@link SplineCoefficients#evaluate(double)}。
     * 这避免了每次查询的冗余 O(n) 预计算。</p>
     *
     * @param x the x-coordinates of the data points (sorted ascending) — 数据点的 x 坐标（升序）
     * @param y the y-coordinates of the data points — 数据点的 y 坐标
     * @return precomputed spline coefficients — 预计算的样条系数
     * @throws MathException if inputs are invalid — 输入无效时抛出
     */
    public static SplineCoefficients precomputeSpline(double[] x, double[] y) {
        validateInputs(x, y, 2);
        validateSorted(x);
        return buildSplineCoefficients(x, y);
    }

    /**
     * Natural cubic spline interpolation (second derivative = 0 at endpoints).
     * 自然三次样条插值（端点处二阶导数为 0）。
     *
     * <p>Constructs a natural cubic spline that passes through all data points with
     * continuous first and second derivatives. The x array must be sorted in
     * strictly ascending order and xi must lie within [{@code x[0]}, {@code x[n-1]}].</p>
     * <p>构建通过所有数据点的自然三次样条，保证一阶和二阶导数连续。
     * x 数组必须严格升序排列，xi 必须在 [{@code x[0]}, {@code x[n-1]}] 范围内。</p>
     *
     * <p>For multiple evaluations on the same dataset, prefer
     * {@link #precomputeSpline(double[], double[])} followed by
     * {@link SplineCoefficients#evaluate(double)} for better performance.</p>
     * <p>对同一数据集进行多次求值时，建议使用
     * {@link #precomputeSpline(double[], double[])} 加
     * {@link SplineCoefficients#evaluate(double)} 以获得更好性能。</p>
     *
     * @param x  the x-coordinates of the data points (sorted ascending) — 数据点的 x 坐标（升序）
     * @param y  the y-coordinates of the data points — 数据点的 y 坐标
     * @param xi the x value at which to interpolate — 待插值的 x 值
     * @return the interpolated y value — 插值后的 y 值
     * @throws MathException if inputs are invalid — 输入无效时抛出
     */
    public static double cubicSpline(double[] x, double[] y, double xi) {
        validateInputs(x, y, 2);
        validateSorted(x);
        SplineCoefficients coeff = buildSplineCoefficients(x, y);
        return coeff.evaluate(xi);
    }

    private static SplineCoefficients buildSplineCoefficients(double[] x, double[] y) {
        int n = x.length;
        int m = n - 1;
        double[] xCopy = x.clone();
        double[] h = new double[m];
        double[] delta = new double[m];
        for (int i = 0; i < m; i++) {
            h[i] = x[i + 1] - x[i];
            delta[i] = (y[i + 1] - y[i]) / h[i];
        }

        double[] sigma = new double[n];
        if (m >= 2) {
            int size = m - 1;
            double[] lower = new double[size];
            double[] diag = new double[size];
            double[] upper = new double[size];
            double[] rhs = new double[size];

            for (int i = 0; i < size; i++) {
                int k = i + 1;
                diag[i] = 2.0 * (h[k - 1] + h[k]);
                rhs[i] = 6.0 * (delta[k] - delta[k - 1]);
                if (i > 0) {
                    lower[i] = h[k - 1];
                }
                if (i < size - 1) {
                    upper[i] = h[k];
                }
            }

            for (int i = 1; i < size; i++) {
                if (Math.abs(diag[i - 1]) < 1e-30) {
                    throw new MathException("Tridiagonal system is ill-conditioned; "
                            + "x values may be too close together / 三对角系统病态，x 值可能间距过小");
                }
                double factor = lower[i] / diag[i - 1];
                diag[i] -= factor * upper[i - 1];
                rhs[i] -= factor * rhs[i - 1];
            }

            double[] sol = new double[size];
            if (Math.abs(diag[size - 1]) < 1e-30) {
                throw new MathException("Tridiagonal system is ill-conditioned / 三对角系统病态");
            }
            sol[size - 1] = rhs[size - 1] / diag[size - 1];
            for (int i = size - 2; i >= 0; i--) {
                sol[i] = (rhs[i] - upper[i] * sol[i + 1]) / diag[i];
            }

            for (int i = 0; i < size; i++) {
                sigma[i + 1] = sol[i];
            }
        }

        // Compute polynomial coefficients for each interval
        double[] a = new double[m];
        double[] b = new double[m];
        double[] c = new double[m];
        double[] d = new double[m];
        for (int i = 0; i < m; i++) {
            a[i] = y[i];
            b[i] = delta[i] - h[i] * (2.0 * sigma[i] + sigma[i + 1]) / 6.0;
            c[i] = sigma[i] / 2.0;
            d[i] = (sigma[i + 1] - sigma[i]) / (6.0 * h[i]);
        }

        return new SplineCoefficients(xCopy, h, a, b, c, d);
    }

    // ---- Validation helpers ----

    private static void validateInputs(double[] x, double[] y, int minPoints) {
        if (x == null) {
            throw new MathException("x array must not be null / x 数组不能为 null");
        }
        if (y == null) {
            throw new MathException("y array must not be null / y 数组不能为 null");
        }
        if (x.length != y.length) {
            throw new MathException(
                    "x and y arrays must have the same length: x.length=" + x.length
                            + ", y.length=" + y.length
                            + " / x 和 y 数组长度必须相同"
            );
        }
        if (x.length > 10_000) {
            throw new MathException(
                    "Too many data points: " + x.length + " (max 10000)"
                            + " / 数据点过多（最大 10000）"
            );
        }
        if (x.length < minPoints) {
            throw new MathException(
                    "At least " + minPoints + " data points required, got " + x.length
                            + " / 至少需要 " + minPoints + " 个数据点，实际 " + x.length
            );
        }
        for (int i = 0; i < x.length; i++) {
            if (Double.isNaN(x[i]) || Double.isInfinite(x[i])) {
                throw new MathException("x[" + i + "] is not finite / x[" + i + "] 不是有限值");
            }
            if (Double.isNaN(y[i]) || Double.isInfinite(y[i])) {
                throw new MathException("y[" + i + "] is not finite / y[" + i + "] 不是有限值");
            }
        }
    }

    private static void validateSorted(double[] x) {
        for (int i = 1; i < x.length; i++) {
            if (x[i] <= x[i - 1]) {
                throw new MathException(
                        "x array must be strictly ascending: x[" + (i - 1) + "]=" + x[i - 1]
                                + " >= x[" + i + "]=" + x[i]
                                + " / x 数组必须严格升序"
                );
            }
        }
    }

    private static void validateDistinct(double[] x) {
        int n = x.length;
        if (n <= 32) {
            // O(n²) direct comparison — avoids clone+sort allocation for small arrays
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (Double.compare(x[i], x[j]) == 0) {
                        throw new MathException(
                                "x values must be distinct: found duplicate " + x[i]
                                        + " / x 值必须互不相同"
                        );
                    }
                }
            }
        } else {
            // O(n log n) check via sorted copy for larger arrays
            double[] sorted = x.clone();
            Arrays.sort(sorted);
            for (int i = 1; i < sorted.length; i++) {
                if (Double.compare(sorted[i], sorted[i - 1]) == 0) {
                    throw new MathException(
                            "x values must be distinct: found duplicate " + sorted[i]
                                    + " / x 值必须互不相同"
                    );
                }
            }
        }
    }
}
