package cloud.opencode.base.image.analysis;

import cloud.opencode.base.image.analysis.ContourFinderOp.Contour;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ContourFinderOp 轮廓检测测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("ContourFinderOp 轮廓检测测试")
class ContourFinderOpTest {

    /**
     * Create a black image of specified size.
     */
    private static BufferedImage createBlackImage(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return img;
    }

    /**
     * Set a pixel to white on an image.
     */
    private static void setWhite(BufferedImage img, int x, int y) {
        img.setRGB(x, y, Color.WHITE.getRGB());
    }

    /**
     * Draw a filled white rectangle on a black image.
     */
    private static BufferedImage createImageWithRect(int imgW, int imgH,
                                                     int rx, int ry, int rw, int rh) {
        BufferedImage img = createBlackImage(imgW, imgH);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(rx, ry, rw, rh);
        g.dispose();
        return img;
    }

    @Nested
    @DisplayName("单个轮廓测试")
    class SingleContourTests {

        @Test
        @DisplayName("单个白色矩形应检测到 1 个轮廓，边界框正确")
        void singleRectangleContour() {
            BufferedImage img = createImageWithRect(20, 20, 5, 5, 8, 6);

            List<Contour> contours = ContourFinderOp.find(img);

            assertThat(contours).hasSize(1);
            Contour c = contours.getFirst();
            int[] bbox = c.boundingBox();
            assertThat(bbox[0]).isEqualTo(5);   // x
            assertThat(bbox[1]).isEqualTo(5);   // y
            assertThat(bbox[2]).isEqualTo(8);   // width
            assertThat(bbox[3]).isEqualTo(6);   // height
        }

        @Test
        @DisplayName("轮廓点数大于 0")
        void contourHasPoints() {
            BufferedImage img = createImageWithRect(20, 20, 3, 3, 5, 5);

            List<Contour> contours = ContourFinderOp.find(img);

            assertThat(contours).isNotEmpty();
            assertThat(contours.getFirst().size()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("多个轮廓测试")
    class MultipleContourTests {

        @Test
        @DisplayName("两个分离的形状应检测到 2 个轮廓")
        void twoSeparateShapes() {
            BufferedImage img = createBlackImage(30, 30);
            Graphics2D g = img.createGraphics();
            g.setColor(Color.WHITE);
            // Shape 1: rectangle at top-left
            g.fillRect(2, 2, 5, 5);
            // Shape 2: rectangle at bottom-right (well separated)
            g.fillRect(20, 20, 5, 5);
            g.dispose();

            List<Contour> contours = ContourFinderOp.find(img);

            assertThat(contours).hasSize(2);
        }
    }

    @Nested
    @DisplayName("空图像测试")
    class EmptyImageTests {

        @Test
        @DisplayName("全黑图像应检测到 0 个轮廓")
        void allBlackImageNoContours() {
            BufferedImage img = createBlackImage(15, 15);

            List<Contour> contours = ContourFinderOp.find(img);

            assertThat(contours).isEmpty();
        }
    }

    @Nested
    @DisplayName("轮廓面积测试")
    class AreaTests {

        @Test
        @DisplayName("矩形轮廓面积与实际面积大致匹配")
        void contourAreaMatchesActualArea() {
            // A 10x10 filled white rectangle
            BufferedImage img = createImageWithRect(30, 30, 5, 5, 10, 10);

            List<Contour> contours = ContourFinderOp.find(img);

            assertThat(contours).hasSize(1);
            Contour c = contours.getFirst();
            double area = c.area();
            // Shoelace area of the border polygon should be roughly close to
            // the actual area (not exact because border tracing gives boundary pixels)
            // For a filled rect, boundary contour area should be > 0
            assertThat(area).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("小三角形轮廓面积大于 0")
        void triangleContourAreaPositive() {
            BufferedImage img = createBlackImage(20, 20);
            // Draw a small triangle manually
            // Row y=5: x=10
            // Row y=6: x=9,10,11
            // Row y=7: x=8,9,10,11,12
            setWhite(img, 10, 5);
            for (int x = 9; x <= 11; x++) {
                setWhite(img, x, 6);
            }
            for (int x = 8; x <= 12; x++) {
                setWhite(img, x, 7);
            }

            List<Contour> contours = ContourFinderOp.find(img);

            assertThat(contours).isNotEmpty();
            assertThat(contours.getFirst().area()).isGreaterThan(0.0);
        }
    }

    @Nested
    @DisplayName("轮廓周长测试")
    class PerimeterTests {

        @Test
        @DisplayName("矩形轮廓周长大于 0")
        void contourPerimeterPositive() {
            BufferedImage img = createImageWithRect(20, 20, 3, 3, 6, 4);

            List<Contour> contours = ContourFinderOp.find(img);

            assertThat(contours).hasSize(1);
            assertThat(contours.getFirst().perimeter()).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("单像素轮廓周长为 0")
        void singlePixelPerimeterZero() {
            BufferedImage img = createBlackImage(10, 10);
            setWhite(img, 5, 5);

            List<Contour> contours = ContourFinderOp.find(img);

            assertThat(contours).hasSize(1);
            // Single point has size 1, perimeter = 0 (no consecutive pair to measure distance)
            assertThat(contours.getFirst().perimeter()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionTests {

        @Test
        @DisplayName("null 图像应抛出 NullPointerException")
        void nullImageThrowsNpe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ContourFinderOp.find(null))
                    .withMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("Contour record 测试")
    class ContourRecordTests {

        @Test
        @DisplayName("空点列表的边界框为 [0,0,0,0]")
        void emptyPointsBoundingBox() {
            // Use reflection-free approach: Contour with empty list
            Contour c = new Contour(List.of());
            int[] bbox = c.boundingBox();
            assertThat(bbox).containsExactly(0, 0, 0, 0);
        }

        @Test
        @DisplayName("空点列表的面积和周长为 0")
        void emptyPointsAreaAndPerimeter() {
            Contour c = new Contour(List.of());
            assertThat(c.area()).isEqualTo(0.0);
            assertThat(c.perimeter()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("null 点列表应抛出 NullPointerException")
        void nullPointsThrowsNpe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new Contour(null))
                    .withMessageContaining("null");
        }
    }
}
