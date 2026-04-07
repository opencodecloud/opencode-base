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
 * Tests for {@link Conv1DOp}.
 */
@DisplayName("Conv1DOp — 一维卷积算子测试")
class Conv1DOpTest {

    private final Conv1DOp op = new Conv1DOp();

    @Nested
    @DisplayName("基本卷积 — stride=1, pad=0")
    class BasicConvolutionTest {

        @Test
        @DisplayName("input[1,1,5] + kernel[1,1,3] → output[1,1,3] 已知值")
        void singleChannelBasic() {
            // Input: [1, 2, 3, 4, 5]
            float[] inputData = {1, 2, 3, 4, 5};
            Tensor input = Tensor.fromFloat(inputData, Shape.of(1, 1, 5));

            // Kernel: [1, 1, 1] (sum kernel)
            float[] weightData = {1, 1, 1};
            Tensor weight = Tensor.fromFloat(weightData, Shape.of(1, 1, 3));

            List<Tensor> result = op.forward(List.of(input, weight), OpAttribute.empty());

            assertThat(result).hasSize(1);
            Tensor output = result.getFirst();
            // L_out = (5 - 3) / 1 + 1 = 3
            assertThat(output.shape()).isEqualTo(Shape.of(1, 1, 3));

            // output[0] = 1+2+3 = 6
            // output[1] = 2+3+4 = 9
            // output[2] = 3+4+5 = 12
            assertThat(output.toFloatArray()).containsExactly(6.0f, 9.0f, 12.0f);
        }

        @Test
        @DisplayName("input[1,1,5] + kernel[1,1,3] 加权卷积核")
        void weightedKernel() {
            // Input: [1, 2, 3, 4, 5]
            float[] inputData = {1, 2, 3, 4, 5};
            Tensor input = Tensor.fromFloat(inputData, Shape.of(1, 1, 5));

            // Kernel: [1, 0, -1]
            float[] weightData = {1, 0, -1};
            Tensor weight = Tensor.fromFloat(weightData, Shape.of(1, 1, 3));

            List<Tensor> result = op.forward(List.of(input, weight), OpAttribute.empty());

            Tensor output = result.getFirst();
            assertThat(output.shape()).isEqualTo(Shape.of(1, 1, 3));

            // output[0] = 1*1 + 2*0 + 3*(-1) = -2
            // output[1] = 2*1 + 3*0 + 4*(-1) = -2
            // output[2] = 3*1 + 4*0 + 5*(-1) = -2
            assertThat(output.toFloatArray()).containsExactly(-2.0f, -2.0f, -2.0f);
        }
    }

    @Nested
    @DisplayName("padding 测试")
    class PaddingTest {

        @Test
        @DisplayName("padding=1 正确填充零值并输出")
        void withPadding() {
            // Input: [1, 2, 3]
            float[] inputData = {1, 2, 3};
            Tensor input = Tensor.fromFloat(inputData, Shape.of(1, 1, 3));

            // Identity kernel at center: [0, 1, 0]
            float[] weightData = {0, 1, 0};
            Tensor weight = Tensor.fromFloat(weightData, Shape.of(1, 1, 3));

            OpAttribute attrs = OpAttribute.builder()
                    .put("padding", 1)
                    .build();

            List<Tensor> result = op.forward(List.of(input, weight), attrs);

            Tensor output = result.getFirst();
            // L_out = (3 + 2*1 - 3) / 1 + 1 = 3
            assertThat(output.shape()).isEqualTo(Shape.of(1, 1, 3));

            // With center-identity kernel and pad=1:
            // output[0] = 0*0 + 1*1 + 2*0 = 1
            // output[1] = 1*0 + 2*1 + 3*0 = 2
            // output[2] = 2*0 + 3*1 + 0*0 = 3
            assertThat(output.toFloatArray()).containsExactly(1.0f, 2.0f, 3.0f);
        }

        @Test
        @DisplayName("padding=1 + sum kernel 包含零填充贡献")
        void paddingWithSumKernel() {
            // Input: [1, 2, 3]
            float[] inputData = {1, 2, 3};
            Tensor input = Tensor.fromFloat(inputData, Shape.of(1, 1, 3));

            // Sum kernel: [1, 1, 1]
            float[] weightData = {1, 1, 1};
            Tensor weight = Tensor.fromFloat(weightData, Shape.of(1, 1, 3));

            OpAttribute attrs = OpAttribute.builder()
                    .put("padding", 1)
                    .build();

            List<Tensor> result = op.forward(List.of(input, weight), attrs);

            Tensor output = result.getFirst();
            assertThat(output.shape()).isEqualTo(Shape.of(1, 1, 3));

            // output[0] = 0+1+2 = 3
            // output[1] = 1+2+3 = 6
            // output[2] = 2+3+0 = 5
            assertThat(output.toFloatArray()).containsExactly(3.0f, 6.0f, 5.0f);
        }
    }

    @Nested
    @DisplayName("stride 测试")
    class StrideTest {

        @Test
        @DisplayName("stride=2 正确跳步输出")
        void stride2() {
            // Input: [1, 2, 3, 4, 5]
            float[] inputData = {1, 2, 3, 4, 5};
            Tensor input = Tensor.fromFloat(inputData, Shape.of(1, 1, 5));

            // Sum kernel: [1, 1, 1]
            float[] weightData = {1, 1, 1};
            Tensor weight = Tensor.fromFloat(weightData, Shape.of(1, 1, 3));

            OpAttribute attrs = OpAttribute.builder()
                    .put("stride", 2)
                    .build();

            List<Tensor> result = op.forward(List.of(input, weight), attrs);

            Tensor output = result.getFirst();
            // L_out = (5 - 3) / 2 + 1 = 2
            assertThat(output.shape()).isEqualTo(Shape.of(1, 1, 2));

            // output[0] = 1+2+3 = 6
            // output[1] = 3+4+5 = 12
            assertThat(output.toFloatArray()).containsExactly(6.0f, 12.0f);
        }
    }

    @Nested
    @DisplayName("偏置测试")
    class BiasTest {

        @Test
        @DisplayName("带偏置的卷积正确添加偏置值")
        void withBias() {
            // Input: [0, 0, 0, 0, 0]
            Tensor input = Tensor.zeros(Shape.of(1, 1, 5));

            // Weight: all zeros
            Tensor weight = Tensor.zeros(Shape.of(2, 1, 3));

            // Bias: [10.0, 20.0]
            Tensor bias = Tensor.fromFloat(new float[]{10.0f, 20.0f}, Shape.of(2));

            List<Tensor> result = op.forward(List.of(input, weight, bias), OpAttribute.empty());

            Tensor output = result.getFirst();
            // L_out = (5 - 3) / 1 + 1 = 3 → shape [1, 2, 3]
            assertThat(output.shape()).isEqualTo(Shape.of(1, 2, 3));

            // All weights zero, so output = bias only
            for (int j = 0; j < 3; j++) {
                assertThat(output.getFloat(0, 0, j)).isEqualTo(10.0f);
                assertThat(output.getFloat(0, 1, j)).isEqualTo(20.0f);
            }
        }
    }

    @Nested
    @DisplayName("多通道测试")
    class MultiChannelTest {

        @Test
        @DisplayName("input[1,2,4] + weight[1,2,3] 多通道正确聚合")
        void multiChannelInput() {
            // Input: 2 channels, length 4
            // Channel 0: [1, 1, 1, 1]
            // Channel 1: [2, 2, 2, 2]
            float[] inputData = {
                    1, 1, 1, 1,  // channel 0
                    2, 2, 2, 2   // channel 1
            };
            Tensor input = Tensor.fromFloat(inputData, Shape.of(1, 2, 4));

            // Weight: 1 output channel, 2 input channels, kernel size 3
            // Channel 0 kernel: [1, 1, 1]
            // Channel 1 kernel: [1, 1, 1]
            float[] weightData = {
                    1, 1, 1,  // input channel 0
                    1, 1, 1   // input channel 1
            };
            Tensor weight = Tensor.fromFloat(weightData, Shape.of(1, 2, 3));

            List<Tensor> result = op.forward(List.of(input, weight), OpAttribute.empty());

            Tensor output = result.getFirst();
            // L_out = (4 - 3) / 1 + 1 = 2
            assertThat(output.shape()).isEqualTo(Shape.of(1, 1, 2));

            // For each output position:
            // ch0 contribution: 1+1+1 = 3, ch1 contribution: 2+2+2 = 6 → total = 9
            assertThat(output.toFloatArray()).containsExactly(9.0f, 9.0f);
        }

        @Test
        @DisplayName("input[1,1,5] + weight[2,1,3] 多输出通道")
        void multiOutputChannel() {
            // Input: [1, 2, 3, 4, 5]
            float[] inputData = {1, 2, 3, 4, 5};
            Tensor input = Tensor.fromFloat(inputData, Shape.of(1, 1, 5));

            // Weight: 2 output channels
            // Output channel 0: [1, 1, 1] (sum)
            // Output channel 1: [1, 0, -1] (difference)
            float[] weightData = {
                    1, 1, 1,   // output channel 0
                    1, 0, -1   // output channel 1
            };
            Tensor weight = Tensor.fromFloat(weightData, Shape.of(2, 1, 3));

            List<Tensor> result = op.forward(List.of(input, weight), OpAttribute.empty());

            Tensor output = result.getFirst();
            assertThat(output.shape()).isEqualTo(Shape.of(1, 2, 3));

            // Output channel 0: [6, 9, 12]
            assertThat(output.getFloat(0, 0, 0)).isEqualTo(6.0f);
            assertThat(output.getFloat(0, 0, 1)).isEqualTo(9.0f);
            assertThat(output.getFloat(0, 0, 2)).isEqualTo(12.0f);

            // Output channel 1: [-2, -2, -2]
            assertThat(output.getFloat(0, 1, 0)).isEqualTo(-2.0f);
            assertThat(output.getFloat(0, 1, 1)).isEqualTo(-2.0f);
            assertThat(output.getFloat(0, 1, 2)).isEqualTo(-2.0f);
        }
    }

    @Nested
    @DisplayName("批次测试")
    class BatchTest {

        @Test
        @DisplayName("batch=2 正确处理多个样本")
        void batchProcessing() {
            // Input: 2 samples, 1 channel, length 3
            float[] inputData = {
                    1, 2, 3,  // sample 0
                    4, 5, 6   // sample 1
            };
            Tensor input = Tensor.fromFloat(inputData, Shape.of(2, 1, 3));

            // Weight: [1, 1, 1]
            float[] weightData = {1, 1, 1};
            Tensor weight = Tensor.fromFloat(weightData, Shape.of(1, 1, 3));

            List<Tensor> result = op.forward(List.of(input, weight), OpAttribute.empty());

            Tensor output = result.getFirst();
            // L_out = (3 - 3) / 1 + 1 = 1
            assertThat(output.shape()).isEqualTo(Shape.of(2, 1, 1));

            // Sample 0: 1+2+3 = 6
            assertThat(output.getFloat(0, 0, 0)).isEqualTo(6.0f);
            // Sample 1: 4+5+6 = 15
            assertThat(output.getFloat(1, 0, 0)).isEqualTo(15.0f);
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
            Tensor input = Tensor.zeros(Shape.of(1, 1, 5));
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
        @DisplayName("非3D输入张量抛出 OpExecutionException")
        void invalidInputRank() {
            Tensor input = Tensor.zeros(Shape.of(1, 5));
            Tensor weight = Tensor.zeros(Shape.of(1, 1, 3));
            assertThatThrownBy(() -> op.forward(List.of(input, weight), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class)
                    .hasMessageContaining("3D");
        }

        @Test
        @DisplayName("非3D权重张量抛出 OpExecutionException")
        void invalidWeightRank() {
            Tensor input = Tensor.zeros(Shape.of(1, 1, 5));
            Tensor weight = Tensor.zeros(Shape.of(1, 3));
            assertThatThrownBy(() -> op.forward(List.of(input, weight), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class)
                    .hasMessageContaining("3D");
        }

        @Test
        @DisplayName("通道数不匹配抛出 OpExecutionException")
        void channelMismatch() {
            Tensor input = Tensor.zeros(Shape.of(1, 2, 5));
            Tensor weight = Tensor.zeros(Shape.of(1, 3, 3));  // C_in=3 != 2
            assertThatThrownBy(() -> op.forward(List.of(input, weight), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class)
                    .hasMessageContaining("mismatch");
        }
    }
}
