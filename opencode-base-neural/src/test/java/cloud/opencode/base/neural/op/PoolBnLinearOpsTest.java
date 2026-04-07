package cloud.opencode.base.neural.op;

import cloud.opencode.base.neural.tensor.Shape;
import cloud.opencode.base.neural.tensor.Tensor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link MaxPool2DOp}, {@link AvgPool2DOp}, {@link BatchNormOp}, and {@link LinearOp}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("Pool + BatchNorm + Linear 算子")
class PoolBnLinearOpsTest {

    private static final float EPSILON = 1e-5f;

    // ==================== MaxPool2D ====================

    @Nested
    @DisplayName("MaxPool2DOp — 二维最大池化")
    class MaxPool2DOpTest {

        @Test
        @DisplayName("input[1,1,4,4] kernel=2 stride=2 → [1,1,2,2] 取最大值")
        void maxPool2dBasic() {
            // Input 4x4:
            // 1  2  3  4
            // 5  6  7  8
            // 9  10 11 12
            // 13 14 15 16
            float[] data = {
                    1, 2, 3, 4,
                    5, 6, 7, 8,
                    9, 10, 11, 12,
                    13, 14, 15, 16
            };
            Tensor input = Tensor.fromFloat(data, Shape.of(1, 1, 4, 4));

            OpAttribute attrs = OpAttribute.builder()
                    .put("kernel_size", 2)
                    .put("stride", 2)
                    .build();

            MaxPool2DOp op = new MaxPool2DOp();
            List<Tensor> outputs = op.forward(List.of(input), attrs);

            assertThat(outputs).hasSize(1);
            Tensor output = outputs.getFirst();
            assertThat(output.shape().dims()).containsExactly(1, 1, 2, 2);

            // Window (0,0): max(1,2,5,6) = 6
            // Window (0,1): max(3,4,7,8) = 8
            // Window (1,0): max(9,10,13,14) = 14
            // Window (1,1): max(11,12,15,16) = 16
            assertThat(output.getFloat(0, 0, 0, 0)).isEqualTo(6.0f);
            assertThat(output.getFloat(0, 0, 0, 1)).isEqualTo(8.0f);
            assertThat(output.getFloat(0, 0, 1, 0)).isEqualTo(14.0f);
            assertThat(output.getFloat(0, 0, 1, 1)).isEqualTo(16.0f);
        }

        @Test
        @DisplayName("kernel=3 stride=1 → 滑动窗口取最大值")
        void maxPool2dKernel3Stride1() {
            // Input 4x4 with same data
            float[] data = {
                    1, 2, 3, 4,
                    5, 6, 7, 8,
                    9, 10, 11, 12,
                    13, 14, 15, 16
            };
            Tensor input = Tensor.fromFloat(data, Shape.of(1, 1, 4, 4));

            OpAttribute attrs = OpAttribute.builder()
                    .put("kernel_size", 3)
                    .put("stride", 1)
                    .build();

            MaxPool2DOp op = new MaxPool2DOp();
            List<Tensor> outputs = op.forward(List.of(input), attrs);

            Tensor output = outputs.getFirst();
            // (4 - 3)/1 + 1 = 2
            assertThat(output.shape().dims()).containsExactly(1, 1, 2, 2);

            // Window (0,0): max of rows 0-2, cols 0-2 = 11
            assertThat(output.getFloat(0, 0, 0, 0)).isEqualTo(11.0f);
            // Window (0,1): max of rows 0-2, cols 1-3 = 12
            assertThat(output.getFloat(0, 0, 0, 1)).isEqualTo(12.0f);
            // Window (1,0): max of rows 1-3, cols 0-2 = 15
            assertThat(output.getFloat(0, 0, 1, 0)).isEqualTo(15.0f);
            // Window (1,1): max of rows 1-3, cols 1-3 = 16
            assertThat(output.getFloat(0, 0, 1, 1)).isEqualTo(16.0f);
        }

        @Test
        @DisplayName("stride 默认等于 kernel_size")
        void maxPool2dDefaultStride() {
            float[] data = {
                    1, 2, 3, 4,
                    5, 6, 7, 8,
                    9, 10, 11, 12,
                    13, 14, 15, 16
            };
            Tensor input = Tensor.fromFloat(data, Shape.of(1, 1, 4, 4));

            // Only set kernel_size, stride should default to kernel_size=2
            OpAttribute attrs = OpAttribute.builder()
                    .put("kernel_size", 2)
                    .build();

            MaxPool2DOp op = new MaxPool2DOp();
            List<Tensor> outputs = op.forward(List.of(input), attrs);

            Tensor output = outputs.getFirst();
            assertThat(output.shape().dims()).containsExactly(1, 1, 2, 2);
            assertThat(output.getFloat(0, 0, 0, 0)).isEqualTo(6.0f);
        }
    }

    // ==================== AvgPool2D ====================

    @Nested
    @DisplayName("AvgPool2DOp — 二维平均池化")
    class AvgPool2DOpTest {

        @Test
        @DisplayName("input[1,1,4,4] kernel=2 stride=2 → [1,1,2,2] 取平均值")
        void avgPool2dBasic() {
            float[] data = {
                    1, 2, 3, 4,
                    5, 6, 7, 8,
                    9, 10, 11, 12,
                    13, 14, 15, 16
            };
            Tensor input = Tensor.fromFloat(data, Shape.of(1, 1, 4, 4));

            OpAttribute attrs = OpAttribute.builder()
                    .put("kernel_size", 2)
                    .put("stride", 2)
                    .build();

            AvgPool2DOp op = new AvgPool2DOp();
            List<Tensor> outputs = op.forward(List.of(input), attrs);

            assertThat(outputs).hasSize(1);
            Tensor output = outputs.getFirst();
            assertThat(output.shape().dims()).containsExactly(1, 1, 2, 2);

            // Window (0,0): avg(1,2,5,6) = 3.5
            // Window (0,1): avg(3,4,7,8) = 5.5
            // Window (1,0): avg(9,10,13,14) = 11.5
            // Window (1,1): avg(11,12,15,16) = 13.5
            assertThat(output.getFloat(0, 0, 0, 0)).isCloseTo(3.5f, within(EPSILON));
            assertThat(output.getFloat(0, 0, 0, 1)).isCloseTo(5.5f, within(EPSILON));
            assertThat(output.getFloat(0, 0, 1, 0)).isCloseTo(11.5f, within(EPSILON));
            assertThat(output.getFloat(0, 0, 1, 1)).isCloseTo(13.5f, within(EPSILON));
        }

        @Test
        @DisplayName("全局平均池化 — kernel_size = H,W")
        void globalAvgPool() {
            // Input [1, 2, 3, 3] — 2 channels, 3x3 spatial
            float[] data = {
                    // Channel 0:
                    1, 2, 3,
                    4, 5, 6,
                    7, 8, 9,
                    // Channel 1:
                    10, 20, 30,
                    40, 50, 60,
                    70, 80, 90
            };
            Tensor input = Tensor.fromFloat(data, Shape.of(1, 2, 3, 3));

            // Global average pooling: kernel_size = 3 (same as H and W)
            OpAttribute attrs = OpAttribute.builder()
                    .put("kernel_size", 3)
                    .put("stride", 3)
                    .build();

            AvgPool2DOp op = new AvgPool2DOp();
            List<Tensor> outputs = op.forward(List.of(input), attrs);

            Tensor output = outputs.getFirst();
            assertThat(output.shape().dims()).containsExactly(1, 2, 1, 1);

            // Channel 0: avg(1..9) = 45/9 = 5.0
            assertThat(output.getFloat(0, 0, 0, 0)).isCloseTo(5.0f, within(EPSILON));
            // Channel 1: avg(10,20..90) = 450/9 = 50.0
            assertThat(output.getFloat(0, 1, 0, 0)).isCloseTo(50.0f, within(EPSILON));
        }

        @Test
        @DisplayName("stride 默认等于 kernel_size")
        void avgPool2dDefaultStride() {
            float[] data = {
                    1, 2, 3, 4,
                    5, 6, 7, 8,
                    9, 10, 11, 12,
                    13, 14, 15, 16
            };
            Tensor input = Tensor.fromFloat(data, Shape.of(1, 1, 4, 4));

            OpAttribute attrs = OpAttribute.builder()
                    .put("kernel_size", 2)
                    .build();

            AvgPool2DOp op = new AvgPool2DOp();
            List<Tensor> outputs = op.forward(List.of(input), attrs);

            Tensor output = outputs.getFirst();
            assertThat(output.shape().dims()).containsExactly(1, 1, 2, 2);
            assertThat(output.getFloat(0, 0, 0, 0)).isCloseTo(3.5f, within(EPSILON));
        }
    }

    // ==================== BatchNorm ====================

    @Nested
    @DisplayName("BatchNormOp — 批归一化")
    class BatchNormOpTest {

        @Test
        @DisplayName("已知输入/参数 → 已知输出")
        void batchNormKnownValues() {
            // Input [1, 2, 1, 1] — 2 channels, 1x1 spatial
            float[] inputData = {3.0f, 7.0f};
            Tensor input = Tensor.fromFloat(inputData, Shape.of(1, 2, 1, 1));

            // Per-channel params
            Tensor scale = Tensor.fromFloat(new float[]{2.0f, 0.5f}, Shape.of(2));
            Tensor bias = Tensor.fromFloat(new float[]{1.0f, -1.0f}, Shape.of(2));
            Tensor mean = Tensor.fromFloat(new float[]{1.0f, 3.0f}, Shape.of(2));
            Tensor var = Tensor.fromFloat(new float[]{4.0f, 16.0f}, Shape.of(2));

            OpAttribute attrs = OpAttribute.builder()
                    .put("epsilon", 0.0f)
                    .build();

            // But epsilon must be > 0 in our impl, so use very small value
            attrs = OpAttribute.builder()
                    .put("epsilon", 1e-10f)
                    .build();

            BatchNormOp op = new BatchNormOp();
            List<Tensor> outputs = op.forward(List.of(input, scale, bias, mean, var), attrs);

            Tensor output = outputs.getFirst();
            assertThat(output.shape().dims()).containsExactly(1, 2, 1, 1);

            // Channel 0: y = 2.0 * (3.0 - 1.0) / sqrt(4.0 + eps) + 1.0
            //           = 2.0 * 2.0 / 2.0 + 1.0 = 3.0
            assertThat(output.getFloat(0, 0, 0, 0)).isCloseTo(3.0f, within(1e-4f));

            // Channel 1: y = 0.5 * (7.0 - 3.0) / sqrt(16.0 + eps) + (-1.0)
            //           = 0.5 * 4.0 / 4.0 + (-1.0) = -0.5
            assertThat(output.getFloat(0, 1, 0, 0)).isCloseTo(-0.5f, within(1e-4f));
        }

        @Test
        @DisplayName("epsilon 处理 — 小方差下的数值稳定性")
        void batchNormEpsilonHandling() {
            // Input with very small variance — epsilon prevents division by zero
            float[] inputData = {5.0f};
            Tensor input = Tensor.fromFloat(inputData, Shape.of(1, 1, 1, 1));

            Tensor scale = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));
            Tensor bias = Tensor.fromFloat(new float[]{0.0f}, Shape.of(1));
            Tensor mean = Tensor.fromFloat(new float[]{5.0f}, Shape.of(1));
            Tensor var = Tensor.fromFloat(new float[]{0.0f}, Shape.of(1));

            float eps = 1e-5f;
            OpAttribute attrs = OpAttribute.builder()
                    .put("epsilon", eps)
                    .build();

            BatchNormOp op = new BatchNormOp();
            List<Tensor> outputs = op.forward(List.of(input, scale, bias, mean, var), attrs);

            Tensor output = outputs.getFirst();
            // y = 1.0 * (5.0 - 5.0) / sqrt(0.0 + 1e-5) + 0.0 = 0.0
            assertThat(output.getFloat(0, 0, 0, 0)).isCloseTo(0.0f, within(1e-4f));
        }

        @Test
        @DisplayName("多空间位置的批归一化")
        void batchNormSpatial() {
            // Input [1, 1, 2, 2]
            float[] inputData = {1.0f, 2.0f, 3.0f, 4.0f};
            Tensor input = Tensor.fromFloat(inputData, Shape.of(1, 1, 2, 2));

            Tensor scale = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));
            Tensor bias = Tensor.fromFloat(new float[]{0.0f}, Shape.of(1));
            Tensor mean = Tensor.fromFloat(new float[]{0.0f}, Shape.of(1));
            Tensor var = Tensor.fromFloat(new float[]{1.0f}, Shape.of(1));

            OpAttribute attrs = OpAttribute.builder()
                    .put("epsilon", 1e-10f)
                    .build();

            BatchNormOp op = new BatchNormOp();
            List<Tensor> outputs = op.forward(List.of(input, scale, bias, mean, var), attrs);

            Tensor output = outputs.getFirst();
            assertThat(output.shape().dims()).containsExactly(1, 1, 2, 2);

            // With mean=0, var=1, scale=1, bias=0: output ≈ input
            assertThat(output.getFloat(0, 0, 0, 0)).isCloseTo(1.0f, within(1e-4f));
            assertThat(output.getFloat(0, 0, 0, 1)).isCloseTo(2.0f, within(1e-4f));
            assertThat(output.getFloat(0, 0, 1, 0)).isCloseTo(3.0f, within(1e-4f));
            assertThat(output.getFloat(0, 0, 1, 1)).isCloseTo(4.0f, within(1e-4f));
        }
    }

    // ==================== Linear ====================

    @Nested
    @DisplayName("LinearOp — 线性（全连接）层")
    class LinearOpTest {

        @Test
        @DisplayName("input[1,3] weight[2,3] bias[2] → output[1,2] 已知值")
        void linearWithBias() {
            // input = [1, 2, 3]
            Tensor input = Tensor.fromFloat(new float[]{1.0f, 2.0f, 3.0f}, Shape.of(1, 3));

            // weight = [[1, 0, 0],   → dot with input = 1
            //           [0, 1, 0]]   → dot with input = 2
            Tensor weight = Tensor.fromFloat(new float[]{
                    1.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f
            }, Shape.of(2, 3));

            // bias = [10, 20]
            Tensor bias = Tensor.fromFloat(new float[]{10.0f, 20.0f}, Shape.of(2));

            LinearOp op = new LinearOp();
            List<Tensor> outputs = op.forward(List.of(input, weight, bias), OpAttribute.empty());

            assertThat(outputs).hasSize(1);
            Tensor output = outputs.getFirst();
            assertThat(output.shape().dims()).containsExactly(1, 2);

            // output[0] = 1*1 + 2*0 + 3*0 + 10 = 11
            assertThat(output.getFloat(0, 0)).isCloseTo(11.0f, within(EPSILON));
            // output[1] = 1*0 + 2*1 + 3*0 + 20 = 22
            assertThat(output.getFloat(0, 1)).isCloseTo(22.0f, within(EPSILON));
        }

        @Test
        @DisplayName("无偏置的线性变换")
        void linearWithoutBias() {
            // input = [1, 2, 3]
            Tensor input = Tensor.fromFloat(new float[]{1.0f, 2.0f, 3.0f}, Shape.of(1, 3));

            // weight = [[1, 2, 3],   → dot with input = 1+4+9 = 14
            //           [4, 5, 6]]   → dot with input = 4+10+18 = 32
            Tensor weight = Tensor.fromFloat(new float[]{
                    1.0f, 2.0f, 3.0f,
                    4.0f, 5.0f, 6.0f
            }, Shape.of(2, 3));

            LinearOp op = new LinearOp();
            List<Tensor> outputs = op.forward(List.of(input, weight), OpAttribute.empty());

            Tensor output = outputs.getFirst();
            assertThat(output.shape().dims()).containsExactly(1, 2);

            assertThat(output.getFloat(0, 0)).isCloseTo(14.0f, within(EPSILON));
            assertThat(output.getFloat(0, 1)).isCloseTo(32.0f, within(EPSILON));
        }

        @Test
        @DisplayName("批量线性变换 — input[2,3] → output[2,2]")
        void linearBatch() {
            // input = [[1, 0, 0], [0, 1, 0]]
            Tensor input = Tensor.fromFloat(new float[]{
                    1.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f
            }, Shape.of(2, 3));

            // weight = [[1, 2, 3], [4, 5, 6]]
            Tensor weight = Tensor.fromFloat(new float[]{
                    1.0f, 2.0f, 3.0f,
                    4.0f, 5.0f, 6.0f
            }, Shape.of(2, 3));

            Tensor bias = Tensor.fromFloat(new float[]{0.0f, 0.0f}, Shape.of(2));

            LinearOp op = new LinearOp();
            List<Tensor> outputs = op.forward(List.of(input, weight, bias), OpAttribute.empty());

            Tensor output = outputs.getFirst();
            assertThat(output.shape().dims()).containsExactly(2, 2);

            // Row 0: [1,0,0] @ [[1,4],[2,5],[3,6]] = [1, 4]
            assertThat(output.getFloat(0, 0)).isCloseTo(1.0f, within(EPSILON));
            assertThat(output.getFloat(0, 1)).isCloseTo(4.0f, within(EPSILON));

            // Row 1: [0,1,0] @ [[1,4],[2,5],[3,6]] = [2, 5]
            assertThat(output.getFloat(1, 0)).isCloseTo(2.0f, within(EPSILON));
            assertThat(output.getFloat(1, 1)).isCloseTo(5.0f, within(EPSILON));
        }
    }
}
