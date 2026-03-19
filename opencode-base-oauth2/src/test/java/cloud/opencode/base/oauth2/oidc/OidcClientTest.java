package cloud.opencode.base.oauth2.oidc;

import cloud.opencode.base.oauth2.OAuth2Client;
import cloud.opencode.base.oauth2.OAuth2Config;
import cloud.opencode.base.oauth2.OAuth2Token;
import cloud.opencode.base.oauth2.exception.OAuth2Exception;
import cloud.opencode.base.oauth2.pkce.PkceChallenge;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * OidcClientTest Tests
 * OidcClientTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("OidcClient 测试")
class OidcClientTest {

    private OAuth2Client oauth2Client;
    private OidcClient oidcClient;

    @BeforeEach
    void setUp() {
        OAuth2Config config = OAuth2Config.builder()
                .clientId("test-client")
                .clientSecret("test-secret")
                .authorizationEndpoint("https://auth.example.com/authorize")
                .tokenEndpoint("https://auth.example.com/token")
                .userInfoEndpoint("https://auth.example.com/userinfo")
                .redirectUri("https://app.example.com/callback")
                .scopes("openid", "profile", "email")
                .build();

        oauth2Client = OAuth2Client.builder()
                .config(config)
                .build();
    }

    @AfterEach
    void tearDown() {
        if (oidcClient != null) {
            oidcClient.close();
        }
        if (oauth2Client != null) {
            oauth2Client.close();
        }
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用builder创建")
        void testBuilder() {
            oidcClient = OidcClient.builder()
                    .oauth2Client(oauth2Client)
                    .build();

            assertThat(oidcClient).isNotNull();
            assertThat(oidcClient.oauth2Client()).isEqualTo(oauth2Client);
        }

        @Test
        @DisplayName("使用builder创建带有oidcConfig")
        void testBuilderWithOidcConfig() {
            OidcConfig oidcConfig = OidcConfig.builder()
                    .issuer("https://issuer.com")
                    .validateIdToken(true)
                    .build();

            oidcClient = OidcClient.builder()
                    .oauth2Client(oauth2Client)
                    .oidcConfig(oidcConfig)
                    .build();

            assertThat(oidcClient.oidcConfig().issuer()).isEqualTo("https://issuer.com");
        }

        @Test
        @DisplayName("oauth2Client为null抛出异常")
        void testBuilderNullOAuth2Client() {
            assertThatThrownBy(() -> OidcClient.builder().build())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("oidcConfig为null使用默认值")
        void testBuilderNullOidcConfig() {
            oidcClient = OidcClient.builder()
                    .oauth2Client(oauth2Client)
                    .oidcConfig(null)
                    .build();

            assertThat(oidcClient.oidcConfig()).isNotNull();
        }
    }

    @Nested
    @DisplayName("generateNonce方法测试")
    class GenerateNonceTests {

        @Test
        @DisplayName("生成非空nonce")
        void testGenerateNonce() {
            oidcClient = OidcClient.builder()
                    .oauth2Client(oauth2Client)
                    .build();

            String nonce = oidcClient.generateNonce();

            assertThat(nonce).isNotNull();
            assertThat(nonce).isNotBlank();
        }

        @Test
        @DisplayName("每次生成不同nonce")
        void testGenerateNonceUnique() {
            oidcClient = OidcClient.builder()
                    .oauth2Client(oauth2Client)
                    .build();

            String nonce1 = oidcClient.generateNonce();
            String nonce2 = oidcClient.generateNonce();

            assertThat(nonce1).isNotEqualTo(nonce2);
        }
    }

    @Nested
    @DisplayName("getAuthorizationUrl方法测试")
    class GetAuthorizationUrlTests {

        @Test
        @DisplayName("基本授权URL")
        void testGetAuthorizationUrl() {
            oidcClient = OidcClient.builder()
                    .oauth2Client(oauth2Client)
                    .build();

            String url = oidcClient.getAuthorizationUrl("state123");

            assertThat(url).contains("response_type=code");
            assertThat(url).contains("client_id=test-client");
            assertThat(url).contains("state=state123");
        }

        @Test
        @DisplayName("带有PKCE的授权URL")
        void testGetAuthorizationUrlWithPkce() {
            oidcClient = OidcClient.builder()
                    .oauth2Client(oauth2Client)
                    .build();

            PkceChallenge pkce = PkceChallenge.generate();
            String url = oidcClient.getAuthorizationUrl("state123", pkce);

            assertThat(url).contains("code_challenge=");
            assertThat(url).contains("code_challenge_method=S256");
        }

        @Test
        @DisplayName("带有PKCE和nonce的授权URL")
        void testGetAuthorizationUrlWithPkceAndNonce() {
            oidcClient = OidcClient.builder()
                    .oauth2Client(oauth2Client)
                    .build();

            PkceChallenge pkce = PkceChallenge.generate();
            String url = oidcClient.getAuthorizationUrl("state123", pkce, "nonce123");

            assertThat(url).contains("nonce=nonce123");
        }

        @Test
        @DisplayName("nonce为null时不添加到URL")
        void testGetAuthorizationUrlNullNonce() {
            oidcClient = OidcClient.builder()
                    .oauth2Client(oauth2Client)
                    .build();

            PkceChallenge pkce = PkceChallenge.generate();
            String url = oidcClient.getAuthorizationUrl("state123", pkce, null);

            assertThat(url).doesNotContain("nonce=");
        }
    }

    @Nested
    @DisplayName("validateIdToken方法测试")
    class ValidateIdTokenTests {

        @Test
        @DisplayName("无ID token抛出异常")
        void testValidateNoIdToken() {
            oidcClient = OidcClient.builder()
                    .oauth2Client(oauth2Client)
                    .build();

            OAuth2Token oauth2Token = OAuth2Token.builder()
                    .accessToken("access123")
                    .build();
            OidcToken oidcToken = new OidcToken(oauth2Token, null);

            assertThatThrownBy(() -> oidcClient.validateIdToken(oidcToken, null))
                    .isInstanceOf(OAuth2Exception.class);
        }

        @Test
        @DisplayName("验证过期token抛出异常")
        void testValidateExpiredToken() {
            OidcConfig oidcConfig = OidcConfig.builder()
                    .validateExpiration(true)
                    .build();

            oidcClient = OidcClient.builder()
                    .oauth2Client(oauth2Client)
                    .oidcConfig(oidcConfig)
                    .build();

            JwtClaims claims = new JwtClaims(
                    null, "user123", List.of("test-client"),
                    Instant.now().minusSeconds(3600), // 已过期
                    null, null, null, null, null, Map.of()
            );
            OidcToken oidcToken = new OidcToken(
                    OAuth2Token.builder().accessToken("access123").idToken("dummy").build(),
                    claims
            );

            assertThatThrownBy(() -> oidcClient.validateIdToken(oidcToken, null))
                    .isInstanceOf(OAuth2Exception.class);
        }

        @Test
        @DisplayName("验证issuer不匹配抛出异常")
        void testValidateIssuerMismatch() {
            OidcConfig oidcConfig = OidcConfig.builder()
                    .issuer("https://expected-issuer.com")
                    .validateExpiration(false)
                    .build();

            oidcClient = OidcClient.builder()
                    .oauth2Client(oauth2Client)
                    .oidcConfig(oidcConfig)
                    .build();

            JwtClaims claims = new JwtClaims(
                    "https://wrong-issuer.com", "user123", List.of("test-client"),
                    Instant.now().plusSeconds(3600), null, null, null, null, null, Map.of()
            );
            OidcToken oidcToken = new OidcToken(
                    OAuth2Token.builder().accessToken("access123").idToken("dummy").build(),
                    claims
            );

            assertThatThrownBy(() -> oidcClient.validateIdToken(oidcToken, null))
                    .isInstanceOf(OAuth2Exception.class);
        }

        @Test
        @DisplayName("验证audience不匹配抛出异常")
        void testValidateAudienceMismatch() {
            OidcConfig oidcConfig = OidcConfig.builder()
                    .validateAudience(true)
                    .validateExpiration(false)
                    .build();

            oidcClient = OidcClient.builder()
                    .oauth2Client(oauth2Client)
                    .oidcConfig(oidcConfig)
                    .build();

            JwtClaims claims = new JwtClaims(
                    null, "user123", List.of("wrong-client"),
                    Instant.now().plusSeconds(3600), null, null, null, null, null, Map.of()
            );
            OidcToken oidcToken = new OidcToken(
                    OAuth2Token.builder().accessToken("access123").idToken("dummy").build(),
                    claims
            );

            assertThatThrownBy(() -> oidcClient.validateIdToken(oidcToken, null))
                    .isInstanceOf(OAuth2Exception.class);
        }

        @Test
        @DisplayName("验证nonce不匹配抛出异常")
        void testValidateNonceMismatch() {
            OidcConfig oidcConfig = OidcConfig.builder()
                    .validateNonce(true)
                    .validateAudience(false)
                    .validateExpiration(false)
                    .build();

            oidcClient = OidcClient.builder()
                    .oauth2Client(oauth2Client)
                    .oidcConfig(oidcConfig)
                    .build();

            JwtClaims claims = new JwtClaims(
                    null, "user123", List.of("test-client"),
                    Instant.now().plusSeconds(3600), null, null, null, "wrong-nonce", null, Map.of()
            );
            OidcToken oidcToken = new OidcToken(
                    OAuth2Token.builder().accessToken("access123").idToken("dummy").build(),
                    claims
            );

            assertThatThrownBy(() -> oidcClient.validateIdToken(oidcToken, "expected-nonce"))
                    .isInstanceOf(OAuth2Exception.class);
        }

        @Test
        @DisplayName("验证缺少必需claim抛出异常")
        void testValidateMissingRequiredClaim() {
            OidcConfig oidcConfig = OidcConfig.builder()
                    .validateAudience(false)
                    .validateExpiration(false)
                    .requiredClaims("email")
                    .build();

            oidcClient = OidcClient.builder()
                    .oauth2Client(oauth2Client)
                    .oidcConfig(oidcConfig)
                    .build();

            JwtClaims claims = new JwtClaims(
                    null, "user123", List.of("test-client"),
                    Instant.now().plusSeconds(3600), null, null, null, null, null, Map.of()
            );
            OidcToken oidcToken = new OidcToken(
                    OAuth2Token.builder().accessToken("access123").idToken("dummy").build(),
                    claims
            );

            assertThatThrownBy(() -> oidcClient.validateIdToken(oidcToken, null))
                    .isInstanceOf(OAuth2Exception.class);
        }
    }

    @Nested
    @DisplayName("Token存储测试")
    class TokenStorageTests {

        @Test
        @DisplayName("存储和获取token")
        void testStoreAndGetToken() {
            oidcClient = OidcClient.builder()
                    .oauth2Client(oauth2Client)
                    .build();

            OidcToken token = new OidcToken(
                    OAuth2Token.builder().accessToken("access123").build(),
                    null
            );

            oidcClient.storeToken("user1", token);

            assertThat(oidcClient.getStoredToken("user1")).isPresent();
        }

        @Test
        @DisplayName("移除token")
        void testRemoveToken() {
            oidcClient = OidcClient.builder()
                    .oauth2Client(oauth2Client)
                    .build();

            OidcToken token = new OidcToken(
                    OAuth2Token.builder().accessToken("access123").build(),
                    null
            );

            oidcClient.storeToken("user1", token);
            oidcClient.removeToken("user1");

            assertThat(oidcClient.getStoredToken("user1")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("builder()创建构建器")
        void testBuilder() {
            OidcClient.Builder builder = OidcClient.builder();
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("oauth2Client设置")
        void testOAuth2Client() {
            oidcClient = OidcClient.builder()
                    .oauth2Client(oauth2Client)
                    .build();

            assertThat(oidcClient.oauth2Client()).isEqualTo(oauth2Client);
        }

        @Test
        @DisplayName("ownedOAuth2Client设置")
        void testOwnedOAuth2Client() {
            OAuth2Client newClient = OAuth2Client.builder()
                    .config(OAuth2Config.builder()
                            .clientId("test")
                            .tokenEndpoint("https://token.example.com")
                            .build())
                    .build();

            oidcClient = OidcClient.builder()
                    .ownedOAuth2Client(newClient)
                    .build();

            assertThat(oidcClient.oauth2Client()).isEqualTo(newClient);
            // close会关闭owned client
            oidcClient.close();
            oidcClient = null;
        }
    }

    @Nested
    @DisplayName("close方法测试")
    class CloseTests {

        @Test
        @DisplayName("close不拥有的client不关闭")
        void testCloseNotOwned() {
            oidcClient = OidcClient.builder()
                    .oauth2Client(oauth2Client)
                    .build();

            assertThatCode(() -> oidcClient.close()).doesNotThrowAnyException();
            // oauth2Client应该仍然可用
        }

        @Test
        @DisplayName("close拥有的client会关闭")
        void testCloseOwned() {
            OAuth2Client newClient = OAuth2Client.builder()
                    .config(OAuth2Config.builder()
                            .clientId("test")
                            .tokenEndpoint("https://token.example.com")
                            .build())
                    .build();

            oidcClient = OidcClient.builder()
                    .ownedOAuth2Client(newClient)
                    .build();

            assertThatCode(() -> oidcClient.close()).doesNotThrowAnyException();
            oidcClient = null;
        }
    }

    @Nested
    @DisplayName("AutoCloseable测试")
    class AutoCloseableTests {

        @Test
        @DisplayName("实现AutoCloseable接口")
        void testImplementsAutoCloseable() {
            assertThat(AutoCloseable.class.isAssignableFrom(OidcClient.class)).isTrue();
        }
    }
}
