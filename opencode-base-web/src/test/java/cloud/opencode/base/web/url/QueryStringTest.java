package cloud.opencode.base.web.url;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("QueryString")
class QueryStringTest {

    @Nested
    @DisplayName("empty()")
    class Empty {

        @Test
        @DisplayName("should create empty query string")
        void shouldCreateEmpty() {
            QueryString qs = QueryString.empty();
            assertThat(qs.isEmpty()).isTrue();
            assertThat(qs.size()).isZero();
            assertThat(qs.toString()).isEmpty();
        }
    }

    @Nested
    @DisplayName("parse(String)")
    class Parse {

        @Test
        @DisplayName("should parse simple query string")
        void shouldParseSimple() {
            QueryString qs = QueryString.parse("name=John&age=30");
            assertThat(qs.get("name")).isEqualTo("John");
            assertThat(qs.get("age")).isEqualTo("30");
            assertThat(qs.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("should handle null input")
        void shouldHandleNull() {
            QueryString qs = QueryString.parse(null);
            assertThat(qs.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("should handle empty string")
        void shouldHandleEmpty() {
            QueryString qs = QueryString.parse("");
            assertThat(qs.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("should strip leading question mark")
        void shouldStripLeadingQuestionMark() {
            QueryString qs = QueryString.parse("?name=John");
            assertThat(qs.get("name")).isEqualTo("John");
        }

        @Test
        @DisplayName("should handle key without value")
        void shouldHandleKeyWithoutValue() {
            QueryString qs = QueryString.parse("flag");
            assertThat(qs.has("flag")).isTrue();
            assertThat(qs.get("flag")).isEmpty();
        }

        @Test
        @DisplayName("should handle URL-encoded values")
        void shouldHandleUrlEncoded() {
            QueryString qs = QueryString.parse("name=hello+world&path=%2Fapi%2Fusers");
            assertThat(qs.get("name")).isEqualTo("hello world");
            assertThat(qs.get("path")).isEqualTo("/api/users");
        }

        @Test
        @DisplayName("should handle multi-value parameters")
        void shouldHandleMultiValue() {
            QueryString qs = QueryString.parse("color=red&color=blue");
            assertThat(qs.getAll("color")).containsExactly("red", "blue");
        }

        @Test
        @DisplayName("should skip empty pairs")
        void shouldSkipEmptyPairs() {
            QueryString qs = QueryString.parse("a=1&&b=2");
            assertThat(qs.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("of(Map)")
    class OfMap {

        @Test
        @DisplayName("should create from map")
        void shouldCreateFromMap() {
            QueryString qs = QueryString.of(Map.of("key", "value"));
            assertThat(qs.get("key")).isEqualTo("value");
            assertThat(qs.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("get(String)")
    class Get {

        @Test
        @DisplayName("should return first value")
        void shouldReturnFirstValue() {
            QueryString qs = QueryString.parse("a=1&a=2");
            assertThat(qs.get("a")).isEqualTo("1");
        }

        @Test
        @DisplayName("should return null for missing key")
        void shouldReturnNullForMissing() {
            QueryString qs = QueryString.parse("a=1");
            assertThat(qs.get("missing")).isNull();
        }
    }

    @Nested
    @DisplayName("get(String, String)")
    class GetWithDefault {

        @Test
        @DisplayName("should return value when present")
        void shouldReturnValue() {
            QueryString qs = QueryString.parse("a=1");
            assertThat(qs.get("a", "default")).isEqualTo("1");
        }

        @Test
        @DisplayName("should return default when missing")
        void shouldReturnDefault() {
            QueryString qs = QueryString.parse("a=1");
            assertThat(qs.get("b", "default")).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("getAll(String)")
    class GetAll {

        @Test
        @DisplayName("should return all values")
        void shouldReturnAllValues() {
            QueryString qs = QueryString.parse("x=1&x=2&x=3");
            assertThat(qs.getAll("x")).containsExactly("1", "2", "3");
        }

        @Test
        @DisplayName("should return empty list for missing key")
        void shouldReturnEmptyForMissing() {
            QueryString qs = QueryString.empty();
            assertThat(qs.getAll("missing")).isEmpty();
        }
    }

    @Nested
    @DisplayName("has(String)")
    class Has {

        @Test
        @DisplayName("should return true for existing key")
        void shouldReturnTrue() {
            QueryString qs = QueryString.parse("key=val");
            assertThat(qs.has("key")).isTrue();
        }

        @Test
        @DisplayName("should return false for missing key")
        void shouldReturnFalse() {
            QueryString qs = QueryString.empty();
            assertThat(qs.has("key")).isFalse();
        }
    }

    @Nested
    @DisplayName("names()")
    class Names {

        @Test
        @DisplayName("should return all parameter names")
        void shouldReturnNames() {
            QueryString qs = QueryString.parse("a=1&b=2&c=3");
            assertThat(qs.names()).containsExactlyInAnyOrder("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("toMap()")
    class ToMap {

        @Test
        @DisplayName("should return first-value map")
        void shouldReturnFirstValueMap() {
            QueryString qs = QueryString.parse("a=1&a=2&b=3");
            Map<String, String> map = qs.toMap();
            assertThat(map.get("a")).isEqualTo("1");
            assertThat(map.get("b")).isEqualTo("3");
        }
    }

    @Nested
    @DisplayName("toMultiMap()")
    class ToMultiMap {

        @Test
        @DisplayName("should return multi-value map")
        void shouldReturnMultiMap() {
            QueryString qs = QueryString.parse("a=1&a=2");
            Map<String, List<String>> map = qs.toMultiMap();
            assertThat(map.get("a")).containsExactly("1", "2");
        }
    }

    @Nested
    @DisplayName("with(String, String)")
    class With {

        @Test
        @DisplayName("should create new query string with added parameter")
        void shouldCreateWithAdded() {
            QueryString qs = QueryString.parse("a=1");
            QueryString updated = qs.with("b", "2");
            assertThat(updated.get("a")).isEqualTo("1");
            assertThat(updated.get("b")).isEqualTo("2");
            // original should be unchanged
            assertThat(qs.has("b")).isFalse();
        }
    }

    @Nested
    @DisplayName("without(String)")
    class Without {

        @Test
        @DisplayName("should create new query string without parameter")
        void shouldCreateWithout() {
            QueryString qs = QueryString.parse("a=1&b=2");
            QueryString updated = qs.without("a");
            assertThat(updated.has("a")).isFalse();
            assertThat(updated.get("b")).isEqualTo("2");
            // original should be unchanged
            assertThat(qs.has("a")).isTrue();
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTest {

        @Test
        @DisplayName("should encode parameters")
        void shouldEncodeParameters() {
            QueryString qs = QueryString.of(Map.of("name", "hello world"));
            assertThat(qs.toString()).isEqualTo("name=hello+world");
        }

        @Test
        @DisplayName("should return empty string for empty query")
        void shouldReturnEmptyForEmpty() {
            assertThat(QueryString.empty().toString()).isEmpty();
        }
    }

    @Nested
    @DisplayName("equals() and hashCode()")
    class EqualsHashCode {

        @Test
        @DisplayName("should be equal for same parameters")
        void shouldBeEqual() {
            QueryString qs1 = QueryString.parse("a=1&b=2");
            QueryString qs2 = QueryString.parse("a=1&b=2");
            assertThat(qs1).isEqualTo(qs2);
            assertThat(qs1.hashCode()).isEqualTo(qs2.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different parameters")
        void shouldNotBeEqual() {
            QueryString qs1 = QueryString.parse("a=1");
            QueryString qs2 = QueryString.parse("a=2");
            assertThat(qs1).isNotEqualTo(qs2);
        }
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTest {

        @Test
        @DisplayName("add should add parameter")
        void addShouldAdd() {
            QueryString qs = QueryString.builder().add("key", "value").build();
            assertThat(qs.get("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("add with null value should use empty string")
        void addNullValueShouldUseEmpty() {
            QueryString qs = QueryString.builder().add("key", null).build();
            assertThat(qs.get("key")).isEmpty();
        }

        @Test
        @DisplayName("addIfNotNull should skip null values")
        void addIfNotNullShouldSkip() {
            QueryString qs = QueryString.builder()
                    .addIfNotNull("a", null)
                    .addIfNotNull("b", "2")
                    .build();
            assertThat(qs.has("a")).isFalse();
            assertThat(qs.get("b")).isEqualTo("2");
        }

        @Test
        @DisplayName("addIfNotEmpty should skip null and empty values")
        void addIfNotEmptyShouldSkip() {
            QueryString qs = QueryString.builder()
                    .addIfNotEmpty("a", null)
                    .addIfNotEmpty("b", "")
                    .addIfNotEmpty("c", "3")
                    .build();
            assertThat(qs.has("a")).isFalse();
            assertThat(qs.has("b")).isFalse();
            assertThat(qs.get("c")).isEqualTo("3");
        }

        @Test
        @DisplayName("set should replace existing values")
        void setShouldReplace() {
            QueryString qs = QueryString.builder()
                    .add("key", "1")
                    .add("key", "2")
                    .set("key", "3")
                    .build();
            assertThat(qs.getAll("key")).containsExactly("3");
        }

        @Test
        @DisplayName("addAll should add all map entries")
        void addAllShouldAddAll() {
            QueryString qs = QueryString.builder()
                    .addAll(Map.of("a", "1", "b", "2"))
                    .build();
            assertThat(qs.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("remove should remove parameter")
        void removeShouldRemove() {
            QueryString qs = QueryString.builder()
                    .add("a", "1").add("b", "2")
                    .remove("a").build();
            assertThat(qs.has("a")).isFalse();
            assertThat(qs.has("b")).isTrue();
        }

        @Test
        @DisplayName("clear should remove all parameters")
        void clearShouldClear() {
            QueryString qs = QueryString.builder()
                    .add("a", "1").add("b", "2")
                    .clear().build();
            assertThat(qs.isEmpty()).isTrue();
        }
    }
}
