package cloud.opencode.base.oauth2.provider;

import cloud.opencode.base.oauth2.OAuth2Config;
import cloud.opencode.base.oauth2.grant.GrantType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * OAuth2ProviderTest Tests
 * OAuth2ProviderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("OAuth2Provider 测试")
class OAuth2ProviderTest {

    @Nested
    @DisplayName("接口方法测试")
    class InterfaceMethodsTests {

        @Test
        @DisplayName("默认方法返回正确值")
        void testDefaultMethods() {
            OAuth2Provider provider = new TestProvider();

            assertThat(provider.userInfoEndpoint()).isNull();
            assertThat(provider.revocationEndpoint()).isNull();
            assertThat(provider.deviceAuthorizationEndpoint()).isNull();
            assertThat(provider.defaultScopes()).isEmpty();
            assertThat(provider.requiresPkce()).isFalse();
            assertThat(provider.supportsDeviceCode()).isFalse();
        }
    }

    @Nested
    @DisplayName("toConfig方法测试")
    class ToConfigTests {

        @Test
        @DisplayName("toConfig创建正确的配置")
        void testToConfig() {
            OAuth2Provider provider = new TestProvider();

            OAuth2Config config = provider.toConfig(
                    "client-id",
                    "client-secret",
                    "https://myapp.com/callback",
                    Set.of("email"),
                    GrantType.AUTHORIZATION_CODE
            );

            assertThat(config.clientId()).isEqualTo("client-id");
            assertThat(config.clientSecret()).isEqualTo("client-secret");
            assertThat(config.redirectUri()).isEqualTo("https://myapp.com/callback");
            assertThat(config.authorizationEndpoint()).isEqualTo("https://auth.test.com/authorize");
            assertThat(config.tokenEndpoint()).isEqualTo("https://auth.test.com/token");
            assertThat(config.grantType()).isEqualTo(GrantType.AUTHORIZATION_CODE);
        }

        @Test
        @DisplayName("toConfig合并默认scopes和额外scopes")
        void testToConfigMergesScopes() {
            OAuth2Provider provider = new ProviderWithScopes();

            OAuth2Config config = provider.toConfig(
                    "client-id",
                    "client-secret",
                    "https://myapp.com/callback",
                    Set.of("email"),
                    GrantType.AUTHORIZATION_CODE
            );

            assertThat(config.scopes()).contains("openid", "profile", "email");
        }
    }

    // 测试用Provider实现
    private static class TestProvider implements OAuth2Provider {
        @Override
        public String name() {
            return "TestProvider";
        }

        @Override
        public String authorizationEndpoint() {
            return "https://auth.test.com/authorize";
        }

        @Override
        public String tokenEndpoint() {
            return "https://auth.test.com/token";
        }
    }

    private static class ProviderWithScopes implements OAuth2Provider {
        @Override
        public String name() {
            return "ProviderWithScopes";
        }

        @Override
        public String authorizationEndpoint() {
            return "https://auth.test.com/authorize";
        }

        @Override
        public String tokenEndpoint() {
            return "https://auth.test.com/token";
        }

        @Override
        public Set<String> defaultScopes() {
            return Set.of("openid", "profile");
        }
    }
}
