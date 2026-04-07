package cloud.opencode.base.web.cors;

import cloud.opencode.base.web.http.HttpHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link CorsConfig}.
 */
@DisplayName("CorsConfig")
class CorsConfigTest {

    // ==================== Builder Tests ====================

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("should build with all fields")
        void shouldBuildWithAllFields() {
            CorsConfig config = CorsConfig.builder()
                    .allowOrigin("https://example.com")
                    .allowMethod("GET", "POST")
                    .allowHeader("Authorization", "Content-Type")
                    .exposeHeader("X-Custom")
                    .allowCredentials(true)
                    .maxAge(3600)
                    .build();

            assertThat(config.allowedOrigins()).containsExactly("https://example.com");
            assertThat(config.allowedMethods()).containsExactlyInAnyOrder("GET", "POST");
            assertThat(config.allowedHeaders()).containsExactlyInAnyOrder("Authorization", "Content-Type");
            assertThat(config.exposedHeaders()).containsExactly("X-Custom");
            assertThat(config.allowCredentials()).isTrue();
            assertThat(config.maxAge()).isEqualTo(3600);
        }

        @Test
        @DisplayName("should build with empty defaults")
        void shouldBuildWithEmptyDefaults() {
            CorsConfig config = CorsConfig.builder().build();

            assertThat(config.allowedOrigins()).isEmpty();
            assertThat(config.allowedMethods()).isEmpty();
            assertThat(config.allowedHeaders()).isEmpty();
            assertThat(config.exposedHeaders()).isEmpty();
            assertThat(config.allowCredentials()).isFalse();
            assertThat(config.maxAge()).isZero();
        }

        @Test
        @DisplayName("should reject credentials with wildcard origin")
        void shouldRejectCredentialsWithWildcard() {
            assertThatThrownBy(() -> CorsConfig.builder()
                    .allowOrigin("*")
                    .allowCredentials(true)
                    .build()
            ).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("allowCredentials")
                    .hasMessageContaining("*");
        }

        @Test
        @DisplayName("should accept credentials with specific origins")
        void shouldAcceptCredentialsWithSpecificOrigins() {
            CorsConfig config = CorsConfig.builder()
                    .allowOrigin("https://example.com")
                    .allowCredentials(true)
                    .build();

            assertThat(config.allowCredentials()).isTrue();
        }

        @Test
        @DisplayName("should reject null origin")
        void shouldRejectNullOrigin() {
            assertThatThrownBy(() -> CorsConfig.builder().allowOrigin((String[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject null method")
        void shouldRejectNullMethod() {
            assertThatThrownBy(() -> CorsConfig.builder().allowMethod((String[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject null header")
        void shouldRejectNullHeader() {
            assertThatThrownBy(() -> CorsConfig.builder().allowHeader((String[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject null expose header")
        void shouldRejectNullExposeHeader() {
            assertThatThrownBy(() -> CorsConfig.builder().exposeHeader((String[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should accumulate multiple origins")
        void shouldAccumulateMultipleOrigins() {
            CorsConfig config = CorsConfig.builder()
                    .allowOrigin("https://a.com")
                    .allowOrigin("https://b.com")
                    .build();

            assertThat(config.allowedOrigins()).containsExactly("https://a.com", "https://b.com");
        }

        @Test
        @DisplayName("should accept varargs origins in single call")
        void shouldAcceptVarargsOrigins() {
            CorsConfig config = CorsConfig.builder()
                    .allowOrigin("https://a.com", "https://b.com", "https://c.com")
                    .build();

            assertThat(config.allowedOrigins()).hasSize(3);
        }
    }

    // ==================== Preset Tests ====================

    @Nested
    @DisplayName("Presets")
    class PresetTests {

        @Test
        @DisplayName("allowAll should allow wildcard origin")
        void allowAllShouldAllowWildcard() {
            CorsConfig config = CorsConfig.allowAll();

            assertThat(config.allowedOrigins()).contains("*");
            assertThat(config.allowCredentials()).isFalse();
            assertThat(config.allowedMethods()).contains("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS");
            assertThat(config.allowedHeaders()).contains("*");
            assertThat(config.maxAge()).isEqualTo(86400);
        }

        @Test
        @DisplayName("restrictive should allow only specified origins with credentials")
        void restrictiveShouldAllowSpecificOrigins() {
            CorsConfig config = CorsConfig.restrictive("https://example.com", "https://api.example.com");

            assertThat(config.allowedOrigins()).containsExactly("https://example.com", "https://api.example.com");
            assertThat(config.allowCredentials()).isTrue();
            assertThat(config.allowedMethods()).contains("GET", "POST", "PUT", "DELETE");
            assertThat(config.allowedHeaders()).contains("Authorization", "Content-Type", "Accept");
            assertThat(config.maxAge()).isEqualTo(3600);
        }

        @Test
        @DisplayName("restrictive should reject null origins")
        void restrictiveShouldRejectNull() {
            assertThatThrownBy(() -> CorsConfig.restrictive((String[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("restrictive should reject empty origins")
        void restrictiveShouldRejectEmpty() {
            assertThatThrownBy(CorsConfig::restrictive)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("At least one origin");
        }
    }

    // ==================== Query Method Tests ====================

    @Nested
    @DisplayName("Query Methods")
    class QueryMethodTests {

        @Test
        @DisplayName("isOriginAllowed should match exact origin")
        void isOriginAllowedExact() {
            CorsConfig config = CorsConfig.builder()
                    .allowOrigin("https://example.com")
                    .build();

            assertThat(config.isOriginAllowed("https://example.com")).isTrue();
            assertThat(config.isOriginAllowed("https://other.com")).isFalse();
        }

        @Test
        @DisplayName("isOriginAllowed should match wildcard")
        void isOriginAllowedWildcard() {
            CorsConfig config = CorsConfig.allowAll();

            assertThat(config.isOriginAllowed("https://anything.com")).isTrue();
        }

        @Test
        @DisplayName("isOriginAllowed should return false for null")
        void isOriginAllowedNull() {
            CorsConfig config = CorsConfig.allowAll();
            assertThat(config.isOriginAllowed(null)).isFalse();
        }

        @Test
        @DisplayName("isMethodAllowed should match case-insensitively")
        void isMethodAllowedCaseInsensitive() {
            CorsConfig config = CorsConfig.builder()
                    .allowMethod("GET", "POST")
                    .build();

            assertThat(config.isMethodAllowed("GET")).isTrue();
            assertThat(config.isMethodAllowed("get")).isTrue();
            assertThat(config.isMethodAllowed("Post")).isTrue();
            assertThat(config.isMethodAllowed("DELETE")).isFalse();
        }

        @Test
        @DisplayName("isMethodAllowed should return false for null")
        void isMethodAllowedNull() {
            CorsConfig config = CorsConfig.allowAll();
            assertThat(config.isMethodAllowed(null)).isFalse();
        }

        @Test
        @DisplayName("isHeaderAllowed should match case-insensitively")
        void isHeaderAllowedCaseInsensitive() {
            CorsConfig config = CorsConfig.builder()
                    .allowHeader("Authorization", "Content-Type")
                    .build();

            assertThat(config.isHeaderAllowed("Authorization")).isTrue();
            assertThat(config.isHeaderAllowed("authorization")).isTrue();
            assertThat(config.isHeaderAllowed("content-type")).isTrue();
            assertThat(config.isHeaderAllowed("X-Custom")).isFalse();
        }

        @Test
        @DisplayName("isHeaderAllowed should match wildcard")
        void isHeaderAllowedWildcard() {
            CorsConfig config = CorsConfig.builder()
                    .allowHeader("*")
                    .build();

            assertThat(config.isHeaderAllowed("Anything")).isTrue();
        }

        @Test
        @DisplayName("isHeaderAllowed should return false for null")
        void isHeaderAllowedNull() {
            CorsConfig config = CorsConfig.allowAll();
            assertThat(config.isHeaderAllowed(null)).isFalse();
        }

        @Test
        @DisplayName("allowsAll should check for wildcard origin")
        void allowsAllCheck() {
            assertThat(CorsConfig.allowAll().allowsAll()).isTrue();
            assertThat(CorsConfig.restrictive("https://example.com").allowsAll()).isFalse();
        }
    }

    // ==================== Header Generation Tests ====================

    @Nested
    @DisplayName("Header Generation")
    class HeaderGenerationTests {

        @Test
        @DisplayName("toHeaders should return empty map for disallowed origin")
        void toHeadersDisallowedOrigin() {
            CorsConfig config = CorsConfig.restrictive("https://example.com");

            Map<String, String> headers = config.toHeaders("https://evil.com");

            assertThat(headers).isEmpty();
        }

        @Test
        @DisplayName("toHeaders should return wildcard for allowAll without credentials")
        void toHeadersWildcard() {
            CorsConfig config = CorsConfig.allowAll();

            Map<String, String> headers = config.toHeaders("https://example.com");

            assertThat(headers).containsEntry("Access-Control-Allow-Origin", "*");
            assertThat(headers).doesNotContainKey("Access-Control-Allow-Credentials");
        }

        @Test
        @DisplayName("toHeaders should return specific origin with credentials")
        void toHeadersWithCredentials() {
            CorsConfig config = CorsConfig.restrictive("https://example.com");

            Map<String, String> headers = config.toHeaders("https://example.com");

            assertThat(headers).containsEntry("Access-Control-Allow-Origin", "https://example.com");
            assertThat(headers).containsEntry("Access-Control-Allow-Credentials", "true");
        }

        @Test
        @DisplayName("toHeaders should include all configured headers")
        void toHeadersAllFields() {
            CorsConfig config = CorsConfig.builder()
                    .allowOrigin("https://example.com")
                    .allowMethod("GET", "POST")
                    .allowHeader("Authorization")
                    .exposeHeader("X-Custom")
                    .allowCredentials(true)
                    .maxAge(3600)
                    .build();

            Map<String, String> headers = config.toHeaders("https://example.com");

            assertThat(headers).containsEntry("Access-Control-Allow-Origin", "https://example.com");
            assertThat(headers.get("Access-Control-Allow-Methods")).contains("GET");
            assertThat(headers.get("Access-Control-Allow-Methods")).contains("POST");
            assertThat(headers).containsEntry("Access-Control-Allow-Headers", "Authorization");
            assertThat(headers).containsEntry("Access-Control-Expose-Headers", "X-Custom");
            assertThat(headers).containsEntry("Access-Control-Allow-Credentials", "true");
            assertThat(headers).containsEntry("Access-Control-Max-Age", "3600");
        }

        @Test
        @DisplayName("toHeaders should omit max-age when zero")
        void toHeadersOmitZeroMaxAge() {
            CorsConfig config = CorsConfig.builder()
                    .allowOrigin("*")
                    .maxAge(0)
                    .build();

            Map<String, String> headers = config.toHeaders("https://example.com");

            assertThat(headers).doesNotContainKey("Access-Control-Max-Age");
        }

        @Test
        @DisplayName("toHeaders should omit empty sets")
        void toHeadersOmitEmptySets() {
            CorsConfig config = CorsConfig.builder()
                    .allowOrigin("*")
                    .build();

            Map<String, String> headers = config.toHeaders("https://example.com");

            assertThat(headers).containsKey("Access-Control-Allow-Origin");
            assertThat(headers).doesNotContainKey("Access-Control-Allow-Methods");
            assertThat(headers).doesNotContainKey("Access-Control-Allow-Headers");
            assertThat(headers).doesNotContainKey("Access-Control-Expose-Headers");
        }
    }

    // ==================== ApplyTo Tests ====================

    @Nested
    @DisplayName("applyTo")
    class ApplyToTests {

        @Test
        @DisplayName("should apply headers to HttpHeaders")
        void shouldApplyToHttpHeaders() {
            CorsConfig config = CorsConfig.restrictive("https://example.com");
            HttpHeaders headers = HttpHeaders.of();

            config.applyTo(headers, "https://example.com");

            assertThat(headers.get("Access-Control-Allow-Origin")).isEqualTo("https://example.com");
            assertThat(headers.get("Access-Control-Allow-Credentials")).isEqualTo("true");
        }

        @Test
        @DisplayName("should not apply headers for disallowed origin")
        void shouldNotApplyForDisallowedOrigin() {
            CorsConfig config = CorsConfig.restrictive("https://example.com");
            HttpHeaders headers = HttpHeaders.of();

            config.applyTo(headers, "https://evil.com");

            assertThat(headers.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("should reject null headers")
        void shouldRejectNullHeaders() {
            CorsConfig config = CorsConfig.allowAll();

            assertThatThrownBy(() -> config.applyTo(null, "https://example.com"))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== Immutability Tests ====================

    @Nested
    @DisplayName("Immutability")
    class ImmutabilityTests {

        @Test
        @DisplayName("allowedOrigins should be unmodifiable")
        void allowedOriginsShouldBeUnmodifiable() {
            CorsConfig config = CorsConfig.allowAll();

            assertThatThrownBy(() -> config.allowedOrigins().add("evil"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("allowedMethods should be unmodifiable")
        void allowedMethodsShouldBeUnmodifiable() {
            CorsConfig config = CorsConfig.allowAll();

            assertThatThrownBy(() -> config.allowedMethods().add("EVIL"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("allowedHeaders should be unmodifiable")
        void allowedHeadersShouldBeUnmodifiable() {
            CorsConfig config = CorsConfig.allowAll();

            assertThatThrownBy(() -> config.allowedHeaders().add("Evil"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("exposedHeaders should be unmodifiable")
        void exposedHeadersShouldBeUnmodifiable() {
            CorsConfig config = CorsConfig.builder()
                    .allowOrigin("*")
                    .exposeHeader("X-Custom")
                    .build();

            assertThatThrownBy(() -> config.exposedHeaders().add("Evil"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("toHeaders result should be unmodifiable")
        void toHeadersShouldBeUnmodifiable() {
            CorsConfig config = CorsConfig.allowAll();
            Map<String, String> headers = config.toHeaders("https://example.com");

            assertThatThrownBy(() -> headers.put("Evil", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // ==================== Record Equality Tests ====================

    @Nested
    @DisplayName("Record equality")
    class RecordEqualityTests {

        @Test
        @DisplayName("equal configs should be equal")
        void equalConfigsShouldBeEqual() {
            CorsConfig a = CorsConfig.builder()
                    .allowOrigin("https://example.com")
                    .allowMethod("GET")
                    .maxAge(3600)
                    .build();
            CorsConfig b = CorsConfig.builder()
                    .allowOrigin("https://example.com")
                    .allowMethod("GET")
                    .maxAge(3600)
                    .build();

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("different configs should not be equal")
        void differentConfigsShouldNotBeEqual() {
            CorsConfig a = CorsConfig.allowAll();
            CorsConfig b = CorsConfig.restrictive("https://example.com");

            assertThat(a).isNotEqualTo(b);
        }
    }
}
