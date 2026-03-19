package cloud.opencode.base.serialization.json;

import cloud.opencode.base.serialization.TypeReference;
import cloud.opencode.base.serialization.exception.OpenSerializationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIf;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@DisplayName("JsonSerializer Tests")
class JsonSerializerTest {

    private JsonSerializer serializer;

    /**
     * Check if a JSON provider is available for OpenJson.
     * Tests requiring actual JSON operations will be skipped if not available.
      *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
    static boolean isJsonProviderAvailable() {
        try {
            Class.forName("cloud.opencode.base.json.OpenJson");
            // Try to trigger initialization
            cloud.opencode.base.json.OpenJson.toJson("");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @BeforeEach
    void setUp() {
        serializer = new JsonSerializer();
    }

    @Nested
    @DisplayName("Format Tests")
    class FormatTests {

        @Test
        @DisplayName("FORMAT constant should be 'json'")
        void formatConstantShouldBeJson() {
            assertThat(JsonSerializer.FORMAT).isEqualTo("json");
        }

        @Test
        @DisplayName("getFormat should return 'json'")
        void getFormatShouldReturnJson() {
            assertThat(serializer.getFormat()).isEqualTo("json");
        }
    }

    @Nested
    @DisplayName("getMimeType Tests")
    class GetMimeTypeTests {

        @Test
        @DisplayName("getMimeType should return application/json")
        void getMimeTypeShouldReturnApplicationJson() {
            assertThat(serializer.getMimeType()).isEqualTo("application/json");
        }
    }

    @Nested
    @DisplayName("isTextBased Tests")
    class IsTextBasedTests {

        @Test
        @DisplayName("isTextBased should return true")
        void isTextBasedShouldReturnTrue() {
            assertThat(serializer.isTextBased()).isTrue();
        }
    }

    @Nested
    @DisplayName("Serialize Tests (requires JSON provider)")
    class SerializeTests {

        @Test
        @DisplayName("Should serialize null to 'null'")
        void shouldSerializeNullToNullString() {
            assumeTrue(isJsonProviderAvailable(), "JSON provider not available");

            byte[] result = serializer.serialize(null);

            String json = new String(result, StandardCharsets.UTF_8);
            assertThat(json).isEqualTo("null");
        }

        @Test
        @DisplayName("Should serialize String")
        void shouldSerializeString() {
            assumeTrue(isJsonProviderAvailable(), "JSON provider not available");

            byte[] result = serializer.serialize("Hello");

            String json = new String(result, StandardCharsets.UTF_8);
            assertThat(json).isEqualTo("\"Hello\"");
        }

        @Test
        @DisplayName("Should serialize Integer")
        void shouldSerializeInteger() {
            assumeTrue(isJsonProviderAvailable(), "JSON provider not available");

            byte[] result = serializer.serialize(42);

            String json = new String(result, StandardCharsets.UTF_8);
            assertThat(json).isEqualTo("42");
        }

        @Test
        @DisplayName("Should serialize List")
        void shouldSerializeList() {
            assumeTrue(isJsonProviderAvailable(), "JSON provider not available");

            List<String> list = List.of("a", "b", "c");

            byte[] result = serializer.serialize(list);

            String json = new String(result, StandardCharsets.UTF_8);
            assertThat(json).isEqualTo("[\"a\",\"b\",\"c\"]");
        }
    }

    @Nested
    @DisplayName("Deserialize with Class Tests (requires JSON provider)")
    class DeserializeWithClassTests {

        @Test
        @DisplayName("Should deserialize null data to null")
        void shouldDeserializeNullDataToNull() {
            String result = serializer.deserialize(null, String.class);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should deserialize empty array to null")
        void shouldDeserializeEmptyArrayToNull() {
            String result = serializer.deserialize(new byte[0], String.class);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should deserialize String")
        void shouldDeserializeString() {
            assumeTrue(isJsonProviderAvailable(), "JSON provider not available");

            byte[] data = "\"Hello\"".getBytes(StandardCharsets.UTF_8);

            String result = serializer.deserialize(data, String.class);

            assertThat(result).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Should deserialize Integer")
        void shouldDeserializeInteger() {
            assumeTrue(isJsonProviderAvailable(), "JSON provider not available");

            byte[] data = "42".getBytes(StandardCharsets.UTF_8);

            Integer result = serializer.deserialize(data, Integer.class);

            assertThat(result).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("Deserialize with TypeReference Tests (requires JSON provider)")
    class DeserializeWithTypeReferenceTests {

        @Test
        @DisplayName("Should return null for null data with TypeReference")
        void shouldReturnNullForNullDataWithTypeReference() {
            TypeReference<List<String>> typeRef = new TypeReference<>() {};

            List<String> result = serializer.deserialize(null, typeRef);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null for empty data with TypeReference")
        void shouldReturnNullForEmptyDataWithTypeReference() {
            TypeReference<List<String>> typeRef = new TypeReference<>() {};

            List<String> result = serializer.deserialize(new byte[0], typeRef);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should deserialize List with TypeReference")
        void shouldDeserializeListWithTypeReference() {
            assumeTrue(isJsonProviderAvailable(), "JSON provider not available");

            byte[] data = "[\"a\",\"b\",\"c\"]".getBytes(StandardCharsets.UTF_8);
            TypeReference<List<String>> typeRef = new TypeReference<>() {};

            List<String> result = serializer.deserialize(data, typeRef);

            assertThat(result).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("Deserialize with Type Tests (requires JSON provider)")
    class DeserializeWithTypeTests {

        @Test
        @DisplayName("Should return null for null data with Type")
        void shouldReturnNullForNullDataWithType() {
            Type type = String.class;

            String result = serializer.deserialize(null, type);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null for empty data with Type")
        void shouldReturnNullForEmptyDataWithType() {
            Type type = String.class;

            String result = serializer.deserialize(new byte[0], type);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should deserialize with Class Type")
        void shouldDeserializeWithClassType() {
            assumeTrue(isJsonProviderAvailable(), "JSON provider not available");

            byte[] data = "\"test\"".getBytes(StandardCharsets.UTF_8);
            Type type = String.class;

            String result = serializer.deserialize(data, type);

            assertThat(result).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("Round-trip Tests (requires JSON provider)")
    class RoundTripTests {

        @Test
        @DisplayName("Should round-trip String")
        void shouldRoundTripString() {
            assumeTrue(isJsonProviderAvailable(), "JSON provider not available");

            String original = "Hello, 世界!";
            byte[] data = serializer.serialize(original);
            String result = serializer.deserialize(data, String.class);

            assertThat(result).isEqualTo(original);
        }

        @Test
        @DisplayName("Should round-trip Integer")
        void shouldRoundTripInteger() {
            assumeTrue(isJsonProviderAvailable(), "JSON provider not available");

            Integer original = 42;
            byte[] data = serializer.serialize(original);
            Integer result = serializer.deserialize(data, Integer.class);

            assertThat(result).isEqualTo(original);
        }

        @Test
        @DisplayName("Should round-trip List")
        void shouldRoundTripList() {
            assumeTrue(isJsonProviderAvailable(), "JSON provider not available");

            List<String> original = List.of("a", "b", "c");
            byte[] data = serializer.serialize(original);
            TypeReference<List<String>> typeRef = new TypeReference<>() {};
            List<String> result = serializer.deserialize(data, typeRef);

            assertThat(result).containsExactlyElementsOf(original);
        }
    }

    // Test helper class
    record TestUser(String name, int age) {}
}
