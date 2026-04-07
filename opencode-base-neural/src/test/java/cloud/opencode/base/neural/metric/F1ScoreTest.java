package cloud.opencode.base.neural.metric;

import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class F1ScoreTest {

    private final F1Score f1Score = new F1Score();

    @Nested
    class Compute {

        @Test
        void shouldReturn1ForPerfectPredictions() {
            Tensor predicted = Tensor.fromFloat(new float[]{1, 0, 1, 0}, Shape.of(4));
            Tensor target = Tensor.fromFloat(new float[]{1, 0, 1, 0}, Shape.of(4));

            Tensor result = f1Score.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(1.0f, within(1e-6f));
        }

        @Test
        void shouldReturn0WhenBothPrecisionAndRecallAreZero() {
            // predicted all 0, target all 0 => TP=0, FP=0, FN=0 => precision=0, recall=0 => F1=0
            Tensor predicted = Tensor.fromFloat(new float[]{0, 0}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{0, 0}, Shape.of(2));

            Tensor result = f1Score.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(1e-6f));
        }

        @Test
        void shouldComputeF1AsHarmonicMean() {
            // TP=1, FP=1, FN=1
            // precision = 1/2 = 0.5, recall = 1/2 = 0.5
            // F1 = 2 * 0.5 * 0.5 / (0.5 + 0.5) = 0.5
            Tensor predicted = Tensor.fromFloat(new float[]{1, 1, 0}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{1, 0, 1}, Shape.of(3));

            Tensor result = f1Score.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.5f, within(1e-6f));
        }

        @Test
        void shouldReturn0ForAllFalsePositives() {
            // TP=0, FP=2, FN=0 => precision=0, recall=0 => F1=0
            Tensor predicted = Tensor.fromFloat(new float[]{1, 1}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{0, 0}, Shape.of(2));

            Tensor result = f1Score.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(1e-6f));
        }

        @Test
        void shouldComputeCorrectF1Score() {
            // TP=2, FP=1, FN=1
            // precision = 2/3, recall = 2/3
            // F1 = 2 * (2/3) * (2/3) / ((2/3) + (2/3)) = 2/3
            Tensor predicted = Tensor.fromFloat(new float[]{1, 1, 1, 0}, Shape.of(4));
            Tensor target = Tensor.fromFloat(new float[]{1, 1, 0, 1}, Shape.of(4));

            Tensor result = f1Score.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(2.0f / 3.0f, within(1e-6f));
        }
    }

    @Nested
    class Validation {

        @Test
        void shouldThrowForNullPredicted() {
            Tensor target = Tensor.fromFloat(new float[]{0}, Shape.of(1));

            assertThatThrownBy(() -> f1Score.compute(null, target))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        void shouldThrowForNullTarget() {
            Tensor predicted = Tensor.fromFloat(new float[]{0}, Shape.of(1));

            assertThatThrownBy(() -> f1Score.compute(predicted, null))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        void shouldThrowForShapeMismatch() {
            Tensor predicted = Tensor.fromFloat(new float[]{0, 1}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{0, 1, 0}, Shape.of(3));

            assertThatThrownBy(() -> f1Score.compute(predicted, target))
                    .isInstanceOf(NeuralException.class);
        }
    }

    @Nested
    class MetricInterface {

        @Test
        void shouldBeAssignableToMetric() {
            Metric metric = new F1Score();
            assertThat(metric).isInstanceOf(Metric.class);
        }
    }
}
