package cloud.opencode.base.image.threshold;

import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.PixelOp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * ThresholdOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("ThresholdOp 固定阈值操作测试")
class ThresholdOpTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        // Create a 4x4 image with gradient grayscale values: 0, 20, 40, ..., 300 (clamped to 255)
        testImage = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = PixelOp.getPixels(testImage);
        for (int i = 0; i < pixels.length; i++) {
            int v = Math.min(i * 20, 255);
            pixels[i] = PixelOp.argb(255, v, v, v);
        }
    }

    @Nested
    @DisplayName("BINARY 模式测试")
    class BinaryModeTests {

        @Test
        @DisplayName("低于阈值的像素置 0，高于等于阈值的像素置 255")
        void binaryThreshold() {
            BufferedImage result = ThresholdOp.apply(testImage, 128, ThresholdOp.Mode.BINARY);
            int[] pixels = PixelOp.getPixels(result);

            for (int px : pixels) {
                int r = PixelOp.red(px);
                assertThat(r).isIn(0, 255);
            }

            // First pixel (gray=0) should be 0, last pixels (gray>=128) should be 255
            assertThat(PixelOp.red(pixels[0])).isEqualTo(0);
            // Pixel at index 7 has gray=140, should be 255
            assertThat(PixelOp.red(pixels[7])).isEqualTo(255);
        }

        @Test
        @DisplayName("便捷方法默认使用 BINARY 模式")
        void convenienceMethodUsesBinary() {
            BufferedImage result1 = ThresholdOp.apply(testImage, 128);
            BufferedImage result2 = ThresholdOp.apply(testImage, 128, ThresholdOp.Mode.BINARY);

            int[] p1 = PixelOp.getPixels(result1);
            int[] p2 = PixelOp.getPixels(result2);
            assertThat(p1).isEqualTo(p2);
        }
    }

    @Nested
    @DisplayName("BINARY_INV 模式测试")
    class BinaryInvModeTests {

        @Test
        @DisplayName("BINARY_INV 与 BINARY 结果相反")
        void binaryInvIsInverseOfBinary() {
            BufferedImage binary = ThresholdOp.apply(testImage, 128, ThresholdOp.Mode.BINARY);
            BufferedImage binaryInv = ThresholdOp.apply(testImage, 128, ThresholdOp.Mode.BINARY_INV);

            int[] binPixels = PixelOp.getPixels(binary);
            int[] invPixels = PixelOp.getPixels(binaryInv);

            for (int i = 0; i < binPixels.length; i++) {
                int binR = PixelOp.red(binPixels[i]);
                int invR = PixelOp.red(invPixels[i]);
                assertThat(binR + invR).isEqualTo(255);
            }
        }
    }

    @Nested
    @DisplayName("TRUNC 模式测试")
    class TruncModeTests {

        @Test
        @DisplayName("低于阈值保持原值，高于等于阈值置为阈值")
        void truncMode() {
            int threshold = 100;
            BufferedImage result = ThresholdOp.apply(testImage, threshold, ThresholdOp.Mode.TRUNC);
            int[] pixels = PixelOp.getPixels(result);

            for (int px : pixels) {
                int r = PixelOp.red(px);
                assertThat(r).isLessThanOrEqualTo(threshold);
            }
        }
    }

    @Nested
    @DisplayName("TOZERO 模式测试")
    class ToZeroModeTests {

        @Test
        @DisplayName("低于阈值置 0，高于等于阈值保持原值")
        void toZeroMode() {
            int threshold = 100;
            BufferedImage result = ThresholdOp.apply(testImage, threshold, ThresholdOp.Mode.TOZERO);
            int[] pixels = PixelOp.getPixels(result);

            for (int px : pixels) {
                int r = PixelOp.red(px);
                // Either 0 or >= threshold
                assertThat(r == 0 || r >= threshold).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("TOZERO_INV 模式测试")
    class ToZeroInvModeTests {

        @Test
        @DisplayName("低于阈值保持原值，高于等于阈值置 0")
        void toZeroInvMode() {
            int threshold = 100;
            BufferedImage result = ThresholdOp.apply(testImage, threshold, ThresholdOp.Mode.TOZERO_INV);
            int[] pixels = PixelOp.getPixels(result);

            for (int px : pixels) {
                int r = PixelOp.red(px);
                // Either 0 or < threshold
                assertThat(r).isLessThan(threshold);
            }
        }
    }

    @Nested
    @DisplayName("边界值测试")
    class BoundaryTests {

        @Test
        @DisplayName("threshold=0 时所有像素为 255（BINARY 模式）")
        void thresholdZeroBinary() {
            BufferedImage result = ThresholdOp.apply(testImage, 0, ThresholdOp.Mode.BINARY);
            int[] pixels = PixelOp.getPixels(result);

            for (int px : pixels) {
                assertThat(PixelOp.red(px)).isEqualTo(255);
            }
        }

        @Test
        @DisplayName("threshold=255 时仅灰度=255的像素为 255（BINARY 模式）")
        void threshold255Binary() {
            // Create image with all pixels at gray=200
            BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
            int[] px = PixelOp.getPixels(img);
            for (int i = 0; i < px.length; i++) {
                px[i] = PixelOp.argb(255, 200, 200, 200);
            }

            BufferedImage result = ThresholdOp.apply(img, 255, ThresholdOp.Mode.BINARY);
            int[] resultPx = PixelOp.getPixels(result);

            for (int p : resultPx) {
                assertThat(PixelOp.red(p)).isEqualTo(0);
            }
        }
    }

    @Nested
    @DisplayName("异常测试")
    class ExceptionTests {

        @Test
        @DisplayName("threshold 小于 0 抛出异常")
        void thresholdBelowRange() {
            assertThatThrownBy(() -> ThresholdOp.apply(testImage, -1, ThresholdOp.Mode.BINARY))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("-1");
        }

        @Test
        @DisplayName("threshold 大于 255 抛出异常")
        void thresholdAboveRange() {
            assertThatThrownBy(() -> ThresholdOp.apply(testImage, 256, ThresholdOp.Mode.BINARY))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("256");
        }

        @Test
        @DisplayName("image 为 null 抛出 NullPointerException")
        void nullImage() {
            assertThatThrownBy(() -> ThresholdOp.apply(null, 128, ThresholdOp.Mode.BINARY))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("mode 为 null 抛出 NullPointerException")
        void nullMode() {
            assertThatThrownBy(() -> ThresholdOp.apply(testImage, 128, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
