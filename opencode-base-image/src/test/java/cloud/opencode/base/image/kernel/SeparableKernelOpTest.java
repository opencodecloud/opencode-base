package cloud.opencode.base.image.kernel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * SeparableKernelOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("SeparableKernelOp 可分离卷积测试")
class SeparableKernelOpTest {

    @Nested
    @DisplayName("可分离卷积与等效 2D 卷积一致性测试")
    class ConsistencyTests {

        @Test
        @DisplayName("3x3 方框核可分离结果与 2D 卷积一致")
        void separableBoxMatchesFull2D() {
            // Create a test image with known pattern
            int w = 16, h = 16;
            int[] gray = new int[w * h];
            for (int i = 0; i < gray.length; i++) {
                gray[i] = (i * 17 + 31) % 256;
            }

            // 3x3 box blur as 2D kernel
            float v = 1.0f / 9;
            float[] box2d = {v, v, v, v, v, v, v, v, v};
            int[] result2d = KernelOp.convolveGray(gray, w, h, box2d, 3, 3, KernelOp.BorderMode.CLAMP);

            // Separable: 1D horizontal [1/3, 1/3, 1/3] then vertical [1/3, 1/3, 1/3]
            float s = 1.0f / 3;
            float[] hKernel = {s, s, s};
            float[] vKernel = {s, s, s};
            int[] resultSep = SeparableKernelOp.convolveGray(gray, w, h, hKernel, vKernel, KernelOp.BorderMode.CLAMP);

            // Allow +-1 tolerance due to rounding differences between intermediate float vs int
            for (int i = 0; i < gray.length; i++) {
                assertThat(resultSep[i]).isCloseTo(result2d[i], within(1));
            }
        }

        @Test
        @DisplayName("ARGB 图像可分离方框核与 2D 卷积一致")
        void separableArgbMatchesFull2D() {
            BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(img);
            for (int i = 0; i < pixels.length; i++) {
                int v = (i * 23 + 7) % 256;
                pixels[i] = PixelOp.argb(255, v, (v + 50) % 256, (v + 100) % 256);
            }

            float bv = 1.0f / 9;
            float[] box2d = {bv, bv, bv, bv, bv, bv, bv, bv, bv};
            BufferedImage result2d = KernelOp.convolve(img, box2d, 3, 3, KernelOp.BorderMode.CLAMP);

            float s = 1.0f / 3;
            BufferedImage resultSep = SeparableKernelOp.convolve(img, new float[]{s, s, s}, new float[]{s, s, s}, KernelOp.BorderMode.CLAMP);

            int[] px2d = PixelOp.getPixels(result2d);
            int[] pxSep = PixelOp.getPixels(resultSep);

            for (int i = 0; i < px2d.length; i++) {
                assertThat(PixelOp.red(pxSep[i])).isCloseTo(PixelOp.red(px2d[i]), within(1));
                assertThat(PixelOp.green(pxSep[i])).isCloseTo(PixelOp.green(px2d[i]), within(1));
                assertThat(PixelOp.blue(pxSep[i])).isCloseTo(PixelOp.blue(px2d[i]), within(1));
            }
        }
    }

    @Nested
    @DisplayName("大图并行处理测试")
    class LargeImageTests {

        @Test
        @DisplayName("2000x2000 图像可分离卷积正确处理")
        void largeImageProcessedCorrectly() {
            int w = 2000, h = 2000;
            int[] gray = new int[w * h];
            // Uniform value = 100
            java.util.Arrays.fill(gray, 100);

            float s = 1.0f / 3;
            float[] hKernel = {s, s, s};
            float[] vKernel = {s, s, s};
            int[] result = SeparableKernelOp.convolveGray(gray, w, h, hKernel, vKernel, KernelOp.BorderMode.CLAMP);

            // Uniform image should remain uniform after box blur
            // Check several positions (avoid full array comparison for speed)
            assertThat(result[0]).isEqualTo(100);
            assertThat(result[w * h / 2]).isEqualTo(100);
            assertThat(result[w * h - 1]).isEqualTo(100);
            // Check center
            assertThat(result[1000 * w + 1000]).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("参数校验测试")
    class ValidationTests {

        @Test
        @DisplayName("偶数长度核抛出异常")
        void evenKernelLengthThrows() {
            BufferedImage img = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);
            assertThatThrownBy(() ->
                    SeparableKernelOp.convolve(img, new float[]{1, 1}, new float[]{1, 1, 1}, KernelOp.BorderMode.ZERO))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("空核抛出异常")
        void emptyKernelThrows() {
            BufferedImage img = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);
            assertThatThrownBy(() ->
                    SeparableKernelOp.convolve(img, new float[0], new float[]{1}, KernelOp.BorderMode.ZERO))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("null 参数抛出异常")
        void nullParamsThrow() {
            BufferedImage img = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);
            float[] k = {1};
            assertThatThrownBy(() -> SeparableKernelOp.convolve(null, k, k, KernelOp.BorderMode.ZERO))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> SeparableKernelOp.convolve(img, null, k, KernelOp.BorderMode.ZERO))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> SeparableKernelOp.convolve(img, k, null, KernelOp.BorderMode.ZERO))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> SeparableKernelOp.convolve(img, k, k, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
