package cloud.opencode.base.classloader.scanner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for CachedScanResult
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
@DisplayName("CachedScanResult Tests")
class CachedScanResultTest {

    @Nested
    @DisplayName("Record Creation Tests")
    class RecordCreationTests {

        @Test
        @DisplayName("Should create record with valid data")
        void shouldCreateRecordWithValidData() {
            Set<String> classNames = Set.of("com.example.Foo", "com.example.Bar");
            CachedScanResult result = new CachedScanResult("abc123", classNames, "2026-04-01T00:00:00Z");

            assertThat(result.classpathHash()).isEqualTo("abc123");
            assertThat(result.classNames()).containsExactlyInAnyOrder("com.example.Foo", "com.example.Bar");
            assertThat(result.timestamp()).isEqualTo("2026-04-01T00:00:00Z");
        }

        @Test
        @DisplayName("Should create defensive copy of classNames")
        void shouldCreateDefensiveCopy() {
            Set<String> original = new HashSet<>();
            original.add("com.example.Foo");

            CachedScanResult result = new CachedScanResult("hash", original, "ts");

            // Modify original — should not affect record
            original.add("com.example.Bar");
            assertThat(result.classNames()).hasSize(1);
            assertThat(result.classNames()).containsExactly("com.example.Foo");
        }

        @Test
        @DisplayName("Should return unmodifiable classNames set")
        void shouldReturnUnmodifiableClassNames() {
            CachedScanResult result = new CachedScanResult("hash", Set.of("A"), "ts");

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> result.classNames().add("B"));
        }

        @Test
        @DisplayName("Should throw on null classpathHash")
        void shouldThrowOnNullHash() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new CachedScanResult(null, Set.of(), "ts"))
                    .withMessageContaining("classpathHash");
        }

        @Test
        @DisplayName("Should throw on null classNames")
        void shouldThrowOnNullClassNames() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new CachedScanResult("hash", null, "ts"))
                    .withMessageContaining("classNames");
        }

        @Test
        @DisplayName("Should throw on null timestamp")
        void shouldThrowOnNullTimestamp() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new CachedScanResult("hash", Set.of(), null))
                    .withMessageContaining("timestamp");
        }

        @Test
        @DisplayName("Should create with empty classNames")
        void shouldCreateWithEmptyClassNames() {
            CachedScanResult result = new CachedScanResult("hash", Set.of(), "ts");

            assertThat(result.classNames()).isEmpty();
        }
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        @DisplayName("Should serialize to JSON and back")
        void shouldSerializeAndDeserialize() {
            Set<String> classNames = new LinkedHashSet<>(List.of(
                    "com.example.Foo", "com.example.Bar", "com.example.Baz"
            ));
            CachedScanResult original = new CachedScanResult("sha256hash", classNames, "2026-04-01T12:00:00Z");

            String json = original.toJson();
            CachedScanResult restored = CachedScanResult.fromJson(json);

            assertThat(restored.classpathHash()).isEqualTo(original.classpathHash());
            assertThat(restored.timestamp()).isEqualTo(original.timestamp());
            assertThat(restored.classNames()).containsExactlyInAnyOrderElementsOf(original.classNames());
        }

        @Test
        @DisplayName("Should serialize empty classNames")
        void shouldSerializeEmptyClassNames() {
            CachedScanResult original = new CachedScanResult("hash", Set.of(), "ts");

            String json = original.toJson();
            CachedScanResult restored = CachedScanResult.fromJson(json);

            assertThat(restored.classNames()).isEmpty();
            assertThat(restored.classpathHash()).isEqualTo("hash");
            assertThat(restored.timestamp()).isEqualTo("ts");
        }

        @Test
        @DisplayName("Should handle special characters in JSON")
        void shouldHandleSpecialCharacters() {
            Set<String> classNames = Set.of("com.example.My\"Class");
            CachedScanResult original = new CachedScanResult("hash\\value", classNames, "ts\nnewline");

            String json = original.toJson();
            CachedScanResult restored = CachedScanResult.fromJson(json);

            assertThat(restored.classpathHash()).isEqualTo("hash\\value");
            assertThat(restored.classNames()).containsExactly("com.example.My\"Class");
            assertThat(restored.timestamp()).isEqualTo("ts\nnewline");
        }

        @Test
        @DisplayName("Should produce sorted classNames in JSON")
        void shouldProduceSortedClassNames() {
            Set<String> classNames = Set.of("Z.class", "A.class", "M.class");
            CachedScanResult result = new CachedScanResult("hash", classNames, "ts");

            String json = result.toJson();

            // A.class should appear before M.class which should appear before Z.class
            int indexA = json.indexOf("A.class");
            int indexM = json.indexOf("M.class");
            int indexZ = json.indexOf("Z.class");
            assertThat(indexA).isLessThan(indexM);
            assertThat(indexM).isLessThan(indexZ);
        }

        @Test
        @DisplayName("Should throw on null JSON input")
        void shouldThrowOnNullJson() {
            assertThatNullPointerException()
                    .isThrownBy(() -> CachedScanResult.fromJson(null));
        }

        @Test
        @DisplayName("Should throw on invalid JSON missing key")
        void shouldThrowOnMissingKey() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> CachedScanResult.fromJson("{\"classpathHash\": \"h\"}"));
        }

        @Test
        @DisplayName("Should produce valid JSON structure")
        void shouldProduceValidJsonStructure() {
            CachedScanResult result = new CachedScanResult("abc", Set.of("X"), "ts");
            String json = result.toJson();

            assertThat(json).startsWith("{");
            assertThat(json).endsWith("}");
            assertThat(json).contains("\"classpathHash\"");
            assertThat(json).contains("\"timestamp\"");
            assertThat(json).contains("\"classNames\"");
        }
    }

    @Nested
    @DisplayName("JSON Escape/Unescape Tests")
    class JsonEscapeTests {

        @Test
        @DisplayName("Should escape special characters")
        void shouldEscapeSpecialChars() {
            assertThat(CachedScanResult.escapeJson("hello\"world")).isEqualTo("hello\\\"world");
            assertThat(CachedScanResult.escapeJson("back\\slash")).isEqualTo("back\\\\slash");
            assertThat(CachedScanResult.escapeJson("new\nline")).isEqualTo("new\\nline");
            assertThat(CachedScanResult.escapeJson("tab\there")).isEqualTo("tab\\there");
        }

        @Test
        @DisplayName("Should unescape special characters")
        void shouldUnescapeSpecialChars() {
            assertThat(CachedScanResult.unescapeJson("hello\\\"world")).isEqualTo("hello\"world");
            assertThat(CachedScanResult.unescapeJson("back\\\\slash")).isEqualTo("back\\slash");
            assertThat(CachedScanResult.unescapeJson("new\\nline")).isEqualTo("new\nline");
        }

        @Test
        @DisplayName("Should handle null in escape")
        void shouldHandleNullEscape() {
            assertThat(CachedScanResult.escapeJson(null)).isEmpty();
        }

        @Test
        @DisplayName("Should handle null in unescape")
        void shouldHandleNullUnescape() {
            assertThat(CachedScanResult.unescapeJson(null)).isNull();
        }

        @Test
        @DisplayName("Should handle unicode escape")
        void shouldHandleUnicodeEscape() {
            assertThat(CachedScanResult.unescapeJson("\\u0041")).isEqualTo("A");
        }
    }
}
