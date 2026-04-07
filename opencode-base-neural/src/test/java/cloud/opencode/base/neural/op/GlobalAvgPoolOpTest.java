package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.exception.OpExecutionException;
import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link GlobalAvgPoolOp}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("GlobalAvgPoolOp — 全局平均池化算子")
class GlobalAvgPoolOpTest {

    private final GlobalAvgPoolOp op = new GlobalAvgPoolOp();

    @Nested
    @DisplayName("forward — 前向计算")
    class ForwardTest {

        @Test
        @DisplayName("input[1,2,3,3] → output[1,2,1,1] 正确计算平均值")
        void globalAvgPoolBasic() {
            // Channel 0: 3x3 with values 1..9, mean = 5.0
            // Channel 1: 3x3 with values 10..18, mean = 14.0
            float[] data = {
                    // Channel 0
                    1, 2, 3,
                    4, 5, 6,
                    7, 8, 9,
                    // Channel 1
                    10, 11, 12,
                    13, 14, 15,
                    16, 17, 18
            };
            Tensor input = Tensor.fromFloat(data, Shape.of(1, 2, 3, 3));

            List<Tensor> outputs = op.forward(List.of(input), OpAttribute.empty());

            assertThat(outputs).hasSize(1);
            Tensor output = outputs.getFirst();
            assertThat(output.shape().dims()).containsExactly(1, 2, 1, 1);

            // Channel 0: mean(1..9) = 45/9 = 5.0
            assertThat(output.getFloat(0, 0, 0, 0)).isCloseTo(5.0f, within(1e-5f));
            // Channel 1: mean(10..18) = 126/9 = 14.0
            assertThat(output.getFloat(0, 1, 0, 0)).isCloseTo(14.0f, within(1e-5f));
        }

        @Test
        @DisplayName("单像素 input[1,1,1,1] → 值不变")
        void globalAvgPoolSinglePixel() {
            float[] data = {42.0f};
            Tensor input = Tensor.fromFloat(data, Shape.of(1, 1, 1, 1));

            List<Tensor> outputs = op.forward(List.of(input), OpAttribute.empty());

            assertThat(outputs).hasSize(1);
            Tensor output = outputs.getFirst();
            assertThat(output.shape().dims()).containsExactly(1, 1, 1, 1);
            assertThat(output.getFloat(0, 0, 0, 0)).isEqualTo(42.0f);
        }

        @Test
        @DisplayName("多批次 input[2,1,2,2] → output[2,1,1,1] 各批次独立计算")
        void globalAvgPoolMultiBatch() {
            float[] data = {
                    // Batch 0, Channel 0: mean(1,2,3,4) = 2.5
                    1, 2,
                    3, 4,
                    // Batch 1, Channel 0: mean(10,20,30,40) = 25.0
                    10, 20,
                    30, 40
            };
            Tensor input = Tensor.fromFloat(data, Shape.of(2, 1, 2, 2));

            List<Tensor> outputs = op.forward(List.of(input), OpAttribute.empty());

            Tensor output = outputs.getFirst();
            assertThat(output.shape().dims()).containsExactly(2, 1, 1, 1);
            assertThat(output.getFloat(0, 0, 0, 0)).isCloseTo(2.5f, within(1e-5f));
            assertThat(output.getFloat(1, 0, 0, 0)).isCloseTo(25.0f, within(1e-5f));
        }
    }

    @Nested
    @DisplayName("异常处理")
    class ExceptionTest {

        @Test
        @DisplayName("null 输入 → OpExecutionException")
        void nullInputThrows() {
            assertThatThrownBy(() -> op.forward(null, OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }

        @Test
        @DisplayName("空输入列表 → OpExecutionException")
        void emptyInputThrows() {
            assertThatThrownBy(() -> op.forward(List.of(), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }

        @Test
        @DisplayName("非 4D 输入 → OpExecutionException")
        void non4DInputThrows() {
            Tensor input = Tensor.fromFloat(new float[]{1, 2, 3}, Shape.of(1, 3));
            assertThatThrownBy(() -> op.forward(List.of(input), OpAttribute.empty()))
                    .isInstanceOf(OpExecutionException.class);
        }
    }
}
