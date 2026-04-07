package cloud.opencode.base.neural.metric;

import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class RecallTest {

    private final Recall recall = new Recall();

    @Nested
    class Compute {

        @Test
        void shouldReturn1ForPerfectRecall() {
            // All actual positives correctly predicted => TP=2, FN=0
            Tensor predicted = Tensor.fromFloat(new float[]{1, 1, 0}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{1, 1, 0}, Shape.of(3));

            Tensor result = recall.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(1.0f, within(1e-6f));
        }

        @Test
        void shouldReturn0ForAllFalseNegatives() {
            // All actual positives missed => TP=0, FN=2
            Tensor predicted = Tensor.fromFloat(new float[]{0, 0}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{1, 1}, Shape.of(2));

            Tensor result = recall.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(1e-6f));
        }

        @Test
        void shouldReturn0WhenNoActualPositives() {
            // No actual positives => TP + FN = 0 => return 0
            Tensor predicted = Tensor.fromFloat(new float[]{1, 1}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{0, 0}, Shape.of(2));

            Tensor result = recall.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(1e-6f));
        }

        @Test
        void shouldComputeCorrectRecall() {
            // TP=1, FN=1 => recall = 0.5
            Tensor predicted = Tensor.fromFloat(new float[]{1, 0, 0, 0}, Shape.of(4));
            Tensor target = Tensor.fromFloat(new float[]{1, 1, 0, 0}, Shape.of(4));

            Tensor result = recall.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.5f, within(1e-6f));
        }

        @Test
        void shouldHandleSingleElement() {
            Tensor predicted = Tensor.fromFloat(new float[]{1}, Shape.of(1));
            Tensor target = Tensor.fromFloat(new float[]{1}, Shape.of(1));

            Tensor result = recall.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(1.0f, within(1e-6f));
        }
    }

    @Nested
    class Validation {

        @Test
        void shouldThrowForNullPredicted() {
            Tensor target = Tensor.fromFloat(new float[]{0}, Shape.of(1));

            assertThatThrownBy(() -> recall.compute(null, target))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        void shouldThrowForNullTarget() {
            Tensor predicted = Tensor.fromFloat(new float[]{0}, Shape.of(1));

            assertThatThrownBy(() -> recall.compute(predicted, null))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        void shouldThrowForShapeMismatch() {
            Tensor predicted = Tensor.fromFloat(new float[]{0, 1}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{0, 1, 1}, Shape.of(3));

            assertThatThrownBy(() -> recall.compute(predicted, target))
                    .isInstanceOf(NeuralException.class);
        }
    }

    @Nested
    class MetricInterface {

        @Test
        void shouldBeAssignableToMetric() {
            Metric metric = new Recall();
            assertThat(metric).isInstanceOf(Metric.class);
        }
    }
}
