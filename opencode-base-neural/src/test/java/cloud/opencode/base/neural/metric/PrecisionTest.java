package cloud.opencode.base.neural.metric;

import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class PrecisionTest {

    private final Precision precision = new Precision();

    @Nested
    class Compute {

        @Test
        void shouldReturn1ForPerfectPrecision() {
            // predicted 1 where target is 1 => TP=2, FP=0
            Tensor predicted = Tensor.fromFloat(new float[]{1, 0, 1, 0}, Shape.of(4));
            Tensor target = Tensor.fromFloat(new float[]{1, 0, 1, 0}, Shape.of(4));

            Tensor result = precision.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(1.0f, within(1e-6f));
        }

        @Test
        void shouldReturn0ForAllFalsePositives() {
            // predicted 1 where target is 0 => TP=0, FP=2
            Tensor predicted = Tensor.fromFloat(new float[]{1, 1}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{0, 0}, Shape.of(2));

            Tensor result = precision.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(1e-6f));
        }

        @Test
        void shouldReturn0WhenNoPredictedPositives() {
            // No positives predicted => TP + FP = 0 => return 0
            Tensor predicted = Tensor.fromFloat(new float[]{0, 0}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{1, 1}, Shape.of(2));

            Tensor result = precision.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(1e-6f));
        }

        @Test
        void shouldComputeCorrectPrecision() {
            // TP=1, FP=1 => precision = 0.5
            Tensor predicted = Tensor.fromFloat(new float[]{1, 1, 0, 0}, Shape.of(4));
            Tensor target = Tensor.fromFloat(new float[]{1, 0, 1, 0}, Shape.of(4));

            Tensor result = precision.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.5f, within(1e-6f));
        }

        @Test
        void shouldHandleSingleElement() {
            Tensor predicted = Tensor.fromFloat(new float[]{1}, Shape.of(1));
            Tensor target = Tensor.fromFloat(new float[]{1}, Shape.of(1));

            Tensor result = precision.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(1.0f, within(1e-6f));
        }
    }

    @Nested
    class Validation {

        @Test
        void shouldThrowForNullPredicted() {
            Tensor target = Tensor.fromFloat(new float[]{0}, Shape.of(1));

            assertThatThrownBy(() -> precision.compute(null, target))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        void shouldThrowForNullTarget() {
            Tensor predicted = Tensor.fromFloat(new float[]{0}, Shape.of(1));

            assertThatThrownBy(() -> precision.compute(predicted, null))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        void shouldThrowForShapeMismatch() {
            Tensor predicted = Tensor.fromFloat(new float[]{0, 1}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{0, 1, 1}, Shape.of(3));

            assertThatThrownBy(() -> precision.compute(predicted, target))
                    .isInstanceOf(NeuralException.class);
        }
    }

    @Nested
    class MetricInterface {

        @Test
        void shouldBeAssignableToMetric() {
            Metric metric = new Precision();
            assertThat(metric).isInstanceOf(Metric.class);
        }
    }
}
