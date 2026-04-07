package cloud.opencode.base.math.distribution;

import cloud.opencode.base.math.exception.MathException;
import cloud.opencode.base.math.special.SpecialFunctions;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Immutable normal (Gaussian) distribution.
 * 不可变正态（高斯）分布
 *
 * <p>Provides PDF, CDF, inverse CDF (quantile function), and random sampling.
 * Thread-safe: instances are immutable and sampling uses {@link ThreadLocalRandom}.</p>
 * <p>提供概率密度函数、累积分布函数、逆累积分布函数（分位函数）和随机抽样。
 * 线程安全：实例不可变，抽样使用 {@link ThreadLocalRandom}。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class NormalDistribution {

    /** Standard normal distribution N(0, 1). / 标准正态分布 N(0, 1) */
    public static final NormalDistribution STANDARD = new NormalDistribution(0.0, 1.0);

    private static final double SQRT_2PI = Math.sqrt(2.0 * Math.PI);
    private static final double SQRT_2 = Math.sqrt(2.0);

    private final double mean;
    private final double stdDev;
    private final double variance;

    private NormalDistribution(double mean, double stdDev) {
        this.mean = mean;
        this.stdDev = stdDev;
        this.variance = stdDev * stdDev;
    }

    /**
     * Creates a normal distribution with the specified mean and standard deviation.
     * 创建具有指定均值和标准差的正态分布
     *
     * @param mean   the mean / 均值
     * @param stdDev the standard deviation, must be &gt; 0 / 标准差，必须 &gt; 0
     * @return a new NormalDistribution instance / 新的正态分布实例
     * @throws IllegalArgumentException if stdDev &le; 0
     */
    public static NormalDistribution of(double mean, double stdDev) {
        if (stdDev <= 0) {
            throw new IllegalArgumentException("Standard deviation must be > 0, got: " + stdDev);
        }
        if (Double.isNaN(mean) || Double.isNaN(stdDev)) {
            throw new IllegalArgumentException("mean and stdDev must not be NaN");
        }
        if (Double.isInfinite(mean) || Double.isInfinite(stdDev)) {
            throw new IllegalArgumentException("mean and stdDev must be finite");
        }
        return new NormalDistribution(mean, stdDev);
    }

    /**
     * Returns the mean of this distribution.
     * 返回此分布的均值
     *
     * @return the mean / 均值
     */
    public double mean() {
        return mean;
    }

    /**
     * Returns the standard deviation of this distribution.
     * 返回此分布的标准差
     *
     * @return the standard deviation / 标准差
     */
    public double standardDeviation() {
        return stdDev;
    }

    /**
     * Returns the variance of this distribution.
     * 返回此分布的方差
     *
     * @return the variance (stdDev^2) / 方差
     */
    public double variance() {
        return variance;
    }

    /**
     * Computes the probability density function (PDF) at x.
     * 计算在 x 处的概率密度函数值
     *
     * <p>f(x) = (1 / (sigma * sqrt(2*pi))) * exp(-0.5 * ((x - mu) / sigma)^2)</p>
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
        double z = (x - mean) / stdDev;
        return Math.exp(-0.5 * z * z) / (stdDev * SQRT_2PI);
    }

    /**
     * Computes the cumulative distribution function (CDF) at x.
     * 计算在 x 处的累积分布函数值
     *
     * <p>F(x) = 0.5 * (1 + erf((x - mu) / (sigma * sqrt(2))))</p>
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
        double z = (x - mean) / (stdDev * SQRT_2);
        return 0.5 * (1.0 + SpecialFunctions.erf(z));
    }

    /**
     * Computes the inverse CDF (quantile function / percent-point function).
     * 计算逆累积分布函数（分位函数）
     *
     * <p>Uses the rational approximation algorithm by Peter Acklam for the
     * standard normal quantile, then scales by mean and standard deviation.</p>
     * <p>使用 Peter Acklam 的有理近似算法计算标准正态分位数，然后按均值和标准差缩放。</p>
     *
     * @param p the cumulative probability, must be in (0, 1) / 累积概率，必须在 (0, 1) 内
     * @return the quantile x such that P(X &le; x) = p / 使得 P(X &le; x) = p 的分位数
     * @throws MathException if p is not in (0, 1)
     */
    public double inverseCdf(double p) {
        if (p <= 0 || p >= 1 || Double.isNaN(p)) {
            throw new MathException("inverseCdf requires p in (0, 1), got: " + p);
        }
        double z = standardNormalInverseCdf(p);
        return mean + stdDev * z;
    }

    /**
     * Generates one random sample from this distribution using the Box-Muller transform.
     * 使用 Box-Muller 变换从此分布生成一个随机样本
     *
     * @return a random sample / 随机样本
     */
    public double sample() {
        // Box-Muller transform
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        double u1 = rng.nextDouble();
        double u2 = rng.nextDouble();
        // Avoid log(0)
        while (u1 == 0.0) {
            u1 = rng.nextDouble();
        }
        double z = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
        return mean + stdDev * z;
    }

    /**
     * Generates n random samples from this distribution.
     * 从此分布生成 n 个随机样本
     *
     * @param n the number of samples, must be &gt; 0 / 样本数量，必须 &gt; 0
     * @return array of n random samples / n 个随机样本的数组
     * @throws IllegalArgumentException if n &le; 0
     */
    private static final int MAX_SAMPLES = 10_000_000;

    public double[] sample(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be > 0, got: " + n);
        }
        if (n > MAX_SAMPLES) {
            throw new IllegalArgumentException("n exceeds maximum of " + MAX_SAMPLES + ", got: " + n);
        }
        double[] samples = new double[n];
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        // Box-Muller produces two independent samples per pair of uniforms
        int i = 0;
        while (i < n) {
            double u1 = rng.nextDouble();
            while (u1 == 0.0) {
                u1 = rng.nextDouble();
            }
            double u2 = rng.nextDouble();
            double r = Math.sqrt(-2.0 * Math.log(u1));
            double theta = 2.0 * Math.PI * u2;
            samples[i] = mean + stdDev * r * Math.cos(theta);
            i++;
            if (i < n) {
                samples[i] = mean + stdDev * r * Math.sin(theta);
                i++;
            }
        }
        return samples;
    }

    /**
     * Peter Acklam's rational approximation for the standard normal inverse CDF.
     * Maximum relative error: 1.15e-9.
     */
    private static double standardNormalInverseCdf(double p) {
        // Coefficients for rational approximation
        final double A1 = -3.969683028665376e+01;
        final double A2 = 2.209460984245205e+02;
        final double A3 = -2.759285104469687e+02;
        final double A4 = 1.383577518672690e+02;
        final double A5 = -3.066479806614716e+01;
        final double A6 = 2.506628277459239e+00;

        final double B1 = -5.447609879822406e+01;
        final double B2 = 1.615858368580409e+02;
        final double B3 = -1.556989798598866e+02;
        final double B4 = 6.680131188771972e+01;
        final double B5 = -1.328068155288572e+01;

        final double C1 = -7.784894002430293e-03;
        final double C2 = -3.223964580411365e-01;
        final double C3 = -2.400758277161838e+00;
        final double C4 = -2.549732539343734e+00;
        final double C5 = 4.374664141464968e+00;
        final double C6 = 2.938163982698783e+00;

        final double D1 = 7.784695709041462e-03;
        final double D2 = 3.224671290700398e-01;
        final double D3 = 2.445134137142996e+00;
        final double D4 = 3.754408661907416e+00;

        final double P_LOW = 0.02425;
        final double P_HIGH = 1.0 - P_LOW;

        double q;
        double r;

        if (p < P_LOW) {
            // Rational approximation for lower region
            q = Math.sqrt(-2.0 * Math.log(p));
            return (((((C1 * q + C2) * q + C3) * q + C4) * q + C5) * q + C6)
                    / ((((D1 * q + D2) * q + D3) * q + D4) * q + 1.0);
        } else if (p <= P_HIGH) {
            // Rational approximation for central region
            q = p - 0.5;
            r = q * q;
            return (((((A1 * r + A2) * r + A3) * r + A4) * r + A5) * r + A6) * q
                    / (((((B1 * r + B2) * r + B3) * r + B4) * r + B5) * r + 1.0);
        } else {
            // Rational approximation for upper region
            q = Math.sqrt(-2.0 * Math.log(1.0 - p));
            return -(((((C1 * q + C2) * q + C3) * q + C4) * q + C5) * q + C6)
                    / ((((D1 * q + D2) * q + D3) * q + D4) * q + 1.0);
        }
    }

    @Override
    public String toString() {
        return "NormalDistribution{mean=" + mean + ", stdDev=" + stdDev + "}";
    }
}
