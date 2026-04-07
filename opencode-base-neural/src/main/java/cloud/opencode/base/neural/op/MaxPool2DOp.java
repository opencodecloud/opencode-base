package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;

/**
 * 2D Max Pooling Operator
 * 二维最大池化算子
 *
 * <p>Applies a sliding-window max pooling over an input tensor of shape (N, C, H, W).
 * For each window, the maximum value is selected. This reduces spatial dimensions
 * while preserving the most salient features.</p>
 * <p>对形状为 (N, C, H, W) 的输入张量执行滑动窗口最大池化。
 * 对每个窗口选取最大值。在保留最显著特征的同时减小空间维度。</p>
 *
 * <p><strong>Attributes | 属性:</strong></p>
 * <ul>
 *   <li>{@code kernel_size} (int, required) — pooling window size | 池化窗口大小</li>
 *   <li>{@code stride} (int, default = kernel_size) — sliding stride | 滑动步幅</li>
 *   <li>{@code padding} (int, default = 0) — zero-padding on each side | 每侧零填充</li>
 * </ul>
 *
 * <p><strong>Inputs | 输入:</strong></p>
 * <ul>
 *   <li>input — tensor of shape (N, C, H, W) | 形状为 (N, C, H, W) 的张量</li>
 * </ul>
 *
 * <p><strong>Outputs | 输出:</strong></p>
 * <ul>
 *   <li>output — tensor of shape (N, C, outH, outW) | 形状为 (N, C, outH, outW) 的张量</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) — 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see AvgPool2DOp
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class MaxPool2DOp implements Op {

    /**
     * Execute 2D max pooling forward computation
     * 执行二维最大池化前向计算
     *
     * @param inputs single input tensor of shape (N, C, H, W) | 单个形状为 (N, C, H, W) 的输入张量
     * @param attrs  operator attributes: kernel_size, stride, padding | 算子属性
     * @return single output tensor of shape (N, C, outH, outW) | 单个输出张量
     * @throws OpExecutionException if input shape or attributes are invalid | 输入形状或属性无效时抛出
     */
    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        if (inputs == null || inputs.size() != 1) {
            throw new OpExecutionException(
                    "MaxPool2D requires exactly 1 input, got " + (inputs == null ? 0 : inputs.size()),
                    "MaxPool2D");
        }
        Tensor input = inputs.getFirst();
        if (input.shape().rank() != 4) {
            throw new OpExecutionException(
                    "MaxPool2D input must be 4D (N,C,H,W), got rank " + input.shape().rank(),
                    "MaxPool2D");
        }

        int kernelSize = attrs.getInt("kernel_size", -1);
        if (kernelSize <= 0) {
            throw new OpExecutionException(
                    "MaxPool2D requires positive kernel_size attribute", "MaxPool2D");
        }
        int stride = attrs.getInt("stride", kernelSize);
        int padding = attrs.getInt("padding", 0);

        if (stride <= 0) {
            throw new OpExecutionException("MaxPool2D stride must be positive", "MaxPool2D");
        }
        if (padding < 0) {
            throw new OpExecutionException("MaxPool2D padding must be non-negative", "MaxPool2D");
        }

        int n = input.shape().dim(0);
        int c = input.shape().dim(1);
        int h = input.shape().dim(2);
        int w = input.shape().dim(3);

        // Use long to prevent overflow in 2*padding
        long outHL = ((long) h + 2L * padding - kernelSize) / stride + 1;
        long outWL = ((long) w + 2L * padding - kernelSize) / stride + 1;

        if (outHL <= 0 || outWL <= 0 || outHL > Integer.MAX_VALUE || outWL > Integer.MAX_VALUE) {
            throw new OpExecutionException(
                    "MaxPool2D output dimensions must be positive: outH=" + outHL + ", outW=" + outWL,
                    "MaxPool2D");
        }
        int outH = (int) outHL;
        int outW = (int) outWL;

        long outSize = (long) n * c * outH * outW;
        if (outSize > Integer.MAX_VALUE) {
            throw new OpExecutionException("output too large", "MaxPool2D");
        }

        // Flatten input to contiguous array for direct indexed access
        float[] inputData = input.toFloatArray();
        float[] outputData = new float[(int) outSize];
        int outIdx = 0;

        for (int bi = 0; bi < n; bi++) {
            for (int ci = 0; ci < c; ci++) {
                int channelOffset = (int) (((long) bi * c + ci) * h * w);
                for (int oh = 0; oh < outH; oh++) {
                    for (int ow = 0; ow < outW; ow++) {
                        float maxVal = Float.NEGATIVE_INFINITY;
                        int hStart = oh * stride - padding;
                        int wStart = ow * stride - padding;

                        for (int kh = 0; kh < kernelSize; kh++) {
                            int ih = hStart + kh;
                            if (ih < 0 || ih >= h) continue;
                            int rowOffset = channelOffset + ih * w;
                            for (int kw = 0; kw < kernelSize; kw++) {
                                int iw = wStart + kw;
                                if (iw >= 0 && iw < w) {
                                    float val = inputData[rowOffset + iw];
                                    if (val > maxVal) {
                                        maxVal = val;
                                    }
                                }
                            }
                        }
                        // If all positions were out of bounds (padding only), use 0
                        outputData[outIdx++] = (maxVal == Float.NEGATIVE_INFINITY) ? 0.0f : maxVal;
                    }
                }
            }
        }

        return List.of(Tensor.wrap(outputData, Shape.of(n, c, outH, outW)));
    }
}
