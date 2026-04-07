package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.internal.Blas;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;

/**
 * 1D Convolution Operator
 * 一维卷积算子
 *
 * <p>Implements standard 1D convolution using the im2col + GEMM approach.
 * Supports configurable stride and padding.</p>
 * <p>使用 im2col + GEMM 方法实现标准一维卷积。
 * 支持可配置的步幅和填充。</p>
 *
 * <p><strong>Inputs | 输入:</strong></p>
 * <ul>
 *   <li>inputs[0]: input tensor [N, C_in, L] - 输入张量 [N, C_in, L]</li>
 *   <li>inputs[1]: weight tensor [C_out, C_in, K] - 权重张量 [C_out, C_in, K]</li>
 *   <li>inputs[2]: (optional) bias tensor [C_out] - （可选）偏置张量 [C_out]</li>
 * </ul>
 *
 * <p><strong>Attributes | 属性:</strong></p>
 * <ul>
 *   <li>stride (int, default=1) - 步幅</li>
 *   <li>padding (int, default=0) - 填充</li>
 * </ul>
 *
 * <p><strong>Output | 输出:</strong></p>
 * <ul>
 *   <li>output[0]: result tensor [N, C_out, L_out] - 结果张量 [N, C_out, L_out]</li>
 *   <li>where L_out = (L + 2*padding - K) / stride + 1</li>
 * </ul>
 *
 * <p><strong>Algorithm | 算法:</strong></p>
 * <ol>
 *   <li>For each sample in batch, apply 1D im2col to unfold input patches -
 *       对批次中每个样本，用一维 im2col 展开输入块</li>
 *   <li>Matrix multiply weight with column matrix via BLAS GEMM -
 *       通过 BLAS GEMM 将权重与列矩阵做矩阵乘法</li>
 *   <li>Add bias if present - 如果有偏置则加上偏置</li>
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
 * @see Blas
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class Conv1DOp implements Op {

    private static final String OP_TYPE = "Conv1D";

    /**
     * Execute 1D convolution forward computation
     * 执行一维卷积前向计算
     *
     * @param inputs ordered input tensors: [input, weight, optional bias] |
     *               有序输入张量: [输入, 权重, 可选偏置]
     * @param attrs  operator attributes (stride, padding) |
     *               算子属性（步幅、填充）
     * @return single-element list containing the output tensor [N, C_out, L_out] |
     *         包含输出张量 [N, C_out, L_out] 的单元素列表
     * @throws OpExecutionException if inputs are null, insufficient, or dimensions are invalid |
     *                              如果输入为 null、不足或维度无效
     */
    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        if (inputs == null || inputs.size() < 2) {
            throw new OpExecutionException(
                    "Conv1D requires at least 2 inputs (input, weight), got: "
                            + (inputs == null ? "null" : inputs.size()), OP_TYPE);
        }

        Tensor input = inputs.get(0);
        Tensor weight = inputs.get(1);
        Tensor bias = inputs.size() > 2 ? inputs.get(2) : null;

        if (input == null || weight == null) {
            throw new OpExecutionException("Conv1D input and weight must not be null", OP_TYPE);
        }

        // Validate ranks
        if (input.shape().rank() != 3) {
            throw new OpExecutionException(
                    "Conv1D input must be 3D [N,C_in,L], got rank " + input.shape().rank(), OP_TYPE);
        }
        if (weight.shape().rank() != 3) {
            throw new OpExecutionException(
                    "Conv1D weight must be 3D [C_out,C_in,K], got rank " + weight.shape().rank(), OP_TYPE);
        }

        int stride = attrs.getInt("stride", 1);
        int padding = attrs.getInt("padding", 0);

        if (stride <= 0) {
            throw new OpExecutionException("Conv1D stride must be positive, got " + stride, OP_TYPE);
        }
        if (padding < 0) {
            throw new OpExecutionException("Conv1D padding must be non-negative, got " + padding, OP_TYPE);
        }

        int n = input.shape().dim(0);
        int cIn = input.shape().dim(1);
        int length = input.shape().dim(2);

        int cOut = weight.shape().dim(0);
        int cInW = weight.shape().dim(1);
        int k = weight.shape().dim(2);

        // Validate channel dimensions
        if (cInW != cIn) {
            throw new OpExecutionException(
                    "Conv1D: weight C_in dimension mismatch, expected "
                            + cIn + " but got " + cInW, OP_TYPE);
        }

        // Validate bias shape
        if (bias != null && bias.size() != cOut) {
            throw new OpExecutionException(
                    "Conv1D: bias size " + bias.size() + " != C_out " + cOut, OP_TYPE);
        }

        // Compute output length (use long to prevent overflow in 2*padding)
        long lOutL = ((long) length + 2L * padding - k) / stride + 1;
        if (lOutL <= 0 || lOutL > Integer.MAX_VALUE) {
            throw new OpExecutionException(
                    "Conv1D: invalid output length " + lOutL
                            + " (L=" + length + ", K=" + k + ", stride=" + stride
                            + ", padding=" + padding + ")", OP_TYPE);
        }
        int lOut = (int) lOutL;
        if (lOut <= 0) { // redundant guard after long check above, kept for safety
            throw new OpExecutionException(
                    "Conv1D: invalid output length " + lOut
                            + " (L=" + length + ", K=" + k + ", stride=" + stride
                            + ", padding=" + padding + ")", OP_TYPE);
        }

        float[] inputData = input.toFloatArray();
        float[] weightData = weight.toFloatArray();
        float[] biasData = bias != null ? bias.toFloatArray() : null;

        long outSize = (long) n * cOut * lOut;
        if (outSize > Integer.MAX_VALUE) {
            throw new OpExecutionException("Conv1D output too large", OP_TYPE);
        }
        float[] outputData = new float[(int) outSize];

        // im2col dimensions: [cIn * K, lOut]
        int colRows = cIn * k;
        int colCols = lOut;
        long colSize = (long) colRows * colCols;
        if (colSize > Integer.MAX_VALUE) {
            throw new OpExecutionException("Conv1D col buffer too large", OP_TYPE);
        }
        float[] col = new float[(int) colSize];

        for (int sample = 0; sample < n; sample++) {
            int inputOffset = sample * cIn * length;
            int outputOffset = sample * cOut * lOut;

            // 1D im2col: unfold input [C_in, L] to col [C_in * K, L_out]
            im2col1d(inputData, inputOffset, cIn, length, k, stride, padding, col);

            // GEMM: weight[C_out, C_in*K] @ col[C_in*K, L_out] → result[C_out, L_out]
            float[] result = Blas.matmul(weightData, cOut, colRows, col, colRows, colCols);

            // Add bias if present
            if (biasData != null) {
                for (int oc = 0; oc < cOut; oc++) {
                    float b = biasData[oc];
                    int rowStart = oc * lOut;
                    for (int j = 0; j < lOut; j++) {
                        result[rowStart + j] += b;
                    }
                }
            }

            // Copy to output
            System.arraycopy(result, 0, outputData, outputOffset, cOut * lOut);
        }

        return List.of(Tensor.wrap(outputData, Shape.of(n, cOut, lOut)));
    }

    /**
     * 1D im2col: unfold input [C_in, L] into column matrix [C_in * K, L_out]
     * 一维 im2col：将输入 [C_in, L] 展开为列矩阵 [C_in * K, L_out]
     *
     * <p>For each output position, extracts a patch of size K from each input channel
     * and places it as a column in the output matrix.</p>
     * <p>对于每个输出位置，从每个输入通道提取大小为 K 的块并放置为输出矩阵的一列。</p>
     *
     * @param inputData   flat input data array | 扁平输入数据数组
     * @param inputOffset offset into inputData for this sample | 此样本在 inputData 中的偏移量
     * @param cIn         number of input channels | 输入通道数
     * @param length      input sequence length | 输入序列长度
     * @param k           kernel size | 卷积核大小
     * @param stride      stride | 步幅
     * @param padding     zero-padding on both sides | 两侧的零填充
     * @param col         output column matrix [C_in * K, L_out] (pre-allocated) |
     *                    输出列矩阵 [C_in * K, L_out]（预分配）
     */
    private static void im2col1d(float[] inputData, int inputOffset,
                                  int cIn, int length, int k,
                                  int stride, int padding, float[] col) {
        // Use long to prevent overflow in 2*padding, then cast (validated by caller)
        int lOut = (int) (((long) length + 2L * padding - k) / stride + 1);

        // Zero-fill the col buffer (required for padding regions)
        java.util.Arrays.fill(col, 0.0f);

        for (int c = 0; c < cIn; c++) {
            int channelOffset = inputOffset + c * length;
            for (int ki = 0; ki < k; ki++) {
                int colRow = c * k + ki;
                int colRowBase = colRow * lOut;
                // Pre-compute valid output position range to avoid per-element bounds checks
                // inPos = outPos * stride - padding + ki; valid when 0 <= inPos < length
                // Use long arithmetic to prevent overflow with large padding/stride values
                int outPosStart = (int) Math.max(0L, ((long) padding - ki + stride - 1) / stride);
                int outPosEnd = (int) Math.min((long) lOut, ((long) length + padding - ki + stride - 1) / stride);
                for (int outPos = outPosStart; outPos < outPosEnd; outPos++) {
                    int inPos = outPos * stride - padding + ki;
                    col[colRowBase + outPos] = inputData[channelOffset + inPos];
                }
            }
        }
    }
}
