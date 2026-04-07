package cloud.opencode.base.pdf.internal.parser;

import cloud.opencode.base.pdf.document.DocumentBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * TextExtractor Expanded Test - Additional tests for text extraction operator processing
 * TextExtractor 扩展测试 - 文本提取操作符处理的附加测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
@DisplayName("TextExtractor 扩展测试")
class TextExtractorExpandedTest {

    private PdfParser.ParsedPdf parsePdf(byte[] data) {
        return PdfParser.parse(data);
    }

    @Nested
    @DisplayName("多行文本提取")
    class MultiLineExtractionTests {

        @Test
        @DisplayName("多行文本正确换行")
        void testMultiLineText() {
            byte[] pdfData = DocumentBuilder.create()
                    .addPage()
                        .text("First Line", 100, 700)
                        .text("Second Line", 100, 680)
                        .text("Third Line", 100, 660)
                    .endPage()
                    .toBytes();

            PdfParser.ParsedPdf parsed = parsePdf(pdfData);
            String text = TextExtractor.extractText(parsed, 0);

            assertThat(text).contains("First Line");
            assertThat(text).contains("Second Line");
            assertThat(text).contains("Third Line");
        }

        @Test
        @DisplayName("同一行不同 x 位置的文本")
        void testSameLineText() {
            byte[] pdfData = DocumentBuilder.create()
                    .addPage()
                        .text("Left", 100, 700)
                        .text("Right", 300, 700)
                    .endPage()
                    .toBytes();

            PdfParser.ParsedPdf parsed = parsePdf(pdfData);
            String text = TextExtractor.extractText(parsed, 0);

            assertThat(text).contains("Left");
            assertThat(text).contains("Right");
        }
    }

    @Nested
    @DisplayName("多页文本提取")
    class MultiPageExtractionTests {

        @Test
        @DisplayName("extractAllText 包含所有页面文本")
        void testExtractAllTextMultiPage() {
            byte[] pdfData = DocumentBuilder.create()
                    .addPage()
                        .text("Page One", 100, 700)
                    .endPage()
                    .addPage()
                        .text("Page Two", 100, 700)
                    .endPage()
                    .addPage()
                        .text("Page Three", 100, 700)
                    .endPage()
                    .toBytes();

            PdfParser.ParsedPdf parsed = parsePdf(pdfData);
            String text = TextExtractor.extractAllText(parsed);

            assertThat(text).contains("Page One");
            assertThat(text).contains("Page Two");
            assertThat(text).contains("Page Three");
        }

        @Test
        @DisplayName("extractText 负索引返回空字符串")
        void testNegativePageIndex() {
            byte[] pdfData = DocumentBuilder.create()
                    .addPage()
                        .text("Hello", 100, 700)
                    .endPage()
                    .toBytes();

            PdfParser.ParsedPdf parsed = parsePdf(pdfData);
            String text = TextExtractor.extractText(parsed, -1);

            assertThat(text).isEmpty();
        }
    }

    @Nested
    @DisplayName("CMap 解析扩展")
    class CMapExtendedTests {

        @Test
        @DisplayName("多个 bfchar 区段")
        void testMultipleBfCharSections() {
            String cmap = """
                    beginbfchar
                    <0041> <0042>
                    endbfchar
                    beginbfchar
                    <0043> <0044>
                    endbfchar
                    """;
            Map<Integer, String> map = TextExtractor.parseCMap(cmap);

            assertThat(map).containsEntry(0x41, "B");
            assertThat(map).containsEntry(0x43, "D");
        }

        @Test
        @DisplayName("多个 bfrange 区段")
        void testMultipleBfRangeSections() {
            String cmap = """
                    beginbfrange
                    <0041> <0043> <0061>
                    endbfrange
                    beginbfrange
                    <0050> <0052> <0070>
                    endbfrange
                    """;
            Map<Integer, String> map = TextExtractor.parseCMap(cmap);

            assertThat(map).containsEntry(0x41, "a");
            assertThat(map).containsEntry(0x42, "b");
            assertThat(map).containsEntry(0x43, "c");
            assertThat(map).containsEntry(0x50, "p");
            assertThat(map).containsEntry(0x51, "q");
            assertThat(map).containsEntry(0x52, "r");
        }

        @Test
        @DisplayName("bfrange 起始大于结束时跳过")
        void testBfRangeStartGreaterThanEnd() {
            String cmap = """
                    beginbfrange
                    <0043> <0041> <0061>
                    endbfrange
                    """;
            Map<Integer, String> map = TextExtractor.parseCMap(cmap);

            // Invalid range (start > end) should be skipped
            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("bfchar 和 bfrange 混合")
        void testMixedBfCharAndRange() {
            String cmap = """
                    beginbfchar
                    <0020> <0020>
                    endbfchar
                    beginbfrange
                    <0041> <005A> <0041>
                    endbfrange
                    """;
            Map<Integer, String> map = TextExtractor.parseCMap(cmap);

            assertThat(map).containsEntry(0x20, " ");
            assertThat(map).containsEntry(0x41, "A");
            assertThat(map).containsEntry(0x5A, "Z");
        }

        @Test
        @DisplayName("不完整的 bfchar 区段（无 end 标记）")
        void testIncompleteBfCharSection() {
            String cmap = "beginbfchar\n<0041> <0042>\n"; // missing endbfchar
            Map<Integer, String> map = TextExtractor.parseCMap(cmap);

            // Should handle gracefully, possibly empty
            assertThat(map).isNotNull();
        }

        @Test
        @DisplayName("不完整的 bfrange 区段（无 end 标记）")
        void testIncompleteBfRangeSection() {
            String cmap = "beginbfrange\n<0041> <0043> <0061>\n"; // missing endbfrange
            Map<Integer, String> map = TextExtractor.parseCMap(cmap);

            assertThat(map).isNotNull();
        }

        @Test
        @DisplayName("2位十六进制 Unicode 映射")
        void testTwoDigitHexUnicode() {
            String cmap = """
                    beginbfchar
                    <01> <41>
                    endbfchar
                    """;
            Map<Integer, String> map = TextExtractor.parseCMap(cmap);

            assertThat(map).containsEntry(1, "A");
        }
    }

    @Nested
    @DisplayName("空页面和边界条件")
    class EdgeCaseTests {

        @Test
        @DisplayName("空 PDF（无文本内容）")
        void testEmptyPdf() {
            byte[] pdfData = DocumentBuilder.create()
                    .addPage()
                    .endPage()
                    .toBytes();

            PdfParser.ParsedPdf parsed = parsePdf(pdfData);
            String text = TextExtractor.extractText(parsed, 0);

            assertThat(text).isNotNull();
        }

        @Test
        @DisplayName("extractAllText 单页")
        void testExtractAllTextSinglePage() {
            byte[] pdfData = DocumentBuilder.create()
                    .addPage()
                        .text("Only Page", 100, 700)
                    .endPage()
                    .toBytes();

            PdfParser.ParsedPdf parsed = parsePdf(pdfData);
            String text = TextExtractor.extractAllText(parsed);

            assertThat(text).contains("Only Page");
        }

        @Test
        @DisplayName("长文本内容提取")
        void testLongText() {
            String longText = "A".repeat(500);
            byte[] pdfData = DocumentBuilder.create()
                    .addPage()
                        .text(longText, 100, 700)
                    .endPage()
                    .toBytes();

            PdfParser.ParsedPdf parsed = parsePdf(pdfData);
            String text = TextExtractor.extractText(parsed, 0);

            assertThat(text).isNotNull();
            assertThat(text.length()).isGreaterThan(0);
        }
    }
}
