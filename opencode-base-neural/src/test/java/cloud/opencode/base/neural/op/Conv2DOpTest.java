package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link Conv2DOp}.
 */
@DisplayName("Conv2DOp — 二维卷积算子测试")
class Conv2DOpTest {

    private final Conv2DOp op = new Conv2DOp();

    @Nested
    @DisplayName("基本卷积 — stride=1, pad=0")
    class BasicConvolutionTest {

        @Test
        @DisplayName("input[1,1,4,4] + weight[1,1,3,3] → output[1,1,2,2] 已知值")
        void singleChannelBasic() {
            // Input: 1 batch, 1 channel, 4x4
            // 1  2  3  4
            // 5  6  7  8
            // 9  10 11 12
            // 13 14 15 16
            float[] inputData = {
                    1, 2, 3, 4,
                    5, 6, 7, 8,
                    9, 10, 11, 12,
                    13, 14, 15, 16
            };
            Tensor input = Tensor.fromFloat(inputData, Shape.of(1, 1, 4, 4));

            // Weight: 1 output channel, 1 input channel, 3x3 kernel (all ones)
            float[] weightData = {
                    1, 1, 1,
                    1, 1, 1,
                    1, 1, 1
            };
            Tensor weight = Tensor.fromFloat(weightData, Shape.of(1, 1, 3, 3));

            List<Tensor> result = op.forward(List.of(input, weight), OpAttribute.empty());

            assertThat(result).hasSize(1);
            Tensor output = result.getFirst();
            assertThat(output.shape()).isEqualTo(Shape.of(1, 1, 2, 2));

            // outH = (4 - 3) / 1 + 1 = 2, outW = 2
            // output[0,0] = sum of 3x3 patch at (0,0): 1+2+3+5+6+7+9+10+11 = 54
            // output[0,1] = sum of 3x3 patch at (0,1): 2+3+4+6+7+8+10+11+12 = 63
            // output[1,0] = sum of 3x3 patch at (1,0): 5+6+7+9+10+11+13+14+15 = 90
            // output[1,1] = sum of 3x3 patch at (1,1): 6+7+8+10+11+12+14+15+16 = 99
            float[] expected = {54, 63, 90, 99};
            assertThat(output.toFloatArray()).containsExactly(expected);
        }
    }

    @Nested
    @DisplayName("stride 和 padding 测试")
    class StridePaddingTest {

        @Test
        @DisplayName("stride=2, padding=1 正确输出形状和值")
        void stride2Padding1() {
            // Input: 1 batch, 1 channel, 4x4
            float[] inputData = new float[16];
            for (int i = 0; i < 16; i++) {
                inputData[i] = i + 1;
            }
            Tensor input = Tensor.fromFloat(inputData, Shape.of(1, 1, 4, 4));

            // Weight: identity-like 3x3 kernel (center = 1, rest = 0)
            float[] weightData = {
                    0, 0, 0,
                    0, 1, 0,
                    0, 0, 0
            };
            Tensor weight = Tensor.fromFloat(weightData, Shape.of(1, 1, 3, 3));

            OpAttribute attrs = OpAttribute.builder()
                    .put("stride", 2)
                    .put("padding", 1)
                    .build();

            List<Tensor> result = op.forward(List.of(input, weight), attrs);

            Tensor output = result.getFirst();
            // outH = (4 + 2*1 - 3) / 2 + 1 = 2, outW = 2
            assertThat(output.shape()).isEqualTo(Shape.of(1, 1, 2, 2));

            // With identity kernel and pad=1, stride=2:
            // output positions sample input at (0,0),(0,2),(2,0),(2,2) => 1,3,9,11
            assertThat(output.toFloatArray()).containsExactly(1, 3, 9, 11);
        }
    }

    @Nested
    @DisplayName("偏置测试")
    class BiasTest {

        @Test
        @DisplayName("带偏置的卷积正确添加偏置值")
        void withBias() {
            // Input: 1 batch, 1 channel, 3x3 (all zeros)
            Tensor input = Tensor.zeros(Shape.of(1, 1, 3, 3));

            // Weight: 2 output channels, 1 input channel, 3x3 kernel
            Tensor weight = Tensor.zeros(Shape.of(2, 1, 3, 3));

            // Bias: [10.0, 20.0]
            Tensor bias = Tensor.fromFloat(new float[]{10.0f, 20.0f}, Shape.of(2));

            List<Tensor> result = op.forward(List.of(input, weight, bias), OpAttribute.empty());

            Tensor output = result.getFirst();
            // outH = (3-3)/1+1 = 1, outW = 1 → shape [1, 2, 1, 1]
            assertThat(output.shape()).isEqualTo(Shape.of(1, 2, 1, 1));

            // All weights are zero, so output = bias only
            assertThat(output.getFloat(0, 0, 0, 0)).isEqualTo(10.0f);
            assertThat(output.getFloat(0, 1, 0, 0)).isEqualTo(20.0f);
        }
    }

    @Nested
    @DisplayName("分组卷积测试")
    class GroupedConvolutionTest {

        @Test
        @DisplayName("groups=2 正确拆分通道并卷积")
        void groups2() {
            // Input: 1 batch, 2 channels, 3x3
            float[] inputData = new float[2 * 3 * 3];
            // Channel 0: all 1s, Channel 1: all 2s
            for (int i = 0; i < 9; i++) {
                inputData[i] = 1.0f;
                inputData[9 + i] = 2.0f;
            }
            Tensor input = Tensor.fromFloat(inputData, Shape.of(1, 2, 3, 3));

            // Weight: 2 output channels, 1 input channel per group, 3x3
            // Group 0: weight for channel 0 (all 1s)
            // Group 1: weight for channel 1 (all 1s)
            float[] weightData = new float[2 * 1 * 3 * 3];
            for (int i = 0; i < 18; i++) {
                weightData[i] = 1.0f;
            }
            Tensor weight = Tensor.fromFloat(weightData, Shape.of(2, 1, 3, 3));

            OpAttribute attrs = OpAttribute.builder()
                    .put("groups", 2)
                    .build();

            List<Tensor> result = op.forward(List.of(input, weight), attrs);

            Tensor output = result.getFirst();
            // outH = (3-3)/1+1 = 1, outW = 1 → shape [1, 2, 1, 1]
            assertThat(output.shape()).isEqualTo(Shape.of(1, 2, 1, 1));

            // Group 0: sum of 9 ones = 9
            // Group 1: sum of 9 twos = 18
            assertThat(output.getFloat(0, 0, 0, 0)).isEqualTo(9.0f);
            assertThat(output.getFloat(0, 1, 0, 0)).isEqualTo(18.0f);
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionTest {

        @Test
        @DisplayName("null 输入列表抛出 OpExecutionException")
        void nullInputs() {
            assertThatThrownBy(() -> op.forward(null, OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("不足的输入数量抛出 OpExecutionException")
        void insufficientInputs() {
            Tensor input = Tensor.zeros(Shape.of(1, 1, 3, 3));
            assertThatThrownBy(() -> op.forward(List.of(input), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }

        @Test
        @DisplayName("null 输入张量抛出 OpExecutionException")
        void nullTensor() {
            assertThatThrownBy(() -> op.forward(Arrays.asList(null, null), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("非4D输入张量抛出 OpExecutionException")
        void invalidRank() {
            Tensor input = Tensor.zeros(Shape.of(3, 3));
            Tensor weight = Tensor.zeros(Shape.of(1, 1, 3, 3));
            assertThatThrownBy(() -> op.forward(List.of(input, weight), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class)
                    .hasMessageContaining("4D");
        }
    }
}
