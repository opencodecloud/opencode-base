package cloud.opencode.base.neural.metric;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

/**
 * Binary Classification Accuracy Metric
 * 二分类准确率指标
 *
 * <p>Computes binary classification accuracy with a configurable decision threshold.
 * Predicted probabilities above the threshold are classified as positive (1),
 * otherwise as negative (0). The result is the fraction of correct predictions.</p>
 * <p>使用可配置的决策阈值计算二分类准确率。预测概率超过阈值的被分类为正例（1），
 * 否则为负例（0）。结果为正确预测的比例。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable threshold (default 0.5) | 可配置阈值（默认 0.5）</li>
 *   <li>Supports probability inputs in [0, 1] range | 支持 [0, 1] 范围的概率输入</li>
 *   <li>Returns scalar tensor with accuracy in [0, 1] | 返回 [0, 1] 范围准确率的标量张量</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (effectively immutable) - 线程安全: 是（有效不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Metric
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class BinaryAccuracy implements Metric {

    private final float threshold;

    /**
     * Create a BinaryAccuracy metric with default threshold of 0.5
     * 创建默认阈值为 0.5 的二分类准确率指标
     */
    public BinaryAccuracy() {
        this(0.5f);
    }

    /**
     * Create a BinaryAccuracy metric with specified threshold
     * 创建指定阈值的二分类准确率指标
     *
     * @param threshold decision threshold in (0, 1) | (0, 1) 范围内的决策阈值
     * @throws NeuralException if threshold is not in (0, 1) | 如果阈值不在 (0, 1) 范围内
     */
    public BinaryAccuracy(float threshold) {
        if (Float.isNaN(threshold) || threshold <= 0.0f || threshold >= 1.0f) {
            throw new NeuralException(
                    "Threshold must be in (0, 1), got " + threshold,
                    NeuralErrorCode.INVALID_METRIC_INPUT);
        }
        this.threshold = threshold;
    }

    /**
     * Get the decision threshold
     * 获取决策阈值
     *
     * @return the threshold | 阈值
     */
    public float threshold() {
        return threshold;
    }

    /**
     * Compute binary classification accuracy
     * 计算二分类准确率
     *
     * @param predicted predicted tensor of shape [N] with probabilities | 形状为 [N] 的概率预测张量
     * @param target    target tensor of shape [N] with binary labels (0 or 1) | 形状为 [N] 的二元标签目标张量
     * @return scalar tensor containing accuracy in [0, 1] | 包含 [0, 1] 范围准确率的标量张量
     * @throws NeuralException if inputs are null, empty, or have incompatible shapes |
     *                         如果输入为空、为零长度或形状不兼容
     */
    @Override
    public Tensor compute(Tensor predicted, Tensor target) {
        MetricValidation.requireNonNull(predicted, "predicted");
        MetricValidation.requireNonNull(target, "target");
        MetricValidation.requireRank(predicted, 1, "predicted");
        MetricValidation.requireRank(target, 1, "target");
        MetricValidation.requireSameShape(predicted, target);

        int n = predicted.shape().dim(0);
        MetricValidation.requirePositiveSize(n);

        float[] pred = predicted.toFloatArray();
        float[] tgt = target.toFloatArray();

        int correct = 0;
        for (int i = 0; i < n; i++) {
            int predLabel = pred[i] >= threshold ? 1 : 0;
            int targetLabel = Math.round(tgt[i]);
            if (predLabel == targetLabel) {
                correct++;
            }
        }

        float accuracy = (float) correct / n;
        return Tensor.scalar(accuracy);
    }
}
