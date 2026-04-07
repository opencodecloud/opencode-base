package cloud.opencode.base.neural.norm;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

/**
 * L2 (Euclidean) Normalizer
 * L2（欧几里得）归一化器
 *
 * <p>Normalizes each sample (row) to unit L2 norm: x / ||x||_2.
 * This normalizer is stateless and does not require fitting.
 * For zero-norm samples, returns zeros.</p>
 * <p>将每个样本（行）归一化为单位 L2 范数：x / ||x||_2。
 * 此归一化器是无状态的，不需要拟合。
 * 对于零范数样本，返回零。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Per-sample L2 normalization — 逐样本 L2 归一化</li>
 *   <li>Stateless — no fit required — 无状态 — 无需拟合</li>
 *   <li>Handles zero-norm samples gracefully — 优雅处理零范数样本</li>
 * </ul>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless after construction).
 *       线程安全: 是（构造后无状态）。</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Normalizer
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class L2Normalizer implements Normalizer {

    /**
     * Create an L2Normalizer
     * 创建 L2 归一化器
     */
    public L2Normalizer() {
        // Stateless — no initialization needed
    }

    /**
     * No-op. L2 normalization is stateless and does not require fitting.
     * 无操作。L2 归一化是无状态的，不需要拟合。
     *
     * @param data ignored | 忽略
     */
    @Override
    public void fit(Tensor data) {
        // No-op: L2 normalization is stateless
    }

    @Override
    public Tensor normalize(Tensor data) {
        if (data == null) {
            throw new NeuralException("Data must not be null",
                    NeuralErrorCode.NORMALIZATION_FAILED);
        }
        float[] flat = data.toFloatArray();
        int rank = data.shape().rank();

        if (rank <= 1) {
            // Treat entire tensor as a single sample
            float norm = l2Norm(flat, 0, flat.length);
            float[] result = new float[flat.length];
            if (norm != 0.0f) {
                for (int i = 0; i < flat.length; i++) {
                    result[i] = flat[i] / norm;
                }
            }
            return Tensor.fromFloat(result, data.shape());
        }

        // rank >= 2: normalize each row (first dimension = samples, rest = features)
        int samples = data.shape().dim(0);
        int features = flat.length / samples;
        float[] result = new float[flat.length];

        for (int s = 0; s < samples; s++) {
            int offset = s * features;
            float norm = l2Norm(flat, offset, features);
            if (norm != 0.0f) {
                for (int f = 0; f < features; f++) {
                    result[offset + f] = flat[offset + f] / norm;
                }
            }
            // else: leave as zeros (default float[] value)
        }
        return Tensor.fromFloat(result, data.shape());
    }

    /**
     * L2 normalization is not invertible without storing per-sample norms.
     * L2 归一化在不存储逐样本范数的情况下不可逆。
     *
     * @param data ignored | 忽略
     * @return never returns | 永不返回
     * @throws UnsupportedOperationException always | 始终抛出
     */
    @Override
    public Tensor denormalize(Tensor data) {
        throw new UnsupportedOperationException(
                "L2 normalization is not invertible without storing per-sample norms");
    }

    private static float l2Norm(float[] data, int offset, int length) {
        double sumSq = 0.0;
        for (int i = 0; i < length; i++) {
            double v = data[offset + i];
            sumSq += v * v;
        }
        return (float) Math.sqrt(sumSq);
    }
}
