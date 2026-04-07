package cloud.opencode.base.image;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ImageStitch 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.3
 */
@DisplayName("ImageStitch 测试")
class ImageStitchTest {

    private BufferedImage img100x80;
    private BufferedImage img200x60;
    private BufferedImage img50x50;

    @BeforeEach
    void setUp() {
        img100x80 = new BufferedImage(100, 80, BufferedImage.TYPE_INT_ARGB);
        img200x60 = new BufferedImage(200, 60, BufferedImage.TYPE_INT_ARGB);
        img50x50 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
    }

    @Nested
    @DisplayName("horizontal 方法测试")
    class HorizontalTests {

        @Test
        @DisplayName("水平拼接两张图片宽度相加")
        void testHorizontalWidth() {
            BufferedImage result = ImageStitch.horizontal(img100x80, img200x60);

            assertThat(result.getWidth()).isEqualTo(300);
        }

        @Test
        @DisplayName("水平拼接高度取最大值")
        void testHorizontalMaxHeight() {
            BufferedImage result = ImageStitch.horizontal(img100x80, img200x60);

            assertThat(result.getHeight()).isEqualTo(80);
        }

        @Test
        @DisplayName("水平拼接含间距")
        void testHorizontalWithGap() {
            BufferedImage result = ImageStitch.horizontal(10, Color.WHITE, img100x80, img200x60);

            assertThat(result.getWidth()).isEqualTo(310); // 100 + 200 + 10
        }

        @Test
        @DisplayName("负间距抛出异常")
        void testHorizontalNegativeGapThrows() {
            assertThatThrownBy(() -> ImageStitch.horizontal(-1, null, img100x80))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("空数组抛出异常")
        void testHorizontalEmptyThrows() {
            assertThatThrownBy(() -> ImageStitch.horizontal())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null数组抛出异常")
        void testHorizontalNullThrows() {
            assertThatThrownBy(() -> ImageStitch.horizontal((BufferedImage[]) null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("数组内null元素抛出异常")
        void testHorizontalNullElementThrows() {
            assertThatThrownBy(() -> ImageStitch.horizontal(img100x80, null, img50x50))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("index 1");
        }
    }

    @Nested
    @DisplayName("vertical 方法测试")
    class VerticalTests {

        @Test
        @DisplayName("垂直拼接宽度取最大值")
        void testVerticalMaxWidth() {
            BufferedImage result = ImageStitch.vertical(img100x80, img200x60);

            assertThat(result.getWidth()).isEqualTo(200);
        }

        @Test
        @DisplayName("垂直拼接高度相加")
        void testVerticalHeight() {
            BufferedImage result = ImageStitch.vertical(img100x80, img200x60);

            assertThat(result.getHeight()).isEqualTo(140);
        }

        @Test
        @DisplayName("垂直拼接含间距")
        void testVerticalWithGap() {
            BufferedImage result = ImageStitch.vertical(5, Color.BLACK, img100x80, img200x60);

            assertThat(result.getHeight()).isEqualTo(145); // 80 + 60 + 5
        }

        @Test
        @DisplayName("空数组抛出异常")
        void testVerticalEmptyThrows() {
            assertThatThrownBy(() -> ImageStitch.vertical())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("grid 方法测试")
    class GridTests {

        @Test
        @DisplayName("2列网格拼接4张图片")
        void testGrid2Columns4Images() {
            List<BufferedImage> images = List.of(img50x50, img50x50, img50x50, img50x50);
            BufferedImage result = ImageStitch.grid(images, 2);

            assertThat(result.getWidth()).isEqualTo(100);  // 2 cols * 50
            assertThat(result.getHeight()).isEqualTo(100); // 2 rows * 50
        }

        @Test
        @DisplayName("3列网格拼接5张图片（最后行不满）")
        void testGrid3Columns5Images() {
            List<BufferedImage> images = List.of(
                    img50x50, img50x50, img50x50, img50x50, img50x50);
            BufferedImage result = ImageStitch.grid(images, 3);

            assertThat(result.getWidth()).isEqualTo(150);  // 3 cols * 50
            assertThat(result.getHeight()).isEqualTo(100); // 2 rows * 50
        }

        @Test
        @DisplayName("单图单列网格")
        void testGridSingleImage() {
            List<BufferedImage> images = List.of(img100x80);
            BufferedImage result = ImageStitch.grid(images, 1);

            assertThat(result.getWidth()).isEqualTo(100);
            assertThat(result.getHeight()).isEqualTo(80);
        }

        @Test
        @DisplayName("列数小于1抛出异常")
        void testGridZeroColumnsThrows() {
            List<BufferedImage> images = List.of(img50x50);
            assertThatThrownBy(() -> ImageStitch.grid(images, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("空列表抛出异常")
        void testGridEmptyListThrows() {
            assertThatThrownBy(() -> ImageStitch.grid(List.of(), 2))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null列表抛出异常")
        void testGridNullListThrows() {
            assertThatNullPointerException().isThrownBy(() -> ImageStitch.grid(null, 2));
        }
    }
}
