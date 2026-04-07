package cloud.opencode.base.math.distribution;

import cloud.opencode.base.math.exception.MathException;
import cloud.opencode.base.math.special.SpecialFunctions;

/**
 * Immutable binomial distribution.
 * 不可变二项分布
 *
 * <p>Provides PMF, CDF, mean, and variance.
 * Thread-safe: instances are immutable.</p>
 * <p>提供概率质量函数、累积分布函数、均值和方差。
 * 线程安全：实例不可变。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class BinomialDistribution {

    private final int n;
    private final double p;
    private final double logGammaNp1;    // logGamma(n + 1), reused in every pmf() call
    private final double logP;           // Math.log(p), reused in every pmf() call
    private final double logQ;           // Math.log(1 - p), reused in every pmf() call

    private BinomialDistribution(int n, double p) {
        this.n = n;
        this.p = p;
        this.logGammaNp1 = (n > 0) ? SpecialFunctions.logGamma(n + 1.0) : 0.0;
        this.logP = (p > 0 && p < 1) ? Math.log(p) : 0.0;
        this.logQ = (p > 0 && p < 1) ? Math.log(1.0 - p) : 0.0;
    }

    /**
     * Creates a binomial distribution with the specified parameters.
     * 创建具有指定参数的二项分布
     *
     * @param n the number of trials, must be &ge; 0 / 试验次数，必须 &ge; 0
     * @param p the probability of success, must be in [0, 1] / 成功概率，必须在 [0, 1] 内
     * @return a new BinomialDistribution instance / 新的二项分布实例
     * @throws IllegalArgumentException if n &lt; 0 or p is not in [0, 1]
     */
    public static BinomialDistribution of(int n, double p) {
        if (n < 0) {
            throw new IllegalArgumentException("Number of trials must be >= 0, got: " + n);
        }
        if (!Double.isFinite(p) || p < 0 || p > 1) {
            throw new IllegalArgumentException("Probability must be in [0, 1], got: " + p);
        }
        return new BinomialDistribution(n, p);
    }

    /**
     * Returns the number of trials.
     * 返回试验次数
     *
     * @return the number of trials / 试验次数
     */
    public int trials() {
        return n;
    }

    /**
     * Returns the probability of success.
     * 返回成功概率
     *
     * @return the probability of success / 成功概率
     */
    public double probability() {
        return p;
    }

    /**
     * Returns the mean of this distribution: n * p.
     * 返回此分布的均值：n * p
     *
     * @return the mean / 均值
     */
    public double mean() {
        return n * p;
    }

    /**
     * Returns the variance of this distribution: n * p * (1 - p).
     * 返回此分布的方差：n * p * (1 - p)
     *
     * @return the variance / 方差
     */
    public double variance() {
        return n * p * (1.0 - p);
    }

    /**
     * Computes the probability mass function (PMF) at k.
     * 计算在 k 处的概率质量函数值
     *
     * <p>P(X = k) = C(n, k) * p^k * (1 - p)^(n - k), computed in log-space.</p>
     *
     * @param k the number of successes / 成功次数
     * @return the probability P(X = k) / 概率 P(X = k)
     */
    public double pmf(int k) {
        if (k < 0 || k > n) {
            return 0.0;
        }
        if (n == 0) {
            return 1.0;
        }
        if (p == 0.0) {
            return k == 0 ? 1.0 : 0.0;
        }
        if (p == 1.0) {
            return k == n ? 1.0 : 0.0;
        }
        // Compute in log-space using precomputed constants
        double logPmf = logGammaNp1
                - SpecialFunctions.logGamma(k + 1.0)
                - SpecialFunctions.logGamma(n - k + 1.0)
                + k * logP
                + (n - k) * logQ;
        return Math.exp(logPmf);
    }

    /**
     * Computes the cumulative distribution function (CDF) at k: P(X &le; k).
     * 计算在 k 处的累积分布函数值：P(X &le; k)
     *
     * <p>Uses the regularized incomplete beta function for efficiency:
     * P(X &le; k) = I(1-p, n-k, k+1).</p>
     *
     * @param k the upper limit / 上限
     * @return the cumulative probability P(X &le; k) / 累积概率
     */
    public double cdf(int k) {
        if (k < 0) {
            return 0.0;
        }
        if (k >= n) {
            return 1.0;
        }
        if (p == 0.0) {
            return 1.0;
        }
        if (p == 1.0) {
            return 0.0;
        }
        // CDF = I_{1-p}(n - k, k + 1) using regularized beta
        return SpecialFunctions.regularizedBeta(1.0 - p, n - k, k + 1.0);
    }

    @Override
    public String toString() {
        return "BinomialDistribution{n=" + n + ", p=" + p + "}";
    }
}
