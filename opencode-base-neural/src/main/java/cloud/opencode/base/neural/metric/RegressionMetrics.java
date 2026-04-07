package cloud.opencode.base.neural.metric;

import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

/**
 * Regression Metrics Utility
 * 回归指标工具类
 *
 * <p>Provides standard regression evaluation metrics: Mean Squared Error (MSE),
 * Mean Absolute Error (MAE), Root Mean Squared Error (RMSE), and R-squared
 * (coefficient of determination). All methods return scalar tensors.</p>
 * <p>提供标准回归评估指标：均方误差（MSE）、平均绝对误差（MAE）、
 * 均方根误差（RMSE）和 R 平方（决定系数）。所有方法返回标量张量。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>MSE: Mean Squared Error | 均方误差</li>
 *   <li>MAE: Mean Absolute Error | 平均绝对误差</li>
 *   <li>RMSE: Root Mean Squared Error | 均方根误差</li>
 *   <li>R-squared: Coefficient of determination | 决定系数</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Metric
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class RegressionMetrics {

    private RegressionMetrics() {
        // utility class
    }

    /**
     * Compute Mean Squared Error: (1/N) * sum((predicted - target)^2)
     * 计算均方误差：(1/N) * sum((predicted - target)^2)
     *
     * @param predicted predicted tensor | 预测张量
     * @param target    target tensor | 目标张量
     * @return scalar tensor containing MSE value | 包含 MSE 值的标量张量
     * @throws cloud.opencode.base.neural.exception.NeuralException if inputs are invalid | 如果输入无效
     */
    public static Tensor mse(Tensor predicted, Tensor target) {
        validateRegressionInputs(predicted, target);

        float[] pred = predicted.toFloatArray();
        float[] tgt = target.toFloatArray();
        int n = pred.length;

        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            double diff = (double) pred[i] - tgt[i];
            sum += diff * diff;
        }

        float mse = (float) (sum / n);
        return Tensor.scalar(mse);
    }

    /**
     * Compute Mean Absolute Error: (1/N) * sum(|predicted - target|)
     * 计算平均绝对误差：(1/N) * sum(|predicted - target|)
     *
     * @param predicted predicted tensor | 预测张量
     * @param target    target tensor | 目标张量
     * @return scalar tensor containing MAE value | 包含 MAE 值的标量张量
     * @throws cloud.opencode.base.neural.exception.NeuralException if inputs are invalid | 如果输入无效
     */
    public static Tensor mae(Tensor predicted, Tensor target) {
        validateRegressionInputs(predicted, target);

        float[] pred = predicted.toFloatArray();
        float[] tgt = target.toFloatArray();
        int n = pred.length;

        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += Math.abs((double) pred[i] - tgt[i]);
        }

        float mae = (float) (sum / n);
        return Tensor.scalar(mae);
    }

    /**
     * Compute Root Mean Squared Error: sqrt(MSE)
     * 计算均方根误差：sqrt(MSE)
     *
     * @param predicted predicted tensor | 预测张量
     * @param target    target tensor | 目标张量
     * @return scalar tensor containing RMSE value | 包含 RMSE 值的标量张量
     * @throws cloud.opencode.base.neural.exception.NeuralException if inputs are invalid | 如果输入无效
     */
    public static Tensor rmse(Tensor predicted, Tensor target) {
        validateRegressionInputs(predicted, target);

        float[] pred = predicted.toFloatArray();
        float[] tgt = target.toFloatArray();
        int n = pred.length;

        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            double diff = (double) pred[i] - tgt[i];
            sum += diff * diff;
        }

        float rmse = (float) Math.sqrt(sum / n);
        return Tensor.scalar(rmse);
    }

    /**
     * Compute R-squared (coefficient of determination): 1 - SS_res / SS_tot
     * 计算 R 平方（决定系数）：1 - SS_res / SS_tot
     *
     * <p>Where SS_res = sum((target - predicted)^2) and SS_tot = sum((target - mean(target))^2).
     * Returns 0 when SS_tot = 0 (all target values are identical).</p>
     * <p>其中 SS_res = sum((target - predicted)^2)，SS_tot = sum((target - mean(target))^2)。
     * 当 SS_tot = 0（所有目标值相同）时返回 0。</p>
     *
     * @param predicted predicted tensor | 预测张量
     * @param target    target tensor | 目标张量
     * @return scalar tensor containing R-squared value | 包含 R 平方值的标量张量
     * @throws cloud.opencode.base.neural.exception.NeuralException if inputs are invalid | 如果输入无效
     */
    public static Tensor rSquared(Tensor predicted, Tensor target) {
        validateRegressionInputs(predicted, target);

        float[] pred = predicted.toFloatArray();
        float[] tgt = target.toFloatArray();
        int n = pred.length;

        // Compute mean of target using double for precision
        double targetSum = 0.0;
        for (int i = 0; i < n; i++) {
            targetSum += tgt[i];
        }
        double targetMean = targetSum / n;

        // Compute SS_res and SS_tot
        double ssRes = 0.0;
        double ssTot = 0.0;
        for (int i = 0; i < n; i++) {
            double residual = tgt[i] - (double) pred[i];
            ssRes += residual * residual;
            double deviation = tgt[i] - targetMean;
            ssTot += deviation * deviation;
        }

        if (ssTot == 0.0) {
            return Tensor.scalar(0.0f);
        }

        float r2 = (float) (1.0 - ssRes / ssTot);
        return Tensor.scalar(r2);
    }

    /**
     * Validate regression metric inputs
     * 验证回归指标输入
     */
    private static void validateRegressionInputs(Tensor predicted, Tensor target) {
        MetricValidation.requireNonNull(predicted, "predicted");
        MetricValidation.requireNonNull(target, "target");
        MetricValidation.requireSameShape(predicted, target);
        MetricValidation.requirePositiveSize(predicted.shape().size());
    }
}
