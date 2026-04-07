package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;
import java.util.Objects;

/**
 * Concatenation Operator
 * 拼接算子
 *
 * <p>Concatenates multiple tensors along a specified axis. All input tensors must
 * have the same shape in all dimensions except the concatenation axis. The output
 * tensor's size along the concatenation axis is the sum of all input sizes along
 * that axis.</p>
 * <p>沿指定轴拼接多个张量。所有输入张量在除拼接轴外的所有维度上必须具有相同形状。
 * 输出张量在拼接轴上的大小是所有输入在该轴上大小的总和。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Concatenation along configurable axis (default 0) - 沿可配置轴拼接（默认 0）</li>
 *   <li>Supports arbitrary number of input tensors - 支持任意数量的输入张量</li>
 *   <li>Shape validation for non-concat dimensions - 非拼接维度的形状验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Op concat = new ConcatOp();
 * OpAttribute attrs = OpAttribute.builder().put("axis", 0).build();
 * List<Tensor> outputs = concat.forward(List.of(tensorA, tensorB), attrs);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Input validation: null, empty input, shape compatibility checks - 输入验证：null、空输入、形状兼容性检查</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class ConcatOp implements Op {

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.isEmpty()) {
            throw new OpExecutionException("ConcatOp requires at least 1 input", "Concat");
        }

        Tensor first = inputs.get(0);
        Shape firstShape = first.shape();
        int rank = firstShape.rank();

        int axis = attrs.getInt("axis", 0);
        int resolvedAxis = axis < 0 ? axis + rank : axis;
        if (resolvedAxis < 0 || resolvedAxis >= rank) {
            throw new OpExecutionException(
                    "ConcatOp axis " + axis + " out of range for rank " + rank, "Concat");
        }

        // Validate shapes and compute total concat dimension
        int totalConcatDim = firstShape.dim(resolvedAxis);
        for (int t = 1; t < inputs.size(); t++) {
            Shape s = inputs.get(t).shape();
            if (s.rank() != rank) {
                throw new OpExecutionException(
                        "ConcatOp all inputs must have same rank, got " + rank + " and " + s.rank(),
                        "Concat");
            }
            for (int d = 0; d < rank; d++) {
                if (d != resolvedAxis && s.dim(d) != firstShape.dim(d)) {
                    throw new OpExecutionException(
                            "ConcatOp shape mismatch at dim " + d + ": " + firstShape.dim(d)
                                    + " vs " + s.dim(d), "Concat");
                }
            }
            totalConcatDim += s.dim(resolvedAxis);
        }

        // Build output shape
        int[] outDims = firstShape.dims();
        outDims[resolvedAxis] = totalConcatDim;
        Shape outShape = Shape.of(outDims);
        float[] outData = new float[outShape.size()];

        // Compute strides for copying: outerSize * concatDim * innerSize
        int innerSize = 1;
        for (int d = resolvedAxis + 1; d < rank; d++) {
            innerSize *= outDims[d];
        }
        int outerSize = 1;
        for (int d = 0; d < resolvedAxis; d++) {
            outerSize *= outDims[d];
        }

        // Copy data from each input tensor
        int concatOffset = 0;
        for (Tensor input : inputs) {
            float[] srcData = input.toFloatArray();
            int srcConcatDim = input.shape().dim(resolvedAxis);
            int srcSliceSize = srcConcatDim * innerSize;
            int outSliceSize = totalConcatDim * innerSize;

            for (int outer = 0; outer < outerSize; outer++) {
                int srcStart = outer * srcSliceSize;
                int dstStart = outer * outSliceSize + concatOffset * innerSize;
                System.arraycopy(srcData, srcStart, outData, dstStart, srcSliceSize);
            }
            concatOffset += srcConcatDim;
        }

        return List.of(Tensor.wrap(outData, outShape));
    }
}
