package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.internal.Activation;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;
import java.util.Objects;

/**
 * GELU Activation Operator
 * GELU 激活算子
 *
 * <p>Applies the Gaussian Error Linear Unit activation function element-wise:
 * {@code output = 0.5 * x * (1 + tanh(sqrt(2/pi) * (x + 0.044715 * x^3)))}.</p>
 * <p>逐元素应用高斯误差线性单元激活函数：
 * {@code output = 0.5 * x * (1 + tanh(sqrt(2/pi) * (x + 0.044715 * x^3)))}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Default activation in Transformer/BERT architectures - Transformer/BERT 架构的默认激活</li>
 *   <li>Delegates to {@link Activation#gelu} - 委托给 Activation.gelu</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Activation#gelu(float[], int, int)
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class GeluOp implements Op {

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.isEmpty()) {
            throw new OpExecutionException("GeluOp requires at least 1 input", "GELU");
        }
        Tensor input = inputs.get(0);
        float[] data = input.toFloatArray();
        Activation.gelu(data, 0, data.length);
        return List.of(Tensor.wrap(data, input.shape()));
    }
}
