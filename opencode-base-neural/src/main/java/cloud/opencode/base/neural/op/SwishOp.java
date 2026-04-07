package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.internal.Activation;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;
import java.util.Objects;

/**
 * Swish (SiLU) Activation Operator
 * Swish (SiLU) 激活算子
 *
 * <p>Applies the Swish activation function element-wise:
 * {@code output = x * sigmoid(x)}. Also known as SiLU (Sigmoid Linear Unit).</p>
 * <p>逐元素应用 Swish 激活函数：{@code output = x * sigmoid(x)}。
 * 又称 SiLU（Sigmoid 线性单元）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Smooth, non-monotonic activation - 平滑、非单调激活</li>
 *   <li>Delegates to {@link Activation#swish} - 委托给 Activation.swish</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Activation#swish(float[], int, int)
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class SwishOp implements Op {

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.isEmpty()) {
            throw new OpExecutionException("SwishOp requires at least 1 input", "Swish");
        }
        Tensor input = inputs.get(0);
        float[] data = input.toFloatArray();
        Activation.swish(data, 0, data.length);
        return List.of(Tensor.wrap(data, input.shape()));
    }
}
