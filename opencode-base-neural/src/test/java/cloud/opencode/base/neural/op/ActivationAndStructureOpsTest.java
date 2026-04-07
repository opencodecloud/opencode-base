package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Combined tests for activation and structure operators.
 * 激活算子和结构算子的合并测试。
 */
@DisplayName("ActivationAndStructureOps — 激活与结构算子测试")
class ActivationAndStructureOpsTest {

    private static final float EPSILON = 1e-5f;

    // ==================== ReluOp ====================

    @Nested
    @DisplayName("ReluOp — ReLU 激活算子")
    class ReluOpTest {

        private final ReluOp op = new ReluOp();

        @Test
        @DisplayName("[-1, 0, 2] → [0, 0, 2]")
        void reluBasic() {
            Tensor input = Tensor.fromFloat(new float[]{-1f, 0f, 2f}, Shape.of(3));
            List<Tensor> outputs = op.forward(List.of(input), OpAttribute.empty());

            assertThat(outputs).hasSize(1);
            float[] data = outputs.get(0).toFloatArray();
            assertThat(data).containsExactly(0f, 0f, 2f);
        }

        @Test
        @DisplayName("null inputs → NullPointerException")
        void nullInputs() {
            assertThatNullPointerException().isThrownBy(() ->
                    op.forward(null, OpAttribute.empty()));
        }

        @Test
        @DisplayName("empty inputs → OpExecutionException")
        void emptyInputs() {
            assertThatThrownBy(() -> op.forward(List.of(), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }

    // ==================== SigmoidOp ====================

    @Nested
    @DisplayName("SigmoidOp — Sigmoid 激活算子")
    class SigmoidOpTest {

        private final SigmoidOp op = new SigmoidOp();

        @Test
        @DisplayName("[0] → [0.5]")
        void sigmoidZero() {
            Tensor input = Tensor.fromFloat(new float[]{0f}, Shape.of(1));
            List<Tensor> outputs = op.forward(List.of(input), OpAttribute.empty());

            assertThat(outputs).hasSize(1);
            assertThat(outputs.get(0).toFloatArray()[0]).isCloseTo(0.5f, within(EPSILON));
        }

        @Test
        @DisplayName("null inputs → NullPointerException")
        void nullInputs() {
            assertThatNullPointerException().isThrownBy(() ->
                    op.forward(null, OpAttribute.empty()));
        }

        @Test
        @DisplayName("empty inputs → OpExecutionException")
        void emptyInputs() {
            assertThatThrownBy(() -> op.forward(List.of(), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }

    // ==================== TanhOp ====================

    @Nested
    @DisplayName("TanhOp — Tanh 激活算子")
    class TanhOpTest {

        private final TanhOp op = new TanhOp();

        @Test
        @DisplayName("[0] → [0]")
        void tanhZero() {
            Tensor input = Tensor.fromFloat(new float[]{0f}, Shape.of(1));
            List<Tensor> outputs = op.forward(List.of(input), OpAttribute.empty());

            assertThat(outputs).hasSize(1);
            assertThat(outputs.get(0).toFloatArray()[0]).isCloseTo(0f, within(EPSILON));
        }

        @Test
        @DisplayName("null inputs → NullPointerException")
        void nullInputs() {
            assertThatNullPointerException().isThrownBy(() ->
                    op.forward(null, OpAttribute.empty()));
        }

        @Test
        @DisplayName("empty inputs → OpExecutionException")
        void emptyInputs() {
            assertThatThrownBy(() -> op.forward(List.of(), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }

    // ==================== SoftmaxOp ====================

    @Nested
    @DisplayName("SoftmaxOp — Softmax 激活算子")
    class SoftmaxOpTest {

        private final SoftmaxOp op = new SoftmaxOp();

        @Test
        @DisplayName("output sums to 1.0")
        void softmaxSumsToOne() {
            Tensor input = Tensor.fromFloat(new float[]{1f, 2f, 3f}, Shape.of(3));
            List<Tensor> outputs = op.forward(List.of(input), OpAttribute.empty());

            assertThat(outputs).hasSize(1);
            float[] data = outputs.get(0).toFloatArray();
            float sum = 0f;
            for (float v : data) {
                sum += v;
            }
            assertThat(sum).isCloseTo(1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("2D batch: each row sums to 1.0")
        void softmaxBatch() {
            Tensor input = Tensor.fromFloat(new float[]{1f, 2f, 3f, 4f, 5f, 6f}, Shape.of(2, 3));
            List<Tensor> outputs = op.forward(List.of(input), OpAttribute.empty());

            float[] data = outputs.get(0).toFloatArray();
            float row0Sum = data[0] + data[1] + data[2];
            float row1Sum = data[3] + data[4] + data[5];
            assertThat(row0Sum).isCloseTo(1.0f, within(EPSILON));
            assertThat(row1Sum).isCloseTo(1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("null inputs → NullPointerException")
        void nullInputs() {
            assertThatNullPointerException().isThrownBy(() ->
                    op.forward(null, OpAttribute.empty()));
        }

        @Test
        @DisplayName("empty inputs → OpExecutionException")
        void emptyInputs() {
            assertThatThrownBy(() -> op.forward(List.of(), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }

    // ==================== FlattenOp ====================

    @Nested
    @DisplayName("FlattenOp — 展平算子")
    class FlattenOpTest {

        private final FlattenOp op = new FlattenOp();

        @Test
        @DisplayName("[1, 3, 4, 4] → [1, 48]")
        void flattenDefault() {
            Tensor input = Tensor.zeros(Shape.of(1, 3, 4, 4));
            List<Tensor> outputs = op.forward(List.of(input), OpAttribute.empty());

            assertThat(outputs).hasSize(1);
            Shape outShape = outputs.get(0).shape();
            assertThat(outShape.rank()).isEqualTo(2);
            assertThat(outShape.dim(0)).isEqualTo(1);
            assertThat(outShape.dim(1)).isEqualTo(48);
        }

        @Test
        @DisplayName("custom start_dim=0 → [48]")
        void flattenStartDimZero() {
            Tensor input = Tensor.zeros(Shape.of(1, 3, 4, 4));
            OpAttribute attrs = OpAttribute.builder().put("start_dim", 0).build();
            List<Tensor> outputs = op.forward(List.of(input), attrs);

            Shape outShape = outputs.get(0).shape();
            assertThat(outShape.rank()).isEqualTo(1);
            assertThat(outShape.dim(0)).isEqualTo(48);
        }

        @Test
        @DisplayName("null inputs → NullPointerException")
        void nullInputs() {
            assertThatNullPointerException().isThrownBy(() ->
                    op.forward(null, OpAttribute.empty()));
        }

        @Test
        @DisplayName("empty inputs → OpExecutionException")
        void emptyInputs() {
            assertThatThrownBy(() -> op.forward(List.of(), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }

    // ==================== ReshapeOp ====================

    @Nested
    @DisplayName("ReshapeOp — 重塑算子")
    class ReshapeOpTest {

        private final ReshapeOp op = new ReshapeOp();

        @Test
        @DisplayName("[1, 12] → [3, 4] with shape attr")
        void reshapeBasic() {
            Tensor input = Tensor.fromFloat(
                    new float[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}, Shape.of(1, 12));
            OpAttribute attrs = OpAttribute.builder().put("shape", new int[]{3, 4}).build();
            List<Tensor> outputs = op.forward(List.of(input), attrs);

            assertThat(outputs).hasSize(1);
            Shape outShape = outputs.get(0).shape();
            assertThat(outShape.rank()).isEqualTo(2);
            assertThat(outShape.dim(0)).isEqualTo(3);
            assertThat(outShape.dim(1)).isEqualTo(4);
            // Verify data preserved
            assertThat(outputs.get(0).getFloat(0, 0)).isEqualTo(1f);
            assertThat(outputs.get(0).getFloat(2, 3)).isEqualTo(12f);
        }

        @Test
        @DisplayName("reshape with -1 inference")
        void reshapeWithInference() {
            Tensor input = Tensor.fromFloat(
                    new float[]{1, 2, 3, 4, 5, 6}, Shape.of(2, 3));
            OpAttribute attrs = OpAttribute.builder().put("shape", new int[]{-1, 2}).build();
            List<Tensor> outputs = op.forward(List.of(input), attrs);

            Shape outShape = outputs.get(0).shape();
            assertThat(outShape.dim(0)).isEqualTo(3);
            assertThat(outShape.dim(1)).isEqualTo(2);
        }

        @Test
        @DisplayName("missing shape attr → OpExecutionException")
        void missingShapeAttr() {
            Tensor input = Tensor.zeros(Shape.of(2, 3));
            assertThatThrownBy(() -> op.forward(List.of(input), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }

        @Test
        @DisplayName("null inputs → NullPointerException")
        void nullInputs() {
            assertThatNullPointerException().isThrownBy(() ->
                    op.forward(null, OpAttribute.empty()));
        }

        @Test
        @DisplayName("empty inputs → OpExecutionException")
        void emptyInputs() {
            assertThatThrownBy(() -> op.forward(List.of(), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }

    // ==================== DropoutOp ====================

    @Nested
    @DisplayName("DropoutOp — Dropout 算子")
    class DropoutOpTest {

        private final DropoutOp op = new DropoutOp();

        @Test
        @DisplayName("passthrough: same data returned")
        void passthrough() {
            Tensor input = Tensor.fromFloat(new float[]{1f, 2f, 3f}, Shape.of(3));
            List<Tensor> outputs = op.forward(List.of(input), OpAttribute.empty());

            assertThat(outputs).hasSize(1);
            assertThat(outputs.get(0)).isSameAs(input);
            assertThat(outputs.get(0).toFloatArray()).containsExactly(1f, 2f, 3f);
        }

        @Test
        @DisplayName("null inputs → NullPointerException")
        void nullInputs() {
            assertThatNullPointerException().isThrownBy(() ->
                    op.forward(null, OpAttribute.empty()));
        }

        @Test
        @DisplayName("empty inputs → OpExecutionException")
        void emptyInputs() {
            assertThatThrownBy(() -> op.forward(List.of(), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }

    // ==================== AddOp ====================

    @Nested
    @DisplayName("AddOp — 逐元素加法算子")
    class AddOpTest {

        private final AddOp op = new AddOp();

        @Test
        @DisplayName("[1, 2] + [3, 4] = [4, 6]")
        void addBasic() {
            Tensor a = Tensor.fromFloat(new float[]{1f, 2f}, Shape.of(2));
            Tensor b = Tensor.fromFloat(new float[]{3f, 4f}, Shape.of(2));
            List<Tensor> outputs = op.forward(List.of(a, b), OpAttribute.empty());

            assertThat(outputs).hasSize(1);
            assertThat(outputs.get(0).toFloatArray()).containsExactly(4f, 6f);
        }

        @Test
        @DisplayName("null inputs → NullPointerException")
        void nullInputs() {
            assertThatNullPointerException().isThrownBy(() ->
                    op.forward(null, OpAttribute.empty()));
        }

        @Test
        @DisplayName("insufficient inputs → OpExecutionException")
        void insufficientInputs() {
            Tensor a = Tensor.fromFloat(new float[]{1f}, Shape.of(1));
            assertThatThrownBy(() -> op.forward(List.of(a), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }

    // ==================== ConcatOp ====================

    @Nested
    @DisplayName("ConcatOp — 拼接算子")
    class ConcatOpTest {

        private final ConcatOp op = new ConcatOp();

        @Test
        @DisplayName("two [1, 3] along axis=0 → [2, 3]")
        void concatAxis0() {
            Tensor a = Tensor.fromFloat(new float[]{1f, 2f, 3f}, Shape.of(1, 3));
            Tensor b = Tensor.fromFloat(new float[]{4f, 5f, 6f}, Shape.of(1, 3));
            OpAttribute attrs = OpAttribute.builder().put("axis", 0).build();
            List<Tensor> outputs = op.forward(List.of(a, b), attrs);

            assertThat(outputs).hasSize(1);
            Tensor out = outputs.get(0);
            assertThat(out.shape().rank()).isEqualTo(2);
            assertThat(out.shape().dim(0)).isEqualTo(2);
            assertThat(out.shape().dim(1)).isEqualTo(3);
            assertThat(out.toFloatArray()).containsExactly(1f, 2f, 3f, 4f, 5f, 6f);
        }

        @Test
        @DisplayName("concat along axis=1")
        void concatAxis1() {
            Tensor a = Tensor.fromFloat(new float[]{1f, 2f}, Shape.of(2, 1));
            Tensor b = Tensor.fromFloat(new float[]{3f, 4f}, Shape.of(2, 1));
            OpAttribute attrs = OpAttribute.builder().put("axis", 1).build();
            List<Tensor> outputs = op.forward(List.of(a, b), attrs);

            Tensor out = outputs.get(0);
            assertThat(out.shape().dim(0)).isEqualTo(2);
            assertThat(out.shape().dim(1)).isEqualTo(2);
            assertThat(out.toFloatArray()).containsExactly(1f, 3f, 2f, 4f);
        }

        @Test
        @DisplayName("null inputs → NullPointerException")
        void nullInputs() {
            assertThatNullPointerException().isThrownBy(() ->
                    op.forward(null, OpAttribute.empty()));
        }

        @Test
        @DisplayName("empty inputs → OpExecutionException")
        void emptyInputs() {
            assertThatThrownBy(() -> op.forward(List.of(), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }
}
