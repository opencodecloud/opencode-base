package cloud.opencode.base.pdf.content;

import cloud.opencode.base.pdf.font.PdfFont;
import cloud.opencode.base.pdf.font.StandardFont;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfText 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PdfText 测试")
class PdfTextTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of 创建基本文本")
        void testOf() {
            PdfText text = PdfText.of("Hello", 100f, 200f);

            assertThat(text.getContent()).isEqualTo("Hello");
            assertThat(text.getX()).isEqualTo(100f);
            assertThat(text.getY()).isEqualTo(200f);
        }

        @Test
        @DisplayName("builder 创建空构建器")
        void testBuilder() {
            PdfText text = PdfText.builder();

            assertThat(text).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder 方法测试")
    class BuilderMethodTests {

        @Test
        @DisplayName("content 设置文本内容")
        void testContent() {
            PdfText text = PdfText.builder().content("Test Content");

            assertThat(text.getContent()).isEqualTo("Test Content");
        }

        @Test
        @DisplayName("position 设置位置")
        void testPosition() {
            PdfText text = PdfText.builder().position(50f, 100f);

            assertThat(text.getX()).isEqualTo(50f);
            assertThat(text.getY()).isEqualTo(100f);
        }

        @Test
        @DisplayName("font 设置字体")
        void testFont() {
            PdfFont font = PdfFont.helvetica();
            PdfText text = PdfText.builder().font(font);

            assertThat(text.getFont()).isEqualTo(font);
        }

        @Test
        @DisplayName("fontSize 设置字体大小")
        void testFontSize() {
            PdfText text = PdfText.builder().fontSize(14f);

            assertThat(text.getFontSize()).isEqualTo(14f);
        }

        @Test
        @DisplayName("color(PdfColor) 设置颜色")
        void testColorWithPdfColor() {
            PdfText text = PdfText.builder().color(PdfColor.RED);

            assertThat(text.getColor()).isEqualTo(PdfColor.RED);
        }

        @Test
        @DisplayName("color(int, int, int) 设置 RGB 颜色")
        void testColorWithRgb() {
            PdfText text = PdfText.builder().color(100, 150, 200);

            assertThat(text.getColor()).isNotNull();
            assertThat(text.getColor().getRed()).isCloseTo(100f / 255f, within(0.01f));
            assertThat(text.getColor().getGreen()).isCloseTo(150f / 255f, within(0.01f));
            assertThat(text.getColor().getBlue()).isCloseTo(200f / 255f, within(0.01f));
        }

        @Test
        @DisplayName("bold 设置粗体")
        void testBold() {
            PdfText text = PdfText.builder().bold(true);

            assertThat(text.isBold()).isTrue();
        }

        @Test
        @DisplayName("italic 设置斜体")
        void testItalic() {
            PdfText text = PdfText.builder().italic(true);

            assertThat(text.isItalic()).isTrue();
        }

        @Test
        @DisplayName("underline 设置下划线")
        void testUnderline() {
            PdfText text = PdfText.builder().underline(true);

            assertThat(text.isUnderline()).isTrue();
        }

        @Test
        @DisplayName("characterSpacing 设置字符间距")
        void testCharacterSpacing() {
            PdfText text = PdfText.builder().characterSpacing(2.5f);

            assertThat(text.getCharacterSpacing()).isEqualTo(2.5f);
        }

        @Test
        @DisplayName("wordSpacing 设置单词间距")
        void testWordSpacing() {
            PdfText text = PdfText.builder().wordSpacing(5.0f);

            assertThat(text.getWordSpacing()).isEqualTo(5.0f);
        }

        @Test
        @DisplayName("rotation 设置旋转角度")
        void testRotation() {
            PdfText text = PdfText.builder().rotation(45f);

            assertThat(text.getRotation()).isEqualTo(45f);
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentApiTests {

        @Test
        @DisplayName("完整的链式调用")
        void testFullFluentApi() {
            PdfText text = PdfText.of("Hello World", 100f, 700f)
                .font(PdfFont.helvetica())
                .fontSize(12f)
                .color(PdfColor.BLACK)
                .bold(true)
                .italic(false)
                .underline(true)
                .characterSpacing(1.0f)
                .wordSpacing(2.0f)
                .rotation(0f);

            assertThat(text.getContent()).isEqualTo("Hello World");
            assertThat(text.getX()).isEqualTo(100f);
            assertThat(text.getY()).isEqualTo(700f);
            assertThat(text.getFontSize()).isEqualTo(12f);
            assertThat(text.isBold()).isTrue();
            assertThat(text.isItalic()).isFalse();
            assertThat(text.isUnderline()).isTrue();
        }

        @Test
        @DisplayName("返回相同实例")
        void testReturnsSameInstance() {
            PdfText text = PdfText.builder();

            assertThat(text.content("Test")).isSameAs(text);
            assertThat(text.position(0, 0)).isSameAs(text);
            assertThat(text.fontSize(12)).isSameAs(text);
            assertThat(text.bold(true)).isSameAs(text);
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认字体大小为 12")
        void testDefaultFontSize() {
            PdfText text = PdfText.builder();

            assertThat(text.getFontSize()).isEqualTo(12f);
        }

        @Test
        @DisplayName("默认颜色为黑色")
        void testDefaultColor() {
            PdfText text = PdfText.builder();

            assertThat(text.getColor()).isEqualTo(PdfColor.BLACK);
        }

        @Test
        @DisplayName("默认不是粗体")
        void testDefaultNotBold() {
            PdfText text = PdfText.builder();

            assertThat(text.isBold()).isFalse();
        }

        @Test
        @DisplayName("默认不是斜体")
        void testDefaultNotItalic() {
            PdfText text = PdfText.builder();

            assertThat(text.isItalic()).isFalse();
        }

        @Test
        @DisplayName("默认没有下划线")
        void testDefaultNoUnderline() {
            PdfText text = PdfText.builder();

            assertThat(text.isUnderline()).isFalse();
        }

        @Test
        @DisplayName("默认字符间距为 0")
        void testDefaultCharacterSpacing() {
            PdfText text = PdfText.builder();

            assertThat(text.getCharacterSpacing()).isEqualTo(0f);
        }

        @Test
        @DisplayName("默认单词间距为 0")
        void testDefaultWordSpacing() {
            PdfText text = PdfText.builder();

            assertThat(text.getWordSpacing()).isEqualTo(0f);
        }

        @Test
        @DisplayName("默认旋转为 0")
        void testDefaultRotation() {
            PdfText text = PdfText.builder();

            assertThat(text.getRotation()).isEqualTo(0f);
        }

        @Test
        @DisplayName("默认无字体")
        void testDefaultNoFont() {
            PdfText text = PdfText.builder();

            assertThat(text.getFont()).isNull();
        }
    }

    @Nested
    @DisplayName("PdfElement 接口测试")
    class PdfElementInterfaceTests {

        @Test
        @DisplayName("实现 PdfElement 接口")
        void testImplementsPdfElement() {
            PdfText text = PdfText.of("Test", 0, 0);

            assertThat(text).isInstanceOf(PdfElement.class);
        }

        @Test
        @DisplayName("getX 返回正确值")
        void testGetX() {
            PdfText text = PdfText.of("Test", 150f, 200f);

            assertThat(text.getX()).isEqualTo(150f);
        }

        @Test
        @DisplayName("getY 返回正确值")
        void testGetY() {
            PdfText text = PdfText.of("Test", 150f, 200f);

            assertThat(text.getY()).isEqualTo(200f);
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("空字符串内容")
        void testEmptyContent() {
            PdfText text = PdfText.of("", 0, 0);

            assertThat(text.getContent()).isEmpty();
        }

        @Test
        @DisplayName("null 内容")
        void testNullContent() {
            PdfText text = PdfText.of(null, 0, 0);

            assertThat(text.getContent()).isNull();
        }

        @Test
        @DisplayName("负坐标值")
        void testNegativeCoordinates() {
            PdfText text = PdfText.of("Test", -100f, -200f);

            assertThat(text.getX()).isEqualTo(-100f);
            assertThat(text.getY()).isEqualTo(-200f);
        }

        @Test
        @DisplayName("大字体大小")
        void testLargeFontSize() {
            PdfText text = PdfText.builder().fontSize(1000f);

            assertThat(text.getFontSize()).isEqualTo(1000f);
        }

        @Test
        @DisplayName("负旋转角度")
        void testNegativeRotation() {
            PdfText text = PdfText.builder().rotation(-90f);

            assertThat(text.getRotation()).isEqualTo(-90f);
        }

        @Test
        @DisplayName("360度旋转")
        void testFullRotation() {
            PdfText text = PdfText.builder().rotation(360f);

            assertThat(text.getRotation()).isEqualTo(360f);
        }
    }

    @Nested
    @DisplayName("final 类测试")
    class FinalClassTests {

        @Test
        @DisplayName("PdfText 是 final 类")
        void testIsFinalClass() {
            assertThat(java.lang.reflect.Modifier.isFinal(PdfText.class.getModifiers())).isTrue();
        }
    }
}
