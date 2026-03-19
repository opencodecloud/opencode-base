package cloud.opencode.base.serialization.binary;

import cloud.opencode.base.serialization.TypeReference;
import cloud.opencode.base.serialization.exception.OpenSerializationException;
import org.junit.jupiter.api.*;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@DisplayName("KryoSerializer Tests")
class KryoSerializerTest {

    private KryoSerializer serializer;

    /**
     * Check if Kryo is available.
      *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
    static boolean isKryoAvailable() {
        try {
            Class.forName("com.esotericsoftware.kryo.Kryo");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @BeforeEach
    void setUp() {
        assumeTrue(isKryoAvailable(), "Kryo not available");
        serializer = new KryoSerializer();
    }

    @Nested
    @DisplayName("Format Tests")
    class FormatTests {

        @Test
        @DisplayName("FORMAT constant should be 'kryo'")
        void formatConstantShouldBeKryo() {
            assertThat(KryoSerializer.FORMAT).isEqualTo("kryo");
        }

        @Test
        @DisplayName("getFormat should return 'kryo'")
        void getFormatShouldReturnKryo() {
            assertThat(serializer.getFormat()).isEqualTo("kryo");
        }
    }

    @Nested
    @DisplayName("getMimeType Tests")
    class GetMimeTypeTests {

        @Test
        @DisplayName("getMimeType should return application/x-kryo")
        void getMimeTypeShouldReturnKryoType() {
            assertThat(serializer.getMimeType()).isEqualTo("application/x-kryo");
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should create serializer")
        void defaultConstructorShouldCreateSerializer() {
            KryoSerializer kryo = new KryoSerializer();
            assertThat(kryo).isNotNull();
            assertThat(kryo.getFormat()).isEqualTo("kryo");
        }

        @Test
        @DisplayName("Constructor with pool size should create serializer")
        void constructorWithPoolSizeShouldCreateSerializer() {
            KryoSerializer kryo = new KryoSerializer(32);
            assertThat(kryo).isNotNull();
            assertThat(kryo.getFormat()).isEqualTo("kryo");
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
        @DisplayName("Should serialize Integer")
        void shouldSerializeInteger() {
            byte[] result = serializer.serialize(42);

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should serialize List")
        void shouldSerializeList() {
            List<String> list = new ArrayList<>(List.of("a", "b", "c"));

            byte[] result = serializer.serialize(list);

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
        @DisplayName("Should serialize custom object")
        void shouldSerializeCustomObject() {
            TestPerson person = new TestPerson("John", 30);

            byte[] result = serializer.serialize(person);

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
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
        @DisplayName("Should deserialize Integer")
        void shouldDeserializeInteger() {
            Integer original = 42;
            byte[] data = serializer.serialize(original);

            Integer result = serializer.deserialize(data, Integer.class);

            assertThat(result).isEqualTo(original);
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
            byte[] invalidData = "not valid kryo data".getBytes();

            assertThatThrownBy(() -> serializer.deserialize(invalidData, String.class))
                    .isInstanceOf(OpenSerializationException.class);
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

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("register with classes should return this")
        void registerWithClassesShouldReturnThis() {
            KryoSerializer result = serializer.register(String.class, Integer.class);

            assertThat(result).isSameAs(serializer);
        }

        @Test
        @DisplayName("register with class and id should return this")
        void registerWithClassAndIdShouldReturnThis() {
            KryoSerializer result = serializer.register(TestPerson.class, 100);

            assertThat(result).isSameAs(serializer);
        }

        @Test
        @DisplayName("Should serialize registered class")
        void shouldSerializeRegisteredClass() {
            serializer.register(TestPerson.class);
            TestPerson original = new TestPerson("Bob", 35);

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
