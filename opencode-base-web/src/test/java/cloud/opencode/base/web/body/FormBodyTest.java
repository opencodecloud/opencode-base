package cloud.opencode.base.web.body;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FormBody")
class FormBodyTest {

    @Nested
    @DisplayName("empty()")
    class Empty {

        @Test
        @DisplayName("should create empty form body")
        void shouldCreateEmpty() {
            FormBody body = FormBody.empty();
            assertThat(body.isEmpty()).isTrue();
            assertThat(body.size()).isZero();
            assertThat(body.getEncoded()).isEmpty();
        }
    }

    @Nested
    @DisplayName("of(Map)")
    class OfMap {

        @Test
        @DisplayName("should create from map")
        void shouldCreateFromMap() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("name", "John");
            map.put("age", "30");
            FormBody body = FormBody.of(map);
            assertThat(body.size()).isEqualTo(2);
            assertThat(body.get("name")).isEqualTo("John");
            assertThat(body.get("age")).isEqualTo("30");
        }
    }

    @Nested
    @DisplayName("of(String...)")
    class OfPairs {

        @Test
        @DisplayName("should create from key-value pairs")
        void shouldCreateFromPairs() {
            FormBody body = FormBody.of("a", "1", "b", "2");
            assertThat(body.size()).isEqualTo(2);
            assertThat(body.get("a")).isEqualTo("1");
            assertThat(body.get("b")).isEqualTo("2");
        }

        @Test
        @DisplayName("should throw for odd number of arguments")
        void shouldThrowForOddArgs() {
            assertThatThrownBy(() -> FormBody.of("a", "1", "b"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("even-length");
        }
    }

    @Nested
    @DisplayName("getContentType()")
    class GetContentType {

        @Test
        @DisplayName("should return form content type")
        void shouldReturnFormContentType() {
            FormBody body = FormBody.of("k", "v");
            assertThat(body.getContentType()).contains("application/x-www-form-urlencoded");
        }
    }

    @Nested
    @DisplayName("getEncoded()")
    class GetEncoded {

        @Test
        @DisplayName("should URL-encode values")
        void shouldUrlEncode() {
            FormBody body = FormBody.of("name", "hello world");
            assertThat(body.getEncoded()).isEqualTo("name=hello+world");
        }

        @Test
        @DisplayName("should separate pairs with ampersand")
        void shouldSeparateWithAmpersand() {
            FormBody body = FormBody.of("a", "1", "b", "2");
            assertThat(body.getEncoded()).isEqualTo("a=1&b=2");
        }
    }

    @Nested
    @DisplayName("get(String)")
    class Get {

        @Test
        @DisplayName("should return first matching value")
        void shouldReturnFirstMatch() {
            FormBody body = FormBody.of("key", "value");
            assertThat(body.get("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("should return null for missing key")
        void shouldReturnNullForMissing() {
            FormBody body = FormBody.of("key", "value");
            assertThat(body.get("missing")).isNull();
        }
    }

    @Nested
    @DisplayName("getAll(String)")
    class GetAll {

        @Test
        @DisplayName("should return all values for key")
        void shouldReturnAllValues() {
            FormBody body = FormBody.builder()
                    .add("color", "red")
                    .add("color", "blue")
                    .build();
            List<String> values = body.getAll("color");
            assertThat(values).containsExactly("red", "blue");
        }

        @Test
        @DisplayName("should return empty list for missing key")
        void shouldReturnEmptyForMissing() {
            FormBody body = FormBody.empty();
            assertThat(body.getAll("missing")).isEmpty();
        }
    }

    @Nested
    @DisplayName("toMap()")
    class ToMap {

        @Test
        @DisplayName("should convert to map with last value for duplicate keys")
        void shouldConvertToMap() {
            FormBody body = FormBody.builder()
                    .add("a", "1")
                    .add("b", "2")
                    .add("a", "3")
                    .build();
            Map<String, String> map = body.toMap();
            assertThat(map).containsEntry("a", "3").containsEntry("b", "2");
        }
    }

    @Nested
    @DisplayName("getContentLength()")
    class GetContentLength {

        @Test
        @DisplayName("should return byte length of encoded body")
        void shouldReturnByteLength() {
            FormBody body = FormBody.of("k", "v");
            assertThat(body.getContentLength()).isEqualTo("k=v".getBytes().length);
        }

        @Test
        @DisplayName("should return zero for empty body")
        void shouldReturnZeroForEmpty() {
            FormBody body = FormBody.empty();
            assertThat(body.getContentLength()).isZero();
        }
    }

    @Nested
    @DisplayName("getBodyPublisher()")
    class GetBodyPublisher {

        @Test
        @DisplayName("should return non-null publisher")
        void shouldReturnPublisher() {
            FormBody body = FormBody.of("k", "v");
            assertThat(body.getBodyPublisher()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getEntries()")
    class GetEntries {

        @Test
        @DisplayName("should return unmodifiable list")
        void shouldReturnUnmodifiable() {
            FormBody body = FormBody.of("k", "v");
            assertThatThrownBy(() -> body.getEntries().add(Map.entry("x", "y")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTest {

        @Test
        @DisplayName("add should convert null value to empty string")
        void addShouldConvertNull() {
            FormBody body = FormBody.builder().add("key", null).build();
            assertThat(body.get("key")).isEmpty();
        }

        @Test
        @DisplayName("addIfNotNull should skip null values")
        void addIfNotNullShouldSkip() {
            FormBody body = FormBody.builder()
                    .addIfNotNull("key", null)
                    .build();
            assertThat(body.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("addIfNotNull should add non-null values")
        void addIfNotNullShouldAdd() {
            FormBody body = FormBody.builder()
                    .addIfNotNull("key", "value")
                    .build();
            assertThat(body.get("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("addAll should add all map entries")
        void addAllShouldAddAll() {
            FormBody body = FormBody.builder()
                    .addAll(Map.of("a", "1", "b", "2"))
                    .build();
            assertThat(body.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTest {

        @Test
        @DisplayName("should include entry count and encoded content")
        void shouldIncludeInfo() {
            FormBody body = FormBody.of("k", "v");
            assertThat(body.toString()).contains("FormBody", "k=v");
        }
    }
}
