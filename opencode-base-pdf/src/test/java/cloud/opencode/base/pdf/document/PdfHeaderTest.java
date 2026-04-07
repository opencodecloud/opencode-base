package cloud.opencode.base.pdf.document;

import cloud.opencode.base.pdf.content.PdfColor;
import cloud.opencode.base.pdf.font.PdfFont;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfHeader 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
@DisplayName("PdfHeader 测试")
class PdfHeaderTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of 创建居中页眉")
        void testOf() {
            PdfHeader header = PdfHeader.of("My Document");

            assertThat(header.getCenter()).isEqualTo("My Document");
            assertThat(header.getLeft()).isNull();
            assertThat(header.getRight()).isNull();
        }

        @Test
        @DisplayName("builder 创建空页眉")
        void testBuilder() {
            PdfHeader header = PdfHeader.builder();

            assertThat(header.getLeft()).isNull();
            assertThat(header.getCenter()).isNull();
            assertThat(header.getRight()).isNull();
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认字号为 9")
        void testDefaultFontSize() {
            PdfHeader header = PdfHeader.builder();

            assertThat(header.getFontSize()).isEqualTo(9f);
        }

        @Test
        @DisplayName("默认颜色为 DARK_GRAY")
        void testDefaultColor() {
            PdfHeader header = PdfHeader.builder();

            assertThat(header.getColor()).isEqualTo(PdfColor.DARK_GRAY);
        }

        @Test
        @DisplayName("默认字体为 null")
        void testDefaultFont() {
            PdfHeader header = PdfHeader.builder();

            assertThat(header.getFont()).isNull();
        }

        @Test
        @DisplayName("默认显示分隔线")
        void testDefaultShowLine() {
            PdfHeader header = PdfHeader.builder();

            assertThat(header.isShowLine()).isTrue();
        }
    }

    @Nested
    @DisplayName("链式构建测试")
    class BuilderTests {

        @Test
        @DisplayName("left 设置左侧文本")
        void testLeft() {
            PdfHeader header = PdfHeader.builder().left("Title");

            assertThat(header.getLeft()).isEqualTo("Title");
        }

        @Test
        @DisplayName("center 设置居中文本")
        void testCenter() {
            PdfHeader header = PdfHeader.builder().center("Center Text");

            assertThat(header.getCenter()).isEqualTo("Center Text");
        }

        @Test
        @DisplayName("right 设置右侧文本")
        void testRight() {
            PdfHeader header = PdfHeader.builder().right("Page {page}");

            assertThat(header.getRight()).isEqualTo("Page {page}");
        }

        @Test
        @DisplayName("fontSize 设置字号")
        void testFontSize() {
            PdfHeader header = PdfHeader.builder().fontSize(12f);

            assertThat(header.getFontSize()).isEqualTo(12f);
        }

        @Test
        @DisplayName("fontSize 非正数抛出异常")
        void testFontSizeInvalid() {
            assertThatThrownBy(() -> PdfHeader.builder().fontSize(0f))
                .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> PdfHeader.builder().fontSize(-1f))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("color 设置颜色")
        void testColor() {
            PdfHeader header = PdfHeader.builder().color(PdfColor.BLACK);

            assertThat(header.getColor()).isEqualTo(PdfColor.BLACK);
        }

        @Test
        @DisplayName("color null 抛出异常")
        void testColorNull() {
            assertThatThrownBy(() -> PdfHeader.builder().color(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("font 设置字体")
        void testFont() {
            PdfFont font = PdfFont.helveticaBold();
            PdfHeader header = PdfHeader.builder().font(font);

            assertThat(header.getFont()).isEqualTo(font);
        }

        @Test
        @DisplayName("showLine 设置分隔线")
        void testShowLine() {
            PdfHeader header = PdfHeader.builder().showLine(false);

            assertThat(header.isShowLine()).isFalse();
        }

        @Test
        @DisplayName("完整链式调用")
        void testFullChain() {
            PdfFont font = PdfFont.courier();
            PdfHeader header = PdfHeader.builder()
                .left("Left")
                .center("Center")
                .right("Right")
                .fontSize(10f)
                .color(PdfColor.BLACK)
                .font(font)
                .showLine(false);

            assertThat(header.getLeft()).isEqualTo("Left");
            assertThat(header.getCenter()).isEqualTo("Center");
            assertThat(header.getRight()).isEqualTo("Right");
            assertThat(header.getFontSize()).isEqualTo(10f);
            assertThat(header.getColor()).isEqualTo(PdfColor.BLACK);
            assertThat(header.getFont()).isEqualTo(font);
            assertThat(header.isShowLine()).isFalse();
        }
    }

    @Nested
    @DisplayName("占位符解析测试")
    class ResolvePlaceholderTests {

        @Test
        @DisplayName("resolveLeft 替换页码")
        void testResolveLeft() {
            PdfHeader header = PdfHeader.builder().left("Page {page} of {total}");

            assertThat(header.resolveLeft(3, 10)).isEqualTo("Page 3 of 10");
        }

        @Test
        @DisplayName("resolveCenter 替换页码")
        void testResolveCenter() {
            PdfHeader header = PdfHeader.of("{page}/{total}");

            assertThat(header.resolveCenter(1, 5)).isEqualTo("1/5");
        }

        @Test
        @DisplayName("resolveRight 替换页码")
        void testResolveRight() {
            PdfHeader header = PdfHeader.builder().right("Page {page}");

            assertThat(header.resolveRight(7, 20)).isEqualTo("Page 7");
        }

        @Test
        @DisplayName("null 文本解析返回 null")
        void testResolveNull() {
            PdfHeader header = PdfHeader.builder();

            assertThat(header.resolveLeft(1, 1)).isNull();
            assertThat(header.resolveCenter(1, 1)).isNull();
            assertThat(header.resolveRight(1, 1)).isNull();
        }

        @Test
        @DisplayName("无占位符的文本原样返回")
        void testResolveNoPlaceholders() {
            PdfHeader header = PdfHeader.of("Static Text");

            assertThat(header.resolveCenter(5, 10)).isEqualTo("Static Text");
        }
    }

    @Nested
    @DisplayName("toString 测试")
    class ToStringTests {

        @Test
        @DisplayName("toString 包含关键信息")
        void testToString() {
            PdfHeader header = PdfHeader.builder()
                .left("L")
                .center("C")
                .right("R");

            String str = header.toString();
            assertThat(str).contains("PdfHeader");
            assertThat(str).contains("L");
            assertThat(str).contains("C");
            assertThat(str).contains("R");
        }
    }
}
