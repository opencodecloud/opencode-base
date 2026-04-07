package cloud.opencode.base.image.filter;

import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.PixelOp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.Random;

import static org.assertj.core.api.Assertions.*;

/**
 * MedianBlurOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("MedianBlurOp 中值模糊滤波器测试")
class MedianBlurOpTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = PixelOp.getPixels(testImage);
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = PixelOp.argb(255, 128, 128, 128);
        }
    }

    @Nested
    @DisplayName("椒盐噪声去除测试")
    class SaltAndPepperNoiseTests {

        @Test
        @DisplayName("ksize=3 对椒盐噪声有降噪效果")
        void medianFilterReducesSaltAndPepperNoise() {
            // Create image with salt-and-pepper noise
            int w = 32, h = 32;
            BufferedImage noisy = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(noisy);
            Random rng = new Random(42);

            int noiseCount = 0;
            for (int i = 0; i < pixels.length; i++) {
                int v = 128; // base gray
                if (rng.nextDouble() < 0.1) {
                    v = rng.nextBoolean() ? 0 : 255; // salt or pepper
                    noiseCount++;
                }
                pixels[i] = PixelOp.argb(255, v, v, v);
            }
            assertThat(noiseCount).isGreaterThan(0);

            BufferedImage filtered = MedianBlurOp.apply(noisy, 3);

            // Count pixels that are extreme (salt/pepper) in the filtered output
            int[] filteredPixels = PixelOp.getPixels(filtered);
            int extremeAfter = 0;
            for (int px : filteredPixels) {
                int r = PixelOp.red(px);
                if (r == 0 || r == 255) {
                    extremeAfter++;
                }
            }

            // Count extreme pixels in noisy input
            int extremeBefore = 0;
            for (int px : pixels) {
                int r = PixelOp.red(px);
                if (r == 0 || r == 255) {
                    extremeBefore++;
                }
            }

            assertThat(extremeAfter).as("Median filter should reduce extreme pixel count")
                    .isLessThan(extremeBefore);
        }
    }

    @Nested
    @DisplayName("正常滤波测试")
    class NormalFilterTests {

        @Test
        @DisplayName("输出尺寸与输入相同")
        void outputSameDimensionsAsInput() {
            BufferedImage result = MedianBlurOp.apply(testImage, 3);

            assertThat(result.getWidth()).isEqualTo(testImage.getWidth());
            assertThat(result.getHeight()).isEqualTo(testImage.getHeight());
            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
        }

        @Test
        @DisplayName("ksize=5 滤波正常工作")
        void kernelSize5Works() {
            BufferedImage result = MedianBlurOp.apply(testImage, 5);

            assertThat(result.getWidth()).isEqualTo(testImage.getWidth());
            assertThat(result.getHeight()).isEqualTo(testImage.getHeight());
        }

        @Test
        @DisplayName("Alpha 通道保持不变")
        void alphaChannelPreserved() {
            BufferedImage result = MedianBlurOp.apply(testImage, 3);

            int[] srcPixels = PixelOp.getPixels(testImage);
            int[] dstPixels = PixelOp.getPixels(result);

            for (int i = 0; i < srcPixels.length; i++) {
                assertThat(PixelOp.alpha(dstPixels[i])).isEqualTo(PixelOp.alpha(srcPixels[i]));
            }
        }

        @Test
        @DisplayName("均匀图像经中值滤波后不变")
        void uniformImageUnchanged() {
            // Uniform gray 128 image - median of 128s should be 128
            BufferedImage result = MedianBlurOp.apply(testImage, 3);

            int[] srcPixels = PixelOp.getPixels(testImage);
            int[] dstPixels = PixelOp.getPixels(result);

            for (int i = 0; i < srcPixels.length; i++) {
                assertThat(PixelOp.red(dstPixels[i])).isEqualTo(PixelOp.red(srcPixels[i]));
                assertThat(PixelOp.green(dstPixels[i])).isEqualTo(PixelOp.green(srcPixels[i]));
                assertThat(PixelOp.blue(dstPixels[i])).isEqualTo(PixelOp.blue(srcPixels[i]));
            }
        }
    }

    @Nested
    @DisplayName("异常参数测试")
    class InvalidParameterTests {

        @Test
        @DisplayName("ksize=1 抛出异常")
        void kernelSizeOneThrowsException() {
            assertThatThrownBy(() -> MedianBlurOp.apply(testImage, 1))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("偶数核大小抛出异常")
        void evenKernelSizeThrowsException() {
            assertThatThrownBy(() -> MedianBlurOp.apply(testImage, 4))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("ksize=0 抛出异常")
        void kernelSizeZeroThrowsException() {
            assertThatThrownBy(() -> MedianBlurOp.apply(testImage, 0))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("负数核大小抛出异常")
        void negativeKernelSizeThrowsException() {
            assertThatThrownBy(() -> MedianBlurOp.apply(testImage, -3))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("null 图像抛出异常")
        void nullImageThrowsException() {
            assertThatThrownBy(() -> MedianBlurOp.apply(null, 3))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
