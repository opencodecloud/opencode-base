package cloud.opencode.base.neural.metric;

import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class BinaryAccuracyTest {

    @Nested
    class DefaultThreshold {

        private final BinaryAccuracy metric = new BinaryAccuracy();

        @Test
        void shouldReturn1ForPerfectPredictions() {
            Tensor predicted = Tensor.fromFloat(new float[]{0.9f, 0.1f, 0.8f, 0.2f}, Shape.of(4));
            Tensor target = Tensor.fromFloat(new float[]{1, 0, 1, 0}, Shape.of(4));

            Tensor result = metric.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(1.0f, within(1e-6f));
        }

        @Test
        void shouldReturn0ForAllWrong() {
            Tensor predicted = Tensor.fromFloat(new float[]{0.9f, 0.1f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{0, 1}, Shape.of(2));

            Tensor result = metric.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(1e-6f));
        }

        @Test
        void shouldReturn0Point5ForHalfCorrect() {
            Tensor predicted = Tensor.fromFloat(new float[]{0.9f, 0.9f, 0.1f, 0.1f}, Shape.of(4));
            Tensor target = Tensor.fromFloat(new float[]{1, 0, 0, 1}, Shape.of(4));

            Tensor result = metric.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.5f, within(1e-6f));
        }

        @Test
        void shouldClassifyExactThresholdAsPositive() {
            // 0.5 >= 0.5 threshold -> predicted as 1
            Tensor predicted = Tensor.fromFloat(new float[]{0.5f}, Shape.of(1));
            Tensor target = Tensor.fromFloat(new float[]{1}, Shape.of(1));

            Tensor result = metric.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(1.0f, within(1e-6f));
        }

        @Test
        void shouldHaveDefaultThreshold() {
            assertThat(metric.threshold()).isCloseTo(0.5f, within(1e-6f));
        }
    }

    @Nested
    class CustomThreshold {

        @Test
        void shouldUseCustomThreshold() {
            BinaryAccuracy metric = new BinaryAccuracy(0.7f);

            // 0.6 < 0.7 -> predicted 0, target 1 -> wrong
            Tensor predicted = Tensor.fromFloat(new float[]{0.6f, 0.8f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{1, 1}, Shape.of(2));

            Tensor result = metric.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.5f, within(1e-6f));
        }

        @Test
        void shouldReturnConfiguredThreshold() {
            BinaryAccuracy metric = new BinaryAccuracy(0.3f);
            assertThat(metric.threshold()).isCloseTo(0.3f, within(1e-6f));
        }
    }

    @Nested
    class Validation {

        @Test
        void shouldThrowForThresholdZero() {
            assertThatThrownBy(() -> new BinaryAccuracy(0.0f))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("Threshold");
        }

        @Test
        void shouldThrowForThresholdOne() {
            assertThatThrownBy(() -> new BinaryAccuracy(1.0f))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("Threshold");
        }

        @Test
        void shouldThrowForNaNThreshold() {
            assertThatThrownBy(() -> new BinaryAccuracy(Float.NaN))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("Threshold");
        }

        @Test
        void shouldThrowForNullPredicted() {
            BinaryAccuracy metric = new BinaryAccuracy();
            Tensor target = Tensor.fromFloat(new float[]{0}, Shape.of(1));

            assertThatThrownBy(() -> metric.compute(null, target))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        void shouldThrowForNullTarget() {
            BinaryAccuracy metric = new BinaryAccuracy();
            Tensor predicted = Tensor.fromFloat(new float[]{0}, Shape.of(1));

            assertThatThrownBy(() -> metric.compute(predicted, null))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        void shouldThrowForShapeMismatch() {
            BinaryAccuracy metric = new BinaryAccuracy();
            Tensor predicted = Tensor.fromFloat(new float[]{0.5f, 0.5f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{0, 1, 1}, Shape.of(3));

            assertThatThrownBy(() -> metric.compute(predicted, target))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        void shouldThrowFor2DPredicted() {
            BinaryAccuracy metric = new BinaryAccuracy();
            Tensor predicted = Tensor.fromFloat(new float[]{0.5f, 0.5f}, Shape.of(1, 2));
            Tensor target = Tensor.fromFloat(new float[]{0}, Shape.of(1));

            assertThatThrownBy(() -> metric.compute(predicted, target))
                    .isInstanceOf(NeuralException.class);
        }
    }
}
