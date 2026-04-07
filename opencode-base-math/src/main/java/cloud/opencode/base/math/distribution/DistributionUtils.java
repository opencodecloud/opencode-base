package cloud.opencode.base.math.distribution;

import cloud.opencode.base.math.exception.MathException;

import java.util.function.DoubleUnaryOperator;

/**
 * Internal utility methods shared by distribution classes.
 * 分布类共享的内部工具方法
 *
 * <p>Not part of the public API. Contains the bisection root-finding algorithm
 * used by inverse CDF implementations.</p>
 * <p>非公开 API。包含逆累积分布函数实现使用的二分法求根算法。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
final class DistributionUtils {

    private static final double TOLERANCE = 1e-12;
    private static final int MAX_ITERATIONS = 100;

    private DistributionUtils() {
        throw new AssertionError("No instances");
    }

    /**
     * Finds x such that cdf(x) = target using bisection search.
     * 使用二分法查找满足 cdf(x) = target 的 x
     *
     * <p>The search interval [lo, hi] is automatically expanded if the initial bounds
     * do not bracket the target value.</p>
     *
     * @param cdf    the CDF function / 累积分布函数
     * @param target the target probability / 目标概率
     * @param lo     initial lower bound / 初始下界
     * @param hi     initial upper bound / 初始上界
     * @return x such that |cdf(x) - target| &lt; tolerance / 满足精度要求的 x
     * @throws MathException if bisection does not converge
     */
    static double bisect(DoubleUnaryOperator cdf, double target, double lo, double hi) {
        // Expand bounds if needed, caching CDF results to avoid redundant evaluations
        double cdfLo = cdf.applyAsDouble(lo);
        for (int i = 0; i < 200 && cdfLo > target; i++) {
            lo = lo == 0 ? -1.0 : lo * 2.0;
            if (!Double.isFinite(lo)) break;
            cdfLo = cdf.applyAsDouble(lo);
        }
        double cdfHi = cdf.applyAsDouble(hi);
        for (int i = 0; i < 200 && cdfHi < target; i++) {
            hi = hi == 0 ? 1.0 : hi * 2.0;
            if (!Double.isFinite(hi)) break;
            cdfHi = cdf.applyAsDouble(hi);
        }
        // Verify bracketing using cached results
        if (!Double.isFinite(lo) || !Double.isFinite(hi)
                || cdfLo > target || cdfHi < target) {
            throw new MathException("Bisection failed: could not bracket target probability " + target);
        }

        double mid = lo;
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            mid = lo + (hi - lo) / 2.0;
            double cdfMid = cdf.applyAsDouble(mid);
            if (Math.abs(cdfMid - target) < TOLERANCE) {
                return mid;
            }
            if (cdfMid < target) {
                lo = mid;
            } else {
                hi = mid;
            }
            if (hi - lo < TOLERANCE * Math.max(1.0, Math.abs(mid))) {
                return mid;
            }
        }
        return mid;
    }
}
