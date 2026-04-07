package cloud.opencode.base.string.codec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("KeyValueCodec")
class KeyValueCodecTest {

    @Nested
    @DisplayName("encode(Map)")
    class EncodeDefault {

        @Test
        @DisplayName("should encode map with default separators")
        void shouldEncodeMap() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("host", "localhost");
            map.put("port", "8080");
            String result = KeyValueCodec.encode(map);
            assertThat(result).isEqualTo("host=localhost;port=8080");
        }

        @Test
        @DisplayName("should return null for null map")
        void shouldReturnNullForNullMap() {
            assertThat(KeyValueCodec.encode(null)).isNull();
        }

        @Test
        @DisplayName("should return null for empty map")
        void shouldReturnNullForEmptyMap() {
            assertThat(KeyValueCodec.encode(Map.of())).isNull();
        }

        @Test
        @DisplayName("should encode single entry")
        void shouldEncodeSingleEntry() {
            String result = KeyValueCodec.encode(Map.of("key", "value"));
            assertThat(result).isEqualTo("key=value");
        }
    }

    @Nested
    @DisplayName("encode(Map, String, String)")
    class EncodeCustom {

        @Test
        @DisplayName("should encode with custom separators")
        void shouldEncodeWithCustomSeparators() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("host", "localhost");
            map.put("port", "8080");
            String result = KeyValueCodec.encode(map, "&", ":");
            assertThat(result).isEqualTo("host:localhost&port:8080");
        }

        @Test
        @DisplayName("should throw NPE for null entry separator")
        void shouldThrowForNullEntrySeparator() {
            assertThatThrownBy(() -> KeyValueCodec.encode(Map.of("k", "v"), null, "="))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw NPE for null key-value separator")
        void shouldThrowForNullKvSeparator() {
            assertThatThrownBy(() -> KeyValueCodec.encode(Map.of("k", "v"), ";", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should return null for null map with custom separators")
        void shouldReturnNullForNullMap() {
            assertThat(KeyValueCodec.encode(null, "&", ":")).isNull();
        }
    }

    @Nested
    @DisplayName("decode(String)")
    class DecodeDefault {

        @Test
        @DisplayName("should decode default format")
        void shouldDecodeDefaultFormat() {
            Map<String, String> result = KeyValueCodec.decode("host=localhost;port=8080");
            assertThat(result).containsEntry("host", "localhost")
                    .containsEntry("port", "8080")
                    .hasSize(2);
        }

        @Test
        @DisplayName("should return empty map for null input")
        void shouldReturnEmptyForNull() {
            assertThat(KeyValueCodec.decode(null)).isEmpty();
        }

        @Test
        @DisplayName("should return empty map for blank input")
        void shouldReturnEmptyForBlank() {
            assertThat(KeyValueCodec.decode("   ")).isEmpty();
        }

        @Test
        @DisplayName("should return empty map for empty input")
        void shouldReturnEmptyForEmpty() {
            assertThat(KeyValueCodec.decode("")).isEmpty();
        }

        @Test
        @DisplayName("should return unmodifiable map")
        void shouldReturnUnmodifiableMap() {
            Map<String, String> result = KeyValueCodec.decode("k=v");
            assertThatThrownBy(() -> result.put("a", "b"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("should skip entries without key-value separator")
        void shouldSkipEntriesWithoutSeparator() {
            Map<String, String> result = KeyValueCodec.decode("key=value;invalid;other=ok");
            assertThat(result).containsEntry("key", "value")
                    .containsEntry("other", "ok")
                    .hasSize(2);
        }

        @Test
        @DisplayName("should handle value containing separator")
        void shouldHandleValueContainingSeparator() {
            Map<String, String> result = KeyValueCodec.decode("url=http://host:8080/path");
            assertThat(result).containsKey("url");
        }
    }

    @Nested
    @DisplayName("decode(String, String, String)")
    class DecodeCustom {

        @Test
        @DisplayName("should decode with custom separators")
        void shouldDecodeWithCustomSeparators() {
            Map<String, String> result = KeyValueCodec.decode("host:localhost&port:8080", "&", ":");
            assertThat(result).containsEntry("host", "localhost")
                    .containsEntry("port", "8080");
        }

        @Test
        @DisplayName("should throw NPE for null entry separator")
        void shouldThrowForNullEntrySeparator() {
            assertThatThrownBy(() -> KeyValueCodec.decode("k=v", null, "="))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw NPE for null key-value separator")
        void shouldThrowForNullKvSeparator() {
            assertThatThrownBy(() -> KeyValueCodec.decode("k=v", ";", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("round-trip")
    class RoundTrip {

        @Test
        @DisplayName("should round-trip encode and decode")
        void shouldRoundTrip() {
            Map<String, String> original = new LinkedHashMap<>();
            original.put("a", "1");
            original.put("b", "2");
            original.put("c", "3");
            String encoded = KeyValueCodec.encode(original);
            Map<String, String> decoded = KeyValueCodec.decode(encoded);
            assertThat(decoded).isEqualTo(original);
        }
    }
}
