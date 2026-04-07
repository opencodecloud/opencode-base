package cloud.opencode.base.json.util;

import cloud.opencode.base.json.exception.OpenJsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonStrings 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.3
 */
@DisplayName("JsonStrings 测试")
class JsonStringsTest {

    @Nested
    @DisplayName("转义测试")
    class EscapeTests {

        @Test
        @DisplayName("特殊字符转义")
        void specialChars() {
            assertThat(JsonStrings.escape("\"")).isEqualTo("\\\"");
            assertThat(JsonStrings.escape("\\")).isEqualTo("\\\\");
        }

        @Test
        @DisplayName("控制字符转义")
        void controlChars() {
            assertThat(JsonStrings.escape("\b")).isEqualTo("\\b");
            assertThat(JsonStrings.escape("\f")).isEqualTo("\\f");
            assertThat(JsonStrings.escape("\n")).isEqualTo("\\n");
            assertThat(JsonStrings.escape("\r")).isEqualTo("\\r");
            assertThat(JsonStrings.escape("\t")).isEqualTo("\\t");
        }

        @Test
        @DisplayName("低位控制字符使用unicode转义")
        void lowControlCharsUnicodeEscape() {
            assertThat(JsonStrings.escape("\u0000")).isEqualTo("\\u0000");
            assertThat(JsonStrings.escape("\u0001")).isEqualTo("\\u0001");
            assertThat(JsonStrings.escape("\u001f")).isEqualTo("\\u001f");
        }

        @Test
        @DisplayName("斜杠不转义")
        void slashNotEscaped() {
            assertThat(JsonStrings.escape("/")).isEqualTo("/");
        }

        @Test
        @DisplayName("普通字符不变")
        void normalCharsUnchanged() {
            assertThat(JsonStrings.escape("hello world")).isEqualTo("hello world");
            assertThat(JsonStrings.escape("abc123")).isEqualTo("abc123");
        }

        @Test
        @DisplayName("Unicode字符不变")
        void unicodeUnchanged() {
            assertThat(JsonStrings.escape("你好")).isEqualTo("你好");
        }

        @Test
        @DisplayName("null输入抛出异常")
        void nullThrows() {
            assertThatThrownBy(() -> JsonStrings.escape(null))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("空字符串")
        void emptyString() {
            assertThat(JsonStrings.escape("")).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("反转义测试")
    class UnescapeTests {

        @Test
        @DisplayName("反转义所有标准序列")
        void allStandardSequences() {
            assertThat(JsonStrings.unescape("\\\"")).isEqualTo("\"");
            assertThat(JsonStrings.unescape("\\\\")).isEqualTo("\\");
            assertThat(JsonStrings.unescape("\\/")).isEqualTo("/");
            assertThat(JsonStrings.unescape("\\b")).isEqualTo("\b");
            assertThat(JsonStrings.unescape("\\f")).isEqualTo("\f");
            assertThat(JsonStrings.unescape("\\n")).isEqualTo("\n");
            assertThat(JsonStrings.unescape("\\r")).isEqualTo("\r");
            assertThat(JsonStrings.unescape("\\t")).isEqualTo("\t");
        }

        @Test
        @DisplayName("反转义unicode序列")
        void unicodeSequences() {
            assertThat(JsonStrings.unescape("\\u0041")).isEqualTo("A");
            assertThat(JsonStrings.unescape("\\u4f60\\u597d")).isEqualTo("你好");
        }

        @Test
        @DisplayName("反转义代理对")
        void surrogatePairs() {
            // U+1F600 (Grinning Face) = \uD83D\uDE00
            String result = JsonStrings.unescape("\\uD83D\\uDE00");
            assertThat(result).isEqualTo("\uD83D\uDE00");
            assertThat(result.codePointAt(0)).isEqualTo(0x1F600);
        }

        @Test
        @DisplayName("无转义字符串快速路径")
        void noEscapeFastPath() {
            assertThat(JsonStrings.unescape("hello")).isEqualTo("hello");
        }

        @Test
        @DisplayName("null输入抛出异常")
        void nullThrows() {
            assertThatThrownBy(() -> JsonStrings.unescape(null))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("不完整转义序列抛出异常")
        void incompleteEscapeThrows() {
            assertThatThrownBy(() -> JsonStrings.unescape("\\"))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("无效转义字符抛出异常")
        void invalidEscapeThrows() {
            assertThatThrownBy(() -> JsonStrings.unescape("\\x"))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("不完整unicode转义抛出异常")
        void incompleteUnicodeThrows() {
            assertThatThrownBy(() -> JsonStrings.unescape("\\u00"))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }
    }

    @Nested
    @DisplayName("往返转义测试")
    class RoundTripTests {

        @Test
        @DisplayName("转义后反转义恢复原值")
        void escapeThenUnescape() {
            String[] testCases = {
                    "hello world",
                    "line1\nline2",
                    "tab\there",
                    "quote\"inside",
                    "back\\slash",
                    "all\b\f\n\r\tspecial",
                    "\u0000\u0001\u001f",
                    "你好世界",
                    ""
            };
            for (String original : testCases) {
                String escaped = JsonStrings.escape(original);
                String unescaped = JsonStrings.unescape(escaped);
                assertThat(unescaped).isEqualTo(original);
            }
        }
    }

    @Nested
    @DisplayName("JSON验证测试")
    class ValidationTests {

        @Test
        @DisplayName("有效JSON对象")
        void validObjects() {
            assertThat(JsonStrings.isValid("{}")).isTrue();
            assertThat(JsonStrings.isValid("{\"key\":\"value\"}")).isTrue();
            assertThat(JsonStrings.isValid("{\"a\":1,\"b\":true,\"c\":null}")).isTrue();
        }

        @Test
        @DisplayName("有效JSON数组")
        void validArrays() {
            assertThat(JsonStrings.isValid("[]")).isTrue();
            assertThat(JsonStrings.isValid("[1,2,3]")).isTrue();
            assertThat(JsonStrings.isValid("[\"a\",true,null]")).isTrue();
        }

        @Test
        @DisplayName("有效JSON字符串")
        void validStrings() {
            assertThat(JsonStrings.isValid("\"hello\"")).isTrue();
            assertThat(JsonStrings.isValid("\"\"")).isTrue();
        }

        @Test
        @DisplayName("有效JSON数字")
        void validNumbers() {
            assertThat(JsonStrings.isValid("42")).isTrue();
            assertThat(JsonStrings.isValid("-3.14")).isTrue();
            assertThat(JsonStrings.isValid("1e10")).isTrue();
            assertThat(JsonStrings.isValid("0")).isTrue();
        }

        @Test
        @DisplayName("有效JSON布尔值和null")
        void validBooleansAndNull() {
            assertThat(JsonStrings.isValid("true")).isTrue();
            assertThat(JsonStrings.isValid("false")).isTrue();
            assertThat(JsonStrings.isValid("null")).isTrue();
        }

        @Test
        @DisplayName("带空白的有效JSON")
        void validWithWhitespace() {
            assertThat(JsonStrings.isValid("  { \"a\" : 1 }  ")).isTrue();
            assertThat(JsonStrings.isValid(" [ 1 , 2 ] ")).isTrue();
        }

        @Test
        @DisplayName("无效JSON - 未终止字符串")
        void invalidUnterminatedString() {
            assertThat(JsonStrings.isValid("\"hello")).isFalse();
        }

        @Test
        @DisplayName("无效JSON - 尾逗号")
        void invalidTrailingComma() {
            assertThat(JsonStrings.isValid("{\"a\":1,}")).isFalse();
            assertThat(JsonStrings.isValid("[1,]")).isFalse();
        }

        @Test
        @DisplayName("无效JSON - 缺少值")
        void invalidMissingValue() {
            assertThat(JsonStrings.isValid("{\"a\":}")).isFalse();
        }

        @Test
        @DisplayName("无效JSON - 多余内容")
        void invalidTrailingContent() {
            assertThat(JsonStrings.isValid("{}extra")).isFalse();
        }

        @Test
        @DisplayName("null/空/空白返回false")
        void nullEmptyBlankReturnFalse() {
            assertThat(JsonStrings.isValid(null)).isFalse();
            assertThat(JsonStrings.isValid("")).isFalse();
            assertThat(JsonStrings.isValid("   ")).isFalse();
        }

        @Test
        @DisplayName("无效JSON - 前导零")
        void invalidLeadingZero() {
            assertThat(JsonStrings.isValid("01")).isFalse();
        }

        @Test
        @DisplayName("嵌套JSON有效")
        void nestedJsonValid() {
            assertThat(JsonStrings.isValid("{\"a\":{\"b\":[1,{\"c\":true}]}}")).isTrue();
        }
    }

    @Nested
    @DisplayName("压缩测试")
    class MinifyTests {

        @Test
        @DisplayName("移除空白")
        void removesWhitespace() {
            String json = "{ \"a\" : 1 , \"b\" : [ 1 , 2 , 3 ] }";
            assertThat(JsonStrings.minify(json)).isEqualTo("{\"a\":1,\"b\":[1,2,3]}");
        }

        @Test
        @DisplayName("已压缩JSON不变")
        void alreadyMinified() {
            String json = "{\"a\":1}";
            assertThat(JsonStrings.minify(json)).isEqualTo(json);
        }

        @Test
        @DisplayName("空对象和数组")
        void emptyContainers() {
            assertThat(JsonStrings.minify("{ }")).isEqualTo("{}");
            assertThat(JsonStrings.minify("[ ]")).isEqualTo("[]");
        }

        @Test
        @DisplayName("字符串中空白保留")
        void whitespaceInStringsPreserved() {
            String json = "{\"key\":\"hello world\"}";
            assertThat(JsonStrings.minify(json)).isEqualTo(json);
        }

        @Test
        @DisplayName("null输入抛出异常")
        void nullThrows() {
            assertThatThrownBy(() -> JsonStrings.minify(null))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("无效JSON抛出异常")
        void invalidJsonThrows() {
            assertThatThrownBy(() -> JsonStrings.minify("{invalid"))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }
    }

    @Nested
    @DisplayName("格式化测试")
    class PrettyPrintTests {

        @Test
        @DisplayName("默认缩进格式化")
        void defaultIndent() {
            String json = "{\"a\":1,\"b\":[1,2]}";
            String result = JsonStrings.prettyPrint(json);
            assertThat(result).contains("\n");
            assertThat(result).contains("  \"a\"");
            assertThat(result).contains("  \"b\"");
        }

        @Test
        @DisplayName("自定义缩进格式化")
        void customIndent() {
            String json = "{\"a\":1}";
            String result = JsonStrings.prettyPrint(json, "\t");
            assertThat(result).contains("\t\"a\"");
        }

        @Test
        @DisplayName("空对象格式化")
        void emptyObject() {
            assertThat(JsonStrings.prettyPrint("{}")).isEqualTo("{}");
        }

        @Test
        @DisplayName("空数组格式化")
        void emptyArray() {
            assertThat(JsonStrings.prettyPrint("[]")).isEqualTo("[]");
        }

        @Test
        @DisplayName("嵌套结构格式化")
        void nestedStructure() {
            String json = "{\"a\":{\"b\":1}}";
            String result = JsonStrings.prettyPrint(json);
            // Should have multiple levels of indentation
            assertThat(result).contains("    \"b\""); // 4 spaces = 2 levels
        }

        @Test
        @DisplayName("null输入抛出异常")
        void nullThrows() {
            assertThatThrownBy(() -> JsonStrings.prettyPrint(null))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("格式化后压缩恢复原值")
        void prettyThenMinifyRoundTrip() {
            String original = "{\"a\":1,\"b\":[1,2,3],\"c\":{\"d\":true}}";
            String pretty = JsonStrings.prettyPrint(original);
            String minified = JsonStrings.minify(pretty);
            assertThat(minified).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("边界条件修复验证")
    class EdgeCaseFixTests {

        @Test
        @DisplayName("minify裸减号抛出异常")
        void minifyBareMinusThrows() {
            assertThatThrownBy(() -> JsonStrings.minify("-"))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("prettyPrint裸减号抛出异常")
        void prettyPrintBareMinusThrows() {
            assertThatThrownBy(() -> JsonStrings.prettyPrint("-"))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("minify前导零01抛出异常")
        void minifyLeadingZeroThrows() {
            assertThatThrownBy(() -> JsonStrings.minify("01"))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }
    }
}
