package cloud.opencode.base.oauth2;

import cloud.opencode.base.oauth2.exception.OAuth2Exception;
import cloud.opencode.base.oauth2.grant.GrantType;
import cloud.opencode.base.oauth2.pkce.PkceChallenge;
import cloud.opencode.base.oauth2.provider.Providers;
import cloud.opencode.base.oauth2.token.InMemoryTokenStore;
import cloud.opencode.base.oauth2.token.TokenStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * OAuth2ClientTest Tests
 * OAuth2ClientTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("OAuth2Client 测试")
class OAuth2ClientTest {

    private OAuth2Client client;

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.close();
        }
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用config创建")
        void testConstructorWithConfig() {
            OAuth2Config config = createBasicConfig();

            client = OAuth2Client.builder()
                    .config(config)
                    .build();

            assertThat(client).isNotNull();
            assertThat(client.config()).isEqualTo(config);
        }

        @Test
        @DisplayName("使用provider创建")
        void testConstructorWithProvider() {
            client = OAuth2Client.builder()
                    .provider(Providers.GOOGLE)
                    .clientId("test-client")
                    .clientSecret("test-secret")
                    .build();

            assertThat(client).isNotNull();
            assertThat(client.config().clientId()).isEqualTo("test-client");
        }

        @Test
        @DisplayName("使用个别参数创建")
        void testConstructorWithIndividualParams() {
            client = OAuth2Client.builder()
                    .clientId("test-client")
                    .clientSecret("test-secret")
                    .redirectUri("https://app.example.com/callback")
                    .scopes("openid", "profile")
                    .grantType(GrantType.AUTHORIZATION_CODE)
                    .build();

            assertThat(client).isNotNull();
            assertThat(client.config().clientId()).isEqualTo("test-client");
            assertThat(client.config().scopes()).contains("openid", "profile");
        }

        @Test
        @DisplayName("config优先于provider")
        void testConfigTakesPrecedence() {
            OAuth2Config config = createBasicConfig();

            client = OAuth2Client.builder()
                    .config(config)
                    .provider(Providers.GOOGLE)
                    .clientId("ignored")
                    .build();

            assertThat(client.config()).isEqualTo(config);
        }

        @Test
        @DisplayName("缺少clientId抛出异常")
        void testMissingClientId() {
            assertThatThrownBy(() -> OAuth2Client.builder().build())
                    .isInstanceOf(OAuth2Exception.class);
        }
    }

    @Nested
    @DisplayName("getAuthorizationUrl方法测试")
    class GetAuthorizationUrlTests {

        @Test
        @DisplayName("生成基本授权URL")
        void testGetAuthorizationUrl() {
            client = createClient();

            String url = client.getAuthorizationUrl("state123");

            assertThat(url).startsWith("https://auth.example.com/authorize");
            assertThat(url).contains("response_type=code");
            assertThat(url).contains("client_id=test-client");
            assertThat(url).contains("state=state123");
        }

        @Test
        @DisplayName("生成带有PKCE的授权URL")
        void testGetAuthorizationUrlWithPkce() {
            client = createClient();
            PkceChallenge pkce = PkceChallenge.generate();

            String url = client.getAuthorizationUrl("state123", pkce);

            assertThat(url).contains("code_challenge=");
            assertThat(url).contains("code_challenge_method=S256");
        }

        @Test
        @DisplayName("生成带有附加参数的授权URL")
        void testGetAuthorizationUrlWithAdditionalParams() {
            client = createClient();
            PkceChallenge pkce = PkceChallenge.generate();
            Map<String, String> additionalParams = Map.of("nonce", "nonce123", "prompt", "consent");

            String url = client.getAuthorizationUrl("state123", pkce, additionalParams);

            assertThat(url).contains("nonce=nonce123");
            assertThat(url).contains("prompt=consent");
        }

        @Test
        @DisplayName("包含redirect_uri")
        void testGetAuthorizationUrlWithRedirectUri() {
            client = createClient();

            String url = client.getAuthorizationUrl("state123");

            assertThat(url).contains("redirect_uri=");
        }

        @Test
        @DisplayName("包含scopes")
        void testGetAuthorizationUrlWithScopes() {
            client = createClient();

            String url = client.getAuthorizationUrl("state123");

            assertThat(url).contains("scope=");
        }

        @Test
        @DisplayName("无授权端点抛出异常")
        void testGetAuthorizationUrlNoEndpoint() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("test-client")
                    .tokenEndpoint("https://token.example.com")
                    .build();

            client = OAuth2Client.builder()
                    .config(config)
                    .build();

            assertThatThrownBy(() -> client.getAuthorizationUrl("state123"))
                    .isInstanceOf(OAuth2Exception.class);
        }
    }

    @Nested
    @DisplayName("exchangeCode方法测试")
    class ExchangeCodeTests {

        @Test
        @DisplayName("code为null抛出异常")
        void testExchangeCodeNullCode() {
            client = createClient();

            assertThatThrownBy(() -> client.exchangeCode(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getClientCredentialsToken方法测试")
    class GetClientCredentialsTokenTests {

        @Test
        @DisplayName("无clientSecret抛出异常")
        void testGetClientCredentialsTokenNoSecret() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("test-client")
                    .tokenEndpoint("https://token.example.com")
                    .build();

            client = OAuth2Client.builder()
                    .config(config)
                    .build();

            assertThatThrownBy(() -> client.getClientCredentialsToken())
                    .isInstanceOf(OAuth2Exception.class);
        }
    }

    @Nested
    @DisplayName("requestDeviceCode方法测试")
    class RequestDeviceCodeTests {

        @Test
        @DisplayName("无设备授权端点抛出异常")
        void testRequestDeviceCodeNoEndpoint() {
            client = createClient();

            assertThatThrownBy(() -> client.requestDeviceCode())
                    .isInstanceOf(OAuth2Exception.class);
        }
    }

    @Nested
    @DisplayName("pollDeviceToken方法测试")
    class PollDeviceTokenTests {

        @Test
        @DisplayName("deviceCode为null抛出异常")
        void testPollDeviceTokenNullCode() {
            client = createClient();

            assertThatThrownBy(() -> client.pollDeviceToken(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("refreshToken方法测试")
    class RefreshTokenTests {

        @Test
        @DisplayName("refreshToken字符串为null抛出异常")
        void testRefreshTokenNullString() {
            client = createClient();

            assertThatThrownBy(() -> client.refreshToken((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("token无refreshToken抛出异常")
        void testRefreshTokenNoRefreshToken() {
            client = createClient();
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .build();

            assertThatThrownBy(() -> client.refreshToken(token))
                    .isInstanceOf(OAuth2Exception.class);
        }
    }

    @Nested
    @DisplayName("revokeToken方法测试")
    class RevokeTokenTests {

        @Test
        @DisplayName("无撤销端点抛出异常")
        void testRevokeTokenNoEndpoint() {
            client = createClient();

            assertThatThrownBy(() -> client.revokeToken("token123"))
                    .isInstanceOf(OAuth2Exception.class);
        }
    }

    @Nested
    @DisplayName("getUserInfo方法测试")
    class GetUserInfoTests {

        @Test
        @DisplayName("无userInfo端点抛出异常")
        void testGetUserInfoNoEndpoint() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("test-client")
                    .tokenEndpoint("https://token.example.com")
                    .build();

            client = OAuth2Client.builder()
                    .config(config)
                    .build();

            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .build();

            assertThatThrownBy(() -> client.getUserInfo(token))
                    .isInstanceOf(OAuth2Exception.class);
        }
    }

    @Nested
    @DisplayName("Token存储测试")
    class TokenStoreTests {

        @Test
        @DisplayName("存储和获取token")
        void testStoreAndGetToken() {
            client = createClient();
            OAuth2Token token = createToken();

            client.storeToken("user1", token);

            Optional<OAuth2Token> retrieved = client.getStoredToken("user1");
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().accessToken()).isEqualTo(token.accessToken());
        }

        @Test
        @DisplayName("获取不存在的token")
        void testGetStoredTokenNotFound() {
            client = createClient();

            Optional<OAuth2Token> retrieved = client.getStoredToken("nonexistent");
            assertThat(retrieved).isEmpty();
        }

        @Test
        @DisplayName("移除token")
        void testRemoveToken() {
            client = createClient();
            OAuth2Token token = createToken();

            client.storeToken("user1", token);
            client.removeToken("user1");

            assertThat(client.getStoredToken("user1")).isEmpty();
        }

        @Test
        @DisplayName("getValidToken - token不存在抛出异常")
        void testGetValidTokenNotFound() {
            client = createClient();

            assertThatThrownBy(() -> client.getValidToken("nonexistent"))
                    .isInstanceOf(OAuth2Exception.class);
        }

        @Test
        @DisplayName("getValidToken - token已过期且无refreshToken抛出异常")
        void testGetValidTokenExpiredNoRefresh() {
            client = createClient();
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .expiresAt(Instant.now().minusSeconds(60))
                    .build();

            client.storeToken("user1", token);

            assertThatThrownBy(() -> client.getValidToken("user1"))
                    .isInstanceOf(OAuth2Exception.class);
        }

        @Test
        @DisplayName("使用自定义TokenStore")
        void testCustomTokenStore() {
            TokenStore tokenStore = new InMemoryTokenStore();
            OAuth2Config config = createBasicConfig();

            client = OAuth2Client.builder()
                    .config(config)
                    .tokenStore(tokenStore)
                    .build();

            OAuth2Token token = createToken();
            client.storeToken("user1", token);

            // 验证使用了自定义store
            assertThat(tokenStore.load("user1")).isPresent();
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("builder()创建构建器")
        void testBuilder() {
            OAuth2Client.Builder builder = OAuth2Client.builder();
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("链式调用")
        void testBuilderChaining() {
            client = OAuth2Client.builder()
                    .provider(Providers.GITHUB)
                    .clientId("client-id")
                    .clientSecret("client-secret")
                    .redirectUri("https://app.example.com/callback")
                    .scopes("read:user", "user:email")
                    .grantType(GrantType.AUTHORIZATION_CODE)
                    .tokenStore(new InMemoryTokenStore())
                    .build();

            assertThat(client).isNotNull();
        }
    }

    @Nested
    @DisplayName("AutoCloseable测试")
    class AutoCloseableTests {

        @Test
        @DisplayName("实现AutoCloseable接口")
        void testImplementsAutoCloseable() {
            assertThat(AutoCloseable.class.isAssignableFrom(OAuth2Client.class)).isTrue();
        }

        @Test
        @DisplayName("close不抛出异常")
        void testClose() {
            client = createClient();
            assertThatCode(() -> client.close()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("多次close不抛出异常")
        void testMultipleClose() {
            client = createClient();
            assertThatCode(() -> {
                client.close();
                client.close();
            }).doesNotThrowAnyException();
        }
    }

    // Helper methods

    private OAuth2Config createBasicConfig() {
        return OAuth2Config.builder()
                .clientId("test-client")
                .clientSecret("test-secret")
                .authorizationEndpoint("https://auth.example.com/authorize")
                .tokenEndpoint("https://auth.example.com/token")
                .redirectUri("https://app.example.com/callback")
                .scopes("openid", "profile")
                .build();
    }

    private OAuth2Client createClient() {
        return OAuth2Client.builder()
                .config(createBasicConfig())
                .build();
    }

    private OAuth2Token createToken() {
        return OAuth2Token.builder()
                .accessToken("access123")
                .refreshToken("refresh123")
                .expiresIn(3600)
                .build();
    }
}
