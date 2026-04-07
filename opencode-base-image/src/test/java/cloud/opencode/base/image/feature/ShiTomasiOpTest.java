package cloud.opencode.base.image.feature;

import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.PixelOp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ShiTomasiOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("ShiTomasiOp Shi-Tomasi 角点检测测试")
class ShiTomasiOpTest {

    /** Checkerboard image: alternating 20x20 black/white blocks in 100x100 image. */
    private BufferedImage checkerboard;

    /** Uniform gray image: all pixels same value. */
    private BufferedImage uniformImage;

    @BeforeEach
    void setUp() {
        // Create checkerboard: 100x100 with 20x20 blocks
        int size = 100;
        int blockSize = 20;
        checkerboard = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = PixelOp.getPixels(checkerboard);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int bx = x / blockSize;
                int by = y / blockSize;
                int v = ((bx + by) % 2 == 0) ? 0 : 255;
                pixels[y * size + x] = PixelOp.argb(255, v, v, v);
            }
        }

        // Create uniform image: 100x100, all gray (128)
        uniformImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int[] uniformPixels = PixelOp.getPixels(uniformImage);
        for (int i = 0; i < uniformPixels.length; i++) {
            uniformPixels[i] = PixelOp.argb(255, 128, 128, 128);
        }
    }

    @Nested
    @DisplayName("detect(image, maxCorners, qualityLevel) 自定义参数检测测试")
    class DetectWithParamsTests {

        @Test
        @DisplayName("棋盘格图像应检测到角点")
        void checkerboard_shouldDetectCorners() {
            List<double[]> corners = ShiTomasiOp.detect(checkerboard, 50, 0.01);

            assertThat(corners).isNotEmpty();

            // All corners should have valid coordinates
            for (double[] corner : corners) {
                assertThat(corner).hasSize(3);
                assertThat(corner[0]).isBetween(0.0, 99.0);
                assertThat(corner[1]).isBetween(0.0, 99.0);
            }

            // Results should be sorted by response descending
            for (int i = 1; i < corners.size(); i++) {
                assertThat(corners.get(i)[2]).isLessThanOrEqualTo(corners.get(i - 1)[2]);
            }
        }

        @Test
        @DisplayName("maxCorners=5 应最多返回 5 个结果")
        void maxCorners5_shouldReturnAtMost5() {
            List<double[]> corners = ShiTomasiOp.detect(checkerboard, 5, 0.01);

            assertThat(corners).hasSizeLessThanOrEqualTo(5);
        }

        @Test
        @DisplayName("qualityLevel=1.0 应返回很少角点")
        void qualityLevel1_shouldReturnVeryFewCorners() {
            List<double[]> corners = ShiTomasiOp.detect(checkerboard, 100, 1.0);

            // With quality level 1.0, only the absolute maximum (if unique) should pass
            assertThat(corners).hasSizeLessThanOrEqualTo(1);
        }

        @Test
        @DisplayName("null 图像应抛出异常")
        void nullImage_shouldThrowException() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ShiTomasiOp.detect(null, 50, 0.01));
        }

        @Test
        @DisplayName("maxCorners 为零或负数应抛出异常")
        void invalidMaxCorners_shouldThrowException() {
            assertThatThrownBy(() -> ShiTomasiOp.detect(checkerboard, 0, 0.01))
                    .isInstanceOf(ImageOperationException.class);

            assertThatThrownBy(() -> ShiTomasiOp.detect(checkerboard, -1, 0.01))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("qualityLevel 超出范围应抛出异常")
        void invalidQualityLevel_shouldThrowException() {
            assertThatThrownBy(() -> ShiTomasiOp.detect(checkerboard, 50, -0.1))
                    .isInstanceOf(ImageOperationException.class);

            assertThatThrownBy(() -> ShiTomasiOp.detect(checkerboard, 50, 1.1))
                    .isInstanceOf(ImageOperationException.class);
        }
    }

    @Nested
    @DisplayName("detect(image) 默认参数检测测试")
    class DetectDefaultTests {

        @Test
        @DisplayName("棋盘格图像应使用默认参数检测到角点")
        void checkerboard_shouldDetectCornersWithDefaults() {
            List<double[]> corners = ShiTomasiOp.detect(checkerboard);

            assertThat(corners).isNotEmpty();
            // Default maxCorners is 100
            assertThat(corners).hasSizeLessThanOrEqualTo(100);
        }

        @Test
        @DisplayName("null 图像应抛出异常")
        void nullImage_shouldThrowException() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ShiTomasiOp.detect(null));
        }

        @Test
        @DisplayName("均匀图像应检测到很少角点")
        void uniformImage_shouldDetectFewCorners() {
            List<double[]> corners = ShiTomasiOp.detect(uniformImage);

            // Uniform image has no edges/corners, but due to quantization
            // there might be a few spurious detections at borders
            // The key assertion is that it's significantly fewer than the checkerboard
            List<double[]> checkerCorners = ShiTomasiOp.detect(checkerboard);
            assertThat(corners.size()).isLessThan(checkerCorners.size());
        }
    }
}
