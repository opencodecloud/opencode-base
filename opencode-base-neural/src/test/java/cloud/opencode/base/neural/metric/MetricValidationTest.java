package cloud.opencode.base.neural.metric;

import cloud.opencode.base.neural.exception.NeuralException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetricValidationTest {

    @Nested
    class RequireNonNull {

        @Test
        void shouldThrowForNull() {
            assertThatThrownBy(() -> MetricValidation.requireNonNull(null, "test"))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("test")
                    .hasMessageContaining("null");
        }

        @Test
        void shouldPassForNonNull() {
            Tensor tensor = Tensor.fromFloat(new float[]{1}, Shape.of(1));

            assertThatNoException().isThrownBy(() -> MetricValidation.requireNonNull(tensor, "test"));
        }
    }

    @Nested
    class RequireRank {

        @Test
        void shouldThrowForWrongRank() {
            Tensor tensor = Tensor.fromFloat(new float[]{1, 2}, Shape.of(1, 2));

            assertThatThrownBy(() -> MetricValidation.requireRank(tensor, 1, "test"))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("1D");
        }

        @Test
        void shouldPassForCorrectRank() {
            Tensor tensor = Tensor.fromFloat(new float[]{1, 2}, Shape.of(2));

            assertThatNoException().isThrownBy(() -> MetricValidation.requireRank(tensor, 1, "test"));
        }
    }

    @Nested
    class RequireSameShape {

        @Test
        void shouldThrowForDifferentShapes() {
            Tensor a = Tensor.fromFloat(new float[]{1, 2}, Shape.of(2));
            Tensor b = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));

            assertThatThrownBy(() -> MetricValidation.requireSameShape(a, b))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("Shape mismatch");
        }

        @Test
        void shouldPassForSameShape() {
            Tensor a = Tensor.fromFloat(new float[]{1, 2}, Shape.of(2));
            Tensor b = Tensor.fromFloat(new float[]{3, 4}, Shape.of(2));

            assertThatNoException().isThrownBy(() -> MetricValidation.requireSameShape(a, b));
        }
    }

    @Nested
    class RequireSameSize {

        @Test
        void shouldThrowForDifferentSizes() {
            assertThatThrownBy(() -> MetricValidation.requireSameSize(2, 3, "a", "b"))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("a")
                    .hasMessageContaining("b");
        }

        @Test
        void shouldPassForSameSizes() {
            assertThatNoException().isThrownBy(() -> MetricValidation.requireSameSize(5, 5, "a", "b"));
        }
    }

    @Nested
    class RequirePositiveSize {

        @Test
        void shouldThrowForZeroSize() {
            assertThatThrownBy(() -> MetricValidation.requirePositiveSize(0))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        void shouldThrowForNegativeSize() {
            assertThatThrownBy(() -> MetricValidation.requirePositiveSize(-1))
                    .isInstanceOf(NeuralException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        void shouldPassForPositiveSize() {
            assertThatNoException().isThrownBy(() -> MetricValidation.requirePositiveSize(1));
        }
    }
}
