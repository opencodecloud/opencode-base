package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for new activation Op implementations.
 */
@DisplayName("New Activation Ops — 新激活算子测试")
class NewActivationOpsTest {

    private static final float EPSILON = 1e-4f;

    private Tensor tensor(float... values) {
        return Tensor.fromFloat(values, Shape.of(values.length));
    }

    @Nested
    @DisplayName("LeakyReluOp")
    class LeakyReluOpTest {

        @Test
        @DisplayName("正值不变，负值乘 alpha")
        void basic() {
            Op op = new LeakyReluOp();
            Tensor input = tensor(-2f, -1f, 0f, 1f, 2f);
            List<Tensor> out = op.forward(List.of(input), OpAttribute.empty());
            float[] result = out.get(0).toFloatArray();
            assertThat(result[0]).isCloseTo(-0.02f, within(EPSILON));
            assertThat(result[1]).isCloseTo(-0.01f, within(EPSILON));
            assertThat(result[2]).isCloseTo(0f, within(EPSILON));
            assertThat(result[3]).isCloseTo(1f, within(EPSILON));
            assertThat(result[4]).isCloseTo(2f, within(EPSILON));
        }

        @Test
        @DisplayName("自定义 alpha")
        void customAlpha() {
            Op op = new LeakyReluOp();
            OpAttribute attrs = OpAttribute.builder().put("alpha", 0.2f).build();
            Tensor input = tensor(-5f);
            float[] result = op.forward(List.of(input), attrs).get(0).toFloatArray();
            assertThat(result[0]).isCloseTo(-1.0f, within(EPSILON));
        }

        @Test
        @DisplayName("空输入抛异常")
        void emptyInput() {
            assertThatThrownBy(() -> new LeakyReluOp().forward(List.of(), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }

    @Nested
    @DisplayName("EluOp")
    class EluOpTest {

        @Test
        @DisplayName("正值不变，负值为 alpha*(exp(x)-1)")
        void basic() {
            Op op = new EluOp();
            Tensor input = tensor(-1f, 0f, 1f);
            float[] result = op.forward(List.of(input), OpAttribute.empty()).get(0).toFloatArray();
            // ELU(-1, alpha=1) = exp(-1)-1 ≈ -0.6321
            assertThat(result[0]).isCloseTo((float) (Math.exp(-1) - 1), within(EPSILON));
            assertThat(result[1]).isCloseTo(0f, within(EPSILON));
            assertThat(result[2]).isCloseTo(1f, within(EPSILON));
        }

        @Test
        @DisplayName("空输入抛异常")
        void emptyInput() {
            assertThatThrownBy(() -> new EluOp().forward(List.of(), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }

    @Nested
    @DisplayName("SeluOp")
    class SeluOpTest {

        @Test
        @DisplayName("正值乘 lambda，负值按 SELU 公式")
        void basic() {
            Op op = new SeluOp();
            Tensor input = tensor(0f, 1f);
            float[] result = op.forward(List.of(input), OpAttribute.empty()).get(0).toFloatArray();
            assertThat(result[0]).isCloseTo(0f, within(EPSILON));
            assertThat(result[1]).isCloseTo(1.0507009873554805f, within(EPSILON));
        }

        @Test
        @DisplayName("空输入抛异常")
        void emptyInput() {
            assertThatThrownBy(() -> new SeluOp().forward(List.of(), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }

    @Nested
    @DisplayName("GeluOp")
    class GeluOpTest {

        @Test
        @DisplayName("GELU(0) = 0")
        void zero() {
            Op op = new GeluOp();
            Tensor input = tensor(0f);
            float[] result = op.forward(List.of(input), OpAttribute.empty()).get(0).toFloatArray();
            assertThat(result[0]).isCloseTo(0f, within(EPSILON));
        }

        @Test
        @DisplayName("GELU 大正值接近 x")
        void largePositive() {
            Op op = new GeluOp();
            Tensor input = tensor(5f);
            float[] result = op.forward(List.of(input), OpAttribute.empty()).get(0).toFloatArray();
            assertThat(result[0]).isCloseTo(5f, within(0.01f));
        }

        @Test
        @DisplayName("空输入抛异常")
        void emptyInput() {
            assertThatThrownBy(() -> new GeluOp().forward(List.of(), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }

    @Nested
    @DisplayName("SwishOp")
    class SwishOpTest {

        @Test
        @DisplayName("Swish(0) = 0")
        void zero() {
            Op op = new SwishOp();
            float[] result = op.forward(List.of(tensor(0f)), OpAttribute.empty()).get(0).toFloatArray();
            assertThat(result[0]).isCloseTo(0f, within(EPSILON));
        }

        @Test
        @DisplayName("Swish(x) = x * sigmoid(x)")
        void positive() {
            Op op = new SwishOp();
            float x = 2f;
            float expected = x * (1.0f / (1.0f + (float) Math.exp(-x)));
            float[] result = op.forward(List.of(tensor(x)), OpAttribute.empty()).get(0).toFloatArray();
            assertThat(result[0]).isCloseTo(expected, within(EPSILON));
        }

        @Test
        @DisplayName("空输入抛异常")
        void emptyInput() {
            assertThatThrownBy(() -> new SwishOp().forward(List.of(), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }

    @Nested
    @DisplayName("MishOp")
    class MishOpTest {

        @Test
        @DisplayName("Mish(0) = 0")
        void zero() {
            Op op = new MishOp();
            float[] result = op.forward(List.of(tensor(0f)), OpAttribute.empty()).get(0).toFloatArray();
            assertThat(result[0]).isCloseTo(0f, within(EPSILON));
        }

        @Test
        @DisplayName("Mish 大正值接近 x")
        void largePositive() {
            Op op = new MishOp();
            float[] result = op.forward(List.of(tensor(25f)), OpAttribute.empty()).get(0).toFloatArray();
            assertThat(result[0]).isCloseTo(25f, within(0.01f));
        }

        @Test
        @DisplayName("空输入抛异常")
        void emptyInput() {
            assertThatThrownBy(() -> new MishOp().forward(List.of(), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }

    @Nested
    @DisplayName("SoftplusOp")
    class SoftplusOpTest {

        @Test
        @DisplayName("Softplus(0) = ln(2)")
        void zero() {
            Op op = new SoftplusOp();
            float[] result = op.forward(List.of(tensor(0f)), OpAttribute.empty()).get(0).toFloatArray();
            assertThat(result[0]).isCloseTo((float) Math.log(2), within(EPSILON));
        }

        @Test
        @DisplayName("Softplus 大正值接近 x")
        void largePositive() {
            Op op = new SoftplusOp();
            float[] result = op.forward(List.of(tensor(25f)), OpAttribute.empty()).get(0).toFloatArray();
            assertThat(result[0]).isCloseTo(25f, within(EPSILON));
        }

        @Test
        @DisplayName("空输入抛异常")
        void emptyInput() {
            assertThatThrownBy(() -> new SoftplusOp().forward(List.of(), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }

    @Nested
    @DisplayName("OpRegistry 注册验证")
    class RegistryTest {

        @Test
        @DisplayName("新激活算子已注册")
        void newOpsRegistered() {
            assertThat(OpRegistry.isSupported("LeakyReLU")).isTrue();
            assertThat(OpRegistry.isSupported("ELU")).isTrue();
            assertThat(OpRegistry.isSupported("SELU")).isTrue();
            assertThat(OpRegistry.isSupported("GELU")).isTrue();
            assertThat(OpRegistry.isSupported("Swish")).isTrue();
            assertThat(OpRegistry.isSupported("Mish")).isTrue();
            assertThat(OpRegistry.isSupported("Softplus")).isTrue();
            assertThat(OpRegistry.isSupported("GlobalAvgPool")).isTrue();
        }
    }
}
