package cloud.opencode.base.json.internal;

import cloud.opencode.base.json.stream.JsonReader;
import cloud.opencode.base.json.stream.JsonToken;
import org.junit.jupiter.api.*;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * BuiltinJsonReaderTest Tests
 * BuiltinJsonReaderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("BuiltinJsonReader 测试")
class BuiltinJsonReaderTest {

    private JsonReader reader(String json) {
        return new BuiltinJsonReader(new StringReader(json));
    }

    @Nested
    @DisplayName("对象读取")
    class ObjectTests {

        @Test void emptyObject() throws Exception {
            try (var r = reader("{}")) {
                r.beginObject();
                assertThat(r.hasNext()).isFalse();
                r.endObject();
            }
        }

        @Test void singleProperty() throws Exception {
            try (var r = reader("{\"name\":\"Alice\"}")) {
                r.beginObject();
                assertThat(r.hasNext()).isTrue();
                assertThat(r.nextName()).isEqualTo("name");
                assertThat(r.nextString()).isEqualTo("Alice");
                assertThat(r.hasNext()).isFalse();
                r.endObject();
            }
        }

        @Test void multipleProperties() throws Exception {
            try (var r = reader("{\"a\":1,\"b\":\"two\",\"c\":true,\"d\":null}")) {
                r.beginObject();
                assertThat(r.nextName()).isEqualTo("a");
                assertThat(r.nextInt()).isEqualTo(1);
                assertThat(r.nextName()).isEqualTo("b");
                assertThat(r.nextString()).isEqualTo("two");
                assertThat(r.nextName()).isEqualTo("c");
                assertThat(r.nextBoolean()).isTrue();
                assertThat(r.nextName()).isEqualTo("d");
                r.nextNull();
                assertThat(r.hasNext()).isFalse();
                r.endObject();
            }
        }

        @Test void nestedObject() throws Exception {
            try (var r = reader("{\"inner\":{\"x\":1}}")) {
                r.beginObject();
                assertThat(r.nextName()).isEqualTo("inner");
                r.beginObject();
                assertThat(r.nextName()).isEqualTo("x");
                assertThat(r.nextInt()).isEqualTo(1);
                r.endObject();
                r.endObject();
            }
        }
    }

    @Nested
    @DisplayName("数组读取")
    class ArrayTests {

        @Test void emptyArray() throws Exception {
            try (var r = reader("[]")) {
                r.beginArray();
                assertThat(r.hasNext()).isFalse();
                r.endArray();
            }
        }

        @Test void intArray() throws Exception {
            try (var r = reader("[1,2,3]")) {
                r.beginArray();
                assertThat(r.nextInt()).isEqualTo(1);
                assertThat(r.nextInt()).isEqualTo(2);
                assertThat(r.nextInt()).isEqualTo(3);
                assertThat(r.hasNext()).isFalse();
                r.endArray();
            }
        }

        @Test void mixedArray() throws Exception {
            try (var r = reader("[\"a\",1,true,null]")) {
                r.beginArray();
                assertThat(r.nextString()).isEqualTo("a");
                assertThat(r.nextInt()).isEqualTo(1);
                assertThat(r.nextBoolean()).isTrue();
                r.nextNull();
                r.endArray();
            }
        }

        @Test void nestedArray() throws Exception {
            try (var r = reader("[[1],[2]]")) {
                r.beginArray();
                r.beginArray();
                assertThat(r.nextInt()).isEqualTo(1);
                r.endArray();
                r.beginArray();
                assertThat(r.nextInt()).isEqualTo(2);
                r.endArray();
                r.endArray();
            }
        }
    }

    @Nested
    @DisplayName("数字类型")
    class NumberTests {

        @Test void longValue() throws Exception {
            try (var r = reader("[9999999999]")) {
                r.beginArray();
                assertThat(r.nextLong()).isEqualTo(9999999999L);
                r.endArray();
            }
        }

        @Test void doubleValue() throws Exception {
            try (var r = reader("[3.14]")) {
                r.beginArray();
                assertThat(r.nextDouble()).isCloseTo(3.14, within(0.001));
                r.endArray();
            }
        }

        @Test void bigDecimal() throws Exception {
            try (var r = reader("[123.456]")) {
                r.beginArray();
                assertThat(r.nextBigDecimal()).isEqualTo(new BigDecimal("123.456"));
                r.endArray();
            }
        }

        @Test void bigInteger() throws Exception {
            try (var r = reader("[999]")) {
                r.beginArray();
                assertThat(r.nextBigInteger()).isEqualTo(BigInteger.valueOf(999));
                r.endArray();
            }
        }

        @Test void nextNumber() throws Exception {
            try (var r = reader("[42]")) {
                r.beginArray();
                assertThat(r.nextNumber()).isEqualTo(42);
                r.endArray();
            }
        }

        @Test void negative() throws Exception {
            try (var r = reader("[-7]")) {
                r.beginArray();
                assertThat(r.nextInt()).isEqualTo(-7);
                r.endArray();
            }
        }
    }

    @Nested
    @DisplayName("字符串转义")
    class StringTests {

        @Test void escaped() throws Exception {
            try (var r = reader("[\"a\\\"b\\\\c\\n\"]")) {
                r.beginArray();
                assertThat(r.nextString()).isEqualTo("a\"b\\c\n");
                r.endArray();
            }
        }

        @Test void unicode() throws Exception {
            try (var r = reader("[\"\\u0041\"]")) {
                r.beginArray();
                assertThat(r.nextString()).isEqualTo("A");
                r.endArray();
            }
        }

        @Test void numberAsString() throws Exception {
            try (var r = reader("[42]")) {
                r.beginArray();
                assertThat(r.nextString()).isEqualTo("42");
                r.endArray();
            }
        }
    }

    @Nested
    @DisplayName("peek 和 skipValue")
    class PeekSkipTests {

        @Test void peekTokens() throws Exception {
            try (var r = reader("{\"a\":1}")) {
                assertThat(r.peek()).isEqualTo(JsonToken.START_OBJECT);
                r.beginObject();
                assertThat(r.peek()).isEqualTo(JsonToken.NAME);
                r.nextName();
                assertThat(r.peek()).isEqualTo(JsonToken.NUMBER);
                r.nextInt();
                assertThat(r.peek()).isEqualTo(JsonToken.END_OBJECT);
                r.endObject();
            }
        }

        @Test void skipPrimitive() throws Exception {
            try (var r = reader("{\"a\":1,\"b\":2}")) {
                r.beginObject();
                r.nextName();
                r.skipValue();
                assertThat(r.nextName()).isEqualTo("b");
                assertThat(r.nextInt()).isEqualTo(2);
                r.endObject();
            }
        }

        @Test void skipObject() throws Exception {
            try (var r = reader("{\"a\":{\"x\":1},\"b\":2}")) {
                r.beginObject();
                r.nextName();
                r.skipValue(); // skip nested object
                assertThat(r.nextName()).isEqualTo("b");
                assertThat(r.nextInt()).isEqualTo(2);
                r.endObject();
            }
        }

        @Test void skipArray() throws Exception {
            try (var r = reader("{\"a\":[1,2,3],\"b\":4}")) {
                r.beginObject();
                r.nextName();
                r.skipValue(); // skip array
                assertThat(r.nextName()).isEqualTo("b");
                assertThat(r.nextInt()).isEqualTo(4);
                r.endObject();
            }
        }
    }

    @Nested
    @DisplayName("位置和配置")
    class MetadataTests {

        @Test void lineColumn() throws Exception {
            try (var r = reader("{}")) {
                assertThat(r.getLineNumber()).isGreaterThanOrEqualTo(1);
                assertThat(r.getColumnNumber()).isGreaterThanOrEqualTo(1);
            }
        }

        @Test void path() throws Exception {
            try (var r = reader("{\"a\":1}")) {
                assertThat(r.getPath()).isNotNull();
            }
        }

        @Test void lenient() throws Exception {
            try (var r = reader("{}")) {
                assertThat(r.isLenient()).isFalse();
                r.setLenient(true);
                assertThat(r.isLenient()).isTrue();
            }
        }
    }
}
