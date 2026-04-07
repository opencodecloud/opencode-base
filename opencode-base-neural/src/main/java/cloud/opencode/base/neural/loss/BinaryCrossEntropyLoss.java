package cloud.opencode.base.neural.loss;

import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

/**
 * Binary Cross-Entropy Loss Function
 * 二元交叉熵损失函数
 *
 * <p>Computes binary cross-entropy loss between predicted probabilities and binary targets:
 * {@code L = -mean(target * log(predicted) + (1 - target) * log(1 - predicted))}.</p>
 * <p>计算预测概率与二元目标之间的二元交叉熵损失：
 * {@code L = -mean(target * log(predicted) + (1 - target) * log(1 - predicted))}。</p>
 *
 * <p>Predicted values are clamped to [{@code epsilon}, 1 - {@code epsilon}] (where
 * {@code epsilon = 1e-7}) to ensure numerical stability and avoid {@code log(0)}.</p>
 * <p>预测值被裁剪到 [{@code epsilon}, 1 - {@code epsilon}]（其中
 * {@code epsilon = 1e-7}）以确保数值稳定性并避免 {@code log(0)}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Binary classification loss - 二元分类损失</li>
 *   <li>Numerical stability via epsilon clamping - 通过epsilon裁剪保证数值稳定性</li>
 *   <li>Mean reduction to scalar - 均值归约为标量</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LossFunction bce = new BinaryCrossEntropyLoss();
 * Tensor predicted = Tensor.fromFloat(new float[]{0.9f, 0.1f, 0.8f}, Shape.of(3));
 * Tensor target = Tensor.fromFloat(new float[]{1.0f, 0.0f, 1.0f}, Shape.of(3));
 * Tensor loss = bce.compute(predicted, target);
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
 * @see CrossEntropyLoss
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class BinaryCrossEntropyLoss implements LossFunction {

    private static final float EPSILON = 1e-7f;

    /**
     * Create a new Binary Cross-Entropy loss function
     * 创建新的二元交叉熵损失函数
     */
    public BinaryCrossEntropyLoss() {
        // stateless
    }

    /**
     * Compute Binary Cross-Entropy loss
     * 计算二元交叉熵损失
     *
     * @param predicted the predicted probability tensor (values should be in [0, 1]) | 预测概率张量（值应在 [0, 1] 范围内）
     * @param target    the binary target tensor (values should be 0 or 1) | 二元目标张量（值应为 0 或 1）
     * @return a scalar tensor containing the BCE loss value | 包含BCE损失值的标量张量
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
            double p = clamp(predData[i]);
            double t = targetData[i];
            sum += -(t * Math.log(p) + (1.0 - t) * Math.log(1.0 - p));
        }

        float bce = (float) (sum / n);
        return Tensor.scalar(bce);
    }

    /**
     * Clamp value to [epsilon, 1 - epsilon] for numerical stability
     * 将值裁剪到 [epsilon, 1 - epsilon] 以保证数值稳定性
     */
    private static double clamp(float value) {
        if (value < EPSILON) return EPSILON;
        if (value > 1.0f - EPSILON) return 1.0 - EPSILON;
        return value;
    }
}
