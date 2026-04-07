package cloud.opencode.base.neural.internal;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.NeuralException;

import java.util.Objects;

/**
 * Activation Function Numerical Computations
 * 激活函数数值计算
 *
 * <p>Provides in-place activation functions for neural network forward inference.
 * All operations are numerically stable and optimized for throughput on float arrays.</p>
 * <p>为神经网络前向推理提供原地激活函数。
 * 所有操作都具有数值稳定性，并针对 float 数组的吞吐量进行了优化。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ReLU: max(0, x) - ReLU 激活</li>
 *   <li>Sigmoid: 1/(1+exp(-x)) - Sigmoid 激活</li>
 *   <li>Tanh: hyperbolic tangent - 双曲正切</li>
 *   <li>Softmax: numerically stable (max subtraction, -Inf guard) - 数值稳定 Softmax</li>
 *   <li>HardSigmoid: clamp(x/6+0.5, 0, 1) - 硬 Sigmoid</li>
 *   <li>HardSwish: x * hardSigmoid(x) - 硬 Swish</li>
 *   <li>LeakyReLU: x &gt;= 0 ? x : alpha*x - 泄漏 ReLU</li>
 *   <li>ELU: x &gt;= 0 ? x : alpha*(exp(x)-1) - 指数线性单元</li>
 *   <li>SELU: self-normalizing exponential linear unit - 自归一化指数线性单元</li>
 *   <li>GELU: Gaussian error linear unit (tanh approximation) - 高斯误差线性单元</li>
 *   <li>Swish (SiLU): x * sigmoid(x) - Swish/SiLU 激活</li>
 *   <li>Mish: x * tanh(softplus(x)) - Mish 激活</li>
 *   <li>Softplus: ln(1+exp(x)) with overflow guard - 带溢出保护的 Softplus</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>All operations are O(n) - 所有操作均为 O(n)</li>
 *   <li>In-place mutation avoids allocation overhead - 原地修改避免分配开销</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility, no shared state) - 线程安全: 是（无状态工具类，无共享状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class Activation {

    private Activation() {}

    /**
     * Apply ReLU activation in-place: x = max(0, x).
     * 原地应用 ReLU 激活函数: x = max(0, x)。
     *
     * @param data   the data array | 数据数组
     * @param offset start offset (inclusive) | 起始偏移量（包含）
     * @param length number of elements to process | 要处理的元素数量
     * @throws NullPointerException if data is null | 如果 data 为 null
     * @throws NeuralException      if offset/length is out of bounds | 如果 offset/length 越界
     */
    public static void relu(float[] data, int offset, int length) {
        Objects.requireNonNull(data, "data must not be null");
        validateRange(data, offset, length);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            if (data[i] < 0.0f) {
                data[i] = 0.0f;
            }
        }
    }

    /**
     * Apply Sigmoid activation in-place: x = 1 / (1 + exp(-x)).
     * 原地应用 Sigmoid 激活函数: x = 1 / (1 + exp(-x))。
     *
     * @param data   the data array | 数据数组
     * @param offset start offset (inclusive) | 起始偏移量（包含）
     * @param length number of elements to process | 要处理的元素数量
     * @throws NullPointerException if data is null | 如果 data 为 null
     * @throws NeuralException      if offset/length is out of bounds | 如果 offset/length 越界
     */
    public static void sigmoid(float[] data, int offset, int length) {
        Objects.requireNonNull(data, "data must not be null");
        validateRange(data, offset, length);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            data[i] = 1.0f / (1.0f + (float) Math.exp(-data[i]));
        }
    }

    /**
     * Apply Tanh activation in-place: x = tanh(x).
     * 原地应用 Tanh 激活函数: x = tanh(x)。
     *
     * @param data   the data array | 数据数组
     * @param offset start offset (inclusive) | 起始偏移量（包含）
     * @param length number of elements to process | 要处理的元素数量
     * @throws NullPointerException if data is null | 如果 data 为 null
     * @throws NeuralException      if offset/length is out of bounds | 如果 offset/length 越界
     */
    public static void tanh(float[] data, int offset, int length) {
        Objects.requireNonNull(data, "data must not be null");
        validateRange(data, offset, length);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            data[i] = (float) Math.tanh(data[i]);
        }
    }

    /**
     * Apply Softmax activation in-place on a single row.
     * 原地对单行应用 Softmax 激活函数。
     *
     * <p>Numerically stable: subtracts max before exponentiation to avoid overflow.</p>
     * <p>数值稳定: 在指数化之前减去最大值以避免溢出。</p>
     *
     * @param data   the data array | 数据数组
     * @param offset start offset of the row (inclusive) | 行的起始偏移量（包含）
     * @param length number of elements in the row | 行中的元素数量
     * @throws NullPointerException if data is null | 如果 data 为 null
     * @throws NeuralException      if offset/length is out of bounds or length <= 0 | 如果 offset/length 越界或 length <= 0
     */
    public static void softmax(float[] data, int offset, int length) {
        Objects.requireNonNull(data, "data must not be null");
        if (length <= 0) {
            throw new NeuralException("softmax length must be > 0, got: " + length,
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
        validateRange(data, offset, length);

        int end = offset + length;

        // Find max for numerical stability
        float max = data[offset];
        for (int i = offset + 1; i < end; i++) {
            if (data[i] > max) {
                max = data[i];
            }
        }

        // exp(x - max) and sum
        // When all inputs are -Infinity (e.g. attention masking), max is -Infinity,
        // exp(-Inf - (-Inf)) = exp(NaN) = NaN. Guard by detecting -Infinity max.
        if (Float.isInfinite(max) && max < 0) {
            // All inputs are -Infinity: fall back to uniform distribution
            float uniform = 1.0f / length;
            for (int i = offset; i < end; i++) {
                data[i] = uniform;
            }
            return;
        }

        float sum = 0.0f;
        for (int i = offset; i < end; i++) {
            float e = (float) Math.exp(data[i] - max);
            data[i] = e;
            sum += e;
        }

        // Normalize — guard against NaN/zero sum (should not happen after max guard above)
        if (sum > 0.0f) {
            float invSum = 1.0f / sum;
            for (int i = offset; i < end; i++) {
                data[i] *= invSum;
            }
        } else {
            // Defensive fallback: uniform distribution
            float uniform = 1.0f / length;
            for (int i = offset; i < end; i++) {
                data[i] = uniform;
            }
        }
    }

    /**
     * Apply Softmax activation in-place on each row of a [rows x cols] matrix.
     * 原地对 [rows x cols] 矩阵的每一行应用 Softmax 激活函数。
     *
     * <p>Each row is softmaxed independently.</p>
     * <p>每一行独立进行 softmax 计算。</p>
     *
     * @param data the flattened row-major matrix | 行主序展平矩阵
     * @param rows number of rows | 行数
     * @param cols number of columns | 列数
     * @throws NullPointerException if data is null | 如果 data 为 null
     * @throws NeuralException      if dimensions are invalid | 如果维度无效
     */
    public static void softmaxBatch(float[] data, int rows, int cols) {
        Objects.requireNonNull(data, "data must not be null");
        if (rows <= 0 || cols <= 0) {
            throw new NeuralException("rows and cols must be > 0, got: rows=" + rows + ", cols=" + cols,
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
        if (data.length < (long) rows * cols) {
            throw new NeuralException(
                    "data length " + data.length + " < rows*cols " + ((long) rows * cols),
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
        for (int r = 0; r < rows; r++) {
            softmax(data, (int) ((long) r * cols), cols);
        }
    }

    /**
     * Apply LeakyReLU activation in-place: x = x >= 0 ? x : alpha * x.
     * 原地应用 LeakyReLU 激活函数: x = x >= 0 ? x : alpha * x。
     *
     * @param data   the data array | 数据数组
     * @param offset start offset (inclusive) | 起始偏移量（包含）
     * @param length number of elements to process | 要处理的元素数量
     * @param alpha  slope for negative values | 负值斜率
     * @throws NullPointerException if data is null | 如果 data 为 null
     * @throws NeuralException      if offset/length is out of bounds | 如果 offset/length 越界
     */
    public static void leakyRelu(float[] data, int offset, int length, float alpha) {
        Objects.requireNonNull(data, "data must not be null");
        validateRange(data, offset, length);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            if (data[i] < 0.0f) {
                data[i] = alpha * data[i];
            }
        }
    }

    /**
     * Apply ELU activation in-place: x = x >= 0 ? x : alpha * (exp(x) - 1).
     * 原地应用 ELU 激活函数: x = x >= 0 ? x : alpha * (exp(x) - 1)。
     *
     * @param data   the data array | 数据数组
     * @param offset start offset (inclusive) | 起始偏移量（包含）
     * @param length number of elements to process | 要处理的元素数量
     * @param alpha  scale for negative region | 负区域缩放系数
     * @throws NullPointerException if data is null | 如果 data 为 null
     * @throws NeuralException      if offset/length is out of bounds | 如果 offset/length 越界
     */
    public static void elu(float[] data, int offset, int length, float alpha) {
        Objects.requireNonNull(data, "data must not be null");
        validateRange(data, offset, length);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            if (data[i] < 0.0f) {
                data[i] = alpha * ((float) Math.exp(data[i]) - 1.0f);
            }
        }
    }

    /**
     * Apply SELU activation in-place: x = lambda * (x >= 0 ? x : alpha * (exp(x) - 1)).
     * 原地应用 SELU 激活函数: x = lambda * (x >= 0 ? x : alpha * (exp(x) - 1))。
     *
     * <p>Uses fixed constants: alpha = 1.6732632423543772, lambda = 1.0507009873554805.</p>
     * <p>使用固定常量: alpha = 1.6732632423543772, lambda = 1.0507009873554805。</p>
     *
     * @param data   the data array | 数据数组
     * @param offset start offset (inclusive) | 起始偏移量（包含）
     * @param length number of elements to process | 要处理的元素数量
     * @throws NullPointerException if data is null | 如果 data 为 null
     * @throws NeuralException      if offset/length is out of bounds | 如果 offset/length 越界
     */
    public static void selu(float[] data, int offset, int length) {
        Objects.requireNonNull(data, "data must not be null");
        validateRange(data, offset, length);
        final float alpha = 1.6732632423543772f;
        final float lambda = 1.0507009873554805f;
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            if (data[i] >= 0.0f) {
                data[i] = lambda * data[i];
            } else {
                data[i] = lambda * alpha * ((float) Math.exp(data[i]) - 1.0f);
            }
        }
    }

    /**
     * Apply GELU activation in-place: x = 0.5 * x * (1 + tanh(sqrt(2/pi) * (x + 0.044715 * x^3))).
     * 原地应用 GELU 激活函数: x = 0.5 * x * (1 + tanh(sqrt(2/pi) * (x + 0.044715 * x^3)))。
     *
     * @param data   the data array | 数据数组
     * @param offset start offset (inclusive) | 起始偏移量（包含）
     * @param length number of elements to process | 要处理的元素数量
     * @throws NullPointerException if data is null | 如果 data 为 null
     * @throws NeuralException      if offset/length is out of bounds | 如果 offset/length 越界
     */
    public static void gelu(float[] data, int offset, int length) {
        Objects.requireNonNull(data, "data must not be null");
        validateRange(data, offset, length);
        final float SQRT_2_OVER_PI = (float) Math.sqrt(2.0 / Math.PI);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            float x = data[i];
            float inner = SQRT_2_OVER_PI * (x + 0.044715f * x * x * x);
            data[i] = 0.5f * x * (1.0f + (float) Math.tanh(inner));
        }
    }

    /**
     * Apply Swish (SiLU) activation in-place: x = x * sigmoid(x).
     * 原地应用 Swish (SiLU) 激活函数: x = x * sigmoid(x)。
     *
     * @param data   the data array | 数据数组
     * @param offset start offset (inclusive) | 起始偏移量（包含）
     * @param length number of elements to process | 要处理的元素数量
     * @throws NullPointerException if data is null | 如果 data 为 null
     * @throws NeuralException      if offset/length is out of bounds | 如果 offset/length 越界
     */
    public static void swish(float[] data, int offset, int length) {
        Objects.requireNonNull(data, "data must not be null");
        validateRange(data, offset, length);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            float sig = 1.0f / (1.0f + (float) Math.exp(-data[i]));
            data[i] = data[i] * sig;
        }
    }

    /**
     * Apply Mish activation in-place: x = x * tanh(ln(1 + exp(x))).
     * 原地应用 Mish 激活函数: x = x * tanh(ln(1 + exp(x)))。
     *
     * @param data   the data array | 数据数组
     * @param offset start offset (inclusive) | 起始偏移量（包含）
     * @param length number of elements to process | 要处理的元素数量
     * @throws NullPointerException if data is null | 如果 data 为 null
     * @throws NeuralException      if offset/length is out of bounds | 如果 offset/length 越界
     */
    public static void mish(float[] data, int offset, int length) {
        Objects.requireNonNull(data, "data must not be null");
        validateRange(data, offset, length);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            float x = data[i];
            // softplus with overflow protection
            float sp = x > 20.0f ? x : (float) Math.log(1.0 + Math.exp(x));
            data[i] = x * (float) Math.tanh(sp);
        }
    }

    /**
     * Apply Softplus activation in-place: x = ln(1 + exp(x)), with overflow protection.
     * 原地应用 Softplus 激活函数: x = ln(1 + exp(x))，带溢出保护。
     *
     * @param data   the data array | 数据数组
     * @param offset start offset (inclusive) | 起始偏移量（包含）
     * @param length number of elements to process | 要处理的元素数量
     * @throws NullPointerException if data is null | 如果 data 为 null
     * @throws NeuralException      if offset/length is out of bounds | 如果 offset/length 越界
     */
    public static void softplus(float[] data, int offset, int length) {
        Objects.requireNonNull(data, "data must not be null");
        validateRange(data, offset, length);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            // For large positive x, softplus(x) ≈ x to avoid overflow
            data[i] = data[i] > 20.0f ? data[i] : (float) Math.log(1.0 + Math.exp(data[i]));
        }
    }

    /**
     * Apply HardSigmoid activation in-place: x = clamp(x/6 + 0.5, 0, 1).
     * 原地应用 HardSigmoid 激活函数: x = clamp(x/6 + 0.5, 0, 1)。
     *
     * @param data   the data array | 数据数组
     * @param offset start offset (inclusive) | 起始偏移量（包含）
     * @param length number of elements to process | 要处理的元素数量
     * @throws NullPointerException if data is null | 如果 data 为 null
     * @throws NeuralException      if offset/length is out of bounds | 如果 offset/length 越界
     */
    public static void hardSigmoid(float[] data, int offset, int length) {
        Objects.requireNonNull(data, "data must not be null");
        validateRange(data, offset, length);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            data[i] = Math.clamp(data[i] / 6.0f + 0.5f, 0.0f, 1.0f);
        }
    }

    /**
     * Apply HardSwish activation in-place: x = x * clamp(x/6 + 0.5, 0, 1).
     * 原地应用 HardSwish 激活函数: x = x * clamp(x/6 + 0.5, 0, 1)。
     *
     * @param data   the data array | 数据数组
     * @param offset start offset (inclusive) | 起始偏移量（包含）
     * @param length number of elements to process | 要处理的元素数量
     * @throws NullPointerException if data is null | 如果 data 为 null
     * @throws NeuralException      if offset/length is out of bounds | 如果 offset/length 越界
     */
    public static void hardSwish(float[] data, int offset, int length) {
        Objects.requireNonNull(data, "data must not be null");
        validateRange(data, offset, length);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            float hs = Math.clamp(data[i] / 6.0f + 0.5f, 0.0f, 1.0f);
            data[i] = data[i] * hs;
        }
    }

    /**
     * Validate that [offset, offset+length) is within the data array bounds.
     * 验证 [offset, offset+length) 是否在数据数组范围内。
     */
    private static void validateRange(float[] data, int offset, int length) {
        if (offset < 0 || length < 0 || length > data.length - offset) {
            throw new NeuralException(
                    "Invalid range: offset=" + offset + ", length=" + length
                            + ", array length=" + data.length,
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
    }
}
