package cloud.opencode.base.pdf.internal.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ContentStreamParser Test - Tests for PDF content stream parsing
 * ContentStreamParser 测试 - PDF 内容流解析测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
@DisplayName("ContentStreamParser 测试")
class ContentStreamParserTest {

    @Nested
    @DisplayName("null 和空输入处理")
    class NullAndEmptyInputTests {

        @Test
        @DisplayName("null 输入抛出 NPE")
        void testNullInput() {
            assertThatThrownBy(() -> ContentStreamParser.parse(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("空输入返回空列表")
        void testEmptyInput() {
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(new byte[0]);
            assertThat(ops).isEmpty();
        }

        @Test
        @DisplayName("仅空白返回空列表")
        void testWhitespaceOnly() {
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse("   \t\n\r  ".getBytes());
            assertThat(ops).isEmpty();
        }
    }

    @Nested
    @DisplayName("数字解析")
    class NumberParsingTests {

        @Test
        @DisplayName("解析整数")
        void testParseInteger() {
            byte[] data = "42 Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().name()).isEqualTo("Tj");
            assertThat(ops.getFirst().operands()).hasSize(1);
            assertThat(ops.getFirst().operands().getFirst()).isInstanceOf(PdfObject.PdfNumber.class);
            assertThat(((PdfObject.PdfNumber) ops.getFirst().operands().getFirst()).value()).isEqualTo(42.0);
        }

        @Test
        @DisplayName("解析负数")
        void testParseNegativeNumber() {
            byte[] data = "-12.5 Td".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(((PdfObject.PdfNumber) ops.getFirst().operands().getFirst()).value()).isEqualTo(-12.5);
        }

        @Test
        @DisplayName("解析正号数字")
        void testParsePositiveNumber() {
            byte[] data = "+3.14 Td".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(((PdfObject.PdfNumber) ops.getFirst().operands().getFirst()).value()).isCloseTo(3.14, within(0.001));
        }

        @Test
        @DisplayName("解析小数点开头的数字")
        void testParseDotPrefix() {
            byte[] data = ".75 Td".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(((PdfObject.PdfNumber) ops.getFirst().operands().getFirst()).value()).isCloseTo(0.75, within(0.001));
        }

        @Test
        @DisplayName("多个数字操作数")
        void testMultipleNumbers() {
            byte[] data = "100 200 300 400 500 600 Tm".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().operands()).hasSize(6);
        }
    }

    @Nested
    @DisplayName("字符串解析")
    class StringParsingTests {

        @Test
        @DisplayName("解析简单文字字符串")
        void testParseLiteralString() {
            byte[] data = "(Hello) Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().operands().getFirst()).isInstanceOf(PdfObject.PdfString.class);
            assertThat(((PdfObject.PdfString) ops.getFirst().operands().getFirst()).value()).isEqualTo("Hello");
        }

        @Test
        @DisplayName("解析带转义字符的字符串")
        void testParseEscapedString() {
            byte[] data = "(Hello\\nWorld\\r\\t\\b\\f) Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            String value = ((PdfObject.PdfString) ops.getFirst().operands().getFirst()).value();
            assertThat(value).contains("Hello\nWorld\r\t");
            assertThat(value).contains("\b");
            assertThat(value).contains("\f");
        }

        @Test
        @DisplayName("解析带括号转义的字符串")
        void testParseEscapedParens() {
            byte[] data = "(a\\(b\\)c) Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            String value = ((PdfObject.PdfString) ops.getFirst().operands().getFirst()).value();
            assertThat(value).isEqualTo("a(b)c");
        }

        @Test
        @DisplayName("解析带反斜杠转义的字符串")
        void testParseEscapedBackslash() {
            byte[] data = "(a\\\\b) Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            String value = ((PdfObject.PdfString) ops.getFirst().operands().getFirst()).value();
            assertThat(value).isEqualTo("a\\b");
        }

        @Test
        @DisplayName("解析带嵌套括号的字符串")
        void testNestedParentheses() {
            byte[] data = "(a(b(c)d)e) Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            String value = ((PdfObject.PdfString) ops.getFirst().operands().getFirst()).value();
            assertThat(value).isEqualTo("a(b(c)d)e");
        }

        @Test
        @DisplayName("解析带八进制转义的字符串")
        void testOctalEscape() {
            // \101 = 'A' (octal 101 = 65 decimal)
            byte[] data = "(\\101\\102\\103) Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            String value = ((PdfObject.PdfString) ops.getFirst().operands().getFirst()).value();
            assertThat(value).isEqualTo("ABC");
        }

        @Test
        @DisplayName("解析带单个八进制数字的转义")
        void testSingleOctalDigit() {
            // \7 = BEL (7)
            byte[] data = "(\\7) Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            String value = ((PdfObject.PdfString) ops.getFirst().operands().getFirst()).value();
            assertThat(value).isEqualTo("\u0007");
        }

        @Test
        @DisplayName("解析非标准转义字符")
        void testUnknownEscape() {
            // \x should just produce 'x'
            byte[] data = "(\\x) Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            String value = ((PdfObject.PdfString) ops.getFirst().operands().getFirst()).value();
            assertThat(value).isEqualTo("x");
        }
    }

    @Nested
    @DisplayName("十六进制字符串解析")
    class HexStringParsingTests {

        @Test
        @DisplayName("解析十六进制字符串")
        void testParseHexString() {
            byte[] data = "<48656C6C6F> Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().operands().getFirst()).isInstanceOf(PdfObject.PdfString.class);
            String value = ((PdfObject.PdfString) ops.getFirst().operands().getFirst()).value();
            assertThat(value).isEqualTo("Hello");
        }

        @Test
        @DisplayName("解析奇数位十六进制字符串（末尾补0）")
        void testParseOddHexString() {
            // <ABC> should be treated as <ABC0>
            byte[] data = "<ABC> Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().operands().getFirst()).isInstanceOf(PdfObject.PdfString.class);
        }

        @Test
        @DisplayName("解析含空白的十六进制字符串")
        void testParseHexStringWithWhitespace() {
            byte[] data = "<48 65 6C 6C 6F> Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            String value = ((PdfObject.PdfString) ops.getFirst().operands().getFirst()).value();
            assertThat(value).isEqualTo("Hello");
        }

        @Test
        @DisplayName("解析空十六进制字符串")
        void testEmptyHexString() {
            byte[] data = "<> Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            String value = ((PdfObject.PdfString) ops.getFirst().operands().getFirst()).value();
            assertThat(value).isEmpty();
        }
    }

    @Nested
    @DisplayName("名称解析")
    class NameParsingTests {

        @Test
        @DisplayName("解析名称对象")
        void testParseName() {
            byte[] data = "/F1 12 Tf".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().name()).isEqualTo("Tf");
            assertThat(ops.getFirst().operands().getFirst()).isInstanceOf(PdfObject.PdfName.class);
            assertThat(((PdfObject.PdfName) ops.getFirst().operands().getFirst()).value()).isEqualTo("F1");
        }

        @Test
        @DisplayName("名称以分隔符结束")
        void testNameEndsAtDelimiter() {
            byte[] data = "/Font(Hello) Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            // /Font is a name operand, (Hello) is a string operand, Tj is operator
            assertThat(ops.getFirst().operands()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("数组解析")
    class ArrayParsingTests {

        @Test
        @DisplayName("解析数字数组")
        void testParseNumberArray() {
            byte[] data = "[10 20 30] TJ".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().operands().getFirst()).isInstanceOf(PdfObject.PdfArray.class);
            PdfObject.PdfArray arr = (PdfObject.PdfArray) ops.getFirst().operands().getFirst();
            assertThat(arr.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("解析混合数组（字符串和数字）")
        void testParseMixedArray() {
            byte[] data = "[(Hello) -50 (World)] TJ".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            PdfObject.PdfArray arr = (PdfObject.PdfArray) ops.getFirst().operands().getFirst();
            assertThat(arr.size()).isEqualTo(3);
            assertThat(arr.get(0)).isInstanceOf(PdfObject.PdfString.class);
            assertThat(arr.get(1)).isInstanceOf(PdfObject.PdfNumber.class);
            assertThat(arr.get(2)).isInstanceOf(PdfObject.PdfString.class);
        }

        @Test
        @DisplayName("解析含十六进制字符串的数组")
        void testParseArrayWithHexString() {
            byte[] data = "[<48656C6C6F> -100 <576F726C64>] TJ".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            PdfObject.PdfArray arr = (PdfObject.PdfArray) ops.getFirst().operands().getFirst();
            assertThat(arr.size()).isEqualTo(3);
            assertThat(arr.get(0)).isInstanceOf(PdfObject.PdfString.class);
            assertThat(((PdfObject.PdfString) arr.get(0)).value()).isEqualTo("Hello");
        }

        @Test
        @DisplayName("解析含名称的数组")
        void testParseArrayWithNames() {
            byte[] data = "[/Name1 /Name2] Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            PdfObject.PdfArray arr = (PdfObject.PdfArray) ops.getFirst().operands().getFirst();
            assertThat(arr.size()).isEqualTo(2);
            assertThat(arr.get(0)).isInstanceOf(PdfObject.PdfName.class);
        }

        @Test
        @DisplayName("解析空数组")
        void testParseEmptyArray() {
            byte[] data = "[] TJ".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            PdfObject.PdfArray arr = (PdfObject.PdfArray) ops.getFirst().operands().getFirst();
            assertThat(arr.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("解析含转义字符串的数组")
        void testParseArrayWithEscapedString() {
            byte[] data = "[(Hello\\nWorld)] TJ".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            PdfObject.PdfArray arr = (PdfObject.PdfArray) ops.getFirst().operands().getFirst();
            assertThat(arr.size()).isEqualTo(1);
            String value = ((PdfObject.PdfString) arr.get(0)).value();
            assertThat(value).isEqualTo("Hello\nWorld");
        }

        @Test
        @DisplayName("解析含八进制转义的数组内字符串")
        void testParseArrayWithOctalEscape() {
            byte[] data = "[(\\101)] TJ".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            PdfObject.PdfArray arr = (PdfObject.PdfArray) ops.getFirst().operands().getFirst();
            String value = ((PdfObject.PdfString) arr.get(0)).value();
            assertThat(value).isEqualTo("A");
        }

        @Test
        @DisplayName("解析含嵌套括号的数组内字符串")
        void testParseArrayWithNestedParens() {
            byte[] data = "[(a(b)c)] TJ".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            PdfObject.PdfArray arr = (PdfObject.PdfArray) ops.getFirst().operands().getFirst();
            String value = ((PdfObject.PdfString) arr.get(0)).value();
            assertThat(value).isEqualTo("a(b)c");
        }

        @Test
        @DisplayName("解析含奇数位十六进制的数组")
        void testParseArrayWithOddHex() {
            byte[] data = "[<A>] TJ".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            PdfObject.PdfArray arr = (PdfObject.PdfArray) ops.getFirst().operands().getFirst();
            assertThat(arr.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("注释解析")
    class CommentParsingTests {

        @Test
        @DisplayName("跳过行注释")
        void testSkipComment() {
            byte[] data = "% This is a comment\n(Hello) Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().name()).isEqualTo("Tj");
        }

        @Test
        @DisplayName("跳过 CR 结尾的注释")
        void testSkipCommentCR() {
            byte[] data = "% comment\r(Hello) Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
        }
    }

    @Nested
    @DisplayName("布尔和 null 解析")
    class BooleanAndNullTests {

        @Test
        @DisplayName("true 作为操作数")
        void testTrueBoolean() {
            byte[] data = "true Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().operands().getFirst()).isInstanceOf(PdfObject.PdfBoolean.class);
            assertThat(((PdfObject.PdfBoolean) ops.getFirst().operands().getFirst()).value()).isTrue();
        }

        @Test
        @DisplayName("false 作为操作数")
        void testFalseBoolean() {
            byte[] data = "false Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().operands().getFirst()).isInstanceOf(PdfObject.PdfBoolean.class);
            assertThat(((PdfObject.PdfBoolean) ops.getFirst().operands().getFirst()).value()).isFalse();
        }

        @Test
        @DisplayName("null 作为操作数")
        void testNullOperand() {
            byte[] data = "null Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().operands().getFirst()).isInstanceOf(PdfObject.PdfNull.class);
        }
    }

    @Nested
    @DisplayName("操作符解析")
    class OperatorParsingTests {

        @Test
        @DisplayName("解析 BT/ET 操作符")
        void testBTET() {
            byte[] data = "BT (Hello) Tj ET".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(3);
            assertThat(ops.get(0).name()).isEqualTo("BT");
            assertThat(ops.get(1).name()).isEqualTo("Tj");
            assertThat(ops.get(2).name()).isEqualTo("ET");
        }

        @Test
        @DisplayName("解析 ' 操作符")
        void testSingleQuoteOperator() {
            byte[] data = "(Hello) '".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().name()).isEqualTo("'");
        }

        @Test
        @DisplayName("解析 \" 操作符")
        void testDoubleQuoteOperator() {
            byte[] data = "1 2 (Hello) \"".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().name()).isEqualTo("\"");
            assertThat(ops.getFirst().operands()).hasSize(3);
        }

        @Test
        @DisplayName("解析星号操作符 T*")
        void testTStarOperator() {
            byte[] data = "T*".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().name()).isEqualTo("T*");
        }

        @Test
        @DisplayName("解析图形状态操作符")
        void testGraphicsStateOperators() {
            byte[] data = "q 1 0 0 1 100 200 cm Q".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(3);
            assertThat(ops.get(0).name()).isEqualTo("q");
            assertThat(ops.get(1).name()).isEqualTo("cm");
            assertThat(ops.get(2).name()).isEqualTo("Q");
        }

        @Test
        @DisplayName("解析 Td 操作符")
        void testTdOperator() {
            byte[] data = "100 200 Td".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().name()).isEqualTo("Td");
            assertThat(ops.getFirst().operands()).hasSize(2);
        }

        @Test
        @DisplayName("解析 TD 操作符")
        void testTDOperator() {
            byte[] data = "0 -14 TD".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().name()).isEqualTo("TD");
        }

        @Test
        @DisplayName("解析 TJ 操作符")
        void testTJOperator() {
            byte[] data = "[(Hello) -100 (World)] TJ".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().name()).isEqualTo("TJ");
        }

        @Test
        @DisplayName("解析 Tf 操作符")
        void testTfOperator() {
            byte[] data = "/F1 12 Tf".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().name()).isEqualTo("Tf");
        }

        @Test
        @DisplayName("解析 TL 操作符")
        void testTLOperator() {
            byte[] data = "14 TL".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().name()).isEqualTo("TL");
        }

        @Test
        @DisplayName("解析 Tm 操作符")
        void testTmOperator() {
            byte[] data = "1 0 0 1 72 700 Tm".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().name()).isEqualTo("Tm");
            assertThat(ops.getFirst().operands()).hasSize(6);
        }
    }

    @Nested
    @DisplayName("复杂内容流")
    class ComplexContentStreamTests {

        @Test
        @DisplayName("解析完整的文本块")
        void testFullTextBlock() {
            String stream = "BT /F1 12 Tf 100 700 Td (Hello World) Tj ET";
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(stream.getBytes(StandardCharsets.US_ASCII));

            assertThat(ops).hasSize(5);
            assertThat(ops.get(0).name()).isEqualTo("BT");
            assertThat(ops.get(1).name()).isEqualTo("Tf");
            assertThat(ops.get(2).name()).isEqualTo("Td");
            assertThat(ops.get(3).name()).isEqualTo("Tj");
            assertThat(ops.get(4).name()).isEqualTo("ET");
        }

        @Test
        @DisplayName("解析多个文本块")
        void testMultipleTextBlocks() {
            String stream = "BT (Line1) Tj 0 -14 Td (Line2) Tj ET BT (Line3) Tj ET";
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(stream.getBytes(StandardCharsets.US_ASCII));

            long tjCount = ops.stream().filter(op -> "Tj".equals(op.name())).count();
            assertThat(tjCount).isEqualTo(3);
        }

        @Test
        @DisplayName("跳过未知字节")
        void testSkipUnknownBytes() {
            // Braces {} are delimiters, will be skipped
            byte[] data = new byte[]{'{', '}', '(', 'H', 'i', ')', ' ', 'T', 'j'};
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().name()).isEqualTo("Tj");
        }

        @Test
        @DisplayName("PDF 空白字符类型 (null, tab, form-feed)")
        void testPdfWhitespaceTypes() {
            // Test null byte (0), tab (9), form feed (12) as whitespace
            byte[] data = new byte[]{0x00, '(', 'A', ')', 0x09, 'T', 'j', 0x0C};
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            assertThat(ops.getFirst().name()).isEqualTo("Tj");
        }
    }

    @Nested
    @DisplayName("Operator 记录测试")
    class OperatorRecordTests {

        @Test
        @DisplayName("Operator name 不能为 null")
        void testOperatorNullName() {
            assertThatThrownBy(() -> new ContentStreamParser.Operator(null, List.of()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Operator operands 不能为 null")
        void testOperatorNullOperands() {
            assertThatThrownBy(() -> new ContentStreamParser.Operator("Tj", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Operator operands 是不可变的")
        void testOperatorOperandsImmutable() {
            ContentStreamParser.Operator op = new ContentStreamParser.Operator("Tj", List.of());
            assertThatThrownBy(() -> op.operands().add(new PdfObject.PdfNull()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("无效数字处理")
    class InvalidNumberTests {

        @Test
        @DisplayName("无效数字字符串回退为0")
        void testInvalidNumberFallback() {
            // A lone '+' or '-' followed by a non-digit
            byte[] data = "+. Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            // Should parse +. as a number (fallback to 0 if invalid)
            assertThat(ops).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("数组内无效元素处理")
    class ArrayInvalidElementTests {

        @Test
        @DisplayName("数组内跳过未知字节")
        void testArraySkipsUnknownByte() {
            // '{' inside array is not a valid element
            byte[] data = "[10 { 20] Tj".getBytes(StandardCharsets.US_ASCII);
            List<ContentStreamParser.Operator> ops = ContentStreamParser.parse(data);

            assertThat(ops).hasSize(1);
            PdfObject.PdfArray arr = (PdfObject.PdfArray) ops.getFirst().operands().getFirst();
            // Should contain at least the numbers
            assertThat(arr.size()).isGreaterThanOrEqualTo(2);
        }
    }
}
