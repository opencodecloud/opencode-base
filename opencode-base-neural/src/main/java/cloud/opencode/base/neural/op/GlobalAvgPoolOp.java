package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;

/**
 * Global Average Pooling Operator
 * 全局平均池化算子
 *
 * <p>Applies global average pooling over the spatial dimensions (H, W) of an
 * input tensor of shape (N, C, H, W), producing output of shape (N, C, 1, 1).
 * For each (n, c) pair, the output is the arithmetic mean of all H*W values.</p>
 * <p>对形状为 (N, C, H, W) 的输入张量在空间维度 (H, W) 上执行全局平均池化，
 * 产出形状为 (N, C, 1, 1) 的输出。对于每个 (n, c) 对，输出为所有 H*W 值的算术平均。</p>
 *
 * <p><strong>Inputs | 输入:</strong></p>
 * <ul>
 *   <li>input — tensor of shape (N, C, H, W) | 形状为 (N, C, H, W) 的张量</li>
 * </ul>
 *
 * <p><strong>Outputs | 输出:</strong></p>
 * <ul>
 *   <li>output — tensor of shape (N, C, 1, 1) | 形状为 (N, C, 1, 1) 的张量</li>
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
public final class GlobalAvgPoolOp implements Op {

    /**
     * Execute global average pooling forward computation.
     * 执行全局平均池化前向计算。
     *
     * @param inputs single input tensor of shape (N, C, H, W) | 单个形状为 (N, C, H, W) 的输入张量
     * @param attrs  operator attributes (unused) | 算子属性（未使用）
     * @return single output tensor of shape (N, C, 1, 1) | 单个形状为 (N, C, 1, 1) 的输出张量
     * @throws OpExecutionException if input is null, wrong count, or wrong rank | 输入为 null、数量错误或秩错误时抛出
     */
    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        if (inputs == null || inputs.size() != 1) {
            throw new OpExecutionException(
                    "GlobalAvgPool requires exactly 1 input, got " + (inputs == null ? 0 : inputs.size()),
                    "GlobalAvgPool");
        }
        Tensor input = inputs.getFirst();
        if (input.shape().rank() != 4) {
            throw new OpExecutionException(
                    "GlobalAvgPool input must be 4D (N,C,H,W), got rank " + input.shape().rank(),
                    "GlobalAvgPool");
        }

        int n = input.shape().dim(0);
        int c = input.shape().dim(1);
        int h = input.shape().dim(2);
        int w = input.shape().dim(3);
        int spatialSize = h * w;
        if (spatialSize <= 0) {
            throw new OpExecutionException(
                    "GlobalAvgPool spatial dimensions must be positive, got H=" + h + " W=" + w,
                    "GlobalAvgPool");
        }

        // Flatten input to contiguous array for direct indexed access
        float[] inputData = input.toFloatArray();
        float[] outputData = new float[n * c];
        float invSpatialSize = 1.0f / spatialSize;
        int outIdx = 0;

        for (int bi = 0; bi < n; bi++) {
            for (int ci = 0; ci < c; ci++) {
                int channelOffset = (bi * c + ci) * spatialSize;
                float sum = 0.0f;
                for (int j = 0; j < spatialSize; j++) {
                    sum += inputData[channelOffset + j];
                }
                outputData[outIdx++] = sum * invSpatialSize;
            }
        }

        return List.of(Tensor.wrap(outputData, Shape.of(n, c, 1, 1)));
    }
}
