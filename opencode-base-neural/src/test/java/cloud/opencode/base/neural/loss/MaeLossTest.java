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
 * Tests for {@link MaeLoss}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("MaeLoss — 平均绝对误差损失函数")
class MaeLossTest {

    private static final float EPSILON = 1e-6f;
    private final MaeLoss loss = new MaeLoss();

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
        }

        @Test
        @DisplayName("should compute correct MAE for known values")
        void shouldComputeCorrectMae() {
            // |diff| = [0.5, 0.5, 0.5], mean = 0.5
            Tensor pred = Tensor.fromFloat(new float[]{1.0f, 2.0f, 3.0f}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{1.5f, 2.5f, 3.5f}, Shape.of(3));

            Tensor result = loss.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.5f, within(EPSILON));
        }

        @Test
        @DisplayName("should compute MAE for single element")
        void shouldComputeForSingleElement() {
            Tensor pred = Tensor.fromFloat(new float[]{3.0f}, Shape.of(1));
            Tensor target = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));

            Tensor result = loss.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(2.0f, within(EPSILON));
        }

        @Test
        @DisplayName("should handle negative differences symmetrically")
        void shouldHandleNegativeDifferences() {
            Tensor pred = Tensor.fromFloat(new float[]{1.0f, 3.0f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{3.0f, 1.0f}, Shape.of(2));

            Tensor result = loss.compute(pred, target);

            // mean(|1-3| + |3-1|) / 2 = mean(2+2)/2 = 2.0
            assertThat(result.toFloatArray()[0]).isCloseTo(2.0f, within(EPSILON));
        }

        @Test
        @DisplayName("should handle mixed positive and negative values")
        void shouldHandleMixedValues() {
            Tensor pred = Tensor.fromFloat(new float[]{-1.0f, 2.0f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, -2.0f}, Shape.of(2));

            Tensor result = loss.compute(pred, target);

            // mean(|-1-1| + |2-(-2)|) / 2 = mean(2+4)/2 = 3.0
            assertThat(result.toFloatArray()[0]).isCloseTo(3.0f, within(EPSILON));
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
