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
 * Tests for {@link DepthwiseConv2DOp}.
 */
@DisplayName("DepthwiseConv2DOp — 深度可分离二维卷积算子测试")
class DepthwiseConv2DOpTest {

    private final DepthwiseConv2DOp op = new DepthwiseConv2DOp();

    @Nested
    @DisplayName("基本深度卷积")
    class BasicDepthwiseTest {

        @Test
        @DisplayName("input[1,3,4,4] + weight[3,1,3,3] → output[1,3,2,2]")
        void threeChannel() {
            // Input: 1 batch, 3 channels, 4x4
            // Channel 0: all 1s, Channel 1: all 2s, Channel 2: all 3s
            float[] inputData = new float[3 * 4 * 4];
            for (int c = 0; c < 3; c++) {
                for (int i = 0; i < 16; i++) {
                    inputData[c * 16 + i] = c + 1;
                }
            }
            Tensor input = Tensor.fromFloat(inputData, Shape.of(1, 3, 4, 4));

            // Weight: 3 channels, depth=1, 3x3 kernel (all ones for each channel)
            float[] weightData = new float[3 * 1 * 3 * 3];
            for (int i = 0; i < weightData.length; i++) {
                weightData[i] = 1.0f;
            }
            Tensor weight = Tensor.fromFloat(weightData, Shape.of(3, 1, 3, 3));

            List<Tensor> result = op.forward(List.of(input, weight), OpAttribute.empty());

            assertThat(result).hasSize(1);
            Tensor output = result.getFirst();
            // outH = (4-3)/1+1 = 2, outW = 2 → shape [1, 3, 2, 2]
            assertThat(output.shape()).isEqualTo(Shape.of(1, 3, 2, 2));

            // Channel 0: 3x3 sum of 1s = 9 at each position
            // Channel 1: 3x3 sum of 2s = 18 at each position
            // Channel 2: 3x3 sum of 3s = 27 at each position
            for (int oh = 0; oh < 2; oh++) {
                for (int ow = 0; ow < 2; ow++) {
                    assertThat(output.getFloat(0, 0, oh, ow)).isEqualTo(9.0f);
                    assertThat(output.getFloat(0, 1, oh, ow)).isEqualTo(18.0f);
                    assertThat(output.getFloat(0, 2, oh, ow)).isEqualTo(27.0f);
                }
            }
        }
    }

    @Nested
    @DisplayName("通道独立性测试")
    class ChannelIndependenceTest {

        @Test
        @DisplayName("各通道使用不同权重独立卷积")
        void preservesChannelIndependence() {
            // Input: 1 batch, 2 channels, 3x3
            // Channel 0: all 1s, Channel 1: all 1s
            float[] inputData = new float[2 * 3 * 3];
            for (int i = 0; i < inputData.length; i++) {
                inputData[i] = 1.0f;
            }
            Tensor input = Tensor.fromFloat(inputData, Shape.of(1, 2, 3, 3));

            // Weight: channel 0 kernel = all 1s, channel 1 kernel = all 2s
            float[] weightData = new float[2 * 1 * 3 * 3];
            for (int i = 0; i < 9; i++) {
                weightData[i] = 1.0f;       // channel 0 kernel
                weightData[9 + i] = 2.0f;   // channel 1 kernel
            }
            Tensor weight = Tensor.fromFloat(weightData, Shape.of(2, 1, 3, 3));

            List<Tensor> result = op.forward(List.of(input, weight), OpAttribute.empty());

            Tensor output = result.getFirst();
            // outH = (3-3)/1+1 = 1, outW = 1 → shape [1, 2, 1, 1]
            assertThat(output.shape()).isEqualTo(Shape.of(1, 2, 1, 1));

            // Channel 0: sum of 9 * (1*1) = 9
            // Channel 1: sum of 9 * (1*2) = 18 (independent kernel)
            assertThat(output.getFloat(0, 0, 0, 0)).isEqualTo(9.0f);
            assertThat(output.getFloat(0, 1, 0, 0)).isEqualTo(18.0f);
        }
    }

    @Nested
    @DisplayName("stride 和 padding 测试")
    class StridePaddingTest {

        @Test
        @DisplayName("stride=2, padding=1 正确输出")
        void stride2Padding1() {
            // Input: 1 batch, 1 channel, 4x4 (values 1..16)
            float[] inputData = new float[16];
            for (int i = 0; i < 16; i++) {
                inputData[i] = i + 1;
            }
            Tensor input = Tensor.fromFloat(inputData, Shape.of(1, 1, 4, 4));

            // Identity kernel: center=1
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
            // outH = (4+2-3)/2+1 = 2, outW = 2
            assertThat(output.shape()).isEqualTo(Shape.of(1, 1, 2, 2));
            // Identity kernel picks input at (0,0),(0,2),(2,0),(2,2) => 1,3,9,11
            assertThat(output.toFloatArray()).containsExactly(1, 3, 9, 11);
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
        @DisplayName("weight dim(1) != 1 抛出 OpExecutionException")
        void invalidWeightDepth() {
            Tensor input = Tensor.zeros(Shape.of(1, 2, 3, 3));
            Tensor weight = Tensor.zeros(Shape.of(2, 2, 3, 3)); // dim(1)=2, should be 1
            assertThatThrownBy(() -> op.forward(List.of(input, weight), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class)
                    .hasMessageContaining("1");
        }
    }
}
