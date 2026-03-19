package cloud.opencode.base.oauth2.provider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ProvidersTest Tests
 * ProvidersTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("Providers 测试")
class ProvidersTest {

    @Nested
    @DisplayName("工具类测试")
    class UtilityClassTests {

        @Test
        @DisplayName("类是final的")
        void testFinalClass() {
            assertThat(java.lang.reflect.Modifier.isFinal(Providers.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("构造函数私有")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = Providers.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("Google Provider测试")
    class GoogleProviderTests {

        @Test
        @DisplayName("GOOGLE常量存在")
        void testGoogleConstant() {
            assertThat(Providers.GOOGLE).isNotNull();
        }

        @Test
        @DisplayName("Google name正确")
        void testGoogleName() {
            assertThat(Providers.GOOGLE.name()).isEqualTo("Google");
        }

        @Test
        @DisplayName("Google endpoints正确")
        void testGoogleEndpoints() {
            assertThat(Providers.GOOGLE.authorizationEndpoint()).contains("google");
            assertThat(Providers.GOOGLE.tokenEndpoint()).contains("googleapis.com");
            assertThat(Providers.GOOGLE.userInfoEndpoint()).contains("google");
        }

        @Test
        @DisplayName("Google默认scopes包含openid")
        void testGoogleDefaultScopes() {
            assertThat(Providers.GOOGLE.defaultScopes()).contains("openid");
        }
    }

    @Nested
    @DisplayName("Microsoft Provider测试")
    class MicrosoftProviderTests {

        @Test
        @DisplayName("MICROSOFT常量存在")
        void testMicrosoftConstant() {
            assertThat(Providers.MICROSOFT).isNotNull();
        }

        @Test
        @DisplayName("Microsoft name正确")
        void testMicrosoftName() {
            assertThat(Providers.MICROSOFT.name()).isEqualTo("Microsoft");
        }

        @Test
        @DisplayName("Microsoft endpoints正确")
        void testMicrosoftEndpoints() {
            assertThat(Providers.MICROSOFT.authorizationEndpoint()).contains("microsoft");
            assertThat(Providers.MICROSOFT.tokenEndpoint()).contains("microsoft");
        }
    }

    @Nested
    @DisplayName("GitHub Provider测试")
    class GitHubProviderTests {

        @Test
        @DisplayName("GITHUB常量存在")
        void testGitHubConstant() {
            assertThat(Providers.GITHUB).isNotNull();
        }

        @Test
        @DisplayName("GitHub name正确")
        void testGitHubName() {
            assertThat(Providers.GITHUB.name()).isEqualTo("GitHub");
        }

        @Test
        @DisplayName("GitHub endpoints正确")
        void testGitHubEndpoints() {
            assertThat(Providers.GITHUB.authorizationEndpoint()).contains("github.com");
            assertThat(Providers.GITHUB.tokenEndpoint()).contains("github.com");
        }
    }

    @Nested
    @DisplayName("Apple Provider测试")
    class AppleProviderTests {

        @Test
        @DisplayName("APPLE常量存在")
        void testAppleConstant() {
            assertThat(Providers.APPLE).isNotNull();
        }

        @Test
        @DisplayName("Apple name正确")
        void testAppleName() {
            assertThat(Providers.APPLE.name()).isEqualTo("Apple");
        }

        @Test
        @DisplayName("Apple endpoints正确")
        void testAppleEndpoints() {
            assertThat(Providers.APPLE.authorizationEndpoint()).contains("apple.com");
            assertThat(Providers.APPLE.tokenEndpoint()).contains("apple.com");
        }
    }

    @Nested
    @DisplayName("Facebook Provider测试")
    class FacebookProviderTests {

        @Test
        @DisplayName("FACEBOOK常量存在")
        void testFacebookConstant() {
            assertThat(Providers.FACEBOOK).isNotNull();
        }

        @Test
        @DisplayName("Facebook name正确")
        void testFacebookName() {
            assertThat(Providers.FACEBOOK.name()).isEqualTo("Facebook");
        }

        @Test
        @DisplayName("Facebook endpoints正确")
        void testFacebookEndpoints() {
            assertThat(Providers.FACEBOOK.authorizationEndpoint()).contains("facebook.com");
            assertThat(Providers.FACEBOOK.tokenEndpoint()).contains("facebook.com");
        }
    }

    @Nested
    @DisplayName("microsoftTenant方法测试")
    class MicrosoftTenantTests {

        @Test
        @DisplayName("microsoftTenant创建租户特定的provider")
        void testMicrosoftTenant() {
            OAuth2Provider provider = Providers.microsoftTenant("my-tenant-id");

            assertThat(provider).isNotNull();
            assertThat(provider.authorizationEndpoint()).contains("my-tenant-id");
            assertThat(provider.tokenEndpoint()).contains("my-tenant-id");
        }

        @Test
        @DisplayName("microsoftTenant使用organizations")
        void testMicrosoftTenantOrganizations() {
            OAuth2Provider provider = Providers.microsoftTenant("organizations");

            assertThat(provider.authorizationEndpoint()).contains("organizations");
        }
    }
}
