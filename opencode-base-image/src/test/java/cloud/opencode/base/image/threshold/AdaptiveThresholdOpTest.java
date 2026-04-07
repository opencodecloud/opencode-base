package cloud.opencode.base.image.threshold;

import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.PixelOp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * AdaptiveThresholdOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("AdaptiveThresholdOp 自适应阈值操作测试")
class AdaptiveThresholdOpTest {

    @Nested
    @DisplayName("MEAN 方法测试")
    class MeanMethodTests {

        @Test
        @DisplayName("不均匀光照图像比全局阈值效果更好")
        void unevenLightingBetterThanGlobal() {
            // Create image simulating uneven lighting: left side dark text on dark bg,
            // right side bright text on bright bg
            int w = 20;
            int h = 10;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(img);

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int baseBrightness = (x < 10) ? 50 : 200; // dark left, bright right
                    boolean isText = (y >= 3 && y <= 6 && x % 3 == 0);
                    int v = isText ? Math.max(0, baseBrightness - 40) : baseBrightness;
                    pixels[y * w + x] = PixelOp.argb(255, v, v, v);
                }
            }

            // Adaptive threshold should produce binary output
            BufferedImage result = AdaptiveThresholdOp.apply(img, 5, 5.0, AdaptiveThresholdOp.Method.MEAN);
            int[] resultPixels = PixelOp.getPixels(result);

            // Verify output is binary (only 0 and 255)
            for (int px : resultPixels) {
                int r = PixelOp.red(px);
                assertThat(r).isIn(0, 255);
            }
        }

        @Test
        @DisplayName("便捷方法默认使用 MEAN 方法")
        void convenienceMethodUsesMean() {
            BufferedImage img = createUniformImage(10, 10, 128);

            BufferedImage result1 = AdaptiveThresholdOp.apply(img, 3, 0.0);
            BufferedImage result2 = AdaptiveThresholdOp.apply(img, 3, 0.0, AdaptiveThresholdOp.Method.MEAN);

            int[] p1 = PixelOp.getPixels(result1);
            int[] p2 = PixelOp.getPixels(result2);
            assertThat(p1).isEqualTo(p2);
        }

        @Test
        @DisplayName("输出仅包含 0 和 255")
        void outputIsBinary() {
            BufferedImage img = createGradientImage(20, 20);

            BufferedImage result = AdaptiveThresholdOp.apply(img, 5, 2.0, AdaptiveThresholdOp.Method.MEAN);
            int[] pixels = PixelOp.getPixels(result);

            for (int px : pixels) {
                int r = PixelOp.red(px);
                int g = PixelOp.green(px);
                int b = PixelOp.blue(px);
                assertThat(r).isIn(0, 255);
                assertThat(g).isIn(0, 255);
                assertThat(b).isIn(0, 255);
            }
        }
    }

    @Nested
    @DisplayName("GAUSSIAN 方法测试")
    class GaussianMethodTests {

        @Test
        @DisplayName("GAUSSIAN 方法产生二值输出")
        void gaussianProducesBinaryOutput() {
            BufferedImage img = createGradientImage(20, 20);

            BufferedImage result = AdaptiveThresholdOp.apply(img, 5, 2.0, AdaptiveThresholdOp.Method.GAUSSIAN);
            int[] pixels = PixelOp.getPixels(result);

            for (int px : pixels) {
                int r = PixelOp.red(px);
                assertThat(r).isIn(0, 255);
            }
        }
    }

    @Nested
    @DisplayName("异常测试")
    class ExceptionTests {

        @Test
        @DisplayName("blockSize 为偶数抛出异常")
        void evenBlockSizeThrows() {
            BufferedImage img = createUniformImage(10, 10, 128);

            assertThatThrownBy(() -> AdaptiveThresholdOp.apply(img, 4, 0.0, AdaptiveThresholdOp.Method.MEAN))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("odd");
        }

        @Test
        @DisplayName("blockSize 小于 3 抛出异常")
        void blockSizeTooSmallThrows() {
            BufferedImage img = createUniformImage(10, 10, 128);

            assertThatThrownBy(() -> AdaptiveThresholdOp.apply(img, 1, 0.0, AdaptiveThresholdOp.Method.MEAN))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("3");
        }

        @Test
        @DisplayName("image 为 null 抛出 NullPointerException")
        void nullImageThrows() {
            assertThatThrownBy(() -> AdaptiveThresholdOp.apply(null, 3, 0.0, AdaptiveThresholdOp.Method.MEAN))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("method 为 null 抛出 NullPointerException")
        void nullMethodThrows() {
            BufferedImage img = createUniformImage(10, 10, 128);

            assertThatThrownBy(() -> AdaptiveThresholdOp.apply(img, 3, 0.0, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("c 为 NaN 抛出异常")
        void nanConstantThrows() {
            BufferedImage img = createUniformImage(10, 10, 128);

            assertThatThrownBy(() -> AdaptiveThresholdOp.apply(img, 3, Double.NaN, AdaptiveThresholdOp.Method.MEAN))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("finite");
        }
    }

    // ---- Helper methods ----

    private static BufferedImage createUniformImage(int w, int h, int grayValue) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = PixelOp.getPixels(img);
        int argb = PixelOp.argb(255, grayValue, grayValue, grayValue);
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = argb;
        }
        return img;
    }

    private static BufferedImage createGradientImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = PixelOp.getPixels(img);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int v = (x * 255) / (w - 1);
                pixels[y * w + x] = PixelOp.argb(255, v, v, v);
            }
        }
        return img;
    }
}
