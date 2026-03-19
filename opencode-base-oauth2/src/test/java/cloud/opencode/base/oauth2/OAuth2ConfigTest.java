package cloud.opencode.base.oauth2;

import cloud.opencode.base.oauth2.grant.GrantType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * OAuth2ConfigTest Tests
 * OAuth2ConfigTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("OAuth2Config 测试")
class OAuth2ConfigTest {

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("使用必需字段构建")
        void testBuildWithRequiredFields() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("client123")
                    .build();

            assertThat(config.clientId()).isEqualTo("client123");
        }

        @Test
        @DisplayName("使用所有字段构建")
        void testBuildWithAllFields() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("client123")
                    .clientSecret("secret456")
                    .authorizationEndpoint("https://auth.example.com/authorize")
                    .tokenEndpoint("https://auth.example.com/token")
                    .userInfoEndpoint("https://auth.example.com/userinfo")
                    .revocationEndpoint("https://auth.example.com/revoke")
                    .deviceAuthorizationEndpoint("https://auth.example.com/device")
                    .redirectUri("https://myapp.com/callback")
                    .scopes(Set.of("openid", "profile"))
                    .grantType(GrantType.AUTHORIZATION_CODE)
                    .connectTimeout(Duration.ofSeconds(10))
                    .readTimeout(Duration.ofSeconds(30))
                    .refreshThreshold(Duration.ofMinutes(5))
                    .build();

            assertThat(config.clientId()).isEqualTo("client123");
            assertThat(config.clientSecret()).isEqualTo("secret456");
            assertThat(config.authorizationEndpoint()).isEqualTo("https://auth.example.com/authorize");
            assertThat(config.tokenEndpoint()).isEqualTo("https://auth.example.com/token");
            assertThat(config.userInfoEndpoint()).isEqualTo("https://auth.example.com/userinfo");
            assertThat(config.revocationEndpoint()).isEqualTo("https://auth.example.com/revoke");
            assertThat(config.deviceAuthorizationEndpoint()).isEqualTo("https://auth.example.com/device");
            assertThat(config.redirectUri()).isEqualTo("https://myapp.com/callback");
            assertThat(config.scopes()).containsExactlyInAnyOrder("openid", "profile");
            assertThat(config.grantType()).isEqualTo(GrantType.AUTHORIZATION_CODE);
        }

        @Test
        @DisplayName("clientId为null抛出异常")
        void testClientIdNull() {
            assertThatThrownBy(() -> OAuth2Config.builder().build())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("scope方法")
        void testScope() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("client123")
                    .scope("openid")
                    .scope("profile")
                    .build();

            assertThat(config.scopes()).contains("openid", "profile");
        }
    }

    @Nested
    @DisplayName("流程检查方法测试")
    class FlowCheckTests {

        @Test
        @DisplayName("isAuthorizationCodeFlow")
        void testIsAuthorizationCodeFlow() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("client123")
                    .grantType(GrantType.AUTHORIZATION_CODE)
                    .build();

            assertThat(config.isAuthorizationCodeFlow()).isTrue();
        }

        @Test
        @DisplayName("isClientCredentialsFlow")
        void testIsClientCredentialsFlow() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("client123")
                    .grantType(GrantType.CLIENT_CREDENTIALS)
                    .build();

            assertThat(config.isClientCredentialsFlow()).isTrue();
        }

        @Test
        @DisplayName("isDeviceCodeFlow")
        void testIsDeviceCodeFlow() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("client123")
                    .grantType(GrantType.DEVICE_CODE)
                    .build();

            assertThat(config.isDeviceCodeFlow()).isTrue();
        }
    }

    @Nested
    @DisplayName("端点检查方法测试")
    class EndpointCheckTests {

        @Test
        @DisplayName("hasUserInfoEndpoint - 有")
        void testHasUserInfoEndpointTrue() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("client123")
                    .userInfoEndpoint("https://auth.example.com/userinfo")
                    .build();

            assertThat(config.hasUserInfoEndpoint()).isTrue();
        }

        @Test
        @DisplayName("hasUserInfoEndpoint - 无")
        void testHasUserInfoEndpointFalse() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("client123")
                    .build();

            assertThat(config.hasUserInfoEndpoint()).isFalse();
        }

        @Test
        @DisplayName("hasRevocationEndpoint - 有")
        void testHasRevocationEndpointTrue() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("client123")
                    .revocationEndpoint("https://auth.example.com/revoke")
                    .build();

            assertThat(config.hasRevocationEndpoint()).isTrue();
        }

        @Test
        @DisplayName("hasRevocationEndpoint - 无")
        void testHasRevocationEndpointFalse() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("client123")
                    .build();

            assertThat(config.hasRevocationEndpoint()).isFalse();
        }

        @Test
        @DisplayName("hasDeviceAuthorizationEndpoint - 有")
        void testHasDeviceAuthorizationEndpointTrue() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("client123")
                    .deviceAuthorizationEndpoint("https://auth.example.com/device")
                    .build();

            assertThat(config.hasDeviceAuthorizationEndpoint()).isTrue();
        }

        @Test
        @DisplayName("hasDeviceAuthorizationEndpoint - 无")
        void testHasDeviceAuthorizationEndpointFalse() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("client123")
                    .build();

            assertThat(config.hasDeviceAuthorizationEndpoint()).isFalse();
        }
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValuesTests {

        @Test
        @DisplayName("默认grantType")
        void testDefaultGrantType() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("client123")
                    .build();

            assertThat(config.grantType()).isEqualTo(GrantType.AUTHORIZATION_CODE);
        }

        @Test
        @DisplayName("默认超时")
        void testDefaultTimeouts() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("client123")
                    .build();

            assertThat(config.connectTimeout()).isNotNull();
            assertThat(config.readTimeout()).isNotNull();
            assertThat(config.refreshThreshold()).isNotNull();
        }

        @Test
        @DisplayName("默认scopes为空")
        void testDefaultScopesEmpty() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("client123")
                    .build();

            assertThat(config.scopes()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodsTests {

        @Test
        @DisplayName("equals和hashCode")
        void testEqualsAndHashCode() {
            OAuth2Config config1 = OAuth2Config.builder()
                    .clientId("client123")
                    .clientSecret("secret456")
                    .build();
            OAuth2Config config2 = OAuth2Config.builder()
                    .clientId("client123")
                    .clientSecret("secret456")
                    .build();

            assertThat(config1).isEqualTo(config2);
            assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
        }

        @Test
        @DisplayName("toString")
        void testToString() {
            OAuth2Config config = OAuth2Config.builder()
                    .clientId("client123")
                    .build();

            assertThat(config.toString()).contains("client123");
        }
    }
}
