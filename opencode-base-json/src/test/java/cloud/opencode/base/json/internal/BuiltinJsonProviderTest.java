package cloud.opencode.base.json.internal;

import cloud.opencode.base.json.JsonConfig;
import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.TypeReference;
import cloud.opencode.base.json.annotation.JsonIgnore;
import cloud.opencode.base.json.annotation.JsonProperty;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;
import cloud.opencode.base.json.spi.JsonFeature;
import cloud.opencode.base.json.stream.JsonReader;
import cloud.opencode.base.json.stream.JsonToken;
import cloud.opencode.base.json.stream.JsonWriter;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * BuiltinJsonProviderTest Tests
 * BuiltinJsonProviderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("BuiltinJsonProvider 测试")
class BuiltinJsonProviderTest {

    private final BuiltinJsonProvider provider = new BuiltinJsonProvider();

    // ==================== Test POJOs ====================

    public static class User {
        @JsonProperty("user_name")
        public String name;
        public int age;
        @JsonIgnore
        public String secret;
        public List<String> tags;

        public User() {}
        public User(String name, int age) { this.name = name; this.age = age; }
    }

    public record UserRecord(String name, int age) {}

    public enum Color { RED, GREEN, BLUE }

    public static class NestedPojo {
        public User user;
        public List<User> friends;
        public Map<String, Integer> scores;
        public NestedPojo() {}
    }

    // ==================== Provider Info ====================

    @Nested
    @DisplayName("提供者信息")
    class ProviderInfoTests {

        @Test void name()      { assertThat(provider.getName()).isEqualTo("builtin"); }
        @Test void version()   { assertThat(provider.getVersion()).isEqualTo("1.0.0"); }
        @Test void priority()  { assertThat(provider.getPriority()).isEqualTo(-100); }
        @Test void available() { assertThat(provider.isAvailable()).isTrue(); }

        @Test
        @DisplayName("支持的特性")
        void supportsFeature() {
            assertThat(provider.supportsFeature(JsonFeature.PRETTY_PRINT)).isTrue();
            assertThat(provider.supportsFeature(JsonFeature.LIMIT_NESTING_DEPTH)).isTrue();
            assertThat(provider.supportsFeature(JsonFeature.IGNORE_UNKNOWN_PROPERTIES)).isTrue();
        }

        @Test
        @DisplayName("configure 不抛异常")
        void configure() {
            assertThatNoException().isThrownBy(() -> provider.configure(JsonConfig.DEFAULT));
            assertThatNoException().isThrownBy(() -> provider.configure(null));
        }

        @Test
        @DisplayName("copy 返回独立实例")
        void copy() {
            var copy = provider.copy();
            assertThat(copy).isNotSameAs(provider);
            assertThat(copy.getName()).isEqualTo("builtin");
        }

        @Test
        @DisplayName("getUnderlyingProvider 返回自身")
        void underlying() {
            BuiltinJsonProvider p = provider.getUnderlyingProvider();
            assertThat(p).isSameAs(provider);
        }
    }

    // ==================== Serialization ====================

    @Nested
    @DisplayName("序列化测试")
    class SerializationTests {

        @Test
        @DisplayName("toJson - Map")
        void toJson_map() {
            String json = provider.toJson(Map.of("a", 1, "b", "hello"));
            assertThat(json).contains("\"a\"").contains("\"b\"").contains("\"hello\"");
        }

        @Test
        @DisplayName("toJson - List")
        void toJson_list() {
            assertThat(provider.toJson(List.of(1, 2, 3))).isEqualTo("[1,2,3]");
        }

        @Test
        @DisplayName("toJson - POJO")
        void toJson_pojo() {
            User user = new User("Alice", 30);
            user.secret = "s3cret";
            user.tags = List.of("java", "dev");
            String json = provider.toJson(user);
            assertThat(json).contains("\"user_name\"").contains("\"Alice\"");
            assertThat(json).contains("\"age\"").contains("30");
            assertThat(json).doesNotContain("secret").doesNotContain("s3cret");
            assertThat(json).contains("\"tags\"");
        }

        @Test
        @DisplayName("toJson - Record")
        void toJson_record() {
            String json = provider.toJson(new UserRecord("Bob", 25));
            assertThat(json).contains("\"name\"").contains("\"Bob\"").contains("25");
        }

        @Test
        @DisplayName("toJson - null")
        void toJson_null() {
            assertThat(provider.toJson(null)).isEqualTo("null");
        }

        @Test
        @DisplayName("toJson - 基本类型")
        void toJson_primitives() {
            assertThat(provider.toJson("hello")).isEqualTo("\"hello\"");
            assertThat(provider.toJson(42)).isEqualTo("42");
            assertThat(provider.toJson(true)).isEqualTo("true");
        }

        @Test
        @DisplayName("toJson - Enum")
        void toJson_enum() {
            assertThat(provider.toJson(Color.RED)).isEqualTo("\"RED\"");
        }

        @Test
        @DisplayName("toJsonBytes")
        void toJsonBytes() {
            byte[] bytes = provider.toJsonBytes(Map.of("k", "v"));
            assertThat(new String(bytes, StandardCharsets.UTF_8)).contains("\"k\"");
        }

        @Test
        @DisplayName("toJson(obj, OutputStream)")
        void toJson_outputStream() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            provider.toJson(Map.of("x", 1), out);
            assertThat(out.toString(StandardCharsets.UTF_8)).contains("\"x\"");
        }

        @Test
        @DisplayName("toJson(obj, Writer)")
        void toJson_writer() {
            StringWriter sw = new StringWriter();
            provider.toJson(List.of(1, 2), sw);
            assertThat(sw.toString()).isEqualTo("[1,2]");
        }
    }

    // ==================== Deserialization ====================

    @Nested
    @DisplayName("反序列化测试")
    class DeserializationTests {

        @Test
        @DisplayName("fromJson - POJO")
        void fromJson_pojo() {
            User user = provider.fromJson("{\"user_name\":\"Alice\",\"age\":30}", User.class);
            assertThat(user.name).isEqualTo("Alice");
            assertThat(user.age).isEqualTo(30);
        }

        @Test
        @DisplayName("fromJson - Record")
        void fromJson_record() {
            UserRecord r = provider.fromJson("{\"name\":\"Bob\",\"age\":25}", UserRecord.class);
            assertThat(r.name()).isEqualTo("Bob");
            assertThat(r.age()).isEqualTo(25);
        }

        @Test
        @DisplayName("fromJson - Map")
        @SuppressWarnings("unchecked")
        void fromJson_map() {
            Map<String, Object> map = provider.fromJson("{\"a\":1}", Map.class);
            assertThat(map).containsKey("a");
        }

        @Test
        @DisplayName("fromJson - List")
        void fromJson_list() {
            List<?> list = provider.fromJson("[1,2,3]", List.class);
            assertThat(list).hasSize(3);
        }

        @Test
        @DisplayName("fromJson - String")
        void fromJson_string() {
            assertThat(provider.fromJson("\"hello\"", String.class)).isEqualTo("hello");
        }

        @Test
        @DisplayName("fromJson - Integer")
        void fromJson_int() {
            assertThat(provider.fromJson("42", Integer.class)).isEqualTo(42);
        }

        @Test
        @DisplayName("fromJson - Boolean")
        void fromJson_boolean() {
            assertThat(provider.fromJson("true", Boolean.class)).isTrue();
        }

        @Test
        @DisplayName("fromJson - Enum")
        void fromJson_enum() {
            assertThat(provider.fromJson("\"GREEN\"", Color.class)).isEqualTo(Color.GREEN);
        }

        @Test
        @DisplayName("fromJson - JsonNode")
        void fromJson_jsonNode() {
            JsonNode node = provider.fromJson("{\"a\":1}", JsonNode.class);
            assertThat(node.isObject()).isTrue();
        }

        @Test
        @DisplayName("fromJson - byte[]")
        void fromJson_bytes() {
            User u = provider.fromJson("{\"user_name\":\"X\",\"age\":1}".getBytes(StandardCharsets.UTF_8), User.class);
            assertThat(u.name).isEqualTo("X");
        }

        @Test
        @DisplayName("fromJson - byte[] with TypeReference")
        void fromJson_bytes_typeReference() {
            Map<String, Integer> map = provider.fromJson(
                    "{\"a\":1}".getBytes(StandardCharsets.UTF_8),
                    new TypeReference<Map<String, Integer>>() {});
            assertThat(map).containsEntry("a", 1);
        }

        @Test
        @DisplayName("fromJson - InputStream")
        void fromJson_inputStream() {
            InputStream in = new ByteArrayInputStream("{\"user_name\":\"Y\",\"age\":2}".getBytes(StandardCharsets.UTF_8));
            User u = provider.fromJson(in, User.class);
            assertThat(u.name).isEqualTo("Y");
        }

        @Test
        @DisplayName("fromJson - Reader")
        void fromJson_reader() {
            StringReader sr = new StringReader("{\"user_name\":\"Z\",\"age\":3}");
            User u = provider.fromJson(sr, User.class);
            assertThat(u.name).isEqualTo("Z");
        }

        @Test
        @DisplayName("fromJson - Type (generic)")
        void fromJson_type() {
            Object result = provider.fromJson("{\"a\":1}", Map.class);
            assertThat(result).isInstanceOf(Map.class);
        }

        @Test
        @DisplayName("fromJson - TypeReference")
        void fromJson_typeReference() {
            Map<String, Integer> map = provider.fromJson("{\"a\":1}", new TypeReference<Map<String, Integer>>() {});
            assertThat(map).containsEntry("a", 1);
        }

        @Test
        @DisplayName("fromJson - 嵌套对象")
        void fromJson_nested() {
            String json = "{\"user\":{\"user_name\":\"N\",\"age\":1},\"friends\":[],\"scores\":{\"math\":100}}";
            NestedPojo n = provider.fromJson(json, NestedPojo.class);
            assertThat(n.user.name).isEqualTo("N");
            assertThat(n.scores).containsEntry("math", 100);
        }

        @Test
        @DisplayName("fromJsonArray")
        void fromJsonArray() {
            List<Integer> nums = provider.fromJsonArray("[1,2,3]", Integer.class);
            assertThat(nums).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("fromJsonMap")
        void fromJsonMap() {
            Map<String, Integer> map = provider.fromJsonMap("{\"a\":1,\"b\":2}", String.class, Integer.class);
            assertThat(map).containsEntry("a", 1).containsEntry("b", 2);
        }
    }

    // ==================== Tree Operations ====================

    @Nested
    @DisplayName("树操作测试")
    class TreeTests {

        @Test
        @DisplayName("parseTree - String")
        void parseTree_string() {
            JsonNode node = provider.parseTree("{\"a\":1}");
            assertThat(node.isObject()).isTrue();
            assertThat(node.get("a").asInt()).isEqualTo(1);
        }

        @Test
        @DisplayName("parseTree - byte[]")
        void parseTree_bytes() {
            JsonNode node = provider.parseTree("{\"b\":2}".getBytes(StandardCharsets.UTF_8));
            assertThat(node.get("b").asInt()).isEqualTo(2);
        }

        @Test
        @DisplayName("treeToValue - POJO")
        void treeToValue() {
            JsonNode node = provider.parseTree("{\"user_name\":\"T\",\"age\":5}");
            User u = provider.treeToValue(node, User.class);
            assertThat(u.name).isEqualTo("T");
        }

        @Test
        @DisplayName("valueToTree - POJO")
        void valueToTree() {
            User u = new User("V", 7);
            JsonNode node = provider.valueToTree(u);
            assertThat(node.isObject()).isTrue();
            assertThat(node.get("user_name").asString()).isEqualTo("V");
        }
    }

    // ==================== Streaming ====================

    @Nested
    @DisplayName("流式API测试")
    class StreamingTests {

        @Test
        @DisplayName("createReader - InputStream")
        void reader_inputStream() throws Exception {
            InputStream in = new ByteArrayInputStream("{\"x\":1}".getBytes(StandardCharsets.UTF_8));
            try (JsonReader reader = provider.createReader(in)) {
                assertThat(reader.peek()).isEqualTo(JsonToken.START_OBJECT);
                reader.beginObject();
                assertThat(reader.hasNext()).isTrue();
            }
        }

        @Test
        @DisplayName("createReader - Reader")
        void reader_reader() throws Exception {
            try (JsonReader reader = provider.createReader(new StringReader("[1,2]"))) {
                assertThat(reader.peek()).isEqualTo(JsonToken.START_ARRAY);
                reader.beginArray();
                assertThat(reader.hasNext()).isTrue();
            }
        }

        @Test
        @DisplayName("createWriter - OutputStream")
        void writer_outputStream() throws Exception {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (JsonWriter writer = provider.createWriter(out)) {
                writer.beginObject().name("k").value("v").endObject();
            }
            assertThat(out.toString(StandardCharsets.UTF_8)).contains("\"k\"").contains("\"v\"");
        }

        @Test
        @DisplayName("createWriter - Writer")
        void writer_writer() throws Exception {
            StringWriter sw = new StringWriter();
            try (JsonWriter writer = provider.createWriter(sw)) {
                writer.beginArray().value(1).value(2).endArray();
            }
            assertThat(sw.toString()).isEqualTo("[1,2]");
        }
    }

    // ==================== Type Conversion ====================

    @Nested
    @DisplayName("类型转换测试")
    class ConversionTests {

        @Test
        @DisplayName("convertValue - Class")
        void convertValue_class() {
            Map<String, Object> map = Map.of("user_name", "C", "age", 10);
            User u = provider.convertValue(map, User.class);
            assertThat(u.name).isEqualTo("C");
        }

        @Test
        @DisplayName("convertValue - TypeReference")
        void convertValue_typeRef() {
            List<Integer> list = provider.convertValue(List.of(1, 2), new TypeReference<List<Integer>>() {});
            assertThat(list).containsExactly(1, 2);
        }
    }

    // ==================== Error Cases ====================

    @Nested
    @DisplayName("异常测试")
    class ErrorTests {

        @Test
        @DisplayName("fromJson null json 抛 NPE")
        void fromJson_null_json() {
            assertThatThrownBy(() -> provider.fromJson((String) null, User.class))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fromJson null byte[] 抛 NPE")
        void fromJson_null_bytes() {
            assertThatThrownBy(() -> provider.fromJson((byte[]) null, User.class))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fromJson null byte[] with TypeReference 抛 NPE")
        void fromJson_null_bytes_typeReference() {
            assertThatThrownBy(() -> provider.fromJson((byte[]) null, new TypeReference<Map<String, Integer>>() {}))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fromJson byte[] with null TypeReference 抛 NPE")
        void fromJson_bytes_null_typeReference() {
            assertThatThrownBy(() -> provider.fromJson("{}".getBytes(StandardCharsets.UTF_8), (TypeReference<?>) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fromJson 无效 JSON 抛异常")
        void fromJson_invalid() {
            assertThatThrownBy(() -> provider.fromJson("{invalid}", User.class))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("parseTree null 抛 NPE")
        void parseTree_null() {
            assertThatThrownBy(() -> provider.parseTree((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fromJsonArray 非数组抛异常")
        void fromJsonArray_notArray() {
            assertThatThrownBy(() -> provider.fromJsonArray("{}", Integer.class))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }

        @Test
        @DisplayName("fromJsonMap 非对象抛异常")
        void fromJsonMap_notObject() {
            assertThatThrownBy(() -> provider.fromJsonMap("[]", String.class, Integer.class))
                    .isInstanceOf(OpenJsonProcessingException.class);
        }
    }
}
