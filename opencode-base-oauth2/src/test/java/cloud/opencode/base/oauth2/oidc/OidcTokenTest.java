package cloud.opencode.base.oauth2.oidc;

import cloud.opencode.base.oauth2.OAuth2Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * OidcTokenTest Tests
 * OidcTokenTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("OidcToken 测试")
class OidcTokenTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建带有claims的OidcToken")
        void testConstructorWithClaims() {
            OAuth2Token oauth2Token = createOAuth2Token();
            JwtClaims claims = createJwtClaims();

            OidcToken oidcToken = new OidcToken(oauth2Token, claims);

            assertThat(oidcToken.oauth2Token()).isEqualTo(oauth2Token);
            assertThat(oidcToken.idTokenClaims()).isPresent();
            assertThat(oidcToken.idTokenClaims().get()).isEqualTo(claims);
        }

        @Test
        @DisplayName("创建不带claims的OidcToken")
        void testConstructorWithoutClaims() {
            OAuth2Token oauth2Token = createOAuth2Token();

            OidcToken oidcToken = new OidcToken(oauth2Token, null);

            assertThat(oidcToken.oauth2Token()).isEqualTo(oauth2Token);
            assertThat(oidcToken.idTokenClaims()).isEmpty();
        }

        @Test
        @DisplayName("oauth2Token为null抛出异常")
        void testConstructorNullOAuth2Token() {
            assertThatThrownBy(() -> new OidcToken(null, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("from方法测试")
    class FromTests {

        @Test
        @DisplayName("从带有idToken的OAuth2Token创建")
        void testFromWithIdToken() {
            String payload = """
                    {"sub":"user123","iss":"https://issuer.com","email":"user@example.com"}
                    """;
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payload.getBytes());
            String idToken = "header." + encodedPayload + ".signature";

            OAuth2Token oauth2Token = OAuth2Token.builder()
                    .accessToken("access123")
                    .idToken(idToken)
                    .build();

            OidcToken oidcToken = OidcToken.from(oauth2Token);

            assertThat(oidcToken.hasIdToken()).isTrue();
            assertThat(oidcToken.subject()).isEqualTo("user123");
            assertThat(oidcToken.issuer()).isEqualTo("https://issuer.com");
            assertThat(oidcToken.email()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("从不带idToken的OAuth2Token创建")
        void testFromWithoutIdToken() {
            OAuth2Token oauth2Token = OAuth2Token.builder()
                    .accessToken("access123")
                    .build();

            OidcToken oidcToken = OidcToken.from(oauth2Token);

            assertThat(oidcToken.hasIdToken()).isFalse();
            assertThat(oidcToken.subject()).isNull();
        }

        @Test
        @DisplayName("oauth2Token为null抛出异常")
        void testFromNullToken() {
            assertThatThrownBy(() -> OidcToken.from(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("空白idToken不解析")
        void testFromBlankIdToken() {
            OAuth2Token oauth2Token = OAuth2Token.builder()
                    .accessToken("access123")
                    .idToken("  ")
                    .build();

            OidcToken oidcToken = OidcToken.from(oauth2Token);

            assertThat(oidcToken.hasIdToken()).isFalse();
        }
    }

    @Nested
    @DisplayName("委托方法测试")
    class DelegatedMethodsTests {

        @Test
        @DisplayName("accessToken委托")
        void testAccessToken() {
            OAuth2Token oauth2Token = OAuth2Token.builder()
                    .accessToken("access123")
                    .build();
            OidcToken oidcToken = new OidcToken(oauth2Token, null);

            assertThat(oidcToken.accessToken()).isEqualTo("access123");
        }

        @Test
        @DisplayName("refreshToken委托")
        void testRefreshToken() {
            OAuth2Token oauth2Token = OAuth2Token.builder()
                    .accessToken("access123")
                    .refreshToken("refresh123")
                    .build();
            OidcToken oidcToken = new OidcToken(oauth2Token, null);

            assertThat(oidcToken.refreshToken()).isEqualTo("refresh123");
        }

        @Test
        @DisplayName("idToken委托")
        void testIdToken() {
            OAuth2Token oauth2Token = OAuth2Token.builder()
                    .accessToken("access123")
                    .idToken("id-token-value")
                    .build();
            OidcToken oidcToken = new OidcToken(oauth2Token, null);

            assertThat(oidcToken.idToken()).isEqualTo("id-token-value");
        }

        @Test
        @DisplayName("isExpired委托")
        void testIsExpired() {
            OAuth2Token oauth2Token = OAuth2Token.builder()
                    .accessToken("access123")
                    .expiresAt(Instant.now().minusSeconds(60))
                    .build();
            OidcToken oidcToken = new OidcToken(oauth2Token, null);

            assertThat(oidcToken.isExpired()).isTrue();
        }

        @Test
        @DisplayName("isExpiringSoon委托")
        void testIsExpiringSoon() {
            OAuth2Token oauth2Token = OAuth2Token.builder()
                    .accessToken("access123")
                    .expiresAt(Instant.now().plusSeconds(60))
                    .build();
            OidcToken oidcToken = new OidcToken(oauth2Token, null);

            assertThat(oidcToken.isExpiringSoon(Duration.ofMinutes(5))).isTrue();
        }

        @Test
        @DisplayName("hasRefreshToken委托")
        void testHasRefreshToken() {
            OAuth2Token oauth2Token = OAuth2Token.builder()
                    .accessToken("access123")
                    .refreshToken("refresh123")
                    .build();
            OidcToken oidcToken = new OidcToken(oauth2Token, null);

            assertThat(oidcToken.hasRefreshToken()).isTrue();
        }

        @Test
        @DisplayName("toBearerHeader委托")
        void testToBearerHeader() {
            OAuth2Token oauth2Token = OAuth2Token.builder()
                    .accessToken("access123")
                    .build();
            OidcToken oidcToken = new OidcToken(oauth2Token, null);

            assertThat(oidcToken.toBearerHeader()).isEqualTo("Bearer access123");
        }

        @Test
        @DisplayName("scopes委托")
        void testScopes() {
            OAuth2Token oauth2Token = OAuth2Token.builder()
                    .accessToken("access123")
                    .scopes(Set.of("openid", "profile"))
                    .build();
            OidcToken oidcToken = new OidcToken(oauth2Token, null);

            assertThat(oidcToken.scopes()).containsExactlyInAnyOrder("openid", "profile");
        }
    }

    @Nested
    @DisplayName("ID Token Claims访问器测试")
    class IdTokenClaimsAccessorTests {

        @Test
        @DisplayName("subject")
        void testSubject() {
            OidcToken oidcToken = createOidcTokenWithClaims();
            assertThat(oidcToken.subject()).isEqualTo("user123");
        }

        @Test
        @DisplayName("issuer")
        void testIssuer() {
            OidcToken oidcToken = createOidcTokenWithClaims();
            assertThat(oidcToken.issuer()).isEqualTo("https://issuer.com");
        }

        @Test
        @DisplayName("audience")
        void testAudience() {
            OidcToken oidcToken = createOidcTokenWithClaims();
            assertThat(oidcToken.audience()).isEqualTo("client1");
        }

        @Test
        @DisplayName("expiration")
        void testExpiration() {
            OidcToken oidcToken = createOidcTokenWithClaims();
            assertThat(oidcToken.expiration()).isNotNull();
        }

        @Test
        @DisplayName("issuedAt")
        void testIssuedAt() {
            OidcToken oidcToken = createOidcTokenWithClaims();
            assertThat(oidcToken.issuedAt()).isNotNull();
        }

        @Test
        @DisplayName("nonce")
        void testNonce() {
            OidcToken oidcToken = createOidcTokenWithClaims();
            assertThat(oidcToken.nonce()).isEqualTo("nonce123");
        }

        @Test
        @DisplayName("email")
        void testEmail() {
            OidcToken oidcToken = createOidcTokenWithClaims();
            assertThat(oidcToken.email()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("name")
        void testName() {
            OidcToken oidcToken = createOidcTokenWithClaims();
            assertThat(oidcToken.name()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("picture")
        void testPicture() {
            OidcToken oidcToken = createOidcTokenWithClaims();
            assertThat(oidcToken.picture()).isEqualTo("https://example.com/photo.jpg");
        }

        @Test
        @DisplayName("无claims时返回null")
        void testAccessorsWithoutClaims() {
            OidcToken oidcToken = new OidcToken(createOAuth2Token(), null);

            assertThat(oidcToken.subject()).isNull();
            assertThat(oidcToken.issuer()).isNull();
            assertThat(oidcToken.audience()).isNull();
            assertThat(oidcToken.expiration()).isNull();
            assertThat(oidcToken.issuedAt()).isNull();
            assertThat(oidcToken.nonce()).isNull();
            assertThat(oidcToken.email()).isNull();
            assertThat(oidcToken.name()).isNull();
            assertThat(oidcToken.picture()).isNull();
        }
    }

    @Nested
    @DisplayName("isEmailVerified方法测试")
    class IsEmailVerifiedTests {

        @Test
        @DisplayName("email_verified为true")
        void testIsEmailVerifiedTrue() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, null, null, null, null, null, null,
                    Map.of("email_verified", true)
            );
            OidcToken oidcToken = new OidcToken(createOAuth2Token(), claims);

            assertThat(oidcToken.isEmailVerified()).isTrue();
        }

        @Test
        @DisplayName("email_verified为false")
        void testIsEmailVerifiedFalse() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, null, null, null, null, null, null,
                    Map.of("email_verified", false)
            );
            OidcToken oidcToken = new OidcToken(createOAuth2Token(), claims);

            assertThat(oidcToken.isEmailVerified()).isFalse();
        }

        @Test
        @DisplayName("email_verified为字符串true")
        void testIsEmailVerifiedStringTrue() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, null, null, null, null, null, null,
                    Map.of("email_verified", "true")
            );
            OidcToken oidcToken = new OidcToken(createOAuth2Token(), claims);

            assertThat(oidcToken.isEmailVerified()).isTrue();
        }

        @Test
        @DisplayName("无claims时返回false")
        void testIsEmailVerifiedNoClaims() {
            OidcToken oidcToken = new OidcToken(createOAuth2Token(), null);
            assertThat(oidcToken.isEmailVerified()).isFalse();
        }
    }

    @Nested
    @DisplayName("isValid方法测试")
    class IsValidTests {

        @Test
        @DisplayName("有效token返回true")
        void testIsValidTrue() {
            JwtClaims claims = new JwtClaims(
                    null, null, null,
                    Instant.now().plusSeconds(3600),
                    Instant.now().minusSeconds(60),
                    null, null, null, null, null
            );
            OidcToken oidcToken = new OidcToken(createOAuth2Token(), claims);

            assertThat(oidcToken.isValid()).isTrue();
        }

        @Test
        @DisplayName("无claims返回false")
        void testIsValidNoClaims() {
            OidcToken oidcToken = new OidcToken(createOAuth2Token(), null);
            assertThat(oidcToken.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("isIdTokenExpired方法测试")
    class IsIdTokenExpiredTests {

        @Test
        @DisplayName("已过期返回true")
        void testIsIdTokenExpiredTrue() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, Instant.now().minusSeconds(60), null, null, null, null, null, null
            );
            OidcToken oidcToken = new OidcToken(createOAuth2Token(), claims);

            assertThat(oidcToken.isIdTokenExpired()).isTrue();
        }

        @Test
        @DisplayName("无claims返回false")
        void testIsIdTokenExpiredNoClaims() {
            OidcToken oidcToken = new OidcToken(createOAuth2Token(), null);
            assertThat(oidcToken.isIdTokenExpired()).isFalse();
        }
    }

    @Nested
    @DisplayName("equals和hashCode测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同oauth2Token相等")
        void testEquals() {
            OAuth2Token oauth2Token = createOAuth2Token();
            OidcToken token1 = new OidcToken(oauth2Token, null);
            OidcToken token2 = new OidcToken(oauth2Token, createJwtClaims());

            assertThat(token1).isEqualTo(token2);
            assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
        }

        @Test
        @DisplayName("不同oauth2Token不相等")
        void testNotEquals() {
            OidcToken token1 = new OidcToken(
                    OAuth2Token.builder().accessToken("access1").build(), null
            );
            OidcToken token2 = new OidcToken(
                    OAuth2Token.builder().accessToken("access2").build(), null
            );

            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() {
            OidcToken token = new OidcToken(createOAuth2Token(), null);
            assertThat(token).isEqualTo(token);
        }

        @Test
        @DisplayName("与null不相等")
        void testNotEqualsNull() {
            OidcToken token = new OidcToken(createOAuth2Token(), null);
            assertThat(token).isNotEqualTo(null);
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含关键信息")
        void testToString() {
            OidcToken oidcToken = createOidcTokenWithClaims();
            String str = oidcToken.toString();

            assertThat(str).contains("OidcToken");
            assertThat(str).contains("subject='user123'");
            assertThat(str).contains("email='user@example.com'");
        }
    }

    // Helper methods

    private OAuth2Token createOAuth2Token() {
        return OAuth2Token.builder()
                .accessToken("access123")
                .refreshToken("refresh123")
                .expiresIn(3600)
                .build();
    }

    private JwtClaims createJwtClaims() {
        Instant now = Instant.now();
        return new JwtClaims(
                "https://issuer.com",
                "user123",
                List.of("client1"),
                now.plusSeconds(3600),
                now.minusSeconds(60),
                now,
                "jwt-id",
                "nonce123",
                "client1",
                Map.of(
                        "email", "user@example.com",
                        "name", "John Doe",
                        "picture", "https://example.com/photo.jpg"
                )
        );
    }

    private OidcToken createOidcTokenWithClaims() {
        return new OidcToken(createOAuth2Token(), createJwtClaims());
    }
}
