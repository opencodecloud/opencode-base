package cloud.opencode.base.pdf.document;

import cloud.opencode.base.pdf.content.*;
import cloud.opencode.base.pdf.font.PdfFont;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * PageBuilder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("PageBuilder 测试")
class PageBuilderTest {

    @Nested
    @DisplayName("文本添加测试")
    class TextAdditionTests {

        @Test
        @DisplayName("text(String, float, float) 添加简单文本")
        void testAddSimpleText() {
            DocumentBuilder docBuilder = DocumentBuilder.create();
            PageBuilder pageBuilder = docBuilder.addPage();

            pageBuilder.text("Hello World", 100f, 700f);

            assertThat(pageBuilder.getElements()).hasSize(1);
            assertThat(pageBuilder.getElements().getFirst()).isInstanceOf(PdfText.class);
        }

        @Test
        @DisplayName("text(String, float, float) null 抛出异常")
        void testAddSimpleTextNull() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();

            assertThatThrownBy(() -> pageBuilder.text(null, 100f, 700f))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("text(String, float, float, PdfFont, float) 添加带样式文本")
        void testAddStyledText() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();

            pageBuilder.text("Styled", 100f, 700f, PdfFont.helveticaBold(), 14f);

            assertThat(pageBuilder.getElements()).hasSize(1);
        }

        @Test
        @DisplayName("text(PdfText) 添加 PdfText 元素")
        void testAddPdfText() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();
            PdfText pdfText = PdfText.of("Test", 50f, 600f);

            pageBuilder.text(pdfText);

            assertThat(pageBuilder.getElements()).hasSize(1);
            assertThat(pageBuilder.getElements().getFirst()).isEqualTo(pdfText);
        }

        @Test
        @DisplayName("text(PdfText) null 抛出异常")
        void testAddPdfTextNull() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();

            assertThatThrownBy(() -> pageBuilder.text((PdfText) null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("paragraph 添加段落")
        void testAddParagraph() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();
            PdfParagraph paragraph = PdfParagraph.of("Long text...", 50f, 700f, 500f);

            pageBuilder.paragraph(paragraph);

            assertThat(pageBuilder.getElements()).hasSize(1);
            assertThat(pageBuilder.getElements().getFirst()).isEqualTo(paragraph);
        }

        @Test
        @DisplayName("paragraph null 抛出异常")
        void testAddParagraphNull() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();

            assertThatThrownBy(() -> pageBuilder.paragraph(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("图像添加测试")
    class ImageAdditionTests {

        @Test
        @DisplayName("image(Path, float, float) 添加图像")
        void testAddImageByPath() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();

            pageBuilder.image(Path.of("/images/logo.png"), 100f, 500f);

            assertThat(pageBuilder.getElements()).hasSize(1);
            assertThat(pageBuilder.getElements().getFirst()).isInstanceOf(PdfImage.class);
        }

        @Test
        @DisplayName("image(Path, float, float) null 抛出异常")
        void testAddImageByPathNull() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();

            assertThatThrownBy(() -> pageBuilder.image((Path) null, 100f, 500f))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("image(Path, float, float, float, float) 添加指定大小的图像")
        void testAddImageWithSize() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();

            pageBuilder.image(Path.of("/images/logo.png"), 100f, 500f, 200f, 100f);

            assertThat(pageBuilder.getElements()).hasSize(1);
        }

        @Test
        @DisplayName("image(PdfImage) 添加 PdfImage 元素")
        void testAddPdfImage() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();
            PdfImage pdfImage = PdfImage.from(Path.of("/img.png"));

            pageBuilder.image(pdfImage);

            assertThat(pageBuilder.getElements()).hasSize(1);
            assertThat(pageBuilder.getElements().getFirst()).isEqualTo(pdfImage);
        }

        @Test
        @DisplayName("image(PdfImage) null 抛出异常")
        void testAddPdfImageNull() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();

            assertThatThrownBy(() -> pageBuilder.image((PdfImage) null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("表格添加测试")
    class TableAdditionTests {

        @Test
        @DisplayName("table 添加表格")
        void testAddTable() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();
            PdfTable table = PdfTable.builder(3).build().position(50f, 600f);

            pageBuilder.table(table);

            assertThat(pageBuilder.getElements()).hasSize(1);
            assertThat(pageBuilder.getElements().getFirst()).isEqualTo(table);
        }

        @Test
        @DisplayName("table null 抛出异常")
        void testAddTableNull() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();

            assertThatThrownBy(() -> pageBuilder.table(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("tableBuilder 创建表格构建器")
        void testTableBuilder() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();
            PdfTable.Builder tableBuilder = pageBuilder.tableBuilder(4);

            assertThat(tableBuilder).isNotNull();
        }
    }

    @Nested
    @DisplayName("图形添加测试")
    class GraphicsAdditionTests {

        @Test
        @DisplayName("line(float, float, float, float) 添加线条")
        void testAddLine() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();

            pageBuilder.line(50f, 700f, 550f, 700f);

            assertThat(pageBuilder.getElements()).hasSize(1);
            assertThat(pageBuilder.getElements().getFirst()).isInstanceOf(PdfLine.class);
        }

        @Test
        @DisplayName("line(PdfLine) 添加线条元素")
        void testAddPdfLine() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();
            PdfLine line = PdfLine.of(100f, 100f, 200f, 200f);

            pageBuilder.line(line);

            assertThat(pageBuilder.getElements()).hasSize(1);
            assertThat(pageBuilder.getElements().getFirst()).isEqualTo(line);
        }

        @Test
        @DisplayName("line(PdfLine) null 抛出异常")
        void testAddPdfLineNull() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();

            assertThatThrownBy(() -> pageBuilder.line(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("rectangle(float, float, float, float) 添加矩形")
        void testAddRectangle() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();

            pageBuilder.rectangle(100f, 500f, 200f, 100f);

            assertThat(pageBuilder.getElements()).hasSize(1);
            assertThat(pageBuilder.getElements().getFirst()).isInstanceOf(PdfRectangle.class);
        }

        @Test
        @DisplayName("filledRectangle 添加填充矩形")
        void testAddFilledRectangle() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();

            pageBuilder.filledRectangle(100f, 500f, 200f, 100f, PdfColor.rgb(200, 200, 200));

            assertThat(pageBuilder.getElements()).hasSize(1);
        }

        @Test
        @DisplayName("filledRectangle null color 抛出异常")
        void testAddFilledRectangleNullColor() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();

            assertThatThrownBy(() -> pageBuilder.filledRectangle(100f, 500f, 200f, 100f, null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("rectangle(PdfRectangle) 添加矩形元素")
        void testAddPdfRectangle() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();
            PdfRectangle rect = PdfRectangle.of(50f, 50f, 100f, 100f);

            pageBuilder.rectangle(rect);

            assertThat(pageBuilder.getElements()).hasSize(1);
            assertThat(pageBuilder.getElements().getFirst()).isEqualTo(rect);
        }

        @Test
        @DisplayName("rectangle(PdfRectangle) null 抛出异常")
        void testAddPdfRectangleNull() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();

            assertThatThrownBy(() -> pageBuilder.rectangle(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("ellipse 添加椭圆")
        void testAddEllipse() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();
            PdfEllipse ellipse = PdfEllipse.of(200f, 400f, 50f, 30f);

            pageBuilder.ellipse(ellipse);

            assertThat(pageBuilder.getElements()).hasSize(1);
            assertThat(pageBuilder.getElements().getFirst()).isEqualTo(ellipse);
        }

        @Test
        @DisplayName("ellipse null 抛出异常")
        void testAddEllipseNull() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();

            assertThatThrownBy(() -> pageBuilder.ellipse(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("页面完成测试")
    class PageCompletionTests {

        @Test
        @DisplayName("endPage 返回文档构建器")
        void testEndPage() {
            DocumentBuilder docBuilder = DocumentBuilder.create();
            PageBuilder pageBuilder = docBuilder.addPage();

            DocumentBuilder result = pageBuilder.endPage();

            assertThat(result).isSameAs(docBuilder);
        }

        @Test
        @DisplayName("nextPage 添加另一个页面")
        void testNextPage() {
            DocumentBuilder docBuilder = DocumentBuilder.create();
            PageBuilder page1 = docBuilder.addPage();
            PageBuilder page2 = page1.nextPage();

            assertThat(page2).isNotSameAs(page1);
            assertThat(docBuilder.getPages()).hasSize(2);
        }

        @Test
        @DisplayName("nextPage(PageSize) 添加指定大小的页面")
        void testNextPageWithSize() {
            DocumentBuilder docBuilder = DocumentBuilder.create();
            PageBuilder page1 = docBuilder.addPage();
            PageBuilder page2 = page1.nextPage(PageSize.A5);

            assertThat(page2.getPageSize()).isEqualTo(PageSize.A5);
        }
    }

    @Nested
    @DisplayName("访问方法测试")
    class AccessorTests {

        @Test
        @DisplayName("getPageSize 返回页面大小")
        void testGetPageSize() {
            PageBuilder pageBuilder = DocumentBuilder.create(PageSize.LETTER).addPage();

            assertThat(pageBuilder.getPageSize()).isEqualTo(PageSize.LETTER);
        }

        @Test
        @DisplayName("getOrientation 返回页面方向")
        void testGetOrientation() {
            DocumentBuilder docBuilder = DocumentBuilder.create()
                .orientation(Orientation.LANDSCAPE);
            PageBuilder pageBuilder = docBuilder.addPage();

            assertThat(pageBuilder.getOrientation()).isEqualTo(Orientation.LANDSCAPE);
        }

        @Test
        @DisplayName("getElements 返回不可变列表")
        void testGetElementsImmutable() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();
            pageBuilder.text("Test", 100f, 700f);

            assertThatThrownBy(() -> pageBuilder.getElements().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getWidth 纵向模式返回页面宽度")
        void testGetWidthPortrait() {
            PageBuilder pageBuilder = DocumentBuilder.create()
                .pageSize(PageSize.A4)
                .orientation(Orientation.PORTRAIT)
                .addPage();

            assertThat(pageBuilder.getWidth()).isEqualTo(PageSize.A4.getWidth());
        }

        @Test
        @DisplayName("getWidth 横向模式返回页面高度")
        void testGetWidthLandscape() {
            PageBuilder pageBuilder = DocumentBuilder.create()
                .pageSize(PageSize.A4)
                .orientation(Orientation.LANDSCAPE)
                .addPage();

            assertThat(pageBuilder.getWidth()).isEqualTo(PageSize.A4.getHeight());
        }

        @Test
        @DisplayName("getHeight 纵向模式返回页面高度")
        void testGetHeightPortrait() {
            PageBuilder pageBuilder = DocumentBuilder.create()
                .pageSize(PageSize.A4)
                .orientation(Orientation.PORTRAIT)
                .addPage();

            assertThat(pageBuilder.getHeight()).isEqualTo(PageSize.A4.getHeight());
        }

        @Test
        @DisplayName("getHeight 横向模式返回页面宽度")
        void testGetHeightLandscape() {
            PageBuilder pageBuilder = DocumentBuilder.create()
                .pageSize(PageSize.A4)
                .orientation(Orientation.LANDSCAPE)
                .addPage();

            assertThat(pageBuilder.getHeight()).isEqualTo(PageSize.A4.getWidth());
        }
    }

    @Nested
    @DisplayName("链式调用测试")
    class FluentApiTests {

        @Test
        @DisplayName("完整的链式调用")
        void testFullFluentApi() {
            DocumentBuilder docBuilder = DocumentBuilder.create();

            docBuilder.addPage()
                .text("Title", 100f, 750f, PdfFont.helveticaBold(), 24f)
                .line(50f, 730f, 550f, 730f)
                .text("Content", 100f, 700f)
                .rectangle(50f, 600f, 500f, 80f)
                .image(Path.of("/logo.png"), 400f, 50f, 100f, 50f)
                .endPage();

            assertThat(docBuilder.getPages()).hasSize(1);
            assertThat(docBuilder.getPages().getFirst().getElements()).hasSize(5);
        }

        @Test
        @DisplayName("链式调用返回相同实例")
        void testChainReturnsSameInstance() {
            PageBuilder pageBuilder = DocumentBuilder.create().addPage();

            assertThat(pageBuilder.text("A", 0, 0)).isSameAs(pageBuilder);
            assertThat(pageBuilder.line(0, 0, 1, 1)).isSameAs(pageBuilder);
            assertThat(pageBuilder.rectangle(0, 0, 1, 1)).isSameAs(pageBuilder);
        }
    }
}
