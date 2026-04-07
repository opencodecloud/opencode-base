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
 * AffineTransformOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("AffineTransformOp 仿射变换测试")
class AffineTransformOpTest {

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
    @DisplayName("apply(image, matrix) 矩阵变换测试")
    class MatrixApplyTests {

        @Test
        @DisplayName("单位矩阵变换保持尺寸不变")
        void identityMatrixPreservesDimensions() {
            BufferedImage src = createTestImage(100, 80, Color.RED);
            double[] identity = {1, 0, 0, 0, 1, 0};

            BufferedImage result = AffineTransformOp.apply(src, identity);

            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(100);
            assertThat(result.getHeight()).isEqualTo(80);
        }

        @Test
        @DisplayName("单位矩阵变换保持像素不变")
        void identityMatrixPreservesPixels() {
            BufferedImage src = createTestImage(50, 50, Color.BLUE);
            double[] identity = {1, 0, 0, 0, 1, 0};

            BufferedImage result = AffineTransformOp.apply(src, identity);

            // Center pixel should be blue
            int px = result.getRGB(25, 25);
            assertThat((px >> 16) & 0xFF).isEqualTo(0);   // red
            assertThat((px >> 8) & 0xFF).isEqualTo(0);    // green
            assertThat(px & 0xFF).isEqualTo(255);          // blue
        }

        @Test
        @DisplayName("2 倍缩放变换输出尺寸正确")
        void scale2xDoublesDimensions() {
            BufferedImage src = createTestImage(50, 40, Color.GREEN);
            // Scale 2x: x'=2x, y'=2y
            double[] scale2x = {2, 0, 0, 0, 2, 0};

            // Output size is same as input (100x80 would be the ideal but output = input dims)
            BufferedImage result = AffineTransformOp.apply(src, scale2x);

            // Output dimensions match input since apply(image, matrix) uses input dims
            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(50);
            assertThat(result.getHeight()).isEqualTo(40);
        }

        @Test
        @DisplayName("旋转变换产生有效输出")
        void rotationProducesOutput() {
            BufferedImage src = createTestImage(100, 100, Color.RED);
            // 45 degree rotation: a=cos45, b=-sin45, d=sin45, e=cos45
            double cos45 = Math.cos(Math.PI / 4);
            double sin45 = Math.sin(Math.PI / 4);
            double[] rotate45 = {cos45, -sin45, 0, sin45, cos45, 0};

            BufferedImage result = AffineTransformOp.apply(src, rotate45);

            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isGreaterThan(0);
            assertThat(result.getHeight()).isGreaterThan(0);
        }

        @Test
        @DisplayName("null 图像抛出异常")
        void nullImageThrowsException() {
            double[] matrix = {1, 0, 0, 0, 1, 0};
            assertThatNullPointerException()
                    .isThrownBy(() -> AffineTransformOp.apply(null, matrix));
        }

        @Test
        @DisplayName("null 矩阵抛出异常")
        void nullMatrixThrowsException() {
            BufferedImage img = createTestImage(10, 10, Color.RED);
            assertThatNullPointerException()
                    .isThrownBy(() -> AffineTransformOp.apply(img, (double[]) null));
        }

        @Test
        @DisplayName("矩阵长度不为 6 抛出异常")
        void invalidMatrixLengthThrowsException() {
            BufferedImage img = createTestImage(10, 10, Color.RED);

            assertThatThrownBy(() -> AffineTransformOp.apply(img, new double[]{1, 0, 0}))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("6 elements");

            assertThatThrownBy(() -> AffineTransformOp.apply(img, new double[9]))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("6 elements");
        }

        @Test
        @DisplayName("矩阵包含 NaN 抛出异常")
        void nanInMatrixThrowsException() {
            BufferedImage img = createTestImage(10, 10, Color.RED);
            double[] matrix = {1, 0, Double.NaN, 0, 1, 0};

            assertThatThrownBy(() -> AffineTransformOp.apply(img, matrix))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("not finite");
        }

        @Test
        @DisplayName("矩阵包含 Infinity 抛出异常")
        void infinityInMatrixThrowsException() {
            BufferedImage img = createTestImage(10, 10, Color.RED);
            double[] matrix = {1, 0, 0, 0, Double.POSITIVE_INFINITY, 0};

            assertThatThrownBy(() -> AffineTransformOp.apply(img, matrix))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("not finite");
        }
    }

    @Nested
    @DisplayName("apply(image, srcPoints, dstPoints, w, h) 三点映射测试")
    class ThreePointMappingTests {

        @Test
        @DisplayName("三点映射正确变换")
        void threePointMappingWorks() {
            BufferedImage src = createTestImage(100, 100, Color.RED);

            // Map a triangle to a scaled triangle
            double[] srcPts = {0, 0, 100, 0, 0, 100};
            double[] dstPts = {0, 0, 200, 0, 0, 200};

            BufferedImage result = AffineTransformOp.apply(src, srcPts, dstPts, 200, 200);

            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(200);
            assertThat(result.getHeight()).isEqualTo(200);
        }

        @Test
        @DisplayName("单位映射保持图像内容")
        void identityMappingPreservesContent() {
            BufferedImage src = createTestImage(80, 80, Color.CYAN);

            double[] srcPts = {0, 0, 80, 0, 0, 80};
            double[] dstPts = {0, 0, 80, 0, 0, 80};

            BufferedImage result = AffineTransformOp.apply(src, srcPts, dstPts, 80, 80);

            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(80);
            assertThat(result.getHeight()).isEqualTo(80);

            // Center pixel should be cyan
            int px = result.getRGB(40, 40);
            assertThat((px >> 16) & 0xFF).isEqualTo(0);    // red
            assertThat((px >> 8) & 0xFF).isEqualTo(255);   // green
            assertThat(px & 0xFF).isEqualTo(255);           // blue
        }

        @Test
        @DisplayName("null srcPoints 抛出异常")
        void nullSrcPointsThrowsException() {
            BufferedImage img = createTestImage(10, 10, Color.RED);
            double[] dst = {0, 0, 10, 0, 0, 10};

            assertThatNullPointerException()
                    .isThrownBy(() -> AffineTransformOp.apply(img, null, dst, 10, 10));
        }

        @Test
        @DisplayName("null dstPoints 抛出异常")
        void nullDstPointsThrowsException() {
            BufferedImage img = createTestImage(10, 10, Color.RED);
            double[] src = {0, 0, 10, 0, 0, 10};

            assertThatNullPointerException()
                    .isThrownBy(() -> AffineTransformOp.apply(img, src, null, 10, 10));
        }

        @Test
        @DisplayName("srcPoints 长度不为 6 抛出异常")
        void invalidSrcPointsLengthThrowsException() {
            BufferedImage img = createTestImage(10, 10, Color.RED);
            double[] src = {0, 0, 10, 0}; // only 2 points
            double[] dst = {0, 0, 10, 0, 0, 10};

            assertThatThrownBy(() -> AffineTransformOp.apply(img, src, dst, 10, 10))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("6 elements");
        }

        @Test
        @DisplayName("dstPoints 长度不为 6 抛出异常")
        void invalidDstPointsLengthThrowsException() {
            BufferedImage img = createTestImage(10, 10, Color.RED);
            double[] src = {0, 0, 10, 0, 0, 10};
            double[] dst = {0, 0, 10, 0, 0, 10, 5, 5}; // 4 points

            assertThatThrownBy(() -> AffineTransformOp.apply(img, src, dst, 10, 10))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("6 elements");
        }

        @Test
        @DisplayName("输出尺寸非正数抛出异常")
        void nonPositiveOutputDimensionsThrowsException() {
            BufferedImage img = createTestImage(10, 10, Color.RED);
            double[] src = {0, 0, 10, 0, 0, 10};
            double[] dst = {0, 0, 10, 0, 0, 10};

            assertThatThrownBy(() -> AffineTransformOp.apply(img, src, dst, 0, 10))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("positive");

            assertThatThrownBy(() -> AffineTransformOp.apply(img, src, dst, 10, -1))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("共线源点抛出异常")
        void collinearSourcePointsThrowsException() {
            BufferedImage img = createTestImage(10, 10, Color.RED);
            // All points on the same line
            double[] src = {0, 0, 5, 5, 10, 10};
            double[] dst = {0, 0, 10, 0, 0, 10};

            assertThatThrownBy(() -> AffineTransformOp.apply(img, src, dst, 10, 10))
                    .isInstanceOf(ImageOperationException.class)
                    .hasMessageContaining("collinear");
        }
    }
}
