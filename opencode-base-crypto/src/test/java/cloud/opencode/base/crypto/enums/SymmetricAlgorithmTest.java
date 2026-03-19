package cloud.opencode.base.crypto.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * SymmetricAlgorithm 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("SymmetricAlgorithm 测试")
class SymmetricAlgorithmTest {

    @Nested
    @DisplayName("getTransformation 测试")
    class GetTransformationTests {

        @Test
        @DisplayName("AES-GCM-128转换字符串")
        void testAesGcm128Transformation() {
            assertThat(SymmetricAlgorithm.AES_GCM_128.getTransformation())
                    .isEqualTo("AES/GCM/NoPadding");
        }

        @Test
        @DisplayName("AES-GCM-256转换字符串")
        void testAesGcm256Transformation() {
            assertThat(SymmetricAlgorithm.AES_GCM_256.getTransformation())
                    .isEqualTo("AES/GCM/NoPadding");
        }

        @Test
        @DisplayName("AES-CBC-128转换字符串")
        void testAesCbc128Transformation() {
            assertThat(SymmetricAlgorithm.AES_CBC_128.getTransformation())
                    .isEqualTo("AES/CBC/PKCS5Padding");
        }

        @Test
        @DisplayName("AES-CTR-256转换字符串")
        void testAesCtr256Transformation() {
            assertThat(SymmetricAlgorithm.AES_CTR_256.getTransformation())
                    .isEqualTo("AES/CTR/NoPadding");
        }

        @Test
        @DisplayName("ChaCha20-Poly1305转换字符串")
        void testChaCha20Poly1305Transformation() {
            assertThat(SymmetricAlgorithm.CHACHA20_POLY1305.getTransformation())
                    .isEqualTo("ChaCha20-Poly1305");
        }

        @Test
        @DisplayName("SM4-GCM转换字符串")
        void testSm4GcmTransformation() {
            assertThat(SymmetricAlgorithm.SM4_GCM.getTransformation())
                    .isEqualTo("SM4/GCM/NoPadding");
        }
    }

    @Nested
    @DisplayName("getKeySize 测试")
    class GetKeySizeTests {

        @Test
        @DisplayName("AES-GCM-128密钥大小")
        void testAesGcm128KeySize() {
            assertThat(SymmetricAlgorithm.AES_GCM_128.getKeySize()).isEqualTo(128);
        }

        @Test
        @DisplayName("AES-GCM-256密钥大小")
        void testAesGcm256KeySize() {
            assertThat(SymmetricAlgorithm.AES_GCM_256.getKeySize()).isEqualTo(256);
        }

        @Test
        @DisplayName("AES-CBC-128密钥大小")
        void testAesCbc128KeySize() {
            assertThat(SymmetricAlgorithm.AES_CBC_128.getKeySize()).isEqualTo(128);
        }

        @Test
        @DisplayName("ChaCha20-Poly1305密钥大小256位")
        void testChaCha20Poly1305KeySize() {
            assertThat(SymmetricAlgorithm.CHACHA20_POLY1305.getKeySize()).isEqualTo(256);
        }

        @Test
        @DisplayName("SM4密钥大小128位")
        void testSm4KeySize() {
            assertThat(SymmetricAlgorithm.SM4_GCM.getKeySize()).isEqualTo(128);
            assertThat(SymmetricAlgorithm.SM4_CBC.getKeySize()).isEqualTo(128);
        }
    }

    @Nested
    @DisplayName("isAead 测试")
    class IsAeadTests {

        @Test
        @DisplayName("AES-GCM是AEAD")
        void testAesGcmIsAead() {
            assertThat(SymmetricAlgorithm.AES_GCM_128.isAead()).isTrue();
            assertThat(SymmetricAlgorithm.AES_GCM_256.isAead()).isTrue();
        }

        @Test
        @DisplayName("AES-CBC不是AEAD")
        void testAesCbcNotAead() {
            assertThat(SymmetricAlgorithm.AES_CBC_128.isAead()).isFalse();
            assertThat(SymmetricAlgorithm.AES_CBC_256.isAead()).isFalse();
        }

        @Test
        @DisplayName("AES-CTR不是AEAD")
        void testAesCtrNotAead() {
            assertThat(SymmetricAlgorithm.AES_CTR_128.isAead()).isFalse();
            assertThat(SymmetricAlgorithm.AES_CTR_256.isAead()).isFalse();
        }

        @Test
        @DisplayName("ChaCha20-Poly1305是AEAD")
        void testChaCha20Poly1305IsAead() {
            assertThat(SymmetricAlgorithm.CHACHA20_POLY1305.isAead()).isTrue();
        }

        @Test
        @DisplayName("SM4-GCM是AEAD")
        void testSm4GcmIsAead() {
            assertThat(SymmetricAlgorithm.SM4_GCM.isAead()).isTrue();
        }

        @Test
        @DisplayName("SM4-CBC不是AEAD")
        void testSm4CbcNotAead() {
            assertThat(SymmetricAlgorithm.SM4_CBC.isAead()).isFalse();
        }
    }

    @Nested
    @DisplayName("isRecommended 测试")
    class IsRecommendedTests {

        @Test
        @DisplayName("AEAD算法被推荐")
        void testAeadAlgorithmsRecommended() {
            assertThat(SymmetricAlgorithm.AES_GCM_128.isRecommended()).isTrue();
            assertThat(SymmetricAlgorithm.AES_GCM_256.isRecommended()).isTrue();
            assertThat(SymmetricAlgorithm.CHACHA20_POLY1305.isRecommended()).isTrue();
            assertThat(SymmetricAlgorithm.SM4_GCM.isRecommended()).isTrue();
        }

        @Test
        @DisplayName("非AEAD算法不被推荐")
        void testNonAeadAlgorithmsNotRecommended() {
            assertThat(SymmetricAlgorithm.AES_CBC_128.isRecommended()).isFalse();
            assertThat(SymmetricAlgorithm.AES_CBC_256.isRecommended()).isFalse();
            assertThat(SymmetricAlgorithm.AES_CTR_128.isRecommended()).isFalse();
            assertThat(SymmetricAlgorithm.AES_CTR_256.isRecommended()).isFalse();
            assertThat(SymmetricAlgorithm.SM4_CBC.isRecommended()).isFalse();
        }
    }

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("所有枚举值存在")
        void testAllValuesExist() {
            assertThat(SymmetricAlgorithm.values()).hasSize(9);
        }

        @Test
        @DisplayName("valueOf返回正确枚举")
        void testValueOf() {
            assertThat(SymmetricAlgorithm.valueOf("AES_GCM_256")).isEqualTo(SymmetricAlgorithm.AES_GCM_256);
            assertThat(SymmetricAlgorithm.valueOf("CHACHA20_POLY1305")).isEqualTo(SymmetricAlgorithm.CHACHA20_POLY1305);
        }

        @Test
        @DisplayName("包含所有算法")
        void testContainsAllAlgorithms() {
            assertThat(SymmetricAlgorithm.values()).contains(
                    SymmetricAlgorithm.AES_GCM_128,
                    SymmetricAlgorithm.AES_GCM_256,
                    SymmetricAlgorithm.AES_CBC_128,
                    SymmetricAlgorithm.AES_CBC_256,
                    SymmetricAlgorithm.AES_CTR_128,
                    SymmetricAlgorithm.AES_CTR_256,
                    SymmetricAlgorithm.CHACHA20_POLY1305,
                    SymmetricAlgorithm.SM4_GCM,
                    SymmetricAlgorithm.SM4_CBC
            );
        }
    }
}
