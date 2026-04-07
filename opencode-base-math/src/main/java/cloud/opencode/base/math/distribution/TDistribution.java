package cloud.opencode.base.math.distribution;

import cloud.opencode.base.math.exception.MathException;
import cloud.opencode.base.math.special.SpecialFunctions;

/**
 * Immutable Student's t-distribution.
 * 不可变学生 t 分布
 *
 * <p>Provides PDF, CDF, and inverse CDF (quantile function).
 * Thread-safe: instances are immutable.</p>
 * <p>提供概率密度函数、累积分布函数和逆累积分布函数（分位函数）。
 * 线程安全：实例不可变。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class TDistribution {

    private final double df;
    private final double pdfCoeff;
    private final double halfDf;          // df / 2.0, used in every cdf() call
    private final double negHalfDfP1;     // -(df + 1.0) / 2.0, used in every pdf() call
    private final double invDf;           // 1.0 / df, used in every pdf() call

    private TDistribution(double df) {
        this.df = df;
        this.halfDf = df / 2.0;
        this.negHalfDfP1 = -(df + 1.0) / 2.0;
        this.invDf = 1.0 / df;
        // Precompute: Gamma((df+1)/2) / (sqrt(df*pi) * Gamma(df/2))
        this.pdfCoeff = Math.exp(
                SpecialFunctions.logGamma((df + 1.0) / 2.0)
                        - 0.5 * Math.log(df * Math.PI)
                        - SpecialFunctions.logGamma(df / 2.0));
    }

    /**
     * Creates a t-distribution with the specified degrees of freedom.
     * 创建具有指定自由度的 t 分布
     *
     * @param degreesOfFreedom the degrees of freedom, must be &gt; 0 / 自由度，必须 &gt; 0
     * @return a new TDistribution instance / 新的 t 分布实例
     * @throws IllegalArgumentException if degreesOfFreedom &le; 0 or is not finite
     */
    public static TDistribution of(double degreesOfFreedom) {
        if (!Double.isFinite(degreesOfFreedom) || degreesOfFreedom <= 0) {
            throw new IllegalArgumentException(
                    "Degrees of freedom must be a positive finite number, got: " + degreesOfFreedom);
        }
        return new TDistribution(degreesOfFreedom);
    }

    /**
     * Returns the degrees of freedom.
     * 返回自由度
     *
     * @return the degrees of freedom / 自由度
     */
    public double degreesOfFreedom() {
        return df;
    }

    /**
     * Computes the probability density function (PDF) at x.
     * 计算在 x 处的概率密度函数值
     *
     * @param x the point at which to evaluate the PDF / 计算 PDF 的点
     * @return the density at x / x 处的密度值
     */
    public double pdf(double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        if (Double.isInfinite(x)) {
            return 0.0;
        }
        return pdfCoeff * Math.pow(1.0 + x * x * invDf, negHalfDfP1);
    }

    /**
     * Computes the cumulative distribution function (CDF) at x.
     * 计算在 x 处的累积分布函数值
     *
     * <p>Uses the regularized incomplete beta function:
     * For x &ge; 0: cdf(x) = 1 - 0.5 * I(df/(df+x^2), df/2, 0.5)
     * For x &lt; 0: cdf(x) = 0.5 * I(df/(df+x^2), df/2, 0.5)</p>
     *
     * @param x the point at which to evaluate the CDF / 计算 CDF 的点
     * @return the cumulative probability P(X &le; x) / 累积概率
     */
    public double cdf(double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        if (x == Double.NEGATIVE_INFINITY) {
            return 0.0;
        }
        if (x == Double.POSITIVE_INFINITY) {
            return 1.0;
        }
        double t = df / (df + x * x);
        double ib = SpecialFunctions.regularizedBeta(t, halfDf, 0.5);
        if (x >= 0) {
            return 1.0 - 0.5 * ib;
        } else {
            return 0.5 * ib;
        }
    }

    /**
     * Computes the inverse CDF (quantile function).
     * 计算逆累积分布函数（分位函数）
     *
     * @param p the cumulative probability, must be in (0, 1) / 累积概率，必须在 (0, 1) 内
     * @return the quantile x such that P(X &le; x) = p / 使得 P(X &le; x) = p 的分位数
     * @throws MathException if p is not in (0, 1)
     */
    public double inverseCdf(double p) {
        if (p <= 0 || p >= 1 || Double.isNaN(p)) {
            throw new MathException("inverseCdf requires p in (0, 1), got: " + p);
        }
        return DistributionUtils.bisect(this::cdf, p, -1000.0, 1000.0);
    }

    @Override
    public String toString() {
        return "TDistribution{df=" + df + "}";
    }
}
