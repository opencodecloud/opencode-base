package cloud.opencode.base.image.internal;

import cloud.opencode.base.image.ImageFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * ConvertOp 操作测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("ConvertOp 操作测试")
class ConvertOpTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(200, 150, BufferedImage.TYPE_INT_RGB);
        // 填充一些颜色
        for (int y = 0; y < 150; y++) {
            for (int x = 0; x < 200; x++) {
                int r = x % 256;
                int g = y % 256;
                int b = (x + y) % 256;
                testImage.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
    }

    @Nested
    @DisplayName("convert方法测试")
    class ConvertTests {

        @Test
        @DisplayName("JPEG到PNG转换")
        void testConvertJpegToPng() {
            BufferedImage result = ConvertOp.convert(testImage, ImageFormat.JPEG, ImageFormat.PNG);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("PNG到JPEG转换")
        void testConvertPngToJpeg() {
            BufferedImage argb = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            BufferedImage result = ConvertOp.convert(argb, ImageFormat.PNG, ImageFormat.JPEG);

            // JPEG不支持透明，应该移除alpha
            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);
        }

        @Test
        @DisplayName("相同格式转换")
        void testConvertSameFormat() {
            BufferedImage result = ConvertOp.convert(testImage, ImageFormat.JPEG, ImageFormat.JPEG);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("grayscale方法测试")
    class GrayscaleTests {

        @Test
        @DisplayName("转换为灰度图")
        void testGrayscale() {
            BufferedImage result = ConvertOp.grayscale(testImage);

            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_BYTE_GRAY);
        }

        @Test
        @DisplayName("灰度图尺寸不变")
        void testGrayscaleDimensions() {
            BufferedImage result = ConvertOp.grayscale(testImage);

            assertThat(result.getWidth()).isEqualTo(200);
            assertThat(result.getHeight()).isEqualTo(150);
        }
    }

    @Nested
    @DisplayName("sepia方法测试")
    class SepiaTests {

        @Test
        @DisplayName("转换为棕褐色调")
        void testSepia() {
            BufferedImage result = ConvertOp.sepia(testImage);

            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(200);
        }

        @Test
        @DisplayName("棕褐色调产生暖色")
        void testSepiaProducesWarmColors() {
            BufferedImage result = ConvertOp.sepia(testImage);

            // 检查一个像素的棕褐色调特征
            int rgb = result.getRGB(100, 75);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            // 棕褐色调：红色 > 绿色 > 蓝色
            assertThat(r).isGreaterThanOrEqualTo(g);
            assertThat(g).isGreaterThanOrEqualTo(b);
        }
    }

    @Nested
    @DisplayName("invert方法测试")
    class InvertTests {

        @Test
        @DisplayName("反转颜色")
        void testInvert() {
            BufferedImage result = ConvertOp.invert(testImage);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("反转后再反转恢复原色")
        void testDoubleInvert() {
            BufferedImage inverted = ConvertOp.invert(testImage);
            BufferedImage restored = ConvertOp.invert(inverted);

            // 检查一些像素是否恢复
            int original = testImage.getRGB(100, 75);
            int finalRgb = restored.getRGB(100, 75);

            // RGB值应该相同（忽略alpha）
            assertThat(original & 0xFFFFFF).isEqualTo(finalRgb & 0xFFFFFF);
        }
    }

    @Nested
    @DisplayName("adjustBrightness方法测试")
    class AdjustBrightnessTests {

        @Test
        @DisplayName("增加亮度")
        void testIncreaseBrightness() {
            BufferedImage result = ConvertOp.adjustBrightness(testImage, 0.5f);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("降低亮度")
        void testDecreaseBrightness() {
            BufferedImage result = ConvertOp.adjustBrightness(testImage, -0.5f);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("亮度为0不变")
        void testZeroBrightness() {
            BufferedImage result = ConvertOp.adjustBrightness(testImage, 0f);

            // 应该与原图相同
            int original = testImage.getRGB(100, 75);
            int adjusted = result.getRGB(100, 75);

            assertThat(original & 0xFFFFFF).isEqualTo(adjusted & 0xFFFFFF);
        }
    }

    @Nested
    @DisplayName("adjustContrast方法测试")
    class AdjustContrastTests {

        @Test
        @DisplayName("增加对比度")
        void testIncreaseContrast() {
            BufferedImage result = ConvertOp.adjustContrast(testImage, 1.5f);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("降低对比度")
        void testDecreaseContrast() {
            BufferedImage result = ConvertOp.adjustContrast(testImage, 0.5f);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("对比度为1不变")
        void testNormalContrast() {
            BufferedImage result = ConvertOp.adjustContrast(testImage, 1.0f);

            // 应该与原图相同
            int original = testImage.getRGB(100, 75);
            int adjusted = result.getRGB(100, 75);

            assertThat(original & 0xFFFFFF).isEqualTo(adjusted & 0xFFFFFF);
        }
    }

    @Nested
    @DisplayName("removeAlpha方法测试")
    class RemoveAlphaTests {

        @Test
        @DisplayName("移除ARGB的alpha通道")
        void testRemoveAlpha() {
            BufferedImage argb = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            BufferedImage result = ConvertOp.removeAlpha(argb);

            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);
        }

        @Test
        @DisplayName("RGB图片不变")
        void testRemoveAlphaFromRGB() {
            BufferedImage result = ConvertOp.removeAlpha(testImage);

            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);
        }
    }

    @Nested
    @DisplayName("addAlpha方法测试")
    class AddAlphaTests {

        @Test
        @DisplayName("添加alpha通道")
        void testAddAlpha() {
            BufferedImage result = ConvertOp.addAlpha(testImage);

            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
        }
    }

    @Nested
    @DisplayName("hasTransparency方法测试")
    class HasTransparencyTests {

        @Test
        @DisplayName("RGB图片无透明度")
        void testRGBNoTransparency() {
            assertThat(ConvertOp.hasTransparency(testImage)).isFalse();
        }

        @Test
        @DisplayName("ARGB图片有透明度")
        void testARGBHasTransparency() {
            BufferedImage argb = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

            assertThat(ConvertOp.hasTransparency(argb)).isTrue();
        }
    }
}
