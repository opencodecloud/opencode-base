package cloud.opencode.base.crypto.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * AsymmetricAlgorithm 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("AsymmetricAlgorithm 测试")
class AsymmetricAlgorithmTest {

    @Nested
    @DisplayName("getTransformation 测试")
    class GetTransformationTests {

        @Test
        @DisplayName("RSA-OAEP-SHA256转换字符串")
        void testRsaOaepSha256Transformation() {
            assertThat(AsymmetricAlgorithm.RSA_OAEP_SHA256.getTransformation())
                    .isEqualTo("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        }

        @Test
        @DisplayName("RSA-OAEP-SHA384转换字符串")
        void testRsaOaepSha384Transformation() {
            assertThat(AsymmetricAlgorithm.RSA_OAEP_SHA384.getTransformation())
                    .isEqualTo("RSA/ECB/OAEPWithSHA-384AndMGF1Padding");
        }

        @Test
        @DisplayName("RSA-OAEP-SHA512转换字符串")
        void testRsaOaepSha512Transformation() {
            assertThat(AsymmetricAlgorithm.RSA_OAEP_SHA512.getTransformation())
                    .isEqualTo("RSA/ECB/OAEPWithSHA-512AndMGF1Padding");
        }

        @Test
        @DisplayName("RSA-PKCS1转换字符串")
        void testRsaPkcs1Transformation() {
            assertThat(AsymmetricAlgorithm.RSA_PKCS1.getTransformation())
                    .isEqualTo("RSA/ECB/PKCS1Padding");
        }

        @Test
        @DisplayName("SM2转换字符串")
        void testSm2Transformation() {
            assertThat(AsymmetricAlgorithm.SM2.getTransformation()).isEqualTo("SM2");
        }
    }

    @Nested
    @DisplayName("getMinKeySize 测试")
    class GetMinKeySizeTests {

        @Test
        @DisplayName("RSA-OAEP最小密钥大小2048")
        void testRsaOaepMinKeySize() {
            assertThat(AsymmetricAlgorithm.RSA_OAEP_SHA256.getMinKeySize()).isEqualTo(2048);
            assertThat(AsymmetricAlgorithm.RSA_OAEP_SHA384.getMinKeySize()).isEqualTo(2048);
            assertThat(AsymmetricAlgorithm.RSA_OAEP_SHA512.getMinKeySize()).isEqualTo(2048);
        }

        @Test
        @DisplayName("RSA-PKCS1最小密钥大小2048")
        void testRsaPkcs1MinKeySize() {
            assertThat(AsymmetricAlgorithm.RSA_PKCS1.getMinKeySize()).isEqualTo(2048);
        }

        @Test
        @DisplayName("SM2最小密钥大小256")
        void testSm2MinKeySize() {
            assertThat(AsymmetricAlgorithm.SM2.getMinKeySize()).isEqualTo(256);
        }
    }

    @Nested
    @DisplayName("isRecommended 测试")
    class IsRecommendedTests {

        @Test
        @DisplayName("RSA-OAEP算法被推荐")
        void testRsaOaepRecommended() {
            assertThat(AsymmetricAlgorithm.RSA_OAEP_SHA256.isRecommended()).isTrue();
            assertThat(AsymmetricAlgorithm.RSA_OAEP_SHA384.isRecommended()).isTrue();
            assertThat(AsymmetricAlgorithm.RSA_OAEP_SHA512.isRecommended()).isTrue();
        }

        @Test
        @DisplayName("RSA-PKCS1不被推荐")
        void testRsaPkcs1NotRecommended() {
            assertThat(AsymmetricAlgorithm.RSA_PKCS1.isRecommended()).isFalse();
        }

        @Test
        @DisplayName("SM2被推荐")
        void testSm2Recommended() {
            assertThat(AsymmetricAlgorithm.SM2.isRecommended()).isTrue();
        }
    }

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("所有枚举值存在")
        void testAllValuesExist() {
            assertThat(AsymmetricAlgorithm.values()).hasSize(5);
        }

        @Test
        @DisplayName("valueOf返回正确枚举")
        void testValueOf() {
            assertThat(AsymmetricAlgorithm.valueOf("RSA_OAEP_SHA256")).isEqualTo(AsymmetricAlgorithm.RSA_OAEP_SHA256);
            assertThat(AsymmetricAlgorithm.valueOf("SM2")).isEqualTo(AsymmetricAlgorithm.SM2);
        }

        @Test
        @DisplayName("包含所有算法")
        void testContainsAllAlgorithms() {
            assertThat(AsymmetricAlgorithm.values()).contains(
                    AsymmetricAlgorithm.RSA_OAEP_SHA256,
                    AsymmetricAlgorithm.RSA_OAEP_SHA384,
                    AsymmetricAlgorithm.RSA_OAEP_SHA512,
                    AsymmetricAlgorithm.RSA_PKCS1,
                    AsymmetricAlgorithm.SM2
            );
        }
    }
}
