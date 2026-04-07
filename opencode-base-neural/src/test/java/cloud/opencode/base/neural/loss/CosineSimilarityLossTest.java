package cloud.opencode.base.neural.loss;

import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link CosineSimilarityLoss}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("CosineSimilarityLoss — 余弦相似度损失函数")
class CosineSimilarityLossTest {

    private static final float EPSILON = 1e-5f;
    private final CosineSimilarityLoss loss = new CosineSimilarityLoss();

    @Nested
    @DisplayName("compute")
    class Compute {

        @Test
        @DisplayName("should return zero for identical vectors")
        void shouldReturnZeroForIdenticalVectors() {
            Tensor pred = Tensor.fromFloat(new float[]{1.0f, 2.0f, 3.0f}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 2.0f, 3.0f}, Shape.of(3));

            Tensor result = loss.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(EPSILON));
        }

        @Test
        @DisplayName("should return zero for parallel vectors with different magnitudes")
        void shouldReturnZeroForParallelVectors() {
            Tensor pred = Tensor.fromFloat(new float[]{2.0f, 4.0f, 6.0f}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 2.0f, 3.0f}, Shape.of(3));

            Tensor result = loss.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(EPSILON));
        }

        @Test
        @DisplayName("should return 1.0 for orthogonal vectors")
        void shouldReturnOneForOrthogonalVectors() {
            Tensor pred = Tensor.fromFloat(new float[]{1.0f, 0.0f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{0.0f, 1.0f}, Shape.of(2));

            Tensor result = loss.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("should return 2.0 for opposite vectors")
        void shouldReturnTwoForOppositeVectors() {
            Tensor pred = Tensor.fromFloat(new float[]{1.0f, 0.0f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{-1.0f, 0.0f}, Shape.of(2));

            Tensor result = loss.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(2.0f, within(EPSILON));
        }

        @Test
        @DisplayName("should return 1.0 for zero predicted vector")
        void shouldReturnOneForZeroPredicted() {
            Tensor pred = Tensor.fromFloat(new float[]{0.0f, 0.0f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 2.0f}, Shape.of(2));

            Tensor result = loss.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("should return 1.0 for zero target vector")
        void shouldReturnOneForZeroTarget() {
            Tensor pred = Tensor.fromFloat(new float[]{1.0f, 2.0f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{0.0f, 0.0f}, Shape.of(2));

            Tensor result = loss.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("should return 1.0 for both zero vectors")
        void shouldReturnOneForBothZeroVectors() {
            Tensor pred = Tensor.fromFloat(new float[]{0.0f, 0.0f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{0.0f, 0.0f}, Shape.of(2));

            Tensor result = loss.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("should handle single element vectors")
        void shouldHandleSingleElement() {
            Tensor pred = Tensor.fromFloat(new float[]{3.0f}, Shape.of(1));
            Tensor target = Tensor.fromFloat(new float[]{5.0f}, Shape.of(1));

            Tensor result = loss.compute(pred, target);

            // Both positive, same direction → cos_sim = 1.0, loss = 0.0
            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(EPSILON));
        }

        @Test
        @DisplayName("should compute known cosine similarity")
        void shouldComputeKnownValue() {
            // cos_sim(45°) = cos(π/4) ≈ 0.7071
            // vectors: (1, 0) and (1, 1) → cos = 1/√2
            Tensor pred = Tensor.fromFloat(new float[]{1.0f, 0.0f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 1.0f}, Shape.of(2));

            Tensor result = loss.compute(pred, target);

            float expected = (float) (1.0 - 1.0 / Math.sqrt(2.0));
            assertThat(result.toFloatArray()[0]).isCloseTo(expected, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("should throw on null predicted")
        void shouldThrowOnNullPredicted() {
            Tensor target = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));

            assertThatThrownBy(() -> loss.compute(null, target))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        @DisplayName("should throw on null target")
        void shouldThrowOnNullTarget() {
            Tensor pred = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));

            assertThatThrownBy(() -> loss.compute(pred, null))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        @DisplayName("should throw on shape mismatch")
        void shouldThrowOnShapeMismatch() {
            Tensor pred = Tensor.fromFloat(new float[]{1.0f, 2.0f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 2.0f, 3.0f}, Shape.of(3));

            assertThatThrownBy(() -> loss.compute(pred, target))
                    .isInstanceOf(NeuralException.class);
        }
    }
}
