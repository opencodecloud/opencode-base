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
 * Tests for {@link SoftmaxOp}
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-neural V1.0.0
 */
@DisplayName("SoftmaxOp")
class SoftmaxOpTest {

    private final SoftmaxOp softmax = new SoftmaxOp();

    @Nested
    @DisplayName("Last Axis Softmax")
    class LastAxisSoftmax {

        @Test
        @DisplayName("should compute softmax along last axis for 2D tensor")
        void softmaxLastAxis2D() {
            // Shape [2, 3], softmax along axis=-1 (last)
            Tensor input = Tensor.fromFloat(
                    new float[]{1.0f, 2.0f, 3.0f, 1.0f, 1.0f, 1.0f}, Shape.of(2, 3));

            List<Tensor> result = softmax.forward(List.of(input), OpAttribute.empty());
            assertThat(result).hasSize(1);

            float[] data = result.get(0).toFloatArray();
            // Row 0: softmax([1,2,3])
            float row0Sum = data[0] + data[1] + data[2];
            assertThat(row0Sum).isCloseTo(1.0f, within(1e-5f));
            // Row 1: softmax([1,1,1]) => [1/3, 1/3, 1/3]
            float row1Sum = data[3] + data[4] + data[5];
            assertThat(row1Sum).isCloseTo(1.0f, within(1e-5f));
            assertThat(data[3]).isCloseTo(1.0f / 3.0f, within(1e-5f));
        }
    }

    @Nested
    @DisplayName("Non-Last Axis Softmax")
    class NonLastAxisSoftmax {

        @Test
        @DisplayName("should compute softmax along axis=0 for [2,3] tensor — each column sums to 1.0")
        void softmaxAxis0On2x3() {
            // Shape [2, 3]:
            // [[1.0, 2.0, 3.0],
            //  [4.0, 5.0, 6.0]]
            // Softmax along axis=0 means for each column j, softmax over rows
            Tensor input = Tensor.fromFloat(
                    new float[]{1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f}, Shape.of(2, 3));

            OpAttribute attrs = OpAttribute.builder().put("axis", 0).build();
            List<Tensor> result = softmax.forward(List.of(input), attrs);
            assertThat(result).hasSize(1);

            Tensor output = result.get(0);
            assertThat(output.shape().rank()).isEqualTo(2);
            assertThat(output.shape().dim(0)).isEqualTo(2);
            assertThat(output.shape().dim(1)).isEqualTo(3);

            float[] data = output.toFloatArray();
            // Each column should sum to 1.0
            for (int col = 0; col < 3; col++) {
                float colSum = data[col] + data[3 + col]; // row0[col] + row1[col]
                assertThat(colSum).as("column %d sum", col).isCloseTo(1.0f, within(1e-5f));
            }

            // For column 0: softmax([1, 4]) => [exp(1)/(exp(1)+exp(4)), exp(4)/(exp(1)+exp(4))]
            float e1 = (float) Math.exp(1.0);
            float e4 = (float) Math.exp(4.0);
            float expected00 = e1 / (e1 + e4);
            float expected10 = e4 / (e1 + e4);
            assertThat(data[0]).isCloseTo(expected00, within(1e-5f));
            assertThat(data[3]).isCloseTo(expected10, within(1e-5f));
        }

        @Test
        @DisplayName("should compute softmax along axis=0 for [3,2,2] tensor")
        void softmaxAxis0On3x2x2() {
            // Shape [3, 2, 2] — softmax along axis=0 (size 3)
            // For each (i, j) in [2, 2], softmax over the 3 elements at [:, i, j]
            float[] inputData = new float[]{
                    // slice 0
                    1.0f, 2.0f,
                    3.0f, 4.0f,
                    // slice 1
                    5.0f, 6.0f,
                    7.0f, 8.0f,
                    // slice 2
                    9.0f, 10.0f,
                    11.0f, 12.0f
            };
            Tensor input = Tensor.fromFloat(inputData, Shape.of(3, 2, 2));
            OpAttribute attrs = OpAttribute.builder().put("axis", 0).build();

            List<Tensor> result = softmax.forward(List.of(input), attrs);
            float[] data = result.get(0).toFloatArray();

            // Check each (inner_row, inner_col) position sums to 1.0 across axis 0
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    int idx = i * 2 + j;
                    // Elements at position (0,i,j), (1,i,j), (2,i,j)
                    float sum = data[idx] + data[4 + idx] + data[8 + idx];
                    assertThat(sum).as("position (%d,%d) sum", i, j)
                            .isCloseTo(1.0f, within(1e-5f));
                }
            }
        }

        @Test
        @DisplayName("should compute softmax along axis=1 for [2,3,4] tensor")
        void softmaxAxis1On2x3x4() {
            // Shape [2, 3, 4] — softmax along axis=1 (size 3)
            float[] inputData = new float[2 * 3 * 4];
            for (int i = 0; i < inputData.length; i++) {
                inputData[i] = i * 0.1f;
            }
            Tensor input = Tensor.fromFloat(inputData, Shape.of(2, 3, 4));
            OpAttribute attrs = OpAttribute.builder().put("axis", 1).build();

            List<Tensor> result = softmax.forward(List.of(input), attrs);
            float[] data = result.get(0).toFloatArray();

            // For each (batch, col) pair, sum over the 3 axis-1 elements should be 1.0
            for (int b = 0; b < 2; b++) {
                for (int c = 0; c < 4; c++) {
                    float sum = 0;
                    for (int a = 0; a < 3; a++) {
                        sum += data[b * 12 + a * 4 + c];
                    }
                    assertThat(sum).as("batch=%d, col=%d sum", b, c)
                            .isCloseTo(1.0f, within(1e-5f));
                }
            }
        }
    }
}
