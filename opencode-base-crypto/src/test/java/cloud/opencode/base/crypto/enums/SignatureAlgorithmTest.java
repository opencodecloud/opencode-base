package cloud.opencode.base.crypto.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * SignatureAlgorithm 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("SignatureAlgorithm 测试")
class SignatureAlgorithmTest {

    @Nested
    @DisplayName("getAlgorithmName 测试")
    class GetAlgorithmNameTests {

        @Test
        @DisplayName("Ed25519算法名")
        void testEd25519AlgorithmName() {
            assertThat(SignatureAlgorithm.ED25519.getAlgorithmName()).isEqualTo("Ed25519");
        }

        @Test
        @DisplayName("Ed448算法名")
        void testEd448AlgorithmName() {
            assertThat(SignatureAlgorithm.ED448.getAlgorithmName()).isEqualTo("Ed448");
        }

        @Test
        @DisplayName("ECDSA-P256-SHA256算法名")
        void testEcdsaP256Sha256AlgorithmName() {
            assertThat(SignatureAlgorithm.ECDSA_P256_SHA256.getAlgorithmName())
                    .isEqualTo("SHA256withECDSA");
        }

        @Test
        @DisplayName("RSA-SHA256算法名")
        void testRsaSha256AlgorithmName() {
            assertThat(SignatureAlgorithm.RSA_SHA256.getAlgorithmName())
                    .isEqualTo("SHA256withRSA");
        }

        @Test
        @DisplayName("RSA-PSS-SHA256算法名")
        void testRsaPssSha256AlgorithmName() {
            assertThat(SignatureAlgorithm.RSA_PSS_SHA256.getAlgorithmName())
                    .isEqualTo("SHA256withRSA/PSS");
        }

        @Test
        @DisplayName("SM2算法名")
        void testSm2AlgorithmName() {
            assertThat(SignatureAlgorithm.SM2.getAlgorithmName())
                    .isEqualTo("SM3withSM2");
        }
    }

    @Nested
    @DisplayName("getKeyAlgorithm 测试")
    class GetKeyAlgorithmTests {

        @Test
        @DisplayName("EdDSA密钥算法")
        void testEdDsaKeyAlgorithm() {
            assertThat(SignatureAlgorithm.ED25519.getKeyAlgorithm()).isEqualTo("EdDSA");
            assertThat(SignatureAlgorithm.ED448.getKeyAlgorithm()).isEqualTo("EdDSA");
        }

        @Test
        @DisplayName("ECDSA密钥算法")
        void testEcdsaKeyAlgorithm() {
            assertThat(SignatureAlgorithm.ECDSA_P256_SHA256.getKeyAlgorithm()).isEqualTo("EC");
            assertThat(SignatureAlgorithm.ECDSA_P384_SHA384.getKeyAlgorithm()).isEqualTo("EC");
            assertThat(SignatureAlgorithm.ECDSA_P521_SHA512.getKeyAlgorithm()).isEqualTo("EC");
        }

        @Test
        @DisplayName("RSA密钥算法")
        void testRsaKeyAlgorithm() {
            assertThat(SignatureAlgorithm.RSA_SHA256.getKeyAlgorithm()).isEqualTo("RSA");
            assertThat(SignatureAlgorithm.RSA_SHA384.getKeyAlgorithm()).isEqualTo("RSA");
            assertThat(SignatureAlgorithm.RSA_SHA512.getKeyAlgorithm()).isEqualTo("RSA");
            assertThat(SignatureAlgorithm.RSA_PSS_SHA256.getKeyAlgorithm()).isEqualTo("RSA");
        }

        @Test
        @DisplayName("SM2密钥算法")
        void testSm2KeyAlgorithm() {
            assertThat(SignatureAlgorithm.SM2.getKeyAlgorithm()).isEqualTo("SM2");
        }
    }

    @Nested
    @DisplayName("isRecommended 测试")
    class IsRecommendedTests {

        @Test
        @DisplayName("EdDSA算法被推荐")
        void testEdDsaRecommended() {
            assertThat(SignatureAlgorithm.ED25519.isRecommended()).isTrue();
            assertThat(SignatureAlgorithm.ED448.isRecommended()).isTrue();
        }

        @Test
        @DisplayName("RSA-PSS算法被推荐")
        void testRsaPssRecommended() {
            assertThat(SignatureAlgorithm.RSA_PSS_SHA256.isRecommended()).isTrue();
            assertThat(SignatureAlgorithm.RSA_PSS_SHA384.isRecommended()).isTrue();
            assertThat(SignatureAlgorithm.RSA_PSS_SHA512.isRecommended()).isTrue();
        }

        @Test
        @DisplayName("普通RSA签名不被推荐")
        void testRsaNotRecommended() {
            assertThat(SignatureAlgorithm.RSA_SHA256.isRecommended()).isFalse();
            assertThat(SignatureAlgorithm.RSA_SHA384.isRecommended()).isFalse();
            assertThat(SignatureAlgorithm.RSA_SHA512.isRecommended()).isFalse();
        }

        @Test
        @DisplayName("ECDSA不被推荐")
        void testEcdsaNotRecommended() {
            assertThat(SignatureAlgorithm.ECDSA_P256_SHA256.isRecommended()).isFalse();
            assertThat(SignatureAlgorithm.ECDSA_P384_SHA384.isRecommended()).isFalse();
            assertThat(SignatureAlgorithm.ECDSA_P521_SHA512.isRecommended()).isFalse();
        }

        @Test
        @DisplayName("SM2不被推荐")
        void testSm2NotRecommended() {
            assertThat(SignatureAlgorithm.SM2.isRecommended()).isFalse();
        }
    }

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("所有枚举值存在")
        void testAllValuesExist() {
            assertThat(SignatureAlgorithm.values()).hasSize(12);
        }

        @Test
        @DisplayName("valueOf返回正确枚举")
        void testValueOf() {
            assertThat(SignatureAlgorithm.valueOf("ED25519")).isEqualTo(SignatureAlgorithm.ED25519);
            assertThat(SignatureAlgorithm.valueOf("RSA_PSS_SHA256")).isEqualTo(SignatureAlgorithm.RSA_PSS_SHA256);
        }

        @Test
        @DisplayName("包含所有算法")
        void testContainsAllAlgorithms() {
            assertThat(SignatureAlgorithm.values()).contains(
                    SignatureAlgorithm.ED25519,
                    SignatureAlgorithm.ED448,
                    SignatureAlgorithm.ECDSA_P256_SHA256,
                    SignatureAlgorithm.ECDSA_P384_SHA384,
                    SignatureAlgorithm.ECDSA_P521_SHA512,
                    SignatureAlgorithm.RSA_SHA256,
                    SignatureAlgorithm.RSA_SHA384,
                    SignatureAlgorithm.RSA_SHA512,
                    SignatureAlgorithm.RSA_PSS_SHA256,
                    SignatureAlgorithm.RSA_PSS_SHA384,
                    SignatureAlgorithm.RSA_PSS_SHA512,
                    SignatureAlgorithm.SM2
            );
        }
    }
}
