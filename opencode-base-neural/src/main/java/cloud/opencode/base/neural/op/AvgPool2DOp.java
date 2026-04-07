package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;

/**
 * 2D Average Pooling Operator
 * 二维平均池化算子
 *
 * <p>Applies a sliding-window average pooling over an input tensor of shape (N, C, H, W).
 * For each window, the average value is computed. Supports global average pooling when
 * kernel_size equals the spatial dimensions (H, W) of the input.</p>
 * <p>对形状为 (N, C, H, W) 的输入张量执行滑动窗口平均池化。
 * 对每个窗口计算平均值。当 kernel_size 等于输入的空间维度 (H, W) 时，支持全局平均池化。</p>
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
 * @see MaxPool2DOp
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class AvgPool2DOp implements Op {

    /**
     * Execute 2D average pooling forward computation
     * 执行二维平均池化前向计算
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
                    "AvgPool2D requires exactly 1 input, got " + (inputs == null ? 0 : inputs.size()),
                    "AvgPool2D");
        }
        Tensor input = inputs.getFirst();
        if (input.shape().rank() != 4) {
            throw new OpExecutionException(
                    "AvgPool2D input must be 4D (N,C,H,W), got rank " + input.shape().rank(),
                    "AvgPool2D");
        }

        int kernelSize = attrs.getInt("kernel_size", -1);
        if (kernelSize <= 0) {
            throw new OpExecutionException(
                    "AvgPool2D requires positive kernel_size attribute", "AvgPool2D");
        }
        int stride = attrs.getInt("stride", kernelSize);
        int padding = attrs.getInt("padding", 0);

        if (stride <= 0) {
            throw new OpExecutionException("AvgPool2D stride must be positive", "AvgPool2D");
        }
        if (padding < 0) {
            throw new OpExecutionException("AvgPool2D padding must be non-negative", "AvgPool2D");
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
                    "AvgPool2D output dimensions must be positive: outH=" + outHL + ", outW=" + outWL,
                    "AvgPool2D");
        }
        int outH = (int) outHL;
        int outW = (int) outWL;

        long outSize = (long) n * c * outH * outW;
        if (outSize > Integer.MAX_VALUE) {
            throw new OpExecutionException("output too large", "AvgPool2D");
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
                        float sum = 0.0f;
                        int count = 0;
                        int hStart = oh * stride - padding;
                        int wStart = ow * stride - padding;

                        for (int kh = 0; kh < kernelSize; kh++) {
                            int ih = hStart + kh;
                            if (ih < 0 || ih >= h) continue;
                            int rowOffset = channelOffset + ih * w;
                            for (int kw = 0; kw < kernelSize; kw++) {
                                int iw = wStart + kw;
                                if (iw >= 0 && iw < w) {
                                    sum += inputData[rowOffset + iw];
                                    count++;
                                }
                            }
                        }
                        // Average over the valid (non-padded) elements in the window
                        outputData[outIdx++] = (count > 0) ? sum / count : 0.0f;
                    }
                }
            }
        }

        return List.of(Tensor.wrap(outputData, Shape.of(n, c, outH, outW)));
    }
}
