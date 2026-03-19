package cloud.opencode.base.expression.function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * StringFunctions Tests
 * StringFunctions 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("StringFunctions Tests | StringFunctions 测试")
class StringFunctionsTest {

    private static Map<String, Function> functions;

    @BeforeAll
    static void setup() {
        functions = StringFunctions.getFunctions();
    }

    @Nested
    @DisplayName("Length Functions Tests | 长度函数测试")
    class LengthFunctionsTests {

        @Test
        @DisplayName("len function | len 函数")
        void testLen() {
            Function len = functions.get("len");
            assertThat(len.apply("hello")).isEqualTo(5);
            assertThat(len.apply("")).isEqualTo(0);
            assertThat(len.apply((Object) null)).isEqualTo(0);
            assertThat(len.apply()).isEqualTo(0);
        }

        @Test
        @DisplayName("length function | length 函数")
        void testLength() {
            Function length = functions.get("length");
            assertThat(length.apply("hello")).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Case Conversion Tests | 大小写转换测试")
    class CaseConversionTests {

        @Test
        @DisplayName("upper function | upper 函数")
        void testUpper() {
            Function upper = functions.get("upper");
            assertThat(upper.apply("hello")).isEqualTo("HELLO");
            assertThat(upper.apply("Hello World")).isEqualTo("HELLO WORLD");
            assertThat(upper.apply((Object) null)).isNull();
            assertThat(upper.apply()).isNull();
        }

        @Test
        @DisplayName("lower function | lower 函数")
        void testLower() {
            Function lower = functions.get("lower");
            assertThat(lower.apply("HELLO")).isEqualTo("hello");
            assertThat(lower.apply("Hello World")).isEqualTo("hello world");
            assertThat(lower.apply((Object) null)).isNull();
        }

        @Test
        @DisplayName("capitalize function | capitalize 函数")
        void testCapitalize() {
            Function capitalize = functions.get("capitalize");
            assertThat(capitalize.apply("hello")).isEqualTo("Hello");
            assertThat(capitalize.apply("HELLO")).isEqualTo("HELLO");
            assertThat(capitalize.apply("")).isEqualTo("");
            assertThat(capitalize.apply((Object) null)).isNull();
        }
    }

    @Nested
    @DisplayName("Trim Functions Tests | 修剪函数测试")
    class TrimFunctionsTests {

        @Test
        @DisplayName("trim function | trim 函数")
        void testTrim() {
            Function trim = functions.get("trim");
            assertThat(trim.apply("  hello  ")).isEqualTo("hello");
            assertThat(trim.apply("\t hello \n")).isEqualTo("hello");
            assertThat(trim.apply((Object) null)).isNull();
        }

        @Test
        @DisplayName("ltrim function | ltrim 函数")
        void testLtrim() {
            Function ltrim = functions.get("ltrim");
            assertThat(ltrim.apply("  hello  ")).isEqualTo("hello  ");
            assertThat(ltrim.apply((Object) null)).isNull();
        }

        @Test
        @DisplayName("rtrim function | rtrim 函数")
        void testRtrim() {
            Function rtrim = functions.get("rtrim");
            assertThat(rtrim.apply("  hello  ")).isEqualTo("  hello");
            assertThat(rtrim.apply((Object) null)).isNull();
        }
    }

    @Nested
    @DisplayName("Substring Functions Tests | 子字符串函数测试")
    class SubstringFunctionsTests {

        @Test
        @DisplayName("substring with start | substring 带起始位置")
        void testSubstringWithStart() {
            Function substring = functions.get("substring");
            assertThat(substring.apply("hello", 2)).isEqualTo("llo");
            assertThat(substring.apply("hello", 0)).isEqualTo("hello");
        }

        @Test
        @DisplayName("substring with start and end | substring 带起始和结束位置")
        void testSubstringWithStartAndEnd() {
            Function substring = functions.get("substring");
            assertThat(substring.apply("hello", 0, 2)).isEqualTo("he");
            assertThat(substring.apply("hello", 1, 4)).isEqualTo("ell");
        }

        @Test
        @DisplayName("substring with bounds check | substring 边界检查")
        void testSubstringBoundsCheck() {
            Function substring = functions.get("substring");
            assertThat(substring.apply("hello", -1)).isEqualTo("hello");
            assertThat(substring.apply("hello", 0, 100)).isEqualTo("hello");
            assertThat(substring.apply("hello", 10)).isEqualTo("");
        }

        @Test
        @DisplayName("substring null handling | substring null 处理")
        void testSubstringNullHandling() {
            Function substring = functions.get("substring");
            assertThat(substring.apply((Object) null, 0)).isNull();
            assertThat(substring.apply("hello")).isNull();
        }

        @Test
        @DisplayName("substr alias | substr 别名")
        void testSubstr() {
            assertThat(functions.get("substr")).isNotNull();
            assertThat(functions.get("substr").apply("hello", 0, 2)).isEqualTo("he");
        }
    }

    @Nested
    @DisplayName("Contains/StartsWith/EndsWith Tests | 包含/开头/结尾测试")
    class ContainsStartsEndsTests {

        @Test
        @DisplayName("contains function | contains 函数")
        void testContains() {
            Function contains = functions.get("contains");
            assertThat(contains.apply("hello world", "world")).isEqualTo(true);
            assertThat(contains.apply("hello world", "foo")).isEqualTo(false);
            assertThat(contains.apply((Object) null, "x")).isEqualTo(false);
        }

        @Test
        @DisplayName("startswith function | startswith 函数")
        void testStartswith() {
            Function startswith = functions.get("startswith");
            assertThat(startswith.apply("hello", "he")).isEqualTo(true);
            assertThat(startswith.apply("hello", "lo")).isEqualTo(false);
            assertThat(startswith.apply((Object) null, "x")).isEqualTo(false);
        }

        @Test
        @DisplayName("endswith function | endswith 函数")
        void testEndswith() {
            Function endswith = functions.get("endswith");
            assertThat(endswith.apply("hello", "lo")).isEqualTo(true);
            assertThat(endswith.apply("hello", "he")).isEqualTo(false);
            assertThat(endswith.apply((Object) null, "x")).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("Index Functions Tests | 索引函数测试")
    class IndexFunctionsTests {

        @Test
        @DisplayName("indexof function | indexof 函数")
        void testIndexOf() {
            Function indexof = functions.get("indexof");
            assertThat(indexof.apply("hello", "l")).isEqualTo(2);
            assertThat(indexof.apply("hello", "x")).isEqualTo(-1);
            assertThat(indexof.apply((Object) null, "x")).isEqualTo(-1);
        }

        @Test
        @DisplayName("lastindexof function | lastindexof 函数")
        void testLastIndexOf() {
            Function lastindexof = functions.get("lastindexof");
            assertThat(lastindexof.apply("hello", "l")).isEqualTo(3);
            assertThat(lastindexof.apply("hello", "x")).isEqualTo(-1);
            assertThat(lastindexof.apply((Object) null, "x")).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("Replace Functions Tests | 替换函数测试")
    class ReplaceFunctionsTests {

        @Test
        @DisplayName("replace function | replace 函数")
        void testReplace() {
            Function replace = functions.get("replace");
            assertThat(replace.apply("hello", "l", "L")).isEqualTo("heLLo");
            assertThat(replace.apply("hello", "x", "y")).isEqualTo("hello");
            assertThat(replace.apply((Object) null, "a", "b")).isNull();
            assertThat(replace.apply("hello")).isEqualTo("hello");
        }

        @Test
        @DisplayName("replaceall function | replaceall 函数")
        void testReplaceAll() {
            Function replaceall = functions.get("replaceall");
            assertThat(replaceall.apply("a1b2c3", "\\d", "X")).isEqualTo("aXbXcX");
        }

        @Test
        @DisplayName("replacefirst function | replacefirst 函数")
        void testReplaceFirst() {
            Function replacefirst = functions.get("replacefirst");
            assertThat(replacefirst.apply("a1b2c3", "\\d", "X")).isEqualTo("aXb2c3");
        }
    }

    @Nested
    @DisplayName("Split/Join Functions Tests | 分割/连接函数测试")
    class SplitJoinFunctionsTests {

        @Test
        @DisplayName("split function | split 函数")
        void testSplit() {
            Function split = functions.get("split");
            Object result = split.apply("a,b,c", ",");
            assertThat(result).isInstanceOf(String[].class);
            assertThat((String[]) result).containsExactly("a", "b", "c");
            assertThat((String[]) split.apply((Object) null, ",")).isEmpty();
        }

        @Test
        @DisplayName("join function | join 函数")
        void testJoin() {
            Function join = functions.get("join");
            assertThat(join.apply(",", "a", "b", "c")).isEqualTo("a,b,c");
            assertThat(join.apply("-", "x")).isEqualTo("x");
            assertThat(join.apply(",")).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("Concat Function Tests | 连接函数测试")
    class ConcatFunctionTests {

        @Test
        @DisplayName("concat function | concat 函数")
        void testConcat() {
            Function concat = functions.get("concat");
            assertThat(concat.apply("a", "b", "c")).isEqualTo("abc");
            assertThat(concat.apply()).isEqualTo("");
            assertThat(concat.apply("hello")).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("Padding Functions Tests | 填充函数测试")
    class PaddingFunctionsTests {

        @Test
        @DisplayName("lpad function | lpad 函数")
        void testLpad() {
            Function lpad = functions.get("lpad");
            assertThat(lpad.apply("5", 3, "0")).isEqualTo("005");
            assertThat(lpad.apply("hello", 3)).isEqualTo("llo");
            assertThat(lpad.apply("hi", 5)).isEqualTo("   hi");
            assertThat(lpad.apply("hi", 5, "")).isEqualTo("   hi");
            assertThat(lpad.apply((Object) null, 5)).isNull();
        }

        @Test
        @DisplayName("rpad function | rpad 函数")
        void testRpad() {
            Function rpad = functions.get("rpad");
            assertThat(rpad.apply("5", 3, "0")).isEqualTo("500");
            assertThat(rpad.apply("hello", 3)).isEqualTo("hel");
            assertThat(rpad.apply("hi", 5)).isEqualTo("hi   ");
            assertThat(rpad.apply((Object) null, 5)).isNull();
        }
    }

    @Nested
    @DisplayName("Repeat Function Tests | 重复函数测试")
    class RepeatFunctionTests {

        @Test
        @DisplayName("repeat function | repeat 函数")
        void testRepeat() {
            Function repeat = functions.get("repeat");
            assertThat(repeat.apply("ab", 3)).isEqualTo("ababab");
            assertThat(repeat.apply("x", 0)).isEqualTo("");
            assertThat(repeat.apply("x", -1)).isEqualTo("");
            assertThat(repeat.apply((Object) null, 5)).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("Reverse Function Tests | 反转函数测试")
    class ReverseFunctionTests {

        @Test
        @DisplayName("reverse function | reverse 函数")
        void testReverse() {
            Function reverse = functions.get("reverse");
            assertThat(reverse.apply("hello")).isEqualTo("olleh");
            assertThat(reverse.apply("")).isEqualTo("");
            assertThat(reverse.apply((Object) null)).isNull();
        }
    }

    @Nested
    @DisplayName("Matches Function Tests | 匹配函数测试")
    class MatchesFunctionTests {

        @Test
        @DisplayName("matches function | matches 函数")
        void testMatches() {
            Function matches = functions.get("matches");
            assertThat(matches.apply("test123", ".*\\d+.*")).isEqualTo(true);
            assertThat(matches.apply("test", "\\d+")).isEqualTo(false);
            assertThat(matches.apply((Object) null, ".*")).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("Empty/Blank Check Tests | 空/空白检查测试")
    class EmptyBlankCheckTests {

        @Test
        @DisplayName("isempty function | isempty 函数")
        void testIsEmpty() {
            Function isempty = functions.get("isempty");
            assertThat(isempty.apply("")).isEqualTo(true);
            assertThat(isempty.apply("  ")).isEqualTo(false);
            assertThat(isempty.apply("hello")).isEqualTo(false);
            assertThat(isempty.apply((Object) null)).isEqualTo(true);
        }

        @Test
        @DisplayName("isblank function | isblank 函数")
        void testIsBlank() {
            Function isblank = functions.get("isblank");
            assertThat(isblank.apply("")).isEqualTo(true);
            assertThat(isblank.apply("  ")).isEqualTo(true);
            assertThat(isblank.apply("\t\n")).isEqualTo(true);
            assertThat(isblank.apply("hello")).isEqualTo(false);
            assertThat(isblank.apply((Object) null)).isEqualTo(true);
        }

        @Test
        @DisplayName("isnotempty function | isnotempty 函数")
        void testIsNotEmpty() {
            Function isnotempty = functions.get("isnotempty");
            assertThat(isnotempty.apply("")).isEqualTo(false);
            assertThat(isnotempty.apply("hello")).isEqualTo(true);
            assertThat(isnotempty.apply((Object) null)).isEqualTo(false);
        }

        @Test
        @DisplayName("isnotblank function | isnotblank 函数")
        void testIsNotBlank() {
            Function isnotblank = functions.get("isnotblank");
            assertThat(isnotblank.apply("")).isEqualTo(false);
            assertThat(isnotblank.apply("  ")).isEqualTo(false);
            assertThat(isnotblank.apply("hello")).isEqualTo(true);
            assertThat(isnotblank.apply((Object) null)).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("Format Function Tests | 格式化函数测试")
    class FormatFunctionTests {

        @Test
        @DisplayName("format function | format 函数")
        void testFormat() {
            Function format = functions.get("format");
            assertThat(format.apply("Hello %s", "World")).isEqualTo("Hello World");
            assertThat(format.apply("%d + %d = %d", 1, 2, 3)).isEqualTo("1 + 2 = 3");
            assertThat(format.apply()).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("GetFunctions Tests | getFunctions 测试")
    class GetFunctionsTests {

        @Test
        @DisplayName("getFunctions returns all functions | getFunctions 返回所有函数")
        void testGetFunctionsReturnsAll() {
            Map<String, Function> funcs = StringFunctions.getFunctions();
            assertThat(funcs).isNotEmpty();
            assertThat(funcs).containsKey("upper");
            assertThat(funcs).containsKey("lower");
            assertThat(funcs).containsKey("trim");
            assertThat(funcs).containsKey("substring");
        }
    }
}
