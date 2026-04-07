package cloud.opencode.base.neural.loss;

import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link LossFunction}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("LossFunction — 损失函数接口")
class LossFunctionTest {

    private static final float EPSILON = 1e-6f;

    @Nested
    @DisplayName("functional interface")
    class FunctionalInterface {

        @Test
        @DisplayName("should be usable as lambda")
        void shouldBeUsableAsLambda() {
            // Custom loss: sum of absolute differences (not mean)
            LossFunction customLoss = (predicted, target) -> {
                float[] p = predicted.toFloatArray();
                float[] t = target.toFloatArray();
                float sum = 0;
                for (int i = 0; i < p.length; i++) {
                    sum += Math.abs(p[i] - t[i]);
                }
                return Tensor.fromFloat(new float[]{sum}, Shape.of(1));
            };

            Tensor pred = Tensor.fromFloat(new float[]{1.0f, 2.0f}, Shape.of(2));
            Tensor target = Tensor.fromFloat(new float[]{3.0f, 4.0f}, Shape.of(2));

            Tensor result = customLoss.compute(pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(4.0f, within(EPSILON));
        }

        @Test
        @DisplayName("should be usable as method reference")
        void shouldBeUsableAsMethodReference() {
            LossFunction fn = new MseLoss();
            Tensor pred = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));
            Tensor target = Tensor.fromFloat(new float[]{2.0f}, Shape.of(1));

            // Use compute as a method reference target
            Tensor result = applyLoss(fn, pred, target);

            assertThat(result.toFloatArray()[0]).isCloseTo(1.0f, within(EPSILON));
        }

        private Tensor applyLoss(LossFunction fn, Tensor pred, Tensor target) {
            return fn.compute(pred, target);
        }
    }
}
