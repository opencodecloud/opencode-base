package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;
import java.util.Objects;

/**
 * Reshape Operator
 * 重塑算子
 *
 * <p>Reshapes the input tensor to a target shape specified via the "shape" attribute.
 * Supports exactly one dimension set to -1 for automatic inference. Uses zero-copy
 * {@link Tensor#reshape} when the tensor is contiguous.</p>
 * <p>将输入张量重塑为通过 "shape" 属性指定的目标形状。
 * 支持恰好一个维度设置为 -1 进行自动推断。
 * 当张量连续时使用零拷贝的 {@link Tensor#reshape}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Zero-copy reshape when contiguous - 连续时零拷贝重塑</li>
 *   <li>Supports -1 dimension inference - 支持 -1 维度推断</li>
 *   <li>Target shape specified via "shape" attribute (int[]) - 通过 "shape" 属性指定目标形状（int[]）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Op reshape = new ReshapeOp();
 * OpAttribute attrs = OpAttribute.builder().put("shape", new int[]{3, 4}).build();
 * List<Tensor> outputs = reshape.forward(List.of(input), attrs);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Input validation: null, empty input, and missing shape attribute checks - 输入验证：null、空输入和缺失形状属性检查</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Tensor#reshape(int...)
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class ReshapeOp implements Op {

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.isEmpty()) {
            throw new OpExecutionException("ReshapeOp requires at least 1 input", "Reshape");
        }
        int[] targetShape = attrs.getIntArray("shape");
        if (targetShape.length == 0) {
            throw new OpExecutionException(
                    "ReshapeOp requires 'shape' attribute (int[])", "Reshape");
        }
        Tensor input = inputs.get(0);
        Tensor result = input.reshape(targetShape);
        return List.of(result);
    }
}
