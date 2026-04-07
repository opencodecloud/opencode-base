package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.internal.Blas;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;

/**
 * Linear (Fully Connected) Operator
 * 线性（全连接）算子
 *
 * <p>Applies a linear transformation: output = input @ weight^T + bias.
 * Uses {@link Blas#gemm} with transB=true for efficient matrix multiplication.</p>
 * <p>执行线性变换：output = input @ weight^T + bias。
 * 使用 {@link Blas#gemm} 并设置 transB=true 以高效矩阵乘法。</p>
 *
 * <p><strong>Inputs | 输入 (2 or 3 tensors):</strong></p>
 * <ul>
 *   <li>input — tensor of shape (N, in_features) | 输入张量</li>
 *   <li>weight — tensor of shape (out_features, in_features) | 权重张量</li>
 *   <li>bias (optional) — tensor of shape (out_features) | 偏置张量（可选）</li>
 * </ul>
 *
 * <p><strong>Outputs | 输出:</strong></p>
 * <ul>
 *   <li>output — tensor of shape (N, out_features) | 输出张量</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) — 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Blas
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class LinearOp implements Op {

    /**
     * Execute linear (fully connected) forward computation
     * 执行线性（全连接）前向计算
     *
     * @param inputs 2 or 3 tensors: input(N,in_features), weight(out_features,in_features),
     *               optional bias(out_features) | 2或3个张量
     * @param attrs  operator attributes (unused) | 算子属性（未使用）
     * @return single output tensor of shape (N, out_features) | 单个输出张量
     * @throws OpExecutionException if inputs are invalid | 输入无效时抛出
     */
    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        if (inputs == null || inputs.size() < 2 || inputs.size() > 3) {
            throw new OpExecutionException(
                    "Linear requires 2 or 3 inputs (input, weight, [bias]), got "
                            + (inputs == null ? 0 : inputs.size()),
                    "Linear");
        }

        Tensor input = inputs.get(0);
        Tensor weight = inputs.get(1);
        Tensor bias = inputs.size() == 3 ? inputs.get(2) : null;

        if (input.shape().rank() != 2) {
            throw new OpExecutionException(
                    "Linear input must be 2D (N, in_features), got rank " + input.shape().rank(),
                    "Linear");
        }
        if (weight.shape().rank() != 2) {
            throw new OpExecutionException(
                    "Linear weight must be 2D (out_features, in_features), got rank " + weight.shape().rank(),
                    "Linear");
        }

        int n = input.shape().dim(0);
        int inFeatures = input.shape().dim(1);
        int outFeatures = weight.shape().dim(0);
        int weightIn = weight.shape().dim(1);

        if (inFeatures != weightIn) {
            throw new OpExecutionException(
                    "Linear input features " + inFeatures + " != weight in_features " + weightIn,
                    "Linear");
        }

        if (bias != null) {
            if (bias.shape().rank() != 1 || bias.shape().dim(0) != outFeatures) {
                throw new OpExecutionException(
                        "Linear bias must be 1D with size " + outFeatures + ", got shape " + bias.shape(),
                        "Linear");
            }
        }

        // output = input @ weight^T using Blas.gemm with transB=true
        // input: [N x inFeatures], weight: [outFeatures x inFeatures]
        // result: [N x outFeatures]
        float[] inputData = input.toFloatArray();
        float[] weightData = weight.toFloatArray();
        float[] outputData = new float[n * outFeatures];

        Blas.gemm(1.0f,
                inputData, n, inFeatures, false,
                weightData, outFeatures, inFeatures, true,
                0.0f, outputData);

        // Add bias if present — extract to flat array to avoid per-element getFloat() overhead
        if (bias != null) {
            float[] biasData = bias.toFloatArray();
            for (int bi = 0; bi < n; bi++) {
                int rowOffset = bi * outFeatures;
                for (int j = 0; j < outFeatures; j++) {
                    outputData[rowOffset + j] += biasData[j];
                }
            }
        }

        return List.of(Tensor.wrap(outputData, Shape.of(n, outFeatures)));
    }
}
