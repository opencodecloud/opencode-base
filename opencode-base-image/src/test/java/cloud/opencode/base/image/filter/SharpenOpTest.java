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
 * SharpenOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("SharpenOp 非锐化掩模锐化滤波器测试")
class SharpenOpTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        // Create a 32x32 test image with varying pixel values
        testImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = PixelOp.getPixels(PixelOp.ensureArgb(testImage));
        for (int i = 0; i < pixels.length; i++) {
            int v = (i * 37) % 256;
            pixels[i] = PixelOp.argb(255, v, v / 2, v / 3);
        }
    }

    @Nested
    @DisplayName("正常锐化测试")
    class NormalSharpenTests {

        @Test
        @DisplayName("默认锐化后输出与输入不同")
        void defaultSharpenProducesDifferentOutput() {
            BufferedImage result = SharpenOp.apply(testImage);

            int[] srcPixels = PixelOp.getPixels(PixelOp.ensureArgb(testImage));
            int[] dstPixels = PixelOp.getPixels(result);

            boolean anyDifferent = false;
            for (int i = 0; i < srcPixels.length; i++) {
                if (srcPixels[i] != dstPixels[i]) {
                    anyDifferent = true;
                    break;
                }
            }
            assertThat(anyDifferent).as("Sharpened output should differ from input").isTrue();
        }

        @Test
        @DisplayName("锐化增强边缘 — 差异大于模糊")
        void sharpenEnhancesEdges() {
            BufferedImage sharpened = SharpenOp.apply(testImage, 1.0, 1.0);
            BufferedImage blurred = GaussianBlurOp.apply(testImage, 1.0);

            int[] srcPixels = PixelOp.getPixels(PixelOp.ensureArgb(testImage));
            int[] sharpPixels = PixelOp.getPixels(sharpened);
            int[] blurPixels = PixelOp.getPixels(blurred);

            // Sharpening should increase the diff from blurred compared to original
            long sharpDiffSum = 0;
            long blurDiffSum = 0;
            for (int i = 0; i < srcPixels.length; i++) {
                sharpDiffSum += Math.abs(PixelOp.red(sharpPixels[i]) - PixelOp.red(blurPixels[i]));
                blurDiffSum += Math.abs(PixelOp.red(srcPixels[i]) - PixelOp.red(blurPixels[i]));
            }
            assertThat(sharpDiffSum).as("Sharpened should differ more from blurred than original does")
                    .isGreaterThanOrEqualTo(blurDiffSum);
        }

        @Test
        @DisplayName("amount=0 输出与输入相似")
        void amountZeroProducesSimilarOutput() {
            BufferedImage result = SharpenOp.apply(testImage, 0.0, 1.0);

            int[] srcPixels = PixelOp.getPixels(PixelOp.ensureArgb(testImage));
            int[] dstPixels = PixelOp.getPixels(result);

            // With amount=0, result = src + 0*(src-blur) = src
            for (int i = 0; i < srcPixels.length; i++) {
                assertThat(PixelOp.red(dstPixels[i]))
                        .isCloseTo(PixelOp.red(srcPixels[i]), within(1));
                assertThat(PixelOp.green(dstPixels[i]))
                        .isCloseTo(PixelOp.green(srcPixels[i]), within(1));
                assertThat(PixelOp.blue(dstPixels[i]))
                        .isCloseTo(PixelOp.blue(srcPixels[i]), within(1));
            }
        }

        @Test
        @DisplayName("输出尺寸与输入相同")
        void outputSameDimensionsAsInput() {
            BufferedImage result = SharpenOp.apply(testImage, 1.5, 2.0);

            assertThat(result.getWidth()).isEqualTo(testImage.getWidth());
            assertThat(result.getHeight()).isEqualTo(testImage.getHeight());
            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
        }

        @Test
        @DisplayName("Alpha 通道保持不变")
        void alphaChannelPreserved() {
            BufferedImage result = SharpenOp.apply(testImage, 1.0);

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
            assertThatThrownBy(() -> SharpenOp.apply(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("amount 为负数抛出异常")
        void negativeAmountThrowsException() {
            assertThatThrownBy(() -> SharpenOp.apply(testImage, -1.0))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("amount 为 NaN 抛出异常")
        void nanAmountThrowsException() {
            assertThatThrownBy(() -> SharpenOp.apply(testImage, Double.NaN))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("amount 为无穷抛出异常")
        void infiniteAmountThrowsException() {
            assertThatThrownBy(() -> SharpenOp.apply(testImage, Double.POSITIVE_INFINITY))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("sigma 为负数抛出异常")
        void negativeSigmaThrowsException() {
            assertThatThrownBy(() -> SharpenOp.apply(testImage, 1.0, -1.0))
                    .isInstanceOf(ImageOperationException.class);
        }
    }
}
