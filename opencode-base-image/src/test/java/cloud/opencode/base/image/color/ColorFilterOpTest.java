package cloud.opencode.base.image.color;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * ColorFilterOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.3
 */
@DisplayName("ColorFilterOp 测试")
class ColorFilterOpTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        // Fill with a known color: R=100, G=150, B=200, A=255
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                testImage.setRGB(x, y, 0xFF6496C8);
            }
        }
    }

    @Nested
    @DisplayName("sepia 方法测试")
    class SepiaTests {

        @Test
        @DisplayName("sepia 返回相同尺寸图像")
        void testSepiaPreservesDimensions() {
            BufferedImage result = ColorFilterOp.sepia(testImage);

            assertThat(result.getWidth()).isEqualTo(10);
            assertThat(result.getHeight()).isEqualTo(10);
        }

        @Test
        @DisplayName("sepia 输出像素为非负值")
        void testSepiaOutputValid() {
            BufferedImage result = ColorFilterOp.sepia(testImage);

            int argb = result.getRGB(5, 5);
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;
            assertThat(r).isBetween(0, 255);
            assertThat(g).isBetween(0, 255);
            assertThat(b).isBetween(0, 255);
        }

        @Test
        @DisplayName("sepia 输出 R >= G >= B（暖色调）")
        void testSepiaWarmTone() {
            BufferedImage result = ColorFilterOp.sepia(testImage);

            int argb = result.getRGB(5, 5);
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;
            assertThat(r).isGreaterThanOrEqualTo(g);
            assertThat(g).isGreaterThanOrEqualTo(b);
        }

        @Test
        @DisplayName("sepia 保留 alpha 通道")
        void testSepiaPreservesAlpha() {
            testImage.setRGB(0, 0, 0x806496C8); // alpha=128
            BufferedImage result = ColorFilterOp.sepia(testImage);

            int argb = result.getRGB(0, 0);
            int alpha = (argb >> 24) & 0xFF;
            assertThat(alpha).isEqualTo(0x80);
        }

        @Test
        @DisplayName("null 图像抛出 NullPointerException")
        void testSepiaNull() {
            assertThatNullPointerException().isThrownBy(() -> ColorFilterOp.sepia(null));
        }
    }

    @Nested
    @DisplayName("invert 方法测试")
    class InvertTests {

        @Test
        @DisplayName("invert 返回相同尺寸图像")
        void testInvertPreservesDimensions() {
            BufferedImage result = ColorFilterOp.invert(testImage);

            assertThat(result.getWidth()).isEqualTo(10);
            assertThat(result.getHeight()).isEqualTo(10);
        }

        @Test
        @DisplayName("invert 反转 RGB 通道")
        void testInvertRGB() {
            // Input: R=0x64, G=0x96, B=0xC8
            BufferedImage result = ColorFilterOp.invert(testImage);

            int argb = result.getRGB(5, 5);
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;
            assertThat(r).isEqualTo(255 - 0x64);
            assertThat(g).isEqualTo(255 - 0x96);
            assertThat(b).isEqualTo(255 - 0xC8);
        }

        @Test
        @DisplayName("invert 保留 alpha 通道")
        void testInvertPreservesAlpha() {
            testImage.setRGB(0, 0, 0x806496C8); // alpha=128
            BufferedImage result = ColorFilterOp.invert(testImage);

            int argb = result.getRGB(0, 0);
            int alpha = (argb >> 24) & 0xFF;
            assertThat(alpha).isEqualTo(0x80);
        }

        @Test
        @DisplayName("double invert 恢复原图")
        void testDoubleInvertRestoresOriginal() {
            BufferedImage once = ColorFilterOp.invert(testImage);
            BufferedImage twice = ColorFilterOp.invert(once);

            int original = testImage.getRGB(5, 5);
            int restored = twice.getRGB(5, 5);
            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("null 图像抛出 NullPointerException")
        void testInvertNull() {
            assertThatNullPointerException().isThrownBy(() -> ColorFilterOp.invert(null));
        }
    }
}
