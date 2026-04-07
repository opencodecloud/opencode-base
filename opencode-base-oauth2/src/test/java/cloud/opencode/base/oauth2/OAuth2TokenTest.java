package cloud.opencode.base.oauth2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * OAuth2TokenTest Tests
 * OAuth2TokenTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("OAuth2Token 测试")
class OAuth2TokenTest {

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("使用必需字段构建")
        void testBuildWithRequiredFields() {
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .build();

            assertThat(token.accessToken()).isEqualTo("access123");
            assertThat(token.tokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("使用所有字段构建")
        void testBuildWithAllFields() {
            Instant now = Instant.now();
            Instant expires = now.plusSeconds(3600);

            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .tokenType("Bearer")
                    .refreshToken("refresh456")
                    .idToken("id789")
                    .scopeString("openid profile email")
                    .issuedAt(now)
                    .expiresAt(expires)
                    .build();

            assertThat(token.accessToken()).isEqualTo("access123");
            assertThat(token.tokenType()).isEqualTo("Bearer");
            assertThat(token.refreshToken()).isEqualTo("refresh456");
            assertThat(token.idToken()).isEqualTo("id789");
            assertThat(token.scopes()).containsExactlyInAnyOrder("openid", "profile", "email");
            assertThat(token.issuedAt()).isEqualTo(now);
            assertThat(token.expiresAt()).isEqualTo(expires);
        }

        @Test
        @DisplayName("使用expiresIn构建")
        void testBuildWithExpiresIn() {
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .expiresIn(3600)
                    .build();

            assertThat(token.expiresAt()).isNotNull();
            assertThat(token.expiresAt()).isAfter(Instant.now());
        }

        @Test
        @DisplayName("使用scopes集合构建")
        void testBuildWithScopesSet() {
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .scopes(Set.of("read", "write"))
                    .build();

            assertThat(token.scopes()).containsExactlyInAnyOrder("read", "write");
        }

        @Test
        @DisplayName("accessToken为null抛出异常")
        void testAccessTokenNull() {
            assertThatThrownBy(() -> OAuth2Token.builder().build())
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("过期方法测试")
    class ExpirationTests {

        @Test
        @DisplayName("isExpired - 已过期")
        void testIsExpiredTrue() {
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .expiresAt(Instant.now().minusSeconds(60))
                    .build();

            assertThat(token.isExpired()).isTrue();
        }

        @Test
        @DisplayName("isExpired - 未过期")
        void testIsExpiredFalse() {
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            assertThat(token.isExpired()).isFalse();
        }

        @Test
        @DisplayName("isExpired - 无过期时间")
        void testIsExpiredNoExpiresAt() {
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .build();

            assertThat(token.isExpired()).isFalse();
        }

        @Test
        @DisplayName("isExpiringSoon - 即将过期")
        void testIsExpiringSoonTrue() {
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .expiresAt(Instant.now().plusSeconds(60))
                    .build();

            assertThat(token.isExpiringSoon(Duration.ofMinutes(5))).isTrue();
        }

        @Test
        @DisplayName("isExpiringSoon - 未即将过期")
        void testIsExpiringSoonFalse() {
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            assertThat(token.isExpiringSoon(Duration.ofMinutes(5))).isFalse();
        }

        @Test
        @DisplayName("remainingTime")
        void testRemainingTime() {
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            Duration remaining = token.remainingTime();
            assertThat(remaining).isNotNull();
            assertThat(remaining.toMinutes()).isGreaterThan(50);
        }

        @Test
        @DisplayName("remainingTime - 已过期")
        void testRemainingTimeExpired() {
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .expiresAt(Instant.now().minusSeconds(60))
                    .build();

            Duration remaining = token.remainingTime();
            assertThat(remaining).isNotNull();
            // 已过期的token，remainingTime可能是负数或ZERO
            assertThat(remaining.isNegative() || remaining.isZero()).isTrue();
        }
    }

    @Nested
    @DisplayName("检查方法测试")
    class CheckMethodsTests {

        @Test
        @DisplayName("hasRefreshToken - 有refresh token")
        void testHasRefreshTokenTrue() {
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .refreshToken("refresh456")
                    .build();

            assertThat(token.hasRefreshToken()).isTrue();
        }

        @Test
        @DisplayName("hasRefreshToken - 无refresh token")
        void testHasRefreshTokenFalse() {
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .build();

            assertThat(token.hasRefreshToken()).isFalse();
        }

        @Test
        @DisplayName("hasIdToken - 有id token")
        void testHasIdTokenTrue() {
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .idToken("id789")
                    .build();

            assertThat(token.hasIdToken()).isTrue();
        }

        @Test
        @DisplayName("hasIdToken - 无id token")
        void testHasIdTokenFalse() {
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .build();

            assertThat(token.hasIdToken()).isFalse();
        }
    }

    @Nested
    @DisplayName("Header方法测试")
    class HeaderMethodsTests {

        @Test
        @DisplayName("toBearerHeader")
        void testToBearerHeader() {
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .build();

            assertThat(token.toBearerHeader()).isEqualTo("Bearer access123");
        }

        @Test
        @DisplayName("toAuthorizationHeader")
        void testToAuthorizationHeader() {
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .tokenType("Bearer")
                    .build();

            assertThat(token.toAuthorizationHeader()).isEqualTo("Bearer access123");
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodsTests {

        @Test
        @DisplayName("equals和hashCode")
        void testEqualsAndHashCode() {
            Instant now = Instant.now();
            OAuth2Token token1 = OAuth2Token.builder()
                    .accessToken("access123")
                    .issuedAt(now)
                    .build();
            OAuth2Token token2 = OAuth2Token.builder()
                    .accessToken("access123")
                    .issuedAt(now)
                    .build();

            assertThat(token1).isEqualTo(token2);
            assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
        }

        @Test
        @DisplayName("toString")
        void testToString() {
            OAuth2Token token = OAuth2Token.builder()
                    .accessToken("access123")
                    .build();

            // toString should NOT contain sensitive access token (redacted)
            assertThat(token.toString()).doesNotContain("access123");
            assertThat(token.toString()).contains("tokenType=Bearer");
            assertThat(token.toString()).contains("OAuth2Token[");
        }
    }
}
