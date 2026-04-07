package cloud.opencode.base.neural.tensor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link TensorMath}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("TensorMath — 张量数学运算")
class TensorMathTest {

    private static final float EPSILON = 1e-5f;

    @Nested
    @DisplayName("exp")
    class ExpTest {

        @Test
        @DisplayName("exp(0) = 1")
        void expZero() {
            Tensor t = Tensor.fromFloat(new float[]{0}, Shape.of(1));
            Tensor result = TensorMath.exp(t);
            assertThat(result.getFloat(0)).isCloseTo(1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("exp(1) = e")
        void expOne() {
            Tensor t = Tensor.fromFloat(new float[]{1}, Shape.of(1));
            Tensor result = TensorMath.exp(t);
            assertThat(result.getFloat(0)).isCloseTo((float) Math.E, within(EPSILON));
        }

        @Test
        @DisplayName("exp 多元素")
        void expMultiple() {
            Tensor t = Tensor.fromFloat(new float[]{0, 1, 2}, Shape.of(3));
            Tensor result = TensorMath.exp(t);
            assertThat(result.getFloat(0)).isCloseTo(1.0f, within(EPSILON));
            assertThat(result.getFloat(1)).isCloseTo((float) Math.E, within(EPSILON));
            assertThat(result.getFloat(2)).isCloseTo((float) Math.exp(2), within(EPSILON));
        }
    }

    @Nested
    @DisplayName("log")
    class LogTest {

        @Test
        @DisplayName("log(1) = 0")
        void logOne() {
            Tensor t = Tensor.fromFloat(new float[]{1}, Shape.of(1));
            Tensor result = TensorMath.log(t);
            assertThat(result.getFloat(0)).isCloseTo(0.0f, within(EPSILON));
        }

        @Test
        @DisplayName("log(e) = 1")
        void logE() {
            Tensor t = Tensor.fromFloat(new float[]{(float) Math.E}, Shape.of(1));
            Tensor result = TensorMath.log(t);
            assertThat(result.getFloat(0)).isCloseTo(1.0f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("sqrt")
    class SqrtTest {

        @Test
        @DisplayName("sqrt(4) = 2")
        void sqrtFour() {
            Tensor t = Tensor.fromFloat(new float[]{4}, Shape.of(1));
            Tensor result = TensorMath.sqrt(t);
            assertThat(result.getFloat(0)).isCloseTo(2.0f, within(EPSILON));
        }

        @Test
        @DisplayName("sqrt(0) = 0")
        void sqrtZero() {
            Tensor t = Tensor.fromFloat(new float[]{0}, Shape.of(1));
            Tensor result = TensorMath.sqrt(t);
            assertThat(result.getFloat(0)).isCloseTo(0.0f, within(EPSILON));
        }

        @Test
        @DisplayName("sqrt(9) = 3")
        void sqrtNine() {
            Tensor t = Tensor.fromFloat(new float[]{9}, Shape.of(1));
            Tensor result = TensorMath.sqrt(t);
            assertThat(result.getFloat(0)).isCloseTo(3.0f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("abs")
    class AbsTest {

        @Test
        @DisplayName("abs(-3) = 3")
        void absNegative() {
            Tensor t = Tensor.fromFloat(new float[]{-3}, Shape.of(1));
            Tensor result = TensorMath.abs(t);
            assertThat(result.getFloat(0)).isEqualTo(3.0f);
        }

        @Test
        @DisplayName("abs(3) = 3")
        void absPositive() {
            Tensor t = Tensor.fromFloat(new float[]{3}, Shape.of(1));
            Tensor result = TensorMath.abs(t);
            assertThat(result.getFloat(0)).isEqualTo(3.0f);
        }

        @Test
        @DisplayName("abs 多元素")
        void absMultiple() {
            Tensor t = Tensor.fromFloat(new float[]{-1, 0, 2, -5}, Shape.of(4));
            Tensor result = TensorMath.abs(t);
            assertThat(result.toFloatArray()).containsExactly(1, 0, 2, 5);
        }
    }

    @Nested
    @DisplayName("neg")
    class NegTest {

        @Test
        @DisplayName("neg 取反")
        void negValues() {
            Tensor t = Tensor.fromFloat(new float[]{1, -2, 3}, Shape.of(3));
            Tensor result = TensorMath.neg(t);
            assertThat(result.toFloatArray()).containsExactly(-1, 2, -3);
        }
    }

    @Nested
    @DisplayName("relu")
    class ReluTest {

        @Test
        @DisplayName("relu(-1) = 0")
        void reluNegative() {
            Tensor t = Tensor.fromFloat(new float[]{-1}, Shape.of(1));
            Tensor result = TensorMath.relu(t);
            assertThat(result.getFloat(0)).isEqualTo(0.0f);
        }

        @Test
        @DisplayName("relu(1) = 1")
        void reluPositive() {
            Tensor t = Tensor.fromFloat(new float[]{1}, Shape.of(1));
            Tensor result = TensorMath.relu(t);
            assertThat(result.getFloat(0)).isEqualTo(1.0f);
        }

        @Test
        @DisplayName("relu(0) = 0")
        void reluZero() {
            Tensor t = Tensor.fromFloat(new float[]{0}, Shape.of(1));
            Tensor result = TensorMath.relu(t);
            assertThat(result.getFloat(0)).isEqualTo(0.0f);
        }

        @Test
        @DisplayName("relu 多元素")
        void reluMultiple() {
            Tensor t = Tensor.fromFloat(new float[]{-3, -1, 0, 1, 5}, Shape.of(5));
            Tensor result = TensorMath.relu(t);
            assertThat(result.toFloatArray()).containsExactly(0, 0, 0, 1, 5);
        }
    }

    @Nested
    @DisplayName("clamp")
    class ClampTest {

        @Test
        @DisplayName("clamp 裁剪到范围")
        void clampValues() {
            Tensor t = Tensor.fromFloat(new float[]{-5, -1, 0, 3, 10}, Shape.of(5));
            Tensor result = TensorMath.clamp(t, -2.0f, 5.0f);
            assertThat(result.toFloatArray()).containsExactly(-2, -1, 0, 3, 5);
        }

        @Test
        @DisplayName("clamp 所有值在范围内")
        void clampNoChange() {
            Tensor t = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(3));
            Tensor result = TensorMath.clamp(t, 0.0f, 10.0f);
            assertThat(result.toFloatArray()).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("sigmoid")
    class SigmoidTest {

        @Test
        @DisplayName("sigmoid(0) = 0.5")
        void sigmoidZero() {
            Tensor t = Tensor.fromFloat(new float[]{0}, Shape.of(1));
            Tensor result = TensorMath.sigmoid(t);
            assertThat(result.getFloat(0)).isCloseTo(0.5f, within(EPSILON));
        }

        @Test
        @DisplayName("sigmoid 大正值接近 1.0")
        void sigmoidLargePositive() {
            Tensor t = Tensor.fromFloat(new float[]{10f}, Shape.of(1));
            Tensor result = TensorMath.sigmoid(t);
            assertThat(result.getFloat(0)).isCloseTo(1.0f, within(1e-4f));
        }
    }

    @Nested
    @DisplayName("tanh")
    class TanhTest {

        @Test
        @DisplayName("tanh(0) = 0")
        void tanhZero() {
            Tensor t = Tensor.fromFloat(new float[]{0}, Shape.of(1));
            Tensor result = TensorMath.tanh(t);
            assertThat(result.getFloat(0)).isCloseTo(0f, within(EPSILON));
        }

        @Test
        @DisplayName("tanh 范围 [-1, 1]")
        void tanhRange() {
            Tensor t = Tensor.fromFloat(new float[]{-100f, 100f}, Shape.of(2));
            Tensor result = TensorMath.tanh(t);
            assertThat(result.getFloat(0)).isCloseTo(-1.0f, within(EPSILON));
            assertThat(result.getFloat(1)).isCloseTo(1.0f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("leakyRelu")
    class LeakyReluTest {

        @Test
        @DisplayName("leakyRelu 正值不变，负值乘 alpha")
        void leakyReluBasic() {
            Tensor t = Tensor.fromFloat(new float[]{-2f, 0f, 1f}, Shape.of(3));
            Tensor result = TensorMath.leakyRelu(t, 0.1f);
            assertThat(result.getFloat(0)).isCloseTo(-0.2f, within(EPSILON));
            assertThat(result.getFloat(1)).isCloseTo(0f, within(EPSILON));
            assertThat(result.getFloat(2)).isCloseTo(1f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("elu")
    class EluTensorTest {

        @Test
        @DisplayName("elu 正值不变")
        void eluPositive() {
            Tensor t = Tensor.fromFloat(new float[]{1f}, Shape.of(1));
            Tensor result = TensorMath.elu(t, 1.0f);
            assertThat(result.getFloat(0)).isCloseTo(1f, within(EPSILON));
        }

        @Test
        @DisplayName("elu 负值")
        void eluNegative() {
            Tensor t = Tensor.fromFloat(new float[]{-1f}, Shape.of(1));
            Tensor result = TensorMath.elu(t, 1.0f);
            assertThat(result.getFloat(0)).isCloseTo((float) (Math.exp(-1) - 1), within(EPSILON));
        }
    }

    @Nested
    @DisplayName("selu")
    class SeluTensorTest {

        @Test
        @DisplayName("selu(0) = 0, selu(1) ≈ lambda")
        void seluBasic() {
            Tensor t = Tensor.fromFloat(new float[]{0f, 1f}, Shape.of(2));
            Tensor result = TensorMath.selu(t);
            assertThat(result.getFloat(0)).isCloseTo(0f, within(EPSILON));
            assertThat(result.getFloat(1)).isCloseTo(1.0507009873554805f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("gelu")
    class GeluTensorTest {

        @Test
        @DisplayName("gelu(0) = 0")
        void geluZero() {
            Tensor t = Tensor.fromFloat(new float[]{0f}, Shape.of(1));
            Tensor result = TensorMath.gelu(t);
            assertThat(result.getFloat(0)).isCloseTo(0f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("swish")
    class SwishTensorTest {

        @Test
        @DisplayName("swish(0) = 0")
        void swishZero() {
            Tensor t = Tensor.fromFloat(new float[]{0f}, Shape.of(1));
            Tensor result = TensorMath.swish(t);
            assertThat(result.getFloat(0)).isCloseTo(0f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("mish")
    class MishTensorTest {

        @Test
        @DisplayName("mish(0) = 0")
        void mishZero() {
            Tensor t = Tensor.fromFloat(new float[]{0f}, Shape.of(1));
            Tensor result = TensorMath.mish(t);
            assertThat(result.getFloat(0)).isCloseTo(0f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("softplus")
    class SoftplusTensorTest {

        @Test
        @DisplayName("softplus(0) = ln(2)")
        void softplusZero() {
            Tensor t = Tensor.fromFloat(new float[]{0f}, Shape.of(1));
            Tensor result = TensorMath.softplus(t);
            assertThat(result.getFloat(0)).isCloseTo((float) Math.log(2), within(EPSILON));
        }

        @Test
        @DisplayName("softplus 大正值溢出保护")
        void softplusOverflow() {
            Tensor t = Tensor.fromFloat(new float[]{100f}, Shape.of(1));
            Tensor result = TensorMath.softplus(t);
            assertThat(result.getFloat(0)).isCloseTo(100f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("不修改原张量")
    class ImmutabilityTest {

        @Test
        @DisplayName("所有运算不修改输入张量")
        void operationsDoNotModifyInput() {
            Tensor t = Tensor.fromFloat(new float[]{-2, 0, 3}, Shape.of(3));
            float[] original = t.toFloatArray();

            TensorMath.exp(t);
            TensorMath.log(t);
            TensorMath.sqrt(t);
            TensorMath.abs(t);
            TensorMath.neg(t);
            TensorMath.relu(t);
            TensorMath.clamp(t, 0, 1);

            assertThat(t.toFloatArray()).containsExactly(original);
        }
    }
}
