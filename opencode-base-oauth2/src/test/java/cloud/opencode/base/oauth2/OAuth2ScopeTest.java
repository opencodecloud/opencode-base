package cloud.opencode.base.oauth2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * OAuth2ScopeTest Tests
 * OAuth2ScopeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("OAuth2Scope 测试")
class OAuth2ScopeTest {

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("OIDC标准scopes")
        void testOidcScopes() {
            assertThat(OAuth2Scope.OPENID).isEqualTo("openid");
            assertThat(OAuth2Scope.PROFILE).isEqualTo("profile");
            assertThat(OAuth2Scope.EMAIL).isEqualTo("email");
            assertThat(OAuth2Scope.ADDRESS).isEqualTo("address");
            assertThat(OAuth2Scope.PHONE).isEqualTo("phone");
            assertThat(OAuth2Scope.OFFLINE_ACCESS).isEqualTo("offline_access");
        }
    }

    @Nested
    @DisplayName("工具类测试")
    class UtilityClassTests {

        @Test
        @DisplayName("类是final的")
        void testFinalClass() {
            assertThat(java.lang.reflect.Modifier.isFinal(OAuth2Scope.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("构造函数私有")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = OAuth2Scope.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("combine方法测试")
    class CombineTests {

        @Test
        @DisplayName("combine多个scope返回Set")
        void testCombine() {
            Set<String> combined = OAuth2Scope.combine("openid", "profile", "email");
            assertThat(combined).containsExactlyInAnyOrder("openid", "profile", "email");
        }

        @Test
        @DisplayName("combine单个scope")
        void testCombineSingle() {
            Set<String> combined = OAuth2Scope.combine("openid");
            assertThat(combined).containsExactly("openid");
        }

        @Test
        @DisplayName("combine空数组")
        void testCombineEmpty() {
            Set<String> combined = OAuth2Scope.combine(new String[0]);
            assertThat(combined).isEmpty();
        }

        @Test
        @DisplayName("combine多个Set")
        void testCombineSets() {
            Set<String> set1 = Set.of("openid", "profile");
            Set<String> set2 = Set.of("email", "phone");
            Set<String> combined = OAuth2Scope.combine(set1, set2);
            assertThat(combined).containsExactlyInAnyOrder("openid", "profile", "email", "phone");
        }
    }

    @Nested
    @DisplayName("parse方法测试")
    class ParseTests {

        @Test
        @DisplayName("parse空格分隔的scope字符串")
        void testParse() {
            Set<String> scopes = OAuth2Scope.parse("openid profile email");
            assertThat(scopes).containsExactlyInAnyOrder("openid", "profile", "email");
        }

        @Test
        @DisplayName("parse单个scope")
        void testParseSingle() {
            Set<String> scopes = OAuth2Scope.parse("openid");
            assertThat(scopes).containsExactly("openid");
        }

        @Test
        @DisplayName("parse空字符串")
        void testParseEmpty() {
            Set<String> scopes = OAuth2Scope.parse("");
            assertThat(scopes).isEmpty();
        }

        @Test
        @DisplayName("parse null")
        void testParseNull() {
            Set<String> scopes = OAuth2Scope.parse(null);
            assertThat(scopes).isEmpty();
        }

        @Test
        @DisplayName("parse多个空格")
        void testParseMultipleSpaces() {
            Set<String> scopes = OAuth2Scope.parse("openid   profile    email");
            assertThat(scopes).containsExactlyInAnyOrder("openid", "profile", "email");
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString将Set转为空格分隔字符串")
        void testToStringSet() {
            Set<String> scopes = Set.of("openid", "profile");
            String result = OAuth2Scope.toString(scopes);
            assertThat(result).contains("openid");
            assertThat(result).contains("profile");
        }

        @Test
        @DisplayName("toString空Set")
        void testToStringEmptySet() {
            Set<String> scopes = Set.of();
            String result = OAuth2Scope.toString(scopes);
            assertThat(result).isEqualTo("");
        }

        @Test
        @DisplayName("toString可变参数")
        void testToStringVarargs() {
            String result = OAuth2Scope.toString("openid", "profile", "email");
            assertThat(result).isEqualTo("openid profile email");
        }
    }

    @Nested
    @DisplayName("defaultOidc方法测试")
    class DefaultOidcTests {

        @Test
        @DisplayName("defaultOidc返回标准OIDC scopes")
        void testDefaultOidc() {
            Set<String> scopes = OAuth2Scope.defaultOidc();
            assertThat(scopes).contains("openid", "profile", "email");
        }
    }

    @Nested
    @DisplayName("Google Scopes测试")
    class GoogleScopesTests {

        @Test
        @DisplayName("Google scope常量")
        void testGoogleScopes() {
            assertThat(OAuth2Scope.Google.DRIVE).isNotNull();
            assertThat(OAuth2Scope.Google.DRIVE_READONLY).isNotNull();
            assertThat(OAuth2Scope.Google.GMAIL_READONLY).isNotNull();
            assertThat(OAuth2Scope.Google.GMAIL_SEND).isNotNull();
            assertThat(OAuth2Scope.Google.GMAIL_FULL).isNotNull();
            assertThat(OAuth2Scope.Google.GMAIL_MODIFY).isNotNull();
            assertThat(OAuth2Scope.Google.CALENDAR).isNotNull();
            assertThat(OAuth2Scope.Google.CALENDAR_READONLY).isNotNull();
        }

        @Test
        @DisplayName("Google scope值")
        void testGoogleScopeValues() {
            assertThat(OAuth2Scope.Google.GMAIL_READONLY).contains("googleapis.com");
            assertThat(OAuth2Scope.Google.DRIVE).contains("googleapis.com");
        }
    }

    @Nested
    @DisplayName("Microsoft Scopes测试")
    class MicrosoftScopesTests {

        @Test
        @DisplayName("Microsoft scope常量")
        void testMicrosoftScopes() {
            assertThat(OAuth2Scope.Microsoft.USER_READ).isNotNull();
            assertThat(OAuth2Scope.Microsoft.USER_READ_ALL).isNotNull();
            assertThat(OAuth2Scope.Microsoft.MAIL_READ).isNotNull();
            assertThat(OAuth2Scope.Microsoft.MAIL_SEND).isNotNull();
            assertThat(OAuth2Scope.Microsoft.MAIL_READWRITE).isNotNull();
            assertThat(OAuth2Scope.Microsoft.CALENDARS_READ).isNotNull();
            assertThat(OAuth2Scope.Microsoft.CALENDARS_READWRITE).isNotNull();
            assertThat(OAuth2Scope.Microsoft.FILES_READ).isNotNull();
            assertThat(OAuth2Scope.Microsoft.FILES_READWRITE).isNotNull();
            assertThat(OAuth2Scope.Microsoft.DEFAULT).isNotNull();
        }

        @Test
        @DisplayName("Microsoft scope值")
        void testMicrosoftScopeValues() {
            assertThat(OAuth2Scope.Microsoft.USER_READ).isEqualTo("User.Read");
            assertThat(OAuth2Scope.Microsoft.DEFAULT).isEqualTo(".default");
        }
    }

    @Nested
    @DisplayName("GitHub Scopes测试")
    class GitHubScopesTests {

        @Test
        @DisplayName("GitHub scope常量")
        void testGitHubScopes() {
            assertThat(OAuth2Scope.GitHub.USER).isNotNull();
            assertThat(OAuth2Scope.GitHub.USER_EMAIL).isNotNull();
            assertThat(OAuth2Scope.GitHub.READ_USER).isNotNull();
            assertThat(OAuth2Scope.GitHub.REPO).isNotNull();
            assertThat(OAuth2Scope.GitHub.PUBLIC_REPO).isNotNull();
            assertThat(OAuth2Scope.GitHub.READ_ORG).isNotNull();
            assertThat(OAuth2Scope.GitHub.GIST).isNotNull();
        }

        @Test
        @DisplayName("GitHub scope值")
        void testGitHubScopeValues() {
            assertThat(OAuth2Scope.GitHub.USER).isEqualTo("user");
            assertThat(OAuth2Scope.GitHub.USER_EMAIL).isEqualTo("user:email");
            assertThat(OAuth2Scope.GitHub.REPO).isEqualTo("repo");
        }
    }
}
