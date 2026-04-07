package cloud.opencode.base.neural.norm;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

/**
 * Z-Score (Standard Score) Normalizer
 * Z分数（标准分数）归一化器
 *
 * <p>Standardizes each feature to have mean=0 and standard deviation=1.
 * The transformation is: (x - mean) / std.
 * For constant features (std == 0), the output is 0.</p>
 * <p>将每个特征标准化为均值=0、标准差=1。
 * 变换公式为：(x - mean) / std。
 * 对于常量特征（std == 0），输出为 0。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Per-feature mean and standard deviation computation — 逐特征均值和标准差计算</li>
 *   <li>Handles constant features (zero std) gracefully — 优雅处理常量特征（零标准差）</li>
 *   <li>Invertible (denormalize supported) — 可逆（支持反归一化）</li>
 * </ul>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: No. This class has mutable state from {@link #fit(Tensor)}.
 *       External synchronization is required for concurrent access.
 *       线程安全: 否。此类因 {@link #fit(Tensor)} 具有可变状态。
 *       并发访问需要外部同步。</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Normalizer
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class ZScoreNormalizer implements Normalizer {

    private float[] mean;
    private float[] std;
    private boolean fitted;

    /**
     * Create a ZScoreNormalizer
     * 创建 Z分数归一化器
     */
    public ZScoreNormalizer() {
        this.fitted = false;
    }

    @Override
    public void fit(Tensor data) {
        validateData(data);
        float[] flat = data.toFloatArray();
        int features = featureCount(data.shape());
        int samples = flat.length / features;

        mean = new float[features];
        std = new float[features];

        // Single-pass: compute sum and sum-of-squares simultaneously
        // variance = E[x²] - (E[x])², using double accumulators to avoid cancellation
        double[] sumD = new double[features];
        double[] sumSqD = new double[features];
        for (int s = 0; s < samples; s++) {
            int offset = s * features;
            for (int f = 0; f < features; f++) {
                double v = flat[offset + f];
                sumD[f] += v;
                sumSqD[f] += v * v;
            }
        }
        for (int f = 0; f < features; f++) {
            double meanD = sumD[f] / samples;
            mean[f] = (float) meanD;
            // variance = E[x²] - E[x]², clamp to 0 for numerical safety
            double variance = Math.max(0.0, sumSqD[f] / samples - meanD * meanD);
            std[f] = (float) Math.sqrt(variance);
        }

        fitted = true;
    }

    @Override
    public Tensor normalize(Tensor data) {
        ensureFitted();
        validateData(data);
        float[] flat = data.toFloatArray();
        int features = featureCount(data.shape());

        float[] result = new float[flat.length];
        for (int i = 0; i < flat.length; i++) {
            int f = i % features;
            if (std[f] == 0.0f) {
                result[i] = 0.0f;
            } else {
                result[i] = (flat[i] - mean[f]) / std[f];
            }
        }
        return Tensor.fromFloat(result, data.shape());
    }

    @Override
    public Tensor denormalize(Tensor data) {
        ensureFitted();
        validateData(data);
        float[] flat = data.toFloatArray();
        int features = featureCount(data.shape());

        float[] result = new float[flat.length];
        for (int i = 0; i < flat.length; i++) {
            int f = i % features;
            result[i] = flat[i] * std[f] + mean[f];
        }
        return Tensor.fromFloat(result, data.shape());
    }

    private void ensureFitted() {
        if (!fitted) {
            throw new NeuralException(
                    "Normalizer has not been fitted. Call fit() first.",
                    NeuralErrorCode.NORMALIZER_NOT_FITTED);
        }
    }

    private static void validateData(Tensor data) {
        if (data == null) {
            throw new NeuralException("Data must not be null",
                    NeuralErrorCode.NORMALIZATION_FAILED);
        }
    }

    /**
     * Determine the number of features.
     * For rank >= 2: last dimension (each row = one sample).
     * For rank <= 1: 1 (each element = one sample with one feature).
     */
    private static int featureCount(Shape shape) {
        int rank = shape.rank();
        if (rank <= 1) {
            return 1;
        }
        return shape.dim(rank - 1);
    }
}
