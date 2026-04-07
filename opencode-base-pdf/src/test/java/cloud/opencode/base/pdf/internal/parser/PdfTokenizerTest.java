package cloud.opencode.base.pdf.internal.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * PdfTokenizer 测试
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
@DisplayName("PdfTokenizer 测试")
class PdfTokenizerTest {

    private PdfTokenizer tokenizer(String content) {
        return new PdfTokenizer(content.getBytes(StandardCharsets.US_ASCII));
    }

    @Nested
    @DisplayName("数字解析")
    class NumberTests {

        @Test
        @DisplayName("解析整数")
        void testInteger() {
            PdfTokenizer t = tokenizer("42 ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfNumber.class);
            assertThat(((PdfObject.PdfNumber) token).intValue()).isEqualTo(42);
        }

        @Test
        @DisplayName("解析负数")
        void testNegativeNumber() {
            PdfTokenizer t = tokenizer("-17 ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfNumber.class);
            assertThat(((PdfObject.PdfNumber) token).intValue()).isEqualTo(-17);
        }

        @Test
        @DisplayName("解析浮点数")
        void testFloat() {
            PdfTokenizer t = tokenizer("3.14 ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfNumber.class);
            assertThat(((PdfObject.PdfNumber) token).value()).isCloseTo(3.14, within(0.001));
        }

        @Test
        @DisplayName("解析正号数字")
        void testPositiveSign() {
            PdfTokenizer t = tokenizer("+5 ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfNumber.class);
            assertThat(((PdfObject.PdfNumber) token).intValue()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("名称解析")
    class NameTests {

        @Test
        @DisplayName("解析简单名称")
        void testSimpleName() {
            PdfTokenizer t = tokenizer("/Type ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfName.class);
            assertThat(((PdfObject.PdfName) token).value()).isEqualTo("Type");
        }

        @Test
        @DisplayName("解析带十六进制转义的名称")
        void testHexEscapeName() {
            PdfTokenizer t = tokenizer("/A#42 ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfName.class);
            assertThat(((PdfObject.PdfName) token).value()).isEqualTo("AB");
        }
    }

    @Nested
    @DisplayName("字符串解析")
    class StringTests {

        @Test
        @DisplayName("解析文字字符串")
        void testLiteralString() {
            PdfTokenizer t = tokenizer("(Hello World) ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfString.class);
            assertThat(((PdfObject.PdfString) token).value()).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("解析带转义的字符串")
        void testEscapeSequences() {
            PdfTokenizer t = tokenizer("(line1\\nline2) ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfString.class);
            assertThat(((PdfObject.PdfString) token).value()).isEqualTo("line1\nline2");
        }

        @Test
        @DisplayName("解析嵌套括号字符串")
        void testNestedParentheses() {
            PdfTokenizer t = tokenizer("(text(inner)text) ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfString.class);
            assertThat(((PdfObject.PdfString) token).value()).isEqualTo("text(inner)text");
        }

        @Test
        @DisplayName("解析十六进制字符串")
        void testHexString() {
            PdfTokenizer t = tokenizer("<48656C6C6F> ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfString.class);
            assertThat(((PdfObject.PdfString) token).value()).isEqualTo("Hello");
        }

        @Test
        @DisplayName("解析奇数长度十六进制字符串")
        void testOddHexString() {
            PdfTokenizer t = tokenizer("<4> ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfString.class);
            // "4" padded to "40" = '@'
            assertThat(((PdfObject.PdfString) token).value()).isEqualTo("@");
        }

        @Test
        @DisplayName("解析八进制转义")
        void testOctalEscape() {
            PdfTokenizer t = tokenizer("(\\101) "); // \101 = 'A'
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfString.class);
            assertThat(((PdfObject.PdfString) token).value()).isEqualTo("A");
        }
    }

    @Nested
    @DisplayName("布尔和空值解析")
    class BooleanNullTests {

        @Test
        @DisplayName("解析 true")
        void testTrue() {
            PdfTokenizer t = tokenizer("true ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfBoolean.class);
            assertThat(((PdfObject.PdfBoolean) token).value()).isTrue();
        }

        @Test
        @DisplayName("解析 false")
        void testFalse() {
            PdfTokenizer t = tokenizer("false ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfBoolean.class);
            assertThat(((PdfObject.PdfBoolean) token).value()).isFalse();
        }

        @Test
        @DisplayName("解析 null")
        void testNull() {
            PdfTokenizer t = tokenizer("null ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfNull.class);
        }
    }

    @Nested
    @DisplayName("数组解析")
    class ArrayTests {

        @Test
        @DisplayName("解析简单数组")
        void testSimpleArray() {
            PdfTokenizer t = tokenizer("[1 2 3] ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfArray.class);
            PdfObject.PdfArray arr = (PdfObject.PdfArray) token;
            assertThat(arr.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("解析空数组")
        void testEmptyArray() {
            PdfTokenizer t = tokenizer("[] ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfArray.class);
            assertThat(((PdfObject.PdfArray) token).isEmpty()).isTrue();
        }

        @Test
        @DisplayName("解析混合类型数组")
        void testMixedArray() {
            PdfTokenizer t = tokenizer("[42 (hello) /Name true] ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfArray.class);
            PdfObject.PdfArray arr = (PdfObject.PdfArray) token;
            assertThat(arr.size()).isEqualTo(4);
            assertThat(arr.get(0)).isInstanceOf(PdfObject.PdfNumber.class);
            assertThat(arr.get(1)).isInstanceOf(PdfObject.PdfString.class);
            assertThat(arr.get(2)).isInstanceOf(PdfObject.PdfName.class);
            assertThat(arr.get(3)).isInstanceOf(PdfObject.PdfBoolean.class);
        }
    }

    @Nested
    @DisplayName("字典解析")
    class DictionaryTests {

        @Test
        @DisplayName("解析简单字典")
        void testSimpleDictionary() {
            PdfTokenizer t = tokenizer("<< /Type /Page /Count 5 >> ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfDictionary.class);
            PdfObject.PdfDictionary dict = (PdfObject.PdfDictionary) token;
            assertThat(dict.getString("Type")).isEqualTo("Page");
            assertThat(dict.getInt("Count", 0)).isEqualTo(5);
        }

        @Test
        @DisplayName("解析空字典")
        void testEmptyDictionary() {
            PdfTokenizer t = tokenizer("<< >> ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfDictionary.class);
            assertThat(((PdfObject.PdfDictionary) token).size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("间接引用解析")
    class ReferenceTests {

        @Test
        @DisplayName("字典值中的间接引用")
        void testReferenceInDictionary() {
            PdfTokenizer t = tokenizer("<< /Pages 2 0 R >> ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfDictionary.class);
            PdfObject.PdfDictionary dict = (PdfObject.PdfDictionary) token;
            PdfObject pages = dict.get("Pages");
            assertThat(pages).isInstanceOf(PdfObject.PdfReference.class);
            assertThat(((PdfObject.PdfReference) pages).objectNumber()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("空白和注释跳过")
    class WhitespaceCommentTests {

        @Test
        @DisplayName("跳过注释")
        void testSkipComment() {
            PdfTokenizer t = tokenizer("%this is a comment\n42 ");
            PdfObject token = t.readToken();
            assertThat(token).isInstanceOf(PdfObject.PdfNumber.class);
            assertThat(((PdfObject.PdfNumber) token).intValue()).isEqualTo(42);
        }

        @Test
        @DisplayName("跳过多个空白")
        void testSkipWhitespace() {
            PdfTokenizer t = tokenizer("   \t\n\r  42 ");
            PdfObject token = t.readToken();
            assertThat(((PdfObject.PdfNumber) token).intValue()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("位置管理")
    class PositionTests {

        @Test
        @DisplayName("seekTo 和 getPosition")
        void testSeekTo() {
            PdfTokenizer t = tokenizer("abcdefghij");
            assertThat(t.getPosition()).isEqualTo(0);
            t.seekTo(5);
            assertThat(t.getPosition()).isEqualTo(5);
        }

        @Test
        @DisplayName("seekTo 越界抛出异常")
        void testSeekOutOfBounds() {
            PdfTokenizer t = tokenizer("abc");
            assertThatThrownBy(() -> t.seekTo(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("readLine 读取一行")
        void testReadLine() {
            PdfTokenizer t = tokenizer("line1\nline2\n");
            assertThat(t.readLine()).isEqualTo("line1");
            assertThat(t.readLine()).isEqualTo("line2");
        }

        @Test
        @DisplayName("searchBackward 查找字符串")
        void testSearchBackward() {
            PdfTokenizer t = tokenizer("hello world startxref\n12345");
            int pos = t.searchBackward("startxref");
            assertThat(pos).isEqualTo(12);
        }
    }
}
