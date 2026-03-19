package cloud.opencode.base.pdf.content;

import cloud.opencode.base.pdf.font.PdfFont;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfCell 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PdfCell 测试")
class PdfCellTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of(String) 创建文本单元格")
        void testOfString() {
            PdfCell cell = PdfCell.of("Hello");

            assertThat(cell).isNotNull();
            assertThat(cell.getContent()).isEqualTo("Hello");
        }

        @Test
        @DisplayName("empty 创建空单元格")
        void testEmpty() {
            PdfCell cell = PdfCell.empty();

            assertThat(cell).isNotNull();
            assertThat(cell.getContent()).isNull();
        }

        @Test
        @DisplayName("builder 创建空构建器")
        void testBuilder() {
            PdfCell cell = PdfCell.builder();

            assertThat(cell).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder 方法测试")
    class BuilderMethodTests {

        @Test
        @DisplayName("content 设置文本内容")
        void testContent() {
            PdfCell cell = PdfCell.builder().content("Cell Content");

            assertThat(cell.getContent()).isEqualTo("Cell Content");
        }

        @Test
        @DisplayName("font 设置字体")
        void testFont() {
            PdfFont font = PdfFont.helveticaBold();
            PdfCell cell = PdfCell.builder().font(font);

            assertThat(cell.getFont()).isEqualTo(font);
        }

        @Test
        @DisplayName("fontSize 设置字体大小")
        void testFontSize() {
            PdfCell cell = PdfCell.builder().fontSize(14f);

            assertThat(cell.getFontSize()).isEqualTo(14f);
        }

        @Test
        @DisplayName("textColor 设置文本颜色")
        void testTextColor() {
            PdfCell cell = PdfCell.builder().textColor(PdfColor.RED);

            assertThat(cell.getTextColor()).isEqualTo(PdfColor.RED);
        }

        @Test
        @DisplayName("backgroundColor 设置背景色")
        void testBackgroundColor() {
            PdfCell cell = PdfCell.builder().backgroundColor(PdfColor.GRAY);

            assertThat(cell.getBackgroundColor()).isEqualTo(PdfColor.GRAY);
        }

        @Test
        @DisplayName("align 设置水平对齐")
        void testAlign() {
            PdfCell cell = PdfCell.builder().align(PdfCell.Alignment.CENTER);

            assertThat(cell.getHorizontalAlignment()).isEqualTo(PdfCell.Alignment.CENTER);
        }

        @Test
        @DisplayName("valign 设置垂直对齐")
        void testValign() {
            PdfCell cell = PdfCell.builder().valign(PdfCell.VerticalAlignment.TOP);

            assertThat(cell.getVerticalAlignment()).isEqualTo(PdfCell.VerticalAlignment.TOP);
        }

        @Test
        @DisplayName("colspan 设置列合并")
        void testColspan() {
            PdfCell cell = PdfCell.builder().content("Wide").colspan(2);

            assertThat(cell.getColspan()).isEqualTo(2);
        }

        @Test
        @DisplayName("rowspan 设置行合并")
        void testRowspan() {
            PdfCell cell = PdfCell.builder().content("Tall").rowspan(3);

            assertThat(cell.getRowspan()).isEqualTo(3);
        }

        @Test
        @DisplayName("padding 设置内边距")
        void testPadding() {
            PdfCell cell = PdfCell.builder().padding(10f);

            assertThat(cell.getPadding()).isEqualTo(10f);
        }

        @Test
        @DisplayName("border 设置边框")
        void testBorder() {
            PdfCell cell = PdfCell.builder().border(2f, PdfColor.RED);

            assertThat(cell.getBorderWidth()).isEqualTo(2f);
            assertThat(cell.getBorderColor()).isEqualTo(PdfColor.RED);
        }
    }

    @Nested
    @DisplayName("Alignment 枚举测试")
    class AlignmentEnumTests {

        @Test
        @DisplayName("包含所有对齐方式")
        void testAllAlignments() {
            assertThat(PdfCell.Alignment.values()).containsExactly(
                PdfCell.Alignment.LEFT,
                PdfCell.Alignment.CENTER,
                PdfCell.Alignment.RIGHT
            );
        }

        @Test
        @DisplayName("valueOf 方法正常工作")
        void testValueOf() {
            assertThat(PdfCell.Alignment.valueOf("LEFT")).isEqualTo(PdfCell.Alignment.LEFT);
            assertThat(PdfCell.Alignment.valueOf("CENTER")).isEqualTo(PdfCell.Alignment.CENTER);
            assertThat(PdfCell.Alignment.valueOf("RIGHT")).isEqualTo(PdfCell.Alignment.RIGHT);
        }
    }

    @Nested
    @DisplayName("VerticalAlignment 枚举测试")
    class VerticalAlignmentEnumTests {

        @Test
        @DisplayName("包含所有垂直对齐方式")
        void testAllVerticalAlignments() {
            assertThat(PdfCell.VerticalAlignment.values()).containsExactly(
                PdfCell.VerticalAlignment.TOP,
                PdfCell.VerticalAlignment.MIDDLE,
                PdfCell.VerticalAlignment.BOTTOM
            );
        }

        @Test
        @DisplayName("valueOf 方法正常工作")
        void testValueOf() {
            assertThat(PdfCell.VerticalAlignment.valueOf("TOP")).isEqualTo(PdfCell.VerticalAlignment.TOP);
            assertThat(PdfCell.VerticalAlignment.valueOf("MIDDLE")).isEqualTo(PdfCell.VerticalAlignment.MIDDLE);
            assertThat(PdfCell.VerticalAlignment.valueOf("BOTTOM")).isEqualTo(PdfCell.VerticalAlignment.BOTTOM);
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentApiTests {

        @Test
        @DisplayName("完整的链式调用")
        void testFullFluentApi() {
            PdfCell cell = PdfCell.of("Content")
                .font(PdfFont.helvetica())
                .fontSize(12f)
                .textColor(PdfColor.BLACK)
                .backgroundColor(PdfColor.WHITE)
                .colspan(2)
                .rowspan(1)
                .align(PdfCell.Alignment.CENTER)
                .valign(PdfCell.VerticalAlignment.MIDDLE)
                .padding(5f)
                .border(1f, PdfColor.BLACK);

            assertThat(cell.getContent()).isEqualTo("Content");
            assertThat(cell.getColspan()).isEqualTo(2);
            assertThat(cell.getHorizontalAlignment()).isEqualTo(PdfCell.Alignment.CENTER);
            assertThat(cell.getVerticalAlignment()).isEqualTo(PdfCell.VerticalAlignment.MIDDLE);
        }

        @Test
        @DisplayName("返回相同实例")
        void testReturnsSameInstance() {
            PdfCell cell = PdfCell.builder();

            assertThat(cell.content("Test")).isSameAs(cell);
            assertThat(cell.colspan(1)).isSameAs(cell);
            assertThat(cell.rowspan(1)).isSameAs(cell);
            assertThat(cell.padding(5f)).isSameAs(cell);
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认字体大小为 12")
        void testDefaultFontSize() {
            PdfCell cell = PdfCell.of("Test");

            assertThat(cell.getFontSize()).isEqualTo(12f);
        }

        @Test
        @DisplayName("默认文本颜色为黑色")
        void testDefaultTextColor() {
            PdfCell cell = PdfCell.of("Test");

            assertThat(cell.getTextColor()).isEqualTo(PdfColor.BLACK);
        }

        @Test
        @DisplayName("默认 colspan 为 1")
        void testDefaultColspan() {
            PdfCell cell = PdfCell.of("Test");

            assertThat(cell.getColspan()).isEqualTo(1);
        }

        @Test
        @DisplayName("默认 rowspan 为 1")
        void testDefaultRowspan() {
            PdfCell cell = PdfCell.of("Test");

            assertThat(cell.getRowspan()).isEqualTo(1);
        }

        @Test
        @DisplayName("默认对齐为 LEFT")
        void testDefaultAlignment() {
            PdfCell cell = PdfCell.of("Test");

            assertThat(cell.getHorizontalAlignment()).isEqualTo(PdfCell.Alignment.LEFT);
        }

        @Test
        @DisplayName("默认垂直对齐为 MIDDLE")
        void testDefaultVerticalAlignment() {
            PdfCell cell = PdfCell.of("Test");

            assertThat(cell.getVerticalAlignment()).isEqualTo(PdfCell.VerticalAlignment.MIDDLE);
        }

        @Test
        @DisplayName("默认内边距为 5")
        void testDefaultPadding() {
            PdfCell cell = PdfCell.of("Test");

            assertThat(cell.getPadding()).isEqualTo(5f);
        }

        @Test
        @DisplayName("默认边框宽度为 0.5")
        void testDefaultBorderWidth() {
            PdfCell cell = PdfCell.of("Test");

            assertThat(cell.getBorderWidth()).isEqualTo(0.5f);
        }

        @Test
        @DisplayName("默认边框颜色为黑色")
        void testDefaultBorderColor() {
            PdfCell cell = PdfCell.of("Test");

            assertThat(cell.getBorderColor()).isEqualTo(PdfColor.BLACK);
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("空字符串内容")
        void testEmptyContent() {
            PdfCell cell = PdfCell.of("");

            assertThat(cell.getContent()).isEmpty();
        }

        @Test
        @DisplayName("大 colspan 值")
        void testLargeColspan() {
            PdfCell cell = PdfCell.builder().colspan(100);

            assertThat(cell.getColspan()).isEqualTo(100);
        }

        @Test
        @DisplayName("大 rowspan 值")
        void testLargeRowspan() {
            PdfCell cell = PdfCell.builder().rowspan(50);

            assertThat(cell.getRowspan()).isEqualTo(50);
        }

        @Test
        @DisplayName("零内边距")
        void testZeroPadding() {
            PdfCell cell = PdfCell.builder().padding(0f);

            assertThat(cell.getPadding()).isEqualTo(0f);
        }
    }

    @Nested
    @DisplayName("final 类测试")
    class FinalClassTests {

        @Test
        @DisplayName("PdfCell 是 final 类")
        void testIsFinalClass() {
            assertThat(java.lang.reflect.Modifier.isFinal(PdfCell.class.getModifiers())).isTrue();
        }
    }
}
