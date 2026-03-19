package cloud.opencode.base.serialization.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenSerializationExceptionTest Tests
 * OpenSerializationExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
@DisplayName("OpenSerializationException Tests")
class OpenSerializationExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor with message should set message")
        void constructorWithMessageShouldSetMessage() {
            var ex = new OpenSerializationException("Test message");

            assertThat(ex.getMessage()).contains("Test message");
            assertThat(ex.getFormat()).isNull();
            assertThat(ex.getTargetType()).isNull();
        }

        @Test
        @DisplayName("Constructor with message and cause should set both")
        void constructorWithMessageAndCauseShouldSetBoth() {
            var cause = new RuntimeException("Cause");
            var ex = new OpenSerializationException("Test message", cause);

            assertThat(ex.getMessage()).contains("Test message");
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("Constructor with full details should set all fields")
        void constructorWithFullDetailsShouldSetAllFields() {
            var cause = new RuntimeException("Cause");
            var ex = new OpenSerializationException("Message", "json", String.class, cause);

            assertThat(ex.getMessage()).contains("Message");
            assertThat(ex.getFormat()).isEqualTo("json");
            assertThat(ex.getTargetType()).isEqualTo(String.class);
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("serializeFailed Factory Tests")
    class SerializeFailedTests {

        @Test
        @DisplayName("serializeFailed should create exception for object")
        void serializeFailedShouldCreateExceptionForObject() {
            var cause = new RuntimeException("IO error");
            var ex = OpenSerializationException.serializeFailed("test", cause);

            assertThat(ex.getMessage()).contains("serialize");
            assertThat(ex.getMessage()).contains("String");
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("serializeFailed should handle null object")
        void serializeFailedShouldHandleNullObject() {
            var ex = OpenSerializationException.serializeFailed(null, new RuntimeException());

            assertThat(ex.getMessage()).contains("null");
        }

        @Test
        @DisplayName("serializeFailed with format should include format")
        void serializeFailedWithFormatShouldIncludeFormat() {
            var ex = OpenSerializationException.serializeFailed("test", "json", new RuntimeException());

            assertThat(ex.getMessage()).contains("json");
            assertThat(ex.getFormat()).isEqualTo("json");
        }
    }

    @Nested
    @DisplayName("deserializeFailed Factory Tests")
    class DeserializeFailedTests {

        @Test
        @DisplayName("deserializeFailed should create exception with data size")
        void deserializeFailedShouldCreateExceptionWithDataSize() {
            byte[] data = new byte[100];
            var ex = OpenSerializationException.deserializeFailed(data, String.class, new RuntimeException());

            assertThat(ex.getMessage()).contains("deserialize");
            assertThat(ex.getMessage()).contains("String");
            assertThat(ex.getMessage()).contains("100 bytes");
            assertThat(ex.getTargetType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("deserializeFailed should handle null data")
        void deserializeFailedShouldHandleNullData() {
            var ex = OpenSerializationException.deserializeFailed(null, String.class, new RuntimeException());

            assertThat(ex.getMessage()).contains("0 bytes");
        }

        @Test
        @DisplayName("deserializeFailed with format should include format")
        void deserializeFailedWithFormatShouldIncludeFormat() {
            byte[] data = new byte[50];
            var ex = OpenSerializationException.deserializeFailed(data, String.class, "kryo", new RuntimeException());

            assertThat(ex.getMessage()).contains("kryo");
            assertThat(ex.getFormat()).isEqualTo("kryo");
        }
    }

    @Nested
    @DisplayName("serializerNotFound Factory Tests")
    class SerializerNotFoundTests {

        @Test
        @DisplayName("serializerNotFound should create exception with format")
        void serializerNotFoundShouldCreateExceptionWithFormat() {
            var ex = OpenSerializationException.serializerNotFound("msgpack");

            assertThat(ex.getMessage()).contains("msgpack");
            assertThat(ex.getMessage()).contains("No serializer found");
            assertThat(ex.getFormat()).isEqualTo("msgpack");
        }
    }

    @Nested
    @DisplayName("unsupportedType Factory Tests")
    class UnsupportedTypeTests {

        @Test
        @DisplayName("unsupportedType with Type should create exception")
        void unsupportedTypeWithTypeShouldCreateException() {
            var ex = OpenSerializationException.unsupportedType(String.class, "protobuf");

            assertThat(ex.getMessage()).contains("String");
            assertThat(ex.getMessage()).contains("protobuf");
            assertThat(ex.getMessage()).contains("not supported");
            assertThat(ex.getFormat()).isEqualTo("protobuf");
        }

        @Test
        @DisplayName("unsupportedType with Class should create exception")
        void unsupportedTypeWithClassShouldCreateException() {
            var ex = OpenSerializationException.unsupportedType(Integer.class, "xml");

            assertThat(ex.getMessage()).contains("Integer");
            assertThat(ex.getMessage()).contains("xml");
            assertThat(ex.getTargetType()).isEqualTo(Integer.class);
        }
    }

    @Nested
    @DisplayName("compressionFailed Factory Tests")
    class CompressionFailedTests {

        @Test
        @DisplayName("compressionFailed should create exception")
        void compressionFailedShouldCreateException() {
            var cause = new RuntimeException("IO error");
            var ex = OpenSerializationException.compressionFailed(cause);

            assertThat(ex.getMessage()).contains("Compression failed");
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("compressionFailed with algorithm should include algorithm")
        void compressionFailedWithAlgorithmShouldIncludeAlgorithm() {
            var ex = OpenSerializationException.compressionFailed("GZIP", new RuntimeException());

            assertThat(ex.getMessage()).contains("GZIP");
            assertThat(ex.getMessage()).contains("Compression failed");
        }
    }

    @Nested
    @DisplayName("decompressionFailed Factory Tests")
    class DecompressionFailedTests {

        @Test
        @DisplayName("decompressionFailed should create exception")
        void decompressionFailedShouldCreateException() {
            var cause = new RuntimeException("IO error");
            var ex = OpenSerializationException.decompressionFailed(cause);

            assertThat(ex.getMessage()).contains("Decompression failed");
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("decompressionFailed with algorithm should include algorithm")
        void decompressionFailedWithAlgorithmShouldIncludeAlgorithm() {
            var ex = OpenSerializationException.decompressionFailed("LZ4", new RuntimeException());

            assertThat(ex.getMessage()).contains("LZ4");
            assertThat(ex.getMessage()).contains("Decompression failed");
        }
    }

    @Nested
    @DisplayName("missingDependency Factory Tests")
    class MissingDependencyTests {

        @Test
        @DisplayName("missingDependency should create exception with format and dependency")
        void missingDependencyShouldCreateExceptionWithFormatAndDependency() {
            var ex = OpenSerializationException.missingDependency("kryo", "com.esotericsoftware:kryo");

            assertThat(ex.getMessage()).contains("kryo");
            assertThat(ex.getMessage()).contains("com.esotericsoftware:kryo");
            assertThat(ex.getMessage()).contains("not available");
            assertThat(ex.getFormat()).isEqualTo("kryo");
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("getFormat should return format")
        void getFormatShouldReturnFormat() {
            var ex = new OpenSerializationException("msg", "json", String.class, null);

            assertThat(ex.getFormat()).isEqualTo("json");
        }

        @Test
        @DisplayName("getTargetType should return target type")
        void getTargetTypeShouldReturnTargetType() {
            var ex = new OpenSerializationException("msg", "json", Integer.class, null);

            assertThat(ex.getTargetType()).isEqualTo(Integer.class);
        }
    }
}
