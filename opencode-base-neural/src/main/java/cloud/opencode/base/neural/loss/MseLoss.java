package cloud.opencode.base.neural.loss;

import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

/**
 * Mean Squared Error Loss Function
 * 均方误差损失函数
 *
 * <p>Computes the mean squared error between predicted and target tensors:
 * {@code L = mean((predicted - target)²)}.</p>
 * <p>计算预测张量和目标张量之间的均方误差：
 * {@code L = mean((predicted - target)²)}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Element-wise squared difference - 逐元素平方差</li>
 *   <li>Mean reduction to scalar - 均值归约为标量</li>
 *   <li>Shape validation - 形状验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LossFunction mse = new MseLoss();
 * Tensor predicted = Tensor.fromFloat(new float[]{1.0f, 2.0f, 3.0f}, Shape.of(3));
 * Tensor target = Tensor.fromFloat(new float[]{1.5f, 2.5f, 3.5f}, Shape.of(3));
 * Tensor loss = mse.compute(predicted, target);
 * // loss = mean((0.5² + 0.5² + 0.5²)) = 0.25
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see LossFunction
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class MseLoss implements LossFunction {

    /**
     * Create a new MSE loss function
     * 创建新的均方误差损失函数
     */
    public MseLoss() {
        // stateless
    }

    /**
     * Compute Mean Squared Error loss
     * 计算均方误差损失
     *
     * @param predicted the predicted output tensor | 预测输出张量
     * @param target    the ground truth target tensor | 真实目标张量
     * @return a scalar tensor containing the MSE loss value | 包含MSE损失值的标量张量
     * @throws cloud.opencode.base.neural.exception.NeuralException
     *         if inputs are null or shapes mismatch |
     *         当输入为空或形状不匹配时抛出
     */
    @Override
    public Tensor compute(Tensor predicted, Tensor target) {
        LossUtil.validateInputs(predicted, target);

        float[] predData = predicted.toFloatArray();
        float[] targetData = target.toFloatArray();
        int n = predData.length;

        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            double diff = (double) predData[i] - (double) targetData[i];
            sum += diff * diff;
        }

        float mse = (float) (sum / n);
        return Tensor.scalar(mse);
    }
}
