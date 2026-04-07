package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;
import java.util.Objects;

/**
 * Flatten Operator
 * 展平算子
 *
 * <p>Reshapes the input tensor to 2D by collapsing dimensions from {@code start_dim}
 * onward into a single dimension. For example, a tensor of shape [N, C, H, W] with
 * {@code start_dim=1} becomes [N, C*H*W]. Uses zero-copy {@link Tensor#reshape} when
 * the tensor is contiguous.</p>
 * <p>将输入张量重塑为二维，将 {@code start_dim} 及之后的维度折叠为单一维度。
 * 例如，形状为 [N, C, H, W] 且 {@code start_dim=1} 的张量变为 [N, C*H*W]。
 * 当张量连续时使用零拷贝的 {@link Tensor#reshape}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Zero-copy reshape when contiguous - 连续时零拷贝重塑</li>
 *   <li>Configurable start_dim (default 1) - 可配置 start_dim（默认 1）</li>
 *   <li>Commonly used between convolutional and fully connected layers - 常用于卷积层和全连接层之间</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Op flatten = new FlattenOp();
 * // Input: [1, 3, 4, 4] → Output: [1, 48]
 * List<Tensor> outputs = flatten.forward(List.of(input), OpAttribute.empty());
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
 * @see Tensor#reshape(int...)
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class FlattenOp implements Op {

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.isEmpty()) {
            throw new OpExecutionException("FlattenOp requires at least 1 input", "Flatten");
        }
        Tensor input = inputs.get(0);
        Shape shape = input.shape();
        int rank = shape.rank();

        int startDim = attrs.getInt("start_dim", 1);
        if (startDim < 0) {
            startDim = startDim + rank;
        }
        if (startDim < 0 || startDim >= rank) {
            throw new OpExecutionException(
                    "FlattenOp start_dim " + startDim + " out of range for rank " + rank, "Flatten");
        }

        // Compute leading dimensions and flattened trailing dimension
        int[] newDims = new int[startDim + 1];
        for (int i = 0; i < startDim; i++) {
            newDims[i] = shape.dim(i);
        }
        int flatDim = 1;
        for (int i = startDim; i < rank; i++) {
            flatDim *= shape.dim(i);
        }
        newDims[startDim] = flatDim;

        Tensor result = input.reshape(newDims);
        return List.of(result);
    }
}
