package cloud.opencode.base.oauth2.provider;

import cloud.opencode.base.oauth2.OAuth2Config;
import cloud.opencode.base.oauth2.grant.GrantType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * CustomProviderTest Tests
 * CustomProviderTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("CustomProvider 测试")
class CustomProviderTest {

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("使用必需字段构建")
        void testBuildWithRequiredFields() {
            CustomProvider provider = CustomProvider.builder()
                    .name("MyProvider")
                    .tokenEndpoint("https://auth.example.com/token")
                    .build();

            assertThat(provider.name()).isEqualTo("MyProvider");
            assertThat(provider.tokenEndpoint()).isEqualTo("https://auth.example.com/token");
        }

        @Test
        @DisplayName("使用所有字段构建")
        void testBuildWithAllFields() {
            CustomProvider provider = CustomProvider.builder()
                    .name("MyProvider")
                    .authorizationEndpoint("https://auth.example.com/authorize")
                    .tokenEndpoint("https://auth.example.com/token")
                    .userInfoEndpoint("https://auth.example.com/userinfo")
                    .revocationEndpoint("https://auth.example.com/revoke")
                    .deviceAuthorizationEndpoint("https://auth.example.com/device")
                    .jwksUri("https://auth.example.com/.well-known/jwks.json")
                    .issuer("https://auth.example.com")
                    .defaultScopes("openid", "profile")
                    .build();

            assertThat(provider.name()).isEqualTo("MyProvider");
            assertThat(provider.authorizationEndpoint()).isEqualTo("https://auth.example.com/authorize");
            assertThat(provider.tokenEndpoint()).isEqualTo("https://auth.example.com/token");
            assertThat(provider.userInfoEndpoint()).isEqualTo("https://auth.example.com/userinfo");
            assertThat(provider.revocationEndpoint()).isEqualTo("https://auth.example.com/revoke");
            assertThat(provider.deviceAuthorizationEndpoint()).isEqualTo("https://auth.example.com/device");
            assertThat(provider.jwksUri()).isEqualTo("https://auth.example.com/.well-known/jwks.json");
            assertThat(provider.issuer()).isEqualTo("https://auth.example.com");
            assertThat(provider.defaultScopes()).containsExactlyInAnyOrder("openid", "profile");
        }

        @Test
        @DisplayName("name为null抛出异常")
        void testNameNull() {
            assertThatThrownBy(() -> CustomProvider.builder()
                    .tokenEndpoint("https://auth.example.com/token")
                    .build())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("tokenEndpoint为null抛出异常")
        void testTokenEndpointNull() {
            assertThatThrownBy(() -> CustomProvider.builder()
                    .name("MyProvider")
                    .build())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("addScope方法")
        void testAddScope() {
            CustomProvider provider = CustomProvider.builder()
                    .name("MyProvider")
                    .tokenEndpoint("https://auth.example.com/token")
                    .addScope("openid")
                    .addScope("profile")
                    .build();

            assertThat(provider.defaultScopes()).contains("openid", "profile");
        }

        @Test
        @DisplayName("defaultScopes使用Set")
        void testDefaultScopesWithSet() {
            CustomProvider provider = CustomProvider.builder()
                    .name("MyProvider")
                    .tokenEndpoint("https://auth.example.com/token")
                    .defaultScopes(Set.of("scope1", "scope2"))
                    .build();

            assertThat(provider.defaultScopes()).containsExactlyInAnyOrder("scope1", "scope2");
        }
    }

    @Nested
    @DisplayName("from方法测试")
    class FromTests {

        @Test
        @DisplayName("from复制现有provider")
        void testFrom() {
            CustomProvider original = CustomProvider.builder()
                    .name("Original")
                    .authorizationEndpoint("https://auth.example.com/authorize")
                    .tokenEndpoint("https://auth.example.com/token")
                    .userInfoEndpoint("https://auth.example.com/userinfo")
                    .defaultScopes("openid")
                    .build();

            CustomProvider copy = CustomProvider.from(original)
                    .name("Copy")
                    .build();

            assertThat(copy.name()).isEqualTo("Copy");
            assertThat(copy.authorizationEndpoint()).isEqualTo(original.authorizationEndpoint());
            assertThat(copy.tokenEndpoint()).isEqualTo(original.tokenEndpoint());
            assertThat(copy.userInfoEndpoint()).isEqualTo(original.userInfoEndpoint());
        }
    }

    @Nested
    @DisplayName("toConfig方法测试")
    class ToConfigTests {

        @Test
        @DisplayName("toConfig创建正确的配置")
        void testToConfig() {
            CustomProvider provider = CustomProvider.builder()
                    .name("MyProvider")
                    .authorizationEndpoint("https://auth.example.com/authorize")
                    .tokenEndpoint("https://auth.example.com/token")
                    .userInfoEndpoint("https://auth.example.com/userinfo")
                    .revocationEndpoint("https://auth.example.com/revoke")
                    .defaultScopes("openid", "profile")
                    .build();

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
            assertThat(config.authorizationEndpoint()).isEqualTo("https://auth.example.com/authorize");
            assertThat(config.tokenEndpoint()).isEqualTo("https://auth.example.com/token");
            assertThat(config.userInfoEndpoint()).isEqualTo("https://auth.example.com/userinfo");
            assertThat(config.revocationEndpoint()).isEqualTo("https://auth.example.com/revoke");
            assertThat(config.scopes()).contains("openid", "profile", "email");
            assertThat(config.grantType()).isEqualTo(GrantType.AUTHORIZATION_CODE);
        }

        @Test
        @DisplayName("toConfig默认grantType为AUTHORIZATION_CODE")
        void testToConfigDefaultGrantType() {
            CustomProvider provider = CustomProvider.builder()
                    .name("MyProvider")
                    .tokenEndpoint("https://auth.example.com/token")
                    .build();

            OAuth2Config config = provider.toConfig("client-id", null, null, null, null);
            assertThat(config.grantType()).isEqualTo(GrantType.AUTHORIZATION_CODE);
        }
    }

    @Nested
    @DisplayName("equals和hashCode测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("equals比较name、authEndpoint、tokenEndpoint")
        void testEquals() {
            CustomProvider provider1 = CustomProvider.builder()
                    .name("MyProvider")
                    .authorizationEndpoint("https://auth.example.com/authorize")
                    .tokenEndpoint("https://auth.example.com/token")
                    .build();

            CustomProvider provider2 = CustomProvider.builder()
                    .name("MyProvider")
                    .authorizationEndpoint("https://auth.example.com/authorize")
                    .tokenEndpoint("https://auth.example.com/token")
                    .build();

            assertThat(provider1).isEqualTo(provider2);
            assertThat(provider1.hashCode()).isEqualTo(provider2.hashCode());
        }

        @Test
        @DisplayName("不同name不相等")
        void testNotEqualsDifferentName() {
            CustomProvider provider1 = CustomProvider.builder()
                    .name("Provider1")
                    .tokenEndpoint("https://auth.example.com/token")
                    .build();

            CustomProvider provider2 = CustomProvider.builder()
                    .name("Provider2")
                    .tokenEndpoint("https://auth.example.com/token")
                    .build();

            assertThat(provider1).isNotEqualTo(provider2);
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含关键信息")
        void testToString() {
            CustomProvider provider = CustomProvider.builder()
                    .name("MyProvider")
                    .authorizationEndpoint("https://auth.example.com/authorize")
                    .tokenEndpoint("https://auth.example.com/token")
                    .build();

            String str = provider.toString();
            assertThat(str).contains("MyProvider");
            assertThat(str).contains("auth.example.com");
        }
    }

    @Nested
    @DisplayName("不可变性测试")
    class ImmutabilityTests {

        @Test
        @DisplayName("defaultScopes是不可变的")
        void testDefaultScopesImmutable() {
            CustomProvider provider = CustomProvider.builder()
                    .name("MyProvider")
                    .tokenEndpoint("https://auth.example.com/token")
                    .defaultScopes("openid")
                    .build();

            Set<String> scopes = provider.defaultScopes();
            assertThatThrownBy(() -> scopes.add("new-scope"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
