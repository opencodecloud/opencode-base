package cloud.opencode.base.neural.internal;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.NeuralException;

import java.util.Objects;

/**
 * Im2Col Column Unfolding
 * Im2Col 列展开
 *
 * <p>Implements the im2col (image to column) transformation that unfolds an input
 * tensor [C, H, W] into a column matrix [C*kH*kW, outH*outW] for efficient
 * convolution via matrix multiplication (GEMM).</p>
 * <p>实现 im2col（图像到列）变换，将输入张量 [C, H, W] 展开为列矩阵
 * [C*kH*kW, outH*outW]，以便通过矩阵乘法（GEMM）高效执行卷积。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unfolds input patches into columns for GEMM-based convolution - 将输入块展开为列用于基于GEMM的卷积</li>
 *   <li>Supports arbitrary stride and zero-padding - 支持任意步幅和零填充</li>
 *   <li>Zero-fill for out-of-bounds (padding) regions - 越界（填充）区域零填充</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time: O(C * kH * kW * outH * outW) - 时间: O(C * kH * kW * outH * outW)</li>
 *   <li>Sequential memory access pattern for output - 输出的顺序内存访问模式</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class Im2Col {

    private Im2Col() {}

    /**
     * Unfold input [C, H, W] to column matrix [C*kH*kW, outH*outW] for one sample.
     * 将输入 [C, H, W] 展开为一个样本的列矩阵 [C*kH*kW, outH*outW]。
     *
     * <p>The output matrix is stored in row-major order: each row corresponds to one
     * element in the kernel across all channels, and each column corresponds to one
     * spatial output position.</p>
     * <p>输出矩阵以行主序存储：每行对应所有通道中卷积核的一个元素，
     * 每列对应一个空间输出位置。</p>
     *
     * @param input    input tensor data in CHW layout | CHW 布局的输入张量数据
     * @param channels number of input channels (C) | 输入通道数 (C)
     * @param height   input height (H) | 输入高度 (H)
     * @param width    input width (W) | 输入宽度 (W)
     * @param kH       kernel height | 卷积核高度
     * @param kW       kernel width | 卷积核宽度
     * @param strideH  vertical stride | 垂直步幅
     * @param strideW  horizontal stride | 水平步幅
     * @param padH     vertical padding | 垂直填充
     * @param padW     horizontal padding | 水平填充
     * @param output   output column matrix of size [C*kH*kW, outH*outW] in row-major order | 行主序输出列矩阵，大小 [C*kH*kW, outH*outW]
     * @throws NullPointerException if input or output is null | 如果 input 或 output 为 null
     * @throws NeuralException      if dimensions are invalid | 如果维度无效
     */
    public static void im2col(float[] input, int channels, int height, int width,
                               int kH, int kW, int strideH, int strideW,
                               int padH, int padW, float[] output) {
        Objects.requireNonNull(input, "input must not be null");
        Objects.requireNonNull(output, "output must not be null");
        validateParameters(channels, height, width, kH, kW, strideH, strideW, padH, padW);

        int outH = outputHeight(height, kH, strideH, padH);
        int outW = outputWidth(width, kW, strideW, padW);

        if (input.length < (long) channels * height * width) {
            throw new NeuralException(
                    "input length " + input.length + " < C*H*W " + (channels * height * width),
                    NeuralErrorCode.INVALID_PARAMETERS);
        }

        int outCols = outH * outW;
        long requiredOutput = (long) channels * kH * kW * outCols;
        if (output.length < requiredOutput) {
            throw new NeuralException(
                    "output length " + output.length + " < required " + requiredOutput,
                    NeuralErrorCode.INVALID_PARAMETERS);
        }

        int outRow = 0;
        for (int c = 0; c < channels; c++) {
            int channelOffset = c * height * width;
            for (int ky = 0; ky < kH; ky++) {
                for (int kx = 0; kx < kW; kx++) {
                    int outIdx = outRow * outCols;
                    for (int oh = 0; oh < outH; oh++) {
                        int iy = oh * strideH - padH + ky;
                        for (int ow = 0; ow < outW; ow++) {
                            int ix = ow * strideW - padW + kx;
                            if (iy >= 0 && iy < height && ix >= 0 && ix < width) {
                                output[outIdx] = input[channelOffset + iy * width + ix];
                            } else {
                                output[outIdx] = 0.0f;
                            }
                            outIdx++;
                        }
                    }
                    outRow++;
                }
            }
        }
    }

    /**
     * Compute the output height for a convolution.
     * 计算卷积的输出高度。
     *
     * <p>Formula: (height + 2*padH - kH) / strideH + 1</p>
     * <p>公式: (height + 2*padH - kH) / strideH + 1</p>
     *
     * @param height  input height | 输入高度
     * @param kH      kernel height | 卷积核高度
     * @param strideH vertical stride | 垂直步幅
     * @param padH    vertical padding | 垂直填充
     * @return output height | 输出高度
     * @throws NeuralException if parameters result in non-positive output | 如果参数导致非正输出
     */
    public static int outputHeight(int height, int kH, int strideH, int padH) {
        if (strideH <= 0) {
            throw new NeuralException("strideH must be > 0, got: " + strideH,
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
        int out = (height + 2 * padH - kH) / strideH + 1;
        if (out <= 0) {
            throw new NeuralException(
                    "Output height is non-positive: " + out
                            + " (height=" + height + ", kH=" + kH
                            + ", strideH=" + strideH + ", padH=" + padH + ")",
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
        return out;
    }

    /**
     * Compute the output width for a convolution.
     * 计算卷积的输出宽度。
     *
     * <p>Formula: (width + 2*padW - kW) / strideW + 1</p>
     * <p>公式: (width + 2*padW - kW) / strideW + 1</p>
     *
     * @param width   input width | 输入宽度
     * @param kW      kernel width | 卷积核宽度
     * @param strideW horizontal stride | 水平步幅
     * @param padW    horizontal padding | 水平填充
     * @return output width | 输出宽度
     * @throws NeuralException if parameters result in non-positive output | 如果参数导致非正输出
     */
    public static int outputWidth(int width, int kW, int strideW, int padW) {
        if (strideW <= 0) {
            throw new NeuralException("strideW must be > 0, got: " + strideW,
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
        int out = (width + 2 * padW - kW) / strideW + 1;
        if (out <= 0) {
            throw new NeuralException(
                    "Output width is non-positive: " + out
                            + " (width=" + width + ", kW=" + kW
                            + ", strideW=" + strideW + ", padW=" + padW + ")",
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
        return out;
    }

    /**
     * Validate im2col parameters.
     * 验证 im2col 参数。
     */
    private static void validateParameters(int channels, int height, int width,
                                            int kH, int kW, int strideH, int strideW,
                                            int padH, int padW) {
        if (channels <= 0) {
            throw new NeuralException("channels must be > 0, got: " + channels,
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
        if (height <= 0 || width <= 0) {
            throw new NeuralException("height and width must be > 0, got: height=" + height + ", width=" + width,
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
        if (kH <= 0 || kW <= 0) {
            throw new NeuralException("kernel dimensions must be > 0, got: kH=" + kH + ", kW=" + kW,
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
        if (strideH <= 0 || strideW <= 0) {
            throw new NeuralException("strides must be > 0, got: strideH=" + strideH + ", strideW=" + strideW,
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
        if (padH < 0 || padW < 0) {
            throw new NeuralException("padding must be >= 0, got: padH=" + padH + ", padW=" + padW,
                    NeuralErrorCode.INVALID_PARAMETERS);
        }
    }
}
