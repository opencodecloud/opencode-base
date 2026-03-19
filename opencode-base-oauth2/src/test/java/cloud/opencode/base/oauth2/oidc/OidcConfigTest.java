package cloud.opencode.base.oauth2.oidc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * OidcConfigTest Tests
 * OidcConfigTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("OidcConfig 测试")
class OidcConfigTest {

    @Nested
    @DisplayName("Record字段测试")
    class RecordFieldsTests {

        @Test
        @DisplayName("Record包含所有字段")
        void testRecordFields() {
            OidcConfig config = new OidcConfig(
                    "https://issuer.com",
                    "https://issuer.com/.well-known/jwks.json",
                    "https://issuer.com/userinfo",
                    true,
                    true,
                    true,
                    true,
                    Duration.ofMinutes(5),
                    Set.of("sub", "email"),
                    Set.of("name", "picture")
            );

            assertThat(config.issuer()).isEqualTo("https://issuer.com");
            assertThat(config.jwksUri()).isEqualTo("https://issuer.com/.well-known/jwks.json");
            assertThat(config.userInfoEndpoint()).isEqualTo("https://issuer.com/userinfo");
            assertThat(config.validateIdToken()).isTrue();
            assertThat(config.validateNonce()).isTrue();
            assertThat(config.validateAudience()).isTrue();
            assertThat(config.validateExpiration()).isTrue();
            assertThat(config.clockSkew()).isEqualTo(Duration.ofMinutes(5));
            assertThat(config.requiredClaims()).containsExactlyInAnyOrder("sub", "email");
            assertThat(config.requestedClaims()).containsExactlyInAnyOrder("name", "picture");
        }

        @Test
        @DisplayName("requiredClaims是不可变的")
        void testRequiredClaimsImmutable() {
            OidcConfig config = OidcConfig.builder()
                    .requiredClaims("sub")
                    .build();

            assertThatThrownBy(() -> config.requiredClaims().add("email"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("requestedClaims是不可变的")
        void testRequestedClaimsImmutable() {
            OidcConfig config = OidcConfig.builder()
                    .requestedClaims("name")
                    .build();

            assertThatThrownBy(() -> config.requestedClaims().add("picture"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("null clockSkew变成默认5分钟")
        void testNullClockSkewDefault() {
            OidcConfig config = new OidcConfig(
                    null, null, null, false, false, false, false, null, null, null
            );

            assertThat(config.clockSkew()).isEqualTo(Duration.ofMinutes(5));
        }

        @Test
        @DisplayName("null requiredClaims变成空Set")
        void testNullRequiredClaimsDefault() {
            OidcConfig config = new OidcConfig(
                    null, null, null, false, false, false, false, null, null, null
            );

            assertThat(config.requiredClaims()).isEmpty();
        }

        @Test
        @DisplayName("null requestedClaims变成空Set")
        void testNullRequestedClaimsDefault() {
            OidcConfig config = new OidcConfig(
                    null, null, null, false, false, false, false, null, null, null
            );

            assertThat(config.requestedClaims()).isEmpty();
        }
    }

    @Nested
    @DisplayName("canValidateSignature方法测试")
    class CanValidateSignatureTests {

        @Test
        @DisplayName("有jwksUri返回true")
        void testCanValidateSignatureTrue() {
            OidcConfig config = OidcConfig.builder()
                    .jwksUri("https://issuer.com/.well-known/jwks.json")
                    .build();

            assertThat(config.canValidateSignature()).isTrue();
        }

        @Test
        @DisplayName("无jwksUri返回false")
        void testCanValidateSignatureFalse() {
            OidcConfig config = OidcConfig.builder().build();

            assertThat(config.canValidateSignature()).isFalse();
        }

        @Test
        @DisplayName("空白jwksUri返回false")
        void testCanValidateSignatureBlank() {
            OidcConfig config = OidcConfig.builder()
                    .jwksUri("  ")
                    .build();

            assertThat(config.canValidateSignature()).isFalse();
        }
    }

    @Nested
    @DisplayName("canValidateIssuer方法测试")
    class CanValidateIssuerTests {

        @Test
        @DisplayName("有issuer返回true")
        void testCanValidateIssuerTrue() {
            OidcConfig config = OidcConfig.builder()
                    .issuer("https://issuer.com")
                    .build();

            assertThat(config.canValidateIssuer()).isTrue();
        }

        @Test
        @DisplayName("无issuer返回false")
        void testCanValidateIssuerFalse() {
            OidcConfig config = OidcConfig.builder().build();

            assertThat(config.canValidateIssuer()).isFalse();
        }

        @Test
        @DisplayName("空白issuer返回false")
        void testCanValidateIssuerBlank() {
            OidcConfig config = OidcConfig.builder()
                    .issuer("  ")
                    .build();

            assertThat(config.canValidateIssuer()).isFalse();
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("使用所有builder方法")
        void testBuilderAllMethods() {
            OidcConfig config = OidcConfig.builder()
                    .issuer("https://issuer.com")
                    .jwksUri("https://issuer.com/.well-known/jwks.json")
                    .userInfoEndpoint("https://issuer.com/userinfo")
                    .validateIdToken(true)
                    .validateNonce(true)
                    .validateAudience(true)
                    .validateExpiration(true)
                    .clockSkew(Duration.ofMinutes(10))
                    .requiredClaims("sub", "email")
                    .requestedClaims("name", "picture")
                    .build();

            assertThat(config.issuer()).isEqualTo("https://issuer.com");
            assertThat(config.jwksUri()).isEqualTo("https://issuer.com/.well-known/jwks.json");
            assertThat(config.userInfoEndpoint()).isEqualTo("https://issuer.com/userinfo");
            assertThat(config.validateIdToken()).isTrue();
            assertThat(config.validateNonce()).isTrue();
            assertThat(config.validateAudience()).isTrue();
            assertThat(config.validateExpiration()).isTrue();
            assertThat(config.clockSkew()).isEqualTo(Duration.ofMinutes(10));
            assertThat(config.requiredClaims()).containsExactlyInAnyOrder("sub", "email");
            assertThat(config.requestedClaims()).containsExactlyInAnyOrder("name", "picture");
        }

        @Test
        @DisplayName("builder默认值")
        void testBuilderDefaults() {
            OidcConfig config = OidcConfig.builder().build();

            assertThat(config.issuer()).isNull();
            assertThat(config.jwksUri()).isNull();
            assertThat(config.userInfoEndpoint()).isNull();
            assertThat(config.validateIdToken()).isTrue();
            assertThat(config.validateNonce()).isFalse();
            assertThat(config.validateAudience()).isTrue();
            assertThat(config.validateExpiration()).isTrue();
            assertThat(config.clockSkew()).isEqualTo(Duration.ofMinutes(5));
            assertThat(config.requiredClaims()).isEmpty();
            assertThat(config.requestedClaims()).isEmpty();
        }

        @Test
        @DisplayName("禁用验证")
        void testBuilderDisableValidation() {
            OidcConfig config = OidcConfig.builder()
                    .validateIdToken(false)
                    .validateNonce(false)
                    .validateAudience(false)
                    .validateExpiration(false)
                    .build();

            assertThat(config.validateIdToken()).isFalse();
            assertThat(config.validateNonce()).isFalse();
            assertThat(config.validateAudience()).isFalse();
            assertThat(config.validateExpiration()).isFalse();
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodsTests {

        @Test
        @DisplayName("defaults()创建默认配置")
        void testDefaults() {
            OidcConfig config = OidcConfig.defaults();

            assertThat(config).isNotNull();
            assertThat(config.validateIdToken()).isTrue();
            assertThat(config.validateAudience()).isTrue();
            assertThat(config.validateExpiration()).isTrue();
            assertThat(config.validateNonce()).isFalse();
        }

        @Test
        @DisplayName("strict()创建严格配置")
        void testStrict() {
            OidcConfig config = OidcConfig.strict(
                    "https://issuer.com",
                    "https://issuer.com/.well-known/jwks.json"
            );

            assertThat(config.issuer()).isEqualTo("https://issuer.com");
            assertThat(config.jwksUri()).isEqualTo("https://issuer.com/.well-known/jwks.json");
            assertThat(config.validateIdToken()).isTrue();
            assertThat(config.validateNonce()).isTrue();
            assertThat(config.validateAudience()).isTrue();
            assertThat(config.validateExpiration()).isTrue();
            assertThat(config.clockSkew()).isEqualTo(Duration.ofMinutes(2));
        }

        @Test
        @DisplayName("builder()创建构建器")
        void testBuilder() {
            OidcConfig.Builder builder = OidcConfig.builder();
            assertThat(builder).isNotNull();
        }
    }
}
