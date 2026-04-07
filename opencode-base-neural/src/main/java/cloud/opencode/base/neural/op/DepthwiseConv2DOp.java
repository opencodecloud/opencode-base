package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.internal.Im2Col;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;

/**
 * Depthwise 2D Convolution Operator
 * 深度可分离二维卷积算子
 *
 * <p>Implements depthwise 2D convolution where each input channel is convolved
 * independently with its own filter (groups = C_in). Uses a direct sliding-window
 * approach without im2col for efficiency on single-channel convolutions.</p>
 * <p>实现深度可分离二维卷积，每个输入通道使用独立的滤波器单独卷积
 * （groups = C_in）。对单通道卷积使用直接滑窗方法以提高效率，无需 im2col。</p>
 *
 * <p><strong>Inputs | 输入:</strong></p>
 * <ul>
 *   <li>inputs[0]: input tensor [N, C_in, H, W] - 输入张量 [N, C_in, H, W]</li>
 *   <li>inputs[1]: weight tensor [C_in, 1, kH, kW] - 权重张量 [C_in, 1, kH, kW]</li>
 *   <li>inputs[2]: (optional) bias tensor [C_in] - （可选）偏置张量 [C_in]</li>
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
 *   <li>output[0]: result tensor [N, C_in, outH, outW] - 结果张量 [N, C_in, outH, outW]</li>
 * </ul>
 *
 * <p><strong>Algorithm | 算法:</strong></p>
 * <p>For each sample and each channel, perform direct sliding-window convolution:
 * iterate over output positions, accumulate kernel-weighted sums from input pixels.</p>
 * <p>对每个样本和每个通道，执行直接滑窗卷积：
 * 遍历输出位置，从输入像素累积核加权和。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Op
 * @see Conv2DOp
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class DepthwiseConv2DOp implements Op {

    private static final String OP_TYPE = "DepthwiseConv2D";

    /**
     * Execute depthwise 2D convolution forward computation
     * 执行深度可分离二维卷积前向计算
     *
     * @param inputs ordered input tensors: [input, weight, optional bias] | 有序输入张量: [输入, 权重, 可选偏置]
     * @param attrs  operator attributes (stride, padding) | 算子属性（步幅、填充）
     * @return single-element list containing the output tensor [N, C_in, outH, outW] |
     *         包含输出张量 [N, C_in, outH, outW] 的单元素列表
     * @throws OpExecutionException if inputs are null, insufficient, or dimensions are invalid |
     *                              如果输入为 null、不足或维度无效
     */
    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        if (inputs == null || inputs.size() < 2) {
            throw new OpExecutionException(
                    "DepthwiseConv2D requires at least 2 inputs (input, weight), got: "
                            + (inputs == null ? "null" : inputs.size()), OP_TYPE);
        }

        Tensor input = inputs.get(0);
        Tensor weight = inputs.get(1);
        Tensor bias = inputs.size() > 2 ? inputs.get(2) : null;

        if (input == null || weight == null) {
            throw new OpExecutionException(
                    "DepthwiseConv2D input and weight must not be null", OP_TYPE);
        }

        // Validate ranks
        if (input.shape().rank() != 4) {
            throw new OpExecutionException(
                    "DepthwiseConv2D input must be 4D [N,C,H,W], got rank "
                            + input.shape().rank(), OP_TYPE);
        }
        if (weight.shape().rank() != 4) {
            throw new OpExecutionException(
                    "DepthwiseConv2D weight must be 4D [C_in,1,kH,kW], got rank "
                            + weight.shape().rank(), OP_TYPE);
        }

        int stride = attrs.getInt("stride", 1);
        int padding = attrs.getInt("padding", 0);

        int n = input.shape().dim(0);
        int cIn = input.shape().dim(1);
        int h = input.shape().dim(2);
        int w = input.shape().dim(3);

        int wCin = weight.shape().dim(0);
        int wDepth = weight.shape().dim(1);
        int kH = weight.shape().dim(2);
        int kW = weight.shape().dim(3);

        // Validate depthwise constraint: weight shape must be [C_in, 1, kH, kW]
        if (wCin != cIn) {
            throw new OpExecutionException(
                    "DepthwiseConv2D: weight dim(0)=" + wCin + " must equal C_in=" + cIn, OP_TYPE);
        }
        if (wDepth != 1) {
            throw new OpExecutionException(
                    "DepthwiseConv2D: weight dim(1) must be 1 for depthwise conv, got " + wDepth, OP_TYPE);
        }

        // Validate bias shape
        if (bias != null && bias.size() != cIn) {
            throw new OpExecutionException(
                    "DepthwiseConv2D: bias size " + bias.size() + " != C_in " + cIn, OP_TYPE);
        }

        int outH = Im2Col.outputHeight(h, kH, stride, padding);
        int outW = Im2Col.outputWidth(w, kW, stride, padding);

        // Validate stride × output dimension fits in int to prevent index overflow in inner loops
        if ((long) outH * stride > Integer.MAX_VALUE || (long) outW * stride > Integer.MAX_VALUE) {
            throw new OpExecutionException(
                    "DepthwiseConv2D: stride * output dimension overflows int", OP_TYPE);
        }

        float[] inputData = input.toFloatArray();
        float[] weightData = weight.toFloatArray();
        float[] biasData = bias != null ? bias.toFloatArray() : null;

        long outSize = (long) n * cIn * outH * outW;
        if (outSize > Integer.MAX_VALUE) {
            throw new OpExecutionException("output too large", OP_TYPE);
        }
        float[] outputData = new float[(int) outSize];

        int channelInputSize = h * w;
        int channelOutputSize = outH * outW;
        int kernelSize = kH * kW;

        for (int sample = 0; sample < n; sample++) {
            int sampleInputBase = sample * cIn * channelInputSize;
            int sampleOutputBase = sample * cIn * channelOutputSize;

            for (int c = 0; c < cIn; c++) {
                int channelInputBase = sampleInputBase + c * channelInputSize;
                int channelOutputBase = sampleOutputBase + c * channelOutputSize;
                int kernelBase = c * kernelSize;

                // Direct sliding window convolution with pre-computed valid kernel bounds
                for (int oh = 0; oh < outH; oh++) {
                    int hBase = oh * stride - padding;
                    int kyMin = Math.max(0, -hBase);
                    int kyMax = Math.min(kH, h - hBase);
                    for (int ow = 0; ow < outW; ow++) {
                        float sum = 0.0f;
                        int wBase = ow * stride - padding;
                        int kxMin = Math.max(0, -wBase);
                        int kxMax = Math.min(kW, w - wBase);
                        for (int ky = kyMin; ky < kyMax; ky++) {
                            int rowOffset = channelInputBase + (hBase + ky) * w;
                            int wRowOffset = kernelBase + ky * kW;
                            for (int kx = kxMin; kx < kxMax; kx++) {
                                sum += inputData[rowOffset + wBase + kx]
                                        * weightData[wRowOffset + kx];
                            }
                        }
                        // Add bias if present
                        if (biasData != null) {
                            sum += biasData[c];
                        }
                        outputData[channelOutputBase + oh * outW + ow] = sum;
                    }
                }
            }
        }

        return List.of(Tensor.wrap(outputData, Shape.of(n, cIn, outH, outW)));
    }
}
