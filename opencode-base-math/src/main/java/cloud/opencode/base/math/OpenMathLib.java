package cloud.opencode.base.math;

import cloud.opencode.base.math.analysis.Differentiation;
import cloud.opencode.base.math.analysis.RootFinder;
import cloud.opencode.base.math.combinatorics.Combinatorics;
import cloud.opencode.base.math.distribution.NormalDistribution;
import cloud.opencode.base.math.integration.NumericalIntegration;
import cloud.opencode.base.math.interpolation.Interpolation;
import cloud.opencode.base.math.linalg.Matrix;
import cloud.opencode.base.math.linalg.Vector;
import cloud.opencode.base.math.special.SpecialFunctions;
import cloud.opencode.base.math.stats.Regression;
import cloud.opencode.base.math.stats.Statistics;
import cloud.opencode.base.math.stats.StreamingStatistics;
import cloud.opencode.base.math.stats.inference.TestResult;
import cloud.opencode.base.math.stats.inference.TTest;

import java.util.function.DoubleUnaryOperator;

/**
 * OpenMathLib - Unified facade for the opencode-base-math module
 * OpenMathLib - opencode-base-math 模块统一门面
 *
 * <p>Provides convenient static access to the most frequently used mathematical
 * functions from all sub-packages. For advanced or less common operations,
 * use the individual classes directly (e.g., {@link Statistics}, {@link Matrix},
 * {@link Combinatorics}).</p>
 * <p>提供对所有子包中最常用数学函数的便捷静态访问。对于高级或不常见的操作，
 * 请直接使用各个类（如 {@link Statistics}、{@link Matrix}、{@link Combinatorics}）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Statistics: percentile, correlation, regression, streaming - 统计: 百分位数、相关、回归、流式</li>
 *   <li>Linear Algebra: vector/matrix creation - 线性代数: 向量/矩阵创建</li>
 *   <li>Analysis: root finding, differentiation, integration, interpolation - 分析: 求根、微分、积分、插值</li>
 *   <li>Combinatorics: binomial, permutation - 组合数学: 二项式、排列</li>
 *   <li>Distributions: normal, t, chi-squared, etc. - 概率分布: 正态、t、卡方等</li>
 *   <li>Special functions: gamma, beta, erf - 特殊函数: gamma、beta、erf</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // One-stop import
 * import cloud.opencode.base.math.OpenMathLib;
 *
 * // Statistics
 * double median = OpenMathLib.percentile(data, 50);
 * double r = OpenMathLib.correlation(x, y);
 *
 * // Root finding
 * double sqrt2 = OpenMathLib.findRoot(x -> x * x - 2, 0, 2);
 *
 * // Integration
 * double area = OpenMathLib.integrate(Math::sin, 0, Math.PI);
 *
 * // Combinatorics
 * long c = OpenMathLib.binomial(20, 10);
 *
 * // Special functions
 * double g = OpenMathLib.gamma(5.0);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class OpenMathLib {

    private OpenMathLib() {
    }

    // ==================== Statistics | 统计 ====================

    /**
     * Computes the p-th percentile of the data.
     * 计算数据的第 p 百分位数
     *
     * @param data the data array / 数据数组
     * @param p    the percentile in [0, 100] / 百分位数 [0, 100]
     * @return the percentile value / 百分位数值
     */
    public static double percentile(double[] data, double p) {
        return Statistics.percentile(data, p);
    }

    /**
     * Computes the Pearson correlation coefficient.
     * 计算 Pearson 相关系数
     *
     * @param x first array / 第一个数组
     * @param y second array / 第二个数组
     * @return correlation in [-1, 1] / 相关系数 [-1, 1]
     */
    public static double correlation(double[] x, double[] y) {
        return Statistics.correlation(x, y);
    }

    /**
     * Computes the sample covariance.
     * 计算样本协方差
     *
     * @param x first array / 第一个数组
     * @param y second array / 第二个数组
     * @return the covariance / 协方差
     */
    public static double covariance(double[] x, double[] y) {
        return Statistics.covariance(x, y);
    }

    /**
     * Computes Spearman's rank correlation coefficient.
     * 计算 Spearman 等级相关系数
     *
     * @param x first array / 第一个数组
     * @param y second array / 第二个数组
     * @return Spearman's rho in [-1, 1] / Spearman rho [-1, 1]
     */
    public static double spearmanCorrelation(double[] x, double[] y) {
        return Statistics.spearmanCorrelation(x, y);
    }

    /**
     * Performs simple linear regression.
     * 执行简单线性回归
     *
     * @param x independent variable / 自变量
     * @param y dependent variable / 因变量
     * @return the linear model / 线性模型
     */
    public static Regression.LinearModel linearRegression(double[] x, double[] y) {
        return Regression.linear(x, y);
    }

    /**
     * Creates a new streaming statistics accumulator.
     * 创建新的流式统计累加器
     *
     * @return a new empty accumulator / 新的空累加器
     */
    public static StreamingStatistics streamingStats() {
        return StreamingStatistics.create();
    }

    // ==================== Hypothesis Testing | 假设检验 ====================

    /**
     * Performs a one-sample t-test.
     * 执行单样本 t 检验
     *
     * @param data the sample data / 样本数据
     * @param mu0  the hypothesized mean / 假设均值
     * @return the test result / 检验结果
     */
    public static TestResult tTestOneSample(double[] data, double mu0) {
        return TTest.oneSample(data, mu0);
    }

    /**
     * Performs Welch's two-sample t-test.
     * 执行 Welch 双样本 t 检验
     *
     * @param x first sample / 第一个样本
     * @param y second sample / 第二个样本
     * @return the test result / 检验结果
     */
    public static TestResult tTestTwoSample(double[] x, double[] y) {
        return TTest.twoSample(x, y);
    }

    // ==================== Linear Algebra | 线性代数 ====================

    /**
     * Creates a vector from components.
     * 从分量创建向量
     *
     * @param components the components / 分量
     * @return a new vector / 新向量
     */
    public static Vector vector(double... components) {
        return Vector.of(components);
    }

    /**
     * Creates a matrix from a 2D array.
     * 从二维数组创建矩阵
     *
     * @param data the matrix data / 矩阵数据
     * @return a new matrix / 新矩阵
     */
    public static Matrix matrix(double[][] data) {
        return Matrix.of(data);
    }

    /**
     * Creates an identity matrix of size n.
     * 创建 n 阶单位矩阵
     *
     * @param n the size / 阶数
     * @return the identity matrix / 单位矩阵
     */
    public static Matrix identityMatrix(int n) {
        return Matrix.identity(n);
    }

    // ==================== Analysis | 数值分析 ====================

    /**
     * Finds a root of f in [a, b] using Brent's method (recommended).
     * 使用 Brent 法在 [a, b] 内求 f 的根（推荐）
     *
     * @param f the function / 函数
     * @param a left endpoint / 左端点
     * @param b right endpoint / 右端点
     * @return the root / 根
     */
    public static double findRoot(DoubleUnaryOperator f, double a, double b) {
        return RootFinder.brent(f, a, b, 1e-12);
    }

    /**
     * Finds a root of f in [a, b] with specified tolerance.
     * 使用指定容差在 [a, b] 内求 f 的根
     *
     * @param f         the function / 函数
     * @param a         left endpoint / 左端点
     * @param b         right endpoint / 右端点
     * @param tolerance convergence tolerance / 收敛容差
     * @return the root / 根
     */
    public static double findRoot(DoubleUnaryOperator f, double a, double b, double tolerance) {
        return RootFinder.brent(f, a, b, tolerance);
    }

    /**
     * Computes the numerical derivative of f at x.
     * 计算 f 在 x 处的数值导数
     *
     * @param f the function / 函数
     * @param x the point / 点
     * @return the approximate derivative / 近似导数
     */
    public static double derivative(DoubleUnaryOperator f, double x) {
        return Differentiation.derivative(f, x);
    }

    /**
     * Numerically integrates f from a to b using Simpson's rule with n=1000.
     * 使用 Simpson 法则对 f 从 a 到 b 数值积分（n=1000）
     *
     * @param f the integrand / 被积函数
     * @param a lower bound / 下限
     * @param b upper bound / 上限
     * @return the approximate integral / 近似积分值
     */
    public static double integrate(DoubleUnaryOperator f, double a, double b) {
        return NumericalIntegration.simpson(f, a, b, 1000);
    }

    /**
     * Interpolates using piecewise linear interpolation.
     * 分段线性插值
     *
     * @param x  the x data points (sorted ascending) / x 数据点（升序）
     * @param y  the y data points / y 数据点
     * @param xi the query point / 查询点
     * @return the interpolated value / 插值结果
     */
    public static double interpolate(double[] x, double[] y, double xi) {
        return Interpolation.linear(x, y, xi);
    }

    // ==================== Combinatorics | 组合数学 ====================

    /**
     * Computes the binomial coefficient C(n, k).
     * 计算二项式系数 C(n, k)
     *
     * @param n total items / 总数
     * @param k items to choose / 选取数
     * @return C(n, k) / 二项式系数
     */
    public static long binomial(int n, int k) {
        return Combinatorics.binomial(n, k);
    }

    /**
     * Computes the permutation P(n, k).
     * 计算排列数 P(n, k)
     *
     * @param n total items / 总数
     * @param k items to arrange / 排列数
     * @return P(n, k) / 排列数
     */
    public static long permutation(int n, int k) {
        return Combinatorics.permutation(n, k);
    }

    // ==================== Distributions | 概率分布 ====================

    /**
     * Returns the standard normal distribution N(0, 1).
     * 返回标准正态分布 N(0, 1)
     *
     * @return the standard normal / 标准正态分布
     */
    public static NormalDistribution standardNormal() {
        return NormalDistribution.STANDARD;
    }

    // ==================== Special Functions | 特殊函数 ====================

    /**
     * Computes the gamma function.
     * 计算 Gamma 函数
     *
     * @param x the input / 输入值
     * @return Gamma(x) / Gamma 函数值
     */
    public static double gamma(double x) {
        return SpecialFunctions.gamma(x);
    }

    /**
     * Computes the error function.
     * 计算误差函数
     *
     * @param x the input / 输入值
     * @return erf(x) / 误差函数值
     */
    public static double erf(double x) {
        return SpecialFunctions.erf(x);
    }

    /**
     * Computes the beta function.
     * 计算 Beta 函数
     *
     * @param a first parameter / 第一个参数
     * @param b second parameter / 第二个参数
     * @return Beta(a, b) / Beta 函数值
     */
    public static double beta(double a, double b) {
        return SpecialFunctions.beta(a, b);
    }
}
