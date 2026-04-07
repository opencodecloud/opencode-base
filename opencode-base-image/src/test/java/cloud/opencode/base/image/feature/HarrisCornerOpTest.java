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
 * HarrisCornerOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("HarrisCornerOp Harris 角点检测测试")
class HarrisCornerOpTest {

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
    @DisplayName("detect(image, k, threshold) 显式参数检测测试")
    class DetectWithThresholdTests {

        @Test
        @DisplayName("棋盘格图像应在交叉点处检测到角点")
        void checkerboard_shouldDetectCornersAtIntersections() {
            List<double[]> corners = HarrisCornerOp.detect(checkerboard, 0.04, 0.0);

            assertThat(corners).isNotEmpty();

            // All corners should have positive response
            for (double[] corner : corners) {
                assertThat(corner).hasSize(3);
                assertThat(corner[0]).isBetween(0.0, 99.0);  // x in bounds
                assertThat(corner[1]).isBetween(0.0, 99.0);  // y in bounds
                assertThat(corner[2]).isGreaterThan(0.0);     // positive response
            }
        }

        @Test
        @DisplayName("均匀图像应检测到很少或没有角点")
        void uniformImage_shouldDetectFewOrNoCorners() {
            List<double[]> corners = HarrisCornerOp.detect(uniformImage, 0.04, 1.0);

            assertThat(corners).isEmpty();
        }

        @Test
        @DisplayName("null 图像应抛出异常")
        void nullImage_shouldThrowException() {
            assertThatNullPointerException()
                    .isThrownBy(() -> HarrisCornerOp.detect(null, 0.04, 1.0));
        }
    }

    @Nested
    @DisplayName("detect(image, maxCorners) Top-N 检测测试")
    class DetectTopNTests {

        @Test
        @DisplayName("棋盘格图像应检测到角点")
        void checkerboard_shouldDetectCorners() {
            List<double[]> corners = HarrisCornerOp.detect(checkerboard, 50);

            assertThat(corners).isNotEmpty();

            // Results should be sorted by response descending
            for (int i = 1; i < corners.size(); i++) {
                assertThat(corners.get(i)[2]).isLessThanOrEqualTo(corners.get(i - 1)[2]);
            }
        }

        @Test
        @DisplayName("maxCorners 限制应生效")
        void maxCorners_shouldLimitResults() {
            List<double[]> corners = HarrisCornerOp.detect(checkerboard, 3);

            assertThat(corners).hasSizeLessThanOrEqualTo(3);
        }

        @Test
        @DisplayName("null 图像应抛出异常")
        void nullImage_shouldThrowException() {
            assertThatNullPointerException()
                    .isThrownBy(() -> HarrisCornerOp.detect(null, 10));
        }

        @Test
        @DisplayName("maxCorners 为零或负数应抛出异常")
        void invalidMaxCorners_shouldThrowException() {
            assertThatThrownBy(() -> HarrisCornerOp.detect(checkerboard, 0))
                    .isInstanceOf(ImageOperationException.class);

            assertThatThrownBy(() -> HarrisCornerOp.detect(checkerboard, -1))
                    .isInstanceOf(ImageOperationException.class);
        }
    }

    @Nested
    @DisplayName("responseImage 响应图测试")
    class ResponseImageTests {

        @Test
        @DisplayName("响应图应与原图尺寸相同")
        void responseImage_shouldHaveSameDimensions() {
            BufferedImage response = HarrisCornerOp.responseImage(checkerboard, 0.04);

            assertThat(response.getWidth()).isEqualTo(checkerboard.getWidth());
            assertThat(response.getHeight()).isEqualTo(checkerboard.getHeight());
        }

        @Test
        @DisplayName("响应图应返回有效图像")
        void responseImage_shouldReturnValidImage() {
            BufferedImage response = HarrisCornerOp.responseImage(checkerboard, 0.04);

            assertThat(response).isNotNull();
            assertThat(response.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
        }

        @Test
        @DisplayName("null 图像应抛出异常")
        void nullImage_shouldThrowException() {
            assertThatNullPointerException()
                    .isThrownBy(() -> HarrisCornerOp.responseImage(null, 0.04));
        }

        @Test
        @DisplayName("均匀图像的响应图像素值应较低")
        void uniformImage_shouldHaveLowResponse() {
            BufferedImage response = HarrisCornerOp.responseImage(uniformImage, 0.04);
            int[] pixels = PixelOp.getPixels(response);

            // Most pixels in the uniform image should have very low or zero response
            int lowCount = 0;
            for (int px : pixels) {
                int r = PixelOp.red(px);
                if (r <= 10) {
                    lowCount++;
                }
            }
            // At least 90% of pixels should have low response
            assertThat(lowCount).isGreaterThan(pixels.length * 9 / 10);
        }
    }
}
