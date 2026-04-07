package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for sequence operators: {@link LstmOp}, {@link BiLstmOp}, {@link CtcDecodeOp}.
 */
@DisplayName("SequenceOps — 序列算子测试")
class SequenceOpsTest {

    private static final float EPSILON = 1e-5f;

    // ==================== LSTM Tests ====================

    @Nested
    @DisplayName("LstmOp — LSTM 算子")
    class LstmOpTest {

        private final LstmOp op = new LstmOp();

        @Test
        @DisplayName("input[1,3,2] hidden=4 → output shapes [1,3,4], [1,4], [1,4]")
        void outputShapesCorrect() {
            int N = 1, T = 3, inputSize = 2, H = 4;
            Tensor input = Tensor.zeros(Shape.of(N, T, inputSize));
            Tensor wIh = Tensor.zeros(Shape.of(4 * H, inputSize));
            Tensor wHh = Tensor.zeros(Shape.of(4 * H, H));
            Tensor bias = Tensor.zeros(Shape.of(4 * H));

            OpAttribute attrs = OpAttribute.builder().put("hidden_size", H).build();
            List<Tensor> outputs = op.forward(List.of(input, wIh, wHh, bias), attrs);

            assertThat(outputs).hasSize(3);
            assertThat(outputs.get(0).shape()).isEqualTo(Shape.of(N, T, H));
            assertThat(outputs.get(1).shape()).isEqualTo(Shape.of(N, H));
            assertThat(outputs.get(2).shape()).isEqualTo(Shape.of(N, H));
        }

        @Test
        @DisplayName("Zero input → gates computable (no NaN)")
        void zeroInputNoNaN() {
            int N = 1, T = 2, inputSize = 3, H = 2;
            Tensor input = Tensor.zeros(Shape.of(N, T, inputSize));
            Tensor wIh = Tensor.zeros(Shape.of(4 * H, inputSize));
            Tensor wHh = Tensor.zeros(Shape.of(4 * H, H));
            Tensor bias = Tensor.zeros(Shape.of(4 * H));

            OpAttribute attrs = OpAttribute.builder().put("hidden_size", H).build();
            List<Tensor> outputs = op.forward(List.of(input, wIh, wHh, bias), attrs);

            // All outputs should contain no NaN
            for (Tensor t : outputs) {
                float[] data = t.toFloatArray();
                for (int i = 0; i < data.length; i++) {
                    assertThat(Float.isNaN(data[i]))
                            .as("NaN found at index %d", i)
                            .isFalse();
                }
            }
        }

        @Test
        @DisplayName("Known single-step: verify gate computation matches manual calculation")
        void knownSingleStep() {
            // N=1, T=1, inputSize=1, H=1
            // Single gate computation with known values
            int N = 1, T = 1, inputSize = 1, H = 1;

            // input x_0 = 1.0
            Tensor input = Tensor.fromFloat(new float[]{1.0f}, Shape.of(N, T, inputSize));
            // weight_ih [4, 1]: i=0.5, f=0.5, g=0.5, o=0.5
            Tensor wIh = Tensor.fromFloat(new float[]{0.5f, 0.5f, 0.5f, 0.5f}, Shape.of(4, inputSize));
            // weight_hh [4, 1]: all zeros (h_0 = 0 anyway)
            Tensor wHh = Tensor.fromFloat(new float[]{0.0f, 0.0f, 0.0f, 0.0f}, Shape.of(4, H));
            // bias [4]: all zeros
            Tensor bias = Tensor.fromFloat(new float[]{0.0f, 0.0f, 0.0f, 0.0f}, Shape.of(4));

            OpAttribute attrs = OpAttribute.builder().put("hidden_size", H).build();
            List<Tensor> outputs = op.forward(List.of(input, wIh, wHh, bias), attrs);

            // Manual computation:
            // gates = wIh @ [1.0] + wHh @ [0.0] + bias = [0.5, 0.5, 0.5, 0.5]
            // i = sigmoid(0.5) = 0.62246
            // f = sigmoid(0.5) = 0.62246
            // g = tanh(0.5)    = 0.46212
            // o = sigmoid(0.5) = 0.62246
            // c_0 = f * 0 + i * g = 0.62246 * 0.46212 = 0.28768
            // h_0 = o * tanh(c_0) = 0.62246 * tanh(0.28768) = 0.62246 * 0.28071 = 0.17482
            float expectedI = 1.0f / (1.0f + (float) Math.exp(-0.5));
            float expectedG = (float) Math.tanh(0.5);
            float expectedO = expectedI; // same as i since same gate value
            float expectedC = expectedI * expectedG; // f * 0 + i * g
            float expectedH = expectedO * (float) Math.tanh(expectedC);

            assertThat(outputs.get(0).getFloat(0, 0, 0)).isCloseTo(expectedH, within(EPSILON));
            assertThat(outputs.get(1).getFloat(0, 0)).isCloseTo(expectedH, within(EPSILON));
            assertThat(outputs.get(2).getFloat(0, 0)).isCloseTo(expectedC, within(EPSILON));
        }

        @Test
        @DisplayName("null inputs → NullPointerException")
        void nullInputsThrows() {
            assertThatThrownBy(() -> op.forward(null, OpAttribute.empty()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("insufficient inputs → OpExecutionException")
        void insufficientInputsThrows() {
            Tensor input = Tensor.zeros(Shape.of(1, 2, 3));
            assertThatThrownBy(() -> op.forward(List.of(input), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }

    // ==================== BiLSTM Tests ====================

    @Nested
    @DisplayName("BiLstmOp — 双向 LSTM 算子")
    class BiLstmOpTest {

        private final BiLstmOp op = new BiLstmOp();

        @Test
        @DisplayName("input[1,3,2] hidden=4 → output shape [1,3,8]")
        void outputShapeCorrect() {
            int N = 1, T = 3, inputSize = 2, H = 4;
            Tensor input = Tensor.zeros(Shape.of(N, T, inputSize));
            Tensor fwdWIh = Tensor.zeros(Shape.of(4 * H, inputSize));
            Tensor fwdWHh = Tensor.zeros(Shape.of(4 * H, H));
            Tensor fwdBias = Tensor.zeros(Shape.of(4 * H));
            Tensor bwdWIh = Tensor.zeros(Shape.of(4 * H, inputSize));
            Tensor bwdWHh = Tensor.zeros(Shape.of(4 * H, H));
            Tensor bwdBias = Tensor.zeros(Shape.of(4 * H));

            OpAttribute attrs = OpAttribute.builder().put("hidden_size", H).build();
            List<Tensor> outputs = op.forward(
                    List.of(input, fwdWIh, fwdWHh, fwdBias, bwdWIh, bwdWHh, bwdBias), attrs);

            assertThat(outputs).hasSize(1);
            assertThat(outputs.get(0).shape()).isEqualTo(Shape.of(N, T, 2 * H));
        }

        @Test
        @DisplayName("Output length matches input sequence length")
        void outputSeqLenMatchesInput() {
            int N = 2, T = 5, inputSize = 3, H = 2;
            Tensor input = Tensor.zeros(Shape.of(N, T, inputSize));
            Tensor fwdWIh = Tensor.zeros(Shape.of(4 * H, inputSize));
            Tensor fwdWHh = Tensor.zeros(Shape.of(4 * H, H));
            Tensor fwdBias = Tensor.zeros(Shape.of(4 * H));
            Tensor bwdWIh = Tensor.zeros(Shape.of(4 * H, inputSize));
            Tensor bwdWHh = Tensor.zeros(Shape.of(4 * H, H));
            Tensor bwdBias = Tensor.zeros(Shape.of(4 * H));

            OpAttribute attrs = OpAttribute.builder().put("hidden_size", H).build();
            List<Tensor> outputs = op.forward(
                    List.of(input, fwdWIh, fwdWHh, fwdBias, bwdWIh, bwdWHh, bwdBias), attrs);

            // Output should be [N, T, 2*H]
            assertThat(outputs.get(0).shape().dim(0)).isEqualTo(N);
            assertThat(outputs.get(0).shape().dim(1)).isEqualTo(T);
            assertThat(outputs.get(0).shape().dim(2)).isEqualTo(2 * H);
        }

        @Test
        @DisplayName("null inputs → NullPointerException")
        void nullInputsThrows() {
            assertThatThrownBy(() -> op.forward(null, OpAttribute.empty()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("insufficient inputs → OpExecutionException")
        void insufficientInputsThrows() {
            Tensor input = Tensor.zeros(Shape.of(1, 2, 3));
            assertThatThrownBy(() -> op.forward(List.of(input), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }

    // ==================== CTC Decode Tests ====================

    @Nested
    @DisplayName("CtcDecodeOp — CTC 解码算子")
    class CtcDecodeOpTest {

        private final CtcDecodeOp op = new CtcDecodeOp();

        @Test
        @DisplayName("Known logits [1,5,4] with clear argmax → correct decoded output")
        void knownLogitsDecodeCorrectly() {
            // 4 classes: 0=blank, 1='a', 2='b', 3='c'
            // Time steps: argmax sequence should be [1, 1, 0, 2, 3]
            // After dedup: [1, 0, 2, 3]
            // After blank removal: [1, 2, 3] → 'a', 'b', 'c'
            float[] logits = {
                    // t=0: class 1 has highest
                    -1.0f, 5.0f, 0.0f, 0.0f,
                    // t=1: class 1 again (consecutive duplicate)
                    -1.0f, 4.0f, 0.0f, 0.0f,
                    // t=2: class 0 (blank)
                    5.0f, -1.0f, -1.0f, -1.0f,
                    // t=3: class 2
                    -1.0f, 0.0f, 5.0f, 0.0f,
                    // t=4: class 3
                    -1.0f, 0.0f, 0.0f, 5.0f
            };
            Tensor input = Tensor.fromFloat(logits, Shape.of(1, 5, 4));
            OpAttribute attrs = OpAttribute.builder().put("blank_index", 0).build();

            List<Tensor> outputs = op.forward(List.of(input), attrs);

            assertThat(outputs).hasSize(1);
            Tensor decoded = outputs.get(0);
            assertThat(decoded.shape()).isEqualTo(Shape.of(1, 5));

            // Decoded: [1, 2, 3, -1, -1]
            assertThat(decoded.getFloat(0, 0)).isCloseTo(1.0f, within(EPSILON));
            assertThat(decoded.getFloat(0, 1)).isCloseTo(2.0f, within(EPSILON));
            assertThat(decoded.getFloat(0, 2)).isCloseTo(3.0f, within(EPSILON));
            assertThat(decoded.getFloat(0, 3)).isCloseTo(-1.0f, within(EPSILON));
            assertThat(decoded.getFloat(0, 4)).isCloseTo(-1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("Consecutive duplicates removed")
        void consecutiveDuplicatesRemoved() {
            // 3 classes: 0=blank, 1='a', 2='b'
            // Argmax sequence: [1, 1, 1, 2, 2]
            // After dedup: [1, 2]
            // After blank removal: [1, 2]
            float[] logits = {
                    -1.0f, 5.0f, 0.0f,  // t=0: class 1
                    -1.0f, 5.0f, 0.0f,  // t=1: class 1
                    -1.0f, 5.0f, 0.0f,  // t=2: class 1
                    -1.0f, 0.0f, 5.0f,  // t=3: class 2
                    -1.0f, 0.0f, 5.0f   // t=4: class 2
            };
            Tensor input = Tensor.fromFloat(logits, Shape.of(1, 5, 3));
            OpAttribute attrs = OpAttribute.builder().put("blank_index", 0).build();

            List<Tensor> outputs = op.forward(List.of(input), attrs);
            Tensor decoded = outputs.get(0);

            // Should decode to [1, 2, -1, -1, -1]
            assertThat(decoded.getFloat(0, 0)).isCloseTo(1.0f, within(EPSILON));
            assertThat(decoded.getFloat(0, 1)).isCloseTo(2.0f, within(EPSILON));
            assertThat(decoded.getFloat(0, 2)).isCloseTo(-1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("Blank tokens removed")
        void blankTokensRemoved() {
            // 3 classes: 0=blank, 1='a', 2='b'
            // Argmax sequence: [0, 1, 0, 2, 0]
            // After dedup: [0, 1, 0, 2, 0] (no consecutive dups)
            // After blank removal: [1, 2]
            float[] logits = {
                    5.0f, -1.0f, -1.0f,  // t=0: blank
                    -1.0f, 5.0f, -1.0f,  // t=1: class 1
                    5.0f, -1.0f, -1.0f,  // t=2: blank
                    -1.0f, -1.0f, 5.0f,  // t=3: class 2
                    5.0f, -1.0f, -1.0f   // t=4: blank
            };
            Tensor input = Tensor.fromFloat(logits, Shape.of(1, 5, 3));
            OpAttribute attrs = OpAttribute.builder().put("blank_index", 0).build();

            List<Tensor> outputs = op.forward(List.of(input), attrs);
            Tensor decoded = outputs.get(0);

            assertThat(decoded.getFloat(0, 0)).isCloseTo(1.0f, within(EPSILON));
            assertThat(decoded.getFloat(0, 1)).isCloseTo(2.0f, within(EPSILON));
            assertThat(decoded.getFloat(0, 2)).isCloseTo(-1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("null inputs → NullPointerException")
        void nullInputsThrows() {
            assertThatThrownBy(() -> op.forward(null, OpAttribute.empty()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("empty inputs → OpExecutionException")
        void emptyInputsThrows() {
            assertThatThrownBy(() -> op.forward(List.of(), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }

    // ==================== CTC Beam Search Tests ====================

    @Nested
    @DisplayName("CtcBeamSearchOp — CTC 束搜索解码算子")
    class CtcBeamSearchOpTest {

        private final CtcBeamSearchOp op = new CtcBeamSearchOp();

        @Test
        @DisplayName("明确 logits 的束搜索结果与贪心解码一致")
        void clearLogitsMatchesGreedy() {
            // 4 classes: 0=blank, 1='a', 2='b', 3='c'
            // Clear argmax: [1, 1, 0, 2, 3]
            // Greedy after dedup+blank removal: [1, 2, 3]
            // Beam search should produce the same result
            float[] logits = {
                    -10.0f, 0.0f, -10.0f, -10.0f,   // t=0: class 1
                    -10.0f, 0.0f, -10.0f, -10.0f,   // t=1: class 1
                    0.0f, -10.0f, -10.0f, -10.0f,    // t=2: blank
                    -10.0f, -10.0f, 0.0f, -10.0f,    // t=3: class 2
                    -10.0f, -10.0f, -10.0f, 0.0f     // t=4: class 3
            };
            Tensor input = Tensor.fromFloat(logits, Shape.of(1, 5, 4));
            OpAttribute attrs = OpAttribute.builder()
                    .put("blank_index", 0)
                    .put("beam_width", 10)
                    .build();

            List<Tensor> outputs = op.forward(List.of(input), attrs);

            assertThat(outputs).hasSize(1);
            Tensor decoded = outputs.get(0);
            assertThat(decoded.shape()).isEqualTo(Shape.of(1, 5));

            // Expected: [1, 2, 3, -1, -1]
            assertThat(decoded.getFloat(0, 0)).isCloseTo(1.0f, within(EPSILON));
            assertThat(decoded.getFloat(0, 1)).isCloseTo(2.0f, within(EPSILON));
            assertThat(decoded.getFloat(0, 2)).isCloseTo(3.0f, within(EPSILON));
            assertThat(decoded.getFloat(0, 3)).isCloseTo(-1.0f, within(EPSILON));
            assertThat(decoded.getFloat(0, 4)).isCloseTo(-1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("模糊 logits — 束搜索利用路径概率发现更好的解码")
        void ambiguousLogitsBeamSearchFindsOptimal() {
            // 3 classes: 0=blank, 1='a', 2='b'
            // t=0: blank=-10, a=-0.01, b=-10  -> argmax: a
            // t=1: blank=-0.01, a=-10, b=-10  -> argmax: blank
            // t=2: blank=-10, a=-0.01, b=-10  -> argmax: a
            // Greedy: argmax=[1,0,1] -> blank removal -> decoded [1,1]
            // Beam search: should also find [1,1] via blank-separated path
            float[] logits = {
                    -10.0f, -0.01f, -10.0f,   // t=0: a dominates
                    -0.01f, -10.0f, -10.0f,    // t=1: blank dominates
                    -10.0f, -0.01f, -10.0f     // t=2: a dominates
            };
            Tensor input = Tensor.fromFloat(logits, Shape.of(1, 3, 3));

            // Greedy: [1, 0, 1] -> blank removal -> [1, 1]
            CtcDecodeOp greedyOp = new CtcDecodeOp();
            OpAttribute greedyAttrs = OpAttribute.builder().put("blank_index", 0).build();
            Tensor greedyDecoded = greedyOp.forward(List.of(input), greedyAttrs).get(0);
            assertThat(greedyDecoded.getFloat(0, 0)).isCloseTo(1.0f, within(EPSILON));
            assertThat(greedyDecoded.getFloat(0, 1)).isCloseTo(1.0f, within(EPSILON));

            // Beam search should find the same optimal decode
            OpAttribute beamAttrs = OpAttribute.builder()
                    .put("blank_index", 0)
                    .put("beam_width", 10)
                    .build();
            Tensor beamDecoded = op.forward(List.of(input), beamAttrs).get(0);
            assertThat(beamDecoded.getFloat(0, 0)).isCloseTo(1.0f, within(EPSILON));
            assertThat(beamDecoded.getFloat(0, 1)).isCloseTo(1.0f, within(EPSILON));
            assertThat(beamDecoded.getFloat(0, 2)).isCloseTo(-1.0f, within(EPSILON));

            // Verify beam produces valid decode and length >= greedy for multi-class scenario
            float[] logits2 = {
                    -10.0f, -0.01f, -10.0f,
                    -10.0f, -0.01f, -10.0f,
                    -10.0f, -10.0f, -0.01f
            };
            Tensor input2 = Tensor.fromFloat(logits2, Shape.of(1, 3, 3));
            Tensor greedyDecoded2 = greedyOp.forward(List.of(input2), greedyAttrs).get(0);
            Tensor beamDecoded2 = op.forward(List.of(input2), beamAttrs).get(0);

            // Both decode to [1, 2, -1]
            assertThat(greedyDecoded2.getFloat(0, 0)).isCloseTo(1.0f, within(EPSILON));
            assertThat(greedyDecoded2.getFloat(0, 1)).isCloseTo(2.0f, within(EPSILON));
            assertThat(beamDecoded2.getFloat(0, 0)).isCloseTo(1.0f, within(EPSILON));
            assertThat(beamDecoded2.getFloat(0, 1)).isCloseTo(2.0f, within(EPSILON));
        }

        @Test
        @DisplayName("beam_width=1 等价于贪心解码")
        void beamWidthOneEquivalentToGreedy() {
            // With beam_width=1, beam search should behave like greedy
            float[] logits = {
                    -10.0f, 0.0f, -10.0f,   // t=0: class 1
                    -10.0f, -10.0f, 0.0f,   // t=1: class 2
                    0.0f, -10.0f, -10.0f,    // t=2: blank
                    -10.0f, 0.0f, -10.0f     // t=3: class 1
            };
            Tensor input = Tensor.fromFloat(logits, Shape.of(1, 4, 3));

            // Greedy
            CtcDecodeOp greedyOp = new CtcDecodeOp();
            OpAttribute greedyAttrs = OpAttribute.builder().put("blank_index", 0).build();
            Tensor greedyDecoded = greedyOp.forward(List.of(input), greedyAttrs).get(0);

            // Beam width 1
            OpAttribute beamAttrs = OpAttribute.builder()
                    .put("blank_index", 0)
                    .put("beam_width", 1)
                    .build();
            Tensor beamDecoded = op.forward(List.of(input), beamAttrs).get(0);

            // Both should produce [1, 2, 1, -1]
            for (int i = 0; i < 4; i++) {
                assertThat(beamDecoded.getFloat(0, i))
                        .as("Position %d", i)
                        .isCloseTo(greedyDecoded.getFloat(0, i), within(EPSILON));
            }
        }

        @Test
        @DisplayName("beam_width=100 在合理时间内完成")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void largeBeamWidthRunsInReasonableTime() {
            // 10 classes, 20 time steps — beam_width=100 should still run fast
            int T = 20, C = 10;
            float[] logits = new float[T * C];
            // Fill with uniform log-probs
            float uniform = (float) -Math.log(C);
            Arrays.fill(logits, uniform);
            Tensor input = Tensor.fromFloat(logits, Shape.of(1, T, C));

            OpAttribute attrs = OpAttribute.builder()
                    .put("blank_index", 0)
                    .put("beam_width", 100)
                    .build();

            List<Tensor> outputs = op.forward(List.of(input), attrs);
            assertThat(outputs).hasSize(1);
            assertThat(outputs.get(0).shape()).isEqualTo(Shape.of(1, T));
        }

        @Test
        @DisplayName("null inputs → NullPointerException")
        void nullInputsThrows() {
            assertThatThrownBy(() -> op.forward(null, OpAttribute.empty()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("empty inputs → OpExecutionException")
        void emptyInputsThrows() {
            assertThatThrownBy(() -> op.forward(List.of(), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }

        @Test
        @DisplayName("invalid beam_width → OpExecutionException")
        void invalidBeamWidthThrows() {
            Tensor input = Tensor.zeros(Shape.of(1, 3, 4));
            OpAttribute attrs = OpAttribute.builder()
                    .put("blank_index", 0)
                    .put("beam_width", 0)
                    .build();
            assertThatThrownBy(() -> op.forward(List.of(input), attrs))
                    .isInstanceOf(OpExecutionException.class);
        }

        @Test
        @DisplayName("logSumExp 数值稳定性")
        void logSumExpNumericalStability() {
            // log(exp(0) + exp(0)) = log(2) ≈ 0.6931
            assertThat(CtcBeamSearchOp.logSumExp(0.0f, 0.0f))
                    .isCloseTo((float) Math.log(2.0), within(1e-4f));

            // When one value is NEG_INF, result is the other
            assertThat(CtcBeamSearchOp.logSumExp(-1e30f, -1.0f))
                    .isCloseTo(-1.0f, within(1e-4f));
            assertThat(CtcBeamSearchOp.logSumExp(-1.0f, -1e30f))
                    .isCloseTo(-1.0f, within(1e-4f));

            // Large values should not overflow
            assertThat(CtcBeamSearchOp.logSumExp(100.0f, 100.0f))
                    .isCloseTo(100.0f + (float) Math.log(2.0), within(1e-3f));
        }

        @Test
        @DisplayName("批量输入 — 多个样本独立解码")
        void batchInputDecodesIndependently() {
            // Batch of 2 samples, each with 3 time steps, 3 classes
            float[] logits = {
                    // Sample 0: clear [1, 0, 2] → decoded [1, 2]
                    -10.0f, 0.0f, -10.0f,
                    0.0f, -10.0f, -10.0f,
                    -10.0f, -10.0f, 0.0f,
                    // Sample 1: clear [2, 0, 1] → decoded [2, 1]
                    -10.0f, -10.0f, 0.0f,
                    0.0f, -10.0f, -10.0f,
                    -10.0f, 0.0f, -10.0f
            };
            Tensor input = Tensor.fromFloat(logits, Shape.of(2, 3, 3));
            OpAttribute attrs = OpAttribute.builder()
                    .put("blank_index", 0)
                    .put("beam_width", 5)
                    .build();

            List<Tensor> outputs = op.forward(List.of(input), attrs);
            Tensor decoded = outputs.get(0);

            // Sample 0: [1, 2, -1]
            assertThat(decoded.getFloat(0, 0)).isCloseTo(1.0f, within(EPSILON));
            assertThat(decoded.getFloat(0, 1)).isCloseTo(2.0f, within(EPSILON));
            assertThat(decoded.getFloat(0, 2)).isCloseTo(-1.0f, within(EPSILON));

            // Sample 1: [2, 1, -1]
            assertThat(decoded.getFloat(1, 0)).isCloseTo(2.0f, within(EPSILON));
            assertThat(decoded.getFloat(1, 1)).isCloseTo(1.0f, within(EPSILON));
            assertThat(decoded.getFloat(1, 2)).isCloseTo(-1.0f, within(EPSILON));
        }
    }
}
