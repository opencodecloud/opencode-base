package cloud.opencode.base.pdf.content;

import cloud.opencode.base.pdf.font.PdfFont;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfParagraph 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PdfParagraph 测试")
class PdfParagraphTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of 创建段落")
        void testOf() {
            PdfParagraph para = PdfParagraph.of("Hello World", 100f, 700f, 400f);

            assertThat(para).isNotNull();
            assertThat(para.getContent()).isEqualTo("Hello World");
            assertThat(para.getX()).isEqualTo(100f);
            assertThat(para.getY()).isEqualTo(700f);
            assertThat(para.getWidth()).isEqualTo(400f);
        }

        @Test
        @DisplayName("builder 创建构建器")
        void testBuilder() {
            PdfParagraph para = PdfParagraph.builder();

            assertThat(para).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder 方法测试")
    class BuilderMethodTests {

        @Test
        @DisplayName("content 设置内容")
        void testContent() {
            PdfParagraph para = PdfParagraph.builder().content("Test Content");

            assertThat(para.getContent()).isEqualTo("Test Content");
        }

        @Test
        @DisplayName("position 设置位置")
        void testPosition() {
            PdfParagraph para = PdfParagraph.builder().position(50f, 600f);

            assertThat(para.getX()).isEqualTo(50f);
            assertThat(para.getY()).isEqualTo(600f);
        }

        @Test
        @DisplayName("width 设置宽度")
        void testWidth() {
            PdfParagraph para = PdfParagraph.builder().width(500f);

            assertThat(para.getWidth()).isEqualTo(500f);
        }

        @Test
        @DisplayName("font 设置字体")
        void testFont() {
            PdfFont font = PdfFont.helvetica();
            PdfParagraph para = PdfParagraph.builder().font(font);

            assertThat(para.getFont()).isEqualTo(font);
        }

        @Test
        @DisplayName("fontSize 设置字体大小")
        void testFontSize() {
            PdfParagraph para = PdfParagraph.builder().fontSize(14f);

            assertThat(para.getFontSize()).isEqualTo(14f);
        }

        @Test
        @DisplayName("color 设置颜色")
        void testColor() {
            PdfParagraph para = PdfParagraph.builder().color(PdfColor.GRAY);

            assertThat(para.getColor()).isEqualTo(PdfColor.GRAY);
        }

        @Test
        @DisplayName("lineSpacing 设置行间距")
        void testLineSpacing() {
            PdfParagraph para = PdfParagraph.builder().lineSpacing(1.5f);

            assertThat(para.getLineSpacing()).isEqualTo(1.5f);
        }

        @Test
        @DisplayName("alignment 设置对齐方式")
        void testAlignment() {
            PdfParagraph para = PdfParagraph.builder().alignment(PdfParagraph.Alignment.JUSTIFY);

            assertThat(para.getAlignment()).isEqualTo(PdfParagraph.Alignment.JUSTIFY);
        }

        @Test
        @DisplayName("firstLineIndent 设置首行缩进")
        void testFirstLineIndent() {
            PdfParagraph para = PdfParagraph.builder().firstLineIndent(24f);

            assertThat(para.getFirstLineIndent()).isEqualTo(24f);
        }
    }

    @Nested
    @DisplayName("Alignment 枚举测试")
    class AlignmentEnumTests {

        @Test
        @DisplayName("包含所有对齐方式")
        void testAllAlignments() {
            assertThat(PdfParagraph.Alignment.values()).containsExactly(
                PdfParagraph.Alignment.LEFT,
                PdfParagraph.Alignment.CENTER,
                PdfParagraph.Alignment.RIGHT,
                PdfParagraph.Alignment.JUSTIFY
            );
        }

        @Test
        @DisplayName("valueOf 方法正常工作")
        void testValueOf() {
            assertThat(PdfParagraph.Alignment.valueOf("LEFT")).isEqualTo(PdfParagraph.Alignment.LEFT);
            assertThat(PdfParagraph.Alignment.valueOf("CENTER")).isEqualTo(PdfParagraph.Alignment.CENTER);
            assertThat(PdfParagraph.Alignment.valueOf("RIGHT")).isEqualTo(PdfParagraph.Alignment.RIGHT);
            assertThat(PdfParagraph.Alignment.valueOf("JUSTIFY")).isEqualTo(PdfParagraph.Alignment.JUSTIFY);
        }
    }

    @Nested
    @DisplayName("PdfElement 接口测试")
    class PdfElementInterfaceTests {

        @Test
        @DisplayName("实现 PdfElement 接口")
        void testImplementsPdfElement() {
            PdfParagraph para = PdfParagraph.of("Test", 0, 0, 100);

            assertThat(para).isInstanceOf(PdfElement.class);
        }

        @Test
        @DisplayName("getX 返回正确值")
        void testGetX() {
            PdfParagraph para = PdfParagraph.of("Test", 150f, 200f, 400f);

            assertThat(para.getX()).isEqualTo(150f);
        }

        @Test
        @DisplayName("getY 返回正确值")
        void testGetY() {
            PdfParagraph para = PdfParagraph.of("Test", 150f, 200f, 400f);

            assertThat(para.getY()).isEqualTo(200f);
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentApiTests {

        @Test
        @DisplayName("完整的链式调用")
        void testFullFluentApi() {
            PdfParagraph para = PdfParagraph.of("Long paragraph text...", 50f, 700f, 500f)
                .font(PdfFont.timesRoman())
                .fontSize(12f)
                .color(PdfColor.BLACK)
                .lineSpacing(1.2f)
                .alignment(PdfParagraph.Alignment.JUSTIFY)
                .firstLineIndent(20f);

            assertThat(para.getContent()).isEqualTo("Long paragraph text...");
            assertThat(para.getWidth()).isEqualTo(500f);
            assertThat(para.getFontSize()).isEqualTo(12f);
            assertThat(para.getLineSpacing()).isEqualTo(1.2f);
            assertThat(para.getAlignment()).isEqualTo(PdfParagraph.Alignment.JUSTIFY);
            assertThat(para.getFirstLineIndent()).isEqualTo(20f);
        }

        @Test
        @DisplayName("返回相同实例")
        void testReturnsSameInstance() {
            PdfParagraph para = PdfParagraph.builder();

            assertThat(para.content("Test")).isSameAs(para);
            assertThat(para.width(100)).isSameAs(para);
            assertThat(para.fontSize(12)).isSameAs(para);
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认字体大小为 12")
        void testDefaultFontSize() {
            PdfParagraph para = PdfParagraph.builder();

            assertThat(para.getFontSize()).isEqualTo(12f);
        }

        @Test
        @DisplayName("默认颜色为黑色")
        void testDefaultColor() {
            PdfParagraph para = PdfParagraph.builder();

            assertThat(para.getColor()).isEqualTo(PdfColor.BLACK);
        }

        @Test
        @DisplayName("默认行间距为 1.2")
        void testDefaultLineSpacing() {
            PdfParagraph para = PdfParagraph.builder();

            assertThat(para.getLineSpacing()).isEqualTo(1.2f);
        }

        @Test
        @DisplayName("默认对齐为 LEFT")
        void testDefaultAlignment() {
            PdfParagraph para = PdfParagraph.builder();

            assertThat(para.getAlignment()).isEqualTo(PdfParagraph.Alignment.LEFT);
        }

        @Test
        @DisplayName("默认首行缩进为 0")
        void testDefaultFirstLineIndent() {
            PdfParagraph para = PdfParagraph.builder();

            assertThat(para.getFirstLineIndent()).isEqualTo(0f);
        }

        @Test
        @DisplayName("默认无字体")
        void testDefaultNoFont() {
            PdfParagraph para = PdfParagraph.builder();

            assertThat(para.getFont()).isNull();
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("空内容")
        void testEmptyContent() {
            PdfParagraph para = PdfParagraph.of("", 0, 0, 100);

            assertThat(para.getContent()).isEmpty();
        }

        @Test
        @DisplayName("长文本内容")
        void testLongContent() {
            String longText = "A".repeat(10000);
            PdfParagraph para = PdfParagraph.of(longText, 0, 0, 100);

            assertThat(para.getContent()).hasSize(10000);
        }

        @Test
        @DisplayName("负坐标")
        void testNegativeCoordinates() {
            PdfParagraph para = PdfParagraph.of("Test", -100f, -200f, 400f);

            assertThat(para.getX()).isEqualTo(-100f);
            assertThat(para.getY()).isEqualTo(-200f);
        }

        @Test
        @DisplayName("大行间距")
        void testLargeLineSpacing() {
            PdfParagraph para = PdfParagraph.builder().lineSpacing(3.0f);

            assertThat(para.getLineSpacing()).isEqualTo(3.0f);
        }

        @Test
        @DisplayName("大首行缩进")
        void testLargeFirstLineIndent() {
            PdfParagraph para = PdfParagraph.builder().firstLineIndent(100f);

            assertThat(para.getFirstLineIndent()).isEqualTo(100f);
        }
    }

    @Nested
    @DisplayName("final 类测试")
    class FinalClassTests {

        @Test
        @DisplayName("PdfParagraph 是 final 类")
        void testIsFinalClass() {
            assertThat(java.lang.reflect.Modifier.isFinal(PdfParagraph.class.getModifiers())).isTrue();
        }
    }
}
