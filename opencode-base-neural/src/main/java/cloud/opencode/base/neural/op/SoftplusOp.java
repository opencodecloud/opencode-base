package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.internal.Activation;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;
import java.util.Objects;

/**
 * Softplus Activation Operator
 * Softplus 激活算子
 *
 * <p>Applies the Softplus activation function element-wise:
 * {@code output = ln(1 + exp(x))}, with overflow protection for large values.</p>
 * <p>逐元素应用 Softplus 激活函数：{@code output = ln(1 + exp(x))}，
 * 对大值有溢出保护。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Smooth approximation of ReLU - ReLU 的平滑近似</li>
 *   <li>Overflow-safe for large positive values - 大正值溢出安全</li>
 *   <li>Delegates to {@link Activation#softplus} - 委托给 Activation.softplus</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Activation#softplus(float[], int, int)
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.3
 */
public final class SoftplusOp implements Op {

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.isEmpty()) {
            throw new OpExecutionException("SoftplusOp requires at least 1 input", "Softplus");
        }
        Tensor input = inputs.get(0);
        float[] data = input.toFloatArray();
        Activation.softplus(data, 0, data.length);
        return List.of(Tensor.wrap(data, input.shape()));
    }
}
