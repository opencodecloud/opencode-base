package cloud.opencode.base.oauth2.discovery;

import cloud.opencode.base.oauth2.OAuth2Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * DiscoveryDocument Tests
 * DiscoveryDocument 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
@DisplayName("DiscoveryDocument 测试")
class DiscoveryDocumentTest {

    private static final String ISSUER = "https://accounts.example.com";

    private DiscoveryDocument createFullDocument() {
        return DiscoveryDocument.builder()
                .issuer(ISSUER)
                .authorizationEndpoint(ISSUER + "/authorize")
                .tokenEndpoint(ISSUER + "/token")
                .userinfoEndpoint(ISSUER + "/userinfo")
                .jwksUri(ISSUER + "/jwks")
                .registrationEndpoint(ISSUER + "/register")
                .revocationEndpoint(ISSUER + "/revoke")
                .introspectionEndpoint(ISSUER + "/introspect")
                .deviceAuthorizationEndpoint(ISSUER + "/device")
                .parEndpoint(ISSUER + "/par")
                .scopesSupported(List.of("openid", "email", "profile"))
                .responseTypesSupported(List.of("code", "token"))
                .grantTypesSupported(List.of("authorization_code", "refresh_token", "client_credentials"))
                .tokenEndpointAuthMethodsSupported(List.of("client_secret_basic", "client_secret_post"))
                .codeChallengeMethodsSupported(List.of("S256", "plain"))
                .build();
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("使用所有字段构建")
        void testBuildFull() {
            DiscoveryDocument doc = createFullDocument();

            assertThat(doc.issuer()).isEqualTo(ISSUER);
            assertThat(doc.authorizationEndpoint()).isEqualTo(ISSUER + "/authorize");
            assertThat(doc.tokenEndpoint()).isEqualTo(ISSUER + "/token");
            assertThat(doc.userinfoEndpoint()).isEqualTo(ISSUER + "/userinfo");
            assertThat(doc.jwksUri()).isEqualTo(ISSUER + "/jwks");
            assertThat(doc.registrationEndpoint()).isEqualTo(ISSUER + "/register");
            assertThat(doc.revocationEndpoint()).isEqualTo(ISSUER + "/revoke");
            assertThat(doc.introspectionEndpoint()).isEqualTo(ISSUER + "/introspect");
            assertThat(doc.deviceAuthorizationEndpoint()).isEqualTo(ISSUER + "/device");
            assertThat(doc.parEndpoint()).isEqualTo(ISSUER + "/par");
        }

        @Test
        @DisplayName("仅使用必需字段构建")
        void testBuildMinimal() {
            DiscoveryDocument doc = DiscoveryDocument.builder()
                    .issuer(ISSUER)
                    .build();

            assertThat(doc.issuer()).isEqualTo(ISSUER);
            assertThat(doc.authorizationEndpoint()).isNull();
            assertThat(doc.tokenEndpoint()).isNull();
            assertThat(doc.scopesSupported()).isEmpty();
            assertThat(doc.responseTypesSupported()).isEmpty();
            assertThat(doc.grantTypesSupported()).isEmpty();
            assertThat(doc.tokenEndpointAuthMethodsSupported()).isEmpty();
            assertThat(doc.codeChallengeMethodsSupported()).isEmpty();
        }

        @Test
        @DisplayName("issuer 为 null 抛出异常")
        void testBuildNullIssuer() {
            assertThatThrownBy(() -> DiscoveryDocument.builder().build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("issuer");
        }

        @Test
        @DisplayName("列表字段为 null 时默认空列表")
        void testNullLists() {
            DiscoveryDocument doc = DiscoveryDocument.builder()
                    .issuer(ISSUER)
                    .scopesSupported(null)
                    .grantTypesSupported(null)
                    .build();

            assertThat(doc.scopesSupported()).isEmpty();
            assertThat(doc.grantTypesSupported()).isEmpty();
        }
    }

    @Nested
    @DisplayName("列表不可变性测试")
    class ImmutabilityTests {

        @Test
        @DisplayName("scopesSupported 返回不可变列表")
        void testScopesSupportedImmutable() {
            DiscoveryDocument doc = createFullDocument();
            assertThatThrownBy(() -> doc.scopesSupported().add("new_scope"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("grantTypesSupported 返回不可变列表")
        void testGrantTypesSupportedImmutable() {
            DiscoveryDocument doc = createFullDocument();
            assertThatThrownBy(() -> doc.grantTypesSupported().add("new_grant"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("codeChallengeMethodsSupported 返回不可变列表")
        void testCodeChallengeMethodsSupportedImmutable() {
            DiscoveryDocument doc = createFullDocument();
            assertThatThrownBy(() -> doc.codeChallengeMethodsSupported().add("new_method"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("supports() 方法测试")
    class SupportsTests {

        @Test
        @DisplayName("supports 支持的授权类型返回 true")
        void testSupportsGrantType() {
            DiscoveryDocument doc = createFullDocument();
            assertThat(doc.supports("authorization_code")).isTrue();
            assertThat(doc.supports("refresh_token")).isTrue();
            assertThat(doc.supports("client_credentials")).isTrue();
        }

        @Test
        @DisplayName("supports 不支持的授权类型返回 false")
        void testSupportsUnsupportedGrantType() {
            DiscoveryDocument doc = createFullDocument();
            assertThat(doc.supports("device_code")).isFalse();
        }

        @Test
        @DisplayName("supports null 返回 false")
        void testSupportsNull() {
            DiscoveryDocument doc = createFullDocument();
            assertThat(doc.supports(null)).isFalse();
        }

        @Test
        @DisplayName("supports 空列表返回 false")
        void testSupportsEmptyList() {
            DiscoveryDocument doc = DiscoveryDocument.builder()
                    .issuer(ISSUER)
                    .build();
            assertThat(doc.supports("authorization_code")).isFalse();
        }
    }

    @Nested
    @DisplayName("supportsScope() 方法测试")
    class SupportsScopeTests {

        @Test
        @DisplayName("supportsScope 支持的范围返回 true")
        void testSupportsScopeValid() {
            DiscoveryDocument doc = createFullDocument();
            assertThat(doc.supportsScope("openid")).isTrue();
            assertThat(doc.supportsScope("email")).isTrue();
        }

        @Test
        @DisplayName("supportsScope 不支持的范围返回 false")
        void testSupportsScopeInvalid() {
            DiscoveryDocument doc = createFullDocument();
            assertThat(doc.supportsScope("admin")).isFalse();
        }

        @Test
        @DisplayName("supportsScope null 返回 false")
        void testSupportsScopeNull() {
            DiscoveryDocument doc = createFullDocument();
            assertThat(doc.supportsScope(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("supportsPkce() 方法测试")
    class SupportsPkceTests {

        @Test
        @DisplayName("supportsPkce 包含 S256 返回 true")
        void testSupportsPkceTrue() {
            DiscoveryDocument doc = createFullDocument();
            assertThat(doc.supportsPkce()).isTrue();
        }

        @Test
        @DisplayName("supportsPkce 不包含 S256 返回 false")
        void testSupportsPkceFalse() {
            DiscoveryDocument doc = DiscoveryDocument.builder()
                    .issuer(ISSUER)
                    .codeChallengeMethodsSupported(List.of("plain"))
                    .build();
            assertThat(doc.supportsPkce()).isFalse();
        }

        @Test
        @DisplayName("supportsPkce 空列表返回 false")
        void testSupportsPkceEmpty() {
            DiscoveryDocument doc = DiscoveryDocument.builder()
                    .issuer(ISSUER)
                    .build();
            assertThat(doc.supportsPkce()).isFalse();
        }
    }

    @Nested
    @DisplayName("toConfig() 方法测试")
    class ToConfigTests {

        @Test
        @DisplayName("toConfig 创建正确的 OAuth2Config")
        void testToConfig() {
            DiscoveryDocument doc = createFullDocument();
            OAuth2Config config = doc.toConfig("my-client-id", "my-secret");

            assertThat(config.clientId()).isEqualTo("my-client-id");
            assertThat(config.clientSecret()).isEqualTo("my-secret");
            assertThat(config.authorizationEndpoint()).isEqualTo(ISSUER + "/authorize");
            assertThat(config.tokenEndpoint()).isEqualTo(ISSUER + "/token");
            assertThat(config.userInfoEndpoint()).isEqualTo(ISSUER + "/userinfo");
            assertThat(config.revocationEndpoint()).isEqualTo(ISSUER + "/revoke");
            assertThat(config.deviceAuthorizationEndpoint()).isEqualTo(ISSUER + "/device");
            assertThat(config.usePkce()).isTrue();
        }

        @Test
        @DisplayName("toConfig 公共客户端（无 secret）")
        void testToConfigPublicClient() {
            DiscoveryDocument doc = createFullDocument();
            OAuth2Config config = doc.toConfig("my-client-id", null);

            assertThat(config.clientId()).isEqualTo("my-client-id");
            assertThat(config.clientSecret()).isNull();
        }

        @Test
        @DisplayName("toConfig PKCE 不支持时 usePkce 为 false")
        void testToConfigNoPkce() {
            DiscoveryDocument doc = DiscoveryDocument.builder()
                    .issuer(ISSUER)
                    .tokenEndpoint(ISSUER + "/token")
                    .codeChallengeMethodsSupported(List.of("plain"))
                    .build();

            OAuth2Config config = doc.toConfig("client-id", null);
            assertThat(config.usePkce()).isFalse();
        }

        @Test
        @DisplayName("toConfig clientId 为 null 抛出异常")
        void testToConfigNullClientId() {
            DiscoveryDocument doc = createFullDocument();
            assertThatThrownBy(() -> doc.toConfig(null, "secret"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("clientId");
        }
    }

    @Nested
    @DisplayName("Record 方法测试")
    class RecordMethodsTests {

        @Test
        @DisplayName("equals 和 hashCode")
        void testEqualsAndHashCode() {
            DiscoveryDocument doc1 = DiscoveryDocument.builder()
                    .issuer(ISSUER)
                    .tokenEndpoint(ISSUER + "/token")
                    .build();
            DiscoveryDocument doc2 = DiscoveryDocument.builder()
                    .issuer(ISSUER)
                    .tokenEndpoint(ISSUER + "/token")
                    .build();

            assertThat(doc1).isEqualTo(doc2);
            assertThat(doc1.hashCode()).isEqualTo(doc2.hashCode());
        }

        @Test
        @DisplayName("toString 包含关键字段")
        void testToString() {
            DiscoveryDocument doc = createFullDocument();
            String str = doc.toString();

            assertThat(str).contains(ISSUER);
        }
    }
}
