package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * CTC Beam Search Decode Operator
 * CTC 束搜索解码算子
 *
 * <p>Performs CTC beam search decoding on log-probability logits. At each time step,
 * extends all active beams with every class, merges duplicates, and prunes to the
 * top {@code beam_width} beams by total log-probability. The final best beam's
 * sequence is returned, padded with -1 to fill a fixed-length tensor.</p>
 * <p>对对数概率 logits 执行 CTC 束搜索解码。在每个时间步，
 * 用所有类别扩展所有活跃束，合并重复项，并按总对数概率裁剪为前
 * {@code beam_width} 个束。返回最终最优束的序列，用 -1 填充到固定长度张量。</p>
 *
 * <p><strong>Inputs | 输入:</strong></p>
 * <ul>
 *   <li>logits: Tensor [N, T, num_classes] — log probabilities (post log-softmax) |
 *       对数概率（log-softmax 后）</li>
 * </ul>
 *
 * <p><strong>Attributes | 属性:</strong></p>
 * <ul>
 *   <li>blank_index (int, default=0): index of the blank token | 空白标记索引</li>
 *   <li>beam_width (int, default=10): number of beams to keep at each step | 每步保留的束数</li>
 * </ul>
 *
 * <p><strong>Outputs | 输出:</strong></p>
 * <ul>
 *   <li>decoded: Tensor [N, T] — decoded class indices padded with -1 |
 *       解码的类别索引，用 -1 填充</li>
 * </ul>
 *
 * <p><strong>Algorithm | 算法:</strong></p>
 * <ol>
 *   <li>Initialize beams with empty sequence (logP_blank=0, logP_nonblank=-inf) |
 *       以空序列初始化束</li>
 *   <li>For each time step, extend beams with blank, same-char, and new-char transitions |
 *       每步用空白、相同字符和新字符扩展束</li>
 *   <li>Merge beams with identical sequences using log-sum-exp |
 *       使用 log-sum-exp 合并相同序列的束</li>
 *   <li>Prune to top beam_width beams by total probability |
 *       裁剪为前 beam_width 个束</li>
 *   <li>Return best beam's sequence | 返回最优束的序列</li>
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
 * @see CtcDecodeOp
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class CtcBeamSearchOp implements Op {

    private static final String OP_NAME = "CTCBeamSearch";

    /** Negative infinity sentinel for log-probability (use a large negative finite value) */
    private static final float NEG_INF = -1e30f;

    @Override
    public List<Tensor> forward(List<Tensor> inputs, OpAttribute attrs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        if (inputs.isEmpty()) {
            throw new OpExecutionException("CtcBeamSearchOp requires at least 1 input [logits]", OP_NAME);
        }

        Tensor logits = inputs.get(0);

        // Validate logits shape [N, T, num_classes]
        if (logits.shape().rank() != 3) {
            throw new OpExecutionException(
                    "Logits must be 3D [N, T, num_classes], got rank " + logits.shape().rank(), OP_NAME);
        }

        int blankIndex = attrs.getInt("blank_index", 0);
        int beamWidth = attrs.getInt("beam_width", 10);

        if (beamWidth <= 0) {
            throw new OpExecutionException("beam_width must be > 0, got " + beamWidth, OP_NAME);
        }

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
            List<Integer> bestSeq = beamSearchDecode(logitsData, n, seqLen, numClasses,
                    blankIndex, beamWidth);

            int writeLen = Math.min(bestSeq.size(), seqLen);
            for (int i = 0; i < writeLen; i++) {
                decoded[n * seqLen + i] = bestSeq.get(i);
            }
        }

        return List.of(Tensor.wrap(decoded, Shape.of(batchSize, seqLen)));
    }

    /**
     * Perform beam search decoding for a single batch sample
     * 对单个批样本执行束搜索解码
     *
     * @param logitsData flat logits array | 扁平 logits 数组
     * @param batchIdx   batch index | 批索引
     * @param seqLen     sequence length | 序列长度
     * @param numClasses number of classes | 类别数
     * @param blankIndex blank token index | 空白标记索引
     * @param beamWidth  beam width | 束宽度
     * @return decoded sequence (no blanks, no duplicates) | 解码序列
     */
    private List<Integer> beamSearchDecode(float[] logitsData, int batchIdx,
                                           int seqLen, int numClasses,
                                           int blankIndex, int beamWidth) {
        // beams: Map from sequence → BeamState(logP_blank, logP_nonblank)
        Map<List<Integer>, BeamState> beams = new HashMap<>();
        beams.put(List.of(), new BeamState(0.0f, NEG_INF));

        for (int t = 0; t < seqLen; t++) {
            int timeOffset = (batchIdx * seqLen + t) * numClasses;
            Map<List<Integer>, BeamState> newBeams = new HashMap<>();

            for (Map.Entry<List<Integer>, BeamState> entry : beams.entrySet()) {
                List<Integer> seq = entry.getKey();
                BeamState state = entry.getValue();
                float totalLogP = logSumExp(state.logPBlank, state.logPNonblank);

                for (int c = 0; c < numClasses; c++) {
                    float logPc = logitsData[timeOffset + c];

                    if (c == blankIndex) {
                        // Extend blank: any prefix can produce blank
                        mergeBeam(newBeams, seq,
                                totalLogP + logPc, NEG_INF);
                    } else if (!seq.isEmpty() && c == seq.get(seq.size() - 1)) {
                        // Same character as last in sequence
                        // Case 1: Direct continuation (only from non-blank path)
                        mergeBeam(newBeams, seq,
                                NEG_INF, state.logPNonblank + logPc);
                        // Case 2: Blank-separated repetition (from blank path)
                        List<Integer> extSeq = extendSequence(seq, c);
                        mergeBeam(newBeams, extSeq,
                                NEG_INF, state.logPBlank + logPc);
                    } else {
                        // New character
                        List<Integer> extSeq = extendSequence(seq, c);
                        mergeBeam(newBeams, extSeq,
                                NEG_INF, totalLogP + logPc);
                    }
                }
            }

            // Prune to top beamWidth beams
            beams = pruneBeams(newBeams, beamWidth);
        }

        // Return best beam
        List<Integer> bestSeq = List.of();
        float bestScore = NEG_INF;
        for (Map.Entry<List<Integer>, BeamState> entry : beams.entrySet()) {
            float score = logSumExp(entry.getValue().logPBlank, entry.getValue().logPNonblank);
            if (score > bestScore) {
                bestScore = score;
                bestSeq = entry.getKey();
            }
        }
        return bestSeq;
    }

    /**
     * Merge a beam state into the new beams map using log-sum-exp
     * 使用 log-sum-exp 将束状态合并到新束映射中
     */
    private void mergeBeam(Map<List<Integer>, BeamState> beams, List<Integer> seq,
                           float logPBlank, float logPNonblank) {
        BeamState existing = beams.get(seq);
        if (existing == null) {
            beams.put(seq, new BeamState(logPBlank, logPNonblank));
        } else {
            beams.put(seq, new BeamState(
                    logSumExp(existing.logPBlank, logPBlank),
                    logSumExp(existing.logPNonblank, logPNonblank)));
        }
    }

    /**
     * Extend a sequence by appending a class index
     * 通过追加类别索引扩展序列
     */
    private List<Integer> extendSequence(List<Integer> seq, int c) {
        List<Integer> extended = new ArrayList<>(seq.size() + 1);
        extended.addAll(seq);
        extended.add(c);
        return List.copyOf(extended);
    }

    /**
     * Prune beams to top-k by total log-probability
     * 按总对数概率裁剪束为前 k 个
     */
    private Map<List<Integer>, BeamState> pruneBeams(Map<List<Integer>, BeamState> beams,
                                                     int beamWidth) {
        if (beams.size() <= beamWidth) {
            return beams;
        }

        List<Map.Entry<List<Integer>, BeamState>> sorted = new ArrayList<>(beams.entrySet());
        sorted.sort((a, b) -> {
            float scoreA = logSumExp(a.getValue().logPBlank, a.getValue().logPNonblank);
            float scoreB = logSumExp(b.getValue().logPBlank, b.getValue().logPNonblank);
            return Float.compare(scoreB, scoreA); // descending
        });

        Map<List<Integer>, BeamState> pruned = new HashMap<>();
        int limit = Math.min(beamWidth, sorted.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<List<Integer>, BeamState> entry = sorted.get(i);
            pruned.put(entry.getKey(), entry.getValue());
        }
        return pruned;
    }

    /**
     * Log-sum-exp of two values: log(exp(a) + exp(b))
     * 两个值的 log-sum-exp: log(exp(a) + exp(b))
     *
     * <p>Computed as max(a,b) + log(1 + exp(-|a-b|)) for numerical stability.</p>
     * <p>以 max(a,b) + log(1 + exp(-|a-b|)) 计算以保证数值稳定性。</p>
     *
     * @param a first log-probability | 第一个对数概率
     * @param b second log-probability | 第二个对数概率
     * @return log(exp(a) + exp(b))
     */
    static float logSumExp(float a, float b) {
        if (a <= NEG_INF + 1) return b;
        if (b <= NEG_INF + 1) return a;
        float maxVal = Math.max(a, b);
        return maxVal + (float) Math.log(1.0 + Math.exp(-Math.abs(a - b)));
    }

    /**
     * Beam state holding blank and non-blank log-probabilities
     * 持有空白和非空白对数概率的束状态
     */
    private record BeamState(float logPBlank, float logPNonblank) {
    }
}
