package cloud.opencode.base.image.edge;

import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.PixelOp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * CannyOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("CannyOp Canny 边缘检测测试")
class CannyOpTest {

    /** Test image: left half black, right half white — strong vertical edge. */
    private BufferedImage edgeImage;

    /** Uniform gray image — no edges. */
    private BufferedImage uniformImage;

    @BeforeEach
    void setUp() {
        int w = 32;
        int h = 32;

        // Edge image: left half black (0), right half white (255)
        edgeImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] edgePixels = PixelOp.getPixels(edgeImage);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int v = (x < w / 2) ? 0 : 255;
                edgePixels[y * w + x] = PixelOp.argb(255, v, v, v);
            }
        }

        // Uniform image: all pixels gray (128)
        uniformImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] uniformPixels = PixelOp.getPixels(uniformImage);
        for (int i = 0; i < uniformPixels.length; i++) {
            uniformPixels[i] = PixelOp.argb(255, 128, 128, 128);
        }
    }

    @Nested
    @DisplayName("二值输出测试")
    class BinaryOutputTests {

        @Test
        @DisplayName("输出仅包含 0 和 255")
        void outputIsBinary() {
            BufferedImage result = CannyOp.apply(edgeImage, 50, 150);

            int[] outPixels = PixelOp.getPixels(result);
            for (int pixel : outPixels) {
                int r = PixelOp.red(pixel);
                assertThat(r).isIn(0, 255);
            }
        }

        @Test
        @DisplayName("默认参数输出也是二值")
        void defaultParamsOutputIsBinary() {
            BufferedImage result = CannyOp.apply(edgeImage);

            int[] outPixels = PixelOp.getPixels(result);
            for (int pixel : outPixels) {
                int r = PixelOp.red(pixel);
                assertThat(r).isIn(0, 255);
            }
        }
    }

    @Nested
    @DisplayName("边缘检测测试")
    class EdgeDetectionTests {

        @Test
        @DisplayName("高对比度图像应检测到边缘")
        void detectsEdgesOnHighContrastImage() {
            BufferedImage result = CannyOp.apply(edgeImage, 30, 100);

            int[] outPixels = PixelOp.getPixels(result);
            // Count edge pixels (value = 255)
            int edgeCount = 0;
            for (int pixel : outPixels) {
                if (PixelOp.red(pixel) == 255) {
                    edgeCount++;
                }
            }
            // Should detect at least some edge pixels along the vertical boundary
            assertThat(edgeCount).isGreaterThan(0);
        }

        @Test
        @DisplayName("均匀图像应几乎没有边缘")
        void uniformImageHasMinimalEdges() {
            BufferedImage result = CannyOp.apply(uniformImage, 50, 150);

            int[] outPixels = PixelOp.getPixels(result);
            int edgeCount = 0;
            for (int pixel : outPixels) {
                if (PixelOp.red(pixel) == 255) {
                    edgeCount++;
                }
            }
            // Uniform image should have very few or no edge pixels
            assertThat(edgeCount).isLessThan(5);
        }

        @Test
        @DisplayName("默认参数正常工作")
        void defaultParamsWork() {
            BufferedImage result = CannyOp.apply(edgeImage);

            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(edgeImage.getWidth());
            assertThat(result.getHeight()).isEqualTo(edgeImage.getHeight());
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionTests {

        @Test
        @DisplayName("null 图像抛出 NullPointerException")
        void nullImageThrows() {
            assertThatThrownBy(() -> CannyOp.apply(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null 图像带阈值抛出 NullPointerException")
        void nullImageWithThresholdsThrows() {
            assertThatThrownBy(() -> CannyOp.apply(null, 50, 150))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("lowThreshold > highThreshold 抛出 ImageOperationException")
        void lowGreaterThanHighThrows() {
            assertThatThrownBy(() -> CannyOp.apply(edgeImage, 200, 100))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("lowThreshold");
        }

        @Test
        @DisplayName("负阈值抛出 ImageOperationException")
        void negativeThresholdThrows() {
            assertThatThrownBy(() -> CannyOp.apply(edgeImage, -10, 100))
                    .isInstanceOf(ImageOperationException.class);
        }
    }
}
