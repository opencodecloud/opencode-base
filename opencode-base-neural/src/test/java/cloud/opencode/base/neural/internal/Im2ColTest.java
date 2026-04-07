package cloud.opencode.base.neural.internal;

import cloud.opencode.base.neural.exception.NeuralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link Im2Col}.
 */
@DisplayName("Im2Col — im2col 列展开测试")
class Im2ColTest {

    @Nested
    @DisplayName("outputHeight / outputWidth — 输出维度计算")
    class OutputDimensionsTest {

        @Test
        @DisplayName("3x3 输入, 2x2 核, stride=1, pad=0 → 2x2 输出")
        void basicOutputDimensions() {
            assertThat(Im2Col.outputHeight(3, 2, 1, 0)).isEqualTo(2);
            assertThat(Im2Col.outputWidth(3, 2, 1, 0)).isEqualTo(2);
        }

        @Test
        @DisplayName("4x4 输入, 2x2 核, stride=2, pad=0 → 2x2 输出")
        void stride2OutputDimensions() {
            assertThat(Im2Col.outputHeight(4, 2, 2, 0)).isEqualTo(2);
            assertThat(Im2Col.outputWidth(4, 2, 2, 0)).isEqualTo(2);
        }

        @Test
        @DisplayName("3x3 输入, 3x3 核, stride=1, pad=1 → 3x3 输出")
        void withPaddingOutputDimensions() {
            assertThat(Im2Col.outputHeight(3, 3, 1, 1)).isEqualTo(3);
            assertThat(Im2Col.outputWidth(3, 3, 1, 1)).isEqualTo(3);
        }

        @Test
        @DisplayName("stride <= 0 抛出异常")
        void invalidStride() {
            assertThatThrownBy(() -> Im2Col.outputHeight(3, 2, 0, 0))
                    .isInstanceOf(NeuralException.class);
        }
    }

    @Nested
    @DisplayName("im2col — 列展开变换")
    class Im2ColTransformTest {

        @Test
        @DisplayName("1通道 3x3 输入, 2x2 核, stride=1, pad=0 → 已知列矩阵")
        void singleChannel3x3() {
            // Input (1 channel, 3x3):
            // 1 2 3
            // 4 5 6
            // 7 8 9
            float[] input = {1, 2, 3, 4, 5, 6, 7, 8, 9};

            int outH = Im2Col.outputHeight(3, 2, 1, 0); // 2
            int outW = Im2Col.outputWidth(3, 2, 1, 0);  // 2
            int outRows = 1 * 2 * 2; // C*kH*kW = 4
            int outCols = outH * outW; // 4
            float[] output = new float[outRows * outCols];

            Im2Col.im2col(input, 1, 3, 3, 2, 2, 1, 1, 0, 0, output);

            // Output is [C*kH*kW, outH*outW] = [4, 4]
            // Row 0 (c=0, ky=0, kx=0): patches at (0,0),(0,1),(1,0),(1,1) → 1,2,4,5
            // Row 1 (c=0, ky=0, kx=1): → 2,3,5,6
            // Row 2 (c=0, ky=1, kx=0): → 4,5,7,8
            // Row 3 (c=0, ky=1, kx=1): → 5,6,8,9
            assertThat(output).containsExactly(
                    1, 2, 4, 5,
                    2, 3, 5, 6,
                    4, 5, 7, 8,
                    5, 6, 8, 9
            );
        }

        @Test
        @DisplayName("stride=2 正确下采样")
        void stride2() {
            // 4x4 input, 2x2 kernel, stride=2, pad=0 → outH=2, outW=2
            float[] input = {
                    1, 2, 3, 4,
                    5, 6, 7, 8,
                    9, 10, 11, 12,
                    13, 14, 15, 16
            };

            int outH = Im2Col.outputHeight(4, 2, 2, 0); // 2
            int outW = Im2Col.outputWidth(4, 2, 2, 0);  // 2
            float[] output = new float[4 * 4]; // C*kH*kW=4, outH*outW=4

            Im2Col.im2col(input, 1, 4, 4, 2, 2, 2, 2, 0, 0, output);

            // 4 output positions: (0,0),(0,2),(2,0),(2,2)
            // Row 0 (ky=0,kx=0): 1, 3, 9, 11
            // Row 1 (ky=0,kx=1): 2, 4, 10, 12
            // Row 2 (ky=1,kx=0): 5, 7, 13, 15
            // Row 3 (ky=1,kx=1): 6, 8, 14, 16
            assertThat(output).containsExactly(
                    1, 3, 9, 11,
                    2, 4, 10, 12,
                    5, 7, 13, 15,
                    6, 8, 14, 16
            );
        }

        @Test
        @DisplayName("有填充时越界位置为零")
        void withPadding() {
            // 2x2 input, 2x2 kernel, stride=1, pad=1 → outH=3, outW=3
            float[] input = {1, 2, 3, 4};
            int outH = Im2Col.outputHeight(2, 2, 1, 1); // 3
            int outW = Im2Col.outputWidth(2, 2, 1, 1);  // 3
            float[] output = new float[4 * 9]; // C*kH*kW=4, outH*outW=9

            Im2Col.im2col(input, 1, 2, 2, 2, 2, 1, 1, 1, 1, output);

            // Row 0 (ky=0,kx=0): positions (oh-1, ow-1) for oh=0..2, ow=0..2
            // (-1,-1)=0, (-1,0)=0, (-1,1)=0, (0,-1)=0, (0,0)=1, (0,1)=2, (1,-1)=0, (1,0)=3, (1,1)=4
            assertThat(output[0]).isEqualTo(0f); // pad
            assertThat(output[4]).isEqualTo(1f); // (0,0)
            assertThat(output[5]).isEqualTo(2f); // (0,1)
        }

        @Test
        @DisplayName("null 输入抛出 NullPointerException")
        void nullInput() {
            assertThatNullPointerException().isThrownBy(() ->
                    Im2Col.im2col(null, 1, 3, 3, 2, 2, 1, 1, 0, 0, new float[16]));
        }

        @Test
        @DisplayName("无效通道数抛出 NeuralException")
        void invalidChannels() {
            assertThatThrownBy(() ->
                    Im2Col.im2col(new float[9], 0, 3, 3, 2, 2, 1, 1, 0, 0, new float[16]))
                    .isInstanceOf(NeuralException.class);
        }
    }
}
