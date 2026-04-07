package cloud.opencode.base.neural.norm;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

/**
 * Min-Max Normalizer
 * 最小-最大归一化器
 *
 * <p>Scales each feature independently to a target range [targetMin, targetMax] (default [0, 1]).
 * The transformation is: (x - dataMin) / (dataMax - dataMin) * (targetMax - targetMin) + targetMin.
 * For constant features (dataMin == dataMax), the output is targetMin.</p>
 * <p>将每个特征独立缩放到目标范围 [targetMin, targetMax]（默认 [0, 1]）。
 * 变换公式为：(x - dataMin) / (dataMax - dataMin) * (targetMax - targetMin) + targetMin。
 * 对于常量特征（dataMin == dataMax），输出为 targetMin。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Per-feature min-max scaling — 逐特征最小-最大缩放</li>
 *   <li>Configurable target range — 可配置的目标范围</li>
 *   <li>Handles constant features gracefully — 优雅处理常量特征</li>
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
public final class MinMaxNormalizer implements Normalizer {

    private final float targetMin;
    private final float targetMax;

    private float[] dataMin;
    private float[] dataMax;
    private boolean fitted;

    /**
     * Create a MinMaxNormalizer with default target range [0, 1]
     * 创建默认目标范围 [0, 1] 的最小-最大归一化器
     */
    public MinMaxNormalizer() {
        this(0.0f, 1.0f);
    }

    /**
     * Create a MinMaxNormalizer with custom target range
     * 创建自定义目标范围的最小-最大归一化器
     *
     * @param targetMin target minimum value | 目标最小值
     * @param targetMax target maximum value | 目标最大值
     * @throws NeuralException if targetMin &gt;= targetMax | 如果 targetMin &gt;= targetMax
     */
    public MinMaxNormalizer(float targetMin, float targetMax) {
        if (targetMin >= targetMax) {
            throw new NeuralException(
                    "targetMin (" + targetMin + ") must be less than targetMax (" + targetMax + ")",
                    NeuralErrorCode.NORMALIZATION_FAILED);
        }
        this.targetMin = targetMin;
        this.targetMax = targetMax;
        this.fitted = false;
    }

    @Override
    public void fit(Tensor data) {
        validateData(data);
        float[] flat = data.toFloatArray();
        int features = featureCount(data.shape());
        int samples = flat.length / features;

        dataMin = new float[features];
        dataMax = new float[features];

        // Initialize with first sample
        System.arraycopy(flat, 0, dataMin, 0, features);
        System.arraycopy(flat, 0, dataMax, 0, features);

        // Scan remaining samples
        for (int s = 1; s < samples; s++) {
            int offset = s * features;
            for (int f = 0; f < features; f++) {
                float v = flat[offset + f];
                if (v < dataMin[f]) dataMin[f] = v;
                if (v > dataMax[f]) dataMax[f] = v;
            }
        }
        fitted = true;
    }

    @Override
    public Tensor normalize(Tensor data) {
        ensureFitted();
        validateData(data);
        float[] flat = data.toFloatArray();
        int features = featureCount(data.shape());
        float targetRange = targetMax - targetMin;

        float[] result = new float[flat.length];
        for (int i = 0; i < flat.length; i++) {
            int f = i % features;
            float range = dataMax[f] - dataMin[f];
            if (range == 0.0f) {
                result[i] = targetMin;
            } else {
                result[i] = (flat[i] - dataMin[f]) / range * targetRange + targetMin;
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
        float targetRange = targetMax - targetMin;

        float[] result = new float[flat.length];
        for (int i = 0; i < flat.length; i++) {
            int f = i % features;
            float range = dataMax[f] - dataMin[f];
            if (range == 0.0f) {
                result[i] = dataMin[f];
            } else {
                result[i] = (flat[i] - targetMin) / targetRange * range + dataMin[f];
            }
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
