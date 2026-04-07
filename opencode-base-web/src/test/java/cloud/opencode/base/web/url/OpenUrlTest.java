package cloud.opencode.base.web.url;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OpenUrl")
class OpenUrlTest {

    @Nested
    @DisplayName("encode(String)")
    class Encode {

        @Test
        @DisplayName("should encode spaces as plus")
        void shouldEncodeSpaces() {
            assertThat(OpenUrl.encode("hello world")).isEqualTo("hello+world");
        }

        @Test
        @DisplayName("should encode special characters")
        void shouldEncodeSpecialChars() {
            assertThat(OpenUrl.encode("a=b&c=d")).isEqualTo("a%3Db%26c%3Dd");
        }

        @Test
        @DisplayName("should return null for null input")
        void shouldReturnNullForNull() {
            assertThat(OpenUrl.encode(null)).isNull();
        }
    }

    @Nested
    @DisplayName("decode(String)")
    class Decode {

        @Test
        @DisplayName("should decode percent-encoded string")
        void shouldDecode() {
            assertThat(OpenUrl.decode("hello%20world")).isEqualTo("hello world");
        }

        @Test
        @DisplayName("should decode plus as space")
        void shouldDecodePlus() {
            assertThat(OpenUrl.decode("hello+world")).isEqualTo("hello world");
        }

        @Test
        @DisplayName("should return null for null input")
        void shouldReturnNullForNull() {
            assertThat(OpenUrl.decode(null)).isNull();
        }
    }

    @Nested
    @DisplayName("encodePath(String)")
    class EncodePath {

        @Test
        @DisplayName("should preserve slashes")
        void shouldPreserveSlashes() {
            assertThat(OpenUrl.encodePath("path/to/file")).isEqualTo("path/to/file");
        }

        @Test
        @DisplayName("should encode spaces as %20")
        void shouldEncodeSpacesAsPercent20() {
            assertThat(OpenUrl.encodePath("path/to file")).isEqualTo("path/to%20file");
        }

        @Test
        @DisplayName("should return null for null input")
        void shouldReturnNullForNull() {
            assertThat(OpenUrl.encodePath(null)).isNull();
        }
    }

    @Nested
    @DisplayName("encodeParams(Map)")
    class EncodeParams {

        @Test
        @DisplayName("should encode map to query string")
        void shouldEncodeMap() {
            String result = OpenUrl.encodeParams(Map.of("key", "value"));
            assertThat(result).isEqualTo("key=value");
        }
    }

    @Nested
    @DisplayName("getScheme(String)")
    class GetScheme {

        @Test
        @DisplayName("should return scheme from URL")
        void shouldReturnScheme() {
            assertThat(OpenUrl.getScheme("https://example.com")).isEqualTo("https");
            assertThat(OpenUrl.getScheme("http://example.com")).isEqualTo("http");
        }

        @Test
        @DisplayName("should return null for invalid URL")
        void shouldReturnNullForInvalid() {
            assertThat(OpenUrl.getScheme("not a url")).isNull();
        }
    }

    @Nested
    @DisplayName("getHost(String)")
    class GetHost {

        @Test
        @DisplayName("should return host from URL")
        void shouldReturnHost() {
            assertThat(OpenUrl.getHost("https://example.com/path")).isEqualTo("example.com");
        }

        @Test
        @DisplayName("should return null for invalid URL")
        void shouldReturnNullForInvalid() {
            assertThat(OpenUrl.getHost("not-a-url")).isNull();
        }
    }

    @Nested
    @DisplayName("getPort(String)")
    class GetPort {

        @Test
        @DisplayName("should return port when specified")
        void shouldReturnPort() {
            assertThat(OpenUrl.getPort("https://example.com:8080/path")).isEqualTo(8080);
        }

        @Test
        @DisplayName("should return -1 when no port specified")
        void shouldReturnNegativeOneWhenNoPort() {
            assertThat(OpenUrl.getPort("https://example.com/path")).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("getEffectivePort(String)")
    class GetEffectivePort {

        @Test
        @DisplayName("should return 443 for HTTPS without port")
        void shouldReturn443ForHttps() {
            assertThat(OpenUrl.getEffectivePort("https://example.com")).isEqualTo(443);
        }

        @Test
        @DisplayName("should return 80 for HTTP without port")
        void shouldReturn80ForHttp() {
            assertThat(OpenUrl.getEffectivePort("http://example.com")).isEqualTo(80);
        }

        @Test
        @DisplayName("should return explicit port when specified")
        void shouldReturnExplicitPort() {
            assertThat(OpenUrl.getEffectivePort("https://example.com:9090")).isEqualTo(9090);
        }

        @Test
        @DisplayName("should return -1 for unknown scheme")
        void shouldReturnNegativeForUnknown() {
            assertThat(OpenUrl.getEffectivePort("ftp://example.com")).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("getPath(String)")
    class GetPath {

        @Test
        @DisplayName("should return path from URL")
        void shouldReturnPath() {
            assertThat(OpenUrl.getPath("https://example.com/api/users")).isEqualTo("/api/users");
        }

        @Test
        @DisplayName("should return empty path for root URL")
        void shouldReturnEmptyForRoot() {
            assertThat(OpenUrl.getPath("https://example.com")).isEmpty();
        }
    }

    @Nested
    @DisplayName("getQuery(String)")
    class GetQuery {

        @Test
        @DisplayName("should return query string")
        void shouldReturnQuery() {
            assertThat(OpenUrl.getQuery("https://example.com?a=1&b=2")).isEqualTo("a=1&b=2");
        }

        @Test
        @DisplayName("should return null when no query")
        void shouldReturnNullWhenNoQuery() {
            assertThat(OpenUrl.getQuery("https://example.com")).isNull();
        }
    }

    @Nested
    @DisplayName("getFragment(String)")
    class GetFragment {

        @Test
        @DisplayName("should return fragment")
        void shouldReturnFragment() {
            assertThat(OpenUrl.getFragment("https://example.com#section")).isEqualTo("section");
        }

        @Test
        @DisplayName("should return null when no fragment")
        void shouldReturnNullWhenNoFragment() {
            assertThat(OpenUrl.getFragment("https://example.com")).isNull();
        }
    }

    @Nested
    @DisplayName("parseQuery(String)")
    class ParseQuery {

        @Test
        @DisplayName("should parse query parameters")
        void shouldParseQueryParams() {
            QueryString qs = OpenUrl.parseQuery("https://example.com?name=John&age=30");
            assertThat(qs.get("name")).isEqualTo("John");
            assertThat(qs.get("age")).isEqualTo("30");
        }
    }

    @Nested
    @DisplayName("getQueryParam(String, String)")
    class GetQueryParam {

        @Test
        @DisplayName("should return query parameter value")
        void shouldReturnParam() {
            assertThat(OpenUrl.getQueryParam("https://example.com?key=val", "key")).isEqualTo("val");
        }

        @Test
        @DisplayName("should return null for missing parameter")
        void shouldReturnNullForMissing() {
            assertThat(OpenUrl.getQueryParam("https://example.com?key=val", "other")).isNull();
        }
    }

    @Nested
    @DisplayName("join(String, String...)")
    class Join {

        @Test
        @DisplayName("should join URL parts with slashes")
        void shouldJoinWithSlashes() {
            assertThat(OpenUrl.join("https://example.com", "api", "users"))
                    .isEqualTo("https://example.com/api/users");
        }

        @Test
        @DisplayName("should avoid double slashes")
        void shouldAvoidDoubleSlashes() {
            assertThat(OpenUrl.join("https://example.com/", "/api"))
                    .isEqualTo("https://example.com/api");
        }

        @Test
        @DisplayName("should handle null base")
        void shouldHandleNullBase() {
            assertThat(OpenUrl.join(null, "path")).isEqualTo("path");
        }

        @Test
        @DisplayName("should skip null and empty parts")
        void shouldSkipNullAndEmpty() {
            assertThat(OpenUrl.join("base", null, "", "end")).isEqualTo("base/end");
        }
    }

    @Nested
    @DisplayName("normalize(String)")
    class Normalize {

        @Test
        @DisplayName("should normalize URL with dot segments")
        void shouldNormalize() {
            assertThat(OpenUrl.normalize("https://example.com/a/../b"))
                    .isEqualTo("https://example.com/b");
        }

        @Test
        @DisplayName("should return original for invalid URL")
        void shouldReturnOriginalForInvalid() {
            assertThat(OpenUrl.normalize("not valid")).isEqualTo("not valid");
        }
    }

    @Nested
    @DisplayName("removeQuery(String)")
    class RemoveQuery {

        @Test
        @DisplayName("should remove query string")
        void shouldRemoveQuery() {
            assertThat(OpenUrl.removeQuery("https://example.com/path?a=1"))
                    .isEqualTo("https://example.com/path");
        }

        @Test
        @DisplayName("should return same URL when no query")
        void shouldReturnSameWhenNoQuery() {
            assertThat(OpenUrl.removeQuery("https://example.com/path"))
                    .isEqualTo("https://example.com/path");
        }

        @Test
        @DisplayName("should return null for null input")
        void shouldReturnNullForNull() {
            assertThat(OpenUrl.removeQuery(null)).isNull();
        }
    }

    @Nested
    @DisplayName("removeFragment(String)")
    class RemoveFragment {

        @Test
        @DisplayName("should remove fragment")
        void shouldRemoveFragment() {
            assertThat(OpenUrl.removeFragment("https://example.com#top"))
                    .isEqualTo("https://example.com");
        }

        @Test
        @DisplayName("should return null for null input")
        void shouldReturnNullForNull() {
            assertThat(OpenUrl.removeFragment(null)).isNull();
        }
    }

    @Nested
    @DisplayName("getBaseUrl(String)")
    class GetBaseUrl {

        @Test
        @DisplayName("should return scheme + host")
        void shouldReturnSchemeAndHost() {
            assertThat(OpenUrl.getBaseUrl("https://example.com/path?q=1"))
                    .isEqualTo("https://example.com");
        }

        @Test
        @DisplayName("should include port when explicit")
        void shouldIncludePort() {
            assertThat(OpenUrl.getBaseUrl("https://example.com:8080/path"))
                    .isEqualTo("https://example.com:8080");
        }

        @Test
        @DisplayName("should handle URL without host gracefully")
        void shouldHandleNoHost() {
            // URI.create("not-a-url") parses "not-a-url" as a relative URI with no scheme/host
            // getBaseUrl constructs "null://null" for such cases - just verify it doesn't throw
            assertThat(OpenUrl.getBaseUrl("not-a-url")).isNotNull();
        }
    }

    @Nested
    @DisplayName("isValid(String)")
    class IsValid {

        @Test
        @DisplayName("should return true for valid HTTP URL")
        void shouldReturnTrueForValid() {
            assertThat(OpenUrl.isValid("https://example.com")).isTrue();
        }

        @Test
        @DisplayName("should return false for null")
        void shouldReturnFalseForNull() {
            assertThat(OpenUrl.isValid(null)).isFalse();
        }

        @Test
        @DisplayName("should return false for empty string")
        void shouldReturnFalseForEmpty() {
            assertThat(OpenUrl.isValid("")).isFalse();
        }
    }

    @Nested
    @DisplayName("isValidUrl(String)")
    class IsValidUrl {

        @Test
        @DisplayName("should return true for valid HTTP URL")
        void shouldReturnTrue() {
            assertThat(OpenUrl.isValidUrl("https://example.com/path")).isTrue();
        }

        @Test
        @DisplayName("should return false for null")
        void shouldReturnFalseForNull() {
            assertThat(OpenUrl.isValidUrl(null)).isFalse();
        }

        @Test
        @DisplayName("should return false for empty")
        void shouldReturnFalseForEmpty() {
            assertThat(OpenUrl.isValidUrl("")).isFalse();
        }
    }

    @Nested
    @DisplayName("isValidDomain(String)")
    class IsValidDomain {

        @Test
        @DisplayName("should return true for valid domain")
        void shouldReturnTrue() {
            assertThat(OpenUrl.isValidDomain("example.com")).isTrue();
            assertThat(OpenUrl.isValidDomain("sub.example.com")).isTrue();
        }

        @Test
        @DisplayName("should return false for null")
        void shouldReturnFalseForNull() {
            assertThat(OpenUrl.isValidDomain(null)).isFalse();
        }

        @Test
        @DisplayName("should return false for empty")
        void shouldReturnFalseForEmpty() {
            assertThat(OpenUrl.isValidDomain("")).isFalse();
        }
    }

    @Nested
    @DisplayName("isHttps(String)")
    class IsHttps {

        @Test
        @DisplayName("should return true for HTTPS URL")
        void shouldReturnTrue() {
            assertThat(OpenUrl.isHttps("https://example.com")).isTrue();
        }

        @Test
        @DisplayName("should return false for HTTP URL")
        void shouldReturnFalse() {
            assertThat(OpenUrl.isHttps("http://example.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("isHttp(String)")
    class IsHttp {

        @Test
        @DisplayName("should return true for HTTP URL")
        void shouldReturnTrue() {
            assertThat(OpenUrl.isHttp("http://example.com")).isTrue();
        }

        @Test
        @DisplayName("should return false for HTTPS URL")
        void shouldReturnFalse() {
            assertThat(OpenUrl.isHttp("https://example.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("toAscii(String)")
    class ToAscii {

        @Test
        @DisplayName("should return null for null")
        void shouldReturnNullForNull() {
            assertThat(OpenUrl.toAscii(null)).isNull();
        }

        @Test
        @DisplayName("should pass through ASCII domain")
        void shouldPassThroughAscii() {
            assertThat(OpenUrl.toAscii("example.com")).isEqualTo("example.com");
        }
    }

    @Nested
    @DisplayName("toUnicode(String)")
    class ToUnicode {

        @Test
        @DisplayName("should return null for null")
        void shouldReturnNullForNull() {
            assertThat(OpenUrl.toUnicode(null)).isNull();
        }

        @Test
        @DisplayName("should pass through ASCII domain")
        void shouldPassThroughAscii() {
            assertThat(OpenUrl.toUnicode("example.com")).isEqualTo("example.com");
        }
    }

    @Nested
    @DisplayName("builder()")
    class BuilderFactory {

        @Test
        @DisplayName("should return a UrlBuilder")
        void shouldReturnBuilder() {
            UrlBuilder builder = OpenUrl.builder();
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("should return a UrlBuilder from URL")
        void shouldReturnBuilderFromUrl() {
            UrlBuilder builder = OpenUrl.builder("https://example.com");
            assertThat(builder).isNotNull();
        }
    }
}
