package cloud.opencode.base.math.distribution;

import cloud.opencode.base.math.exception.MathException;
import cloud.opencode.base.math.special.SpecialFunctions;

/**
 * Immutable F-distribution (Fisher-Snedecor distribution).
 * 不可变 F 分布（Fisher-Snedecor 分布）
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
public final class FDistribution {

    private final double df1;
    private final double df2;
    private final double halfDf1;        // df1 / 2.0, used in every pdf()/cdf() call
    private final double halfDf2;        // df2 / 2.0, used in every cdf() call
    private final double halfDf1Sum;     // (df1 + df2) / 2.0, used in every pdf() call
    private final double logPdfConst;

    private FDistribution(double df1, double df2) {
        this.df1 = df1;
        this.df2 = df2;
        this.halfDf1 = df1 / 2.0;
        this.halfDf2 = df2 / 2.0;
        this.halfDf1Sum = (df1 + df2) / 2.0;
        // Precompute log of the constant part of PDF
        this.logPdfConst = halfDf1 * Math.log(df1)
                + halfDf2 * Math.log(df2)
                + SpecialFunctions.logGamma(halfDf1Sum)
                - SpecialFunctions.logGamma(halfDf1)
                - SpecialFunctions.logGamma(halfDf2);
    }

    /**
     * Creates an F-distribution with the specified degrees of freedom.
     * 创建具有指定自由度的 F 分布
     *
     * @param df1 numerator degrees of freedom, must be &gt; 0 / 分子自由度，必须 &gt; 0
     * @param df2 denominator degrees of freedom, must be &gt; 0 / 分母自由度，必须 &gt; 0
     * @return a new FDistribution instance / 新的 F 分布实例
     * @throws IllegalArgumentException if df1 or df2 &le; 0 or not finite
     */
    public static FDistribution of(double df1, double df2) {
        if (!Double.isFinite(df1) || df1 <= 0) {
            throw new IllegalArgumentException(
                    "Numerator df must be a positive finite number, got: " + df1);
        }
        if (!Double.isFinite(df2) || df2 <= 0) {
            throw new IllegalArgumentException(
                    "Denominator df must be a positive finite number, got: " + df2);
        }
        return new FDistribution(df1, df2);
    }

    /**
     * Returns the numerator degrees of freedom.
     * 返回分子自由度
     *
     * @return the numerator degrees of freedom / 分子自由度
     */
    public double numeratorDf() {
        return df1;
    }

    /**
     * Returns the denominator degrees of freedom.
     * 返回分母自由度
     *
     * @return the denominator degrees of freedom / 分母自由度
     */
    public double denominatorDf() {
        return df2;
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
        if (x < 0 || Double.isInfinite(x)) {
            return 0.0;
        }
        if (x == 0.0) {
            if (df1 < 2) {
                return Double.POSITIVE_INFINITY;
            } else if (df1 == 2) {
                return 1.0;
            } else {
                return 0.0;
            }
        }
        double logPdf = logPdfConst
                + (halfDf1 - 1.0) * Math.log(x)
                - halfDf1Sum * Math.log(df1 * x + df2);
        return Math.exp(logPdf);
    }

    /**
     * Computes the cumulative distribution function (CDF) at x.
     * 计算在 x 处的累积分布函数值
     *
     * <p>F(x) = I(df1*x/(df1*x+df2), df1/2, df2/2) where I is the regularized
     * incomplete beta function.</p>
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
        double t = df1 * x / (df1 * x + df2);
        return SpecialFunctions.regularizedBeta(t, halfDf1, halfDf2);
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
        double upperBound = Math.max(df1 + df2, 10.0) * 10.0;
        return DistributionUtils.bisect(this::cdf, p, 0.0, upperBound);
    }

    @Override
    public String toString() {
        return "FDistribution{df1=" + df1 + ", df2=" + df2 + "}";
    }
}
