package cloud.opencode.base.neural.metric;

import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class AccuracyTest {

    private final Accuracy accuracy = new Accuracy();

    @Nested
    class Compute1D {

        @Test
        void shouldReturn1ForPerfectPredictions() {
            Tensor predicted = Tensor.fromFloat(new float[]{0, 1, 2, 3}, Shape.of(4));
            Tensor target = Tensor.fromFloat(new float[]{0, 1, 2, 3}, Shape.of(4));

            Tensor result = accuracy.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(1.0f, within(1e-6f));
        }

        @Test
        void shouldReturn0ForAllWrong() {
            Tensor predicted = Tensor.fromFloat(new float[]{0, 0, 0, 0}, Shape.of(4));
            Tensor target = Tensor.fromFloat(new float[]{1, 1, 1, 1}, Shape.of(4));

            Tensor result = accuracy.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(1e-6f));
        }

        @Test
        void shouldReturn0Point5ForHalfCorrect() {
            Tensor predicted = Tensor.fromFloat(new float[]{0, 1, 0, 1}, Shape.of(4));
            Tensor target = Tensor.fromFloat(new float[]{0, 1, 1, 0}, Shape.of(4));

            Tensor result = accuracy.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.5f, within(1e-6f));
        }

        @Test
        void shouldHandleSingleElement() {
            Tensor predicted = Tensor.fromFloat(new float[]{2}, Shape.of(1));
            Tensor target = Tensor.fromFloat(new float[]{2}, Shape.of(1));

            Tensor result = accuracy.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(1.0f, within(1e-6f));
        }
    }

    @Nested
    class Compute2D {

        @Test
        void shouldArgmaxAndComputeAccuracy() {
            // 3 samples, 3 classes: predicted probabilities
            Tensor predicted = Tensor.fromFloat(new float[]{
                    0.1f, 0.8f, 0.1f,   // argmax = 1
                    0.7f, 0.2f, 0.1f,   // argmax = 0
                    0.1f, 0.1f, 0.8f    // argmax = 2
            }, Shape.of(3, 3));
            Tensor target = Tensor.fromFloat(new float[]{1, 0, 2}, Shape.of(3));

            Tensor result = accuracy.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(1.0f, within(1e-6f));
        }

        @Test
        void shouldReturn0ForAllWrongArgmax() {
            Tensor predicted = Tensor.fromFloat(new float[]{
                    0.1f, 0.8f, 0.1f,   // argmax = 1, target = 0
                    0.1f, 0.1f, 0.8f,   // argmax = 2, target = 1
            }, Shape.of(2, 3));
            Tensor target = Tensor.fromFloat(new float[]{0, 1}, Shape.of(2));

            Tensor result = accuracy.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(1e-6f));
        }

        @Test
        void shouldHandlePartialCorrect2D() {
            Tensor predicted = Tensor.fromFloat(new float[]{
                    0.9f, 0.1f,   // argmax = 0
                    0.3f, 0.7f,   // argmax = 1
                    0.6f, 0.4f,   // argmax = 0
                    0.2f, 0.8f    // argmax = 1
            }, Shape.of(4, 2));
            Tensor target = Tensor.fromFloat(new float[]{0, 1, 1, 0}, Shape.of(4));

            // correct: sample 0 (0==0), sample 1 (1==1) => 2/4 = 0.5
            Tensor result = accuracy.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.5f, within(1e-6f));
        }
    }

    @Nested
    class Validation {

        @Test
        void shouldThrowForNullPredicted() {
            Tensor target = Tensor.fromFloat(new float[]{0}, Shape.of(1));

            assertThatThrownBy(() -> accuracy.compute(null, target))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("predicted");
        }

        @Test
        void shouldThrowForNullTarget() {
            Tensor predicted = Tensor.fromFloat(new float[]{0}, Shape.of(1));

            assertThatThrownBy(() -> accuracy.compute(predicted, null))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("target");
        }

        @Test
        void shouldThrowForTargetRankNot1() {
            Tensor predicted = Tensor.fromFloat(new float[]{0, 1}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{0, 1}, Shape.of(1, 2));

            assertThatThrownBy(() -> accuracy.compute(predicted, target))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("1D");
        }

        @Test
        void shouldThrowFor3DPredicted() {
            Tensor predicted = Tensor.fromFloat(new float[]{0, 1}, Shape.of(1, 1, 2));
            Tensor target = Tensor.fromFloat(new float[]{0}, Shape.of(1));

            assertThatThrownBy(() -> accuracy.compute(predicted, target))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("rank");
        }

        @Test
        void shouldThrowForSizeMismatch() {
            Tensor predicted = Tensor.fromFloat(new float[]{0, 1, 2}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{0, 1}, Shape.of(2));

            assertThatThrownBy(() -> accuracy.compute(predicted, target))
                    .isInstanceOf(NeuralException.class);
        }
    }

    @Nested
    class MetricInterface {

        @Test
        void shouldBeAssignableToMetric() {
            Metric metric = new Accuracy();
            Tensor predicted = Tensor.fromFloat(new float[]{1, 0}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{1, 0}, Shape.of(2));

            Tensor result = metric.compute(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(1.0f, within(1e-6f));
        }
    }
}
