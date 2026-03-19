package cloud.opencode.base.json.internal;

import cloud.opencode.base.json.stream.JsonWriter;
import org.junit.jupiter.api.*;

import java.io.StringWriter;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * BuiltinJsonWriterTest Tests
 * BuiltinJsonWriterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("BuiltinJsonWriter 测试")
class BuiltinJsonWriterTest {

    private StringWriter sw;
    private JsonWriter writer;

    @BeforeEach
    void setUp() {
        sw = new StringWriter();
        writer = new BuiltinJsonWriter(sw);
    }

    @AfterEach
    void tearDown() {
        writer.close();
    }

    @Nested
    @DisplayName("对象写入")
    class ObjectTests {

        @Test void emptyObject() {
            writer.beginObject().endObject();
            writer.flush();
            assertThat(sw.toString()).isEqualTo("{}");
        }

        @Test void singleProperty() {
            writer.beginObject().name("a").value("b").endObject();
            writer.flush();
            assertThat(sw.toString()).isEqualTo("{\"a\":\"b\"}");
        }

        @Test void multipleProperties() {
            writer.beginObject()
                    .name("a").value(1)
                    .name("b").value("two")
                    .name("c").value(true)
                    .endObject();
            writer.flush();
            assertThat(sw.toString()).contains("\"a\":1").contains("\"b\":\"two\"").contains("\"c\":true");
        }

        @Test void nestedObject() {
            writer.beginObject()
                    .name("inner").beginObject().name("x").value(1).endObject()
                    .endObject();
            writer.flush();
            assertThat(sw.toString()).contains("\"inner\":{\"x\":1}");
        }
    }

    @Nested
    @DisplayName("数组写入")
    class ArrayTests {

        @Test void emptyArray() {
            writer.beginArray().endArray();
            writer.flush();
            assertThat(sw.toString()).isEqualTo("[]");
        }

        @Test void intArray() {
            writer.beginArray().value(1).value(2).value(3).endArray();
            writer.flush();
            assertThat(sw.toString()).isEqualTo("[1,2,3]");
        }

        @Test void mixedArray() {
            writer.beginArray().value("a").value(1).value(true).nullValue().endArray();
            writer.flush();
            assertThat(sw.toString()).isEqualTo("[\"a\",1,true,null]");
        }

        @Test void nestedArrays() {
            writer.beginArray()
                    .beginArray().value(1).endArray()
                    .beginArray().value(2).endArray()
                    .endArray();
            writer.flush();
            assertThat(sw.toString()).isEqualTo("[[1],[2]]");
        }
    }

    @Nested
    @DisplayName("值类型写入")
    class ValueTests {

        @Test void stringValue()  { writer.beginArray().value("hello").endArray(); writer.flush(); assertThat(sw.toString()).contains("\"hello\""); }
        @Test void intValue()     { writer.beginArray().value(42).endArray(); writer.flush(); assertThat(sw.toString()).isEqualTo("[42]"); }
        @Test void longValue()    { writer.beginArray().value(9999999999L).endArray(); writer.flush(); assertThat(sw.toString()).contains("9999999999"); }
        @Test void doubleValue()  { writer.beginArray().value(3.14).endArray(); writer.flush(); assertThat(sw.toString()).contains("3.14"); }
        @Test void boolTrue()     { writer.beginArray().value(true).endArray(); writer.flush(); assertThat(sw.toString()).isEqualTo("[true]"); }
        @Test void boolFalse()    { writer.beginArray().value(false).endArray(); writer.flush(); assertThat(sw.toString()).isEqualTo("[false]"); }
        @Test void nullValue()    { writer.beginArray().nullValue().endArray(); writer.flush(); assertThat(sw.toString()).isEqualTo("[null]"); }
        @Test void numberValue()  { writer.beginArray().value((Number) BigDecimal.valueOf(1.5)).endArray(); writer.flush(); assertThat(sw.toString()).contains("1.5"); }

        @Test void jsonValue() {
            writer.beginObject().name("raw").jsonValue("[1,2]").endObject();
            writer.flush();
            assertThat(sw.toString()).contains("[1,2]");
        }

        @Test void nullString() {
            writer.beginArray().value((String) null).endArray();
            writer.flush();
            assertThat(sw.toString()).isEqualTo("[null]");
        }
    }

    @Nested
    @DisplayName("字符串转义")
    class EscapeTests {

        @Test void escapedChars() {
            writer.beginArray().value("a\"b\\c\n\t\r").endArray();
            writer.flush();
            String result = sw.toString();
            assertThat(result).contains("\\\"").contains("\\\\").contains("\\n").contains("\\t");
        }

        @Test void controlChar() {
            writer.beginArray().value("a\u0001b").endArray();
            writer.flush();
            assertThat(sw.toString()).contains("\\u0001");
        }
    }

    @Nested
    @DisplayName("配置")
    class ConfigTests {

        @Test void indent() {
            writer.setIndent("  ");
            writer.beginObject().name("a").value(1).endObject();
            writer.flush();
            assertThat(sw.toString()).contains("\n").contains("  ");
        }

        @Test void lenient() {
            assertThat(writer.isLenient()).isFalse();
            writer.setLenient(true);
            assertThat(writer.isLenient()).isTrue();
        }

        @Test void htmlSafe() {
            assertThat(writer.isHtmlSafe()).isFalse();
            writer.setHtmlSafe(true);
            assertThat(writer.isHtmlSafe()).isTrue();
        }

        @Test void htmlSafeEscaping() {
            writer.setHtmlSafe(true);
            writer.beginArray().value("<script>").endArray();
            writer.flush();
            assertThat(sw.toString()).doesNotContain("<script>").contains("\\u003c");
        }

        @Test void serializeNulls() {
            writer.setSerializeNulls(true);
            writer.beginObject().name("a").nullValue().endObject();
            writer.flush();
            assertThat(sw.toString()).contains("null");
        }
    }

    @Nested
    @DisplayName("便捷方法")
    class ConvenienceTests {

        @Test void property() {
            writer.beginObject().property("k", "v").endObject();
            writer.flush();
            assertThat(sw.toString()).isEqualTo("{\"k\":\"v\"}");
        }

        @Test void propertyInt() {
            writer.beginObject().property("k", 42).endObject();
            writer.flush();
            assertThat(sw.toString()).isEqualTo("{\"k\":42}");
        }

        @Test void propertyNull() {
            writer.beginObject().propertyNull("k").endObject();
            writer.flush();
            assertThat(sw.toString()).contains("\"k\":null");
        }
    }
}
