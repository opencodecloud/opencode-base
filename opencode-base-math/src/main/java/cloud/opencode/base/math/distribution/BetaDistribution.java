package cloud.opencode.base.math.distribution;

import cloud.opencode.base.math.exception.MathException;
import cloud.opencode.base.math.special.SpecialFunctions;

/**
 * Immutable Beta distribution.
 * 不可变 Beta 分布
 *
 * <p>Provides PDF, CDF, inverse CDF (quantile function), mean, and variance.
 * Thread-safe: instances are immutable.</p>
 * <p>提供概率密度函数、累积分布函数、逆累积分布函数（分位函数）、均值和方差。
 * 线程安全：实例不可变。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class BetaDistribution {

    private final double alpha;
    private final double beta;
    private final double logNormConst;

    private BetaDistribution(double alpha, double beta) {
        this.alpha = alpha;
        this.beta = beta;
        // log(B(alpha, beta)) = logGamma(alpha) + logGamma(beta) - logGamma(alpha+beta)
        this.logNormConst = SpecialFunctions.logGamma(alpha)
                + SpecialFunctions.logGamma(beta)
                - SpecialFunctions.logGamma(alpha + beta);
    }

    /**
     * Creates a Beta distribution with the specified parameters.
     * 创建具有指定参数的 Beta 分布
     *
     * @param alpha the first shape parameter, must be &gt; 0 / 第一形状参数，必须 &gt; 0
     * @param beta  the second shape parameter, must be &gt; 0 / 第二形状参数，必须 &gt; 0
     * @return a new BetaDistribution instance / 新的 Beta 分布实例
     * @throws IllegalArgumentException if alpha or beta &le; 0 or not finite
     */
    public static BetaDistribution of(double alpha, double beta) {
        if (!Double.isFinite(alpha) || alpha <= 0) {
            throw new IllegalArgumentException("Alpha must be a positive finite number, got: " + alpha);
        }
        if (!Double.isFinite(beta) || beta <= 0) {
            throw new IllegalArgumentException("Beta must be a positive finite number, got: " + beta);
        }
        return new BetaDistribution(alpha, beta);
    }

    /**
     * Returns the alpha (first shape) parameter.
     * 返回 alpha（第一形状）参数
     *
     * @return the alpha parameter / alpha 参数
     */
    public double alpha() {
        return alpha;
    }

    /**
     * Returns the beta (second shape) parameter.
     * 返回 beta（第二形状）参数
     *
     * @return the beta parameter / beta 参数
     */
    public double beta() {
        return beta;
    }

    /**
     * Returns the mean of this distribution: alpha / (alpha + beta).
     * 返回此分布的均值：alpha / (alpha + beta)
     *
     * @return the mean / 均值
     */
    public double mean() {
        return alpha / (alpha + beta);
    }

    /**
     * Returns the variance of this distribution: alpha*beta / ((alpha+beta)^2 * (alpha+beta+1)).
     * 返回此分布的方差：alpha*beta / ((alpha+beta)^2 * (alpha+beta+1))
     *
     * @return the variance / 方差
     */
    public double variance() {
        double sum = alpha + beta;
        return (alpha * beta) / (sum * sum * (sum + 1.0));
    }

    /**
     * Computes the probability density function (PDF) at x.
     * 计算在 x 处的概率密度函数值
     *
     * <p>f(x) = x^(alpha-1) * (1-x)^(beta-1) / B(alpha, beta)</p>
     *
     * @param x the point at which to evaluate the PDF, should be in [0, 1] / 计算 PDF 的点
     * @return the density at x / x 处的密度值
     */
    public double pdf(double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        if (x < 0 || x > 1) {
            return 0.0;
        }
        if (x == 0.0) {
            if (alpha < 1) {
                return Double.POSITIVE_INFINITY;
            } else if (alpha == 1) {
                return Math.exp(-logNormConst);
            } else {
                return 0.0;
            }
        }
        if (x == 1.0) {
            if (beta < 1) {
                return Double.POSITIVE_INFINITY;
            } else if (beta == 1) {
                return Math.exp(-logNormConst);
            } else {
                return 0.0;
            }
        }
        double logPdf = (alpha - 1.0) * Math.log(x) + (beta - 1.0) * Math.log(1.0 - x) - logNormConst;
        return Math.exp(logPdf);
    }

    /**
     * Computes the cumulative distribution function (CDF) at x.
     * 计算在 x 处的累积分布函数值
     *
     * <p>F(x) = I_x(alpha, beta) where I is the regularized incomplete beta function.</p>
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
        if (x >= 1) {
            return 1.0;
        }
        return SpecialFunctions.regularizedBeta(x, alpha, beta);
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
        return DistributionUtils.bisect(this::cdf, p, 0.0, 1.0);
    }

    @Override
    public String toString() {
        return "BetaDistribution{alpha=" + alpha + ", beta=" + beta + "}";
    }
}
