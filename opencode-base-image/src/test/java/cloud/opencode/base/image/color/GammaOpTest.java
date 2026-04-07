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
 * GammaOp 伽马校正测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("GammaOp 伽马校正测试")
class GammaOpTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = PixelOp.getPixels(testImage);
        for (int i = 0; i < pixels.length; i++) {
            int v = i * 16; // 0, 16, 32, ..., 240
            pixels[i] = PixelOp.argb(255, v, v, v);
        }
    }

    @Nested
    @DisplayName("正常伽马校正测试")
    class NormalGammaTests {

        @Test
        @DisplayName("gamma=1.0 输出等于输入")
        void gamma1LeavesUnchanged() {
            BufferedImage result = GammaOp.apply(testImage, 1.0);
            int[] src = PixelOp.getPixels(testImage);
            int[] dst = PixelOp.getPixels(result);

            for (int i = 0; i < src.length; i++) {
                assertThat(PixelOp.red(dst[i])).isEqualTo(PixelOp.red(src[i]));
                assertThat(PixelOp.green(dst[i])).isEqualTo(PixelOp.green(src[i]));
                assertThat(PixelOp.blue(dst[i])).isEqualTo(PixelOp.blue(src[i]));
            }
        }

        @Test
        @DisplayName("gamma=2.2 图像变亮（LUT 使用 1/gamma 指数）")
        void gamma2Point2MakesBrighter() {
            BufferedImage result = GammaOp.apply(testImage, 2.2);
            int[] src = PixelOp.getPixels(testImage);
            int[] dst = PixelOp.getPixels(result);

            // gammaLut uses exponent 1/gamma, so gamma > 1 brightens mid-tones
            long sumSrc = 0, sumDst = 0;
            for (int i = 0; i < src.length; i++) {
                sumSrc += PixelOp.red(src[i]);
                sumDst += PixelOp.red(dst[i]);
            }
            // Overall average should be brighter (higher)
            assertThat(sumDst).isGreaterThan(sumSrc);
        }
    }

    @Nested
    @DisplayName("参数校验测试")
    class ValidationTests {

        @Test
        @DisplayName("gamma<=0 抛出异常")
        void gammaZeroThrows() {
            assertThatThrownBy(() -> GammaOp.apply(testImage, 0.0))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("gamma<0 抛出异常")
        void gammaNegativeThrows() {
            assertThatThrownBy(() -> GammaOp.apply(testImage, -1.0))
                    .isInstanceOf(ImageOperationException.class);
        }

        @Test
        @DisplayName("null 图像抛出异常")
        void nullImageThrows() {
            assertThatThrownBy(() -> GammaOp.apply(null, 1.0))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("NaN gamma 抛出异常")
        void nanGammaThrows() {
            assertThatThrownBy(() -> GammaOp.apply(testImage, Double.NaN))
                    .isInstanceOf(ImageOperationException.class);
        }
    }
}
