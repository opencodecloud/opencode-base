package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * HardSigmoidOp + HardSwishOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("HardSigmoid / HardSwish 激活算子测试")
class HardActivationOpsTest {

    @Nested
    @DisplayName("HardSigmoid 测试")
    class HardSigmoidTests {

        private final Op op = new HardSigmoidOp();

        @Test
        @DisplayName("x=0 → 0.5")
        void zeroInput() {
            Tensor input = Tensor.fromFloat(new float[]{0.0f}, Shape.of(1));
            List<Tensor> result = op.forward(List.of(input), OpAttribute.empty());
            assertThat(result.get(0).toFloatArray()[0]).isCloseTo(0.5f, within(1e-6f));
        }

        @Test
        @DisplayName("x=3 → clamp(3/6+0.5=1.0) = 1.0")
        void positiveClamp() {
            Tensor input = Tensor.fromFloat(new float[]{3.0f, 6.0f, 10.0f}, Shape.of(3));
            float[] out = op.forward(List.of(input), OpAttribute.empty()).get(0).toFloatArray();
            assertThat(out[0]).isCloseTo(1.0f, within(1e-6f));
            assertThat(out[1]).isCloseTo(1.0f, within(1e-6f));
            assertThat(out[2]).isCloseTo(1.0f, within(1e-6f));
        }

        @Test
        @DisplayName("x=-3 → clamp(-3/6+0.5=0.0) = 0.0")
        void negativeClamp() {
            Tensor input = Tensor.fromFloat(new float[]{-3.0f, -6.0f, -10.0f}, Shape.of(3));
            float[] out = op.forward(List.of(input), OpAttribute.empty()).get(0).toFloatArray();
            assertThat(out[0]).isCloseTo(0.0f, within(1e-6f));
            assertThat(out[1]).isCloseTo(0.0f, within(1e-6f));
            assertThat(out[2]).isCloseTo(0.0f, within(1e-6f));
        }

        @Test
        @DisplayName("线性区间 x=1 → 1/6+0.5 ≈ 0.6667")
        void linearRegion() {
            Tensor input = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));
            float[] out = op.forward(List.of(input), OpAttribute.empty()).get(0).toFloatArray();
            assertThat(out[0]).isCloseTo(1.0f / 6.0f + 0.5f, within(1e-5f));
        }

        @Test
        @DisplayName("输出范围在 [0, 1]")
        void outputRange() {
            Tensor input = Tensor.fromFloat(new float[]{-100f, -3f, 0f, 3f, 100f}, Shape.of(5));
            float[] out = op.forward(List.of(input), OpAttribute.empty()).get(0).toFloatArray();
            for (float v : out) {
                assertThat(v).isBetween(0.0f, 1.0f);
            }
        }

        @Test
        @DisplayName("null 输入抛异常")
        void nullInput() {
            assertThatThrownBy(() -> op.forward(null, OpAttribute.empty()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("空输入列表抛异常")
        void emptyInputs() {
            assertThatThrownBy(() -> op.forward(List.of(), OpAttribute.empty()))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("HardSwish 测试")
    class HardSwishTests {

        private final Op op = new HardSwishOp();

        @Test
        @DisplayName("x=0 → 0*0.5 = 0")
        void zeroInput() {
            Tensor input = Tensor.fromFloat(new float[]{0.0f}, Shape.of(1));
            float[] out = op.forward(List.of(input), OpAttribute.empty()).get(0).toFloatArray();
            assertThat(out[0]).isCloseTo(0.0f, within(1e-6f));
        }

        @Test
        @DisplayName("x≥3 → x*1.0 = x (identity)")
        void largePositive() {
            Tensor input = Tensor.fromFloat(new float[]{3.0f, 5.0f, 10.0f}, Shape.of(3));
            float[] out = op.forward(List.of(input), OpAttribute.empty()).get(0).toFloatArray();
            assertThat(out[0]).isCloseTo(3.0f, within(1e-5f));
            assertThat(out[1]).isCloseTo(5.0f, within(1e-5f));
            assertThat(out[2]).isCloseTo(10.0f, within(1e-5f));
        }

        @Test
        @DisplayName("x≤-3 → x*0.0 = 0")
        void largeNegative() {
            Tensor input = Tensor.fromFloat(new float[]{-3.0f, -5.0f, -10.0f}, Shape.of(3));
            float[] out = op.forward(List.of(input), OpAttribute.empty()).get(0).toFloatArray();
            assertThat(out[0]).isCloseTo(0.0f, within(1e-5f));
            assertThat(out[1]).isCloseTo(0.0f, within(1e-5f));
            assertThat(out[2]).isCloseTo(0.0f, within(1e-5f));
        }

        @Test
        @DisplayName("线性区间 x=1 → 1*(1/6+0.5) ≈ 0.6667")
        void linearRegion() {
            Tensor input = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));
            float[] out = op.forward(List.of(input), OpAttribute.empty()).get(0).toFloatArray();
            float expected = 1.0f * (1.0f / 6.0f + 0.5f);
            assertThat(out[0]).isCloseTo(expected, within(1e-5f));
        }

        @Test
        @DisplayName("x=-1 → -1*clamp(-1/6+0.5) = -1*0.3333 ≈ -0.3333")
        void negativeLinear() {
            Tensor input = Tensor.fromFloat(new float[]{-1.0f}, Shape.of(1));
            float[] out = op.forward(List.of(input), OpAttribute.empty()).get(0).toFloatArray();
            float expected = -1.0f * (-1.0f / 6.0f + 0.5f);
            assertThat(out[0]).isCloseTo(expected, within(1e-5f));
        }

        @Test
        @DisplayName("与 HardSigmoid 关系验证: hardswish(x) = x * hardsigmoid(x)")
        void relationToHardSigmoid() {
            float[] values = {-4f, -2f, -1f, 0f, 0.5f, 1f, 2f, 4f};
            Tensor input = Tensor.fromFloat(values.clone(), Shape.of(values.length));

            float[] hsResult = new HardSigmoidOp().forward(List.of(input), OpAttribute.empty())
                    .get(0).toFloatArray();
            float[] hwResult = op.forward(List.of(input), OpAttribute.empty())
                    .get(0).toFloatArray();

            for (int i = 0; i < values.length; i++) {
                assertThat(hwResult[i]).isCloseTo(values[i] * hsResult[i], within(1e-5f));
            }
        }

        @Test
        @DisplayName("null 输入抛异常")
        void nullInput() {
            assertThatThrownBy(() -> op.forward(null, OpAttribute.empty()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("OpRegistry 已注册")
        void registered() {
            assertThat(OpRegistry.isSupported("HardSwish")).isTrue();
            assertThat(OpRegistry.isSupported("HardSigmoid")).isTrue();
        }
    }
}
