package cloud.opencode.base.neural.metric;

import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

/**
 * Binary Recall Metric
 * 二分类召回率指标
 *
 * <p>Computes binary recall (sensitivity): TP / (TP + FN), where TP is the number of
 * true positives and FN is the number of false negatives. Returns 0 when there are
 * no actual positives (TP + FN = 0).</p>
 * <p>计算二分类召回率（灵敏度）：TP / (TP + FN)，其中 TP 为真正例数，FN 为假负例数。
 * 当没有实际正例（TP + FN = 0）时返回 0。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Binary recall computation | 二分类召回率计算</li>
 *   <li>Safe handling of zero denominator | 安全处理零分母</li>
 *   <li>Returns scalar tensor with recall in [0, 1] | 返回 [0, 1] 范围召回率的标量张量</li>
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
 * @see F1Score
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class Recall implements Metric {

    /**
     * Compute binary recall
     * 计算二分类召回率
     *
     * @param predicted predicted tensor of shape [N] with binary labels (0 or 1) | 形状为 [N] 的二元标签预测张量
     * @param target    target tensor of shape [N] with binary labels (0 or 1) | 形状为 [N] 的二元标签目标张量
     * @return scalar tensor containing recall in [0, 1] | 包含 [0, 1] 范围召回率的标量张量
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

        float recall = computeRecall(predicted.toFloatArray(), target.toFloatArray(), n);
        return Tensor.scalar(recall);
    }

    /**
     * Compute recall from raw arrays
     * 从原始数组计算召回率
     */
    static float computeRecall(float[] pred, float[] tgt, int n) {
        int tp = 0;
        int fn = 0;
        for (int i = 0; i < n; i++) {
            int p = Math.round(pred[i]);
            int t = Math.round(tgt[i]);
            if (t == 1) {
                if (p == 1) {
                    tp++;
                } else {
                    fn++;
                }
            }
        }
        int denominator = tp + fn;
        if (denominator == 0) {
            return 0.0f;
        }
        return (float) tp / denominator;
    }
}
