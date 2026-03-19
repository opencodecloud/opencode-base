package cloud.opencode.base.crypto.jwt;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenSignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link JwtUtil}.
 * JwtUtil单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("JwtUtil Tests / JwtUtil测试")
class JwtUtilTest {

    private static final String SECRET = "super-secret-key-for-testing-jwt-12345";
    private static final String TEST_SUBJECT = "user123";
    private static final String TEST_ISSUER = "auth-service";

    private KeyPair rsaKeyPair;
    private KeyPair ecKeyPair;

    @BeforeEach
    void setUp() throws Exception {
        // Generate RSA key pair
        KeyPairGenerator rsaGen = KeyPairGenerator.getInstance("RSA");
        rsaGen.initialize(2048);
        rsaKeyPair = rsaGen.generateKeyPair();

        // Generate EC key pair
        KeyPairGenerator ecGen = KeyPairGenerator.getInstance("EC");
        ecGen.initialize(256);
        ecKeyPair = ecGen.generateKeyPair();
    }

    @Nested
    @DisplayName("Builder Tests / 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("builder()创建构建器")
        void testBuilderFactory() {
            JwtUtil.Builder builder = JwtUtil.builder();
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("使用HS256签名")
        void testSignWithHS256() {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .secret(SECRET)
                    .subject(TEST_SUBJECT)
                    .sign();

            assertThat(token).isNotNull();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("使用HS384签名")
        void testSignWithHS384() {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS384)
                    .secret(SECRET)
                    .subject(TEST_SUBJECT)
                    .sign();

            assertThat(token).isNotNull();
        }

        @Test
        @DisplayName("使用HS512签名")
        void testSignWithHS512() {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS512)
                    .secret(SECRET)
                    .subject(TEST_SUBJECT)
                    .sign();

            assertThat(token).isNotNull();
        }

        @Test
        @DisplayName("使用RS256签名")
        void testSignWithRS256() {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.RS256)
                    .privateKey(rsaKeyPair.getPrivate())
                    .subject(TEST_SUBJECT)
                    .sign();

            assertThat(token).isNotNull();
        }

        @Test
        @DisplayName("使用ES256签名")
        void testSignWithES256() {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.ES256)
                    .privateKey(ecKeyPair.getPrivate())
                    .subject(TEST_SUBJECT)
                    .sign();

            assertThat(token).isNotNull();
        }

        @Test
        @DisplayName("设置所有声明")
        void testSetAllClaims() {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .secret(SECRET)
                    .issuer(TEST_ISSUER)
                    .subject(TEST_SUBJECT)
                    .audience("api-service")
                    .issuedAtNow()
                    .expiresIn(Duration.ofHours(1))
                    .generateJwtId()
                    .claim("role", "admin")
                    .sign();

            JwtClaims claims = JwtUtil.verify(token, SECRET);
            assertThat(claims.issuer()).isEqualTo(TEST_ISSUER);
            assertThat(claims.subject()).isEqualTo(TEST_SUBJECT);
            assertThat(claims.audience()).containsExactly("api-service");
            assertThat(claims.issuedAt()).isNotNull();
            assertThat(claims.expiration()).isNotNull();
            assertThat(claims.jwtId()).isNotNull();
            assertThat(claims.getString("role")).isEqualTo("admin");
        }

        @Test
        @DisplayName("设置自定义头部")
        void testCustomHeader() {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .secret(SECRET)
                    .header("kid", "key-1")
                    .subject(TEST_SUBJECT)
                    .sign();

            JwtUtil.JwtParts parts = JwtUtil.parse(token);
            assertThat(parts.header()).containsEntry("kid", "key-1");
        }

        @Test
        @DisplayName("使用JwtClaims对象")
        void testWithJwtClaims() {
            JwtClaims claims = JwtClaims.builder()
                    .subject(TEST_SUBJECT)
                    .issuer(TEST_ISSUER)
                    .build();

            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .secret(SECRET)
                    .claims(claims)
                    .sign();

            JwtClaims verified = JwtUtil.verify(token, SECRET);
            assertThat(verified.subject()).isEqualTo(TEST_SUBJECT);
            assertThat(verified.issuer()).isEqualTo(TEST_ISSUER);
        }

        @Test
        @DisplayName("使用Map设置声明")
        void testWithClaimsMap() {
            Map<String, Object> claimsMap = Map.of(
                    "sub", TEST_SUBJECT,
                    "role", "user"
            );

            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .secret(SECRET)
                    .claims(claimsMap)
                    .sign();

            JwtClaims verified = JwtUtil.verify(token, SECRET);
            assertThat(verified.subject()).isEqualTo(TEST_SUBJECT);
            assertThat(verified.getString("role")).isEqualTo("user");
        }

        @Test
        @DisplayName("多值受众")
        void testMultipleAudience() {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .secret(SECRET)
                    .audience(List.of("api1", "api2"))
                    .sign();

            JwtClaims claims = JwtUtil.verify(token, SECRET);
            assertThat(claims.audience()).containsExactly("api1", "api2");
        }

        @Test
        @DisplayName("对称算法无密钥抛出异常")
        void testSymmetricWithoutSecretThrows() {
            JwtUtil.Builder builder = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .subject(TEST_SUBJECT);

            assertThatThrownBy(builder::sign)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Secret must be set");
        }

        @Test
        @DisplayName("非对称算法无私钥抛出异常")
        void testAsymmetricWithoutPrivateKeyThrows() {
            JwtUtil.Builder builder = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.RS256)
                    .subject(TEST_SUBJECT);

            assertThatThrownBy(builder::sign)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Private key must be set");
        }
    }

    @Nested
    @DisplayName("Verification Tests / 验证测试")
    class VerificationTests {

        @Test
        @DisplayName("验证有效的HMAC JWT")
        void testVerifyValidHmacJwt() {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .secret(SECRET)
                    .subject(TEST_SUBJECT)
                    .expiresIn(Duration.ofHours(1))
                    .sign();

            JwtClaims claims = JwtUtil.verify(token, SECRET);
            assertThat(claims.subject()).isEqualTo(TEST_SUBJECT);
        }

        @Test
        @DisplayName("验证有效的RSA JWT")
        void testVerifyValidRsaJwt() {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.RS256)
                    .privateKey(rsaKeyPair.getPrivate())
                    .subject(TEST_SUBJECT)
                    .expiresIn(Duration.ofHours(1))
                    .sign();

            JwtClaims claims = JwtUtil.verify(token, rsaKeyPair.getPublic());
            assertThat(claims.subject()).isEqualTo(TEST_SUBJECT);
        }

        @Test
        @DisplayName("验证有效的ECDSA JWT")
        void testVerifyValidEcdsaJwt() {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.ES256)
                    .privateKey(ecKeyPair.getPrivate())
                    .subject(TEST_SUBJECT)
                    .expiresIn(Duration.ofHours(1))
                    .sign();

            JwtClaims claims = JwtUtil.verify(token, ecKeyPair.getPublic());
            assertThat(claims.subject()).isEqualTo(TEST_SUBJECT);
        }

        @Test
        @DisplayName("错误密钥验证失败")
        void testVerifyWithWrongSecretFails() {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .secret(SECRET)
                    .subject(TEST_SUBJECT)
                    .sign();

            assertThatThrownBy(() -> JwtUtil.verify(token, "wrong-secret"))
                    .isInstanceOf(OpenSignatureException.class)
                    .hasMessageContaining("Invalid signature");
        }

        @Test
        @DisplayName("错误公钥验证失败")
        void testVerifyWithWrongPublicKeyFails() throws Exception {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.RS256)
                    .privateKey(rsaKeyPair.getPrivate())
                    .subject(TEST_SUBJECT)
                    .sign();

            // Generate different key pair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair differentKeyPair = keyGen.generateKeyPair();

            assertThatThrownBy(() -> JwtUtil.verify(token, differentKeyPair.getPublic()))
                    .isInstanceOf(OpenSignatureException.class)
                    .hasMessageContaining("Invalid signature");
        }

        @Test
        @DisplayName("已过期的JWT验证失败")
        void testVerifyExpiredJwtFails() {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .secret(SECRET)
                    .subject(TEST_SUBJECT)
                    .expiration(Instant.now().minusSeconds(60))
                    .sign();

            assertThatThrownBy(() -> JwtUtil.verify(token, SECRET))
                    .isInstanceOf(OpenSignatureException.class)
                    .hasMessageContaining("expired");
        }

        @Test
        @DisplayName("尚未生效的JWT验证失败")
        void testVerifyNotYetValidJwtFails() {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .secret(SECRET)
                    .subject(TEST_SUBJECT)
                    .notBefore(Instant.now().plusSeconds(3600))
                    .sign();

            assertThatThrownBy(() -> JwtUtil.verify(token, SECRET))
                    .isInstanceOf(OpenSignatureException.class)
                    .hasMessageContaining("not yet valid");
        }

        @Test
        @DisplayName("对称算法使用公钥验证抛出异常")
        void testVerifySymmetricWithPublicKeyThrows() {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .secret(SECRET)
                    .subject(TEST_SUBJECT)
                    .sign();

            assertThatThrownBy(() -> JwtUtil.verify(token, rsaKeyPair.getPublic()))
                    .isInstanceOf(OpenSignatureException.class)
                    .hasMessageContaining("requires secret");
        }

        @Test
        @DisplayName("非对称算法使用密钥验证抛出异常")
        void testVerifyAsymmetricWithSecretThrows() {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.RS256)
                    .privateKey(rsaKeyPair.getPrivate())
                    .subject(TEST_SUBJECT)
                    .sign();

            assertThatThrownBy(() -> JwtUtil.verify(token, SECRET))
                    .isInstanceOf(OpenSignatureException.class)
                    .hasMessageContaining("requires public key");
        }

        @Test
        @DisplayName("null token验证抛出异常")
        void testVerifyNullTokenThrows() {
            assertThatThrownBy(() -> JwtUtil.verify(null, SECRET))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null secret验证抛出异常")
        void testVerifyNullSecretThrows() {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .secret(SECRET)
                    .subject(TEST_SUBJECT)
                    .sign();

            assertThatThrownBy(() -> JwtUtil.verify(token, (String) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Parse Tests / 解析测试")
    class ParseTests {

        @Test
        @DisplayName("parse()解析JWT部分")
        void testParse() {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .secret(SECRET)
                    .subject(TEST_SUBJECT)
                    .issuer(TEST_ISSUER)
                    .sign();

            JwtUtil.JwtParts parts = JwtUtil.parse(token);

            assertThat(parts).isNotNull();
            assertThat(parts.algorithm()).isEqualTo(JwtAlgorithm.HS256);
            assertThat(parts.header()).containsEntry("alg", "HS256");
            assertThat(parts.header()).containsEntry("typ", "JWT");
            assertThat(parts.claims().subject()).isEqualTo(TEST_SUBJECT);
            assertThat(parts.claims().issuer()).isEqualTo(TEST_ISSUER);
            assertThat(parts.signature()).isNotNull();
            assertThat(parts.signatureInput()).isNotNull();
        }

        @Test
        @DisplayName("parseUnsafe()不验证直接解析")
        void testParseUnsafe() {
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .secret(SECRET)
                    .subject(TEST_SUBJECT)
                    .sign();

            JwtClaims claims = JwtUtil.parseUnsafe(token);
            assertThat(claims.subject()).isEqualTo(TEST_SUBJECT);
        }

        @Test
        @DisplayName("无效格式JWT解析失败")
        void testParseInvalidFormatFails() {
            assertThatThrownBy(() -> JwtUtil.parse("invalid-token"))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("Invalid JWT format");
        }

        @Test
        @DisplayName("只有两段的JWT解析失败")
        void testParseTwoSegmentsFails() {
            assertThatThrownBy(() -> JwtUtil.parse("header.payload"))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("Invalid JWT format");
        }

        @Test
        @DisplayName("缺少alg的JWT解析失败")
        void testParseMissingAlgFails() {
            // Create a token without alg header (invalid)
            String invalidToken = "eyJ0eXAiOiJKV1QifQ.eyJzdWIiOiJ1c2VyMTIzIn0.signature";
            assertThatThrownBy(() -> JwtUtil.parse(invalidToken))
                    .isInstanceOf(OpenCryptoException.class);
        }
    }

    @Nested
    @DisplayName("Quick Sign Tests / 快速签名测试")
    class QuickSignTests {

        @Test
        @DisplayName("quickSign()使用HS256快速签名")
        void testQuickSign() {
            String token = JwtUtil.quickSign(TEST_SUBJECT, SECRET, Duration.ofHours(1));

            assertThat(token).isNotNull();

            JwtClaims claims = JwtUtil.verify(token, SECRET);
            assertThat(claims.subject()).isEqualTo(TEST_SUBJECT);
            assertThat(claims.issuedAt()).isNotNull();
            assertThat(claims.expiration()).isNotNull();
        }

        @Test
        @DisplayName("quickSign()使用JwtClaims")
        void testQuickSignWithClaims() {
            JwtClaims claims = JwtClaims.builder()
                    .subject(TEST_SUBJECT)
                    .issuer(TEST_ISSUER)
                    .expiresIn(Duration.ofHours(1))
                    .build();

            String token = JwtUtil.quickSign(claims, SECRET);

            JwtClaims verified = JwtUtil.verify(token, SECRET);
            assertThat(verified.subject()).isEqualTo(TEST_SUBJECT);
            assertThat(verified.issuer()).isEqualTo(TEST_ISSUER);
        }
    }

    @Nested
    @DisplayName("Special Characters Tests / 特殊字符测试")
    class SpecialCharactersTests {

        @Test
        @DisplayName("声明中包含特殊字符")
        void testSpecialCharactersInClaims() {
            String specialValue = "Hello, \"World\"! \n\t/\\";
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .secret(SECRET)
                    .claim("special", specialValue)
                    .sign();

            JwtClaims claims = JwtUtil.verify(token, SECRET);
            assertThat(claims.getString("special")).isEqualTo(specialValue);
        }

        @Test
        @DisplayName("声明中包含Unicode字符")
        void testUnicodeInClaims() {
            String unicodeValue = "中文测试 日本語 한국어";
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .secret(SECRET)
                    .claim("unicode", unicodeValue)
                    .sign();

            JwtClaims claims = JwtUtil.verify(token, SECRET);
            assertThat(claims.getString("unicode")).isEqualTo(unicodeValue);
        }
    }

    @Nested
    @DisplayName("Complex Claims Tests / 复杂声明测试")
    class ComplexClaimsTests {

        @Test
        @DisplayName("嵌套对象声明")
        void testNestedObjectClaim() {
            Map<String, Object> nestedObj = Map.of(
                    "level", 1,
                    "name", "test"
            );

            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .secret(SECRET)
                    .claim("nested", nestedObj)
                    .sign();

            JwtClaims claims = JwtUtil.verify(token, SECRET);
            @SuppressWarnings("unchecked")
            Map<String, Object> retrieved = (Map<String, Object>) claims.get("nested");
            assertThat(retrieved).containsEntry("name", "test");
        }

        @Test
        @DisplayName("数组声明")
        void testArrayClaim() {
            List<String> arrayValue = List.of("item1", "item2", "item3");
            String token = JwtUtil.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .secret(SECRET)
                    .claim("items", arrayValue)
                    .sign();

            JwtClaims claims = JwtUtil.verify(token, SECRET);
            assertThat(claims.<String>getList("items")).containsExactly("item1", "item2", "item3");
        }
    }
}
