package cloud.opencode.base.neural.metric;

import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

/**
 * Binary Precision Metric
 * 二分类精确率指标
 *
 * <p>Computes binary precision: TP / (TP + FP), where TP is the number of true positives
 * and FP is the number of false positives. Returns 0 when there are no positive
 * predictions (TP + FP = 0).</p>
 * <p>计算二分类精确率：TP / (TP + FP)，其中 TP 为真正例数，FP 为假正例数。
 * 当没有正例预测（TP + FP = 0）时返回 0。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Binary precision computation | 二分类精确率计算</li>
 *   <li>Safe handling of zero denominator | 安全处理零分母</li>
 *   <li>Returns scalar tensor with precision in [0, 1] | 返回 [0, 1] 范围精确率的标量张量</li>
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
 * @see Recall
 * @see F1Score
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class Precision implements Metric {

    /**
     * Compute binary precision
     * 计算二分类精确率
     *
     * @param predicted predicted tensor of shape [N] with binary labels (0 or 1) | 形状为 [N] 的二元标签预测张量
     * @param target    target tensor of shape [N] with binary labels (0 or 1) | 形状为 [N] 的二元标签目标张量
     * @return scalar tensor containing precision in [0, 1] | 包含 [0, 1] 范围精确率的标量张量
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

        float precision = computePrecision(predicted.toFloatArray(), target.toFloatArray(), n);
        return Tensor.scalar(precision);
    }

    /**
     * Compute precision from raw arrays
     * 从原始数组计算精确率
     */
    static float computePrecision(float[] pred, float[] tgt, int n) {
        int tp = 0;
        int fp = 0;
        for (int i = 0; i < n; i++) {
            int p = Math.round(pred[i]);
            int t = Math.round(tgt[i]);
            if (p == 1) {
                if (t == 1) {
                    tp++;
                } else {
                    fp++;
                }
            }
        }
        int denominator = tp + fp;
        if (denominator == 0) {
            return 0.0f;
        }
        return (float) tp / denominator;
    }
}
