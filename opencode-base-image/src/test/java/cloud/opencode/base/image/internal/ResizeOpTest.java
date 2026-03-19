package cloud.opencode.base.image.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * ResizeOp 操作测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("ResizeOp 操作测试")
class ResizeOpTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
    }

    @Nested
    @DisplayName("resize方法测试")
    class ResizeTests {

        @Test
        @DisplayName("调整到指定尺寸")
        void testResize() {
            BufferedImage result = ResizeOp.resize(testImage, 400, 300);

            assertThat(result.getWidth()).isEqualTo(400);
            assertThat(result.getHeight()).isEqualTo(300);
        }

        @Test
        @DisplayName("放大图片")
        void testResizeEnlarge() {
            BufferedImage result = ResizeOp.resize(testImage, 1600, 1200);

            assertThat(result.getWidth()).isEqualTo(1600);
            assertThat(result.getHeight()).isEqualTo(1200);
        }

        @Test
        @DisplayName("非等比例缩放")
        void testResizeNonProportional() {
            BufferedImage result = ResizeOp.resize(testImage, 500, 500);

            assertThat(result.getWidth()).isEqualTo(500);
            assertThat(result.getHeight()).isEqualTo(500);
        }

        @Test
        @DisplayName("宽度为0抛出异常")
        void testResizeZeroWidth() {
            assertThatThrownBy(() -> ResizeOp.resize(testImage, 0, 300))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("高度为0抛出异常")
        void testResizeZeroHeight() {
            assertThatThrownBy(() -> ResizeOp.resize(testImage, 400, 0))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负宽度抛出异常")
        void testResizeNegativeWidth() {
            assertThatThrownBy(() -> ResizeOp.resize(testImage, -100, 300))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("resizeToFit方法测试")
    class ResizeToFitTests {

        @Test
        @DisplayName("按比例缩小")
        void testResizeToFit() {
            BufferedImage result = ResizeOp.resizeToFit(testImage, 400, 400);

            // 应该保持比例，宽度为400，高度为300
            assertThat(result.getWidth()).isEqualTo(400);
            assertThat(result.getHeight()).isEqualTo(300);
        }

        @Test
        @DisplayName("高度限制缩小")
        void testResizeToFitHeightLimited() {
            BufferedImage result = ResizeOp.resizeToFit(testImage, 1000, 300);

            // 应该保持比例，高度为300
            assertThat(result.getHeight()).isEqualTo(300);
            assertThat(result.getWidth()).isEqualTo(400);
        }

        @Test
        @DisplayName("不放大图片")
        void testResizeToFitNoUpscale() {
            BufferedImage result = ResizeOp.resizeToFit(testImage, 1600, 1200);

            // 不应该放大，返回原始尺寸
            assertThat(result.getWidth()).isEqualTo(800);
            assertThat(result.getHeight()).isEqualTo(600);
        }
    }

    @Nested
    @DisplayName("scale方法测试")
    class ScaleTests {

        @Test
        @DisplayName("按因子缩小")
        void testScaleDown() {
            BufferedImage result = ResizeOp.scale(testImage, 0.5);

            assertThat(result.getWidth()).isEqualTo(400);
            assertThat(result.getHeight()).isEqualTo(300);
        }

        @Test
        @DisplayName("按因子放大")
        void testScaleUp() {
            BufferedImage result = ResizeOp.scale(testImage, 2.0);

            assertThat(result.getWidth()).isEqualTo(1600);
            assertThat(result.getHeight()).isEqualTo(1200);
        }

        @Test
        @DisplayName("缩放因子为1")
        void testScaleOne() {
            BufferedImage result = ResizeOp.scale(testImage, 1.0);

            assertThat(result.getWidth()).isEqualTo(800);
            assertThat(result.getHeight()).isEqualTo(600);
        }

        @Test
        @DisplayName("缩放因子为0抛出异常")
        void testScaleZero() {
            assertThatThrownBy(() -> ResizeOp.scale(testImage, 0))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负缩放因子抛出异常")
        void testScaleNegative() {
            assertThatThrownBy(() -> ResizeOp.scale(testImage, -0.5))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("scaleToWidth方法测试")
    class ScaleToWidthTests {

        @Test
        @DisplayName("按宽度缩放")
        void testScaleToWidth() {
            BufferedImage result = ResizeOp.scaleToWidth(testImage, 400);

            assertThat(result.getWidth()).isEqualTo(400);
            assertThat(result.getHeight()).isEqualTo(300);
        }

        @Test
        @DisplayName("放大宽度")
        void testScaleToWidthEnlarge() {
            BufferedImage result = ResizeOp.scaleToWidth(testImage, 1600);

            assertThat(result.getWidth()).isEqualTo(1600);
            assertThat(result.getHeight()).isEqualTo(1200);
        }
    }

    @Nested
    @DisplayName("scaleToHeight方法测试")
    class ScaleToHeightTests {

        @Test
        @DisplayName("按高度缩放")
        void testScaleToHeight() {
            BufferedImage result = ResizeOp.scaleToHeight(testImage, 300);

            assertThat(result.getHeight()).isEqualTo(300);
            assertThat(result.getWidth()).isEqualTo(400);
        }

        @Test
        @DisplayName("放大高度")
        void testScaleToHeightEnlarge() {
            BufferedImage result = ResizeOp.scaleToHeight(testImage, 1200);

            assertThat(result.getHeight()).isEqualTo(1200);
            assertThat(result.getWidth()).isEqualTo(1600);
        }
    }

    @Nested
    @DisplayName("resizeToCover方法测试")
    class ResizeToCoverTests {

        @Test
        @DisplayName("覆盖目标区域")
        void testResizeToCover() {
            BufferedImage result = ResizeOp.resizeToCover(testImage, 300, 300);

            assertThat(result.getWidth()).isEqualTo(300);
            assertThat(result.getHeight()).isEqualTo(300);
        }

        @Test
        @DisplayName("宽图片覆盖")
        void testResizeToCoverWideImage() {
            BufferedImage result = ResizeOp.resizeToCover(testImage, 200, 200);

            assertThat(result.getWidth()).isEqualTo(200);
            assertThat(result.getHeight()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("图片类型测试")
    class ImageTypeTests {

        @Test
        @DisplayName("保持RGB类型")
        void testPreservesRGBType() {
            BufferedImage rgb = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            BufferedImage result = ResizeOp.resize(rgb, 50, 50);

            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);
        }

        @Test
        @DisplayName("保持ARGB类型")
        void testPreservesARGBType() {
            BufferedImage argb = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            BufferedImage result = ResizeOp.resize(argb, 50, 50);

            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
        }
    }
}
