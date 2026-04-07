package cloud.opencode.base.math.distribution;

import cloud.opencode.base.math.exception.MathException;
import cloud.opencode.base.math.special.SpecialFunctions;

/**
 * Immutable Gamma distribution.
 * 不可变 Gamma 分布
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
public final class GammaDistribution {

    private final double shape;
    private final double scale;
    private final double logNormConst;

    private GammaDistribution(double shape, double scale) {
        this.shape = shape;
        this.scale = scale;
        // log normalization constant: shape*ln(scale) + logGamma(shape)
        this.logNormConst = shape * Math.log(scale) + SpecialFunctions.logGamma(shape);
    }

    /**
     * Creates a Gamma distribution with the specified shape and scale.
     * 创建具有指定形状和尺度的 Gamma 分布
     *
     * @param shape the shape parameter (k or alpha), must be &gt; 0 / 形状参数，必须 &gt; 0
     * @param scale the scale parameter (theta), must be &gt; 0 / 尺度参数，必须 &gt; 0
     * @return a new GammaDistribution instance / 新的 Gamma 分布实例
     * @throws IllegalArgumentException if shape or scale &le; 0 or not finite
     */
    public static GammaDistribution of(double shape, double scale) {
        if (!Double.isFinite(shape) || shape <= 0) {
            throw new IllegalArgumentException("Shape must be a positive finite number, got: " + shape);
        }
        if (!Double.isFinite(scale) || scale <= 0) {
            throw new IllegalArgumentException("Scale must be a positive finite number, got: " + scale);
        }
        return new GammaDistribution(shape, scale);
    }

    /**
     * Returns the shape parameter.
     * 返回形状参数
     *
     * @return the shape parameter / 形状参数
     */
    public double shape() {
        return shape;
    }

    /**
     * Returns the scale parameter.
     * 返回尺度参数
     *
     * @return the scale parameter / 尺度参数
     */
    public double scale() {
        return scale;
    }

    /**
     * Returns the mean of this distribution: shape * scale.
     * 返回此分布的均值：shape * scale
     *
     * @return the mean / 均值
     */
    public double mean() {
        return shape * scale;
    }

    /**
     * Returns the variance of this distribution: shape * scale^2.
     * 返回此分布的方差：shape * scale^2
     *
     * @return the variance / 方差
     */
    public double variance() {
        return shape * scale * scale;
    }

    /**
     * Computes the probability density function (PDF) at x.
     * 计算在 x 处的概率密度函数值
     *
     * <p>f(x) = x^(shape-1) * exp(-x/scale) / (scale^shape * Gamma(shape))</p>
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
            if (shape < 1) {
                return Double.POSITIVE_INFINITY;
            } else if (shape == 1) {
                return 1.0 / scale;
            } else {
                return 0.0;
            }
        }
        double logPdf = (shape - 1.0) * Math.log(x) - x / scale - logNormConst;
        return Math.exp(logPdf);
    }

    /**
     * Computes the cumulative distribution function (CDF) at x.
     * 计算在 x 处的累积分布函数值
     *
     * <p>F(x) = P(shape, x/scale) where P is the regularized lower
     * incomplete gamma function.</p>
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
        return SpecialFunctions.regularizedGammaP(shape, x / scale);
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
        double upperBound = Math.max(shape * scale * 10.0, 100.0);
        return DistributionUtils.bisect(this::cdf, p, 0.0, upperBound);
    }

    @Override
    public String toString() {
        return "GammaDistribution{shape=" + shape + ", scale=" + scale + "}";
    }
}
