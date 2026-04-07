package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.internal.Activation;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;
import java.util.Objects;

/**
 * Mish Activation Operator
 * Mish 激活算子
 *
 * <p>Applies the Mish activation function element-wise:
 * {@code output = x * tanh(ln(1 + exp(x)))}.</p>
 * <p>逐元素应用 Mish 激活函数：{@code output = x * tanh(ln(1 + exp(x)))}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Self-regularized non-monotonic activation - 自正则化非单调激活</li>
 *   <li>Delegates to {@link Activation#mish} - 委托给 Activation.mish</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Activation#mish(float[], int, int)
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class MishOp implements Op {

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.isEmpty()) {
            throw new OpExecutionException("MishOp requires at least 1 input", "Mish");
        }
        Tensor input = inputs.get(0);
        float[] data = input.toFloatArray();
        Activation.mish(data, 0, data.length);
        return List.of(Tensor.wrap(data, input.shape()));
    }
}
