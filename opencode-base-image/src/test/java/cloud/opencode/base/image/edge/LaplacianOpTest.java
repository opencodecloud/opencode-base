package cloud.opencode.base.image.edge;

import cloud.opencode.base.image.kernel.PixelOp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * LaplacianOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("LaplacianOp Laplacian 边缘检测测试")
class LaplacianOpTest {

    /** Test image: left half black, right half white — strong vertical edge. */
    private BufferedImage edgeImage;

    /** Uniform gray image — no edges. */
    private BufferedImage uniformImage;

    @BeforeEach
    void setUp() {
        int w = 32;
        int h = 32;

        // Edge image: left half black (0), right half white (255)
        edgeImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] edgePixels = PixelOp.getPixels(edgeImage);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int v = (x < w / 2) ? 0 : 255;
                edgePixels[y * w + x] = PixelOp.argb(255, v, v, v);
            }
        }

        // Uniform image: all pixels gray (128)
        uniformImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] uniformPixels = PixelOp.getPixels(uniformImage);
        for (int i = 0; i < uniformPixels.length; i++) {
            uniformPixels[i] = PixelOp.argb(255, 128, 128, 128);
        }
    }

    @Nested
    @DisplayName("边缘检测测试")
    class EdgeDetectionTests {

        @Test
        @DisplayName("应检测到边缘图像中的边缘")
        void detectsEdgesInEdgeImage() {
            BufferedImage result = LaplacianOp.apply(edgeImage);

            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(edgeImage.getWidth());
            assertThat(result.getHeight()).isEqualTo(edgeImage.getHeight());

            int[] outPixels = PixelOp.getPixels(result);
            // Near the edge boundary, Laplacian response should be high
            int midY = 16;
            // Check pixels at x=15 (just left of edge) or x=16 (just right)
            int nearEdgeVal = PixelOp.red(outPixels[midY * 32 + 15]);
            int nearEdgeVal2 = PixelOp.red(outPixels[midY * 32 + 16]);
            int maxEdge = Math.max(nearEdgeVal, nearEdgeVal2);
            assertThat(maxEdge).isGreaterThan(50);
        }

        @Test
        @DisplayName("均匀图像 Laplacian 响应接近零")
        void uniformImageNearZero() {
            BufferedImage result = LaplacianOp.apply(uniformImage);

            int[] outPixels = PixelOp.getPixels(result);
            for (int pixel : outPixels) {
                int r = PixelOp.red(pixel);
                assertThat(r).isLessThanOrEqualTo(5);
            }
        }

        @Test
        @DisplayName("输出像素值在 [0, 255] 范围内")
        void outputValuesInRange() {
            BufferedImage result = LaplacianOp.apply(edgeImage);

            int[] outPixels = PixelOp.getPixels(result);
            for (int pixel : outPixels) {
                int r = PixelOp.red(pixel);
                assertThat(r).isBetween(0, 255);
            }
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionTests {

        @Test
        @DisplayName("null 图像抛出 NullPointerException")
        void nullImageThrows() {
            assertThatThrownBy(() -> LaplacianOp.apply(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
