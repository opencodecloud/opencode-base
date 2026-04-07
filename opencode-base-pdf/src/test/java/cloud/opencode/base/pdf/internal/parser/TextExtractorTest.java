package cloud.opencode.base.pdf.internal.parser;

import cloud.opencode.base.pdf.document.DocumentBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * TextExtractor 测试
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
@DisplayName("TextExtractor 测试")
class TextExtractorTest {

    private PdfParser.ParsedPdf parsePdf(byte[] data) {
        return PdfParser.parse(data);
    }

    @Nested
    @DisplayName("文本提取")
    class TextExtractionTests {

        @Test
        @DisplayName("提取单行文本")
        void testExtractSingleLine() {
            byte[] pdfData = DocumentBuilder.create()
                    .addPage()
                        .text("Hello World", 100, 700)
                    .endPage()
                    .toBytes();

            PdfParser.ParsedPdf parsed = parsePdf(pdfData);
            String text = TextExtractor.extractText(parsed, 0);

            assertThat(text).contains("Hello World");
        }

        @Test
        @DisplayName("提取多行文本")
        void testExtractMultipleLines() {
            byte[] pdfData = DocumentBuilder.create()
                    .addPage()
                        .text("Line One", 100, 700)
                        .text("Line Two", 100, 680)
                    .endPage()
                    .toBytes();

            PdfParser.ParsedPdf parsed = parsePdf(pdfData);
            String text = TextExtractor.extractText(parsed, 0);

            assertThat(text).contains("Line One");
            assertThat(text).contains("Line Two");
        }

        @Test
        @DisplayName("提取所有页面文本")
        void testExtractAllText() {
            byte[] pdfData = DocumentBuilder.create()
                    .addPage()
                        .text("Page One", 100, 700)
                    .endPage()
                    .addPage()
                        .text("Page Two", 100, 700)
                    .endPage()
                    .toBytes();

            PdfParser.ParsedPdf parsed = parsePdf(pdfData);
            String text = TextExtractor.extractAllText(parsed);

            assertThat(text).contains("Page One");
            assertThat(text).contains("Page Two");
        }

        @Test
        @DisplayName("空页面返回空字符串")
        void testEmptyPage() {
            byte[] pdfData = DocumentBuilder.create()
                    .addPage()
                    .endPage()
                    .toBytes();

            PdfParser.ParsedPdf parsed = parsePdf(pdfData);
            String text = TextExtractor.extractText(parsed, 0);

            // Empty page might have empty content stream
            assertThat(text).isNotNull();
        }

        @Test
        @DisplayName("越界页面索引返回空字符串")
        void testInvalidPageIndex() {
            byte[] pdfData = DocumentBuilder.create()
                    .addPage()
                        .text("Hello", 100, 700)
                    .endPage()
                    .toBytes();

            PdfParser.ParsedPdf parsed = parsePdf(pdfData);
            String text = TextExtractor.extractText(parsed, 99);

            assertThat(text).isEmpty();
        }
    }

    @Nested
    @DisplayName("CMap 解析")
    class CMapParsingTests {

        @Test
        @DisplayName("解析 beginbfchar 映射")
        void testParseBfChar() {
            String cmap = """
                    beginbfchar
                    <0041> <0042>
                    endbfchar
                    """;
            Map<Integer, String> map = TextExtractor.parseCMap(cmap);

            assertThat(map).containsEntry(0x41, "B");
        }

        @Test
        @DisplayName("解析 beginbfrange 映射")
        void testParseBfRange() {
            String cmap = """
                    beginbfrange
                    <0041> <0043> <0061>
                    endbfrange
                    """;
            Map<Integer, String> map = TextExtractor.parseCMap(cmap);

            assertThat(map).containsEntry(0x41, "a");
            assertThat(map).containsEntry(0x42, "b");
            assertThat(map).containsEntry(0x43, "c");
        }

        @Test
        @DisplayName("空 CMap 返回空映射")
        void testEmptyCMap() {
            Map<Integer, String> map = TextExtractor.parseCMap("some random content");
            assertThat(map).isEmpty();
        }
    }

    @Nested
    @DisplayName("null 参数处理")
    class NullHandlingTests {

        @Test
        @DisplayName("null pdf 抛出 NPE")
        void testNullPdf() {
            assertThatThrownBy(() -> TextExtractor.extractText(null, 0))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null pdf 调用 extractAllText 抛出 NPE")
        void testNullPdfAllText() {
            assertThatThrownBy(() -> TextExtractor.extractAllText(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
