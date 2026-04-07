package cloud.opencode.base.json.spi;

import cloud.opencode.base.json.JsonConfig;
import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.TypeReference;
import cloud.opencode.base.json.stream.JsonReader;
import cloud.opencode.base.json.stream.JsonWriter;
import cloud.opencode.base.json.stream.JsonToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonProvider 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonProvider 测试")
class JsonProviderTest {

    @Nested
    @DisplayName("接口方法定义测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("接口定义了所有必要方法")
        void testInterfaceMethods() throws NoSuchMethodException {
            // Provider Information
            assertThat(JsonProvider.class.getMethod("getName")).isNotNull();
            assertThat(JsonProvider.class.getMethod("getVersion")).isNotNull();
            assertThat(JsonProvider.class.getMethod("getPriority")).isNotNull();
            assertThat(JsonProvider.class.getMethod("isAvailable")).isNotNull();

            // Configuration
            assertThat(JsonProvider.class.getMethod("configure", JsonConfig.class)).isNotNull();
            assertThat(JsonProvider.class.getMethod("supportsFeature", JsonFeature.class)).isNotNull();

            // Serialization
            assertThat(JsonProvider.class.getMethod("toJson", Object.class)).isNotNull();
            assertThat(JsonProvider.class.getMethod("toJsonBytes", Object.class)).isNotNull();
            assertThat(JsonProvider.class.getMethod("toJson", Object.class, OutputStream.class)).isNotNull();
            assertThat(JsonProvider.class.getMethod("toJson", Object.class, Writer.class)).isNotNull();

            // Deserialization
            assertThat(JsonProvider.class.getMethod("fromJson", String.class, Class.class)).isNotNull();
            assertThat(JsonProvider.class.getMethod("fromJson", String.class, Type.class)).isNotNull();
            assertThat(JsonProvider.class.getMethod("fromJson", String.class, TypeReference.class)).isNotNull();
            assertThat(JsonProvider.class.getMethod("fromJson", byte[].class, Class.class)).isNotNull();
            assertThat(JsonProvider.class.getMethod("fromJson", byte[].class, TypeReference.class)).isNotNull();
            assertThat(JsonProvider.class.getMethod("fromJson", InputStream.class, Class.class)).isNotNull();
            assertThat(JsonProvider.class.getMethod("fromJson", Reader.class, Class.class)).isNotNull();

            // Collection Deserialization
            assertThat(JsonProvider.class.getMethod("fromJsonArray", String.class, Class.class)).isNotNull();
            assertThat(JsonProvider.class.getMethod("fromJsonMap", String.class, Class.class, Class.class)).isNotNull();

            // JsonNode Operations
            assertThat(JsonProvider.class.getMethod("parseTree", String.class)).isNotNull();
            assertThat(JsonProvider.class.getMethod("parseTree", byte[].class)).isNotNull();
            assertThat(JsonProvider.class.getMethod("treeToValue", JsonNode.class, Class.class)).isNotNull();
            assertThat(JsonProvider.class.getMethod("valueToTree", Object.class)).isNotNull();

            // Streaming API
            assertThat(JsonProvider.class.getMethod("createReader", InputStream.class)).isNotNull();
            assertThat(JsonProvider.class.getMethod("createReader", Reader.class)).isNotNull();
            assertThat(JsonProvider.class.getMethod("createWriter", OutputStream.class)).isNotNull();
            assertThat(JsonProvider.class.getMethod("createWriter", Writer.class)).isNotNull();

            // Type Conversion
            assertThat(JsonProvider.class.getMethod("convertValue", Object.class, Class.class)).isNotNull();
            assertThat(JsonProvider.class.getMethod("convertValue", Object.class, TypeReference.class)).isNotNull();

            // Utility Methods
            assertThat(JsonProvider.class.getMethod("getUnderlyingProvider")).isNotNull();
            assertThat(JsonProvider.class.getMethod("copy")).isNotNull();
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("getPriority是默认方法")
        void testGetPriorityIsDefault() throws NoSuchMethodException {
            assertThat(JsonProvider.class.getMethod("getPriority").isDefault()).isTrue();
        }

        @Test
        @DisplayName("isAvailable是默认方法")
        void testIsAvailableIsDefault() throws NoSuchMethodException {
            assertThat(JsonProvider.class.getMethod("isAvailable").isDefault()).isTrue();
        }
    }

    @Nested
    @DisplayName("返回类型测试")
    class ReturnTypeTests {

        @Test
        @DisplayName("getName返回String")
        void testGetNameReturnType() throws NoSuchMethodException {
            assertThat(JsonProvider.class.getMethod("getName").getReturnType())
                .isEqualTo(String.class);
        }

        @Test
        @DisplayName("getVersion返回String")
        void testGetVersionReturnType() throws NoSuchMethodException {
            assertThat(JsonProvider.class.getMethod("getVersion").getReturnType())
                .isEqualTo(String.class);
        }

        @Test
        @DisplayName("getPriority返回int")
        void testGetPriorityReturnType() throws NoSuchMethodException {
            assertThat(JsonProvider.class.getMethod("getPriority").getReturnType())
                .isEqualTo(int.class);
        }

        @Test
        @DisplayName("isAvailable返回boolean")
        void testIsAvailableReturnType() throws NoSuchMethodException {
            assertThat(JsonProvider.class.getMethod("isAvailable").getReturnType())
                .isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("toJson返回String")
        void testToJsonReturnType() throws NoSuchMethodException {
            assertThat(JsonProvider.class.getMethod("toJson", Object.class).getReturnType())
                .isEqualTo(String.class);
        }

        @Test
        @DisplayName("toJsonBytes返回byte[]")
        void testToJsonBytesReturnType() throws NoSuchMethodException {
            assertThat(JsonProvider.class.getMethod("toJsonBytes", Object.class).getReturnType())
                .isEqualTo(byte[].class);
        }

        @Test
        @DisplayName("parseTree返回JsonNode")
        void testParseTreeReturnType() throws NoSuchMethodException {
            assertThat(JsonProvider.class.getMethod("parseTree", String.class).getReturnType())
                .isEqualTo(JsonNode.class);
        }

        @Test
        @DisplayName("createReader返回JsonReader")
        void testCreateReaderReturnType() throws NoSuchMethodException {
            assertThat(JsonProvider.class.getMethod("createReader", InputStream.class).getReturnType())
                .isEqualTo(JsonReader.class);
        }

        @Test
        @DisplayName("createWriter返回JsonWriter")
        void testCreateWriterReturnType() throws NoSuchMethodException {
            assertThat(JsonProvider.class.getMethod("createWriter", OutputStream.class).getReturnType())
                .isEqualTo(JsonWriter.class);
        }

        @Test
        @DisplayName("copy返回JsonProvider")
        void testCopyReturnType() throws NoSuchMethodException {
            assertThat(JsonProvider.class.getMethod("copy").getReturnType())
                .isEqualTo(JsonProvider.class);
        }
    }

    @Nested
    @DisplayName("Mock实现测试")
    class MockImplementationTests {

        @Test
        @DisplayName("可以创建Mock实现")
        void testMockImplementation() {
            JsonProvider provider = new MockJsonProvider();

            assertThat(provider.getName()).isEqualTo("mock");
            assertThat(provider.getVersion()).isEqualTo("1.0.0");
            assertThat(provider.getPriority()).isEqualTo(0);
            assertThat(provider.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("Mock实现序列化/反序列化")
        void testMockSerialization() {
            JsonProvider provider = new MockJsonProvider();

            String json = provider.toJson("test");
            assertThat(json).isEqualTo("\"test\"");

            String value = provider.fromJson("\"test\"", String.class);
            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("Mock实现tree操作")
        void testMockTreeOperations() {
            JsonProvider provider = new MockJsonProvider();

            JsonNode node = provider.parseTree("{}");
            assertThat(node).isNotNull();

            JsonNode tree = provider.valueToTree("test");
            assertThat(tree).isNotNull();
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("默认getPriority返回0")
        void testDefaultGetPriority() {
            JsonProvider provider = new MockJsonProvider();
            assertThat(provider.getPriority()).isEqualTo(0);
        }

        @Test
        @DisplayName("默认isAvailable返回true")
        void testDefaultIsAvailable() {
            JsonProvider provider = new MockJsonProvider();
            assertThat(provider.isAvailable()).isTrue();
        }
    }

    // Mock implementation for testing
    static class MockJsonProvider implements JsonProvider {

        @Override
        public String getName() {
            return "mock";
        }

        @Override
        public String getVersion() {
            return "1.0.0";
        }

        @Override
        public void configure(JsonConfig config) {
            // No-op
        }

        @Override
        public boolean supportsFeature(JsonFeature feature) {
            return true;
        }

        @Override
        public String toJson(Object obj) {
            if (obj instanceof String s) {
                return "\"" + s + "\"";
            }
            return obj.toString();
        }

        @Override
        public byte[] toJsonBytes(Object obj) {
            return toJson(obj).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public void toJson(Object obj, OutputStream output) {
            try {
                output.write(toJsonBytes(obj));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void toJson(Object obj, Writer writer) {
            try {
                writer.write(toJson(obj));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T fromJson(String json, Class<T> clazz) {
            if (clazz == String.class && json.startsWith("\"")) {
                return (T) json.substring(1, json.length() - 1);
            }
            return null;
        }

        @Override
        public <T> T fromJson(String json, Type type) {
            return null;
        }

        @Override
        public <T> T fromJson(String json, TypeReference<T> typeReference) {
            return null;
        }

        @Override
        public <T> T fromJson(byte[] json, Class<T> clazz) {
            return fromJson(new String(json, StandardCharsets.UTF_8), clazz);
        }

        @Override
        public <T> T fromJson(byte[] json, TypeReference<T> typeReference) {
            return fromJson(new String(json, StandardCharsets.UTF_8), typeReference);
        }

        @Override
        public <T> T fromJson(InputStream input, Class<T> clazz) {
            return null;
        }

        @Override
        public <T> T fromJson(Reader reader, Class<T> clazz) {
            return null;
        }

        @Override
        public <T> List<T> fromJsonArray(String json, Class<T> elementType) {
            return List.of();
        }

        @Override
        public <K, V> Map<K, V> fromJsonMap(String json, Class<K> keyType, Class<V> valueType) {
            return Map.of();
        }

        @Override
        public JsonNode parseTree(String json) {
            return JsonNode.object();
        }

        @Override
        public JsonNode parseTree(byte[] json) {
            return JsonNode.object();
        }

        @Override
        public <T> T treeToValue(JsonNode node, Class<T> clazz) {
            return null;
        }

        @Override
        public JsonNode valueToTree(Object obj) {
            return JsonNode.of(obj.toString());
        }

        @Override
        public JsonReader createReader(InputStream input) {
            return new MockJsonReader();
        }

        @Override
        public JsonReader createReader(Reader reader) {
            return new MockJsonReader();
        }

        @Override
        public JsonWriter createWriter(OutputStream output) {
            return new MockJsonWriter();
        }

        @Override
        public JsonWriter createWriter(Writer writer) {
            return new MockJsonWriter();
        }

        @Override
        public <T> T convertValue(Object obj, Class<T> clazz) {
            return null;
        }

        @Override
        public <T> T convertValue(Object obj, TypeReference<T> typeReference) {
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getUnderlyingProvider() {
            return (T) this;
        }

        @Override
        public JsonProvider copy() {
            return new MockJsonProvider();
        }
    }

    static class MockJsonReader implements JsonReader {
        @Override public void beginObject() {}
        @Override public void endObject() {}
        @Override public void beginArray() {}
        @Override public void endArray() {}
        @Override public boolean hasNext() { return false; }
        @Override public JsonToken peek() { return JsonToken.END_DOCUMENT; }
        @Override public String nextName() { return ""; }
        @Override public String nextString() { return ""; }
        @Override public boolean nextBoolean() { return false; }
        @Override public void nextNull() {}
        @Override public int nextInt() { return 0; }
        @Override public long nextLong() { return 0; }
        @Override public double nextDouble() { return 0; }
        @Override public java.math.BigInteger nextBigInteger() { return java.math.BigInteger.ZERO; }
        @Override public java.math.BigDecimal nextBigDecimal() { return java.math.BigDecimal.ZERO; }
        @Override public Number nextNumber() { return 0; }
        @Override public void skipValue() {}
        @Override public String getPath() { return "$"; }
        @Override public int getLineNumber() { return 1; }
        @Override public int getColumnNumber() { return 1; }
        @Override public void setLenient(boolean lenient) {}
        @Override public boolean isLenient() { return false; }
        @Override public void close() {}
    }

    static class MockJsonWriter implements JsonWriter {
        @Override public JsonWriter beginObject() { return this; }
        @Override public JsonWriter endObject() { return this; }
        @Override public JsonWriter beginArray() { return this; }
        @Override public JsonWriter endArray() { return this; }
        @Override public JsonWriter name(String name) { return this; }
        @Override public JsonWriter value(String value) { return this; }
        @Override public JsonWriter value(boolean value) { return this; }
        @Override public JsonWriter value(int value) { return this; }
        @Override public JsonWriter value(long value) { return this; }
        @Override public JsonWriter value(double value) { return this; }
        @Override public JsonWriter value(Number value) { return this; }
        @Override public JsonWriter nullValue() { return this; }
        @Override public JsonWriter jsonValue(String json) { return this; }
        @Override public JsonWriter setIndent(String indent) { return this; }
        @Override public JsonWriter setSerializeNulls(boolean serializeNulls) { return this; }
        @Override public JsonWriter setLenient(boolean lenient) { return this; }
        @Override public boolean isLenient() { return false; }
        @Override public JsonWriter setHtmlSafe(boolean htmlSafe) { return this; }
        @Override public boolean isHtmlSafe() { return false; }
        @Override public void flush() {}
        @Override public void close() {}
    }
}
