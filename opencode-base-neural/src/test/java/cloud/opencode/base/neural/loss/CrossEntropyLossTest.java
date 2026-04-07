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
 * Tests for {@link CrossEntropyLoss}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("CrossEntropyLoss — 分类交叉熵损失函数")
class CrossEntropyLossTest {

    private static final float EPSILON = 1e-5f;
    private final CrossEntropyLoss loss = new CrossEntropyLoss();

    @Nested
    @DisplayName("compute")
    class Compute {

        @Test
        @DisplayName("should compute correct loss for single sample")
        void shouldComputeForSingleSample() {
            // Single sample, 3 classes, predicted [0.7, 0.2, 0.1], target class 0
            Tensor pred = Tensor.fromFloat(new float[]{0.7f, 0.2f, 0.1f}, Shape.of(1, 3));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 0.0f, 0.0f}, Shape.of(1, 3));

            Tensor result = loss.compute(pred, target);

            // -log(0.7) ≈ 0.3567
            float expected = (float) -Math.log(0.7);
            assertThat(result.toFloatArray()[0]).isCloseTo(expected, within(EPSILON));
        }

        @Test
        @DisplayName("should compute mean over batch")
        void shouldComputeMeanOverBatch() {
            // Batch of 2, 3 classes
            Tensor pred = Tensor.fromFloat(new float[]{
                    0.7f, 0.2f, 0.1f,  // sample 1
                    0.1f, 0.8f, 0.1f   // sample 2
            }, Shape.of(2, 3));
            Tensor target = Tensor.fromFloat(new float[]{
                    1.0f, 0.0f, 0.0f,  // sample 1: class 0
                    0.0f, 1.0f, 0.0f   // sample 2: class 1
            }, Shape.of(2, 3));

            Tensor result = loss.compute(pred, target);

            // loss1 = -log(0.7), loss2 = -log(0.8)
            // mean = (-log(0.7) + -log(0.8)) / 2
            float expected = (float) ((-Math.log(0.7) + -Math.log(0.8)) / 2.0);
            assertThat(result.toFloatArray()[0]).isCloseTo(expected, within(EPSILON));
        }

        @Test
        @DisplayName("should return near-zero for perfect predictions")
        void shouldReturnNearZeroForPerfectPredictions() {
            Tensor pred = Tensor.fromFloat(new float[]{0.9999f, 0.0001f}, Shape.of(1, 2));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 0.0f}, Shape.of(1, 2));

            Tensor result = loss.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isLessThan(0.001f);
        }

        @Test
        @DisplayName("should handle extreme predictions with epsilon clamping")
        void shouldHandleExtremePredictions() {
            Tensor pred = Tensor.fromFloat(new float[]{0.0f, 1.0f}, Shape.of(1, 2));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 0.0f}, Shape.of(1, 2));

            Tensor result = loss.compute(pred, target);

            float lossValue = result.toFloatArray()[0];
            assertThat(lossValue).isFinite();
            assertThat(lossValue).isGreaterThan(0.0f);
        }

        @Test
        @DisplayName("should handle soft labels")
        void shouldHandleSoftLabels() {
            Tensor pred = Tensor.fromFloat(new float[]{0.6f, 0.4f}, Shape.of(1, 2));
            Tensor target = Tensor.fromFloat(new float[]{0.8f, 0.2f}, Shape.of(1, 2));

            Tensor result = loss.compute(pred, target);

            // -(0.8*log(0.6) + 0.2*log(0.4))
            float expected = (float) -(0.8 * Math.log(0.6) + 0.2 * Math.log(0.4));
            assertThat(result.toFloatArray()[0]).isCloseTo(expected, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("should throw on null predicted")
        void shouldThrowOnNullPredicted() {
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 0.0f}, Shape.of(1, 2));

            assertThatThrownBy(() -> loss.compute(null, target))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        @DisplayName("should throw on null target")
        void shouldThrowOnNullTarget() {
            Tensor pred = Tensor.fromFloat(new float[]{0.5f, 0.5f}, Shape.of(1, 2));

            assertThatThrownBy(() -> loss.compute(pred, null))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        @DisplayName("should throw on shape mismatch")
        void shouldThrowOnShapeMismatch() {
            Tensor pred = Tensor.fromFloat(new float[]{0.5f, 0.5f}, Shape.of(1, 2));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 0.0f, 0.0f}, Shape.of(1, 3));

            assertThatThrownBy(() -> loss.compute(pred, target))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        @DisplayName("should throw on 1D tensor input")
        void shouldThrowOn1DInput() {
            Tensor pred = Tensor.fromFloat(new float[]{0.5f, 0.5f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 0.0f}, Shape.of(2));

            assertThatThrownBy(() -> loss.compute(pred, target))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("2D");
        }

        @Test
        @DisplayName("should throw on 3D tensor input")
        void shouldThrowOn3DInput() {
            Tensor pred = Tensor.fromFloat(new float[]{0.5f, 0.5f, 0.5f, 0.5f}, Shape.of(1, 2, 2));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 0.0f, 0.0f, 1.0f}, Shape.of(1, 2, 2));

            assertThatThrownBy(() -> loss.compute(pred, target))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("2D");
        }
    }
}
