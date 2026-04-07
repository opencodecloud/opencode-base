package cloud.opencode.base.neural.loss;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

/**
 * Categorical Cross-Entropy Loss Function
 * 分类交叉熵损失函数
 *
 * <p>Computes categorical cross-entropy loss for multi-class classification.
 * Input shape must be [N, C] where N is the batch size and C is the number of classes:
 * {@code L = -mean(sum_c(target_c * log(predicted_c)))} where the sum is over classes
 * and the mean is over the batch dimension.</p>
 * <p>计算多分类的分类交叉熵损失。输入形状必须为 [N, C]，其中 N 为批次大小，C 为类别数：
 * {@code L = -mean(sum_c(target_c * log(predicted_c)))}，其中对类别维度求和，
 * 对批次维度求均值。</p>
 *
 * <p>Predicted values are clamped to [{@code epsilon}, 1 - {@code epsilon}] (where
 * {@code epsilon = 1e-7}) to ensure numerical stability.</p>
 * <p>预测值被裁剪到 [{@code epsilon}, 1 - {@code epsilon}]
 * 以确保数值稳定性。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multi-class classification loss - 多分类损失</li>
 *   <li>Batch-aware computation (mean over batch, sum over classes) - 批次感知计算</li>
 *   <li>Numerical stability via epsilon clamping - 通过epsilon裁剪保证数值稳定性</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LossFunction ce = new CrossEntropyLoss();
 * // Batch of 2 samples, 3 classes
 * Tensor predicted = Tensor.fromFloat(new float[]{
 *     0.7f, 0.2f, 0.1f,  // sample 1
 *     0.1f, 0.8f, 0.1f   // sample 2
 * }, Shape.of(2, 3));
 * Tensor target = Tensor.fromFloat(new float[]{
 *     1.0f, 0.0f, 0.0f,  // sample 1: class 0
 *     0.0f, 1.0f, 0.0f   // sample 2: class 1
 * }, Shape.of(2, 3));
 * Tensor loss = ce.compute(predicted, target);
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
 * @see BinaryCrossEntropyLoss
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class CrossEntropyLoss implements LossFunction {

    private static final float EPSILON = 1e-7f;

    /**
     * Create a new Categorical Cross-Entropy loss function
     * 创建新的分类交叉熵损失函数
     */
    public CrossEntropyLoss() {
        // stateless
    }

    /**
     * Compute Categorical Cross-Entropy loss
     * 计算分类交叉熵损失
     *
     * @param predicted the predicted probability tensor with shape [N, C] | 形状为 [N, C] 的预测概率张量
     * @param target    the one-hot encoded target tensor with shape [N, C] | 形状为 [N, C] 的独热编码目标张量
     * @return a scalar tensor containing the cross-entropy loss value | 包含交叉熵损失值的标量张量
     * @throws NeuralException if inputs are null, shapes mismatch, or rank is not 2 |
     *                         当输入为空、形状不匹配或阶数不为2时抛出
     */
    @Override
    public Tensor compute(Tensor predicted, Tensor target) {
        LossUtil.validateInputs(predicted, target);

        if (predicted.shape().rank() != 2) {
            throw new NeuralException(
                    "CrossEntropyLoss requires 2D tensors [N, C], got rank " + predicted.shape().rank(),
                    NeuralErrorCode.INVALID_LOSS_INPUT);
        }

        int batchSize = predicted.shape().dim(0);
        int numClasses = predicted.shape().dim(1);

        float[] predData = predicted.toFloatArray();
        float[] targetData = target.toFloatArray();

        double batchSum = 0.0;
        for (int n = 0; n < batchSize; n++) {
            double sampleSum = 0.0;
            int rowOffset = n * numClasses;
            for (int c = 0; c < numClasses; c++) {
                double t = targetData[rowOffset + c];
                if (t != 0.0) {
                    double p = clamp(predData[rowOffset + c]);
                    sampleSum += t * Math.log(p);
                }
            }
            batchSum += sampleSum;
        }

        float loss = (float) (-batchSum / batchSize);
        return Tensor.scalar(loss);
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
