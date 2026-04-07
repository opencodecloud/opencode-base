package cloud.opencode.base.image.analysis;

import cloud.opencode.base.image.analysis.ConnectedComponentsOp.Component;
import cloud.opencode.base.image.analysis.ConnectedComponentsOp.Connectivity;
import cloud.opencode.base.image.analysis.ConnectedComponentsOp.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * ConnectedComponentsOp 连通域分析测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("ConnectedComponentsOp 连通域分析测试")
class ConnectedComponentsOpTest {

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
     * Create a white image of specified size.
     */
    private static BufferedImage createWhiteImage(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return img;
    }

    /**
     * Set a pixel to white on a black background image.
     */
    private static void setWhite(BufferedImage img, int x, int y) {
        img.setRGB(x, y, Color.WHITE.getRGB());
    }

    @Nested
    @DisplayName("单个连通域测试")
    class SingleComponentTests {

        @Test
        @DisplayName("一个 3x3 白色方块应检测为 1 个连通域，面积为 9")
        void singleSquareComponent() {
            BufferedImage img = createBlackImage(10, 10);
            // Draw a 3x3 white square at (2,2)
            for (int y = 2; y <= 4; y++) {
                for (int x = 2; x <= 4; x++) {
                    setWhite(img, x, y);
                }
            }

            Result result = ConnectedComponentsOp.analyze(img);

            assertThat(result.componentCount()).isEqualTo(1);
            Component c = result.components().getFirst();
            assertThat(c.area()).isEqualTo(9);
            assertThat(c.boundsX()).isEqualTo(2);
            assertThat(c.boundsY()).isEqualTo(2);
            assertThat(c.boundsWidth()).isEqualTo(3);
            assertThat(c.boundsHeight()).isEqualTo(3);
        }

        @Test
        @DisplayName("单个像素连通域的面积为 1")
        void singlePixelComponent() {
            BufferedImage img = createBlackImage(10, 10);
            setWhite(img, 5, 5);

            Result result = ConnectedComponentsOp.analyze(img);

            assertThat(result.componentCount()).isEqualTo(1);
            Component c = result.components().getFirst();
            assertThat(c.area()).isEqualTo(1);
            assertThat(c.centroidX()).isEqualTo(5);
            assertThat(c.centroidY()).isEqualTo(5);
            assertThat(c.boundsWidth()).isEqualTo(1);
            assertThat(c.boundsHeight()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("多个连通域测试")
    class MultipleComponentTests {

        @Test
        @DisplayName("两个分离的方块应检测为 2 个连通域")
        void twoSeparateSquares() {
            BufferedImage img = createBlackImage(10, 10);
            // Square 1: 3x3 at (0,0)
            for (int y = 0; y <= 2; y++) {
                for (int x = 0; x <= 2; x++) {
                    setWhite(img, x, y);
                }
            }
            // Square 2: 2x2 at (7,7)
            for (int y = 7; y <= 8; y++) {
                for (int x = 7; x <= 8; x++) {
                    setWhite(img, x, y);
                }
            }

            Result result = ConnectedComponentsOp.analyze(img);

            assertThat(result.componentCount()).isEqualTo(2);

            // Find the two components by area
            Component larger = result.components().stream()
                    .filter(c -> c.area() == 9).findFirst().orElseThrow();
            Component smaller = result.components().stream()
                    .filter(c -> c.area() == 4).findFirst().orElseThrow();

            assertThat(larger.boundsX()).isEqualTo(0);
            assertThat(larger.boundsY()).isEqualTo(0);
            assertThat(larger.boundsWidth()).isEqualTo(3);
            assertThat(larger.boundsHeight()).isEqualTo(3);

            assertThat(smaller.boundsX()).isEqualTo(7);
            assertThat(smaller.boundsY()).isEqualTo(7);
            assertThat(smaller.boundsWidth()).isEqualTo(2);
            assertThat(smaller.boundsHeight()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("4-连通 vs 8-连通测试")
    class ConnectivityTests {

        @Test
        @DisplayName("对角线像素在 8-连通下为 1 个连通域")
        void diagonalPixelsEightConnectivity() {
            BufferedImage img = createBlackImage(5, 5);
            // Diagonal: (0,0), (1,1), (2,2)
            setWhite(img, 0, 0);
            setWhite(img, 1, 1);
            setWhite(img, 2, 2);

            Result result = ConnectedComponentsOp.analyze(img, Connectivity.EIGHT);

            assertThat(result.componentCount()).isEqualTo(1);
            assertThat(result.components().getFirst().area()).isEqualTo(3);
        }

        @Test
        @DisplayName("对角线像素在 4-连通下为多个连通域")
        void diagonalPixelsFourConnectivity() {
            BufferedImage img = createBlackImage(5, 5);
            // Diagonal: (0,0), (1,1), (2,2)
            setWhite(img, 0, 0);
            setWhite(img, 1, 1);
            setWhite(img, 2, 2);

            Result result = ConnectedComponentsOp.analyze(img, Connectivity.FOUR);

            // Each diagonal pixel is its own component under 4-connectivity
            assertThat(result.componentCount()).isEqualTo(3);
            for (Component c : result.components()) {
                assertThat(c.area()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("水平相邻像素在两种连通性下都为 1 个连通域")
        void horizontalPixelsBothConnectivities() {
            BufferedImage img = createBlackImage(5, 5);
            setWhite(img, 1, 2);
            setWhite(img, 2, 2);
            setWhite(img, 3, 2);

            Result result4 = ConnectedComponentsOp.analyze(img, Connectivity.FOUR);
            Result result8 = ConnectedComponentsOp.analyze(img, Connectivity.EIGHT);

            assertThat(result4.componentCount()).isEqualTo(1);
            assertThat(result8.componentCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("全黑图像应检测为 0 个连通域")
        void allBlackImage() {
            BufferedImage img = createBlackImage(10, 10);

            Result result = ConnectedComponentsOp.analyze(img);

            assertThat(result.componentCount()).isEqualTo(0);
            assertThat(result.components()).isEmpty();
        }

        @Test
        @DisplayName("全白图像应检测为 1 个连通域，覆盖整个图像")
        void allWhiteImage() {
            int w = 8;
            int h = 6;
            BufferedImage img = createWhiteImage(w, h);

            Result result = ConnectedComponentsOp.analyze(img);

            assertThat(result.componentCount()).isEqualTo(1);
            Component c = result.components().getFirst();
            assertThat(c.area()).isEqualTo(w * h);
            assertThat(c.boundsX()).isEqualTo(0);
            assertThat(c.boundsY()).isEqualTo(0);
            assertThat(c.boundsWidth()).isEqualTo(w);
            assertThat(c.boundsHeight()).isEqualTo(h);
        }

        @Test
        @DisplayName("null 图像应抛出 NullPointerException")
        void nullImageThrowsException() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ConnectedComponentsOp.analyze(null))
                    .withMessageContaining("null");
        }

        @Test
        @DisplayName("null 连通性应抛出 NullPointerException")
        void nullConnectivityThrowsException() {
            BufferedImage img = createBlackImage(5, 5);
            assertThatNullPointerException()
                    .isThrownBy(() -> ConnectedComponentsOp.analyze(img, null))
                    .withMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("质心计算测试")
    class CentroidTests {

        @Test
        @DisplayName("对称方块的质心应在中心位置")
        void centroidOfSymmetricSquare() {
            BufferedImage img = createBlackImage(10, 10);
            // 3x3 square at (3,3) -> center should be (4,4)
            for (int y = 3; y <= 5; y++) {
                for (int x = 3; x <= 5; x++) {
                    setWhite(img, x, y);
                }
            }

            Result result = ConnectedComponentsOp.analyze(img);
            Component c = result.components().getFirst();

            assertThat(c.centroidX()).isEqualTo(4);
            assertThat(c.centroidY()).isEqualTo(4);
        }

        @Test
        @DisplayName("水平线段的质心应在中间位置")
        void centroidOfHorizontalLine() {
            BufferedImage img = createBlackImage(10, 10);
            // Horizontal line at y=5, x=2..6 (5 pixels)
            for (int x = 2; x <= 6; x++) {
                setWhite(img, x, 5);
            }

            Result result = ConnectedComponentsOp.analyze(img);
            Component c = result.components().getFirst();

            assertThat(c.centroidX()).isEqualTo(4); // mean of 2,3,4,5,6 = 4
            assertThat(c.centroidY()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("边界框测试")
    class BoundingBoxTests {

        @Test
        @DisplayName("L 形连通域的边界框应包含所有像素")
        void boundingBoxOfLShape() {
            BufferedImage img = createBlackImage(10, 10);
            // L-shape:
            // (1,1), (1,2), (1,3) - vertical bar
            // (2,3), (3,3)        - horizontal bar
            setWhite(img, 1, 1);
            setWhite(img, 1, 2);
            setWhite(img, 1, 3);
            setWhite(img, 2, 3);
            setWhite(img, 3, 3);

            Result result = ConnectedComponentsOp.analyze(img);

            assertThat(result.componentCount()).isEqualTo(1);
            Component c = result.components().getFirst();
            assertThat(c.area()).isEqualTo(5);
            assertThat(c.boundsX()).isEqualTo(1);
            assertThat(c.boundsY()).isEqualTo(1);
            assertThat(c.boundsWidth()).isEqualTo(3); // x: 1..3
            assertThat(c.boundsHeight()).isEqualTo(3); // y: 1..3
        }
    }

    @Nested
    @DisplayName("标签矩阵测试")
    class LabelMatrixTests {

        @Test
        @DisplayName("标签矩阵中背景像素标签为 0")
        void backgroundLabelsAreZero() {
            BufferedImage img = createBlackImage(5, 5);
            setWhite(img, 2, 2);

            Result result = ConnectedComponentsOp.analyze(img);

            // Check background pixels are 0
            assertThat(result.labels()[0][0]).isEqualTo(0);
            assertThat(result.labels()[4][4]).isEqualTo(0);
            // Check foreground pixel has positive label
            assertThat(result.labels()[2][2]).isGreaterThan(0);
        }

        @Test
        @DisplayName("同一连通域的像素具有相同标签")
        void sameComponentSameLabel() {
            BufferedImage img = createBlackImage(5, 5);
            setWhite(img, 1, 1);
            setWhite(img, 2, 1);
            setWhite(img, 1, 2);

            Result result = ConnectedComponentsOp.analyze(img);

            int label = result.labels()[1][1];
            assertThat(label).isGreaterThan(0);
            assertThat(result.labels()[1][2]).isEqualTo(label);
            assertThat(result.labels()[2][1]).isEqualTo(label);
        }

        @Test
        @DisplayName("不同连通域的像素具有不同标签")
        void differentComponentsDifferentLabels() {
            BufferedImage img = createBlackImage(10, 10);
            setWhite(img, 0, 0);
            setWhite(img, 9, 9);

            Result result = ConnectedComponentsOp.analyze(img);

            assertThat(result.componentCount()).isEqualTo(2);
            int label1 = result.labels()[0][0];
            int label2 = result.labels()[9][9];
            assertThat(label1).isGreaterThan(0);
            assertThat(label2).isGreaterThan(0);
            assertThat(label1).isNotEqualTo(label2);
        }
    }

    @Nested
    @DisplayName("默认连通性测试")
    class DefaultConnectivityTests {

        @Test
        @DisplayName("默认使用 8-连通分析")
        void defaultIsEightConnectivity() {
            BufferedImage img = createBlackImage(5, 5);
            // Diagonal pixels
            setWhite(img, 0, 0);
            setWhite(img, 1, 1);

            Result defaultResult = ConnectedComponentsOp.analyze(img);
            Result eightResult = ConnectedComponentsOp.analyze(img, Connectivity.EIGHT);

            assertThat(defaultResult.componentCount()).isEqualTo(eightResult.componentCount());
            assertThat(defaultResult.componentCount()).isEqualTo(1);
        }
    }
}
