package cloud.opencode.base.pdf.content;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfTable 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PdfTable 测试")
class PdfTableTest {

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of 创建指定列数表格")
        void testOf() {
            PdfTable table = PdfTable.of(3);

            assertThat(table).isNotNull();
            assertThat(table.getColumns()).isEqualTo(3);
        }

        @Test
        @DisplayName("builder 创建表格构建器")
        void testBuilder() {
            PdfTable.Builder builder = PdfTable.builder(4);

            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder 方法测试")
    class BuilderMethodTests {

        @Test
        @DisplayName("columnWidths 设置列宽")
        void testColumnWidths() {
            PdfTable table = PdfTable.builder(3)
                .columnWidths(100f, 200f, 150f)
                .build();

            assertThat(table.getColumnWidths()).containsExactly(100f, 200f, 150f);
        }

        @Test
        @DisplayName("header 添加表头行")
        void testHeader() {
            PdfTable table = PdfTable.builder(3)
                .header("ID", "Name", "Price")
                .build();

            assertThat(table.getHeaderRows()).hasSize(1);
        }

        @Test
        @DisplayName("row 添加数据行")
        void testRow() {
            PdfTable table = PdfTable.builder(3)
                .row("1", "Product A", "$100")
                .row("2", "Product B", "$200")
                .build();

            assertThat(table.getDataRows()).hasSize(2);
        }

        @Test
        @DisplayName("row 使用 PdfCell 添加行")
        void testRowWithCells() {
            PdfCell cell1 = PdfCell.of("Cell 1");
            PdfCell cell2 = PdfCell.of("Cell 2");

            PdfTable table = PdfTable.builder(2)
                .row(cell1, cell2)
                .build();

            assertThat(table.getDataRows()).hasSize(1);
        }

        @Test
        @DisplayName("borderWidth 设置边框宽度")
        void testBorderWidth() {
            PdfTable table = PdfTable.builder(2)
                .borderWidth(2.0f)
                .build();

            assertThat(table.getBorderWidth()).isEqualTo(2.0f);
        }

        @Test
        @DisplayName("borderColor 设置边框颜色")
        void testBorderColor() {
            PdfTable table = PdfTable.builder(2)
                .borderColor(PdfColor.GRAY)
                .build();

            assertThat(table.getBorderColor()).isEqualTo(PdfColor.GRAY);
        }

        @Test
        @DisplayName("headerBackground 设置表头背景色")
        void testHeaderBackground() {
            PdfTable table = PdfTable.builder(2)
                .headerBackground(PdfColor.GRAY)
                .build();

            assertThat(table.getHeaderBackground()).isEqualTo(PdfColor.GRAY);
        }

        @Test
        @DisplayName("cellPadding 设置单元格内边距")
        void testCellPadding() {
            PdfTable table = PdfTable.builder(2)
                .cellPadding(10f)
                .build();

            assertThat(table.getCellPadding()).isEqualTo(10f);
        }

        @Test
        @DisplayName("build 构建表格")
        void testBuild() {
            PdfTable table = PdfTable.builder(3)
                .columnWidths(100f, 200f, 100f)
                .header("A", "B", "C")
                .row("1", "2", "3")
                .build();

            assertThat(table).isNotNull();
            assertThat(table.getColumns()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("表格内容方法测试")
    class TableContentTests {

        @Test
        @DisplayName("position 设置位置")
        void testPosition() {
            PdfTable table = PdfTable.of(2).position(100f, 500f);

            assertThat(table.getX()).isEqualTo(100f);
            assertThat(table.getY()).isEqualTo(500f);
        }

        @Test
        @DisplayName("width 设置表格宽度")
        void testWidth() {
            PdfTable table = PdfTable.of(2).width(400f);

            assertThat(table.getWidth()).isEqualTo(400f);
        }

        @Test
        @DisplayName("columnWidths 设置列宽数组")
        void testColumnWidthsArray() {
            PdfTable table = PdfTable.of(3).columnWidths(100f, 150f, 200f);

            assertThat(table.getColumnWidths()).containsExactly(100f, 150f, 200f);
        }

        @Test
        @DisplayName("header 添加表头")
        void testTableHeader() {
            PdfTable table = PdfTable.of(2)
                .header("Col1", "Col2");

            assertThat(table.getHeaderRows()).isNotEmpty();
        }

        @Test
        @DisplayName("header 使用 PdfCell 添加表头")
        void testTableHeaderWithCells() {
            PdfCell cell1 = PdfCell.of("Header 1");
            PdfCell cell2 = PdfCell.of("Header 2");
            PdfTable table = PdfTable.of(2)
                .header(cell1, cell2);

            assertThat(table.getHeaderRows()).hasSize(1);
        }

        @Test
        @DisplayName("row 添加行")
        void testTableRow() {
            PdfTable table = PdfTable.of(2)
                .row("A", "B")
                .row("C", "D");

            assertThat(table.getDataRows()).hasSize(2);
        }

        @Test
        @DisplayName("headerBackground 设置表头背景色")
        void testHeaderBackgroundOnTable() {
            PdfTable table = PdfTable.of(2).headerBackground(PdfColor.GRAY);

            assertThat(table.getHeaderBackground()).isEqualTo(PdfColor.GRAY);
        }

        @Test
        @DisplayName("alternateRowColors 设置交替行颜色")
        void testAlternateRowColors() {
            PdfTable table = PdfTable.of(2).alternateRowColors(PdfColor.WHITE, PdfColor.GRAY);

            assertThat(table.getOddRowColor()).isEqualTo(PdfColor.WHITE);
            assertThat(table.getEvenRowColor()).isEqualTo(PdfColor.GRAY);
        }

        @Test
        @DisplayName("cellPadding 设置单元格内边距")
        void testCellPaddingOnTable() {
            PdfTable table = PdfTable.of(2).cellPadding(10f);

            assertThat(table.getCellPadding()).isEqualTo(10f);
        }

        @Test
        @DisplayName("borderWidth 设置边框宽度")
        void testBorderWidthOnTable() {
            PdfTable table = PdfTable.of(2).borderWidth(2f);

            assertThat(table.getBorderWidth()).isEqualTo(2f);
        }

        @Test
        @DisplayName("borderColor 设置边框颜色")
        void testBorderColorOnTable() {
            PdfTable table = PdfTable.of(2).borderColor(PdfColor.RED);

            assertThat(table.getBorderColor()).isEqualTo(PdfColor.RED);
        }
    }

    @Nested
    @DisplayName("PdfElement 接口测试")
    class PdfElementInterfaceTests {

        @Test
        @DisplayName("实现 PdfElement 接口")
        void testImplementsPdfElement() {
            PdfTable table = PdfTable.of(2);

            assertThat(table).isInstanceOf(PdfElement.class);
        }

        @Test
        @DisplayName("getX 返回正确值")
        void testGetX() {
            PdfTable table = PdfTable.of(2).position(150f, 200f);

            assertThat(table.getX()).isEqualTo(150f);
        }

        @Test
        @DisplayName("getY 返回正确值")
        void testGetY() {
            PdfTable table = PdfTable.of(2).position(150f, 200f);

            assertThat(table.getY()).isEqualTo(200f);
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentApiTests {

        @Test
        @DisplayName("完整的链式调用")
        void testFullFluentApi() {
            PdfTable table = PdfTable.of(3)
                .position(50f, 700f)
                .width(500f)
                .columnWidths(100f, 200f, 200f)
                .header("ID", "Name", "Price")
                .row("1", "Product A", "$99")
                .row("2", "Product B", "$149")
                .borderWidth(0.5f)
                .borderColor(PdfColor.GRAY)
                .headerBackground(PdfColor.GRAY)
                .cellPadding(5f);

            assertThat(table.getColumns()).isEqualTo(3);
            assertThat(table.getX()).isEqualTo(50f);
            assertThat(table.getWidth()).isEqualTo(500f);
            assertThat(table.getBorderWidth()).isEqualTo(0.5f);
        }

        @Test
        @DisplayName("返回相同实例")
        void testReturnsSameInstance() {
            PdfTable table = PdfTable.of(2);

            assertThat(table.position(0, 0)).isSameAs(table);
            assertThat(table.width(100)).isSameAs(table);
            assertThat(table.borderWidth(1)).isSameAs(table);
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认边框宽度为 0.5")
        void testDefaultBorderWidth() {
            PdfTable table = PdfTable.of(2);

            assertThat(table.getBorderWidth()).isEqualTo(0.5f);
        }

        @Test
        @DisplayName("默认边框颜色为黑色")
        void testDefaultBorderColor() {
            PdfTable table = PdfTable.of(2);

            assertThat(table.getBorderColor()).isEqualTo(PdfColor.BLACK);
        }

        @Test
        @DisplayName("默认单元格内边距为 5")
        void testDefaultCellPadding() {
            PdfTable table = PdfTable.of(2);

            assertThat(table.getCellPadding()).isEqualTo(5f);
        }

        @Test
        @DisplayName("默认无表头背景色")
        void testDefaultNoHeaderBackground() {
            PdfTable table = PdfTable.of(2);

            assertThat(table.getHeaderBackground()).isNull();
        }
    }

    @Nested
    @DisplayName("final 类测试")
    class FinalClassTests {

        @Test
        @DisplayName("PdfTable 是 final 类")
        void testIsFinalClass() {
            assertThat(java.lang.reflect.Modifier.isFinal(PdfTable.class.getModifiers())).isTrue();
        }
    }
}
