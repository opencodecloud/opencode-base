package cloud.opencode.base.oauth2;

import cloud.opencode.base.oauth2.oidc.JwtClaims;
import cloud.opencode.base.oauth2.pkce.PkceChallenge;
import cloud.opencode.base.oauth2.token.FileTokenStore;
import cloud.opencode.base.oauth2.token.InMemoryTokenStore;
import cloud.opencode.base.oauth2.token.TokenStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenOAuth2Test Tests
 * OpenOAuth2Test 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("OpenOAuth2 测试")
class OpenOAuth2Test {

    private OAuth2Client client;

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.close();
        }
    }

    @Nested
    @DisplayName("工具类测试")
    class UtilityClassTests {

        @Test
        @DisplayName("类是final的")
        void testFinalClass() {
            assertThat(java.lang.reflect.Modifier.isFinal(OpenOAuth2.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("构造函数私有")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = OpenOAuth2.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("Provider工厂方法测试")
    class ProviderFactoryMethodsTests {

        @Test
        @DisplayName("google()创建Google客户端构建器")
        void testGoogle() {
            OAuth2Client.Builder builder = OpenOAuth2.google("client-id", "client-secret");

            assertThat(builder).isNotNull();

            client = builder
                    .redirectUri("https://app.example.com/callback")
                    .build();

            assertThat(client.config().clientId()).isEqualTo("client-id");
            assertThat(client.config().authorizationEndpoint()).contains("google.com");
        }

        @Test
        @DisplayName("microsoft()创建Microsoft客户端构建器")
        void testMicrosoft() {
            OAuth2Client.Builder builder = OpenOAuth2.microsoft("client-id", "client-secret");

            assertThat(builder).isNotNull();

            client = builder
                    .redirectUri("https://app.example.com/callback")
                    .build();

            assertThat(client.config().clientId()).isEqualTo("client-id");
            assertThat(client.config().authorizationEndpoint()).contains("microsoft");
        }

        @Test
        @DisplayName("microsoft(tenantId)创建特定租户的Microsoft客户端构建器")
        void testMicrosoftWithTenant() {
            OAuth2Client.Builder builder = OpenOAuth2.microsoft("tenant-123", "client-id", "client-secret");

            assertThat(builder).isNotNull();

            client = builder
                    .redirectUri("https://app.example.com/callback")
                    .build();

            assertThat(client.config().clientId()).isEqualTo("client-id");
            assertThat(client.config().authorizationEndpoint()).contains("tenant-123");
        }

        @Test
        @DisplayName("github()创建GitHub客户端构建器")
        void testGithub() {
            OAuth2Client.Builder builder = OpenOAuth2.github("client-id", "client-secret");

            assertThat(builder).isNotNull();

            client = builder
                    .redirectUri("https://app.example.com/callback")
                    .build();

            assertThat(client.config().clientId()).isEqualTo("client-id");
            assertThat(client.config().authorizationEndpoint()).contains("github.com");
        }

        @Test
        @DisplayName("apple()创建Apple客户端构建器")
        void testApple() {
            OAuth2Client.Builder builder = OpenOAuth2.apple("client-id", "client-secret");

            assertThat(builder).isNotNull();

            client = builder
                    .redirectUri("https://app.example.com/callback")
                    .build();

            assertThat(client.config().clientId()).isEqualTo("client-id");
            assertThat(client.config().authorizationEndpoint()).contains("apple.com");
        }

        @Test
        @DisplayName("facebook()创建Facebook客户端构建器")
        void testFacebook() {
            OAuth2Client.Builder builder = OpenOAuth2.facebook("client-id", "client-secret");

            assertThat(builder).isNotNull();

            client = builder
                    .redirectUri("https://app.example.com/callback")
                    .build();

            assertThat(client.config().clientId()).isEqualTo("client-id");
            assertThat(client.config().authorizationEndpoint()).contains("facebook.com");
        }
    }

    @Nested
    @DisplayName("自定义Provider测试")
    class CustomProviderTests {

        @Test
        @DisplayName("client()创建自定义客户端构建器")
        void testClient() {
            OAuth2Client.Builder builder = OpenOAuth2.client();

            assertThat(builder).isNotNull();

            client = builder
                    .clientId("client-id")
                    .clientSecret("client-secret")
                    .build();

            assertThat(client).isNotNull();
        }

        @Test
        @DisplayName("client(provider)创建带有provider的客户端构建器")
        void testClientWithProvider() {
            OAuth2Client.Builder builder = OpenOAuth2.client(cloud.opencode.base.oauth2.provider.Providers.GOOGLE);

            assertThat(builder).isNotNull();

            client = builder
                    .clientId("client-id")
                    .clientSecret("client-secret")
                    .redirectUri("https://app.example.com/callback")
                    .build();

            assertThat(client.config().authorizationEndpoint()).contains("google.com");
        }

        @Test
        @DisplayName("fromConfig()从配置创建客户端")
        void testFromConfig() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("client-id")
                    .clientSecret("client-secret")
                    .tokenEndpoint("https://auth.example.com/token")
                    .build();

            client = OpenOAuth2.fromConfig(config);

            assertThat(client).isNotNull();
            assertThat(client.config()).isEqualTo(config);
        }
    }

    @Nested
    @DisplayName("PKCE测试")
    class PkceTests {

        @Test
        @DisplayName("generatePkce()生成PKCE挑战")
        void testGeneratePkce() {
            PkceChallenge pkce = OpenOAuth2.generatePkce();

            assertThat(pkce).isNotNull();
            assertThat(pkce.verifier()).isNotNull();
            assertThat(pkce.challenge()).isNotNull();
            assertThat(pkce.method()).isEqualTo("S256");
        }

        @Test
        @DisplayName("每次生成不同的PKCE")
        void testGeneratePkceUnique() {
            PkceChallenge pkce1 = OpenOAuth2.generatePkce();
            PkceChallenge pkce2 = OpenOAuth2.generatePkce();

            assertThat(pkce1.verifier()).isNotEqualTo(pkce2.verifier());
            assertThat(pkce1.challenge()).isNotEqualTo(pkce2.challenge());
        }
    }

    @Nested
    @DisplayName("JWT测试")
    class JwtTests {

        @Test
        @DisplayName("parseJwt()解析JWT")
        void testParseJwt() {
            String payload = """
                    {"sub":"user123","iss":"https://issuer.com","exp":9999999999}
                    """;
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payload.getBytes());
            String jwt = "header." + encodedPayload + ".signature";

            JwtClaims claims = OpenOAuth2.parseJwt(jwt);

            assertThat(claims.sub()).isEqualTo("user123");
            assertThat(claims.iss()).isEqualTo("https://issuer.com");
        }

        @Test
        @DisplayName("isExpired()检查token是否过期")
        void testIsExpired() {
            OAuth2Token expiredToken = OAuth2Token.builder()
                    .accessToken("access123")
                    .expiresAt(Instant.now().minusSeconds(60))
                    .build();

            OAuth2Token validToken = OAuth2Token.builder()
                    .accessToken("access123")
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            assertThat(OpenOAuth2.isExpired(expiredToken)).isTrue();
            assertThat(OpenOAuth2.isExpired(validToken)).isFalse();
        }

        @Test
        @DisplayName("isExpiringSoon()检查token是否即将过期")
        void testIsExpiringSoon() {
            OAuth2Token soonExpiring = OAuth2Token.builder()
                    .accessToken("access123")
                    .expiresAt(Instant.now().plusSeconds(60))
                    .build();

            OAuth2Token notExpiringSoon = OAuth2Token.builder()
                    .accessToken("access123")
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            assertThat(OpenOAuth2.isExpiringSoon(soonExpiring, Duration.ofMinutes(5))).isTrue();
            assertThat(OpenOAuth2.isExpiringSoon(notExpiringSoon, Duration.ofMinutes(5))).isFalse();
        }
    }

    @Nested
    @DisplayName("Token Store测试")
    class TokenStoreTests {

        @Test
        @DisplayName("inMemoryTokenStore()创建内存存储")
        void testInMemoryTokenStore() {
            TokenStore store = OpenOAuth2.inMemoryTokenStore();

            assertThat(store).isNotNull();
            assertThat(store).isInstanceOf(InMemoryTokenStore.class);
        }

        @Test
        @DisplayName("fileTokenStore(Path)创建文件存储")
        void testFileTokenStorePath(@TempDir Path tempDir) {
            TokenStore store = OpenOAuth2.fileTokenStore(tempDir);

            assertThat(store).isNotNull();
            assertThat(store).isInstanceOf(FileTokenStore.class);
        }

        @Test
        @DisplayName("fileTokenStore(String)创建用户目录下的文件存储")
        void testFileTokenStoreAppName() {
            TokenStore store = OpenOAuth2.fileTokenStore("test-app");

            assertThat(store).isNotNull();
            assertThat(store).isInstanceOf(FileTokenStore.class);
        }
    }

    @Nested
    @DisplayName("Configuration Builder测试")
    class ConfigBuilderTests {

        @Test
        @DisplayName("configBuilder()创建配置构建器")
        void testConfigBuilder() {
            OAuth2Config.Builder builder = OpenOAuth2.configBuilder();

            assertThat(builder).isNotNull();

            OAuth2Config config = builder
                    .clientId("client-id")
                    .clientSecret("client-secret")
                    .tokenEndpoint("https://auth.example.com/token")
                    .build();

            assertThat(config.clientId()).isEqualTo("client-id");
        }
    }
}
