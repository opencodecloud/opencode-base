package cloud.opencode.base.image.filter;

import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.PixelOp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * BilateralFilterOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("BilateralFilterOp 双边滤波器测试")
class BilateralFilterOpTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        // Create a 32x32 test image with a sharp edge: left half dark, right half bright
        testImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = PixelOp.getPixels(PixelOp.ensureArgb(testImage));
        int width = testImage.getWidth();
        for (int y = 0; y < testImage.getHeight(); y++) {
            for (int x = 0; x < width; x++) {
                int v = (x < width / 2) ? 40 : 200;
                pixels[y * width + x] = PixelOp.argb(255, v, v, v);
            }
        }
    }

    @Nested
    @DisplayName("正常滤波测试")
    class NormalFilterTests {

        @Test
        @DisplayName("平坦区域被平滑")
        void smoothsFlatAreas() {
            BufferedImage result = BilateralFilterOp.apply(testImage, 5, 50.0, 50.0);

            int[] srcPixels = PixelOp.getPixels(PixelOp.ensureArgb(testImage));
            int[] dstPixels = PixelOp.getPixels(result);
            int width = testImage.getWidth();

            // Sample a pixel deep in the left flat area (away from edge)
            // Flat area pixels should remain close to original since all neighbors are similar
            int flatIdx = 5 * width + 3; // row 5, col 3 — deep in left half
            int srcR = PixelOp.red(srcPixels[flatIdx]);
            int dstR = PixelOp.red(dstPixels[flatIdx]);
            assertThat(dstR).isCloseTo(srcR, within(5));
        }

        @Test
        @DisplayName("边缘被保留 — 边缘像素变化小于高斯模糊")
        void preservesEdges() {
            BufferedImage bilateral = BilateralFilterOp.apply(testImage, 5, 30.0, 30.0);
            BufferedImage gaussian = GaussianBlurOp.apply(testImage, 2.0);

            int[] srcPixels = PixelOp.getPixels(PixelOp.ensureArgb(testImage));
            int[] bilateralPixels = PixelOp.getPixels(bilateral);
            int[] gaussianPixels = PixelOp.getPixels(gaussian);
            int width = testImage.getWidth();

            // Compare edge pixels (column 15 and 16 — at the boundary)
            long bilateralEdgeDiff = 0;
            long gaussianEdgeDiff = 0;
            for (int y = 4; y < 28; y++) {
                for (int col = width / 2 - 1; col <= width / 2; col++) {
                    int idx = y * width + col;
                    bilateralEdgeDiff += Math.abs(PixelOp.red(bilateralPixels[idx]) - PixelOp.red(srcPixels[idx]));
                    gaussianEdgeDiff += Math.abs(PixelOp.red(gaussianPixels[idx]) - PixelOp.red(srcPixels[idx]));
                }
            }

            assertThat(bilateralEdgeDiff)
                    .as("Bilateral filter should preserve edges better than Gaussian blur")
                    .isLessThan(gaussianEdgeDiff);
        }

        @Test
        @DisplayName("默认参数正常工作")
        void defaultParamsWork() {
            BufferedImage result = BilateralFilterOp.apply(testImage);

            assertThat(result.getWidth()).isEqualTo(testImage.getWidth());
            assertThat(result.getHeight()).isEqualTo(testImage.getHeight());
            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
        }

        @Test
        @DisplayName("输出尺寸与输入相同")
        void outputSameDimensionsAsInput() {
            BufferedImage result = BilateralFilterOp.apply(testImage, 3, 50.0, 50.0);

            assertThat(result.getWidth()).isEqualTo(testImage.getWidth());
            assertThat(result.getHeight()).isEqualTo(testImage.getHeight());
        }

        @Test
        @DisplayName("Alpha 通道保持不变")
        void alphaChannelPreserved() {
            BufferedImage result = BilateralFilterOp.apply(testImage, 5, 50.0, 50.0);

            int[] srcPixels = PixelOp.getPixels(PixelOp.ensureArgb(testImage));
            int[] dstPixels = PixelOp.getPixels(result);

            for (int i = 0; i < srcPixels.length; i++) {
                assertThat(PixelOp.alpha(dstPixels[i])).isEqualTo(PixelOp.alpha(srcPixels[i]));
            }
        }
    }

    @Nested
    @DisplayName("异常参数测试")
    class InvalidParameterTests {

        @Test
        @DisplayName("null 图像抛出异常")
        void nullImageThrowsException() {
            assertThatThrownBy(() -> BilateralFilterOp.apply(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("偶数核大小抛出异常")
        void evenKernelSizeThrowsException() {
            assertThatThrownBy(() -> BilateralFilterOp.apply(testImage, 4, 50.0, 50.0))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("核大小小于 3 抛出异常")
        void kernelSizeTooSmallThrowsException() {
            assertThatThrownBy(() -> BilateralFilterOp.apply(testImage, 1, 50.0, 50.0))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("sigmaColor 为负数抛出异常")
        void negativeSigmaColorThrowsException() {
            assertThatThrownBy(() -> BilateralFilterOp.apply(testImage, 5, -1.0, 50.0))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("sigmaSpace 为零抛出异常")
        void zeroSigmaSpaceThrowsException() {
            assertThatThrownBy(() -> BilateralFilterOp.apply(testImage, 5, 50.0, 0.0))
                    .isInstanceOf(ImageOperationException.class);
        }
    }
}
