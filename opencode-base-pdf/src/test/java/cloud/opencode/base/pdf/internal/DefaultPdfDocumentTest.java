package cloud.opencode.base.pdf.internal;

import cloud.opencode.base.pdf.PdfDocument;
import cloud.opencode.base.pdf.PdfPage;
import cloud.opencode.base.pdf.document.DocumentBuilder;
import cloud.opencode.base.pdf.document.Metadata;
import cloud.opencode.base.pdf.document.PageSize;
import cloud.opencode.base.pdf.exception.OpenPdfException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultPdfDocument 端到端测试
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
@DisplayName("DefaultPdfDocument 测试")
class DefaultPdfDocumentTest {

    @TempDir
    Path tempDir;

    private byte[] createTestPdf() {
        return DocumentBuilder.create()
                .title("Test Title")
                .author("Test Author")
                .subject("Test Subject")
                .creator("Test Creator")
                .addPage()
                    .text("Hello World", 100, 700)
                    .text("Second Line", 100, 680)
                .endPage()
                .addPage()
                    .text("Page Two Content", 100, 700)
                .endPage()
                .toBytes();
    }

    @Nested
    @DisplayName("打开文档")
    class OpenTests {

        @Test
        @DisplayName("从字节数组打开")
        void testOpenFromBytes() {
            byte[] pdfData = createTestPdf();
            DefaultPdfDocument doc = DefaultPdfDocument.open(pdfData);

            assertThat(doc).isNotNull();
            assertThat(doc.getPageCount()).isEqualTo(2);
            doc.close();
        }

        @Test
        @DisplayName("从文件路径打开")
        void testOpenFromPath() throws Exception {
            byte[] pdfData = createTestPdf();
            Path file = tempDir.resolve("test.pdf");
            Files.write(file, pdfData);

            DefaultPdfDocument doc = DefaultPdfDocument.open(file);
            assertThat(doc.getPageCount()).isEqualTo(2);
            doc.close();
        }

        @Test
        @DisplayName("从输入流打开")
        void testOpenFromInputStream() {
            byte[] pdfData = createTestPdf();
            InputStream is = new ByteArrayInputStream(pdfData);

            DefaultPdfDocument doc = DefaultPdfDocument.open(is);
            assertThat(doc.getPageCount()).isEqualTo(2);
            doc.close();
        }

        @Test
        @DisplayName("null 字节数组抛出 NPE")
        void testOpenNullBytes() {
            assertThatThrownBy(() -> DefaultPdfDocument.open((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null 路径抛出 NPE")
        void testOpenNullPath() {
            assertThatThrownBy(() -> DefaultPdfDocument.open((Path) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null 输入流抛出 NPE")
        void testOpenNullStream() {
            assertThatThrownBy(() -> DefaultPdfDocument.open((InputStream) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("不存在的文件抛出异常")
        void testOpenNonExistentFile() {
            Path fake = tempDir.resolve("nonexistent.pdf");
            assertThatThrownBy(() -> DefaultPdfDocument.open(fake))
                    .isInstanceOf(OpenPdfException.class);
        }
    }

    @Nested
    @DisplayName("页面访问")
    class PageAccessTests {

        @Test
        @DisplayName("获取页数")
        void testGetPageCount() {
            byte[] pdfData = createTestPdf();
            try (var doc = DefaultPdfDocument.open(pdfData)) {
                assertThat(doc.getPageCount()).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("获取指定页面")
        void testGetPage() {
            byte[] pdfData = createTestPdf();
            try (var doc = DefaultPdfDocument.open(pdfData)) {
                PdfPage page1 = doc.getPage(1);
                assertThat(page1).isNotNull();
                assertThat(page1.getPageNumber()).isEqualTo(1);

                PdfPage page2 = doc.getPage(2);
                assertThat(page2.getPageNumber()).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("无效页码抛出异常")
        void testGetInvalidPage() {
            byte[] pdfData = createTestPdf();
            try (var doc = DefaultPdfDocument.open(pdfData)) {
                assertThatThrownBy(() -> doc.getPage(0))
                        .isInstanceOf(OpenPdfException.class);
                assertThatThrownBy(() -> doc.getPage(3))
                        .isInstanceOf(OpenPdfException.class);
            }
        }

        @Test
        @DisplayName("获取所有页面")
        void testGetPages() {
            byte[] pdfData = createTestPdf();
            try (var doc = DefaultPdfDocument.open(pdfData)) {
                assertThat(doc.getPages()).hasSize(2);
            }
        }
    }

    @Nested
    @DisplayName("元数据")
    class MetadataTests {

        @Test
        @DisplayName("获取标题")
        void testGetTitle() {
            byte[] pdfData = createTestPdf();
            try (var doc = DefaultPdfDocument.open(pdfData)) {
                Metadata meta = doc.getMetadata();
                assertThat(meta.title()).isEqualTo("Test Title");
            }
        }

        @Test
        @DisplayName("获取作者")
        void testGetAuthor() {
            byte[] pdfData = createTestPdf();
            try (var doc = DefaultPdfDocument.open(pdfData)) {
                Metadata meta = doc.getMetadata();
                assertThat(meta.author()).isEqualTo("Test Author");
            }
        }

        @Test
        @DisplayName("获取主题")
        void testGetSubject() {
            byte[] pdfData = createTestPdf();
            try (var doc = DefaultPdfDocument.open(pdfData)) {
                Metadata meta = doc.getMetadata();
                assertThat(meta.subject()).isEqualTo("Test Subject");
            }
        }

        @Test
        @DisplayName("获取创建者")
        void testGetCreator() {
            byte[] pdfData = createTestPdf();
            try (var doc = DefaultPdfDocument.open(pdfData)) {
                Metadata meta = doc.getMetadata();
                assertThat(meta.creator()).isEqualTo("Test Creator");
            }
        }
    }

    @Nested
    @DisplayName("文本提取")
    class TextExtractionTests {

        @Test
        @DisplayName("从第一页提取文本")
        void testExtractTextPage1() {
            byte[] pdfData = createTestPdf();
            try (var doc = DefaultPdfDocument.open(pdfData)) {
                String text = doc.getPage(1).extractText();
                assertThat(text).contains("Hello World");
            }
        }

        @Test
        @DisplayName("从第二页提取文本")
        void testExtractTextPage2() {
            byte[] pdfData = createTestPdf();
            try (var doc = DefaultPdfDocument.open(pdfData)) {
                String text = doc.getPage(2).extractText();
                assertThat(text).contains("Page Two Content");
            }
        }
    }

    @Nested
    @DisplayName("页面属性")
    class PagePropertiesTests {

        @Test
        @DisplayName("获取页面宽度和高度")
        void testPageDimensions() {
            byte[] pdfData = createTestPdf();
            try (var doc = DefaultPdfDocument.open(pdfData)) {
                PdfPage page = doc.getPage(1);
                // A4 default: 595 x 842
                assertThat(page.getWidth()).isCloseTo(595f, within(2f));
                assertThat(page.getHeight()).isCloseTo(842f, within(2f));
            }
        }

        @Test
        @DisplayName("获取页面大小")
        void testPageSize() {
            byte[] pdfData = createTestPdf();
            try (var doc = DefaultPdfDocument.open(pdfData)) {
                PdfPage page = doc.getPage(1);
                assertThat(page.getPageSize()).isEqualTo(PageSize.A4);
            }
        }
    }

    @Nested
    @DisplayName("保存操作")
    class SaveTests {

        @Test
        @DisplayName("保存到文件")
        void testSaveToFile() throws Exception {
            byte[] pdfData = createTestPdf();
            Path outFile = tempDir.resolve("output.pdf");

            try (var doc = DefaultPdfDocument.open(pdfData)) {
                doc.save(outFile);
            }

            assertThat(Files.exists(outFile)).isTrue();
            assertThat(Files.size(outFile)).isGreaterThan(0);

            // Re-open and verify
            try (var doc = DefaultPdfDocument.open(outFile)) {
                assertThat(doc.getPageCount()).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("保存到输出流")
        void testSaveToStream() {
            byte[] pdfData = createTestPdf();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try (var doc = DefaultPdfDocument.open(pdfData)) {
                doc.save(out);
            }

            assertThat(out.size()).isGreaterThan(0);
        }

        @Test
        @DisplayName("toBytes 返回 PDF 数据")
        void testToBytes() {
            byte[] pdfData = createTestPdf();
            try (var doc = DefaultPdfDocument.open(pdfData)) {
                byte[] bytes = doc.toBytes();
                assertThat(bytes).isNotEmpty();
                assertThat(new String(bytes, 0, 5)).startsWith("%PDF-");
            }
        }
    }

    @Nested
    @DisplayName("加密检查")
    class EncryptionTests {

        @Test
        @DisplayName("非加密 PDF 返回 false")
        void testNotEncrypted() {
            byte[] pdfData = createTestPdf();
            try (var doc = DefaultPdfDocument.open(pdfData)) {
                assertThat(doc.isEncrypted()).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("表单检查")
    class FormTests {

        @Test
        @DisplayName("无表单 PDF 返回 false")
        void testNoForm() {
            byte[] pdfData = createTestPdf();
            try (var doc = DefaultPdfDocument.open(pdfData)) {
                assertThat(doc.hasForm()).isFalse();
            }
        }

        @Test
        @DisplayName("getForm 返回 null")
        void testGetFormNull() {
            byte[] pdfData = createTestPdf();
            try (var doc = DefaultPdfDocument.open(pdfData)) {
                assertThat(doc.getForm()).isNull();
            }
        }
    }

    @Nested
    @DisplayName("不支持的写操作")
    class UnsupportedWriteTests {

        @Test
        @DisplayName("addPage 抛出 UnsupportedOperationException")
        void testAddPage() {
            byte[] pdfData = createTestPdf();
            try (var doc = DefaultPdfDocument.open(pdfData)) {
                assertThatThrownBy(doc::addPage)
                        .isInstanceOf(UnsupportedOperationException.class);
            }
        }

        @Test
        @DisplayName("setMetadata 抛出 UnsupportedOperationException")
        void testSetMetadata() {
            byte[] pdfData = createTestPdf();
            try (var doc = DefaultPdfDocument.open(pdfData)) {
                assertThatThrownBy(() -> doc.setMetadata(Metadata.empty()))
                        .isInstanceOf(UnsupportedOperationException.class);
            }
        }

        @Test
        @DisplayName("setPassword 抛出 UnsupportedOperationException")
        void testSetPassword() {
            byte[] pdfData = createTestPdf();
            try (var doc = DefaultPdfDocument.open(pdfData)) {
                assertThatThrownBy(() -> doc.setPassword("user", "owner"))
                        .isInstanceOf(UnsupportedOperationException.class);
            }
        }
    }

    @Nested
    @DisplayName("关闭后操作")
    class ClosedDocumentTests {

        @Test
        @DisplayName("关闭后 getPageCount 抛出异常")
        void testClosedGetPageCount() {
            byte[] pdfData = createTestPdf();
            var doc = DefaultPdfDocument.open(pdfData);
            doc.close();

            assertThatThrownBy(doc::getPageCount)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("closed");
        }
    }
}
