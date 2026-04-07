package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;

/**
 * Batch Normalization Operator (Inference Mode)
 * 批归一化算子（推理模式）
 *
 * <p>Applies batch normalization in inference mode over an input tensor of shape (N, C, H, W).
 * Uses pre-computed running mean and variance for normalization:</p>
 * <pre>y = (x - running_mean) / sqrt(running_var + epsilon) * scale + bias</pre>
 * <p>对形状为 (N, C, H, W) 的输入张量执行推理模式的批归一化。
 * 使用预计算的滑动均值和方差进行归一化：</p>
 * <pre>y = (x - running_mean) / sqrt(running_var + epsilon) * scale + bias</pre>
 *
 * <p><strong>Attributes | 属性:</strong></p>
 * <ul>
 *   <li>{@code epsilon} (float, default = 1e-5) — small constant for numerical stability |
 *       用于数值稳定性的小常数</li>
 * </ul>
 *
 * <p><strong>Inputs | 输入 (5 tensors):</strong></p>
 * <ul>
 *   <li>input — tensor of shape (N, C, H, W) | 输入张量</li>
 *   <li>scale — tensor of shape (C) | 缩放参数</li>
 *   <li>bias — tensor of shape (C) | 偏置参数</li>
 *   <li>running_mean — tensor of shape (C) | 滑动均值</li>
 *   <li>running_var — tensor of shape (C) | 滑动方差</li>
 * </ul>
 *
 * <p><strong>Outputs | 输出:</strong></p>
 * <ul>
 *   <li>output — tensor of shape (N, C, H, W) | 输出张量</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) — 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class BatchNormOp implements Op {

    /**
     * Execute batch normalization forward computation (inference mode)
     * 执行批归一化前向计算（推理模式）
     *
     * @param inputs 5 tensors: input(N,C,H,W), scale(C), bias(C), running_mean(C), running_var(C) |
     *               5个张量：输入、缩放、偏置、滑动均值、滑动方差
     * @param attrs  operator attributes: epsilon | 算子属性
     * @return single output tensor of shape (N, C, H, W) | 单个输出张量
     * @throws OpExecutionException if inputs are invalid | 输入无效时抛出
     */
    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        if (inputs == null || inputs.size() != 5) {
            throw new OpExecutionException(
                    "BatchNorm requires exactly 5 inputs (input, scale, bias, running_mean, running_var), got "
                            + (inputs == null ? 0 : inputs.size()),
                    "BatchNorm");
        }

        Tensor input = inputs.get(0);
        Tensor scale = inputs.get(1);
        Tensor bias = inputs.get(2);
        Tensor runningMean = inputs.get(3);
        Tensor runningVar = inputs.get(4);

        if (input == null || scale == null || bias == null || runningMean == null || runningVar == null) {
            throw new OpExecutionException("BatchNorm: all 5 input tensors must be non-null", "BatchNorm");
        }

        if (input.shape().rank() != 4) {
            throw new OpExecutionException(
                    "BatchNorm input must be 4D (N,C,H,W), got rank " + input.shape().rank(),
                    "BatchNorm");
        }

        int n = input.shape().dim(0);
        int c = input.shape().dim(1);
        int h = input.shape().dim(2);
        int w = input.shape().dim(3);

        // Validate per-channel parameter shapes
        validateChannelParam(scale, c, "scale");
        validateChannelParam(bias, c, "bias");
        validateChannelParam(runningMean, c, "running_mean");
        validateChannelParam(runningVar, c, "running_var");

        float epsilon = attrs.getFloat("epsilon", 1e-5f);
        if (epsilon <= 0) {
            throw new OpExecutionException("BatchNorm epsilon must be positive", "BatchNorm");
        }

        long outSize = (long) n * c * h * w;
        if (outSize > Integer.MAX_VALUE) {
            throw new OpExecutionException("output too large", "BatchNorm");
        }

        // Flatten input to contiguous array for direct indexed access (avoids per-pixel getFloat overhead)
        float[] inputData = input.toFloatArray();
        float[] meanData = runningMean.toFloatArray();
        float[] varData = runningVar.toFloatArray();
        float[] scaleData = scale.toFloatArray();
        float[] biasData = bias.toFloatArray();

        float[] outputData = new float[(int) outSize];
        int hw = h * w;
        int outIdx = 0;

        for (int bi = 0; bi < n; bi++) {
            for (int ci = 0; ci < c; ci++) {
                // Pre-compute: y = gamma / sqrt(var + eps) * (x - mean) + beta
                float invStd = 1.0f / (float) Math.sqrt(varData[ci] + epsilon);
                float scaleFactor = scaleData[ci] * invStd;
                float meanVal = meanData[ci];
                float betaVal = biasData[ci];

                int channelOffset = (bi * c + ci) * hw;
                for (int j = 0; j < hw; j++) {
                    outputData[outIdx++] = scaleFactor * (inputData[channelOffset + j] - meanVal) + betaVal;
                }
            }
        }

        return List.of(Tensor.wrap(outputData, Shape.of(n, c, h, w)));
    }

    /**
     * Validate that a per-channel parameter tensor has the correct size
     * 验证逐通道参数张量的大小正确
     */
    private static void validateChannelParam(Tensor param, int channels, String name) {
        if (param.shape().rank() != 1 || param.shape().dim(0) != channels) {
            throw new OpExecutionException(
                    "BatchNorm " + name + " must be 1D with size " + channels
                            + ", got shape " + param.shape(),
                    "BatchNorm");
        }
    }
}
