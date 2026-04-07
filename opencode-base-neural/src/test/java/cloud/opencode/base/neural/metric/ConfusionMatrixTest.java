package cloud.opencode.base.neural.metric;

import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class ConfusionMatrixTest {

    @Nested
    class ComputeMatrix {

        @Test
        void shouldComputePerfectMatrix() {
            Tensor predicted = Tensor.fromFloat(new float[]{0, 1, 2}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{0, 1, 2}, Shape.of(3));

            int[][] matrix = ConfusionMatrix.compute(predicted, target, 3);

            // Diagonal should be all 1s
            assertThat(matrix[0][0]).isEqualTo(1);
            assertThat(matrix[1][1]).isEqualTo(1);
            assertThat(matrix[2][2]).isEqualTo(1);
            // Off-diagonal should be 0
            assertThat(matrix[0][1]).isEqualTo(0);
            assertThat(matrix[1][0]).isEqualTo(0);
        }

        @Test
        void shouldComputeWithMisclassifications() {
            // target=0 predicted=1, target=1 predicted=0, target=2 predicted=2
            Tensor predicted = Tensor.fromFloat(new float[]{1, 0, 2}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{0, 1, 2}, Shape.of(3));

            int[][] matrix = ConfusionMatrix.compute(predicted, target, 3);

            // matrix[actual][predicted]
            assertThat(matrix[0][1]).isEqualTo(1); // actual=0, predicted=1
            assertThat(matrix[1][0]).isEqualTo(1); // actual=1, predicted=0
            assertThat(matrix[2][2]).isEqualTo(1); // actual=2, predicted=2
        }

        @Test
        void shouldHandleBinaryClassification() {
            Tensor predicted = Tensor.fromFloat(new float[]{1, 1, 0, 0}, Shape.of(4));
            Tensor target = Tensor.fromFloat(new float[]{1, 0, 1, 0}, Shape.of(4));

            int[][] matrix = ConfusionMatrix.compute(predicted, target, 2);

            assertThat(matrix[0][0]).isEqualTo(1); // TN
            assertThat(matrix[0][1]).isEqualTo(1); // FP
            assertThat(matrix[1][0]).isEqualTo(1); // FN
            assertThat(matrix[1][1]).isEqualTo(1); // TP
        }

        @Test
        void shouldHandleMultipleSamplesPerCell() {
            Tensor predicted = Tensor.fromFloat(new float[]{0, 0, 0, 1, 1}, Shape.of(5));
            Tensor target = Tensor.fromFloat(new float[]{0, 0, 1, 1, 0}, Shape.of(5));

            int[][] matrix = ConfusionMatrix.compute(predicted, target, 2);

            assertThat(matrix[0][0]).isEqualTo(2); // TN
            assertThat(matrix[0][1]).isEqualTo(1); // FP
            assertThat(matrix[1][0]).isEqualTo(1); // FN
            assertThat(matrix[1][1]).isEqualTo(1); // TP
        }
    }

    @Nested
    class AccuracyFromMatrix {

        @Test
        void shouldReturn1ForPerfectMatrix() {
            int[][] matrix = {{3, 0}, {0, 2}};

            assertThat(ConfusionMatrix.accuracy(matrix)).isCloseTo(1.0f, within(1e-6f));
        }

        @Test
        void shouldReturn0ForAllWrong() {
            int[][] matrix = {{0, 3}, {2, 0}};

            assertThat(ConfusionMatrix.accuracy(matrix)).isCloseTo(0.0f, within(1e-6f));
        }

        @Test
        void shouldComputeCorrectAccuracy() {
            int[][] matrix = {{2, 1}, {1, 2}};
            // diagonal = 4, total = 6 => 4/6 = 2/3
            assertThat(ConfusionMatrix.accuracy(matrix)).isCloseTo(2.0f / 3.0f, within(1e-6f));
        }

        @Test
        void shouldReturn0ForEmptyMatrix() {
            int[][] matrix = {{0, 0}, {0, 0}};

            assertThat(ConfusionMatrix.accuracy(matrix)).isCloseTo(0.0f, within(1e-6f));
        }
    }

    @Nested
    class PrecisionFromMatrix {

        @Test
        void shouldComputeCorrectPrecision() {
            // class 1: TP=2, FP=1 (column sum for class 1 = 3)
            int[][] matrix = {{2, 1}, {1, 2}};

            assertThat(ConfusionMatrix.precision(matrix, 1)).isCloseTo(2.0f / 3.0f, within(1e-6f));
        }

        @Test
        void shouldReturn0WhenColumnSumIsZero() {
            // class 1: no predictions for class 1
            int[][] matrix = {{3, 0}, {2, 0}};

            assertThat(ConfusionMatrix.precision(matrix, 1)).isCloseTo(0.0f, within(1e-6f));
        }
    }

    @Nested
    class RecallFromMatrix {

        @Test
        void shouldComputeCorrectRecall() {
            // class 1: TP=2, FN=1 (row sum for class 1 = 3)
            int[][] matrix = {{2, 1}, {1, 2}};

            assertThat(ConfusionMatrix.recall(matrix, 1)).isCloseTo(2.0f / 3.0f, within(1e-6f));
        }

        @Test
        void shouldReturn0WhenRowSumIsZero() {
            // class 1: no actual class 1 samples
            int[][] matrix = {{3, 2}, {0, 0}};

            assertThat(ConfusionMatrix.recall(matrix, 1)).isCloseTo(0.0f, within(1e-6f));
        }
    }

    @Nested
    class F1ScoreFromMatrix {

        @Test
        void shouldComputeCorrectF1() {
            // class 1: precision=2/3, recall=2/3
            // F1 = 2 * (2/3) * (2/3) / ((2/3) + (2/3)) = 2/3
            int[][] matrix = {{2, 1}, {1, 2}};

            assertThat(ConfusionMatrix.f1Score(matrix, 1)).isCloseTo(2.0f / 3.0f, within(1e-6f));
        }

        @Test
        void shouldReturn0WhenPrecisionAndRecallAreZero() {
            int[][] matrix = {{3, 0}, {0, 0}};

            assertThat(ConfusionMatrix.f1Score(matrix, 1)).isCloseTo(0.0f, within(1e-6f));
        }
    }

    @Nested
    class Validation {

        @Test
        void shouldThrowForNullPredicted() {
            Tensor target = Tensor.fromFloat(new float[]{0}, Shape.of(1));

            assertThatThrownBy(() -> ConfusionMatrix.compute(null, target, 2))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        void shouldThrowForNullTarget() {
            Tensor predicted = Tensor.fromFloat(new float[]{0}, Shape.of(1));

            assertThatThrownBy(() -> ConfusionMatrix.compute(predicted, null, 2))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        void shouldThrowForNonPositiveNumClasses() {
            Tensor predicted = Tensor.fromFloat(new float[]{0}, Shape.of(1));
            Tensor target = Tensor.fromFloat(new float[]{0}, Shape.of(1));

            assertThatThrownBy(() -> ConfusionMatrix.compute(predicted, target, 0))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("numClasses");
        }

        @Test
        void shouldThrowForLabelOutOfRange() {
            Tensor predicted = Tensor.fromFloat(new float[]{3}, Shape.of(1));
            Tensor target = Tensor.fromFloat(new float[]{0}, Shape.of(1));

            assertThatThrownBy(() -> ConfusionMatrix.compute(predicted, target, 2))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("out of range");
        }

        @Test
        void shouldThrowForNegativeLabel() {
            Tensor predicted = Tensor.fromFloat(new float[]{0}, Shape.of(1));
            Tensor target = Tensor.fromFloat(new float[]{-1}, Shape.of(1));

            assertThatThrownBy(() -> ConfusionMatrix.compute(predicted, target, 2))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("out of range");
        }

        @Test
        void shouldThrowForShapeMismatch() {
            Tensor predicted = Tensor.fromFloat(new float[]{0, 1}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{0, 1, 2}, Shape.of(3));

            assertThatThrownBy(() -> ConfusionMatrix.compute(predicted, target, 3))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        void shouldThrowForNullMatrix() {
            assertThatThrownBy(() -> ConfusionMatrix.accuracy(null))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        void shouldThrowForEmptyMatrix() {
            assertThatThrownBy(() -> ConfusionMatrix.accuracy(new int[0][0]))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        void shouldThrowForInvalidClassIdx() {
            int[][] matrix = {{1, 0}, {0, 1}};

            assertThatThrownBy(() -> ConfusionMatrix.precision(matrix, 2))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("out of range");
        }

        @Test
        void shouldThrowForNegativeClassIdx() {
            int[][] matrix = {{1, 0}, {0, 1}};

            assertThatThrownBy(() -> ConfusionMatrix.recall(matrix, -1))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("out of range");
        }
    }
}
