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
 * Tests for {@link HuberLoss}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("HuberLoss — Huber损失函数（平滑L1）")
class HuberLossTest {

    private static final float EPSILON = 1e-6f;

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("should use default delta of 1.0")
        void shouldUseDefaultDelta() {
            HuberLoss loss = new HuberLoss();
            assertThat(loss.delta()).isCloseTo(1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("should accept custom delta")
        void shouldAcceptCustomDelta() {
            HuberLoss loss = new HuberLoss(0.5f);
            assertThat(loss.delta()).isCloseTo(0.5f, within(EPSILON));
        }

        @Test
        @DisplayName("should throw on zero delta")
        void shouldThrowOnZeroDelta() {
            assertThatThrownBy(() -> new HuberLoss(0.0f))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("should throw on negative delta")
        void shouldThrowOnNegativeDelta() {
            assertThatThrownBy(() -> new HuberLoss(-1.0f))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("should throw on NaN delta")
        void shouldThrowOnNaNDelta() {
            assertThatThrownBy(() -> new HuberLoss(Float.NaN))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        @DisplayName("should throw on infinite delta")
        void shouldThrowOnInfiniteDelta() {
            assertThatThrownBy(() -> new HuberLoss(Float.POSITIVE_INFINITY))
                    .isInstanceOf(NeuralException.class);
        }
    }

    @Nested
    @DisplayName("compute")
    class Compute {

        @Test
        @DisplayName("should return zero for identical tensors")
        void shouldReturnZeroForIdenticalTensors() {
            HuberLoss loss = new HuberLoss();
            Tensor pred = Tensor.fromFloat(new float[]{1.0f, 2.0f, 3.0f}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{1.0f, 2.0f, 3.0f}, Shape.of(3));

            Tensor result = loss.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(EPSILON));
        }

        @Test
        @DisplayName("should behave like MSE for small errors (within delta)")
        void shouldBehaveLikeMseForSmallErrors() {
            HuberLoss loss = new HuberLoss(1.0f);
            // diff = 0.5, which is <= delta=1.0, so loss = 0.5 * 0.5² = 0.125
            Tensor pred = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));
            Tensor target = Tensor.fromFloat(new float[]{1.5f}, Shape.of(1));

            Tensor result = loss.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.125f, within(EPSILON));
        }

        @Test
        @DisplayName("should behave like MAE for large errors (beyond delta)")
        void shouldBehaveLikeMaeForLargeErrors() {
            HuberLoss loss = new HuberLoss(1.0f);
            // diff = 3.0, which is > delta=1.0
            // loss = delta * (|diff| - 0.5 * delta) = 1.0 * (3.0 - 0.5) = 2.5
            Tensor pred = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));
            Tensor target = Tensor.fromFloat(new float[]{4.0f}, Shape.of(1));

            Tensor result = loss.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(2.5f, within(EPSILON));
        }

        @Test
        @DisplayName("should handle mixed small and large errors")
        void shouldHandleMixedErrors() {
            HuberLoss loss = new HuberLoss(1.0f);
            // diff1 = 0.5 (small): 0.5 * 0.25 = 0.125
            // diff2 = 3.0 (large): 1.0 * (3.0 - 0.5) = 2.5
            // mean = (0.125 + 2.5) / 2 = 1.3125
            Tensor pred = Tensor.fromFloat(new float[]{1.0f, 1.0f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{1.5f, 4.0f}, Shape.of(2));

            Tensor result = loss.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(1.3125f, within(EPSILON));
        }

        @Test
        @DisplayName("should respect custom delta")
        void shouldRespectCustomDelta() {
            HuberLoss loss = new HuberLoss(0.5f);
            // diff = 1.0, delta = 0.5
            // 1.0 > 0.5, so linear region: 0.5 * (1.0 - 0.25) = 0.375
            Tensor pred = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));
            Tensor target = Tensor.fromFloat(new float[]{2.0f}, Shape.of(1));

            Tensor result = loss.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.375f, within(EPSILON));
        }

        @Test
        @DisplayName("should be continuous at delta boundary")
        void shouldBeContinuousAtBoundary() {
            HuberLoss loss = new HuberLoss(1.0f);
            // At exactly delta, quadratic: 0.5 * 1² = 0.5
            // Slightly beyond: delta * (1.0001 - 0.5) ≈ 0.5001 (should be close to 0.5)
            Tensor predAt = Tensor.fromFloat(new float[]{0.0f}, Shape.of(1));
            Tensor targetAt = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));

            Tensor predBeyond = Tensor.fromFloat(new float[]{0.0f}, Shape.of(1));
            Tensor targetBeyond = Tensor.fromFloat(new float[]{1.0001f}, Shape.of(1));

            float lossAt = loss.compute(predAt, targetAt).toFloatArray()[0];
            float lossBeyond = loss.compute(predBeyond, targetBeyond).toFloatArray()[0];

            assertThat(lossBeyond).isCloseTo(lossAt, within(0.001f));
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("should throw on null predicted")
        void shouldThrowOnNullPredicted() {
            HuberLoss loss = new HuberLoss();
            Tensor target = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));

            assertThatThrownBy(() -> loss.compute(null, target))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        @DisplayName("should throw on shape mismatch")
        void shouldThrowOnShapeMismatch() {
            HuberLoss loss = new HuberLoss();
            Tensor pred = Tensor.fromFloat(new float[]{1.0f, 2.0f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));

            assertThatThrownBy(() -> loss.compute(pred, target))
                    .isInstanceOf(NeuralException.class);
        }
    }

    @Nested
    @DisplayName("LossFunction interface")
    class LossFunctionInterface {

        @Test
        @DisplayName("should be usable as LossFunction")
        void shouldBeUsableAsLossFunction() {
            LossFunction fn = new HuberLoss(1.0f);
            Tensor pred = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));
            Tensor target = Tensor.fromFloat(new float[]{1.5f}, Shape.of(1));

            Tensor result = fn.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isGreaterThan(0.0f);
        }
    }
}
