package cloud.opencode.base.math.distribution;

import cloud.opencode.base.math.exception.MathException;
import cloud.opencode.base.math.special.SpecialFunctions;

/**
 * Immutable log-normal distribution.
 * 不可变对数正态分布
 *
 * <p>A random variable X follows a log-normal distribution if ln(X) is normally distributed
 * with mean mu and standard deviation sigma.
 * Provides PDF, CDF, inverse CDF (quantile function), mean, and variance.
 * Thread-safe: instances are immutable.</p>
 * <p>若 ln(X) 服从均值为 mu、标准差为 sigma 的正态分布，则随机变量 X 服从对数正态分布。
 * 提供概率密度函数、累积分布函数、逆累积分布函数（分位函数）、均值和方差。
 * 线程安全：实例不可变。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class LogNormalDistribution {

    private static final double SQRT_2PI = Math.sqrt(2.0 * Math.PI);
    private static final double SQRT_2 = Math.sqrt(2.0);

    private final double mu;
    private final double sigma;

    private LogNormalDistribution(double mu, double sigma) {
        this.mu = mu;
        this.sigma = sigma;
    }

    /**
     * Creates a log-normal distribution with the specified parameters.
     * 创建具有指定参数的对数正态分布
     *
     * @param mu    the mean of the underlying normal distribution / 底层正态分布的均值
     * @param sigma the standard deviation of the underlying normal distribution,
     *              must be &gt; 0 / 底层正态分布的标准差，必须 &gt; 0
     * @return a new LogNormalDistribution instance / 新的对数正态分布实例
     * @throws IllegalArgumentException if sigma &le; 0 or parameters are not finite
     */
    public static LogNormalDistribution of(double mu, double sigma) {
        if (!Double.isFinite(mu)) {
            throw new IllegalArgumentException("mu must be finite, got: " + mu);
        }
        if (!Double.isFinite(sigma) || sigma <= 0) {
            throw new IllegalArgumentException("sigma must be a positive finite number, got: " + sigma);
        }
        return new LogNormalDistribution(mu, sigma);
    }

    /**
     * Returns the mu parameter (mean of the log).
     * 返回 mu 参数（对数的均值）
     *
     * @return mu / mu 参数
     */
    public double mu() {
        return mu;
    }

    /**
     * Returns the sigma parameter (standard deviation of the log).
     * 返回 sigma 参数（对数的标准差）
     *
     * @return sigma / sigma 参数
     */
    public double sigma() {
        return sigma;
    }

    /**
     * Returns the mean of this distribution: exp(mu + sigma^2 / 2).
     * 返回此分布的均值：exp(mu + sigma^2 / 2)
     *
     * @return the mean / 均值
     */
    public double mean() {
        return Math.exp(mu + sigma * sigma / 2.0);
    }

    /**
     * Returns the variance of this distribution: (exp(sigma^2) - 1) * exp(2*mu + sigma^2).
     * 返回此分布的方差：(exp(sigma^2) - 1) * exp(2*mu + sigma^2)
     *
     * @return the variance / 方差
     */
    public double variance() {
        double s2 = sigma * sigma;
        return Math.expm1(s2) * Math.exp(2.0 * mu + s2);
    }

    /**
     * Computes the probability density function (PDF) at x.
     * 计算在 x 处的概率密度函数值
     *
     * <p>f(x) = 1 / (x * sigma * sqrt(2*pi)) * exp(-(ln(x) - mu)^2 / (2*sigma^2))</p>
     *
     * @param x the point at which to evaluate the PDF / 计算 PDF 的点
     * @return the density at x / x 处的密度值
     */
    public double pdf(double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        if (x <= 0 || Double.isInfinite(x)) {
            return 0.0;
        }
        double lnX = Math.log(x);
        double z = (lnX - mu) / sigma;
        return Math.exp(-0.5 * z * z) / (x * sigma * SQRT_2PI);
    }

    /**
     * Computes the cumulative distribution function (CDF) at x.
     * 计算在 x 处的累积分布函数值
     *
     * <p>F(x) = 0.5 * erfc(-(ln(x) - mu) / (sigma * sqrt(2)))</p>
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
        double z = (Math.log(x) - mu) / (sigma * SQRT_2);
        return 0.5 * SpecialFunctions.erfc(-z);
    }

    /**
     * Computes the inverse CDF (quantile function).
     * 计算逆累积分布函数（分位函数）
     *
     * <p>Since ln(X) ~ N(mu, sigma), the quantile is exp(mu + sigma * z_p)
     * where z_p is the standard normal quantile. This uses bisection for robustness.</p>
     *
     * @param p the cumulative probability, must be in (0, 1) / 累积概率，必须在 (0, 1) 内
     * @return the quantile x such that P(X &le; x) = p / 使得 P(X &le; x) = p 的分位数
     * @throws MathException if p is not in (0, 1)
     */
    public double inverseCdf(double p) {
        if (p <= 0 || p >= 1 || Double.isNaN(p)) {
            throw new MathException("inverseCdf requires p in (0, 1), got: " + p);
        }
        // Use the analytical result: Q(p) = exp(mu + sigma * Phi^{-1}(p))
        // where Phi^{-1} is the standard normal inverse CDF
        double zp = NormalDistribution.STANDARD.inverseCdf(p);
        return Math.exp(mu + sigma * zp);
    }

    @Override
    public String toString() {
        return "LogNormalDistribution{mu=" + mu + ", sigma=" + sigma + "}";
    }
}
