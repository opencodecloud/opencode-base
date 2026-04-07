package cloud.opencode.base.image.color;

import cloud.opencode.base.image.exception.ImageOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * BrightnessContrastOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.3
 */
@DisplayName("BrightnessContrastOp 测试")
class BrightnessContrastOpTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        // Fill with mid-gray (128, 128, 128)
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                testImage.setRGB(x, y, 0xFF808080);
            }
        }
    }

    @Nested
    @DisplayName("brightness 方法测试")
    class BrightnessTests {

        @Test
        @DisplayName("亮度因子1.0不改变图像")
        void testBrightnessNoChange() {
            BufferedImage result = BrightnessContrastOp.brightness(testImage, 1.0);

            assertThat(result.getWidth()).isEqualTo(100);
            assertThat(result.getHeight()).isEqualTo(100);
            int argb = result.getRGB(50, 50);
            int r = (argb >> 16) & 0xFF;
            assertThat(r).isEqualTo(0x80);
        }

        @Test
        @DisplayName("亮度因子2.0使图像变亮")
        void testBrightnessIncrease() {
            BufferedImage result = BrightnessContrastOp.brightness(testImage, 2.0);

            int argb = result.getRGB(50, 50);
            int r = (argb >> 16) & 0xFF;
            // 0x80 * 2 = 0x100, clamped to 255
            assertThat(r).isEqualTo(255);
        }

        @Test
        @DisplayName("亮度因子0.5使图像变暗")
        void testBrightnessDecrease() {
            BufferedImage result = BrightnessContrastOp.brightness(testImage, 0.5);

            int argb = result.getRGB(50, 50);
            int r = (argb >> 16) & 0xFF;
            // 0x80 * 0.5 = 0x40
            assertThat(r).isEqualTo(0x40);
        }

        @Test
        @DisplayName("亮度因子0不改变Alpha通道")
        void testBrightnessPreservesAlpha() {
            testImage.setRGB(0, 0, 0x80808080); // semi-transparent
            BufferedImage result = BrightnessContrastOp.brightness(testImage, 1.0);

            int argb = result.getRGB(0, 0);
            int alpha = (argb >> 24) & 0xFF;
            assertThat(alpha).isEqualTo(0x80);
        }

        @Test
        @DisplayName("负数因子抛出异常")
        void testBrightnessNegativeFactorThrows() {
            assertThatThrownBy(() -> BrightnessContrastOp.brightness(testImage, -1.0))
                    .isInstanceOf(ImageOperationException.class);
        }
    }

    @Nested
    @DisplayName("contrast 方法测试")
    class ContrastTests {

        @Test
        @DisplayName("对比度因子1.0不改变图像")
        void testContrastNoChange() {
            BufferedImage result = BrightnessContrastOp.contrast(testImage, 1.0);

            int argb = result.getRGB(50, 50);
            int r = (argb >> 16) & 0xFF;
            assertThat(r).isEqualTo(0x80);
        }

        @Test
        @DisplayName("对比度因子2.0增加对比度")
        void testContrastIncrease() {
            // Set pixel to 200 (above mid 128)
            testImage.setRGB(10, 10, 0xFFC8C8C8);
            BufferedImage result = BrightnessContrastOp.contrast(testImage, 2.0);

            int argb = result.getRGB(10, 10);
            int r = (argb >> 16) & 0xFF;
            // (200 - 128) * 2 + 128 = 272, clamped to 255
            assertThat(r).isEqualTo(255);
        }

        @Test
        @DisplayName("对比度因子0.0使图像变灰")
        void testContrastZero() {
            // Set pixel to 200
            testImage.setRGB(10, 10, 0xFFC8C8C8);
            BufferedImage result = BrightnessContrastOp.contrast(testImage, 0.0);

            int argb = result.getRGB(10, 10);
            int r = (argb >> 16) & 0xFF;
            // (200 - 128) * 0 + 128 = 128
            assertThat(r).isEqualTo(128);
        }

        @Test
        @DisplayName("负数因子抛出异常")
        void testContrastNegativeFactorThrows() {
            assertThatThrownBy(() -> BrightnessContrastOp.contrast(testImage, -0.5))
                    .isInstanceOf(ImageOperationException.class);
        }
    }
}
