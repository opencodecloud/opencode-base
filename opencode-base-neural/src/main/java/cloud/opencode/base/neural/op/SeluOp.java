package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.internal.Activation;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;
import java.util.Objects;

/**
 * SELU Activation Operator
 * SELU 激活算子
 *
 * <p>Applies the Scaled Exponential Linear Unit activation function element-wise:
 * {@code output = lambda * (x >= 0 ? x : alpha * (exp(x) - 1))} with fixed constants
 * alpha = 1.6732632423543772 and lambda = 1.0507009873554805.</p>
 * <p>逐元素应用缩放指数线性单元激活函数，使用固定常量
 * alpha = 1.6732632423543772，lambda = 1.0507009873554805。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Self-normalizing activation for deep networks - 深层网络的自归一化激活</li>
 *   <li>Delegates to {@link Activation#selu} - 委托给 Activation.selu</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Activation#selu(float[], int, int)
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class SeluOp implements Op {

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.isEmpty()) {
            throw new OpExecutionException("SeluOp requires at least 1 input", "SELU");
        }
        Tensor input = inputs.get(0);
        float[] data = input.toFloatArray();
        Activation.selu(data, 0, data.length);
        return List.of(Tensor.wrap(data, input.shape()));
    }
}
