package cloud.opencode.base.web.body;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JsonBody")
class JsonBodyTest {

    @AfterEach
    void resetSerializer() {
        JsonBody.setSerializer(null);
    }

    @Nested
    @DisplayName("of(String)")
    class OfString {

        @Test
        @DisplayName("should create from JSON string")
        void shouldCreateFromString() {
            JsonBody body = JsonBody.of("{\"key\":\"value\"}");
            assertThat(body.getJson()).isEqualTo("{\"key\":\"value\"}");
        }

        @Test
        @DisplayName("should calculate content length in UTF-8")
        void shouldCalculateContentLength() {
            JsonBody body = JsonBody.of("{}");
            assertThat(body.getContentLength()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("of(Map)")
    class OfMap {

        @Test
        @DisplayName("should convert map to JSON")
        void shouldConvertMap() {
            JsonBody body = JsonBody.of(Map.of("name", "John"));
            assertThat(body.getJson()).contains("\"name\":\"John\"");
        }

        @Test
        @DisplayName("should handle null values in map")
        void shouldHandleNullValues() {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("key", null);
            JsonBody body = JsonBody.of(map);
            assertThat(body.getJson()).contains("\"key\":null");
        }

        @Test
        @DisplayName("should handle numeric values")
        void shouldHandleNumbers() {
            JsonBody body = JsonBody.of(Map.of("count", 42));
            assertThat(body.getJson()).contains("\"count\":42");
        }

        @Test
        @DisplayName("should handle boolean values")
        void shouldHandleBooleans() {
            JsonBody body = JsonBody.of(Map.of("active", true));
            assertThat(body.getJson()).contains("\"active\":true");
        }

        @Test
        @DisplayName("should handle nested maps")
        void shouldHandleNestedMaps() {
            Map<String, Object> nested = Map.of("inner", "val");
            JsonBody body = JsonBody.of(Map.of("outer", nested));
            assertThat(body.getJson()).contains("\"outer\":{\"inner\":\"val\"}");
        }

        @Test
        @DisplayName("should handle iterables")
        void shouldHandleIterables() {
            JsonBody body = JsonBody.of(Map.of("items", List.of(1, 2, 3)));
            assertThat(body.getJson()).contains("\"items\":[1,2,3]");
        }
    }

    @Nested
    @DisplayName("of(Object)")
    class OfObject {

        @Test
        @DisplayName("should delegate String to of(String)")
        void shouldDelegateString() {
            JsonBody body = JsonBody.of((Object) "{\"a\":1}");
            assertThat(body.getJson()).isEqualTo("{\"a\":1}");
        }

        @Test
        @DisplayName("should delegate Map to of(Map)")
        void shouldDelegateMap() {
            Object map = Map.of("key", "val");
            JsonBody body = JsonBody.of(map);
            assertThat(body.getJson()).contains("\"key\":\"val\"");
        }

        @Test
        @DisplayName("should throw when no serializer configured for non-String/Map")
        void shouldThrowWithoutSerializer() {
            assertThatThrownBy(() -> JsonBody.of(new Object()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("serializer not configured");
        }

        @Test
        @DisplayName("should use default serializer when configured")
        void shouldUseDefaultSerializer() {
            JsonBody.setSerializer(obj -> "\"custom\"");
            JsonBody body = JsonBody.of(new Object());
            assertThat(body.getJson()).isEqualTo("\"custom\"");
        }
    }

    @Nested
    @DisplayName("of(T, Function)")
    class OfWithSerializer {

        @Test
        @DisplayName("should use provided serializer")
        void shouldUseProvidedSerializer() {
            JsonBody body = JsonBody.of(42, num -> "{\"number\":" + num + "}");
            assertThat(body.getJson()).isEqualTo("{\"number\":42}");
        }
    }

    @Nested
    @DisplayName("setSerializer/getSerializer")
    class SerializerMethods {

        @Test
        @DisplayName("should set and get serializer")
        void shouldSetAndGet() {
            assertThat(JsonBody.getSerializer()).isNull();
            JsonBody.setSerializer(Object::toString);
            assertThat(JsonBody.getSerializer()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getContentType()")
    class GetContentType {

        @Test
        @DisplayName("should return JSON content type with charset")
        void shouldReturnJsonContentType() {
            JsonBody body = JsonBody.of("{}");
            assertThat(body.getContentType()).contains("application/json").contains("charset=utf-8");
        }
    }

    @Nested
    @DisplayName("getBodyPublisher()")
    class GetBodyPublisher {

        @Test
        @DisplayName("should return non-null publisher")
        void shouldReturnPublisher() {
            JsonBody body = JsonBody.of("{}");
            assertThat(body.getBodyPublisher()).isNotNull();
        }
    }

    @Nested
    @DisplayName("JSON escaping")
    class JsonEscaping {

        @Test
        @DisplayName("should escape special characters")
        void shouldEscapeSpecialChars() {
            JsonBody body = JsonBody.of(Map.of("text", "line1\nline2\ttab\"quote\\backslash"));
            String json = body.getJson();
            assertThat(json).contains("\\n", "\\t", "\\\"", "\\\\");
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTest {

        @Test
        @DisplayName("should include length")
        void shouldIncludeLength() {
            JsonBody body = JsonBody.of("{}");
            assertThat(body.toString()).contains("JsonBody", "length=");
        }
    }
}
