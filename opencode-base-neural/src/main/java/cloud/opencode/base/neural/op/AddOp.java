package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;
import java.util.Objects;

/**
 * Element-wise Addition Operator
 * 逐元素加法算子
 *
 * <p>Computes element-wise addition of two tensors with the same shape:
 * {@code output = inputs[0] + inputs[1]}. Delegates to {@link Tensor#add}.</p>
 * <p>计算两个相同形状张量的逐元素加法：
 * {@code output = inputs[0] + inputs[1]}。委托给 {@link Tensor#add}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Element-wise addition of two tensors - 两个张量的逐元素加法</li>
 *   <li>Same-shape requirement enforced by Tensor.add - 由 Tensor.add 强制相同形状要求</li>
 *   <li>Used for residual connections (skip connections) - 用于残差连接（跳跃连接）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Op add = new AddOp();
 * List<Tensor> outputs = add.forward(List.of(tensorA, tensorB), OpAttribute.empty());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Input validation: null and insufficient input checks - 输入验证：null 和不足输入检查</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Tensor#add(Tensor)
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class AddOp implements Op {

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.size() < 2) {
            throw new OpExecutionException("AddOp requires at least 2 inputs", "Add");
        }
        Tensor result = inputs.get(0).add(inputs.get(1));
        return List.of(result);
    }
}
