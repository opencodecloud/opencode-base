package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.internal.Activation;
import cloud.opencode.base.neural.internal.Blas;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Bidirectional LSTM (BiLSTM) Operator
 * 双向 LSTM（BiLSTM）算子
 *
 * <p>Implements a bidirectional LSTM by running a forward LSTM (t=0 to T-1) and a backward
 * LSTM (t=T-1 to 0) with separate weight sets, then concatenating their outputs along
 * the last dimension to produce a [N, T, 2*hidden] output.</p>
 * <p>通过分别运行前向 LSTM（t=0 到 T-1）和后向 LSTM（t=T-1 到 0）并使用独立的权重集，
 * 然后沿最后一个维度拼接它们的输出，生成 [N, T, 2*hidden] 的输出。</p>
 *
 * <p><strong>Inputs | 输入:</strong></p>
 * <ul>
 *   <li>input: Tensor [N, T, input_size] — input sequence | 输入序列</li>
 *   <li>fwd_weight_ih: Tensor [4*hidden, input_size] — forward input-to-hidden weights | 前向输入到隐藏权重</li>
 *   <li>fwd_weight_hh: Tensor [4*hidden, hidden] — forward hidden-to-hidden weights | 前向隐藏到隐藏权重</li>
 *   <li>fwd_bias: Tensor [4*hidden] — forward gate biases | 前向门偏置</li>
 *   <li>bwd_weight_ih: Tensor [4*hidden, input_size] — backward input-to-hidden weights | 后向输入到隐藏权重</li>
 *   <li>bwd_weight_hh: Tensor [4*hidden, hidden] — backward hidden-to-hidden weights | 后向隐藏到隐藏权重</li>
 *   <li>bwd_bias: Tensor [4*hidden] — backward gate biases | 后向门偏置</li>
 * </ul>
 *
 * <p><strong>Attributes | 属性:</strong></p>
 * <ul>
 *   <li>hidden_size (int): hidden state dimension | 隐藏状态维度</li>
 * </ul>
 *
 * <p><strong>Outputs | 输出:</strong></p>
 * <ul>
 *   <li>all_h: Tensor [N, T, 2*hidden] — concatenated forward and backward hidden states |
 *       拼接的前向和后向隐藏状态</li>
 * </ul>
 *
 * <p><strong>Algorithm | 算法:</strong></p>
 * <ol>
 *   <li>Forward LSTM: process t=0 to T-1 with forward weights | 前向 LSTM：用前向权重处理 t=0 到 T-1</li>
 *   <li>Backward LSTM: process t=T-1 to 0 with backward weights | 后向 LSTM：用后向权重处理 t=T-1 到 0</li>
 *   <li>Concatenate forward and backward outputs along last dimension |
 *       沿最后一个维度拼接前向和后向输出</li>
 * </ol>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Input validation: null, size, and shape checks - 输入验证：null、大小和形状检查</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see LstmOp
 * @see Activation#sigmoid(float[], int, int)
 * @see Activation#tanh(float[], int, int)
 * @see Blas#gemm(float, float[], int, int, boolean, float[], int, int, boolean, float, float[])
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class BiLstmOp implements Op {

    private static final String OP_NAME = "BiLSTM";

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.size() < 7) {
            throw new OpExecutionException(
                    "BiLstmOp requires 7 inputs [input, fwd_weight_ih, fwd_weight_hh, fwd_bias, "
                            + "bwd_weight_ih, bwd_weight_hh, bwd_bias], got " + inputs.size(),
                    OP_NAME);
        }

        Tensor input = inputs.get(0);
        Tensor fwdWeightIh = inputs.get(1);
        Tensor fwdWeightHh = inputs.get(2);
        Tensor fwdBias = inputs.get(3);
        Tensor bwdWeightIh = inputs.get(4);
        Tensor bwdWeightHh = inputs.get(5);
        Tensor bwdBias = inputs.get(6);

        int hiddenSize = attrs.getInt("hidden_size", -1);
        if (hiddenSize <= 0) {
            throw new OpExecutionException("hidden_size attribute must be positive", OP_NAME);
        }

        // Validate input shape [N, T, input_size]
        if (input.shape().rank() != 3) {
            throw new OpExecutionException(
                    "Input must be 3D [N, T, input_size], got rank " + input.shape().rank(), OP_NAME);
        }

        int batchSize = input.shape().dim(0);
        int seqLen = input.shape().dim(1);
        int inputSize = input.shape().dim(2);
        int H = hiddenSize;
        int gateSize = 4 * H;

        // Validate forward weight shapes
        validateWeights(fwdWeightIh, fwdWeightHh, fwdBias, gateSize, inputSize, H, "forward");
        // Validate backward weight shapes
        validateWeights(bwdWeightIh, bwdWeightHh, bwdBias, gateSize, inputSize, H, "backward");

        float[] inputData = input.toFloatArray();

        // Forward LSTM: t = 0 -> T-1
        float[] fwdAllH = runLstm(inputData, batchSize, seqLen, inputSize, H,
                fwdWeightIh.toFloatArray(), fwdWeightHh.toFloatArray(), fwdBias.toFloatArray(),
                false);

        // Backward LSTM: t = T-1 -> 0
        float[] bwdAllH = runLstm(inputData, batchSize, seqLen, inputSize, H,
                bwdWeightIh.toFloatArray(), bwdWeightHh.toFloatArray(), bwdBias.toFloatArray(),
                true);

        // Concatenate forward and backward along last dimension: [N, T, 2*H]
        long combinedSize = (long) batchSize * seqLen * 2 * H;
        if (combinedSize > Integer.MAX_VALUE) {
            throw new OpExecutionException("output too large", OP_NAME);
        }
        float[] combined = new float[(int) combinedSize];
        for (int n = 0; n < batchSize; n++) {
            for (int t = 0; t < seqLen; t++) {
                int fwdOffset = (n * seqLen + t) * H;
                int bwdOffset = (n * seqLen + t) * H;
                int outOffset = (n * seqLen + t) * 2 * H;
                System.arraycopy(fwdAllH, fwdOffset, combined, outOffset, H);
                System.arraycopy(bwdAllH, bwdOffset, combined, outOffset + H, H);
            }
        }

        return List.of(Tensor.wrap(combined, Shape.of(batchSize, seqLen, 2 * H)));
    }

    /**
     * Run a single-direction LSTM over the input sequence.
     * 在输入序列上运行单方向 LSTM。
     *
     * @param inputData flat input data [N, T, inputSize] | 展平的输入数据
     * @param batchSize batch size N | 批大小
     * @param seqLen    sequence length T | 序列长度
     * @param inputSize input feature size | 输入特征大小
     * @param H         hidden size | 隐藏层大小
     * @param wIh       input-to-hidden weights [4H, inputSize] | 输入到隐藏权重
     * @param wHh       hidden-to-hidden weights [4H, H] | 隐藏到隐藏权重
     * @param biasData  gate biases [4H] | 门偏置
     * @param reverse   if true, process t=T-1 to 0 | 如果为 true，从 t=T-1 到 0 处理
     * @return all hidden states [N, T, H] (in original time order) | 所有隐藏状态（按原始时间顺序）
     */
    private float[] runLstm(float[] inputData, int batchSize, int seqLen, int inputSize,
                            int H, float[] wIh, float[] wHh, float[] biasData, boolean reverse) {
        int gateSize = 4 * H;
        long allHSize = (long) batchSize * seqLen * H;
        if (allHSize > Integer.MAX_VALUE) {
            throw new OpExecutionException("output too large", OP_NAME);
        }
        float[] allH = new float[(int) allHSize];

        for (int n = 0; n < batchSize; n++) {
            float[] h = new float[H];
            float[] c = new float[H];

            for (int step = 0; step < seqLen; step++) {
                int t = reverse ? (seqLen - 1 - step) : step;

                // Extract x_t [inputSize]
                float[] xt = new float[inputSize];
                int inputOffset = (n * seqLen + t) * inputSize;
                System.arraycopy(inputData, inputOffset, xt, 0, inputSize);

                // gates = weight_ih @ x_t + weight_hh @ h + bias
                float[] gatesIh = new float[gateSize];
                Blas.gemm(1.0f, wIh, gateSize, inputSize, false,
                        xt, inputSize, 1, false,
                        0.0f, gatesIh);

                float[] gatesHh = new float[gateSize];
                Blas.gemm(1.0f, wHh, gateSize, H, false,
                        h, H, 1, false,
                        0.0f, gatesHh);

                float[] gates = new float[gateSize];
                for (int i = 0; i < gateSize; i++) {
                    gates[i] = gatesIh[i] + gatesHh[i] + biasData[i];
                }

                // Split and activate
                Activation.sigmoid(gates, 0, H);       // i gate
                Activation.sigmoid(gates, H, H);        // f gate
                Activation.tanh(gates, 2 * H, H);       // g (cell candidate)
                Activation.sigmoid(gates, 3 * H, H);    // o gate

                // c_t = f * c_{t-1} + i * g
                for (int i = 0; i < H; i++) {
                    c[i] = gates[H + i] * c[i] + gates[i] * gates[2 * H + i];
                }

                // h_t = o * tanh(c_t)
                float[] tanhC = Arrays.copyOf(c, H);
                Activation.tanh(tanhC, 0, H);
                for (int i = 0; i < H; i++) {
                    h[i] = gates[3 * H + i] * tanhC[i];
                }

                // Store in original time order
                System.arraycopy(h, 0, allH, (n * seqLen + t) * H, H);
            }
        }

        return allH;
    }

    /**
     * Validate weight and bias tensor shapes.
     * 验证权重和偏置张量的形状。
     */
    private void validateWeights(Tensor weightIh, Tensor weightHh, Tensor bias,
                                 int gateSize, int inputSize, int H, String direction) {
        if (weightIh.shape().rank() != 2 || weightIh.shape().dim(0) != gateSize
                || weightIh.shape().dim(1) != inputSize) {
            throw new OpExecutionException(
                    direction + " weight_ih must be [" + gateSize + ", " + inputSize + "], got "
                            + weightIh.shape(), OP_NAME);
        }
        if (weightHh.shape().rank() != 2 || weightHh.shape().dim(0) != gateSize
                || weightHh.shape().dim(1) != H) {
            throw new OpExecutionException(
                    direction + " weight_hh must be [" + gateSize + ", " + H + "], got "
                            + weightHh.shape(), OP_NAME);
        }
        if (bias.size() != gateSize) {
            throw new OpExecutionException(
                    direction + " bias must have " + gateSize + " elements, got " + bias.size(),
                    OP_NAME);
        }
    }
}
