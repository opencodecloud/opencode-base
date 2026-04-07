package cloud.opencode.base.image.morphology;

import cloud.opencode.base.image.kernel.ChannelOp;
import cloud.opencode.base.image.kernel.PixelOp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * MorphologyOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("MorphologyOp 形态学操作测试")
class MorphologyOpTest {

    private StructuringElement se3x3;

    @BeforeEach
    void setUp() {
        se3x3 = StructuringElement.rect(3, 3);
    }

    /**
     * Create a test image: black background with a white rectangle in the center.
     */
    private BufferedImage createWhiteRectOnBlack(int imgW, int imgH, int rectX, int rectY, int rectW, int rectH) {
        BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = PixelOp.getPixels(img);
        // Fill black
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = PixelOp.argb(255, 0, 0, 0);
        }
        // Fill white rectangle
        for (int y = rectY; y < rectY + rectH && y < imgH; y++) {
            for (int x = rectX; x < rectX + rectW && x < imgW; x++) {
                pixels[y * imgW + x] = PixelOp.argb(255, 255, 255, 255);
            }
        }
        return img;
    }

    /**
     * Count pixels above a threshold in the result image.
     */
    private int countWhitePixels(BufferedImage img) {
        int[] pixels = PixelOp.getPixels(PixelOp.ensureArgb(img));
        int[] gray = ChannelOp.toGray(pixels);
        int count = 0;
        for (int v : gray) {
            if (v > 128) {
                count++;
            }
        }
        return count;
    }

    @Nested
    @DisplayName("erode 腐蚀测试")
    class ErodeTests {

        @Test
        @DisplayName("腐蚀缩小白色区域")
        void erodeShrinkWhite() {
            BufferedImage img = createWhiteRectOnBlack(20, 20, 5, 5, 10, 10);
            int whiteBefore = countWhitePixels(img);

            BufferedImage eroded = MorphologyOp.erode(img, se3x3);
            int whiteAfter = countWhitePixels(eroded);

            assertThat(whiteAfter).isLessThan(whiteBefore);
        }

        @Test
        @DisplayName("全黑图像腐蚀后不变")
        void erodeBlackImageUnchanged() {
            BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(img);
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = PixelOp.argb(255, 0, 0, 0);
            }

            BufferedImage eroded = MorphologyOp.erode(img, se3x3);
            assertThat(countWhitePixels(eroded)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("dilate 膨胀测试")
    class DilateTests {

        @Test
        @DisplayName("膨胀扩大白色区域")
        void dilateExpandsWhite() {
            BufferedImage img = createWhiteRectOnBlack(20, 20, 8, 8, 4, 4);
            int whiteBefore = countWhitePixels(img);

            BufferedImage dilated = MorphologyOp.dilate(img, se3x3);
            int whiteAfter = countWhitePixels(dilated);

            assertThat(whiteAfter).isGreaterThan(whiteBefore);
        }

        @Test
        @DisplayName("全白图像膨胀后不变")
        void dilateWhiteImageUnchanged() {
            BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(img);
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = PixelOp.argb(255, 255, 255, 255);
            }

            BufferedImage dilated = MorphologyOp.dilate(img, se3x3);
            int[] resultPixels = PixelOp.getPixels(PixelOp.ensureArgb(dilated));
            int[] gray = ChannelOp.toGray(resultPixels);
            for (int v : gray) {
                assertThat(v).isEqualTo(255);
            }
        }
    }

    @Nested
    @DisplayName("open 开运算测试")
    class OpenTests {

        @Test
        @DisplayName("开运算去除小噪点")
        void openRemovesSmallNoise() {
            // Create black image with a single white pixel (noise)
            BufferedImage img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(img);
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = PixelOp.argb(255, 0, 0, 0);
            }
            // Single white pixel at center
            pixels[10 * 20 + 10] = PixelOp.argb(255, 255, 255, 255);

            BufferedImage opened = MorphologyOp.open(img, se3x3);
            int whiteAfter = countWhitePixels(opened);

            // The single pixel noise should be removed
            assertThat(whiteAfter).isEqualTo(0);
        }

        @Test
        @DisplayName("开运算保留较大结构")
        void openPreservesLargeStructures() {
            BufferedImage img = createWhiteRectOnBlack(30, 30, 5, 5, 20, 20);
            int whiteBefore = countWhitePixels(img);

            BufferedImage opened = MorphologyOp.open(img, se3x3);
            int whiteAfter = countWhitePixels(opened);

            // Large structure should be mostly preserved (slight boundary erosion)
            assertThat(whiteAfter).isGreaterThan(whiteBefore / 2);
        }
    }

    @Nested
    @DisplayName("close 闭运算测试")
    class CloseTests {

        @Test
        @DisplayName("闭运算填充小空隙")
        void closeFillsSmallGaps() {
            // Create a white image with a single black pixel (gap)
            BufferedImage img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(img);
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = PixelOp.argb(255, 255, 255, 255);
            }
            // Single black pixel at center
            pixels[10 * 20 + 10] = PixelOp.argb(255, 0, 0, 0);

            int whiteBefore = countWhitePixels(img);
            BufferedImage closed = MorphologyOp.close(img, se3x3);
            int whiteAfter = countWhitePixels(closed);

            // Gap should be filled
            assertThat(whiteAfter).isGreaterThanOrEqualTo(whiteBefore);
        }
    }

    @Nested
    @DisplayName("gradient 梯度测试")
    class GradientTests {

        @Test
        @DisplayName("梯度提取边缘")
        void gradientExtractsEdges() {
            BufferedImage img = createWhiteRectOnBlack(20, 20, 5, 5, 10, 10);

            BufferedImage grad = MorphologyOp.gradient(img, se3x3);
            int[] gradPixels = PixelOp.getPixels(PixelOp.ensureArgb(grad));
            int[] gray = ChannelOp.toGray(gradPixels);

            // Interior of rectangle and exterior should be ~0
            // Edges should be bright
            int edgeCount = 0;
            for (int v : gray) {
                if (v > 128) {
                    edgeCount++;
                }
            }
            assertThat(edgeCount).isGreaterThan(0);

            // The total gradient area should be much less than the rectangle area
            assertThat(edgeCount).isLessThan(10 * 10);
        }

        @Test
        @DisplayName("均匀图像梯度为零")
        void gradientUniformImageIsZero() {
            BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(img);
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = PixelOp.argb(255, 128, 128, 128);
            }

            BufferedImage grad = MorphologyOp.gradient(img, se3x3);
            int[] gradPixels = PixelOp.getPixels(PixelOp.ensureArgb(grad));
            int[] gray = ChannelOp.toGray(gradPixels);

            for (int v : gray) {
                assertThat(v).isEqualTo(0);
            }
        }
    }

    @Nested
    @DisplayName("异常测试")
    class ExceptionTests {

        @Test
        @DisplayName("erode null image 抛出异常")
        void erodeNullImage() {
            assertThatThrownBy(() -> MorphologyOp.erode(null, se3x3))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("erode null element 抛出异常")
        void erodeNullElement() {
            BufferedImage img = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);
            assertThatThrownBy(() -> MorphologyOp.erode(img, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("dilate null image 抛出异常")
        void dilateNullImage() {
            assertThatThrownBy(() -> MorphologyOp.dilate(null, se3x3))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("gradient null image 抛出异常")
        void gradientNullImage() {
            assertThatThrownBy(() -> MorphologyOp.gradient(null, se3x3))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("topHat null image 抛出异常")
        void topHatNullImage() {
            assertThatThrownBy(() -> MorphologyOp.topHat(null, se3x3))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("blackHat null image 抛出异常")
        void blackHatNullImage() {
            assertThatThrownBy(() -> MorphologyOp.blackHat(null, se3x3))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
