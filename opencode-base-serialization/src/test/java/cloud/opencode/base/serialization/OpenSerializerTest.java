package cloud.opencode.base.serialization;

import cloud.opencode.base.serialization.binary.JdkSerializer;
import cloud.opencode.base.serialization.exception.OpenSerializationException;
import org.junit.jupiter.api.*;

import java.io.Serializable;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenSerializerTest Tests
 * OpenSerializerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
@DisplayName("OpenSerializer Tests")
class OpenSerializerTest {

    private static Serializer originalDefault;
    private static JdkSerializer jdkSerializer;

    @BeforeAll
    static void setUpClass() {
        // Save original default and set JDK serializer as default for tests
        originalDefault = OpenSerializer.getDefault();
        jdkSerializer = new JdkSerializer();
        OpenSerializer.register(jdkSerializer);
        OpenSerializer.setDefault(jdkSerializer);
    }

    @AfterAll
    static void tearDownClass() {
        // Restore original default
        if (originalDefault != null) {
            OpenSerializer.setDefault(originalDefault);
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("getConfig should return default config initially")
        void getConfigShouldReturnDefaultConfig() {
            SerializerConfig config = OpenSerializer.getConfig();

            assertThat(config).isNotNull();
        }

        @Test
        @DisplayName("setConfig should set global config")
        void setConfigShouldSetGlobalConfig() {
            SerializerConfig originalConfig = OpenSerializer.getConfig();
            SerializerConfig newConfig = SerializerConfig.builder()
                    .enableCompression(true)
                    .build();

            try {
                OpenSerializer.setConfig(newConfig);
                assertThat(OpenSerializer.getConfig()).isSameAs(newConfig);
            } finally {
                OpenSerializer.setConfig(originalConfig);
            }
        }

        @Test
        @DisplayName("setConfig should reject null")
        void setConfigShouldRejectNull() {
            assertThatThrownBy(() -> OpenSerializer.setConfig(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Config");
        }
    }

    @Nested
    @DisplayName("Serializer Registration Tests")
    class SerializerRegistrationTests {

        @Test
        @DisplayName("register should add serializer")
        void registerShouldAddSerializer() {
            TestSerializer testSerializer = new TestSerializer("custom");

            OpenSerializer.register(testSerializer);

            assertThat(OpenSerializer.hasFormat("custom")).isTrue();
            assertThat(OpenSerializer.get("custom")).isSameAs(testSerializer);
        }

        @Test
        @DisplayName("register should reject null")
        void registerShouldRejectNull() {
            assertThatThrownBy(() -> OpenSerializer.register(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Serializer");
        }

        @Test
        @DisplayName("setDefault with format should change default serializer")
        void setDefaultWithFormatShouldChangeDefaultSerializer() {
            Serializer currentDefault = OpenSerializer.getDefault();

            try {
                // Register a test serializer
                TestSerializer testSerializer = new TestSerializer("test-default");
                OpenSerializer.register(testSerializer);

                OpenSerializer.setDefault("test-default");

                assertThat(OpenSerializer.getDefault()).isSameAs(testSerializer);
            } finally {
                OpenSerializer.setDefault(currentDefault);
            }
        }

        @Test
        @DisplayName("setDefault with serializer should change default")
        void setDefaultWithSerializerShouldChangeDefault() {
            Serializer currentDefault = OpenSerializer.getDefault();
            TestSerializer testSerializer = new TestSerializer("direct-default");

            try {
                OpenSerializer.setDefault(testSerializer);

                assertThat(OpenSerializer.getDefault()).isSameAs(testSerializer);
            } finally {
                OpenSerializer.setDefault(currentDefault);
            }
        }

        @Test
        @DisplayName("setDefault with serializer should reject null")
        void setDefaultWithSerializerShouldRejectNull() {
            assertThatThrownBy(() -> OpenSerializer.setDefault((Serializer) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Serializer");
        }
    }

    @Nested
    @DisplayName("get Tests")
    class GetTests {

        @Test
        @DisplayName("get should return serializer for known format")
        void getShouldReturnSerializerForKnownFormat() {
            // JDK serializer should be available
            Serializer serializer = OpenSerializer.get("jdk");

            assertThat(serializer).isNotNull();
            assertThat(serializer.getFormat()).isEqualTo("jdk");
        }

        @Test
        @DisplayName("get should throw for unknown format")
        void getShouldThrowForUnknownFormat() {
            assertThatThrownBy(() -> OpenSerializer.get("unknown-format"))
                    .isInstanceOf(OpenSerializationException.class)
                    .hasMessageContaining("No serializer found");
        }
    }

    @Nested
    @DisplayName("getDefault Tests")
    class GetDefaultTests {

        @Test
        @DisplayName("getDefault should return a serializer")
        void getDefaultShouldReturnASerializer() {
            Serializer serializer = OpenSerializer.getDefault();

            assertThat(serializer).isNotNull();
        }
    }

    @Nested
    @DisplayName("getFormats Tests")
    class GetFormatsTests {

        @Test
        @DisplayName("getFormats should return available formats")
        void getFormatsShouldReturnAvailableFormats() {
            Set<String> formats = OpenSerializer.getFormats();

            assertThat(formats).isNotEmpty();
            // JDK should always be available
            assertThat(formats).contains("jdk");
        }

        @Test
        @DisplayName("getFormats should return immutable set")
        void getFormatsShouldReturnImmutableSet() {
            Set<String> formats = OpenSerializer.getFormats();

            assertThatThrownBy(() -> formats.add("new-format"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("hasFormat Tests")
    class HasFormatTests {

        @Test
        @DisplayName("hasFormat should return true for registered format")
        void hasFormatShouldReturnTrueForRegisteredFormat() {
            assertThat(OpenSerializer.hasFormat("jdk")).isTrue();
        }

        @Test
        @DisplayName("hasFormat should return false for unknown format")
        void hasFormatShouldReturnFalseForUnknownFormat() {
            assertThat(OpenSerializer.hasFormat("nonexistent-format")).isFalse();
        }
    }

    @Nested
    @DisplayName("Serialize Tests")
    class SerializeTests {

        @Test
        @DisplayName("serialize should serialize object with default serializer")
        void serializeShouldSerializeObjectWithDefaultSerializer() {
            byte[] result = OpenSerializer.serialize("Hello");

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("serialize with format should use specified serializer")
        void serializeWithFormatShouldUseSpecifiedSerializer() {
            byte[] result = OpenSerializer.serialize("Hello", "jdk");

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("serializeToString should return string")
        void serializeToStringShouldReturnString() {
            String result = OpenSerializer.serializeToString("Hello");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("serializeToString with format should use specified serializer")
        void serializeToStringWithFormatShouldUseSpecifiedSerializer() {
            String result = OpenSerializer.serializeToString("Hello", "jdk");

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Deserialize with Class Tests")
    class DeserializeWithClassTests {

        @Test
        @DisplayName("deserialize should deserialize with default serializer")
        void deserializeShouldDeserializeWithDefaultSerializer() {
            byte[] data = OpenSerializer.serialize("Hello");

            String result = OpenSerializer.deserialize(data, String.class);

            assertThat(result).isEqualTo("Hello");
        }

        @Test
        @DisplayName("deserialize with format should use specified serializer")
        void deserializeWithFormatShouldUseSpecifiedSerializer() {
            byte[] data = OpenSerializer.serialize("Hello", "jdk");

            String result = OpenSerializer.deserialize(data, String.class, "jdk");

            assertThat(result).isEqualTo("Hello");
        }

        // Note: String-based serialize/deserialize doesn't work with binary serializers like JDK
        // because the binary data cannot be safely converted to/from UTF-8 strings.
    }

    @Nested
    @DisplayName("Deserialize with TypeReference Tests")
    class DeserializeWithTypeReferenceTests {

        @Test
        @DisplayName("deserialize with TypeReference should work")
        void deserializeWithTypeReferenceShouldWork() {
            ArrayList<String> original = new ArrayList<>(List.of("a", "b", "c"));
            byte[] data = OpenSerializer.serialize(original, "jdk");
            TypeReference<ArrayList<String>> typeRef = new TypeReference<>() {};

            ArrayList<String> result = OpenSerializer.deserialize(data, typeRef);

            assertThat(result).containsExactly("a", "b", "c");
        }

        // Note: String-based deserialize doesn't work with binary serializers like JDK

        @Test
        @DisplayName("deserialize with TypeReference and format should work")
        void deserializeWithTypeReferenceAndFormatShouldWork() {
            ArrayList<Integer> original = new ArrayList<>(List.of(1, 2, 3));
            byte[] data = OpenSerializer.serialize(original, "jdk");
            TypeReference<ArrayList<Integer>> typeRef = new TypeReference<>() {};

            ArrayList<Integer> result = OpenSerializer.deserialize(data, typeRef, "jdk");

            assertThat(result).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("Convenience Deserialization Tests")
    class ConvenienceDeserializationTests {

        @Test
        @DisplayName("deserializeList should deserialize to List")
        void deserializeListShouldDeserializeToList() {
            ArrayList<String> original = new ArrayList<>(List.of("a", "b", "c"));
            byte[] data = OpenSerializer.serialize(original, "jdk");

            List<String> result = OpenSerializer.deserializeList(data, String.class);

            assertThat(result).containsExactly("a", "b", "c");
        }

        // Note: String-based deserializeList doesn't work with binary serializers like JDK

        @Test
        @DisplayName("deserializeSet should deserialize to Set")
        void deserializeSetShouldDeserializeToSet() {
            HashSet<String> original = new HashSet<>(Set.of("a", "b", "c"));
            byte[] data = OpenSerializer.serialize(original, "jdk");

            Set<String> result = OpenSerializer.deserializeSet(data, String.class);

            assertThat(result).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("deserializeMap should deserialize to Map")
        void deserializeMapShouldDeserializeToMap() {
            HashMap<String, Integer> original = new HashMap<>();
            original.put("one", 1);
            original.put("two", 2);
            byte[] data = OpenSerializer.serialize(original, "jdk");

            Map<String, Integer> result = OpenSerializer.deserializeMap(data, String.class, Integer.class);

            assertThat(result).containsEntry("one", 1);
            assertThat(result).containsEntry("two", 2);
        }

        // Note: String-based deserializeMap doesn't work with binary serializers like JDK
    }

    @Nested
    @DisplayName("deepCopy Tests")
    class DeepCopyTests {

        @Test
        @DisplayName("deepCopy should create deep copy")
        void deepCopyShouldCreateDeepCopy() {
            TestData original = new TestData("Hello", 42);

            TestData copy = OpenSerializer.deepCopy(original);

            assertThat(copy).isNotSameAs(original);
            assertThat(copy.name()).isEqualTo(original.name());
            assertThat(copy.value()).isEqualTo(original.value());
        }

        @Test
        @DisplayName("deepCopy should return null for null input")
        void deepCopyShouldReturnNullForNullInput() {
            Object result = OpenSerializer.deepCopy(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("deepCopy with format should use specified serializer")
        void deepCopyWithFormatShouldUseSpecifiedSerializer() {
            TestData original = new TestData("Test", 100);

            TestData copy = OpenSerializer.deepCopy(original, "jdk");

            assertThat(copy).isNotSameAs(original);
            assertThat(copy).isEqualTo(original);
        }

        @Test
        @DisplayName("deepCopy with format should return null for null input")
        void deepCopyWithFormatShouldReturnNullForNullInput() {
            Object result = OpenSerializer.deepCopy(null, "jdk");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("isDeepCloneAvailable should return availability status")
        void isDeepCloneAvailableShouldReturnAvailabilityStatus() {
            // This test verifies the method exists and returns a boolean
            // The actual value depends on whether deepclone module is on classpath
            boolean available = OpenSerializer.isDeepCloneAvailable();

            // Just verify it returns a boolean without throwing
            assertThat(available).isIn(true, false);
        }

        @Test
        @DisplayName("deepCopy should handle complex nested objects")
        void deepCopyShouldHandleComplexNestedObjects() {
            record Inner(String data) implements java.io.Serializable {}
            record Outer(String name, Inner inner) implements java.io.Serializable {}

            Outer original = new Outer("test", new Inner("nested"));

            Outer copy = OpenSerializer.deepCopy(original);

            assertThat(copy).isNotSameAs(original);
            assertThat(copy.name()).isEqualTo("test");
            assertThat(copy.inner()).isNotSameAs(original.inner());
            assertThat(copy.inner().data()).isEqualTo("nested");
        }
    }

    @Nested
    @DisplayName("convert Tests")
    class ConvertTests {

        @Test
        @DisplayName("convert should return null for null input")
        void convertShouldReturnNullForNullInput() {
            Object result = OpenSerializer.convert(null, String.class);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("convert with TypeReference should return null for null input")
        void convertWithTypeReferenceShouldReturnNullForNullInput() {
            TypeReference<List<String>> typeRef = new TypeReference<>() {};

            Object result = OpenSerializer.convert(null, typeRef);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("convert with format should return null for null input")
        void convertWithFormatShouldReturnNullForNullInput() {
            Object result = OpenSerializer.convert(null, String.class, "jdk");

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Round-trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("Should round-trip complex object")
        void shouldRoundTripComplexObject() {
            TestData original = new TestData("Complex", 999);

            byte[] serialized = OpenSerializer.serialize(original);
            TestData deserialized = OpenSerializer.deserialize(serialized, TestData.class);

            assertThat(deserialized).isEqualTo(original);
        }

        @Test
        @DisplayName("Should round-trip with specific format")
        void shouldRoundTripWithSpecificFormat() {
            TestData original = new TestData("Formatted", 123);

            byte[] serialized = OpenSerializer.serialize(original, "jdk");
            TestData deserialized = OpenSerializer.deserialize(serialized, TestData.class, "jdk");

            assertThat(deserialized).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Class should be final")
        void classShouldBeFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(OpenSerializer.class.getModifiers())).isTrue();
        }
    }

    // Test helper classes
    record TestData(String name, int value) implements Serializable {
        @java.io.Serial
        private static final long serialVersionUID = 1L;
    }

    // Simple test serializer for registration tests
    static class TestSerializer implements Serializer {
        private final String format;

        TestSerializer(String format) {
            this.format = format;
        }

        @Override
        public byte[] serialize(Object obj) {
            return new byte[0];
        }

        @Override
        public <T> T deserialize(byte[] data, Class<T> type) {
            return null;
        }

        @Override
        public <T> T deserialize(byte[] data, TypeReference<T> typeRef) {
            return null;
        }

        @Override
        public <T> T deserialize(byte[] data, java.lang.reflect.Type type) {
            return null;
        }

        @Override
        public String getFormat() {
            return format;
        }
    }
}
