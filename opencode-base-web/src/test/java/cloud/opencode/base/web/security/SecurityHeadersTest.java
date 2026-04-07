package cloud.opencode.base.web.security;

import cloud.opencode.base.web.http.HttpHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link SecurityHeaders}.
 */
@DisplayName("SecurityHeaders")
class SecurityHeadersTest {

    // ==================== Builder Tests ====================

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("should build with all headers")
        void shouldBuildWithAllHeaders() {
            SecurityHeaders headers = SecurityHeaders.builder()
                    .contentSecurityPolicy("default-src 'self'")
                    .strictTransportSecurity(31536000, true)
                    .xFrameOptions(SecurityHeaders.FrameOption.DENY)
                    .xContentTypeOptions()
                    .xXssProtection()
                    .referrerPolicy(SecurityHeaders.ReferrerPolicy.NO_REFERRER)
                    .permissionsPolicy("geolocation=()")
                    .crossOriginEmbedderPolicy("require-corp")
                    .crossOriginOpenerPolicy("same-origin")
                    .build();

            Map<String, String> map = headers.toMap();
            assertThat(map).hasSize(9);
            assertThat(map).containsEntry("Content-Security-Policy", "default-src 'self'");
            assertThat(map).containsEntry("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            assertThat(map).containsEntry("X-Frame-Options", "DENY");
            assertThat(map).containsEntry("X-Content-Type-Options", "nosniff");
            assertThat(map).containsEntry("X-XSS-Protection", "0");
            assertThat(map).containsEntry("Referrer-Policy", "no-referrer");
            assertThat(map).containsEntry("Permissions-Policy", "geolocation=()");
            assertThat(map).containsEntry("Cross-Origin-Embedder-Policy", "require-corp");
            assertThat(map).containsEntry("Cross-Origin-Opener-Policy", "same-origin");
        }

        @Test
        @DisplayName("should build with empty headers")
        void shouldBuildWithEmptyHeaders() {
            SecurityHeaders headers = SecurityHeaders.builder().build();

            assertThat(headers.toMap()).isEmpty();
        }

        @Test
        @DisplayName("should set HSTS without includeSubDomains")
        void shouldSetHstsWithoutSubDomains() {
            SecurityHeaders headers = SecurityHeaders.builder()
                    .strictTransportSecurity(3600, false)
                    .build();

            assertThat(headers.toMap())
                    .containsEntry("Strict-Transport-Security", "max-age=3600");
        }

        @Test
        @DisplayName("should reject negative HSTS max-age")
        void shouldRejectNegativeHstsMaxAge() {
            assertThatThrownBy(() -> SecurityHeaders.builder()
                    .strictTransportSecurity(-1, false))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("maxAge");
        }

        @Test
        @DisplayName("should reject null CSP policy")
        void shouldRejectNullCsp() {
            assertThatThrownBy(() -> SecurityHeaders.builder().contentSecurityPolicy(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject null frame option")
        void shouldRejectNullFrameOption() {
            assertThatThrownBy(() -> SecurityHeaders.builder().xFrameOptions(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject null referrer policy")
        void shouldRejectNullReferrerPolicy() {
            assertThatThrownBy(() -> SecurityHeaders.builder().referrerPolicy(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject null permissions policy")
        void shouldRejectNullPermissionsPolicy() {
            assertThatThrownBy(() -> SecurityHeaders.builder().permissionsPolicy(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject null COEP policy")
        void shouldRejectNullCoep() {
            assertThatThrownBy(() -> SecurityHeaders.builder().crossOriginEmbedderPolicy(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should reject null COOP policy")
        void shouldRejectNullCoop() {
            assertThatThrownBy(() -> SecurityHeaders.builder().crossOriginOpenerPolicy(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("later builder call should overwrite earlier")
        void shouldOverwriteEarlierBuilderCall() {
            SecurityHeaders headers = SecurityHeaders.builder()
                    .contentSecurityPolicy("default-src 'self'")
                    .contentSecurityPolicy("default-src 'none'")
                    .build();

            assertThat(headers.toMap())
                    .containsEntry("Content-Security-Policy", "default-src 'none'");
        }
    }

    // ==================== Preset Tests ====================

    @Nested
    @DisplayName("Presets")
    class PresetTests {

        @Test
        @DisplayName("strict should include all restrictive headers")
        void strictShouldIncludeAllHeaders() {
            SecurityHeaders headers = SecurityHeaders.strict();
            Map<String, String> map = headers.toMap();

            assertThat(map).containsKey("Content-Security-Policy");
            assertThat(map.get("Content-Security-Policy")).contains("default-src 'none'");
            assertThat(map.get("Content-Security-Policy")).contains("frame-ancestors 'none'");
            assertThat(map).containsEntry("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            assertThat(map).containsEntry("X-Frame-Options", "DENY");
            assertThat(map).containsEntry("X-Content-Type-Options", "nosniff");
            assertThat(map).containsEntry("X-XSS-Protection", "0");
            assertThat(map).containsEntry("Referrer-Policy", "no-referrer");
            assertThat(map).containsKey("Permissions-Policy");
            assertThat(map).containsEntry("Cross-Origin-Embedder-Policy", "require-corp");
            assertThat(map).containsEntry("Cross-Origin-Opener-Policy", "same-origin");
        }

        @Test
        @DisplayName("standard should include common security headers")
        void standardShouldIncludeCommonHeaders() {
            SecurityHeaders headers = SecurityHeaders.standard();
            Map<String, String> map = headers.toMap();

            assertThat(map).containsEntry("Content-Security-Policy", "default-src 'self'");
            assertThat(map).containsEntry("Strict-Transport-Security", "max-age=31536000");
            assertThat(map).containsEntry("X-Frame-Options", "SAMEORIGIN");
            assertThat(map).containsEntry("X-Content-Type-Options", "nosniff");
            assertThat(map).containsEntry("X-XSS-Protection", "0");
            assertThat(map).containsEntry("Referrer-Policy", "strict-origin-when-cross-origin");
        }

        @Test
        @DisplayName("standard should not include cross-origin policies")
        void standardShouldNotIncludeCrossOriginPolicies() {
            SecurityHeaders headers = SecurityHeaders.standard();
            Map<String, String> map = headers.toMap();

            assertThat(map).doesNotContainKey("Cross-Origin-Embedder-Policy");
            assertThat(map).doesNotContainKey("Cross-Origin-Opener-Policy");
            assertThat(map).doesNotContainKey("Permissions-Policy");
        }
    }

    // ==================== Enum Tests ====================

    @Nested
    @DisplayName("Enums")
    class EnumTests {

        @Test
        @DisplayName("FrameOption values should have correct header values")
        void frameOptionValues() {
            assertThat(SecurityHeaders.FrameOption.DENY.value()).isEqualTo("DENY");
            assertThat(SecurityHeaders.FrameOption.SAMEORIGIN.value()).isEqualTo("SAMEORIGIN");
        }

        @Test
        @DisplayName("ReferrerPolicy values should have correct header values")
        void referrerPolicyValues() {
            assertThat(SecurityHeaders.ReferrerPolicy.NO_REFERRER.value())
                    .isEqualTo("no-referrer");
            assertThat(SecurityHeaders.ReferrerPolicy.NO_REFERRER_WHEN_DOWNGRADE.value())
                    .isEqualTo("no-referrer-when-downgrade");
            assertThat(SecurityHeaders.ReferrerPolicy.ORIGIN.value())
                    .isEqualTo("origin");
            assertThat(SecurityHeaders.ReferrerPolicy.ORIGIN_WHEN_CROSS_ORIGIN.value())
                    .isEqualTo("origin-when-cross-origin");
            assertThat(SecurityHeaders.ReferrerPolicy.SAME_ORIGIN.value())
                    .isEqualTo("same-origin");
            assertThat(SecurityHeaders.ReferrerPolicy.STRICT_ORIGIN.value())
                    .isEqualTo("strict-origin");
            assertThat(SecurityHeaders.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN.value())
                    .isEqualTo("strict-origin-when-cross-origin");
            assertThat(SecurityHeaders.ReferrerPolicy.UNSAFE_URL.value())
                    .isEqualTo("unsafe-url");
        }

        @Test
        @DisplayName("FrameOption should have exactly 2 values")
        void frameOptionCount() {
            assertThat(SecurityHeaders.FrameOption.values()).hasSize(2);
        }

        @Test
        @DisplayName("ReferrerPolicy should have exactly 8 values")
        void referrerPolicyCount() {
            assertThat(SecurityHeaders.ReferrerPolicy.values()).hasSize(8);
        }
    }

    // ==================== ApplyTo Tests ====================

    @Nested
    @DisplayName("applyTo")
    class ApplyToTests {

        @Test
        @DisplayName("should apply all headers to HttpHeaders")
        void shouldApplyAllHeaders() {
            SecurityHeaders secHeaders = SecurityHeaders.strict();
            HttpHeaders httpHeaders = HttpHeaders.of();

            secHeaders.applyTo(httpHeaders);

            assertThat(httpHeaders.get("X-Frame-Options")).isEqualTo("DENY");
            assertThat(httpHeaders.get("X-Content-Type-Options")).isEqualTo("nosniff");
            assertThat(httpHeaders.get("X-XSS-Protection")).isEqualTo("0");
            assertThat(httpHeaders.get("Content-Security-Policy")).isNotNull();
            assertThat(httpHeaders.get("Strict-Transport-Security")).isNotNull();
            assertThat(httpHeaders.get("Referrer-Policy")).isEqualTo("no-referrer");
        }

        @Test
        @DisplayName("should reject null httpHeaders")
        void shouldRejectNullHttpHeaders() {
            SecurityHeaders secHeaders = SecurityHeaders.standard();

            assertThatThrownBy(() -> secHeaders.applyTo(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should overwrite existing headers")
        void shouldOverwriteExistingHeaders() {
            HttpHeaders httpHeaders = HttpHeaders.of()
                    .set("X-Frame-Options", "SAMEORIGIN");

            SecurityHeaders secHeaders = SecurityHeaders.builder()
                    .xFrameOptions(SecurityHeaders.FrameOption.DENY)
                    .build();

            secHeaders.applyTo(httpHeaders);

            assertThat(httpHeaders.get("X-Frame-Options")).isEqualTo("DENY");
        }
    }

    // ==================== Immutability Tests ====================

    @Nested
    @DisplayName("Immutability")
    class ImmutabilityTests {

        @Test
        @DisplayName("toMap should return unmodifiable map")
        void toMapShouldBeUnmodifiable() {
            SecurityHeaders headers = SecurityHeaders.strict();
            Map<String, String> map = headers.toMap();

            assertThatThrownBy(() -> map.put("Evil", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("multiple toMap calls should return same content")
        void multipleToMapCallsShouldReturnSameContent() {
            SecurityHeaders headers = SecurityHeaders.standard();

            assertThat(headers.toMap()).isEqualTo(headers.toMap());
        }
    }

    // ==================== ToString Tests ====================

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should include SecurityHeaders prefix")
        void shouldIncludePrefix() {
            SecurityHeaders headers = SecurityHeaders.builder()
                    .xContentTypeOptions()
                    .build();

            assertThat(headers.toString()).startsWith("SecurityHeaders");
            assertThat(headers.toString()).contains("nosniff");
        }
    }
}
