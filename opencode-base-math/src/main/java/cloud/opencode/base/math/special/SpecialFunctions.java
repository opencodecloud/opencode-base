package cloud.opencode.base.math.special;

import cloud.opencode.base.math.exception.MathException;

/**
 * Special mathematical functions (Gamma, Beta, Error function, etc.).
 * 特殊数学函数（Gamma、Beta、误差函数等）
 *
 * <p>All methods are stateless and thread-safe.
 * Implementations use well-known numerical approximations with documented error bounds.</p>
 * <p>所有方法无状态且线程安全。实现使用已知的数值近似方法，具有文档化的误差范围。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class SpecialFunctions {

    private SpecialFunctions() {
        throw new AssertionError("No instances");
    }

    /** Lanczos approximation g parameter. / Lanczos 近似 g 参数 */
    private static final double LANCZOS_G = 7.0;

    /** Lanczos coefficients (g=7, n=9) from Numerical Recipes. / Lanczos 系数 */
    private static final double[] LANCZOS_COEFF = {
            0.99999999999980993,
            676.5203681218851,
            -1259.1392167224028,
            771.32342877765313,
            -176.61502916214059,
            12.507343278686905,
            -0.13857109526572012,
            9.9843695780195716e-6,
            1.5056327351493116e-7
    };

    /** Maximum number of iterations for continued fraction / series expansions. */
    private static final int MAX_ITERATIONS = 200;

    /** Convergence epsilon for iterative algorithms. */
    private static final double EPSILON = 1e-14;

    /**
     * Computes the Gamma function using the Lanczos approximation.
     * 使用 Lanczos 近似计算 Gamma 函数
     *
     * <p>For negative non-integer values, the reflection formula is used:
     * Gamma(x) = pi / (sin(pi*x) * Gamma(1-x)).</p>
     * <p>对于负非整数值，使用反射公式：Gamma(x) = pi / (sin(pi*x) * Gamma(1-x))。</p>
     *
     * @param x the argument / 参数
     * @return Gamma(x) / Gamma 函数值
     * @throws MathException if x is a non-positive integer (pole) / 如果 x 为非正整数（极点）
     */
    public static double gamma(double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        // Check for non-positive integers (poles)
        if (x <= 0 && x == Math.floor(x)) {
            throw new MathException("Gamma function has poles at non-positive integers: " + x);
        }
        if (x == Double.POSITIVE_INFINITY) {
            return Double.POSITIVE_INFINITY;
        }

        if (x < 0.5) {
            // Reflection formula: Gamma(x) = pi / (sin(pi*x) * Gamma(1-x))
            double sinPiX = Math.sin(Math.PI * x);
            if (sinPiX == 0) {
                throw new MathException("Gamma function has poles at non-positive integers: " + x);
            }
            return Math.PI / (sinPiX * gamma(1.0 - x));
        }

        return Math.exp(logGammaLanczos(x));
    }

    /**
     * Computes the natural logarithm of the Gamma function.
     * 计算 Gamma 函数的自然对数
     *
     * <p>More numerically stable than {@code Math.log(gamma(x))} for large x.</p>
     * <p>对于大 x 值，比 {@code Math.log(gamma(x))} 在数值上更稳定。</p>
     *
     * @param x the argument, must be positive / 参数，必须为正数
     * @return ln(Gamma(x)) / Gamma 函数自然对数值
     * @throws MathException if x is not positive / 如果 x 不是正数
     */
    public static double logGamma(double x) {
        if (Double.isNaN(x) || x <= 0) {
            throw new MathException("logGamma requires a positive argument, got: " + x);
        }
        if (x == Double.POSITIVE_INFINITY) {
            return Double.POSITIVE_INFINITY;
        }
        return logGammaLanczos(x);
    }

    /**
     * Internal Lanczos log-gamma for x >= 0.5.
     */
    private static double logGammaLanczos(double x) {
        double z = x - 1.0;
        double sum = LANCZOS_COEFF[0];
        for (int i = 1; i < LANCZOS_COEFF.length; i++) {
            sum += LANCZOS_COEFF[i] / (z + i);
        }
        double t = z + LANCZOS_G + 0.5;
        return 0.5 * Math.log(2.0 * Math.PI) + (z + 0.5) * Math.log(t) - t + Math.log(sum);
    }

    /**
     * Computes the Beta function: B(a, b) = Gamma(a) * Gamma(b) / Gamma(a + b).
     * 计算 Beta 函数：B(a, b) = Gamma(a) * Gamma(b) / Gamma(a + b)
     *
     * @param a first parameter, must be positive / 第一个参数，必须为正数
     * @param b second parameter, must be positive / 第二个参数，必须为正数
     * @return Beta(a, b) / Beta 函数值
     * @throws MathException if a or b is not positive / 如果 a 或 b 不是正数
     */
    public static double beta(double a, double b) {
        if (!Double.isFinite(a) || !Double.isFinite(b) || a <= 0 || b <= 0) {
            throw new MathException("Beta function requires positive finite arguments, got a=" + a + ", b=" + b);
        }
        // Use log-gamma for numerical stability
        return Math.exp(logGamma(a) + logGamma(b) - logGamma(a + b));
    }

    /**
     * Computes the error function erf(x).
     * 计算误差函数 erf(x)
     *
     * <p>Uses Abramowitz &amp; Stegun approximation 7.1.26 in Horner form.
     * Maximum error approximately 1.5e-7.</p>
     * <p>使用 Abramowitz &amp; Stegun 近似 7.1.26 的 Horner 形式。
     * 最大误差约 1.5e-7。</p>
     *
     * @param x the argument / 参数
     * @return erf(x), in the range [-1, 1] / 误差函数值，范围 [-1, 1]
     */
    public static double erf(double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        if (x == Double.POSITIVE_INFINITY) {
            return 1.0;
        }
        if (x == Double.NEGATIVE_INFINITY) {
            return -1.0;
        }
        if (x == 0.0) {
            return 0.0;
        }
        boolean negative = x < 0;
        double erfcVal = erfcCore(Math.abs(x));
        double result = 1.0 - erfcVal;
        return negative ? -result : result;
    }

    /**
     * Computes the complementary error function erfc(x) = 1 - erf(x).
     * 计算互补误差函数 erfc(x) = 1 - erf(x)
     *
     * <p>Computed directly (not as 1 - erf(x)) for better precision when x is large.</p>
     * <p>直接计算（非 1 - erf(x)），以在 x 较大时获得更好的精度。</p>
     *
     * @param x the argument / 参数
     * @return erfc(x), in the range [0, 2] / 互补误差函数值，范围 [0, 2]
     */
    public static double erfc(double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        if (x == Double.POSITIVE_INFINITY) {
            return 0.0;
        }
        if (x == Double.NEGATIVE_INFINITY) {
            return 2.0;
        }
        if (x == 0.0) {
            return 1.0;
        }
        boolean negative = x < 0;
        double result = erfcCore(Math.abs(x));
        return negative ? (2.0 - result) : result;
    }

    /**
     * Core erfc computation using Abramowitz &amp; Stegun approximation 7.1.26 in Horner form.
     * Shared by both erf() and erfc() to eliminate code duplication.
     *
     * @param absX the absolute value of x (must be &ge; 0)
     * @return erfc(absX)
     */
    private static double erfcCore(double absX) {
        double t = 1.0 / (1.0 + 0.3275911 * absX);
        double poly = t * (0.254829592
                + t * (-0.284496736
                + t * (1.421413741
                + t * (-1.453152027
                + t * 1.061405429))));
        return poly * Math.exp(-absX * absX);
    }

    /**
     * Computes the regularized incomplete beta function I_x(a, b).
     * 计算正则化不完全 Beta 函数 I_x(a, b)
     *
     * <p>Uses continued fraction expansion (Lentz's method) for efficient convergence.</p>
     * <p>使用连分数展开（Lentz 方法）实现高效收敛。</p>
     *
     * @param x the integration upper limit, in [0, 1] / 积分上限，范围 [0, 1]
     * @param a first shape parameter, must be positive / 第一个形状参数，必须为正数
     * @param b second shape parameter, must be positive / 第二个形状参数，必须为正数
     * @return I_x(a, b), in [0, 1] / 正则化不完全 Beta 函数值
     * @throws MathException if parameters are invalid or algorithm does not converge
     */
    public static double regularizedBeta(double x, double a, double b) {
        if (Double.isNaN(a) || Double.isNaN(b) || Double.isNaN(x)) {
            throw new MathException("regularizedBeta does not accept NaN arguments");
        }
        if (a <= 0 || b <= 0) {
            throw new MathException("regularizedBeta requires positive a and b, got a=" + a + ", b=" + b);
        }
        if (x < 0 || x > 1) {
            throw new MathException("regularizedBeta requires x in [0, 1], got: " + x);
        }
        if (x == 0) {
            return 0.0;
        }
        if (x == 1) {
            return 1.0;
        }

        // Use symmetry relation when x > (a+1)/(a+b+2) for better convergence
        if (x > (a + 1.0) / (a + b + 2.0)) {
            return 1.0 - regularizedBeta(1.0 - x, b, a);
        }

        // Use continued fraction (Numerical Recipes betacf)
        // I_x(a,b) = x^a * (1-x)^b / (a * B(a,b)) * betacf(a,b,x)
        double bt = Math.exp(
                logGamma(a + b) - logGamma(a) - logGamma(b)
                        + a * Math.log(x) + b * Math.log(1.0 - x));

        return bt * betaContinuedFraction(a, b, x) / a;
    }

    /**
     * Continued fraction for the regularized incomplete beta function.
     * Evaluates betacf(a, b, x) using the modified Lentz's method
     * (Numerical Recipes 3rd ed., Section 6.4).
     */
    private static double betaContinuedFraction(double a, double b, double x) {
        double tiny = 1e-30;
        double qab = a + b;
        double qap = a + 1.0;
        double qam = a - 1.0;

        // First step of Lentz's method
        double c = 1.0;
        double d = 1.0 - qab * x / qap;
        if (Math.abs(d) < tiny) {
            d = tiny;
        }
        d = 1.0 / d;
        double h = d;

        for (int m = 1; m <= MAX_ITERATIONS; m++) {
            int m2 = 2 * m;

            // Even step (d_{2m})
            double aa = m * (b - m) * x / ((qam + m2) * (a + m2));
            d = 1.0 + aa * d;
            if (Math.abs(d) < tiny) {
                d = tiny;
            }
            c = 1.0 + aa / c;
            if (Math.abs(c) < tiny) {
                c = tiny;
            }
            d = 1.0 / d;
            h *= d * c;

            // Odd step (d_{2m+1})
            aa = -(a + m) * (qab + m) * x / ((a + m2) * (qap + m2));
            d = 1.0 + aa * d;
            if (Math.abs(d) < tiny) {
                d = tiny;
            }
            c = 1.0 + aa / c;
            if (Math.abs(c) < tiny) {
                c = tiny;
            }
            d = 1.0 / d;
            double delta = d * c;
            h *= delta;

            if (Math.abs(delta - 1.0) < EPSILON) {
                return h;
            }
        }
        throw new MathException("regularizedBeta continued fraction did not converge after "
                + MAX_ITERATIONS + " iterations for a=" + a + ", b=" + b + ", x=" + x);
    }

    /**
     * Computes the regularized lower incomplete gamma function P(a, x) = gamma(a, x) / Gamma(a).
     * 计算正则化下不完全 Gamma 函数 P(a, x) = gamma(a, x) / Gamma(a)
     *
     * <p>Uses series expansion for x &lt; a+1, and continued fraction for x &ge; a+1.</p>
     * <p>当 x &lt; a+1 时使用级数展开，x &ge; a+1 时使用连分数展开。</p>
     *
     * @param a shape parameter, must be positive / 形状参数，必须为正数
     * @param x upper integration limit, must be non-negative / 积分上限，必须非负
     * @return P(a, x), in [0, 1] / 正则化下不完全 Gamma 函数值
     * @throws MathException if parameters are invalid or algorithm does not converge
     */
    public static double regularizedGammaP(double a, double x) {
        if (Double.isNaN(a) || Double.isNaN(x)) {
            throw new MathException("regularizedGammaP does not accept NaN arguments");
        }
        if (a <= 0) {
            throw new MathException("regularizedGammaP requires positive a, got: " + a);
        }
        if (x < 0) {
            throw new MathException("regularizedGammaP requires non-negative x, got: " + x);
        }
        if (x == 0) {
            return 0.0;
        }
        if (x == Double.POSITIVE_INFINITY) {
            return 1.0;
        }

        if (x < a + 1.0) {
            // Series representation
            return gammaPSeries(a, x);
        } else {
            // Continued fraction for the upper incomplete gamma, then use P = 1 - Q
            return 1.0 - gammaQContinuedFraction(a, x);
        }
    }

    /**
     * Series expansion for P(a, x).
     */
    private static double gammaPSeries(double a, double x) {
        double logPrefix = a * Math.log(x) - x - logGamma(a);
        double sum = 1.0 / a;
        double term = 1.0 / a;
        for (int n = 1; n <= MAX_ITERATIONS; n++) {
            term *= x / (a + n);
            sum += term;
            if (Math.abs(term) < Math.abs(sum) * EPSILON) {
                return sum * Math.exp(logPrefix);
            }
        }
        throw new MathException("regularizedGammaP series did not converge after "
                + MAX_ITERATIONS + " iterations for a=" + a + ", x=" + x);
    }

    /**
     * Continued fraction for Q(a, x) = 1 - P(a, x) using Lentz's method.
     */
    private static double gammaQContinuedFraction(double a, double x) {
        double logPrefix = a * Math.log(x) - x - logGamma(a);
        double tiny = 1e-30;

        double b0 = x + 1.0 - a;
        double c = 1.0 / tiny;
        double d = 1.0 / b0;
        double f = d;

        for (int i = 1; i <= MAX_ITERATIONS; i++) {
            double an = -i * (i - a);
            double bn = x + 2.0 * i + 1.0 - a;
            d = bn + an * d;
            if (Math.abs(d) < tiny) {
                d = tiny;
            }
            c = bn + an / c;
            if (Math.abs(c) < tiny) {
                c = tiny;
            }
            d = 1.0 / d;
            double delta = c * d;
            f *= delta;
            if (Math.abs(delta - 1.0) < EPSILON) {
                return f * Math.exp(logPrefix);
            }
        }
        throw new MathException("regularizedGammaP continued fraction did not converge after "
                + MAX_ITERATIONS + " iterations for a=" + a + ", x=" + x);
    }
}
