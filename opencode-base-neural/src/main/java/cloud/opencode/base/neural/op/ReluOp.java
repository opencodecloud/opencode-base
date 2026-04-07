package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.internal.Activation;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;
import java.util.Objects;

/**
 * ReLU Activation Operator
 * ReLU 激活算子
 *
 * <p>Applies the Rectified Linear Unit activation function element-wise:
 * {@code output = max(0, x)}. Creates a copy of the input data and applies
 * the activation in-place on the copy via {@link Activation#relu}.</p>
 * <p>逐元素应用修正线性单元激活函数：{@code output = max(0, x)}。
 * 创建输入数据的副本，并通过 {@link Activation#relu} 在副本上原地应用激活。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Element-wise max(0, x) activation - 逐元素 max(0, x) 激活</li>
 *   <li>Non-destructive: input tensor is not modified - 非破坏性：不修改输入张量</li>
 *   <li>Delegates to numerically optimized {@link Activation#relu} - 委托给数值优化的 Activation.relu</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Op relu = new ReluOp();
 * List<Tensor> outputs = relu.forward(List.of(input), OpAttribute.empty());
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
 * @see Activation#relu(float[], int, int)
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class ReluOp implements Op {

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.isEmpty()) {
            throw new OpExecutionException("ReluOp requires at least 1 input", "ReLU");
        }
        Tensor input = inputs.get(0);
        float[] data = input.toFloatArray();
        Activation.relu(data, 0, data.length);
        return List.of(Tensor.wrap(data, input.shape()));
    }
}
