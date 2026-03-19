package cloud.opencode.base.json.internal;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonParserTest Tests
 * JsonParserTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonParser 测试")
class JsonParserTest {

    private JsonNode parse(String json) {
        return new JsonParser(json).parse();
    }

    @Nested
    @DisplayName("基本类型解析")
    class PrimitiveTests {

        @Test void string()       { assertThat(parse("\"hello\"").asString()).isEqualTo("hello"); }
        @Test void emptyString()  { assertThat(parse("\"\"").asString()).isEmpty(); }
        @Test void integer()      { assertThat(parse("42").asInt()).isEqualTo(42); }
        @Test void negative()     { assertThat(parse("-7").asInt()).isEqualTo(-7); }
        @Test void zero()         { assertThat(parse("0").asInt()).isEqualTo(0); }
        @Test void longValue()    { assertThat(parse("9999999999").asLong()).isEqualTo(9999999999L); }
        @Test void decimal()      { assertThat(parse("3.14").asDouble()).isCloseTo(3.14, within(0.001)); }
        @Test void exponent()     { assertThat(parse("1e10").asDouble()).isCloseTo(1e10, within(1.0)); }
        @Test void negativeExp()  { assertThat(parse("1.5e-3").asDouble()).isCloseTo(0.0015, within(0.0001)); }
        @Test void boolTrue()     { assertThat(parse("true").asBoolean()).isTrue(); }
        @Test void boolFalse()    { assertThat(parse("false").asBoolean()).isFalse(); }
        @Test void nullValue()    { assertThat(parse("null").isNull()).isTrue(); }
    }

    @Nested
    @DisplayName("字符串转义")
    class EscapeTests {

        @Test void escapedQuote()     { assertThat(parse("\"a\\\"b\"").asString()).isEqualTo("a\"b"); }
        @Test void escapedBackslash() { assertThat(parse("\"a\\\\b\"").asString()).isEqualTo("a\\b"); }
        @Test void escapedSlash()     { assertThat(parse("\"a\\/b\"").asString()).isEqualTo("a/b"); }
        @Test void escapedNewline()   { assertThat(parse("\"a\\nb\"").asString()).isEqualTo("a\nb"); }
        @Test void escapedTab()       { assertThat(parse("\"a\\tb\"").asString()).isEqualTo("a\tb"); }
        @Test void escapedCr()        { assertThat(parse("\"a\\rb\"").asString()).isEqualTo("a\rb"); }
        @Test void escapedUnicode()   { assertThat(parse("\"\\u0041\"").asString()).isEqualTo("A"); }
        @Test void escapedBackspace() { assertThat(parse("\"a\\bb\"").asString()).isEqualTo("a\bb"); }
        @Test void escapedFormfeed()  { assertThat(parse("\"a\\fb\"").asString()).isEqualTo("a\fb"); }
    }

    @Nested
    @DisplayName("对象解析")
    class ObjectTests {

        @Test
        @DisplayName("空对象")
        void emptyObject() {
            JsonNode node = parse("{}");
            assertThat(node.isObject()).isTrue();
        }

        @Test
        @DisplayName("单属性对象")
        void singleProp() {
            JsonNode node = parse("{\"name\":\"Alice\"}");
            assertThat(node.get("name").asString()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("多属性对象")
        void multiProps() {
            JsonNode node = parse("{\"a\":1,\"b\":\"two\",\"c\":true}");
            assertThat(node.get("a").asInt()).isEqualTo(1);
            assertThat(node.get("b").asString()).isEqualTo("two");
            assertThat(node.get("c").asBoolean()).isTrue();
        }

        @Test
        @DisplayName("嵌套对象")
        void nested() {
            JsonNode node = parse("{\"inner\":{\"x\":1}}");
            assertThat(node.get("inner").get("x").asInt()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("数组解析")
    class ArrayTests {

        @Test void emptyArray() { assertThat(parse("[]").isArray()).isTrue(); }

        @Test
        @DisplayName("混合类型数组")
        void mixedArray() {
            JsonNode arr = parse("[1,\"two\",true,null]");
            assertThat(arr.get(0).asInt()).isEqualTo(1);
            assertThat(arr.get(1).asString()).isEqualTo("two");
            assertThat(arr.get(2).asBoolean()).isTrue();
            assertThat(arr.get(3).isNull()).isTrue();
        }

        @Test
        @DisplayName("嵌套数组")
        void nestedArray() {
            JsonNode arr = parse("[[1,2],[3,4]]");
            assertThat(arr.get(0).get(0).asInt()).isEqualTo(1);
            assertThat(arr.get(1).get(1).asInt()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("空白处理")
    class WhitespaceTests {

        @Test
        @DisplayName("多余空白")
        void extraWhitespace() {
            JsonNode node = parse("  { \"a\" : 1 , \"b\" : 2 }  ");
            assertThat(node.get("a").asInt()).isEqualTo(1);
        }

        @Test
        @DisplayName("换行和制表符")
        void newlinesAndTabs() {
            JsonNode node = parse("{\n\t\"x\"\n:\n1\n}");
            assertThat(node.get("x").asInt()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("错误处理")
    class ErrorTests {

        @Test void empty()        { assertThatThrownBy(() -> parse("")).isInstanceOf(OpenJsonProcessingException.class); }
        @Test void blank()        { assertThatThrownBy(() -> parse("   ")).isInstanceOf(OpenJsonProcessingException.class); }
        @Test void incomplete()   { assertThatThrownBy(() -> parse("{\"a\":")).isInstanceOf(OpenJsonProcessingException.class); }
        @Test void trailing()     { assertThatThrownBy(() -> parse("{}extra")).isInstanceOf(OpenJsonProcessingException.class); }
        @Test void badEscape()    { assertThatThrownBy(() -> parse("\"\\x\"")).isInstanceOf(OpenJsonProcessingException.class); }
        @Test void noCloseBrace() { assertThatThrownBy(() -> parse("{\"a\":1")).isInstanceOf(OpenJsonProcessingException.class); }
        @Test void noCloseArray() { assertThatThrownBy(() -> parse("[1,2")).isInstanceOf(OpenJsonProcessingException.class); }
        @Test void badNumber()    { assertThatThrownBy(() -> parse("01")).isInstanceOf(OpenJsonProcessingException.class); }
        @Test void invalidStep()  { assertThatThrownBy(() -> parse("1.")).isInstanceOf(OpenJsonProcessingException.class); }

        @Test
        @DisplayName("嵌套深度超限")
        void depthLimit() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 600; i++) sb.append("[");
            for (int i = 0; i < 600; i++) sb.append("]");
            assertThatThrownBy(() -> parse(sb.toString())).isInstanceOf(OpenJsonProcessingException.class);
        }
    }

    @Nested
    @DisplayName("大数字精度")
    class BigNumberTests {

        @Test
        @DisplayName("BigDecimal 精度")
        void bigDecimal() {
            JsonNode node = parse("123456789012345678901234567890.123456789");
            assertThat(node.asBigDecimal()).isEqualTo(new BigDecimal("123456789012345678901234567890.123456789"));
        }

        @Test
        @DisplayName("超大整数")
        void bigInteger() {
            JsonNode node = parse("99999999999999999999999999999999");
            assertThat(node.asBigDecimal()).isNotNull();
        }
    }
}
