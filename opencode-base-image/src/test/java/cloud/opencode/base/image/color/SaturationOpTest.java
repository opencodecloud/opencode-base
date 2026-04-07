package cloud.opencode.base.image.color;

import cloud.opencode.base.image.exception.ImageOperationException;
import cloud.opencode.base.image.kernel.PixelOp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * SaturationOp 饱和度调整测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("SaturationOp 饱和度调整测试")
class SaturationOpTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = PixelOp.getPixels(testImage);
        // Create a colorful image
        pixels[0] = PixelOp.argb(255, 255, 0, 0);
        pixels[1] = PixelOp.argb(255, 0, 255, 0);
        pixels[2] = PixelOp.argb(255, 0, 0, 255);
        pixels[3] = PixelOp.argb(255, 255, 255, 0);
        pixels[4] = PixelOp.argb(255, 128, 64, 192);
        pixels[5] = PixelOp.argb(255, 200, 100, 50);
        pixels[6] = PixelOp.argb(255, 50, 150, 250);
        pixels[7] = PixelOp.argb(255, 100, 200, 100);
        pixels[8] = PixelOp.argb(255, 128, 128, 128);
        pixels[9] = PixelOp.argb(255, 255, 128, 64);
        pixels[10] = PixelOp.argb(255, 64, 128, 255);
        pixels[11] = PixelOp.argb(255, 200, 200, 50);
        pixels[12] = PixelOp.argb(255, 10, 10, 10);
        pixels[13] = PixelOp.argb(255, 245, 245, 245);
        pixels[14] = PixelOp.argb(255, 180, 90, 45);
        pixels[15] = PixelOp.argb(255, 30, 60, 90);
    }

    @Nested
    @DisplayName("正常饱和度调整测试")
    class NormalTests {

        @Test
        @DisplayName("factor=1.0 输出近似不变")
        void factor1Unchanged() {
            BufferedImage result = SaturationOp.apply(testImage, 1.0);
            int[] src = PixelOp.getPixels(testImage);
            int[] dst = PixelOp.getPixels(result);

            for (int i = 0; i < src.length; i++) {
                // HSV roundtrip may introduce small rounding errors
                assertThat(Math.abs(PixelOp.red(src[i]) - PixelOp.red(dst[i])))
                        .isLessThanOrEqualTo(2);
                assertThat(Math.abs(PixelOp.green(src[i]) - PixelOp.green(dst[i])))
                        .isLessThanOrEqualTo(2);
                assertThat(Math.abs(PixelOp.blue(src[i]) - PixelOp.blue(dst[i])))
                        .isLessThanOrEqualTo(2);
            }
        }

        @Test
        @DisplayName("factor=0.0 产生灰度图像")
        void factor0ProducesGrayscale() {
            BufferedImage result = SaturationOp.apply(testImage, 0.0);
            int[] dst = PixelOp.getPixels(result);

            for (int i = 0; i < dst.length; i++) {
                int r = PixelOp.red(dst[i]);
                int g = PixelOp.green(dst[i]);
                int b = PixelOp.blue(dst[i]);
                // In a desaturated image, R=G=B (or very close due to HSV rounding)
                assertThat(Math.abs(r - g))
                        .as("Pixel %d: R=%d G=%d should be equal", i, r, g)
                        .isLessThanOrEqualTo(1);
                assertThat(Math.abs(g - b))
                        .as("Pixel %d: G=%d B=%d should be equal", i, g, b)
                        .isLessThanOrEqualTo(1);
            }
        }
    }

    @Nested
    @DisplayName("参数校验测试")
    class ValidationTests {

        @Test
        @DisplayName("factor<0 抛出异常")
        void negativeFactorThrows() {
            assertThatThrownBy(() -> SaturationOp.apply(testImage, -0.5))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("null 图像抛出异常")
        void nullImageThrows() {
            assertThatThrownBy(() -> SaturationOp.apply(null, 1.0))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("NaN factor 抛出异常")
        void nanFactorThrows() {
            assertThatThrownBy(() -> SaturationOp.apply(testImage, Double.NaN))
                    .isInstanceOf(ImageOperationException.class);
        }
    }
}
