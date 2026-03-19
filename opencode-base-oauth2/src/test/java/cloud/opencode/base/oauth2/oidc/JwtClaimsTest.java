package cloud.opencode.base.oauth2.oidc;

import cloud.opencode.base.oauth2.exception.OAuth2Exception;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * JwtClaimsTest Tests
 * JwtClaimsTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("JwtClaims 测试")
class JwtClaimsTest {

    @Nested
    @DisplayName("Record字段测试")
    class RecordFieldsTests {

        @Test
        @DisplayName("Record包含所有必需字段")
        void testRecordFields() {
            Instant now = Instant.now();
            JwtClaims claims = new JwtClaims(
                    "https://issuer.com",
                    "user123",
                    List.of("client1", "client2"),
                    now.plusSeconds(3600),
                    now.minusSeconds(60),
                    now,
                    "jwt-id",
                    "nonce123",
                    "azp-value",
                    Map.of("custom", "value")
            );

            assertThat(claims.iss()).isEqualTo("https://issuer.com");
            assertThat(claims.sub()).isEqualTo("user123");
            assertThat(claims.aud()).containsExactly("client1", "client2");
            assertThat(claims.exp()).isEqualTo(now.plusSeconds(3600));
            assertThat(claims.nbf()).isEqualTo(now.minusSeconds(60));
            assertThat(claims.iat()).isEqualTo(now);
            assertThat(claims.jti()).isEqualTo("jwt-id");
            assertThat(claims.nonce()).isEqualTo("nonce123");
            assertThat(claims.azp()).isEqualTo("azp-value");
            assertThat(claims.claims()).containsEntry("custom", "value");
        }

        @Test
        @DisplayName("aud是不可变的")
        void testAudImmutable() {
            JwtClaims claims = new JwtClaims(
                    null, null, List.of("aud1"), null, null, null, null, null, null, null
            );

            assertThatThrownBy(() -> claims.aud().add("new"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("claims是不可变的")
        void testClaimsImmutable() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, null, null, null, null, null, null, Map.of("key", "value")
            );

            assertThatThrownBy(() -> claims.claims().put("new", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("null aud变成空列表")
        void testNullAudBecomesEmptyList() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, null, null, null, null, null, null, null
            );

            assertThat(claims.aud()).isEmpty();
        }

        @Test
        @DisplayName("null claims变成空Map")
        void testNullClaimsBecomesEmptyMap() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, null, null, null, null, null, null, null
            );

            assertThat(claims.claims()).isEmpty();
        }
    }

    @Nested
    @DisplayName("isExpired方法测试")
    class IsExpiredTests {

        @Test
        @DisplayName("已过期返回true")
        void testIsExpiredTrue() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, Instant.now().minusSeconds(60), null, null, null, null, null, null
            );

            assertThat(claims.isExpired()).isTrue();
        }

        @Test
        @DisplayName("未过期返回false")
        void testIsExpiredFalse() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, Instant.now().plusSeconds(3600), null, null, null, null, null, null
            );

            assertThat(claims.isExpired()).isFalse();
        }

        @Test
        @DisplayName("exp为null返回false")
        void testIsExpiredNullExp() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, null, null, null, null, null, null, null
            );

            assertThat(claims.isExpired()).isFalse();
        }
    }

    @Nested
    @DisplayName("isNotYetValid方法测试")
    class IsNotYetValidTests {

        @Test
        @DisplayName("尚未生效返回true")
        void testIsNotYetValidTrue() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, null, Instant.now().plusSeconds(60), null, null, null, null, null
            );

            assertThat(claims.isNotYetValid()).isTrue();
        }

        @Test
        @DisplayName("已生效返回false")
        void testIsNotYetValidFalse() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, null, Instant.now().minusSeconds(60), null, null, null, null, null
            );

            assertThat(claims.isNotYetValid()).isFalse();
        }

        @Test
        @DisplayName("nbf为null返回false")
        void testIsNotYetValidNullNbf() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, null, null, null, null, null, null, null
            );

            assertThat(claims.isNotYetValid()).isFalse();
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

            assertThat(claims.isValid()).isTrue();
        }

        @Test
        @DisplayName("已过期返回false")
        void testIsValidExpired() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, Instant.now().minusSeconds(60), null, null, null, null, null, null
            );

            assertThat(claims.isValid()).isFalse();
        }

        @Test
        @DisplayName("尚未生效返回false")
        void testIsValidNotYetValid() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, null, Instant.now().plusSeconds(60), null, null, null, null, null
            );

            assertThat(claims.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("getClaim方法测试")
    class GetClaimTests {

        @Test
        @DisplayName("获取存在的claim")
        void testGetClaimExists() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, null, null, null, null, null, null, Map.of("custom", "value")
            );

            Optional<Object> claim = claims.getClaim("custom");
            assertThat(claim).isPresent();
            assertThat(claim.get()).isEqualTo("value");
        }

        @Test
        @DisplayName("获取不存在的claim")
        void testGetClaimNotExists() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, null, null, null, null, null, null, Map.of()
            );

            assertThat(claims.getClaim("nonexistent")).isEmpty();
        }
    }

    @Nested
    @DisplayName("getClaimAsString方法测试")
    class GetClaimAsStringTests {

        @Test
        @DisplayName("字符串claim")
        void testGetClaimAsStringString() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, null, null, null, null, null, null, Map.of("str", "value")
            );

            assertThat(claims.getClaimAsString("str")).contains("value");
        }

        @Test
        @DisplayName("数字claim转字符串")
        void testGetClaimAsStringNumber() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, null, null, null, null, null, null, Map.of("num", 42)
            );

            assertThat(claims.getClaimAsString("num")).contains("42");
        }

        @Test
        @DisplayName("不存在的claim返回empty")
        void testGetClaimAsStringNotExists() {
            JwtClaims claims = new JwtClaims(
                    null, null, null, null, null, null, null, null, null, Map.of()
            );

            assertThat(claims.getClaimAsString("nonexistent")).isEmpty();
        }
    }

    @Nested
    @DisplayName("audience方法测试")
    class AudienceMethodTests {

        @Test
        @DisplayName("返回第一个受众")
        void testAudienceFirst() {
            JwtClaims claims = new JwtClaims(
                    null, null, List.of("aud1", "aud2"), null, null, null, null, null, null, null
            );

            assertThat(claims.audience()).isEqualTo("aud1");
        }

        @Test
        @DisplayName("空列表返回null")
        void testAudienceEmpty() {
            JwtClaims claims = new JwtClaims(
                    null, null, List.of(), null, null, null, null, null, null, null
            );

            assertThat(claims.audience()).isNull();
        }
    }

    @Nested
    @DisplayName("hasAudience方法测试")
    class HasAudienceTests {

        @Test
        @DisplayName("包含受众返回true")
        void testHasAudienceTrue() {
            JwtClaims claims = new JwtClaims(
                    null, null, List.of("aud1", "aud2"), null, null, null, null, null, null, null
            );

            assertThat(claims.hasAudience("aud1")).isTrue();
            assertThat(claims.hasAudience("aud2")).isTrue();
        }

        @Test
        @DisplayName("不包含受众返回false")
        void testHasAudienceFalse() {
            JwtClaims claims = new JwtClaims(
                    null, null, List.of("aud1"), null, null, null, null, null, null, null
            );

            assertThat(claims.hasAudience("aud2")).isFalse();
        }
    }

    @Nested
    @DisplayName("parse方法测试")
    class ParseTests {

        @Test
        @DisplayName("解析有效JWT")
        void testParseValidJwt() {
            // Create a simple JWT payload
            String payload = """
                    {"sub":"user123","iss":"https://issuer.com","aud":"client1","exp":9999999999,"iat":1000000000}
                    """;
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payload.getBytes());
            String jwt = "header." + encodedPayload + ".signature";

            JwtClaims claims = JwtClaims.parse(jwt);

            assertThat(claims.sub()).isEqualTo("user123");
            assertThat(claims.iss()).isEqualTo("https://issuer.com");
            assertThat(claims.hasAudience("client1")).isTrue();
        }

        @Test
        @DisplayName("解析包含数组受众的JWT")
        void testParseJwtWithArrayAudience() {
            String payload = """
                    {"sub":"user123","aud":["client1","client2"]}
                    """;
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payload.getBytes());
            String jwt = "header." + encodedPayload + ".signature";

            JwtClaims claims = JwtClaims.parse(jwt);

            assertThat(claims.aud()).containsExactly("client1", "client2");
        }

        @Test
        @DisplayName("解析null token抛出异常")
        void testParseNullToken() {
            assertThatThrownBy(() -> JwtClaims.parse(null))
                    .isInstanceOf(OAuth2Exception.class);
        }

        @Test
        @DisplayName("解析空token抛出异常")
        void testParseEmptyToken() {
            assertThatThrownBy(() -> JwtClaims.parse(""))
                    .isInstanceOf(OAuth2Exception.class);

            assertThatThrownBy(() -> JwtClaims.parse("  "))
                    .isInstanceOf(OAuth2Exception.class);
        }

        @Test
        @DisplayName("解析无效格式JWT抛出异常")
        void testParseInvalidFormat() {
            assertThatThrownBy(() -> JwtClaims.parse("invalid"))
                    .isInstanceOf(OAuth2Exception.class);
        }

        @Test
        @DisplayName("解析包含布尔值的JWT")
        void testParseJwtWithBoolean() {
            String payload = """
                    {"sub":"user123","email_verified":true}
                    """;
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payload.getBytes());
            String jwt = "header." + encodedPayload + ".signature";

            JwtClaims claims = JwtClaims.parse(jwt);

            assertThat(claims.getClaim("email_verified")).contains(true);
        }

        @Test
        @DisplayName("解析包含数字的JWT")
        void testParseJwtWithNumbers() {
            String payload = """
                    {"sub":"user123","exp":1234567890,"custom_int":42}
                    """;
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payload.getBytes());
            String jwt = "header." + encodedPayload + ".signature";

            JwtClaims claims = JwtClaims.parse(jwt);

            assertThat(claims.exp()).isEqualTo(Instant.ofEpochSecond(1234567890));
        }

        @Test
        @DisplayName("解析包含nonce的JWT")
        void testParseJwtWithNonce() {
            String payload = """
                    {"sub":"user123","nonce":"abc123","azp":"client-id"}
                    """;
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payload.getBytes());
            String jwt = "header." + encodedPayload + ".signature";

            JwtClaims claims = JwtClaims.parse(jwt);

            assertThat(claims.nonce()).isEqualTo("abc123");
            assertThat(claims.azp()).isEqualTo("client-id");
        }
    }
}
