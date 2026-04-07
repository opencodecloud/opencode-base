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
 * BoxBlurOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("BoxBlurOp 均值模糊滤波器测试")
class BoxBlurOpTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = PixelOp.getPixels(testImage);
        for (int i = 0; i < pixels.length; i++) {
            int v = (i * 37) % 256;
            pixels[i] = PixelOp.argb(255, v, v / 2, v / 3);
        }
    }

    @Nested
    @DisplayName("正常模糊测试")
    class NormalBlurTests {

        @Test
        @DisplayName("ksize=3 输出已平滑处理")
        void boxBlur3ProducesSmoothedOutput() {
            BufferedImage result = BoxBlurOp.apply(testImage, 3);

            int[] srcPixels = PixelOp.getPixels(testImage);
            int[] dstPixels = PixelOp.getPixels(result);

            // The output should differ from input (smoothing changes pixel values)
            boolean anyDifferent = false;
            for (int i = 0; i < srcPixels.length; i++) {
                if (srcPixels[i] != dstPixels[i]) {
                    anyDifferent = true;
                    break;
                }
            }
            assertThat(anyDifferent).as("Box blur output should differ from input").isTrue();
        }

        @Test
        @DisplayName("大核 ksize=31 正常工作")
        void largeKernelSize31Works() {
            BufferedImage result = BoxBlurOp.apply(testImage, 31);

            assertThat(result.getWidth()).isEqualTo(testImage.getWidth());
            assertThat(result.getHeight()).isEqualTo(testImage.getHeight());
            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
        }

        @Test
        @DisplayName("输出尺寸与输入相同")
        void outputSameDimensionsAsInput() {
            BufferedImage result = BoxBlurOp.apply(testImage, 5);

            assertThat(result.getWidth()).isEqualTo(testImage.getWidth());
            assertThat(result.getHeight()).isEqualTo(testImage.getHeight());
            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
        }

        @Test
        @DisplayName("Alpha 通道保持不变")
        void alphaChannelPreserved() {
            BufferedImage result = BoxBlurOp.apply(testImage, 3);

            int[] srcPixels = PixelOp.getPixels(testImage);
            int[] dstPixels = PixelOp.getPixels(result);

            for (int i = 0; i < srcPixels.length; i++) {
                assertThat(PixelOp.alpha(dstPixels[i])).isEqualTo(PixelOp.alpha(srcPixels[i]));
            }
        }

        @Test
        @DisplayName("ksize=1 输出等于输入（恒等操作）")
        void kernelSizeOneIsIdentity() {
            BufferedImage result = BoxBlurOp.apply(testImage, 1);

            int[] srcPixels = PixelOp.getPixels(testImage);
            int[] dstPixels = PixelOp.getPixels(result);

            for (int i = 0; i < srcPixels.length; i++) {
                assertThat(PixelOp.red(dstPixels[i])).isEqualTo(PixelOp.red(srcPixels[i]));
                assertThat(PixelOp.green(dstPixels[i])).isEqualTo(PixelOp.green(srcPixels[i]));
                assertThat(PixelOp.blue(dstPixels[i])).isEqualTo(PixelOp.blue(srcPixels[i]));
            }
        }

        @Test
        @DisplayName("均匀图像经均值模糊后不变")
        void uniformImageUnchanged() {
            BufferedImage uniform = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(uniform);
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = PixelOp.argb(255, 100, 100, 100);
            }

            BufferedImage result = BoxBlurOp.apply(uniform, 5);
            int[] dstPixels = PixelOp.getPixels(result);

            for (int i = 0; i < dstPixels.length; i++) {
                assertThat(PixelOp.red(dstPixels[i])).isEqualTo(100);
                assertThat(PixelOp.green(dstPixels[i])).isEqualTo(100);
                assertThat(PixelOp.blue(dstPixels[i])).isEqualTo(100);
            }
        }
    }

    @Nested
    @DisplayName("异常参数测试")
    class InvalidParameterTests {

        @Test
        @DisplayName("ksize=0 抛出异常")
        void kernelSizeZeroThrowsException() {
            assertThatThrownBy(() -> BoxBlurOp.apply(testImage, 0))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("偶数核大小抛出异常")
        void evenKernelSizeThrowsException() {
            assertThatThrownBy(() -> BoxBlurOp.apply(testImage, 4))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("负数核大小抛出异常")
        void negativeKernelSizeThrowsException() {
            assertThatThrownBy(() -> BoxBlurOp.apply(testImage, -1))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("null 图像抛出异常")
        void nullImageThrowsException() {
            assertThatThrownBy(() -> BoxBlurOp.apply(null, 3))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
