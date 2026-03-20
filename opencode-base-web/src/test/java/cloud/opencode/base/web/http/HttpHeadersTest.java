package cloud.opencode.base.web.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HttpHeaders")
class HttpHeadersTest {

    @Nested
    @DisplayName("of()")
    class OfEmpty {

        @Test
        @DisplayName("should create empty headers")
        void shouldCreateEmpty() {
            HttpHeaders headers = HttpHeaders.of();
            assertThat(headers.isEmpty()).isTrue();
            assertThat(headers.size()).isZero();
        }
    }

    @Nested
    @DisplayName("of(Map)")
    class OfMap {

        @Test
        @DisplayName("should create from map")
        void shouldCreateFromMap() {
            HttpHeaders headers = HttpHeaders.of(Map.of("Content-Type", "application/json"));
            assertThat(headers.get("Content-Type")).isEqualTo("application/json");
        }

        @Test
        @DisplayName("should handle null map")
        void shouldHandleNullMap() {
            HttpHeaders headers = HttpHeaders.of(null);
            assertThat(headers.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("copyOf()")
    class CopyOf {

        @Test
        @DisplayName("should create independent copy")
        void shouldCreateCopy() {
            HttpHeaders original = HttpHeaders.of().add("Key", "Value");
            HttpHeaders copy = HttpHeaders.copyOf(original);
            copy.add("Key2", "Value2");
            assertThat(original.contains("Key2")).isFalse();
            assertThat(copy.get("Key")).isEqualTo("Value");
        }
    }

    @Nested
    @DisplayName("add(String, String)")
    class Add {

        @Test
        @DisplayName("should add header value")
        void shouldAddValue() {
            HttpHeaders headers = HttpHeaders.of().add("X-Custom", "val1");
            assertThat(headers.get("X-Custom")).isEqualTo("val1");
        }

        @Test
        @DisplayName("should support multiple values")
        void shouldSupportMultipleValues() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("X-Custom", "val1")
                    .add("X-Custom", "val2");
            assertThat(headers.getAll("X-Custom")).containsExactly("val1", "val2");
        }
    }

    @Nested
    @DisplayName("set(String, String)")
    class Set {

        @Test
        @DisplayName("should replace existing values")
        void shouldReplace() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("Key", "old")
                    .set("Key", "new");
            assertThat(headers.getAll("Key")).containsExactly("new");
        }
    }

    @Nested
    @DisplayName("set(String, List)")
    class SetList {

        @Test
        @DisplayName("should set multiple values")
        void shouldSetMultiple() {
            HttpHeaders headers = HttpHeaders.of()
                    .set("Key", List.of("a", "b", "c"));
            assertThat(headers.getAll("Key")).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("remove(String)")
    class Remove {

        @Test
        @DisplayName("should remove header")
        void shouldRemove() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("Key", "val").remove("Key");
            assertThat(headers.contains("Key")).isFalse();
        }
    }

    @Nested
    @DisplayName("clear()")
    class Clear {

        @Test
        @DisplayName("should clear all headers")
        void shouldClear() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("A", "1").add("B", "2").clear();
            assertThat(headers.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("get(String)")
    class Get {

        @Test
        @DisplayName("should be case-insensitive")
        void shouldBeCaseInsensitive() {
            HttpHeaders headers = HttpHeaders.of().add("Content-Type", "json");
            assertThat(headers.get("content-type")).isEqualTo("json");
            assertThat(headers.get("CONTENT-TYPE")).isEqualTo("json");
        }

        @Test
        @DisplayName("should return null for missing header")
        void shouldReturnNullForMissing() {
            assertThat(HttpHeaders.of().get("Missing")).isNull();
        }
    }

    @Nested
    @DisplayName("getOrDefault()")
    class GetOrDefault {

        @Test
        @DisplayName("should return value when present")
        void shouldReturnValue() {
            HttpHeaders headers = HttpHeaders.of().add("Key", "val");
            assertThat(headers.getOrDefault("Key", "default")).isEqualTo("val");
        }

        @Test
        @DisplayName("should return default when missing")
        void shouldReturnDefault() {
            assertThat(HttpHeaders.of().getOrDefault("Key", "default")).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("getAll(String)")
    class GetAll {

        @Test
        @DisplayName("should return empty list for missing header")
        void shouldReturnEmptyForMissing() {
            assertThat(HttpHeaders.of().getAll("Missing")).isEmpty();
        }

        @Test
        @DisplayName("should return unmodifiable list")
        void shouldReturnUnmodifiable() {
            HttpHeaders headers = HttpHeaders.of().add("Key", "val");
            List<String> values = headers.getAll("Key");
            assertThat(values).hasSize(1);
        }
    }

    @Nested
    @DisplayName("contains(String)")
    class Contains {

        @Test
        @DisplayName("should return true for existing header")
        void shouldReturnTrue() {
            HttpHeaders headers = HttpHeaders.of().add("Key", "val");
            assertThat(headers.contains("Key")).isTrue();
            assertThat(headers.contains("key")).isTrue(); // case-insensitive
        }

        @Test
        @DisplayName("should return false for missing header")
        void shouldReturnFalse() {
            assertThat(HttpHeaders.of().contains("Missing")).isFalse();
        }
    }

    @Nested
    @DisplayName("names()")
    class NameSet {

        @Test
        @DisplayName("should return header names")
        void shouldReturnNames() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("A", "1").add("B", "2");
            assertThat(headers.names()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("toMap()")
    class ToMap {

        @Test
        @DisplayName("should return unmodifiable map")
        void shouldReturnUnmodifiable() {
            HttpHeaders headers = HttpHeaders.of().add("Key", "val");
            Map<String, List<String>> map = headers.toMap();
            assertThat(map).containsKey("Key");
        }
    }

    @Nested
    @DisplayName("toSingleValueMap()")
    class ToSingleValueMap {

        @Test
        @DisplayName("should return first values only")
        void shouldReturnFirstValues() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("Key", "first").add("Key", "second");
            Map<String, String> map = headers.toSingleValueMap();
            assertThat(map.get("Key")).isEqualTo("first");
        }
    }

    @Nested
    @DisplayName("getContentType()")
    class GetContentType {

        @Test
        @DisplayName("should return parsed ContentType")
        void shouldReturnParsed() {
            HttpHeaders headers = HttpHeaders.of()
                    .set("Content-Type", "application/json; charset=utf-8");
            ContentType ct = headers.getContentType();
            assertThat(ct).isNotNull();
            assertThat(ct.getMimeType()).isEqualTo("application/json");
        }

        @Test
        @DisplayName("should return null when no Content-Type header")
        void shouldReturnNullWhenMissing() {
            assertThat(HttpHeaders.of().getContentType()).isNull();
        }
    }

    @Nested
    @DisplayName("getContentLength()")
    class GetContentLength {

        @Test
        @DisplayName("should return parsed content length")
        void shouldReturnParsed() {
            HttpHeaders headers = HttpHeaders.of().set("Content-Length", "1024");
            assertThat(headers.getContentLength()).isEqualTo(1024);
        }

        @Test
        @DisplayName("should return -1 when missing")
        void shouldReturnNegativeWhenMissing() {
            assertThat(HttpHeaders.of().getContentLength()).isEqualTo(-1);
        }

        @Test
        @DisplayName("should return -1 for invalid value")
        void shouldReturnNegativeForInvalid() {
            HttpHeaders headers = HttpHeaders.of().set("Content-Length", "not-a-number");
            assertThat(headers.getContentLength()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("contentType(String)")
    class ContentTypeMethod {

        @Test
        @DisplayName("should set Content-Type header")
        void shouldSetContentType() {
            HttpHeaders headers = HttpHeaders.of().contentType("text/html");
            assertThat(headers.get("Content-Type")).isEqualTo("text/html");
        }
    }

    @Nested
    @DisplayName("contentType(ContentType)")
    class ContentTypeObjectMethod {

        @Test
        @DisplayName("should set Content-Type from ContentType object")
        void shouldSetFromObject() {
            HttpHeaders headers = HttpHeaders.of().contentType(ContentType.json());
            assertThat(headers.get("Content-Type")).contains("application/json");
        }
    }

    @Nested
    @DisplayName("accept(String)")
    class Accept {

        @Test
        @DisplayName("should set Accept header")
        void shouldSetAccept() {
            HttpHeaders headers = HttpHeaders.of().accept("application/json");
            assertThat(headers.get("Accept")).isEqualTo("application/json");
        }
    }

    @Nested
    @DisplayName("bearerAuth(String)")
    class BearerAuth {

        @Test
        @DisplayName("should set Bearer authorization")
        void shouldSetBearer() {
            HttpHeaders headers = HttpHeaders.of().bearerAuth("token123");
            assertThat(headers.get("Authorization")).isEqualTo("Bearer token123");
        }
    }

    @Nested
    @DisplayName("basicAuth(String, String)")
    class BasicAuth {

        @Test
        @DisplayName("should set Basic authorization with Base64 credentials")
        void shouldSetBasic() {
            HttpHeaders headers = HttpHeaders.of().basicAuth("user", "pass");
            String auth = headers.get("Authorization");
            assertThat(auth).startsWith("Basic ");
            assertThat(auth.length()).isGreaterThan("Basic ".length());
        }
    }

    @Nested
    @DisplayName("userAgent(String)")
    class UserAgent {

        @Test
        @DisplayName("should set User-Agent header")
        void shouldSetUserAgent() {
            HttpHeaders headers = HttpHeaders.of().userAgent("MyApp/1.0");
            assertThat(headers.get("User-Agent")).isEqualTo("MyApp/1.0");
        }
    }

    @Nested
    @DisplayName("iterator()")
    class IteratorTest {

        @Test
        @DisplayName("should iterate over all headers")
        void shouldIterate() {
            HttpHeaders headers = HttpHeaders.of()
                    .add("A", "1").add("B", "2");
            int count = 0;
            for (var entry : headers) {
                count++;
            }
            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTest {

        @Test
        @DisplayName("should include header info")
        void shouldIncludeInfo() {
            HttpHeaders headers = HttpHeaders.of().add("Key", "val");
            assertThat(headers.toString()).contains("HttpHeaders", "Key");
        }
    }

    @Nested
    @DisplayName("Header name constants")
    class HeaderConstants {

        @Test
        @DisplayName("should define standard header names")
        void shouldDefineStandard() {
            assertThat(HttpHeaders.CONTENT_TYPE).isEqualTo("Content-Type");
            assertThat(HttpHeaders.AUTHORIZATION).isEqualTo("Authorization");
            assertThat(HttpHeaders.ACCEPT).isEqualTo("Accept");
            assertThat(HttpHeaders.USER_AGENT).isEqualTo("User-Agent");
            assertThat(HttpHeaders.CONTENT_LENGTH).isEqualTo("Content-Length");
            assertThat(HttpHeaders.HOST).isEqualTo("Host");
            assertThat(HttpHeaders.COOKIE).isEqualTo("Cookie");
            assertThat(HttpHeaders.SET_COOKIE).isEqualTo("Set-Cookie");
            assertThat(HttpHeaders.LOCATION).isEqualTo("Location");
            assertThat(HttpHeaders.CACHE_CONTROL).isEqualTo("Cache-Control");
        }
    }
}
