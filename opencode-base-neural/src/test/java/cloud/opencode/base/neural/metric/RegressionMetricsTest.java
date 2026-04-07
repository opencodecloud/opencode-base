package cloud.opencode.base.neural.metric;

import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class RegressionMetricsTest {

    @Nested
    class Mse {

        @Test
        void shouldReturn0ForPerfectPredictions() {
            Tensor predicted = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));

            Tensor result = RegressionMetrics.mse(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(1e-6f));
        }

        @Test
        void shouldComputeCorrectMse() {
            // errors: [1, -1, 2], squared: [1, 1, 4], mean = 6/3 = 2
            Tensor predicted = Tensor.fromFloat(new float[]{2, 1, 5}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));

            Tensor result = RegressionMetrics.mse(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(2.0f, within(1e-6f));
        }

        @Test
        void shouldHandleSingleElement() {
            Tensor predicted = Tensor.fromFloat(new float[]{3.0f}, Shape.of(1));
            Tensor target = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));

            Tensor result = RegressionMetrics.mse(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(4.0f, within(1e-6f));
        }

        @Test
        void shouldWork2D() {
            Tensor predicted = Tensor.fromFloat(new float[]{1, 2, 3, 4}, Shape.of(2, 2));
            Tensor target = Tensor.fromFloat(new float[]{1, 2, 3, 4}, Shape.of(2, 2));

            Tensor result = RegressionMetrics.mse(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(1e-6f));
        }
    }

    @Nested
    class Mae {

        @Test
        void shouldReturn0ForPerfectPredictions() {
            Tensor predicted = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));

            Tensor result = RegressionMetrics.mae(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(1e-6f));
        }

        @Test
        void shouldComputeCorrectMae() {
            // errors: |1|, |-1|, |2| = 1, 1, 2, mean = 4/3
            Tensor predicted = Tensor.fromFloat(new float[]{2, 1, 5}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));

            Tensor result = RegressionMetrics.mae(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(4.0f / 3.0f, within(1e-5f));
        }

        @Test
        void shouldHandleNegativeErrors() {
            Tensor predicted = Tensor.fromFloat(new float[]{0, 0}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{3, -3}, Shape.of(2));

            Tensor result = RegressionMetrics.mae(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(3.0f, within(1e-6f));
        }
    }

    @Nested
    class Rmse {

        @Test
        void shouldReturn0ForPerfectPredictions() {
            Tensor predicted = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));

            Tensor result = RegressionMetrics.rmse(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(1e-6f));
        }

        @Test
        void shouldComputeCorrectRmse() {
            // MSE = 2, RMSE = sqrt(2) ≈ 1.4142
            Tensor predicted = Tensor.fromFloat(new float[]{2, 1, 5}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));

            Tensor result = RegressionMetrics.rmse(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo((float) Math.sqrt(2.0), within(1e-5f));
        }

        @Test
        void shouldBeSqrtOfMse() {
            Tensor predicted = Tensor.fromFloat(new float[]{1.5f, 2.5f, 3.5f, 4.5f}, Shape.of(4));
            Tensor target = Tensor.fromFloat(new float[]{1, 2, 3, 4}, Shape.of(4));

            float mse = RegressionMetrics.mse(predicted, target).toFloatArray()[0];
            float rmse = RegressionMetrics.rmse(predicted, target).toFloatArray()[0];

            assertThat(rmse).isCloseTo((float) Math.sqrt(mse), within(1e-5f));
        }
    }

    @Nested
    class RSquared {

        @Test
        void shouldReturn1ForPerfectPredictions() {
            Tensor predicted = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));

            Tensor result = RegressionMetrics.rSquared(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(1.0f, within(1e-6f));
        }

        @Test
        void shouldReturn0WhenPredictingMean() {
            // If predicted = mean(target) for all, then SS_res = SS_tot => R^2 = 0
            Tensor predicted = Tensor.fromFloat(new float[]{2, 2, 2}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));

            Tensor result = RegressionMetrics.rSquared(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(1e-5f));
        }

        @Test
        void shouldReturn0WhenAllTargetsAreIdentical() {
            // SS_tot = 0 => return 0
            Tensor predicted = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{5, 5, 5}, Shape.of(3));

            Tensor result = RegressionMetrics.rSquared(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.0f, within(1e-6f));
        }

        @Test
        void shouldReturnNegativeForWorseThanMean() {
            // Predictions worse than just predicting mean => R^2 < 0
            Tensor predicted = Tensor.fromFloat(new float[]{10, -10, 10}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));

            Tensor result = RegressionMetrics.rSquared(predicted, target);

            assertThat(result.toFloatArray()[0]).isLessThan(0.0f);
        }

        @Test
        void shouldComputeCorrectRSquared() {
            // target = [1, 2, 3], mean = 2
            // predicted = [1.1, 2.1, 2.9]
            // SS_res = 0.01 + 0.01 + 0.01 = 0.03
            // SS_tot = 1 + 0 + 1 = 2
            // R^2 = 1 - 0.03/2 = 0.985
            Tensor predicted = Tensor.fromFloat(new float[]{1.1f, 2.1f, 2.9f}, Shape.of(3));
            Tensor target = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));

            Tensor result = RegressionMetrics.rSquared(predicted, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(0.985f, within(1e-3f));
        }
    }

    @Nested
    class Validation {

        @Test
        void shouldThrowForNullPredictedMse() {
            Tensor target = Tensor.fromFloat(new float[]{0}, Shape.of(1));

            assertThatThrownBy(() -> RegressionMetrics.mse(null, target))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        void shouldThrowForNullTargetMae() {
            Tensor predicted = Tensor.fromFloat(new float[]{0}, Shape.of(1));

            assertThatThrownBy(() -> RegressionMetrics.mae(predicted, null))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        void shouldThrowForShapeMismatchRmse() {
            Tensor predicted = Tensor.fromFloat(new float[]{0, 1}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{0, 1, 2}, Shape.of(3));

            assertThatThrownBy(() -> RegressionMetrics.rmse(predicted, target))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        void shouldThrowForShapeMismatchRSquared() {
            Tensor predicted = Tensor.fromFloat(new float[]{0, 1}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{0, 1, 2}, Shape.of(3));

            assertThatThrownBy(() -> RegressionMetrics.rSquared(predicted, target))
                    .isInstanceOf(NeuralException.class);
        }
    }
}
