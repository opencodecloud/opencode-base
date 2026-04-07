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
 * LSTM (Long Short-Term Memory) Operator
 * LSTM（长短期记忆）算子
 *
 * <p>Implements a single-layer unidirectional LSTM with fused gates. The four gates
 * (input, forget, cell candidate, output) are computed via two matrix multiplications
 * per time step using {@link Blas#gemm}, then split and activated via
 * {@link Activation#sigmoid} and {@link Activation#tanh}.</p>
 * <p>实现单层单向 LSTM，采用融合门计算。每个时间步通过 {@link Blas#gemm}
 * 进行两次矩阵乘法计算四个门（输入门、遗忘门、候选细胞、输出门），
 * 然后拆分并通过 {@link Activation#sigmoid} 和 {@link Activation#tanh} 激活。</p>
 *
 * <p><strong>Inputs | 输入:</strong></p>
 * <ul>
 *   <li>input: Tensor [N, T, input_size] — input sequence | 输入序列</li>
 *   <li>weight_ih: Tensor [4*hidden, input_size] — input-to-hidden weights for fused gates [i,f,g,o] |
 *       输入到隐藏的权重，融合门 [i,f,g,o]</li>
 *   <li>weight_hh: Tensor [4*hidden, hidden] — hidden-to-hidden weights for fused gates |
 *       隐藏到隐藏的权重，融合门</li>
 *   <li>bias: Tensor [4*hidden] — biases for fused gates | 融合门偏置</li>
 * </ul>
 *
 * <p><strong>Attributes | 属性:</strong></p>
 * <ul>
 *   <li>hidden_size (int): hidden state dimension | 隐藏状态维度</li>
 * </ul>
 *
 * <p><strong>Outputs | 输出:</strong></p>
 * <ul>
 *   <li>all_h: Tensor [N, T, hidden] — hidden states for all time steps | 所有时间步的隐藏状态</li>
 *   <li>last_h: Tensor [N, hidden] — last hidden state | 最后一个隐藏状态</li>
 *   <li>last_c: Tensor [N, hidden] — last cell state | 最后一个细胞状态</li>
 * </ul>
 *
 * <p><strong>Algorithm | 算法:</strong></p>
 * <pre>{@code
 * For t = 0..T-1:
 *   gates = weight_ih @ x_t + weight_hh @ h_{t-1} + bias
 *   i = sigmoid(gates[0:H])
 *   f = sigmoid(gates[H:2H])
 *   g = tanh(gates[2H:3H])
 *   o = sigmoid(gates[3H:4H])
 *   c_t = f * c_{t-1} + i * g
 *   h_t = o * tanh(c_t)
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Input validation: null, size, and shape checks - 输入验证：null、大小和形状检查</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Activation#sigmoid(float[], int, int)
 * @see Activation#tanh(float[], int, int)
 * @see Blas#gemm(float, float[], int, int, boolean, float[], int, int, boolean, float, float[])
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class LstmOp implements Op {

    private static final String OP_NAME = "LSTM";

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.size() < 4) {
            throw new OpExecutionException(
                    "LstmOp requires 4 inputs [input, weight_ih, weight_hh, bias], got " + inputs.size(),
                    OP_NAME);
        }

        Tensor input = inputs.get(0);
        Tensor weightIh = inputs.get(1);
        Tensor weightHh = inputs.get(2);
        Tensor bias = inputs.get(3);

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
        if (H > Integer.MAX_VALUE / 4) {
            throw new OpExecutionException("hidden_size too large: " + H, OP_NAME);
        }
        int gateSize = 4 * H;

        // Validate weight shapes
        if (weightIh.shape().rank() != 2 || weightIh.shape().dim(0) != gateSize
                || weightIh.shape().dim(1) != inputSize) {
            throw new OpExecutionException(
                    "weight_ih must be [" + gateSize + ", " + inputSize + "], got " + weightIh.shape(),
                    OP_NAME);
        }
        if (weightHh.shape().rank() != 2 || weightHh.shape().dim(0) != gateSize
                || weightHh.shape().dim(1) != H) {
            throw new OpExecutionException(
                    "weight_hh must be [" + gateSize + ", " + H + "], got " + weightHh.shape(), OP_NAME);
        }
        if (bias.size() != gateSize) {
            throw new OpExecutionException(
                    "bias must have " + gateSize + " elements, got " + bias.size(), OP_NAME);
        }

        float[] wIh = weightIh.toFloatArray();
        float[] wHh = weightHh.toFloatArray();
        float[] biasData = bias.toFloatArray();
        float[] inputData = input.toFloatArray();

        // Output buffers
        long allHSize = (long) batchSize * seqLen * H;
        if (allHSize > Integer.MAX_VALUE) {
            throw new OpExecutionException("output too large", OP_NAME);
        }
        float[] allH = new float[(int) allHSize];
        float[] lastH = new float[batchSize * H];
        float[] lastC = new float[batchSize * H];

        // Process each batch sample
        for (int n = 0; n < batchSize; n++) {
            float[] h = new float[H];
            float[] c = new float[H];

            for (int t = 0; t < seqLen; t++) {
                // Extract x_t [1, inputSize]
                float[] xt = new float[inputSize];
                int inputOffset = (n * seqLen + t) * inputSize;
                System.arraycopy(inputData, inputOffset, xt, 0, inputSize);

                // gates = weight_ih @ x_t + weight_hh @ h + bias
                // weight_ih [gateSize, inputSize] @ x_t [inputSize, 1] -> [gateSize, 1]
                float[] gatesIh = new float[gateSize];
                Blas.gemm(1.0f, wIh, gateSize, inputSize, false,
                        xt, inputSize, 1, false,
                        0.0f, gatesIh);

                // weight_hh [gateSize, H] @ h [H, 1] -> [gateSize, 1]
                float[] gatesHh = new float[gateSize];
                Blas.gemm(1.0f, wHh, gateSize, H, false,
                        h, H, 1, false,
                        0.0f, gatesHh);

                // Combine: gates = gatesIh + gatesHh + bias
                float[] gates = new float[gateSize];
                for (int i = 0; i < gateSize; i++) {
                    gates[i] = gatesIh[i] + gatesHh[i] + biasData[i];
                }

                // Split and activate gates
                // i = sigmoid(gates[0:H])
                Activation.sigmoid(gates, 0, H);
                // f = sigmoid(gates[H:2H])
                Activation.sigmoid(gates, H, H);
                // g = tanh(gates[2H:3H])
                Activation.tanh(gates, 2 * H, H);
                // o = sigmoid(gates[3H:4H])
                Activation.sigmoid(gates, 3 * H, H);

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

                // Store h_t in allH
                System.arraycopy(h, 0, allH, (n * seqLen + t) * H, H);
            }

            // Store last h and c
            System.arraycopy(h, 0, lastH, n * H, H);
            System.arraycopy(c, 0, lastC, n * H, H);
        }

        Tensor allHOutput = Tensor.wrap(allH, Shape.of(batchSize, seqLen, H));
        Tensor lastHOutput = Tensor.wrap(lastH, Shape.of(batchSize, H));
        Tensor lastCOutput = Tensor.wrap(lastC, Shape.of(batchSize, H));

        return List.of(allHOutput, lastHOutput, lastCOutput);
    }
}
