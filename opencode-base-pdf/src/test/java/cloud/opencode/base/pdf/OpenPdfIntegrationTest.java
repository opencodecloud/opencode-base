package cloud.opencode.base.pdf;

import cloud.opencode.base.pdf.document.DocumentBuilder;
import cloud.opencode.base.pdf.document.Metadata;
import cloud.opencode.base.pdf.exception.OpenPdfException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenPdf Integration Test - Tests OpenPdf facade methods with real PDF documents
 * OpenPdf 集成测试 - 使用真实 PDF 文档测试 OpenPdf 门面方法
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
@DisplayName("OpenPdf 集成测试")
class OpenPdfIntegrationTest {

    @TempDir
    Path tempDir;

    private byte[] createSimplePdf() {
        return DocumentBuilder.create()
                .title("Test PDF")
                .author("Test Author")
                .addPage()
                    .text("Hello World", 100, 700)
                .endPage()
                .toBytes();
    }

    private byte[] createMultiPagePdf() {
        return DocumentBuilder.create()
                .title("Multi Page")
                .addPage()
                    .text("Page One Content", 100, 700)
                .endPage()
                .addPage()
                    .text("Page Two Content", 100, 700)
                .endPage()
                .addPage()
                    .text("Page Three Content", 100, 700)
                .endPage()
                .toBytes();
    }

    private Path writePdf(byte[] data, String name) throws Exception {
        Path file = tempDir.resolve(name);
        Files.write(file, data);
        return file;
    }

    @Nested
    @DisplayName("open 方法测试")
    class OpenMethodTests {

        @Test
        @DisplayName("从 Path 打开 PDF")
        void testOpenFromPath() throws Exception {
            Path file = writePdf(createSimplePdf(), "test.pdf");

            try (PdfDocument doc = OpenPdf.open(file)) {
                assertThat(doc).isNotNull();
                assertThat(doc.getPageCount()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("从 InputStream 打开 PDF")
        void testOpenFromInputStream() {
            byte[] data = createSimplePdf();

            try (PdfDocument doc = OpenPdf.open(new ByteArrayInputStream(data))) {
                assertThat(doc).isNotNull();
                assertThat(doc.getPageCount()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("从字节数组打开 PDF")
        void testOpenFromBytes() {
            byte[] data = createSimplePdf();

            try (PdfDocument doc = OpenPdf.open(data)) {
                assertThat(doc).isNotNull();
                assertThat(doc.getPageCount()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("open(Path, password) 抛出 UnsupportedOperationException")
        void testOpenWithPassword() throws Exception {
            Path file = writePdf(createSimplePdf(), "test.pdf");

            assertThatThrownBy(() -> OpenPdf.open(file, "password"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("getPageCount 方法测试")
    class GetPageCountTests {

        @Test
        @DisplayName("单页 PDF 返回 1")
        void testSinglePage() throws Exception {
            Path file = writePdf(createSimplePdf(), "single.pdf");

            assertThat(OpenPdf.getPageCount(file)).isEqualTo(1);
        }

        @Test
        @DisplayName("多页 PDF 返回正确页数")
        void testMultiPage() throws Exception {
            Path file = writePdf(createMultiPagePdf(), "multi.pdf");

            assertThat(OpenPdf.getPageCount(file)).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("getMetadata 方法测试")
    class GetMetadataTests {

        @Test
        @DisplayName("获取标题和作者")
        void testGetMetadata() throws Exception {
            Path file = writePdf(createSimplePdf(), "meta.pdf");

            Metadata meta = OpenPdf.getMetadata(file);

            assertThat(meta).isNotNull();
            assertThat(meta.title()).isEqualTo("Test PDF");
            assertThat(meta.author()).isEqualTo("Test Author");
        }
    }

    @Nested
    @DisplayName("isEncrypted 方法测试")
    class IsEncryptedTests {

        @Test
        @DisplayName("普通 PDF 不是加密的")
        void testNotEncrypted() throws Exception {
            Path file = writePdf(createSimplePdf(), "plain.pdf");

            assertThat(OpenPdf.isEncrypted(file)).isFalse();
        }
    }

    @Nested
    @DisplayName("hasForm 方法测试")
    class HasFormTests {

        @Test
        @DisplayName("普通 PDF 无表单")
        void testNoForm() throws Exception {
            Path file = writePdf(createSimplePdf(), "noform.pdf");

            assertThat(OpenPdf.hasForm(file)).isFalse();
        }
    }

    @Nested
    @DisplayName("isSigned 方法测试")
    class IsSignedTests {

        @Test
        @DisplayName("普通 PDF 未签名")
        void testNotSigned() throws Exception {
            Path file = writePdf(createSimplePdf(), "unsigned.pdf");

            assertThat(OpenPdf.isSigned(file)).isFalse();
        }

        @Test
        @DisplayName("不存在的文件抛出异常")
        void testSignedNonExistentFile() {
            Path fake = tempDir.resolve("nonexistent.pdf");

            assertThatThrownBy(() -> OpenPdf.isSigned(fake))
                    .isInstanceOf(OpenPdfException.class);
        }

        @Test
        @DisplayName("多页 PDF 未签名")
        void testMultiPageNotSigned() throws Exception {
            Path file = writePdf(createMultiPagePdf(), "multi_unsigned.pdf");

            assertThat(OpenPdf.isSigned(file)).isFalse();
        }
    }

    @Nested
    @DisplayName("extractText 方法测试")
    class ExtractTextTests {

        @Test
        @DisplayName("提取单页文本")
        void testExtractText() throws Exception {
            Path file = writePdf(createSimplePdf(), "text.pdf");

            String text = OpenPdf.extractText(file);

            assertThat(text).contains("Hello World");
        }

        @Test
        @DisplayName("提取多页文本")
        void testExtractMultiPageText() throws Exception {
            Path file = writePdf(createMultiPagePdf(), "multitext.pdf");

            String text = OpenPdf.extractText(file);

            assertThat(text).contains("Page One Content");
            assertThat(text).contains("Page Two Content");
            assertThat(text).contains("Page Three Content");
        }
    }

    @Nested
    @DisplayName("表单操作不支持测试")
    class FormOperationUnsupportedTests {

        @Test
        @DisplayName("fillForm 抛出 UnsupportedOperationException")
        void testFillForm() throws Exception {
            Path file = writePdf(createSimplePdf(), "form.pdf");

            assertThatThrownBy(() -> OpenPdf.fillForm(file, Map.of("key", "val")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("fillAndFlatten 抛出 UnsupportedOperationException")
        void testFillAndFlatten() throws Exception {
            Path file = writePdf(createSimplePdf(), "flatten.pdf");

            assertThatThrownBy(() -> OpenPdf.fillAndFlatten(file, Map.of("key", "val")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("extractFormFields 抛出 UnsupportedOperationException")
        void testExtractFormFields() throws Exception {
            Path file = writePdf(createSimplePdf(), "fields.pdf");

            assertThatThrownBy(() -> OpenPdf.extractFormFields(file))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
