package cloud.opencode.base.neural.metric;

import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

/**
 * F1 Score Metric
 * F1 分数指标
 *
 * <p>Computes the F1 score, the harmonic mean of precision and recall:
 * F1 = 2 * precision * recall / (precision + recall).
 * Returns 0 when both precision and recall are 0.</p>
 * <p>计算 F1 分数，即精确率和召回率的调和平均值：
 * F1 = 2 * precision * recall / (precision + recall)。
 * 当精确率和召回率均为 0 时返回 0。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Harmonic mean of precision and recall | 精确率和召回率的调和平均值</li>
 *   <li>Delegates to {@link Precision} and {@link Recall} computations | 委托给精确率和召回率计算</li>
 *   <li>Safe handling of zero denominator | 安全处理零分母</li>
 *   <li>Returns scalar tensor with F1 in [0, 1] | 返回 [0, 1] 范围 F1 分数的标量张量</li>
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
 * @see Precision
 * @see Recall
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class F1Score implements Metric {

    /**
     * Compute F1 score
     * 计算 F1 分数
     *
     * @param predicted predicted tensor of shape [N] with binary labels (0 or 1) | 形状为 [N] 的二元标签预测张量
     * @param target    target tensor of shape [N] with binary labels (0 or 1) | 形状为 [N] 的二元标签目标张量
     * @return scalar tensor containing F1 score in [0, 1] | 包含 [0, 1] 范围 F1 分数的标量张量
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

        float precision = Precision.computePrecision(pred, tgt, n);
        float recall = Recall.computeRecall(pred, tgt, n);

        float denominator = precision + recall;
        float f1 = (denominator == 0.0f) ? 0.0f : 2.0f * precision * recall / denominator;

        return Tensor.scalar(f1);
    }
}
