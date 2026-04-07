package cloud.opencode.base.math.analysis;

import cloud.opencode.base.math.exception.MathException;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

/**
 * Numerical root-finding algorithms for single-variable real functions.
 * 单变量实函数的数值求根算法
 *
 * <p>Provides static methods for bisection, Brent's method, Newton-Raphson,
 * and the secant method. All methods are stateless and thread-safe.</p>
 * <p>提供二分法、Brent 法、牛顿-拉弗森法和割线法的静态方法。
 * 所有方法无状态且线程安全。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class RootFinder {

    private static final int MAX_ITERATIONS = 1000;

    private RootFinder() {
        throw new AssertionError("No instances");
    }

    /**
     * Finds a root of {@code f} in the interval {@code [a, b]} using the bisection method.
     * 使用二分法在区间 [a, b] 内查找函数 f 的根
     *
     * <p>Requires {@code f(a)} and {@code f(b)} to have opposite signs (or one is zero).</p>
     * <p>要求 f(a) 与 f(b) 异号（或其中一个为零）。</p>
     *
     * @param f         the function / 函数
     * @param a         left endpoint of the interval / 区间左端点
     * @param b         right endpoint of the interval / 区间右端点
     * @param tolerance convergence tolerance on {@code |b - a|} / 收敛容差，基于 |b - a|
     * @return an approximate root / 近似根
     * @throws MathException if inputs are invalid or the method does not converge
     */
    public static double bisection(DoubleUnaryOperator f, double a, double b, double tolerance) {
        Objects.requireNonNull(f, "Function f must not be null");
        requireFinite(a, "a");
        requireFinite(b, "b");
        requirePositive(tolerance, "tolerance");

        double fa = f.applyAsDouble(a);
        double fb = f.applyAsDouble(b);
        requireSignChange(fa, fb);

        if (fa == 0.0) return a;
        if (fb == 0.0) return b;

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            double mid = a + (b - a) / 2.0;
            if (Math.abs(b - a) < tolerance) {
                return mid;
            }
            double fmid = f.applyAsDouble(mid);
            if (fmid == 0.0) {
                return mid;
            }
            if (Math.copySign(1.0, fa) * Math.copySign(1.0, fmid) < 0) {
                b = mid;
                fb = fmid;
            } else {
                a = mid;
                fa = fmid;
            }
        }
        throw new MathException("Bisection did not converge within " + MAX_ITERATIONS + " iterations");
    }

    /**
     * Finds a root of {@code f} in the interval {@code [a, b]} using Brent's method.
     * 使用 Brent 法在区间 [a, b] 内查找函数 f 的根
     *
     * <p>Combines bisection, secant, and inverse quadratic interpolation for robust
     * and fast convergence. This is the recommended general-purpose root finder.</p>
     * <p>结合二分法、割线法和逆二次插值以实现稳健且快速的收敛。
     * 这是推荐的通用求根方法。</p>
     *
     * @param f         the function / 函数
     * @param a         left endpoint of the interval / 区间左端点
     * @param b         right endpoint of the interval / 区间右端点
     * @param tolerance convergence tolerance / 收敛容差
     * @return an approximate root / 近似根
     * @throws MathException if inputs are invalid or the method does not converge
     */
    public static double brent(DoubleUnaryOperator f, double a, double b, double tolerance) {
        Objects.requireNonNull(f, "Function f must not be null");
        requireFinite(a, "a");
        requireFinite(b, "b");
        requirePositive(tolerance, "tolerance");

        double fa = f.applyAsDouble(a);
        double fb = f.applyAsDouble(b);
        requireSignChange(fa, fb);

        if (fa == 0.0) return a;
        if (fb == 0.0) return b;

        // Ensure |f(a)| >= |f(b)| so b is the best guess
        if (Math.abs(fa) < Math.abs(fb)) {
            double tmp = a; a = b; b = tmp;
            double ftmp = fa; fa = fb; fb = ftmp;
        }

        double c = a;
        double fc = fa;
        boolean mflag = true;
        double s = 0;
        double d = 0;

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            if (Math.abs(b - a) < tolerance) {
                return b;
            }
            if (fb == 0.0) {
                return b;
            }

            if (fa != fc && fb != fc) {
                // Inverse quadratic interpolation
                s = (a * fb * fc) / ((fa - fb) * (fa - fc))
                  + (b * fa * fc) / ((fb - fa) * (fb - fc))
                  + (c * fa * fb) / ((fc - fa) * (fc - fb));
            } else {
                // Secant method
                s = b - fb * (b - a) / (fb - fa);
            }

            // Conditions for bisection
            double midpoint = (a + b) / 2.0;
            boolean cond1 = !(s > Math.min(((3 * a + b) / 4.0), b) && s < Math.max(((3 * a + b) / 4.0), b));
            boolean cond2 = mflag && Math.abs(s - b) >= Math.abs(b - c) / 2.0;
            boolean cond3 = !mflag && Math.abs(s - b) >= Math.abs(c - d) / 2.0;
            boolean cond4 = mflag && Math.abs(b - c) < tolerance;
            boolean cond5 = !mflag && Math.abs(c - d) < tolerance;

            if (cond1 || cond2 || cond3 || cond4 || cond5) {
                s = midpoint;
                mflag = true;
            } else {
                mflag = false;
            }

            double fs = f.applyAsDouble(s);
            d = c;
            c = b;
            fc = fb;

            if (fa * fs < 0) {
                b = s;
                fb = fs;
            } else {
                a = s;
                fa = fs;
            }

            // Keep |f(a)| >= |f(b)|
            if (Math.abs(fa) < Math.abs(fb)) {
                double tmp = a; a = b; b = tmp;
                double ftmp = fa; fa = fb; fb = ftmp;
            }
        }
        throw new MathException("Brent's method did not converge within " + MAX_ITERATIONS + " iterations");
    }

    /**
     * Finds a root of {@code f} using the Newton-Raphson method.
     * 使用牛顿-拉弗森法查找函数 f 的根
     *
     * <p>Requires both the function and its derivative. Convergence is quadratic
     * near simple roots but may diverge for poor initial guesses.</p>
     * <p>需要提供函数及其导数。在简单根附近为二次收敛，
     * 但初始猜测不佳时可能发散。</p>
     *
     * @param f         the function / 函数
     * @param df        the derivative of f / f 的导数
     * @param x0        initial guess / 初始猜测值
     * @param tolerance convergence tolerance on {@code |f(x)|} / 收敛容差，基于 |f(x)|
     * @return an approximate root / 近似根
     * @throws MathException if the derivative is too close to zero or the method does not converge
     */
    public static double newton(DoubleUnaryOperator f, DoubleUnaryOperator df,
                                double x0, double tolerance) {
        Objects.requireNonNull(f, "Function f must not be null");
        Objects.requireNonNull(df, "Derivative df must not be null");
        requireFinite(x0, "x0");
        requirePositive(tolerance, "tolerance");

        double x = x0;
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            double fx = f.applyAsDouble(x);
            if (Math.abs(fx) < tolerance) {
                return x;
            }
            double dfx = df.applyAsDouble(x);
            if (Math.abs(dfx) < 1e-30) {
                throw new MathException("Derivative too close to zero at x=" + x);
            }
            x = x - fx / dfx;
            if (!Double.isFinite(x)) {
                throw new MathException("Newton's method diverged at iteration " + i
                        + " / 牛顿法在第 " + i + " 次迭代发散");
            }
        }
        throw new MathException("Newton's method did not converge within " + MAX_ITERATIONS + " iterations");
    }

    /**
     * Finds a root of {@code f} using the secant method.
     * 使用割线法查找函数 f 的根
     *
     * <p>Similar to Newton's method but approximates the derivative using
     * two function evaluations. Does not require an explicit derivative.</p>
     * <p>类似牛顿法，但使用两个函数值近似导数。无需提供显式导数。</p>
     *
     * @param f         the function / 函数
     * @param x0        first initial point / 第一个初始点
     * @param x1        second initial point / 第二个初始点
     * @param tolerance convergence tolerance on {@code |f(x)|} / 收敛容差，基于 |f(x)|
     * @return an approximate root / 近似根
     * @throws MathException if the method does not converge
     */
    public static double secant(DoubleUnaryOperator f, double x0, double x1, double tolerance) {
        Objects.requireNonNull(f, "Function f must not be null");
        requireFinite(x0, "x0");
        requireFinite(x1, "x1");
        requirePositive(tolerance, "tolerance");

        double xPrev = x0;
        double xCurr = x1;
        double fPrev = f.applyAsDouble(xPrev);
        double fCurr = f.applyAsDouble(xCurr);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            if (Math.abs(fCurr) < tolerance) {
                return xCurr;
            }
            double denom = fCurr - fPrev;
            if (Math.abs(denom) < 1e-30) {
                throw new MathException("Secant method: function values too close, cannot compute next iterate");
            }
            double xNext = xCurr - fCurr * (xCurr - xPrev) / denom;
            if (!Double.isFinite(xNext)) {
                throw new MathException("Secant method diverged at iteration " + i
                        + " / 割线法在第 " + i + " 次迭代发散");
            }
            xPrev = xCurr;
            fPrev = fCurr;
            xCurr = xNext;
            fCurr = f.applyAsDouble(xCurr);
        }
        throw new MathException("Secant method did not converge within " + MAX_ITERATIONS + " iterations");
    }

    // ==================== Internal Helpers ====================

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new MathException(name + " must be finite, got: " + value);
        }
    }

    private static void requirePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new MathException(name + " must be positive and finite, got: " + value);
        }
    }

    private static void requireSignChange(double fa, double fb) {
        if (Math.signum(fa) == Math.signum(fb) && fa != 0.0 && fb != 0.0) {
            throw new MathException(
                    "Function values at endpoints must have opposite signs, got f(a)=" + fa + ", f(b)=" + fb);
        }
    }
}
