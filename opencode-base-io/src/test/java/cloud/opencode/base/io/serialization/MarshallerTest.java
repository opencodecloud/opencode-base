package cloud.opencode.base.io.serialization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Marshaller")
class MarshallerTest {

    @Nested
    @DisplayName("anonymous implementation")
    class AnonymousImplementation {

        private final Marshaller<String> stringMarshaller = new Marshaller<>() {
            @Override
            public byte[] marshal(String value) {
                return value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String unmarshal(byte[] data) {
                return data == null || data.length == 0 ? null : new String(data, StandardCharsets.UTF_8);
            }
        };

        @Test
        @DisplayName("should marshal string to bytes")
        void shouldMarshal() {
            byte[] result = stringMarshaller.marshal("hello");
            assertThat(result).isEqualTo("hello".getBytes(StandardCharsets.UTF_8));
        }

        @Test
        @DisplayName("should unmarshal bytes to string")
        void shouldUnmarshal() {
            String result = stringMarshaller.unmarshal("world".getBytes(StandardCharsets.UTF_8));
            assertThat(result).isEqualTo("world");
        }

        @Test
        @DisplayName("should round-trip marshal and unmarshal")
        void shouldRoundTrip() {
            String original = "test message";
            byte[] marshalled = stringMarshaller.marshal(original);
            String restored = stringMarshaller.unmarshal(marshalled);
            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("should handle null in marshal")
        void shouldHandleNullMarshal() {
            byte[] result = stringMarshaller.marshal(null);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should handle null in unmarshal")
        void shouldHandleNullUnmarshal() {
            String result = stringMarshaller.unmarshal(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should handle empty bytes in unmarshal")
        void shouldHandleEmptyUnmarshal() {
            String result = stringMarshaller.unmarshal(new byte[0]);
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("integer marshaller")
    class IntegerMarshaller {

        private final Marshaller<Integer> intMarshaller = new Marshaller<>() {
            @Override
            public byte[] marshal(Integer value) {
                String s = String.valueOf(value);
                return s.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public Integer unmarshal(byte[] data) {
                return Integer.parseInt(new String(data, StandardCharsets.UTF_8));
            }
        };

        @Test
        @DisplayName("should marshal integer to bytes")
        void shouldMarshalInteger() {
            byte[] result = intMarshaller.marshal(42);
            assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("42");
        }

        @Test
        @DisplayName("should unmarshal bytes to integer")
        void shouldUnmarshalInteger() {
            Integer result = intMarshaller.unmarshal("42".getBytes(StandardCharsets.UTF_8));
            assertThat(result).isEqualTo(42);
        }
    }
}
