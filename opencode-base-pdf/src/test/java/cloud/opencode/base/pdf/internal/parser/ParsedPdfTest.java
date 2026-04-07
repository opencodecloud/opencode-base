package cloud.opencode.base.pdf.internal.parser;

import cloud.opencode.base.pdf.document.DocumentBuilder;
import cloud.opencode.base.pdf.exception.OpenPdfException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * ParsedPdf Test - Tests for PDF structure parsing and stream decoding
 * ParsedPdf 测试 - PDF 结构解析和流解码测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
@DisplayName("ParsedPdf 测试")
class ParsedPdfTest {

    private byte[] generatePdf(String text) {
        return DocumentBuilder.create()
                .title("Test Document")
                .author("Test Author")
                .addPage()
                    .text(text, 100, 700)
                .endPage()
                .toBytes();
    }

    @Nested
    @DisplayName("对象解析")
    class ObjectResolutionTests {

        @Test
        @DisplayName("resolveObject 解析间接对象")
        void testResolveObject() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            // The Root reference in trailer should be resolvable
            PdfObject rootRef = parsed.getTrailer().get("Root");
            assertThat(rootRef).isInstanceOf(PdfObject.PdfReference.class);
            int objNum = ((PdfObject.PdfReference) rootRef).objectNumber();

            PdfObject resolved = parsed.resolveObject(objNum);
            assertThat(resolved).isInstanceOf(PdfObject.PdfDictionary.class);
        }

        @Test
        @DisplayName("resolveObject 缓存命中")
        void testResolveObjectCached() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            PdfObject rootRef = parsed.getTrailer().get("Root");
            int objNum = ((PdfObject.PdfReference) rootRef).objectNumber();

            PdfObject first = parsed.resolveObject(objNum);
            PdfObject second = parsed.resolveObject(objNum);
            assertThat(first).isSameAs(second); // Should be cached
        }

        @Test
        @DisplayName("resolve 非引用返回自身")
        void testResolveNonRef() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            PdfObject.PdfString str = new PdfObject.PdfString("test");
            assertThat(parsed.resolve(str)).isSameAs(str);
        }

        @Test
        @DisplayName("resolveObject 对无效偏移返回 PdfNull")
        void testResolveInvalidOffset() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            // Object number 99999 shouldn't exist, xref offset will be -1
            PdfObject result = parsed.resolveObject(99999);
            assertThat(result).isInstanceOf(PdfObject.PdfNull.class);
        }
    }

    @Nested
    @DisplayName("页面树解析")
    class PageTreeTests {

        @Test
        @DisplayName("单页 PDF 返回正确页面")
        void testSinglePage() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            List<PdfObject.PdfDictionary> pages = parsed.getPages();
            assertThat(pages).hasSize(1);
            assertThat(pages.getFirst().getString("Type")).isEqualTo("Page");
        }

        @Test
        @DisplayName("多页 PDF 返回所有页面")
        void testMultiplePages() {
            byte[] pdfData = DocumentBuilder.create()
                    .addPage().text("P1", 100, 700).endPage()
                    .addPage().text("P2", 100, 700).endPage()
                    .addPage().text("P3", 100, 700).endPage()
                    .toBytes();
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            List<PdfObject.PdfDictionary> pages = parsed.getPages();
            assertThat(pages).hasSize(3);
        }

        @Test
        @DisplayName("页面有 MediaBox")
        void testPageHasMediaBox() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            PdfObject.PdfDictionary page = parsed.getPages().getFirst();
            PdfObject.PdfArray mediaBox = page.getArray("MediaBox");
            assertThat(mediaBox).isNotNull();
            assertThat(mediaBox.size()).isEqualTo(4);
        }

        @Test
        @DisplayName("页面有 Contents")
        void testPageHasContents() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            PdfObject.PdfDictionary page = parsed.getPages().getFirst();
            PdfObject contentsRef = page.get("Contents");
            assertThat(contentsRef).isNotNull();
        }
    }

    @Nested
    @DisplayName("流解码")
    class StreamDecodingTests {

        @Test
        @DisplayName("解析含 FlateDecode 流的 PDF")
        void testFlateDecodeStream() {
            // DocumentBuilder generates PDFs with FlateDecode streams
            byte[] pdfData = generatePdf("Compressed Content");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            PdfObject.PdfDictionary page = parsed.getPages().getFirst();
            PdfObject contentsRef = page.get("Contents");
            PdfObject resolved = parsed.resolve(contentsRef);

            // Content stream should be a PdfStream after decompression
            assertThat(resolved).isInstanceOf(PdfObject.PdfStream.class);
            PdfObject.PdfStream stream = (PdfObject.PdfStream) resolved;
            assertThat(stream.dataLength()).isGreaterThan(0);
        }

        @Test
        @DisplayName("解码后流包含文本操作符")
        void testDecodedStreamContainsText() {
            byte[] pdfData = generatePdf("Hello World");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            PdfObject.PdfDictionary page = parsed.getPages().getFirst();
            PdfObject contentsRef = page.get("Contents");
            PdfObject resolved = parsed.resolve(contentsRef);

            assertThat(resolved).isInstanceOf(PdfObject.PdfStream.class);
            String content = new String(((PdfObject.PdfStream) resolved).data(), StandardCharsets.US_ASCII);
            // Should contain text operators like BT, Tj, ET
            assertThat(content).contains("BT");
        }
    }

    @Nested
    @DisplayName("文档信息")
    class DocumentInfoTests {

        @Test
        @DisplayName("getInfo 返回非空信息")
        void testGetInfo() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            PdfObject.PdfDictionary info = parsed.getInfo();
            assertThat(info).isNotNull();
            assertThat(info.getString("Title")).isEqualTo("Test Document");
        }

        @Test
        @DisplayName("isEncrypted 返回 false")
        void testIsEncrypted() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            assertThat(parsed.isEncrypted()).isFalse();
        }

        @Test
        @DisplayName("getTrailer 包含 Root")
        void testTrailerHasRoot() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            assertThat(parsed.getTrailer().containsKey("Root")).isTrue();
        }

        @Test
        @DisplayName("getTrailer 包含 Size")
        void testTrailerHasSize() {
            byte[] pdfData = generatePdf("Hello");
            PdfParser.ParsedPdf parsed = PdfParser.parse(pdfData);

            assertThat(parsed.getTrailer().containsKey("Size")).isTrue();
        }
    }

    @Nested
    @DisplayName("PdfObject 辅助方法")
    class PdfObjectHelperTests {

        @Test
        @DisplayName("PdfDictionary getString 返回 Name 值")
        void testGetStringFromName() {
            PdfObject.PdfDictionary dict = new PdfObject.PdfDictionary(
                    Map.of("Type", new PdfObject.PdfName("Page")));

            assertThat(dict.getString("Type")).isEqualTo("Page");
        }

        @Test
        @DisplayName("PdfDictionary getString 返回 String 值")
        void testGetStringFromString() {
            PdfObject.PdfDictionary dict = new PdfObject.PdfDictionary(
                    Map.of("Title", new PdfObject.PdfString("My Doc")));

            assertThat(dict.getString("Title")).isEqualTo("My Doc");
        }

        @Test
        @DisplayName("PdfDictionary getString 对非字符串返回 null")
        void testGetStringReturnsNull() {
            PdfObject.PdfDictionary dict = new PdfObject.PdfDictionary(
                    Map.of("Count", new PdfObject.PdfNumber(5)));

            assertThat(dict.getString("Count")).isNull();
            assertThat(dict.getString("NonExistent")).isNull();
        }

        @Test
        @DisplayName("PdfDictionary getInt 使用默认值")
        void testGetIntDefault() {
            PdfObject.PdfDictionary dict = new PdfObject.PdfDictionary(Map.of());

            assertThat(dict.getInt("Missing", 42)).isEqualTo(42);
        }

        @Test
        @DisplayName("PdfDictionary getFloat 使用默认值")
        void testGetFloatDefault() {
            PdfObject.PdfDictionary dict = new PdfObject.PdfDictionary(Map.of());

            assertThat(dict.getFloat("Missing", 3.14f)).isEqualTo(3.14f);
        }

        @Test
        @DisplayName("PdfDictionary getFloat 返回数值")
        void testGetFloatValue() {
            PdfObject.PdfDictionary dict = new PdfObject.PdfDictionary(
                    Map.of("Width", new PdfObject.PdfNumber(595.0)));

            assertThat(dict.getFloat("Width", 0f)).isEqualTo(595.0f);
        }

        @Test
        @DisplayName("PdfDictionary getDictionary 返回嵌套字典")
        void testGetDictionary() {
            PdfObject.PdfDictionary inner = new PdfObject.PdfDictionary(
                    Map.of("Key", new PdfObject.PdfString("Value")));
            PdfObject.PdfDictionary outer = new PdfObject.PdfDictionary(
                    Map.of("Inner", inner));

            assertThat(outer.getDictionary("Inner")).isEqualTo(inner);
            assertThat(outer.getDictionary("Missing")).isNull();
        }

        @Test
        @DisplayName("PdfDictionary getDictionary 对非字典返回 null")
        void testGetDictionaryNonDict() {
            PdfObject.PdfDictionary dict = new PdfObject.PdfDictionary(
                    Map.of("Key", new PdfObject.PdfNumber(1)));

            assertThat(dict.getDictionary("Key")).isNull();
        }

        @Test
        @DisplayName("PdfDictionary getBoolean 返回布尔值")
        void testGetBoolean() {
            PdfObject.PdfDictionary dict = new PdfObject.PdfDictionary(
                    Map.of("Flag", new PdfObject.PdfBoolean(true)));

            assertThat(dict.getBoolean("Flag", false)).isTrue();
            assertThat(dict.getBoolean("Missing", false)).isFalse();
        }

        @Test
        @DisplayName("PdfDictionary getBoolean 对非布尔返回默认值")
        void testGetBooleanNonBool() {
            PdfObject.PdfDictionary dict = new PdfObject.PdfDictionary(
                    Map.of("Key", new PdfObject.PdfNumber(1)));

            assertThat(dict.getBoolean("Key", true)).isTrue();
        }

        @Test
        @DisplayName("PdfDictionary size 返回条目数")
        void testDictionarySize() {
            PdfObject.PdfDictionary dict = new PdfObject.PdfDictionary(
                    Map.of("A", new PdfObject.PdfNull(), "B", new PdfObject.PdfNull()));

            assertThat(dict.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("PdfNumber intValue/floatValue/longValue")
        void testPdfNumberConversions() {
            PdfObject.PdfNumber num = new PdfObject.PdfNumber(42.7);

            assertThat(num.intValue()).isEqualTo(42);
            assertThat(num.floatValue()).isCloseTo(42.7f, within(0.01f));
            assertThat(num.longValue()).isEqualTo(42L);
        }

        @Test
        @DisplayName("PdfString rawBytes")
        void testPdfStringRawBytes() {
            PdfObject.PdfString str = new PdfObject.PdfString("Hello");

            assertThat(str.rawBytes()).isEqualTo("Hello".getBytes(StandardCharsets.ISO_8859_1));
        }

        @Test
        @DisplayName("PdfArray isEmpty 和 size")
        void testPdfArrayEmptyAndSize() {
            PdfObject.PdfArray empty = new PdfObject.PdfArray(List.of());
            assertThat(empty.isEmpty()).isTrue();
            assertThat(empty.size()).isEqualTo(0);

            PdfObject.PdfArray nonEmpty = new PdfObject.PdfArray(List.of(new PdfObject.PdfNull()));
            assertThat(nonEmpty.isEmpty()).isFalse();
            assertThat(nonEmpty.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("PdfStream dataLength 和 data")
        void testPdfStreamDataLength() {
            byte[] testData = "test data".getBytes();
            PdfObject.PdfDictionary dict = new PdfObject.PdfDictionary(Map.of());
            PdfObject.PdfStream stream = new PdfObject.PdfStream(dict, testData);

            assertThat(stream.dataLength()).isEqualTo(testData.length);
            assertThat(stream.data()).isEqualTo(testData);
            // Data should be defensively copied
            assertThat(stream.data()).isNotSameAs(testData);
        }

        @Test
        @DisplayName("PdfDictionary getArray 对非数组返回 null")
        void testGetArrayNonArray() {
            PdfObject.PdfDictionary dict = new PdfObject.PdfDictionary(
                    Map.of("Key", new PdfObject.PdfNumber(1)));

            assertThat(dict.getArray("Key")).isNull();
        }

        @Test
        @DisplayName("PdfDictionary getInt 返回数字值")
        void testGetIntValue() {
            PdfObject.PdfDictionary dict = new PdfObject.PdfDictionary(
                    Map.of("Count", new PdfObject.PdfNumber(10)));

            assertThat(dict.getInt("Count", 0)).isEqualTo(10);
        }

        @Test
        @DisplayName("PdfDictionary getInt 对非数字返回默认值")
        void testGetIntNonNumber() {
            PdfObject.PdfDictionary dict = new PdfObject.PdfDictionary(
                    Map.of("Key", new PdfObject.PdfString("text")));

            assertThat(dict.getInt("Key", 99)).isEqualTo(99);
        }
    }

    @Nested
    @DisplayName("头部验证")
    class HeaderValidationTests {

        @Test
        @DisplayName("无 PDF 头部的数据抛出异常")
        void testMissingHeader() {
            byte[] data = "This is not a PDF file with enough length to avoid too-short check".getBytes();
            assertThatThrownBy(() -> PdfParser.parse(data))
                    .isInstanceOf(OpenPdfException.class);
        }

        @Test
        @DisplayName("无效的 PDF 版本抛出异常")
        void testInvalidVersion() {
            byte[] data = "%PDF-XX followed by enough data to pass min length check. some more random content here padding".getBytes();
            assertThatThrownBy(() -> PdfParser.parse(data))
                    .isInstanceOf(OpenPdfException.class);
        }

        @Test
        @DisplayName("找不到 startxref 抛出异常")
        void testMissingStartxref() {
            byte[] data = "%PDF-1.7\nsome content without startxref or xref table present in this document data".getBytes();
            assertThatThrownBy(() -> PdfParser.parse(data))
                    .isInstanceOf(OpenPdfException.class);
        }
    }
}
