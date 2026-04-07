package cloud.opencode.base.neural.init;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.Arrays;
import java.util.Random;

/**
 * Weight Initialization Utility
 * 权重初始化工具类
 *
 * <p>Provides static factory methods for creating tensors initialized with various
 * distributions commonly used in neural network weight initialization. All methods
 * return a new {@link Tensor} with the specified shape.</p>
 * <p>提供用于创建以各种神经网络权重初始化常用分布初始化的张量的静态工厂方法。
 * 所有方法返回具有指定形状的新 {@link Tensor}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Xavier/Glorot initialization (uniform and normal) — Xavier/Glorot 初始化（均匀和正态）</li>
 *   <li>He/Kaiming initialization (uniform and normal) — He/Kaiming 初始化（均匀和正态）</li>
 *   <li>LeCun initialization (uniform and normal) — LeCun 初始化（均匀和正态）</li>
 *   <li>Basic initializations: uniform, normal, zeros, ones, constant —
 *       基础初始化：均匀、正态、全零、全一、常量</li>
 * </ul>
 *
 * <p><strong>Fan Calculation | Fan 计算:</strong></p>
 * <ul>
 *   <li>rank &gt;= 2: fanIn = dim(1) * receptiveFieldSize, fanOut = dim(0) * receptiveFieldSize,
 *       where receptiveFieldSize = product of dims[2:]</li>
 *   <li>rank == 1: fanIn = fanOut = dim(0)</li>
 *   <li>rank == 0: fanIn = fanOut = 1</li>
 * </ul>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class, thread safety depends on the provided
 *       {@link Random} instance). 线程安全: 是（无状态工具类，线程安全性取决于提供的
 *       {@link Random} 实例）。</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Tensor
 * @see Shape
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class WeightInit {

    private WeightInit() {
        throw new AssertionError("No instances");
    }

    // ==================== Xavier/Glorot Initialization | Xavier/Glorot 初始化 ====================

    /**
     * Xavier/Glorot uniform initialization
     * Xavier/Glorot 均匀初始化
     *
     * <p>Samples from U(-limit, limit) where limit = sqrt(6 / (fanIn + fanOut)).</p>
     * <p>从 U(-limit, limit) 采样，其中 limit = sqrt(6 / (fanIn + fanOut))。</p>
     *
     * @param shape tensor shape | 张量形状
     * @param rng   random number generator | 随机数生成器
     * @return initialized tensor | 初始化后的张量
     * @throws NeuralException if shape or rng is null | 如果 shape 或 rng 为 null
     */
    public static Tensor xavierUniform(Shape shape, Random rng) {
        validateInputs(shape, rng);
        int[] fan = computeFan(shape);
        float limit = (float) Math.sqrt(6.0 / (fan[0] + fan[1]));
        return fillUniform(shape, -limit, limit, rng);
    }

    /**
     * Xavier/Glorot normal initialization
     * Xavier/Glorot 正态初始化
     *
     * <p>Samples from N(0, std) where std = sqrt(2 / (fanIn + fanOut)).</p>
     * <p>从 N(0, std) 采样，其中 std = sqrt(2 / (fanIn + fanOut))。</p>
     *
     * @param shape tensor shape | 张量形状
     * @param rng   random number generator | 随机数生成器
     * @return initialized tensor | 初始化后的张量
     * @throws NeuralException if shape or rng is null | 如果 shape 或 rng 为 null
     */
    public static Tensor xavierNormal(Shape shape, Random rng) {
        validateInputs(shape, rng);
        int[] fan = computeFan(shape);
        float std = (float) Math.sqrt(2.0 / (fan[0] + fan[1]));
        return fillNormal(shape, 0.0f, std, rng);
    }

    // ==================== He/Kaiming Initialization | He/Kaiming 初始化 ====================

    /**
     * He/Kaiming uniform initialization
     * He/Kaiming 均匀初始化
     *
     * <p>Samples from U(-limit, limit) where limit = sqrt(6 / fanIn).</p>
     * <p>从 U(-limit, limit) 采样，其中 limit = sqrt(6 / fanIn)。</p>
     *
     * @param shape tensor shape | 张量形状
     * @param rng   random number generator | 随机数生成器
     * @return initialized tensor | 初始化后的张量
     * @throws NeuralException if shape or rng is null | 如果 shape 或 rng 为 null
     */
    public static Tensor heUniform(Shape shape, Random rng) {
        validateInputs(shape, rng);
        int fanIn = computeFan(shape)[0];
        float limit = (float) Math.sqrt(6.0 / fanIn);
        return fillUniform(shape, -limit, limit, rng);
    }

    /**
     * He/Kaiming normal initialization
     * He/Kaiming 正态初始化
     *
     * <p>Samples from N(0, std) where std = sqrt(2 / fanIn).</p>
     * <p>从 N(0, std) 采样，其中 std = sqrt(2 / fanIn)。</p>
     *
     * @param shape tensor shape | 张量形状
     * @param rng   random number generator | 随机数生成器
     * @return initialized tensor | 初始化后的张量
     * @throws NeuralException if shape or rng is null | 如果 shape 或 rng 为 null
     */
    public static Tensor heNormal(Shape shape, Random rng) {
        validateInputs(shape, rng);
        int fanIn = computeFan(shape)[0];
        float std = (float) Math.sqrt(2.0 / fanIn);
        return fillNormal(shape, 0.0f, std, rng);
    }

    // ==================== LeCun Initialization | LeCun 初始化 ====================

    /**
     * LeCun uniform initialization
     * LeCun 均匀初始化
     *
     * <p>Samples from U(-limit, limit) where limit = sqrt(3 / fanIn).</p>
     * <p>从 U(-limit, limit) 采样，其中 limit = sqrt(3 / fanIn)。</p>
     *
     * @param shape tensor shape | 张量形状
     * @param rng   random number generator | 随机数生成器
     * @return initialized tensor | 初始化后的张量
     * @throws NeuralException if shape or rng is null | 如果 shape 或 rng 为 null
     */
    public static Tensor lecunUniform(Shape shape, Random rng) {
        validateInputs(shape, rng);
        int fanIn = computeFan(shape)[0];
        float limit = (float) Math.sqrt(3.0 / fanIn);
        return fillUniform(shape, -limit, limit, rng);
    }

    /**
     * LeCun normal initialization
     * LeCun 正态初始化
     *
     * <p>Samples from N(0, std) where std = sqrt(1 / fanIn).</p>
     * <p>从 N(0, std) 采样，其中 std = sqrt(1 / fanIn)。</p>
     *
     * @param shape tensor shape | 张量形状
     * @param rng   random number generator | 随机数生成器
     * @return initialized tensor | 初始化后的张量
     * @throws NeuralException if shape or rng is null | 如果 shape 或 rng 为 null
     */
    public static Tensor lecunNormal(Shape shape, Random rng) {
        validateInputs(shape, rng);
        int fanIn = computeFan(shape)[0];
        float std = (float) Math.sqrt(1.0 / fanIn);
        return fillNormal(shape, 0.0f, std, rng);
    }

    // ==================== Basic Initializations | 基础初始化 ====================

    /**
     * Uniform distribution initialization
     * 均匀分布初始化
     *
     * <p>Samples from U(low, high).</p>
     * <p>从 U(low, high) 采样。</p>
     *
     * @param shape tensor shape | 张量形状
     * @param low   lower bound (inclusive) | 下界（含）
     * @param high  upper bound (exclusive) | 上界（不含）
     * @param rng   random number generator | 随机数生成器
     * @return initialized tensor | 初始化后的张量
     * @throws NeuralException if shape or rng is null, or low &gt;= high |
     *                         如果 shape 或 rng 为 null，或 low &gt;= high
     */
    public static Tensor uniform(Shape shape, float low, float high, Random rng) {
        validateInputs(shape, rng);
        if (low >= high) {
            throw new NeuralException(
                    "low (" + low + ") must be less than high (" + high + ")",
                    NeuralErrorCode.INVALID_INIT_PARAMS);
        }
        return fillUniform(shape, low, high, rng);
    }

    /**
     * Normal distribution initialization
     * 正态分布初始化
     *
     * <p>Samples from N(mean, std).</p>
     * <p>从 N(mean, std) 采样。</p>
     *
     * @param shape tensor shape | 张量形状
     * @param mean  distribution mean | 分布均值
     * @param std   distribution standard deviation (must be positive) | 分布标准差（必须为正）
     * @param rng   random number generator | 随机数生成器
     * @return initialized tensor | 初始化后的张量
     * @throws NeuralException if shape or rng is null, or std &lt;= 0 |
     *                         如果 shape 或 rng 为 null，或 std &lt;= 0
     */
    public static Tensor normal(Shape shape, float mean, float std, Random rng) {
        validateInputs(shape, rng);
        if (std <= 0) {
            throw new NeuralException(
                    "std (" + std + ") must be positive",
                    NeuralErrorCode.INVALID_INIT_PARAMS);
        }
        return fillNormal(shape, mean, std, rng);
    }

    /**
     * Zero initialization
     * 全零初始化
     *
     * @param shape tensor shape | 张量形状
     * @return zero-filled tensor | 全零张量
     * @throws NeuralException if shape is null | 如果 shape 为 null
     */
    public static Tensor zeros(Shape shape) {
        if (shape == null) {
            throw new NeuralException("Shape must not be null",
                    NeuralErrorCode.INVALID_INIT_PARAMS);
        }
        return Tensor.zeros(shape);
    }

    /**
     * One initialization
     * 全一初始化
     *
     * @param shape tensor shape | 张量形状
     * @return one-filled tensor | 全一张量
     * @throws NeuralException if shape is null | 如果 shape 为 null
     */
    public static Tensor ones(Shape shape) {
        if (shape == null) {
            throw new NeuralException("Shape must not be null",
                    NeuralErrorCode.INVALID_INIT_PARAMS);
        }
        return Tensor.ones(shape);
    }

    /**
     * Constant initialization
     * 常量初始化
     *
     * @param shape tensor shape | 张量形状
     * @param value the constant fill value | 常量填充值
     * @return constant-filled tensor | 常量张量
     * @throws NeuralException if shape is null | 如果 shape 为 null
     */
    public static Tensor constant(Shape shape, float value) {
        if (shape == null) {
            throw new NeuralException("Shape must not be null",
                    NeuralErrorCode.INVALID_INIT_PARAMS);
        }
        float[] data = new float[shape.size()];
        Arrays.fill(data, value);
        return Tensor.fromFloat(data, shape);
    }

    // ==================== Internal Helpers | 内部辅助方法 ====================

    /**
     * Compute fanIn and fanOut from shape.
     * 根据形状计算 fanIn 和 fanOut。
     *
     * @param shape the tensor shape | 张量形状
     * @return int[2] = {fanIn, fanOut}
     */
    static int[] computeFan(Shape shape) {
        int rank = shape.rank();
        if (rank == 0) {
            return new int[]{1, 1};
        }
        if (rank == 1) {
            int n = shape.dim(0);
            return new int[]{n, n};
        }
        // rank >= 2
        long receptiveFieldSize = 1;
        for (int i = 2; i < rank; i++) {
            receptiveFieldSize *= shape.dim(i);
        }
        long fanInL  = (long) shape.dim(1) * receptiveFieldSize;
        long fanOutL = (long) shape.dim(0) * receptiveFieldSize;
        if (fanInL > Integer.MAX_VALUE || fanOutL > Integer.MAX_VALUE) {
            throw new NeuralException(
                    "Fan value overflows int: fanIn=" + fanInL + ", fanOut=" + fanOutL,
                    NeuralErrorCode.INVALID_INIT_PARAMS);
        }
        int fanIn  = (int) fanInL;
        int fanOut = (int) fanOutL;
        return new int[]{fanIn, fanOut};
    }

    private static void validateInputs(Shape shape, Random rng) {
        if (shape == null) {
            throw new NeuralException("Shape must not be null",
                    NeuralErrorCode.INVALID_INIT_PARAMS);
        }
        if (rng == null) {
            throw new NeuralException("Random generator must not be null",
                    NeuralErrorCode.INVALID_INIT_PARAMS);
        }
    }

    private static Tensor fillUniform(Shape shape, float low, float high, Random rng) {
        float[] data = new float[shape.size()];
        float range = high - low;
        for (int i = 0; i < data.length; i++) {
            data[i] = low + rng.nextFloat() * range;
        }
        return Tensor.fromFloat(data, shape);
    }

    private static Tensor fillNormal(Shape shape, float mean, float std, Random rng) {
        float[] data = new float[shape.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = mean + (float) rng.nextGaussian() * std;
        }
        return Tensor.fromFloat(data, shape);
    }
}
