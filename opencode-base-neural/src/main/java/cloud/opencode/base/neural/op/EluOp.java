package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.internal.Activation;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;
import java.util.Objects;

/**
 * ELU Activation Operator
 * ELU 激活算子
 *
 * <p>Applies the Exponential Linear Unit activation function element-wise:
 * {@code output = x >= 0 ? x : alpha * (exp(x) - 1)}. The alpha parameter
 * defaults to 1.0.</p>
 * <p>逐元素应用指数线性单元激活函数：{@code output = x >= 0 ? x : alpha * (exp(x) - 1)}。
 * alpha 参数默认值为 1.0。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable alpha via "alpha" attribute - 通过 "alpha" 属性配置缩放系数</li>
 *   <li>Smooth negative region for better gradient flow - 平滑负区域以改善梯度流</li>
 *   <li>Delegates to {@link Activation#elu} - 委托给 Activation.elu</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Activation#elu(float[], int, int, float)
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class EluOp implements Op {

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.isEmpty()) {
            throw new OpExecutionException("EluOp requires at least 1 input", "ELU");
        }
        Tensor input = inputs.get(0);
        float[] data = input.toFloatArray();
        float alpha = attrs.getFloat("alpha", 1.0f);
        Activation.elu(data, 0, data.length, alpha);
        return List.of(Tensor.wrap(data, input.shape()));
    }
}
