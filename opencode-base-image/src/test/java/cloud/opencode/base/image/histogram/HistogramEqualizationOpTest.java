package cloud.opencode.base.image.histogram;

import cloud.opencode.base.image.kernel.ChannelOp;
import cloud.opencode.base.image.kernel.PixelOp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * HistogramEqualizationOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("HistogramEqualizationOp 直方图均衡化测试")
class HistogramEqualizationOpTest {

    /**
     * Create a low-contrast image (all values in a narrow range).
     */
    private BufferedImage createLowContrastImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = PixelOp.getPixels(img);
        for (int i = 0; i < pixels.length; i++) {
            // Values between 100 and 120 — very low contrast
            int v = 100 + (i % 21);
            pixels[i] = PixelOp.argb(255, v, v, v);
        }
        return img;
    }

    /**
     * Compute histogram spread (max - min of non-zero bins).
     */
    private int histogramSpread(BufferedImage img) {
        int[] pixels = PixelOp.getPixels(PixelOp.ensureArgb(img));
        int[] gray = ChannelOp.toGray(pixels);
        int min = 255, max = 0;
        for (int v : gray) {
            if (v < min) { min = v; }
            if (v > max) { max = v; }
        }
        return max - min;
    }

    @Nested
    @DisplayName("apply 灰度均衡化测试")
    class GrayEqualizationTests {

        @Test
        @DisplayName("低对比度图像均衡化后对比度增加")
        void lowContrastImageImproved() {
            BufferedImage img = createLowContrastImage(16, 16);
            int spreadBefore = histogramSpread(img);

            BufferedImage equalized = HistogramEqualizationOp.apply(img);
            int spreadAfter = histogramSpread(equalized);

            assertThat(spreadAfter).isGreaterThan(spreadBefore);
        }

        @Test
        @DisplayName("均匀图像均衡化后不崩溃")
        void uniformImageDoesNotCrash() {
            BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(img);
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = PixelOp.argb(255, 128, 128, 128);
            }

            BufferedImage equalized = HistogramEqualizationOp.apply(img);
            assertThat(equalized).isNotNull();
            assertThat(equalized.getWidth()).isEqualTo(10);
            assertThat(equalized.getHeight()).isEqualTo(10);
        }

        @Test
        @DisplayName("输出图像尺寸与输入相同")
        void outputSameSize() {
            BufferedImage img = createLowContrastImage(20, 15);
            BufferedImage equalized = HistogramEqualizationOp.apply(img);
            assertThat(equalized.getWidth()).isEqualTo(20);
            assertThat(equalized.getHeight()).isEqualTo(15);
        }

        @Test
        @DisplayName("null image 抛出异常")
        void nullImageThrows() {
            assertThatThrownBy(() -> HistogramEqualizationOp.apply(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("applyColor 彩色均衡化测试")
    class ColorEqualizationTests {

        @Test
        @DisplayName("彩色均衡化可以正常运行")
        void colorEqualizationWorks() {
            int w = 16, h = 16;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(img);
            for (int i = 0; i < pixels.length; i++) {
                // Low contrast color image
                int r = 100 + (i % 10);
                int g = 50 + (i % 10);
                int b = 150 + (i % 10);
                pixels[i] = PixelOp.argb(255, r, g, b);
            }

            BufferedImage equalized = HistogramEqualizationOp.applyColor(img);
            assertThat(equalized).isNotNull();
            assertThat(equalized.getWidth()).isEqualTo(w);
            assertThat(equalized.getHeight()).isEqualTo(h);
        }

        @Test
        @DisplayName("彩色均衡化改善低对比度图像")
        void colorEqualizationImprovesContrast() {
            int w = 16, h = 16;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(img);
            for (int i = 0; i < pixels.length; i++) {
                // Narrow range colors
                int v = 100 + (i % 21);
                pixels[i] = PixelOp.argb(255, v, v, v);
            }

            int spreadBefore = histogramSpread(img);
            BufferedImage equalized = HistogramEqualizationOp.applyColor(img);
            int spreadAfter = histogramSpread(equalized);

            // Color equalization via V channel should also improve spread
            assertThat(spreadAfter).isGreaterThanOrEqualTo(spreadBefore);
        }

        @Test
        @DisplayName("null image 抛出异常")
        void nullImageThrows() {
            assertThatThrownBy(() -> HistogramEqualizationOp.applyColor(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
