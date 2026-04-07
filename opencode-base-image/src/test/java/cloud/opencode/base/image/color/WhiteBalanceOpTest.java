package cloud.opencode.base.image.color;

import cloud.opencode.base.image.kernel.PixelOp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * WhiteBalanceOp 白平衡测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("WhiteBalanceOp 白平衡测试")
class WhiteBalanceOpTest {

    @Nested
    @DisplayName("正常白平衡测试")
    class NormalTests {

        @Test
        @DisplayName("均匀灰色图像白平衡后不变")
        void uniformGrayUnchanged() {
            BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(img);
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = PixelOp.argb(255, 128, 128, 128);
            }

            BufferedImage result = WhiteBalanceOp.apply(img);
            int[] dst = PixelOp.getPixels(result);

            for (int i = 0; i < dst.length; i++) {
                assertThat(PixelOp.red(dst[i])).isEqualTo(128);
                assertThat(PixelOp.green(dst[i])).isEqualTo(128);
                assertThat(PixelOp.blue(dst[i])).isEqualTo(128);
            }
        }

        @Test
        @DisplayName("白平衡后通道均值近似相等")
        void outputMeansRoughlyEqual() {
            BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = PixelOp.getPixels(img);
            // Create an image with unbalanced channels
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = PixelOp.argb(255, 200, 100, 50);
            }

            BufferedImage result = WhiteBalanceOp.apply(img);
            int[] dst = PixelOp.getPixels(result);

            long sumR = 0, sumG = 0, sumB = 0;
            for (int px : dst) {
                sumR += PixelOp.red(px);
                sumG += PixelOp.green(px);
                sumB += PixelOp.blue(px);
            }
            double meanR = (double) sumR / dst.length;
            double meanG = (double) sumG / dst.length;
            double meanB = (double) sumB / dst.length;

            // After white balance, channel means should be approximately equal
            assertThat(Math.abs(meanR - meanG)).isLessThan(2.0);
            assertThat(Math.abs(meanG - meanB)).isLessThan(2.0);
            assertThat(Math.abs(meanR - meanB)).isLessThan(2.0);
        }
    }

    @Nested
    @DisplayName("参数校验测试")
    class ValidationTests {

        @Test
        @DisplayName("null 图像抛出异常")
        void nullImageThrows() {
            assertThatThrownBy(() -> WhiteBalanceOp.apply(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
