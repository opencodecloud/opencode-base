package cloud.opencode.base.math.stats;

/**
 * Regression analysis utilities.
 * 回归分析工具类
 *
 * <p>Provides least-squares linear regression with R-squared goodness-of-fit.</p>
 * <p>提供最小二乘线性回归及 R 平方拟合优度。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class Regression {

    private Regression() {
        throw new AssertionError("No instances");
    }

    /**
     * Performs ordinary least-squares linear regression.
     * 执行普通最小二乘线性回归
     *
     * <p>Fits the model y = slope * x + intercept that minimizes the sum of squared residuals.</p>
     * <p>拟合最小化残差平方和的模型 y = slope * x + intercept。</p>
     *
     * @param x the independent variable values / 自变量值
     * @param y the dependent variable values / 因变量值
     * @return a {@link LinearModel} containing slope, intercept, and R-squared / 包含斜率、截距和 R 平方的线性模型
     * @throws IllegalArgumentException if arrays are null, empty, different lengths, or have fewer than 2 elements
     */
    public static LinearModel linear(double[] x, double[] y) {
        requireValidBasic(x, y);

        int n = x.length;

        // Two-pass algorithm for numerical stability
        // Pass 1: compute means with inline finite check (eliminates separate O(n) validation)
        double sumX = 0.0;
        double sumY = 0.0;
        for (int i = 0; i < n; i++) {
            if (!Double.isFinite(x[i])) {
                throw new IllegalArgumentException("x[" + i + "] is not finite: " + x[i]);
            }
            if (!Double.isFinite(y[i])) {
                throw new IllegalArgumentException("y[" + i + "] is not finite: " + y[i]);
            }
            sumX += x[i];
            sumY += y[i];
        }
        double meanX = sumX / n;
        double meanY = sumY / n;

        // Pass 2: compute deviations from mean
        double sxx = 0.0;
        double sxy = 0.0;
        double syy = 0.0;
        for (int i = 0; i < n; i++) {
            double dx = x[i] - meanX;
            double dy = y[i] - meanY;
            sxx += dx * dx;
            sxy += dx * dy;
            syy += dy * dy;
        }

        double slope;
        double intercept;
        double rSquared;

        if (sxx == 0.0) {
            // All x values are identical; slope is undefined, use 0
            slope = 0.0;
            intercept = meanY;
            rSquared = 0.0;
        } else {
            slope = sxy / sxx;
            intercept = meanY - slope * meanX;
            if (syy == 0.0) {
                // All y values are identical; perfect fit (constant)
                rSquared = 1.0;
            } else {
                rSquared = (sxy * sxy) / (sxx * syy);
                // Clamp to [0, 1] to handle floating point rounding
                rSquared = Math.max(0.0, Math.min(1.0, rSquared));
            }
        }

        return new LinearModel(slope, intercept, rSquared);
    }

    /**
     * Result of a linear regression: y = slope * x + intercept.
     * 线性回归结果：y = slope * x + intercept
     *
     * @param slope     the slope of the regression line / 回归线斜率
     * @param intercept the y-intercept of the regression line / 回归线截距
     * @param rSquared  the coefficient of determination (R^2) / 决定系数（R^2）
     * @author Leon Soo
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-math V1.0.3
     */
    public record LinearModel(double slope, double intercept, double rSquared) {

        /**
         * Predicts the y value for a given x.
         * 预测给定 x 值对应的 y 值
         *
         * @param x the independent variable value / 自变量值
         * @return the predicted y value / 预测的 y 值
         */
        public double predict(double x) {
            return slope * x + intercept;
        }

        /**
         * Computes residuals (y_actual - y_predicted) for the given data.
         * 计算给定数据的残差（实际值 - 预测值）
         *
         * @param x the independent variable values / 自变量值
         * @param y the actual dependent variable values / 实际因变量值
         * @return array of residuals / 残差数组
         * @throws IllegalArgumentException if arrays are null, empty, or different lengths
         */
        public double[] residuals(double[] x, double[] y) {
            if (x == null) {
                throw new IllegalArgumentException("x must not be null");
            }
            if (y == null) {
                throw new IllegalArgumentException("y must not be null");
            }
            if (x.length != y.length) {
                throw new IllegalArgumentException(
                        "Arrays must have the same length, got: " + x.length + " and " + y.length);
            }
            if (x.length == 0) {
                throw new IllegalArgumentException("Arrays must not be empty");
            }
            double[] residuals = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                residuals[i] = y[i] - predict(x[i]);
            }
            return residuals;
        }
    }

    /**
     * Basic validation: null, empty, length checks only.
     * Finite checks are inlined into the computation loop in linear().
     */
    private static void requireValidBasic(double[] x, double[] y) {
        if (x == null) {
            throw new IllegalArgumentException("x must not be null");
        }
        if (y == null) {
            throw new IllegalArgumentException("y must not be null");
        }
        if (x.length == 0) {
            throw new IllegalArgumentException("x must not be empty");
        }
        if (y.length == 0) {
            throw new IllegalArgumentException("y must not be empty");
        }
        if (x.length != y.length) {
            throw new IllegalArgumentException(
                    "Arrays must have the same length, got: " + x.length + " and " + y.length);
        }
        if (x.length < 2) {
            throw new IllegalArgumentException("Linear regression requires at least 2 data points, got: " + x.length);
        }
    }
}
