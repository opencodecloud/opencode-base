package cloud.opencode.base.serialization.binary;

import cloud.opencode.base.serialization.TypeReference;
import cloud.opencode.base.serialization.exception.OpenSerializationException;
import org.junit.jupiter.api.*;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * JdkSerializerTest Tests
 * JdkSerializerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
@DisplayName("JdkSerializer Tests")
class JdkSerializerTest {

    private JdkSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new JdkSerializer();
    }

    @Nested
    @DisplayName("Format Tests")
    class FormatTests {

        @Test
        @DisplayName("FORMAT constant should be 'jdk'")
        void formatConstantShouldBeJdk() {
            assertThat(JdkSerializer.FORMAT).isEqualTo("jdk");
        }

        @Test
        @DisplayName("getFormat should return 'jdk'")
        void getFormatShouldReturnJdk() {
            assertThat(serializer.getFormat()).isEqualTo("jdk");
        }
    }

    @Nested
    @DisplayName("getMimeType Tests")
    class GetMimeTypeTests {

        @Test
        @DisplayName("getMimeType should return java serialized object type")
        void getMimeTypeShouldReturnJavaSerializedObjectType() {
            assertThat(serializer.getMimeType()).isEqualTo("application/x-java-serialized-object");
        }
    }

    @Nested
    @DisplayName("supports Tests")
    class SupportsTests {

        @Test
        @DisplayName("Should support Serializable types")
        void shouldSupportSerializableTypes() {
            assertThat(serializer.supports(String.class)).isTrue();
            assertThat(serializer.supports(Integer.class)).isTrue();
            assertThat(serializer.supports(ArrayList.class)).isTrue();
            assertThat(serializer.supports(HashMap.class)).isTrue();
        }

        @Test
        @DisplayName("Should not support non-Serializable types")
        void shouldNotSupportNonSerializableTypes() {
            assertThat(serializer.supports(Thread.class)).isFalse();
        }

        @Test
        @DisplayName("Should support custom Serializable class")
        void shouldSupportCustomSerializableClass() {
            assertThat(serializer.supports(TestPerson.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Serialize Tests")
    class SerializeTests {

        @Test
        @DisplayName("Should serialize String")
        void shouldSerializeString() {
            byte[] result = serializer.serialize("Hello World");

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should serialize null to empty array")
        void shouldSerializeNullToEmptyArray() {
            byte[] result = serializer.serialize(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should serialize List")
        void shouldSerializeList() {
            List<String> list = List.of("a", "b", "c");

            byte[] result = serializer.serialize(new ArrayList<>(list));

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should serialize Map")
        void shouldSerializeMap() {
            Map<String, Integer> map = new HashMap<>();
            map.put("one", 1);
            map.put("two", 2);

            byte[] result = serializer.serialize(map);

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should serialize custom Serializable object")
        void shouldSerializeCustomSerializableObject() {
            TestPerson person = new TestPerson("John", 30);

            byte[] result = serializer.serialize(person);

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should throw for non-serializable object")
        void shouldThrowForNonSerializableObject() {
            Object notSerializable = new Object();

            assertThatThrownBy(() -> serializer.serialize(notSerializable))
                    .isInstanceOf(OpenSerializationException.class)
                    .hasMessageContaining("serialize");
        }
    }

    @Nested
    @DisplayName("Deserialize with Class Tests")
    class DeserializeWithClassTests {

        @Test
        @DisplayName("Should deserialize String")
        void shouldDeserializeString() {
            String original = "Hello World";
            byte[] data = serializer.serialize(original);

            String result = serializer.deserialize(data, String.class);

            assertThat(result).isEqualTo(original);
        }

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
        @DisplayName("Should deserialize custom object")
        void shouldDeserializeCustomObject() {
            TestPerson original = new TestPerson("Jane", 25);
            byte[] data = serializer.serialize(original);

            TestPerson result = serializer.deserialize(data, TestPerson.class);

            assertThat(result.name()).isEqualTo("Jane");
            assertThat(result.age()).isEqualTo(25);
        }

        @Test
        @DisplayName("Should throw for invalid data")
        void shouldThrowForInvalidData() {
            byte[] invalidData = "not valid serialized data".getBytes();

            assertThatThrownBy(() -> serializer.deserialize(invalidData, String.class))
                    .isInstanceOf(OpenSerializationException.class)
                    .hasMessageContaining("deserialize");
        }
    }

    @Nested
    @DisplayName("Deserialize with TypeReference Tests")
    class DeserializeWithTypeReferenceTests {

        @Test
        @DisplayName("Should deserialize with TypeReference")
        void shouldDeserializeWithTypeReference() {
            ArrayList<String> original = new ArrayList<>(List.of("a", "b", "c"));
            byte[] data = serializer.serialize(original);

            TypeReference<ArrayList<String>> typeRef = new TypeReference<>() {};
            ArrayList<String> result = serializer.deserialize(data, typeRef);

            assertThat(result).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("Deserialize with Type Tests")
    class DeserializeWithTypeTests {

        @Test
        @DisplayName("Should deserialize with Class Type")
        void shouldDeserializeWithClassType() {
            String original = "test";
            byte[] data = serializer.serialize(original);

            Type type = String.class;
            String result = serializer.deserialize(data, type);

            assertThat(result).isEqualTo(original);
        }

        @Test
        @DisplayName("Should throw for non-Class Type")
        void shouldThrowForNonClassType() {
            ArrayList<String> original = new ArrayList<>(List.of("a"));
            byte[] data = serializer.serialize(original);

            Type parameterizedType = new TypeReference<List<String>>() {}.getType();

            assertThatThrownBy(() -> serializer.deserialize(data, parameterizedType))
                    .isInstanceOf(OpenSerializationException.class)
                    .hasMessageContaining("not supported");
        }
    }

    @Nested
    @DisplayName("Round-trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("Should round-trip String")
        void shouldRoundTripString() {
            String original = "Hello, 世界!";
            byte[] data = serializer.serialize(original);
            String result = serializer.deserialize(data, String.class);

            assertThat(result).isEqualTo(original);
        }

        @Test
        @DisplayName("Should round-trip Integer")
        void shouldRoundTripInteger() {
            Integer original = 42;
            byte[] data = serializer.serialize(original);
            Integer result = serializer.deserialize(data, Integer.class);

            assertThat(result).isEqualTo(original);
        }

        @Test
        @DisplayName("Should round-trip List")
        @SuppressWarnings("unchecked")
        void shouldRoundTripList() {
            ArrayList<Integer> original = new ArrayList<>(List.of(1, 2, 3, 4, 5));
            byte[] data = serializer.serialize(original);
            ArrayList<Integer> result = (ArrayList<Integer>) serializer.deserialize(data, ArrayList.class);

            assertThat(result).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("Should round-trip Map")
        @SuppressWarnings("unchecked")
        void shouldRoundTripMap() {
            HashMap<String, String> original = new HashMap<>();
            original.put("key1", "value1");
            original.put("key2", "value2");

            byte[] data = serializer.serialize(original);
            HashMap<String, String> result = (HashMap<String, String>) serializer.deserialize(data, HashMap.class);

            assertThat(result).containsEntry("key1", "value1");
            assertThat(result).containsEntry("key2", "value2");
        }

        @Test
        @DisplayName("Should round-trip custom object")
        void shouldRoundTripCustomObject() {
            TestPerson original = new TestPerson("Alice", 28);
            byte[] data = serializer.serialize(original);
            TestPerson result = serializer.deserialize(data, TestPerson.class);

            assertThat(result).isEqualTo(original);
        }
    }

    // Test helper class
    record TestPerson(String name, int age) implements Serializable {
        @java.io.Serial
        private static final long serialVersionUID = 1L;
    }
}
