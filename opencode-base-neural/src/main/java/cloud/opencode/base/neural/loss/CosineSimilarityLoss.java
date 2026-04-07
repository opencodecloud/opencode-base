package cloud.opencode.base.neural.loss;

import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

/**
 * Cosine Similarity Loss Function
 * 余弦相似度损失函数
 *
 * <p>Computes the cosine similarity loss between predicted and target tensors:
 * {@code L = 1 - cos_sim(predicted, target)} where
 * {@code cos_sim = dot(a, b) / (||a|| * ||b||)}.</p>
 * <p>计算预测张量和目标张量之间的余弦相似度损失：
 * {@code L = 1 - cos_sim(predicted, target)}，其中
 * {@code cos_sim = dot(a, b) / (||a|| * ||b||)}。</p>
 *
 * <p>When either vector has zero norm, the loss is 1.0 (maximum dissimilarity).
 * The loss ranges from 0 (identical direction) to 2 (opposite direction).</p>
 * <p>当任一向量的范数为零时，损失为1.0（最大不相似度）。
 * 损失范围从0（方向一致）到2（方向相反）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Direction-based similarity loss - 基于方向的相似度损失</li>
 *   <li>Handles zero-norm vectors gracefully - 优雅地处理零范数向量</li>
 *   <li>Scale-invariant comparison - 尺度不变的比较</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LossFunction cosine = new CosineSimilarityLoss();
 * Tensor predicted = Tensor.fromFloat(new float[]{1.0f, 0.0f}, Shape.of(2));
 * Tensor target = Tensor.fromFloat(new float[]{0.0f, 1.0f}, Shape.of(2));
 * Tensor loss = cosine.compute(predicted, target);
 * // loss = 1.0 (orthogonal vectors)
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
public final class CosineSimilarityLoss implements LossFunction {

    /**
     * Create a new Cosine Similarity loss function
     * 创建新的余弦相似度损失函数
     */
    public CosineSimilarityLoss() {
        // stateless
    }

    /**
     * Compute Cosine Similarity loss
     * 计算余弦相似度损失
     *
     * @param predicted the predicted output tensor | 预测输出张量
     * @param target    the ground truth target tensor | 真实目标张量
     * @return a scalar tensor containing the cosine similarity loss value (range [0, 2]) |
     *         包含余弦相似度损失值的标量张量（范围 [0, 2]）
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

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < n; i++) {
            double a = predData[i];
            double b = targetData[i];
            dot += a * b;
            normA += a * a;
            normB += b * b;
        }

        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);

        // Handle zero-norm vectors: return loss of 1.0
        float loss;
        if (normA == 0.0 || normB == 0.0) {
            loss = 1.0f;
        } else {
            double cosineSimilarity = dot / (normA * normB);
            // Clamp to [-1, 1] to handle floating-point rounding
            cosineSimilarity = Math.max(-1.0, Math.min(1.0, cosineSimilarity));
            loss = (float) (1.0 - cosineSimilarity);
        }

        return Tensor.scalar(loss);
    }
}
