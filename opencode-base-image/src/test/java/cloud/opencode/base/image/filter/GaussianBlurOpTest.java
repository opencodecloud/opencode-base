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
 * GaussianBlurOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("GaussianBlurOp 高斯模糊滤波器测试")
class GaussianBlurOpTest {

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
        @DisplayName("sigma=1.0 模糊后输出与输入不同")
        void blurWithSigma1ProducesDifferentOutput() {
            BufferedImage result = GaussianBlurOp.apply(testImage, 1.0);

            int[] srcPixels = PixelOp.getPixels(testImage);
            int[] dstPixels = PixelOp.getPixels(result);

            boolean anyDifferent = false;
            for (int i = 0; i < srcPixels.length; i++) {
                if (srcPixels[i] != dstPixels[i]) {
                    anyDifferent = true;
                    break;
                }
            }
            assertThat(anyDifferent).as("Blurred output should differ from input").isTrue();
        }

        @Test
        @DisplayName("显式核大小模糊")
        void blurWithExplicitKernelSize() {
            BufferedImage result = GaussianBlurOp.apply(testImage, 1.5, 7);

            assertThat(result.getWidth()).isEqualTo(testImage.getWidth());
            assertThat(result.getHeight()).isEqualTo(testImage.getHeight());
        }

        @Test
        @DisplayName("输出尺寸与输入相同")
        void outputSameDimensionsAsInput() {
            BufferedImage result = GaussianBlurOp.apply(testImage, 2.0);

            assertThat(result.getWidth()).isEqualTo(testImage.getWidth());
            assertThat(result.getHeight()).isEqualTo(testImage.getHeight());
            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
        }

        @Test
        @DisplayName("Alpha 通道保持不变")
        void alphaChannelPreserved() {
            BufferedImage result = GaussianBlurOp.apply(testImage, 1.0);

            int[] srcPixels = PixelOp.getPixels(testImage);
            int[] dstPixels = PixelOp.getPixels(result);

            for (int i = 0; i < srcPixels.length; i++) {
                assertThat(PixelOp.alpha(dstPixels[i])).isEqualTo(PixelOp.alpha(srcPixels[i]));
            }
        }
    }

    @Nested
    @DisplayName("自动核大小计算测试")
    class AutoKernelSizeTests {

        @Test
        @DisplayName("sigma=1.0 自动核大小为 7")
        void sigmaOneKernelSizeSeven() {
            // ceil(3 * 1.0) * 2 + 1 = 3 * 2 + 1 = 7
            int size = GaussianBlurOp.computeKernelSize(1.0);
            assertThat(size).isEqualTo(7);
        }

        @Test
        @DisplayName("sigma=0.5 自动核大小为 5")
        void sigmaHalfKernelSizeFive() {
            // ceil(3 * 0.5) * 2 + 1 = ceil(1.5) * 2 + 1 = 2 * 2 + 1 = 5
            int size = GaussianBlurOp.computeKernelSize(0.5);
            assertThat(size).isEqualTo(5);
        }

        @Test
        @DisplayName("sigma=2.0 自动核大小为 13")
        void sigmaTwoKernelSizeThirteen() {
            // ceil(3 * 2.0) * 2 + 1 = 6 * 2 + 1 = 13
            int size = GaussianBlurOp.computeKernelSize(2.0);
            assertThat(size).isEqualTo(13);
        }
    }

    @Nested
    @DisplayName("核生成测试")
    class KernelGenerationTests {

        @Test
        @DisplayName("核归一化总和为 1.0")
        void kernelSumsToOne() {
            float[] kernel = GaussianBlurOp.generateKernel(1.0, 7);
            double sum = 0;
            for (float v : kernel) {
                sum += v;
            }
            assertThat(sum).isCloseTo(1.0, within(1e-5));
        }

        @Test
        @DisplayName("核中心值最大")
        void kernelCenterIsMax() {
            float[] kernel = GaussianBlurOp.generateKernel(1.0, 7);
            int center = kernel.length / 2;
            for (int i = 0; i < kernel.length; i++) {
                assertThat(kernel[center]).isGreaterThanOrEqualTo(kernel[i]);
            }
        }

        @Test
        @DisplayName("核关于中心对称")
        void kernelIsSymmetric() {
            float[] kernel = GaussianBlurOp.generateKernel(1.5, 9);
            int n = kernel.length;
            for (int i = 0; i < n / 2; i++) {
                assertThat(kernel[i]).isCloseTo(kernel[n - 1 - i], within(1e-6f));
            }
        }
    }

    @Nested
    @DisplayName("异常参数测试")
    class InvalidParameterTests {

        @Test
        @DisplayName("sigma=0 抛出异常")
        void sigmaZeroThrowsException() {
            assertThatThrownBy(() -> GaussianBlurOp.apply(testImage, 0))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("sigma 为负数抛出异常")
        void negativeSigmaThrowsException() {
            assertThatThrownBy(() -> GaussianBlurOp.apply(testImage, -1.0))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("偶数核大小抛出异常")
        void evenKernelSizeThrowsException() {
            assertThatThrownBy(() -> GaussianBlurOp.apply(testImage, 1.0, 4))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("null 图像抛出异常")
        void nullImageThrowsException() {
            assertThatThrownBy(() -> GaussianBlurOp.apply(null, 1.0))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("NaN sigma 抛出异常")
        void nanSigmaThrowsException() {
            assertThatThrownBy(() -> GaussianBlurOp.apply(testImage, Double.NaN))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("Infinite sigma 抛出异常")
        void infiniteSigmaThrowsException() {
            assertThatThrownBy(() -> GaussianBlurOp.apply(testImage, Double.POSITIVE_INFINITY))
                    .isInstanceOf(ImageOperationException.class);
        }
    }
}
