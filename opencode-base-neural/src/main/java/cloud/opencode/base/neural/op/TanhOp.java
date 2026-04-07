package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.internal.Activation;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;
import java.util.Objects;

/**
 * Tanh Activation Operator
 * Tanh 激活算子
 *
 * <p>Applies the hyperbolic tangent activation function element-wise:
 * {@code output = tanh(x)}. Creates a copy of the input data and applies
 * the activation in-place on the copy via {@link Activation#tanh}.</p>
 * <p>逐元素应用双曲正切激活函数：{@code output = tanh(x)}。
 * 创建输入数据的副本，并通过 {@link Activation#tanh} 在副本上原地应用激活。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Element-wise tanh activation - 逐元素双曲正切激活</li>
 *   <li>Output range (-1, 1) - 输出范围 (-1, 1)</li>
 *   <li>Non-destructive: input tensor is not modified - 非破坏性：不修改输入张量</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Op tanh = new TanhOp();
 * List<Tensor> outputs = tanh.forward(List.of(input), OpAttribute.empty());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Input validation: null and empty input checks - 输入验证：null 和空输入检查</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Activation#tanh(float[], int, int)
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class TanhOp implements Op {

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.isEmpty()) {
            throw new OpExecutionException("TanhOp requires at least 1 input", "Tanh");
        }
        Tensor input = inputs.get(0);
        float[] data = input.toFloatArray();
        Activation.tanh(data, 0, data.length);
        return List.of(Tensor.wrap(data, input.shape()));
    }
}
