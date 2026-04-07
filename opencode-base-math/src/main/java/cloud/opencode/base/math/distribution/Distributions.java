package cloud.opencode.base.math.distribution;

import java.util.function.DoubleUnaryOperator;

/**
 * Utility class providing common probability distribution functions.
 * 提供常用概率分布函数的工具类
 *
 * <p>Includes uniform, exponential, and Poisson distribution helpers.
 * All methods are stateless and thread-safe.</p>
 * <p>包括均匀分布、指数分布和泊松分布辅助方法。
 * 所有方法无状态且线程安全。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class Distributions {

    private Distributions() {
        throw new AssertionError("No instances");
    }

    /**
     * Returns the PDF of a uniform distribution on [min, max].
     * 返回 [min, max] 上均匀分布的概率密度函数
     *
     * <p>The returned operator evaluates to 1/(max-min) for x in [min, max], and 0 otherwise.</p>
     * <p>返回的算子在 x 属于 [min, max] 时值为 1/(max-min)，否则为 0。</p>
     *
     * @param min the lower bound / 下界
     * @param max the upper bound, must be &gt; min / 上界，必须 &gt; min
     * @return a DoubleUnaryOperator representing the PDF / 表示 PDF 的 DoubleUnaryOperator
     * @throws IllegalArgumentException if min &ge; max or values are not finite
     */
    public static DoubleUnaryOperator uniform(double min, double max) {
        if (!Double.isFinite(min) || !Double.isFinite(max)) {
            throw new IllegalArgumentException("min and max must be finite, got min=" + min + ", max=" + max);
        }
        if (min >= max) {
            throw new IllegalArgumentException("min must be < max, got min=" + min + ", max=" + max);
        }
        double density = 1.0 / (max - min);
        return x -> (x >= min && x <= max) ? density : 0.0;
    }

    /**
     * Computes the PDF of the exponential distribution at x.
     * 计算指数分布在 x 处的概率密度函数值
     *
     * <p>f(x) = lambda * exp(-lambda * x) for x &ge; 0, 0 otherwise.</p>
     *
     * @param lambda the rate parameter, must be &gt; 0 / 速率参数，必须 &gt; 0
     * @param x      the point at which to evaluate / 计算点
     * @return the PDF value at x / x 处的 PDF 值
     * @throws IllegalArgumentException if lambda &le; 0
     */
    public static double exponentialPdf(double lambda, double x) {
        validateLambda(lambda);
        if (x < 0) {
            return 0.0;
        }
        return lambda * Math.exp(-lambda * x);
    }

    /**
     * Computes the CDF of the exponential distribution at x.
     * 计算指数分布在 x 处的累积分布函数值
     *
     * <p>F(x) = 1 - exp(-lambda * x) for x &ge; 0, 0 otherwise.</p>
     *
     * @param lambda the rate parameter, must be &gt; 0 / 速率参数，必须 &gt; 0
     * @param x      the point at which to evaluate / 计算点
     * @return the CDF value at x / x 处的 CDF 值
     * @throws IllegalArgumentException if lambda &le; 0
     */
    public static double exponentialCdf(double lambda, double x) {
        validateLambda(lambda);
        if (x < 0) {
            return 0.0;
        }
        // Use -expm1 for better precision near 0
        return -Math.expm1(-lambda * x);
    }

    /**
     * Computes the probability mass function (PMF) of the Poisson distribution.
     * 计算泊松分布的概率质量函数
     *
     * <p>P(X = k) = (lambda^k * exp(-lambda)) / k!</p>
     * <p>Computed in log-space for numerical stability.</p>
     * <p>在对数空间中计算以保证数值稳定性。</p>
     *
     * @param lambda the rate parameter, must be &gt; 0 / 速率参数，必须 &gt; 0
     * @param k      the number of events, must be &ge; 0 / 事件数，必须 &ge; 0
     * @return P(X = k) / 概率质量函数值
     * @throws IllegalArgumentException if lambda &le; 0 or k &lt; 0
     */
    public static double poissonPmf(double lambda, int k) {
        validateLambda(lambda);
        if (k < 0) {
            throw new IllegalArgumentException("k must be >= 0, got: " + k);
        }
        // Compute in log-space: k*ln(lambda) - lambda - ln(k!)
        // ln(k!) = sum(ln(i)) for i=1..k, or use logGamma(k+1)
        double logPmf = k * Math.log(lambda) - lambda - logFactorial(k);
        return Math.exp(logPmf);
    }

    /**
     * Computes the CDF of the Poisson distribution: P(X &le; k).
     * 计算泊松分布的累积分布函数：P(X &le; k)
     *
     * <p>Computed as the sum of PMF values from 0 to k.</p>
     * <p>通过求和 PMF 值（从 0 到 k）计算。</p>
     *
     * @param lambda the rate parameter, must be &gt; 0 / 速率参数，必须 &gt; 0
     * @param k      the upper limit, must be &ge; 0 / 上限，必须 &ge; 0
     * @return P(X &le; k) / 累积分布函数值
     * @throws IllegalArgumentException if lambda &le; 0 or k &lt; 0
     */
    public static double poissonCdf(double lambda, int k) {
        validateLambda(lambda);
        if (k < 0) {
            throw new IllegalArgumentException("k must be >= 0, got: " + k);
        }
        if (k > 1_000_000) {
            throw new IllegalArgumentException("k exceeds max 1000000 for Poisson CDF, got: " + k);
        }
        // Sum PMFs in log-space for stability, with early termination
        double sum = 0.0;
        double logLambda = Math.log(lambda);
        double logTerm = -lambda; // log(PMF(0)) = -lambda
        sum += Math.exp(logTerm);
        for (int i = 1; i <= k; i++) {
            logTerm += logLambda - Math.log(i);
            sum += Math.exp(logTerm);
            // Early termination: remaining terms are negligible when sum ≈ 1
            if (sum >= 1.0 - 1e-15) {
                return 1.0;
            }
        }
        return Math.min(sum, 1.0); // Clamp to [0, 1] for floating-point safety
    }

    /**
     * Computes ln(k!) using iterative summation for small k.
     */
    private static double logFactorial(int k) {
        if (k <= 1) {
            return 0.0;
        }
        return cloud.opencode.base.math.special.SpecialFunctions.logGamma(k + 1.0);
    }

    private static void validateLambda(double lambda) {
        if (lambda <= 0 || !Double.isFinite(lambda)) {
            throw new IllegalArgumentException("lambda must be a positive finite number, got: " + lambda);
        }
    }
}
