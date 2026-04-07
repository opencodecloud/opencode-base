package cloud.opencode.base.oauth2.security;

import cloud.opencode.base.oauth2.exception.OAuth2ErrorCode;
import cloud.opencode.base.oauth2.exception.OAuth2Exception;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * IssuerValidator Tests
 * IssuerValidator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
@DisplayName("IssuerValidator 测试")
class IssuerValidatorTest {

    @Nested
    @DisplayName("validate方法测试")
    class ValidateTests {

        @Test
        @DisplayName("匹配的颁发者通过验证")
        void testMatchingIssuers() {
            assertThatCode(() ->
                    IssuerValidator.validate(
                            "https://auth.example.com",
                            "https://auth.example.com"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("不匹配的颁发者抛出ISSUER_MISMATCH")
        void testMismatchedIssuers() {
            assertThatThrownBy(() ->
                    IssuerValidator.validate(
                            "https://auth.example.com",
                            "https://evil.example.com"))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(ex -> {
                        OAuth2Exception oauthEx = (OAuth2Exception) ex;
                        assertThat(oauthEx.errorCode()).isEqualTo(OAuth2ErrorCode.ISSUER_MISMATCH);
                    });
        }

        @Test
        @DisplayName("null expectedIssuer跳过验证")
        void testNullExpectedIssuerSkipsValidation() {
            assertThatCode(() ->
                    IssuerValidator.validate(null, "https://any.example.com"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null expectedIssuer和null actualIssuer跳过验证")
        void testBothNullSkipsValidation() {
            assertThatCode(() ->
                    IssuerValidator.validate(null, null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null actualIssuer抛出ISSUER_MISMATCH")
        void testNullActualIssuerThrowsException() {
            assertThatThrownBy(() ->
                    IssuerValidator.validate("https://auth.example.com", null))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(ex -> {
                        OAuth2Exception oauthEx = (OAuth2Exception) ex;
                        assertThat(oauthEx.errorCode()).isEqualTo(OAuth2ErrorCode.ISSUER_MISMATCH);
                        assertThat(oauthEx.getMessage()).contains("Issuer is missing");
                    });
        }

        @Test
        @DisplayName("大小写敏感比较")
        void testCaseSensitive() {
            assertThatThrownBy(() ->
                    IssuerValidator.validate(
                            "https://Auth.Example.com",
                            "https://auth.example.com"))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(ex -> {
                        OAuth2Exception oauthEx = (OAuth2Exception) ex;
                        assertThat(oauthEx.errorCode()).isEqualTo(OAuth2ErrorCode.ISSUER_MISMATCH);
                    });
        }

        @Test
        @DisplayName("尾部斜杠不同视为不匹配")
        void testTrailingSlashMismatch() {
            assertThatThrownBy(() ->
                    IssuerValidator.validate(
                            "https://auth.example.com",
                            "https://auth.example.com/"))
                    .isInstanceOf(OAuth2Exception.class)
                    .satisfies(ex -> {
                        OAuth2Exception oauthEx = (OAuth2Exception) ex;
                        assertThat(oauthEx.errorCode()).isEqualTo(OAuth2ErrorCode.ISSUER_MISMATCH);
                    });
        }
    }

    @Nested
    @DisplayName("matches方法测试")
    class MatchesTests {

        @Test
        @DisplayName("匹配的颁发者返回true")
        void testMatching() {
            assertThat(IssuerValidator.matches(
                    "https://auth.example.com",
                    "https://auth.example.com")).isTrue();
        }

        @Test
        @DisplayName("不匹配的颁发者返回false")
        void testNotMatching() {
            assertThat(IssuerValidator.matches(
                    "https://auth.example.com",
                    "https://evil.example.com")).isFalse();
        }

        @Test
        @DisplayName("null expectedIssuer返回false")
        void testNullExpected() {
            assertThat(IssuerValidator.matches(null, "https://auth.example.com")).isFalse();
        }

        @Test
        @DisplayName("null actualIssuer返回false")
        void testNullActual() {
            assertThat(IssuerValidator.matches("https://auth.example.com", null)).isFalse();
        }

        @Test
        @DisplayName("两个null返回false")
        void testBothNull() {
            assertThat(IssuerValidator.matches(null, null)).isFalse();
        }

        @Test
        @DisplayName("空字符串比较")
        void testEmptyStrings() {
            assertThat(IssuerValidator.matches("", "")).isTrue();
        }

        @Test
        @DisplayName("相同内容的不同字符串实例")
        void testDifferentStringInstances() {
            String a = new String("https://auth.example.com");
            String b = new String("https://auth.example.com");

            assertThat(IssuerValidator.matches(a, b)).isTrue();
        }

        @Test
        @DisplayName("Unicode字符串比较")
        void testUnicodeComparison() {
            assertThat(IssuerValidator.matches(
                    "https://auth.example.com/\u00e9",
                    "https://auth.example.com/\u00e9")).isTrue();
        }
    }

    @Nested
    @DisplayName("安全性测试")
    class SecurityTests {

        @Test
        @DisplayName("使用恒定时间比较（功能验证）")
        void testConstantTimeComparison() {
            // Verify that comparison works correctly regardless of where mismatch occurs
            // This is a functional test - true timing tests need benchmarking
            assertThat(IssuerValidator.matches("abcdefgh", "abcdefgX")).isFalse();
            assertThat(IssuerValidator.matches("Xbcdefgh", "abcdefgh")).isFalse();
            assertThat(IssuerValidator.matches("abcXefgh", "abcdefgh")).isFalse();
        }
    }
}
