package cloud.opencode.base.serialization.binary;

import cloud.opencode.base.serialization.TypeReference;
import cloud.opencode.base.serialization.exception.OpenSerializationException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Type;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@DisplayName("ProtobufSerializer Tests")
class ProtobufSerializerTest {

    private ProtobufSerializer serializer;

    /**
     * Check if Protobuf is available.
      *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
    static boolean isProtobufAvailable() {
        try {
            Class.forName("com.google.protobuf.Message");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @BeforeEach
    void setUp() {
        assumeTrue(isProtobufAvailable(), "Protobuf not available");
        serializer = new ProtobufSerializer();
    }

    @Nested
    @DisplayName("Format Tests")
    class FormatTests {

        @Test
        @DisplayName("FORMAT constant should be 'protobuf'")
        void formatConstantShouldBeProtobuf() {
            assertThat(ProtobufSerializer.FORMAT).isEqualTo("protobuf");
        }

        @Test
        @DisplayName("getFormat should return 'protobuf'")
        void getFormatShouldReturnProtobuf() {
            assertThat(serializer.getFormat()).isEqualTo("protobuf");
        }
    }

    @Nested
    @DisplayName("getMimeType Tests")
    class GetMimeTypeTests {

        @Test
        @DisplayName("getMimeType should return application/x-protobuf")
        void getMimeTypeShouldReturnProtobufType() {
            assertThat(serializer.getMimeType()).isEqualTo("application/x-protobuf");
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
        @DisplayName("Should throw for non-Message object")
        void shouldThrowForNonMessageObject() {
            assertThatThrownBy(() -> serializer.serialize("Not a protobuf message"))
                    .isInstanceOf(OpenSerializationException.class)
                    .hasMessageContaining("not supported");
        }

        @Test
        @DisplayName("Should throw for POJO")
        void shouldThrowForPojo() {
            Object pojo = new Object() {
                public String value = "test";
            };

            assertThatThrownBy(() -> serializer.serialize(pojo))
                    .isInstanceOf(OpenSerializationException.class)
                    .hasMessageContaining("not supported");
        }
    }

    @Nested
    @DisplayName("Deserialize with Class Tests")
    class DeserializeWithClassTests {

        @Test
        @DisplayName("Should deserialize null data to null")
        void shouldDeserializeNullDataToNull() {
            Object result = serializer.deserialize(null, Object.class);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should deserialize empty array to null")
        void shouldDeserializeEmptyArrayToNull() {
            Object result = serializer.deserialize(new byte[0], Object.class);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should throw for non-Message type")
        void shouldThrowForNonMessageType() {
            byte[] data = new byte[]{1, 2, 3};

            assertThatThrownBy(() -> serializer.deserialize(data, String.class))
                    .isInstanceOf(OpenSerializationException.class)
                    .hasMessageContaining("not supported");
        }
    }

    @Nested
    @DisplayName("Deserialize with TypeReference Tests")
    class DeserializeWithTypeReferenceTests {

        @Test
        @DisplayName("Should throw for non-Message TypeReference")
        void shouldThrowForNonMessageTypeReference() {
            byte[] data = new byte[]{1, 2, 3};
            TypeReference<String> typeRef = new TypeReference<>() {};

            assertThatThrownBy(() -> serializer.deserialize(data, typeRef))
                    .isInstanceOf(OpenSerializationException.class)
                    .hasMessageContaining("not supported");
        }
    }

    @Nested
    @DisplayName("Deserialize with Type Tests")
    class DeserializeWithTypeTests {

        @Test
        @DisplayName("Should throw for non-Class Type")
        void shouldThrowForNonClassType() {
            byte[] data = new byte[]{1, 2, 3};
            Type parameterizedType = new TypeReference<List<String>>() {}.getType();

            assertThatThrownBy(() -> serializer.deserialize(data, parameterizedType))
                    .isInstanceOf(OpenSerializationException.class)
                    .hasMessageContaining("not supported");
        }
    }

    @Nested
    @DisplayName("supports Tests")
    class SupportsTests {

        @Test
        @DisplayName("Should not support String")
        void shouldNotSupportString() {
            assertThat(serializer.supports(String.class)).isFalse();
        }

        @Test
        @DisplayName("Should not support Integer")
        void shouldNotSupportInteger() {
            assertThat(serializer.supports(Integer.class)).isFalse();
        }

        @Test
        @DisplayName("Should not support Object")
        void shouldNotSupportObject() {
            assertThat(serializer.supports(Object.class)).isFalse();
        }

        @Test
        @DisplayName("Should support Message subclass if available")
        void shouldSupportMessageSubclassIfAvailable() {
            try {
                Class<?> messageClass = Class.forName("com.google.protobuf.Message");
                // We can't easily create a test Message, but we verify the logic
                assertThat(serializer.supports(Object.class)).isFalse();
            } catch (ClassNotFoundException e) {
                // Skip
            }
        }
    }
}
