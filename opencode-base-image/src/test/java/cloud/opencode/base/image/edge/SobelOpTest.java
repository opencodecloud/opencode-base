package cloud.opencode.base.image.edge;

import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.ChannelOp;
import cloud.opencode.base.image.kernel.PixelOp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * SobelOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("SobelOp Sobel 边缘检测测试")
class SobelOpTest {

    /** Test image: left half black, right half white — strong vertical edge in the middle. */
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
    @DisplayName("完整梯度幅值测试")
    class FullGradientTests {

        @Test
        @DisplayName("边缘图像应检测到边缘，边缘区域像素值较高")
        void edgeImageHasHighValues() {
            BufferedImage result = SobelOp.apply(edgeImage);

            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(edgeImage.getWidth());
            assertThat(result.getHeight()).isEqualTo(edgeImage.getHeight());

            int[] outPixels = PixelOp.getPixels(result);
            // Check pixels near the vertical edge (x = 15,16) have high gradient
            int midY = 16;
            int edgeX = 16; // right at the boundary
            int edgeVal = PixelOp.red(outPixels[midY * 32 + edgeX]);
            assertThat(edgeVal).isGreaterThan(50);

            // Check far-from-edge pixels have low gradient
            int flatVal = PixelOp.red(outPixels[midY * 32 + 4]);
            assertThat(flatVal).isLessThan(30);
        }

        @Test
        @DisplayName("均匀图像梯度接近零")
        void uniformImageNearZero() {
            BufferedImage result = SobelOp.apply(uniformImage);

            int[] outPixels = PixelOp.getPixels(result);
            for (int pixel : outPixels) {
                int r = PixelOp.red(pixel);
                assertThat(r).isLessThanOrEqualTo(5);
            }
        }
    }

    @Nested
    @DisplayName("方向梯度测试")
    class DirectionalGradientTests {

        @Test
        @DisplayName("dx=1,dy=0 应检测垂直边缘")
        void horizontalGradientDetectsVerticalEdge() {
            BufferedImage result = SobelOp.apply(edgeImage, 1, 0);

            assertThat(result).isNotNull();
            int[] outPixels = PixelOp.getPixels(result);
            // Near the vertical edge, horizontal gradient should be high
            int midY = 16;
            int edgeX = 16;
            int edgeVal = PixelOp.red(outPixels[midY * 32 + edgeX]);
            assertThat(edgeVal).isGreaterThan(50);
        }

        @Test
        @DisplayName("dx=0,dy=1 对纯垂直边缘响应低")
        void verticalGradientLowForVerticalEdge() {
            BufferedImage result = SobelOp.apply(edgeImage, 0, 1);

            int[] outPixels = PixelOp.getPixels(result);
            // The edge is vertical so vertical gradient should be low in the middle
            int midY = 16;
            int midX = 16;
            int val = PixelOp.red(outPixels[midY * 32 + midX]);
            // Vertical gradient on a purely vertical edge: should be much lower than horizontal
            BufferedImage hResult = SobelOp.apply(edgeImage, 1, 0);
            int[] hPixels = PixelOp.getPixels(hResult);
            int hVal = PixelOp.red(hPixels[midY * 32 + midX]);
            assertThat(val).isLessThan(hVal);
        }
    }

    @Nested
    @DisplayName("gradientMagnitude 灰度数组测试")
    class GradientMagnitudeArrayTests {

        @Test
        @DisplayName("灰度数组梯度幅值正确计算")
        void gradientMagnitudeOnArray() {
            int w = 32;
            int h = 32;
            int[] gray = new int[w * h];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    gray[y * w + x] = (x < w / 2) ? 0 : 255;
                }
            }

            int[] magnitude = SobelOp.gradientMagnitude(gray, w, h);

            assertThat(magnitude).hasSize(w * h);
            // Values should be in [0, 255]
            for (int v : magnitude) {
                assertThat(v).isBetween(0, 255);
            }
            // Edge area should have high values
            int edgeVal = magnitude[16 * w + 16];
            assertThat(edgeVal).isGreaterThan(50);
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionTests {

        @Test
        @DisplayName("null 图像抛出 NullPointerException")
        void nullImageThrows() {
            assertThatThrownBy(() -> SobelOp.apply(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("方向梯度 null 图像抛出 NullPointerException")
        void nullImageDirectionalThrows() {
            assertThatThrownBy(() -> SobelOp.apply(null, 1, 0))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("dx=0,dy=0 抛出 ImageOperationException")
        void zeroDxDyThrows() {
            assertThatThrownBy(() -> SobelOp.apply(edgeImage, 0, 0))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("gradientMagnitude null 数组抛出 NullPointerException")
        void nullArrayThrows() {
            assertThatThrownBy(() -> SobelOp.gradientMagnitude(null, 10, 10))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
