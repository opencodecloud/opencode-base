package cloud.opencode.base.crypto.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * DigestAlgorithm 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("DigestAlgorithm 测试")
class DigestAlgorithmTest {

    @Nested
    @DisplayName("getAlgorithmName 测试")
    class GetAlgorithmNameTests {

        @Test
        @DisplayName("MD5算法名")
        void testMd5AlgorithmName() {
            assertThat(DigestAlgorithm.MD5.getAlgorithmName()).isEqualTo("MD5");
        }

        @Test
        @DisplayName("SHA-1算法名")
        void testSha1AlgorithmName() {
            assertThat(DigestAlgorithm.SHA1.getAlgorithmName()).isEqualTo("SHA-1");
        }

        @Test
        @DisplayName("SHA-256算法名")
        void testSha256AlgorithmName() {
            assertThat(DigestAlgorithm.SHA256.getAlgorithmName()).isEqualTo("SHA-256");
        }

        @Test
        @DisplayName("SHA-512算法名")
        void testSha512AlgorithmName() {
            assertThat(DigestAlgorithm.SHA512.getAlgorithmName()).isEqualTo("SHA-512");
        }

        @Test
        @DisplayName("SHA3-256算法名")
        void testSha3_256AlgorithmName() {
            assertThat(DigestAlgorithm.SHA3_256.getAlgorithmName()).isEqualTo("SHA3-256");
        }

        @Test
        @DisplayName("SM3算法名")
        void testSm3AlgorithmName() {
            assertThat(DigestAlgorithm.SM3.getAlgorithmName()).isEqualTo("SM3");
        }

        @Test
        @DisplayName("BLAKE3算法名")
        void testBlake3AlgorithmName() {
            assertThat(DigestAlgorithm.BLAKE3.getAlgorithmName()).isEqualTo("BLAKE3");
        }
    }

    @Nested
    @DisplayName("getDigestLength 测试")
    class GetDigestLengthTests {

        @Test
        @DisplayName("MD5摘要长度128位")
        void testMd5DigestLength() {
            assertThat(DigestAlgorithm.MD5.getDigestLength()).isEqualTo(128);
        }

        @Test
        @DisplayName("SHA-1摘要长度160位")
        void testSha1DigestLength() {
            assertThat(DigestAlgorithm.SHA1.getDigestLength()).isEqualTo(160);
        }

        @Test
        @DisplayName("SHA-256摘要长度256位")
        void testSha256DigestLength() {
            assertThat(DigestAlgorithm.SHA256.getDigestLength()).isEqualTo(256);
        }

        @Test
        @DisplayName("SHA-512摘要长度512位")
        void testSha512DigestLength() {
            assertThat(DigestAlgorithm.SHA512.getDigestLength()).isEqualTo(512);
        }

        @Test
        @DisplayName("SHA-224摘要长度224位")
        void testSha224DigestLength() {
            assertThat(DigestAlgorithm.SHA224.getDigestLength()).isEqualTo(224);
        }

        @Test
        @DisplayName("SHA-384摘要长度384位")
        void testSha384DigestLength() {
            assertThat(DigestAlgorithm.SHA384.getDigestLength()).isEqualTo(384);
        }

        @Test
        @DisplayName("BLAKE2B-512摘要长度512位")
        void testBlake2b512DigestLength() {
            assertThat(DigestAlgorithm.BLAKE2B_512.getDigestLength()).isEqualTo(512);
        }
    }

    @Nested
    @DisplayName("isSecure 测试")
    class IsSecureTests {

        @Test
        @DisplayName("MD5不安全")
        void testMd5NotSecure() {
            assertThat(DigestAlgorithm.MD5.isSecure()).isFalse();
        }

        @Test
        @DisplayName("SHA-1不安全")
        void testSha1NotSecure() {
            assertThat(DigestAlgorithm.SHA1.isSecure()).isFalse();
        }

        @Test
        @DisplayName("SHA-256安全")
        void testSha256Secure() {
            assertThat(DigestAlgorithm.SHA256.isSecure()).isTrue();
        }

        @Test
        @DisplayName("SHA-512安全")
        void testSha512Secure() {
            assertThat(DigestAlgorithm.SHA512.isSecure()).isTrue();
        }

        @Test
        @DisplayName("SHA3-256安全")
        void testSha3_256Secure() {
            assertThat(DigestAlgorithm.SHA3_256.isSecure()).isTrue();
        }

        @Test
        @DisplayName("SM3安全")
        void testSm3Secure() {
            assertThat(DigestAlgorithm.SM3.isSecure()).isTrue();
        }

        @Test
        @DisplayName("BLAKE3安全")
        void testBlake3Secure() {
            assertThat(DigestAlgorithm.BLAKE3.isSecure()).isTrue();
        }
    }

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("所有枚举值存在")
        void testAllValuesExist() {
            assertThat(DigestAlgorithm.values()).hasSize(14);
        }

        @Test
        @DisplayName("valueOf返回正确枚举")
        void testValueOf() {
            assertThat(DigestAlgorithm.valueOf("SHA256")).isEqualTo(DigestAlgorithm.SHA256);
            assertThat(DigestAlgorithm.valueOf("MD5")).isEqualTo(DigestAlgorithm.MD5);
            assertThat(DigestAlgorithm.valueOf("BLAKE3")).isEqualTo(DigestAlgorithm.BLAKE3);
        }

        @Test
        @DisplayName("包含所有主要算法")
        void testContainsMainAlgorithms() {
            assertThat(DigestAlgorithm.values()).contains(
                    DigestAlgorithm.MD5,
                    DigestAlgorithm.SHA1,
                    DigestAlgorithm.SHA224,
                    DigestAlgorithm.SHA256,
                    DigestAlgorithm.SHA384,
                    DigestAlgorithm.SHA512,
                    DigestAlgorithm.SHA3_224,
                    DigestAlgorithm.SHA3_256,
                    DigestAlgorithm.SHA3_384,
                    DigestAlgorithm.SHA3_512,
                    DigestAlgorithm.SM3,
                    DigestAlgorithm.BLAKE2B_256,
                    DigestAlgorithm.BLAKE2B_512,
                    DigestAlgorithm.BLAKE3
            );
        }
    }
}
