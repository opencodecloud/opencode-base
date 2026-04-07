package cloud.opencode.base.math.analysis;

import cloud.opencode.base.math.exception.MathException;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

/**
 * Numerical differentiation utilities using finite difference methods.
 * 使用有限差分法的数值微分工具
 *
 * <p>Provides central difference approximations for first and second derivatives,
 * as well as Richardson extrapolation for improved accuracy.
 * All methods are stateless and thread-safe.</p>
 * <p>提供一阶和二阶导数的中心差分近似，
 * 以及理查森外推法以提高精度。所有方法无状态且线程安全。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class Differentiation {

    private Differentiation() {
        throw new AssertionError("No instances");
    }

    /**
     * Computes the first derivative of {@code f} at {@code x} using central difference.
     * 使用中心差分法计算函数 f 在 x 处的一阶导数
     *
     * <p>The step size is automatically chosen as {@code max(1e-8, |x| * 1e-8)}.</p>
     * <p>步长自动选择为 {@code max(1e-8, |x| * 1e-8)}。</p>
     *
     * @param f the function / 函数
     * @param x the point at which to differentiate / 求导点
     * @return the approximate first derivative / 近似一阶导数
     * @throws MathException if inputs are invalid
     */
    public static double derivative(DoubleUnaryOperator f, double x) {
        Objects.requireNonNull(f, "Function f must not be null");
        requireFinite(x, "x");
        double h = Math.max(1e-8, Math.abs(x) * 1e-8);
        return centralDifference(f, x, h);
    }

    /**
     * Computes the first derivative of {@code f} at {@code x} using central difference
     * with an explicit step size.
     * 使用指定步长的中心差分法计算函数 f 在 x 处的一阶导数
     *
     * @param f the function / 函数
     * @param x the point at which to differentiate / 求导点
     * @param h the step size (must be positive) / 步长（必须为正）
     * @return the approximate first derivative / 近似一阶导数
     * @throws MathException if inputs are invalid
     */
    public static double derivative(DoubleUnaryOperator f, double x, double h) {
        Objects.requireNonNull(f, "Function f must not be null");
        requireFinite(x, "x");
        requirePositiveH(h);
        return centralDifference(f, x, h);
    }

    /**
     * Computes the second derivative of {@code f} at {@code x} using central difference.
     * 使用中心差分法计算函数 f 在 x 处的二阶导数
     *
     * <p>The step size is automatically chosen as {@code max(1e-5, |x| * 1e-5)}.</p>
     * <p>步长自动选择为 {@code max(1e-5, |x| * 1e-5)}。</p>
     *
     * @param f the function / 函数
     * @param x the point at which to differentiate / 求导点
     * @return the approximate second derivative / 近似二阶导数
     * @throws MathException if inputs are invalid
     */
    public static double secondDerivative(DoubleUnaryOperator f, double x) {
        Objects.requireNonNull(f, "Function f must not be null");
        requireFinite(x, "x");
        double h = Math.max(1e-5, Math.abs(x) * 1e-5);
        return centralSecondDifference(f, x, h);
    }

    /**
     * Computes the second derivative of {@code f} at {@code x} using central difference
     * with an explicit step size.
     * 使用指定步长的中心差分法计算函数 f 在 x 处的二阶导数
     *
     * @param f the function / 函数
     * @param x the point at which to differentiate / 求导点
     * @param h the step size (must be positive) / 步长（必须为正）
     * @return the approximate second derivative / 近似二阶导数
     * @throws MathException if inputs are invalid
     */
    public static double secondDerivative(DoubleUnaryOperator f, double x, double h) {
        Objects.requireNonNull(f, "Function f must not be null");
        requireFinite(x, "x");
        requirePositiveH(h);
        return centralSecondDifference(f, x, h);
    }

    /**
     * Computes the first derivative of {@code f} at {@code x} using Richardson extrapolation.
     * 使用理查森外推法计算函数 f 在 x 处的一阶导数
     *
     * <p>Richardson extrapolation improves accuracy by combining central difference
     * estimates at decreasing step sizes using a Neville-like extrapolation tableau.
     * Higher orders yield more accurate results for smooth functions.</p>
     * <p>理查森外推法通过在递减步长下组合中心差分估计，
     * 使用类似 Neville 的外推表来提高精度。对于光滑函数，更高阶数可获得更精确的结果。</p>
     *
     * @param f     the function / 函数
     * @param x     the point at which to differentiate / 求导点
     * @param order the extrapolation order, in [1, 6] / 外推阶数，范围 [1, 6]
     * @return the most accurate derivative estimate / 最精确的导数估计
     * @throws MathException if inputs are invalid
     */
    public static double richardson(DoubleUnaryOperator f, double x, int order) {
        Objects.requireNonNull(f, "Function f must not be null");
        requireFinite(x, "x");
        if (order < 1 || order > 6) {
            throw new MathException("Richardson order must be in [1, 6], got: " + order);
        }

        int n = order + 1; // number of step sizes
        double h0 = Math.max(0.1, Math.abs(x) * 0.1);
        double[][] d = new double[n][n];

        // Fill the first column with central differences at decreasing h
        double h = h0;
        for (int i = 0; i < n; i++) {
            d[i][0] = centralDifference(f, x, h);
            h *= 0.5; // iterative halving instead of Math.pow(2, i)
        }

        // Neville-like extrapolation tableau (iterative 4^j instead of Math.pow)
        double factor = 4.0;
        for (int j = 1; j < n; j++) {
            for (int i = j; i < n; i++) {
                d[i][j] = (factor * d[i][j - 1] - d[i - 1][j - 1]) / (factor - 1);
            }
            factor *= 4.0;
        }

        return d[n - 1][n - 1];
    }

    // ==================== Internal Helpers ====================

    private static double centralDifference(DoubleUnaryOperator f, double x, double h) {
        return (f.applyAsDouble(x + h) - f.applyAsDouble(x - h)) / (2.0 * h);
    }

    private static double centralSecondDifference(DoubleUnaryOperator f, double x, double h) {
        return (f.applyAsDouble(x + h) - 2.0 * f.applyAsDouble(x) + f.applyAsDouble(x - h)) / (h * h);
    }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new MathException(name + " must be finite, got: " + value);
        }
    }

    private static void requirePositiveH(double h) {
        if (!Double.isFinite(h) || h <= 0) {
            throw new MathException("Step size h must be positive and finite, got: " + h);
        }
    }
}
