package cloud.opencode.base.neural.metric;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

/**
 * Classification Accuracy Metric
 * 分类准确率指标
 *
 * <p>Computes the fraction of correct predictions. Supports both direct class label
 * predictions (1D tensor of shape [N]) and probability distribution predictions
 * (2D tensor of shape [N, C]), where C is the number of classes. For 2D predictions,
 * argmax is applied along the class dimension to obtain predicted labels.</p>
 * <p>计算正确预测的比例。支持直接类别标签预测（形状为 [N] 的一维张量）和概率分布预测
 * （形状为 [N, C] 的二维张量，其中 C 为类别数）。对于二维预测，沿类别维度取 argmax 获取预测标签。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Supports [N] and [N, C] predicted tensor shapes | 支持 [N] 和 [N, C] 预测张量形状</li>
 *   <li>Automatic argmax for probability distributions | 概率分布自动取 argmax</li>
 *   <li>Returns scalar tensor with accuracy value in [0, 1] | 返回 [0, 1] 范围的准确率标量张量</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Metric
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class Accuracy implements Metric {

    /**
     * Compute classification accuracy
     * 计算分类准确率
     *
     * @param predicted predicted tensor of shape [N] or [N, C] | 形状为 [N] 或 [N, C] 的预测张量
     * @param target    target tensor of shape [N] with integer class labels | 形状为 [N] 的目标张量（整数类别标签）
     * @return scalar tensor containing accuracy in [0, 1] | 包含 [0, 1] 范围准确率的标量张量
     * @throws NeuralException if inputs are null, empty, or have incompatible shapes |
     *                         如果输入为空、为零长度或形状不兼容
     */
    @Override
    public Tensor compute(Tensor predicted, Tensor target) {
        MetricValidation.requireNonNull(predicted, "predicted");
        MetricValidation.requireNonNull(target, "target");

        int rank = predicted.shape().rank();
        if (rank < 1 || rank > 2) {
            throw new NeuralException(
                    "Predicted tensor must be 1D [N] or 2D [N, C], got rank " + rank,
                    NeuralErrorCode.INVALID_METRIC_INPUT);
        }
        MetricValidation.requireRank(target, 1, "target");

        int n = predicted.shape().dim(0);
        MetricValidation.requireSameSize(n, target.shape().dim(0), "predicted dim(0)", "target dim(0)");
        MetricValidation.requirePositiveSize(n);

        float[] predLabels;
        if (rank == 2) {
            predLabels = argmaxPerRow(predicted);
        } else {
            predLabels = predicted.toFloatArray();
        }

        float[] targetLabels = target.toFloatArray();
        int correct = 0;
        for (int i = 0; i < n; i++) {
            if (Math.round(predLabels[i]) == Math.round(targetLabels[i])) {
                correct++;
            }
        }

        float accuracy = (float) correct / n;
        return Tensor.scalar(accuracy);
    }

    /**
     * Compute argmax per row for a 2D tensor [N, C]
     * 对二维张量 [N, C] 的每行计算 argmax
     */
    private static float[] argmaxPerRow(Tensor tensor) {
        int n = tensor.shape().dim(0);
        int c = tensor.shape().dim(1);
        float[] result = new float[n];
        float[] data = tensor.toFloatArray();

        for (int i = 0; i < n; i++) {
            int offset = i * c;
            float maxVal = data[offset];
            int maxIdx = 0;
            for (int j = 1; j < c; j++) {
                if (data[offset + j] > maxVal) {
                    maxVal = data[offset + j];
                    maxIdx = j;
                }
            }
            result[i] = maxIdx;
        }
        return result;
    }
}
