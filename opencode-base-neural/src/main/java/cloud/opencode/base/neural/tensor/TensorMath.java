package cloud.opencode.base.neural.tensor;

/**
 * Tensor Math — Static Mathematical Operations
 * 张量数学 — 静态数学运算
 *
 * <p>Provides element-wise mathematical functions applied to tensors.
 * Each method creates and returns a new tensor with the result,
 * leaving the input tensor unchanged.</p>
 * <p>提供应用于张量的逐元素数学函数。
 * 每个方法创建并返回包含结果的新张量，保持输入张量不变。</p>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Tensor
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class TensorMath {

    private TensorMath() {
        // Utility class, no instantiation
    }

    /**
     * Element-wise exponential: e^x
     * 逐元素指数函数：e^x
     *
     * @param t input tensor | 输入张量
     * @return new tensor with exp applied | 应用 exp 后的新张量
     */
    public static Tensor exp(Tensor t) {
        return applyUnary(t, v -> (float) Math.exp(v));
    }

    /**
     * Element-wise natural logarithm: ln(x)
     * 逐元素自然对数：ln(x)
     *
     * @param t input tensor | 输入张量
     * @return new tensor with log applied | 应用 log 后的新张量
     */
    public static Tensor log(Tensor t) {
        return applyUnary(t, v -> (float) Math.log(v));
    }

    /**
     * Element-wise square root: sqrt(x)
     * 逐元素平方根：sqrt(x)
     *
     * @param t input tensor | 输入张量
     * @return new tensor with sqrt applied | 应用 sqrt 后的新张量
     */
    public static Tensor sqrt(Tensor t) {
        return applyUnary(t, v -> (float) Math.sqrt(v));
    }

    /**
     * Element-wise absolute value: |x|
     * 逐元素绝对值：|x|
     *
     * @param t input tensor | 输入张量
     * @return new tensor with abs applied | 应用 abs 后的新张量
     */
    public static Tensor abs(Tensor t) {
        return applyUnary(t, Math::abs);
    }

    /**
     * Element-wise negation: -x
     * 逐元素取反：-x
     *
     * @param t input tensor | 输入张量
     * @return new tensor with negation applied | 应用取反后的新张量
     */
    public static Tensor neg(Tensor t) {
        return applyUnary(t, v -> -v);
    }

    /**
     * Element-wise clamp: clamp(x, min, max)
     * 逐元素裁剪：clamp(x, min, max)
     *
     * @param t   input tensor | 输入张量
     * @param min minimum value | 最小值
     * @param max maximum value | 最大值
     * @return new tensor with values clamped to [min, max] | 值裁剪到 [min, max] 的新张量
     */
    public static Tensor clamp(Tensor t, float min, float max) {
        return applyUnary(t, v -> Math.max(min, Math.min(max, v)));
    }

    /**
     * Element-wise ReLU activation: max(0, x)
     * 逐元素 ReLU 激活函数：max(0, x)
     *
     * @param t input tensor | 输入张量
     * @return new tensor with ReLU applied | 应用 ReLU 后的新张量
     */
    public static Tensor relu(Tensor t) {
        return applyUnary(t, v -> Math.max(0, v));
    }

    /**
     * Element-wise Sigmoid activation: 1 / (1 + exp(-x))
     * 逐元素 Sigmoid 激活函数：1 / (1 + exp(-x))
     *
     * @param t input tensor | 输入张量
     * @return new tensor with sigmoid applied | 应用 sigmoid 后的新张量
     */
    public static Tensor sigmoid(Tensor t) {
        return applyUnary(t, v -> 1.0f / (1.0f + (float) Math.exp(-v)));
    }

    /**
     * Element-wise Tanh activation: tanh(x)
     * 逐元素 Tanh 激活函数：tanh(x)
     *
     * @param t input tensor | 输入张量
     * @return new tensor with tanh applied | 应用 tanh 后的新张量
     */
    public static Tensor tanh(Tensor t) {
        return applyUnary(t, v -> (float) Math.tanh(v));
    }

    /**
     * Element-wise LeakyReLU activation: x >= 0 ? x : alpha * x
     * 逐元素 LeakyReLU 激活函数：x >= 0 ? x : alpha * x
     *
     * @param t     input tensor | 输入张量
     * @param alpha slope for negative values | 负值斜率
     * @return new tensor with leaky relu applied | 应用 LeakyReLU 后的新张量
     */
    public static Tensor leakyRelu(Tensor t, float alpha) {
        return applyUnary(t, v -> v >= 0 ? v : alpha * v);
    }

    /**
     * Element-wise ELU activation: x >= 0 ? x : alpha * (exp(x) - 1)
     * 逐元素 ELU 激活函数：x >= 0 ? x : alpha * (exp(x) - 1)
     *
     * @param t     input tensor | 输入张量
     * @param alpha scale for negative region | 负区域缩放系数
     * @return new tensor with ELU applied | 应用 ELU 后的新张量
     */
    public static Tensor elu(Tensor t, float alpha) {
        return applyUnary(t, v -> v >= 0 ? v : alpha * ((float) Math.exp(v) - 1.0f));
    }

    /**
     * Element-wise SELU activation: lambda * (x >= 0 ? x : alpha * (exp(x) - 1))
     * 逐元素 SELU 激活函数
     *
     * @param t input tensor | 输入张量
     * @return new tensor with SELU applied | 应用 SELU 后的新张量
     */
    public static Tensor selu(Tensor t) {
        final float alpha = 1.6732632423543772f;
        final float lambda = 1.0507009873554805f;
        return applyUnary(t, v -> v >= 0 ? lambda * v : lambda * alpha * ((float) Math.exp(v) - 1.0f));
    }

    /**
     * Element-wise GELU activation: 0.5 * x * (1 + tanh(sqrt(2/pi) * (x + 0.044715 * x^3)))
     * 逐元素 GELU 激活函数
     *
     * @param t input tensor | 输入张量
     * @return new tensor with GELU applied | 应用 GELU 后的新张量
     */
    public static Tensor gelu(Tensor t) {
        final float c = (float) Math.sqrt(2.0 / Math.PI);
        return applyUnary(t, v -> {
            float inner = c * (v + 0.044715f * v * v * v);
            return 0.5f * v * (1.0f + (float) Math.tanh(inner));
        });
    }

    /**
     * Element-wise Swish (SiLU) activation: x * sigmoid(x)
     * 逐元素 Swish (SiLU) 激活函数：x * sigmoid(x)
     *
     * @param t input tensor | 输入张量
     * @return new tensor with Swish applied | 应用 Swish 后的新张量
     */
    public static Tensor swish(Tensor t) {
        return applyUnary(t, v -> v * (1.0f / (1.0f + (float) Math.exp(-v))));
    }

    /**
     * Element-wise Mish activation: x * tanh(ln(1 + exp(x)))
     * 逐元素 Mish 激活函数：x * tanh(ln(1 + exp(x)))
     *
     * @param t input tensor | 输入张量
     * @return new tensor with Mish applied | 应用 Mish 后的新张量
     */
    public static Tensor mish(Tensor t) {
        return applyUnary(t, v -> {
            float sp = v > 20.0f ? v : (float) Math.log(1.0 + Math.exp(v));
            return v * (float) Math.tanh(sp);
        });
    }

    /**
     * Element-wise Softplus activation: ln(1 + exp(x)), with overflow protection
     * 逐元素 Softplus 激活函数：ln(1 + exp(x))，带溢出保护
     *
     * @param t input tensor | 输入张量
     * @return new tensor with Softplus applied | 应用 Softplus 后的新张量
     */
    public static Tensor softplus(Tensor t) {
        return applyUnary(t, v -> v > 20.0f ? v : (float) Math.log(1.0 + Math.exp(v)));
    }

    // ==================== Internal Helpers | 内部辅助方法 ====================

    /**
     * Apply a unary float function to every element, producing a new contiguous tensor
     */
    private static Tensor applyUnary(Tensor t, FloatUnaryOp op) {
        int size = t.size();
        float[] result = new float[size];
        if (t.isContiguous()) {
            // Fast path: direct array access — no copy of source, no clone in wrap
            float[] src = t.data();
            for (int i = 0; i < size; i++) {
                result[i] = op.apply(src[i]);
            }
        } else {
            // Non-contiguous: single copy via toFloatArray()
            float[] src = t.toFloatArray();
            for (int i = 0; i < size; i++) {
                result[i] = op.apply(src[i]);
            }
        }
        return Tensor.wrap(result, t.shape());
    }

    /**
     * Functional interface for unary float operations
     */
    @FunctionalInterface
    private interface FloatUnaryOp {
        float apply(float v);
    }
}
