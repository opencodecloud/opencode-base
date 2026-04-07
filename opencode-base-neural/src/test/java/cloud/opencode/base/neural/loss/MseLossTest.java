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
 * Tests for {@link MseLoss}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("MseLoss — 均方误差损失函数")
class MseLossTest {

    private static final float EPSILON = 1e-6f;
    private final MseLoss loss = new MseLoss();

    @Nested
    @DisplayName("compute")
    class Compute {

        @Test
        @DisplayName("should return zero for identical tensors")
        void shouldReturnZeroForIdenticalTensors() {
            Tensor pred = Tensor.fromFloat(new float[]{1.0f, 2.0f, 3.0f}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 2.0f, 3.0f}, Shape.of(3));

            Tensor result = loss.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(EPSILON));
            assertThat(result.shape()).isEqualTo(Shape.of(1));
        }

        @Test
        @DisplayName("should compute correct MSE for known values")
        void shouldComputeCorrectMse() {
            // diff = [0.5, 0.5, 0.5], diff² = [0.25, 0.25, 0.25], mean = 0.25
            Tensor pred = Tensor.fromFloat(new float[]{1.0f, 2.0f, 3.0f}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{1.5f, 2.5f, 3.5f}, Shape.of(3));

            Tensor result = loss.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.25f, within(EPSILON));
        }

        @Test
        @DisplayName("should compute MSE for single element")
        void shouldComputeForSingleElement() {
            Tensor pred = Tensor.fromFloat(new float[]{3.0f}, Shape.of(1));
            Tensor target = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));

            Tensor result = loss.compute(pred, target);

            // (3-1)² = 4.0
            assertThat(result.toFloatArray()[0]).isCloseTo(4.0f, within(EPSILON));
        }

        @Test
        @DisplayName("should handle 2D tensors")
        void shouldHandle2DTensors() {
            Tensor pred = Tensor.fromFloat(new float[]{1.0f, 2.0f, 3.0f, 4.0f}, Shape.of(2, 2));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 2.0f, 3.0f, 4.0f}, Shape.of(2, 2));

            Tensor result = loss.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(EPSILON));
        }

        @Test
        @DisplayName("should handle large differences")
        void shouldHandleLargeDifferences() {
            Tensor pred = Tensor.fromFloat(new float[]{100.0f, -100.0f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{-100.0f, 100.0f}, Shape.of(2));

            Tensor result = loss.compute(pred, target);

            // mean((200)² + (200)²) / 2 = 40000
            assertThat(result.toFloatArray()[0]).isCloseTo(40000.0f, within(1.0f));
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
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("should throw on null target")
        void shouldThrowOnNullTarget() {
            Tensor pred = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));

            assertThatThrownBy(() -> loss.compute(pred, null))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("should throw on shape mismatch")
        void shouldThrowOnShapeMismatch() {
            Tensor pred = Tensor.fromFloat(new float[]{1.0f, 2.0f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 2.0f, 3.0f}, Shape.of(3));

            assertThatThrownBy(() -> loss.compute(pred, target))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("Shape mismatch");
        }
    }

    @Nested
    @DisplayName("LossFunction interface")
    class LossFunctionInterface {

        @Test
        @DisplayName("should be usable as LossFunction")
        void shouldBeUsableAsLossFunction() {
            LossFunction fn = new MseLoss();
            Tensor pred = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));
            Tensor target = Tensor.fromFloat(new float[]{2.0f}, Shape.of(1));

            Tensor result = fn.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(1.0f, within(EPSILON));
        }
    }
}
