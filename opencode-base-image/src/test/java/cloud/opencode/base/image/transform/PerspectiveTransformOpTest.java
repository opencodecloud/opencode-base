package cloud.opencode.base.image.transform;

import cloud.opencode.base.image.exception.ImageOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * PerspectiveTransformOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("PerspectiveTransformOp 透视变换测试")
class PerspectiveTransformOpTest {

    private static BufferedImage createTestImage(int width, int height, Color color) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setColor(color);
            g.fillRect(0, 0, width, height);
        } finally {
            g.dispose();
        }
        return img;
    }

    @Nested
    @DisplayName("apply 透视变换测试")
    class ApplyTests {

        @Test
        @DisplayName("单位透视变换保持图像相似")
        void identityPerspectivePreservesImage() {
            BufferedImage src = createTestImage(100, 100, Color.RED);

            // Identity mapping: same 4 corners
            double[] srcPts = {0, 0, 99, 0, 99, 99, 0, 99};
            double[] dstPts = {0, 0, 99, 0, 99, 99, 0, 99};

            BufferedImage result = PerspectiveTransformOp.apply(src, srcPts, dstPts, 100, 100);

            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(100);
            assertThat(result.getHeight()).isEqualTo(100);

            // Center pixel should be red (or very close)
            int px = result.getRGB(50, 50);
            int r = (px >> 16) & 0xFF;
            int g = (px >> 8) & 0xFF;
            int b = px & 0xFF;
            assertThat(r).isGreaterThan(200);
            assertThat(g).isLessThan(50);
            assertThat(b).isLessThan(50);
        }

        @Test
        @DisplayName("文档矫正：梯形转矩形")
        void documentRectificationTrapezoidToRectangle() {
            // Create a 400x300 image with content
            BufferedImage src = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = src.createGraphics();
            try {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, 400, 300);
                g.setColor(Color.BLACK);
                g.fillRect(50, 30, 300, 240);
            } finally {
                g.dispose();
            }

            // Trapezoid source region (simulating perspective distortion)
            double[] srcPts = {50, 30, 350, 30, 380, 270, 20, 270};
            // Rectangle destination
            double[] dstPts = {0, 0, 300, 0, 300, 240, 0, 240};

            BufferedImage result = PerspectiveTransformOp.apply(src, srcPts, dstPts, 300, 240);

            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(300);
            assertThat(result.getHeight()).isEqualTo(240);

            // The center of the rectified region should contain content (not transparent)
            int centerPx = result.getRGB(150, 120);
            int alpha = (centerPx >> 24) & 0xFF;
            assertThat(alpha).isGreaterThan(0);
        }

        @Test
        @DisplayName("透视变换输出有效图像")
        void perspectiveTransformProducesValidOutput() {
            BufferedImage src = createTestImage(200, 200, Color.BLUE);

            // Slight perspective shift
            double[] srcPts = {0, 0, 199, 0, 199, 199, 0, 199};
            double[] dstPts = {20, 10, 180, 20, 190, 190, 10, 180};

            BufferedImage result = PerspectiveTransformOp.apply(src, srcPts, dstPts, 200, 200);

            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(200);
            assertThat(result.getHeight()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("参数验证测试")
    class ValidationTests {

        @Test
        @DisplayName("null 图像抛出异常")
        void nullImageThrowsException() {
            double[] src = {0, 0, 100, 0, 100, 100, 0, 100};
            double[] dst = {0, 0, 100, 0, 100, 100, 0, 100};

            assertThatNullPointerException()
                    .isThrownBy(() -> PerspectiveTransformOp.apply(null, src, dst, 100, 100));
        }

        @Test
        @DisplayName("null srcPoints 抛出异常")
        void nullSrcPointsThrowsException() {
            BufferedImage img = createTestImage(10, 10, Color.RED);
            double[] dst = {0, 0, 10, 0, 10, 10, 0, 10};

            assertThatNullPointerException()
                    .isThrownBy(() -> PerspectiveTransformOp.apply(img, null, dst, 10, 10));
        }

        @Test
        @DisplayName("null dstPoints 抛出异常")
        void nullDstPointsThrowsException() {
            BufferedImage img = createTestImage(10, 10, Color.RED);
            double[] src = {0, 0, 10, 0, 10, 10, 0, 10};

            assertThatNullPointerException()
                    .isThrownBy(() -> PerspectiveTransformOp.apply(img, src, null, 10, 10));
        }

        @Test
        @DisplayName("srcPoints 长度不足抛出异常")
        void insufficientSrcPointsThrowsException() {
            BufferedImage img = createTestImage(10, 10, Color.RED);
            double[] src = {0, 0, 10, 0, 10, 10}; // only 3 points (6 elements)
            double[] dst = {0, 0, 10, 0, 10, 10, 0, 10};

            assertThatThrownBy(() -> PerspectiveTransformOp.apply(img, src, dst, 10, 10))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("8 elements");
        }

        @Test
        @DisplayName("dstPoints 长度不足抛出异常")
        void insufficientDstPointsThrowsException() {
            BufferedImage img = createTestImage(10, 10, Color.RED);
            double[] src = {0, 0, 10, 0, 10, 10, 0, 10};
            double[] dst = {0, 0, 10, 0}; // only 2 points

            assertThatThrownBy(() -> PerspectiveTransformOp.apply(img, src, dst, 10, 10))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("8 elements");
        }

        @Test
        @DisplayName("输出宽度为零抛出异常")
        void zeroOutputWidthThrowsException() {
            BufferedImage img = createTestImage(10, 10, Color.RED);
            double[] src = {0, 0, 10, 0, 10, 10, 0, 10};
            double[] dst = {0, 0, 10, 0, 10, 10, 0, 10};

            assertThatThrownBy(() -> PerspectiveTransformOp.apply(img, src, dst, 0, 10))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("输出高度为负数抛出异常")
        void negativeOutputHeightThrowsException() {
            BufferedImage img = createTestImage(10, 10, Color.RED);
            double[] src = {0, 0, 10, 0, 10, 10, 0, 10};
            double[] dst = {0, 0, 10, 0, 10, 10, 0, 10};

            assertThatThrownBy(() -> PerspectiveTransformOp.apply(img, src, dst, 10, -5))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("NaN 坐标抛出异常")
        void nanCoordinatesThrowsException() {
            BufferedImage img = createTestImage(10, 10, Color.RED);
            double[] src = {0, 0, 10, 0, 10, 10, 0, Double.NaN};
            double[] dst = {0, 0, 10, 0, 10, 10, 0, 10};

            assertThatThrownBy(() -> PerspectiveTransformOp.apply(img, src, dst, 10, 10))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("not finite");
        }

        @Test
        @DisplayName("Infinity 坐标抛出异常")
        void infinityCoordinatesThrowsException() {
            BufferedImage img = createTestImage(10, 10, Color.RED);
            double[] src = {0, 0, 10, 0, 10, 10, 0, 10};
            double[] dst = {Double.NEGATIVE_INFINITY, 0, 10, 0, 10, 10, 0, 10};

            assertThatThrownBy(() -> PerspectiveTransformOp.apply(img, src, dst, 10, 10))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("not finite");
        }
    }
}
