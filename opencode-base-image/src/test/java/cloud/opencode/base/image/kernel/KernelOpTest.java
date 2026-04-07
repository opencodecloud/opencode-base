package cloud.opencode.base.image.kernel;

import cloud.opencode.base.image.exception.ImageOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * KernelOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("KernelOp 卷积引擎测试")
class KernelOpTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = PixelOp.getPixels(testImage);
        for (int i = 0; i < pixels.length; i++) {
            int v = (i * 37) % 256; // deterministic pattern
            pixels[i] = PixelOp.argb(255, v, v, v);
        }
    }

    @Nested
    @DisplayName("identity 核测试")
    class IdentityKernelTests {

        @Test
        @DisplayName("3x3 identity 核输出等于输入")
        void identityKernelPreservesImage() {
            float[] identity = {0, 0, 0, 0, 1, 0, 0, 0, 0};
            BufferedImage result = KernelOp.convolve(testImage, identity, 3, 3, KernelOp.BorderMode.ZERO);

            int[] srcPixels = PixelOp.getPixels(testImage);
            int[] dstPixels = PixelOp.getPixels(result);

            for (int i = 0; i < srcPixels.length; i++) {
                assertThat(PixelOp.red(dstPixels[i])).isEqualTo(PixelOp.red(srcPixels[i]));
                assertThat(PixelOp.green(dstPixels[i])).isEqualTo(PixelOp.green(srcPixels[i]));
                assertThat(PixelOp.blue(dstPixels[i])).isEqualTo(PixelOp.blue(srcPixels[i]));
                assertThat(PixelOp.alpha(dstPixels[i])).isEqualTo(PixelOp.alpha(srcPixels[i]));
            }
        }

        @Test
        @DisplayName("identity 核对灰度数组也保持不变")
        void identityKernelGray() {
            int[] gray = {10, 20, 30, 40, 50, 60, 70, 80, 90};
            float[] identity = {0, 0, 0, 0, 1, 0, 0, 0, 0};
            int[] result = KernelOp.convolveGray(gray, 3, 3, identity, 3, 3, KernelOp.BorderMode.CLAMP);
            assertThat(result).isEqualTo(gray);
        }
    }

    @Nested
    @DisplayName("BorderMode 边界处理测试")
    class BorderModeTests {

        @Test
        @DisplayName("ZERO 模式边界补零")
        void zeroBorderMode() {
            // 3x3 image, all pixels = 100
            int[] gray = {100, 100, 100, 100, 100, 100, 100, 100, 100};
            // Shift-left kernel: only reads pixel to the right
            float[] kernel = {0, 0, 0, 0, 0, 1, 0, 0, 0};
            int[] result = KernelOp.convolveGray(gray, 3, 3, kernel, 3, 3, KernelOp.BorderMode.ZERO);
            // Rightmost column should get 0 (from zero-padded border)
            assertThat(result[2]).isEqualTo(0);  // (2,0)
            assertThat(result[5]).isEqualTo(0);  // (2,1)
            assertThat(result[8]).isEqualTo(0);  // (2,2)
            // Others should be 100
            assertThat(result[0]).isEqualTo(100);
            assertThat(result[1]).isEqualTo(100);
        }

        @Test
        @DisplayName("CLAMP 模式边界截断到边缘")
        void clampBorderMode() {
            int[] gray = {100, 100, 100, 100, 100, 100, 100, 100, 100};
            // Shift-left kernel
            float[] kernel = {0, 0, 0, 0, 0, 1, 0, 0, 0};
            int[] result = KernelOp.convolveGray(gray, 3, 3, kernel, 3, 3, KernelOp.BorderMode.CLAMP);
            // All should be 100 because clamped edge = 100
            for (int v : result) {
                assertThat(v).isEqualTo(100);
            }
        }

        @Test
        @DisplayName("MIRROR 模式边界镜像")
        void mirrorBorderMode() {
            // 3x1 image: [10, 20, 30]
            int[] gray = {10, 20, 30};
            // Read pixel to the right
            float[] kernel = {0, 0, 1};
            int[] result = KernelOp.convolveGray(gray, 3, 1, kernel, 3, 1, KernelOp.BorderMode.MIRROR);
            // result[0] = gray[1] = 20 (right of 0 is 1)
            assertThat(result[0]).isEqualTo(20);
            // result[1] = gray[2] = 30 (right of 1 is 2)
            assertThat(result[1]).isEqualTo(30);
            // result[2] = gray[1] = 20 (right of 2 is mirrored to 1)
            assertThat(result[2]).isEqualTo(20);
        }

        @Test
        @DisplayName("WRAP 模式边界环绕")
        void wrapBorderMode() {
            // 3x1 image: [10, 20, 30]
            int[] gray = {10, 20, 30};
            // Read pixel to the right
            float[] kernel = {0, 0, 1};
            int[] result = KernelOp.convolveGray(gray, 3, 1, kernel, 3, 1, KernelOp.BorderMode.WRAP);
            // result[2] = gray[0] = 10 (right of 2 wraps to 0)
            assertThat(result[2]).isEqualTo(10);
            assertThat(result[0]).isEqualTo(20);
            assertThat(result[1]).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("参数校验测试")
    class ValidationTests {

        @Test
        @DisplayName("null 图像抛出异常")
        void nullImageThrows() {
            float[] k = {0, 0, 0, 0, 1, 0, 0, 0, 0};
            assertThatThrownBy(() -> KernelOp.convolve(null, k, 3, 3, KernelOp.BorderMode.ZERO))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null 卷积核抛出异常")
        void nullKernelThrows() {
            assertThatThrownBy(() -> KernelOp.convolve(testImage, null, 3, 3, KernelOp.BorderMode.ZERO))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null 边界模式抛出异常")
        void nullBorderThrows() {
            float[] k = {0, 0, 0, 0, 1, 0, 0, 0, 0};
            assertThatThrownBy(() -> KernelOp.convolve(testImage, k, 3, 3, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("偶数核尺寸抛出异常")
        void evenKernelDimensionThrows() {
            float[] k = new float[4];
            assertThatThrownBy(() -> KernelOp.convolve(testImage, k, 2, 2, KernelOp.BorderMode.ZERO))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("核数组长度不匹配抛出异常")
        void kernelLengthMismatchThrows() {
            float[] k = new float[5]; // should be 9 for 3x3
            assertThatThrownBy(() -> KernelOp.convolve(testImage, k, 3, 3, KernelOp.BorderMode.ZERO))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("灰度卷积像素数组长度不匹配抛出异常")
        void grayPixelLengthMismatchThrows() {
            float[] k = {0, 0, 0, 0, 1, 0, 0, 0, 0};
            assertThatThrownBy(() -> KernelOp.convolveGray(new int[5], 3, 3, k, 3, 3, KernelOp.BorderMode.ZERO))
                    .isInstanceOf(ImageOperationException.class);
        }
    }

    @Nested
    @DisplayName("卷积正确性测试")
    class CorrectnessTests {

        @Test
        @DisplayName("3x3 方框模糊核计算正确")
        void boxBlurCorrect() {
            // 3x3 image, center = 90, rest = 0
            int[] gray = {0, 0, 0, 0, 90, 0, 0, 0, 0};
            float v = 1.0f / 9;
            float[] box = {v, v, v, v, v, v, v, v, v};
            int[] result = KernelOp.convolveGray(gray, 3, 3, box, 3, 3, KernelOp.BorderMode.ZERO);
            // All interior pixels see the center 90, so each = round(90/9) = 10
            assertThat(result[4]).isEqualTo(10); // center
            assertThat(result[0]).isEqualTo(10); // corner
        }
    }
}
