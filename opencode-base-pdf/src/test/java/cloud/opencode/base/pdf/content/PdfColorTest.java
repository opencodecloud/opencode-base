package cloud.opencode.base.pdf.content;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfColor 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PdfColor 测试")
class PdfColorTest {

    @Nested
    @DisplayName("预定义颜色测试")
    class PredefinedColorsTests {

        @Test
        @DisplayName("BLACK 颜色正确")
        void testBlack() {
            assertThat(PdfColor.BLACK.getRed()).isEqualTo(0f);
            assertThat(PdfColor.BLACK.getGreen()).isEqualTo(0f);
            assertThat(PdfColor.BLACK.getBlue()).isEqualTo(0f);
        }

        @Test
        @DisplayName("WHITE 颜色正确")
        void testWhite() {
            assertThat(PdfColor.WHITE.getRed()).isEqualTo(1f);
            assertThat(PdfColor.WHITE.getGreen()).isEqualTo(1f);
            assertThat(PdfColor.WHITE.getBlue()).isEqualTo(1f);
        }

        @Test
        @DisplayName("RED 颜色正确")
        void testRed() {
            assertThat(PdfColor.RED.getRed()).isEqualTo(1f);
            assertThat(PdfColor.RED.getGreen()).isEqualTo(0f);
            assertThat(PdfColor.RED.getBlue()).isEqualTo(0f);
        }

        @Test
        @DisplayName("GREEN 颜色正确")
        void testGreen() {
            assertThat(PdfColor.GREEN.getRed()).isEqualTo(0f);
            assertThat(PdfColor.GREEN.getGreen()).isEqualTo(1f);
            assertThat(PdfColor.GREEN.getBlue()).isEqualTo(0f);
        }

        @Test
        @DisplayName("BLUE 颜色正确")
        void testBlue() {
            assertThat(PdfColor.BLUE.getRed()).isEqualTo(0f);
            assertThat(PdfColor.BLUE.getGreen()).isEqualTo(0f);
            assertThat(PdfColor.BLUE.getBlue()).isEqualTo(1f);
        }

        @Test
        @DisplayName("YELLOW 颜色正确")
        void testYellow() {
            assertThat(PdfColor.YELLOW.getRed()).isEqualTo(1f);
            assertThat(PdfColor.YELLOW.getGreen()).isEqualTo(1f);
            assertThat(PdfColor.YELLOW.getBlue()).isEqualTo(0f);
        }

        @Test
        @DisplayName("CYAN 颜色正确")
        void testCyan() {
            assertThat(PdfColor.CYAN.getRed()).isEqualTo(0f);
            assertThat(PdfColor.CYAN.getGreen()).isEqualTo(1f);
            assertThat(PdfColor.CYAN.getBlue()).isEqualTo(1f);
        }

        @Test
        @DisplayName("MAGENTA 颜色正确")
        void testMagenta() {
            assertThat(PdfColor.MAGENTA.getRed()).isEqualTo(1f);
            assertThat(PdfColor.MAGENTA.getGreen()).isEqualTo(0f);
            assertThat(PdfColor.MAGENTA.getBlue()).isEqualTo(1f);
        }

        @Test
        @DisplayName("GRAY 颜色正确")
        void testGray() {
            float expected = 128f / 255f;
            assertThat(PdfColor.GRAY.getRed()).isCloseTo(expected, within(0.01f));
            assertThat(PdfColor.GRAY.getGreen()).isCloseTo(expected, within(0.01f));
            assertThat(PdfColor.GRAY.getBlue()).isCloseTo(expected, within(0.01f));
        }

        @Test
        @DisplayName("LIGHT_GRAY 颜色正确")
        void testLightGray() {
            float expected = 192f / 255f;
            assertThat(PdfColor.LIGHT_GRAY.getRed()).isCloseTo(expected, within(0.01f));
            assertThat(PdfColor.LIGHT_GRAY.getGreen()).isCloseTo(expected, within(0.01f));
            assertThat(PdfColor.LIGHT_GRAY.getBlue()).isCloseTo(expected, within(0.01f));
        }

        @Test
        @DisplayName("DARK_GRAY 颜色正确")
        void testDarkGray() {
            float expected = 64f / 255f;
            assertThat(PdfColor.DARK_GRAY.getRed()).isCloseTo(expected, within(0.01f));
            assertThat(PdfColor.DARK_GRAY.getGreen()).isCloseTo(expected, within(0.01f));
            assertThat(PdfColor.DARK_GRAY.getBlue()).isCloseTo(expected, within(0.01f));
        }

        @Test
        @DisplayName("ORANGE 颜色正确")
        void testOrange() {
            assertThat(PdfColor.ORANGE.getRed()).isEqualTo(1f);
            assertThat(PdfColor.ORANGE.getGreen()).isCloseTo(200f / 255f, within(0.01f));
            assertThat(PdfColor.ORANGE.getBlue()).isEqualTo(0f);
        }
    }

    @Nested
    @DisplayName("rgb(int,int,int) 工厂方法测试")
    class RgbIntFactoryTests {

        @Test
        @DisplayName("rgb 创建正确颜色")
        void testRgb() {
            PdfColor color = PdfColor.rgb(100, 150, 200);

            assertThat(color.getRed()).isCloseTo(100f / 255f, within(0.01f));
            assertThat(color.getGreen()).isCloseTo(150f / 255f, within(0.01f));
            assertThat(color.getBlue()).isCloseTo(200f / 255f, within(0.01f));
            assertThat(color.getAlpha()).isEqualTo(1f);
        }

        @Test
        @DisplayName("rgb 边界值 0")
        void testRgbMinValues() {
            PdfColor color = PdfColor.rgb(0, 0, 0);

            assertThat(color.getRed()).isEqualTo(0f);
            assertThat(color.getGreen()).isEqualTo(0f);
            assertThat(color.getBlue()).isEqualTo(0f);
        }

        @Test
        @DisplayName("rgb 边界值 255")
        void testRgbMaxValues() {
            PdfColor color = PdfColor.rgb(255, 255, 255);

            assertThat(color.getRed()).isEqualTo(1f);
            assertThat(color.getGreen()).isEqualTo(1f);
            assertThat(color.getBlue()).isEqualTo(1f);
        }

        @Test
        @DisplayName("rgb 默认 alpha 为 1")
        void testRgbDefaultAlpha() {
            PdfColor color = PdfColor.rgb(128, 128, 128);

            assertThat(color.getAlpha()).isEqualTo(1f);
        }
    }

    @Nested
    @DisplayName("rgb(float,float,float) 工厂方法测试")
    class RgbFloatFactoryTests {

        @Test
        @DisplayName("rgb 创建浮点颜色")
        void testRgbFloat() {
            PdfColor color = PdfColor.rgb(0.5f, 0.6f, 0.7f);

            assertThat(color.getRed()).isEqualTo(0.5f);
            assertThat(color.getGreen()).isEqualTo(0.6f);
            assertThat(color.getBlue()).isEqualTo(0.7f);
        }

        @Test
        @DisplayName("rgb 浮点边界值 0")
        void testRgbFloatMin() {
            PdfColor color = PdfColor.rgb(0f, 0f, 0f);

            assertThat(color.getRed()).isEqualTo(0f);
            assertThat(color.getGreen()).isEqualTo(0f);
            assertThat(color.getBlue()).isEqualTo(0f);
        }

        @Test
        @DisplayName("rgb 浮点边界值 1")
        void testRgbFloatMax() {
            PdfColor color = PdfColor.rgb(1f, 1f, 1f);

            assertThat(color.getRed()).isEqualTo(1f);
            assertThat(color.getGreen()).isEqualTo(1f);
            assertThat(color.getBlue()).isEqualTo(1f);
        }
    }

    @Nested
    @DisplayName("rgba 工厂方法测试")
    class RgbaFactoryTests {

        @Test
        @DisplayName("rgba 创建带透明度颜色")
        void testRgba() {
            PdfColor color = PdfColor.rgba(100, 150, 200, 0.5f);

            assertThat(color.getRed()).isCloseTo(100f / 255f, within(0.01f));
            assertThat(color.getGreen()).isCloseTo(150f / 255f, within(0.01f));
            assertThat(color.getBlue()).isCloseTo(200f / 255f, within(0.01f));
            assertThat(color.getAlpha()).isEqualTo(0.5f);
        }

        @Test
        @DisplayName("rgba 全透明")
        void testRgbaFullyTransparent() {
            PdfColor color = PdfColor.rgba(255, 0, 0, 0f);

            assertThat(color.getAlpha()).isEqualTo(0f);
        }

        @Test
        @DisplayName("rgba 完全不透明")
        void testRgbaFullyOpaque() {
            PdfColor color = PdfColor.rgba(255, 0, 0, 1f);

            assertThat(color.getAlpha()).isEqualTo(1f);
        }

        @Test
        @DisplayName("rgba 半透明")
        void testRgbaHalfTransparent() {
            PdfColor color = PdfColor.rgba(0, 0, 0, 0.5f);

            assertThat(color.getAlpha()).isEqualTo(0.5f);
        }
    }

    @Nested
    @DisplayName("gray 工厂方法测试")
    class GrayFactoryTests {

        @Test
        @DisplayName("gray(int) 创建灰度颜色")
        void testGrayInt() {
            PdfColor color = PdfColor.gray(128);

            float expected = 128f / 255f;
            assertThat(color.getRed()).isCloseTo(expected, within(0.01f));
            assertThat(color.getGreen()).isCloseTo(expected, within(0.01f));
            assertThat(color.getBlue()).isCloseTo(expected, within(0.01f));
        }

        @Test
        @DisplayName("gray(int) 黑色")
        void testGrayBlack() {
            PdfColor color = PdfColor.gray(0);

            assertThat(color.getRed()).isEqualTo(0f);
            assertThat(color.getGreen()).isEqualTo(0f);
            assertThat(color.getBlue()).isEqualTo(0f);
        }

        @Test
        @DisplayName("gray(int) 白色")
        void testGrayWhite() {
            PdfColor color = PdfColor.gray(255);

            assertThat(color.getRed()).isEqualTo(1f);
            assertThat(color.getGreen()).isEqualTo(1f);
            assertThat(color.getBlue()).isEqualTo(1f);
        }

        @Test
        @DisplayName("gray(float) 创建灰度颜色")
        void testGrayFloat() {
            PdfColor color = PdfColor.gray(0.5f);

            assertThat(color.getRed()).isEqualTo(0.5f);
            assertThat(color.getGreen()).isEqualTo(0.5f);
            assertThat(color.getBlue()).isEqualTo(0.5f);
        }

        @Test
        @DisplayName("gray 是 GRAYSCALE 模型")
        void testGrayColorModel() {
            PdfColor color = PdfColor.gray(128);

            assertThat(color.getModel()).isEqualTo(PdfColor.ColorModel.GRAYSCALE);
        }
    }

    @Nested
    @DisplayName("hex 工厂方法测试")
    class HexFactoryTests {

        @Test
        @DisplayName("hex 解析带 # 的颜色码")
        void testHexWithHash() {
            PdfColor color = PdfColor.hex("#FF5500");

            assertThat(color.getRed()).isEqualTo(1f);
            assertThat(color.getGreen()).isCloseTo(85f / 255f, within(0.01f));
            assertThat(color.getBlue()).isEqualTo(0f);
        }

        @Test
        @DisplayName("hex 解析无 # 的颜色码")
        void testHexWithoutHash() {
            PdfColor color = PdfColor.hex("FF5500");

            assertThat(color.getRed()).isEqualTo(1f);
            assertThat(color.getGreen()).isCloseTo(85f / 255f, within(0.01f));
            assertThat(color.getBlue()).isEqualTo(0f);
        }

        @Test
        @DisplayName("hex 小写字母")
        void testHexLowercase() {
            PdfColor color = PdfColor.hex("#ff5500");

            assertThat(color.getRed()).isEqualTo(1f);
            assertThat(color.getGreen()).isCloseTo(85f / 255f, within(0.01f));
            assertThat(color.getBlue()).isEqualTo(0f);
        }

        @Test
        @DisplayName("hex 黑色")
        void testHexBlack() {
            PdfColor color = PdfColor.hex("#000000");

            assertThat(color.getRed()).isEqualTo(0f);
            assertThat(color.getGreen()).isEqualTo(0f);
            assertThat(color.getBlue()).isEqualTo(0f);
        }

        @Test
        @DisplayName("hex 白色")
        void testHexWhite() {
            PdfColor color = PdfColor.hex("#FFFFFF");

            assertThat(color.getRed()).isEqualTo(1f);
            assertThat(color.getGreen()).isEqualTo(1f);
            assertThat(color.getBlue()).isEqualTo(1f);
        }
    }

    @Nested
    @DisplayName("from(Color) 工厂方法测试")
    class FromColorFactoryTests {

        @Test
        @DisplayName("从 AWT Color 创建")
        void testFromAwtColor() {
            Color awtColor = new Color(100, 150, 200);
            PdfColor pdfColor = PdfColor.from(awtColor);

            assertThat(pdfColor.getRed()).isCloseTo(100f / 255f, within(0.01f));
            assertThat(pdfColor.getGreen()).isCloseTo(150f / 255f, within(0.01f));
            assertThat(pdfColor.getBlue()).isCloseTo(200f / 255f, within(0.01f));
        }

        @Test
        @DisplayName("从带透明度的 AWT Color 创建")
        void testFromAwtColorWithAlpha() {
            Color awtColor = new Color(100, 150, 200, 128);
            PdfColor pdfColor = PdfColor.from(awtColor);

            assertThat(pdfColor.getAlpha()).isCloseTo(128f / 255f, within(0.01f));
        }
    }

    @Nested
    @DisplayName("toAwtColor 方法测试")
    class ToAwtColorTests {

        @Test
        @DisplayName("转换为 AWT Color")
        void testToAwtColor() {
            PdfColor pdfColor = PdfColor.rgb(100, 150, 200);
            Color awtColor = pdfColor.toAwtColor();

            assertThat(awtColor.getRed()).isEqualTo(100);
            assertThat(awtColor.getGreen()).isEqualTo(150);
            assertThat(awtColor.getBlue()).isEqualTo(200);
        }

        @Test
        @DisplayName("转换保留透明度")
        void testToAwtColorWithAlpha() {
            PdfColor pdfColor = PdfColor.rgba(100, 150, 200, 0.5f);
            Color awtColor = pdfColor.toAwtColor();

            assertThat(awtColor.getAlpha()).isCloseTo(128, within(1));
        }

        @Test
        @DisplayName("黑色转换")
        void testToAwtColorBlack() {
            Color awtColor = PdfColor.BLACK.toAwtColor();

            assertThat(awtColor).isEqualTo(Color.BLACK);
        }

        @Test
        @DisplayName("白色转换")
        void testToAwtColorWhite() {
            Color awtColor = PdfColor.WHITE.toAwtColor();

            assertThat(awtColor).isEqualTo(Color.WHITE);
        }
    }

    @Nested
    @DisplayName("颜色模型测试")
    class ColorModelTests {

        @Test
        @DisplayName("RGB 颜色模型")
        void testRgbColorModel() {
            PdfColor color = PdfColor.rgb(100, 150, 200);

            assertThat(color.getModel()).isEqualTo(PdfColor.ColorModel.RGB);
        }

        @Test
        @DisplayName("GRAYSCALE 颜色模型")
        void testGrayscaleColorModel() {
            PdfColor color = PdfColor.gray(128);

            assertThat(color.getModel()).isEqualTo(PdfColor.ColorModel.GRAYSCALE);
        }

        @Test
        @DisplayName("ColorModel 枚举值存在")
        void testColorModelEnum() {
            assertThat(PdfColor.ColorModel.values()).containsExactly(
                PdfColor.ColorModel.RGB,
                PdfColor.ColorModel.CMYK,
                PdfColor.ColorModel.GRAYSCALE
            );
        }

        @Test
        @DisplayName("ColorModel valueOf")
        void testColorModelValueOf() {
            assertThat(PdfColor.ColorModel.valueOf("RGB")).isEqualTo(PdfColor.ColorModel.RGB);
            assertThat(PdfColor.ColorModel.valueOf("CMYK")).isEqualTo(PdfColor.ColorModel.CMYK);
            assertThat(PdfColor.ColorModel.valueOf("GRAYSCALE")).isEqualTo(PdfColor.ColorModel.GRAYSCALE);
        }
    }

    @Nested
    @DisplayName("toHex 方法测试")
    class ToHexTests {

        @Test
        @DisplayName("转换为十六进制字符串")
        void testToHex() {
            PdfColor color = PdfColor.rgb(255, 85, 0);

            assertThat(color.toHex()).isEqualToIgnoringCase("#FF5500");
        }

        @Test
        @DisplayName("黑色转换")
        void testToHexBlack() {
            assertThat(PdfColor.BLACK.toHex()).isEqualToIgnoringCase("#000000");
        }

        @Test
        @DisplayName("白色转换")
        void testToHexWhite() {
            assertThat(PdfColor.WHITE.toHex()).isEqualToIgnoringCase("#FFFFFF");
        }

        @Test
        @DisplayName("红色转换")
        void testToHexRed() {
            assertThat(PdfColor.RED.toHex()).isEqualToIgnoringCase("#FF0000");
        }

        @Test
        @DisplayName("绿色转换")
        void testToHexGreen() {
            assertThat(PdfColor.GREEN.toHex()).isEqualToIgnoringCase("#00FF00");
        }

        @Test
        @DisplayName("蓝色转换")
        void testToHexBlue() {
            assertThat(PdfColor.BLUE.toHex()).isEqualToIgnoringCase("#0000FF");
        }
    }

    @Nested
    @DisplayName("darker 方法测试")
    class DarkerTests {

        @Test
        @DisplayName("darker 创建深色版本")
        void testDarker() {
            PdfColor color = PdfColor.rgb(1f, 0.5f, 0.5f);
            PdfColor darker = color.darker(0.5f);

            assertThat(darker.getRed()).isEqualTo(0.5f);
            assertThat(darker.getGreen()).isEqualTo(0.25f);
            assertThat(darker.getBlue()).isEqualTo(0.25f);
        }

        @Test
        @DisplayName("darker 因子为 0 变成黑色")
        void testDarkerZero() {
            PdfColor color = PdfColor.WHITE;
            PdfColor darker = color.darker(0f);

            assertThat(darker.getRed()).isEqualTo(0f);
            assertThat(darker.getGreen()).isEqualTo(0f);
            assertThat(darker.getBlue()).isEqualTo(0f);
        }

        @Test
        @DisplayName("darker 因子为 1 不变")
        void testDarkerOne() {
            PdfColor color = PdfColor.rgb(0.5f, 0.5f, 0.5f);
            PdfColor darker = color.darker(1f);

            assertThat(darker.getRed()).isEqualTo(0.5f);
            assertThat(darker.getGreen()).isEqualTo(0.5f);
            assertThat(darker.getBlue()).isEqualTo(0.5f);
        }

        @Test
        @DisplayName("darker 保留 alpha")
        void testDarkerPreservesAlpha() {
            PdfColor color = PdfColor.rgba(255, 255, 255, 0.5f);
            PdfColor darker = color.darker(0.5f);

            assertThat(darker.getAlpha()).isEqualTo(0.5f);
        }
    }

    @Nested
    @DisplayName("lighter 方法测试")
    class LighterTests {

        @Test
        @DisplayName("lighter 创建浅色版本")
        void testLighter() {
            PdfColor color = PdfColor.rgb(0.5f, 0.5f, 0.5f);
            PdfColor lighter = color.lighter(0.5f);

            assertThat(lighter.getRed()).isEqualTo(0.75f);
            assertThat(lighter.getGreen()).isEqualTo(0.75f);
            assertThat(lighter.getBlue()).isEqualTo(0.75f);
        }

        @Test
        @DisplayName("lighter 因子为 1 变成白色")
        void testLighterOne() {
            PdfColor color = PdfColor.rgb(0.5f, 0.5f, 0.5f);
            PdfColor lighter = color.lighter(1f);

            assertThat(lighter.getRed()).isEqualTo(1f);
            assertThat(lighter.getGreen()).isEqualTo(1f);
            assertThat(lighter.getBlue()).isEqualTo(1f);
        }

        @Test
        @DisplayName("lighter 因子为 0 不变")
        void testLighterZero() {
            PdfColor color = PdfColor.rgb(0.5f, 0.5f, 0.5f);
            PdfColor lighter = color.lighter(0f);

            assertThat(lighter.getRed()).isEqualTo(0.5f);
            assertThat(lighter.getGreen()).isEqualTo(0.5f);
            assertThat(lighter.getBlue()).isEqualTo(0.5f);
        }

        @Test
        @DisplayName("lighter 保留 alpha")
        void testLighterPreservesAlpha() {
            PdfColor color = PdfColor.rgba(0, 0, 0, 0.5f);
            PdfColor lighter = color.lighter(0.5f);

            assertThat(lighter.getAlpha()).isEqualTo(0.5f);
        }
    }

    @Nested
    @DisplayName("withAlpha 方法测试")
    class WithAlphaTests {

        @Test
        @DisplayName("withAlpha 创建新透明度颜色")
        void testWithAlpha() {
            PdfColor color = PdfColor.rgb(255, 0, 0);
            PdfColor withAlpha = color.withAlpha(0.5f);

            assertThat(withAlpha.getRed()).isEqualTo(1f);
            assertThat(withAlpha.getGreen()).isEqualTo(0f);
            assertThat(withAlpha.getBlue()).isEqualTo(0f);
            assertThat(withAlpha.getAlpha()).isEqualTo(0.5f);
        }

        @Test
        @DisplayName("withAlpha 0 完全透明")
        void testWithAlphaZero() {
            PdfColor color = PdfColor.RED;
            PdfColor transparent = color.withAlpha(0f);

            assertThat(transparent.getAlpha()).isEqualTo(0f);
        }

        @Test
        @DisplayName("withAlpha 1 完全不透明")
        void testWithAlphaOne() {
            PdfColor color = PdfColor.rgba(255, 0, 0, 0.5f);
            PdfColor opaque = color.withAlpha(1f);

            assertThat(opaque.getAlpha()).isEqualTo(1f);
        }
    }

    @Nested
    @DisplayName("toString 方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString 返回格式化字符串")
        void testToString() {
            PdfColor color = PdfColor.rgb(255, 128, 0);

            assertThat(color.toString()).contains("PdfColor");
        }

        @Test
        @DisplayName("黑色 toString")
        void testToStringBlack() {
            assertThat(PdfColor.BLACK.toString()).contains("0.00");
        }

        @Test
        @DisplayName("白色 toString")
        void testToStringWhite() {
            assertThat(PdfColor.WHITE.toString()).contains("1.00");
        }
    }

    @Nested
    @DisplayName("final 类测试")
    class FinalClassTests {

        @Test
        @DisplayName("PdfColor 是 final 类")
        void testIsFinalClass() {
            assertThat(java.lang.reflect.Modifier.isFinal(PdfColor.class.getModifiers())).isTrue();
        }
    }
}
