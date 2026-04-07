package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.List;
import java.util.Objects;

/**
 * CTC (Connectionist Temporal Classification) Greedy Decode Operator
 * CTC（连接时序分类）贪心解码算子
 *
 * <p>Performs greedy CTC decoding on logits by taking the argmax at each time step,
 * removing consecutive duplicates, and stripping blank tokens. The decoded output
 * is padded with -1 to fill a fixed-length tensor.</p>
 * <p>对 logits 执行贪心 CTC 解码：在每个时间步取 argmax，
 * 去除连续重复，并移除空白标记。解码输出用 -1 填充到固定长度张量。</p>
 *
 * <p><strong>Inputs | 输入:</strong></p>
 * <ul>
 *   <li>logits: Tensor [N, T, num_classes] — raw logits (pre-softmax or post-softmax) |
 *       原始 logits（softmax 前或后）</li>
 * </ul>
 *
 * <p><strong>Attributes | 属性:</strong></p>
 * <ul>
 *   <li>blank_index (int, default=0): index of the blank token | 空白标记索引</li>
 *   <li>beam_width (int, default=10): reserved for future beam search | 预留给未来的束搜索宽度</li>
 * </ul>
 *
 * <p><strong>Outputs | 输出:</strong></p>
 * <ul>
 *   <li>decoded: Tensor [N, T] — decoded class indices padded with -1 |
 *       解码的类别索引，用 -1 填充</li>
 * </ul>
 *
 * <p><strong>Algorithm (Greedy Decode) | 算法（贪心解码）:</strong></p>
 * <ol>
 *   <li>For each batch sample, take argmax at each time step | 对每个批样本，在每个时间步取 argmax</li>
 *   <li>Remove consecutive duplicate indices | 去除连续重复索引</li>
 *   <li>Remove indices equal to blank_index | 移除等于 blank_index 的索引</li>
 *   <li>Pad remaining positions with -1 | 用 -1 填充剩余位置</li>
 * </ol>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Input validation: null, rank, and attribute checks - 输入验证：null、维度和属性检查</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class CtcDecodeOp implements Op {

    private static final String OP_NAME = "CTCDecode";

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.isEmpty()) {
            throw new OpExecutionException("CtcDecodeOp requires at least 1 input [logits]", OP_NAME);
        }

        Tensor logits = inputs.get(0);

        // Validate logits shape [N, T, num_classes]
        if (logits.shape().rank() != 3) {
            throw new OpExecutionException(
                    "Logits must be 3D [N, T, num_classes], got rank " + logits.shape().rank(), OP_NAME);
        }

        int blankIndex = attrs.getInt("blank_index", 0);
        // beam_width reserved for future use
        // int beamWidth = attrs.getInt("beam_width", 10);

        int batchSize = logits.shape().dim(0);
        int seqLen = logits.shape().dim(1);
        int numClasses = logits.shape().dim(2);

        if (blankIndex < 0 || blankIndex >= numClasses) {
            throw new OpExecutionException(
                    "blank_index " + blankIndex + " out of range [0, " + numClasses + ")", OP_NAME);
        }

        float[] logitsData = logits.toFloatArray();

        // Output: [N, T] padded with -1
        float[] decoded = new float[batchSize * seqLen];
        java.util.Arrays.fill(decoded, -1.0f);

        for (int n = 0; n < batchSize; n++) {
            // Step 1: argmax at each time step
            int[] argmaxSeq = new int[seqLen];
            for (int t = 0; t < seqLen; t++) {
                int offset = (n * seqLen + t) * numClasses;
                int bestIdx = 0;
                float bestVal = logitsData[offset];
                for (int cls = 1; cls < numClasses; cls++) {
                    float val = logitsData[offset + cls];
                    if (val > bestVal) {
                        bestVal = val;
                        bestIdx = cls;
                    }
                }
                argmaxSeq[t] = bestIdx;
            }

            // Step 2 & 3: Remove consecutive duplicates and blank tokens
            int writePos = 0;
            int prev = -1;
            for (int t = 0; t < seqLen; t++) {
                int current = argmaxSeq[t];
                if (current != prev) {
                    if (current != blankIndex) {
                        decoded[n * seqLen + writePos] = current;
                        writePos++;
                    }
                }
                prev = current;
            }
            // Remaining positions are already -1 from Arrays.fill
        }

        return List.of(Tensor.wrap(decoded, Shape.of(batchSize, seqLen)));
    }
}
