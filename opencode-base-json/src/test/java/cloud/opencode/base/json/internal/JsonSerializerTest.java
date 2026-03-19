package cloud.opencode.base.json.internal;

import cloud.opencode.base.json.JsonNode;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonSerializerTest Tests
 * JsonSerializerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonSerializer 测试")
class JsonSerializerTest {

    private String serialize(Object obj) {
        return new JsonSerializer(false).serialize(obj);
    }

    private String serializePretty(Object obj) {
        return new JsonSerializer(true).serialize(obj);
    }

    @Nested
    @DisplayName("基本类型序列化")
    class PrimitiveTests {

        @Test void nullValue()  { assertThat(serialize(null)).isEqualTo("null"); }
        @Test void string()     { assertThat(serialize("hello")).isEqualTo("\"hello\""); }
        @Test void integer()    { assertThat(serialize(42)).isEqualTo("42"); }
        @Test void longValue()  { assertThat(serialize(100L)).isEqualTo("100"); }
        @Test void doubleValue(){ assertThat(serialize(3.14)).isEqualTo("3.14"); }
        @Test void boolTrue()   { assertThat(serialize(true)).isEqualTo("true"); }
        @Test void boolFalse()  { assertThat(serialize(false)).isEqualTo("false"); }
    }

    @Nested
    @DisplayName("特殊数字")
    class SpecialNumberTests {

        @Test void nan()             { assertThat(serialize(Double.NaN)).isEqualTo("null"); }
        @Test void positiveInfinity(){ assertThat(serialize(Double.POSITIVE_INFINITY)).isEqualTo("null"); }
        @Test void negativeInfinity(){ assertThat(serialize(Double.NEGATIVE_INFINITY)).isEqualTo("null"); }
        @Test void floatNaN()        { assertThat(serialize(Float.NaN)).isEqualTo("null"); }

        @Test
        @DisplayName("BigDecimal 使用 toPlainString")
        void bigDecimal() {
            assertThat(serialize(new BigDecimal("123.456"))).isEqualTo("123.456");
        }

        @Test
        @DisplayName("BigInteger")
        void bigInteger() {
            assertThat(serialize(new BigInteger("999999999999999999"))).isEqualTo("999999999999999999");
        }
    }

    @Nested
    @DisplayName("字符串转义")
    class StringEscapeTests {

        @Test void quote()     { assertThat(serialize("a\"b")).isEqualTo("\"a\\\"b\""); }
        @Test void backslash() { assertThat(serialize("a\\b")).isEqualTo("\"a\\\\b\""); }
        @Test void newline()   { assertThat(serialize("a\nb")).isEqualTo("\"a\\nb\""); }
        @Test void tab()       { assertThat(serialize("a\tb")).isEqualTo("\"a\\tb\""); }
        @Test void cr()        { assertThat(serialize("a\rb")).isEqualTo("\"a\\rb\""); }
        @Test void backspace() { assertThat(serialize("a\bb")).isEqualTo("\"a\\bb\""); }
        @Test void formfeed()  { assertThat(serialize("a\fb")).isEqualTo("\"a\\fb\""); }

        @Test
        @DisplayName("控制字符用 \\uXXXX")
        void controlChar() {
            String result = serialize("a\u0001b");
            assertThat(result).isEqualTo("\"a\\u0001b\"");
        }
    }

    @Nested
    @DisplayName("集合序列化")
    class CollectionTests {

        @Test
        @DisplayName("List")
        void list() {
            assertThat(serialize(List.of(1, 2, 3))).isEqualTo("[1,2,3]");
        }

        @Test
        @DisplayName("空 List")
        void emptyList() {
            assertThat(serialize(List.of())).isEqualTo("[]");
        }

        @Test
        @DisplayName("Set")
        void set() {
            String result = serialize(Set.of("a"));
            assertThat(result).isEqualTo("[\"a\"]");
        }

        @Test
        @DisplayName("Map")
        void map() {
            String result = serialize(Map.of("k", "v"));
            assertThat(result).contains("\"k\"").contains("\"v\"");
        }

        @Test
        @DisplayName("空 Map")
        void emptyMap() {
            assertThat(serialize(Map.of())).isEqualTo("{}");
        }
    }

    @Nested
    @DisplayName("数组序列化")
    class ArrayTests {

        @Test void objectArray()  { assertThat(serialize(new String[]{"a","b"})).isEqualTo("[\"a\",\"b\"]"); }
        @Test void intArray()     { assertThat(serialize(new int[]{1,2})).isEqualTo("[1,2]"); }
        @Test void longArray()    { assertThat(serialize(new long[]{1L,2L})).isEqualTo("[1,2]"); }
        @Test void doubleArray()  { assertThat(serialize(new double[]{1.1,2.2})).contains("1.1").contains("2.2"); }
        @Test void booleanArray() { assertThat(serialize(new boolean[]{true,false})).isEqualTo("[true,false]"); }
        @Test void emptyArray()   { assertThat(serialize(new int[0])).isEqualTo("[]"); }
    }

    @Nested
    @DisplayName("JsonNode 序列化")
    class JsonNodeTests {

        @Test
        @DisplayName("ObjectNode")
        void objectNode() {
            JsonNode node = JsonNode.object().put("a", 1).put("b", "two");
            String json = serialize(node);
            assertThat(json).contains("\"a\":1").contains("\"b\":\"two\"");
        }

        @Test
        @DisplayName("ArrayNode")
        void arrayNode() {
            JsonNode node = JsonNode.array().add(1).add("two").add(true);
            assertThat(serialize(node)).isEqualTo("[1,\"two\",true]");
        }

        @Test
        @DisplayName("NullNode")
        void nullNode() {
            assertThat(serialize(JsonNode.nullNode())).isEqualTo("null");
        }
    }

    @Nested
    @DisplayName("其他类型")
    class OtherTypeTests {

        @Test
        @DisplayName("Enum")
        void enumValue() {
            assertThat(serialize(Thread.State.RUNNABLE)).isEqualTo("\"RUNNABLE\"");
        }

        @Test
        @DisplayName("UUID")
        void uuid() {
            UUID u = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            assertThat(serialize(u)).isEqualTo("\"550e8400-e29b-41d4-a716-446655440000\"");
        }
    }

    @Nested
    @DisplayName("美化打印")
    class PrettyPrintTests {

        @Test
        @DisplayName("对象美化")
        void prettyObject() {
            String json = serializePretty(Map.of("a", 1));
            assertThat(json).contains("\n").contains("  ");
        }

        @Test
        @DisplayName("数组美化")
        void prettyArray() {
            String json = serializePretty(List.of(1, 2));
            assertThat(json).contains("\n");
        }
    }
}
