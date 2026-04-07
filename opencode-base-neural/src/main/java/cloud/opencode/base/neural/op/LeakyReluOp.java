package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.internal.Activation;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;
import java.util.Objects;

/**
 * LeakyReLU Activation Operator
 * LeakyReLU 激活算子
 *
 * <p>Applies the Leaky Rectified Linear Unit activation function element-wise:
 * {@code output = x >= 0 ? x : alpha * x}. The alpha parameter controls the slope
 * for negative inputs and defaults to 0.01.</p>
 * <p>逐元素应用 LeakyReLU 激活函数：{@code output = x >= 0 ? x : alpha * x}。
 * alpha 参数控制负值区域的斜率，默认值为 0.01。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable alpha slope via "alpha" attribute - 通过 "alpha" 属性配置斜率</li>
 *   <li>Non-destructive: input tensor is not modified - 非破坏性：不修改输入张量</li>
 *   <li>Delegates to {@link Activation#leakyRelu} - 委托给 Activation.leakyRelu</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Activation#leakyRelu(float[], int, int, float)
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class LeakyReluOp implements Op {

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.isEmpty()) {
            throw new OpExecutionException("LeakyReluOp requires at least 1 input", "LeakyReLU");
        }
        Tensor input = inputs.get(0);
        float[] data = input.toFloatArray();
        float alpha = attrs.getFloat("alpha", 0.01f);
        Activation.leakyRelu(data, 0, data.length, alpha);
        return List.of(Tensor.wrap(data, input.shape()));
    }
}
