package cloud.opencode.base.pdf.document;

import cloud.opencode.base.pdf.content.PdfColor;
import cloud.opencode.base.pdf.font.PdfFont;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfFooter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
@DisplayName("PdfFooter 测试")
class PdfFooterTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of 创建居中页脚")
        void testOf() {
            PdfFooter footer = PdfFooter.of("Page {page} of {total}");

            assertThat(footer.getCenter()).isEqualTo("Page {page} of {total}");
            assertThat(footer.getLeft()).isNull();
            assertThat(footer.getRight()).isNull();
        }

        @Test
        @DisplayName("builder 创建空页脚")
        void testBuilder() {
            PdfFooter footer = PdfFooter.builder();

            assertThat(footer.getLeft()).isNull();
            assertThat(footer.getCenter()).isNull();
            assertThat(footer.getRight()).isNull();
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认字号为 9")
        void testDefaultFontSize() {
            PdfFooter footer = PdfFooter.builder();

            assertThat(footer.getFontSize()).isEqualTo(9f);
        }

        @Test
        @DisplayName("默认颜色为 DARK_GRAY")
        void testDefaultColor() {
            PdfFooter footer = PdfFooter.builder();

            assertThat(footer.getColor()).isEqualTo(PdfColor.DARK_GRAY);
        }

        @Test
        @DisplayName("默认字体为 null")
        void testDefaultFont() {
            PdfFooter footer = PdfFooter.builder();

            assertThat(footer.getFont()).isNull();
        }

        @Test
        @DisplayName("默认不显示分隔线")
        void testDefaultShowLine() {
            PdfFooter footer = PdfFooter.builder();

            assertThat(footer.isShowLine()).isFalse();
        }
    }

    @Nested
    @DisplayName("链式构建测试")
    class BuilderTests {

        @Test
        @DisplayName("left 设置左侧文本")
        void testLeft() {
            PdfFooter footer = PdfFooter.builder().left("Confidential");

            assertThat(footer.getLeft()).isEqualTo("Confidential");
        }

        @Test
        @DisplayName("center 设置居中文本")
        void testCenter() {
            PdfFooter footer = PdfFooter.builder().center("Page {page}");

            assertThat(footer.getCenter()).isEqualTo("Page {page}");
        }

        @Test
        @DisplayName("right 设置右侧文本")
        void testRight() {
            PdfFooter footer = PdfFooter.builder().right("2026-04-04");

            assertThat(footer.getRight()).isEqualTo("2026-04-04");
        }

        @Test
        @DisplayName("fontSize 设置字号")
        void testFontSize() {
            PdfFooter footer = PdfFooter.builder().fontSize(8f);

            assertThat(footer.getFontSize()).isEqualTo(8f);
        }

        @Test
        @DisplayName("fontSize 非正数抛出异常")
        void testFontSizeInvalid() {
            assertThatThrownBy(() -> PdfFooter.builder().fontSize(0f))
                .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> PdfFooter.builder().fontSize(-5f))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("color 设置颜色")
        void testColor() {
            PdfFooter footer = PdfFooter.builder().color(PdfColor.GRAY);

            assertThat(footer.getColor()).isEqualTo(PdfColor.GRAY);
        }

        @Test
        @DisplayName("color null 抛出异常")
        void testColorNull() {
            assertThatThrownBy(() -> PdfFooter.builder().color(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("font 设置字体")
        void testFont() {
            PdfFont font = PdfFont.timesRoman();
            PdfFooter footer = PdfFooter.builder().font(font);

            assertThat(footer.getFont()).isEqualTo(font);
        }

        @Test
        @DisplayName("showLine 设置分隔线")
        void testShowLine() {
            PdfFooter footer = PdfFooter.builder().showLine(true);

            assertThat(footer.isShowLine()).isTrue();
        }

        @Test
        @DisplayName("完整链式调用")
        void testFullChain() {
            PdfFont font = PdfFont.courier();
            PdfFooter footer = PdfFooter.builder()
                .left("Left")
                .center("Center")
                .right("Right")
                .fontSize(8f)
                .color(PdfColor.GRAY)
                .font(font)
                .showLine(true);

            assertThat(footer.getLeft()).isEqualTo("Left");
            assertThat(footer.getCenter()).isEqualTo("Center");
            assertThat(footer.getRight()).isEqualTo("Right");
            assertThat(footer.getFontSize()).isEqualTo(8f);
            assertThat(footer.getColor()).isEqualTo(PdfColor.GRAY);
            assertThat(footer.getFont()).isEqualTo(font);
            assertThat(footer.isShowLine()).isTrue();
        }
    }

    @Nested
    @DisplayName("占位符解析测试")
    class ResolvePlaceholderTests {

        @Test
        @DisplayName("resolveLeft 替换页码")
        void testResolveLeft() {
            PdfFooter footer = PdfFooter.builder().left("Page {page} of {total}");

            assertThat(footer.resolveLeft(3, 10)).isEqualTo("Page 3 of 10");
        }

        @Test
        @DisplayName("resolveCenter 替换页码")
        void testResolveCenter() {
            PdfFooter footer = PdfFooter.of("{page}/{total}");

            assertThat(footer.resolveCenter(1, 5)).isEqualTo("1/5");
        }

        @Test
        @DisplayName("resolveRight 替换页码")
        void testResolveRight() {
            PdfFooter footer = PdfFooter.builder().right("Page {page}");

            assertThat(footer.resolveRight(7, 20)).isEqualTo("Page 7");
        }

        @Test
        @DisplayName("null 文本解析返回 null")
        void testResolveNull() {
            PdfFooter footer = PdfFooter.builder();

            assertThat(footer.resolveLeft(1, 1)).isNull();
            assertThat(footer.resolveCenter(1, 1)).isNull();
            assertThat(footer.resolveRight(1, 1)).isNull();
        }

        @Test
        @DisplayName("无占位符的文本原样返回")
        void testResolveNoPlaceholders() {
            PdfFooter footer = PdfFooter.of("Static Footer");

            assertThat(footer.resolveCenter(5, 10)).isEqualTo("Static Footer");
        }
    }

    @Nested
    @DisplayName("PdfHeader 与 PdfFooter 默认差异测试")
    class HeaderFooterDifferenceTests {

        @Test
        @DisplayName("PdfHeader 默认 showLine=true, PdfFooter 默认 showLine=false")
        void testShowLineDefaults() {
            PdfHeader header = PdfHeader.builder();
            PdfFooter footer = PdfFooter.builder();

            assertThat(header.isShowLine()).isTrue();
            assertThat(footer.isShowLine()).isFalse();
        }
    }

    @Nested
    @DisplayName("toString 测试")
    class ToStringTests {

        @Test
        @DisplayName("toString 包含关键信息")
        void testToString() {
            PdfFooter footer = PdfFooter.builder()
                .left("L")
                .center("C")
                .right("R");

            String str = footer.toString();
            assertThat(str).contains("PdfFooter");
            assertThat(str).contains("L");
            assertThat(str).contains("C");
            assertThat(str).contains("R");
        }
    }
}
