package cloud.opencode.base.serialization;

import cloud.opencode.base.serialization.binary.JdkSerializer;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * StreamingSerializerTest Tests - Tests for Serializer interface streaming default methods
 * StreamingSerializerTest 测试类 - 测试 Serializer 接口的流式默认方法
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.3
 */
@DisplayName("Streaming Serializer Tests")
class StreamingSerializerTest {

    private Serializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new JdkSerializer();
    }

    @Nested
    @DisplayName("Serialize to Stream Tests")
    class SerializeToStreamTests {

        @Test
        @DisplayName("should serialize string to output stream")
        void shouldSerializeStringToOutputStream() throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            serializer.serialize("Hello, Stream!", out);

            byte[] data = out.toByteArray();
            assertThat(data).isNotEmpty();

            // Verify round-trip
            String result = serializer.deserialize(data, String.class);
            assertThat(result).isEqualTo("Hello, Stream!");
        }

        @Test
        @DisplayName("should serialize integer to output stream")
        void shouldSerializeIntegerToOutputStream() throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            serializer.serialize(42, out);

            byte[] data = out.toByteArray();
            assertThat(data).isNotEmpty();

            Integer result = serializer.deserialize(data, Integer.class);
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("should serialize list to output stream")
        void shouldSerializeListToOutputStream() throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ArrayList<String> list = new ArrayList<>(List.of("a", "b", "c"));

            serializer.serialize(list, out);

            byte[] data = out.toByteArray();
            assertThat(data).isNotEmpty();
        }

        @Test
        @DisplayName("should serialize record to output stream")
        void shouldSerializeRecordToOutputStream() throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            TestRecord record = new TestRecord("test", 123);

            serializer.serialize(record, out);

            byte[] data = out.toByteArray();
            assertThat(data).isNotEmpty();

            TestRecord result = serializer.deserialize(data, TestRecord.class);
            assertThat(result).isEqualTo(record);
        }
    }

    @Nested
    @DisplayName("Deserialize from Stream Tests")
    class DeserializeFromStreamTests {

        @Test
        @DisplayName("should deserialize string from input stream")
        void shouldDeserializeStringFromInputStream() throws IOException {
            byte[] data = serializer.serialize("Hello, Stream!");
            ByteArrayInputStream in = new ByteArrayInputStream(data);

            String result = serializer.deserialize(in, String.class);

            assertThat(result).isEqualTo("Hello, Stream!");
        }

        @Test
        @DisplayName("should deserialize integer from input stream")
        void shouldDeserializeIntegerFromInputStream() throws IOException {
            byte[] data = serializer.serialize(42);
            ByteArrayInputStream in = new ByteArrayInputStream(data);

            Integer result = serializer.deserialize(in, Integer.class);

            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("should deserialize list from input stream")
        void shouldDeserializeListFromInputStream() throws IOException {
            ArrayList<String> original = new ArrayList<>(List.of("x", "y", "z"));
            byte[] data = serializer.serialize(original);
            ByteArrayInputStream in = new ByteArrayInputStream(data);

            TypeReference<ArrayList<String>> typeRef = new TypeReference<>() {};
            ArrayList<String> result = serializer.deserialize(in, typeRef);

            assertThat(result).containsExactly("x", "y", "z");
        }

        @Test
        @DisplayName("should deserialize record from input stream")
        void shouldDeserializeRecordFromInputStream() throws IOException {
            TestRecord original = new TestRecord("data", 999);
            byte[] data = serializer.serialize(original);
            ByteArrayInputStream in = new ByteArrayInputStream(data);

            TestRecord result = serializer.deserialize(in, TestRecord.class);

            assertThat(result).isEqualTo(original);
        }

        @Test
        @DisplayName("should round-trip through streams")
        void shouldRoundTripThroughStreams() throws IOException {
            TestRecord original = new TestRecord("round-trip", 42);

            // Serialize to stream
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            serializer.serialize(original, out);

            // Deserialize from stream
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            TestRecord result = serializer.deserialize(in, TestRecord.class);

            assertThat(result).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("Null Argument Tests")
    class NullArgumentTests {

        @Test
        @DisplayName("serialize to null stream should throw")
        void serializeToNullStreamShouldThrow() {
            assertThatThrownBy(() -> serializer.serialize("test", (OutputStream) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("deserialize from null stream should throw")
        void deserializeFromNullStreamShouldThrow() {
            assertThatThrownBy(() -> serializer.deserialize((InputStream) null, String.class))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("deserialize with null class should throw")
        void deserializeWithNullClassShouldThrow() {
            byte[] data = serializer.serialize("test");
            ByteArrayInputStream in = new ByteArrayInputStream(data);

            assertThatThrownBy(() -> serializer.deserialize(in, (Class<?>) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("serialize null object to stream should not throw for JDK serializer")
        void serializeNullObjectToStreamShouldNotThrowForJdkSerializer() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // JDK serializer may or may not support null - just check it doesn't crash unexpectedly
            assertThatCode(() -> serializer.serialize(null, out))
                    .doesNotThrowAnyException();
        }
    }

    // Test helper record
    record TestRecord(String name, int value) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }
}
