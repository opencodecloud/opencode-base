package cloud.opencode.base.pdf.internal.parser;

import cloud.opencode.base.pdf.document.DocumentBuilder;
import cloud.opencode.base.pdf.exception.OpenPdfException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfParser 测试
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
@DisplayName("PdfParser 测试")
class PdfParserTest {

    /** Generates a simple PDF with given text content using DocumentBuilder */
    private byte[] generatePdf(String text) {
        return DocumentBuilder.create()
                .title("Test Document")
                .author("Test Author")
                .addPage()
                    .text(text, 100, 700)
                .endPage()
                .toBytes();
    }

    /** Generates a multi-page PDF */
    private byte[] generateMultiPagePdf(String... texts) {
        DocumentBuilder builder = DocumentBuilder.create()
                .title("Multi Page Test");
        for (int i = 0; i < texts.length; i++) {
            if (i == 0) {
                builder.addPage()
                        .text(texts[i], 100, 700)
                        .endPage();
            } else {
                builder.addPage()
                        .text(texts[i], 100, 700)
                        .endPage();
            }
        }
        return builder.toBytes();
    }

    @Nested
    @DisplayName("PDF 结构解析")
    class StructureParsingTests {

        @Test
        @DisplayName("解析有效 PDF 返回非空结果")
        void testParseValidPdf() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            assertThat(parsed).isNotNull();
            assertThat(parsed.getTrailer()).isNotNull();
        }

        @Test
        @DisplayName("trailer 包含 Root 和 Size")
        void testTrailerContents() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            assertThat(parsed.getTrailer().containsKey("Root")).isTrue();
            assertThat(parsed.getTrailer().containsKey("Size")).isTrue();
        }

        @Test
        @DisplayName("解析 null 数据抛出 NPE")
        void testParseNull() {
            assertThatThrownBy(() -> PdfParser.parse(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("解析过短的数据抛出异常")
        void testParseTooShort() {
            assertThatThrownBy(() -> PdfParser.parse(new byte[5]))
                    .isInstanceOf(OpenPdfException.class);
        }

        @Test
        @DisplayName("解析非 PDF 数据抛出异常")
        void testParseInvalidData() {
            assertThatThrownBy(() -> PdfParser.parse("Not a PDF file at all".getBytes()))
                    .isInstanceOf(OpenPdfException.class);
        }
    }

    @Nested
    @DisplayName("页面树解析")
    class PageTreeTests {

        @Test
        @DisplayName("单页 PDF 返回 1 个页面")
        void testSinglePage() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            List<PdfObject.PdfDictionary> pages = parsed.getPages();
            assertThat(pages).hasSize(1);
        }

        @Test
        @DisplayName("两页 PDF 返回 2 个页面")
        void testTwoPages() {
            byte[] pdfData = generateMultiPagePdf("Page 1", "Page 2");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            List<PdfObject.PdfDictionary> pages = parsed.getPages();
            assertThat(pages).hasSize(2);
        }

        @Test
        @DisplayName("页面包含 Type=Page")
        void testPageType() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            PdfObject.PdfDictionary page = parsed.getPages().getFirst();
            assertThat(page.getString("Type")).isEqualTo("Page");
        }

        @Test
        @DisplayName("页面有 MediaBox")
        void testPageMediaBox() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            PdfObject.PdfDictionary page = parsed.getPages().getFirst();
            PdfObject.PdfArray mediaBox = page.getArray("MediaBox");
            assertThat(mediaBox).isNotNull();
            assertThat(mediaBox.size()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("对象解析")
    class ObjectResolutionTests {

        @Test
        @DisplayName("resolve 间接引用返回实际对象")
        void testResolveReference() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            // Root is an indirect reference in trailer
            PdfObject rootRef = parsed.getTrailer().get("Root");
            assertThat(rootRef).isInstanceOf(PdfObject.PdfReference.class);

            PdfObject root = parsed.resolve(rootRef);
            assertThat(root).isInstanceOf(PdfObject.PdfDictionary.class);
        }

        @Test
        @DisplayName("resolve 非引用对象返回自身")
        void testResolveNonReference() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            PdfObject.PdfNumber num = new PdfObject.PdfNumber(42);
            assertThat(parsed.resolve(num)).isSameAs(num);
        }
    }

    @Nested
    @DisplayName("文档信息")
    class DocumentInfoTests {

        @Test
        @DisplayName("获取 Info 字典")
        void testGetInfo() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            PdfObject.PdfDictionary info = parsed.getInfo();
            assertThat(info).isNotNull();
        }

        @Test
        @DisplayName("Info 字典包含 Title")
        void testInfoTitle() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            PdfObject.PdfDictionary info = parsed.getInfo();
            assertThat(info).isNotNull();
            String title = info.getString("Title");
            assertThat(title).isEqualTo("Test Document");
        }

        @Test
        @DisplayName("Info 字典包含 Author")
        void testInfoAuthor() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            PdfObject.PdfDictionary info = parsed.getInfo();
            assertThat(info).isNotNull();
            String author = info.getString("Author");
            assertThat(author).isEqualTo("Test Author");
        }

        @Test
        @DisplayName("非加密 PDF isEncrypted 返回 false")
        void testNotEncrypted() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            assertThat(parsed.isEncrypted()).isFalse();
        }
    }
}
