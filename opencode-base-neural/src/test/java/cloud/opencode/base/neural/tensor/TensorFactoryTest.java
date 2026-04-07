package cloud.opencode.base.neural.tensor;

import cloud.opencode.base.neural.exception.TensorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link TensorFactory}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("TensorFactory — 张量工厂")
class TensorFactoryTest {

    @Nested
    @DisplayName("arange")
    class ArangeTest {

        @Test
        @DisplayName("arange(5) = [0, 1, 2, 3, 4]")
        void arangeFive() {
            Tensor t = TensorFactory.arange(5);
            assertThat(t.shape().dims()).containsExactly(5);
            assertThat(t.toFloatArray()).containsExactly(0, 1, 2, 3, 4);
        }

        @Test
        @DisplayName("arange(1) = [0]")
        void arangeOne() {
            Tensor t = TensorFactory.arange(1);
            assertThat(t.toFloatArray()).containsExactly(0);
        }

        @Test
        @DisplayName("arange(0) 应抛出异常")
        void arangeZeroShouldThrow() {
            assertThatThrownBy(() -> TensorFactory.arange(0))
                    .isInstanceOf(TensorException.class);
        }

        @Test
        @DisplayName("arange 负数应抛出异常")
        void arangeNegativeShouldThrow() {
            assertThatThrownBy(() -> TensorFactory.arange(-3))
                    .isInstanceOf(TensorException.class);
        }
    }

    @Nested
    @DisplayName("fill")
    class FillTest {

        @Test
        @DisplayName("fill(shape, 7) 所有元素为7")
        void fillAllSevens() {
            Tensor t = TensorFactory.fill(Shape.of(2, 3), 7.0f);
            assertThat(t.shape().dims()).containsExactly(2, 3);
            float[] data = t.toFloatArray();
            for (float v : data) {
                assertThat(v).isEqualTo(7.0f);
            }
        }

        @Test
        @DisplayName("fill(shape, 0) 所有元素为0")
        void fillZeros() {
            Tensor t = TensorFactory.fill(Shape.of(4), 0.0f);
            assertThat(t.toFloatArray()).containsExactly(0, 0, 0, 0);
        }

        @Test
        @DisplayName("fill 负数值")
        void fillNegative() {
            Tensor t = TensorFactory.fill(Shape.of(2), -3.14f);
            assertThat(t.toFloatArray()).containsExactly(-3.14f, -3.14f);
        }
    }

    @Nested
    @DisplayName("eye")
    class EyeTest {

        @Test
        @DisplayName("eye(3) 生成3x3单位矩阵")
        void eye3() {
            Tensor t = TensorFactory.eye(3);
            assertThat(t.shape().dims()).containsExactly(3, 3);
            // Check diagonal is 1, off-diagonal is 0
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    float expected = (i == j) ? 1.0f : 0.0f;
                    assertThat(t.getFloat(i, j)).isEqualTo(expected);
                }
            }
        }

        @Test
        @DisplayName("eye(1) 生成1x1单位矩阵")
        void eye1() {
            Tensor t = TensorFactory.eye(1);
            assertThat(t.shape().dims()).containsExactly(1, 1);
            assertThat(t.getFloat(0, 0)).isEqualTo(1.0f);
        }

        @Test
        @DisplayName("eye(0) 应抛出异常")
        void eyeZeroShouldThrow() {
            assertThatThrownBy(() -> TensorFactory.eye(0))
                    .isInstanceOf(TensorException.class);
        }

        @Test
        @DisplayName("eye 负数应抛出异常")
        void eyeNegativeShouldThrow() {
            assertThatThrownBy(() -> TensorFactory.eye(-1))
                    .isInstanceOf(TensorException.class);
        }
    }
}
