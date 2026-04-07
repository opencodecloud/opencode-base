package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.internal.Activation;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;
import java.util.Objects;

/**
 * Softmax Activation Operator
 * Softmax 激活算子
 *
 * <p>Applies numerically stable softmax along a configurable axis of the input tensor.
 * For the last axis (default), uses the optimized {@link Activation#softmaxBatch} path.
 * For non-last axes, decomposes the tensor into [outer, axis, inner] dimensions and
 * applies softmax over strided elements for each (outer, inner) position.</p>
 * <p>沿输入张量的可配置轴应用数值稳定的 Softmax。对于最后一个轴（默认），
 * 使用优化的 {@link Activation#softmaxBatch} 路径。对于非末轴，将张量分解为
 * [outer, axis, inner] 维度，并对每个 (outer, inner) 位置的跨步元素应用 Softmax。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Numerically stable softmax (max subtraction) - 数值稳定的 Softmax（最大值减法）</li>
 *   <li>Supports batched input (2D+) - 支持批量输入（二维及以上）</li>
 *   <li>Configurable axis via "axis" attribute (default -1, last axis) - 可通过 "axis" 属性配置轴（默认 -1，最后一个轴）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Op softmax = new SoftmaxOp();
 * List<Tensor> outputs = softmax.forward(List.of(input), OpAttribute.empty());
 * // Output probabilities sum to 1.0 along the last axis
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
 * @see Activation#softmax(float[], int, int)
 * @see Activation#softmaxBatch(float[], int, int)
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class SoftmaxOp implements Op {

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.isEmpty()) {
            throw new OpExecutionException("SoftmaxOp requires at least 1 input", "Softmax");
        }
        Tensor input = inputs.get(0);
        Shape shape = input.shape();
        float[] data = input.toFloatArray();

        int rank = shape.rank();
        int axis = attrs.getInt("axis", -1);
        int resolvedAxis = axis < 0 ? axis + rank : axis;
        if (resolvedAxis < 0 || resolvedAxis >= rank) {
            throw new OpExecutionException(
                    "SoftmaxOp axis " + axis + " out of range for rank " + rank, "Softmax");
        }

        // Softmax along last axis: treat as [rows, cols] where cols = dim(last)
        int cols = shape.dim(resolvedAxis);
        int rows = data.length / cols;

        if (resolvedAxis == rank - 1) {
            // Fast path: softmax along last axis
            Activation.softmaxBatch(data, rows, cols);
        } else {
            // Non-last axis softmax:
            // Decompose the tensor into [outerSize, axisSize, innerSize] where
            //   innerSize = product of dims after resolvedAxis
            //   outerSize = total / (axisSize * innerSize)
            // For each (outer, inner) position, apply softmax over axisSize elements
            // spaced innerSize apart in the flat array.
            int axisSize = shape.dim(resolvedAxis);
            int innerSize = 1;
            for (int d = resolvedAxis + 1; d < rank; d++) {
                innerSize *= shape.dim(d);
            }
            int outerSize = data.length / (axisSize * innerSize);

            for (int outer = 0; outer < outerSize; outer++) {
                for (int inner = 0; inner < innerSize; inner++) {
                    int base = outer * axisSize * innerSize + inner;
                    // Find max for numerical stability
                    float max = Float.NEGATIVE_INFINITY;
                    for (int a = 0; a < axisSize; a++) {
                        max = Math.max(max, data[base + a * innerSize]);
                    }
                    // Compute exp and sum
                    float sum = 0;
                    for (int a = 0; a < axisSize; a++) {
                        int idx = base + a * innerSize;
                        data[idx] = (float) Math.exp(data[idx] - max);
                        sum += data[idx];
                    }
                    // Normalize
                    if (sum > 0) {
                        for (int a = 0; a < axisSize; a++) {
                            data[base + a * innerSize] /= sum;
                        }
                    }
                }
            }
        }

        return List.of(Tensor.wrap(data, shape));
    }
}
