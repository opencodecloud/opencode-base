package cloud.opencode.base.image.compare;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * ImageCompare 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("ImageCompare 图像比较测试")
class ImageCompareTest {

    // --- Helper methods ---

    private static BufferedImage solidImage(Color color, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return img;
    }

    private static BufferedImage redImage() {
        return solidImage(Color.RED, 100, 100);
    }

    private static BufferedImage blueImage() {
        return solidImage(Color.BLUE, 100, 100);
    }

    private static BufferedImage smallRedImage() {
        return solidImage(Color.RED, 50, 50);
    }

    @Nested
    @DisplayName("diff 像素级差异")
    class DiffTests {

        @Test
        @DisplayName("相同图像 diffPercent 约为 0.0")
        void sameImageZeroDiff() {
            BufferedImage img = redImage();
            ImageCompare.DiffResult result = ImageCompare.diff(img, img);
            assertThat(result.diffPercent()).isCloseTo(0.0, within(0.001));
        }

        @Test
        @DisplayName("完全不同图像 diffPercent > 0.5")
        void differentImageHighDiff() {
            ImageCompare.DiffResult result = ImageCompare.diff(redImage(), blueImage());
            assertThat(result.diffPercent()).isGreaterThan(0.5);
        }

        @Test
        @DisplayName("diffImage 尺寸与参考图一致")
        void diffImageDimensionsMatchReference() {
            BufferedImage a = redImage();
            BufferedImage b = blueImage();
            ImageCompare.DiffResult result = ImageCompare.diff(a, b);
            assertThat(result.diffImage().getWidth()).isEqualTo(a.getWidth());
            assertThat(result.diffImage().getHeight()).isEqualTo(a.getHeight());
        }

        @Test
        @DisplayName("null 抛出异常")
        void nullThrows() {
            assertThatThrownBy(() -> ImageCompare.diff(null, redImage()))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> ImageCompare.diff(redImage(), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("ssim 结构相似性")
    class SsimTests {

        @Test
        @DisplayName("相同图像 SSIM > 0.99")
        void sameImageHighSsim() {
            BufferedImage img = redImage();
            assertThat(ImageCompare.ssim(img, img)).isGreaterThan(0.99);
        }

        @Test
        @DisplayName("不同图像 SSIM < 0.8")
        void differentImageLowSsim() {
            assertThat(ImageCompare.ssim(redImage(), blueImage())).isLessThan(0.8);
        }

        @Test
        @DisplayName("null 抛出异常")
        void nullThrows() {
            assertThatThrownBy(() -> ImageCompare.ssim(null, redImage()))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("mse 均方误差")
    class MseTests {

        @Test
        @DisplayName("相同图像 MSE 为 0.0")
        void sameImageZeroMse() {
            BufferedImage img = redImage();
            assertThat(ImageCompare.mse(img, img)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("不同图像 MSE > 0")
        void differentImagePositiveMse() {
            assertThat(ImageCompare.mse(redImage(), blueImage())).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("null 抛出异常")
        void nullThrows() {
            assertThatThrownBy(() -> ImageCompare.mse(null, redImage()))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("psnr 峰值信噪比")
    class PsnrTests {

        @Test
        @DisplayName("相同图像 PSNR 为正无穷")
        void sameImageInfinityPsnr() {
            BufferedImage img = redImage();
            assertThat(ImageCompare.psnr(img, img)).isEqualTo(Double.POSITIVE_INFINITY);
        }

        @Test
        @DisplayName("不同图像 PSNR 为有限正数")
        void differentImageFinitePsnr() {
            double psnr = ImageCompare.psnr(redImage(), blueImage());
            assertThat(psnr).isFinite().isPositive();
        }

        @Test
        @DisplayName("null 抛出异常")
        void nullThrows() {
            assertThatThrownBy(() -> ImageCompare.psnr(null, redImage()))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("不同尺寸处理")
    class DifferentSizeTests {

        @Test
        @DisplayName("diff 能处理不同尺寸图像")
        void diffHandlesDifferentSizes() {
            ImageCompare.DiffResult result = ImageCompare.diff(redImage(), smallRedImage());
            assertThat(result.diffImage().getWidth()).isEqualTo(100);
            assertThat(result.diffImage().getHeight()).isEqualTo(100);
            // Same color, resized — should be very similar
            assertThat(result.diffPercent()).isCloseTo(0.0, within(0.01));
        }

        @Test
        @DisplayName("ssim 能处理不同尺寸图像")
        void ssimHandlesDifferentSizes() {
            double ssim = ImageCompare.ssim(redImage(), smallRedImage());
            assertThat(ssim).isGreaterThan(0.99);
        }

        @Test
        @DisplayName("mse 能处理不同尺寸图像")
        void mseHandlesDifferentSizes() {
            double mse = ImageCompare.mse(redImage(), smallRedImage());
            assertThat(mse).isCloseTo(0.0, within(1.0));
        }

        @Test
        @DisplayName("psnr 能处理不同尺寸图像")
        void psnrHandlesDifferentSizes() {
            double psnr = ImageCompare.psnr(redImage(), smallRedImage());
            // Same color content, so either infinity or very high
            assertThat(psnr).isGreaterThan(30.0);
        }
    }
}
