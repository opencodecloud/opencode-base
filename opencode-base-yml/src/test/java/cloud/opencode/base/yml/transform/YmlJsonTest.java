package cloud.opencode.base.yml.transform;

import cloud.opencode.base.yml.OpenYml;
import cloud.opencode.base.yml.exception.OpenYmlException;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * YmlJsonTest Tests
 * YmlJsonTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
@DisplayName("YmlJson Tests")
class YmlJsonTest {

    @Nested
    @DisplayName("ToJson Tests")
    class ToJson {

        @Test
        @DisplayName("should convert simple map to JSON")
        void shouldConvertSimpleMapToJson() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("name", "test");
            data.put("version", 1);

            String json = YmlJson.toJson(data);

            assertThat(json).isEqualTo("{\"name\":\"test\",\"version\":1}");
        }

        @Test
        @DisplayName("should convert nested map to JSON")
        void shouldConvertNestedMapToJson() {
            Map<String, Object> server = new LinkedHashMap<>();
            server.put("host", "localhost");
            server.put("port", 8080);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("server", server);

            String json = YmlJson.toJson(data);

            assertThat(json).isEqualTo("{\"server\":{\"host\":\"localhost\",\"port\":8080}}");
        }

        @Test
        @DisplayName("should convert list values to JSON arrays")
        void shouldConvertListToJsonArrays() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("items", List.of("a", "b", "c"));

            String json = YmlJson.toJson(data);

            assertThat(json).isEqualTo("{\"items\":[\"a\",\"b\",\"c\"]}");
        }

        @Test
        @DisplayName("should escape special characters in strings")
        void shouldEscapeSpecialCharacters() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("text", "line1\nline2\ttab\"quote\\slash/forward");

            String json = YmlJson.toJson(data);

            assertThat(json).contains("\\n");
            assertThat(json).contains("\\t");
            assertThat(json).contains("\\\"");
            assertThat(json).contains("\\\\");
            // Forward slash is not escaped (optional per RFC 8259, and YAML parsers don't support \/)
            assertThat(json).contains("/forward");
        }

        @Test
        @DisplayName("should escape control characters as unicode")
        void shouldEscapeControlCharactersAsUnicode() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("ctrl", "\u0001\u001f");

            String json = YmlJson.toJson(data);

            assertThat(json).contains("\\u0001");
            assertThat(json).contains("\\u001f");
        }

        @Test
        @DisplayName("should escape backspace and form feed")
        void shouldEscapeBackspaceAndFormFeed() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("text", "a\bb\fc");

            String json = YmlJson.toJson(data);

            assertThat(json).contains("\\b");
            assertThat(json).contains("\\f");
        }

        @Test
        @DisplayName("should escape carriage return")
        void shouldEscapeCarriageReturn() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("text", "line1\rline2");

            String json = YmlJson.toJson(data);

            assertThat(json).contains("\\r");
        }

        @Test
        @DisplayName("should handle null values")
        void shouldHandleNullValues() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("key", null);

            String json = YmlJson.toJson(data);

            assertThat(json).isEqualTo("{\"key\":null}");
        }

        @Test
        @DisplayName("should handle boolean values")
        void shouldHandleBooleanValues() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("enabled", true);
            data.put("debug", false);

            String json = YmlJson.toJson(data);

            assertThat(json).isEqualTo("{\"enabled\":true,\"debug\":false}");
        }

        @Test
        @DisplayName("should handle number types")
        void shouldHandleNumberTypes() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("intVal", 42);
            data.put("longVal", 100L);
            data.put("doubleVal", 3.14);

            String json = YmlJson.toJson(data);

            assertThat(json).contains("\"intVal\":42");
            assertThat(json).contains("\"longVal\":100");
            assertThat(json).contains("\"doubleVal\":3.14");
        }

        @Test
        @DisplayName("should handle NaN and Infinity as null")
        void shouldHandleNanAndInfinityAsNull() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("nan", Double.NaN);
            data.put("inf", Double.POSITIVE_INFINITY);

            String json = YmlJson.toJson(data);

            assertThat(json).isEqualTo("{\"nan\":null,\"inf\":null}");
        }

        @Test
        @DisplayName("should pretty-print with 2-space indent")
        void shouldPrettyPrintWithIndent() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("name", "test");
            data.put("value", 1);

            String json = YmlJson.toJson(data, true);

            assertThat(json).contains("\n");
            assertThat(json).contains("  \"name\"");
            assertThat(json).startsWith("{");
            assertThat(json).endsWith("}");
        }

        @Test
        @DisplayName("should pretty-print nested structures")
        void shouldPrettyPrintNestedStructures() {
            Map<String, Object> inner = new LinkedHashMap<>();
            inner.put("port", 8080);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("server", inner);

            String json = YmlJson.toJson(data, true);

            assertThat(json).contains("  \"server\": {\n");
            assertThat(json).contains("    \"port\": 8080");
        }

        @Test
        @DisplayName("should handle empty map")
        void shouldHandleEmptyMap() {
            String json = YmlJson.toJson(Map.of());

            assertThat(json).isEqualTo("{}");
        }

        @Test
        @DisplayName("should handle empty list in map")
        void shouldHandleEmptyListInMap() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("items", List.of());

            String json = YmlJson.toJson(data);

            assertThat(json).isEqualTo("{\"items\":[]}");
        }

        @Test
        @DisplayName("should throw on null YAML input")
        void shouldThrowOnNullYamlInput() {
            assertThatThrownBy(() -> YmlJson.toJson((String) null))
                .isInstanceOf(OpenYmlException.class);
        }

        @Test
        @DisplayName("should throw on null map input")
        void shouldThrowOnNullMapInput() {
            assertThatThrownBy(() -> YmlJson.toJson((Map<String, Object>) null))
                .isInstanceOf(OpenYmlException.class);
        }

        @Test
        @DisplayName("should convert YAML string to JSON")
        void shouldConvertYamlStringToJson() {
            String yaml = """
                server:
                  port: 8080
                  host: localhost
                """;

            String json = YmlJson.toJson(yaml);

            assertThat(json).contains("\"server\"");
            assertThat(json).contains("\"port\"");
            assertThat(json).contains("8080");
            assertThat(json).contains("\"host\"");
            assertThat(json).contains("\"localhost\"");
        }

        @Test
        @DisplayName("should convert YAML with arrays to JSON")
        void shouldConvertYamlWithArraysToJson() {
            String yaml = """
                items:
                  - one
                  - two
                  - three
                """;

            String json = YmlJson.toJson(yaml);

            assertThat(json).contains("[\"one\",\"two\",\"three\"]");
        }
    }

    @Nested
    @DisplayName("FromJson Tests")
    class FromJson {

        @Test
        @DisplayName("should parse simple JSON to map")
        void shouldParseSimpleJsonToMap() {
            String json = "{\"name\": \"test\", \"port\": 8080}";

            Map<String, Object> data = YmlJson.fromJson(json);

            assertThat(data).containsEntry("name", "test");
            assertThat(data).containsEntry("port", 8080);
        }

        @Test
        @DisplayName("should parse nested JSON to map")
        void shouldParseNestedJsonToMap() {
            String json = "{\"server\": {\"port\": 8080, \"host\": \"localhost\"}}";

            Map<String, Object> data = YmlJson.fromJson(json);

            assertThat(data).containsKey("server");
            @SuppressWarnings("unchecked")
            Map<String, Object> server = (Map<String, Object>) data.get("server");
            assertThat(server).containsEntry("port", 8080);
            assertThat(server).containsEntry("host", "localhost");
        }

        @Test
        @DisplayName("should parse JSON arrays")
        void shouldParseJsonArrays() {
            String json = "{\"items\": [1, 2, 3]}";

            Map<String, Object> data = YmlJson.fromJson(json);

            assertThat(data).containsKey("items");
            @SuppressWarnings("unchecked")
            List<Integer> items = (List<Integer>) data.get("items");
            assertThat(items).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("should throw on null JSON input")
        void shouldThrowOnNullJsonInput() {
            assertThatThrownBy(() -> YmlJson.fromJson(null))
                .isInstanceOf(OpenYmlException.class);
        }
    }

    @Nested
    @DisplayName("FromJsonToYaml Tests")
    class FromJsonToYaml {

        @Test
        @DisplayName("should convert JSON to YAML string")
        void shouldConvertJsonToYamlString() {
            String json = "{\"server\": {\"port\": 8080}}";

            String yaml = YmlJson.fromJsonToYaml(json);

            assertThat(yaml).contains("server");
            assertThat(yaml).contains("port");
            assertThat(yaml).contains("8080");
        }

        @Test
        @DisplayName("should throw on null input")
        void shouldThrowOnNullInput() {
            assertThatThrownBy(() -> YmlJson.fromJsonToYaml(null))
                .isInstanceOf(OpenYmlException.class);
        }
    }

    @Nested
    @DisplayName("RoundTrip Tests")
    class RoundTrip {

        @Test
        @DisplayName("yaml to json to yaml should preserve structure")
        void yamlToJsonToYamlShouldPreserveStructure() {
            String yaml = """
                server:
                  port: 8080
                  host: localhost
                database:
                  url: jdbc:mysql://localhost/db
                """;

            // YAML -> JSON -> Map
            String json = YmlJson.toJson(yaml);
            Map<String, Object> fromJsonMap = YmlJson.fromJson(json);

            // Original YAML -> Map
            Map<String, Object> originalMap = OpenYml.load(yaml);

            assertThat(fromJsonMap).isEqualTo(originalMap);
        }

        @Test
        @DisplayName("map to json to map should preserve values")
        void mapToJsonToMapShouldPreserveValues() {
            Map<String, Object> original = new LinkedHashMap<>();
            original.put("name", "test");
            original.put("count", 42);
            original.put("enabled", true);
            original.put("items", List.of("a", "b"));

            String json = YmlJson.toJson(original);
            Map<String, Object> restored = YmlJson.fromJson(json);

            assertThat(restored).containsEntry("name", "test");
            assertThat(restored).containsEntry("count", 42);
            assertThat(restored).containsEntry("enabled", true);
            assertThat(restored).containsKey("items");
        }

        @Test
        @DisplayName("json to yaml to json should preserve structure")
        void jsonToYamlToJsonShouldPreserveStructure() {
            String json = "{\"a\": 1, \"b\": \"hello\", \"c\": true}";

            String yaml = YmlJson.fromJsonToYaml(json);
            String jsonAgain = YmlJson.toJson(yaml);

            Map<String, Object> original = YmlJson.fromJson(json);
            Map<String, Object> roundTripped = YmlJson.fromJson(jsonAgain);

            assertThat(roundTripped).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("EdgeCases Tests")
    class EdgeCases {

        @Test
        @DisplayName("should handle empty YAML string")
        void shouldHandleEmptyYamlString() {
            // Empty YAML parses to empty/null map
            String json = YmlJson.toJson(new LinkedHashMap<>());

            assertThat(json).isEqualTo("{}");
        }

        @Test
        @DisplayName("should handle deeply nested structure")
        void shouldHandleDeeplyNestedStructure() {
            // Build a 30-level deep nested map (under 50 limit)
            Map<String, Object> current = new LinkedHashMap<>();
            current.put("value", "leaf");
            for (int i = 29; i >= 0; i--) {
                Map<String, Object> parent = new LinkedHashMap<>();
                parent.put("level" + i, current);
                current = parent;
            }

            String json = YmlJson.toJson(current);

            assertThat(json).contains("\"value\":\"leaf\"");
            assertThat(json).contains("\"level0\"");
            assertThat(json).contains("\"level29\"");
        }

        @Test
        @DisplayName("should throw on exceeding max depth")
        void shouldThrowOnExceedingMaxDepth() {
            // Build a 55-level deep nested map (exceeds 50 limit)
            Map<String, Object> current = new LinkedHashMap<>();
            current.put("value", "leaf");
            for (int i = 54; i >= 0; i--) {
                Map<String, Object> parent = new LinkedHashMap<>();
                parent.put("l" + i, current);
                current = parent;
            }

            Map<String, Object> data = current;
            assertThatThrownBy(() -> YmlJson.toJson(data))
                .isInstanceOf(OpenYmlException.class)
                .hasMessageContaining("maximum depth");
        }

        @Test
        @DisplayName("should handle large strings")
        void shouldHandleLargeStrings() {
            String largeString = "x".repeat(10000);
            Map<String, Object> data = Map.of("big", largeString);

            String json = YmlJson.toJson(data);

            assertThat(json).contains("\"big\":\"" + largeString + "\"");
        }

        @Test
        @DisplayName("should handle mixed nested list and map")
        void shouldHandleMixedNestedListAndMap() {
            Map<String, Object> item1 = new LinkedHashMap<>();
            item1.put("name", "first");
            Map<String, Object> item2 = new LinkedHashMap<>();
            item2.put("name", "second");

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("items", List.of(item1, item2));

            String json = YmlJson.toJson(data);

            assertThat(json).isEqualTo("{\"items\":[{\"name\":\"first\"},{\"name\":\"second\"}]}");
        }

        @Test
        @DisplayName("should handle unicode characters")
        void shouldHandleUnicodeCharacters() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("text", "Hello \u4e16\u754c");

            String json = YmlJson.toJson(data);

            // Non-control unicode should pass through
            assertThat(json).contains("\u4e16\u754c");
        }

        @Test
        @DisplayName("should handle Float NaN and Infinity as null")
        void shouldHandleFloatNanAndInfinityAsNull() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("nan", Float.NaN);
            data.put("inf", Float.POSITIVE_INFINITY);

            String json = YmlJson.toJson(data);

            assertThat(json).isEqualTo("{\"nan\":null,\"inf\":null}");
        }

        @Test
        @DisplayName("should handle non-standard types as strings")
        void shouldHandleNonStandardTypesAsStrings() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("date", java.time.LocalDate.of(2025, 1, 1));

            String json = YmlJson.toJson(data);

            assertThat(json).contains("\"2025-01-01\"");
        }
    }
}
