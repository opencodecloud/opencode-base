package cloud.opencode.base.serialization.xml;

import cloud.opencode.base.serialization.TypeReference;
import cloud.opencode.base.serialization.exception.OpenSerializationException;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.junit.jupiter.api.*;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@DisplayName("XmlSerializer Tests")
class XmlSerializerTest {

    private XmlSerializer serializer;

    /**
     * Check if JAXB is available.
      *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
    static boolean isJaxbAvailable() {
        try {
            Class.forName("jakarta.xml.bind.JAXBContext");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @BeforeEach
    void setUp() {
        assumeTrue(isJaxbAvailable(), "JAXB not available");
        serializer = new XmlSerializer();
    }

    @Nested
    @DisplayName("Format Tests")
    class FormatTests {

        @Test
        @DisplayName("FORMAT constant should be 'xml'")
        void formatConstantShouldBeXml() {
            assertThat(XmlSerializer.FORMAT).isEqualTo("xml");
        }

        @Test
        @DisplayName("getFormat should return 'xml'")
        void getFormatShouldReturnXml() {
            assertThat(serializer.getFormat()).isEqualTo("xml");
        }
    }

    @Nested
    @DisplayName("getMimeType Tests")
    class GetMimeTypeTests {

        @Test
        @DisplayName("getMimeType should return application/xml")
        void getMimeTypeShouldReturnApplicationXml() {
            assertThat(serializer.getMimeType()).isEqualTo("application/xml");
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
    @DisplayName("Serialize Tests")
    class SerializeTests {

        @Test
        @DisplayName("Should serialize null to empty array")
        void shouldSerializeNullToEmptyArray() {
            byte[] result = serializer.serialize(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should serialize JAXB annotated object")
        void shouldSerializeJaxbAnnotatedObject() {
            TestUser user = new TestUser();
            user.setName("John");
            user.setAge(30);

            byte[] result = serializer.serialize(user);

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
            String xml = new String(result, StandardCharsets.UTF_8);
            assertThat(xml).contains("John");
            assertThat(xml).contains("30");
        }

        @Test
        @DisplayName("Should throw for non-JAXB object")
        void shouldThrowForNonJaxbObject() {
            // Use a regular class without JAXB annotations
            class NonJaxbClass {
                public String value = "test";
            }
            NonJaxbClass nonJaxb = new NonJaxbClass();

            // JAXB may throw RuntimeException or OpenSerializationException depending on the error
            assertThatThrownBy(() -> serializer.serialize(nonJaxb))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Deserialize with Class Tests")
    class DeserializeWithClassTests {

        @Test
        @DisplayName("Should deserialize null data to null")
        void shouldDeserializeNullDataToNull() {
            TestUser result = serializer.deserialize((byte[]) null, TestUser.class);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should deserialize empty array to null")
        void shouldDeserializeEmptyArrayToNull() {
            TestUser result = serializer.deserialize(new byte[0], TestUser.class);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should deserialize JAXB annotated object")
        void shouldDeserializeJaxbAnnotatedObject() {
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><testUser><name>Jane</name><age>25</age></testUser>";
            byte[] data = xml.getBytes(StandardCharsets.UTF_8);

            TestUser result = serializer.deserialize(data, TestUser.class);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Jane");
            assertThat(result.getAge()).isEqualTo(25);
        }

        @Test
        @DisplayName("Should throw for invalid XML")
        void shouldThrowForInvalidXml() {
            byte[] invalidData = "not valid xml".getBytes(StandardCharsets.UTF_8);

            assertThatThrownBy(() -> serializer.deserialize(invalidData, TestUser.class))
                    .isInstanceOf(OpenSerializationException.class);
        }
    }

    @Nested
    @DisplayName("Deserialize with TypeReference Tests")
    class DeserializeWithTypeReferenceTests {

        @Test
        @DisplayName("Should deserialize with TypeReference")
        void shouldDeserializeWithTypeReference() {
            TestUser user = new TestUser();
            user.setName("Bob");
            user.setAge(35);
            byte[] data = serializer.serialize(user);

            TypeReference<TestUser> typeRef = new TypeReference<>() {};
            TestUser result = serializer.deserialize(data, (TypeReference<TestUser>) typeRef);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Bob");
        }
    }

    @Nested
    @DisplayName("Deserialize with Type Tests")
    class DeserializeWithTypeTests {

        @Test
        @DisplayName("Should deserialize with Class Type")
        void shouldDeserializeWithClassType() {
            TestUser user = new TestUser();
            user.setName("Alice");
            user.setAge(28);
            byte[] data = serializer.serialize(user);

            Type type = TestUser.class;
            TestUser result = serializer.deserialize(data, type);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("Should throw for non-Class Type")
        void shouldThrowForNonClassType() {
            TestUser user = new TestUser();
            user.setName("Test");
            user.setAge(1);
            byte[] data = serializer.serialize(user);

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
        @DisplayName("Should round-trip JAXB object")
        void shouldRoundTripJaxbObject() {
            TestUser original = new TestUser();
            original.setName("Charlie");
            original.setAge(40);

            byte[] data = serializer.serialize(original);
            TestUser result = serializer.deserialize(data, TestUser.class);

            assertThat(result.getName()).isEqualTo(original.getName());
            assertThat(result.getAge()).isEqualTo(original.getAge());
        }
    }

    @Nested
    @DisplayName("String Convenience Methods Tests")
    class StringConvenienceMethodsTests {

        @Test
        @DisplayName("serializeToString should return XML string")
        void serializeToStringShouldReturnXmlString() {
            TestUser user = new TestUser();
            user.setName("Dan");
            user.setAge(45);

            String result = serializer.serializeToString(user);

            assertThat(result).contains("Dan");
            assertThat(result).contains("45");
        }

        @Test
        @DisplayName("deserialize from string should work")
        void deserializeFromStringShouldWork() {
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><testUser><name>Eve</name><age>50</age></testUser>";

            TestUser result = serializer.deserialize(xml, TestUser.class);

            assertThat(result.getName()).isEqualTo("Eve");
            assertThat(result.getAge()).isEqualTo(50);
        }
    }

    // Test helper class with JAXB annotations
    @XmlRootElement
    public static class TestUser {
        private String name;
        private int age;

        public TestUser() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }
}
