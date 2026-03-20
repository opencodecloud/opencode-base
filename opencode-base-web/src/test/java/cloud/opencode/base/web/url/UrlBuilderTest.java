package cloud.opencode.base.web.url;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UrlBuilder")
class UrlBuilderTest {

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create builder with default HTTPS scheme")
        void shouldCreateWithDefaults() {
            String url = UrlBuilder.create().host("example.com").build();
            assertThat(url).startsWith("https://");
        }
    }

    @Nested
    @DisplayName("from(String)")
    class FromString {

        @Test
        @DisplayName("should parse existing URL")
        void shouldParseExistingUrl() {
            String url = UrlBuilder.from("https://example.com:8080/path?q=1")
                    .build();
            assertThat(url).isEqualTo("https://example.com:8080/path?q=1");
        }

        @Test
        @DisplayName("should handle null URL")
        void shouldHandleNull() {
            UrlBuilder builder = UrlBuilder.from((String) null);
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("should handle empty URL")
        void shouldHandleEmpty() {
            UrlBuilder builder = UrlBuilder.from("");
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("should preserve fragment")
        void shouldPreserveFragment() {
            String url = UrlBuilder.from("https://example.com/path#section").build();
            assertThat(url).contains("#section");
        }
    }

    @Nested
    @DisplayName("scheme()")
    class Scheme {

        @Test
        @DisplayName("should set custom scheme")
        void shouldSetScheme() {
            String url = UrlBuilder.create().scheme("http").host("example.com").build();
            assertThat(url).startsWith("http://");
        }

        @Test
        @DisplayName("http() should set HTTP scheme")
        void httpShouldSetHttp() {
            String url = UrlBuilder.create().http().host("example.com").build();
            assertThat(url).startsWith("http://");
        }

        @Test
        @DisplayName("https() should set HTTPS scheme")
        void httpsShouldSetHttps() {
            String url = UrlBuilder.create().https().host("example.com").build();
            assertThat(url).startsWith("https://");
        }
    }

    @Nested
    @DisplayName("host()")
    class Host {

        @Test
        @DisplayName("should set host")
        void shouldSetHost() {
            String url = UrlBuilder.create().host("api.example.com").build();
            assertThat(url).contains("api.example.com");
        }
    }

    @Nested
    @DisplayName("port()")
    class Port {

        @Test
        @DisplayName("should include non-default port")
        void shouldIncludeNonDefaultPort() {
            String url = UrlBuilder.create().host("example.com").port(8080).build();
            assertThat(url).contains(":8080");
        }

        @Test
        @DisplayName("should omit default HTTPS port 443")
        void shouldOmitDefault443() {
            String url = UrlBuilder.create().https().host("example.com").port(443).build();
            assertThat(url).doesNotContain(":443");
        }

        @Test
        @DisplayName("should omit default HTTP port 80")
        void shouldOmitDefault80() {
            String url = UrlBuilder.create().http().host("example.com").port(80).build();
            assertThat(url).doesNotContain(":80");
        }
    }

    @Nested
    @DisplayName("path()")
    class PathTest {

        @Test
        @DisplayName("should set path")
        void shouldSetPath() {
            String url = UrlBuilder.create().host("example.com").path("/api/users").build();
            assertThat(url).isEqualTo("https://example.com/api/users");
        }

        @Test
        @DisplayName("should handle null path")
        void shouldHandleNullPath() {
            String url = UrlBuilder.create().host("example.com").path(null).build();
            assertThat(url).isEqualTo("https://example.com");
        }
    }

    @Nested
    @DisplayName("appendPath()")
    class AppendPath {

        @Test
        @DisplayName("should append path segment")
        void shouldAppendPath() {
            String url = UrlBuilder.create().host("example.com")
                    .path("/api").appendPath("users").build();
            assertThat(url).isEqualTo("https://example.com/api/users");
        }

        @Test
        @DisplayName("should avoid double slashes when appending")
        void shouldAvoidDoubleSlashes() {
            String url = UrlBuilder.create().host("example.com")
                    .path("/api/").appendPath("/users").build();
            assertThat(url).isEqualTo("https://example.com/api/users");
        }

        @Test
        @DisplayName("should handle null segment")
        void shouldHandleNullSegment() {
            String url = UrlBuilder.create().host("example.com")
                    .path("/api").appendPath(null).build();
            assertThat(url).isEqualTo("https://example.com/api");
        }

        @Test
        @DisplayName("should handle empty segment")
        void shouldHandleEmptySegment() {
            String url = UrlBuilder.create().host("example.com")
                    .path("/api").appendPath("").build();
            assertThat(url).isEqualTo("https://example.com/api");
        }
    }

    @Nested
    @DisplayName("pathParam()")
    class PathParam {

        @Test
        @DisplayName("should substitute path parameters")
        void shouldSubstitute() {
            String url = UrlBuilder.create().host("example.com")
                    .path("/users/{id}/posts/{postId}")
                    .pathParam("id", "123")
                    .pathParam("postId", "456")
                    .build();
            assertThat(url).isEqualTo("https://example.com/users/123/posts/456");
        }

        @Test
        @DisplayName("should reject path traversal in parameters")
        void shouldRejectPathTraversal() {
            assertThatThrownBy(() ->
                    UrlBuilder.create().host("example.com")
                            .path("/users/{id}")
                            .pathParam("id", "../etc/passwd")
                            .build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("dangerous");
        }

        @Test
        @DisplayName("should reject double slash in parameters")
        void shouldRejectDoubleSlash() {
            assertThatThrownBy(() ->
                    UrlBuilder.create().host("example.com")
                            .path("/users/{id}")
                            .pathParam("id", "a//b")
                            .build()
            ).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("pathParams()")
    class PathParams {

        @Test
        @DisplayName("should set multiple path parameters")
        void shouldSetMultiple() {
            String url = UrlBuilder.create().host("example.com")
                    .path("/users/{id}")
                    .pathParams(Map.of("id", "123"))
                    .build();
            assertThat(url).contains("/users/123");
        }
    }

    @Nested
    @DisplayName("queryParam()")
    class QueryParam {

        @Test
        @DisplayName("should add query parameter")
        void shouldAddParam() {
            String url = UrlBuilder.create().host("example.com")
                    .queryParam("page", "1").build();
            assertThat(url).isEqualTo("https://example.com?page=1");
        }
    }

    @Nested
    @DisplayName("queryParamIfNotNull()")
    class QueryParamIfNotNull {

        @Test
        @DisplayName("should add when value is not null")
        void shouldAddNonNull() {
            String url = UrlBuilder.create().host("example.com")
                    .queryParamIfNotNull("page", "1").build();
            assertThat(url).contains("page=1");
        }

        @Test
        @DisplayName("should skip when value is null")
        void shouldSkipNull() {
            String url = UrlBuilder.create().host("example.com")
                    .queryParamIfNotNull("page", null).build();
            assertThat(url).doesNotContain("page");
        }
    }

    @Nested
    @DisplayName("queryParamIfNotEmpty()")
    class QueryParamIfNotEmpty {

        @Test
        @DisplayName("should skip empty values")
        void shouldSkipEmpty() {
            String url = UrlBuilder.create().host("example.com")
                    .queryParamIfNotEmpty("q", "").build();
            assertThat(url).doesNotContain("q=");
        }
    }

    @Nested
    @DisplayName("queryParams()")
    class QueryParams {

        @Test
        @DisplayName("should add all parameters from map")
        void shouldAddAll() {
            String url = UrlBuilder.create().host("example.com")
                    .queryParams(Map.of("a", "1", "b", "2")).build();
            assertThat(url).contains("a=1").contains("b=2");
        }
    }

    @Nested
    @DisplayName("queryString()")
    class QueryStringTest {

        @Test
        @DisplayName("should add parameters from QueryString")
        void shouldAddFromQueryString() {
            QueryString qs = QueryString.of(Map.of("key", "val"));
            String url = UrlBuilder.create().host("example.com")
                    .queryString(qs).build();
            assertThat(url).contains("key=val");
        }
    }

    @Nested
    @DisplayName("fragment()")
    class Fragment {

        @Test
        @DisplayName("should add fragment")
        void shouldAddFragment() {
            String url = UrlBuilder.create().host("example.com")
                    .fragment("section1").build();
            assertThat(url).endsWith("#section1");
        }

        @Test
        @DisplayName("should not add empty fragment")
        void shouldNotAddEmptyFragment() {
            String url = UrlBuilder.create().host("example.com")
                    .fragment("").build();
            assertThat(url).doesNotContain("#");
        }
    }

    @Nested
    @DisplayName("build()")
    class Build {

        @Test
        @DisplayName("should build complete URL")
        void shouldBuildCompleteUrl() {
            String url = UrlBuilder.create()
                    .https().host("api.example.com").port(8080)
                    .path("/users/{id}")
                    .pathParam("id", "123")
                    .queryParam("fields", "name,email")
                    .fragment("profile")
                    .build();
            assertThat(url).isEqualTo("https://api.example.com:8080/users/123?fields=name%2Cemail#profile");
        }

        @Test
        @DisplayName("should build path-only URL when no host")
        void shouldBuildPathOnly() {
            String url = UrlBuilder.create().path("/api/test").build();
            assertThat(url).isEqualTo("/api/test");
        }
    }

    @Nested
    @DisplayName("buildUri()")
    class BuildUri {

        @Test
        @DisplayName("should return URI")
        void shouldReturnUri() {
            java.net.URI uri = UrlBuilder.create().host("example.com").path("/test").buildUri();
            assertThat(uri.getHost()).isEqualTo("example.com");
            assertThat(uri.getPath()).isEqualTo("/test");
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTest {

        @Test
        @DisplayName("should delegate to build()")
        void shouldDelegateToBuild() {
            UrlBuilder builder = UrlBuilder.create().host("example.com");
            assertThat(builder.toString()).isEqualTo(builder.build());
        }
    }
}
