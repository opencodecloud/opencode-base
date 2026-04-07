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
 * Tests for {@link BinaryCrossEntropyLoss}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("BinaryCrossEntropyLoss — 二元交叉熵损失函数")
class BinaryCrossEntropyLossTest {

    private static final float EPSILON = 1e-5f;
    private final BinaryCrossEntropyLoss loss = new BinaryCrossEntropyLoss();

    @Nested
    @DisplayName("compute")
    class Compute {

        @Test
        @DisplayName("should return near-zero loss for perfect predictions")
        void shouldReturnNearZeroForPerfectPredictions() {
            Tensor pred = Tensor.fromFloat(new float[]{0.9999f, 0.0001f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 0.0f}, Shape.of(2));

            Tensor result = loss.compute(pred, target);

            // Perfect predictions should yield very low loss
            assertThat(result.toFloatArray()[0]).isLessThan(0.01f);
        }

        @Test
        @DisplayName("should return high loss for wrong predictions")
        void shouldReturnHighLossForWrongPredictions() {
            Tensor pred = Tensor.fromFloat(new float[]{0.1f, 0.9f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 0.0f}, Shape.of(2));

            Tensor result = loss.compute(pred, target);

            // Wrong predictions should yield high loss
            assertThat(result.toFloatArray()[0]).isGreaterThan(1.0f);
        }

        @Test
        @DisplayName("should compute known BCE value")
        void shouldComputeKnownBceValue() {
            // For p=0.5, t=1: -log(0.5) = 0.6931...
            // For p=0.5, t=0: -log(0.5) = 0.6931...
            // mean = 0.6931...
            Tensor pred = Tensor.fromFloat(new float[]{0.5f, 0.5f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 0.0f}, Shape.of(2));

            Tensor result = loss.compute(pred, target);

            float expected = (float) -Math.log(0.5);
            assertThat(result.toFloatArray()[0]).isCloseTo(expected, within(EPSILON));
        }

        @Test
        @DisplayName("should handle extreme predictions with epsilon clamping")
        void shouldHandleExtremePredictions() {
            // Should not produce NaN or Infinity due to log(0) protection
            Tensor pred = Tensor.fromFloat(new float[]{0.0f, 1.0f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 0.0f}, Shape.of(2));

            Tensor result = loss.compute(pred, target);

            float lossValue = result.toFloatArray()[0];
            assertThat(lossValue).isFinite();
            assertThat(lossValue).isGreaterThan(0.0f);
        }

        @Test
        @DisplayName("should handle single element")
        void shouldHandleSingleElement() {
            Tensor pred = Tensor.fromFloat(new float[]{0.7f}, Shape.of(1));
            Tensor target = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));

            Tensor result = loss.compute(pred, target);

            // -log(0.7) ≈ 0.3567
            float expected = (float) -Math.log(0.7);
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
            Tensor pred = Tensor.fromFloat(new float[]{0.5f}, Shape.of(1));

            assertThatThrownBy(() -> loss.compute(pred, null))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        @DisplayName("should throw on shape mismatch")
        void shouldThrowOnShapeMismatch() {
            Tensor pred = Tensor.fromFloat(new float[]{0.5f, 0.5f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));

            assertThatThrownBy(() -> loss.compute(pred, target))
                    .isInstanceOf(NeuralException.class);
        }
    }
}
