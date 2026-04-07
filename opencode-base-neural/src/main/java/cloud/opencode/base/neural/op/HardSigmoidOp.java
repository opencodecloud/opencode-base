package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;
import java.util.Objects;

/**
 * HardSigmoid Activation Operator
 * HardSigmoid 激活算子
 *
 * <p>Applies the hard sigmoid activation function element-wise:
 * {@code output = clamp(x / 6 + 0.5, 0, 1)}. This is a computationally efficient
 * piecewise-linear approximation of the sigmoid function, widely used in
 * MobileNetV3 and PP-OCR lightweight models.</p>
 * <p>逐元素应用 HardSigmoid 激活函数：{@code output = clamp(x / 6 + 0.5, 0, 1)}。
 * 这是 sigmoid 函数的高效分段线性近似，广泛用于 MobileNetV3 和 PP-OCR 轻量模型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Piecewise-linear sigmoid approximation - 分段线性 sigmoid 近似</li>
 *   <li>No exp() call, faster than standard sigmoid - 无 exp() 调用，比标准 sigmoid 更快</li>
 *   <li>Required by MobileNetV3 architecture - MobileNetV3 架构必需</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Op hardSigmoid = new HardSigmoidOp();
 * List<Tensor> outputs = hardSigmoid.forward(List.of(input), OpAttribute.empty());
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) element-wise - 时间复杂度: O(n) 逐元素</li>
 *   <li>No transcendental function calls - 无超越函数调用</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class HardSigmoidOp implements Op {

    private static final String OP_TYPE = "HardSigmoid";

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.isEmpty()) {
            throw new OpExecutionException("HardSigmoidOp requires at least 1 input", OP_TYPE);
        }
        Tensor input = inputs.get(0);
        float[] data = input.toFloatArray();

        // hard_sigmoid(x) = clamp(x / 6 + 0.5, 0, 1)
        for (int i = 0; i < data.length; i++) {
            data[i] = Math.clamp(data[i] / 6.0f + 0.5f, 0.0f, 1.0f);
        }

        return List.of(Tensor.wrap(data, input.shape()));
    }
}
