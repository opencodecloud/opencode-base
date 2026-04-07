package cloud.opencode.base.math.distribution;

import cloud.opencode.base.math.exception.MathException;
import cloud.opencode.base.math.special.SpecialFunctions;

/**
 * Immutable chi-squared distribution.
 * 不可变卡方分布
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
public final class ChiSquaredDistribution {

    private final double df;
    private final double halfDf;
    private final double logNormConst;

    private ChiSquaredDistribution(double df) {
        this.df = df;
        this.halfDf = df / 2.0;
        // log normalization constant: (df/2)*ln(2) + logGamma(df/2)
        this.logNormConst = halfDf * Math.log(2.0) + SpecialFunctions.logGamma(halfDf);
    }

    /**
     * Creates a chi-squared distribution with the specified degrees of freedom.
     * 创建具有指定自由度的卡方分布
     *
     * @param degreesOfFreedom the degrees of freedom, must be &gt; 0 / 自由度，必须 &gt; 0
     * @return a new ChiSquaredDistribution instance / 新的卡方分布实例
     * @throws IllegalArgumentException if degreesOfFreedom &le; 0 or is not finite
     */
    public static ChiSquaredDistribution of(double degreesOfFreedom) {
        if (!Double.isFinite(degreesOfFreedom) || degreesOfFreedom <= 0) {
            throw new IllegalArgumentException(
                    "Degrees of freedom must be a positive finite number, got: " + degreesOfFreedom);
        }
        return new ChiSquaredDistribution(degreesOfFreedom);
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
     * <p>f(x) = x^(df/2-1) * exp(-x/2) / (2^(df/2) * Gamma(df/2))</p>
     *
     * @param x the point at which to evaluate the PDF / 计算 PDF 的点
     * @return the density at x / x 处的密度值
     */
    public double pdf(double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        if (x < 0 || Double.isInfinite(x)) {
            return 0.0;
        }
        if (x == 0.0) {
            if (df < 2) {
                return Double.POSITIVE_INFINITY;
            } else if (df == 2) {
                return 0.5;
            } else {
                return 0.0;
            }
        }
        double logPdf = (halfDf - 1.0) * Math.log(x) - x / 2.0 - logNormConst;
        return Math.exp(logPdf);
    }

    /**
     * Computes the cumulative distribution function (CDF) at x.
     * 计算在 x 处的累积分布函数值
     *
     * <p>F(x) = P(df/2, x/2) where P is the regularized lower incomplete gamma function.</p>
     *
     * @param x the point at which to evaluate the CDF / 计算 CDF 的点
     * @return the cumulative probability P(X &le; x) / 累积概率
     */
    public double cdf(double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        if (x <= 0) {
            return 0.0;
        }
        if (x == Double.POSITIVE_INFINITY) {
            return 1.0;
        }
        return SpecialFunctions.regularizedGammaP(halfDf, x / 2.0);
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
        return DistributionUtils.bisect(this::cdf, p, 0.0, df + 100.0 * Math.sqrt(2.0 * df));
    }

    @Override
    public String toString() {
        return "ChiSquaredDistribution{df=" + df + "}";
    }
}
