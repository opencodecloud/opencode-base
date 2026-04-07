package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;
import java.util.Objects;

/**
 * HardSwish Activation Operator
 * HardSwish 激活算子
 *
 * <p>Applies the hard swish activation function element-wise:
 * {@code output = x * hard_sigmoid(x) = x * clamp(x / 6 + 0.5, 0, 1)}.
 * This is a computationally efficient approximation of the swish function
 * ({@code x * sigmoid(x)}), widely used in MobileNetV3 and PP-OCR lightweight models.</p>
 * <p>逐元素应用 HardSwish 激活函数：
 * {@code output = x * hard_sigmoid(x) = x * clamp(x / 6 + 0.5, 0, 1)}。
 * 这是 swish 函数（{@code x * sigmoid(x)}）的高效近似，
 * 广泛用于 MobileNetV3 和 PP-OCR 轻量模型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Efficient swish approximation without exp() - 无 exp() 的高效 swish 近似</li>
 *   <li>Required by MobileNetV3 / PP-OCR architecture - MobileNetV3 / PP-OCR 架构必需</li>
 *   <li>Smooth, non-monotonic activation for better gradient flow - 平滑非单调激活，梯度流更好</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Op hardSwish = new HardSwishOp();
 * List<Tensor> outputs = hardSwish.forward(List.of(input), OpAttribute.empty());
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
 * @see HardSigmoidOp
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class HardSwishOp implements Op {

    private static final String OP_TYPE = "HardSwish";

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.isEmpty()) {
            throw new OpExecutionException("HardSwishOp requires at least 1 input", OP_TYPE);
        }
        Tensor input = inputs.get(0);
        float[] data = input.toFloatArray();

        // hard_swish(x) = x * hard_sigmoid(x) = x * clamp(x/6 + 0.5, 0, 1)
        for (int i = 0; i < data.length; i++) {
            float hs = Math.clamp(data[i] / 6.0f + 0.5f, 0.0f, 1.0f);
            data[i] = data[i] * hs;
        }

        return List.of(Tensor.wrap(data, input.shape()));
    }
}
