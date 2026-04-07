package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.internal.Blas;
import cloud.opencode.base.neural.internal.Im2Col;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;

/**
 * 2D Convolution Operator
 * 二维卷积算子
 *
 * <p>Implements standard 2D convolution using the im2col + GEMM approach.
 * Supports configurable stride, padding, dilation, and grouped convolution.</p>
 * <p>使用 im2col + GEMM 方法实现标准二维卷积。
 * 支持可配置的步幅、填充、膨胀和分组卷积。</p>
 *
 * <p><strong>Inputs | 输入:</strong></p>
 * <ul>
 *   <li>inputs[0]: input tensor [N, C_in, H, W] - 输入张量 [N, C_in, H, W]</li>
 *   <li>inputs[1]: weight tensor [C_out, C_in/groups, kH, kW] - 权重张量 [C_out, C_in/groups, kH, kW]</li>
 *   <li>inputs[2]: (optional) bias tensor [C_out] - （可选）偏置张量 [C_out]</li>
 * </ul>
 *
 * <p><strong>Attributes | 属性:</strong></p>
 * <ul>
 *   <li>stride (int, default=1) - 步幅</li>
 *   <li>padding (int, default=0) - 填充</li>
 *   <li>dilation (int, default=1) - 膨胀（当前仅支持 1）</li>
 *   <li>groups (int, default=1) - 分组数</li>
 * </ul>
 *
 * <p><strong>Output | 输出:</strong></p>
 * <ul>
 *   <li>output[0]: result tensor [N, C_out, outH, outW] - 结果张量 [N, C_out, outH, outW]</li>
 * </ul>
 *
 * <p><strong>Algorithm | 算法:</strong></p>
 * <ol>
 *   <li>For each sample in batch, apply im2col to unfold input patches - 对批次中每个样本，用 im2col 展开输入块</li>
 *   <li>Matrix multiply weight with column matrix via BLAS GEMM - 通过 BLAS GEMM 将权重与列矩阵做矩阵乘法</li>
 *   <li>Add bias if present - 如果有偏置则加上偏置</li>
 *   <li>For groups &gt; 1, split channels and convolve each group separately - 分组大于1时，拆分通道分别卷积</li>
 * </ol>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Op
 * @see Im2Col
 * @see Blas
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class Conv2DOp implements Op {

    private static final String OP_TYPE = "Conv2D";

    /**
     * Execute 2D convolution forward computation
     * 执行二维卷积前向计算
     *
     * @param inputs ordered input tensors: [input, weight, optional bias] | 有序输入张量: [输入, 权重, 可选偏置]
     * @param attrs  operator attributes (stride, padding, dilation, groups) | 算子属性（步幅、填充、膨胀、分组）
     * @return single-element list containing the output tensor [N, C_out, outH, outW] |
     *         包含输出张量 [N, C_out, outH, outW] 的单元素列表
     * @throws OpExecutionException if inputs are null, insufficient, or dimensions are invalid |
     *                              如果输入为 null、不足或维度无效
     */
    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        if (inputs == null || inputs.size() < 2) {
            throw new OpExecutionException(
                    "Conv2D requires at least 2 inputs (input, weight), got: "
                            + (inputs == null ? "null" : inputs.size()), OP_TYPE);
        }

        Tensor input = inputs.get(0);
        Tensor weight = inputs.get(1);
        Tensor bias = inputs.size() > 2 ? inputs.get(2) : null;

        if (input == null || weight == null) {
            throw new OpExecutionException("Conv2D input and weight must not be null", OP_TYPE);
        }

        // Validate ranks
        if (input.shape().rank() != 4) {
            throw new OpExecutionException(
                    "Conv2D input must be 4D [N,C,H,W], got rank " + input.shape().rank(), OP_TYPE);
        }
        if (weight.shape().rank() != 4) {
            throw new OpExecutionException(
                    "Conv2D weight must be 4D [Cout,Cin/g,kH,kW], got rank " + weight.shape().rank(), OP_TYPE);
        }

        int stride = attrs.getInt("stride", 1);
        int padding = attrs.getInt("padding", 0);
        int dilation = attrs.getInt("dilation", 1);
        int groups = attrs.getInt("groups", 1);

        if (dilation != 1) {
            throw new OpExecutionException(
                    "Conv2D currently only supports dilation=1, got " + dilation, OP_TYPE);
        }

        int n = input.shape().dim(0);
        int cIn = input.shape().dim(1);
        int h = input.shape().dim(2);
        int w = input.shape().dim(3);

        int cOut = weight.shape().dim(0);
        int cInPerGroup = weight.shape().dim(1);
        int kH = weight.shape().dim(2);
        int kW = weight.shape().dim(3);

        // Validate groups
        if (groups <= 0 || cIn % groups != 0 || cOut % groups != 0) {
            throw new OpExecutionException(
                    "Conv2D: groups=" + groups + " must divide both C_in=" + cIn
                            + " and C_out=" + cOut, OP_TYPE);
        }
        if (cInPerGroup != cIn / groups) {
            throw new OpExecutionException(
                    "Conv2D: weight C_in/groups dimension mismatch, expected "
                            + (cIn / groups) + " but got " + cInPerGroup, OP_TYPE);
        }

        // Validate bias shape
        if (bias != null && bias.size() != cOut) {
            throw new OpExecutionException(
                    "Conv2D: bias size " + bias.size() + " != C_out " + cOut, OP_TYPE);
        }

        int outH = Im2Col.outputHeight(h, kH, stride, padding);
        int outW = Im2Col.outputWidth(w, kW, stride, padding);

        float[] inputData = input.toFloatArray();
        float[] weightData = weight.toFloatArray();
        float[] biasData = bias != null ? bias.toFloatArray() : null;

        long outSize = (long) n * cOut * outH * outW;
        if (outSize > Integer.MAX_VALUE) {
            throw new OpExecutionException("output too large", OP_TYPE);
        }
        float[] outputData = new float[(int) outSize];

        int cOutPerGroup = cOut / groups;
        int inputGroupSize = cInPerGroup * h * w;
        int outputGroupSize = cOutPerGroup * outH * outW;
        int weightGroupSize = cOutPerGroup * cInPerGroup * kH * kW;

        int colRows = cInPerGroup * kH * kW;
        int colCols = outH * outW;
        long colSize = (long) colRows * colCols;
        if (colSize > Integer.MAX_VALUE) {
            throw new OpExecutionException("col buffer too large", OP_TYPE);
        }
        float[] col = new float[(int) colSize];

        // Pre-allocate reusable buffers outside the loop to reduce GC pressure
        float[] groupInput = new float[cInPerGroup * h * w];
        float[] groupWeight = new float[weightGroupSize];

        for (int sample = 0; sample < n; sample++) {
            int inputBaseOffset = sample * cIn * h * w;
            int outputBaseOffset = sample * cOut * outH * outW;

            for (int g = 0; g < groups; g++) {
                // Extract the group's input channels (reuse buffer)
                int groupInputOffset = inputBaseOffset + g * inputGroupSize;
                System.arraycopy(inputData, groupInputOffset, groupInput, 0, groupInput.length);

                // Im2Col for this group
                Im2Col.im2col(groupInput, cInPerGroup, h, w, kH, kW,
                        stride, stride, padding, padding, col);

                // Weight for this group: [cOutPerGroup, cInPerGroup*kH*kW] (reuse buffer)
                int weightOffset = g * weightGroupSize;
                System.arraycopy(weightData, weightOffset, groupWeight, 0, groupWeight.length);

                // GEMM: weight[cOutPerGroup, colRows] x col[colRows, colCols] → result[cOutPerGroup, colCols]
                float[] groupOutput = Blas.matmul(groupWeight, cOutPerGroup, colRows,
                        col, colRows, colCols);

                // Add bias if present
                if (biasData != null) {
                    int biasOffset = g * cOutPerGroup;
                    for (int oc = 0; oc < cOutPerGroup; oc++) {
                        float b = biasData[biasOffset + oc];
                        int rowStart = oc * colCols;
                        for (int j = 0; j < colCols; j++) {
                            groupOutput[rowStart + j] += b;
                        }
                    }
                }

                // Copy to output
                int outGroupOffset = outputBaseOffset + g * outputGroupSize;
                System.arraycopy(groupOutput, 0, outputData, outGroupOffset, outputGroupSize);
            }
        }

        Tensor result = Tensor.wrap(outputData, Shape.of(n, cOut, outH, outW));
        return List.of(result);
    }
}
