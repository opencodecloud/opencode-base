package cloud.opencode.base.pdf.internal;

import cloud.opencode.base.pdf.PdfPage;
import cloud.opencode.base.pdf.content.*;
import cloud.opencode.base.pdf.document.DocumentBuilder;
import cloud.opencode.base.pdf.document.Orientation;
import cloud.opencode.base.pdf.document.PageSize;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultPdfPage Test - Tests for read-only PDF page implementation
 * DefaultPdfPage 测试 - 只读 PDF 页面实现测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
@DisplayName("DefaultPdfPage 测试")
class DefaultPdfPageTest {

    private DefaultPdfDocument openTestPdf() {
        byte[] data = DocumentBuilder.create()
                .addPage()
                    .text("Hello World", 100, 700)
                .endPage()
                .toBytes();
        return DefaultPdfDocument.open(data);
    }

    private DefaultPdfDocument openLetterPdf() {
        byte[] data = DocumentBuilder.create(PageSize.LETTER)
                .addPage()
                    .text("Letter size", 100, 700)
                .endPage()
                .toBytes();
        return DefaultPdfDocument.open(data);
    }

    @Nested
    @DisplayName("页面属性")
    class PagePropertiesTests {

        @Test
        @DisplayName("获取页码")
        void testGetPageNumber() {
            try (var doc = openTestPdf()) {
                PdfPage page = doc.getPage(1);
                assertThat(page.getPageNumber()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("获取 A4 页面大小")
        void testGetA4PageSize() {
            try (var doc = openTestPdf()) {
                PdfPage page = doc.getPage(1);
                assertThat(page.getPageSize()).isEqualTo(PageSize.A4);
            }
        }

        @Test
        @DisplayName("获取 LETTER 页面大小")
        void testGetLetterPageSize() {
            try (var doc = openLetterPdf()) {
                PdfPage page = doc.getPage(1);
                assertThat(page.getPageSize()).isEqualTo(PageSize.LETTER);
            }
        }

        @Test
        @DisplayName("获取 A4 页面宽度")
        void testGetWidth() {
            try (var doc = openTestPdf()) {
                PdfPage page = doc.getPage(1);
                assertThat(page.getWidth()).isCloseTo(595f, within(2f));
            }
        }

        @Test
        @DisplayName("获取 A4 页面高度")
        void testGetHeight() {
            try (var doc = openTestPdf()) {
                PdfPage page = doc.getPage(1);
                assertThat(page.getHeight()).isCloseTo(842f, within(2f));
            }
        }

        @Test
        @DisplayName("纵向 PDF 的方向")
        void testPortraitOrientation() {
            try (var doc = openTestPdf()) {
                PdfPage page = doc.getPage(1);
                assertThat(page.getOrientation()).isEqualTo(Orientation.PORTRAIT);
            }
        }

        @Test
        @DisplayName("获取旋转角度（默认0度）")
        void testGetRotation() {
            try (var doc = openTestPdf()) {
                PdfPage page = doc.getPage(1);
                assertThat(page.getRotation()).isEqualTo(0);
            }
        }
    }

    @Nested
    @DisplayName("文本提取")
    class TextExtractionTests {

        @Test
        @DisplayName("从页面提取文本")
        void testExtractText() {
            try (var doc = openTestPdf()) {
                PdfPage page = doc.getPage(1);
                String text = page.extractText();
                assertThat(text).contains("Hello World");
            }
        }
    }

    @Nested
    @DisplayName("只读操作抛出异常")
    class ReadOnlyOperationsTests {

        @Test
        @DisplayName("setRotation 抛出异常")
        void testSetRotation() {
            try (var doc = openTestPdf()) {
                PdfPage page = doc.getPage(1);
                assertThatThrownBy(() -> page.setRotation(90))
                        .isInstanceOf(UnsupportedOperationException.class);
            }
        }

        @Test
        @DisplayName("addText(String, float, float) 抛出异常")
        void testAddTextString() {
            try (var doc = openTestPdf()) {
                PdfPage page = doc.getPage(1);
                assertThatThrownBy(() -> page.addText("test", 0, 0))
                        .isInstanceOf(UnsupportedOperationException.class);
            }
        }

        @Test
        @DisplayName("addText(PdfText) 抛出异常")
        void testAddPdfText() {
            try (var doc = openTestPdf()) {
                PdfPage page = doc.getPage(1);
                PdfText text = PdfText.of("test", 0, 0);
                assertThatThrownBy(() -> page.addText(text))
                        .isInstanceOf(UnsupportedOperationException.class);
            }
        }

        @Test
        @DisplayName("addParagraph 抛出异常")
        void testAddParagraph() {
            try (var doc = openTestPdf()) {
                PdfPage page = doc.getPage(1);
                PdfParagraph para = PdfParagraph.of("test", 0, 0, 100);
                assertThatThrownBy(() -> page.addParagraph(para))
                        .isInstanceOf(UnsupportedOperationException.class);
            }
        }

        @Test
        @DisplayName("addImage 抛出异常")
        void testAddImage() {
            try (var doc = openTestPdf()) {
                PdfPage page = doc.getPage(1);
                PdfImage image = PdfImage.from(new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}, PdfImage.ImageFormat.PNG);
                assertThatThrownBy(() -> page.addImage(image))
                        .isInstanceOf(UnsupportedOperationException.class);
            }
        }

        @Test
        @DisplayName("addTable 抛出异常")
        void testAddTable() {
            try (var doc = openTestPdf()) {
                PdfPage page = doc.getPage(1);
                PdfTable table = PdfTable.builder(2)
                        .columnWidths(100f, 100f)
                        .build();
                assertThatThrownBy(() -> page.addTable(table))
                        .isInstanceOf(UnsupportedOperationException.class);
            }
        }

        @Test
        @DisplayName("addLine 抛出异常")
        void testAddLine() {
            try (var doc = openTestPdf()) {
                PdfPage page = doc.getPage(1);
                PdfLine line = PdfLine.of(0, 0, 100, 100);
                assertThatThrownBy(() -> page.addLine(line))
                        .isInstanceOf(UnsupportedOperationException.class);
            }
        }

        @Test
        @DisplayName("addRectangle 抛出异常")
        void testAddRectangle() {
            try (var doc = openTestPdf()) {
                PdfPage page = doc.getPage(1);
                PdfRectangle rect = PdfRectangle.of(0, 0, 100, 100);
                assertThatThrownBy(() -> page.addRectangle(rect))
                        .isInstanceOf(UnsupportedOperationException.class);
            }
        }
    }

    @Nested
    @DisplayName("DefaultPdfDocument 扩展测试")
    class DefaultPdfDocumentExtendedTests {

        @Test
        @DisplayName("addPage(PageSize) 抛出异常")
        void testAddPageWithSize() {
            try (var doc = openTestPdf()) {
                assertThatThrownBy(() -> doc.addPage(PageSize.LETTER))
                        .isInstanceOf(UnsupportedOperationException.class);
            }
        }

        @Test
        @DisplayName("insertPage 抛出异常")
        void testInsertPage() {
            try (var doc = openTestPdf()) {
                assertThatThrownBy(() -> doc.insertPage(1, null))
                        .isInstanceOf(UnsupportedOperationException.class);
            }
        }

        @Test
        @DisplayName("removePage 抛出异常")
        void testRemovePage() {
            try (var doc = openTestPdf()) {
                assertThatThrownBy(() -> doc.removePage(1))
                        .isInstanceOf(UnsupportedOperationException.class);
            }
        }

        @Test
        @DisplayName("getParsed 返回非空")
        void testGetParsed() {
            try (var doc = openTestPdf()) {
                assertThat(doc.getParsed()).isNotNull();
            }
        }

        @Test
        @DisplayName("getMetadata 缓存结果")
        void testGetMetadataCached() {
            try (var doc = openTestPdf()) {
                var meta1 = doc.getMetadata();
                var meta2 = doc.getMetadata();
                assertThat(meta1).isSameAs(meta2);
            }
        }

        @Test
        @DisplayName("关闭后 getMetadata 抛出异常")
        void testClosedGetMetadata() {
            var doc = openTestPdf();
            doc.close();

            assertThatThrownBy(doc::getMetadata)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("关闭后 getPages 抛出异常")
        void testClosedGetPages() {
            var doc = openTestPdf();
            doc.close();

            assertThatThrownBy(doc::getPages)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("关闭后 isEncrypted 抛出异常")
        void testClosedIsEncrypted() {
            var doc = openTestPdf();
            doc.close();

            assertThatThrownBy(doc::isEncrypted)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("关闭后 hasForm 抛出异常")
        void testClosedHasForm() {
            var doc = openTestPdf();
            doc.close();

            assertThatThrownBy(doc::hasForm)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("关闭后 toBytes 抛出异常")
        void testClosedToBytes() {
            var doc = openTestPdf();
            doc.close();

            assertThatThrownBy(doc::toBytes)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("关闭后 save(Path) 抛出异常")
        void testClosedSavePath() {
            var doc = openTestPdf();
            doc.close();

            assertThatThrownBy(() -> doc.save(java.nio.file.Path.of("/tmp/test.pdf")))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("关闭后 save(OutputStream) 抛出异常")
        void testClosedSaveStream() {
            var doc = openTestPdf();
            doc.close();

            assertThatThrownBy(() -> doc.save(new java.io.ByteArrayOutputStream()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("关闭后 getPage 抛出异常")
        void testClosedGetPage() {
            var doc = openTestPdf();
            doc.close();

            assertThatThrownBy(() -> doc.getPage(1))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("关闭后 getForm 抛出异常")
        void testClosedGetForm() {
            var doc = openTestPdf();
            doc.close();

            assertThatThrownBy(doc::getForm)
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
