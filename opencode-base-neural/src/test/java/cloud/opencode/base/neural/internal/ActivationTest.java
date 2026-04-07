package cloud.opencode.base.neural.internal;

import cloud.opencode.base.neural.exception.NeuralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link Activation}.
 */
@DisplayName("Activation — 激活函数测试")
class ActivationTest {

    private static final float EPSILON = 1e-5f;

    @Nested
    @DisplayName("relu — ReLU 激活函数")
    class ReluTest {

        @Test
        @DisplayName("relu([-1, 0, 1]) = [0, 0, 1]")
        void reluBasic() {
            float[] data = {-1f, 0f, 1f};
            Activation.relu(data, 0, 3);
            assertThat(data).containsExactly(0f, 0f, 1f);
        }

        @Test
        @DisplayName("relu 对正值无影响")
        void reluPositiveUnchanged() {
            float[] data = {2.5f, 3.7f, 0.1f};
            Activation.relu(data, 0, 3);
            assertThat(data[0]).isCloseTo(2.5f, within(EPSILON));
            assertThat(data[1]).isCloseTo(3.7f, within(EPSILON));
            assertThat(data[2]).isCloseTo(0.1f, within(EPSILON));
        }

        @Test
        @DisplayName("relu 使用 offset 和 length")
        void reluWithOffset() {
            float[] data = {-5f, -3f, 2f, -1f, 4f};
            Activation.relu(data, 1, 3);
            // Only indices 1,2,3 are processed
            assertThat(data[0]).isEqualTo(-5f); // untouched
            assertThat(data[1]).isEqualTo(0f);
            assertThat(data[2]).isEqualTo(2f);
            assertThat(data[3]).isEqualTo(0f);
            assertThat(data[4]).isEqualTo(4f); // untouched
        }
    }

    @Nested
    @DisplayName("sigmoid — Sigmoid 激活函数")
    class SigmoidTest {

        @Test
        @DisplayName("sigmoid(0) = 0.5")
        void sigmoidZero() {
            float[] data = {0f};
            Activation.sigmoid(data, 0, 1);
            assertThat(data[0]).isCloseTo(0.5f, within(EPSILON));
        }

        @Test
        @DisplayName("sigmoid 大正值接近 1.0")
        void sigmoidLargePositive() {
            float[] data = {10f};
            Activation.sigmoid(data, 0, 1);
            assertThat(data[0]).isCloseTo(1.0f, within(1e-4f));
        }

        @Test
        @DisplayName("sigmoid 大负值接近 0.0")
        void sigmoidLargeNegative() {
            float[] data = {-10f};
            Activation.sigmoid(data, 0, 1);
            assertThat(data[0]).isCloseTo(0.0f, within(1e-4f));
        }
    }

    @Nested
    @DisplayName("tanh — Tanh 激活函数")
    class TanhTest {

        @Test
        @DisplayName("tanh(0) = 0")
        void tanhZero() {
            float[] data = {0f};
            Activation.tanh(data, 0, 1);
            assertThat(data[0]).isCloseTo(0f, within(EPSILON));
        }

        @Test
        @DisplayName("tanh 输出范围在 [-1, 1]")
        void tanhRange() {
            float[] data = {-100f, 100f};
            Activation.tanh(data, 0, 2);
            assertThat(data[0]).isCloseTo(-1.0f, within(EPSILON));
            assertThat(data[1]).isCloseTo(1.0f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("softmax — Softmax 激活函数")
    class SoftmaxTest {

        @Test
        @DisplayName("softmax 输出总和为 1.0")
        void softmaxSumsToOne() {
            float[] data = {1f, 2f, 3f};
            Activation.softmax(data, 0, 3);

            float sum = data[0] + data[1] + data[2];
            assertThat(sum).isCloseTo(1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("softmax 保持单调性: 较大输入对应较大输出")
        void softmaxMonotonic() {
            float[] data = {1f, 2f, 3f};
            Activation.softmax(data, 0, 3);

            assertThat(data[2]).isGreaterThan(data[1]);
            assertThat(data[1]).isGreaterThan(data[0]);
        }

        @Test
        @DisplayName("softmax 相等输入产生均匀分布")
        void softmaxUniform() {
            float[] data = {5f, 5f, 5f};
            Activation.softmax(data, 0, 3);

            float expected = 1.0f / 3.0f;
            assertThat(data[0]).isCloseTo(expected, within(EPSILON));
            assertThat(data[1]).isCloseTo(expected, within(EPSILON));
            assertThat(data[2]).isCloseTo(expected, within(EPSILON));
        }

        @Test
        @DisplayName("softmax 数值稳定性: 大值不溢出")
        void softmaxNumericalStability() {
            float[] data = {1000f, 1001f, 1002f};
            Activation.softmax(data, 0, 3);

            float sum = data[0] + data[1] + data[2];
            assertThat(sum).isCloseTo(1.0f, within(EPSILON));
            assertThat(data[0]).isGreaterThan(0f);
        }
    }

    @Nested
    @DisplayName("softmaxBatch — 批量 Softmax")
    class SoftmaxBatchTest {

        @Test
        @DisplayName("每行独立 softmax, 各行总和为 1.0")
        void batchEachRowSumsToOne() {
            float[] data = {1, 2, 3, 4, 5, 6};
            Activation.softmaxBatch(data, 2, 3);

            float row0Sum = data[0] + data[1] + data[2];
            float row1Sum = data[3] + data[4] + data[5];
            assertThat(row0Sum).isCloseTo(1.0f, within(EPSILON));
            assertThat(row1Sum).isCloseTo(1.0f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("leakyRelu — LeakyReLU 激活函数")
    class LeakyReluTest {

        @Test
        @DisplayName("leakyRelu 正值不变，负值乘 alpha")
        void leakyReluBasic() {
            float[] data = {-2f, 0f, 1f};
            Activation.leakyRelu(data, 0, 3, 0.01f);
            assertThat(data[0]).isCloseTo(-0.02f, within(EPSILON));
            assertThat(data[1]).isCloseTo(0f, within(EPSILON));
            assertThat(data[2]).isCloseTo(1f, within(EPSILON));
        }

        @Test
        @DisplayName("leakyRelu 自定义 alpha=0.2")
        void leakyReluCustomAlpha() {
            float[] data = {-5f, 3f};
            Activation.leakyRelu(data, 0, 2, 0.2f);
            assertThat(data[0]).isCloseTo(-1.0f, within(EPSILON));
            assertThat(data[1]).isCloseTo(3f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("elu — ELU 激活函数")
    class EluTest {

        @Test
        @DisplayName("elu 正值不变，负值为 alpha*(exp(x)-1)")
        void eluBasic() {
            float[] data = {-1f, 0f, 1f};
            Activation.elu(data, 0, 3, 1.0f);
            assertThat(data[0]).isCloseTo((float) (Math.exp(-1) - 1), within(EPSILON));
            assertThat(data[1]).isCloseTo(0f, within(EPSILON));
            assertThat(data[2]).isCloseTo(1f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("selu — SELU 激活函数")
    class SeluTest {

        @Test
        @DisplayName("selu(0) = 0, selu(1) ≈ lambda")
        void seluBasic() {
            float[] data = {0f, 1f};
            Activation.selu(data, 0, 2);
            assertThat(data[0]).isCloseTo(0f, within(EPSILON));
            assertThat(data[1]).isCloseTo(1.0507009873554805f, within(EPSILON));
        }

        @Test
        @DisplayName("selu 负值收敛")
        void seluNegative() {
            float[] data = {-10f};
            Activation.selu(data, 0, 1);
            // Should be lambda * alpha * (exp(-10) - 1) ≈ -lambda*alpha
            float expected = 1.0507009873554805f * 1.6732632423543772f * ((float) Math.exp(-10) - 1.0f);
            assertThat(data[0]).isCloseTo(expected, within(1e-3f));
        }
    }

    @Nested
    @DisplayName("gelu — GELU 激活函数")
    class GeluTest {

        @Test
        @DisplayName("gelu(0) = 0")
        void geluZero() {
            float[] data = {0f};
            Activation.gelu(data, 0, 1);
            assertThat(data[0]).isCloseTo(0f, within(EPSILON));
        }

        @Test
        @DisplayName("gelu 大正值接近 x")
        void geluLargePositive() {
            float[] data = {5f};
            Activation.gelu(data, 0, 1);
            assertThat(data[0]).isCloseTo(5f, within(0.01f));
        }
    }

    @Nested
    @DisplayName("swish — Swish 激活函数")
    class SwishTest {

        @Test
        @DisplayName("swish(0) = 0")
        void swishZero() {
            float[] data = {0f};
            Activation.swish(data, 0, 1);
            assertThat(data[0]).isCloseTo(0f, within(EPSILON));
        }

        @Test
        @DisplayName("swish(x) = x * sigmoid(x)")
        void swishComputation() {
            float x = 2f;
            float[] data = {x};
            Activation.swish(data, 0, 1);
            float expected = x * (1.0f / (1.0f + (float) Math.exp(-x)));
            assertThat(data[0]).isCloseTo(expected, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("mish — Mish 激活函数")
    class MishTest {

        @Test
        @DisplayName("mish(0) = 0")
        void mishZero() {
            float[] data = {0f};
            Activation.mish(data, 0, 1);
            assertThat(data[0]).isCloseTo(0f, within(EPSILON));
        }

        @Test
        @DisplayName("mish 大正值接近 x")
        void mishLargePositive() {
            float[] data = {25f};
            Activation.mish(data, 0, 1);
            assertThat(data[0]).isCloseTo(25f, within(0.01f));
        }
    }

    @Nested
    @DisplayName("softplus — Softplus 激活函数")
    class SoftplusTest {

        @Test
        @DisplayName("softplus(0) = ln(2)")
        void softplusZero() {
            float[] data = {0f};
            Activation.softplus(data, 0, 1);
            assertThat(data[0]).isCloseTo((float) Math.log(2), within(EPSILON));
        }

        @Test
        @DisplayName("softplus 大正值接近 x（溢出保护）")
        void softplusLargePositive() {
            float[] data = {100f};
            Activation.softplus(data, 0, 1);
            assertThat(data[0]).isCloseTo(100f, within(EPSILON));
        }

        @Test
        @DisplayName("softplus 始终非负")
        void softplusNonNegative() {
            float[] data = {-5f, -1f, 0f, 1f, 5f};
            Activation.softplus(data, 0, 5);
            for (float v : data) {
                assertThat(v).isGreaterThan(0f);
            }
        }
    }

    @Nested
    @DisplayName("hardSigmoid — HardSigmoid 激活函数")
    class HardSigmoidTest {

        @Test
        @DisplayName("hardSigmoid(0) = 0.5")
        void hardSigmoidZero() {
            float[] data = {0f};
            Activation.hardSigmoid(data, 0, 1);
            assertThat(data[0]).isCloseTo(0.5f, within(EPSILON));
        }

        @Test
        @DisplayName("hardSigmoid 输出裁剪到 [0,1]")
        void hardSigmoidClamp() {
            float[] data = {-10f, 10f};
            Activation.hardSigmoid(data, 0, 2);
            assertThat(data[0]).isCloseTo(0f, within(EPSILON));
            assertThat(data[1]).isCloseTo(1f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("hardSwish — HardSwish 激活函数")
    class HardSwishTest {

        @Test
        @DisplayName("hardSwish(0) = 0")
        void hardSwishZero() {
            float[] data = {0f};
            Activation.hardSwish(data, 0, 1);
            assertThat(data[0]).isCloseTo(0f, within(EPSILON));
        }

        @Test
        @DisplayName("hardSwish 大正值接近 x")
        void hardSwishLargePositive() {
            float[] data = {10f};
            Activation.hardSwish(data, 0, 1);
            assertThat(data[0]).isCloseTo(10f, within(EPSILON));
        }
    }

    @Nested
    @DisplayName("异常处理")
    class ExceptionTest {

        @Test
        @DisplayName("null 数据抛出 NullPointerException")
        void nullData() {
            assertThatNullPointerException().isThrownBy(() ->
                    Activation.relu(null, 0, 1));
        }

        @Test
        @DisplayName("越界 offset 抛出 NeuralException")
        void outOfBoundsOffset() {
            assertThatThrownBy(() -> Activation.relu(new float[3], 5, 1))
                    .isInstanceOf(NeuralException.class);
        }

        @Test
        @DisplayName("softmax length <= 0 抛出 NeuralException")
        void softmaxZeroLength() {
            assertThatThrownBy(() -> Activation.softmax(new float[3], 0, 0))
                    .isInstanceOf(NeuralException.class);
        }
    }
}
