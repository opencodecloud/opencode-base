package cloud.opencode.base.image.internal;

import cloud.opencode.base.image.Position;
import cloud.opencode.base.image.watermark.ImageWatermark;
import cloud.opencode.base.image.watermark.TextWatermark;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.*;

/**
 * WatermarkOp 操作测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("WatermarkOp 操作测试")
class WatermarkOpTest {

    private BufferedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        // 填充白色背景
        Graphics2D g = testImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 800, 600);
        g.dispose();
    }

    @Nested
    @DisplayName("文字水印测试")
    class TextWatermarkTests {

        @Test
        @DisplayName("应用文字水印")
        void testApplyTextWatermark() {
            TextWatermark watermark = TextWatermark.of("Copyright 2024");
            BufferedImage result = WatermarkOp.apply(testImage, watermark);

            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(800);
            assertThat(result.getHeight()).isEqualTo(600);
        }

        @Test
        @DisplayName("不同位置的文字水印")
        void testTextWatermarkPositions() {
            for (Position pos : Position.values()) {
                TextWatermark watermark = TextWatermark.of("Test", pos);
                BufferedImage result = WatermarkOp.apply(testImage, watermark);

                assertThat(result).isNotNull();
            }
        }

        @Test
        @DisplayName("自定义字体水印")
        void testCustomFontWatermark() {
            TextWatermark watermark = TextWatermark.builder()
                .text("Custom Font")
                .font("Arial", Font.BOLD, 48)
                .color(Color.RED)
                .opacity(0.5f)
                .build();

            BufferedImage result = WatermarkOp.apply(testImage, watermark);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("图片水印测试")
    class ImageWatermarkTests {

        @Test
        @DisplayName("应用图片水印")
        void testApplyImageWatermark() {
            BufferedImage wmImage = new BufferedImage(100, 50, BufferedImage.TYPE_INT_ARGB);
            ImageWatermark watermark = ImageWatermark.of(wmImage);

            BufferedImage result = WatermarkOp.apply(testImage, watermark);

            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(800);
        }

        @Test
        @DisplayName("不同位置的图片水印")
        void testImageWatermarkPositions() {
            BufferedImage wmImage = new BufferedImage(100, 50, BufferedImage.TYPE_INT_ARGB);

            for (Position pos : Position.values()) {
                ImageWatermark watermark = ImageWatermark.of(wmImage, pos);
                BufferedImage result = WatermarkOp.apply(testImage, watermark);

                assertThat(result).isNotNull();
            }
        }

        @Test
        @DisplayName("透明图片水印")
        void testTransparentImageWatermark() {
            BufferedImage wmImage = new BufferedImage(100, 50, BufferedImage.TYPE_INT_ARGB);
            ImageWatermark watermark = ImageWatermark.of(wmImage, Position.CENTER, 0.5f);

            BufferedImage result = WatermarkOp.apply(testImage, watermark);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("平铺文字水印测试")
    class TiledTextWatermarkTests {

        @Test
        @DisplayName("应用平铺文字水印")
        void testApplyTiledTextWatermark() {
            TextWatermark watermark = TextWatermark.of("SAMPLE");
            BufferedImage result = WatermarkOp.applyTiled(testImage, watermark, 100, 50);

            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(800);
        }

        @Test
        @DisplayName("密集平铺水印")
        void testDenseTiledWatermark() {
            TextWatermark watermark = TextWatermark.of("X");
            BufferedImage result = WatermarkOp.applyTiled(testImage, watermark, 20, 20);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("平铺图片水印测试")
    class TiledImageWatermarkTests {

        @Test
        @DisplayName("应用平铺图片水印")
        void testApplyTiledImageWatermark() {
            BufferedImage wmImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
            ImageWatermark watermark = ImageWatermark.of(wmImage);

            BufferedImage result = WatermarkOp.applyTiled(testImage, watermark, 30, 30);

            assertThat(result).isNotNull();
            assertThat(result.getWidth()).isEqualTo(800);
        }
    }

    @Nested
    @DisplayName("图片类型测试")
    class ImageTypeTests {

        @Test
        @DisplayName("保持RGB类型")
        void testPreservesRGBType() {
            TextWatermark watermark = TextWatermark.of("Test");
            BufferedImage result = WatermarkOp.apply(testImage, watermark);

            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);
        }

        @Test
        @DisplayName("保持ARGB类型")
        void testPreservesARGBType() {
            BufferedImage argb = new BufferedImage(200, 150, BufferedImage.TYPE_INT_ARGB);
            TextWatermark watermark = TextWatermark.of("Test");

            BufferedImage result = WatermarkOp.apply(argb, watermark);

            assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
        }
    }
}
