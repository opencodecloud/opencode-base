package cloud.opencode.base.image.draw;

import cloud.opencode.base.image.exception.ImageOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * DrawOp 绘图操作测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("DrawOp 绘图操作测试")
class DrawOpTest {

    private static final int WIDTH = 100;
    private static final int HEIGHT = 100;
    private static final Color BG_COLOR = Color.WHITE;

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = testImage.createGraphics();
        g.setColor(BG_COLOR);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.dispose();
    }

    /**
     * Check that at least one pixel in the result differs from the blank white background.
     */
    private boolean hasNonBackgroundPixels(BufferedImage result) {
        int bgRgb = BG_COLOR.getRGB();
        for (int y = 0; y < result.getHeight(); y++) {
            for (int x = 0; x < result.getWidth(); x++) {
                if (result.getRGB(x, y) != bgRgb) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check that the original image is not modified.
     */
    private void assertOriginalUnmodified(BufferedImage result) {
        assertThat(result).isNotSameAs(testImage);
        // original should still be all white
        int bgRgb = BG_COLOR.getRGB();
        for (int y = 0; y < testImage.getHeight(); y++) {
            for (int x = 0; x < testImage.getWidth(); x++) {
                assertThat(testImage.getRGB(x, y)).isEqualTo(bgRgb);
            }
        }
    }

    @Nested
    @DisplayName("线条绘制测试")
    class LineTest {

        @Test
        @DisplayName("绘制线条后端点像素非背景色")
        void drawLinePixelsAtEndpoints() {
            BufferedImage result = DrawOp.line(testImage, 10, 10, 90, 90, Color.RED, 2);

            assertThat(result.getWidth()).isEqualTo(WIDTH);
            assertThat(result.getHeight()).isEqualTo(HEIGHT);
            assertThat(hasNonBackgroundPixels(result)).isTrue();
            assertOriginalUnmodified(result);
        }

        @Test
        @DisplayName("线条端点像素为绘制颜色")
        void lineEndpointPixelColor() {
            BufferedImage result = DrawOp.line(testImage, 10, 10, 90, 90, Color.RED, 3);

            // With anti-aliasing the exact endpoint might not be pure red,
            // but the pixel should not be white
            int pixel = result.getRGB(10, 10);
            assertThat(pixel).isNotEqualTo(BG_COLOR.getRGB());
        }
    }

    @Nested
    @DisplayName("矩形绘制测试")
    class RectTest {

        @Test
        @DisplayName("绘制矩形轮廓")
        void drawRectOutline() {
            BufferedImage result = DrawOp.rect(testImage, 10, 10, 50, 40, Color.BLUE, 2, false);

            assertThat(result.getWidth()).isEqualTo(WIDTH);
            assertThat(result.getHeight()).isEqualTo(HEIGHT);
            assertThat(hasNonBackgroundPixels(result)).isTrue();
            assertOriginalUnmodified(result);
        }

        @Test
        @DisplayName("绘制填充矩形")
        void drawRectFilled() {
            BufferedImage result = DrawOp.rect(testImage, 10, 10, 50, 40, Color.BLUE, 1, true);

            // Center of the rectangle should be filled
            int centerPixel = result.getRGB(35, 30);
            assertThat(centerPixel).isNotEqualTo(BG_COLOR.getRGB());
        }

        @Test
        @DisplayName("轮廓矩形内部区域为背景色")
        void outlineRectInteriorIsBackground() {
            BufferedImage result = DrawOp.rect(testImage, 20, 20, 60, 60, Color.BLUE, 1, false);

            // Interior center should still be background
            int centerPixel = result.getRGB(50, 50);
            assertThat(centerPixel).isEqualTo(BG_COLOR.getRGB());
        }
    }

    @Nested
    @DisplayName("圆形绘制测试")
    class CircleTest {

        @Test
        @DisplayName("绘制圆形轮廓")
        void drawCircleOutline() {
            BufferedImage result = DrawOp.circle(testImage, 50, 50, 30, Color.GREEN, 2, false);

            assertThat(hasNonBackgroundPixels(result)).isTrue();
            assertOriginalUnmodified(result);
        }

        @Test
        @DisplayName("填充圆形中心区域被填充")
        void filledCircleCenterIsFilled() {
            BufferedImage result = DrawOp.circle(testImage, 50, 50, 30, Color.GREEN, 1, true);

            // Center should be filled
            int centerPixel = result.getRGB(50, 50);
            assertThat(centerPixel).isNotEqualTo(BG_COLOR.getRGB());
        }
    }

    @Nested
    @DisplayName("椭圆绘制测试")
    class EllipseTest {

        @Test
        @DisplayName("绘制椭圆")
        void drawEllipse() {
            BufferedImage result = DrawOp.ellipse(testImage, 50, 50, 40, 20, Color.MAGENTA, 2, false);

            assertThat(hasNonBackgroundPixels(result)).isTrue();
            assertOriginalUnmodified(result);
        }

        @Test
        @DisplayName("填充椭圆中心区域被填充")
        void filledEllipseCenterIsFilled() {
            BufferedImage result = DrawOp.ellipse(testImage, 50, 50, 40, 20, Color.MAGENTA, 1, true);

            int centerPixel = result.getRGB(50, 50);
            assertThat(centerPixel).isNotEqualTo(BG_COLOR.getRGB());
        }
    }

    @Nested
    @DisplayName("多边形绘制测试")
    class PolygonTest {

        @Test
        @DisplayName("绘制三角形")
        void drawTriangle() {
            int[] xPoints = {50, 10, 90};
            int[] yPoints = {10, 90, 90};
            BufferedImage result = DrawOp.polygon(testImage, xPoints, yPoints, Color.ORANGE, 2, false);

            assertThat(hasNonBackgroundPixels(result)).isTrue();
            assertOriginalUnmodified(result);
        }

        @Test
        @DisplayName("填充三角形")
        void drawFilledTriangle() {
            int[] xPoints = {50, 10, 90};
            int[] yPoints = {10, 90, 90};
            BufferedImage result = DrawOp.polygon(testImage, xPoints, yPoints, Color.ORANGE, 1, true);

            // Centroid area should be filled
            int centroidPixel = result.getRGB(50, 63);
            assertThat(centroidPixel).isNotEqualTo(BG_COLOR.getRGB());
        }
    }

    @Nested
    @DisplayName("箭头绘制测试")
    class ArrowTest {

        @Test
        @DisplayName("绘制箭头包含箭头头部")
        void drawArrowWithHead() {
            BufferedImage result = DrawOp.arrow(testImage, 10, 50, 90, 50, Color.BLACK, 2);

            assertThat(hasNonBackgroundPixels(result)).isTrue();
            assertOriginalUnmodified(result);

            // The arrowhead area near endpoint should have drawn pixels
            int nearTip = result.getRGB(85, 50);
            assertThat(nearTip).isNotEqualTo(BG_COLOR.getRGB());
        }

        @Test
        @DisplayName("箭头线上的像素非背景色")
        void arrowLinePixels() {
            BufferedImage result = DrawOp.arrow(testImage, 10, 50, 90, 50, Color.DARK_GRAY, 3);

            // Middle of the line
            int midPixel = result.getRGB(50, 50);
            assertThat(midPixel).isNotEqualTo(BG_COLOR.getRGB());
        }
    }

    @Nested
    @DisplayName("文本绘制测试")
    class TextTest {

        @Test
        @DisplayName("绘制文本后输出与空白不同")
        void textRendersAndDiffersFromBlank() {
            Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
            BufferedImage result = DrawOp.text(testImage, "Hello", 10, 50, font, Color.BLACK);

            assertThat(hasNonBackgroundPixels(result)).isTrue();
            assertOriginalUnmodified(result);
        }

        @Test
        @DisplayName("绘制文本尺寸不变")
        void textPreservesDimensions() {
            Font font = new Font(Font.SERIF, Font.BOLD, 16);
            BufferedImage result = DrawOp.text(testImage, "Test", 5, 20, font, Color.RED);

            assertThat(result.getWidth()).isEqualTo(WIDTH);
            assertThat(result.getHeight()).isEqualTo(HEIGHT);
        }
    }

    @Nested
    @DisplayName("参数校验测试")
    class ValidationTest {

        @Test
        @DisplayName("null 图像抛出异常")
        void nullImageThrowsException() {
            assertThatThrownBy(() -> DrawOp.line(null, 0, 0, 10, 10, Color.RED, 1))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("Image");
        }

        @Test
        @DisplayName("null 颜色抛出异常")
        void nullColorThrowsException() {
            assertThatThrownBy(() -> DrawOp.line(testImage, 0, 0, 10, 10, null, 1))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("Color");
        }

        @Test
        @DisplayName("thickness <= 0 抛出异常")
        void zeroThicknessThrowsException() {
            assertThatThrownBy(() -> DrawOp.line(testImage, 0, 0, 10, 10, Color.RED, 0))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("Thickness");

            assertThatThrownBy(() -> DrawOp.rect(testImage, 0, 0, 10, 10, Color.RED, -1, false))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("Thickness");
        }

        @Test
        @DisplayName("text 方法 null 参数抛出异常")
        void textNullParamsThrowException() {
            Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

            assertThatThrownBy(() -> DrawOp.text(null, "hi", 0, 0, font, Color.RED))
                    .isInstanceOf(ImageOperationException.class);

            assertThatThrownBy(() -> DrawOp.text(testImage, null, 0, 0, font, Color.RED))
                    .isInstanceOf(ImageOperationException.class);

            assertThatThrownBy(() -> DrawOp.text(testImage, "hi", 0, 0, null, Color.RED))
                    .isInstanceOf(ImageOperationException.class);

            assertThatThrownBy(() -> DrawOp.text(testImage, "hi", 0, 0, font, null))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("polygon null 点数组抛出异常")
        void polygonNullPointsThrowException() {
            assertThatThrownBy(() -> DrawOp.polygon(testImage, null, new int[]{1, 2, 3}, Color.RED, 1, false))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> DrawOp.polygon(testImage, new int[]{1, 2, 3}, null, Color.RED, 1, false))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("polygon 点数组长度不匹配抛出异常")
        void polygonMismatchedArraysThrowException() {
            assertThatThrownBy(() -> DrawOp.polygon(testImage, new int[]{1, 2}, new int[]{1, 2, 3}, Color.RED, 1, false))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("same length");
        }

        @Test
        @DisplayName("polygon 少于3个点抛出异常")
        void polygonTooFewPointsThrowException() {
            assertThatThrownBy(() -> DrawOp.polygon(testImage, new int[]{1, 2}, new int[]{1, 2}, Color.RED, 1, false))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("at least 3");
        }
    }
}
