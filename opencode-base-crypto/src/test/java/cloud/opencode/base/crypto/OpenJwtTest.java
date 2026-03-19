package cloud.opencode.base.crypto;

import cloud.opencode.base.crypto.jwt.JwtAlgorithm;
import cloud.opencode.base.crypto.jwt.JwtClaims;
import cloud.opencode.base.crypto.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link OpenJwt}.
 * OpenJwt单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("OpenJwt Tests / OpenJwt测试")
class OpenJwtTest {

    private static final String SECRET = "super-secret-key-for-testing-jwt-12345";
    private static final String TEST_SUBJECT = "user123";
    private static final String TEST_ISSUER = "auth-service";

    private KeyPair rsaKeyPair;
    private KeyPair ecKeyPair;

    @BeforeEach
    void setUp() {
        rsaKeyPair = OpenJwt.generateRsaKeyPair();
        ecKeyPair = OpenJwt.generateEcKeyPair();
    }

    @Nested
    @DisplayName("Quick Sign Tests / 快速签名测试")
    class QuickSignTests {

        @Test
        @DisplayName("sign()使用HS256快速签名")
        void testQuickSign() {
            String token = OpenJwt.sign(TEST_SUBJECT, SECRET, Duration.ofHours(1));

            assertThat(token).isNotNull();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("sign()签名后可验证")
        void testQuickSignAndVerify() {
            String token = OpenJwt.sign(TEST_SUBJECT, SECRET, Duration.ofHours(1));

            JwtClaims claims = OpenJwt.verify(token, SECRET);
            assertThat(claims.subject()).isEqualTo(TEST_SUBJECT);
            assertThat(claims.expiration()).isNotNull();
            assertThat(claims.issuedAt()).isNotNull();
        }

        @Test
        @DisplayName("sign()使用JwtClaims签名")
        void testSignWithClaims() {
            JwtClaims claims = JwtClaims.builder()
                    .subject(TEST_SUBJECT)
                    .issuer(TEST_ISSUER)
                    .expiresIn(Duration.ofHours(1))
                    .build();

            String token = OpenJwt.sign(claims, SECRET);

            JwtClaims verified = OpenJwt.verify(token, SECRET);
            assertThat(verified.subject()).isEqualTo(TEST_SUBJECT);
            assertThat(verified.issuer()).isEqualTo(TEST_ISSUER);
        }

        @Test
        @DisplayName("sign()使用指定HMAC算法")
        void testSignWithAlgorithm() {
            JwtClaims claims = JwtClaims.builder()
                    .subject(TEST_SUBJECT)
                    .build();

            String token = OpenJwt.sign(claims, SECRET, JwtAlgorithm.HS512);

            JwtUtil.JwtParts parts = JwtUtil.parse(token);
            assertThat(parts.algorithm()).isEqualTo(JwtAlgorithm.HS512);
        }
    }

    @Nested
    @DisplayName("RSA Sign Tests / RSA签名测试")
    class RsaSignTests {

        @Test
        @DisplayName("signRsa()使用RS256签名")
        void testSignRsa() {
            String token = OpenJwt.signRsa(TEST_SUBJECT, rsaKeyPair.getPrivate(), Duration.ofHours(1));

            assertThat(token).isNotNull();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("signRsa()签名后可验证")
        void testSignRsaAndVerify() {
            String token = OpenJwt.signRsa(TEST_SUBJECT, rsaKeyPair.getPrivate(), Duration.ofHours(1));

            JwtClaims claims = OpenJwt.verify(token, rsaKeyPair.getPublic());
            assertThat(claims.subject()).isEqualTo(TEST_SUBJECT);
        }

        @Test
        @DisplayName("signRsa()使用JwtClaims签名")
        void testSignRsaWithClaims() {
            JwtClaims claims = JwtClaims.builder()
                    .subject(TEST_SUBJECT)
                    .issuer(TEST_ISSUER)
                    .expiresIn(Duration.ofHours(1))
                    .build();

            String token = OpenJwt.signRsa(claims, rsaKeyPair.getPrivate());

            JwtClaims verified = OpenJwt.verify(token, rsaKeyPair.getPublic());
            assertThat(verified.subject()).isEqualTo(TEST_SUBJECT);
            assertThat(verified.issuer()).isEqualTo(TEST_ISSUER);
        }

        @Test
        @DisplayName("signRsa()使用指定RSA算法")
        void testSignRsaWithAlgorithm() {
            JwtClaims claims = JwtClaims.builder()
                    .subject(TEST_SUBJECT)
                    .build();

            String token = OpenJwt.signRsa(claims, rsaKeyPair.getPrivate(), JwtAlgorithm.RS512);

            JwtUtil.JwtParts parts = JwtUtil.parse(token);
            assertThat(parts.algorithm()).isEqualTo(JwtAlgorithm.RS512);
        }
    }

    @Nested
    @DisplayName("ECDSA Sign Tests / ECDSA签名测试")
    class EcdsaSignTests {

        @Test
        @DisplayName("signEc()使用ES256签名")
        void testSignEc() {
            String token = OpenJwt.signEc(TEST_SUBJECT, ecKeyPair.getPrivate(), Duration.ofHours(1));

            assertThat(token).isNotNull();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("signEc()签名后可验证")
        void testSignEcAndVerify() {
            String token = OpenJwt.signEc(TEST_SUBJECT, ecKeyPair.getPrivate(), Duration.ofHours(1));

            JwtClaims claims = OpenJwt.verify(token, ecKeyPair.getPublic());
            assertThat(claims.subject()).isEqualTo(TEST_SUBJECT);
        }

        @Test
        @DisplayName("signEc()使用JwtClaims签名")
        void testSignEcWithClaims() {
            JwtClaims claims = JwtClaims.builder()
                    .subject(TEST_SUBJECT)
                    .issuer(TEST_ISSUER)
                    .expiresIn(Duration.ofHours(1))
                    .build();

            String token = OpenJwt.signEc(claims, ecKeyPair.getPrivate());

            JwtClaims verified = OpenJwt.verify(token, ecKeyPair.getPublic());
            assertThat(verified.subject()).isEqualTo(TEST_SUBJECT);
            assertThat(verified.issuer()).isEqualTo(TEST_ISSUER);
        }

        @Test
        @DisplayName("signEc()使用指定ECDSA算法")
        void testSignEcWithAlgorithm() {
            JwtClaims claims = JwtClaims.builder()
                    .subject(TEST_SUBJECT)
                    .build();

            String token = OpenJwt.signEc(claims, ecKeyPair.getPrivate(), JwtAlgorithm.ES256);

            JwtUtil.JwtParts parts = JwtUtil.parse(token);
            assertThat(parts.algorithm()).isEqualTo(JwtAlgorithm.ES256);
        }
    }

    @Nested
    @DisplayName("Verification Tests / 验证测试")
    class VerificationTests {

        @Test
        @DisplayName("verify()使用HMAC密钥验证")
        void testVerifyWithSecret() {
            String token = OpenJwt.sign(TEST_SUBJECT, SECRET, Duration.ofHours(1));

            JwtClaims claims = OpenJwt.verify(token, SECRET);
            assertThat(claims.subject()).isEqualTo(TEST_SUBJECT);
        }

        @Test
        @DisplayName("verify()使用RSA公钥验证")
        void testVerifyWithRsaPublicKey() {
            String token = OpenJwt.signRsa(TEST_SUBJECT, rsaKeyPair.getPrivate(), Duration.ofHours(1));

            JwtClaims claims = OpenJwt.verify(token, rsaKeyPair.getPublic());
            assertThat(claims.subject()).isEqualTo(TEST_SUBJECT);
        }

        @Test
        @DisplayName("verify()使用EC公钥验证")
        void testVerifyWithEcPublicKey() {
            String token = OpenJwt.signEc(TEST_SUBJECT, ecKeyPair.getPrivate(), Duration.ofHours(1));

            JwtClaims claims = OpenJwt.verify(token, ecKeyPair.getPublic());
            assertThat(claims.subject()).isEqualTo(TEST_SUBJECT);
        }
    }

    @Nested
    @DisplayName("Parse Unsafe Tests / 不安全解析测试")
    class ParseUnsafeTests {

        @Test
        @DisplayName("parseUnsafe()解析JWT但不验证")
        void testParseUnsafe() {
            String token = OpenJwt.sign(TEST_SUBJECT, SECRET, Duration.ofHours(1));

            JwtClaims claims = OpenJwt.parseUnsafe(token);
            assertThat(claims.subject()).isEqualTo(TEST_SUBJECT);
        }

        @Test
        @DisplayName("parseUnsafe()可解析修改过签名的JWT")
        void testParseUnsafeModifiedSignature() {
            String token = OpenJwt.sign(TEST_SUBJECT, SECRET, Duration.ofHours(1));
            // Modify the signature part but keep valid base64
            String[] parts = token.split("\\.");
            // Use a different valid base64 string for signature
            String tamperedToken = parts[0] + "." + parts[1] + ".dGFtcGVyZWRfc2lnbmF0dXJl";

            // parseUnsafe should still work (doesn't verify signature)
            JwtClaims claims = OpenJwt.parseUnsafe(tamperedToken);
            assertThat(claims.subject()).isEqualTo(TEST_SUBJECT);
        }
    }

    @Nested
    @DisplayName("Key Generation Tests / 密钥生成测试")
    class KeyGenerationTests {

        @Test
        @DisplayName("generateRsaKeyPair()生成2048位RSA密钥对")
        void testGenerateRsaKeyPair() {
            KeyPair keyPair = OpenJwt.generateRsaKeyPair();

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic()).isNotNull();
            assertThat(keyPair.getPrivate()).isNotNull();
            assertThat(keyPair.getPublic().getAlgorithm()).isEqualTo("RSA");
        }

        @Test
        @DisplayName("generateRsaKeyPair(keySize)生成指定大小RSA密钥对")
        void testGenerateRsaKeyPairWithSize() {
            KeyPair keyPair = OpenJwt.generateRsaKeyPair(4096);

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic().getAlgorithm()).isEqualTo("RSA");
        }

        @Test
        @DisplayName("generateEcKeyPair()生成P-256 EC密钥对")
        void testGenerateEcKeyPair() {
            KeyPair keyPair = OpenJwt.generateEcKeyPair();

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic()).isNotNull();
            assertThat(keyPair.getPrivate()).isNotNull();
            assertThat(keyPair.getPublic().getAlgorithm()).isEqualTo("EC");
        }

        @Test
        @DisplayName("generateEcKeyPair(curveName)生成指定曲线EC密钥对")
        void testGenerateEcKeyPairWithCurve() {
            KeyPair keyPair = OpenJwt.generateEcKeyPair("secp384r1");

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic().getAlgorithm()).isEqualTo("EC");
        }

        @Test
        @DisplayName("生成的RSA密钥对可用于签名和验证")
        void testGeneratedRsaKeyPairWorks() {
            KeyPair keyPair = OpenJwt.generateRsaKeyPair();
            String token = OpenJwt.signRsa(TEST_SUBJECT, keyPair.getPrivate(), Duration.ofHours(1));
            JwtClaims claims = OpenJwt.verify(token, keyPair.getPublic());

            assertThat(claims.subject()).isEqualTo(TEST_SUBJECT);
        }

        @Test
        @DisplayName("生成的EC密钥对可用于签名和验证")
        void testGeneratedEcKeyPairWorks() {
            KeyPair keyPair = OpenJwt.generateEcKeyPair();
            String token = OpenJwt.signEc(TEST_SUBJECT, keyPair.getPrivate(), Duration.ofHours(1));
            JwtClaims claims = OpenJwt.verify(token, keyPair.getPublic());

            assertThat(claims.subject()).isEqualTo(TEST_SUBJECT);
        }
    }

    @Nested
    @DisplayName("Builder Access Tests / 构建器访问测试")
    class BuilderAccessTests {

        @Test
        @DisplayName("builder()返回JwtUtil.Builder")
        void testBuilder() {
            JwtUtil.Builder builder = OpenJwt.builder();
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("builder()可用于完整JWT构建")
        void testBuilderFullUsage() {
            String token = OpenJwt.builder()
                    .algorithm(JwtAlgorithm.HS256)
                    .secret(SECRET)
                    .subject(TEST_SUBJECT)
                    .issuer(TEST_ISSUER)
                    .expiresIn(Duration.ofHours(1))
                    .claim("role", "admin")
                    .sign();

            JwtClaims claims = OpenJwt.verify(token, SECRET);
            assertThat(claims.subject()).isEqualTo(TEST_SUBJECT);
            assertThat(claims.getString("role")).isEqualTo("admin");
        }

        @Test
        @DisplayName("claims()返回JwtClaims.Builder")
        void testClaims() {
            JwtClaims.Builder builder = OpenJwt.claims();
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("claims()可用于构建JwtClaims")
        void testClaimsFullUsage() {
            JwtClaims claims = OpenJwt.claims()
                    .subject(TEST_SUBJECT)
                    .issuer(TEST_ISSUER)
                    .expiresIn(Duration.ofHours(1))
                    .build();

            assertThat(claims.subject()).isEqualTo(TEST_SUBJECT);
            assertThat(claims.issuer()).isEqualTo(TEST_ISSUER);
        }
    }

    @Nested
    @DisplayName("Null Parameter Tests / 空参数测试")
    class NullParameterTests {

        @Test
        @DisplayName("sign()使用null claims抛出异常")
        void testSignNullClaimsThrows() {
            assertThatThrownBy(() -> OpenJwt.sign(null, SECRET, JwtAlgorithm.HS256))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("claims must not be null");
        }

        @Test
        @DisplayName("sign()使用null secret抛出异常")
        void testSignNullSecretThrows() {
            JwtClaims claims = JwtClaims.builder().subject(TEST_SUBJECT).build();
            assertThatThrownBy(() -> OpenJwt.sign(claims, null, JwtAlgorithm.HS256))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("secret must not be null");
        }

        @Test
        @DisplayName("signRsa()使用null claims抛出异常")
        void testSignRsaNullClaimsThrows() {
            assertThatThrownBy(() -> OpenJwt.signRsa(null, rsaKeyPair.getPrivate(), JwtAlgorithm.RS256))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("claims must not be null");
        }

        @Test
        @DisplayName("signRsa()使用null privateKey抛出异常")
        void testSignRsaNullPrivateKeyThrows() {
            JwtClaims claims = JwtClaims.builder().subject(TEST_SUBJECT).build();
            assertThatThrownBy(() -> OpenJwt.signRsa(claims, null, JwtAlgorithm.RS256))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("privateKey must not be null");
        }

        @Test
        @DisplayName("signEc()使用null claims抛出异常")
        void testSignEcNullClaimsThrows() {
            assertThatThrownBy(() -> OpenJwt.signEc(null, ecKeyPair.getPrivate(), JwtAlgorithm.ES256))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("claims must not be null");
        }

        @Test
        @DisplayName("signEc()使用null privateKey抛出异常")
        void testSignEcNullPrivateKeyThrows() {
            JwtClaims claims = JwtClaims.builder().subject(TEST_SUBJECT).build();
            assertThatThrownBy(() -> OpenJwt.signEc(claims, null, JwtAlgorithm.ES256))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("privateKey must not be null");
        }
    }

    @Nested
    @DisplayName("End-to-End Tests / 端到端测试")
    class EndToEndTests {

        @Test
        @DisplayName("完整的HMAC JWT流程")
        void testCompleteHmacFlow() {
            // Create claims
            JwtClaims claims = OpenJwt.claims()
                    .subject("user@example.com")
                    .issuer("my-app")
                    .audience("api-service")
                    .issuedAtNow()
                    .expiresIn(Duration.ofMinutes(30))
                    .generateJwtId()
                    .claim("role", "admin")
                    .claim("permissions", java.util.List.of("read", "write"))
                    .build();

            // Sign
            String token = OpenJwt.sign(claims, SECRET);

            // Verify
            JwtClaims verified = OpenJwt.verify(token, SECRET);

            // Assert all claims
            assertThat(verified.subject()).isEqualTo("user@example.com");
            assertThat(verified.issuer()).isEqualTo("my-app");
            assertThat(verified.audience()).containsExactly("api-service");
            assertThat(verified.issuedAt()).isNotNull();
            assertThat(verified.expiration()).isNotNull();
            assertThat(verified.jwtId()).isNotNull();
            assertThat(verified.getString("role")).isEqualTo("admin");
            assertThat(verified.<String>getList("permissions")).containsExactly("read", "write");
        }

        @Test
        @DisplayName("完整的RSA JWT流程")
        void testCompleteRsaFlow() {
            // Generate key pair
            KeyPair keyPair = OpenJwt.generateRsaKeyPair();

            // Create claims
            JwtClaims claims = OpenJwt.claims()
                    .subject("user@example.com")
                    .issuer("secure-app")
                    .expiresIn(Duration.ofHours(24))
                    .claim("scope", "full")
                    .build();

            // Sign with private key
            String token = OpenJwt.signRsa(claims, keyPair.getPrivate());

            // Verify with public key
            JwtClaims verified = OpenJwt.verify(token, keyPair.getPublic());

            // Assert
            assertThat(verified.subject()).isEqualTo("user@example.com");
            assertThat(verified.issuer()).isEqualTo("secure-app");
            assertThat(verified.getString("scope")).isEqualTo("full");
        }

        @Test
        @DisplayName("完整的ECDSA JWT流程")
        void testCompleteEcdsaFlow() {
            // Generate key pair
            KeyPair keyPair = OpenJwt.generateEcKeyPair();

            // Create claims
            JwtClaims claims = OpenJwt.claims()
                    .subject("device-12345")
                    .issuer("iot-gateway")
                    .expiresIn(Duration.ofDays(30))
                    .claim("device_type", "sensor")
                    .build();

            // Sign with private key
            String token = OpenJwt.signEc(claims, keyPair.getPrivate());

            // Verify with public key
            JwtClaims verified = OpenJwt.verify(token, keyPair.getPublic());

            // Assert
            assertThat(verified.subject()).isEqualTo("device-12345");
            assertThat(verified.issuer()).isEqualTo("iot-gateway");
            assertThat(verified.getString("device_type")).isEqualTo("sensor");
        }
    }
}
