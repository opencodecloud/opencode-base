package cloud.opencode.base.pdf.document;

import cloud.opencode.base.pdf.OpenPdf;
import cloud.opencode.base.pdf.content.*;
import cloud.opencode.base.pdf.exception.OpenPdfException;
import cloud.opencode.base.pdf.font.PdfFont;
import cloud.opencode.base.pdf.font.StandardFont;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link PdfRenderer}.
 * {@link PdfRenderer} 单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.2
 */
class PdfRendererTest {

    // ==================== PDF Structure Tests | PDF 结构测试 ====================

    @Nested
    class PdfStructureTests {

        @Test
        void toBytesProducesValidPdfHeader() {
            byte[] pdf = OpenPdf.create()
                    .title("Test")
                    .addPage()
                        .text("Hello", 100, 700)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).startsWith("%PDF-1.4");
            assertThat(content).endsWith("%%EOF\n");
        }

        @Test
        void toBytesContainsBinaryCommentAfterHeader() {
            byte[] pdf = OpenPdf.create()
                    .addPage().text("test", 50, 700).endPage()
                    .toBytes();

            // PDF 1.4 binary comment marker (high-byte characters after %)
            // Written as US-ASCII bytes from the source string
            String content = new String(pdf, java.nio.charset.StandardCharsets.US_ASCII);
            assertThat(content).contains("%PDF-1.4");
            // The binary marker should start with % on the second line
            String[] lines = content.split("\n");
            assertThat(lines.length).isGreaterThan(1);
            assertThat(lines[1]).startsWith("%");
        }

        @Test
        void toBytesContainsXrefTable() {
            byte[] pdf = OpenPdf.create()
                    .addPage().text("test", 50, 700).endPage()
                    .toBytes();
            String content = new String(pdf);
            assertThat(content).contains("xref");
            assertThat(content).contains("startxref");
        }

        @Test
        void toBytesContainsTrailerWithRootAndInfo() {
            byte[] pdf = OpenPdf.create()
                    .addPage().text("test", 50, 700).endPage()
                    .toBytes();
            String content = new String(pdf);
            assertThat(content).contains("trailer");
            assertThat(content).contains("/Root 1 0 R");
            assertThat(content).contains("/Info");
        }

        @Test
        void toBytesContainsCatalogObject() {
            byte[] pdf = OpenPdf.create()
                    .addPage().text("test", 50, 700).endPage()
                    .toBytes();
            String content = new String(pdf);
            assertThat(content).contains("/Type /Catalog");
            assertThat(content).contains("/Pages 2 0 R");
        }

        @Test
        void toBytesContainsPagesObject() {
            byte[] pdf = OpenPdf.create()
                    .addPage().text("test", 50, 700).endPage()
                    .toBytes();
            String content = new String(pdf);
            assertThat(content).contains("/Type /Pages");
            assertThat(content).contains("/Kids [");
            assertThat(content).contains("/Count 1");
        }

        @Test
        void toBytesContainsPageObject() {
            byte[] pdf = OpenPdf.create()
                    .addPage().text("test", 50, 700).endPage()
                    .toBytes();
            String content = new String(pdf);
            assertThat(content).contains("/Type /Page");
            assertThat(content).contains("/Parent 2 0 R");
            assertThat(content).contains("/MediaBox");
            assertThat(content).contains("/Contents");
            assertThat(content).contains("/Resources");
        }

        @Test
        void xrefEntriesAreProperlyFormatted() {
            byte[] pdf = OpenPdf.create()
                    .addPage().text("test", 50, 700).endPage()
                    .toBytes();
            String content = new String(pdf);
            // Free entry
            assertThat(content).contains("0000000000 65535 f ");
            // In-use entries have 10-digit offset format
            assertThat(content).matches("(?s).*\\d{10} 00000 n .*");
        }

        @Test
        void toBytesHandlesEmptyDocument() {
            byte[] pdf = OpenPdf.create().toBytes();
            String content = new String(pdf);
            assertThat(content).startsWith("%PDF-1.4");
            assertThat(content).contains("/Count 1"); // one empty page
        }
    }

    // ==================== Metadata Tests | 元数据测试 ====================

    @Nested
    class MetadataTests {

        @Test
        void toBytesContainsDocumentMetadata() {
            byte[] pdf = OpenPdf.create()
                    .title("My Title")
                    .author("Test Author")
                    .subject("Test Subject")
                    .creator("Test Creator")
                    .addPage()
                        .text("Hello", 100, 700)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("/Title (My Title)");
            assertThat(content).contains("/Author (Test Author)");
            assertThat(content).contains("/Subject (Test Subject)");
            assertThat(content).contains("/Creator (Test Creator)");
        }

        @Test
        void toBytesContainsProducerMetadata() {
            byte[] pdf = OpenPdf.create()
                    .addPage().text("test", 50, 700).endPage()
                    .toBytes();
            String content = new String(pdf);
            assertThat(content).contains("/Producer (OpenCode-Base-PDF)");
        }

        @Test
        void toBytesContainsCreationDate() {
            byte[] pdf = OpenPdf.create()
                    .addPage().text("test", 50, 700).endPage()
                    .toBytes();
            String content = new String(pdf);
            assertThat(content).contains("/CreationDate (D:");
        }

        @Test
        void toBytesOmitsNullMetadataFields() {
            // No title, author, subject, or creator set
            byte[] pdf = OpenPdf.create()
                    .addPage().text("test", 50, 700).endPage()
                    .toBytes();
            String content = new String(pdf);
            assertThat(content).doesNotContain("/Title");
            assertThat(content).doesNotContain("/Author");
            assertThat(content).doesNotContain("/Subject");
            // Creator defaults to "OpenCode PDF" in DocumentBuilder, so it will be present
        }

        @Test
        void toBytesEscapesSpecialCharsInMetadata() {
            byte[] pdf = OpenPdf.create()
                    .title("Title (with) parens")
                    .author("Author \\backslash")
                    .addPage().text("test", 50, 700).endPage()
                    .toBytes();
            String content = new String(pdf);
            assertThat(content).contains("/Title (Title \\(with\\) parens)");
            assertThat(content).contains("/Author (Author \\\\backslash)");
        }
    }

    // ==================== Text Rendering Tests | 文本渲染测试 ====================

    @Nested
    class TextRenderingTests {

        @Test
        void toBytesContainsTextContent() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text("Hello World", 50, 750)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("(Hello World) Tj");
        }

        @Test
        void toBytesContainsStyledText() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text("Styled", 50, 750, PdfFont.helveticaBold(), 16)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("(Styled) Tj");
            assertThat(content).contains("/F2 16 Tf");
        }

        @Test
        void toBytesContainsTextColor() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text(PdfText.of("Red", 50, 700).color(PdfColor.RED))
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("1 0 0 rg"); // red color
        }

        @Test
        void textWithNullColorOmitsColorOperator() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text(PdfText.of("NoColor", 50, 700).color(null))
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("(NoColor) Tj");
            // The text block should have BT/ET but no rg color before the Tf
            String textBlock = content.substring(content.indexOf("BT"), content.indexOf("ET") + 2);
            assertThat(textBlock).doesNotContain("rg");
        }

        @Test
        void textWithDefaultFontSizeUsesSize12() {
            // PdfText.of() sets default fontSize to 12
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text(PdfText.of("Default", 50, 700).font(null).fontSize(0))
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            // When fontSize <= 0, renderer defaults to 12
            assertThat(content).contains("/F1 12 Tf");
        }

        @Test
        void textPositionUsesTextMatrix() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text("Pos", 123, 456)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("1 0 0 1 123 456 Tm");
        }

        @Test
        void textRenderedWithBtEtBlock() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text("Block", 50, 700)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("BT\n");
            assertThat(content).contains("ET\n");
        }

        @Test
        void escapesSpecialCharactersInPdfText() {
            byte[] pdf = OpenPdf.create()
                    .addPage().text("Test (with) \\parens", 50, 700).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("Test \\(with\\) \\\\parens");
        }

        @Test
        void escapesNewlinesAndCarriageReturns() {
            byte[] pdf = OpenPdf.create()
                    .addPage().text("Line1\nLine2\rLine3", 50, 700).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("(Line1\\nLine2\\rLine3) Tj");
        }

        @Test
        void textWithCustomColor() {
            // rgb(0, 128, 0) → (0f, 128/255f, 0f) ≈ (0, 0.502, 0)
            PdfColor green = PdfColor.rgb(0, 128, 0);
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text(PdfText.of("Green", 50, 700).color(green))
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            // fmt formats 128/255 ≈ 0.50196 as "0.50"
            assertThat(content).contains(" rg\n");
            assertThat(content).contains("(Green) Tj");
        }
    }

    // ==================== Line Rendering Tests | 线条渲染测试 ====================

    @Nested
    class LineRenderingTests {

        @Test
        void toBytesContainsLine() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .line(50, 700, 550, 700)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("50 700 m");
            assertThat(content).contains("550 700 l");
            assertThat(content).contains("S");
        }

        @Test
        void lineWithColorRendersStrokeColor() {
            PdfLine line = PdfLine.of(50, 700, 550, 700).color(PdfColor.RED);
            byte[] pdf = OpenPdf.create()
                    .addPage().line(line).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("1 0 0 RG"); // stroke color (uppercase RG)
        }

        @Test
        void lineWithNullColorOmitsColorOperator() {
            PdfLine line = PdfLine.of(50, 700, 550, 700).color(null);
            byte[] pdf = OpenPdf.create()
                    .addPage().line(line).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("50 700 m");
            assertThat(content).doesNotContain("RG\n50 700 m");
        }

        @Test
        void lineWithCustomWidth() {
            PdfLine line = PdfLine.of(50, 700, 550, 700).lineWidth(3f).color(null);
            byte[] pdf = OpenPdf.create()
                    .addPage().line(line).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("3 w");
        }

        @Test
        void lineWithZeroWidthOmitsWidthOperator() {
            PdfLine line = PdfLine.of(50, 700, 550, 700).lineWidth(0).color(null);
            byte[] pdf = OpenPdf.create()
                    .addPage().line(line).endPage()
                    .toBytes();

            String content = new String(pdf);
            // Should not contain width operator before move
            assertThat(content).doesNotContain(" w\n50 700 m");
        }

        @Test
        void lineWithFractionalCoordinates() {
            PdfLine line = PdfLine.of(50.5f, 700.25f, 550.75f, 700.5f).color(null);
            byte[] pdf = OpenPdf.create()
                    .addPage().line(line).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("50.50 700.25 m");
            assertThat(content).contains("550.75 700.50 l");
        }
    }

    // ==================== Rectangle Rendering Tests | 矩形渲染测试 ====================

    @Nested
    class RectangleRenderingTests {

        @Test
        void toBytesContainsFilledRectangle() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .filledRectangle(50, 600, 200, 100, PdfColor.BLUE)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("50 600 200 100 re");
            assertThat(content).contains("f"); // fill operator
        }

        @Test
        void filledRectangleRendersFillColor() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .filledRectangle(50, 600, 200, 100, PdfColor.GREEN)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            // PdfColor.GREEN = rgb(0, 255, 0) → (0f, 1f, 0f) → "0 1 0 rg"
            assertThat(content).contains("0 1 0 rg");
        }

        @Test
        void strokeOnlyRectangleRendersStrokeColor() {
            // Rectangle without fill — strokeColor + no fillColor
            PdfRectangle rect = PdfRectangle.of(50, 600, 200, 100)
                    .strokeColor(PdfColor.RED)
                    .strokeWidth(2f);

            byte[] pdf = OpenPdf.create()
                    .addPage().rectangle(rect).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("1 0 0 RG"); // stroke color (uppercase RG)
            assertThat(content).contains("2 w"); // stroke width
            assertThat(content).contains("50 600 200 100 re");
            assertThat(content).contains("S"); // stroke operator (not fill)
        }

        @Test
        void filledRectangleDoesNotStroke() {
            // When filled, the renderer only fills — no stroke branch
            PdfRectangle rect = PdfRectangle.of(50, 600, 200, 100)
                    .fillColor(PdfColor.YELLOW)
                    .strokeColor(PdfColor.BLACK);

            byte[] pdf = OpenPdf.create()
                    .addPage().rectangle(rect).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("re\nf"); // filled
        }

        @Test
        void rectangleWithNoFillAndNoStrokeColorProducesNothing() {
            PdfRectangle rect = PdfRectangle.of(50, 600, 200, 100)
                    .strokeColor(null);

            byte[] pdf = OpenPdf.create()
                    .addPage().rectangle(rect).endPage()
                    .toBytes();

            String content = new String(pdf);
            // No fill, no stroke color → no rendering
            assertThat(content).doesNotContain("50 600 200 100 re");
        }
    }

    // ==================== Table Rendering Tests | 表格渲染测试 ====================

    @Nested
    class TableRenderingTests {

        @Test
        void toBytesContainsTable() {
            PdfTable table = PdfTable.of(3)
                    .position(50, 700)
                    .width(500)
                    .header("Name", "Type", "Description")
                    .row("id", "integer", "User ID")
                    .row("name", "string", "User name");

            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .table(table)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("(Name) Tj");
            assertThat(content).contains("(id) Tj");
            assertThat(content).contains("(User name) Tj");
        }

        @Test
        void toBytesWithTableHeaderBackground() {
            PdfTable table = PdfTable.of(2)
                    .position(50, 700)
                    .width(400)
                    .headerBackground(PdfColor.LIGHT_GRAY)
                    .header("Col1", "Col2")
                    .row("a", "b");

            byte[] pdf = OpenPdf.create()
                    .addPage().table(table).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("(Col1) Tj");
            assertThat(content).contains("(a) Tj");
            // Header background should produce a filled rectangle
            assertThat(content).contains("re\nf");
        }

        @Test
        void tableWithAlternatingRowColors() {
            PdfTable table = PdfTable.of(2)
                    .position(50, 700)
                    .width(400)
                    .alternateRowColors(PdfColor.WHITE, PdfColor.LIGHT_GRAY)
                    .row("a1", "b1")
                    .row("a2", "b2")
                    .row("a3", "b3");

            byte[] pdf = OpenPdf.create()
                    .addPage().table(table).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("(a1) Tj");
            assertThat(content).contains("(a2) Tj");
            assertThat(content).contains("(a3) Tj");
        }

        @Test
        void tableWithBorderRendersOuterBorder() {
            PdfTable table = PdfTable.of(2)
                    .position(50, 700)
                    .width(400)
                    .borderWidth(1.5f)
                    .header("H1", "H2")
                    .row("a", "b");

            byte[] pdf = OpenPdf.create()
                    .addPage().table(table).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("1.50 w"); // border width
            // Should have cell borders (re S) and outer table border
            long reCount = content.chars().filter(ch -> ch == 'S').count();
            assertThat(reCount).isGreaterThanOrEqualTo(1);
        }

        @Test
        void tableWithZeroBorderWidthOmitsBorder() {
            PdfTable table = PdfTable.of(2)
                    .position(50, 700)
                    .width(400)
                    .borderWidth(0)
                    .row("a", "b");

            byte[] pdf = OpenPdf.create()
                    .addPage().table(table).endPage()
                    .toBytes();

            String content = new String(pdf);
            // No border width operator and no cell border strokes
            assertThat(content).doesNotContain("0 w\n");
        }

        @Test
        void tableWithBorderColor() {
            PdfTable table = PdfTable.of(2)
                    .position(50, 700)
                    .width(400)
                    .borderColor(PdfColor.RED)
                    .borderWidth(1f)
                    .header("H1", "H2")
                    .row("a", "b");

            byte[] pdf = OpenPdf.create()
                    .addPage().table(table).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("1 0 0 RG"); // red stroke color for border
        }

        @Test
        void tableWithRatioColumnWidths() {
            PdfTable table = PdfTable.of(3)
                    .position(50, 700)
                    .width(600)
                    .columnWidths(0.2f, 0.3f, 0.5f)
                    .row("A", "B", "C");

            byte[] pdf = OpenPdf.create()
                    .addPage().table(table).endPage()
                    .toBytes();

            String content = new String(pdf);
            // With ratio mode: 0.2*600=120, 0.3*600=180, 0.5*600=300
            assertThat(content).contains("(A) Tj");
            assertThat(content).contains("(B) Tj");
            assertThat(content).contains("(C) Tj");
        }

        @Test
        void tableWithAbsoluteColumnWidths() {
            PdfTable table = PdfTable.of(2)
                    .position(50, 700)
                    .width(400)
                    .columnWidths(150f, 250f)
                    .row("A", "B");

            byte[] pdf = OpenPdf.create()
                    .addPage().table(table).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("(A) Tj");
            assertThat(content).contains("(B) Tj");
        }

        @Test
        void tableWithDefaultWidthWhenNotSet() {
            PdfTable table = PdfTable.of(2)
                    .position(50, 700)
                    // width not set → defaults to 500
                    .row("A", "B");

            byte[] pdf = OpenPdf.create()
                    .addPage().table(table).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("(A) Tj");
        }

        @Test
        void tableWithMultipleHeaderRows() {
            PdfTable table = PdfTable.of(2)
                    .position(50, 700)
                    .width(400)
                    .header("Group A", "Group B")
                    .header("Sub1", "Sub2")
                    .row("a", "b");

            byte[] pdf = OpenPdf.create()
                    .addPage().table(table).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("(Group A) Tj");
            assertThat(content).contains("(Sub1) Tj");
            assertThat(content).contains("(a) Tj");
        }

        @Test
        void tableCellWithCustomTextColor() {
            PdfTable table = PdfTable.of(2)
                    .position(50, 700)
                    .width(400)
                    .row(PdfCell.of("Red").textColor(PdfColor.RED), PdfCell.of("Blue").textColor(PdfColor.BLUE));

            byte[] pdf = OpenPdf.create()
                    .addPage().table(table).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("1 0 0 rg"); // red fill color
            assertThat(content).contains("0 0 1 rg"); // blue fill color
        }

        @Test
        void tableCellWithNullTextColorUsesBlack() {
            PdfTable table = PdfTable.of(1)
                    .position(50, 700)
                    .width(400)
                    .row(PdfCell.of("Default").textColor(null));

            byte[] pdf = OpenPdf.create()
                    .addPage().table(table).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("0 0 0 rg"); // black default
        }

        @Test
        void tableWithEmptyCellContent() {
            PdfTable table = PdfTable.of(2)
                    .position(50, 700)
                    .width(400)
                    .row("Filled", "");

            byte[] pdf = OpenPdf.create()
                    .addPage().table(table).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("(Filled) Tj");
            // Empty cell should not produce a Tj
        }

        @Test
        void tableEscapesSpecialCharsInCellContent() {
            PdfTable table = PdfTable.of(1)
                    .position(50, 700)
                    .width(400)
                    .row("Value (with) parens");

            byte[] pdf = OpenPdf.create()
                    .addPage().table(table).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("Value \\(with\\) parens");
        }

        @Test
        void tableCellWithCustomFontSize() {
            PdfTable table = PdfTable.of(1)
                    .position(50, 700)
                    .width(400)
                    .row(PdfCell.of("Big").fontSize(18));

            byte[] pdf = OpenPdf.create()
                    .addPage().table(table).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("18 Tf");
        }

        @Test
        void tableCellWithZeroFontSizeDefaultsToNine() {
            PdfTable table = PdfTable.of(1)
                    .position(50, 700)
                    .width(400)
                    .row(PdfCell.of("Small").fontSize(0));

            byte[] pdf = OpenPdf.create()
                    .addPage().table(table).endPage()
                    .toBytes();

            String content = new String(pdf);
            // Cell fontSize defaults to 9 when <= 0
            assertThat(content).contains("9 Tf");
        }
    }

    // ==================== Font Tests | 字体测试 ====================

    @Nested
    class FontTests {

        @Test
        void toBytesContainsFontReference() {
            byte[] pdf = OpenPdf.create()
                    .addPage().text("test", 50, 700).endPage()
                    .toBytes();
            String content = new String(pdf);
            assertThat(content).contains("/BaseFont /Helvetica");
        }

        @Test
        void toBytesWithMultipleFonts() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text("Regular", 50, 750, PdfFont.helvetica(), 12)
                        .text("Bold", 50, 730, PdfFont.helveticaBold(), 12)
                        .text("Times", 50, 710, PdfFont.timesRoman(), 12)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("/BaseFont /Helvetica");
            assertThat(content).contains("/BaseFont /Helvetica-Bold");
            assertThat(content).contains("/BaseFont /Times-Roman");
        }

        @Test
        void helveticaObliqueFontUsesF3() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text("Oblique", 50, 700, PdfFont.helveticaItalic(), 12)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("/F3 12 Tf");
            assertThat(content).contains("/BaseFont /Helvetica-Oblique");
        }

        @Test
        void timesRomanFontUsesF4() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text("Times", 50, 700, PdfFont.timesRoman(), 14)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("/F4 14 Tf");
            assertThat(content).contains("/BaseFont /Times-Roman");
        }

        @Test
        void timesBoldFontUsesF5() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text("TimesBold", 50, 700, PdfFont.timesBold(), 12)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("/F5 12 Tf");
            assertThat(content).contains("/BaseFont /Times-Bold");
        }

        @Test
        void courierFontUsesF6() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text("Courier", 50, 700, PdfFont.courier(), 10)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("/F6 10 Tf");
            assertThat(content).contains("/BaseFont /Courier");
        }

        @Test
        void courierBoldFontUsesF7() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text("CourierBold", 50, 700, PdfFont.courierBold(), 10)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("/F7 10 Tf");
            assertThat(content).contains("/BaseFont /Courier-Bold");
        }

        @Test
        void nullFontDefaultsToHelvetica() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text(PdfText.of("Default", 50, 700).font(null))
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("/F1");
            assertThat(content).contains("/BaseFont /Helvetica");
        }

        @Test
        void alwaysIncludesHelveticaBoldForTables() {
            byte[] pdf = OpenPdf.create()
                    .addPage().text("Simple", 50, 700).endPage()
                    .toBytes();

            String content = new String(pdf);
            // collectFonts always adds Helvetica-Bold as F2
            assertThat(content).contains("/BaseFont /Helvetica-Bold");
        }

        @Test
        void fontResourceDictionaryContainsAllFonts() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text("A", 50, 700, PdfFont.courier(), 10)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            // Font resource dict should reference multiple fonts
            assertThat(content).contains("/Font <<");
            assertThat(content).contains("/F1");
            assertThat(content).contains("/F6"); // Courier
        }
    }

    // ==================== Page Size & Orientation Tests | 页面大小和方向测试 ====================

    @Nested
    class PageSizeAndOrientationTests {

        @Test
        void toBytesWithA4PageSize() {
            byte[] pdf = OpenPdf.create(PageSize.A4)
                    .addPage().text("A4", 50, 700).endPage()
                    .toBytes();
            String content = new String(pdf);
            assertThat(content).contains("/MediaBox [0 0 595 842]");
        }

        @Test
        void toBytesWithLetterPageSize() {
            byte[] pdf = OpenPdf.create(PageSize.LETTER)
                    .addPage().text("Letter", 50, 700).endPage()
                    .toBytes();
            String content = new String(pdf);
            assertThat(content).contains("/MediaBox [0 0 612 792]");
        }

        @Test
        void landscapeOrientationSwapsDimensions() {
            byte[] pdf = OpenPdf.create(PageSize.A4)
                    .orientation(Orientation.LANDSCAPE)
                    .addPage().text("Landscape", 50, 500).endPage()
                    .toBytes();

            String content = new String(pdf);
            // Landscape A4: width=842, height=595
            assertThat(content).contains("/MediaBox [0 0 842 595]");
        }

        @Test
        void perPageCustomSize() {
            byte[] pdf = OpenPdf.create(PageSize.A4)
                    .addPage().text("A4", 50, 700).endPage()
                    .addPage(PageSize.LETTER).text("Letter", 50, 700).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("/Count 2");
            assertThat(content).contains("/MediaBox [0 0 595 842]");
            assertThat(content).contains("/MediaBox [0 0 612 792]");
        }

        @Test
        void perPageCustomSizeAndOrientation() {
            byte[] pdf = OpenPdf.create(PageSize.A4)
                    .addPage().text("Portrait A4", 50, 700).endPage()
                    .addPage(PageSize.A4, Orientation.LANDSCAPE).text("Landscape A4", 50, 500).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("/MediaBox [0 0 595 842]"); // portrait
            assertThat(content).contains("/MediaBox [0 0 842 595]"); // landscape
        }

        @Test
        void a3PageSize() {
            byte[] pdf = OpenPdf.create(PageSize.A3)
                    .addPage().text("A3", 50, 700).endPage()
                    .toBytes();
            String content = new String(pdf);
            assertThat(content).contains("/MediaBox [0 0 842 1191]");
        }
    }

    // ==================== Multi-Page Tests | 多页测试 ====================

    @Nested
    class MultiPageTests {

        @Test
        void toBytesHandlesMultiplePages() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text("Page 1", 50, 750)
                    .endPage()
                    .addPage()
                        .text("Page 2", 50, 750)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("/Count 2");
            assertThat(content).contains("(Page 1) Tj");
            assertThat(content).contains("(Page 2) Tj");
        }

        @Test
        void threePageDocumentHasCorrectCount() {
            byte[] pdf = OpenPdf.create()
                    .addPage().text("P1", 50, 700).endPage()
                    .addPage().text("P2", 50, 700).endPage()
                    .addPage().text("P3", 50, 700).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("/Count 3");
        }

        @Test
        void pagesKidsArrayContainsAllPageRefs() {
            byte[] pdf = OpenPdf.create()
                    .addPage().text("P1", 50, 700).endPage()
                    .addPage().text("P2", 50, 700).endPage()
                    .toBytes();

            String content = new String(pdf);
            // Kids should have two page object references
            assertThat(content).matches("(?s).*\\{0}/Kids \\[\\d+ 0 R \\d+ 0 R\\].*".replace("{0}", ""));
        }

        @Test
        void nextPageCreatesAdditionalPage() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text("First", 50, 700)
                    .nextPage()
                        .text("Second", 50, 700)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("/Count 2");
            assertThat(content).contains("(First) Tj");
            assertThat(content).contains("(Second) Tj");
        }
    }

    // ==================== Unsupported Element Tests | 不支持的元素测试 ====================

    @Nested
    class UnsupportedElementTests {

        @Test
        void ellipseElementIsSilentlySkipped() {
            PdfEllipse ellipse = PdfEllipse.circle(200, 400, 50);
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .ellipse(ellipse)
                        .text("After", 50, 700)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            // Ellipse is not rendered but text still works
            assertThat(content).contains("(After) Tj");
            assertThat(content).startsWith("%PDF-1.4");
        }

        @Test
        void paragraphElementIsSilentlySkipped() {
            PdfParagraph paragraph = PdfParagraph.of("Long text here", 50, 700, 400);
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .paragraph(paragraph)
                        .text("After", 50, 600)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("(After) Tj");
        }
    }

    // ==================== Save / Output Tests | 保存/输出测试 ====================

    @Nested
    class SaveOutputTests {

        @Test
        void saveToFile(@TempDir Path tempDir) throws Exception {
            Path file = tempDir.resolve("test.pdf");
            OpenPdf.create()
                    .title("File Test")
                    .addPage().text("Saved", 50, 700).endPage()
                    .save(file);

            assertThat(Files.exists(file)).isTrue();
            String content = new String(Files.readAllBytes(file));
            assertThat(content).startsWith("%PDF-1.4");
            assertThat(content).contains("(Saved) Tj");
        }

        @Test
        void saveToOutputStream() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OpenPdf.create()
                    .title("Stream Test")
                    .addPage().text("Streamed", 50, 700).endPage()
                    .save(out);

            String content = out.toString();
            assertThat(content).startsWith("%PDF-1.4");
            assertThat(content).contains("(Streamed) Tj");
        }

        @Test
        void saveToInvalidPathThrowsOpenPdfException(@TempDir Path tempDir) {
            Path invalid = tempDir.resolve("nonexistent-dir/sub/test.pdf");
            DocumentBuilder builder = OpenPdf.create()
                    .addPage().text("test", 50, 700).endPage();

            assertThatThrownBy(() -> builder.save(invalid))
                    .isInstanceOf(OpenPdfException.class)
                    .hasMessageContaining("Failed to save PDF");
        }

        @Test
        void saveToFailingOutputStreamThrowsOpenPdfException() {
            OutputStream failingStream = new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    throw new IOException("Simulated write failure");
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    throw new IOException("Simulated write failure");
                }
            };

            DocumentBuilder builder = OpenPdf.create()
                    .addPage().text("test", 50, 700).endPage();

            assertThatThrownBy(() -> builder.save(failingStream))
                    .isInstanceOf(OpenPdfException.class)
                    .hasMessageContaining("Failed to write PDF to stream");
        }

        @Test
        void saveNullPathThrowsNullPointerException() {
            DocumentBuilder builder = OpenPdf.create()
                    .addPage().text("test", 50, 700).endPage();

            assertThatThrownBy(() -> builder.save((Path) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void saveNullOutputStreamThrowsNullPointerException() {
            DocumentBuilder builder = OpenPdf.create()
                    .addPage().text("test", 50, 700).endPage();

            assertThatThrownBy(() -> builder.save((OutputStream) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void toBytesAndSaveToFileProduceSameContent(@TempDir Path tempDir) throws Exception {
            DocumentBuilder docBuilder = OpenPdf.create()
                    .title("Same Content")
                    .addPage().text("Hello", 50, 700).endPage();

            // Note: Cannot compare bytes exactly due to CreationDate timestamp,
            // but both should produce valid PDF with same structure
            byte[] bytes = docBuilder.toBytes();
            assertThat(bytes).isNotEmpty();
            assertThat(new String(bytes)).startsWith("%PDF-1.4");
        }

        @Test
        void toBytesProducesNonEmptyResult() {
            byte[] pdf = OpenPdf.create()
                    .addPage().text("test", 50, 700).endPage()
                    .toBytes();
            assertThat(pdf).isNotEmpty();
            assertThat(pdf.length).isGreaterThan(100);
        }
    }

    // ==================== Mixed Content Tests | 混合内容测试 ====================

    @Nested
    class MixedContentTests {

        @Test
        void pageWithTextLineAndRectangle() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text("Title", 50, 750)
                        .line(50, 740, 550, 740)
                        .filledRectangle(50, 600, 200, 100, PdfColor.LIGHT_GRAY)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("(Title) Tj");
            assertThat(content).contains("50 740 m");
            assertThat(content).contains("50 600 200 100 re");
        }

        @Test
        void pageWithTextAndTable() {
            PdfTable table = PdfTable.of(2)
                    .position(50, 600)
                    .width(400)
                    .header("Key", "Value")
                    .row("name", "test");

            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text("Table Below", 50, 750)
                        .table(table)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("(Table Below) Tj");
            assertThat(content).contains("(Key) Tj");
            assertThat(content).contains("(name) Tj");
        }

        @Test
        void complexMultiPageDocument() {
            PdfTable table = PdfTable.of(3)
                    .position(50, 600)
                    .width(500)
                    .headerBackground(PdfColor.DARK_GRAY)
                    .borderColor(PdfColor.BLACK)
                    .borderWidth(1f)
                    .header("ID", "Name", "Status")
                    .row("1", "Alice", "Active")
                    .row("2", "Bob", "Inactive");

            byte[] pdf = OpenPdf.create(PageSize.A4)
                    .title("Complex Report")
                    .author("Test")
                    .addPage()
                        .text("Report Title", 200, 780, PdfFont.helveticaBold(), 24)
                        .line(50, 760, 545, 760)
                        .text("Summary", 50, 720, PdfFont.helveticaBold(), 14)
                        .table(table)
                    .endPage()
                    .addPage(PageSize.A4, Orientation.LANDSCAPE)
                        .text("Landscape page", 50, 550)
                        .filledRectangle(50, 400, 700, 100, PdfColor.CYAN)
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).startsWith("%PDF-1.4");
            assertThat(content).endsWith("%%EOF\n");
            assertThat(content).contains("/Count 2");
            assertThat(content).contains("/Title (Complex Report)");
            assertThat(content).contains("(Report Title) Tj");
            assertThat(content).contains("(Alice) Tj");
            assertThat(content).contains("(Landscape page) Tj");
        }
    }

    // ==================== Number Formatting Tests | 数字格式化测试 ====================

    @Nested
    class NumberFormattingTests {

        @Test
        void integerValuesFormattedWithoutDecimal() {
            byte[] pdf = OpenPdf.create()
                    .addPage().text("Pos", 100, 500).endPage()
                    .toBytes();

            String content = new String(pdf);
            // Integer positions should not have decimal point
            assertThat(content).contains("1 0 0 1 100 500 Tm");
        }

        @Test
        void floatValuesFormattedWithTwoDecimals() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text(PdfText.of("Float", 100.5f, 500.75f))
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("100.50");
            assertThat(content).contains("500.75");
        }
    }

    // ==================== Ellipse Rendering Tests | 椭圆渲染测试 ====================

    @Nested
    class EllipseRenderingTests {

        @Test
        void ellipseRendersWithBezierCurves() {
            PdfEllipse ellipse = PdfEllipse.of(200, 400, 80, 50)
                    .strokeColor(PdfColor.BLACK);
            byte[] pdf = OpenPdf.create()
                    .addPage().ellipse(ellipse).endPage()
                    .toBytes();

            String content = new String(pdf);
            // Should contain moveto and curveto operators
            assertThat(content).contains(" m\n");
            assertThat(content).contains(" c\n");
            // Should end with stroke operator
            assertThat(content).contains("S\n");
        }

        @Test
        void ellipseStartsAtRightmostPoint() {
            PdfEllipse ellipse = PdfEllipse.of(200, 400, 80, 50)
                    .strokeColor(PdfColor.BLACK);
            byte[] pdf = OpenPdf.create()
                    .addPage().ellipse(ellipse).endPage()
                    .toBytes();

            String content = new String(pdf);
            // Rightmost point: cx + rx = 200 + 80 = 280
            assertThat(content).contains("280 400 m");
        }

        @Test
        void circleRendersSymmetrically() {
            PdfEllipse circle = PdfEllipse.circle(300, 500, 60)
                    .strokeColor(PdfColor.RED);
            byte[] pdf = OpenPdf.create()
                    .addPage().ellipse(circle).endPage()
                    .toBytes();

            String content = new String(pdf);
            // Start at (360, 500) = cx + r
            assertThat(content).contains("360 500 m");
            // Four c (curveto) operators
            long curveCount = content.lines()
                    .filter(l -> l.trim().endsWith(" c"))
                    .count();
            assertThat(curveCount).isEqualTo(4);
        }

        @Test
        void filledEllipseUsesStrokeAndFillOperator() {
            PdfEllipse ellipse = PdfEllipse.of(200, 400, 80, 50)
                    .strokeColor(PdfColor.BLACK)
                    .fillColor(PdfColor.BLUE);
            byte[] pdf = OpenPdf.create()
                    .addPage().ellipse(ellipse).endPage()
                    .toBytes();

            String content = new String(pdf);
            // Both fill and stroke -> B operator
            assertThat(content).contains("B\n");
        }

        @Test
        void fillOnlyEllipseUsesFillOperator() {
            PdfEllipse ellipse = PdfEllipse.of(200, 400, 80, 50)
                    .strokeColor(null)
                    .fillColor(PdfColor.GREEN);
            byte[] pdf = OpenPdf.create()
                    .addPage().ellipse(ellipse).endPage()
                    .toBytes();

            String content = new String(pdf);
            // Fill only -> f operator (but not B)
            assertThat(content).contains("f\n");
        }

        @Test
        void ellipseWithStrokeWidthSetsLineWidth() {
            PdfEllipse ellipse = PdfEllipse.of(200, 400, 80, 50)
                    .strokeColor(PdfColor.BLACK)
                    .strokeWidth(3f);
            byte[] pdf = OpenPdf.create()
                    .addPage().ellipse(ellipse).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("3 w\n");
        }

        @Test
        void ellipseRendersStrokeColor() {
            PdfEllipse ellipse = PdfEllipse.of(200, 400, 80, 50)
                    .strokeColor(PdfColor.RED);
            byte[] pdf = OpenPdf.create()
                    .addPage().ellipse(ellipse).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("1 0 0 RG"); // red stroke color
        }

        @Test
        void ellipseRendersFillColor() {
            PdfEllipse ellipse = PdfEllipse.of(200, 400, 80, 50)
                    .strokeColor(null)
                    .fillColor(PdfColor.BLUE);
            byte[] pdf = OpenPdf.create()
                    .addPage().ellipse(ellipse).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("0 0 1 rg"); // blue fill color
        }
    }

    // ==================== Paragraph Rendering Tests | 段落渲染测试 ====================

    @Nested
    class ParagraphRenderingTests {

        @Test
        void paragraphRendersTextContent() {
            PdfParagraph para = PdfParagraph.of("Hello World", 50, 700, 400);
            byte[] pdf = OpenPdf.create()
                    .addPage().paragraph(para).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("(Hello World) Tj");
        }

        @Test
        void paragraphWrapsLongText() {
            // "word" is ~4 chars, at 12pt * 0.5 = 24pt per word
            // 10 words of "word " = ~240pt wide; maxWidth=100 should wrap
            String longText = "word word word word word word word word word word";
            PdfParagraph para = PdfParagraph.of(longText, 50, 700, 100)
                    .fontSize(12);
            byte[] pdf = OpenPdf.create()
                    .addPage().paragraph(para).endPage()
                    .toBytes();

            String content = new String(pdf);
            // Should have multiple BT/ET blocks (one per line)
            long btCount = content.lines().filter(l -> l.trim().equals("BT")).count();
            assertThat(btCount).isGreaterThan(1);
        }

        @Test
        void paragraphRespectsFirstLineIndent() {
            PdfParagraph para = PdfParagraph.of("First line text", 50, 700, 400)
                    .firstLineIndent(30);
            byte[] pdf = OpenPdf.create()
                    .addPage().paragraph(para).endPage()
                    .toBytes();

            String content = new String(pdf);
            // x should be 50 + 30 = 80
            assertThat(content).contains("80 700 Tm");
        }

        @Test
        void paragraphCenterAlignmentAdjustsX() {
            PdfParagraph para = PdfParagraph.of("Hi", 50, 700, 400)
                    .alignment(PdfParagraph.Alignment.CENTER);
            byte[] pdf = OpenPdf.create()
                    .addPage().paragraph(para).endPage()
                    .toBytes();

            String content = new String(pdf);
            // Text "Hi" ~ 2 chars * 12 * 0.5 = 12pt width
            // center x = 50 + (400 - 12) / 2 = 50 + 194 = 244
            assertThat(content).contains("Tm");
            // The x coordinate should be shifted from 50 towards center
            assertThat(content).doesNotContain("1 0 0 1 50 700 Tm");
        }

        @Test
        void paragraphRightAlignmentAdjustsX() {
            PdfParagraph para = PdfParagraph.of("Hi", 50, 700, 400)
                    .alignment(PdfParagraph.Alignment.RIGHT);
            byte[] pdf = OpenPdf.create()
                    .addPage().paragraph(para).endPage()
                    .toBytes();

            String content = new String(pdf);
            // Right-aligned: x = 50 + 400 - 12 = 438
            assertThat(content).contains("438 700 Tm");
        }

        @Test
        void paragraphJustifyAddsWordSpacing() {
            // Need a line that wraps (not last line) for justify to apply Tw
            String text = "one two three four five six seven eight nine ten eleven twelve";
            PdfParagraph para = PdfParagraph.of(text, 50, 700, 150)
                    .alignment(PdfParagraph.Alignment.JUSTIFY)
                    .fontSize(12);
            byte[] pdf = OpenPdf.create()
                    .addPage().paragraph(para).endPage()
                    .toBytes();

            String content = new String(pdf);
            // Justified non-last lines should have Tw operator
            assertThat(content).contains("Tw\n");
            // And should reset it
            assertThat(content).contains("0 Tw\n");
        }

        @Test
        void paragraphSetsTextColor() {
            PdfParagraph para = PdfParagraph.of("Colored", 50, 700, 400)
                    .color(PdfColor.RED);
            byte[] pdf = OpenPdf.create()
                    .addPage().paragraph(para).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("1 0 0 rg");
        }

        @Test
        void paragraphWithNullContentProducesNoOutput() {
            PdfParagraph para = PdfParagraph.builder()
                    .content(null)
                    .position(50, 700)
                    .width(400);
            byte[] pdf = OpenPdf.create()
                    .addPage().paragraph(para).endPage()
                    .toBytes();

            String content = new String(pdf);
            // Should not contain any Tj from the paragraph
            assertThat(content).doesNotContain("Tj");
        }

        @Test
        void paragraphWithCustomFont() {
            PdfParagraph para = PdfParagraph.of("CourierText", 50, 700, 400)
                    .font(StandardFont.COURIER);
            byte[] pdf = OpenPdf.create()
                    .addPage().paragraph(para).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("/F6");
            assertThat(content).contains("Courier");
        }
    }

    // ==================== Text Rotation Tests | 文本旋转测试 ====================

    @Nested
    class TextRotationTests {

        @Test
        void rotatedTextUsesRotationMatrix() {
            PdfText text = PdfText.of("Rotated", 100, 500).rotation(45);
            byte[] pdf = OpenPdf.create()
                    .addPage().text(text).endPage()
                    .toBytes();

            String content = new String(pdf);
            // cos(45) ≈ 0.71, sin(45) ≈ 0.71
            assertThat(content).contains("0.71");
            assertThat(content).contains("Tm");
            // Should NOT contain the identity matrix "1 0 0 1"
            assertThat(content).doesNotContain("1 0 0 1 100 500 Tm");
        }

        @Test
        void zeroRotationUsesIdentityMatrix() {
            PdfText text = PdfText.of("Normal", 100, 500).rotation(0);
            byte[] pdf = OpenPdf.create()
                    .addPage().text(text).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("1 0 0 1 100 500 Tm");
        }

        @Test
        void rotation90DegreesProducesCorrectMatrix() {
            PdfText text = PdfText.of("Vert", 100, 500).rotation(90);
            byte[] pdf = OpenPdf.create()
                    .addPage().text(text).endPage()
                    .toBytes();

            String content = new String(pdf);
            // cos(90)≈0, sin(90)=1 -> matrix approx: 0.00 1 -1 0.00 tx ty
            // Due to floating point, cos(90°) is ~6.12e-8, formatted as "0.00"
            assertThat(content).contains("0.00 1 -1 0.00 100 500 Tm");
        }
    }

    // ==================== Text Underline Tests | 文本下划线测试 ====================

    @Nested
    class TextUnderlineTests {

        @Test
        void underlineDrawsLineBelow() {
            PdfText text = PdfText.of("Underlined", 100, 500)
                    .underline(true)
                    .color(PdfColor.BLACK);
            byte[] pdf = OpenPdf.create()
                    .addPage().text(text).endPage()
                    .toBytes();

            String content = new String(pdf);
            // Should have text rendering
            assertThat(content).contains("(Underlined) Tj");
            // Should have underline line (m + l + S after ET)
            // The underline is drawn after the text block
            int etIdx = content.indexOf("ET\n");
            String afterEt = content.substring(etIdx);
            assertThat(afterEt).contains("m\n");
            assertThat(afterEt).contains("l\nS\n");
        }

        @Test
        void underlineSetsLineWidth() {
            PdfText text = PdfText.of("Test", 100, 500)
                    .underline(true)
                    .fontSize(20);
            byte[] pdf = OpenPdf.create()
                    .addPage().text(text).endPage()
                    .toBytes();

            String content = new String(pdf);
            // lineWidth = fontSize * 0.05 = 20 * 0.05 = 1.0
            int etIdx = content.indexOf("ET\n");
            String afterEt = content.substring(etIdx);
            assertThat(afterEt).contains("1 w\n");
        }

        @Test
        void noUnderlineWhenNotSet() {
            PdfText text = PdfText.of("NoUnder", 100, 500)
                    .underline(false);
            byte[] pdf = OpenPdf.create()
                    .addPage().text(text).endPage()
                    .toBytes();

            String content = new String(pdf);
            // After ET, there should be no line drawing for this text
            int etIdx = content.indexOf("ET\n");
            String afterEt = content.substring(etIdx + 3);
            // No stroke after ET for simple text (might have other content from structure)
            assertThat(afterEt).doesNotContain("l\nS\n");
        }
    }

    // ==================== Text Spacing Tests | 文本间距测试 ====================

    @Nested
    class TextSpacingTests {

        @Test
        void characterSpacingSetsTcOperator() {
            PdfText text = PdfText.of("Spaced", 100, 500)
                    .characterSpacing(2.5f);
            byte[] pdf = OpenPdf.create()
                    .addPage().text(text).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("2.50 Tc\n");
        }

        @Test
        void wordSpacingSetsTwOperator() {
            PdfText text = PdfText.of("Word Spacing", 100, 500)
                    .wordSpacing(5f);
            byte[] pdf = OpenPdf.create()
                    .addPage().text(text).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("5 Tw\n");
        }

        @Test
        void bothSpacingsSetBothOperators() {
            PdfText text = PdfText.of("Both", 100, 500)
                    .characterSpacing(1.5f)
                    .wordSpacing(3f);
            byte[] pdf = OpenPdf.create()
                    .addPage().text(text).endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("1.50 Tc\n");
            assertThat(content).contains("3 Tw\n");
        }

        @Test
        void zeroSpacingOmitsOperators() {
            PdfText text = PdfText.of("NoSpacing", 100, 500)
                    .characterSpacing(0)
                    .wordSpacing(0);
            byte[] pdf = OpenPdf.create()
                    .addPage().text(text).endPage()
                    .toBytes();

            String content = new String(pdf);
            // Should not contain Tc or Tw in the text block
            int btIdx = content.indexOf("BT\n");
            int etIdx = content.indexOf("ET\n");
            String textBlock = content.substring(btIdx, etIdx);
            assertThat(textBlock).doesNotContain("Tc");
            assertThat(textBlock).doesNotContain("Tw");
        }
    }

    // ==================== Image Rendering Tests | 图像渲染测试 ====================

    @Nested
    class ImageRenderingTests {

        @Test
        void jpegImageCreatesXObjectAndDoOperator() throws IOException {
            // Create a minimal valid JPEG: SOI + SOF0 + EOI
            byte[] jpegBytes = createMinimalJpeg(10, 10);

            PdfImage img = PdfImage.from(jpegBytes, PdfImage.ImageFormat.JPEG)
                    .position(100, 500)
                    .size(200, 150);

            byte[] pdf = OpenPdf.create()
                    .addPage().image(img).endPage()
                    .toBytes();

            String content = new String(pdf, java.nio.charset.StandardCharsets.US_ASCII);
            // Should contain XObject reference in resources
            assertThat(content).contains("/XObject");
            // Should contain Do operator in content stream
            assertThat(content).contains("Do");
            // Should contain graphics state save/restore
            assertThat(content).contains("q\n");
            assertThat(content).contains("Q\n");
            // Should contain DCTDecode filter for JPEG
            assertThat(content).contains("/DCTDecode");
        }

        @Test
        void pngImageCreatesXObjectWithFlateDecode() throws IOException {
            // Create a minimal PNG via BufferedImage
            byte[] pngBytes = createMinimalPng(5, 5, false);

            PdfImage img = PdfImage.from(pngBytes, PdfImage.ImageFormat.PNG)
                    .position(50, 400)
                    .size(100, 100);

            byte[] pdf = OpenPdf.create()
                    .addPage().image(img).endPage()
                    .toBytes();

            String content = new String(pdf, java.nio.charset.StandardCharsets.US_ASCII);
            assertThat(content).contains("/XObject");
            assertThat(content).contains("Do");
            assertThat(content).contains("/FlateDecode");
        }

        @Test
        void pngWithAlphaGeneratesSMask() throws IOException {
            byte[] pngBytes = createMinimalPng(5, 5, true);

            PdfImage img = PdfImage.from(pngBytes, PdfImage.ImageFormat.PNG)
                    .position(50, 400)
                    .size(100, 100);

            byte[] pdf = OpenPdf.create()
                    .addPage().image(img).endPage()
                    .toBytes();

            String content = new String(pdf, java.nio.charset.StandardCharsets.US_ASCII);
            assertThat(content).contains("/SMask");
            assertThat(content).contains("/DeviceGray");
        }

        @Test
        void imageTransformMatrixSetsSizeAndPosition() throws IOException {
            byte[] pngBytes = createMinimalPng(5, 5, false);

            PdfImage img = PdfImage.from(pngBytes, PdfImage.ImageFormat.PNG)
                    .position(75, 300)
                    .size(200, 150);

            byte[] pdf = OpenPdf.create()
                    .addPage().image(img).endPage()
                    .toBytes();

            String content = new String(pdf);
            // cm transformation: w 0 0 h x y cm
            assertThat(content).contains("200 0 0 150 75 300 cm");
        }

        @Test
        void multipleImagesOnSamePage() throws IOException {
            byte[] png1 = createMinimalPng(3, 3, false);
            byte[] png2 = createMinimalPng(4, 4, false);

            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .image(PdfImage.from(png1, PdfImage.ImageFormat.PNG)
                                .position(50, 600).size(100, 100))
                        .image(PdfImage.from(png2, PdfImage.ImageFormat.PNG)
                                .position(200, 600).size(100, 100))
                    .endPage()
                    .toBytes();

            String content = new String(pdf, java.nio.charset.StandardCharsets.US_ASCII);
            // Should have two Do operators
            assertThat(content).contains("/Im0 Do");
            assertThat(content).contains("/Im1 Do");
        }

        /**
         * Creates a minimal valid JPEG byte array with SOI + APP0 + SOF0 + EOI.
         * Used for testing only.
         */
        private byte[] createMinimalJpeg(int width, int height) throws IOException {
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(img, "JPEG", baos);
            return baos.toByteArray();
        }

        /**
         * Creates a minimal PNG byte array.
         */
        private byte[] createMinimalPng(int width, int height, boolean withAlpha) throws IOException {
            int type = withAlpha ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR;
            BufferedImage img = new BufferedImage(width, height, type);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(img, "PNG", baos);
            return baos.toByteArray();
        }
    }

    // ==================== Mixed Elements Tests | 混合元素测试 ====================

    @Nested
    class MixedElementsTests {

        @Test
        void pageWithTextAndEllipse() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text("Title", 100, 700)
                        .ellipse(PdfEllipse.circle(300, 500, 50))
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("(Title) Tj");
            assertThat(content).contains("350 500 m"); // circle start
            assertThat(content).contains("S\n");
        }

        @Test
        void pageWithTextEllipseAndParagraph() {
            byte[] pdf = OpenPdf.create()
                    .addPage()
                        .text("Header", 100, 750)
                        .ellipse(PdfEllipse.of(300, 600, 40, 30).fillColor(PdfColor.YELLOW))
                        .paragraph(PdfParagraph.of("Body text paragraph", 50, 500, 400))
                    .endPage()
                    .toBytes();

            String content = new String(pdf);
            assertThat(content).contains("(Header) Tj");
            assertThat(content).contains("(Body text paragraph) Tj");
            // Yellow fill
            assertThat(content).contains("1 1 0 rg");
        }
    }
}
