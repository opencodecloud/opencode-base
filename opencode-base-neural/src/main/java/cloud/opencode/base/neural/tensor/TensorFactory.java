package cloud.opencode.base.neural.tensor;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.TensorException;

import java.util.Arrays;

/**
 * Tensor Factory — Special Creation Methods
 * 张量工厂 — 特殊创建方法
 *
 * <p>Utility class providing factory methods for creating tensors with specific
 * patterns such as arithmetic ranges, constant fills, and identity matrices.</p>
 * <p>提供工厂方法的工具类，用于创建具有特定模式的张量，
 * 如等差序列、常量填充和单位矩阵。</p>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Tensor
 * @see Shape
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class TensorFactory {

    private TensorFactory() {
        // Utility class, no instantiation
    }

    /**
     * Create a 1D tensor with values [0, 1, 2, ..., n-1]
     * 创建值为 [0, 1, 2, ..., n-1] 的一维张量
     *
     * @param n number of elements (must be &gt; 0) | 元素数量（必须 &gt; 0）
     * @return 1D tensor with ascending integers | 含递增整数的一维张量
     * @throws TensorException if n is not positive | 如果 n 非正
     */
    public static Tensor arange(int n) {
        if (n <= 0) {
            throw new TensorException(
                    "arange requires n > 0, got " + n, NeuralErrorCode.INVALID_PARAMETERS);
        }
        float[] data = new float[n];
        for (int i = 0; i < n; i++) {
            data[i] = i;
        }
        return Tensor.fromFloat(data, Shape.of(n));
    }

    /**
     * Create a tensor filled with a constant value
     * 创建用常量值填充的张量
     *
     * @param shape the tensor shape | 张量形状
     * @param value the fill value | 填充值
     * @return constant-filled tensor | 常量填充张量
     */
    public static Tensor fill(Shape shape, float value) {
        float[] data = new float[shape.size()];
        Arrays.fill(data, value);
        return Tensor.fromFloat(data, shape);
    }

    /**
     * Create an n x n identity matrix
     * 创建 n x n 单位矩阵
     *
     * @param n matrix size (must be &gt; 0) | 矩阵大小（必须 &gt; 0）
     * @return identity matrix tensor | 单位矩阵张量
     * @throws TensorException if n is not positive | 如果 n 非正
     */
    public static Tensor eye(int n) {
        if (n <= 0) {
            throw new TensorException(
                    "eye requires n > 0, got " + n, NeuralErrorCode.INVALID_PARAMETERS);
        }
        float[] data = new float[n * n];
        for (int i = 0; i < n; i++) {
            data[i * n + i] = 1.0f;
        }
        return Tensor.fromFloat(data, Shape.of(n, n));
    }
}
